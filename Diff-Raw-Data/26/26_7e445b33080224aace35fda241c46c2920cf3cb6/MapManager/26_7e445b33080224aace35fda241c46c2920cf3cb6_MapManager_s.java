 package mapeditor;
 
 import java.awt.Image;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 /**
  * @author Lionleaf
  *	A class to handle tilesets and tiles. TODO make it non-static, really bad code.
  */
 public class MapManager {
 	private static int currentID = 0;
 	private static int currentMapOID = 1;
 
 	public static void writeMap(File file){
 //		DataOutputStream os = null;
 //		try {
 //			os = new DataOutputStream(new FileOutputStream(file));
 //		} catch (FileNotFoundException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //			return;
 //		}
 //
 //
 //		byte id = 1;
 //		try {
 //			os.write(id);
 //			os.write(CData.getArraySizeX());
 //			os.write(CData.getArraySizeY());
 //			for(int y = 0; y < CData.getArraySizeY(); y++){
 //				for(int x = 0; x < CData.getArraySizeX(); x++){
 //					os.write(CData.level[x][y].getLImageID());
 //				}
 //			}
 //			os.close();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} 
 	}
 	
 	public static void loadTestMap(){
 		CData.loadLevel(64*8, 32*8, 64, 64/2);
 		CData.mainFrame.repaint();
 	}
 
 	public static void loadMap(File file) {
 //		FileInputStream input = null;
 //		try {
 //			input = new FileInputStream(file);
 //		} catch (FileNotFoundException e) {
 //			JOptionPane.showMessageDialog(CData.mainFrame, "Failed to open map file!");
 //			return;
 //		}
 //
 //		try {
 //			int id = input.read();
 //			int height = input.read();
 //			int width = input.read();
 //
 //			for (int y = 0; y < width; y++) {
 //				for (int x = 0; x < height; x++) {
 //					CData.level[x][y] = new Tile((byte) input.read());
 //				}
 //			}
 //			CData.setLevelSizeX(width);
 //			CData.setLevelSizeY(height);
 //
 //		} catch (IOException e) {
 //			JOptionPane.showMessageDialog(CData.mainFrame, "Map file corrupt!");
 //			return;
 //		}
 //		CData.mainFrame.repaint();
 
 	}
 
 	public static void writeTileSet(String file){
 		try{
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
 			//root elements
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("TileSet");
 			rootElement.setAttribute("nr_of_tiles", CData.images.size()+"");
 			rootElement.setAttribute("nr_of_movetypes", CData.tileTypes.length + "");
 			
 			
 			doc.appendChild(rootElement);
 
 			for(LImage tile : CData.images.values()){
 				Element tileElem = doc.createElement("Tile");
 				rootElement.appendChild(tileElem);
 
 				tileElem.setAttribute("id", ""+tile.getID());
 
 				tileElem.setAttribute("resource",tile.getResource());
 
 				for (int i = 0; i < CData.tileTypes.length; i++) {
 					//Create the element
 					Element state = doc.createElement("state");
 					tileElem.appendChild(state);
 					//Set the name of the state
 					state.setAttribute("name", CData.tileTypes[i]);
 					//Calculate the value
 //					String value = generateTileStatesString(tile.getTileStates(), i);
 //					Text stateVal = doc.createTextNode(value);
 //					//Append value
 //					state.appendChild(stateVal);
 				}
 
 			}
 
 
 			saveToFile(file, doc);
 
 		}catch(ParserConfigurationException pce){
 			pce.printStackTrace();
 		}
 	}
 
 
 	public static void saveToFile(String fileName, Document doc){
 
 		TransformerFactory xf = TransformerFactory.newInstance();
 		xf.setAttribute("indent-number", new Integer(2));
 
 		Transformer xformer;
 		try {
 
 			xformer = xf.newTransformer();
 			xformer.setOutputProperty(OutputKeys.METHOD, "xml");
 			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 
 			File file = new File(".", fileName);
 			OutputStream os = new FileOutputStream(file);
 
 			Result result = new StreamResult(new OutputStreamWriter(os,
 			"UTF-8"));
 
 			xformer.transform(new DOMSource(doc), result);
 
 
 		} catch (TransformerConfigurationException e) {
 
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 
 			e.printStackTrace();
 		} catch (TransformerException e) {
 
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 
 			e.printStackTrace();
 		}
 	}
 
 //	private static String generateTileStatesString(boolean[][][] tileStates, int k) {
 //		StringBuffer sb = new StringBuffer();
 //		int val;
 //		for (int i = 0; i < CData.TILE_BLOCKS; i++) {
 //			for (int j = 0; j < CData.TILE_BLOCKS; j++) {
 //				val = tileStates[i][j][k] ? 1 : 0; 
 //				sb.append(val + "");
 //			}
 //		}
 //		return sb.toString();
 //	}
 
 //	public static void loadTileSet(File file){
 //		try {
 //			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 //			DocumentBuilder docBuilder;
 //
 //			docBuilder = docFactory.newDocumentBuilder();
 //
 //			Document doc = docBuilder.parse(file);
 //
 //			doc.getDocumentElement().normalize();
 //
 //			NodeList nodeList = doc.getElementsByTagName("Tile");
 //
 //			CData.tiles.clear();
 //
 //			for (int i = 0; i < nodeList.getLength(); i++) {
 //				Node node = nodeList.item(i);
 //				NamedNodeMap attMap = node.getAttributes();
 //				int id = Integer.parseInt(
 //						attMap.getNamedItem("id").getTextContent());
 //				String resource = attMap.
 //					getNamedItem("resource").getTextContent();
 //
 //				String imgstr = resource + ".png";
 //
 //				Image img = new ImageIcon(imgstr).getImage();
 //
 //				Tile tile = new Tile(img, (byte)id, imgstr);
 //
 //				//Parse children
 //				NodeList childList = node.getChildNodes();
 //				for(int j = 0; j < childList.getLength(); j++){
 //					Node n = childList.item(j);
 //					if(n.getNodeName() == "state"){
 //						NamedNodeMap stateMap = n.getAttributes();
 //						
 //						String stateType = stateMap.
 //							getNamedItem("name").getTextContent();
 //						
 //						//Find the movetype
 //						for (int k = 0; k < CData.moveTypes.length; k++) {
 //							if(CData.moveTypes[k].equals(stateType)){
 //								addMoveTypes(tile, k, n.getTextContent());
 //								break;
 //							}
 //						}
 //						
 //					}
 //				}
 //				
 //				
 //				CData.tiles.put(id,tile);
 //			}
 //
 //		} catch (ParserConfigurationException e) {
 //
 //			e.printStackTrace();
 //		} catch (SAXException e) {
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //
 //			e.printStackTrace();
 //		}
 //		
 //		if(CData.tileChoosePanel != null){
 //			CData.tileChoosePanel.updateList();
 //		}
 //		if(CData.mainFrame != null){
 //			CData.mainFrame.repaint();
 //		}
 //
 //	}
 	public static void removeImage(LImage image) {
 		CData.images.remove(image.getID());
 
 		if(CData.mainFrame != null){
 			CData.mainFrame.repaint();
 		}
 
 	}
 
 	public static void addImages(File[] imgFiles){
 		for(File file : imgFiles){
 			Image image = new ImageIcon(file.toString()).getImage();
 			CData.images.put(currentID, new LImage(image, (byte)currentID, file.getName()));
 			currentID++;
 		}
 		if(CData.mainFrame != null){
 			CData.mainFrame.repaint();
 		}
 	}
 	
 	public static void addMapO(){
 		addMapO(new MapObject((short) currentMapOID));
 	}
 	
 	public static void addMapO(MapObject mapO){
 		CData.mapObjects.put(mapO.getID(), mapO);
 		if(mapO.getName() == null){
 			mapO.setName("MapO " + currentMapOID);
 		}
 		currentMapOID++;
		if(CData.imgChoosePanel != null){
			CData.imgChoosePanel.updateList();
			CData.imgChoosePanel.repaint();
		}
 	}
 	
 	public static void removeMapO(MapObject mapO){
 		CData.mapObjects.remove(mapO.getID());
 		if(CData.mainFrame != null){
 			CData.mainFrame.repaint();
 		}
 	}
 }
