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
