 package com.alexrnl.subtitlecorrector.io.subrip;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.subtitlecorrector.common.Subtitle;
 import com.alexrnl.subtitlecorrector.common.SubtitleFile;
 import com.alexrnl.subtitlecorrector.io.SubtitleWriter;
 
 /**
  * Writer for the SubRip format.<br />
  * @author Alex
  */
 public class SubRipWriter extends SubtitleWriter {
 	/** Logger */
 	private static Logger			lg	= Logger.getLogger(SubRipWriter.class.getName());
 	
 	/** The date formatter */
 	private final SimpleDateFormat	dateFormat;
 	/** The subtitle counter (required by SubRip format) */
 	private Integer					subtitleCounter;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default constructor, uses UTF-8 for writing the file.
 	 */
 	public SubRipWriter () {
 		this(StandardCharsets.UTF_8);
 	}
 
 	/**
 	 * Constructor #2.<br />
 	 * @param charSet
 	 *        the character to use for this writer.
 	 */
 	public SubRipWriter (final Charset charSet) {
 		super(charSet);
 		dateFormat = new SimpleDateFormat(SubRip.SUBRIP_DATE_FORMAT);
 		subtitleCounter = null;
 	}
 	
 	@Override
 	protected void writeHeader (final SubtitleFile file, final BufferedWriter writer) throws IOException {
 		if (subtitleCounter != null) {
 			throw new IllegalStateException("It seems that the previous file was not finished fully writen");
 		}
 		subtitleCounter = 0;
 	}
 
 	@Override
 	protected void writeFooter (final SubtitleFile file, final BufferedWriter writer) throws IOException {
 		if (lg.isLoggable(Level.INFO)) {
 			lg.info("Successfully writen " + subtitleCounter + " subtitles");
 		}
 		subtitleCounter = null;
 	}
 
 	@Override
 	protected void writeSubtitle (final Subtitle subtitle, final BufferedWriter writer) throws IOException {
		writer.write(++subtitleCounter);
 		writer.write(System.lineSeparator());
 		writer.write(dateFormat.format(subtitle.getBegin()));
 		writer.write(SubRip.SUBRIP_DATE_SEPARATOR);
 		writer.write(dateFormat.format(subtitle.getEnd()));
 		writer.write(System.lineSeparator());
 		writer.write(subtitle.getContent());
 		writer.write(System.lineSeparator());
 	}
 }
