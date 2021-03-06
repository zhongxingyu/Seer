 /*******************************************************************************
  * Copyright (c) 2011 OBiBa. All rights reserved.
  *  
  * This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0.
  *  
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.obiba.opal.web.gwt.app.client.wizard.derive.view;
 
 import org.obiba.opal.web.gwt.app.client.navigator.view.VariableViewHelper;
 import org.obiba.opal.web.model.client.magma.CategoryDto;
 
 public class ValueMapEntry {
   public enum ValueMapEntryType {
     CATEGORY_NAME, DISTINCT_VALUE, RANGE, EMPTY_VALUES, OTHER_VALUES
   }
 
   private ValueMapEntryType type;
 
   private String value;
 
   private String label;
 
   private String newValue;
 
   private boolean missing;
 
   private ValueMapEntry(ValueMapEntryType type, String value, String label, String newValue, boolean missing) {
     super();
     this.type = type;
     this.value = value;
    this.label = value;
     this.newValue = newValue;
     this.missing = missing;
   }
 
   public boolean isType(ValueMapEntryType... types) {
     for(ValueMapEntryType t : types) {
       if(type.equals(t)) return true;
     }
     return false;
   }
 
   public ValueMapEntryType getType() {
     return type;
   }
 
   public String getValue() {
     return value;
   }
 
   public String getLabel() {
     return label;
   }
 
   public String getNewValue() {
     return newValue;
   }
 
   public void setNewValue(String newValue) {
     this.newValue = newValue;
   }
 
   public boolean isMissing() {
     return missing;
   }
 
   public void setMissing(boolean missing) {
     this.missing = missing;
   }
 
   public static Builder fromCategory(CategoryDto cat) {
     return new Builder(ValueMapEntryType.CATEGORY_NAME).value(cat.getName()).label(VariableViewHelper.getLabelValue(cat.getAttributesArray()));
   }
 
   public static Builder fromDistinct(String value) {
     return new Builder(ValueMapEntryType.DISTINCT_VALUE).value(value).label("");
   }
 
   public static Builder fromRange(Long lower, Long upper) {
     String value = "";
     if(lower == null) {
       value = "-" + upper;
     } else if(upper == null) {
       value = lower + "+";
     } else {
       value = lower + "-" + upper;
     }
     return new Builder(ValueMapEntryType.RANGE).value(value).label("");
   }
 
   public static Builder createEmpties(String label) {
     return new Builder(ValueMapEntryType.EMPTY_VALUES).value("null").label(label).missing();
   }
 
   public static Builder createOthers(String label) {
     return new Builder(ValueMapEntryType.OTHER_VALUES).value("*").label(label);
   }
 
   public static Builder create(ValueMapEntryType type) {
     return new Builder(type);
   }
 
   public static class Builder {
     private ValueMapEntry entry;
 
     private Builder(ValueMapEntryType type) {
       entry = new ValueMapEntry(type, "", "", "", false);
     }
 
     public Builder value(String value) {
       entry.value = value;
       return this;
     }
 
     public Builder label(String label) {
       entry.label = label;
       return this;
     }
 
     public Builder newValue(String newValue) {
       entry.newValue = newValue;
       return this;
     }
 
     public Builder missing() {
       entry.missing = true;
       return this;
     }
 
     public ValueMapEntry build() {
       return entry;
     }
   }
 }
