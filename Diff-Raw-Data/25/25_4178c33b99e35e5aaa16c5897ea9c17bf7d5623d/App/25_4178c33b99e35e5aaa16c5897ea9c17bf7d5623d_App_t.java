 package ru.xrm.app;
 
 import java.util.Date;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import ru.xrm.app.config.Config;
 import ru.xrm.app.domain.VacancyLink;
 import ru.xrm.app.domain.VacancyPage;
 import ru.xrm.app.domain.VacancySection;
 import ru.xrm.app.httpclient.CachingHttpFetcher;
 import ru.xrm.app.httpclient.UrlHelper;
 
 public class App 
 {
 
 	public static void main( String[] args )
 	{
 		UrlHelper urlHelper= new UrlHelper();
 		Config config=new Config();
 		try{
 			config.load("config.xml");
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		String homePage="http://e1.ru/business/job";
 
 		CachingHttpFetcher hf=new CachingHttpFetcher("cache");
 
 		// home page
 		String content = hf.fetch(homePage,"windows-1251");
 
 		VacancySectionParser vsp=new VacancySectionParser(config, content);
 		List<VacancySection> sections=vsp.parse();
 
 		// get one kindOfBusiness
 		for (VacancySection section:sections){
 			System.out.format("\n*** NEW SECTION: %s ***\n",section.getName());
 			String vacancyListUrl=section.getHref();
 
 			if (vacancyListUrl.charAt(0)=='/'){
 				// relative url
 				vacancyListUrl=urlHelper.getBasename(homePage)+vacancyListUrl;
 			}
 
 			// get first page of vacancies
 			content=hf.fetch(vacancyListUrl,"windows-1251");
 			VacancyListOnePageParser vlopp=new VacancyListOnePageParser(config, content);
 			// get rest pages
 			VacancyPage firstPage=new VacancyPage(vacancyListUrl);
 			List<VacancyPage> pages=vlopp.getPages();			
 			pages.add(0, firstPage);
 			SortedSet<VacancyPage> allPages=new TreeSet<VacancyPage>(pages);
 
 			Deque<VacancyPage> pageQueue = new LinkedList<VacancyPage>();
 			// push all pages to pageQueue
 			for (VacancyPage p:pages){
 				pageQueue.addLast(p);
 			}
 
 			int pageCounter=0;
 			
 			// loop through all pages
 			while(true){
 				if (pageQueue.isEmpty())break;
 				VacancyPage p=pageQueue.poll();
 				String vacancyNextPageUrl=p.getHref();
 				if (vacancyNextPageUrl.charAt(0)=='/'){
 					vacancyNextPageUrl=urlHelper.getBasename(homePage)+vacancyNextPageUrl;
 				}
 				// will not process page0 again
 				if (vacancyNextPageUrl.endsWith("page=0")){
 					continue;
 				}
				//System.out.format("%d %s ",pageCounter, vacancyNextPageUrl);
				System.out.format(" -- %d --\n",pageCounter);
 				// try to add some new pages
 				List<VacancyPage> newPages=vlopp.getPages();
 				for (VacancyPage newPage:newPages){
 					boolean exists=false;
 					for (VacancyPage ap:allPages){
 						if (ap.getHref().equals(newPage.getHref())){
 							exists=true;
 						}
 					}
 
 					if (!exists){
 						allPages.add(newPage);
 						pageQueue.addLast(newPage);
 					}
 				}
 
				
 				content=hf.fetch(vacancyNextPageUrl, "windows-1251");
				
 				vlopp.setHtml(content);
 
 				List<VacancyLink> lvl = vlopp.parse();
 
 				for (VacancyLink vl: lvl){
 
 					String link=vl.getHref();
 					if (link.charAt(0)=='/'){
 						link=urlHelper.getBasename(homePage) + link;
 					}
 					//System.out.format("Fetching %s\n",link);
 
 					// get vacancy itself
					Date d1=new Date();
					content=hf.fetch(link, "windows-1251");
					Date d2=new Date();
					System.out.format(" %d ms ",d2.getTime()-d1.getTime());
 					//VacancyParser vacancyParser=new VacancyParser(config, content);
 					//Vacancy v=vacancyParser.parse();
 
 				} // loop vacancies
				System.out.println();
 				pageCounter++;
 			}// loop pages
 		}
 		System.out.println("Done!");
 	}
 }
