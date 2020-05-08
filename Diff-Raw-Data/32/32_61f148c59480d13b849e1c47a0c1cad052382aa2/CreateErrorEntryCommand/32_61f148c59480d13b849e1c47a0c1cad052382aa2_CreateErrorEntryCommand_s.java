 package de.thatsich.bachelor.errorgeneration.restricted.command.commands;
 
 import java.nio.file.Path;
 
 import org.opencv.core.Mat;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 
 import de.thatsich.bachelor.errorgeneration.api.entities.ErrorEntry;
 import de.thatsich.bachelor.errorgeneration.api.entities.IErrorGenerator;
 import de.thatsich.bachelor.errorgeneration.restricted.service.ErrorFactoryService;
 import de.thatsich.core.javafx.ACommand;
 
 public class CreateErrorEntryCommand extends ACommand<ErrorEntry> {
 
 	// Fields
 	final private Mat imageMat;
 	final private Path imagePath;
 	final private IErrorGenerator generator;
 	
 	// Injections
 	@Inject private ErrorFactoryService factory;
 	
 	@Inject
 	public CreateErrorEntryCommand(@Assisted Mat imageMat, @Assisted Path imagePath, @Assisted IErrorGenerator generator) {
 		this.imageMat = imageMat;
 		this.imagePath = imagePath;
 		this.generator = generator;
 	}
 
 	@Override
 	protected ErrorEntry call() throws Exception {
 		Mat copy = this.imageMat.clone();
 		copy = this.generator.generateError(copy);
 		this.log.info("Error generated.");
 		
		return this.factory.getErrorEntryFromMat( this.imagePath, this.imageMat, copy );
 	}
 }
