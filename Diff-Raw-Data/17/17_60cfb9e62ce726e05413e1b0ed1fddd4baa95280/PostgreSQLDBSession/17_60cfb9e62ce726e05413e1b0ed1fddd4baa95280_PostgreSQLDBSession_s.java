 /*
  * Copyright (C) 2009 Solertium Corporation
  *
  * This file is part of the open source GoGoEgo project.
  *
  * GoGoEgo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *  
  * GoGoEgo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
  * 
  * Unless you have been granted a different license in writing by the
  * copyright holders for GoGoEgo, only the GNU General Public License
  * grants you rights to modify or redistribute this code.
  */
 
 package com.solertium.db.vendor;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 import net.jcip.annotations.ThreadSafe;
 
 import com.solertium.db.CBoolean;
 import com.solertium.db.CDate;
 import com.solertium.db.CDateTime;
 import com.solertium.db.CDouble;
 import com.solertium.db.CInteger;
 import com.solertium.db.CLong;
 import com.solertium.db.CString;
 import com.solertium.db.CanonicalColumnName;
 import com.solertium.db.Column;
 import com.solertium.db.ConversionException;
 import com.solertium.db.DBException;
 import com.solertium.db.DBSession;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.IllegalExecutionLevelException;
 import com.solertium.db.Row;
 import com.solertium.db.StringLiteral;
 import com.solertium.util.Replacer;
 
 /**
  * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
  *         href="http://www.solertium.com">Solertium Corporation</a>
  */
 @ThreadSafe
 public class PostgreSQLDBSession extends DBSession {
 
 	private final DataSource ds;
 
 	public PostgreSQLDBSession(final String name, final DataSource ds) {
 		this.ds = ds;
 	}
 
 	@Override
 	protected void _createTable(final String table, final Row prototype,
 			final ExecutionContext ec) throws DBException {
 		if (ec.getExecutionLevel() < ExecutionContext.ADMIN)
 			throw new IllegalExecutionLevelException(
 					"The execution context must be elevated to ADMIN level to create or delete tables.");
 		_doUpdate("CREATE TABLE " + formatIdentifier(table) + " ("
 				+ formatCreateSpecifier(prototype) + ")", ec);
 		createIndices(table, prototype, ec);
 	}
 
 	private String formatIdentifierColumn(final String identifier) {
 		String newIdent;
 		switch(getIdentifierCase()){
 			case CASE_LOWER:
 				newIdent = ("\"" + identifier.toLowerCase() + "\"");
 			case CASE_UPPER:
 				newIdent = ("\"" + identifier.toUpperCase() + "\"");
 			case CASE_UNCHECKED:
 				newIdent = (identifier.toLowerCase());
 			default:
 				newIdent = ("\"" + identifier + "\"");
 		}
 		return newIdent;
 	}
 	
 	public String formatIdentifier(final String identifier) {
 		return formatIdentifier(identifier, false);
 	}
 	
 	public String formatIdentifier(final String identifier, boolean isColumn) {
 		if (isColumn)
 			return formatIdentifierColumn(identifier);
 		else
 			return doFormatIdentifier(identifier);
 	}
 	
 	private String doFormatIdentifier(final String identifier) {
 		StringBuilder newIdent = new StringBuilder();
 		String schema = getSchema();
 		
 		String[] toFormat;
 		if (schema == null || "public".equals(schema))
 			toFormat = new String[] { identifier };
 		else
 			toFormat = new String[] { schema, identifier };
 		
 		for (int i = 0; i < toFormat.length; i++) {
 			String value = toFormat[i];
 			switch(getIdentifierCase()){
 				case CASE_LOWER:
 					newIdent.append("\"" + value.toLowerCase() + "\"");
 				case CASE_UPPER:
 					newIdent.append("\"" + value.toUpperCase() + "\"");
				case CASE_UNCHECKED:
					newIdent.append(value.toLowerCase());
 				default:
 					newIdent.append("\"" + value + "\"");
 			}
 			if (i+1 < toFormat.length)
 				newIdent.append('.');
 		}
 		return newIdent.toString();
 	}
 
 	@Override
 	public String formatLiteral(final StringLiteral literal) {
 		if (literal == null)
 			return "NULL";
 		String s = literal.getString();
 		if (s == null)
 			return "NULL";
 		s = Replacer.replace(s, "'", "''");
 		s = Replacer.replace(s, "\\", "\\\\");
 		return "'" + s + "'";
 	}
 	
 	public String formatCanonicalColumnName(final CanonicalColumnName name) {
 		final StringBuffer ret = new StringBuffer(128);
 		final String t = name.getTable();
 		if (t != null) {
 			ret.append(formatIdentifier(t));
 			ret.append(".");
 		}
 		final String f = name.getField();
 		if ("*".equals(f))
 			ret.append("*");
 		else {
 			ret.append(formatIdentifier(f, true));
 		}
 		return ret.toString();
 	}
 
 	@Override
 	protected DataSource getDataSource() {
 		return ds;
 	}
 
 	@Override
 	protected String getDBColumnType(CBoolean c) {
 		return "BOOLEAN";
 	}
 
 	@Override
 	public String getDBColumnType(final CDate c) {
 		return "DATE";
 	}
 
 	@Override
 	public String getDBColumnType(final CDateTime c) {
 		return "TIMESTAMP";
 	}
 
 	@Override
 	public String getDBColumnType(final CDouble c) {
 		return "DOUBLE PRECISION";
 	}
 
 	@Override
 	public String getDBColumnType(final CInteger c) {
 		return "INT";
 	}
 
 	@Override
 	public String getDBColumnType(final CLong c) {
 		return "BIGINT";
 	}
 
 	@Override
 	public String getDBColumnType(final CString c) {
 		/*
 		 * FIXME: for some reason, this always returns a 
 		 * Text type.  Not sure why, so the author can 
 		 * remove this comment later if need be.  For now, 
 		 * for consistency with changes made in SIS-1, I 
 		 * am changing this function to follow change at 
 		 * http://review.source.gogoego.com/585 
 		 */
 //		int scale = c.getScale();
 //		if (scale == 0)
 //			scale = 255;
 //		if (scale < 255)
 //			return "VARCHAR(" + scale + ")";
 //		return "LONGVARCHAR";
 //		Was just...
 //		return "TEXT";
 		
 		int scale = c.getScale();
 		if (scale == 0)
 			scale = 255;
 		
 		if (scale <= 255)
 			return "VARCHAR(" + scale + ")";
 		
 		return "TEXT";
 	}
 	
 	protected Row _listColumns(Connection conn, String tableName, ExecutionContext ec, boolean captureData) throws UnsupportedOperationException {
 		try {
 			final DatabaseMetaData md = conn.getMetaData();
 			
 			ResultSet rs = md.getColumns(conn.getCatalog(), null, tableName, "%");
 					
 			return rsToRow(rs);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new UnsupportedOperationException(e);	
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new UnsupportedOperationException(e);
 		}
 	}
 	
 	private Row rsToRow(final ResultSet rs) throws SQLException {
 		final Row row = new Row();
 		while (rs.next()) {
 			final ResultSetMetaData rsmd = rs.getMetaData();
 			final String name = rs.getString("COLUMN_NAME");
 			final String type = rs.getString("TYPE_NAME");
 			try {
 				Column c = convertColumn(type, rs, rsmd, null);
 				c.setLocalName(name);				
 				row.add(c);
 			} catch (NullPointerException e) {
 				System.out.println("### DBSession Failure: Failed converting column with type " + type + " and name " + name);
 			}
 		}
 		return row;
 	}
 
 	@Override
 	public Row rsToRow(final ResultSet rs, final ResultSetMetaData rsmd) throws SQLException {
 		final Row r = new Row();
 		final int columns = rsmd.getColumnCount();
 		for (int i = 1; i <= columns; i++) {
 			String typename = rsmd.getColumnTypeName(i);
 			typename = typename.toUpperCase();
 			Column c = convertColumn(typename, rs, rsmd, i);					
 			
 			if (c != null) {
 				c.setLocalName(rsmd.getColumnName(i));
 				r.add(c);
 			}
 		}
 		return r;
 	}
 	
 	private Column convertColumn(final String type, final ResultSet rs, final ResultSetMetaData rsmd, final Integer i) throws SQLException {
 		Column c = null;
 		final String typename = type.toUpperCase();
 		if (typename.startsWith("VARCHAR")) {
 			c = new CString();
 			c.setScale(255);
 			try {
 				if (i != null)
 					c.setObject(rs.getString(i.intValue()));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 							+ e.getMessage());
 			} 
 		} else if (typename.startsWith("TEXT")) {
 			c = new CString();
 			c.setScale(65536);
 			try {
 				if (i != null)
 					c.setObject(rs.getString(i.intValue()));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 						+ e.getMessage());
 			} 
 		} else if (typename.startsWith("BOOL")) {
 			c = new CBoolean();
 			try {
 				if (i != null)
 					c.setObject(rs.getBoolean(i.intValue()));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 						+ e.getMessage());
 			} 
 		} else if (typename.startsWith("CHAR")) {
 			c = new CString();
 			if (i != null)
 				c.setScale(rsmd.getPrecision(i.intValue()));
 			try {
 				if (i != null)
 					c.setObject(Replacer.stripWhitespace(rs.getString(i.intValue())));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 						+ e.getMessage());
 			} 
 		} else if (typename.startsWith("FLOAT") || typename.startsWith("DOUBLE")) {
 			c = new CDouble();
 			try {
 				if (i != null)
 					c.setObject(rs.getDouble(i.intValue()));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 						+ e.getMessage());
 			} 
 		} else if (typename.startsWith("INT")) {
 			c = new CInteger();
 			try {
 				if (i != null)
 					c.setObject(rs.getInt(i.intValue()));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 						+ e.getMessage());
 			}
 		} else if (typename.startsWith("DATE")
 				|| typename.startsWith("TIMESTAMP")) {
 			c = new CDateTime();
 			try {
 				if (i != null)
 					c.setObject(rs.getTimestamp(i.intValue()));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 						+ e.getMessage());
 			} 
 		} else if (typename.startsWith("BIGINT")) {
 			c = new CLong();
 			try {
 				if (i != null)
 					c.setObject(rs.getLong(i.intValue()));
 			} catch (final ConversionException e) {
 				throw new SQLException("Conversion problem: "
 						+ e.getMessage());
 			}
 		}
 		return c;
 	}
 
 }
