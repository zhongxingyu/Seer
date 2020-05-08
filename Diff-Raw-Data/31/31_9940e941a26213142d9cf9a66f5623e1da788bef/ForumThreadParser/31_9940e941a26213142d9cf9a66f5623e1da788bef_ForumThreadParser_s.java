 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 public class ForumThreadParser {
 
 	private Document doc;
 	private Elements  threadList;
 	
 	public ForumThreadParser() throws IOException{
		doc = Jsoup.connect("http://www.forum.hr/forumdisplay.php?f=141").get();
		threadList = doc.getElementById("threadslist").getElementsByTag("tr");
 	}
 	
 	public List<ForumThread> getThreadList(){
 		List<ForumThread> thList = new ArrayList<ForumThread>();
 		int threadId;
 		int counter = 0;
 		for (Element e : threadList){
 			ForumThread thread = new ForumThread();
 			if (counter >= 2){
 				//get thread id
 				threadId = Integer.parseInt(e.child(0).attr("id").split("_")[2]);
 				thread.id = threadId;
 				System.out.println(thread.id);
 				
 				//get thread url
 				thread.threadUrl = "http://forum.hr/showthread.php?t=" + threadId;
 				System.out.println(thread.threadUrl);
 				
 				//get thread name
 				Elements asd = e.getElementsByTag("div").get(0).getElementsByTag("a");
 				for (Element a : asd){
 					if (a.attr("id").matches("thread_title_"+threadId)){
 						thread.threadName = a.text();
 						break;
 					}
 				}
 				System.out.println(thread.threadName);
 				//get number of pages
 				Elements anchors = e.getElementsByTag("span").tagName("a");
 				if (!anchors.select("[href^=showthread.php]").isEmpty())
 					thread.numOfPages = Integer.parseInt(anchors.select("[href^=showthread.php]").last().attr("href").split("&page=")[1]);
 				if (thread.numOfPages == 0)
 					thread.numOfPages = 1;
 				
 				//get isTop thread
 				Elements divTags  = e.getElementsByTag("img").select("[alt=Top tema]");
 				if (divTags.isEmpty()){
 					thread.isTop = false;
 				}
 				else{
 					thread.isTop = true;
 				}
 				
 				//get information about last post
 				divTags  = e.getElementsByTag("div").select("[class=smallfont]");
 				/*
 				for (Element dt : divTags)
 					System.out.println(dt);
 				*/
 				
 				if (!divTags.isEmpty()){
 					thread.lastPost = divTags.select("[style=text-align:right; white-space:nowrap]").get(0).text();
 					thread.threadAuthor = divTags.select("[style=cursor:pointer]").get(0).text();
 				}
 				System.out.println(thread.lastPost);
 				System.out.println(thread.threadAuthor);
 				System.out.println(thread.isTop);
 				System.out.println("Broj stranica: " + thread.numOfPages);
 				System.out.println("######################################");
 			}
 			counter += 1;
 			thList.add(thread);
 		}
 		System.out.println(threadList.size());
 		return thList;
 	}
 	
 }
