 package nl.tue.fingerpaint.client.gui.buttons;
 
 import io.ashton.fastpress.client.fast.PressEvent;
 import io.ashton.fastpress.client.fast.PressHandler;
 
 import java.util.List;
 
 import com.google.gwt.user.client.Timer;
 
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 
 /**
  * Button that can be used to load a protocol from the local storage.
  * 
  * @author Group Fingerpaint
  */
 public class LoadProtocolButton extends FastButton implements PressHandler {
 
 	/**
 	 * Reference to the model. Used to get the currently selected geometry.
 	 */
 	protected ApplicationState as;
 
 	/**
 	 * Construct a new button that can be used to load a protocol from the local
 	 * storage.
 	 * 
 	 * @param appState
 	 *            Reference to the model, used to retrieve the currently
 	 *            selected geometry.
 	 */
 	public LoadProtocolButton(ApplicationState appState) {
 		super(FingerpaintConstants.INSTANCE.btnLoadProt());
 		this.as = appState;
 		addPressHandler(this);
 		ensureDebugId("loadProtocolButton");
 	}
 
 	/**
 	 * Creates a panel with the names of all locally stored distributions.
 	 * @param event The event that has fired.
 	 */
 	@Override
 	public void onPress(PressEvent event) {
 		GuiState.loadPanel.setIsLoading();
 		Timer runLater = new Timer() {
 			@Override
 			public void run() {
 				GuiState.loadVerticalPanel.clear();
 				
 				List<String> geometryProtocols = StorageManager.INSTANCE
 						.getProtocols(as.getGeometryChoice());
 				GuiState.loadProtocolCellList.fillCellList(geometryProtocols);
 		
 				GuiState.loadVerticalPanel.addList(GuiState.loadProtocolCellList);
 				GuiState.loadVerticalPanel.add(GuiState.closeLoadButton);
				GuiState.loadPanel.center();
 			}
 		};
 		runLater.schedule(100);
 	}
 
 }
