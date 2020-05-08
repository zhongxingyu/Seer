 package nextgenforreal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nextgenforreal.utilities.dataservice.Company;
 import nextgenforreal.utilities.dataservice.PMF;
 
 public class SecurityLookupServlet extends HttpServlet {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger logger = Logger.getLogger(
 			SendmsgServlet.class.getName()
 	);
 	
 	@SuppressWarnings("unchecked")
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
 		String cb = req.getParameter("callback");
 		String queryParam = req.getParameter("name_startsWith");
 		
 		res.setContentType("application/json");
 		res.setStatus(HttpServletResponse.SC_OK);
 		
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(Company.class);
 		List<Company> results = null;
 		if(queryParam != null && !queryParam.equals("")) {
 			try {
 				q.setFilter("fullCompanyName.startsWith(queryParam)");
 				q.declareParameters("String queryParam");
 				q.setRange(0, 10);
 				results = (List<Company>) q.execute(queryParam);
 			} finally {
 				q.closeAll();
 			}
 		}
 		
 		results = results == null ? new ArrayList<Company>() : results;
 		StringBuffer sb = new StringBuffer(String.format("%s({\"securities\":[", cb));
 		int count = 0;
 		for(Company result : results) {
			sb.append(String.format("{label: \"%s\", value: \"%s\"}", result.getCompanyTicker(), result.getCompanyName()));
 			if(count < results.size() - 1) {
 				sb.append(",");
 				++count;
 			}
 		}
 		sb.append("]});");
 		
 		res.getWriter().write(sb.toString());
 		res.getWriter().flush();
 	}
 }
