 /**
  * Copyright (C) 2009 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.connectors.sap.bos5connector;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sap.mw.jco.JCO.Client;
 import com.sap.mw.jco.JCO.Function;
 import com.sap.mw.jco.JCO.ParameterList;
 import com.sap.mw.jco.JCO.Repository;
 import com.sap.mw.jco.JCO.Structure;
 import com.sap.mw.jco.JCO.Table;
 
 /**
  * @author Charles Souillard
  */
 public class SAPCallFunctionConnector extends SAPAbstractConnector {
 
 	private static final int EXPECTED_INPUT_COLUMN_NUMBER = 4;
 	private static final List<String> INPUT_PARAMETER_TYPES = new ArrayList<String>();
 	public static final String TABLE_INPUT = "table_input";
 	public static final String INPUT_TABLE = "input_table";
 	public static final String INPUT_STRUCTURE = "input_structure";
 	public static final String INPUT_SINGLE = "input_single";
 
 	private static final int EXPECTED_OUTPUT_COLUMN_NUMBER = 4;
 	private static final List<String> OUTPUT_PARAMETER_TYPES = new ArrayList<String>();
 	public static final String TABLE_OUTPUT = "table_output";
 	public static final String OUTPUT_TABLE = "output_table";
 	public static final String OUTPUT_STRUCTURE = "output_structure";
 	public static final String OUTPUT_SINGLE = "output_single";
 
 	static {
 		INPUT_PARAMETER_TYPES.add(TABLE_INPUT);
 		INPUT_PARAMETER_TYPES.add(INPUT_TABLE);
 		INPUT_PARAMETER_TYPES.add(INPUT_STRUCTURE);
 		INPUT_PARAMETER_TYPES.add(INPUT_SINGLE);
 
 		OUTPUT_PARAMETER_TYPES.add(TABLE_OUTPUT);
 		OUTPUT_PARAMETER_TYPES.add(OUTPUT_TABLE);
 		OUTPUT_PARAMETER_TYPES.add(OUTPUT_STRUCTURE);
 		OUTPUT_PARAMETER_TYPES.add(OUTPUT_SINGLE);
 	}
 
 	// CONNECTOR INPUT PARAMETERS
 	protected String functionName;
 	protected boolean commitOnSuccess;
 	protected boolean rollbackOnFailure;
 	protected String htmlOutput;
 
 	// columns in the map must be in the following order:
 	// parameterType
 	// tableName
 	// parameterName
 	// parameterValue
 	protected List<List<Object>> inputParameters;
 
 	// columns in the map must be in the following order:
 	// parameterType
 	// tableName
 	// XPath
 	// variableName
 	protected List<List<String>> outputParameters;
 
 	// CONNECTOR OUTPUT PARAMETERS
 	protected List<Serializable> results = new ArrayList<Serializable>();
 
 	@SuppressWarnings("unchecked")
 	public void executeConnector() throws Exception {
 		final Client jcoClient = getJcoClient();
 		final Repository jcoRepository = createRepository(jcoClient);
 		final Function jcoFunction = createFunction(jcoRepository, functionName);
 
 		final ParameterList tableParameterList = jcoFunction
 				.getTableParameterList();
 
 		if (inputParameters != null) {
 			final ParameterList inputParameterList = jcoFunction
 					.getImportParameterList();
 			for (List<Object> column : inputParameters) {
 				final String parameterType = (String) column.get(0);
 				final String tableName = (String) column.get(1);
 				final String parameterName = (String) column.get(2);
 				final Object parameterValue = column.get(3);
 
 				if (INPUT_STRUCTURE.equals(parameterType)) {
 					final Structure structure = inputParameterList
 							.getStructure(tableName);
 					structure.setValue(parameterValue, parameterName);
 				} else if (INPUT_SINGLE.equals(parameterType)) {
 					inputParameterList.setValue(parameterValue, parameterName);
 				} else {
 					// INPUT_TABLE or TABLE_INPUT
 					List<Object> list = new ArrayList<Object>();
 					if (parameterValue instanceof List<?>) {
 						list.addAll((List<Object>) parameterValue);
 					} else {
 						list.add(parameterValue);
 					}
					int rowSize = 0;
					rowSize = list.size();
 					Table table = null;
 					if (INPUT_TABLE.equals(parameterType)) {
 						table = inputParameterList.getTable(tableName);
 					} else if (TABLE_INPUT.equals(parameterType)) {
 
 						table = tableParameterList.getTable(tableName);
 					}
 
 					table.firstRow();
 					for (int rowNb = 0; rowNb < rowSize; rowNb++) {
 						if (table.getNumRows() < (rowNb + 1))
 							table.appendRow();
 						table.setValue(list.get(rowNb), parameterName);
 						table.nextRow();
 					}
 				}
 			}
 		}
 
 		try {
 			// execute the function
 			executeFunction(jcoClient, jcoFunction);
 			if (isNotNull(htmlOutput)) {
 				jcoFunction.writeHTML(htmlOutput);
 			}
 
 			// manage output
 			if (outputParameters != null) {
 				final ParameterList outputParameterList = jcoFunction
 						.getExportParameterList();
 				for (List<String> row : outputParameters) {
					final String parameterType = (String) row.get(0);
					final String tableName = (String) row.get(1);
					final String xpath = (String) row.get(2);
					// final String variableName = row.get(3);
 
					Serializable variableValue = null;
 					if (OUTPUT_STRUCTURE.equals(parameterType)) {
 						final Structure structure = outputParameterList
 								.getStructure(tableName);
 						variableValue = structure.getString(xpath);
 					} else if (OUTPUT_SINGLE.equals(parameterType)) {
 						variableValue = outputParameterList.getString(xpath);
 					} else {
 						// OUTPUT_TABLE or TABLE_OUTPUT
 						Table table = null;
 						if (OUTPUT_TABLE.equals(parameterType)) {
 							table = outputParameterList.getTable(tableName);
 						} else if (TABLE_OUTPUT.equals(parameterType)) {
 							table = tableParameterList.getTable(tableName);
 						}
 						ArrayList<String> list = new ArrayList<String>();
 						boolean hasMoreElements = !table.isEmpty();
 						while (hasMoreElements) {
 							list.add(table.getString(xpath));
 							if (table.isLastRow()) {
 								hasMoreElements = false;
 							} else {
 								table.nextRow();
 							}
 						}
 						if (list.size() == 0) {
 							variableValue = null;
 						} else if (list.size() == 1) {
 							variableValue = list.get(0);
 						} else {
 							variableValue = list;
 						}
 
 					}
 					this.results.add(variableValue);
 				}
 			}
 
 			if (commitOnSuccess) {
 				final Function commitFunction = createFunction(jcoRepository,
 						SAPConstants.BAPI_TRANSACTION_COMMIT);
 				executeFunction(jcoClient, commitFunction);
 			}
 		} catch (Throwable e) {
 			if (rollbackOnFailure) {
 				final Function rollbackFunction = createFunction(jcoRepository,
 						SAPConstants.BAPI_TRANSACTION_ROLLBACK);
 				executeFunction(jcoClient, rollbackFunction);
 			}
 		}
 
 		// release client
		releaseClient(jcoClient);
 	}
 
 	public List<String> validateValues() {
 		final List<String> errors = new ArrayList<String>();
 		errors.addAll(super.validateValues());
 
 		// check INPUT_PARAMETER_TYPES
 		if (inputParameters != null) {
 			int inputRowNb = 1;
 			for (List<Object> row : inputParameters) {
 				if (row.size() != EXPECTED_INPUT_COLUMN_NUMBER) {
                     errors.add(
                     "Row number: " + inputRowNb + " has a wrong number of columns: " + row.size() + " (instead of " + EXPECTED_INPUT_COLUMN_NUMBER + ").");
 				}
 				if (!INPUT_PARAMETER_TYPES.contains(row.get(0))) {
 					errors.add(
                     "Row number: " + inputRowNb + " has a wrong value for parameterType: " + row.get(0) + " (instead of " + INPUT_PARAMETER_TYPES + ").");
 				}
 				inputRowNb++;
 			}
 		}
 		// check OUTPUT_PARAMETER_TYPES
 		if (outputParameters != null) {
 			int outputRowNb = 1;
 			for (List<String> row : outputParameters) {
 				if (row.size() != EXPECTED_OUTPUT_COLUMN_NUMBER) {
 					errors.add(
                             "Row number: " + outputRowNb + " has a wrong number of columns: " + row.size() + " (instead of " + EXPECTED_OUTPUT_COLUMN_NUMBER + ").");
 
 				}
 				if (!OUTPUT_PARAMETER_TYPES.contains(row.get(0))) {
 					errors.add(
                             "Row number: " + outputRowNb + " has a wrong value for parameterType: " + row.get(0) + " (instead of " + OUTPUT_PARAMETER_TYPES + ").");
 				}
 				outputRowNb++;
 			}
 		}
 		return errors;
 	}
 
 	// External GETTERS
 	public List<Serializable> getResults() {
 		return results;
 	}
 
 	// SETTERS
 	public void setInputParameters(final List<List<Object>> inputParameters) {
 		this.inputParameters = inputParameters;
 	}
 
 	public void setOutputParameters(List<List<String>> outputParameters) {
 		this.outputParameters = outputParameters;
 	}
 
 	public void setFunctionName(String functionName) {
 		this.functionName = functionName;
 	}
 
 	public void setCommitOnSuccess(boolean commitOnSuccess) {
 		this.commitOnSuccess = commitOnSuccess;
 	}
 
 	public void setRollbackOnFailure(boolean rollbackOnFailure) {
 		this.rollbackOnFailure = rollbackOnFailure;
 	}
 
 	public void setHtmlOutput(String htmlOutput) {
 		this.htmlOutput = htmlOutput;
 	}
 
 }
