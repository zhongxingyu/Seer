 package Model;
 
 import java.io.*;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 import org.w3c.dom.*;
 
 //This class, when instantiated and passed an airport; takes the airports runways and their
 //values and creates an XML file with them. It provides a JFileChooser window to select where
 //to save the file and how to name it.
//testing
 public class SaveToXMLFile {
 
 	File file;
 	DocumentBuilderFactory documentBuilderFactory;
 	DocumentBuilder documentBuilder;
 	Document document;
 	Element rootElement;
 	
 	//constructor for Airport
 	public SaveToXMLFile(Airport a) throws Exception {
 
 		
 		
 		String root = "Airport";
 		documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		documentBuilder = documentBuilderFactory.newDocumentBuilder();
 		document = documentBuilder.newDocument();
 		rootElement = document.createElement(root);
 		document.appendChild(rootElement);
 		
 		//First element, the airport's name
 		Element airportName = document.createElement("AirportName");
 		airportName.appendChild(document.createTextNode(a.getName()));
 		rootElement.appendChild(airportName);
 		
 		
 		this.addNodesAndElements(a);
 		
 		//Creating JFileChooser object and storing its return value
 		this.createFChooserAndStore();
 		
 	}//end of constructor
 	
 	public void addNodesAndElements(Airport a) {
 		
 		int numberOfRunways = a.runways().size(); //number of physical runways
 
 		for (int j = 0; j < numberOfRunways; j++) { // looping through all
 													// physical runways
 
 			Element physicalRunway = document.createElement("PhysicalRunway");
 			String nam = ((PhysicalRunway) a.runways().get(j)).getId();// name of the physical runway
 			// physicalRunway.appendChild(document.createTextNode(nam));
 
 			Element prName = document.createElement("Name");
 			prName.appendChild(document.createTextNode(nam));
 			physicalRunway.appendChild(prName);
 
 			for (int i = 0; i < 2; i++) { // looping through each actual runway (2)
 				Runway r = (Runway) a.runways().get(j).getRunway(i);// getting a runway
 				
 				// Creating runway element and appending to root element
 				Element em = document.createElement("Runway");
 
 				// Creating each of the runway's elements and appending to the runway element
 				Element name = document.createElement("RunwayName");
 				name.appendChild(document.createTextNode(r.getName()));
 				em.appendChild(name);
 
 				Element tora = document.createElement("TORA");
 				String to = Double.toString(r.getTORA(1));// getting the tora value that can be modified
 				tora.appendChild(document.createTextNode(to));
 				em.appendChild(tora);
 
 				Element asda = document.createElement("ASDA");
 				String as = Double.toString(r.getASDA(1));
 				asda.appendChild(document.createTextNode(as));
 				em.appendChild(asda);
 
 				Element toda = document.createElement("TODA");
 				String tod = Double.toString(r.getTODA(1));
 				toda.appendChild(document.createTextNode(tod));
 				em.appendChild(toda);
 
 				Element lda = document.createElement("LDA");
 				String ld = Double.toString(r.getLDA(1));
 				lda.appendChild(document.createTextNode(ld));
 				em.appendChild(lda);
 
 				Element displacedThreshold = document
 						.createElement("DisplacedThreshold");
 				String dt = Double.toString(r.getDisplacedThreshold(1));
 				displacedThreshold.appendChild(document.createTextNode(dt));
 				em.appendChild(displacedThreshold);
 
 				physicalRunway.appendChild(em);
 				// rootElement.appendChild(em);
 			}// end of loop
 
 			rootElement.appendChild(physicalRunway);
 		}// end of loop
 
 	}
 	
 	public void createFChooserAndStore() throws IOException, TransformerException {
 
 		JFileChooser fc = new JFileChooser();
 
 		int returnVal = fc.showSaveDialog(null);
 
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = fc.getSelectedFile();
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
 			StreamResult result = new StreamResult(file);
 			file.createNewFile();
 			DOMSource source = new DOMSource(document);
 			transformer.transform(source, result);
 		} else {
 			System.out.println("Save command cancelled by user.");
 		}
 
 	}
 	
 }
