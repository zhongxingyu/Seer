 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 import com.google.gson.Gson;
 
 public class CreeperServlet extends HttpServlet
 {
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		doStuff(req, res);
 	}
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		doStuff(req, res);
 	}
 	public void doStuff(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		/*
 		HttpSession session = req.getSession(true);
 		session.setAttribute("key", "value");
 		(String)session.getAttribute("key")
 		*/
 		/*
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 		out.println("<html>");
 		out.close();
 		*/
 		/*
 		res.sendRedirect("success.html");
 		*/
 		Gson gson = new Gson();
 		res.setContentType("text/html");
 		//res.setContentType("application/json");
 		
 		PrintWriter out = res.getWriter();
 		DBAL db = DBAL.getInstance(out);
 		
 		HttpSession session = req.getSession(true);
 		
 		String uidString = (String)session.getAttribute("user_id");
 		int uid = 0; //user id is 0 when not logged in
 		if (uidString != null)
 			uid = Integer.parseInt(uidString);
 		
 		String method = req.getParameter("method");
 		String type = req.getParameter("type");
 		
 		if (method == null || type == null)
 		{ 
 			out.println("{\"success\":false,\"error\":\"Missing a method or type parameter. Request aborted.\"}");
 			out.close(); db.close(); return;
 		}
 		
 		if (method.equalsIgnoreCase("create"))
 		{
 			//check for login when needed
 			if (!type.equalsIgnoreCase("member") && uid == 0)
 			{
 				out.println("{\"success\":false,\"error\":\"You need to be logged in to do this action.\"}");
 				out.close(); db.close(); return;
 			}
 			else if (type.equalsIgnoreCase("member") && uid != 0)
 			{
 				out.println("{\"success\":false,\"error\":\"You need to be logged out to do this action.\"}");
 				out.close(); db.close(); return;
 			}
 			if (type.equalsIgnoreCase("song"))
 			{
 				//create song, assigned to album and artist
 				String name = req.getParameter("name");
 				String album_id = req.getParameter("album_id");
 				String artist_id = req.getParameter("artist_id");
 				String track_number = req.getParameter("track_number");
 				// Do we have all parameters?
 				if (name == null || album_id == null || track_number == null || artist_id == null) 
 				{
 					out.println("{\"success\":false,\"error\":\"Missing a parameter needed for song creation.\"}");
 					out.close(); db.close(); return;
 				}
 				try 
 				{
 					// Check all numbers are in range or actually integers
 					if (Integer.parseInt(artist_id) < 1 || Integer.parseInt(album_id) < 1 || Integer.parseInt(track_number) < 1)
 					{
 						throw new NumberFormatException();
 					}
 					// Check if valid IDs
 					ResultSet rs = db.query("SELECT album_id FROM album WHERE album_id = " + Integer.parseInt(album_id));
 					if (!rs.next())
 					{
 						out.println("{\"success\":false,\"error\":\"There is no album associated with the album id specified.\"}");
 						rs.close();
 						out.close(); db.close(); return;
 					}
 					rs.close();
 					rs = db.query("SELECT artist_id FROM artist WHERE artist_id = " + Integer.parseInt(artist_id));
 					if (!rs.next())
 					{
 						out.println("{\"success\":false,\"error\":\"There is no artist associated with the artist id specified.\"}");
 						rs.close();
 						out.close(); db.close(); return;
 					}
 					rs.close();
 					// Insertion time!
 					PreparedStatement query = db.preparedStatement("INSERT INTO song (`name`, album_id, artist_id, track_number) VALUES (?, ?, ?, ?)");
 					query.setString(1, name);
 					query.setInt(2, Integer.parseInt(album_id));
 					query.setInt(3, Integer.parseInt(artist_id));
 					query.setInt(4, Integer.parseInt(track_number));
 					query.executeUpdate();
 					rs = db.query("SELECT LAST_INSERT_ID()");
 					rs.next();
 					out.println("{\"success\":true;\"id\":"+rs.getInt(1)+"}");
 					rs.close();
 					out.close(); db.close(); return;
 				}
 				// Invalid numbers!
 				catch (NumberFormatException e)
 				{
 					out.println("{\"success\":false,\"error\":\"One of the integer parameters was out of range or an invalid number.\"}");
 					out.close(); db.close(); return;
 				}
 				// Oh noes!
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in running the insertion.\"}");
 					//out.println(e);
 					out.close(); db.close(); return;
 				}
 			}
 			else if (type.equalsIgnoreCase("album"))
 			{
 				//create album, assigned to artist
 				String name = req.getParameter("name");
 				String artist_id = req.getParameter("artist_id");
 				// Do we have all parameters?
 				if (name == null || artist_id == null) 
 				{
 					out.println("{\"success\":false,\"error\":\"Missing a parameter needed for album creation.\"}");
 					out.close(); db.close(); return;
 				}
 				try 
 				{
 					// Check all numbers are in range or actually integers
 					if (Integer.parseInt(artist_id) < 1)
 					{
 						throw new NumberFormatException();
 					}
 					// Check if valid IDs
 					ResultSet rs = db.query("SELECT artist_id FROM artist WHERE artist_id = " + Integer.parseInt(artist_id));
 					if (!rs.next())
 					{
 						out.println("{\"success\":false,\"error\":\"There is no artist associated with the artist id specified.\"}");
 						rs.close();
 						out.close(); db.close(); return;
 					}
 					rs.close();
 					// Insertion time!
 					PreparedStatement query = db.preparedStatement("INSERT INTO album (`name`, artist_id) VALUES (?, ?)");
 					query.setString(1, name);
 					query.setInt(2, Integer.parseInt(artist_id));
 					query.executeUpdate();
 					rs = db.query("SELECT LAST_INSERT_ID()");
 					rs.next();
 					out.println("{\"success\":true;\"id\":"+rs.getInt(1)+"}");
 					rs.close();
 					out.close(); db.close(); return;
 				}
 				// Invalid numbers!
 				catch (NumberFormatException e)
 				{
 					out.println("{\"success\":false,\"error\":\"One of the integer parameters was out of range or an invalid number.\"}");
 					out.close(); db.close(); return;
 				}
 				// Oh noes!
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in running the insertion.\"}");
 					//out.println(e);
 					out.close(); db.close(); return;
 				}
 			}
 			else if (type.equalsIgnoreCase("artist"))
 			{
 				//create artist
 				String name = req.getParameter("name");
 				// Do we have all parameters?
 				if (name == null) 
 				{
 					out.println("{\"success\":false,\"error\":\"Missing a parameter needed for artist creation.\"}");
 					out.close(); db.close(); return;
 				}
 				try 
 				{
 					// Insertion time!
 					PreparedStatement query = db.preparedStatement("INSERT INTO artist (`name`) VALUES (?)");
 					query.setString(1, name);
 					query.executeUpdate();
 					ResultSet rs = db.query("SELECT LAST_INSERT_ID()");
 					rs.next();
 					out.println("{\"success\":true;\"id\":"+rs.getInt(1)+"}");
 					rs.close();
 					out.close(); db.close(); return;
 				}
 				// Oh noes!
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in running the insertion.\"}");
 					//out.println(e);
 					out.close(); db.close(); return;
 				}
 			}
 			else if (type.equalsIgnoreCase("playlist"))
 			{
 				//create playlist (from list of songs)
 				String name = req.getParameter("name");
 				String songs = req.getParameter("songs");
 				// Do we have all parameters?
 				if (name == null || songs == null) 
 				{
 					out.println("{\"success\":false,\"error\":\"Missing a parameter needed for playlist creation.\"}");
 					out.close(); db.close(); return;
 				}
 				int[] song_ids;
 				try
 				{
 					//get the integer array of song ids
 					song_ids = gson.fromJson(songs, int[].class); 
 					for (int i = 0; i < song_ids.length; i++)
 					{
 						//check if each song id valid
 						if (song_ids[i] < 1) throw new Exception();
 					}
 					//clip off the square brackets
 					String song_id_list = songs.substring(1, songs.length() - 1);
 					//check all song ids are valid
 					ResultSet rs = db.query("SELECT COUNT(*) FROM song WHERE song_id IN ("+song_id_list+")");
 					rs.next();
 					if (rs.getInt(1) != song_ids.length) //check length of results
 					{
 						out.println("{\"success\":false,\"error\":\"At least one of the provided songs does not exist.\"}");
 						out.close(); db.close(); return;
 					}
 					rs.close();
 					//all good, insertion time!
 					PreparedStatement query = db.preparedStatement("INSERT INTO playlist (`name`, member_id) VALUES (?, ?)");
 					query.setString(1, name);
 					query.setInt(2, uid);
 					query.executeUpdate();
 					rs = db.query("SELECT LAST_INSERT_ID()");
 					rs.next();
 					int playlist_id = rs.getInt(1);
 					for (int i = 0; i < song_ids.length; i++)
 					{
 						query = db.preparedStatement("INSERT INTO playlistsong (playlist_id, song_id, track_number) VALUES (?, ?, ?)");
 						query.setInt(1, playlist_id);
 						query.setInt(2, song_ids[i]);
 						query.setInt(3, i);
 						query.executeUpdate();
 					}
 					rs.close();
 					out.println("{\"success\":true;\"id\":"+playlist_id+"}");
 					out.close(); db.close(); return;
 				}
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"The list of song ids is malformed, contains invalid numbers, or has numbers outside the valid range.\"}");
 					//out.println(e.getMessage());
 					out.close(); db.close(); return;
 				}
 			}
 			else if (type.equalsIgnoreCase("member"))
 			{
 				//create a new member
 				String username = req.getParameter("username");
 				String password = req.getParameter("password");
 				// Do we have all parameters?
 				if (username == null || password == null) 
 				{
 					out.println("{\"success\":false,\"error\":\"Missing a parameter needed for member creation.\"}");
 					out.close(); db.close(); return;
 				}
 				try 
 				{
 					PreparedStatement query = db.preparedStatement("SELECT username FROM member WHERE username = ?");
 					query.setString(1, username);
 					ResultSet rs = query.executeQuery();
 					if (rs.next())
 					{
 						out.println("{\"success\":false,\"error\":\"There is already a user with this username.\"}");
 						out.close(); db.close(); return;
 					}
 					rs.close();
 					query = db.preparedStatement("INSERT INTO member (username, password) VALUES (?, ?)");
 					query.setString(1, username);
 					query.setString(2, password);
 					query.executeUpdate();
 					rs = db.query("SELECT LAST_INSERT_ID()");
 					rs.next();
 					uid = rs.getInt(1);
 					rs.close();
 					session.setAttribute("user_id", "" + uid);
 					out.println("{\"success\":true}");
 					out.close(); db.close(); return;
 				}
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in creating a new member.\"}");
 					//out.println(e.getMessage());
 					out.close(); db.close(); return;
 				}
 			}
 		}
 		else if (method.equalsIgnoreCase("read"))
 		{
 			String search = req.getParameter("search"); //optional query string
 			String sort = req.getParameter("sort"); //optional sort (asc or desc)
 			String id = req.getParameter("id"); //optional id for a single item
 			String duration = req.getParameter("duration"); //optional limit count of result
 			String start = req.getParameter("start"); //optional start index of result
 			if (id != null) //parse the id if exists
 			{
 				try
 				{
 					if (Integer.parseInt(id) < 1)
 						throw new NumberFormatException();
 				}
 				catch (NumberFormatException e)
 				{
 					out.println("{\"success\":false,\"error\":\"ID must be an integer above zero.\"}");
 					out.close(); db.close(); return;
 				}
 			}
 			if (start != null) //parse the start index if exists
 			{
 				try
 				{
 					if (Integer.parseInt(start) < 0)
 						throw new NumberFormatException();
 				}
 				catch (NumberFormatException e)
 				{
 					out.println("{\"success\":false,\"error\":\"Start index must be an integer at or above zero.\"}");
 					out.close(); db.close(); return;
 				}
 			}
 			if (duration != null) //parse the count limit if exists
 			{
 				try
 				{
 					if (Integer.parseInt(duration) < 1)
 						throw new NumberFormatException();
 				}
 				catch (NumberFormatException e)
 				{
 					out.println("{\"success\":false,\"error\":\"Limit duration must be an integer above zero.\"}");
 					out.close(); db.close(); return;
 				}
 			}
 			if (sort != null) //parse the sort if exists
 			{
 				if (sort.equalsIgnoreCase("asc"))
 					sort = "ASC";
 				else if (sort.equalsIgnoreCase("desc"))
 					sort = "DESC";
 				else
 					sort = null; //invalid sort parameter, ignore
 			}
 			if (type.equalsIgnoreCase("playlists"))
 			{
 				//get list of playlists [name, list of songs]
 				//	can search by member or random
 				//	can sort by playlist name
 				//	can also specify by id to return that playlist
 				String queryStr = "SELECT * FROM playlist NATURAL JOIN member";
 				if (id != null) //return just one result
 				{
 					queryStr += " WHERE playlist_id = ?";
 				}
 				else if (search != null) //searching
 				{
 					queryStr += " WHERE `name` LIKE ?";
 				}
 				if (sort != null)
 				{
 					queryStr += " ORDER BY `name` " + sort;
 				}
 				else
 				{
 					queryStr += " ORDER BY playlist_id DESC";
 				}
 				if (duration != null) //limit
 				{
 					queryStr += " LIMIT ";
 					if (start != null)
 					{
 						queryStr += start + ", ";
 					}
 					queryStr += duration;
 				}
 				try
 				{
 					PreparedStatement query = db.preparedStatement(queryStr);
 					if (id != null)
 						query.setInt(1, Integer.parseInt(id));
 					else if (search != null)
 						query.setString(1, "%"+search+"%");
 					ResultSet rs = query.executeQuery();
 					ArrayList<Playlist> list = new ArrayList<Playlist>();
 					while (rs.next())
 					{
 						Playlist pl = new Playlist(rs.getInt("playlist_id"), rs.getInt("member_id"), rs.getString("name"), rs.getString("username"));
 						PreparedStatement query2 = db.preparedStatement("SELECT song.song_id, artist.artist_id, album.album_id, song.name AS song_name, artist.name AS artist_name, album.name AS album_name, playlistsong.track_number FROM playlistsong, song, artist, album WHERE playlistsong.playlist_id = ? AND playlistsong.song_id = song.song_id AND song.artist_id = artist.artist_id AND song.album_id = album.album_id ORDER BY track_number ASC");
 						query2.setInt(1, rs.getInt("playlist_id"));
 						ResultSet rs2 = query2.executeQuery();
 						while (rs2.next())
 						{
 							//Song(int id, String n, int t, int ali, String aln, int ari, String arn)
 							pl.addSong(new Song(rs2.getInt("song_id"), rs2.getString("song_name"), rs2.getInt("track_number"), rs2.getInt("album_id"), rs2.getString("album_name"), rs2.getInt("artist_id"), rs2.getString("artist_name")));
 						}
 						rs2.close();
 						list.add(pl);
 					}
 					rs.close();
 					out.println("{\"success\":true,\"results\":"+gson.toJson(list)+"}");
 					out.close(); db.close(); return;
 				}
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in searching for playlists.\"}");
 					//out.println(e.getMessage());
 					out.close(); db.close(); return;
 				}
 			}
 			else if (type.equalsIgnoreCase("artists"))
 			{
 				//get list of artists [name, list of albums]
 				//	can search by artist name
 				//	can sort by artist name
 				//	can also specify by id to return that artist
 				String queryStr = "SELECT * FROM artist";
 				if (id != null) //return just one result
 				{
 					queryStr += " WHERE artist_id = ?";
 				}
 				else if (search != null) //searching
 				{
 					queryStr += " WHERE `name` LIKE ?";
 				}
 				if (sort != null)
 				{
 					queryStr += " ORDER BY `name` " + sort;
 				}
 				else
 				{
 					queryStr += " ORDER BY artist_id DESC";
 				}
 				if (duration != null) //limit
 				{
 					queryStr += " LIMIT ";
 					if (start != null)
 					{
 						queryStr += start + ", ";
 					}
 					queryStr += duration;
 				}
 				try
 				{
 					PreparedStatement query = db.preparedStatement(queryStr);
 					if (id != null)
 						query.setInt(1, Integer.parseInt(id));
 					else if (search != null)
 						query.setString(1, "%"+search+"%");
 					ResultSet rs = query.executeQuery();
 					ArrayList<Artist> list = new ArrayList<Artist>();
 					while (rs.next())
 					{
 						Artist ar = new Artist(rs.getInt("artist_id"), rs.getString("name"));
 						PreparedStatement query2 = db.preparedStatement("SELECT * FROM album WHERE artist_id = ? ORDER BY name ASC");
 						query2.setInt(1, rs.getInt("artist_id"));
 						ResultSet rs2 = query2.executeQuery();
 						while (rs2.next())
 						{
 							ar.addAlbum(new Album(rs2.getInt("album_id"), -1, "", rs2.getString("name")));
 						}
 						rs2.close();
 						list.add(ar);
 					}
 					rs.close();
 					out.println("{\"success\":true,\"results\":"+gson.toJson(list)+"}");
 					out.close(); db.close(); return;
 				}
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in searching for artists.\"}");
 					//out.println(e.getMessage());
 					out.close(); db.close(); return;
 				}
 			}
 			else if (type.equalsIgnoreCase("albums"))
 			{
 				//get list of albums [name, artist info, list of songs]
 				//	can search by album name
 				//	can sort by album name, artist name 
 				//	can also specify an id to return that album
 				String queryStr = "SELECT album_id, artist.artist_id, artist.name AS artist_name, album.name AS album_name FROM album, artist WHERE artist.artist_id = album.artist_id";
 				if (id != null) //return just one result
 				{
 					queryStr += " AND album.album_id = ?";
 				}
 				else if (search != null) //searching
 				{
 					queryStr += " AND album.name LIKE ?";
 				}
 				if (sort != null)
 				{
 					queryStr += " ORDER BY album.name " + sort;
 				}
 				else
 				{
 					queryStr += " ORDER BY artist.artist_id DESC";
 				}
 				if (duration != null) //limit
 				{
 					queryStr += " LIMIT ";
 					if (start != null)
 					{
 						queryStr += start + ", ";
 					}
 					queryStr += duration;
 				}
 				try
 				{
 					PreparedStatement query = db.preparedStatement(queryStr);
 					if (id != null)
 						query.setInt(1, Integer.parseInt(id));
 					else if (search != null)
 						query.setString(1, "%"+search+"%");
 					ResultSet rs = query.executeQuery();
 					ArrayList<Album> list = new ArrayList<Album>();
 					while (rs.next())
 					{
 						Album al = new Album(rs.getInt("album_id"), rs.getInt("artist_id"), rs.getString("artist_name"), rs.getString("album_name"));
 						PreparedStatement query2 = db.preparedStatement("SELECT artist.artist_id, song_id, song.name AS song_name, track_number, artist.name AS artist_name FROM song, artist WHERE artist.artist_id = song.artist_id AND album_id = ? ORDER BY track_number ASC");
 						query2.setInt(1, rs.getInt("album_id"));
 						ResultSet rs2 = query2.executeQuery();
 						while (rs2.next())
 						{
 							al.addSong(new Song(rs2.getInt("song_id"), rs2.getString("song_name"), rs2.getInt("track_number"), -1, null, rs2.getInt("artist_id"), rs2.getString("artist_name")));
 						}
 						rs2.close();
 						list.add(al);
 					}
 					rs.close();
 					out.println("{\"success\":true,\"results\":"+gson.toJson(list)+"}");
 					out.close(); db.close(); return;
 				}
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in searching for playlists.\"}");
 					//out.println(e.getMessage());
 					out.close(); db.close(); return;
 				}
 			}
 			else if (type.equalsIgnoreCase("members"))
 			{
 				//get list of members [info, list of playlists]
 				//	can search by member name
 				//	can sort by member name
 				//	can also specify an id to return that member
 				String queryStr = "SELECT member_id, username FROM member";
 				if (id != null) //return just one result
 				{
 					queryStr += " WHERE member_id = ?";
 				}
 				else if (search != null) //searching
 				{
 					queryStr += " WHERE username LIKE ?";
 				}
 				if (sort != null)
 				{
 					queryStr += " ORDER BY username " + sort;
 				}
 				else
 				{
 					queryStr += " ORDER BY member_id DESC";
 				}
 				if (duration != null) //limit
 				{
 					queryStr += " LIMIT ";
 					if (start != null)
 					{
 						queryStr += start + ", ";
 					}
 					queryStr += duration;
 				}
 				try
 				{
 					PreparedStatement query = db.preparedStatement(queryStr);
 					if (id != null)
 						query.setInt(1, Integer.parseInt(id));
 					else if (search != null)
 						query.setString(1, "%"+search+"%");
 					ResultSet rs = query.executeQuery();
 					ArrayList<Member> list = new ArrayList<Member>();
 					while (rs.next())
 					{
 						Member m = new Member(rs.getInt("member_id"), rs.getString("username"));
 						PreparedStatement query2 = db.preparedStatement("SELECT * FROM playlist WHERE member_id = ? ORDER BY `name` ASC");
 						query2.setInt(1, rs.getInt("member_id"));
 						ResultSet rs2 = query2.executeQuery();
 						while (rs2.next())
 						{
 							m.addPlaylist(new Playlist(rs2.getInt("playlist_id"), -1, rs2.getString("name"), ""));
 						}
 						rs2.close();
 						list.add(m);
 					}
 					rs.close();
 					out.println("{\"success\":true,\"results\":"+gson.toJson(list)+"}");
 					out.close(); db.close(); return;
 				}
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in searching for artists.\"}");
 					//out.println(e.getMessage());
 					out.close(); db.close(); return;
 				}
 			}
 		}
 		else if (method.equalsIgnoreCase("update"))
 		{
 			String action = req.getParameter("action");
 			if (type.equalsIgnoreCase("playlist"))
 			{
 				if (uid == 0)
 				{
 					out.println("{\"success\":false,\"error\":\"You must be logged in to take this action.\"}");
 					out.close(); db.close(); return;
 				}
 				String playlist_id = req.getParameter("playlist_id");
 				if (playlist_id == null)
 				{
 					out.println("{\"success\":false,\"error\":\"No playlist was specified.\"}");
 					out.close(); db.close(); return;
 				}
 				try
 				{
 					PreparedStatement query = db.preparedStatement("SELECT member_id FROM playlist WHERE playlist_id = ? AND member_id = ?");
 					query.setInt(1, Integer.parseInt(playlist_id));
 					query.setInt(2, uid);
 					ResultSet rs = query.executeQuery();
 					if (!rs.next())
 					{
 						out.println("{\"success\":false,\"error\":\"The specified playlist could not be found.\"}");
 						rs.close();
 						out.close(); db.close(); return;
 					}
 					rs.close();
 				}
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in checking the playlist.\"}");
 					//out.println(e);
 					out.close(); db.close(); return;
 				}
 				if (action.equalsIgnoreCase("rename"))
 				{
 					//renames playlist
 					String name = req.getParameter("name");
 					// Do we have all parameters?
 					if (name == null) 
 					{
 						out.println("{\"success\":false,\"error\":\"Missing a parameter needed for playlist update.\"}");
 						out.close(); db.close(); return;
 					}
 					try 
 					{
 						PreparedStatement query = db.preparedStatement("UPDATE playlist SET `name` = ? WHERE playlist_id = ?");
 						query.setString(1, name);
 						query.setInt(2, Integer.parseInt(playlist_id));
 						query.executeUpdate();
 						out.println("{\"success\":true}");
 					}
 					catch (Exception e)
 					{
 						out.println("{\"success\":false,\"error\":\"There was an error in renaming the playlist.\"}");
 						//out.println(e);
 						out.close(); db.close(); return;
 					}
 					out.println("{\"success\":true}");
 					out.close(); db.close(); return;
 				}
 				else if (action.equalsIgnoreCase("addsongs"))
 				{
 					//adds list of song ids to playlist
 					String songs = req.getParameter("songs");
 					// Do we have all parameters?
 					if (songs == null) 
 					{
 						out.println("{\"success\":false,\"error\":\"Missing a parameter needed for playlist update.\"}");
 						out.close(); db.close(); return;
 					}
 					try 
 					{
 						int[] song_ids = gson.fromJson(songs, int[].class);
 						for (int i = 0; i < song_ids.length; i++)
 						{
 							//check if each song id valid
 							if (song_ids[i] < 1) throw new Exception();
 						}
 						//clip off the square brackets
 						String song_id_list = songs.substring(1, songs.length() - 1);
 						//check all songs exist
 						ResultSet rs = db.query("SELECT COUNT(*) FROM song WHERE song_id IN ("+song_id_list+")");
 						rs.next();
 						if (rs.getInt(1) != song_ids.length) //check length of results
 						{
 							out.println("{\"success\":false,\"error\":\"At least one of the provided songs does not exist.\"}");
 							rs.close();
 							out.close(); db.close(); return;
 						}
 						rs.close();
 						//remove any songs already in the playlist
 						rs = db.query("SELECT song_id FROM playlistsong WHERE playlist_id = "+Integer.parseInt(playlist_id)+" AND song_id IN ("+song_id_list+")");
 						ArrayList<Integer> existing = new ArrayList<Integer>();
 						while (rs.next())
 						{
 							existing.add(new Integer(rs.getInt("song_id")));
 						}
 						rs.close();
 						ArrayList<Integer> adding = new ArrayList<Integer>();
 						for (int i = 0; i < song_ids.length; i++)
 						{
 							//only add songs to the add list if they don't already exist
 							if (!existing.contains(new Integer(song_ids[i])))
 								adding.add(new Integer(song_ids[i]));
 						}
 						//get the current highest value of track num
 						rs = db.query("SELECT MAX(track_number) FROM playlistsong WHERE playlist_id = "+Integer.parseInt(playlist_id));
 						rs.next();
 						int nextTrackNum = rs.getInt(1) + 1;
 						rs.close();
 						//finally add the songs to the playlist
 						for (int i = 0; i < adding.size(); i++)
 						{
 							PreparedStatement query = db.preparedStatement("INSERT INTO playlistsong (playlist_id, song_id, track_number) VALUES (?, ?, ?)");
 							query.setInt(1, Integer.parseInt(playlist_id));
 							query.setInt(2, adding.get(i).intValue());
 							query.setInt(3, nextTrackNum++);
 							query.executeUpdate();
 						}
 						out.println("{\"success\":true}");
 						out.close(); db.close(); return;
 					}
 					catch (Exception e)
 					{
 						out.println("{\"success\":false,\"error\":\"The list of song ids is malformed, contains invalid numbers, or has numbers outside the valid range.\"}");
 						//out.println(e);
 						out.close(); db.close(); return;
 					}
 				}
 				else if (action.equalsIgnoreCase("removesongs"))
 				{
 					//removes list of song ids from playlist
 					String songs = req.getParameter("songs");
 					// Do we have all parameters?
 					if (songs == null) 
 					{
 						out.println("{\"success\":false,\"error\":\"Missing a parameter needed for playlist update.\"}");
 						out.close(); db.close(); return;
 					}
 					try 
 					{
 						int[] song_ids = gson.fromJson(songs, int[].class);
 						for (int i = 0; i < song_ids.length; i++)
 						{
 							//check if each song id valid
 							if (song_ids[i] < 1) throw new Exception();
 						}
 						//clip off the square brackets
 						String song_id_list = songs.substring(1, songs.length() - 1);
 						//just delete all song ids listed from the playlist
 						//	no point in checking if songs are even in playlist
 						PreparedStatement query = db.preparedStatement("DELETE FROM playlistsong WHERE playlist_id = ? AND song_id IN ("+song_id_list+")");
 						query.setInt(1, Integer.parseInt(playlist_id));
 						query.executeUpdate();
 						//fix the track numbers while maintaining the order
 						query = db.preparedStatement("SELECT playlistsong_id FROM playlistsong WHERE playlist_id = ? ORDER BY track_number ASC");
 						query.setInt(1, Integer.parseInt(playlist_id));
 						ResultSet rs = query.executeQuery();
 						int track_num = 1;
 						while (rs.next())
 						{
 							query = db.preparedStatement("UPDATE playlistsong SET track_number = ? WHERE playlistsong_id = ?");
 							query.setInt(1, track_num++);
 							query.setInt(2, rs.getInt(1));
 							query.executeUpdate();
 						}
 						rs.close();
 						out.println("{\"success\":true}");
 						out.close(); db.close(); return;
 					}
 					catch (Exception e)
 					{
 						out.println("{\"success\":false,\"error\":\"The list of song ids is malformed, contains invalid numbers, or has numbers outside the valid range.\"}");
 						//out.println(e);
 						out.close(); db.close(); return;
 					}
 				}
 			}
 			else if (type.equalsIgnoreCase("user"))
 			{
 				if (action.equalsIgnoreCase("login") && uid != 0)
 				{
 					out.println("{\"success\":false,\"error\":\"You are already logged in.\"}");
 					out.close(); db.close(); return;
 				}
 				if (action.equalsIgnoreCase("logout") && uid == 0)
 				{
 					out.println("{\"success\":false,\"error\":\"You are not logged in.\"}");
 					out.close(); db.close(); return;
 				}
 				if (action.equalsIgnoreCase("login"))
 				{
 					//login user
 					String username = req.getParameter("username");
 					String password = req.getParameter("password");
 					// Do we have all parameters?
 					if (username == null || password == null) 
 					{
 						out.println("{\"success\":false,\"error\":\"Missing a parameter needed for login.\"}");
 						out.close(); db.close(); return;
 					}
 					try
 					{
 						PreparedStatement query = db.preparedStatement("SELECT member_id FROM member WHERE username = ? AND password = ?");
 						query.setString(1, username);
 						query.setString(2, password);
 						ResultSet rs = query.executeQuery();
 						if (rs.next())
 						{
 							session.setAttribute("user_id", "" + rs.getInt("member_id"));
 							out.println("{\"success\":true}");
 							rs.close();
 							out.close(); db.close(); return;
 						}
 						else
 						{
 							session.setAttribute("user_id", "" + 0);
 							out.println("{\"success\":false,\"error\":\"Invalid username/password combination.\"}");
 							rs.close();
 							out.close(); db.close(); return;
 						}
 					}
 					catch (Exception e)
 					{
 						out.println("{\"success\":false,\"error\":\"There was an error in logging in.\"}");
 						//out.println(e);
 						out.close(); db.close(); return;
 					}
 				}
 				else if (action.equalsIgnoreCase("logout"))
 				{
 					//logout user
 					session.setAttribute("user_id", "" + 0);
 					out.println("{\"success\":true}");
 				}
 			}
 		}
 		else if (method.equalsIgnoreCase("delete"))
 		{
 			if (uid == 0)
 			{
 				out.println("{\"success\":false,\"error\":\"You need to be logged in to do this action.\"}");
 				out.close(); db.close(); return;
 			}
 			if (type.equalsIgnoreCase("playlist"))
 			{
 				//deletes playlist with given id 
 				String playlist_id = req.getParameter("playlist_id");
 				// Do we have all parameters?
 				if (playlist_id == null) 
 				{
 					out.println("{\"success\":false,\"error\":\"Missing a parameter needed for playlist deletion.\"}");
 					out.close(); db.close(); return;
 				}
 				try
 				{
 					ResultSet rs = db.query("SELECT playlist_id FROM playlist WHERE playlist_id = " + Integer.parseInt(playlist_id) + " AND member_id = " + uid);
 					if (!rs.next())
 					{
 						out.println("{\"success\":false,\"error\":\"The specified playlist does not exist.\"}");
 						rs.close();
 						out.close(); db.close(); return;
 					}
 					rs.close();
 					db.query("DELETE FROM playlist WHERE playlist_id = " + Integer.parseInt(playlist_id) + " AND member_id = " + uid);
 					db.query("DELETE FROM playlistsong WHERE playlist_id = " + Integer.parseInt(playlist_id));
 					out.println("{\"success\":true}");
 				}
 				catch (NumberFormatException e)
 				{
 					out.println("{\"success\":false,\"error\":\"One of the integer parameters was out of range or an invalid number.\"}");
 					out.close(); db.close(); return;
 				}
 				// Oh noes!
 				catch (Exception e)
 				{
 					out.println("{\"success\":false,\"error\":\"There was an error in running the deletion.\"}");
 					//out.println(e);
 					out.close(); db.close(); return;
 				}
 			}
 		}
 		out.println("{\"success\":false,\"error\":\"This action does not exist.\"}");
 		out.close();
 	}
}
