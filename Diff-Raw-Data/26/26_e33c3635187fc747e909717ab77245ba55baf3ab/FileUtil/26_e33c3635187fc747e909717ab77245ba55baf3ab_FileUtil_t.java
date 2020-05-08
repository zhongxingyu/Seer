 package ch.guitarpracticebuddy.util;
 
 import ch.guitarpracticebuddy.domain.ExerciseAttachment;
 import ch.guitarpracticebuddy.domain.ExerciseDefinition;
 import com.google.common.io.Closeables;
 import com.google.common.io.Files;
 import javafx.scene.image.Image;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class FileUtil {
 
     public static final String SEPARATOR = "/";
     public static final String EXERCISE_FOLDER_PREFIX = "exercise_data/exercise_";
 
     public static List<String> copyFilesToApplicationHome(List<File> selectedFiles, ExerciseDefinition exerciseDefinition) {
         List<String> fileNames = new ArrayList<String>();
         for (File selectedFile : selectedFiles) {
             try {
                 new File(getApplicationHome() + SEPARATOR + getExerciseDirectory(exerciseDefinition)).mkdirs();
                 final File destination = new File(getExerciseDirectoryAbsolute(exerciseDefinition) + selectedFile.getName());
                 Files.copy(selectedFile, destination);
                 fileNames.add(selectedFile.getName());
             } catch (IOException e) {
                 throw new IllegalArgumentException("Cannot copy file", e);
             }
         }
         return fileNames;
     }
 
     private static String getExerciseDirectoryAbsolute(ExerciseDefinition exerciseDefinition) {
         return getApplicationHome() + SEPARATOR + getExerciseDirectory(exerciseDefinition) + SEPARATOR;
     }
 
     private static String getExerciseDirectory(ExerciseDefinition exerciseDefinition) {
        return EXERCISE_FOLDER_PREFIX + exerciseDefinition.getFolder();
     }
 
     private static String getApplicationHome() {
         return System.getProperty("guitarbuddy.home");
     }
 
     public static File toFiles(ExerciseDefinition exerciseDefinition, ExerciseAttachment exerciseAttachment) {
         return new File(getExerciseDirectoryAbsolute(exerciseDefinition) + exerciseAttachment.getFilePath());
     }
 
     public static Image loadImage(ExerciseDefinition exerciseDefinition, ExerciseAttachment exerciseAttachment) {
         FileInputStream fileInputStream = null;
         try {
             fileInputStream = new FileInputStream(toFiles(exerciseDefinition, exerciseAttachment));
             Image image = new Image(fileInputStream);
             return image;
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 Closeables.close(fileInputStream, true);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return null;
 
     }
 
 }
