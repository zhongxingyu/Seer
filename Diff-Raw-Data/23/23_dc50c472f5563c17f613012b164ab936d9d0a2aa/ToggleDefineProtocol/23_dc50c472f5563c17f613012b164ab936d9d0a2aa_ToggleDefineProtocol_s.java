 package nl.tue.fingerpaint.client.gui.buttons;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ToggleButton;
 
 /**
  * ToggleButton that can be used to indicate if a protocol is being defined. If
  * not in default state, then adding a step will execute it immediately.
  * 
  * @author Group Fingerpaint
  */
 public class ToggleDefineProtocol extends Button implements ClickHandler {
 
 	/**
 	 * Reference to the entrypoint. Used to change the visibility of the
 	 * protocol widgets.
 	 */
 	protected Fingerpaint fp;
 
 	/**
 	 * Variable that represents whether the protocol widgets are hidden (not
 	 * visible)
 	 */
 	protected boolean isHidden;
 
 	/**
 	 * Construct a new {@link ToggleButton} that can be used to indicate if a
 	 * protocol is being defined.
 	 * 
 	 * @param parent
 	 *            Reference to the entrypoint, used to change the visibility of
 	 *            the protocol widgets.
 	 * @see ToggleDefineProtocol
 	 */
 	public ToggleDefineProtocol(Fingerpaint parent) {
 		super(FingerpaintConstants.INSTANCE.btnDefProt());
 		this.fp = parent;
 		addClickHandler(this);
 		ensureDebugId("ToggleDefineProtocol");
 		this.setText("Define protocol");
 		this.isHidden = true;
 
 		// TODO: Style
 	}
 
 	/**
 	 * Toggles the protocol widgets as visible or invisible, and resets the
 	 * protocol if the widgets are being hidden.
 	 * 
 	 * @param event
 	 *            The event that has fired.
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 		if (isHidden) {
 			fp.setProtocolWidgetsVisible(true);
 			this.setText("Hide Define protocol");
 		} else {
			fp.resetProtocol();
 			fp.setProtocolWidgetsVisible(false);
 			this.setText("Define protocol");
 		}
 		isHidden = !isHidden;
 	}
 
 	/**
 	 * Returns whether the protocol widgets are hidden (not visible)
 	 * 
 	 * @return Boolean that represents whether the protocol widgets are hidden
 	 *         (not visible)
 	 */
 	public boolean isHidden() {
 		return isHidden;
 	}
 }
