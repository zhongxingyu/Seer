 package ch.softhenge.supren.exif.service;
 
 import static org.junit.Assert.*;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.softhenge.supren.exif.entity.FilePattern;
 import ch.softhenge.supren.exif.entity.ImageFile;
 
 /**
  * Test Photos from Class Path
  * 
  * @author werni
  *
  */
 public class ImageServiceTestPhotosFromClassPath {
 
 	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
 
 	private ImageService imageService;
 
 	@Before
 	public void setUp() throws Exception {
 		String fileURL = this.getClass().getClassLoader().getResource("imgfiles").getPath();
 		imageService = new ImageService("ruro.properties", fileURL);
 		LOGGER.setLevel(Level.FINE);
         ConsoleHandler handler = new ConsoleHandler();
         handler.setLevel(Level.FINE);
 		LOGGER.addHandler(handler);
 	}
 
 	@Test
 	public void testListImageFilesToRename() {
 		imageService.createImageFilesMap();
 		imageService.resetImageFileList();
 		imageService.createImageFilesMap();
 		imageService.createImageFilesMap();
 		Map<FilePattern, Collection<ImageFile>> mapOfImageFiles = imageService.getMapOfImageFiles();
 	}
 
 	@Test
 	public void testMvCommand() {
 		imageService.createMvAndUndoCommands();
 		String mvCommand = imageService.getMvCommand();
 		LOGGER.fine("mvCommands");
 		LOGGER.fine(mvCommand);
 		String mvUndoCommand = imageService.getMvUndoCommand();
 		LOGGER.fine("mvUndoCommand");
 		LOGGER.fine(mvUndoCommand);
 		String mvError = imageService.getMvError();
 		LOGGER.fine("mvError");
 		LOGGER.fine(mvError);
 	}
 	
 	@Test
 	public void testGetListOfUnknownImageFiles() {
 		Collection<ImageFile> imageFiles = imageService.getListOfUnknownImageFiles();
 		assertEquals(imageFiles.size(), 0);
 	}
 	
 	@Test
	public void testCreateCsvSeperatedStringOfImageFiles() {
		String csvText = imageService.createCsvSeperatedStringOfImageFiles();
		LOGGER.info(csvText);
 	}
 	
 }
