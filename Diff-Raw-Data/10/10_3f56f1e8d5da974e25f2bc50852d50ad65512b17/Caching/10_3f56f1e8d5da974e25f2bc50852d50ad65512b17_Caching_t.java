 package com.NFCGeo;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public abstract class Caching {
 	
 	public Caching(){
 		
 	}
 	
 	public static void Add(Cache c) throws SQLException{
 		String temp = new String("");
 		
 		temp += ("INSERT INTO caches(name,creator,loc_lat,loc_long) values(\'");
 		temp += (c.getName() + "\',\'");
 		temp += (c.getCreator() + "\',");
 		temp += (c.getLoc_lat() + ",");
 		temp += (c.getLoc_long() + ");");
 		
 		MainMenu.dbHandle.addDB(temp);
 	}
 	
 	public static Cache[] GetName(String name) throws SQLException{
 		String temp = new String("");
 		
 		temp += ("SELECT * FROM caches WHERE name=\'");
 		temp += (name + "\'");
 		
 		ResultSet r = MainMenu.dbHandle.queryTable(temp);
 		
 		int size = 0;
 		while(r.next()){
 			size++;
 		}
 		
 		r.first();
 		
 		Cache[] c = new Cache[size];
 		int i;
 		
 		for(i = 0; i < size; i++){
 			c[i] = new Cache();
 			c[i].setName(r.getString("name"));
 			c[i].setCreator(r.getString("creator"));
 			c[i].setLoc_lat(r.getInt("loc_lat"));
 			c[i].setLoc_long(r.getInt("loc_long"));
 			r.next();
 		}
 		
 		return c;
 	}
 	
 	public static Cache[] GetCreator(String creator) throws SQLException{
 		String temp = new String("");
 		
 		temp += ("SELECT * FROM caches WHERE creator=\'");
 		temp += (creator + "\'");
 		
 		ResultSet r = MainMenu.dbHandle.queryTable(temp);
 		
 		int size = 0;
 		while(r.next()){
 			size++;
 		}
 		
 		r.first();
 		
 		Cache[] c = new Cache[size];
 		int i;
 		
 		for(i = 0; i < size; i++){
 			c[i] = new Cache();
 			c[i].setName(r.getString("name"));
 			c[i].setCreator(r.getString("creator"));
 			c[i].setLoc_lat(r.getInt("loc_lat"));
 			c[i].setLoc_long(r.getInt("loc_long"));
 			r.next();
 		}
 		
 		return c;
 	}
 	
 	public static Cache[] GetLocation(int pos_lat, int pos_long, int offset) throws SQLException{
 		String temp = new String("");
 		
 		temp += ("SELECT * FROM caches WHERE loc_lat BETWEEN ");
 		temp += Integer.toString(pos_lat-offset);
 		temp += (" AND ");
 		temp += Integer.toString(pos_lat+offset);
 		temp += (" AND loc_long BETWEEN ");
 		temp += Integer.toString(pos_long-offset);
 		temp += (" AND ");
 		temp += Integer.toString(pos_long+offset);
 		try
 		{
 			ResultSet r = MainMenu.dbHandle.queryTable(temp);
 			
 			int size = 0;
 			while(r.next()){
 				size++;
 			}
 			
 			r.first();
 			
 			Cache[] c = new Cache[size];
 			int i;
 			
 			for(i = 0; i < size; i++){
 				c[i] = new Cache();
				c[i].setId(r.getString("id"));
 				c[i].setName(r.getString("name"));
 				c[i].setCreator(r.getString("creator"));
 				c[i].setLoc_lat(r.getInt("loc_lat"));
 				c[i].setLoc_long(r.getInt("loc_long"));
 				r.next();
 			}
 			return c;
 		} catch(Exception e)
 		{
 			return null;
 		}
 	}
 	
 	public static Cache GetID(int id) throws SQLException{
 		String temp = new String("");
 		
 		temp += ("SELECT * FROM caches WHERE id=");
 		temp += (Integer.toString(id));
 		
 		ResultSet r = MainMenu.dbHandle.queryTable(temp);
 		
 		
 		Cache c = new Cache();
 		r.next();
 		
 		c.setName(r.getString("name"));
 		c.setCreator(r.getString("creator"));
 		c.setLoc_lat(r.getInt("loc_lat"));
 		c.setLoc_long(r.getInt("loc_long"));	
 		
 		return c;
 	
 	}
 }
