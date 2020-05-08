 package fr.cg95.cvq.service.request.civil.impl;
 
import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.civil.BirthDetailsRequest;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.request.civil.IBirthDetailsRequestService;
 import fr.cg95.cvq.service.request.condition.EqualityChecker;
 import fr.cg95.cvq.service.request.condition.EqualityListChecker;
 import fr.cg95.cvq.service.request.impl.RequestService;
 import java.util.Arrays;
 
 /**
  * Implementation of the {@link IBirthDetailsRequestService birth details request service}.
  * 
  * @author bor@zenexity.fr
  */
 public final class BirthDetailsRequestService 
     extends RequestService implements IBirthDetailsRequestService {
     
     @Override
     public boolean accept(Request request) {
         return request instanceof BirthDetailsRequest;
     }
 
     @Override
     public Request getSkeletonRequest() throws CvqException {
         BirthDetailsRequest request = new BirthDetailsRequest();
        // this test is here only because, on frontoffice homepage, requests are created
         // to display their translated labels, while the currentSite has already been reset by
         // the "after" in openSessionInViewFilter
         if (SecurityContext.getCurrentSite() != null) {
            LocalAuthority localAuthority = SecurityContext.getCurrentSite();
            request.setBirthCity(localAuthority.getDisplayTitle());
            request.setBirthPostalCode(localAuthority.getPostalCode().substring(0,2));
         }
         return request;
     }
 
     protected void initFilledConditions() {
         super.initFilledConditions();
         filledConditions.put("requesterQuality", new EqualityChecker("Other"));
         filledConditions.put("format",
             new EqualityListChecker(Arrays.asList("FullCopy", "ExtractWithRelationship")));
     }
 }
