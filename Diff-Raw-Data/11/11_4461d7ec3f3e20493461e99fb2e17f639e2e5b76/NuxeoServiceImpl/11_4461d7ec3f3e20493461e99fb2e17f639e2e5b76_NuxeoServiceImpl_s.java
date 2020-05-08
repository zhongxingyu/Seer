 package org.esup.portlet.intranet.domain.nuxeo;
 
 import org.nuxeo.ecm.automation.client.Constants;
 import org.nuxeo.ecm.automation.client.Session;
 import org.nuxeo.ecm.automation.client.adapters.DocumentService;
 import org.nuxeo.ecm.automation.client.model.Document;
 import org.nuxeo.ecm.automation.client.model.Documents;
 import org.nuxeo.ecm.automation.client.model.FileBlob;
 import org.nuxeo.ecm.automation.client.model.PaginableDocuments;
 import org.nuxeo.ecm.automation.client.model.PropertyMap;
 import org.springframework.stereotype.Service;
 @Service
 public class NuxeoServiceImpl implements NuxeoService{
 	
 	private String defaultCondition = "AND (ecm:mixinType != 'HiddenInNavigation') AND (ecm:currentLifeCycleState != 'deleted') ";
 	
 	@Override
 	public Documents getList(NuxeoResource nuxeoResource) throws Exception{
 		Session session = nuxeoResource.getSession();
 		Document root = (Document) session
 							.newRequest(DocumentService.FetchDocument)
 							.set("value", nuxeoResource.getIntranetPath())
 							.execute();
		Documents docs = (Documents) session
							.newRequest("Document.GetChildren")
							.setHeader(Constants.HEADER_NX_SCHEMAS, "*")
							.setInput(root)
							.execute();
		return docs;
 	}
 	
 	@Override
 	public PaginableDocuments news(NuxeoResource nuxeoResource, int pageSize) throws Exception{
 		String query = "SELECT * FROM Document WHERE (ecm:primaryType <> 'Folder') AND (ecm:path STARTSWITH \"" 
 				+ nuxeoResource.getIntranetPath() + "\")"
 				+ defaultCondition +" ORDER BY dc:modified DESC ";
 		Session session = nuxeoResource.getSession();
 		return (PaginableDocuments)session
 				.newRequest("Document.PageProvider")
 				.setHeader(Constants.HEADER_NX_SCHEMAS, "*")
 		        .set("page", 0)
 		        .set("pageSize",pageSize)
 		        .set("query", query)
 		        .execute();
 	}
 	
 	@Override
 	public Documents search(NuxeoResource nuxeoResource, String key) throws Exception {
 		String query = "SELECT * FROM Document WHERE (ecm:fulltext = \"" + key
 				+ "\") AND (ecm:isCheckedInVersion = 0) AND (ecm:path STARTSWITH \"" 
 				+ nuxeoResource.getIntranetPath() + "\") "+ defaultCondition +" ORDER BY dc:modified DESC";
 		return queryList(nuxeoResource, query);
 	}
 	
 	private Documents queryList(NuxeoResource nuxeoResource, String query) throws Exception{
 		Session session = nuxeoResource.getSession();
 		Documents docs = (Documents) session
 							.newRequest(DocumentService.Query)
 							.setHeader(Constants.HEADER_NX_SCHEMAS, "*")
 							.set("query", query)
 							.execute();
 		return docs;
 	}
 	
 	@Override
 	public FileBlob getFile(NuxeoResource nuxeoResource, String uid) throws Exception{
 		Session session = nuxeoResource.getSession();
 		// Get the file document where blob was attached
 		Document doc = (Document) session
 							.newRequest(DocumentService.FetchDocument)
 							.setHeader(Constants.HEADER_NX_SCHEMAS, "*")
 							.set("value", uid)
 							.execute();
 		
 		// get the file content property
 		PropertyMap map = doc.getProperties().getMap("file:content");
 		// get the data URL
 		String path = map.getString("data");
 		 
 		// download the file from its remote location
 		FileBlob blob = (FileBlob) session.getFile(path);
 		return blob;
 	}
 
 
 
 }
