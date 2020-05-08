 package org.fiz;
 
 import java.util.*;
 
 /**
  * Junit tests for the TreeSection class.
  */
 public class TreeSectionTest extends junit.framework.TestCase {
     protected ClientRequest cr;
     protected static class RequestFactory  {
         public static DataRequest request(String nodeName) {
             return RawDataManager.newRequest(new Dataset(
                 "parent", nodeName, "record", new Dataset(
                 "name", "child1", "text", "Child #1")));
         }
     }
 
     public void setUp() {
         cr = new ClientRequestFixture();
         ServletRequestFixture.session = null;
         Config.setDataset("styles", YamlDataset.newStringInstance(
                 "TreeSection:\n" +
                 "  leaf: \"leaf: @name\"\n" +
                 "  node: \"node: @name\"\n" +
                 "  node-expanded: \"node-expanded: @name\"\n" +
                 "  leaf2: \"leaf2: @name\"\n" +
                 "  node2: \"node2: @name\"\n" +
                 "  node2-expanded: \"node-expanded: @name\"\n"));
     }
 
     public void test_constructor_defaultProperties() {
         TreeSection tree = new TreeSection(new Dataset("id", "1234",
                 "requestFactory", "getInfo"));
         assertEquals("class property", null,
                 tree.pageProperty.className);
         assertEquals("edgeFamily property", "treeSolid",
                 tree.pageProperty.edgeFamily);
         assertEquals("id property", "1234",
                 tree.pageProperty.id);
         assertEquals("leafStyle property", "TreeSection.leaf",
                 tree.pageProperty.leafStyle);
         assertEquals("nodeStyle property", "TreeSection.node",
                 tree.pageProperty.nodeStyle);
         assertEquals("requestFactory property", "getInfo",
                 tree.pageProperty.requestFactory);
     }
     public void test_constructor_explicitProperties() {
         TreeSection tree = new TreeSection(new Dataset("class", "class10",
                 "edgeFamily", "family44","id", "1234",
                 "leafStyle", "style for leaves", "nodeStyle", "style for nodes",
                 "requestFactory", "getInfo"));
         assertEquals("class property", "class10",
                 tree.pageProperty.className);
         assertEquals("edgeFamily property", "family44",
                 tree.pageProperty.edgeFamily);
         assertEquals("id property", "1234",
                 tree.pageProperty.id);
         assertEquals("leafStyle property", "style for leaves",
                 tree.pageProperty.leafStyle);
         assertEquals("nodeStyle property", "style for nodes",
                 tree.pageProperty.nodeStyle);
         assertEquals("requestFactory property", "getInfo",
                 tree.pageProperty.requestFactory);
     }
 
     public void test_addDataRequests() {
         TreeSection tree = new TreeSection(new Dataset("id", "1234",
                 "requestFactory",
                 "org.fiz.TreeSectionTest$RequestFactory.request"));
         tree.addDataRequests(cr);
         assertEquals("properties dataset", "parent: \"\"\n" +
                 "record:\n" +
                 "    name: child1\n" +
                 "    text: \"Child #1\"\n",
                 cr.unnamedRequests.get(0).getResponseData().toString());
     }
 
     public void test_ajaxExpand() {
         cr.setClientRequestType(ClientRequest.Type.AJAX);
         TreeSection.PageProperty p = new TreeSection.PageProperty("TreeSection",
                 "treeSolid", "tree1", "TreeSection.leaf", "TreeSection.node",
                 "TreeSectionTest$RequestFactory.request");
         p.names.put("tree1_2", "node16");
         cr.setPageProperty("tree1",  p);
         cr.jsCode = null;
         cr.mainDataset = new Dataset("sectionId", "tree1", "nodeId", "tree1_2");
         TreeSection.ajaxExpand(cr);
         assertEquals("Ajax javascript",
                 "Fiz.ids[\"tree1_2\"].expand(\"<table cellspacing=\\\"0\\\" " +
                 "class=\\\"TreeSection\\\" id=\\\"tree1_2\\\">\\n  " +
                 "<tr id=\\\"tree1_2_0\\\">\\n    <td class=\\\"left\\\" " +
                 "style=\\\"background-image: url(/static/fiz/images/" +
                 "treeSolid-line.gif); background-repeat: " +
                 "no-repeat;\\\"><img src=\\\"/static/fiz/images/" +
                 "treeSolid-leaf.gif\\\"></td>\\n    <td " +
                 "class=\\\"right\\\">leaf: child1</td>\\n  " +
                 "</tr>\\n</table>\\n\");\n",
                 cr.jsCode.toString());
     }
 
     public void test_render_basics() {
         TreeSection tree = new TreeSection(new Dataset("id", "tree1",
                 "requestFactory", "TreeSectionTest$RequestFactory.request"));
         tree.addDataRequests(cr);
         tree.render(cr);
         assertEquals("generated HTML", "\n" +
                 "<!-- Start TreeSection tree1 -->\n" +
                 "<table cellspacing=\"0\" class=\"TreeSection\" " +
                 "id=\"tree1\">\n" +
                 "  <tr id=\"tree1_0\">\n" +
                 "    <td class=\"left\" style=\"background-image: " +
                 "url(/static/fiz/images/treeSolid-line.gif); background-repeat: " +
                 "no-repeat;\"><img src=\"/static/fiz/images/treeSolid-leaf." +
                 "gif\"></td>\n" +
                 "    <td class=\"right\">leaf: child1</td>\n" +
                 "  </tr>\n" +
                 "</table>\n" +
                 "<!-- End TreeSection tree1 -->\n",
                 cr.getHtml().getBody().toString());
         assertEquals("accumulated Javascript",
                 "Fiz.pageId = \"1\";\n",
                  cr.getHtml().jsCode.toString());
         TestUtil.assertSubstring("CSS file names", "TreeSection.css",
                 cr.getHtml().getCssFiles());
         assertEquals("Javascript file names",
                 "static/fiz/Fiz.js, static/fiz/TreeRow.js",
                 cr.getHtml().getJsFiles());
         assertEquals("names of defined page properties", "tree1",
                 StringUtil.join(cr.pageState.properties.keySet(), ", "));
     }
     public void test_render_explicitClass() {
         TreeSection tree = new TreeSection(new Dataset("id", "tree1",
                 "requestFactory", "TreeSectionTest$RequestFactory.request",
                 "class", "xyzzy"));
         tree.addDataRequests(cr);
         tree.render(cr);
         assertEquals("generated HTML", "\n" +
                 "<!-- Start TreeSection tree1 -->\n" +
                 "<table cellspacing=\"0\" class=\"xyzzy\" " +
                 "id=\"tree1\">\n" +
                 "  <tr id=\"tree1_0\">\n" +
                 "    <td class=\"left\" style=\"background-image: " +
                 "url(/static/fiz/images/treeSolid-line.gif); background-repeat: " +
                 "no-repeat;\"><img src=\"/static/fiz/images/treeSolid-leaf." +
                 "gif\"></td>\n" +
                 "    <td class=\"right\">leaf: child1</td>\n" +
                 "  </tr>\n" +
                 "</table>\n" +
                 "<!-- End TreeSection tree1 -->\n",
                 cr.getHtml().getBody().toString());
     }
 
     public void test_renderChildren_basics() {
         StringBuilder out = new StringBuilder();
         ArrayList<Dataset> children = new ArrayList<Dataset>();
         children.add(new Dataset("name", "Alice", "id", "111",
                 "expandable", "1"));
         children.add(new Dataset("name", "Bob"));
         TreeSection.PageProperty p = new TreeSection.PageProperty("TreeSection",
                 "treeSolid", "tree1", "TreeSection.leaf", "TreeSection.node",
                 "TreeSectionTest$RequestFactory.request");
         TreeSection.renderChildren(cr, p, children, "tree1_3", out);
         assertEquals("generated HTML", "  <tr id=\"tree1_3_0\">\n" +
                 "    <td class=\"left\" style=\"background-image: url(" +
                 "/static/fiz/images/treeSolid-line.gif); background-repeat: " +
                 "repeat-y;\" onclick=\"void new Fiz.Ajax({url: &quot;" +
                "/TreeSection/ajaxExpand?sectionId=tree1&amp;" +
                 "nodeId=tree1%5f3%5f0&quot;});\">" +
                 "<img src=\"/static/fiz/images/treeSolid-plus.gif\"></td>\n" +
                 "    <td class=\"right\">node: Alice</td>\n" +
                 "  </tr>\n" +
                 "  <tr id=\"tree1_3_0_childRow\" style=\"display:none\">\n" +
                 "    <td style=\"background-image: url(/static/fiz/images/" +
                 "treeSolid-line.gif); background-repeat: repeat-y;\"></td>\n" +
                 "    <td><div class=\"nested\" id=\"tree1_3_0_childDiv\">" +
                 "</div></td>\n" +
                 "  </tr>\n" +
                 "  <tr id=\"tree1_3_1\">\n" +
                 "    <td class=\"left\" style=\"background-image: url(" +
                 "/static/fiz/images/treeSolid-line.gif); background-repeat: " +
                 "no-repeat;\"><img src=\"/static/fiz/images/treeSolid-leaf.gif\">" +
                 "</td>\n" +
                 "    <td class=\"right\">leaf: Bob</td>\n" +
                 "  </tr>\n",
                 out.toString());
         assertEquals("names in pageProperty", "tree1_3_0",
                 StringUtil.join(p.names.keySet(), ", "));
         assertEquals("value for node", "Alice",
                 p.names.get("tree1_3_0"));
     }
     public void test_renderChildren_explicitStyleInChild() {
         Config.setDataset("styles", new Dataset("xyzzy", "name: @name"));
         StringBuilder out = new StringBuilder();
         ArrayList<Dataset> children = new ArrayList<Dataset>();
         children.add(new Dataset("name", "Alice", "id", "111",
                 "style", "xyzzy"));
         TreeSection.PageProperty p = new TreeSection.PageProperty("TreeSection",
                 "treeSolid", "tree1", "TreeSection.leaf", "TreeSection.node",
                 "none");
         TreeSection.renderChildren(cr, p, children, "tree1_3", out);
         assertEquals("generated HTML", "  <tr id=\"tree1_3_0\">\n" +
                 "    <td class=\"left\" style=\"background-image: url" +
                 "(/static/fiz/images/treeSolid-line.gif); " +
                 "background-repeat: no-repeat;\">" +
                 "<img src=\"/static/fiz/images/treeSolid-leaf.gif\"></td>\n" +
                 "    <td class=\"right\">name: Alice</td>\n" +
                 "  </tr>\n",
                 out.toString());
     }
     public void test_renderChildren_javascriptForExpandableElement() {
         StringBuilder out = new StringBuilder();
         ArrayList<Dataset> children = new ArrayList<Dataset>();
         children.add(new Dataset("name", "Alice", "id", "111",
                 "expandable", "1"));
         children.add(new Dataset("name", "Bob"));
         TreeSection.PageProperty p = new TreeSection.PageProperty("TreeSection",
                 "treeSolid", "tree1", "TreeSection.leaf", "TreeSection.node",
                 "none");
         TreeSection.renderChildren(cr, p, children, "tree1_3", out);
         String javascript = cr.getHtml().jsCode.toString();
         assertEquals("accumulated Javascript",
                 "Fiz.ids[\"tree1_3_0\"] = new Fiz.TreeRow(\"tree1_3_0\", " +
                 "\"  <tr id=\\\"tree1_3_0\\\">\\n    <td class=\\\"left\\\" " +
                 "style=\\\"background-image: url(/static/fiz/images/treeSolid-" +
                 "line.gif); background-repeat: repeat-y;\\\" onclick=\\\"void " +
                "new Fiz.Ajax({url: &quot;/TreeSection/ajaxExpand?" +
                 "sectionId=tree1&amp;nodeId=tree1%5f3%5f0&quot;});\\\">" +
                 "<img src=\\\"/static/fiz/images/treeSolid-plus.gif\\\"></td>\\n" +
                 "    <td class=\\\"right\\\">node: Alice</td>\\n  </tr>\\n\"," +
                 " \"  <tr id=\\\"tree1_3_0\\\">\\n    <td class=\\\"left\\\" " +
                 "style=\\\"background-image: url(/static/fiz/images/treeSolid-" +
                 "line.gif); background-repeat: repeat-y;\\\" onclick=\\\"" +
                 "Fiz.ids['tree1_3_0'].unexpand();\\\"><img src=\\\"/static/fiz/" +
                 "images/treeSolid-minus.gif\\\"></td>\\n    <td class=\\\"" +
                 "right\\\">node-expanded: Alice</td>\\n  </tr>\\n\");\n",
                  javascript.substring(javascript.indexOf("Fiz.ids")));
     }
     public void test_renderChildren_lastRowExpandable() {
         StringBuilder out = new StringBuilder();
         ArrayList<Dataset> children = new ArrayList<Dataset>();
         children.add(new Dataset("name", "Alice", "id", "111",
                 "expandable", "1"));
         TreeSection.PageProperty p = new TreeSection.PageProperty("TreeSection",
                 "treeSolid", "tree1", "TreeSection.leaf", "TreeSection.node",
                 "none");
         TreeSection.renderChildren(cr, p, children, "tree1_3", out);
         TestUtil.assertSubstring("generated HTML",
                 "<tr id=\"tree1_3_0_childRow\" style=\"display:none\">\n" +
                 "    <td></td>\n" +
                 "    <td><div class=\"nested\" id=\"tree1_3_0_childDiv\">" +
                 "</div></td>\n" +
                 "  </tr>\n",
                 out.toString());
     }
 
 }
