 // @formatter:off
 /*******************************************************************************
  * This file is part of JMad. Copyright (c) 2008-2011, CERN. All rights reserved. Licensed under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
  * of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
  * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 // @formatter:on
 
 package cern.accsoft.steering.jmad.domain.result.tfs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import cern.accsoft.steering.jmad.JMadConstants;
 import cern.accsoft.steering.jmad.domain.result.ResultType;
 import cern.accsoft.steering.jmad.domain.var.MadxVariable;
 import cern.accsoft.steering.jmad.domain.var.enums.MadxTwissVariable;
 import cern.accsoft.steering.jmad.util.MadxVarType;
 
 public class TfsResultImpl implements TfsResult {
 
     /** the class logger */
     private static final Logger LOGGER = Logger.getLogger(TfsResultImpl.class);
 
     /** The representation of the summary table for this result */
     private TfsSummary tfsSummary;
 
     /**
      * If the result includes the elementnames, then we store the index of the element-names for fast access.
      */
     private final Map<String, Integer> elementIndizes = new HashMap<String, Integer>();
 
     /** here the Lists of values are stored. */
     private final Map<String, List<String>> valueLists = new LinkedHashMap<String, List<String>>();
 
     /** also the double values are stored to improve performance */
     private final Map<String, List<Double>> doubleValueLists = new LinkedHashMap<String, List<Double>>();
 
     /**
      * In order to be able to work with simple array-list we additionly store the keys seperately:
      */
     private final List<String> keys = new ArrayList<String>();
 
     /** The types of the variables (columns in the file) */
     private final Map<String, MadxVarType> varTypes = new HashMap<String, MadxVarType>();
 
     //
     // public methods needed for filling the result
     //
 
     /**
      * clears all internal variables.
      */
     public void clear() {
         elementIndizes.clear();
         varTypes.clear();
         valueLists.clear();
         doubleValueLists.clear();
         keys.clear();
     }
 
     /**
      * adds an empty column to the internal data.
      * 
      * @param key the key for this column.
      */
     public void createColumn(String key) {
         keys.add(unifyKey(key));
         valueLists.put(unifyKey(key), new ArrayList<String>());
     }
 
     /**
      * sets the Type of the column corresponding to the given key
      * 
      * @param key the key for which to set the type
      * @param varType the type for the key
      */
     public void setVarType(String key, MadxVarType varType) {
         varTypes.put(unifyKey(key), varType);
     }
 
     /**
      * adds a new line of data to the dataset.
      * 
      * @param values the values to set as Strings.
      */
     public void addRow(List<String> values) {
         int columnNumber = 0;
         Collection<List<String>> lists = valueLists.values();
         for (List<String> list : lists) {
             list.add(values.get(columnNumber));
 
             // if this is the column with element-name, then store the index
             if (unifyKey(MadxTwissVariable.NAME.getMadxName()).equalsIgnoreCase(keys.get(columnNumber))) {
                 elementIndizes.put(values.get(columnNumber).toLowerCase(), list.size() - 1);
             }
             columnNumber++;
         }
     }
 
     /**
      * converts the data to Double and returns as ArrayList.
      * 
      * @param key the key for which to get the data
      * @return the ArrayList with values for one key (column in file)
      * @throws TfsResultException if the calculation of the data fails
      */
     private List<Double> calcDoubleData(String key) throws TfsResultException {
         ArrayList<Double> doubleList = new ArrayList<Double>();
         List<String> valueList = valueLists.get(key);
 
         if (!valueLists.containsKey(key)) {
             LOGGER.error("No Data for key '" + key + "'");
             return null;
         }
 
         if (!getVarType(key).equals(MadxVarType.DOUBLE)) {
             LOGGER.error("Data for key '" + key + "' is not of type Double!");
             return null;
         }
 
         for (String value : valueList) {
             try {
                 doubleList.add(Double.parseDouble(value));
             } catch (NumberFormatException e) {
                 throw new TfsResultException("Error while converting value '" + value + "' to Double", e);
             }
         }
 
         return doubleList;
     }
 
     /**
      * Validates, if all values can be correctly retrieved. If not, it throws an exception.
      * 
      * @throws TfsResultException if the verification fails.
      */
     public void verify() throws TfsResultException {
         for (String key : keys) {
             MadxVarType varType = getVarType(key);
             if (varType == null) {
                 throw new TfsResultException("Could not determine vartype vor key " + key.toString()
                         + ". Dont know how to verify!");
             }
             if (varType == MadxVarType.STRING) {
                 if (getStringData(key) == null) {
                     throw new TfsResultException("Some String-data is null!");
                 }
             } else if (varType == MadxVarType.DOUBLE) {
                 if (getDoubleData(key) == null) {
                     throw new TfsResultException("Some Double-data is null!");
                 }
             } else {
                 throw new TfsResultException("Result contains Data of type " + varType.toString()
                         + ". Dont know how to verify!");
             }
         }
     }

     /**
      * Converts all double data for fast retrieval.
      * 
      * @throws TfsResultException if the conversion from string to double values is not possible
      */
     public void convert() throws TfsResultException {
         doubleValueLists.clear();
         for (String key : keys) {
             MadxVarType varType = getVarType(key);
             if (MadxVarType.DOUBLE.equals(varType)) {
                 try {
                     List<Double> doubleValues = calcDoubleData(key);
                     doubleValueLists.put(key, doubleValues);
                 } catch (TfsResultException e) {
                     throw new TfsResultException("Double-data cannot be calculated!", e);
                 }
             }
         }
     }
 
     /**
      * ensures that all keys are the same case.
      * 
      * @param key the key to unify
      * @return the converted key.
      */
     private static final String unifyKey(String key) {
         return key.toUpperCase(JMadConstants.DEFAULT_LOCALE);
     }
 
     //
     // methods of interface TfsResult
     //
 
     @Override
     public int getColumnCount() {
         return valueLists.size();
     }
 
     @Override
     public Integer getElementIndex(String elementName) {
         String key = elementName.toLowerCase();
         if (!elementIndizes.containsKey(key)) {
             LOGGER.warn("Result does not seem to contain values for element '" + elementName + "'");
         }
         return elementIndizes.get(key);
     }
 
     @Override
     public List<Double> getDoubleData(MadxVariable resultVariable) {
         return getDoubleData(resultVariable.getMadxName());
     }
 
     @Override
     public List<Double> getDoubleData(String key) {
        return this.createImmutableList(doubleValueLists.get(unifyKey(key)));
     }
 
     @Override
     public List<String> getStringData(MadxVariable resultVariable) {
         return getStringData(resultVariable.getMadxName());
     }
 
     @Override
     public List<String> getStringData(String key) {
        return this.createImmutableList(valueLists.get(unifyKey(key)));
    }

    /**
     * make the input list unmodifiable. checks if the input list is <code>null</code> and returns <code>null</code> in
     * this case.
     * 
     * @param <T> the generic type of the list elements
     * @param input the input list
     * @return the immutable list or <code>null</code> if input was <code>null</code>
     */
    private <T> List<T> createImmutableList(List<T> input) {
        if (input == null) {
            return null;
        } else {
            return Collections.unmodifiableList(input);
        }
     }
 
     @Override
     public List<String> getKeys() {
         return keys;
     }
 
     @Override
     public MadxVarType getVarType(String key) {
         return varTypes.get(unifyKey(key));
     }
 
     @Override
     public MadxVarType getVarType(MadxVariable var) {
         return getVarType(var.getMadxName());
     }
 
     //
     // Methods of interface Result
     //
 
     @Override
     public final ResultType getResultType() {
         return ResultType.TFS_RESULT;
     }
 
     @Override
     public TfsSummary getSummary() {
         return this.tfsSummary;
     }
 
     public void setTfsSummary(TfsSummary tfsSummary) {
         this.tfsSummary = tfsSummary;
     }
 }
