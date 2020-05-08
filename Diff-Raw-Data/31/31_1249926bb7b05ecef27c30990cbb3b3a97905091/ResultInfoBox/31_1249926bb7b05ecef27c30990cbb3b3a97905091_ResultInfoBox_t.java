 package at.fakeroot.sepm.client;
 
 import at.fakeroot.sepm.shared.client.serialize.SearchResult;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 /**
  * The InfoBox informs the User about the number of results for the recent search
  * @author RK
  */
 public class ResultInfoBox extends Composite
 {
 	Label title = new Label();
 	Label errText = new Label();
 	Label numText = new Label();
 	Label zoomText = new Label("Zoom in for further results.");
 	String errDetail = null;
 	InlineLabel durationText;
 	TagCloud tagCloud;
 
 
 	private class LoadingTimer extends Timer {
 		@Override
 		public void run() {
 			String newText = numText.getText();
 			newText += ".";
 			if (newText.equals("Loading....")) newText = "Loading";
 			numText.setText(newText);
 		}
 	}
 	
 	Timer loadingTimer = new LoadingTimer();
 
 	/**
 	 * Constructor. Sets the Layout of the Box
 	 */
 	public ResultInfoBox(IGeoManager gm)
 	{
 		tagCloud = new TagCloud(gm);
 		FlowPanel fPanel = new FlowPanel();
 		fPanel.getElement().setId("resultInfoBox");
 		
 		durationText = new InlineLabel();
 		durationText.setStyleName("floatRight");
 		
 		title.setText("Result info");
 		title.setStyleName("title");
 		
 		errText.setStyleName("error");
 		errText.setVisible(false);
 		errText.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				if (errDetail != null) {
 					final DialogBox errBox = new DialogBox(false, true);
 					VerticalPanel vPanel = new VerticalPanel();
 					ScrollPanel sPanel = new ScrollPanel();
 					
 					// set the ScrollPanel's size
 					sPanel.add(vPanel);
 
 					errBox.setWidget(sPanel);
 					errBox.setText(errText.getText());
 					
 					vPanel.add(new HTML(errDetail));
 					
 					Button ok = new Button("Ok");
 					ok.addClickHandler(new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							errBox.hide();
 						}
 					});
 					vPanel.add(ok);
 
					int h = (RootPanel.get().getOffsetHeight()*80)/100;
					int w = (RootPanel.get().getOffsetWidth()*90)/100;

 					errBox.center();

					h = Math.min(sPanel.getOffsetHeight(), h);
					w = Math.min(sPanel.getOffsetWidth(), w);
					sPanel.setSize(w+"px", h+"px");
 				}
 			}
 		});
 		
 		zoomText.setVisible(false);
 		tagCloud.setVisible(false);
 		
 		fPanel.add(durationText);
 		fPanel.add(title);
 		fPanel.add(errText);
 		fPanel.add(numText);
 		fPanel.add(zoomText);
 		fPanel.add(tagCloud);
 		initWidget(fPanel);
 	}
 	
 	/**
 	 * sets the text, which is displayed in the InfoBox, depending on the number of 
 	 * results of the recent search
 	 * @param shown int the number of results, which are shown on the map
 	 * @param available int the number of results, which are found in the area
 	 * @param countLimit int the max limit for the Box; if there are more results in the DB (int available)
 	 * @param duration time in milliseconds the search took
 	 * then the countLimit there will be displayed 'int shown results out of more then int countLimit'  
 	 */
 	public void refresh(SearchResult result)
 	{
 		int results = result.getResults().size();
 		boolean hasMore = result.hasMore();
 		
 		hideError();
 		// stop the timer
 		stopLoading();
 		
 		if (result.hasError()) {
 			showError(result.getErrorMessage(), result.getErrorDetail());
 		}
 		else {
 			//No results
 			if(results == 0) numText.setText("No results found.");
 			//1 result
 			else {
 				if (results == 1) numText.setText("1 result");
 				else if (hasMore) {
 					numText.setText("more than " + results + " results");
 					zoomText.setVisible(true);
 				}
 				else {
 					numText.setText(results + " results");
 				}
 				tagCloud.refresh(result.getResults().iterator());
 				tagCloud.setVisible(true);
 			}
 		}
 
 		// print the duration
 		durationText.setText((result.getDuration()/1000.)+" sec");
 	}
 	
 	public void startLoading() {
 		hideError();
 		zoomText.setVisible(false);
 		durationText.setVisible(false);
 		tagCloud.setVisible(false);
 		numText.setText("Loading");
 		loadingTimer.scheduleRepeating(500);
 	}
 	
 	public void stopLoading() {
 		loadingTimer.cancel();
 		numText.setText("");
 		durationText.setText("");
 		durationText.setVisible(true);
 	}
 	
 	public void showError(String msg, String detail) {
 		zoomText.setVisible(false);
 		numText.setVisible(false);
 		tagCloud.setVisible(false);
 		
 		errText.setText(msg);
 		errDetail = detail;
 		if (detail != null) {
 			// make error text clickable
 			errText.getElement().getStyle().setProperty("cursor", "pointer");
 		}
 		errText.setVisible(true);
 	}
 	
 	public void showError(Throwable t) {
 		/// TODO improve me...
 		showError(t.getMessage(), "Error");
 	}
 	
 	public void hideError() {
 		errText.getElement().getStyle().setProperty("cursor", "");
 		errText.setVisible(false);
 		numText.setVisible(true);
 	}
 }
