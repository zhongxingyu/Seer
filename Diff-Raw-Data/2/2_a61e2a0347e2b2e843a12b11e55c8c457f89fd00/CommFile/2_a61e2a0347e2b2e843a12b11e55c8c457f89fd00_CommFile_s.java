 package uonlineeditor;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.util.regex.*;
 
 /**
  *
  * @author houjing
  */
 public abstract class CommFile {
 
 	private JFileChooser fc;
 	protected File file;
 	public boolean approved = false;
 	public static int OPEN = 0;
 	public static int SAVE = 1;
 
 	public CommFile(int mode) {
 		int ret;
 		fc = new JFileChooser();
 
 		if (mode == OPEN) {
 			System.out.println("open for");
 			fc.setDialogType(JFileChooser.OPEN_DIALOG);
 			fc.setFileFilter(getFileFilter());
 			ret = fc.showDialog(null, null);
 		}
 		else if (mode == SAVE) {
 			System.out.println("saving for");
 			fc.setDialogType(JFileChooser.SAVE_DIALOG);
 			fc.setFileFilter(getFileFilter());
 			ret = fc.showDialog(null, null);
 		}
 		else ret = JFileChooser.CANCEL_OPTION;
 
 		if (ret == JFileChooser.APPROVE_OPTION) {
 			file = fc.getSelectedFile();
 			String ext = ((FileNameExtensionFilter) fc.getFileFilter()).getExtensions()[0];
			if (!Pattern.compile("\\."+ext+"$").matcher(file.getName()).matches()) file = new File(file.getAbsolutePath() + "." + ext);
 			approved = true;
 		}
 		else approved = false;
 	}
 
 	protected CSVReader getCsvReader() {
 		CSVReader cr = null;
 		try {
 			System.out.println(file);
 			FileReader fr = new FileReader(file);
 			cr = new CSVReader(fr);
 		} catch (FileNotFoundException ex) { Logger.getLogger(AreasFile.class.getName()).log(Level.SEVERE, null, ex); }
 		return cr;
 	}
 
 	protected CSVWriter getCsvWriter() {
 		CSVWriter cw = null;
 		try {
 			cw = new CSVWriter(new FileWriter(file));
 		} catch (IOException ex) { Logger.getLogger(AreasFile.class.getName()).log(Level.SEVERE, null, ex); }
 		return cw;
 	}
 
 	public abstract FileNameExtensionFilter getFileFilter();
 
 }
