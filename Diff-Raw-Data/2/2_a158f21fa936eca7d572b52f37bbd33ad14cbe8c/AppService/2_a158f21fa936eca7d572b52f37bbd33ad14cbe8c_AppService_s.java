 package com.example.services;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import com.example.models.DataPoint;
 import com.example.models.Return;
 import com.example.models.Trip;
 
 @Path("/rest")
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 public class AppService {
 	
 	@POST
 	@Path("/trip")
 	public Response addTrip(Trip t){
 		
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		
 		try {
 			System.out.println("URL reached");
 			
 			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpool");
 			st = conn.createStatement();
 			
			rs = st.executeQuery("SELECT * from uid WHERE uid =" + t.userId);
 			
 			System.out.println("Query Executed");
 			
 			if (!rs.next()){
 				st.executeUpdate("INSERT into uid (user_id) values (" + t.userId + ")");
 			}
 			
 			System.out.println("uid inserted if need be");
 			
 			String update = "INSERT into trip (user_id,start,finish) values (" + t.userId + ",'" + t.start + "','" + t.finish + "')";
 					
 			st.executeUpdate(update);
 			
 			System.out.println("very nice wawa wee wa");
 			
 			return Response.status(201).entity(new Return(201, "Great Success!")).build();
 		} catch (Exception e){
 			e.printStackTrace();
 			return Response.status(500).entity(new Return(500, "DB Error")).build();
 		} finally {
 			try { if (rs != null) rs.close(); } catch(Exception e) { }
 			try { if (st != null) st.close(); } catch(Exception e) { }
 			try { if (conn != null) conn.close(); } catch(Exception e) { }
 		}	
 		
 		
 	}
 	
 	@POST
 	@Path("/point")
 	public Response addPoint(DataPoint pt){
 		
 		System.out.println("method reached");
 		
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		
 		try {
 			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpool");
 			st = conn.createStatement();
 
 			rs = st.executeQuery("SELECT * from uid WHERE uid =" + pt.userId);
 
 			if (!rs.next()){
 				st.executeUpdate("INSERT into uid (user_id) values (" + pt.userId + ")");
 			}
 			
 			String update = "INSERT into data_pt (user_id,lat,lon,time) values (" + pt.userId + "," + pt.lat + "," + pt.lon + ",'" + pt.timeStamp + "')";
 			
 			
 			st.executeUpdate(update);			
 			return Response.status(201).entity(new Return(201, "Great Success!")).build();
 		} catch (SQLException e){
 			return Response.status(500).entity(new Return(500, "DB Error")).build();
 		} finally {
 			try { if (st != null) st.close(); } catch(Exception e) { }
 			try { if (conn != null) conn.close(); } catch(Exception e) { }
 		}	
 		
 			
 	}
 
 	@GET
 	@Path("/getpoints/{uid}/{tid}")
 	public Response getPoints(@PathParam("uid") Long uid, @PathParam("tid") Long tid) {
 		
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		
 		try {
 			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpool");
 			st = conn.createStatement();
 			
 			List<DataPoint> points = new LinkedList<DataPoint>();
 			
 			String query = "SELECT * FROM data_pt INNER JOIN trip ON trip.user_id = data_pt.user_id AND time >= start AND time <= finish " +
 					"WHERE trip_id =" + tid + " AND trip.user_id =" + uid + " ORDER BY time ASC";
 			
 			rs = st.executeQuery(query);
 			
 			while (rs.next()){
 				
 				Long id = rs.getLong(1);
 				Double lat = rs.getDouble(2);
 				Double lon = rs.getDouble(3);
 				String stamp = rs.getString(4);
 				
 				points.add(new DataPoint(id, lat, lon, stamp));
 			}
 			
 			return Response.ok(points).build();
 		} catch (SQLException e){
 			return Response.status(500).build();
 		} finally {
 			try { if (rs != null) rs.close(); } catch(Exception e) { }
 			try { if (st != null) st.close(); } catch(Exception e) { }
 			try { if (conn != null) conn.close(); } catch(Exception e) { }
 		}	
 		
 	}
 	
 	@GET
 	@Path("/gettrips/{uid}")
 	public Response getTrips(@PathParam("uid") Long uid){
 		
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		
 		try {
 			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpool");
 			st = conn.createStatement();
 
 			List<Trip> trips = new LinkedList<Trip>();
 			
 			String query = "SELECT * from trip WHERE user_id =" + uid + " ORDER BY start desc";
 			
 			rs = st.executeQuery(query);
 			
 			while (rs.next()){
 				
 				Long tripId = rs.getLong(1);
 				Long userId = rs.getLong(2);
 				String start = rs.getString(3);
 				String finish = rs.getString(4);
 				
 				trips.add(new Trip(tripId,userId,start,finish));
 			}
 
 			return Response.ok(trips).build();
 			
 		} catch (SQLException e){
 			return Response.status(500).build();
 		} finally {
 			try { if (rs != null) rs.close(); } catch(Exception e) { }
 			try { if (st != null) st.close(); } catch(Exception e) { }
 			try { if (conn != null) conn.close(); } catch(Exception e) { }
 		}	
 		
 	}
 	
 	
 }
