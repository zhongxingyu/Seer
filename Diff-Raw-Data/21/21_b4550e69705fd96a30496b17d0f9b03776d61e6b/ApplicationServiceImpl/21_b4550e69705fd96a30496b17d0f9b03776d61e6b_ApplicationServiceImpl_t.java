 package no.niths.services.development;
 
import java.util.ArrayList;
 import java.util.List;
 
import no.niths.common.helpers.LazyFixer;
 import no.niths.domain.development.Application;
 import no.niths.domain.development.Developer;
 import no.niths.infrastructure.development.interfaces.ApplicationRepository;
 import no.niths.infrastructure.interfaces.GenericRepository;
 import no.niths.services.AbstractGenericService;
 import no.niths.services.development.interfaces.ApplicationService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * Service Class for Application
  *
  * <p>
  * Inherits the basic CRUD actions and has methods
  * for getByApplicationKey, getTopApps,
  * enableApplication and disableApplication
  * </p>
  */
 @Service
 public class ApplicationServiceImpl extends AbstractGenericService<Application>
 		implements ApplicationService {
 	
 	private static final Logger logger = LoggerFactory
 			.getLogger(ApplicationServiceImpl.class);
 	
	private LazyFixer<Application> lazyFixer = new LazyFixer<Application>();
	
 	@Autowired
 	private ApplicationRepository repo;
 
 	@Override
 	@Deprecated
 	public Application getByApplicationToken(String token) {
 		return repo.getByApplicationToken(token);
 	}
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	public List<Application> getTopApps(int maxResults){
 		return repo.getTopApps(maxResults);
 	}
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	public Application getByApplicationKey(String key, boolean enabled) {
		Application app = repo.getByApplicationKey(key, enabled);
		ArrayList<Application> ts = new ArrayList<Application>();
	    ts.add(app);
	    lazyFixer.fetchChildren(ts);
	    return ts.get(0);

 	}
 
 	@Override
 	public GenericRepository<Application> getRepository() {
 		return repo;
 	}
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	public void enableApplication(Long applicationId) {
 		Application applications = validate(repo.getById(applicationId),
 				Application.class);
 		if ((applications.getEnabled() != null) || (!applications.getEnabled())) {
 			applications.setEnabled(true);
 			logger.debug("Application " + applications.getTitle() + " is enabled");
 		}
 		
 	}
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	public void disableApplication(Long applicationId) {
 		Application applicatipns = validate(repo.getById(applicationId),
 				Developer.class);
 		if (applicatipns.getEnabled() != null && applicatipns.getEnabled()) {
 			applicatipns.setEnabled(false);
 			logger.debug("Application " + applicatipns.getTitle() + " is disabled");
 		}
 		
 	}
 }
