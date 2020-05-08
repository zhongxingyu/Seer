 package pl.psnc.dl.wf4ever.sms;
 
 import java.net.URI;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 
 import com.hp.hpl.jena.xmloutput.impl.Basic;
 
 public class RO_RDFXMLWriter
 	extends Basic
 {
 
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
			if (resourceURI.getRawQuery() != null)
				path = path.concat("?").concat(resourceURI.getRawQuery());
			if (resourceURI.getRawFragment() != null)
				path = path.concat("#").concat(resourceURI.getRawFragment());
			return path;
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
