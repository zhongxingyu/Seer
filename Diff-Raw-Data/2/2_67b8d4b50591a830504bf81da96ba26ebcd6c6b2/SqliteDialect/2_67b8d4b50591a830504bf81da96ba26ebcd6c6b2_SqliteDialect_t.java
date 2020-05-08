 package com.tkym.labs.record;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.tkym.labs.record.RecordstoreDialect.AbstractDatastoreDialect;
 import com.tkym.labs.record.TableMeta.ColumnMeta;
 import com.tkym.labs.record.TableMeta.ColumnMetaType;
 import com.tkym.labs.record.TableMeta.IndexMeta;
 
 
 /**
  * Sql String Generator
  * @author kazunari
  */
 class SqliteDialect extends AbstractDatastoreDialect{
 	private enum SqliteColumnType{
 		INTEGER,
 		TEXT,
 		REAL,
 		BLOB,
 		NULL
 	}
 	
 	protected Map<ColumnMetaType<?>, SqliteColumnType> typeMap = 
 			new HashMap<ColumnMetaType<?>, SqliteColumnType>();
 	
 	SqliteDialect(){
 		typeMap.put(ColumnMeta.INTEGER, SqliteColumnType.INTEGER);
 		typeMap.put(ColumnMeta.LONG,    SqliteColumnType.INTEGER);
 		typeMap.put(ColumnMeta.SHORT,   SqliteColumnType.INTEGER);
 		typeMap.put(ColumnMeta.BYTE, 	SqliteColumnType.INTEGER);
 		typeMap.put(ColumnMeta.DOUBLE,  SqliteColumnType.REAL);
 		typeMap.put(ColumnMeta.FLOAT,   SqliteColumnType.REAL);
 		typeMap.put(ColumnMeta.STRING,  SqliteColumnType.TEXT);
 		typeMap.put(ColumnMeta.DATE,    SqliteColumnType.INTEGER);
 		typeMap.put(ColumnMeta.BOOLEAN, SqliteColumnType.NULL);
 	}
 	
 	@Override
 	public String[] createCreateIndexStatements(TableMeta meta) {
 		String[] ret = new String[meta.indexes().length];
 		for (int i=0; i<ret.length; i++)
 			ret[i] = makeCreateIndexStatement(meta.tableName(), meta.indexes()[i]);
 		return ret;
 	}
 	
 	private String makeCreateIndexStatement(String tablename, IndexMeta index){
 		StringBuilder sb = new StringBuilder();
 		sb.append("create ");
 		if (index.isUnique())
 			sb.append("unique ");
 		sb.append("index ");
 		sb.append(index.getName()+" ");
 		sb.append("on ");
 		sb.append(tablename+" ");
 		sb.append("(");
 		boolean first = true;
 		for (ColumnMeta<?> col : index.columns()){
 			if (first) first = false;
 			else sb.append(", ");
 			sb.append(col.getName());
 		}
 		sb.append(")");
 		return sb.toString();
 	}
 	
 	@Override
 	public String createCreateStatement(TableMeta tableMeta){
 		StringBuilder sb = new StringBuilder();
 		sb.append("create table ");
 		sb.append(tableMeta.tableName());
 		sb.append(" (");
 		boolean first = true;
		for(ColumnMeta<?> column : tableMeta.allColumn()){
 			if(first) first = false;
 			else sb.append(", ");
 			sb.append(column.getName());
 			if(column.getType() != null)
 				if(typeMap.containsKey(column.getType()))
 					sb.append(" "+typeMap.get(column.getType()).toString());
 			if(tableMeta.isKey(column))
 				sb.append(" primarykey");
 		}
 		sb.append(")");
 		return sb.toString();
 	}
 }
