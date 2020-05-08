 package Model;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 import org.w3c.dom.*;
 
 /**This class, when instantiated and passed an airport; takes the airports runways and their
  * values and creates an XML file with them. It provides a JFileChooser window to select where
  * to save the file and how to name it. It also works with obstacles.
  * @author Oscar
  */
 public class SaveToXMLFile {
 
 	private File file;
 	private DocumentBuilderFactory documentBuilderFactory;
 	private DocumentBuilder documentBuilder;
 	private Document document;
 	private Element rootElement;
 
 
 	/**
 	 * Constructor for Airport
 	 * @param airport The airport to save
 	 * @throws Exception Relating to writing files or generating xml
 	 * TODO: Throw only relevant exceptions. 
 	 */
 	public SaveToXMLFile(Airport airport) throws Exception {
 		String root = "Airport";
 		this.createDocBuilderFactory(root);
 
 		//First element, the airport's name
 		Element airportName = document.createElement("Airport_Name");
 		airportName.appendChild(document.createTextNode(airport.getName()));
 		rootElement.appendChild(airportName);
 
 		this.addNodesAndElements(airport);
 
 		//Creating JFileChooser object and storing its return value
 		this.createFChooserAndStore();
 	}
 
 	/**
 	 * Constructor for obstacle
 	 * @param obstacle The obstacle to save
 	 * @throws Exception Relating to reading files or generating xml
 	 */
 	public SaveToXMLFile(Obstacle obstacle) throws Exception{
 
 		String root = "Obstacle";
 		this.createDocBuilderFactory(root);
 
 		//First element, the obstacle's name
 		Element obstacleName = document.createElement("Obstacle_Name");
 		obstacleName.appendChild(document.createTextNode(obstacle.getName()));
 		rootElement.appendChild(obstacleName);
 
 		this.addNodesAndElementsObstacle(obstacle);
 
 		//Creating JFileChooser object and storing its return value
 		this.createFChooserAndStore();
 	}
 	
 	/**
 	 * Constructor for contacts
	 * @param contacts The list of contacts to save, boolean indicating silent save or not
 	 * @throws Exception Relating to reading files or generating xml
 	 */
 	public SaveToXMLFile(ArrayList<Contact> contacts, boolean silentOrNot) throws Exception{
 
 		String root = "Contacts";
 		this.createDocBuilderFactory(root);
 
 		this.addNodesAndElementsContacts(contacts);
 
 		if (!silentOrNot){
 			//Creating JFileChooser object and storing its return value
 			this.createFChooserAndStore();
 		}else{
 			silentSaveContacts("~/file.xml");
 		}
 	}
 
 	/**
 	 * Creates DocumentBuilderFactory using string for root element
 	 * @param root The root element
 	 * @throws ParserConfigurationException 
 	 */
 	public void createDocBuilderFactory(String root) throws ParserConfigurationException{
 		documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		documentBuilder = documentBuilderFactory.newDocumentBuilder();
 		document = documentBuilder.newDocument();
 		rootElement = document.createElement(root);
 		document.appendChild(rootElement);
 	}
 
 	/**
 	 * Adds the nodes and elements to the xml
 	 * @param contacts The list of contacts to be saved
 	 */
 	public void addNodesAndElementsContacts(ArrayList<Contact> contactList) {
 
 		//int numberOfRunways = airport.runways().size(); //number of physical runways
 
 		//for (PhysicalRunway runway: airport.runways()) { 
 
 
 			/*Element physicalRunway = document.createElement("PhysicalRunway");
 			String namePhysicalRunwayString = runway.getId();
 			// physicalRunway.appendChild(document.createTextNode(nam));
 
 			Element physicalRunwayName = document.createElement("Name");
 			physicalRunwayName.appendChild(document.createTextNode(namePhysicalRunwayString));
 			physicalRunway.appendChild(physicalRunwayName);*/
 
 			for (int i = 0; i < contactList.size(); i++) { // looping through each contact
 				
 				Contact thisContact = contactList.get(i);// grabbing a contact
 
 				// Creating contact element and appending to root element
 				Element element = document.createElement("Contact");
 
 				// Creating each of the contact's elements and appending to the contact element
 				Element firstName = document.createElement("First_Name");
 				firstName.appendChild(document.createTextNode(thisContact.getFirstName()));
 				element.appendChild(firstName);
 
 				Element lastName = document.createElement("Last_Name");
 				lastName.appendChild(document.createTextNode(thisContact.getLastName()));
 				element.appendChild(lastName);
 
 				Element email = document.createElement("Email_Address");
 				email.appendChild(document.createTextNode(thisContact.getEmail()));
 				element.appendChild(email);
 
 
 				rootElement.appendChild(element);
 				// rootElement.appendChild(em);
 			}
 
 		
 
 	}
 	
 	
 	/**
 	 * Adds the nodes and elements to the xml
 	 * @param airport The airport to be saved
 	 */
 	public void addNodesAndElements(Airport airport) {
 
 		//int numberOfRunways = airport.runways().size(); //number of physical runways
 
 		for (PhysicalRunway runway: airport.getPhysicalRunways()) { // looping through all physical runways
 
 
 			Element physicalRunway = document.createElement("PhysicalRunway");
 			String namePhysicalRunwayString = runway.getId();
 			// physicalRunway.appendChild(document.createTextNode(nam));
 
 			Element physicalRunwayName = document.createElement("Name");
 			physicalRunwayName.appendChild(document.createTextNode(namePhysicalRunwayString));
 			physicalRunway.appendChild(physicalRunwayName);
 			
 			Element resa = document.createElement("RESA");
 			String resaString = Double.toString(runway.getRESA()/*.getRESA()*/);
 			resa.appendChild(document.createTextNode(resaString));
 			physicalRunway.appendChild(resa);
 			
 			Element stopway = document.createElement("Stopway");
 			String stopwayString = Double.toString(runway.getStopway());
 			stopway.appendChild(document.createTextNode(stopwayString));
 			physicalRunway.appendChild(stopway);
 			
 			Element blastAllowance = document.createElement("Blast_Allowance");
 			String blastString = Double.toString(runway.getBlastAllowance());
 			blastAllowance.appendChild(document.createTextNode(blastString));
 			physicalRunway.appendChild(blastAllowance);
 			
 			Element runwayStripWidth = document.createElement("Runway_Strip_Width");
 			String runwayWidthString = Double.toString(runway.getRunwayStripWidth());
 			runwayStripWidth.appendChild(document.createTextNode(runwayWidthString));
 			physicalRunway.appendChild(runwayStripWidth);
 			
 			Element clearAndGradedWidth = document.createElement("Clear_And_Graded_Width");
 			String clearWidthString = Double.toString(runway.getClearedAndGradedWidth());
 			clearAndGradedWidth.appendChild(document.createTextNode(clearWidthString));
 			physicalRunway.appendChild(clearAndGradedWidth);
 			
 			Element distanceAwayFromThreshold = document.createElement("Distance_Away_From_Threshold");
 			String distanceFromThresString = Double.toString(runway.getDistanceAwayFromThreshold());
 			distanceAwayFromThreshold.appendChild(document.createTextNode(distanceFromThresString));
 			physicalRunway.appendChild(distanceAwayFromThreshold);
 			
 			Element distanceAwayFromCenterline = document.createElement("Distance_Away_From_Centerline");
 			String distanceFromCenterString = Double.toString(runway.getDistanceAwayFromCenterLine());
 			distanceAwayFromCenterline.appendChild(document.createTextNode(distanceFromCenterString));
 			physicalRunway.appendChild(distanceAwayFromCenterline);
 			
 			Element angleOfSlope = document.createElement("Angle_Of_Slope");
 			String angleString = Double.toString(runway.getAngleOfSlope());
 			angleOfSlope.appendChild(document.createTextNode(angleString));
 			physicalRunway.appendChild(angleOfSlope);
 			
 			
 
 			for (int i = 0; i < 2; i++) { // looping through each actual runway (2)
 				Runway runwayObject = runway.getRunway(i);// getting a runway
 
 				// Creating runway element and appending to root element
 				Element element = document.createElement("Runway");
 
 				// Creating each of the runway's elements and appending to the runway element
 				Element name = document.createElement("RunwayName");
 				name.appendChild(document.createTextNode(runwayObject.getName()));
 				element.appendChild(name);
 
 				Element tora = document.createElement("TORA");
 				String toraString = Double.toString(runwayObject.getTORA(1));// getting the tora value that can be modified
 				tora.appendChild(document.createTextNode(toraString));
 				element.appendChild(tora);
 
 				Element asda = document.createElement("ASDA");
 				String asdaString = Double.toString(runwayObject.getASDA(1));
 				asda.appendChild(document.createTextNode(asdaString));
 				element.appendChild(asda);
 
 				Element toda = document.createElement("TODA");
 				String todaString = Double.toString(runwayObject.getTODA(1));
 				toda.appendChild(document.createTextNode(todaString));
 				element.appendChild(toda);
 
 				Element lda = document.createElement("LDA");
 				String ldaString = Double.toString(runwayObject.getLDA(1));
 				lda.appendChild(document.createTextNode(ldaString));
 				element.appendChild(lda);
 
 				Element displacedThreshold = document
 						.createElement("DisplacedThreshold");
 				String displacedThresholdString = Double.toString(runwayObject.getDisplacedThreshold(1));
 				displacedThreshold.appendChild(document.createTextNode(displacedThresholdString));
 				element.appendChild(displacedThreshold);
 
 				physicalRunway.appendChild(element);
 				// rootElement.appendChild(em);
 			}
 
 			rootElement.appendChild(physicalRunway);
 		}
 
 	}
 
 	/**
 	 * Adds the nodes and elements to the xml
 	 * @param obstacle The obstacle to be saved
 	 */
 	public void addNodesAndElementsObstacle(Obstacle obstacle) {
 
 		Element height = document.createElement("Height");
 		String heightString = Double.toString(obstacle.getHeight());
 		height.appendChild(document.createTextNode(heightString));
 		rootElement.appendChild(height);
 
 		Element width = document.createElement("Width");
 		String widthString = Double.toString(obstacle.getWidth());
 		width.appendChild(document.createTextNode(widthString));
 		rootElement.appendChild(width);
 
 		Element length = document.createElement("Length");
 		String lengthString = Double.toString(obstacle.getLength());
 		length.appendChild(document.createTextNode(lengthString));
 		rootElement.appendChild(length);
 
 	}
 
 
 	/**
 	 * Creates File chooser and saves XML to given file
 	 * @throws IOException
 	 * @throws TransformerException
 	 */
 	public void createFChooserAndStore() throws IOException, TransformerException {
 
 		JFileChooser fileChooser = new JFileChooser();
 
 		int returnValue = fileChooser.showSaveDialog(null);
 
 		if (returnValue == JFileChooser.APPROVE_OPTION) {
 			file = fileChooser.getSelectedFile();
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
 	
 	public void silentSaveContacts(String address) throws IOException, TransformerException {
 
 		file = new File("address");
 		
 		TransformerFactory transformerFactory = TransformerFactory
 				.newInstance();
 		Transformer transformer = transformerFactory.newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		
 		StreamResult result = new StreamResult(file/*System.out*/);
 		DOMSource source = new DOMSource(document);
 		file.createNewFile();
 		
 		transformer.transform(source, result);
 		
 		
 
 	}
 
 }
