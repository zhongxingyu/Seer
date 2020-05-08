 import ij.*;
 import ij.process.*;
 import ij.gui.*;
 import java.awt.*;
 import ij.plugin.*;
 import ij.plugin.frame.*;
 import ij.io.OpenDialog;
 import ij.io.FileInfo;
 import ij.io.FileOpener;
 import java.io.*;
 import java.util.*;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 
 public class Import_Qcamraw implements PlugIn {
 	static File dir;
 	
 	public void run(String arg) {
 		
 		JFileChooser jc = new JFileChooser();
 		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF Images", "qcamraw", "gif");
 		jc.setFileFilter(filter);
 		jc.setMultiSelectionEnabled(true);
 		if (dir==null) {
 			String sdir = OpenDialog.getDefaultDirectory();
 			if (sdir!=null)
 			dir = new File(sdir);
 		}
 		if (dir!=null) {
 			jc.setCurrentDirectory(dir);
 		}
 		int returnVal = jc.showOpenDialog(IJ.getInstance());
 		File[] files = jc.getSelectedFiles();
 		
 		if (files.length==0) { // getSelectedFiles does not work on some JVMs
 			files = new File[1];
 			files[0] = jc.getSelectedFile();
 		}
 		
 		String directory = jc.getCurrentDirectory().getPath() + Prefs.getFileSeparator();
 		ImageCalculator ic = new ImageCalculator();
 		
 		try{
 			ImagePlus averagedIm = new ImagePlus();
 			for (int i = 0; i < files.length; i++) {
 				
 				File f = files[i];
 				HashMap<String, String> headerHash = getHeader(directory, f.getName());
 				FileInfo fi = loadImages(directory, f.getName(), headerHash);
 				ImagePlus ip = new FileOpener(fi).open(false);
 				StackConverter icon = new StackConverter(ip);
 				icon.convertToGray32();
 
 				for (int j = 1; j < 1 + ip.getImageStack().getSize(); j++) { // stack numbering starts with 0!
 					ip.getImageStack().getProcessor(j).multiply(1.0 / (double) files.length);
 				}
 				
 				if (i == 0) {
 					averagedIm = ip;
 				} else {
 					averagedIm = ic.run("Add create 32-bit stack", averagedIm, ip);			
 				}
 			}
 			
 			averagedIm.show();
 			
 		} catch(IOException x){
 			new ij.gui.MessageDialog(IJ.getInstance(), "errors", "error");
 		}
 		
 	}
 
 	private HashMap<String, String> getHeader(String directory, String fileName) throws IOException {
 		HashMap<String, String> headerHash = new HashMap<String, String>();
 		FileReader fr = new FileReader(directory + fileName);
 		BufferedReader br = new BufferedReader(fr);
 		
 		for (int k = 0; k < 8; k++){ // the first 8 lines are critical, according to their documentation.
 			String headerLine = br.readLine();
 			String[] keyValue = headerLine.split(": ");
 			headerHash.put(keyValue[0], keyValue[1]);
 		}
 
 		// it would be better if other key-value pairs are stored.  
 		br.close();
 			
 		return headerHash;
 	}
 
 	private FileInfo loadImages(String directory, String fileName, HashMap<String, String> headerHash) throws IOException {
 		
 		String headerSizeValue = (String) headerHash.get("Fixed-Header-Size");
 		Integer headerSize = Integer.parseInt(headerSizeValue.replaceAll(" \\[bytes\\]",""));		
 		File f = new File(directory + fileName);
 		long fileLength = f.length();			
 		int frameSize = Integer.parseInt(headerHash.get("Frame-Size").replaceAll(" \\[bytes\\]",""));
 		String[] roi = headerHash.get("ROI").split(", ");		
 		
 		FileInfo fi = new FileInfo();
 		fi.width = Integer.parseInt(roi[2]) - Integer.parseInt(roi[0]);
 		fi.height = Integer.parseInt(roi[3]) - Integer.parseInt(roi[1]);
 		fi.offset = Integer.parseInt(headerSizeValue.replaceAll(" \\[bytes\\]",""));
 		fi.nImages = (int) (fileLength - headerSize) / frameSize;
 		if (headerHash.get("Image-Encoding").matches("raw16")){
 			fi.fileType = FileInfo.GRAY16_UNSIGNED; // what about other conditions?
 		}
 		fi.intelByteOrder = true;
 		fi.fileName = fileName;
 		fi.directory = directory;
 		return fi;
 	}
 
 }
