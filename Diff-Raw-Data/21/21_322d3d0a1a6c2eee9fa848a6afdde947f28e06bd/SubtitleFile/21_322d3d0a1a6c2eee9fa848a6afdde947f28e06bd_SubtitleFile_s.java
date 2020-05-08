 package com.alexrnl.subtitlecorrector.common;
 
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.utils.CollectionUtils;
 
 /**
  * Represent a single subtitle file.<br />
  * @author Alex
  */
 public class SubtitleFile extends TreeSet<Subtitle> {
 	/** Logger */
 	private static Logger		lg					= Logger.getLogger(SubtitleFile.class.getName());
 	
 	/** Serial Version UID */
 	private static final long	serialVersionUID	= -5783605599009372323L;
 	
 	/** The actual file which is represented */
	private final Path			file;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param file
 	 *        the file backed-up by this instance.
 	 */
 	public SubtitleFile (final Path file) {
 		super();
		this.file = file;
 		
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine(this.getClass().getSimpleName() + " created: " + toString());
 		}
 	}
 	
 	/**
 	 * Return the file represented by this instance.
 	 * @return the file.
 	 */
 	public Path getFile () {
		return file;
 	}
 	
 	/**
 	 * Update the subtitle so the ordering is updated if necessary.
 	 */
 	public void update () {
 		if (CollectionUtils.isSorted(this)) {
 			return;
 		}
 
 		final List<Subtitle> tmp = new ArrayList<>(this);
 		this.clear();
 		for (final Subtitle subtitle : tmp) {
 			this.add(subtitle);
 		}
 	}
 }
