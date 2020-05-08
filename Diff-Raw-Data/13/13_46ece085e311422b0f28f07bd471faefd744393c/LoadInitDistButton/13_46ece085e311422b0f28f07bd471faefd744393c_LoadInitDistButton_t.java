 package nl.tue.fingerpaint.client.gui.buttons;
 
 import java.util.List;
 
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 import nl.tue.fingerpaint.shared.GeometryNames;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 /**
  * Button that can be used to load a previously saved initial concentration
  * distribution.
  * 
  * @author Group Fingerpaint
  */
 public class LoadInitDistButton extends Button implements ClickHandler {
 
 	/**
 	 * Reference to the model. Used to set the loaded concentration
 	 * distribution.
 	 */
 	protected ApplicationState as;
 
 	/**
 	 * Construct a new button that can be used to load a previously saved
 	 * initial concentration distribution.
 	 * 
 	 * @param appState
 	 *            Reference to the model, used to set the loaded initial
 	 *            concentration distribution.
 	 */
 	public LoadInitDistButton(ApplicationState appState) {
 		super(FingerpaintConstants.INSTANCE.btnLoadDist());
 		this.as = appState;
 		addClickHandler(this);
 		ensureDebugId("loadInitDistButton");
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		// Get all initial distributions for current geometry
 		List<String> geometryDistributions = StorageManager.INSTANCE
 				.getDistributions(GeometryNames.getShortName(as
 						.getGeometryChoice()));
 
 		// Create a cell to render each value.
 		TextCell textCell = new TextCell();
 
 		// Create a CellList that uses the cell.
 		CellList<String> cellList = new CellList<String>(textCell);
 		cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
 
 		// Add a selection model to handle user selection.
 		final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
 		cellList.setSelectionModel(selectionModel);
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					public void onSelectionChange(SelectionChangeEvent event) {
 						String selected = selectionModel.getSelectedObject();
 
 						// get the selected initial distribution, and
 						// set it in the AS
 						double[] dist = StorageManager.INSTANCE
 								.getDistribution(GeometryNames.getShortName(as
 										.getGeometryChoice()), selected);
 						as.setInitialDistribution(dist);
 						as.drawDistribution();
 						GuiState.loadPanel.removeFromParent();
 					}
 				});
 
 		// Set the total row count. This isn't strictly necessary, but
 		// it affects
 		// paging calculations, so its good habit to keep the row count
 		// up to date.
 		cellList.setRowCount(geometryDistributions.size(), true);
 
 		// Push the data into the widget.
 		cellList.setRowData(0, geometryDistributions);
 
 		GuiState.loadVerticalPanel.add(cellList);
 		GuiState.loadVerticalPanel.add(GuiState.closeLoadButton);
 		GuiState.loadPanel.center();
 	}
 
 }
