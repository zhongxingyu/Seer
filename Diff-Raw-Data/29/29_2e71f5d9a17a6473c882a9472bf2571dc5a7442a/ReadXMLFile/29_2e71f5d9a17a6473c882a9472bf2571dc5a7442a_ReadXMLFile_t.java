 package fr.istic.synthlab.util;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.io.File;
 import java.util.HashMap;
 
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
 import fr.istic.synthlab.controller.module.mix.CModuleMIX;
 import fr.istic.synthlab.controller.module.out.CModuleOUT;
 import fr.istic.synthlab.controller.module.rep.CModuleREP;
 import fr.istic.synthlab.controller.module.vca.CModuleVCA;
 import fr.istic.synthlab.controller.module.vcf.CModuleVCF_HP;
 import fr.istic.synthlab.controller.module.vcf.CModuleVCF_LP;
 import fr.istic.synthlab.controller.module.vco.CModuleVCO;
 import fr.istic.synthlab.controller.synthesizer.CSynthesizer;
 import fr.istic.synthlab.controller.wire.CWire;
 import fr.istic.synthlab.controller.wire.ICWire;
 
 public class ReadXMLFile {
 
 	private Document doc;
 	private HashMap<String, IModule> modules;
 	private HashMap<IWire, String[]> portsToConnect;
 
 	public ReadXMLFile(File file) {
 		portsToConnect = new HashMap<IWire, String[]>();
 		modules = new HashMap<String, IModule>();
 		try {
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			doc = dBuilder.parse(file);
 			doc.getDocumentElement().normalize();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadSynthesizer() {
 		try {
 			NodeList nList = doc.getElementsByTagName("module");
 
 			for (int temp = 0; temp < nList.getLength(); temp++) {
 				Node nNode = nList.item(temp);
 
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 					Element eElement = (Element) nNode;
 					ICModule module = null;
 					
 					if(eElement.getAttribute("name").startsWith("VCO")){
 						module = new CModuleVCO();
 					} else if (eElement.getAttribute("name").startsWith("VCA")){
 						module = new CModuleVCA();
 					} else if (eElement.getAttribute("name").startsWith("OUT")){
 						module = new CModuleOUT();
 					} else if (eElement.getAttribute("name").startsWith("EG")){
 						module = new CModuleEG();
 					} else if (eElement.getAttribute("name").startsWith("REP")){
 						module = new CModuleREP();
					} else if (eElement.getAttribute("name").startsWith("VCF LP24")){
 						module = new CModuleVCF_LP();
					} else if (eElement.getAttribute("name").startsWith("VCF HP24")){
 						module = new CModuleVCF_HP();
 					} else if (eElement.getAttribute("name").startsWith("AudioScope")){
 						module = new CModuleAudioScope();
 					} else if (eElement.getAttribute("name").startsWith("MIX")){
 						module = new CModuleMIX();
 					} else {
 						throw new Exception("Module not recognized in xml file");
 					}
 					
 					int x = (int) Double.parseDouble(eElement.getAttribute("x"));
 					int y = (int) Double.parseDouble(eElement.getAttribute("y"));
 					Point p = new Point(x,y);
 					module.getPresentation().setPosition(p);
 					
 					for (int i = 0; i < eElement.getElementsByTagName("wire")
 							.getLength(); i++) {
 						String portName = ((Element) eElement.getElementsByTagName("wire").item(i)).getAttribute("outputPort");
 						
 						IWire wire = new CWire();
 						IOutputPort outport = (IOutputPort)module.getPortByName(portName);
 						wire.connect(outport);
 						
 						String[] str = {
 								((Element) eElement.getElementsByTagName(
 										"wire").item(i))
 										.getAttribute("inputPortModuleName"), 
 								
 								((Element) eElement.getElementsByTagName(
 										"wire").item(i))
 										.getAttribute("inputPort"),
 										
 								((Element) eElement.getElementsByTagName(
 										"wire").item(i))
 										.getAttribute("colorR"),
 										
 								((Element) eElement.getElementsByTagName(
 										"wire").item(i))
 										.getAttribute("colorG"),
 										
 								((Element) eElement.getElementsByTagName(
 										"wire").item(i))
 										.getAttribute("colorB")
 						};
 						
 						portsToConnect.put(wire,str);
 					}
 					
 					for (int i = 0; i < eElement.getElementsByTagName(
 							"parameter").getLength(); i++) {
 						String key = ((Element) eElement.getElementsByTagName("parameter").item(i)).getAttribute("key");
 						Double value = Double.parseDouble(((Element) eElement.getElementsByTagName("parameter").item(i)).getAttribute("value"));
 						module.setParameter(key, value);
 					}
 					modules.put(module.getName(), module);
 					CSynthesizer.getInstance().add(module);
 				}
 				
 			}
 			for(IWire wire : portsToConnect.keySet()){
 				String moduleName = portsToConnect.get(wire)[0];
 				String portName = portsToConnect.get(wire)[1];
 				Color color = new Color(
 						Integer.parseInt(portsToConnect.get(wire)[2]),
 						Integer.parseInt(portsToConnect.get(wire)[3]),
 						Integer.parseInt(portsToConnect.get(wire)[4])
 				);
 				
 				((ICWire)wire).getPresentation().setColor(color);
 				
 				IModule moduleToConnect = modules.get(moduleName);
 				IPort port = moduleToConnect.getPortByName(portName);
 				wire.connect((IInputPort)port);
 				CSynthesizer.getInstance().add(wire);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
