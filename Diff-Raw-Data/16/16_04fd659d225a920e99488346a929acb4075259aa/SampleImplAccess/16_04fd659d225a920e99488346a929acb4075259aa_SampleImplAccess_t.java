 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation
  *
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sample.impl;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.EntityAccessOverloadException;
 import org.sakaiproject.entity.api.EntityCopyrightException;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.entity.api.EntityNotDefinedException;
 import org.sakaiproject.entity.api.EntityPermissionException;
 import org.sakaiproject.entity.api.EntityProducer;
 import org.sakaiproject.entity.api.HttpAccess;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.util.StringUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * <p>
  * SampleImplAccess is a service implementation that demonstrates how to store application-private content in ContentHosting, and to make these
  * available from the Access servlet under the application's control. This code would usually be found in a service implementation of the
  * application's service API. In this example, there is no API, but there is still an implementation. For a real application, we would add the API
  * implementation code to this code.
  * <p>
  * The content is stored in ContentHosting's "private" space (/private/...), in a folder named for this application. The name choosen for example this
  * is "sampleAccess". In our init() method, we make sure that this space exists. Note the use of a SecurityAdvisor there to assure that the
  * ContentHostingService calls pass security.
  * <p>
  * A SecurityAdvisor is code you provide, related only to processing of the current request (i.e. code running on the current thread). When there is a
  * security advisor, any normal security calls are first sent to the advisor code. That code can allow the access, deny the access, or say nothing
  * about the access, passing the decision on to any other SecurityAdvisors in a stack, or to the normal security processing code.
  * <p>
  * You use a security advisor when you can establish that some operation should be allowed to happen, but you are going to collaborate with some other
  * service to make the action happen, and the other service might not know that it is ok to perform the calls you are about to make. The natural
  * security it would invoke might not be proper for your application. So you place a security advisor on the stack and take over security for the
  * duration of your request processing.
  * <p>
  * In a normal application, the end-user's HTTP interaction would result in content to save in the application's ContentHosting area, either from form
  * fields or from a file upload. Once the body bytes are created, the application would save these in some named and located file in ContentHosting.
  * We emulate this and show how to do it in the init() method, creating our one hosted item.
  * <p>
  * In a normal application, the HTML sent to the end-user would include URL references to the files in our private ContentHosting space. These might
  * be IMG tags (to embed images), or FRAME or IFRAME addresses, or anything else HTML allows. We ask the ContentResources for their URLs so we can
  * form our HTML responses.
  * <p>
  * When the request comes back from the browser to the URL of our ContentHosting items, the Access servlet fields the request. We register as a
  * EntityProducer in our init() method so that Access will allow us to handle the heart of these requests.
  * <p>
  * Note that we also declare our implementation class as implementing EntityProducer, which is needed to register. This is best done in the
  * implementation class instead of the API, since nobody using your API services really need to know you are an EntityProducer.
  * <p>
  * The way that we know that the URL is for us to handle is because we set the ContentResource to have a URL prefix, our own special one, that we
  * recognize. While an application can use any form of URL after this it wants, it is convienent to just prefix the ContentHosting's natural Access
  * URL with our special code, leaving the rest as a ContentHosting entity reference. We do that in this example.
  * <p>
  * The key thing we are responsible for is determining if the access is allowed. The natural security set on our private area in ContentHosting
  * assures that nobody has any access at all to the items there. If we decide to grant access, we setup a security advisor so that ContentHosting's
  * security check is satisfied.
  * <p>
  * The most common way to check security is to encode the context id into the URL, and into the ContentHosting collection structure of your private
  * content area. Then you can access the context directly from the URL and do a security check. This is what we do in this example.
  * <p>
  * An alternate way to handle security is to store meta-data about each item in your application database. Then you need to encode enough information
  * in the URL to read the item's meta-data. From the meta-data, you can get the ContentHosting reference and security information. We do not show this
  * in this example.
  * <p>
  * If you deploy this sample, you will have a working Access servlet / ContentHosting using application that serves up a single file to anyone
  * authorized to see it. You can test this by entering this URL:
  * <ul>
  * <li>http://<your server dns and port>/access/sampleAccess/content/private/sampleAccess/mercury/test.txt</li>
  * </ul>
  * <p>
  * Note that there are some methods needed for EntityProducer that we don't use here. The Entity Bus will be later enhanced to make this a cleaner
  * process.
  * <p>
  * The key methods are: parseEntityReference...
  */
 public class SampleImplAccess implements EntityProducer
 {
 	/** Our logger. */
 	private static Log M_log = LogFactory.getLog(SampleImplAccess.class);
 
 	/** The chunk size used when streaming (100k). */
 	protected static final int STREAM_BUFFER_SIZE = 102400;
 
 	/*************************************************************************************************************************************************
 	 * These are really the only part of the example that would, if we had it, better go in the API interface than the implementation class.
 	 ************************************************************************************************************************************************/
 
 	/**
 	 * The type string for this application: should not change over time as it may be stored in various parts of persistent entities.
 	 */
 	static final String APPLICATION_ID = "sakai:sampleAccess";
 
 	/** This string starts the references to resources in this service. */
 	static final String REFERENCE_ROOT_NAME = "sampleAccess";
 
 	static final String REFERENCE_ROOT = "/" + REFERENCE_ROOT_NAME;
 
 	/*************************************************************************************************************************************************
 	 * Abstractions, etc.
 	 ************************************************************************************************************************************************/
 
 	/**
 	 * Setup a security advisor.
 	 */
 	protected void pushAdvisor()
 	{
 		// setup a security advisor
 		m_securityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 	}
 
 	/**
 	 * Remove our security advisor.
 	 */
 	protected void popAdvisor()
 	{
 		m_securityService.popAdvisor();
 	}
 
 	/**
 	 * Check security for this entity.
 	 * 
 	 * @param ref
 	 *        The Reference to the entity.
 	 * @return true if allowed, false if not.
 	 */
 	protected boolean checkSecurity(Reference ref)
 	{
 		// TODO:
 		return true;
 	}
 
 	/*************************************************************************************************************************************************
 	 * Dependencies
 	 ************************************************************************************************************************************************/
 
 	/** Dependency: ContentHostingService */
 	protected ContentHostingService m_contentHostingService = null;
 
 	/**
 	 * Dependency: ContentHostingService.
 	 * 
 	 * @param service
 	 *        The ContentHostingService.
 	 */
 	public void setContentHostingService(ContentHostingService service)
 	{
 		m_contentHostingService = service;
 	}
 
 	/** Dependency: EntityManager */
 	protected EntityManager m_entityManager = null;
 
 	/**
 	 * Dependency: EntityManager.
 	 * 
 	 * @param service
 	 *        The EntityManager.
 	 */
 	public void setEntityManager(EntityManager service)
 	{
 		m_entityManager = service;
 	}
 
 	/** Dependency: ServerConfigurationService. */
 	protected ServerConfigurationService m_serverConfigurationService = null;
 
 	/**
 	 * Dependency: ServerConfigurationService.
 	 * 
 	 * @param service
 	 *        The ServerConfigurationService.
 	 */
 	public void setServerConfigurationService(ServerConfigurationService service)
 	{
 		m_serverConfigurationService = service;
 	}
 
 	/** Dependency: SecurityService */
 	protected SecurityService m_securityService = null;
 
 	/**
 	 * Dependency: SecurityService.
 	 * 
 	 * @param service
 	 *        The SecurityService.
 	 */
 	public void setSecurityService(SecurityService service)
 	{
 		m_securityService = service;
 	}
 
 	/** Dependency: SessionManager */
 	protected SessionManager m_sessionManager = null;
 
 	/**
 	 * Dependency: SessionManager.
 	 * 
 	 * @param service
 	 *        The SessionManager.
 	 */
 	public void setSessionManager(SessionManager service)
 	{
 		m_sessionManager = service;
 	}
 
 	/*************************************************************************************************************************************************
 	 * Init and Destroy
 	 ************************************************************************************************************************************************/
 
 	/**
 	 * Assure that a collection with this name exists in the container collection: create it if it is missing.
 	 * 
 	 * @param container
 	 *        The full path of the container collection.
 	 * @param name
 	 *        The collection name to check and create.
 	 */
 	protected void assureCollection(String container, String name)
 	{
		String collection = container + name;
 		try
 		{
			m_contentHostingService.getCollection(collection);
 		}
 		catch (IdUnusedException e)
 		{
 			// create it
			M_log.info("init: creating root collection: "+ collection);
 
 			try
 			{
				ContentCollectionEdit edit = m_contentHostingService.addCollection(collection);
 				ResourcePropertiesEdit props = edit.getPropertiesEdit();
 
 				// set the alternate reference root so we get all requests
 				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 
 				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 
 				m_contentHostingService.commitCollection(edit);
 			}
 			catch (IdUsedException e2)
 			{
 				M_log.warn("init: creating our root collection: " + e2.toString());
 			}
 			catch (IdInvalidException e2)
 			{
 				M_log.warn("init: creating our root collection: " + e2.toString());
 			}
 			catch (PermissionException e2)
 			{
 				M_log.warn("init: creating our root collection: " + e2.toString());
 			}
 			catch (InconsistentException e2)
 			{
 				M_log.warn("init: creating our root collection: " + e2.toString());
 			}
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("init: checking our root collection: " + e.toString());
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("init: checking our root collection: " + e.toString());
 		}
 	}
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 		// setup a security advisor
 		pushAdvisor();
 
 		// set the current user to admin
 		Session s = m_sessionManager.getCurrentSession();
 		String oldUserId = null;
 		if (s != null)
 		{
 			oldUserId = s.getUserId();
 			s.setUserId("admin");
 		}
 		else
 		{
 			M_log.warn("init - no SessionManager.getCurrentSession, cannot set to admin user");
 		}
 
 		try
 		{
 			// register as an entity producer
 			m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);
 
 			// assume private exists
 			// make sure our root area exists
			assureCollection("/private/", REFERENCE_ROOT_NAME + "/");
 
 			// make sure the context collection for mercury in there exists.
			assureCollection("/private" + REFERENCE_ROOT + "/", "mercury/");
 
 			// check if the content is available
 			String contentRef = "/private" + REFERENCE_ROOT + "/mercury/" + "test.txt";
 
 			try
 			{
 				m_contentHostingService.getResource(contentRef);
 			}
 			catch (IdUnusedException e)
 			{
 				// create it
 				try
 				{
 					ContentResourceEdit edit = m_contentHostingService.addResource(contentRef);
 					edit.setContent("Hello there!".getBytes());
 					edit.setContentType("text/plain");
 					ResourcePropertiesEdit props = edit.getPropertiesEdit();
 
 					// set the alternate reference root so we get all requests
 					props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 
 					props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, "test.txt");
 
 					m_contentHostingService.commitResource(edit);
 				}
 				catch (PermissionException e2)
 				{
 					M_log.warn("init: creating our content: " + e2.toString());
 				}
 				catch (IdUsedException e2)
 				{
 					M_log.warn("init: creating our content: " + e2.toString());
 				}
 				catch (IdInvalidException e2)
 				{
 					M_log.warn("init: creating our content: " + e2.toString());
 				}
 				catch (InconsistentException e2)
 				{
 					M_log.warn("init: creating our content: " + e2.toString());
 				}
 				catch (ServerOverloadException e2)
 				{
 					M_log.warn("init: creating our content: " + e2.toString());
 				}
 				catch (OverQuotaException e2)
 				{
 					M_log.warn("init: creating our content: " + e2.toString());
 				}
 
 				M_log.info("init: creating content");
 			}
 			catch (TypeException e)
 			{
 				M_log.warn("init: checking our content: " + e.toString());
 			}
 			catch (PermissionException e)
 			{
 				M_log.warn("init: checking our content: " + e.toString());
 			}
 
 			M_log.info("init()");
 		}
 		catch (Throwable t)
 		{
 			M_log.warn("init(): ", t);
 		}
 
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 
 			// restore the current user, if any
 			if ((oldUserId != null) && (s != null))
 			{
 				s.setUserId(oldUserId);
 			}
 		}
 	}
 
 	/**
 	 * Returns to uninitialized state.
 	 */
 	public void destroy()
 	{
 		M_log.info("destroy()");
 	}
 
 	/*************************************************************************************************************************************************
 	 * EntityProducer
 	 ************************************************************************************************************************************************/
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean parseEntityReference(String reference, Reference ref)
 	{
 		if (reference.startsWith(REFERENCE_ROOT))
 		{
 			// we will get null, sampleAccess, content, private, sampleAccess, <context>, test.txt
 			// we will store the context, and the ContentHosting reference in our id field.
 			String id = null;
 			String context = null;
 			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);
 
 			if (parts.length > 5)
 			{
 				context = parts[5];
 				id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
 			}
 
 			ref.set(APPLICATION_ID, null, id, null, context);
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public HttpAccess getHttpAccess()
 	{
 		return new HttpAccess()
 		{
 			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
 					throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
 			{
 				// decide on security
 				if (!checkSecurity(ref))
 				{
 					throw new EntityPermissionException(m_sessionManager.getCurrentSessionUserId(), "sampleAccess", ref.getReference());
 				}
 
 				// isolate the ContentHosting reference
 				Reference contentHostingRef = m_entityManager.newReference(ref.getId());
 
 				// setup a security advisor
 				pushAdvisor();
 				try
 				{
 					// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 					EntityProducer service = contentHostingRef.getEntityProducer();
 					if (service == null) throw new EntityNotDefinedException(ref.getReference());
 
 					// get the producer's HttpAccess helper, it might not support one
 					HttpAccess access = service.getHttpAccess();
 					if (access == null) throw new EntityNotDefinedException(ref.getReference());
 
 					// let the helper do the work
 					access.handleAccess(req, res, contentHostingRef, copyrightAcceptedRefs);
 				}
 				finally
 				{
 					// clear the security advisor
 					popAdvisor();
 				}
 			}
 		};
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Entity getEntity(Reference ref)
 	{
 		// decide on security
 		if (!checkSecurity(ref)) return null;
 
 		// isolate the ContentHosting reference
 		Reference contentHostingRef = m_entityManager.newReference(ref.getId());
 
 		// setup a security advisor
 		pushAdvisor();
 		try
 		{
 			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 			EntityProducer service = contentHostingRef.getEntityProducer();
 			if (service == null) return null;
 
 			// pass on the request
 			return service.getEntity(contentHostingRef);
 		}
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Collection getEntityAuthzGroups(Reference ref, String userId)
 	{
 		// Since we handle security ourself, we won't support anyone else asking
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getEntityDescription(Reference ref)
 	{
 		// decide on security
 		if (!checkSecurity(ref)) return null;
 
 		// isolate the ContentHosting reference
 		Reference contentHostingRef = m_entityManager.newReference(ref.getId());
 
 		// setup a security advisor
 		pushAdvisor();
 		try
 		{
 			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 			EntityProducer service = contentHostingRef.getEntityProducer();
 			if (service == null) return null;
 
 			// pass on the request
 			return service.getEntityDescription(contentHostingRef);
 		}
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ResourceProperties getEntityResourceProperties(Reference ref)
 	{
 		// decide on security
 		if (!checkSecurity(ref)) return null;
 
 		// isolate the ContentHosting reference
 		Reference contentHostingRef = m_entityManager.newReference(ref.getId());
 
 		// setup a security advisor
 		pushAdvisor();
 		try
 		{
 			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
 			EntityProducer service = contentHostingRef.getEntityProducer();
 			if (service == null) return null;
 
 			// pass on the request
 			return service.getEntityResourceProperties(contentHostingRef);
 		}
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getEntityUrl(Reference ref)
 	{
 		return m_serverConfigurationService.getAccessUrl() + ref.getReference();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getLabel()
 	{
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
 			Set userListAllowImport)
 	{
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
 	{
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean willArchiveMerge()
 	{
 		return false;
 	}
 }
