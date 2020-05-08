 package com.psddev.dari.util;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.lang.instrument.ClassDefinition;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.Instrumentation;
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.JarURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLConnection;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.tools.DiagnosticCollector;
 import javax.tools.FileObject;
 import javax.tools.ForwardingJavaFileManager;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaFileManager;
 import javax.tools.JavaFileObject;
 import javax.tools.SimpleJavaFileObject;
 import javax.tools.StandardLocation;
 import javax.tools.ToolProvider;
 
 import org.objectweb.asm.ClassAdapter;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CodeUtils {
 
     public static final String JAVA_SOURCE_DIRECTORY_PROPERTY = "javaSourceDirectory";
     public static final String RESOURCE_DIRECTORY_PROPERTY = "resourceDirectory";
 
     private static final String BUILD_PROPERTIES_PATH = "build.properties";
     private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
     private static final Logger LOGGER = LoggerFactory.getLogger(CodeUtils.class);
 
     private static final URI DEFAULT_URI; static {
         try {
             DEFAULT_URI = new URI("mem:/");
         } catch (URISyntaxException ex) {
             throw new IllegalStateException(ex);
         }
     }
 
     private static final String ATTRIBUTE_PREFIX = CodeUtils.class.getName() + ".";
     private static final String WEBAPP_SOURCE_DIRECTORIES_ATTRIBUTE = ATTRIBUTE_PREFIX + "webappSourceDirectories";
 
     private static final Set<File> SOURCE_DIRECTORIES;
     private static final Set<File> RESOURCE_DIRECTORIES;
     private static final Map<File, Long> JAR_LAST_MODIFIED_MAP;
 
     // Scan all build property files to find source directories.
     static {
         Set<File> sources = new HashSet<File>();
         Set<File> resources = new HashSet<File>();
         Map<File, Long> jars = new HashMap<File, Long>();
 
         try {
             for (Enumeration<URL> i = ObjectUtils.getCurrentClassLoader().getResources(BUILD_PROPERTIES_PATH); i.hasMoreElements(); ) {
                 try {
                     URLConnection buildConnection = i.nextElement().openConnection();
                     InputStream buildInput = buildConnection.getInputStream();
                     try {
                         Properties build = new Properties();
                         build.load(buildInput);
 
                         Set<File> directories = new HashSet<File>();
 
                         String sourceString = build.getProperty(JAVA_SOURCE_DIRECTORY_PROPERTY);
                         if (sourceString != null) {
                             File source = new File(sourceString);
                             if (source.exists()) {
                                 sources.add(source);
                                 directories.add(source);
                             }
                         }
 
                         String resourceString = build.getProperty(RESOURCE_DIRECTORY_PROPERTY);
                         if (resourceString != null) {
                             File resource = new File(resourceString);
                             if (resource.exists()) {
                                 resources.add(resource);
                                 directories.add(resource);
                             }
                         }
 
                         if (buildConnection instanceof JarURLConnection) {
                             URL jarUrl = ((JarURLConnection) buildConnection).getJarFileURL();
                             URLConnection jarConnection = jarUrl.openConnection();
                             InputStream jarInput = jarConnection.getInputStream();
                             try {
                                 long lastModified = jarConnection.getLastModified();
                                 for (File directory : directories) {
                                     LOGGER.info("Found sources in [{}] originally packaged in [{}]", directory, jarUrl);
                                     jars.put(directory, lastModified);
                                 }
                             } finally {
                                 jarInput.close();
                             }
                         }
 
                     } finally {
                         buildInput.close();
                     }
                 } catch (IOException ex) {
                 }
             }
         } catch (IOException ex) {
         }
 
         SOURCE_DIRECTORIES = Collections.unmodifiableSet(sources);
         RESOURCE_DIRECTORIES = Collections.unmodifiableSet(resources);
         JAR_LAST_MODIFIED_MAP = Collections.unmodifiableMap(jars);
     }
 
     public static Set<File> getSourceDirectories() {
         return SOURCE_DIRECTORIES;
     }
 
     public static Set<File> getResourceDirectories() {
         return RESOURCE_DIRECTORIES;
     }
 
     public static Long getJarLastModified(File directory) {
         return JAR_LAST_MODIFIED_MAP.get(directory);
     }
 
     /**
      * Returns an immuntable map of all web application source directories
      * keyed by their context paths.
      */
     public static Map<String, File> getWebappSourceDirectories(ServletContext context) {
         @SuppressWarnings("unchecked")
         Map<String, File> sourceDirectories = (Map<String, File>) context.getAttribute(WEBAPP_SOURCE_DIRECTORIES_ATTRIBUTE);
         if (sourceDirectories != null) {
             return sourceDirectories;
         }
 
         Map<String, File> unsorted = new HashMap<String, File>();
         try {
             addWebappSourceDirectories(context, unsorted, "/");
         } catch (IOException error) {
             return Collections.emptyMap();
         }
 
         List<String> prefixes = new ArrayList<String>(unsorted.keySet());
         Collections.sort(prefixes);
         Collections.reverse(prefixes);
 
         sourceDirectories = new LinkedHashMap<String, File>();
         for (String prefix : prefixes) {
             sourceDirectories.put(prefix, unsorted.get(prefix));
         }
 
         sourceDirectories = Collections.unmodifiableMap(sourceDirectories);
         context.setAttribute(WEBAPP_SOURCE_DIRECTORIES_ATTRIBUTE, sourceDirectories);
         return sourceDirectories;
     }
 
     private static void addWebappSourceDirectories(ServletContext context, Map<String, File> sourceDirectories, String path) throws IOException {
         @SuppressWarnings("unchecked")
         Set<String> children = (Set<String>) context.getResourcePaths(path);
 
         if (children != null) {
             for (String child : children) {
                 if (child.endsWith("/build.properties")) {
                     int webInfAt = child.indexOf("/WEB-INF/");
 
                     if (webInfAt > -1) {
                         InputStream buildInput = context.getResourceAsStream(child);
                         Properties buildProperties = new Properties();
 
                         try {
                             buildProperties.load(buildInput);
                         } finally {
                             buildInput.close();
                         }
 
                         File sourceDirectory = ObjectUtils.to(File.class, buildProperties.get(SourceFilter.WEBAPP_SOURCES_PROPERTY));
 
                         if (sourceDirectory.exists()) {
                             sourceDirectories.put(
                                     StringUtils.ensureEnd(child.substring(0, webInfAt), "/"),
                                     sourceDirectory);
                         }
                     }
 
                 } else if (child.endsWith("/")) {
                     addWebappSourceDirectories(context, sourceDirectories, child);
                 }
             }
         }
     }
 
     /**
      * Returns the original source file associated with the given {@code path}
      * in the given {@code context}.
      */
     public static File getWebappSource(ServletContext context, String path) {
         for (Map.Entry<String, File> entry : getWebappSourceDirectories(context).entrySet()) {
             String prefix = entry.getKey();
 
             if (path.startsWith(prefix)) {
                 return new File(entry.getValue(), path.substring(prefix.length()));
             }
         }
 
         return null;
     }
 
     /**
      * Returns all original source paths that begin with the given {@code path}
      * in the same format as {@link ServletContext#getResourcePaths}.
      */
     @SuppressWarnings("unchecked")
     public static Set<String> getResourcePaths(ServletContext context, String path) {
         File source = getWebappSource(context, path);
 
        if (source != null && source.exists()) {
             Set<String> paths = new LinkedHashSet<String>();
             String[] children = source.list();
 
             if (children != null) {
                 for (String child : children) {
                     String childPath = path + child;
 
                     if (new File(source, child).isDirectory()) {
                         childPath += "/";
                     }
                     paths.add(childPath);
                 }
             }
 
             return paths;
 
         } else {
             return (Set<String>) context.getResourcePaths(path);
         }
     }
 
     /**
      * Returns the original source as an input stream associated with the
      * given {@code path} in the given {@code context}.
      */
     public static InputStream getResourceAsStream(ServletContext context, String path) {
         File source = getWebappSource(context, path);
 
         if (source != null) {
             try {
                 return new FileInputStream(source);
             } catch (FileNotFoundException error) {
             }
         }
 
         return context.getResourceAsStream(path);
     }
 
     /**
      * Returns the source file associated with the given
      * {@code className}.
      *
      * @return May be {@code null} if there's no such file or if
      *         the source directory information isn't available.
      */
     public static File getSource(String className) {
         int dollarAt = className.indexOf('$');
         if (dollarAt > -1) {
             className = className.substring(0, dollarAt);
         }
 
         className = className.replace('.', File.separatorChar);
         for (File sourceDirectory : getSourceDirectories()) {
             File source = new File(sourceDirectory, className + ".java");
             if (source.exists()) {
                 return source;
             }
         }
 
         return null;
     }
 
     public static Object compileJava(String code) throws Exception {
         StringBuilder classPathsBuilder = new StringBuilder();
         for (ClassLoader loader = ObjectUtils.getCurrentClassLoader();
                 loader != null;
                 loader = loader.getParent()) {
             if (loader instanceof URLClassLoader) {
                 for (URL url : ((URLClassLoader) loader).getURLs()) {
                     classPathsBuilder.append(IoUtils.toFile(url, StringUtils.UTF_8).getPath());
                     classPathsBuilder.append(File.pathSeparator);
                 }
             }
         }
 
         MemoryFileManager fileManager = new MemoryFileManager(COMPILER.getStandardFileManager(null, null, null));
         try {
 
             DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
             List<String> options = new ArrayList<String>();
             options.add("-classpath");
             options.add(classPathsBuilder.toString());
             JavaFileObject source = new StringSource(code);
             JavaCompiler.CompilationTask task = COMPILER.getTask(null, fileManager, diagnostics, options, null, Arrays.asList(source));
 
             synchronized (COMPILER) {
                 if (task.call()) {
                     Set<Class<?>> classes = new HashSet<Class<?>>();
                     ClassLoader classLoader = fileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
                     for (String className : fileManager.classes.keySet()) {
                         classes.add(classLoader.loadClass(className));
                     }
                     return classes;
                 }
             }
 
             if (!diagnostics.getDiagnostics().isEmpty()) {
                 return diagnostics;
             } else {
                 throw new IllegalArgumentException(
                         "Can't find a static method without any parameters!");
             }
 
         } finally {
             fileManager.close();
         }
     }
 
     @SuppressWarnings("unchecked")
     public static Object evaluateJava(String code) throws Exception {
         Object result = compileJava(code);
 
         if (result instanceof Set) {
             for (Class<?> c : (Set<Class<?>>) result) {
                 for (Method method : c.getDeclaredMethods()) {
                     if (Modifier.isStatic(method.getModifiers()) &&
                             method.getReturnType() != Void.class &&
                             method.getParameterTypes().length == 0) {
 
                         method.setAccessible(true);
                         try {
                             return method.invoke(null);
                         } catch (InvocationTargetException ex) {
                             Throwable cause = ex.getCause();
                             throw cause instanceof Exception ? (Exception) cause : ex;
                         }
                     }
                 }
             }
         }
 
         return result;
     }
 
     // ---
 
     private static class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
 
         private final Map<String, byte[]> classes = new LinkedHashMap<String, byte[]>();
 
         public MemoryFileManager(JavaFileManager fileManager) {
             super(fileManager);
         }
 
         @Override
         public ClassLoader getClassLoader(Location location) {
             return new ByteArrayClassLoader(ObjectUtils.getCurrentClassLoader(), classes);
         }
 
         @Override
         public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
             return new ByteArrayClass();
         }
 
         @Override
         public boolean hasLocation(Location location) {
             return location == StandardLocation.CLASS_OUTPUT ||
                     location == StandardLocation.CLASS_PATH;
         }
 
         private class ByteArrayClass extends SimpleJavaFileObject {
 
             public ByteArrayClass() {
                 super(DEFAULT_URI, JavaFileObject.Kind.CLASS);
             }
 
             public OutputStream openOutputStream() {
                 return new FilterOutputStream(new ByteArrayOutputStream()) {
                     @Override
                     public void close() throws IOException {
                         byte[] bytecode = ((ByteArrayOutputStream) out).toByteArray();
                         String className = new ClassReader(bytecode).getClassName().replace('/', '.');
                         classes.put(className, bytecode);
                     }
                 };
             }
         }
     }
 
     public static class ByteArrayClassLoader extends ClassLoader {
 
         private final Map<String, byte[]> classes;
 
         public ByteArrayClassLoader(ClassLoader parent, Map<String, byte[]> classes) {
             super(parent);
             this.classes = classes;
         }
 
         @Override
         public Class<?> loadClass(String className) throws ClassNotFoundException {
             byte[] bytecode = classes.get(className);
             if (bytecode != null) {
                 return defineClass(className, bytecode, 0, bytecode.length);
             } else {
                 return super.loadClass(className);
             }
         }
     }
 
     private static class StringSource extends SimpleJavaFileObject {
 
         private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
                 "(?m)^[\\s\\p{javaJavaIdentifierPart}]*(?:class|interface)\\s+" +
                 "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)");
 
         private final String code;
 
         public StringSource(String code) {
             super(guessClassName(code), JavaFileObject.Kind.SOURCE);
             this.code = code;
         }
 
         private static URI guessClassName(String code) {
             Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(code);
             if (classNameMatcher.find()) {
                 return DEFAULT_URI.resolve(classNameMatcher.group(1) + JavaFileObject.Kind.SOURCE.extension);
             } else {
                 return DEFAULT_URI;
             }
         }
 
         @Override
         public CharSequence getCharContent(boolean ignoreEncodingErrors) {
             return code;
         }
     }
 
     // ---
 
     private static Class<?> AGENT_CLASS;
     private static final Instrumentation INSTRUMENTATION;
     private static final Map<String, String> JSP_SERVLET_PATHS_MAP = new HashMap<String, String>();
     private static final Map<String, String> JSP_LINE_NUMBERS_MAP = new HashMap<String, String>();
 
     private static final ClassFileTransformer JSP_CLASS_RECORDER = new ClassFileTransformer() {
 
         @Override
         public byte[] transform(
                 ClassLoader loader,
                 String className,
                 Class<?> classBeingRedefined,
                 ProtectionDomain protectionDomain,
                 byte[] bytecode) {
 
             ClassReader reader = new ClassReader(bytecode);
             String superName = reader.getSuperName();
 
             if ("org/apache/jasper/runtime/HttpJspBase".equals(superName)) {
                 ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
                 reader.accept(new SmapAdapter(writer, className), ClassReader.SKIP_FRAMES);
             }
 
             return null;
         }
     };
 
     private static class SmapAdapter extends ClassAdapter {
 
         private static final Pattern LINE_SECTION_PATTERN = Pattern.compile("(?s)(\\S+)\\s+\\*L\\s+(.*?)\\s*\\*E");
 
         private final String className;
 
         public SmapAdapter(ClassVisitor delegate, String initialClassName) {
             super(delegate);
             className = initialClassName;
         }
 
         @Override
         public void visitSource(String source, String debug) {
             if (!debug.startsWith("SMAP")) {
                 return;
             }
 
             Matcher lineSectionMatcher = LINE_SECTION_PATTERN.matcher(debug);
             if (lineSectionMatcher.find()) {
                 String name = className.replace('/', '.');
                 JSP_SERVLET_PATHS_MAP.put(name, lineSectionMatcher.group(1));
                 JSP_LINE_NUMBERS_MAP.put(name, lineSectionMatcher.group(2));
             }
         }
     }
 
     static {
         Class<?> agentClass = getAgentClass();
         Instrumentation instrumentation = null;
         if (agentClass != null) {
             try {
                 instrumentation = (Instrumentation) agentClass.getField("INSTRUMENTATION").get(null);
                 instrumentation.addTransformer(JSP_CLASS_RECORDER, true);
             } catch (Exception ex) {
             }
         }
         INSTRUMENTATION = instrumentation;
     }
 
     /**
      * Returns the agent class that provides the instrumentation
      * instance.
      *
      * @return May be {@code null}.
      */
     private static synchronized Class<?> getAgentClass() {
         try {
             AGENT_CLASS = ClassLoader.getSystemClassLoader().loadClass(Agent.class.getName());
             return AGENT_CLASS;
         } catch (ClassNotFoundException ex) {
         }
 
         Class<?> vmClass = ObjectUtils.getClassByName("com.sun.tools.attach.VirtualMachine");
         if (vmClass == null) {
             return null;
         }
 
         // Hack to guess this app's PID.
         RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
         String pid = runtime.getName();
         int atAt = pid.indexOf('@');
         if (atAt < 0) {
             LOGGER.info("Can't guess the VM PID!");
             return null;
         } else {
             pid = pid.substring(0, atAt);
         }
 
         Object vm = null;
         try {
             try {
 
                 // Hack around Mac OS X not using the correct
                 // temporary directory.
                 String newTmpdir = System.getenv("TMPDIR");
                 if (newTmpdir != null) {
                     String oldTmpdir = System.getProperty("java.io.tmpdir");
                     if (oldTmpdir != null) {
                         try {
                             System.setProperty("java.io.tmpdir", newTmpdir);
                             vm = vmClass.getMethod("attach", String.class).invoke(null, pid);
                         } finally {
                             System.setProperty("java.io.tmpdir", oldTmpdir);
                         }
                     }
                 }
 
                 // Create a temporary instrumentation agent JAR.
                 String agentName = Agent.class.getName();
                 File file = File.createTempFile(agentName, ".jar");
                 try {
 
                     Manifest manifest = new Manifest();
                     Attributes attributes = manifest.getMainAttributes();
                     attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
                     attributes.putValue("Agent-Class", Agent.class.getName());
                     attributes.putValue("Can-Redefine-Classes", "true");
                     attributes.putValue("Can-Retransform-Classes", "true");
 
                     JarOutputStream jar = new JarOutputStream(new FileOutputStream(file), manifest);
                     try {
                         String entryName = agentName.replace('.', '/') + ".class";
                         jar.putNextEntry(new JarEntry(entryName));
                         InputStream originalInput = CodeUtils.class.getResourceAsStream("/" + entryName);
                         try {
                             IoUtils.copy(originalInput, jar);
                         } finally {
                             originalInput.close();
                         }
                         jar.closeEntry();
                     } finally {
                         jar.close();
                     }
 
                     vmClass.getMethod("loadAgent", String.class).invoke(vm, file.getAbsolutePath());
                     AGENT_CLASS = ClassLoader.getSystemClassLoader().loadClass(Agent.class.getName());
 
                 } finally {
                     if (!file.delete()) {
                         file.deleteOnExit();
                     }
                 }
 
             } finally {
                 if (vm != null) {
                     vmClass.getMethod("detach").invoke(vm);
                 }
             }
 
         } catch (Exception ex) {
             LOGGER.info("Can't create an instrumentation instance!", ex);
         }
 
         return AGENT_CLASS;
     }
 
     /**
      * Exposes an instrumentation instance. While this class is technically
      * public, it shouldn't be accessed directly.
      * Use {@link CodeUtils#getInstrumentation} instead.
      */
     public static final class Agent {
 
         public static Instrumentation INSTRUMENTATION;
 
         public static void agentmain(String agentArguments, Instrumentation instrumentation) {
             INSTRUMENTATION = instrumentation;
         }
     }
 
     /**
      * Returns an instrumentation instance for redefining classes on the fly.
      *
      * @return May be {@code null}.
      */
     public static Instrumentation getInstrumentation() {
         return INSTRUMENTATION;
     }
 
     /**
      * For receiving notifications when classes are redefined through
      * {@link #redefineClasses}.
      */
     public static interface RedefineClassesListener {
         public void redefined(Set<Class<?>> classes);
     }
 
     private static final Set<RedefineClassesListener> REDEFINE_CLASSES_LISTENERS = new HashSet<RedefineClassesListener>();
 
     /**
      * Adds the given {@code listener} to be notified when classes
      * are redefined through {@link #redefineClasses}.
      *
      * @param listener If {@code null}, does nothing.
      */
     public static void addRedefineClassesListener(RedefineClassesListener listener) {
         if (listener != null) {
             REDEFINE_CLASSES_LISTENERS.add(listener);
         }
     }
 
     /**
      * Removes the given {@code listener} so that it's no longer notified
      * when classes are redefined through {@link #redefineClasses}.
      *
      * @param listener If {@code null}, does nothing.
      */
     public static void removeRedefineClassesListener(RedefineClassesListener listener) {
         if (listener != null) {
             REDEFINE_CLASSES_LISTENERS.remove(listener);
         }
     }
 
     /**
      * Redefines all classes according to the given {@code definitions}.
      *
      * @return Definitions that failed to redefine the class.
      * @see Instrumentation#redefineClasses
      */
     public static List<ClassDefinition> redefineClasses(List<ClassDefinition> definitions) {
         Set<Class<?>> successes = new HashSet<Class<?>>();
         List<ClassDefinition> failures = new ArrayList<ClassDefinition>();
         Instrumentation instrumentation = getInstrumentation();
 
         if (instrumentation == null) {
             failures.addAll(definitions);
 
         } else {
             for (ClassDefinition definition : definitions) {
                 try {
                     instrumentation.redefineClasses(definition);
                     Class<?> c = definition.getDefinitionClass();
                     successes.add(c);
 
                 } catch (Exception error) {
                     failures.add(definition);
                 }
             }
         }
 
         if (!successes.isEmpty()) {
             LOGGER.info("Redefined {}", successes);
 
             for (RedefineClassesListener listener : REDEFINE_CLASSES_LISTENERS) {
                 listener.redefined(successes);
             }
         }
 
         return failures;
     }
 
     public static String getJspServletPath(String className) {
         return JSP_SERVLET_PATHS_MAP.get(className);
     }
 
     public static int getJspLineNumber(String className, int javaLineNumber) {
         String jspLineNumbers = JSP_LINE_NUMBERS_MAP.get(className);
         if (jspLineNumbers == null) {
             return -1;
         }
 
         BufferedReader reader = new BufferedReader(new StringReader(jspLineNumbers));
         try {
             int at, inputStart, inputRepeat, outputStart, outputIncrement, offset;
             String input, output;
 
             for (String line; (line = reader.readLine()) != null; ) {
                 at = line.indexOf('#');
                 if (at > -1) {
                     line = line.substring(at + 1);
                 }
 
                 at = line.indexOf(':');
                 if (at < 0) {
                     continue;
                 }
 
                 input = line.substring(0, at);
                 output = line.substring(at + 1);
 
                 at = input.indexOf(',');
                 if (at < 0) {
                     inputStart = ObjectUtils.to(int.class, input);
                     inputRepeat = 1;
                 } else {
                     inputStart = ObjectUtils.to(int.class, input.substring(0, at));
                     inputRepeat = ObjectUtils.to(int.class, input.substring(at + 1));
                 }
 
                 at = output.indexOf(',');
                 if (at < 0) {
                     outputStart = ObjectUtils.to(int.class, output);
                     outputIncrement = 1;
                 } else {
                     outputStart = ObjectUtils.to(int.class, output.substring(0, at));
                     outputIncrement = ObjectUtils.to(int.class, output.substring(at + 1));
                 }
 
                 offset = javaLineNumber - outputStart;
                 if (offset >= 0 && offset < inputRepeat * outputIncrement) {
                     return inputStart + offset / outputIncrement;
                 }
             }
 
         } catch (IOException ex) {
         }
 
         return -1;
     }
 }
