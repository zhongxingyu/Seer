 package gsingh.learnkirtan;
 
import gsingh.learnkirtan.component.shabadeditor.ShabadEditor;
 import gsingh.learnkirtan.shabad.Shabad;
 import gsingh.learnkirtan.utility.DialogUtility;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.io.FileUtils;
 import org.w3c.dom.Document;
 
 /**
  * Responsible for managing the opening and saving of files
  * 
  * @author Gulshan
  * 
  */
 public class FileManager {
 
 	public enum SaveResult {
 		NOTSAVEDCANCELLED, NOTSAVED, SAVEDEXISTING, SAVEDNEW, OVERWRITE
 	}
 
 	/** The file extension of Shabad files */
 	private static final String EXTENSION = ".sbd";
 
 	/** The path to the settings files */
 	private static final String SETTINGS_FILE = "config";
 
 	/** The filechooser used for opening and saving files */
 	private final JFileChooser fc;
 	{
 		fc = new JFileChooser();
 		FileFilter filter = new FileNameExtensionFilter("SBD (Shabad) File",
 				"sbd");
 		fc.setFileFilter(filter);
 	}
 
 	/**
 	 * The file in which your shabad will be saved or was opened from. When the
 	 * program is first started, it has the value of {@code null}.
 	 */
 	private File curFile;
 
 	/**
 	 * Writes a DOM to the settings file
 	 * 
 	 * @param dom
 	 *            The DOM to persist
 	 * @throws TransformerFactoryConfigurationError
 	 * @throws TransformerException
 	 */
 	public void saveSettings(Document dom)
 			throws TransformerFactoryConfigurationError, TransformerException {
 		// Prepare the DOM document for writing
 		Source source = new DOMSource(dom);
 
 		// Prepare the output file
 		File file = new File(SETTINGS_FILE);
 		Result result = new StreamResult(file);
 
 		// Write the DOM document to the file
 		Transformer xformer = TransformerFactory.newInstance().newTransformer();
 		xformer.transform(source, result);
 	}
 
 	/**
 	 * Prompts the user if they would like to save and saves if accepted
 	 * 
 	 * @return a {@code SaveResult} describing the action taken
 	 * @throws IOException
 	 */
 	public SaveResult safeSave(Shabad shabad) throws IOException {
 
 		int result = DialogUtility.showSaveDialog();
 		if (DialogUtility.isCancelledOrClosed(result))
 			return SaveResult.NOTSAVEDCANCELLED;
 
 		if (DialogUtility.isYes(result)) {
 			return saveShabad(shabad);
 		}
 
 		return SaveResult.NOTSAVED;
 	}
 
 	/**
 	 * Sets the invariants for a new file
 	 */
 	public void newFile() {
 		curFile = null;
 	}
 
 	/**
 	 * Saves the text in the shabadEditor to the file specified. If no file is
 	 * specified, the user is prompted to specify one.
 	 * 
 	 * @throws IOException
 	 */
 	public SaveResult saveShabad(Shabad shabad) throws IOException {
 
 		// If no file is currently opened
 		if (!isFileOpened()) {
 			int returnVal = fc.showSaveDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				curFile = fc.getSelectedFile();
 
 				curFile = appendFileExtension(curFile);
 
 				// If the selected file exists and we're about to overwrite it
 				if (curFile.exists()) {
 					int result = DialogUtility.showOverwriteDialog();
 
 					// If the user is OK with overwriting it
 					if (DialogUtility.isOK(result)) {
 						write(shabad);
 						return SaveResult.OVERWRITE;
 					} else {
 						return SaveResult.NOTSAVEDCANCELLED;
 					}
 				} else {
 					write(shabad);
 					return SaveResult.SAVEDNEW;
 				}
 			} else {
 				return SaveResult.NOTSAVEDCANCELLED;
 			}
 		} else {
 			write(shabad);
 			return SaveResult.SAVEDEXISTING;
 		}
 	}
 
 	/**
 	 * Returns true if a file is currently opened, false otherwise
 	 * 
 	 */
 	public boolean isFileOpened() {
 		if (curFile == null)
 			return false;
 		else
 			return true;
 	}
 
 	/**
 	 * Append the file extension if not present
 	 * 
 	 * @param file
 	 *            The file to append the extension to
 	 */
 	private File appendFileExtension(File file) {
 		String filename = file.getName();
 		if (isMissingExtension(filename)) {
 			curFile = new File(curFile.getAbsolutePath() + EXTENSION);
 		}
 
 		return curFile;
 	}
 
 	/**
 	 * @param filename
 	 *            the filename to check
 	 * @return true if filename does not end in the file extension
 	 */
 	private boolean isMissingExtension(String filename) {
 		if (filename.length() <= 4
 				|| !filename.substring(filename.length() - 4).equals(EXTENSION))
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * Writes a {@link Shabad} to a file
 	 * 
 	 * @throws IOException
 	 */
 	private void write(Shabad shabad) throws IOException {
 		BufferedWriter bw = new BufferedWriter(new FileWriter(curFile));
 		bw.write(shabad.getShabadText());
 		bw.close();
 	}
 
 	/**
 	 * Prompts the user for a file to open and opens the selected file.
 	 * 
 	 * @return true if a file was opened, false otherwise
 	 * 
 	 * @throws IOException
 	 */
	public boolean openFile(ShabadEditor shabadEditor) throws IOException {
 		int returnVal = fc.showOpenDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 
 			if (file.exists()) {
 				shabadEditor.setText(FileUtils.readFileToString(file));
 
 				curFile = file;
 				return true;
 			} else {
 				DialogUtility.showFileDoesntExistDialog();
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Gets the name of the currently opened file
 	 * 
 	 * @return The name of the opened file. If no file is opened, returns the
 	 *         empty string
 	 */
 	public String getFileName() {
 		if (isFileOpened()) {
 			return curFile.getName();
 		} else {
 			return "";
 		}
 	}
 }
