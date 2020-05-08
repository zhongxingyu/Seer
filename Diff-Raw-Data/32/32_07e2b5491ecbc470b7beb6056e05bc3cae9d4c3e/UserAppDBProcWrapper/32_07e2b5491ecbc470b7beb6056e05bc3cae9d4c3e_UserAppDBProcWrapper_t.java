 package com.eucalyptus.webui.server.user;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.server.SorterProxy;
 import com.eucalyptus.webui.server.db.DBProcWrapper;
 import com.eucalyptus.webui.server.db.ResultSetWrapper;
 import com.eucalyptus.webui.shared.dictionary.ConfDef;
 import com.eucalyptus.webui.shared.dictionary.DBTableColName;
 import com.eucalyptus.webui.shared.dictionary.DBTableName;
 import com.eucalyptus.webui.shared.user.EnumUserAppStatus;
 import com.eucalyptus.webui.shared.user.UserApp;
 import com.eucalyptus.webui.shared.user.UserAppStateCount;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 
 public class UserAppDBProcWrapper {
 	public UserAppDBProcWrapper(SorterProxy sorterProxy) {
 		this.sorterProxy = sorterProxy;
 	}
 	
 	public void addUserApp(UserApp userApp) throws UserAppSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = addUserAppSql(userApp);
 		
 		System.out.println(sql);
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			throw new UserAppSyncException ("Database fails");
 		}
 	}
 	
 	public void updateUserApp(UserApp userApp) throws UserAppSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = updateUserAppSql(userApp);
 		
 		System.out.println(sql);
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			throw new UserAppSyncException ("Database fails");
 		}
 	}
 	
 	public UserApp lookupUserApp(int userAppId) throws UserAppSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		StringBuilder sql = lookupUserAppSql(userAppId);
 				
 		//EnumUserAppStatus.DEFAULT means that query all the user applications
 		System.out.println(sql.toString());
 		
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbProc.query(sql.toString());
 			ResultSet rs = rsw.getResultSet();
 			
 			if (!rs.wasNull()) {
 				rs.last();
 				
 				assert(rs.getRow() <= 1);
 				
 				if (rs.getRow() ==0) {
 					rsw.close();
 					throw new UserAppSyncException("User app not existed");
 				}
 				else
 				{	
 					int id = Integer.valueOf(rs.getString(DBTableColName.USER_APP.ID));
 					Date appTime = rs.getDate(DBTableColName.USER_APP.APP_TIME);
 					Date startingTime = rs.getDate(DBTableColName.USER_APP.SRV_STARTINGTIME);
 					Date endingTime = rs.getDate(DBTableColName.USER_APP.SRV_ENDINGTIME);
 					String state = rs.getString(DBTableColName.USER_APP.STATUS);
 					int del = rs.getInt(DBTableColName.USER_APP.DEL);
 					String keyPair = rs.getString(DBTableColName.USER_APP.KEYPAIR);
 					String securityGroup = rs.getString(DBTableColName.USER_APP.SECURITY_GROUP);
 					String comment = rs.getString(DBTableColName.USER_APP.COMMENT);
 					int userId = rs.getInt(DBTableColName.USER_APP.USER_ID);
 					int ncpus = rs.getInt(DBTableColName.USER_APP.NCPUS);
 					int mem = rs.getInt(DBTableColName.USER_APP.MEM);
 					int disk = rs.getInt(DBTableColName.USER_APP.DISK);
 					int bw = rs.getInt(DBTableColName.USER_APP.BW);
 					
 					int vitId = rs.getInt(DBTableColName.USER_APP.VM_IMAGE_TYPE_ID);
 					String euca_vi_key = rs.getString(DBTableColName.USER_APP.EUCA_VI_KEY);
 					
 					EnumUserAppStatus userAppRegStatus = EnumUserAppStatus.NONE;
 					if (!Strings.isNullOrEmpty(state))
 						userAppRegStatus = EnumUserAppStatus.values()[Integer.valueOf(state)];
 										
 					rsw.close();
 					
 					UserApp userApp = new UserApp(id, appTime, userAppRegStatus, del, comment,
 										ncpus, mem, disk, bw,
 										keyPair, securityGroup, userId, vitId, startingTime, endingTime, euca_vi_key);
 					
 					return userApp; 
 				}
 			}
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserAppSyncException("Fail to query user apps");
 		}
 		finally {
 			try {
 				rsw.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 	public ResultSetWrapper queryUserApp(int accountId, int userId, EnumUserAppStatus state, SearchRange range) throws UserAppSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		StringBuilder sql = userAppAccountUserViewSql();
 				
 		//EnumUserAppStatus.DEFAULT means that query all the user applications
 		if (state != EnumUserAppStatus.NONE) {
 			sql.append(" AND ").
 			append(DBTableName.USER_APP).
 			append(".").
 			append(DBTableColName.USER_APP.STATUS).
 			append(" = ").
 			append(state.ordinal());
 		}
 		
 		if (accountId != 0) {
 			sql.append(" AND ").
 			append(DBTableName.USER).
 			append(".").
 			append(DBTableColName.USER.ACCOUNT_ID).
 			append(" = ").
 			append(accountId);
 		}
 		
 		if (userId != 0) {
 			sql.append(" AND ").
 			append(DBTableName.USER_APP).
 			append(".").
 			append(DBTableColName.USER_APP.USER_ID).
 			append(" = ").
 			append(userId);
 		}
 		
 		if (this.sorterProxy != null) {
 			String orderBy = this.sorterProxy.orderBy(range);
 			if (orderBy != null)
 				sql.append(orderBy);
 		}
 		
 		System.out.println(sql.toString());
 		
 		try {
 			ResultSetWrapper result = dbProc.query(sql.toString());
 			
 			return result;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserAppSyncException("Fail to query user apps");
 		}
 	}
 	
 	public void delUserApps(ArrayList<String> ids) throws UserAppSyncException {
 		if (ids == null || ids.size() == 0)
 			return;
 		
 		String sql = delUserAppSql(ids);
 		
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserAppSyncException("Database fails");
 		}
 	}
 	
 	public void updateUserState(ArrayList<String> ids, EnumUserAppStatus state) throws UserAppSyncException {
 		if (ids == null || ids.size() == 0)
 			return;
 		
 		String sql = updateUserAppStateSql(ids, state);
 		
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserAppSyncException("Database fails");
 		}
 	}
 	
 	public ArrayList<UserAppStateCount> countUserAppByState(int accountId, int userId) throws UserAppSyncException {
 		String sql = countUserAppByStateSql(accountId, userId);
 		
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		try {
 			ResultSetWrapper rsw = dbProc.query(sql);
 			ResultSet rs = rsw.getResultSet();
 			
             if (rs != null) {
             	ArrayList<UserAppStateCount> counts = Lists.newArrayList();
             	
             	while (rs.next()) { 
             		
             		EnumUserAppStatus appState = EnumUserAppStatus.NONE;
 					
 					String appStateStr = rs.getString(DBTableColName.USER_APP.STATUS);
 					if (!Strings.isNullOrEmpty(appStateStr))
 						appState = EnumUserAppStatus.values()[Integer.valueOf(appStateStr)];
 					
 					int count = 0;
 					String countStr = rs.getString("count");
 					if (!Strings.isNullOrEmpty(countStr))
 						count = Integer.valueOf(countStr);
 						
 					UserAppStateCount userAppState = new UserAppStateCount();
 					userAppState.setCountValue(appState, count);
 					
 					counts.add(userAppState);
                 }
             	
             	rsw.close();
             	return counts;
             } 
             else {	
 				rsw.close();
 				return null;
 			}
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserAppSyncException("Database fails");
 		}
 	}
 	
 	private String addUserAppSql(UserApp userApp) {
 		StringBuilder str = new StringBuilder();
 		str.append("INSERT INTO ").
 		append(DBTableName.USER_APP).append(" ( ").
 		append(DBTableColName.USER_APP.ID).append(", ").
 		append(DBTableColName.USER_APP.APP_TIME).append(", ").
 		append(DBTableColName.USER_APP.SRV_STARTINGTIME).append(", ").
 		append(DBTableColName.USER_APP.SRV_ENDINGTIME).append(", ").
 		append(DBTableColName.USER_APP.STATUS).append(", ").
 		append(DBTableColName.USER_APP.DEL).append(", ").
 		append(DBTableColName.USER_APP.KEYPAIR).append(", ").
 		append(DBTableColName.USER_APP.SECURITY_GROUP).append(", ").
 		append(DBTableColName.USER_APP.COMMENT).append(", ").
 		append(DBTableColName.USER_APP.NCPUS).append(", ").
 		append(DBTableColName.USER_APP.MEM).append(", ").
 		append(DBTableColName.USER_APP.DISK).append(", ").
 		append(DBTableColName.USER_APP.BW).append(", ").
 		append(DBTableColName.USER_APP.USER_ID).append(", ").
 		append(DBTableColName.USER_APP.VM_IMAGE_TYPE_ID).
 		append(") VALUES (null, ");
 		
 		str.append("'");
 		str.append(dateformat.format(userApp.getAppTime()).toString());
 		str.append("', '");
 		
 		str.append(dateformat.format(userApp.getSrvStartingTime()).toString());
 		str.append("', '");
 		
 		str.append(dateformat.format(userApp.getSrvEndingTime()).toString());
 		str.append("', ");
 		
 		str.append(userApp.getStatus().ordinal());
 		str.append(", ");
 		
 		str.append(ConfDef.DB_DEL_FIELD_VALID_STATE);
 		str.append(", ");
 		
 		if (Strings.isNullOrEmpty(userApp.getKeyPair())) {
 			str.append("null, ");
 		}
 		else {
 			str.append("'").append(userApp.getKeyPair());
 			str.append("', ");
 		}
 		
 		if (Strings.isNullOrEmpty(userApp.getSecurityGroup())) {
 			str.append("null, ");
 		}
 		else {
 			str.append("'").append(userApp.getSecurityGroup());
 			str.append("', ");
 		}
 		
 		if (Strings.isNullOrEmpty(userApp.getComments())) {
 			str.append("null, ");
 		}
 		else {
 			str.append("'").append(userApp.getComments());
 			str.append("', ");
 		}
 		
 		str.append(userApp.getNcpus());
 		str.append(", ");
 		
 		str.append(userApp.getMem());
 		str.append(", ");
 		
 		str.append(userApp.getDisk());
 		str.append(", ");
 		
 		str.append(userApp.getBw());
 		str.append(", ");
 		
 		str.append(userApp.getUserId());
 		str.append(", ");
 		
 		str.append(userApp.getVmIdImageTypeId());
 		
 		str.append(")");
 		
 		return str.toString();
 	}
 	
 	private String updateUserAppSql(UserApp userApp) {
 		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.USER_APP).append(" SET ");
 		
 		if (userApp.getStatus() != EnumUserAppStatus.NONE) {
 			str.append(DBTableColName.USER_APP.STATUS).append(" = '").
 			append(userApp.getStatus().ordinal()).
 			append("', ");
 		}
 		
 		if (userApp.getAppTime() != null) {
 			str.append(DBTableColName.USER_APP.APP_TIME).append(" = '").
 			append(dateformat.format(userApp.getAppTime()).toString()).
 			append("', ");
 		}
 		
 		if (userApp.getSrvStartingTime() != null) {
 			str.append(DBTableColName.USER_APP.SRV_STARTINGTIME).append(" = '").
 			append(dateformat.format(userApp.getSrvStartingTime()).toString()).
 			append("', ");
 		}
 		
 		if (userApp.getSrvEndingTime() != null) {
 			str.append(DBTableColName.USER_APP.SRV_ENDINGTIME).append(" = '").
 			append(dateformat.format(userApp.getSrvEndingTime())).
 			append("', ");
 		}
 		
 		if (userApp.getKeyPair() != null) {
 			str.append(DBTableColName.USER_APP.KEYPAIR).append(" = '").
 			append(userApp.getKeyPair()).
 			append("', ");
 		}
 		
 		if (userApp.getSecurityGroup() != null) {
 			str.append(DBTableColName.USER_APP.SECURITY_GROUP).append(" = '").
 			append(userApp.getSecurityGroup()).
 			append("', ");
 		}
 		
 		if (userApp.getComments() != null) {
 			str.append(DBTableColName.USER_APP.COMMENT).append(" = '").
 			append(userApp.getComments()).
 			append("', ");
 		}
 		
 		if (userApp.getNcpus() != 0) {
 			str.append(DBTableColName.USER_APP.NCPUS).append(" = ").
 			append(userApp.getNcpus()).
 			append(", ");
 		}
 		
 		if (userApp.getMem() != 0) {
 			str.append(DBTableColName.USER_APP.MEM).append(" = ").
 			append(userApp.getMem()).
 			append(", ");
 		}
 		
 		if (userApp.getDisk() != 0) {
 			str.append(DBTableColName.USER_APP.DISK).append(" = ").
 			append(userApp.getDisk()).
 			append(", ");
 		}
 		
 		if (userApp.getBw() != 0) {
 			str.append(DBTableColName.USER_APP.BW).append(" = ").
 			append(userApp.getBw()).
 			append(", ");
 		}
 		
 		if (userApp.getVmIdImageTypeId() != 0) {
 			str.append(DBTableColName.USER_APP.USER_ID).append(" = ").
 			append(userApp.getVmIdImageTypeId()).
 			append(", ");
 		}
 		
 		if (userApp.getEucaVMInstanceKey() != null) {
 			str.append(DBTableColName.USER_APP.EUCA_VI_KEY).append(" = '").
 			append(userApp.getEucaVMInstanceKey()).
 			append("', ");
 		}
 		
 		if (str.length() > 2)
 			str.delete(str.length() -2, str.length());
 		
 		str.append(" WHERE ").append(DBTableColName.USER_APP.ID).append(" = ").
 		append(userApp.getUAId());
 		
 		return str.toString();
 	}
 	
 	private StringBuilder lookupUserAppSql(int userAppId) {
 		StringBuilder sql = new StringBuilder("SELECT * ").
 				
 				append(" FROM ").
 				append(DBTableName.USER_APP).
 				
 				append(" WHERE ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.DEL).
 				append(" = 0 ").
 				append(" AND ").
 				append(DBTableColName.USER_APP.ID).
 				append(" = ").
 				append(userAppId);
 		
 		return sql;
 	}
 	
 	private StringBuilder userAppAccountUserViewSql() {
 		StringBuilder sql = new StringBuilder("SELECT ").
 				append(DBTableName.USER_APP).append(".*").
 				append(", ").
 				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME).
 				append(", ").
 				append(DBTableName.USER).append(".*").
 				append(", ").
 				append(DBTableName.VM_IMAGE_TYPE).append(".*").
 				
 				append(" FROM ").
 				append("( ").
 				append(DBTableName.USER_APP).
 				
 				append(" LEFT JOIN ").
 				append(DBTableName.USER).
 				append(" ON ").
 				append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
 				append(" = ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.USER_ID).
 				append(" ) ").
 				
 				append(" LEFT JOIN ").
 				append(DBTableName.ACCOUNT).
 				append(" ON ").
 				append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
 				append(" = ").
 				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
 				
 				append(" LEFT JOIN ").
 				append(DBTableName.VM_IMAGE_TYPE).
 				append(" ON ").
 				append(DBTableName.VM_IMAGE_TYPE).append(".").append(DBTableColName.VM_IMAGE_TYPE.ID).
 				append(" = ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.VM_IMAGE_TYPE_ID).
 				
 				append(" WHERE ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.DEL).
 				append(" = 0 ");
 		
 		return sql;
 	}
 	
 	private String delUserAppSql(ArrayList<String> ids) {
 
 		StringBuilder sql = new StringBuilder("UPDATE ").append(DBTableName.USER_APP).append(" SET ");
 		
 		sql.append(DBTableColName.USER_APP.DEL).append(" = 1 ");
 		sql.append(" WHERE ");
 		
 		for (String str : ids) {
 			sql.append(DBTableColName.USER_APP.ID).append(" = ").
 			append(str).
 			append(" OR ");
 		}
 		
 		sql.delete(sql.length() -3 , sql.length());
 		
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 	
 	private String updateUserAppStateSql(ArrayList<String> ids, EnumUserAppStatus state) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE ").
 		append(DBTableName.USER_APP).
 		append(" SET ").
 		append(DBTableColName.USER_APP.STATUS).
 		append(" = ").
 		append(state.ordinal()).
 		append(" WHERE ");
 		
 		for (String str : ids) {
 			sql.append(DBTableColName.USER_APP.ID).
 			append(" = '").
 			append(str).
 			append("' or ");
 		}
 		
 		sql.delete(sql.length() -3 , sql.length());
 		
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 	
 	private String countUserAppByStateSql(int accountId, int userId) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT ").
 		append(DBTableColName.USER_APP.STATUS).append(", ").
 		append(" COUNT(*) AS count").
 		append(" FROM ").
 		append("( ").
 		append(DBTableName.USER_APP).
 		append(" LEFT JOIN ").
 		append(DBTableName.USER).
 		append(" ON ").
 		append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
 		append(" = ").
 		append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.USER_ID).
 		append(" ) ").
 		append(" LEFT JOIN ").
 		append(DBTableName.ACCOUNT).
 		append(" ON ").
 		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
 		append(" = ").
 		append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
 		append(" WHERE 1=1 AND ").
 		append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.DEL).append(" = 0 ");;
 		
 		if (accountId > 0)
 			sql.append(" AND ").append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).append(" = ").append(accountId);
 		
 		if (userId > 0)
 			sql.append(" AND ").append(DBTableName.USER).append(".").append(DBTableColName.USER_APP.USER_ID).append(" = ").append(userId);
 		
 		sql.append(" GROUP BY ").
 		append(DBTableColName.USER_APP.STATUS);
 				
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 	
 	SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
 	SorterProxy sorterProxy;
 }
