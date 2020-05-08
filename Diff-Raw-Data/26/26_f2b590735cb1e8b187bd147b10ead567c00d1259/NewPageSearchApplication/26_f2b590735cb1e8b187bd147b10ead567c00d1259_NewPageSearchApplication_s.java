 package net.inervo.TedderBot.NewPageSearch;
 
 /*
  * Copyright (c) 2011, Ted Timmons, Inervo Networks All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
  * Inervo Networks nor the names of its contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.inervo.WMFWiki11;
 import net.inervo.TedderBot.BotFlag;
 import net.inervo.TedderBot.Configuration;
 import net.inervo.Wiki.PageEditor;
 import net.inervo.Wiki.RetryEditor;
 import net.inervo.Wiki.WikiFetcher;
 import net.inervo.Wiki.WikiHelpers;
 import net.inervo.Wiki.Cache.ArticleCache;
 import net.inervo.Wiki.Cache.CachedFetcher;
 
 public class NewPageSearchApplication {
 	private static final long DAY_IN_MILLISECONDS = 86400000;
 	// consider this an "oversearch" period.
 	public static final int PREPEND_SEARCH_DAYS = -7;
 	private static final Logger logger = Logger.getLogger(NewPageSearchApplication.class.getCanonicalName()); // only
 
 	//private static final String DEBUG_SEARCH = "Astro";
 	private static final String DEBUG_SEARCH = "";
 
 	public static void main( String[] args ) throws Exception
 	{
 		if ( args.length < 2 ) {
 			print( "need params given in this order: AWS prop file, wiki prop file" );
 		}
 
 		for (Handler h : Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getHandlers()) {
 			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).removeHandler(h);
 		}
 		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(new FileHandler("newpagesearch.log"));
 
 		// shutdown hook. Enable early so we don't blow up the cache.
 		System.setProperty("net.sf.ehcache.enableShutdownHook", "true");
 
 		print("hello world!");
 		ArticleCache ac = null;
 
 		String amazonProps = "AwsCredentials.properties";
 		String wikiProps = "wiki.properties";
 
 		if ( args.length >= 2 ) {
 			amazonProps = args[0];
 			wikiProps = args[1];
 		}
 
 		try {
 			PersistentKeystore.initialize( amazonProps );
 			Configuration config = new Configuration( wikiProps );
 
 			WMFWiki11 wiki = new WMFWiki11("en.wikipedia.org");
 			wiki.setMaxLag(15);
 
 			// wiki.setThrottle( 5000 );
 			wiki.login(config.getWikipediaUser(), config.getWikipediaPassword().toCharArray());
 			wiki.setMarkBot(true);
 			print("db lag (seconds): " + wiki.getCurrentDatabaseLag());
 			BotFlag.check(wiki);
 
 			ac = new ArticleCache(wiki);
 			WikiFetcher fetcher = new CachedFetcher(ac);
 
 			PageRules rules = new PageRules(fetcher, "User:AlexNewArtBot/Master", DEBUG_SEARCH);
 
 			PageEditor editor = new RetryEditor(wiki);
 
 			String lastProcessed = PersistentKeystore.get(PersistentKeystore.DEFAULT_KEY,
 					PersistentKeystore.LAST_PROCESSED);
 			print("last processed: " + lastProcessed);
 
 			List<PageRule> ruleList = rules.getRules();
 			
 			// hacky way to sort by "last processed". Take elements before or equal to lastProcessed and put them later.
 			if (lastProcessed != null) {
 				List<PageRule> ruleListBefore = new ArrayList<PageRule>();
 				List<PageRule> ruleListAfter = new ArrayList<PageRule>();
 				
 				boolean found = false;
 				for(PageRule rule: ruleList) {
 					print("comparing " + lastProcessed.toLowerCase() + " to " + rule.getSearchName().toLowerCase());
 					if (found == true) {
 						ruleListAfter.add(rule);
 					} else if (rule.getSearchName().toLowerCase().contentEquals(lastProcessed.toLowerCase()) ) {
 						found = true;
 						print("found: " + lastProcessed);
 						ruleListBefore.add(rule);
 					} else {
 						ruleListBefore.add(rule);
 					}
 				}
 				ruleList = new ArrayList<PageRule>();
 				ruleList.addAll(ruleListAfter);
 				ruleList.addAll(ruleListBefore);
 			}
 
 			NewPageFetcher npp = new NewPageFetcher(wiki, fetcher, editor);
 
 			for (PageRule rule : ruleList) {
 				print("rule: " + rule.getSearchName());
 			}
 
 			for (PageRule rule : ruleList) {
 				BotFlag.check(wiki);
 
 				String searchName = rule.getSearchName();
 
 				print( "processing rule " + searchName + ", current time: "
 					+ WikiHelpers.calendarToTimestamp( new GregorianCalendar( TimeZone.getTimeZone( "America/Los_Angeles" ) ) ) );
 				long startClock = System.currentTimeMillis();
 
 				// store it before we run. That way we'll begin at n+1 even if this one frequently fails.
 				PersistentKeystore.put(PersistentKeystore.DEFAULT_KEY, PersistentKeystore.LAST_PROCESSED,
 						rule.getSearchName(), true);
 
 				String startTime = getStartTime(searchName);
 				if (!isDeltaGreaterThanOneDay(WikiHelpers.timestampToCalendar(startTime), new GregorianCalendar())) {
 					print("rule " + rule.getSearchName() + " has been run recently, skipping.");
 					continue;
 				}
 
 				String endTime = npp.runFetcher(startTime, rule);
 				storeStartTime(searchName, endTime);
 
 				long endClock = System.currentTimeMillis();
 				print("done processing rule " + searchName + ", time: "
 						+ deltaMillisecondsToString(endClock - startClock));
 
 			}
 
 		} finally {
 			if (ac != null) {
 				ac.shutdown();
 			}
 		}
 	}
 
 	public static boolean isDeltaGreaterThanOneDay( Calendar obj1, Calendar obj2 )
 	{
 		long delta = Math.abs( obj1.getTimeInMillis() - obj2.getTimeInMillis() );
 		if ( delta > DAY_IN_MILLISECONDS ) {
 			return true;
 		}
 		return false;
 	}
 
 	public static String deltaMillisecondsToString( long delta )
 	{
 		long deltaSeconds = ( delta / 1000 ) % 60;
		long deltaMinutes = ( deltaSeconds / 60 ) % 60;
		long deltaHours = ( deltaMinutes / 60 ) % 60;
 
 		return String.format("%d hours, %d minutes, %d seconds", deltaHours, deltaMinutes, deltaSeconds);
 	}
 
 	public static class SortRulesByRuleNameAlpha implements Comparator<PageRule> {
 		public SortRulesByRuleNameAlpha()
 		{
 		};
 
 		@Override
 		public int compare( PageRule arg0, PageRule arg1 )
 		{
 			return arg0.getSearchName().compareTo( arg1.getSearchName() );
 		}
 	}
 
 	private static void print( String s )
 	{
 		logger.log( Level.INFO, s );
 	}
 
 	public static String getStartTime( String searchName ) throws FileNotFoundException, IllegalArgumentException, IOException
 	{
 		String startTime = PersistentKeystore.get( searchName, "lastRunTime" );
 
 		if (startTime == null || startTime.isEmpty()) {
 			startTime = getDefaultStartTime();
 		} else {
 			Calendar start = WikiHelpers.timestampToCalendar(startTime);
 			start.add(Calendar.DAY_OF_MONTH, PREPEND_SEARCH_DAYS);
 			startTime = WikiHelpers.calendarToTimestamp(start);
 		}
 
 		return startTime;
 	}
 
 	public static void storeStartTime( String searchName, String lastStamp )
 	{
 		PersistentKeystore.put( searchName, "lastRunTime", lastStamp, true );
 	}
 
 	public static String getDefaultStartTime() throws FileNotFoundException, IllegalArgumentException, IOException
 	{
 		String startTime = PersistentKeystore.get( "default", "lastRunTime" );
 
 		Calendar start = null;
 		if (startTime == null || startTime.isEmpty()) {
 			// if we didn't have the key in our keystore, use a default of today minus our padding.
 			start = new GregorianCalendar();
 		} else {
 			start = WikiHelpers.timestampToCalendar(startTime);
 		}
 
 		// pad back the start time.
 		start.add(Calendar.DAY_OF_MONTH, PREPEND_SEARCH_DAYS);
 
 		// given our Calendar object, get a String (again).
 		startTime = WikiHelpers.calendarToTimestamp(start);
 
 		return startTime;
 	}
 
 }
