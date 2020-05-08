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
 import ru.xrm.app.threads.OnePageWorker;
 
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
 		String basename=urlHelper.getBasename(homePage);
 
 		CachingHttpFetcher hf=new CachingHttpFetcher("cache");
 
 		// home page
 		String content = hf.fetch(homePage,"windows-1251");
 
 		VacancySectionParser vsp=new VacancySectionParser(config, content);
 		List<VacancySection> sections=vsp.parse();
 
 		Date d1=new Date();
 		
 		// for all sections
 		for (VacancySection section:sections){
 			System.out.format("\n*** NEW SECTION: %s ***\n",section.getName());
 			String vacancyListCurrentPageUrl=section.getHref();
 
 			vacancyListCurrentPageUrl=urlHelper.constructAbsoluteUrl(vacancyListCurrentPageUrl, basename);
 
 			// get first page of vacancies
 			content=hf.fetch(vacancyListCurrentPageUrl,"windows-1251");
 			VacancyListOnePageParser onePageParser=new VacancyListOnePageParser(config, content);
 			// get pages except current, link to first page we'll get from other pages
 			List<VacancyPage> pages=onePageParser.getPages();		
 			// if we have just one page, then getPages returns zero size list
 			if (pages.size()==0){
 				pages.add(new VacancyPage(vacancyListCurrentPageUrl));
 			}
 
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
 				vacancyNextPageUrl=urlHelper.constructAbsoluteUrl(vacancyNextPageUrl, basename);
 				
 				//System.out.format("%d %s ",pageCounter, vacancyNextPageUrl);
 				System.out.format("%d ",pageCounter);
 				// try to add some new pages
 				List<VacancyPage> newPages=onePageParser.getPages();
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
 	
				OnePageWorker opw=new OnePageWorker(config, vacancyNextPageUrl, basename, "windows-1251", hf);
 				try {
 					opw.call();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				pageCounter++;
 				if (pageCounter%30==0){
 					System.out.println();
 				}
 			}// loop pages
 			System.out.println();
 		}
 		Date d2=new Date();
 		System.out.format("\n %d ms. \n",d2.getTime()-d1.getTime());
 		System.out.println("Done!");
 	}
 }
