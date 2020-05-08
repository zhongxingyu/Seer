 package fr.cg95.cvq.service.request.job;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.DateUtils;
 import fr.cg95.cvq.util.localization.ILocalizationService;
 import fr.cg95.cvq.util.mail.IMailService;
 import fr.cg95.cvq.security.SecurityContext;
 import org.apache.log4j.Logger;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
 * Permits to manage drafts 
  *
  * @author Victor Bartel (vba@zenexity.fr)
  */
 
 public class DraftManagementJob {
     
     private IRequestDAO requestDAO;
     private IRequestService requestService;
     private Integer liveDuration;
     private Integer notificationBeforeDelete;
     private ILocalAuthorityRegistry localAuthorityRegistry;
     private IMailService mailService;
     private IIndividualService individualService;
     private ILocalizationService localizationService;
     private static Logger logger = Logger.getLogger(DraftManagementJob.class);
     
     public void deleteExpiredDrafts() throws CvqException {
         Set<Critere> criterias = this.prepareQueryParams(liveDuration);
         List<Request> requests = this.requestDAO.search(criterias,null,null,0,0);
         for(Request r : requests) this.requestService.delete(r.getId());
     }
     
     public Integer sendNotifications() throws CvqException {
         Integer counter = 0; 
         Integer limit = liveDuration - notificationBeforeDelete;
         
         List<Request> requests = this.requestDAO.listByDraftNotification(
             IRequestService.DRAFT_DELETE_NOTIFICATION,
            DateUtils.getShiftedDate(Calendar.DAY_OF_YEAR, -limit));
         
         for(Request r : requests) {
             Adult adult = this.individualService.getAdultById(r.getRequesterId());
             String from = r.getRequestType().getCategory().getPrimaryEmail();
             boolean sent = false;
             
             try {
                mailService.send(from, adult.getEmail(), null,
                        "[CapDémat] Expiration d'une demande sauvée en tant que brouillon",
                        this.buildMailTemplate(r));
                 sent = true;
                 counter ++;
             } catch(CvqException e) {
                 logger.error("sendNotifications() "+e.getMessage());
             } finally {
                 if(sent)
                     requestService.addSystemAction(r.getId(),
                         IRequestService.DRAFT_DELETE_NOTIFICATION);
             }
         }
         return counter;
     }
     
     protected String buildMailTemplate(Request request) throws CvqException {
         String template = this.localAuthorityRegistry.getBufferedCurrentLocalAuthorityResource(
             ILocalAuthorityRegistry.TXT_ASSETS_RESOURCE_TYPE,
             "NotificationBeforeDraftDelete.txt",
             false
         );
         
         template = template.replace("${requestType}", 
             localizationService.getRequestLabelTranslation(
                 request.getClass().getName(), "fr", false));
         template = template.replace("${requestId}",request.getId().toString());
         template = template.replace("${creationDate}", DateUtils.format(request.getCreationDate()));
         template = template.replace("${expirationDate}",
             DateUtils.format(DateUtils.getShiftedDate(Calendar.DAY_OF_YEAR,liveDuration+1))
         );
         
         return template;
     }
     
     protected Set<Critere> prepareQueryParams(Integer dateInterval) {
         Set<Critere> criterias = new HashSet<Critere>();
         
         Critere criteria = new Critere();
         criteria.setAttribut(Request.SEARCH_BY_CREATION_DATE);
         criteria.setComparatif(Critere.LTE);
        criteria.setValue(DateUtils.getShiftedDate(Calendar.DAY_OF_YEAR, -dateInterval));
         criterias.add(criteria);
         
         criteria = new Critere();
         criteria.setAttribut(Request.DRAFT);
         criteria.setComparatif(Critere.EQUALS);
         criteria.setValue(true);
         criterias.add(criteria);
         
         return criterias;
     }
 
     public void setRequestService(IRequestService requestService) {
         this.requestService = requestService;
     }
 
     public void setLiveDuration(Integer liveDuration) {
         this.liveDuration = liveDuration;
     }
 
     public void setNotificationBeforeDelete(Integer notificationBeforeDelete) {
         this.notificationBeforeDelete = notificationBeforeDelete;
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setMailService(IMailService mailService) {
         this.mailService = mailService;
     }
 
     public void setIndividualService(IIndividualService individualService) {
         this.individualService = individualService;
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setLocalizationService(ILocalizationService localizationService) {
         this.localizationService = localizationService;
     }
 }
