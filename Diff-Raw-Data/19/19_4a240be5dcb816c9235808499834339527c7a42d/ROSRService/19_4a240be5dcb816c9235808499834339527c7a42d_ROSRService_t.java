 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal.services;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.scribe.model.Token;
 
 import pl.psnc.dl.wf4ever.portal.model.RoFactory;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.AnonId;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * @author Piotr Hołubowicz
  * 
  */
 public class ROSRService
 {
 
 	private static final Logger log = Logger.getLogger(ROSRService.class);
 
 	private static final URI baseURI = URI.create("http://sandbox.wf4ever-project.org/rosrs5");
 
 
 	public static ClientResponse createResearchObject(String roId, Token dLibraToken)
 	{
 		Client client = Client.create();
 		WebResource webResource = client.resource(baseURI.toString()).path("ROs");
 		return webResource.header("Authorization", "Bearer " + dLibraToken.getToken()).type("text/plain")
 				.post(ClientResponse.class, roId);
 	}
 
 
 	public static InputStream getResource(URI resourceURI)
 	{
 		Client client = Client.create();
 		WebResource webResource = client.resource(resourceURI.toString());
 		return webResource.get(InputStream.class);
 	}
 
 
 	public static ClientResponse sendResource(URI resourceURI, InputStream content, String contentType,
 			Token dLibraToken)
 	{
 		Client client = Client.create();
 		WebResource webResource = client.resource(resourceURI.toString());
 		return webResource.header("Authorization", "Bearer " + dLibraToken.getToken()).type(contentType)
 				.put(ClientResponse.class, content);
 	}
 
 
 	public static ClientResponse deleteResource(URI resourceURI, Token dLibraToken)
 	{
 		Client client = Client.create();
 		WebResource webResource = client.resource(resourceURI.toString());
 		return webResource.header("Authorization", "Bearer " + dLibraToken.getToken()).delete(ClientResponse.class);
 	}
 
 
 	public static List<URI> getROList()
 		throws Exception
 	{
 		return getROList(null);
 	}
 
 
 	public static List<URI> getROList(Token dLibraToken)
 		throws MalformedURLException, URISyntaxException
 	{
 		Client client = Client.create();
 		WebResource webResource = client.resource(baseURI.toString()).path("ROs");
 		String response;
 		if (dLibraToken == null) {
 			response = webResource.get(String.class);
 		}
 		else {
 			response = webResource.header("Authorization", "Bearer " + dLibraToken.getToken()).get(String.class);
 		}
 		List<URI> uris = new ArrayList<URI>();
 		for (String s : response.split("[\\r\\n]+")) {
 			if (!s.isEmpty()) {
 				uris.add(new URI(s));
 			}
 		}
 		return uris;
 	}
 
 
 	public static ClientResponse deleteResearchObject(URI researchObjectURI, Token dLibraToken)
 	{
 		Client client = Client.create();
 		WebResource webResource = client.resource(researchObjectURI.toString());
 		return webResource.header("Authorization", "Bearer " + dLibraToken.getToken()).delete(ClientResponse.class);
 	}
 
 
 	/**
 	 * Creates an annotation and an empty annotation body in ROSRS
 	 * 
 	 * @param researchObjectURI
 	 * @param targetURI
 	 * @param username
 	 * @param dLibraToken
 	 * @throws OAuthException
 	 * @throws URISyntaxException
 	 */
 	public static ClientResponse addAnnotation(URI researchObjectURI, URI targetURI, String username, Token dLibraToken)
 		throws URISyntaxException
 	{
 		InputStream is = ROSRService.getResource(researchObjectURI.resolve(".ro/manifest"));
 		OntModel manifest = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
 		manifest.read(is, null);
 
 		URI bodyURI = ROSRService.createAnnotationBodyURI(researchObjectURI, targetURI);
 		addAnnotation(manifest, researchObjectURI, targetURI, bodyURI, username);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		manifest.write(out);
 		sendResource(researchObjectURI.resolve(".ro/manifest"), new ByteArrayInputStream(out.toByteArray()),
 			"application/rdf+xml", dLibraToken);
 
 		OntModel body = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
 		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
 		body.write(out2);
 		return sendResource(bodyURI, new ByteArrayInputStream(out2.toByteArray()), "application/rdf+xml", dLibraToken);
 	}
 
 
 	public static ClientResponse deleteAnnotation(URI researchObjectURI, URI annURI, Token dLibraToken)
 		throws IllegalArgumentException, URISyntaxException
 	{
 		InputStream is = ROSRService.getResource(researchObjectURI.resolve(".ro/manifest"));
 		OntModel manifest = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
 		manifest.read(is, null);
 
 		Individual ann = manifest.getIndividual(annURI.toString());
 		if (ann == null) {
 			throw new IllegalArgumentException("Annotation URI is not valid");
 		}
 		Resource body = ann.getPropertyResourceValue(RoFactory.aoBody);
 		try {
 			deleteResource(new URI(body.getURI()), dLibraToken);
 		}
 		catch (Exception e) {
 			log.warn("Problem with deleting annotation body: " + e.getMessage());
 		}
 
 		manifest.removeAll(ann, null, null);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		manifest.write(out);
 		return sendResource(researchObjectURI.resolve(".ro/manifest"), new ByteArrayInputStream(out.toByteArray()),
 			"application/rdf+xml", dLibraToken);
 	}
 
 
 	/**
 	 * Adds an annotation to the manifest model
 	 * 
 	 * @param manifest
 	 * @param researchObjectURI
 	 * @param targetURI
 	 * @param bodyURI
 	 * @throws URISyntaxException
 	 */
 	public static void addAnnotation(OntModel manifest, URI researchObjectURI, URI targetURI, URI bodyURI,
 			String username)
 		throws URISyntaxException
 	{
 		Individual ann = manifest.createIndividual(createAnnotationURI(manifest, researchObjectURI).toString(),
 			RoFactory.aggregatedAnnotation);
 		ann.addProperty(RoFactory.annotatesResource, manifest.createResource(targetURI.toString()));
 		ann.addProperty(RoFactory.aoBody, manifest.createResource(bodyURI.toString()));
 		ann.addProperty(DCTerms.created, manifest.createTypedLiteral(Calendar.getInstance()));
 		Individual agent = manifest.createResource(new AnonId(username)).as(Individual.class);
 		agent.setOntClass(RoFactory.foafAgent);
 		agent.addProperty(RoFactory.foafName, username);
 		ann.addProperty(DCTerms.creator, agent);
		Individual ro = manifest.createResource(researchObjectURI.toString()).as(Individual.class);
		ro.addProperty(RoFactory.aggregates, ann);
 	}
 
 
 	/**
 	 * 
 	 * @param manifest
 	 * @param researchObjectURI
 	 * @return i.e.
 	 *         http://sandbox.wf4ever-project.org/rosrs5/ROs/ann217/52a272f1-864f-4a42
 	 *         -89ff-2501a739d6f0
 	 */
 	private static URI createAnnotationURI(OntModel manifest, URI researchObjectURI)
 	{
 		URI ann = null;
 		do {
 			ann = researchObjectURI.resolve(UUID.randomUUID().toString());
 		}
 		while (manifest.containsResource(manifest.createResource(ann.toString())));
 		return ann;
 	}
 
 
 	/**
 	 * 
 	 * @param researchObjectURI
 	 * @param targetURI
 	 * @return i.e.
 	 *         http://sandbox.wf4ever-project.org/rosrs5/ROs/ann217/.ro/ro--5600459667350895101.
 	 *         rdf
 	 * @throws URISyntaxException
 	 */
 	public static URI createAnnotationBodyURI(URI researchObjectURI, URI targetURI)
 		throws URISyntaxException
 	{
 		String targetName;
 		if (targetURI.equals(researchObjectURI))
 			targetName = "ro";
 		else
 			targetName = targetURI.resolve(".").relativize(targetURI).toString();
 		String randomBit = "" + Math.abs(UUID.randomUUID().getLeastSignificantBits());
 
 		return researchObjectURI.resolve(".ro/" + targetName + "-" + randomBit);
 	}
 
 
 	public static String[] getWhoAmi(Token dLibraToken)
 		throws URISyntaxException
 	{
 		Client client = Client.create();
 		WebResource webResource = client.resource(baseURI.toString()).path("whoami");
 		return webResource.header("Authorization", "Bearer " + dLibraToken.getToken()).get(String.class)
 				.split("[\\r\\n]+");
 	}
 }
