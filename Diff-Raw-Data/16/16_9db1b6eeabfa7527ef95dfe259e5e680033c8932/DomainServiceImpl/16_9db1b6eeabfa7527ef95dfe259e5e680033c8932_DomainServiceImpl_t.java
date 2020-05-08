 /**
  * ESUP-Portail Blank Application - Copyright (c) 2010 ESUP-Portail consortium.
  */
 package org.esupportail.syncfsnx.domain;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.activation.MimetypesFileTypeMap;
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.syncfsnx.domain.beans.Configurator;
 import org.esupportail.syncfsnx.domain.beans.SyncDocument;
 import org.esupportail.syncfsnx.domain.beans.SyncDocumentType;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
 import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.FileBlob;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * @author Raymond Bourges
  * 
  */
 public class DomainServiceImpl implements DomainService, InitializingBean {
 
 	/**
 	 * For Serialize.
 	 */
 	private static final long serialVersionUID = 5562208937407153456L;
 
 	/**
 	 * For Logging.
 	 */
 	private final Logger logger = new LoggerImpl(this.getClass());
 
 	/**
 	 * configurator
 	 */
 	private Configurator configurator;
 	
 	/**
 	 * nuxeo session
 	 */
 	private Session nxSession;
 
 	@Override
 	public void synchronise() {
 		//get local documents
 		logger.info("get local documents list");
 		HashMap<String, SyncDocument> localDocuments = getLocalDocuments();
 		//get remote documents
 		logger.info("get remote documents list");
 		HashMap<String, SyncDocument> remoteDocuments = getRemoteDocuments();
 		//remove unnecessary remote documents (not presents locally)
 		logger.info("remove unnecessary remote documents");
 		removeUnnecessaryRemoteDocuments(localDocuments, remoteDocuments);
 		//put local documents to remote server
 		logger.info("put local documents to remote server");
 		putLocalDocuments(localDocuments, remoteDocuments);
 	}
 
 	private void putLocalDocuments(
 			HashMap<String, SyncDocument> localDocuments,
 			HashMap<String, SyncDocument> remoteDocuments) {
 		List<String> localKeys = new ArrayList<String>(localDocuments.keySet());
 		Collections.sort(localKeys);
 		List<String> remoteKeys = new ArrayList<String>(remoteDocuments.keySet());
 		Collections.sort(remoteKeys);
 		for (String localKey : localKeys) {
 			SyncDocument local = localDocuments.get(localKey); 
 			//is local document is not present remotely ?
 			if (remoteDocuments.get(localKey) == null) {
 				createNxDocument(local);
 			}
 			else {
 				SyncDocument remote = remoteDocuments.get(localKey); 
 				//is local document not equal remote document (for example modification date is different)
 				if (!local.isFolder()) {
 					if (local.getModificationDate().after(remote.getModificationDate())) {
 						removeNxDocument(local);
 						createNxDocument(local);
 						
 					}
 				}				
 			}			
 		}
 	}
 
 	private void createNxDocument(SyncDocument local) {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Creating " + local.getRelativePath() + " on Nuxeo");
 		}
 		Session session = getNxSession();
 		try {
 			//find parent path
 			String remotePath = configurator.getRemotePath() + local.getRelativePath();
 			String remoteParentPath = remotePath.substring(0, remotePath.lastIndexOf("/"));
 			String finalPath = remotePath.substring(remotePath.lastIndexOf("/") + 1);;
 			Document root = (Document) session.newRequest("Document.Fetch").set("value", remoteParentPath).execute();
 			if (local.isFolder()) {
 				Document rep = (Document) session.newRequest("Document.Create")
 					.setInput(root)
 					.set("type", "Folder")
 					.set("name", finalPath)
 					.set("properties", "dc:title=" + finalPath)
 					.execute();
 			} else {
 				Document doc = (Document) session.newRequest("Document.Create")
 					.setInput(root)
 					.set("type", "File")
 					.set("name", finalPath)
 					.set("properties", "dc:title=" + finalPath)
 					.execute();
 				String LocalPath = configurator.getLocalPath() + local.getRelativePath();
 				File file = new File(LocalPath);
 				FileBlob fb = new FileBlob(file);
 				//find mimeType
 				MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
 				String mimeType = mimeTypesMap.getContentType(file);
 				fb.setMimeType(mimeType);
 				session.newRequest("Blob.Attach")
 					.setHeader(Constants.HEADER_NX_VOIDOP, "true")
 					.setInput(fb)
 					.set("document", doc).execute();
 			}
 		} catch (Exception e) {
 			error("Error creating " + local.getRelativePath() + " document", e);
 		}
 	}
 
 	private void removeUnnecessaryRemoteDocuments(
 			HashMap<String, SyncDocument> localDocuments,
 			HashMap<String, SyncDocument> remoteDocuments) {
 		List<String> remoteKeys = new ArrayList<String>(remoteDocuments.keySet());
 		Collections.sort(remoteKeys);
 		String path = "/////////////";
 		for (String remoteKey : remoteKeys) {
 			//is remote document is not present locally ?
 			if (localDocuments.get(remoteKey) == null) {
 				//test that current key is not a children of already removed file
 				if (!remoteKey.startsWith(path)) {
 					//remove the remote document
 					SyncDocument document = remoteDocuments.get(remoteKey);
 					removeNxDocument(document);
 					path = document.getRelativePath();
 				}				
 			}
 		}
 	}
 
 	/**
 	 * @param document
 	 * remove a document from nuxeo
 	 */
 	private void removeNxDocument(SyncDocument document) {
 		if (logger.isDebugEnabled()) {
 			logger.debug("removing " + document.getRelativePath() + " from Nuxeo");
 		}
 		String path = configurator.getRemotePath() + document.getRelativePath();
 		Session session = getNxSession();
 		Document root;
 		try {
 			root = (Document) session.newRequest("Document.Fetch").set("value", path).execute();
 			session.newRequest("Document.Delete").setInput(root).execute();
 		} catch (Exception e) {
 			error("Error removing " + path + " from nuxeo", e);
 		}
 	}
 
 	private HashMap<String, SyncDocument> getRemoteDocuments() {
 		HashMap<String, SyncDocument> ret = new HashMap<String, SyncDocument>();
 		String path = configurator.getRemotePath();
 		try {
 			ListRemoteFolder(path, getNxSession(), ret);
 		} catch (Exception e) {
 			error("Error reading nuxeo documents", e);
 		}
 		if (logger.isDebugEnabled()) {
 			logger.debug("remote documents:");
 			for (String key : ret.keySet()) {
 				SyncDocument syncDocument = ret.get(key);
 				logger.debug("-->" + syncDocument.getRelativePath());
 			}			
 		}		
 		return ret;
 	}
 
 	/**
 	 * @param msg error message
 	 * @param e exception
 	 * log a error and throw it
 	 */
 	private void error(String msg, Exception e) {
 		logger.error(msg);
 		if (e != null) {
 			throw new RuntimeException(msg, e);			
 		} else {
 			throw new RuntimeException(msg);
 		}
 	}
 
 	private void ListRemoteFolder(String path, Session session, HashMap<String,SyncDocument> documents) throws Exception {
 		Document root = (Document) session.newRequest("Document.Fetch").set("value", path).execute();
 		Documents docs = (Documents) session.newRequest("Document.GetChildren").setInput(root).execute();
 		for (Document document : docs) {
 			SyncDocument current = new SyncDocument();
 			Date modificationDate = document.getLastModified(); 
 			current.setModificationDate(modificationDate);
 			String relativePath = document.getPath();
 			relativePath = relativePath.replaceFirst(configurator.getRemotePath(), "");
 			current.setRelativePath(relativePath);
 			if (document.getType().equals("Folder")) {
 				current.setDocumentType(SyncDocumentType.FOLDER);
 				ListRemoteFolder(document.getPath(), session, documents);
 			}
 			else {
 				current.setDocumentType(SyncDocumentType.FILE);				
 			}
 			documents.put(relativePath, current);
 		}
 	}
 
 	private HashMap<String, SyncDocument> getLocalDocuments() {
 		HashMap<String, SyncDocument> ret = new HashMap<String, SyncDocument>();
 		File root = new File(configurator.getLocalPath());
 		if (!root.isDirectory()) {
 			error(configurator.getLocalPath() + " must be a folder !", null);
 		}
 		listLocalFolder(root, ret);
 		if (logger.isDebugEnabled()) {
 			logger.debug("local documents:");
 			for (String key : ret.keySet()) {
 				SyncDocument syncDocument = ret.get(key);
 				logger.debug("-->" + syncDocument.getRelativePath());
 			}			
 		}		
 		return ret;
 	}
 
 	public void listLocalFolder (File file, HashMap<String, SyncDocument> documents) {
 		if (!file.isHidden()) {
 			SyncDocument current = new SyncDocument();
 			Date modificationDate = new Date(file.lastModified()); 
 			current.setModificationDate(modificationDate);
 			String relativePath = file.getAbsolutePath();
 			relativePath = relativePath.replaceFirst(configurator.getLocalPath(), "");
 			current.setRelativePath(relativePath);
 			if (file.isDirectory()) {
 				current.setDocumentType(SyncDocumentType.FOLDER);
 				File[] files = file.listFiles();
 				if (files != null){
 					for (File children : files) {
 						listLocalFolder(children, documents);
 					}
 				}
 			}
 			else {
 				current.setDocumentType(SyncDocumentType.FILE);
 			}
 			if (!relativePath.equals("")) {
 				documents.put(relativePath, current);				
 			}
 		}
 	}
 
 	public void afterPropertiesSet() throws Exception {
 		Assert.notNull(configurator, "propertie configurator of class " + this.getClass() + " can't be null!");
 	}
 
 	/**
 	 * @param configurator the configurator to set
 	 */
 	public void setConfigurator(Configurator configurator) {
 		this.configurator = configurator;
 	}
 
 	/**
 	 * @return the session
 	 */
 	public Session getNxSession() {
 		if (nxSession == null) {
 			HttpAutomationClient client = new HttpAutomationClient(configurator.getNuxeoAutomationURL());
 			Session session;
 			try {
 				session = client.getSession(configurator.getUser(), configurator.getPassword());
 				nxSession = session;
 			} catch (Exception e) {
 				error("Error creting nuxeo session", e);
 			}
 		}
 		return nxSession;
 	}
 
 }
