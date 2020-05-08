 package com.lemondo.commons.db;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
public class TableMetaData implements ModelMetaData<Map<String, Object>> {
 
 	private String tableName;
 	private Map<String, Integer> columnDef;
 	private boolean deactivatedFlag;
 
 	public TableMetaData(String tableName, Map<String, Integer> columnDef, boolean deactivatedFlag) {
 		super();
 		this.tableName = tableName;
 		this.columnDef = columnDef;
 		this.deactivatedFlag = deactivatedFlag;
 	}
 
 	public Map<String, Integer> getColumnDef() {
 		return new HashMap<String, Integer>(this.columnDef);
 	}
 
 	@Override
 	public String genInsertSql(Set<String> columns) {
 		StringBuilder insertClause = new StringBuilder("INSERT INTO ").append(tableName).append(" (`id`");
 		StringBuilder valuesClause = new StringBuilder(" VALUES").append(" (?");
 
 		for (String col : columns) {
 			if (columnDef.containsKey(col)) {
 				insertClause.append(",`").append(col).append("`");
 				valuesClause.append(",?");
 			} else {
 				throw new RuntimeException("BOOM: invalid field!");
 			}
 		}
 
 		return insertClause.append(")").append(valuesClause).append(")").toString();
 	}
 
 	@Override
 	public String genUpdateSql(Set<String> columns) {
 		StringBuilder updateSql = new StringBuilder("UPDATE ").append(tableName).append(" SET");
 
 		String prefix = " `";
 		for (String col : columns) {
 			if (columnDef.containsKey(col)) {
 				updateSql.append(prefix).append(col).append("`=?");
 				prefix = ",`";
 			} else {
 				throw new RuntimeException("BOOM: invalid field!");
 			}
 		}
 
 		return updateSql.append(" WHERE `id`=?").append(deactivatedFlag ? " AND `deactivated`=0" : "").toString();
 	}
 
 	@Override
 	public String genDeleteSql() {
 		StringBuilder deleteSql = new StringBuilder();
 
 		if (deactivatedFlag) {
 			deleteSql.append("UPDATE ").append(tableName).append(" SET `deactivated`=1 WHERE `deactivated`=0 AND `id`=?");
 		} else {
 			deleteSql.append("DELETE FROM ").append(tableName).append(" WHERE `id`=?");
 		}
 
 		return deleteSql.toString();
 	}
 
 	private String genFilterString(Set<FilterCondition> filter) {
 		StringBuilder result = new StringBuilder();
 
 		String prefix = "";
 		for (FilterCondition condition : filter) {
 			result.append(prefix).append("`").append(condition.columnName).append("`").append(condition.operator).append("?");
 			prefix = " AND ";
 		}
 
 		return result.toString();
 	}
 
 	private String genOrderByString(List<String> sortFields) {
 		if (sortFields == null) {
 			throw new RuntimeException("BOOM!");
 		} else {
 			StringBuilder result = new StringBuilder();
 
 			String prefix = " ORDER BY ";
 			for (int i = 0; i < sortFields.size(); i++) {
 				result.append(prefix).append(sortFields.get(i));
 				prefix = ",";
 			}
 
 			return result.toString();
 		}
 	}
 
 	@Override
 	public String genSelectSql(boolean allRows, Set<FilterCondition> filter, List<String> sortFields) {
 		StringBuilder selectSql = new StringBuilder("SELECT `id`");
 
 		Set<String> columns = columnDef.keySet();
 		for (String column : columns) {
 			selectSql.append(",`").append(column).append("`");
 		}
 
 		selectSql.append(" FROM ").append(tableName);
 
 		String filterPrefix = " WHERE ";
 		if (deactivatedFlag) {
 			selectSql.append(filterPrefix).append("`deactivated`=0");
 			filterPrefix = " AND ";
 		}
 		if (!allRows) {
 			selectSql.append(filterPrefix).append("`id`=?");
 			filterPrefix = " AND ";
 		}
 		if (filter != null) {
 			selectSql.append(filterPrefix).append(genFilterString(filter));
 		}
 		if (allRows && sortFields != null) {
 			selectSql.append(genOrderByString(sortFields));
 		}
 		// TODO Add paging here
 
 		return selectSql.toString();
 	}
 
 }
