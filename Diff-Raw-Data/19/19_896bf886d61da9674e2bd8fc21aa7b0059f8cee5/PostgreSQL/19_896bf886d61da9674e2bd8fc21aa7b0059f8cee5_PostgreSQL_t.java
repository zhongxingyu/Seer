 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.db.postgres;
 
 import java.sql.*;
 
 import om.tnavigator.db.OmQueries;
 import om.tnavigator.db.DatabaseAccess.Transaction;
 
 /**
  * Specialisation of OmQueries for PostgreSQL.
  */
 public class PostgreSQL extends OmQueries
 {
 	/**
 	 * @param prefix the database prefix to use.
 	 */
 	public PostgreSQL(String prefix) {
 		super(prefix);
 	}
 
 	public void checkDatabaseConnection(Transaction dat) throws SQLException
 	{
 		dat.query("SELECT version()");
 	}
 
 	public int getInsertedSequenceID(Transaction dat,String table,String column) throws SQLException
 	{
		ResultSet rs=dat.query("SELECT currval('"+getPrefix()+table+"_"+column+"_seq')");
 		rs.next();
 		return rs.getInt(1);
 	}
 
 	public String getURL(String server,String database,String username,String password) throws ClassNotFoundException
 	{
 		Class.forName("org.postgresql.Driver");
 		return "jdbc:postgresql://"+server+"/"+database+"?user="+username+"&password="+password;
 	}
 
 	protected String getCurrentDateFunction()
 	{
 		return "CURRENT_TIMESTAMP";
 	}
 	
 	
 }
