 package org.geworkbench.parsers;
 
 import java.io.File;
 import java.io.InterruptedIOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.parsers.resources.Resource;
 import org.geworkbench.builtin.projects.DataSetNode;
 import org.geworkbench.builtin.projects.ProjectNode;
 import org.geworkbench.builtin.projects.ProjectTreeNode;
 import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
 import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
 
 /**
  * Handles parsing of ARACNe adjacency matrix .txt files.
  * based on AffyFileFormat
  *
  * @author os2201
  * @version $Id: $
  *
  */
 public class AdjacencyMatrixFileFormat extends DataSetFileFormat {
     private Log log = LogFactory.getLog(this.getClass());
 
     String[] adjMatrixExtensions = {"txt"};
     AdjacencyMatrixFileFilter adjMatrixFilter = null;
 
 	private ProjectNode projectNode;
 
     public void setProjectNode(ProjectNode projectNode) {
 		this.projectNode = projectNode;
 	}
 
 	public AdjacencyMatrixFileFormat() {
         formatName = "Adjacency Matrix";   // Setup the display name for the format.
         adjMatrixFilter = new AdjacencyMatrixFileFilter();
     }
 
 	@Override
 	public DSDataSet<? extends DSBioObject> getDataFile(final File file)
 			throws InputFileFormatException, InterruptedIOException {
 
 		DSDataSet<?> ds = getMArraySet(file);
 		return ds;
 	}
 
 	@SuppressWarnings("unused")
 	@Override
 	public DSDataSet<? extends DSBioObject> getDataFile(File[] files)
 			throws InputFileFormatException {
 		return null;
 	}
 
 	@SuppressWarnings({ "unchecked", "unused" })
 	@Override
 	public DSDataSet<? extends DSBioObject> getMArraySet(File file)
 			throws InputFileFormatException, InterruptedIOException {
 
 		final Object[] s = {null};
 
 		//  invoke data set selection on the EDT
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					s[0] = selectDataSet();
 				}
 			});
 		} catch (InterruptedException e) {
 			log.info(e);
 		} catch (InvocationTargetException e) {
 			log.error(e);
 		}
 
 		AdjacencyMatrixDataSet adjMatrixDS = null;
 		if ((s[0] != null)) {
 			DataSetNode selected = (DataSetNode) s[0];
 			DSDataSet<DSMicroarray> ds = selected.dataFile;
 			if (!(ds instanceof CSMicroarraySet)) {
 				JOptionPane.showMessageDialog(null,
 						"Not a Microarray Set selected", "Unable to Load",
 						JOptionPane.ERROR_MESSAGE);
 			} else {
 				CSMicroarraySet<DSMicroarray> mASet = (CSMicroarraySet<DSMicroarray>) ds;
 				String adjMatrixFileStr = file.getPath();
 				String fileName = file.getName();
 
 				AdjacencyMatrix matrix = AdjacencyMatrixDataSet
 						.parseAdjacencyMatrix(adjMatrixFileStr, mASet);
 
 				adjMatrixDS = new AdjacencyMatrixDataSet(matrix, 0, 0, 0,
 						fileName, "network loaded", mASet);
 
 			}
 		} else {
 			JOptionPane.showMessageDialog(null, "No Microarray Set selected",
 					"Unable to Load", JOptionPane.ERROR_MESSAGE);
 		}
 
 		return adjMatrixDS;
 	}
 
 	private Object selectDataSet() {
 		// get list of data sets that a selected adjacency matrix could be attached to
 		ArrayList<DataSetNode> dataSetstmp = new ArrayList<DataSetNode>();
 		for (Enumeration<?> en = projectNode.children(); en.hasMoreElements();) {
 			ProjectTreeNode node = (ProjectTreeNode) en.nextElement();
 			if (node instanceof DataSetNode) {
 				dataSetstmp.add((DataSetNode) node);
 			}
 		}

		Object ret;
		if (dataSetstmp.isEmpty()){
			JOptionPane.showMessageDialog(null, "No Microarray Set is available");
			ret = null;
		} else {
			DataSetNode[] dataSets = dataSetstmp.toArray(new DataSetNode[1]);
			ret = JOptionPane.showInputDialog(
                    null,
                    "Microarray Dataset:",
                    "Select Dataset",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    dataSets,
                    dataSets[0]);
		}

		return ret;
 	}
 
 	@Override
 	public boolean checkFormat(File file) throws InterruptedIOException {
 		return true;
 	}
 
 	@Override
 	public Resource getResource(File file) {
         return null;
 	}
 
 	@Override
 	public String[] getFileExtensions() {
 		return adjMatrixExtensions;
 	}
 
     public FileFilter getFileFilter() {
         return adjMatrixFilter;
     }
 
     class AdjacencyMatrixFileFilter extends FileFilter {
         public String getDescription() {
             return getFormatName();
         }
 
         public boolean accept(File f) {
             boolean returnVal = false;
             for (int i = 0; i < adjMatrixExtensions.length; ++i)
                 if (f.isDirectory() || f.getName().toLowerCase().endsWith(adjMatrixExtensions[i])) {
                     return true;
                 }
             return returnVal;
         }
     }
 }
