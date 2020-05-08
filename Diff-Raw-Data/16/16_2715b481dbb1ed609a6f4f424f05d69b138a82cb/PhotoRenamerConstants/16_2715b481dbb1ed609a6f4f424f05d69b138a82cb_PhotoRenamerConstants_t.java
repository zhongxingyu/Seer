 package org.zephir.photorenamer.core;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 public class PhotoRenamerConstants {
 
 	private PhotoRenamerConstants() {}
 
	public static final String VERSION = "1.5.6";
 
 	public static final String SUFFIX_ORIGINAL_FILENAME_TOKEN = "%ori%";
 	
 	public static final String DEFAULT_PATTERN = "yyyy_MMdd_HHmmss";
 	
 	public static final String USER_DIR = System.getProperty("user.dir");
 
 	public static final List<String> FILES_WITH_METADATA_EXTENSION_LIST = Arrays.asList(new String[] {"jpg", "jpeg", "tiff", "psd", "png", "bmp", "gif", "nef", "cr2", "orf", "arw", "rw2"});
 	public static final List<String> FILES_ROTABLE = Arrays.asList(new String[] {"jpg", "jpeg"});
 	public static final List<String> FILES_TO_RENAME_EXTENSION_LIST = Arrays.asList(new String[] {"avi", "mpg", "mov", "wmv", "mts"});
 	
 	// command-line arguments
 	public static final String COMMAND_LINE_ARG_PATTERN = "pattern";
 	public static final String COMMAND_LINE_ARG_DELTA = "delta";
 	public static final String COMMAND_LINE_ARG_FOLDER = "folder";
 	public static final String COMMAND_LINE_ARG_SUFFIX = "suffix";
 	public static final String COMMAND_LINE_ARG_EXTRA = "extra";
 	public static final String COMMAND_LINE_ARG_ROTATE = "rotate";
 	public static final String COMMAND_LINE_ARG_HELP = "?";
 	public static final String COMMAND_LINE_USAGE = "Usage: java -jar PhotoRenamer.jar"
 													+ " [--" + COMMAND_LINE_ARG_FOLDER + " <folderToProcess>]"
 													+ " [--" + COMMAND_LINE_ARG_DELTA + " (-)(<deltaInMinutes>m|<deltaInSeconds>s|<deltaInHours>h|<deltaInHours>d]"
 													+ " [--" + COMMAND_LINE_ARG_PATTERN + " <SimpleDateFormatPattern>]"
 													+ " [--" + COMMAND_LINE_ARG_SUFFIX + " <suffixText>]"
 													+ " [--" + COMMAND_LINE_ARG_EXTRA + "]  [--" + COMMAND_LINE_ARG_ROTATE + "]\n"
 													+ " If one parameter contains spaces, you need to add ' around it (e.g.: --suffix ' - by wIndou').\n"
 													+ " Inside the suffix, you can use "+SUFFIX_ORIGINAL_FILENAME_TOKEN+" to use the original filename without extension";
 	public static final String FORM_HELP = "PhotoRenamer v" + VERSION + " made by wInd (wInd@zephir.org)\n\n" +
 		
 		"When you hit the 'Proceed !' button, the tool will go through the photos of the input folder,\n" +
 		"and rename them based on their EXIF data and the parameters provided\n\n" +
 		"Parameters: \n" +
 		"Input folder: folder containing the photos to rename\n" +
 		"Photo suffix: suffix to add after the date in the filename of the photos\n" +
 		"Delta: time to add or substract from the EXIF date in seconds(s)/minutes(m)/hours(h)/days(d) (e.g.: -15s, 60m, -48h, -1d, 30d3h-15s)\n" +
 		"Date pattern: format of the date (y: year M: month, d: day, H: hour, m: min, s: sec)\n" +
 		"Rename extra files: whether the extra files should be renamed too based on their creation date (" + StringUtils.join(FILES_TO_RENAME_EXTENSION_LIST, ", ") + ")\n" +
 		"Rotate images: auto rotation of the images based on the EXIF orientation\n";
 }
