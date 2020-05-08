 package telnet;
 
 import java.awt.Frame;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Scanner;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import telnet.ScrollingTextPane.TextPane;
 
 public class ManagedFile {
 
 	private TextPane inputArea;
 	private File file;
 	private boolean saved = true;
 
 	public void promptSaveOnQuit() {
 		if (!saved) {
 			promptSave("Would you like to save before quitting?");
 		}
 	}
 
 	public void promptNew() {
 		promptSave("Would you like to save before starting a new file?");
 		file = null;
 		saved = true;
		inputArea.setText("");
 
 	}
 
 	public boolean promptOpen() {
 		promptSave("Would you like to save before opening a file?");
 		if (selectFile("Open")) {
 			inputArea.setText("");
 			try {
 				Scanner f = new Scanner(new FileReader(file));
 				while (f.hasNextLine()) {
 					inputArea.append(f.nextLine());
 				}
 				inputArea.setText(inputArea.getText().substring(0,
 						inputArea.getText().length() - 1));
 			} catch (Exception e1) {
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public void promptSave() {
 		promptSave(null);
 	}
 
 	public void promptSave(String prompt) {
 		if (!saved && (prompt == null || prompt(prompt))) {
 			if (file == null) {
 				saveDialog("Save");
 			} else {
 				save();
 			}
 		}
 	}
 
 	public void promptSaveAs() {
 		saveDialog("SaveAs");
 	}
 
 	private void saveDialog(String title) {
 		File tempFile = file;
 		if (selectFile(title)) {
 			if (file.exists()) {
 				if (prompt("Would you like to overwrite this file?")) {
 					save();
 				} else {
 					file = tempFile;
 				}
 			} else {
 				save();
 			}
 		} else {
 			save();
 		}
 	}
 
 	private boolean prompt(String msg) {
 		return JOptionPane.showConfirmDialog(new Frame(), msg, "Are You Sure?",
 				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0;
 	}
 
 	private boolean selectFile(String dialogType) {
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle(dialogType);
 		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.setFileFilter(new FileFilter() {
 
 			@Override
 			public boolean accept(File f) {
 				if (f.isDirectory()) {
 					return true;
 				}
 				return isClojureFile(f);
 			}
 
 			@Override
 			public String getDescription() {
 				return "Clojure Files";
 			}
 		});
 
 		boolean toReturn = fc.showSaveDialog(fc) == JFileChooser.APPROVE_OPTION;
 		if (toReturn) {
 			file = fc.getSelectedFile();
 			if (!isClojureFile(file)) {
 				file = new File(file.toString() + ".clj");
 			}
 		}
 		return toReturn;
 	}
 
 	private boolean isClojureFile(File f) {
 		String ext = null;
 		String s = f.getName();
 		int i = s.lastIndexOf('.');
 
 		if (i > 0 && i < s.length() - 1) {
 			ext = s.substring(i + 1).toLowerCase();
 		}
 		return (ext == null ? false : ext.equals("clj"));
 	}
 
 	private void save() {
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(file));
 			out.write(inputArea.getText());
 			out.close();
 			saved = true;
 		} catch (Exception e1) {
 		}
 	}
 
 	public void setSaved(boolean saved) {
 		this.saved = saved;
 	}
 
 	public void setInputSource(TextPane inputArea) {
 		this.inputArea = inputArea;
 	}
 
 	public String toString() {
 		return (file == null ? "File not saved" : file.getPath()) + " ";
 	}
 
 }
