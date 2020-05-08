 /**
  * 
  */
 package ee.olegus.fortran.ast;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import ee.olegus.fortran.ast.text.Location;
 
 /**
  * Class that all statements should inherit from.
  * 
  * @author olegus
  *
  */
 public abstract class Statement extends Construct {
	private Location location;
 	private String label;
 
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public Location getLocation() {
 		if(location==null)
 			location = new Location();
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 	
 	public void generateXML(ContentHandler handler) throws SAXException {
 		if(label!=null) {
 			handler.startElement("", "", "label", null);
 			handler.characters(label.toCharArray(), 0, label.length());
 			handler.endElement("", "", "label");
 		}
 		if(location!=null) {
 			location.generateXML(handler);
 		}
 	}	
 }
