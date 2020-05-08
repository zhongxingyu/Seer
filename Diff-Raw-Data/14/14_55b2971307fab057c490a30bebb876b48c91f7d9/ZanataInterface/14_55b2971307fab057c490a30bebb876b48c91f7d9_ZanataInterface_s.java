 package com.redhat.topicindex.zanata;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;

 import org.zanata.common.LocaleId;
 import org.zanata.rest.client.ITranslationResources;
 import org.zanata.rest.client.ZanataProxyFactory;
 import org.zanata.rest.dto.VersionInfo;
 import org.zanata.rest.dto.resource.Resource;
 import org.zanata.rest.dto.resource.ResourceMeta;
 import org.zanata.rest.dto.resource.TranslationsResource;
 import org.zanata.util.VersionUtility;
 
 import com.redhat.ecs.commonutils.ExceptionUtilities;
 import com.redhat.ecs.constants.CommonConstants;
 
 public class ZanataInterface
 {
 	private static final ZanataDetails details = new ZanataDetails();
 	
 	public static final ZanataProxyFactory proxyFactory;
 	static {
 		URI URI = null;
 		try {
 			URI = new URI(details.getServer());
 		} catch (URISyntaxException e) {
 		}
 		final VersionInfo versionInfo = VersionUtility.getVersionInfo(ZanataProxyFactory.class);
 		
 		proxyFactory = new ZanataProxyFactory(URI, details.getUsername(), details.getUsername(), versionInfo);
 	}
 	
 	public static boolean getZanataResourceExists(final String id)
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
 	
 	public static Resource getZanataResource(final String id)
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
 	
 	public static List<ResourceMeta> getZanataResources()
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
 
 	public static void createFile(final Resource resource)
 	{
 		try
 		{
 			final ITranslationResources client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<String> response = client.post(resource, null, true);
 			
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
 	
 	public static boolean getTranslationsExists(final String id, final LocaleId locale)
 	{
 		try
 		{
			final ZanataDetails details = new ZanataDetails();
			final String URI = details.getUrl();

			final ITranslationResources client = ProxyFactory.create(ITranslationResources.class, URI);
 			final ClientResponse<TranslationsResource> response = client.getTranslations(id, locale, null);
 
			return Response.Status.fromStatusCode(response.getStatus()) == Response.Status.OK;
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return false;
 	}
 
 	public static TranslationsResource getTranslations(final String id, final LocaleId locale)
 	{
 		try
 		{
 			final ITranslationResources client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<TranslationsResource> response = client.getTranslations(id, locale, null);
 
 			if (Response.Status.fromStatusCode(response.getStatus()) == Response.Status.OK)
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
 	
 	public static TranslationsResource deleteResource(final String id)
 	{
 		try
 		{
 			final ITranslationResources client = proxyFactory.getTranslationResources(details.getProject(), details.getVersion());
 			final ClientResponse<String> response = client.deleteResource(id);
 			
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
 	private String url;
 
 	public ZanataDetails()
 	{
 		this.server = System.getProperty(CommonConstants.ZANATA_SERVER_PROPERTY);
 		this.project = System.getProperty(CommonConstants.ZANATA_PROJECT_PROPERTY);
 		this.version = System.getProperty(CommonConstants.ZANATA_PROJECT_VERSION_PROPERTY);
 		this.username = System.getProperty(CommonConstants.ZANATA_USERNAME_PROPERTY);
 		this.token = System.getProperty(CommonConstants.ZANATA_TOKEN_PROPERTY);
 		this.url = server + "/seam/resource/restv1/projects/p/" + project + "/iterations/i/" + version + "/r";
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
 		return url;
 	}
 
 	public void setUrl(String url)
 	{
 		this.url = url;
 	}
 }
