 package ceid.netcins.frontend;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import rice.p2p.commonapi.Id;
 import ceid.netcins.CatalogService;
 import ceid.netcins.content.ContentProfile;
 import ceid.netcins.json.Json;
 
 public class GetContentHandler extends CatalogFrontendAbstractHandler {
 
 	private static final long serialVersionUID = -2901313244513782698L;
 
 	public GetContentHandler(CatalogService catalogService,
 			Hashtable<String, Object> queue) {
 		super(catalogService, queue);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException {
		Map<Id, ContentProfile> content = catalogService.getUser().getSharedContentProfiles();
 		response.setContentType("application/json");
 		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(Json.toString(content));
 	}
 }
