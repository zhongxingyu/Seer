 package framework.db;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * PreparedStatement의 Batch 처리를 이용하기 위한 객체
  */
 public class BatchPreparedStatement extends AbstractStatement {
 	private String sql = null;
 	private DB db = null;
 	private PreparedStatement pstmt = null;
 	private List<List<Object>> paramList = new ArrayList<List<Object>>();
 	private Object caller = null;
 
 	public static BatchPreparedStatement create(String sql, DB db, Object caller) {
 		return new BatchPreparedStatement(sql, db, caller);
 	}
 
 	private BatchPreparedStatement(String sql, DB db, Object caller) {
 		this.sql = sql;
 		this.db = db;
 		this.caller = caller;
 	}
 
 	public void addBatch(Object[] where) {
 		List<Object> param = new ArrayList<Object>();
 		for (Object obj : where) {
 			param.add(obj);
 		}
 		paramList.add(param);
 	}
 
 	protected PreparedStatement getPrepareStatment() {
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
 
 	@Override
 	public void close() {
 		try {
 			if (pstmt != null) {
 				pstmt.close();
 				pstmt = null;
 			}
 			clearParamList();
 		} catch (SQLException e) {
 			logger.error("", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void clearParamList() {
 		paramList = new ArrayList<List<Object>>();
 	}
 
 	public int[] executeBatch() {
 		if (getSQL() == null) {
 			logger.error("Query is Null");
 			return new int[] { 0 };
 		}
 		int[] upCnts = null;
 		try {
 			PreparedStatement pstmt = getPrepareStatment();
 			if (logger.isDebugEnabled()) {
 				StringBuilder log = new StringBuilder();
 				log.append("@Sql Start (BATCH P_STATEMENT) FetchSize : " + pstmt.getFetchSize() + " Caller : " + caller.getClass().getName() + "\n");
 				log.append("@Sql Command : \n" + getQueryString());
 				logger.debug(log.toString());
 			}
 			for (List<Object> params : paramList) {
 				for (int i = 1, length = params.size(); i <= length; i++) {
 					Object param = params.get(i - 1);
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
 				pstmt.addBatch();
 			}
 			upCnts = pstmt.executeBatch();
 			if (logger.isDebugEnabled()) {
 				logger.debug("@Sql End (BATCH P_STATEMENT)");
 			}
 		} catch (SQLException e) {
 			logger.error("", e);
 			throw new RuntimeException(e.getMessage() + "\nSQL : " + getQueryString());
 		}
 		return upCnts;
 	}
 
 	public void setSQL(String newSql) {
 		close();
 		sql = newSql;
 	}
 
 	public String getSQL() {
 		return sql;
 	}
 
 	@Override
 	public String toString() {
 		return "SQL : " + getSQL();
 	}
 
 	public String getQueryString() {
 		StringBuilder buf = new StringBuilder();
 		for (List<Object> param : paramList) {
 			Object value = null;
 			int qMarkCount = 0;
 			StringTokenizer token = new StringTokenizer(getSQL(), "?");
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
 			buf.append("\n");
 		}
 		return buf.toString().trim();
 	}
 }
