 package org.motechproject.whp.reports.service;
 
 import org.motechproject.util.DateUtil;
 import org.motechproject.whp.reports.contract.adherence.ProviderAdherenceSummaries;
 import org.motechproject.whp.reports.contract.adherence.ProviderAdherenceSummary;
 import org.motechproject.whp.reports.dao.ProviderAdherenceQueryDAO;
 import org.motechproject.whp.reports.domain.TreatmentWeek;
 import org.motechproject.whp.reports.model.AdherenceStatus;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.motechproject.whp.reports.date.WHPDate.toSqlDate;
 
 @Service
 @Transactional(readOnly = true)
 public class ProviderAdherenceDataService {
 
     private ProviderAdherenceQueryDAO providerAdherenceQueryDAO;
 
     ProviderAdherenceDataService() {
         //for spring proxy
     }
 
     @Autowired
     public ProviderAdherenceDataService(ProviderAdherenceQueryDAO providerAdherenceQueryDAO) {
         this.providerAdherenceQueryDAO = providerAdherenceQueryDAO;
     }
 
     public ProviderAdherenceSummaries getAdherenceSummary(String district) {
         List<ProviderAdherenceSummary> providerAdherenceSummaries = providerAdherenceQueryDAO.getProviderAdherenceSummaries(district);
         Map<String, ProviderAdherenceSummary> adherenceSummaryMap = adherenceSummaryMap(providerAdherenceSummaries);
 
         List<AdherenceStatus> adherenceStatuses = getAdherenceStatusesForPast8Weeks(district);
 
         updateAdherenceSummary(adherenceSummaryMap, adherenceStatuses);
 
         return new ProviderAdherenceSummaries(district, providerAdherenceSummaries);
     }
 
     private void updateAdherenceSummary(Map<String, ProviderAdherenceSummary> adherenceSummaryMap, List<AdherenceStatus> adherenceStatuses) {
         for(AdherenceStatus adherenceStatus : adherenceStatuses){
             if(!adherenceStatus.isAdherenceGiven())
                 adherenceSummaryMap.get(adherenceStatus.getProviderId()).incrementAdherenceMissingWeeks();
         }
     }
 
     private List<AdherenceStatus> getAdherenceStatusesForPast8Weeks(String district) {
        TreatmentWeek treatmentWeek = new TreatmentWeek(DateUtil.today().minusWeeks(9));
         List<AdherenceStatus> adherenceStatuses = new ArrayList<>();
         for(int i=0; i < 8; i++){
             adherenceStatuses.addAll(providerAdherenceQueryDAO.getAdherenceGivenStatus(district,
                     toSqlDate(treatmentWeek.startDate()),
                     toSqlDate(treatmentWeek.endDate())));
             treatmentWeek.moveToNextWeek();
         }
         return adherenceStatuses;
     }
 
     private Map<String, ProviderAdherenceSummary> adherenceSummaryMap(List<ProviderAdherenceSummary> providerAdherenceSummaries) {
         Map<String, ProviderAdherenceSummary> adherenceSummaryMap = new HashMap<>();
         for(ProviderAdherenceSummary adherenceSummary : providerAdherenceSummaries){
             adherenceSummaryMap.put(adherenceSummary.getProviderId(), adherenceSummary);
         }
         return adherenceSummaryMap;
     }
 }
