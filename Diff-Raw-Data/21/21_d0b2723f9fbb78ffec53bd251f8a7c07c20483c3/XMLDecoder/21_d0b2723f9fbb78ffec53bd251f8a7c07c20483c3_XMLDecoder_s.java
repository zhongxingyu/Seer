 /**
  * 
  */
 package pt.utl.ist.tese.utils;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.grlea.log.SimpleLogger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import pt.utl.ist.tese.domain.ImageInfo;
 import pt.utl.ist.tese.main.Main;
 
 
 
 /**
  * @author averissimo
  *
  */
 public class XMLDecoder {
 
 	private static final SimpleLogger log = new SimpleLogger(XMLDecoder.class);
 
 	private static final String XML_PATH = Main.ARTIST_PATH + File.separator + "var" + File.separator + "a.xml";
 	
 	public static ArrayList<ImageInfo> read() {
 
 		ArrayList<ImageInfo> list = new ArrayList<ImageInfo>();
 		
 		try {
 			File file = new File(XML_PATH);
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document doc = db.parse(file);
 			doc.getDocumentElement().normalize();
 			NodeList nodeLst = doc.getElementsByTagName("row");
 
 			for (int s = 0; s < nodeLst.getLength(); s++) {				
 				Node fstNode = nodeLst.item(s);
 				if ( s != 0 && s % 10000 == 0 )
 					log.info("s: " + s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
 					Element fstElmnt = (Element) fstNode;
 					NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("field");
 					Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
 					NodeList fstNm = fstNmElmnt.getChildNodes();
					String path = ((Node) fstNm.item(0)).getNodeValue();
 					String dir = Main.ARTIST_PATH + File.separator + "cbir";
 					path = path.replace( "/Users/averissimo/artists/cbir/64" , "" );
 					ImageInfo img = new ImageInfo( s , ImageInfo.SUBSPACES.length );
 					boolean status = true;
 					for ( int i = 0 ; i < ImageInfo.SUBSPACES.length ; i++ ) {
 						File f = new File( dir + File.separator + ImageInfo.SUBSPACES[i] + path );
 						img.insertPixelVector(f, ImageInfo.SUBSPACES[i] , i );
 						status = status && f.exists();
 						if ( !f.exists())
 							log.error("does not exists -- " + f.getAbsolutePath());
 					}
 					if ( status )
 						list.add(img);
 				}
 
 			}
 		} catch (Exception e) {
 			log.error("error: " + e.getMessage());
 		}
 		return list;
 	}
 } 
