 package nl.tue.fingerpaint.client.gui.buttons;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 
 /**
  * Button that can be used to view the performance of the previously executed
  * mixing run in a single graph.
  * 
  * @author Group Fingerpaint
  */
 public class ViewSingleGraphButton extends Button implements ClickHandler {
 	/**
 	 * Reference to the entrypoint. Used to call the functionality of the graph
 	 * visualisator.
 	 */
 	protected Fingerpaint fp;
 
 	/**
 	 * Reference to the model. Used to retrieve the segregation of the
 	 * previously executed mixing run.
 	 */
 	protected ApplicationState as;
 
 	/**
 	 * Construct a new button that can be used to view the performance of the
 	 * previously executed mixing run in a single graph.
 	 * 
 	 * @param parent
 	 *            Reference to the entrypoint, used to call the functionality of
 	 *            the graph visualisator.
 	 * @param appState
 	 *            Reference to the model, used to retrieve the segregation of
 	 *            the previously executed mixing run.
 	 */
 	public ViewSingleGraphButton(Fingerpaint parent, ApplicationState appState) {
 		super(FingerpaintConstants.INSTANCE.btnViewSingleGraph());
 		this.fp = parent;
 		this.as = appState;
 		addClickHandler(this);
 		setEnabled(false);
 		ensureDebugId("viewSingleGraphButton");
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		ArrayList<double[]> performance = new ArrayList<double[]>();
 		performance.add(as.getSegregation());
 
 		// Clear old data
 		if (fp.getGraphVisualisator() != null) {
 			fp.getGraphVisualisator().clearSegregationResults();
 		}
 		GuiState.viewSingleGraphPopupPanel.clear();
 		GuiState.viewSingleGraphVerticalPanel.clear();
 		GuiState.viewSingleGraphGraphPanel.clear();
 
 		// Make graph and add it to viewSingleGraphVerticalPanel
 		fp.createGraph(GuiState.viewSingleGraphGraphPanel,
 				new ArrayList<String>(Arrays.asList("Current mixing run")),
 				performance, new AsyncCallback<Boolean>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						caught.printStackTrace();
 					}
 
 					@Override
 					public void onSuccess(Boolean result) {
 						GuiState.viewSingleGraphVerticalPanel
 								.add(GuiState.viewSingleGraphGraphPanel);
 						GuiState.viewSingleGraphVerticalPanel
 								.add(GuiState.viewSingleGraphHorizontalPanel);
 						GuiState.viewSingleGraphPopupPanel
 								.add(GuiState.viewSingleGraphVerticalPanel);
 						GuiState.viewSingleGraphPopupPanel.center();
 					}
 				});
 	}
 
 }
