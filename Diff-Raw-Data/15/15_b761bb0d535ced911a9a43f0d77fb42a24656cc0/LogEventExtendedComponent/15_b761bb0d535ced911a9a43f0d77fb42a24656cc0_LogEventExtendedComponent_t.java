 package md.frolov.legume.client.ui.components;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableSortedMap;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.Splittable;
 
 import md.frolov.legume.client.elastic.model.reply.LogEvent;
 import md.frolov.legume.client.ui.controls.FieldActionsDropdown;
 import md.frolov.legume.client.util.ConversionUtils;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
 public class LogEventExtendedComponent extends Composite
 {
     interface LogEventExtendedComponentUiBinder extends UiBinder<Widget, LogEventExtendedComponent>
     {
     }
 
     interface CSS extends CssResource
     {
         String keyLabel();
        String filterLabel();
     }
 
     private static LogEventExtendedComponentUiBinder binder = GWT.create(LogEventExtendedComponentUiBinder.class);
 
     @UiField
     FlexTable container;
     @UiField
     CSS css;
 
     public LogEventExtendedComponent(LogEvent logEvent)
     {
         initWidget(binder.createAndBindUi(this));
 
         HTMLTable.ColumnFormatter columnFormatter = container.getColumnFormatter();
        columnFormatter.setWidth(0, "0");
        columnFormatter.setWidth(1, "0");
 
         fillIn(logEvent);
     }
 
     private void fillIn(final LogEvent logEvent)
     {
         Date timestamp = logEvent.getTimestamp();
         addRow("time", "@timestamp", logEvent.getTimestamp(), timestamp);
         addRow("type", "@type", logEvent.getType(), timestamp);
         addRow("message", "@message", logEvent.getMessage(), timestamp);
         addRow("source", "@source", logEvent.getSource(), timestamp);
         addRow("source host", "@source_host", logEvent.getSourceHost(), timestamp);
 
         String tags = Joiner.on(", ").join(logEvent.getTags());
         addRow("tags", "@tags", tags, timestamp);
 
         Map<String, Splittable> fields = ImmutableSortedMap.copyOf(logEvent.getFields());
         for (Iterator<Map.Entry<String, Splittable>> iterator = fields.entrySet().iterator(); iterator.hasNext(); )
         {
             Map.Entry<String, Splittable> entry = iterator.next();
 
             Splittable theValue = entry.getValue();
             String value;
             if(theValue.isIndexed()) {
                 List<String> values = Lists.newArrayList();
                 for(int i=0; i<theValue.size(); i++) {
                     values.add(theValue.get(i).asString());
                 }
                 value = Joiner.on(',').join(values);
             } else {
                 value = theValue.asString();
             }
             addRow(entry.getKey(), "@fields." + entry.getKey(), value, timestamp);
         }
     }
 
     private void addRow(String name, String fieldName, Object value, Date timestamp)
     {
         String valueStr = getValueStr(value);
         int row = container.getRowCount();
         Label fieldNameLabel = new Label(name);
         container.setWidget(row, 0, fieldNameLabel);
        container.getCellFormatter().setStyleName(row, 0, css.keyLabel());
         container.setWidget(row, 1, new FieldActionsDropdown(fieldName, valueStr, timestamp.getTime()));
        container.getCellFormatter().setStyleName(row, 1, css.filterLabel());
         container.setText(row, 2, valueStr);
     }
 
     private String getValueStr(final Object value)
     {
         if(value == null ) {
             return "";
         }
         if(value instanceof Date) {
             return ConversionUtils.INSTANCE.dateToString((Date) value);
         }
         if(value instanceof Iterable) {
             return Joiner.on(", ").join((Iterable<?>) value);
         }
         return value.toString();
     }
 }
