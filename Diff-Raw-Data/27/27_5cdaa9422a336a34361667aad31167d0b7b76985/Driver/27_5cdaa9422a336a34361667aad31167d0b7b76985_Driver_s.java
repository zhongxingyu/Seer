 package net.ccaper.LandscapePortraitImageSort;
 
 import java.io.File;
 import java.util.List;
 
 import net.ccaper.LandscapePortraitImageSort.enums.ImageOrientation;
 import net.ccaper.LandscapePortraitImageSort.service.CopyImage;
 import net.ccaper.LandscapePortraitImageSort.service.IterateDirectories;
 import net.ccaper.LandscapePortraitImageSort.serviceImpl.CopyImageImpl;
 import net.ccaper.LandscapePortraitImageSort.serviceImpl.IterateDirectoriesImpl;
 import net.ccaper.LandscapePortraitImageSort.spring.AppConfig;
 import net.ccaper.LandscapePortraitImageSort.util.LandscapePortraitOrientationUtils;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
 public class Driver {
   public static final Log LOG = LogFactory.getLog(Driver.class);
 
   public static void main(String[] args) {
     @SuppressWarnings("resource")
     ApplicationContext context = new AnnotationConfigApplicationContext(
         AppConfig.class);
     // TODO: check if exists and is readable
     File startDirectory = (File) context.getBean("startDirectory");
     // TODO: check if exists and is writable
     File destinationDirectory = (File) context.getBean("destinationDirectory");
     @SuppressWarnings("unchecked")
     List<File> ignoreDirectories = (List<File>) context
     .getBean("ignoreDirectories");
     @SuppressWarnings("unchecked")
     List<File> ignoreFiles = (List<File>) context.getBean("ignoreFiles");
 
     Driver.LOG.info("Starting Landscape Portrait Image Sort");
     Driver.LOG.info("Start directory: " + startDirectory);
     Driver.LOG.info("Destination directory: " + destinationDirectory);
     Driver.LOG.info("Ignore directories: "
         + StringUtils.join(ignoreDirectories, ", "));
     Driver.LOG.info("Ignore files: " + StringUtils.join(ignoreFiles, ", "));
     Driver.LOG.info("Filtering files for extensions: "
         + StringUtils.join(AppConfig.IMAGE_TYPES, ", "));
     IterateDirectories iterateDirectories = new IterateDirectoriesImpl(
         startDirectory, ignoreFiles, ignoreDirectories);
     File file = iterateDirectories.getFile();
     LandscapePortraitOrientationUtils landscapePortraitUtils = new LandscapePortraitOrientationUtils();
     CopyImage copyImage = new CopyImageImpl(startDirectory,
         destinationDirectory);
     while (file != null) {
      file = iterateDirectories.getFile();
       ImageOrientation orientation = landscapePortraitUtils
           .getImageOrientation(file);
      File copiedFile = copyImage.copyImageToOrientationDirectory(file,
          orientation);
      Driver.LOG.info(String.format(
          "File '%s' was found to have orientation %s, and was copied '%s'",
          file.getAbsolutePath(), orientation, copiedFile.getAbsolutePath()));
     }
     Driver.LOG.info(String.format("Number files found: %d",
         iterateDirectories.getNumberFilesFound()));
     Driver.LOG.info(String.format("Number files skipped: %d",
         iterateDirectories.getNumberFilesSkipped()));
     Driver.LOG.info(String.format("Number files not skipped: %d",
         iterateDirectories.getNumberFilesNotSkipped()));
     Driver.LOG.info(String.format("Number directories found: %d",
         iterateDirectories.getNumberDirectoriesFound()));
     Driver.LOG.info(String.format("Number directories skipped: %d",
         iterateDirectories.getNumberDirectoriesSkipped()));
     Driver.LOG.info(String.format("Number directories not skipped: %d",
         iterateDirectories.getNumberDirectoriesNotSkipped()));
     Driver.LOG.info(String.format("Number non image files: %d",
         iterateDirectories.getNumberNonImageFiles()));
     Driver.LOG.info(String.format("Number of landscape orientation images: %d",
         landscapePortraitUtils.getNumberLandscapeOrientationImages()));
     Driver.LOG.info(String.format("Number of portrait orientation images: %d",
         landscapePortraitUtils.getNumberPortraitOrientationImages()));
     Driver.LOG.info(String.format("Number of equal orientation images: %d",
         landscapePortraitUtils.getNumberEqualOrientationImages()));
     Driver.LOG.info(String.format(
        "Number of images where orietation could not be determined: %d",
         landscapePortraitUtils.getNumberOrientationProblems()));
     Driver.LOG.info(String.format("Number of images successfully copied: %d",
         copyImage.getNumberFileCopySuccess()));
     Driver.LOG.info(String.format(
         "Number of images not successfully copied: %d",
         copyImage.getNumberFileCopyFailures()));
     Driver.LOG.info("Ending Landscape Portrait Image Sort");
   }
 }
