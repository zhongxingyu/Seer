 package topshelf.utils.persist;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 import javax.persistence.EntityManager;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.google.inject.persist.Transactional;
 
 /**
  * Filter every servlet call through a Guice PersistService handled
  * @Transaction annotated method.
  * 
  * @author bloo
  *
  */
 @Singleton
 public class JpaTxnFilter implements Filter {
 
 	private Logger logger = Logger.getLogger(JpaTxnFilter.class);
 	private Provider<EntityManager> emf;
 	
 	@Inject
 	public JpaTxnFilter(Provider<EntityManager> emf) {
 		this.emf = emf;
 	}
 	
 	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}
 
 	@Override
 	@Transactional // this does all the work for us!
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		if (!emf.get().getTransaction().isActive()) {
 			logger.warn("Transaction not active!");
 		}
 		chain.doFilter(request, response);
 	}
 
 	@Override
	public void destroy() {}
 }
