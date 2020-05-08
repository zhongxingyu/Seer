 package com.codingstory.polaris.web.client;
 
 import com.codingstory.polaris.web.client.stub.CodeSearchStub;
 import com.google.common.base.Stopwatch;
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.logging.Logger;
 
import static com.codingstory.polaris.web.client.stub.CodeSearchStub.*;

 public class SearchResultPage extends Composite {
     interface MyUiBinder extends UiBinder<HTMLPanel, SearchResultPage> {
     }
     private static final MyUiBinder UI_BINDER = GWT.create(MyUiBinder.class);
     private static final Logger LOGGER = Logger.getLogger(SearchResultPage.class.getName());
 
     @UiField
     SearchBox searchBox;
     @UiField
     Button searchButton;
     @UiField
     FlowPanel searchResultListPanel;
     @UiField
     Label latencyLabel;
 
     public SearchResultPage(String query) {
         initWidget(UI_BINDER.createAndBindUi(this));
         Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
             @Override
             public void execute() {
                 searchBox.setFocus(true);
             }
         });
         enableSearchPanel(false);
         executeSearch(query);
     }
 
     @UiHandler("searchButton")
     void onSearchButton(ClickEvent event) {
         String query = searchBox.getText();
         PageController.switchToSearchResult(query);
     }
 
     @UiHandler("searchBox")
     void onSearchBoxKeyDown(KeyDownEvent event) {
         if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
             // Defer firing the event to make the query completion happens first.
             Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                 @Override
                 public void execute() {
                     NativeHelper.click(searchButton.getElement());
                 }
             });
         }
     }
 
     private void executeSearch(String query) {
         final Stopwatch stopwatch = new Stopwatch().start();
         searchBox.setText(query);
         SearchRequest req = new SearchRequest();
         req.setQuery(query);
         CodeSearchStub.search(req, new Callback<SearchResponse, Throwable>() {
             @Override
             public void onFailure(Throwable e) {
                 PageController.switchToErrorPage(e);
             }
 
             @Override
             public void onSuccess(SearchResponse resp) {
                 enableSearchPanel(true);
                 if (resp.getStatus() != StatusCode.OK) {
                     PageController.switchToErrorPage("Got " + resp.getStatus() + " during search");
                     return;
                 }
                 searchResultListPanel.clear();
                 for (SearchResponse.Entry result : resp.getEntries()) {
                     String href = "p=source&file=" + result.getFileId();
                     SearchResultEntryWidget entryWidget = new SearchResultEntryWidget(result, href);
                     searchResultListPanel.add(entryWidget);
                 }
                 long serverLatency = resp.getLatency();
                 long rpcLatency = stopwatch.elapsedMillis() - serverLatency;
                latencyLabel.setText("Server latency: " + serverLatency + " ms; Client latency: " + rpcLatency + " ms");
             }
         });
     }
 
     private void enableSearchPanel(boolean enabled) {
         searchBox.setEnabled(enabled);
         searchButton.setEnabled(enabled);
     }
 }
