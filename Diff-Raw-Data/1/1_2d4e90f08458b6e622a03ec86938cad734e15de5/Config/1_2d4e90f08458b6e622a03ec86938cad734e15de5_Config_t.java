 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import static org.joe_e.array.ConstArray.array;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Type;
 import java.security.SecureRandom;
 
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.URLEncoding;
 import org.joe_e.file.Filesystem;
 import org.ref_send.promise.eventual.Sink;
 import org.waterken.cache.Cache;
 import org.waterken.http.MediaType;
 import org.waterken.id.Importer;
 import org.waterken.jos.JODBCache;
 import org.waterken.net.Execution;
 import org.waterken.project.Project;
 import org.waterken.remote.http.Browser;
 import org.waterken.syntax.Serializer;
 import org.waterken.syntax.json.JSONDeserializer;
 import org.waterken.syntax.json.JSONSerializer;
 import org.waterken.thread.Concurrent;
 import org.waterken.vat.Pool;
 import org.waterken.vat.Vat;
 
 /**
  * Server configuration.
  */
 public final class
 Config {
 
     private
     Config() {}
 
     // initialize bootstrap configuration from system properties
     static private final File configFolder;
     static private final ClassLoader code;
     static {
         try {
             configFolder = new File(Project.home, System.getProperty(
                 "waterken.config", "config")).getCanonicalFile();
             code = Project.connect("dns");
         } catch (final Exception e) { throw new Error(e.getMessage(), e); }
     }
     static public    final File keys= Filesystem.file(configFolder, "keys.jks");
     static protected final Pool vats = new JODBCache();
     
     /**
      * Prints a summary of the configuration information.
      * @param hostname  configured SSL hostname
      * @param err       output stream
      */
     static protected void
     summarize(final String hostname, final PrintStream err) throws Exception {
         err.println("hostname: <" + hostname + ">");
         err.println("config folder: <" + configFolder + ">");
     }
     
     /**
      * Gets the root database.
      * @throws Exception    any problem
      */
     static protected Vat
     vat() throws Exception {
         return vats.connect(read(File.class, "vatRootFolder"));
     }
 
     static protected final String ext = ".json";
     static protected final MediaType mime= new MediaType("application", "json");
     
     /**
      * Initializes a configuration setting.
      * @param name      setting name
      * @param value     setting value
      */
     static public void
     init(final String name, final Object value) {
         try {
             final File file = Filesystem.file(configFolder, name + ext);
             final OutputStream out = Filesystem.writeNew(file);
             new JSONSerializer().run(Serializer.render, browser.export,
                                      array(value)).writeTo(out);
             out.flush();
             out.close();
         } catch (final Exception e) { throw new Error(e.getMessage(), e); }
     }
 
     /**
      * Reads a configuration setting.
      * @param name  setting name
      * @return setting value, or <code>null</code> if not set
      */
     static public <T> T
     read(final Class<T> T, final String name) {
         final File file = Filesystem.file(configFolder, name + ext);
         if (!file.isFile()) { return null; }
         return (T)new ImporterX().run(T, file.toURI().toString());
     }
 
     static private   final Cache<File,Object> settings = Cache.make();
     static private   final LastModified tag = new LastModified();
     static protected final Execution exe = new Execution() {
         public void
         sleep(final long ms) throws InterruptedException { Thread.sleep(ms); }
         
         public void
         yield() { Thread.yield(); }
     };
     static {
         settings.put(Filesystem.file(configFolder, "vats" + ext), vats);
         settings.put(Filesystem.file(configFolder, "tag" + ext), tag);
     }
     
     static public final Browser browser = Browser.make(
         new Proxy(), new SecureRandom(), code,
         Concurrent.loop(Thread.currentThread().getThreadGroup(), "config"),
         new Sink());
 
     /**
      * Reads configuration files.
      */
     static private final class
     ImporterX extends Struct implements Importer {
         public Object
         run(final Class<?> type, final String URL) {
             if ("file:".regionMatches(true, 0, URL, 0, "file:".length())) {
                 try {
                     String filename = URL.substring("file:".length());
                     filename = filename.replace('/', File.separatorChar);
                     filename = URLEncoding.decode(filename);
                     final File file = new File(filename).getCanonicalFile();
                     if (!file.getName().endsWith(ext)) { return file; }
                     final Token pumpkin = new Token();
                     final Object found = settings.fetch(pumpkin, file);
                     if (pumpkin != found) { return found; }
                     final InputStream in = Filesystem.read(file);
                     final Object r = new JSONDeserializer().run(
                         file.toURI().toString(), this, code, mime, in,
                         PowerlessArray.array((Type)type)).get(0);
                     in.close();
                     settings.put(file, r);
                     return r;
                 } catch (final Exception e) {
                     e.printStackTrace();
                     throw new Error(e.getMessage(), e);
                 }
             }
             return browser.connect.run(type, URL);
         }
     }
 }
