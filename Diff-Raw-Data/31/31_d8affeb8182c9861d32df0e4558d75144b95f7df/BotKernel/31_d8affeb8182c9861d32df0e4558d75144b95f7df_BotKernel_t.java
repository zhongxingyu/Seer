 package com.jreddit.botkernel;
 
 import java.io.*;
 import java.util.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import com.omrlnr.jreddit.*;
 import com.omrlnr.jreddit.utils.Utils;
 
 /**
  *
  * Reddit BotKernel
  *
  */
 public class BotKernel implements Runnable {
 
     //
     // Default location of the file which contains bots we will load at
     // startup
     //
     private static final String DEFAULT_BOTS_FILE = "scratch/bots.properties";
 
     //
     // A list of crawler we are managing.
     //
     private List<Crawler> _crawlers = new ArrayList<Crawler>();
 
     //
     // A list of bots we are managing. 
     //
     private List<Bot> _bots = new ArrayList<Bot>();
 
     private Map<Runnable, Thread> _threadMap = new HashMap<Runnable, Thread>();
 
     private static BotKernel _botKernel = null; 
 
     private Object _lock    = new Object();
     private Object _logLock = new Object();
 
     //
     // Ensure there is only one BotKernel in this VM instance.
     //
     public static synchronized BotKernel getBotKernel() {
         if(_botKernel == null) {
             _botKernel = new BotKernel();
         }
         return _botKernel;
     }
 
     /**
      *
      * Add a crawler.
      *
      * @param crawler   The crawler to add.
      *
      */
     public void addCrawler(Crawler crawler) {
         synchronized(_lock) {
 
             log("Crawler to add: " + crawler);
 
             for(Crawler c: _crawlers) {
                 if(c.getName().equals(crawler.getName())) {
                     //
                     // Don't allow multiple crawlers with the same unique 
                     // name.
                     //
                     log("Crawler " + crawler.getName() + 
                         " already added to botkernel crawlers. Skipping.");
                     return;
                 }
             }
 
             //
             // Add the crawler to our list of crawlers
             //
             _crawlers.add(crawler);
     
             //
             // Start the crawler.
             //
             Thread thread = new Thread(crawler);
             thread.setName(crawler.getName());
             thread.start();
     
             _threadMap.put(crawler, thread);
     
             log("addCrawler() Crawler " + crawler.getName() + " started.");
 
         }
     }
 
     /**
      *
      * Remove a crawler form our crawlers.
      *
      * @param crawler   The crawler to remove.
      *
      */
     public void removeCrawler(Crawler crawler) {
         synchronized(_lock) {
             //
             // Remove the crawler from our list.
             //
             _crawlers.remove(crawler);
         }
 
         crawler.shutdown();
 
         //
         // Join thread only outside of synchronized block.
         // Otherwise we deadlock if it is calling into us
         // and grabbing the lock.
         //
         stopThread(crawler); 
     }
 
     public void shutdownKernel() {
 
         log("INFO Shutting down botkernel...");
 
         //
         // Wonder if we should spin a shutdown thread for each
         // one of these...
         //
         for(Bot bot: _bots) {
             log("Shutting down bot " + bot.getName());
             bot.shutdown();
         }
 
         //
         // I don't even know if we care about this so much.
         // This will let our threads join() but we've really
         // already called shutdown() on the bots, so they should have
         // done whatever they needed for shutdown. Maybe this is nice to
         // test deadlocks? Or maybe just comment it out?
         //
         for(Bot bot: _bots) {
             stopThread(bot);
         }
 
         //
        //  Stop crawlers
        //
        for(Crawler crawler: _crawlers) {
            log("Shutting down crawler " + crawler.getName());
            crawler.shutdown();
        }
        for(Crawler crawler: _crawlers) {
            stopThread(crawler);
        }


        //
         // stopThread() will wait for threads to join().
         // Any threads still running are not releasing resources
         // nicely. 
         //
         
         logInfo();
 
         log("INFO BotKernel shut down...");
     }
 
     public void stopBot(String name) {
         Bot removeBot = null;
         synchronized(_lock) {
             for(Bot bot: _bots) {
                 if(bot.getName().equals(name)) {
                     removeBot = bot;
                     break;
                }
             }
     
             //
             // Remove the bot from our bot list.
             //
             if(removeBot != null) {
                 _bots.remove(removeBot);
             }
         }
 
         //
         // Only call into the other bot after we have relased the _lock
         // otherwise we risk deadlock if the bot instance we are shutting
         // down makes calls back into the botkernel.
         //
 
         if(removeBot != null) {
             log("Stopping bot " + name);
             removeBot.shutdown();
                     
             //
             // Let the thread join and remove it from our mapping.
             // But do not join() from within a synchronized block
             // in the event the thread is still doing somehing calling
             // into here (like for log() calls.)
             //
             stopThread(removeBot); 
         }
 
     }
 
     private void stopThread(Runnable runnable) {
 
             try {
                 Thread thread = _threadMap.get(runnable);
                 if(thread != Thread.currentThread()) {
                     // Wait for the bot thread to die
                     log("Waiting for thread " + runnable + " to join()...");
                     thread.join();
                     log("Thread join()'ed...");
                 } else {
                     log("Not waiting on current thread to join() itself.");
                 }
 
                 // Remove from thread map 
                 synchronized(_lock) {
                     log("Removing thread from thread map.");
                     _threadMap.remove(runnable);
                 }
 
                 log("stopThread() Threads:   " + Thread.activeCount() );
                 log("stopThread() Bots:      " + _bots.size() );
                 log("stopThread() Crawlers:  " + _crawlers.size() );
     
             } catch(InterruptedException ie) {
                 ie.printStackTrace();
     
                 //
                 // 
                 //
                 throw new RuntimeException(ie);
             }
     }
 
 
     public void loadBot(String classname) {
         synchronized(_lock) {
             try {
 
                 log("Loading bot " + classname);
     
                 Class clazz = Class.forName(classname);
                 Bot bot = (Bot)clazz.newInstance();
                 bot.init();
             
                 Thread thread = new Thread(bot);
                 thread.setName(bot.getName());
                 thread.start();
             
                 _bots.add(bot);
     
                 _threadMap.put(bot, thread);
     
                 log("Bot " + classname + " (" + bot.getName() + ") loaded.");
     
             } catch (ClassNotFoundException cnfe) {
                 log("Error finding class " + classname);
             } catch (InstantiationException ie) {
                 log("Error instantiating class " + classname);
             } catch (IllegalAccessException iae) {
                 log("Illegal access exception for class " + classname);
             }
         }
     }
 
     //
     // Used for logging
     //
     public static DateFormat DATE_FORMAT = 
                         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
     /**
      * Write a log message.
      */
     public void log(String msg) {
         synchronized(_logLock) {
             Date date = new Date();
             String prettyDate = DATE_FORMAT.format(date);
             System.out.println(
                 prettyDate + " " +
                 "[" + Thread.currentThread().getName() + "] " + 
                 msg);
         }
     }
 
     /**
      *
      * A util method for logging info.
      *
      */
     public void logInfo() {
         log("Threads:   " + Thread.activeCount() );
         log("Bots:      " + _bots.size() );
         log("Crawlers:  " + _crawlers.size() );
     }
 
 
     public void run() { 
         try {
             log("Attempting to load default bots.");
             Properties props = new Properties();
             InputStream in = new FileInputStream(DEFAULT_BOTS_FILE);
             props.load(in);
             in.close();
             
             Set<String> keys = props.stringPropertyNames();
             List<String> sortedKeys = new ArrayList<String>(keys);
             Collections.sort(sortedKeys);
             for(String key: sortedKeys) {
 
                 // log("Inspecting key " + key);
 
                 if(key.startsWith("bots.")) {
 
                     //
                     // We are loading a bot.
                     // 
                     String classname = props.getProperty(key);
                     loadBot(classname);
 
                 }
             }
 
         } catch(IOException ioe) {
             ioe.printStackTrace();
             log("IOException loading default bots.");
         }
 
         log("BotKernel default thread done...");
     }
 
     /**
      *
      * Main entry point for starting the bot kernel.
      *
      */
     public static void main(String[] args) {
         Runnable runnable = BotKernel.getBotKernel();
         Thread mainThread = new Thread(runnable);
         mainThread.setName("MAIN");
         mainThread.start();
         try {
             // System.out.println("Joining bootstrap thread...");
 
             mainThread.join();
             // System.out.println("Joined bootstrap thread...");
 
         } catch (InterruptedException ie) {
             //
             // 
             //
             throw new RuntimeException("Error cannot join bootstrap thread.");
         }
     }
 
 }
