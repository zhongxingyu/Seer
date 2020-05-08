 package org.geoserver.monitoring.ui;
 
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.geoserver.monitoring.monitors.RequestMonitor;
 import org.geoserver.monitoring.monitors.RequestStats;
 import org.geoserver.web.GeoServerApplication;
 import org.geoserver.web.GeoServerBasePage;
 import org.geoserver.web.wicket.GeoServerDataProvider;
 import org.geoserver.web.wicket.GeoServerTablePanel;
 import org.geoserver.web.wicket.GeoServerDataProvider.Property;
 
 public class MonitoringPage extends GeoServerBasePage {
 
     private RequestStatsPanel table;
 
     public MonitoringPage() {
         Form form = new Form("theform");
         add(form);
 
         GeoServerDataProvider<RequestStats> provider = new RequestStatsProvider();
 
         table = new RequestStatsPanel("table", provider) {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void onSelectionUpdate(AjaxRequestTarget target) {
                 // removal.setEnabled(table.getSelection().size() > 0);
                 // target.addComponent(removal);
             }
         };
         table.setOutputMarkupId(true);
         form.add(table);
 
         setHeaderPanel(headerPanel());
     }
 
     protected Component headerPanel() {
         Fragment header = new Fragment(HEADER_PANEL, "header", this);
 
         AjaxLink link = new AjaxLink("refresh") {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 target.addComponent(table);
             }
         };
 
         header.add(link);
         return header;
     }
 
     private static class RequestStatsProvider extends GeoServerDataProvider<RequestStats> {
 
         private static final long serialVersionUID = 1L;
         
         static final Property<RequestStats> HOST = new BeanProperty<RequestStats>("Host",
         "host");
         
         static final Property<RequestStats> REMOTEADDR = new BeanProperty<RequestStats>("Client",
         "remoteAddr");
 
         static final Property<RequestStats> STATUS = new BeanProperty<RequestStats>("Status",
                 "status");
 
         static final Property<RequestStats> SERVICE = new BeanProperty<RequestStats>("Service",
                 "serviceName");
 
         static final Property<RequestStats> VERSION = new BeanProperty<RequestStats>("Version",
                 "serviceVersion");
 
         static final Property<RequestStats> OPERATION = new BeanProperty<RequestStats>("Operation",
                 "operationName");
 
         static final Property<RequestStats> LAYERS = new AbstractProperty<RequestStats>("Layers",
                 true) {
 
             private static final long serialVersionUID = 1L;
 
             public Object getPropertyValue(final RequestStats item) {
                 List<String> layerNames = item.getLayerNames();
                 if (layerNames.size() == 1) {
                     return layerNames.get(0);
                 }
                 StringBuilder sb = new StringBuilder();
                 for (Iterator<String> it = layerNames.iterator(); it.hasNext();) {
                     sb.append(it.next());
                     if (it.hasNext()) {
                         sb.append(',');
                     }
                 }
                 return sb.toString();
             }
         };
 
         static final Property<RequestStats> START_TIME = new BeanProperty<RequestStats>(
                 "Start Time", "startTime");
 
         static final Property<RequestStats> TOTAL_TIME = new BeanProperty<RequestStats>(
                 "Total Time", "totalTime");
 
         static final Property<RequestStats> RESPONSE_LENGTH = new BeanProperty<RequestStats>(
                 "Response Length", "responseLength");
 
         static final Property<RequestStats> CONTENT_TYPE = new BeanProperty<RequestStats>(
                 "Content Type", "responseContentType");
         
         @SuppressWarnings("unchecked")
         private static final List<Property<RequestStats>> PROPERTIES = Collections
                 .unmodifiableList(Arrays.asList(HOST, REMOTEADDR, STATUS, SERVICE, VERSION, OPERATION, LAYERS,
                         START_TIME, TOTAL_TIME, RESPONSE_LENGTH, CONTENT_TYPE));
 
         @Override
         protected List<RequestStats> getItems() {
             throw new UnsupportedOperationException("We don't want to load all of the data from the DB, use iterator(start, to)");
         }
         
         @Override
         public Iterator<RequestStats> iterator(int first, int count) {
             RequestMonitor monitor = (RequestMonitor) GeoServerApplication.get().getBean("requestMonitor");
             return monitor.getPagedRequests(new Date(System.currentTimeMillis() - 3600 * 1000 * 24), new Date(), first, count).iterator();
         }
         
         @Override
         public int size() {
             RequestMonitor monitor = (RequestMonitor) GeoServerApplication.get().getBean("requestMonitor");
             return (int) monitor.getRequetsCount(new Date(System.currentTimeMillis() - 3600 * 1000 * 24), new Date());
         }
         
         @Override
         public int fullSize() {
             return size();
         }
         
 
         @Override
         protected List<GeoServerDataProvider.Property<RequestStats>> getProperties() {
             return PROPERTIES;
         }
 
        @Override
        protected IModel newModel(Object object) {
            return new Model((RequestStats) object);
         }
     }
 
     private static class RequestStatsPanel extends GeoServerTablePanel<RequestStats> {
 
         private static final int KB = 1024;
 
         private static final int MB = KB * KB;
 
         private static final int GB = MB * 1024;
 
         private static final long serialVersionUID = 1L;
 
         private static DateFormat DATE_FORMAT = DateFormat.getTimeInstance();
 
         public RequestStatsPanel(final String id, GeoServerDataProvider<RequestStats> dataProvider) {
             super(id, dataProvider, true);
         }
 
         @Override
         protected Component getComponentForProperty(final String id, final IModel itemModel,
                 final Property<RequestStats> property) {
 
             final Object propertyValue = property.getPropertyValue((RequestStats) itemModel
                     .getObject());
 
 
             if (property == RequestStatsProvider.START_TIME) {
                 String startTime = DATE_FORMAT.format(((Date) propertyValue));
                 return new Label(id, startTime);
             } else if (property == RequestStatsProvider.TOTAL_TIME) {
                 return new Label(id, String.valueOf(propertyValue) + "ms");
             } else if (property == RequestStatsProvider.RESPONSE_LENGTH) {
                 String length = toLengthString(((Long) propertyValue).longValue());
                 return new Label(id, length);
             }
             // use default label
             return null;
         }
 
         private static final NumberFormat LENGTH_FORMAT = NumberFormat.getNumberInstance();
         static {
             LENGTH_FORMAT.setMinimumFractionDigits(0);
             LENGTH_FORMAT.setMaximumFractionDigits(2);
         }
 
         private String toLengthString(final long length) {
             double len = length;
             String postfix = " B";
             if (len > GB) {
                 len /= GB;
                 postfix = " G";
             } else if (len > MB) {
                 len /= MB;
                 postfix = " M";
             } else if (len > KB) {
                 len /= 1024;
                 postfix = " K";
             }
             return LENGTH_FORMAT.format(len) + postfix;
         }
     }
 }
