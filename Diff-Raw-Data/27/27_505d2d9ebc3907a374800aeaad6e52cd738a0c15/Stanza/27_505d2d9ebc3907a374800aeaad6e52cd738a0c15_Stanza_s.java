 package ar.edu.itba.pdc.stanzas;
 
 import ar.edu.itba.pdc.jabber.JabberElement;
 import ar.edu.itba.pdc.jabber.Message;
 import ar.edu.itba.pdc.jabber.Presence;
 
 public class Stanza {
 
 	private StanzaType type;
 	private boolean completed, rejected;
 	private JabberElement element;
 	private String xmlString = "";
 	
 	public Stanza() {
 		this.completed = false;
 		this.rejected = false;
 	}
 	
 	public Stanza(StanzaType type) {
 		this();
 		this.type = type;
 	}
 	
 	public Stanza(StanzaType type, JabberElement element) {
 		this(type);
 		this.element = element;
 	}
 	
 	public String getType() {
 		return type.toString();
 	}
 	
 	public String getXMLString() {
 		return xmlString;
 	}
 	
 	public void setXMLString(String xmlString) {
 		this.xmlString = xmlString;
 	}
 	
 	public void complete() {
 		this.completed = true;
 	}
 	
 	public void setType(StanzaType type) {
 		this.type = type;
 	}
 	
 	public boolean isComplete() {
 		return completed;
 	}
 	
 	public boolean isMessage() {
 		return element != null && element.getClass() == Message.class;
 	}
 	
 	public boolean isPresence() {
 		return element != null && element.getClass() == Presence.class;
 	}
 	
 	public JabberElement getElement() {
 		return element;
 	}
 	
 	@Override
 	public String toString() {
 		if (element != null)
 			return element.toString();
 		return "Default Stanza";
 	}
 	
 	public void setElement(JabberElement element) {
 		this.element = element;
 	}
 	
 	public void reject() {
 		this.rejected = true;
 	}
 	
 	public boolean isrejected() {
 		return rejected;
 	}
 	
 	
 	
 }
