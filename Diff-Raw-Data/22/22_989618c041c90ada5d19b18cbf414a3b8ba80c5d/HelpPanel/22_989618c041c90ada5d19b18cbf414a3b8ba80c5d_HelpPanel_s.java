 package no.knubo.accounting.client.help;
 
 import java.util.MissingResourceException;
 
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.HelpTexts;
 import no.knubo.accounting.client.misc.WidgetIds;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.EventTarget;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.Event.NativePreviewHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class HelpPanel extends Composite implements NativePreviewHandler {
 
     DisclosurePanel disclosurePanel;
     private static HelpPanel me;
 
     private Frame mainFrame;
     private HTML contextHelp;
     private final Elements messages;
     private final HelpTexts helpTexts;
     private String help;
     private String prevHelp = "";
     private HandlerRegistration nativePreviewHandler;
 
     public void setCurrentWidget(Widget widget, WidgetIds currentPage) {
         mainFrame.setUrl("help/" + messages.HELP_ROOT() + "/" + currentPage.name() + ".html");
         resize(widget);
     }
 
     public void resize(Widget widget) {
 
        int width = widget.getOffsetWidth();
         int height = widget.getOffsetHeight();
 
        if ((800 - width) < 400) {
            mainFrame.setWidth("400px");
        } else {
            mainFrame.setWidth((800 - width) + "px");
        }
 
        if (height < 400) {
            mainFrame.setHeight("400px");
         } else {
             mainFrame.setHeight(height + "px");
         }
     }
 
     public static HelpPanel getInstance(Elements messages, HelpTexts helpTexts) {
         if (me == null) {
             me = new HelpPanel(messages, helpTexts);
         }
 
         return me;
     }
 
     private HelpPanel(Elements messages, HelpTexts helpTexts) {
         this.messages = messages;
         this.helpTexts = helpTexts;
         disclosurePanel = new DisclosurePanel();
         disclosurePanel.setHeader(new Label(messages.help()));
         disclosurePanel.setWidth("100%");
         disclosurePanel.setHeight("100%");
         mainFrame = new Frame();
         contextHelp = new HTML();
         contextHelp.setStyleName("contexthelp");
 
         VerticalPanel dp = new VerticalPanel();
         dp.setWidth("100%");
         dp.add(contextHelp);
         dp.add(mainFrame);
         addEventHandler();
         disclosurePanel.add(dp);
         initWidget(disclosurePanel);
 
         /* Every second poll and see if help should be updated. */
         new Timer() {
             @Override
             public void run() {
 
                 if (help != null && prevHelp != help) {
                     contextHelp.setHTML(help);
                     prevHelp = help;
                 }
             }
 
         }.scheduleRepeating(1000);
     }
 
     /**
      * Each time this is called it makes sure that it is on top of the event
      * stack so that it receives events.
      */
     public void addEventHandler() {
         if (nativePreviewHandler != null) {
             nativePreviewHandler.removeHandler();
         }
         nativePreviewHandler = Event.addNativePreviewHandler(this);
     }
 
     public void onPreviewNativeEvent(NativePreviewEvent event) {
         EventTarget eventTarget = event.getNativeEvent().getEventTarget();
 
         if (eventTarget == null) {
             return;
         }
 
         if (!com.google.gwt.dom.client.Element.is(eventTarget)) {
             return;
         }
 
         Element elem = Element.as(eventTarget);
 
         String id = elem.getPropertyString("id");
 
         if (id == null) {
             return;
         }
 
         String helpText = null;
         try {
             helpText = helpTexts.getString(id);
         } catch (MissingResourceException e) {
             helpText = "";
         }
 
         String message = null;
         try {
             message = messages.getString(id);
         } catch (MissingResourceException e) {
             message = "";
         }
 
         help = formatHelp(message, helpText);
 
     }
 
     private String formatHelp(String header, String text) {
         return "<strong>" + header + "</strong><br>" + (text != null ? text : "---");
     }
 
 }
