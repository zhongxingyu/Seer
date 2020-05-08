 package webspider.core.crawler;
 
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import webspider.Settings;
 import webspider.gui.CrawlPanel;
 import webspider.actions.SpiderActions;
 import static webspider.Settings.*;
 
 /**
  * Spider interface to be accessed by GUI
  * @author Zsolt Bitvai, Shaabi Mohammed
  *
  */
public class Spider extends Observable implements myIWSpider {
     private SpiderImpl spider;
     private SpiderActions actions;
 
     public Spider(SpiderActions actions) {
         this.actions = actions;
 		try {
 			this.spider = new SpiderImpl(new URL(DEFAULT_URL), this.actions);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
     }
 
     @Override
 	public void openUserInterface() {
 		Settings.BACK_BUTTON = false;
                 actions.setPanel(new CrawlPanel(actions));
                 actions.openInterface();
 	}
 
 	@Override
 	public void closeUserInterface() {
 		actions.closeInterface();
 	}
 
 	@Override
 	public void startIWSpider(String mySeed) {
 		try {
 			this.spider = new SpiderImpl(new URL(mySeed), this.actions);
 			this.spider.start();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public boolean isIWRobotSafe(String myUrl) {
 		checkInit();
 		try {
 			return this.spider.isRobotAllowed(new URL(myUrl));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	@Override
 	public void stopIWSpider() {
 		checkInit();
 		this.spider.stop();
 	}
 
 	@Override
 	public void resumeIWSpider() {
 		checkInit();
 		this.spider.start();
 	}
 
 	@Override
 	public void killIWSpider() {
 		checkInit();
 		try {
 			this.spider.stop();
 			this.spider.printToFile();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} 
 	}
 
 	@Override
 	public String[] getLocalIWUrls() {
 		checkInit();
 		List<String> stringURLs = new ArrayList<String>();
 		for (URL url : this.spider.getLocalLinks()){
 			stringURLs.add(url.toString());
 		}
 		return stringURLs.toArray(new String[]{});
 	}
 
 	@Override
 	public String[] getExternalIWURLs() {
 		checkInit();
 		List<String> stringURLs = new ArrayList<String>();
 		for (URL url : this.spider.getExternalLinks()){
 			stringURLs.add(url.toString());
 		}
 		return stringURLs.toArray(new String[]{});
 	}
 
     // Status update functions
     public String getStatus() {
     	checkInit();
     	return this.spider.getStatus();
     }
 
     public int getLocalLinks() {
     	checkInit();
     	return this.spider.getLocalLinks().size();
     }
 
     public int getDeadLinks() {
     	checkInit();
     	return this.spider.getDeadLinks().size();
     }
 
     public int getNonParsableLinks() {
     	checkInit();
     	return this.spider.getNonParsableLinks().size();
     }
 
     public int getExternalLinks() {
     	checkInit();
     	return this.spider.getExternalLinks().size();
     }
 
     public int getDisallowedLinks() {
     	checkInit();
     	return this.spider.getDisallowedLinks().size();
     }
    
     private void checkInit(){
 //    	if (this.spider == null){
 //			throw new IllegalStateException("Spider hasn't been initialized. Call startIWSpider first");
 //		}
     	//use defautl values
     }
 }
