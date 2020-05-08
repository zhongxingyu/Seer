 package pl.solr.solrjt.mock;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.solr.client.solrj.SolrRequest;
 import org.apache.solr.client.solrj.request.UpdateRequest;
 
 public class StrictTemplateResolver implements TemplateResolver {
 
 	public String resolve(final SolrRequest req) {
		return DigestUtils.md5Hex(getFileBase(req)) + ".xml";
	}

	public String getFileBase(SolrRequest req) {
 		if (req instanceof UpdateRequest) {
			return ((UpdateRequest) req).getDocuments().toString();
 		} else {
			return req.getParams().toString();
 		}
 	}
 

 }
