 package euphonia.core;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import euphonia.core.transformation.Transformation;
 
 public class Migration
 {
 	private static final Log log = LogFactory.getLog(Migration.class);
 	
 	private List<Table> tables = new ArrayList<Table>();
 	private Table lastTable;
 	private Field lastField;
 	
 	private boolean incremental = false;
 	
 	private String sourceDatabase, targetDatabase;
 	private DBMS sourceDBMS, targetDBMS;
 	
 	protected boolean addTable(Table table)
 	{
 		return tables.add(table);
 	}
 
 	public Table table(String name)
 	{
 		Table table = new Table(name, this);
 		lastTable = table;
 		return table;
 	}
 
 	public Field field(String name)
 	{
 		this.lastField = new Field(name, lastTable, this); 
 		return lastField;
 	}
 	
 	public Migration run()
 	{
 		DatabaseConnection source = ConnectionFactory.getConnection(sourceDBMS)
 			.open(sourceDatabase, null, null);
 		DatabaseConnection target = ConnectionFactory.getConnection(targetDBMS)
 			.open(targetDatabase, null, null);
 
 		try
 		{
 			for (Table table: tables)
 				runTable(source, target, table);
 		}
 		finally
 		{
 			source.close();
 			target.close();
 		}
 		
 		return this;
 	}
 	
 	private void runTable(DatabaseConnection source, DatabaseConnection target, Table table)
 	{
 		readDataFromSourceTable(table, source);
		if (incremental)
			deleteFromTargetTable(table, target);
 		writeDataToTargetTable(table, target);
 	}
 
 	private void writeDataToTargetTable(Table table, DatabaseConnection target)
 	{
 		StringBuilder sql = createInsertQuery(table);
 		
 		try
 		{
 			PreparedStatement ps = target.getConnection().prepareStatement(sql.toString());
 			try
 			{
 				for (int count = 1; count <= table.recordCount(); count++)
 				{
 					ps.clearParameters();
 					int paramCount = 1;
 					for (Field field: table.fields())
 					{
 						log.debug("Including value " + table.getValue(field.sourceName, count-1) + 
 							" for field " + field + " in table " + table);
 						ps.setObject(paramCount, field.copy(table.getValue(field.sourceName, count-1)));
 						paramCount++;
 					}
 					ps.execute();
 				}
 			}
 			finally
 			{
 				ps.close();
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	private StringBuilder createInsertQuery(Table table)
 	{
 		StringBuilder sql = new StringBuilder()
 			.append("insert into ")
 			.append(table.targetName)
 			.append(" (");
 		
 		for (Field field: table.fields())
 			sql.append(field.targetName).append(',');
 		sql.delete(sql.length()-1, sql.length());
 		sql.append(") VALUES(");
 		for (int i = 0; i < table.fieldCount(); i++)
 			sql.append("?,");
 		sql.delete(sql.length()-1, sql.length());
 		sql.append(')');
 		log.debug(sql);
 		return sql;
 	}
 
 
 	private void readDataFromSourceTable(Table table, DatabaseConnection source)
 	{
 		try
 		{
 			ResultSet result = source.executeQuery("select * from " + table.sourceName);
 			try
 			{
 				ResultSetMetaData metadata = result.getMetaData();
 				Map<String, Integer> columns = loadColumns(metadata);
 				while (result.next())
 				{
 					for (Field field: table.fields())
 					{
 						Integer index = columns.get(field.sourceName.toUpperCase()); 
 						field.sourceType = metadata.getColumnTypeName(index);
 						table.putValue(field.sourceName, result.getObject(field.sourceName));
 					}
 				}
 			}
 			finally
 			{
 				result.close();
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	private Map<String, Integer> loadColumns(ResultSetMetaData metadata)
 		throws SQLException
 	{
 		Map<String, Integer> columns = new HashMap<String, Integer>();
 		for (int i = 1; i <= metadata.getColumnCount(); i++)
 			columns.put(metadata.getColumnName(i).toUpperCase(), i);
 		return columns;
 	}
 
 	private void deleteFromTargetTable(Table table, DatabaseConnection target)
 	{
 		target.execute("delete from " + table.targetName);
 	}
 
 	private boolean sourceWasLast = false;
 	
 	public Migration from(String databaseSource)
 	{
 		this.sourceDatabase = databaseSource;
 		sourceWasLast = true;
 		return this;
 	}
 
 
 	public Migration to(String databaseTarget)
 	{
 		this.targetDatabase = databaseTarget;
 		sourceWasLast = false;
 		return this;
 	}
 
 
 	public Migration in(DBMS sgbd)
 	{
 		if (sourceWasLast)
 			this.sourceDBMS = sgbd;
 		else
 			this.targetDBMS = sgbd;
 		return this;
 	}
 
 	public Migration allFields()
 	{
 		try
 		{
 			DatabaseConnection source = ConnectionFactory.getConnection(sourceDBMS)
 				.open(sourceDatabase, null, null);
 			try
 			{
 				String tableName = lastTable.sourceName;
 				ResultSet result = source.executeQuery("select * from " + tableName);
 				try
 				{
 					ResultSetMetaData metadata = result.getMetaData();
 					for (int i = 1; i <= metadata.getColumnCount(); i++)
 					{
 						String columnName = metadata.getColumnName(i);
 						this.field(columnName).to(columnName);
 					}
 				}
 				finally
 				{
 					result.close();
 				}
 			}
 			finally
 			{
 				source.close();
 			}
 			return this;
 			
 		}
 		catch (SQLException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	public Migration withTransformation(Transformation transformation)
 	{
 		lastField.transformation(transformation);
 		return this;
 	}
 
 	public Migration incremental()
 	{
 		incremental = true;
 		return this;
 	}
 }
 
 class Table
 {
 	String sourceName;
 	private Migration migration;
 	String targetName;
 	private Map<String, List<Object>> sourceMap = new HashMap<String, List<Object>>();
 	private List<Field> fields = new ArrayList<Field>();
 
 	protected void addField(Field field)
 	{
 		fields.add(field);
 		sourceMap.put(field.sourceName, new ArrayList<Object>());
 	}
 
 	protected int recordCount()
 	{
 		return sourceMap.get(sourceMap.keySet().iterator().next()).size();
 	}
 	
 	protected int fieldCount()
 	{
 		return fields.size();
 	}
 	
 	protected Iterable<Field> fields()
 	{
 		return fields;
 	}
 
 	protected Object getValue(String field, int index)
 	{
 		return sourceMap.get(field).get(index);
 	}
 	
 	protected void putValue(String field, Object value)
 	{
 		sourceMap.get(field).add(value);
 	}
 	
 	public Table(String name, Migration migration)
 	{
 		this.sourceName = name;		
 		this.migration = migration;
 		migration.addTable(this);
 	}
 	
 	public Migration to(String targetName)
 	{
 		this.targetName = targetName;
 		return migration;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return new StringBuilder()
 			.append('(')
 			.append(sourceName)
 			.append(',')
 			.append(targetName)
 			.append(')')
 			.toString();
 	}
 }
 
 class Field
 {
 	String sourceName;
 	String sourceType;
 	Migration migration;
 	String targetName;
 	Transformation transformation;
 	
 	public Field(String name, Table table, Migration migration)
 	{
 		this.sourceName = name;		
 		this.migration = migration;
 		table.addField(this);
 	}
 	
 	public Object copy(Object value)
 	{
 		return transformation == null ? value : transformation.transform(value);
 	}
 
 	public Field transformation(Transformation transformation)
 	{
 		this.transformation = transformation;
 		return this;
 	}
 	
 	public Transformation transformation()
 	{
 		return transformation;
 	}
 
 	public Migration to(String targetName)
 	{
 		this.targetName = targetName;
 		return migration;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return new StringBuilder()
 			.append('(')
 			.append(sourceName)
 			.append(',')
 			.append(targetName)
 			.append(')')
 			.toString();
 	}
 	
 }
