 package gov.nih.nci.evs.reportwriter.utils;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.log4j.*;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team (David Yee)
  * @version 1.0
  */
 
 public class AsciiToHtmlFormatter extends BaseFileFormatter {
     private static Logger _logger = Logger
         .getLogger(AsciiToHtmlFormatter.class);
 
     public Boolean convert(String textfile, String delimiter) throws Exception {
         return convert2(textfile, "htm", delimiter);
     }
 
     public Boolean convert(String textfile, String delimiter, String outfile)
             throws Exception {
        BufferedReader br = getBufferReader(textfile);
         MyFileOutputStream out = new MyFileOutputStream(outfile);
        printHeader(out, outfile);
 
         // Prints topmost report banner:
         Vector<String> headings = getColumnHeadings(textfile, delimiter);
         out.writeln_indent("<tr><td colspan=\"" + (headings.size() + 1)
             + "\" class=\"dataTablePrimaryLabel\" height=\"20\">");
         out.writeln_inden1("Report: " + getReportName(out.getFilename()));
         out.writeln_undent("</td></tr>");
 
         // Prints table heading:
         headings.add(0, "#");
         out.writeln_indent("<tr class=\"dataTableHeader\">");
         for (String heading : headings)
             out.writeln_inden1("<th class=\"dataCellText\">" + heading
                 + "</th>");
         out.writeln_undent("</tr>");
 
         // Note: Special Case for CDISC STDM Terminology report.
         int extensible_col = findColumnIndicator(headings, "Extensible");
 
         // Prints contents:
         int row = 0;
         while (true) {
             String line = br.readLine();
             if (line == null)
                 break;
             if (line.length() <= 0)
                 continue;
 
             Vector<String> v = parseData(line, delimiter);
             v.add(0, Integer.toString(row)); // From adding # column
 
             String rowColor = row % 2 == 1 ? "dataRowDark" : "dataRowLight";
             out.writeln_indent("<tr class=\"" + rowColor + "\">");
             String bgColor = "";
             if (extensible_col >= 0) {
                 // Note: Special Case for CDISC STDM Terminology report.
                 String eValue = v.get(extensible_col - 1); // -1 from # column
                 if (eValue == null || eValue.trim().length() <= 0)
                     bgColor = " bgColor=\"skyblue\"";
             }
            
             for (int col = 0; col < v.size(); col++) {
                 if (row <= 0) // Skip heading row
                     continue;
 
                 String value = v.get(col);
                 if (value == null || value.trim().length() <= 0)
                     value = "&nbsp;";
                 else if (_ncitCodeColumns.contains(col - 1)) // -1 from # column
                     value = getNCItCodeUrl(value, false);
                 out.writeln_inden1("<td class=\"dataCellText\"" + bgColor + ">"
                     + value + "</td>");
             }
             out.writeln_undent("</tr>");
             row++;
         }

        printFooter(out);
         br.close();
        out.close();
        return Boolean.TRUE;
     }
 
    private void printHeader(MyFileOutputStream out, String filename)
            throws Exception {
         out.writeln_normal("<html>");
         out.writeln_indent("<head>");
         out.writeln_inden1("<title>" + getReportName(out.getFilename())
             + "</title>");
         printStyles(out);
         out.writeln_undent("</head>");
         out.writeln_indent("<body>");
         out.writeln_indent("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"dataTable\" width=\"100%\">");
     }
 
     private void printFooter(MyFileOutputStream out) throws Exception {
         out.writeln_undent("</table>");
         out.writeln_undent("</body>");
         out.writeln_normal("</html>");
     }
 
     private void printStyles(MyFileOutputStream out) throws Exception {
         out.writeln_indent("<style>");
         out.writeln_normal("  * {");
         out.writeln_normal("    font-family: Helvetica, Geneva, Times, Verdana, sans-serif;");
         out.writeln_normal("    font-size: 7pt;");
         out.writeln_normal("  }");
         out.writeln_normal("  .dataTablePrimaryLabel{ /* for the first row */");
         out.writeln_normal("    font-family:arial,helvetica,verdana,sans-serif;");
         out.writeln_normal("    font-size:0.9em;");
         out.writeln_normal("    font-weight:bold;");
         out.writeln_normal("    background-color:#5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    color:#FFFFFF; /* constant: white */");
         out.writeln_normal("    border-top:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    border-left:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    border-right:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    padding-left:0.4em;");
         out.writeln_normal("  }");
         out.writeln_normal("  .dataTable{ /* for the main table below the labels */");
         out.writeln_normal("    font-family:arial,helvetica,verdana,sans-serif;");
         out.writeln_normal("    font-size:0.9em;");
         out.writeln_normal("    color:#000000; /* constant: black */");
         out.writeln_normal("    border-top:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    border-left:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("  }");
         out.writeln_normal("  .dataTableHeader{ /* for the horizontal column headers */");
         out.writeln_normal("    font-family:arial,helvetica,verdana,sans-serif;");
         out.writeln_normal("    background-color:#CCCCCC; /* constant: medium gray */");
         out.writeln_normal("    color:#000000; /* constant: black */");
         out.writeln_normal("    font-weight:bold;");
         out.writeln_normal("    border-right:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    border-bottom:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("  }");
         out.writeln_normal("  .dataCellText{ /* for text output cells */");
         out.writeln_normal("    border-right:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    border-bottom:1px solid #5C5C5C; /* constant: dark gray */");
         out.writeln_normal("    text-align:left;");
         out.writeln_normal("  }");
         out.writeln_normal("  .dataRowLight{ /* for the light color of alternating rows */");
         out.writeln_normal("    background-color:#FFFFFF; /* constant: white */");
         out.writeln_normal("    color:#000000; /* constant: black */");
         out.writeln_normal("  }");
         out.writeln_normal("  .dataRowDark{ /* for the dark color of alternating rows */");
         out.writeln_normal("    background-color:#F4F4F5; /* constant: light gray */");
         out.writeln_normal("    color:#000000; /* constant: black */");
         out.writeln_normal("  }");
         out.writeln_undent("</style>");
     }
 
     private String getNCItCodeUrl(String code, boolean separateWindow) {
         String ncitCodeUrl = super.getNCItCodeUrl(code);
         StringBuffer buffer = new StringBuffer();
         buffer.append("<a href=\"" + ncitCodeUrl + "\"");
         if (separateWindow)
             buffer.append(" target=\"_blank\"");
         buffer.append(">");
         buffer.append(code);
         buffer.append("</a>");
         return buffer.toString();
     }
 
     private String getReportName(String filename) {
         String reportName = filename.replace("__", " (");
         reportName = reportName.replace(".htm", ")");
         return reportName;
     }
 
     public static void test(String textfile, int[] ncitCodeColumns) {
         try {
             String delimiter = "\t";
             AsciiToHtmlFormatter formatter = new AsciiToHtmlFormatter();
             formatter.setNcitUrl("http://ncit-dev.nci.nih.gov/ncitbrowser/");
             formatter.setNcitCodeColumns(ncitCodeColumns);
             formatter.convert(textfile, delimiter);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
         String dir = "C:/apps/evs/ncireportwriter-webapp/downloads/";
         test(dir + "CDISC_SDTM_Terminology__10.06e.txt", new int[] { 0, 1 });
         test(dir + "CDISC_Subset_REPORT__10.06e.txt", new int[] { 1, 3 });
         test(dir + "CDRH_Subset_REPORT__10.06e.txt", new int[] { 1, 3, 9 });
         test(dir + "FDA-SPL_Country_Code_REPORT__10.06e.txt", new int[] { 1 });
         test(dir + "FDA-UNII_Subset_REPORT__10.06e.txt", new int[] { 2 });
         test(dir + "Individual_Case_Safety_(ICS)_Subset_REPORT__10.06e.txt",
             new int[] { 1, 3 });
         test(dir + "Structured_Product_Labeling_(SPL)_REPORT__10.06e.txt",
             new int[] { 1, 3 });
         _logger.debug("Done");
     }
 }
