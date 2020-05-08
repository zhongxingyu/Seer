 package nl.tue.fingerpaint.client.gui.buttons;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 
 /**
  * Button that can be used to execute the current mixing protocol.
  * 
  * @author Group Fingerpaint
  */
 public class MixNowButton extends Button implements ClickHandler {
 	/**
 	 * Reference to the entrypoint. Used to execute the current mixing protocol.
 	 */
 	protected Fingerpaint fp;
 
 	/**
 	 * Reference to the model. Used to retrieve the current mixing protocol from
 	 * the entrypoint.
 	 */
 	protected ApplicationState as;
 
 	/**
 	 * Construct a new button that can be used to execute the current mixing
 	 * protocol.
 	 * 
 	 * @param parent
 	 *            Reference to the entrypoint, used to execute the current
 	 *            mixing protocol.
 	 * @param appState
 	 *            Reference to the model, used to retrieve the current mixing
 	 *            protocol from the parent.
 	 */
 	public MixNowButton(Fingerpaint parent, ApplicationState appState) {
 		super(FingerpaintConstants.INSTANCE.btnMixNow());
 		this.fp = parent;
 		this.as = appState;
 		addClickHandler(this);
 		setEnabled(false);
 		ensureDebugId("mixNowButton");
 	}
 
 	/**
 	 * Executes the mixing run with the currently defined protocol.
 	 * @param event The event that has fired.
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
		fp.executeMixingRun(as.getProtocol(), as.getNrSteps());
 	}
 
 }
