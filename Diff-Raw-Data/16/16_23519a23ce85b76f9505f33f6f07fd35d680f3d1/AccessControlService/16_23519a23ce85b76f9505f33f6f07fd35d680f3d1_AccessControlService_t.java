 package org.purl.wf4ever.rosrs.client.accesscontrol;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.log4j.Logger;
 import org.purl.wf4ever.rosrs.client.evo.ROEVOService;
 
 import com.damnhandy.uri.template.UriTemplate;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.shared.JenaException;
 import com.hp.hpl.jena.util.FileManager;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.WebResource.Builder;
 
 /**
  * The notification service.
  * 
  * @see http 
  *      ://wf4ever-project.org/wiki/display/docs/Showcase+129.+The+notification
  *      +service
  * @author piotrekhol
  * 
  */
 public class AccessControlService implements Serializable {
 
 	/** id. */
 	private static final long serialVersionUID = 4882901036606057893L;
 
 	/** Logger. */
 	private static final Logger LOGGER = Logger.getLogger(ROEVOService.class);
 
 	/** Notifications service URI. */
 	private URI serviceUri;
 
 	/** OAuth 2 access token. */
 	private String token;
 
 	/** HTTP client. */
 	private transient Client client;
 
 	/** Notifications resource URI template. */
 	private String permissionsUriTemplateString;
 
 	/** Notifications resource URI template. */
 	private String modesUriTemplateString;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param serviceUri
 	 *            Notification service URI
 	 * @param token
 	 *            RODL access token
 	 */
 	public AccessControlService(URI serviceUri, String token) {
 		this.serviceUri = serviceUri;
 		this.token = token;
 	}
 
 	/**
 	 * Return an HTTP client, creating it if necessary.
 	 * 
 	 * @return an HTTP client
 	 */
 	private Client getClient() {
 		if (client == null) {
 			client = Client.create();
 		}
 		return client;
 	}
 
 	/**
 	 * Load the accesscontrol service description.
 	 */
 
 	void init() {
 		try {
 			Model model = FileManager.get().loadModel(serviceUri.toString());
 			Resource serviceResource = model.getResource(serviceUri.toString());
 			this.permissionsUriTemplateString = serviceResource
 					.listProperties(pl.psnc.dl.wf4ever.vocabulary.AccessControlService.permissions)
 					.next().getObject().asLiteral().getString();
 			this.modesUriTemplateString = serviceResource
 					.listProperties(pl.psnc.dl.wf4ever.vocabulary.AccessControlService.modes)
 					.next().getObject().asLiteral().getString();
 		} catch (JenaException e) {
 			LOGGER.error("Could not initialize the notification service: "
 					+ e.getLocalizedMessage());
 		}
 	}
 
 	public String getPermissionsUriTemplateString() {
 		return permissionsUriTemplateString;
 	}
 
 	public String getModesUriTemplateString() {
 		return modesUriTemplateString;
 	}
 
 	public URI getPermissionsUri(int id) {
 		if (permissionsUriTemplateString == null) {
 			init();
 		}
 		return URI.create(permissionsUriTemplateString).resolve(Integer.toString(id));
 	}
 
 	public URI getPermissionsUri(URI roUri) {
 		if (permissionsUriTemplateString == null) {
 			init();
 		}
 		if (permissionsUriTemplateString == null) {
 			init();
 		}
 		UriTemplate uriTemplate = UriTemplate.fromTemplate(permissionsUriTemplateString);
 		if (roUri != null) {
 			uriTemplate = uriTemplate.set("ro", roUri.toString());
 		}
 		return serviceUri.resolve(UriBuilder.fromUri(uriTemplate.expand()).build());
 	}
 
 	public URI getModesUri(int id) {
 		if (modesUriTemplateString == null) {
 			init();
 		}
 		return URI.create(permissionsUriTemplateString).resolve(Integer.toString(id));
 	}
 
 	public URI getModeUri(URI roUri) {
 		if (modesUriTemplateString == null) {
 			init();
 		}
 		UriTemplate uriTemplate = UriTemplate.fromTemplate(modesUriTemplateString);
 		if (roUri != null) {
 			uriTemplate = uriTemplate.set("ro", roUri.toString());
 		}
 		return serviceUri.resolve(UriBuilder.fromUri(uriTemplate.expand()).build());
 	}
 
 	public AccessMode getMode(URI roUri) {
 		WebResource webResource = getClient().resource(getModeUri(roUri));
 		Builder builder = webResource.accept(MediaType.APPLICATION_JSON).header("Authorization",
 				"Bearer " + token);
 		return webResource.get(AccessMode.class);
 	}
 
 	public List<Permission> getPermissions(URI roUri) {
 		WebResource webResource = getClient().resource(getModeUri(roUri));
 		Builder builder = webResource.accept(MediaType.APPLICATION_JSON).header("Authorization",
 				"Bearer " + token);
 		return Arrays.asList(webResource.get(Permission.class));
 	}
 }
