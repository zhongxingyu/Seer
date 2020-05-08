 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
  * 
  * Portions completed before September 1, 2008
  * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.mneme.impl;
 
 import java.awt.Container;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipOutputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.ambrosia.util.EscapeRefUrl;
 import org.etudes.mneme.api.Assessment;
 import org.etudes.mneme.api.AssessmentService;
 import org.etudes.mneme.api.Attachment;
 import org.etudes.mneme.api.AttachmentService;
 import org.etudes.mneme.api.MnemeService;
 import org.etudes.mneme.api.Question;
 import org.etudes.mneme.api.SecurityService;
 import org.etudes.mneme.api.Submission;
 import org.etudes.mneme.api.SubmissionService;
 import org.etudes.util.TranslationImpl;
 import org.etudes.util.api.Translation;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
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
 import org.sakaiproject.event.api.EventTrackingService;
 import org.sakaiproject.exception.CopyrightException;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdLengthException;
 import org.sakaiproject.exception.IdUniquenessException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.id.api.IdManager;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.util.BaseResourcePropertiesEdit;
 import org.sakaiproject.util.StringUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.sun.image.codec.jpeg.JPEGCodec;
 import com.sun.image.codec.jpeg.JPEGEncodeParam;
 import com.sun.image.codec.jpeg.JPEGImageEncoder;
 
 /**
  * AttachmentServiceImpl implements AttachmentService.
  */
 public class AttachmentServiceImpl implements AttachmentService, EntityProducer
 {
 	/** A thread-local key to the List of resource bodies translated so far in the thread. */
 	public final static String THREAD_TRANSLATIONS_BODY_KEY = "AttachmentService.body.translations.";
 
 	/** A thread-local key to the List of Translations we have made so far in the thread. */
 	public final static String THREAD_TRANSLATIONS_KEY = "AttachmentService.translations.";
 
 	/** Our logger. */
 	private static Log M_log = LogFactory.getLog(AttachmentServiceImpl.class);
 
 	protected final static String PROP_THUMB = "attachment:thumb";
 
 	protected final static String PROP_UNIQUE_HOLDER = "attachment:unique";
 
 	/** The chunk size used when streaming (100k). */
 	protected static final int STREAM_BUFFER_SIZE = 102400;
 
 	/** Assessment service. */
 	protected AssessmentService assessmentService = null;
 
 	/** Dependency: ContentHostingService */
 	protected ContentHostingService contentHostingService = null;
 
 	/** Dependency: EntityManager */
 	protected EntityManager entityManager = null;
 
 	/** Dependency: EventTrackingService */
 	protected EventTrackingService eventTrackingService = null;
 
 	/** Dependency: IdManager. */
 	protected IdManager idManager = null;
 
 	/** Configuration: to make thumbs for images or not. */
 	protected boolean makeThumbs = true;
 
 	/** Dependency: SecurityService */
 	protected SecurityService securityService = null;
 
 	/** Dependency: SecurityService */
 	protected org.sakaiproject.authz.api.SecurityService securityServiceSakai = null;
 
 	/** Dependency: ServerConfigurationService */
 	protected ServerConfigurationService serverConfigurationService = null;
 
 	/** Dependency: SessionManager */
 	protected SessionManager sessionManager = null;
 
 	/** Dependency: SiteService */
 	protected SiteService siteService = null;
 
 	/** Dependency: SubmissionService */
 	protected SubmissionService submissionService = null;
 
 	/** Dependency: ThreadLocalManager */
 	protected ThreadLocalManager threadLocalManager = null;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, FileItem file,
 			boolean makeThumb, String altRef)
 	{
 		pushAdvisor();
 
 		try
 		{
 			String name = file.getName();
 			if (name != null)
 			{
 				name = massageName(name);
 			}
 
 			String type = file.getContentType();
 
 			// TODO: change to file.getInputStream() for after Sakai 2.3 more efficient support
 			// InputStream body = file.getInputStream();
 			byte[] body = file.get();
 
 			long size = file.getSize();
 
 			// detect no file selected
 			if ((name == null) || (type == null) || (body == null) || (size == 0))
 			{
 				// TODO: if using input stream, close it
 				// if (body != null) body.close();
 				return null;
 			}
 
 			Reference rv = addAttachment(name, name, application, context, prefix, onConflict, type, body, size, makeThumb, altRef);
 			return rv;
 		}
 		finally
 		{
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, Reference resourceRef,
 			boolean makeThumb, String altRef)
 	{
 		// make sure we can read!
 		pushAdvisor();
 
 		try
 		{
 			// get an id from the reference string
 			String chsId = resourceRef.getId();
 			if (chsId.startsWith("/content/"))
 			{
 				chsId = chsId.substring("/content".length());
 			}
 
 			// make sure we can read!
 			ContentResource resource = this.contentHostingService.getResource(chsId);
 			String type = resource.getContentType();
 			long size = resource.getContentLength();
 			byte[] body = resource.getContent();
 			String name = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 
 			// form an id from the resource id
 			String id = massageName(resource.getId());
 
 			Reference rv = addAttachment(id, name, application, context, prefix, onConflict, type, body, size, makeThumb, altRef);
 			return rv;
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		catch (ServerOverloadException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		finally
 		{
 			// clear the security advisor
 			popAdvisor();
 		}
 
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, String name, byte[] body,
 			String type, boolean makeThumb, String altRef)
 	{
 		pushAdvisor();
 
 		try
 		{
 			if (name != null)
 			{
 				name = massageName(name);
 			}
 
 			long size = body.length;
 
 			// detect no file selected
 			if ((name == null) || (type == null) || (body == null) || (size == 0))
 			{
 				return null;
 			}
 
 			Reference rv = addAttachment(name, name, application, context, prefix, onConflict, type, body, size, makeThumb, altRef);
 			return rv;
 		}
 		finally
 		{
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
 	{
 		return null;
 	}
 
 	/**
 	 * Returns to uninitialized state.
 	 */
 	public void destroy()
 	{
 		M_log.info("destroy()");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Attachment> findFiles(String application, String context, String prefix)
 	{
 		return findTypes(application, context, prefix, null, false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Attachment> findImages(String application, String context, String prefix)
 	{
 		return findTypes(application, context, prefix, "image/", false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Attachment> findThumbs(String application, String context, String prefix)
 	{
 		return findTypes(application, context, prefix, null, true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Entity getEntity(Reference ref)
 	{
 		// decide on security
 		if (!checkSecurity(ref)) return null;
 
 		// isolate the ContentHosting reference
 		Reference contentHostingRef = entityManager.newReference(ref.getId());
 
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
 		Reference contentHostingRef = entityManager.newReference(ref.getId());
 
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
 
 		// for download
 		if (DOWNLOAD.equals(ref.getContainer()))
 		{
 			if (DOWNLOAD_ALL_SUBMISSIONS_QUESTION.equals(ref.getSubType()))
 			{
 				ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
 
 				// use the site title and question ids for the file name
 				String[] outerParts = StringUtil.split(ref.getId(), ".");
 				String[] parts = StringUtil.split(outerParts[0], "_");
 				String siteTitle = ref.getContext();
 				try
 				{
 					Site site = this.siteService.getSite(ref.getContext());
 					siteTitle = site.getTitle();
 				}
 				catch (IdUnusedException e)
 				{
 				}
 				String fileName = siteTitle + "_" + parts[1] + "_Submissions.zip";
 				fileName = fileName.replace(' ', '_');
 
 				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, "application/zip");
 				props.addProperty("DAV:displayname", fileName);
 				props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "FALSE");
 
 				return props;
 			}
 			else
 			{
 				// unknown download request
 				M_log.warn("getEntityResourceProperties: unknown download request: " + ref.getReference());
 				return null;
 			}
 		}
 
 		// else for private docs CHS ref
 		else
 		{
 			// isolate the ContentHosting reference
 			Reference contentHostingRef = entityManager.newReference(ref.getId());
 
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
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getEntityUrl(Reference ref)
 	{
 		return serverConfigurationService.getAccessUrl() + ref.getReference();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public HttpAccess getHttpAccess()
 	{
 		final AttachmentServiceImpl service = this;
 
 		return new HttpAccess()
 		{
 			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
 					throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
 			{
 				// decide on security
 				if (!checkSecurity(ref))
 				{
 					throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "sampleAccess", ref.getReference());
 				}
 
 				// for download
 				if (DOWNLOAD.equals(ref.getContainer()))
 				{
 					service.handleAccessDownload(req, res, ref, copyrightAcceptedRefs);
 				}
 
 				// for private docs, forward to CHS
 				else
 				{
 					// isolate the ContentHosting reference
 					Reference contentHostingRef = entityManager.newReference(ref.getId());
 
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
 			}
 		};
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
 	public Reference getReference(String refString)
 	{
 		Reference ref = this.entityManager.newReference(refString);
 		return ref;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Set<String> harvestAttachmentsReferenced(String data, boolean normalize)
 	{
 		// return an insertion-ordered set
 		Set<String> rv = new LinkedHashSet<String>();
 		if (data == null) return rv;
 
 		// harvest the main data
 		rv.addAll(harvestAttachmentsReferenced(data, normalize, null));
 
 		// process a set of references - initially the main data's references
 		Set<String> process = new LinkedHashSet<String>();
 		process.addAll(rv);
 
 		while (!process.isEmpty())
 		{
 			Set<String> secondary = new LinkedHashSet<String>();
 			for (String ref : process)
 			{
 				// check for any html
 				String type = getReferencedDocumentType(ref);
 				if ("text/html".equals(type))
 				{
 					// read the referenced html
 					String secondaryData = readReferencedDocument(ref);
 
 					// harvest it
 					secondary.addAll(harvestAttachmentsReferenced(secondaryData, normalize, ref));
 				}
 			}
 
 			// ignore any secondary we already have
 			secondary.removeAll(rv);
 
 			// collect the secondary
 			rv.addAll(secondary);
 
 			// process the secondary
 			process.clear();
 			process.addAll(secondary);
 		}
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Set<String> harvestEmbedded(String reference, boolean normalize)
 	{
 		// return an insertion-ordered set
 		Set<String> rv = new LinkedHashSet<String>();
 		if (reference == null) return rv;
 
 		// deal with %20, &amp;, and other encoded URL stuff
 		if (normalize)
 		{
 			reference = decodeUrl(reference);
 		}
 
 		// start with this reference
 		rv.add(reference);
 
 		// process a set of references - initially the passed in reference
 		Set<String> process = new LinkedHashSet<String>();
 		process.addAll(rv);
 
 		while (!process.isEmpty())
 		{
 			Set<String> secondary = new LinkedHashSet<String>();
 			for (String ref : process)
 			{
 				// check for any html
 				String type = getReferencedDocumentType(ref);
 				if ("text/html".equals(type))
 				{
 					// read the referenced html
 					String secondaryData = readReferencedDocument(ref);
 
 					// harvest it
 					secondary.addAll(harvestAttachmentsReferenced(secondaryData, normalize, ref));
 				}
 			}
 
 			// ignore any secondary we already have
 			secondary.removeAll(rv);
 
 			// collect the secondary
 			rv.addAll(secondary);
 
 			// process the secondary
 			process.clear();
 			process.addAll(secondary);
 		}
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Translation> importResources(String application, String context, String prefix, NameConflictResolution onConflict,
 			Set<String> resources, boolean makeThumb, String altRef)
 	{
 		// get our thread-local list of translations made in this thread, for this application/prefix combination
 		String threadKey = THREAD_TRANSLATIONS_KEY + application + "." + prefix;
 		List<Translation> threadTranslations = (List<Translation>) threadLocalManager.get(threadKey);
 		if (threadTranslations == null)
 		{
 			threadTranslations = new ArrayList<Translation>();
 			threadLocalManager.set(threadKey, threadTranslations);
 		}
 
 		// get our thread-local list of bodies translated in this thread
 		threadKey = THREAD_TRANSLATIONS_BODY_KEY + application + "." + prefix;
 		List<String> threadBodyTranslations = (List<String>) threadLocalManager.get(threadKey);
 		if (threadBodyTranslations == null)
 		{
 			threadBodyTranslations = new ArrayList<String>();
 			threadLocalManager.set(threadKey, threadBodyTranslations);
 		}
 
 		// collect any that may need html body translation in a second pass
 		List<Reference> toTranslate = new ArrayList<Reference>();
 
 		// collect translations
 		List<Translation> rv = new ArrayList<Translation>();
 
 		for (String refString : resources)
 		{
 			// if we have done this already in the thread, just skip it
 			boolean skip = false;
 			for (Translation imported : threadTranslations)
 			{
 				if (refString.equals(imported.getFrom()))
 				{
 					skip = true;
 
 					// we need the translation as part of the return set
 					rv.add(imported);
 
 					break;
 				}
 			}
 			if (skip) continue;
 
 			Reference ref = this.entityManager.newReference(refString);
 
 			// move the referenced resource into our docs
 			Reference imported = addAttachment(application, context, prefix, onConflict, ref, makeThumb, altRef);
 			if (imported != null)
 			{
 				// make the translation
 				TranslationImpl t = new TranslationImpl(ref.getReference(), imported.getReference());
 				rv.add(t);
 				threadTranslations.add(t);
 
 				// do we need a second-pass translation?
 				String importedRef = imported.getReference();
 				String type = getReferencedDocumentType(importedRef);
 				if ("text/html".equals(type))
 				{
 					// check if we have done this already in the thread (Reference.equals() is not to be trusted -ggolden)
 					boolean found = false;
 					for (String bodyTranslatedReference : threadBodyTranslations)
 					{
 						if (bodyTranslatedReference.equals(importedRef))
 						{
 							found = true;
 							break;
 						}
 					}
 
 					if (!found)
 					{
 						toTranslate.add(imported);
 						threadBodyTranslations.add(importedRef);
 					}
 				}
 			}
 			else
 			{
 				M_log.warn("importResources: failed to import resource: " + ref.getReference());
 			}
 		}
 
 		// now that we have all the translations, update any resources that have html bodies
 		for (Reference ref : toTranslate)
 		{
 			// translate using the full set we have so far for the thread
 			translateHtmlBody(ref, threadTranslations, context);
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 		try
 		{
 			// register as an entity producer
 			entityManager.registerEntityProducer(this, REFERENCE_ROOT);
 
 			M_log.info("init(): thumbs: " + this.makeThumbs);
 		}
 		catch (Throwable t)
 		{
 			M_log.warn("init(): ", t);
 		}
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
 	public boolean parseEntityReference(String reference, Reference ref)
 	{
 		if (reference.startsWith(REFERENCE_ROOT))
 		{
 			// we will get null, sampleAccess, content, private, sampleAccess, <context>, test.txt
 			// we will store the context, and the ContentHosting reference in our id field.
 			String id = null;
 			String context = null;
 			String subType = null;
 			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);
 			String container = null;
 
 			// support for the download access
 			if ((parts.length == 6) && (DOWNLOAD.equals(parts[2])))
 			{
 				context = parts[4];
 
 				// set id to the requested download
 				id = parts[5];
 
 				// set the sub-type to the type of download
 				subType = parts[3];
 
 				// set the container to "download"
 				container = parts[2];
 			}
 
 			// support for private docs
 			else if (parts.length > 5)
 			{
 				context = parts[5];
 				id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
 			}
 
 			ref.set(APPLICATION_ID_ROOT + MNEME_APPLICATION, subType, id, container, context);
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeAttachment(Reference ref)
 	{
 		pushAdvisor();
 
 		try
 		{
 			String id = entityManager.newReference(ref.getId()).getId();
 
 			// check if this has a unique containing collection
 			ContentResource resource = this.contentHostingService.getResource(id);
 			ContentCollection collection = resource.getContainingCollection();
 
 			// remove the resource
 			this.contentHostingService.removeResource(id);
 
 			// if the collection was made just to hold the attachment, remove it as well
 			if (collection.getProperties().getProperty(PROP_UNIQUE_HOLDER) != null)
 			{
 				this.contentHostingService.removeCollection(collection.getId());
 			}
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("removeAttachment: " + e.toString());
 		}
 		catch (ServerOverloadException e)
 		{
 			M_log.warn("removeAttachment: " + e.toString());
 		}
 		catch (InUseException e)
 		{
 			M_log.warn("removeAttachment: " + e.toString());
 
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("removeAttachment: " + e.toString());
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("removeAttachment: " + e.toString());
 		}
 		finally
 		{
 			popAdvisor();
 		}
 	}
 
 	/**
 	 * Set the AssessmentService.
 	 * 
 	 * @param service
 	 *        the AssessmentService.
 	 */
 	public void setAssessmentService(AssessmentService service)
 	{
 		this.assessmentService = service;
 	}
 
 	/**
 	 * Dependency: ContentHostingService.
 	 * 
 	 * @param service
 	 *        The ContentHostingService.
 	 */
 	public void setContentHostingService(ContentHostingService service)
 	{
 		contentHostingService = service;
 	}
 
 	/**
 	 * Dependency: EntityManager.
 	 * 
 	 * @param service
 	 *        The EntityManager.
 	 */
 	public void setEntityManager(EntityManager service)
 	{
 		entityManager = service;
 	}
 
 	/**
 	 * Dependency: EventTrackingService.
 	 * 
 	 * @param service
 	 *        The EventTrackingService.
 	 */
 	public void setEventTrackingService(EventTrackingService service)
 	{
 		eventTrackingService = service;
 	}
 
 	/**
 	 * Set the IdManager
 	 * 
 	 * @param IdManager
 	 *        The IdManager
 	 */
 	public void setIdManager(IdManager idManager)
 	{
 		this.idManager = idManager;
 	}
 
 	/**
 	 * Set the make thumbs setting
 	 * 
 	 * @param value
 	 *        the string of the boolean for the make thumbs setting.
 	 */
 	public void setMakeThumbs(String value)
 	{
 		this.makeThumbs = Boolean.valueOf(value);
 	}
 
 	/**
 	 * Dependency: SecurityService.
 	 * 
 	 * @param service
 	 *        The SecurityService.
 	 */
 	public void setSecurityService(SecurityService service)
 	{
 		this.securityService = service;
 	}
 
 	/**
 	 * Dependency: SecurityService.
 	 * 
 	 * @param service
 	 *        The SecurityService.
 	 */
 	public void setSecurityServiceSakai(org.sakaiproject.authz.api.SecurityService service)
 	{
 		this.securityServiceSakai = service;
 	}
 
 	/**
 	 * Dependency: ServerConfigurationService.
 	 * 
 	 * @param service
 	 *        The ServerConfigurationService.
 	 */
 	public void setServerConfigurationService(ServerConfigurationService service)
 	{
 		this.serverConfigurationService = service;
 	}
 
 	/**
 	 * Dependency: SessionManager.
 	 * 
 	 * @param service
 	 *        The SessionManager.
 	 */
 	public void setSessionManager(SessionManager service)
 	{
 		this.sessionManager = service;
 	}
 
 	/**
 	 * Dependency: SiteService.
 	 * 
 	 * @param service
 	 *        The SiteService.
 	 */
 	public void setSiteService(SiteService service)
 	{
 		this.siteService = service;
 	}
 
 	/**
 	 * Dependency: SubmissionService.
 	 * 
 	 * @param service
 	 *        The SubmissionService.
 	 */
 	public void setSubmissionService(SubmissionService service)
 	{
 		this.submissionService = service;
 	}
 
 	/**
 	 * Dependency: ThreadLocalManager.
 	 * 
 	 * @param service
 	 *        The ThreadLocalManager.
 	 */
 	public void setThreadLocalManager(ThreadLocalManager service)
 	{
 		this.threadLocalManager = service;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String translateEmbeddedReferences(String data, List<Translation> translations)
 	{
 		return translateEmbeddedReferences(data, translations, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean willArchiveMerge()
 	{
 		return false;
 	}
 
 	/**
 	 * Go through the add process, trying the various requested conflict resolution steps.
 	 * 
 	 * @param idName
 	 *        The CHS id (name part) to use.
 	 * @param name
 	 *        The display name.
 	 * @param application
 	 * @param context
 	 * @param prefix
 	 * @param onConflict
 	 * @param type
 	 * @param body
 	 * @param size
 	 * @param makeThumb
 	 * @param altRef
 	 * @return
 	 */
 	protected Reference addAttachment(String idName, String name, String application, String context, String prefix,
 			NameConflictResolution onConflict, String type, byte[] body, long size, boolean makeThumb, String altRef)
 	{
 		String id = contentHostingId(idName, application, context, prefix, (onConflict == NameConflictResolution.alwaysUseFolder), altRef);
 		Reference rv = doAdd(id, name, type, body, size, false, (onConflict == NameConflictResolution.rename), altRef);
 
 		// if this failed and we need to fall back to using a folder, try again
 		if ((rv == null) && (onConflict == NameConflictResolution.useFolder))
 		{
 			id = contentHostingId(idName, application, context, prefix, true, altRef);
 			rv = doAdd(id, name, type, body, size, false, false, altRef);
 		}
 
 		// if we have not added one, and we are to use the one we have
 		if (rv == null)
 		{
 			if (onConflict == NameConflictResolution.keepExisting)
 			{
 				try
 				{
 					// get the existing
 					ContentResource existing = this.contentHostingService.getResource(id);
 
 					// use the alternate reference
 					String ref = existing.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
 					rv = entityManager.newReference(ref);
 				}
 				catch (PermissionException e)
 				{
 					M_log.warn("addAttachment: " + e);
 				}
 				catch (IdUnusedException e)
 				{
 					M_log.warn("addAttachment: " + e);
 				}
 				catch (TypeException e)
 				{
 					M_log.warn("addAttachment: " + e);
 				}
 			}
 		}
 
 		// if we added one
 		else
 		{
 			// if we want thumbs
 			if (makeThumb)
 			{
 				// if it is an image
 				if (type.toLowerCase().startsWith("image/"))
 				{
 					addThumb(rv, body, altRef);
 				}
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Add a thumbnail for the image at this reference with this name and contents.
 	 * 
 	 * @param resource
 	 *        The image resource reference.
 	 * @param body
 	 *        The image bytes.
 	 * @param altRef
 	 *        The alternate Reference for the resource.
 	 * @return A reference to the thumbnail, or null if not made.
 	 */
 	protected Reference addThumb(Reference resource, byte[] body, String altRef)
 	{
 		// if disabled
 		if (!this.makeThumbs) return null;
 
 		// base the thumb name on the resource name
 		String[] parts = StringUtil.split(resource.getId(), "/");
 		String thumbName = parts[parts.length - 1] + THUMB_SUFFIX;
 		Reference ref = this.getReference(resource.getId());
 		String thumbId = ref.getId() + THUMB_SUFFIX;
 		try
 		{
 			byte[] thumb = makeThumb(body, 80, 80, 0.75f);
 			Reference thumbRef = doAdd(thumbId, thumbName, "image/jpeg", thumb, thumb.length, true, false, altRef);
 
 			return thumbRef;
 		}
 		catch (IOException e)
 		{
 			M_log.warn("addAttachment: thumbing: " + e.toString());
 		}
 		catch (InterruptedException e)
 		{
 			M_log.warn("addAttachment: thumbing: " + e.toString());
 		}
 
 		return null;
 	}
 
 	/**
 	 * If the ref is relative, reconstruct the full ref based on where it was embedded.
 	 * 
 	 * @param ref
 	 *        The reference string.
 	 * @param parentRef
 	 *        The reference to the resource in which ref is embedded.
 	 * @return The ref string unmodified if not relative, or the full reference to the resource if relative.
 	 */
 	protected String adjustRelativeReference(String ref, String parentRef)
 	{
 		// if no transport, not a "mailto:", and it does not start with "/", it is a relative reference
 		if ((parentRef != null) && (ref != null) && (parentRef.length() > 0) && (ref.length() > 0) && (ref.indexOf("://") == -1)
 				&& (!(ref.startsWith("/")) && (!(ref.toLowerCase().startsWith("mailto:")))))
 		{
 			// replace the part after the last "/" in the parentRef with the ref
 			int pos = parentRef.lastIndexOf('/');
 			if (pos != -1)
 			{
 				String fullRef = "/access" + parentRef.substring(0, pos + 1) + ref;
 				return fullRef;
 			}
 		}
 
 		return ref;
 	}
 
 	/**
 	 * Assure that a collection with this name exists in the container collection: create it if it is missing.
 	 * 
 	 * @param container
 	 *        The full path of the container collection.
 	 * @param name
 	 *        The collection name to check and create (no trailing slash needed).
 	 * @param uniqueHolder
 	 *        true if the folder is being created solely to hold the attachment uniquely.
 	 * @param altRef
 	 *        the alternate reference for the resource.
 	 */
 	protected void assureCollection(String container, String name, boolean uniqueHolder, String altRef)
 	{
 		try
 		{
 			contentHostingService.getCollection(container + name + "/");
 		}
 		catch (IdUnusedException e)
 		{
 			try
 			{
 				ContentCollectionEdit edit = contentHostingService.addCollection(container + name + "/");
 				ResourcePropertiesEdit props = edit.getPropertiesEdit();
 
 				// set the alternate reference root so we get all requests
 				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, altRef);
 
 				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 
 				// mark it if it is a unique holder
 				if (uniqueHolder)
 				{
 					props.addProperty(PROP_UNIQUE_HOLDER, PROP_UNIQUE_HOLDER);
 				}
 
 				contentHostingService.commitCollection(edit);
 			}
 			catch (IdUsedException e2)
 			{
 				// M_log.warn("init: creating our root collection: " + e2.toString());
 			}
 			catch (IdInvalidException e2)
 			{
 				M_log.warn("assureCollection: " + e2.toString());
 			}
 			catch (PermissionException e2)
 			{
 				M_log.warn("assureCollection: " + e2.toString());
 			}
 			catch (InconsistentException e2)
 			{
 				M_log.warn("assureCollection: " + e2.toString());
 			}
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("assureCollection(2): " + e.toString());
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("assureCollection(2): " + e.toString());
 		}
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
 		String userId = this.sessionManager.getCurrentSessionUserId();
 		String context = ref.getContext();
 
 		// for download
 		if (DOWNLOAD.equals(ref.getContainer()))
 		{
 			// TODO: if students can download anything, check the sub-type
 			// if (DOWNLOAD_ALL_SUBMISSIONS_QUESTION.equals(ref.getSubType()))
 
 			// manage or grade permission for the context
 			if (this.securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context)) return true;
 			if (this.securityService.checkSecurity(userId, MnemeService.GRADE_PERMISSION, context)) return true;
 
 			return false;
 		}
 
 		// for private docs
 		else
 		{
 			String[] parts = StringUtil.split(ref.getId(), "/");
 			if ((parts.length == 9) && (SUBMISSIONS_AREA.equals(parts[5])))
 			{
 				// manage or grade permission for the context is still a winner
 				if (this.securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context)) return true;
 				if (this.securityService.checkSecurity(userId, MnemeService.GRADE_PERMISSION, context)) return true;
 
 				// otherwise, user must be submission user and have submit permission
 				if (this.securityService.checkSecurity(userId, MnemeService.SUBMIT_PERMISSION, context))
 				{
 					Submission submission = this.submissionService.getSubmission(parts[6]);
 					if (submission != null)
 					{
 						if (submission.getUserId().equals(userId))
 						{
 							return true;
 						}
 					}
 				}
 
 				return false;
 			}
 
 			// this is how we used to do it, without a submission id in the ref
 			if (this.securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context)) return true;
 			if (this.securityService.checkSecurity(userId, MnemeService.SUBMIT_PERMISSION, context)) return true;
 			return false;
 		}
 	}
 
 	/**
 	 * Compute the content hosting id.
 	 * 
 	 * @param name
 	 *        The file name.
 	 * @param application
 	 *        The application prefix for the collection in private.
 	 * @param context
 	 *        The context associated with the attachment.
 	 * @param prefix
 	 *        Any prefix path for within the context are of the application in private.
 	 * @param uniqueHolder
 	 *        If true, a uniquely named folder is created to hold the resource.
 	 * @param altRef
 	 *        the alternate reference for the resource.
 	 * @return
 	 */
 	protected String contentHostingId(String name, String application, String context, String prefix, boolean uniqueHolder, String altRef)
 	{
 		// form the content hosting path, and make sure all the folders exist
 		String contentPath = "/private/";
 		assureCollection(contentPath, application, false, altRef);
 		contentPath += application + "/";
 		assureCollection(contentPath, context, false, altRef);
 		contentPath += context + "/";
 		if ((prefix != null) && (prefix.length() > 0))
 		{
 			// allow multi-part prefix
 			if (prefix.indexOf('/') != -1)
 			{
 				String[] prefixes = StringUtil.split(prefix, "/");
 				for (String pre : prefixes)
 				{
 					assureCollection(contentPath, pre, false, altRef);
 					contentPath += pre + "/";
 				}
 			}
 			else
 			{
 				assureCollection(contentPath, prefix, false, altRef);
 				contentPath += prefix + "/";
 			}
 		}
 		if (uniqueHolder)
 		{
 			String uuid = this.idManager.createUuid();
 			assureCollection(contentPath, uuid, true, altRef);
 			contentPath += uuid + "/";
 		}
 
 		contentPath += name;
 
 		return contentPath;
 	}
 
 	/**
 	 * Decode the URL as a browser would.
 	 * 
 	 * @param url
 	 *        The URL.
 	 * @return the decoded URL.
 	 */
 	protected String decodeUrl(String url)
 	{
 		try
 		{
 			// these the browser will convert when it's making the URL to send
 			String processed = url.replaceAll("&amp;", "&");
 			processed = processed.replaceAll("&lt;", "<");
 			processed = processed.replaceAll("&gt;", ">");
 			processed = processed.replaceAll("&quot;", "\"");
 
 			// if a browser sees a plus, it sends a plus (URLDecoder will change it to a space)
 			processed = processed.replaceAll("\\+", "%2b");
 
 			// and the rest of the works, including %20 and + handling
 			String decoded = URLDecoder.decode(processed, "UTF-8");
 
 			return decoded;
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			M_log.warn("decodeUrl: " + e);
 		}
 		catch (IllegalArgumentException e)
 		{
 			M_log.warn("decodeUrl: " + e);
 		}
 
 		return url;
 	}
 
 	/**
 	 * Perform the add.
 	 * 
 	 * @param id
 	 *        The content hosting id.
 	 * @param name
 	 *        The display name.
 	 * @param type
 	 *        The mime type.
 	 * @param body
 	 *        The body bytes.
 	 * @param size
 	 *        The body size.
 	 * @param thumb
 	 *        If true, mark this as a thumb.
 	 * @param renameToFit
 	 *        If true, let CHS rename the file on name conflict.
 	 * @param altRef
 	 *        the alternate reference for the resource.
 	 * @return The Reference to the added attachment.
 	 */
 	protected Reference doAdd(String id, String name, String type, byte[] body, long size, boolean thumb, boolean renameToFit, String altRef)
 	{
 		try
 		{
 			if (!renameToFit)
 			{
 				ContentResourceEdit edit = this.contentHostingService.addResource(id);
 				edit.setContent(body);
 				edit.setContentType(type);
 				ResourcePropertiesEdit props = edit.getPropertiesEdit();
 
 				// set the alternate reference root so we get all requests TODO:
 				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, altRef);
 
 				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 
 				if (thumb)
 				{
 					props.addProperty(PROP_THUMB, PROP_THUMB);
 				}
 
 				this.contentHostingService.commitResource(edit);
 
 				String ref = edit.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
 				Reference reference = entityManager.newReference(ref);
 
 				return reference;
 			}
 			else
 			{
 				String[] parts = StringUtil.split(id, "/");
 				String destinationCollection = StringUtil.unsplit(parts, 0, parts.length - 1, "/") + "/";
 				String destinationName = parts[parts.length - 1];
 
 				// set the alternate reference root so we get all requests
 				ResourcePropertiesEdit props = this.contentHostingService.newResourceProperties();
 				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, altRef);
 
 				// mark if a thumb
 				if (thumb)
 				{
 					props.addProperty(PROP_THUMB, PROP_THUMB);
 				}
 
 				ContentResource uploadedResource = this.contentHostingService.addResource(destinationName, destinationCollection, 255, type, body,
 						props, 0);
 
 				// need to set the display name based on the id that it got
 				ContentResourceEdit edit = this.contentHostingService.editResource(uploadedResource.getId());
 				props = edit.getPropertiesEdit();
 				parts = edit.getId().split("/");
 				destinationName = parts[parts.length - 1];
 				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, destinationName);
 				this.contentHostingService.commitResource(edit);
 
 				String ref = uploadedResource.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
 				Reference reference = entityManager.newReference(ref);
 
 				return reference;
 			}
 		}
 		catch (PermissionException e2)
 		{
 			M_log.warn("addAttachment: creating our content: " + e2.toString());
 		}
 		catch (IdUsedException e2)
 		{
 		}
 		catch (IdInvalidException e2)
 		{
 			M_log.warn("addAttachment: creating our content: " + e2.toString());
 		}
 		catch (InconsistentException e2)
 		{
 			M_log.warn("addAttachment: creating our content: " + e2.toString());
 		}
 		catch (ServerOverloadException e2)
 		{
 			M_log.warn("addAttachment: creating our content: " + e2.toString());
 		}
 		catch (OverQuotaException e2)
 		{
 			M_log.warn("addAttachment: creating our content: " + e2.toString());
 		}
 		catch (IdUniquenessException e)
 		{
 		}
 		catch (IdLengthException e)
 		{
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("addAttachment: creating our content: " + e.toString());
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("addAttachment: creating our content: " + e.toString());
 		}
 		catch (InUseException e)
 		{
 			M_log.warn("addAttachment: creating our content: " + e.toString());
 		}
 		finally
 		{
 			// try
 			// {
 			// // TODO: if using input stream
 			// if (body != null) body.close();
 			// }
 			// catch (IOException e)
 			// {
 			// }
 		}
 
 		return null;
 	}
 
 	/**
 	 * If the document matches typePrefix and thumbnail requirement, add it to the attachments.
 	 * 
 	 * @param attachments
 	 *        The list of attachments.
 	 * @param doc
 	 *        The resource.
 	 * @param typePrefix
 	 *        if null, match any type, else match only the types that match this prefix.
 	 * @param thumbs
 	 *        if true, include only thumbs, else skip them.
 	 */
 	protected void filterTypes(List<Attachment> attachments, ContentResource doc, String typePrefix, boolean thumbs)
 	{
 		// only matching types
 		if ((typePrefix == null) || (doc.getContentType().toLowerCase().startsWith(typePrefix.toLowerCase())))
 		{
 			// thumbs? not thumbs?
 			if ((doc.getProperties().getProperty(this.PROP_THUMB) != null) == thumbs)
 			{
 				String ref = doc.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
 				String url = doc.getUrl(ContentHostingService.PROP_ALTERNATE_REFERENCE);
 
 				// strip the leading transport, server DNS, port, etc, to make a root-relative URL
 				int pos = url.indexOf("/access");
 				if (pos != -1) url = url.substring(pos);
 
 				String escapedUrl = EscapeRefUrl.escapeRefUrl(ref, url);
 
 				Attachment a = new AttachmentImpl(doc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME), ref, escapedUrl, doc
 						.getContentType());
 				attachments.add(a);
 			}
 		}
 	}
 
 	/**
 	 * Find all the attachments in the docs area of the application for this context. Select only those matching type.
 	 * 
 	 * @param application
 	 *        The application prefix for the collection in private.
 	 * @param context
 	 *        The context associated with the attachment.
 	 * @param prefix
 	 *        Any prefix path for within the context are of the application in private.
 	 * @param typePrefix
 	 *        if null, all but the thumbs. Otherwise only those matching the prefix in mime type.
 	 * @param thumbs
 	 *        if true, return only thumbs, else return no thumbs.
 	 * @return A List of Attachments to the attachments.
 	 */
 	protected List<Attachment> findTypes(String application, String context, String prefix, String typePrefix, boolean thumbs)
 	{
 		// permission
 		pushAdvisor();
 
 		List<Attachment> rv = new ArrayList<Attachment>();
 
 		try
 		{
 			// form the content hosting path to the docs collection
 			String docsCollection = "/private/";
 			docsCollection += application + "/";
 			docsCollection += context + "/";
 			if ((prefix != null) && (prefix.length() > 0))
 			{
 				docsCollection += prefix + "/";
 			}
 
 			// get the members of this collection
 			ContentCollection docs = contentHostingService.getCollection(docsCollection);
 			List<Object> members = docs.getMemberResources();
 			for (Object m : members)
 			{
 				if (m instanceof ContentCollection)
 				{
 					// get the member within
 					ContentCollection holder = (ContentCollection) m;
 					List<Object> innerMembers = holder.getMemberResources();
 					for (Object mm : innerMembers)
 					{
 						if (mm instanceof ContentResource)
 						{
 							filterTypes(rv, (ContentResource) mm, typePrefix, thumbs);
 						}
 					}
 				}
 
 				else if (m instanceof ContentResource)
 				{
 					filterTypes(rv, (ContentResource) m, typePrefix, thumbs);
 				}
 			}
 		}
 		catch (IdUnusedException e)
 		{
 		}
 		catch (TypeException e)
 		{
 		}
 		catch (PermissionException e)
 		{
 		}
 		finally
 		{
 			popAdvisor();
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Get a CHS document's mime type.
 	 * 
 	 * @param ref
 	 *        The document reference.
 	 * @return The document's mime type.
 	 */
 	protected String getReferencedDocumentType(String ref)
 	{
 		// bypass security when reading the resource to copy
 		pushAdvisor();
 
 		try
 		{
 			// get an id from the reference string
 			Reference reference = this.entityManager.newReference(ref);
 			String id = reference.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			try
 			{
 				// read the resource
 				ContentResource r = this.contentHostingService.getResource(id);
 				String type = r.getContentType();
 
 				return type;
 			}
 			catch (IdUnusedException e)
 			{
 			}
 			catch (TypeException e)
 			{
 				M_log.warn("getReferencedDocumentType: " + e.toString());
 			}
 			catch (PermissionException e)
 			{
 				M_log.warn("getReferencedDocumentType: " + e.toString());
 			}
 		}
 		finally
 		{
 			popAdvisor();
 		}
 
 		return "";
 	}
 
 	/**
 	 * Process the access request for a download (not CHS private docs).
 	 * 
 	 * @param req
 	 * @param res
 	 * @param ref
 	 * @param copyrightAcceptedRefs
 	 * @throws PermissionException
 	 * @throws IdUnusedException
 	 * @throws ServerOverloadException
 	 * @throws CopyrightException
 	 */
 	protected void handleAccessDownload(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
 			throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
 	{
 		if (DOWNLOAD_ALL_SUBMISSIONS_QUESTION.equals(ref.getSubType()))
 		{
 			// the file name has assessment_question.zip
 			String[] outerParts = StringUtil.split(ref.getId(), ".");
 			String[] parts = StringUtil.split(outerParts[0], "_");
 
 			// assessment
 			Assessment assessment = this.assessmentService.getAssessment(parts[0]);
 			if (assessment == null)
 			{
 				M_log.warn("handleAccessDownload: invalid assessment id: " + parts[0]);
 				return;
 			}
 
 			// question
 			Question question = assessment.getParts().getQuestion(parts[1]);
 			if (question == null)
 			{
 				M_log.warn("handleAccessDownload: invalid question id: " + parts[1]);
 				return;
 			}
 
 			String contentType = (String) ref.getProperties().get(ResourceProperties.PROP_CONTENT_TYPE);
 			String disposition = "attachment; filename=\"" + (String) ref.getProperties().get("DAV:displayname") + "\"";
 
 			OutputStream out = null;
 			ZipOutputStream zip = null;
 			try
 			{
 				res.setContentType(contentType);
 				res.addHeader("Content-Disposition", disposition);
 
 				out = res.getOutputStream();
 				zip = new ZipOutputStream(out);
 
 				this.submissionService.zipSubmissionsQuestion(zip, assessment, question);
 
 				zip.flush();
 			}
 			catch (Throwable e)
 			{
				M_log.warn("zipping: ", e);
 			}
 			finally
 			{
 				if (zip != null)
 				{
 					try
 					{
 						zip.close();
 					}
 					catch (Throwable e)
 					{
 						M_log.warn("closing zip: " + e.toString());
 					}
 				}
 			}
 
 			// track event
 			this.eventTrackingService.post(this.eventTrackingService.newEvent(MnemeService.DOWNLOAD_SQ, ref.getReference(), false));
 		}
 		else
 		{
 			M_log.warn("handleAccessDownload: unknown request: " + ref.getReference());
 		}
 	}
 
 	/**
 	 * Collect all the attachment references in the html data:<br />
 	 * Anything referenced by a src= or href=. in our content docs, or in a site content area <br />
 	 * Ignore anything in a myWorkspace content area or the public content area. <br />
 	 * 
 	 * @param data
 	 *        The data string.
 	 * @param normalize
 	 *        if true, decode the references by URL decoding rules.
 	 * @param parentRef
 	 *        Reference string to the embedding (parent) resource - used to resolve relative references.
 	 * @return The set of attachment references.
 	 */
 	protected Set<String> harvestAttachmentsReferenced(String data, boolean normalize, String parentRef)
 	{
 		Set<String> rv = new HashSet<String>();
 		if (data == null) return rv;
 
 		// pattern to find any src= or href= text
 		// groups: 0: the whole matching text 1: src|href 2: the string in the quotes 3: the terminator character
 		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
 
 		Matcher m = p.matcher(data);
 		while (m.find())
 		{
 			if (m.groupCount() == 3)
 			{
 				String ref = m.group(2);
 
 				if (ref != null) ref = ref.trim();
 
 				// expand to a full reference if relative
 				ref = adjustRelativeReference(ref, parentRef);
 
 				// harvest any content hosting reference
 				int index = ref.indexOf("/access/content/");
 				if (index != -1)
 				{
 					// except for any in /user/ or /public/
 					if (ref.indexOf("/access/content/user/") != -1)
 					{
 						index = -1;
 					}
 					else if (ref.indexOf("/access/content/public/") != -1)
 					{
 						index = -1;
 					}
 				}
 
 				// harvest also the mneme docs references
 				if (index == -1) index = ref.indexOf("/access/mneme/content/");
 
 				// TODO: further filter to docs root and context (optional)
 				if (index != -1)
 				{
 					// save just the reference part (i.e. after the /access);
 					String refString = ref.substring(index + 7);
 
 					// deal with %20 and other encoded URL stuff
 					if (normalize)
 					{
 						refString = decodeUrl(refString);
 					}
 
 					rv.add(refString);
 				}
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Create a thumbnail image from the full image in the byte[], of the desired width and height and quality, preserving aspect ratio.
 	 * 
 	 * @param full
 	 *        The full image bytes.
 	 * @param width
 	 *        The desired max width (pixels).
 	 * @param height
 	 *        The desired max height (pixels).
 	 * @param quality
 	 *        The JPEG quality (0 - 1).
 	 * @return The thumbnail JPEG as a byte[].
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	protected byte[] makeThumb(byte[] full, int width, int height, float quality) throws IOException, InterruptedException
 	{
 		// read the image from the byte array, waiting till it's processed
 		Image fullImage = Toolkit.getDefaultToolkit().createImage(full);
 		MediaTracker tracker = new MediaTracker(new Container());
 		tracker.addImage(fullImage, 0);
 		tracker.waitForID(0);
 
 		// get the full image dimensions
 		int fullWidth = fullImage.getWidth(null);
 		int fullHeight = fullImage.getHeight(null);
 
 		// preserve the aspect of the full image, not exceeding the thumb dimensions
 		if (fullWidth > fullHeight)
 		{
 			// full width will take the full desired width, set the appropriate height
 			height = (int) ((((float) width) / ((float) fullWidth)) * ((float) fullHeight));
 		}
 		else
 		{
 			// full height will take the full desired height, set the appropriate width
 			width = (int) ((((float) height) / ((float) fullHeight)) * ((float) fullWidth));
 		}
 
 		// draw the scaled thumb
 		BufferedImage thumbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2D = thumbImage.createGraphics();
 		g2D.drawImage(fullImage, 0, 0, width, height, null);
 
 		// encode as jpeg to a byte array
 		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 		BufferedOutputStream out = new BufferedOutputStream(byteStream);
 		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
 		param.setQuality(quality, false);
 		encoder.setJPEGEncodeParam(param);
 		encoder.encode(thumbImage);
 		out.close();
 		byte[] thumb = byteStream.toByteArray();
 
 		return thumb;
 	}
 
 	/**
 	 * Trim the name to only the characters after the last slash of either kind.<br />
 	 * Remove junk from uploaded file names.
 	 * 
 	 * @param name
 	 *        The string to trim.
 	 * @return The trimmed string.
 	 */
 	protected String massageName(String name)
 	{
 		// if there are any slashes, forward or back, take from the last one found to the right as the name
 		int pos = -1;
 		for (int i = name.length() - 1; i >= 0; i--)
 		{
 			char c = name.charAt(i);
 			if ((c == '/') || (c == '\\'))
 			{
 				pos = i + 1;
 				break;
 			}
 		}
 
 		if (pos != -1)
 		{
 			name = name.substring(pos);
 		}
 
 		return name;
 	}
 
 	/**
 	 * Remove our security advisor.
 	 */
 	protected void popAdvisor()
 	{
 		securityServiceSakai.popAdvisor();
 	}
 
 	/**
 	 * Setup a security advisor.
 	 */
 	protected void pushAdvisor()
 	{
 		// setup a security advisor
 		securityServiceSakai.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 	}
 
 	/**
 	 * Read a document from content hosting.
 	 * 
 	 * @param ref
 	 *        The document reference.
 	 * @return The document content in a String.
 	 */
 	protected String readReferencedDocument(String ref)
 	{
 		// bypass security when reading the resource to copy
 		pushAdvisor();
 
 		try
 		{
 			// get an id from the reference string
 			Reference reference = this.entityManager.newReference(ref);
 			String id = reference.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			try
 			{
 				// read the resource
 				ContentResource r = this.contentHostingService.getResource(id);
 
 				// get the body into a string
 				byte[] body = r.getContent();
 				if (body == null) return null;
 
 				String bodyString = new String(body, "UTF-8");
 
 				return bodyString;
 			}
 			catch (IOException e)
 			{
 				M_log.warn("readReferencedDocument: " + e.toString());
 			}
 			catch (IdUnusedException e)
 			{
 			}
 			catch (TypeException e)
 			{
 				M_log.warn("readReferencedDocument: " + e.toString());
 			}
 			catch (PermissionException e)
 			{
 				M_log.warn("readReferencedDocument: " + e.toString());
 			}
 			catch (ServerOverloadException e)
 			{
 				M_log.warn("readReferencedDocument: " + e.toString());
 			}
 		}
 		finally
 		{
 			popAdvisor();
 		}
 
 		return "";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected String translateEmbeddedReferences(String data, Collection<Translation> translations, String parentRef)
 	{
 		if (data == null) return data;
 		if (translations == null) return data;
 
 		// pattern to find any src= or href= text
 		// groups: 0: the whole matching text 1: src|href 2: the string in the quotes 3: the terminator character
 		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
 
 		Matcher m = p.matcher(data);
 		StringBuffer sb = new StringBuffer();
 
 		// process each "harvested" string (avoiding like strings that are not in src= or href= patterns)
 		while (m.find())
 		{
 			if (m.groupCount() == 3)
 			{
 				String ref = m.group(2);
 				String terminator = m.group(3);
 
 				if (ref != null) ref = ref.trim();
 
 				// expand to a full reference if relative
 				ref = adjustRelativeReference(ref, parentRef);
 
 				// harvest any content hosting reference
 				int index = ref.indexOf("/access/content/");
 				if (index != -1)
 				{
 					// except for any in /user/ or /public/
 					if (ref.indexOf("/access/content/user/") != -1)
 					{
 						index = -1;
 					}
 					else if (ref.indexOf("/access/content/public/") != -1)
 					{
 						index = -1;
 					}
 				}
 
 				// harvest also the mneme docs references
 				if (index == -1) index = ref.indexOf("/access/mneme/content/");
 
 				if (index != -1)
 				{
 					// save just the reference part (i.e. after the /access);
 					String normal = ref.substring(index + 7);
 
 					// deal with %20, &amp;, and other encoded URL stuff
 					normal = decodeUrl(normal);
 
 					// translate the normal form
 					String translated = normal;
 					for (Translation translation : translations)
 					{
 						translated = translation.translate(translated);
 					}
 
 					// URL encode translated
 					String escaped = EscapeRefUrl.escapeUrl(translated);
 
 					// if changed, replace
 					if (!normal.equals(translated))
 					{
 						m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "=\"" + ref.substring(0, index + 7) + escaped + terminator));
 					}
 				}
 			}
 		}
 
 		m.appendTail(sb);
 
 		return sb.toString();
 	}
 
 	/**
 	 * Translate the resource's body html with the translations.
 	 * 
 	 * @param ref
 	 *        The resource reference.
 	 * @param translations
 	 *        The complete set of translations.
 	 * @param context
 	 *        The context.
 	 */
 	protected void translateHtmlBody(Reference ref, Collection<Translation> translations, String context)
 	{
 		// ref is the destination ("to" in the translations) resource - we need the "parent ref" from the source ("from" in the translations) resource
 		String parentRef = ref.getReference();
 		for (Translation translation : translations)
 		{
 			parentRef = translation.reverseTranslate(parentRef);
 		}
 
 		// bypass security when reading the resource to copy
 		pushAdvisor();
 
 		try
 		{
 			// Reference does not know how to make the id from a private docs reference.
 			String id = ref.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			// get the resource
 			ContentResource resource = this.contentHostingService.getResource(id);
 			String type = resource.getContentType();
 
 			// translate if we are html
 			if (type.equals("text/html"))
 			{
 				byte[] body = resource.getContent();
 				if (body != null)
 				{
 					String bodyString = new String(body, "UTF-8");
 					String translated = translateEmbeddedReferences(bodyString, translations, parentRef);
 					body = translated.getBytes("UTF-8");
 
 					ContentResourceEdit edit = this.contentHostingService.editResource(resource.getId());
 					edit.setContent(body);
 					this.contentHostingService.commitResource(edit, 0);
 				}
 			}
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			M_log.warn("translateHtmlBody: " + e.toString());
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("translateHtmlBody: " + e.toString());
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("translateHtmlBody: " + e.toString());
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("translateHtmlBody: " + e.toString());
 		}
 		catch (ServerOverloadException e)
 		{
 			M_log.warn("translateHtmlBody: " + e.toString());
 		}
 		catch (InUseException e)
 		{
 			M_log.warn("translateHtmlBody: " + e.toString());
 		}
 		catch (OverQuotaException e)
 		{
 			M_log.warn("translateHtmlBody: " + e.toString());
 		}
 		finally
 		{
 			popAdvisor();
 		}
 	}
 }
