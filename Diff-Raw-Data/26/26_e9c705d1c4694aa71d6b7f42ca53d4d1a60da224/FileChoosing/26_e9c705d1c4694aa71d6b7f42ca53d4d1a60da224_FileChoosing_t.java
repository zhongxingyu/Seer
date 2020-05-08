 package net.sf.anathema.basics.swt.file;
 
 import static net.sf.anathema.basics.swt.file.FileTypes.*;
 
 import java.io.File;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 
 public class FileChoosing {
  private static final String ANY_PDF_FILTER = '*' + PDF_EXTENSION;
  private static final String ANY_NOTE_FILTER = '*' + NOTE_EXTENSION;
  private static final String ANY_PDF_FILTER_NAME = "Portable Document Format (" + ANY_PDF_FILTER + ")";
  private static final String ANY_NOTE_FILTER_NAME = "Anathema Note (" + ANY_NOTE_FILTER + ")";
 
   public static File savePdfFile(File startingDirectory, Shell shell) {
     return getFile(
         startingDirectory,
         shell,
        new String[] { ANY_PDF_FILTER },
        new String[] { ANY_PDF_FILTER_NAME },
         SWT.SAVE);
   }
 
   public static File openNoteFile(File startingDirectory, Shell shell) {
     return getFile(
         startingDirectory,
         shell,
        new String[] { ANY_NOTE_FILTER },
        new String[] { ANY_NOTE_FILTER_NAME },
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
