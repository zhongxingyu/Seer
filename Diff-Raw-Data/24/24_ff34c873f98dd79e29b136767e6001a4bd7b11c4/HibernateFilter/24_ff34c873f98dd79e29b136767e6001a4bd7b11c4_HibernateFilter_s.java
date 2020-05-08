 package yfsc.infrastructure;
 
 import java.io.IOException;
 import javax.servlet.*;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class HibernateFilter implements Filter {
     
    private SessionFactory sessionFactory;
     
     @Autowired
    public HibernateFilter(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         Session session = sessionFactory.getCurrentSession();
        if (session.getTransaction().isActive()) {
			session.getTransaction().commit();
		}
 		session.beginTransaction();
         
         chain.doFilter(request, response);
         
        session.getTransaction().commit();
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException { }
     @Override
     public void destroy() { }
 }
