 package se.softhouse.garden.orchid.vaadin.widgetset.client.ui;
 
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.ui.VTextField;
 
 public class VOrchidInputField extends VTextField {
 
 	public VOrchidInputField() {
		getElement().setAttribute("type", "text");
 	}
 
 	/**
 	 * Called whenever an update is received from the server
 	 */
 	@Override
 	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
 
 		// Process attributes/variables from the server
 		// The attribute names are the same as we used in
 		// paintContent on the server-side
 		String type = uidl.getStringAttribute("type");
 		String pattern = uidl.getStringAttribute("pattern");
 		String step = uidl.getStringAttribute("step");
 
 		getElement().setAttribute("type", type);
 		if (pattern != null) {
 			getElement().setAttribute("pattern", pattern);
 		}
 		if (step != null) {
 			getElement().setAttribute("step", step);
 
 		}
 	}
 
 }
