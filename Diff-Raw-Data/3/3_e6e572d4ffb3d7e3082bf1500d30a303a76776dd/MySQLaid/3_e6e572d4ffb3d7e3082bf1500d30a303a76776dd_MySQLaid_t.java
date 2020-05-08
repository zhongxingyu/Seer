 package io;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import filter.FilterItem;
 import filter.FilterState;
 
 public class MySQLaid extends MySQL {
 	public MySQLaid(Properties mySqlProps) {
 		super(mySqlProps);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public void init(){
 		super.init();
 		addPrepStmt("addFilter"			, "INSERT IGNORE INTO filter (id, board, reason, status) VALUES (?,?,?,?)");
 		addPrepStmt("updateFilter"		, "UPDATE filter SET status = ? WHERE id = ?");
 		addPrepStmt("filterState"		, "SELECT status FROM filter WHERE  id = ?");
 		addPrepStmt("pendingFilter"		, "SELECT board, reason, id FROM filter WHERE status = 1 ORDER BY board, reason ASC");
 		addPrepStmt("filterTime"		, "UPDATE filter SET timestamp = ? WHERE id = ?");
 		addPrepStmt("oldestFilter"		, "SELECT id FROM filter ORDER BY timestamp ASC LIMIT 1");
 	}
 	
 	public boolean addFilter(FilterItem fi){
 		return addFilter(fi.getUrl().toString(), fi.getBoard(), fi.getReason(), fi.getState());
 	}
 	
 	/**
 	 * Adds a filter item to the database.
 	 * @param id id of the item
 	 * @param board board alias
 	 * @param reason reason for adding the filter
 	 * @param state initial state of the filter
 	 * @return true if the filter was added, else false
 	 */
 	public boolean addFilter(String id, String board, String reason, FilterState state){
 		reconnect();
 		try {
 			PreparedStatement addFilter = getPrepStmt("addFilter");
 			addFilter.setString(1, id);
 			addFilter.setString(2, board);
 			addFilter.setString(3, reason);
 			addFilter.setShort(4, (short) state.ordinal());
 			int res = addFilter.executeUpdate();
 	
 			if(res == 0){
 				logger.warning("filter already exists!");
 				return false;
 			}else{
 				return true;
 			}
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		}
 	
 		return false;
 	}
 	
 	public void updateState(String id, FilterState state){
 		reconnect();
 		PreparedStatement updateFilter = getPrepStmt("updateFilter");
 		try {
 			updateFilter.setShort(1, (short)state.ordinal());
 			updateFilter.setString(2, id);
 			updateFilter.executeUpdate();
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		}
 	}
 
 	public FilterState getFilterState(String id){
 		reconnect();
 		ResultSet rs = null;
 
 		try {
 			getPrepStmt("filterState").setString(1, id);
 			rs = prepStmtQuery("filterState");
 			if(rs.next()){
 				FilterState fs = FilterState.values()[(int)rs.getShort(1)];
 				return fs; 
 			}
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		}finally{
 			if(rs != null){
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					logger.warning(RS_CLOSE_ERR+e.getMessage());
 				}
 			}
 		}
 		return FilterState.UNKNOWN;
 	}
 
 	/**
 	 * Returns all items in the filter with state set to pending (1).
 	 * @return a list of all pending filter items
 	 */
 	public LinkedList<FilterItem> getPendingFilters(){
 		reconnect();
 		PreparedStatement pendingFilter = getPrepStmt("pendingFilter");
 		ResultSet rs = null;
 
 		try {
 			rs = pendingFilter.executeQuery();
 			LinkedList<FilterItem> result = new LinkedList<FilterItem>();
 			while(rs.next()){
 				URL url;
 				url = new URL(rs.getString("id"));
 
 				result.add(new FilterItem(url, rs.getString("board"), rs.getString("reason"),  FilterState.PENDING));
 			}
 			
 			return result;
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		} catch (MalformedURLException e) {
 			logger.warning("Unable to create URL "+e.getMessage());
 		}finally{
 			try {
 				if(rs != null){
 					rs.close();
 				}
 			} catch (SQLException e) {
 				logger.warning(RS_CLOSE_ERR+e.getMessage());
 			}
 		}
 		return new LinkedList<FilterItem>();
 	}
 	
 	public void updateFilterTimestamp(String id){
 		reconnect();
 		PreparedStatement updateTimestamp = getPrepStmt("filterTime");
 		try {
 			updateTimestamp.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
 			updateTimestamp.setString(2, id);
 			updateTimestamp.executeUpdate();
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		}
 	}
 	
 	public String getOldestFilter(){
 		reconnect();
 		ResultSet rs = null;
 		PreparedStatement getOldest = getPrepStmt("oldestFilter");
 
 		try {
 			rs = getOldest.executeQuery();
 			if(rs.next()){
 				String s = rs.getString(1);
 				rs.close();
 				return s;
 			}else {
 				rs.close();
 				return null;
 			}
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getLocalizedMessage());
 		}finally{
 			if(rs != null){
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					logger.warning(RS_CLOSE_ERR+e.getMessage());
 				}
 			}
 		}
 		return null;
 	}
 }
