 package net.sf.anathema.basics.swt.file;
 
 import static net.sf.anathema.basics.swt.file.FileTypes.*;
 
 import java.io.File;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 
 public class FileChoosing {
  // TODO Extensions nicht verdoppeln
  private static final String PDF_EXTENSION_NAME = "Portable Document Format (" + PDF_EXTENSION + ")";
  private static final String NOTE_EXTENSION_NAME = "Anathema Note (" + NOTE_EXTENSION + ")";
 
   public static File savePdfFile(File startingDirectory, Shell shell) {
     return getFile(
         startingDirectory,
         shell,
        new String[] { PDF_EXTENSION },
        new String[] { PDF_EXTENSION_NAME },
         SWT.SAVE);
   }
 
   public static File openNoteFile(File startingDirectory, Shell shell) {
     return getFile(
         startingDirectory,
         shell,
        new String[] { NOTE_EXTENSION },
        new String[] { NOTE_EXTENSION_NAME },
         SWT.OPEN);
   }
 
   private static File getFile(File startingDirectory, Shell shell, String[] extensions, String[] filterNames, int style) {
     FileDialog dialog = new FileDialog(shell, style);
     if (startingDirectory != null) {
       dialog.setFileName(startingDirectory.getPath());
     }
     if (extensions != null) {
       dialog.setFilterExtensions(extensions);
       dialog.setFilterNames(filterNames);
     }
     String file = dialog.open();
     if (file != null) {
       file = file.trim();
       if (file.length() > 0) {
         return new File(file);
       }
     }
     return null;
   }
 }
