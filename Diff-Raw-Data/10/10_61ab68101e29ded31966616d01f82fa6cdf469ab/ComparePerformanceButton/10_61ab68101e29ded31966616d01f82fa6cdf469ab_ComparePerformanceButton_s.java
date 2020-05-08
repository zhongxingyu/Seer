 package nl.tue.fingerpaint.client.gui.buttons;
 
 import io.ashton.fastpress.client.fast.PressEvent;
 import io.ashton.fastpress.client.fast.PressHandler;
 
 import java.util.ArrayList;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.gui.labels.NoFilesFoundLabel;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.Range;
 
 /**
  * Button that can be used to compare the performance of previously saved mixing
  * runs.
  * 
  * @author Group Fingerpaint
  */
 public class ComparePerformanceButton extends FastButton implements PressHandler {
 
 	private Fingerpaint fp;
 	private VerticalPanel compareVerticalPanel = new VerticalPanel();
 	private HorizontalPanel compareHorizontalPanel = new HorizontalPanel();
 	
 	/** the label for when there are no files found to load */
 	NoFilesFoundLabel noFilesFoundLabel = new NoFilesFoundLabel();
 
 	/**
 	 * Construct a new button that can be used to compare the performance of
 	 * previously saved mixing runs. When clicked, it opens the
 	 * {@link GuiState#compareSelectPopupPanel} pop-up.
 	 * 
 	 * @param parent
 	 *            Reference to the entrypoint, used to export the graphs.
 	 */
 	public ComparePerformanceButton(Fingerpaint parent) {
 		super(FingerpaintConstants.INSTANCE.btnComparePerfomance());
 		this.fp = parent;
 		addPressHandler(this);
 		ensureDebugId("comparePerformanceButton");
 
 		GuiState.compareButton = new CompareButton(fp);
 
 		initialise();
 
 	}
 
 	/**
 	 * Creates a list containing the names of all locally stored results.
 	 * @param event The event that has fired.
 	 */
 	@Override
 	public void onPress(PressEvent event) {
 		GuiState.loadPanel.setIsLoading();
 		Timer runLater = new Timer() {
 			@Override
 			public void run() {
 				ArrayList<String> resultNames = (ArrayList<String>) StorageManager.INSTANCE
 						.getResults();
 
 				GuiState.compareSelectPopupCellList.setVisibleRangeAndClearData(new Range(0, resultNames.size()), true);
 
 				// Push the data into the widget.
 				GuiState.compareSelectPopupCellList.setRowData(0, resultNames);
 				
 				//reconstruct the selection popup
 				setupSelectionPopup(GuiState.compareSelectPopupCellList);
 
 				GuiState.loadPanel.hide();
 				GuiState.compareSelectPopupPanel.center();
 			}
 		};
 		runLater.schedule(100);
 	}
 
 	private void initialise() {
 		// Initialise all components of the second popup panel
 		VerticalPanel vertPanel = new VerticalPanel();
 		HorizontalPanel horPanel = new HorizontalPanel();
 		horPanel.add(GuiState.newCompareButton);
 		GuiState.exportMultipleGraphButton = new ExportMultipleGraphsButton(fp);
 		horPanel.add(GuiState.exportMultipleGraphButton);
 		horPanel.add(GuiState.closeCompareButton);
 		GuiState.comparePopupPanel.add(vertPanel);
 		vertPanel.add(GuiState.compareGraphPanel);
 		vertPanel.add(horPanel);
 
 		// Add the first vertical panel of the first popup panel, 
 		// the rest of the initialisation is done dynamically in setupSelectionPopup
 		GuiState.compareSelectPopupPanel.add(compareVerticalPanel);
		
 	}
 	
 	/**
 	 * Method to reconstruct the selection popup, 
 	 * adds the argument list to the popup if it is not empty
 	 * 
 	 * @param list the list to be added to the selection popup
 	 */
 	private void setupSelectionPopup(CellList<String> list){
 		compareVerticalPanel.clear();
 		compareHorizontalPanel.clear();
 		//insert no saves found message if the list is empty, add the list if it is not
		if(list.getVisibleItemCount() == 0){
 			compareVerticalPanel.add(noFilesFoundLabel);
		}else{
			compareVerticalPanel.add(list);
 		}
 
 		compareVerticalPanel.add(compareHorizontalPanel);
 		//omit the compareButton if the list is empty
 		if(list.getVisibleItemCount() != 0){
 			compareHorizontalPanel.add(GuiState.compareButton);
 		}
 		compareHorizontalPanel.add(GuiState.cancelCompareButton);
 	}
 }
