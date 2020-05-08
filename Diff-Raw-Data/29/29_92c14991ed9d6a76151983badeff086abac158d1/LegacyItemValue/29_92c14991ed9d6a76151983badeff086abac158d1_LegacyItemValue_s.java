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
 package com.amee.domain.data;
 
 import com.amee.domain.*;
 import com.amee.domain.path.Pathable;
 import com.amee.platform.science.*;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Index;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.annotation.Resource;
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 @Table(name = "ITEM_VALUE")
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 @Configurable(autowire = Autowire.BY_TYPE)
 public class LegacyItemValue extends AMEEEntity implements Pathable, ExternalValue {
 
     // 32767 because this is bigger than 255, smaller than 65535 and fits into an exact number of bits.
     public final static int VALUE_SIZE = 32767;
     public final static int UNIT_SIZE = 255;
     public final static int PER_UNIT_SIZE = 255;
 
     @Transient
     @Resource
     private ILocaleService localeService;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "ITEM_VALUE_DEFINITION_ID")
     private ItemValueDefinition itemValueDefinition;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "ITEM_ID")
     private LegacyItem item;
 
     @Column(name = "VALUE", nullable = false, length = VALUE_SIZE)
     private String value = "";
 
     @Column(name = "UNIT", nullable = true, length = UNIT_SIZE)
     private String unit;
 
     @Column(name = "PER_UNIT", nullable = true, length = PER_UNIT_SIZE)
     private String perUnit;
 
     @Column(name = "START_DATE")
     @Index(name = "START_DATE_IND")
     private Date startDate = new Date();
 
     @Transient
     private boolean historyAvailable = false;
 
     @Transient
     private transient String fullPath;
 
     @Transient
     private transient ItemValue adapter;
 
     public LegacyItemValue() {
         super();
     }
 
     public LegacyItemValue(ItemValueDefinition itemValueDefinition, LegacyItem item) {
         this();
         setItemValueDefinition(itemValueDefinition);
         setItem(item);
         item.addItemValue(this);
         // Default startDate is that of the parent Item.
         this.startDate = item.getStartDate();
     }
 
     public LegacyItemValue(ItemValueDefinition itemValueDefinition, LegacyItem item, String value) {
         this(itemValueDefinition, item);
         setValue(value);
     }
 
     protected void copyTo(LegacyItemValue o) {
         super.copyTo(o);
         o.localeService = localeService;
         o.itemValueDefinition = itemValueDefinition;
         o.item = item;
         o.value = value;
         o.unit = unit;
         o.perUnit = perUnit;
         o.startDate = (startDate != null) ? (Date) startDate.clone() : null;
         o.historyAvailable = historyAvailable;
         o.fullPath = fullPath;
     }
 
     public LegacyItemValue getCopy() {
         LegacyItemValue clone = new LegacyItemValue();
         copyTo(clone);
         return clone;
     }
 
     public String getUsableValue() {
        if (!isUsableValue())
             return null;

        return getValue();
     }
 
     public boolean isUsableValue() {
         return !StringUtils.isBlank(getValue());
     }
 
 //    public JSONObject getJSONObject() throws JSONException {
 //        return getJSONObject(true);
 //    }
 
 //    public JSONObject getIdentityJSONObject() throws JSONException {
 //        JSONObject obj = new JSONObject();
 //        obj.put("uid", getUid());
 //        obj.put("path", getPath());
 //        return obj;
 //    }
 
 //    public Element getElement(Document document) {
 //        return getElement(document, true);
 //    }
 //
 //    public Element getElement(Document document, boolean detailed) {
 //        return builder.getElement(document, detailed);
 //    }
 
 //    public Element getIdentityElement(Document document) {
 //        return XMLUtils.getIdentityElement(document, "ItemValue", this);
 //    }
 
     /**
      * Returns the hierarchy of objects including this object.
      * <p/>
      * Note: This only used in the O&B UI.
      *
      * @return list of entities in hierarchical order
      */
     public List<IAMEEEntityReference> getHierarchy() {
         List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
         entities.add(this);
         entities.add(this.getItem());
         DataCategory dc = this.getItem().getDataCategory();
         while (dc != null) {
             entities.add(dc);
             dc = dc.getDataCategory();
         }
         Collections.reverse(entities);
         return entities;
     }
 
     public String getName() {
         return getItemValueDefinition().getName();
     }
 
     public String getDisplayName() {
         return getName();
     }
 
     public String getPath() {
         return getItemValueDefinition().getPath();
     }
 
     public String getDisplayPath() {
         return getPath();
     }
 
     /**
      * Get the full path of this Item.
      *
      * @return the full path
      */
     public String getFullPath() {
         // Need to build the fullPath?
         if (fullPath == null) {
             // Is there a parent.
             if (getItem() != null) {
                 // There is a parent.
                 fullPath = getItem().getFullPath() + "/" + getDisplayPath();
             } else {
                 // There must be a parent.
                 throw new RuntimeException("Item has no parent.");
             }
         }
         return fullPath;
     }
 
     public ItemValueDefinition getItemValueDefinition() {
         return itemValueDefinition;
     }
 
     public void setItemValueDefinition(ItemValueDefinition itemValueDefinition) {
         this.itemValueDefinition = itemValueDefinition;
     }
 
     public LegacyItem getItem() {
         return item;
     }
 
     public void setItem(LegacyItem item) {
         this.item = item;
     }
 
     public String getValue() {
         if (getItemValueDefinition().isText() && !LocaleHolder.isDefaultLocale()) {
             return localeService.getLocaleNameValue(this, value);
         } else {
             return value;
         }
     }
 
     public void setValue(String value) {
         // Make sure value is not null and is not too long.
         if (value == null) {
             value = "";
         }
         if (value.length() > VALUE_SIZE) {
             value = value.substring(0, VALUE_SIZE - 1);
         }
         // Ensure numerics are a valid format.
         if (getItemValueDefinition().isDouble() && !value.isEmpty()) {
             try {
                 Double.parseDouble(value);
             } catch (NumberFormatException e) {
                 log.warn("setValue() - Invalid number format: " + value);
                 throw new IllegalArgumentException("Invalid number format: " + value);
             }
         }
         this.value = value;
     }
 
     public StartEndDate getStartDate() {
         return new StartEndDate(startDate);
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public boolean isDouble() {
         return getItemValueDefinition().isDouble();
     }
 
     public ObjectType getObjectType() {
         return ObjectType.IV;
     }
 
     public AmountUnit getUnit() {
         return StringUtils.isNotBlank(unit) ? AmountUnit.valueOf(unit) : getItemValueDefinition().getUnit();
     }
 
     public AmountUnit getCanonicalUnit() {
         return getItemValueDefinition().getUnit();
     }
 
     public void setUnit(String unit) throws IllegalArgumentException {
         if (!getItemValueDefinition().isValidUnit(unit)) {
             throw new IllegalArgumentException("The unit argument is not valid: " + unit);
         }
         this.unit = unit;
     }
 
     public AmountPerUnit getPerUnit() {
         if (StringUtils.isNotBlank(perUnit)) {
             if (perUnit.equals("none")) {
                 return AmountPerUnit.valueOf(getItem().getDuration());
             } else {
                 return AmountPerUnit.valueOf(perUnit);
             }
         } else {
             return getItemValueDefinition().getPerUnit();
         }
     }
 
     public AmountPerUnit getCanonicalPerUnit() {
         return getItemValueDefinition().getPerUnit();
     }
 
     public void setPerUnit(String perUnit) throws IllegalArgumentException {
         if (!getItemValueDefinition().isValidPerUnit(perUnit)) {
             throw new IllegalArgumentException("The perUnit argument is not valid: " + perUnit);
         }
         this.perUnit = perUnit;
     }
 
     @Override
     public AmountCompoundUnit getCompoundUnit() {
         return getUnit().with(getPerUnit());
     }
 
     @Override
     public AmountCompoundUnit getCanonicalCompoundUnit() {
         return getItemValueDefinition().getCanonicalCompoundUnit();
     }
 
     @Override
     public boolean hasUnit() {
         return (StringUtils.isNotBlank(unit) && getItemValueDefinition().isAnyUnit()) || getItemValueDefinition().hasUnit();
     }
 
     @Override
     public boolean hasPerUnit() {
         return (StringUtils.isNotBlank(perUnit) && getItemValueDefinition().isAnyPerUnit()) || getItemValueDefinition().hasPerUnit();
     }
 
     public boolean hasPerTimeUnit() {
         return hasPerUnit() && getPerUnit().isTime();
     }
 
     public boolean isNonZero() {
         return getItemValueDefinition().isDouble() &&
                 !StringUtils.isBlank(getValue()) &&
                 Double.parseDouble(getValue()) != 0.0;
     }
 
     public String getLabel() {
         return getItemValueDefinition().getLabel();
     }
 
     @Override
     public boolean isTrash() {
         return status.equals(AMEEStatus.TRASH) || getItem().isTrash() || getItemValueDefinition().isTrash();
     }
 
     /**
      * As an ExternalValue, an ItemValue is convertible. It can be converted, at runtime, from one unit to another.
      *
      * @return true, always
      */
     public boolean isConvertible() {
         return true;
     }
 
     public boolean isHistoryAvailable() {
         return historyAvailable;
     }
 
     public void setHistoryAvailable(boolean historyAvailable) {
         this.historyAvailable = historyAvailable;
     }
 
     public ItemValue getAdapter() {
         return adapter;
     }
 
     public void setAdapter(ItemValue adapter) {
         this.adapter = adapter;
     }
 }
