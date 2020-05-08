 package petter.bjelm;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class ImageParsing {
 
 	private String monthText = "00";
 	private String type = "Grnsaker";
 	private ArrayList<String> imgNamesArray = new ArrayList<String>();
 	private ArrayList<String> imgURLArray = new ArrayList<String>();
 	private Document doc;
 	private Document imgDoc;
 
 	public ImageParsing() {
 		
 	}
 
 	// * ndrar URL till dagens datum */
 	public String getCurrDate() {
 		Calendar cal = Calendar.getInstance();
 		int month = cal.get(Calendar.MONTH) + 1;
 		if (month < 10) {
 			monthText = "0" + Integer.toString(month);
 		} else {
 			monthText = Integer.toString(month);
 		}
 		return monthText;
 	}
 
 	public void parse() throws IOException, ParserConfigurationException,
 			SAXException, TransformerException {
 		URL url = new URL(
 				"http://xn--ssongsmat-v2a.nu/w/api.php?format=xml&action=ask&query=[[Kategori:"
 						+ type + "]][[I+ssong+Z3::1912-" + monthText + "-"
 						+ "15" + "]]|?bild");
 		URLConnection conn = url.openConnection();
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		doc = builder.parse(conn.getInputStream());
 
 		doc.getDocumentElement().normalize();
 	}
 
 	public String parseImg(String imgText) throws IOException,
 			ParserConfigurationException, SAXException, TransformerException {
 		String s = null;
 		URL url = new URL(
 				"http://xn--ssongsmat-v2a.nu/w/api.php?format=xml&action=query&titles="
 						+ imgText.replace(" ", "%20")
 						+ "&prop=imageinfo&iiprop=url");
 		URLConnection conn = url.openConnection();
 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		imgDoc = builder.parse(conn.getInputStream());
 
 		imgDoc.getDocumentElement().normalize();
 
 		Element imgRoot = imgDoc.getDocumentElement();
 		NodeList imgNodes = imgRoot.getElementsByTagName("ii");
 		for (int i = 0; i < imgNodes.getLength(); i++) {
 			Node imgData = imgNodes.item(i);
 
 			if (imgData instanceof Element) {
 				Element name = (Element) imgData;
 				s = name.getAttribute("url");
 
 			}
 		}
 		return s;
 	}
 
 	public ArrayList<String> getURLArray(String kat) throws IOException,
 			ParserConfigurationException, SAXException, TransformerException {
 		System.out.println("Loading image URL:s......");
 		
 		type = kat;
 		
		imgNamesArray.clear();
 		imgURLArray.clear();
 		
 		parse();
 
 		Element root = doc.getDocumentElement();
 		NodeList nodes = root.getElementsByTagName("value");
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node data = nodes.item(i);
 
 			if (data instanceof Element) {
 				Element name = (Element) data;
 				imgNamesArray.add(name.getAttribute("fulltext"));
 
 			}
 		}
 
 		for (int i = 0; i < imgNamesArray.size(); i++) {
 			imgURLArray.add(parseImg(imgNamesArray.get(i)));
 		}
 		return imgURLArray;
 	}
 
 	public void setDate(String date) {
 
 		monthText = date;
 
 	}
 }
