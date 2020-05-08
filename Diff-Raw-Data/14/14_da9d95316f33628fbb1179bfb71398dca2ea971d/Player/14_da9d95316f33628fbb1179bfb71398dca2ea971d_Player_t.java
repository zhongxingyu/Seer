 
 package dbx;
 
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 
 public class Player implements DBObj
 {
     private long id;
     private String name;
     private int health;
     private int skill_level;
     private int birth_date;
     private int pos_x;
     private int pos_y;
     private int region_id;
     
     /**
      * Database access operation object.
      */
     private DBX dbx;
     
     /**
      * Database iteration object.
      */
     private DBXIter dbxIter;
     
 
     public long getId()
     {
         return id;
     }
 
     public void setId(long id)
     {
         this.id = id;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
    public int getHealth()
     {
         return health;
     }
     
     public void setHealth(int health)
     {
         this.health = health;
     }
     
    public int getSkillLevel()
     {
         return skill_level;
     }
     
     public void setSkillLevel(int skill_level)
     {
         this.skill_level = skill_level;
     }
 
    public int getBirthDate()
     {
         return birth_date;
     }
 
     public void setBirthDate(int birth_date)
     {
         this.birth_date = birth_date;
     }
     
     public int getPosX()
     {
         return pos_x;
     }
     
     public void setPosX(int pos_x)
     {
         this.pos_x = pos_x;
     }
     
     public int getPosY()
     {
         return pos_y;
     }
     
     public void setPosY(int pos_y)
     {
         this.pos_y = pos_y;
     }
     
     public int getRegionID()
     {
         return region_id;
     }
     
     public void setRegionID(int region_id)
     {
         this.region_id = region_id;
     }
     
     /**
      * Set the object used for database access operations.
      * @param dbx_p Database access operation object.
      */
     @Override
     public void setDbx(DBX dbx_p)
     {
         dbx = dbx_p;
     }
         
     /**
      * Get the object used for database access operations.
      * @return Database access operation object.
      */
     @Override
     public DBX getDbx()
     {
         return dbx;
     }
 
    public Player(DBX dbx_p)
     {
         dbx = dbx_p;
     }
     
    public Player(Player t)
     {
         this.id = t.id;
         this.name = t.name;
         this.health = t.health;
         this.skill_level = t.skill_level;
         this.pos_x = t.pos_x;
         this.pos_y = t.pos_y;
         this.region_id = t.region_id;
         this.dbx = t.dbx;
         
         // Do not copy the iterator information, since that is private
         // to the specific object.
         this.dbxIter = null;
     }
     
     /**
      * Create the object on the database.
      * @throws Exception 
      */
     @Override
     public void db_create() throws Exception
     {
         String sqlStr; // SQL query text
         PreparedStatement pstmt; // SQL statement object
         int rowCount; // Rows affected by statement execution
         ResultSet rs; // Result set for generated key
         long newId; // ID of newly created test entity
         String keyColumns[] = {"id"}; // Key attribute
 
         // SQL query
         sqlStr = "INSERT INTO player (name, health, skill_level, birth_date, pos_x, pos_y, region_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
 
         // Create the SQL statement object and prepare the statement
         pstmt = dbx.getConnection().prepareStatement(sqlStr, keyColumns);
 
         // Set the parameter values, with the index corresponding to
         // the appropriate question mark sequence in the query string.
         pstmt.setString(1, this.getName());
         pstmt.setInt(2, this.getHealth());
         pstmt.setInt(3, this.getSkillLevel());
         pstmt.setInt(4, this.getBirthDate());
         pstmt.setInt(5, this.getPosX());
         pstmt.setInt(6, this.getPosY());
         pstmt.setInt(7, this.getRegionID());
     
         // Execute the query
         rowCount = pstmt.executeUpdate();
         if(rowCount != 1)
         {
             throw new Exception("ERROR: DBX.testCreate: statement execution failed");
         }
 
         // Get the key value of the new generated entity
         rs = pstmt.getGeneratedKeys();
         rs.next();
         newId = rs.getLong(1);
         this.setId(newId);
 
         rs.close();
         pstmt.close();
 
         dbx.getConnection().commit();
     }
     
     /**
      * Read the object data from the database.
      * @throws Exception 
      */
     @Override
     public void db_read() throws Exception
     {
         String sqlStr; // SQL query text
         PreparedStatement pstmt; // SQL statement object
         ResultSet rs; // Result set for entity read
 
         // SQL query
         sqlStr = "SELECT name, health, skill_level, birth_date, pos_x, pos_y, region_id FROM player WHERE id = ?";
 
         // Create the SQL statement object and prepare the statement
         pstmt = dbx.getConnection().prepareStatement(sqlStr);
 
         // Set the parameter values, with the index corresponding to
         // the appropriate question mark sequence in the query string.
         pstmt.setLong(1, this.id);
 
         // Execute the query
         rs = pstmt.executeQuery();
         
         // Read the single entity from the result set.
         if(!rs.next())
         {
             throw new Exception("ERROR: DBX.testRead: no entity found");
         }
         this.setId(rs.getLong(1));
         this.setName(rs.getString(2));
         this.setHealth(rs.getInt(3));
         this.setSkillLevel(rs.getInt(4));
         this.setBirthDate(rs.getInt(5));
         this.setPosX(rs.getInt(5));
         this.setPosY(rs.getInt(6));
         this.setRegionID(rs.getInt(7));
        
         if(rs.next())
         {
             throw new Exception("ERROR: DBX.testRead: more than one entity found");
         }
 
         rs.close();
         pstmt.close();
         dbx.getConnection().commit();
     }
     
     /**
      * Modify the object data on the database to match the current object.
      * @throws Exception 
      */
     @Override
     public void db_update() throws Exception
     {
         String sqlStr; // SQL query text
         PreparedStatement pstmt; // SQL statement object
         int rowCount; // Rows affected by statement execution
 
         // SQL query
         sqlStr = "UPDATE player SET name = ?, health = ?, skill_level = ?, birth_date = ?, pos_x = ?, pos_y = ?, region_id = ? WHERE id = ?";
 
         // Create the SQL statement object and prepare the statement
         pstmt = dbx.getConnection().prepareStatement(sqlStr);
 
         // Set the parameter values, with the index corresponding to
         // the appropriate question mark sequence in the query string.
         pstmt.setString(1, this.getName());
         pstmt.setInt(2, this.getHealth());
         pstmt.setInt(3, this.getSkillLevel());
         pstmt.setInt(4, this.getBirthDate());
         pstmt.setInt(5, this.getPosX());
         pstmt.setInt(6, this.getPosY());
         pstmt.setInt(7, this.getRegionID());
         pstmt.setLong(8, this.getId());
 
         // Execute the query
         rowCount = pstmt.executeUpdate();
         if(rowCount < 1)
         {
             throw new Exception("ERROR: DBX.testUpdate: statement execution failed");
         }
 
         pstmt.close();
         dbx.getConnection().commit();
     }
     
     /**
      * Delete the object from the database.
      * @throws Exception 
      */
     @Override
     public void db_delete() throws Exception
     {
         String sqlStr; // SQL query text
         PreparedStatement pstmt; // SQL statement object
         int rowCount; // Rows affected by statement execution
 
         // SQL query
         sqlStr = "DELETE player FROM player WHERE id = ?";
 
         // Create the SQL statement object and prepare the statement
         pstmt = dbx.getConnection().prepareStatement(sqlStr);
 
         // Set the parameter values, with the index corresponding to
         // the appropriate question mark sequence in the query string.
         pstmt.setLong(1, this.getId());
 
         // Execute the query
         rowCount = pstmt.executeUpdate();
         if(rowCount < 1)
         {
             throw new Exception("ERROR: DBX.testDelete: statement execution failed");
         }
 
         pstmt.close();
         dbx.getConnection().commit();
     }
 
     /**
      * ======================================================================
      * Functionality for iterating over several objects.
      * ======================================================================
      */   
     
     /**
      * Initialize iterator and perform database query and other database
      * operations that enable iterating over several objets.
      * @throws Exception 
      */
     @Override
     public void db_iter_begin() throws Exception
     {
         if(dbxIter != null)
         {
             throw new Exception("ERROR: Test.db_iter_begin: iterator already in use");
         }
         dbxIter = new DBXIter();
 
         // SQL query
         dbxIter.sqlStr = "SELECT name, health, skill_level, birth_date, pos_x, pos_y, region_id FROM player WHERE id = ?";
 
         // Create the SQL statement object and prepare the statement
         dbxIter.pstmt = dbx.getConnection().prepareStatement(dbxIter.sqlStr);
 
         // Set the parameter values, with the index corresponding to
         // the appropriate question mark sequence in the query string.
         // pstmt.setLong(1, x);
 
         // Execute the query
         dbxIter.rs = dbxIter.pstmt.executeQuery();
 }
     
     /**
      * Retrieve the data for the next object from the database and place
      * it on the current object.
      * 
      * @return True if retrieval was successful, false if there are no more
      * objects to retrieve.
      * 
      * @throws Exception 
      */
     @Override
     public boolean db_iter_next() throws Exception
     {
         if(dbxIter == null)
         {
             throw new Exception("ERROR: Test.db_iter_next: iterator not ready");
         }
         
         boolean haveMore;
         
         haveMore = dbxIter.rs.next();
         if(haveMore)
         {
             this.setId(dbxIter.rs.getLong(1));
             this.setName(dbxIter.rs.getString(2));
             this.setHealth(dbxIter.rs.getInt(3));
             this.setSkillLevel(dbxIter.rs.getInt(4));
             this.setBirthDate(dbxIter.rs.getInt(5));
             this.setPosX(dbxIter.rs.getInt(6));
             this.setPosY(dbxIter.rs.getInt(7));
             this.setRegionID(dbxIter.rs.getInt(8));
         }
         
         return haveMore;
     }
     
     /**
      * End iteration and release associated resources.
      * @throws Exception 
      */
     @Override
     public void db_iter_end() throws Exception
     {
         if(dbxIter == null)
         {
             throw new Exception("ERROR: Test.db_iter_end: attempting to end non existent iteration");
         }
         
         dbxIter.rs.close();
         dbxIter.pstmt.close();
         dbx.getConnection().commit();
 
         dbxIter = null;
     }   
 }
