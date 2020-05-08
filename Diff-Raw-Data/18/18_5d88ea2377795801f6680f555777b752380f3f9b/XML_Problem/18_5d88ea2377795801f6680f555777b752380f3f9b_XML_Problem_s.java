 package moleculeMaker;
 
 import java.io.File;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 
 /**
  * <Problem>
  *  <Molecule name="molecule name">
  *      <Element>
  *          <Location x="0" y="0" /> (valid input: 0-240)
  *          <Label></Label> (valid input: text or empty)
  *          <Charge></Charge> (valid input: 0-2 ~ 0-None, 1-Negative, 2-Positive)
  *          <Electrons></Electrons> (valid input: 0-6)
  *          <Type></Type> (valid input: 0-2 ~ 0-None, 1-Ep, 2-Np)
  *      </Element>
  *      <Bond> (location A & B match locations on elements)
  *          <LocationA x="0" y="0" /> (valid input: 0-240)
  *          <LocationB x="0" y="0" /> (valid input: 0-240)
  *          <BondOrder></BondOrder> (valid input: 1-3 ~ lines)
  *      </Bond>
  *  </Molecule>
  *  <Arrow> (arrow travels A to B. locationsA/B match locations on elements 
  *  and middle location [locationB-locationA] of bonds)
  *      <LocationA x="0" y="0" /> (valid input: 0-240 ~ if location is a bond, 
  *      use it's middle point -- x:(bond.locationA.x+bond.locationB.x)/2, 
  *      y:(bond.locationA.y+bond.locationB.y)/2)
  *      <LocationB x="0" y="0" /> (valid input: 0-240 ~ see line above)
  *      <Order></Order> (valid input: 2-3 ~ order the problem is solved via arrows -- 
  *      order 1 is interpreted by the app)
  *  </Arrow>
  * </Problem>
 
  * @author igraham
  *
  */
 public class XML_Problem
 {
 	
 	public XML_Problem(ConnectionList molecule1, ConnectionList molecule2)
 	{
 		try {
 			 
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			//Here's the root of the problem!
 			org.w3c.dom.Element root = doc.createElement("Problem");
 			doc.appendChild(root);
 			//Not Implemented: Molecules should have names, there should be a way to add this in via the interface.
 			//Also not implemented is the ability to distinguish between the electrophile and the nucleophile.
 			//This is already set up in the Element class but it is not set up in the interface.
 			setUpXML(molecule1, doc, root);
 			setUpXML(molecule2, doc, root);
 			
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			StreamResult result = new StreamResult(new File("problem.xml"));
			//StreamResult result = new StreamResult(System.out);
 
 			try {
 				transformer.transform(source, result);
 				System.out.println("File saved!");
 			} catch (TransformerException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		catch(ParserConfigurationException pce) 
 		{
 			pce.printStackTrace();
 		} catch (TransformerConfigurationException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void setUpXML(ConnectionList molecule, Document doc, org.w3c.dom.Element root)
 	{
 		org.w3c.dom.Element mol = doc.createElement("Molecule");
 		root.appendChild(mol);
 		for(moleculeMaker.Element a : molecule.getMap().values())
 		{
 			org.w3c.dom.Element Element = doc.createElement("Element");
 			mol.appendChild(Element);
 				org.w3c.dom.Element loc = doc.createElement("Location");
 					//X and Y values for the location of the Element are set here. Since an Element
 					//cannot be created without an x and a y value then there is no need to 
 					//check to see if they are null or something.
 					Attr x = doc.createAttribute("x");
 					x.setValue(""+a.getX());
 					Attr y = doc.createAttribute("y");
 					y.setValue(""+a.getY());
 					loc.setAttributeNode(x);
 					loc.setAttributeNode(y);
 				Element.appendChild(loc);
 				org.w3c.dom.Element lab = doc.createElement("Label");
 					//Default value for the label set here, which would be
 					//Either some text or nothing.
 					String name;
 					if(a.getName() == null){name = "";}else{name = a.getName();}
 					lab.appendChild(doc.createTextNode(""+name));
 				Element.appendChild(lab);
 				org.w3c.dom.Element charge = doc.createElement("Charge");
 					//Default charge set to zero here. Also checks to see whether the Element's
 					//charge is between 0 and 2 or not.
 					int chrg = 0;
 					if(a.getCharge()>= 0 && a.getCharge() <=2){chrg = a.getCharge();}
 					charge.appendChild(doc.createTextNode(""+chrg));
 				Element.appendChild(charge);
 				org.w3c.dom.Element elec = doc.createElement("Electrons");
 					//Default value of the number of electrons in the Element is set to 0, also
 					//the number of electrons in the Element is checked to see whether it is between
 					//the numbers 0 and 6.
 					int elecs = 0;
 					if(a.getElectrons()>=0 && a.getElectrons()<=6){elecs = a.getElectrons();}
					charge.appendChild(doc.createTextNode(""+elecs));
 				Element.appendChild(elec);
 				org.w3c.dom.Element type = doc.createElement("Type");
 				//Here we set the type, but like I said I don't think this is implemented in the interface
 				//yet, so I will leave this alone.
 					int eType = 0;
 					if(a.getType()>=0 && a.getType()<=2){eType = a.getType();}
 					type.appendChild(doc.createTextNode(""+eType));
 				Element.appendChild(type);
 		}
 		
 		for(Bond b : molecule.getBondHash().values())
 		{
 			org.w3c.dom.Element bond = doc.createElement("Bond");
 			mol.appendChild(bond);
 				//X and Y values for the locations of the bond are set here. Since an bond
 				//cannot be created without two valid Elements then there is no need to 
 				//check to see if they are null or something.
 				org.w3c.dom.Element loc1 = doc.createElement("LocationA");
 					Attr x1 = doc.createAttribute("x");
 					x1.setValue(""+b.getConnector().getX());
 					Attr y1 = doc.createAttribute("y");
 					y1.setValue(""+b.getConnector().getY());
 					loc1.setAttributeNode(x1);
 					loc1.setAttributeNode(y1);
 				bond.appendChild(loc1);
 				org.w3c.dom.Element loc2 = doc.createElement("LocationB");
 					Attr x2 = doc.createAttribute("x");
 					x2.setValue(""+b.getConnectee().getX());
 					Attr y2 = doc.createAttribute("y");
 					y2.setValue(""+b.getConnectee().getY());
 					loc1.setAttributeNode(x2);
 					loc1.setAttributeNode(y2);
 				bond.appendChild(loc2);
 				org.w3c.dom.Element bOrder = doc.createElement("BondOrder");
 					bOrder.appendChild(doc.createTextNode(""+b.getBondOrder()));
 				bond.appendChild(bOrder);
 		}
 		for(Arrow a : molecule.getArrowHash().values())
 		{
 			org.w3c.dom.Element arrow = doc.createElement("Arrow");
 			mol.appendChild(arrow);
 				//X and Y values for the locations of the Arrow are set here. Since an Arrow
 				//cannot be created without two valid Elements then there is no need to 
 				//check to see if they are null or something.
 				org.w3c.dom.Element loc1 = doc.createElement("LocationA");
 					Attr x1 = doc.createAttribute("x");
 					x1.setValue(""+a.getConnector().getX());
 					Attr y1 = doc.createAttribute("y");
 					y1.setValue(""+a.getConnector().getY());
 					loc1.setAttributeNode(x1);
 					loc1.setAttributeNode(y1);
 				arrow.appendChild(loc1);
 				org.w3c.dom.Element loc2 = doc.createElement("LocationB");
 					Attr x2 = doc.createAttribute("x");
 					x2.setValue(""+a.getConnectee().getX());
 					Attr y2 = doc.createAttribute("y");
 					y2.setValue(""+a.getConnectee().getY());
 					loc1.setAttributeNode(x2);
 					loc1.setAttributeNode(y2);
 				arrow.appendChild(loc2);
 				org.w3c.dom.Element bOrder = doc.createElement("Order");
 					bOrder.appendChild(doc.createTextNode(""+a.getOrder()));
 				arrow.appendChild(bOrder);
 		}
 		
 	}
 
 }
