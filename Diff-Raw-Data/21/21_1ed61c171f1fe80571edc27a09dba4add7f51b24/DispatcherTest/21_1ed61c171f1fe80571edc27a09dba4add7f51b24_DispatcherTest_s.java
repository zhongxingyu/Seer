 package org.fiz;
 
 import javax.servlet.*;
 import java.util.*;
 import org.apache.log4j.*;
 
 /**
  * Junit tests for the Dispatcher class.
  */
 
 public class DispatcherTest extends junit.framework.TestCase {
 
     protected Dispatcher dispatcher;
 
     public void setUp() throws ServletException {
         dispatcher = new Dispatcher();
         Dispatcher.testMode = true;
         dispatcher.clearCaches = false;
         dispatcher.init(new ServletConfigFixture(
                 new ServletContextFixture()));
         Config.setDataset("main", new Dataset("searchPackages", "org.fiz"));
     }
 
     public void test_UnsupportedUriError() {
         Error e = new Dispatcher.UnsupportedUrlError("/a/b/c", "smelled funny");
         assertEquals("exception message",
                 "unsupported URL \"/a/b/c\": smelled funny", e.getMessage());
     }
 
     public void test_init() throws ServletException {
         ServletContextFixture context = new ServletContextFixture();
         ServletConfigFixture config = new ServletConfigFixture(context);
         dispatcher.init(config);
        assertEquals("Config path", "test/testData/WEB-INF/config",
                 Config.getSearchPath()[0]);
        assertEquals("Css path", "test/testData/WEB-INF/css",
                 Css.getSearchPath()[0]);
         assertEquals("clearCaches variable", false,
                 dispatcher.clearCaches);
     }
 
     // Node tests for clearCaches: it is already exercised elsewhere.
 
     public void test_destroy() {
         DispatcherTest1Interactor.count = 0;
         DispatcherTest1Interactor.destroyCount = 0;
         DispatcherTest2Interactor.destroyCount = 0;
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/incCount"), new ServletResponseFixture());
         dispatcher.service(new ServletRequestFixture("/dispatcherTest2/xyz"),
                 new ServletResponseFixture());
         assertEquals("first interactor not yet destroyed", 0,
                 DispatcherTest1Interactor.destroyCount);
         assertEquals("second interactor not yet destroyed", 0,
                 DispatcherTest2Interactor.destroyCount);
         dispatcher.destroy();
         assertEquals("first interactor destroyed", 1,
                 DispatcherTest1Interactor.destroyCount);
         assertEquals("second interactor destroyed", 3,
                 DispatcherTest2Interactor.destroyCount);
     }
 
     public void test_service_clearCaches() {
         dispatcher.clearCaches = true;
         Config.setDataset("test", new Dataset());
         Config.setDataset("main", new Dataset());
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/incCount"), new ServletResponseFixture());
         assertEquals("Config cache size (caches cleared)", 1,
                 Config.cache.size());
         dispatcher.clearCaches = false;
         Config.setDataset("test", new Dataset());
         Config.setDataset("main", new Dataset());
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/incCount"), new ServletResponseFixture());
         assertEquals("Config cache size (caches no longer cleared)", 2,
                 Config.cache.size());
     }
     public void test_service_parseMethodEndingInSlash() {
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/bogus/a/b/c"), new ServletResponseFixture());
         TestUtil.assertSubstring("error message",
                 "couldn't find method \"bogus\" with proper signature",
                 dispatcher.basicMessage);
     }
     public void test_service_parseMethodNotEndingInSlash() {
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/bogus"), new ServletResponseFixture());
         TestUtil.assertSubstring("error message",
                 "couldn't find method \"bogus\" with proper signature",
                 dispatcher.basicMessage);
     }
     public void test_service_notEnoughInfoInUri() {
         dispatcher.service(new ServletRequestFixture(null),
                 new ServletResponseFixture());
         TestUtil.assertSubstring("no path info",
                 "URL doesn't contain class name and/or method name",
                 dispatcher.basicMessage);
         dispatcher.basicMessage = null;
         dispatcher.service(new ServletRequestFixture("abc"),
                 new ServletResponseFixture());
         TestUtil.assertSubstring("no slashes",
                 "URL doesn't contain class name and/or method name",
                 dispatcher.basicMessage);
         dispatcher.basicMessage = null;
         dispatcher.service(new ServletRequestFixture("//a/b/c"),
                 new ServletResponseFixture());
         TestUtil.assertSubstring("empty class name",
                 "URL doesn't contain class name and/or method name",
                 dispatcher.basicMessage);
         dispatcher.basicMessage = null;
         dispatcher.service(new ServletRequestFixture("/a//b/c"),
                 new ServletResponseFixture());
         TestUtil.assertSubstring("empty method name",
                 "URL doesn't contain class name and/or method name",
                 dispatcher.basicMessage);
         dispatcher.basicMessage = null;
         dispatcher.service(new ServletRequestFixture("/a/b/c"),
                 new ServletResponseFixture());
         TestUtil.assertSubstring("simplest valid URL",
                 "couldn't find class \"AInteractor\"",
                 dispatcher.basicMessage);
     }
     public void test_service_setAjaxAndInvokeMethod() {
         DispatcherTest1Interactor.count = 0;
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/incCount"), new ServletResponseFixture());
         assertEquals("non-Ajax request", false, DispatcherTest1Interactor.isAjax);
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/ajaxIncCount"),
                 new ServletResponseFixture());
         assertEquals("Ajax request", true, DispatcherTest1Interactor.isAjax);
         assertEquals("count variable", 2, DispatcherTest1Interactor.count);
     }
     public void test_service_setPostType() {
         DispatcherTest1Interactor.count = 0;
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/postTest"), new ServletResponseFixture());
         assertEquals("request type", ClientRequest.Type.POST,
                 DispatcherTest1Interactor.requestType);
     }
     public void test_service_exceptionInMethod() {
         dispatcher.service(new ServletRequestFixture("/dispatcherTest1/error"),
                 new ServletResponseFixture());
         TestUtil.assertSubstring("error message", "error in method",
                 dispatcher.basicMessage);
     }
     public void test_service_exception_createClientRequest() {
         ServletResponseFixture response = new ServletResponseFixture();
         dispatcher.service(new ServletRequestFixture("/dispatcherTest2/ajaxBogus"),
                 response);
         assertEquals("AJAX response",
                 "Fiz.addBulletinMessage(\"bulletinError\", \"uncaughtAjax: " +
                 "unsupported URL &quot;/x/y/z&quot;: couldn't find method " +
                 "&quot;ajaxBogus&quot; with proper signature in class " +
                 "DispatcherTest2Interactor\");\n",
                 response.toString());
     }
     public void test_service_exception_HandledError() {
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/handledError"),
                 new ServletResponseFixture());
         assertEquals("error message", null, dispatcher.basicMessage);
     }
     public void test_service_exception_dontLogUserError() {
         StringAppender appender = new StringAppender();
         dispatcher.logger = Logger.getRootLogger();
         dispatcher.logger.removeAllAppenders();
         dispatcher.logger.addAppender(appender);
         dispatcher.logger.setLevel(Level.WARN);
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/userError"), new ServletResponseFixture());
         assertEquals("log message", "",
                 appender.log.toString());
     }
     public void test_service_exception_logMessage() {
         StringAppender appender = new StringAppender();
         dispatcher.logger = Logger.getRootLogger();
         dispatcher.logger.removeAllAppenders();
         dispatcher.logger.addAppender(appender);
         dispatcher.service(new ServletRequestFixture("/dispatcherTest1/error"),
                 new ServletResponseFixture());
         assertEquals("error message", "error in method",
                 dispatcher.basicMessage);
         String message = appender.log.toString().replaceAll("\\r\\n", "\n");
         TestUtil.assertSubstring("log message",
                 "unhandled exception for URL \"/x/y/z?a=b&c=d\":\n" +
                 "java.lang.Error: error in method\n" +
                 "\tat org.fiz.DispatcherTest1Interactor.error",
                 message);
     }
     public void test_service_exception_ajaxUserError() {
         ServletResponseFixture response = new ServletResponseFixture();
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest1/ajaxUserError"), response);
         assertEquals("AJAX response",
                 "Fiz.addBulletinMessage(\"bulletinError\", " +
                 "\"Ajax user error\");\n",
                 response.toString());
     }
     public void test_service_exception_ajaxInternalError() {
         ServletResponseFixture response = new ServletResponseFixture();
         dispatcher.service(new ServletRequestFixture(
                 "/dispatcherTest2/ajaxBogus"), response);
         assertEquals("AJAX response",
                 "Fiz.addBulletinMessage(\"bulletinError\", \"uncaughtAjax: " +
                 "unsupported URL &quot;/x/y/z&quot;: couldn't find method " +
                 "&quot;ajaxBogus&quot; with proper signature in class " +
                 "DispatcherTest2Interactor\");\n",
                 response.toString());
     }
     public void test_service_exception_postUserError() {
         ServletResponseFixture response = new ServletResponseFixture();
         ServletRequestFixture request = new ServletRequestFixture(
                 "/dispatcherTest1/postUserError");
         request.parameterMap = new Hashtable<String,String>();
         dispatcher.service(request, response);
         TestUtil.assertMatchingSubstring("Response Javascript",
                 "window.parent.Fiz.FormSection.handleResponse(" +
                 "\"Fiz.addBulletinMessage(\\\"" +
                 "bulletinError\\\", \\\"Post user error\\\");\\n\");",
                 response.toString(), "window.parent.Fiz[^\n]*");
     }
     public void test_service_exception_postInternalError() {
         ServletResponseFixture response = new ServletResponseFixture();
         ServletRequestFixture request = new ServletRequestFixture(
                 "/dispatcherTest2/postBogus");
         request.parameterMap = new Hashtable<String,String>();
         dispatcher.service(request, response);
         TestUtil.assertMatchingSubstring("Response Javascript",
                 "window.parent.Fiz.FormSection.handleResponse(" +
                 "\"Fiz.addBulletinMessage(\\\"" +
                 "bulletinError\\\", \\\"uncaughtPost: unsupported URL " +
                 "&quot;/x/y/z&quot;: couldn't find method &quot;postBogus" +
                 "&quot; with proper signature in class " +
                 "DispatcherTest2Interactor\\\");\\n\");",
                 response.toString(), "window.parent.Fiz[^\n]*");
     }
     public void test_service_exception_htmlResponseClearFirst() {
         Config.setDataset("styles", new Dataset("uncaught",
                 new Dataset("html", "test: @message",
                 "clearHtml", "true")));
         ServletResponseFixture response = new ServletResponseFixture();
         dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                 response);
         TestUtil.assertSubstring("HTML body", "<body>\n" +
                 "test: error in DispatcherTest2.x</body>\n",
                 response.toString());
     }
     public void test_service_exception_htmlResponseDontClear() {
         Config.setDataset("styles", new Dataset("uncaught",
                 new Dataset("html", "test: @message",
                 "clearHtml", "false")));
         ServletResponseFixture response = new ServletResponseFixture();
         dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                 response);
         TestUtil.assertSubstring("HTML body", "<body>\n" +
                 "<p>Sample text.</p>\n" +
                 "test: error in DispatcherTest2.x</body>\n",
                 response.toString());
     }
     public void test_service_exception_multipleErrors() {
         Config.setDataset("styles", new Dataset("uncaught",
                 new Dataset("html", "test: @message @bogus",
                 "clearHtml", "true")));
         ServletResponseFixture response = new ServletResponseFixture();
         dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                 response);
         TestUtil.assertSubstring("HTML body", "<body>\n" +
                 "<div class=\"uncaughtException\">Multiple internal errors in " +
                 "the server!  Details are in the server's log</div>.\n" +
                 "</body>\n",
                 response.toString());
     }
 
     public void test_findClass() {
         Class<?> cl = dispatcher.findClass("org.fiz.Interactor", null);
         TestUtil.assertSubstring("name of found class", "org.fiz.Interactor",
                 cl.getName());
         boolean gotException = false;
         try {
             dispatcher.findClass("bogus_xyz",
                     new ServletRequestFixture("/first/second"));
         }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": can't find class " +
                     "\"bogus_xyz\"", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_findMethod_basics() {
         DispatcherTest1Interactor.initCount = 0;
         Dispatcher.UrlMethod method = dispatcher.findMethod(
                 "dispatcherTest1/incCount", 15,
                 new ServletRequestFixture("/dispatcherTest1/incCount"));
         assertEquals("init invoked during first call", 1,
                 DispatcherTest1Interactor.initCount);
         assertEquals("method name from first call", "incCount",
                 method.method.getName());
         assertEquals("class of Interactor object", "DispatcherTest1Interactor",
                 method.interactor.getClass().getSimpleName());
         DispatcherTest1Interactor.initCount = 0;
         method = dispatcher.findMethod("dispatcherTest1/incCount", 15,
                 new ServletRequestFixture("/dispatcherTest1/incCount"));
         assertEquals("init not invoked during second call", 0,
                 DispatcherTest1Interactor.initCount);
         assertEquals("method name from second call", "incCount",
                 method.method.getName());
     }
     public void test_findMethod_nonexistentClass() {
         boolean gotException = false;
         try {
             dispatcher.findMethod("missingClass/a", 12,
                     new ServletRequestFixture("/missingClass/a"));
         }
         catch (ClassNotFoundError e) {
             assertEquals("exception message",
                     "couldn't find class \"MissingClassInteractor\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findMethod_updateClassMap() {
         dispatcher.findMethod("dispatcherTest1/incCount", 15,
                 new ServletRequestFixture("/dispatcherTest1/incCount/extra"));
         assertEquals("DispatcherTest1Interactor",
                 StringUtil.join(dispatcher.interactorMap.keySet(), ", "));
     }
     public void test_findMethod_invokeInit() {
         DispatcherTest1Interactor.count = 0;
         DispatcherTest1Interactor.initCount = 0;
         dispatcher.findMethod("dispatcherTest1/incCount", 15,
                 new ServletRequestFixture("/dispatcherTest1/incCount"));
         assertEquals("init invoked during first request", 1,
                 DispatcherTest1Interactor.initCount);
         dispatcher.findMethod("dispatcherTest1/incCount", 15,
                 new ServletRequestFixture("/dispatcherTest1/incCount"));
         assertEquals("init not invoked during second request", 1,
                 DispatcherTest1Interactor.initCount);
     }
     public void test_findMethod_scanMethods() {
         dispatcher.findMethod("dispatcherTest1/incCount", 15,
                 new ServletRequestFixture("/dispatcherTest1/incCount/extra"));
         ArrayList<String> names = new ArrayList<String>();
         names.addAll(dispatcher.methodMap.keySet());
         Collections.sort(names);
         assertEquals("dispatcherTest1/ajaxIncCount, " +
                 "dispatcherTest1/ajaxUserError, dispatcherTest1/error, " +
                 "dispatcherTest1/handledError, dispatcherTest1/incCount, " +
                 "dispatcherTest1/postTest, dispatcherTest1/postUserError, " +
                 "dispatcherTest1/resetCount, dispatcherTest1/userError",
                 StringUtil.join(names, ", "));
     }
     public void test_findMethod_methodNotFound() {
         Class<?> cl = dispatcher.findClass("org.fiz.Interactor", null);
         TestUtil.assertSubstring("name of found class", "org.fiz.Interactor",
                 cl.getName());
         boolean gotException = false;
         try {
         dispatcher.findMethod("dispatcherTest1/twoArgs", 15,
                 new ServletRequestFixture("/dispatcherTest1/twoArgs"));
         }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": couldn't find method " +
                     "\"twoArgs\" with proper signature in class " +
                     "DispatcherTest1Interactor", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_findDirectMethod_basics() {
         String name = "DispatcherTest3/ajaxRequest";
         Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                 name, name.indexOf("/"),
                 new ServletRequestFixture());
         assertEquals("method name", "ajaxRequest", method.method.getName());
         assertEquals("DispatcherTest3/ajaxRequest",
                 StringUtil.join(dispatcher.methodMap.keySet(), ", "));
     }
     public void test_findDirectMethod_methodNameDoesntStartWithAjax() {
         boolean gotException = false;
         try {
             String name = "DispatcherTest3/bogus";
             Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                     name, name.indexOf("/"),
                     new ServletRequestFixture());
             }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": method name must start " +
                     "with \"ajax\"", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findDirectMethod_badClassName() {
         boolean gotException = false;
         try {
             String name = "Bogus/ajaxRequest";
             Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                     name, name.indexOf("/"),
                     new ServletRequestFixture());
             }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": can't find " +
                     "class \"Bogus\"", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findDirectMethod_classDoesntImplementDirectAjax() {
         boolean gotException = false;
         try {
             String name = "IOError/ajaxRequest";
             Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                     name, name.indexOf("/"),
                     new ServletRequestFixture());
             }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": class IOError doesn't " +
                     "implement DirectAjax interface", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findDirectMethod_noSuchMethod() {
         boolean gotException = false;
         try {
             String name = "DispatcherTest3/ajaxBogus";
             Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                     name, name.indexOf("/"),
                     new ServletRequestFixture());
             }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": couldn't find " +
                     "method \"ajaxBogus\" with proper signature in class " +
                     "DispatcherTest3", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findDirectMethod_badMethodSignature() {
         boolean gotException = false;
         try {
             String name = "DispatcherTest3/ajaxExtraArgument";
             Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                     name, name.indexOf("/"),
                     new ServletRequestFixture());
             }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": couldn't find " +
                     "method \"ajaxExtraArgument\" with proper signature " +
                     "in class DispatcherTest3", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findDirectMethod_badMethodSignature2() {
         boolean gotException = false;
         try {
             String name = "DispatcherTest3/ajaxBadSignature";
             Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                     name, name.indexOf("/"),
                     new ServletRequestFixture());
             }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": couldn't find " +
                     "method \"ajaxBadSignature\" with proper signature " +
                     "in class DispatcherTest3", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findDirectMethod_methodNotStatic() {
         boolean gotException = false;
         try {
             String name = "DispatcherTest3/ajaxNotStatic";
             Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                     name, name.indexOf("/"),
                     new ServletRequestFixture());
             }
         catch (Dispatcher.UnsupportedUrlError e) {
             assertEquals("exception message",
                     "unsupported URL \"/x/y/z\": method \"ajaxNotStatic\" " +
                     "in class DispatcherTest3 isn't static", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_findDirectMethod_invokeMethod() {
         dispatcher.service(new ServletRequestFixture(
                 "/DispatcherTest3/ajaxRequest"), new ServletResponseFixture());
         assertEquals("DispatcherTest3 log", "Invoked ajaxRequest",
                 DispatcherTest3.log);
     }
 
     public void test_initMainConfigDataset_compound() {
         Config.setDataset("main", new CompoundDataset(new Dataset("a", "1"),
                 new Dataset("b", "2")));
         Dispatcher.initMainConfigDataset("/a/b/c");
         assertEquals("updated main dataset", "Component #0:\n" +
                 "  a:    1\n" +
                 "  home: /a/b/c\n" +
                 "Component #1:\n" +
                 "  b: 2\n",
                 Config.getDataset("main").toString());
     }
     public void test_initMainConfigDataset_simpleDataset() {
         Config.setDataset("main", new Dataset("a", "1"));
         Dispatcher.initMainConfigDataset("/a/b/c");
         assertEquals("updated main dataset", "a:    1\n" +
                 "home: /a/b/c\n",
                 Config.getDataset("main").toString());
     }
 }
