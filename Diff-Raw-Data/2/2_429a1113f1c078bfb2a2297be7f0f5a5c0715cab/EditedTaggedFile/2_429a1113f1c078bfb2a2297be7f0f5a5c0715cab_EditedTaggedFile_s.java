 package jfmi.control;
 
 import java.util.TreeSet;
 
 import jfmi.control.TaggedFile;
 import jfmi.control.FileTagging;
 
 /** An EditedTaggedFile represents a TaggedFile that is being edited by the
   user, but which has not been saved to the repository yet. It keeps track of
   the TaggedFile being edited, as well as the taggings that have been added
   to and removed from the TaggedFile.
   */
 public class EditedTaggedFile {
 
 	// PRIVATE INSTANCE Fields
 	private TaggedFile editedFile;
 	private TreeSet<FileTagging> addedTaggings;
 	private TreeSet<FileTagging> removedTaggings;
 	private TreeSet<FileTagging> updatedTaggings;
 
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	public EditedTaggedFile() {
 		editedFile = new TaggedFile();
 		addedTaggings = new TreeSet<FileTagging>();
 		removedTaggings = new TreeSet<FileTagging>();
		updateTaggings = new TreeSet<FileTagging>();
 	}
 
 	public EditedTaggedFile(TaggedFile editedFile_)
 	{
 		this(
 			editedFile_, 
 			new TreeSet<FileTagging>(), 
 			new TreeSet<FileTagging>(),
 			new TreeSet<FileTagging>()
 			);
 	}
 
 	public EditedTaggedFile(TaggedFile editedFile_,
 							TreeSet<FileTagging> added,
 							TreeSet<FileTagging> removed,
 							TreeSet<FileTagging> updated)
 	{
 		setEditedFile(editedFile_);
 		setAddedTaggings(added);
 		setRemovedTaggings(removed);
 	}
 
 	public TaggedFile getEditedFile()
 	{
 		return editedFile;
 	}
 
 	public TreeSet<FileTagging> getAddedTaggings()
 	{
 		return addedTaggings;
 	}
 
 	public TreeSet<FileTagging> getRemovedTaggings()
 	{
 		return removedTaggings;
 	}
 	
 	public TreeSet<FileTagging> getUpdatedTaggings()
 	{
 		return updatedTaggings;
 	}
 
 	public void setEditedFile(TaggedFile editedFile_)
 	{
 		editedFile = editedFile_;
 	}
 
 	public void setAddedTaggings(TreeSet<FileTagging> added)
 	{
 		addedTaggings = added;
 	}
 
 	public void setRemovedTaggings(TreeSet<FileTagging> removed)
 	{
 		removedTaggings = removed;
 	}
 	
 	public void setUpdatedTaggings(TreeSet<FileTagging> updated)
 	{
 		updatedTaggings = updated;
 	}
 
 }
