 package ch.softhenge.supren.exif.factory;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 
 import ch.softhenge.supren.exif.entity.ExifFileInfo;
 import ch.softhenge.supren.exif.entity.FilePattern;
 import ch.softhenge.supren.exif.entity.ImageFile;
 import ch.softhenge.supren.exif.file.ImageFileValidator;
 import ch.softhenge.supren.exif.property.UserPropertyReader;
 import ch.softhenge.supren.exif.property.UserPropertyReader.PropertyName;
 
 /**
  * Image Service is able to handle image filenames and compare them against patterns.
  * 
  * @author Werni
  *
  */
 public class ImageService {
 
 	private static final String UNKNOWN_PATTERN = "Unknown";
 
 	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
 	
 	private final File baseDir;
 	private final UserPropertyReader userPropertyReader;
 	private final ExifService exifService;
 	private final ImageFileValidator imageFileValidator;
 
 	/**The Map with the filename pattern as key **/
 	private final Map<String, Collection<ImageFile>> mapOfImageFiles;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param resourceFileName
 	 * @param baseDirectory
 	 */
 	public ImageService(String resourceFileName, String baseDirectory) {
 		this.baseDir = new File(baseDirectory);
 		this.userPropertyReader = new UserPropertyReader(resourceFileName);
 		this.exifService = new ExifService();
 		this.imageFileValidator = new ImageFileValidator(userPropertyReader);
 		this.mapOfImageFiles = new HashMap<String, Collection<ImageFile>>();
 		Map<Integer, String> infilePatternMap = this.userPropertyReader.getPropertyMapOfProperty(PropertyName.InfilePattern);
 		for (String infilePattern : infilePatternMap.values()) {
 			this.mapOfImageFiles.put(infilePattern, new ArrayList<ImageFile>());
 		}
 		this.mapOfImageFiles.put(UNKNOWN_PATTERN, new ArrayList<ImageFile>());
 	}
 
 	
 	public void RenameFiles() {
 	}
 
 	/**
 	 * Create a list of Image Files that are candidates to rename and save it
 	 * as a map and get it using getMapOfImageFiles.
 	 */
 	public void createImageFilesMap() {
 		if (getListOfImageFiles().size() == 0) {
 			Collection<File> listAllImageFiles = listAllImageFilesInDir();
 			for (File file : listAllImageFiles) {
 				FilePattern filePattern = imageFileValidator.getFilePattern(file.getName());
 				ImageFile imageFile;
 				String mapKey;
 				if (filePattern != null) {
 					mapKey = filePattern.getFilePatternString();
 					String imageNumber = imageFileValidator.getInfilePatternImgNum(file.getName(), filePattern.getPatternIdx());
 					imageFile = new ImageFile(file, imageNumber, filePattern.getFilePatternString());
 				} else {
 					mapKey = UNKNOWN_PATTERN;
 					imageFile = new ImageFile(file, null, null);
 				}
 				this.mapOfImageFiles.get(mapKey).add(imageFile);
 			}
 		}
 	}
 
 	
 	public Map<String, Collection<ImageFile>> getMapOfImageFiles() {
 		return mapOfImageFiles;
 	}
 	
 	/**
 	 * 
 	 * @return a List of all Image Files
 	 */
 	public Collection<ImageFile> getListOfImageFiles() {
 		Collection<ImageFile> resultFiles = new ArrayList<>();
 		for (Collection<ImageFile> imageFiles : this.mapOfImageFiles.values()) {
			imageFiles.addAll(resultFiles);
 		}
 		return resultFiles;
 	}
 
 
 	/**
 	 * Empties the list of Image Files
 	 */
 	public void resetImageFileList() {
 		for (Collection<ImageFile> imageFiles : this.mapOfImageFiles.values()) {
 			imageFiles.clear();
 		}
 	}
 	
 	
 	/**
 	 * Return a Collection of all image Files in the base directory
 	 * 
 	 * @return
 	 */
 	private Collection<File> listAllImageFilesInDir() {
         String fileExtensionList = userPropertyReader.getPropertyMapOfProperty(PropertyName.fileExtensionList).get(UserPropertyReader.INDEX_IF_EXACTLYONE); 
 		String[] extensions = fileExtensionList.split(",");
 		
 		long currTime = System.currentTimeMillis();
 		Collection<File> listFiles = FileUtils.listFiles(baseDir, extensions, true);
 		LOGGER.fine("Anz Files: " + listFiles.size() + " , took: " + (System.currentTimeMillis() - currTime) + " ms");
 		
 		return listFiles;
 	}
 	
 	private void enrichImageFileWithExifInfo(ImageFile imageFile) {
 		ExifFileInfo exifFileInfo = exifService.getExifInfoFromImageFile(imageFile.getImageFile());
 		String cameraModel4ch = imageFileValidator.getCameraModel4chForCameraModel(exifFileInfo.getCameraModel());
 		imageFile.setExifFileInfo(exifFileInfo);
 		imageFile.setCameraModel4ch(cameraModel4ch);
 	}
 	
 }
