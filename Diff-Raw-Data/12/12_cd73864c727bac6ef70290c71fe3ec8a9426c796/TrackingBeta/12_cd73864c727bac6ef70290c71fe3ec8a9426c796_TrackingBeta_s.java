 package controllers;
 
 import com.abperf.UserDevice;
 import com.alienmegacorp.bundles.ClosureBundle;
 import com.google.javascript.jscomp.CompilationLevel;
 import java.util.Map;
 import java.util.UUID;
 import models.Domain;
 import models.Domain.DomainAccess;
 import models.PageView;
 import models.Project;
 import models.TestCSS;
 import org.apache.commons.lang.StringUtils;
 import play.Play;
 import play.mvc.Http.StatusCode;
 import play.mvc.Util;
 
 public class TrackingBeta extends BaseController {
     /**
      * Response code may be 200, 402 or 429.
      *
      * @param proj Project ID.
      * @param time Time the page loaded at.
      * @param url
      * @param tests The map key is the test group name and the value is the test ID.
      * @param user
      */
     public static void start(final Long proj, final long time, final String url,
             final Map<String, String> tests, String user) {
         allowCrossDomain();
         onlyPOSTallowed();
 
         if (tests.isEmpty()) {
             response.print("There are no AB tests on this page.");
             response.status = StatusCode.FORBIDDEN;
             return;
         }
 
         final Project project = Project.findById(proj);
         if (project.hasReachedPageViewQuota()) {
             response.print("Page view limit has been reached.");
             response.status = StatusCode.FORBIDDEN;
             return;
         }
 
         if (project.waitingForPayment()) {
             response.print("Waiting for payment.");
             response.status = StatusCode.FORBIDDEN;
             return;
         }
 
         final Domain domain = Domain.findByProjectAndURL(project, url);
         if (domain == null) {
             response.print("This domain is not assigned to " + project.name);
             response.status = StatusCode.FORBIDDEN;
             return;
         }
 
         if (domain.access.equals(DomainAccess.PRIVATE)) {
             response.print("This domain is private. It does not count towards your page view quota.");
             response.status = StatusCode.FORBIDDEN;
             return;
         }
 
         boolean newUser = false;
         if (user == null) {
             user = UUID.randomUUID().toString();
             newUser = true;
         }
 
         final PageView pageView = new PageView(domain.project, time, url, tests, user);
         pageView.create();
 
         project.pageViews++;
         project.save();
 
         response.setHeader("Content-Type", "text/plain");
         response.print("pv=" + pageView.id);
         if (newUser) {
             response.print("\nuser=" + user);
         }
         if (!pageView.testIDsWithUnknownCSS.isEmpty()) {
             response.print("\ncss=" + StringUtils.join(pageView.testIDsWithUnknownCSS, ','));
         }
         ok();
     }
 
     /**
      * @param pv {@link PageView#id}
      * @param time Time the page loaded at.
      * @param status "active" or "inactive".
      * @param css The keys are the test IDs, the values are the CSS.
      */
     public static void ping(final long pv, final long time, final String status,
             final Map<String, String> css) {
         allowCrossDomain();
         onlyPOSTallowed();
 
         final PageView pageView = PageView.findById(pv);
         pageView.pingsJSON.addProperty(String.valueOf(time), status);
         // We must call "setJSONstringsFromJSONobjects" manually so that at least one field changes.
         // If no field changes *before* JPA hooks, then no hooks are called and there is no need to
         // save the object.
         pageView.setJSONstringsFromJSONobjects();
         pageView.save();
 
         if (css != null && !css.isEmpty()) {
             for (final String id: css.keySet()) {
                 TestCSS.setCSS(id, css.get(id));
             }
         }
 
         ok();
     }
 
     /**
      * @param id The project ID. Only used client-side.
      */
     public static void clientScripts(final int id) {
         // Check the browser is supported: Chrome 5+, Firefox 4+, Safari 4+.
         UserDevice device = (UserDevice) request.args.get("device");
         if ((device.chrome && device.chromeVersion >= 5)
                 || (device.firefox && device.firefoxVersion >= 4)
                 || (device.safari && device.safariVersion >= 4)) {
             // Browser is supported.
             response.cacheFor("61d");
             Bundles.sendBundle(clientScripts);
         } else {
             renderText("// Browser not supported");
         }
     }
 
     private static final ClosureBundle clientScripts = createClientScriptsBundle();
 
     @Util
     private static ClosureBundle createClientScriptsBundle() {
         final ClosureBundle bundle = new ClosureBundle(
                 Play.getFile("public/bundles/client-beta.js"),
                 Play.getFile("public/js/client"));
         bundle.setClosureLibraryDir(Play.getFile("public/closure"));
         bundle.setCopyrightNotice("Copyright 2012 A/B Performance");
         bundle.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
         bundle.setEntryNamespaces("abperf", "abperf.debug");
         bundle.setOutputWrapper("(function(){ %output% })();");
         bundle.setDefineToBooleanLiteral("goog.DEBUG", Play.mode.isDev());
         ClosureBundle.setPythonExecutable(Play.configuration.getProperty("python"));
 
         return bundle;
     }
 
     /**
      * Only POST is allowed in production. In development, GET is allowed just to make debugging easier.
      */
     @Util
     private static void onlyPOSTallowed() {
         if (com.abperf.Constants.IS_PROD) {
             requireHttpMethod("POST");
         }
     }
 
     @Util
     private static void allowCrossDomain() {
         // This has the limitation of only allowing other hosts on the same port
         // as AB Perf (80). There seems to be no way around this. The limitation
         // is in our Terms Of Use.
         response.setHeader("Access-Control-Allow-Origin", "*");
     }
 }
