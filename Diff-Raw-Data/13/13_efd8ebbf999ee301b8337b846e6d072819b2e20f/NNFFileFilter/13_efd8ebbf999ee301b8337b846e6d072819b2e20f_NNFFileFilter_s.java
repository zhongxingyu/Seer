 package cytoscape.util;
 
 import java.net.URL;
 import java.net.URLConnection;
 
 import cytoscape.data.ImportHandler;
 import cytoscape.data.readers.GraphReader;
import cytoscape.data.readers.InteractionsReader;
 import cytoscape.logger.CyLogger;
 
 /**
  * File filter for Nested Network Format (NNF).
  * 
  * @author kono, ruschein
  * @since Cytoscape 2.7.0
  */
 public class NNFFileFilter extends CyFileFilter {
 	/**
 	 * NNF Files are Graphs.
 	 */
 	private static String fileNature = ImportHandler.GRAPH_NATURE;
 
 	
 	/**
 	 * File Extension.
 	 */
 	private static String fileExtension = "nnf";
 
 	
 	/**
 	 * Content Types
 	 */
 	private static String[] contentTypes = { "text/nnf" };
 
 	
 	/**
 	 * Filter Description.
 	 */
 	private static String description = "NNF files";
 
 	
 	/**
 	 * Constructor.
 	 */
 	public NNFFileFilter() {
 		super(fileExtension, description, fileNature);
 	}
 
 	
 	/**
 	 * Gets Graph Reader.
 	 * 
 	 * @param fileName
 	 *            File name.
 	 * @return GraphReader Object.
 	 */
 	public GraphReader getReader(String fileName) {
		reader = new InteractionsReader(fileName);
 
 		return reader;
 	}
 
 	/**
 	 * Gets Graph Reader.
 	 * 
 	 * @param fileName
 	 *            File name.
 	 * @return GraphReader Object.
 	 */
 	public GraphReader getReader(URL url, URLConnection conn) {
 		try {
 			// Get the input stream
			reader = new InteractionsReader(conn.getInputStream(), url
					.toString());
 		} catch (Exception e) {
			CyLogger.getLogger(NNFFileFilter.class).error(
					"Unable to get NNF reader: " + e.getMessage());
 			reader = null;
 		}
 		return reader;
 	}
 }
