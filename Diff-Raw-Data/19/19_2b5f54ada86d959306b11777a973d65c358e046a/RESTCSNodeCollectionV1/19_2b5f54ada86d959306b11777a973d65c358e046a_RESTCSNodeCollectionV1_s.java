 package org.jboss.pressgang.ccms.rest.v1.collections.contentspec;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseUpdateCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTCSNodeCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 
 public class RESTCSNodeCollectionV1 extends RESTBaseUpdateCollectionV1<RESTCSNodeV1, RESTCSNodeCollectionV1, RESTCSNodeCollectionItemV1> {

    private List<RESTCSNodeCollectionItemV1> items;
 
     @Override
     public List<RESTCSNodeCollectionItemV1> getItems() {
         return items;
     }
 
     @Override
     public void setItems(List<RESTCSNodeCollectionItemV1> items) {
         this.items = items;
     }
 
     @Override
     protected void addItem(final RESTCSNodeV1 item, final Integer state) {
         if (items == null) {
             this.items = new ArrayList<RESTCSNodeCollectionItemV1>();
         }
 
         items.add(new RESTCSNodeCollectionItemV1(item, state));
     }
 }
