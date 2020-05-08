 package org.jcopybook;
 
 import net.sf.cb2xml.util.XmlUtils;
 import org.w3c.dom.Document;
 
 /**
  * User: miolini
  * Date: 22.05.12 3:42
  */
 public class JCopybookException extends Exception {
 	protected String contextIn;
 	protected String contextOut;
 	protected String elementName;
 	protected String elementValue;
 	protected String layout;
 
 	public JCopybookException(String message, Throwable throwable, String contextIn, String contextOut, String elementName, String elementValue, Document layout) {
		super(message, throwable);
 		this.contextIn = contextIn;
 		this.contextOut = contextOut;
 		this.elementName = elementName;
 		this.elementValue = elementValue;
 		this.layout = XmlUtils.domToString(layout).toString();
 	}
 
 	public String getContextIn() {
 		return contextIn;
 	}
 
 	public void setContextIn(String contextIn) {
 		this.contextIn = contextIn;
 	}
 
 	public String getContextOut() {
 		return contextOut;
 	}
 
 	public void setContextOut(String contextOut) {
 		this.contextOut = contextOut;
 	}
 
 	public String getElementName() {
 		return elementName;
 	}
 
 	public void setElementName(String elementName) {
 		this.elementName = elementName;
 	}
 
 	public String getElementValue() {
 		return elementValue;
 	}
 
 	public void setElementValue(String elementValue) {
 		this.elementValue = elementValue;
 	}

	@Override
	public String toString() {
		return "JCopybookException{" +
				"message='" + getMessage() + "\'\n" +
				", contextIn='" + contextIn + "\'\n" +
				", contextOut='" + contextOut + "\'\n" +
				", elementName='" + elementName + "\'\n" +
				", elementValue='" + elementValue + "\'\n" +
				", layout='" + layout + "\'\n" +
				'}';
	}
 }
