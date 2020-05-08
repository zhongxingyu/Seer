 package battleships.message;
 
 import com.thoughtworks.xstream.XStream;
 
 public abstract class Message {
 	protected String type;
 	
 	public Message(String type){
 		this.type=type;
 	}
 	
 	/**
 	 * Converts the object to an XML-string.
 	 * */
	public String toXML(){
 		String xml;
 		XStream xstream = new XStream();
 		xml = xstream.toXML(this);
 		return xml;
 	}
 	
 	/**
 	 * Converts an XML String to a Message.
 	 */
 	public static Message toMessage(String data)
 	{
 		XStream xstream = new XStream();
 		Message message;
 		try
 		{
 			message = (Message)xstream.fromXML(data);
 		}
 		catch(Exception e)
 		{
 			return null;
 		}
 		return message;
 	}
 	
 	/**
 	 * Returns the type of concrete message.
 	 * */
 	public String getType(){return type;}
 }
