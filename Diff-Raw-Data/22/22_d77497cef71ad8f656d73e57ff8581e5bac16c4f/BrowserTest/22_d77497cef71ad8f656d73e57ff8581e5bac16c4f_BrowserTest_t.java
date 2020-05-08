 package edu.uchc.octane.test;
 
 import java.io.File;
 import java.io.IOException;
 
 import ij.ImagePlus;
 import edu.uchc.octane.Browser;
 import edu.uchc.octane.ThresholdDialog;
 
 public class BrowserTest {
 	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ImagePlus imp = ij.IJ.openImage(args[0]);
 		imp.show();
 		//TrajDataset data = new TrajDataset(p);
		File file = new File(args[0]+".dataset");
 		if (file.exists()) {
 			Browser b = new Browser(imp);
 			b.setup();
 		} else {
 			ThresholdDialog finderDlg = new ThresholdDialog(imp);
 			if (finderDlg.openDialog() == true) {
 				Browser b= new Browser(imp);
 				b.setup(finderDlg.getProcessedNodes());
 			} else {
 				imp.close();
 			}
 		}
 	}
 }
