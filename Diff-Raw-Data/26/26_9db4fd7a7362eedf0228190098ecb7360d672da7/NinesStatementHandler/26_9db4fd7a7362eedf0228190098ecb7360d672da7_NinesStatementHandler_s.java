 /** 
  *  Copyright 2007 Applied Research in Patacriticism and the University of Virginia
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  **/
 package org.nines;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.nines.RDFIndexerConfig.Mode;
 import org.openrdf.model.Statement;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.BNodeImpl;
 import org.openrdf.model.impl.LiteralImpl;
 import org.openrdf.model.impl.URIImpl;
 import org.openrdf.rio.RDFHandler;
 import org.openrdf.rio.RDFHandlerException;
 
 final class NinesStatementHandler implements RDFHandler {
     private final static Logger log = Logger.getLogger(NinesStatementHandler.class.getName());
 
     private HashMap<String, HashMap<String, ArrayList<String>>> documents;
     private String dateBNodeId;
     private HashMap<String, ArrayList<String>> doc;
     private Boolean title_sort_added = false;
     private File file;
     private RDFIndexerConfig config;
     private ErrorReport errorReport;
     private String documentURI;
     private long largestTextField = -1;
     private LinkCollector linkCollector;
 
     public NinesStatementHandler(ErrorReport errorReport, LinkCollector linkCollector, RDFIndexerConfig config) {
         this.errorReport = errorReport;
         this.config = config;
         doc = new HashMap<String, ArrayList<String>>();
         documentURI = "";
         documents = new HashMap<String, HashMap<String, ArrayList<String>>>();
         this.linkCollector = linkCollector;
         System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
         System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");
     }
 
     public void handleStatement(Statement statement) throws RDFHandlerException {
 
         String subject = statement.getSubject().stringValue();
         String predicate = statement.getPredicate().stringValue();
         String object = statement.getObject().stringValue();
 
         // if the object of the triple is blank, skip it, it is nothing worth indexing
         if (object == null || object.length() == 0)
             return;
 
         // start of a new document
         if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(predicate)
             && statement.getSubject() instanceof URIImpl) {
             if (documents.get(subject) != null) {
                 errorReport.addError(new IndexerError(this.file.toString(), subject, "Duplicate URI"));
                 log.info("*** Duplicate: " + subject);
             }
             doc = new HashMap<String, ArrayList<String>>();
             addField(doc, "uri", subject);
             documents.put(subject, doc);
             title_sort_added = false;
             documentURI = subject;
             log.info("Parsing RDF for document: " + subject);
             errorReport.flush();
         }
         
         // Check for any unsupported nines:* attributes and issue error if any exist
         if (predicate.startsWith("http://www.nines.org/schema#")) {
             addError( "NINES is no longer a valid attribute: "+ predicate);
             return;
         }
 
         if (predicate.startsWith("http://www.collex.org/schema#")) {
             String attribute = predicate.substring("http://www.collex.org/schema#".length());
             if (!(attribute.equals("archive") || attribute.equals("freeculture") || attribute.equals("source_xml")
                 || attribute.equals("source_html") || attribute.equals("source_sgml") || attribute.equals("federation")
                 || attribute.equals("ocr") || attribute.equals("genre") || attribute.equals("thumbnail")
                 || attribute.equals("text") || attribute.equals("fulltext") || attribute.equals("image")
                 || attribute.equals("discipline"))) {
 
                 addError("Collex does not support this property: " + predicate );
                 return;
             }
         }
 
         // parse RDF statements into fields, return when the statement has been handled
         if (handleFederation(predicate, object))
             return;
         if (handleOcr(predicate, object))
             return;
         if (handleFullText(predicate, object))
             return;
         if (handleCollexSourceXml(predicate, object))
             return;
         if (handleCollexSourceHtml(predicate, object))
             return;
         if (handleCollexSourceSgml(predicate, object))
             return;
         if (handleArchive(predicate, object))
             return;
         if (handleFreeCulture(predicate, object))
             return;
         if (handleTitle(predicate, object))
             return;
         if (handleAlternative(predicate, object))
             return;
         if (handleGenre(predicate, object))
             return;
         if (handleDate(subject, predicate, statement.getObject()))
             return;
         if (handleDateLabel(subject, predicate, object))
             return;
         if (handleSource(predicate, object))
             return;
         if (handleThumbnail(predicate, object))
             return;
         if (handleImage(predicate, object))
             return;
         if (handleURL(predicate, object))
             return;
         if (handleText(predicate, object))
             return;
         if (handleRole(predicate, object))
             return;
         if (handlePerson(predicate, object))
             return;
         if (handleFormat(predicate, object))
             return;
         if (handleLanguage(predicate, object))
             return;
         if (handleGeospacial(predicate, object))
             return;
         if (handleProvenance(predicate, object))
             return;
         if (handleDiscipline(predicate, object))
             return;
         if (handleSubject(predicate, object))
             return;
         if (handleType(predicate, object))
             return;
     }
 
     private boolean handleFederation(String predicate, String object) {
         if ("http://www.collex.org/schema#federation".equals(predicate)) {
             if (object.equals("NINES") || object.equals("18thConnect") || object.equals("MESA")) {
                 addField(doc, "federation", object);
             } else {
                 addError("Unknown federation: " + object);
             }
             return true;
         }
         return false;
     }
 
     private boolean handleOcr(String predicate, String object) {
         if ("http://www.collex.org/schema#ocr".equals(predicate)) {
             if ("true".equalsIgnoreCase(object)) {
                 // only add a ocr field if it's true. No field set implies "F"alse
                 addField(doc, "is_ocr", "T");
                 return true;
             }
         }
         return false;
     }
 
     private boolean handlePerson(String predicate, String object) {
         if ("http://www.collex.org/schema#person".equals(predicate)) {
             addField(doc, "person", object);
             return true;
         }
         return false;
     }
 
     private boolean handleFormat(String predicate, String object) {
         if ("http://purl.org/dc/elements/1.1/format".equals(predicate)) {
             addField(doc, "format", object);
             return true;
         }
         return false;
     }
 
     private boolean handleLanguage(String predicate, String object) {
         if ("http://purl.org/dc/elements/1.1/language".equals(predicate)) {
             addField(doc, "language", object);
             return true;
         }
         return false;
     }
 
     private boolean handleGeospacial(String predicate, String object) {
         if ("http://www.collex.org/schema#geospacial".equals(predicate)) {
             addField(doc, "geospacial", object);
             return true;
         }
         return false;
     }
 
     private boolean handleCollexSourceXml(String predicate, String object) {
         if ("http://www.collex.org/schema#source_xml".equals(predicate)) {
             addField(doc, "source_xml", object);
             return true;
         }
         return false;
     }
 
     private boolean handleCollexSourceHtml(String predicate, String object) {
         if ("http://www.collex.org/schema#source_html".equals(predicate)) {
             addField(doc, "source_html", object);
             return true;
         }
         return false;
     }
 
     private boolean handleCollexSourceSgml(String predicate, String object) {
         if ("http://www.collex.org/schema#source_sgml".equals(predicate)) {
             addField(doc, "source_sgml", object);
             return true;
         }
         return false;
     }
 
     private boolean handleArchive(String predicate, String object) {
         if ("http://www.collex.org/schema#archive".equals(predicate)) {
             addField(doc, "archive", object);
             return true;
         }
         return false;
     }
 
     private boolean handleFreeCulture(String predicate, String object) {
         if ("http://www.collex.org/schema#freeculture".equals(predicate)) {
             if ("false".equalsIgnoreCase(object)) {
                 addFieldEntry(doc, "freeculture", "F", true); // "F"alse
             } else if ("true".equalsIgnoreCase(object)) {
                 addFieldEntry(doc, "freeculture", "T", true); // "T"rue
             }
             return true;
         }
         return false;
     }
 
     private boolean handleFullText(String predicate, String object) {
         if ("http://www.collex.org/schema#fulltext".equals(predicate)) {
             if ("false".equalsIgnoreCase(object)) {
                 // only add a fulltext field if its false. No field set implies "T"rue
                 addField(doc, "has_full_text", "F"); // "F"alse
             }
             return true;
         }
         return false;
     }
 
     private boolean handleTitle(String predicate, String object) {
         if ("http://purl.org/dc/elements/1.1/title".equals(predicate)) {
             addField(doc, "title", object);
             if (!title_sort_added) {
                 addField(doc, "title_sort", object);
                 title_sort_added = true;
             }
             return true;
         }
         return false;
     }
 
     private boolean handleProvenance(String predicate, String object) {
         if ("http://purl.org/dc/elements/1.1/provenance".equals(predicate)) {
             addField(doc, "provenance", object);
             return true;
         }
         return false;
     }
 
     private boolean handleType(String predicate, String object) {
         if ("http://purl.org/dc/elements/1.1/type".equals(predicate)) {
             addField(doc, "doc_type", object);
             return true;
         }
         return false;
     }
 
     private boolean handleDiscipline(String predicate, String object) {
             if ("http://www.collex.org/schema#discipline".equals(predicate)) {
                 addField(doc, "discipline", object);
                 return true;
             }
             return false;
         }
 
     private boolean handleSubject(String predicate, String object) {
         if ("http://purl.org/dc/elements/1.1/subject".equals(predicate)) {
             addField(doc, "subject", object);
             return true;
         }
         return false;
     }
 
     private boolean handleAlternative(String predicate, String object) {
         if ("http://purl.org/dc/terms/alternative".equals(predicate)) {
             addField(doc, "alternative", object);
             return true;
         }
         return false;
     }
 
     private boolean handleGenre(String predicate, String object) {
         if ("http://www.collex.org/schema#genre".equals(predicate)) {
             // ignore deprecated genres for backward compatibility
             if (!"Primary".equals(object) && !"Secondary".equals(object)) {
                 addField(doc, "genre", object);
             }
             return true;
         }
         return false;
     }
 
     private boolean handleDate(String subject, String predicate, Value value) {
         if ("http://purl.org/dc/elements/1.1/date".equals(predicate)) {
             String object = value.stringValue().trim();
             if (value instanceof LiteralImpl) {
                 // For backwards compatibility of simple <dc:date>, but also useful for cases where label and value are
                 // the same
                 if (object.matches("^[0-9]{4}$")) {
                     addField(doc, "year", object.substring(0, 4));
                 }
 
                 ArrayList<String> years = null;
                 try {
                    years = parseYears(object);
 
                     if (years.size() == 0) {
                         addError("Invalid date format: " + object);
                         return false;
                     }
 
                     for (String year : years) {
                         addField(doc, "year", year);
                     }
 
                     addField(doc, "date_label", object);
                 } catch (NumberFormatException e) {
                     addError( "Invalid date format: " + object);
                 }
             } else {
                 BNodeImpl bnode = (BNodeImpl) value;
                 dateBNodeId = bnode.getID();
             }
 
             return true;
         }
 
         return false;
     }
 
     private boolean handleDateLabel(String subject, String predicate, String object) {
         if (subject.equals(dateBNodeId)) {
             // if dateBNodeId matches, we assume we're under a <collex:date> and simply
             // look for <rdfs:label> and <rdf:value>
 
             if ("http://www.w3.org/2000/01/rdf-schema#label".equals(predicate)) {
                 addField(doc, "date_label", object);
                 return true;
             }
 
             if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#value".equals(predicate)) {
                 try {
                     ArrayList<String> years = parseYears(object);
                     for (String year : years) {
                         addField(doc, "year", year);
                     }
                 } catch (NumberFormatException e) {
                     addError("Invalid date format: " + object);
                 }
                 return true;
             }
         }
         return false;
     }
 
     private boolean handleSource(String predicate, String object) {
         if ("http://purl.org/dc/elements/1.1/source".equals(predicate)) {
             addField(doc, "source", object);
             return true;
         }
         return false;
     }
 
     private boolean handleThumbnail(String predicate, String object) {
         if ("http://www.collex.org/schema#thumbnail".equals(predicate)) {
             addField(doc, "thumbnail", object);
             return true;
         }
         return false;
     }
 
     private boolean handleImage(String predicate, String object) {
         if ("http://www.collex.org/schema#image".equals(predicate)) {
             addField(doc, "image", object);
             return true;
         }
         return false;
     }
 
     private boolean handleURL(String predicate, String object) {
         if ("http://www.w3.org/2000/01/rdf-schema#seeAlso".equals(predicate)) {
             addField(doc, "url", object);
             return true;
         }
         return false;
     }
 
     private boolean handleText(String predicate, String object) {
 
         // first, check if this object is TEXT. If it is the predicate
         // will have the #text url below....
         if ("http://www.collex.org/schema#text".equals(predicate)) {
 
             // Objects with external content will have some form of
             // http url as the content.
             String text = object;
             boolean externalText = false;
             if (object.trim().startsWith("http://") && object.trim().indexOf(" ") == -1) {
                 addFieldEntry(doc, "text_url", text, false);
 
                 // only in index mode do we attempt to grab 
                 // full text from the full text folder
                 if (config.mode == Mode.INDEX) {
                     externalText = true;
                     text = getFullText( text );
                 } else {
                     text = "";
                 }
             }
 
             if (text.length() > 0) {
                 this.largestTextField = Math.max(this.largestTextField, text.length());
                 // NOTE: the !externalText signals to the add method that it
                 // should NOT perform any cleanup. Text goes in untouched.
                 addFieldEntry(doc, "text", text, false, !externalText);
             }
 
             return true;
         }
         return false;
     }
     
     /**
      * find the full path to the full text root baseed on 
      * the path to the original rdf sources
      * @return
      */
     private String findFullTextRoot() {
         String path = this.config.sourceDir.toString();
         int pos = path.indexOf("/rdf/");
         path = path.substring(0, pos) + "/fulltext/";
         path += SolrClient.safeCore(this.config.archiveName) + "/";
         return path;
     }
     
     /**
      * Read the full text for <code>uri</code> from the fulltext area of the solr sources.
      * If any errors are encountered, log them and return an empty string
      * 
      * @param uri
      * @return A string containing the full text - or an empty string if errors occur.
      */
     private String getFullText(String uri) {
 
         String fullTextRoot = findFullTextRoot() ;
         File root = new File( fullTextRoot );
         if (root.exists() == false) {
             this.errorReport
                 .addError(new IndexerError("", uri, "Missing full text source directory " + root.toString()));
             return "";
         }
 
         // convert URL into filename for text
         String name = uri.replaceAll("/", "SL");
         name = name.replace(":", "CL");
         name = name.replace("?", "QU");
         name = name.replace("=", "EQ");
         name = name.replace("&", "AMP");
         File textFile = new File(fullTextRoot + name + ".txt");
         if (textFile.exists() == false) {
             this.errorReport.addError(new IndexerError("", uri, "Missing full text file " + textFile.toString()));
             return "";
         }
 
         // read it!
         FileInputStream is = null;
         try {
             is = new FileInputStream(textFile);
             return IOUtils.toString( is, "UTF-8");
         } catch (IOException e) {
             errorReport.addError(new IndexerError(textFile.toString(), uri, "Unable to read full text" + ": "
                 + e.toString()));
             return "";
         } finally {
             IOUtils.closeQuietly(is);
         }
     }
 
     private boolean handleRole(String predicate, String object) {
         if (predicate.startsWith("http://www.loc.gov/loc.terms/relators/")) {
             String role = predicate.substring("http://www.loc.gov/loc.terms/relators/".length());
             addField(doc, "role_" + role, object);
             addField(doc, "role", "role_" + role);
             return true;
         }
         return false;
     }
 
     public static ArrayList<String> parseYears(String value) {
         ArrayList<String> years = new ArrayList<String>();
 
         if ("unknown".equalsIgnoreCase(value.trim()) || "uncertain".equalsIgnoreCase(value.trim())) {
             years.add("Uncertain");
         } else {
 
             // expand 184u to 1840-1849
             if (value.indexOf('u') != -1) {
                 char[] yearChars = value.toCharArray();
                 int numLength = value.length();
                 int i, factor = 1, startPos = 0;
 
                 if (numLength > 4)
                     numLength = 4;
 
                 // increase factor according to size of number
                 for (i = 0; i < numLength; i++)
                     factor *= 10;
 
                 // start looking for 'u', decreasing factor as we go
                 for (i = startPos; i < value.length(); i++) {
                     if (yearChars[i] == 'u') {
                         int padSize = value.length() - i;
                         String formatStr = "%0" + padSize + "d";
                         // iterate over each year
                         for (int j = 0; j < factor; j++) {
                             years.add(value.substring(0, i) + String.format(formatStr, j));
                         }
                         // once one 'u' char is found, we are done
                         break;
                     }
                     factor = factor / 10;
                 }
             } else {
                 // 1862-12-25,1863-01-01 1875 1954-10
                 StringTokenizer tokenizer = new StringTokenizer(value);
                 while (tokenizer.hasMoreTokens()) {
                     String range = tokenizer.nextToken();
 
                     int commaPos = range.indexOf(',');
 //if (commaPos == -1) {
 //	commaPos = range.indexOf('-');
 //}
                     String start, finish;
                     if (commaPos == -1) {
                         start = finish = range;
                     } else {
                         start = range.substring(0, commaPos);
                         finish = range.substring(commaPos + 1);
                     }
                     if (start.length() >= 4 && finish.length() >= 4) {
                         years.addAll(enumerateYears(start.substring(0, 4), finish.substring(0, 4)));
                     }
                 }
 
             }
         }
 
         return years;
     }
 
     public void addField(HashMap<String, ArrayList<String>> map, String name, String value) {
         // skip null fields
         if (value == null || name == null)
             return;
 
         // if the field is a url, check to see if it is reachable
         if (config.collectLinks && value.trim().startsWith("http://") && value.trim().indexOf(" ") == -1
             && !"uri".equals(name)) {
             linkCollector.addLink(documentURI, this.file.toString(), value);
         }
 
         addFieldEntry(map, name, value, false);
     }
     
     /**
      * Add a CLEANED field entry. The entry will be normalize, escape sequences stripped
      * and invalid utf-8 chars stripped
      * @param map
      * @param name
      * @param value
      * @param replace
      */
     private void addFieldEntry(HashMap<String, ArrayList<String>> map, String name, String value, Boolean replace) {
         addFieldEntry(map, name, value, replace, true);
     }
 
     /**
      * Add a new field entry and optionally clean the data
      * @param map
      * @param name
      * @param value
      * @param replace
      * @param clean
      */
     private void addFieldEntry(HashMap<String, ArrayList<String>> map, String name, String value, boolean replace, boolean clean) {
 
         // clean everything going in? 
         String data = value;
         if ( clean ) {
             data = TextUtils.stripEscapeSequences(data, this.errorReport, this.file, this.documentURI);
             data = TextUtils.normalizeWhitespace(data);
             data = TextUtils.stripUnknownUTF8(data, this.errorReport, this.file, this.documentURI);
         }
        
         // make sure we add to array for already existing fields
         if (map.containsKey(name) && replace == false) {
             ArrayList<String> pastValues = map.get(name);
             pastValues.add(data);
             map.put(name, pastValues);
         } else {
             ArrayList<String> values = new ArrayList<String>();
             values.add(data);
             map.put(name, values);
         }
     }
 
 
     private String getFirstField(HashMap<String, ArrayList<String>> object, String field) {
         ArrayList<String> objectArray = object.get(field);
         if (objectArray != null) {
             return objectArray.get(0);
         }
         return "";
     }
 
     public HashMap<String, HashMap<String, ArrayList<String>>> getDocuments() {
         // add author_sort: we do that here because we have a few different fields we look at and the order they appear
         // shouldn't matter, so we wait to the end to find them.
         Set<String> keys = documents.keySet();
         for (String uri : keys) {
             HashMap<String, ArrayList<String>> object = documents.get(uri);
             String author = getFirstField(object, "role_AUT");
             String artist = getFirstField(object, "role_ART");
             String editor = getFirstField(object, "role_EDT");
             String publisher = getFirstField(object, "role_PUB");
             String translator = getFirstField(object, "role_TRN");
             String printer = getFirstField(object, "role_CRE");
             String etcher = getFirstField(object, "role_ETR");
             String engraver = getFirstField(object, "role_EGR");
             if (author.length() > 0)
                 addField(object, "author_sort", author);
             else if (artist.length() > 0)
                 addField(object, "author_sort", artist);
             else if (editor.length() > 0)
                 addField(object, "author_sort", editor);
             else if (publisher.length() > 0)
                 addField(object, "author_sort", publisher);
             else if (translator.length() > 0)
                 addField(object, "author_sort", translator);
             else if (printer.length() > 0)
                 addField(object, "author_sort", printer);
             else if (etcher.length() > 0)
                 addField(object, "author_sort", etcher);
             else if (engraver.length() > 0)
                 addField(object, "author_sort", engraver);
 
             // add year_sort
            String year_sort_field = getFirstField(object, "date_label");
            if (year_sort_field.length() > 0)
                 addField(object, "year_sort", year_sort_field);
            else {
                year_sort_field = getFirstField(object, "year");
                if (year_sort_field.length() > 0)
                    addField(object, "year_sort", year_sort_field);
             }
 
             // add fulltext and ocr indicators
             ArrayList<String> objectArray = object.get("text");
             if (objectArray != null) { // If we have a text field
                 if (object.get("has_full_text") == null)
                     addField(object, "has_full_text", "T");
             } else {
                 if (object.get("has_full_text") == null)
                     addField(object, "has_full_text", "F");
             }
             objectArray = object.get("is_ocr");
             if (objectArray == null) // If we weren't told differently, then it is not an ocr object
                 addField(object, "is_ocr", "F");
             objectArray = object.get("freeculture");
             if (objectArray == null) // If we weren't told differently, then it is freeculture
                 addField(object, "freeculture", "T");
         }
         return documents;
     }
 
     private static ArrayList<String> enumerateYears(String startYear, String endYear) {
         int y1 = Integer.parseInt(startYear);
         int y2 = Integer.parseInt(endYear);
 
         ArrayList<String> years = new ArrayList<String>();
         years.add(startYear);
         if (y2 <= y1)
             return years;
 
         for (int i = y1 + 1; i <= y2; i++) {
             years.add("" + i);
         }
 
         return years;
     }
     
     private void addError( final String message ) {
         this.errorReport.addError(new IndexerError(this.file.toString(), this.documentURI, message));
     }
 
     public void setFile(final File file) {
         this.file = file;
     }
     
     public long getLargestTextSize() {
         return this.largestTextField;
     }
 
     public void endRDF() throws RDFHandlerException {
         // no-op
     }
 
     public void handleComment(String arg0) throws RDFHandlerException {
         // no-op
     }
 
     public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
         // no-op
     }
 
     public void startRDF() throws RDFHandlerException {
         // no-op
     }
 }
