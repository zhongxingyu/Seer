 package com.gentics.cr.lucene.information;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.RAMDirectory;
 
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.util.Constants;
 
 /**
  * Information entry for a special directory such as the DYM or 
  * autocomplete directory.
  * @author Christopher
  *
  */
 public class SpecialDirectoryInformationEntry {
 	/**
 	 * Logger.
 	 */
 	protected static final Logger LOG = Logger
 	.getLogger(SpecialDirectoryInformationEntry.class);
 	/**
 	 * directory.
 	 */
 	private Directory directory;
 	/**
 	 * identifyer.
 	 */
 	private String id;
 	/**
 	 * Constructor.
 	 * @param dir directory
 	 */
 	public SpecialDirectoryInformationEntry(final Directory dir) {
 		directory = dir;
 		id = createDirectoryIdentifyer(dir);
 	}
 	
 	/**
 	 * Create a identifyer.
 	 * @param dir directory.
 	 * @return identifyer.
 	 */
 	public static String createDirectoryIdentifyer(final Directory dir) {
 		String id = "";
 		if (dir instanceof FSDirectory) {
			id = ((FSDirectory) dir).getFile().getPath();
 		} else if (dir instanceof RAMDirectory) {
 			id = "RAM_" + dir.toString();
 		} else {
 			id = "UNKNOWN" + dir;
 		}
 		return id;
 	}
 	/**
 	 * Get Identifyer.
 	 * @return identifyer.
 	 */
 	public final String getId() {
 		return id;
 	}
 	
 	/**
 	 * Get the directory size in bytes.
 	 * @return size in bytes
 	 */
 	public final long indexSize() {
 		long size = 0;
 		if (directory instanceof FSDirectory) {
 			size = FileUtils.sizeOfDirectory(
					((FSDirectory) directory).getFile());
 		} else if (directory instanceof RAMDirectory) {
 			size = ((RAMDirectory) directory).sizeInBytes();
 		}
 		return size;
 	}
 	
 	/**
 	 * get the index size in MegaBytes.
 	 * @return index size in MegaBytes
 	 */
 	public final double sizeMB() {
 		return indexSize() * Constants.MEGABYTES_PER_BYTE;
 	}
 
 	/**
 	 * Get the time the directory was modified.
 	 * For RAMDirectories this is always the current time.
 	 * @return last modified
 	 */
 	public final Date lastModified() {
 		long date = 0;
 		if (directory instanceof FSDirectory) {
			date = ((FSDirectory) directory).getFile().lastModified();
 		} else if (directory instanceof RAMDirectory) {
 			date = System.currentTimeMillis();
 		}
 		return new Date(date);
 	}
 
 	/**
 	 * Check if the index is optimized.
 	 * @return true if optimized.
 	 */
 	public final boolean isOptimized() {
 		boolean ret = false;
 		IndexReader reader	= null;
 			try {
 				reader = IndexReader.open(directory);
 				ret = reader.isOptimized();
 			} catch (IOException ex) {
 				LOG.error("IOException happened during test of index. ", ex);
 			} finally {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					LOG.error("IOException happened during test of index. ", e);
 				}
 			}
 			
 		return ret;
 	}
 	
 	/**
 	 * Get the number of documents in the index.
 	 * @return number of docs
 	 */
 	public final int getDocCount() {
 		IndexReader reader	= null;
 		int count = 0;
 		try {
 			reader = IndexReader.open(directory);
 			count = reader.numDocs();
 		} catch (IOException ex) {
 			LOG.error("IOException happened during test of index. ", ex);
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				LOG.error("IOException happened during test of index. ", e);
 			}
 		}
 		
 		return count;
 	}
 	
 	/**
 	 * Check if directory is locked.
 	 * @return true if locked.
 	 */
 	public final boolean isLocked() {
 		boolean ret = false;
 		try {
 			ret = IndexWriter.isLocked(directory);
 		} catch (IOException ex) {
 			LOG.error("IOException happened during test of index. ", ex);
 		} 
 			
 		return ret;
 	}
 }
