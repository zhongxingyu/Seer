 package org.imirsel.nema.webapp.webflow.validator;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.springframework.binding.message.MessageBuilder;
 import org.springframework.binding.message.MessageContext;
 import org.springframework.binding.validation.ValidationContext;
 
 import org.imirsel.nema.model.ExecutableBundle.ExecutableType;
 import org.imirsel.nema.webapp.model.UploadedExecutableBundle;
 
 /**
  * 
  * @author shirk
  */
 public class UploadedExecutableBundleValidator {
    private static final Logger logger = 
       Logger.getLogger(UploadedExecutableBundleValidator.class.getName());
    
    public void validateUpload(UploadedExecutableBundle uploadedExecutable,
          ValidationContext context) {
       MessageContext messages = context.getMessageContext();
 
       String fileName = uploadedExecutable.getFileName();
       String fileExtension = fileName.substring(fileName.length() - 3);
 
      // First make sure the uploaded file type matches what is allowed for
       // the selected executable type.
       if (uploadedExecutable.getType() == ExecutableType.JAVA) {
          if (!fileExtension.equalsIgnoreCase("jar")
                && !fileExtension.equalsIgnoreCase("zip")) {
             messages.addMessage(new MessageBuilder().error().source("file")
                   .defaultText(
                         "Invalid file type. For Java "
                               + "executables, only JAVA or ZIP files are "
                               + "allowed.").build());
             return;
          }
       } else {
          if (!fileExtension.equalsIgnoreCase("zip")) {
             messages
                   .addMessage(new MessageBuilder().error().source("file")
                         .defaultText(
                               "Invalid file type. Only ZIP files are allowed.")
                         .build());
             return;
          }
       }
 
      // Next make sure the file is readable in ZIP format. Ensure not corrupt.
       boolean isReadable = false;
       try {
          isReadable = uploadedExecutable.readableAsZip();
       } catch (IOException e) {
          logger.warning(e.getMessage());
       }
 
       if (!isReadable) {
          messages.addMessage(new MessageBuilder().error().source("file")
                .defaultText("Uploaded file is corrupt or cannot be read.")
                .build());
          return;
       }
 
       // Finally, validate the the specified executable file exists within
       // the uploaded archive.
       try {
          if (!uploadedExecutable.containsExecutable()) {
             String exeFileType;
             if (uploadedExecutable.getType() == ExecutableType.JAVA) {
                exeFileType = "main class";
             } else {
                exeFileType = "executable file";
             }
             messages.addMessage(new MessageBuilder().error().source("file")
                   .defaultText(
                         "The specified " + exeFileType
                               + " was not found in the uploaded archive.")
                   .build());
             return;
          }
       } catch (IOException e) {
          logger.warning(e.getMessage());
       }
    }
 
    public boolean isNullOrEmpty(String str) {
       return str == null || str.trim().equals("");
    }
 
 }
