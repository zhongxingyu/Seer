 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers;
 
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import models.Asset;
 import models.Chapter;
 import models.TextReference;
 import play.mvc.Controller;
 
 /**
  *
  * 
  * Starting point for uploading of xml-files
  * 
  */
 public class UploadXml extends Application {
 
     public static void testXsltTransformation() throws Exception {
         // inline to test xslt of simple file
         // xmlToHtml("/home/pe/gruntvig/xml/1826_433A_1_v1.xml");
         render();
     }
 
     public static void uploadForm() {
         render();
     }
 
     /**
      * Handle upload of xml-theFile
      * Check for theFile-type and create assets in database
      * 
      */
     public static void uploadFile(String filesname, String comment, File epub) {
         File theFile = epub;
         System.out.println("Starting upload of: " + filesname);
         Asset asset = null;
         String fileName = theFile.getName();
         if (fileName.endsWith(".jpg")) {
             if (fileName.contains("_medium") || fileName.contains("_low")) {
                 Asset.uploadCountryImage(fileName, comment, theFile);
             } else {
                 Asset.uploadImage(filesname, comment, theFile);
             }
         } else {
             asset = Asset.uploadXmlFile(filesname, comment, theFile);
         }
         if (fileName.equals("place.xml")) {
             TextReference.uploadReferenceFilePlace(asset);
         } else if (fileName.equals(("bible.xml"))) {
             TextReference.uploadReferenceFileBible(asset);
         } else if (fileName.equals("myth.xml")
                 || fileName.equals("pers.xml")
                 ) {
             TextReference.uploadReferenceFile(asset);
         } else if (fileName.replace(".xml", "").endsWith("_com")) {
             TextReference.uploadComments(asset);
         }       
         
         if (asset == null) {
             Controller.renderHtml("Upload of file done: ");
         } else {
             render(asset);
         }
     }
 }
