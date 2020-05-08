 package controller.user_admin;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import model.User;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import util.HibernateUtil;
 import util.HttpServlet.HttpServlet;
 import util.form.user.UpdateForm;
 
 @SuppressWarnings("serial")
 public class Delete extends HttpServlet {
 
 	private Session sessionHibernate;
 	private Transaction tx;
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 
 		sessionHibernate = HibernateUtil.currentSession();
 		tx = sessionHibernate.beginTransaction();
 		
		User user = (User)sessionHibernate.load(User.class, new Integer(request.getParameter("id")));
 		
 		sessionHibernate.delete(user);
 
 		tx.commit();
 
 		HibernateUtil.closeSession();
 
 		response.sendRedirect("user-admin-list");
 	}
 }
