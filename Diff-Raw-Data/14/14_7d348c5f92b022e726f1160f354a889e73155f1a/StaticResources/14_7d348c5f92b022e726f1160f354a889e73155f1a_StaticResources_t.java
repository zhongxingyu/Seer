 package resources;
 
import static com.google.common.base.Objects.firstNonNull;
 import static java.lang.String.format;
 
 import java.io.File;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 @Path("/")
 public class StaticResources {
	
	private String staticDirectory;

	public StaticResources() {
		this.staticDirectory = firstNonNull(System.getProperty("fr.valtech.staticdir"), "../../static/");
	}
 
 	@GET
 	public Response index() {
 		return Response.ok(serveStaticFile("index.html")).type(MediaType.TEXT_HTML).build();
 	}
 
 	@GET
 	@Path("/{subfolder: .*}")
 	public File serveStaticFile(@PathParam("subfolder") final String subFolder) {
		final String pathname = format("%s/%s", staticDirectory, subFolder);
 		return new File(pathname);
 	}
 
 	@GET
 	@Path("/{subfolder: .*}.json")
 	@Produces(MediaType.APPLICATION_JSON)
 	public File serveStaticJson(@PathParam("subfolder") final String subFolder) {
		final String pathname = format("%s/%s.json", staticDirectory, subFolder);
 		return new File(pathname);
 	}
 }
