 package edu.cmu.eventtracker.serverlocator;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import com.caucho.hessian.server.HessianServlet;
 
 import edu.cmu.eventtracker.dto.ShardResponse;
 
 public class ServerLocatorServiceImpl extends HessianServlet
 		implements
 			ServerLocatorService {
 	private static final String protocol = "jdbc:derby:";
 
 	private Connection shardsConnection;
 
 	public ServerLocatorServiceImpl() {
 
 	}
 
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		int port = (Integer) this.getServletContext().getAttribute("PORT");
 		try {
 			shardsConnection = DriverManager.getConnection(protocol
 					+ "shardsDB" + port + ";create=true", null);
 
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	@Override
 	public ShardResponse getUserShard(String username) {
 		ResultSet rs = null;
 
 		try {
 			int hash = username.hashCode();
 			PreparedStatement usersStatement = shardsConnection
 					.prepareStatement("select master, slave from usershard join (Select max(nodeid) maxnode from usershard where nodeid <= ?) s on usershard.nodeid = maxnode");
 
 			usersStatement.setInt(1, hash);
 			usersStatement.execute();
 			rs = usersStatement.getResultSet();
 
 			if (rs.next()) {
 				return new ShardResponse(rs.getString("master"),
 						rs.getString("slave"));
 			} else {
 				PreparedStatement usersMaxStatement = shardsConnection
 						.prepareStatement("Select master, slave, max(nodeid) from usershard");
 
 				usersMaxStatement.execute();
 
 				if (rs.next()) {
 					return new ShardResponse(rs.getString("master"),
 							rs.getString("slave"));
 				}
 			}
 			return null;
 
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new IllegalStateException(e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public ShardResponse getLocationShard(double lat, double lng) {
 		ResultSet rs = null;
 		try {
 			PreparedStatement locationsStatement = shardsConnection
 					.prepareStatement("Select master, slave, min((latmax-latmin) * (lngmax - lngmin)) from locationshard where latmin <= ? and ? < latmax and lngmin <= ? and ? < lngmax group by master, slave, lngmin, lngmax, latmax, latmin");
 
 			locationsStatement.setDouble(1, lat);
 			locationsStatement.setDouble(2, lat);
 			locationsStatement.setDouble(3, lng);
 			locationsStatement.setDouble(4, lng);
 			locationsStatement.execute();
 			locationsStatement.setMaxRows(1);
 			rs = locationsStatement.getResultSet();
 			if (rs.next()) {
 				return new ShardResponse(rs.getString("master"),
 						rs.getString("slave"));
 			}
 			return null;
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new IllegalStateException(e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public List<ShardResponse> getAllLocationShards() {
 		ResultSet rs = null;
 		List<ShardResponse> shards = new ArrayList<ShardResponse>();
 
 		try {
 			PreparedStatement locationsStatement = shardsConnection
 					.prepareStatement("Select * from locationshard");
 
 			locationsStatement.execute();
 			rs = locationsStatement.getResultSet();
			if (rs.next()) {
 				ShardResponse response = new ShardResponse(
 						rs.getString("master"), rs.getString("slave"));
 
 				response.setLatmin(rs.getDouble("latmin"));
 				response.setLngmin(rs.getDouble("lngmin"));
 				response.setLatmax(rs.getDouble("latmax"));
 				response.setLngmax(rs.getDouble("lngmax"));
 				shards.add(response);
 			}
 
 			return shards;
 
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new IllegalStateException(e);
 				}
 			}
 		}
 	}
 
 	public void addUserShard(int nodeid, String master, String slave) {
 		try {
 			PreparedStatement createShard = shardsConnection
 					.prepareStatement("insert into usershard(nodeid, master, slave) values(?, ?, ?) ");
 
 			createShard.setInt(1, nodeid);
 			createShard.setString(2, master);
 			createShard.setString(3, slave);
 			createShard.execute();
 
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public void addLocationShard(double latmin, double lngmin, double latmax,
 			double lngmax, String master, String slave, String name) {
 		try {
 			PreparedStatement createShard = shardsConnection
 					.prepareStatement("insert into locationshard(latmin, lngmin, latmax, lngmax, master, slave, name) values(?, ?, ?, ?, ?, ?, ?) ");
 
 			createShard.setDouble(1, latmin);
 			createShard.setDouble(2, lngmin);
 			createShard.setDouble(3, latmax);
 			createShard.setDouble(4, lngmax);
 			createShard.setString(5, master);
 			createShard.setString(6, slave);
 			createShard.setString(7, name);
 			createShard.execute();
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public void clearTables() {
 		try {
 			shardsConnection.prepareStatement("Delete from locationshard")
 					.execute();
 			shardsConnection.prepareStatement("Delete from usershard")
 					.execute();
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	@Override
 	public ShardResponse findLocationShard(String hostname) {
 		try {
 			PreparedStatement ps = shardsConnection
 					.prepareStatement("Select master, slave from locationshard where master=? or slave=?");
 			ps.setString(1, hostname);
 			ps.setString(2, hostname);
 			ps.execute();
 			ResultSet rs = ps.getResultSet();
 			if (rs.next()) {
 				return new ShardResponse(rs.getString("master"),
 						rs.getString("slave"));
 			}
 			return null;
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 }
