 package org.fiz;
 
 import java.util.*;
 
 /**
  * A TableSection displays information in tabular form consisting of rows and
  * columns.  TableSections support the following constructor properties:
  *   class:          (optional) Used as the {@code class} attribute for
  *                   the HTML table that displays the TableSection.
  *   emptyTemplate:  (optional) If the table is empty ({@code request} returns
  *                   no data), this template will be expanded (using
  *                   the main dataset) and displayed in a single row in the
  *                   table.  If this property is not specified then a default
  *                   value will be supplied; specify this property with an
  *                   empty value if you want an empty table to simply appear
  *                   empty.
  *   errorStyle:     (optional) If {@code request} returns an error then
  *                   this property contains the name of a template in the
  *                   {@code styles} dataset, which is expanded with the
  *                   error data and the main dataset.  The resulting HTML
  *                   is displayed as the body of the table.  In addition,
  *                   if there exists a template in the {@code styles} dataset
  *                   with the same name followed by "-bulletin", it is expanded
  *                   and the resulting HTML is displayed in the bulletin.
  *                   Defaults to "TableSection.error".
  *   id:             (optional) Used as the {@code id} attribute for the
  *                   HTML table that displays the TableSection.
  *   lastRowClass:   (optional) If this property is defined, its value will
  *                   be used as the class for the bottom row in the table.
  *                   This can be used to display the bottom row differently,
  *                   e.g. for totals.
  *   noHeader:       (optional) If this property is defined, then the
  *                   header row for the table will not be displayed, even
  *                   if some of the columns offer header information.
  *                   Regardless of this property, the header row will be
  *                   omitted if none of the columns generate header info.
  *   request:        (required) Specifies a DataRequest that will supply
  *                   data to display in the TableSection (either the name
  *                   of a request in the {@code dataRequests} configuration
  *                   dataset or a nested dataset containing the request's
  *                   arguments directly).  The response to this request must
  *                   contain one {@code record} child for each row of
  *                   the table.
  */
 public class TableSection extends Section {
     // The following variables are copies of constructor arguments.  See
     // the constructor documentation for details.
     protected Formatter[] columns;
 
     /**
      * Construct a TableSection.
      * @param properties           Contains configuration information
      *                             for the table; see description above.
      * @param columns              The remaining arguments describe the
      *                             columns of the table, in order from
      *                             left to right.  For each data record
      *                             returned by {@code request} one row will
      *                             be generated in the table; each column's
      *                             {@code html} method will be invoked to
      *                             generate the HTML between the {@code <td>}
      *                             and the {@code </td>}) for that column.
      *                             The dataset passed to the {@code html}
      *                             methods will include the information for
      *                             the row as well as the main dataset for the
      *                             client request.  If a column is also a
      *                             Column then it will also be invoked to
      *                             generate header HTML for the column.
      */
     public TableSection(Dataset properties, Formatter ... columns) {
         this.properties = properties;
         this.columns = columns;
        if (!properties.containsKey("request")) {
            throw new InternalError("TableSection constructor invoked " +
                    "without a \"request\" property");
        }
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
     public void html(ClientRequest cr) {
         Html html = cr.getHtml();
         StringBuilder out = html.getBody();
         Dataset mainDataset = cr.getMainDataset();
         String lastRowClass = properties.check("lastRowClass");
 
         // The following dataset will eventually include the data from
         // the current row plus the main dataset.
         CompoundDataset dataForRow = new CompoundDataset(null, mainDataset);
 
         // Start.
         if (!properties.containsKey("class")) {
             html.includeCssFile("TableSection.css");
         }
         Template.expand("\n<!-- Start TableSection {{@id}} -->\n" +
                 "<table {{id=\"@id\"}} class=\"@class?{TableSection}\" " +
                 "cellspacing=\"0\">\n",
                 properties, out);
 
         // Header row.
         if (!properties.containsKey("noHeader")) {
             int oldLength = out.length();
             boolean anyHeaders = false;
             out.append("  <tr class=\"header\">\n");
 
             // Each iteration through the following loop generates the header
             // for one column.  While doing this, see if any of the columns
             // produce actual headers (if not, we will omit the entire
             // header row).
             for (int col = 0; col < columns.length; col++) {
                 printTd(col, out);
                 int headerStart = out.length();
                 Formatter f = columns[col];
                 if (f instanceof Column) {
                     ((Column) f).headerHtml(cr, out);
                 }
                 if (out.length() > headerStart) {
                     anyHeaders = true;
                 }
                 out.append("</td>\n");
             }
             if (anyHeaders) {
                 out.append("  </tr>\n");
             } else {
                 // None of the columns had a header to display; omit the
                 // entire header row.
                 out.setLength(oldLength);
             }
         }
 
         // Body rows.
         Dataset response = dataRequest.getResponseData();
         if (response == null) {
             // The request generated an error.  Display information about
             // the error in a single row.
             out.append("  <tr class=\"error\">\n    <td colspan=\"");
             out.append(columns.length);
             out.append("\">");
             Dataset[] errors = dataRequest.getErrorData();
             errors[0].set("sectionType", "table");
             cr.showErrorInfo(properties.check("errorStyle"),
                     "TableSection.error", errors[0]);
             out.append("</td>\n  </tr>\n");
         } else {
             ArrayList<Dataset> rows = response.getChildren("record");
             if (rows.size() == 0) {
                 // The table is empty.  Display a single row containing
                 // information about that fact.
                 out.append("  <tr class=\"empty\">\n    <td colspan=\"");
                 out.append(columns.length);
                 out.append("\">");
                 String template = properties.check("emptyTemplate");
                 if (template == null) {
                     template = "There are no records to display";
                 }
                 Template.expand(template, mainDataset, out);
                 out.append("</td>\n  </tr>\n");
             } else {
                 // Normal case: there are records to display.
                 for (int i = 0; i < rows.size(); i++) {
                     dataForRow.setComponent(0, rows.get(i));
                     out.append("  <tr class=\"");
                     if ((i == (rows.size()-1)) && (lastRowClass != null)) {
                         out.append(lastRowClass);
                         out.append("\">\n");
                     } else if ((i & 1) != 0) {
                         out.append("odd\">\n");
                     } else {
                         out.append("even\">\n");
                     }
                     for (int col = 0; col < columns.length; col++) {
                         printTd(col, out);
                         columns[col].html(cr, dataForRow, out);
                         out.append("</td>\n");
                     }
                     out.append("  </tr>\n");
                 }
             }
         }
 
         // End.
         Template.expand("</table>\n<!-- End TableSection {{@id}} -->\n",
                 properties, out);
     }
 
     /**
      * This method is invoked during the first phase of rendering a page;
      * it calls {@code cr.registerDataRequest} for each of the
      * DataRequests needed by this section to gather data to be displayed.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     @Override
     public void registerRequests(ClientRequest cr) {
         dataRequest = cr.registerDataRequest(properties, "request");
         if (dataRequest == null) {
             throw new Dataset.MissingValueError("request");
         }
     }
 
     /**
      * Generates the {@code <td>} for a table element, with a "left" or
      * "right" class to mark the elements on the ends of the row
      * @param column               Index of this column (0 refers to the
      *                             leftmost column).
      * @param out                  HTML is appended here.
      */
     protected void printTd(int column, StringBuilder out) {
         if (column == 0) {
             out.append("    <td class=\"left\">");
         } else if (column == (columns.length-1)) {
             out.append("    <td class=\"right\">");
         } else {
             out.append("    <td>");
         }
     }
 }
