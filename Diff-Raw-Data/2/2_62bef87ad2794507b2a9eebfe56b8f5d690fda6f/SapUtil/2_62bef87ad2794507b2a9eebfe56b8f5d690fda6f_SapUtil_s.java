 /** 
  *    Copyright (C) 2011, Starschema Ltd. <info at starschema.net>
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 2 of the License, or
  *    any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  **/
 
 package org.talend.repository.sapwizard.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.talend.core.model.metadata.MetadataToolHelper;
 import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
 import org.talend.core.model.metadata.builder.connection.InputSAPFunctionParameterTable;
 import org.talend.core.model.metadata.builder.connection.MetadataColumn;
 import org.talend.core.model.metadata.builder.connection.MetadataTable;
 import org.talend.core.model.metadata.builder.connection.OutputSAPFunctionParameterTable;
 import org.talend.core.model.metadata.builder.connection.SAPConnection;
 import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
 import org.talend.core.model.metadata.builder.connection.SAPTestInputParameterTable;
 import org.talend.core.repository.model.ProxyRepositoryFactory;
 
 import com.sap.conn.jco.JCoDestination;
 import com.sap.conn.jco.JCoDestinationManager;
 import com.sap.conn.jco.JCoException;
 import com.sap.conn.jco.JCoFunction;
 import com.sap.conn.jco.JCoTable;
 import com.sap.conn.jco.ext.Environment;
 
 /**
  * @author Ammu
  * 
  */
 public class SapUtil {
 
 	private static SapCustomDataProvider customDataProvider;
 	private final static Map<String, String> PLSAP_DADTYPE_MAP = new HashMap<String, String>();
 	private final static Map<String, Boolean> SAP_KEY_IDENTIFIER = new HashMap<String, Boolean>();
 
 	static {
 		PLSAP_DADTYPE_MAP.put("P", "id_Double");
 		PLSAP_DADTYPE_MAP.put("C", "id_String");
 		PLSAP_DADTYPE_MAP.put("D", "id_Date");
 		PLSAP_DADTYPE_MAP.put("F", "id_Double");
 		PLSAP_DADTYPE_MAP.put("I", "id_Integer");
 		PLSAP_DADTYPE_MAP.put("b", "id_Double");
 		PLSAP_DADTYPE_MAP.put("s", "id_Double");
 		PLSAP_DADTYPE_MAP.put("N", "id_String");
 		PLSAP_DADTYPE_MAP.put("S", "id_String");
 		PLSAP_DADTYPE_MAP.put("T", "id_String");
 		PLSAP_DADTYPE_MAP.put("X", "id_String");
 
 		SAP_KEY_IDENTIFIER.put(" ", false);
 		SAP_KEY_IDENTIFIER.put("X", true);
 		SAP_KEY_IDENTIFIER.put("", false);
 	}
 
 	/**
 	 * @param client
 	 * @param language
 	 * @param sysNumber
 	 * @param host
 	 * @param userName
 	 * @param password
 	 * @return
 	 * @throws Throwable
 	 * 
 	 */
 	public static boolean connectSAPserver(String client, String language, String sysNumber, String host, String userName, String password)
 			throws Throwable {
 		registerSAPServerDetails(client, language, sysNumber, host, userName, password);
 		boolean connected = false;
 		try {
 			JCoDestination dest = JCoDestinationManager.getDestination(SapCustomDataProvider.SAP_SERVER);
 			connected = dest.getAttributes() != null;
 		} catch (Exception exception) {
 			connected = false;
 			throw exception;
 		} catch (Throwable throwable) {
 			connected = false;
 			throw throwable;
 		}
 		return connected;
 	}
 
 	private static void registerSAPServerDetails(String client, String language, String sysNumber, String host, String userName, String password) {
 		if (customDataProvider != null) {
 			Environment.unregisterDestinationDataProvider(customDataProvider);
 		}
 		customDataProvider = new SapCustomDataProvider(client, language, sysNumber, host, userName, password);
 		Environment.registerDestinationDataProvider(customDataProvider);
 	}
 
 	public static SAPFunctionUnit createFunctionForGivenTable(String tableName, SAPConnection connection) throws Exception {
 		registerSAPServerDetails(connection.getClient(), connection.getLanguage(), connection.getSystemNumber(), connection.getHost(),
 				connection.getUsername(), connection.getPassword());
 		JCoDestination destination = null;
 		try {
 			destination = JCoDestinationManager.getDestination(SapCustomDataProvider.SAP_SERVER);
 			JCoFunction function = destination.getRepository().getFunction("DDIF_FIELDINFO_GET");
 			if (function == null) {
 				throw new RuntimeException("DDIF_FIELDINFO_GET not found in SAP.");
 			}
 			function.getImportParameterList().setValue("TABNAME", tableName.trim());
 			function.getImportParameterList().setValue("LANGU", connection.getLanguage().trim().toUpperCase());
 			function.execute(destination);
 
 			return createFunctionUnit(tableName, connection, function);
 
 		} catch (JCoException e) {
 			throw e;
 		}
 	}
 
 	private static SAPFunctionUnit createFunctionUnit(String tableName, SAPConnection connection, JCoFunction function) {
 
 		SAPFunctionUnit functionUnit;
 		OutputSAPFunctionParameterTable outputParameterTable;
 		InputSAPFunctionParameterTable inputParameterTable;
 		MetadataTable metadataTable;
 		SAPTestInputParameterTable testInputParameterTable;
 
 		ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
 
 		functionUnit = ConnectionFactory.eINSTANCE.createSAPFunctionUnit();
 		functionUnit.setName(tableName);
 		functionUnit.setLabel(tableName);
 		functionUnit.setOutputType(SapParameterTypeEnum.OUTPUT_SINGLE.getDisplayLabel());
 		functionUnit.setConnection(connection);
 		functionUnit.setId(proxyRepositoryFactory.getNextId());
 
 		// New Input parameter table
 		inputParameterTable = ConnectionFactory.eINSTANCE.createInputSAPFunctionParameterTable();
 		inputParameterTable.setFunctionUnit(functionUnit);
 		inputParameterTable.setId(proxyRepositoryFactory.getNextId());
 		inputParameterTable.setLabel(functionUnit.getName());
 		// createParamsForFunction(inputParameterTable, function, 0);
 
 		// New out parameter table
 		outputParameterTable = ConnectionFactory.eINSTANCE.createOutputSAPFunctionParameterTable();
 		outputParameterTable.setFunctionUnit(functionUnit);
 		outputParameterTable.setId(proxyRepositoryFactory.getNextId());
 		outputParameterTable.setLabel(functionUnit.getName());
 
 		// New Test parameter table
 		testInputParameterTable = ConnectionFactory.eINSTANCE.createSAPTestInputParameterTable();
 		testInputParameterTable.setFunctionUnit(functionUnit);
 		testInputParameterTable.setId(proxyRepositoryFactory.getNextId());
 		testInputParameterTable.setLabel(function.getName());
 		// createParamsForFunction(testInputParameterTable, function, 0);
 
 		// New Metadata table
 		metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
 		metadataTable.setId(proxyRepositoryFactory.getNextId());
 		metadataTable.setLabel(tableName);
 		metadataTable.getColumns().addAll(getColumns(function));
 		// FIXME
 		// metadataTable.setConnection(connection);
 		functionUnit.setInputParameterTable(inputParameterTable);
 		functionUnit.setOutputParameterTable(outputParameterTable);
 		functionUnit.setMetadataTable(metadataTable);
 		functionUnit.setTestInputParameterTable(testInputParameterTable);
 		connection.getFuntions().add(functionUnit);
 		// functionUnit.getTables().add(metadataTable);
 		return functionUnit;
 	}
 
 	private static List<MetadataColumn> getColumns(JCoFunction function) {
 		JCoTable data = function.getTableParameterList().getTable("DFIES_TAB");
 		data.firstRow();
 		Set<String> columnsAlreadyAdded = new HashSet<String>();
 		List<MetadataColumn> columns = new ArrayList<MetadataColumn>();
 		for (int i = 0; i < data.getNumRows(); i++, data.nextRow()) {
 
 			final MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
 
 			metadataColumn.setLabel(MetadataToolHelper.validateColumnName(data.getString("FIELDNAME"), 0));
 			metadataColumn.setKey(SAP_KEY_IDENTIFIER.get(data.getString("KEYFLAG")));
 			metadataColumn.setTalendType(PLSAP_DADTYPE_MAP.get(data.getString("INTTYPE")));
 			metadataColumn.setSourceType("");
 			try {
 				metadataColumn.setLength(Integer.parseInt(data.getString("LENG")));
 			} catch (final NumberFormatException e) {
 				metadataColumn.setLength(0);
 			}
 			try {
 				metadataColumn.setPrecision(Integer.parseInt("-1"));
 			} catch (final NumberFormatException e) {
 				metadataColumn.setPrecision(0);
 			}
 			metadataColumn.setNullable(true);
 			metadataColumn.setDefaultValue("");
 			metadataColumn.setComment(data.getString("FIELDTEXT"));
			metadataColumn.setPattern(data.getString("INTTYPE").equals("D") ? "yyyyMMdd" : "");
 			metadataColumn.setOriginalField(data.getString("FIELDNAME"));
 
 			if (!columnsAlreadyAdded.contains(metadataColumn.getLabel())) {
 				columns.add(metadataColumn);
 				columnsAlreadyAdded.add(metadataColumn.getLabel());
 			}
 		}
 		return columns;
 	}
 
 }
