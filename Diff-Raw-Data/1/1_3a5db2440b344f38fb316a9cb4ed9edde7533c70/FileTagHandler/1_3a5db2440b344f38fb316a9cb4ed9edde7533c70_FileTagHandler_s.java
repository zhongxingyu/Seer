 package jfmi.control;
 
 import javax.swing.JOptionPane;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Vector;
 
 import jfmi.control.FileTag;
 import jfmi.dao.FileTagDAO;
 import jfmi.gui.FileTagHandlerDialog;
 import jfmi.gui.GUIUtil;
 
 /** A FileTagHandler handles application logic concerned with adding, removing,
   and updating tag records in the database. A FileTagHandler uses its parent
   JFMIApp's database to save tag changes, and a FileTagHandlerDialog to interface
   with a user.
   */
 public class FileTagHandler {
 
 	// PRIVATE INSTANCE Methods
 	private JFMIApp jfmiApp;
 	private FileTagHandlerDialog tagHandlerDialog;
 
 	private FileTagDAO fileTagDAO;
 
 	private Vector<FileTag> fileTagData;
 
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	/** Construct a FileTagHandler with the specified JFMIApp as its parent.
 	  @param jfmiApp_ JFMIApp reference used as this instance's parent.
 	  */
 	public FileTagHandler(JFMIApp jfmiApp_) 
 	{
 		setJFMIApp(jfmiApp_);
 
 		fileTagDAO = new FileTagDAO();
 
 		fileTagData = null;
 
 		tagHandlerDialog = new FileTagHandlerDialog(jfmiApp.getJFMIGUI(), this);
 		tagHandlerDialog.setVisible(false);
 	}
 
 	/** Adds a tag to the repository.
 	  @param tag the tag to attempt to store
 	  @param showErrors if true, error messages are displayed
 	  @return true if the tag is added successfully
 	  */
 	public boolean addTagToRepo(FileTag tag, boolean showErrors)
 	{
 		try {
 			boolean created = fileTagDAO.create(tag);
 
 			if (created == false && showErrors) {
 				GUIUtil.showErrorDialog(
 					"Failed to create the new tag. Make sure the tag does not"
 					+ " already exist."
 				);
 			}
 			
 			return created;
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An error occurred while attempting to create the tag "
 					+ "\"" + tag.getTag() + "\" in the repository.",
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 	/** Begins an interaction with the user that allows them to enter the
 	  value for a new FileTag and store it in the repository.
 	  */
 	public void beginAddTagInteraction()
 	{
 		String newTag = getNewTagFromUser();
 
 		if (newTag == null) {
 			return;	// user cancelled interaction
 		}
 
 		FileTag tag = new FileTag(newTag);
 		addTagToRepo(tag, true);
 		updateDataAndGUI(true);
 	}
 
 	/** Begins an interaction with the user which allows them to remove
 	  tags from the repository. 
 	  @param tags a list of tags to be deleted from the repository
 	  */
 	public void beginDeleteTagsInteraction(List<FileTag> tags)
 	{
 		if (tags == null) {
 			return;
 		}
 
 		String msg = "Are you sure you want to delete the selected tags?";
 
 		/* We delete the tags from the repo. We then update handler data, as
 		   well as the the application's FileHandler's data.
 		   */
 		if (tagHandlerDialog.getUserConfirmation(msg)) {
 			deleteTagsFromRepo(tags, true);
 			updateDataAndGUI(true);
 			jfmiApp.getFileHandler().updateDataAndGUI(true);
 		}
 	}
 
 	/** Begins an interaction with the user which allows them to enter
 	  a new value for a tag they have selected.
 	  @param editMe the file tag the user has selected to edit
 	  */
 	public void beginEditTagInteraction(FileTag editMe)
 	{
 		// Ask GUI to get new value from user
 		String editVal = getEditedTagFromUser(editMe.getTag());
 
 		if (editVal == null || editMe.getTag().equals(editVal)) {
 			return;	// return if user cancelled, or did not edit tag
 		}
 
 		String oldTag = editMe.getTag();
 		editMe.setTag(editVal);
 
 		if (updateTagInRepo(oldTag, editMe, true)) {
 			updateDataAndGUI(true);
 		 	jfmiApp.getFileHandler().updateDataAndGUI(true);
 		}
 	}
 
 	/** When called, displays an interface to allow the user to
 	  add/remove tags.
 	  */
 	public void beginManageTagsInteraction()
 	{
 		updateDataAndGUI(true);
 		tagHandlerDialog.setVisible(true);
 	}
 
 	/** Deletes a collection of tags from the repository.
 	  @param tags the FileTags to be deleted
 	  @param showErrors if true, error messages are displayed
 	  @return true if no problems occurred deleting the tags 
 	  */
 	public boolean deleteTagsFromRepo(Collection<FileTag> tags,
 										boolean showErrors)
 	{
 		if (tags == null) {
 			return true;	// technically 0 tags were deleted successfully
 		}
 
 		boolean allWereDeleted = true;
 
 		for (FileTag fileTag : tags) {
 			if (deleteTagFromRepo(fileTag, showErrors) == false) {
 				allWereDeleted = false;
 			}
 		}
 
 		return allWereDeleted;
 	}
 
 	/** Deletes a file tag from the underlying repository.
 	  @param tag the file tag to delete
 	  @param showErrors if true, error messages are displayed
 	  @return true if the tag was deleted successfully
 	  */
 	public boolean deleteTagFromRepo(FileTag tag, boolean showErrors)
 	{
 		try {
 			return fileTagDAO.delete(tag);
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An error occurred with the repository while attempting to"
 					+ " delete the tag \"" + tag.getTag() + "\".",
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 	/** Accessor for the fileTagData field. This may be null. The method
 	  readFileTagDataFromRepo() should be called before this method.
 	  @return Access to the list of tag records this instance has retrieved
 	  		from the application repository.
 	  */
 	public List<FileTag> getFileTagData()
 	{
 		return fileTagData;
 	}
 
 	/** Accessor for the fileTagData field. This may be null. The method
 	  readFileTagDataFromRepo() should be called before this method.
 	  @return an array of file tags from the repository 
 	  */
 	public FileTag[] getFileTagDataAsArray()
 	{
 		if (fileTagData == null) {
 			return null;
 		} else {
 			return fileTagData.toArray(new FileTag[0]);
 		}
 	}
 
 	/** Reads all file tags from the repository, and updates the handler's
 	  data list.
 	  @param showErrors if true, error messages are displayed
 	  */
 	public boolean readFileTagDataFromRepo(boolean showErrors)
 	{
 		try {
 			setFileTagData(fileTagDAO.readAll());
 			return true;
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An error occurred refreshing the list of tags from the"
 					+ " repository.",
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 	/** Sets the handler's file tag data from a Collection of FileTag.
 	  @param tags the handler's tag data is initialized from this parameter
 	  */
 	public void setFileTagData(Collection<FileTag> tags)
 	{
 		fileTagData = new Vector<FileTag>(tags);
 	}
 
 	/** Mutator for the jfmiApp field.
 	  */
 	public final void setJFMIApp(JFMIApp jfmiApp_)
 	{
 		if (jfmiApp_ == null) {
 			throw new IllegalArgumentException("error: jfmiApp_ is null");
 		}
 
 		jfmiApp = jfmiApp_;
 	}
 
 	/** Tells the handler to update its data from the database, and refresh
 	  the data displayed by its GUI.
 	  @param showErrors if true, error messages are displayed
 	  */
 	public void updateDataAndGUI(boolean showErrors) 
 	{
 		readFileTagDataFromRepo(showErrors);
 		tagHandlerDialog.setTagJListData(fileTagData);
 	}
 
 	/** Updates the FileTag record having the specified id, with the new
 	  data contained in the updateMe object.
 	  @param id the current tag/id of the target file tag
 	  @param updateMe a FileTag containing the new information
 	  @param showErrors if true, errors will be displayed to the user
 	  @return true if the tag was updated successfully
 	  */
 	public boolean updateTagInRepo(String id, 
 								   FileTag updateMe, 
 								   boolean showErrors)
 	{
 		try {
 			boolean updated = fileTagDAO.update(updateMe, id.toString());
 
 			if (!updated && showErrors) {
 				GUIUtil.showErrorDialog(
 					"Failed to update the tag \"" + id + "\""
 					+ " to \"" + updateMe.getTag()  + "\"."
 				);
 			}
 
 			return updated;
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An error occurred while updating tag \"" + id + "\""
 					+ " to \"" + updateMe.getTag()  + "\".",
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 
 	//************************************************************
 	// PRIVATE INSTANCE Methods
 	//************************************************************
 	
 	/** Gets the edited value of a tag from the user. 
 	  @param currentVal the current value of the target tag
 	  @return the edited tag value, null if user cancelled
 	  */
 	private String getEditedTagFromUser(String currentVal)
 	{
 		String emptyPrompt = "An empty tag value is not valid.";
 
 		String input = tagHandlerDialog.showEditTagDialog(currentVal);
 		while (input != null && input.equals("")) {
 			input = tagHandlerDialog.showEditTagDialog(currentVal, emptyPrompt);
 		}
 
 		return input;
 	}
 
 	/** Gets the value of a new tag from the user.
 	  @return the new tag value, null if user cancelled
 	  */
 	private String getNewTagFromUser()
 	{
 		String emptyPrompt = "An empty tag value is not valid.";
 
 		String input = tagHandlerDialog.showAddTagDialog();
 		while (input != null && input.equals("")) {
 			input = tagHandlerDialog.showAddTagDialog(emptyPrompt);
 		}
 
 		return input;
 	}
 
 	
 
 }
