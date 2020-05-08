 /**
 * Copyright © 2012 Joseph Walton-Rivers <webpigeon@unitycoders.co.uk>
  * Copyright © 2012 Bruce Cowan <bruce@bcowan.me.uk>
  *
  * This file is part of uc_PircBotX.
  *
  * uc_PircBotX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * uc_PircBotX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with uc_PircBotX.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.unitycoders.pircbotx.data.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import uk.co.unitycoders.pircbotx.types.Lart;
 
 /**
  * Stores a load of larts.
  * 
  * @author Joseph Walton-Rivers
  * @author Bruce Cowan
  */
 public class LartModel
 {
 	private final Connection conn;
 	private final PreparedStatement createLart;
 	private final PreparedStatement readLarts;
 	private final PreparedStatement deleteLart;
 	private final PreparedStatement specificLart;
 	private final PreparedStatement randomLart;
 	private final PreparedStatement lastId;
 	private final PreparedStatement alterLart;
 
 	/**
 	 * Creates a new LartModel.
 	 *
 	 * @param conn the database connection
 	 * @throws SQLException if there was a database error
 	 */
 	public LartModel(Connection conn) throws SQLException
 	{
 		this.conn = conn;
 
 		buildTable();
 		this.createLart = conn.prepareStatement("INSERT INTO larts (channel, nick, pattern) VALUES (?, ?, ?)");
 		this.readLarts = conn.prepareStatement("SELECT * FROM larts");
 		this.deleteLart = conn.prepareStatement("DELETE FROM larts WHERE id = ?");
 		this.specificLart = conn.prepareStatement("SELECT * FROM larts WHERE id = ?");
 		this.randomLart = conn.prepareStatement("SELECT * FROM larts ORDER BY RANDOM() LIMIT 1");
 		this.lastId = conn.prepareStatement("SELECT last_insert_rowid();");
 		this.alterLart = conn.prepareStatement("UPDATE larts SET channel = ?, nick = ?, pattern = ? WHERE id = ?");
 	}
 
 	private void buildTable() throws SQLException
 	{
 		Statement stmt = this.conn.createStatement();
 		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS larts"
 				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, channel TEXT, nick TEXT, pattern TEXT)");
 	}
 
 	/**
 	 * Stores a lart in the database.
 	 *
 	 * @param channel the channel where the lart belongs to
 	 * @param user the user who created the lart
 	 * @param pattern the pattern of the lart
 	 * @return the ID of the newly-created lart
 	 * @throws IllegalArgumentException if no $who section is given
 	 * @throws SQLException if there was a database error
 	 */
 	public int storeLart(Channel channel, User user, String pattern) throws IllegalArgumentException, SQLException
 	{
 		if (!pattern.contains("$who"))
 			throw new IllegalArgumentException("No $who section found");
 
 		createLart.clearParameters();
 		createLart.setString(1, channel.getName());
 		createLart.setString(2, user.getNick());
 		createLart.setString(3, pattern);
 		createLart.execute();
 
 		// Do this manually because getGeneratedKeys() is broken
 		lastId.clearParameters();
 		lastId.execute();
 		ResultSet rs = lastId.getResultSet();
 		return rs.getInt(1);
 	}
 
 	/**
 	 * Deletes a lart from the database.
 	 *
 	 * @param id the ID of the lart to delete
 	 * @return <code>true</code> if successful, <code>false</code> if not
 	 * @throws SQLException if there was a database error
 	 */
 	public boolean deleteLart(int id) throws SQLException
 	{
 		deleteLart.clearParameters();
 		deleteLart.setInt(1, id);
 		return (deleteLart.executeUpdate() > 0);
 	}
 
 	private Lart buildLart(ResultSet rs) throws SQLException
 	{
 		int id = rs.getInt(1);
 		String channel = rs.getString(2);
 		String nick = rs.getString(3);
 		String pattern = rs.getString(4);
 		return new Lart(id, channel, nick, pattern);
 	}
 
 	/**
 	 * Gets a {@link Lart} from the database.
 	 *
 	 * @param id the ID of the lart to get
 	 * @return the lart
 	 * @throws SQLException if there was a database error
 	 */
 	public Lart getLart(int id) throws SQLException
 	{
 		specificLart.clearParameters();
 		specificLart.setInt(1, id);
 		specificLart.execute();
 
 		ResultSet rs = specificLart.getResultSet();
 		return buildLart(rs);
 	}
 
 	/**
 	 * Gets a random {@link Lart} from the database.
 	 *
 	 * @return a random lart
 	 * @throws SQLException if there was a database error
 	 */
 	public Lart getRandomLart() throws SQLException
 	{
 		ResultSet rs = randomLart.executeQuery();
 		return buildLart(rs);
 	}
 
 	/**
 	 * Gets a {@link List} of all {@link Lart}s in the database.
 	 *
 	 * @return a list of all the larts
 	 */
 	public List<Lart> getAllLarts()
 	{
 		List<Lart> larts = new ArrayList<Lart>();
 
 		try
 		{
 			ResultSet rs = readLarts.executeQuery();
 			while (rs.next())
 			{
 				int id = rs.getInt(1);
 				String channel = rs.getString(2);
 				String nick = rs.getString(3);
 				String pattern = rs.getString(4);
 
 				Lart lart = new Lart(id, channel, nick, pattern);
 				larts.add(lart);
 			}
 			rs.close();
 		} catch (SQLException ex)
 		{
 			ex.printStackTrace();
 		}
 
 		return larts;
 	}
 
 	public void alterLart(int id, Channel channel, User user, String pattern) throws SQLException
 	{
 		if (!pattern.contains("$who"))
 			throw new IllegalArgumentException("No $who section found");
 
 		alterLart.clearParameters();
 		alterLart.setString(1, channel.getName());
 		alterLart.setString(2, user.getNick());
 		alterLart.setString(3, pattern);
 		alterLart.setInt(4, id);
 		alterLart.execute();
 	}
 }
