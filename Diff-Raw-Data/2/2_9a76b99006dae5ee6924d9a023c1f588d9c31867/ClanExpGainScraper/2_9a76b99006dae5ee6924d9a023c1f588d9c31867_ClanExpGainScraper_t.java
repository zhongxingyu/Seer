 package com.wikia.runescape;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.*;
 
 import org.mediawiki.MediaWiki;
 
 /*-
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class ClanExpGainScraper {
 	private static final DecimalFormat commaFormatter = new DecimalFormat(",##0", new DecimalFormatSymbols(Locale.US));
 
 	private static final SimpleDateFormat utcDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
 
 	private static final SimpleDateFormat utcTimeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
 
 	static {
 		utcDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
 		utcTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
 	}
 
 	public static final int MAX_COLUMNS = 8;
 
 	/**
 	 * @param args
 	 *            Command-line arguments. May be either of the following:
 	 *            <ul>
 	 *            <li>"--config-file", followed by the path to the configuration
 	 *            file to use
 	 *            </ul>
 	 */
 	public static void main(final String[] args) {
 		Settings settings = null;
 		{
 			boolean fatalError = false;
 			File configFile = new File(System.getProperty("user.home"), ".clanscraper.conf");
 			// Read the bot's configuration file. Its default location, if not
 			// overridden on the command-line, is $HOME/.clanscraper.conf.
 			for (int i = 0; i < args.length; i++) {
 				if ("--config-file".equals(args[i])) {
 					i++;
 					if (i >= args.length) {
 						System.err.println("The command line needs a file name after the --config-file option");
 						fatalError = true;
 					} else {
 						configFile = new File(args[i]);
 					}
 				}
 			}
 
 			// Require some things out of it from the start...
 			if (!fatalError) {
 				settings = new Settings(configFile);
 				if (settings.getProperty("Wiki") == null) {
 					System.err.println("The configuration file " + configFile.toString() + " does not contain a value for Wiki, the wiki to work on");
 					fatalError = true;
 				}
 				if (settings.getProperty("LoginName") == null) {
 					System.err.println("The configuration file " + configFile.toString() + " does not contain a value for LoginName, the username of the bot account on the wiki");
 					fatalError = true;
 				}
 				if (settings.getProperty("LoginPassword") == null) {
 					System.err.println("The configuration file " + configFile.toString() + " does not contain a value for LoginPassword, the password of the bot account on the wiki");
 					fatalError = true;
 				}
 				if (settings.getProperty("OutputPage") == null) {
 					System.err.println("The configuration file " + configFile.toString() + " does not contain a value for OutputPage, the name of the page updated by the bot on the wiki");
 					fatalError = true;
 				}
 				if (settings.getProperty("LogPage") == null) {
 					System.err.println("The configuration file " + configFile.toString() + " does not contain a value for LogPage, the name of the page the wiki updated with a summary of actions and errors");
 					fatalError = true;
 				}
 				if (settings.getProperty("Clan") == null) {
 					System.err.println("The configuration file " + configFile.toString() + " does not contain a value for Clan, the name of the clan to gather information for");
 					fatalError = true;
 				}
 			}
 			if (fatalError) {
 				System.exit(1);
 				return;
 			}
 		}
 
 		// Which wiki are we working on?
 		final MediaWiki wiki = new MediaWiki(settings.getProperty("Wiki"), settings.getProperty("ScriptPath", "")).setUsingCompression(true);
 
 		while (true) {
 			try {
 				wiki.logIn(settings.getProperty("LoginName"), settings.getProperty("LoginPassword").toCharArray());
 				break;
 			} catch (final MediaWiki.LoginFailureException e) {
 				System.err.println("Login failed; please check LoginName and LoginPassword in the configuration file");
 				e.printStackTrace();
 				System.exit(1);
 				return;
 			} catch (final MediaWiki.LoginDelayException t) {
 				System.err.println("Login throttled; retrying in " + t.getWaitTime() + " seconds");
 				try {
 					Thread.sleep((long) t.getWaitTime() * 1000);
 				} catch (InterruptedException e) {
 					// don't care
 				}
 			} catch (final MediaWiki.BlockException b) {
 				System.err.println("User blocked; please check its block log");
 				b.printStackTrace();
 				System.exit(1);
 				return;
 			} catch (IOException e) {
 				System.err.println("Network error occurred while logging in; retrying shortly");
 				e.printStackTrace();
 				shortDelay();
 			} catch (MediaWiki.MediaWikiException e) {
 				System.err.println("Network error occurred while logging in; retrying shortly");
 				e.printStackTrace();
 				shortDelay();
 			}
 		}
 
 		Date runTime = new Date();
 
 		StringWriter logChars = new StringWriter();
 		PrintWriter log = new PrintWriter(logChars);
 
 		StringWriter successLogChars = new StringWriter();
 		PrintWriter successLog = new PrintWriter(successLogChars);
 
 		int attempt;
 		MediaWiki.EditToken editToken = null;
 
 		attempt = 0;
 		while (attempt < 3)
 			// retry loop for edit token
 			try {
 				attempt++;
 				editToken = wiki.startEdit(settings.getProperty("OutputPage"));
 				break;
 			} catch (Throwable e) {
 				log.println("While getting an edit token for " + settings.getProperty("OutputPage") + ", attempt " + attempt + ":");
 				e.printStackTrace(log);
 			}
 		if (editToken == null) {
 			log.println("Giving up after too many attempts.");
 			postLog(wiki, runTime, settings, logChars);
 			System.exit(1);
 			return;
 		}
 
 		// 1. Read the table from the bot's output page.
 		String text = null;
 		attempt = 0;
 		while (attempt < 3)
 			// retry loop for output page readout
 			try {
 				attempt++;
 				Iterator<MediaWiki.Revision> i = wiki.getLastRevision(settings.getProperty("OutputPage"));
 				if (i.hasNext()) {
 					MediaWiki.Revision rev = i.next();
 					if (rev == null) {
 						text = "";
 						break;
 					}
 					if (!rev.isContentHidden() && rev.getContent() != null) {
 						text = rev.getContent();
 						break;
 					} else
 						log.println("While getting the text of " + settings.getProperty("OutputPage") + ", attempt " + attempt + ": Hidden content or no content returned");
 				} else
 					log.println("While getting the text of " + settings.getProperty("OutputPage") + ", attempt " + attempt + ": No revision information returned");
 			} catch (Throwable e) {
 				log.println("While getting the text of " + settings.getProperty("OutputPage") + ", attempt " + attempt + ":");
 				e.printStackTrace(log);
 			}
 		if (text == null) {
 			log.println("Giving up after too many attempts.");
 			postLog(wiki, runTime, settings, logChars);
 			System.exit(1);
 			return;
 		}
 
 		// 2. Parse the rows and columns of the table.
 		List<List<String>> table;
 		try {
 			table = parseDataTable(text);
 		} catch (ParseException e) {
 			log.println("While parsing the text of " + settings.getProperty("OutputPage") + " as a table:");
 			e.printStackTrace(log);
 			log.println("Context:" + text.substring(Math.max(0, e.getErrorOffset() - 40), Math.min(text.length(), e.getErrorOffset() + 40)));
 			postLog(wiki, runTime, settings, logChars);
 			System.exit(1);
 			return;
 		}
 		if (table.size() > 2)
 			Collections.sort(table.subList(1, table.size()), new TableRowComparator());
 
 		// 3. Delete the last run's Difference column, and formatting.
 		if (!table.isEmpty() && !table.get(0).isEmpty() && table.get(0).get(table.get(0).size() - 1).equals("Last difference")) {
 			for (List<String> row : table) {
 				row.remove(row.size() - 1);
 			}
 		}
 
 		for (List<String> row : table) {
 			for (int i = 0; i < row.size(); i++) {
 				String cellContents = row.get(i);
 				if (cellContents != null) {
 					// Formatting attributes for cells are before the |.
 					int index = cellContents.indexOf('|');
 					if (index != -1)
 						cellContents = cellContents.substring(index + 1);
 				}
 				if (cellContents.trim().length() == 0)
 					cellContents = null;
 				row.set(i, cellContents);
 			}
 		}
 
 		// 4. Get user info from the clan high scores.
 		attempt = 0;
 		List<ClanMember> members = null;
 		while (attempt < 3)
 			try {
 				members = HSScraper.getAll(settings.getProperty("Clan"));
 				break;
 			} catch (IOException e) {
 				log.println("While getting information about the members of " + settings.getProperty("Clan") + ", attempt " + attempt + ":");
 				e.printStackTrace(log);
 			}
 		if (members == null) {
 			log.println("Giving up after too many attempts.");
 			postLog(wiki, runTime, settings, logChars);
 			System.exit(1);
 			return;
 		}
 
 		// 5. Add a column to the table. If there is no column to begin
 		// with, also add a clanmates column.
 		if (table.isEmpty()) {
 			List<String> firstRow = new ArrayList<String>();
 			firstRow.add("Clanmate");
 			table.add(firstRow);
 		}
 		for (List<String> row : table) {
 			row.add(null);
 		}
 
 		// 6. Fill in the new column from the clanmates' current total
 		// EXP gains. Some will need new rows to be added; some will be
 		// gone.
 
 		// New cell header: run timestamp
 		table.get(0).set(table.get(0).size() - 1, utcDateFormatter.format(runTime) + "<br />" + utcTimeFormatter.format(runTime) + " UTC");
 
 		for (ClanMember member : members) {
 			List<List<String>> dataRows = table.subList(1, table.size());
 			int index = Collections.binarySearch(dataRows, Collections.singletonList(member.getName()), new TableRowComparator());
 			List<String> row;
 			if (index < 0) {
 				index = -index - 1; // Clanmate not present, insert here
 				row = new ArrayList<String>();
 				for (@SuppressWarnings("unused")
 				String dummy : table.get(0))
 					row.add(null);
 				row.set(0, member.getName());
 				dataRows.add(index, row);
 			} else
 				row = dataRows.get(index);
 
 			row.set(row.size() - 1, commaFormatter.format(member.getExpGain()));
 		}
 
 		// 7. Cap to 8 columns of data. (Plus the clanmate name column)
 		if (table.get(0).size() > MAX_COLUMNS + 1) {
 			for (List<String> row : table)
 				row.remove(1);
 		}
 
 		// 8. Add the Difference column as well as added and removed members to
 		// the log.
 		// There must be at least 2 columns of data for this.
 		if (table.get(0).size() > 2) { // 2 data columns, 1 name column
 			table.get(0).add("Last difference");
 			for (int i = 1; i < table.size(); i++) {
 				List<String> row = table.get(i);
 
 				if (row.get(row.size() - 2) == null && row.get(row.size() - 1) != null)
 					// Added
 					successLog.println("Join: " + row.get(0).replace("&nbsp;", " "));
 				else if (row.get(row.size() - 2) != null && row.get(row.size() - 1) == null)
 					// Removed
 					successLog.println("Quit: " + row.get(0).replace("&nbsp;", " "));
 
 				if (row.get(row.size() - 2) != null && row.get(row.size() - 1) != null)
 					row.add(commaFormatter.format(Long.parseLong(row.get(row.size() - 1).replace(",", "")) - Long.parseLong(row.get(row.size() - 2).replace(",", ""))));
 				else
 					row.add(null);
 			}
 		}
 
 		// 9. Create the new table.
 		text = createDataTable(table);
 
 		// 10. Write it on the bot's output page.
 		attempt = 0;
 		while (attempt < 3)
 			// network retry loop for edit token
 			try {
 				attempt++;
 				wiki.createOrReplacePage(editToken, text, "Add new data", true /* bot */, false /* minor */);
 				break;
 			} catch (Throwable e) {
 				log.println("While editing " + settings.getProperty("OutputPage") + ", attempt " + attempt + ":");
 				e.printStackTrace(log);
 			}
 		if (attempt == 3) {
 			log.println("Giving up after too many attempts.");
 			postLog(wiki, runTime, settings, logChars);
 			System.exit(1);
 			return;
 		}
 
 		log.print(successLogChars.toString());
 		postLog(wiki, runTime, settings, logChars);
 	}
 
 	/**
 	 * A comparator that compares rows of the user table. This is used to sort
 	 * the table, insert new rows and look up clanmates by name
 	 */
 	private static class TableRowComparator implements Comparator<List<String>> {
 		public int compare(List<String> o1, List<String> o2) {
 			// Compare according to the first column, which is the clanmate's
 			// name.
 			return o1.get(0).compareToIgnoreCase(o2.get(0));
 		}
 	}
 
 	/**
 	 * Settings are Properties that automatically load and store themselves into
 	 * files. Reads and writes ignore <tt>IOException</tt>s; the errors are
 	 * logged to Java Logging instead.
 	 */
 	private static class Settings extends Properties {
 		private static final long serialVersionUID = 1L;
 
 		private final File file;
 
 		public Settings(final File file) {
 			this.file = file;
 			try {
 				final InputStream in = new FileInputStream(file);
 				try {
 					load(in);
 				} finally {
 					in.close();
 				}
 			} catch (final IOException e) {
 				System.err.println("Settings file cannot be read; using no settings at all");
 				e.printStackTrace();
 			}
 		}
 
 		public void store() throws IOException {
 			final OutputStream out = new FileOutputStream(file);
 			try {
 				store(out, null);
 			} finally {
 				out.close();
 			}
 		}
 	}
 
 	private static void shortDelay() {
 		try {
 			Thread.sleep(45000);
 		} catch (final InterruptedException e) {
 			// don't care
 		}
 	}
 
 	/**
 	 * Clan high score scraper.
 	 * 
 	 * @author User:122.58.38.62, also known as User:Cursed Pyres
 	 */
 	public static class HSScraper {
 		private static final String clanHome = "http://services.runescape.com/m=clan-home/clan/";
 
 		public static final String strFormat = "http://services.runescape.com/m=clan-hiscores/members.ws?pageNum=%d&clanName=%s&pageSize=%d&ranking=-1";
 
 		private static final Pattern nameCleanup = Pattern.compile("[^-A-Za-z0-9_]");
 
 		public static final int PER_PAGE = 45;
 
 		public static List<ClanMember> getAll(String clan) throws IOException {
 			List<ClanMember> list = new ArrayList<ClanMember>();
 			HttpURLConnection http = (HttpURLConnection) new URL(clanHome + clan).openConnection();
 			http.setConnectTimeout(15000);
 			http.setReadTimeout(30000);
 			BufferedReader inLines = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
 			// Need to Open the Clan home to find the number of members before
 			// running
 			// Could run the name parser until it errors. This is a better
 			// solution (I THINK)
 			int members = -1;
 
 			String s = inLines.readLine();
 			while (s != null) {
 				if (s.startsWith("<a class=\"clanStats\" id=\"Clanstat_1")) {
 					// This is the members line.
 					String beginStr = "<span class=\"clanstatVal FlatHeader\">";
 					int beginIndex = s.indexOf(beginStr);
 					int endIndex = s.indexOf("</span>", beginIndex);
 					s = s.substring(beginIndex + beginStr.length(), endIndex);
 					members = Integer.parseInt(s.trim());
 					break;
 				}
 				s = inLines.readLine();
 			}
 			inLines.close();
 			if (members == -1) {
 				return list;
 			}
 			for (int i = 1; i <= (members + PER_PAGE - 1) / PER_PAGE; i++) {
 				// Open a Socket to the Page.
 				String built = String.format(strFormat, i, clan, PER_PAGE);
 				http = (HttpURLConnection) new URL(built).openConnection();
 				http.setConnectTimeout(15000);
 				http.setReadTimeout(30000);
 				inLines = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
 
 				s = inLines.readLine();
 				while (s != null) {
					if (s.endsWith("<div class=\"membersListRow\">")) {
 						inLines.readLine(); // dummy read
 						inLines.readLine(); // dummy read
 						String name = nameCleanup.matcher(stripTags(inLines.readLine())).replaceAll("&nbsp;");
 						String rank = stripTags(inLines.readLine());
 
 						while (!s.contains("totalXP")) {
 							s = inLines.readLine(); // dummy reads
 						}
 
 						// except the last one, which contains the EXP
 						String xp = stripTags(s).replace(",", "");
 						long xpVal = Long.parseLong(xp);
 						String kills = stripTags(inLines.readLine());
 						kills = kills.replace(",", "");
 						int killsVal = Integer.parseInt(kills);
 
 						list.add(new ClanMember(name, rank, false, xpVal, killsVal));
 					}
 
 					s = inLines.readLine();
 				}
 				inLines.close();
 			}
 
 			return list;
 		}
 
 		private static String stripTags(String str) {
 			return str.substring(str.indexOf(">") + 1, str.indexOf("</")).trim();
 		}
 	}
 
 	public static class ClanMember {
 		private final String name;
 
 		private final String rank;
 
 		private final boolean subscriber;
 
 		private final long xp;
 
 		private final int kills;
 
 		public ClanMember(String name, String rank, boolean subscriber, long xp, int kills) {
 			this.name = name;
 			this.rank = rank;
 			this.subscriber = subscriber;
 			this.xp = xp;
 			this.kills = kills;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public String getRank() {
 			return rank;
 		}
 
 		public boolean isSubscriber() {
 			return subscriber;
 		}
 
 		/**
 		 * Returns the number of experience points acquired in total by this
 		 * <tt>ClanMember</tt> while s/he has been in the clan.
 		 */
 		public long getExpGain() {
 			return xp;
 		}
 
 		public int getKills() {
 			return kills;
 		}
 	}
 
 	private static void postLog(MediaWiki wiki, Date runTime, Settings settings, StringWriter ss) {
 		while (true)
 			// retry loop
 			try {
 				wiki.createPageSection(wiki.startEdit(settings.getProperty("LogPage")), "Log for " + utcDateFormatter.format(runTime) + " " + utcTimeFormatter.format(runTime), "<pre>" + ss.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</pre>", true /* bot */, false /* minor */);
 				break;
 			} catch (Throwable t) {
 				System.err.println("Failed to post the log; retrying shortly");
 				shortDelay();
 			}
 	}
 
 	public static List<List<String>> parseDataTable(String text) throws ParseException {
 		List<Token> tokens = tokenizeDataTable(text);
 		int pos = 0;
 		// Optional Whitespace. Ignored.
 		while (pos < tokens.size() && (tokens.get(pos).type == TokenType.WHITESPACE || tokens.get(pos).type == TokenType.NEWLINE_AND_WHITESPACE))
 			pos++;
 		// Table start required here, if present. If the text is over, return an
 		// empty table.
 		if (pos >= tokens.size())
 			return new ArrayList<List<String>>();
 		else if (tokens.get(pos).type != TokenType.TABLE_START)
 			throw new ParseException("Start of table not found", tokens.get(pos).position);
 		else {
 			List<List<String>> result = new ArrayList<List<String>>();
 			ArrayList<String> activeRow = null; // first rowchange is optional
 			StringBuilder activeCell = null;
 			pos++;
 			// Optional Text/Newline/Whitespace. This is the style of the table
 			// and everything leading up to the first row or cell change.
 			// Ignored.
 			while (pos < tokens.size() && (tokens.get(pos).type == TokenType.TEXT || tokens.get(pos).type == TokenType.NEWLINE_AND_WHITESPACE || tokens.get(pos).type == TokenType.WHITESPACE))
 				pos++;
 			// From now on, read table specials.
 			loop: while (pos < tokens.size()) {
 				Token cur = tokens.get(pos);
 				switch (cur.type) {
 				case ROW_CHANGE:
 					pos++;
 					if (activeRow != null) {
 						String previousCell = activeCell.toString().trim();
 						activeRow.add(previousCell.length() > 0 ? previousCell : null);
 						result.add(activeRow);
 					}
 					activeRow = new ArrayList<String>();
 					// Optional Text/Newline/Whitespace. This is the style of
 					// the row. Ignored.
 					while (pos < tokens.size() && (tokens.get(pos).type == TokenType.TEXT || tokens.get(pos).type == TokenType.NEWLINE_AND_WHITESPACE || tokens.get(pos).type == TokenType.WHITESPACE))
 						pos++;
 					// No cell yet for the new line.
 					activeCell = null;
 					break;
 				case CELL_CHANGE:
 					pos++;
 					if (activeCell != null) {
 						if (activeRow == null) // First row did not have |-
 							activeRow = new ArrayList<String>();
 						String previousCell = activeCell.toString().trim();
 						activeRow.add(previousCell.length() > 0 ? previousCell : null);
 					}
 					if (activeCell == null)
 						activeCell = new StringBuilder();
 					else
 						activeCell.setLength(0);
 					// Optional Text/Newline/Whitespace. This is the style of
 					// the row. Ignored.
 					while (pos < tokens.size() && (tokens.get(pos).type == TokenType.TEXT || tokens.get(pos).type == TokenType.NEWLINE_AND_WHITESPACE || tokens.get(pos).type == TokenType.WHITESPACE)) {
 						activeCell.append(tokens.get(pos).content);
 						pos++;
 					}
 					break;
 				case TABLE_END:
 					pos++;
 					if (activeCell != null) {
 						if (activeRow == null)
 							activeRow = new ArrayList<String>();
 						String previousCell = activeCell.toString().trim();
 						activeRow.add(previousCell.length() > 0 ? previousCell : null);
 					}
 					if (activeRow != null) {
 						result.add(activeRow);
 					}
 					// Optional Newline/Whitespace. This is what's allowed after
 					// the table. Ignored.
 					while (pos < tokens.size() && (tokens.get(pos).type == TokenType.NEWLINE_AND_WHITESPACE || tokens.get(pos).type == TokenType.WHITESPACE))
 						pos++;
 					break loop;
 				}
 			}
 			if (pos > tokens.size())
 				throw new ParseException("End of table not found", text.length());
 			else if (pos < tokens.size())
 				throw new ParseException("Text exists after the end of the table", tokens.get(pos).position);
 			else
 				return result;
 		}
 	}
 
 	private static List<Token> tokenizeDataTable(String text) throws ParseException {
 		final List<Token> result = new ArrayList<Token>();
 		// Form the first token, WHITESPACE, if required.
 		int pos = 0;
 		while (pos < text.length()) {
 			int start = pos;
 			if (text.charAt(pos) == '\r' || text.charAt(pos) == '\n') {
 				pos++;
 				while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
 					pos++;
 				result.add(new Token(TokenType.NEWLINE_AND_WHITESPACE, text.substring(start, pos), start));
 			} else if (Character.isWhitespace(text.charAt(pos))) {
 				pos++;
 				while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
 					pos++;
 				result.add(new Token(TokenType.WHITESPACE, text.substring(start, pos), start));
 			} else if (text.charAt(pos) == '|') {
 				pos++;
 				if (pos < text.length() && text.charAt(pos) == '-') {
 					// Row change
 					result.add(new Token(TokenType.ROW_CHANGE, "|-", start));
 					pos++;
 				} else if (pos < text.length() && text.charAt(pos) == '+') {
 					throw new ParseException("|+ is not supported", start);
 				} else if (pos < text.length() && text.charAt(pos) == '}') {
 					// Table end
 					result.add(new Token(TokenType.TABLE_END, "|}", start));
 					pos++;
 				} else {
 					// Cell change
 					result.add(new Token(TokenType.CELL_CHANGE, "|", start));
 				}
 			} else if (text.charAt(pos) == '!') {
 				// Heading cell
 				pos++;
 				result.add(new Token(TokenType.CELL_CHANGE, "!", start));
 			} else if (text.charAt(pos) == '{') {
 				pos++;
 				if (pos < text.length() && text.charAt(pos) == '|') {
 					// Table start
 					pos++;
 					result.add(new Token(TokenType.TABLE_START, "{|", start));
 				} else {
 					result.add(new Token(TokenType.TEXT, "{", start));
 				}
 			} else {
 				// Text. Stop at ||, !! or \r/\n.
 				pos++;
 				while (pos < text.length() && !(text.charAt(pos) == '\r' || text.charAt(pos) == '\n')) {
 					if (pos + 1 < text.length() && ((text.charAt(pos) == '|' && text.charAt(pos + 1) == '|') || (text.charAt(pos) == '!' && text.charAt(pos + 1) == '!'))) {
 						// Inline change of cell. Flush text, add cell change,
 						// restart.
 						if (pos > start)
 							result.add(new Token(TokenType.TEXT, text.substring(start, pos), start));
 						result.add(new Token(TokenType.CELL_CHANGE, "||", pos));
 						pos += 2;
 						start = pos;
 					} else {
 						pos++;
 					}
 				}
 				if (pos > start)
 					result.add(new Token(TokenType.TEXT, text.substring(start, pos), start));
 			}
 		}
 
 		return result;
 	}
 
 	public static String createDataTable(List<List<String>> table) {
 		StringBuilder result = new StringBuilder("{|class=\"wikitable sortable\"");
 		for (String headerCell : table.get(0)) {
 			result.append("\r\n !").append(tableEscape(headerCell));
 		}
 		for (int rowIndex = 1; rowIndex < table.size(); rowIndex++) {
 			result.append("\r\n |-");
 			List<String> row = table.get(rowIndex);
 			// Bold the clanmate name.
 			result.append("\r\n |style=\"font-weight: bold\"|").append(row.get(0));
 			for (int columnIndex = 1; columnIndex < row.size(); columnIndex++) {
 				String cell = row.get(columnIndex);
 				result.append("\r\n |align=\"right\"|");
 				if (cell != null)
 					result.append(tableEscape(cell));
 			}
 		}
 		return result.append("\r\n |}").toString();
 	}
 
 	/**
 	 * Prevents -, + and } from combining with | to form table specials.
 	 * 
 	 * @param text
 	 *            The text to escape.
 	 * @return the escaped text
 	 */
 	private static String tableEscape(String text) {
 		if (text.length() > 0 && (text.charAt(0) == '-' || text.charAt(0) == '+' || text.charAt(0) == '}'))
 			return " " + text;
 		else
 			return text;
 	}
 
 	private static class Token {
 		public TokenType type;
 
 		public String content;
 
 		public int position;
 
 		public Token(TokenType type, String content, int position) {
 			this.type = type;
 			this.content = content;
 			this.position = position;
 		}
 	}
 
 	private static enum TokenType {
 		TABLE_START, ROW_CHANGE, CELL_CHANGE, NEWLINE_AND_WHITESPACE, WHITESPACE, TEXT, TABLE_END
 	}
 }
