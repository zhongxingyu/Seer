 package net.unikernel.bummel.project_model.api;
 
 import java.io.IOException;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Locale;
 import org.openide.util.Exceptions;
import org.openide.xml.EntityCatalog;
 import org.openide.xml.XMLUtil;
 import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author mcangel
  */
 public class PortsScanner
 {
 	HashMap<String, String> portsDirections = new HashMap<>();
 	HashMap<String, Double> portsOffsets = new HashMap<>();
 
 	/**
 	 * Create new PortsScanner with org.w3c.dom.Document.
 	 */
 	public PortsScanner(Document document)
 	{
 		org.w3c.dom.NodeList nodes = 
 				((org.w3c.dom.Element) document.getElementsByTagName("ports")
 				.item(0)).getElementsByTagName("port");
 		for (int i = 0; i < nodes.getLength(); i++)
 		{
 			org.w3c.dom.Element el = (org.w3c.dom.Element) nodes.item(i);
 			portsDirections.put(el.getAttribute("name"),
 					el.getAttribute("direction"));
 			try
 			{
 				portsOffsets.put(el.getAttribute("name"),
 						NumberFormat.getNumberInstance()
 						.parse(el.getAttribute("offset")
 						.replaceAll("\\.", 
 						new String(new char[]{DecimalFormatSymbols.getInstance()
 						.getDecimalSeparator()}))).doubleValue());
 			} catch (ParseException ex)
 			{
 				Exceptions.printStackTrace(ex);
 			}
 		}
 	}
 	
 	/**
 	 * Create new PortsScanner for the provided BasicElement.
 	 */
 	public PortsScanner(BasicElement element) throws IOException, SAXException
 	{
		this(XMLUtil.parse(new InputSource(element.getClass().getResourceAsStream(
				//check if path to the xml file with data is annotated
				element.getClass().getAnnotation(PortsData.class)==null?
				//if not - default
				"ports.xml":
				//if annotated - get it
				element.getClass().getAnnotation(PortsData.class).portsFile())),
				true, false, null, EntityCatalog.getDefault()));
 	}
 	
 	public String getPortDirection(String port)
 	{
 		return portsDirections.get(port);
 	}
 	
 	public Double getPortOffset(String port)
 	{
 		return portsOffsets.get(port);
 	}
 }
