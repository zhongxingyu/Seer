 package ted;
 
 import java.util.Vector;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2007)
  * 
  * This is the counter that counts down to the next parse round of ted
  * 
  * @author Roel
  * @author Joost
  *  
  * ted License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  *
  */
 
 public class TedParseHandler extends Thread
 {
 	/* 
 	 * TedParseHandler keeps track of the shows that are currently parsed.
 	 * These shows are contained in the runningThreads vector
 	 * 
 	 * There are multiple ways to initialize the parseHandler, empty, single serie,
 	 * single TedParser or a vector of TedParser. Before calling start() on the handler
 	 * you can add as may show as you want by using addParseThread.
 	 * 
 	 * This thread will run as long as their are still show parsing. After all
 	 * shows are finished it will destroy itself.
 	 * 
 	 * By calling stopParsing() isParsing will be set on true which causes the 
 	 * threads to stop.
 	 * 
 	 */
 	private Vector<TedParser> runningThreads;
 	private volatile Thread parseThread;
 	private TedMainDialog mainDialog;
 	
 	public TedParseHandler(TedMainDialog mainDialog)
 	{
 		runningThreads = new Vector<TedParser>();
 		this.mainDialog = mainDialog;
 	}
 	
 	public TedParseHandler(TedSerie serie, TedMainDialog mainDialog)
 	{
 		runningThreads = new Vector<TedParser>();
 		this.mainDialog = mainDialog;
 		this.addParseThread(serie, false);
 	}
 	
 	public TedParseHandler(TedParser thread, TedMainDialog mainDialog)
 	{
 		runningThreads = new Vector<TedParser>();
 		runningThreads.add(thread);
 		this.mainDialog = mainDialog;
 	}
 	
 	public TedParseHandler(Vector<TedParser> threads, TedMainDialog mainDialog)
 	{
 		runningThreads = new Vector<TedParser>();
 		runningThreads = threads;
 		this.mainDialog = mainDialog;
 	}
 	
 	public TedParseHandler(TedSerie serie, TedMainDialog tedMainDialog,
 			boolean forceParse) 
 	{
 		runningThreads = new Vector<TedParser>();
 		this.mainDialog = tedMainDialog;
 		this.addParseThread(serie, forceParse);
 	}
 
 
 	public void start() 
 	{
 		parseThread = new Thread(this);
 		parseThread.start();
     }
 	
 	public void stopThread()
 	{
 		parseThread = null;
 		
 		// call garbage collector to cleanup dirt
 		Runtime.getRuntime().gc(); 
 	}
 	
 	/*
 	 * Stops the parsing of all the shows
 	 */
 	public void stopParsing()
 	{
 		TedLog.simpleLog("Parsing of the shows interrupted by user\n");
 		mainDialog.setStopParsing(true);
 		this.stopThread();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Thread#run()
 	 */
 	public void run()
 	{ 
 		Thread thisThread = Thread.currentThread();
 		
 		// disables menu buttons, etc...
 		mainDialog.setStatusToParsing();
 	
 		// Start the parsings of the shows
 		for(int thread=0; thread<runningThreads.size(); thread++)
 		{
 			runningThreads.get(thread).start();
 		}
 		
 		// As long as stopThread() isn't called continue
 		// this is a nice way to stop the thread (stop() is decrepated)
 		while(thisThread == parseThread)
 		{
 			boolean allStopped = true;
 			try 
 			{
                 thisThread.sleep(1);
                 
                 for(int thread=0; thread<runningThreads.size(); thread++)
         		{
                 	// as long as their is one show still parsing continue
                 	allStopped &= !runningThreads.get(thread).isAlive();
         		}
                 
                 if(allStopped)
                 {
                 	// stop this thread
                 	this.stopThread();
                 }
             } 
 			catch (InterruptedException e)
 			{
             }
 		}
 		
 		// set everything back to normal
 		mainDialog.getSerieTable().tableUpdate();
 		mainDialog.setStatusToIdle();
 	}
 	
 	/*
 	 * Add a show which has to be parsed
 	 */	
 	public void addParseThread(TedSerie serie, boolean forceParse) 
 	{
 		// if it is the day to start checking the serie again
 		serie.updateShowStatus();
 		
 		TedParser tp = new TedParser(serie, mainDialog, forceParse);
 		runningThreads.add(tp);		
 	}
 }
