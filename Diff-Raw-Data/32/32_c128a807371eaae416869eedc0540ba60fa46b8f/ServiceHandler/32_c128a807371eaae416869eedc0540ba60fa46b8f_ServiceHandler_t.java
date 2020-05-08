 /**
  * CCCamp07 ScoringSystem
  * A CTF scoring bot & flag+advisory reporting system
  *
  * (C) 2007, Hans-Christian Esperer
  * hc at hcespererorg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the following
  *   disclaimer in the documentation and/or other materials provided
  *   with the distribution.
  * * Neither the name of the H. Ch. Esperer nor the names of his
  *   contributors may be used to endorse or promote products derived
  *   from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * POSSIBILITY OF SUCH DAMAGE
  **************************************************************************/
 
 package de.sectud.ctf07.scoringsystem;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Random;
 
 import org.hcesperer.utils.DBConnection;
 import org.hcesperer.utils.SQLConnection;
 
 import de.sectud.ctf07.scoringsystem.Executor.Action;
 import de.sectud.ctf07.scoringsystem.ReturnCode.ErrorValues;
 import de.sectud.ctf07.scoringsystem.ReturnCode.Success;
 
 /**
  * (service, team) flag handler.
  * 
  * This class calls the testscript for the specified service to a) store flags
  * and b) retrieve successfully stored flags later. For that purpose, the
  * testscript must a) exist and b) be executable.
  * 
  * It is strongly recommended to create a thread for each instance of this
  * class, as to prevent services and teams to interfere with one another.
  * 
  * @author Hans-Christian Esperer
  * 
  */
 public class ServiceHandler implements Runnable, QueueJob {
 
 	/**
 	 * Minimal age of a flag that may be collected in seconds
 	 */
 	private static int MINIMAL_FLAG_AGE = 900;
 
 	@SuppressWarnings("unused")
 	private int sid;
 
 	private String name;
 
 	private String script;
 
 	private int interval;
 
 	private boolean noQuit = true;
 
 	private final String team;
 
 	private final ReturnCode RETCODE_TIMEOUT = ReturnCode.makeReturnCode(
 			Success.FAILURE, ErrorValues.TIMEOUT);
 
 	private final EventLogger log = Logger.getLogger();
 
 	/**
 	 * RNG
 	 */
 	private Random random = new Random();
 
 	/**
 	 * Set to true if flags should be delivered
 	 */
 	private boolean distributeFlags = false;
 
 	/**
 	 * used for status display
 	 */
 	private static int CHARSTOPAD = 20;
 
 	/**
 	 * deprecated
 	 */
 	private final int startupSleepTime;
 
 	/**
 	 * Indicates whether runonce() was called already
 	 */
 	private boolean firstrun = true;
 
 	static {
 		try {
 			MINIMAL_FLAG_AGE = Integer.valueOf(SQLConnection.getInstance()
 					.getProperty("flagMinimalAge", "900"));
 		} catch (NumberFormatException e) {
 			MINIMAL_FLAG_AGE = 900;
 		}
 		System.out.printf(
 				"ServiceHandler: flagMinimalAge set to %d seconds.\n",
 				MINIMAL_FLAG_AGE);
 	}
 
 	public ServiceHandler(int sid, String name, String team, String script,
 			ScriptType type, int interval, int startDelay) {
 		this.sid = sid;
 		this.name = name;
 		this.script = script;
 		this.interval = interval;
 		this.team = team;
 		this.startupSleepTime = startDelay;
 
 		log.logMessage(this, "Created service handler for service %s, team %s",
 				name, team);
 	}
 
 	public void quit() {
 		System.out.printf("%s: quitting.\n", toString());
 		this.noQuit = false;
 	}
 
 	private void storeFlags() throws InterruptedException {
 		if (!this.distributeFlags) {
 			return;
 		}
 		byte[] bytes = new byte[32];
 		try {
 			Connection connection = DBConnection.getInstance().getDB();
 			String teamHost;
 			try {
 				PreparedStatement mainPS;
 				ResultSet mainRS;
 				mainPS = connection
 						.prepareStatement("select uid,team_name,team_host from teams where team_name=?");
 				mainPS.setString(1, this.team);
 				mainRS = mainPS.executeQuery();
 				mainRS.next();
 				teamHost = mainRS.getString(3);
 				mainPS.close();
 			} finally {
 				DBConnection.getInstance().returnConnection(connection);
 			}
 
 			// distribute 5 flags per iteration
 			int iters = 5;
 			for (int i = 0; i < iters; i++) {
 				if (!this.noQuit) {
 					return;
 				}
 				try {
 					random.nextBytes(bytes);
 					String flagID = Stuff.toHex(bytes);
 					String flagIDID = flagIDFromFlag(flagID);
 					ReturnCode retCode = null;
 					String verboseMessage = "";
 					ServiceStatus ss = Executor.runTestscript(script,
 							Executor.Action.STORE, teamHost, flagIDID, flagID);
 					verboseMessage = ss.getStatusMessage();
 					retCode = ss.getReturnCode();
 					int hostID = ss.getExecutingHost();
 
					printSREvent(hostID, "=>", this.team, this.name, retCode,
							verboseMessage);

 					if (RETCODE_TIMEOUT.equals(retCode)) {
						reportServiceStatus(this.team, this.name, retCode,
								"Timeout");
						// Do not store any more flags if this attempt
						// timed out
						return;
 					} else {
 						reportAsAcceptingIfCurrentlyNotRunning();
 					}
 
 					if (retCode.getSuccess() != Success.SUCCESS) {
 						reportServiceStatus(this.team, this.name, retCode,
 								verboseMessage);
 					} else {
 						connection = DBConnection.getInstance().getDB();
 						try {
 							/*
 							 * store flag in database
 							 */
 							PreparedStatement ps = connection
 									.prepareStatement("insert into flags(flag_name,flag_collected,flag_team,flag_service,flag_teamhost,flag_disttime) values(?,?,?,?,?,?)");
 							ps.setString(1, flagID);
 							ps.setBoolean(2, false);
 							ps.setString(3, this.team);
 							ps.setString(4, this.name);
 							ps.setString(5, teamHost);
 							ps.setLong(6, (Calendar.getInstance()
 									.getTimeInMillis() / 1000)
 									+ random.nextInt((this.interval / 10) + 1));
 							ps.execute();
 							ps.close();
 						} finally {
 							DBConnection.getInstance().returnConnection(
 									connection);
 							connection = null;
 						}
 					}
 
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void printSREvent(int hostID, String string, String team2,
 			String name2, ReturnCode retCode, String verboseMessage) {
 		String sucflr = retCode.getSuccess().toString();
 		String reason = retCode.getReason().toString();
 		String vm = verboseMessage.replace('\n', ' ');
 		String sep = "|";
 		String padding = "                                                                     ";
 		if (retCode.getSuccess() == Success.SUCCESS) {
 			reason = "";
 			sep = "";
 		}
 		int charsToPad = (CHARSTOPAD - (string.length() + team2.length() + name2
 				.length())) + 3;
 		if (charsToPad < 1) {
 			CHARSTOPAD -= charsToPad;
 			padding = "";
 		} else {
 			padding = padding.substring(0, charsToPad);
 		}
 		System.out.printf(
 				"%02d _[1m%s_[0m%s[_[1m%s_[0m] %s%s [_[1m%s_[0m%s%s]\n"
 						.replace('_', (char) 27), hostID, string, team2, name2,
 				padding, sucflr, reason, sep, vm);
 	}
 
 	private boolean noStatusAvailable(String team, String service)
 			throws SQLException {
 		Connection c = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = c
 					.prepareStatement("select uid from states where status_team=? and status_service=?");
 			ps.setString(1, team);
 			ps.setString(2, service);
 			ResultSet rs = ps.executeQuery();
 			boolean res = rs.next();
 			ps.close();
 			return !res;
 		} finally {
 			DBConnection.getInstance().returnConnection(c);
 		}
 	}
 
 	@Override
 	public void runonce() {
 		if (firstrun) {
 			try {
 				if (noStatusAvailable(this.team, this.name)) {
 					reportServiceStatus(this.team, this.name, ReturnCode
 							.makeReturnCode(Success.FAILURE,
 									ErrorValues.STATUSUNKNOWN),
 							"Please wait some minutes, until the gameserver can "
 									+ "determine the service status");
 				}
 				firstrun = false;
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 		/*
 		 * catch _ANY_ error, and resume work
 		 */
 		try {
 			retrieveFlags();
 			storeFlags();
 		} catch (Throwable t) {
 			System.err.println("-----SERIOUS ERROR HAPPENED AT THREAD "
 					+ Thread.currentThread().getName() + "-----");
 			System.err.println("Resuming operation in 60 seconds");
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			Thread.sleep(startupSleepTime);
 		} catch (Exception e) {
 		}
 		try {
 			if (noStatusAvailable(this.team, this.name)) {
 				reportServiceStatus(this.team, this.name, ReturnCode
 						.makeReturnCode(Success.FAILURE,
 								ErrorValues.STATUSUNKNOWN),
 						"Please wait some minutes, until the gameserver can "
 								+ "determine the service status");
 			}
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 		/*
 		 * do this forever for each service+team
 		 */
 		while (noQuit) {
 			/*
 			 * catch _ANY_ error, and resume work
 			 */
 			try {
 				retrieveFlags();
 				storeFlags();
 
 				Thread.sleep(this.interval * 1000);
 			} catch (InterruptedException e) {
 				System.err.println("Interrupted; terminating");
 				this.noQuit = false;
 				break;
 			} catch (Throwable t) {
 				System.err.println("-----SERIOUS ERROR HAPPENED AT THREAD "
 						+ Thread.currentThread().getName() + "-----");
 				System.err.println("Resuming operation in 60 seconds");
 				try {
 					Thread.sleep(60000);
 				} catch (InterruptedException e) {
 					System.err.println("Interrupted; terminating");
 					this.noQuit = false;
 					break;
 				}
 			}
 		}
 	}
 
 	private void retrieveFlags() throws InterruptedException {
 		int minimalFlagAge = MINIMAL_FLAG_AGE;
 		/*
 		 * this loop is escaped as soon as there are no delivered flags left to
 		 * collect
 		 */
 		while (true) {
 			if (!this.noQuit) {
 				return;
 			}
 			ResultSet mainRS;
 			mainRS = null;
 			PreparedStatement mainPS = null;
 			Connection connection = DBConnection.getInstance().getDB();
 
 			String flagID;
 			String teamID;
 			String teamHost;
 			boolean flagDefended;
 
 			/*
 			 * check if there is a flag
 			 */
 			try {
 				mainPS = connection
 						.prepareStatement("select flag_name,flag_team,flag_teamhost,flag_captured from flags "
 								+ "where flag_collected=false "
 								+ "and flag_service=? and flag_disttime<? "
 								+ "and flag_team=? order by flag_disttime");
 				mainPS.setString(1, this.name);
 				mainPS.setLong(2,
 						(Calendar.getInstance().getTimeInMillis() / 1000)
 								- minimalFlagAge);
 				mainPS.setString(3, this.team);
 				mainRS = mainPS.executeQuery();
 
 				if (!mainRS.next()) {
 					return;
 				}
 				flagID = mainRS.getString(1);
 				teamID = mainRS.getString(2);
 				teamHost = mainRS.getString(3);
 				flagDefended = !mainRS.getBoolean(4);
 			} catch (SQLException e1) {
 				return;
 			} finally {
 				DBConnection.getInstance().returnConnection(connection);
 				connection = null;
 			}
 
 			/*
 			 * flags left; collect the earliest one delivered
 			 */
 			log.logMessage(this, "Trying to get flag %s for team %s", flagID,
 					teamID);
 			ReturnCode retCode = null;
 			String errorMessage = "";
 			String flagIDID = flagIDFromFlag(flagID);
 			int hostID = -1;
 			try {
 				ServiceStatus ss = Executor.runTestscript(script,
 						Action.RETRIEVE, teamHost, flagIDID, flagID);
 				retCode = ss.getReturnCode();
 				errorMessage = ss.getStatusMessage();
 				hostID = ss.getExecutingHost();
 				if (RETCODE_TIMEOUT.equals(retCode)) {
 					reportServiceStatus(teamID, this.name, retCode,
 							errorMessage);
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			printSREvent(hostID, flagDefended ? "<=" : "CF", this.team,
 					this.name, retCode, errorMessage);
 
 			// mark the flag as updated
 			connection = DBConnection.getInstance().getDB();
 			try {
 				PreparedStatement ps;
 				ps = connection
 						.prepareStatement("update flags set flag_collected=true where flag_name=?");
 				ps.setString(1, flagID);
 				ps.execute();
 				ps = connection
 						.prepareStatement("update flags set flag_collectingteam=? where flag_name=?");
 				/*
 				 * Count only if flag was successfully retrieved *and* not
 				 * previously captured
 				 */
 				if ((retCode.getSuccess() == Success.SUCCESS) && flagDefended) {
 					PreparedStatement ps2 = connection
 							.prepareStatement("select team_points_defensive from teams where team_name=?");
 					ps2.setString(1, teamID);
 					ResultSet rs2 = ps2.executeQuery();
 					rs2.next();
 					int points = rs2.getInt(1);
 					PreparedStatement ps3 = connection
 							.prepareStatement("update teams set team_points_defensive=? where team_name=?");
 					ps3.setInt(1, points + 1);
 					ps3.setString(2, teamID);
 					ps3.execute();
 					ps.setString(1, teamID);
 				} else {
 					ps.setString(1, "none");
 				}
 				ps.setString(2, flagID);
 				ps.execute();
 				ps.close();
 
 				reportServiceStatus(teamID, this.name, retCode, errorMessage);
				if (RETCODE_TIMEOUT.equals(retCode)) {
					// Do not check any more flags if the flag retrieval
					// attempt timed out.
					return;
				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} finally {
 				DBConnection.getInstance().returnConnection(connection);
 			}
 		}
 	}
 
 	public String flagIDFromFlag(String flagID) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("SHA1");
 			byte[] res = md.digest(flagID.getBytes("utf-8"));
 			return Stuff.toHex(res).substring(0, 8).toLowerCase();
 		} catch (NoSuchAlgorithmException e) {
 		} catch (UnsupportedEncodingException e) {
 		}
 
 		// emergency solution; should work as well
 		return flagID.substring(0, 8);
 	}
 
 	private void reportAsAcceptingIfCurrentlyNotRunning() throws SQLException {
 		// Connection connection = DBConnection.getInstance().getDB();
 		// try {
 		// PreparedStatement ps = connection
 		// .prepareStatement("select status_text from states where status_team=?
 		// and
 		// status_service=?");
 		// ps.setString(1, this.team);
 		// ps.setString(2, this.name);
 		// ResultSet rs = ps.executeQuery();
 		// if (rs.next()) {
 		// String s = rs.getString(1);
 		// ps.close();
 		// ps = null;
 		// if (s.equals("running")) {
 		// DBConnection.getInstance().returnConnection(connection);
 		// connection = null;
 		// reportServiceStatus(this.team, this.name,
 		// ServiceStatus.ACCEPTINGFLAGS, "");
 		// }
 		// }
 		// if (ps != null) {
 		// ps.close();
 		// }
 		// } finally {
 		// if (connection != null) {
 		// DBConnection.getInstance().returnConnection(connection);
 		// connection = null;
 		// }
 		// }
 	}
 
 	private void reportServiceStatus(String teamID, String service,
 			ReturnCode retCode, String errorDescription) throws SQLException {
 		String error;
 		if (retCode.getSuccess() == Success.SUCCESS) {
 			error = "running";
 		} else {
 			error = retCode.getReason().getDescription();
 		}
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select uid from states where status_team=? and status_service=?");
 			ps.setString(1, teamID);
 			ps.setString(2, service);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				long uid = rs.getLong(1);
 				ps.close();
 				ps = connection
 						.prepareStatement("update states set status_text=? where uid=?");
 				ps.setLong(2, uid);
 				ps.setString(1, error);
 				ps.execute();
 				ps.close();
 				ps = connection
 						.prepareStatement("update states set status_verboseerror=? where uid=?");
 				ps.setString(1, errorDescription);
 				ps.setLong(2, uid);
 				ps.execute();
 				ps.close();
 				ps = connection
 						.prepareStatement("update states set status_color=? where uid=?");
 				ps.setLong(2, uid);
 				ps.setString(1, retCode.getColor());
 				ps.execute();
 				ps.close();
 				ps = connection
 						.prepareStatement("update states set status_updated=? where uid=?");
 				ps.setLong(2, uid);
 				ps.setLong(1, Calendar.getInstance().getTimeInMillis() / 1000);
 				ps.execute();
 				ps.close();
 			} else {
 				ps.close();
 				ps = connection
 						.prepareStatement("insert into states (status_team,status_service,status_text,"
 								+ "status_updated,status_verboseerror,status_color) values (?,?,?,?,?,?)");
 				ps.setString(1, teamID);
 				ps.setString(2, service);
 				ps.setString(3, error);
 				ps.setLong(4, Calendar.getInstance().getTimeInMillis() / 1000);
 				ps.setString(5, errorDescription);
 				ps.setString(6, retCode.getColor());
 				ps.execute();
 				ps.close();
 			}
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getTeam() {
 		return team;
 	}
 
 	public void setDistributeFlags(boolean b) {
 		this.distributeFlags = b;
 	}
 
 	public boolean getDistributeFlags() {
 		return this.distributeFlags;
 	}
 }
