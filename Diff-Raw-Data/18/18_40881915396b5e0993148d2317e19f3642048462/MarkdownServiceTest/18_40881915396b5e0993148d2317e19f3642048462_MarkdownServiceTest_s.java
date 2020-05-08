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
 
 import java.net.URL;
 
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.github.enr.markdownj.extras.MarkdownService;
 import com.github.enr.markdownj.extras.MarkdownServiceImpl;
 import com.petebevin.markdown.Entities;
 
 /**
  * 
  */
 public class MarkdownServiceTest {
 
     MarkdownService service;
 
     URL headerUrl;
 
     URL footerUrl;
 
     URL winEncodingHeaderUrl;
 
     String markdownContent = "# This is an H1";
     String htmlContent = "<h1>This is an H1</h1>";
 
     String expected;
 
     @BeforeMethod
     public void initData() {
         service = new MarkdownServiceImpl();
         headerUrl = this.getClass().getResource("/site/templates/header.html");
         winEncodingHeaderUrl = this.getClass().getResource("/site/templates/header-win.html");
         footerUrl = this.getClass().getResource("/site/templates/footer.html");
         expected = String.format("<html>%s%s%s%s</html>%s", MarkdownService.EOL, htmlContent, MarkdownService.EOL, MarkdownService.EOL,
                 MarkdownService.EOL);
     }
 
     @Test
     public void testUsingPaths() {
         service.setContent(markdownContent);
         service.setHeaderPath(headerUrl.getPath());
         service.setFooterPath(footerUrl.getPath());
         assertEquals(service.process(), expected);
     }
 
     @Test
     public void testUsingUrls() {
         service.setContent(markdownContent);
         service.setHeaderUrl(headerUrl);
         service.setFooterUrl(footerUrl);
         assertEquals(service.process(), expected);
     }
 
     @Test
     public void testUsingStrings() {
         service.setContent(markdownContent);
         service.setHeader(String.format("<html>%s", MarkdownService.EOL));
         service.setFooter(String.format("%s</html>%s", MarkdownService.EOL, MarkdownService.EOL));
         assertEquals(service.process(), expected);
     }
 
     @Test
     public void testOnlyContent() {
         service.setContent(markdownContent);
         String expectedForContentOnly = htmlContent + MarkdownService.EOL;
         assertEquals(service.process(), expectedForContentOnly);
     }
 
     @Test
     public void testWinEncoding() {
         service.setContent(markdownContent);
         service.setHeaderUrl(winEncodingHeaderUrl);
         service.setFooterUrl(footerUrl);
         assertEquals(service.process(), expected);
     }
 
     @Test
     public void testCodeBlockTemplateSetting() {
         service.setContent("\tlang:java\n\tpackage my;\n");
         service.setCodeBlockTemplate("<pre lang=\"%s\">%s</pre>");
         assertEquals(service.process(), "<pre lang=\"java\">package my;</pre>\n");
     }
 
     @Test
     public void testHtmlEntities() {
         service.setContent("I'am an ò & <>");
        service.setHtmlEntities(Entities.HTML_401);
        assertEquals(service.process(), "<p>I'am an &#242; &#38; &#60;&#62;</p>\n");
     }
 }
