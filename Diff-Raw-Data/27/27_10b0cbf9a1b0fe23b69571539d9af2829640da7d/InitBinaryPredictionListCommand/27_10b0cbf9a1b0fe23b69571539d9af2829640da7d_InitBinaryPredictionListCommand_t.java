 package de.thatsich.bachelor.prediction.intern.command.commands;
 
 import java.io.IOException;
 import java.nio.file.DirectoryIteratorException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.List;
 
 import javafx.collections.FXCollections;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 
 import de.thatsich.bachelor.prediction.api.entities.BinaryPrediction;
 import de.thatsich.bachelor.prediction.intern.service.BinaryPredictionFileStorageService;
 import de.thatsich.core.javafx.ACommand;
 
 public class InitBinaryPredictionListCommand extends ACommand<List<BinaryPrediction>> {
 	
 	// Fields
 	private final Path binaryPredictionFolderPath;
 	
 	// Injects
 	@Inject BinaryPredictionFileStorageService fileStorage;
 	
 	@Inject
 	protected InitBinaryPredictionListCommand(@Assisted Path binaryPredictionFolderPath) {
 		this.binaryPredictionFolderPath = binaryPredictionFolderPath;
 	}
 
	@SuppressWarnings("unused")
 	@Override
 	protected List<BinaryPrediction> call() throws Exception {
 		final List<BinaryPrediction> binaryPredictionList = FXCollections.observableArrayList();
 
 		final String GLOB_PATTERN = "*.{png}";
 		
 		// traverse whole directory and search for yaml files
 		// try to open them
 		// and parse the correct classifier
 		try (DirectoryStream<Path> stream = Files.newDirectoryStream(binaryPredictionFolderPath, GLOB_PATTERN)) {
 			for (Path child : stream) {
 				// split the file name 
 				// and check if has 5 members
 				// and extract them
 				final String fileName = child.getFileName().toString();
 				final String[] fileNameSplit = fileName.split("_");
 //				if (fileNameSplit.length != 5) throw new WrongNumberArgsException("Expected 5 encoded information but found " + fileNameSplit.length);
 				log.info("Split FileNmae.");
 				
 				final String classificationName = fileNameSplit[0];
 				final String extractorName = fileNameSplit[1];
 				final int frameSize = Integer.parseInt(fileNameSplit[2]);
 				final String errorName = fileNameSplit[3];
 				final String id = fileNameSplit[4];
 				log.info("Prepared SubInformation.");
 				
 				final BinaryPrediction prediction = fileStorage.load(child);
 				binaryPredictionList.add(prediction);
 			}
 		} catch (IOException | DirectoryIteratorException e) {
 			e.printStackTrace();
 		}
 		log.info("All BinaryClassification extracted.");
 		
 		return binaryPredictionList;
 	}
 
 }
