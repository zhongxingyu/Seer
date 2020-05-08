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
 		res.setContentType("application/json");
 		
 		PrintWriter out = res.getWriter();
 		DBAL db = DBAL.getInstance(out);
 		
 		HttpSession session = req.getSession(true);
 		//boolean isLoggedIn = ((String)session.getAttribute("logged-in")).equals("Y");
 		
 		String method = req.getParameter("method");
 		String type = req.getParameter("type");
 		
 		if (method == null || type == null)
 		{ 
 			out.println("{success:false,error:\"Missing a method or type parameter. Request aborted.\"}");
 			return;
 		}
 		
 		if (method.equalsIgnoreCase("create"))
 		{
 			if (type.equalsIgnoreCase("song"))
 			{
 				//create song, assigned to album
 				String name = req.getParameter("name");
 				String album_id = req.getParameter("album_id");
 				String artist_id = req.getParameter("artist_id");
 				String track_number = req.getParameter("track_number");
 				// Do we have all parameters?
 				if (name == null || album_id == null || track_number == null || artist_id == null) 
 				{
 					out.println("{success:false,error:\"Missing a parameter needed for song creation.\"}");
 					return;
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
						out.println("{success:false,error:\"There is no album associated with the album id specified.\"}");
						return;
					}
					rs = db.query("SELECT artist_id FROM artist WHERE artist_id = " + Integer.parseInt(artist_id));
					if (!rs.next())
					{
						out.println("{success:false,error:\"There is no artist associated with the artist id specified.\"}");
						return;
					}
 					// Insertion time!
 					PreparedStatement query = db.preparedStatement("INSERT INTO song (name, album_id, artist_id, track_number) VALUES (?, ?, ?, ?)");
 					query.setString(1, name);
 					query.setInt(2, Integer.parseInt(album_id));
 					query.setInt(3, Integer.parseInt(artist_id));
 					query.setInt(4, Integer.parseInt(track_number));
 					query.executeUpdate();
					rs = db.query("SELECT LAST_INSERT_ID()");
					rs.next();
					out.println("{success:true;id:"+rs.getInt(1)+"}");
 					return;
 				}
 				// Invalid numbers!
 				catch (NumberFormatException e)
 				{
 					out.println("{success:false,error:\"Either the `album_id` or `track_number` was not a valid number or index.\"}");
 					return;
 				}
 				// Oh noes!
 				catch (Exception e)
 				{
 					out.println("{success:false,error:\"There was an error in running the insertion.\"}");
					out.println(e);
 					return;
 				}
 			}
 			else if (type.equalsIgnoreCase("album"))
 			{
 				//create album, assigned to artist
 			}
 			else if (type.equalsIgnoreCase("artist"))
 			{
 				//create artist
 				String name = req.getParameter("name");
 			}
 			else if (type.equalsIgnoreCase("playlist"))
 			{
 				//create playlist (from list of songs)
 			}
 			else if (type.equalsIgnoreCase("member"))
 			{
 				//member signup
 			}
 		}
 		else if (method.equalsIgnoreCase("read"))
 		{
 			if (type.equalsIgnoreCase("playlists"))
 			{
 				//get list of playlists [name, list of songs]
 				//	can search by member or random
 				//	can sort by playlist name
 				//	can also specify by id to return that playlist
 			}
 			else if (type.equalsIgnoreCase("artists"))
 			{
 				//get list of artists [name, list of albums]
 				//	can search by artist info
 				//	can sort by artist name
 				//	can also specify by id to return that artist
 			}
 			else if (type.equalsIgnoreCase("albums"))
 			{
 				//get list of albums [name, artist info, list of songs]
 				//	can search by album name
 				//	can sort by album name, artist name 
 				//	can also specify an id to return that album
 			}
 			else if (type.equalsIgnoreCase("members"))
 			{
 				//get list of members [info, list of playlists]
 				//	can search by member name
 				//	can sort by member name
 				//	can also specify an id to return that member
 			}
 		}
 		else if (method.equalsIgnoreCase("update"))
 		{
 			String action = req.getParameter("action");
 			if (type.equalsIgnoreCase("playlist"))
 			{
 				if (action.equalsIgnoreCase("rename"))
 				{
 					//renames playlist
 				}
 				else if (action.equalsIgnoreCase("addsongs"))
 				{
 					//adds list of song ids to playlist
 				}
 				else if (action.equalsIgnoreCase("removesongs"))
 				{
 					//removes list of song ids from playlist
 				}
 			}
 			else if (type.equalsIgnoreCase("user"))
 			{
 				if (action.equalsIgnoreCase("login"))
 				{
 					String username = req.getParameter("username");
 					String password = req.getParameter("password");
 					//login user
 				}
 				else if (action.equalsIgnoreCase("logout"))
 				{
 					//logout user
 				}
 			}
 		}
 		else if (method.equalsIgnoreCase("delete"))
 		{
 			if (type.equalsIgnoreCase("playlist"))
 			{
 				//deletes playlist with given id 
 			}
 		}
 		out.close();
 	}
 }
