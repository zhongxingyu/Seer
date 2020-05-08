 package com.aavu.server.service.impl;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Date;
 
 import org.acegisecurity.Authentication;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 import com.aavu.client.domain.GDocument;
 import com.aavu.client.domain.GSpreadsheet;
 import com.aavu.client.domain.GoogleData;
 import com.aavu.client.domain.Topic;
 import com.aavu.client.domain.User;
 import com.aavu.client.exception.HippoBusinessException;
 import com.aavu.client.exception.HippoException;
 import com.aavu.server.dao.SelectDAO;
 import com.aavu.server.service.TheGoogleService;
 import com.aavu.server.service.TopicService;
 import com.aavu.server.service.UserService;
 import com.google.gdata.client.docs.DocsService;
 import com.google.gdata.client.http.AuthSubUtil;
 import com.google.gdata.data.Category;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.docs.DocumentListEntry;
 import com.google.gdata.data.docs.DocumentListFeed;
 import com.google.gdata.util.ServiceException;
 
 /**
  * TODO Not transactional, need to get a better solution to these batch loads, bc one big
  * transaction can explode on us.
  * 
  * @author Jeff Dwyer
  * 
  */
 public class GoogleServicesImpl implements TheGoogleService {
 
 	private static final Logger log = Logger.getLogger(GoogleServicesImpl.class);
 
 	private TopicService topicService;
 	private UserService userService;
 
 	private SelectDAO selectDAO;
 
 	private DocsService service;
 
 	private String rootName;
 
 	private String docListURL;
 	private String authScopeURL;
 
 
 	public GoogleServicesImpl(String applicationName, String rootName) {
 		service = new DocsService(applicationName);
 		this.rootName = rootName;
 	}
 
 
	public int importDocsForToken(String onetimeUseToken) throws IOException, ServiceException {
		service.setAuthSubToken(onetimeUseToken);
 		return doImport(null);
 	}
 
 	public int importDocsForUser(final String username, String password) throws IOException,
 			ServiceException {
 
 		service.setUserCredentials(username, password);
 		return doImport(username);
 	}
 
 	/**
 	 * do a create new if exists for all docs
 	 * 
 	 * Don't call it get* since that's our select only aop code
 	 * 
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	private int doImport(final String googleUsername) throws IOException, ServiceException {
 		URL documentListFeedUrl = new URL(docListURL);
 		final DocumentListFeed feed = service.getFeed(documentListFeedUrl, DocumentListFeed.class);
 
 		final Authentication authentication = SecurityContextHolder.getContext()
 				.getAuthentication();
 		final User user = userService.getCurrentUser();
 
 		if (log.isDebugEnabled()) {
 			for (DocumentListEntry entry : feed.getEntries()) {
 				log.debug("Found Entry: " + entry.getTitle().getPlainText());
 			}
 		}
 
 
 		Thread addTagThread = new Thread() {
 			public void run() {
 				try {
 					SecurityContextHolder.getContext().setAuthentication(authentication);
 
 					// create a parent entry so that everything doesn't just wack onto the main
 					// desktop.
 					//
 					Topic googleRoot = topicService.createNewIfNonExistent(rootName);
 
 					// sort by username, since somebody might have docs for jdwyah@gmail.com and
 					// jdwyah@myhippocampus.com
 					// Unfortunately, token style authentication doesn't let us do this.
 					Topic userRoot = googleRoot;
 					if (googleUsername != null) {
 						userRoot = topicService.createNewIfNonExistent(googleUsername, googleRoot);
 					}
 
 					log.debug("Adding to userRoot " + userRoot);
 
 					for (DocumentListEntry entry : feed.getEntries()) {
 
 						log.info("Adding gdoc " + entry.getTitle().getPlainText());
 						add(user, entry, userRoot);
 
 
 
 					}
 
 
 				} catch (HippoException e) {
 					log.error("addGoogleDoc thread " + e);
 				}
 
 				log.info("Google Apps Update Complete");
 				userService.setGoogleAppsUpdate();
 			}
 		};
 		addTagThread.setPriority(Thread.MIN_PRIORITY);
 		addTagThread.start();
 
 		log.info("Returning to caller.");
 
 		return feed.getEntries().size();
 
 
 	}
 
 	/**
 	 * == on URI
 	 * 
 	 * @param doc
 	 * @param parent
 	 * @throws HippoBusinessException
 	 */
 	private void add(User user, DocumentListEntry doc, Topic parent) throws HippoBusinessException {
 
 
 		Link link = doc.getLink(null, "text/html");
 
 		if (link == null) {
 			throw new HippoBusinessException("No text/HTML Link");
 		}
 
 		Topic existingT = selectDAO.getForURI(link.getHref(), user, user);
 
 		if (existingT == null) {
 
 			GoogleData data = null;
 
 			if (isDocument(doc)) {
 				data = topicService.createNewIfNonExistent(doc.getTitle().getPlainText(),
 						GDocument.class, parent);
 
 			} else if (isSpreadsheet(doc)) {
 				data = topicService.createNewIfNonExistent(doc.getTitle().getPlainText(),
 						GSpreadsheet.class, parent);
 			} else {
 				log.warn("No Support for this google doc type ");
 			}
 
 			// data.setCreated(new Date(doc.getPublished().getValue()));
 			if (doc.getUpdated() != null) {
 				data.setLastUpdated(new Date(doc.getUpdated().getValue()));
 			}
 
 			data.setUri(link.getHref());
 
 
 			topicService.save(data);
 
 		}
 		// if it already exists.. update? I guess we can update the title, we might overwrite
 		// something, but it's probably more likely that gapps is up to date.
 		//
 		else {
 			if (existingT instanceof GoogleData) {
 				GoogleData existing = (GoogleData) existingT;
 
 				existing.setTitle(doc.getTitle().getPlainText());
 
 				existing.setUri(link.getHref());
 
 				topicService.save(existing);
 			} else {
 				throw new HippoBusinessException("Retrieved non Google Data " + link);
 			}
 
 		}
 
 
 
 	}
 
 	public void printDocumentEntry(DocumentListEntry doc) {
 
 		String shortId = doc.getId().substring(doc.getId().lastIndexOf('/') + 1);
 
 		log.info(" -- Document(" + shortId + "/" + doc.getTitle().getPlainText() + ")");
 
 		for (Category c : doc.getCategories()) {
 			log.info("cat: " + c);
 		}
 		for (Link l : doc.getLinks()) {
 			log.info("link: " + l.getHref() + " " + l.getTitle());
 		}
 
 		log.info("go there " + doc.getDocumentLink().getHref() + " "
 				+ doc.getDocumentLink().getTitle());
 		log.info("title: " + doc.getTitle().getPlainText());
 		log.info("summary: " + doc.getSummary());
 		log.info("updated: " + doc.getUpdated());
 		log.info("published: " + doc.getPublished());
 
 		System.out.println(isDocument(doc));
 		System.out.println(isSpreadsheet(doc));
 
 	}
 
 	private boolean is(DocumentListEntry doc, String label) {
 		for (Category c : doc.getCategories()) {
 			if (label.equals(c.getLabel())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isDocument(DocumentListEntry doc) {
 		return is(doc, "document");
 	}
 
 	private boolean isSpreadsheet(DocumentListEntry doc) {
 		return is(doc, "spreadsheet");
 	}
 
 	@Required
 	public void setTopicService(TopicService topicService) {
 		this.topicService = topicService;
 	}
 
 	@Required
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 	@Required
 	public void setSelectDAO(SelectDAO selectDAO) {
 		this.selectDAO = selectDAO;
 	}
 
 	@Required
 	public void setDocListURL(String docListURL) {
 		this.docListURL = docListURL;
 	}
 
 	@Required
 	public void setAuthScopeURL(String authScopeURL) {
 		this.authScopeURL = authScopeURL;
 	}
 
 
 	// private static final String DEFAULT_PROTOCOL = "https";
 	// private static final String DEFAULT_DOMAIN = "www.google.com";
 	// public String getAuthorizationURL(String googleAuthReturnURL, Strin domain) {
 	//		
 	// if (StringUtils.hasText(domain)) {
 	// return AuthSubUtil.getRequestUrl(DEFAULT_PROTOCOL, domain, googleAuthReturnURL,
 	// docListURL, false, true);
 	// } else {
 	// return AuthSubUtil.getRequestUrl(DEFAULT_PROTOCOL, DEFAULT_DOMAIN, googleAuthReturnURL,
 	// docListURL, false, true);
 	// }
 	//
 	// }
 	public String getAuthorizationURL(String googleAuthReturnURL) {
		return AuthSubUtil.getRequestUrl(googleAuthReturnURL, authScopeURL, false, false);
 	}
 
 }
