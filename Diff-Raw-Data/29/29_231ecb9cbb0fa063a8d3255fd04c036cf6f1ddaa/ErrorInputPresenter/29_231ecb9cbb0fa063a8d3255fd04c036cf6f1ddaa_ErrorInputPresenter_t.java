 package de.thatsich.bachelor.javafx.presentation.b_error;
 
 import java.net.URL;
 import java.nio.file.Path;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.ObservableList;
 import javafx.concurrent.WorkerStateEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
import javafx.scene.control.Button;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.TextField;
 import javafx.util.StringConverter;
 
 import org.opencv.core.Mat;
 
 import com.google.inject.Inject;
 
 import de.thatsich.bachelor.javafx.business.command.ApplyErrorCommand;
 import de.thatsich.bachelor.javafx.business.command.CommandFactory;
 import de.thatsich.bachelor.javafx.business.command.CreateErrorImageCommand;
 import de.thatsich.bachelor.javafx.business.command.DeleteErrorEntryCommand;
 import de.thatsich.bachelor.javafx.business.command.GetLastErrorGeneratorIndexCommand;
 import de.thatsich.bachelor.javafx.business.command.InitErrorGeneratorListCommand;
 import de.thatsich.bachelor.javafx.business.model.ErrorEntries;
 import de.thatsich.bachelor.javafx.business.model.ErrorGenerators;
 import de.thatsich.bachelor.javafx.business.model.ErrorState;
 import de.thatsich.bachelor.javafx.business.model.ImageEntries;
 import de.thatsich.bachelor.javafx.business.model.entity.ErrorEntry;
 import de.thatsich.bachelor.javafx.business.model.entity.ImageEntry;
 import de.thatsich.bachelor.service.ConfigService;
 import de.thatsich.core.javafx.AFXMLPresenter;
 import de.thatsich.core.javafx.CommandExecutor;
 import de.thatsich.core.opencv.IErrorGenerator;
 
 /**
  * Presenter
  * 
  * Represents the input controls for the error column
  * 
  * @author Minh
  *
  */
 public class ErrorInputPresenter extends AFXMLPresenter {
 
 	// Nodes
 	@FXML private ChoiceBox<IErrorGenerator> nodeChoiceBoxErrorGenerator;
 	@FXML private TextField nodeTextFieldErrorCount;
 	
	@FXML private Button nodeButtonGenerateErrors;
	@FXML private Button nodeButtonPermutateErrors;
	@FXML private Button nodeButtonRemoveError;
	@FXML private Button nodeButtonResetErrors;
	
 	// Injects
 	@Inject private ImageEntries imageEntryList;
 	@Inject private ErrorState errorState;
 	@Inject private ErrorEntries errorEntryList;
 	@Inject private ErrorGenerators errorGeneratorList;
 	@Inject private ConfigService config;
 	@Inject private CommandFactory commander;
 	
 	// ================================================== 
 	// Initializable Implementation 
 	// ==================================================
 	@Override
 	public void initialize(URL location, ResourceBundle resoure) {
 		this.bindChoiceBoxErrorGenerator();
 		this.bindTextFieldErrorCount();
		this.bindButton();
 		
 		this.initErrorGeneratorList();
 	}
 	
 	/**
 	 * Initialize the ErrorGeneratorList and preselects the last selected one
 	 */
 	private void initErrorGeneratorList() {
 		final InitErrorGeneratorListSucceededHandler initHandler = new InitErrorGeneratorListSucceededHandler();
 		final GetLastErrorGeneratorIndexSucceededHandler lastHandler = new GetLastErrorGeneratorIndexSucceededHandler(); 
 		final ExecutorService executor = CommandExecutor.newFixedThreadPool(1);
 		
 		final InitErrorGeneratorListCommand initCommand = this.commander.createInitErrorGeneratorListCommand();
 		initCommand.setOnSucceeded(initHandler);
 		initCommand.setExecutor(executor);
 		initCommand.start();
 		this.log.info("Initialized ErrorGeneratorList Retrieval.");
 		
 		final GetLastErrorGeneratorIndexCommand lastCommand = this.commander.createGetLastErrorGeneratorIndexCommand();
 		lastCommand.setOnSucceeded(lastHandler);
 		lastCommand.setExecutor(executor);
 		lastCommand.start();
 		this.log.info("Initialized LastErrorGeneratorIndex Retrieval.");
 		
 		executor.shutdown();
 		this.log.info("Shutting down Executor.");
 	}
 	
 	// ================================================== 
 	// Bindings Implementation 
 	// ==================================================	
 	/**
 	 * Bind ChoiceBoxErrorGenerator together with its corresponding Model part.
 	 */
 	private void bindChoiceBoxErrorGenerator() {
 		this.nodeChoiceBoxErrorGenerator.setConverter(new StringConverter<IErrorGenerator>() {
 			@Override public String toString(IErrorGenerator errorGenerator) { return errorGenerator.getName(); }
 			@Override public IErrorGenerator fromString(String arg0) { return null; }
 		});
 		this.log.info("Set up StringErrorGeneratorConverter for proper name display.");
 		
 		this.nodeChoiceBoxErrorGenerator.itemsProperty().bindBidirectional(this.errorGeneratorList.getErrorGeneratorListProperty());
 		this.nodeChoiceBoxErrorGenerator.valueProperty().bindBidirectional(this.errorGeneratorList.getSelectedErrorGeneratorProperty());
 		this.log.info("Bound ChoiceBoxErrorGenerator to Model.");
 		
 		this.nodeChoiceBoxErrorGenerator.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
 			@Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
 				config.setLastErrorGeneratorIndexInt(newValue.intValue());
 			}
 		});
 		this.log.info("Bound ChoiceBoxErrorGenerator to Config.");
 		
 		this.nodeChoiceBoxErrorGenerator.getSelectionModel().select(this.config.getLastErrorGeneratorIndexInt());
 		this.log.info("Initialized ChoiceBoxErrorGenerator from Config.");
 	}
 	
 	/**
 	 * Bind TextFieldErrorCount to the Model and tries to validate the input to only non-negative inputs
 	 * and initialize the value for it
 	 */
 	private void bindTextFieldErrorCount() {
 		this.nodeTextFieldErrorCount.textProperty().addListener(new ChangeListener<String>() {
 			@Override
 			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
 				if (newValue.matches("\\d+")) {
 					final int count = Integer.parseInt(newValue);
 					errorState.getErrorLoopCountProperty().set(count);
 					config.setLastErrorCountInt(count);
 				} else {
 					nodeTextFieldErrorCount.setText(oldValue);
 				}
 			}
 		});
 		
 		final int lastErrorLoopCount = this.config.getLastErrorCountInt();
 		this.nodeTextFieldErrorCount.textProperty().set(Integer.toString(lastErrorLoopCount));
 		this.errorState.getErrorLoopCountProperty().set(lastErrorLoopCount);
 	}
 	
	private void bindButton() {
		this.nodeButtonGenerateErrors.disableProperty().bind(this.imageEntryList.getSelectedImageEntryProperty().isNull().or(this.nodeChoiceBoxErrorGenerator.valueProperty().isNull()).or(this.nodeTextFieldErrorCount.textProperty().isNull()));
		this.nodeButtonPermutateErrors.disableProperty().bind(this.imageEntryList.getImageEntryListProperty().emptyProperty().or(this.nodeChoiceBoxErrorGenerator.valueProperty().isNull()).or(this.nodeTextFieldErrorCount.textProperty().isNull()));
		this.nodeButtonRemoveError.disableProperty().bind(this.errorEntryList.getSelectedErrorEntryProperty().isNull());
		this.nodeButtonResetErrors.disableProperty().bind(this.errorEntryList.getErrorEntryListProperty().emptyProperty());
	}
	
 	// ================================================== 
 	// GUI Implementation 
 	// ==================================================
 	/**
 	 * Generates a single error output depending on selected image, generator and amount
 	 */
 	@FXML private void onGenerateErrorsAction() {
 		final ImageEntry imageEntry = this.imageEntryList.getSelectedImageEntryProperty().get();
 		final Mat image = imageEntry.getMat().clone();
 		final IErrorGenerator generator = this.errorGeneratorList.getSelectedErrorGeneratorProperty().get();
 		final int loops = this.errorState.getErrorLoopCountProperty().get();
 		final ExecutorService executor = CommandExecutor.newFixedThreadPool(loops);
 		this.log.info("Initialized Executor for generating all Errors.");
 		
 		for (int step = 0; step < loops; step++) {
 			final String dateTime = generator.getName() + "_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "_" + UUID.randomUUID().toString();
 			final Path imagePath = this.errorState.getErrorEntryFolderPath().get().resolve(dateTime + ".png");
 			this.log.info("Path: " + imagePath);
 			
 			final ErrorApplicationSucceededHandler handler = new ErrorApplicationSucceededHandler();
 			final ApplyErrorCommand command = this.commander.createApplyErrorCommand(image, imagePath, generator);
 			command.setOnSucceeded(handler);
 			command.setExecutor(executor);
 			command.start();
 		}
 		this.log.info("File created and added to ErrorList.");
 		
 		executor.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				System.gc();
 			}
 		});
 		this.log.info("Running Garbage Collector.");
 		
 		executor.shutdown();
 		this.log.info("Shutting down Executor.");
 	}
 	
 	/**
 	 * Delete and remove the selected ErrorEntry
 	 */
 	@FXML private void onRemoveErrorAction() {
 		final ErrorEntry entry = this.errorEntryList.getSelectedErrorEntryProperty().get();
 		this.log.info("Fetched selected ErrorEntry.");
 		
 		if (entry == null) {
 			this.log.info("Selection was empty. Deleting nothing.");
 			return;
 		}
 		
 		final DeleteSucceededHandler handler = new DeleteSucceededHandler();
 		final DeleteErrorEntryCommand command = this.commander.createDeleteErrorEntryCommand(entry);
 		command.setOnSucceeded(handler);
 		command.start();
 		this.log.info("File deleted and removed from ErrorList.");
 	}
 	
 	/**
 	 * Cross-Set of all input images to selected error generator
 	 */
 	@FXML private void onPermutateErrorsAction() {
 		final ObservableList<ImageEntry> imageEntries = this.imageEntryList.getImageEntryListProperty().get();
 		final IErrorGenerator generator = this.errorGeneratorList.getSelectedErrorGeneratorProperty().get();
 		final int loops = this.errorState.getErrorLoopCountProperty().get();
 		final ExecutorService executor = CommandExecutor.newFixedThreadPool(imageEntries.size() * loops);
 		final Path errorEntryFolderPath = this.errorState.getErrorEntryFolderPath().get();
 		this.log.info("Initialized Executor for permutating all Errors.");
 		
 		for (ImageEntry entry : imageEntries) {
 			final Mat image = entry.getMat().clone();
 			for (int step = 0; step < loops; step++) {
 				final String dateTime = generator.getName() + "_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "_" + UUID.randomUUID().toString();
 				Path imagePath = errorEntryFolderPath.resolve(dateTime + ".png");
 				this.log.info("Path: " + imagePath);
 				
 				final ErrorApplicationSucceededHandler handler = new ErrorApplicationSucceededHandler();
 				final ApplyErrorCommand command = this.commander.createApplyErrorCommand(image, imagePath, generator);
 				command.setOnSucceeded(handler);
 				command.setExecutor(executor);
 				command.start();
 			}
 		}
 		this.log.info("Files created and added to ErrorList.");
 		
 		executor.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				System.gc();
 			}
 		});
 		this.log.info("Running Garbage Collector.");
 		
 		executor.shutdown();
 		this.log.info("Shutting down Executor.");
 	}
 	
 	/**
 	 * Deletes each file and remove each entry
 	 */
 	@FXML private void onResetErrorsAction() {
 		final List<ErrorEntry> errorEntryList = this.errorEntryList.getErrorEntryListProperty().get();
 		final ExecutorService executor = CommandExecutor.newFixedThreadPool(errorEntryList.size());
 		this.log.info("Initialized Executor for resetting all Errors.");
 		
 		for (ErrorEntry entry : errorEntryList) {
 			final DeleteSucceededHandler handler = new DeleteSucceededHandler();
 			final DeleteErrorEntryCommand command = this.commander.createDeleteErrorEntryCommand(entry);
 			command.setOnSucceeded(handler);
 			command.setExecutor(executor);
 			command.start();
 			this.log.info("File Deletion executed.");
 		}
 		
 		executor.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				System.gc();
 			}
 		});
 		this.log.info("Running Garbage Collector.");
 		
 		executor.shutdown();
 		this.log.info("Shutting down Executor.");
 	}
 
 	// ================================================== 
 	// Handler Implementation 
 	// ==================================================
 	
 	/**
 	 * Handler for what should happen if the Command was successfull 
 	 * for initializing the Error Generator List
 	 * 
 	 * @author Minh
 	 */
 	@SuppressWarnings("unchecked")
 	private class InitErrorGeneratorListSucceededHandler implements EventHandler<WorkerStateEvent> {
 
 		@Override
 		public void handle(WorkerStateEvent event) {
 			final List<IErrorGenerator> generatorList = (List<IErrorGenerator>) event.getSource().getValue();
 			
 			errorGeneratorList.getErrorGeneratorListProperty().get().addAll(generatorList);
 			log.info("Added all ErrorGenerators.");
 		}
 	}
 	
 	/**
 	 * Handler for what should happen if the Command was successfull 
 	 * for getting LastErrorGeneratorIndex
 	 * 
 	 * @author Minh
 	 */
 	private class GetLastErrorGeneratorIndexSucceededHandler implements EventHandler<WorkerStateEvent> {
 
 		@Override
 		public void handle(WorkerStateEvent event) {
 			final Integer commandResult = (Integer) event.getSource().getValue();
 			log.info("Retrieved last selected error generator index.");
 			
 			if (commandResult != null) {
 				final IErrorGenerator selectedErrorGenerator = errorGeneratorList.getErrorGeneratorListProperty().get(commandResult);
 				errorGeneratorList.getSelectedErrorGeneratorProperty().set(selectedErrorGenerator);
 				log.info("Set last selected error generator index in Model.");
 			}
 		}
 	}
 
 	/**
 	 * Handler for what should happen if the Command was successfull 
 	 * for deleting the error
 	 * 
 	 * @author Minh
 	 */
 	private class DeleteSucceededHandler implements EventHandler<WorkerStateEvent> {
 		@Override public void handle(WorkerStateEvent event) {
 			final ErrorEntry deletion = (ErrorEntry) event.getSource().getValue();
 			final ObservableList<ErrorEntry> entryList = errorEntryList.getErrorEntryListProperty().get();
 			
 			entryList.remove(deletion);
 			log.info("Removed ErrorEntry from Database.");
 			
 			if (entryList.size() > 0) {
 				final ErrorEntry first = entryList.get(0);
 				errorEntryList.getSelectedErrorEntryProperty().set(first);
 				log.info("Reset Selection to first ErrorEntry.");
			} else {
				errorEntryList.getSelectedErrorEntryProperty().set(null);
				log.info("Reset Selection to null.");
 			}
 		}
 	}
 	
 	/**
 	 * Handler for what should happen if the Command was successfull 
 	 * for applying the error
 	 * 
 	 * @author Minh
 	 */
 	private class ErrorApplicationSucceededHandler implements EventHandler<WorkerStateEvent> {
 		@Override public void handle(WorkerStateEvent event) {
 			final ErrorEntry error = (ErrorEntry) event.getSource().getValue();
 			log.info("Fetched ErrorEntry from Command.");
 
 			final CreateSucceededHandler handler = new CreateSucceededHandler();
 			final CreateErrorImageCommand command = commander.createCreateErrorImageCommand(error);
 			command.setOnSucceeded(handler);
 			command.start();
 		}
 	}
 
 	/**
 	 * Handler for what should happen if the Command was successfull 
 	 * for deleting the error
 	 * 
 	 * @author Minh
 	 */
 	private class CreateSucceededHandler implements EventHandler<WorkerStateEvent> {
 		@Override public void handle(WorkerStateEvent event) {
 			final ErrorEntry addition = (ErrorEntry) event.getSource().getValue();
 			
 			errorEntryList.getErrorEntryListProperty().get().add(addition);
 			log.info("Added ErrorEntry to Database.");
 			
 			errorEntryList.getSelectedErrorEntryProperty().set(addition);
 			log.info("Set current to selected ErrorEntry.");
 		}
 	}
 }
