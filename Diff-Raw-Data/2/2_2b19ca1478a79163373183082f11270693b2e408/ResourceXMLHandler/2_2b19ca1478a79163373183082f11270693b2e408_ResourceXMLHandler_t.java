 package org.isatools.novartismetastore.xml;
 
 import org.isatools.novartismetastore.resource.ResourceDescription;
 import uk.ac.ebi.utils.xml.XPathReader;
 
 import javax.xml.xpath.XPathConstants;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 public class ResourceXMLHandler {
 
     public static final String resourceFileLocation = "config/resource-description.xml";
 
     public ResourceDescription parseXML() {
         try {
             XPathReader reader = new XPathReader(new FileInputStream(resourceFileLocation));
 
             String name = (String) reader.read("/resource/name", XPathConstants.STRING);
             String abbreviation = (String) reader.read("/resource/abbreviation", XPathConstants.STRING);
             String queryURL = (String) reader.read("/resource/queryURL", XPathConstants.STRING);
 
             return new ResourceDescription(name, abbreviation, queryURL);
 
         } catch (FileNotFoundException e) {
            return new ResourceDescription("Repository", "REPO", "URL is unknown");
         }
     }
 
     public static void main(String[] args) {
         ResourceXMLHandler handler = new ResourceXMLHandler();
         ResourceDescription desc = handler.parseXML();
 
         System.out.println(desc.getResourceName());
     }
 }
