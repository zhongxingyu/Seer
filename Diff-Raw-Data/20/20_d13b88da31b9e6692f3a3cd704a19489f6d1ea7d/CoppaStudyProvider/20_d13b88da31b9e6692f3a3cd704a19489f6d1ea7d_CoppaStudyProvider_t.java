 package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;
 
 import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
 import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
 import gov.nih.nci.coppa.common.LimitOffset;
 import gov.nih.nci.coppa.services.pa.Id;
 import gov.nih.nci.coppa.services.pa.StudyProtocol;
 import gov.nih.nci.coppa.services.pa.StudySite;
 import org.iso._21090.ST;
 import org.osgi.framework.BundleContext;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 public class CoppaStudyProvider implements StudyProvider {
     private BundleContext bundleContext;
 
     public CoppaStudyProvider(BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     public List<Study> getStudies(List<Study> parameters) {
         List<Study> studies = new ArrayList<Study>(parameters.size());
         for (Study param : parameters) {
             String extension = param.getSecondaryIdentifierValue(
                 CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
             if (extension != null) {
                 Id id = studyProtocolIdentifier(extension);
 
                 StudyProtocol raw = CoppaProviderHelper.getCoppaAccessor(bundleContext).getStudyProtocol(id);
 
                 Study found = createStudy(raw);
                 studies.add(found);
             }
         }
         return studies;
     }
 
     public Study detect(Study param, Collection<Study> studies) {
         String extension = param.getSecondaryIdentifierValue(
             CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
 
         if (extension != null) {
             for (Study study : studies) {
                 if (extension.equals(study.getSecondaryIdentifierValue(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE))) {
                     return study;
                 }
             }
         }
 
         return null;
     }
 
     public List<Study> search(String partialName) {
         StudyProtocol base = new StudyProtocol();
 
         ST titleTemplate = new ST();
         titleTemplate.setValue(partialName);
         base.setOfficialTitle(titleTemplate);
 
         StudyProtocol[] raw = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchStudyProtocols(base, smallLimit());
         if (raw == null) {
             return Collections.emptyList();
         } else {
             List<Study> results = new ArrayList<Study>(raw.length);
             for (StudyProtocol protocol : raw) {
                 results.add(createStudy(protocol));
             }
             return results;
         }
     }
 
     public String providerToken() {
         return CoppaProviderConstants.PROVIDER_TOKEN;
     }
 
     //////////// Search Helper Methods
 
     private static LimitOffset smallLimit() {
         LimitOffset lo = new LimitOffset();
         lo.setLimit(40); // Limit since autocomplete might return too many.
         lo.setOffset(0);
         return lo;
     }
 
     //////////// Object creation helpers
 
     Study createStudy(StudyProtocol p) {
         Study s = null;
         
         if (p != null) {
             s = new Study();
 
             s.setAssignedIdentifier(
                p.getIdentifier().getExtension());
 
             s.setLongTitle(
                 p.getOfficialTitle().getValue());
 
             Map<String, String> ids = extractSecondaryIdentifiers(p);
             addSecondaryIdentifiers(s, ids);
         }
 
         return s;
     }
 
     private Map<String, String> extractSecondaryIdentifiers(StudyProtocol p)  {
         MapBuilder<String, String> ids =
                 new MapBuilder<String, String>();
 
        if (p.getIdentifier() != null) {
            ids.put(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE, p.getIdentifier().getExtension());
         }
 
         if (p.getPublicTitle() != null && p.getPublicTitle().getValue() != null) {
             ids.put("Public Title", p.getPublicTitle().getValue());
         }
 
         if (p.getOfficialTitle() != null && p.getOfficialTitle().getValue() != null) {
             ids.put("Official Title", p.getOfficialTitle().getValue());
         }
 
        if (p.getIdentifier() != null) {
             Id studyProtocolId = studyProtocolIdentifier(
                    p.getIdentifier().getExtension());
 
             String leadOrgIdent = findLeadOrganizationIdentifier(studyProtocolId);
 
             if (leadOrgIdent != null) {
                 ids.put("Lead Organization Identifier", leadOrgIdent);
             }
         }
 
         return ids.toMap();
     }
 
     private void addSecondaryIdentifiers(Study s, Map<String, String> ids) {
         for (Map.Entry<String, String> entry : ids.entrySet()) {
             StudySecondaryIdentifier si = new StudySecondaryIdentifier();
             si.setType(entry.getKey());
             si.setValue(entry.getValue());
             s.addSecondaryIdentifier(si);
         }
     }
 
     // TODO: CoppaStudySiteProvider might be a better place for this
     private Id studyProtocolIdentifier(String extension) {
         Id id = new Id();
         id.setExtension(extension);
         return id;
     }
 
     private String findLeadOrganizationIdentifier(Id studyProtocolId) {
         StudySite[] studySite = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchStudySitesByStudyProtocolId(studyProtocolId);
         if (studySite != null) {
             for(StudySite s: studySite) {
                 // This is how we find the Organization which is leading
                 // the StudyProtocol.
                 if ("Lead Organization".equals(s.getFunctionalCode().getCode()) && s.getLocalStudyProtocolIdentifier() != null) {
                     return s.getLocalStudyProtocolIdentifier().getValue();
                 }
             }            
         }
         return null;
     }
 }
