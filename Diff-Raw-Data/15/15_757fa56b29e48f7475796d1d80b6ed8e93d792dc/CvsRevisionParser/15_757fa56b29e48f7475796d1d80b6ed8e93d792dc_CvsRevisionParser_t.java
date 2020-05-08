 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile$ 
 	Created on $Date$ 
 */
 
 package net.sf.statcvs.input;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.util.LookaheadReader;
 
 /**
  * Parses all revisions of one file.
  * 
  * @author Anja Jentzsch
  * @author Richard Cyganiak
 * @author Felipe Hoffa (fix for parsing new cvs log date format)
  * @version $Id$
  */
 public class CvsRevisionParser {
 
 	private static Logger logger
 			= Logger.getLogger(CvsRevisionParser.class.getName());
 
 	/**
 	 * Revision Delimiter in CVS log file
 	 */
 	public static final String REVISION_DELIMITER =
 			"----------------------------";
 	/**
 	 * File Delimiter in CVS log file
 	 */
 	public static final String FILE_DELIMITER
 			= "======================================"
 			+ "=======================================";
 
 	private static final String LOG_TIMESTAMP_FORMAT =
 		"yyyy/MM/dd HH:mm:ss zzz";
	private static final String LOG_TIMESTAMP_FORMAT_NEW = 
		"yyyy-MM-dd HH:mm:ss Z zzz";
 	private static final Locale LOG_TIMESTAMP_LOCALE = Locale.US;
 	private static SimpleDateFormat logTimeFormat =
 		new SimpleDateFormat(LOG_TIMESTAMP_FORMAT, LOG_TIMESTAMP_LOCALE);
	private static SimpleDateFormat logTimeFormatNew = 
		new SimpleDateFormat(LOG_TIMESTAMP_FORMAT_NEW, LOG_TIMESTAMP_LOCALE);
 	private LookaheadReader logReader;
 	private CvsLogBuilder builder;
 	private boolean fileDone = false;
 	private RevisionData revision;
 
 	/**
 	 * Default Constructor CvsRevisionParser.
 	 * @param logReader the reader
 	 * @param builder a <tt>Builder</tt> for the creation process
 	 */
 	public CvsRevisionParser(LookaheadReader logReader, CvsLogBuilder builder) {
 		this.logReader = logReader;
 		this.builder = builder;
 	}
 
 	/**
 	 * Parses the list of revisions for one file
 	 * @throws LogSyntaxException on syntax error in the log
 	 * @throws IOException on read error
 	 */
 	public void parse() throws LogSyntaxException, IOException {
 		if (FILE_DELIMITER.equals(logReader.getCurrentLine())) {
 			return;
 		}
 		logReader.getNextLine();
 		do {
 			revision = new RevisionData();
 			parseRevision();
 			builder.buildRevision(revision);
 		} while (!fileDone);
 	}
 
 	private void parseRevision() throws IOException, LogSyntaxException {
 		if (!isNewRevisionLine(logReader.getCurrentLine())) {
 			throw new LogSyntaxException(
 				"expected 'revision' but found '" + logReader.getCurrentLine()
 						+ "' in line " + logReader.getLineNumber());
 		}
 		String revNo = logReader.getCurrentLine().substring("revision ".length());
 		revision.setRevisionNumber(revNo);
 		parseDateLine(logReader.getNextLine());
 		if (logReader.getNextLine().startsWith("branches:")) {
 			logReader.getNextLine();
 		}
 		StringBuffer comment = new StringBuffer();
 		while (true) {
 			String line = logReader.getCurrentLine();
 			if (REVISION_DELIMITER.equals(line)) {
 				String next = logReader.getNextLine();
 				if (isNewRevisionLine(next)) {
 					revision.setComment(comment.toString());
 					return;
 				}
 			} else if (FILE_DELIMITER.equals(line)) {
 				String next = logReader.getNextLine();
 				if (next == null || "".equals(next)) {
 					revision.setComment(comment.toString());
 					fileDone = true;
 					return;
 				}
 			} else {
 				logReader.getNextLine();
 			}
 			if (comment.length() != 0) {
 				comment.append('\n');
 			}
 			comment.append(line);
 		}
 	}
 
 	private void parseDateLine(String line)
 			throws IOException, LogSyntaxException {
 
 		// date: 2000/06/19 04:56:21;  author: somebody;  state: Exp;  lines: +114 -45
 
 		// get the creation date
 		int endOfDateIndex = line.indexOf(';', 6);
 		String dateString = line.substring(6, endOfDateIndex) + " GMT";
 		Date date = convertFromLogTime(dateString);
 		if (date == null) {
 			throw new LogSyntaxException(
 					"unexpected date format in line " + logReader.getLineNumber());
 		}
 		revision.setDate(date);
 
 		// get the author name
 		int endOfAuthorIndex = line.indexOf(';', endOfDateIndex + 1);
 		revision.setLoginName(
 				line.substring(endOfDateIndex + 11, endOfAuthorIndex));
 
 		// get the file state (because this revision might be "dead")
 		String fileState = line.substring(endOfAuthorIndex + 10,
 				line.indexOf(';', endOfAuthorIndex + 1));
 		if (isDeadState(fileState)) {
 			revision.setStateDead();
 			return;
 		}
 		revision.setStateExp();
 
 		// is this an initial revision?
 		int beginOfLinesIndex = line.indexOf("lines:", endOfAuthorIndex + 1);
 		if (beginOfLinesIndex < 0) {
 			return;
 		}
 		
 		// get lines added and lines removed
 		StringTokenizer st =
 				new StringTokenizer(line.substring(beginOfLinesIndex + 8));
 		int linesAdded = Integer.parseInt(st.nextToken());
 		String removed = st.nextToken();
 		if (removed.indexOf(';') >= 0) {
 			removed = removed.substring(0, removed.indexOf(';'));
 		}
 		int linesRemoved = -Integer.parseInt(removed);
 		revision.setLines(linesAdded, linesRemoved);
 	}
 
 	private boolean isNewRevisionLine(String line) {
 		return line.startsWith("revision ");
 	}
 
 	private boolean isDeadState(String state) throws IOException {
 		if ("dead".equals(state)) {
 			return true;
 		}
 		if ("Exp".equals(state)) {
 			return false;
 		}
 		logger.warning("unknown file state '" + state + "' at line "
 				+ logReader.getLineNumber());
 		return false;
 	}
 
 
 	/**
 	 * Returns a date from a given modTime String of a cvs logfile
 	 * @param modTime modTime String of a cvs logfile
 	 * @return Date date from a given modTime String of a cvs logfile
 	 */
 	private static Date convertFromLogTime(String modTime) {
 		try {
			if (modTime.indexOf('-') > 0) {
				return logTimeFormatNew.parse(modTime);
			} else {
			    return logTimeFormat.parse(modTime);
			}
 		} catch (ParseException e) {
 			// fallback is to return null
 			return null;
 		}
 	}
 }
