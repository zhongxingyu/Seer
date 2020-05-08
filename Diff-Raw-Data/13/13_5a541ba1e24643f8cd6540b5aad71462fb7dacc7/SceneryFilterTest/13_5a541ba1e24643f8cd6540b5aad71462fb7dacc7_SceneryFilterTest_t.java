 package br.com.devx.scenery.web;
 
 import br.com.devx.scenery.web.chanchito.ChanchitoTemplateHandler;
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 
 public class SceneryFilterTest extends HttpUnitTestCase {
     private TargetApp m_app;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         AppsConfig config = AppsConfig.getInstance();
         config.reset();
         m_app = config.getTargetApp();
         m_app.setWebappPath("src/test/webapp");
         m_app.addTemplateHandler(new ChanchitoTemplateHandler());
     }
 
 
     public void testIndexHtml() throws IOException, SAXException {
         pageShouldContains("/", "Hello, index.html!");
     }
 
     /**
      * Se o cenario estah mapeado, usa-o
      */
     public void testVelocity() throws IOException, SAXException {
         pageShouldContains("/velocity.do", "Hello, Zeh maneh");
     }
 
     /**
      * Freemarker
      */
     public void testFreemarker() throws IOException, SAXException {
         pageShouldContains("/freemarker.do", "Hello, Zeh maneh");
     }
 
     public void testFreemarkerInclude() throws IOException, SAXException {
         pageShouldContains("/freemarker.do?test=include", "Hello, Zeh maneh");
     }
 
     public void testFreemarkerMacro() throws IOException, SAXException {
         pageShouldContains("/freemarker.do?test=macro", "<script type=");
     }
 
     public void testCustomTemplate() throws IOException, SAXException {
         pageShouldContains("/custom-template.do", "direct: Hello, Mr. Custom Template");
     }
 
     public void testCustomTemplateEx() throws IOException, SAXException {
         pageShouldContains("/custom-template-ex.do", "<html>", "Mr. Custom Template");
     }
 
     public void testRedirect() throws IOException, SAXException {
         m_app.setAlternativeUrl("http://gandralf.github.com");
         pageShouldContains("/scenery-manager", "scenery manager");
     }
 
     public void testSceneryError() throws IOException, SAXException {
         pageShouldContains("/velocity.do?error=true",
                 "<pre>     &quot;i'll forget a comma here&quot;</pre>",
                 "&quot;nice. the last one doesn't need that&quot;</span>");
     }
 
    public void testSceneryError2() throws IOException, SAXException {
        pageShouldContains("/velocity.do?error=2",
                "I was distracted by",
                "};");
    }

     public void testFreemarkerTemplateError() throws IOException, SAXException {
         pageShouldContains("/freemarker.do?test=ftl-error", "How do you do?", "&lt;");
     }
 
     public void testVelocityTemplateError() throws IOException, SAXException {
         pageShouldContains("/velocity.do?test=vm-error", "How do you do?", "&lt;");
     }
 
     public void testVelocityMacro() throws IOException, SAXException {
         pageShouldContains("/velocityx.do", "<td bgcolor=\"blue\">hello</td>");
     }
 
     public void testVelocityParse() throws IOException, SAXException {
         pageShouldContains("/velocityx.do?test=parse", "<td bgcolor=\"blue\">hello</td>");
     }
 
     public void testVelocityTools() throws IOException, SAXException {
         pageShouldContains("/tool.do",
                 "He didn't say, \\\"Stop!\\\"",
                 "&quot;bread&quot; &amp; &quot;butter&quot;",
                 "hello+here+%26+there");
     }
 
     public void testWebInfClasses() throws IOException, SAXException {
         pageShouldContains("/classloader.do", "Hello, Me!, your id is 1");
     }
 
     public void testWebInfLib() throws IOException, SAXException {
         pageShouldContains("/classloader.do?from=lib", "Hello, Me!, your id is 1");
     }
 
     public void testSitemesh() throws IOException, SAXException {
         pageShouldContains("/sitemesh.do",
                 "Hello, Zeh maneh!", // In hello.ftl (body)
                 "<title>Hello, sitemesh!</title>", // In main.ftl decorator
                 "Welcome to the world");
     }
 
     public void testVMSitemesh() throws IOException, SAXException {
         pageShouldContains("/vmsitemesh.do",
                 "Hello, Zeh maneh!", // body at hello.vm
                 "<title>Hello, sitemesh!</title>", // decorator at main.vm
                 "Welcome to the world", // decorator at main.vm
                 "This is velocity!"); // body at hello.vm
     }
 
     public void testHeadBody() throws IOException, SAXException {
         String text = pageContentInLowerCase("/vmsitemesh-head-body.do");
         assertTextContains(text,
                 "Hello, Zeh maneh!", // body at hello.vm
                 "<title>Hello, sitemesh!</title>", // main.vm decorator
                 "Welcome to the world");  // main.vm decorator
         assertTextHasNoDuplicatedTags(text);
     }
 
     private void assertTextHasNoDuplicatedTags(String text) {
         assertEquals("Double html", -1, text.indexOf("<html>", text.indexOf("<html>") + 1));
         assertEquals("Double head", -1, text.indexOf("<head>", text.indexOf("<head>") + 1));
         assertEquals("Double body", -1, text.indexOf("<body>", text.indexOf("<body>") + 1));
     }
 
     public void testSitemeshPrint() throws IOException, SAXException {
         String text = pageContentInLowerCase("/sitemesh-print.do");
         assertTextContains(text, "Hello, Zeh maneh!", "<title>Hello, sitemesh!</title>");
         assertFalse(text.contains("Welcome to the world"));
     }
 
     private void pageShouldContains(String uri, String ... expectedTexts) throws IOException, SAXException {
         String text = pageContentInLowerCase(uri);
         assertTextContains(text, expectedTexts);
     }
 
     private String pageContentInLowerCase(String uri) throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost" + uri );
         WebResponse response = m_client.getResponse(request);
         
         return response.getText().trim().toLowerCase();
     }
 
     private void assertTextContains(String text, String ... expectedTexts) {
         for (String expectedText: expectedTexts) {
             String expected = expectedText.toLowerCase();
             assertTrue("\"" + text + "\"\n does not contains " + expected, text.contains(expected));
         }
     }
 }
