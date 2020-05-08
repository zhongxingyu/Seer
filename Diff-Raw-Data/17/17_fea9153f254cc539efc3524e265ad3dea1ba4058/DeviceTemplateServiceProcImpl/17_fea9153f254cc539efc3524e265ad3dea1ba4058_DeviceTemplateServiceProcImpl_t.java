 package com.eucalyptus.webui.server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.eucalyptus.webui.client.activity.DeviceCPUActivity.CPUState;
 import com.eucalyptus.webui.client.activity.DeviceDiskActivity.DiskState;
 import com.eucalyptus.webui.client.activity.DeviceMemoryActivity.MemoryState;
 import com.eucalyptus.webui.client.service.EucalyptusServiceException;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
 import com.eucalyptus.webui.client.session.Session;
 import com.eucalyptus.webui.server.db.DBProcWrapper;
 import com.eucalyptus.webui.server.db.ResultSetWrapper;
 import com.eucalyptus.webui.server.dictionary.DBTableColName;
 import com.eucalyptus.webui.server.dictionary.DBTableName;
 import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
 import com.eucalyptus.webui.shared.resource.Template;
 import com.eucalyptus.webui.shared.user.LoginUserProfile;
 
 class DeviceTemplateDBProcWrapper {
 
 	private static final Logger LOG = Logger.getLogger(DeviceTemplateDBProcWrapper.class.getName());
 
 	DBProcWrapper wrapper = DBProcWrapper.Instance();
 
 	ResultSetWrapper doQuery(String request) throws Exception {
 		LOG.info(request);
 		return wrapper.query(request);
 	}
 
 	void doUpdate(String request) throws Exception {
 		LOG.info(request);
 		wrapper.update(request);
 	}
 
 	ResultSetWrapper queryAllTemplates(String starttime, String endtime) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT * FROM ");
 		sb.append(DBTableName.TEMPLATE);
 		sb.append(" WHERE 1=1 ");
 		
 		if (starttime != null) {
 			sb.append(" AND ");
 			sb.append(DBTableColName.TEMPLATE.STARTTIME).append(" >= ").append("\"").append(starttime).append("\"");
 		}
 		if (endtime != null) {
 			sb.append(" AND ");
 			sb.append(DBTableColName.TEMPLATE.STARTTIME).append(" <= ").append("\"").append(endtime).append("\"");
 		}
 		return doQuery(sb.toString());
 	}
 	
 	ResultSetWrapper lookupTemplateByID(int template_id) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT * FROM ").append(DBTableName.TEMPLATE).append(" WHERE ").append(DBTableColName.TEMPLATE.ID).append(" = ").append(template_id);
 		return doQuery(sb.toString());
 	}
 
 	ResultSetWrapper queryTemplate(int template_id) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT * FROM ");
 		sb.append(DBTableName.TEMPLATE);
 		sb.append(" WHERE ");
 		sb.append(DBTableColName.TEMPLATE.ID);
 		sb.append(" = ").append(template_id);
 		return doQuery(sb.toString());
 	}
 	
 	private boolean isEmpty(String s) {
 		return s == null || s.length() == 0;
 	}
 	
 	ResultSetWrapper lookupCPUService(String cpu, int server_id) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT ");
 		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID).append(", ");
 		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.ID);
 		sb.append(" FROM ");
 		sb.append(DBTableName.CPU).append(" LEFT JOIN ").append(DBTableName.CPU_SERVICE);
 		sb.append(" ON ");
 		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID);
 		sb.append(" = ");
 		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.CPU_ID);
 		sb.append(" WHERE ");
 		sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.STATE);
 		sb.append(" = ").append(CPUState.RESERVED.getValue());
 		sb.append(" AND ");
 		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.SERVER_ID);
 		sb.append(" = ").append("\"").append(server_id).append("\"");
 		sb.append(" AND ");
 		sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.NAME);
 		sb.append(" = ").append("\"").append(cpu).append("\"");
 		return doQuery(sb.toString());
 	}
 	
 	void addCPUService(int cs_id, int user_id, String cs_starttime, int cs_life, int cs_state) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("UPDATE ");
 		sb.append(DBTableName.CPU_SERVICE);
 		sb.append(" SET ");
 
 		sb.append(DBTableColName.CPU_SERVICE.USER_ID).append(" = ").append("\"").append(user_id).append("\", ");
 		sb.append(DBTableColName.CPU_SERVICE.STARTTIME).append(" = ").append("\"").append(cs_starttime).append("\", ");
 		sb.append(DBTableColName.CPU_SERVICE.LIFE).append(" = ").append(cs_life).append(", ");
 		sb.append(DBTableColName.CPU_SERVICE.STATE).append(" = ").append(cs_state);
 		sb.append(" WHERE ");
 		sb.append(DBTableColName.CPU_SERVICE.ID).append(" = ").append(cs_id);
 		doUpdate(sb.toString());
 	}
 	
 	ResultSetWrapper lookupMemoryDevice(long memory, int server_id) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT ");
 		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.ID).append(", ");
 		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.ID);
 		sb.append(" FROM ");
 		sb.append(DBTableName.MEMORY).append(" LEFT JOIN ").append(DBTableName.MEM_SERVICE);
 		sb.append(" ON ");
 		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.ID);
 		sb.append(" = ");
 		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.MEM_ID);
 		sb.append(" WHERE ");
 		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.STATE);
 		sb.append(" = ").append(MemoryState.RESERVED.getValue());
 		sb.append(" AND ");
 		sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.SERVER_ID);
 		sb.append(" = ").append("\"").append(server_id).append("\"");
 		sb.append(" AND ");
 		sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.USED);
 		sb.append(" >= ").append("\"").append(memory).append("\"");
 		return doQuery(sb.toString());
 	}
 	
 	void addMemoryService(int ms_id, int mem_id, int user_id, long ms_used, String ms_starttime,
 			int ms_life, int ms_state) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("INSERT INTO ").append(DBTableName.MEM_SERVICE).append(" (");
 		sb.append(DBTableColName.MEM_SERVICE.USED).append(", ");
 		sb.append(DBTableColName.MEM_SERVICE.STARTTIME).append(", ");
 		sb.append(DBTableColName.MEM_SERVICE.LIFE).append(", ");
 		sb.append(DBTableColName.MEM_SERVICE.STATE).append(", ");
 		sb.append(DBTableColName.MEM_SERVICE.USER_ID).append(", ");
 		sb.append(DBTableColName.MEM_SERVICE.MEM_ID).append(") VALUES (");
 		sb.append(0).append(", ");
 		sb.append("\"").append(ms_starttime).append("\", ");
 		sb.append(ms_life).append(", ");
 		sb.append(ms_state).append(", ");
 		sb.append(user_id).append(", ");
 		sb.append(mem_id).append(")");
 		doUpdate(sb.toString());
 		
 		sb = new StringBuilder();
 		sb.append("SELECT MAX(").append(DBTableColName.MEM_SERVICE.ID).append(") FROM ").append(DBTableName.MEM_SERVICE);
 		
 		ResultSetWrapper rsw = null;
 		int new_ms_id = -1;
 		try {
 			rsw = doQuery(sb.toString());
 			ResultSet rs = rsw.getResultSet();
 			if (rs.next()) {
 				new_ms_id = rs.getInt(1);
 			}
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 				throw e;
 			}
 		}
 		assert (new_ms_id != -1);
 		
 		sb = new StringBuilder();
 		sb.append("UPDATE ");
 		sb.append(DBTableName.MEM_SERVICE).append(" A, ");
 		sb.append(DBTableName.MEM_SERVICE).append(" B");
 		sb.append(" SET ");
 		sb.append("A.").append(DBTableColName.MEM_SERVICE.USED).append(" = ").append(ms_used).append(", ");
 		sb.append("B.").append(DBTableColName.MEM_SERVICE.USED).append(" = ");
 		sb.append("(B.").append(DBTableColName.MEM_SERVICE.USED).append(" - ").append(ms_used).append(")");
 		sb.append(" WHERE ");
 		sb.append("A.").append(DBTableColName.MEM_SERVICE.ID).append(" = ").append(new_ms_id);
 		sb.append(" AND ");
 		sb.append("B.").append(DBTableColName.MEM_SERVICE.ID).append(" = ").append(ms_id);
 		sb.append(" AND ");
 		sb.append("A.").append(DBTableColName.MEM_SERVICE.MEM_ID).append(" = ");
 		sb.append("B.").append(DBTableColName.MEM_SERVICE.MEM_ID);
 		sb.append(" AND ");
 		sb.append("B.").append(DBTableColName.MEM_SERVICE.USED).append(" >= ").append(ms_used);
 		doUpdate(sb.toString());
 		
 		sb = new StringBuilder();
 		sb.append("DELETE FROM ");
 		sb.append(DBTableName.MEM_SERVICE);
 		sb.append(" WHERE ");
 		sb.append(DBTableColName.MEM_SERVICE.USED).append(" = ").append(0);
 		sb.append(" AND ");
 		sb.append(DBTableColName.MEM_SERVICE.STATE).append(" != ").append(MemoryState.RESERVED.getValue());
 		doUpdate(sb.toString());
 	}
 		
 	ResultSetWrapper lookupDiskDevice(long disk, int server_id) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT ");
 		sb.append(DBTableName.DISK).append(".").append(DBTableColName.DISK.ID).append(", ");
 		sb.append(DBTableName.DISK_SERVICE).append(".").append(DBTableColName.DISK_SERVICE.ID);
 		sb.append(" FROM ");
 		sb.append(DBTableName.DISK).append(" LEFT JOIN ").append(DBTableName.DISK_SERVICE);
 		sb.append(" ON ");
 		sb.append(DBTableName.DISK).append(".").append(DBTableColName.DISK.ID);
 		sb.append(" = ");
 		sb.append(DBTableName.DISK_SERVICE).append(".").append(DBTableColName.DISK_SERVICE.DISK_ID);
 		sb.append(" WHERE ");
 		sb.append(DBTableName.DISK_SERVICE).append(".").append(DBTableColName.DISK_SERVICE.STATE);
 		sb.append(" = ").append(DiskState.RESERVED.getValue());
 		sb.append(" AND ");
 		sb.append(DBTableName.DISK).append(".").append(DBTableColName.DISK.SERVER_ID);
 		sb.append(" = ").append("\"").append(server_id).append("\"");
 		sb.append(" AND ");
 		sb.append(DBTableName.DISK_SERVICE).append(".").append(DBTableColName.DISK_SERVICE.USED);
 		sb.append(" >= ").append("\"").append(disk).append("\"");
 		return doQuery(sb.toString());
 	}
 	
 	void addDiskService(int ds_id, int disk_id, int user_id, long ds_used, String ds_starttime,
 			int ds_life, int ds_state) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("INSERT INTO ").append(DBTableName.DISK_SERVICE).append(" (");
 		sb.append(DBTableColName.DISK_SERVICE.USED).append(", ");
 		sb.append(DBTableColName.DISK_SERVICE.STARTTIME).append(", ");
 		sb.append(DBTableColName.DISK_SERVICE.LIFE).append(", ");
 		sb.append(DBTableColName.DISK_SERVICE.STATE).append(", ");
 		sb.append(DBTableColName.DISK_SERVICE.USER_ID).append(", ");
 		sb.append(DBTableColName.DISK_SERVICE.DISK_ID).append(") VALUES (");
 		sb.append(0).append(", ");
 		sb.append("\"").append(ds_starttime).append("\", ");
 		sb.append(ds_life).append(", ");
 		sb.append(ds_state).append(", ");
 		sb.append(user_id).append(", ");
 		sb.append(disk_id).append(")");
 		doUpdate(sb.toString());
 		
 		sb = new StringBuilder();
 		sb.append("SELECT MAX(").append(DBTableColName.DISK_SERVICE.ID).append(") FROM ").append(DBTableName.DISK_SERVICE);
 		
 		ResultSetWrapper rsw = null;
 		int new_ms_id = -1;
 		try {
 			rsw = doQuery(sb.toString());
 			ResultSet rs = rsw.getResultSet();
 			if (rs.next()) {
 				new_ms_id = rs.getInt(1);
 			}
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 				throw e;
 			}
 		}
 		assert (new_ms_id != -1);
 		
 		sb = new StringBuilder();
 		sb.append("UPDATE ");
 		sb.append(DBTableName.DISK_SERVICE).append(" A, ");
 		sb.append(DBTableName.DISK_SERVICE).append(" B");
 		sb.append(" SET ");
 		sb.append("A.").append(DBTableColName.DISK_SERVICE.USED).append(" = ").append(ds_used).append(", ");
 		sb.append("B.").append(DBTableColName.DISK_SERVICE.USED).append(" = ");
 		sb.append("(B.").append(DBTableColName.DISK_SERVICE.USED).append(" - ").append(ds_used).append(")");
 		sb.append(" WHERE ");
 		sb.append("A.").append(DBTableColName.DISK_SERVICE.ID).append(" = ").append(new_ms_id);
 		sb.append(" AND ");
 		sb.append("B.").append(DBTableColName.DISK_SERVICE.ID).append(" = ").append(ds_id);
 		sb.append(" AND ");
 		sb.append("A.").append(DBTableColName.DISK_SERVICE.DISK_ID).append(" = ");
 		sb.append("B.").append(DBTableColName.DISK_SERVICE.DISK_ID);
 		sb.append(" AND ");
 		sb.append("B.").append(DBTableColName.DISK_SERVICE.USED).append(" >= ").append(ds_used);
 		doUpdate(sb.toString());
 		
 		sb = new StringBuilder();
 		sb.append("DELETE FROM ");
 		sb.append(DBTableName.DISK_SERVICE);
 		sb.append(" WHERE ");
 		sb.append(DBTableColName.DISK_SERVICE.USED).append(" = ").append(0);
 		sb.append(" AND ");
 		sb.append(DBTableColName.DISK_SERVICE.STATE).append(" != ").append(DiskState.RESERVED.getValue());
 		doUpdate(sb.toString());
 	}
 	
 	ResultSetWrapper lookupServer(String cpu, int ncpu, long memory, long disk) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("SELECT DISTINCT(A.").append(DBTableColName.SERVER.ID).append(") FROM ");
 		
 		if (isEmpty(cpu) || ncpu <= 0) {
 			sb.append("(SELECT ");
 			sb.append(DBTableColName.SERVER.ID).append(" FROM ").append(DBTableName.SERVER);
 			sb.append(") AS A");
 		}
 		else {
 			sb.append("(SELECT ");
 			sb.append(DBTableColName.CPU.SERVER_ID).append(" FROM ");
 			sb.append("(SELECT ").append(DBTableColName.CPU.SERVER_ID).append(", ");
 			sb.append("COUNT(").append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID).append(") AS COUNT FROM ");
 			sb.append(DBTableName.CPU).append(" LEFT JOIN ").append(DBTableName.CPU_SERVICE);
 			sb.append(" ON ");
 			sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.ID);
 			sb.append(" = ");
 			sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.CPU_ID);
 			sb.append(" WHERE ");
 			sb.append(DBTableName.CPU_SERVICE).append(".").append(DBTableColName.CPU_SERVICE.STATE);
 			sb.append(" = ").append(CPUState.RESERVED.getValue());
 			sb.append(" AND ");
 			sb.append(DBTableName.CPU).append(".").append(DBTableColName.CPU.NAME);
 			sb.append(" = ").append("\"").append(cpu).append("\"");
 			sb.append(" GROUP BY ");
 			sb.append(DBTableColName.CPU.SERVER_ID).append(") AS A0");
 			sb.append(" WHERE ");
 			sb.append("A0.COUNT >= ").append(ncpu).append(") AS A");
 		}
 		
 		if (memory > 0) {
 			sb.append(" LEFT JOIN ");
 			sb.append("(SELECT DISTINCT(");
 			sb.append(DBTableColName.MEMORY.SERVER_ID).append(") FROM ");
 			sb.append(DBTableName.MEMORY).append(" LEFT JOIN ").append(DBTableName.MEM_SERVICE);
 			sb.append(" ON ");
 			sb.append(DBTableName.MEMORY).append(".").append(DBTableColName.MEMORY.ID);
 			sb.append(" = ");
 			sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.MEM_ID);
 			sb.append(" WHERE ");
 			sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.STATE);
 			sb.append(" = ").append(MemoryState.RESERVED.getValue());
 			sb.append(" AND ");
 			sb.append(DBTableName.MEM_SERVICE).append(".").append(DBTableColName.MEM_SERVICE.USED);
 			sb.append(" >= ").append("\"").append(memory).append("\") AS B");
 			sb.append(" ON A.").append(DBTableColName.SERVER.ID).append(" = B.").append(DBTableColName.SERVER.ID);
 		}
 		
 		if (disk > 0) {
 			sb.append(" LEFT JOIN ");
 			sb.append("(SELECT DISTINCT(");
 			sb.append(DBTableColName.DISK.SERVER_ID).append(") FROM ");
 			sb.append(DBTableName.DISK).append(" LEFT JOIN ").append(DBTableName.DISK_SERVICE);
 			sb.append(" ON ");
 			sb.append(DBTableName.DISK).append(".").append(DBTableColName.DISK.ID);
 			sb.append(" = ");
 			sb.append(DBTableName.DISK_SERVICE).append(".").append(DBTableColName.DISK_SERVICE.DISK_ID);
 			sb.append(" WHERE ");
 			sb.append(DBTableName.DISK_SERVICE).append(".").append(DBTableColName.DISK_SERVICE.STATE);
 			sb.append(" = ").append(DiskState.RESERVED.getValue());
 			sb.append(" AND ");
 			sb.append(DBTableName.DISK_SERVICE).append(".").append(DBTableColName.DISK_SERVICE.USED);
 			sb.append(" >= ").append("\"").append(disk).append("\") AS C");
 			sb.append(" ON A.").append(DBTableColName.SERVER.ID).append(" = C.").append(DBTableColName.SERVER.ID);
 		}
 		
 		sb.append(" WHERE 1=1");
 		return doQuery(sb.toString());
 	}
 
 	void modifyTemplate(int template_id, String cpu, int ncpus, String mem, String disk, String bw, String image) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("UPDATE ");
 		sb.append(DBTableName.TEMPLATE);
 		sb.append(" SET ");
 
 		sb.append(DBTableColName.TEMPLATE.CPU).append(" = ").append("\"").append(cpu).append("\"").append(", ");
 		sb.append(DBTableColName.TEMPLATE.NCPUS).append(" = ").append("\"").append(ncpus).append("\"").append(", ");
 		sb.append(DBTableColName.TEMPLATE.MEM).append(" = ").append("\"").append(mem).append("\"").append(", ");
 		sb.append(DBTableColName.TEMPLATE.DISK).append(" = ").append("\"").append(disk).append("\"").append(", ");
 		sb.append(DBTableColName.TEMPLATE.BW).append(" = ").append("\"").append(bw).append("\"").append(", ");
 		sb.append(DBTableColName.TEMPLATE.IMAGE).append(" = ").append("\"").append(image).append("\"");
 		
 		sb.append(" WHERE ");
 		sb.append(DBTableColName.TEMPLATE.ID).append(" = ").append(template_id);
 		doUpdate(sb.toString());
 	}
 
 	void addTemplate(String mark, String cpu, int ncpus, String mem, String disk, String bw, String image, String starttime)
 	        throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("INSERT INTO ").append(DBTableName.TEMPLATE).append(" (");
 		sb.append(DBTableColName.TEMPLATE.MARK).append(", ");
 		sb.append(DBTableColName.TEMPLATE.CPU).append(", ");
 		sb.append(DBTableColName.TEMPLATE.NCPUS).append(", ");
 		sb.append(DBTableColName.TEMPLATE.MEM).append(", ");
 		sb.append(DBTableColName.TEMPLATE.DISK).append(", ");
 		sb.append(DBTableColName.TEMPLATE.BW).append(", ");
 		sb.append(DBTableColName.TEMPLATE.IMAGE).append(", ");
 		sb.append(DBTableColName.TEMPLATE.STARTTIME).append(")");
 		sb.append(" VALUES ").append("(");
 		sb.append("\"").append(mark).append("\", ");
 		sb.append("\"").append(cpu).append("\", ");
 		sb.append("\"").append(ncpus).append("\", ");
 		sb.append("\"").append(mem).append("\", ");
 		sb.append("\"").append(disk).append("\", ");
 		sb.append("\"").append(bw).append("\", ");
 		sb.append("\"").append(image).append("\", ");
 		sb.append("\"").append(starttime).append("\")");
 		doUpdate(sb.toString());
 	}
 
 	void deleteTemplate(int template_id) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("DELETE FROM ");
 		sb.append(DBTableName.TEMPLATE);
 		sb.append(" WHERE ").append(DBTableColName.TEMPLATE.ID).append(" = ").append(template_id);
 		doUpdate(sb.toString());
 	}
 	
 	ResultSetWrapper listCPUNames() throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT DISTINCT ").append(DBTableColName.CPU.NAME).append(" FROM ").append(DBTableName.CPU);
 		sb.append(" WHERE 1=1");
 		return doQuery(sb.toString());
 	}
 
 }
 
 public class DeviceTemplateServiceProcImpl {
 
 	private DeviceTemplateDBProcWrapper dbproc = new DeviceTemplateDBProcWrapper();
 
 	private LoginUserProfile getUser(Session session) {
 		return LoginUserProfileStorer.instance().get(session.getId());
 	}
 	
 	private boolean isEmpty(String s) {
 		return s == null || s.length() == 0;
 	}
 
 	public SearchResult lookupTemplate(Session session, String search, SearchRange range, Date starttime, Date endtime) {
 		ResultSetWrapper rsw = null;
 		try {
 //			if (!getUser(session).isSystemAdmin()) {
 //				return null;
 //			}
 			
 			String sstarttime = null;
 			if (starttime != null) {
 				sstarttime = formatter.format(starttime);
 			}
 			String sendtime = null;
 			if (endtime != null) {
 				sendtime = formatter.format(endtime);
 			}
 			rsw = dbproc.queryAllTemplates(sstarttime, sendtime);
 			List<SearchResultRow> rows = convertResults(rsw);
 			if (rows != null) {
 				int length = Math.min(range.getLength(), rows.size() - range.getStart());
 				SearchResult result = new SearchResult(rows.size(), range);
 				result.setDescs(FIELDS_ROOT);
 				int from = range.getStart(), to = range.getStart() + length;
 				if (from < to) {
 					result.setRows(rows.subList(from, to));
 				}
 				for (SearchResultRow row : result.getRows()) {
 					System.out.println(row);
 				}
 				return result;
 			}
 			return null;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public SearchResultRow modifyTemplate(Session session, SearchResultRow row, String cpu, int ncpus, 
 			String mem, String disk, String bw, String image) {
 		try {
 			if (!getUser(session).isSystemAdmin()) {
 				return null;
 			}
 			int template_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_TEMPLATE_ID));
 			modifyTemplate(template_id, cpu, ncpus, mem, disk, bw, image);
 			return lookupService(row, template_id);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private SearchResultRow lookupService(SearchResultRow row, int template_id) throws Exception {
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbproc.queryTemplate(template_id);
 			ResultSet rs = rsw.getResultSet();
 			if (!rs.next()) {
 				return null;
 			}
 			String cpu = rs.getString(DBTableColName.TEMPLATE.CPU);
 			String ncpus = rs.getString(DBTableColName.TEMPLATE.NCPUS);
 			if (isEmpty(cpu)) {
 				ncpus = "";
 			}
 			row.setField(TABLE_COL_INDEX_CPU, cpu);
 			row.setField(TABLE_COL_INDEX_NCPUS, ncpus);
 			row.setField(TABLE_COL_INDEX_MEM, rs.getString(DBTableColName.TEMPLATE.MEM));
 			row.setField(TABLE_COL_INDEX_DISK, rs.getString(DBTableColName.TEMPLATE.DISK));
 			row.setField(TABLE_COL_INDEX_BW, rs.getString(DBTableColName.TEMPLATE.BW));
 			row.setField(TABLE_COL_INDEX_IMAGE, rs.getString(DBTableColName.TEMPLATE.IMAGE));
 			return row;
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 				throw e;
 			}
 		}
 	}
 
 	public List<SearchResultRow> deleteTemplate(Session session, List<SearchResultRow> list) {
 		try {
 			if (list == null) {
 				return null;
 			}
 			if (!getUser(session).isSystemAdmin()) {
 				return null;
 			}
 			List<SearchResultRow> result = new ArrayList<SearchResultRow>();
 			for (SearchResultRow row : list) {
 				int template_id = Integer.parseInt(row.getField(TABLE_COL_INDEX_TEMPLATE_ID));
 				if (!deleteTemplate(template_id)) {
 					break;
 				}
 				result.add(row);
 			}
 			return result;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public boolean addTemplate(Session session, String mark, String cpu, int ncpus, String mem, String disk,
 			String bw, String image) {
 		try {
 			if (!getUser(session).isSystemAdmin()) {
 				return false;
 			}
 			if (isEmpty(mark)) {
 				return false;
 			}
 			addTemplate(mark, cpu, ncpus, mem, disk, bw, image);
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public List<String> listDeviceCPUNames(Session session) {
 		try {
 			LoginUserProfile user = getUser(session);
 			if (!user.isSystemAdmin()) {
 				return null;
 			}
 			return listCPUNames();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public void actionTemplate(Session session, int template_id, int user_id, int life) {
 		LoginUserProfile user = getUser(session);
 		if (!user.isSystemAdmin() || life < 0) {
 			return;
 		}
 		try {
 			doActionTemplate(template_id, user_id, life);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Template lookupTemplateByID(Session session, int template_id) throws EucalyptusServiceException {
 		if (!getUser(session).isSystemAdmin()) {
 			throw new EucalyptusServiceException("权限不足 无效操作");
 		}
 		
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbproc.lookupTemplateByID(template_id);
 			ResultSet rs = rsw.getResultSet();
 			rs.next();
 			Template template = new Template();
 			template.setCPU(rs.getString(DBTableColName.TEMPLATE.CPU));
 			template.setDisk(rs.getString(DBTableColName.TEMPLATE.DISK));
 			template.setImage(rs.getString(DBTableColName.TEMPLATE.IMAGE));
 			template.setMem(rs.getString(DBTableColName.TEMPLATE.MEM));
 			template.setMark(rs.getString(DBTableColName.TEMPLATE.MARK));
 			template.setBw(rs.getString(DBTableColName.TEMPLATE.BW));
			template.setNcpus(rs.getString(DBTableColName.TEMPLATE.NCPUS));
 			return template;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new EucalyptusServiceException("获取模板失败");
 		}
 		finally {
 			if (rsw != null) {
 				try {
 					rsw.close();
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private void doActionTemplate(int template_id, int user_id, int life) throws Exception {
 		String cpu = "";
 		int ncpus = 1;
 		long memory = 0;
 		long disk = 0;
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbproc.queryTemplate(template_id);
 			ResultSet rs = rsw.getResultSet();
 			if (rs.next()) {
 				cpu = rs.getString(DBTableColName.TEMPLATE.CPU);
 				String s;
 				s = rs.getString(DBTableColName.TEMPLATE.MEM);
 				if (!isEmpty(s)) {
 					memory = Long.parseLong(s);
 				}
 				s = rs.getString(DBTableColName.TEMPLATE.DISK);
 				if (!isEmpty(s)) {
 					disk = Long.parseLong(s);
 				}
 			}
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 		System.err.println(" = " + cpu + " " + memory + " " + disk);
 		if (isEmpty(cpu) && memory == 0 && disk == 0) {
 			return;
 		}
 		
 		int server_id = -1;
 		rsw = null;
 		try {
 			rsw = dbproc.lookupServer(cpu, ncpus, memory, disk);
 			ResultSet rs = rsw.getResultSet();
 			if (rs.next()) {
 				server_id = rs.getInt(DBTableColName.SERVER.ID);
 			}
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 		String starttime = formatter.format(new Date());
 		if (!isEmpty(cpu) && ncpus > 0) {
 			addCPUService(cpu, ncpus, user_id, server_id, starttime, life);
 		}
 		if (memory > 0) {
 			addMemoryService(memory, user_id, server_id, starttime, life);
 		}
 		if (disk > 0) {
 			addDiskService(disk, user_id, server_id, starttime, life);
 		}
 	}
 	
 	private void addCPUService(String cpu, int ncpus, int user_id, int server_id, String starttime, int life) throws Exception {
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbproc.lookupCPUService(cpu, server_id);
 			ResultSet rs = rsw.getResultSet();
 			for (int i = 0; i < ncpus; i ++) {
 				rs.next();
 				int cs_id = rs.getInt(2);
 				dbproc.addCPUService(cs_id, user_id, starttime, life, CPUState.INUSE.getValue());
 			}
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	}
 	
 	private void addMemoryService(long memory, int user_id, int server_id, String starttime, int life) throws Exception {
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbproc.lookupMemoryDevice(memory, server_id);
 			ResultSet rs = rsw.getResultSet();
 			if (rs.next()) {
 				int mem_id = rs.getInt(1), ms_id = rs.getInt(2);
 				dbproc.addMemoryService(ms_id, mem_id, user_id, memory, starttime, life, MemoryState.INUSE.getValue());
 			}
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	}
 	
 	private void addDiskService(long disk, int user_id, int server_id, String starttime, int life) throws Exception {
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbproc.lookupDiskDevice(disk, server_id);
 			ResultSet rs = rsw.getResultSet();
 			if (rs.next()) {
 				int disk_id = rs.getInt(1), ds_id = rs.getInt(2);
 				dbproc.addDiskService(ds_id, disk_id, user_id, disk, starttime, life, DiskState.INUSE.getValue());
 			}
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	}
 	
 	private List<String> listCPUNames() throws Exception {
 		ResultSetWrapper rsw = null;
 		try {
 			rsw = dbproc.listCPUNames();
 			List<String> list = new ArrayList<String>();
 			ResultSet rs = rsw.getResultSet();
 			while (rs.next()) {
 				String name = rs.getString(DBTableColName.CPU.NAME);
 				if (!isEmpty(name)) {
 					list.add(name);
 				}
 			}
 			return list;
 		}
 		catch (Exception e) {
 			throw e;
 		}
 		finally {
 			try {
 				if (rsw != null) {
 					rsw.close();
 				}
 			}
 			catch (Exception e) {
 				throw e;
 			}
 		}
 	}
 
 	private boolean deleteTemplate(int template_id) {
 		try {
 			dbproc.deleteTemplate(template_id);
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private void modifyTemplate(int template_id, String cpu, int ncpus, String mem, String disk, String bw, String image) throws Exception {
 		dbproc.modifyTemplate(template_id, cpu, ncpus, mem, disk, bw, image);
 	}
 
 	private void addTemplate(String mark, String cpu, int ncpus, String mem, String disk, String bw, String image)
 	        throws Exception {
 		dbproc.addTemplate(mark, cpu, ncpus, mem, disk, bw, image, formatter.format(new Date()));
 	}
 
 	private List<SearchResultRow> convertResults(ResultSetWrapper rsw) {
 		if (rsw != null) {
 			ResultSet rs = rsw.getResultSet();
 			List<SearchResultRow> rows = new LinkedList<SearchResultRow>();
 			try {
 				int index = 1;
 				while (rs.next()) {
 					rows.add(convertRootResultRow(rs, index ++));
 				}
 				return rows;
 			}
 			catch (SQLException e) {
 				e.printStackTrace();
 			}
 			finally {
 				try {
 					rsw.close();
 				}
 				catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 
 	private SearchResultRow convertRootResultRow(ResultSet rs, int index) throws SQLException {
 		String cpu = rs.getString(DBTableColName.TEMPLATE.CPU);
 		String ncpus = rs.getString(DBTableColName.TEMPLATE.NCPUS);
 		if (isEmpty(cpu)) {
 			ncpus = "";
 		}
 		return new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.TEMPLATE.ID), "", Integer.toString(index),
 				rs.getString(DBTableColName.TEMPLATE.MARK), cpu, ncpus,
 				rs.getString(DBTableColName.TEMPLATE.MEM),
 				rs.getString(DBTableColName.TEMPLATE.DISK),
 				rs.getString(DBTableColName.TEMPLATE.BW),
 				rs.getString(DBTableColName.TEMPLATE.IMAGE),
 				rs.getString(DBTableColName.TEMPLATE.STARTTIME)));
 	}
 
 	private static final int LAN_SELECT = 1;
 	
 	public static final int TABLE_COL_INDEX_TEMPLATE_ID = 0;
 	
 	public static final int TABLE_COL_INDEX_CHECKBOX = 1;
 	public static final int TABLE_COL_INDEX_NO = 2;
 	public static final int TABLE_COL_INDEX_MARK = 3;
 	public static final int TABLE_COL_INDEX_CPU = 4;
 	public static final int TABLE_COL_INDEX_NCPUS = 5;
 	public static final int TABLE_COL_INDEX_MEM = 6;
 	public static final int TABLE_COL_INDEX_DISK = 7;
 	public static final int TABLE_COL_INDEX_BW = 8;
 	public static final int TABLE_COL_INDEX_IMAGE = 9;
 
 	private static final String[] TABLE_COL_TITLE_CHECKBOX = {"", ""};
 	private static final String[] TABLE_COL_TITLE_NO = {"", "序号"};
 	private static final String[] TABLE_COL_TITLE_MARK = {"", "标识"};
 	private static final String[] TABLE_COL_TITLE_CPU = {"", "CPU"};
 	private static final String[] TABLE_COL_TITLE_NCPUS = {"", "CPU数量"};
 	private static final String[] TABLE_COL_TITLE_MEM = {"", "内存"};
 	private static final String[] TABLE_COL_TITLE_DISK = {"", "硬盘"};
 	private static final String[] TABLE_COL_TITLE_BW = {"", "带宽"};
 	private static final String[] TABLE_COL_TITLE_IMAGE = {"", "镜像"};
 	private static final String[] TABLE_COL_TITLE_STARTTIME = {"", "开始时间"};
 
 	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
 			new SearchResultFieldDesc(null, "0%", false),
 			new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKBOX[LAN_SELECT], "4%", false),
 			new SearchResultFieldDesc(TABLE_COL_TITLE_NO[LAN_SELECT], false, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false),
 	        new SearchResultFieldDesc(TABLE_COL_TITLE_MARK[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
 	        new SearchResultFieldDesc(TABLE_COL_TITLE_CPU[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
 	        new SearchResultFieldDesc(TABLE_COL_TITLE_NCPUS[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
 	        new SearchResultFieldDesc(TABLE_COL_TITLE_MEM[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
 	        new SearchResultFieldDesc(TABLE_COL_TITLE_DISK[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
 	        new SearchResultFieldDesc(TABLE_COL_TITLE_BW[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
 	        new SearchResultFieldDesc(TABLE_COL_TITLE_IMAGE[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
             new SearchResultFieldDesc(TABLE_COL_TITLE_STARTTIME[LAN_SELECT], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false));
 }
