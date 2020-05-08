 package com.scurab.web.drifmaps.database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import com.scurab.web.drifmaps.server.MainApplication;
 import com.scurab.web.drifmaps.shared.datamodel.Detail;
 import com.scurab.web.drifmaps.shared.datamodel.MapItem;
 import com.scurab.web.drifmaps.shared.datamodel.Star;
 
 public class Database
 {	
 	/**
 	 * {@link MainApplication#readPropertiesFile()}
 	 */
 	public static String DBFILE = null;
 	private Connection mConnection = null;	
 	private Statement mStatement = null;
 	static
 	{
 		try
 		{
 			Class.forName("org.sqlite.JDBC");
 			
 		} 
 		catch (ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	protected Database(String fileName) throws SQLException
 	{
 		mConnection = DriverManager.getConnection(String.format("jdbc:sqlite:%s",fileName));		
 		mStatement = mConnection.createStatement();
 	}
 	
 	public Database() throws SQLException
 	{
 		mConnection = DriverManager.getConnection(String.format("jdbc:sqlite:%s",DBFILE));		
 		mStatement = mConnection.createStatement();
 	}
 	
 	/**
 	 * Delete all tables
 	 * @throws SQLException
 	 */
 	public void deleteAll() throws SQLException
 	{
 		for(String table : Structure.Tables.ALL_TABLES)
 		{
 			String qry = String.format(Structure.Queries.DELETE_DATA_FROM_TABLE_TEMPLATE, table);
 			mStatement.executeUpdate(qry);
 		}
 	}
 	
 	public void dropAll() throws SQLException
 	{
 		for(String table : Structure.Tables.ALL_TABLES)
 		{
 			try
 			{
 				String qry = String.format(Structure.Queries.DROP_TABLE_TEMPLATE, table);
 				mStatement.executeUpdate(qry);
 			}
 			catch(SQLException sqle)
 			{
 				if(!sqle.getMessage().equalsIgnoreCase(String.format("no such table: %s",table)))
 					throw sqle;
 					
 			}
 		}
 	}
 	
 	/**
 	 * Creates all neccessary tables
 	 * @throws SQLException
 	 */
 	public void createTables() throws SQLException
 	{
 		for(String qry : Structure.Queries.ALL_CREATE_TABLE_QUERIES)
 			mStatement.executeUpdate(qry);
 	}	
 	
 	/**
 	 * Get stars according to input coordinates
 	 * @param x1 - min x
 	 * @param y1 - min y
 	 * @param x2 - max x
 	 * @param y2 - max y
 	 * @return
 	 * @throws SQLException
 	 */
 	public List<Star> getStars(double x1, double y1, double x2, double y2) throws SQLException
 	{
 		if(x2 < x1 || y2 < y1)
 			throw new IllegalArgumentException("Condition (x2 < x1 || y2 < y1) is true");
 		//x1 < x && x < x2 && y1< y && y < y2
 		String filter = String.format("%1$s <= %2$s AND %2$s <= %3$s AND %4$s <= %5$s AND %5$s <= %6$s",x1,Structure.Stars.X,x2,y1,Structure.Stars.Y,y2);
 		return getStars(filter);
 	}
 	
 	/**
 	 * Get all stars 
 	 * @return
 	 * @throws SQLException
 	 */
 	public List<Star> getStars() throws SQLException
 	{
 		return getStars(null);
 	}
 	
 	/**
 	 * Add new Star
 	 * @param s
 	 * @throws SQLException
 	 */
 	public void addNewStar(Star s) throws SQLException
 	{
 		s.setId(System.nanoTime());		
 		String qry = String.format(Structure.Queries.INSERT_INTO_STAR_TEMPLATE, Structure.Tables.STARS,
 								   Structure.Stars.ID,Structure.Stars.NOTE, Structure.Stars.X,Structure.Stars.Y, Structure.Stars.TYPE,
 								   s.getId(), convertEmptyStringToNull(s.getNote(), true), s.getX(),s.getY(), convertEmptyStringToNull(s.getType(), true));
 		mStatement.executeUpdate(qry);
 	}
 	
 	/**
 	 * Update star's note and star's type
 	 * @param s
 	 * @throws SQLException
 	 */
 	public void updateStar(Star s) throws SQLException
 	{
 		if(s.getId() == 0)
 			throw new IllegalArgumentException("id can't be 0!");
 				
 		String qry = String.format(Structure.Queries.UPDATE_STARNOTE_TEMPLATE, Structure.Tables.STARS, 
 				Structure.Stars.NOTE, convertEmptyStringToNull(s.getNote(), true), 
 				Structure.Stars.TYPE, convertEmptyStringToNull(s.getType(), true),
 			    Structure.Stars.ID,s.getId());
 		mStatement.executeUpdate(qry);
 	}
 	
 	public void deleteStar(Star s) throws SQLException
 	{
 		if(s.getId() == 0)
 			throw new IllegalArgumentException("id can't be 0!");
 		String qry = String.format(Structure.Queries.DELETE_TEMPLATE_BY_ID, Structure.Tables.STARS, Structure.Stars.ID,s.getId());
 		mStatement.executeUpdate(qry);
 	}
 	
 	/**
 	 * Delete items and according Cons, Pros, Details
 	 * @param mi
 	 * @throws SQLException
 	 */
 	public void deleteMapItem(MapItem mi) throws SQLException
 	{
 		if(mi.getId() == 0)
 			throw new IllegalArgumentException("id can't be 0!");
 		String qry = String.format(Structure.Queries.DELETE_TEMPLATE_BY_ID, Structure.Tables.MAP_ITEMS, Structure.Stars.ID,mi.getId());
 		mStatement.executeUpdate(qry);
 		deleteCons(mi.getId());
 		deletePros(mi.getId());
 		deleteDetails(mi.getId());
 	}
 	
 	/**
 	 * getStars implementations
 	 * @param filter append to query
 	 * @return
 	 * @throws SQLException
 	 */
 	private List<Star> getStars(String filter) throws SQLException
 	{
 		List<Star> result = new ArrayList<Star>();
 		ResultSet rs;
 		if(filter == null)
 			rs = mStatement.executeQuery(Structure.Queries.SELECT_ALL_FROM_STARS);
 		else
 		{
 			String qry = String.format("%s WHERE %s",Structure.Queries.SELECT_ALL_FROM_STARS,filter);
 			rs = mStatement.executeQuery(qry);
 		}
 		
 		while(rs.next())
 		{
 			Star s = new Star();
 			s.setId(rs.getLong(Structure.Stars.ID));
 			s.setNote(rs.getString(Structure.Stars.NOTE));
 			s.setType(rs.getString(Structure.Stars.TYPE));
 			s.setX(rs.getDouble(Structure.Stars.X));
 			s.setY(rs.getDouble(Structure.Stars.Y));
 			result.add(s);
 		}
 		rs.close();
 		return result;
 	}
 	
 	/**
 	 * Get map items according to coordinates
 	 * (x2 < x1 || y2 < y1) => IllegalArgumentException
 	 * @param x1 min of x
 	 * @param y1 min of y
 	 * @param x2 max of x
 	 * @param y2 max of y
 	 * @param deep true to fill nested objects, otherwise {@link MapItem#details} {@link MapItem#pros} {@link MapItem#cons} will be null
 	 * @return loaded data
 	 * @throws SQLException
 	 */
 	public List<MapItem> get(double x1, double y1, double x2, double y2, boolean deep) throws SQLException
 	{
 		if(x2 < x1 || y2 < y1)
 			throw new IllegalArgumentException("Condition (x2 < x1 || y2 < y1) is true");
 		//x1 < x && x < x2 && y1< y && y < y2
 		String filter = String.format("%1$s <= %2$s AND %2$s <= %3$s AND %4$s <= %5$s AND %5$s <= %6$s",x1,Structure.MapItems.X,x2,y1,Structure.MapItems.Y,y2);
 		return getMapItems(filter, deep);
 	}
 	
 	/**
 	 * Returns all data from db
 	 * @param deep true to fill nested objects, otherwise {@link MapItem#details} {@link MapItem#pros} {@link MapItem#cons} will be null
 	 * @return
 	 * @throws SQLException
 	 */
 	public List<MapItem> get(boolean deep) throws SQLException
 	{
 		return getMapItems(null, deep);
 	}
 	
 	/**
 	 * SELECT implementation
 	 * @param filter - if null returns all data
 	 * @param deep - to load nested objects
 	 * @return
 	 * @throws SQLException
 	 */
 	public List<MapItem> getMapItems(String filter, boolean deep) throws SQLException
 	{
 		List<MapItem> result = new ArrayList<MapItem>();
 		HashMap<Long,MapItem> help = new HashMap<Long,MapItem>();
 		List<Long> ids = new ArrayList<Long>();
 		ResultSet rs;
 		if(filter == null)
 			rs = mStatement.executeQuery(Structure.Queries.SELECT_ALL_FROM_MAP_ITEMS);
 		else
 		{
 			String qry = String.format("%s WHERE %s",Structure.Queries.SELECT_ALL_FROM_MAP_ITEMS,filter);
 			rs = mStatement.executeQuery(qry);
 		}
 		while(rs.next())
 		{
 			MapItem mi = new MapItem();
 			mi.setId(rs.getLong(Structure.MapItems.ID));
 			mi.setAuthor(rs.getString(Structure.MapItems.AUTHOR));
 			mi.setCity(rs.getString(Structure.MapItems.CITY));			
 			mi.setCountry(rs.getString(Structure.MapItems.COUNTRY));
 			mi.setName(rs.getString(Structure.MapItems.NAME));
 			mi.setStreet(rs.getString(Structure.MapItems.STREET));
 			mi.setStreetViewLink(rs.getString(Structure.MapItems.STREETVIEWLINK));
 			mi.setContact(rs.getString(Structure.MapItems.CONTACT));
 			mi.setWeb(rs.getString(Structure.MapItems.WEBLINK));
 			mi.setX(rs.getDouble(Structure.MapItems.X));
 			mi.setY(rs.getDouble(Structure.MapItems.Y));
 			mi.setType(rs.getString(Structure.MapItems.TYPE));
 			mi.setRating(rs.getInt(Structure.MapItems.RATING));
 			ids.add(mi.getId());
 			help.put(mi.getId(), mi);
 			result.add(mi);
 		}
 		rs.close();
 		if(deep)
 		{
 			for(long l : ids)
 			{
 				fillPros(help.get(l));
 				fillCons(help.get(l));
 				fillDetails(help.get(l));
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Fill pros into param object
 	 * @param mi values will be filled into this object
 	 * @throws SQLException
 	 */
 	private void fillPros(MapItem mi) throws SQLException
 	{
 		String filter = String.format(" WHERE %s = %s ORDER BY %s",Structure.Pros.MAPITEM_ID,mi.getId(),Structure.Pros.VALUE);
 		String qry = Structure.Queries.SELECT_ALL_PROS + filter;
 		ResultSet rs = mStatement.executeQuery(qry);
 		List<String> result = new ArrayList<String>();
 		while(rs.next())
 		{
 			result.add(rs.getString(Structure.Pros.VALUE));
 		}
 		rs.close();
 		mi.setPros(result);
 		rs.close();
 	}
 	
 	/**
 	 * same as {@link #fillPros(MapItem)}
 	 * @param mi
 	 * @throws SQLException
 	 */
 	private void fillCons(MapItem mi) throws SQLException
 	{
 		String filter = String.format(" WHERE %s = %s ORDER BY %s",Structure.Cons.MAPITEM_ID,mi.getId(),Structure.Cons.VALUE);
 		String qry = Structure.Queries.SELECT_ALL_CONS + filter;
 		ResultSet rs = mStatement.executeQuery(qry);
 		List<String> result = new ArrayList<String>();
 		while(rs.next())
 		{
 			result.add(rs.getString(Structure.Pros.VALUE));
 		}
 		rs.close();
 		mi.setCons(result);
 		rs.close();
 	}
 	
 	/**
 	 * Same as {@link #fillPros(MapItem)}
 	 * @param mi
 	 * @throws SQLException
 	 */
 	private void fillDetails(MapItem mi) throws SQLException
 	{
 		String filter = String.format(" WHERE %s = %s ORDER BY [%s] DESC",Structure.Details.MAPITEM_ID,mi.getId(),Structure.Details.WHEN);
 		String qry = Structure.Queries.SELECT_ALL_DETAILS + filter;
 		ResultSet rs = mStatement.executeQuery(qry);
 		List<Detail> result = new ArrayList<Detail>();
 		while(rs.next())
 		{
 			Detail d = new Detail();
 			d.setId(rs.getLong(Structure.Details.ID));
 			d.setWhat(rs.getString(Structure.Details.WHAT));
 			d.setDetail(rs.getString(Structure.Details.VALUE));
 			d.setWhen(new Date(rs.getLong(Structure.Details.WHEN)));
 			result.add(d);
 		}
 		rs.close();
 		mi.setDetails(result);
 		rs.close();
 	}
 	
 	/**
 	 * Writes new MapItem into DB, write is deep => all nested objects are saved to
 	 * @param mi
 	 * @throws SQLException
 	 */
 	public void addNewMapItem(MapItem mi) throws SQLException
 	{		
 		mi.setId(System.nanoTime());
 		String qry = genInsertQuery(mi);
 		int i = mStatement.executeUpdate(qry);
 		addNewPros(mi.getId(), mi.getPros());
 		addNewCons(mi.getId(), mi.getCons());
 		addNewDetails(mi.getId(), mi.getDetails());		
 	}
 	
 	public void updateMapItem(MapItem mi) throws SQLException
 	{
 		String qry = genUpdateQuery(mi);
 		int i = mStatement.executeUpdate(qry);
 		
 		deletePros(mi.getId());
 		addNewPros(mi.getId(), mi.getPros());
 		deleteCons(mi.getId());
 		addNewCons(mi.getId(), mi.getCons());
 		deleteDetails(mi.getId());
 		addNewDetails(mi.getId(), mi.getDetails());
 	}
 	
 	/**
 	 * Implementation of saving pros
 	 * @param parentId
 	 * @param pros
 	 * @throws SQLException
 	 */
 	private void addNewPros(long parentId, List<String> pros) throws SQLException
 	{
 		if(pros == null || pros.size() == 0)
 			return;
 		if(parentId == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		boolean execute = false;
 		for(String s :pros)
 		{
 			if(s != null && s.trim().length() > 0)
 			{
 				String v = s.trim().replace("'", "''");
 				execute = true;
 				String qry = String.format("INSERT INTO [%s] ([%s],[%s]) VALUES (%s,'%s');",Structure.Tables.PROS,Structure.Pros.MAPITEM_ID,Structure.Pros.VALUE,parentId,v);
 				mStatement.addBatch(qry);
 			}
 		}
 		if(execute)
 			mStatement.executeBatch();
 	}
 	
 	
 	
 	/**
 	 * Delete all prose according to parentId
 	 * @param parentId
 	 * @throws SQLException 
 	 */
 	private void deletePros(long parentId) throws SQLException
 	{
 		if(parentId == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		String qry = Structure.Queries.DELETE_PROS_BY_MAPITEM_ID + parentId;
 		mStatement.executeUpdate(qry);
 		mStatement.close();
 	}
 	
 	/**
 	 * same as {@link #deletePros(int)}
 	 * @param parentId
 	 * @throws SQLException
 	 */
 	private void deleteCons(long parentId) throws SQLException
 	{
 		if(parentId == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		String qry = Structure.Queries.DELETE_CONS_BY_MAPITEM_ID + parentId;
 		mStatement.executeUpdate(qry);
 	}
 	
 	/**
 	 * same as {@link #deletePros(int)}
 	 * @param parentId
 	 * @throws SQLException
 	 */
 	private void deleteDetails(long parentId) throws SQLException
 	{
 		if(parentId == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		String qry = Structure.Queries.DELETE_DETAILS_BY_MAPITEM_ID + parentId;
 		mStatement.executeUpdate(qry);
 	}
 		
 	public void deleteDetail(long detailId) throws SQLException
 	{
 		if(detailId == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		String qry = Structure.Queries.DELETE_DETAIL_BY_ID + detailId;
 		mStatement.executeUpdate(qry);
 	}
 	
 	private void addNewCons(long parentId, List<String> cons) throws SQLException
 	{
 		if(cons == null || cons.size() == 0)
 			return;
 		if(parentId == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		boolean execute = false;
 		for(String s :cons)
 		{
 			if(s != null && s.trim().length() > 0)
 			{	
 				String v = s.trim().replace("'", "''");
 				execute = true;
 				String qry = String.format("INSERT INTO [%s] ([%s],[%s]) VALUES (%s,'%s');",Structure.Tables.CONS,Structure.Cons.MAPITEM_ID, Structure.Cons.VALUE,parentId,v);
 				mStatement.addBatch(qry);
 			}
 		}
 		if(execute)
 			mStatement.executeBatch();
 	}
 	
 	/**
 	 * Add single single detail to db 
 	 * @param parentId
 	 * @param d
 	 * @throws SQLException
 	 */
 	public void addNewDetail(long parentId, Detail d) throws SQLException
 	{
 		addNewDetails(parentId, Arrays.asList(new Detail[]{d}));
 	}
 	
 	/**
 	 * Updates single detail
 	 * @param d
 	 * @throws SQLException 
 	 */
 	public void updateDetail(Detail d) throws SQLException
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append(String.format("UPDATE %s SET ",Structure.Tables.DETAILS));
 		sb.append(String.format("[%s] = ",Structure.Details.VALUE));
 		sb.append(String.format("%s,",convertEmptyStringToNull(d.getDetail(),true)));
 		sb.append(String.format("[%s] = ",Structure.Details.WHAT));
 		sb.append(String.format("%s,",convertEmptyStringToNull(d.getWhat(),true)));
 		sb.append(String.format("[%s] = ",Structure.Details.WHEN));
 		sb.append(String.format("%s",d.getTime().getTime()));
 		sb.append(String.format(" WHERE %s = %s",Structure.Details.ID, d.getId()));
 		String qry = sb.toString();
 		mStatement.executeUpdate(qry);
 	}
 	
 	/**
 	 * Deletes single detail
 	 * @param d
 	 * @throws SQLException
 	 */
 	public void deleteDetail(Detail d) throws SQLException
 	{
 		String qry = String.format(Structure.Queries.DELETE_TEMPLATE_BY_ID,Structure.Tables.DETAILS,Structure.Details.ID,d.getId());
 		mStatement.executeUpdate(qry);
 	}
 	
 	/**
 	 * Same as {@link #addNewPros(int, List)}
 	 * null what or detail is ignored
 	 * @param parentId
 	 * @param data
 	 * @throws SQLException
 	 */
 	private void addNewDetails(long parentId, List<Detail> data) throws SQLException
 	{
 		if(data == null || data.size() == 0)
 			return;
 		if(parentId == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		boolean execute = false;
 		for(Detail d : data)
 		{
 			if(d == null)
 				continue;
 			d.setId(System.nanoTime());
 			String what = convertEmptyStringToNull(d.getWhat(), true);
 			String detail = convertEmptyStringToNull(d.getDetail(), true);
 			long time = (d.getTime() == null) ? System.currentTimeMillis() : d.getTime().getTime();
 			if(what != null || detail != null)
 			{				
 				StringBuilder sb1 = new StringBuilder();
 				StringBuilder sb2 = new StringBuilder();
 				sb1.append(String.format("INSERT INTO [%s] (",Structure.Tables.DETAILS));
 				sb2.append("VALUES (");
 				sb1.append(String.format("[%s],",Structure.Details.ID));
 				sb2.append(String.format("%s,",d.getId()));
 				
 				sb1.append(String.format("[%s],",Structure.Details.MAPITEM_ID));
 				sb2.append(String.format("%s,",parentId));
 				if(what != null)
 				{
 					execute = true;
 					sb1.append(String.format("[%s],",Structure.Details.WHAT));
 					sb2.append(String.format("%s,",what));
 				}
 				if(detail != null)
 				{
 					execute = true;
 					sb1.append(String.format("[%s],",Structure.Details.VALUE));
 					sb2.append(String.format("%s,",detail));
 				}
 				
 				sb1.append(String.format("[%s]",Structure.Details.WHEN));
 				sb2.append(String.format("%s",time));
 				if(execute)
 				{
 					sb1.append(") ");
 					sb2.append(");");
 					String qry = sb1.toString() + sb2.toString();
 					mStatement.addBatch(qry);
 				}
 			}
 		}
 		
 		if(execute)
 			mStatement.executeBatch();
 	}
 	
 	/**
 	 * Convert empty (trimmed) string into null
 	 * @param value
 	 * @return null if value is null, or value can be trimmed into length 0
 	 */
 	public String convertEmptyStringToNull(String value)
 	{
 		return convertEmptyStringToNull(value, false);
 	}
 	
 	public String convertEmptyStringToNull(String value, boolean addQuotMarks)
 	{
 		if(value == null || value.trim().length() == 0)
 			return null;
 		else
 		{
 			String result = null;
 			if(addQuotMarks)
				result = "'" + value.trim() + "'";
			else 
				result = value.trim().replace("'","''");
			
			result = result.replace("'", "''").replace("\"", "\"\"");
 			return result;
 		}
 	}
 	
 	/**
 	 * Generates insert query
 	 * Only not null values are handled
 	 * @param mi
 	 * @return
 	 */
 	private String genInsertQuery(MapItem mi)
 	{
 		StringBuilder sb = new StringBuilder();
 		StringBuilder values = new StringBuilder();
 		sb.append(String.format("INSERT INTO %s (",Structure.Tables.MAP_ITEMS));		
 		if(mi.getAuthor() != null && mi.getAuthor().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.AUTHOR));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getAuthor(),true)));
 		}
 		if(mi.getCity() != null && mi.getCity().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.CITY));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getCity(),true)));
 		}
 		if(mi.getContact() != null && mi.getContact().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.CONTACT));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getContact(),true)));
 		}
 		if(mi.getName() != null && mi.getName().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.NAME));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getName(),true)));
 		}
 		if(mi.getStreet() != null && mi.getStreet().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.STREET));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getStreet(),true)));
 		}
 		if(mi.getCountry() != null && mi.getCountry().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.COUNTRY));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getCountry(),true)));
 		}
 		if(mi.getStreetViewLink() != null && mi.getStreetViewLink().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.STREETVIEWLINK));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getStreetViewLink(),true)));
 		}
 		if(mi.getType() != null && mi.getType().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.TYPE));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getType(),true)));
 		}
 		
 		if(mi.getWeb() != null && mi.getWeb().length() > 0)
 		{
 			sb.append(String.format("%s,",Structure.MapItems.WEBLINK));
 			values.append(String.format("%s,",convertEmptyStringToNull(mi.getWeb(),true)));
 		}
 		if(mi.getX() !=  0 && mi.getX() != 0)
 		{
 			sb.append(String.format("%s,%s,",Structure.MapItems.X,Structure.MapItems.Y));
 			values.append(String.format("%s,%s,",mi.getX(), mi.getY()));
 		}
 		
 		sb.append(String.format("%s, ",Structure.MapItems.RATING));
 		values.append(String.format("%s, ",mi.getRating()));
 		
 		if(values.length() == 0)
 			throw new IllegalArgumentException("No values generated, because everythink to save is null or empty?!");
 		
 		sb.append(String.format("%s",Structure.MapItems.ID));
 		values.append(String.format("%s);",mi.getId()));
 		
 		values.insert(0," VALUES (");
 		sb.append(") ");		
 		
 		String result = sb.toString() + values.toString(); 
 		System.out.println(result);
 		return result;
 	}
 	
 	
 	/**
 	 * Generates update query
 	 * @param mi
 	 * @return
 	 */
 	private String genUpdateQuery(MapItem mi)
 	{
 		if(mi.getId() == 0)
 			throw new IllegalArgumentException("ParentId can't be 0!");
 		StringBuilder sb = new StringBuilder();
 		sb.append(String.format("UPDATE %s SET ",Structure.Tables.MAP_ITEMS));
 		sb.append(String.format("%s = ",Structure.MapItems.AUTHOR));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getAuthor(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.CITY));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getCity(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.CONTACT));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getContact(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.COUNTRY));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getCountry(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.NAME));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getName(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.STREET));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getStreet(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.STREETVIEWLINK));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getStreetViewLink(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.TYPE));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getType(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.WEBLINK));
 		sb.append(String.format("%s, ",convertEmptyStringToNull(mi.getWeb(),true)));
 		sb.append(String.format("%s = ",Structure.MapItems.X));	
 		sb.append(String.format("%s, ",mi.getX()));
 		sb.append(String.format("%s = ",Structure.MapItems.Y));
 		sb.append(String.format("%s, ",mi.getY()));
 		sb.append(String.format("%s = ",Structure.MapItems.RATING));
 		sb.append(String.format("%s",mi.getRating()));
 		
 		sb.append(String.format(" WHERE %s = %s",Structure.MapItems.ID,mi.getId()));
 		String result = sb.toString(); 
 		System.out.println(result);
 		return result;
 	}
 	
 	/**
 	 * Close statement and connection
 	 * Do it every time if you know, you are not going to use it later
 	 * @throws SQLException 
 	 */
 	public void close() throws SQLException
 	{
 		mStatement.close();
 		mConnection.close();
 	}
 	
 	public ResultSet executeQuery(String query) throws SQLException
 	{
 		return mStatement.executeQuery(query);
 	}
 	
 	/**
 	 * Returns ordered map item types
 	 * @return
 	 * @throws SQLException
 	 */
 	public List<String> getMapItemTypes() throws SQLException
 	{
 		ResultSet rs = mStatement.executeQuery(Structure.Queries.SELECT_ALL_FROM_MAPITEMTYPES);
 		List<String> result = new ArrayList<String>();
 		while(rs.next())
 			result.add(rs.getString(Structure.MapItemTypes.TYPE));
 		rs.close();		
 		return result;
 	}
 	
 	/**
 	 * Adds new item type
 	 * @param value
 	 * @throws SQLException
 	 */
 	public void addNewMapItemItem(String value) throws SQLException
 	{
 		value = convertEmptyStringToNull(value);
 		if(value == null)
 			throw new IllegalArgumentException("Value must be not empty string!");
 		
 		//check if record isn't alrady in table
 		String countColumn = "Cnt";
 		String qry = String.format("SELECT COUNT(*) as %s FROM %s WHERE %s = '%s'",countColumn,Structure.Tables.MAPITEM_TYPES,
 													Structure.MapItemTypes.TYPE,value);
 		ResultSet rs = mStatement.executeQuery(qry);
 		if(rs.next() && rs.getInt(countColumn) == 0)
 		{	
 			rs.close();
 			qry = String.format(Structure.Queries.INSERT_INTO_MAPITEMTYPES_TEMPLATE,Structure.Tables.MAPITEM_TYPES,
 										Structure.MapItemTypes.ID,Structure.MapItemTypes.TYPE,
 										System.nanoTime(),value);
 			mStatement.execute(qry);
 		}
 	}
 	
 	public void deleteMapItemType(String value) throws SQLException
 	{
 		value = convertEmptyStringToNull(value, true);
 		if(value == null)
 			throw new IllegalArgumentException("Value must be not empty string!");
 		String qry = Structure.Queries.DELETE_MAPITEM_TYPE + value;
 		mStatement.execute(qry);
 	}
 	
 	/**
 	 * Just class for easier database words work
 	 * @author Joe Scurab
 	 *
 	 */
 	public static final class Structure
 	{
 		public static class Tables 
 		{
 			public static final String MAP_ITEMS = "MapItems";
 			public static final String PROS = "Pros";
 			public static final String CONS = "Cons";
 			public static final String DETAILS = "Details";
 			public static final String STARS = "Stars";
 			public static final String MAPITEM_TYPES = "MapItemTypes";
 			public static final String[] ALL_TABLES = new String[] {DETAILS, CONS,PROS,MAP_ITEMS, STARS, MAPITEM_TYPES};
 		}
 		
 		public static class Queries
 		{
 			public static final String CREATE_TABLE_MAP_ITEM = "CREATE VIRTUAL TABLE MapItems USING fts3(ID NUMERIC PRIMARY KEY NOT NULL, Author TEXT NOT NULL, City TEXT NOT NULL, Country TEXT NOT NULL, Name TEXT NOT NULL, Street TEXT NOT NULL, Contact TEXT, WebLink TEXT, StreetViewLink TEXT, X FLOAT NOT NULL, Y FLOAT NOT NULL, Type TEXT, Rating NUMERIC NOT NULL)";
 			public static final String CREATE_TABLE_PROS = "CREATE VIRTUAL TABLE Pros USING fts3(MapItem_FK NUMERIC NOT NULL, Value TEXT NOT NULL)";
 			public static final String CREATE_TABLE_CONS = "CREATE VIRTUAL TABLE Cons USING fts3(MapItem_FK NUMERIC NOT NULL, Value TEXT NOT NULL)";			
 			public static final String CREATE_TABLE_DETAILS = "CREATE VIRTUAL TABLE Details USING fts3(ID NUMERIC PRIMARY KEY NOT NULL, MapItem_FK NUMERIC NOT NULL, What TEXT, Value TEXT NOT NULL, [When] DATETIME NOT NULL)";
 			public static final String CREATE_TABLE_STARS = "CREATE VIRTUAL TABLE Stars USING fts3(ID NUMERIC PRIMARY KEY NOT NULL, Note TEXT, StarType TEXT NOT NULL, When NUMERIC NOT NULL, X FLOAT NOT NULL, Y FLOAT NOT NULL)";
 			public static final String CREATE_TABLE_MAPITEMTYPE = "CREATE VIRTUAL TABLE MapItemTypes USING fts3(ID NUMERIC PRIMARY KEY NOT NULL, MapItemType UNIQUE TEXT NOT NULL)";
 			public static final String[] ALL_CREATE_TABLE_QUERIES = new String[] {CREATE_TABLE_MAP_ITEM,CREATE_TABLE_PROS,CREATE_TABLE_CONS,CREATE_TABLE_DETAILS,CREATE_TABLE_STARS,CREATE_TABLE_MAPITEMTYPE};
 			public static final String DELETE_DATA_FROM_TABLE_TEMPLATE = "DELETE FROM %s";
 			public static final String DROP_TABLE_TEMPLATE = "DROP TABLE %s";
 			public static final String SELECT_ALL_FROM_MAP_ITEMS = String.format("SELECT * FROM %s",Tables.MAP_ITEMS);
 			public static final String SELECT_ALL_PROS = String.format("SELECT * FROM %s",Tables.PROS);
 			public static final String SELECT_ALL_CONS = String.format("SELECT * FROM %s",Tables.CONS);
 			public static final String SELECT_ALL_DETAILS = String.format("SELECT * FROM %s",Tables.DETAILS);
 			public static final String SELECT_ALL_FROM_STARS = String.format("SELECT * FROM %s",Tables.STARS);
 			public static final String SELECT_ALL_FROM_MAPITEMTYPES = String.format("SELECT * FROM %s ORDER BY %s",Tables.MAPITEM_TYPES,MapItemTypes.TYPE);
 			public static final String DELETE_PROS_BY_MAPITEM_ID = String.format("DELETE FROM %s WHERE %s = ",Tables.PROS, Structure.Pros.MAPITEM_ID);
 			public static final String DELETE_CONS_BY_MAPITEM_ID = String.format("DELETE FROM %s WHERE %s = ",Tables.CONS, Structure.Cons.MAPITEM_ID);
 			public static final String DELETE_DETAILS_BY_MAPITEM_ID = String.format("DELETE FROM %s WHERE %s = ",Tables.DETAILS, Structure.Details.MAPITEM_ID);
 			public static final String DELETE_DETAIL_BY_ID = String.format("DELETE FROM %s WHERE %s = ",Tables.DETAILS, Structure.Details.ID);
 			public static final String DELETE_MAPITEM_TYPE = String.format("DELETE FROM %s WHERE %s = ",Tables.MAPITEM_TYPES, Structure.MapItemTypes.TYPE);
 			public static final String SELECT_TABLES_COUNT_FROM_SQLITE_MASTER = "SELECT Count(*) as Cnt FROM sqlite_master WHERE type = 'table'";
 			public static final String SELECT_ALL_FROM_SQLITE_MASTER = "SELECT * FROM sqlite_master";
 			public static final String INSERT_INTO_STAR_TEMPLATE = "INSERT INTO [%s] (%s, %s, %s, %s, %s) VALUES (%s, %s, %s, %s, %s);";
 			public static final String UPDATE_STARNOTE_TEMPLATE = "UPDATE [%s] SET [%s] = %s, [%s] = %s WHERE %s = %s";
 			public static final String DELETE_TEMPLATE_BY_ID = "DELETE FROM [%s] WHERE %s = %s";
 			public static final String INSERT_INTO_MAPITEMTYPES_TEMPLATE = "INSERT INTO [%s] ([%s],[%s]) VALUES (%s,'%s');";
 		}
 		
 		public static class MapItems
 		{
 			public static final String ID = "ID";
 			public static final String AUTHOR = "Author";
 			public static final String CITY = "City";
 			public static final String NAME = "Name";
 			public static final String STREET = "Street";
 			public static final String COUNTRY = "Country";
 			public static final String WEBLINK = "WebLink";
 			public static final String STREETVIEWLINK = "StreetViewLink";
 			public static final String CONTACT = "Contact";
 			public static final String X = "X";
 			public static final String Y = "Y";
 			public static final String TYPE = "TYPE";
 			public static final String RATING = "Rating";
 		}
 		
 		public static class Pros
 		{
 			public static final String MAPITEM_ID = "MapItem_FK";
 			public static final String VALUE = "Value";
 		}
 		
 		public static class Cons
 		{
 			public static final String MAPITEM_ID = "MapItem_FK";
 			public static final String VALUE = "Value";
 		}
 		
 		public static class Details
 		{
 			public static final String ID = "ID";
 			public static final String MAPITEM_ID = "MapItem_FK";
 			public static final String WHAT = "What";
 			public static final String VALUE = "Value";
 			public static final String WHEN = "When";
 		}
 		
 		public static class Stars
 		{
 			public static final String ID = "ID";
 			public static final String NOTE = "Note";
 			public static final String WHEN = "When";
 			public static final String TYPE = "StarType";
 			public static final String X = "X";
 			public static final String Y = "Y";
 		}
 		
 		public static class MapItemTypes
 		{
 			public static final String ID = "ID";
 			public static final String TYPE = "MapItemType";
 		}
 	}
 }
