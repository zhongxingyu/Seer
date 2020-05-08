 package ca.drusk.flightmanager.client.ui.main_pages.data_display;
 
 import ca.drusk.flightmanager.client.data.Relation;
 import ca.drusk.flightmanager.client.ui.custom_widgets.ResultsTable;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Provides common functionality for all data result displays. Holds a
  * {@link ResultsTable} and a button to refresh its contents.
  * 
  * @author drusk
  * 
  */
 public abstract class AbstractDataDisplay implements IsWidget {
 
 	private VerticalPanel display = new VerticalPanel();
 
 	private ResultsTable results;
 
 	private Label errorLabel;
 
 	private static int SPACING = 10;
 
	protected AbstractDataDisplay() {
 		createRefreshButton();
 		retrieveResultsAndAddToDisplay();
 		display.setSpacing(SPACING);
 	}
 
 	private void createRefreshButton() {
 		Button refreshButton = new Button("Refresh");
 		refreshButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				removeFromDisplayIfNotNull(errorLabel);
 				removeFromDisplayIfNotNull(results);
 
 				retrieveResultsAndAddToDisplay();
 			}
 
 		});
 
 		display.add(refreshButton);
 	}
 
 	protected abstract void retrieveResultsAndAddToDisplay();
 
 	private void removeFromDisplayIfNotNull(IsWidget isWidget) {
 		if (isWidget != null) {
 			display.remove(isWidget);
 		}
 	}
 
 	protected void generateErrorLabel(Throwable caught) {
 		errorLabel = new Label(
 				"An error occured while communicating with the server: "
 						+ caught.getMessage());
 		display.add(errorLabel);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return display;
 	}
 
 	protected void generateResultsTable(Relation result) {
 		results = new ResultsTable(result);
 		display.add(results);
 	}
 
 	protected final class TableGeneratingCallback implements
 			AsyncCallback<Relation> {
 		@Override
 		public void onFailure(Throwable caught) {
 			generateErrorLabel(caught);
 		}
 
 		@Override
 		public void onSuccess(Relation result) {
 			generateResultsTable(result);
 		}
 	}
 }
