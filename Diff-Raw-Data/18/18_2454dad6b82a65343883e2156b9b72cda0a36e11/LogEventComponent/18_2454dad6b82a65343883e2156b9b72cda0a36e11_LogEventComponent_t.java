 package md.frolov.legume.client.ui.components;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 import md.frolov.legume.client.elastic.model.reply.LogEvent;
 import md.frolov.legume.client.events.LogMessageHoverEvent;
 import md.frolov.legume.client.gin.WidgetInjector;
 import md.frolov.legume.client.service.ColorizeService;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
 public class LogEventComponent extends Composite
 {
     private static final DateTimeFormat DTF = DateTimeFormat.getFormat("dd/MM HH:mm:ss.sss");
     private static final int MAX_SUMMARY_WIDTH = 300;
 
     interface LogEventComponentUiBinder extends UiBinder<Widget, LogEventComponent>
     {
     }
 
     interface Css extends CssResource
     {
         String detailsSelected();
     }
 
     private static LogEventComponentUiBinder binder = GWT.create(LogEventComponentUiBinder.class);
 
     private final String id;
     private final LogEvent logEvent;
 
     private WidgetInjector injector = WidgetInjector.INSTANCE;
     private ColorizeService colorizeService = injector.colorizeService();
     private EventBus eventBus = injector.eventBus();
 
     @UiField
     Label time;
     @UiField
     FlowPanel type;
     @UiField
     Label message;
     @UiField
     Panel summary;
     @UiField
     FlowPanel details;
     @UiField
     FlowPanel detailsContainer;
     @UiField
     FocusPanel focusPanel;
 
     @UiField
     Css style;
     @UiField
     FlowPanel box;
 
     public LogEventComponent(String id, LogEvent logEvent)
     {
         initWidget(binder.createAndBindUi(this));
         this.id = id;
         this.logEvent = logEvent;
 
         time.setText(DTF.format(logEvent.getTimestamp()));
         String summaryText = abbreviate(logEvent.getMessage(), MAX_SUMMARY_WIDTH);
         message.setText(summaryText);
 
         addColorClasses(logEvent);
     }
 
     private String abbreviate(String string, int maxWidth)
     {
         if (string.length() < maxWidth)
         {
             return string;
         }
         else
         {
             return string.substring(0, maxWidth) + "...";
         }
     }
 
     private void addColorClasses(LogEvent logEvent)
     {
        addColorClass("@type", logEvent.getType());
        addColorClass("@source", logEvent.getSource());
        addColorClass("@source_path", logEvent.getSourcePath());
        addColorClass("@message", logEvent.getMessage());
        addColorClass("@source_host", logEvent.getSourceHost());
 
         for (Map.Entry<String, List<String>> entry : logEvent.getFields().entrySet())
         {
             String fieldName = "@fields." + entry.getKey();
            addColorClass(fieldName, entry.getValue());
         }
     }
 
     private void addColorClass(final String fieldName, final String value)
     {
        if (!colorizeService.isFieldColorizable(fieldName))
        {
            return;
        }
         if (value == null || value.length() == 0)
         {
             return;
         }
         focusPanel.addStyleName(colorizeService.getCssClassName(fieldName, value));
     }
 
     private void addColorClass(final String fieldName, final List<String> values)
     {
         if (values == null || values.isEmpty())
         {
             return;
         }
 
         for (String value : values)
         {
             addColorClass(fieldName, value);
         }
     }
 
     @UiHandler("focusPanel")
     public void toggleSummaryAndDetails(final ClickEvent event)
     {
         if (summary.isVisible())
         {
             detailsContainer.add(new LogEventExtendedComponent(logEvent));
             summary.setVisible(false);
             details.setVisible(true);
             focusPanel.addStyleName(style.detailsSelected());
         }
         else
         {
             summary.setVisible(true);
             details.setVisible(false);
             detailsContainer.clear();
             focusPanel.removeStyleName(style.detailsSelected());
         }
         focusPanel.setFocus(false);
     }
 
     @UiHandler("focusPanel")
     public void handleMouseOver(final MouseOverEvent event)
     {
         eventBus.fireEvent(new LogMessageHoverEvent(logEvent.getTimestamp().getTime()));
     }
 }
