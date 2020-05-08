 package jipdbs.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jipdbs.core.JIPDBS;
 import jipdbs.info.AliasResult;
 
 public class AliasServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 8123604074174537109L;
 
 	private JIPDBS app;
 
 	private final int DEFAULT_LIMIT = 10;
 	
 	@Override
 	public void init() throws ServletException {
 		app = (JIPDBS) getServletContext().getAttribute("jipdbs");
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		String id = req.getParameter("id");
 		String type = req.getParameter("type");
 		
		int page = 1;
 		try {
			page = Integer.parseInt(req.getParameter("o"));
			if (page <= 0) page = 1;
 		} catch (NumberFormatException e) {
 			// Ignore.
 		}
		
		int offset = (page - 1) * DEFAULT_LIMIT;
 
 		List<AliasResult> list = new ArrayList<AliasResult>();
 
 		int[] count = new int[1];
 		if ("ip".equals(type)) {
 			list = app.aliasip(id, offset, DEFAULT_LIMIT, count);
 		} else {
 			list = app.alias(id, offset, DEFAULT_LIMIT, count);	
 		}
 
 		req.setAttribute("hasMore", new Boolean((offset + DEFAULT_LIMIT) < count[0]));
 		req.setAttribute("list", list);
 		req.setAttribute("total", count[0]);
 		req.setAttribute("pages", (int) Math.ceil((double) count[0] / DEFAULT_LIMIT));
		req.setAttribute("offset", page);
 	}
 }
