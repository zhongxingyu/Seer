 package framework.db;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * Prepared Statement 를 이용하기 위한 객체
  */
 public class PreparedStatement extends AbstractStatement {
 	private String sql = null;
 	private DB db = null;
 	private java.sql.PreparedStatement pstmt = null;
 	private RecordSet rs = null;
 	private int upCnt = 0;
 	private List<Object> param = new ArrayList<Object>();
 	private Object caller = null;
 
 	public static PreparedStatement create(String sql, DB db, Object caller) {
 		return new PreparedStatement(sql, db, caller);
 	}
 
 	private PreparedStatement(String sql, DB db, Object caller) {
 		this.sql = sql;
 		this.db = db;
 		this.caller = caller;
 	}
 
 	@Override
 	public void close() {
 		try {
 			if (pstmt != null) {
 				pstmt.close();
 				pstmt = null;
 			}
 			clearParam();
 		} catch (SQLException e) {
 			logger.error("", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void clearParam() {
 		param = new ArrayList<Object>();
 	}
 
 	public RecordSet executeQuery() {
 		return executeQuery(0, 0);
 	}
 
 	public RecordSet executeQuery(int currPage, int pageSize) {
 		if (getSQL() == null) {
 			logger.error("Query is Null");
 			return null;
 		}
 		try {
 			java.sql.PreparedStatement pstmt = getPrepareStatment();
 			if (getParamSize() > 0) {
 				for (int i = 1; i <= getParamSize(); i++) {
 					if (getObject(i - 1) == null || "".equals(getObject(i - 1))) {
 						pstmt.setNull(i, java.sql.Types.VARCHAR);
 					} else {
 						pstmt.setObject(i, getObject(i - 1));
 					}
 				}
 			}
 			if (logger.isDebugEnabled()) {
 				StringBuilder log = new StringBuilder();
 				log.append("@Sql Start (P_STATEMENT) FetchSize : " + pstmt.getFetchSize() + " Caller : " + caller.getClass().getName() + "\n");
 				log.append("@Sql Command : \n" + getQueryString());
 				logger.debug(log.toString());
 			}
 			rs = new RecordSet(pstmt.executeQuery(), currPage, pageSize);
 			if (logger.isDebugEnabled()) {
 				logger.debug("@Sql End (P_STATEMENT)");
 			}
 		} catch (SQLException e) {
 			logger.error("", e);
 			throw new RuntimeException(e.getMessage() + "\nSQL : " + getQueryString());
 		}
 		return rs;
 	}
 
 	public RecordSet executeQuery(String sql) {
 		setSQL(sql);
 		return executeQuery(0, 0);
 	}
 
 	public RecordSet executeQuery(String sql, int currPage, int pageSize) {
 		setSQL(sql);
 		return executeQuery(currPage, pageSize);
 	}
 
 	public int executeUpdate() {
 		if (getSQL() == null) {
 			logger.error("Query is Null");
 			return 0;
 		}
 		try {
 			java.sql.PreparedStatement pstmt = getPrepareStatment();
 			if (getParamSize() > 0) {
 				for (int i = 1; i <= getParamSize(); i++) {
 					Object param = getObject(i - 1);
 					if (param == null || "".equals(param)) {
 						pstmt.setNull(i, java.sql.Types.VARCHAR);
 					} else if (param instanceof CharSequence) {
 						pstmt.setString(i, param.toString());
 					} else if (param instanceof byte[]) {
 						int size = ((byte[]) param).length;
 						if (size > 0) {
 							InputStream is = new ByteArrayInputStream((byte[]) param);
 							pstmt.setBinaryStream(i, is, size);
 						} else {
 							pstmt.setBinaryStream(i, null, 0);
 						}
 					} else {
 						pstmt.setObject(i, param);
 					}
 				}
 			}
 			if (logger.isDebugEnabled()) {
 				StringBuilder log = new StringBuilder();
 				log.append("@Sql Start (P_STATEMENT) FetchSize : " + pstmt.getFetchSize() + " Caller : " + caller.getClass().getName() + "\n");
 				log.append("@Sql Command : \n" + getQueryString());
 				logger.debug(log.toString());
 			}
 			upCnt = pstmt.executeUpdate();
 			if (logger.isDebugEnabled()) {
 				logger.debug("@Sql End (P_STATEMENT)");
 			}
 		} catch (SQLException e) {
 			logger.error("", e);
 			throw new RuntimeException(e.getMessage() + "\nSQL : " + getQueryString());
 		}
 		return upCnt;
 	}
 
 	public int executeUpdate(String sql) {
 		setSQL(sql);
 		return executeUpdate();
 	}
 
 	public Object getObject(int idx) {
 		return param.get(idx);
 	}
 
 	public Object[] getParams() {
 		if (param == null)
 			return null;
 		return param.toArray();
 	}
 
 	public int getParamSize() {
 		return param.size();
 	}
 
 	protected java.sql.PreparedStatement getPrepareStatment() {
 		if (getSQL() == null) {
 			logger.error("Query is Null");
 			return null;
 		}
 		try {
 			if (pstmt == null) {
 				pstmt = db.getConnection().prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 				pstmt.setFetchSize(100);
 			}
 		} catch (SQLException e) {
 			logger.error("", e);
 			throw new RuntimeException(e);
 		}
 		return pstmt;
 	}
 
 	public RecordSet getRecordSet() {
 		return rs;
 	}
 
 	public String getSQL() {
 		return sql;
 	}
 
 	public String getString(int idx) {
 		return (String) getObject(idx);
 	}
 
 	public int getUpdateCount() {
 		return upCnt;
 	}
 
 	public void set(Object[] obj) {
 		if (obj == null) {
 			return;
 		}
 		clearParam();
 		for (int i = 0; i < obj.length; i++) {
 			set(i, obj[i]);
 		}
 	}
 
 	public void set(int idx, double value) {
 		set(idx, Double.valueOf(value));
 	}
 
 	public void set(int idx, int value) {
 		set(idx, Integer.valueOf(value));
 	}
 
 	public void set(int idx, long value) {
 		set(idx, Long.valueOf(value));
 	}
 
 	public void set(int idx, Object obj) {
 		param.add(idx, obj);
 	}
 
 	public void set(int idx, byte[] value) {
 		set(idx, (Object) value);
 	}
 
 	public void setSQL(String newSql) {
 		close();
 		sql = newSql;
 	}
 
 	@Override
 	public String toString() {
 		return "SQL : " + getSQL();
 	}
 
 	public String getQueryString() {
 		Object value = null;
 		int qMarkCount = 0;
 		StringTokenizer token = new StringTokenizer(getSQL(), "?");
 		StringBuilder buf = new StringBuilder();
 		while (token.hasMoreTokens()) {
 			String oneChunk = token.nextToken();
 			buf.append(oneChunk);
 			if (param.size() > qMarkCount) {
 				value = param.get(qMarkCount++);
 				if (value == null || "".equals(value)) {
 					value = "NULL";
				} else if (value instanceof CharSequence || value instanceof Date) {
 					value = "'" + value + "'";
 				}
 			} else {
 				if (token.hasMoreTokens()) {
 					value = null;
 				} else {
 					value = "";
 				}
 			}
 			buf.append("" + value);
 		}
 		return buf.toString().trim();
 	}
 }
