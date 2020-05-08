 package pro.trousev.cleer.desktop;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import pro.trousev.cleer.Database;
 
 public class DatabaseSqlite implements Database {
 
 	Connection link = null;
 	DatabaseSqlite(String path) throws SQLException, ClassNotFoundException
 	{
		//Class.forName("org.hsqldb.jdbcDriver");
 		Class.forName("org.sqlite.JDBC");
 		//link = DriverManager.getConnection(String.format("jdbc:hsqldb:file:%s;hsqldb.default_table_type=cached;hsqldb.script_format=3",path),"sa","");
 
 		//link = DriverManager.getConnection(String.format("jdbc:hsqldb:file:%s",path),"sa","");
 		link = DriverManager.getConnection(String.format("jdbc:sqlite:%s",path));
 
 	}
 	@Override
 	public void close() 
 	{
 		/*try {
 			link.prepareStatement("SHUTDOWN").execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}*/
 		try {
 			link.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	public void finalize() throws Throwable
 	{
 		close();
 		super.finalize();
 	}
 	public class DatabaseObjectHsql implements Database.DatabaseObject
 	{
 		String _id = null;
 		String _contents = null;
 		DatabaseSqlite _parent = null;
 		String _section= null;
 		String _search = null;
 		DatabaseObjectHsql(String section, DatabaseSqlite parent, String id, String contents)
 		{
 			_parent = parent;
 			_id = id;
 			_contents = contents;
 			_section = section;
 		}
 		DatabaseObjectHsql(DatabaseSqlite parent, String id)
 		{
 			_parent = parent;
 			_id = id;
 		}
 		@Override
 		public String id() {
 			return _id;
 		}
 
 		@Override
 		public String contents() {
 			if(_contents == null)
 			{
 				try {
 					ResultSet set = _parent.link.prepareStatement(String.format("select value from %s where id='%s';",_section,_id)).executeQuery();
 					set.next();
 					_contents = set.getString(1);
 					set.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 			return _contents;
 		}
 		@Override
 		public String toString()
 		{
 			return id()+": "+contents();
 		}
 		@Override
 		public boolean update(String contents, String search) {
 			if(contents != null) contents = contents.replace("'", "''");
 			if(search != null) search = search.replace("'", "''");
 			if(search != null) search = search.toLowerCase();
 			String query = null;
 			if(contents == null)
 				query=String.format("update %s set search='%s' where id='%s'", _section, search,_id);
 			else if (search == null)
 				query=String.format("update %s set value='%s'  where id='%s'",_section,contents,_id);
 			else 
 				query=String.format("update %s set value='%s' , search='%s' where id='%s'",_section,contents,  search,_id);
 			try {
 				if(_parent.link.prepareStatement(query).execute())
 				{
 					if(contents != null)
 						_contents = null;
 				}
 				return false;
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return false;
 			}
 			
 		}
 		@Override
 		public boolean update_contents(String contents) {
 			return update(contents, null);
 		}
 		@Override
 		public boolean update_search(String search) {
 			return update(null,search);
 		}
 		@Override
 		public String search() {
 			if(_search == null)
 			{
 				try {
 					ResultSet set = _parent.link.prepareStatement(String.format("select search from %s where id='%s';",_section,_id)).executeQuery();
 					set.next();
 					_search = set.getString(1);
 					set.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 			return _search;
 
 		}
 	}
 	@Override
 	public DatabaseObject store(String section, String contents, String keywords) {
 		keywords = keywords.toLowerCase();
 		keywords = keywords.replace("'","''");
 		contents = contents.replace("'","''");
 		try {
 			link.prepareStatement(String.format("INSERT INTO %s(value,search) VALUES('%s','%s');",section,contents, keywords)).execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return null;
 	}
 
 	@Override
 	public List<DatabaseObject> search(String section, String query) {
 		String[] keywords = query.split(" ");
 		String where = null;
 		List<DatabaseObject> ans = new ArrayList<Database.DatabaseObject>();
 		for(String word : keywords)
 		{
 			word = word.replace("'", "''");
 			word = word.toLowerCase();
 			String clause = String.format("search LIKE '%%%s%%'",word);
 			if(where == null)
 				where = clause;
 			else where += " AND "+clause;
 		}
 		try {
 			String qq; 
 			if(where == null)
 				qq = String.format("SELECT id, value FROM %s;",section);
 			else
 				qq = String.format("SELECT id, value FROM %s WHERE %s;",section,where);
 			ResultSet set = link.prepareStatement(qq).executeQuery();
 			while(set.next())
 			{
 				ans.add(new DatabaseObjectHsql(section,this, set.getString(1),set.getString(2)));
 			}
 			set.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return ans;
 	}
 
 	@Override
 	public boolean remove(String section, DatabaseObject object) {
 		try {
 			return link.prepareStatement(String.format("DELETE FROM %s WHERE id='%s';", section,object.id())).execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	@Override
 	public boolean declare_section(String section) {
 	    try
 	    {
 	    	//This is for HSQLDB
 	    	//return  link.prepareStatement(String.format("CREATE TABLE %s (id IDENTITY , value VARCHAR(10000), search VARCHAR(10000))", section)).execute();
 	    	//This is for SQLite
 	    	link.prepareStatement(String.format("CREATE TABLE %s (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, value VARCHAR(10000), search VARCHAR(10000))", section)).execute();
 	    	link.prepareStatement(String.format("CREATE INDEX IF NOT EXISTS idx_search_%s ON %s (search)", section,section)).execute();
 	    	return true;
 	    }
 	    catch (Exception e)
 	    {
 	    	return false;
 	    }
	    
 	}
 	@Override
 	public boolean clear_section(String section) {
 	    try
 	    {
 	    	return  link.prepareStatement(
 	    		String.format("DELETE FROM %s ;", section)
                 )
                 .execute();
 	    }
 	    catch (Exception e)
 	    {
 	    	return false;
 	    }
 	}
 	@Override
 	public boolean begin() {
 		try {
 			return link.prepareStatement("BEGIN TRANSACTION").execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	@Override
 	public boolean commit() {
 		try {
 			return link.prepareStatement("COMMIT TRANSACTION").execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	@Override
 	public boolean rollback() {
 		try {
 			return link.prepareStatement("ROLLBACK TRANSACTION").execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 }
