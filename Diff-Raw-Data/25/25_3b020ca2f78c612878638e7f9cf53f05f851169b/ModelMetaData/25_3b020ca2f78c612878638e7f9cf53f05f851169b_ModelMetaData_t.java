 package com.lemondo.commons.db;
 
 import java.util.List;
 import java.util.Set;
 
public interface ModelMetaData {
 
 	public String genInsertSql(Set<String> columns);
 
 	public String genUpdateSql(Set<String> columns);
 
 	public String genDeleteSql();
 
 	public String genSelectSql(boolean allRows, Set<FilterCondition> filter, List<String> sortFields);
 
 }
