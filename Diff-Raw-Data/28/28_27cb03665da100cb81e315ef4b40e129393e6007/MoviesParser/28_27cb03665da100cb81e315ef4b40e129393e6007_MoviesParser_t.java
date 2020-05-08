 package de.softwarekollektiv.dbs.parser.imdb;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Map;
 
 import de.softwarekollektiv.dbs.dbcon.DbConnection;
 import de.softwarekollektiv.dbs.parser.Parser;
 
 public class MoviesParser extends AbstractImdbParser implements Parser {
 
	/*
 	private static final Pattern yearPattern = Pattern
 			.compile("(\\d{4})?-?((\\d{4})|\\?{4})");
	*/
	
 	private final DbConnection dbcon;
 	private final Map<String, Integer> movIdCache;
 
 	private PreparedStatement movStmt;
 	private static int nextId = 1;
 
 	public MoviesParser(DbConnection dbcon, String file,
 			Map<String, Integer> movIdCache) {
 		super(dbcon, file);
 		super.skipLines = 4;
 
 		this.dbcon = dbcon;
 		this.movIdCache = movIdCache;
 	}
 
 	/*
 	 * Each line is independend from any other It consists of one title, release
 	 * year and a Category seperated by \t+
 	 */
 	@Override
 	protected void newLine(String[] lineParts) throws SQLException {
 
 		String movieTitle = lineParts[0];
		// We ignore the movie release year
		// String movieReleaseString = null;
 		String movieCategory = null;
 
 		// normal case
 		if (lineParts.length == 3) {
			// movieReleaseString = lineParts[1];
 			movieCategory = lineParts[2];
 		}
 		// the release year field is missing
 		else if (lineParts.length == 2) {
 			movieCategory = lineParts[1];
 		// extra country field appears
 		} else if (lineParts.length == 4) {
 			movieCategory = lineParts[3];
 		}
 
 
 		int currentId = nextId++;
 		movStmt.setInt(1, currentId);
 		movStmt.setString(2, movieTitle);
 		movStmt.setString(3, movieCategory);
 		movStmt.addBatch();
 		movIdCache.put(movieTitle, currentId);
 	}
 
	/*
 	private String parseReleaseString(String input) {
 
 		Matcher m = yearPattern.matcher(input);
 		m.find();
 		String localRelease;
 		if (m.groupCount() >= 2) {
 			String group2 = m.group(2);
 			if (group2.equals("????")) {
 				localRelease = "2011";
 			} else
 				localRelease = group2;
 		} else {
 			localRelease = m.group(1);
 		}
 		return localRelease;
	}*/
 
 	@Override
 	protected void prepareStatements() throws SQLException {
 		movStmt = dbcon.getConnection().prepareStatement(
 				"INSERT INTO movies VALUES (?, ?, null, null, ?, null);");
 	}
 
 	@Override
 	protected void closeStatements() throws SQLException {
 		movStmt.close();
 	}
 
 	@Override
 	protected void executeBatchStatements() throws SQLException {
 		movStmt.executeBatch();
 	}
 }
