 package rssscout;
 
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class CheckStatusTimer {
 
     Toolkit toolkit;
     Timer timer;
 
     public CheckStatusTimer() {
         toolkit = Toolkit.getDefaultToolkit();
         timer = new Timer();
         int timeBetweenSearches = MyBot.getTimeBetweenSearches();        
        int milliseconds = timeBetweenSearches * 60 * 1000;
         // 1 800 000
         
         timer.schedule(new RemindTask(), 10000, //initial delay (20 seconds)
                 milliseconds); //subsequent rate (30 minutes)
     }
 
     class RemindTask extends TimerTask {
 
         @Override
         public void run() {
 
             String chan = MyBot.getChan();
             
             
             ArrayList<RssObject> thisRssOb = MyBot.getRssObject();
             
             for (int i = 0;i<thisRssOb.size(); i++)
             {
                 RSSReader thisReader = new RSSReader();
                 ArrayList<String> thisAL = thisRssOb.get(i).searchTermsList;
                 String url = thisRssOb.get(i).url;
                 String chans = thisRssOb.get(i).chans;
                 String color = thisRssOb.get(i).color;
                 thisReader.writeNews(url, chans, color, thisAL); // computers & tech
             }
 
         }
     }
 }
