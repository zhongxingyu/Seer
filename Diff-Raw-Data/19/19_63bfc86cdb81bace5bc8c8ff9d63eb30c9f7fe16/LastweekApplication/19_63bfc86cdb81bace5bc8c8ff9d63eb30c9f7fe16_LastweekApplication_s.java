 /*
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.web.application;
 
 
 import loc.marc.commons.business.services.general.GeneralService;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.Request;
 import org.apache.wicket.Response;
 import org.apache.wicket.Session;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
 import org.apache.wicket.protocol.http.request.CryptedUrlWebRequestCodingStrategy;
 import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
 import org.apache.wicket.request.IRequestCodingStrategy;
 import org.apache.wicket.request.IRequestCycleProcessor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.marc.lastweek.business.services.aaa.AaaService;
 import com.marc.lastweek.business.services.classifiedads.ClassifiedAdsService;
 import com.marc.lastweek.business.services.images.ImageService;
 import com.marc.lastweek.business.services.mail.MailService;
 import com.marc.lastweek.web.pages.classifiedad.ClassifiedAdDetailPage;
 import com.marc.lastweek.web.pages.classifiedadslisting.FavoriteClassifiedAdsPage;
 import com.marc.lastweek.web.pages.classifiedadslisting.FilterResultsPage;
 import com.marc.lastweek.web.pages.facebook.CrossDomainReceiverPage;
 import com.marc.lastweek.web.pages.facebook.TestPage;
 import com.marc.lastweek.web.pages.main.MainPage;
 import com.marc.lastweek.web.session.LastweekSession;
 
 
 @Component
 public class LastweekApplication extends WebApplication {
 
 	
     @Autowired
     private AaaService aaaService;
    
     @Autowired
     private GeneralService generalService;
    
     @Autowired
     private ClassifiedAdsService classifiedAdsService;
     
     @Autowired
     private ImageService imageService;
     
     @Autowired
     private MailService mailService;
    
     public LastweekApplication() {
         super();
     }
    
     @Override
     public Class<? extends WebPage> getHomePage() {
         return MainPage.class;
     }
 
   
 
    
     /* (non-Javadoc)
      * @see org.apache.wicket.authentication.AuthenticatedWebApplication#init()
      */
     @Override
     protected void init() {
        
         super.init();
         
         
         /*
          * Mount bookmarkeable pages for prettyfied URLs
          */
        mountBookmarkablePage("/search", FilterResultsPage.class);
         mountBookmarkablePage("/details", ClassifiedAdDetailPage.class);
         mountBookmarkablePage("/favorites", FavoriteClassifiedAdsPage.class);
         mountBookmarkablePage("/facebookconnect", CrossDomainReceiverPage.class);
         mountBookmarkablePage("/facebooktest", TestPage.class);
        
         /*
          * Remove wicket tags from result HTML
          */
         getMarkupSettings().setStripWicketTags(true);
     }  
    
     /*
      * This enables the encryption of non-prettified URLs
      */
     @Override
     protected IRequestCycleProcessor newRequestCycleProcessor() {
     	
         return new WebRequestCycleProcessor() {
             @Override
             protected IRequestCodingStrategy newRequestCodingStrategy() {
                 return new CryptedUrlWebRequestCodingStrategy(new WebRequestCodingStrategy());
             }
         };
     }  
 
     
     @Override
     public Session newSession(Request request, Response response) {
         return new LastweekSession(request);
     }
     
     public static LastweekApplication get() {
         return (LastweekApplication) Application.get();
     }
 
     public AaaService getAaaService() {
         return this.aaaService;
     }
 
     public GeneralService getGeneralService() {
         return this.generalService;
     }
 
     public ClassifiedAdsService getClassifiedAdsService() {
         return this.classifiedAdsService;
     }
 
 	public ImageService getImageService() {
 		return this.imageService;
 	}
     
     public MailService getMailService() {
     	return this.mailService;
     }
 
 }
 
