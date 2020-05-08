 package com.zand.areaguard.area.sql;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.zand.areaguard.area.Area;
 import com.zand.areaguard.area.Cuboid;
 import com.zand.areaguard.area.World;
 import com.zand.areaguard.area.error.ErrorArea;
 import com.zand.areaguard.area.error.ErrorCuboid;
 import com.zand.areaguard.area.error.ErrorWorld;
 
 public class SqlCuboid implements Cuboid {
 	final static public ErrorCuboid
 		COULD_NOT_CONNECT = new ErrorCuboid(),
 		SQL_ERROR = new ErrorCuboid();
 	final private SqlStorage storage;
 	final private int id;
 	
 	@Override public boolean equals(Object o) {
 		if (o instanceof SqlCuboid)
 			return id == ((SqlCuboid)o).id;
 		if (o instanceof Integer)
 			return id == ((Integer)o).intValue();
 		return false;
 	}
 	
 	@Override public int hashCode() {
 		return id;
 	}
 	
 	public SqlCuboid(SqlStorage storage, int id) {
 		this.storage = storage;
 		this.id = id;
 	}
 
 	@Override
 	public Area getArea() {
 		Area area = null;
 		String sql = "SELECT AreaId FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) area = new SqlArea(storage, rs.getInt(1));
 				else area = ErrorArea.NOT_FOUND;
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 				area = SqlArea.SQL_ERROR;
 			}
 
 			storage.disconnect();
 		} else area = SqlArea.COULD_NOT_CONNECT;
 		return area;
 	}
 
 	@Override
 	public int[] getCoords() {
 		int coords[] = new int[6];
 		String sql = "SELECT x1,y1,z1,x2,y2,z2 FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) 
 					for (int i = 0; i < 6; i++)
 						coords[i] = rs.getInt(1 + i);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return coords;
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public int getPriority() {
 		int priority = 100;
 		String sql = "SELECT Priority FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) priority = rs.getInt(1);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return priority;
 	}
 
 	@Override
 	public World getWorld() {
 		World world = null;
 		String sql = "SELECT WorldId FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) world = new SqlWorld(storage, rs.getInt(1));
 				if (!world.exsists()) world = ErrorWorld.NOT_FOUND;
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 				world = SqlWorld.SQL_ERROR;
 			}
 
 			storage.disconnect();
 		} else world = SqlWorld.COULD_NOT_CONNECT;
 		return world;
 	}
 
 	@Override
 	public boolean pointInside(World world, int x, int y, int z) {
 		int coords[] = getCoords();
 		return ((coords[0] <= x && coords[3] >= x) &&
 				(coords[1] <= x && coords[4] >= x) &&
 				(coords[2] <= x && coords[5] >= x) &&
 				getWorld().getName().equals(world.getName()));
 	}
 
 	@Override
 	public boolean setArea(Area area) {
 		String update = "UPDATE `" + storage.tablePrefix + "Cuboids` "
 		+ "SET AreaId=? "
 		+ "WHERE Id=?;";
 		if (storage.connect())
 			return false;
 		try {
 			PreparedStatement ps = storage.conn.prepareStatement(update);
 			ps.setInt(1, area.getId());
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
 	public boolean setPriority(int priority) {
 		String update = "UPDATE `" + storage.tablePrefix + "Cuboids` "
 		+ "SET Priority=? "
 		+ "WHERE Id=?;";
 		if (storage.connect())
 			return false;
 		try {
 			PreparedStatement ps = storage.conn.prepareStatement(update);
 			ps.setInt(1, priority);
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
 	public boolean setLocation(World world, int[] coords) {
 		String update = "UPDATE `" + storage.tablePrefix + "Cuboids` "
 		+ "SET WorldId=?, x1=?, y1=?, z1=?, x2=?, y2=?, z2=? "
 		+ "WHERE Id=?;";
 		if (storage.connect())
 			return false;
 		try {
 			PreparedStatement ps = storage.conn.prepareStatement(update);
 			ps.setInt(1, world.getId());
 			for (int i = 0; i < 6; i++)
 				ps.setInt(i + 2,coords[i]);
 			ps.setInt(8, getId());
 		
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
 	public boolean exsists() {
 		boolean ret = false;
 		String sql = "SELECT COUNT(*) FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ? LIMIT 1";
 
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
 	public long getBlockCount() {
 		int coords[] = getCoords();
 		return 
 		(coords[3]-coords[0]+1)*
 		(coords[4]-coords[1]+1)*
 		(coords[5]-coords[2]+1);
 	}
 
 	@Override
 	public boolean isActive() {
 		boolean active = true;
 		String sql = "SELECT Active FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ? LIMIT 1";
 
 		if (storage.connect()) {
 			try {
 				PreparedStatement ps = storage.conn.prepareStatement(sql);
 				ps.setInt(1, getId());
 				ps.execute();
 
 				// Get the result
 				ResultSet rs = ps.getResultSet();
 				if (rs.next()) active = rs.getBoolean(1);
 
 				// Close events
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			storage.disconnect();
 		}
 		return active;
 	}
 
 	@Override
 	public boolean setActive(boolean active) {
 		String update = "UPDATE `" + storage.tablePrefix + "Cuboids` "
 		+ "SET Active=? "
 		+ "WHERE Id=?";
 		if (!storage.connect())
 			return false;
 		try {
 			PreparedStatement ps = storage.conn.prepareStatement(update);
 			ps.setBoolean(1, active);
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
 	public String getCreator() {
 		String creator = "";
		String sql = "SELECT Creator FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ? LIMIT 1";
 
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
 		String sql = "DELETE FROM `" + storage.tablePrefix + "Cuboids` WHERE Id = ?";
 
 		if (storage.connect()) {
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
 
 			storage.disconnect();
 		} else return false;
 		return true;
 	}
 }
