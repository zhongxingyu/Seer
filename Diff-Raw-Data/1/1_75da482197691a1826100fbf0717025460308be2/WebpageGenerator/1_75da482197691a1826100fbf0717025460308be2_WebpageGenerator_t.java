 package de.sectud.ctf07.scoringsystem;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.TreeSet;
 
 import org.hcesperer.utils.DBConnection;
 import org.hcesperer.utils.djb.DJBSettings;
 
 import util.HTMLFilter;
 
 /**
  * This is a big, ugly class...
  * 
  * Anyway. This classes purpose is to export the current scoring data as HTML,
  * RSS and XML. The HTML files can be stored directly in a public directory
  * (i.e., a directory that gets published by a webserver). The RSS file, too,
  * can be placed in a public directory to be accessible by RSS readers.
  * 
  * The XML file contains more detailed information about the scoring, as well as
  * host-team relations. This data should be considered sensitive and not
  * published directly. However, at your discretion, you may use some tool to
  * interpret this data. For example, Jan 'ilo' Dillmann has created a nice
  * export tool that can read the XML file and create a nifty flash animation of
  * the current stats. More information can be found at
  * http://ctf.hcesperer.org/gameserver/
  * 
  * @author Hans-Christian Esperer
  * @email hc@hcesperer.org
  * 
  */
 public class WebpageGenerator implements Runnable {
 	/**
 	 * 
 	 */
 	private String htmlHEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
 			+ "\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
 			+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
 			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" "
 			+ "lang=\"en\">\n<head><title>%s</title><meta http-equiv=\"refresh\""
 			+ " content=\"60\" />\n"
 			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"ctf.css\" />\n"
 			+ "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Advisory-Feed\" href=\"rss.xml\" />"
 			+ "\n</head>\n<body><div class=\"main\"><div class=\"cnt\">";
 
 	private String htmlFOOTER = "<p><a href=\"rank.html\">Team ranks</a> -- "
 			+ "<a href=\"status.html\">Service states</a> -- "
 			+ "<a href=\"adv.html\">Advisories</a> -- "
 			+ "<a href=\"pending.html\">Pending advisories</a></p>"
 			+ "<hr /><p><a href=\"http://ctf.hcesperer.org/gameserver/\">HC's"
 			+ " scorebot $Id: WebpageGenerator.java 140 2008-09-12 10:39:59Z hc $</a></p></div></div></body></html>";
 
 	private String htmlSIMPLEFOOTER = "<hr /><p><a href=\"http://ctf.hcesperer"
 			+ ".org/gameserver/\">HC's"
 			+ " scorebot $Id: WebpageGenerator.java 140 2008-09-12 10:39:59Z hc $</a></p></div></div></body></html>";
 
 	private static final String XMLHEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
 			+ "<!DOCTYPE ctfscoredata PUBLIC \"-//various.ctf//DTD ctfscoredata 1.1//EN\" \"TBD://TBA\">\n"
 			+ "<ctfscoredata xmlns=\"TBD://TBA\" generationtime=\"%d\">\n";
 
 	private static final String XMLFOOTER = "</ctfscoredata>";
 
 	private static final String RSSHEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<rss version=\"2.0\">";
 
 	private static final String RSSFOOTER = "</channel>\n</rss>";
 
 	private final String wwwPath;
 
 	private final String wwwRoot;
 
 	private ArrayList<Long> ranks = new ArrayList<Long>(40);
 
 	private int iteration;
 
 	private static final int RESOLUTION = 10000;
 
 	public class ServiceStatus {
 		private final String status;
 
 		private final String detailesMessage;
 
 		private final String color;
 
 		public String getDetailesMessage() {
 			if (detailesMessage == null) {
 				return "";
 			}
 			return detailesMessage;
 		}
 
 		public String getStatus() {
 			if (status == null) {
 				return "";
 			}
 			return status;
 		}
 
 		public String getColor() {
 			if (color == null) {
 				return "";
 			}
 			return color;
 		}
 
 		public ServiceStatus(final String status, final String detailesMessage,
 				final String color) {
 			super();
 			this.status = status;
 			this.detailesMessage = detailesMessage;
 			this.color = color;
 		}
 
 		@Override
 		public int hashCode() {
 			final int PRIME = 31;
 			int result = 1;
 			result = PRIME * result + ((color == null) ? 0 : color.hashCode());
 			result = PRIME
 					* result
 					+ ((detailesMessage == null) ? 0 : detailesMessage
 							.hashCode());
 			result = PRIME * result
 					+ ((status == null) ? 0 : status.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			final ServiceStatus other = (ServiceStatus) obj;
 			if (color == null) {
 				if (other.color != null)
 					return false;
 			} else if (!color.equals(other.color))
 				return false;
 			if (detailesMessage == null) {
 				if (other.detailesMessage != null)
 					return false;
 			} else if (!detailesMessage.equals(other.detailesMessage))
 				return false;
 			if (status == null) {
 				if (other.status != null)
 					return false;
 			} else if (!status.equals(other.status))
 				return false;
 			return true;
 		}
 
 	}
 
 	/**
 	 * @param args
 	 * @throws FileNotFoundException
 	 */
 	public static void main(String[] args) throws FileNotFoundException {
 		new WebpageGenerator().run();
 	}
 
 	private void loadStrings() {
 		htmlHEADER = DJBSettings.loadText("control/htmlheader", htmlHEADER);
 		htmlFOOTER = DJBSettings.loadText("control/htmlfooter", htmlFOOTER);
 		htmlSIMPLEFOOTER = DJBSettings.loadText("control/simplefooter",
 				htmlSIMPLEFOOTER);
 	}
 
 	public WebpageGenerator() throws FileNotFoundException {
 		wwwPath = DJBSettings.loadString("control/wwwpath", "www");
 		wwwRoot = DJBSettings.loadString("control/wwwroot",
 				"http://localhost/score");
 		File f = new File(wwwPath + "/adv");
 		f.mkdirs();
 		if (!f.exists()) {
 			System.out.println("Error: permission denied to !" + wwwPath);
 			throw new FileNotFoundException("permission denied to " + wwwPath);
 		}
 		try {
 			URL url = getClass().getResource("www/ctf.css");
 			byte[] buf = DJBSettings.readBytes(url, 65535);
 			PrintStream ps = new PrintStream(wwwPath + "/ctf.css");
 			ps.write(buf);
 			ps.close();
 			ps = new PrintStream(wwwPath + "/adv/ctf.css");
 			ps.write(buf);
 			ps.close();
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 		System.out.println("Using path " + wwwPath);
 	}
 
 	public void run() {
 		try {
 			for (;;) {
 				try {
 					long begtime = System.currentTimeMillis();
 					System.out.println("Starting generation...");
 					handleStuff();
 					System.out.printf("Generation done. Time needed; %d ms.\n",
 							System.currentTimeMillis() - begtime);
 				} catch (RuntimeException e) {
 					System.err.println("=====BEGIN ERROR=====");
 					e.printStackTrace();
 					System.err.println("=====END ERROR=====");
 				}
 				System.out.println(System.currentTimeMillis()
 						+ ": regenerated websites");
 				Thread.sleep(60000);
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Update all requested data sources
 	 */
 	private void handleStuff() {
 		Connection c = DBConnection.getInstance().getDB();
 		try {
 			loadStrings();
 			handleRank(c, wwwPath + "/rank.html");
 			handleAdvisories(c, wwwPath);
 			handleStatus(c, wwwPath + "/status.html");
 			updateStatsTables(c);
 			iteration--;
 			if (iteration <= 0) {
 				generateXMLFile(c, "scoringdata.xml");
 				generateRSSFeed(c, wwwPath + "/rss.xml");
 				iteration = 2;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBConnection.getInstance().returnConnection(c);
 			c = null;
 		}
 	}
 
 	/**
 	 * Generate RSS feed
 	 * 
 	 * @param c
 	 *            connection to use
 	 * @param filename
 	 *            filename to rss
 	 * @throws SQLException
 	 * @throws FileNotFoundException
 	 */
 	private void generateRSSFeed(Connection c, String filename)
 			throws SQLException, FileNotFoundException {
 		PrintStream out = new PrintStream(filename);
 		Statement s = c.createStatement();
 		ResultSet rs;
 
 		out.println(RSSHEADER);
 
 		out
 				.println("<!-- $Id: WebpageGenerator.java 140 2008-09-12 10:39:59Z hc $ -->");
 		out.println("<channel>");
 		out.println("<title>Advisories</title>");
 		out.println("<link>http://ctf.hcesperer.org/gameserver/</link>");
 		out.println("<description>CTF advisories</description>");
 		out.println("<language>en</language>");
 
 		rs = s.executeQuery("select uid,advisory_team,advisory_description"
 				+ " from advisories order by uid");
 		try {
 			while (rs.next()) {
 				long uid = rs.getLong(1);
 				// String advTeam = escape(rs.getString(2));
 				String advDesc = escape(rs.getString(3));
 				String advService;
 				String advSeverity;
 				try {
 					String desc = advDesc;
 					int pos = desc.indexOf("New advisory by");
 					if (pos != -1) {
 						desc = desc.substring(pos);
 					}
 					String[] descParts = desc.split("\n");
 					advService = descParts[1].split(":", 2)[1].trim();
 					advSeverity = descParts[2].split(":", 2)[1].trim();
 				} catch (Throwable t) {
 					advService = "";
 					advSeverity = "";
 				}
 
 				if (advDesc.length() > 256) {
 					advDesc = advDesc.substring(0, 256) + "...";
 				}
 				out
 						.printf(
 								"<item>\n  <title>%s (%s)</title>\n  <description>%s</description>\n"
 										+ "  <link>%s/adv/a_%d.html</link>\n  <guid>%s/adv/a_%d.html</guid>\n</item>",
 								HTMLFilter.filter(advService), HTMLFilter
 										.filter(advSeverity), HTMLFilter
 										.filter(HTMLFilter.filter(advDesc)
 												.replace("\n", "<br />")),
 								wwwRoot, uid, wwwRoot, uid);
 			}
 			out.println(RSSFOOTER);
 			out.close();
 		} finally {
 			rs.close();
 		}
 	}
 
 	/**
 	 * @param c
 	 * @param filename
 	 * @throws FileNotFoundException
 	 * @throws SQLException
 	 */
 	private void handleStatus(Connection c, String filename)
 			throws FileNotFoundException, SQLException {
 		PrintStream ps = new PrintStream(filename);
 		String[] teams = getTeams(c);
 		String[] services = getServices(c);
 		HashMap<String, ServiceStatus> states = getStates(c);
 		ps.print(String.format(htmlHEADER, "Service status"));
 		ps.print("<h1>Service status</h1>");
 		ps.print("<table>");
 		ps.print("<tr><td></td>");
 		for (int i = 0; i < services.length; i++) {
 			ps.print("<td><strong>");
 			ps.print(HTMLFilter.filter(services[i]));
 			ps.print("</strong></td>");
 		}
 		ps.print("</tr>");
 		for (int i = 0; i < teams.length; i++) {
 			ps.print("<tr><td><strong>");
 			ps.print(teams[i]);
 			ps.print("</strong></td>");
 			for (int j = 0; j < services.length; j++) {
 				ServiceStatus status = states.get(teams[i] + " " + services[j]);
 				String color = HTMLFilter.filter(status.getColor());
 				ps.print("<td style=\"color: " + color + "\" >");
 				ps.print("<abbr title=\"");
 				ps.print(HTMLFilter.filter(status.getDetailesMessage()));
 				ps.print("\">");
 				ps.print(HTMLFilter.filter(status.getStatus()));
 				ps.print("</abbr>");
 				ps.print("</td>");
 			}
 			ps.print("</tr>");
 		}
 		ps.print("</table>");
 		ps.print(htmlFOOTER);
 		ps.close();
 	}
 
 	private HashMap<String, ServiceStatus> getStates(Connection c)
 			throws SQLException {
 		HashMap<String, ServiceStatus> map = new HashMap<String, ServiceStatus>();
 		Statement s = c.createStatement();
 		try {
 			ResultSet rs = s
 					.executeQuery("select status_team,status_service,status_text,status_verboseerror,status_color from states");
 			while (rs.next()) {
 				String team = rs.getString(1);
 				String service = rs.getString(2);
 				ServiceStatus status = new ServiceStatus(rs.getString(3), rs
 						.getString(4), rs.getString(5));
 				map.put(team + " " + service, status);
 			}
 			rs.close();
 		} finally {
 			s.close();
 		}
 		return map;
 	}
 
 	private String[] getServices(Connection c) throws SQLException {
 		Statement s = c.createStatement();
 		ResultSet rs = s
 				.executeQuery("select service_name from services order by uid");
 		ArrayList<String> teams = new ArrayList<String>(10);
 		while (rs.next()) {
 			teams.add(rs.getString(1));
 		}
 		rs.close();
 		s.close();
 		return teams.toArray(new String[0]);
 	}
 
 	private String[] getTeams(Connection c) throws SQLException {
 		Statement s = c.createStatement();
 		ArrayList<String> teams;
 		try {
 			ResultSet rs = s
 					.executeQuery("select team_name from teams order by uid");
 			teams = new ArrayList<String>(10);
 			while (rs.next()) {
 				teams.add(rs.getString(1));
 			}
 			rs.close();
 		} finally {
 			s.close();
 		}
 		return teams.toArray(new String[0]);
 	}
 
 	private void handleAdvisories(Connection c, String string)
 			throws SQLException, FileNotFoundException {
 		Statement s = c.createStatement();
 		try {
 			ResultSet rs = s
 					.executeQuery("select uid,advisory_description,advisory_team,advisory_status,advisory_comment from advisories where advisory_generated=false");
 			ArrayList<Long> tbu = new ArrayList<Long>(50);
 			try {
 				while (rs.next()) {
 					Long uid = rs.getLong(1);
 					String desc = rs.getString(2);
 					String team = rs.getString(3);
 					String status = rs.getString(4);
 					String comment = rs.getString(5);
 					boolean pending = "pending".equals(status);
 					// Add all non-pending advisories to a list
 					// so they are not re-generated later.
 					if (!pending) {
 						tbu.add(uid);
 					}
 					PrintStream ps = new PrintStream(string + "/adv/a_" + uid
 							+ ".html");
 					ps.print(String.format(htmlHEADER, "Advisory # " + uid));
 					ps.print("<h1>Advisory #" + uid + "</h1>");
 					ps.print("<h2>From team " + HTMLFilter.filter(team)
 							+ "</h2>");
 					ps.print("<p>");
 					ps.print(HTMLFilter.filter(desc).replace("\n", "<br />"));
 					ps.print("</p>");
 					if (!pending) {
 						ps.print("<h2>Rating</h2>");
 						ps.print("<p>");
 						ps.print(HTMLFilter.filter(comment).replace("\n",
 								"<br />"));
 						ps.print("</p>");
 					}
 					ps
 							.print("<p><a href=\"../adv.html\" onclick=\"javascript:history.goback();return false;\">Go back</a></p>");
 					ps.print(htmlSIMPLEFOOTER);
 					ps.close();
 				}
 			} finally {
 				rs.close();
 			}
 			for (Long uid : tbu) {
 				s
 						.executeUpdate("update advisories set advisory_generated=true where uid="
 								+ uid);
 			}
 			PrintStream ps = new PrintStream(string + "/adv.html");
 			PrintStream ps2 = new PrintStream(string + "/pending.html");
 			ps.print(String.format(htmlHEADER, "Advisories"));
 			ps2.print(String.format(htmlHEADER, "Pending Advisories"));
 			ps.print("<h1>List of advisories</h1>");
 			ps2.print("<h1>List of pending advisories</h1>");
 			ps.print("<table><tr><th>Team</th><th style=\"text-align: "
 					+ "left\">Advisory</th>"
 					+ "<th>Status</th><th>Rating</th></tr>");
 			ps2.print("<table><tr><th>Team</th><th style=\"text-align: "
 					+ "left\">Advisory</th>"
 					+ "<th>Status</th><th>Rating</th></tr>");
 			rs = s
 					.executeQuery("select uid,advisory_team,advisory_description,"
 							+ "advisory_status,advisory_comment from advisories order"
 							+ " by uid desc");
 			try {
 				while (rs.next()) {
 					long uid = rs.getLong(1);
 					String team = rs.getString(2);
 					String desc = rs.getString(3);
 					String status = rs.getString(4);
 					String comment = rs.getString(5);
 					PrintStream s_p = ("pending".equals(status)) ? ps2 : ps;
 					s_p.print("<tr><td>");
 					s_p.print(HTMLFilter.filter(team));
 					s_p.print("</td><td style=\"width: 400px\">");
 					String d = null;
 					try {
 						int pos = desc.indexOf("New advisory by");
 						if (pos != -1) {
 							desc = desc.substring(pos);
 						}
 						d = HTMLFilter.filter(desc);
 						String[] descParts = d.split("\n");
 						d = "<strong>BY</strong>: "
 								+ descParts[0].split(":")[1]
 								+ "  <strong>service</strong>: "
 								+ descParts[1].split(":")[1]
 								+ "   <strong>severity</strong>: "
 								+ descParts[2].split(":")[1];
 					} catch (Throwable t) {
 					}
 					if (d == null) {
 						d = "NO DESC";
 					}
 					if (d.length() > 128) {
 						d = d.substring(0, 128);
 					}
 					s_p.print(d);
 					s_p.print(" (<a href=\"adv/a_" + uid
 							+ ".html\">details</a>)");
 					s_p.print("</td><td>");
 					s_p.print(HTMLFilter.filter(status));
 					s_p.print("</td><td style=\"width: 250px\">");
 					s_p.print(HTMLFilter.filter((comment != null) ? comment
 							: ""));
 					s_p.print("</td></tr>");
 				}
 			} finally {
 				rs.close();
 			}
 			ps.print("</table>");
 			ps.print(htmlFOOTER);
 			ps2.print("</table>");
 			ps2.print(htmlFOOTER);
 			ps.close();
 			ps2.close();
 		} finally {
 			s.close();
 		}
 	}
 
 	private void handleRank(Connection c, String string)
 			throws FileNotFoundException, SQLException {
 		PrintStream ps = new PrintStream(string);
 		Statement s = c.createStatement();
 		ArrayList<TeamPoints> teams = new ArrayList<TeamPoints>(20);
 		int max_o = 1;
 		int max_d = 1;
 		int max_a = 1;
 		int max_h = 1;
 		try {
 			ResultSet rs = s
 					.executeQuery("select uid,team_name,team_points_offensive,team_points_defensive,team_points_advisories,team_points_hacking from teams");
 			while (rs.next()) {
 				int off = rs.getInt(3);
 				int def = rs.getInt(4);
 				int adv = rs.getInt(5);
 				int hac = rs.getInt(6);
 				TeamPoints team = new TeamPoints(rs.getLong(1),
 						rs.getString(2), off, def, adv, hac);
 				teams.add(team);
 				if (max_o < off) {
 					max_o = off;
 				}
 				if (max_d < def) {
 					max_d = def;
 				}
 				if (max_a < adv) {
 					max_a = adv;
 				}
 				if (max_h < hac) {
 					max_h = hac;
 				}
 			}
 			rs.close();
 		} finally {
 			s.close();
 		}
 
 		TreeSet<TeamPoints> orderedTeams = new TreeSet<TeamPoints>();
 		for (TeamPoints team : teams) {
 			orderedTeams.add(new TeamPoints(team.getUID(), team.getName(), team
 					.getOff()
 					* RESOLUTION / max_o, team.getDef() * RESOLUTION / max_d,
 					team.getAdv() * RESOLUTION / max_a, team.getHac()
 							* RESOLUTION / max_h));
 		}
 
 		ps.print(String.format(htmlHEADER, "Team rank display"));
 		ps.print("<h1>Team ranking</h1>");
 		ps.print("<table>");
 		ps
 				.print("<tr><th>Team</th><th>Offensive points</th><th>Defensive points"
 						+ "</th><th>Advisory points</th><th>Rule compliance</th><th>Total points</th></tr>");
 		ranks.clear();
 		while (!orderedTeams.isEmpty()) {
 			TeamPoints team = orderedTeams.pollLast();
 			this.ranks.add(team.getUID());
 			ps.print("<tr><td>");
 			ps.print(HTMLFilter.filter(team.getName()));
 			ps.print("<br /><img src=\"team" + String.valueOf(team.getUID())
 					+ ".png\" alt=\"\" />");
 			ps.print("</td><td>");
 			ps.print(team.getOffPercS());
 			ps.print("%</td><td>");
 			ps.print(team.getDefPercS());
 			ps.print("%</td><td>");
 			ps.print(team.getAdvPercS());
 			ps.print("%</td><td>");
 			ps.print(team.getHacPercS());
 			ps.print("%</td><td>");
 			ps.print(team.getTotalPercS());
 			ps.print("%</td></tr>");
 		}
 		ps.print("</table>");
 		ps.print(htmlFOOTER);
 
 		ps.close();
 	}
 
 	/**
 	 * Update the stats tables used by spida's ctf-vis (http://www.spida.net)
 	 * 
 	 * @throws SQLException
 	 */
 	private void updateStatsTables(Connection c) throws SQLException {
 		long curTime = System.currentTimeMillis() / 1000;
 		PreparedStatement ps = c
 				.prepareStatement("insert into stats_times (stats_time) values (?)");
 		try {
 			ps.setLong(1, curTime);
 			ps.execute();
		} catch (SQLException e) { // we can ignore this one
 		} finally {
 			ps.close();
 		}
 		Statement s = c.createStatement();
 		try {
 			/* Store the current points in the stats table */
 			ResultSet rs = s
 					.executeQuery("select team_name,team_points_offensive,team_points_defensive,team_points_advisories,team_points_hacking from teams");
 			ps = c
 					.prepareStatement("insert into stats_points (stats_time,"
 							+ "stats_team,stats_points_defensive,stats_points_offensive,"
 							+ "stats_points_advisory,stats_points_rulecompliance)"
 							+ " values (?,?,?,?,?,?)");
 			try {
 				while (rs.next()) {
 					String d1 = rs.getString(1);
 					int d2 = rs.getInt(2);
 					int d3 = rs.getInt(3);
 					int d4 = rs.getInt(4);
 					int d5 = rs.getInt(5);
 					ps.setLong(1, curTime);
 					ps.setString(2, d1);
 					ps.setInt(3, d2);
 					ps.setInt(4, d3);
 					ps.setInt(5, d4);
 					ps.setInt(6, d5);
 					ps.execute();
 				}
 			} finally {
 				rs.close();
 				ps.close();
 			}
 
 			/* Store the current service states in the stats table */
 			rs = s.executeQuery("select status_team,status_service,"
 					+ "status_text,status_verboseerror from states");
 			ps = c.prepareStatement("insert into stats_services (stats_time,"
 					+ "stats_team,stats_service,stats_status,stats_statusmsg) "
 					+ "values (?,?,?,?,?)");
 			try {
 				while (rs.next()) {
 					String d1 = rs.getString(1);
 					String d2 = rs.getString(2);
 					String d3 = rs.getString(3);
 					String d4 = rs.getString(4);
 					ps.setLong(1, curTime);
 					ps.setString(2, d1);
 					ps.setString(3, d2);
 					ps.setString(4, d3);
 					ps.setString(5, d4);
 					ps.execute();
 				}
 			} finally {
 				rs.close();
 				ps.close();
 			}
 		} finally {
 			s.close();
 		}
 
 	}
 
 	private void generateXMLFile(Connection c, String filename)
 			throws FileNotFoundException, SQLException {
 		PrintStream out = new PrintStream(filename);
 		Statement s = c.createStatement();
 		try {
 			ResultSet rs;
 
 			HashMap<String, Long> teamMap = new HashMap<String, Long>();
 
 			out.printf(XMLHEADER, System.currentTimeMillis() / 1000);
 
 			out.println("  <teams>");
 			rs = s.executeQuery("select uid,team_name from teams order by uid");
 			try {
 				while (rs.next()) {
 					long uid = rs.getLong(1);
 					String teamName = rs.getString(2);
 					out.printf("    <team id=\"%d\" name=\"%s\" />\n", uid,
 							HTMLFilter.filter(teamName));
 					teamMap.put(teamName, uid);
 				}
 			} finally {
 				rs.close();
 			}
 			out.println("  </teams>");
 
 			StatCounter<String> lostFlags = new StatCounter<String>();
 			StatCounter<String> capturedFlags = new StatCounter<String>();
 			StatCounter<String> serviceFlags = new StatCounter<String>();
 			StatCounter<StringPair> lostFlagsPerService = new StatCounter<StringPair>();
 			StatCounter<StringPair> capturedFlagsPerService = new StatCounter<StringPair>();
 			rs = s
 					.executeQuery("select flag_fromteam,flag_collectingteam,flag_service from flagstats");
 			try {
 				while (rs.next()) {
 					String fromTeam = rs.getString(1);
 					String collectingTeam = rs.getString(2);
 					String service = rs.getString(3);
 					lostFlags.count(fromTeam);
 					capturedFlags.count(collectingTeam);
 					serviceFlags.count(service);
 					lostFlagsPerService
 							.count(new StringPair(fromTeam, service));
 					capturedFlagsPerService.count(new StringPair(
 							collectingTeam, service));
 				}
 			} finally {
 				rs.close();
 			}
 
 			ArrayList<String> serviceNames = new ArrayList<String>(20);
 			rs = s
 					.executeQuery("select service_name from services order by uid");
 			while (rs.next()) {
 				serviceNames.add(rs.getString(1));
 			}
 			rs.close();
 
 			out.println("  <teamdata>");
 			rs = s
 					.executeQuery("select uid,team_host,team_name from teams order by uid");
 			try {
 				while (rs.next()) {
 					long uid = rs.getLong(1);
 					String teamHost = rs.getString(2);
 					String teamName = rs.getString(3);
 					out.printf("    <team id=\"%d\">\n", uid);
 					out.printf(
 							"      <property key=\"host\" value=\"%s\" />\n",
 							teamHost);
 					out
 							.printf(
 									"      <property key=\"lostFlags\" value=\"%d\" />\n",
 									lostFlags.getCount(teamName));
 					out
 							.printf(
 									"      <property key=\"capturedFlags\" values=\"%d\" />\n",
 									capturedFlags.getCount(teamName));
 					out.println("      <detailedStats>");
 					for (String serviceName : serviceNames) {
 						out
 								.printf(
 										"        <count type=\"capturedFlags\" subject=\"%s\" value=\"%d\" />\n",
 										serviceName, capturedFlagsPerService
 												.getCount(new StringPair(
 														teamName, serviceName)));
 						out
 								.printf(
 										"        <count type=\"lostFlags\" subject=\"%s\" value=\"%d\" />\n",
 										serviceName, lostFlagsPerService
 												.getCount(new StringPair(
 														teamName, serviceName)));
 					}
 					out.println("      </detailedStats>");
 					out.println("    </team>");
 				}
 				out.println("  </teamdata>");
 			} finally {
 				rs.close();
 			}
 
 			out.println("  <scoreblock>");
 			rs = s
 					.executeQuery("select uid,team_points_offensive,team_points_defensive,"
 							+ "team_points_advisories,team_points_hacking from teams order by uid");
 			try {
 				while (rs.next()) {
 					long uid = rs.getLong(1);
 					int off = rs.getInt(2);
 					int def = rs.getInt(3);
 					int adv = rs.getInt(4);
 					int hak = rs.getInt(5);
 					out.printf("    <team id=\"%d\">\n", uid);
 					out
 							.printf(
 									"      <points type=\"offensive\" value=\"%d\" />\n",
 									off);
 					out
 							.printf(
 									"      <points type=\"defensive\" value=\"%d\" />\n",
 									def);
 					out
 							.printf(
 									"      <points type=\"advisory\" value=\"%d\" />\n",
 									adv);
 					out.printf(
 							"      <points type=\"hacking\" value=\"%d\" />\n",
 							hak);
 					out.println("    </team>");
 				}
 			} finally {
 				rs.close();
 			}
 			out.println("  </scoreblock>");
 
 			out.println("  <services>");
 			rs = s
 					.executeQuery("select uid,service_name from services order by uid");
 			try {
 				while (rs.next()) {
 					long uid = rs.getLong(1);
 					String s_name = rs.getString(2);
 					out.printf("    <service id=\"%d\" name=\"%s\">\n", uid,
 							s_name);
 					out
 							.printf(
 									"      <stat type=\"capturedFlags\" value=\"%d\" />\n",
 									serviceFlags.getCount(s_name));
 					out.println("    </service>");
 				}
 				out.println("  </services>");
 			} finally {
 				rs.close();
 			}
 
 			StringBuilder sb = new StringBuilder();
 			sb.append("  <ranking order=\"");
 			boolean firstEntry = true;
 			for (Long uid : ranks) {
 				if (!firstEntry) {
 					sb.append(":");
 				} else {
 					firstEntry = false;
 				}
 				sb.append(uid);
 			}
 			sb.append("\" />");
 			out.println(sb);
 
 			out.println("  <advisories>");
 			rs = s
 					.executeQuery("select uid,advisory_team,advisory_description,"
 							+ "advisory_status,advisory_time,advisory_comment,advisory_from"
 							+ " from advisories order by uid");
 			try {
 				while (rs.next()) {
 					long uid = rs.getLong(1);
 					String advTeam = escape(rs.getString(2));
 					String advDesc = escape(rs.getString(3));
 					String advStatus = escape(rs.getString(4));
 					long advTime = rs.getLong(5);
 					String advComment = escape(rs.getString(6));
 					String advFrom = escape(rs.getString(7));
 
 					Long teamID = teamMap.get(advTeam);
 					if (teamID == null) {
 						teamID = Long.valueOf(-1);
 					}
 					out
 							.printf(
 									"    <advisory id=\"%d\" team=\"%d\" status=\"%s\" awardedpoints=\"0\" "
 											+ "comment=\"%s\" time=\"%d\" service=\"%s\">%s</advisory>\n",
 									uid, teamID, advStatus, advComment,
 									advTime, advFrom, advDesc);
 				}
 			} finally {
 				rs.close();
 			}
 			out.println("  </advisories>");
 
 			out.println(XMLFOOTER);
 			out.close();
 		} finally {
 			s.close();
 		}
 	}
 
 	private static String escape(String s) {
 		return HTMLFilter.filter(s);
 	}
 
 	public class TeamPoints implements Comparable<TeamPoints> {
 		private final String name;
 
 		private final int off;
 
 		private final int def;
 
 		private final int adv;
 
 		private final int hac;
 
 		private final int total;
 
 		private final long uid;
 
 		@Override
 		public int hashCode() {
 			final int PRIME = 31;
 			int result = 1;
 			result = PRIME * result + adv;
 			result = PRIME * result + def;
 			result = PRIME * result + hac;
 			result = PRIME * result + ((name == null) ? 0 : name.hashCode());
 			result = PRIME * result + off;
 			result = PRIME * result + (int) (uid ^ (uid >>> 32));
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			final TeamPoints other = (TeamPoints) obj;
 			if (adv != other.adv)
 				return false;
 			if (def != other.def)
 				return false;
 			if (hac != other.hac)
 				return false;
 			if (name == null) {
 				if (other.name != null)
 					return false;
 			} else if (!name.equals(other.name))
 				return false;
 			if (off != other.off)
 				return false;
 			if (uid != other.uid)
 				return false;
 			return true;
 		}
 
 		public int getTotal() {
 			return total;
 		}
 
 		public int getAdv() {
 			return adv;
 		}
 
 		public int getDef() {
 			return def;
 		}
 
 		public int getHac() {
 			return hac;
 		}
 
 		public double getAdvPerc() {
 			return (double) adv * 100 / RESOLUTION;
 		}
 
 		public double getDefPerc() {
 			return (double) def * 100 / RESOLUTION;
 		}
 
 		public double getOffPerc() {
 			return (double) off * 100 / RESOLUTION;
 		}
 
 		public double getHacPerc() {
 			return (double) hac * 100 / RESOLUTION;
 		}
 
 		public double getTotalPerc() {
 			return (double) total * 100 / RESOLUTION;
 		}
 
 		public long getUID() {
 			return uid;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public int getOff() {
 			return off;
 		}
 
 		public String formatNumber(double number) {
 			NumberFormat nf = new DecimalFormat("#00.00");
 			return nf.format(number);
 		}
 
 		public String getOffPercS() {
 			return formatNumber(getOffPerc());
 		}
 
 		public String getDefPercS() {
 			return formatNumber(getDefPerc());
 		}
 
 		public String getAdvPercS() {
 			return formatNumber(getAdvPerc());
 		}
 
 		public String getHacPercS() {
 			return formatNumber(getHacPerc());
 		}
 
 		public String getTotalPercS() {
 			return formatNumber(getTotalPerc());
 		}
 
 		public TeamPoints(final long uid, final String name, final int off,
 				final int def, final int adv, final int hac) {
 			super();
 			this.uid = uid;
 			this.name = name;
 			this.off = off;
 			this.def = def;
 			this.adv = adv;
 			this.hac = hac;
 			this.total = (off + def + adv + hac) / 4;
 		}
 
 		public int compareTo(TeamPoints o) {
 			if (o.total > total) {
 				return -1;
 			}
 			if (o.total < total) {
 				return 1;
 			}
 			return name.compareTo(o.name);
 		}
 	}
 }
