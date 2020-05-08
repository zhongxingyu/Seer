 package com.eucalyptus.webui.server.vm;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import com.eucalyptus.webui.server.db.DBProcWrapper;
 import com.eucalyptus.webui.server.db.ResultSetWrapper;
 import com.eucalyptus.webui.server.dictionary.DBTableColName;
 import com.eucalyptus.webui.server.dictionary.DBTableName;
 
 public class VITDBProcWrapper {
 	public void addVIT(VmImageType vit) throws VITSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = addVITSql(vit);
 		
 		System.out.println(sql);
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			throw new VITSyncException ("Database fails");
 		}
 	}
 	
 	public void updateVIT(VmImageType vit) throws VITSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = updateVITSql(vit);
 		
 		System.out.println(sql);
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			throw new VITSyncException ("Database fails");
 		}
 	}
 	
 	public void delVITs(ArrayList<String> ids) throws VITSyncException {
 		if (ids == null || ids.size() == 0)
 			return;
 		
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = delVITSql(ids);
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new VITSyncException("Database fails");
 		}
 	}
 	
 	public VmImageType lookupVIT(int vitId) throws VITSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = lookupVITSql(vitId);
 		
 		try {
 			ResultSetWrapper rsWrapper = dbProc.query(sql);
 			ResultSet rs = rsWrapper.getResultSet();
 			
 			if (!rs.wasNull()) {
 				rs.last();
 				
 				assert(rs.getRow() <= 1);
 				
 				if (rs.getRow() ==0) {
 					rsWrapper.close();
 					throw new VITSyncException("VM image type not existed");
 				}
 				else
 				{	
 					int id = Integer.valueOf(rs.getString(DBTableColName.VM_IMAGE_TYPE.ID));
 					String os = rs.getString(DBTableColName.VM_IMAGE_TYPE.OS);
 					String ver = rs.getString(DBTableColName.VM_IMAGE_TYPE.VER);
					String euca_vit_id = rs.getString(DBTableColName.VM_IMAGE_TYPE.EUCA_VIT_ID);
 											
 					VmImageType vit = new VmImageType(id, os, ver, euca_vit_id);
 					rsWrapper.close();
 					
 					return vit; 
 				}
 			}
 			else { 
 				rsWrapper.close();
 				throw new VITSyncException("Database fails");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new VITSyncException("Database fails");
 		}
 	}
 	
 	private String addVITSql(VmImageType vit) {
 		StringBuilder str = new StringBuilder();
 		str.append("INSERT INTO ").
 		append(DBTableName.VM_IMAGE_TYPE).append(" ( ").
 		append(DBTableColName.VM_IMAGE_TYPE.ID).append(", ").
 		append(DBTableColName.VM_IMAGE_TYPE.OS).append(", ").
 		append(DBTableColName.VM_IMAGE_TYPE.VER).append(", ").
 		append(DBTableColName.VM_IMAGE_TYPE.DEL).append(", ").
		append(DBTableColName.VM_IMAGE_TYPE.EUCA_VIT_ID).
 		append(") VALUES (null, ");
 		
 		str.append("'");
 		str.append(vit.getOs());
 		str.append("', '");
 		
 		str.append(vit.getVer());
 		str.append("', ");
 		
 		str.append(0);
 		str.append(", '");
 		
 		str.append(vit.getEucaVITId());
 		str.append("')");
 		
 		return str.toString();
 	}
 	
 	private String updateVITSql(VmImageType vit) {
 		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.VM_IMAGE_TYPE).append(" SET ");
 		
 		if (vit.getOs() != null) {
 			str.append(DBTableColName.VM_IMAGE_TYPE.OS).append(" = '").
 			append(vit.getOs()).
 			append("', ");
 		}
 		
 		if (vit.getVer() != null) {
 			str.append(DBTableColName.VM_IMAGE_TYPE.VER).append(" = '").
 			append(vit.getVer()).
 			append("', ");
 		}
 		
 		if (vit.getEucaVITId() != null) {
			str.append(DBTableColName.VM_IMAGE_TYPE.EUCA_VIT_ID).append(" = '").
 			append(vit.getEucaVITId()).
 			append("', ");
 		}
 		
 		if (str.length() > 2)
 			str.delete(str.length() -2, str.length());
 		
 		str.append(" WHERE ").append(DBTableColName.VM_IMAGE_TYPE.ID).append(" = ").
 		append(vit.getId());
 		
 		return str.toString();
 	}
 	
 	private String delVITSql(ArrayList<String> ids) {
 		
 		if (ids == null || ids.size() == 0)
 			return null;
 		
 		StringBuilder sql = new StringBuilder("UPDATE ").append(DBTableName.VM_IMAGE_TYPE).append(" SET ");
 		
 		sql.append(DBTableColName.VM_IMAGE_TYPE.DEL).append(" = 1 ");
 		sql.append(" WHERE ");
 		
 		for (String str : ids) {
 			sql.append(DBTableColName.VM_IMAGE_TYPE.ID).append(" = ").
 			append(str).
 			append(" OR ");
 		}
 		
 		sql.delete(sql.length() -3 , sql.length());
 		
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 	
 	private String lookupVITSql(int vitId) {
 		StringBuilder sql = new StringBuilder("SELECT * FROM ").
 								append(DBTableName.VM_IMAGE_TYPE).
 								append(" WHERE ").
 								append(DBTableColName.VM_IMAGE_TYPE.DEL).append(" = 0 ").
 								append(" AND ").
 								append(DBTableColName.VM_IMAGE_TYPE.ID).append(" = ").
 								append(vitId);
 		
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 }
