 package nl.tue.fingerpaint.client.gui.buttons;
 
import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 
 /**
  * Button to reset the current distribution to completely white.
  * 
  * @author Group Fingerpaint
  */
 public class ResetDistButton extends Button implements ClickHandler {
 
 	/** Reference to the model. Used to reset the current distribution. */
 	protected ApplicationState as;
 
 	/**
 	 * Construct a new button that can be used to reset the current 
 	 * concentration distribution to a completely white distribution.
 	 * 
 	 * @param appState
 	 *            Reference to the model, used to reset the current
 	 *            distribution to completely white.
 	 */
 	public ResetDistButton(ApplicationState appState) {
 		super(FingerpaintConstants.INSTANCE.btnResetDist());
 		this.as = appState;
 		addClickHandler(this);
 		ensureDebugId("resetDistButton");
 	}
 
 	/**
 	 * Resets the distribution to a completely white distribution.
 	 * @param event The event that has fired.
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 		as.getGeometry().resetDistribution();
		GuiState.viewSingleGraphButton.setEnabled(false);
 	}
 }
