 /*
  * This file is part of experimaestro.
  * Copyright (c) 2012 B. Piwowarski <benjamin@bpiwowar.net>
  *
  * experimaestro is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * experimaestro is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package sf.net.experimaestro.manager.js;
 
 import bpiwowar.argparser.utils.Introspection;
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.log4j.Hierarchy;
 import org.apache.log4j.Level;
 import org.mozilla.javascript.Callable;
 import org.mozilla.javascript.ConsString;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.EvaluatorException;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.NativeObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.Undefined;
 import org.mozilla.javascript.UniqueTag;
 import org.mozilla.javascript.Wrapper;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import sf.net.experimaestro.connectors.Connector;
 import sf.net.experimaestro.connectors.SingleHostConnector;
 import sf.net.experimaestro.connectors.XPMConnector;
 import sf.net.experimaestro.connectors.XPMProcess;
 import sf.net.experimaestro.connectors.XPMProcessBuilder;
 import sf.net.experimaestro.exceptions.ExperimaestroCannotOverwrite;
 import sf.net.experimaestro.exceptions.ExperimaestroRuntimeException;
 import sf.net.experimaestro.exceptions.ValueMismatchException;
 import sf.net.experimaestro.exceptions.XPMRhinoException;
 import sf.net.experimaestro.manager.AlternativeType;
 import sf.net.experimaestro.manager.Manager;
 import sf.net.experimaestro.manager.NSContext;
 import sf.net.experimaestro.manager.QName;
 import sf.net.experimaestro.manager.Repository;
 import sf.net.experimaestro.manager.TaskFactory;
 import sf.net.experimaestro.manager.XPMXPathFunctionResolver;
 import sf.net.experimaestro.scheduler.CommandArgument;
 import sf.net.experimaestro.scheduler.CommandArguments;
 import sf.net.experimaestro.scheduler.CommandLineTask;
 import sf.net.experimaestro.scheduler.Dependency;
 import sf.net.experimaestro.scheduler.Resource;
 import sf.net.experimaestro.scheduler.ResourceData;
 import sf.net.experimaestro.scheduler.ResourceLocator;
 import sf.net.experimaestro.scheduler.ResourceState;
 import sf.net.experimaestro.scheduler.Scheduler;
 import sf.net.experimaestro.scheduler.TokenResource;
 import sf.net.experimaestro.server.TasksServlet;
 import sf.net.experimaestro.utils.Cleaner;
 import sf.net.experimaestro.utils.JSUtils;
 import sf.net.experimaestro.utils.XMLUtils;
 import sf.net.experimaestro.utils.io.LoggerPrintWriter;
 import sf.net.experimaestro.utils.log.Logger;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathFunctionResolver;
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import static java.lang.String.format;
 import static sf.net.experimaestro.utils.JSUtils.unwrap;
 
 /**
  * This class contains both utility static methods and functions that can be
  * called from javascript
  *
  * @author B. Piwowarski <benjamin@bpiwowar.net>
  */
 
 /**
  * @author B. Piwowarski <benjamin@bpiwowar.net>
  */
 public class XPMObject {
 
     final static private Logger LOGGER = Logger.getLogger();
     public static final String DEFAULT_GROUP = "XPM_DEFAULT_GROUP";
 
     /**
      * The experiment repository
      */
     private final Repository repository;
 
     /**
      * Our scope (global among javascripts)
      */
     private final Scriptable scope;
 
     /**
      * The context (local)
      */
     private Context context;
 
     /**
      * The task scheduler
      */
     private final Scheduler scheduler;
 
     /**
      * The environment
      */
     private final Map<String, String> environment;
 
     /**
      * The connector for default inclusion
      */
     ResourceLocator currentResourceLocator;
 
     /**
      * Properties set by the script that will be returned
      */
     Map<String, Object> properties = new HashMap<>();
 
     /**
      * Logging should be directed to an output
      */
     final Hierarchy loggerRepository;
 
     /**
      * Root logger
      */
     private Logger rootLogger;
 
     /**
      * Default group for new jobs
      */
     String defaultGroup = "";
 
     /**
      * Default locks for new jobs
      */
     Map<Resource<?>, Object> defaultLocks = new TreeMap<>(Resource.ID_COMPARATOR);
 
     /**
      * List of submitted jobs (so that we don't submit them twice with the same script
      * by default)
      */
     Map<ResourceLocator, Resource> submittedJobs = new HashMap<>();
 
     /**
      * Simulate flags: jobs will not be submitted (but commands will be evaluated)
      */
     boolean simulate;
 
     /**
      * The resource cleaner
      */
     private final Cleaner cleaner;
 
     static final JSUtils.FunctionDefinition[] definitions = {
             new JSUtils.FunctionDefinition(XPMObject.class, "qname", Object.class, String.class),
             new JSUtils.FunctionDefinition(XPMObject.class, "include", null),
             new JSUtils.FunctionDefinition(XPMObject.class, "include_repository", null),
             new JSUtils.FunctionDefinition(XPMObject.class, "script_file", null),
             new JSUtils.FunctionDefinition(XPMObject.class, "xpath", String.class, Object.class),
             new JSUtils.FunctionDefinition(XPMObject.class, "path", null),
             new JSUtils.FunctionDefinition(XPMObject.class, "value", null),
             new JSUtils.FunctionDefinition(XPMObject.class, "file", null),
             new JSUtils.FunctionDefinition(XPMObject.class, "format", null),
             new JSUtils.FunctionDefinition(XPMObject.class, "unwrap", Object.class),
     };
 
 
     /**
      * Initialise a new XPM object
      *
      * @param currentResourceLocator The xpath to the current script
      * @param context                The JS context
      * @param environment            The environment variables
      * @param scope                  The JS scope for execution
      * @param repository             The task repository
      * @param scheduler              The job scheduler
      * @param loggerRepository       The logger for the script
      * @throws IllegalAccessException
      * @throws InstantiationException
      * @throws InvocationTargetException
      * @throws SecurityException
      * @throws NoSuchMethodException
      */
     public XPMObject(ResourceLocator currentResourceLocator,
                      Context context,
                      Map<String, String> environment,
                      Scriptable scope,
                      Repository repository,
                      Scheduler scheduler,
                      Hierarchy loggerRepository,
                      Cleaner cleaner)
             throws IllegalAccessException, InstantiationException,
             InvocationTargetException, SecurityException, NoSuchMethodException {
         LOGGER.debug("Current script is %s", currentResourceLocator);
         this.currentResourceLocator = currentResourceLocator;
         this.context = context;
         this.environment = environment;
         this.scope = scope;
         this.repository = repository;
         this.scheduler = scheduler;
         this.loggerRepository = loggerRepository;
         this.cleaner = cleaner;
         this.rootLogger = Logger.getLogger(loggerRepository);
 
 
         context.setWrapFactory(JSBaseObject.XPMWrapFactory.INSTANCE);
 
         // --- Tells E4X to preserve whitespaces
         // XML.ignoreWhitespace=false
 
         final Scriptable jsXML = (Scriptable) scope.get("XML", scope);
         scope.put("ignoreWhitespace", jsXML, false);
 
         // --- Define functions and classes
 
         // Define the new classes (scans the package for implementations of ScriptableObject)
         ArrayList<Class<?>> list = new ArrayList<>();
 
         try {
             final String packageName = getClass().getPackage().getName();
             final String resourceName = packageName.replace('.', '/');
             final Enumeration<URL> urls = XPMObject.class.getClassLoader().getResources(resourceName);
             while (urls.hasMoreElements()) {
                 URL url = urls.nextElement();
                 Introspection.addClasses(new Introspection.Checker() {
                     @Override
                     public boolean accepts(Class<?> aClass) {
                         return (ScriptableObject.class.isAssignableFrom(aClass) || JSConstructable.class.isAssignableFrom(aClass) || JSBaseObject.class.isAssignableFrom(aClass))
                                 && ((aClass.getModifiers() & Modifier.ABSTRACT) == 0);
                     }
                 }, list, packageName, -1, url);
             }
         } catch (IOException e) {
             LOGGER.error(e, "While trying to grab resources");
         }
 
         for (Class<?> aClass : list) {
             JSBaseObject.defineClass(scope, (Class<? extends Scriptable>) aClass);
         }
 
         // Add functions
         for (JSUtils.FunctionDefinition definition : definitions)
             JSUtils.addFunction(scope, definition);
 
         // Add functions from our Function object
 
         Map<String, MethodFunction> functionsMap = JSBaseObject.analyzeClass(XPMFunctions.class);
         for (MethodFunction methodFunction : functionsMap.values()) {
             ScriptableObject.putProperty(scope, methodFunction.name, methodFunction);
         }
 
 
         // --- Add new objects
 
         // namespace
         addNewObject(context, scope, "xp", "Namespace", new Object[]{"xp",
                 Manager.EXPERIMAESTRO_NS});
         addNewObject(context, scope, "xs", "Namespace", new Object[]{"xs",
                 Manager.XMLSCHEMA_NS});
 
         // xpm object
         addNewObject(context, scope, "xpm", "XPM", new Object[]{});
         ((JSXPM) get(scope, "xpm")).set(this);
 
         // xml object (to construct XML easily)
         addNewObject(context, scope, "xml", "XMLConstructor", new Object[]{});
 
         // tasks object
         addNewObject(context, scope, "tasks", "Tasks", new Object[]{this});
 
         // logger
         addNewObject(context, scope, "logger", JSBaseObject.getClassName(JSLogger.class), new Object[]{this, "xpm"});
 
         // --- Get the default group from the environment
         if (environment.containsKey(DEFAULT_GROUP))
             defaultGroup = environment.get(DEFAULT_GROUP);
 
     }
 
     static XPMObject getXPMObject(Scriptable scope) {
         while (scope.getParentScope() != null)
             scope = scope.getParentScope();
         return ((JSXPM) scope.get("xpm", scope)).xpm;
     }
 
 
     /**
      * Clone properties from this XPM instance
      */
     private XPMObject clone(ResourceLocator scriptpath, Scriptable scriptScope, TreeMap<String, String> newEnvironment) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
         final XPMObject clone = new XPMObject(scriptpath, context, newEnvironment, scriptScope, repository, scheduler, loggerRepository, cleaner);
         clone.defaultGroup = this.defaultGroup;
         clone.defaultLocks.putAll(this.defaultLocks);
         clone.submittedJobs = new HashMap<>(this.submittedJobs);
         clone.simulate = simulate;
         return clone;
     }
 
 
     static private void addNewObject(Context cx, Scriptable scope,
                                      final String objectName, final String className,
                                      final Object[] params) {
         ScriptableObject.defineProperty(scope, objectName,
                 cx.newObject(scope, className, params), 0);
     }
 
 
     public Logger getRootLogger() {
         return rootLogger;
     }
 
     /**
      * Includes a repository
      *
      * @param _connector
      * @param path
      * @param repositoryMode True if we include a repository
      * @return
      */
     public XPMObject include(Object _connector, String path, boolean repositoryMode)
             throws Exception {
         // Get the connector
         if (_connector instanceof Wrapper)
             _connector = ((Wrapper) _connector).unwrap();
 
         Connector connector;
         if (_connector instanceof JSConnector)
             connector = ((JSConnector) _connector).getConnector();
         else
             connector = (Connector) _connector;
 
         return include(new ResourceLocator(connector, path), repositoryMode);
     }
 
     /**
      * Includes a repository
      *
      * @param path The xpath, absolute or relative to the current evaluated script
      * @throws IllegalAccessException
      * @throws InstantiationException
      */
     public XPMObject include(String path, boolean repositoryMode) throws Exception {
         ResourceLocator scriptpath = currentResourceLocator.resolvePath(path, true);
         LOGGER.debug("Including repository file [%s]", scriptpath);
         return include(scriptpath, repositoryMode);
     }
 
     /**
      * Final method called for inclusion of a script
      *
      * @param scriptpath
      * @param repositoryMode If true, runs in a separate environement
      * @throws Exception
      */
     private XPMObject include(ResourceLocator scriptpath, boolean repositoryMode) throws Exception {
 
         try (InputStream inputStream = scriptpath.getFile().getContent().getInputStream()) {
             Scriptable scriptScope = scope;
             XPMObject xpmObject = this;
             if (repositoryMode) {
                 // Run the script in a new environment
                 scriptScope = context.initStandardObjects();
                 final TreeMap<String, String> newEnvironment = new TreeMap<>(environment);
                 xpmObject = clone(scriptpath, scriptScope, newEnvironment);
 
             }
 
             Context.getCurrentContext().evaluateReader(scriptScope, new InputStreamReader(inputStream), scriptpath.toString(), 1, null);
 
             return xpmObject;
         }
 
     }
 
 
     static XPMObject include(Context cx, Scriptable thisObj, Object[] args,
                              Function funObj, boolean repositoryMode) throws Exception {
         XPMObject xpm = getXPM(thisObj);
 
         if (args.length == 1)
             // Use the current connector
             return xpm.include(Context.toString(args[0]), repositoryMode);
         else if (args.length == 2)
             // Use the supplied connector
             return xpm.include(args[0], Context.toString(args[1]), repositoryMode);
         else
             throw new IllegalArgumentException("includeRepository expects one or two arguments");
 
     }
 
     /**
      * Retrievs the XPMObject from the JavaScript context
      */
     private static XPMObject getXPM(Scriptable thisObj) {
         return ((JSXPM) thisObj.get("xpm", thisObj)).xpm;
     }
 
 
     /**
      * Javascript method calling {@linkplain #include(String, boolean)}
      */
     static public Map<String, Object> js_include_repository(Context cx, Scriptable thisObj, Object[] args,
                                                             Function funObj) throws Exception {
 
         final XPMObject xpmObject = include(cx, thisObj, args, funObj, true);
         return xpmObject.properties;
     }
 
     /**
      * Javascript method calling {@linkplain #include(String, boolean)}
      */
     static public void js_include(Context cx, Scriptable thisObj, Object[] args,
                                   Function funObj) throws Exception {
 
         include(cx, thisObj, args, funObj, false);
     }
 
     /**
      * Creates a new JavaScript object
      */
     Scriptable newObject(Class<?> aClass, Object... arguments) {
         return context.newObject(scope, JSBaseObject.getClassName(aClass), arguments);
     }
 
     /**
      * Returns a JSFileObject that corresponds to the path. This can
      * be used when building command lines containing path to resources
      * or executables
      *
      * @return A {@JSFileObject}
      */
     @JSHelp("Returns a FileObject corresponding to the path")
     static public Object js_path(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws FileSystemException {
         if (args.length != 1)
             throw new IllegalArgumentException("xpath() needs one argument");
 
         XPMObject xpm = getXPM(thisObj);
 
         if (args[0] instanceof JSFileObject)
             return args[0];
 
         final Object o = unwrap(args[0]);
 
         if (o instanceof JSFileObject)
             return o;
 
         if (o instanceof FileObject)
             return xpm.newObject(JSFileObject.class, xpm, o);
 
         if (o instanceof String)
             return xpm.newObject(JSFileObject.class, xpm, xpm.currentResourceLocator.resolvePath(o.toString(), true).getFile());
 
         throw new ExperimaestroRuntimeException("Cannot convert type [%s] to a file xpath", o.getClass().toString());
     }
 
     static public String js_format(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
         if (args.length == 0)
             return "";
 
         Object fargs[] = new Object[args.length - 1];
         for (int i = 1; i < args.length; i++)
             fargs[i - 1] = unwrap(args[i]);
         String format = JSUtils.toString(args[0]);
         return String.format(format, fargs);
     }
 
 
     /**
      * Returns an XML element that corresponds to the wrapped value
      *
      * @return An XML element
      */
     static public Object js_value(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
         if (args.length != 1)
             if (args.length != 1)
                 throw new IllegalArgumentException("value() needs one argument");
         final Object object = unwrap(args[0]);
 
         Document doc = XMLUtils.newDocument();
         XPMObject xpm = getXPM(thisObj);
         return JSUtils.domToE4X(doc.createElement(JSUtils.toString(object)), xpm.context, xpm.scope);
 
     }
 
 
     /**
      * Returns the current script location
      */
     static public JSFileObject js_script_file(Context cx, Scriptable thisObj, Object[] args, Function funObj)
             throws FileSystemException {
         if (args.length != 0)
             throw new IllegalArgumentException("script_file() has no argument");
 
         XPMObject xpm = getXPM(thisObj);
 
         return new JSFileObject(xpm, xpm.currentResourceLocator.getFile());
     }
 
     @JSHelp(value = "Returns a file relative to the current connector")
     public static Scriptable js_file(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws FileSystemException {
         XPMObject xpm = getXPM(thisObj);
         if (args.length != 1)
             throw new IllegalArgumentException("file() takes only one argument");
         final String arg = JSUtils.toString(args[0]);
         return xpm.context.newObject(xpm.scope, JSFileObject.JSCLASSNAME,
                 new Object[]{xpm, xpm.currentResourceLocator.resolvePath(arg).getFile()});
     }
 
 
     @JSHelp(value = "Unwrap an annotated XML value into a native JS object")
     public static Object js_unwrap(Object object) {
         return object.toString();
     }
 
 
     /**
      * Returns a QName object
      *
      * @param ns        The namespace: can be the URI string, or a javascript
      *                  Namespace object
      * @param localName the localname
      * @return a QName object
      */
     static public Object js_qname(Object ns, String localName) {
         // First unwrapToObject the object
         if (ns instanceof Wrapper)
             ns = ((Wrapper) ns).unwrap();
 
         // If ns is a javascript Namespace object
         if (ns instanceof ScriptableObject) {
             ScriptableObject scriptableObject = (ScriptableObject) ns;
             if (scriptableObject.getClassName().equals("Namespace")) {
                 Object object = scriptableObject.get("uri", null);
                 return new QName(object.toString(), localName);
             }
         }
 
         // If ns is a string
         if (ns instanceof String)
             return new QName((String) ns, localName);
 
         throw new ExperimaestroRuntimeException("Not implemented (%s)", ns.getClass());
     }
 
 
     public static Object get(Scriptable scope, final String name) {
         Object object = scope.get(name, scope);
         if (object == null && object == Undefined.instance)
             object = null;
         else if (object instanceof Wrapper)
             object = ((Wrapper) object).unwrap();
         return object;
     }
 
     /**
      * Get the information about a given task
      *
      * @param namespace The namespace
      * @param id        The ID within the namespace
      * @return
      */
     public Scriptable getTaskFactory(String namespace, String id) {
         TaskFactory factory = repository.getFactory(new QName(namespace, id));
         LOGGER.debug("Creating a new JS task factory %s", factory.getId());
         return context.newObject(scope, "XPMTaskFactory",
                 new Object[]{Context.javaToJS(factory, scope)});
     }
 
     /**
      * Get the information about a given task
      *
      * @param localPart
      * @return
      */
     public Scriptable getTask(String namespace, String localPart) {
         return getTask(new QName(namespace, localPart));
     }
 
     public Scriptable getTask(QName qname) {
         TaskFactory factory = repository.getFactory(qname);
         if (factory == null)
             throw new ExperimaestroRuntimeException("Could not find a task with name [%s]", qname);
         LOGGER.info("Creating a new JS task [%s]", factory.getId());
         return new JSTaskWrapper(factory.create());
     }
 
     /**
      * Runs an XPath
      *
      * @param path
      * @param xml
      * @return
      * @throws javax.xml.xpath.XPathExpressionException
      *
      */
     static public Object js_xpath(String path, Object xml)
             throws XPathExpressionException {
         Node dom = (Node) JSUtils.toDOM(null, xml);
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(new NSContext(dom));
         XPathFunctionResolver old = xpath.getXPathFunctionResolver();
         xpath.setXPathFunctionResolver(new XPMXPathFunctionResolver(old));
 
         XPathExpression expression = xpath.compile(path);
         String list = (String) expression.evaluate(
                 dom instanceof Document ? ((Document) dom).getDocumentElement()
                         : dom, XPathConstants.STRING);
         return list;
     }
 
     /**
      * Recursive flattening of an array
      *
      * @param array The array to flatten
      * @param list  A list of strings that will be filled
      */
     static public void flattenArray(NativeArray array, List<String> list) {
         int length = (int) array.getLength();
 
         for (int i = 0; i < length; i++) {
             Object el = array.get(i, array);
             if (el instanceof NativeArray) {
                 flattenArray((NativeArray) el, list);
             } else
                 list.add(toString(el));
         }
 
     }
 
     static String toString(Object object) {
         if (object instanceof NativeJavaObject)
             return ((NativeJavaObject) object).unwrap().toString();
         return object.toString();
     }
 
 
     /**
      * Simple evaluation of shell commands (does not create a job)
      *
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     public NativeArray evaluate(Object jsargs, NativeObject options) throws Exception {
         Map<String, byte[]> pFiles = new TreeMap<>();
         CommandArguments arguments = getCommandArguments(jsargs, pFiles);
 
         // Run the process and captures the output
         final SingleHostConnector connector = currentResourceLocator.getConnector().getConnector(null);
         XPMProcessBuilder builder = connector.processBuilder();
 
 
         TreeMap<String, FileObject> files = new TreeMap<>();
 
         try {
             for (Map.Entry<String, byte[]> key2Content : pFiles.entrySet()) {
                 FileObject file = connector.getTemporaryFile(key2Content.getKey() + "_", ".input");
                 files.put(key2Content.getKey(), file);
                 OutputStream out = file.getContent().getOutputStream();
                 out.write(key2Content.getValue());
                 out.close();
                 LOGGER.info("Created temporary file %s", file);
             }
 
             builder.command(arguments.toStrings(connector, files));
             if (options != null && options.has("stdout", options)) {
                 FileObject stdout = getFileObject(connector, unwrap(options.get("stdout", options)));
                 builder.redirectOutput(XPMProcessBuilder.Redirect.to(stdout));
             } else {
                 builder.redirectOutput(XPMProcessBuilder.Redirect.PIPE);
             }
 
 
             builder.detach(false);
             XPMProcess p = builder.start();
             BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
             int len = 0;
             char[] buffer = new char[8192];
             StringBuffer sb = new StringBuffer();
             while ((len = input.read(buffer, 0, buffer.length)) >= 0)
                 sb.append(buffer, 0, len);
             input.close();
 
             int error = p.waitFor();
             return new NativeArray(new Object[]{error, sb.toString()});
         } finally {
             // Remove temporary files
             for (FileObject file : files.values()) {
                 try {
                     file.delete();
                 } catch (Throwable t) {
                     LOGGER.warn("Could not delete temporary file %s [%s]", file, t);
                 }
             }
 
         }
     }
 
 
     /**
      * Log a message to be returned to the client
      */
     public void log(String format, Object... objects) {
         rootLogger.info(format, objects);
         LOGGER.debug(format, objects);
     }
 
 
     /**
      * Get a QName
      */
     public QName qName(String namespaceURI, String localPart) {
         return new QName(namespaceURI, localPart);
     }
 
     /**
      * Get experimaestro namespace
      */
     public String ns() {
         return Manager.EXPERIMAESTRO_NS;
     }
 
     // XML Utilities
 
     public Object domToE4X(Node node) {
         return JSUtils.domToE4X(node, context, scope);
     }
 
     public String xmlToString(Node node) {
         return XMLUtils.toString(node);
     }
 
 
     /**
      * Creates a new command line job
      *
      * @param path    The identifier for this job
      * @param jsargs  The command line
      * @param options The options
      * @return
      * @throws Exception
      */
     public JSResource commandlineJob(Object path, NativeArray jsargs, NativeObject options) throws Exception {
         CommandLineTask task = null;
         // --- XPMProcess arguments: convert the javascript array into a Java array
         // of String
         LOGGER.debug("Adding command line job");
 
         Map<String, byte[]> pFiles = new TreeMap<>();
         final CommandArguments command = getCommandArguments(jsargs, pFiles);
 
         // --- Create the task
 
 
         final Connector connector;
 
         if (options != null && options.has("connector", options)) {
             connector = ((JSConnector) options.get("connector", options)).getConnector();
         } else {
             connector = currentResourceLocator.getConnector();
         }
 
         // Store connector in database
         scheduler.put(connector);
 
         // Resolve the xpath for the given connector
         if (path instanceof FileObject) {
             path = connector.getMainConnector().resolve((FileObject) path);
         } else
             path = connector.getMainConnector().resolve(path.toString());
 
         final ResourceLocator locator = new ResourceLocator(connector.getMainConnector(), path.toString());
         task = new CommandLineTask(scheduler, locator, command);
 
         if (submittedJobs.containsKey(locator)) {
             getRootLogger().info("Not submitting %s [duplicate]", locator);
             if (simulate)
                 return new JSResource(submittedJobs.get(locator));
             return new JSResource(scheduler.getResource(locator));
         }
 
         for (Map.Entry<String, byte[]> entry : pFiles.entrySet())
             task.setParameterFile(entry.getKey(), entry.getValue());
 
         // -- Adds default locks
         for (Map.Entry<Resource<?>, Object> lock : defaultLocks.entrySet()) {
             Dependency dependency = lock.getKey().createDependency(lock.getValue());
             task.addDependency(dependency);
         }
 
         // --- Options
 
         if (options != null) {
             // --- XPMProcess launcher
             if (options.has("launcher", options)) {
                 final Object launcher = options.get("launcher", options);
                 if (launcher != null && !(launcher instanceof UniqueTag))
                     task.setLauncher(((JSLauncher) launcher).getLauncher());
 
             }
 
             // --- Redirect standard output
             if (options.has("stdin", options)) {
                 final Object stdin = unwrap(options.get("stdin", options));
                 if (stdin instanceof String || stdin instanceof ConsString) {
                     task.setInput(stdin.toString());
                 } else throw new ExperimaestroRuntimeException("Unsupported stdin type [%s]", stdin.getClass());
             }
 
             // --- Redirect standard output
             if (options.has("stdout", options)) {
                 FileObject fileObject = getFileObject(connector, unwrap(options.get("stdout", options)));
                 task.setOutput(fileObject);
             }
 
             // --- Redirect standard error
             if (options.has("stderr", options)) {
                 FileObject fileObject = getFileObject(connector, unwrap(options.get("stderr", options)));
                 task.setError(fileObject);
             }
 
 
             // --- Resources to lock
             if (options.has("lock", options)) {
                 NativeArray resources = (NativeArray) options.get("lock", options);
                 for (int i = (int) resources.getLength(); --i >= 0; ) {
                     NativeArray array = (NativeArray) resources.get(i, resources);
                     if (array.getLength() != 2)
                         throw new XPMRhinoException(new IllegalArgumentException("Wrong number of arguments for lock"));
 //                    final String connectorId = Context.toString(array.get(0, array));
                     final String rsrcPath = Context.toString(array.get(0, array));
 
                     ResourceLocator depLocator = ResourceLocator.parse(rsrcPath);
                     Resource resource = scheduler.getResource(depLocator);
 
                     final Object o = array.get(1, array);
                     LOGGER.debug("Adding dependency on [%s] of type [%s]", resource, o);
                     if (resource == null)
                         if (simulate) {
                             if (!submittedJobs.containsKey(depLocator))
                                 LOGGER.error("The dependency [%s] cannot be found", depLocator);
                         } else throw new ExperimaestroRuntimeException("Resource [%s] was not found", rsrcPath);
 
                     if (!simulate) {
                         final Dependency dependency = resource.createDependency(o);
                         task.addDependency(dependency);
                     }
                 }
 
             }
 
         }
 
         // Update the task status now that it is initialized
         task.setGroup(defaultGroup);
 
         final Resource old = scheduler.getResource(locator);
         if (old != null) {
             // TODO: if equal, do not try to replace the task
             if (!task.replace(old)) {
                 getRootLogger().warn(String.format("Cannot override resource [%s]", task.getIdentifier()));
                old.init(scheduler);
                 return new JSResource(old);
             } else {
                 getRootLogger().info(String.format("Overwriting resource [%s]", task.getIdentifier()));
             }
         }
 
         task.set(ResourceState.WAITING);
         if (simulate) {
             PrintWriter pw = new LoggerPrintWriter(getRootLogger(), Level.INFO);
             pw.format("[SIMULATE] Starting job: %s%n", task.toString());
             pw.format("Command: %s%n", task.getCommand().toString());
             pw.format("Locator: %s", locator.toString());
             pw.flush();
         } else {
             scheduler.store(task, false);
         }
 
         return new JSResource(task);
     }
 
     private static FileObject getFileObject(Connector connector, Object stdout) throws FileSystemException {
         if (stdout instanceof String || stdout instanceof ConsString)
             return connector.getMainConnector().resolveFile(stdout.toString());
 
         if (stdout instanceof JSFileObject)
             return connector.getMainConnector().resolveFile(stdout.toString());
 
         if (stdout instanceof FileObject)
             return (FileObject) stdout;
 
         throw new ExperimaestroRuntimeException("Unsupported stdout type [%s]", stdout.getClass());
     }
 
     /**
      * Transform an array of JS objects into a command line argument object
      *
      *
      * @param jsargs         The input array
      * @param parameterFiles (out) A map that will contain the parameter files defined in the command line
      * @return a valid {@linkplain CommandArgument} object
      */
     private static CommandArguments getCommandArguments(Object jsargs, Map<String, byte[]> parameterFiles) {
         final CommandArguments command = new CommandArguments();
 
         if (jsargs instanceof NativeArray) {
             NativeArray array = ((NativeArray) jsargs);
 
             for (Object _object : array) {
                 final CommandArgument argument = new CommandArgument();
                 Object object = unwrap(_object);
                 StringBuilder sb = new StringBuilder();
 
                 // XML argument (deprecated -- too many problems with E4X!)
                 if (JSUtils.isXML(object)) {
 
                     // Walk through
                     for (Node child : xmlAsList(JSUtils.toDOM(array, object)))
                         argumentWalkThrough(sb, argument, child);
 
                 } else {
                     argumentWalkThrough(array, sb, argument, object, parameterFiles);
                 }
 
                 if (sb.length() > 0)
                     argument.add(sb.toString());
 
                 command.add(argument);
             }
 
         } else
             throw new RuntimeException(format(
                     "Cannot handle an array of type %s", jsargs.getClass()));
         return command;
     }
 
     /**
      * Recursive parsing of the command line
      */
     private static void argumentWalkThrough(Scriptable scope, StringBuilder sb, CommandArgument argument, Object object,
                                             Map<String, byte[]> parameterFiles) {
         if (object instanceof JSFileObject)
             object = ((JSFileObject) object).getFile();
 
         if (object instanceof FileObject) {
             if (sb.length() > 0) {
                 argument.add(sb.toString());
                 sb.delete(0, sb.length());
             }
             argument.add(new CommandArgument.Path((FileObject) object));
         } else if (object instanceof NativeArray) {
             for (Object child : (NativeArray) object)
                 argumentWalkThrough(scope, sb, argument, unwrap(child), parameterFiles);
         } else if (JSUtils.isXML(object)) {
             final Object node = JSUtils.toDOM(scope, object);
             for (Node child : xmlAsList(node))
                 argumentWalkThrough(sb, argument, child);
         } else if (object instanceof JSParameterFile) {
             final JSParameterFile pFile = (JSParameterFile) object;
             argument.add(new CommandArgument.ParameterFile(pFile.key));
             parameterFiles.put(pFile.key, pFile.value);
 
         } else {
             sb.append(JSUtils.toString(object));
         }
     }
 
     /**
      * Walk through a node hierarchy to build a command argument
      *
      * @param sb
      * @param argument
      * @param node
      */
     private static void argumentWalkThrough(StringBuilder sb, CommandArgument argument, Node node) {
         switch (node.getNodeType()) {
             case Node.TEXT_NODE:
                 sb.append(node.getTextContent());
                 break;
 
             case Node.ATTRIBUTE_NODE:
                 if (Manager.XP_PATH.sameQName(node)) {
                     argument.add(new CommandArgument.Path(node.getNodeValue()));
                 } else
                     sb.append(node.getTextContent());
                 break;
 
             case Node.DOCUMENT_NODE:
                 argumentWalkThrough(sb, argument, ((Document) node).getDocumentElement());
                 break;
 
             case Node.ELEMENT_NODE:
                 Element element = (Element) node;
                 if (XMLUtils.is(Manager.XP_PATH, element)) {
                     if (sb.length() > 0) {
                         argument.add(sb.toString());
                         sb.delete(0, sb.length());
                     }
                     argument.add(new CommandArgument.Path(element.getTextContent()));
                 } else {
                     for (Node child : XMLUtils.children(node))
                         argumentWalkThrough(sb, argument, child);
                 }
 
                 break;
             default:
                 throw new ExperimaestroRuntimeException("Unhandled command XML node  " + node.toString());
         }
 
     }
 
     /**
      * Transforms an XML related object into a list
      *
      * @param object
      * @return
      */
     private static Iterable<? extends Node> xmlAsList(Object object) {
 
         if (object instanceof Node) {
             Node node = (Node) object;
             return (node.getNodeType() == Node.ELEMENT_NODE && node.getChildNodes().getLength() > 1)
                     || node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE ?
 
                     XMLUtils.children(node) : Arrays.asList(node);
         }
 
         if (object instanceof NodeList)
             return XMLUtils.iterable((NodeList) object);
 
         throw new AssertionError("Cannot handle object of type " + object.getClass());
     }
 
     public void register(Closeable closeable) {
         cleaner.register(closeable);
     }
 
     public void unregister(AutoCloseable autoCloseable) {
         cleaner.unregister(autoCloseable);
     }
 
     Repository getRepository() {
         return repository;
     }
 
     public void setLocator(ResourceLocator locator) {
         this.currentResourceLocator = locator;
     }
 
 
     // --- Javascript methods
 
     static public class JSXPM extends JSBaseObject {
         XPMObject xpm;
 
         public JSXPM() {
         }
 
 
         protected void set(XPMObject xpm) {
             this.xpm = xpm;
         }
 
         @Override
         public String getClassName() {
             return "XPM";
         }
 
         @JSFunction("set_property")
         public void setProperty(String name, Object object) {
             final Object x = unwrap(object);
             xpm.properties.put(name, object);
         }
 
         @JSFunction("set_default_group")
         public void setDefaultGroup(String name) {
             xpm.defaultGroup = name;
         }
 
         @JSFunction("set_default_lock")
         @JSHelp("Adds a new resource to lock for all jobs to be started")
         public void setDefaultLock(Object resource, Object parameters) {
             xpm.defaultLocks.put((Resource) unwrap(resource), parameters);
         }
 
         @JSFunction("token_resource")
         @JSHelp("Retrieve (or creates) a token resource with a given xpath")
         public Scriptable getTokenResource(
                 @JSArgument(name = "path", help = "The path of the resource") String path
         ) throws ExperimaestroCannotOverwrite {
             final ResourceLocator locator = new ResourceLocator(XPMConnector.getInstance(), path);
             final Resource resource = xpm.scheduler.getResource(locator);
             final TokenResource tokenResource;
             if (resource == null) {
                 tokenResource = new TokenResource(xpm.scheduler, new ResourceData(locator), 0);
                 tokenResource.init(xpm.scheduler);
                 xpm.scheduler.store(tokenResource, false);
             } else {
                 if (!(resource instanceof TokenResource))
                     throw new AssertionError(String.format("Resource %s exists and is not a token", path));
                 tokenResource = (TokenResource) resource;
             }
 
             return xpm.context.newObject(xpm.scope, "TokenResource", new Object[]{tokenResource});
         }
 
 
         static public void log(Level level, Context cx, Scriptable thisObj, Object[] args, Function funObj) {
             if (args.length < 1)
                 throw new ExperimaestroRuntimeException("There should be at least one argument for log()");
 
             String format = Context.toString(args[0]);
             Object[] objects = new Object[args.length - 1];
             for (int i = 1; i < args.length; i++)
                 objects[i - 1] = unwrap(args[i]);
 
             ((JSXPM) thisObj).xpm.log(format, objects);
         }
 
         @JSFunction()
         public void log() {
 
         }
 
         @JSFunction("logger")
         public Scriptable getLogger(String name) {
             return xpm.newObject(JSLogger.class, xpm, name);
         }
 
 
         @JSFunction("log_level")
         @JSHelp(value = "Sets the logger debug level")
         public void setLogLevel(
                 @JSArgument(name = "name") String name,
                 @JSArgument(name = "level") String level
         ) {
             Logger.getLogger(xpm.loggerRepository, name).setLevel(Level.toLevel(level));
         }
 
 
         @JSFunction("get_script_path")
         public String getScriptPath() {
             return xpm.currentResourceLocator.getPath();
         }
 
         @JSFunction("get_script_file")
         public Scriptable getScriptFile() throws FileSystemException {
             return xpm.newObject(JSFileObject.class, xpm, xpm.currentResourceLocator.getFile());
         }
 
         /**
          * Add a module
          */
         @JSFunction("add_module")
         public JSModule addModule(Object object) {
             JSModule module = new JSModule(xpm, xpm.repository, xpm.scope, (NativeObject) object);
             LOGGER.debug("Adding module [%s]", module.module.getId());
             xpm.repository.addModule(module.module);
             return module;
         }
 
         /**
          * Add an experiment
          *
          * @param object
          * @return
          */
         @JSFunction("add_task_factory")
         public Scriptable add_task_factory(NativeObject object) throws ValueMismatchException {
             JSTaskFactory factory = new JSTaskFactory(xpm.scope, object, xpm.repository);
             xpm.repository.addFactory(factory.factory);
             return xpm.context.newObject(xpm.scope, "XPMTaskFactory",
                     new Object[]{factory});
         }
 
         @JSFunction("get_task")
         public Scriptable getTask(QName name) {
             return xpm.getTask(name);
         }
 
         @JSFunction("get_task")
         public Scriptable getTask(
                 String namespaceURI,
                 String localName) {
             return xpm.getTask(namespaceURI, localName);
         }
 
 
         @JSFunction(value = "evaluate", optional = 1)
         public NativeArray evaluate(
                 NativeArray command,
                 NativeObject options
         ) throws Exception {
             return xpm.evaluate(command, options);
         }
 
         @JSFunction("file")
         @JSHelp(value = "Returns a file relative to the current connector")
         public Scriptable file(@JSArgument(name = "filepath") String filepath) throws FileSystemException {
             return xpm.context.newObject(xpm.scope, JSFileObject.JSCLASSNAME,
                     new Object[]{xpm, xpm.currentResourceLocator.resolvePath(filepath).getFile()});
         }
 
         @JSFunction
         public Scriptable file(@JSArgument(name = "file") JSFileObject file) throws FileSystemException {
             return file;
         }
 
 
         @JSFunction(value = "command_line_job", optional = 1)
         @JSHelp(value = "Schedule a command line job")
         public Scriptable commandlineJob(@JSArgument(name = "jobId") Object path,
                                          @JSArgument(type = "Array", name = "command") NativeArray jsargs,
                                          @JSArgument(type = "Map", name = "options") NativeObject jsoptions) throws Exception {
             JSResource jsResource = xpm.commandlineJob(path, jsargs, jsoptions);
             return jsResource;
         }
 
         /**
          * Declare an alternative
          *
          * @param qname A qualified name
          */
         @JSFunction("declare_alternative")
         @JSHelp(value = "Declare a qualified name as an alternative input")
         public void declareAlternative(Object qname) {
             AlternativeType type = new AlternativeType((QName) qname);
             xpm.repository.addType(type);
         }
 
 
         /**
          * Useful for debugging E4X: outputs the DOM view
          *
          * @param xml an E4X object
          */
         @JSFunction("output_e4x")
         @JSHelp("Outputs the E4X XML object")
         public void outputE4X(@JSArgument(name = "xml", help = "The XML object") Object xml) {
             final Iterable<? extends Node> list = XPMObject.xmlAsList(JSUtils.toDOM(null, xml));
             for (Node node : list) {
                 output(node);
             }
         }
 
         @JSFunction("publish")
         @JSHelp("Publish the repository on the web server")
         public void publish() throws InterruptedException {
             TasksServlet.updateRepository(xpm.currentResourceLocator.toString(), xpm.repository);
         }
 
         @JSFunction
         @JSHelp("Set the simulate flag: When true, the jobs are not submitted but just output")
         public boolean simulate(boolean simulate) {
             boolean old = xpm.simulate;
             xpm.simulate = simulate;
             return simulate;
         }
 
         @JSFunction
         public boolean simulate() {
             return xpm.simulate;
         }
 
         private void output(Node node) {
             switch (node.getNodeType()) {
                 case Node.ELEMENT_NODE:
                     xpm.log("[element %s]", node.getNodeName());
                     for (Node child : XMLUtils.children(node))
                         output(child);
                     xpm.log("[/element %s]", node.getNodeName());
                     break;
                 case Node.TEXT_NODE:
                     xpm.log("text [%s]", node.getTextContent());
                     break;
                 default:
                     xpm.log("%s", node.toString());
             }
         }
     }
 
 
     static class XPMFunctions {
         @JSFunction("merge")
         static public NativeObject merge(NativeObject... objects) {
             NativeObject returned = new NativeObject();
             for (NativeObject object : objects) {
                 for (Map.Entry<Object, Object> entry : object.entrySet()) {
                     Object key = entry.getKey();
                     if (returned.has(key.toString(), returned))
                         throw new XPMRhinoException("Conflicting id in merge: %s", key);
                     returned.put(key.toString(), returned, entry.getValue());
                 }
 
             }
             return returned;
         }
 
         @JSFunction(scope = true)
         @JSHelp(value = "Transform plans outputs with a function")
         public static Scriptable transform(Context cx, Scriptable scope, Callable f, JSAbstractOperator... operators) throws FileSystemException {
             return new JSTransform(cx, scope, f, operators);
         }
 
 
         @JSFunction("assert")
         public static void _assert(boolean condition) {
             if (!condition)
                 throw new EvaluatorException("assertion failed");
         }
         @JSFunction("assert")
         public static void _assert(boolean condition, String format, Object... objects) {
             if (!condition)
                 throw new EvaluatorException("assertion failed: " + String.format(format, objects));
         }
 
     }
 }
