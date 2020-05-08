 package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;
 
 import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
 import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import gov.nih.nci.coppa.po.Organization;
 import gov.nih.nci.coppa.po.ResearchOrganization;
 import gov.nih.nci.coppa.services.pa.Id;
 import org.iso._21090.II;
 import org.osgi.framework.BundleContext;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class CoppaStudySiteProvider implements edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider, RefreshableProvider {
     private BundleContext bundleContext;
 
     public CoppaStudySiteProvider(BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     public List<List<StudySite>> getAssociatedSites(List<Study> studies) {
         List<List<StudySite>> results = new ArrayList<List<StudySite>>(studies.size());
 
         for (Study study : studies) {
             String ext = study.getSecondaryIdentifierValue(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
             if (ext != null) {
                 Id id = new Id();
                 id.setExtension(ext);
                 gov.nih.nci.coppa.services.pa.StudySite[] studySites = getCoppaAccessor(bundleContext).searchStudySitesByStudyProtocolId(id);
 
                 if (studySites == null || studySites.length == 0) {
                     results.add(null);
                 } else {
                     II[] researchOrgIIs = getResearchOrganizationIds(studySites);
 
                     gov.nih.nci.coppa.po.Id[] researchOrgIds = tranformIds(gov.nih.nci.coppa.po.Id.class, researchOrgIIs);
                     ResearchOrganization[] researchOrgs = getCoppaAccessor(bundleContext).getResearchOrganizations(researchOrgIds);
 
                     II[] orgIIs = getPlayerIds(researchOrgs);
 
                     gov.nih.nci.coppa.po.Id[] orgIDs = tranformIds(gov.nih.nci.coppa.po.Id.class, orgIIs);
                     Organization[] organizations = getOrganizationsById(orgIDs);
 
                     List<Site> sites = pscSites(organizations);
 
                     results.add(buildStudySites(sites));
                 }
             }
         }
 
         return results;
     }
 
     private Organization[] getOrganizationsById(gov.nih.nci.coppa.po.Id[]  ids) {
         List<Organization> orgs = new ArrayList<Organization>();
         for (gov.nih.nci.coppa.po.Id id : ids) {
             Organization org = getCoppaAccessor(bundleContext).getOrganization(id);
             if (org != null) {
                 orgs.add(org);
             }
         }
         return orgs.toArray(new Organization[0]);
     }
 
 
     private II[] getResearchOrganizationIds(gov.nih.nci.coppa.services.pa.StudySite[] studySites) {
         List<II> ids = new ArrayList<II>();
         for(gov.nih.nci.coppa.services.pa.StudySite studySite : studySites) {
             II ii = studySite.getResearchOrganization();
             ids.add(ii);
         }
         return ids.toArray(new II[0]);
     }
 
     private List<StudySite> buildStudySites(List<Site> sites) {
         List<StudySite> studySites = new ArrayList<StudySite>();
         for (Site site : sites) {
             StudySite ss = new StudySite();
             ss.setSite(site);
             studySites.add(ss);
         }
         return studySites;
     }
 
     // COPPA does not support navigating the relationships in this way.
     // Since StudySiteConsumer will never remove a StudySite, returning empty
     // like this is safe.
     public List<List<StudySite>> getAssociatedStudies(List<Site> sites) {
         List<List<StudySite>> none = new ArrayList<List<StudySite>>(sites.size());
         while (none.size() < sites.size()) {
             none.add(Collections.<StudySite>emptyList());
         }
         return none;
     }
 
     public String providerToken() {
         return CoppaProviderConstants.PROVIDER_TOKEN;
     }
 
     public Integer getRefreshInterval() {
         return 15;
     }
 }
