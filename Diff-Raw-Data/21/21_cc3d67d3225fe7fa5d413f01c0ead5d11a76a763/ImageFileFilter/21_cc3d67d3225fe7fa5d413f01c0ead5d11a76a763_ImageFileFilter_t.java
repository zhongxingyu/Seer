 /**
  * 
  */
 package org.geworkbench.util;
 
 import java.io.File;
 
 import javax.swing.filechooser.FileFilter;
 
 abstract class ImageFileFilter extends FileFilter {
 	public abstract String getExtension();
 	
 	static class BitmapFileFilter extends ImageFileFilter {
 		public String getDescription() {
			return "Bitmap Files (*.bmp)";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean imageFile = name.endsWith("bmp") || name.endsWith("BMP");
 			if (f.isDirectory() || imageFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "bmp";
 		}
 
 	}
 
 	static class JPEGFileFilter extends ImageFileFilter {
 		public String getDescription() {
			return "Joint Photographic Experts Group Files (*.jpg)";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean imageFile = name.endsWith("jpg") || name.endsWith("JPG");
 			if (f.isDirectory() || imageFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "jpg";
 		}
 
 	}
 
 	static class PNGFileFilter extends ImageFileFilter {
 		public String getDescription() {
			return "Portable Network Graphics Files (*.png)";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean imageFile = name.endsWith("png") || name.endsWith("PNG");
 			if (f.isDirectory() || imageFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "png";
 		}
 
 	}
 
 	static class TIFFFileFilter extends ImageFileFilter {
 		public String getDescription() {
			return "Tag(ged) Image File Format (*.tif)";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean imageFile = name.endsWith("tif") || name.endsWith("TIF")
 					|| name.endsWith("tiff") || name.endsWith("TIFF");
 			if (f.isDirectory() || imageFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "tif";
 		}
 
 	}
 
 }
