 package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import gov.nih.nci.coppa.po.HealthCareFacility;
 import gov.nih.nci.coppa.po.Organization;
 import gov.nih.nci.coppa.po.ResearchOrganization;
 import gov.nih.nci.coppa.services.entities.organization.client.OrganizationClient;
 import gov.nih.nci.coppa.services.structuralroles.researchorganization.client.ResearchOrganizationClient;
 import org.apache.axis.types.URI;
 
 /**
  * @author Rhett Sutphin
  */
 public enum PoOperation implements HubOperation {
     GET_ORGANIZATION(   "getById", Organization.class,                 OrganizationClient.class),
     ORGANIZATION_SEARCH("search",  Organization[].class,               OrganizationClient.class),
     GET_RESEARCH_ORGANIZATIONS("getByIds", ResearchOrganization[].class, ResearchOrganizationClient.class),
     GET_RESEARCH_ORGANIZATIONS_BY_PLAYER_IDS("getByPlayerIds", ResearchOrganization[].class, ResearchOrganizationClient.class),
    GET_HEALTH_CARE_FACILITIES("getByPlayerIds", HealthCareFacility[].class, HealthCareFacility.class);
 
     private String operationName;
     private Class<?> responseType;
     private Class<?> clientClass;
 
     private PoOperation(String operationName, Class<?> responseType, Class<?> clientClass) {
         this.operationName = operationName;
         this.responseType = responseType;
         this.clientClass = clientClass;
     }
 
     public String getServiceType() {
         return getClientClass().getSimpleName().
             replaceAll("Client", "").
             replaceAll("([a-z])([A-Z])", "$1_$2").
             toUpperCase()
             ;
     }
 
     public String getOperationName() {
         return operationName;
     }
 
     public Class<?> getClientClass() {
         return clientClass;
     }
 
     public Class<?> getResponseType() {
         return responseType;
     }
 
     public URI getNamespaceURI() {
         try {
             return new URI("http://po.coppa.nci.nih.gov");
         } catch (URI.MalformedURIException e) {
             throw new StudyCalendarError("It isn't malformed", e);
         }
     }
 }
