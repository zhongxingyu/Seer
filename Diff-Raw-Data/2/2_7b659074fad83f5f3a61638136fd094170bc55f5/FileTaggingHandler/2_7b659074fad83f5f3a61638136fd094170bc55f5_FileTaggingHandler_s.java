 package jfmi.control;
 
 import java.util.Collection;
 import java.sql.SQLException;
 
 import jfmi.control.FileTag;
 import jfmi.control.FileTagging;
 import jfmi.dao.FileTaggingDAO;
 import jfmi.gui.GUIUtil;
 
 /** A TaggingHandler handles application logic concerned with adding, removing,
   or updating taggings in the database. Taggings represent a unique tagging
   of a file with a tag and optional comment.
   */
 public class FileTaggingHandler {
 
 	// PRIVATE INSTANCE Fields
 	private JFMIApp jfmiApp;
 	private FileTaggingDAO fileTaggingDAO;
 
 
 	//************************************************************	
 	// PUBLIC CLASS methods
 	//************************************************************	
 
 	/** Constructs a new FileTaggingHandler associated with the specified
 	  JFMIApp.
 	  @param jfmiApp_ the JFMIApp to associate this handler with
 	  @throws IllegalArgumentException if jfmiApp_ is null
 	  */
 	public FileTaggingHandler(JFMIApp jfmiApp_)
 	{
 		init(jfmiApp_);
 	}
 
 	/** Deletes a subset of FileTaggings from the underlying repository,
 	  identified by the specified FileTag collection.
 	  @param tags a collection of tags by which to target taggings for deletion
 	  @param showErrors if true, error messages will be displayed
 	  @return true if no problems occurred deleting the taggings
 	  */
 	public boolean deleteTaggingsByTags(Collection<FileTag> tags,
 										boolean showErrors)
 	{
 		boolean deleteSuccessful = true;
 
 		for (FileTag tag : tags) {
 			if (deleteTaggingsByTag(tag, showErrors) == false) {
 				deleteSuccessful = false;
 			}
 		}
 
 		return deleteSuccessful;
 	}
 
 	/** Deletes all FileTagging records which contain the specified tag from
 	  the underlying repository.
 	  @param tag the tag by which to identify records for deletion
 	  @param showErrors if true, error messages will be displayed
 	  @return true if no errors occurred
 	  */
 	public boolean deleteTaggingsByTag(FileTag tag, boolean showErrors)
 	{
 		try {
 			fileTaggingDAO.deleteByTag(tag.getTag().toString());
 			return true;
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An error occurred while accessing the repository to delete"
 					+ " file taggings with tag \"" + tag.getTag() + "\".",
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}	
 
 	/** Associates this handler with the specified JFMIApp.
 	  @param jfmiApp_ the JFMIApp to associate this handler with
 	  */
 	public void setJFMIApp(JFMIApp jfmiApp_)
 	{
 		if (jfmiApp_ == null) {
 			throw new IllegalArgumentException("jfmiApp_ cannot be null");
 		}
 
 		jfmiApp = jfmiApp_;
 	}
 
 	/** Updates a FileTagging in the repository.
 	  @param tagging the FileTagging to update
 	  @param showErrors if true, errors are displayed to the user
 	  @return true if no errors occurred
 	  */
 	public boolean updateFileTaggingInRepo(
 		FileTagging tagging, 
 		boolean showErrors
 	)
 	{
 		try {
 			boolean updated;
 		    updated = fileTaggingDAO.update(tagging, tagging.getTaggingId());
 
 			if (!updated && showErrors) {
 				GUIUtil.showErrorDialog(
 					"Failed to update the file tagging in the repository."
 				);
 			}
 
 			return updated;
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An repository error occurred while updating the file"
 					+ " tagging.",
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 	/** Updates a Collection of FileTaggings in the repository.
 	  @param taggings a Collection<FileTagging> to update
 	  @param showErrors if true, errors are displayed to the user
 	  @return true if no errors occurred
 	  */
 	public boolean updateFileTaggingsInRepo(
 		Collection<FileTagging> taggings,
 		boolean showErrors
 	)
 	{
 		boolean taggingsAreUpdated = true;
 
 		for (FileTagging ft : taggings) {
			if (updateFileTaggingInRepo(ft, true) == false) {
 				taggingsAreUpdated = false;
 			}
 		}		
 
 		return taggingsAreUpdated;
 	}
 
 
 	//************************************************************	
 	// PRIVATE CLASS methods
 	//************************************************************	
 
 	/** Initialize the handler.
 	  @param jfmiApp_ the JFMIApp to associate this handler with
 	  @throws IllegalArgumentException if jfmiApp_ is null
 	  */
 	private final void init(JFMIApp jfmiApp_)
 	{
 		setJFMIApp(jfmiApp_);
 		fileTaggingDAO = new FileTaggingDAO();
 	}
 
 }
