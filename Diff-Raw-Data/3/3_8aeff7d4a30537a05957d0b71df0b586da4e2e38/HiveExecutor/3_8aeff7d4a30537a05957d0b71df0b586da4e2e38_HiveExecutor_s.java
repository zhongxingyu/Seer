 package org.hquery.queryExecutor.impl;
 
 import static org.hquery.common.util.HQueryConstants.QUERY;
 import static org.hquery.common.util.HQueryConstants.USER_PREF;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.ql.session.SessionState;
 import org.apache.log4j.Logger;
 import org.hquery.common.util.Context;
 import org.hquery.common.util.HQueryUtil;
 import org.hquery.common.util.UserPreferences;
 import org.hquery.queryExecutor.QueryExecutor;
 import org.hquery.querygen.dialect.HiveDialect;
 import org.hquery.status.impl.JobStatusCheckerImpl.StatusCheckerThread;
 
 public class HiveExecutor implements QueryExecutor {
 
 	private String sqlString;
 	private String userOutputFile;
 	private String delimiter;
 	private String hqueryFileLocation;
 	private String sessionId;
 	private StatusCheckerThread statusCheckerThread;
 
 	public void setStatusCheckerThread(StatusCheckerThread statusCheckerThread) {
 		this.statusCheckerThread = statusCheckerThread;
 	}
 
 	private static Logger logger = Logger.getLogger(HiveExecutor.class);
 
 	@Override
 	public void setQuery(String sqlString) {
 		this.sqlString = sqlString;
 	}
 
 	@Override
 	public String executeQuery(Context ctx) {
 		HiveConf conf = new HiveConf(this.getClass());
 		SessionState.start(conf);
 		SessionState sessionState = SessionState.get();
 		sessionId = sessionState.getSessionId();
 
 		if (StringUtils.isBlank(this.sqlString))
 			this.sqlString = (String) ctx.get(QUERY);
 
 		UserPreferences preferences = (UserPreferences) ctx.get(USER_PREF);
 		if (preferences != null) {
 			this.userOutputFile = preferences.getOutputFile();
 			this.delimiter = preferences.getOutputFileType().getDelimiter();
 		}
 
 		hqueryFileLocation = HQueryUtil.getResourceString("hquery-conf",
 				"hquery.output.directory") + File.separator + sessionId;
 
 		if (logger.isInfoEnabled())
 			logger.info("Job output will be saved to following HDFS location: "
 					+ hqueryFileLocation);
 		this.sqlString = HiveDialect
 				.getInsertOverwriteString(hqueryFileLocation)
 				.append(this.sqlString).toString();
 		ExecutorService executor = Executors.newSingleThreadExecutor();
 		executor.submit(new Worker());
 		executor.shutdown();
 
 		if (logger.isDebugEnabled())
 			logger.debug("Now submitted the job");
 
 		return sessionId;
 	}
 
 	class Worker implements Runnable {
 
 		@Override
 		public void run() {
 
 			Connection con = null;
 			Statement stmt = null;
 			ResultSet res = null;
 			try {
 				String driverName = HQueryUtil.getResourceString("hquery-conf",
 						"hive.driverClass");
 				String hiveServerHost = HQueryUtil.getResourceString(
 						"hquery-conf", "hive.server.host");
 				String hiveServerPort = HQueryUtil.getResourceString(
 						"hquery-conf", "hive.server.port");
 				String dbName = HQueryUtil.getResourceString("hquery-conf",
 						"hive.db.name");
 
 				assert (StringUtils.isNotBlank(driverName)) : "Hive driver name can't be null";
 				assert (StringUtils.isNotBlank(hiveServerHost)) : "Hive server host name can't be null";
 				assert (StringUtils.isNotBlank(hiveServerPort)) : "Hive server port name can't be null";
 				assert (StringUtils.isNotBlank(dbName)) : "DB name can't be null";
 
 				Class.forName(driverName);
 				con = DriverManager.getConnection(new StringBuffer(
 						"jdbc:hive://").append(hiveServerHost).append(':')
 						.append(hiveServerPort).append('/').append(dbName)
 						.toString(), "", "");
 				stmt = con.createStatement();
 				if (logger.isDebugEnabled())
 					logger.debug("Executing following query: " + sqlString);
 				res = stmt.executeQuery(sqlString);
				statusCheckerThread.requestStop();
 				if (logger.isDebugEnabled())
 					logger.debug("Query execution over, processing and writing output .. ");
 				String fileProcessingString = HQueryUtil
 						.getDelimiterTranslationCommand(hqueryFileLocation
 								+ File.separator + "*", delimiter,
 								userOutputFile);
 				try {
 					String[] cmd = { "/bin/sh", "-c", fileProcessingString };
 					Process process = Runtime.getRuntime().exec(cmd);
 					int result = process.waitFor();
 
 					StringWriter writer = new StringWriter();
 					IOUtils.copy(process.getErrorStream(), writer, null);
 					String errorString = writer.toString();
 					if (StringUtils.isNotBlank(errorString))
 						logger.error(errorString);
 
 					if (logger.isInfoEnabled())
 						logger.info((result == 0) ? "File processing completed successfully, output written to: "
 								+ userOutputFile
 								: "Error encountered while file processing: Error:\n "
 										+ errorString);
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				} catch (InterruptedException ie) {
 					ie.printStackTrace();
 				}
 
 				try {
 					Thread.sleep(Long.parseLong(HQueryUtil.getResourceString(
 							"hquery-conf", "hquery.cooldown.period"))); //giving other daemon threads chance for graceful stop 
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				if (logger.isInfoEnabled())
 					logger.info("Processing over, worker thread terminating now");
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					res.close();
 					stmt.close();
 					con.close();
 				} catch (SQLException e) {
 					logger.error("Error encountered when trying to close resultset/statement/connection: ["
 							+ e.getMessage() + "]");
 				}
 			}
 		}
 	}
 
 }
