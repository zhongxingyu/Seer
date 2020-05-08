 package com.aereo.prov;
 
 import java.io.File;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.zkoss.zul.ListModel;
 
 import com.aereo.prov.components.Antenna;
 import com.aereo.prov.components.Xcoder;
 
 
 public class WriteXML {
 	
 	public void writeXMLToFile(ListModel antennaListModel, ListModel xcoderListModel,String filePath) throws Exception{
 //		try {
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			
 			// root node
 			Document doc = dBuilder.newDocument();
 			Element provNode = doc.createElement("antsvrcfg");
 			doc.appendChild(provNode);
 			
 			// antennaBoardNode
 			Element antennaBoardNode = doc.createElement("AAB");
 			provNode.appendChild(antennaBoardNode);
 			
 			for (int j=0; j < antennaListModel.getSize(); j++) {
 				Element antennaElement = doc.createElement("AntBoard");
 				Antenna antenna = (Antenna)antennaListModel.getElementAt(j);
 	        	
 				if(antenna.isChecked()){
 					antennaElement.setAttribute("assetid",antenna.getAssetid());
 		        	antennaElement.setAttribute("boardType",antenna.getBoardtype());
 		        	antennaElement.setAttribute("macAddr",antenna.getMacaddr());
 		        	antennaElement.setAttribute("longShelfId",antenna.getLongshelfid());
 		        	antennaElement.setAttribute("SlotId",antenna.getSlotid());
 		        	antennaElement.setAttribute("zone",antenna.getZone());
 		        	antennaElement.setAttribute("locaddr",antenna.getLocaddr());
 		        	antennaElement.setAttribute("ipaddr",antenna.getIpaddr());
 		        	int resourceid = Utilities.getResourceId(0,Integer.parseInt(antenna.getLongshelfid()),Integer.parseInt(antenna.getSlotid()));
 		        	antennaElement.setAttribute("resourceid",""+resourceid);
 		        	
 		        	antennaBoardNode.appendChild(antennaElement);
 				}
 			}
 			
 			// xcoderBoardNode
 			Element xcoderBoardNode = doc.createElement("BT2");
 			provNode.appendChild(xcoderBoardNode);
 			
 			for (int j=0; j < xcoderListModel.getSize(); j++) {
 				Element xcoderElement = doc.createElement("XCodeBoard");
 				Xcoder xcoder = (Xcoder)xcoderListModel.getElementAt(j);
 	        	
 				if(xcoder.isChecked()){
					xcoderElement.setAttribute("assetid",xcoder.getAssetid());
 		        	xcoderElement.setAttribute("boardType",xcoder.getBoardtype());
 		        	xcoderElement.setAttribute("macAddr",xcoder.getMacaddr());
 		        	xcoderElement.setAttribute("longShelfId",xcoder.getLongshelfid());
 		        	xcoderElement.setAttribute("SlotId",xcoder.getSlotid());
 		        	xcoderElement.setAttribute("zone",xcoder.getZone());
 		        	xcoderElement.setAttribute("locaddr",xcoder.getLocaddr());
 		        	xcoderElement.setAttribute("ipaddr",xcoder.getIpaddr());
 		        	int resourceid = Utilities.getResourceId(1,Integer.parseInt(xcoder.getLongshelfid()),Integer.parseInt(xcoder.getSlotid()));
 		        	xcoderElement.setAttribute("resourceid",""+resourceid);
 		        	
 		        	xcoderBoardNode.appendChild(xcoderElement);
 				}
 			}
 			
 			
 		    //set up a transformer
 		    TransformerFactory transfac = TransformerFactory.newInstance();
 		    Transformer trans = transfac.newTransformer();
 		 
 		    //create string from xml tree
 		    //StringWriter sw = new StringWriter();
 		    //StreamResult result = new StreamResult(sw);
 		    StreamResult result = new StreamResult(new File(filePath));
 		    DOMSource source = new DOMSource(doc);
 		    trans.transform(source, result);
 		    
 		    //String xmlString = sw.toString();
 		    //return xmlString;
 			
 //		}
 //		catch(Exception e) {
 //			e.printStackTrace();
 //			//return "";
 //		}
 		
 	}
 	
 }
