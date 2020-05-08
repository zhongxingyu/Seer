 /**
  * Copyright (C) 2011-
  *
  * All rights reserved.
  */
 package gov.va.vinci.v3nlp;
 
 import org.apache.commons.validator.GenericValidator;
 import org.w3c.dom.Document;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.*;
 
 /**
  * Non-nlp utilities.
  */
 public class Utilities {
     public static String getUsernameAsDirectory(String userToken) {
         if (GenericValidator.isBlankOrNull(userToken)) {
             return "";
         } else {
            return XORCipher.decode(userToken).replace("\\", "-") + "/";
         }
     }
 
     public static String getUsername(String userToken) {
         if (GenericValidator.isBlankOrNull(userToken)) {
             return "";
         } else {
            return XORCipher.decode(userToken);
         }
     }
 
     public static String transformDoc(Document doc) throws Exception{
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
             //initialize StreamResult with File object to save to file
             StreamResult result = new StreamResult(new StringWriter());
             DOMSource source = new DOMSource(doc);
             transformer.transform(source, result);
 
             return result.getWriter().toString();
     }
 
     public static void serializeObject(String path, Object e) {
         OutputStream os;
         try {
             os = new FileOutputStream(path);
             ObjectOutput oo = new ObjectOutputStream(os);
             oo.writeObject(e);
             oo.close();
         } catch (Exception e1) {
             e1.printStackTrace();
         }
     }
 }
