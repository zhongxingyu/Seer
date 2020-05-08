 package zcu.xutil.sql;
 
 import static java.sql.ResultSet.CONCUR_READ_ONLY;
 import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
 import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class QueryOptions {
 	public static final QueryOptions DEFAULT = new QueryOptions(0, 0, 0);
 	protected final int fetchsize;
 	protected final int maxrows;
 	protected final int start;
 	protected int timeoutSeconds;
 
 	public QueryOptions(int fetchsize, int maxrows) {
 		this(fetchsize, maxrows, 0);
 	}
 
 	/**
 	 *
 	 * @param fetchsize
 	 *            the fetchsize
 	 * @param maxrows
 	 *            the maxrows
 	 * @param start
 	 *            fetch result from, 0 first row, -1 last row.
 	 * 
 	 */
 	public QueryOptions(int fetchsize, int maxrows, int start) {
 		this.fetchsize = fetchsize;
 		this.maxrows = maxrows;
 		this.start = start;
 	}
 
 	/**
 	 * 多行处理句柄
 	 * 
	 * @param timeout
 	 *            query timeout seconds
 	 * 
 	 * @return this QueryOptions
 	 */
 
 	public final QueryOptions setTimeOut(int timeout) {
 		timeoutSeconds = timeout;
 		return this;
 	}
 	
 	protected  Statement createStatement(Connection c) throws SQLException{
 		return options(c.createStatement(start == 0 ? TYPE_FORWARD_ONLY : TYPE_SCROLL_INSENSITIVE,CONCUR_READ_ONLY ));
 	}
 	
 	protected  PreparedStatement prepareStatement(Connection c,String sql) throws SQLException{
 		return options(c.prepareStatement(sql,start == 0 ? TYPE_FORWARD_ONLY : TYPE_SCROLL_INSENSITIVE,CONCUR_READ_ONLY));
 	}
 	
 	protected <T extends Statement> T options(T st) throws SQLException {
 		if (fetchsize > 0)
 			st.setFetchSize(fetchsize);
 		if (maxrows > 0)
 			st.setMaxRows(maxrows);
 		if (timeoutSeconds > 0)
 			st.setQueryTimeout(timeoutSeconds);
 		return st;
 	}
 
 	final boolean location(ResultSet rs) throws SQLException {
 		return start == 0 ? rs.next() : rs.absolute(start > 0 ? start + 1 : start);
 	}
 }
