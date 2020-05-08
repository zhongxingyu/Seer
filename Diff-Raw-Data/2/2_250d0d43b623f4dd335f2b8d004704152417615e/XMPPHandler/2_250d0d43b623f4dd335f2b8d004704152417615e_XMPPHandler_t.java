 package ar.edu.itba.pdc.parser;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import ar.edu.itba.pdc.jabber.JIDConfiguration;
 import ar.edu.itba.pdc.jabber.JabberElement;
 import ar.edu.itba.pdc.jabber.Message;
 import ar.edu.itba.pdc.jabber.Presence;
 import ar.edu.itba.pdc.stanzas.Stanza;
 
 public class XMPPHandler extends DefaultHandler {
 	
 	private List<Stanza> stanzas;
 	private Stanza currentStanza;
 	private int indentCount;
 	
 	private ParsingState parsingState = ParsingState.parsingStart;
 	private StateCallback callback = null;
 	
 	public XMPPHandler(StateCallback callback) {
 		this.callback = callback;
 		stanzas = new LinkedList<Stanza>();
 		indentCount = 0;
 	}
 	
 	public XMPPHandler(StateCallback callback, ParsingState state) {
 		this(callback);
 		this.parsingState = state;
 	}
 	
 	public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
 		if (indentCount == 1) {
 			currentStanza = new Stanza();
 			
 			/* Element name parsing */
 			if (elementName.equals("message")) {
 				currentStanza.setElement(JabberElement.createMessage(attributes.getValue("from"), attributes.getValue("to")));
 			} else if (elementName.equals("presence")) {
 				currentStanza.setElement(JabberElement.createPresence(attributes.getValue("from"), attributes.getValue("to")));
 				((Presence)currentStanza.getElement()).setType(attributes.getValue("type"));
 			} else if (elementName.equals("challenge")) {
 				if (attributes.getValue("xmlns") != null && attributes.getValue("xmlns").equals("urn:ietf:params:xml:ns:xmpp-sasl")) {
 					callback.changeState(ParsingState.waitingClientAuthResponse);
 				}
 			} else if (elementName.equals("response") && parsingState == ParsingState.waitingClientAuthResponse) {
 				if (attributes.getValue("xmlns").equals("urn:ietf:params:xml:ns:xmpp-sasl")) {
 					currentStanza.setElement(JabberElement.createJIDConfiguration());
 					parsingState = ParsingState.authBody;
 				}
 			}
 			
 			
 		} else if (indentCount > 0){
 			if (currentStanza.isMessage()) {
 				
 				/* Inner element name parsing */
 				if (elementName.equals("body")) {
 					this.parsingState = ParsingState.messageBody;
 				} else if (elementName.equals("delay")) {
 					this.parsingState = ParsingState.presenceDelay;
 				}
 			}
 		}
 		
 		 indentCount++; 
 	}	
 	 
 	public void endElement(String s, String s1, String element) throws SAXException {
 		 indentCount--;
 		 if (indentCount == 1) {
 			 currentStanza.complete();
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
 			case authBody:
 				((JIDConfiguration)(currentStanza.getElement())).setJID(str);
 				break;
 			case presenceDelay:
 				((Presence)currentStanza.getElement()).setDelay(str);
 				break;
 		}
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
 
 }
