 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.kikaineko.ssu.db;
 
 import java.math.BigDecimal;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 
 import org.kikaineko.util.FileIO;
 /**
  *
  * @author Masayuki Ioki
  *
  */
 public class Mapper {
 
 	public static final String DB2 = "db2";
 	public static final String ORACLE = "oracle";
 	public static final String NETEZZA = "nz";
 	private static String dbmsType = null;
 
 	public static void setDbmsType(String jdbcClass) {
 		String s = jdbcClass.toLowerCase();
 		if (s.indexOf("db2") != -1) {
 			dbmsType = "db2";
 		} else if (s.indexOf("oracle") != -1) {
 			dbmsType = "oracle";
 		} else if (s.indexOf("netezza") != -1){
 			dbmsType = "nz";
 		}
 	}
 
 	public static String getDbmsType() {
 		return dbmsType;
 	}
 
 	private static boolean isEmpty(String s) {
 		return s == null || s.length() == 0;
 	}
 
 	protected static String sanit(String s) {
 		if (s.indexOf("\"") != -1) {
 			s = s.replaceAll("\\\"", "\\\"\\\"");
 		}
 		return s;
 	}
 
 	public static String normTOCSV(ResultSet rset, String name, int type)
 			throws Exception {
 		switch (type) {
 		case Types.CHAR:
 		case Types.VARCHAR:
 		case Types.LONGVARCHAR:
 		case Types.NVARCHAR:
 		case Types.LONGNVARCHAR:
 			String charCode = FileIO.code(FileIO.DBSelectCode);
 
 			String s = null;
 			if (charCode != null) {
 				byte[] bs = rset.getBytes(name);
 				if (bs != null) {
 					s = new String(bs, charCode);
 				}
 			} else {
 				s = rset.getString(name);
 			}
 			if (s != null)
 				return "\"" + sanit(s) + "\"";
 			return "\"\"";
 
 		case Types.BINARY:
 		case Types.VARBINARY:
 		case Types.LONGVARBINARY:
 			byte[] bs = rset.getBytes(name);
 			if (bs != null && bs.length != 0)
 				return "\"" + toS(bs) + "\"";
 			return "\"\"";
 		case Types.BIT:
 			boolean b = rset.getBoolean(name);
 			return "\"" + Boolean.valueOf(b).toString() + "\"";
 
 		case Types.TINYINT:
 		case Types.SMALLINT:
 			short sc = rset.getShort(name);
 			return Short.toString(sc);
 
 		case Types.INTEGER:
 			int j = rset.getInt(name);
 			return Integer.toString(j);
 
 		case Types.BIGINT:
 			long l = rset.getLong(name);
 			return Long.toString(l);
 
 		case Types.REAL:
 			float f = rset.getFloat(name);
 			return Float.toString(f);
 
 		case Types.FLOAT:
 		case Types.DOUBLE:
 			double dd = rset.getDouble(name);
 			return Double.toString(dd);
 		case Types.NUMERIC:
 		case Types.DECIMAL:
 			BigDecimal bd = rset.getBigDecimal(name);
 			if (bd != null)
 				return bd.toString();
 			return "";
 		case Types.DATE:
 			if (getDbmsType().equals(DB2)) {
 				java.sql.Date d = rset.getDate(name);
 				return "\"" + TimeF.toS(d) + "\"";
 			}
 			else if (getDbmsType().equals(ORACLE)) {
 				java.sql.Timestamp ts0 = rset.getTimestamp(name);
 				return "\"" + TimeF.toS(ts0).split("\\.")[0] + "\"";
 			}
 			return "\"" + rset.getDate(name) + "\"";
 		case Types.TIME:
 			java.sql.Time t = rset.getTime(name);
 			return "\"" + TimeF.toS(t) + "\"";
 		case Types.TIMESTAMP:
 			java.sql.Timestamp ts = rset.getTimestamp(name);
 			return "\"" + TimeF.toS(ts) + "\"";
 		default:
 			break;
 		}
 		throw new SQLException(name + " is Uncontroled TYPE.");
 	}
 
 	public static Object normFromCSV(String s, int type) throws Exception {
 		try {
 			return _normFromCSV(s, type);
 		} catch (SQLException se){
 			throw se;
 		} catch (Exception e) {
 			throw new FormatException(e);
 		}
 	}
 
 	private static Object _normFromCSV(String s, int type) throws SQLException {
 		String ss = s;
 		if(ss!=null){
 			s = ss.trim();
 		}
 		switch (type) {
 		case Types.CHAR:
 		case Types.VARCHAR:
 		case Types.LONGVARCHAR:
 		case Types.NVARCHAR:
 		case Types.LONGNVARCHAR:
 		case Types.BINARY:
 		case Types.VARBINARY:
 		case Types.LONGVARBINARY:
 			if (isEmpty(ss)) {
 				return "";
 			}
 			return ss;
 		case Types.BIT:
 			if ("0".equals(s)
 					|| (!isEmpty(s) && "true".equals(s.toLowerCase()))) {
 				return Boolean.TRUE;
 			} else {
 				return Boolean.FALSE;
 			}
 		case Types.TINYINT:
 		case Types.SMALLINT:
 			if (!isEmpty(s)) {
 				return Short.valueOf(s);
 			} else {
 				return new Short((short) 0);
 			}
 		case Types.INTEGER:
 			if (!isEmpty(s)) {
 				return new Integer(s);
 			} else {
 				return new Integer(0);
 			}
 		case Types.BIGINT:
 			if (!isEmpty(s)) {
 				return new Long(s);
 			} else {
 				return new Long(0);
 			}
 
 		case Types.REAL:
 			if (!isEmpty(s)) {
 				return new Float(s);
 			} else {
 				return new Float(0);
 			}
 		case Types.FLOAT:
 		case Types.DOUBLE:
 			if (!isEmpty(s)) {
 				return new Double(s);
 			} else {
 				return new Double(0);
 			}
 		case Types.NUMERIC:
 		case Types.DECIMAL:
 			if (!isEmpty(s)) {
 				return new SSUBigDecimal(new BigDecimal(s));
 			} else {
 				return "";
 			}
 
 		case Types.DATE:
 			if (getDbmsType().equals(DB2)) {
 				return TimeF.toSFromDate(s);
 			}
 			else if (getDbmsType().equals(ORACLE)) {
 				return TimeF.toSFromTimestamp(s).split("\\.")[0];
 			}else{return TimeF.toSFromDate(s);}
 		case Types.TIME:
 			return TimeF.toSFromTime(s);
 		case Types.TIMESTAMP:
 			return TimeF.toSFromTimestamp(s);
 		default:
 			break;
 		}
 		throw new SQLException(s + " is Uncontroled TYPE.");
 	}
 
 	public static Object normFromDB(ResultSet rset, int i, int type)
 			throws Exception {
 		switch (type) {
 		case Types.CHAR:
 		case Types.VARCHAR:
 		case Types.LONGVARCHAR:
 		case Types.NVARCHAR:
 		case Types.LONGNVARCHAR:
 			String charCode = FileIO.code(FileIO.DBSelectCode);
 
 			String s = null;
 			if (charCode != null) {
 				byte[] bs = rset.getBytes(i);
 				if (bs != null) {
 					s = new String(bs, charCode);
 				}
 			} else {
 				s = rset.getString(i);
 			}
 			if (s != null) {
 				if (getDbmsType().equals(DB2)) {
 					// trim right space
 					return s.replaceAll("\\s+$", "");
 				}
 				return s;
 			}
			return "";
 
 		case Types.BINARY:
 		case Types.VARBINARY:
 		case Types.LONGVARBINARY:
 			byte[] bs = rset.getBytes(i);
 			if (bs != null && bs.length != 0)
 				return toS(bs);
 			return "";
 		case Types.BIT:
 			boolean b = rset.getBoolean(i);
 			return new Boolean(b);
 
 		case Types.TINYINT:
 		case Types.SMALLINT:
 			short sc = rset.getShort(i);
 			return new Short(sc);
 
 		case Types.INTEGER:
 			int j = rset.getInt(i);
 			return new Integer(j);
 
 		case Types.BIGINT:
 			long l = rset.getLong(i);
 			return new Long(l);
 
 		case Types.REAL:
 			float f = rset.getFloat(i);
 			return new Float(f);
 
 		case Types.FLOAT:
 		case Types.DOUBLE:
 			double dd = rset.getDouble(i);
 			return new Double(dd);
 		case Types.NUMERIC:
 		case Types.DECIMAL:
 			BigDecimal bd = rset.getBigDecimal(i);
 			if (bd != null) {
 				return new SSUBigDecimal(bd);
 			}
 			return "";
 		case Types.DATE:
 			if (getDbmsType().equals(DB2)) {
 				return TimeF.toS(rset.getDate(i));
 			}
 			else if (getDbmsType().equals(ORACLE)) {
 				java.sql.Timestamp ts0 = rset.getTimestamp(i);
 				return TimeF.toS(ts0).split("\\.")[0];
 			}else {
 				return TimeF.toS(rset.getDate(i));
 			}
 		case Types.TIME:
 			java.sql.Time t = rset.getTime(i);
 			return TimeF.toS(t);
 		case Types.TIMESTAMP:
 			java.sql.Timestamp ts = rset.getTimestamp(i);
 			return TimeF.toS(ts);
 		default:
 			break;
 		}
 		throw new SQLException("Uncontroled TYPE.");
 	}
 
 	private static String toS(byte[] bs) {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < bs.length; i++) {
 			sb.append(bs[i]);
 		}
 		return sb.toString();
 	}
 }
