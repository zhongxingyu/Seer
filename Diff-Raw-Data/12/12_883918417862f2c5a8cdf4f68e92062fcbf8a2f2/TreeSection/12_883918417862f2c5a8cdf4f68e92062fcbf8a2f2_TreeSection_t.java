 package org.fiz;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * A TreeSection displays data in a hierarchical form where subtrees
  * can be expanded or unexpanded by the user, and indentation and
  * graphics are used to show the nesting structure.  TreeSections
  * support the following constructor properties:
  *   class:          (optional) Used as the {@code class} attribute for
  *                   the HTML element that contains the TreeSection.  Defaults
  *                   to {@code TreeSection}.
  *   edgeFamily:     (optional) Determines how to display the lines along
  *                   the left edge of the tree, which connect parents to their
  *                   children.  The value forms the base name for a family
  *                   of images.  If the value is {@code x}, then there
  *                   must exist images {@code x-line.gif}, {@code x-leaf.gif},
  *                   {@code x-plus.gif}, and {@code x-minus.gif}.  See the
  *                   images themselves and the generated HTML for details
  *                   on how the images are used.  Fiz has built-in support
  *                   for families {@code treeSolid}, {@code treeDotted}, and
  *                   {@code treeNoLines}, but applications can define
  *                   additional families.  Defaults to {@code treeSolid}.
  *   id:             (required) Used as the {@code id} attribute for the
  *                   HTML element that contains the section.  Must be
  *                   unique among all ids for the page.
  *   leafStyle:      (optional) Style to use for displaying leaves of the
  *                   tree (which cannot be expanded); overridden by a
  *                   {@code style} value in records returned by
  *                   {@code request}.  Defaults to {@code TreeSection.leaf}.
  *   nodeStyle:      (optional) Style to use for displaying nodes that
  *                   have children and hence can be expanded; overridden
  *                   by a {@code style} value in records returned by
  *                   {@code request}.  Defaults to {@code TreeSection.node}.
  *   requestFactory: (required) Identifies a factory method that takes a
  *                   single String argument and returns a DataRequest whose
  *                   response will describe the contents of a node in the
  *                   tree.  Must have the form {@code class.method}, where
  *                   {@code class} is a Java class name and {@code method}
  *                   is a static method in the class.  The values expected in
  *                   the request's response are described below.  The string
  *                   argument to the factory method is the name of the node
  *                   whose contents are desired (the {@code name} value from
  *                   the record for that node, returned by a previous
  *                   request): empty string refers to root of the tree, and
  *                   is used to fetch the top-level nodes.
  *
  * The response to a DataRequest generated from {@code requestFactory} consists
  * of a dataset with one {@code record} child for each node at the current
  * level.  The TreeSection will use the following elements, if they are
  * present in a record:
  *   expandable      A value of 1 means this node can be expanded (it has
  *                   children).  If this value is absent, or has any value
  *                   other than 1, then this is a leaf node.
  *   name            Must be provided if the node is expandable; this value
  *                   is passed to the {@code requestFactory} method
  *                   to fetch the node's children.
  *   style           If present, specifies the style(s) to use for displaying
  *                   this node.  See below for more information on styles.
  *
  * Additional elements besides these may be used to display the node, as
  * determined by the style.
  *
  * Styles are used to customize the display of tree elements in a multi-step
  * process.  The style to use for a given tree element is determined by the
  * {@code style} value in the record for that tree element, if there is one.
  * If there is not an explicit {@code style} for the particular element, then
  * the {@code nodeStyle} configuration property for the TreeSection is used
  * if the element is expandable; otherwise the {@code leafStyle} configuration
  * property is used.  The resulting style is used as the path name of an entry
  * in the {@code styles} configuration dataset; the value of this entry is
  * a template, which is expanded with the record for the element to produce
  * HTML for the element.
  *
  * For elements that have children, the element can be rendered differently
  * when it is expanded (its children are visible) from when it is unexpanded.
  * When the element is expanded, "-expanded" is appended to its style name,
  * so that it will select a different entry in the {@code styles}
  * configuration dataset.
  */
 public class TreeSection extends Section implements DirectAjax {
     // One object of the following class is stored as a page property
     // for each TreeSection in a page: it holds data that we will need
     // later on to process Ajax requests to expand nodes in the tree.
     protected static class PageProperty implements Serializable {
         // The following variables are just copies of configuration properties
         // for the section, or null if no such property.
         protected String className;
         protected String edgeFamily;
         protected String id;
         protected String leafStyle;
         protected String nodeStyle;
         protected String requestFactory;
 
         // For each expandable node that has been displayed in the table
         // so far, there is one entry in the following structure, which maps
         // from the node's id to the node's name, which is used to get
         // information about the node's children.
         protected HashMap<String,String> names = new HashMap<String,String>();
 
         public PageProperty(String className, String edgeFamily, String id,
                 String leafStyle, String nodeStyle, String requestFactory) {
             this.className = className;
             this.edgeFamily = edgeFamily;
             this.id = id;
             this.leafStyle = leafStyle;
             this.nodeStyle = nodeStyle;
             this.requestFactory = requestFactory;
         }
     }
 
     // Reference to the page state for this section, stored as a page
     // property named {@code TreeSection-id}, where {@code id} is the
     // id attribute for the section..
     protected PageProperty pageProperty;
 
     // Holds the request that provides information about top-level nodes
     // in the tree.
     DataRequest dataRequest;
 
     /**
      * Construct a TreeSection.
      * @param properties           Contains configuration information
      *                             for the section; see description above.
      */
     public TreeSection(Dataset properties) {
         this.properties = properties;
         pageProperty = new PageProperty(properties.check("class"),
                 properties.check("edgeFamily"), properties.get("id"),
                 properties.check("leafStyle"), properties.check("nodeStyle"),
                 properties.get("requestFactory"));
         if (pageProperty.edgeFamily == null) {
             pageProperty.edgeFamily = "treeSolid";
         }
         if (pageProperty.leafStyle == null) {
             pageProperty.leafStyle = "TreeSection.leaf";
         }
         if (pageProperty.nodeStyle == null) {
             pageProperty.nodeStyle = "TreeSection.node";
         }
     }
 
     /**
      * This method is invoked during the first phase of rendering a page;
      * it is used to create a data request that will provide information about
      * the top level of the tree.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     @Override
     public void addDataRequests(ClientRequest cr) {
         dataRequest = (DataRequest) Util.invokeStaticMethod(
                 pageProperty.requestFactory, "");
         cr.addDataRequest(dataRequest);
     }
 
     /**
      * This method is an Ajax entry point, invoked to expand an element
      * in a TreeSection.
      * @param cr                   Overall information about the client
      *                             request being serviced;  there must
      *                             be {@code sectionId} and {@code nodeId}
      *                             values in the main dataset, which
      *                             give the id attributes for the TreeSection
      *                             and for the node being expanded.
      */
     public static void ajaxExpand(ClientRequest cr) {
         // Retrieve state information about the row and the overall section.
         Dataset main = cr.getMainDataset();
         PageProperty pageProperty = (PageProperty)
                 cr.getPageProperty(main.get("sectionId"));
 
         // Invoke a data request to fetch information about the children of
         // the element being expanded, then generate a <table> that will
         // display the children.
         String id = main.get("nodeId");
         DataRequest request = (DataRequest) Util.invokeStaticMethod(
                 pageProperty.requestFactory, pageProperty.names.get(id));
 
         StringBuilder html = new StringBuilder();
         Template.expand("<table cellspacing=\"0\" " +
                 "class=\"@1?{TreeSection}\" " +
                 "id=\"@2\">\n", html, pageProperty.className, id);
         renderChildren(cr, pageProperty,
                 request.getResponseOrAbort().getChildren("record"),
                 id, html);
         html.append("</table>\n");
 
         // Generate a Javascript method call to instantiate the HTML.
         StringBuilder javascript = new StringBuilder(html.length() + 50);
         Template.expand("Fiz.ids[\"@1\"].expand(\"@2\");\n", javascript,
                 Template.SpecialChars.JAVASCRIPT, id, html);
         cr.evalJavascript(javascript);
     }
 
     /**
      * This method is invoked during the final phase of rendering a page;
      * it generates HTML for this section and appends it to the Html
      * object associated with {@code cr}.
      * @param cr                   Overall information about the client
      *                             request being serviced; HTML will be
      *                             appended to {@code cr.getHtml()}.
      */
     @Override
     public void render(ClientRequest cr) {
         cr.setPageProperty(pageProperty.id, pageProperty);
         Html html = cr.getHtml();
         StringBuilder out = html.getBody();
         if (pageProperty.className == null) {
             html.includeCssFile("TreeSection.css");
         }
         html.includeJsFile("static/fiz/TreeRow.js");
         Template.expand("\n<!-- Start TreeSection @1 -->\n" +
                 "<table cellspacing=\"0\" class=\"@2?{TreeSection}\" " +
                 "id=\"@1\">\n", out, pageProperty.id, pageProperty.className);
         renderChildren(cr, pageProperty,
                 dataRequest.getResponseOrAbort().getChildren("record"),
                 pageProperty.id, out);
         Template.expand("</table>\n" +
                 "<!-- End TreeSection @1 -->\n", out, pageProperty.id);
     }
 
     /**
      * This method generates rows for an HTML table that will display all
      * of the children of a particular node.  It is invoked both from the
      * {@code html} method and from {@code ajaxExpand}, which is a static
      * method.
      * @param cr                   Overall information about the client
      *                             request being serviced; HTML will be
      *                             appended to {@code cr.getHtml()}.
      * @param pageProperty         State for this section, retained in the
      *                             session for future Ajax requests.
      * @param children             Contains one dataset for each child
      *                             to display, in the order they should be
      *                             displayed.
      * @param baseId               Used to generate ids for the rows of
      *                             the table; rows will have ids
      *                             {@code baseId.0}, {@code baseId.1}, etc.
      * @param out                  Generated HTML is appended here.  Only
      *                             {@code <tr>} elements are generated here;
      *                             the {@code <table>} and {@code </table>}
      *                             elements must be generated by the caller.
      */
     protected static void renderChildren(ClientRequest cr,
             PageProperty pageProperty, ArrayList<Dataset> children,
             String baseId, StringBuilder out) {
         // The following variable is used to generate an alternate (expanded)
         // style for each row.  It is declared here so that it can be reused
         // for each of the rows (minimize garbage collection).
         StringBuilder expandedRow = new StringBuilder();
 
         Dataset styles = Config.getDataset("styles");
         for (int i = 0; i < children.size(); i++) {
             Dataset child = children.get(i);
             Boolean lastElement = (i == (children.size()-1));
             String rowId = baseId + "_" + i;
 
             // Is this child a node (expandable) or a leaf?
             Boolean expandable = false;
             String value = child.check("expandable");
             if ((value != null) && (value.equals("1"))) {
                 expandable = true;
             }
 
             // Compute the style to use for this child.
             String style = child.check("style");
             if (style == null) {
                 style = (expandable ? pageProperty.nodeStyle :
                         pageProperty.leafStyle);
             }
 
             // Each child is drawn as a table row with two cells.  The
             // left cell contains lines connecting all of the children
             // together vertically (if desired) and a box displaying plus
             // or minus for nodes (initially plus).  The right cell displays
             // the icon(s) and name for this child (or whatever is specified
             // by the template for this child's style).
             int rowStart = out.length();
             Template.expand("  <tr id=\"@1\">\n", out, rowId);
             String repeat = "repeat-y";
             if (lastElement) {
                 // Don't extend the vertical line for the last element:
                 // it should have an "L" shape.
                 repeat = "no-repeat";
             }
             Template.expand("    <td class=\"left\" " +
                     "style=\"background-image: url(/static/fiz/images/" +
                     "@1-line.gif); background-repeat: @2;\"",
                     out, pageProperty.edgeFamily, repeat);
             int midPoint = out.length();
 
             // If the element is expandable, generate an onclick handler
             // for the left cell (which will contain a "+" box).
             if (expandable) {
                 Template.expand(" onclick=\"@1\"", out,
                    Ajax.invoke(cr, "/TreeSection/ajaxExpand?" +
                     "sectionId=@1&nodeId=@2", pageProperty.id, rowId));
             }
 
             // Now display one of 2 images in the left cell: a plus if the
             // element is expandable, or a horizontal line if this is a
             // leaf node.
             Template.expand("><img src=\"/static/fiz/images/@1-@2.gif\"></td>\n",
                     out, pageProperty.edgeFamily,
                     (expandable ? "plus": "leaf"));
 
             // Render the cell on the right, using a template selected by
             // the style.
             out.append("    <td class=\"right\">");
             Template.expand(styles.getPath(style), child, out);
             out.append("</td>\n");
             out.append("  </tr>\n");
 
             // If this element is expandable then do some additional things
             // to support expansion and unexpansion of this element.
             if (expandable) {
                 pageProperty.names.put(rowId, child.get("name"));
 
                 // Next, create a Javascript object holding two copies of
                 // the HTML for the table row: the one we just rendered (used
                 // when the element is unexpanded), and an alternate version
                 // to use when the element is expanded.
                 expandedRow.setLength(0);
                 expandedRow.append(out.substring(rowStart, midPoint));
                 Template.expand(" onclick=\"Fiz.ids['@1'].unexpand();\">" +
                         "<img src=\"/static/fiz/images/@2-minus.gif\"></td>\n",
                         expandedRow, rowId, pageProperty.edgeFamily);
                 expandedRow.append("    <td class=\"right\">");
                 Template.expand(styles.getPath(style + "-expanded"),
                         child, expandedRow);
                 expandedRow.append("</td>\n");
                 expandedRow.append("  </tr>\n");
                 cr.evalJavascript(Template.expand("Fiz.ids[\"@1\"] = " +
                         "new Fiz.TreeRow(\"@1\", \"@2\", \"@3\");\n",
                         Template.SpecialChars.JAVASCRIPT, rowId,
                         out.substring(rowStart), expandedRow));
 
                 // Finally, add an additional row to the table we are
                 // generating, which will hold the children of this element
                 // if/when the element is expanded.  For now, this row is
                 // invisible.  We add this row now because we have information
                 // now that is needed to generate the row (lastEement), and
                 // that information won't be readily available when it's
                 // time to expand the element.
                 Template.expand("  <tr id=\"@(1)_childRow\" " +
                         "style=\"display:none\">\n", out, rowId);
                 if (!lastElement) {
                     Template.expand("    <td style=\"background-image: " +
                             "url(/static/fiz/images/@1-line.gif); " +
                             "background-repeat: repeat-y;\">",
                             out, pageProperty.edgeFamily);
                 } else {
                     out.append("    <td>");
                 }
                 Template.expand("</td>\n    <td><div class=\"nested\" " +
                         "id=\"@(1)_childDiv\"></div></td>\n", out, rowId);
                 out.append("  </tr>\n");
             }
         }
     }
 }
