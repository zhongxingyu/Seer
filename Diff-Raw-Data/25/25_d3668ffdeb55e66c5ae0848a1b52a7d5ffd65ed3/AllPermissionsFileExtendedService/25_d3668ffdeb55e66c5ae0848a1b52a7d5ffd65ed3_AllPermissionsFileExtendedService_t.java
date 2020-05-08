 package org.pillarone.riskanalytics.application.client;
 
 import com.ulcjava.base.client.FileContents;
 import com.ulcjava.base.client.ServiceException;
 import com.ulcjava.base.client.UIComponent;
 import com.ulcjava.base.shared.FileChooserConfig;
 import com.ulcjava.base.trusted.AllPermissionsFileService;
 
 import java.io.File;
 import java.io.InputStream;
 
 /**
  * @author fouad.jaada@intuitive-collaboration.com
  */
 public class AllPermissionsFileExtendedService extends AllPermissionsFileService {
 
     @Override
    public FileContents storeFile(String filePath, boolean confirmBeforeOverwrite, boolean allowChoosingNewFileInOverwritePrompt, FileChooserConfig fileChooserConfig, InputStream in) throws ServiceException {
         createDirectory(filePath);
        return super.storeFile(filePath, confirmBeforeOverwrite, allowChoosingNewFileInOverwritePrompt, fileChooserConfig, in);
     }
 
     @Override
    public FileContents storeFile(FileChooserConfig fileChooserConfig, UIComponent parent, boolean confirmBeforeOverwrite, boolean allowChoosingNewFileInOverwritePrompt, InputStream in) throws ServiceException {
         createDirectory(fileChooserConfig.getSelectedFile());
        return super.storeFile(fileChooserConfig, parent, confirmBeforeOverwrite, allowChoosingNewFileInOverwritePrompt, in);
     }
 
     private void createDirectory(String filePath) {
         final int index = filePath.lastIndexOf(File.separator);
         if (index != -1) {
             File f = new File(filePath.substring(0, index));
             if (!f.exists()) {
                 if(!f.mkdirs()) {
                     throw new RuntimeException("Failed to create directories on client.");
                 }
             }
         }
     }
 }
