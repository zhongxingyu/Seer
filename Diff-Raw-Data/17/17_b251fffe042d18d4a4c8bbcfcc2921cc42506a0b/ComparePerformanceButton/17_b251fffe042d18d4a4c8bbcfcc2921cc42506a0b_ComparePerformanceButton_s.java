 package nl.tue.fingerpaint.client.gui.buttons;
 
 import java.util.ArrayList;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Button that can be used to compare the performance of previously saved mixing
  * runs.
  * 
  * @author Group Fingerpaint
  */
 public class ComparePerformanceButton extends Button implements ClickHandler {
 
 	private Fingerpaint fp;
 
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
 		addClickHandler(this);
 		ensureDebugId("comparePerformanceButton");
 
 		GuiState.compareButton = new CompareButton(fp);
 
 		initialise();
 
 	}
 
 	/**
 	 * Creates a list containing the names of all locally stored results.
 	 * @param event The event that has fired.
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 		ArrayList<String> resultNames = (ArrayList<String>) StorageManager.INSTANCE
 				.getResults();
 
 		GuiState.compareSelectPopupCellList.setRowCount(resultNames.size());
 
 		// Push the data into the widget.
 		GuiState.compareSelectPopupCellList.setRowData(0, resultNames);
 
		// Alternate between white and light-gray background colors
		for (int i = 0; i < resultNames.size(); i += 2) {
			GuiState.compareSelectPopupCellList.getRowElement(i).addClassName("cellListStyleGray");
		}
		for (int i = 1; i < resultNames.size(); i += 2) {
			GuiState.compareSelectPopupCellList.getRowElement(i).addClassName("cellListStyleWhite");
		}
		
 		GuiState.compareSelectPopupPanel
 				.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
 					public void setPosition(int offsetWidth, int offsetHeight) {
 						GuiState.compareSelectPopupPanel.center();
 					}
 				});
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
 
 		// Initialise all components of the first popup panel
 		VerticalPanel compareVerticalPanel = new VerticalPanel();
 		GuiState.compareSelectPopupPanel.add(compareVerticalPanel);
 		compareVerticalPanel.add(GuiState.compareSelectPopupCellList);
 		HorizontalPanel compareHorizontalPanel = new HorizontalPanel();
 		compareVerticalPanel.add(compareHorizontalPanel);
 		compareHorizontalPanel.add(GuiState.compareButton);
 		compareHorizontalPanel.add(GuiState.cancelCompareButton);
 	}
 }
