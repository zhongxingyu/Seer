 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.filters.experimental;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.neo4j.graphdb.Node;

 /**
  * Represents a composite filter which contains two or more filters and relations between them.
  * <p>
  * For example the following condition <i>a<=property<=b</i> can be represented as
  * <p>
  * <code>filter1 and filter2</code>,
  * <p>
  * where filter1 - property>=a, filter2 - property<=b
  * </p>
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class CompositeFilter implements IFilter {
     private List<Relation> relations = new ArrayList<Relation>(0);
     private List<IFilter> subfilters = new ArrayList<IFilter>(0);
     private String property;
 
     public CompositeFilter() {
     }
 
     public CompositeFilter(String property) {
         this.property = property;
     }
 
     /**
      * @return Returns the property.
      */
     public String getProperty() {
         return property;
     }
 
     /**
      * @param property The property to set.
      */
     public void setProperty(String property) {
         this.property = property;
     }
 
     @Override
     public boolean accept(Object value) {
        Node node = (Node)value;
         int n = subfilters.size();
         if (n >= 0) {
            IFilter filter = subfilters.get(0);
            String prop = filter.getProperty();
            boolean result = (filter instanceof CompositeFilter) ? filter.accept(node) : node.hasProperty(prop)&&filter.accept(node.getProperty(filter
                    .getProperty()));
             for (int i = 1; i <= n - 1; i++) {
                filter = subfilters.get(i);
                prop = filter.getProperty();
                boolean res = (filter instanceof CompositeFilter) ? filter.accept(node) : node.hasProperty(prop)&& filter.accept(node.getProperty(prop));
                 if (relations.get(i - 1).equals(Relation.AND)) {
                     result = result && res;
                 } else {
                     result = result || res;
                 }
             }
             return result;
         }
         return false;
     }
 
     /**
      * @return Returns the relations.
      */
     public List<Relation> getRelations() {
         return relations;
     }
 
     /**
      * @return Returns the subfilters.
      */
     public List<IFilter> getSubfilters() {
         return subfilters;
     }
 
     @Override
     public String toString() {
         return super.toString();
     }
 }
