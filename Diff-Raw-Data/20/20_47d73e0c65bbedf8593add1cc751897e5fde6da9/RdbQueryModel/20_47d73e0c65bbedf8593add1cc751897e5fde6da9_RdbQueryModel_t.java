 package jp.co.flect.javascript.jqgrid;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Arrays;
 
 import jp.co.flect.rdb.SelectTokenizer;
 import jp.co.flect.util.DateFormatHolder;
 
 public class RdbQueryModel {
 	
 	private Connection con;
 	private String baseQuery;
 	private ColModel model;
 	private boolean hasWhere;
 	
 	private boolean useOffset;
 	private String where;
 	private String order;
 	
 	private DateFormatHolder formats = new DateFormatHolder();
 	
 	public RdbQueryModel(Connection con, String query) throws SQLException{
 		this(con, query, new RdbColModelFactory(con).getQueryModel(query));
 	}
 	
 	public RdbQueryModel(Connection con, String query, ColModel model) {
 		this.con = con;
 		this.model = model;
 		this.baseQuery = checkQuery(query);
 	}
 	
 	private String checkQuery(String query) {
 		SelectTokenizer st = new SelectTokenizer(query);
 		StringBuilder ret = new StringBuilder();
 		StringBuilder buf = new StringBuilder();
 		
		int bracketCount = 0;
 		int n = st.next(buf);
 		while (true) {
 			boolean addSpace = true;
 			switch (n) {
 				case SelectTokenizer.T_LITERAL:
 					String s = buf.toString();
 					if (s.equalsIgnoreCase("where")) {
 						this.hasWhere = true;
					} else if (bracketCount == 0 && s.equalsIgnoreCase("order")) {
 						n = st.next(buf);
 						if (n != SelectTokenizer.T_LITERAL || !buf.toString().equalsIgnoreCase("by")) {
 							throw new IllegalArgumentException(query);
 						}
 						n = st.next(buf);
 						StringBuilder orderBuf = new StringBuilder();
 						while (n != SelectTokenizer.T_END) {
 							orderBuf.append(buf).append(" ");
 							n = st.next(buf);
 						}
 						this.order = orderBuf.toString().trim();
 						return ret.toString().trim();
 					}
 					break;
 				case SelectTokenizer.T_END:
 					return ret.toString().trim();
 				case SelectTokenizer.T_ERROR:
 					throw new IllegalArgumentException(query);
 				case SelectTokenizer.T_STRING:
 					break;
 				case SelectTokenizer.T_COMMA:
					addSpace = false;
					break;
 				case SelectTokenizer.T_OPEN_BRACKET:
					addSpace = false;
					bracketCount++;
					break;
 				case SelectTokenizer.T_CLOSE_BRACKET:
 					addSpace = false;
					bracketCount--;
 					break;
 				case SelectTokenizer.T_NUMBER:
 				case SelectTokenizer.T_BOOLEAN:
 					break;
 			}
 			if (n == SelectTokenizer.T_STRING) {
 				ret.append(" '").append(buf).append("' ");
 			} else {
 				if (addSpace) {
 					ret.append(" ");
 				}
 				ret.append(buf);
 			}
 			n = st.next(buf);
 		}
 	}
 	
 	public DateFormatHolder getDateFormatHolder() { return this.formats;}
 	public void setDateFormatHolder(DateFormatHolder h) { this.formats = h;}
 	
 	public Connection getConnection() { return this.con;}
 	public String getBaseQuery() { return this.baseQuery;}
 	
 	public boolean isUseOffset() { return this.useOffset;}
 	public void setUseOffset(boolean b) { this.useOffset = b;}
 	
 	public String getWhere() { return this.where;}
 	public void setWhere(String s) { this.where = s;}
 	
 	public List<Object> setFilters(String jsonStr) {
 		if (jsonStr == null || jsonStr.length() == 0) {
 			this.where = null;
 			return null;
 		}
 		Filters filters = new Filters(this.model);
 		Filters.SQLInfo info = filters.getSQL(jsonStr);
 		String str = info.getSQL();
 		if (str != null && str.trim().length() > 0) {
 			this.where = str;
 			return info.getParams();
 		} else {
 			this.where = null;
 			return null;
 		}
 	}
 	
 	public String getOrder() { return this.order;}
 	public void setOrder(String s) { this.order = s;}
 	public void setOrder(String col, boolean asc) {
 		if (col == null || col.length() == 0) {
 			this.order = null;
 		} else {
 			this.order = asc ? col : col + " DESC";
 		}
 	}
 	
 	public void clear() {
 		this.where = null;
 		this.order = null;
 	}
 		
 	public GridData getGridData(int page, int rowCount, Object... params) throws SQLException {
 		return getGridData(page, rowCount, Arrays.asList(params));
 	}
 	
 	public GridData getGridData(int page, int rowCount, List<?> params) throws SQLException {
 		StringBuilder buf = new StringBuilder(this.baseQuery);
 		if (this.where != null) {
 			buf.append(this.hasWhere ? " AND " : " WHERE ");
 			buf.append(this.where);
 		}
 		String countQuery = buildCountQuery(buf.toString());
 		if (this.order != null) {
 			buf.append(" ORDER BY ").append(this.order);
 		}
 		RdbQuery query = new RdbQuery(this.con, buf.toString(), countQuery, this.useOffset);
 		try {
 			query.setDateFormatHolder(this.formats);
 			return query.getGridData(page, rowCount, params);
 		} finally {
 			query.close();
 		}
 	}
 	
 	private String buildCountQuery(String query) {
 		StringBuilder ret = new StringBuilder();
 		ret.append("SELECT COUNT(*) FROM (");
 		
 		StringBuilder buf = new StringBuilder();
 		SelectTokenizer st = new SelectTokenizer(query);
 		int n = st.next(buf);
 		while (n != SelectTokenizer.T_END) {
 			String s = buf.toString();
 			if (n == SelectTokenizer.T_STRING) {
 				ret.append("'").append(s).append("' ");
 			} else {
 				ret.append(s).append(" ");
 			}
 			n = st.next(buf);
 		}
 		ret.append(") CNT");
 		return ret.toString();
 	}
 }
