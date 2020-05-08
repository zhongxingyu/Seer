 /*
  * NCBI Linkout generator for Dryad
  *
  * Created on Oct 18, 2012
  * Last updated on Oct 24, 2012
  * 
  */
 package org.datadryad.interop;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Date;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * This class handles links to other databases (e.g., genbank, taxonomy, etc.) 
  */
 public class OtherTarget extends LinkoutTarget {
 
 
     final static String RESOURCEBASE = "http://datadryad.org/resource/";
 
    private final static int OBJECTLIMIT = 150000;
 
     private int linkCount = 0;   //incremented for each generated Link
     private int objectCount = OBJECTLIMIT;  //incremented for each generated ObjId
     private int fileCount = 1; 
 
 
     @Override
     void save(String targetFile) throws IOException{
         FileOutputStream s = null;
         OutputStreamWriter w = null;
         for (DryadPackage pkg : packages){
             if (pkg.hasSeqLinks()){                           
                 for (String dbName : pkg.getSeqDBs()){
                     if (objectCount >= OBJECTLIMIT){
                         if (w != null){
                             w.append(closeLinkSet());
                             w.close();
                         }
                         objectCount = 0;
                         String nextFile = targetFile + fileCount + ".xml";
                         fileCount++;
                         s = new FileOutputStream(nextFile, false);
                         w = new OutputStreamWriter(s);
                         w.append(generateHeader());
                         w.append(openLinkSet());
                     }
                     w.append(generateLink(pkg,dbName,generateProviderId(),generateIconUrl()));
                 }
             }
         }
         if (w != null){
             w.append(closeLinkSet());
             w.close();
         }
     }
 
 
     private String generateHeader(){
         final StringBuilder header = new StringBuilder();
         header.append("<?xml version=" + '"' + "1.0" + '"' + " encoding=" + '"' + "UTF-8" + '"' + "?>\n");
         header.append("<!DOCTYPE " + DTDROOTELEMENT + " PUBLIC ");
         header.append('"' + DTDPUBLICID + '"');
         header.append(" " + '"' + DTDURL + '"' + ">\n");
         return header.toString();
     }
 
 
     String openLinkSet(){
         return "<" + DTDROOTELEMENT + ">\n";
     }
 
     String closeLinkSet(){
         return "</" + DTDROOTELEMENT + ">\n";       
     }
 
 
     private String generateLink(DryadPackage pkg, String db, String provider, String iconURL){
         final StringBuilder result = new StringBuilder(500);
         result.append(getIndent(1));        
         result.append("<Link>\n");
         result.append(generateLinkId());
         result.append(provider);
         result.append(iconURL);
         result.append(generateObjectSelector(pkg,db));
         result.append(generateObjectUrl(pkg));
         result.append(getIndent(1));        
         result.append("</Link>\n");
         return result.toString();
     }
 
     private String generateLinkId(){
         final StringBuilder result = new StringBuilder(60);
         result.append(getIndent(2));        
         result.append("<LinkId>");
         result.append("dryad.seq.");
         result.append(DATEF.format(new Date()));
         result.append(".");
         result.append(Integer.toString(linkCount++));
         result.append("</LinkId>\n");
         return result.toString();
     }
 
 
 
 
     //TODO what a package that has references from multiple databases
     private String generateObjectSelector(DryadPackage pkg,String db){
         final StringBuilder result = new StringBuilder(300);
         result.append(getIndent(2));
         result.append("<ObjectSelector>\n");
         result.append(generateDatabase(db));
         result.append(generateObjectList(pkg,db));
         result.append(getIndent(2));
         result.append("</ObjectSelector>\n");
         return result.toString();
     }
 
 
     private String generateObjectList(DryadPackage pkg,String db){
         final StringBuilder result = new StringBuilder(120);
         result.append(getIndent(3));
         result.append("<ObjectList>\n");
         for (SequenceRecord sr : pkg.getSeqLinksforDB(db)){
             objectCount++;
             result.append(generateObjIdElement(sr.getID()));
             result.append("\n");   //insert xml comment here??
         }
         result.append(getIndent(3));
         result.append("</ObjectList>\n");
         return result.toString();
     }
 
 
 
     String generateObjectUrl(DryadPackage pkg){
         final StringBuilder result = new StringBuilder(120);
         result.append(getIndent(2));
         result.append("<ObjectUrl>\n");
         result.append(generateBase()); 
         result.append(generateRule(pkg));
         result.append(generateSubjectType());
         result.append(getIndent(2));
         result.append("</ObjectUrl>\n");
         return result.toString();
     }
 
     String generateBase(){
         StringBuilder result = new StringBuilder(30);
         result.append(getIndent(3));
         result.append("<Base>");
         result.append(RESOURCEBASE);
         result.append("</Base>\n");
         return result.toString();
     }
 
 
     String generateRule(DryadPackage pkg){
         final StringBuilder result = new StringBuilder(30);
         result.append(getIndent(3));
         result.append("<Rule>");
         result.append(StringEscapeUtils.escapeHtml(pkg.getDOI()));
         result.append("</Rule>\n");
         return result.toString();
     }
 
 
 }
