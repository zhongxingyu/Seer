 package database;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 
 import server.BadParameterException;
 import server.Server;
 import shared.Util;
 import shared.Value;
 
 @Path("/batch")
 public class Images {
 	
 	@POST
 	@Path("sample")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String sampleImage(@FormParam("projectid") Integer projectid) {
 		if (projectid == null) throw new BadParameterException();
 		try {
 			Map<String, String> image = DB.get("SELECT file FROM images WHERE projectid = ?", projectid).get(0);
 			return Server.URI + image.get("file") + "\n";
 		} catch (Exception e) {
 			throw new BadParameterException();
 		}
 	}
 
 	@POST
 	@Path("get")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String getBatch(@FormParam("projectid") Integer projectid, @FormParam("username") String username) {
 		List<String> response = new ArrayList<String>();
 		
 		if (projectid == null) throw new BadParameterException();
 		
 		try {
 			Map<String, String> image = DB.get("SELECT images.rowid AS imageid, * FROM projects, images WHERE projects.rowid = images.projectid AND projectid = ? AND username IS NULL", projectid).get(0);
 			String[] values = {username, image.get("imageid")};
 			DB.run("UPDATE images SET username = ? WHERE rowid = ?", values);
 			
 			response.add(image.get("imageid"));
 			response.add(String.valueOf(projectid));
 			response.add(Server.URI + image.get("file"));
 			response.add(image.get("firstycoord"));
 			response.add(image.get("recordheight"));
 			response.add(image.get("recordsperimage"));
 			
 			List<Map<String, String>> fields = DB.get("SELECT rowid, * FROM fields WHERE projectid = ? ORDER BY rowid ASC", projectid);
 			for (Map<String, String> field : fields) {
 				response.add(field.get("rowid"));
 				response.add(String.valueOf(fields.indexOf(field) + 1));
 				response.add(field.get("title"));
 				response.add(Server.URI + field.get("helphtml"));
 				response.add(field.get("xcoord"));
 				response.add(field.get("width"));
 				
 				if (field.get("knowndata") != null) {
 					response.add(Server.URI + field.get("knowndata"));
 				}
 			
 			}
 			
 			return shared.Util.join(response, "\n") + "\n";
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new BadParameterException();
 		}
 	}
 	
 	@POST
 	@Path("submit")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String submitBatch(@FormParam("batch") Integer imageid, @FormParam("record_values") String _values, @FormParam("username") String username) {
 		if (imageid == null || _values == null) throw new BadParameterException();
 			
 		try {
 			Map<String, String> imageInfo = DB.get("SELECT * FROM images WHERE rowid = ?", imageid).get(0);
 			Integer projectid = Integer.parseInt(imageInfo.get("projectid"));
 			if (!imageInfo.get("username").equals(username)) throw new BadParameterException();
 		
 			List<Map<String, String>> fields = DB.get("SELECT rowid,* FROM fields WHERE projectid = ? ORDER BY rowid ASC", projectid);
 			Map<String, String> projectInfo = DB.get("SELECT * FROM projects WHERE rowid = ?", projectid).get(0);
 			int recordsperimage = Integer.parseInt(projectInfo.get("recordsperimage"));
 			int firstycoord = Integer.parseInt(projectInfo.get("firstycoord"));
 			int recordheight = Integer.parseInt(projectInfo.get("recordheight"));
 			int numberValues = fields.size() * recordsperimage;
 			
 			String[] values = _values.split(",");
 			if (values.length != numberValues) throw new BadParameterException();
 			for (int i = 0, f = 0, r = 0; i < numberValues; i++) {
 				Map<String, String> properties = new HashMap<String, String>();
 				properties.put("fieldid", fields.get(f).get("rowid"));
 				properties.put("imageid", imageid.toString());
 				properties.put("value", values[i]);
 				properties.put("ycoord", String.valueOf(firstycoord + (r * recordheight)));
 				
 				if ((++f + 1) > fields.size()) {
 					f = 0;
 					++r;
 				}
 				
 				new Value(properties).save();
 			}
 
 			DB.run("UPDATE images SET username = NULL WHERE rowid = ?", imageid);
 			return "TRUE\n";
 		} catch (Exception e) {
 			throw new BadParameterException();
 		}
 	}
 	
 	@POST
 	@Path("search")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String search(@FormParam("fields") String _fields, @FormParam("search_values") String _searchValues) {
 		String[] fields = _fields.split(",");
 		String[] searchValues = _searchValues.split(",");
 		
 		String[] p1 = new String[fields.length];
 		String[] p2 = new String[searchValues.length];
 		
 		for (int i = 0; i < fields.length; i++) {
 			p1[i] = "?";
 		}
 		
 		for (int i = 0; i < searchValues.length; i++) {
 			p2[i] = "?";
 		}
 		
		String sql = "SELECT `values`.*, `values`.rowid, images.file FROM `values`, images WHERE images.rowid = `values`.imageid AND value IN (" + Util.join(p1, ",") + ") AND fieldid IN (" + Util.join(p2, ",") + ")";
 		List<Map<String, String>> results = DB.get(sql, Util.arrayConcat(searchValues, fields));
 		
 		StringBuilder response = new StringBuilder();
 		for (Map<String, String> result : results) {
 			response
 				.append(result.get("imageid"))
 				.append("\n")
 				.append(Server.URI + result.get("file"))
 				.append("\n")
 				.append(result.get("rowid"))
 				.append("\n")
 				.append(result.get("fieldid"))
 				.append("\n")
 			;
 		}
 		
 		return response.toString();
 	}
 
 }
