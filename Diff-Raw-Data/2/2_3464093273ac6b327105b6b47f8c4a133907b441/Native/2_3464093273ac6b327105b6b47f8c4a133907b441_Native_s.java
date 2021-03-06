 /* Copyright (c) 2007, 2008, 2009 Timothy Wall, All Rights Reserved
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * <p/>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.  
  */
 package com.sun.jna;
 
 import java.awt.Component;
 import java.awt.GraphicsEnvironment;
 import java.awt.HeadlessException;
 import java.awt.Window;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import com.sun.jna.Callback.UncaughtExceptionHandler;
 import com.sun.jna.Structure.FFIType;
 
 /** Provides generation of invocation plumbing for a defined native
  * library interface.  Also provides various utilities for native operations.
  * <p>
  * {@link #getTypeMapper} and {@link #getStructureAlignment} are provided
  * to avoid having to explicitly pass these parameters to {@link Structure}s, 
  * which would require every {@link Structure} which requires custom mapping
  * or alignment to define a constructor and pass parameters to the superclass.
  * To avoid lots of boilerplate, the base {@link Structure} constructor
  * figures out these properties based on its enclosing interface.<p>
  * <a name=library_loading></a>
  * <h2>Library Loading</h2>
  * When JNA classes are loaded, the native shared library (jnidispatch) is
  * loaded as well.  An attempt is made to load it from the system library path
  * using {@link System#loadLibrary}.  If not found, the appropriate library
  * will be extracted from the class path into a temporary directory and
  * loaded from there.  If your system has additional security constraints
  * regarding execution or load of files (SELinux, for example), you should 
  * probably install the native library in an accessible location and configure 
  * your system accordingly, rather than relying on JNA to extract the library 
  * from its own jar file.
  * <p>
  * @see Library
  * @author Todd Fast, todd.fast@sun.com
  * @author twall@users.sf.net
  */
 public final class Native {
 
     private static String nativeLibraryPath;
     private static boolean unpacked;
     private static Map typeMappers = new WeakHashMap();
     private static Map alignments = new WeakHashMap();
     private static Map options = new WeakHashMap();
     private static Map libraries = new WeakHashMap();
     private static final UncaughtExceptionHandler DEFAULT_HANDLER = 
         new UncaughtExceptionHandler() {
             public void uncaughtException(Callback c, Throwable e) {
                 System.err.println("JNA: Callback " + c + " threw the following exception:");
                 e.printStackTrace();
             }
         };
     private static UncaughtExceptionHandler callbackExceptionHandler = DEFAULT_HANDLER;
     
     /** The size of a native pointer (<code>void*</code>) on the current 
      * platform, in bytes. 
      */
     public static final int POINTER_SIZE;
     /** Size of a native <code>long</code> type, in bytes. */
     public static final int LONG_SIZE;
     /** Size of a native <code>wchar_t</code> type, in bytes. */
     public static final int WCHAR_SIZE;
     /** Size of a native <code>size_t</code> type, in bytes. */
     public static final int SIZE_T_SIZE;
 
     private static final int TYPE_VOIDP = 0;
     private static final int TYPE_LONG = 1;
     private static final int TYPE_WCHAR_T = 2;
     private static final int TYPE_SIZE_T = 3;
 
     static {
         loadNativeLibrary();
         POINTER_SIZE = sizeof(TYPE_VOIDP);
         LONG_SIZE = sizeof(TYPE_LONG);
         WCHAR_SIZE = sizeof(TYPE_WCHAR_T);
         SIZE_T_SIZE = sizeof(TYPE_SIZE_T);
         // Perform initialization of other JNA classes until *after* 
         // initializing the above final fields
         initIDs();
         if (Boolean.getBoolean("jna.protected")) {
             setProtected(true);
         }
     }
     
     /** Ensure our unpacked native library gets cleaned up if this class gets
         garbage-collected.
     */
     private static final Object finalizer = new Object() {
         protected void finalize() {
             deleteNativeLibrary();
         }
     };
 
     /** Remove any unpacked native library.  Forcing the class loader to
         unload it first is required on Windows, since the temporary native
         library can't be deleted until the native library is unloaded.  Any
         deferred execution we might install at this point would prevent the
         Native class and its class loader from being GC'd, so we instead force
         the native library unload just a little bit prematurely.
      */
     private static boolean deleteNativeLibrary() {
         String path = nativeLibraryPath;
         if (path == null || !unpacked) return true;
         File flib = new File(path);
         if (flib.delete()) {
             nativeLibraryPath = null;
             unpacked = false;
             return true;
         }
         // Reach into the bowels of ClassLoader to force the native
         // library to unload
         try {
             ClassLoader cl = Native.class.getClassLoader();
             Field f = ClassLoader.class.getDeclaredField("nativeLibraries");
             f.setAccessible(true);
             List libs = (List)f.get(cl);
             for (Iterator i = libs.iterator();i.hasNext();) {
                 Object lib = i.next();
                 f = lib.getClass().getDeclaredField("name");
                 f.setAccessible(true);
                 String name = (String)f.get(lib);
                 if (name.equals(path) || name.indexOf(path) != -1) {
                     Method m = lib.getClass().getDeclaredMethod("finalize", new Class[0]);
                     m.setAccessible(true);
                     m.invoke(lib, new Object[0]);
                     nativeLibraryPath = null;
                     if (unpacked) {
                         if (flib.exists()) {
                             if (flib.delete()) {
                                 unpacked = false;
                                 return true;
                             }
                             return false;
                         }
                     }
                     return true;
                 }
             }
         }
         catch(Exception e) {
         }
         return false;
     }
 
     private Native() { }
     
     private static native void initIDs();
 
     /** Set whether native memory accesses are protected from invalid
      * accesses.  This should only be set true when testing or debugging,
      * and should not be considered reliable or robust for applications
      * where JNA native calls are occurring on multiple threads.
      * Protected mode will be automatically set if the
      * system property <code>jna.protected</code> has a value of "true"
      * when the JNA library is first loaded.<p>
      * If not supported by the underlying platform, this setting will
      * have no effect.<p>
      * NOTE: On platforms which support signals (non-Windows), JNA uses
      * signals to trap errors.  This may interfere with the JVM's own use of
      * signals.  When protected mode is enabled, you should make use of the
      * jsig library, if available (see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/vm/signal-chaining.html">Signal Chaining</a>).
      * In short, set the environment variable <code>LD_PRELOAD</code> to the
      * path to <code>libjsig.so</code> in your JRE lib directory
      * (usually ${java.home}/lib/${os.arch}/libjsig.so) before launching your
      * Java application.
      */
     public static synchronized native void setProtected(boolean enable);
     
     /** Returns whether protection is enabled.  Check the result of this method
      * after calling {@link #setProtected setProtected(true)} to determine
      * if this platform supports protecting memory accesses.
      */
     public static synchronized native boolean isProtected();
 
     /** Set whether the system last error result is captured after every
      * native invocation.  Defaults to <code>true</code> (<code>false</code>
      * for direct-mapped calls).<p>
      * @deprecated The preferred method of obtaining the last error result is
      * to declare your mapped method to throw {@link LastErrorException}
      * instead. 
      */
     public static synchronized native void setPreserveLastError(boolean enable);
     
     /** Indicates whether the system last error result is preserved
      * after every invocation.<p>
      * @deprecated The preferred method of obtaining the last error result is
      * to declare your mapped method to throw {@link LastErrorException}
      * instead. 
      */
     public static synchronized native boolean getPreserveLastError();
     
     /** Utility method to get the native window ID for a Java {@link Window}
      * as a <code>long</code> value.
      * This method is primarily for X11-based systems, which use an opaque
      * <code>XID</code> (usually <code>long int</code>) to identify windows.
      * @throws HeadlessException if the current VM is running headless 
      */
     public static long getWindowID(Window w) throws HeadlessException {
         return getComponentID(w);
     }
 
     /** Utility method to get the native window ID for a heavyweight Java 
      * {@link Component} as a <code>long</code> value.
      * This method is primarily for X11-based systems, which use an opaque
      * <code>XID</code> (usually <code>long int</code>) to identify windows. 
      * @throws HeadlessException if the current VM is running headless 
      */
     public static long getComponentID(Component c) throws HeadlessException {
         if (GraphicsEnvironment.isHeadless()) {
             throw new HeadlessException("No native windows when headless");
         }
         if (c.isLightweight()) {
             throw new IllegalArgumentException("Component must be heavyweight");
         }
         if (!c.isDisplayable()) 
             throw new IllegalStateException("Component must be displayable");
         // On X11 VMs prior to 1.5, the window must be visible
         if (Platform.isX11()
             && System.getProperty("java.version").startsWith("1.4")) {
             if (!c.isVisible()) {
                 throw new IllegalStateException("Component must be visible");
             }
         }
         // By this point, we're certain that Toolkit.loadLibraries() has
         // been called, thus avoiding AWT/JAWT link errors
         // (see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6539705).
         return getWindowHandle0(c);
     }
     
     /** Utility method to get the native window pointer for a Java 
      * {@link Window} as a {@link Pointer} value.  This method is primarily for 
      * w32, which uses the <code>HANDLE</code> type (actually 
      * <code>void *</code>) to identify windows. 
      * @throws HeadlessException if the current VM is running headless 
      */
     public static Pointer getWindowPointer(Window w) throws HeadlessException {
         return getComponentPointer(w);
     }
     
     /** Utility method to get the native window pointer for a heavyweight Java 
      * {@link Component} as a {@link Pointer} value.  This method is primarily 
      * for w32, which uses the <code>HANDLE</code> type (actually 
      * <code>void *</code>) to identify windows. 
      * @throws HeadlessException if the current VM is running headless 
      */
     public static Pointer getComponentPointer(Component c) throws HeadlessException {
         return new Pointer(getComponentID(c));
     }
     
     private static native long getWindowHandle0(Component c);
 
     /** Convert a direct {@link Buffer} into a {@link Pointer}. 
      * @throws IllegalArgumentException if the buffer is not direct.
      */
     public static native Pointer getDirectBufferPointer(Buffer b);
     
     /** Obtain a Java String from the given native byte array.  If there is
      * no NUL terminator, the String will comprise the entire array.  If the
      * system property <code>jna.encoding</code> is set, its value will 
      * override the platform default encoding (if supported).
      */
     public static String toString(byte[] buf) {
         String encoding = System.getProperty("jna.encoding");
         String s = null;
         if (encoding != null) {
             try {
                 s = new String(buf, encoding);
             }
             catch(UnsupportedEncodingException e) { }
         }
         if (s == null) {
             s = new String(buf);
         }
         int term = s.indexOf(0);
         if (term != -1)
             s = s.substring(0, term);
         return s;
     }
     
     /** Obtain a Java String from the given native wchar_t array.  If there is
      * no NUL terminator, the String will comprise the entire array.
      */
     public static String toString(char[] buf) {
         String s = new String(buf); 
         int term = s.indexOf(0);
         if (term != -1)
             s = s.substring(0, term);
         return s;
     }
     
     /** Load a library interface from the given shared library, providing
      * the explicit interface class.
      */
     public static Object loadLibrary(String name, Class interfaceClass) {
         return loadLibrary(name, interfaceClass, Collections.EMPTY_MAP);
     }
 
     /** Load a library interface from the given shared library, providing
      * the explicit interface class and a map of options for the library.
      * If no library options are detected the map is interpreted as a map
      * of Java method names to native function names.
      * @param name
      * @param interfaceClass
      * @param libOptions Map of library options
      */
     public static Object loadLibrary(String name, 
                                      Class interfaceClass,
                                      Map libOptions) {
         Library.Handler handler = 
             new Library.Handler(name, interfaceClass, libOptions);
         ClassLoader loader = interfaceClass.getClassLoader();
         Library proxy = (Library)
             Proxy.newProxyInstance(loader, new Class[] {interfaceClass},
                                    handler);
         synchronized(libraries) {
             if (!libOptions.isEmpty()) 
                 options.put(interfaceClass, libOptions);
             if (libOptions.containsKey(Library.OPTION_TYPE_MAPPER))
                 typeMappers.put(interfaceClass, libOptions.get(Library.OPTION_TYPE_MAPPER));
             if (libOptions.containsKey(Library.OPTION_STRUCTURE_ALIGNMENT))
                 alignments.put(interfaceClass, libOptions.get(Library.OPTION_STRUCTURE_ALIGNMENT));
             libraries.put(interfaceClass, new WeakReference(proxy));
         }
         return proxy;
     }
 
     /** Attempts to force initialization of an instance of the library interface
      * by loading a public static field of the requisite type.
      * Returns whether an instance variable was instantiated. 
      * Expects that lock on libraries is already held
      */
     private static void loadLibraryInstance(Class cls) {
         if (cls != null && !libraries.containsKey(cls)) {
             try {
                 Field[] fields = cls.getFields();
                 for (int i=0;i < fields.length;i++) {
                     Field field = fields[i];
                     if (field.getType() == cls 
                         && Modifier.isStatic(field.getModifiers())) {
                         // Ensure the field gets initialized by reading it
                         libraries.put(cls, new WeakReference(field.get(null)));
                         break;
                     }
                 }
             }
             catch (Exception e) {
                 throw new IllegalArgumentException("Could not access instance of " 
                                                    + cls + " (" + e + ")");
             }
         }
     }
     
     /** Find the library interface corresponding to the given class.  Checks
      * all ancestor classes and interfaces for a declaring class which
      * implements {@link Library}.
      */
     static Class findEnclosingLibraryClass(Class cls) {
         if (cls == null) { 
             return null;
         }
         if (Library.class.isAssignableFrom(cls)) {
             return cls;
         }
         if (Callback.class.isAssignableFrom(cls)) {
             cls = CallbackReference.findCallbackClass(cls);
         }
         Class fromDeclaring = findEnclosingLibraryClass(cls.getDeclaringClass());
         if (fromDeclaring != null) {
             return fromDeclaring;
         }
         return findEnclosingLibraryClass(cls.getSuperclass());
     }
     
 
     /** Return the preferred native library configuration options for the given 
      * class.
      * @see Library 
      */
     public static Map getLibraryOptions(Class type) {
         synchronized(libraries) {
             Class interfaceClass = findEnclosingLibraryClass(type);
             if (interfaceClass != null) 
                 loadLibraryInstance(interfaceClass);
             else
                 interfaceClass = type;
             if (!options.containsKey(interfaceClass)) {
                 try {
                     Field field = interfaceClass.getField("OPTIONS");
                     field.setAccessible(true);
                     options.put(interfaceClass, field.get(null));
                 }
                 catch (NoSuchFieldException e) {
                 }
                 catch (Exception e) {
                     throw new IllegalArgumentException("OPTIONS must be a public field of type java.util.Map (" 
                                                        + e + "): " + interfaceClass);
                 }
             }
             return (Map)options.get(interfaceClass);
         }
     }
 
     /** Return the preferred {@link TypeMapper} for the given native interface.
      * See {@link com.sun.jna.Library#OPTION_TYPE_MAPPER}. 
      */
     public static TypeMapper getTypeMapper(Class cls) {
         synchronized(libraries) {
             Class interfaceClass = findEnclosingLibraryClass(cls);
             if (interfaceClass != null)
                 loadLibraryInstance(interfaceClass);
             else
                 interfaceClass = cls;
 
             if (!typeMappers.containsKey(interfaceClass)) {
                 try {
                     Field field = interfaceClass.getField("TYPE_MAPPER");
                     field.setAccessible(true);
                     typeMappers.put(interfaceClass, field.get(null));
                 }
                 catch (NoSuchFieldException e) {
                     Map options = getLibraryOptions(cls);
                     if (options != null
                         && options.containsKey(Library.OPTION_TYPE_MAPPER)) {
                         typeMappers.put(interfaceClass, options.get(Library.OPTION_TYPE_MAPPER));
                     }
                 }
                 catch (Exception e) {
                     throw new IllegalArgumentException("TYPE_MAPPER must be a public field of type "
                                                        + TypeMapper.class.getName() + " (" 
                                                        + e + "): " + interfaceClass);
                 }
             }
             return (TypeMapper)typeMappers.get(interfaceClass);
         }
     }
 
     /** Return the preferred structure alignment for the given native interface. 
      * See {@link com.sun.jna.Library#OPTION_STRUCTURE_ALIGNMENT}.
      */
     public static int getStructureAlignment(Class cls) {
         synchronized(libraries) {
             Class interfaceClass = findEnclosingLibraryClass(cls);
             if (interfaceClass != null) 
                 loadLibraryInstance(interfaceClass);
             else
                 interfaceClass = cls;
             if (!alignments.containsKey(interfaceClass)) {
                 try {
                     Field field = interfaceClass.getField("STRUCTURE_ALIGNMENT");
                     field.setAccessible(true);
                     alignments.put(interfaceClass, field.get(null));
                 }
                 catch(NoSuchFieldException e) {
                     Map options = getLibraryOptions(interfaceClass);
                     if (options != null
                         && options.containsKey(Library.OPTION_STRUCTURE_ALIGNMENT)) {
                         alignments.put(interfaceClass, options.get(Library.OPTION_STRUCTURE_ALIGNMENT));
                     }
                 }
                 catch(Exception e) {
                     throw new IllegalArgumentException("STRUCTURE_ALIGNMENT must be a public field of type int ("
                                                        + e + "): " + interfaceClass);
                 }
             }
             Integer value = (Integer)alignments.get(interfaceClass);
             return value != null ? value.intValue() : Structure.ALIGN_DEFAULT;
         }
     }
     
     /** Return a byte array corresponding to the given String.  If the
      * system property <code>jna.encoding</code> is set, its value will override
      * the default platform encoding (if supported).
      */
     static byte[] getBytes(String s) {
         String encoding = System.getProperty("jna.encoding");
         if (encoding != null) {
             try {
                 return s.getBytes(encoding);
             }
             catch (UnsupportedEncodingException e) {
             }
         }
         return s.getBytes();
     }
 
     /** Obtain a NUL-terminated byte buffer equivalent to the given String. */
     public static byte[] toByteArray(String s) {
         byte[] bytes = getBytes(s);
         byte[] buf = new byte[bytes.length+1];
         System.arraycopy(bytes, 0, buf, 0, bytes.length);
         return buf;
     }
 
     /** Obtain a NUL-terminated wide character buffer equivalent to the given
         String.
     */ 
     public static char[] toCharArray(String s) {
         char[] chars = s.toCharArray();
         char[] buf = new char[chars.length+1];
         System.arraycopy(chars, 0, buf, 0, chars.length);
         return buf;
     }
 
     private static String getNativeLibraryResourcePath() {
         String arch = System.getProperty("os.arch").toLowerCase();
         String osPrefix;
         if (Platform.isWindows()) {
             osPrefix = "win32-" + arch;
         }
         else if (Platform.isMac()) {
             osPrefix = "darwin";
         }
         else if (Platform.isLinux()) {
             if ("x86".equals(arch)) {
                 arch = "i386";
             }
             else if ("x86_64".equals(arch)) {
                 arch = "amd64";
             }
             osPrefix = "linux-" + arch;
         }
         else if (Platform.isSolaris()) {
             osPrefix = "sunos-" + arch;
         }
         else {
             osPrefix = System.getProperty("os.name").toLowerCase();
             int space = osPrefix.indexOf(" ");
             if (space != -1) {
                 osPrefix = osPrefix.substring(0, space);
             }
             osPrefix += "-" + arch;
         }
         return "/com/sun/jna/" + osPrefix;
     }
 
     /**
      * Loads the JNA stub library.  It will first attempt to load this library
      * from the directories specified in jna.boot.library.path.  If that fails,
      * it will fallback to loading from the system library paths. Finally it will
      * attempt to extract the stub library from from the JNA jar file, and load it.
      * <p>
      * The jna.boot.library.path property is mainly to support jna.jar being
      * included in -Xbootclasspath, where java.library.path and LD_LIBRARY_PATH
      * are ignored.  It might also be useful in other situations.
      * </p>
      */
     private static void loadNativeLibrary() {
         String libName = "jnidispatch";
         String bootPath = System.getProperty("jna.boot.library.path");
         if (bootPath != null) {
             String[] dirs = bootPath.split(File.pathSeparator);
             for (int i = 0; i < dirs.length; ++i) {
                 String path = new File(new File(dirs[i]), System.mapLibraryName(libName)).getAbsolutePath();
                 try {
                     System.load(path);
                     nativeLibraryPath = path;
                     return;
                 } catch (UnsatisfiedLinkError ex) {
                 }
                 if (Platform.isMac()) {
                     String orig, ext;
                     if (path.endsWith("dylib")) {
                         orig = "dylib";
                         ext = "jnilib";
                     } else {
                         orig = "jnilib";
                         ext = "dylib";
                     }
                     try {
                         path = path.substring(0, path.lastIndexOf(orig)) + ext;
                         System.load(path);
                         nativeLibraryPath = path;
                         return;
                     } catch (UnsatisfiedLinkError ex) {
                     }
                 }
             }
         }
         try {
             System.loadLibrary(libName);
             nativeLibraryPath = libName;
         }
         catch(UnsatisfiedLinkError e) {
             loadNativeLibraryFromJar();
         }
     }
 
     /**
      * Extracts and loads the JNA stub library from jna.jar
      */
     private static void loadNativeLibraryFromJar() {
         String libname = System.mapLibraryName("jnidispatch");
         String resourceName = getNativeLibraryResourcePath() + "/" + libname;
         URL url = Native.class.getResource(resourceName);
                 
         // Add an ugly hack for OpenJDK (soylatte) - JNI libs use the usual .dylib extension
         if (url == null && Platform.isMac() && resourceName.endsWith(".dylib")) {
             resourceName = resourceName.substring(0, resourceName.lastIndexOf(".dylib")) + ".jnilib";
             url = Native.class.getResource(resourceName);
         }
         if (url == null) {
             throw new UnsatisfiedLinkError("jnidispatch (" + resourceName 
                                            + ") not found in resource path");
         }
     
         File lib = null;
         if (url.getProtocol().toLowerCase().equals("file")) {
             // NOTE: use older API for 1.3 compatibility
             lib = new File(URLDecoder.decode(url.getPath()));
         }
         else {
             InputStream is = Native.class.getResourceAsStream(resourceName);
             if (is == null) {
                 throw new Error("Can't obtain jnidispatch InputStream");
             }
             
             FileOutputStream fos = null;
             try {
                 // Suffix is required on windows, or library fails to load
                 // Let Java pick the suffix, except on windows, to avoid
                 // problems with Web Start.
                 lib = File.createTempFile("jna", Platform.isWindows()?".dll":null);
                 lib.deleteOnExit();
                 ClassLoader cl = Native.class.getClassLoader();
                 if (Platform.deleteNativeLibraryAfterVMExit()
                     && (cl == null
                         || cl.equals(ClassLoader.getSystemClassLoader()))) {
                     Runtime.getRuntime().addShutdownHook(new DeleteNativeLibrary(lib));
                 }
                 fos = new FileOutputStream(lib);
                 int count;
                 byte[] buf = new byte[1024];
                 while ((count = is.read(buf, 0, buf.length)) > 0) {
                     fos.write(buf, 0, count);
                 }
             }
             catch(IOException e) {
                 throw new Error("Failed to create temporary file for jnidispatch library: " + e);
             }
             finally {
                 try { is.close(); } catch(IOException e) { }
                 if (fos != null) {
                     try { fos.close(); } catch(IOException e) { }
                 }
             }
             unpacked = true;
         }
         System.load(lib.getAbsolutePath());
         nativeLibraryPath = lib.getAbsolutePath();
     }
 
     /**
      * Initialize field and method IDs for native methods of this class. 
      * Returns the size of a native pointer.
      **/
     private static native int sizeof(int type);
 
     private static native String getNativeVersion();
     private static native String getAPIChecksum();
 
     private static final ThreadLocal lastError = new ThreadLocal() {
         protected synchronized Object initialValue() {
             return new Integer(0);
         }
     };
     
     /** Retrieve the last error set by the OS.  This corresponds to
      * <code>GetLastError()</code> on Windows, and <code>errno</code> on
      * most other platforms.  The value is preserved per-thread, but whether 
      * the original value is per-thread depends on the underlying OS.  The 
      * result is undefined if {@link #getPreserveLastError} is 
      * <code>false</code>.<p>
      * The preferred method of obtaining the last error result is
      * to declare your mapped method to throw {@link LastErrorException}
      * instead. 
      */
     public static int getLastError() {
         return ((Integer)lastError.get()).intValue();
     }
     
     /** Set the OS last error code.  Whether the setting is per-thread
      * or global depends on the underlying OS.
      */
     public static native void setLastError(int code);
 
     /** Update the last error value (called from native code). */
     static void updateLastError(int e) {
         lastError.set(new Integer(e));
     }
 
     /**
      * Returns a synchronized (thread-safe) library backed by the specified
      * library.  This wrapping will prevent simultaneous invocations of any
      * functions mapped to a given {@link NativeLibrary}.  Note that the 
      * native library may still be sensitive to being called from different
      * threads.
      * <p>
      * @param  library the library to be "wrapped" in a synchronized library.
      * @return a synchronized view of the specified library.
      */
     public static Library synchronizedLibrary(final Library library) {
         Class cls = library.getClass();
         if (!Proxy.isProxyClass(cls)) {
             throw new IllegalArgumentException("Library must be a proxy class");
         }
         InvocationHandler ih = Proxy.getInvocationHandler(library);
         if (!(ih instanceof Library.Handler)) {
             throw new IllegalArgumentException("Unrecognized proxy handler: " + ih);
         }
         final Library.Handler handler = (Library.Handler)ih; 
         InvocationHandler newHandler = new InvocationHandler() {
             public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                 synchronized(handler.getNativeLibrary()) {
                     return handler.invoke(library, method, args);
                 }
             }
         };
         return (Library)Proxy.newProxyInstance(cls.getClassLoader(),
                                                cls.getInterfaces(),
                                                newHandler);
     }
     
     /** If running web start, determine the location of a given native 
      * library.  This value may be used to properly set 
      * <code>jna.library.path</code> so that JNA can load libraries identified
      * by the &lt;nativelib&gt; tag in the JNLP configuration file.  Returns 
      * <code>null</code> if the Web Start native library cache location can not 
      * be determined.  Note that the path returned may be different for any
      * given library name.
      * <p>
      * Use <code>System.getProperty("javawebstart.version")</code> to detect
      * whether your code is running under Web Start.
      * @throws IllegalArgumentException if the library can't be found by the
      * Web Start class loader, which usually means it wasn't included as 
      * a <code>&lt;nativelib&gt;</code> resource in the JNLP file.
      */
     public static String getWebStartLibraryPath(final String libName) {
         if (System.getProperty("javawebstart.version") == null)
             return null;
         try {
 
             final ClassLoader cl = Native.class.getClassLoader();
             Method m = (Method)AccessController.doPrivileged(new PrivilegedAction() {
                 public Object run() {
                     try {
                         Method m = ClassLoader.class.getDeclaredMethod("findLibrary", new Class[] { String.class });
                         m.setAccessible(true);
                         return m;
                     }
                     catch(Exception e) {
                         return null;
                     }
                 }
             });
             String libpath = (String)m.invoke(cl, new Object[] { libName });
             if (libpath != null) {
                 return new File(libpath).getParent();
             }
             String msg = 
                 "Library '" + libName + "' was not found by class loader " + cl;
            throw new IllegalArgumentException(msg);
         }
         catch (Exception e) {
             return null;
         }
     }
 
     /** For internal use only. */
     public static class DeleteNativeLibrary extends Thread {
         private final File file;
         public DeleteNativeLibrary(File file) {
             this.file = file;
         }
         public void run() {
             // If we can't force an unload/delete, spawn a new process
             // to do so
             if (!deleteNativeLibrary()) {
                 try {
                     Runtime.getRuntime().exec(new String[] {
                         System.getProperty("java.home") + "/bin/java",
                         "-cp", System.getProperty("java.class.path"),
                         getClass().getName(),
                         file.getAbsolutePath(),
                     });
                 }
                 catch(IOException e) { e.printStackTrace(); }
             }
         }
         public static void main(String[] args) {
             if (args.length == 1) {
                 File file = new File(args[0]);
                 if (file.exists()) { 
                     long start = System.currentTimeMillis();
                     while (!file.delete() && file.exists()) {
                         try { Thread.sleep(10); }
                         catch(InterruptedException e) { }
                         if (System.currentTimeMillis() - start > 5000) {
                             System.err.println("Could not remove temp file: "
                                                + file.getAbsolutePath());
                             break;
                         }
                     }
                 }
             }
             System.exit(0);
         }
     }
 
     /** Returns the native size of the given class, in bytes. 
      * For use with arrays.
      */
     public static int getNativeSize(Class type, Object value) {
         if (type.isArray()) {
             int len = Array.getLength(value);
             if (len > 0) {
                 Object o = Array.get(value, 0);
                 return len * getNativeSize(type.getComponentType(), o);
             }
             // Don't process zero-length arrays
             throw new IllegalArgumentException("Arrays of length zero not allowed in structure: " + type);
         }
         if (Structure.class.isAssignableFrom(type)
             && !Structure.ByReference.class.isAssignableFrom(type)) {
             if (value == null)
                 value = Structure.newInstance(type);
             return ((Structure)value).size();
         }
         try {
             return Native.getNativeSize(type);
         }
         catch(IllegalArgumentException e) {
             throw new IllegalArgumentException("The type \"" + type.getName()
                                                + "\" is not supported as a structure field: "
                                                + e.getMessage());
         }
     }
 
     /** Returns the native size for a given Java class.  Structures are
      * assumed to be <code>struct</code> pointers unless they implement
      * {@link Structure.ByValue}.
      */
     public static int getNativeSize(Class cls) {
         if (NativeMapped.class.isAssignableFrom(cls)) {
             cls = NativeMappedConverter.getInstance(cls).nativeType();
         }
         // boolean defaults to 32 bit integer if not otherwise mapped
         if (cls == boolean.class || cls == Boolean.class) return 4;
         if (cls == byte.class || cls == Byte.class) return 1;
         if (cls == short.class || cls == Short.class) return 2; 
         if (cls == char.class || cls == Character.class) return WCHAR_SIZE;
         if (cls == int.class || cls == Integer.class) return 4;
         if (cls == long.class || cls == Long.class) return 8;
         if (cls == float.class || cls == Float.class) return 4;
         if (cls == double.class || cls == Double.class) return 8;
         if (Structure.class.isAssignableFrom(cls)) {
             if (Structure.ByValue.class.isAssignableFrom(cls)) {
                 return Structure.newInstance(cls).size();
             }
             return POINTER_SIZE;
         }
         if (Pointer.class.isAssignableFrom(cls)
             || Buffer.class.isAssignableFrom(cls)
             || Callback.class.isAssignableFrom(cls)
             || String.class == cls
             || WString.class == cls) {
             return POINTER_SIZE;
         }
         throw new IllegalArgumentException("Native size for type \"" + cls.getName() 
         								   + "\" is unknown");
     }
 
     /** Indicate whether the given class is supported as a native argument
      * type.
      */  
     public static boolean isSupportedNativeType(Class cls) {
         if (Structure.class.isAssignableFrom(cls)) {
             return true;
         }
         try {
             return getNativeSize(cls) != 0;
         }
         catch(IllegalArgumentException e) {
             return false;
         }
     }
 
     /** Set the default handler invoked when a callback throws an uncaught
      * exception.  If the given handler is <code>null</code>, the default
      * handler will be reinstated.
      */
     public static void setCallbackExceptionHandler(UncaughtExceptionHandler eh) {
         callbackExceptionHandler = eh == null ? DEFAULT_HANDLER : eh;
     }
 
     /** Returns the current handler for callback uncaught exceptions. */
     public static UncaughtExceptionHandler getCallbackExceptionHandler() {
         return callbackExceptionHandler;
     }
     
     /** When called from a class static initializer, maps all native methods
      * found within that class to native libraries via the JNA raw calling
      * interface.
      * @param libName library name to which functions should be bound
      */
     public static void register(String libName) {
         register(getNativeClass(getCallingClass()),
                  NativeLibrary.getInstance(libName));
     }
 
     /** When called from a class static initializer, maps all native methods
      * found within that class to native libraries via the JNA raw calling
      * interface.
      * @param lib native library to which functions should be bound
      */
     public static void register(NativeLibrary lib) {
         register(getNativeClass(getCallingClass()), lib);
     }
 
     static Class getNativeClass(Class cls) {
         Method[] methods = cls.getDeclaredMethods();
         for (int i=0;i < methods.length;i++) {
             if ((methods[i].getModifiers() & Modifier.NATIVE) != 0) {
                 return cls;
             }
         }
         int idx = cls.getName().lastIndexOf("$");
         if (idx != -1) {
             String name = cls.getName().substring(0, idx);
             try {
                 return getNativeClass(Class.forName(name, true, cls.getClassLoader()));
             }
             catch(ClassNotFoundException e) {
             }
         }
         throw new IllegalArgumentException("Can't determine class with native methods from the current context (" + cls + ")");
     }
 
     static Class getCallingClass() {
         Class[] context = new SecurityManager() {
             public Class[] getClassContext() {
                 return super.getClassContext();
             }
         }.getClassContext();
         if (context.length < 4) {
             throw new IllegalStateException("This method must be called from the static initializer of a class");
         }
         return context[3];
     }
 
     private static Map registeredClasses = new HashMap();
     private static Map registeredLibraries = new HashMap();
     private static Object unloader = new Object() {
         protected void finalize() {
             synchronized(registeredClasses) {
                 for (Iterator i=registeredClasses.entrySet().iterator();i.hasNext();) {
                     Map.Entry e = (Map.Entry)i.next();
                     unregister((Class)e.getKey(), (long[])e.getValue());
                     i.remove();
                 }
             }
         }
     };
 
     /** Remove all native mappings for the calling class.
         Should only be called if the class is no longer referenced and about
         to be garbage collected.
      */
     public static void unregister() {
         unregister(getNativeClass(getCallingClass()));
     }
 
     /** Remove all native mappings for the given class.
         Should only be called if the class is no longer referenced and about
         to be garbage collected.
      */
     public static void unregister(Class cls) {
         synchronized(registeredClasses) {
             if (registeredClasses.containsKey(cls)) {
                 unregister(cls, (long[])registeredClasses.get(cls));
                 registeredClasses.remove(cls);
                 registeredLibraries.remove(cls);
             }
         }
     }
 
     /** Unregister the native methods for the given class. */
     private static native void unregister(Class cls, long[] handles);
 
     private static String getSignature(Class cls) {
         if (cls.isArray()) {
             return "[" + getSignature(cls.getComponentType());
         }
         if (cls.isPrimitive()) {
             if (cls == void.class) return "V";
             if (cls == boolean.class) return "Z";
             if (cls == byte.class) return "B";
             if (cls == short.class) return "S";
             if (cls == char.class) return "C";
             if (cls == int.class) return "I";
             if (cls == long.class) return "J";
             if (cls == float.class) return "F";
             if (cls == double.class) return "D";
         }
         return "L" + cls.getName().replace(".", "/") + ";";
     }
 
     private static final int CVT_UNSUPPORTED = -1;
     private static final int CVT_DEFAULT = 0;
     private static final int CVT_POINTER = 1;
     private static final int CVT_STRING = 2;
     private static final int CVT_STRUCTURE = 3;
     private static final int CVT_STRUCTURE_BYVAL = 4;
     private static final int CVT_BUFFER = 5;
     private static final int CVT_ARRAY_BYTE = 6;
     private static final int CVT_ARRAY_SHORT = 7;
     private static final int CVT_ARRAY_CHAR = 8;
     private static final int CVT_ARRAY_INT = 9;
     private static final int CVT_ARRAY_LONG = 10;
     private static final int CVT_ARRAY_FLOAT = 11;
     private static final int CVT_ARRAY_DOUBLE = 12;
     private static final int CVT_ARRAY_BOOLEAN = 13;
     private static final int CVT_BOOLEAN = 14;
     private static final int CVT_CALLBACK = 15;
 
     private static int getConversion(Class type) {
         if (Pointer.class.isAssignableFrom(type)) {
             return CVT_POINTER;
         }
         if (String.class == type) {
             return CVT_STRING;
         }
         if (Buffer.class.isAssignableFrom(type)) {
             return CVT_BUFFER;
         }
         if (Structure.class.isAssignableFrom(type)) {
             if (Structure.ByValue.class.isAssignableFrom(type)) {
                 return CVT_STRUCTURE_BYVAL;
             }
             return CVT_STRUCTURE;
         }
         if (type.isArray()) {
             switch(type.getName().charAt(1)) {
             case 'Z': return CVT_ARRAY_BOOLEAN;
             case 'B': return CVT_ARRAY_BYTE;
             case 'S': return CVT_ARRAY_SHORT;
             case 'C': return CVT_ARRAY_CHAR;
             case 'I': return CVT_ARRAY_INT;
             case 'J': return CVT_ARRAY_LONG;
             case 'F': return CVT_ARRAY_FLOAT;
             case 'D': return CVT_ARRAY_DOUBLE;
             default: break;
             }
         }
         if (type.isPrimitive()) {
             return type == boolean.class ? CVT_BOOLEAN : CVT_DEFAULT;
         }
         if (Callback.class.isAssignableFrom(type)) {
             return CVT_CALLBACK;
         }
         return -1;
     }
 
     /** When called from a class static initializer, maps all native methods
      * found within that class to native libraries via the JNA raw calling
      * interface.
      * @param lib library to which functions should be bound
      */
     // TODO: Pointer, NativeLong                                            
     // TODO: structure by value                                             
     // TODO: byref, primitive arrays                                        
     // TODO: stdcall name mapping                                           
     // TODO: derive library, etc. from annotations (per-class or per-method)
     // TODO: get function name from annotation
     // TODO: read parameter type mapping from annotations (long/native long)
     public static void register(Class cls, NativeLibrary lib) {
         Method[] methods = cls.getDeclaredMethods();
         List mlist = new ArrayList();
         for (int i=0;i < methods.length;i++) {
             if ((methods[i].getModifiers() & Modifier.NATIVE) != 0) {
                 mlist.add(methods[i]);
             }
         }
         long[] handles = new long[mlist.size()];
         for (int i=0;i < handles.length;i++) {
             Method method = (Method)mlist.get(i);
             String sig = "(";
             Class rtype = method.getReturnType();
             Class[] ptypes = method.getParameterTypes();
             long[] atypes = new long[ptypes.length];
             int[] cvt = new int[ptypes.length];
             int rcvt = getConversion(rtype);
             if (rcvt == CVT_UNSUPPORTED) {
                 throw new IllegalArgumentException(rtype + " is not a supported return type (in method " + method.getName() + " in " + cls + ")");
             }
             for (int t=0;t < ptypes.length;t++) {
                 Class type = ptypes[t];
                 sig += getSignature(type);
                 cvt[t] = getConversion(type);
                 if (cvt[t] == CVT_UNSUPPORTED) {
                     throw new IllegalArgumentException(type + " is not a supported argument type (in method " + method.getName() + " in " + cls + ")");
                 }
                 // All conversions other than struct by value and primitives
                 // result in a pointer passed to the native function
                 if (cvt[t] == CVT_STRUCTURE_BYVAL
                     || cvt[t] == CVT_DEFAULT) {
                     atypes[t] = FFIType.get(type).peer;
                 }
                 else {
                     atypes[t] = FFIType.get(Pointer.class).peer;
                 }
             }
             sig += ")";
             sig += getSignature(rtype);
             
             String name = method.getName();
             FunctionMapper mapper = (FunctionMapper)lib.getOptions().get(Library.OPTION_FUNCTION_MAPPER);
             if (mapper != null) {
                 name = mapper.getFunctionName(lib, method);
             }
             Function f = lib.getFunction(name, method);
             try {
                 handles[i] = registerMethod(cls, method.getName(),
                                             sig, cvt, atypes, rcvt,
                                             FFIType.get(rtype).peer, rtype, 
                                             f.peer, f.callFlags);
             }
             catch(NoSuchMethodError e) {
                 throw new UnsatisfiedLinkError("No method " + method.getName() + " with signature " + sig + " in " + cls);
             }
         }
         synchronized(registeredClasses) {
             registeredClasses.put(cls, handles);
             registeredLibraries.put(cls, lib);
         }
     }
 
     private static native long registerMethod(Class cls,
                                               String name,
                                               String signature,
                                               int[] conversions,
                                               long[] arg_types,
                                               int rconversion,
                                               long rtype,
                                               Class rclass,
                                               long fptr,
                                               int callFlags);
     
     static native long ffi_prep_cif(int abi, int nargs, long ffi_return_type, long ffi_types);
     static native void ffi_call(long cif, long fptr, long resp, long args);
     static native long ffi_prep_closure();
 
     /** Prints JNA library details to the console. */
     public static void main(String[] args) {
         final String DEFAULT_TITLE = "Java Native Access (JNA)";
         final String UNKNOWN_VERSION = "unknown - package information missing";
         Package pkg = Native.class.getPackage();
         String title = pkg.getSpecificationTitle();
         if (title == null) title = DEFAULT_TITLE;
         String version = pkg.getSpecificationVersion();
         if (version == null) version = UNKNOWN_VERSION;
         title += " API Version " + version;
         System.out.println(title);
         version = pkg.getImplementationVersion();
         if (version == null) version = UNKNOWN_VERSION;
         System.out.println("Version: " + version);
         System.out.println(" Native: " + getNativeVersion() + " ("
                            + getAPIChecksum() + ")");
         System.exit(0);
     }
 }
