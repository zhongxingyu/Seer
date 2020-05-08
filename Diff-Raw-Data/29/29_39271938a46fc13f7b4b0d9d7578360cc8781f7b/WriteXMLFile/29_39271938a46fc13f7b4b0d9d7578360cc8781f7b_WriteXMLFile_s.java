 package fr.istic.synthlab.util;
 
 import java.awt.Point;
 import java.io.File;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import fr.istic.synthlab.abstraction.module.IModule;
 import fr.istic.synthlab.abstraction.wire.IWire;
 import fr.istic.synthlab.controller.module.ICModule;
 
 public class WriteXMLFile {
 
 	private Document doc;
 	private Element rootElement;
 	private File directory;
 
 	public WriteXMLFile(File directory) {
 		this.directory = directory;
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder docBuilder;
 			docBuilder = docFactory.newDocumentBuilder();
 
 			// root elements
 			doc = docBuilder.newDocument();
 			rootElement = doc.createElement("modules");
 			doc.appendChild(rootElement);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// write the content into xml file
 	public void saveToXML() {
 		try {
 			TransformerFactory transformerFactory = TransformerFactory
 					.newInstance();
 			Transformer transformer;
 
 			transformer = transformerFactory.newTransformer();
 
 			DOMSource source = new DOMSource(doc);
 			
 			StreamResult result = new StreamResult(directory);
 
 			// Output to console for testing
 			StreamResult res = new StreamResult(System.out);
 
 			transformer.transform(source, result);
 			transformer.transform(source, res);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void saveModules(List<IModule> modules) {
 		for(IModule module : modules){
			
 			Element moduleName = doc.createElement("module");
 			rootElement.appendChild(moduleName);
 			
 			Point p = ((ICModule)module).getPresentation().getPosition();
 			System.out.println(p);
 			
 			// Nom module
 			Attr attrName = doc.createAttribute("name");
 			attrName.setValue(module.getName());
 			moduleName.setAttributeNode(attrName);
 			
 			// Position x
 			Attr attrX = doc.createAttribute("x");
 			attrX.setValue(Double.toString(p.getX()));
 			moduleName.setAttributeNode(attrX);
 			
 			// Position y
 			Attr attrY = doc.createAttribute("y");
 			attrY.setValue(Double.toString(p.getY()));
 			moduleName.setAttributeNode(attrY);
 			
 			// Ajout des ports utilises du module
 			for(IWire wire : module.getWires()){
 				if(module.containsPort(wire.getOutput())){
 					// set le port
 					Element port = doc.createElement("port");
 					moduleName.appendChild(port);
 					
 					// Nom port
 					Attr attrPortName = doc.createAttribute("name");
 					attrPortName.setValue(wire.getOutput().getName());
 					port.setAttributeNode(attrPortName);
 					
 					// Connected to Module name
 					Attr attrPortConnectedToModuleName = doc.createAttribute("connectedToModuleName");
 					attrPortConnectedToModuleName.setValue(wire.getInput().getModule().getName());
 					port.setAttributeNode(attrPortConnectedToModuleName);
 					
 					// Connected to Module port
 					Attr attrPortConnectedToModulePort = doc.createAttribute("connectedToModulePort");
 					attrPortConnectedToModulePort.setValue(wire.getInput().getName());
 					port.setAttributeNode(attrPortConnectedToModulePort);
 					
 				}
 			}
 			
 			for(String key : module.getParameters().keySet()){
 				// set le parametre
 				Element param = doc.createElement("parameter");
 				moduleName.appendChild(param);
 				
 				// Nom parametre
 				Attr attrParamName = doc.createAttribute("key");
 				attrParamName.setValue(key);
 				param.setAttributeNode(attrParamName);
 				
 				// Valeur parametre
 				Attr attrParamValue = doc.createAttribute("value");
 				attrParamValue.setValue(module.getParameters().get(key).toString());
 				param.setAttributeNode(attrParamValue);
 			}
 		}
 		saveToXML();
 	}
 	
 // STRUCTURE DU XML BATI :
 //	<?xml version="1.0" encoding="UTF-8" standalone="no"?>
 //	<modules>
 //		<module name="OUT1" x="10.0" y="10.0">
 //			<port connectedToModuleName="VCA1" connectedToModulePort="Output" name="In" type="input"/>
 //		</module>
 //		<module name="VCO1" x="14.0" y="296.0"/>
 //		<module name="VCA1" x="482.0" y="300.0">
 //			<port connectedToModuleName="VCO1" connectedToModulePort="Square" name="Input" type="input"/>
 //		</module>
 //		<module name="VCA2" x="1010.0" y="64.0">
 //			<port connectedToModuleName="VCO1" connectedToModulePort="SawTooth" name="Input" type="input"/>
 //		</module>
 //	</modules>
 }
