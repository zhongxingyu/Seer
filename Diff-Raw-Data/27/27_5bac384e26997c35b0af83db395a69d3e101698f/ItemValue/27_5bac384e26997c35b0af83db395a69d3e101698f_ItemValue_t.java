 /**
  * This file is part of AMEE.
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package gc.carbon.domain.data;
 
 import com.jellymold.utils.domain.APIUtils;
 import com.jellymold.utils.domain.PersistentObject;
 import com.jellymold.utils.domain.UidGen;
 import gc.carbon.domain.*;
 import gc.carbon.domain.data.builder.v2.ItemValueBuilder;
 import gc.carbon.domain.path.Pathable;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.persistence.*;
 import java.util.Calendar;
 import java.util.Date;
 
 @Entity
 @Table(name = "ITEM_VALUE")
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class ItemValue implements PersistentObject, Pathable {
 
     // 32767 because this is bigger than 255, smaller
     // than 65535 and fits into an exact number of bits
     public final static int VALUE_SIZE = 32767;
     public final static int UNIT_SIZE = 255;
     public final static int PER_UNIT_SIZE = 255;
 
     @Id
     @GeneratedValue
     @Column(name = "ID")
     private Long id;
 
     @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
     private String uid = "";
 
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "ITEM_VALUE_DEFINITION_ID")
     private ItemValueDefinition itemValueDefinition;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "ITEM_ID")
     private Item item;
 
     @Column(name = "VALUE", nullable = false, length = VALUE_SIZE)
     private String value = "";
 
     
     @Column(name = "CREATED")
     private Date created = Calendar.getInstance().getTime();
 
     @Column(name = "MODIFIED")
     private Date modified = Calendar.getInstance().getTime();
 
     @Column(name = "UNIT", nullable = true, length = UNIT_SIZE)
     private String unit;
 
     @Column(name = "PER_UNIT", nullable = true, length = PER_UNIT_SIZE)
     private String perUnit;
 
     @Transient
     private Builder builder;
 
     public ItemValue() {
         super();
         setUid(UidGen.getUid());
         setBuilder(new ItemValueBuilder(this));
     }
 
     public ItemValue(ItemValueDefinition itemValueDefinition, Item item, String value) {
         this();
         setItemValueDefinition(itemValueDefinition);
         setItem(item);
         setValue(value);
         item.add(this);
     }
 
     public String toString() {
         return "ItemValue_" + getUid();
     }
 
     public void setBuilder(Builder builder) {
         this.builder = builder;
     }
 
     public String getUsableValue() {
         String value = getValue();
         if (value != null) {
             if (value.length() == 0) {
                 value = null;
             }
             // TODO: more validations, based on ValueType
         }
         return value;
     }
 
 
     @Transient
     public JSONObject getJSONObject(boolean detailed) throws JSONException {
         return builder.getJSONObject(detailed);
     }
 
     @Transient
     public JSONObject getJSONObject() throws JSONException {
         return getJSONObject(true);
     }
 
     @Transient
     public JSONObject getIdentityJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         obj.put("uid", getUid());
         obj.put("path", getPath());
         return obj;
     }
 
     @Transient
     public Element getElement(Document document) {
         return getElement(document, true);
     }
 
     @Transient
     public Element getElement(Document document, boolean detailed) {
         return builder.getElement(document, detailed);
     }
 
     @Transient
     public Element getIdentityElement(Document document) {
         return APIUtils.getIdentityElement(document, this);
 
     }
 
     @Transient
     public String getName() {
         return getItemValueDefinition().getName();
     }
 
     @Transient
     public String getDisplayName() {
         return getItemValueDefinition().getName();
     }
 
     @Transient
     public String getPath() {
         return getItemValueDefinition().getPath();
     }
 
     @Transient
     public String getDisplayPath() {
         return getItemValueDefinition().getPath();
     }
 
     @PrePersist
     public void onCreate() {
         Date now = Calendar.getInstance().getTime();
         setCreated(now);
         setModified(now);
     }
 
     @PreUpdate
     public void onModify() {
         setModified(Calendar.getInstance().getTime());
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getUid() {
         return uid;
     }
 
     public void setUid(String uid) {
         if (uid == null) {
             uid = "";
         }
         this.uid = uid;
     }
 
     public ItemValueDefinition getItemValueDefinition() {
         return itemValueDefinition;
     }
 
     public void setItemValueDefinition(ItemValueDefinition itemValueDefinition) {
         this.itemValueDefinition = itemValueDefinition;
     }
 
     public Item getItem() {
         return item;
     }
 
     public void setItem(Item item) {
         this.item = item;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         if (value == null) {
             value = "";
         }
         if (value.length() > VALUE_SIZE) {
             value = value.substring(0, VALUE_SIZE - 1);
         }
         this.value = value;
     }
 
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
     public Date getModified() {
         return modified;
     }
 
     public void setModified(Date modified) {
         this.modified = modified;
     }
 
     @Transient
     public ObjectType getObjectType() {
         return ObjectType.IV;
     }
 
     public Unit getUnit() {
         return (unit != null) ? Unit.valueOf(unit) : itemValueDefinition.getUnit();
     }
 
     public void setUnit(String unit) throws IllegalArgumentException {
         if (!itemValueDefinition.isValidUnit(unit)) {
             throw new IllegalArgumentException();
         }
         this.unit = unit;
     }
 
     public PerUnit getPerUnit() {
         return (perUnit != null) ? perUnit() : itemValueDefinition.getPerUnit();
     }
 
     public void setPerUnit(String perUnit) throws IllegalArgumentException {
         if (!itemValueDefinition.isValidPerUnit(perUnit)) {
             throw new IllegalArgumentException();
         }
         this.perUnit = perUnit;
     }
 
     public Unit getCompoundUnit() {
         return getUnit().with(getPerUnit());
     }
 
     public boolean hasUnits() {
         return itemValueDefinition.hasUnits();
     }
 
     public boolean hasPerUnits() {
         return itemValueDefinition.hasPerUnits();
     }
 
     private PerUnit perUnit() {
         if (perUnit.equals("none")) {
             return PerUnit.valueOf(getItem().getDuration());
         } else {
             return PerUnit.valueOf(perUnit);
         }
     }
 
     public ItemValue getCopy() {
         ItemValue clone = new ItemValue();
         clone.setUid(getUid());
         clone.setValue(getValue());
         clone.setItemValueDefinition(getItemValueDefinition());
         clone.setItem(getItem());
         if (hasUnits())
             clone.setUnit(getUnit().toString());
         if (hasPerUnits())
             clone.setPerUnit(getPerUnit().toString());
         return clone;
     }
 }
