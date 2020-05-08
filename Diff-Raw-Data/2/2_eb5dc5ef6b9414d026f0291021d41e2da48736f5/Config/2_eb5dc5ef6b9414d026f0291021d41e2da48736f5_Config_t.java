 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax.config;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.file.Filesystem;
 import org.waterken.syntax.Exporter;
 import org.waterken.syntax.Importer;
 import org.waterken.syntax.Syntax;
 import org.waterken.syntax.json.JSONDeserializer;
 import org.waterken.syntax.json.JSONSerializer;
 
 /**
  * A folder of serialized configuration settings.
  * <p>
  * This class provides convenient access to a folder of JSON files; each of
  * which represents a particular configuration setting. The class provides
  * methods for {@linkplain #init initializing} and {@linkplain #read reading}
  * these settings.
  * </p>
  * <p>
  * For example, consider a folder with contents:
  * </p>
  * <pre>
  * config/
  *     - username.json
  *         { "=" : "tyler.close" }
  *     - port.json
  *         { "=" : 8088 }
  *     - home.json
  *         {
 *             "class" : [ "org.example.hypertext.Anchor" ],
  *             "icon" : "home.png",
  *             "href" : "http://waterken.sourceforge.net/",
  *             "tooltip" : "Home page"
  *         }
  * </pre>
  * <p>
  * These settings can be read with code:
  * </p>
  * <pre>
  * final Config config = new Config(new File("config/"),
  *                                  getClass().getClassLoader());
  * final String username = config.read("username");
  * final int port = config.read("port");
  * final Anchor home = config.read("home");
  * </pre>
  */
 public final class
 Config {
     
     /**
      * JSON syntax
      */
     static private final Syntax json =
         new Syntax(".json", new JSONSerializer(), new JSONDeserializer());
     
     /**
      * each known syntax
      */
     static private final PowerlessArray<Syntax> known = PowerlessArray.array(
         json
     );
 
     private final File root;
     private final ClassLoader code;
     private final String baseURI;
     private final Importer connect;
     private final Exporter export;
     private final PowerlessArray<Syntax> supported;
     private final Syntax output;
     
     private       PowerlessArray<String> cacheKeys;
     private       ConstArray<Object> cacheValues;
     
     /**
      * Constructs an instance.
      * @param root      root folder for configuration files
      * @param code      class loader for serialized objects
      * @param baseURI   base URI for all referenced objects
      * @param connect   remote reference importer, may be <code>null</code>
      * @param export    remote reference exporter, may be <code>null</code>
      * @param supported each supported {@linkplain #read input} syntax
      * @param output    {@linkplain #init output} syntax
      */
     public
     Config(final File root, final ClassLoader code,
            final String baseURI, final Importer connect, final Exporter export, 
            final PowerlessArray<Syntax> supported, final Syntax output) {
         this.root = root;
         this.code = code;
         this.baseURI = baseURI;
         this.connect = connect;
         this.export = export;
         this.supported = supported;
         this.output = output;
         
         cacheKeys = PowerlessArray.array(new String[] {});
         cacheValues = ConstArray.array(new Object[] {});
     }
     
     /**
      * Constructs an instance.
      * @param root      root folder for configuration files
      * @param code      class loader for serialized objects
      * @param baseURI   base URI for all referenced objects
      * @param connect   remote reference importer, may be <code>null</code>
      * @param export    remote reference exporter, may be <code>null</code>
      */
     public
     Config(final File root, final ClassLoader code,
            final String baseURI, final Importer connect, final Exporter export){
         this(root, code, baseURI, connect, export, known, json);
     }
     
     /**
      * Constructs an instance.
      * @param root      root folder for configuration files
      * @param code      class loader for serialized objects
      */
     public
     Config(final File root, final ClassLoader code) {
         this(root, code, "file:///", null, null);
     }
     
     /**
      * Reads a configuration setting.
      * @param <T>   expected value type
      * @param name  setting name
      * @return <code>{@link #read(String, Type) read}(name, Object.class)</code>
      * @throws Exception    any problem connecting to the identified reference
      */
     public @SuppressWarnings("unchecked") <T> T
     read(final String name) throws Exception {
         final Object r = read(name, Object.class);
         return (T)r;
     }
     
     /**
      * Reads a configuration setting.
      * <p>
      * Any <code>name</code> argument containing a period is assumed to refer
      * to a configuration file, rather than the serialized object. For example:
      * </p>
      * <pre>
      * final Config config = &hellip;
      * final String username = config.read("username");
      * final File usernameFile = config.read("username.json");
      * </pre>
      * <p>
      * The configuration folder itself can be accessed using the code:
      * </p>
      * <pre>
      * final File root = config.read("");
      * </pre>
      * @param <T> expected value type
      * @param name setting name
      * @param type expected value type, used as a hint to help deserialization
      * @return setting value, or <code>null</code> if not set
      * @throws Exception any problem connecting to the identified reference
      */
     public @SuppressWarnings("unchecked") <T> T
     read(final String name, final Type type) throws Exception {
         return (T)sub(root, baseURI).apply(name, baseURI, type);
     }
     
     private Importer
     sub(final File root, final String baseURI) { return new Importer() {
         public Object
         apply(final String href, final String base,
                                final Type type) throws Exception {
             if (!baseURI.equals(base) || -1 != href.indexOf(':')) {
                 return connect.apply(href, base, type);
             }
             
             // check the cache
             String path = baseURI.substring(0, baseURI.lastIndexOf('/') + 1);
             final String key = path + href; {
                 for (int i = cacheKeys.length(); 0 != i--;) {
                     if (cacheKeys.get(i).equals(key)) {
                         return cacheValues.get(i);
                     }
                 }
             }
 
             // descend to the named file
             File folder = root;     // sub-folder containing file
             String name = href;     // filename
             while (true) {
                 final int i = name.indexOf('/');
                 if (-1 == i) { break; }
                 folder = Filesystem.file(folder, name.substring(0, i));
                 path += name.substring(0, i + 1);
                 name = name.substring(i + 1);
             }
             if ("".equals(name)) { return folder; }
             if (-1 != name.indexOf('.')) {return Filesystem.file(folder, name);}
 
             // deserialize the named object
             Object r = null;
             for (final Syntax syntax : supported) {
                 final String filename = name + syntax.ext;
                 final File file = Filesystem.file(folder, filename);
                 if (!file.isFile()) { continue; }
                 final String subBaseURI = path + filename;
                 r = syntax.deserializer.deserialize(subBaseURI,
                     sub(folder, subBaseURI), type, code, Filesystem.read(file));
                 break;
             }
             cacheKeys = cacheKeys.with(key);
             cacheValues = cacheValues.with(r);
             return r;
         }
     }; }
     
     /**
      * Initializes a configuration setting.
      * @param name  setting name
      * @param value setting value
      * @throws Exception    any problem persisting the <code>value</code>
      */
     public void
     init(final String name, final Object value) throws Exception {
         init(name, value, export);
     }
     
     /**
      * Initializes a configuration setting.
      * @param name      setting name
      * @param value     setting value
      * @param export    remote reference exporter, may be <code>null</code>
      * @throws Exception    any problem persisting the <code>value</code>
      */
     public void
     init(final String name, final Object value,
                             final Exporter export) throws Exception {
         final ByteArray content =
             output.serializer.serialize(export, Object.class, value);
         final OutputStream out =
             Filesystem.writeNew(Filesystem.file(root, name + output.ext));
         out.write(content.toByteArray());
         out.flush();
         out.close();
     }
     
     /**
      * Creates a temporary override of a configuration setting.
      * @param name  setting name
      * @param value transient setting value
      */
     public void
     override(final String name, final Object value) {
         Filesystem.file(root, name);
         cacheKeys = cacheKeys.with(baseURI + name);
         cacheValues = cacheValues.with(value);
     }
 }
