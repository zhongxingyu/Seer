 package com.zand.areaguard.area.sql;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import com.zand.areaguard.area.Area;
 import com.zand.areaguard.area.Cuboid;
 import com.zand.areaguard.area.List;
 import com.zand.areaguard.area.Msg;
 import com.zand.areaguard.area.World;
 import com.zand.areaguard.area.error.ErrorArea;
 
 public class SqlArea implements Area {
 	final static public ErrorArea
 	COULD_NOT_CONNECT = new ErrorArea("COULD NOT CONNECT"),
 	SQL_ERROR = new ErrorArea("SQL ERROR");
 	final private SqlStorage storage;
 	final private int id;
 	
 	protected SqlArea(SqlStorage storage, int id) {
 		this.storage = storage;
 		this.id = id;
 	}
 	
 	@Override public boolean equals(Object o) {
 		if (o instanceof SqlArea)
 			return id == ((SqlArea)o).id;
 		if (o instanceof Integer)
 			return id == ((Integer)o).intValue();
 		return false;
 	}
 	
 	@Override public int hashCode() {
 		return id;
 	}
 	
 	@Override
 	public ArrayList<Cuboid> getCubiods() {
 		ArrayList<Cuboid> cubiods = new ArrayList<Cuboid>();
 		String sql = "SELECT Id FROM `" + storage.tablePrefix + "Cuboids` WHERE AreaId = ?";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) 
 					cubiods.add(new SqlCuboid(storage, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return cubiods;
 	}
 	
 	@Override
 	public ArrayList<Cuboid> getCubiods(boolean active) {
 		ArrayList<Cuboid> cubiods = new ArrayList<Cuboid>();
 		String sql = "SELECT Id FROM `" + storage.tablePrefix + "Cuboids` WHERE AreaId = ? AND Active = ?";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.setBoolean(2, active);
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) 
 					cubiods.add(new SqlCuboid(storage, rs.getInt(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return cubiods;
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public List getList(String name) {
 		return new SqlList(storage, getId(), name);
 	}
 
 	@Override
 	public ArrayList<List> getLists() {
 		ArrayList<List> lists = new ArrayList<List>();
 		String sql = "SELECT DISTINCT Name FROM `" + storage.tablePrefix + "Lists` WHERE AreaId = ?";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) lists.add(getList(rs.getString(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return lists;
 	}
 
 	@Override
 	public Msg getMsg(String name) {
 		return new SqlMsg(storage, getId(), name);
 	}
 
 	@Override
 	public ArrayList<Msg> getMsgs() {
 		ArrayList<Msg> msgs = new ArrayList<Msg>();
 		String sql = "SELECT DISTINCT Name FROM `" + storage.tablePrefix + "Msgs` WHERE AreaId = ?";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) msgs.add(getMsg(rs.getString(1)));
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return msgs;
 	}
 
 	@Override
 	public String getName() {
 		String name = "";
 		String sql = "SELECT Name FROM `" + storage.tablePrefix + "Areas` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) name = rs.getString(1);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return name;
 	}
 
 	@Override
 	public boolean isOwner(String player) {
 		return getList("owners").hasValue(player);
 	}
 
 	@Override
 	public boolean setName(String name) {
 		if (name == null || name.isEmpty()) return false;
 		
 		String update = "UPDATE `" + storage.tablePrefix + "Areas` "
 		+ "SET Name=? "
		+ "WHERE Id=? " 
		+ "LIMIT 1;";
 		if (storage.connect())
 			return false;
 		try {
 			PreparedStatement ps = storage.conn.prepareStatement(update);
 			ps.setString(1, name);
 			ps.setInt(2, getId());
 			
 		
 			ps.execute();
 			ps.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			storage.disconnect();
 			return false;
 		}
 		
 		storage.disconnect();
 		return true;
 	}
 
 	@Override
 	public boolean pointInside(World world, int x, int y, int z) {
 		for (Cuboid cubiod : getCubiods(true))
 			if (cubiod.pointInside(world, x, y, z))
 				return true;
 		return false;
 	}
 
 	@Override
 	public boolean pointInside(String world, int x, int y, int z) {
 		return pointInside(storage.getWorld(world), x, y, z);
 	}
 
 	@Override
 	public boolean exsists() {
 		boolean ret = false;
 		String sql = "SELECT COUNT(*) FROM `" + storage.tablePrefix + "Areas` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				while (rs.next()) ret = (rs.getInt(1) > 0);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return ret;
 	}
 
 	@Override
 	public Area getParrent() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean setParrent(Area parrent) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public String getCreator() {
 		String creator = "";
 		String sql = "SELECT Creator FROM `" + storage.tablePrefix + "Areas` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) creator = rs.getString(1);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return creator;
 	}
 
 	@Override
 	public boolean delete() {
 		String sqls[] = {
 				"DELETE FROM `" + storage.tablePrefix + "Lists` WHERE AreaId = ?",
 				"DELETE FROM `" + storage.tablePrefix + "Msgs` WHERE AreaId = ?",
 				"DELETE FROM `" + storage.tablePrefix + "Cuboids` WHERE AreaId = ?",
 				"DELETE FROM `" + storage.tablePrefix + "Areas` WHERE Id = ?" };
 
 		if (storage.connect()) {
 			for (String sql : sqls) {
 				try {
 					PreparedStatement ps = storage.conn.prepareStatement(sql);
 					ps.setInt(1, getId());
 					ps.execute();
 	
 					// Close events
 					ps.close();
 	
 				} catch (SQLException e) {
 					e.printStackTrace();
 					storage.disconnect();
 					return false;
 				}
 			}
 
 			storage.disconnect();
 		} else return false;
 		return true;
 	}
 }
