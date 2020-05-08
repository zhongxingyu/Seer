 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  *  Notes:
  *  
  * 	TABLE_CAT String => table catalog (may be null)
  * 	TABLE_SCHEM String => table schema (may be null)
  * 	TABLE_NAME String => table name
  * 	TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
  * 	REMARKS String => explanatory comment on the table
  * 	TYPE_CAT String => the types catalog (may be null)
  * 	TYPE_SCHEM String => the types schema (may be null)
  * 	TYPE_NAME String => type name (may be null)
  * 	SELF_REFERENCHoopG_COL_NAME String => name of the designated "identifier" column of a typed table (may be null)
  * 	REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (may be null)
  * 
  */
 
 package edu.cmu.cs.in.hoop.hoops.load;
 
 import java.util.ArrayList;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import edu.cmu.cs.in.base.HoopStringTools;
 import edu.cmu.cs.in.base.io.HoopFileTools;
 //import edu.cmu.cs.in.base.kv.HoopKV;
 import edu.cmu.cs.in.base.kv.HoopKVInteger;
 import edu.cmu.cs.in.base.kv.HoopKVString;
 import edu.cmu.cs.in.base.HoopDataType;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.base.HoopInterface;
 import edu.cmu.cs.in.hoop.hoops.base.HoopLoadBase;
 import edu.cmu.cs.in.hoop.properties.types.HoopEnumSerializable;
 import edu.cmu.cs.in.hoop.properties.types.HoopStringSerializable;
 
 /**
  * 
  */
 public class HoopMySQLReader extends HoopLoadBase implements HoopInterface
 {
 	private static final long serialVersionUID = 1777614084867154645L;
 	
 	public	String driver = "com.mysql.jdbc.Driver";
     public	String driverAlias="mysql";
     public	String protocol ="composite";
     
     public	HoopStringSerializable username=null;
     public	HoopStringSerializable password=null;
     public	HoopStringSerializable dbName=null;
     public	HoopStringSerializable dbServer=null;
     
     public	HoopStringSerializable queryTable=null;
     public	HoopStringSerializable queryColumns=null;
         
     public	HoopEnumSerializable queryType=null;
     
     private Connection connection = null;
                
     /**
      * 
      */
     public HoopMySQLReader ()
     {
 		setClassName ("HoopMySQLReader");
 		debug ("HoopMySQLReader ()");
 		
 		setHoopDescription ("Load KVs from a MySQL Database");
 
 		removeInPort ("KV");
 		
 		// Default values ...
     	
     	username=new HoopStringSerializable (this,"username","root");
     	password=new HoopStringSerializable (this,"password","");
     	dbName=new HoopStringSerializable (this,"dbName","");
     	dbServer=new HoopStringSerializable (this,"dbServer","127.0.0.1");
     	    	
     	queryTable=new HoopStringSerializable (this,"queryTable","default");
     	queryColumns=new HoopStringSerializable (this,"queryColumns","");
     	    	
     	queryType=new HoopEnumSerializable (this,"queryType","TABLEINFO,TABLEDATA,DATABASES,DATABASEINFO");
     }
     /**
      * 
      */
 	public String getUsername() 
 	{
 		return username.getValue();
 	}
     /**
      * 
      */	
 	public void setUsername(String aValue) 
 	{
 		this.username.setValue(aValue);
 	}
     /**
      * 
      */	
 	public String getPassword() 
 	{
 		return password.getValue();
 	}
     /**
      * 
      */	
 	public void setPassword(String aValue) 
 	{
 		this.password.setValue (aValue);
 	}    
 	/**
 	 * 
 	 * @return
 	 */
 	public String getDbName() 
 	{
 		return dbName.getValue();
 	}
 	/**
 	 * 
 	 */
 	public void setDbName(String aValue) 
 	{
 		this.dbName.setValue(aValue);
 	}
 	/**
 	 * 
 	 */
 	public Connection getConnection() 
 	{
 		return connection;
 	}
 	/**
 	 * 
 	 */
 	public void setConnection(Connection conn) 
 	{
 		this.connection = conn;
 	}	
     /**
      * Loads the appropriate JDBC driver for this environment/framework. For
      * example, if we are in an embedded environment, we load Derby's
      * embedded Driver, <code>org.apache.derby.jdbc.EmbeddedDriver</code>.
      * 
      * The JDBC driver is loaded by loading its class.
      * If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
      * be automatically loaded, making this code optional.
      * 
      * In an embedded environment, this will also start up the Derby
      * engine (though not any databases), since it is not already
      * running. In a client environment, the Derby engine is being run
      * by the network server framework.
      * 
      * In an embedded environment, any static Derby system properties
      * must be set before loading the driver to take effect.
      */
     private Boolean loadDriver() 
     {
     	debug ("loadDriver ()");
     	
 		if (this.getExecutionCount ()>0)
 		{
 			debug ("We've already connected to the database");
 			return (true);
 		}    	
     	
     	protocol="jdbc:"+driverAlias+"://"+dbServer.getValue()+":3306/mysql";
     	
         try 
         {
             Class.forName(driver).newInstance();
             debug ("Loaded the appropriate driver");
         } 
         catch (ClassNotFoundException cnfe) 
         {
             debug ("Unable to load the JDBC driver " + driver +". Please check your CLASSPATH.");
             this.setErrorString("Unable to load the JDBC driver " + driver +". Please check your CLASSPATH.");
             cnfe.printStackTrace(System.err);
             
             debug ("CLASSPATH: " + HoopFileTools.getClassPath ());
             
             return (false);
         } 
         catch (InstantiationException ie) 
         {
             debug ("Unable to instantiate the JDBC driver " + driver);
             this.setErrorString("Unable to instantiate the JDBC driver " + driver);
             ie.printStackTrace(System.err);
             return (false);
         } 
         catch (IllegalAccessException iae) 
         {
             debug ("Not allowed to access the JDBC driver " + driver);
             this.setErrorString("Not allowed to access the JDBC driver " + driver);
             iae.printStackTrace(System.err);
             return (false);
         }
         
         return (true);
     }
 	/**
 	 * http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-configuration-properties.html
 	 */
 	private Boolean connect ()
 	{
 		debug ("connect ()");
 		
 		if (this.getExecutionCount ()>0)
 		{
 			debug ("We've already connected to the database");
 			return (true);
 		}
 				
 		protocol="jdbc:"+driverAlias+"://"+dbServer.getValue()+":3306/"+dbName.getValue();
 		
         try 
         {
         	debug ("Connecting to: " + protocol + " with username '" + username.getValue() + "' and password: " + password.getValue());
         	
 			connection = DriverManager.getConnection (protocol,username.getValue(),password.getValue());
 		} 
         catch (SQLException e) 
         {
         	this.setErrorString("Error connecting to database");
         	printSQLException (e);
         	debug ("RETURNING FALSE ...");
 			return (false);
 		}
 
         debug ("Connected to database " + dbName.getValue());
         
         return (true);
 	}    
 	/**
 	 * http://www.kitebird.com/articles/jdbc.html
 	 */
 	public Boolean runHoop (HoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 					
 		if (username.getValue().isEmpty()==true)
 		{
 			this.setErrorString("Please provide a username for the database");
 			return (false);
 		}
 		
 		if (password.getValue().isEmpty()==true)
 		{
 			this.setErrorString("Please provide a password for the username");
 			return (false);
 		}		
 				
 		if (loadDriver()==false)
 		{
 			return (false);
 		}
 		
 		if (connect ()==false)
 		{
 			return (false);
 		}
 		
 		if (dbName.getValue().isEmpty()==true)
 		{
 			return (getDatabases ());
 		}
 		
 		debug ("Executing MySQL Hoop with query type: " + queryType.getValue());
 				
 		if (queryType.getValue().toLowerCase().equals("tableinfo")==true)
 		{					
 			getTableRows ();
 			return (true);
 		}
 		
 		if (queryType.getValue().toLowerCase().equals("databases")==true)
 		{
 			return (getDatabases ());
 		}
 		
 		if (queryType.getValue().toLowerCase().equals("databaseinfo")==true)
 		{
 			return (getDatabaseInfo ());
 		}
 		
 		if (queryType.getValue().toLowerCase().equals("tabledata")==true)	
 		{
 			if (this.getExecutionCount()==0)
 			{
 				getDatabaseInfo ();
 				
 				debug ("Batch size for table '"+queryTable.getPropValue()+"' is: " + batchSize.getPropValue());
 				debug ("Max load size for table '"+queryTable.getPropValue()+"' is: " + queryMax.getPropValue());
 							
 			    loadIndex=0; // Reset our global index
 			    bCount=0; // Reset our global batch index
 												
 				bSize=Integer.parseInt(batchSize.getPropValue());
 			    loadMax=Integer.parseInt(queryMax.getPropValue());
 								
 			    if (loadMax>0)
 			    {
 			    	if (bSize>loadMax)
 			    		loadMax=bSize;
 			    }	
 				
 				HoopKVString aKV=(HoopKVString) getKVFromKey(queryTable.getPropValue ());
 				
 				if (aKV==null)
 				{
 					this.setErrorString ("Error: can't obtain key from: " + queryTable.getPropValue ());
 					debug ("Error: can't obtain key from: " + queryTable.getValue ());
 					return (false);
 				}
 				
 				int loadMaxTemp=Integer.parseInt(aKV.getValue());
 				
 				debug ("User max cap set to: " + loadMaxTemp);
 				
 				if ((loadMaxTemp<loadMax) || (loadMax<=0))
 				{
 					loadMax=loadMaxTemp;
 				}
 				
 				debug ("Loading rows from table " + queryTable.getPropValue () + " with chunk size: " + bSize + " for a total of: " + loadMax + " rows");
 								
 				resetData (); // We have the information we wanted, no need to keep it
 			}
  			
 			if (runQuery ()==false)
 				return (false);
 		}
 		
 		if (this.getDone()==true)
 		{
 			close ();
 		}
 		
 		return (true);
 	}	
 	/**
 	 * 
 	 */
 	private Boolean getDatabases ()
 	{
 		debug ("getDatabases ()");
 		
 		if (connection==null)
 		{
 			this.setErrorString("Error: no connection to database yet");
 			return (false);
 		}
 								
 		Statement statement=null;
 		
 		try 
 		{
 			statement = connection.createStatement ();
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			return (false);
 		}
 		
 		try 
 		{
 			statement.executeQuery ("SHOW DATABASES");
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			return (false);			
 		}
 		
 		ResultSet resultSet=null;
 		
 		try 
 		{
 			resultSet = statement.getResultSet ();
 		} 
 		catch (SQLException e1) 
 		{		
 			printSQLException (e1);
 			return (false);			
 		}
 		
 		int count = 0;
 		
 		resetData ();
 		
 		try 
 		{
 			while (resultSet.next ())
 			{				
 				HoopKVInteger tableKV=new HoopKVInteger ();
 								
 				tableKV.setKey(count);
 				tableKV.setValue(resultSet.getString("Database"));
 				
 				addKV (tableKV);
 				
 				++count;
 			}
 		} 
 		catch (SQLException e1) 
 		{		
 			printSQLException (e1);
 			return (false);			
 		}
 
 		try 
 		{
 			resultSet.close ();
 		} 
 		catch (SQLException e) 
 		{		
 			printSQLException (e);
 			return (false);			
 		}
 		
 		try 
 		{
 			statement.close ();
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			return (false);			
 		}
 		
 		Runtime r = Runtime.getRuntime();
 		r.gc();
 		
 		debug (count + " rows were retrieved, verification date size is: " + this.getData().size() + " from " + loadIndex + " with size: " + bSize);
 				
 		return (true);
 	}	
 	/**
 	 *  
 	 */
 	private Boolean getDatabaseInfo ()
 	{
 		debug ("getDatabaseInfo ()");
 		
 		/*
 		if (this.getExecutionCount ()>0)
 		{
 			debug ("We've already connected to the database");
 			return (true);
 		}
 		*/
 		
 		this.setKVType (0,HoopDataType.STRING,"Table");
 		this.setKVType (1,HoopDataType.STRING,"Nr. Rows");
 		
 		DatabaseMetaData metadata=null;
 		
 		try 
 		{
 			metadata = connection.getMetaData();
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			this.setErrorString("Error getting metadata from database");
 			return (false);
 		}
 		
 		ResultSet tableSet=null;
 		
 		try 
 		{
 			tableSet=metadata.getTables(null, null, null, new String[]{"TABLE"});
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			return (false);
 		}
 				
 		try 
 		{
 			while (tableSet.next()) 
 			{
 				String tableType=null;
 				
 				try 
 				{
 					Integer rowCount=1;
 					
 					tableType = tableSet.getString("TABLE_NAME");					
 					HoopKVString tableKV=new HoopKVString ();
 					tableKV.setKey(tableType);
 					
 					try
 					{
 						Statement st = connection.createStatement();
 						debug ("SELECT COUNT(*) FROM "+ tableType);
 						ResultSet res = st.executeQuery("SELECT COUNT(*) FROM "+ tableType);
 						
 						while (res.next())
 						{
 							rowCount = res.getInt(1);
 						}
 						
 						debug ("Number of columns for table "+ tableType + " is: " + rowCount);
 					}
 					catch (SQLException s)
 					{
 						printSQLException (s);
 						return (false);
 					}					
 					
 					tableKV.setValue(rowCount.toString());
 					
 					addKV (tableKV);
 				} 
 				catch (SQLException e) 
 				{
 					printSQLException (e);
 					return (false);
 				}				
 			}
 		} 
 		catch (SQLException e1) 
 		{
 			printSQLException (e1);
 			return (false);
 		}		
 		
 		try 
 		{
 			tableSet.close();
 		} 
 		catch (SQLException e) 
 		{		
 			printSQLException (e);
 			return (false);
 		}
 								
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	private Boolean getTableRows ()
 	{
 		debug ("getTableRows ()");
 				
 		if (connection==null)
 		{
 			this.setErrorString("Error: no connection to database yet");
 			return (false);
 		}
 		
 		if (queryTable.getValue().isEmpty()==true)
 		{
 			this.setErrorString("Error: please provide the name of a table for which to obtain column names");
 			return (false);			
 		}
 		
 		this.setKVType (0,HoopDataType.STRING,"Row Index");
 		this.setKVType (1,HoopDataType.STRING,"Row Name");
 		
 		try
 		{
 			  Statement st = connection.createStatement();
 			  
 			  ResultSet rs = st.executeQuery("SELECT * FROM " + queryTable.getValue());
 			  
 			  ResultSetMetaData md = rs.getMetaData();
 			  
 			  int col = md.getColumnCount();
 			  
 			  debug ("Number of entries : "+ col);
 			  			  
 			  for (int i = 1; i <= col; i++)
 			  {
 				  HoopKVInteger column=new HoopKVInteger ();
 				  
 				  String col_name=md.getColumnName(i);
 
 				  column.setKey(i);
 				  column.setValue(col_name);
 				  
 				  addKV (column);
 			  }			  			  
 			  
 			  st.close ();
 			  
 			  rs.close ();
 		}
 		catch (SQLException s)
 		{
 			printSQLException (s);
 			return (false);
 		}
 		
 		return (true);
 	}
 	/**
 	 * http://www.kitebird.com/articles/jdbc.html
 	 * 
 	 * For example: s.executeQuery ("SELECT id, name, category FROM animal");
 	 */
 	private Boolean runQuery ()
 	{
 		debug ("runQuery ()");
 		
 		if (connection==null)
 		{
 			this.setErrorString("Error: no connection to database yet");
 			return (false);
 		}
 				
 		if (queryColumns.getValue().isEmpty()==true)
 		{
 			debug ("Error: no target columns provided for SQL query");
 			return (false);
 		}
 				
 		if (queryTable.getValue().isEmpty()==true)
 		{
 			debug ("Error: no target table provided for SQL query");
 			return (false);
 		}
 
 		if (this.getExecutionCount()==0)
 		{
 			queryColumnsToTypes ();
 		}	
 								
 		String fullQuery=createQuery ();
 				
 		Statement statement=null;
 		
 		try 
 		{
 			statement = connection.createStatement ();
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			return (false);
 		}
 		
 		debug ("Executing: " + fullQuery);
 		
 		try 
 		{
 			statement.executeQuery (fullQuery);
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			return (false);			
 		}
 		
 		ResultSet resultSet=null;
 		
 		try 
 		{
 			resultSet = statement.getResultSet ();
 		} 
 		catch (SQLException e1) 
 		{		
 			printSQLException (e1);
 			return (false);			
 		}
 		
 		int count=0;
 		
 		resetData ();
 		
 		try 
 		{
 			while (resultSet.next ())
 			{				
 				HoopKVString tableKV=new HoopKVString ();
 				
 				ArrayList <HoopDataType> dbTypes=this.getTypes ();
 				
 				for (int i=0;i<dbTypes.size();i++)
 				{
 					HoopDataType aType=dbTypes.get(i);
 					
 					if (aType.getType()==HoopDataType.INT)
 					{
 						//debug ("Adding INT type");
 						
 						Integer idVal = resultSet.getInt (aType.getTypeValue());
 						
 						//debug ("nameVal: " + idVal);
 						
 						if (i==0)
 						{
 							tableKV.setKey(idVal.toString());
 						}
 						else
 							tableKV.setValue(idVal.toString (),i);
 					}
 					
 					if (aType.getType()==HoopDataType.STRING)
 					{						
 						//debug ("Adding STRING type");
 						
 						String nameVal = resultSet.getString (aType.getTypeValue());
 						
 						//debug ("nameVal: " + nameVal);
 						
 						if (i==0)
 						{
 							tableKV.setKey(nameVal);
 							//tableKV.setValue(nameVal,i);
 						}
 						else
 							tableKV.setValue(nameVal,i-1);						
 					}
 				}
 				
 				//debug ("Entry " + count + " has " + tableKV.getValuesRaw().size() + " values");
 				
 				//debug ("Key ("+count+"): " + tableKV.getKeyString () + ", Value: " + tableKV.getValue());
 				
 				addKV (tableKV);
 				
 				++count;				
 			}
 			
 			bCount++;
 		} 
 		catch (SQLException e1) 
 		{		
 			printSQLException (e1);
 			return (false);			
 		}
 
 		try 
 		{
 			resultSet.close ();
 		} 
 		catch (SQLException e) 
 		{		
 			printSQLException (e);
 			return (false);			
 		}
 		
 		try 
 		{
 			statement.close ();
 		} 
 		catch (SQLException e) 
 		{
 			printSQLException (e);
 			return (false);			
 		}
 		
 		Runtime r = Runtime.getRuntime();
 		r.gc();
 		
 		debug (count + " rows were retrieved, verification date size is: " + this.getData().size() + " starting at " + loadIndex + " with size: " + bSize);
 		
 		int retTotal=(bCount*bSize);
 		
 		debug ("tmpIndex: " + bCount + ", retTotal: " + retTotal);
 		
 		getVisualizer ().setExecutionInfo (" R: " + bCount + " = ("+retTotal+") out of " + loadMax);
 		
 	    loadIndex+=count;
 	    
 	    if (loadIndex<loadMax)
 	    {
 	    	debug ("loadIndex="+loadIndex+" < loadMax="+loadMax);
 	    	
 	    	this.setDone(false);
 	    }
 	    else
 	    	debug ("And ........ we're done here");
 	    
 	    if (reachedMax ()==true)
 	    {
 	    	debug ("Regardless if we're done or not, we've reached our max and so we're quitting ...");
 	    	this.setDone(true);
 	    }
 		
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	private String createQuery ()
 	{
 		debug ("createQuery ()");
 								
 		StringBuffer fullQuery=new StringBuffer ();
 		
 		//Integer calcWindow=loadIndex+bSize;
 		
 		fullQuery.append ("SELECT ");
 		fullQuery.append (queryColumns.getValue());
 		fullQuery.append (" from ");
 		fullQuery.append (queryTable.getValue());
 		fullQuery.append (" limit ");
 		fullQuery.append (loadIndex.toString());
 		fullQuery.append (",");
 		fullQuery.append (bSize.toString());
 		
 		debug ("Executing full query: [" + fullQuery.toString() + "]");		
 
 		return (fullQuery.toString());
 	}
 	/**
 	 * 
 	 */
 	private Boolean close ()
 	{
 		debug ("close ()");
 		
 		if (connection!=null)
 		{
 			try 
 			{
 				connection.close();
 			} 
 			catch (SQLException e) 
 			{
 				this.setErrorString("Error: Unable to close connection to database");
 				printSQLException (e);
 				return (false);
 			}
 		}
 		
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	private void queryColumnsToTypes ()
 	{
 		debug ("queryColumnsToTypes ()");
 		
 		ArrayList <String> columnList=HoopStringTools.splitComma(queryColumns.getValue());
 		
 		// For now we'll get everything as a string
 	
 		for (int j=0;j<columnList.size();j++)
 		{
 			debug ("Setting column " + j + " (String) to: " + columnList.get(j));
 			
 			this.setKVType (j,HoopDataType.STRING,columnList.get(j));
 		}
 	}		    
     /**
      * Prints details of an SQLException chain to <code>System.err</code>.
      * Details included are SQL State, Error code, Exception message.
      *
      * @param e the SQLException from which to print details.
      */
     protected void printSQLException (SQLException e)
     {
         // Unwraps the entire exception chain to unveil the real cause of the
         // Exception.
     	
         while (e != null)
         {
             debug ("----- SQLException -----");
             debug ("  SQL State:  " + e.getSQLState());
             debug ("  Error Code: " + e.getErrorCode());
             debug ("  Message:    " + e.getMessage());
             
             this.setErrorString ("SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode()+ ", Message: " + e.getMessage());
             
             e.printStackTrace (System.err);
             e = e.getNextException();
         }
     }	 
     /**
      * 
      */
     private Boolean reachedMax ()
     {
     	debug ("reachedMax ()");
     	
     	if (queryMax.getPropValue().isEmpty()==false)
     	{
     		Integer testValue=Integer.parseInt(queryMax.getPropValue());
     		
     		if (testValue>0)
     		{
     			debug ("We have a user set max, checking ...");
     			
     			if (loadIndex>=testValue)
     				return (true);
     		}
     		else
     		{
     			debug ("We're comparing the current index to the max value ("+loadIndex+","+testValue+")");
     			
     			if (loadIndex>=loadMax)
     				return (true);
     		}
     	}
     	
     	return (false);
     }
 	/**
 	 * 
 	 */
 	public HoopBase copy ()
 	{
 		return (new HoopMySQLReader ());
 	}    
 }
