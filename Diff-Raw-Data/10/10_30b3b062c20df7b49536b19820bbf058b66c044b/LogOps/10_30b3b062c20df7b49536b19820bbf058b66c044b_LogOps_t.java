 package ecologylab.services.logging;
 
 import java.util.ArrayList;
 
 import ecologylab.xml.ArrayListState;
 import ecologylab.xml.VectorState;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.XmlTranslationException;
 
 /**
  * Send an intermediate sequence of ops to the logging server.
  * 
  * 1) Keep mixed initiative loggin data Set
  * 2) Handle recieved logging messages from client 
  * 
  * @author eunyee
  *
  */
 public class LogOps extends LogRequestMessage
 {
	public ArrayList set	=	new ArrayList();
 	
 	public void addNestedElement(ElementState elementState)
 	{
 		if (elementState instanceof MixedInitiativeOp)
 			set.add(elementState);
 	}
 	
 	public void clearSet()
 	{
 		set.clear();
 	}
 
 	static final String START			= "<log_ops>";
 	static final int	START_OFFSET	= START.length();
 	static final String	END				= "</log_ops>";
 	
 	/**
 	 * The string that the LoggingServer will write.
 	 * Eliminates the outer <log_ops> XML element.
 	 */
 	String getMessageString() throws XmlTranslationException
 	{
 		String xmlString	= xmlString();
	
 		int start			= xmlString.indexOf(START) + START_OFFSET;
 		int end				= xmlString.indexOf(END);
		if( (start==-1) || (end==-1) )
		{
			debug("RECEIVE MESSAGE : " + xmlString);
			return "\n";
		}
 		return (String) xmlString.substring(start, end) + "\n";
 	//	return (String)this.translateToXML(false) + "\n";
 	}
 	
 }
