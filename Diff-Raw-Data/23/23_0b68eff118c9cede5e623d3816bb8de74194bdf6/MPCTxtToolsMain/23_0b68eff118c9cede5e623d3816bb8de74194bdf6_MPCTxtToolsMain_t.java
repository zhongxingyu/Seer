 package main;
 
 import instruments.InstrumentReplacerMain;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 /*
 import javax.swing.JScrollPane;
 import javax.swing.JLabel;
  */
 import errorChecking.CancelException;
 import errorChecking.ErrorDialogs;
 import fileSplitter.FileSplitterMain;
 
 /**
  * A program that implements interesting text
  * file editing features into a more intuitive,
  * more user-friendly application... hopefully.
  * @author RehdBlob
  * @version 1.06
  * @since 1.00a
  * @since 2011.0802
  */
 public class MPCTxtToolsMain {
 
     private static final String genHelp = "Mario Paint Composer Text Tools\n" +
             "Programmed by: RehdBlob (c)2011-2013\n" +
             "Version: 1.07\n\n" +
             "Meant to help replace instruments, split files and increase tempo,\n" +
             "or delete instruments with ease.\n" +
             "More functionalities will be added... eventually.\n\n" +
             "Disclaimer: While every effort is made to back up files,\n" +
             "(The program creates \"old\" files before re-writing\n" +
             "anything) I cannot be responsible for any possible\n" +
             "data,file loss, and/or damages that may occur from\n" +
             "using this program.\n\n" +
             "Since v1.05, the \"Prefs folder backup\" will back up all\n" +
             "of the files currently residing within the directory\n" +
             "that the MPC Text Tools program is located. The program\n" +
             "saves this backup in a folder called \"Prefsbackup\"\n\n" +
             "More help options are below, if needed.\n";
 
     private static final String instRepHelp = "General steps:\n" +
             "1. Enter filename.\n" +
            "2. Select the instrument that you wish to replace.\n" +
            "3. Select the instrument that you wish to replace that\n" +
             "instrument with. \"Delete\" is also an option.\n" +
             "4. The program does the rest...";
 
     private static final String fileSplitHelp = "General steps:\n" +
             "1. Enter filename.\n" +
             "2. Enter the number of times that you wish to multiply\n" +
             "the current tempo by. An added functionality for handling\n" +
             "tempo gaps will probably be programmed in in the near future,\n" +
             "but for now, it's not in the program.\n" +
             "3. The program will now increase the tempo of that file by the\n" +
             "specified number of times, creating intermediate files and\n" +
             "updating the MarioPaintSongList.txt file if necessary.";
 
     /**
      * Outputs the main menu of the program.
      */
     public static void main(String[] args) {
         boolean repeat = true;
         while (repeat) {
             Object[] options = {"Instrument Replacer",
                     "File splitter", "Prefs folder backup",
                     "Help/Info", "Exit"};
             int n = JOptionPane.showOptionDialog(null,
                    "Select the function you want to use:\n\n\n",
                    "Mario Paint Composer Text Tools 1.07",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null, options, options[3]);
 
             switch(n) {
             case 0:
                 try	{
                     InstrumentReplacerMain.start(fileName());
                 } catch(NullPointerException e)	{
                     /* Do nothing! */
                 } catch(CancelException e) {
                     /* Do nothing! */
                 } catch(IOException e) {
                     /* Do nothing! */
                 }
                 break;
             case 1:
                 try	{
                     FileSplitterMain.start(fileName());
                 } catch(NullPointerException e) {
                     /* Do nothing! */
                 } catch(CancelException e) {
                     /* Do nothing! */
                 } catch(IOException e) {
                     /* Do nothing! */
                 }
                 break;
             case 2:
                 try {
                     PrefsBackup.backup();
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                     ErrorDialogs.notBackedUp();
                 } catch (Exception e) {
                     e.printStackTrace();
                     ErrorDialogs.notBackedUp();
                 }
                 break;
             case 3:
                 help();
                 break;
             case 4:
                 repeat = false;
                 break;
             default:
                 repeat = false;
                 return;
             }
         }
     }
 
     /**
      * @since 1.02
      * @since 2011.0808
      */
     private static void help()
     {
         boolean repeat = true;
         while (repeat == true)
         {
             Object [] options = {"Instrument Replacer", "File Splitter",
             "Back"};
             int n = JOptionPane.showOptionDialog(null, genHelp, "General Info",
                     JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
                     null, options, options[0]);
             switch(n)
             {
             case 0:
                 JOptionPane.showMessageDialog(null, instRepHelp,
                         "Instrument Replacer Help", JOptionPane.INFORMATION_MESSAGE);
                 break;
             case 1:
                 JOptionPane.showMessageDialog(null, fileSplitHelp,
                         "File Splitter Help", JOptionPane.INFORMATION_MESSAGE);
                 break;
                 /*
 				case 2:
 					JOptionPane.showMessageDialog(null, new JScrollPane(new JLabel(changeLog)),
 							"Changelog", JOptionPane.INFORMATION_MESSAGE);
 					break;
                  */
             default:
                 repeat = false;
                 break;
             }
         }
     }
 
     @SuppressWarnings("unused")
    private static void notDone() {
         JOptionPane.showMessageDialog(null, "Feature not implemented" +
                 " yet!", "Error", JOptionPane.WARNING_MESSAGE);
     }
 
     /**
      * 
      * @return Some file that the user chooses.
      * @throws CancelException
      * @throws IOException
      */
     public static File fileName() throws CancelException, IOException {
         JFileChooser fd = new JFileChooser(".");
         FileFilter filter = new FileNameExtensionFilter(
                 "Text file (*.txt)", "txt");
         fd.addChoosableFileFilter(filter);
         int returnVal = fd.showOpenDialog(null);
         if(returnVal == JFileChooser.APPROVE_OPTION) {
             return fd.getSelectedFile();
         }
         else if (returnVal == JFileChooser.CANCEL_OPTION)
             throw new CancelException();
         else {
             System.out.print("Error opening file!");
             throw new IOException();
         }
     }
 }
