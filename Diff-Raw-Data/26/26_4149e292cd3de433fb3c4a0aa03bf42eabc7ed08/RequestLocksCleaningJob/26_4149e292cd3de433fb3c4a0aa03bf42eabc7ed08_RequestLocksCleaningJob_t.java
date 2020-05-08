 package fr.cg95.cvq.service.request.job;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 
import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.IRequestService;
 
 /**
  * Remove obsolete request locks from memory and db
  * 
  * @author jsb@zenexity.fr
  *
  */
 public class RequestLocksCleaningJob implements BeanFactoryAware {
 
    private ILocalAuthorityRegistry localAuthorityRegistry;
     private IRequestService requestService;
     private BeanFactory beanFactory;
 
     public void init() {
         requestService =
             (IRequestService)beanFactory.getBean("defaultRequestService");
     }
 
     public void launchJob() {
        localAuthorityRegistry.browseAndCallback(this, "cleanRequestLocks", null);
    }

    public void cleanRequestLocks() {
         requestService.cleanRequestLocks();
     }
 
     @Override
     public void setBeanFactory(BeanFactory beanFactory)
         throws BeansException {
         this.beanFactory = beanFactory;
     }

    public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
        this.localAuthorityRegistry = localAuthorityRegistry;
    }
 }
