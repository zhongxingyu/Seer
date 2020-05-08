 package org.fiz;
 
 import java.io.*;
 
 /**
  * Junit tests for the TabSection class.
  */
 
 public class TabSectionTest extends junit.framework.TestCase {
     protected ClientRequest cr;
 
     public void setUp() {
         cr = new ClientRequestFixture();
         TabSection.setTemplate(null);
     }
 
     public void tearDown() {
         Util.deleteTree("_tabtest_");
     }
 
     public void test_constructor() {
         TabSection section = new TabSection(new Dataset("a", "1", "b", "2"),
                 "tab14", new Dataset("id", "1"), new Dataset("id", "2"));
         assertEquals("configuration properties", "a: 1\n" +
                 "b: 2\n",
                 section.properties.toString());
         assertEquals("selected tab", "tab14", section.selected);
         assertEquals("tab descriptions", "id: 1\n" +
                 "--------\n" +
                 "id: 2\n",
                 StringUtil.join(section.tabs, "--------\n"));
     }
 
     public void test_html_basics() {
         TabSection section = new TabSection(new Dataset("id", "section12"),
                 "second",
                 new Dataset("id", "first", "text", "First", "url", "/a/b"),
                 new Dataset("id", "second", "text", "Second", "url", "/a/c"),
                 new Dataset("id", "third", "text", "Third", "url", "/xyz"));
         section.html(cr);
         TestUtil.assertXHTML(cr.getHtml().getBody().toString());
         assertEquals("generated HTML", "\n" +
                 "<!-- Start TabSection section12 -->\n" +
                 "<table id=\"section12\" class=\"TabSection\" cellspacing=\"0\">\n" +
                 "  <tr>\n" +
                 "    <td class=\"spacer\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"left\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"mid\" id=\"section12.first\">" +
                 "<a href=\"/a/b\"><div>First</div></a></td>\n" +
                 "    <td class=\"right\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"spacer\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"leftSelected\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"midSelected\" id=\"section12.second\">" +
                 "<a href=\"/a/c\"><div>Second</div></a></td>\n" +
                 "    <td class=\"rightSelected\"><img src=\"/fizlib" +
                 "/images/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"spacer\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"left\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"mid\" id=\"section12.third\">" +
                 "<a href=\"/xyz\"><div>Third</div></a></td>\n" +
                 "    <td class=\"right\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "    <td class=\"rightSpacer\"><img src=\"/fizlib/images" +
                 "/blank.gif\" alt=\"\" /></td>\n" +
                 "  </tr>\n" +
                 "</table>\n" +
                 "<!-- End TabSection section12 -->\n",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_noRequest() {
         TabSection section = new TabSection(new Dataset("id", "section12"),
                 "xxx",
                 new Dataset("id", "first", "text", "Name: @name, age: @age",
                 "url", "/a/b"));
         cr.showSections(section);
         TestUtil.assertXHTML(cr.getHtml().getBody().toString());
         TestUtil.assertSubstring("tab text", "<div>Name: Alice, age: 36</div>",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_useResultOfRequest() {
         TabSection section = new TabSection(YamlDataset.newStringInstance(
                 "id:      section12\n" +
                 "request:\n" +
                 "  manager: raw\n" +
                 "  result:\n" +
                 "    age: 44\n"),
                 "xxx",
                 new Dataset("id", "first", "text", "Name: @name, age: @age",
                 "url", "/a/b"));
         cr.showSections(section);
         TestUtil.assertXHTML(cr.getHtml().getBody().toString());
         TestUtil.assertSubstring("tab text", "<div>Name: Alice, age: 44</div>",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_defaultStyle() {
         Config.setDataset("tabSections", YamlDataset.newStringInstance(
                 "testStyle:\n" +
                 "  name: testStyle\n" +
                 "tabGray:\n" +
                 "  name: tabGray\n"));
         Config.setDataset("css", new Dataset("name", "css config",
                 "border", "1px solid red"));
         TabSection.setTemplate("name: <@name>, border: @border");
         TabSection section = new TabSection(new Dataset("id", "section12"),
                 "xxx",
                 new Dataset("id", "first", "text", "Name: @name, age: @age",
                 "url", "/a/b"));
         section.html(cr);
         assertEquals("generated CSS", "name: <tabGray>, border: 1px solid red",
                 cr.getHtml().css.toString());
     }
     public void test_html_explicitStyle() {
         Config.setDataset("tabSections", YamlDataset.newStringInstance(
                 "testStyle:\n" +
                 "  sidePadding: 2px\n" +
                 "  border: 1px solid red\n"));
         TabSection.setTemplate("sidePadding: @sidePadding, border: @border");
         TabSection section = new TabSection(new Dataset("id", "section12",
                 "style", "testStyle"),
                 "xxx",
                 new Dataset("id", "first", "text", "Name: @name, age: @age",
                 "url", "/a/b"));
         section.html(cr);
         assertEquals("generated CSS",
                 "sidePadding: 2px, border: 1px solid red",
                 cr.getHtml().css.toString());
     }
     public void test_html_explicitClass() {
         TabSection section = new TabSection(new Dataset("id", "section12",
                 "class", "testClass"),
                 "xxx",
                 new Dataset("id", "first", "text", "Name: @name, age: @age",
                 "url", "/a/b"));
         section.html(cr);
         TestUtil.assertSubstring("class attribute",
                 "<table id=\"section12\" class=\"testClass\"",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_templateExpansionForUrl() {
         TabSection section = new TabSection(new Dataset("id", "section12"),
                 "xxx",
                 new Dataset("id", "first", "text", "First",
                 "url", "/a/@name"));
         section.html(cr);
         TestUtil.assertSubstring("generated URL", "<a href=\"/a/Alice\">",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_javascriptAction() {
         TabSection section = new TabSection(new Dataset("id", "section12"),
                 "xxx",
                 new Dataset("id", "first", "text", "First",
                 "javascript", "window.xyz=\"@text\""));
         cr.getMainDataset().set("text", "<\">");
         section.html(cr);
         assertEquals("included Javascript files",
                "fizlib/Fiz.js, fizlib/TabSection.js",
                 cr.getHtml().getJsFiles());
         TestUtil.assertSubstring("onclick handler",
                 "<a href=\"#\" onclick=\"Fiz.TabSection.selectTab" +
                 "(&quot;section12.first&quot;); window.xyz=&quot;&lt;" +
                 "\\&quot;&gt;&quot;; return false;\">",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_ajaxAction() {
         TabSection section = new TabSection(new Dataset("id", "section12"),
                 "xxx",
                 new Dataset("id", "first", "text", "First",
                 "ajaxUrl", "a/b/@name"));
         section.html(cr);
         TestUtil.assertSubstring("onclick handler",
                 "<a href=\"#\" onclick=\"Fiz.TabSection.selectTab" +
                 "(&quot;section12.first&quot;); " +
                 "void new Fiz.Ajax(&quot;a/b/Alice&quot;);;",
                 cr.getHtml().getBody().toString());
     }
 
     public void test_registerRequests() {
         TabSection section = new TabSection(YamlDataset.newStringInstance(
                 "id:      section12\n" +
                 "request:\n" +
                 "  manager: raw\n" +
                 "  result:\n" +
                 "    age: 44\n"),
                 "xxx",
                 new Dataset("id", "first", "text", "Name: @name, age: @age",
                 "url", "/a/b"));
         section.registerRequests(cr);
         assertEquals("request information",
                 "manager: raw\n" +
                         "result:\n" +
                         "    age: 44\n",
                 section.dataRequest.getRequestData().toString());
     }
     public void test_registerRequests_noRequest() {
         TabSection section = new TabSection(new Dataset("id", "section12"),
                 "xxx",
                 new Dataset("id", "first", "text", "First",
                 "ajaxUrl", "a/b/@name"));
         section.registerRequests(cr);
         assertEquals("request pointer", null, section.dataRequest);
     }
     public void test_getTemplate_usedCachedCopy() {
         TabSection.setTemplate("cached TabSection template");
         assertEquals("getTemplate result", "cached TabSection template",
                 TabSection.getTemplate());
     }
 
     // No tests for setTemplate: it is already fully exercised by
     // other tests.
 }
