 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework;
 
 import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;
 import cx.ath.mancel01.dependencyshot.DependencyShot;
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
 import cx.ath.mancel01.webframework.cache.CacheService;
 import cx.ath.mancel01.webframework.compiler.CompilationException;
 import cx.ath.mancel01.webframework.compiler.HotSwapAgent;
 import cx.ath.mancel01.webframework.compiler.RequestCompiler;
 import cx.ath.mancel01.webframework.compiler.WebFrameworkClassLoader;
 import cx.ath.mancel01.webframework.data.JPAService;
 import cx.ath.mancel01.webframework.http.Request;
 import cx.ath.mancel01.webframework.http.Response;
 import cx.ath.mancel01.webframework.http.Session;
 import cx.ath.mancel01.webframework.integration.dependencyshot.DependencyShotIntegrator;
 import cx.ath.mancel01.webframework.routing.Router;
 import cx.ath.mancel01.webframework.routing.WebMethod;
 import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
 import cx.ath.mancel01.webframework.view.FrameworkPage;
 import cx.ath.mancel01.webframework.view.Render;
 import cx.ath.mancel01.webframework.view.Renderable;
 import cx.ath.mancel01.webframework.view.View;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.lang.instrument.ClassDefinition;
 import java.util.ArrayList;
 import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
 
 /**
  *
  * @author mathieuancelin
  */
 public class FrameworkHandler {
 
     private InjectorImpl injector;
     private WebBinder configBinder;
     private final File rootDir;
     private Class rootController;
     private boolean started = false;
     private final String contextRoot;
     private File publicResources;
     private Class<? extends Binder> binderClass;
     private String binderClassName;
     private Router router;
     private FileGrabber viewGrabber;
     private WebFrameworkClassLoader loader;
    private AtomicLong requestCount = new AtomicLong(0);
 
     public FrameworkHandler(String binderClassName, String contextRoot,
             File rootDir, FileGrabber viewGrabber) {
         if ("".equals(contextRoot)) {
             throw new RuntimeException("Can't have an empty context root");
         }
         this.binderClassName = binderClassName;
         this.rootDir = rootDir;
         this.contextRoot = contextRoot;
         this.router = new Router();
         this.viewGrabber = viewGrabber;
     }
 
     private void configureInjector(InjectorImpl inj) {
         inj.allowCircularDependencies(true);
         inj.registerShutdownHook();
         new DependencyShotIntegrator(inj).registerBindings();
     }
 
     public void validate() {
         if (rootController == null) {
             throw new RuntimeException("You need to register a root controler");
         }
     }
 
     public void start() {
         loader = new WebFrameworkClassLoader(getClass().getClassLoader());
         Thread.currentThread().setContextClassLoader(loader);
         WebFramework.init(rootDir, contextRoot, loader);
         publicResources = WebFramework.PUBLIC_RESOURCES;
         try {
             this.binderClass =
                 (Class<? extends Binder>) loader.loadClass(binderClassName);
             this.router.reset();
             this.configBinder = (WebBinder) this.binderClass.newInstance();
             this.configBinder.setDispatcher(this);
             this.injector = DependencyShot.getInjector(configBinder);
             configureInjector(this.injector);
         } catch (Exception e) {
             throw new RuntimeException("Error at injector creation", e);
         }
         CacheService.start();
         JPAService.start(loader);
         this.started = true;
     }
 
     public void stop() {
         CacheService.stop();
         JPAService.stop();
         this.started = false;
         this.injector.triggerLifecycleDestroyCallbacks();
     }
 
     public void registrerController(Class<?> clazz) {
         router.registrerController(clazz);
     }
 
     public Response process(Request request) {
         try {
             if (started) {
                 WebFramework.logger.trace("asked resource => {}", request.path);
                 long start = System.currentTimeMillis();
                 Response res = Response.current.get();
                 request.contextRoot = contextRoot;
                 String path = request.path;
                 if (!"/".equals(contextRoot)) {
                     path = path.replace(contextRoot, "");
                     if (!path.startsWith("/")) {
                         path = "/" + path;
                     }
                 }
                 if (path.endsWith("favicon.ico")) {
                     path = "/public/img/favicon.ico";
                 }
                 if (path.startsWith("/public/")) {
                     File asked = new File(publicResources, path.replace("/public/", ""));
                     if (asked.exists()) {
                         res.direct = asked;
                     } else {
                         WebFramework.logger.warn("file not found : {}", path);
                         return Render.notFound().render();
                     }
                     res.out = new ByteArrayOutputStream();
                     return res;
                 }
                 Thread.currentThread().setContextClassLoader(loader);
                 if (WebFramework.dev) {
                     List<String> classesNames = new ArrayList<String>();
                     List<String> classes = new ArrayList<String>();
                     List<ClassDefinition> newDefinitions = new ArrayList<ClassDefinition>();
                     WebFramework.findClasses(classesNames, WebFramework.JAVA_SOURCES);
                     boolean changed = false;
                     for(String className : classesNames) {
                         String name = className.replace(WebFramework.JAVA_SOURCES.getAbsolutePath() + "/", "").replace(".java", "");
                         classes.add(name.replace("/", "."));
                         boolean tmp = RequestCompiler.compile(name);
                         if (tmp) {
                             changed = true;
                             File clazz = new File(WebFramework.FWK_COMPILED_CLASSES_PATH, name + ".class");
                             newDefinitions.add(new ClassDefinition(loader.loadClass(name.replace("/", ".")),
                                     WebFrameworkClassLoader.getClassDefinition(clazz)));
                         }
                     }
                     WebFrameworkClassLoader.setClassesNames(classes);
                     if (changed) {
                        long count = requestCount.incrementAndGet();
                        if (count > 2) {
                            if (HotSwapAgent.enabled) {
                                try {
                                    HotSwapAgent.reload(newDefinitions.toArray(new ClassDefinition[newDefinitions.size()]));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    stop();
                                    // clean classloader ?
                                    start();
                                }
                            } else {
                                 stop();
                                 // clean classloader ?
                                 start();
                             }
                         } else {
                             stop();
                             // clean classloader ?
                             start();
                         }
                     }
                     WebFramework.logger.trace("configuration bootstrap : {} ms.", (System.currentTimeMillis() - start));
                     start = System.currentTimeMillis();
                 }
                 Session.current.set(Session.restore());
                 WebMethod webMethod = null;
                 try {
                     webMethod = router.route(request, contextRoot);
                 } catch (Throwable t) {
                     return new FrameworkPage("No route found for " + path,
                             "<b>registered routes are</b> :<br/><br/>"
                             + router.getHtmlRoutesTable()).render();
                 }
                 WebFramework.logger.trace("routing : {} ms.", (System.currentTimeMillis() - start));
                 start = System.currentTimeMillis();
                 return render(request, webMethod);
             } else {
                 throw new RuntimeException("Framework not started ...");
             }
         } catch (Throwable t) {
             return new FrameworkPage("Error", "Ooops, an error occured : <br/><br/>"
                    + getErrorMessage(t)).render();
         } finally {
             Session.current.remove();
         }
     }
 
     Response render(Request request, WebMethod webMethod) throws Exception {
         try {
             Class controllerClass = webMethod.getClazz();
             String methodName = webMethod.getMethod().getName();
             long start = System.currentTimeMillis();
             JPAService.getInstance().startTx();
             start = System.currentTimeMillis();
             Object controller = injector.getInstance(controllerClass);
             WebFramework.logger.trace("controller injection : {} ms.", (System.currentTimeMillis() - start));
             start = System.currentTimeMillis();
             Object ret = webMethod.invoke(request, controller);
             WebFramework.logger.trace("controller method invocation : {} ms.", (System.currentTimeMillis() - start));
             JPAService.getInstance().stopTx(false);
             AsyncJob.current.remove();
             if (ret == null) {
                 return new FrameworkPage("Nothing returned", "<h1>Ooops</h1> it seems that your controller method doesn't return"
                         + " anything.<br/><br/>If you use the Render api, don't forget to call the go() method.").render();
             }
             Session.current.get().save();
             if (ret instanceof Renderable) {
                 Renderable renderable = (Renderable) ret;
                 if (renderable instanceof View) { // ok that's not really OO but what the hell !
                     return ((View) renderable).render(methodName, controllerClass, viewGrabber);
                 }
                 return renderable.render();
             } else {
                 return new FrameworkPage("Oooops, can't render", "can't render an object of type : <br/><br/>"
                         + ret.getClass().getName()).render();
             }
         } catch (SuspendException s) {
             s.waitFor();
             return process(s.getReq());
         }
     }
 
     private Response createErrorResponse(Throwable t) {
         Throwable original = t;
         Throwable cause = null;
         while (cause == null) {
             t = t.getCause();
             if (t == null) {
                 break;
             }
             if (t instanceof CompilationException) {
                 cause = t;
             }
         }
         if (cause == null) { // TODO : error page with stacktrace
             return new FrameworkPage("Error", getErrorMessage(original)).render();
         }
         return new FrameworkPage("Compilation error", cause.getMessage().replace("\n", "<br/>")).render();
     }
 
     private String getErrorMessage(Throwable t) {
         t.printStackTrace();
         StringBuilder message = new StringBuilder();
         Throwable tmp = t;
         while (tmp != null) {
             message.append("<b>");
             message.append(tmp.getMessage());
             message.append("</b><br/><br/>");
             for (StackTraceElement elmt : tmp.getStackTrace()) {
                 if (!elmt.toString().contains("com.sun.grizzly")) {
                     message.append(elmt.toString().replace("$", ""));
                     message.append("<br/>");
                 }
             }
             message.append("<br/>");
             tmp = tmp.getCause();
             if (tmp != null)
                 message.append("<b>caused by : </b>");
         }
         return message.toString().replace("\n", "<br/>");
     }
 }
