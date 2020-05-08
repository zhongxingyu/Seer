 package jipdbs.web.processors;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import jipdbs.core.JIPDBS;
 import jipdbs.core.PageLink;
 import jipdbs.core.Parameters;
 import jipdbs.core.util.Functions;
 import jipdbs.core.util.Validator;
 import jipdbs.info.SearchResult;
 import jipdbs.web.CommonConstants;
 import jipdbs.web.Flash;
 
 import org.apache.commons.lang.StringUtils;
 
 import ar.sgt.resolver.exception.ProcessorException;
 import ar.sgt.resolver.processor.ResolverContext;
 
 public class SearchProcessor extends FlashResponseProcessor {
 
 	private static final Logger log = Logger.getLogger(SearchProcessor.class
 			.getName());
 
 	@Override
 	public String processProcessor(ResolverContext context) throws ProcessorException {
 
 		HttpServletRequest req = context.getRequest();
 
 		JIPDBS app = (JIPDBS) context.getServletContext()
 				.getAttribute("jipdbs");
 
 		int page = 1;
 		int pageSize = CommonConstants.DEFAULT_PAGE_SIZE;
 		String query = null;
 		String type = null;
 		String mode = null;
 		
 		if (context.getParameter("query") != null) {
 			query = context.getParameter("query");
 		}
 		if (context.getParameter("type") != null) {
 			type = context.getParameter("type");
 		}
 		if (req.getParameter("mode") != null) {
 			mode = req.getParameter("mode");
 		}		
 		if (req.getParameter("p") != null) {
 			try {
 				page = Integer.parseInt(req.getParameter("p"));
 			} catch (NumberFormatException e) {
 			}
 		}
 		if (req.getParameter("ps") != null) {
 			try {
 				pageSize = Integer.parseInt(req.getParameter("ps"));
 			} catch (NumberFormatException e) {
 			}
 		}
 
 		int offset = (page - 1) * pageSize;
 		int limit = Math.min(pageSize, Parameters.MAX_ENTITY_LIMIT);
 		int[] total = new int[1];
 
 		String queryValue = query;
 
 		long time = System.currentTimeMillis();
 
 		List<SearchResult> list = new ArrayList<SearchResult>();
 
 		if ("server".equals(type)) {
 			log.fine("Server filter");
 			queryValue = "";
 			list = app.byServerSearch(query, offset, limit, total);
 		} else if ("ban".equals(type)) {
 			log.fine("Ban list");
 			list = app.bannedQuery(offset, limit, total);
 		} else if (StringUtils.isEmpty(query)) {
 			log.fine("Empty");
 			list = app.rootQuery(offset, limit, total);
		} else if (Validator.isValidIp(query)) {
 			log.finest("Buscando IP " + query);
 			query = Functions.fixIp(query);
 			queryValue = query;
 			list = app.ipSearch(query, offset, limit, total);
 		} else if (Validator.isValidClientId(query)) {
 			log.finest("Buscando Client ID " + query);
 			list = app.clientIdSearch(query.substring(1), offset, limit, total);
 		} else {
 			log.finest("Buscando Alias " + query);
 			if (Validator.isValidPlayerName(query)) {
 				boolean[] exactMatch = new boolean[1];
 				exactMatch[0] = true;
 				list = app.aliasSearch(query, offset, limit, total, exactMatch);
 				if (!exactMatch[0] && list.size() > 0) {
 					Flash.info(
 							req,
 							"No se encontraron resultados precisos. "
 									+ "Los resultados mostrados son variaciones del nombre.");
 					if (total[0] > Parameters.MAX_NGRAM_QUERY / 2) {
 						Flash.warn(req,
 								"Su búsqueda arroja demasiados resultados."
 										+ " Por favor, sea más específico.");
 					}
 				}
 			} else
 				Flash.error(req, "Consulta inválida. Caracteres inválidos.");
 		}
 
 		time = System.currentTimeMillis() - time;
 
 		int totalElements = Math.min(Parameters.MAX_ENTITY_LIMIT, total[0]);
 
 		int totalPages = (int) Math.ceil((double) totalElements / pageSize);
 
 		req.setAttribute("list", list);
 		req.setAttribute("count", totalElements);
 		req.setAttribute("time", time);
 		req.setAttribute("queryValue", queryValue);
 		req.setAttribute("query", query);
 		req.setAttribute("pageLink", new PageLink(page, pageSize, totalPages));
 
 		if ("code".equals(mode)) return "/data/search_bbcode.jsp";
 		return null;
 	}
 
 }
