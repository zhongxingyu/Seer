 package controller.user_admin;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.User;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import util.HibernateUtil;
 import util.form.user.RegisterForm;
 
 @SuppressWarnings("serial")
 public class Add extends HttpServlet {
 
 	private Session session;
 	private Transaction tx;
 
 	private RegisterForm form;
 
 	protected void before(HttpServletRequest request, HttpServletResponse response) {
 		this.form = new RegisterForm();
 		form.setRequest(request);
 	}
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 		
 		// Creation de notre objet Session grace � notre HibernateUtil
 		session = HibernateUtil.currentSession();
 
 		request.setAttribute("languages", session.createQuery("from Language").list());
 
 		HibernateUtil.closeSession();
 
 		request.setAttribute("form", form);
 		RequestDispatcher dispatch = request
 				.getRequestDispatcher("WEB-INF/src/view/user_admin/add.jsp");
 		dispatch.forward(request, response);
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 		
 		if (form.validate()) {
 			session = HibernateUtil.currentSession();
 			tx = session.beginTransaction();
 			
 			//Création de l'utilisateur
 			User user = new User();
 			user.setIdUser(null);
 			form.fillUser(user,session);
 
 			session.save(user);
 
 			tx.commit();
 
 			HibernateUtil.closeSession();
 
 			response.sendRedirect("user-admin-list");
 		} else {
 			doGet(request, response);
 		}
 
 	}
 }
