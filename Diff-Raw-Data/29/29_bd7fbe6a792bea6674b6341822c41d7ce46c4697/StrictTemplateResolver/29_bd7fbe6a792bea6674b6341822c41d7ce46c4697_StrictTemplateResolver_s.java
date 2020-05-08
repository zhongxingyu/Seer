 package pl.solr.solrjt.mock;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.solr.client.solrj.SolrRequest;
 import org.apache.solr.client.solrj.request.UpdateRequest;
 
 public class StrictTemplateResolver implements TemplateResolver {
 
 	public String resolve(final SolrRequest req) {
		String fileBase;
 		if (req instanceof UpdateRequest) {
			fileBase = ((UpdateRequest) req).getDocuments().toString();
 		} else {
			fileBase = req.getParams().toString();
 		}
		return DigestUtils.md5Hex(fileBase) + ".xml";
 	}
 
 }
