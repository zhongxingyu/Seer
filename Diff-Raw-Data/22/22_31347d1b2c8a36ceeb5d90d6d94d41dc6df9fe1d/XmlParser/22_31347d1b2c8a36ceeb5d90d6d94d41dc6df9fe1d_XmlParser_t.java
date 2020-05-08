 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import java.util.Scanner;
 import java.awt.EventQueue;
 import java.io.File;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 /*  
 28 july 2013
 Authors: Alex Southwell
 Purpose: import/export/edit xml files for Legacy of Barubash
 */
  public class XmlParser
  {
         // Creates trigger objects
		public static trigger[] currentTrigger;
 		static additional[] currentAdditional;
 		static EditorUi currentUi;
 		
 	public static void main(String argv[])
 	{
 		
 		
 		EventQueue.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 			currentUi = new EditorUi();
 			importTriggerXml();
 			importAdditionalXml();
 			//displayAllAdditionals();
 			//editTriggerItems();
 			//searchTriggerName();
 			//exportXml();
 			populateAdditionalList();
 			populateTriggerList();
 			}
 		});
 		
 
 
 	}
 	
 	public static void populateTriggerList()
 	{
 		for (int i = 0; i<currentTrigger.length; i++)
 		{
 			currentUi.addTriggerListItem(currentTrigger[i].getTriggerName());
 		}
 		currentUi.triggersList.setSelectedIndex(0);
 	}
 	
 	public static void populateTItemList(String triggerName)
 	{
 		for (int i = 0; i<currentTrigger.length; i++)
 		{
 			if (currentTrigger[i].getTriggerName().equals(triggerName))
 			{
 				String currentArray[] = currentTrigger[i].getArrayItems();
 				for (int j = 0; j<currentArray.length; j++)	
 				{
 					EditorUi.addTItemListItem(currentArray[j]);
 				}
 			}
 		}
 		
 	}	
 	public static void populateAdditionalList()
 	{
 		for (int i = 0; i<currentAdditional.length; i++)
 		{
 			currentUi.addAdditionalListItem(currentAdditional[i].getAdditionalName());
 		}
 		currentUi.additionalsList.setSelectedIndex(0);
 	}
 	
 	public static void populateAItemList(String AdditionalName)
 	{
 		for (int i = 0; i<currentAdditional.length; i++)
 		{
 			if (currentAdditional[i].getAdditionalName().equals(AdditionalName))
 			{
 				String currentArray[] = currentAdditional[i].getArrayItems();
 				for (int j = 0; j< currentArray.length; j++)	
 				{
 					currentUi.addAItemListItem(currentArray[j]);
 				}
 			}
 		}
 		
 	}
 	
 	
     public static void importTriggerXml()
     {
          try {
         	 // Prepares the file for access
              File fXmlFile = new File("triggers.xml");
              DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
              Document doc = dBuilder.parse(fXmlFile);
           
              // optional, but recommended
              // read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
              doc.getDocumentElement().normalize();
              
              // Selects which nodelist
              NodeList nList = doc.getElementsByTagName("string-array");
                        // Creates trigger objects
              currentTrigger = new trigger[nList.getLength()];
              
              // Loops all of the lists
              for (int temp = 0; temp < nList.getLength(); temp++) 
              {
             	 // Creates an array to be filled with items
                  String[] arrayItems = new String[20];
                  
                  // Selects which node 
                  Node nNode = nList.item(temp);
                  
                  // Checks for correct node type
                  if (nNode.getNodeType() == Node.ELEMENT_NODE) 
                  {          
                 	 // Selects the element from the node
                 	 Element eElement = (Element) nNode;                
                      
                      // Loops all of the items in the node
                      for (int i=0; i<eElement.getElementsByTagName("item").getLength();i++)
                      {
                          // Stores item in the array
                     	 arrayItems[i]=eElement.getElementsByTagName("item").item(i).getTextContent();
                      }
                      
                      // Creates a new object with the Trigger name and array of items
                      currentTrigger[temp] = new trigger(eElement.getAttribute("name"),arrayItems);
                      
                  }
  
              }
             // currentUi.updateTriggersLabel(Integer.toString(triggerCounter));
          } catch (Exception e) 
          {
              e.printStackTrace();
          }
     }
     
     public static void importAdditionalXml()
     {
     	 int additionalCounter = 0;
          try {
         	 // Prepares the file for access
              File fXmlFile = new File("additionals.xml");
              DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
              Document doc = dBuilder.parse(fXmlFile);
           
              // optional, but recommended
              // read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
              doc.getDocumentElement().normalize();
              
              // Displays the node name
              System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
              
              // Selects which nodelist
              NodeList nList = doc.getElementsByTagName("string-array");
           
              System.out.println("----------------------------");
              
              // Creates additional objects
              currentAdditional = new additional[nList.getLength()];
              
              // Loops all of the lists
              for (int temp = 0; temp < nList.getLength(); temp++) 
              {                 
                  // Selects which node 
                  Node nNode = nList.item(temp);
                  
                  // Checks for correct node type
                  if (nNode.getNodeType() == Node.ELEMENT_NODE) 
                  {          
                 	 // Selects the element from the node
                 	 Element eElement = (Element) nNode;                
                      
                 	 // Creates an array to be filled with items
                      String[] arrayItems = new String[eElement.getElementsByTagName("item").getLength()];
                 	 
                      // Loops all of the items in the node
                      for (int i=0; i<eElement.getElementsByTagName("item").getLength();i++)
                      {
                          // Stores item in the array
                     	 arrayItems[i]=eElement.getElementsByTagName("item").item(i).getTextContent();
                      }
                      
                      // Creates a new object with the Trigger name and array of items
                      currentAdditional[temp] = new additional(eElement.getAttribute("name"),arrayItems);
                      
                      additionalCounter++;
                  }
  
              }
              System.out.println("Additional imported: "+ additionalCounter);
          } catch (Exception e) 
          {
              e.printStackTrace();
          }
     }
     
     public static void displayAllAdditionals()
     {
     	for (int i = 0; i<currentAdditional.length;i++)
     	{
     		String newAdditionalName = currentAdditional[i].getAdditionalName();
     		System.out.println(newAdditionalName);
     		String newArray[] = currentAdditional[i].getArrayItems();
     		for (int j =0; j< newArray.length; j++)
     		{
     			System.out.println(newArray[j]);
     		}
     	}
     }
 
     public static void searchAdditionalName()
     {
     	// addnpc,imagetype,x,y,trigger,npctype,triggerfrobs?,name,gotox,gotoy,triggerEndMotion,ai,direction,animation
 		
     }
     
     public static void exportXml()
     {
     	try
     	{// Creates a file for writing
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
         Document doc = docBuilder.newDocument();
         Element rootElement = doc.createElement("resources");
         doc.appendChild(rootElement);
                 
         // Loops the objects into the file
 	        for (int i = 0; i< currentTrigger.length; i++)
 	        {
 	            Element stringArray = doc.createElement("string-array");
 	            rootElement.appendChild(stringArray);
 	            stringArray.setAttribute("name", currentTrigger[i].getTriggerName());
 	            
 	    		String newArray[] = currentTrigger[i].getArrayItems();
 	    		
 	    		for (int j =0 ; newArray[j]!=null; j++)
 	    		{
 	    			Element item = doc.createElement("item");
 	    			item.appendChild(doc.createTextNode(newArray[j]));
 	    			rootElement.appendChild(item);
 	    		}
 	    		
 	        }	
 	    		// write the content into xml file
 	    		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 	    		Transformer transformer = transformerFactory.newTransformer();
 	    		DOMSource source = new DOMSource(doc);
 	    		StreamResult result = new StreamResult(new File("newtriggers.xml"));
 	     
 	    		// Output to console for testing
 	    		// StreamResult result = new StreamResult(System.out);
 	     
 	    		transformer.transform(source, result);
 	     
 	    		System.out.println("File saved!");
 	        
 	    	} catch (ParserConfigurationException pce) {
 	    		pce.printStackTrace();
 	    	} catch (TransformerException tfe) {
 	    		tfe.printStackTrace();
 	    	}
     }
 
     public static void editTriggerItems()
     {
         Scanner in = new Scanner(System.in);
         
         System.out.print("Enter trigger number: ");
         int requestedTrigger = in.nextInt();
         
         System.out.print("Enter item number: ");
         int requestedItem = in.nextInt();
         
         in.nextLine();
         
         String currentArray[] = currentTrigger[requestedTrigger].getArrayItems();
         
         System.out.println("TriggerName: " + currentTrigger[requestedTrigger].getTriggerName() + " | ItemNumber: " + requestedItem);
         System.out.println(currentArray[requestedItem]);
         
         System.out.print("Enter new item value: ");
         String editedItem = in.nextLine();
                
         System.out.println("New value: ");
         System.out.println(editedItem);
         
         System.out.print("Confirm (y/n): ");
         String confirmItem = in.nextLine();
         
         if (confirmItem.equals("y") || confirmItem.equals("Y"))
         {
         	currentArray[requestedItem]= editedItem;
         	currentTrigger[requestedTrigger].setArrayItems(currentArray);
         	String a[] = currentTrigger[requestedTrigger].getArrayItems();
         	System.out.println(a[requestedItem]);
         }
     }
 
    
  
  }
      
