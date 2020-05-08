 package jfmi.control;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import jfmi.dao.TaggedFileDAO;
 import jfmi.gui.GUIUtil;
 import jfmi.gui.JFMIFrame;
 import jfmi.gui.TaggedFileHandlerGUI;
 import jfmi.gui.TaggedFileViewDialog;
 
 
 /** A controller class for handling the logic of updating/adding/deleting which
   files kept in the database.
   */
 public class TaggedFileHandler {
 
 	// PRIVATE INSTANCE Fields
 	private JFMIApp jfmiApp;
 	private TaggedFileHandlerGUI fileGUI;
 
 	private TaggedFileDAO taggedFileDAO;
 
 	private Vector<TaggedFile> taggedFiles; 
 
 
 	//************************************************************	
 	// PUBLIC INSTANCE Methods
 	//************************************************************	
 
 	/** Constructs a new TaggedFileHandler associated with the specified
 	  JFMIApp.
 	  @param jfmiApp_ the JFMIApp to associate with this handler
 	  @throws IllegalArgumentException if jfmiApp_ is null
 	  */
 	public TaggedFileHandler(JFMIApp jfmiApp_)
 	{
 		setJFMIApp(jfmiApp_);
 		taggedFileDAO = new TaggedFileDAO();
 		fileGUI = new TaggedFileHandlerGUI(jfmiApp.getJFMIGUI(), this);
 	}
 
 	/** Given an array of File objects, attempts to create TaggedFile objects
 	  and insert them into the repository.
 	  @param files an array of File to be newly created in the repository
 	  */
 	public void addFilesToRepo(File[] files)
 	{
 		TaggedFile newFile;
 
 		if (files == null) {
 			return;
 		}
 
 		for (File f : files) {
 			newFile = new TaggedFile();
 			newFile.setFile(f);
 				
 			addTaggedFileToRepo(newFile, true);
 		}
 
 	}
 
 	/** Attempts to add a TaggedFile to the repository. If showErrors is
 	  true, any errors which occur are displayed to the user.
 	  @param file the TaggedFile to be added to the repository
 	  @param showErrors if true, the user receives error messages
 	  @return true if the file was added successfully
 	  */
 	public boolean addTaggedFileToRepo(TaggedFile file, boolean showErrors)
 	{
 		try {
 			boolean creationSuccess = taggedFileDAO.create(file);
 
 			if (!creationSuccess && showErrors) {
 				GUIUtil.showErrorDialog(
 					"The following file could not be added: " 
 					+ file.getFilePath()
 				);
 			}
 
 			return creationSuccess;
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An error occurred while adding the file: " 
 					+ file.getFilePath(), 
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 	/** Begins an interaction with the user that allows them to add new files
 	  to the repository for tagging.
 	  */
 	public void beginAddFileInteraction()
 	{
 		File[] selectedFiles = fileGUI.displayFileChooser();
 		addFilesToRepo(selectedFiles);
 		updateDataAndGUI(true);
 	}
 
 	/** Begins an interaction with the user which adds a tagging to a 
 	  EditedTaggedFile and redisplays the file's information to the user.
 	  @param updateMe the EditedTaggedFile to update with a new tagging
 	  @param newTagging the new FileTagging to add to the file
 	  */
 	public void beginAddTaggingInteraction(EditedTaggedFile updateMe,
 											FileTagging newTagging)
 	{
 		TreeSet<FileTagging> addedTaggings = updateMe.getAddedTaggings();	
 
 		if (addedTaggings == null) {
 			addedTaggings = new TreeSet<FileTagging>();
 			addedTaggings.add(newTagging);
 			updateMe.setAddedTaggings(addedTaggings);
 		} else {
 			addedTaggings.add(newTagging);
 		}
 
 		fileGUI.getFileViewer().updateDisplayedFile(updateMe);
 	}
 
 	/** Begins an interaction with the user that allows them to delete
 	  selected files from the repository.
 	  @param selectedFiles files selected by the user for deletion
 	  */
 	public void beginDeleteFilesInteraction(List<TaggedFile> selectedFiles)
 	{
 		if (fileGUI.getConfirmation("Confirm deletion of files.") == false) {
 			return;	
 		}
 
 		deleteFilesFromRepo(selectedFiles);
 		updateDataAndGUI(true);
 	}
 
 	/** Begins an interaction with the user which remoevs a tagging from an 
 	  EditedTaggedFile and redisplays the file's information to the user.
	  FIXME What if the user is removing a non-saved tag?
 	  @param updateMe the EditedTaggedFile from which to remove a tagging
 	  @param newTagging the FileTagging to remoev from the file
 	  */
 	public void beginRemoveTaggingInteraction(EditedTaggedFile updateMe,
 											  FileTagging deadTagging)
 	{
 		TreeSet<FileTagging> removedTaggings = updateMe.getRemovedTaggings();	
 
 		if (removedTaggings == null) {
 			removedTaggings = new TreeSet<FileTagging>();
 			removedTaggings.add(deadTagging);
 			updateMe.setRemovedTaggings(removedTaggings);
 		} else {
 			removedTaggings.add(deadTagging);
 		}
 
 		fileGUI.getFileViewer().updateDisplayedFile(updateMe);
 	}
 
 	/** Begins an interaction which allows the user to update the specified
   	  file's path.
 	  @param updateMe the EditedTaggedFile whose path will be updated
 	  */	  
 	public void beginUpdateFilePathInteraction(EditedTaggedFile updateMe)
 	{
 		TaggedFile file = updateMe.getEditedFile();
 
 		// Get an updated path from the user
 		File[] selected = fileGUI.displayFileChooser(file.getFile());
 		if (selected == null) {
 			return;
 		}
 
 		// Set the new path in the file and redisplay
 		file.setFile(selected[0]);
 		fileGUI.getFileViewer().updateDisplayedFile(updateMe);	
 	}
 
 	/** Begins an interaction with the user which allows them to save a
 	  file they have been editing.
 	  @param saveMe the EditedTaggedFile to be updated in the repository
 	  */
 	public void beginSaveFileInteraction(EditedTaggedFile saveMe)
 	{
 		// Confirm the update
 		boolean confirmed = fileGUI.getConfirmation("Are you sure you want to "
 													+ "save any file changes?");
 		if (!confirmed) {
 			return;
 		}
 
 		// Update the repository
 		if (updateTaggedFileInRepo(saveMe, true)) {
 			GUIUtil.showAlert("File saved successfully.");
 
 			/* Redisplay the current file in the fileViewer and in
 			 the application's GUI */
 			fileGUI.getFileViewer().updateDisplayedFile(saveMe);
 			updateDataAndGUI(true);
 		}
 	}
 
 	/** Begins an interaction with the user that allows them to view and
 	  update a specific file. This method updates the list of files displayed
 	  by the 
 	  @param viewMe the file to view
 	  */
 	public void beginViewFileInteraction(TaggedFile viewMe)
 	{
 		TaggedFileViewDialog fileViewer = fileGUI.getFileViewer();
 		FileTag[] tags;
 
 		// Get all tags, so the user can browse/add them to the file
 		jfmiApp.getTagHandler().readFileTagDataFromRepo(true);
 		tags = jfmiApp.getTagHandler().getFileTagDataAsArray();
 
 		if (tags != null) {
 			fileViewer.getTagJList().setListData(tags);
 		}
 
 		// Wrap the specified file in an EditedTaggedFile
 		EditedTaggedFile editFile = new EditedTaggedFile(viewMe);
 
 		// Tell the file viewer to display the specified file
 		fileViewer.updateDisplayedFile(editFile);
 		fileViewer.setVisible(true);
 	}
 
 	/** Deletes the TaggedFiles in the specified list from the repository.
 	  @param files list of files to be deleted
 	  */
 	public void deleteFilesFromRepo(List<TaggedFile> files)
 	{
 		if (files == null) {
 			return;
 		}
 	
 		for (TaggedFile tf : files) {
 			deleteTaggedFileFromRepo(tf, true);
 		}
 	}
 
 	/** Deletes a TaggedFile from the underlying repository.
 	  @param file the file to delete
 	  @param showErrors if true, the method displays any error messages
 	  @return true if the file was deleted successfully
 	  */
 	public boolean deleteTaggedFileFromRepo(TaggedFile file,
 											 boolean showErrors)
 	{
 		try {
 			return taggedFileDAO.delete(file);
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"Failed to delete file: " + file.getFilePath(),
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 	/** Sets this instance's associated JFMIApp.
 	  @param jfmiApp_ the JFMIApp to associate this handler with
 	  @throws IllegalArgumentException if jfmiApp_ is null
 	  */
 	public final void setJFMIApp(JFMIApp jfmiApp_)
 	{
 		if (jfmiApp_ == null) {
 			throw new IllegalArgumentException("jfmiApp_ cannot be null");
 		} 
 
 		jfmiApp = jfmiApp_;
 	}
 
 	/** Constructs a new Vector<TaggedFile> for this instance from the
 	  specified Collection.
 	  @param collection The Collection to construct a new Vector from.
 	  */
 	public void setTaggedFiles(Collection<TaggedFile> collection)
 	{
 		taggedFiles = new Vector<TaggedFile>(collection);
 	}
 
 	/** Refreshes the handler's data from the repository, and updates its
 	  associated GUI.
 	  @param showErrors if true, the method displays errors
 	  @return true if data was updated and sent to GUI successfully
 	  */
 	public boolean updateDataAndGUI(boolean showErrors)
 	{
 		boolean readSuccess = readTaggedFilesFromRepo(true);
 
 		if (readSuccess) {
 			updateGUITaggedFileJList();
 		}
 
 		return readSuccess;
 	}
 
 	/** Updates a TaggedFile's file information (id, path) in the repository.
 	  @param updateMe the TaggedFile to update
 	  @param showErrors if true, errors are displayed to the user
 	  @return true if the update was performed successfully
 	  */ 
 	public boolean updateFileInfoInRepo(TaggedFile updateMe, boolean showErrors)
 	{
 		try {
 			boolean success;
 		    success = taggedFileDAO.update(updateMe, updateMe.getFileId());
 
 			if (!success && showErrors) {
 				GUIUtil.showErrorDialog(
 					"Failed to update the file in the repository."
 				);
 			}
 
 			return success;
 
 		} catch (SQLException e) {
 			if (showErrors) {
 				GUIUtil.showErrorDialog(
 					"An error occurred with the repository while performing"
 					+ " the update.",
 					e.toString()
 				);
 			}
 		}
 
 		return false;
 	}
 
 	/** Updates an EditedTaggedFile's file taggings in the repository.
 	  @param updateMe the EditedTaggedFile to update
 	  @param showErrors if true, errors are displayed to the user
 	  @return true if the update was performed successfully
 	  */ 
 	public boolean updateFileTaggingsInRepo(EditedTaggedFile updateMe, 
 											boolean showErrors)
 	{
 		FileTaggingHandler taggingHandler = jfmiApp.getTaggingHandler();	
 		TreeSet<FileTagging> added = updateMe.getAddedTaggings();
 		TreeSet<FileTagging> removed = updateMe.getRemovedTaggings();
 		TreeSet<FileTagging> updated = updateMe.getUpdatedTaggings();
 		boolean goodAdd = true;
 		boolean goodRemove = true;
 		boolean goodUpdate = true;
 
 		if (added != null && !added.isEmpty()) {
 			goodAdd = taggingHandler.addTaggingsToRepo(added, showErrors);		
 		}
 
 		if (removed != null && !removed.isEmpty()) {
 			goodRemove = taggingHandler.deleteTaggingsFromRepo(removed,
 															   showErrors);		
 		}
 
 		if (updated != null && !updated.isEmpty()) {
 			goodUpdate = taggingHandler.updateFileTaggingsInRepo(updated,
 																 showErrors);		
 		}
 
 		return goodAdd && goodRemove && goodUpdate;
 	}
 
 	/** Updates the specified EditedTaggedFile's file information (id, path) 
 	  as well as it's file taggings in the repository.
 	  @param updateMe the EditedTaggedFile to update
 	  @param showErrors if true, errors are displayed to the user
 	  @return true if the update was performed successfully
 	  */
 	public boolean updateTaggedFileInRepo(EditedTaggedFile updateMe, 
 											boolean showErrors)
 	{
 		boolean updatedInfo = updateFileInfoInRepo(
 									updateMe.getEditedFile(), 
 									showErrors
 								);
 		boolean updatedTaggings = updateFileTaggingsInRepo(updateMe, showErrors);
 
 		return updatedInfo && updatedTaggings;
 	}
 
 	//************************************************************	
 	// PRIVATE INSTANCE Methods
 	//************************************************************	
 
 	/** Gets an updated list of TaggedFiles from the repository, and updates
 	  the taggedFiles field.
 	  @param showError if true, and an error occurs, display a message
 	  @return true if the files were refreshed successfully
 	  */
 	private boolean readTaggedFilesFromRepo(boolean showError)
 	{
 		try {
 			setTaggedFiles(taggedFileDAO.readAll());
 
 		} catch (SQLException e) {
 			if (showError) {
 				GUIUtil.showErrorDialog(
 					"Failed to refresh the list of files from the database.",
 					e.toString()
 				);
 			}
 			return false;
 		}
 
 		return true;
 	}
 
 	/** Updates this instance's GUI with the latest values in the Vector
 	  of TaggedFile objects.
 	  */
 	private void updateGUITaggedFileJList()
 	{
 		jfmiApp.getJFMIGUI().setTaggedFileJListData(taggedFiles);
 	}
 
 }
