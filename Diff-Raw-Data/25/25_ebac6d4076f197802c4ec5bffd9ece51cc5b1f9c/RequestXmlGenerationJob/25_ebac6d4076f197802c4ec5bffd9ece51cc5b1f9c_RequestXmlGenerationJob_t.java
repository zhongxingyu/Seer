 package fr.cg95.cvq.service.request.job;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Type;
 import fr.cg95.cvq.business.external.ExternalServiceTrace;
 import fr.cg95.cvq.business.external.TraceStatusEnum;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.DateUtils;
 
 /**
  * This job is parametrized to generate and erase traced 
  * request xml files.
  * 
  * @author vba@zenexity.fr
  *
  */
public class RequestXmlGenerationJob implements BeanFactoryAware {
 
     private IRequestService requestService;
     private ILocalAuthorityRegistry localAuthorityRegistry;
     private IExternalService externalService;
    private BeanFactory beanFactory;

    public void init() {
        requestService =
            (IRequestService)beanFactory.getBean("defaultRequestService");
    }
 
     public void launchJob() {
         localAuthorityRegistry
             .browseAndCallback(this, "performGeneration", null);
         localAuthorityRegistry
             .browseAndCallback(this, "eraseAcknowledgedRequests", null);
     }
 
     public void performGeneration()
         throws CvqException {
         Set<Critere> criteriaSet = new HashSet<Critere>(2);
         criteriaSet.add(new Critere("requestType.label",
             externalService.getGenerableRequestTypes(), Critere.IN));
         criteriaSet.add(new Critere("validationDate",
             DateUtils.getShiftedDate(Calendar.DAY_OF_YEAR, -2), Critere.GTE));
         Critere statusCritere =
             new Critere(ExternalServiceTrace.SEARCH_BY_STATUS,
                 TraceStatusEnum.ACKNOWLEDGED, Critere.EQUALS);
         for (Request r : requestService.get(criteriaSet, null, null, 0, 0)) {
             criteriaSet.clear();
             criteriaSet.add(statusCritere);
             criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
                 String.valueOf(r.getId()), Critere.EQUALS));
             if (!xmlFileExists(r.getId())
                 && externalService.getTraces(criteriaSet, null, null)
                     .isEmpty()) {
                 localAuthorityRegistry.saveLocalAuthorityResource(
                     Type.REQUEST_XML, String.valueOf(r.getId()),
                     r.modelToXmlString().getBytes());
             }
         }
     }
     
     public void eraseAcknowledgedRequests() {
         Set<Critere> criteres = new HashSet<Critere>();
         criteres.add(new Critere(ExternalServiceTrace.SEARCH_BY_STATUS,
             TraceStatusEnum.ACKNOWLEDGED, Critere.EQUALS));
         for (ExternalServiceTrace t :
             externalService.getTraces(criteres, null, null)) {
             localAuthorityRegistry.removeLocalAuthorityResource(
                 Type.REQUEST_XML, t.getKey());
         }
     }
     
     protected boolean xmlFileExists(Long id) {
         File file = localAuthorityRegistry.getLocalAuthorityResourceFile(
             Type.REQUEST_XML, String.valueOf(id), false);
         if (file == null) return false;
         else return file.exists();
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setExternalService(IExternalService externalService) {
         this.externalService = externalService;
     }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
 }
