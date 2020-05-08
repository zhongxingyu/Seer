 package fr.cg95.cvq.service.request.job;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Type;
 import fr.cg95.cvq.business.external.ExternalServiceTrace;
 import fr.cg95.cvq.business.external.TraceStatusEnum;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.users.CreationBean;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.testtool.ServiceTestCase;
 
 public class RequestXmlGenerationJobTest extends ServiceTestCase {
 
     private IExternalService           externalService;
     private RequestXmlGenerationJob        generationJob;
     private CreationBean                   creationBean;
     private IExternalProviderService fakeExternalService;
 
     @Override
     protected void onSetUp() throws Exception {
         super.onSetUp();
         
         this.externalService = 
             super.<IExternalService>getApplicationBean("externalService");
         this.generationJob = 
             super.<RequestXmlGenerationJob>getApplicationBean("requestXmlGenerationJob");
         this.fakeExternalService = 
             super.<IExternalProviderService>getApplicationBean("fakeExternalService");
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.ADMIN_CONTEXT);
 
         ExternalServiceBean esb = new ExternalServiceBean();
         esb.setGenerateTracedRequest(true);
         List<String> requestTypes = new ArrayList<String>();
         requestTypes.add(IRequestService.VO_CARD_REGISTRATION_REQUEST);
         esb.setRequestTypes(requestTypes);
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         lacb.registerExternalService(fakeExternalService, esb);
     }
     
     @Override
     protected void onTearDown() throws Exception {
         this.eraseTestFiles();
         super.onTearDown();
     }
     
     public void testXmlDataGeneration() throws Exception {
 
        // register the mock external provider service with the LACB
        ExternalServiceBean esb = new ExternalServiceBean();
        List<String> requestTypes = new ArrayList<String>();
        requestTypes.add(IRequestService.VO_CARD_REGISTRATION_REQUEST);
        esb.setRequestTypes(requestTypes);
        esb.setSupportAccountsByHomeFolder(true);
        esb.setGenerateTracedRequest(true);
        LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
        lacb.registerExternalService(fakeExternalService, esb);

         Long id1, id2, id3;
         this.createDummyEntities();
         this.validateRequest();
         id1 = this.creationBean.getRequestId();
         this.createDummyEntities();
         this.validateRequest();
         id2 = this.creationBean.getRequestId();
         this.createDummyEntities();
         this.validateRequest();
         id3 = this.creationBean.getRequestId();
         
         SecurityContext.setCurrentContext(SecurityContext.ADMIN_CONTEXT);
 
         this.externalService.addTrace(new ExternalServiceTrace(null, String.valueOf(id1), null,
                 null, null, "MyName", TraceStatusEnum.SENT));
         this.externalService.addTrace(new ExternalServiceTrace(null, String.valueOf(id2), null, 
                 null, null, "MyName", TraceStatusEnum.ACKNOWLEDGED));
         this.continueWithNewTransaction();
 
         this.generationJob.launchJob();
         this.remakeSecurityContext();
 
         File file = iLocalAuthorityRegistry.getRequestXmlResource(id3);
         assertTrue(file.exists());
         file = iLocalAuthorityRegistry.getRequestXmlResource(id1);
         assertTrue(file.exists());
         file = iLocalAuthorityRegistry.getRequestXmlResource(id2);
         assertFalse(file.exists());
     }
     
     public void testXmlDataErasing() throws Exception {
 
         this.createDummyEntities();
         this.validateRequest();
 
         SecurityContext.setCurrentContext(SecurityContext.ADMIN_CONTEXT);
         this.generationJob.performGeneration();
 
         File file = iLocalAuthorityRegistry.getRequestXmlResource(creationBean.getRequestId());
         assertTrue(file.exists());
 
         ExternalServiceTrace trace = new ExternalServiceTrace();
         trace.setKey(String.valueOf(this.creationBean.getRequestId()));
         trace.setKeyOwner("capdemat");
         trace.setName("MyName");
         trace.setStatus(TraceStatusEnum.ACKNOWLEDGED);
         this.externalService.addTrace(trace);
 
         this.generationJob.eraseAcknowledgedRequests();
         assertFalse(file.exists());
             
     }
     
     protected String getXmlOutputFolder() {
         return String.format("%1$s/%2$s/%3$s/",
             iLocalAuthorityRegistry.getAssetsBase(),
             SecurityContext.getCurrentConfigurationBean().getName(),
             Type.REQUEST_XML.getFolder());
     }
     
     protected void createDummyEntities() throws CvqException {
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.ADMIN_CONTEXT);
         this.creationBean = this.gimmeAnHomeFolder();
         this.continueWithNewTransaction();
     }
     
     protected void validateRequest() throws CvqInvalidTransitionException, CvqException {
         SecurityContext.setCurrentSite(localAuthorityName,SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
         iRequestWorkflowService.updateRequestState(creationBean.getRequestId(),
             RequestState.COMPLETE, null);
         iRequestWorkflowService.updateRequestState(creationBean.getRequestId(),
             RequestState.VALIDATED, null);
         this.continueWithNewTransaction();
     }
     
     protected void remakeSecurityContext() throws CvqException {
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithSiteRoles);
     }
     
     protected void eraseTestFiles() {
         File dir = new File(this.getXmlOutputFolder());
         List<File> files = new ArrayList<File>();
         
         files = Arrays.asList(dir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 if (new File(dir, name).isDirectory()) {
                     return false;
                 }
                 name = name.toLowerCase();
                 return name.endsWith(Type.REQUEST_XML.getExtension());
             }
         }));
         
         for (File file : files) {
             file.delete();
         }
     }
 }
