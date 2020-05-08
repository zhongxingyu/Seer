 package com.alexrnl.subtitlecorrector.common;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Class describing a simple subtitle.
  * @author Alex
  */
 public class Subtitle implements Comparable<Subtitle> {
 	/** Logger */
 	private static Logger	lg	= Logger.getLogger(Subtitle.class.getName());
 	
 	/** Timestamp (in milliseconds) for beginning of subtitle display */
 	private long			begin;
 	/** Timestamp (in milliseconds) for end of subtitle display */
 	private long			end;
 	/** The content of the subtitle */
 	private String			content;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default constructor.
 	 */
 	public Subtitle () {
 		this(0, 0, null);
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * @param begin
 	 *        the beginning of the subtitle display (in milliseconds).
 	 * @param end
 	 *        the end of the subtitle display (in milliseconds).
 	 * @param content
 	 *        the content of the subtitle.
 	 */
 	public Subtitle (final long begin, final long end, final String content) {
 		super();
 		this.begin = begin;
 		this.end = end;
 		this.content = content;
 		
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine(this.getClass().getSimpleName() + " created: " + toString());
 		}
 	}
 	
 	/**
 	 * Return the attribute begin.
 	 * @return the attribute begin.
 	 */
 	public long getBegin () {
 		return begin;
 	}
 	
 	/**
 	 * Set the attribute begin.
 	 * @param begin
 	 *        the attribute begin.
 	 */
 	public void setBegin (final long begin) {
 		this.begin = begin;
 	}
 	
 	/**
 	 * Return the attribute end.
 	 * @return the attribute end.
 	 */
 	public long getEnd () {
 		return end;
 	}
 	
 	/**
 	 * Set the attribute end.
 	 * @param end
 	 *        the attribute end.
 	 */
 	public void setEnd (final long end) {
 		this.end = end;
 	}
 	
 	/**
 	 * Return the attribute content.
 	 * @return the attribute content.
 	 */
 	public String getContent () {
 		return content;
 	}
 	
 	/**
 	 * Set the attribute content.
 	 * @param content
 	 *        the attribute content.
 	 */
 	public void setContent (final String content) {
 		this.content = content;
 	}
 	
 	/**
 	 * Computes the display duration of the subtitle.
 	 * @return the duration.
 	 */
 	public long getDuration () {
 		return end - begin;
 	}
 	
 	/**
 	 * Check the validity of the subtitle.<br />
 	 * A subtitle is valid if the end time is greater (or equal) to the begin time.
 	 * @return <true>true</true> if the subtitle is valid.
 	 */
 	public boolean isValid () {
		return begin < end;
 	}
 	
 	/**
 	 * Return the text representation of the subtitle as follow:
 	 * 
 	 * <pre>
 	 * [begin, end] content
 	 * </pre>
 	 */
 	@Override
 	public String toString () {
 		return "[" + begin + ", " + end + "] " + content;
 	}
 	
 	/**
 	 * Compare the subtitles based on their begin times.
 	 */
 	@Override
 	public int compareTo (final Subtitle sub) {
 		return Long.valueOf(begin).compareTo(sub.getBegin());
 	}
 	
 }
