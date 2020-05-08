 package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaAccessor;
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
 import gov.nih.nci.coppa.common.LimitOffset;
 import gov.nih.nci.coppa.po.Organization;
 import gov.nih.nci.coppa.po.ResearchOrganization;
 import gov.nih.nci.coppa.services.entities.organization.client.OrganizationClient;
 import gov.nih.nci.coppa.services.pa.Id;
 import gov.nih.nci.coppa.services.pa.StudyProtocol;
 import gov.nih.nci.coppa.services.pa.StudySite;
 import gov.nih.nci.coppa.services.pa.faults.PAFault;
 import gov.nih.nci.coppa.services.pa.studyprotocolservice.client.StudyProtocolServiceClient;
 import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
 import gov.nih.nci.coppa.services.structuralroles.researchorganization.client.ResearchOrganizationClient;
 import org.apache.axis.types.URI;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.rmi.RemoteException;
 import java.util.Dictionary;
 
 /**
  * Access strategy for COPPA which talks directly to a COPPA implementation.
  *
  * @author Rhett Sutphin
  */
 public class DirectCoppaAccessor implements CoppaAccessor, ManagedService {
     public static final String SERVICE_PID = "edu.northwestern.bioinformatics.studycalendar.coppa-direct-strategy";
 
     public static final String PA_SERVICE_BASE_KEY = "coppa.grid.pa.baseUri";
     public static final String PO_SERVICE_BASE_KEY = "coppa.grid.po.baseUri";
 
     private static final String STUDY_PROTOCOL_SERVICE_RELATIVE = "/services/cagrid/StudyProtocolService";
     private static final String STUDY_SITE_SERVICE_RELATIVE = "/services/cagrid/StudySiteService";
     private static final String ORGANIZATION_SERVICE_RELATIVE = "/services/cagrid/Organization";
     private static final String RESEARCH_ORGANIZATION_ENDPOINT = "/services/cagrid/ResearchOrganization";
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private ServiceRegistration registration;
 
     private StudyProtocolServiceClient studyProtocolServiceClient;
     private StudySiteServiceClient studySiteServiceClient;
     private OrganizationClient organizationClient;
     private ResearchOrganizationClient researchOrganizationClient;
 
     public void register(BundleContext bundleContext) {
         registration = bundleContext.registerService(new String[] {
             ManagedService.class.getName(), CoppaAccessor.class.getName()
         }, this, new MapBuilder<String, Object>().
             put(Constants.SERVICE_PID, SERVICE_PID).toDictionary());
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     public void updated(Dictionary dictionary) throws ConfigurationException {
         if (dictionary != null) {
             registration.setProperties(dictionary);
             initializePaClients((String) dictionary.get(PA_SERVICE_BASE_KEY));
             initializePoClients((String) dictionary.get(PO_SERVICE_BASE_KEY));
         }
     }
 
     private void initializePaClients(String paBase) {
         if (paBase == null) {
             throw new StudyCalendarValidationException(
                 PA_SERVICE_BASE_KEY + " not set; cannot initialize " + getClass().getName());
         }
         try {
             studyProtocolServiceClient = new StudyProtocolServiceClient(uriJoin(paBase, STUDY_PROTOCOL_SERVICE_RELATIVE));
             studySiteServiceClient = new StudySiteServiceClient(uriJoin(paBase, STUDY_SITE_SERVICE_RELATIVE));
         } catch (URI.MalformedURIException e) {
             throw new StudyCalendarSystemException("Failed to create one of the PA client instances", e);
         } catch (RemoteException e) {
             throw new StudyCalendarSystemException("Failed to create one of the PA client instances", e);
         }
     }
 
     private void initializePoClients(String poBase) {
         if (poBase == null) {
             throw new StudyCalendarValidationException(
                 PO_SERVICE_BASE_KEY + " not set; cannot initialize " + getClass().getName());
         }
         try {
             organizationClient = new OrganizationClient(uriJoin(poBase, ORGANIZATION_SERVICE_RELATIVE));
             researchOrganizationClient = new ResearchOrganizationClient(uriJoin(poBase, RESEARCH_ORGANIZATION_ENDPOINT));
         } catch (URI.MalformedURIException e) {
             throw new StudyCalendarSystemException("Failed to create one of the PO client instances", e);
         } catch (RemoteException e) {
             throw new StudyCalendarSystemException("Failed to create one of the PO client instances", e);
         }
     }
 
     private String uriJoin(String baseUri, String relative) {
         if (baseUri.endsWith("/")) {
             return baseUri + relative.substring(1);
         } else {
             return baseUri + relative;
         }
     }
 
     public StudyProtocol getStudyProtocol(Id id) {
         try {
             return studyProtocolServiceClient.getStudyProtocol(id);
         } catch (RemoteException e) {
             log.error("COPPA study protocol get failed", e);
             return null;
         }
     }
 
     public StudyProtocol[] searchStudyProtocols(StudyProtocol criteria, LimitOffset limit) {
         try {
             return studyProtocolServiceClient.search(criteria, limit);
         } catch(RemoteException e) {
             log.error("COPPA study protocol search failed", e);
             return new StudyProtocol[0];
         }
     }
 
     public StudySite[] searchStudySitesByStudyProtocolId(Id id) {
         try {
            return studySiteServiceClient.getByStudyProtocol(id);
         } catch (PAFault e) {
             log.error("COPPA study site search failed", e);
             return new StudySite[0];
         } catch (RemoteException e) {
             log.error("COPPA study site search failed", e);
             return new StudySite[0];
         }
     }
 
     public Organization[] searchOrganizations(Organization criteria) {
         try {
             return organizationClient.search(criteria);
         } catch (RemoteException e) {
             log.error("COPPA organization search failed", e);
             return new Organization[0];
         }
     }
 
     public Organization getOrganization(gov.nih.nci.coppa.po.Id id) {
         try {
             return organizationClient.getById(id);
         } catch (RemoteException e) {
             log.error("COPPA organization search failed", e);
             return null;
         }
     }
 
     public ResearchOrganization[] getResearchOrganizations(gov.nih.nci.coppa.po.Id[] ids ) {
         try {
             return researchOrganizationClient.getByIds(ids);
         } catch (RemoteException e) {
             log.error("COPPA research organization search failed", e);
             return new ResearchOrganization[0];
         }
     }
 }
