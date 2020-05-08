 package edu.nouri.photoReX;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import edu.nouri.photoReX.crawler.Crawler;
 
 public class CrawlRunner {
 
 	public static void main(String[] args) 
 	{
 		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); 
 		System.out.print("" + df.format(new Date()) + " "); 
 
 		long start = System.currentTimeMillis(); 
 			if (args.length<1)
 			{
 				System.out.println("Error: No crawler defined in the argument. "); 
 				printUsage(); 
 				return; 
 			}
 		
 		try{
 			Class<?> theClass = Class.forName("edu.nouri.photoReX.crawler." + args[0]); 
 			Crawler crawler; 
 			if (args.length>1)
 			{
 				Constructor<?> initer; 
 				switch(args.length){
 				case 2:
 					initer = theClass.getConstructor(String.class);
 					crawler = (Crawler) initer.newInstance(args[1]); 
 					break;
 				case 3:
 					initer = theClass.getConstructor(String.class, String.class); 
 					crawler = (Crawler) initer.newInstance(args[1], args[2]); 
 					break; 
 				default: 
 					throw new NoSuchMethodException("more arguments provided"); 					
 				}				
 			}else
 				crawler = (Crawler) theClass.newInstance(); 
 				
 			//crawler is now ready
			System.out.print(crawler.redisSetName + "  constructed. Fetching... "); 
 			int rcode = crawler.fetch();
 			long end = System.currentTimeMillis(); 
 			if (rcode==0)
 				System.out.println(" successfully done in " + (end-start)/1000.0 + " seconds."); 
 			else
 				System.out.println(" Error: " + rcode + ". took " + (end-start)/1000.0 + " seconds."); 
 				
 		}catch(ClassNotFoundException ex)
 		{
 			System.out.println("The Crawler name specified was not found"); 
 			ex.printStackTrace(); 
 		}catch(InstantiationException ex){
 			System.out.println("Could not instantiate the crawler:"); 
 			ex.printStackTrace(); 
 		}
 		catch(IllegalAccessException ex){
 			ex.printStackTrace(); 
 		}catch(NoSuchMethodException ex){
 			System.out.println("No method in the specified crawler accepts these arguments"); 
 			ex.printStackTrace(); 
 		}catch(InvocationTargetException ex){
 			ex.printStackTrace(); 
 		}
 	}
 	
 	public static void printUsage()
 	{
 		System.out.println("Usage: java CrawlRunner [crawlerName] [crawlerArgs]"); 
 		System.out.println("The arguments can be one of these:");
 		System.out.println("[crawlerName]=FHPFeaturedCrawler, [crawlerArgs]=featureName (one of these: popular, upcoming, editors, fresh_today, fresh_week)"); 
 	}
 	
 
 }
