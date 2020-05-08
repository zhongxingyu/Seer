 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.mail.Authenticator;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 
 import com.google.gson.GsonBuilder;
 
 import edu.mssm.pharm.maayanlab.HibernateUtil;
 import edu.mssm.pharm.maayanlab.JSONify;
 import edu.mssm.pharm.maayanlab.math.HashFunctions;
 
 @WebServlet(urlPatterns = {"/account", "/login", "/register", "/forgot", "/reset", "/status", "/logout"})
 public class Account extends HttpServlet {
 	
 	private static final long serialVersionUID = 19776535963654466L;
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		JSONify json = new JSONify(new GsonBuilder().registerTypeAdapter(List.class, new ListAdapter()).create());
 		
 		HttpSession httpSession = request.getSession();
 		
 		User user = (User) httpSession.getAttribute("user");
 		
 		if (request.getServletPath().equals("/logout")) {
 			httpSession.removeAttribute("user");
 			response.sendRedirect("");
 			return;
 		}
 		
 		if (user == null) {
 			json.add("user", "");
 		}
 		else {
 			json.add("user", (user.getFirst() != null) ? user.getFirst() : user.getEmail());	// Display name
 			
 			// Get user lists
 			if (request.getServletPath().equals("/account")) {
 				SessionFactory sf = HibernateUtil.getSessionFactory();
 				Session session = null;
 				try {
 					session = sf.getCurrentSession();
 				} catch (HibernateException he) {
 					session = sf.openSession();
 				}
 				session.beginTransaction();
 				
 				session.update(user);
 				json.add("lists", user.getLists());
 				
 				session.getTransaction().commit();
 				session.close();
 			}
 		}
 		
 		json.write(response.getWriter());
 	}
 	
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// Create database session
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Session session = sf.openSession();
 		session.beginTransaction();
 		
 		boolean success;
 		if (request.getServletPath().equals("/register"))
 			success = register(session, request, response);
 		else if (request.getServletPath().equals("/forgot"))
 			success = forgot(session, request, response);
 		else if (request.getServletPath().equals("/reset"))
 			success = reset(session, request, response);
 		else
 			success = login(session, request, response);
 		
 		session.getTransaction().commit();
 		session.close();
 		
 		if (request.getServletPath().equals("/forgot") && success) {
 			response.setContentType("text/plain");
 			response.getWriter().print("success");
 		}
 		else if (request.getServletPath().equals("/reset") && success)
 			response.sendRedirect("login.html");
 		else if (success)
 			response.sendRedirect("account.html");
 		else
 			request.getRequestDispatcher("account-error.jsp").forward(request, response);
 	}
 	
 	private boolean register(Session session, HttpServletRequest request, HttpServletResponse response) {
 		String email = request.getParameter("email");
 		
 		// Check for existing email
 		Criteria criteria = session.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user != null) {	// If exists, throw error
 			request.setAttribute("error", "The email you entered is already registered.");
 			return false;
 		}
 		else {	// Else, create user
 			User newUser = new User(email, 
 									request.getParameter("password"),
 									request.getParameter("firstname"), 
 									request.getParameter("lastname"), 
 									request.getParameter("institution"));
 			session.save(newUser);				
 			request.getSession().setAttribute("user", newUser);
 			return true;
 		}
 	}
 	
 	private boolean login(Session session, HttpServletRequest request, HttpServletResponse response) {
 		String email = request.getParameter("email");
 		String password = request.getParameter("password");
 		
 		Criteria criteria = session.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user == null) {
 			request.setAttribute("error", "The email you entered does not belong to a registered user.");
 			return false;
 		}
 		else {
 			if (user.checkPassword(password)) {
 				user.updateAccessed();
 				session.update(user);
 				request.getSession().setAttribute("user", user);
 				return true;
 			}
 			else {
 				request.setAttribute("error", "The password you entered is incorrect.");
 				return false;
 			}
 		}
 	}
 	
 	private boolean forgot(Session session, HttpServletRequest request, HttpServletResponse response) {
 		String email = request.getParameter("email");
 		Criteria criteria = session.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user == null) {
 			request.setAttribute("error", "The email you entered does not belong to a registered user.");
 			return false;
 		}
 		
 		// One day token for password reset
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		String token = user.getEmail() + user.getSalt() + formatter.format(Calendar.getInstance().getTime());
 		token = HashFunctions.md5(token);
 		
 		Properties props = new Properties();
 		props.put("mail.smtp.host", "mail.maayanlab.net");
 		props.put("mail.smtp.auth", "true");
 		javax.mail.Session mailSession = javax.mail.Session.getInstance(props, new Authenticator() {
 			@Override
 			public PasswordAuthentication getPasswordAuthentication() {
 				return new PasswordAuthentication("amp@maayanlab.net", "1amp1");
 			}
 		});
 		
 		MimeMessage message = new MimeMessage(mailSession);
 		try {
 			message.setFrom(new InternetAddress("Enrichr@amp.pharm.mssm.edu"));
 			message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
 			message.setSubject("Enrichr Password Reset");
 			message.setSentDate(new Date());
			message.setText("Reset your password at http://amp.pharm.mssm.edu/Enrichr/reset.html?user=" + email + "&token=" + token + ".\n\nIf you did not request this password request, please ignore this email.");
 			Transport.send(message);
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		}
 		
 		return true;
 	}
 	
 	private boolean reset(Session session, HttpServletRequest request, HttpServletResponse response) {
 		String email = request.getParameter("email");
 		Criteria criteria = session.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user == null) {
 			request.setAttribute("error", "The email you entered does not belong to a registered user.");
 			return false;
 		}
 		
 		// Generate today and yesterday's tokens
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		Calendar calendar = Calendar.getInstance();
 		String today = user.getEmail() + user.getSalt() + formatter.format(calendar.getTime());
 		calendar.add(Calendar.DATE, -1);
 		String yesterday = user.getEmail() + user.getSalt() + formatter.format(calendar.getTime());
 		
 		String token = request.getParameter("token");		
 		if (!token.equalsIgnoreCase(HashFunctions.md5(today)) && !token.equalsIgnoreCase(HashFunctions.md5(yesterday))) {
 			request.setAttribute("error", "Your reset token has expired. Please visit the Forgot Your Password? page again to request a new one.");
 			return false;
 		}
 		
 		user.updatePassword(request.getParameter("password"));
 		session.update(user);
 		
 		return true;
 	}
 	
 	// Static function to commit new lists to the user so the Enrichr class doesn't make any db calls
 	static void updateUser(User user) {
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Session session = null;
 		try {
 			session = sf.getCurrentSession();
 		} catch (HibernateException he) {
 			session = sf.openSession();
 		}
 		session.beginTransaction();
 		session.update(user);
 		session.getTransaction().commit();
 		session.close();
 	}
 }
