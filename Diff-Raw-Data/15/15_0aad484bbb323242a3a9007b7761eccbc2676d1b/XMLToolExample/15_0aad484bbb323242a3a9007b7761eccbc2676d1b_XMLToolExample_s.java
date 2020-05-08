package vooga.towerdefense.util;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.w3c.dom.Element;
 
 
 /**
  * This is example code shows how to use the XMLtool.
  * 
  * @author Yoshida
  * 
  */
 public class XMLToolExample {
     private static XMLTool myDoc;
     private static XMLTool myReadDoc;
     
     /**
      * Loads the XMLTool tester methods.
      * @param args
      */
     public static void main (String args[]) {
         // makes the file;
         makeXMLFile();
         // Writes the document
         myDoc.writeFile("XMLExample.xml");
         // Reads the file
         readFile();
     }
     
     private static void makeXMLFile () {
         // Creates a new XML document
         myDoc = new XMLTool();
         
         // Creates a new root. Import org.w3c.dom.Element
         Element earth = myDoc.makeRoot("Earth");
         
         // Creates a new node without a value. This node will not show up because it was not
         // appended to the tree.
         Element PrimateElement = myDoc.makeElement("Primates");
         
         /*
          * Creates a new node with a value. Note that the element Human is not a leaf in this
          * example. Thus, the value "Homo sapiens" is lost it the file. Do not do this!
          * Create an new element, such as "species" with a value "Homo sapiens"
          */
         Element humanElement = myDoc.makeElement("Human", "Homo sapiens");
         
         // Creates a HashMap with some stuff in it.
         Map<String, String> duvallMap = new HashMap<String, String>();
         duvallMap.put("fingers", "10");
         duvallMap.put("eyes", "2");
         duvallMap.put("language", "Java");
         duvallMap.put("occupation", "writing good code");
         
         // Creates a human with certain characteristics in the map
         Element duvallElement = myDoc.makeElementsFromMap("Robert", duvallMap);
         
         // Add/append Elements
         myDoc.addChildElement(earth, humanElement);
         myDoc.addChildElement(humanElement, duvallElement);
         
     }
     
     private static void readFile () {
         myReadDoc = new XMLTool();
         myReadDoc.setDoc("XMLExample.xml");
         
         // getting one unique element form the XML file.
         Element bobElement = myReadDoc.getElementFromTag("Robert");
         Element languageElement = myReadDoc.getElementFromTag("language");
         // getting the value from an element.
         System.out.println("The content of the element " + languageElement.getTagName() + " is: " +
                            languageElement.getTextContent());
         // It is also possible to get the content of an element using the XMLTool.
         // Note that the XMLTool has a slightly different implementation.
         System.out.println("The content of the element " + "language" + " is: " +
                            myReadDoc.getContentFromTag("language"));
         // The XMLTool also lets you get a Map that was initially added to the file.
         Map<String, String> bobMap = new HashMap<String, String>();
         bobMap = myReadDoc.getMapFromParentElement(bobElement);
         System.out.println(bobMap.toString());
         
     }
 }
