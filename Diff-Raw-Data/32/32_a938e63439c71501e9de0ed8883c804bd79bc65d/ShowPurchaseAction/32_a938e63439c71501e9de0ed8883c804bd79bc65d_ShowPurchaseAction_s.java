 package net.malta.web.app;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.enclosing.util.HibernateSession;
 import net.malta.model.Item;
 import net.malta.model.Purchase;
 import net.malta.model.Purchase;
 import net.malta.model.PurchaseImpl;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 public class ShowPurchaseAction extends Action {
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest req, HttpServletResponse res) throws Exception {
 
 		Session session = new HibernateSession().currentSession(this
 				.getServlet().getServletContext());
 //
 //		Purchase purchase = new PurchaseImpl();
//		Criteria criteria = session.createCriteria(Purchase.class);
//		if (req.getParameter("id") != null
//				&& !req.getParameter("id").equals("")) {
//			criteria.add(Restrictions.idEq(Integer.valueOf(req
//					.getParameter("id"))));
//			purchase = (Purchase) criteria.uniqueResult();
//
//		} else if (req.getAttribute("id") != null
//				&& !req.getAttribute("id").equals("")) {
//			criteria.add(Restrictions.idEq(req.getAttribute("id")));
//			purchase = (Purchase) criteria.uniqueResult();
//		}
 //		
 		Purchase purchase = (Purchase)req.getSession().getAttribute("purchase");
 		
 //		Criteria criteriaitem=session.createCriteria(Item.class);
 //		criteriaitem.createCriteria("choises").add(Restrictions.eq("purchase", purchase));
 //		criteriaitem.addOrder(Order.desc("stocknum"));
 //		req.setAttribute("items", criteriaitem.list());
 		
 //		System.out.println("ss"+criteriaitem.list());
 		
 //		req.setAttribute("purchase",purchase);
 
 //		req.setAttribute("purchase", purchase);
 //
 		return mapping.findForward("success");
 	}
 }
