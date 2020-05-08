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
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 import org.openrdf.rio.ParseErrorListener;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.rdfxml.RDFXMLParser;
 
 public class RdfDocumentParser {
     private static long largestTextSize = 0;
     public final static Logger log = Logger.getLogger(RdfDocumentParser.class.getName());
 
     public static long getLargestTextSize() {
         return largestTextSize;
     }
 
     public static HashMap<String, HashMap<String, ArrayList<String>>> parse(final File file, ErrorReport errorReport,
             LinkCollector linkCollector, RDFIndexerConfig config) throws IOException {
 
         largestTextSize = 0;
         RDFXMLParser parser = new RDFXMLParser();
         NinesStatementHandler statementHandler = new NinesStatementHandler(errorReport, linkCollector, config);
         statementHandler.setFilename(file.getName());
 
         parser.setRDFHandler(statementHandler);
         parser.setParseErrorListener(new ParseErrorListener() {
             public void warning(String string, int i, int i1) {
                 log.info("warning = " + string);
             }
 
             public void error(String string, int i, int i1) {
                 log.info("error = " + string);
             }
 
             public void fatalError(String string, int i, int i1) {
                 log.info("fatalError = " + string);
             }
         });
         parser.setVerifyData(true);
         parser.setStopAtFirstError(true);
 
         // parse file
         try {
             Charset cs = Charset.availableCharsets().get("UTF-8");
             CharsetDecoder decoder = cs.newDecoder();
             InputStreamReader is = new InputStreamReader(new FileInputStream(file), decoder);
             parser.parse(is, "http://foo/" + file.getName());
 
         } catch (RDFParseException e) {
             errorReport.addError(new IndexerError(file.getName(), "", "Parse Error on Line " + e.getLineNumber() + ": "
                     + e.getMessage()));
         } catch (RDFHandlerException e) {
             errorReport.addError(new IndexerError(file.getName(), "", "StatementHandler Exception: " + e.getMessage()));
        } catch (MalformedInputException me) {
            // can't continue with this; just return a null now
            errorReport.addError(new IndexerError(file.getName(), "", "Contains invalid characters. Unable to parse"));
            return null;
         } catch (Exception e) {
             errorReport.addError(new IndexerError(file.getName(), "", "RDF Parser Error: " + e.getMessage()));
         }
 
         // retrieve parsed data
         HashMap<String, HashMap<String, ArrayList<String>>> docHash = statementHandler.getDocuments();
 
         // process tags
         Collection<HashMap<String, ArrayList<String>>> documents = docHash.values();
         for (HashMap<String, ArrayList<String>> document : documents) {
 
             // normalize tags, replace spaces with dashes, lowercase
             ArrayList<String> tags = document.remove("tag");
             if (tags != null) {
                 for (int i = 0; i < tags.size(); i++) {
                     String tag = tags.get(i);
                     tag = tag.toLowerCase();
                     tag = tag.replaceAll(" ", "-");
                     tags.set(i, tag);
                 }
                 // username is archive name
                 String archive = document.get("archive").get(0);
                 ArrayList<String> nameList = new ArrayList<String>();
                 nameList.add(archive);
                 document.put("username", nameList);
                 document.put(archive + "_tag", tags);
             }
         }
 
         largestTextSize = statementHandler.getLargestTextSize();
         return docHash;
     }
 }
