 package ar.edu.itba.pdc.parser;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import ar.edu.itba.pdc.jabber.JabberElement;
 import ar.edu.itba.pdc.jabber.Message;
 import ar.edu.itba.pdc.jabber.Presence;
 import ar.edu.itba.pdc.stanzas.Stanza;
 
 public class XMPPHandler extends DefaultHandler {
 	
 	private List<Stanza> stanzas;
 	private Stanza currentStanza;
 	private int indentCount;
 	
 	private StringBuffer currentXMLElement;
 	
 	private ParsingState parsingState = ParsingState.parsingStart;
 	
 	public XMPPHandler() {
 		stanzas = new LinkedList<Stanza>();
 		indentCount = 0;
 	}
 	
 	public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
 		if (indentCount == 1) {
 			currentStanza = new Stanza();
 			currentXMLElement = new StringBuffer();
 			
 			/* Element name parsing */
 			if (elementName.equals("message")) {
 				currentStanza.setElement(JabberElement.createMessage(attributes.getValue("from"), attributes.getValue("to")));
 			} else if (elementName.equals("presence")) {
 				currentStanza.setElement(JabberElement.createPresence(attributes.getValue("from"), attributes.getValue("to")));
 				((Presence)currentStanza.getElement()).setType(attributes.getValue("type"));
 			} 	
 			
 		} else if (indentCount > 0){
 			if (currentStanza.isMessage()) {
 
 				/* Inner element name parsing */
 				if (elementName.equals("body")) {
 					this.parsingState = ParsingState.messageBody;
 				} else if (elementName.equals("delay")) {
 					this.parsingState = ParsingState.presenceDelay;
 				} else if (elementName.equals("active")) {
 					((Message)currentStanza.getElement()).setActiveXmlns(attributes.getValue("xmlns"));
 					this.parsingState = ParsingState.activeState;
 				}
 			}
 		}
 		
 		if (indentCount > 0)
 			currentXMLElement.append(generateXMLOpeningTag(elementName, attributes));
 		
 		indentCount++; 
 	}	
 	 
 	public void endElement(String s, String s1, String element) throws SAXException {
 		 indentCount--;
 		 if (indentCount > 0)
 			 currentXMLElement.append(generateXMLClosingTag(element));
 		 if (indentCount == 1) {
 			 currentStanza.complete();
 			 currentStanza.setXMLString(currentXMLElement.toString());
 			 stanzas.add(currentStanza);
 			 System.out.println("Completada stanza: " + element);
 		 }
 	}
 	
 	public void characters(char[] ch, int start, int length)
 			throws SAXException {
 		String str = new String(ch).substring(start, start + length);
 		switch(parsingState) {
 			case messageBody:
 				((Message)(currentStanza.getElement())).setMessage(str);
 				parsingState = ParsingState.parsingStart;
 				break;
 			case presenceDelay:
 				if (currentStanza.isPresence())
 					((Presence)currentStanza.getElement()).setDelay(str);
 				break;
 			case activeState:
 				if (currentStanza.isMessage())
 					((Message)currentStanza.getElement()).setActive(str);
 				break;
 		}
		if (indentCount > 0 && currentXMLElement != null) 
 			currentXMLElement.append(str);
 	}
 	
 	public List<Stanza> getStanzaList() {
 		return stanzas;
 	}
 	
 	public boolean hasIncompleteElements() {
 		return currentStanza == null || !currentStanza.isComplete();
 	}
 	
 	public void setState(ParsingState state) {
 		this.parsingState = state;
 	}
 	
 	private StringBuffer generateXMLOpeningTag(String name, Attributes attributes) {
 		StringBuffer sb = new StringBuffer("<");
 		sb.append(name);
 		for (int i = 0; i < attributes.getLength(); i++) {
 			sb.append(" ");
 			sb.append(attributes.getLocalName(i));
 			sb.append("='");
 			sb.append(attributes.getValue(i));
 			sb.append("'");
 		}
 		sb.append(">");
 		return sb;
 	}
 	
 	private StringBuffer generateXMLClosingTag(String name) {
 		return new StringBuffer("</" + name + ">");
 	}
 
 }
