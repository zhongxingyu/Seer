 package jipdbs.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jipdbs.JIPDBS;
 import jipdbs.PageLink;
 import jipdbs.SearchResult;
 import jipdbs.util.Functions;
 
 public class SearchServlet extends HttpServlet {
 
 	private static final int DEFAULT_PAGE_SIZE = 20;
 
 	private static final long serialVersionUID = -729953187311026007L;
 
 	private JIPDBS app;
 
 	@Override
 	public void init() throws ServletException {
 		app = (JIPDBS) getServletContext().getAttribute("jipdbs");
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		int page = 1;
 		int pageSize = DEFAULT_PAGE_SIZE;
 
 		try {
 			page = Integer.parseInt(req.getParameter("p"));
 		} catch (NumberFormatException e) {
 			// Ignore.
 		}
 
 		try {
 			pageSize = Integer.parseInt(req.getParameter("ps"));
 		} catch (NumberFormatException e) {
 			// Ignore.
 		}
 		
 		int offset = (page - 1) * pageSize;
 		int limit = pageSize;
 
 		String query = req.getParameter("q");
 		String type = req.getParameter("t");
 
 		List<SearchResult> list = new ArrayList<SearchResult>();
 
 		int[] total = new int[1];
 
 		long time = System.currentTimeMillis();
 
 		String queryValue = query;
 		if (query == null || "".equals(query)) {
 			list = app.rootQuery(offset, limit, total);
 		} else {
 			// this is to get the modified value and show it in search box
 			if ("ip".equals(type)) {
 				query = Functions.fixIp(query);
 				queryValue = query;
 			}
 			else if ("s".equals(type)) queryValue = "";
 			
 			list = app.search(query, type, offset, limit, total);
 		}
 		
 		time = System.currentTimeMillis() - time;
 
 		int totalPages = (int) Math.ceil((double) total[0] / pageSize);
 
 		req.setAttribute("list", list);
		req.setAttribute("queryValue", queryValue);
		req.setAttribute("query", query);
 		req.setAttribute("type", type);
 		req.setAttribute("count", total[0]);
 		req.setAttribute("time", time);
 		req.setAttribute("pageLink", new PageLink(page, pageSize, totalPages));
 	}
 }
