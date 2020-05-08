 package org.geworkbench.bison.datastructure.biocollections;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import javax.swing.JOptionPane;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.parsers.InputFileFormatException;
 
 /**
  * @author John Watkinson
  * @version $Id$
  */
 public class AdjacencyMatrixDataSet extends CSAncillaryDataSet<DSMicroarray> {
 
 	private static final long serialVersionUID = 2222442531807486171L;
 
 	static Log log = LogFactory.getLog(AdjacencyMatrixDataSet.class);
 
     private AdjacencyMatrix matrix;
 
     private final double threshold;
     private String networkName;
 
 	@SuppressWarnings("unchecked")
 	public AdjacencyMatrixDataSet(final AdjacencyMatrix matrix, final double threshold, final String name, final String networkName, final DSMicroarraySet<? extends DSMicroarray> parent) {
         super((DSDataSet<DSMicroarray>) parent, name);
         setID(RandomNumberGenerator.getID());
         this.matrix = matrix;
         this.threshold = threshold;
         this.networkName = networkName;
     }
 
	private static String getExportName(AdjacencyMatrix.Node node) {
        if (node.type == NodeType.MARKER) {
        	return node.marker.getLabel();
        } else if (node.type == NodeType.STRING) {
        	return node.stringId;
        } else {
        	return "unknown";
        }
	}
	
     public void writeToFile(String fileName) {
         File file = new File(fileName);
 
         try {
             file.createNewFile();
             if (!file.canWrite()) {
                 JOptionPane.showMessageDialog(null, "Cannot write to specified file.");
                 return;
             }
             BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 
             // if entry key is less than 0, for CNKB component, it means the gene is in currently selected microarray.
             for (AdjacencyMatrix.Node node1 : matrix.getNodes()) {
                writer.write(getExportName(node1) + "\t");
 
                 for (AdjacencyMatrix.Edge edge : matrix.getEdges(node1)) {
                    writer.write(getExportName(edge.node2) + "\t" + edge.info.value + "\t");
                 }
                 writer.write("\n");
             }
             writer.close();
         } catch (IOException e) {
             log.error(e);
         }
     }
 
     public void readFromFile(String fileName, DSMicroarraySet<DSMicroarray> maSet) {
     	try {
 			matrix = parseAdjacencyMatrix(fileName, maSet);
 		} catch (InputFileFormatException e) {
 			log.error(e);
 		}
     }
 
 	public static AdjacencyMatrix parseAdjacencyMatrix(String fileName,
 			DSMicroarraySet<DSMicroarray> maSet)
 			throws InputFileFormatException {
 		int connectionsInstantiated = 0;
 		int connectionsIgnored = 0;
 
         BufferedReader br = null;
         AdjacencyMatrix matrix = new AdjacencyMatrix(fileName, maSet);
 
         try {
 
             //            readMappings(new File(name));
             br = new BufferedReader(new FileReader(fileName));
             try {
                 //                String line = br.readLine();
                 String line;
                 int ctr = 0;
                 //                while (br.ready()) {
                 while ((line = br.readLine()) != null) {
                     if (ctr++ % 100 == 0) {
                         log.debug("Reading line " + ctr);
                     }
                     //skip comments
                     if (line.startsWith(">")) continue;
                     if (line.length() > 0 && line.charAt(0) != '-') {
                         StringTokenizer tr = new StringTokenizer(line, "\t:");
 
                         //String geneAccess = new String(tr.nextToken());
                         String strGeneId1 = new String(tr.nextToken());
                         DSGeneMarker m = maSet.getMarkers().get(strGeneId1);
                         if (m == null) { //we don't have this gene in our MicroarraySet
                         	//we skip it
                         	continue;
                         }
 						while (tr.hasMoreTokens()) {
 							String strGeneId2 = new String(tr.nextToken());
 							DSGeneMarker m2 = maSet.getMarkers()
 									.get(strGeneId2);
 							if (m2 == null) { // we don't have this gene in our
 												// MicroarraySet
 								// we skip it
 								continue;
 							}
 							String strMi = new String(tr.nextToken());
 							float mi = Float.parseFloat(strMi);
 							if (m != m2) {
 								connectionsInstantiated++;
 								matrix.add(new AdjacencyMatrix.Node(m),
 										new AdjacencyMatrix.Node(m2), mi, null);
 								// this.addInteractionType2(geneId1, geneId2,
 								// mi);
 							} else {
 								connectionsIgnored++;
 							}
 						}
 
                     }
                     //                    line = br.readLine();
                 }
                 log.debug("Connections instantiated " + connectionsInstantiated);
                 log.debug("Connections ignored " + connectionsIgnored);
                 log.debug("Total processed " + (connectionsInstantiated + connectionsIgnored));
             } catch (NumberFormatException ex) {
                 ex.printStackTrace();
             	throw new InputFileFormatException(ex.getMessage());
             } catch (IOException ex) {
                 ex.printStackTrace();
             	throw new InputFileFormatException(ex.getMessage());
             }
         } catch (FileNotFoundException ex3) {
             ex3.printStackTrace();
         	throw new InputFileFormatException(ex3.getMessage());
         }
 
         return matrix;
 	}
 
     public AdjacencyMatrix getMatrix() {
         return matrix;
     }
 
     public double getThreshold() {
         return threshold;
     }
 
     public File getDataSetFile() {
         // no-op
         return null;
     }
 
     public void setDataSetFile(File file) {
         // no-op
     }
 
     public String getNetworkName() {
         return networkName;
     }
 
     public void setNetworkName(String networkName) {
         this.networkName = networkName;
     }
 }
