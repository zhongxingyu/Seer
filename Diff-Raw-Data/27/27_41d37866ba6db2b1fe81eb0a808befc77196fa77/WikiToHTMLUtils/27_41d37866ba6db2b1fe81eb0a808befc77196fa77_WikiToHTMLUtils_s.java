 /*
  * ConcourseConnect
  * Copyright 2009 Concursive Corporation
  * http://www.concursive.com
  *
  * This file is part of ConcourseConnect, an open source social business
  * software and community platform.
  *
  * Concursive ConcourseConnect is free software: you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, version 3 of the License.
  *
  * Under the terms of the GNU Affero General Public License you must release the
  * complete source code for any application that uses any part of ConcourseConnect
  * (system header files and libraries used by the operating system are excluded).
  * These terms must be included in any work that has ConcourseConnect components.
  * If you are developing and distributing open source applications under the
  * GNU Affero General Public License, then you are free to use ConcourseConnect
  * under the GNU Affero General Public License.
  *
  * If you are deploying a web site in which users interact with any portion of
  * ConcourseConnect over a network, the complete source code changes must be made
  * available.  For example, include a link to the source archive directly from
  * your web site.
  *
  * For OEMs, ISVs, SIs and VARs who distribute ConcourseConnect with their
  * products, and do not license and distribute their source code under the GNU
  * Affero General Public License, Concursive provides a flexible commercial
  * license.
  *
  * To anyone in doubt, we recommend the commercial license. Our commercial license
  * is competitively priced and will eliminate any confusion about how
  * ConcourseConnect can be used and distributed.
  *
  * ConcourseConnect is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with ConcourseConnect.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Attribution Notice: ConcourseConnect is an Original Work of software created
  * by Concursive Corporation
  */
 
 package com.concursive.connect.web.modules.wiki.utils;
 
 import com.concursive.commons.date.DateUtils;
 import com.concursive.commons.db.DatabaseUtils;
 import com.concursive.commons.phone.PhoneNumberBean;
 import com.concursive.commons.phone.PhoneNumberUtils;
 import com.concursive.commons.text.StringUtils;
 import com.concursive.connect.web.modules.login.dao.User;
 import com.concursive.connect.web.modules.login.utils.UserUtils;
 import com.concursive.connect.web.modules.profile.dao.Project;
 import com.concursive.connect.web.modules.profile.utils.ProjectUtils;
 import com.concursive.connect.web.modules.wiki.dao.*;
 import com.concursive.connect.web.utils.HtmlSelect;
 import com.concursive.connect.web.utils.HtmlSelectCurrencyCode;
 import com.concursive.connect.web.utils.LookupElement;
 import com.concursive.connect.web.utils.LookupList;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Class to manipulate wiki objects
  *
  * @author matt rajkowski
  * @version $Id$
  * @created February 7, 2006
  */
 public class WikiToHTMLUtils {
 
   private static Log LOG = LogFactory.getLog(WikiToHTMLUtils.class);
 
   public static String CRLF = "\n";
   public static String CONTENT_NEEDS_FORMATTING = "needs_formatting";
   public static String CONTENT_PREFORMATTED = "preformatted";
   public static String CONTENT_PRE = "pre-preformatted";
   public static String CONTENT_CODE = "code-preformatted";
 
   public static String getHTML(WikiToHTMLContext context, Connection db) {
     String content = context.getWiki().getContent();
     if (content == null) {
       return null;
     }
     // Chunk the content into manageable pieces
     LinkedHashMap<String, String> chunks = chunkContent(content, context.isEditMode());
     StringBuffer sb = new StringBuffer();
     for (String type : chunks.keySet()) {
       String chunk = chunks.get(type);
       LOG.trace("========= CHUNK [" + type + "] =========");
       if (type.endsWith(CONTENT_PREFORMATTED)) {
         if (!context.canAppend()) {
           continue;
         }
         LOG.trace(chunk);
         if (type.endsWith(CONTENT_CODE + "remove-test")) {
           sb.append("<code>");
         } else {
           sb.append("<pre>");
         }
         sb.append(chunk);
         if (type.endsWith(CONTENT_CODE + "remove-test")) {
           sb.append("</code>");
         } else {
           sb.append("</pre>");
         }
         sb.append(CRLF);
       } else if (type.endsWith(CONTENT_NEEDS_FORMATTING)) {
         String formatted = getHTML(context, db, chunk);
         LOG.trace(formatted);
         sb.append(formatted);
       }
     }
     return sb.toString();
 
     // Tables (With linewraps)
     // Header ||
     // Cell |
 
     // Ordered List
     // *
     // **
 
     // Unordered List
     // #
     // ##
 
     // Links
     // [[Wiki link]]
     // [[Wiki Link|Renamed]]
     // [[http://external]]
 
     // Images
     // [[Image:Filename.jpg]]
     // [[Image:Filename.jpg|A caption]]
     // [[Image:Filename.jpg|link=wiki]]
     // [[Image:Filename.jpg|thumb]]
     // [[Image:Filename.jpg|right]]
     // [[Image:Filename.jpg|left]]
 
     // Videos
     // [[Video:http://www.youtube.com/watch?v=3LkNlTNHZzE]]
     // [[Video:http://www.ustream.tv/flash/live/1/371673]]
     // [[Video:http://www.justin.tv/vasalini]]
     // [[Video:http://www.livestream.com/spaceflightnow]]
     // [[Video:http://qik.com/zeroio]]
 
     // Forms
     // [{form name="wikiForm"}]
     // ---
     // [{group value="New Group" display="false"}]
     // ---
     // [{label value="This is a text field" display="false"}]
     // [{field type="text" name="cf10" maxlength="30" size="30" value="" required="false"}]
     // [{description value="This is the text to display after"}]
     // +++
   }
 
   public static LinkedHashMap<String, String> chunkContent(String content, boolean editMode) {
     // Reduce the content into formatted and unformatted chunks...
     LinkedHashMap<String, String> chunks = new LinkedHashMap<String, String>();
     int preIndex = -1;
     int codeIndex = -1;
     int chunkCount = 0;
     while ((preIndex = content.indexOf("<pre>")) > -1 ||
         (codeIndex = content.indexOf("<code")) > -1) {
       int blockStart = -1;
       int startIndex = -1;
       int endIndex = -1;
       int blockEnd = -1;
       String type = CONTENT_PREFORMATTED;
       if (preIndex > -1 && codeIndex > -1) {
         if (preIndex < codeIndex) {
           blockStart = preIndex;
           startIndex = preIndex + 5;
           endIndex = content.indexOf("</pre>", startIndex);
           blockEnd = endIndex + 6;
           type = CONTENT_PRE;
         } else {
           blockStart = codeIndex;
           startIndex = codeIndex + 6;
           endIndex = content.indexOf("</code>", startIndex);
           blockEnd = endIndex + 7;
           type = CONTENT_CODE;
         }
       } else if (preIndex > -1) {
         blockStart = preIndex;
         startIndex = preIndex + 5;
         endIndex = content.indexOf("</pre>", startIndex);
         blockEnd = endIndex + 6;
         type = CONTENT_PRE;
       } else {
         blockStart = codeIndex;
         startIndex = codeIndex + 6;
         endIndex = content.indexOf("</code>", startIndex);
         blockEnd = endIndex + 7;
         type = CONTENT_CODE;
       }
 
       // Store non-preformatted chunks in a map for further processing
       if (blockStart > 0) {
         chunks.put(++chunkCount + CONTENT_NEEDS_FORMATTING, content.substring(0, blockStart));
       }
       // Process pre-formatted text here...
       String preText = content.substring(startIndex, endIndex);
       while (preText.startsWith(CRLF)) {
         preText = preText.substring(1);
       }
       while (preText.endsWith(CRLF)) {
         preText = preText.substring(0, preText.length() - 1);
       }
       String text = StringUtils.toHtmlValue(preText, false, false);
       text = processMarkupCharacters(text);
       if (editMode) {
         text = StringUtils.toBasicHtmlChars(text);
       }
       chunks.put(++chunkCount + type, text);
       if (blockEnd < content.length()) {
         content = content.substring(blockEnd);
       } else {
         content = "";
       }
     }
     // Store the rest of the content
     if (content.length() > 0) {
       chunks.put(++chunkCount + CONTENT_NEEDS_FORMATTING, content);
     }
     return chunks;
   }
 
   public static String getHTML(WikiToHTMLContext context, Connection db, String content) {
     return getHTML(context, db, content, false);
   }
 
   private static String getHTML(WikiToHTMLContext context, Connection db, String content, boolean inTable) {
     boolean inParagraph = false;
     boolean unorderedList = false;
     int unorderedIndent = 0;
     boolean orderedList = false;
     int orderedIndent = 0;
     boolean header = true;
 
     try {
       StringBuffer sb = new StringBuffer();
       BufferedReader in = new BufferedReader(new StringReader(content));
       String line = null;
 
       while ((line = in.readLine()) != null) {
 
         // Tables
         if (line.startsWith("|")) {
 
           if (orderedList) {
             for (int i = 0; i < orderedIndent; i++) {
               append(context, sb, "</ol>");
             }
             orderedList = false;
             orderedIndent = 0;
             append(context, sb, CRLF);
           }
           if (unorderedList) {
             for (int i = 0; i < unorderedIndent; i++) {
               append(context, sb, "</ul>");
             }
             unorderedList = false;
             unorderedIndent = 0;
             append(context, sb, CRLF);
           }
           if (inParagraph) {
             append(context, sb, "</p>");
             append(context, sb, CRLF);
             inParagraph = false;
           }
           // parseTable operates over all the lines that make up the table,
           // it will have to look forward so it returns an unparsed line
           line = parseTable(context, db, in, line, sb);
           if (line == null) {
             continue;
           }
         }
 
         // Forms
         if (line.startsWith("[{form")) {
           if (orderedList) {
             for (int i = 0; i < orderedIndent; i++) {
               append(context, sb, "</ol>");
             }
             orderedList = false;
             orderedIndent = 0;
             append(context, sb, CRLF);
           }
           if (unorderedList) {
             for (int i = 0; i < unorderedIndent; i++) {
               append(context, sb, "</ul>");
             }
             unorderedList = false;
             unorderedIndent = 0;
             append(context, sb, CRLF);
           }
           if (inParagraph) {
             append(context, sb, "</p>");
             append(context, sb, CRLF);
             inParagraph = false;
           }
           // parseForm operates over all the lines that make up the form,
           // it will have to look forward so it returns an unparsed line
           parseForm(context, db, in, line, sb);
           continue;
         }
 
         // Section
         if (line.startsWith("=") && line.endsWith("=")) {
           if (orderedList) {
             for (int i = 0; i < orderedIndent; i++) {
               append(context, sb, "</ol>");
             }
             orderedList = false;
             orderedIndent = 0;
             append(context, sb, CRLF);
           }
           if (unorderedList) {
             for (int i = 0; i < unorderedIndent; i++) {
               append(context, sb, "</ul>");
             }
             unorderedList = false;
             unorderedIndent = 0;
             append(context, sb, CRLF);
           }
           if (inParagraph) {
             append(context, sb, "</p>");
             append(context, sb, CRLF);
             inParagraph = false;
           }
           int hCount = parseHCount(line, "=");
           if (hCount > 6) {
             hCount = 6;
           }
           String section = line.substring(line.indexOf("=") + hCount, line.lastIndexOf("=") - hCount + 1);
           header = true;
           context.foundHeader(hCount);
           String headerAnchor = null;
           // Store the h2's with anchors
           if (hCount == 2) {
             headerAnchor = StringUtils.toHtmlValue(section).replace(" ", "_");
             context.getHeaderAnchors().put(headerAnchor, section);
           }
           append(context, sb, "<h" + hCount + (headerAnchor != null ? " id=\"" + headerAnchor + "\"" : "") + ">");
           if (context.canAppend()) {
             if (!context.isEditMode() && context.getShowEditSection() && !inTable) {
               if (hasUserProjectAccess(db, context.getUserId(), context.getProjectId(), "wiki", "add")) {
                 sb.append("<span class=\"editsection\"><a href=\"" + context.getServerUrl() + "/modify/" + context.getProject().getUniqueId() + "/wiki" + (StringUtils.hasText(context.getWiki().getSubject()) ? "/" + context.getWiki().getSubjectLink() : "") + "?section=" + context.getSectionIdCount() + "\">edit</a></span>");
               }
             }
           }
 
           if (context.isEditMode()) {
             append(context, sb, "<span>");
           }
           append(context, sb, StringUtils.toHtml(section));
           if (context.isEditMode()) {
             append(context, sb, "</span>");
           }
           append(context, sb, "</h" + hCount + ">");
           append(context, sb, CRLF);
           continue;
         }
         if (header) {
           header = false;
           if (line.trim().equals("")) {
             // remove the extra space a user may leave after a header
             continue;
           }
         }
 
         // Determine if this is a bulleted list
         if (line.startsWith("*")) {
           int hCount = parseHCount(line, "*");
           if (!unorderedList) {
             unorderedList = true;
           }
           if (hCount != unorderedIndent) {
             if (hCount > unorderedIndent) {
               append(context, sb, "<ul>");
             } else {
               for (int i = hCount; i < unorderedIndent; i++) {
                 append(context, sb, "</ul>");
               }
             }
             unorderedIndent = hCount;
           }
           append(context, sb, "<li>");
           parseLine(context, db, line.substring(hCount).trim(), sb);
           append(context, sb, "</li>");
           append(context, sb, CRLF);
           continue;
         } else {
           if (unorderedList) {
             for (int i = 0; i < unorderedIndent; i++) {
               append(context, sb, "</ul>");
             }
             unorderedList = false;
             unorderedIndent = 0;
             append(context, sb, CRLF);
           }
         }
 
         // Determine if this is a numbered list
         if (line.startsWith("#")) {
           int hCount = parseHCount(line, "#");
           if (!orderedList) {
             orderedList = true;
           }
           if (hCount != orderedIndent) {
             if (hCount > orderedIndent) {
               append(context, sb, "<ol>");
             } else {
               for (int i = hCount; i < orderedIndent; i++) {
                 append(context, sb, "</ol>");
               }
             }
             orderedIndent = hCount;
           }
           append(context, sb, "<li>");
           parseLine(context, db, line.substring(hCount).trim(), sb);
           append(context, sb, "</li>");
           append(context, sb, CRLF);
           continue;
         } else {
           if (orderedList) {
             for (int i = 0; i < orderedIndent; i++) {
               append(context, sb, "</ol>");
             }
             orderedList = false;
             orderedIndent = 0;
             append(context, sb, CRLF);
           }
         }
 
         if (inParagraph && line.length() != 0) {
           append(context, sb, "<br />");
         }
 
         // Parse the line
         if (!inParagraph && line.length() > 0) {
           append(context, sb, "<p>");
           inParagraph = true;
         }
         if (line.length() == 0) {
           if (inParagraph) {
             append(context, sb, "</p>");
             append(context, sb, CRLF);
             inParagraph = false;
           }
         } else {
           boolean hasReturn = parseLine(context, db, line, sb);
           if (hasReturn) {
             //append(context, sb, "<br />");
           } else {
             append(context, sb, CRLF);
           }
         }
       }
       // Cleanup now that the lines are finished
       if (orderedList) {
         for (int i = 0; i < orderedIndent; i++) {
           append(context, sb, "</ol>");
         }
         append(context, sb, CRLF);
       }
       if (unorderedList) {
         for (int i = 0; i < unorderedIndent; i++) {
           append(context, sb, "</ul>");
         }
         append(context, sb, CRLF);
       }
       if (inParagraph) {
         append(context, sb, "</p>");
         append(context, sb, CRLF);
       }
       in.close();
       return sb.toString();
     } catch (Exception e) {
       LOG.error(e);
     }
     LOG.warn("Could not convert wiki markup");
     return "Content cannot be displayed due to a parsing error, use markup mode to find the error";
   }
 
   protected static int parseHCount(String line, String id) {
     int count = 0;
     for (int i = 0; i < line.length(); i++) {
       char c = line.charAt(i);
       if (id.equals(String.valueOf(c))) {
         ++count;
       } else {
         return count;
       }
     }
     return 1;
   }
 
   protected static String parseTable(WikiToHTMLContext context, Connection db, BufferedReader in, String line, StringBuffer sb) throws Exception {
     if (line == null) {
       return line;
     }
     // Implement tables as...
     // ||header col1||header col2
     // !continued||
     // |colA1|colA2
     // !continued
     // !continued|
     // |colB1|colB2 [[Security, Registration, Invitation|Installation Options]]|
     append(context, sb, "<table class=\"wikiTable\">");
     append(context, sb, CRLF);
     int rowHighlight = 0;
     int rowCount = 0;
 
     // Keep track of the table's custom styles
     HashMap<Integer, String> cStyle = new HashMap<Integer, String>();
 
     while (line != null && (line.startsWith("|") || line.startsWith("!"))) {
 
       // Build a complete line
       String lineToParse = line;
       while (!line.endsWith("|")) {
         line = in.readLine();
         if (line == null) {
           // there is an error in the line to process
           return null;
         }
         if (line.startsWith("!")) {
           lineToParse += CRLF + line.substring(1);
         }
       }
       line = lineToParse;
 
       // Determine if the row can output
       boolean canOutput = true;
 
       // Track the row being processed
       ++rowCount;
 
       String cellType = null;
       Scanner sc = null;
       if (line.startsWith("||") && line.endsWith("||")) {
         cellType = "th";
         sc = new Scanner(line).useDelimiter("[|][|]");
 //        sc = new Scanner(line.substring(2, line.length() - 2)).useDelimiter("[|][|]");
       } else if (line.startsWith("|")) {
         cellType = "td";
         sc = new Scanner(line.substring(1, line.length() - 1)).useDelimiter("\\|(?=[^\\]]*(?:\\[|$))");
       }
 
       if (sc != null) {
 
         // Determine the column span
         int colSpan = 1;
         // Determine the cell being output
         int cellCount = 0;
 
         while (sc.hasNext()) {
           String cellData = sc.next();
           if (cellData.length() == 0) {
             ++colSpan;
             continue;
           }
 
           // Track the cell count being output
           ++cellCount;
 
           if (rowCount == 1) {
             // Parse and validate the style input
             LOG.debug("Checking style value: " + cellData);
             if (cellData.startsWith("{") && cellData.endsWith("}")) {
               String[] style = cellData.substring(1, cellData.length() - 1).split(":");
               String attribute = style[0].trim();
               String value = style[1].trim();
               if ("width".equals(attribute)) {
                 // Validate the width style
                 if (StringUtils.hasAllowedOnly("0123456789%.", value)) {
                   cStyle.put(cellCount, attribute + ": " + value + ";");
                 }
               } else {
                 LOG.debug("Unsupported style: " + cellData);
               }
               canOutput = false;
             }
           }
 
           // Output the header
           if (canOutput) {
 
             if (cellCount == 1) {
               // This is the table header row
               if ("td".equals(cellType)) {
                 rowHighlight = (rowHighlight != 1 ? 1 : 2);
                 append(context, sb, "<tr class=\"row" + rowHighlight + "\">");
               } else {
                 append(context, sb, "<tr>");
               }
             }
 
             // Start the cell output
             append(context, sb, "<" + cellType);
             // Output the colspan
             if (colSpan > 1) {
               append(context, sb, " colspan=\"" + colSpan + "\"");
             }
             // Output any style
             if (cStyle.size() > 0 && rowCount == 2) {
               String style = cStyle.get(cellCount);
               if (style != null) {
                 append(context, sb, " style=\"" + style + "\"");
               }
             }
             // End the cell output
             append(context, sb, ">");
 
             // Output the data
             if (" ".equals(cellData) || "¬†".equals(cellData)) {
               // Put a blank space in blank cells for output consistency
               append(context, sb, "&nbsp;");
             } else {
               // Output the cell as a complete wiki
               String formatted = getHTML(context, db, cellData, true).trim();
               LOG.trace(formatted);
               sb.append(formatted);
             }
 
             // Close the cell
             append(context, sb, "</" + cellType + ">");
           }
         }
         if (canOutput) {
           append(context, sb, "</tr>");
           append(context, sb, CRLF);
         }
 
       }
       // read another line to see if it's part of the table
       line = in.readLine();
     }
     append(context, sb, "</table>");
     append(context, sb, CRLF);
     return line;
   }
 
   public static CustomForm retrieveForm(BufferedReader in, String line) throws Exception {
     // Forms
     // [{form name="wikiForm"}]
     // ---
     // [{group value="New Group" display="false"}]
     // ---
     // [{label value="This is a text field" display="false"}]
     // [{field type="text" name="cf10" maxlength="30" size="30" value="" required="false"}]
     // [{description value="This is the text to display after"}]
     // xxx
 
     // Convert wiki to objects...
     CustomForm form = new CustomForm();
     form.setName(extractValue("name", line));
     LOG.debug("Form name: " + form.getName());
     CustomFormGroup currentGroup = null;
     while (line != null && !line.startsWith("+++") && (line = in.readLine()) != null) {
       if (line.startsWith("[{group")) {
         LOG.debug("found group");
         // Process the line as a group
         currentGroup = new CustomFormGroup();
         currentGroup.setName(extractValue("value", line));
         currentGroup.setDisplay(extractValue("display", line));
         form.add(currentGroup);
       } else if (line.startsWith("[{label")) {
         LOG.debug("found a field label");
         // Process this block as a field
         CustomFormField field = new CustomFormField();
         field.setLabel(extractValue("value", line));
         field.setLabelDisplay(extractValue("display", line));
         while (!line.startsWith("---") && !line.startsWith("___") && !line.startsWith("+++") && (line = in.readLine()) != null) {
           if (line.startsWith("[{field")) {
             LOG.debug("  found field");
             field.setType(extractValue("type", line));
             field.setName(extractValue("name", line));
             field.setRequired(extractValue("required", line));
             field.setDefaultValue(extractValue("value", line));
             field.setSize(extractValue("size", line));
             field.setMaxLength(extractValue("maxlength", line));
             field.setColumns(extractValue("cols", line));
             field.setRows(extractValue("rows", line));
             field.setOptions(extractValue("options", line));
           } else if (line.startsWith("[{description")) {
             LOG.debug("  found description");
             field.setAdditionalText(extractValue("value", line));
           } else if (line.startsWith("[{entry")) {
             LOG.debug("  found entry");
             field.setValue(extractValue("value", line));
             field.setValueCurrency(extractValue("currency", line));
           }
         }
         if (currentGroup == null) {
           currentGroup = new CustomFormGroup();
           currentGroup.setDisplay(false);
         }
         currentGroup.add(field);
       }
     }
     return form;
   }
 
   protected static String parseForm(WikiToHTMLContext context, Connection db, BufferedReader in, String line, StringBuffer sb) throws Exception {
     if (line == null) {
       return line;
     }
     CustomForm form = retrieveForm(in, line);
     context.foundForm(form);
     if (context.canAppend()) {
       // Output the form based on editmode
       if (context.isEditMode()) {
         LOG.debug("parseForm: editMode");
         // Construct an HTML form for filling out
         int groupCount = 0;
         for (CustomFormGroup group : form) {
           ++groupCount;
           if (groupCount > 1) {
             sb.append("<br />");
           }
           sb.append("<table cellpadding=\"4\" cellspacing=\"0\" width=\"100%\" class=\"pagedList\">");
           if (StringUtils.hasText(group.getName())) {
             sb.append("<tr><th colspan=\"2\">").append(StringUtils.toHtml(group.getName())).append("</th></tr>");
           }
           for (CustomFormField field : group) {
             // Determine if there is a property supplied for this field's value
             if (context.getFormPropertyMap() != null) {
               Map<String, String> propertyMap = context.getFormPropertyMap();
               LOG.debug("Looking up propertyMap for name: " + field.getName());
               if (propertyMap.containsKey(field.getName())) {
                 String thisValue = propertyMap.get(field.getName());
                 if (StringUtils.hasText(thisValue)) {
                   LOG.debug("  Setting value: " + thisValue);
                   field.setValue(thisValue);
                 }
               }
             }
             sb.append("<tr class=\"containerBody\">" +
                 "<td valign=\"top\" class=\"formLabel\">").append(StringUtils.toHtml(field.getLabel())).append("</td>" +
                 "<td valign=\"top\">").append(toHtmlFormField(field, context));
             if (StringUtils.hasText(field.getAdditionalText())) {
               sb.append("&nbsp;").append(StringUtils.toHtml(field.getAdditionalText()));
             }
             sb.append("</td></tr>");
           }
           sb.append("</table>");
         }
         sb.append(CRLF);
       } else {
         LOG.debug("parseForm: displayMode");
         // Construct HTML output for viewing the form data
         LOG.debug("constructing html output...");
         boolean dataOutput = false;
         sb.append("<div class=\"infobox\">");
         sb.append("<table class=\"pagedList\">");
         for (CustomFormGroup group : form) {
           LOG.debug(" group...");
           if (group.getDisplay() && StringUtils.hasText(group.getName())) {
             if (!dataOutput) {
               dataOutput = true;
             }
             sb.append("<tr><th colspan=\"2\">").append(StringUtils.toHtml(group.getName())).append("</th></tr>");
           }
           for (CustomFormField field : group) {
             LOG.debug("  field...");
             if (field.hasValue()) {
               if (!dataOutput) {
                 dataOutput = true;
               }
               sb.append("<tr class=\"containerBody\">");
               if (field.getLabelDisplay()) {
                 LOG.debug("   output w/label");
                 sb.append("<td class=\"formLabel\">").append(StringUtils.toHtml(field.getLabel())).append("</td>");
                 sb.append("<td>");
                 sb.append(toHtml(field, context.getWiki(), context.getServerUrl()));
                 sb.append("</td>");
               } else {
                 LOG.debug("   output");
                 sb.append("<td colspan=\"2\">");
                 sb.append("<center>");
                 sb.append(toHtml(field, context.getWiki(), context.getServerUrl()));
                 sb.append("</center>");
                 sb.append("</td>");
               }
               sb.append("</tr>");
             }
           }
         }
         // Show the group names to the user if there are no fields to show
         if (!dataOutput) {
           LOG.debug("!dataOutput");
           sb.append("<tr><td colspan=\"2\" align=\"center\">");
           int count = 0;
           for (CustomFormGroup group : form) {
             ++count;
             if (count > 1) {
               sb.append("<br />");
             }
             sb.append(StringUtils.toHtml(group.getName()));
           }
           sb.append("</td></tr>");
         }
         LOG.debug("Check permissions");
         if (context.getProject() != null && hasUserProjectAccess(db, context.getUserId(), context.getProject().getId(), "wiki", "add")) {
           sb.append("<tr><td colspan=\"2\" align=\"center\">");
           sb.append("<a href=\"" + context.getServerUrl() + "/modify/" + context.getProject().getUniqueId() + "/wiki" + (StringUtils.hasText(context.getWiki().getSubject()) ? "/" + context.getWiki().getSubjectLink() : "") + "?form=1\">Fill out this form</a>");
           sb.append("</td></tr>");
         }
         sb.append("</table>");
         sb.append("</div>");
       }
     }
     LOG.debug("finished with form.");
     context.foundFormEnd();
     return null;
   }
 
   protected static boolean parseLine(WikiToHTMLContext context, Connection db, String line, StringBuffer main) throws Exception {
     if (!context.canAppend()) {
       return false;
     }
     boolean needsCRLF = true;
     boolean bold = false;
     boolean italic = false;
     boolean bolditalic = false;
     boolean underline = false;
     StringBuffer subject = new StringBuffer();
     StringBuffer sb = new StringBuffer();
     StringBuffer data = new StringBuffer();
     int linkL = 0;
     int linkR = 0;
     int attr = 0;
     int underlineAttr = 0;
 
     // parse characters
     for (int i = 0; i < line.length(); i++) {
       char c1 = line.charAt(i);
       String c = String.valueOf(c1);
       // False attr/links
       if (!"'".equals(c) && attr == 1) {
         data.append("'").append(c);
         attr = 0;
         continue;
       }
       if (!"_".equals(c) && underlineAttr == 1) {
         data.append("_").append(c);
         underlineAttr = 0;
         continue;
       }
       if (!"[".equals(c) && linkL == 1) {
         data.append("[").append(c);
         linkL = 0;
         continue;
       }
       if (!"]".equals(c) && linkR == 1) {
         data.append("]").append(c);
         linkR = 0;
         continue;
       }
       // Links
       if ("[".equals(c)) {
         ++linkL;
         continue;
       }
       if ("]".equals(c)) {
         ++linkR;
         if (linkL == 2 && linkR == 2) {
           flushData(data, sb);
           // Different type of links...
           String link = subject.toString();
 
           if (link.startsWith("Image:") || link.startsWith("image:")) {
             // Image link
             WikiImageLink wikiImageLink = new WikiImageLink(link, context.getProjectId(), context.getImageList(), (i + 1 == line.length()), context.isEditMode(), context.getServerUrl());
             sb.append(wikiImageLink.getValue());
             needsCRLF = wikiImageLink.getNeedsCRLF();
           } else if (link.startsWith("Video:") || link.startsWith("video:")) {
             // Video link
             WikiVideoLink wikiVideoLink = new WikiVideoLink(link, context.isEditMode(), context.getServerUrl());
             sb.append(wikiVideoLink.getValue());
             needsCRLF = wikiVideoLink.getNeedsCRLF();
           } else {
             // Any other kind of link
             // Parser for inter-project wiki links
             WikiLink wikiLink = new WikiLink(link, context.getProjectId());
             // Place a wiki link
             String cssClass = "wikiLink";
             String url = null;
             if (WikiLink.REFERENCE.equals(wikiLink.getStatus())) {
               sb.append("<a class=\"wikiLink external\" target=\"_blank\" href=\"" + wikiLink.getEntity() + "\">" + StringUtils.toHtml(wikiLink.getName()) + "</a>");
             } else {
               LOG.debug("Found wiki link: " + wikiLink.toString());
               Project thisProject = null;
               if (wikiLink.getProjectId() > -1) {
                 LOG.debug("Loading wiki link project: " + wikiLink.getProjectId());
                 thisProject = ProjectUtils.loadProject(wikiLink.getProjectId());
               } else {
                 thisProject = new Project();
               }
               // Links...
               if ("profile".equalsIgnoreCase(wikiLink.getArea())) {
                 // Links to a profile page
                 cssClass = "wikiLink external";
                 url = context.getServerUrl() + "/show/" + thisProject.getUniqueId();
               } else if ("badge".equalsIgnoreCase(wikiLink.getArea())) {
                 // Links to a badge
                 cssClass = "wikiLink external";
                 url = context.getServerUrl() + "/badge/" + wikiLink.getEntityId();
               } else if ("wiki".equalsIgnoreCase(wikiLink.getArea())) {
                 // Links to another wiki page
                 if (StringUtils.hasText(wikiLink.getEntity())) {
                   url = context.getServerUrl() + "/show/" + thisProject.getUniqueId() + "/wiki/" + wikiLink.getEntityTitle();
                 } else {
                   url = context.getServerUrl() + "/show/" + thisProject.getUniqueId() + "/wiki";
                 }
                 // Check to see if this is an external wiki
                 if (context.getProjectId() > -1 && context.getProjectId() != thisProject.getId()) {
                   cssClass = "wikiLink external";
                 }
                 // Check to see if the target wiki exists to draw the wiki entry differently
                 if (!WikiList.checkExistsBySubject(db, wikiLink.getEntity(), wikiLink.getProjectId())) {
                   cssClass = "wikiLink newWiki";
                   // If user has access to edit, then use an edit link
                   if (hasUserProjectAccess(db, context.getUserId(), wikiLink.getProjectId(), wikiLink.getArea(), "edit")) {
                     String wikiSubject = StringUtils.hasText(wikiLink.getEntity()) ? "/" + wikiLink.getEntityTitle() : "";
                     url = context.getServerUrl() + "/modify/" + thisProject.getUniqueId() + "/wiki" + wikiSubject;
                   }
                 }
               } else {
                 cssClass = "wikiLink external";
                 url = context.getServerUrl() + "/show/" + thisProject.getUniqueId() + "/" + wikiLink.getArea().toLowerCase() + (StringUtils.hasText(wikiLink.getEntity()) ? "/" + wikiLink.getEntityId() : "");
               }
               // Display the resulting URL
               if (wikiLink.getProjectId() == -1 ||
                   wikiLink.getProjectId() == context.getProjectId() ||
                   hasUserProjectAccess(db, context.getUserId(), wikiLink.getProjectId(), wikiLink.getPermissionArea(), "view")) {
                 String rel = "";
                 if ("app".equals(wikiLink.getArea())) {
                   // open apps in a panel
                   rel = " rel=\"shadowbox\"";
                 }
                 sb.append("<a class=\"" + cssClass + "\" href=\"" + url + "\"" + rel + ">" + StringUtils.toHtml(wikiLink.getName().trim()) + "</a>");
                 if (wikiLink.getName().endsWith(" ")) {
                   sb.append(" ");
                 }
               } else {
                 LOG.debug("USING DENIED LINK");
                 cssClass = "wikiLink denied";
                 sb.append("<a class=\"" + cssClass + "\" href=\"#\" onmouseover=\"window.status='" + StringUtils.jsStringEscape(url) + ";'\">" + StringUtils.toHtml(wikiLink.getName()) + "</a>");
               }
             }
           }
           subject.setLength(0);
           linkL = 0;
           linkR = 0;
         }
         continue;
       }
       if (!"[".equals(c) && linkL == 2 && !"]".equals(c)) {
         subject.append(c);
         continue;
       }
       // Attribute properties
       // TODO: Handle when there are more than 5 '''''
       if ("'".equals(c)) {
         ++attr;
         continue;
       }
       if ("_".equals(c)) {
         ++underlineAttr;
         continue;
       }
       if (!"_".equals(c) && underlineAttr == 2) {
         if (!underline) {
           flushData(data, sb);
           sb.append("<span style=\"text-decoration: underline;\">");
           data.append(c);
           underline = true;
         } else {
           flushData(data, sb);
           sb.append("</span>");
           data.append(c);
           underline = false;
         }
         underlineAttr = 0;
         continue;
 
       }
       if (!"'".equals(c) && attr > 1) {
         if (attr == 2) {
           if (!italic) {
             flushData(data, sb);
             sb.append("<em>");
             data.append(c);
             italic = true;
           } else {
             flushData(data, sb);
             sb.append("</em>");
             data.append(c);
             italic = false;
           }
           attr = 0;
           continue;
         }
         if (attr == 3) {
           if (!bold) {
             flushData(data, sb);
             sb.append("<strong>");
             data.append(c);
             bold = true;
           } else {
             flushData(data, sb);
             sb.append("</strong>");
             data.append(c);
             bold = false;
           }
           attr = 0;
           continue;
         }
         if (attr >= 5) {
           if (!bolditalic) {
             flushData(data, sb);
             sb.append("<strong><em>");
             data.append(c);
             bolditalic = true;
           } else {
             flushData(data, sb);
             sb.append("</em></strong>");
             data.append(c);
             bolditalic = false;
           }
           attr = attr - 5;
           // TODO: if attr > 0 then need to set bold/itals cout
           continue;
         }
       }
       data.append(c);
     }
     for (int x = 0; x < linkR; x++) {
       data.append("]");
     }
     for (int x = 0; x < linkL; x++) {
       data.append("[");
     }
     if (attr == 1) {
       data.append("'");
     }
     if (underlineAttr == 1) {
       data.append("_");
     }
     flushData(data, sb);
     if (italic) {
       sb.append("</em>");
     }
     if (underline) {
       sb.append("</span>");
     }
     if (bold) {
       sb.append("</strong>");
     }
     if (bolditalic) {
       sb.append("</em></strong>");
     }
     String newLine = sb.toString();
 
     // handle strikethrough
     newLine = StringUtils.replace(newLine, StringUtils.toHtmlValue("<s>"), "<span style=\"text-decoration: line-through;\">");
     newLine = StringUtils.replace(newLine, StringUtils.toHtmlValue("</s>"), "</span>");
     newLine = StringUtils.replace(newLine, "\n", "<br />");
     if (" ".equals(newLine)) {
       newLine = "&nbsp;";
     }
     main.append(newLine);
     return needsCRLF;
   }
 
   protected static void append(WikiToHTMLContext context, StringBuffer sb, String content) {
     if (context.canAppend()) {
       String value = content;
       value = processMarkupCharacters(value);
       sb.append(value);
     }
   }
 
   protected static void flushData(StringBuffer data, StringBuffer sb) {
     if (data.length() > 0) {
       String value = data.toString();
       value = processMarkupCharacters(value);
       sb.append(StringUtils.toHtmlValue(value, false, false));
       data.setLength(0);
     }
   }
 
   private static boolean hasUserProjectAccess(Connection db, int userId, int projectId, String section, String permissionAction) throws SQLException {
     if (db == null) {
       LOG.error("hasUserProjectAccess: failed - database is null");
       return false;
     }
     // Load the user (will not be found if a guest)
     User thisUser = null;
     try {
       thisUser = UserUtils.loadUser(userId);
     } catch (Exception notAUser) {
       LOG.debug("hasUserProjectAccess: userId not found - " + userId + " - using a guest user");
       thisUser = UserUtils.createGuestUser();
     }
     // Check access
     String permission = "project-" + section.toLowerCase(Locale.ENGLISH) + "-" + permissionAction;
     return ProjectUtils.hasAccess(projectId, thisUser, permission);
   }
 
   public static String extractValue(String param, String line) {
     int index = line.indexOf(" " + param + "=\"");
     if (index == -1) {
       return null;
     }
     int start = index + 1 + param.length() + 2;
     int end = line.indexOf("\"", start);
     if (end == -1) {
       return null;
     }
     return line.substring(start, end);
   }
 
   public static String toHtmlFormField(CustomFormField field, WikiToHTMLContext context) {
     // Set a default value
     if (field.getValue() == null) {
       field.setValue(field.getDefaultValue());
     }
     // Protect against any arbitrary input
     String fieldName = StringUtils.toHtmlValue(field.getName());
     // Return output based on type
     switch (field.getType()) {
       case CustomFormField.TEXTAREA:
         String textAreaValue = StringUtils.replace(field.getValue(), "^", CRLF);
         return ("<textarea cols=\"" + field.getColumns() + "\" rows=\"" + field.getRows() + "\" name=\"" + fieldName + "\">" + StringUtils.toString(textAreaValue) + "</textarea>");
       case CustomFormField.SELECT:
         LookupList lookupList = field.getLookupList();
         int selectedItemId = -1;
         for (LookupElement thisElement : lookupList) {
           if (field.getValue().equals(thisElement.getDescription())) {
             selectedItemId = thisElement.getCode();
           }
         }
         return lookupList.getHtmlSelect(fieldName, selectedItemId);
       case CustomFormField.CHECKBOX:
         return ("<input type=\"checkbox\" name=\"" + fieldName + "\" value=\"ON\" " + ("true".equals(field.getValue()) ? "checked" : "") + ">");
       case CustomFormField.CALENDAR:
         String calendarValue = field.getValue();
         if (StringUtils.hasText(calendarValue)) {
           try {
             String convertedDate = DateUtils.getUserToServerDateTimeString(null, DateFormat.SHORT, DateFormat.LONG, field.getValue());
             Timestamp timestamp = DatabaseUtils.parseTimestamp(convertedDate);
             Locale locale = Locale.getDefault();
             int dateFormat = DateFormat.SHORT;
             SimpleDateFormat dateFormatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance(dateFormat, locale);
             calendarValue = dateFormatter.format(timestamp);
           } catch (Exception e) {
             LOG.error(e);
           }
         }
         // Output with a calendar control
         String language = System.getProperty("LANGUAGE");
         String country = System.getProperty("COUNTRY");
         return ("<input type=\"text\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" size=\"10\" value=\"" + StringUtils.toHtmlValue(calendarValue) + "\" > " +
             "<a href=\"javascript:popCalendar('inputForm', '" + fieldName + "','" + language + "','" + country + "');\">" + "<img src=\"" + context.getServerUrl() + "/images/icons/stock_form-date-field-16.gif\" " + "border=\"0\" align=\"absmiddle\" height=\"16\" width=\"16\"/></a>");
       case CustomFormField.PERCENT:
         return ("<input type=\"text\" name=\"" + fieldName + "\" size=\"5\" value=\"" + StringUtils.toHtmlValue(field.getValue()) + "\"> " + "%");
       case CustomFormField.INTEGER:
         // Determine the value to display in the field
         String integerValue = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(integerValue)) {
           try {
             NumberFormat formatter = NumberFormat.getInstance();
             integerValue = formatter.format(StringUtils.getIntegerNumber(field.getValue()));
           } catch (Exception e) {
             LOG.warn("Could not format integer: " + field.getValue());
           }
         }
         return ("<input type=\"text\" name=\"" + fieldName + "\" size=\"8\" value=\"" + integerValue + "\"> ");
       case CustomFormField.FLOAT:
         // Determine the value to display in the field
         String decimalValue = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(decimalValue)) {
           try {
             NumberFormat formatter = NumberFormat.getInstance();
             decimalValue = formatter.format(StringUtils.getDoubleNumber(field.getValue()));
           } catch (Exception e) {
             LOG.warn("Could not decimal format: " + field.getValue());
           }
         }
         return ("<input type=\"text\" name=\"" + fieldName + "\" size=\"8\" value=\"" + decimalValue + "\"> ");
       case CustomFormField.CURRENCY:
         // Use a currencyCode for formatting
         String currencyCode = field.getValueCurrency();
         if (currencyCode == null) {
           currencyCode = field.getCurrency();
         }
         if (!StringUtils.hasText(currencyCode)) {
           currencyCode = "USD";
         }
         HtmlSelect currencyCodeList = HtmlSelectCurrencyCode.getSelect(fieldName + "Currency", currencyCode);
         // Determine the valut to display in the field
         String currencyValue = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(currencyValue)) {
           try {
             NumberFormat formatter = NumberFormat.getNumberInstance();
             formatter.setMaximumFractionDigits(2);
             currencyValue = formatter.format(StringUtils.getDoubleNumber(field.getValue()));
           } catch (Exception e) {
             LOG.warn("Could not currencyCode format: " + field.getValue());
           }
         }
         return (currencyCodeList.getHtml() + "<input type=\"text\" name=\"" + fieldName + "\" size=\"8\" value=\"" + currencyValue + "\"> ");
       case CustomFormField.EMAIL:
         return ("<input type=\"text\" " + "name=\"" + fieldName + "\" maxlength=\"255\" size=\"40\" value=\"" + StringUtils.toHtmlValue(field.getValue()) + "\" />");
       case CustomFormField.PHONE:
         return ("<input type=\"text\" " + "name=\"" + fieldName + "\" maxlength=\"60\" size=\"20\" value=\"" + StringUtils.toHtmlValue(field.getValue()) + "\" />");
       case CustomFormField.URL:
         String value = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(value)) {
           if (!value.contains("://")) {
             value = "http://" + field.getValue();
           }
         }
         return ("<input type=\"text\" " + "name=\"" + fieldName + "\" maxlength=\"255\" size=\"40\" value=\"" + StringUtils.toHtmlValue(value) + "\" />");
       default:
         int maxlength = field.getMaxLength();
         int size = -1;
         if (maxlength > -1) {
           if (maxlength > 40) {
             size = 40;
           } else {
             size = maxlength;
           }
         }
         return ("<input type=\"text\" " + "name=\"" + fieldName + "\" " + (maxlength == -1 ? "" : "maxlength=\"" + maxlength + "\" ") + (size == -1 ? "" : "size=\"" + size + "\" ") + "value=\"" + StringUtils.toHtmlValue(field.getValue()) + "\" />");
     }
   }
 
   public static String toHtml(CustomFormField field, Wiki wiki, String contextPath) {
     // Return output based on type
     switch (field.getType()) {
       case CustomFormField.TEXTAREA:
         String textAreaValue = StringUtils.replace(field.getValue(), "^", CRLF);
         return StringUtils.toHtml(textAreaValue);
       case CustomFormField.SELECT:
         return StringUtils.toHtml(field.getValue());
       case CustomFormField.CHECKBOX:
         if ("true".equals(field.getValue())) {
           return "Yes";
         } else {
           return "No";
         }
       case CustomFormField.CALENDAR:
         String calendarValue = field.getValue();
         if (StringUtils.hasText(calendarValue)) {
           try {
             String convertedDate = DateUtils.getUserToServerDateTimeString(null, DateFormat.SHORT, DateFormat.LONG, field.getValue());
             Timestamp timestamp = DatabaseUtils.parseTimestamp(convertedDate);
             Locale locale = Locale.getDefault();
             int dateFormat = DateFormat.SHORT;
             SimpleDateFormat dateFormatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance(dateFormat, locale);
             calendarValue = dateFormatter.format(timestamp);
           } catch (Exception e) {
             LOG.error(e);
           }
         }
         return StringUtils.toHtml(calendarValue);
       case CustomFormField.PERCENT:
         return StringUtils.toHtml(field.getValue()) + "%";
       case CustomFormField.INTEGER:
         // Determine the value to display in the field
         String integerValue = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(integerValue)) {
           try {
             NumberFormat formatter = NumberFormat.getInstance();
             integerValue = formatter.format(StringUtils.getIntegerNumber(field.getValue()));
           } catch (Exception e) {
             LOG.warn("Could not integer format: " + field.getValue());
           }
         }
         return integerValue;
       case CustomFormField.FLOAT:
         // Determine the value to display in the field
         String decimalValue = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(decimalValue)) {
           try {
             NumberFormat formatter = NumberFormat.getInstance();
             decimalValue = formatter.format(StringUtils.getDoubleNumber(field.getValue()));
           } catch (Exception e) {
             LOG.warn("Could not decimal format: " + field.getValue());
           }
         }
         return decimalValue;
       case CustomFormField.CURRENCY:
         // Use a currency for formatting
         String currencyCode = field.getValueCurrency();
         if (currencyCode == null) {
           currencyCode = field.getCurrency();
         }
         if (!StringUtils.hasText(currencyCode)) {
           currencyCode = "USD";
         }
         try {
           NumberFormat formatter = NumberFormat.getCurrencyInstance();
           if (currencyCode != null) {
             Currency currency = Currency.getInstance(currencyCode);
             formatter.setCurrency(currency);
           }
           return (StringUtils.toHtml(formatter.format(StringUtils.getDoubleNumber(field.getValue()))));
         } catch (Exception e) {
           LOG.error(e.getMessage());
         }
         return StringUtils.toHtml(field.getValue());
       case CustomFormField.EMAIL:
         return StringUtils.toHtml(field.getValue());
       case CustomFormField.PHONE:
         PhoneNumberBean phone = new PhoneNumberBean();
         phone.setNumber(field.getValue());
         PhoneNumberUtils.format(phone, Locale.getDefault());
         return StringUtils.toHtml(phone.toString());
       case CustomFormField.URL:
         String value = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(value)) {
           if (!value.contains("://")) {
             value = "http://" + value;
           }
           if (value.contains("://")) {
             return ("<a href=\"" + StringUtils.toHtml(value) + "\">" + StringUtils.toHtml(value) + "</a>");
           }
         }
         return StringUtils.toHtml(value);
       case CustomFormField.IMAGE:
         String image = StringUtils.toHtmlValue(field.getValue());
         if (StringUtils.hasText(image)) {
           Project project = ProjectUtils.loadProject(wiki.getProjectId());
           return ("<img src=\"" + contextPath + "/show/" + project.getUniqueId() + "/wiki-image/" + image + "\"/>");
         }
         return StringUtils.toHtml(image);
       default:
         return StringUtils.toHtml(field.getValue());
     }
   }
 
   /**
    * When round-tripping wiki markup, these characters are escaped for proper
    * parsing.
    *
    * @param text
    * @return
    */
   public static String processMarkupCharacters(String text) {
     text = StringUtils.replace(text, "\\*", "*");
     text = StringUtils.replace(text, "\\#", "#");
     text = StringUtils.replace(text, "\\=", "=");
     text = StringUtils.replace(text, "\\|", "|");
     text = StringUtils.replace(text, "\\{", "[");
     text = StringUtils.replace(text, "\\}", "]");
     return text;
   }
 }
