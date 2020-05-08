 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.enr.markdownj.extras;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.github.enr.markdownj.extras.FileUtils;
 import com.github.enr.markdownj.extras.MarkdownApp;
 
 /**
  * Integration test for MarkdownApp
  */
 public class AppTest {
 
     static private Logger logger;
 
     @SuppressWarnings("static-access")
     protected Logger log() {
         if (this.logger == null)
             this.logger = LoggerFactory.getLogger(this.getClass());
         return this.logger;
     }
 
     private String headerPath;
     private String footerPath;
     private String sourcePath;
 
     private String baseTestDestination;
 
     @BeforeClass
     public void initData() {
         headerPath = resourceToPath("/site/templates/header.html");
         footerPath = resourceToPath("/site/templates/footer.html");
         sourcePath = resourceToPath("/site/markdown");
         baseTestDestination = "target/markdownj";
     }
 
     @Test
     public void testMainMethodCall() {
         String destination = buildDestinationDir("testMainMethodCall");
         String[] args = { "--source", sourcePath, "--destination", destination, "--header", headerPath, "--footer", footerPath };
         MarkdownApp.main(args);
         File destinationFile = new File(destination + "/sub/file.html");
         assertTrue(destinationFile.exists());
         assertEquals(readCreatedFile(destinationFile), "<html>\n<h1>This is an H1</h1>\n\n<p>file.markdown</p>\n\n</html>\n");
     }
 
     @Test
     public void testMainMethodCallWithExtensionsArgument() {
         String destination = buildDestinationDir("testMainMethodCallWithExtensionsArgument");
         String extensions = "markdown,text";
         String[] args = { "--source", sourcePath, "--destination", destination, "--header", headerPath, "--footer", footerPath, "--extensions",
                 extensions };
         MarkdownApp.main(args);
         File destinationFile = new File(destination + "/sub/file.html");
         assertTrue(destinationFile.exists());
         assertEquals(readCreatedFile(destinationFile), "<html>\n<h1>This is an H1</h1>\n\n<p>file.markdown</p>\n\n</html>\n");
         File mdExtDestinationFile = new File(destination + "/sub/md-ext.html");
         assertTrue(!mdExtDestinationFile.exists(), String.format("File with extension 'md' processed, but processable extensions list is: '%s'",
                 extensions));
     }
 
     @Test
     public void testProgrammaticCall() {
         String destination = buildDestinationDir("testProgrammaticCall");
         MarkdownApp app = new MarkdownApp();
         app.setSource(sourcePath);
         app.setDestination(destination);
         app.setHeader(headerPath);
         app.setFooter(footerPath);
         app.process();
         File destinationFile = new File(destination + "/sub/file.html");
         assertTrue(destinationFile.exists());
         assertEquals(readCreatedFile(destinationFile), "<html>\n<h1>This is an H1</h1>\n\n<p>file.markdown</p>\n\n</html>\n");
         File mdExtDestinationFile = new File(destination + "/sub/md-ext.html");
         assertTrue(mdExtDestinationFile.exists());
     }
 
     @Test
     public void testNoProcessableExtensions() {
         String destination = buildDestinationDir("testNoProcessableExtensions");
         File mdExtDestinationFile = new File(destination + "/sub/md-ext.html");
         MarkdownApp app = new MarkdownApp();
         app.setSource(sourcePath);
         app.setDestination(destination);
         app.setHeader(headerPath);
         app.setFooter(footerPath);
         app.process();
         assertTrue(mdExtDestinationFile.exists(), "File with extension 'md' not processed");
     }
 
     @Test
     public void testFileWithNoProcessableExtension() {
         String destination = buildDestinationDir("testFileWithNoProcessableExtension");
         File mdExtDestinationFile = new File(destination + "/sub/md-ext.html");
         MarkdownApp app = new MarkdownApp();
         app.setSource(sourcePath);
         app.setDestination(destination);
         app.setHeader(headerPath);
         app.setFooter(footerPath);
         List<String> exts = new ArrayList<String>();
         exts.add("markdown");
         app.setProcessableExtensions(exts);
         app.process();
         assertTrue(!mdExtDestinationFile.exists(), String.format("File with extension 'md' processed, but processable extensions are: %s", app
                 .getProcessableExtensions()));
     }
 
     @Test
     public void testFileWithProcessableExtension() {
         String destination = buildDestinationDir("testFileWithProcessableExtension");
         File mdExtDestinationFile = new File(destination + "/sub/md-ext.html");
         MarkdownApp app = new MarkdownApp();
         app.setSource(sourcePath);
         app.setDestination(destination);
         app.setHeader(headerPath);
         app.setFooter(footerPath);
         app.addProcessableExtension("md");
         app.process();
         assertTrue(mdExtDestinationFile.exists(), "File with extension 'md' not processed");
     }
 
     @Test
     public void testCodeBlockTemplate() {
         String destination = buildDestinationDir("testCodeBlockTemplate");
         String template = "<pre lang=\"%s\">%s</pre>";
         String s = sourcePath + "/code";
         String[] args = { "--source", s, "--destination", destination, "--code-template", template };
         MarkdownApp.main(args);
         File destinationFile = new File(destination + "/java.html");
         assertTrue(destinationFile.exists());
         assertEquals(readCreatedFile(destinationFile), "<p>code:<pre lang=\"java\">import org.markdownj.*;</pre></p>\n");
     }
 
     @Test
     public void testSpecialChars() {
         String destination = buildDestinationDir("testSpecialChars");
 
         MarkdownApp app = new MarkdownApp();
         app.setSource(sourcePath);
         app.setDestination(destination);
         app.setHeader(headerPath);
         app.setFooter(footerPath);
         app.setCharEncoding("UTF-8");
         app.process();
         File destinationFile = new File(destination + "/entities.html");
         assertTrue(destinationFile.exists());
         String results = readCreatedFile(destinationFile);
        assertEquals(results, "<html>\n<h1>This is H1</h1>\n\n<p>4 £ for a à</p>\n\n<pre><code>and nòw ìs code\n</code></pre>\n\n<p>the end!</p>\n\n</html>\n");
     }
 
 
     @Test
     public void testAdvancedSyntaxAndSpecialChars() {
         String destination = buildDestinationDir("testAdvancedSyntaxAndSpecialChars");
 
         MarkdownApp app = new MarkdownApp();
         app.setSource(sourcePath);
         app.setDestination(destination);
         app.setHeader(headerPath);
         app.setFooter(footerPath);
         app.setCharEncoding("UTF-8");
         app.process();
         File destinationFile = new File(destination + "/syntax.html");
         assertTrue(destinationFile.exists());
         String results = readCreatedFile(destinationFile);
         assertEquals(results, "<html>\n<p>Xite is a tool for static site generation from source files which can be in various formats.</p>\n\n<p><a href=\"http://daringfireball.net/projects/markdown\" title=\"Markdown\">Markdown</a> syntax.</p>\n\n<p>Another <a href=\"http://example.tld\">http://example.tld</a> link</p>\n\n</html>\n");
     }
     
     /**
      * Utility method to resolve path to resources.
      * 
      * @param resource
      * @return
      */
     private String resourceToPath(String resource) {
         URL url = this.getClass().getResource(resource);
         File uf = new File(url.getFile());
         return FileUtils.normalizedPath(uf.getAbsolutePath());
     }
 
     /**
      * Utility method to create destination directories.
      * 
      * @param id
      * @return
      */
     private String buildDestinationDir(String id) {
         String destination = baseTestDestination + "/" + id;
         return destination;
     }
 
     private String readCreatedFile(File afile) {
         String text;
         try {
             text = org.apache.commons.io.FileUtils.readFileToString(afile, "UTF-8");
             // Standardize line endings:
             text = text.replaceAll("\\r\\n", "\n"); // DOS to Unix
             text = text.replaceAll("\\r", "\n"); // Mac to Unix
             text = text.replaceAll("^[ \\t]+$", "");
             return text;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
 }
