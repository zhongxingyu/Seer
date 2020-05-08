 package com.github.steppenwells.client;
 
 import com.github.steppenwells.client.model.DismaxField;
 import com.github.steppenwells.client.ui.DismaxFieldControl;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class SolrDismaxCalibrationTool implements EntryPoint {
 
     /**
      * Create a remote service proxy to talk to the server-side Greeting service.
      */
     private final SolrServiceAsync solrService = GWT.create(SolrService.class);
 
     private TextBox solrServerUrlEntryField;
     private final List<DismaxFieldControl> fieldControls = new ArrayList<DismaxFieldControl>();
 
     private VerticalPanel fieldsPanel;
    private VerticalPanel graphPanel;
     private VerticalPanel resultsPanel;
 
     private final DismaxToolState dismaxToolState;
     private Label queryDisplayLabel;
 
     public SolrDismaxCalibrationTool() {
         dismaxToolState = new DismaxToolState(this, solrService);
     }
 
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
 
         final VerticalPanel toolLayoutPanel = new VerticalPanel();
 
         HorizontalPanel solrUrlPanel = createSolrUrlPanel();
         toolLayoutPanel.add(solrUrlPanel);
 
         fieldsPanel = new VerticalPanel();
        graphPanel = initQueryReportingPanel();
         resultsPanel = new VerticalPanel();
 
         HorizontalPanel calibrationControls = new HorizontalPanel();
         calibrationControls.add(fieldsPanel);
        calibrationControls.add(graphPanel);
         toolLayoutPanel.add(calibrationControls);
 
         toolLayoutPanel.add(resultsPanel);
 
         RootPanel.get("fieldsContainer").add(toolLayoutPanel);
 
 //    final Button sendButton = new Button("Send");
 //    final TextBox nameField = new TextBox();
 //    nameField.setText("GWT User");
 //
 //    // We can add style names to widgets
 //    sendButton.addStyleName("sendButton");
 //
 //    // Add the nameField and sendButton to the RootPanel
 //    // Use RootPanel.get() to get the entire body element
 //    RootPanel.get("nameFieldContainer").add(nameField);
 //    RootPanel.get("sendButtonContainer").add(sendButton);
 //
 //    // Focus the cursor on the name field when the app loads
 //    nameField.setFocus(true);
 //    nameField.selectAll();
 //
 //    // Create the popup dialog box
 //    final DialogBox dialogBox = new DialogBox();
 //    dialogBox.setText("Remote Procedure Call");
 //    dialogBox.setAnimationEnabled(true);
 //    final Button closeButton = new Button("Close");
 //    // We can set the id of a widget by accessing its Element
 //    closeButton.getElement().setId("closeButton");
 //    final Label textToServerLabel = new Label();
 //    final HTML serverResponseLabel = new HTML();
 //    VerticalPanel dialogVPanel = new VerticalPanel();
 //    dialogVPanel.addStyleName("dialogVPanel");
 //    dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
 //    dialogVPanel.add(textToServerLabel);
 //    dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
 //    dialogVPanel.add(serverResponseLabel);
 //    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
 //    dialogVPanel.add(closeButton);
 //    dialogBox.setWidget(dialogVPanel);
 //
 //    // Add a handler to close the DialogBox
 //    closeButton.addClickHandler(new ClickHandler() {
 //      public void onClick(ClickEvent event) {
 //        dialogBox.hide();
 //        sendButton.setEnabled(true);
 //        sendButton.setFocus(true);
 //      }
 //    });
 //
 //    // Create a handler for the sendButton and nameField
 //    class MyHandler implements ClickHandler, KeyUpHandler {
 //      /**
 //       * Fired when the user clicks on the sendButton.
 //       */
 //      public void onClick(ClickEvent event) {
 //        sendNameToServer();
 //      }
 //
 //      /**
 //       * Fired when the user types in the nameField.
 //       */
 //      public void onKeyUp(KeyUpEvent event) {
 //        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 //          sendNameToServer();
 //        }
 //      }
 //
 //      /**
 //       * Send the name from the nameField to the server and wait for a response.
 //       */
 //      private void sendNameToServer() {
 //        sendButton.setEnabled(false);
 //        String textToServer = nameField.getText();
 //        textToServerLabel.setText(textToServer);
 //        serverResponseLabel.setText("");
 //        greetingService.greetServer(textToServer, new AsyncCallback<String>() {
 //          public void onFailure(Throwable caught) {
 //            // Show the RPC error message to the user
 //            dialogBox.setText("Remote Procedure Call - Failure");
 //            serverResponseLabel.addStyleName("serverResponseLabelError");
 //            serverResponseLabel.setHTML(SERVER_ERROR);
 //            dialogBox.center();
 //            closeButton.setFocus(true);
 //          }
 //
 //          public void onSuccess(String result) {
 //            dialogBox.setText("Remote Procedure Call");
 //            serverResponseLabel.removeStyleName("serverResponseLabelError");
 //            serverResponseLabel.setHTML(result);
 //            dialogBox.center();
 //            closeButton.setFocus(true);
 //          }
 //        });
 //      }
 //    }
 //
 //    // Add a handler to send the name to the server
 //    MyHandler handler = new MyHandler();
 //    sendButton.addClickHandler(handler);
 //    nameField.addKeyUpHandler(handler);
     }
 
     private VerticalPanel initQueryReportingPanel() {
         VerticalPanel panel = new VerticalPanel();
 
         this.queryDisplayLabel = new Label();
         panel.add(queryDisplayLabel);
 
         return panel;
     }
 
     private void showFields(String solrUrl) {
         dismaxToolState.getFieldsFor(solrUrl);
 
         for (DismaxFieldControl fieldControl : fieldControls) {
             fieldsPanel.remove(fieldControl);
         }
         fieldControls.clear();
 
         for (DismaxField dismaxField : dismaxToolState.getFields()) {
             DismaxFieldControl dismaxFieldControl = new DismaxFieldControl(dismaxField, dismaxToolState);
             fieldControls.add(dismaxFieldControl);
             fieldsPanel.add(dismaxFieldControl);
         }
 
     }
 
     private HorizontalPanel createSolrUrlPanel() {
         HorizontalPanel solrUrlPanel = new HorizontalPanel();
         solrUrlPanel.add(new Label("Root url of the solr server"));
         solrServerUrlEntryField = new TextBox();
         solrServerUrlEntryField.setTitle("Root url of the solr server");
         solrUrlPanel.add(solrServerUrlEntryField);
 
         Button connectButton = new Button("Connect");
 
         connectButton.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent clickEvent) {
                 showFields(solrServerUrlEntryField.getValue());
             }
         });
 
         solrUrlPanel.add(connectButton);
         return solrUrlPanel;
     }
 
     public List<DismaxFieldControl> getFieldControls() {
         return fieldControls;
     }
 
     public void displayQueryString() {
         queryDisplayLabel.setText(dismaxToolState.getDismaxQueryString());
     }
 }
