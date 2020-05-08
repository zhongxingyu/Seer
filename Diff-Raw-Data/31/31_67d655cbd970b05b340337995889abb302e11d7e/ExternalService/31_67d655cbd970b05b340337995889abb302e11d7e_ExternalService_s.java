 package fr.cg95.cvq.external.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 
 import fr.cg95.cvq.business.external.ExternalServiceIdentifierMapping;
 import fr.cg95.cvq.business.external.ExternalServiceIndividualMapping;
 import fr.cg95.cvq.business.external.ExternalServiceTrace;
 import fr.cg95.cvq.business.external.TraceStatusEnum;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.business.users.payment.PaymentState;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.dao.external.IExternalServiceTraceDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.permission.CvqPermissionException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.request.IRequestService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.util.DateUtils;
 import fr.cg95.cvq.util.quering.BaseOperator;
 import fr.cg95.cvq.util.quering.CriteriasDescriptor;
 import fr.cg95.cvq.util.quering.ISelectArgument;
 import fr.cg95.cvq.util.quering.SelectField;
 import fr.cg95.cvq.util.quering.criterias.ISearchCriteria;
 import fr.cg95.cvq.util.quering.criterias.InCriteria;
 import fr.cg95.cvq.util.quering.criterias.SimpleCriteria;
 import fr.cg95.cvq.util.quering.sort.SortCriteria;
 import fr.cg95.cvq.util.quering.sort.SortDirection;
 
 public class ExternalService implements IExternalService, BeanFactoryAware {
 
     private static Logger logger = Logger.getLogger(ExternalService.class);
 
     private IGenericDAO genericDAO;
     private IExternalServiceTraceDAO externalServiceTraceDAO;
     private IHomeFolderService homeFolderService;
     private IRequestService requestService;
     
     private ListableBeanFactory beanFactory;
 
     public void init() {
         this.homeFolderService = (IHomeFolderService)
             beanFactory.getBeansOfType(IHomeFolderService.class, false, false).values().iterator().next();
     }
     
     public boolean authenticate(String externalServiceLabel, String password) {
         IExternalProviderService externalProviderService =
             getExternalServiceByLabel(externalServiceLabel);
         if (externalProviderService == null)
             return false;
         
         ExternalServiceBean esb = getBeanForExternalService(externalProviderService);
         if (esb.getPassword().equals(password))
             return true;
         
         return false;
     }
 
     public void sendRequest(Request request) throws CvqException {
 
         // get the external services interested by this request type
         String requestTypeLabel = request.getRequestType().getLabel();
         Set<IExternalProviderService> externalProviderServices = 
             getExternalServicesByRequestType(requestTypeLabel);
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return;
         
         HomeFolder homeFolder = homeFolderService.getById(request.getHomeFolderId());
         for (IExternalProviderService externalProviderService : externalProviderServices) {
             // before sending the request to the external service, eventually set 
             // the external identifiers if they are known ...
             String externalServiceLabel = externalProviderService.getLabel();
             ExternalServiceIdentifierMapping esim = 
                 getIdentifierMapping(externalServiceLabel, homeFolder.getId());
             if (esim != null) {
                 homeFolder.setExternalId(esim.getExternalId());
                 homeFolder.setExternalCapDematId(esim.getExternalCapDematId());
                 for (Individual individual : homeFolder.getIndividuals()) {
                     Set<ExternalServiceIndividualMapping> esimSet =
                         esim.getIndividualsMappings();
                     for (ExternalServiceIndividualMapping esimTemp : esimSet) {
                         if (esimTemp.getIndividualId().equals(individual.getId())) {
                             individual.setExternalId(esimTemp.getExternalId());
                             individual.setExternalCapDematId(esimTemp.getExternalCapDematId());
                             break;
                         }
                     }
                 }
             } else {
                 // no existing external service mapping : create a new one to store
                 // the CapDemat external identifier
                 esim = new ExternalServiceIdentifierMapping();
                 esim.setExternalServiceLabel(externalServiceLabel);
                 esim.setHomeFolderId(homeFolder.getId());
                 esim.setExternalCapDematId(UUID.randomUUID().toString());
                 for (Individual individual : homeFolder.getIndividuals()) {
                     String externalCapDematId = UUID.randomUUID().toString();
                     esim.addIndividualMapping(individual.getId(), externalCapDematId, null);
                     individual.setExternalCapDematId(externalCapDematId);
                 }
                 
                 genericDAO.create(esim);
             }
             ExternalServiceTrace est = null;
             if (!externalProviderService.handlesTraces()) {
                 est = new ExternalServiceTrace();
                 est.setDate(new Date());
                 est.setKeyOwner("capdemat");
                 est.setKey(String.valueOf(request.getId()));
                 est.setName(externalServiceLabel);
             }
             try {
                 externalProviderService.sendRequest(requestService.fillRequestXml(request));
                 if (!externalProviderService.handlesTraces()) {
                     est.setStatus(TraceStatusEnum.SENT);
                 }
             } catch (CvqException ce) {
                 logger.error("sendRequest() error while sending request to " 
                         + externalServiceLabel);
                 if (!externalProviderService.handlesTraces()) {
                     est.setStatus(TraceStatusEnum.NOT_SENT);
                 }
             }
             if (!externalProviderService.handlesTraces()) {
                 externalServiceTraceDAO.create(est);
             }
         }
     }
 
     public void creditHomeFolderAccounts(Payment payment)
             throws CvqException {
 
         if (!payment.getState().equals(PaymentState.VALIDATED)) {
             logger.info("creditHomeFolderAccounts() not re-routing non validated payment");
             return;
         }
         
         Map<String, List<PurchaseItem>> externalServicesToNotify = 
             new HashMap<String, List<PurchaseItem>>();
         Set<PurchaseItem> purchaseItems = payment.getPurchaseItems();
         for (PurchaseItem purchaseItem : purchaseItems) {
             
             // if purchase item is managed by an external service, 
             // stack it for notification of the associated external service
             if (purchaseItem instanceof ExternalAccountItem) {
                 logger.debug("creditHomeFolderAccounts() item managed by an external service : " 
                         + purchaseItem.getFriendlyLabel());
                 ExternalAccountItem externalAccountItem = (ExternalAccountItem) purchaseItem;
                 externalAccountItem.setSupportedBroker(payment.getBroker());
                 String externalServiceLabel = externalAccountItem.getExternalServiceLabel();
                 if (externalServicesToNotify.get(externalServiceLabel) == null) {
                     externalServicesToNotify.put(externalServiceLabel, 
                             new ArrayList<PurchaseItem>());
                 }
                 externalServicesToNotify.get(externalServiceLabel).add(purchaseItem);
             }
         }
         
         if (!externalServicesToNotify.isEmpty()) {
             for (String externalServiceLabel : externalServicesToNotify.keySet()) {
                 IExternalProviderService service = getExternalServiceByLabel(externalServiceLabel);
                 if (service == null) {
                     logger.error("notifyPayments() No external service with label " + 
                             externalServiceLabel + " has been found");
                     continue;
                 }
                 ExternalServiceIdentifierMapping esim = 
                     getIdentifierMapping(externalServiceLabel, payment.getHomeFolder().getId());
                 service.creditHomeFolderAccounts(externalServicesToNotify.get(externalServiceLabel), 
                         payment.getCvqReference(), payment.getBankReference(), 
                         payment.getHomeFolder().getId(), 
                         esim == null ? null : esim.getExternalCapDematId(), 
                         esim == null ? null : esim.getExternalId(), payment.getCommitDate());
             }
         }
 
     }
 
     public boolean hasMatchingExternalService(String requestLabel) {
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestType(requestLabel);
         if (externalProviderServices != null && !externalProviderServices.isEmpty())
             return true;
         else
             return false;
     }
 
     public Map<Date, String> getConsumptionsByRequest(Request request, Date dateFrom, Date dateTo)
         throws CvqException {
 
         Map<Date, String> resultMap = new LinkedHashMap<Date, String>();
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestType(request.getRequestType().getLabel());
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return resultMap;
 
         for (IExternalProviderService externalProviderService : externalProviderServices) {
             Map<Date, String> serviceMap = 
                 externalProviderService.getConsumptionsByRequest(request, dateFrom, dateTo);
             if (serviceMap != null && !serviceMap.isEmpty())
                 resultMap.putAll(serviceMap);            
         }
         
         return resultMap;
     }
 
     public Set<ExternalAccountItem> getExternalAccounts(Long homeFolderId, 
             Set<String> homeFolderRequestTypes, String type) 
         throws CvqException {
 
         Set<ExternalAccountItem> accountsInfoSet = new HashSet<ExternalAccountItem>();
         if (homeFolderRequestTypes == null || homeFolderRequestTypes.isEmpty())
             return accountsInfoSet;
         
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestTypes(homeFolderRequestTypes);
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return accountsInfoSet;
 
         for (IExternalProviderService service : externalProviderServices) {
             ExternalServiceBean esb = getBeanForExternalService(service);
             // ask accounts information by home folder or by request
             // according to what the external service supports
             if (esb.supportAccountsByHomeFolder()) {
                 ExternalServiceIdentifierMapping esim = 
                     getIdentifierMapping(service.getLabel(), homeFolderId);
                 Map<String, List<ExternalAccountItem>> homeFolderAccounts = 
                     service.getAccountsByHomeFolder(homeFolderId, 
                             esim == null ? null : esim.getExternalCapDematId(), 
                             esim == null ? null : esim.getExternalId());
                 if (homeFolderAccounts != null && homeFolderAccounts.get(type) != null) {
                     accountsInfoSet.addAll(homeFolderAccounts.get(type));
                 }
             } else if (esb.supportAccountsByRequest()) {
                 logger.warn("getExternalAccounts() accounts by request is currently not supported");
             }
         }
         
         return accountsInfoSet;
     }
 
     public Map<Individual, Map<String, String>> getIndividualAccountsInformation(Long homeFolderId, 
             Set<String> homeFolderRequestTypes)
             throws CvqException {
         
         Map<Individual, Map<String,String>> result = new HashMap<Individual, Map<String,String>>();
         
         if (homeFolderRequestTypes == null || homeFolderRequestTypes.isEmpty())
             return result;
         
         Set<IExternalProviderService> externalProviderServices =
             getExternalServicesByRequestTypes(homeFolderRequestTypes);
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return result;
         
         for (IExternalProviderService externalProviderService : externalProviderServices) {
             ExternalServiceIdentifierMapping esim = 
                 getIdentifierMapping(externalProviderService.getLabel(), homeFolderId);
             Map<Individual, Map<String, String>> serviceResults = 
                 externalProviderService.getIndividualAccountsInformation(homeFolderId, 
                         esim == null ? null : esim.getExternalCapDematId(), 
                         esim == null ? null : esim.getExternalId());
             if (serviceResults != null) {
                 for (Individual individual : serviceResults.keySet()) {
                     if (result.get(individual) == null) {
                         result.put(individual, serviceResults.get(individual));
                     } else {
                         Map<String, String> currentIndividualInfo = result.get(individual);
                         currentIndividualInfo.putAll(serviceResults.get(individual));
                     }
                 }
             }
         }
         
         return result;
     }
 
     public void loadDepositAccountDetails(ExternalDepositAccountItem edai) throws CvqException {
         IExternalProviderService externalProviderService = 
             getExternalServiceByLabel(edai.getExternalServiceLabel());
         externalProviderService.loadDepositAccountDetails(edai);
     }
 
     public void loadInvoiceDetails(ExternalInvoiceItem eii) throws CvqException {
         IExternalProviderService externalProviderService = 
             getExternalServiceByLabel(eii.getExternalServiceLabel());
         externalProviderService.loadInvoiceDetails(eii);
     }
     
     public Set<String> getRequestTypesForExternalService(String externalServiceLabel) {
         IExternalProviderService externalProviderService = 
             getExternalServiceByLabel(externalServiceLabel);
         ExternalServiceBean esb = getBeanForExternalService(externalProviderService);
 
         if (esb.getRequestTypes() != null)
             return new HashSet<String>(esb.getRequestTypes());
         else
             return null;
     }
 
     public Set<String> getGenerableRequestTypes() {
         Set<String> result = new HashSet<String>();
 
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Map<IExternalProviderService, ExternalServiceBean> externalProviderServices = 
             lacb.getExternalServices();
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return null;
 
         for (IExternalProviderService service : externalProviderServices.keySet()) {
             ExternalServiceBean esb = externalProviderServices.get(service);
             if (esb.getGenerateTracedRequest())
                 result.addAll(esb.getRequestTypes());
         }
         
         return result;
     }
 
     public void addHomeFolderMapping(String externalServiceLabel, Long homeFolderId,
             String externalId) throws CvqPermissionException {
         
         ExternalServiceIdentifierMapping esim =
             getIdentifierMapping(externalServiceLabel, homeFolderId);
         
         if (esim == null) {
             esim = new ExternalServiceIdentifierMapping();
             esim.setExternalServiceLabel(externalServiceLabel);
             esim.setHomeFolderId(homeFolderId);
             esim.setExternalCapDematId(UUID.randomUUID().toString());
         }
         
         esim.setExternalId(externalId);
         
         genericDAO.create(esim);
     }
 
     public void addHomeFolderMapping(ExternalServiceIdentifierMapping esim)
             throws CvqPermissionException {
 
         ExternalServiceIdentifierMapping esimFromDb =
             getIdentifierMapping(esim.getExternalServiceLabel(), esim.getHomeFolderId());
         
         if (esimFromDb == null) {
             genericDAO.create(esim);
             esim.setExternalCapDematId(UUID.randomUUID().toString());
         } else {
             esimFromDb.setExternalId(esim.getExternalId());
             genericDAO.update(esimFromDb);
         }
     }
 
     public void addIndividualMapping(String externalServiceLabel, Long homeFolderId,
             Long individualId, String externalId) throws CvqException {
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("externalServiceLabel", BaseOperator.EQUALS, 
                 externalServiceLabel));
         criterias.add(new SimpleCriteria("homeFolderId", BaseOperator.EQUALS,
                 homeFolderId));
         
         Set<ExternalServiceIdentifierMapping> esimSet =
             externalServiceTraceDAO.<ExternalServiceIdentifierMapping,ExternalServiceIdentifierMapping>get(criterias, ExternalServiceIdentifierMapping.class);
         if (esimSet == null || esimSet.isEmpty())
             throw new CvqException("Mapping not found for external service / home folder : "
                     + externalServiceLabel + "/" + homeFolderId );
         else if (esimSet.size() > 1)
             throw new CvqException("Too many mappings found for external service / home folder : "
                     + externalServiceLabel + "/" + homeFolderId );
         
         ExternalServiceIdentifierMapping esim = esimSet.iterator().next();
         Set<ExternalServiceIndividualMapping> esimIndividuals = esim.getIndividualsMappings();
         boolean foundExistingIndividualMapping = false;
         for (ExternalServiceIndividualMapping esimIndividual : esimIndividuals) {
             if (esimIndividual.getIndividualId().equals(individualId)) {
                 logger.debug("addIndividualMapping() found an existing mapping for " + individualId);
                 esimIndividual.setExternalId(externalId);
                 foundExistingIndividualMapping = true;
                 break;
             }
         }
         if (!foundExistingIndividualMapping) {
             ExternalServiceIndividualMapping esimIndividual = 
                 new ExternalServiceIndividualMapping();
             esimIndividual.setIndividualId(individualId);
             esimIndividual.setExternalCapDematId(UUID.randomUUID().toString());
             esimIndividual.setExternalId(externalId);
             esimIndividuals.add(esimIndividual);
         }
         
         externalServiceTraceDAO.update(esim);
     }
 
     public void deleteHomeFolderMapping(String externalServiceLabel, Long homeFolderId) 
         throws CvqPermissionException {
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("externalServiceLabel", BaseOperator.EQUALS, 
                 externalServiceLabel));
         criterias.add(new SimpleCriteria("homeFolderId", BaseOperator.EQUALS,
                 homeFolderId));
         
         ExternalServiceIdentifierMapping esim =
             externalServiceTraceDAO.<ExternalServiceIdentifierMapping,ExternalServiceIdentifierMapping>get(criterias, ExternalServiceIdentifierMapping.class).iterator().next();
         
         externalServiceTraceDAO.delete(esim);
     }
 
     public void deleteHomeFoldersMappings(String externalServiceLabel)
         throws CvqPermissionException {
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("externalServiceLabel", BaseOperator.EQUALS, 
                 externalServiceLabel));
         
         Set<ExternalServiceIdentifierMapping> esimSet =
             externalServiceTraceDAO.<ExternalServiceIdentifierMapping,ExternalServiceIdentifierMapping>get(criterias, ExternalServiceIdentifierMapping.class);
 
         for (ExternalServiceIdentifierMapping esim : esimSet) {
             externalServiceTraceDAO.delete(esim);
         }
     }
 
     public ExternalServiceIdentifierMapping getIdentifierMapping(String externalServiceLabel,
             Long homeFolderId) {
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("externalServiceLabel", BaseOperator.EQUALS, 
                 externalServiceLabel));
         criterias.add(new SimpleCriteria("homeFolderId", BaseOperator.EQUALS,
                 homeFolderId));
         
         Set<ExternalServiceIdentifierMapping> esimSet =
             externalServiceTraceDAO.<ExternalServiceIdentifierMapping,ExternalServiceIdentifierMapping>get(criterias, ExternalServiceIdentifierMapping.class);
         if (esimSet == null || esimSet.isEmpty())
             return null;
         else 
             return esimSet.iterator().next();
     }
 
     public ExternalServiceIdentifierMapping getIdentifierMapping(String externalServiceLabel,
             String externalId) {
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("externalServiceLabel", BaseOperator.EQUALS, 
                 externalServiceLabel));
         criterias.add(new SimpleCriteria("externalId", BaseOperator.EQUALS, externalId));
         
         Set<ExternalServiceIdentifierMapping> esimSet =
             externalServiceTraceDAO.<ExternalServiceIdentifierMapping,ExternalServiceIdentifierMapping>get(criterias, ExternalServiceIdentifierMapping.class);
         if (esimSet == null || esimSet.isEmpty())
             return null;
         else 
             return esimSet.iterator().next();
     }
 
     public Set<ExternalServiceIdentifierMapping> getIdentifiersMappings(String externalServiceLabel) {
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("externalServiceLabel", BaseOperator.EQUALS, 
                 externalServiceLabel));
         
         Set<ExternalServiceIdentifierMapping> esimSet =
             externalServiceTraceDAO.<ExternalServiceIdentifierMapping,ExternalServiceIdentifierMapping>get(criterias, ExternalServiceIdentifierMapping.class);
         if (esimSet == null || esimSet.isEmpty())
             return null;
         else 
             return esimSet;
     }
 
     /**
      * Get the configuration bean associated to the given external provider service.
      */
     private ExternalServiceBean getBeanForExternalService(IExternalProviderService externalProviderService) {
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         return lacb.getExternalServices().get(externalProviderService);
     }
 
     /**
      * Get the external provider service that has the given label.
      */
     private IExternalProviderService getExternalServiceByLabel(final String externalServiceLabel) {
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Map<IExternalProviderService, ExternalServiceBean> externalProviderServices = 
             lacb.getExternalServices();
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return null;
 
         for (IExternalProviderService service : externalProviderServices.keySet()) {
             if (service.getLabel().equals(externalServiceLabel))
                 return service;
         }
 
         return null;
     }
 
     public Set<IExternalProviderService> getExternalServicesByRequestType(final String requestTypeLabel) {
 
         Set<String> requestTypesLabels = new HashSet<String>();
         requestTypesLabels.add(requestTypeLabel);
         return getExternalServicesByRequestTypes(requestTypesLabels);
     }
 
     public IExternalProviderService getExternalServiceByRequestType(final String requestTypeLabel) {
         if (hasMatchingExternalService(requestTypeLabel)) {
             return getExternalServicesByRequestType(requestTypeLabel).toArray(new IExternalProviderService[1])[0];
         }
         return null;
     }
 
     /**
      * Get the list of external services objects for the current local authority
      * interested in events about the given request types.
      */
     private Set<IExternalProviderService> getExternalServicesByRequestTypes(final Set<String> requestTypesLabels) {
 
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         Map<IExternalProviderService, ExternalServiceBean> externalProviderServices = 
             lacb.getExternalServices();
         if (externalProviderServices == null || externalProviderServices.isEmpty())
             return null;
 
         Set<IExternalProviderService> resultSet = new HashSet<IExternalProviderService>();
         for (IExternalProviderService service : externalProviderServices.keySet()) {
             ExternalServiceBean esb = externalProviderServices.get(service);
             for (String requestTypeLabel : requestTypesLabels) {
                 if (esb.supportRequestType(requestTypeLabel)) {
                     resultSet.add(service);
                 }
             }
         }
 
         return resultSet;
     }
 
     public Long addTrace(ExternalServiceTrace trace) throws CvqPermissionException {
         trace.setDate(new Date());
         return externalServiceTraceDAO.create(trace);
     }
 
     public Set<ExternalServiceTrace> getTraces(String key, String name,
             TraceStatusEnum status, Date dateFrom, Date dateTo) {
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("key",BaseOperator.EQUALS,key));
         criterias.add(new SimpleCriteria("name",BaseOperator.EQUALS, name));
         criterias.add(new SimpleCriteria("date",BaseOperator.GTE,dateFrom));
         criterias.add(new SimpleCriteria("date",BaseOperator.LTE,dateTo));
         if (status != null) 
             criterias.add(new SimpleCriteria("status",BaseOperator.EQUALS,status.toString())); 
         
         return this.externalServiceTraceDAO.<ExternalServiceTrace,ExternalServiceTrace>get(
                 criterias, ExternalServiceTrace.class);
     }
 
     public Set<ExternalServiceTrace> getTraces(Long key, String name,
             TraceStatusEnum status, Date dateFrom, Date dateTo) {
         return getTraces(String.valueOf(key), name, status, dateFrom, dateTo);
     }
 
     public Set<ExternalServiceTrace> getTraces(String key, String label) {
         CriteriasDescriptor criteriasDescriptor = new CriteriasDescriptor();
         criteriasDescriptor.addSort(new SortCriteria(ExternalServiceTrace.class, "date", SortDirection.ASC));
         criteriasDescriptor.addSearch(new SimpleCriteria("key", BaseOperator.EQUALS, key));
         criteriasDescriptor.addSearch(new SimpleCriteria("keyOwner", BaseOperator.EQUALS, "capdemat"));
         criteriasDescriptor.addSearch(new SimpleCriteria("name", BaseOperator.EQUALS, label));
         return externalServiceTraceDAO.<ExternalServiceTrace, ExternalServiceTrace>get(criteriasDescriptor, ExternalServiceTrace.class);
     }
 
     public Set<ExternalServiceTrace> getTraces(Long key, String label) {
         return getTraces(String.valueOf(key), label);
     }
 
     public Set<Long> getTraceKeysByStatus(Set<Long> ids, Set<String> statuses) {
         if (ids == null || ids.isEmpty()) 
             return new HashSet<Long>();
         
         Set<ISelectArgument> fields = new HashSet<ISelectArgument>();
         fields.add(new SelectField(ExternalServiceTrace.class,"key"));
 
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new InCriteria("key", BaseOperator.IN, ids));
         criterias.add(new InCriteria("status", BaseOperator.IN, statuses));
         
         return this.externalServiceTraceDAO.<ExternalServiceTrace,Long> get(fields, criterias,
                 ExternalServiceTrace.class);
     }
 
     public Set<ExternalServiceTrace> getTracesByStatus(TraceStatusEnum status) {
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("status",BaseOperator.EQUALS,status.toString()));
         
         return this.externalServiceTraceDAO.<ExternalServiceTrace,ExternalServiceTrace> get(
                 criterias, ExternalServiceTrace.class);
     }
 
     public int deleteTraces(String key, String keyOwner) throws CvqPermissionException,
             CvqObjectNotFoundException {
         
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("key",BaseOperator.EQUALS,key));
         criterias.add(new SimpleCriteria("keyOwner",BaseOperator.EQUALS,keyOwner));
         
         return this.externalServiceTraceDAO.<ExternalServiceTrace>delete(criterias,
                 ExternalServiceTrace.class);
     }
 
     public int deleteTraces(Long key, String keyOwner)
         throws CvqPermissionException, CvqObjectNotFoundException {
         return deleteTraces(String.valueOf(key), keyOwner);
     }
 
     public int deleteTraces(String name) throws CvqPermissionException, 
             CvqObjectNotFoundException {
         
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new SimpleCriteria("name",BaseOperator.EQUALS,name));
         
         return this.externalServiceTraceDAO.<ExternalServiceTrace>delete(criterias,
                 ExternalServiceTrace.class);
     }
 
     public Set<Long> getRequestIds(Set<ISearchCriteria> searchCriterias) {
         Set<ISelectArgument> fields = new HashSet<ISelectArgument>();
         fields.add(new SelectField(Request.class,"id"));
         
         return this.externalServiceTraceDAO.<Request,Long> get(fields,searchCriterias,Request.class);
     }
 
     public Set<Long> getValidatedRequestIds(Set<String> requestTypesLabels, int numberOfDays) {
         if (requestTypesLabels == null || requestTypesLabels.isEmpty())
             return new HashSet<Long>();
         
         Date prevDate = DateUtils.getShiftedDate(Calendar.DAY_OF_YEAR, numberOfDays);
         
         Set<ISelectArgument> fields = new HashSet<ISelectArgument>();
         fields.add(new SelectField(Request.class,"id"));
         
         Set<ISearchCriteria> criterias = new HashSet<ISearchCriteria>();
         criterias.add(new InCriteria("requestType.label",BaseOperator.IN, requestTypesLabels));
         criterias.add(new SimpleCriteria("validationDate",BaseOperator.GTE, prevDate));
             
         return this.externalServiceTraceDAO.<Request,Long> get(fields,criterias,Request.class);
     }
 
     public void setExternalId(String externalServiceLabel, Long homeFolderId, Long individualId, 
             String externalId) {
         ExternalServiceIdentifierMapping identifierMapping = 
             getIdentifierMapping(externalServiceLabel, homeFolderId);
         for (ExternalServiceIndividualMapping esim : identifierMapping.getIndividualsMappings()) {
             if (esim.getIndividualId().equals(individualId)) {
                 esim.setExternalId(externalId);
                 break;
             }
         }
     }
 
     public ExternalServiceTrace getLastTrace(String key, String label) {
         CriteriasDescriptor criteriasDescriptor = new CriteriasDescriptor();
         criteriasDescriptor.setMax(1);
         criteriasDescriptor.addSort(new SortCriteria(ExternalServiceTrace.class, "date", SortDirection.DESC));
         criteriasDescriptor.addSearch(new SimpleCriteria("key", BaseOperator.EQUALS, key));
         criteriasDescriptor.addSearch(new SimpleCriteria("keyOwner", BaseOperator.EQUALS, "capdemat"));
         criteriasDescriptor.addSearch(new SimpleCriteria("name", BaseOperator.EQUALS, label));
         Set<ExternalServiceTrace> traces = externalServiceTraceDAO.<ExternalServiceTrace, ExternalServiceTrace>get(criteriasDescriptor, ExternalServiceTrace.class);
         ExternalServiceTrace lastTrace = null;
         if (!traces.isEmpty()) {
             lastTrace = traces.toArray(new ExternalServiceTrace[]{})[0];
         }
         return lastTrace;
     }
 
     public ExternalServiceTrace getLastTrace(Long key, String label) {
         return getLastTrace(String.valueOf(key), label);
     }
 
     public boolean hasTraceWithStatus(String key, String label, TraceStatusEnum status) {
         CriteriasDescriptor criteriasDescriptor = new CriteriasDescriptor();
         criteriasDescriptor.addSearch(new SimpleCriteria("key", BaseOperator.EQUALS, key));
         criteriasDescriptor.addSearch(new SimpleCriteria("keyOwner", BaseOperator.EQUALS, "capdemat"));
         criteriasDescriptor.addSearch(new SimpleCriteria("name", BaseOperator.EQUALS, label));
         criteriasDescriptor.addSearch(new SimpleCriteria("status", BaseOperator.EQUALS, status.toString()));
         return !externalServiceTraceDAO.<ExternalServiceTrace, ExternalServiceTrace>get(criteriasDescriptor, ExternalServiceTrace.class).isEmpty();
     }
 
     public boolean hasTraceWithStatus(Long key, String label, TraceStatusEnum status) {
         return hasTraceWithStatus(String.valueOf(key), label, status);
     }
 
     public void create(ExternalServiceTrace trace)
         throws CvqPermissionException {
         externalServiceTraceDAO.create(trace);
     }
 
     public List<String> checkExternalReferential(Request request) {
         List<String> errors = new ArrayList<String>();
         for (IExternalProviderService eps :
             getExternalServicesByRequestType(request.getRequestType().getLabel())) {
             try {
                 errors.addAll(eps.checkExternalReferential(requestService.fillRequestXml(request)));
             } catch (CvqException e) {
                 errors.add("Erreur lors de la vérification des référentiels pour " + eps.getLabel());
             }
         }
         return errors;
     }
 
     public void setExternalServiceTraceDAO(IExternalServiceTraceDAO externalServiceTraceDAO) {
         this.externalServiceTraceDAO = externalServiceTraceDAO;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     @Override
     public void setBeanFactory(BeanFactory arg0) throws BeansException {
         this.beanFactory = (ListableBeanFactory) arg0;
     }
 
     public void setRequestService(IRequestService requestService) {
         this.requestService = requestService;
     }
 }
