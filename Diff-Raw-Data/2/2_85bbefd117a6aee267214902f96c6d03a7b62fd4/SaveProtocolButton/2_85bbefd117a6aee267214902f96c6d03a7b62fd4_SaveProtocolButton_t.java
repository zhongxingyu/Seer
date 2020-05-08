 package nl.tue.fingerpaint.client.gui.buttons;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 
 /**
  * Button that can be used to save a mixing protocol.
  * 
  * @author Group Fingerpaint
  */
 public class SaveProtocolButton extends Button implements ClickHandler {
 	
 	/**
 	 * Reference to the entrypoint. Used to change which save button has been clicked
 	 * last.
 	 */
 	protected Fingerpaint fp;
 
 	/**
 	 * Construct a new button that can be used to save a mixing
 	 * protocol. When clicked, it will open the save item panel.
 	 * 
 	 * @param parent
 	 *            Reference to the entrypoint, used to change which save button has
 	 *            been clicked last.
 	 */
 	public SaveProtocolButton(Fingerpaint parent){
 		super(FingerpaintConstants.INSTANCE.btnSaveProt());
 		this.fp = parent;
 		addClickHandler(this);
		// Initially, saving a protocol is not possible (no protocol has been defined yet).
		setEnabled(false);
 		ensureDebugId("saveProtocolButton");
 	}
 	
 	/**
 	 * Creates the save protocol popup panel.
 	 * @param event The event that has fired.
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 		fp.setLastClicked(StorageManager.KEY_PROTOCOLS);
 		fp.showSavePanel();
 	}
 }
