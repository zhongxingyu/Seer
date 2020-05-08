 package org.jboss.pressgang.ccms.rest.v1.collections.contentspec.join;
 
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1.ADD_STATE;
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1.REMOVE_STATE;
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseUpdateCollectionItemV1.UPDATE_STATE;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseUpdateCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.join.RESTCSRelatedNodeCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.join.RESTCSRelatedNodeV1;
 
 public class RESTCSRelatedNodeCollectionV1 extends RESTBaseUpdateCollectionV1<RESTCSRelatedNodeV1, RESTCSRelatedNodeCollectionV1,
         RESTCSRelatedNodeCollectionItemV1> {

    private List<RESTCSRelatedNodeCollectionItemV1> items;
 
     @Override
     public List<RESTCSRelatedNodeCollectionItemV1> getItems() {
         return items;
     }
 
     @Override
     public void setItems(List<RESTCSRelatedNodeCollectionItemV1> items) {
         this.items = items;
     }
 
     @Override
     protected void addItem(final RESTCSRelatedNodeV1 item, final Integer state) {
         if (items == null) {
             this.items = new ArrayList<RESTCSRelatedNodeCollectionItemV1>();
         }
 
         items.add(new RESTCSRelatedNodeCollectionItemV1(item, state));
     }
 
     /**
      * This method will clear out any child items that are marked for both add and remove, or duplicated add and remove requests.
      * Override this method to deal with collections where the children are not uniquely identified by only their id.
      */
     @Override
     protected void ignoreDuplicatedChangeItemRequests() {
         if (this.getItems() != null) {
             final List<RESTCSRelatedNodeCollectionItemV1> items = new ArrayList<RESTCSRelatedNodeCollectionItemV1>(this.getItems());
         
             /* on the second loop, remove any items that are marked for both add and remove is separate items */
             for (int i = 0; i < items.size(); ++i) {
                 final RESTCSRelatedNodeCollectionItemV1 child1 = items.get(i);
                 final RESTCSRelatedNodeV1 childItem1 = child1.getItem();
 
                 // New Entity so ignore it
                 if (childItem1.getId() == null) continue;
                 
                 /* at this point we know that either add1 or remove1 will be true, but not both */
                 final boolean add1 = child1.getState() == ADD_STATE;
                 final boolean remove1 = child1.getState() == REMOVE_STATE;
                 final boolean update1 = child1.getState() == UPDATE_STATE;
                 
                 /* Loop a second time, looking for duplicates */
                 for (int j = i + 1; j < items.size(); ++j) {
                     final RESTCSRelatedNodeCollectionItemV1 child2 = items.get(j);
                     final RESTCSRelatedNodeV1 childItem2 = child2.getItem();
 
                     // New Entity so ignore it
                     if (childItem2.getId() == null) continue;
 
                     final boolean relationshipIdEqual = childItem1.getRelationshipId() == null && childItem2.getRelationshipId() == null
                             || childItem1.getRelationshipId() != null && childItem1.getRelationshipId().equals(
                             childItem2.getRelationshipId());
                     
                     /* Check the RelatedNode objects for their value as well as their IDs to ensure duplicates are removed*/
                     if (childItem1.getId().equals(childItem2.getId()) && relationshipIdEqual) {
                         final boolean add2 = child2.getState() == ADD_STATE;
                         final boolean remove2 = child2.getState() == REMOVE_STATE;
                         final boolean update2 = child2.getState() == UPDATE_STATE;
                         
                         /* Do some checks on values that could be null */
                         final boolean typeEqual = childItem1.getRelationshipType() == null && childItem2.getRelationshipType() == null ||
                                 childItem1.getRelationshipType() != null && childItem1.getRelationshipType().equals(
                                 childItem2.getRelationshipType());
                         
                         /* check for double add, double remove, double update, and remove one instance */
                         if ((add1 && add2) || (remove1 && remove2) || (update1 && update2)) {
                             /* 
                              * If the relationship types are equal then we only need to remove one item. If
                              * the are different then both should be removed. 
                              */
                             if (typeEqual) {
                                 this.getItems().remove(child1);
                             } else {
                                 this.getItems().remove(child1);
                                 this.getItems().remove(child2);
                             }
                         }
                         
                         /* check for double add, double remove, add and remove, remove and add */
                         if ((add1 && remove2) || (remove1 && add2) || (update1 && remove2) || (update2 && remove1) || (update1 && add2)
                                 || (update2 && add1)) {
                             this.getItems().remove(child1);
                             this.getItems().remove(child2);
                         }
                     }
                 }
             }
         }
     }
 }
