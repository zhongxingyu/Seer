 package org.miw.sig.carpooling.persistence;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.miw.sig.carpooling.model.Marker;
 import org.miw.sig.carpooling.model.Route;
 import org.miw.sig.carpooling.util.Constants;
 import org.miw.sig.carpooling.util.Helper;
 
 public class CarpoolingDAO implements RoutesDataService {
 
 	private static Logger logger = Logger.getLogger(CarpoolingDAO.class);
 
 	@Override
 	public Route saveRoute(Route route) {
 
 		Marker from = saveMarker(route.getFrom());
 		Marker to = saveMarker(route.getTo());
 
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "saveRoute");
 
 		try {
 
 			ps = conn.prepareStatement(sql);
 			ps.setString(1, route.getEmail());
 			ps.setInt(2, from.getId());
 			ps.setInt(3, to.getId());
 
 			ps.execute();
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(null, ps, conn);
 		}
 
 		route = findId(route);
 		if (route.getMarkers() != null) {
 			for (Marker m : route.getMarkers()) {
 				m = saveMarker(m);
 				saveRouteMarker(m, route);
 			}
 		}
 		return route;
 
 	}
 
 	@Override
 	public List<Route> getRoutes() {
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "getRoutes");
 		ResultSet rs = null;
 		List<Route> routes = new ArrayList<Route>();
 
 		try {
 			ps = conn.prepareStatement(sql);
 			rs = ps.executeQuery();
 
 			while (rs.next()) {
 
 				Route r = new Route();
 				r.setId(rs.getInt("idroutes"));
 				r.setEmail(rs.getString("email"));
 				r.setFrom(getMarker(rs.getInt("home")));
 				r.setTo(getMarker(rs.getInt("finish")));
 				r.setMarkers(getMarkers(r.getId()));
 				routes.add(r);
 			}
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(rs, ps, conn);
 		}
 
 		if (routes.size() == 0)
 			return Collections.emptyList();
 		else
 			return routes;
 
 	}
 
 	private Marker getMarker(int id) {
 
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "getMarker");
 		ResultSet rs = null;
 		Marker m = null;
 
 		try {
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, id);
 			rs = ps.executeQuery();
 
 			if (rs.first()) {
 
 				m = new Marker();
 				m.setId(id);
 				m.setName(rs.getString("name"));
 				m.setLatitude(rs.getString("latitude"));
 				m.setLongitude(rs.getString("longitude"));
 			}
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(rs, ps, conn);
 		}
 
 		return m;
 
 	}
 
 	@Override
 	public Marker saveMarker(Marker marker) {
 
 		Marker m2 = findId(marker);
 		if (m2.getId() != null)
 			return m2;
 
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "saveMarker");
 
 		try {
 
 			ps = conn.prepareStatement(sql);
 			ps.setString(1, marker.getName());
 			ps.setString(2, marker.getLatitude());
 			ps.setString(3, marker.getLongitude());
 
 			ps.execute();
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(null, ps, conn);
 		}
 
 		marker = findId(marker);
 
 		return marker;
 	}
 
 	@Override
 	public List<Marker> getMarkers(Integer idroute) {
 
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "getMarkers");
 		ResultSet rs = null;
 		List<Marker> markers = new ArrayList<Marker>();
 
 		try {
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, idroute);
 			rs = ps.executeQuery();
 
 			while (rs.next()) {
 
 				Marker m = new Marker();
 				m.setId(rs.getInt("idmarkers"));
 				m.setLatitude(rs.getString("latitude"));
 				m.setLongitude(rs.getString("longitude"));
 				m.setName(rs.getString("name"));
 				markers.add(m);
 			}
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(rs, ps, conn);
 		}
 
 		if (markers.size() == 0)
 			return Collections.emptyList();
 		else
 			return markers;
 	}
 
 	private void saveRouteMarker(Marker m, Route route) {
 
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "saveMarkerRoute");
 
 		try {
 
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, route.getId());
 			ps.setInt(2, m.getId());
 
 			ps.execute();
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(null, ps, conn);
 		}
 
 	}
 
 	private Route findId(Route route) {
 
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "getRoute");
 		ResultSet rs = null;
 
 		try {
 			ps = conn.prepareStatement(sql);
 			ps.setString(1, route.getEmail());
 			ps.setInt(2, route.getFrom().getId());
 			ps.setInt(3, route.getTo().getId());
 			rs = ps.executeQuery();
 
 			if (rs.first()) {
 
 				route.setId(rs.getInt("idroutes"));
 
 			}
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(rs, ps, conn);
 		}
 		return route;
 	}
 
 	private Marker findId(Marker marker) {
 
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "getIdMarker");
 		ResultSet rs = null;
 
 		try {
 			ps = conn.prepareStatement(sql);
 			ps.setString(1, marker.getName());
 			ps.setString(2, marker.getLatitude());
 			ps.setString(3, marker.getLongitude());
 			rs = ps.executeQuery();
 
 			if (rs.first()) {
 
 				marker.setId(rs.getInt("idmarkers"));
 
 			}
 
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(rs, ps, conn);
 		}
 		return marker;
 
 	}
 
 	@Override
 	public Route getRoute(Integer idRoute) {
 		Connection conn = JDBCHelper.connect();
 		PreparedStatement ps = null;
 		String sql = Helper.getProperty(Constants.QUERIES, "getRouteById");
 		ResultSet rs = null;
 		Route route = null;
 
 		try {
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, idRoute);
 
 			rs = ps.executeQuery();
 
 			if (rs.first()) {
 				route = new Route();
 				route.setEmail(rs.getString("email"));
 				route.setFrom(getMarker(rs.getInt("home")));
 				route.setTo(getMarker(rs.getInt("finish")));
 				route.setMarkers(getMarkers(route.getId()));
 			}
 		} catch (SQLException e) {
 			logger.error(e.getMessage());
 		} finally {
 			JDBCHelper.close(rs, ps, conn);
 		}
 		return route;
 	}
 
 }
