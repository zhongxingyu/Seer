 package ca.idrc.tagin.tags;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 
 import ca.idrc.tagin.tags.dao.TagsDao;
 import ca.idrc.tagin.tags.dao.TagsEntityManager;
 
 @SuppressWarnings("serial")
 public class TagsServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String urn = req.getParameter("urn");
 		String label = req.getParameter("label");

		if (urn != null && label != null && !urn.isEmpty() && !label.isEmpty()) {
			TagsDao dao = new TagsEntityManager();
			dao.assignLabel(urn, label);
			dao.close();
			resp.setContentType("application/json");
			resp.getWriter().println(label);
		}
 	}
 }
