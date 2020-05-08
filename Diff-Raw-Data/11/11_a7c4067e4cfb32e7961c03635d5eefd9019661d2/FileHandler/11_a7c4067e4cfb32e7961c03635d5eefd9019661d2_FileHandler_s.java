 package lichen.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser; 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import ij.ImagePlus;
 import ij.io.FileSaver;
 import ij.io.Opener;
 
 public class FileHandler {
 
 	private String lastdir = "";
 	private String lastImage;
 	private JFileChooser jfc;
 	private Image img;
 
 
 
 	public FileHandler() { 
 	}
 
 
 	/**
 	 * Shows FileChooser Dialog to choose a picture
 	 * @return - the picture
 	 */
 	public ImagePlus openImagePlus(){
 
 		final JFileChooser jc;
 
 		if(lastdir.equals("")){ 
 			jc = new JFileChooser("");
 		}else{ 
 			jc = new JFileChooser(lastdir);
 		}
 
 		LichenFileChooser ljc = new LichenFileChooser(jc);
 		jc.addPropertyChangeListener(ljc);
 		jc.setAccessory(ljc);
 
 		int returnVal = jc.showDialog(null, "Wähle Bild");
 
 		if(returnVal == JFileChooser.APPROVE_OPTION){
 
 
 			Opener opener = new Opener();
 			ImagePlus imp = opener.openImage(jc.getSelectedFile().toString());
 			File file = new File(jc.getSelectedFile().toString());
 		
 			lastdir = jc.getSelectedFile().toString();
 			lastImage = jc.getSelectedFile().toString();
 
 			if(imp == null){
 				throw new NullPointerException("cannot load image");
 			}
 
 			//rotate Image if high side is up
 			if(imp.getWidth() < imp.getHeight()){
 				imp.setProcessor( imp.getProcessor().rotateRight());
 			} 
 	
 			  long heapSize = Runtime.getRuntime().maxMemory()/ (1024*1024); 
 
 			/* Warnung wenn Bild zu groß */
			if(file.length()/(1024*1024)*100 > heapSize){ /* Image size bigger than maxMemory*100  */
 				JOptionPane.showMessageDialog(MainGUI.getInstance(), "Das Bild ist möglicherweise zu groß für den verfügbaren Arbeitsspeicher, wenn " +
 						"das Programm wenig Arbeitsspeicher hat, kann das zu " +
 						"Problemen führen.\nDer maximale Arbeitsspeicher muss erhöht, oder die Größe des Bildes verkleinert werden.\n" +
 						"Bitte lesen Sie hierzu das Benutzerhandbuch", "Bildgröße", JOptionPane.WARNING_MESSAGE); 
 			} 
 
 			return imp; 
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Save an image to file
 	 * @param imp imagePlus to be saved
 	 * @param path - path/filename of the image
 	 */
 	public boolean saveImage(ImagePlus imp, String path){
 		FileSaver filesaver = new FileSaver(imp);
 		return filesaver.save(path); 
 	}
 
 	/**
 	 * reloads the last image
 	 * @return 
 	 */
 	public ImagePlus reloadImage() throws NullPointerException{
 		Opener opener = new Opener();
 		ImagePlus imp = opener.openImage(lastImage);
 
 		if(imp == null){
 			throw new NullPointerException("cannot load image");
 		}
 
 		if(imp.getWidth() < imp.getHeight()){
 			imp.setProcessor( imp.getProcessor().rotateRight());
 		}
 
 		return imp;	
 	} 
 	public String getLastDir(){
 		return this.lastdir;
 
 	}
 
 	public void setLastDir(String inipath) {
 		this.lastdir = inipath;
 
 	}
 
 
 
 }
