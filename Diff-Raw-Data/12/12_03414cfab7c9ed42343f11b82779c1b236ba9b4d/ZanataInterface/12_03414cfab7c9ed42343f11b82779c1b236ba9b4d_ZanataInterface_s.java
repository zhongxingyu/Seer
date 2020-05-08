 package com.redhat.topicindex.zanata;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.jboss.resteasy.client.ClientResponse;
 import org.zanata.common.LocaleId;
 import org.zanata.rest.client.ITranslationResources;
 import org.zanata.rest.dto.VersionInfo;
 import org.zanata.rest.dto.resource.Resource;
 import org.zanata.rest.dto.resource.ResourceMeta;
 import org.zanata.rest.dto.resource.TranslationsResource;
 import org.zanata.util.VersionUtility;
 
 import com.redhat.ecs.commonutils.ExceptionUtilities;
 import com.redhat.ecs.constants.CommonConstants;
 
 public class ZanataInterface
 {
 	private final static ZanataDetails defaultDetails = new ZanataDetails();
 	private final ZanataDetails details;
 	private final ZanataProxyFactory proxyFactory;
 	private final ZanataLocaleManager localeManager;
 
 	public ZanataInterface()
 	{
 		this(defaultDetails.getProject());
 	}
 
 	/**
 	 * Constructs the interface with a custom project
 	 * 
 	 * @param projectOverride
 	 *            The name of the Zanata project to work with, which override the default specidie
 	 */
 	public ZanataInterface(final String projectOverride)
 	{
 		details = new ZanataDetails(defaultDetails);
 		details.setProject(projectOverride);
 
 		URI URI = null;
 		try
 		{
 			URI = new URI(details.getServer());
 		}
 		catch (URISyntaxException e)
 		{
 		}
 		final VersionInfo versionInfo = VersionUtility.getVersionInfo(ZanataProxyFactory.class);
 		if (versionInfo.getVersionNo() == null || versionInfo.getVersionNo().isEmpty() || versionInfo.getVersionNo().equals("unknown"))
 			versionInfo.setVersionNo("1.5.0");
 
 		proxyFactory = new ZanataProxyFactory(URI, details.getUsername(), details.getToken(), versionInfo);
 		localeManager = ZanataLocaleManager.getInstance(details.getProject());
 	}
 
 	public boolean getZanataResourceExists(final String id)
 	{
 		try
 		{
 			final ITranslationResources client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<Resource> response = client.getResource(id, null);
 
 			final Status status = Response.Status.fromStatusCode(response.getStatus());
 
 			return status == Response.Status.OK;
 
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return false;
 	}
 
 	public Resource getZanataResource(final String id)
 	{
 		try
 		{
 			final ITranslationResources client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<Resource> response = client.getResource(id, null);
 
 			final Status status = Response.Status.fromStatusCode(response.getStatus());
 
 			if (status == Response.Status.OK)
 			{
 				final Resource entity = response.getEntity();
 				return entity;
 			}
 			else
 			{
 				System.out.println("REST call to getResource() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason was " + status.getReasonPhrase());
 			}
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return null;
 	}
 
 	public List<ResourceMeta> getZanataResources()
 	{
 		try
 		{
 			final ITranslationResources client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<List<ResourceMeta>> response = client.get(null);
 
 			final Status status = Response.Status.fromStatusCode(response.getStatus());
 
 			if (status == Response.Status.OK)
 			{
 				final List<ResourceMeta> entities = response.getEntity();
 				return entities;
 			}
 			else
 			{
 				System.out.println("REST call to get() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason was " + status.getReasonPhrase());
 			}
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return null;
 	}
 
 	public void createFile(final Resource resource)
 	{
 		try
 		{
 			final IFixedTranslationResources client = proxyFactory.getFixedTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<String> response = client.post(details.getUsername(), details.getToken(), resource, null, true);
 
 			final Status status = Response.Status.fromStatusCode(response.getStatus());
 
 			if (status == Response.Status.CREATED)
 			{
 				final String entity = response.getEntity();
 				if (entity.trim().length() != 0)
 					System.out.println(entity);
 			}
 			else
 			{
 				System.out.println("REST call to createResource() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason was " + status.getReasonPhrase());
 			}
 
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 	}
 
 	public boolean getTranslationsExists(final String id, final LocaleId locale)
 	{
 		ITranslationResources client = null;
 		ClientResponse<TranslationsResource> response = null;
 
 		try
 		{
 			client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			response = client.getTranslations(id, locale, null);
 
 			final Status status = Response.Status.fromStatusCode(response.getStatus());
 
			/* Remove the locale if it is forbidden, or older version of zanata return a 404 */
			if (status == Response.Status.FORBIDDEN || status == Response.Status.NOT_FOUND)
 			{
				System.out.println("Removing " + locale + " from further sync requests.");
 				localeManager.removeLocale(locale);
 			}
 
 			return status == Response.Status.OK;
 
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 
 		}
 		finally
 		{
 			/*
 			 * If you are using RESTEasy client framework, and returning a Response from your service method, you will explicitly need to release the
 			 * connection.
 			 */
 			if (response != null)
 				response.releaseConnection();
 		}
 
 		return false;
 	}
 
 	public TranslationsResource getTranslations(final String id, final LocaleId locale)
 	{
 		try
 		{
 			final ITranslationResources client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<TranslationsResource> response = client.getTranslations(id, locale, null);
 
 			final Status status = Response.Status.fromStatusCode(response.getStatus());
 
 			/* Remove the locale if it is forbidden */
 			if (status == Response.Status.FORBIDDEN)
 			{
 				localeManager.removeLocale(locale);
 			}
 			else if (status == Response.Status.OK)
 			{
 				final TranslationsResource retValue = response.getEntity();
 				return retValue;
 			}
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return null;
 	}
 
 	public TranslationsResource deleteResource(final String id)
 	{
 		try
 		{
 			final IFixedTranslationResources client = proxyFactory.getFixedTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<String> response = client.deleteResource(details.getUsername(), details.getToken(), id);
 
 			final Status status = Response.Status.fromStatusCode(response.getStatus());
 
 			if (status == Response.Status.OK)
 			{
 				final String entity = response.getEntity();
 				if (entity.trim().length() != 0)
 					System.out.println(entity);
 			}
 			else
 			{
 				System.out.println("REST call to deleteResource() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason was " + status.getReasonPhrase());
 			}
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return null;
 	}
 
 	public List<LocaleId> getZanataLocales()
 	{
 		return localeManager.getLocales();
 	}
 }
 
 /**
  * A utility class to pull out the Zanata details from the system properties
  */
 class ZanataDetails
 {
 	private String server;
 	private String project;
 	private String version;
 	private String username;
 	private String token;
 
 	public ZanataDetails()
 	{
 		this.server = System.getProperty(CommonConstants.ZANATA_SERVER_PROPERTY);
 		this.project = System.getProperty(CommonConstants.ZANATA_PROJECT_PROPERTY);
 		this.version = System.getProperty(CommonConstants.ZANATA_PROJECT_VERSION_PROPERTY);
 		this.username = System.getProperty(CommonConstants.ZANATA_USERNAME_PROPERTY);
 		this.token = System.getProperty(CommonConstants.ZANATA_TOKEN_PROPERTY);
 	}
 
 	public ZanataDetails(final ZanataDetails zanataDetails)
 	{
 		this.server = zanataDetails.server;
 		this.project = zanataDetails.project;
 		this.version = zanataDetails.version;
 		this.username = zanataDetails.username;
 		this.token = zanataDetails.token;
 	}
 
 	public String getServer()
 	{
 		return server;
 	}
 
 	public void setServer(String server)
 	{
 		this.server = server;
 	}
 
 	public String getProject()
 	{
 		return project;
 	}
 
 	public void setProject(String project)
 	{
 		this.project = project;
 	}
 
 	public String getVersion()
 	{
 		return version;
 	}
 
 	public void setVersion(String version)
 	{
 		this.version = version;
 	}
 
 	public String getUsername()
 	{
 		return username;
 	}
 
 	public void setUsername(String username)
 	{
 		this.username = username;
 	}
 
 	public String getToken()
 	{
 		return token;
 	}
 
 	public void setToken(String token)
 	{
 		this.token = token;
 	}
 
 	public String getUrl()
 	{
 		return server + "/seam/resource/restv1/projects/p/" + project + "/iterations/i/" + version + "/r";
 	}
 }
