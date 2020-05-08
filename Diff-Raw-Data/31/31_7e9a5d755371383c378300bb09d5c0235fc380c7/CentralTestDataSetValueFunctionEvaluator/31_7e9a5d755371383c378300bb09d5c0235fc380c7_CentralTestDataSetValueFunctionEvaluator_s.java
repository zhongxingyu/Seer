 /*******************************************************************************
  * Copyright (c) 2004, 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 
 package org.eclipse.jubula.client.core.functions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.jubula.client.core.businessprocess.TestDataCubeBP;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.businessprocess.TestExecutionEvent;
import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.ITDManager;
 import org.eclipse.jubula.client.core.model.ITestDataCubePO;
 import org.eclipse.jubula.client.core.model.ITestDataPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.utils.ExecObject;
 import org.eclipse.jubula.client.core.utils.GuiParamValueConverter;
 import org.eclipse.jubula.client.core.utils.NullValidator;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 /**
  * This function returns the String value of a cell of a central central test
  * data set. Therefore you need to define an unique key for this central test
  * data set. You can get the cell value using the parameters DATA_SET_NAME,
  * ENTRY_KEY, COLUMN_NAME.
  * 
  * @author BREDEX GmbH
  */
 public class CentralTestDataSetValueFunctionEvaluator 
     extends AbstractFunctionEvaluator {
     /**
      * a map of the data cubes
      */
     private static final Map<String, ITestDataCubePO> DATA_CUBES = 
             new HashMap<String, ITestDataCubePO>();
     /**
      * the parameter descriptions
      */
     private static final Map<ITestDataCubePO, 
         Map<String, IParamDescriptionPO>> PARAM_DESCRIPTIONS = 
             new HashMap<ITestDataCubePO, Map<String, IParamDescriptionPO>>();
     /**
      * the unique key map
      */
     private static final Map<IParamDescriptionPO, 
         Map<String, Integer>> UNIQUE_KEYS = 
             new HashMap<IParamDescriptionPO, Map<String, Integer>>();
 
     /**
      * Add listener that refresh the caching of central test data sets every time the test
      * execution starts
      */
     static {
        DataEventDispatcher.getInstance().addTestSuiteStateListener(
                new DataEventDispatcher.ITestSuiteStateListener() {
                    public void handleTSStateChanged(TestExecutionEvent event) {
                        if (event.getState() == TestExecutionEvent
                                .TEST_EXEC_RESULT_TREE_READY) {
                            registerDataCubes();
                            PARAM_DESCRIPTIONS.clear();
                            UNIQUE_KEYS.clear();
                        }
                    }
                }, true);
     }
 
     /**
      * Register all central test data sets that are available and create a cache with the
      * name of the central test data set as key
      */
     private static void registerDataCubes() {
         DATA_CUBES.clear();
         ITestDataCubePO[] allTestDataCubesFor = TestDataCubeBP
                 .getAllTestDataCubesFor(GeneralStorage.getInstance()
                         .getProject());
         for (ITestDataCubePO testDataCubePO : allTestDataCubesFor) {
             DATA_CUBES.put(testDataCubePO.getName(), testDataCubePO);
         }
     }
 
     /**
      * Register all columns in a central test data set and create a cache with the name of
      * the column as key
      * 
      * @param dataSet
      *            the data table, that has different columns
      * @return a map with all columns for this central test data sets
      */
     private static Map<String, IParamDescriptionPO> registerParamDescription(
             ITestDataCubePO dataSet) {
 
         Map<String, IParamDescriptionPO> paramDescriptionsForOneDataSet = 
                 new HashMap<String, IParamDescriptionPO>();
         for (IParamDescriptionPO description : dataSet.getParameterList()) {
             paramDescriptionsForOneDataSet.put(description.getName(),
                     description);
         }
         PARAM_DESCRIPTIONS.put(dataSet, paramDescriptionsForOneDataSet);
         return paramDescriptionsForOneDataSet;
     }
 
     /**
      * Register all key values for a key column of a specific central test data set.
      * 
      * @param dataSet
      *            central test data set that contains keys and columns
      * @param keyColumn
      *            Column where the key is stored
      * @param lang
      *            language to get the key value
      * @return a map specific for the column containing the row number for a
      *         given key
      * @throws InvalidDataException
      */
     private static Map<String, Integer> registerUniqueKeyMap(
             ITestDataCubePO dataSet, IParamDescriptionPO keyColumn, Locale lang)
         throws InvalidDataException {
         Map<String, Integer> keyMap;
         keyMap = new HashMap<String, Integer>();
         ITDManager dataManager = dataSet.getDataManager();
         for (int i = 0; i < dataManager.getDataSetCount(); i++) {
             ITestDataPO cell = dataManager.getCell(i, keyColumn);
             String cellValue = cell.getValue(lang);
             if (keyMap.get(cellValue) != null) {
                 throw new InvalidDataException("The key '" + cellValue //$NON-NLS-1$
                         + "' for column '" + keyColumn.getName() //$NON-NLS-1$
                         + "' is not unique in central test data set '" + dataSet.getName() //$NON-NLS-1$
                         + "'!", MessageIDs.E_FUNCTION_EVAL_ERROR); //$NON-NLS-1$
             }
             keyMap.put(cellValue, i);
         }
         UNIQUE_KEYS.put(keyColumn, keyMap);
         return keyMap;
     }
 
     /** {@inheritDoc} */
     public String evaluate(String[] arguments) throws InvalidDataException {
         validateParamCount(arguments, 4);
         ITestDataCubePO dataSet = validateDataSetName(arguments[0]);
         Locale lang = TestExecution.getInstance().getLocale();
         IParamDescriptionPO keyColumn = validateColumnName(dataSet,
                 arguments[1]);
         int entryKey = validateEntryKey(dataSet, keyColumn, arguments[2], lang);
         IParamDescriptionPO column = validateColumnName(dataSet, arguments[3]);
         return getDataSetValue(dataSet, entryKey, column, lang);
     }
 
     /**
      * return the value of a central test data set cell identified by the given parameters
      * 
      * @param dataSet
      *            where the value is stored
      * @param row
      *            number of the given row
      * @param column
      *            descriptor for the column
      * @param lang
      *            language to get the key value
      * @return value as a string, if it is another function it would be
      *         validated too
      * @throws InvalidDataException
      */
     private static String getDataSetValue(ITestDataCubePO dataSet, int row,
             IParamDescriptionPO column, Locale lang)
         throws InvalidDataException {
         String dataSetValue = dataSet.getDataManager().getCell(row, column)
                 .getValue(lang);
         return new GuiParamValueConverter(dataSetValue, dataSet, lang, null,
                 new NullValidator()).getExecutionString(
                 new ArrayList<ExecObject>(), lang);
     }
 
     /**
      * Validate and return descriptor of a central test data set column. Cache will be used
      * if available. After resolving any key from the given central test data set, all
      * columns in this set should be cached.
      * 
      * @param dataSet
      *            that contains value
      * @param columnName
      *            unique name of the column in this central test data set
      * @return descriptor for this column
      * @throws InvalidDataException
      */
     private static IParamDescriptionPO validateColumnName(
             ITestDataCubePO dataSet, String columnName)
         throws InvalidDataException {
         Map<String, IParamDescriptionPO> map = PARAM_DESCRIPTIONS
                 .get(dataSet);
         if (map == null) {
             map = registerParamDescription(dataSet);
         }
         IParamDescriptionPO column = map.get(columnName);
         if (column != null) {
             return column;
         }
         throw new InvalidDataException("Column '" + columnName  //$NON-NLS-1$
                 + "' is not available in central test data set '" + dataSet.getName() + "'!", //$NON-NLS-1$ //$NON-NLS-2$
                 MessageIDs.E_FUNCTION_EVAL_ERROR);
     }
 
     /**
      * Validates if a given key is available in a central test data set and returns the row
      * number. This method uses keys from a cache if available. If not the key
      * will be load and put to cache.
      * 
      * @param dataSet
      *            that contains key
      * @param keyColumn
      *            that contains key
      * @param key
      *            unique key
      * @param lang
      *            language for key value
      * @return row number
      * @throws InvalidDataException
      */
     private static int validateEntryKey(ITestDataCubePO dataSet,
             IParamDescriptionPO keyColumn, String key, Locale lang)
         throws InvalidDataException {
         Map<String, Integer> keyMap = UNIQUE_KEYS.get(keyColumn);
         if (keyMap == null) {
             keyMap = registerUniqueKeyMap(dataSet, keyColumn, lang);
         }
         Integer row = keyMap.get(key);
         if (row != null) {
             return row;
         }
         throw new InvalidDataException("Key '" + key //$NON-NLS-1$
                 + "' is not available in column '" + keyColumn.getName() //$NON-NLS-1$
                 + "' in central test data set '" //$NON-NLS-1$ 
                 + dataSet.getName() + "'!", //$NON-NLS-1$
                 MessageIDs.E_FUNCTION_EVAL_ERROR);
     }
 
     /**
      * Returns a given central test data set using the cache if central test data set exists
      * 
      * @param dataSetName
      *            unique name of the central test data set
      * @return central test data set with the given name
      * @throws InvalidDataException
      */
     private static ITestDataCubePO validateDataSetName(String dataSetName)
         throws InvalidDataException {
         ITestDataCubePO dataCube = DATA_CUBES.get(dataSetName);
         if (dataCube != null) {
             return dataCube;
         }
 
         throw new InvalidDataException("Central test data set '" + dataSetName //$NON-NLS-1$
                 + "' is not available!", MessageIDs.E_FUNCTION_EVAL_ERROR); //$NON-NLS-1$
     }
 
 }
