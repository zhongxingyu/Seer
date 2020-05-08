 package org.eweb4j.component.dwz.menu.navmenu;
 
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 
 import org.eweb4j.component.dwz.menu.MenuException;
 import org.eweb4j.mvc.action.annotation.Result;
 
 @Path("${NavMenuConstant.MODEL_NAME}")
 public class SearchNavMenuAction extends NavMenuBaseAction {
 
 	@GET
 	@POST
 	@Path("/search")
 	@Result("${NavMenuConstant.PAGING_ACTION_RESULT}")
 	public String doSearchAndPaging(Map model) {
 		try {
 			model.put("listPage", service.getSearchResult(keyword, pageNum, numPerPage));
 		} catch (MenuException e) {
 			return dwz.getFailedJson(e.getMessage()).toString();
 		}
 
 		return "success";
 	}
 
 	@GET
 	@POST
 	@Path("/lookupSearch")
 	@Result("${NavMenuConstant.LOOKUP_ACTION_RESULT}")
 	public String doLookupSearch(Map model) {
 		return this.doSearchAndPaging(model);
 	}
 
 }
