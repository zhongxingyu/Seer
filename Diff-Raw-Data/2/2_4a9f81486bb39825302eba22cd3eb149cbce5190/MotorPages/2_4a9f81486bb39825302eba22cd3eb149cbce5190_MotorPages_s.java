 package com.motorpast.additional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.motorpast.pages.ErrorPage;
 import com.motorpast.pages.HelpPage;
 import com.motorpast.pages.Index;
 import com.motorpast.pages.MileageResultCaptchaPage;
 import com.motorpast.pages.ResultPage;
 import com.motorpast.pages.SiteNoticePage;
 import com.motorpast.pages.SitemapXml;
 
 public enum MotorPages
 {
     /**
      * pages for navigation ordered by priority, 0 = highest...
      */
     Index(Index.class, true),
     Help(HelpPage.class, true),
    SiteNotice(SiteNoticePage.class, true),
     Error(ErrorPage.class, false),
     MileageResultCaptcha(MileageResultCaptchaPage.class, false),
     MileageResult(ResultPage.class, false),
     Sitemap(SitemapXml.class, false),
     ;
 
 
     private final boolean showInNavigation;
     private final Class<?> clazz;
 
     private MotorPages(final Class<?> clazz, final boolean showInNavigation) {
         this.showInNavigation = showInNavigation;
         this.clazz = clazz;
     }
 
 
     private static final List<Class<?>> Pages;
 
     static {
         Pages = new ArrayList<Class<?>>(MotorPages.values().length);
 
         for(MotorPages page : MotorPages.values()) {
             if(page.showInNavigation) {
                 Pages.add(page.clazz);
             }
         }
     }
 
     /**
      * @return all pages for the navigation ordered by display-priority
      */
     public final static Class<?>[] getPagesForNavigation() {
         final Class<?>[] pagesForNavigation = new Class<?>[Pages.size()];
 
         for(int i = 0; i < Pages.size(); i++) {
             pagesForNavigation[i] = Pages.get(i);
         }
 
         return pagesForNavigation;
     }
 }
