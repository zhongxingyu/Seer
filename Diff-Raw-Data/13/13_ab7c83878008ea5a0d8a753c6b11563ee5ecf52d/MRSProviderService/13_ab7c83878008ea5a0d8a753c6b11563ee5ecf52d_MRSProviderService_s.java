 package org.motechproject.care.reporting.ft.couch.service;
 
 import org.motechproject.care.reporting.ft.couch.domain.Provider;
 import org.motechproject.care.reporting.ft.utils.TimedRunner;
 import org.motechproject.care.reporting.ft.utils.TimedRunnerBreakCondition;
 import org.motechproject.couch.mrs.model.CouchProviderImpl;
 import org.motechproject.couch.mrs.repository.impl.AllCouchProvidersImpl;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 @Component
 public class MRSProviderService {
 
     private final AllCouchProvidersImpl allCouchProviders;
     private final MRSPersonService mrsPersionService;
 
     @Autowired
     public MRSProviderService(AllCouchProvidersImpl allCouchProviders, MRSPersonService mrsPersionService) {
         this.allCouchProviders = allCouchProviders;
         this.mrsPersionService = mrsPersionService;
     }
 
    public Provider waitAndFindByProviderId(final String providerId, final int tries, final int intervalSleep, TimedRunnerBreakCondition breakCondition)  {
         TimedRunner<Provider> timedRunner = new TimedRunner<Provider>(tries, intervalSleep, breakCondition) {
             @Override
             protected Provider run() {
                 return getFor(providerId);
             }
         };
 
         try {
             return timedRunner.executeWithTimeout();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 

     public Provider getFor(String providerId) {
         if (providerId == null) {
             return null;
         }
 
        CouchProviderImpl couchProvider = allCouchProviders.findByProviderId(providerId).get(0);
         return new Provider(couchProvider.getId(), couchProvider.getProviderId(), mrsPersionService.getFor(couchProvider.getPersonId()));
     }
 
     public void delete(String providerId) {
         List<CouchProviderImpl> providers = allCouchProviders.findByProviderId(providerId);
        if(providers.size() == 0) {
             return;
         }
 
         CouchProviderImpl provider = providers.get(0);
         mrsPersionService.delete(provider.getPersonId());
         allCouchProviders.remove(provider);
 
     }
 }
