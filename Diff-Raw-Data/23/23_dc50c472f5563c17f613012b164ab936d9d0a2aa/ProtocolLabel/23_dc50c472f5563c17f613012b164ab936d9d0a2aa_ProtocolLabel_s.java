 package nl.tue.fingerpaint.client.gui.labels;
 
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * Header for the protocol representation.
  * 
  * @author Group Fingerpaint
  */
 public class ProtocolLabel extends Label {
 
 	/**
 	 * Construct the {@link ProtocolLabel}.
 	 * 
 	 * Initially, this label is not visible and gets a special ID.
 	 */
 	public ProtocolLabel() {
 		super(FingerpaintConstants.INSTANCE.lblProtocol());
 		setVisible(false);
		ensureDebugId("protocolLabel");
 	}
 	
 }
