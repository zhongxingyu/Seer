 package org.iplantc.core.tito.client.windows;
 
 import java.util.ArrayList;
 
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.events.NewToolRequestSubmitEvent;
 import org.iplantc.core.tito.client.events.NewToolRequestSubmitEventHandler;
 import org.iplantc.core.tito.client.panels.ToolRequestFormPanel;
 import org.iplantc.core.uicommons.client.events.EventBus;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.event.shared.HandlerRegistration;
 
 public class NewToolRequestWindow extends Window {
 
     private final ToolRequestFormPanel requestForm;
     private ArrayList<HandlerRegistration> handlers;
 
     public NewToolRequestWindow() {
         requestForm = new ToolRequestFormPanel();
         requestForm.getSubmitButton()
                 .addSelectionListener(new NewToolSelectionListenerImpl(requestForm));
 
         requestForm.getCancelButton().addSelectionListener(new NewToolSelectionCancelListenerImpl(this));
         this.setHeading(I18N.DISPLAY.requestNewTool());
         this.setLayout(new FitLayout());
        this.setSize(500, 500);
         this.setResizable(false);
         this.add(requestForm);
 
         addHandlers();
     }
 
     private void addHandlers() {
         EventBus bus = EventBus.getInstance();
         handlers = new ArrayList<HandlerRegistration>();
         handlers.add(bus.addHandler(NewToolRequestSubmitEvent.TYPE,
                 new NewToolRequestSubmitEventHandlerImpl(this)));
     }
 
     private class NewToolSelectionListenerImpl extends SelectionListener<ButtonEvent> {
         private final ToolRequestFormPanel requestForm;
 
         public NewToolSelectionListenerImpl(final ToolRequestFormPanel requestForm) {
             this.requestForm = requestForm;
         }
 
         @Override
         public void componentSelected(ButtonEvent ce) {
             if (requestForm.validate()) {
                 requestForm.submit();
             }
         }
     }
 
     private class NewToolSelectionCancelListenerImpl extends SelectionListener<ButtonEvent> {
         private final Window window;
 
         public NewToolSelectionCancelListenerImpl(final Window window) {
             this.window = window;
         }
 
         @Override
         public void componentSelected(ButtonEvent ce) {
             if (window != null) {
                 window.hide();
             }
         }
     }
 
     private class NewToolRequestSubmitEventHandlerImpl implements NewToolRequestSubmitEventHandler {
         private final Window window;
 
         public NewToolRequestSubmitEventHandlerImpl(NewToolRequestWindow window) {
             this.window = window;
         }
 
         @Override
         public void onRequestComplete(NewToolRequestSubmitEvent event) {
             if (window != null) {
                 window.hide();
             }
         }
     }
 
 }
