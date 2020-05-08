 package nl.mdlware.confluence.plugins.citation;
 
 public class XMLDocumentWrapper {
     public static String wrapIntoValidXML(String pageContents) {
        return "<?xml version=\"1.0\"?><!DOCTYPE some_name [<!ENTITY nbsp \"&#160;\"><!ENTITY ndash   \"&#8211;\"><!ENTITY mdash   \"&#8212;\"><!ENTITY rsquo   \"&#8217;\">  ]><p>" + pageContents + "</p>";
     }
 }
