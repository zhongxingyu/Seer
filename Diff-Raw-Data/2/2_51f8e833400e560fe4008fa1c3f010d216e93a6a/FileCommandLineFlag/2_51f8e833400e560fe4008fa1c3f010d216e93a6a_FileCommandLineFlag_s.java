 package org.sylvanbytes.util.cli.flag;
 
 import org.sylvanbytes.util.cli.validator.exception.InvalidFilePermissionsException;
 import org.sylvanbytes.util.cli.validator.exception.StringIsNotAFileException;
 import org.sylvanbytes.util.cli.validator.CommandLineFlagValueValidator;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * A command line flag used for specifing flags
  */
 public class FileCommandLineFlag extends CommandLineFlagBase {
 
     private List<File> files;
 
 
    enum FileVisibility{
         READ,
         WRITE,
         EXECUTE
     }
 
     /**
      * Create a command-line flag with an array of keys, a help string and an array of file visibilities (read, write, execue),
      * that must pass for the command-line flag's value to be stored.
      */
     public FileCommandLineFlag(String[] flagKey, String versionHelpStr, FileVisibility[] visibilities) {
         this(flagKey, versionHelpStr, Arrays.asList(visibilities));
     }
 
     /**
      * Create a command-line flag that is meant to point to a file with a particular visibilities (read, write, execute).
      */
     public FileCommandLineFlag(String[] flagKey, String versionHelpStr, FileVisibility fileVisibility) {
         this(flagKey, versionHelpStr, new FileVisibility[]{fileVisibility});
     }
 
 
     private FileCommandLineFlag(String[] flagKey, String versionHelpStr, final Collection<FileVisibility> visibilities) {
         super(flagKey, versionHelpStr, new CommandLineFlagValueValidator() {
             @Override
             public void validate(String str) {
                 File file = new File(str);
                 if(file.isFile()){
                     throw new StringIsNotAFileException(file);
                 }
 
                 if(!file.canRead() && visibilities.contains(FileVisibility.READ)){
                     throw new InvalidFilePermissionsException(file, InvalidFilePermissionsException.Permission.READ);
                 }
 
                 if(file.canWrite() && visibilities.contains(FileVisibility.WRITE)){
                     throw new InvalidFilePermissionsException(file, InvalidFilePermissionsException.Permission.WRITE);
                 }
 
                 if(file.canExecute() && visibilities.contains(FileVisibility.EXECUTE)){
                     throw new InvalidFilePermissionsException(file, InvalidFilePermissionsException.Permission.EXECUTE);
                 }
             }
         });
         files = new ArrayList<>();
     }
 
     @Override
     public void addValue(String value) {
         getValidator().validate(value);
         files.add(new File(value));
     }
 
     @Override
     public List<String> getValueAsString() {
        List<String> filePaths = new ArrayList<>();
        for(File file : files){
            filePaths.add(file.getPath());
        }
         return filePaths;
     }
 
     @Override
     public List<Boolean> getValueAsBoolean() {
         List<Boolean> filePaths = new ArrayList<>();
         for(File file : files){
             filePaths.add(Boolean.getBoolean(file.getPath()));
         }
         return filePaths;
     }
 }
