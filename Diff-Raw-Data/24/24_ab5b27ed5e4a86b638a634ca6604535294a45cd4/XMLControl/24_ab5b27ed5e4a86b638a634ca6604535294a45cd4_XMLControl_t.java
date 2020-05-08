 package tagEditor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.codec.binary.Base64;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class XMLControl {
 
 	/** Reads the xml-file given with file and returns a DefaultMutableTreeNode representation of it
 	 * 
 	 * @param file - the xml-file that shall be read and transformed into a DefaultMutableTreeNode
 	 * @return the DefaultMutableTreeNode representation of the xml-file
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	public static DefaultMutableTreeNode readCache(File file) throws ParserConfigurationException, SAXException, IOException {
 	    // do something with the current node instead of System.out
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		Document doc = dBuilder.parse(file);
 	    return build(doc.getDocumentElement());
 	}
 	
 	/** Will transform a XML-file in form of a Element (w3c.dom) into a DefaultMutableTreeNode
 	 * 
 	 * @param e - the xml-tree in form of a Element (w3c.dom)
 	 * @return the DefaultMutableTreeNode representation of the xml-file
 	 */
 	public static DefaultMutableTreeNode build(Element e) {
 		   DefaultMutableTreeNode result = null;
 		   if(e.getNodeName() == "directory"){
 			   result = new DefaultMutableTreeNode(new Directory(e.getAttribute("filename")));
 			   for(int i = 0; i < e.getChildNodes().getLength(); i++){
 				   if(e.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
 					   result.add(build((Element) e.getChildNodes().item(i)));
 				   }
 			   }
 		   } else {
 			   MP3File mp3 = new MP3File();
 			   ArrayList<Frame> frameList = new ArrayList<Frame>();
 			   mp3.setPath(e.getAttribute("filepath"));
 			   NodeList nodes = e.getChildNodes();
 			   for(int i = 0; i < nodes.getLength(); i++){
 				    Node node = nodes.item(i);
 				    switch (node.getNodeName()) {
 				   	case "artist": mp3.setArtist(node.getTextContent()); break;
 				   	case "album":  mp3.setAlbum(node.getTextContent()); break;
 				   	case "song":   mp3.setSong(node.getTextContent()); break;
 				   	case "year":   mp3.setYear(node.getTextContent()); break;
 				   	case "tagsize":mp3.setSize(new Integer(node.getTextContent())); break;
				    case "cover":  mp3.setCover(Base64.decodeBase64(node.getTextContent())); break;
 				   	case "frame": 
 				   		NodeList frames = node.getChildNodes();
 				   		Frame frame = new Frame();
 				   		frame.setID(node.getAttributes().getNamedItem("ID").getTextContent());
 				   		for(int j = 0; j < frames.getLength(); j++){
 				   			Node frameNode = frames.item(j);
 				   			switch (frameNode.getNodeName()){
 				   			case "flags": 			frame.setFlags((short) (int) new Integer (frameNode.getTextContent())); break;
 				   			case "encodingflag": 	frame.setEncodingflag(Base64.decodeBase64(frameNode.getTextContent())[0]); break;
 				   			case "body":			frame.setBody(Base64.decodeBase64(frameNode.getTextContent())); break;
 				   			case "pictureType":		frame.setPictureType(Base64.decodeBase64(frameNode.getTextContent())[0]); break;
 				   			case "imageDescription":frame.setImageDescription(Base64.decodeBase64(frameNode.getTextContent())); break;
 				   			case "MIMEType":		frame.setMIMEType(Base64.decodeBase64(frameNode.getTextContent()));break;			
 				   			}
 				   		}
 				   		frameList.add(frame);
 				   		break;
 				   }
 				  mp3.setFrames(frameList);
 				  result = new DefaultMutableTreeNode(mp3);	   
 			   }
 		   }
 		  return result;         
 		}
 	/** Will write xml-representation of the given DefaulteMutableTreeNode into the file given with pathToDirectory
 	 * 
 	 * @param root - the root element of the DefaultMutableTreeNode
 	 * @param pathToDirectory - the location of the xml file
 	 * @throws ParserConfigurationException
 	 * @throws TransformerException
 	 */
 	public static void writeCache(DefaultMutableTreeNode root, String pathToDirectory) throws ParserConfigurationException, TransformerException {
 	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 	    Document doc = docBuilder.newDocument();
	    System.out.println("Writing Cache in the location " + pathToDirectory);
 	    Element rootElement = fillXml(doc,root);
 	    doc.appendChild(rootElement);
 	    // write the content into xml file
  		TransformerFactory transformerFactory = TransformerFactory.newInstance();
  		Transformer transformer = transformerFactory.newTransformer();
  		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
  		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
  		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "cache.dtd");
  		DOMSource source = new DOMSource(doc);
  		StreamResult result = new StreamResult(new File(pathToDirectory));
  		transformer.transform(source, result);
 	  
 	}
 	
 	/** Recursive function that will transform the given DefaultMutableTreeNode into a Element, uses doc as Element-creater-platform...
 	 * 
 	 * @param doc - the xml-document that serves as Element-creator
 	 * @param root - the DefaultMutableTreeNode that will be transformed
 	 * @return the xml-representation of the DefaultMutableTreeNode
 	 */		
 	private static Element fillXml(Document doc, DefaultMutableTreeNode root){
 		 Element currentRoot = doc.createElement("directory");
 		 currentRoot.setAttribute("filename",((Directory) root.getUserObject()).getPath());
 		 currentRoot.setAttribute("id", ((Directory) root.getUserObject()).getPath());
 		 for(int i = 0; i < root.getChildCount(); i++){
 		    	DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
 		    		if(node.getUserObject() instanceof MP3File) {
 		    			MP3File mp3 = (MP3File) node.getUserObject();
 		    			currentRoot.appendChild(MP3ToXML(mp3,doc));	
 		    		} else {
 		    			currentRoot.appendChild(fillXml(doc,(DefaultMutableTreeNode) root.getChildAt(i)));
 		    			
 		    		}
 		    }
 		 return currentRoot;
 		 
 	}
 	/** Takes a mp3 serialize it to xml
 	 * 
 	 * @param mp3 - the mp3 that shall be serialized
 	 * @param doc - the xml-document that serves as Element-creator
 	 * @return the serialized mp3 in form of an Element (w3c.dom)
 	 */
 	public static Element MP3ToXML(MP3File mp3, Document doc){
 	 Element currentElement = null, SubElement = null, SubSubElement = null;
 	 
 		currentElement = doc.createElement("mp3");
 		currentElement.setAttribute("filepath", mp3.getPath());
 		currentElement.setAttribute("id", mp3.getPath());
 			SubElement = doc.createElement("artist");
 			SubElement.appendChild(doc.createTextNode(mp3.getArtist()));
 			currentElement.appendChild(SubElement);
 			SubElement = doc.createElement("song");
 			SubElement.appendChild(doc.createTextNode(mp3.getSong()));
 			currentElement.appendChild(SubElement);
 			SubElement = doc.createElement("album");
 			SubElement.appendChild(doc.createTextNode(mp3.getAlbum()));
 			currentElement.appendChild(SubElement);
 			SubElement = doc.createElement("year");
 			SubElement.appendChild(doc.createTextNode(mp3.getYear()));
 			currentElement.appendChild(SubElement);
 			SubElement = doc.createElement("tagsize");
 			SubElement.appendChild(doc.createTextNode(((Integer)mp3.getSize()).toString()));
 			currentElement.appendChild(SubElement);
 			SubElement = doc.createElement("cover");
 			SubElement.appendChild(doc.createTextNode(new Base64().encodeToString(mp3.getCover())));
 			currentElement.appendChild(SubElement);
 			for(Frame f: mp3.getFrames()){
 				SubElement = doc.createElement("frame");
 				SubElement.setAttribute("ID", f.getID());
 				currentElement.appendChild(SubElement);
 					SubSubElement = doc.createElement("flags");
 					SubSubElement.appendChild(doc.createTextNode(((Integer)(int) f.getFlags()).toString()));
 					SubElement.appendChild(SubSubElement);
 					SubSubElement = doc.createElement("encodingflag");
 					SubSubElement.appendChild(doc.createTextNode(new Base64().encodeToString(new byte[]{f.getEncodingflag()})));
 					SubElement.appendChild(SubSubElement);
 					SubSubElement = doc.createElement("body");
 					SubSubElement.appendChild(doc.createTextNode(new Base64().encodeToString(f.getBody())));
 					SubElement.appendChild(SubSubElement);
 					if(f.getID().equals("APIC")){
 						SubSubElement = doc.createElement("pictureType");
 						SubSubElement.appendChild(doc.createTextNode(new Base64().encodeToString(new byte[]{f.getPictureType()})));
 						SubElement.appendChild(SubSubElement);
 						SubSubElement = doc.createElement("imageDescription");
 						SubSubElement.appendChild(doc.createTextNode(new Base64().encodeToString(f.getImageDescription())));
 						SubElement.appendChild(SubSubElement);
 						SubSubElement = doc.createElement("MIMEType");
 						SubSubElement.appendChild(doc.createTextNode(new Base64().encodeToString(f.getMIMEType())));
 						SubElement.appendChild(SubSubElement);
 					}
 			}
 			return currentElement;
 	}
 	
 	
 	/** Takes a directory and serialize it to xml
 	 * 
 	 * @param directory - the directory that shall be serialized
 	 * @param doc - the xml-document that serves as Element-creator
 	 * @return the serialized version of the directory
 	 */
 	public static Element DirectoryToXML(Directory directory, Document doc){
 		Element e = doc.createElement("directory");
 		e.setAttribute("filename", directory.getPath());
 		e.setIdAttribute("filename", true);
 		return e;
 	}
 	
 }
 
 
