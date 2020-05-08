 package cz.cuni.mff.odcleanstore.data;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import virtuoso.jena.driver.VirtGraph;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.shared.UniqueGraphNameGenerator;
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * Utility used to copy graphs into temporary graphs in dirty database
  * @author Jakub Daniel
  */
 public class DebugGraphFileLoader {
 	private static final Logger LOG = LoggerFactory.getLogger(DebugGraphFileLoader.class);
 
 	private String temporaryGraphURIPrefix;
 	private JDBCConnectionCredentials connectionCredentials;
 	
 	public DebugGraphFileLoader(URI temporaryGraphURIPrefix, JDBCConnectionCredentials connectionCredentials) {
 		this.temporaryGraphURIPrefix = temporaryGraphURIPrefix.toString();
 		this.connectionCredentials = connectionCredentials;
 	}
 	
 	private static String getInputBaseURI (String temporaryGraphURIPrefix, String discriminator) {
 		return temporaryGraphURIPrefix + "/" + discriminator + "/input/";
 	}
 	
 	public HashMap<String, String> load (InputStream input, String discriminator) throws Exception {
 		try {
 			return loadImpl(new MultipleFormatLoader().load(input, getInputBaseURI(this.temporaryGraphURIPrefix, discriminator)), discriminator);
 		} catch (Exception e) {
 			LOG.error(String.format("Could not finish loading debug graphs from input: %s", e.getMessage()));
 				
 			throw e;
 		}
 	}
 	
 	private HashMap<String, String> loadImpl (NamedGraphSetImpl namedGraphSet, String discriminator) throws Exception {	
 		/**
 		 * Copy them into unique graphs
 		 */
		UniqueGraphNameGenerator graphNameGen = new UniqueGraphNameGenerator("http://example.com/" + discriminator + "/debug/", connectionCredentials);
 		
 		HashMap<String, String> graphs = new HashMap<String, String>();
 		
 		try {
 			Iterator<NamedGraph> it = namedGraphSet.listGraphs();
 
 			while (it.hasNext()) {
 				NamedGraph graph = it.next();
 			
 				String name = graph.getGraphName().toString();
 				String temporaryName;
 				
 				if (graphs.containsKey(name)) {
 					temporaryName = graphs.get(name);
 				} else {
 					temporaryName = graphNameGen.nextURI();
 				}
 			
 				graphs.put(name, temporaryName);
 			
 				VirtGraph temporaryGraph = new VirtGraph(temporaryName,
 						connectionCredentials.getConnectionString(),
 						connectionCredentials.getUsername(),
 						connectionCredentials.getPassword());
 			
 				ExtendedIterator<Triple> triples = graph.find(Node.ANY, Node.ANY, Node.ANY);
 
 				/**
 				 * Copying contents into unique temporary destination graphs in dirty database
 				 */
 				while (triples.hasNext()) {
 					Triple triple = triples.next();
 				
 					temporaryGraph.add(triple);
 				}
 			
 				LOG.info(String.format("Input debug graph <%s> copied into <%s>", name, temporaryName));
 			}
 		} catch (Exception e) {
 			
 			unload(graphs);
 			
 			throw e;
 		}
 		
 		return graphs;
 	}
 	
 	public void unload (HashMap<String, String> graphs) {
 		Set<String> keys = graphs.keySet();
 
 		Iterator<String> it = keys.iterator();
 
 		/**
 		 * Drop all graphs
 		 */
 		while (it.hasNext()) {
 			String key = it.next();
 
 			try {
 				VirtGraph temporaryGraph = new VirtGraph(graphs.get(key),
 						connectionCredentials.getConnectionString(),
 						connectionCredentials.getUsername(),
 						connectionCredentials.getPassword());
 				
 				temporaryGraph.clear();
 				
 				LOG.info(String.format("Temporary copy <%s> of input debug graph <%s> cleared", graphs.get(key), key));
 			} catch (Exception e) {
 			}
 		}
 	}
 }
