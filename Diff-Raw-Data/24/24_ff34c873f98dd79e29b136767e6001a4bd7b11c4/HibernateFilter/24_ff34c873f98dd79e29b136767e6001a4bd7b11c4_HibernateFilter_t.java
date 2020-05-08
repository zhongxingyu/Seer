 package yfsc.infrastructure;
 
 import java.io.IOException;
 import javax.servlet.*;
import java.util.logging.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class HibernateFilter implements Filter {
     
    private final SessionFactory sessionFactory;
	private final Logger logger;
     
     @Autowired
    public HibernateFilter(SessionFactory sessionFactory, Logger logger) {
         this.sessionFactory = sessionFactory;
		this.logger = logger;
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         Session session = sessionFactory.getCurrentSession();
		
 		session.beginTransaction();
         
         chain.doFilter(request, response);
         
		try {
			session.getTransaction().commit();
		}
		catch (Exception e) {
			logger.warning("Rolling back Hibernate transaction");
			session.getTransaction().rollback();
		}
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException { }
     @Override
     public void destroy() { }
 }
