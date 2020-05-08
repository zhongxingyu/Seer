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
 import com.amee.domain.item.BaseItemValue;
 import com.amee.domain.item.HistoryValue;
 import com.amee.domain.item.NumberValue;
 import com.amee.domain.item.data.*;
 import com.amee.domain.item.profile.BaseProfileItemValue;
 import com.amee.domain.item.profile.NuProfileItem;
 import com.amee.domain.item.profile.ProfileItemNumberValue;
 import com.amee.domain.item.profile.ProfileItemTextValue;
 import com.amee.domain.path.Pathable;
 import com.amee.platform.science.*;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.persistence.Transient;
 import java.util.Date;
 import java.util.List;
 
 @Configurable(autowire = Autowire.BY_TYPE)
 public class ItemValue extends AMEEEntityAdapter implements Pathable, ExternalValue {
 
     @Autowired
     private IDataItemService dataItemService;
 
     @Autowired
     private IProfileItemService profileItemService;
 
     private LegacyItemValue legacyEntity;
     private BaseItemValue nuEntity;
 
     @Transient
    private transient Object valueOveride;
 
     public ItemValue() {
         super();
     }
 
     public ItemValue(ItemValueDefinition itemValueDefinition, Item item, boolean isHistory) {
         super();
         if (item.isLegacy()) {
             setLegacyEntity(new LegacyItemValue(itemValueDefinition, item.getLegacyEntity()));
             getLegacyEntity().setAdapter(this);
         } else {
             BaseItemValue itemValue;
             // Data or Profile?
             if (NuDataItem.class.isAssignableFrom(item.getNuEntity().getClass())) {
                 // Work with DataItems.
                 NuDataItem dataItem = (NuDataItem) item.getNuEntity();
                 // Create a nu style value.
                 if (itemValueDefinition.getValueDefinition().getValueType().equals(ValueType.INTEGER) ||
                         itemValueDefinition.getValueDefinition().getValueType().equals(ValueType.DOUBLE)) {
                     // Item is a number.
                     if (isHistory) {
                         itemValue = new DataItemNumberValueHistory(itemValueDefinition, dataItem);
                     } else {
                         itemValue = new DataItemNumberValue(itemValueDefinition, dataItem);
                     }
                 } else {
                     // Item is text.
                     if (isHistory) {
                         itemValue = new DataItemTextValueHistory(itemValueDefinition, dataItem);
                     } else {
                         itemValue = new DataItemTextValue(itemValueDefinition, dataItem);
                     }
                 }
             } else if (NuProfileItem.class.isAssignableFrom(item.getNuEntity().getClass())) {
                 // Work with ProfileItems.
                 NuProfileItem profileItem = (NuProfileItem) item.getNuEntity();
                 // Create a nu style value.
                 if (itemValueDefinition.getValueDefinition().getValueType().equals(ValueType.INTEGER) ||
                         itemValueDefinition.getValueDefinition().getValueType().equals(ValueType.DOUBLE)) {
                     // Item is a number.
                     itemValue = new ProfileItemNumberValue(itemValueDefinition, profileItem);
                 } else {
                     // Item is text.
                     itemValue = new ProfileItemTextValue(itemValueDefinition, profileItem);
                 }
             } else {
                 throw new IllegalStateException("Item should be either a NuDataItem or NuProfileItem.");
             }
             setNuEntity(itemValue);
             getNuEntity().setAdapter(this);
         }
     }
 
     public ItemValue(ItemValueDefinition itemValueDefinition, Item item, String value) {
         this(itemValueDefinition, item, false);
         setValue(value);
     }
 
     public ItemValue(ItemValueDefinition itemValueDefinition, Item item, String value, boolean isHistory) {
         this(itemValueDefinition, item, isHistory);
         setValue(value);
     }
 
     public ItemValue(LegacyItemValue itemValue) {
         super();
         setLegacyEntity(itemValue);
         getLegacyEntity().setAdapter(this);
     }
 
     public ItemValue(BaseItemValue itemValue) {
         super();
         setNuEntity(itemValue);
         getNuEntity().setAdapter(this);
     }
 
     public static ItemValue getItemValue(LegacyItemValue legacyItemValue) {
         if (legacyItemValue != null) {
             if (legacyItemValue.getAdapter() != null) {
                 return legacyItemValue.getAdapter();
             } else {
                 return new ItemValue(legacyItemValue);
             }
         } else {
             return null;
         }
     }
 
     public static ItemValue getItemValue(BaseItemValue baseItemValue) {
         if (baseItemValue != null) {
             if (baseItemValue.getAdapter() != null) {
                 return baseItemValue.getAdapter();
             } else {
                 return new ItemValue(baseItemValue);
             }
         } else {
             return null;
         }
     }
 
     public ItemValue getCopy() {
         if (isLegacy()) {
             return ItemValue.getItemValue(getLegacyEntity().getCopy());
         } else {
             return ItemValue.getItemValue(getNuEntity().getCopy());
         }
     }
 
     @Override
     public String getUsableValue() {
         if (isLegacy()) {
             return getLegacyEntity().getUsableValue();
         } else {
             return getNuEntity().getUsableValue();
         }
     }
 
     public boolean isUsableValue() {
         if (isLegacy()) {
             return getLegacyEntity().isUsableValue();
         } else {
             return getNuEntity().isUsableValue();
         }
     }
 
     public List<IAMEEEntityReference> getHierarchy() {
         if (isLegacy()) {
             return getLegacyEntity().getHierarchy();
         } else {
             return getNuEntity().getHierarchy();
         }
     }
 
     @Override
     public String getName() {
         if (isLegacy()) {
             return getLegacyEntity().getName();
         } else {
             return getNuEntity().getName();
         }
     }
 
     @Override
     public String getDisplayName() {
         if (isLegacy()) {
             return getLegacyEntity().getDisplayName();
         } else {
             return getNuEntity().getDisplayName();
         }
     }
 
     @Override
     public String getPath() {
         if (isLegacy()) {
             return getLegacyEntity().getPath();
         } else {
             return getNuEntity().getPath();
         }
     }
 
     @Override
     public String getDisplayPath() {
         if (isLegacy()) {
             return getLegacyEntity().getDisplayPath();
         } else {
             return getNuEntity().getDisplayPath();
         }
     }
 
     @Override
     public String getFullPath() {
         if (isLegacy()) {
             return getLegacyEntity().getFullPath();
         } else {
             return getNuEntity().getFullPath();
         }
     }
 
     public ItemValueDefinition getItemValueDefinition() {
         if (isLegacy()) {
             return getLegacyEntity().getItemValueDefinition();
         } else {
             return getNuEntity().getItemValueDefinition();
         }
     }
 
     public void setItemValueDefinition(ItemValueDefinition itemValueDefinition) {
         if (isLegacy()) {
             getLegacyEntity().setItemValueDefinition(itemValueDefinition);
         } else {
             getNuEntity().setItemValueDefinition(itemValueDefinition);
         }
     }
 
     public Item getItem() {
         if (isLegacy()) {
             return Item.getItem(getLegacyEntity().getItem());
         } else {
             return Item.getItem(getNuEntity().getItem());
         }
     }
 
     public void setItem(Item item) {
         if (isLegacy()) {
             getLegacyEntity().setItem(item.getLegacyEntity());
         } else {
             getNuEntity().setItem(item.getNuEntity());
         }
     }
 
     public String getValue() {
        if (valueOveride != null) {
            return valueOveride.toString();
         }
         
         if (isLegacy()) {
             return getLegacyEntity().getValue();
         } else {
             return getNuEntity().getValueAsString();
         }
     }
 
     public void setValue(String value) {
         if (isLegacy()) {
             getLegacyEntity().setValue(value);
         } else {
             getNuEntity().setValue(value);
         }
     }
 
     @Override
     public StartEndDate getStartDate() {
         if (isLegacy()) {
             return getLegacyEntity().getStartDate();
         } else {
             if (BaseProfileItemValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return ((BaseProfileItemValue) getNuEntity()).getProfileItem().getStartDate();
             } else if (BaseDataItemValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 if (ExternalHistoryValue.class.isAssignableFrom(getNuEntity().getClass())) {
                     return ((ExternalHistoryValue) getNuEntity()).getStartDate();
                 } else {
                     return new StartEndDate(IDataItemService.EPOCH);
                 }
             } else {
                 throw new IllegalStateException("A BaseProfileItemValue or BaseDataItemValue instance was expected.");
             }
         }
     }
 
     public void setStartDate(Date startDate) {
         if (isLegacy()) {
             getLegacyEntity().setStartDate(startDate);
         } else {
             if (BaseDataItemValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 if (HistoryValue.class.isAssignableFrom(getNuEntity().getClass())) {
                     ((HistoryValue) getNuEntity()).setStartDate(startDate);
                 }
             } else {
                 throw new IllegalStateException("A BaseDataItemValue instance was expected.");
             }
         }
     }
 
     @Override
     public boolean isDouble() {
         if (isLegacy()) {
             return getLegacyEntity().isDouble();
         } else {
             return getNuEntity().isDouble();
         }
     }
 
     @Override
     public AmountUnit getUnit() {
         if (isLegacy()) {
             return getLegacyEntity().getUnit();
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return ((NumberValue) getNuEntity()).getUnit();
             } else {
                 return getItemValueDefinition().getUnit();
             }
         }
     }
 
     @Override
     public AmountUnit getCanonicalUnit() {
         if (isLegacy()) {
             return getLegacyEntity().getCanonicalUnit();
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return ((NumberValue) getNuEntity()).getCanonicalUnit();
             } else {
                 return getItemValueDefinition().getUnit();
             }
         }
     }
 
     public void setUnit(String unit) throws IllegalArgumentException {
         if (isLegacy()) {
             getLegacyEntity().setUnit(unit);
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 ((NumberValue) getNuEntity()).setUnit(unit);
             } else {
                 throw new IllegalStateException("A NumberValue was expected.");
             }
         }
     }
 
     @Override
     public AmountPerUnit getPerUnit() {
         if (isLegacy()) {
             return getLegacyEntity().getPerUnit();
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return ((NumberValue) getNuEntity()).getPerUnit();
             } else {
                 return getItemValueDefinition().getPerUnit();
             }
         }
     }
 
     @Override
     public AmountPerUnit getCanonicalPerUnit() {
         if (isLegacy()) {
             return getLegacyEntity().getCanonicalPerUnit();
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return ((NumberValue) getNuEntity()).getCanonicalPerUnit();
             } else {
                 return getItemValueDefinition().getPerUnit();
             }
         }
     }
 
     public void setPerUnit(String perUnit) throws IllegalArgumentException {
         if (isLegacy()) {
             getLegacyEntity().setPerUnit(perUnit);
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 ((NumberValue) getNuEntity()).setPerUnit(perUnit);
             } else {
                 throw new IllegalStateException("A NumberValue was expected.");
             }
         }
     }
 
     @Override
     public AmountCompoundUnit getCompoundUnit() {
         if (isLegacy()) {
             return getLegacyEntity().getCompoundUnit();
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return ((NumberValue) getNuEntity()).getCompoundUnit();
             } else {
                 return getUnit().with(getPerUnit());
             }
         }
     }
 
     @Override
     public AmountCompoundUnit getCanonicalCompoundUnit() {
         if (isLegacy()) {
             return getLegacyEntity().getCanonicalCompoundUnit();
         } else {
             if (NumberValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return ((NumberValue) getNuEntity()).getCanonicalCompoundUnit();
             } else {
                 return getItemValueDefinition().getCanonicalCompoundUnit();
             }
         }
     }
 
     @Override
     public boolean hasUnit() {
         if (isLegacy()) {
             return getLegacyEntity().hasUnit();
         } else {
             return NumberValue.class.isAssignableFrom(getNuEntity().getClass()) &&
                     ((NumberValue) getNuEntity()).hasUnit();
         }
     }
 
     @Override
     public boolean hasPerUnit() {
         if (isLegacy()) {
             return getLegacyEntity().hasPerUnit();
         } else {
             return NumberValue.class.isAssignableFrom(getNuEntity().getClass()) &&
                     ((NumberValue) getNuEntity()).hasPerUnit();
         }
     }
 
     public boolean hasPerTimeUnit() {
         return hasPerUnit() && getPerUnit().isTime();
     }
 
     public boolean isNonZero() {
         if (isLegacy()) {
             return getLegacyEntity().isNonZero();
         } else {
             return getNuEntity().isNonZero();
         }
     }
 
     @Override
     public String getLabel() {
         if (isLegacy()) {
             return getLegacyEntity().getLabel();
         } else {
             return getNuEntity().getLabel();
         }
     }
 
     @Override
     public boolean isTrash() {
         return getAdaptedEntity().isTrash();
     }
 
     @Override
     public boolean isConvertible() {
         if (isLegacy()) {
             return getLegacyEntity().isConvertible();
         } else {
             return getNuEntity().isConvertible();
         }
     }
 
     public boolean isHistoryAvailable() {
         if (isLegacy()) {
             return getLegacyEntity().isHistoryAvailable();
         } else {
             return getNuEntity().isHistoryAvailable();
         }
     }
 
     public void setHistoryAvailable(boolean historyAvailable) {
         if (isLegacy()) {
             getLegacyEntity().setHistoryAvailable(historyAvailable);
         } else {
             getNuEntity().setHistoryAvailable(historyAvailable);
         }
     }
 
     @Override
     public ObjectType getObjectType() {
         return ObjectType.IV;
     }
 
     public IItemService getItemService() {
         if (isLegacy()) {
             throw new IllegalStateException("IItemService implementations should not use the new services.");
         } else {
             if (BaseDataItemValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return dataItemService;
             } else if (BaseProfileItemValue.class.isAssignableFrom(getNuEntity().getClass())) {
                 return profileItemService;
             } else {
                 throw new IllegalStateException("A BaseDataItemValue or BaseProfileItemValue was expected.");
             }
         }
     }
 
     public LegacyItemValue getLegacyEntity() {
         return legacyEntity;
     }
 
     public void setLegacyEntity(LegacyItemValue legacyEntity) {
         legacyEntity.setAdapter(this);
         this.legacyEntity = legacyEntity;
     }
 
     public BaseItemValue getNuEntity() {
         return nuEntity;
     }
 
     public void setNuEntity(BaseItemValue nuEntity) {
         this.nuEntity = nuEntity;
     }
 
    public Object getValueOveride() {
        return valueOveride;
     }
 
    public void setValueOveride(Object valueOveride) {
        this.valueOveride = valueOveride;
     }
 }
