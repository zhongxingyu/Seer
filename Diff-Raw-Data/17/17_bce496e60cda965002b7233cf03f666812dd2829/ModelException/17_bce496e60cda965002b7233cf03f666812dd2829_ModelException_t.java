 package Sergi.MVC.Controller;
 
 import java.io.IOException;
 import java.text.ParseException;
 
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.w3c.dom.DOMException;
 import org.xml.sax.SAXException;
 
 
 public class ModelException extends Exception {
 
 	Exception e = new Exception();
	String string = "";
 	private static final long serialVersionUID = 4360693429330353093L;
 
 	public ModelException(ClassNotFoundException e) {
 		this.e = e;
 	}
 
	public ModelException(ClassNotFoundException e, String string) {
		this.e = e;
		this.string = string;
	}
	
 	public ModelException(InstantiationException e) {
 		this.e = e;
 	}
 
 	public ModelException(IllegalAccessException e) {
 		this.e = e;
 	}
 
 	public ModelException(UnsupportedLookAndFeelException e) {
 		this.e = e;
 	}
 
 	public ModelException(DOMException e) {
 		this.e = e;
 	}
 
 	public ModelException(ParserConfigurationException e) {
 		this.e = e;
 	}
 
 	public ModelException(SAXException e) {
 		this.e = e;
 	}
 
 	public ModelException(IOException e) {
 		this.e = e;
 	}
 
 	public ModelException(ParseException e) {
 		this.e = e;
 	}
 
 	public ModelException(TransformerConfigurationException e) {
 		this.e = e;
 	}
 
 	public ModelException(TransformerException e) {
 		this.e = e;
 	}
 
 	private String getExceptionDetals(Exception e) {
 		StringBuilder str = new StringBuilder(e.getMessage() + "\n");
 		for(int i = 0; i < e.getStackTrace().length; i++)
 			str.append("    " + e.getStackTrace()[i] + "\n");
 		return str.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
		if(string != "")
			return "! " + string;
 		return getExceptionDetals(e);
 	}
 	
 	
 }
