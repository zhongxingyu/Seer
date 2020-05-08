 package org.geoserver.web.data.table;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.web.GeoServerApplication;
 import org.springframework.beans.support.PropertyComparator;
 
 public class LayerProvider extends SortableDataProvider {
     
     public static final String TYPE = "type";
     public static final String TYPE_PROPERTY = "type";
     public static final String WORKSPACE = "workspace";
     public static final String WORKSPACE_PROPERTY = "resource.store.workspace.name";
     public static final String STORE = "store";
     public static final String STORE_PROPERTY = "resource.store.name";
     public static final String NAME = "name";
     public static final String NAME_PROPERTY = "name";
     public static final String ENABLED = "enabled";
     public static final String ENABLED_PROPERTY = "enabled";
     public static final String SRS = "SRS";
     public static final String SRS_PROPERTY = "resource.srs";
 
 
     public Iterator iterator(int first, int count) {
         // grab list
         Catalog catalog = GeoServerApplication.get().getCatalog();
         List<LayerInfo> layers = catalog.getLayers();
         
         // global sorting
         Comparator comparator = getComparator();
         if(comparator != null)
             Collections.sort(layers, comparator);
         
         // paging
         int last = first + count;
         if(last > layers.size()) 
             last = layers.size();
         return layers.subList(first, last).iterator();
     }
 
     Comparator getComparator() {
         SortParam sort = getSort();
         if(sort == null || sort.getProperty() == null)
             return null;
         
         if(TYPE.equals(sort.getProperty())) {
             return new PropertyComparator(TYPE_PROPERTY, true, sort.isAscending());
         } else if(STORE.equals(sort.getProperty())) {
             return new PropertyComparator(STORE_PROPERTY, true, sort.isAscending());
         } else if(WORKSPACE.equals(sort.getProperty())) {
             return new PropertyComparator(WORKSPACE_PROPERTY, true, sort.isAscending());
         } else if(NAME.equals(sort.getProperty())) {
             return new PropertyComparator(NAME_PROPERTY, true, sort.isAscending());
         } else if(ENABLED.equals(sort.getProperty())) {
             return new PropertyComparator(ENABLED_PROPERTY, true, sort.isAscending());
         } else if(SRS.equals(sort.getProperty())) {
             // for this I had to roll a special comparator, the SRS name does not
             // fully follows the bean conventions it seems
             return new SRSComparator(sort.isAscending());
         } else {
             return null;
         }
     }
 
     public IModel model(Object object) {
         return new LayerInfoDetachableModel((LayerInfo) object);
     }
 
     public int size() {
         return getCatalog().getLayers().size();
     }
 
     private Catalog getCatalog() {
         return GeoServerApplication.get().getCatalog();
     }
     
     static class LayerInfoDetachableModel extends LoadableDetachableModel {
         String name;
         boolean selected;
         
         public LayerInfoDetachableModel(LayerInfo layer) {
             super(layer);
             this.name = layer.getName();
         }
         
         
         @Override
         protected Object load() {
             return GeoServerApplication.get().getCatalog().getLayerByName(name);
         }
     }
     
 
     static class SRSComparator implements Comparator<LayerInfo> {
         
         boolean ascending;
         
         public SRSComparator(boolean ascending) {
             this.ascending = ascending;
         }
 
         public int compare(LayerInfo o1, LayerInfo o2) {
            // split out authority and code
            String[] srs1 = o1.getResource().getSRS().split(":");
            String[] srs2 = o2.getResource().getSRS().split(":");
            
            // use sign to control sort order
            int sign = ascending ? 1 : -1;
            if(srs1[0].equalsIgnoreCase(srs2[0]) && srs1.length > 1 && srs2.length > 1) {
                // in case of same authority, compare numbers
                return new Integer(srs1[1]).compareTo(new Integer(srs2[1])) * sign;
            } else {
                // compare authorities
                return srs1[0].compareToIgnoreCase(srs2[0]) * sign;
            }
         }
         
     }
 
 }
