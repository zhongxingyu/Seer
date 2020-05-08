 package com.alexrnl.subtitlecorrector.io;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.alexrnl.commons.utils.object.AutoEquals;
 import com.alexrnl.commons.utils.object.AutoHashCode;
 import com.alexrnl.commons.utils.object.Field;
 
 /**
 * Class defining a subtitle format.<br >/
  * @author Alex
  */
 public class SubtitleFormat {
 	/** The name of the format */
 	private final String			name;
 	/** The extensions of subtitle encoded in that format */
 	private final Set<String>		extensions;
 	/** The reader for this subtitle's format */
 	private final SubtitleReader	reader;
 	/** The writer for this subtitle's format */
 	private final SubtitleWriter	writer;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param name
 	 *        the name of the subtitle format.
 	 * @param reader
 	 *        the reader for the format.
 	 * @param writer
 	 *        the writer for the format.
 	 * @param extensions
 	 *        the extensions of the subtitle in that format.
 	 */
 	public SubtitleFormat (final String name, final SubtitleReader reader,
 			final SubtitleWriter writer, final String... extensions) {
 		this(name, Arrays.asList(extensions), reader, writer);
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * @param name
 	 *        the name of the subtitle format.
 	 * @param extensions
 	 *        the extensions of the subtitle in that format.
 	 * @param reader
 	 *        the reader for the format.
 	 * @param writer
 	 *        the writer for the format.
 	 */
 	public SubtitleFormat (final String name, final Collection<String> extensions,
 			final SubtitleReader reader, final SubtitleWriter writer) {
 		super();
 		this.name = name;
 		this.extensions = Collections.unmodifiableSortedSet(new TreeSet<>(extensions));
 		this.reader = reader;
 		this.writer = writer;
 	}
 	
 	/**
 	 * Return the attribute name.
 	 * @return the attribute name.
 	 */
 	@Field
 	public String getName () {
 		return name;
 	}
 	
 	/**
 	 * Return the attribute extensions.
 	 * @return the attribute extensions.
 	 */
 	public Set<String> getExtensions () {
 		return extensions;
 	}
 	
 	/**
 	 * Return the attribute reader.
 	 * @return the attribute reader.
 	 */
 	public SubtitleReader getReader () {
 		return reader;
 	}
 	
 	/**
 	 * Return the attribute writer.
 	 * @return the attribute writer.
 	 */
 	public SubtitleWriter getWriter () {
 		return writer;
 	}
 	
 	@Override
 	public int hashCode () {
 		return AutoHashCode.getInstance().hashCode(this);
 	}
 	
 	@Override
 	public boolean equals (final Object obj) {
 		if (!(obj instanceof SubtitleFormat)) {
 			return false;
 		}
 		return AutoEquals.getInstance().compare(this, (SubtitleFormat) obj);
 	}
 	
 	@Override
 	public String toString () {
 		return getName() + " " + extensions;
 	}
 	
 }
