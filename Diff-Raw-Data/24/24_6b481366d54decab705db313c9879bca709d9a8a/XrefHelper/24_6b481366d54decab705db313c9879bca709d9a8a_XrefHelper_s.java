 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2009 Etudes, Inc.
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
 
 package org.etudes.util;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.util.api.Translation;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.authz.cover.SecurityService;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.content.cover.ContentHostingService;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.entity.cover.EntityManager;
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
 import org.sakaiproject.thread_local.cover.ThreadLocalManager;
 import org.sakaiproject.util.StringUtil;
 
 /**
  * XrefHelper has helper methods for the cross reference fixer handlers
  */
 public class XrefHelper
 {
 	/** A thread-local key to the List of Translations we have made so far in the thread. */
 	public final static String THREAD_TRANSLATIONS_BODY_KEY = "XrefHelper.body.translations";
 
 	/** A thread-local key to the List of Translations we have made so far in the thread. */
 	public final static String THREAD_TRANSLATIONS_KEY = "XrefHelper.translations";
 
 	/** Our log. */
 	private static Log M_log = LogFactory.getLog(XrefHelper.class);
 
 	/**
 	 * Clear all thread local cached info. Use between sites on the same thread.
 	 */
 	public static void clearThreadCaches()
 	{
 		ThreadLocalManager.set(XrefHelper.THREAD_TRANSLATIONS_BODY_KEY, null);
 		ThreadLocalManager.set(XrefHelper.THREAD_TRANSLATIONS_KEY, null);
 	}
 
 	/**
 	 * Get all the resource references embedded in the html that need harvesting because they are from another site.<br />
 	 * If any are html, repeat the process for those to harvest their embedded references.
 	 * 
 	 * @param data
 	 *        The html data.
 	 * @param siteId
 	 *        The destination site id.
 	 * @return The set of reference strings. Note: this is ordered so a dependent resource is after the one it is embedded in.
 	 */
 	public static Set<String> harvestEmbeddedReferences(String data, String siteId)
 	{
 		// return an insertion-ordered set
 		Set<String> rv = new LinkedHashSet<String>();
 		if (data == null) return rv;
 
 		// harvest the main data
 		rv.addAll(harvestEmbeddedReferencesIn(data, siteId, null));
 
 		// process a set of references - initially the main data's references
 		Set<String> process = new LinkedHashSet<String>();
 		process.addAll(rv);
 
 		while (!process.isEmpty())
 		{
 			Set<String> secondary = new LinkedHashSet<String>();
 			for (String ref : process)
 			{
 				// check for any html
 				String type = readReferencedDocumentType(ref);
 				if ("text/html".equals(type))
 				{
 					// read the referenced html
 					String secondaryData = readReferencedDocument(ref);
 
 					// harvest it
 					secondary.addAll(harvestEmbeddedReferencesIn(secondaryData, siteId, ref));
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
 	 * For this existing resource, harvest any embedded references into the site, translating them and the resource.
 	 * 
 	 * @param resource
 	 *        The CHS resource.
 	 * @param siteId
 	 *        The site in which the resource is to live.
 	 * @param tool
 	 *        The tool identifier.
 	 */
 	public static void harvestTranslateResource(ContentResource resource, String siteId, String tool)
 	{
 		// skip if not html
 		if (!"text/html".equals(resource.getContentType())) return;
 
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		try
 		{
 			// get the body
 			byte[] body = resource.getContent();
 			if (body != null)
 			{
 				String bodyString = new String(body, "UTF-8");
 
 				// get all embedded references, deep
 				Set<String> embeddedReferences = harvestEmbeddedReferences(bodyString, siteId);
 
 				// import the embedded references
 				List<Translation> translations = XrefHelper.importTranslateResources(embeddedReferences, siteId, tool);
 
 				// translate the resource
 				String newBodyString = translateEmbeddedReferences(bodyString, translations, siteId);
 				body = newBodyString.getBytes("UTF-8");
 
 				// update the resource
 				ContentResourceEdit edit = ContentHostingService.editResource(resource.getId());
 				edit.setContent(body);
 				ContentHostingService.commitResource(edit, 0);
 			}
 		}
 		catch (ServerOverloadException e)
 		{
 			M_log.warn("harvestTranslateResource: " + e);
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			M_log.warn("harvestTranslateResource: " + e);
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("harvestTranslateResource: " + e);
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("harvestTranslateResource: " + e);
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("harvestTranslateResource: " + e);
 		}
 		catch (InUseException e)
 		{
 			M_log.warn("harvestTranslateResource: " + e);
 		}
 		catch (OverQuotaException e)
 		{
 			M_log.warn("harvestTranslateResource: " + e);
 		}
 		finally
 		{
 			SecurityService.popAdvisor();
 		}
 
 	}
 
 	/**
 	 * Assure that all the referenced resources are imported or found in the context.
 	 * 
 	 * @param refs
 	 *        The set of references to embedded documents.
 	 * @param context
 	 *        The destination context.
 	 * @param tool
 	 *        the tool id (used as part of the attachment area resource name).
 	 * @return a Translation list for each embedded document to its location in this context.
 	 */
 	public static List<Translation> importResources(Set<String> refs, String context, String tool)
 	{
 		return importResources(refs, context, tool, false, false);
 	}
 
 	/**
 	 * Assure that all the referenced resources are imported or found in the context; translate any embedded references in html resources imported.
 	 * 
 	 * @param refs
 	 *        The set of references to embedded documents.
 	 * @param context
 	 *        The destination context.
 	 * @param tool
 	 *        the tool id (used as part of the attachment area resource name).
 	 * @return a Translation list for each embedded document to its location in this context.
 	 */
 	public static List<Translation> importTranslateResources(Set<String> refs, String context, String tool)
 	{
 		return importResources(refs, context, tool, true, false);
 	}
 
 	/**
 	 * Properly lower case a CHS reference.
 	 * 
 	 * @param ref
 	 *        The reference.
 	 * @return The lower cased reference.
 	 */
 	public static String lowerCase(String ref)
 	{
 		// if we start with /content/private/meleteDocs, or /meleteDocs/content/private/meleteDocs, we need the capital D
 		// - everything else can be lowered
 		String rv = ref.toLowerCase();
 
 		if (rv.startsWith("/content/private/meletedocs"))
 		{
 			rv = "/content/private/meleteDocs" + rv.substring("/content/private/meletedocs".length());
 		}
 		else if (rv.startsWith("/meletedocs/content/private/meletedocs"))
 		{
 			rv = "/meleteDocs/content/private/meleteDocs" + rv.substring("/meletedocs/content/private/meletedocs".length());
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Replace any full URL references that include the server DNS, port, etc, with a root-relative one (i.e. starting with "/access" or "/library" or whatever)
 	 * 
 	 * @param data
 	 *        the html data.
 	 * @return The shortened data.
 	 */
 	public static String shortenFullUrls(String data)
 	{
 		if (data == null) return data;
 
 		Pattern p = getPattern();
 		Matcher m = p.matcher(data);
 		StringBuffer sb = new StringBuffer();
 
 		// for the relative access check: matches ..\..\access\ etc with any number of leading "../"
 		Pattern relAccessPattern = Pattern.compile("^(../)+(access/.*)");
 
 		// process each "harvested" string (avoiding like strings that are not in src= or href= patterns)
 		while (m.find())
 		{
 			if (m.groupCount() == 2)
 			{
 				String ref = m.group(2);
 
 				// if this is an access to our own server, shorten it to root relative (i.e. starting with "/access")
 				int pos = internallyHostedUrl(ref);
 				if (pos != -1)
 				{
 					ref = ref.substring(pos);
 					m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "=\"" + ref + "\""));
 				}
 
 				// if this is a relative access URL, fix it
 				else
 				{
 					Matcher relAccessMatcher = relAccessPattern.matcher(ref);
 					if (relAccessMatcher.matches())
 					{
 						ref = "/" + relAccessMatcher.group(2);
 						m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "=\"" + ref + "\""));
 					}
 				}
 			}
 		}
 
 		m.appendTail(sb);
 
 		return sb.toString();
 	}
 
 	/**
 	 * Replace any embedded references in the html data with the translated, new references listed in translations.
 	 * 
 	 * @param data
 	 *        the html data.
 	 * @param translations
 	 *        The translations.
 	 * @param siteId
 	 *        The site id.
 	 * @return The translated html data.
 	 */
 	public static String translateEmbeddedReferences(String data, Collection<Translation> translations, String siteId)
 	{
 		return translateEmbeddedReferences(data, translations, siteId, null);
 	}
 
 	/**
 	 * Replace any embedded references in the html data with the translated, new references listed in translations.
 	 * 
 	 * @param data
 	 *        the html data.
 	 * @param translations
 	 *        The translations.
 	 * @param siteId
 	 *        The site id.
 	 * @param parentRef
 	 *        Reference of the resource that has data as body.
 	 * @return The translated html data.
 	 */
 	public static String translateEmbeddedReferences(String data, Collection<Translation> translations, String siteId, String parentRef)
 	{
 		if (data == null) return data;
 		if (translations == null) return data;
 
 		// get our thread-local list of translations made in this thread (if any)
 		List<Translation> threadTranslations = (List<Translation>) ThreadLocalManager.get(THREAD_TRANSLATIONS_KEY);
 
 		Pattern p = getPattern();
 		Matcher m = p.matcher(data);
 		StringBuffer sb = new StringBuffer();
 
 		// process each "harvested" string (avoiding like strings that are not in src= or href= patterns)
 		while (m.find())
 		{
 			if (m.groupCount() == 2)
 			{
 				String ref = m.group(2);
 
 				// expand to a full reference if relative
 				ref = adjustRelativeReference(ref, parentRef);
 
 				// harvest any content hosting reference
 				int index = indexContentReference(ref);
 				if (index != -1)
 				{
 					// except those we don't want to harvest
 					if (exception(ref, siteId)) index = -1;
 				}
 
 				if (index != -1)
 				{
 					// save just the reference part (i.e. after the /access);
 					String normal = ref.substring(index + 7);
 
 					// deal with %20 and other encoded URL stuff
 					try
 					{
 						normal = URLDecoder.decode(normal, "UTF-8");
 					}
 					catch (UnsupportedEncodingException e)
 					{
 						M_log.warn("translateEmbeddedReferences: " + e);
 					}
 					catch (IllegalArgumentException e)
 					{
 						M_log.warn("translateEmbeddedReferences: " + e);
 					}
 
 					// translate the normal form
 					String translated = normal;
 					for (Translation translation : translations)
 					{
 						translated = translation.translate(translated);
 					}
 
 					// also translate with our global list
 					if (threadTranslations != null)
 					{
 						for (Translation translation : threadTranslations)
 						{
 							translated = translation.translate(translated);
 						}
 					}
 
 					// URL encode translated
 					String escaped = escapeUrl(translated);
 
 					// if changed, replace
 					if (!normal.equals(translated))
 					{
 						m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "=\"" + ref.substring(0, index + 7) + escaped + "\""));
 					}
 				}
 			}
 		}
 
 		m.appendTail(sb);
 
 		return sb.toString();
 	}
 
 	/**
 	 * Replace any embedded references in the html data with the translated, new references listed in translations.<br />
 	 * Also replace any references that include the full server DNS with a root-relative (i.e. starting with "/access") one.
 	 * 
 	 * @param data
 	 *        the html data.
 	 * @param translations
 	 *        The translations.
 	 * @param siteId
 	 *        The site id.
 	 * @param parentRef
 	 *        Reference of the resource that has data as body.
 	 * @return The translated html data.
 	 */
 	public static String translateEmbeddedReferencesAndShorten(String data, Collection<Translation> translations, String siteId, String parentRef)
 	{
 		String rv = translateEmbeddedReferences(data, translations, siteId, parentRef);
 		rv = shortenFullUrls(rv);
 
 		return rv;
 	}
 
 	/**
 	 * Translate the resource's body html with the translations. Optionally shorten full URLs.
 	 * 
 	 * @param ref
 	 *        The resource reference.
 	 * @param translations
 	 *        The complete set of translations.
 	 * @param context
 	 *        The context.
 	 * @param shortenUrls
 	 *        if true, full references to resources on this server are shortened.
 	 */
 	public static void translateHtmlBody(Reference ref, Collection<Translation> translations, String context, boolean shortenUrls)
 	{
 		// ref is the destination ("to" in the translations) resource - we need the "parent ref" from the source ("from" in the translations) resource
 		String parentRef = ref.getReference();
 		for (Translation translation : translations)
 		{
 			parentRef = translation.reverseTranslate(parentRef);
 		}
 
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		try
 		{
 			// Reference does not know how to make the id from a private docs reference.
 			String id = ref.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			// get the resource
 			ContentResource resource = ContentHostingService.getResource(id);
 			String type = resource.getContentType();
 
 			// translate if we are html
 			if (type.equals("text/html"))
 			{
 				byte[] body = resource.getContent();
 				if (body != null)
 				{
 					String bodyString = new String(body, "UTF-8");
 
 					String translated = null;
 					if (shortenUrls)
 					{
 						translated = translateEmbeddedReferencesAndShorten(bodyString, translations, context, parentRef);
 					}
 					else
 					{
 						translated = translateEmbeddedReferences(bodyString, translations, context, parentRef);
 					}
 					body = translated.getBytes("UTF-8");
 
 					ContentResourceEdit edit = ContentHostingService.editResource(resource.getId());
 					edit.setContent(body);
 					ContentHostingService.commitResource(edit, 0);
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
 			SecurityService.popAdvisor();
 		}
 	}
 
 	/**
 	 * Add an attachment in content hosting that is a copy of another resource.
 	 * 
 	 * @param resourceRef
 	 *        the source to copy.
 	 * @param context
 	 *        The destination context.
 	 * @param tool
 	 *        The tool id for the attachment area.
 	 * @return A reference to the new resource, or null if it was not created.
 	 */
 	protected static Reference addAttachment(Reference resourceRef, String context, String tool)
 	{
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		try
 		{
 			// Reference does not know how to make the id from a private docs reference.
 			String id = resourceRef.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			ContentResource resource = ContentHostingService.getResource(id);
 			String type = resource.getContentType();
 			long size = resource.getContentLength();
 			byte[] body = resource.getContent();
 			String name = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 
 			// make sure to remove the reference root sakai:reference-root
 			ResourcePropertiesEdit props = ContentHostingService.newResourceProperties();
 			props.addAll(resource.getProperties());
 			props.removeProperty("sakai:reference-root");
 
 			ContentResource attachment = ContentHostingService.addAttachmentResource(name, context, tool, type, body, props);
 
 			String ref = attachment.getReference();
 			Reference reference = EntityManager.newReference(ref);
 
 			return reference;
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
 		catch (IdInvalidException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		catch (InconsistentException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		catch (IdUsedException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		catch (OverQuotaException e)
 		{
 			M_log.warn("addAttachment: " + e.toString());
 		}
 		finally
 		{
 			SecurityService.popAdvisor();
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
 	protected static String adjustRelativeReference(String ref, String parentRef)
 	{
		// if no transport, and it does not start with "/", it is a relative reference
		if ((parentRef != null) && (ref != null) && (ref.indexOf("://") == -1) && (!(ref.startsWith("/"))))
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
 	 * Return a string based on id that is fully escaped using URL rules, using a UTF-8 underlying encoding.
 	 * 
 	 * @param id
 	 *        The string to escape.
 	 * @return id fully escaped using URL rules.
 	 */
 	protected static String escapeUrl(String id)
 	{
 		if (id == null) return "";
 		id = id.trim();
 		try
 		{
 			// convert the string to bytes in UTF-8
 			byte[] bytes = id.getBytes("UTF-8");
 
 			StringBuilder buf = new StringBuilder();
 			for (int i = 0; i < bytes.length; i++)
 			{
 				byte b = bytes[i];
 				if (("$&+,:;=?@ '\"<>#%{}|\\^~[]`^?;".indexOf((char) b) != -1) || (b <= 0x1F) || (b == 0x7F) || (b >= 0x80))
 				{
 					buf.append("%");
 					buf.append(Integer.toString(b, 16));
 				}
 				else
 				{
 					buf.append((char) b);
 				}
 			}
 
 			String rv = buf.toString();
 			return rv;
 		}
 		catch (Exception e)
 		{
 			return id;
 		}
 	}
 
 	/**
 	 * Check for an exception to the harvesting
 	 * 
 	 * @param ref
 	 *        The resource ref.
 	 * @param siteId
 	 *        The site id.
 	 * @return true if this ref should not be harvested, false if it is not an exception.
 	 */
 	protected static boolean exception(String ref, String siteId)
 	{
 		// except for any in /user/ or /public/ or (if we have a site) the site or in the ite's attachment or the site's private docs areas
 		if (ref.indexOf("/access/content/user/") != -1) return true;
 		if (ref.indexOf("/access/content/public/") != -1) return true;
 		if (siteId == null) return false;
 
 		if (ref.indexOf("/access/content/group/" + siteId) != -1) return true;
 		if (ref.indexOf("/access/content/attachment/" + siteId) != -1) return true;
 		if (ref.indexOf("/access/mneme/content/private/mneme/" + siteId) != -1) return true;
 		if (ref.indexOf("/access/meleteDocs/content/private/meleteDocs/" + siteId) != -1) return true;
 
 		return false;
 	}
 
 	/**
 	 * Create the embedded reference detection pattern. It creates three groups: 0 - the entire matc, 1- src|href, 2-the reference.
 	 * 
 	 * @return The Pattern.
 	 */
 	protected static Pattern getPattern()
 	{
 		return Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
 	}
 
 	/**
 	 * Get all the resource references embedded in the html that need harvesting because they are from another site.<br />
 	 * If any are html, repeat the process for those to harvest their embedded references.
 	 * 
 	 * @param data
 	 *        The html data.
 	 * @param siteId
 	 *        The destination site id.
 	 * @param parentRef
 	 *        Reference string to the embedding (parent) resource - used to resolve relative references.
 	 * @return The set of reference strings.
 	 */
 	protected static Set<String> harvestEmbeddedReferencesIn(String data, String siteId, String parentRef)
 	{
 		Set<String> rv = new HashSet<String>();
 		if (data == null) return rv;
 
 		Pattern p = getPattern();
 		Matcher m = p.matcher(data);
 		while (m.find())
 		{
 			if (m.groupCount() == 2)
 			{
 				String ref = m.group(2);
 
 				// expand to a full reference if relative
 				ref = adjustRelativeReference(ref, parentRef);
 
 				// harvest any content hosting reference
 				int index = indexContentReference(ref);
 				if (index != -1)
 				{
 					// except those we don't want to harvest
 					if (exception(ref, siteId)) index = -1;
 				}
 
 				if (index != -1)
 				{
 					// save just the reference part (i.e. after the /access);
 					String refString = ref.substring(index + 7);
 
 					// deal with %20 and other encoded URL stuff
 					try
 					{
 						refString = URLDecoder.decode(refString, "UTF-8");
 					}
 					catch (UnsupportedEncodingException e)
 					{
 						M_log.warn("harvestEmbeddedReferences: " + e);
 					}
 					catch (IllegalArgumentException e)
 					{
 						M_log.warn("harvestEmbeddedReferences: " + e);
 					}
 
 					// normalize to lower case... except for the upper case private areas
 					refString = lowerCase(refString);
 
 					rv.add(refString);
 				}
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Import the references resource into the context in the same relative position; let the name drift a bit to fit in.
 	 * 
 	 * @param resourceRef
 	 *        the source to copy.
 	 * @param context
 	 *        The destination context.
 	 * @return A reference to the new resource, or null if it was not created.
 	 */
 	protected static Reference importResourceAdjustName(Reference resourceRef, String context)
 	{
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		try
 		{
 			// Reference does not know how to make the id from a private docs reference.
 			String id = resourceRef.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			// get the source
 			ContentResource resource = ContentHostingService.getResource(id);
 			String type = resource.getContentType();
 			long size = resource.getContentLength();
 			byte[] body = resource.getContent();
 
 			String[] parts = StringUtil.split(id, "/");
 			String relativePath = null;
 			if (!"group".equals(parts[1]))
 			{
 				// for privateDocs, or an attachment, or anything other than a site's resources area, put it in the root of the new site
 				relativePath = "/";
 			}
 			else
 			{
 				// the relative path and name of the source if from a site's resources area (i.e. /group/<source site>/path/b/c/name)
 				relativePath = "/" + ((parts.length > 4) ? (StringUtil.unsplit(parts, 3, parts.length - 4, "/") + "/") : "");
 			}
 
 			// the new resource collection and name
 			String destinationCollection = "/group/" + context + relativePath;
 			String destinationName = parts[parts.length - 1];
 
 			// make sure to remove the reference root sakai:reference-root
 			ResourcePropertiesEdit props = ContentHostingService.newResourceProperties();
 			props.addAll(resource.getProperties());
 			props.removeProperty("sakai:reference-root");
 
 			ContentResource importedResource = ContentHostingService.addResource(destinationName, destinationCollection, 255, type, body, props, 0);
 
 			// return a reference to it
 			Reference reference = EntityManager.newReference(importedResource.getReference());
 			return reference;
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (ServerOverloadException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdInvalidException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (InconsistentException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (OverQuotaException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdUniquenessException e)
 		{
 		}
 		catch (IdLengthException e)
 		{
 		}
 		finally
 		{
 			SecurityService.popAdvisor();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Import the references resource into the context in the same relative position, unless there is a resource there already (name conflict).
 	 * 
 	 * @param resourceRef
 	 *        the source to copy.
 	 * @param context
 	 *        The destination context.
 	 * @return A reference to the new resource, or if we didn't copy because of a name conflict, a reference to the existing resource that blocked the copy.
 	 */
 	protected static Reference importResourceIfNoConflict(Reference resourceRef, String context)
 	{
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		String destinationCollection = null;
 		String destinationName = null;
 		try
 		{
 			// Reference does not know how to make the id from a private docs reference.
 			String id = resourceRef.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			// get the source
 			ContentResource resource = ContentHostingService.getResource(id);
 			String type = resource.getContentType();
 			long size = resource.getContentLength();
 			byte[] body = resource.getContent();
 
 			String[] parts = StringUtil.split(id, "/");
 			String relativePath = null;
 			if (!"group".equals(parts[1]))
 			{
 				// for privateDocs, or an attachment, or anything other than a site's resources area, put it in the root of the new site
 				relativePath = "/";
 			}
 			else
 			{
 				// the relative path and name of the source if from a site's resources area (i.e. /group/<source site>/path/b/c/name)
 				relativePath = "/" + ((parts.length > 4) ? (StringUtil.unsplit(parts, 3, parts.length - 4, "/") + "/") : "");
 			}
 
 			// the new resource collection and name
 			destinationCollection = "/group/" + context + relativePath;
 			destinationName = parts[parts.length - 1];
 
 			// make sure to remove the reference root sakai:reference-root
 			ResourcePropertiesEdit props = ContentHostingService.newResourceProperties();
 			props.addAll(resource.getProperties());
 			props.removeProperty("sakai:reference-root");
 
 			ContentResource importedResource = ContentHostingService.addResource(destinationCollection + destinationName, type, body, props, 0);
 
 			// return a reference to it
 			Reference reference = EntityManager.newReference(importedResource.getReference());
 			return reference;
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (ServerOverloadException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdInvalidException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (InconsistentException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (OverQuotaException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdUsedException e)
 		{
 			// we have a resource here already, make a reference to it
 			String chsId = destinationCollection + destinationName;
 
 			// return a reference to the existing file
 			Reference reference = EntityManager.newReference(ContentHostingService.getReference(chsId));
 			return reference;
 		}
 		finally
 		{
 			SecurityService.popAdvisor();
 		}
 
 		return null;
 	}
 
 	/**
 	 * If needed, import the references resource into the context in the same relative position. If we find an existing resource that matches, use it.
 	 * 
 	 * @param resourceRef
 	 *        the source to copy.
 	 * @param context
 	 *        The destination context.
 	 * @return A reference to the new or found resource, or null if it was not created.
 	 */
 	protected static Reference importResourceIfNoMatch(Reference resourceRef, String context)
 	{
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		try
 		{
 			// Reference does not know how to make the id from a private docs reference.
 			String id = resourceRef.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			// get the source
 			ContentResource resource = ContentHostingService.getResource(id);
 			String type = resource.getContentType();
 			int size = resource.getContentLength();
 			byte[] body = resource.getContent();
 
 			String[] parts = StringUtil.split(id, "/");
 			String relativePath = null;
 			if (!"group".equals(parts[1]))
 			{
 				// for privateDocs, or an attachment, or anything other than a site's resources area, put it in the root of the new site
 				relativePath = "/";
 			}
 			else
 			{
 				// the relative path and name of the source if from a site's resources area (i.e. /group/<source site>/path/b/c/name)
 				relativePath = "/" + ((parts.length > 4) ? (StringUtil.unsplit(parts, 3, parts.length - 4, "/") + "/") : "");
 			}
 			String fname = parts[parts.length - 1];
 			String extension = null;
 			if (fname.indexOf('.') != -1)
 			{
 				int pos = fname.lastIndexOf('.');
 				extension = fname.substring(pos + 1);
 				fname = fname.substring(0, pos);
 			}
 
 			int maxNum = -1;
 			boolean nameInUse = false;
 
 			// get info on everyone in the destination's relative path collection
 			ContentCollection targetCollection = null;
 			try
 			{
 				targetCollection = ContentHostingService.getCollection("/group/" + context + relativePath);
 				List<Object> members = targetCollection.getMemberResources();
 				for (Object member : members)
 				{
 					// ignore collections
 					if (member instanceof ContentResource)
 					{
 						ContentResource mr = (ContentResource) member;
 
 						// isolate the member's name and extension
 						String mrFname = mr.getId().substring(mr.getId().lastIndexOf('/') + 1, mr.getId().length());
 						String mrExtension = null;
 						if (mrFname.indexOf('.') != -1)
 						{
 							int pos = mrFname.lastIndexOf('.');
 							mrExtension = mrFname.substring(pos + 1);
 							mrFname = mrFname.substring(0, pos);
 						}
 
 						// the extensions have to match
 						if (!StringUtil.different(extension, mrExtension, true))
 						{
 							// the names have to match
 							if (!StringUtil.different(fname, mrFname, true))
 							{
 								// the resource name is in use already
 								nameInUse = true;
 
 								// no extension is the "0" one.
 								int num = 0;
 
 								// keep track of the biggest numeric extension
 								if (num > maxNum) maxNum = num;
 
 								// if the member 'matches' the import, use the member
 								if (matches(mr, type, size, body))
 								{
 									return EntityManager.newReference(mr.getReference());
 								}
 							}
 
 							// if the member name has a "dash-number" extension past the source name, we can still use it if it matches size and type
 							else if (mrFname.startsWith(fname + "-"))
 							{
 								// the rest after the "-" has to be numeric
 								try
 								{
 									int num = Integer.valueOf(mrFname.substring(fname.length() + 1));
 
 									// keep track of the biggest numeric extension
 									if (num > maxNum) maxNum = num;
 
 									// if the member 'matches' the import, use the member
 									if (matches(mr, type, size, body))
 									{
 										return EntityManager.newReference(mr.getReference());
 									}
 								}
 								catch (NumberFormatException e)
 								{
 									// not numeric... ignore
 								}
 							}
 						}
 
 					}
 				}
 			}
 			catch (IdUnusedException e)
 			{
 				// there is no collection at the new site's relative path
 			}
 
 			// there's nothing here to use, so import it
 
 			// adjust the fname with a "dash-number" if needed
 			if ((nameInUse) && (maxNum > -1))
 			{
 				// maxNum is the greatest numeric extension we saw in the collection for this name, so we want the next
 				fname += "-" + Integer.toString(maxNum + 1);
 			}
 
 			// the new resource id
 			String destinationPath = "/group/" + context + relativePath + fname + ((extension == null) ? "" : ("." + extension));
 
 			// make sure to remove the reference root sakai:reference-root
 			ResourcePropertiesEdit props = ContentHostingService.newResourceProperties();
 			props.addAll(resource.getProperties());
 			props.removeProperty("sakai:reference-root");
 
 			// create the new resource
 			ContentResource importedResource = ContentHostingService.addResource(destinationPath, type, body, props, 0);
 
 			// return a reference to it
 			Reference reference = EntityManager.newReference(importedResource.getReference());
 			return reference;
 		}
 		catch (PermissionException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdUnusedException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (TypeException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (ServerOverloadException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdInvalidException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (InconsistentException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (IdUsedException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		catch (OverQuotaException e)
 		{
 			M_log.warn("importResource: " + e.toString());
 		}
 		finally
 		{
 			SecurityService.popAdvisor();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Assure that all the referenced resources are imported or found in the context.
 	 * 
 	 * @param refs
 	 *        The set of references to embedded documents.
 	 * @param context
 	 *        The destination context.
 	 * @param tool
 	 *        the tool id (used as part of the attachment area resource name).
 	 * @param translate
 	 *        if true, translate any embedded references in html resources imported.
 	 * @param shortenUrls
 	 *        if true, full references to resources on this server are shortened.
 	 * @return a Translation list for each embedded document to its location in this context.
 	 */
 	protected static List<Translation> importResources(Set<String> refs, String context, String tool, boolean translate, boolean shortenUrls)
 	{
 		// get our thread-local list of translations made in this thread
 		List<Translation> threadTranslations = (List<Translation>) ThreadLocalManager.get(THREAD_TRANSLATIONS_KEY);
 		if (threadTranslations == null)
 		{
 			threadTranslations = new ArrayList<Translation>();
 			ThreadLocalManager.set(THREAD_TRANSLATIONS_KEY, threadTranslations);
 		}
 
 		// get our thread-local list of bodies translated in this thread
 		List<String> threadBodyTranslations = (List<String>) ThreadLocalManager.get(THREAD_TRANSLATIONS_BODY_KEY);
 		if (threadBodyTranslations == null)
 		{
 			threadBodyTranslations = new ArrayList<String>();
 			ThreadLocalManager.set(THREAD_TRANSLATIONS_BODY_KEY, threadBodyTranslations);
 		}
 
 		// collect any that may need html body translation in a second pass
 		List<Reference> toTranslate = new ArrayList<Reference>();
 
 		List<Translation> rv = new ArrayList<Translation>();
 		for (String refString : refs)
 		{
 			// if we have done this already in the thread, just skip it
 			boolean skip = false;
 			for (Translation imported : threadTranslations)
 			{
 				if (refString.equals(imported.getFrom()))
 				{
 					skip = true;
 					break;
 				}
 			}
 			if (skip) continue;
 
 			Reference ref = EntityManager.newReference(refString);
 
 			// find or import the referenced resource in the context
 			Reference imported = importResourceIfNoConflict(ref, context);
 			if (imported != null)
 			{
 				// make the translation - return it and add it to the thread list
 				Translation t = new TranslationImpl(ref.getReference(), imported.getReference());
 				rv.add(t);
 				threadTranslations.add(t);
 			}
 			else
 			{
 				// if for some reason we could not get the file imported, try a simpler process as an attachment
 				imported = addAttachment(ref, context, tool);
 				if (imported != null)
 				{
 					// make the translation - return it and add it to the thread list
 					Translation t = new TranslationImpl(ref.getReference(), imported.getReference());
 					rv.add(t);
 					threadTranslations.add(t);
 				}
 				else
 				{
 					// well, we tried. The embedded cross reference will remain in place.
 					M_log.warn("importResources: failed to move resource: " + ref.getReference());
 				}
 			}
 
 			// do we need a second-pass translation?
 			if (imported != null)
 			{
 				String importedRef = imported.getReference();
 				String type = readReferencedDocumentType(importedRef);
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
 		}
 
 		// now that we have all the translations, update any resources that have html bodies
 		if (translate)
 		{
 			for (Reference ref : toTranslate)
 			{
 				// translate using the full set we have so far for the thread
 				translateHtmlBody(ref, threadTranslations, context, shortenUrls);
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Check if the reference is a content hosting reference.
 	 * 
 	 * @param ref
 	 *        The references string.
 	 * @return true if ref is a content hosting reference, false if not.
 	 */
 	protected static int indexContentReference(String ref)
 	{
 		// regular content
 		int rv = ref.indexOf("/access/content/");
 		if (rv == -1)
 		{
 			// known private content
 			rv = ref.indexOf("/access/mneme/content/");
 			if (rv == -1)
 			{
 				rv = ref.indexOf("/access/meleteDocs/content/");
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Check if this URL is being hosted by us on this server. Consider the primary and also some alternate URL roots.
 	 * 
 	 * @param url
 	 *        The url to check.
 	 * @return -1 if not, or the index position in the url of the start of the relative portion (i.e. after the server URL root)
 	 */
 	protected static int internallyHostedUrl(String url)
 	{
 		// form the access root, and check for alternate ones
 		String serverUrl = ServerConfigurationService.getServerUrl();
 		String[] alternateUrls = ServerConfigurationService.getStrings("alternateServerUrlRoots");
 
 		if (url.startsWith(serverUrl)) return serverUrl.length();
 		if (alternateUrls != null)
 		{
 			for (String alternateUrl : alternateUrls)
 			{
 				if (url.startsWith(alternateUrl)) return alternateUrl.length();
 			}
 		}
 
 		return -1;
 	}
 
 	/**
 	 * Check for a 'match' between a local resource and an import candidate
 	 * 
 	 * @param mr
 	 *        The local resource.
 	 * @param type
 	 *        The import candidate mime type.
 	 * @param sizeThe
 	 *        import candidate body size.
 	 * @param body
 	 *        The import candidate body.
 	 * @return true if there is a match, false if not.
 	 * @throws ServerOverloadException
 	 */
 	protected static boolean matches(ContentResource mr, String type, int size, byte[] body) throws ServerOverloadException
 	{
 		// check if the type and size match - get the cheap tests done first
 		if (!StringUtil.different(mr.getContentType(), type, true))
 		{
 			// meta-data matches, check the body - we want EXACT
 			if (type.toLowerCase().endsWith("html"))
 			{
 				// TODO: special html compare... size may not match, body may have been translated
 				boolean same = false;
 
 				if (same)
 				{
 					return true;
 				}
 			}
 			else
 			{
 				// byte by byte compare, if the size matches
 				if (mr.getContentLength() == size)
 				{
 					// get the candidate resource
 					byte[] mrBody = mr.getContent();
 
 					// compare, byte for byte
 					boolean same = Arrays.equals(body, mrBody);
 
 					// use this member if it matches
 					if (same)
 					{
 						return true;
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Read a document from content hosting.
 	 * 
 	 * @param ref
 	 *        The document reference.
 	 * @return The document content in a String.
 	 */
 	protected static String readReferencedDocument(String ref)
 	{
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		try
 		{
 			// get an id from the reference string
 			Reference reference = EntityManager.newReference(ref);
 			String id = reference.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			try
 			{
 				// read the resource
 				ContentResource r = ContentHostingService.getResource(id);
 
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
 			SecurityService.popAdvisor();
 		}
 
 		return "";
 	}
 
 	/**
 	 * Read a document's mime type from content hosting.
 	 * 
 	 * @param ref
 	 *        The document reference.
 	 * @return The document's mime type.
 	 */
 	protected static String readReferencedDocumentType(String ref)
 	{
 		// bypass security when reading the resource to copy
 		SecurityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 
 		try
 		{
 			// get an id from the reference string
 			Reference reference = EntityManager.newReference(ref);
 			String id = reference.getId();
 			if (id.startsWith("/content/"))
 			{
 				id = id.substring("/content".length());
 			}
 
 			try
 			{
 				// read the resource
 				ContentResource r = ContentHostingService.getResource(id);
 				String type = r.getContentType();
 
 				return type;
 			}
 			catch (IdUnusedException e)
 			{
 			}
 			catch (TypeException e)
 			{
 				M_log.warn("readReferencedDocumentType: " + e.toString());
 			}
 			catch (PermissionException e)
 			{
 				M_log.warn("readReferencedDocumentType: " + e.toString());
 			}
 		}
 		finally
 		{
 			SecurityService.popAdvisor();
 		}
 
 		return "";
 	}
 }
