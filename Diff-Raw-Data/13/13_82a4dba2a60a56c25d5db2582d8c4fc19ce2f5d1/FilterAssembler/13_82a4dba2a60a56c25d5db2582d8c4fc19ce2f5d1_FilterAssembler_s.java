 /*
  * Copyright open knowledge GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.openknowledge.util.dge.filter.web;
 
 import de.openknowledge.util.dge.filter.FilterChoiceFieldMetaData;
 import de.openknowledge.util.dge.filter.FilterExpression;
 import de.openknowledge.util.dge.filter.FilterFieldMetaData;
 import de.openknowledge.util.dge.filter.FilterFieldType;
 import de.openknowledge.util.dge.filter.FilterManager;
 import de.openknowledge.util.dge.filter.FilterOperand;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import static org.apache.commons.lang.Validate.notNull;
 
 /**
  * Represents the filter rows for a dynamic filter form. Finally the valid filter rows must be converted into a FilterExpression.
  *
  * @author Marc Petersen - open knowledge GmbH
  */
 public class FilterAssembler<T extends Collection> implements Serializable {
 
   private static final long serialVersionUID = -4108166213555175328L;
   private static final Log LOG = LogFactory.getLog(FilterAssembler.class);
 
   private boolean filterActive = false;
   private List<FilterOperand> possibleOperands = Arrays.asList(FilterOperand.values());
 
   private List<FilterRow> filterRows;
   private Class<?> filterLine;
   private List<Object> filterChoiceHolders = new ArrayList<Object>();
 
   private transient FilterManager filterManager;
 
   /**
    * Allocates a <code>FilterAssembler</code> object.
    * <p/>
   * <i>Note:</i> Use <code>FilterAssembler(Class<?> aFilterLine, Class<?>... theChoiceManagers)</code> if <code>aFilterLine</code> contains
   * {@link de.openknowledge.util.dge.filter.annotation.FilterChoiceField} annotations.
    *
    * @param aFilterLine The class which contains {@link de.openknowledge.util.dge.filter.annotation.FilterField} annotations.
    */
   public FilterAssembler(Class<?> aFilterLine) {
     notNull(aFilterLine);
 
     filterLine = aFilterLine;
 
     resetFilter();
   }
 
   /**
    * Allocates a <code>FilterManager</code> object and initializes it so that it is able to extract {@link FilterFieldMetaData} from the
    * given <code>aFilterLine</code> class. It expects at least one class - <code>theChoiceHolders</code> - which contains {@link
    * de.openknowledge.util.dge.filter.annotation.FilterChoice} annotations as sources for {@linkplain
    * de.openknowledge.util.dge.filter.annotation.FilterChoiceField}s.
    * <p/>
    * <i>Note</i>: Use <code>FilterManager(Class<?> aFilterLine)</code> if the <code>aFilterLine</code> class does not contain {@link
    * de.openknowledge.util.dge.filter.annotation.FilterChoiceField} annotations
    *
    * @param aFilterLine      The class which contains {@link de.openknowledge.util.dge.filter.annotation.FilterField} annotations.
    * @param theChoiceHolders One or more classes to scan for {@link de.openknowledge.util.dge.filter.annotation.FilterChoice} annotations.
    */
   public FilterAssembler(Class<?> aFilterLine, Object... theChoiceHolders) {
     notNull(aFilterLine);
     notNull(theChoiceHolders);
 
     filterChoiceHolders = Arrays.asList(theChoiceHolders);
     filterLine = aFilterLine;
 
     resetFilter();
   }
 
   /**
    * @param collection
    * @return
    */
   // TODO JavaDoc!
   // TODO Test me!
   public Collection filter(Collection collection) {
     getFilterManager().resetExpressions();
 
     for (FilterRow row : filterRows) {
       FilterExpression fe = row.asExpression();
       if (fe != null) {
         LOG.info("Adding FilterExpression '" + fe + "' to filterManager.");
         getFilterManager().addExpression(fe);
       } else {
         LOG.info("FilterRow  '" + row + "' is not a valid FilterExpression.");
       }
     }
 
     LOG.info("FilterManager contains " + getFilterManager().getExpressions().size() + " FilterExpressions.");
     return getFilterManager().filter(collection);
   }
 
   /**
    * Looks up and assembles the {@link FilterFieldMetaData} for a given method name.
    * The given methodName needs to match the exact name of the method, e.g. <code>getFoo</code>.
    *
    * @param methodName The method name to get the meta data for.
    * @return The found meta data or null if no method has been found.
    */
   // TODO Change lookup method to expect the property name not the method name, e.g. 'foo' instead of 'getFoo'.
   // TODO Should this method throw an IllegalArgumentException instead of returning null?
   protected FilterFieldMetaData getMetaDataByString(String methodName) {
     notNull(methodName, "The methodName should not be null");
     LOG.info("Searching for method with name '" + methodName + "'");
 
     for (FilterFieldMetaData data : getFilterFieldMetaData()) {
       if (data.getTargetMethod().getName().equals(methodName)) {
        LOG.info("Method found for name '" + methodName +"'");
         return data;
       }
     }
 
     LOG.info("No method was found for name '" + methodName + "'");
     return null;
   }
 
   public List<FilterFieldMetaData> getFilterFieldMetaData() {
     return getFilterManager().getFilterFieldMetaData();
   }
 
   public List<FilterRow> getFilterRows() {
     return filterRows;
   }
 
   public List<FilterOperand> getPossibleOperands() {
     return possibleOperands;
   }
 
   /**
    * Removes the given FilterRow from the list of rows.
    * If the list is empty after removing the row, a new row will be added.
    *
    * @param row The FilterRow to remove from the list of rows.
    */
   public void removeFilterRow(FilterRow row) {
     filterRows.remove(row);
     if (filterRows.isEmpty()) {
       filterRows.add(new FilterRow());
     }
   }
 
   /**
    * Adds an empty {@link FilterRow} to the list.
    */
   public void addNewFilterRow() {
     filterRows.add(new FilterRow());
   }
 
   /**
    * Removes all <code>FilterRows</code> and adds one empty <code>FilterRow</code>.
    */
   public void resetFilter() {
     filterRows = new ArrayList<FilterRow>();
     filterRows.add(new FilterRow());
     getFilterManager().resetExpressions();
     LOG.info("Filter has been reset.");
   }
 
   protected FilterManager getFilterManager() {
     if (filterManager == null) {
       filterManager = new FilterManager<T>(filterLine, filterChoiceHolders);
     }
     return filterManager;
   }
 
   /**
    * UI Helper to decide whether to filter the objects or not.
    *
    * @return
    */
   public boolean isFilterActive() {
     return filterActive;
   }
 
   public void setFilterActive(boolean aFilterActive) {
     filterActive = aFilterActive;
     if (!filterActive) {
       resetFilter();
     }
   }
 
   public void activateFilter() {
     setFilterActive(true);
   }
 
   public void deactivateFilter() {
     setFilterActive(false);
   }
 
   public class FilterRow implements Serializable {
 
     private static final long serialVersionUID = -9079360861380466493L;
 
     private String methodName;
     private transient FilterFieldMetaData metaData;
     private FilterOperand filterOperand;
 
     private Object selection;
     private Date date;
     private BigDecimal bigDecimal;
 
     public FilterOperand getFilterOperand() {
       return filterOperand;
     }
 
     public FilterExpression asExpression() {
       if (isValid()) {
         return new FilterExpression(getMetaData(), filterOperand, getValue());
       }
       return null;
     }
 
     private Comparable getValue() {
       switch (metaData.getType()) {
         case BIGDECIMAL:
           return bigDecimal;
         case DATE:
           return date;
         case SELECT:
           return (Comparable) selection;
         default:
           throw new IllegalArgumentException("Type not supported: " + getMetaData().getType());
       }
     }
 
     /**
      * Checks whether this {@link FilterRow} is a valid and thus fulfills the requirements to be converted into a {@link FilterExpression}.
      *
      * @return Returns <code>true</code> if the object is valid, otherwise <code>false</code>
      */
     public boolean isValid() {
       if (getMetaData() == null) {
         return false;
       }
 
       if (filterOperand == null) {
         return false;
       }
 
       return getValue() != null;
     }
 
     public Collection getChoiceValues() {
       if (getMetaData() instanceof FilterChoiceFieldMetaData) {
         for (Object o : filterChoiceHolders) {
           try {
             return (Collection) ((FilterChoiceFieldMetaData) getMetaData()).getChoiceValues(o);
           } catch (RuntimeException e) {
             // expected.
           }
         }
       }
       return new ArrayList<Object>();
     }
 
     protected Object getObjectByString(String s) {
       for (Object o : getChoiceValues()) {
         if (o.toString().equals(s)) {
           return o;
         }
       }
       return null;
     }
 
     public void setFilterOperand(FilterOperand aFilterOperand) {
       filterOperand = aFilterOperand;
     }
 
     public void setStringFilterOperand(String aFilterOperand) {
 
       if (StringUtils.isEmpty(aFilterOperand)) {
         aFilterOperand = "EQ";
       }
 
       setFilterOperand(FilterOperand.valueOf(aFilterOperand));
     }
 
     public String getStringFilterOperand() {
       if (filterOperand == null) {
         return "";
       }
       return filterOperand.toString();
     }
 
     public String getSelection() {
       if (selection != null) {
         return selection.toString();
       }
       return null;
     }
 
     public void setSelection(String aSelection) {
       selection = getObjectByString(aSelection);
     }
 
     public BigDecimal getBigDecimal() {
       return bigDecimal;
     }
 
     public void setBigDecimal(BigDecimal aBigDecimal) {
       bigDecimal = aBigDecimal;
     }
 
     public String getMethodName() {
       if (getMetaData() != null) {
         return getMetaData().getTargetMethod().getName();
       }
       return null;
     }
 
     public void setMethodName(String aMethodName) {
       methodName = aMethodName;
       selection = null;
       date = null;
       bigDecimal = null;
       metaData = null;
       filterOperand = null;
     }
 
     public FilterFieldType getFilterFieldType() {
       if (getMetaData() != null) {
         return getMetaData().getType();
       }
       return null;
     }
 
     public void setFilterFieldType(FilterFieldType type) {
       // just a dummy. Needed for inputHidden field.
     }
 
     public Date getDate() {
      return new Date(date.getTime());
     }
 
     public void setDate(final Date aDate) {
       date = aDate;
     }
 
     protected FilterFieldMetaData getMetaData() {
       if (metaData == null && methodName != null) {
         metaData = getMetaDataByString(methodName);
 
         if (metaData != null && metaData instanceof FilterChoiceFieldMetaData) {
           filterOperand = FilterOperand.EQ;
         }
       }
       return metaData;
     }
   }
 
 }
