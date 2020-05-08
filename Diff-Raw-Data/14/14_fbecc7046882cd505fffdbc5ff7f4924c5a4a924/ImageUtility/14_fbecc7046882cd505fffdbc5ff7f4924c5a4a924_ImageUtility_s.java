 package org.lazydog.comic.image.utility;
 
 import org.lazydog.comic.criteria.criterion.ComparisonOperation;
 import org.lazydog.comic.criteria.criterion.LogicalOperation;
 import org.lazydog.comic.criteria.Criteria;
 import org.lazydog.comic.criteria.CriteriaFactory;
 import org.lazydog.comic.model.Image;
 import org.lazydog.comic.model.ImageType;
import org.lazydog.comic.model.User;
 import org.lazydog.comic.service.ComicService;
 import java.io.File;
 import java.io.PrintStream;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 
 /**
  * Image utility.
  *
  * @author  Ron Rickard
  */
 public class ImageUtility {
 
     private static final String ADD_COMMAND = "add";
     private static final String REMOVE_COMMAND = "remove";
     private static final String INCOMING_TYPE_VALUE = "Incoming";
     private static final String TRASH_TYPE_VALUE = "Trash";
 
     private ComicService comicService;
 
     /**
      * Constructor.
      *
      * @throws  Exception  if unable to initialize ImageUtility.
      */
     private ImageUtility()
             throws Exception {
 
         // Declare.
         Context context;
 
         // Initialize the context.
         context = new InitialContext();
 
         // Initialize the service.
         comicService = (ComicService)context.lookup("ejb/ComicService");
     }
 
     /**
      * Send the error (exception) to the print stream.
      *
      * @param  printStream  the print stream.
      * @param  error        the error.
      */
     private static void error(PrintStream printStream,
                              Exception error) {
         printStream.println("The following error has occurred:");
         error.printStackTrace(printStream);
 
     }
 
     /**
      * Strip the export directory name off the directory path.
      * 
      * @param  directoryPath  the directory path.
      * 
      * @return  the directory path with the export directory name stripped off.
      */
     private String getNonExportDirectoryPath(String directoryPath) {
 
         return directoryPath.replace("/export", "");
     }
 
     /**
      * Get the user.
      * 
      * @return  the user.
      * 
      * @throws  Exception  if unable to get the user.
      */
    private User getUser()
             throws Exception {
 
         // Declare.
        Criteria<User> criteria;
         CriteriaFactory criteriaFactory;
        User user;
 
         // Initialize criteria factory.
         criteriaFactory = CriteriaFactory.instance();
 
         // Set the criteria.
        criteria = criteriaFactory.createCriteria(User.class);
         criteria.add(ComparisonOperation.eq("name", "admin"));
 
         // Find the user.
         user = comicService.find(criteria);
 
         return user;
     }
 
     /**
      * Get the image for the file name and directory path.
      *
      * @param  fileName       the file name.
      * @param  directoryPath  the directory path.
      *
      * @return  the image.
      *
      * @throws  Exception  if unable to get the image.
      */
     private Image getImage(String fileName,
                            String directoryPath)
             throws Exception {
 
         // Declare.
         Criteria<Image> criteria;
         CriteriaFactory criteriaFactory;
         Image image;
 
         // Initialize criteria factory.
         criteriaFactory = CriteriaFactory.instance();
 
         // Set the criteria.
         criteria = criteriaFactory.createCriteria(Image.class);
         criteria.add(ComparisonOperation.eq("fileName", fileName));
         criteria.add(LogicalOperation.and(
                 ComparisonOperation.eq("type.directoryPath", directoryPath)));
 
         // Find the image.
         image = comicService.find(criteria);
 
         return image;
     }
 
     /**
      * Get the image type for the directory path.
      * 
      * @param  directoryPath  the directory path.
      * 
      * @return  the image type.
      * 
      * @throws  Exception  if unable to get the image type.
      */
     private ImageType getImageType(String directoryPath)
             throws Exception {
 
         // Declare.
         Criteria<ImageType> criteria;
         CriteriaFactory criteriaFactory;
         ImageType imageType;
 
         // Initialize criteria factory.
         criteriaFactory = CriteriaFactory.instance();
 
         // Set the criteria.
         criteria = criteriaFactory.createCriteria(ImageType.class);
         criteria.add(ComparisonOperation.eq("directoryPath", directoryPath));
 
         // Find the image type.
         imageType = comicService.find(criteria);
 
         return imageType;
     }
 
     /**
      * Process the file per the command.
      * 
      * @param file     the file.
      * @param command  the command.
      * 
      * @throws  Exception  if unable to process the file.
      */
     private void process(File file, String command)
             throws Exception {
 
         // Check if this is the add command.
         if (command.equals(ImageUtility.ADD_COMMAND)) {
 
             // Declare.
             ImageType imageType;
 
             // Get the image type.
             imageType = this.getImageType(this.getNonExportDirectoryPath(file.getParent()));
 
             // Check if there is no image type or it is not an incoming image.
             if (imageType == null || 
                 !imageType.getValue().equals(ImageUtility.INCOMING_TYPE_VALUE)) {
                 System.out.println("The image file [" +
                         file.getCanonicalPath() +
                         "] is not in a valid directory.");
                 System.exit(1);
             }
             else {
 
                 // Declare.
                 Image image;
 
                 // Create a new image.
                 image = new Image();
                 image.setFileName(file.getName());
                 image.setType(imageType);
 
                 // Add the image to the database.
                 this.comicService.save(image, this.getUser());
             }
         }
 
         // Check if this is the delete command.
         else if (command.equals(ImageUtility.REMOVE_COMMAND)) {
 
             // Declare.
             Image image;
 
             // Get the image.
             image = this.getImage(file.getName(), this.getNonExportDirectoryPath(file.getParent()));
 
             // Check if there is no image.
             if (image == null) {
                 System.out.println("The image file [" +
                         file.getCanonicalPath() +
                         "] does not exist in the database.");
                 System.exit(1);
             }
             // Check if the image type is not an incoming or trash image.
             else if (!image.getType().getValue().equals(ImageUtility.INCOMING_TYPE_VALUE) &&
                      !image.getType().getValue().equals(ImageUtility.TRASH_TYPE_VALUE)) {
                 System.out.println("The image file [" +
                         file.getCanonicalPath() +
                         "] is not in a valid directory.");
                 System.exit(1);
             }
             else {
                 // Remove the image from the database.
                 this.comicService.remove(Image.class, image.getId());
             }
         }
     }
 
     /**
      * Send the usage to the print stream.
      *
      * @param  printStream  the print stream.
      */
     private static void usage(PrintStream printStream) {
         printStream.println("usage: ImageUtility <add | remove> <image file>");
         printStream.println("Add/remove a image file to/from the image service.");
         printStream.println();
         printStream.println("    remove <image file>      Remove a image file.");
         printStream.println("    add <image file>         Add a image file.");
     }
 
     /**
      * Main for image utility.
      * 
      * @param  arguments  the command line arguments.
      */
     public static void main(String[] arguments) {
 
         try {
 
             // Declare.
             ImageUtility imageUtility;
 
             // Initialize.
             imageUtility = new ImageUtility();
         
             // Check if there are not exactly 2 arguments and the
             // first argument is not the add or remove command.
             if (arguments.length != 2 ||
                 (!arguments[0].equals(ImageUtility.ADD_COMMAND) &&
                  !arguments[0].equals(ImageUtility.REMOVE_COMMAND))) {
 
                 // Print the usage and exit.
                 ImageUtility.usage(System.out);
                 System.exit(1);
             }
             else {
 
                 // Declare.
                 File file;
 
                 // Set the file.
                 file = new File(arguments[1]);
 
                 // Check if the file exists.
                 if (!file.exists()) {
                     System.out.println("The image file [" +
                             file.getCanonicalPath() + "] does not exist.");
                     System.exit(1);
                 }
                 else {
 
                     // Process the file.
                     imageUtility.process(new File(file.getCanonicalPath()), arguments[0]);
 
                     System.out.println("The image file [" +
                             file.getCanonicalPath() + "] " + arguments[0] +
                             " was successful.");
                     System.exit(0);
                 }
             }
         }
         catch(Exception e) {
 
             // Print the error message and exit.
             ImageUtility.error(System.out, e);
             System.exit(1);
         }
     }
 }
