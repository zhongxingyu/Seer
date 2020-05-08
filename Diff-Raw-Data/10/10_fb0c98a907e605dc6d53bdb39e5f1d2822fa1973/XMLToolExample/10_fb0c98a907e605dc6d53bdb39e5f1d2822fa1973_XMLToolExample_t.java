 package util.XMLToolExample;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.w3c.dom.Element;
 import util.XMLTool;
 
 
 /**
  * This is example code shows how to use the XMLtool.
  * 
  * @author Yoshida
  * 
  */
 public class XMLToolExample {
     private static final String SAVING_LOCATION = "/src/util/XMLToolExample/";
     private static final String FILENAME = "XMLTest.xml";
     
     private static XMLTool myDoc;
     private static XMLTool myReadDoc;
     
     /**
      * Loads the XMLTool tester methods.
      * 
      * @param args
      */
     public static void main (String args[]) {
         // makes the file;
         makeXMLFile();
         // Writes the document
         myDoc.writeFile(SAVING_LOCATION + FILENAME);
         // Reads the file
         readFile();
     }
     
     private static void makeXMLFile () {
         // Step 1: Create a new XML document
         myDoc = new XMLTool();
         
         // Step 2: Creates a new root with the desired tag. Import org.w3c.dom.Element
         Element earth = myDoc.makeRoot("Earth");
         
         /*
          * Step 3: Make Elements. The XML file can be viewed as a tree data structure. Those
          * elements are the nodes of the XML tree. There are three basic ways to create an Element.
          */
         
         /*
          * Creating a new node without a value. Later on any node must be appended to the tree with
          * the root. Otherwise, the element will not show up in the XML file.
          */
         Element PrimateElement = myDoc.makeElement("Primates");
         
         /*
          * Creating a new node with a value. However if the element is not a leaf, the value
          * will be lost it the file. In this case, "Homo sapiens" will be lost in the document.
          * Create an new element, such as "species" with a value "Homo sapiens"
          */
         Element humanElement = myDoc.makeElement("Human", "Homo sapiens");
         
         // Creating a HashMap with data for Robert C. Duvall
         Map<String, String> profMap = new HashMap<String, String>();
         profMap.put("name", "Robert C. Duvall");
         profMap.put("fingers", "10");
         profMap.put("eyes", "2");
         profMap.put("occupation", "writing good code");
         // Creating a HashMap with data for Robert S. Duvall
         Map<String, String> actorMap = new HashMap<String, String>();
         actorMap.put("name", "Robert S. Duvall");
         actorMap.put("fingers", "10");
         actorMap.put("eyes", "2");
         // Creating a HashMap with the number of artistic awards of Robert S. Duvall...
         Map<String, String> awardsMap = new HashMap<String, String>();
         awardsMap.put("oscar", "1");
         awardsMap.put("emmy", "2");
         awardsMap.put("goldenGlobe", "4");
         awardsMap.put("bafta", "1");
         
         /*
          * Creating an element from a Map. This method is particularly useful when working with
          * a high amount of information to be recorded in the XML file.
          */
         Element profElement = myDoc.makeElementsFromMap("RobertC", profMap);
         Element actorElement = myDoc.makeElementsFromMap("RobertS", actorMap);
         
         /*
          * Step 4: adding children to parent elements. This step can be done with the previous step.
          * There are three ways to append a child to a parent element and one way to do it in bulk
          * using a HashMap.
          */
         
         /*
          * It is possible to add a child element to a parent element with:
          * one child element,
          * a child tag String (this creates an empty node),
          * a child tag and value Strings.
          */
         myDoc.addChild(earth, humanElement);
         myDoc.addChild(profElement, "language", "Java");
         myDoc.addChild(humanElement, profElement);
         myDoc.addChild(humanElement, actorElement);
         
         /*
          * Another way to add multiple children elements is to use a Map.
          */
         Element awardsElement = myDoc.makeElement("awards");
         myDoc.addChildrenFromMap(awardsElement, awardsMap);
         myDoc.addChild(actorElement, awardsElement);
         
     }
     
     private static void readFile () {
         /*
          * The reading process is initialized with the constructor of the XMLTool. The XMLTool will
          * automatically read and translate the XML file at the destination.
          */
         myReadDoc = new XMLTool(SAVING_LOCATION + FILENAME);
         
         /*
          * There are many different ways to find and get data from the XMl file using the XMLTool.
          * This tool can handle getting elements from a tag in the entire document or in a parent
          * element in the format of a single element or a List of elements. It can get the content
          * from element in various forms.
          */
         
         // getting one unique element from the XML file.
         Element bobElement = myReadDoc.getElement("RobertC");
         
         /*
          * If you are worried there could be more than one instance of that variable in the same
          * file, use ask the tool to return a list, as follows.
          */
         List<Element> humanElements = myReadDoc.getElementList("Human");
         
         /*
          * It is also possible to get the content of an element using the XMLTool.
          * "language" is the tag.
          */
         System.out.println("The content of the element " + "language" + " is: " +
                            myReadDoc.getContent("language"));
         
         // The XMLTool also lets you get a Map that was initially added to the file.
         Map<String, String> bobMap = new HashMap<String, String>();
         bobMap = myReadDoc.getChildrenStringMap(bobElement);
         System.out.println(bobMap.toString());
         
     }
}
