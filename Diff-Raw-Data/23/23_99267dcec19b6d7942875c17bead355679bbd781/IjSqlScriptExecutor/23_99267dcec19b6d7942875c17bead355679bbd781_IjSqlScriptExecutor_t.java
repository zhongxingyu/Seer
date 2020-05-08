/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 package net.sf.springderby;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.derby.tools.ij;
 import org.springframework.core.io.Resource;
 import org.springframework.dao.DataAccessException;
 
/**
 * SQL Script executor based on {@link ij}.
 * Note that this executor requires version 10.2.1.6 or later of <tt>derbytools.jar</tt> since it
 * depends on the {@link ij#runScript(Connection, InputStream, String, java.io.OutputStream, String)} method,
 * which doesn't exist in prior versions.
 * 
 * @author Andreas Veithen
 * @version $Id$
 */
 public class IjSqlScriptExecutor implements SqlScriptExecutor {
 	private final static Log log = LogFactory.getLog(IjSqlScriptExecutor.class);
 	
 	public void executeScript(Connection connection, Resource script, String encoding) throws SQLException, DataAccessException, IOException {
 		InputStream in = script.getInputStream();
 		try {
 			LoggerOutputStream out = new LoggerOutputStream(log, "UTF-8");
 			try {
 				// runScript returns the number of SQLExceptions thrown during the execution
 				if (ij.runScript(connection, in, encoding, out, "UTF-8") > 0) {
 					// TODO: this exception is no longer appropriate
 					throw new SchemaCreationException("Script " + script.getFilename() + " executed with errors");
 				}
 			}
 			finally {
 				out.close();
 			}
 		}
 		finally {
 			in.close();
 		}
 	}
 }
