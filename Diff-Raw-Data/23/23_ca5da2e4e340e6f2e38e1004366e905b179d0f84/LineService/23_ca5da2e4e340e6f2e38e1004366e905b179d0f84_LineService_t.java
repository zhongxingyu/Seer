 package webservice;
 
 import java.util.ArrayList;
 import java.lang.Integer;
   
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import model.AccessManager;
 import dto.Line;
 import helper.CrowdHelper;
 
 @Path("/lineService")
 public class LineService {
 	
 	@GET
 	@Path("/getlines")
 	@Produces("application/json")
 	public ArrayList<Line> getlines() {
 		String lines = null;
 		ArrayList<Line> lineList = new ArrayList<Line>();
 		try {
 			lineList = new AccessManager().getLines();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return lineList;
 	}
 	
 	
 	@PUT	
 	@Path("/addline")
 	@Produces(MediaType.TEXT_PLAIN)	
 	public String addline(@FormParam("lat") String inlat, 
 			@FormParam("lng") String inlng,
 			@FormParam("type") String type	,	
 			@FormParam("count") String strcount,
 			@FormParam("vote") String strvote
 			) {
 		int lineId = 0;
 		
 		try
 		{
			System.out.print("---------" +inlat);
			System.out.print("---------" +inlng);
			System.out.print("---------" +type);
 		
 		CrowdHelper helper = new CrowdHelper();
 		double lat = helper.getDouble("lat",inlat,false,0);
		double lng = helper.getDouble("lng",inlng,false,0);
 		
 		int count = helper.getInt("count",strcount,false,0);
 		int vote = helper.getInt("vote",strvote,false,0);
 		
 		 
 		 Line line = new Line();
 		 line.setLat(lat);
 		 line.setLng(lng);
 		 line.setType(type);
 		 line.setCount(count);
 		 line.setVote(vote); 
 		 
		 lineId = new AccessManager().addLine(line);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 						
 		return Integer.toString(lineId);
 		 
 	}
 	
 	
 	
 	
 }
