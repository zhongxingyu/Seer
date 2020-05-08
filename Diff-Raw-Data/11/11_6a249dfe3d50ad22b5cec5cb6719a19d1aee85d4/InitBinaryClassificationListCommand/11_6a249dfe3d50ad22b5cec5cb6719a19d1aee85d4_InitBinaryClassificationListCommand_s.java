 package de.thatsich.bachelor.classificationtraining.restricted.controller.commands;
 
 import java.io.IOException;
 import java.nio.file.DirectoryIteratorException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.List;
 
 import org.opencv.ml.CvRTrees;
 import org.opencv.ml.CvSVM;
 
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.collections.FXCollections;
 import javafx.concurrent.Task;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
 
 import de.thatsich.bachelor.classificationtraining.api.entities.IBinaryClassification;
 import de.thatsich.bachelor.classificationtraining.restricted.application.guice.BinaryClassificationProvider;
 import de.thatsich.bachelor.classificationtraining.restricted.model.logic.BinaryClassifierConfiguration;
 import de.thatsich.core.javafx.Command;
 
 public class InitBinaryClassificationListCommand extends Command<List<IBinaryClassification>> {
 	
 	@Inject private BinaryClassificationProvider provider;
 	
 	private final ObjectProperty<Path> binaryClassificationFolderPath = new SimpleObjectProperty<Path>();
 	
 	@Inject
 	protected InitBinaryClassificationListCommand(@Assisted Path binaryClassificationFolderPath) {
 		this.binaryClassificationFolderPath.set(binaryClassificationFolderPath);
 	}
 	
 	@Override
 	protected Task<List<IBinaryClassification>> createTask() {
 		return new Task<List<IBinaryClassification>>() {
 			@Override protected List<IBinaryClassification> call() throws Exception {
 				final Path folderPath = binaryClassificationFolderPath.get(); 
 				final List<IBinaryClassification> binaryClassificationList = FXCollections.observableArrayList();
 				
 				if (Files.notExists(folderPath) || !Files.isDirectory(folderPath)) Files.createDirectories(folderPath);
 				
 				final String GLOB_PATTERN = "*.{yaml}";
 				
 				// traverse whole directory and search for yaml files
 				// try to open them
 				// and parse the correct classifier
 				try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, GLOB_PATTERN)) {
 					for (Path child : stream) {
 						try {
 						// split the file name 
 						// and check if has 5 members
 						// and extract them
 						final String fileName = child.getFileName().toString();
 						final String[] fileNameSplit = fileName.split("_");
 						if (fileNameSplit.length != 5) throw new WrongNumberArgsException("Expected 5 encoded information but found " + fileNameSplit.length);
 						log.info("Split FileNmae.");
 						
 						final String classificationName = fileNameSplit[0];
 						final String extractorName = fileNameSplit[1];
 						final int frameSize = Integer.parseInt(fileNameSplit[2]);
 						final String errorName = fileNameSplit[3];
 						final String id = fileNameSplit[4];
 						log.info("Prepared SubInformation.");
 						
 						final BinaryClassifierConfiguration config = new BinaryClassifierConfiguration(child, classificationName, extractorName, frameSize, errorName, id);
 						IBinaryClassification classification;
 						switch(classificationName) {
 							case "RandomForestBinaryClassifier":
 								classification = provider.createRandomForestBinaryClassification(new CvRTrees(), config);
 								break;
 							case "SVMBinaryClassifier":
 								classification = provider.createSVMBinaryClassification(new CvSVM(), config);
 								break;
 							default:
 								throw new IllegalStateException("Unknown Classification");
 						}
 						log.info("Resolved BinaryClassification.");
 						
						classification.load(fileName);
 						log.info("Loaded YAML into BinaryClassification.");
 
 						binaryClassificationList.add(classification);
 						log.info("Added " + child + " with Attribute " + Files.probeContentType(child));
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 						
 					}
 				} catch (IOException | DirectoryIteratorException e) {
 					e.printStackTrace();
 				}
 				log.info("All BinaryClassification added.");
 				
 				return binaryClassificationList;
 			}
 		};
 	}
 
 }
