 package net.crimsoncactus.radioshark.client;
 
 
import static com.google.gwt.event.dom.client.KeyCodes.KEY_DELETE;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class LineDetailsWidget extends Composite {
     Table table;
     LinesTableModel model;
//    private ChatSession chatSession;
 
     public LineDetailsWidget(ChatSession chatSession) {
//        this.chatSession = chatSession;
         model = new LinesTableModel(chatSession);
         table = new Table(model);
         
         table.addKeyPressHandler(new KeyPressHandler() {
             
             public void onKeyPress(KeyPressEvent event) {
                 switch (event.getNativeEvent().getKeyCode()) {
                 case KEY_DELETE:
                     int sel = table.getSelectedRow();
                     if (sel != -1) {
                         model.clearCall(sel);
                     }
                 }
             }
         });
 
         
         
         VerticalPanel panel = new VerticalPanel();
         panel.setWidth("400px");
         panel.add(table);
         
         FlowPanel buttonPanel = new FlowPanel();
         Button clearButton = new Button("Clear");
         clearButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 model.clear();
             }
         });
         buttonPanel.add(clearButton);
 //        buttonPanel.add(new Button("Reject"));
 //        buttonPanel.add(new Button("Hangup"));
 //        buttonPanel.add(new Button("Hold"));
         panel.add(buttonPanel);
         
         Timer timeTimer = new Timer() {
             
             @Override
             public void run() {
                 table.refreshColumn(3);
             }
         };
         timeTimer.scheduleRepeating(500);
         
         initWidget(panel);
     }
     
 }
