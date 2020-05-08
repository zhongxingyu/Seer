 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.schlueters.phpttestrunner.results.fromhtmloutput;
 
 import org.xml.sax.InputSource;
import java.io.*;
 
 import java.util.StringTokenizer;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.apache.xerces.parsers.DOMParser;
 
 import de.schlueters.phpttestrunner.results.Result;
 
 
 /**
  *
  * @author johannes
  */
 public class HTMLResult extends Result {
     public HTMLResult(File file) throws Exception {
         super(file);
     }
     
     protected void parse(File file) throws Exception {
         DOMParser parser = new DOMParser();
         parser.parse(new InputSource(new BufferedReader(new FileReader(file))));
         Document doc = parser.getDocument();
         String redirectBase = null;
         String replaceBase = null;
         
         NodeList trs = doc.getElementsByTagName("tr");
         for (int i = 0; i < trs.getLength(); i++) {
             Node tr = trs.item(i);
             NodeList tds = tr.getChildNodes();
             
             if (tds.getLength() == 6) {            
                 testExecuted(new HTMLTest(tds, redirectBase, replaceBase));
             } else if (tds.getLength() == 1) {
                 StringTokenizer tokenzier = new StringTokenizer(tds.item(0).getFirstChild().getNodeValue());
                 
                 assert(tokenzier.nextToken().equals("--->"));
                 if (redirectBase == null) {
                     redirectBase = tokenzier.nextToken();
                     assert(tokenzier.nextToken().charAt(0) == '(');
                     replaceBase = tokenzier.nextToken().substring(1);
                     replaceBase = replaceBase.substring(0, replaceBase.lastIndexOf("/"));
                 } else {
                     redirectBase = null;
                 }
             }
         }
     }
 }
