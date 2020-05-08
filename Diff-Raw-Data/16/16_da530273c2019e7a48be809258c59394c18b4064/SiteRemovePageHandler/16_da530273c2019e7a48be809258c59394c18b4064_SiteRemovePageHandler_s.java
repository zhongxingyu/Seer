 package org.sakaiproject.sdata.services.site;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.sakaiproject.Kernel;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.sdata.tool.json.JSONServiceHandler;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SitePage;
 import org.sakaiproject.site.api.SiteService;
 
 public class SiteRemovePageHandler extends JSONServiceHandler
 {
 	private SiteService siteService;
 
 	@Override
 	public void init(Map<String, String> config) throws ServletException
 	{
		super.init(config);
 		siteService = Kernel.siteService();
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		try
 		{
 			String siteId = request.getParameter("siteid");
 			String pageId = request.getParameter("pageId");
 			Site site = siteService.getSite(siteId);
 			SitePage page = site.getPage(pageId);
 			site.removePage(page);
 		}
 		catch (IdUnusedException iue)
 		{
 			sendError(request, response, iue);
 		}
 	}
 }
