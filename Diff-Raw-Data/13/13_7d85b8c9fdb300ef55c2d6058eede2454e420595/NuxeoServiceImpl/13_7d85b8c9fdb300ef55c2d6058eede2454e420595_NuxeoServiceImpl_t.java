 package org.esup.portlet.intranet.nuxeo;
 
 import org.esup.portlet.intranet.web.NuxeoResource;
 import org.nuxeo.ecm.automation.client.jaxrs.Constants;
 import org.nuxeo.ecm.automation.client.jaxrs.Session;
 import org.nuxeo.ecm.automation.client.jaxrs.adapters.DocumentService;
 import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
 import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
 import org.nuxeo.ecm.automation.client.jaxrs.model.FileBlob;
 import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
 import org.springframework.stereotype.Service;
 
 @Service
 public class NuxeoServiceImpl implements NuxeoService{
 	
 	private String defaultCondition = "AND (ecm:mixinType != 'HiddenInNavigation') AND (ecm:currentLifeCycleState != 'deleted') ";
 	
 	private Documents queryList(NuxeoResource nuxeoResource, String query) throws Exception{
 		Session session = nuxeoResource.getNuxeoSession();
 		Documents docs = (Documents) session.newRequest(DocumentService.Query).setHeader(
 		        Constants.HEADER_NX_SCHEMAS, "*").set("query", query).execute();
 		return docs;
 	}
 	
 	@Override
 	public Documents getList(NuxeoResource nuxeoResource) throws Exception{
 		return getList(nuxeoResource, nuxeoResource.getIntranetPath());
 	}
 
 	@Override
 	public Documents getList(NuxeoResource nuxeoResource, String intranetPath) throws Exception{
 		if (intranetPath == null)
 			intranetPath = nuxeoResource.getIntranetPath();
 		Session session = nuxeoResource.getNuxeoSession();
 		Document root = (Document) session.newRequest(DocumentService.FetchDocument).set("value", intranetPath).execute();
 		if(root != null){
 			String query = "SELECT * FROM Document WHERE ecm:parentId = '"+root.getId()+"'" + defaultCondition + " ORDER BY dc:title";
 			return queryList(nuxeoResource, query);
 		}
 		return null;
 	}
 	
 	@Override
 	public FileBlob getFile(NuxeoResource nuxeoResource, String filePath) throws Exception{
 		Session session = nuxeoResource.getNuxeoSession();
 		// Get the file document where blob was attached
 		Document doc = (Document) session.newRequest(DocumentService.FetchDocument).setHeader(
 		        Constants.HEADER_NX_SCHEMAS, "*").set("value", filePath).execute();
 		
 		// get the file content property
 		PropertyMap map = doc.getProperties().getMap("file:content");
 		// get the data URL
 		String path = map.getString("data");
 		 
 		// download the file from its remote location
 		FileBlob blob = (FileBlob) session.getFile(path);
 		return blob;
 	}
 
 	@Override
 	public Documents getNews(NuxeoResource nuxeoResource) throws Exception{
 		String query = "SELECT * FROM Document WHERE (ecm:primaryType <> 'Folder') AND (ecm:path STARTSWITH \"" + nuxeoResource.getIntranetPath() + "\")"
 				+ defaultCondition +" ORDER BY dc:modified DESC ";
 		return queryList(nuxeoResource, query);
 	}
 
 	@Override
 	public Documents search(NuxeoResource nuxeoResource, String key) throws Exception{
 		return search(false, nuxeoResource, key);
 	}
 	
 	@Override
 	public Documents search(boolean isMobile, NuxeoResource nuxeoResource, String key) throws Exception {
		
		String query;
		if(isMobile){
			//Se limiter aux documents et pas les dossiers (la vue desktop propose aussi les dossiers)
			query = "SELECT * FROM Document WHERE (ecm:primaryType <> 'Folder') AND (ecm:fulltext = \"" + key
					+ "\") AND (ecm:isCheckedInVersion = 0) AND (ecm:path STARTSWITH \"" + nuxeoResource.getIntranetPath() + "\") "+ defaultCondition +" ORDER BY dc:modified DESC";
		}else{
			query = "SELECT * FROM Document WHERE (ecm:fulltext = \"" + key
					+ "\") AND (ecm:isCheckedInVersion = 0) AND (ecm:path STARTSWITH \"" + nuxeoResource.getIntranetPath() + "\") "+ defaultCondition +" ORDER BY dc:modified DESC";
		}
		
 		return queryList(nuxeoResource, query);
 	}
 
 }
