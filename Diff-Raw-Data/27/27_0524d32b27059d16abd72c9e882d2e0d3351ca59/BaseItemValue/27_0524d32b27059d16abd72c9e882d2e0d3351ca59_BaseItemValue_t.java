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
 package com.amee.domain.item;
 
 import com.amee.domain.AMEEEntity;
 import com.amee.domain.IAMEEEntityReference;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.data.ItemValueDefinition;
 import com.amee.domain.path.Pathable;
 import com.amee.platform.science.ExternalGenericValue;
 import org.apache.commons.lang.StringUtils;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 @MappedSuperclass
 public abstract class BaseItemValue extends AMEEEntity implements Pathable, ExternalGenericValue {
 
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "ITEM_VALUE_DEFINITION_ID")
     private ItemValueDefinition itemValueDefinition;
 
     @Transient
     private transient boolean historyAvailable;
 
     @Transient
     private transient String fullPath;
 
     @Transient
     private transient ItemValue adapter;
 
     public BaseItemValue() {
         super();
     }
 
     public BaseItemValue(ItemValueDefinition itemValueDefinition) {
         this();
         setItemValueDefinition(itemValueDefinition);
     }
 
     protected void copyTo(BaseItemValue o) {
         super.copyTo(o);
         o.itemValueDefinition = itemValueDefinition;
        o.historyAvailable = historyAvailable;
     }
 
     public BaseItemValue getCopy() {
         try {
             BaseItemValue clone = getClass().newInstance();
             copyTo(clone);
             return clone;
        } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Check that the ItemValueDefinition matches the BaseItemValue instance.
      */
     public abstract void checkItemValueDefinition();
 
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
 
     public boolean isHistoryAvailable() {
         return historyAvailable;
     }
 
     public void setHistoryAvailable(boolean historyAvailable) {
         this.historyAvailable = historyAvailable;
     }
 
     /**
      * Get the full path of this Item.
      *
      * @return the full path
      */
     @Override
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
 
     @Override
     public String getName() {
         return getItemValueDefinition().getName();
     }
 
     @Override
     public String getDisplayName() {
         return getName();
     }
 
     @Override
     public String getPath() {
         return getItemValueDefinition().getPath();
     }
 
     @Override
     public String getDisplayPath() {
         return getPath();
     }
 
     @Override
     public String getLabel() {
         return getItemValueDefinition().getLabel();
     }

     public boolean isNonZero() {
         return getItemValueDefinition().isDouble() &&
                 !StringUtils.isBlank(getValueAsString()) &&
                 Double.parseDouble(getValueAsString()) != 0.0;
     }
 
     public ItemValueDefinition getItemValueDefinition() {
         return itemValueDefinition;
     }
 
     public void setItemValueDefinition(ItemValueDefinition itemValueDefinition) {
         this.itemValueDefinition = itemValueDefinition;
         checkItemValueDefinition();
     }
 
     public abstract BaseItem getItem();
 
     public abstract void setItem(BaseItem item);
 
     public abstract String getValueAsString();
 
     public abstract boolean isUsableValue();
 
     public String getUsableValue() {
         if (!isUsableValue()) {
             return null;
         } else {
             return getValueAsString();
         }
     }
 
     public abstract void setValue(String value);
 
     public ItemValue getAdapter() {
         return adapter;
     }
 
     public void setAdapter(ItemValue adapter) {
         this.adapter = adapter;
     }
 
 }
