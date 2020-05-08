 package org.freecode.irc.votebot;
 
 import org.freecode.irc.*;
 import org.freecode.irc.event.CtcpRequestListener;
 import org.freecode.irc.event.NumericListener;
 import org.freecode.irc.event.PrivateMessageListener;
 
 import java.io.*;
 import java.lang.management.ManagementFactory;
 import java.sql.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Locale;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: Shivam
  * Date: 17/06/13
  * Time: 00:05
  */
 public class FreeVoteBot implements PrivateMessageListener {
 
 	private static final String CHANNEL = "#freecode";
 	private String nick, realName, serverHost, user;
 	private int port;
 	private IrcConnection connection;
 	private Connection dbConn;
 	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
 
 	static {
 		SDF.setTimeZone(TimeZone.getTimeZone("Europe/London"));
 	}
 
 	public FreeVoteBot(String nick, String user, String realName, String serverHost, String[] chans, int port) {
 		try {
 			connection = new IrcConnection(serverHost, port);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			Class.forName("org.sqlite.JDBC");
 			dbConn = DriverManager.getConnection("jdbc:sqlite:freecode.db");
 			Statement statement = dbConn.createStatement();
 			statement.setQueryTimeout(5);
 			statement.executeUpdate("CREATE TABLE IF NOT EXISTS polls (id integer PRIMARY KEY AUTOINCREMENT, question string NOT NULL, options string NOT NULL DEFAULT 'yes,no,abstain', closed BOOLEAN DEFAULT 0, expiry INTEGER DEFAULT 0, creator STRING DEFAULT 'null')");
 			statement.executeUpdate("CREATE TABLE IF NOT EXISTS votes (pollId integer, voter string NOT NULL, answerIndex integer NOT NULL)");
 
 		} catch (ClassNotFoundException | SQLException e) {
 			e.printStackTrace();
 		}
 		this.nick = nick;
 		this.realName = realName;
 		this.serverHost = serverHost;
 		this.port = port;
 		this.user = user;
 		NumericListener nickInUse = new NumericListener() {
 			public int getNumeric() {
 				return IrcConnection.ERR_NICKNAMEINUSE;
 			}
 
 			public void execute(String rawLine) {
 				FreeVoteBot.this.nick = FreeVoteBot.this.nick + "_";
 				try {
 					connection.sendRaw("NICK " + FreeVoteBot.this.nick);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		connection.addListener(nickInUse);
 		try {
 			connection.register(nick, user, realName);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		//connection.removeListener(nickInUse);
 		connection.addListener(this);
 		connection.addListener(new CtcpRequestListener() {
 
 			public void onCtcpRequest(CtcpRequest request) {
 				if (request.getCommand().equals("VERSION")) {
 					request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
 							request.getNick(), "VERSION", "FreeVoteBot by " + CHANNEL + " on irc.rizon.net"));
 				} else if (request.getCommand().equals("PING")) {
 					request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
 							request.getNick(), "PING", request.getArguments()));
 				}
 			}
 		});
 		File pass = new File("password.txt");
 		if (pass.exists()) {
 			try {
 				BufferedReader read = new BufferedReader(new FileReader(pass));
 				String s = read.readLine();
 				if (s != null) {
 					connection.send(new Privmsg("NickServ", "identify " + s, connection));
 				}
 				read.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		for (String channel : chans) {
 			connection.joinChannel(channel);
 		}
 	}
 
 	public static void main(String[] args) {
 		String nick = null, user = null, realName = null, serverHost = null;
 		int port = 6667;
 		String[] chans = new String[]{CHANNEL};
 		if (args.length % 2 == 0 && args.length > 0) {
 			for (int i = 0; i < args.length; i++) {
 				String arg = args[i];
 				String nextArg = args[++i];
 				if (arg.equalsIgnoreCase("--nick") || arg.equalsIgnoreCase("-n")) {
 					nick = nextArg;
 				} else if (arg.equalsIgnoreCase("--user") || arg.equalsIgnoreCase("-u")) {
 					user = nextArg;
 				} else if (arg.equalsIgnoreCase("--realname") || arg.equalsIgnoreCase("-r")) {
 					realName = nextArg;
 				} else if (arg.equalsIgnoreCase("--host") || arg.equalsIgnoreCase("-h")) {
 					serverHost = nextArg;
 				} else if (arg.equalsIgnoreCase("--port") || arg.equalsIgnoreCase("-p")) {
 					try {
 						port = Integer.parseInt(nextArg);
 					} catch (NumberFormatException e) {
 						System.out.println("Failed to parse port: " + nextArg);
 						System.out.println("Using default port: " + port);
 						port = 6667;
 					}
 				} else if (arg.equalsIgnoreCase("--channels") || arg.equalsIgnoreCase("-c")) {
 					chans = nextArg.split(",");
 				}
 			}
 			new FreeVoteBot(nick, user, realName, serverHost, chans, port);
 		} else {
 			System.out.println("Incorrect argument count, exiting.");
 			System.out.println("Usage: java FreeVoteBot -n nick -u user -r realname -h host -p port -c #channel,#list");
 			System.exit(1);
 		}
 
 	}
 
 	private void vote(final int nId, final int id, final Privmsg privmsg) {
 
 		privmsg.getIrcConnection().addListener(new NoticeFilter() {
 			public boolean accept(Notice notice) {
 				if (notice.getNick().equals("ChanServ") && notice.getMessage().equals("Permission denied.")) {
 					notice.getIrcConnection().removeListener(this);
 					return false;
 				}
 				return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains(privmsg.getNick());
 			}
 
 			public void run(Notice notice) {
 				try {
 					String mainNick = notice.getMessage().substring(notice.getMessage().indexOf("Main nick:") + 10).trim();
 					System.out.println(mainNick);
 					ResultSet rs;
 					PreparedStatement statement;
 					statement = dbConn.prepareStatement("SELECT * FROM polls WHERE id = ? AND closed = 0");
 					statement.setInt(1, id);
 					rs = statement.executeQuery();
 					if (rs.next()) {
 						long time = rs.getLong("expiry");
 						if (System.currentTimeMillis() < time) {
 							statement = dbConn.prepareStatement("SELECT * FROM votes WHERE voter = ? AND pollId = ?");
 							statement.setString(1, mainNick);
 							statement.setInt(2, id);
 							rs = statement.executeQuery();
 							if (rs.next()) {
 								if (rs.getInt("answerIndex") == nId) {
 									privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "You've already voted with this option!", privmsg.getIrcConnection()));
 								} else {
 									PreparedStatement stmt = dbConn.prepareStatement("UPDATE votes SET answerIndex = ? WHERE voter = ? AND pollId = ?");
 									stmt.setInt(1, nId);
 									stmt.setString(2, mainNick);
 									stmt.setInt(3, id);
 									stmt.execute();
 									privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Vote updated.", privmsg.getIrcConnection()));
 								}
 							} else {
 								PreparedStatement stmt = dbConn.prepareStatement("INSERT INTO votes(pollId,voter,answerIndex) VALUES (?,?,?)");
 								stmt.setInt(1, id);
 								stmt.setString(2, mainNick);
 								stmt.setInt(3, nId);
 								stmt.execute();
 								privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Vote cast.", privmsg.getIrcConnection()));
 
 							}
 						} else {
 							privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Voting is closed for this poll.", privmsg.getIrcConnection()));
 
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				privmsg.getIrcConnection().removeListener(this);
 			}
 		});
 		privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + CHANNEL + " " + privmsg.getNick(), privmsg.getIrcConnection()));
 
 	}
 
 	private static String getProcessId(final String fallback) {
 		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
 		final int index = jvmName.indexOf('@');
 		if (index < 1) {
 			return fallback;
 		}
 		try {
 			return Long.toString(Long.parseLong(jvmName.substring(0, index)));
 		} catch (NumberFormatException e) {
 		}
 		return fallback;
 	}
 
 	public void onPrivmsg(final Privmsg privmsg) {
 		if (privmsg.getMessage().toLowerCase().startsWith("!createpoll")) {
 			long txp = 604800 * 1000;
 			final String msg;
 			if (privmsg.getMessage().matches("!createpoll \\d{1,6}[whsdmWHSDM]? .+")) {
 				final String[] parts = privmsg.getMessage().split(" ", 3);
 				try {
 					txp = parseExpiry(parts[1]);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				msg = parts[2];
 			} else {
 				final String[] parts = privmsg.getMessage().split(" ", 2);
 				msg = parts[1];
 			}
 			final long exp = System.currentTimeMillis() + txp;
 			if (msg.isEmpty() || msg.length() < 5) {
 				privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Question is too short.", privmsg.getIrcConnection()));
 				return;
 			}
 			privmsg.getIrcConnection().addListener(new NoticeFilter() {
 				public boolean accept(Notice notice) {
 					Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
 					Matcher matcher = pattern.matcher(notice.getMessage());
 					if (matcher.find() && matcher.find()) {
 						String access = matcher.group(1);
 						System.out.println(access);
 						if (access.equals("AOP") || access.equals("Founder") || access.equals("SOP")) {
 							return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getNick() + "\u0002");
 						}
 					}
 					if (notice.getMessage().equals("Permission denied."))
 						notice.getIrcConnection().removeListener(this);
 					return false;
 				}
 
 				public void run(Notice notice) {
 					try {
 						String mainNick = notice.getMessage().substring(notice.getMessage().indexOf("Main nick:") + 10).trim();
 						PreparedStatement statement = dbConn.prepareStatement("INSERT INTO polls(question, expiry, creator) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
 						statement.setString(1, msg.trim());
 						statement.setLong(2, exp);
 						statement.setString(3, mainNick);
 						statement.execute();
 						ResultSet rs = statement.getGeneratedKeys();
 						if (rs.next()) {
 							int id = rs.getInt(1);
 							privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Created poll, type !vote " + id + " yes/no/abstain to vote.", privmsg.getIrcConnection()));
 						}
 						privmsg.getIrcConnection().removeListener(this);
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + CHANNEL + " " + privmsg.getNick(), privmsg.getIrcConnection()));
 
 		} else if (privmsg.getMessage().toLowerCase().startsWith("!v ") || privmsg.getMessage().toLowerCase().startsWith("!vote ")) {
 			final String msg = privmsg.getMessage().substring(privmsg.getMessage().indexOf(' ')).trim();
 			System.out.println(msg);
 			final String[] split = msg.split(" ", 2);
 			if (split.length == 2) {
 				String ids = split[0];
 				String vote = split[1].toLowerCase();
 				if (!vote.equalsIgnoreCase("yes") && !vote.equalsIgnoreCase("no") && !vote.equalsIgnoreCase("abstain")) {
 					return;
 				}
 				final int nId;
 				if (vote.equalsIgnoreCase("yes")) {
 					nId = 0;
 				} else if (vote.equalsIgnoreCase("no")) {
 					nId = 1;
 				} else {
 					nId = 2;
 				}
 				if (!ids.matches("\\d+")) {
 					return;
 				}
 				final int id = Integer.parseInt(ids);
 				vote(nId, id, privmsg);
 			} else if (split.length == 1) {
 				String id = split[0];
 				if (!id.matches("\\d+")) {
 					return;
 				}
 				try {
 					ResultSet rs;
 					PreparedStatement statement;
 					statement = dbConn.prepareStatement("SELECT * FROM polls WHERE id = ?");
 					statement.setInt(1, Integer.parseInt(id));
 					rs = statement.executeQuery();
 					String question = null;
 					String[] options = null;
 					String expiry = null;
 					String closed = null;
 					if (rs.next()) {
 						question = rs.getString("question");
 						options = stringToArray(rs.getString("options"));
 						long exp = rs.getLong("expiry");
 						expiry = SDF.format(new Date(exp));
 						closed = rs.getBoolean("closed") ? "Closed" : "Open";
						if (System.currentTimeMillis() < exp) {
 							closed = "Expired";
 						}
 					}
 					if (question != null) {
 						statement = dbConn.prepareStatement("SELECT * FROM votes WHERE pollId = ?");
 						statement.setInt(1, Integer.parseInt(id));
 						rs = statement.executeQuery();
 						int yes = 0, no = 0, abstain = 0;
 						while (rs.next()) {
 							int i = rs.getInt("answerIndex");
 							if (i == 0) {
 								yes++;
 							} else if (i == 1) {
 								no++;
 							} else if (i == 2) {
 								abstain++;
 							}
 						}
 						privmsg.send("Poll #" + id + ": " + question +
 								" Options: " + Arrays.toString(options) + " Yes: " + yes + " No: " + no + " Abstain: "
 								+ abstain + " Ends: " + expiry + " Status: " + closed);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 		} else if (privmsg.getMessage().toLowerCase().startsWith("!msg ") && privmsg.getNick().equals("Speed")) {
 			String msg = privmsg.getMessage().substring(4).trim();
 			String[] split = msg.split(" ", 2);
 			String target = split[0];
 			msg = split[1];
 			privmsg.getIrcConnection().send(new Privmsg(target, msg, privmsg.getIrcConnection()));
 		} else if (privmsg.getMessage().toLowerCase().startsWith("!j ") && privmsg.getNick().equals("Speed")) {
 			String msg = privmsg.getMessage().substring(2).trim();
 			privmsg.getIrcConnection().joinChannel(msg);
 		} else if (privmsg.getMessage().toLowerCase().equals("!rebuild") && privmsg.getNick().equals("Speed")) {
 			try {
 				connection.getWriter().write("QUIT :Rebuilding!\r\n");
 				connection.getWriter().flush();
 				connection.getWriter().close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			try {
 				Runtime.getRuntime().exec("./run.sh");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else if (privmsg.getMessage().toLowerCase().startsWith("!y ")) {
 			String id = privmsg.getMessage().replace("!y", "").trim();
 			if (id.matches("\\d+")) {
 				int i = Integer.parseInt(id);
 				vote(0, i, privmsg);
 			}
 		} else if (privmsg.getMessage().toLowerCase().startsWith("!n ")) {
 			String id = privmsg.getMessage().replace("!n", "").trim();
 			if (id.matches("\\d+")) {
 				int i = Integer.parseInt(id);
 				vote(1, i, privmsg);
 			}
 		} else if (privmsg.getMessage().toLowerCase().startsWith("!a ")) {
 			String id = privmsg.getMessage().replace("!a", "").trim();
 			if (id.matches("\\d+")) {
 				int i = Integer.parseInt(id);
 				vote(2, i, privmsg);
 			}
 		} else if (privmsg.getMessage().equalsIgnoreCase("!polls")) {
 			try {
 				PreparedStatement statement = dbConn.prepareStatement("SELECT * FROM polls WHERE closed = 0 AND expiry > ?");
 				statement.setLong(1, System.currentTimeMillis());
 				ResultSet results = statement.executeQuery();
 				privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "List of polls:", privmsg.getIrcConnection()));
 
 				while (results.next()) {
 					final String question = results.getString("question");
 					int id = results.getInt("id");
 					long expiry = results.getLong("expiry");
 					String[] options = stringToArray(results.getString("options"));
 					PreparedStatement stmt = dbConn.prepareStatement("SELECT * FROM votes WHERE pollId = ?");
 					stmt.setInt(1, id);
 					ResultSet rs = stmt.executeQuery();
 					int yes = 0, no = 0, abstain = 0;
 					while (rs.next()) {
 						int i = rs.getInt("answerIndex");
 						if (i == 0) {
 							yes++;
 						} else if (i == 1) {
 							no++;
 						} else if (i == 2) {
 							abstain++;
 						}
 					}
 
 					String msg = "Poll #" + String.valueOf(id) + ": " + question +
 							" Ends: " + SDF.format(new Date(expiry))
 							+ " Yes: " + yes + " No: " + no + " Abstain: " + abstain;
 					privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), msg, privmsg.getIrcConnection()));
 
 				}
 				privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "End list of polls.", privmsg.getIrcConnection()));
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else if (privmsg.getMessage().toLowerCase().startsWith("!closepoll ") || privmsg.getMessage().toLowerCase().startsWith("!openpoll ")) {
 			String[] parts = privmsg.getMessage().split(" ", 2);
 			if (parts.length != 2) {
 				return;
 			}
 			final int i;
 			if (privmsg.getMessage().toLowerCase().contains("openpoll")) {
 				i = 0;
 			} else {
 				i = 1;
 			}
 			String msg = parts[1];
 			if (msg.matches("\\d+")) {
 				final int id = Integer.parseInt(msg);
 
 				privmsg.getIrcConnection().addListener(new NoticeFilter() {
 					public boolean accept(Notice notice) {
 						Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
 						Matcher matcher = pattern.matcher(notice.getMessage());
 						if (matcher.find() && matcher.find()) {
 							String access = matcher.group(1);
 							if (access.equals("AOP") || access.equals("Founder") || access.equals("SOP")) {
 								return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getNick() + "\u0002");
 							}
 						}
 						if (notice.getMessage().equals("Permission denied."))
 							notice.getIrcConnection().removeListener(this);
 						return false;
 					}
 
 					public void run(Notice notice) {
 						PreparedStatement ps = null;
 						try {
 							ps = dbConn.prepareStatement("UPDATE polls SET closed = ? WHERE id = ?");
 							ps.setInt(1, i);
 							ps.setInt(2, id);
 							int rowsAffected = ps.executeUpdate();
 							if (rowsAffected > 0) {
 								privmsg.send(i == 1 ? "Poll closed." : "Poll opened.");
 
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						privmsg.getIrcConnection().removeListener(this);
 					}
 				});
 				privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + CHANNEL + " " + privmsg.getNick(), privmsg.getIrcConnection()));
 			}
 		}
 	}
 
 	private long parseExpiry(String expiry) {
 		if (expiry.matches("\\d{1,6}[whsdmWHSDM]?")) {
 			long multiplier = 1000;
 			if (Character.isLetter(expiry.charAt(expiry.length() - 1))) {
 				char c = Character.toLowerCase(expiry.charAt(expiry.length() - 1));
 				switch (c) {
 					case 'w':
 						multiplier *= 604800;
 						break;
 					case 'h':
 						multiplier *= 3600;
 						break;
 					case 'm':
 						multiplier *= 60;
 						break;
 					case 'd':
 						multiplier *= 86400;
 						break;
 					default:
 						break;
 				}
 				expiry = expiry.substring(0, expiry.length() - 1);
 			}
 			long exp = Long.parseLong(expiry) * multiplier;
 			return exp;
 		} else {
 			throw new IllegalArgumentException("too big");
 		}
 	}
 
 	private String[] stringToArray(String users) {
 		if (users == null || users.length() == 0)
 			return new String[0];
 		ArrayList<String> voters = new ArrayList<String>();
 		int start = 0;
 		for (int i = 1; i < users.length(); i++) {
 			char c = users.charAt(i);
 			if (c == ',' || i == users.length() - 1) {
 				if (i - 1 > 0 && users.charAt(i - 1) == '\\' && i != users.length() - 1) {
 					continue;
 				} else {
 					String str = users.substring(start, i == users.length() - 1 ? i + 1 : i);
 					voters.add(str);
 					start = i + 1;
 				}
 			}
 		}
 		return voters.toArray(new String[voters.size()]);
 	}
 
 	public String arrayToString(String[] users) {
 		StringBuilder builder = new StringBuilder();
 		for (String s : users) {
 			builder.append(s).append(',');
 		}
 		return builder.substring(0, builder.lastIndexOf(","));
 	}
 }
