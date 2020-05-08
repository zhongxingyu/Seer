 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import helpers.Helpers;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.util.List;
 import javax.persistence.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import net.sf.saxon.lib.NamespaceConstant;
 import play.db.jpa.GenericModel;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * 
  * Text-refs which are shown in popups are kept in this model
  * 
  */
 @Entity
 public class TextReference extends GenericModel {
 
     @Id
     @SequenceGenerator(name = "reference_id_seq_gen", sequenceName = "reference_id_seq")
     @GeneratedValue(generator = "reference_id_seq_gen")
     public long id;
     @Lob
     public String textId;
     public long alternativTo;
     @Lob
     public String showName;
     @Lob
     public String type;
     @Lob
     public String fileName;
 
     public TextReference(String textId, long alternativeTo, String showName, String type) {
         this.textId = textId;
         this.alternativTo = alternativeTo;
         this.showName = showName;
         this.type = type;
     }
 
     public TextReference(String textId, long alternativeTo, String showName, String type, String fileName) {
         this.fileName = fileName;
         this.textId = textId;
         this.alternativTo = alternativeTo;
         this.showName = showName;
         this.type = type;
     }
 
     /**
      * Return a xml-file as a xml-doc
      */
     private static Document fileAsXml(File file) {
         Document doc = null;
         try {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             doc = db.parse(file);
             doc.getDocumentElement().normalize();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return doc;
     }
 
     /**
      * delete old refs when uploading new reference-file
      */
     private static void deleteOldRererences(String fileName, String type) {
         Query query = TextReference.em().createQuery("delete from TextReference r where r.type like :name");
         query.setParameter("name", type);
         int deleted = query.executeUpdate();
         System.out.println("Deleted " + deleted + " old references");
     }
 
     
     /**
      * 
      * Handle upload of comments shown in the the comments-tab
      * Each comment is kept precompiiled and separately in the database and the complete comments-doc is concatenated
      * If the strucure of com-files it chaged, this function must be updated
      * 
      * 
      */
     public static void uploadComments(Asset asset) {
         System.out.println("Processing asset: " + asset.html);
         int del = TextReference.delete("fileName =  ? and type = ?", asset.fileName, Asset.commentType);
         System.out.println("Number of deleted comments: " + del);
         Document doc = Helpers.stringToNode(asset.html);
         List<Node> refs = Helpers.getChildrenOfType(doc, "div");
         StringBuilder strB = new StringBuilder();
         int i = 0;
         for (Node row : refs) {
             // String id = asset.fileName.replace("_com.xml", "") + "_" + row.getAttributes().getNamedItem("id").getNodeValue();
             // System.out.println("Processing row: " + Helpers.nodeToString(row));
             if ((row.getAttributes().getNamedItem("class") != null) &&  (row.getAttributes().getNamedItem("class").getNodeValue().contains("about"))) 
                 strB.append(Helpers.nodeToString(row));
             if ((row.getAttributes().getNamedItem("class") != null) &&  (row.getAttributes().getNamedItem("class").getNodeValue().contains("litList"))) 
                 strB.append(Helpers.nodeToString(row));                        
             if (row.getAttributes().getNamedItem("id") == null) continue;
             i++;
             String id = "scrollTarget" + "_" + asset.fileName.replace("_com.xml", "") + "_" + row.getAttributes().getNamedItem("id").getNodeValue();
 
             row.getAttributes().getNamedItem("id").setNodeValue(id);
             String content = Helpers.nodeToString(row);
             content = content.replaceFirst("xmlns.*?xhtml.", "");
             TextReference textRef = new TextReference(id, -1, content, Asset.commentType, asset.fileName);
             textRef.save();
             strB.append(content);
         }
         System.out.println("Number of comments in file: " + i);
 
         asset.html =  strB.toString();
         asset.save();
     }
 
     /**
      * Extract text-references to be shown i popups
      * Each ref is kept separately and precompiled in the database
      * 
      */
     // check if dtd sequence is set, this is an assumption here
     public static void uploadReferenceFile(Asset asset) {
         // System.out.println("Uploading reference-file: " + asset.html);
         TextReference.delete("type = ?", asset.type);
         try {
             Document doc = Helpers.stringToNode(asset.html);
             List<Node> refs = Helpers.getChildrenOfType(doc, "div");
             System.out.println("Number of refs: " + refs.size());
             // System.out.println("Trasformed refs as html: " + asset.html);
             for (Node ref : refs) {
                if(ref.getAttributes().getNamedItem("id") == null) continue; // do not create post for altName
                 String id = ref.getAttributes().getNamedItem("id").getNodeValue();
                 System.out.println("Creating ref-id: " + id);
                 System.out.println("  " + Helpers.nodeToString(ref));
                 TextReference textRef = new TextReference(id, -1, Helpers.nodeToString(ref), asset.type, asset.fileName);
                 // System.out.println("Ref: " + textRef.showName);
                 textRef.save();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     
     public static void uploadReferenceFilePlace(Asset asset) {
         // System.out.println("Uploading reference-file: " + asset.html);
         TextReference.delete("type = ?", asset.type);
         try {
             Document doc = Helpers.stringToNode(asset.html);
             List<Node> refs = Helpers.getChildrenOfType(doc, "div");
             System.out.println("Number of possible refs: " + refs.size());
             for (Node ref : refs) {
                 if (ref.getAttributes().getNamedItem("id") == null) {
                     System.out.println("not found id");
                     continue;
                 } else System.out.println("Found id");
                 String id = ref.getAttributes().getNamedItem("id").getNodeValue();
                 TextReference textRef = new TextReference(id, -1, Helpers.nodeToString(ref), asset.type, asset.fileName);
                 textRef.save();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     public static void uploadReferenceFileBible(Asset asset) {
         TextReference.delete("type = ?", asset.type);
         try {
             Document doc = Helpers.stringToNode(asset.xml);            
             XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//*:rs");
             NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
             System.out.println("------- Number of bible-references found: " + nodes.getLength());
             for (int i = 0; i < nodes.getLength(); i++) {
                 Node n = nodes.item(i);
                 String ref = n.getAttributes().getNamedItem("key").getNodeValue();
                 String html = "<div class='bibleref'>" + ref + "</div>";
                 TextReference textRef = new TextReference(ref, -1, html, asset.type, asset.fileName);
                 textRef.save();
             }
             System.out.println("Number of bible refs: " + TextReference.find("type = ?", asset.type).fetch().size());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     
 
     /**
      * Get a reference based on its id
      * Used from ajax-lookups
      * @return ref as html
      * 
      */
     public static String getReference(String textId) {
         TextReference ref = TextReference.find("textId = ?", textId).first();
         if (ref != null) {
             System.out.println("Refname: " + ref.showName);
         } else {
             System.out.println("Not found myth: " + textId);
         }
         return ref != null ? ref.showName : "";
     }
 }
