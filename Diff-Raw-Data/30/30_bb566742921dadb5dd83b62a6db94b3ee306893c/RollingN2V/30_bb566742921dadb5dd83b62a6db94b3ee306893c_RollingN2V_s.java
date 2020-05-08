 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.store.n2v;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.joe_e.Struct;
 import org.joe_e.file.Filesystem;
 import org.joe_e.file.InvalidFilenameException;
 import org.joe_e.var.Milestone;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Receiver;
 import org.waterken.archive.Archive;
 import org.waterken.archive.ArchiveOutput;
 import org.waterken.archive.n2v.N2V;
 import org.waterken.archive.n2v.N2VOutput;
 import org.waterken.store.DoesNotExist;
 import org.waterken.store.Store;
 import org.waterken.store.StoreMaker;
 import org.waterken.store.Update;
 
 /**
  * An {@link N2V} based {@link Store} implementation.
  */
 public final class
 RollingN2V extends Struct implements StoreMaker, Serializable {
     static private final long serialVersionUID = 1L;
     
     private final Receiver<Long> sleep;
     
     /**
      * Constructs an instance.
      */
     public
     RollingN2V(final Receiver<Long> sleep) {
         this.sleep = sleep;
     }
     
     // org.waterken.store.StoreMaker interface
     
     public Store
     apply(final Receiver<Promise<?>> background,
         final File parent, final File dir) {
         final File pending = Filesystem.file(dir, ".pending");
         final File committed = Filesystem.file(dir, ".committed");
         return new Store() {
             private int lastId = 0;                 // id of newest archive file
             private ArrayList<File> files = null;   // un-merged commit files
             private ArrayList<N2V> versions = null; // un-merged commit archives
 
             private Update active = null;           // Is an update in progress?
             private boolean mergeScheduled = false; // Is a merge scheduled?
             
             public void
             clean() throws IOException {
                 final File tmp = Filesystem.file(parent,".dead."+dir.getName());
                 if (dir.isDirectory()) {
                     rename(dir, tmp);
                 }
                 if (tmp.isDirectory()) {
                     deleteRecursively(tmp);
                 }
             }
             
             public synchronized Update
             update() throws DoesNotExist, IOException {
                 while (null != active) {
                     try {
                         wait();
                     } catch (final InterruptedException e) {
                         throw new InterruptedIOException();
                     }
                 }
                 
                 if (null == versions) {
                     try {
                         // recover from a previous incarnation
                         if (pending.isDirectory()) {
                             if (committed.isDirectory()) {
                                 deleteRecursively(committed);
                             }
                             deleteRecursively(pending);
                         } else if (committed.isDirectory()) {
                             renameAll(committed, dir);
                         }
     
                         // load the recovered persistent state
                         final File[] fs = dir.listFiles();
                         if (null == fs) { throw new IOException(); }
                         int n = 0;
                         for (int i = 0; i != fs.length; ++i) {
                             if (fs[i].getName().endsWith(".n2v") &&
                                     fs[i].isFile()) {
                                 fs[n++] = fs[i];
                             }
                         }
                         Arrays.sort(fs, 0, n, new Comparator<File>() {
                             public int
                             compare(final File a, final File b) {
                                 return id(a) - id(b);
                             }
                         });
                         files = new ArrayList<File>(n);
                         final ArrayList<N2V> prior = new ArrayList<N2V>(n);
                         for (int i = 0; i != n; ++i) {
                             files.add(fs[i]);
                             prior.add(N2V.open(fs[i]));
                         }
                         lastId = 0 != n ? id(fs[n - 1]) : 0;
                         
                         // mark the store as fully loaded
                         versions = prior;
                     } catch (final IOException e) {
                         if (!dir.isDirectory()) { throw new DoesNotExist(); }
                         throw e;
                     }
                 }
                 
                 // take ownership of the loaded archives
                 final ArrayList<N2V> prior = versions;
                 versions = null;
                 
                 // construct an update transaction
                 final Milestone<Boolean> committing = Milestone.make();
                 final Milestone<Boolean> mutated = Milestone.make();
                 final Milestone<ArchiveOutput> updates = Milestone.make();
                 final Object lock = this;
                 return active = new Update() {
                     
                     public void
                     close() {
                         synchronized (lock) {
                             if (this != active) { return; }
                             
                             active = null;
                             lock.notify();
                             committing.set(true);
                             if (updates.is()) {
                                 try {
                                     updates.get().close();
                                 } catch (final IOException e) {}
                             }
                         }
                     }
 
                     public InputStream
                     read(final String name) throws IOException {
                         if (this != active) { throw new AssertionError(); }
                         if (committing.is()) { throw new AssertionError(); }
                         
                         for (int i = prior.size(); 0 != i--;) {
                             final Archive.Entry r = prior.get(i).find(name);
                             if (null != r) { return r.open(); }
                         }
                         throw new FileNotFoundException();
                     }
                     
                     public OutputStream
                     write(final String filename) throws IOException {
                         if (this != active) { throw new AssertionError(); }
                         if (committing.is()) { throw new AssertionError(); }
                         if(!ok(filename)){throw new InvalidFilenameException();}
                         
                         if (!updates.is()) {
                             if (!mutated.is()) {
                                 mkdir(pending);
                                 mutated.set(true);
                             }
                             updates.set(new N2VOutput(writeNew(
                                 Filesystem.file(pending, name(++lastId)))));
                         }
                         return updates.get().append(filename);
                     }
                     
                     public Store
                     nest(final String filename) throws IOException {
                         if (this != active) { throw new AssertionError(); }
                         if (committing.is()) { throw new AssertionError(); }
                         
                         final String was = "." + filename + ".was";
                        if (filename.startsWith(".") || !ok(filename) ||
                            Filesystem.file(dir, was).isFile() ||
                             (mutated.is() &&
                                     Filesystem.file(pending, was).isFile())) {
                            throw new InvalidFilenameException();
                         }
                         if (!mutated.is()) {
                             mkdir(pending);
                             mutated.set(true);
                         }
                         final File child = Filesystem.file(pending, filename);
                         mkdir(child);
                         writeNew(Filesystem.file(pending, was)).close();
                         return apply(null, pending, child);
                     }
                     
                     public void
                     commit() throws IOException {
                         if (this != active) { throw new AssertionError(); }
                         if (committing.is()) { throw new AssertionError(); }
                         
                         committing.set(true);
                         if (mutated.is()) {
                             if (updates.is()) {
                                 updates.get().finish();
                                 updates.get().close();
                             }
                             markCommitted();
                             if (!prior.isEmpty()) {
                                 if (updates.is()) {
                                     final File f =
                                         Filesystem.file(dir, name(lastId));
                                     prior.add(N2V.open(f));
                                     files.add(f);
                                 }
                                 
                                 // give up ownership of the archives
                                 versions = prior;
                                 
                                 if (!mergeScheduled && null != background) {
                                     background.apply(new Promise<Void>() {
                                         public Void
                                         call() throws IOException {
                                             merge();
                                             return null;
                                         }
                                     });
                                     mergeScheduled = true;
                                 }
                             }
                         } else {
                             // give up ownership of the archives
                             versions = prior;
                         }
                     }
                 };
             }
             
             protected synchronized void
             merge() throws IOException {
                 while (null != active) {
                     try {
                         wait();
                     } catch (final InterruptedException e) {
                         throw new InterruptedIOException();
                     }
                 }
 
                 mergeScheduled = false;
                 if (null == versions) { return; }
                 
                 final int n = files.size();
                 int i = n - 1;
                 long sum = files.get(i).length();
                 for (; 0 != i; --i) {
                     final long l = files.get(i - 1).length();
                     if (l / 10 > sum) { break; }
                     sum += l;
                 }
                 if (n - i > 1) {
                 	try {
                         mkdir(pending);
                 	} catch (final IOException e) {
                 		if (dir.isDirectory()) { throw e; }
                 		return;	// database no longer exists, skip the merge
                 	}
                 	
                     // take ownership of the loaded archives
                     final ArrayList<N2V> prior = versions;
                     versions = null;
 
                     final String name = name(++lastId);
                     final FileOutputStream sout =
                         new FileOutputStream(Filesystem.file(pending, name));
                     final FileChannel out = sout.getChannel();
                     final List<N2V> sub = prior.subList(i, n);
                     N2V.merge(out, sub);
                     out.force(true);
                     out.close();
                     sout.close();
                     markCommitted();
                     
                     for (final N2V version : sub) { version.close(); }
                     final List<File> subFiles = files.subList(i, n);
                     for (final File file : subFiles) { file.delete(); }
                     sub.clear();
                     subFiles.clear();
                     final File f = Filesystem.file(dir, name);
                     prior.add(N2V.open(f));
                     files.add(f);
                     
                     // give up ownership of the archives
                     versions = prior;
                 }
             }
             
             private void
             markCommitted() throws IOException {
                 while (true) {
                     try {
                         rename(pending, committed);
                         break;
                     } catch (final IOException e) {
                         if (!pending.isDirectory()) { throw e; }
                         sleep.apply(100L);
                     }
                 }
                 renameAll(committed, dir);
             }
         };
     }
     
     static private String
     name(final int n) {
         final StringBuilder r=new StringBuilder(Integer.SIZE/4+".n2v".length());
         for (int i = Integer.SIZE; 0 != i;) {
             i -= 4;
             r.append(toHex((n >>> i) & 0x0F));
         }
         r.append(".n2v");
         return r.toString();
     }
     
     static private char
     toHex(final int b) { return (char)(b < 10 ? '0' + b : 'A' + (b - 10)); }
     
     static private int
     id(final File file) {
         final String name = file.getName();
         if (!name.endsWith(".n2v")) { throw new RuntimeException(); }
         if (name.length() != Integer.SIZE / 4 + ".n2v".length()) {
             throw new RuntimeException();
         }
         int r = 0;
         for (int i = 0; i != Integer.SIZE / 4; ++i) {
             r <<= 4;
             r |= fromHex(name.charAt(i));
         }
         return r;
     }
     
     static private int
     fromHex(final char c) {
         return c <= '9' && c >= '0'
             ? c - '0'
         : c <= 'F' && c >= 'A'
             ? c - 'A' + 10
         : c <= 'f' && c >= 'a'
             ? c - 'a' + 10
         : 1 / 0;
     }
     
     static private boolean
     ok(final String name)  {
         if (0 == name.length()) { return false; }
         for (int i = name.length(); i-- != 0;) {
             if (disallowed.indexOf(name.charAt(i)) != -1) { return false; }
         }
         return true;
     }
     
     // the rest is static implementation helpers for file I/O
     
     /**
      * Creates a file for writing.
      * @param file  file to create
      * @return opened output stream
      * @throws IOException    <code>file</code> could not be created
      */
     static private OutputStream
     writeNew(final File file) throws IOException {
         if (!file.createNewFile()) { throw new IOException(); }
         return new SynchedFileOutputStream(file);
     }
     
     /**
      * Makes a new directory.
      * @param dir   directory to make
      * @throws IOException  <code>dir</code> could not be created
      */
     static private void
     mkdir(final File dir) throws IOException {
         if (!dir.mkdir()) { throw new IOException(); }
     }
     
     static private void
     rename(final File from, final File to) throws IOException {
         if (!from.renameTo(to)) { throw new IOException(); }
     }
     
     /**
      * Renames the children of a source folder to a destination folder.
      * @param from  source folder
      * @param to    destination folder
      * @throws IOException  any I/O problem
      */
     static private void
     renameAll(final File from, final File to) throws IOException {
         final File[] e = from.listFiles(new FileFilter() {
             public boolean
             accept(final File child) {
                 final File dst = Filesystem.file(to, child.getName());
                 dst.delete();
                 return !child.renameTo(dst);
             }
         });
         if (null == e) { throw new IOException(from.toString()); }
         if (0 != e.length) { throw new IOException(e[0].toString()); }
         if (!from.delete()) { throw new IOException(); }
     }
 
     /**
      * Recursively deletes a filesystem entry.
      * @param file  entry to recursively delete
      * @throws IOException  any I/O problem
      */
     static private void
     deleteRecursively(final File file) throws IOException {
         if (file.isDirectory()) {
             final File[] e = file.listFiles(new FileFilter() {
                 public boolean
                 accept(final File child) {
                     try {
                         deleteRecursively(child);
                     } catch (final IOException e) { return true; }
                     return false;
                 }
             });
             if (null == e) { throw new IOException(file.toString()); }
             if (0 != e.length) { throw new IOException(e[0].toString()); }
         }
         if (!file.delete()) { throw new IOException(); }
     }
 }
