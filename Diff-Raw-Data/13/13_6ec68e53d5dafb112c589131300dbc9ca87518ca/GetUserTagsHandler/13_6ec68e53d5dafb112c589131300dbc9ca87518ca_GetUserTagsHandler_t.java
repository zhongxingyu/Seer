 package ceid.netcins.frontend;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import rice.p2p.commonapi.Id;
 import ceid.netcins.CatalogService;
 import ceid.netcins.json.Json;
 import ceid.netcins.social.TagCloud;
 
 public class GetUserTagsHandler extends CatalogFrontendAbstractHandler {
 
 	private static final long serialVersionUID = 7508245962714729908L;
 
 	public GetUserTagsHandler(CatalogService catalogService, Hashtable<String, Object> queue) {
 		super(catalogService, queue);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		response.setContentType("application/json");
 		response.setStatus(HttpServletResponse.SC_OK);
 
		Vector<Object> res = new Vector<Object>();
		res.add(RequestStatusSuccessTag);
 		Map<Id, TagCloud> userTags = catalogService.getUser().getUserTagClouds();
 		if (userTags == null) {
			res.add(new HashMap<String, String>());
			response.getWriter().write(Json.toString(res.toArray()));
 			return;
 		}
		res.add(userTags);
		response.getWriter().write(Json.toString(res.toArray()));
 	}
 }
