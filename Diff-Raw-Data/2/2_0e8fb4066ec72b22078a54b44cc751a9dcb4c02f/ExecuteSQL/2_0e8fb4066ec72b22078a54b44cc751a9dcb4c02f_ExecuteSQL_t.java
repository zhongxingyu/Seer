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
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.system.components.ext.StreamDelimiter;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.tools.db.AbstractDBComponent;
 
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Execute SQL",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.any,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "tools, database, sql",
         description = "This component executes SQL statements passed in the input",
         dependency = { "protobuf-java-2.2.0.jar", "sqlite-jdbc-3.7.2.jar",
                        "guava-r09.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar" }
 )
 public class ExecuteSQL extends AbstractDBComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The SQL statements to be executed" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         super.initializeCallBack(ccp);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         super.executeCallBack(cc);
 
         componentInputCache.storeIfAvailable(cc, IN_TEXT);
 
         if (connectionPool == null || !componentInputCache.hasData(IN_TEXT))
             // we're not ready to process yet, return
             return;
 
         Connection connection = null;
         Statement stmt = null;
         try {
             connection = connectionPool.getConnection();
             stmt = connection.createStatement();
 
             Object input;
             while ((input = componentInputCache.retrieveNext(IN_TEXT)) != null) {
                 if (input instanceof StreamDelimiter) {
                     StreamDelimiter sd = (StreamDelimiter) input;
                     if (sd.getStreamId() == streamId)
                         throw new ComponentExecutionException(String.format("Stream id conflict! Incoming stream has the same id (%d) " +
                                 "as the one set for this component (%s)!", streamId, getClass().getSimpleName()));
 
                     // No output to push...
                     continue;
                 }
 
                 List<String> stmts = new ArrayList<String>();
                 String sqlStatements = DataTypeParser.parseAsString(input)[0];
                 for (String sql : sqlStatements.split("\n")) {
                 	sql = sql.trim();
                 	if (sql.startsWith("--") || sql.length() == 0)
                 		continue;
                 	if (sql.endsWith(";"))
                 		stmts.add(sql);
                 	else
                 		console.warning(String.format("Ignoring malformed SQL statement '%s'", sql));
                 }
 
                 for (String sql : stmts)
                     stmt.addBatch(sql);
 
                 int[] results = stmt.executeBatch();
 
                 for (int i = 0, iMax = results.length; i < iMax; i++) {
                     if (results[i] == Statement.EXECUTE_FAILED)
                         console.warning("SQL EXECUTE_FAILED: " + stmts.get(i));
                     else {
                         if (results[i] >= 0)
                            console.fine(String.format("%d rows updated: %s", results[i], stmts.get(i)));
                         else
                             console.fine("SQL SUCCESS_NO_INFO: " + stmts.get(i));
                     }
                 }
             }
         }
         finally {
             releaseConnection(connection, stmt);
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
         executeCallBack(componentContext);
     }
 
     @Override
     public void handleStreamTerminators() throws Exception {
         executeCallBack(componentContext);
     }
 }
