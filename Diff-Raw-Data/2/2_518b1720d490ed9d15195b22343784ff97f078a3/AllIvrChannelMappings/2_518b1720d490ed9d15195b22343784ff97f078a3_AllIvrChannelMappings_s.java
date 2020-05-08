 package org.motechproject.ghana.national.repository;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.ektorp.CouchDbConnector;
 import org.ektorp.ViewQuery;
 import org.ektorp.support.View;
 import org.motechproject.dao.MotechBaseRepository;
 import org.motechproject.ghana.national.domain.IVRChannelMapping;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 @Repository
 public class AllIvrChannelMappings extends MotechBaseRepository<IVRChannelMapping> {
 
     private List<IVRChannelMapping> allMappings;
 
     @Autowired
     public AllIvrChannelMappings(@Qualifier("couchDbConnector") CouchDbConnector db) {
         super(IVRChannelMapping.class, db);
         allMappings = super.getAll();
     }
 
     public List<IVRChannelMapping> allMappings(){
        return allMappings;
     }
 
     @View(name = "find_by_phone_number_pattern", map = "function(doc) { if(doc.type === 'IVRChannelMapping') emit(doc.phoneNumberPattern, doc) }")
     public IVRChannelMapping findByPhoneNumberPattern(String phoneNumberPattern) {
         if (StringUtils.isEmpty(phoneNumberPattern)) {
             return null;
         }
 
         ViewQuery viewQuery = createQuery("find_by_phone_number_pattern").key(phoneNumberPattern).includeDocs(true).cacheOk(true);
         List<IVRChannelMapping> ivrChannelMappings = db.queryView(viewQuery, IVRChannelMapping.class);
         return CollectionUtils.isEmpty(ivrChannelMappings) ? null : ivrChannelMappings.get(0);
     }
 
     @Override
     public void add(IVRChannelMapping ivrChannelMapping) {
         IVRChannelMapping ivrChannelMappingFromDb = findByPhoneNumberPattern(ivrChannelMapping.getPhoneNumberPattern());
         if (ivrChannelMappingFromDb == null) {
             super.add(ivrChannelMapping);
         } else {
             update(ivrChannelMapping, ivrChannelMappingFromDb);
         }
     }
 
     private void update(IVRChannelMapping ivrChannelMapping, IVRChannelMapping ivrChannelMappingFromDb) {
         ivrChannelMappingFromDb.ivrChannel(ivrChannelMapping.getIvrChannel());
         ivrChannelMappingFromDb.phoneNumberPattern(ivrChannelMapping.getPhoneNumberPattern());
         super.update(ivrChannelMappingFromDb);
 
     }
 
 }
