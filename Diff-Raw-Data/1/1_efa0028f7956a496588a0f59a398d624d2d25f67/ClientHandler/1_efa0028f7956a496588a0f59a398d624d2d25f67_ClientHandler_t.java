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
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 
 import net.sourceforge.adela.exceptions.FunctionNotFoundException;
 import net.sourceforge.adela.exceptions.WrongParameterCountException;
 import net.sourceforge.adela.interfaces.IFunctionCallback;
 import net.sourceforge.adela.interpreter.ADELAInterpreter;
 import net.sourceforge.adela.interpreter.ADELAParseTree;
 
 import org.hcesperer.utils.DBConnection;
 import org.hcesperer.utils.SQLConnection;
 import org.hcesperer.utils.djb.DJBSettings;
 
 public class ClientHandler extends Thread implements IFunctionCallback {
 
 	private static Integer MINIMAL_FLAG_AGE = 900;
 
 	private Socket socket;
 
 	private boolean dontQuit = true;
 
 	private FlushingLoggingPrintStream writer = FlushingLoggingPrintStream.STDERRSTREAM;
 
 	private LimitedBufferedReader reader = null;
 
 	private boolean isAdmin = false;
 
 	private int adminTriesLeft = 3;
 
 	private long creationTime;
 
 	private InputStream input;
 
 	private OutputStream output;
 
 	private byte[] ipAddress;
 
 	private int reportedFlags = 0;
 
 	private int calledFunctions = 0;
 
 	private ADELAInterpreter evaluator;
 
 	private HashMap<String, Object> locals;
 
 	private PrintStream logFile;
 
 	private static final String permissionError = DJBSettings.loadString(
 			"control/msgs/permissiondenied", "Requires admin privileges");
 
 	private static final String wrongPWD = DJBSettings.loadString(
 			"control/msgs/passwordincorrect", "Wrong password");
 
 	private static final String scorePrompt = DJBSettings.loadString(
 			"control/msgs/prompt", "scorebot");
 
 	private static final String txtHelp = DJBSettings
 			.loadText(
 					"control/msgs/motd",
 					"Welcome to the CTF scorebot 0.4.37\n"
 							+ "-----------------------------------------------------------------\n"
 							+ "type \"man.reportflag\", \"man.reportadvisory\", "
 							+ "\"man\",\n  \"copyright\" or "
 							+ "\"license\" for more information.");
 
 	private boolean printEvaluationParseTree;
 	static {
 		try {
 			MINIMAL_FLAG_AGE = Integer.valueOf(SQLConnection.getInstance()
 					.getProperty("flagMinimalAge", "900"));
 		} catch (NumberFormatException e) {
 			MINIMAL_FLAG_AGE = 900;
 		}
 		System.out.printf("ClientHandler: flagMinimalAge set to %d seconds.\n",
 				MINIMAL_FLAG_AGE);
 	}
 
 	public ClientHandler(Socket clientSocket) {
 		this.socket = clientSocket;
 		creationTime = Calendar.getInstance().getTimeInMillis() / 1000;
 		ipAddress = clientSocket.getInetAddress().getAddress();
 	}
 
 	public long getCreationTime() {
 		return creationTime;
 	}
 
 	@Override
 	public void run() {
 		try {
 			this.socket.setSoTimeout(60000);
 		} catch (SocketException e2) {
 			e2.printStackTrace();
 			return;
 		}
 		try {
 			this.logFile = new PrintStream("logs/" + "SESS_"
 					+ this.socket.getInetAddress().getHostAddress() + "_"
 					+ System.currentTimeMillis());
 		} catch (FileNotFoundException e) {
 			this.logFile = System.err;
 		}
 		locals = new HashMap<String, Object>();
 		this.evaluator = new ADELAInterpreter();
 		evaluator.SetVariable("quit", "Enter quit() to quit!");
 		evaluator.SetVariable("help", Manual.getInstance());
 		evaluator.SetVariable("man", Manual.getInstance());
 		try {
 			evaluator.SetVariable("license", Manual.getInstance().getPage(
 					"LICENSE"));
 		} catch (IOException e1) {
 			evaluator.SetVariable("license",
 					"Error while reading license file!");
 		}
 		evaluator.AddBuiltinHandlers();
 		evaluator.DeclareClassSafe(TeamHandler.class);
 		evaluator.DeclareClassSafe(ServiceManager.class);
 		evaluator.SetCallback(this);
 		try {
 			input = socket.getInputStream();
 			output = socket.getOutputStream();
 
 			reader = new LimitedBufferedReader(new InputStreamReader(input));
 			reader.setLogFile(logFile);
 			writer = new FlushingLoggingPrintStream(new PrintStream(output),
 					logFile);
 			try {
 				writer.println("Welcome, "
 						+ this.socket.getInetAddress().getHostAddress());
 			} catch (Exception e) {
 				e.printStackTrace(System.err);
 				if (logFile != null) {
 					e.printStackTrace(logFile);
 				}
 				writer.println("Error; giving up");
 				return;
 			}
 
 			evaluator.SetVariable("help", txtHelp);
 			String txtCopyright = Stuff.TEXT_ATTRS_BOLD
 					+ "CTF scorebot 0.4.37\n"
 					+ Stuff.TEXT_ATTRS_OFF
 					+ "(C) 2007-2008, Hans-Christian Esperer\n"
 					+ "echo hcathcespererdotorg | sed 's/at/@/' | sed 's/dot/./'";
 			evaluator.SetVariable("copyright", txtCopyright);
 
 			writer.println(txtHelp);
 			int exceptions = 0;
 			int iterations = 0;
 			// Object lastRetVal = null;
 			while (dontQuit) {
 				try {
 					if (iterations++ > 250) {
 						dontQuit = false;
 						System.out
 								.println("Overflow; terminating client connection");
 					}
 					// String rvDpl = "";
 					// if (lastRetVal != null) {
 					// rvDpl = lastRetVal.toString().split("\n")[0];
 					// }
 					// rvDpl = rvDpl.length() == 0 ? ""
 					// : (rvDpl.length() < 25 ? rvDpl + " " : rvDpl
 					// .substring(0, 22)
 					// + "... ");
 					// if (rvDpl.charAt(0) == '\033') {
 					// rvDpl = "";
 					// }
 					writer.print("\r" + Stuff.beginPrompt() + scorePrompt + " "
 					/* + rvDpl */+ (this.isAdmin ? "#" : ">")
 							+ Stuff.endPrompt() + " ");
 					String line = reader.readLine().trim();
 					if (line.length() != 0) {
 						writer.println();
 						ADELAParseTree tree = ADELAParseTree.Parse(line);
 						if (tree != null) {
 							if (this.printEvaluationParseTree) {
 								writer.println(String.format(
 										"Printing evaluation parsetree:\n%s\n",
 										tree.toStringNonFlat()));
 							}
 							Object result = tree.doEvaluate(locals, evaluator);
 							// lastRetVal = result;
 							if (result == null) {
 							} else {
 								if (result.getClass() != Boolean.class) {
 									writer.println(result.toString());
 								}
 							}
 						} else {
 							writer.println("Syntax error");
 						}
 					}
 					exceptions = 0;
 				} catch (Exception e) {
 					writer.println("Error: " + e.getMessage());
 					if (exceptions++ > 3) {
 						writer.println("Too many exceptions");
 						dontQuit = false;
 					}
 				}
 			}
 			dontQuit = false;
 			writer.println("bye!");
 			cleanup();
 		} catch (IOException e) {
 			e.printStackTrace(System.err);
 			writer.println("ERROR: " + e.getLocalizedMessage());
 		}
 		dontQuit = false;
 	}
 
 	private void cleanup() {
 		try {
 			writer.println("Timeout; bye");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			reader.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			input.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			output.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public Object FunctionCallback(String funcName, Object[] vParams)
 			throws WrongParameterCountException, FunctionNotFoundException {
 		ClientFunctions function;
 
 		if (calledFunctions++ > 128) {
 			writer.println("Too many function calls!");
 			this.dontQuit = false;
 			return null;
 		}
 
 		try {
 			function = ClientFunctions.valueOf(funcName);
 		} catch (Exception e) {
 			throw new FunctionNotFoundException(funcName);
 		}
 
 		if (this.isAdmin) {
 			creationTime = (Calendar.getInstance().getTimeInMillis() / 1000) + 60000 * 30 /*
 																							 * 30
 																							 * minutes
 																							 * timeout
 																							 */;
 		}
 
 		try {
 			switch (function) {
 			case reportflag:
 				if (vParams.length != 2) {
 					throw new WrongParameterCountException(funcName, 2,
 							vParams.length);
 				}
 				return reportFlag((Integer) vParams[0], (String) vParams[1]);
 
 			case help:
 				if (vParams.length != 0) {
 					throw new WrongParameterCountException(funcName, 0,
 							vParams.length);
 				}
 				return Manual.getInstance();
 
 			case quit:
 				doQuit();
 				return false;
 
 			case log:
 				if (this.isAdmin) {
 					throw new FunctionNotFoundException(funcName);
 				}
 				writer.println("log: not allowed");
 				return false;
 
 			case list:
 				if (this.isAdmin) {
 					throw new FunctionNotFoundException(funcName);
 				}
 				writer.println("list: not allowed");
 				return false;
 
 			case array:
 				if (this.isAdmin) {
 					throw new FunctionNotFoundException(funcName);
 				}
 				writer.println("array: not allowed");
 				return false;
 
 			case dict:
 				if (this.isAdmin) {
 					throw new FunctionNotFoundException(funcName);
 				}
 				writer.println("dict: not allowed");
 				return false;
 
 			case readtext:
 				if (vParams.length != 0) {
 					throw new WrongParameterCountException(funcName, 0,
 							vParams.length);
 				}
 				return readText();
 
 			case reportadvisory:
 				if (vParams.length != 2) {
 					throw new WrongParameterCountException(funcName, 2,
 							vParams.length);
 				}
 				return reportAdvisory((Integer) vParams[0], (String) vParams[1]);
 
 			case range:
 				if (this.isAdmin) {
 					throw new FunctionNotFoundException(funcName);
 				}
 				writer.println("not allowed");
 				return false;
 
 			case clearstats:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (clearStatsTables()) {
 					writer.println("Stats tables cleared.");
 				} else {
 					writer.println("Error; couldn't clear stats tables");
 				}
 				return true;
 			case cleardbs:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (clearDBs()) {
 					writer
 							.println("Teams and services deleted; counters reset");
 				} else {
 					writer.println("Error; check the console");
 				}
 			case zeropoints:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (zeroPoints()) {
 					writer.println("All points reset to zero");
 				} else {
 					writer.println("Error; check the console");
 				}
 				return true;
 
 			case deleteflags:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (deleteFlags()) {
 					writer.println("All flags deleted");
 				} else {
 					writer.println("Error; check the console");
 				}
 				return true;
 
 			case admin:
 				if (vParams.length > 1) {
 					throw new WrongParameterCountException(funcName, 1,
 							vParams.length);
 				}
 				if (this.isAdmin) {
 					writer.println("Error: can't drop admin privileges!");
 					this.isAdmin = false;
 					this.evaluator
 							.SetVariableMap(new HashMap<String, Object>());
 					this.evaluator.setAllowArbitraryInstanciiation(false);
 					this.evaluator.setTrusted(false);
 					this.dontQuit = false;
 					return true;
 				}
 
 				String enteredPassword;
 				if (vParams.length == 0) {
 					writer.write(Stuff.noLocalEcho());
 					writer.print("Password: ");
 					reader.munch(3);
 					enteredPassword = reader.readLine();
 					writer.write(Stuff.localEcho());
 					reader.munch(3);
 				} else {
 					enteredPassword = (String) vParams[0];
 				}
 
 				if (enteredPassword.equals(SQLConnection.getInstance()
 						.getProperty("adminPassword", "fooBAR#@!"))) {
 					isAdmin = true;
 					this.evaluator.SetVariable("db", SQLConnection
 							.getInstance());
 					this.evaluator.DeclareClassSafe(SQLConnection.class);
 					writer.println("Login successful!");
 					try {
 						this.socket.setSoTimeout(60000 * 10 /*
 															 * 10 minutes
 															 * timeout
 															 */);
 					} catch (SocketException e2) {
 						e2.printStackTrace();
 					}
 					return true;
 				}
 				isAdmin = false;
 				Thread.sleep(2000);
 				writer.println(wrongPWD);
 
 				if ((adminTriesLeft--) < 0) {
 					this.dontQuit = false;
 				}
 				return false;
 
 			case ladv:
 				if (vParams.length != 0) {
 					throw new WrongParameterCountException(funcName, 0,
 							vParams.length);
 				}
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				listAdvisories();
 				return true;
 
 			case functions:
 				showFunctions();
 				return true;
 
 			case tree:
 				if (vParams.length == 0) {
 					this.printEvaluationParseTree = true;
 				} else {
 					this.printEvaluationParseTree = (Boolean) vParams[0];
 				}
 				return true;
 
 			case reject:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 2) {
 					throw new WrongParameterCountException(funcName, 2,
 							vParams.length);
 				}
 				markAdvisory((Integer) vParams[0], "rejected", 0,
 						(String) vParams[1]);
 				return true;
 			case accept:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 3) {
 					throw new WrongParameterCountException(funcName, 3,
 							vParams.length);
 				}
 				markAdvisory((Integer) vParams[0], "accepted",
 						(Integer) vParams[1], (String) vParams[2]);
 				return true;
 
 			case genflags:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 2) {
 					throw new WrongParameterCountException(funcName, 2,
 							vParams.length);
 				}
 				return genFlags((Integer) vParams[0], (String) vParams[1]);
 
 			case delete:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 1) {
 					throw new WrongParameterCountException(funcName, 3,
 							vParams.length);
 				}
 				deleteAdvisory((Integer) vParams[0]);
 				return true;
 
 			case dechp:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 2) {
 					throw new WrongParameterCountException(funcName, 2,
 							vParams.length);
 				}
 				incHP((Integer) vParams[0], 0 - ((Integer) vParams[1]));
 				return true;
 			case inchp:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 2) {
 					throw new WrongParameterCountException(funcName, 2,
 							vParams.length);
 				}
 				incHP((Integer) vParams[0], (Integer) vParams[1]);
 				return true;
 
 			case createteam:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 1) {
 					throw new WrongParameterCountException(funcName, 1,
 							vParams.length);
 				}
 				return createTeam((String) vParams[0]);
 
 			case getteam:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 1) {
 					throw new WrongParameterCountException(funcName, 1,
 							vParams.length);
 				}
 				return getTeam((Integer) vParams[0]);
 
 			case gc:
 				writer.print("Running garbage collector:");
 				if (!isAdmin) {
 					writer.println("just kidding; requires admin privileges!");
 					return false;
 				}
 				Runtime.getRuntime().gc();
 				writer.println("done.");
 				return true;
 
 			case createservice:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 1) {
 					throw new WrongParameterCountException(funcName, 1,
 							vParams.length);
 				}
 				return createService((String) vParams[0]);
 
 			case getservice:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				if (vParams.length != 1) {
 					throw new WrongParameterCountException(funcName, 1,
 							vParams.length);
 				}
 				return getService((Integer) vParams[0]);
 
 			case listteams:
 			case lt:
 				return listTeams();
 
 			case listservices:
 			case ls:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 				return listServices();
 
 			case getclass:
 				if (!isAdmin) {
 					writer.println(permissionError);
 					return false;
 				}
 
 				if (!SQLConnection.getInstance().getProperty("GetClass",
 						"disabled").equals("enabled")) {
 					writer
 							.println("getclass is disabled. You can enable it in the 'settings' file");
 					return false;
 				}
 
 				if (vParams.length != 1) {
 					throw new WrongParameterCountException(funcName, 1,
 							vParams.length);
 				}
 				this.evaluator.setAllowArbitraryInstanciiation(true);
 				this.evaluator.setTrusted(true);
 				Class cls = Class.forName((String) vParams[0]);
 				if (cls != null) {
 					this.evaluator.SetVariable(cls.getSimpleName(), cls);
 					writer.println("Imported " + cls.getSimpleName());
 					return true;
 				} else {
 					writer.println("Error: cannot import class "
 							+ (String) vParams[0] + "!");
 					return false;
 				}
 
 			case dir:
 				for (int j = 0; j < 2; j++) {
 
 					String[] keys = null;
 					if (j == 0) {
 						keys = this.evaluator.GetVariables();
 					} else if (j == 1) {
 						keys = new String[this.locals.keySet().size()];
 						Iterator<String> itr = this.locals.keySet().iterator();
 						int k = 0;
 						while (itr.hasNext()) {
 							keys[k++] = itr.next();
 						}
 					}
 					for (int i = 0; i < keys.length; i++) {
 						Object obj = null;
 						if (j == 0) {
 							obj = this.evaluator.GetVariable(keys[i]);
 						} else if (j == 1) {
 							obj = this.locals.get(keys[i]);
 						}
 						String desc = "null";
 						if (obj != null) {
 							if (obj != null) {
 								desc = obj.toString();
 							}
 						}
 						int pos = desc.indexOf("\n");
 						if (pos != -1) {
 							desc = desc.substring(0, pos - 1);
 						}
 						if (desc.length() > 40) {
 							desc = desc.substring(0, 40) + "...";
 						}
 						writer.format("%s\t%s\t= %s%s\n", obj == null ? ""
 								: obj.getClass().getSimpleName(), keys[i],
 								desc, Stuff.TEXT_ATTRS_OFF);
 					}
 				}
 				return true;
 			}
 		} catch (Exception e) {
 			writer.println("Error: " + e.getMessage());
 		}
 		return null;
 	}
 
 	private boolean deleteFlags() {
 		Connection c = DBConnection.getInstance().getDB();
 		try {
 			Statement s = c.createStatement();
 			try {
 				s.execute("delete from flags");
 				return true;
 			} finally {
 				s.close();
 			}
 		} catch (Throwable t) {
 			t.printStackTrace();
 			return false;
 		} finally {
 			DBConnection.getInstance().returnConnection(c);
 		}
 	}
 
 	private boolean zeroPoints() {
 		Connection c = DBConnection.getInstance().getDB();
 		try {
 			Statement s = c.createStatement();
 			try {
 				s.execute("update teams set team_points_offensive=0");
 				s.execute("update teams set team_points_defensive=0");
 				s.execute("update teams set team_points_advisories=0");
 				s.execute("update teams set team_points_hacking=0");
 				return true;
 			} finally {
 				s.close();
 			}
 		} catch (Throwable t) {
 			t.printStackTrace();
 			return false;
 		} finally {
 			DBConnection.getInstance().returnConnection(c);
 		}
 	}
 
 	private boolean clearStatsTables() {
 		Connection c = DBConnection.getInstance().getDB();
 		try {
 			Statement s = c.createStatement();
 			try {
 				s.execute("delete from stats_points");
 				s.execute("delete from stats_services");
 				s.execute("delete from stats_times");
 				return true;
 			} finally {
 				s.close();
 			}
 		} catch (Throwable t) {
 			t.printStackTrace();
 			return false;
 		} finally {
 			DBConnection.getInstance().returnConnection(c);
 		}
 	}
 
 	private boolean clearDBs() throws SQLException {
 		if (!clearStatsTables()) {
 			return false;
 		}
 		Connection c = DBConnection.getInstance().getDB();
 		try {
 			Statement s = c.createStatement();
 			try {
 				s.execute("delete from teams");
 				s.execute("delete from services");
 				s.execute("alter sequence teams_uid_seq restart 1");
 				s.execute("alter sequence services_uid_seq restart 1");
 				s.execute("delete from states");
 			} finally {
 				s.close();
 			}
 			return true;
 		} finally {
 			DBConnection.getInstance().returnConnection(c);
 		}
 	}
 
 	private boolean listServices() {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select service_name,service_script,service_check_interval,uid from services order by uid");
 			ResultSet rs = ps.executeQuery();
 			writer.println("ID\tname\tscript\tinterval");
 			while (rs.next()) {
 				writer.format("%d\t%s\t%s\t%s\n", rs.getInt(4),
 						rs.getString(1), rs.getString(2), rs.getString(3));
 			}
 			ps.close();
 			return false;
 		} catch (SQLException e) {
 			writer.printError(e);
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 		return false;
 	}
 
 	private ServiceManager createService(String serviceName) {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("insert into services(service_name,service_script_type) values(?,'binary')");
 			ps.setString(1, serviceName);
 			ps.execute();
 			ps = connection
 					.prepareStatement("select uid from services where service_name=?");
 			ps.setString(1, serviceName);
 			ResultSet rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Error: couldn't create service " + serviceName
 						+ "!");
 				ps.close();
 				return null;
 			}
 			int res = rs.getInt(1);
 			ps.close();
 			return new ServiceManager(writer, res);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			writer.println("Error: " + e.toString());
 			return null;
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private ServiceManager getService(Integer serviceID) {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select uid from services where uid=?");
 			ps.setInt(1, serviceID);
 			ResultSet rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Error: service #" + serviceID
 						+ " doesn't exist!");
 				return null;
 			}
 			ps.close();
 			return new ServiceManager(writer, serviceID);
 		} catch (Exception e) {
 			writer.println("Error: " + e.toString());
 			return null;
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private TeamHandler getTeam(Integer teamID) {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select uid from teams where uid=?");
 			ps.setInt(1, teamID);
 			ResultSet rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Error: team #" + teamID + " doesn't exist!");
 				return null;
 			}
 			ps.close();
 			return new TeamHandler(writer, teamID);
 		} catch (Exception e) {
 			writer.println("Error: " + e.toString());
 			return null;
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private boolean listTeams() {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select team_points_offensive,team_points_defensive,team_points_advisories,team_points_hacking,team_name,uid,team_host from teams order by uid");
 			ResultSet rs = ps.executeQuery();
 			writer.println("ID\tTeam\toff\tdef\tadv\thak\thost");
 			writer
 					.println("-----------------------------------------------------------------");
 			while (rs.next()) {
 				String tName = rs.getString(5);
 				String tHost = rs.getString(7);
 				int tID = rs.getInt(6);
 				int[] points = new int[4];
 				if (this.isAdmin) {
 					for (int i = 0; i < 4; i++) {
 						points[i] = rs.getInt(i + 1);
 					}
 					writer.format("%d\t%s\t%d\t%d\t%d\t%d\t%s\n", tID, tName
 							.substring(0, (tName.length() < 6) ? tName.length()
 									: 6), points[0], points[1], points[2],
 							points[3], tHost);
 				} else {
 					writer.format("%d\t%s\tn/a\tn/a\tn/a\tn/a\t%s\n", tID,
 							tName.substring(0, (tName.length() < 6) ? tName
 									.length() : 6), tHost);
 				}
 			}
 			ps.close();
 			return true;
 		} catch (SQLException e) {
 			writer.println("Error: " + e);
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 		return false;
 	}
 
 	private TeamHandler createTeam(String teamName) {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("insert into teams(team_name) values(?)");
 			ps.setString(1, teamName);
 			ps.execute();
 			ps = connection
 					.prepareStatement("select uid from teams where team_name=?");
 			ps.setString(1, teamName);
 			ResultSet rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Error: couldn't create team " + teamName + "!");
 				ps.close();
 				return null;
 			}
 			int result = rs.getInt(1);
 			ps.close();
 			return new TeamHandler(writer, result);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			writer.println("Error: " + e.toString());
 			return null;
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private void incHP(int teamID, int i) {
 		PreparedStatement ps;
 		ResultSet rs;
 
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			ps = connection
 					.prepareStatement("update teams set team_points_hacking=team_points_hacking+? where uid=?");
 			ps.setInt(1, i);
 			ps.setInt(2, teamID);
 			ps.execute();
 			ps = connection
 					.prepareStatement("select team_points_hacking from teams where uid=?");
 			ps.setInt(1, teamID);
 			rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Error: team " + teamID + " doesn't exist!");
 			} else {
 				writer.format("Team #%d now has %d hacking points\n", teamID,
 						rs.getInt(1));
 			}
 			ps.close();
 		} catch (SQLException e) {
 			writer.printError(e);
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private void deleteAdvisory(int advID) {
 		PreparedStatement ps;
 		ResultSet rs;
 
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			ps = connection
 					.prepareStatement("select advisory_description from advisories where uid=?");
 			ps.setInt(1, advID);
 			rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Error: advisory #" + advID + " doesn't exist!");
 			} else {
 				ps = connection
 						.prepareStatement("delete from advisories where uid=?");
 				ps.setInt(1, advID);
 				ps.execute();
 				writer.println("Advisory deleted");
 				ps.close();
 			}
 		} catch (SQLException e) {
 			writer.printError(e);
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private boolean genFlags(Integer numFlags, String separator) {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			int nfmo = numFlags - 1;
 			for (int i = 0; i < numFlags; i++) {
 				byte[] bytes = new byte[32];
 				Random random = new Random();
 				random.nextBytes(bytes);
 				String flagID = Stuff.toHex(bytes);
 				PreparedStatement ps = connection
 						.prepareStatement("insert into flags(flag_name,flag_collected,flag_team,"
 								+ "flag_service,flag_teamhost,flag_disttime) values (?,?,?,?,?,?)");
 				ps.setString(1, flagID);
 				ps.setBoolean(2, false);
 				ps.setString(3, "WoD");
 				ps.setString(4, "adlsmtp");
 				ps.setString(5, "localhost");
 				ps.setLong(6, Calendar.getInstance().getTimeInMillis() / 1000);
 				ps.execute();
 				ps.close();
 				// hack
 				if (i == nfmo) {
 					separator = "";
 				}
 				writer.print(flagID + separator);
 			}
 			writer.println();
 			return true;
 		} catch (SQLException e) {
 			writer.printError(e);
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 		return false;
 	}
 
 	private void markAdvisory(Integer advID, String status, int points,
 			String note) {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select advisory_team,advisory_status from advisories where uid=?");
 			ps.setInt(1, advID);
 			ResultSet rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Advisory " + advID + " doesn't exist!");
 				return;
 			}
 			if (!(rs.getString(2).equals("pending"))) {
 				writer.println("Advisory " + advID + " was already rated!");
 				return;
 			}
 			String teamName = rs.getString(1);
 			connection.prepareStatement("begin").execute();
 			ps = connection
 					.prepareStatement("update advisories set advisory_status=? where uid=?");
 			ps.setString(1, status);
 			ps.setInt(2, advID);
 			ps.execute();
 			ps.close();
 			ps = connection
 					.prepareStatement("update advisories set advisory_comment=? where uid=?");
 			ps.setString(1, "[" + points + "] " + note);
 			ps.setInt(2, advID);
 			ps.execute();
 			ps.close();
 			ps = connection
 					.prepareStatement("select team_points_advisories from teams where team_name=?");
 			ps.setString(1, teamName);
 			rs = ps.executeQuery();
 			rs.next();
 			int tmp_val = rs.getInt(1);
 			ps.close();
 			ps = connection
 					.prepareStatement("update teams set team_points_advisories=? where team_name=?");
 			ps.setInt(1, tmp_val + points);
 			ps.setString(2, teamName);
 			ps.execute();
 			ps.close();
 			connection.prepareStatement("commit").execute();
 			writer.println("ok");
 		} catch (Exception e) {
 			writer.printError(e);
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private void listAdvisories() {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select uid,advisory_team,advisory_description from advisories where advisory_status='pending' order by advisory_time");
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				String desc = rs.getString(3);
 				if (desc == null) {
 					desc = "";
 				}
 				writer.println(rs.getLong(1) + ": [" + rs.getString(2) + "]  "
 						+ desc.replace('\n', ' '));
 			}
 			ps.close();
 		} catch (Exception e) {
 			writer.printError(e);
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 	}
 
 	private boolean reportAdvisory(Integer teamID, String advisory) {
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			String teamName;
 			PreparedStatement ps = connection
 					.prepareStatement("select team_name from teams where uid=?");
 			ps.setInt(1, teamID);
 			ResultSet rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Team # " + teamID + " does not exist!");
 				ps.close();
 				return false;
 			}
 			teamName = rs.getString(1);
 			ps.close();
 			ps = connection
 					.prepareStatement("insert into advisories (advisory_team,advisory_description,advisory_time,advisory_status,advisory_from) values (?,?,?,?,?)");
 			ps.setString(1, teamName);
 			ps.setString(2, advisory);
 			ps.setLong(3, Calendar.getInstance().getTimeInMillis() / 1000);
 			ps.setString(4, "pending");
 			ps.setString(5, this.socket.getInetAddress().toString());
 			ps.execute();
 			ps.close();
 			writer.println("Your advisory has been reported.");
 			return true;
 		} catch (SQLException e) {
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 
 		return false;
 	}
 
 	private Object readText() {
 		StringBuilder string = new StringBuilder();
 		int lines = 0;
 
 		writer.printf("Max 100 lines; max %d chars per line\n",
 				LimitedBufferedReader.getLineLength());
 		writer
 				.println("Write some text, finish with a single dot on a separate line (^\\.$)");
 
 		String line;
 		while (true) {
 			try {
 				writer.print(lines + "> ");
 				line = reader.readLine();
 			} catch (IOException e) {
 				return e;
 			}
 			if (line.equals(".")) {
 				break;
 			}
 			string.append(line);
 			string.append('\n');
 			if (lines++ > 100) {
 				break;
 			}
 		}
 
 		return string.toString();
 	}
 
 	private boolean doQuit() {
 		dontQuit = false;
 		try {
 			this.socket.setSoTimeout(10);
 			this.socket.setSoLinger(false, 0);
 			this.socket.close();
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	private void showFunctions() {
 		ClientFunctions[] functions = ClientFunctions.values();
 		writer.println("Available functions");
 		writer
 				.println("-----------------------------------------------------------------");
 		for (int i = 0; i < functions.length; i++) {
 			writer.println(functions[i].toString());
 		}
 	}
 
 	private boolean reportFlag(Integer integer, String string) {
 		/*
 		 * To make accidental DoS attacks by teams scripting flag reporting
 		 * harder, we limit the number of flags reported per session to 100.
 		 * After that, the connection is terminated. Re-connecting can take up
 		 * to 5 seconds.
 		 */
 		if (reportedFlags++ > 100) {
 			writer.println("Too many flags!");
 			this.dontQuit = false;
 			return false;
 		}
 
 		Connection connection = DBConnection.getInstance().getDB();
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select flag_name,flag_team,flag_service,flag_collected,flag_disttime from flags where flag_name=?");
 			ps.setString(1, string);
 			ResultSet rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Flag \"" + string + "\" does not exist!");
 				ps.close();
 				return false;
 			}
 			String flagName = rs.getString(1);
 			String flagTeam = rs.getString(2);
 			String flagService = rs.getString(3);
 			boolean flagCollected = rs.getBoolean(4);
 			long flagDisttime = rs.getLong(5);
 			ps.close();
 
 			/*
 			 * If flagCollected is false, we check if the flag is too old
 			 */
 			if (!flagCollected) {
 				if (flagDisttime < ((System.currentTimeMillis() / 1000) - MINIMAL_FLAG_AGE)) {
 					flagCollected = true;
 				}
 			}
 
 			/*
 			 * If flagCollected is true, an attempt to retrieve the flag from
 			 * the originating service has been made. (Whether successfully or
 			 * not does not matter)
 			 */
 			if (flagCollected) {
 				writer.println("This flag is not valid anymore!");
 				return false;
 			}
 
 			/*
 			 * Check whether the supposed reporting team does exist at all.
 			 */
 			ps = connection
 					.prepareStatement("select team_name,team_points_offensive from teams where uid=?");
 			ps.setInt(1, integer);
 			rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Team ID " + integer + " invalid!");
 				ps.close();
 				return false;
 			}
 
 			/*
 			 * The reporting team does exist, so get its name and current
 			 * offensive scores.
 			 */
 			String teamName = rs.getString(1);
 			int teamOffensivePoints = rs.getInt(2);
 			ps.close();
 
 			/*
 			 * Alright. Now we check if the team has the service running
 			 * successfully itself. If not, bail out
 			 */
 			ps = connection
 					.prepareStatement("select status_text from states where status_team=? and status_service=?");
 			ps.setString(1, teamName);
 			ps.setString(2, flagService);
 			rs = ps.executeQuery();
 			if (!rs.next()) {
 				writer.println("Errm, sorry. Wait a couple of minutes "
 						+ "and try again. Internal error code #1842.");
 				return false;
 			}
 			String ownServiceStatus = rs.getString(1);
 			ps.close();
 			if (!"running".equals(ownServiceStatus)) {
 				writer
 						.println("Sorry, you do not have that service running yourself, "
 								+ "or your version of the service is broken.");
 				return false;
 			}
 
 			/*
 			 * Teams may not report flags from their own services.
 			 */
 			if (teamName.equals(flagTeam)) {
 				writer.println("You cannot report your own flags!");
 				return false;
 			}
 
 			/*
 			 * Mark the reporting of a flag. Because multiple teams can report
 			 * the same flag, but each team can report a flag only once, we have
 			 * to store the reportings in a separate database.
 			 * 
 			 * Also, that database can be used to generate statistical data.
 			 * Hence, we also store the service the flag is originating from.
 			 */
 			boolean canReport = markReporting(connection, flagName, flagTeam,
 					flagService, teamName);
 
 			if (!canReport) {
 				writer.println("Sorry, you cannot report a flag twice.");
 				return false;
 			}
 
 			/*
 			 * Mark the flag as captured. This doesn't prevent other teams from
 			 * reporting it, but prevents the gameserver to award defensive
 			 * points to the originating team.
 			 */
 			ps = connection
 					.prepareStatement("update flags set flag_captured=true where flag_name=?");
 			ps.setString(1, flagName);
 			if (ps.executeUpdate() != 1) {
 				writer.println("An error occured and has been logged.");
 				ps.close();
 				System.out.println("WTF!? A flag that existed a "
 						+ "moment ago seems to have vanished (or maybe even"
 						+ " was duplicated, meaning constraints are gone)... "
 						+ "Check your hdds with smartmontools ;-)");
 				return false;
 			}
 			ps.close();
 
 			/*
 			 * Finally, success.
 			 * 
 			 * Well, almost. Store it in the database first, then report the
 			 * success, not the other way round. Makes DJB happy ;-)
 			 */
 			ps = connection
 					.prepareStatement("update teams set team_points_offensive=team_points_offensive+1 where uid=?");
 			ps.setInt(1, integer);
 			if (ps.executeUpdate() != 1) {
 				writer
 						.println("An error has occured and logged. This is bad."
 								+ " Sorry for the inconvenience. Hint to organizers: set "
 								+ "up the database properly next time ;-)");
 				ps.close();
 				return false;
 			}
 			ps.close();
 
 			/*
 			 * All set. We can report success.
 			 */
 			writer.printf("You successfully reported a flag for "
 					+ "service %s from team %s.\nYou now have"
 					+ " %d offensive points.\n", flagService, flagTeam,
 					teamOffensivePoints + 1);
 
 			return true;
 		} catch (SQLException e) {
 			writer.println("An error has occured and logged."
 					+ " We apologize for the inconvenience.");
 			e.printStackTrace();
 		} finally {
 			DBConnection.getInstance().returnConnection(connection);
 		}
 		return false;
 	}
 
 	/**
 	 * Check whether a team has already reported a certain flag. If not, the
 	 * reporting is logged, so that afterwards statistics can be built.
 	 * 
 	 * Warning: This function does not check whether flagTeam.equals(teamName).
 	 * You must do this yourself, otherwise, teams can report their own flags.
 	 * 
 	 * Also note that while it does not appear so, this function is indeed
 	 * thread safe. ;-)
 	 * 
 	 * @param connection
 	 *            SQL connection to be used
 	 * @param flagName
 	 *            the flag
 	 * @param flagTeam
 	 *            team the flag belongs to (i.e., the attacked team)
 	 * @param flagService
 	 *            service the flag belongs to (for statistics)
 	 * @param teamName
 	 *            name of the reporting team
 	 * @return true if flag has *not* been reported yet by the specified team,
 	 *         otherwise false.
 	 */
 	private static boolean markReporting(Connection connection,
 			String flagName, String flagTeam, String flagService,
 			String teamName) {
 		try {
 			PreparedStatement ps = connection
 					.prepareStatement("select uid from flagstats where flag_name=? and flag_collectingteam=?");
 			ps.setString(1, flagName);
 			ps.setString(2, teamName);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				ps.close();
 				return false;
 			}
 			ps.close();
 			ps = connection
 					.prepareStatement("insert into flagstats (flag_name,flag_fromteam,flag_collectingteam,flag_service) values (?,?,?,?)");
 			ps.setString(1, flagName);
 			ps.setString(2, flagTeam);
 			ps.setString(3, teamName);
 			ps.setString(4, flagService);
 			ps.execute();
            ps.close();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean doStop() {
 		return doQuit();
 	}
 
 	public ComparableBA getIP() {
 		return new ComparableBA(ipAddress);
 	}
 
 	public boolean isQuit() {
 		return !dontQuit;
 	}
 
 	@Override
 	public String toString() {
 		return this.socket.getInetAddress().toString();
 	}
 }
