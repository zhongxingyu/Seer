 package cz.dagblog.echo2blogger;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 public class Main {
 	private static final Logger log = Logger.getLogger(Main.class.getName());
 
 	public static void main(String[] args) throws Exception {
 		if(args.length != 4) {
			System.out.println("Missing input arguments: username, password, blog id for access to Blogger and registered site name for access to Echo service.\n Example java -jar echo2blogger roman.pichlik@gmail.com secret 4053149 dagblog.cz");
 			System.exit(0);
 		}
 		log.info("Starting comments migration from Echo service for site " + args[3] + "  to Blogger for blog " + args[0] + ". There is known limitation of Blogger API, a custom author for comments is currently not supported. All new comments will appear as if they were created by the currently authenticated user. An original author is preserved on first line of comment.");		
 		log.warning("Existing comments will be overwritten. Do you want to still continue? Please press any key for continue...");		
 		System.in.read();
 		log.warning("Please temporary disable Blogger comments email notification otherwise your maibox will blow up? Please press any key for continue...");
 		System.in.read();
 		log.warning("Migration process may take serious amount of time regarding to count of comments. You may break this process anytime and start from the beginning.");
 		Blogger blogger = new Blogger(args[0], args[1], args[2]);
 		Echo echo = new Echo(args[3]);		
 		List<String> ids = blogger.fetchPostIds();
 		int commentsCounter = 0;
 		
 		for (String id : ids) {
 			List<Echo.Comment> comments = echo.fetchComments(id);
 			if(comments.size() > 0) {
 				log.info("Transfering comments for post " + id);
 				commentsCounter += comments.size();
 				log.info("Number of comments " + comments.size());
 				blogger.createOrUpdateComments(id, comments);
 			}	
 			break;
 		}
 		log.info("Migration process finished. Totaly transfered " + commentsCounter + " comments");
 	}
 
 }
