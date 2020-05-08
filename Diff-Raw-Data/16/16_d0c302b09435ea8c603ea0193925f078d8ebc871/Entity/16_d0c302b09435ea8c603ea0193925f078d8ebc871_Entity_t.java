 package jpaoletti.jpm.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**An Entity is the visual representation and operation over a class of a data model. One entity is 
  * configured through an xml file.<br/>
  * <h2>Simple entity configuration file</h2>
  * <pre>
  * {@code
  * <?xml version='1.0' ?>
 * <!DOCTYPE entity SYSTEM "entity.dtd">
  * <entity id="entity_id" clazz="the.entity.Class" cached="true/false">
  *     <auditable>false</auditable>
  *     ...
  *     <operations>
  *     ...
  *     </operations>
  *     <field>
  *     ...
  *     </field>
  *     <field>
  *     ...
  *     </field>
  * </entity>
  * }
  * </pre>
  * @author jpaoletti
  * */
 public class Entity extends PMCoreObject {
 
     /**Represents the entity id. This must me unique.*/
     private String id;
     /**The full name of the class represented by the entity.*/
     private String clazz;
     /**A filter for the list of instances of the entity.
      * <br/>{@code <listfilter class="a.class.implementing.ListFilter" />}
      * @see ListFilter */
     private ListFilter listfilter;
     /**If defined, represents the order of the fields. 
      * <br/>{@code <order>field_id2 field_id3 field_id1</order>}
      * @see Field*/
     private String order;
     /**If defined, indicates the id of another entity to inherits the fields, not the 
      * operations for now.
      * <br/>{@code <extendz>other_entity_id</extendz>}*/
     private String extendz;
     /**The parent entity if extendz is defined*/
     private Entity extendzEntity;
     /**Indicates if the entity is auditable so every time an instance is modified, the PM will
      * create an auditory entry of the change 
      * <br/><pre>{@code <auditable>true</auditable>}</pre> 
      * */
     private Boolean auditable;
     /**Defines an owner to the entity. It makes this entity "weak".<br/>
      * <pre>
      * {@code
      *  <owner>
      *     <entityId>owner_entity_id</entityId>
      *     <entityProperty>owner_property</entityProperty>
      *     <localProperty>local_pointer_to_owner</localProperty>
      *     <entityCollectionClass></entityCollectionClass>
      *  </owner>
      *  }
      * </pre>
      * @see EntityOwner*/
     private EntityOwner owner;
     /**List of fields
      * @see Field*/
     private ArrayList<Field> fields;
     /**Internal map to optimize getFieldById() method
      * @see #getFieldById(String)*/
     private Map<String, Field> fieldsbyid;
     /**Operations of the entity. Standard operations are "add", "edit", "delete", "show", "list"
      * but the programmer can define whatever he wants.
      * <br/>{@code <operations>...</operations>}
      * @see Operations
      * @see Operation
      * @see OperationContext*/
     private Operations operations;
     /**A list of highlights.*/
     private Highlights highlights;
     /** Data Access*/
     private DataAccess dataAccess;
     /**Avoid counting items*/
     private Boolean noCount;
     private List<Entity> weaks;
     private boolean cached;
 
     /**Default constructor*/
     public Entity() {
         super();
         fieldsbyid = null;
         extendzEntity = null;
         cached = false;
     }
 
     /**
      * Return the list of fields including inherited ones.
      *
      * @return The list
      */
     public ArrayList<Field> getAllFields() {
         final ArrayList<Field> r = new ArrayList<Field>();
         final List<String> ids = new ArrayList<String>();
         if (getFields() != null) {
             for (Field field : getFields()) {
                 r.add(field);
                 ids.add(field.getId());
             }
         }
         if (getExtendzEntity() != null) {
             for (Field field : getExtendzEntity().getAllFields()) {
                 if (!ids.contains(field.getId())) {
                     r.add(field);
                 }
             }
         }
         return r;
     }
 
     /**
      * Returns a list of this entity instances with null from and count and
      * with the given filter
      *
      * @param ctx The context
      * @param filter The filter
      * @return The list
      * @throws PMException
      */
     public List<?> getList(PMContext ctx, EntityFilter filter) throws PMException {
         return getList(ctx, filter, null, null, null);
     }
 
     /**
      * Return a list of this entity instances with null from and count and the
      * filter took from the entity container of the context
      *
      * @param ctx The context
      * @return The list
      * @throws PMException
      */
     public List<?> getList(PMContext ctx) throws PMException {
         EntityFilter filter = (this.equals(ctx.getEntity())) ? ctx.getEntityContainer().getFilter() : null;
         return getList(ctx, filter, null, null, null);
     }
 
     /**
      * Returns a list taken from data access with the given parameters.
      * 
      * @param ctx The context
      * @param filter A filter
      * @param from The index of the first element
      * @param count The maximun number of items retrieved
      * @return The list
      * @throws PMException
      */
     public List<?> getList(PMContext ctx, EntityFilter filter, ListSort sort, Integer from, Integer count) throws PMException {
         return getDataAccess().list(ctx, filter, sort, from, count);
     }
 
     /**Getter for a field by its id
      * @param id The Field id
      * @return The Field with the given id*/
     public Field getFieldById(String id) {
         return getFieldsbyid().get(id);
     }
 
     /**Getter for fieldsbyid. If its null, this methods fill it
      * @return The mapped field list
      * */
     private Map<String, Field> getFieldsbyid() {
         if (fieldsbyid == null) {
             fieldsbyid = new HashMap<String, Field>();
             for (Field f : getAllFields()) {
                 fieldsbyid.put(f.getId(), f);
             }
         }
         return fieldsbyid;
     }
 
     /**This method sorts the fields and returns them
      * @return fields ordered
      * */
     public ArrayList<Field> getOrderedFields() {
         try {
             if (isOrdered()) {
                 ArrayList<Field> r = new ArrayList<Field>(getAllFields());
                 Collections.sort(r, new FieldComparator(getOrder()));
                 return r;
             }
         } catch (Exception e) {
             getPresentationManager().error(e);
         }
         return getAllFields();
     }
 
     /**Determine if the entity have the order property
      * @return true if order != null*/
     public boolean isOrdered() {
         return getOrder() != null;
     }
 
     /**
      * String representation of an entity
      * 
      * @return The string
      */
     @Override
     public String toString() {
         return "Entity (" + id + ") " + clazz;
     }
 
     /**This method fills the extendsFields variable with the parent Fields.
      * If some field is redefined, parent field is ignored
      * @param entity The parent entity given by PM engine*/
     @Deprecated
     public void fillFields(Entity entity) {
         for (Field field : entity.getAllFields()) {
             if (!containsField(field.getId())) {
                 getFields().add(field);
             }
         }
     }
 
     /**Check if there is a Field with an id
      * @param id The field id
      * @return true if the entity contains a Field with the given id*/
     private boolean containsField(String id) {
         return getFieldById(id) != null;
     }
 
     /**Getter for id
      * @return the id
      */
     public String getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(String id) {
         this.id = id;
     }
 
     /**Getter for clazz
      * @return the clazz
      */
     public String getClazz() {
         return clazz;
     }
 
     /**
      * @param clazz the clazz to set
      */
     public void setClazz(String clazz) {
         this.clazz = clazz;
     }
 
     /**Getter for listfilter
      * @return the listfilter
      */
     public ListFilter getListfilter() {
         return listfilter;
     }
 
     /**
      * @param listfilter the listfilter to set
      */
     public void setListfilter(ListFilter listfilter) {
         this.listfilter = listfilter;
     }
 
     /**Getter for order
      * @return the order
      */
     public String getOrder() {
         return order;
     }
 
     /**
      * @param order the order to set
      */
     public void setOrder(String order) {
         this.order = order;
     }
 
     /**The name of the parent entity
      * @return the extendz
      */
     public String getExtendz() {
         return extendz;
     }
 
     /**
      * @param extendz the extendz to set
      */
     public void setExtendz(String extendz) {
         this.extendz = extendz;
     }
 
     /**Indicates if the entity is auditable or not
      * @return the auditable
      */
     public boolean isAuditable() {
         if (auditable == null) {
             return false;
         }
         return auditable;
     }
 
     /**
      * @param auditable the auditable to set
      */
     public void setAuditable(boolean auditable) {
         this.auditable = auditable;
     }
 
     /**Getter for owner
      * @return the owner
      */
     public EntityOwner getOwner() {
         return owner;
     }
 
     /**
      * @param owner the owner to set
      */
     public void setOwner(EntityOwner owner) {
         this.owner = owner;
     }
 
     /** Getter for entity fields
      * @return the fields
      */
     public ArrayList<Field> getFields() {
         return fields;
     }
 
     /**
      * @param fields the fields to set
      */
     public void setFields(ArrayList<Field> fields) {
         this.fields = fields;
     }
 
     /**Getter for entity operations
      * @return the operations
      */
     public Operations getOperations() {
         return operations;
     }
 
     /**
      * @param operations the operations to set
      */
     public void setOperations(Operations operations) {
         this.operations = operations;
     }
 
     /**
      * @param extendzEntity the extendzEntity to set
      */
     public void setExtendzEntity(Entity extendzEntity) {
         this.extendzEntity = extendzEntity;
     }
 
     /**
      * @return the extendzEntity
      */
     public Entity getExtendzEntity() {
         return extendzEntity;
     }
 
     /**
      * @param highlights the highlights to set
      */
     public void setHighlights(Highlights highlights) {
         this.highlights = highlights;
     }
 
     /**
      * @return the highlights
      */
     public Highlights getHighlights() {
         return highlights;
     }
 
     /**
      * @return the dataAccess
      */
     public DataAccess getDataAccess() {
         if (dataAccess == null) {
             try {
                 dataAccess = (DataAccess) PresentationManager.getPm().newInstance(PresentationManager.getPm().getDefaultDataAccess());
                 dataAccess.setEntity(this);
             } catch (Exception e) {
                 getPresentationManager().error(e);
             }
         }
         return dataAccess;
     }
 
     /**
      * @param weaks the weaks to set
      */
     public void setWeaks(List<Entity> weaks) {
         this.weaks = weaks;
     }
 
     /**
      * @return the weaks
      */
     public List<Entity> getWeaks() {
         return weaks;
     }
 
     /**
      * Looks for the weak entity corresponding to the given field in this
      * string entity
      *
      * @param field
      * @return the weak entity
      */
     public Entity getWeak(Field field) {
         for (Entity entity : getWeaks()) {
             if (entity.getOwner().getEntityProperty().equals(field.getProperty())) {
                 return entity;
             }
         }
         return null;
     }
 
     /**
      * 
      * @return the hashcode
      */
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         return result;
     }
 
     /**
      * Compares two entities by id to check if they are equals
      * @param obj
      * @return true if both are the same entity
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Entity)) {
             return false;
         }
         Entity other = (Entity) obj;
         if (id == null) {
             if (other.id != null) {
                 return false;
             }
         } else if (!id.equals(other.id)) {
             return false;
         }
         return true;
     }
 
     /**
      * @param noCount the noCount to set
      */
     public void setNoCount(Boolean noCount) {
         this.noCount = noCount;
     }
 
     /**
      * @return the noCount
      */
     public Boolean getNoCount() {
         if (noCount == null) {
             return false;
         }
         return noCount;
     }
 
     /**
      * Looks for an apropiate highlight for this field+instance
      * @param field
      * @param instance
      * @return the highlight
      */
     public Highlight getHighlight(Field field, Object instance) {
         if (getHighlights() == null) {
             return null;
         }
         return getHighlights().getHighlight(this, field, instance);
     }
 
     /**
      *
      * @return true if the entity is weak
      */
     public boolean isWeak() {
         return getOwner() != null;
     }
 
     public boolean isCached() {
         return cached;
     }
 
     public void setCached(boolean cached) {
         this.cached = cached;
     }
 }
