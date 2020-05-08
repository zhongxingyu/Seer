 package uk.ac.cam.dashboard.util;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HibernateSessionRequestFilter implements Filter {
 
 	public static final String SESSION_FACTORY = HibernateSessionRequestFilter.class
 			.getName() + ".SessionFactory";
 
 	private static Logger log = LoggerFactory
 			.getLogger(HibernateSessionRequestFilter.class);
 
 	private SessionFactory sf;
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		log.debug("Initializing filter...");
 		sf = HibernateUtil.getSF();
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 //		sf.getCurrentSession().beginTransaction();
 		chain.doFilter(request, response);
 		
 		Transaction transaction = sf.getCurrentSession().getTransaction();
 
        if (transaction.isActive()) {
        	try {
        		sf.getCurrentSession().getTransaction().commit();
        	}
        	catch (HibernateException e) {
        		sf.getCurrentSession().getTransaction().rollback();
        		throw e;
        	}
        }
 	}
 
 	@Override
 	public void destroy() {
 	}
 
 	public static Session openSession(HttpServletRequest request) {
 		SessionFactory factory = (SessionFactory) request
 				.getAttribute(SESSION_FACTORY);
 		return factory.openSession();
 	}
 }
