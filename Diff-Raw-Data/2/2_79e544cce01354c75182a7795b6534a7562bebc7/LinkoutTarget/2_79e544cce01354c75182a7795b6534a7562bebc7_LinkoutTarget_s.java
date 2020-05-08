 /*
  * NCBI Linkout generator for Dryad
  *
  * Created on May 2, 2012
  * Last updated on Oct 24, 2012
  * 
  */
 package org.datadryad.interop;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.SimpleDateFormat;
 import java.util.HashSet;
 import java.util.Set;
 
 
 
 public abstract class LinkoutTarget {
     
     
     final static int INDENTSIZE = 3;
     
    final static String ICONURL = "http://www.nescent.org/wg/dryad/images/7/7f/DryadLogo-Button.png";
     final static String DISCOVERBASE = "http://datadryad.org/discover?";
     final static String DISCOVERRULE = "query=" + "%22&lo.doi;%22";
     final static String DTDROOTELEMENT = "LinkSet";
     final static String DTDPUBLICID = "-//NLM//DTD LinkOut 1.0//EN";
     final static String DTDURL = "http://www.ncbi.nlm.nih.gov/entrez/linkout/doc/LinkOut.dtd";
     
     final static String DRYADPROVIDERID = "7893";
 
     final static SimpleDateFormat DATEF = new SimpleDateFormat("yyyy-MM-dd");
     
     final Set <DryadPackage> packages = new HashSet<DryadPackage>();
     
 
     void addPackage(DryadPackage pkg) {
         packages.add(pkg);
         
     }
     
     
     //NCBI seems to require query=&lo.doi;" which isn't really legal xml, so xom is out...
     abstract void save(String targetFile) throws IOException;
 
     
     String generateProviderId(){
         StringBuilder result = new StringBuilder(60);
         result.append(getIndent(2));
         result.append("<ProviderId>");
         result.append(DRYADPROVIDERID);
         result.append("</ProviderId>\n");
         return result.toString();
     }
     
     String generateIconUrl(){
         StringBuilder result = new StringBuilder(60);
         result.append(getIndent(2));
         result.append("<IconUrl>");
         result.append(ICONURL);
         result.append("</IconUrl>\n");
         return result.toString();
     }
     
     String generateDatabase(String dbName){
         StringBuilder result = new StringBuilder(30);
         result.append(getIndent(3));
         result.append("<Database>");
         result.append(dbName);
         result.append("</Database>\n");
         return result.toString();
     }
 
     String generateSubjectType(){
         return getIndent(3)+"<SubjectType>supplemental materials</SubjectType>\n";
     }
     
     protected String generateObjIdElement(String dbId){
         StringBuilder result = new StringBuilder(40);
         result.append(getIndent(4));
         result.append("<ObjId>");
         result.append(dbId);
         result.append("</ObjId>");
         return result.toString();
     }
     
     
     abstract String generateBase();   
     
     final private static String indents = "     ";
     String getIndent(int indentCount){
         if (indentCount == 0)
             return "";
         if (indentCount == 1)
             return indents.substring(0,INDENTSIZE);
         StringBuilder result = new StringBuilder(INDENTSIZE*indentCount);
         for(int i = 0; i<indentCount; i++){
             result.append(indents.substring(0,INDENTSIZE));
         }
         return result.toString();
     }
     
 }
