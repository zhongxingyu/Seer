 package com.blackstar.db.dao.impl;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.blackstar.db.dao.interfaces.EquipmentTypeDAO;
 import com.blackstar.model.Equipmenttype;
 
 public class MySQLEquipmentTypeDAO implements EquipmentTypeDAO, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4137438371500773507L;
 
 	@Override
 	public Equipmenttype findEquipmentType() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Equipmenttype> selectAllEquipmentType() {
 		List<Equipmenttype> lstEquipmentType = new ArrayList<Equipmenttype>();
 		Equipmenttype equipmentType = null;
 		try {
 			Connection conn = MySQLDAOFactory.createConnection();
			PreparedStatement ps = conn.prepareStatement("Select * from equipmenttype");
 			ResultSet rs = ps.executeQuery();
 			
 			while(rs.next()) {
 				equipmentType = new Equipmenttype(rs.getString("equipmentTypeId").charAt(0), rs.getString("equipmentType"));
 				lstEquipmentType.add(equipmentType);
 						
 			}
 		} catch (ClassNotFoundException | SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// TODO Auto-generated method stub
 		return lstEquipmentType;
 	}
 
 	@Override
 	public int insertEquipmentType() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public boolean updateEquipmentType() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Equipmenttype getEquipmentTypeById(char id) {
 		Equipmenttype equipmentType = null;
 		Connection conn = null;
 		try {
 			conn = MySQLDAOFactory.createConnection();
			PreparedStatement ps = conn.prepareStatement("Select * from equipmenttype where equipmenttypeId = ?");
 			ps.setString(1, Character.toString(id));
 			ResultSet rs = ps.executeQuery();
 			
 			while(rs.next()) {
 				equipmentType = new Equipmenttype(rs.getString("equipmentTypeId").charAt(0), rs.getString("equipmentType"));
 						
 			}
 		} catch (ClassNotFoundException | SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		finally{
 			if(conn != null){
 				try {
 					conn.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		// TODO Auto-generated method stub
 		return equipmentType;
 	}
 
 }
