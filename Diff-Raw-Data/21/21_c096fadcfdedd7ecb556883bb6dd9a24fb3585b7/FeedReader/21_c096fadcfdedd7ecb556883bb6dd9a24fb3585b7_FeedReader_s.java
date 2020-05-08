 import java.net.URL;
 import java.util.Iterator;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 public class FeedReader {
 
 	// Permanent store of 10 stories
 	static Story[] stories = new Story[10];
 	
     public static void main(final String[] args) {
     	
         boolean firstrun = true;
     	
     	while(true){
 	    		try {
 	            	
 	            	// Initialise feed
 	                final URL feedUrl = new URL("http://news.google.com/news?pz=1&cf=all&hl=en&q=Oil&output=rss");
 	                final SyndFeedInput input = new SyndFeedInput();
 	                final SyndFeed feed = input.build(new XmlReader(feedUrl));
 	                
 	                // Initialise storage
 	                Integer index, subIndex, numNews, sHttp;
 	                String sTitle, sDesc, relURL;
 	                SyndEntry synd;
 	                NewsAnalyst nAnal;
 	                Story[] newstories = new Story[10];
 	                
 	                int n = 1;
 	                
 	                for (final Iterator<?> iter = feed.getEntries().iterator();
 	                     iter.hasNext();)
 	                {
 	                		
 	                		// Grabs the RSS object
 	                    	synd = (SyndEntry) iter.next();
 	                    	sTitle = (String) synd.getTitle();         
 	                        sDesc = (String) synd.getDescription().getValue();
 	                       
 	                        // Print the number of related articles
 	                        index = sDesc.lastIndexOf("<b>all ") + 7;
 	                        
 	                        if (index != 6) {           
 	                        	// Grabs the number of articles
 		                        subIndex = sDesc.substring(index).indexOf(" ") + index;
 		                        numNews = Integer.parseInt(sDesc.substring(index, subIndex).replaceAll(",", ""));
 		                        
 	                        	// Grab related articles RSS feed
 		                        sHttp = sDesc.substring(0, index).lastIndexOf("http://");
 		                        relURL = sDesc.substring(sHttp, index - 15).replaceAll("&amp;", "&") + "&output=rss";
 	     
 	                        }
	                        else {
 	                        	// Number of articles not listed
 	                        	index = sDesc.lastIndexOf("<b>and more") + 11;
	                        	numNews = 10;
 	                        	
 	                        	// Grab related articles RSS feed
 		                        sHttp = sDesc.substring(0, index).lastIndexOf("http://");
 		                        relURL = sDesc.substring(sHttp, index - 19).replaceAll("&amp;", "&") + "&output=rss";
	                        	
 	                        }
 	                        
 	                        byte storyid = -1;
 	                        if(!firstrun){
 	                        	// iterates through the ten old stories, checking whether they match
 		                        for(byte i = 0; i < 10; i++){
 		                        	if(stories[i].getLink().compareTo(relURL) == 0 || stories[i].getTitle().compareTo(sTitle) == 0){
 		                        		storyid = i;
 		                        		// if there are many new stories, it will reanalyse the story
 		                        		if(numNews > (10 + stories[i].getSites())){
 		                        			storyid = -1;
 		                        			break;
 		                        		}
 		                        		// updates title and link
 		                        		newstories[n-1] = stories[i];
 		                        		newstories[n-1].setTitle(sTitle);
 		                        		newstories[n-1].setLink(relURL);
 		                        		break;
 		                        	}
 		                        }
 	                        }
 		                    
 	                        if(storyid == -1){
 	                        	// analyses story
 	                        	System.out.println("----- NEW/UPDATED -------");
 	                        	nAnal = new NewsAnalyst(relURL, numNews, n, sTitle);
 	                        	newstories[n-1] = nAnal.getStory();
 	                        }
 	                        
 	                     // prints story details
 	                        System.out.println("--------------" + n + "---------------");
                         	System.out.println("Title: " + newstories[n-1].getTitle());
                         	System.out.println("Sites: " + newstories[n-1].getSites() + "; Rank: " + newstories[n-1].getRank());
                         	System.out.println("URL: " + newstories[n-1].getLink());
                         	System.out.println("Sentiment: " + newstories[n-1].getSentiment());
                         	System.out.println("Keywords: " + newstories[n-1].printKeyWords());
 	                        
 	                        n++;
 	                        Thread.sleep( (5000) ); 
 	                }
 	                
 	                Thread.sleep( (10000) ); 
 	                firstrun = false;
 	                stories = newstories;
 	                
 	            }
 	            catch (Exception ex) {
 	                ex.printStackTrace();
 	                System.out.println("ERROR: " + ex.getMessage());
 	            }
 	    		System.out.println("---------------------------------------------------------");
             }
     }
 }
