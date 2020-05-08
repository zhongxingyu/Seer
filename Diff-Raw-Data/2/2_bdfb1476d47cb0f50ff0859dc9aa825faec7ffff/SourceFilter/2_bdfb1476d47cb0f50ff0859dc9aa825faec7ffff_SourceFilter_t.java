 package com.psddev.dari.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.instrument.ClassDefinition;
 import java.lang.instrument.Instrumentation;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.StandardLocation;
 import javax.tools.ToolProvider;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Enables rapid web application development by making source code changes
  * immediately available.
  *
  * <p>To configure, add these definitions to the {@code web.xml} deployment
  * descriptor file:
  *
  * <p><blockquote><pre>{@literal
 <filter>
     <filter-name>SourceFilter</filter-name>
     <filter-class>com.psddev.dari.util.SourceFilter</filter-class>
 </filter>
 <filter-mapping>
     <filter-name>SourceFilter</filter-name>
     <url-pattern>/*</url-pattern>
 </filter-mapping>
  * }</pre></blockquote>
  *
  * <p>And the application must include a {@code build.properties} file
  * that specifies the locations of the source code:
  *
  * <ul>
  * <li>{@link CodeUtils#JAVA_SOURCE_DIRECTORY_PROPERTY}
  * <li>{@link #WEBAPP_SOURCES_PROPERTY}
  * </ul>
  *
  * <p>You can skip this step if the project uses Apache Maven to manage
  * the build and inherits from {@code com.psddev:dari-parent}, because
  * that parent POM automatically adds those properties during the
  * {@code generate-resources} phase.
  */
 public class SourceFilter extends AbstractFilter {
 
     /**
      * Build property that specifies the directory containing the
      * web application sources, such as JSPs.
      */
     public static final String WEBAPP_SOURCES_PROPERTY = "webappSourceDirectory";
 
     public static final String DEFAULT_INTERCEPT_PATH = "/_sourceFilter";
     public static final String INTERCEPT_PATH_SETTING = "dari/sourceFilterInterceptPath";
 
     public static final String DEFAULT_RELOADER_PATH = "/reloader/";
     public static final String RELOADER_PATH_SETTING = "dari/sourceFilterReloaderPath";
     public static final String RELOADER_ACTION_PARAMETER = "action";
     public static final String RELOADER_PING_ACTION = "ping";
     public static final String RELOADER_RELOAD_ACTION = "reload";
     public static final String RELOADER_CONTEXT_PATH_PARAMETER = "contextPath";
     public static final String RELOADER_REQUEST_PATH_PARAMETER = "requestPath";
 
     private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
     private static final Logger LOGGER = LoggerFactory.getLogger(SourceFilter.class);
 
     private static final String CLASSES_PATH = "/WEB-INF/classes/";
     private static final String BUILD_PROPERTIES_PATH = "build.properties";
     private static final String ISOLATING_RESPONSE_ATTRIBUTE = SourceFilter.class.getName() + ".isolatingResponse";
     private static final String IS_ISOLATION_DONE_ATTRIBUTE = SourceFilter.class.getName() + ".isIsolationDone";
     private static final String COPIED_ATTRIBUTE = SourceFilter.class.getName() + ".copied";
 
     private static final String CATALINA_BASE_PROPERTY = "catalina.base";
     private static final String RELOADER_MAVEN_ARTIFACT_ID = "dari-reloader-tomcat6";
     private static final String RELOADER_MAVEN_VERSION = "2.0-SNAPSHOT";
     private static final String RELOADER_MAVEN_URL = "http://public.psddev.com/maven/com/psddev/" + RELOADER_MAVEN_ARTIFACT_ID + "/" + RELOADER_MAVEN_VERSION + "/";
     private static final Pattern BUILD_NUMBER_PATTERN = Pattern.compile("<buildNumber>([^<]*)</buildNumber>");
     private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("<timestamp>([^<]*)</timestamp>");
 
     private File classOutput;
     private final Set<File> javaSourcesSet = new HashSet<File>();
     private final Map<String, File> webappSourcesMap = new HashMap<String, File>();
     private final Map<JavaFileObject, Long> sourceModifieds = new HashMap<JavaFileObject, Long>();
     private final Map<String, Date> changedClasses = new TreeMap<String, Date>();
 
     // --- AbstractFilter support ---
 
     @Override
     protected void doInit() {
         if (COMPILER == null) {
             LOGGER.info("Java compiler not available!");
             return;
         }
 
         ServletContext context = getServletContext();
         String classOutputString = context.getRealPath(CLASSES_PATH);
         if (classOutputString == null) {
             LOGGER.info("Can't get the real path to [{}]!", CLASSES_PATH);
             return;
         }
 
         classOutput = new File(classOutputString);
         if (classOutput.exists()) {
             LOGGER.info("Saving recompiled Java classes to [{}]", classOutput);
         } else {
             LOGGER.info("[{}] doesn't exist!", classOutput);
             classOutput = null;
             return;
         }
 
         javaSourcesSet.addAll(CodeUtils.getSourceDirectories());
 
         processWarBuildProperties(context, "");
         for (String contextPath : JspUtils.getEmbeddedSettings(context).keySet()) {
             processWarBuildProperties(context, contextPath);
         }
     }
 
     /**
      * Processes the build properties file associated with the given
      * {@code context} and {@code contextPath} and adds its source
      * directories.
      *
      * @param context Can't be {@code null}.
      * @param contextPath Can't be {@code null}.
      */
     private void processWarBuildProperties(ServletContext context, String contextPath) {
         InputStream buildPropertiesInput = context.getResourceAsStream(contextPath + CLASSES_PATH + BUILD_PROPERTIES_PATH);
         if (buildPropertiesInput == null) {
             return;
         }
 
         try {
             try {
                 Properties buildProperties = new Properties();
                 buildProperties.load(buildPropertiesInput);
 
                 String javaSourcesString = buildProperties.getProperty(CodeUtils.JAVA_SOURCE_DIRECTORY_PROPERTY);
                 if (javaSourcesString != null) {
                     File javaSources = new File(javaSourcesString);
                     if (javaSources.exists()) {
                         javaSourcesSet.add(javaSources);
                         LOGGER.info("Found Java sources in [{}]", javaSources);
                     }
                 }
 
                 String webappSourcesString = buildProperties.getProperty(WEBAPP_SOURCES_PROPERTY);
                 if (webappSourcesString != null) {
                     File webappSources = new File(webappSourcesString);
                     if (webappSources.exists()) {
                         LOGGER.info("Copying webapp sources from [{}] to [{}/]", webappSources, contextPath);
                         webappSourcesMap.put(contextPath, webappSources);
                     }
                 }
 
             } finally {
                 buildPropertiesInput.close();
             }
 
         } catch (IOException ex) {
         }
     }
 
     @Override
     protected void doDestroy() {
         classOutput = null;
         javaSourcesSet.clear();
         webappSourcesMap.clear();
         sourceModifieds.clear();
         changedClasses.clear();
     }
 
     @Override
     protected void doDispatch(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         if (Settings.isProduction()) {
             chain.doFilter(request, response);
             return;
         }
 
         if (request.getAttribute(IS_ISOLATION_DONE_ATTRIBUTE) != null) {
             return;
         }
 
         IsolatingResponse isolatingResponse = (IsolatingResponse) request.getAttribute(ISOLATING_RESPONSE_ATTRIBUTE);
         if (isolatingResponse != null) {
             if (!JspUtils.getCurrentServletPath(request).equals(isolatingResponse.jsp)) {
                 isolatingResponse = null;
 
             } else {
                 @SuppressWarnings("all")
                 HtmlWriter html = new HtmlWriter(isolatingResponse.getResponse().getWriter());
                 html.putAllStandardDefaults();
 
                 try {
                     StringWriter writer = new StringWriter();
                     JspUtils.include(request, response, writer, request.getParameter("_draft"));
                     html.writeStart("pre");
                         html.writeHtml(writer.toString().trim());
                     html.writeEnd();
 
                 } catch (Exception ex) {
                     html.writeStart("pre", "class", "alert alert-error");
                         html.writeObject(ex);
                     html.writeEnd();
 
                 } finally {
                     request.setAttribute(IS_ISOLATION_DONE_ATTRIBUTE, Boolean.TRUE);
                 }
                 return;
             }
         }
 
         copyWebappSource(request);
         super.doDispatch(request, response, chain);
     }
 
     @Override
     protected void doRequest(
             final HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws IOException, ServletException {
 
         COPY_RESOURCES.get();
 
         // Intercept special actions.
         if (!ObjectUtils.isBlank(request.getParameter("_reload")) &&
                 isReloaderAvailable(request)) {
             compileJavaSources();
             response.sendRedirect(StringUtils.addQueryParameters(
                     getReloaderPath(),
                     RELOADER_CONTEXT_PATH_PARAMETER, request.getContextPath(),
                     RELOADER_REQUEST_PATH_PARAMETER, JspUtils.getAbsolutePath(request, "", "_reload", null),
                     RELOADER_ACTION_PARAMETER, RELOADER_RELOAD_ACTION));
             return;
         }
 
         String servletPath = request.getServletPath();
         if (servletPath.startsWith(getInterceptPath())) {
             String action = request.getParameter("action");
 
             if ("ping".equals(action)) {
                 response.setContentType("text/plain");
                 response.setCharacterEncoding("UTF-8");
                 response.getWriter().write("OK");
 
             } else if ("install".equals(action)) {
                 if (isReloaderAvailable(request)) {
                     String requestPath = request.getParameter("requestPath");
                     response.sendRedirect(ObjectUtils.isBlank(requestPath) ? "/" : requestPath);
                 } else {
                     @SuppressWarnings("all")
                     ReloaderInstaller installer = new ReloaderInstaller(request, response);
                     installer.writeStart();
                 }
 
             } else {
                 throw new IllegalArgumentException(String.format(
                         "[%s] isn't a valid intercept action!", action));
             }
             return;
         }
 
         String contentType = ObjectUtils.getContentType(servletPath);
         if (contentType != null &&
                 (contentType.startsWith("image/") ||
                 contentType.startsWith("video/") ||
                 contentType.equals("text/css") ||
                 contentType.equals("text/javascript"))) {
             chain.doFilter(request, response);
             return;
         }
 
         final DiagnosticCollector<JavaFileObject> errors = compileJavaSources();
         final boolean hasBackgroundTasks;
         if (errors == null &&
                 !changedClasses.isEmpty() &&
                 !JspUtils.isAjaxRequest(request)) {
 
             if (hasBackgroundTasks()) {
                 hasBackgroundTasks = true;
 
             } else if (isReloaderAvailable(request)) {
                 changedClasses.clear();
                 response.sendRedirect(StringUtils.addQueryParameters(
                         getReloaderPath(),
                         RELOADER_CONTEXT_PATH_PARAMETER, request.getContextPath(),
                         RELOADER_REQUEST_PATH_PARAMETER, JspUtils.getAbsolutePath(request, ""),
                         RELOADER_ACTION_PARAMETER, RELOADER_RELOAD_ACTION));
                 return;
 
             } else {
                 hasBackgroundTasks = false;
             }
 
         } else {
             hasBackgroundTasks = false;
         }
 
         IsolatingResponse isolatingResponse = null;
         String jsp = request.getParameter("_jsp");
         if (!ObjectUtils.isBlank(jsp)) {
             response.setContentType("text/plain");
             isolatingResponse = new IsolatingResponse(response, jsp);
             response = isolatingResponse;
             request.setAttribute(ISOLATING_RESPONSE_ATTRIBUTE, isolatingResponse);
         }
 
         chain.doFilter(request, response);
         if (errors == null && (
                 changedClasses.isEmpty() ||
                 JspUtils.isAjaxRequest(request) ||
                 isolatingResponse != null)) {
             return;
         }
 
         // Can't reload automatically so at least let the user know
         // if viewing an HTML page.
         String responseContentType = response.getContentType();
         if (responseContentType == null ||
                 !responseContentType.startsWith("text/html")) {
             return;
         }
 
         try {
             new HtmlWriter(response.getWriter()) {{
                 putDefault(StackTraceElement.class, HtmlFormatter.STACK_TRACE_ELEMENT);
                 putDefault(Throwable.class, HtmlFormatter.THROWABLE);
 
                 write("<div style=\"" +
                         "background: rgba(204, 0, 0, 0.8);" +
                         "-moz-border-radius: 5px;" +
                         "-webkit-border-radius: 5px;" +
                         "border-radius: 5px;" +
                         "color: white;" +
                         "font-family: 'Helvetica Neue', 'Arial', sans-serif;" +
                         "font-size: 13px;" +
                         "line-height: 18px;" +
                         "margin: 0;" +
                         "max-height: 50%;" +
                         "max-width: 350px;" +
                         "overflow: auto;" +
                         "padding: 5px;" +
                         "position: fixed;" +
                         "right: 5px;" +
                         "top: 5px;" +
                         "word-wrap: break-word;" +
                         "z-index: 1000000;" +
                         "\">");
 
                     if (errors == null) {
                         if (hasBackgroundTasks) {
                             writeHtml("The application wasn't reloaded automatically because there are background tasks running!");
 
                         } else {
                             writeHtml("The application must be reloaded before the changes to these classes become visible. ");
                             writeStart("a",
                                     "href", JspUtils.getAbsolutePath(request, getInterceptPath(),
                                             "action", "install",
                                             "requestPath", JspUtils.getAbsolutePath(request, "")),
                                     "style", "color: white; text-decoration: underline;");
                                 writeHtml("Install the reloader");
                             writeEnd();
                             writeHtml(" to automate this process.");
                             writeTag("br");
                             writeTag("br");
 
                             for (Map.Entry<String, Date> entry : changedClasses.entrySet()) {
                                 writeHtml(entry.getKey());
                                 writeHtml(" - ");
                                 writeObject(entry.getValue());
                                 writeTag("br");
                             }
                         }
 
                     } else {
                         writeHtml("Syntax errors!");
                         writeStart("ol");
                             for (Diagnostic<?> diagnostic : errors.getDiagnostics()) {
                                 if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                                     writeStart("li", "data-line", diagnostic.getLineNumber(), "data-column", diagnostic.getColumnNumber());
                                         writeHtml(diagnostic.getMessage(null));
                                     writeEnd();
                                 }
                             }
                         writeEnd();
                     }
 
                 write("</div>");
             }};
         } catch (Exception ex) {
         }
     }
 
     /**
      * Returns the path that intercepts all special actions.
      *
      * @return Always starts and ends with a slash.
      */
     private static String getInterceptPath() {
         return StringUtils.ensureStart(Settings.getOrDefault(String.class, INTERCEPT_PATH_SETTING, DEFAULT_INTERCEPT_PATH), "/");
     }
 
     /**
      * Returns the path to the application that can reload this one.
      *
      * @return Always starts and ends with a slash.
      */
     private static String getReloaderPath() {
         return StringUtils.ensureSurrounding(Settings.getOrDefault(String.class, RELOADER_PATH_SETTING, DEFAULT_RELOADER_PATH), "/");
     }
 
     /**
      * Returns {@code true} if the reloader is available in the same
      * server as this application.
      *
      * @param request Can't be {@code null}.
      */
     private boolean isReloaderAvailable(HttpServletRequest request) {
         String servletPath = request.getServletPath();
         String reloaderPath = getReloaderPath();
 
        if (!servletPath.startsWith(reloaderPath)) {
             try {
                 URL pingUrl = new URL(StringUtils.addQueryParameters(
                         JspUtils.getHostUrl(request) + reloaderPath,
                         RELOADER_ACTION_PARAMETER, RELOADER_PING_ACTION));
 
                 // To avoid infinite redirects in case the ping hits this
                 // application.
                 URLConnection pingConnection = pingUrl.openConnection();
                 if (pingConnection instanceof HttpURLConnection) {
                     ((HttpURLConnection) pingConnection).setInstanceFollowRedirects(false);
                 }
 
                 InputStream pingInput = pingConnection.getInputStream();
                 try {
                     return "OK".equals(IoUtils.toString(pingInput, StringUtils.UTF_8));
                 } finally {
                     pingInput.close();
                 }
 
             } catch (IOException ex) {
             }
         }
 
         return false;
     }
 
     /** Returns {@code true} if there are any background tasks running. */
     private boolean hasBackgroundTasks() {
         for (TaskExecutor executor : TaskExecutor.Static.getAll()) {
             String executorName = executor.getName();
             if (!("Periodic Caches".equals(executorName) ||
                     "Miscellaneous Tasks".equals(executorName))) {
                 for (Object task : executor.getTasks()) {
                     if (task instanceof Task && ((Task) task).isRunning()) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     /** Copies all resources. */
     private PullThroughValue<Void> COPY_RESOURCES = new PullThroughValue<Void>() {
 
         @Override
         protected boolean isExpired(Date lastProduce) {
             return System.currentTimeMillis() - lastProduce.getTime() > 1000;
         }
 
         @Override
         protected Void produce() throws IOException {
             if (classOutput != null) {
                 for (File resourceDirectory : CodeUtils.getResourceDirectories()) {
                     Long jarModified = CodeUtils.getJarLastModified(resourceDirectory);
                     copy(resourceDirectory, jarModified, resourceDirectory);
                 }
             }
             return null;
         }
 
         private void copy(File resourceDirectory, Long jarModified, File resource) throws IOException {
             if (resource.isDirectory()) {
                 for (File child : resource.listFiles()) {
                     copy(resourceDirectory, jarModified, child);
                 }
 
             } else {
                 File output = new File(classOutput, resource.toString().substring(resourceDirectory.toString().length()).replace(File.separatorChar, '/'));
 
                 long resourceModified = resource.lastModified();
                 long outputModified = output.lastModified();
                 if ((jarModified == null || resourceModified > jarModified) &&
                         resourceModified > outputModified) {
                     IoUtils.copy(resource, output);
                     LOGGER.info("Copied [{}]", resource);
                 }
             }
         }
     };
 
     /**
      * Compile any Java source files that's changed and redefine them
      * in place if possible.
      */
     private DiagnosticCollector<JavaFileObject> compileJavaSources() throws IOException {
         if (javaSourcesSet.isEmpty()) {
             return null;
         }
 
         StandardJavaFileManager fileManager = COMPILER.getStandardFileManager(null, null, null);
         Set<JavaFileObject> newSourceFiles = new HashSet<JavaFileObject>();
         Map<JavaFileObject, String> expectedOutputFiles = new HashMap<JavaFileObject, String>();
         Map<String, Date> notRedefinedClasses = new HashMap<String, Date>();
         List<ClassDefinition> toBeRedefined = new ArrayList<ClassDefinition>();
 
         try {
             fileManager.setLocation(StandardLocation.SOURCE_PATH, javaSourcesSet);
             fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(classOutput));
 
             for (JavaFileObject sourceFile : fileManager.list(
                     StandardLocation.SOURCE_PATH,
                     "",
                     Collections.singleton(JavaFileObject.Kind.SOURCE),
                     true)) {
 
                 String className = fileManager.inferBinaryName(StandardLocation.SOURCE_PATH, sourceFile);
                 JavaFileObject outputFile = fileManager.getJavaFileForOutput(
                         StandardLocation.CLASS_OUTPUT,
                         className,
                         JavaFileObject.Kind.CLASS,
                         null);
 
                 // Did the source file originate from a JAR?
                 Long jarModified = null;
                 String sourcePath = sourceFile.toUri().getPath();
                 for (File javaSources : javaSourcesSet) {
                     if (sourcePath.startsWith(javaSources.getPath())) {
                         jarModified = CodeUtils.getJarLastModified(javaSources);
                         break;
                     }
                 }
 
                 long sourceModified = sourceFile.getLastModified();
                 Long outputModified = outputFile.getLastModified();
                 if ((jarModified == null || sourceModified > jarModified) &&
                         ((outputModified != 0 && sourceModified > outputModified) ||
                         (outputModified == 0 &&
                                 ((outputModified = sourceModifieds.get(outputFile)) == null ||
                                 sourceModified > outputModified)))) {
                     newSourceFiles.add(sourceFile);
                     expectedOutputFiles.put(outputFile, className);
                     sourceModifieds.put(outputFile, sourceModified);
                 }
             }
 
             if (!newSourceFiles.isEmpty()) {
                 LOGGER.info("Recompiling {}", newSourceFiles);
 
                 // Compiler can't use the current class loader so try to
                 // guess all of its class paths.
                 Set<File> classPaths = new LinkedHashSet<File>();
                 for (ClassLoader loader = ObjectUtils.getCurrentClassLoader();
                         loader != null;
                         loader = loader.getParent()) {
                     if (loader instanceof URLClassLoader) {
                         for (URL url : ((URLClassLoader) loader).getURLs()) {
                             File file = IoUtils.toFile(url, StringUtils.UTF_8);
                             if (file != null) {
                                 classPaths.add(file);
                             }
                         }
                     }
                 }
 
                 fileManager.setLocation(StandardLocation.CLASS_PATH, classPaths);
                 DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
 
                 // Compiler can't process multiple tasks at the same time.
                 synchronized (COMPILER) {
 
                     // File systems can't usually handle microsecond resolution.
                     long compileStart = System.currentTimeMillis() / 1000 * 1000;
 
                     if (!COMPILER.getTask(null, fileManager, diagnostics, Arrays.asList("-g"), null, newSourceFiles).call()) {
                         return diagnostics;
 
                     } else {
                         Set<Class<? extends ClassEnhancer>> enhancerClasses = ObjectUtils.findClasses(ClassEnhancer.class);
 
                         for (JavaFileObject outputFile : fileManager.list(
                                 StandardLocation.CLASS_OUTPUT,
                                 "",
                                 Collections.singleton(JavaFileObject.Kind.CLASS),
                                 true)) {
                             if (outputFile.getLastModified() < compileStart) {
                                 continue;
                             }
 
                             InputStream input = outputFile.openInputStream();
                             byte[] bytecode;
                             try {
                                 bytecode = IoUtils.toByteArray(input);
                             } finally {
                                 input.close();
                             }
 
                             byte[] enhancedBytecode = ClassEnhancer.Static.enhance(bytecode, enhancerClasses);
                             if (enhancedBytecode != null) {
                                 bytecode = enhancedBytecode;
                             }
 
                             OutputStream output = outputFile.openOutputStream();
                             try {
                                 output.write(bytecode);
                             } finally {
                                 output.close();
                             }
 
                             String outputClassName = fileManager.inferBinaryName(StandardLocation.CLASS_OUTPUT, outputFile);
                             notRedefinedClasses.put(outputClassName, new Date(outputFile.getLastModified()));
 
                             Class<?> outputClass = ObjectUtils.getClassByName(outputClassName);
                             if (outputClass != null) {
                                 toBeRedefined.add(new ClassDefinition(outputClass, bytecode));
                             }
                         }
 
                         // Try to redefine the classes in place.
                         List<ClassDefinition> failures = CodeUtils.redefineClasses(toBeRedefined);
                         toBeRedefined.removeAll(failures);
 
                         for (ClassDefinition success : toBeRedefined) {
                             notRedefinedClasses.remove(success.getDefinitionClass().getName());
                         }
 
                         if (!failures.isEmpty() && LOGGER.isInfoEnabled()) {
                             StringBuilder messageBuilder = new StringBuilder();
                             messageBuilder.append("Can't redefine [");
                             for (ClassDefinition failure : failures) {
                                 messageBuilder.append(failure.getDefinitionClass().getName());
                                 messageBuilder.append(", ");
                             }
                             messageBuilder.setLength(messageBuilder.length() - 2);
                             messageBuilder.append("]!");
                         }
                     }
                 }
             }
 
         } finally {
             fileManager.close();
         }
 
         // Remember all classes that's changed but not yet redefined.
         changedClasses.putAll(notRedefinedClasses);
         return null;
     }
 
     /**
      * Copies the webapp source associated with the given {@code request}.
      *
      * @param request Can't be {@code null}.
      */
     private void copyWebappSource(HttpServletRequest request) throws IOException {
         ServletContext context = getServletContext();
         String path = JspUtils.getCurrentServletPath(request);
         if (path.startsWith("/WEB-INF/_draft/")) {
             return;
         }
 
         String contextPath = JspUtils.getEmbeddedContextPath(context, path);
         File webappSources = webappSourcesMap.get(contextPath);
         if (webappSources == null) {
             return;
         }
 
         String outputFileString = context.getRealPath(path);
         if (outputFileString == null) {
             return;
         }
 
         @SuppressWarnings("unchecked")
         Set<String> copied = (Set<String>) request.getAttribute(COPIED_ATTRIBUTE);
         if (copied == null) {
             copied = new HashSet<String>();
             request.setAttribute(COPIED_ATTRIBUTE, copied);
         }
         if (copied.contains(outputFileString)) {
             return;
         } else {
             copied.add(outputFileString);
         }
 
         File sourceFile = new File(webappSources, path.substring(contextPath.length()).replace('/', File.separatorChar));
         File outputFile = new File(outputFileString);
         if (sourceFile.isDirectory() ||
                 outputFile.isDirectory()) {
             return;
         }
 
         if (sourceFile.exists()) {
             if (!outputFile.exists()) {
                 File outputParent = outputFile.getParentFile();
                 if (!outputParent.exists()) {
                     outputParent.mkdirs();
                 }
             } else if (sourceFile.lastModified() <= outputFile.lastModified()) {
                 return;
             }
 
             IoUtils.copy(sourceFile, outputFile);
             LOGGER.info("Copied [{}]", sourceFile);
 
         } else if (outputFile.exists() &&
                 !outputFile.isDirectory()) {
             LOGGER.info("[{}] disappeared!", sourceFile);
         }
     }
 
     /** {@linkplain SourceFilter} utility methods. */
     public static final class Static {
 
         private Static() {
         }
 
         /**
          * Returns the servlet path for pinging this web application to
          * make sure that it's running.
          *
          * @return Never {@code null}.
          */
         public static String getInterceptPingPath() {
             return StringUtils.addQueryParameters(getInterceptPath(), "action", "ping");
         }
 
         /** @deprecated Use {@link CodeUtils#getInstrumentation} instead. */
         @Deprecated
         public static Instrumentation getInstrumentation() {
             return CodeUtils.getInstrumentation();
         }
     }
 
     /** Installs an web application that can reload other web applications. */
     private class ReloaderInstaller extends HtmlWriter {
 
         private final HttpServletRequest request;
         private final HttpServletResponse response;
 
         public ReloaderInstaller(
                 HttpServletRequest request,
                 HttpServletResponse response)
                 throws IOException {
 
             super(response.getWriter());
             this.request = request;
             this.response = response;
         }
 
         public void writeStart() throws IOException {
             response.setContentType("text/html");
             response.setCharacterEncoding("UTF-8");
 
             writeTag("!doctype html");
             writeStart("html");
                 writeStart("head");
 
                     writeStart("title").writeHtml("Installing Reloader").writeEnd();
 
                     writeStart("link",
                             "href", JspUtils.getAbsolutePath(request, "/_resource/bootstrap/css/bootstrap.css"),
                             "rel", "stylesheet",
                             "type", "text/css");
                     writeStart("style", "type", "text/css");
                         write(".hero-unit { background: transparent; left: 0; margin: -72px 0 0 60px; padding: 0; position: absolute; top: 50%; }");
                         write(".hero-unit h1 { line-height: 1.33; }");
                     writeEnd();
 
                 writeEnd();
                 writeStart("body");
 
                     writeStart("div", "class", "hero-unit");
                         writeStart("h1");
                             writeHtml("Installing Reloader");
                         writeEnd();
                         try {
                             writeStart("ul", "class", "muted");
                             try {
                                 flush();
                                 install();
                                 writeStart("script", "type", "text/javascript");
                                     write("location.href = '" + StringUtils.escapeJavaScript(
                                             JspUtils.getAbsolutePath(request, "")) + "';");
                                 writeEnd();
                             } finally {
                                 writeEnd();
                             }
                         } catch (Exception ex) {
                             writeObject(ex);
                         }
                     writeEnd();
 
                 writeEnd();
             writeEnd();
         }
 
         private void addProgress(Object... messageParts) throws IOException {
             writeStart("li");
                 for (int i = 0, length = messageParts.length; i < length; ++ i) {
                     Object part = messageParts[i];
                     if (i % 2 == 0) {
                         writeHtml(part);
                     } else {
                         writeStart("em");
                             writeHtml(part);
                         writeEnd();
                     }
                 }
             writeEnd();
             flush();
         }
 
         private void install() throws IOException {
             String catalinaBase = System.getProperty(CATALINA_BASE_PROPERTY);
             if (ObjectUtils.isBlank(catalinaBase)) {
                 throw new IllegalStateException(String.format(
                         "[%s] system property isn't set!", CATALINA_BASE_PROPERTY));
             }
 
             URL metadataUrl = new URL(RELOADER_MAVEN_URL + "maven-metadata.xml");
             String metadata = IoUtils.toString(metadataUrl);
             addProgress("Looking for it using ", metadataUrl);
 
             Matcher timestampMatcher = TIMESTAMP_PATTERN.matcher(metadata);
             if (!timestampMatcher.find()) {
                 throw new IllegalStateException("No timestamp in Maven metadata!");
             }
 
             Matcher buildNumberMatcher = BUILD_NUMBER_PATTERN.matcher(metadata);
             if (!buildNumberMatcher.find()) {
                 throw new IllegalStateException("No build number in Maven metadata!");
             }
 
             File webappsDirectory = new File(catalinaBase, "webapps");
             File war = File.createTempFile("dari-reloader-", null, webappsDirectory);
             try {
 
                 URL warUrl = new URL(
                         RELOADER_MAVEN_URL +
                         RELOADER_MAVEN_ARTIFACT_ID + "-" +
                         RELOADER_MAVEN_VERSION.replace("-SNAPSHOT", "") + "-" +
                         timestampMatcher.group(1) + "-" +
                         buildNumberMatcher.group(1) + ".war");
                 addProgress("Downloading it from ", warUrl);
                 addProgress("Saving it to ", war);
 
                 InputStream warInput = warUrl.openStream();
                 try {
                     FileOutputStream warOutput = new FileOutputStream(war);
                     try {
                         IoUtils.copy(warInput, warOutput);
                     } finally {
                         warOutput.close();
                     }
                 } finally {
                     warInput.close();
                 }
 
                 String reloaderPath = getReloaderPath();
                 reloaderPath = reloaderPath.substring(1, reloaderPath.length() - 1);
                 File reloaderWar = new File(webappsDirectory, reloaderPath + ".war");
 
                 reloaderWar.delete();
                 new File(catalinaBase,
                         "conf" + File.separator +
                         "Catalina" + File.separator +
                         "localhost" + File.separator +
                         reloaderPath + ".xml").
                         delete();
 
                 addProgress("Deploying it to ", "/" + reloaderPath);
                 war.renameTo(reloaderWar);
 
                 for (int i = 0; i < 20; ++ i) {
                     if (isReloaderAvailable(request)) {
                         addProgress("Finished!");
                         return;
                     }
 
                     try {
                         Thread.sleep(2000);
                     } catch (InterruptedException ex) {
                         break;
                     }
                 }
 
                 throw new IllegalStateException("Can't deploy!");
 
             } finally {
                 war.delete();
             }
         }
     }
 
     private static class IsolatingResponse extends HttpServletResponseWrapper {
 
         public final String jsp;
 
         private final ServletOutputStream output = new ServletOutputStream() {
             @Override
             public void write(int b) {
             }
         };
 
         private final PrintWriter writer = new PrintWriter(output);
 
         public IsolatingResponse(HttpServletResponse response, String jsp) {
             super(response);
             this.jsp = jsp;
         }
 
         @Override
         public ServletOutputStream getOutputStream() throws IOException {
             return output;
         }
 
         @Override
         public PrintWriter getWriter() throws IOException {
             return writer;
         }
     }
 }
