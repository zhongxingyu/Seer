 package pl.psnc.dl.wf4ever.sms;
 
 import java.net.URI;
import java.net.URISyntaxException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 
import org.apache.log4j.Logger;

 import com.hp.hpl.jena.xmloutput.impl.Basic;
 
 public class RO_RDFXMLWriter
 	extends Basic
 {
 
	private static final Logger log = Logger.getLogger(RO_RDFXMLWriter.class);

 	private URI researchObjectURI;
 
 	private List<URI> namedGraphsURIs;
 
 	private URI baseURI;
 
 
 	public void setResearchObjectURI(URI researchObjectURI)
 	{
 		this.researchObjectURI = researchObjectURI;
 	}
 
 
 	@Override
 	protected String relativize(String uri)
 	{
 		if (researchObjectURI == null || baseURI == null) {
 			return super.relativize(uri);
 		}
 		URI resourceURI = URI.create(uri).normalize();
 		if (resourceURI.toString().startsWith(researchObjectURI.toString())) {
 			Path localPath = Paths.get(baseURI.resolve(".").getPath()).relativize(Paths.get(resourceURI.getPath()));
 			String path;
 			if (namedGraphsURIs.contains(resourceURI)) {
 				path = localPath.toString() + ".rdf";
 			}
 			else {
 				path = localPath.toString();
 			}
			try {
				return new URI(null, null, path, resourceURI.getQuery(), resourceURI.getFragment()).toString();
			}
			catch (URISyntaxException e) {
				log.error("Can't relativize the URI " + resourceURI, e);
				return path;
			}
 		}
 		else {
 			return super.relativize(uri);
 		}
 	}
 
 
 	/**
 	 * @return the researchObjectURI
 	 */
 	public URI getResearchObjectURI()
 	{
 		return researchObjectURI;
 	}
 
 
 	public void setNamedGraphs(List<URI> namedGraphsURIs)
 	{
 		this.namedGraphsURIs = namedGraphsURIs;
 	}
 
 
 	public void setBaseURI(URI baseURI)
 	{
 		this.baseURI = baseURI;
 	}
 }
