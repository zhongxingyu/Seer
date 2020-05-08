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
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.rio.StatementHandler;
 import org.openrdf.rio.StatementHandlerException;
 import org.openrdf.sesame.sailimpl.memory.BNodeNode;
 import org.openrdf.sesame.sailimpl.memory.LiteralNode;
 import org.openrdf.sesame.sailimpl.memory.URINode;
 import org.apache.commons.lang.StringEscapeUtils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 import net.sf.saxon.functions.Substring;
 import org.apache.commons.httpclient.ConnectTimeoutException;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.NoHttpResponseException;
 import org.apache.log4j.Logger;
 
 public class NinesStatementHandler implements StatementHandler {
   public final static Logger log = Logger.getLogger(NinesStatementHandler.class.getName());
 
   private HashMap<String, HashMap<String, ArrayList<String>>> documents;
   private String dateBNodeId;
   private HashMap<String, ArrayList<String>> doc;
   private Boolean title_sort_added = false;
   private String filename; 
   private RDFIndexerConfig config;
   private ErrorReport errorReport;
   private HttpClient httpClient;
   private String documentURI;
   private LinkCollector linkCollector;
   private static final int SOLR_REQUEST_NUM_RETRIES = 5; // how many times we should try to connect with solr before giving up
   private static final int SOLR_REQUEST_RETRY_INTERVAL = 30 * 1000; // milliseconds
   private Boolean ignore = false;
 
   public NinesStatementHandler( ErrorReport errorReport, LinkCollector linkCollector, RDFIndexerConfig config  ) {
 	 this.errorReport = errorReport;
 	 this.config = config;
          this.httpClient = new HttpClient();
 	 doc = new HashMap<String, ArrayList<String>>();
 	 documentURI = "";
 	 documents = new HashMap<String, HashMap<String, ArrayList<String>>>();
 	 this.linkCollector = linkCollector;
 	System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
 	System.setProperty ("org.apache.commons.logging.simplelog.showdatetime", "true");
       System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");
   }
   
   public void handleStatement(Resource resource, URI uri, Value value) throws StatementHandlerException {
 	  if (ignore)
 		  return;
 
     String subject = resource.toString();
     String predicate = uri.getURI();
     String object = value.toString().trim();
 
     // if the object of the triple is blank, skip it, it is nothing worth indexing
     if (object == null || object.length() == 0)
       return;
     
     // start of a new document
     if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(predicate) && resource instanceof URINode) {
 		if (documents.size() >= config.maxDocsPerFolder) {
 			ignore = true;
 	       log.info("*** Ignoring rest of file starting here: " + subject);
 			return;
 		}
       if (documents.get(subject) != null) {
         errorReport.addError(new IndexerError(filename, subject, "Duplicate URI"));
 		log.info("*** Duplicate: " + subject);
       }
       doc = new HashMap<String, ArrayList<String>>();
       addField(doc, "uri", subject);
       documents.put(subject, doc);
 	  title_sort_added = false;
       documentURI = subject;
       //if( documentURI.equals("") ) documentURI = subject;
       log.info("Parsing RDF for document: "+subject );
       errorReport.flush();
     }
     // Check for any unsupported nines:* attributes and issue error if any exist
     if (predicate.startsWith("http://www.nines.org/schema#")) {
 
         errorReport.addError(
             new IndexerError(filename, documentURI, "NINES is no longer a valid attribute: " + predicate));
 
         return;
     }
 
     if (predicate.startsWith("http://www.collex.org/schema#")) {
       String attribute = predicate.substring("http://www.collex.org/schema#".length());
       if (! (attribute.equals("archive") || attribute.equals("freeculture") ||
           attribute.equals("source") || attribute.equals("federation") || attribute.equals("ocr") ||
           attribute.equals("genre") || attribute.equals("thumbnail") || attribute.equals("text") || attribute.equals("fulltext") ||
           attribute.equals("image"))) {
 
         errorReport.addError(
             new IndexerError(filename, documentURI, "Collex does not support this property: " + predicate));
       
         return;
       }
     }
     
     // parse RDF statements into fields, return when the statement has been handled    
     if( handleFederation(predicate, object) ) return;
     if( handleOcr(predicate, object) ) return;
     if( handleFullText(predicate, object) ) return;
     if( handleArchive(predicate, object) ) return;
     if( handleFreeCulture(predicate, object) ) return;
     if( handleTitle(predicate, object) ) return;
     if( handleAlternative(predicate, object) ) return;
     if( handleGenre(predicate, object) ) return;
     if( handleDate(subject, predicate, value) ) return;
     if( handleDateLabel(subject, predicate, object) ) return;
     if( handleSource(predicate, object) ) return;
     if( handleThumbnail(predicate, object) ) return;
     if( handleImage(predicate, object) ) return;
     if( handleURL(predicate, object) ) return;
     if( handleText(predicate, object) ) return;
     if( handleRole(predicate, object) ) return;
     if( handlePerson(predicate, object) ) return;
     if( handleFormat(predicate, object) ) return;
     if( handleLanguage(predicate, object) ) return;
     if( handleGeospacial(predicate, object) ) return;
   }
   
   public boolean handleFederation( String predicate, String object ) {
 	  if ("http://www.collex.org/schema#federation".equals(predicate)) {
 		  if (object.equals("NINES") || object.equals("18th Connect"))
 		      addField(doc, "federation", object);
 		  else
 			   errorReport.addError(new IndexerError(filename, documentURI, "Unknown federation: " + object));
 	      return true;
 	  }
 	  return false;
   }
 
   public boolean handleOcr( String predicate, String object ) {
 	  if ("http://www.collex.org/schema#ocr".equals(predicate)) {
 		if ("true".equalsIgnoreCase(object)) {
         // only add a ocr field if it's true.  No field set implies "F"alse
 	      addField(doc, "is_ocr", "T");
 	      return true;
 		}
 	  }
 	  return false;
   }
 
   public boolean handlePerson( String predicate, String object ) {
 	  if ("http://www.collex.org/schema#person".equals(predicate)) {
 	      addField(doc, "person", object);
 	      return true;
 	  }
 	  return false;
   }
 
   public boolean handleFormat( String predicate, String object ) {
 	  if ("http://www.collex.org/schema#format".equals(predicate)) {
 	      addField(doc, "format", object);
 	      return true;
 	  }
 	  return false;
   }
 
   public boolean handleLanguage( String predicate, String object ) {
 	  if ("http://www.collex.org/schema#language".equals(predicate)) {
 	      addField(doc, "language", object);
 	      return true;
 	  }
 	  return false;
   }
 
   public boolean handleGeospacial( String predicate, String object ) {
 	  if ("http://www.collex.org/schema#geospacial".equals(predicate)) {
 	      addField(doc, "geospacial", object);
 	      return true;
 	  }
 	  return false;
   }
 
   public boolean handleArchive( String predicate, String object ) {
 	  if ("http://www.collex.org/schema#archive".equals(predicate)) {
 	      addField(doc, "archive", object);
 	      return true;
 	  }
 	  return false;
   }
     
   public boolean handleFreeCulture( String predicate, String object ) {
     if ("http://www.collex.org/schema#freeculture".equals(predicate)) {
       if ("false".equalsIgnoreCase(object)) {
         // only add a freeculture field if its false.  No field set implies "T"rue
         addField(doc, "freeculture", "F");  // "F"alse
       }
       return true;
     }
     return false;
   }
 
   public boolean handleFullText( String predicate, String object ) {
     if ("http://www.collex.org/schema#fulltext".equals(predicate)) {
       if ("false".equalsIgnoreCase(object)) {
         // only add a fulltext field if its false.  No field set implies "T"rue
         addField(doc, "has_full_text", "F");  // "F"alse
       }
       return true;
     }
     return false;
   }
 
   public boolean handleTitle( String predicate, String object ) {
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
   
   public boolean handleAlternative( String predicate, String object ) {
     if ("http://purl.org/dc/terms/alternative".equals(predicate)) {
       addField(doc, "alternative", object);
       return true;
     }
     return false;
   }
   
   public boolean handleGenre( String predicate, String object ) {
     if ("http://www.collex.org/schema#genre".equals(predicate)) {
       // ignore deprecated genres for backward compatibility
       if (!"Primary".equals(object) && !"Secondary".equals(object)) {
         addField(doc, "genre", object);
       }
       return true;
     }
     return false;
   }
   
   public boolean handleDate( String subject, String predicate, Value value ) {
     if ("http://purl.org/dc/elements/1.1/date".equals(predicate)) {
       String object = value.toString().trim();
       if (value instanceof LiteralNode) {
         // For backwards compatibility of simple <dc:date>, but also useful for cases where label and value are the same
         if (object.matches("^[0-9]{4}.*")) {
           addField(doc, "year", object.substring(0, 4));
         }
 
         ArrayList<String> years = null;
         try {
           years = parseYears(object);
           
           if( years.size() == 0 ) {
               errorReport.addError( new IndexerError(filename, documentURI, "Invalid date format: " + object) );
               return false;
           }
           
           for (String year : years) {
             addField(doc, "year", year);
           }
 
           addField(doc, "date_label", object);
         } catch (NumberFormatException e) {
           errorReport.addError(
               new IndexerError(filename, documentURI, "Invalid date format: " + object));
         }
       } else {
         BNodeNode bnode = (BNodeNode) value;
         dateBNodeId = bnode.getID();
       }
       
       return true;
     }
     
     return false;
   }
   
   public boolean handleDateLabel( String subject, String predicate, String object ) {
     if (subject.equals(dateBNodeId)) {
       // if dateBNodeId matches, we assume we're under a <nines:date> and simply
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
           errorReport.addError(
               new IndexerError(filename, documentURI, "Invalid date format: " + object));
         }
         return true;
       }
     }
     return false;
   }
   
   public boolean handleSource( String predicate, String object ) {
     if ("http://purl.org/dc/elements/1.1/source".equals(predicate)) {
       addField(doc, "source", object);
       return true;
     }
     return false;
   }
   
   public boolean handleThumbnail( String predicate, String object ) {
     if ("http://www.collex.org/schema#thumbnail".equals(predicate)) {
       addField(doc, "thumbnail", object);
       return true;
     }
     return false;
   }
   
   public boolean handleImage( String predicate, String object ) {
     if ("http://www.collex.org/schema#image".equals(predicate)) {
       addField(doc, "image", object);
       return true;
     }
     return false;
   }
 
   public boolean handleURL( String predicate, String object ) {
     if ("http://www.w3.org/2000/01/rdf-schema#seeAlso".equals(predicate)) {
       addField(doc, "url", object);
       return true;
     }
     return false;
   }
   
   public boolean handleText( String predicate, String object ) {
     if ("http://www.collex.org/schema#text".equals(predicate)) {
       try {
         String text = object;
         if (object.trim().startsWith("http://") && object.trim().indexOf(" ") == -1) {
           addFieldEntry(doc, "text_url", text);
 		  if (text.endsWith(".pdf") || text.endsWith(".PDF")) {
 			errorReport.addError(new IndexerError(filename, documentURI, "PDF file ignored for now: " + text));
 			text = "";
 		  }
 		  else {
 			 if( config.retrieveFullText ) text = fetchContent(object);
 			  if (config.reindexFullText) {
 				  text = getFullText(doc.get("uri").get(0), httpClient );
 
 			  		text = unescapeXML(text);
 
					text = cleanText(text);
 
 					// print out all lines with ampersands to see what we're up against.
 					printLinesContaining(text, "&");
 					printLinesContaining(text, "{");
 					printLinesContaining(text, "}");
 
 			  }
 		  }
         }
 		if (text.length() > 0)
 	        addFieldEntry(doc, "text", text);
 //        addFieldEntry(doc, "content", text);
       } catch (IOException e) {
         String uriVal = documentURI;
         errorReport.addError(
             new IndexerError(filename, uriVal, e.getMessage()));
       }
       return true;
     }
     return false;
   }
 
   private String removeDirtyLines(String text, String [] matchList) {
 		String [] arr = text.split("\n");
 		Boolean foundOne = false;
 		for (int i = 0; i < arr.length; i++) {
 			String currLine = arr[i];
 			for (int j = 0; j < matchList.length; j++)
 				if (currLine.equals(matchList[j])) {
 					arr[i] = "";
 					foundOne = true;
 				}
 		}
 		if (foundOne) {
 			text = "";
 			for (int i = 0; i < arr.length; i++) {
 				if (arr[i].length() > 0)
 					text += arr[i] + "\n";
 			}
 		}
 		return text;
   }
 
   private void printLinesContaining(String text, String match) {
 		int amp = text.indexOf(match);
 		if ((text.indexOf("& ") == amp) || (text.indexOf("&c.") == amp) || (text.indexOf("&c ") == amp) || (text.indexOf("&\n") == amp))	// skip legitimate uses of &
 			amp = text.indexOf(match, amp + 1);
 		while (amp >= 0) {
 			int start = amp - 15;
 			if (start < 0) start = 0;
 			int end = amp + 30;
 			if (end >= text.length())
 				end = text.length() - 1;
 			String extract = text.substring(start, end);
 			extract = replaceMatch(extract, "\n", "\\n");
 			extract = replaceMatch(extract, "\r", "\\r");
 			Boolean skip = false;
 			if (text.length() > amp+3) {
 				if ((text.substring(amp, amp+2).equals("& ")) ||
 						(text.substring(amp, amp+2).equals("&\n")) ||
 						(text.substring(amp, amp+3).equals("&c ")) ||
 						(text.substring(amp-1, amp+3).equals("A&M ")) ||
 						(text.substring(amp-1, amp+3).equals("Q&A:")) ||
 						(text.substring(amp-1, amp+3).equals("1&2 ")) ||
 						(text.substring(amp-1, amp+3).equals("1&2\n")) ||
 						(text.substring(amp, amp+3).equals("&c.")))
 					skip = true;
 			}
 			if (!skip)
 				errorReport.addError(new IndexerError(filename, documentURI, "Text: " + extract));
 //			int nextAmp = text.indexOf(match, amp + 1);
 //			while ((amp >= 0) && ((text.indexOf("& ", amp + 1) == nextAmp) || (text.indexOf("&c.", amp + 1) == nextAmp) || (text.indexOf("&c ", amp + 1) == nextAmp))) {	// skip legitimate uses of &
 //				amp = nextAmp;
 //				nextAmp = text.indexOf(match, amp + 1);
 //			}
 //			amp = nextAmp;
 			amp = text.indexOf(match, amp + 1);
 		}
   }
 
   private String getFullText(String uri, HttpClient httpclient ) {
 	  String fullText = "";
     String solrUrl = config.solrBaseURL + config.solrExistingIndex + "/select";
 
     GetMethod get = new GetMethod(solrUrl);
     NameValuePair queryParam = new NameValuePair("q", "uri:\""+uri + "\"");
     NameValuePair params[] = new NameValuePair[]{queryParam};
     get.setQueryString(params);
 
     int result;
     try {
       int solrRequestNumRetries = SOLR_REQUEST_NUM_RETRIES;
       do {
         result = httpclient.executeMethod(get);
         solrRequestNumRetries--;
         if(result != 200) {
           try {
             Thread.sleep(SOLR_REQUEST_RETRY_INTERVAL);
           } catch(InterruptedException e) {
             log.info(">>>> Thread Interrupted");
           }
         }
       } while(result != 200 && solrRequestNumRetries > 0);
 
       if (result != 200) {
         errorReport.addError(new IndexerError("","","cannot reach URL: " + solrUrl));
       }
 
       //String response = get.getResponseBodyAsString();
 //		BufferedReader reader = new BufferedReader(get.getResponseBodyAsStream());
 //		StringBuilder sb = new StringBuilder();
 //		String line = null;
 //		try {
 //			while ((line = reader.readLine()) != null) {
 //				sb.append(line + "\n");
 //			}
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 //		fullText = sb.toString();
 
 //		BufferedInputStream bis = new BufferedInputStream(get.getResponseBodyAsStream());
 //	  byte[] b = new byte[4096];
 //	  int len;
 //	  while ((len = bis.read(b)) > 0) {
 //        String str = new String(b, 0, len, get.getResponseCharSet());
 //		  String start = "<arr name=\"text\"><str>";
 //		  String stop = "</str></arr>";
 //		  if (fullText.length() == 0) {
 //			  int iStart = str.indexOf(start);
 //			if (iStart >= 0) {
 //			  fullText = str.substring(iStart+start.length());
 //			  int iEnd = fullText.indexOf(stop);
 //			  if (iEnd >= 0) {
 //				  fullText = fullText.substring(0, iEnd);
 //				  break;
 //			  }
 //			}
 //		  } else {
 //			  int iEnd = str.indexOf(stop);
 //			  if (iEnd == -1)
 //				  fullText += str;
 //			  else {
 //				  fullText += str.substring(0, iEnd);
 //				  break;
 //			  }
 //		  }
 //      }
 	  //fullText = parseXML(response);
 	  fullText = get.getResponseBodyAsString();
 	  String start = "<arr name=\"text\"><str>";
 	  String stop = "</str></arr>";
 	 fullText = trimBracketed(fullText, start, stop);
 
     } catch (NoHttpResponseException e) {
       errorReport.addError(new IndexerError("","","The SOLR server didn't respond to the http request to: " + solrUrl));
     } catch (ConnectTimeoutException e) {
       errorReport.addError(new IndexerError("","","The SOLR server timed out on the http request to: " + solrUrl));
     } catch (IOException e) {
       errorReport.addError(new IndexerError("","","An IO Error occurred attempting to access: " + solrUrl));
 	}
     finally {
       get.releaseConnection();
     }
 
     return unescapeXML(fullText);
 }
 
 //  private class StrPair {
 //	  public String first;
 //	  public String second;
 //	  public StrPair(String f, String s) {
 //		  first = f;
 //		  second = s;
 //	  }
 //  }
 
   private String unescapeXML(String str) {
 	str = str.replaceAll("&nbsp;", " ");	// This is escaped to a unicode char. For our purposes, we want this to be a space.
 	str = StringEscapeUtils.unescapeHtml(str);
 
 	//for some reason, unescapeHtml doesn't get everything.
 //	str = str.replaceAll("&#8226;", "•");
 //	str = str.replaceAll("&quot;", "\"");
 //
 //	// Also, some of the text we get is missing the final semi colon
 //	str = str.replaceAll("&nbsp;", " ");
 //	str = str.replaceAll("&nbsp", " ");
 //	str = str.replaceAll("&mdash;", "-");
 //	str = str.replaceAll("&mdash", "-");
 //	str = str.replaceAll("&#151;", "-");
 //	str = str.replaceAll("&#151", "-");
 //	str = str.replaceAll("&hyphen;", "-");
 //	str = str.replaceAll("&hyphen", "-");
 //	str = str.replaceAll("&colon;", ":");
 //	str = str.replaceAll("&colon", ":");
 
 	return str;
 
 //	String [] arr = str.split("&");
 //	StrPair[] matchList = new StrPair[]{
 //		new StrPair("lt;", "<"),
 //		new StrPair("gt;", ">"),
 //		new StrPair("nbsp;", " "),
 //		new StrPair("nbsp", " "),
 //		new StrPair("#034;", "\""),
 //		new StrPair("ldquo;", "\""),
 //		new StrPair("#039;", "'"),
 //		new StrPair("#39;", "'"),
 //		new StrPair("#8217;", "'"),
 //		new StrPair("#8216;", "'"),
 //		new StrPair("#146;", "'"),
 //		new StrPair("#339;", "œ"),
 //		new StrPair("#8226;", "•"),
 //		new StrPair("#8230;", "..."),
 //		new StrPair("mdash;", "-"),
 //		new StrPair("mdash", "-"),
 //		new StrPair("ndash;", "-"),
 //
 //		new StrPair("#8211;", "-"),
 //		new StrPair("hyphen;", "-"),
 //		new StrPair("hyphen", "-"),
 //		new StrPair("#151;", "-"),
 //		new StrPair("#151", "-"),
 //		new StrPair("colon;", ":"),
 //		new StrPair("colon", ":"),
 //		new StrPair("eacute;", "é"),
 //		new StrPair("ccedil;", "ç"),
 //		new StrPair("#147;", "\""),
 //		new StrPair("#148;", "\""),
 //		new StrPair("quot;", "\""),
 //		new StrPair("lsquo;", "'"),
 //
 //		new StrPair("agrave;", "à"),
 //		new StrPair("ugrave;", "ù"),
 //		new StrPair("egrave;", "è"),
 //		new StrPair("#8212;", "-"),
 //		new StrPair("#9;", ""),
 //		new StrPair("#145;", "'"),
 //		new StrPair("hellip;", "..."),
 //		new StrPair("Uuml;", "Ü"),
 //		new StrPair("uuml;", "ü"),
 //		new StrPair("ouml;", "ö"),
 //		new StrPair("Aacute;", "Á"),
 //		new StrPair("aacute;", "á"),
 //		new StrPair("acute;", "´"),
 //		new StrPair("macr;", "¯"),
 //
 //		new StrPair("sect;", "§"),
 //		new StrPair("szlig;", "ß"),
 //		new StrPair("auml;", "ä"),
 //		new StrPair("ecirc;", "ê"),
 //		new StrPair("ocirc;", "ô"),
 //		new StrPair("icirc;", "î"),
 //
 //		new StrPair("iuml;", "ï"),
 //		new StrPair("oacute;", "ó"),
 //		new StrPair("rsquo;", "'"),
 //		new StrPair("#156;", "œ"),
 //		new StrPair("Eacute;", "É"),
 //		new StrPair("rdquo;", "\""),
 //		new StrPair("oelig;", "œ"),
 //		new StrPair("Auml;", "Ä"),
 //		new StrPair("acirc;", "â"),
 //
 //		new StrPair("euml;", "ë"),
 //		new StrPair("copy;", "©"),
 //		new StrPair("iacute;", "í"),
 //		new StrPair("ntilde;", "ñ"),
 //		new StrPair("pound;", "£"),
 //		new StrPair("uacute;", "ú"),
 //		new StrPair("Egrave;", "È"),
 //		new StrPair("Icirc;", "î"),
 //		new StrPair("Euml;", "Ë"),
 //		new StrPair("cedil;", "ç"),
 //		new StrPair("Ocirc;", "Ô"),
 //		new StrPair("Igrave;", "Ì"),
 //		new StrPair("Icirc;", "Î"),
 //		new StrPair("Ograve;", "Ò"),
 //		new StrPair("scaron;", ""),
 //		new StrPair("Ecirc;", "Ê"),
 //		new StrPair("thorn;", "þ"),
 //		new StrPair("uacute;", "ú"),
 //		new StrPair("aelig;", "æ"),
 //		new StrPair("Agrave;", "À"),
 //		new StrPair("Oslash;", "Ø"),
 //		new StrPair("oslash;", "ø"),
 //		new StrPair("iquest;", "¿"),
 //		new StrPair("middot;", "·"),
 //		new StrPair("yacute;", "ý"),
 //		new StrPair("deg;", "°"),
 //		new StrPair("yen;", "¥"),
 //		new StrPair("#x17D;", "Ž"),
 //		new StrPair("#x17E;", " 	ž "),
 //		new StrPair("#x159;", "ř "),
 //		new StrPair("#131;", "ƒ"),
 //		new StrPair("#150;", "-"),
 //		new StrPair("#135;", "‡"),
 //		new StrPair("#138;", "Š"),
 //		new StrPair("#157;", ""),
 //		new StrPair("#0;", ""),
 //		new StrPair("#169;", "©"),
 //		new StrPair("#009;", ""),
 //		new StrPair("atilde;", "ã"),
 //		new StrPair("Atilde;", "Ã"),
 //		new StrPair("Ntilde;", "Ñ"),
 //
 //		new StrPair("Scaron;", "Š"),
 //		new StrPair("Acirc;", "Â"),
 //		new StrPair("ograve;", "ò"),
 //		new StrPair("ucirc;", "û"),
 //		new StrPair("#16;", ""),
 //		new StrPair("#17;", ""),
 //		new StrPair("#7;", ""),
 //		new StrPair("#42;", "*"),
 //		new StrPair("#42", "*"),
 //		new StrPair("#163;", "£"),
 //		new StrPair("#8224;", "†"),
 //
 //		new StrPair("#166;", "¦"),
 //		new StrPair("#171;", "«"),
 //		new StrPair("#173;", ""),
 //		new StrPair("#180;", "´"),
 //		new StrPair("#183;", "·"),
 //		new StrPair("#187;", "»"),
 //		new StrPair("#191;", "¿"),
 //		new StrPair("otilde;", "õ"),
 //		new StrPair("Ccedil;", "Ç"),
 //		new StrPair("Iacute;", "Í"),
 //		new StrPair("Uacute;", "Ú"),
 //		new StrPair("yuml;", "ÿ"),
 //		new StrPair("reg;", "®"),
 //		new StrPair("eacute", "é"),
 //		new StrPair("Oacute;", "Ó"),
 //		new StrPair("Otilde;", "Õ"),
 //		new StrPair("igrave;", "ì"),
 //		new StrPair("Ouml;", "Ö"),
 //		new StrPair("eth;", "ð"),
 //		new StrPair("AElig;", "Æ"),
 //		new StrPair("Yacute;", "Ý"),
 //		new StrPair("Aring;", "Å"),
 //		new StrPair("THORN;", "Þ"),
 //		new StrPair("pounds;", "£"),
 //		new StrPair("#233;", "é"),
 //		new StrPair("#252;", "ü"),
 //		new StrPair("#153;", "™"),
 //		new StrPair("#176;", "°"),
 //		new StrPair("#177;", "±"),
 //		new StrPair("#161;", "¡"),
 //		new StrPair("#162;", "¢"),
 //		new StrPair("#167;", "§"),
 //		new StrPair("#168;", "¨"),
 //		new StrPair("#169;", "©"),
 //		new StrPair("#225;", "á"),
 //		new StrPair("#224;", "à"),
 //		new StrPair("#230;", "æ"),
 //		new StrPair("#242;", "ò"),
 //		new StrPair("#246;", "ö"),
 //		new StrPair("#232;", "è"),
 //		new StrPair("#182;", "¶"),
 //		new StrPair("#175;", "¯"),
 //		new StrPair("raquo;", "»"),
 //		new StrPair("aring;", "å"),
 //		new StrPair("rarr;", "→"),
 //		new StrPair("iexcl;", "¡"),
 //		new StrPair("emsp;", ""),
 //		new StrPair("#x02013;", "-"),
 //		new StrPair("#x02014;", "-"),
 //		new StrPair("#x000A0;", " "),
 //		new StrPair("#x000F6;", "ö"),
 //		new StrPair("#x000FA;", "ú"),
 //		new StrPair("#x000C9;", "É"),
 //
 //		new StrPair("frac14;", "¼"),
 //		new StrPair("frac12;", "½"),
 //		new StrPair("para;", "¶"),
 //		new StrPair("frac34;", "¾"),
 //		new StrPair("times;", "×"),
 //		new StrPair("Ucirc;", "Û")
 //	};
 //	String newStr = arr[0];
 //	for (int i = 1; i < arr.length; i++) {
 //		for (int j = 0; j < matchList.length; j++) {
 //			if (arr[i].startsWith(matchList[j].first))
 //				newStr += "&" + matchList[j].second + arr[i].substring(matchList[j].first.length());
 //			else
 //				newStr += "&" + arr[i];
 //		}
 //	}
 //
 //	return newStr;
   }
 
 //  private String parseXML(String str) {
 //	  String start = "<arr name=\"text\"><str>";
 //	  String stop = "</str></arr>";
 //	 String fullText = trimBracketed(str, start, stop);
 //	 fullText = replaceMatch(fullText, "&lt;", "<");
 //	 fullText = replaceMatch(fullText, "&gt;", ">");
 //	 fullText = replaceMatch(fullText, "&amp;", "&");
 //	return fullText;
 //    }
 
 
 
   public boolean handleRole( String predicate, String object ) {
     if (predicate.startsWith("http://www.loc.gov/loc.terms/relators/")) {
       String role = predicate.substring("http://www.loc.gov/loc.terms/relators/".length());
       addField(doc, "role_" + role, object);
       return true;
     }
     return false;
   }
 
   
   public static ArrayList<String> parseYears(String value) {
     ArrayList<String> years = new ArrayList<String>();
 
     if ("unknown".equalsIgnoreCase(value.trim()) ||
         "uncertain".equalsIgnoreCase(value.trim())) {
       years.add("Uncertain");
     } else {
 
       // expand 184u to 1840-1849
       if (value.indexOf('u') != -1) {
         char[] yearChars = value.toCharArray();
         int numLength = value.length();
         int i, factor = 1, startPos = 0;
 
         if (numLength > 4) numLength = 4;
 
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
           String start, finish;
           if (commaPos == -1) {
             start = finish = range;
           } else {
             start = range.substring(0, commaPos);
             finish = range.substring(commaPos + 1);
           }
           if( start.length() >= 4 && finish.length() >= 4){
             years.addAll(enumerateYears(start.substring(0, 4), finish.substring(0, 4)));            
           }
         }
 
       }
     }
 
     return years;
   }
 
   public void addField(HashMap<String, ArrayList<String>> map, String name, String value) {
     // skip null fields
     if (value == null || name == null) return;
 
     // if the field is a url, check to see if it is reachable
     if ( config.collectLinks && value.trim().startsWith("http://") && value.trim().indexOf(" ") == -1 && !"uri".equals(name)) {
     	linkCollector.addLink(documentURI, filename, value);
     }
 
     addFieldEntry(map,name,value);
   }
   
   public static void addFieldEntry(HashMap<String, ArrayList<String>> map, String name, String value) {  
     // make sure we add to array for already existing fields
     if (map.containsKey(name)) {
       ArrayList<String> pastValues = map.get(name);
       pastValues.add(value);
       map.put(name, pastValues);
     } else {
       ArrayList<String> values = new ArrayList<String>();
       values.add(value);
       map.put(name, values);
     }
   }
 
   private String getFirstField(HashMap<String, ArrayList<String>> object, String field) {
 	ArrayList<String> objectArray = object.get(field);
 	if( objectArray != null ) {
 		return objectArray.get(0);
 	}
 	return "";
   }
 
   public HashMap<String, HashMap<String, ArrayList<String>>> getDocuments() {
 	// add author_sort: we do that here because we have a few different fields we look at and the order they appear shouldn't matter, so we wait to the end to find them.
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
 
 		// add fulltext and ocr indicators
 		ArrayList<String> objectArray = object.get("text");
 		if( objectArray != null ) {	// If we have a text field
 			if (object.get("has_full_text") == null)
 				addField(object, "has_full_text", "T");
 			objectArray = object.get("is_ocr");
 			if( objectArray == null )	// If we weren't told differently, then it is not an ocr object
 				addField(object, "is_ocr", "F");
 		} else {
 			if (object.get("has_full_text") == null)
 				addField(object, "has_full_text", "F");
 		}
 	}
     return documents;
   }
 
   private String fetchContent(String url) throws IOException {      
     GetMethod get = new GetMethod(url);
     int result;
     try {
       result = httpClient.executeMethod(get);
       if (result != 200) {
         throw new IOException(result+" code returned for URL: " + url );
       }
 
       BufferedInputStream bis = new BufferedInputStream(get.getResponseBodyAsStream());
       ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
       byte[] b = new byte[4096];
       int len;
 
       while ((len = bis.read(b)) > 0) {
         contentStream.write(b, 0, len);
       }
 
       String fullText = contentStream.toString("UTF-8");
       String cleanedFullText = fullText;
 
	  cleanedFullText = cleanText(fullText);
       return cleanedFullText;
     } finally {
       get.releaseConnection();
     }
   }
 
	public String cleanText(String fullText) {
 		// If the text contains markup, remove it.
 		// We may be passed plain text, or we may be passed html, so any strategy we use needs to work for both.
 		// We can assume that if it is plain text, it won't have stuff that looks like tags in it.
 		if (fullText == null)
 			return fullText;
 
		// remove everything between <head>...</head>
		fullText = removeTag(fullText, "head");
 
		// remove everything between <script>..</script>
		fullText = removeTag(fullText, "script");
 
		// remove everything between <...>
		//fullText = removeBracketed(fullText, "<", ">");
 
 		// Get rid of non-unix line endings
 		fullText = fullText.replaceAll("\r", "");
 
 		// remove all "&..;" encoding
 		//fullText = fullText.replaceAll("\\&[a-z]{1,5}\\;", " ");
 
 		// Clean up the file a little bit -- there shouldn't be two spaces in a row or blank lines
 		fullText = fullText.replaceAll("\t", " ");
 		fullText = fullText.replaceAll(" +", " ");
 		fullText = fullText.replaceAll(" \n", "\n");
 		fullText = fullText.replaceAll("\n ", "\n");
 		fullText = fullText.replaceAll("\n+", "\n");
 
 
 		//      if (fullText != null && fullText.indexOf("<") != -1) {
 		//        return fullText.replaceAll("\\<.*?\\>", "");
 		//      }
 		  return fullText;
 	}
 
   public static ArrayList<String> enumerateYears(String startYear, String endYear) {
     int y1 = Integer.parseInt(startYear);
     int y2 = Integer.parseInt(endYear);
 
     ArrayList<String> years = new ArrayList<String>();
     years.add(startYear);
     if (y2 <= y1) return years;
 
     for (int i = y1 + 1; i <= y2; i++) {
       years.add("" + i);
     }
 
     return years;
   }
 
   public void setFilename(String filename) {
     this.filename = filename;
   }
 
 	private String replaceMatch(String fullText, String match, String newText) {
 		int start = fullText.indexOf(match);
 		while (start != -1) {
 			fullText = fullText.substring(0, start) + newText + fullText.substring(start + match.length());
 			start = fullText.indexOf(match);
 		}
 		return fullText;
 	}
 
 	private String removeBracketed(String fullText, String left, String right) {
 		int start = fullText.indexOf(left);
 		while (start != -1) {
 			int end = fullText.indexOf(right, start);
 			if (end == -1) {
 				start = -1;
 			} else {
 				fullText = fullText.substring(0, start) + "\n" + fullText.substring(end + right.length());
 				start = fullText.indexOf(left);
 			}
 		}
 		return fullText;
 	}
 
 	private String trimBracketed(String fullText, String left, String right) {
 		int start = fullText.indexOf(left);
 		if (start == -1)
 			return "";
 		start += left.length();
 		int end = fullText.indexOf(right, start);
 		if (end == -1)
 			return "";
 		return fullText.substring(start, end);
 	}
 
 	private String removeTag(String fullText, String tag) {
 		return removeBracketed(fullText, "<" + tag, "</" + tag + ">");
 	}
 }
