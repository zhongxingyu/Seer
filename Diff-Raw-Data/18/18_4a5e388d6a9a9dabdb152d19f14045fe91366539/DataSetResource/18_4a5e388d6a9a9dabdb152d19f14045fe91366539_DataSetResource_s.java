 /**
  * 
  */
 package uk.ac.ids.resources;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.freemarker.TemplateRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.ServerResource;
 
 import uk.ac.ids.Main;
 
 /**
  * @author Christophe Guéret <christophe.gueret@gmail.com>
  * 
  */
 public class DataSetResource extends ServerResource {
 	private String datasetName;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.restlet.resource.UniformResource#doInit()
 	 */
 	@Override
 	protected void doInit() throws ResourceException {
 		// Get the attributes value taken from the URI template
 		datasetName = (String) getRequest().getAttributes().get("DB");
 
 		// If no ID has been given, return a 404
 		if (datasetName == null) {
 			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
 			setExisting(false);
 		}
 
 	}
 
 	/**
 	 * Returns an HTML homepage for the data set
 	 * 
 	 * @return an HTML representation of the resource
 	 */
 	@Get("html")
 	public Representation toHTML() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("meta", getApplication().getMappings(datasetName).getMetaData());
 		return new TemplateRepresentation("dataset.html", getApplication().getConfiguration(), map, MediaType.TEXT_HTML);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.restlet.resource.Resource#getApplication()
 	 */
 	@Override
 	public Main getApplication() {
 		return (Main) super.getApplication();
 	}
 }
