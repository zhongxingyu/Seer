 package org.motechproject.ghana.national.repository;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.ektorp.CouchDbConnector;
 import org.ektorp.ViewQuery;
 import org.ektorp.support.View;
 import org.joda.time.DateTime;
 import org.motechproject.dao.MotechBaseRepository;
 import org.motechproject.ghana.national.domain.IVRCallCenterNoMapping;
 import org.motechproject.ghana.national.domain.IVRChannelMapping;
 import org.motechproject.ghana.national.domain.mobilemidwife.Language;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.model.Time;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 import java.util.Arrays;
 import java.util.List;
 
 @Repository
 public class AllIvrCallCenterNoMappings extends MotechBaseRepository<IVRCallCenterNoMapping> {
 
     private List<IVRCallCenterNoMapping> allMappings;
 
     @Autowired
     public AllIvrCallCenterNoMappings(@Qualifier("couchDbConnector") CouchDbConnector db) {
         super(IVRCallCenterNoMapping.class, db);
         allMappings = super.getAll();
     }
 
     public List<IVRCallCenterNoMapping> allMappings(){
        return allMappings.isEmpty() ? super.getAll() : allMappings;
     }
 
     @View(name = "find_by_lang_day_and_time", map = "function(doc) { if(doc.type === 'IVRCallCenterNoMapping') emit([doc.language, doc.dayOfWeek, doc.startTime, doc.endTime], doc) }")
     public IVRCallCenterNoMapping findByLangDayAndTime(Language language, DayOfWeek dayOfWeek, Time startTime, Time endTime) {
         if (language == null || dayOfWeek == null || startTime == null || endTime == null) {
             return null;
         }
 
         ViewQuery viewQuery = createQuery("find_by_lang_day_and_time").key(Arrays.asList(language, dayOfWeek, startTime, endTime)).includeDocs(true).cacheOk(true);
         List<IVRCallCenterNoMapping> ivrChannelMappings = db.queryView(viewQuery, IVRCallCenterNoMapping.class);
         return CollectionUtils.isEmpty(ivrChannelMappings) ? null : ivrChannelMappings.get(0);
     }
 
     @Override
     public void add(IVRCallCenterNoMapping ivrCallCenterNoMapping) {
         IVRCallCenterNoMapping ivrCallCenterNoMappingFromDb = findByLangDayAndTime(ivrCallCenterNoMapping.getLanguage(), ivrCallCenterNoMapping.getDayOfWeek(), ivrCallCenterNoMapping.getStartTime(), ivrCallCenterNoMapping.getEndTime());
         if (ivrCallCenterNoMappingFromDb == null) {
             super.add(ivrCallCenterNoMapping);
         } else {
             update(ivrCallCenterNoMapping, ivrCallCenterNoMappingFromDb);
         }
     }
 
     private void update(IVRCallCenterNoMapping ivrCallCenterNoMapping, IVRCallCenterNoMapping ivrCallCenterNoMappingFromDb) {
         ivrCallCenterNoMappingFromDb.language(ivrCallCenterNoMapping.getLanguage());
         ivrCallCenterNoMappingFromDb.dayOfWeek(ivrCallCenterNoMapping.getDayOfWeek());
         ivrCallCenterNoMappingFromDb.startTime(ivrCallCenterNoMapping.getStartTime());
         ivrCallCenterNoMappingFromDb.endTime(ivrCallCenterNoMapping.getEndTime());
         ivrCallCenterNoMappingFromDb.phoneNumber(ivrCallCenterNoMapping.getPhoneNumber());
         super.update(ivrCallCenterNoMappingFromDb);
 
     }
 
 }
