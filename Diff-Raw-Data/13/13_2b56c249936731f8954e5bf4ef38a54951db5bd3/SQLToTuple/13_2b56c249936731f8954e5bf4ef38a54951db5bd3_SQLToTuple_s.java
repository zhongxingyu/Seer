 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.tools.tuples;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 
 
 /**
  * This component reads from an sql SELECT pushes its content inside of a tuple
  *
  * @author Mike Haberman
  *
  */
 
 @Component(
 		name = "SQL To Tuple",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tuple, tools, database",
 		description = "This component reads a mysql database",
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class SQLToTuple extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS -----------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_QUERY,
             description = "Database query statement whose contents will be pushed out e.g. select * from a,b where a.id = b.id"
     )
     protected static final String IN_QUERY = Names.PORT_QUERY;
 
 	//------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TUPLES,
 			description = "tuples (based on the SQL)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
 	)
 	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentOutput(
 			name = Names.PORT_META_TUPLE,
 			description = "meta data for the tuple (column names from select!)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
 	@ComponentProperty(
 			name = "user",
 			description = "user",
 		    defaultValue = ""
 	)
 	protected static final String PROP_USER = "user";
 
 	@ComponentProperty(
 			name = "password",
 			description = "password",
 		    defaultValue = ""
 	)
 	protected static final String PROP_PASSWORD = "password";
 
 	@ComponentProperty(
 			name = "hostDB",
 			description = "host/database eg. localhost/testDB",
 		    defaultValue = ""
 	)
 	protected static final String PROP_DB = "hostDB";
 
 	@ComponentProperty(
 			name = "protocol",
 			description = "jdbc protocol for hostURL",
 		    defaultValue = "jdbc:mysql://"
 	)
 	protected static final String PROP_PROTOCOL = "protocol";
 
 	@ComponentProperty(
 			name = "JDBCDriver",
 			description = "jdbc driver to be loaded, jar must be in classpath",
 		    defaultValue = "com.mysql.jdbc.Driver"
 	)
 	protected static final String PROP_JDBC = "JDBCDriver";
 
 
 
 	//--------------------------------------------------------------------------------------------
 	Connection connect = null;
 	//--------------------------------------------------------------------------------------------
 
 
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception
     {
 		String user        = ccp.getProperty(PROP_USER).trim();
 	    String password    = ccp.getProperty(PROP_PASSWORD).trim();
 	    String protocol    = ccp.getProperty(PROP_PROTOCOL).trim();
 	    String hostDB     = ccp.getProperty(PROP_DB).trim();
 	    String JDBC_DRIVER = ccp.getProperty(PROP_JDBC).trim();


	    //String fullURL = protocol + hostDB + "?" + "user="+user + "&password="+password;
 	    String fullURL = protocol + hostDB;
 
 	    console.info("connect using " + fullURL);
 
 		// This will load the MySQL driver, each DB has its own driver
 	    try {
 	        Class.forName(JDBC_DRIVER);
 	    }
 	    catch (Exception e) {
 	        console.log(Level.SEVERE, "Could not load the JDBC driver: " + JDBC_DRIVER, e);
 	        throw e;
 	    }
 
 		// Setup the connection with the DB
 		try {
 		    connect = DriverManager.getConnection(fullURL, user, password);
 		}
 		catch (Exception e) {
 		    console.log(Level.SEVERE, "Could not connect to the DB", e);
 		    throw e;
 		}
 
 		if (connect == null) {
 			String msg = "Unable to get connection to database";
 			console.severe(msg);
 			throw new ComponentExecutionException(msg);
 		}
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception
     {
 		SimpleTuplePeer outPeer;
 		SimpleTuple outTuple;
 
 		if (connect == null) {
 			console.severe("sql connection never established");
 			return;
 		}
 
 		Statement statement = null;
 		ResultSet resultSet = null;
 		String[] input = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_QUERY));
 
 		String SQL = input[0].trim();
 
 		// Statements allow to issue SQL queries to the database
 		statement = connect.createStatement();
 		// Result set get the result of the SQL query
 		resultSet = statement.executeQuery(SQL);
 		ResultSetMetaData rsMetaData = resultSet.getMetaData();
 
 	    int numberOfColumns = rsMetaData.getColumnCount();
 	    String[] fieldNames = new String[numberOfColumns];
 	    for (int i = 0; i < numberOfColumns; i++) {
 		      String columnName = rsMetaData.getColumnName(i+1);
 		      fieldNames[i] = columnName;
 	    }
 	    outPeer = new SimpleTuplePeer(fieldNames);
 		outTuple = outPeer.createTuple();
 
 		List<Strings> output = new ArrayList<Strings>();
 		while (resultSet.next()) {
 			for (int i = 0; i < numberOfColumns; i++) {
 			      String columnName = rsMetaData.getColumnName(i+1);
 			      String value = resultSet.getString(columnName);
 			      outTuple.setValue(i, value);
 			}
 			output.add(outTuple.convert());
 		}
 

 		resultSet.close();
 		statement.close();
 

 		Strings[] results = new Strings[output.size()];
 		output.toArray(results);
 		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
 		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
 	    //
 		// metaData for this tuple producer
 		//
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
 
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception
     {
         if (connect != null) {
            connect.close();
         }

     }
 }
