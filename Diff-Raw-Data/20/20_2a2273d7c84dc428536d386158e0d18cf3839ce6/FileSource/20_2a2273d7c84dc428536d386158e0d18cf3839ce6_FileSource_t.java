 package com.senselessweb.soundcloud.domain.sources;
 
 import java.io.File;
 
 import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 import com.senselessweb.soundcloud.util.IdentityUtils;
 
 /**
  * A media source represented by a file.
  * 
  * @author thomas
  *
  */
 public class FileSource extends AbstractMediaSource
 {
	
	/**
	 * The log
	 */
	private static final Log log = LogFactory.getLog(FileSource.class);
 
 	/**
 	 * The file
 	 */
 	private final File file;
 	
 	/**
 	 * @param title The title
 	 * @param file The file of this media source
 	 */
 	public FileSource(final String title, final File file)
 	{
 		super(title);
 		
		if (!file.isFile()) 
			log.warn("File \"" + file + "\" does not exist!");
 		this.file = new File(file.getAbsolutePath());
 	}
 	
 	/**
 	 * Returns the file.
 	 * 
 	 * @return The file. Never null.
 	 */
 	public File getFile()
 	{
 		return this.file;
 	}
 	
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return "FileSource[" + this.file + "]";
 	}
 	
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj)
 	{
 		if (!(obj instanceof FileSource)) return false;
 		final FileSource other = (FileSource) obj;
 		return IdentityUtils.areEqual(this.file, other.file);
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode()
 	{
 		return HashCodeBuilder.reflectionHashCode(this, true);
 	}
 
 }
