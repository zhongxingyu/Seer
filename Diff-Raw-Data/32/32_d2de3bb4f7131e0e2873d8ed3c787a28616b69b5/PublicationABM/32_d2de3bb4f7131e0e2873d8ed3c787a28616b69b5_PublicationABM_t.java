 package web;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import services.ComboService;
 import services.PublicationService;
 import transfer.bussiness.Publication;
 import transfer.forms.Combo;
 import transfer.forms.PublicationForm;
 
 public class PublicationABM extends HttpServlet {
 
 	PublicationService ps = PublicationService.getInstance();
 	PublicationForm pf;
 	ComboService cs;
 	List<Combo> typeList;
 	List<Combo> oTypeList;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		Publication p;
 		pf = new PublicationForm();
 		cs = ComboService.getInstance();
 		typeList = cs.getTypes();
 		oTypeList = cs.getOperationTypes();
 
 		if (req.getParameter("pid") != null) {
 			int publicationId = Integer.valueOf(req.getParameter("pid"));
 			p = ps.getPublication(publicationId);
 			if(p.getUserId() == (Integer)req.getSession().getAttribute("userId"))
 				pf = new PublicationForm(p);
 		} else {
 
 		}
		System.out.println(pf.isCable());
 		req.setAttribute("publication", pf);
 		req.setAttribute("typeList", typeList);
 		req.setAttribute("oTypeList", oTypeList);
 		req.getRequestDispatcher("/WEB-INF/jsp/publicationDetail.jsp").forward(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		pf = new PublicationForm(Integer.valueOf(req
 				.getParameter("publicationId")), Integer.valueOf(req
 				.getParameter("userId")), Integer.valueOf(req
 				.getParameter("type")), Integer.valueOf(req
 				.getParameter("operation_type")), req.getParameter("address"),
 				req.getParameter("city"), req.getParameter("price"),
 				Integer.valueOf(req.getParameter("environments")),
 				req.getParameter("covered"), req.getParameter("uncovered"),
 				req.getParameter("age"), Boolean.valueOf(req
 						.getParameter("cable")), Boolean.valueOf(req
 						.getParameter("phone")), Boolean.valueOf(req
 						.getParameter("pool")), Boolean.valueOf(req
 						.getParameter("living")), Boolean.valueOf(req
 						.getParameter("paddle")), Boolean.valueOf(req
 						.getParameter("barbecue")),
 				req.getParameter("description"));
 		List<String> errors = pf.validate();
 		if( !errors.isEmpty() ){
 			cs = ComboService.getInstance();
 			typeList = cs.getTypes();
 			oTypeList = cs.getOperationTypes();
 			req.setAttribute("errors", errors);
 			req.setAttribute("publication", pf);
 			req.setAttribute("typeList", typeList);
 			req.setAttribute("oTypeList", oTypeList);
 			req.getRequestDispatcher("/WEB-INF/jsp/publicationDetail.jsp").forward(req, resp);
 		}else{
 			int userId = (Integer)req.getSession().getAttribute("userId");
 			ps.save(pf.toBussiness(), userId);
 			resp.sendRedirect("publicationList");
 		}
 		
 	}
 
 }
