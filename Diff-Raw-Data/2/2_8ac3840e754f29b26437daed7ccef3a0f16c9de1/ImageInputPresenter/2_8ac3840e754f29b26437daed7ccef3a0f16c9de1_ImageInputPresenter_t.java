 package de.thatsich.bachelor.imageprocessing.restricted.controller;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 
 import javafx.fxml.FXML;
 import javafx.scene.control.Button;
 
 import com.google.inject.Inject;
 
 import de.thatsich.bachelor.imageprocessing.api.core.IImageEntries;
 import de.thatsich.bachelor.imageprocessing.api.core.IImageState;
 import de.thatsich.bachelor.imageprocessing.api.entities.ImageEntry;
 import de.thatsich.bachelor.imageprocessing.restricted.command.ImageInitCommander;
 import de.thatsich.bachelor.imageprocessing.restricted.command.commands.CopyFileCommand;
 import de.thatsich.bachelor.imageprocessing.restricted.command.commands.DeleteImageEntryCommand;
 import de.thatsich.bachelor.imageprocessing.restricted.command.provider.IImageCommandProvider;
 import de.thatsich.bachelor.imageprocessing.restricted.controller.handler.AddImageEntrySucceededHandler;
 import de.thatsich.bachelor.imageprocessing.restricted.controller.handler.DeleteImageEntrySucceededHandler;
 import de.thatsich.core.javafx.AFXMLPresenter;
 import de.thatsich.core.javafx.CommandExecutor;
 
 /**
  * Presenter
  * 
  * Represents the input controls for the image database part.
  * Offers basic ability to modify the underlying image-database.
  *  
  *  
  * @author Tran Minh Do
  *
  */
 public class ImageInputPresenter extends AFXMLPresenter {
 
 	// Nodes
 	@FXML private Button nodeButtonRemoveImage;
 	@FXML private Button nodeButtonResetDatabase;
 
 	// Injects
 	@Inject private IImageCommandProvider commander;	
 	@Inject private IImageEntries imageEntries;
 	@Inject private IImageState imageState;
 	@Inject private ImageFileChooser chooser;
 	
 	@Inject ImageInitCommander initCommander;
  
 	// Initialization Implementation 
 	@Override protected void initComponents() {
 	}
  
 	// Binding Implementation 
 	@Override protected void bindComponents() {
 		this.bindButtons();
 	}
 
 	private void bindButtons() {
 		this.nodeButtonRemoveImage.disableProperty().bind(this.imageEntries.selectedImageEntryProperty().isNull());
 		this.nodeButtonResetDatabase.disableProperty().bind(this.imageEntries.imageEntriesmageEntryListProperty().emptyProperty());
 	}
 
 	// ================================================== 
 	// GUI Implementation 
 	// ==================================================
 	/**
 	 * Shows a FileChooser and
 	 * adds selected image to model
 	 * 
 	 * @throws IOException
 	 */
 	@FXML private void onAddImageAction() throws IOException {
 
 		final List<Path> imagePathList = this.chooser.show();
 		if (imagePathList == null) return;
 		this.log.info("Fetched Path from chosen Image.");
 
 		final ExecutorService executor = CommandExecutor.newFixedThreadPool(imagePathList.size());
 		this.log.info("Created Executor.");
 		
 		for (final Path imagePath : imagePathList) {
			final Path copyPath = this.imageState.getImageFolderPath().resolve(imagePath.getFileName());
 			this.log.info("Created new Path: " + copyPath);
 
 			final CopyFileCommand command = this.commander.createCopyFileCommand(imagePath, copyPath);
 			command.setOnSucceededCommandHandler(AddImageEntrySucceededHandler.class);
 			command.setExecutor(executor);
 			command.start();
 			this.log.info("File copied and inserted into EntryList.");
 		}
 		
 		executor.shutdown();
 		this.log.info("Shutting down Executor.");
 	}
 
 	/**
 	 * Removes the currently selected image
 	 * 
 	 * @throws IOException
 	 */
 	@FXML private void onRemoveImageAction() throws IOException {
 		final ImageEntry choice = this.imageEntries.getSelectedImageEntry();
 		this.log.info("Fetched selected ImageEntry.");
 
 		if (choice == null) {
 			this.log.info("Selection was empty. Deleting nothing.");
 			return;
 		}
 
 		final DeleteImageEntryCommand command = this.commander.createDeleteImageEntryCommand(choice);
 		command.setOnSucceededCommandHandler(DeleteImageEntrySucceededHandler.class);
 		command.start();
 		this.log.info("File deleted and removed from EntryList.");
 	}
 
 	/**
 	 * Resets the image data base
 	 * 
 	 * @throws IOException 
 	 */
 	@FXML private void onResetDatabaseAction() throws IOException {
 		final List<ImageEntry> imageEntryList = this.imageEntries.imageEntriesmageEntryListProperty();
 		final ExecutorService executor = CommandExecutor.newFixedThreadPool(imageEntryList.size());
 		this.log.info("Initialized Executor for resetting all Errors.");
 
 		for (ImageEntry entry : imageEntryList) {
 			final DeleteImageEntryCommand command = this.commander.createDeleteImageEntryCommand(entry);
 			command.setOnSucceededCommandHandler(DeleteImageEntrySucceededHandler.class);
 			command.setExecutor(executor);
 			command.start();
 		}
 		this.log.info("EntryList resetted.");
 
 		executor.execute(new Runnable() {
 			@Override public void run() { System.gc(); }
 		});
 		this.log.info("Running Garbage Collector.");
 
 		executor.shutdown();
 		this.log.info("Shutting down Executor.");
 	}
 }
