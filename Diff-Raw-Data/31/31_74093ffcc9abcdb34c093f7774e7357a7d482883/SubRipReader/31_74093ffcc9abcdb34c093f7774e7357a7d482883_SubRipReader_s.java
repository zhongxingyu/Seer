 package com.alexrnl.subtitlecorrector.io.subrip;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.subtitlecorrector.common.Subtitle;
 import com.alexrnl.subtitlecorrector.common.SubtitleFile;
 import com.alexrnl.subtitlecorrector.io.SubtitleReader;
 
 /**
  * Reader for the SubRip format.<br />
  * @author Alex
  */
 public class SubRipReader extends SubtitleReader {
 	/** Logger */
 	private static Logger			lg	= Logger.getLogger(SubRipReader.class.getName());
 	
 	/** The date formatter */
 	private final SimpleDateFormat	dateFormatter;
 	
 	/**
 	 * Constructor #1.<br />
 	 */
 	public SubRipReader () {
 		super();
 		dateFormatter = new SimpleDateFormat(SubRip.SUBRIP_DATE_FORMAT);
 	}
 
 
 
 	@Override
 	protected Subtitle readSubtitle (final SubtitleFile subtitleFile, final BufferedReader reader)
 			throws IOException {
 		final long begin;
 		final long end;
 		final StringBuilder content = new StringBuilder();
 		String currentLine;
 		
 		// Removing empty lines
 		do {
			currentLine = reader.readLine().trim();
		} while (!currentLine.isEmpty());
 		// Get the subtitle number
		final int subtitleIndex = Integer.parseInt(currentLine);
 		if (lg.isLoggable(Level.INFO)) {
 			lg.info("Reading subtitle#" + subtitleIndex);
 		}
 		
 		// Get the begin and end dates of the subtitle
 		currentLine = reader.readLine().trim();
 		final String[] dates = currentLine.split(SubRip.SUBRIP_DATE_SEPARATOR);
 		if (dates.length < 2) {
 			throw new IOException("Could not parse " + currentLine + " as a valid line in the subrip format");
 		}
 		try {
 			begin = dateFormatter.parse(dates[0]).getTime();
 			end = dateFormatter.parse(dates[1]).getTime();
 		} catch (final ParseException e) {
 			lg.warning("Could not parse either " + dates[0] + " or " + dates[1] + " as a date");
 			throw new IOException("Problem while parsing a date", e);
 		}
 		
 		// Get the content of the subtitle
 		do {
 			currentLine = reader.readLine().trim();
			content.append(currentLine);
 		} while (!currentLine.isEmpty());
 		
 		return new Subtitle(begin, end, content.toString());
 	}
 }
