 package org.geworkbench.util;
 
 import java.awt.Image;
 import java.awt.image.renderable.ParameterBlock;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import javax.media.jai.InterpolationNearest;
 import javax.media.jai.JAI;
 import javax.media.jai.RenderedOp;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  *
  * @author First Genetic Trust Inc.
  * @version $Id$
  */
 public class SaveImage {
 	private static Log log = LogFactory.getLog(SaveImage.class);
 	
 	private Image imageToBeSaved = null;
 
     public SaveImage() {
     }
 
     public SaveImage(Image image) {
         imageToBeSaved = image;
     }
 
     public void setImage(Image image) {
         imageToBeSaved = image;
     }
 
     // this method is to be invoked from GUI (EDT)
     public void save() {
 		JFileChooser fc = new JFileChooser(".");
 
 		FileFilter bitmapFilter = new ImageFileFilter.BitmapFileFilter();
 		FileFilter jpegFilter = new ImageFileFilter.JPEGFileFilter();
 		FileFilter pngFilter = new ImageFileFilter.PNGFileFilter();
 		FileFilter tiffFilter = new ImageFileFilter.TIFFFileFilter();
		fc.setFileFilter(tiffFilter);
		fc.setFileFilter(pngFilter);
 		fc.setFileFilter(jpegFilter);
 		fc.setFileFilter(bitmapFilter);
 
 		int choice = fc.showSaveDialog(null);
 		if (choice == JFileChooser.APPROVE_OPTION) {
 			String imageFilename = fc.getSelectedFile()
 					.getAbsolutePath();
 			String filename = fc.getSelectedFile().getName();
 			String ext = null;
 			int i = filename.lastIndexOf('.');
 			if (i > 0 && i < filename.length() - 1) {
 				ext = filename.substring(i + 1).toLowerCase();
 			} else {
 				FileFilter filter = fc.getFileFilter();
 				if (filter instanceof ImageFileFilter) {
 					ImageFileFilter selectedFilter = (ImageFileFilter) filter;
 					ext = selectedFilter.getExtension();
 					log.info("File extension: " + ext);
 				}
 			}
 			if (ext == null)
 				ext = "jpg";
 			if (imageFilename != null) {
 				save(imageFilename, ext);
 			}	
 		}
     }
     
     private void save(String filename, String ext) {
         ParameterBlock pb = new ParameterBlock();
         pb.add(imageToBeSaved);
         RenderedOp op0 = JAI.create("awtImage", pb);
         pb.removeParameters();
         pb.removeSources();
         pb.addSource(op0);
         pb.add(1.0F);
         pb.add(1.0F);
         pb.add(0.0F);
         pb.add(0.0F);
         pb.add(new InterpolationNearest());
         if (!filename.endsWith(ext))
             filename += "." + ext;
         RenderedOp op1 = JAI.create("scale", pb, null);
 
         FileOutputStream stream = null;
         try {
             stream = new FileOutputStream(new File(filename));
         } catch (IOException ioe) {
         }
 
         if (ext.equals("bmp")) {
             JAI.create("encode", op1, stream, "BMP", null);
         } else if (ext.equals("png")) {
             JAI.create("encode", op1, stream, "PNG", null);
         } else if (ext.equals("jpg") || ext.equals("JPEG")) {
             JAI.create("encode", op1, stream, "JPEG", null);
         } else if (ext.equals("tif") || ext.equals("TIFF")) {
             JAI.create("encode", op1, stream, "TIFF", null);
         }
 
         if (stream != null) {
             try {
                 stream.flush();
                 stream.close();
             } catch (IOException ioe) {
             }
 
         }
 
     }
 
 }
 
