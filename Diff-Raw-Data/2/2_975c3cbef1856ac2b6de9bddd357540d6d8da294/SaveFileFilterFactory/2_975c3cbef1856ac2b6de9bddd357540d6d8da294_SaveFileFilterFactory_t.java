 /**
  * 
  */
 package org.geworkbench.builtin.projects;
 
 import java.io.File;
 
 import javax.swing.filechooser.FileFilter;
 
 import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
 
 /**
  * This class is a factory to create FileFilter used by ProjectPanel's
  * savaAsFile method.
  * 
  * @author zji
  * @version $Id$
  * 
  */
 public class SaveFileFilterFactory {	
 	
 	static CustomFileFilter createFilter(
 			DSDataSet<? extends DSBioObject> dataset) {
 		if (dataset instanceof CSMicroarraySet) {
 			return new ExpFileFilter();
 		} else if (dataset instanceof CSProteinStructure) {
 			return new PDBFileFilter();
 		} else if (dataset instanceof CSSequenceSet) {
 			return new SequenceFileFilter();
 		} else if (dataset instanceof AdjacencyMatrixDataSet) {
 			return new AdjFileFilter();
 		} else {
 			return new DefaultFileFilter();
 		}
 	}
 	
 	static public TabDelimitedFileFilter getTabDelimitedFileFilter()
 	{
 		return new TabDelimitedFileFilter();
 	}
 
 	
 
 	static private enum ImageType {
 		Bitmap, JPEG, PNG, TIFF
 	};
 
 	/**
 	 * Simplified version for default format.
 	 * 
 	 * @return
 	 */
 	static ImageFileFilter createImageFileFilter() {
 		return createImageFileFilter(ImageType.Bitmap);
 	}
 	
 	static ImageFileFilter createImageFileFilter(ImageType imageType) {
 		switch (imageType) {
 		case Bitmap:
 			return new BitmapFileFilter();
 		case JPEG:
 			return new JPEGFileFilter();
 		case PNG:
 			return new PNGFileFilter();
 		case TIFF:
 			return new TIFFFileFilter();
 		default:
 			return new BitmapFileFilter();
 		}
 	}
 
 	/**
 	 * The class for saving files other than images.
 	 * 
 	 * @author zji
 	 * 
 	 */
 	static abstract class CustomFileFilter extends FileFilter {
 		abstract public String getExtension();
 	}
 
 	static private class ExpFileFilter extends CustomFileFilter {
 		public String getDescription() {
 			return "Exp Files";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean tabFile = name.endsWith("exp") || name.endsWith("EXP");
 			if (f.isDirectory() || tabFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "exp";
 		}
 
 	}
 
 	static private class PDBFileFilter extends CustomFileFilter {
 		public String getDescription() {
 			return "PDB File Format";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean tabFile = name.endsWith("pdb") || name.endsWith("PDB");
 			if (f.isDirectory() || tabFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "pdb";
 		}
 
 	}
 
 	static private class DefaultFileFilter extends CustomFileFilter {
 		public String getDescription() {
 			return "Text File";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean tabFile = name.endsWith("txt") || name.endsWith("TXT");
 			if (f.isDirectory() || tabFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "txt";
 		}
 
 	}
 	
 	
 
	static public class TabDelimitedFileFilter extends CustomFileFilter {
 		public String getDescription() {
 			return "tab-delimited file (*.txt)";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean tabFile = name.endsWith("txt") || name.endsWith("TXT");
 			if (f.isDirectory() || tabFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "txt";
 		}
 
 	}
 	
 
 	static private class SequenceFileFilter extends CustomFileFilter {
 		public String getDescription() {
 			return "Fasta File";
 		}
 
 		public boolean accept(File f) {
 			String name = f.getName();
 			boolean tabFile = name.endsWith("fa") || name.endsWith("FA")
 					|| name.endsWith("fasta") || name.endsWith("FASTA");
 			if (f.isDirectory() || tabFile) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getExtension() {
 			return "fa";
 		}
 
 	}
 
 	private static class AdjFileFilter extends CustomFileFilter {
 		private static final String fileExt = "adj";
 
 		public String getExtension() {
 			return fileExt;
 		}
 
 		public String getDescription() {
 			return "Adjacency Matrix Files (.adj)";
 		}
 
 		public boolean accept(File f) {
 			if (f.isDirectory() || f.getName().endsWith(fileExt)) {
 				return true;
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * ImageFileFilter is for saving image file.
 	 * 
 	 * @author zji
 	 * 
 	 */
 	static abstract class ImageFileFilter extends FileFilter {
 		public abstract String getExtension();
 	}
 
 	static private class BitmapFileFilter extends ImageFileFilter {
 		public String getDescription() {
 			return "Bitmap Files";
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
 
 	static private class JPEGFileFilter extends ImageFileFilter {
 		public String getDescription() {
 			return "Joint Photographic Experts Group Files";
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
 
 	static private class PNGFileFilter extends ImageFileFilter {
 		public String getDescription() {
 			return "Portable Network Graphics Files";
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
 
 	static private class TIFFFileFilter extends ImageFileFilter {
 		public String getDescription() {
 			return "Tag(ged) Image File Format";
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
