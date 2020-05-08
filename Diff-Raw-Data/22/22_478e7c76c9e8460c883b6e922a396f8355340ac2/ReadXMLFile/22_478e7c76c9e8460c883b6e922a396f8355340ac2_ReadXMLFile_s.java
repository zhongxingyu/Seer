 package fr.istic.synthlab.util;
 
 import java.awt.Point;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import fr.istic.synthlab.abstraction.module.IModule;
 import fr.istic.synthlab.abstraction.port.IInputPort;
 import fr.istic.synthlab.abstraction.port.IOutputPort;
 import fr.istic.synthlab.abstraction.port.IPort;
 import fr.istic.synthlab.abstraction.wire.IWire;
 import fr.istic.synthlab.controller.module.ICModule;
 import fr.istic.synthlab.controller.module.audioscope.CModuleAudioScope;
 import fr.istic.synthlab.controller.module.eg.CModuleEG;
 import fr.istic.synthlab.controller.module.out.CModuleOUT;
 import fr.istic.synthlab.controller.module.rep.CModuleREP;
 import fr.istic.synthlab.controller.module.vca.CModuleVCA;
 import fr.istic.synthlab.controller.module.vcf.CModuleVCFA_LP;
 import fr.istic.synthlab.controller.module.vco.CModuleVCO;
 import fr.istic.synthlab.controller.synthesizer.CSynthesizer;
 import fr.istic.synthlab.controller.wire.CWire;
 
 public class ReadXMLFile {
 
 	private Document doc;
 	private HashMap<String, IModule> modules;
 	private HashMap<IWire, String[]> portsToConnect;
 
 	public ReadXMLFile() {
 		portsToConnect = new HashMap<IWire, String[]>();
 		modules = new HashMap<String, IModule>();
 		try {
 			File fXmlFile = new File("savedInstance.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			doc = dBuilder.parse(fXmlFile);
 			doc.getDocumentElement().normalize();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadSynthesizer() {
		List<Object> modulesAndWires =  new ArrayList<Object>();
 		try {
 			NodeList nList = doc.getElementsByTagName("module");
 
 			for (int temp = 0; temp < nList.getLength(); temp++) {
 				Node nNode = nList.item(temp);
 
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 					Element eElement = (Element) nNode;
 					ICModule module = null;
 					
 					if(eElement.getAttribute("name").startsWith("VCO")){
 						module = new CModuleVCO(CSynthesizer.getInstance());
 					} else if (eElement.getAttribute("name").startsWith("VCA")){
 						module = new CModuleVCA(CSynthesizer.getInstance());
 					} else if (eElement.getAttribute("name").startsWith("OUT")){
 						module = new CModuleOUT(CSynthesizer.getInstance());
 					} else if (eElement.getAttribute("name").startsWith("EG")){
 						module = new CModuleEG(CSynthesizer.getInstance());
 					} else if (eElement.getAttribute("name").startsWith("REP")){
 						module = new CModuleREP(CSynthesizer.getInstance());
 					} else if (eElement.getAttribute("name").startsWith("VCFA LP24")){
 						module = new CModuleVCFA_LP(CSynthesizer.getInstance());
 					} else if (eElement.getAttribute("name").startsWith("AudioScope")){
 						module = new CModuleAudioScope(CSynthesizer.getInstance());
 					} else {
 						throw new Exception("Module not recognized in xml file");
 					}
 					
 					int x = (int) Double.parseDouble(eElement.getAttribute("x"));
 					int y = (int) Double.parseDouble(eElement.getAttribute("y"));
 					Point p = new Point(x,y);
 					module.getPresentation().setPosition(p);
 					
 					for (int i = 0; i < eElement.getElementsByTagName("port")
 							.getLength(); i++) {
 						String portName = ((Element) eElement.getElementsByTagName("port").item(i)).getAttribute("name");
 						
 						IWire wire = new CWire();
 						IOutputPort outport = (IOutputPort)module.getPortByName(portName);
 						wire.connect(outport);
 						
 						String[] str = {((Element) eElement.getElementsByTagName(
 								"port").item(i))
 								.getAttribute("connectedToModuleName"), 
 								
 								((Element) eElement.getElementsByTagName(
 										"port").item(i))
 										.getAttribute("connectedToModulePort")};
 						
 						portsToConnect.put(wire,str);
 					}
 					
 					for (int i = 0; i < eElement.getElementsByTagName(
 							"parameter").getLength(); i++) {
 						String key = ((Element) eElement.getElementsByTagName("parameter").item(i)).getAttribute("key");
 						Double value = Double.parseDouble(((Element) eElement.getElementsByTagName("parameter").item(i)).getAttribute("value"));
 						module.setParameter(key, value);
 					}
 					modules.put(module.getName(), module);
					modulesAndWires.add(module);
 					CSynthesizer.getInstance().add(module);
 				}
 				
				for(IWire wire : portsToConnect.keySet()){
					String moduleName = portsToConnect.get(wire)[0];
					String portName = portsToConnect.get(wire)[1];
					IModule moduleToConnect = modules.get(moduleName);
					IPort port = moduleToConnect.getPortByName(portName);
					wire.connect((IInputPort)port);
					modulesAndWires.add(wire);
					CSynthesizer.getInstance().add(wire);
				}
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
