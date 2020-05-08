 package uk.co.brotherlogic.mdb.artist;
 
 /**
  * Class to deal with getting groops
  * @author Simon Tucker
  * NB: Updated for Postgres
  */
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import uk.co.brotherlogic.mdb.Cache;
 import uk.co.brotherlogic.mdb.Connect;
 import uk.co.brotherlogic.mdb.Utils;
 
 public class GetArtists
 {
 	public static GetArtists create() throws SQLException
 	{
 		if (singleton == null)
 			singleton = new GetArtists();
 
 		return singleton;
 	}
 
 	public static void main(String[] args) throws Exception
 	{
 		System.err.println(GetArtists.create().search("ampton").size());
 	}
 
 	// Map of name --> artist
 	private final Map<String, Artist> artists;
 
 	private final Map<String, Artist> tempStore;
 
 	private final Cache<Artist> artistCache = new Cache<Artist>();
 	// Prepared Statements to use
 	private final PreparedStatement insertQuery;
 
 	private final PreparedStatement collectQuery;
 	private final PreparedStatement collectQueryShowName;
 
 	private boolean executed = false;
 
 	private static GetArtists singleton;
 
 	private GetArtists() throws SQLException
 	{
 		// Set the required parameters
 		tempStore = new TreeMap<String, Artist>();
 
 		// Initialise the Set
 		artists = new TreeMap<String, Artist>();
 
 		// Build the set
 		insertQuery = Connect.getConnection().getPreparedStatement(
 				"INSERT INTO Artist (sort_name, show_name) VALUES (?,?)");
 		collectQuery = Connect.getConnection().getPreparedStatement(
 				"SELECT artist_id,show_name FROM Artist WHERE sort_name = ?");
 		collectQueryShowName = Connect.getConnection().getPreparedStatement(
 				"SELECT artist_id,sort_name FROM Artist WHERE show_name = ?");
 	}
 
 	public int[] addArtists(Collection<Artist> art) throws SQLException
 	{
 		// Prepare the array
 		int[] ret = new int[art.size()];
 
 		// Iterate through the array
 		int count = 0;
 		for (Artist tempArt : art)
 		{
 			// Save the artist
 			ret[count] = saveArtist(tempArt);
 
 			// Increment the count
 			count++;
 		}
 
 		// Return the constructed array
 		return ret;
 	}
 
 	public void cancel()
 	{
 		// Necessary for this to finish, so just leave in background
 	}
 
 	public void commitArtists()
 	{
 		artists.putAll(tempStore);
 		tempStore.clear();
 	}
 
 	public void execute() throws SQLException
 	{
 		// Get a statement and run the query
 		PreparedStatement s = Connect.getConnection().getPreparedStatement(
 				"SELECT sort_name,artist_id,show_name FROM Artist");
 		ResultSet rs = s.executeQuery();
 
 		// Fill the set
 		while (rs.next())
 		{
 			String art = rs.getString(1);
 			int num = rs.getInt(2);
 			String show = rs.getString(3);
 
 			artists.put(art, new Artist(art, show, num));
 		}
 
 		// Close the database objects
 		rs.close();
 		s.close();
 
 		executed = true;
 	}
 
 	public boolean exist(String name)
 	{
 		return artists.keySet().contains(name);
 	}
 
 	public Artist getArtist(int num) throws SQLException
 	{
 
 		Artist artist = artistCache.get(num);
 
 		if (artist == null)
 		{
 			PreparedStatement s = Connect
 					.getConnection()
 					.getPreparedStatement(
 							"SELECT sort_name, show_name FROM Artist WHERE artist_id = ?");
 			s.setInt(1, num);
 			ResultSet rs = Connect.getConnection().executeQuery(s);
 
 			// Move on and return the relevant artust
 			rs.next();
 			String sort = rs.getString(1);
 			String show = rs.getString(2);
 
 			rs.close();
 			s.close();
 
 			// Add this new artist
 			artist = new Artist(sort, show, num);
 			artistCache.add(num, artist);
 		}
 
 		return artist;
 	}
 
 	public Artist getArtist(String name) throws SQLException
 	{
 		if (exist(name))
 			return artists.get(name);
 		else if (tempStore.containsKey(name))
 			return tempStore.get(name);
 		else
 		{
 			collectQuery.setString(1, name);
 			ResultSet rs = collectQuery.executeQuery();
 
 			// Move on and return the relevant artust
 			if (rs.next())
 			{
 				int num = rs.getInt(1);
 				String showName = rs.getString(2);
 
 				rs.close();
 
 				// Add this new artist
 				artists.put(name, new Artist(name, showName, num));
 
 				return artists.get(name);
 			} else
 			{
 				rs.close();
 				return new Artist(name, Utils.flipString(name), -1);
 			}
 		}
 	}
 
 	public Artist getArtistFromShowName(String name) throws SQLException
 	{
 		if (exist(name))
 			return artists.get(name);
 		else if (tempStore.containsKey(name))
 			return tempStore.get(name);
 		else
 		{
 			collectQueryShowName.setString(1, name);
 			ResultSet rs = collectQueryShowName.executeQuery();
 
 			// Move on and return the relevant artust
 			if (rs.next())
 			{
 				int num = rs.getInt(1);
 				String sortName = rs.getString(2);
 
 				rs.close();
 
 				// Add this new artist
 				artists.put(name, new Artist(sortName, name, num));
 
 				return artists.get(name);
 			} else
 			{
 				rs.close();
 				return new Artist(name, Utils.flipString(name), -1);
 			}
 		}
 	}
 
 	public Collection<Artist> getArtists()
 	{
 		try
 		{
 			if (!executed)
 				execute();
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 
 		return artists.values();
 	}
 
 	public int saveArtist(Artist art) throws SQLException
 	{
 		int newID = -1;
 
 		// Check this artist doesn't already exist
 		collectQuery.setString(1, art.getSortName());
 		ResultSet rs = collectQuery.executeQuery();
 		if (!rs.next())
 		{
 			// Add this new artist
 			insertQuery.setString(1, art.getSortName());
 			insertQuery.setString(2, art.getShowName());
 			insertQuery.execute();
 
 			// Get the new number
 			collectQuery.setString(1, art.getSortName());
 			rs = collectQuery.executeQuery();
 			rs.next();
 		}
 
 		newID = rs.getInt(1);
 		rs.close();
 
 		return newID;
 	}
 
 	public List<Artist> search(String query) throws SQLException
 	{
 		List<Artist> artists = new LinkedList<Artist>();
 		PreparedStatement s = Connect
 				.getConnection()
 				.getPreparedStatement(
						"SELECT sort_name,artist_id,show_name FROM artist WHERE lower(show_name) like ?");
 		s.setString(1, "%" + query.toLowerCase() + "%");
 
 		ResultSet rs = Connect.getConnection().executeQuery(s);
 		while (rs.next())
 		{
 			Artist art = new Artist(rs.getString(1), rs.getString(3), rs
 					.getInt(2));
 			artists.add(art);
 		}
 
 		return artists;
 
 	}
 }
