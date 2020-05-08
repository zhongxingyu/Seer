 package pl.solr.solrjt.mock;
 
 import org.apache.solr.client.solrj.SolrRequest;
 
 public interface TemplateResolver {
 
 	String resolve(SolrRequest req);
 
	String getFileBase(SolrRequest req);

 }
