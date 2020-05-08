 package org.jboss.pressgang.ccms.rest.v1.collections.contentspec;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTContentSpecCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 
 public class RESTContentSpecCollectionV1 extends RESTBaseCollectionV1<RESTContentSpecV1, RESTContentSpecCollectionV1,
         RESTContentSpecCollectionItemV1> {
    private List<RESTContentSpecCollectionItemV1> items = new ArrayList<RESTContentSpecCollectionItemV1>();
 
     @Override
     public List<RESTContentSpecCollectionItemV1> getItems() {
         return items;
     }
 
     @Override
     public void setItems(final List<RESTContentSpecCollectionItemV1> items) {
         this.items = items;
     }
 
     @Override
     protected void addItem(final RESTContentSpecV1 item, final Integer state) {
         if (items == null) {
             this.items = new ArrayList<RESTContentSpecCollectionItemV1>();
         }
 
         items.add(new RESTContentSpecCollectionItemV1(item, state));
     }
 }
