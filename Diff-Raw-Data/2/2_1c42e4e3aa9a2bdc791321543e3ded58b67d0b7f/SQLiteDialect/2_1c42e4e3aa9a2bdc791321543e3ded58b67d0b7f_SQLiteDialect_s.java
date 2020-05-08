 /*
  * Hibernate Dialect for SQLite
  * Copyright (C) 2011 David Harcombe
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
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
 package com.gramercysoftware.hibernate;
 
 import java.sql.Types;
 
 import org.hibernate.dialect.Dialect;
 import org.hibernate.dialect.function.SQLFunctionTemplate;
 import org.hibernate.dialect.function.StandardSQLFunction;
 import org.hibernate.dialect.function.VarArgsSQLFunction;
 import org.hibernate.type.StandardBasicTypes;
 
 public class SQLiteDialect extends Dialect {
 	public SQLiteDialect() {
 		registerColumnType(Types.BIT, "integer");
 		registerColumnType(Types.TINYINT, "tinyint");
 		registerColumnType(Types.SMALLINT, "smallint");
 		registerColumnType(Types.INTEGER, "integer");
 		registerColumnType(Types.BIGINT, "bigint");
 		registerColumnType(Types.FLOAT, "float");
 		registerColumnType(Types.REAL, "real");
 		registerColumnType(Types.DOUBLE, "double");
 		registerColumnType(Types.NUMERIC, "numeric");
 		registerColumnType(Types.DECIMAL, "decimal");
 		registerColumnType(Types.CHAR, "char");
 		registerColumnType(Types.VARCHAR, "varchar");
 		registerColumnType(Types.LONGVARCHAR, "longvarchar");
 		registerColumnType(Types.DATE, "date");
 		registerColumnType(Types.TIME, "time");
 		registerColumnType(Types.TIMESTAMP, "timestamp");
 		registerColumnType(Types.BINARY, "blob");
 		registerColumnType(Types.VARBINARY, "blob");
 		registerColumnType(Types.LONGVARBINARY, "blob");
		// registerColumnType(Types.NULL, "null");
 		registerColumnType(Types.BLOB, "blob");
 		registerColumnType(Types.CLOB, "clob");
 		registerColumnType(Types.BOOLEAN, "integer");
 
 		registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""));
 		registerFunction("mod", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "?1 % ?2"));
 		registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
 		registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
 	}
 
 	public boolean supportsIdentityColumns() {
 		return true;
 	}
 
 	public boolean hasDataTypeInIdentityColumn() {
 		return false;
 	}
 
 	public String getIdentityColumnString() {
 		return "integer";
 	}
 
 	public String getIdentitySelectString() {
 		return "select last_insert_rowid()";
 	}
 
 	public boolean supportsLimit() {
 		return true;
 	}
 
 	protected String getLimitString(String query, boolean hasOffset) {
 		return new StringBuffer(query.length() + 20).append(query).append(hasOffset ? " limit ? offset ?" : " limit ?").toString();
 	}
 
 	public boolean supportsTemporaryTables() {
 		return true;
 	}
 
 	public String getCreateTemporaryTableString() {
 		return "create temporary table if not exists";
 	}
 
 	public boolean dropTemporaryTableAfterUse() {
 		return false;
 	}
 
 	public boolean supportsCurrentTimestampSelection() {
 		return true;
 	}
 
 	public boolean isCurrentTimestampSelectStringCallable() {
 		return false;
 	}
 
 	public String getCurrentTimestampSelectString() {
 		return "select current_timestamp";
 	}
 
 	public boolean supportsUnionAll() {
 		return true;
 	}
 
 	public boolean hasAlterTable() {
 		return false;
 	}
 
 	public boolean dropConstraints() {
 		return false;
 	}
 
 	public String getAddColumnString() {
 		return "add column";
 	}
 
 	public String getForUpdateString() {
 		return "";
 	}
 
 	public boolean supportsOuterJoinForUpdate() {
 		return false;
 	}
 
 	public String getDropForeignKeyString() {
 		throw new UnsupportedOperationException("No drop foreign key syntax supported by SQLiteDialect");
 	}
 
 	public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
 		throw new UnsupportedOperationException("No add foreign key syntax supported by SQLiteDialect");
 	}
 
 	public String getAddPrimaryKeyConstraintString(String constraintName) {
 		throw new UnsupportedOperationException("No add primary key syntax supported by SQLiteDialect");
 	}
 
 	public boolean supportsIfExistsBeforeTableName() {
 		return true;
 	}
 
 	public boolean supportsCascadeDelete() {
 		return false;
 	}
 }
