 package web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.BSMap;
 import model.BSRun;
 import model.STATUS;
 
 import common.HibernateUtil;
 
 /**
  * Displays matches for a given run aggregated by map
  * @author stevearc
  *
  */
 public class MatchesByMapServlet extends HttpServlet {
 	private static final long serialVersionUID = 5352256649398067730L;
 	public static final String NAME = "/matches_by_map.html";
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/html");
 		response.setStatus(HttpServletResponse.SC_OK);
 		PrintWriter out = response.getWriter();
 		out.println("<html><head>");
 		out.println("<title>Battlecode Tester</title>");
 		out.println("<link rel=\"stylesheet\" href=\"css/table.css\" />");
 		out.println("<link rel='stylesheet' href='/css/jquery-ui-1.8.16.custom.css' />");
 		out.println("<link rel='stylesheet' href='/css/jquery-ui.css' />");
 		out.println("</head>");
 		out.println("<body style='background:none'>");
 
 		String strId = request.getParameter("id");
 		if (strId == null || !strId.matches("\\d+")) {
 			out.println("Invalid id</body></html>");
 			return;
 		}
 		Long id = new Long(Integer.parseInt(strId));
 		EntityManager em = HibernateUtil.getEntityManager();
 		BSRun run = em.find(BSRun.class, id);
 
 		out.println("<table id=\"matches_table\" class='datatable'>" +
 				"<thead>" + 
 				"<tr>" +
 				"<th>Map</th>" +
 				"<th>Win %</th>" +
 				"<th>Size</th>" +
 				"</tr>" +
 				"</thead>" +
 		"<tbody>");
 		List<Object[]> valuePairs = em.createQuery("select map, match.result.winner, count(*) from BSMatch match inner join match.map as map where match.run = ?" +
 				"and match.status = ? group by map, match.result.winner order by map, match.result.winner", Object[].class)
 				.setParameter(1, run)
 				.setParameter(2, STATUS.COMPLETE)
 				.getResultList();
 		for (int i = 0; i < valuePairs.size(); i++) {
 			Object[] valuePair = valuePairs.get(i);
 			BSMap map = (BSMap) valuePair[0];
			long aCount = (Long) valuePair[2];
 			long bCount = 0;
			if (i + 1 < valuePairs.size() && map.equals(valuePairs.get(i+1)[0])) {
				bCount = (Long) valuePairs.get(i+1)[2];
				i++;
 			}
 			out.println("<tr>");
 			out.println("<td>" + map.getMapName() + "</td>");
 			out.println("<td>" + WebUtil.getFormattedWinPercentage((double)aCount/(aCount+bCount)) + "</td>");
 			out.println("<td>" + map.getSize() + "</td>");
 			out.println("</tr>");
 		}
 		out.println("</tbody>");
 		out.println("</table>");
 
 		out.println("<script src='/js/jquery-1.7.1.min.js'></script>");
 		out.println("<script src='/js/jquery-ui-1.8.16.custom.min.js'></script>");
 		out.println("<script src='js/jquery.dataTables.min.js'></script>");
 		out.println("<script type=\"text/javascript\" src=\"js/matches_by_map.js\"></script>");
 		out.println("</body></html>");
 	}
 }
