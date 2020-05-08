 package jipdbs.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jipdbs.JIPDBS;
 import jipdbs.SearchResult;
 
 public class SearchServlet extends HttpServlet {
 
 	private static final int DEFAULT_PAGE_SIZE = 20;
 
 	private static final long serialVersionUID = -729953187311026007L;
 
 	private JIPDBS app;
 
 	public static class PageLink {
 
 		final int pageNumber;
 		final int pageSize;
 		final int totalPages;
 
 		public PageLink(int pageNumber, int pageSize, int totalPages) {
 			this.pageNumber = pageNumber;
 			this.totalPages = totalPages;
 			this.pageSize = pageSize;
 		}
 
 		public int getTotalPages() {
 			return totalPages;
 		}
 
 		public int getPageNumber() {
 			return pageNumber;
 		}
 
 		public int getPageSize() {
 			return pageSize;
 		}
 	}
 
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
 		if (query == null || "".equals(query))
 			list = app.rootQuery(offset, limit, total);
 		else
 			list = app.search(query, type, offset, limit, total);
 
 		int totalPages = (int) Math.ceil((double) total[0] / pageSize);
 
 		req.setAttribute("list", list);
 		req.setAttribute("query", query);
 		req.setAttribute("type", type);
 		req.setAttribute("pageLink", new PageLink(page, pageSize, totalPages));
 	}
 }
