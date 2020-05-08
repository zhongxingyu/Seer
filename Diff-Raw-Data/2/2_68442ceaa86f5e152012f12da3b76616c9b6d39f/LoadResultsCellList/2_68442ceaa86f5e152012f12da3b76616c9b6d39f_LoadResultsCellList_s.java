 package nl.tue.fingerpaint.client.gui.celllists;
 
 import java.util.List;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.gui.menu.MenuLevelSwitcher;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.storage.ResultStorage;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 import nl.tue.fingerpaint.shared.model.MixingProtocol;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 /**
  * CellList that is used to show all saved results, of which the user can select
  * one to load.
  * 
  * @author Group Fingerpaint
  */
 public class LoadResultsCellList extends CellList<String> {
 	/**
 	 * Reference to the model. Used to set the loaded results.
 	 */
 	protected ApplicationState as;
 
 	/** Fingerpaint class needed for mixing */
 	protected Fingerpaint fp;
 
 	/** The selection model of this cell list. */
 	final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
 
 	/**
 	 * Construct a new cell list that can be used to select results from all
 	 * available saved results.
 	 * 
 	 * @param fp
 	 *            Reference to the entryPoint, needed to execute the loaded
 	 *            mixing run.
 	 * 
 	 * @param appState
 	 *            Reference to the model, used to set the selected concentration
 	 *            distribution.
 	 */
 	public LoadResultsCellList(Fingerpaint fp, ApplicationState appState) {
 		super(new TextCell());
 		this.fp = fp;
 		this.as = appState;
 		setSelectionModel();
 		setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
 		setSelectionModel(selectionModel);
 		ensureDebugId("loadResultsCellList");
 	}
 
 	/**
 	 * Fills this CellList with the items in {@code results}.
 	 * 
 	 * @param results
 	 *            List of all initial concentration distributions currently
 	 *            stored in the local storage.
 	 */
 	public void fillCellList(List<String> results) {
 		setRowCount(results.size(), true);
 		setRowData(0, results);
 		// Alternate between white and light-gray background colors
 		for (int i = 0; i < results.size(); i += 2) {
 			this.getRowElement(i).addClassName("cellListStyleGray");
 		}
 		for (int i = 1; i < results.size(); i += 2) {
 			this.getRowElement(i).addClassName("cellListStyleWhite");
 		}
 	}
 
 	/**
 	 * Sets the selection model for this cell list.
 	 */
 	private void setSelectionModel() {
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					public void onSelectionChange(SelectionChangeEvent event) {
 						String selected = selectionModel.getSelectedObject();
 
 						if (selected != null) {
 
 							// get the selected results, and recompute them
 							ResultStorage result = StorageManager.INSTANCE
 									.getResult(selected);
 
 							// recompute the result and repaint
 							recompute(result);
 
 							selectionModel.setSelected(selected, false);
 							GuiState.loadPanel.removeFromParent();
 						}
 					}
 				});
 	}
 
 	/**
 	 * Recomputes the result distribution to match the stored values, data for
 	 * the calculation is taken from the parameter result
 	 * 
 	 * @param result
 	 *            the result containing the initial distribution and the
 	 *            protocol
 	 */
 	protected void recompute(ResultStorage result) {		
 		// load the protocol in the protocol box
 		MixingProtocol prot = result.getMixingProtocol();
 		as.setProtocol(prot);
 		GuiState.labelProtocolRepresentation.setText(as.getProtocol().toString());
 		GuiState.nrStepsSpinner.setValue(result.getNrSteps());
 
 		// load the distribution and the segregation
 		as.getGeometry().drawDistribution(result.getDistribution());
 		as.setSegregation(result.getSegregation());
 		
 		//enable buttons
 		GuiState.saveResultsButton.setEnabled(true);
 		GuiState.viewSingleGraphButton.setEnabled(true);
 		GuiState.mixNowButton.setEnabled(true);
 		GuiState.labelProtocolRepresentation.setVisible(true);
 		GuiState.saveProtocolButton.setEnabled(true);
 		
 		// open protocol menu by first going to the main menu and then opening
 		// the actual protocol menu: this looks nicer :-)
 		MenuLevelSwitcher.go(0, new AsyncCallback<Boolean>() {
 			
 			@Override
 			public void onSuccess(Boolean result) {
 				MenuLevelSwitcher.showSub1MenuDefineProtocol();
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				// will not be called
 			}
 		});
 	}
 }
