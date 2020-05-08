 package nl.mdlware.confluence.plugins.citation;
 
 public class XMLDocumentWrapper {
     public static String wrapIntoValidXML(String pageContents) {
        return "<?xml version=\"1.0\"?><!DOCTYPE some_name [<!ENTITY nbsp \"&#160;\">]><p>" + pageContents + "</p>";
     }
 }
