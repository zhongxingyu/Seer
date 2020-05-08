 /**
  * 
  */
 package org.ariadne_eu.metadata.resultsformat;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import net.sourceforge.minor.lucene.core.searcher.IndexSearchDelegate;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.search.Hits;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.response.FacetField;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.params.ModifiableSolrParams;
 import org.apache.solr.core.SolrCore;
 import org.apache.solr.servlet.DirectSolrConnection;
 import org.ariadne.config.PropertiesManager;
 import org.ariadne_eu.metadata.query.QueryMetadataLuceneImpl;
 import org.ariadne_eu.utils.config.RepositoryConstants;
 
 /**
  * @author gonzalo
  *
  */
 public class ResultDelegateSolrImpl implements IndexSearchDelegate{
 	
 	private static Logger log = Logger.getLogger(QueryMetadataLuceneImpl.class);
 	private int start;
     private int max;
     private String lQuery;
     private static String instanceDir;
     private static String dataDir;
     private static String loggingPath;
     private DirectSolrConnection conn;
     private static Vector facetFields;
     
     static {
         try {
//        	instanceDir = PropertiesManager.getInstance().getProperty(RepositoryConstants.SR_SOLR_INSTANCEDIR);
        	instanceDir = (PropertiesManager.getInstance().getPropFile()).replaceAll("install/ariadne.properties", "solr/");
         	dataDir = PropertiesManager.getInstance().getProperty(RepositoryConstants.SR_SOLR_DATADIR);
         	loggingPath = PropertiesManager.getInstance().getProperty(RepositoryConstants.REPO_LOG4J_DIR);
         	
         	facetFields = new Vector();
         	int i = 1;
             while(PropertiesManager.getInstance().getProperty(RepositoryConstants.SR_SOLR_FACETFIELD + "." + i) != null) {
             	facetFields.add(PropertiesManager.getInstance().getProperty(RepositoryConstants.SR_SOLR_FACETFIELD + "." + i));
                 i++;
             }
         	
             if(instanceDir == null) {
 //            	instanceDir = "db2-fn:xmlcolumn(\"METADATASTORE.LOMXML\")";
//                log.error("initialize:property \""+ RepositoryConstants.SR_SOLR_INSTANCEDIR +"\" not defined");
            	log.error("Could not load Solr instance dir!");
             } else if (dataDir == null) {
 //            	dataDir = "db2-fn:xmlcolumn(\"METADATASTORE.LOMXML\")";
                 log.warn("initialize:property \""+ RepositoryConstants.SR_SOLR_DATADIR + "\" not defined");
             } else if (loggingPath == null) {
 //            	loggingPath = "db2-fn:xmlcolumn(\"METADATASTORE.LOMXML\")";
                 log.warn("initialize:property \""+ RepositoryConstants.REPO_LOG4J_DIR +"\" not defined");
             } else if (! (facetFields.size() > 0)) {
             	log.error("initialize:property \"" + RepositoryConstants.SR_SOLR_FACETFIELD + ".n\" not defined");
             }
             
         } catch (Throwable t) {
             log.error("initialize: ", t);
         }
     }
 	
 	public ResultDelegateSolrImpl(int start, int max, String lQuery) {
         this.start = start;
         this.max = max;
         this.lQuery = lQuery;
        
         conn = new DirectSolrConnection(instanceDir, dataDir, loggingPath);
     }
 	
 	public String result(Hits hits) throws Exception {
		log.debug(PropertiesManager.getInstance().getPropFile());
 	    StringBuilder sBuild = new StringBuilder();
 	    sBuild.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
 	    
 	    SolrCore core = SolrCore.getSolrCore();
 		SolrServer server = new EmbeddedSolrServer(core);
 		
 		SolrQuery solrQuery = new  SolrQuery().setQuery(lQuery).setFacet(true).setFacetLimit(-1).setFacetMinCount(0).setFacetSort(true);
 
 		ModifiableSolrParams params = new ModifiableSolrParams();
 		params.set("queryResultWindowSize", Integer.toString(max));
 		solrQuery.add(params);
 		
 //		System.out.println(solrQuery.get("query/queryResultWindowSize"));
 //		System.out.println(solrQuery.get("queryResultWindowSize"));
 		
 //		ModifiableSolrParams params = new ModifiableSolrParams();
 //		System.out.println(params.get("query/queryResultWindowSize"));
 //	    params.set("queryResultWindowSize", Integer.toString(max));
 	    
 //	    QueryResponse response = solr.query(params);
 //		.setParam("queryResultWindowSize", Integer.toString(max))
 		
 		
 		for (Iterator iterator = facetFields.iterator(); iterator.hasNext();) {
 			String facetField = (String) iterator.next();
 			solrQuery.addFacetField(facetField);
 		}
 		
 		QueryResponse rsp = server.query(solrQuery);
 //		System.out.println(rsp.getResults().getNumFound());
 		
 		List facetsFields = rsp.getFacetFields();
 		sBuild.append("<facets>\n");
 		if (facetsFields.size() > 0) {
 			List facetValues;
 			FacetField facetField;
 			FacetField.Count innerFacetField;
 			for (Iterator facetIterator = facetsFields.iterator(); facetIterator.hasNext();) {
 				facetField = (FacetField) facetIterator.next();
 				sBuild.append("<facet_field name=\"" + facetField.getName() +"\">\n");
 				
 				facetValues = facetField.getValues();
 				if (facetValues != null) {
 					for (Iterator ifacetIterator = facetValues.iterator(); ifacetIterator.hasNext();) {
 						innerFacetField = (FacetField.Count) ifacetIterator.next();
 						sBuild.append("<facet_count name=\"" + innerFacetField.getName() +"\">" + innerFacetField.getCount() + "</facet_count>\n");
 					}
 				}
 				
 				sBuild.append("</facet_field>\n");
 				
 			}
 		}
 		sBuild.append("</facets>\n");
 //		sBuild.append("<results>\n");
 //		
 //		SolrDocumentList docs = rsp.getResults();
 //		SolrDocument doc;
 //		for (int i = start-1; i < docs.size() && (max < 0 || i < start-1+max); i++) {
 //			doc = docs.get(i);
 //			sBuild.append(doc.getFieldValue("maceenrichedlom")+"\n");
 //		}
 //
 //	    sBuild.append("</results>");
 	    sBuild.append("</response>");
 	    
 	    conn.close();
 
     	return sBuild.toString();
 	}
 	
 	
 
 	
 	
 
 }
