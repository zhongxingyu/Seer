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
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.system.components.ext.StreamDelimiter;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.tools.db.AbstractDBComponent;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 
 /**
  * This component reads from an sql SELECT pushes its content inside of a tuple
  *
  * @author Mike Haberman
  * see http://jolbox.com/ for connection pool
  * RUNTIME: depends on:
  * guava-r06.jar   (google collections)
  * slf4j-api-1.6.1.jar, slf4j-log4j12-1.6.1.jar (connection pooling logging)
  *
  * @author Boris Capitanu
  *
  */
 
 @Component(
 		name = "SQL To Tuple",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.any,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tuple, tools, database",
 		description = "This component reads a mysql database",
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar",
 				      "guava-r06.jar", "slf4j-api-1.6.1.jar","slf4j-log4j12-1.6.1.jar"}
 )
 public class SQLToTuple extends AbstractDBComponent {
 
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
 
 	//--------------------------------------------------------------------------------------------
 
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    super.initializeCallBack(ccp);
 	}
 
 	@Override
 	public void executeCallBack(ComponentContext cc) throws Exception {
 	    super.executeCallBack(cc);
 
 	    componentInputCache.storeIfAvailable(cc, IN_QUERY);
 
 	    if (connectionPool == null || !componentInputCache.hasData(IN_QUERY))
 	        // Not ready to process
 	        return;
 
         Object input;
         while ((input = componentInputCache.retrieveNext(IN_QUERY)) != null) {
             if (input instanceof StreamDelimiter) {
                 StreamDelimiter sd = (StreamDelimiter) input;
                 if (sd.getStreamId() == streamId)
                     throw new ComponentExecutionException(String.format("Stream id conflict! Incoming stream has the same id (%d) " +
                             "as the one set for this component (%s)!", streamId, getClass().getSimpleName()));
 
                 cc.pushDataComponentToOutput(OUT_META_TUPLE, sd);
                 cc.pushDataComponentToOutput(OUT_TUPLES, sd);
                 continue;
             }
 
             String query = DataTypeParser.parseAsString(input)[0];
             processQuery(query);
         }
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         super.disposeCallBack(ccp);
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public boolean isAccumulator() {
         return false;
     }
 
     @Override
     public void handleStreamInitiators() throws Exception {
         // Since this component is a FiringPolicy.any, it is possible that when handleStreamInitiators() is
         // called, there could be non-StreamDelimiter data that has arrived on other ports which would be
         // lost if we don't call 'executeCallBack' (when handleStreamInitiators() gets called, executeCallBack is NOT called)
 
         executeCallBack(componentContext);
     }
 
     @Override
     public void handleStreamTerminators() throws Exception {
         executeCallBack(componentContext);
     }
 
     //--------------------------------------------------------------------------------------------
 
     protected void processQuery(String query) throws SQLException, ComponentContextException {
         SimpleTuplePeer outPeer;
         List<Strings> output;
 
         Connection connection = null;
         Statement stmt = null;
         try {
             connection = connectionPool.getConnection(); // fetch a connection
 
             // Statements allow to issue SQL queries to the database
             stmt = connection.createStatement();
             // Result set get the result of the SQL query
             console.fine("DB Query: " + query);
             ResultSet resultSet = stmt.executeQuery(query);
             ResultSetMetaData rsMetaData = resultSet.getMetaData();
 
             int numberOfColumns = rsMetaData.getColumnCount();
             String[] fieldNames = new String[numberOfColumns];
             for (int i = 0; i < numberOfColumns; i++) {
                 String columnName = rsMetaData.getColumnName(i+1);
                 fieldNames[i] = columnName;
             }
             outPeer = new SimpleTuplePeer(fieldNames);
             SimpleTuple outTuple = outPeer.createTuple();
 
             output = new ArrayList<Strings>();
             while (resultSet.next()) {
                 for (int i = 0; i < numberOfColumns; i++) {
                     String columnName = rsMetaData.getColumnName(i+1);
                     String value = resultSet.getString(columnName);
                     outTuple.setValue(i, value);
                 }
                 output.add(outTuple.convert());
             }
         }
         finally {
             releaseConnection(connection, stmt);
         }
 
         // Output message to the error output port
         if (output.size() == 0)
             outputError("No database records match the search query.", Level.WARNING);
 
         Strings[] results = new Strings[output.size()];
         output.toArray(results);
         StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
         componentContext.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
         //
         // metaData for this tuple producer
         //
         componentContext.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
     }
 }
