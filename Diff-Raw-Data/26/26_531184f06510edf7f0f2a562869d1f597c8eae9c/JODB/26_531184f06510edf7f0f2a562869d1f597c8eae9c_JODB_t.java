 // Copyright 2002-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.jos;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.joe_e.Immutable;
 import org.joe_e.JoeE;
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.URLEncoding;
 import org.joe_e.file.InvalidFilenameException;
 import org.joe_e.reflect.Reflection;
 import org.joe_e.var.Milestone;
 import org.ref_send.log.Event;
 import org.ref_send.promise.Fulfilled;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.eventual.NOP;
 import org.ref_send.promise.eventual.Receiver;
 import org.ref_send.promise.eventual.Task;
 import org.waterken.base32.Base32;
 import org.waterken.db.Creator;
 import org.waterken.db.CyclicGraph;
 import org.waterken.db.Effect;
 import org.waterken.db.ProhibitedCreation;
 import org.waterken.db.ProhibitedModification;
 import org.waterken.db.Root;
 import org.waterken.db.Service;
 import org.waterken.db.Transaction;
 import org.waterken.db.UnknownClass;
 import org.waterken.db.Database;
 import org.waterken.project.Project;
 import org.waterken.store.Sink;
 import org.waterken.store.Store;
 import org.waterken.store.Update;
 import org.waterken.trace.EventSender;
 import org.waterken.trace.Tracer;
 import org.waterken.trace.TurnCounter;
 import org.waterken.trace.project.ProjectTracer;
 
 /**
  * An object graph stored as a set of Java Object Serialization files.
  */
 /* package */ final class
 JODB<S> extends Database<S> {
     
     /**
      * Canonicalizes a {@link Root} name.
      * @param name  chosen name
      * @return canonical name
      */
     static private String
     canonicalize(final String name) {
         /*
          * All lower-case names are used to hide differences in how
          * different file systems handle case-sensitivity.
          */
         return name.toLowerCase(Locale.ENGLISH);
     }
 
     /**
      * file extension for a serialized Java object tree
      */
     static protected final String ext = ".jos";
 
     static private final int keyChars = 14;
     static private final int keyBytes = keyChars * 5 / 8 + 1;
 
     /**
      * Turns an object identifier into a filename.
      * @param id    object identifier
      * @return corresponding filename
      */
     static private String
     filename(final byte[] id) {
         return Base32.encode(id).substring(0, keyChars);
     }
 
     private final Receiver<Event> stderr;   // log event output
     private final Store store;              // byte storage
 
     protected
     JODB(final S session, final Receiver<Service> service,
          final Receiver<Event> stderr, final Store store) {
         super(session, service);
         this.stderr = stderr;
         this.store = store;
     }
     
     static private <S> Receiver<Object>
     makeDestructor(final Receiver<Effect<S>> effect) {
         class Destruct extends Struct implements Receiver<Object>, Serializable{
             static private final long serialVersionUID = 1L;
             
             public void
             run(final Object ignored) {
                 effect.run(new Effect<S>() {
                     public void
                     run(final Database<S> origin) {
                         while (true) {
                             try {
                                 ((JODB<?>)origin).store.clean();
                                 break;
                             } catch (final Exception e) {
                                 try {
                                     Thread.sleep(1000);
                                 } catch (final InterruptedException e1) {}
                             }
                         }
                     }
                 });
             }
         }
         return new Destruct();
     }
 
     // org.waterken.vat.Vat interface
     
     private String project;
     private ClassLoader code = JODB.class.getClassLoader();
     
     public synchronized String
     getProject() throws Exception {
         wake();
         return project;
     }
 
     public synchronized <R extends Immutable> Promise<R>
     enter(final boolean isQuery, final Transaction<R> body) throws Exception {
         wake();
         return process(isQuery, body);
     }
     
     /**
      * Has the {@link #wake wake} {@link Task} been run?
      */
     private final Milestone<Boolean> awake = Milestone.plan();
     
     private void
     wake() throws Exception {
         if (!awake.is()) {
             process(Transaction.query, new Transaction<Immutable>() {
                 public Immutable
                 run(final Root local) throws Exception {
                     final Task<?> wake = local.fetch(null, Database.wake);
                     if (null != wake) { wake.run(); }
                     return null;
                 }
             });
             awake.mark(true);
         }
     }
 
     /**
      * An object store entry.
      */
     static private final class
     Bucket extends Struct {
         final boolean created;      // Is this a newly created bucket?
         final Object value;         // contained object
         final ByteArray version;    // secure hash of serialization, or
                                     // <code>null</code> if not known
 
         Bucket(final boolean created, final Object value,
                final ByteArray version) {
             if (!created && null == version) { throw new AssertionError(); }
             
             this.created = created;
             this.value = value;
             this.version = version;
         }
         
         /**
          * Constructs a pseudo-persistent bucket.
          * <p>
          * Some buckets aren't actually persistent, but are reconstructed at
          * the start of each transaction.
          * </p>
          * @param value a pseudo-persistent object
          */
         Bucket(final Object value) {
             this(false, value, ByteArray.array());
         }
     }
 
     private SecureRandom prng;      // pseudo-random number generator
 
     protected <R extends Immutable> Promise<R>
     process(final boolean isQuery, final Transaction<R> body) throws Exception {
         final Update update = store.update();
 
         // setup tables to keep track of what's been loaded from disk
         // and what needs to be written out to disk
         final HashMap<String,Bucket> f2b =          // [ filename => bucket ]
             new HashMap<String,Bucket>(64);
         final IdentityHashMap<Object,String> o2f =  // [ object => filename ]
             new IdentityHashMap<Object,String>(64);
         final IdentityHashMap<Object,String> o2wf = // [ object=>weak filename ]
             new IdentityHashMap<Object,String>(4);
         final HashSet<String> xxx =                 // [ dirty filename ]
             new HashSet<String>(64);
         final Milestone<Boolean> externalStateAccessed = Milestone.plan();
         final Root root = new Root() {
 
             /**
              * Creates an object store entry.
              * <p>
              * Each entry is stored as a binding in the persistent store. The
              * corresponding filename is of the form XYZ.jos, where the XYZ
              * component may be chosen by the client and the ".jos" extension is
              * automatically appended by the implementation. All filenames are
              * composed of only lower case letters. For example,
              * ".stuff.jos.jos" is the filename corresponding to the user chosen
              * name ".Stuff.JOS". The file extension chosen by the user carries
              * no significance in this implementation, and neither does a
              * filename starting with a "." character.
              * </p>
              * <p>
              * The name generated by {@link #export} is considered the
              * canonical name for an object. Names assigned to an object via
              * {@link #link} are valid for lookup, but will not be returned by
              * {@link #export}. This feature is implemented by wrapping a
              * {@linkplain #link linked} object in a {@link SymbolicLink}, and
              * unwrapping it on {@link #fetch}.
              * </p>
              */
             public void
             link(final String name,
                  final Object value) throws InvalidFilenameException,
                                             ProhibitedModification {
                 if (isQuery) {
                     throw new ProhibitedModification(
                             Reflection.getName(Root.class));
                 }
 
                 final String filename = canonicalize(name);
                 final boolean exists;
                 try {
                     exists = f2b.containsKey(filename) ||
                              update.includes(filename + ext);
                 } catch (final Exception e) { throw new Error(e); }
                 if (exists) { throw new InvalidFilenameException(); }
                 f2b.put(filename,new Bucket(true,new SymbolicLink(value),null));
                 xxx.add(filename);
             }
 
 			public @SuppressWarnings("unchecked") <T> T
             fetch(final Object otherwise, final String name) {
                 final Bucket b = load(canonicalize(name));
                 return (T)(null == b
                     ? otherwise
                 : b.value instanceof SymbolicLink
                     ? ((SymbolicLink)b.value).target
                 : b.value);
             }
 
             /**
              * for detecting cyclic object graphs
              */
             private final Bucket pumpkin = new Bucket(null);
             private final ArrayList<String> stack = new ArrayList<String>(16);
 
             /**
              * Gets the corresponding bucket, loading from the store if needed.
              * @param filename  name of corresponding bucket
              * @return corresponding bucket, or <code>null</code> if none
              * @throws RuntimeException syntax problem with persistent state
              * @throws Error            I/O problem
              */
             private Bucket
             load(final String filename) throws RuntimeException {
                 Bucket bucket = f2b.get(filename);
                 if (null == bucket) {
                     f2b.put(filename, pumpkin);
                     stack.add(filename);
                     try {
                         bucket = read(filename);
                     } catch (final FileNotFoundException e) {
                         return null;
                     } finally {
                         f2b.remove(filename);
                         stack.remove(stack.size() - 1);
                     }
                     f2b.put(filename, bucket);
                     o2f.put(bucket.value, filename);
                     if (!JoeE.isFrozen(bucket.value)) { xxx.add(filename); }
                 } else if (pumpkin == bucket) {
                     final PowerlessArray.Builder<String> types =
                         PowerlessArray.builder(stack.size());
                     for (int i = stack.size(); 0 != i--;) {
                         final String at = stack.get(i);
                         final Class<?>[] type = { Object.class };
                         InputStream in = null;
                         try {
                             in = update.read(at + ext);
                             final SubstitutionStream oin =
                                     new SubstitutionStream(true, code, in) {
                                 protected Object
                                 resolveObject(Object x) {
                                     if (x instanceof Wrapper) {
                                         type[0] = x.getClass();
                                         x = null;
                                     }
                                     return x;
                                 }
                             };
                             in = oin;
                             final Object x = oin.readObject();
                             if (null != x) { type[0] = x.getClass(); }
                             types.append(Reflection.getName(type[0]));
                         } catch (final Exception e) {
                             // skip over broken bucket
                         } finally {
                             if(null!=in){try{in.close();}catch(IOException e){}}
                         }
 
                         if (filename.equals(at)) { break; }
                     }
                     throw new CyclicGraph(types.snapshot());
                 }
                 return bucket;
             }
 
             /**
              * Reads a stored object graph.
              * @param filename  name of bucket to read
              * @return corresponding bucket
              * @throws FileNotFoundException    no corresponding bucket
              * @throws RuntimeException syntax problem with persistent state
              * @throws Error            I/O problem
              */
             private Bucket
             read(final String filename) throws FileNotFoundException {
                 InputStream in = null;
                 try {
                     in = update.read(filename + ext);
                     final Mac mac;
                     if (canonicalize(JODB.secret).equals(filename)) {
                         mac = null; // stop recursion in loading master secret
                     } else {
                         mac = allocMac(this);
                         in = new MacInputStream(in, mac);
                     }
                     final Root loader = this;
                     final SubstitutionStream oin =
                             new SubstitutionStream(true, code, in) {
                         protected Object
                         resolveObject(Object x) throws IOException {
                             if (x instanceof File) {
                                 externalStateAccessed.mark(true);
                             } else if (x instanceof Wrapper) {
                                 x = ((Wrapper)x).peel(loader);
                             }
                             return x;
                         }
                     };
                     in = oin;
                     final Object value = oin.readObject();
                     in.close();
                     in = null;
                     if (null == mac) { return new Bucket(value); }
                     final byte[] version = mac.doFinal();
                     freeMac(mac);
                     return new Bucket(false, value, ByteArray.array(version));
                 } catch (final FileNotFoundException e) {
                     throw e;
                 } catch (final ClassNotFoundException e) {
                     throw new UnknownClass(e.getMessage());
                 } catch (final IOException e) {
                     throw new Error(e);
                 } catch (final RuntimeException e) {
                     throw e;
                 } catch (final Exception e) {
                     throw new RuntimeException(e.getMessage(), e);
                 } finally {
                     if (null != in) {try{in.close();}catch(final Exception e){}}
                 }
             }
 
             public String
             export(final Object value, final boolean isWeak) {
                 String filename = o2f.get(value);
                 if (null != filename) { return filename; }
                 if (null == value || Slicer.inline(value.getClass())) {
                     // reuse an equivalent persistent identity;
                     // otherwise, determine the persistent identity
                     final byte[] rawVersion;
                     try {
                         final Mac mac = allocMac(this);
                         final Slicer out = new Slicer(value, this,
                                 new MacOutputStream(new Sink(), mac));
                         out.writeObject(value);
                         out.flush();
                         out.close();
                         rawVersion = mac.doFinal();
                         freeMac(mac);
                     } catch (final Exception e) { throw new Error(e); }
                     final ByteArray version = ByteArray.array(rawVersion);
                     final byte[] id = new byte[keyBytes];
                     System.arraycopy(rawVersion, 0, id, 0, id.length);
                     while (true) {
                         filename = filename(id);
                         try {
                             final Bucket b = load(filename);
                             if (null == b) {
                                 f2b.put(filename,
                                         new Bucket(true, value, version));
                                 o2f.put(value, filename);
                                 xxx.add(filename);
                                 break;
                             }
                             if (version.equals(b.version)) { break; }
                         } catch (final Exception e) { /*skip broken bucket*/ }
                         for (int i = 0; i != id.length && 0 == ++id[i]; ++i) {}
                     }
                     return filename;
                 }
                 
                 // to support caching of query responses, forbid
                 // export of selfish state in a query transaction
                 if (isQuery) {
                     throw new ProhibitedCreation(
                         Reflection.getName(value.getClass()));
                 }
                 
                 // check for an existing weak identity
                 filename = o2wf.get(value);
                 if (null != filename) {
                     if (!isWeak) {
                         o2wf.remove(value);
                         o2f.put(value, filename);
                         xxx.add(filename);
                     }
                     return filename;
                 }
 
                 // assign a new persistent identity
                 try {
                     do {
                         final byte[] id = new byte[keyBytes];
                         prng.nextBytes(id);
                         filename = filename(id);
                     } while (f2b.containsKey(filename) ||
                              update.includes(filename + ext));
                 } catch (final Exception e) { throw new Error(e); }
                 
                 // persist the persistent identity
                 if (isWeak) {
                     o2wf.put(value, filename);
                 } else {
                     o2f.put(value, filename);
                     xxx.add(filename);
                 }
                 f2b.put(filename, new Bucket(true, value, null));
                 return filename;
             }
         };
         final Task<String> tagger = new Task<String>() {
             public String
             run() throws Exception {
                 if (externalStateAccessed.is()) { throw new Exception(); }
 
                 final Mac mac = allocMac(root);
                 for (final Bucket b : f2b.values()) {
                     if (!b.created) { mac.update(b.version.toByteArray()); }
                 }
                 if (code instanceof Project) {
                     // include code timestamp in the ETag
                     final long buffer = ((Project)code).timestamp;
                     for (int i = Long.SIZE; i != 0;) {
                         i -= Byte.SIZE;
                         mac.update((byte)(buffer >>> i));
                     }
                 }
                 final byte[] id = mac.doFinal();
                 freeMac(mac);
                 return '\"' + Base32.encode(id).substring(0, 2*keyChars) + '\"';
             }
         };
         final LinkedList<Service> services = new LinkedList<Service>();
         final Receiver<Effect<S>> effect = new Receiver<Effect<S>>() {
             public void
             run(final Effect<S> task) {
                 services.add(new Service() {
                     public Void
                     run() throws Exception {
                         task.run(JODB.this);
                         return null;
                     }
                 });
             }
         };
         final Creator creator = new Creator() {
             public <X extends Immutable> Promise<X>
             run(final String project, final String base, String name,
                 final Transaction<X> setup) throws InvalidFilenameException,
                                                    ProhibitedModification {
                 if (isQuery) {
                     throw new ProhibitedModification(
                             Reflection.getName(Creator.class));
                 }
 
                 Store subStore;
                 if (null != name) {
                     name = canonicalize(name);
                     try {
                         subStore = update.nest(name);
                     } catch (final InvalidFilenameException e) {
                         throw e;
                     } catch (final Exception e) { throw new Error(e); }
                 } else {
                     while (true) {
                         try {
                             final byte[] d = new byte[4];
                             prng.nextBytes(d);
                             name = Base32.encode(d).substring(0, 6);
                             subStore = update.nest(name);
                             break;
                         } catch (final InvalidFilenameException e) {
                         } catch (final Exception e) { throw new Error(e); }
                     }
                 }
                 try {
                     final String here = base + URLEncoding.encode(name) + "/";
                     final byte[] bits = new byte[128 / Byte.SIZE];
                     prng.nextBytes(bits);
                     final ByteArray secretBits = ByteArray.array(bits);
                     final JODB<S> sub = new JODB<S>(null, null, null == stderr
                         ? new Receiver<Event>() {
                             public void
                             run(final Event event) {}
                         } : stderr, subStore);
                     sub.project = null != project ? project : JODB.this.project;
                     sub.code = Project.connect(sub.project);
                     return sub.process(Transaction.update, new Transaction<X>(){
                         public X
                         run(final Root local) throws Exception {
                             local.link(Database.project, sub.project);
                             local.link(Database.here, here);
                             local.link(secret, secretBits);
                             final TurnCounter turn = TurnCounter.make(here);
                             local.link(JODB.flip, turn.flip);
                             final ClassLoader code =
                                 local.fetch(null, Database.code);
                             final Tracer tracer = ProjectTracer.make(code, 2);
                             final Receiver<Effect<S>> effect =
                                 local.fetch(null, Database.effect);
                             final Receiver<Event> txerr =
                                 local.fetch(null, JODB.txerr);
                             local.link(Database.destruct, EventSender.
                                 makeDestructor(makeDestructor(effect), txerr,
                                                turn.mark, tracer));
                             local.link(Database.log,
                                 EventSender.makeLog(txerr, turn.mark, tracer));
                             return setup.run(local);
                         }
                     });
                 } catch (final Exception e) { throw new Error(e); }
             }
         };
         final LinkedList<Event> events = new LinkedList<Event>();
         final Receiver<Event> txerr = new Receiver<Event>() {
             public void
             run(final Event event) { events.add(event); }
         };
         
         Promise<R> r;
         try {
             // finish Vat initialization, which was delayed to avoid doing
             // anything intensive while holding the global "live" lock
             if (null == prng) { prng = new SecureRandom(); }
             if (null == project) {
                 try {
                     project = root.fetch(null, Database.project);
                 } catch (final Exception e) { throw new Error(e); }
                 code = Project.connect(project);
             }
     
             // setup the pseudo-persistent objects
             f2b.put(canonicalize(Database.code),    new Bucket(code));
             f2b.put(canonicalize(Database.creator), new Bucket(creator));
             f2b.put(canonicalize(Database.effect),  new Bucket(effect));
             f2b.put(canonicalize(Database.nothing), new Bucket(null));
             f2b.put(canonicalize(Database.tagger),  new Bucket(tagger));
             f2b.put(canonicalize(JODB.txerr),       new Bucket(txerr));
             f2b.put(canonicalize(".root"),          new Bucket(root));
             if (null == stderr) {
                 // short-circuit the log implementation
                 f2b.put(canonicalize(Database.log), new Bucket(new NOP()));
             }
             for (final Map.Entry<String,Bucket> x : f2b.entrySet()) {
                 o2f.put(x.getValue().value, x.getKey());
             }
 
             // execute the transaction body
             try {
                 if (!isQuery) {
                     final Task<?> flip = root.fetch(null, JODB.flip);
                     if (null != flip) { flip.run(); }
                 }
                 r = Fulfilled.detach(body.run(root));
             } catch (final Exception e) {
                 r = new Rejected<R>(e);
             }
 
             // persist the modifications
             while (!xxx.isEmpty()) {
                 final Iterator<String> i = xxx.iterator();
                 final String filename = i.next();
                 final Bucket b = f2b.get(filename);
                 i.remove();
 
                 final OutputStream fout;
                 if (isQuery && !b.created) {
                     fout = new MacOutputStream(new Sink(), allocMac(root)) {
                         public void
                         close() throws IOException {
                             super.close();
                             final ByteArray v = ByteArray.array(mac.doFinal());
                             freeMac(mac);
                             if (!v.equals(b.version)) {
                                 throw new ProhibitedModification(
                                     Reflection.getName(null != b.value
                                         ? b.value.getClass() : Void.class));
                             }
                         }
                     };
                 } else {
                     fout = update.write(filename + ext);
                 }
                 final Slicer out = new Slicer(b.value, root, fout);
                 out.writeObject(b.value);
                 out.flush();
                 out.close();
             }
             update.commit();
         } catch (final Error e) {
             // allow the caller to recover from an aborted transaction
             if (e instanceof OutOfMemoryError) { System.gc(); }
             final Throwable cause = e.getCause();
             if (cause instanceof Exception) { throw (Exception)cause; }
             throw new Exception(e);
         } finally {
             update.close();
         }
         
         // output the log events for the committed transaction
         if (null != stderr) {
            while (!events.isEmpty()) { stderr.run(events.removeFirst()); }
         }
         
         // schedule any services
         if (null != service) {
            while (!services.isEmpty()) { service.run(services.removeFirst()); }
         }
         
         return r;
     }
     
     static private final String flip = ".flip";     // name of loop turn flipper
     static private final String txerr = ".txerr";   // name of tx event output
 
     /*
      * In testing, allocation of hash objects doubled serialization time, so I'm
      * keeping a pool of them. Sucky code is like cancer.
      */
     static private final String secret = ".secret"; // name of master MAC key
     private final ArrayList<Mac> macs = new ArrayList<Mac>();
     private SecretKeySpec master;                   // MAC key generation secret
 
     private Mac
     allocMac(final Root local) throws Exception {
         if (!macs.isEmpty()) { return macs.remove(macs.size() - 1); }
         if (null == master) {
             final ByteArray bits = local.fetch(null, secret);
             master = new SecretKeySpec(bits.toByteArray(), "HmacSHA256");
         }
         final Mac r = Mac.getInstance("HmacSHA256");
         r.init(master);
         return r;
     }
 
     private void
     freeMac(final Mac h) { macs.add(h); }
 }
