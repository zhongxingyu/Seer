 package com.wideplay.warp.components.core;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.wideplay.warp.annotations.Component;
 import com.wideplay.warp.module.componentry.ClassReflectionCache;
 import com.wideplay.warp.module.componentry.PropertyDescriptor;
 import com.wideplay.warp.module.componentry.Renderable;
 import com.wideplay.warp.module.pages.PageClassReflection;
 import com.wideplay.warp.rendering.ComponentHandler;
 import com.wideplay.warp.rendering.HtmlWriter;
 import com.wideplay.warp.util.beans.BeanUtils;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 24/03/2007
  *
  * Generates a table from a collection of values with columns mapped to JavaBean
  * properties of the first bean's class.
  *
  *  w:property and w:title are mutually exclusive
  *
  * @author Dhanji R. Prasanna
  * @since 1.0
  */
 @Component
 public class Table implements Renderable {
     private String items;
     private final ClassReflectionCache classCache;
     private Map<String, ComponentHandler> columns;
     private Map<String, ComponentHandler> customColumns;    //non-property columns
 
     @Inject
     public Table(ClassReflectionCache classCache) {
         this.classCache = classCache;
     }
 
     public void render(HtmlWriter writer, List<? extends ComponentHandler> nestedComponents, Injector injector, PageClassReflection reflection, Object page) {
         String id = writer.newId(this);
         writer.element("table", "id", id);
 
         //obtain the bound object
         Object itemsObject = BeanUtils.getFromPropertyExpression(items, page);
 
         //build a cache of child columns (if we havent already) TODO validate these "columns" in the template at startup time
         if (null == columns) {
             this.columns = new LinkedHashMap<String, ComponentHandler>();
             this.customColumns = new LinkedHashMap<String, ComponentHandler>();
 
             //only populate the cache if there are child components
             if (null != nestedComponents)
                 for (ComponentHandler columnHandler : nestedComponents) {
                     Map<String, PropertyDescriptor> map = columnHandler.getPropertyValueExpressions();
 
                     //look for custom columns, w:property and w:title are mutually exclusive (title takes precedence)
                     if (map.containsKey(Column.TITLE)) {
                         this.customColumns.put(map.get(Column.TITLE).getValue(), columnHandler);
 
                     } else if (map.containsKey(Column.PROPERTY)) {    //use a property-based column override
                         PropertyDescriptor propertyDescriptor = map.get(Column.PROPERTY);
                         this.columns.put(propertyDescriptor.getValue(), columnHandler);
                     }
                 }
         }
 
         //see if it is an iterable
         if (itemsObject instanceof Iterable) {
             Map<String, String> propertiesAndLabels = null;
             Iterator iter = ((Iterable) itemsObject).iterator();
 
             boolean isFirst = true;
             while(iter.hasNext()) {
                 Object item = iter.next();
 
                 if (isFirst) {
                     //get the resource bundle associated with this model object (if any)
                     propertiesAndLabels = classCache.getPropertyLabelMap(item);
                     writeHeader(writer, propertiesAndLabels);
                     isFirst = false;
                     writer.element("tbody");
                 }
 
                 writeRow(item, writer, propertiesAndLabels, injector, reflection);
             }
 
             writer.end("tbody");
 
         } else {    //is an array
             Map<String, String> propertiesAndLabels = null;
             Object[] array = ((Object[]) itemsObject);
 
             for (int i = 0 ; i < array.length; i++) {
                 Object item = array[i];
 
                 if (0 == i) {
                     propertiesAndLabels = classCache.getPropertyLabelMap(item);
                     writeHeader(writer, propertiesAndLabels);
                     writer.element("tbody");
                 }
 
                 writeRow(item, writer, propertiesAndLabels, injector, reflection);
             }
             writer.end("tbody");
         }
         writer.end("table");
     }
 
     private void writeHeader(HtmlWriter writer, Map<String, String> propertiesAndLabels) {
         //write out header
         writer.element("thead");
         writer.element("tr");
         for (String label : propertiesAndLabels.values()) {
             writer.element("th");
             writer.writeRaw(label);
             writer.end("th");
         }
 
         //render custom (non-property) column headings
         for (String customColumnLabel : customColumns.keySet()) {
             //render using column component override (children)
             writer.element("th");
             writer.writeRaw(customColumnLabel);
             writer.end("th");
         }
         
         writer.end("tr");
         writer.end("thead");
     }
 
     private void writeRow(Object item, HtmlWriter writer, Map<String, String> propertiesAndLabels, Injector injector, PageClassReflection reflection) {
         writer.element("tr");
         for (String property : propertiesAndLabels.keySet()) {
             writer.element("td");
 
             //see if this particular property should be rendered by an overriding column component
             ComponentHandler child = columns.get(property);
             if (null != child) {
                 //render using column component override (children)
                 child.handleRender(writer, injector, reflection, item);
 
             } else {    //write normally
                 //stringize the property value only if it is not null (prevent NPE)
                 Object value = BeanUtils.getFromPropertyExpression(property, item);
                 if (null != value)
                     writer.writeRaw(value.toString());
                 else
                     writer.writeRaw(null);
             }
 
             writer.end("td");
         }
 
         //render non-property columns
         for (ComponentHandler customCol : customColumns.values()) {
             //render using column component override (children)
             writer.element("td");
             customCol.handleRender(writer, injector, reflection, item);
             writer.end("td");
         }
 
 
         writer.end("tr");
     }
 
     public String getItems() {
         return items;
     }
 
     public void setItems(String items) {
         this.items = items;
     }
 }
