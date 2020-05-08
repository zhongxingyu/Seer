 package jfmi.control;
 
 import java.io.File;
 import java.util.TreeSet;
 
import jfmi.dao.FileTaggingComparator;
 
 /** Represents a file in the file system, along with its associated taggings (if
   any).
   */
 public class TaggedFile implements Comparable<TaggedFile> {
 
 	// PRIVATE INSTANCE Fields
 	private int fileId;
 	private File file;
 	private TreeSet<FileTagging> fileTaggings;
 
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	/** Construct a default TaggedFile with negative id, an empty path, "", and
 	  null set of taggings.
 	  */
 	public TaggedFile()
 	{
 		this(-1, "", null);
 	}
 
 	/** Construct a TaggedFile with the specified id, path, and set of
 	  taggings.
 	  @param id the new object's id
 	  @param path path of the new object
 	  @param taggings set of the file's taggings
 	  */
 	public TaggedFile(int id, String path, TreeSet<FileTagging> taggings)
 	{
 		this(id, new File(path), taggings);	
 	}
 
 	/** Construct a TaggedFile with the specified id, path, and set of
 	  taggings.
 	  @param id the new object's id
 	  @param file File which has the path of the new object
 	  @param taggings set of the file's taggings
 	  */
 	public TaggedFile(int id, File file, TreeSet<FileTagging> taggings)
 	{
 		setFileId(id);
 		setFile(file);
 		setFileTaggings(taggings);
 	}
 
 	/** Compares this instance against another TaggedFile for equality.
 	  @param o a FileTag to compare against
 	  @return true if this instance is equal to the argument
 	  */
 	public boolean equals(TaggedFile o)
 	{
 		boolean taggingsEqual;
 
 		if (fileTaggings == null || o.fileTaggings == null) {
 			taggingsEqual = fileTaggings == null && o.fileTaggings == null;
 		} else {
 			taggingsEqual = fileTaggings.equals(o.fileTaggings);	
 		}
 
 		return fileId == o.fileId && file.equals(o.file) && taggingsEqual;
 	}
 
 	/** Access the file field.
 	  @return the instance's file field
 	  */
 	public File getFile()
 	{
 		return file;
 	}
 
 	/** Access the fileId field.
 	  @return the file's integer id
 	  */
 	public int getFileId()
 	{
 		return fileId;
 	}
 
 	/** Access the file name.
 	  @return the String value of the file's name
 	  */
 	public String getFileName() 
 	{
 		return file.getName();
 	}
 
 	/** Access the file path.
 	  @return the String value of the file's path
 	  */
 	public String getFilePath()
 	{
 		return file.getPath();
 	}
 
 	/** Returns this TaggedFile's associated tags as a String.
 	  @return the tags associated with this file as a String
 	  */
 	public String getFileTagsAsString()
 	{
 		StringBuilder str = new StringBuilder("");
 
 		for (FileTagging ftagging : fileTaggings) {
 			str.append(" [");
 			str.append(ftagging.getTag());
 			str.append("] ");
 		}
 
 		return str.toString();
 	}
 
 	/** Return a reference to this TaggedFile's taggings.
 	  @return a TreeSet<FileTagging> of this file's taggings, may be null
 	  */
 	public TreeSet<FileTagging> getFileTaggings()
 	{
 		return fileTaggings;
 	}
 
 	/** Returns the TaggedFile's set of FileTaggings as an array.
 	  @return an array of this file's taggings - null if none exist
 	  */
 	public FileTagging[] getFileTaggingsAsArray()
 	{
 		if (fileTaggings == null || fileTaggings.isEmpty()) {
 			return null;
 		}
 
 		return fileTaggings.toArray(new FileTagging[0]);
 	}
 
 	/** Sets this instance's file field.
 	  @param file File to use as this instance's - can be null
 	  */
 	public void setFile(File file)
 	{
 		this.file = file;
 	}
 
 	/** Sets the file id.
 	  @param id integer to use as this file's id
 	  */
 	public void setFileId(int id)
 	{
 		fileId = id;
 	}
 
 	/** Sets this instance's file path.
 	  @param path String whose value is the desired path
 	  */
 	public void setFilePath(String path)
 	{
 		setFile(new File(path));
 	}
 
 	/** Sets the set of taggings for this file.
 	  @param taggingSet set of taggings for this file - can be null
 	  */
 	public void setFileTaggings(TreeSet<FileTagging> taggingSet)
 	{
 		fileTaggings = taggingSet;
 	}
 
 	/** Sets the set of taggings for this file from an array.
 	  @param taggingArray an array of FileTagging objects - null sets the
 	  		taggings to null
 	  */
 	public void setFileTaggings(FileTagging[] taggingArray)
 	{
 		if (taggingArray == null) {
 			fileTaggings = null;
 		} else {
			if (fileTaggings == null) {
				fileTaggings = new TreeSet<FileTagging>(
													new FileTaggingComparator()
													);
			}

 			fileTaggings.clear();

 			for (FileTagging tagging : taggingArray) {
 				fileTaggings.add(tagging);
 			}
 		}
 	}
 
 	/**
 	  @return a String representation of the TaggedFile
 	  */
 	public String toString()
 	{
 		return getFilePath();
 	}
 
 
 	//************************************************************
 	// IMPLEMENTATION of Comparable<TaggedFile>
 	//************************************************************
 
 	/** This function implements a natural ordering for instances of the
 	  TaggedFile class. Ordering is performed first on the file path, then on
 	  the file id. FileTaggings are not considered for ordering, unless they
 	  are equal.
 	  @param o a TaggedFile to compare this instance against for ordering
 	  @return -1, 0, 1 as this instance is less than, equal to, greater than
 	  		the argument
 	  */
 	public int compareTo(TaggedFile o)
 	{
 		if (this.equals(o)) {
 			return 0;
 		}
 
 		if (file.getPath().compareTo(o.file.getPath()) == 0) {
 
 			if (((Integer)fileId).compareTo(o.fileId) == 0) {
 				
 				/* If fileId and file are both equal, then we do not care
 				   about ordering based on the taggings. So this instance is
 				   always first. */
 				return -1;
 
 			} else {
 				return ((Integer)fileId).compareTo(o.fileId);
 			}
 
 		} else {
 			return file.getPath().compareTo(o.file.getPath());
 		}
 	}
 
 }
 
