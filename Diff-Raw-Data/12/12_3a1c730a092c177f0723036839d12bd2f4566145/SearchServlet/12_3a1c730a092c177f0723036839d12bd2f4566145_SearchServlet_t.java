 package jipdbs.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jipdbs.JIPDBS;
 import jipdbs.PageLink;
 import jipdbs.Parameters;
 import jipdbs.bean.SearchResult;
 import jipdbs.util.Functions;
 
 import org.datanucleus.util.StringUtils;
 
 public class SearchServlet extends HttpServlet {
 
 	private static final int DEFAULT_PAGE_SIZE = 20;
 
 	private static final long serialVersionUID = -729953187311026007L;
 
 	private JIPDBS app;
 
 	private final static String IP_RE = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d|[\\*])){3}$";
 	private final static Pattern IP_VALIDATOR = Pattern.compile(IP_RE);
 	
 	private static final Logger log = Logger.getLogger(SearchServlet.class.getName());
 	
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
 		String type = "";
 		try {
 			type = req.getParameter("t");
 		} catch (Exception e) {
 		}
 		
 		List<SearchResult> list = new ArrayList<SearchResult>();
 
 		int[] total = new int[1];
 
 		long time = System.currentTimeMillis();
 
 		// this is to get the modified value and show it in search box
 		String queryValue = query;
 
		if ("s".equals(type)) {
 			log.finest("Buscando SERVER");
 			queryValue = "";
 			list = app.byServerSearch(query, offset, limit, total);
 		} else if ("ban".equals(type)) {
 			log.finest("Buscando BAN");
 			queryValue = "";
 			list = app.bannedQuery(offset, limit, total);
		} else if (StringUtils.notEmpty(query)) {
 			Matcher matcher = IP_VALIDATOR.matcher(query);
 			if (matcher.matches()) {
 				log.finest("Buscando IP " + query);
 				query = Functions.fixIp(query);
 				queryValue = query;
 				list = app.ipSearch(query, offset, limit, total);
 			} else {
 				log.finest("Buscando Alias " + query);
 				if (validPlayerNameChars(query)) {
 					boolean[] exactMatch = new boolean[1];
 					exactMatch[0] = true;
 					list = app.aliasSearch(query, offset, limit, total, exactMatch);
 					if (!exactMatch[0] && list.size() > 0) {
 						Flash.info(
 								req,
 								"No se encontraron resultados precisos. "
 										+ "Los resultados mostrados son variaciones del nombre.");
 						if (total[0] > Parameters.MAX_NGRAM_QUERY / 2) {
 							Flash.warn(req, "Su búsqueda arroja demasiados resultados."
 									+ " Por favor, sea más específico.");
 						}
 					}
 				} else
 					Flash.error(req, "Consulta inválida. Caracteres inválidos.");					
 			}
		} else {
			log.fine("Empty");
			list = app.rootQuery(offset, limit, total);
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
 
 	private static boolean validPlayerNameChars(String query) {
 		if (query == null)
 			return false;
 
 		for (int i = 0; i < query.length(); i++)
 			if (!validPlayerNameChar(query.charAt(i)))
 				return false;
 		return true;
 	}
 
 	private static boolean validPlayerNameChar(char c) {
 		// Continuously improve this.
 		return c < 256 && c != ' ';
 	}
 }
