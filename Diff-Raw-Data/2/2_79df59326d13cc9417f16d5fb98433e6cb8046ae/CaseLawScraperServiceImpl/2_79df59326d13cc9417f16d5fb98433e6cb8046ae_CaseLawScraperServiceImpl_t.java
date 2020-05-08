 package lawscraper.server.service;
 
 import lawscraper.server.components.PartFactory;
 import lawscraper.server.entities.caselaw.CaseLaw;
 import lawscraper.server.repositories.CaseLawRepository;
 import lawscraper.server.scrapers.ZipDataUtil;
 import lawscraper.server.scrapers.caselawscraper.CaseLawScraper;
 import lawscraper.shared.scraper.LawScraperSource;
 import lawscraper.shared.scraper.ScraperStatus;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Created by erik, IT Bolaget Per & Per AB
  * Copyright Inspectera AB
  * Date: 2/24/12
  * Time: 9:07 AM
  */
 
 @Service("CaseLawScraperServiceImpl")
 @Transactional(readOnly = true)
 public class CaseLawScraperServiceImpl implements CaseLawScraperService {
 
     @Autowired
     PartFactory partFactory;
     @Autowired
     CaseLawRepository caseLawRepository;
 
     public CaseLawScraperServiceImpl() {
         System.out.println("ScraperService instantiated");
     }
 
     @Override
     public ScraperStatus scrapeCaseLaws(LawScraperSource lawScraperSource) {
         switch (lawScraperSource) {
             case INTERNET:
                 return scrapeLawsFromInternet();
             case ZIPFILE:
                 return scrapeLawsFromZipFile();
             default:
                 throw new IllegalArgumentException();
         }
     }
 
 
     private ScraperStatus scrapeLawsFromZipFile() {
         ScraperStatus scraperStatus = new ScraperStatus();
         int i = 0;
         Set<CaseLaw> caseLaws = new HashSet<CaseLaw>();
         CaseLawScraper scraper;
         try {
             for (ZipDataUtil.CaseLawEntry caseLawEntry : ZipDataUtil.getAllCaseLaws()) {
                 scraper = new CaseLawScraper();
                 try {
                     System.out.println("--");
                     System.out.println("Scraping...");
                     scraper.parse(caseLawEntry.getInputStream());
                     scraperStatus.increaseScrapedLaws();
 
                    //hello

                     caseLaws.add(scraper.getCaseLaw());
 
                     System.out.println("Done: Scraped laws: " + scraperStatus.getScrapedLaws());
                     System.out.println("--");
 
                     if (i == 10) {
                         saveCaseLaws(caseLaws);
                         i = 0;
                         caseLaws.clear();
                     }
                     i++;
                 } catch (Exception e) {
                     System.out.println("Failed to parse " + caseLawEntry.getName());
                     e.printStackTrace();
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return scraperStatus;
     }
 
     @Transactional(readOnly = false)
     private void saveCaseLaws(Set<CaseLaw> caseLaws) {
         for (CaseLaw caseLaw : caseLaws) {
             caseLawRepository.save(caseLaw);
         }
     }
 
     private ScraperStatus scrapeLawsFromInternet() {
         ScraperStatus scraperStatus = new ScraperStatus();
         return scraperStatus;
     }
 }
