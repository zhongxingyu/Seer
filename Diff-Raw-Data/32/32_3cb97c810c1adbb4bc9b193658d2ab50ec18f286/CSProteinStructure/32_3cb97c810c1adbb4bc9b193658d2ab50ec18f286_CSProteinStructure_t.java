 package org.geworkbench.bison.datastructure.bioobjects.structure;
 
 import java.io.BufferedReader;
import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 
 /**
  * 
  * @author mhall
  * @author zji
 * @version $Id: CSProteinStructure.java,v 1.6 2009-05-15 20:33:24 chiangy Exp $
  * 
  */
 @SuppressWarnings("unchecked")
 // raw type warning depends on other classes
 public class CSProteinStructure extends CSAncillaryDataSet implements
 		DSProteinStructure {
 
 	private static final long serialVersionUID = 7279040007729050440L;
 	private File dataFile = null;
 	private int chainoffset = 21;
 	private String content = null;
 
 	public CSProteinStructure(DSDataSet parent, String label) {
 		super(parent, label);
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public File getDataSetFile() {
 		return dataFile;
 	}
 
 	public void setDataSetFile(File file) {
 		dataFile = file;
         content = getContent(file);
 	}
 
 	public HashMap<String, Integer> getChains() {
 		// /get chains from pdb file
 		File prtfile = this.getFile();
 		File pdbfile = prtfile.getAbsoluteFile();
 		HashMap<String, Integer> chainhm = new HashMap<String, Integer>();
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(pdbfile));
 			String line = null;
 			while ((line = br.readLine()) != null) {
 				if (line.startsWith("ATOM  ") || line.startsWith("HETATM")) {
 					chainhm
 							.put(line.substring(chainoffset, chainoffset + 1),
 									1);
 				}
 			}
 			br.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				br.close();
 			} catch (IOException e) {
 			}
 		}
 		if (chainhm.get(" ") != null) {
 			chainhm.remove(" ");
 			chainhm.put("_", 1);
 		}
 		return chainhm;
 	}
 
 	/** Return the content of a file */
 	static private String getContent(File pdbfile) {
 		byte[] fileBytes = null;
 		try {
 			FileInputStream fileIn = new FileInputStream(pdbfile);
 			DataInputStream dataIn = new DataInputStream(fileIn);
 			fileBytes = new byte[dataIn.available()];
 			dataIn.readFully(fileBytes);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return new String(fileBytes);
 	}
 
 	/* this is implemented in CSDataSet. It is overridden here to make sure the content is caught all the time */
 	@Override
     public void setFile(File file) {
         this.file = file;
         content = getContent(file);
     }
 
	/**
	 * For now, it works by calling setFile() when create a protein object.
	 * The content will be load to the <code>content</code> variable.
	 * This writeToFile() will write the content to the given filename.
	 * 
	 * If in the future, we have some protein object which only exist in
	 * memory and doesn't have file associated with it, then this mechanism
	 * needs to be modified.
	 */
	@Override
	public void writeToFile(String fileName) {
		if (content == null)
			throw new RuntimeException("Must call setFile() before invoking this method.");
		else {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						fileName));
				writer.write(content);
				writer.flush();
				writer.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
 }
