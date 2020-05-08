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
 
 import edu.mssm.pharm.maayanlab.HibernateUtil;
 import edu.mssm.pharm.maayanlab.JSONify;
 import edu.mssm.pharm.maayanlab.math.HashFunctions;
 
 @WebServlet(urlPatterns = {"/account", "/login", "/register", "/forgot", "/reset", "/status", "/logout"})
 public class Account extends HttpServlet {
 	
 	private static final long serialVersionUID = 19776535963654466L;
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		JSONify json = new JSONify();
 		
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
			json.add("user", user.getEmail());
			json.add("firstname", user.getFirst());
 			
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
				
				json.add("lastname", user.getLast());
				json.add("institution", user.getInstitute());
 				json.add("lists", user.getLists());
 				
 				session.getTransaction().commit();
 				session.close();
 			}
 		}
 		
 		json.write(response.getWriter());
 	}
 	
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// All form submissions are ajax and take json responses
 		response.setContentType("application/json");
 		JSONify json = new JSONify();
 		
 		// Create database session
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Session dbSession = sf.openSession();
 		dbSession.beginTransaction();
 		
 		if (request.getServletPath().equals("/login"))
 			login(request, response, dbSession, json);
 		else if (request.getServletPath().equals("/register"))
 			register(request, response, dbSession, json);
 		else if (request.getServletPath().equals("/forgot"))
 			forgot(request, response, dbSession, json);
 		else if (request.getServletPath().equals("/reset"))			
 			reset(request, response, dbSession, json);
 		else if (request.getServletPath().equals("/account"))
 			modify(request, response, dbSession, json);
 		
 		// Close database session and write out json
 		dbSession.getTransaction().commit();
 		dbSession.close();		
 		json.write(response.getWriter());
 	}
 	
 	private void register(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
 		// Check for existing email
 		String email = request.getParameter("email");
 		Criteria criteria = dbSession.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user != null) {	// If exists, throw error
 			json.add("message", "The email you entered is already registered.");
 		}
 		else {	// Else, create user
 			User newUser = new User(email, 
 									request.getParameter("password"),
 									request.getParameter("firstname"), 
 									request.getParameter("lastname"), 
 									request.getParameter("institution"));
 			dbSession.save(newUser);
 			request.getSession().setAttribute("user", newUser);
 			json.add("redirect", "index.html");
 		}
 	}
 	
 	private void login(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
 		String email = request.getParameter("email");
 		String password = request.getParameter("password");
 		
 		Criteria criteria = dbSession.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user == null) {
 			json.add("message", "The email you entered does not belong to a registered user.");
 		}
 		else if (user.checkPassword(password)) {
 			user.updateAccessed();
 			dbSession.update(user);
 			request.getSession().setAttribute("user", user);
 			json.add("redirect", "account.html");
 		}
 		else {
 			json.add("message", "The password you entered is incorrect.");
 		}
 	}
 	
 	private void forgot(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
 		String email = request.getParameter("email");
 		Criteria criteria = dbSession.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user == null) {
 			json.add("message", "The email you entered does not belong to a registered user.");
 		}
 		else {
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
 				message.setText("Reset your password at http://amp.pharm.mssm.edu/Enrichr/reset.html?user=" + email + "&token=" + token + ".\n\nIf you did not request this password reset, please ignore this email.");
 				Transport.send(message);
 			} catch (MessagingException e) {
 				e.printStackTrace();
 			}
 			
 			json.add("message", "Password reset request sent! Check your email for a reset link.");
 		}
 	}
 	
 	private void reset(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
 		String email = request.getParameter("email");
 		Criteria criteria = dbSession.createCriteria(User.class)
 				.add(Restrictions.eq("email", email).ignoreCase());
 		User user = (User) criteria.uniqueResult();
 		
 		if (user == null) {
 			json.add("message", "The email you entered does not belong to a registered user.");			
 		}
 		else {
 			// Generate today and yesterday's tokens
 			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 			Calendar calendar = Calendar.getInstance();
 			String today = user.getEmail() + user.getSalt() + formatter.format(calendar.getTime());
 			calendar.add(Calendar.DATE, -1);
 			String yesterday = user.getEmail() + user.getSalt() + formatter.format(calendar.getTime());
 			
 			String token = request.getParameter("token");		
 			if (!token.equalsIgnoreCase(HashFunctions.md5(today)) && !token.equalsIgnoreCase(HashFunctions.md5(yesterday))) {
 				json.add("message", "Your reset token has expired. Please request a new one.");
 			}
 			else {
 				user.updatePassword(request.getParameter("password"));
 				dbSession.update(user);
 				json.add("redirect", "login.html");
 			}
 		}
 	}
 	
 	private void modify(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {		
 		User user = (User) request.getSession().getAttribute("user");
 		String password = request.getParameter("password");
 		
 		if (user == null) {
 			json.add("redirect", "login.html");
 		}
 		else if (user.checkPassword(password)) {
 			user.updateUser(request.getParameter("email"), 
							request.getParameter("newpassword"), 
							request.getParameter("firstname"), 
							request.getParameter("lastname"),
							request.getParameter("institution"));
 			dbSession.update(user);
 			json.add("message", "Changes saved.");
 		}
 		else {
 			json.add("message", "The password you entered is incorrect.");
 		}
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
