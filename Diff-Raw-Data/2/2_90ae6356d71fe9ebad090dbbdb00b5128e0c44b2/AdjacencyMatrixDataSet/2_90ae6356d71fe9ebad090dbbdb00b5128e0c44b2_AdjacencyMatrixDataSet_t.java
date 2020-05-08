 package org.geworkbench.bison.datastructure.biocollections;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
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
 
 	public static final String SIF_FORMART = "sif format";
 	public static final String ADJ_FORMART = "adj format";
 	public static final String GENE_NAME = "gene name";
 	public static final String ENTREZ_ID = "entrez id";
 	public static final String OTHER = "other";
 	public static final String PROBESET_ID = "probeset id";
 
 	static Log log = LogFactory.getLog(AdjacencyMatrixDataSet.class);
 
 	private AdjacencyMatrix matrix;
 
 	private final double threshold;
 	private String networkName;
 
 	public AdjacencyMatrixDataSet(final AdjacencyMatrix matrix,
 			final double threshold, final String name,
 			final String networkName,
 			final DSMicroarraySet parent) {
 		super((DSDataSet<DSMicroarray>) parent, name);
 		setID(RandomNumberGenerator.getID());
 		this.matrix = matrix;
 		this.threshold = threshold;
 		this.networkName = networkName;
 	}
 
 	public String getExportName(AdjacencyMatrix.Node node) {
 		if (node.type == NodeType.MARKER) {
 			return node.marker.getLabel();
 		} else if (node.type == NodeType.GENE_SYMBOL) {
 			return node.stringId;
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
 				JOptionPane.showMessageDialog(null,
 						"Cannot write to specified file.");
 				return;
 			}
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 
 			// if entry key is less than 0, for CNKB component, it means the
 			// gene is in currently selected microarray.
 			for (AdjacencyMatrix.Node node1 : matrix.getNodes()) {
 				writer.write(getExportName(node1) + "\t");
 
 				for (AdjacencyMatrix.Edge edge : matrix.getEdges(node1)) {
 					writer.write(getExportName(edge.node2) + "\t"
 							+ edge.info.value + "\t");
 				}
 				writer.write("\n");
 			}
 			writer.close();
 		} catch (IOException e) {
 			log.error(e);
 		}
 	}
 
 	/**
 	 * Constructor that takes a filename to create by reading and parsing the
 	 * file.
 	 * 
 	 * @param matrix
 	 * @param threshold
 	 * @param name
 	 * @param networkName
 	 * @param parent
 	 * @param fileName
 	 */
 	public AdjacencyMatrixDataSet(final double threshold, final String name,
 			final String networkName,
 			final DSMicroarraySet parent, String fileName)
 			throws InputFileFormatException {
 
 		super((DSDataSet<DSMicroarray>) parent, name);
 		setID(RandomNumberGenerator.getID());
 
 		this.threshold = threshold;
 		this.networkName = networkName;
 
 		matrix = parseAdjacencyMatrix(fileName, parent, null, ADJ_FORMART,
 				PROBESET_ID, true);
 	}
 
 	private static AdjacencyMatrix.Node token2node(String token,
 			final String selectedRepresentedBy, final boolean isRestrict, final DSMicroarraySet maSet) {
 		DSGeneMarker m = null;
 		if (selectedRepresentedBy.equals(PROBESET_ID)
 				|| selectedRepresentedBy.equals(GENE_NAME)
 				|| selectedRepresentedBy.equals(ENTREZ_ID))
 			m = maSet.getMarkers().get(token);
 
 		AdjacencyMatrix.Node node = null;
 
 		if (m == null && isRestrict) {
 			// we don't have this gene in our MicroarraySet
 			// we skip it
 			return null;
 		} else if (m == null && !isRestrict) {
 			if (selectedRepresentedBy.equals(GENE_NAME))
 				node = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
 						token);
 			else
 				node = new AdjacencyMatrix.Node(NodeType.STRING, token);
 		} else {
 			if (selectedRepresentedBy.equals(PROBESET_ID))
 				node = new AdjacencyMatrix.Node(m);
 			else
 				node = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
 						m.getGeneName());
 		}
 		return node;
 	}
 
 	public static AdjacencyMatrix parseAdjacencyMatrix(String fileName,
 			final DSMicroarraySet maSet,
 			Map<String, String> interactionTypeSifMap, String format,
 			String selectedRepresentedBy, boolean isRestrict)
 			throws InputFileFormatException {
 
 		AdjacencyMatrix matrix = new AdjacencyMatrix(fileName, maSet,
 				interactionTypeSifMap);
 
 		try {
 
 			BufferedReader br = new BufferedReader(new FileReader(fileName));
 
 			String line = null;
 
 			while ((line = br.readLine()) != null) {
 				// skip comments
 				if (line.trim().equals("") || line.startsWith(">")
 						|| line.startsWith("-"))
 					continue;
 
 				StringTokenizer tr = new StringTokenizer(line, "\t: :");
 
 				AdjacencyMatrix.Node node = token2node(tr.nextToken(), selectedRepresentedBy, isRestrict, maSet);
 				if(node==null) continue; // skip it when we don't have it
 
 				String interactionType = null;
 				if (format.equals(SIF_FORMART) && tr.hasMoreTokens())
 					interactionType = tr.nextToken().toLowerCase();
 
 				while (tr.hasMoreTokens()) {
 
 					String strGeneId2 = tr.nextToken();
 					AdjacencyMatrix.Node node2 = token2node(strGeneId2, selectedRepresentedBy, isRestrict, maSet);
					if(node2==null) continue; // skip it when we don't have it
 
 					float mi = 0.8f;
 					if (format.equals(ADJ_FORMART)) {
 						if (!tr.hasMoreTokens())
 							throw new InputFileFormatException(
 									"invalid format around " + strGeneId2);
 						mi = Float.parseFloat(tr.nextToken());
 					}
 
 					matrix.add(node, node2, mi, interactionType);
 				} // end of the token loop for one line
 			} // end of reading while loop
 		} catch (NumberFormatException ex) {
 			throw new InputFileFormatException(ex.getMessage());
 		} catch (FileNotFoundException ex3) {
 			throw new InputFileFormatException(ex3.getMessage());
 		} catch (IOException ex) {
 			throw new InputFileFormatException(ex.getMessage());
 		} catch (Exception e) {
 			throw new InputFileFormatException(e.getMessage());
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
