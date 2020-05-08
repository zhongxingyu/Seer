 package gov.usgs.cida.nude.column;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class SimpleColumn implements Column {
 
 	protected final String columnName;
 	protected final String tableName;
 	protected final String schemaName;
 	protected final Class<?> valueType;
 	protected final boolean isDisplay;
 	
 	public SimpleColumn(String columnName) {
 		this(columnName, null, null, String.class, true);
 	}
 	
 	public SimpleColumn(String columnName, boolean isDisplayable) {
 		this(columnName, null, null, String.class, isDisplayable);
 	}
 	
 	public SimpleColumn(String column, String table, String schema, Class<?> type, boolean isDisplayable) {
 		this.columnName = (StringUtils.isNotBlank(column))?column:"";
 		this.tableName = (StringUtils.isNotBlank(table))?table:"";
 		this.schemaName = (StringUtils.isNotBlank(schema))?schema:"";
 		this.valueType = type;
 		this.isDisplay = isDisplayable;
 	}
 	
 	@Override
 	public String getName() {
 		return this.columnName;
 	}
 
 	@Override
 	public String getQualifiedName() {
 		StringBuffer result = new StringBuffer();
 		String tableName = this.getTableName();
 		String colName = this.getName();
 		if (StringUtils.isNotBlank(tableName)) {
 			result.append(tableName).append(".");
 		}
 		result.append(colName);
 		return result.toString();
 	}
 
 	@Override
 	public String getFullName() {
 		StringBuffer result = new StringBuffer();
 		String schemaName = this.getSchemaName();
 		String tableName = this.getTableName();
 		String colName = this.getName();
 		if (StringUtils.isNotBlank(schemaName)) {
 			result.append(schemaName).append(".");
 		}
 		if (StringUtils.isNotBlank(tableName)) {
 			result.append(tableName).append(".");
 		}
 		result.append(colName);
 		return result.toString();
 	}
 
 	@Override
 	public String getTableName() {
 		return this.tableName;
 	}
 
 	@Override
 	public String getSchemaName() {
 		return this.schemaName;
 	}
 
 	@Override
 	public Class<?> getValueType() {
 		return this.valueType;
 	}
 
 	@Override
 	public boolean isDisplayable() {
 		return this.isDisplay;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) { return false; }
 		if (obj == this) { return true; }
 		if (obj instanceof Column) {
 			Column rhs = (Column) obj;
 			return new EqualsBuilder()
 				.append(this.getFullName(), rhs.getFullName())
 				.append(this.getValueType(), rhs.getValueType())
 				.isEquals();
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
		return new HashCodeBuilder(51, 89)
 				.append(this.getFullName())
 				.append(this.getValueType())
 				.toHashCode();
 	}
 }
