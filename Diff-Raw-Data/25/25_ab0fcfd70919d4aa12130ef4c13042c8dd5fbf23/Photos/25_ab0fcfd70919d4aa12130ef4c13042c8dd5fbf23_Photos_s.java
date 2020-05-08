 package local.tagavatar.server;
 
 import java.sql.*;
 import local.tagavatar.server.Settings;
 import org.json.*;
 import local.tagavatar.server.Likes;
 import local.tagavatar.server.Dislikes;
 
 public class Photos {
 	
 	private Connection con;
 	
 	public Photos(){
 		Settings s=new Settings();
 		this.con=s.get_connection();
 	}
 	
 	public Boolean create(String title, String desc, String photo, String username){
 		String sql="INSERT INTO photos(`title`,`desc`,`photo`,`user_id`,`datetime`) VALUES(?,?,?,?,?)";
 		try{
 			PreparedStatement ps=this.con.prepareStatement(sql);
 			ps.setString(1, title);
 			ps.setString(2, desc);
 			ps.setString(3, photo);
 			ps.setString(4, username);
 			ps.setString(5, "");
 			ps.executeUpdate();
 			return true;
 		}catch(Exception e){
 			return false;
 		}
 	}
 	
 	public String user_photos(String username){
 		String sql="SELECT `title`,`desc`,`photo` FROM photos WHERE user_id='"+username+"' ORDER BY id DESC";
 		try{
 			Statement st=this.con.createStatement();
 			ResultSet rs=st.executeQuery(sql);
 			JSONArray json2=new JSONArray();
 			while(rs.next()){
 				//json2
 				//json2.put(rs.getString("title"));
 				JSONObject json=new JSONObject();
 				json.put("title", rs.getString("title"));
 				json.put("desc", rs.getString("desc"));
 				json.put("photo", rs.getString("photo"));
 				json2.put(json);
 			}
 			return json2.toString();
 		}catch(Exception e){
 			return e.getMessage();
 		}
 	}
 	
 	public String get_random(String username){
 		String sql="SELECT `id`,`title`,`desc`,`photo`,`user_id` FROM photos WHERE user_id!='"+username+"' ORDER BY RAND() LIMIT 1";
 		JSONObject json=new JSONObject();
 		try{
 			Likes l=new Likes();
 			Dislikes d=new Dislikes();
 			Statement st=this.con.createStatement();
 			ResultSet rs=st.executeQuery(sql);
 			while(rs.next()){
 				json.put("status", true);
 				json.put("title", rs.getString("title"));
 				json.put("photo", rs.getString("photo"));
 				json.put("desc", rs.getString("desc"));
 				json.put("username", rs.getString("user_id"));
 				json.put("photo_id", rs.getInt("id"));
 				json.put("likes", l.get_likes(rs.getInt("id")));
 				json.put("dislikes", d.get_dislikes(rs.getInt("id")));
 				json.put("ilike", l.i_like(rs.getInt("id"), username));
 				json.put("idislike", d.i_dislike(rs.getInt("id"), username));
 			}
 			return json.toString();
 		}catch(Exception e){
 			json.put("status", false);
 			json.put("message", e.getMessage());
 			return json.toString();
 		}
 	}
 	
 	public String search(String title){
 		String sql="SELECT id, photos.title, photos.desc,photos.photo,photos.user_id FROM photos WHERE photos.title LIKE '%"+title+"%' ";
 		Likes l=new Likes();
 		try{
 			Statement st=this.con.createStatement();
 			JSONArray obj=new JSONArray();
 			ResultSet rs=st.executeQuery(sql);
 			while(rs.next()){
 				JSONObject json=new JSONObject();
 				json.put("title", rs.getString("title"));
 				json.put("desc", rs.getString("desc"));
 				json.put("photo", rs.getString("photo"));
 				json.put("username", rs.getString("user_id"));
 				json.put("likes",l.get_likes(rs.getInt("id")));
 				obj.put(json);
 			}
 			return obj.toString();
 		}catch(Exception e){
 			JSONObject json=new JSONObject();
 			json.put("success", false);
 			return json.toString();
 		}
 	}
 	
 	public String get_less_photos(String username){
 		String sql="SELECT * FROM photos WHERE `user_id`='"+username+"' ORDER BY RAND() LIMIT 3";
		Photos p=new Photos();
 		JSONArray json=new JSONArray();
 		Likes l=new Likes();
 		Dislikes d=new Dislikes();
 		try{
 			Statement st=this.con.createStatement();
 			ResultSet rs=st.executeQuery(sql);
 			while(rs.next()){
 				JSONObject j=new JSONObject();
 				j.put("id", rs.getInt("id"));
 				j.put("title", rs.getString("title"));
 				j.put("desc", rs.getString("desc"));
 				j.put("photo", rs.getString("photo"));
 				j.put("likes", l.get_likes(rs.getInt("id")));
 				j.put("dislikes", d.get_dislikes(rs.getInt("id")));
 				json.put(j);
 			}
 			return json.toString();
 		}catch(Exception e){
 			JSONObject json2=new JSONObject();
 			json2.put("error", e.getMessage());
 			return json2.toString();
 		}
 	}
 	
 	public String get_latest_photos(){
		String sql="SELECT `title`,`desc`,`photo` FROM photos ORDER BY id DESC";
		JSONArray j=new JSONArray();
 		try{
 			Likes l=new Likes();
 			Dislikes d=new Dislikes();
 			Statement st=this.con.createStatement();
 			ResultSet rs=st.executeQuery(sql);
 			while(rs.next()){
 				JSONObject json=new JSONObject();
				json.put("status", true);
 				json.put("title", rs.getString("title"));
				json.put("photo", rs.getString("photo"));
 				json.put("desc", rs.getString("desc"));
				json.put("username", rs.getString("user_id"));
				json.put("photo_id", rs.getInt("id"));
 				json.put("likes", l.get_likes(rs.getInt("id")));
 				json.put("dislikes", d.get_dislikes(rs.getInt("id")));
 			}
 			return j.toString();
 		}catch(Exception e){
			JSONObject json=new JSONObject();
			json.put("status", false);
			json.put("message", e.getMessage());
			return json.toString();
 		}
 	}
 	
 }
