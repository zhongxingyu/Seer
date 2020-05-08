 package org.pdc.nca;
 
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.InputSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Main executable 
  * @author Uday
  *
  */
 public class FeedReader {
 	
     
 	private static Logger logger = LoggerFactory.getLogger(FeedReader.class);
 	
 	String feedURL = "http://alerts.weather.gov/cap/us.php?x=0";
 	String candidateHazardsDirectory = "candidateHazards";
 	
 	//time to IDLE in cache (seconds) - a week by default
 	int tti = 60*60*24*7;  
     //time to LIVE in seconds - a month by default
 	int ttl = 60*60*24*30;
 	
 	// by default the output is the more readable java properties file type 
 	boolean xmlOutput=false; 
 
 	//database cache usage - default is no
 	boolean useDiskCache = true; 
 
 	//database cache usage - default is no
 	boolean useDatabaseCache = false; 
 	/**
 	 * various constructors
 	 */
 	public FeedReader()
 	{
 	}
 	public FeedReader (String feedLoc)
 	{
 	   feedURL = StringUtils.trim(feedLoc);
 	}
 	public FeedReader (boolean xmlFlag)
 	{
 		xmlOutput = xmlFlag;
 	}
 	public FeedReader (String feedLoc, String hazardsDir)
 	{
 		feedURL = feedLoc;
 		candidateHazardsDirectory = hazardsDir;
 	}
 	public FeedReader (String feedLoc, String hazardsDir, int a_tti, int a_ttl)
 	{
 		feedURL = feedLoc;
 		candidateHazardsDirectory = hazardsDir;
 		tti = a_tti; ttl = a_ttl;
 	}
 	public FeedReader (String feedLoc, boolean xmlFlag)
 	{
 		feedURL = feedLoc;
 		xmlOutput = xmlFlag;
 	}
 	
 	
 	/**
 	 * loadConfiguration
 	 * @param  file name
 	 * @return true if able to set feed reader from properties, false if failed
 	 */
 	public boolean loadConfiguration(String fileName)
 	{
 		PropertiesConfiguration config = null;
 		try
 		{
 		   config = new PropertiesConfiguration(fileName); 
 		   feedURL                   = config.getString("feedURL", feedURL);
 		   candidateHazardsDirectory = config.getString("candidateHazardsDirectory", candidateHazardsDirectory);
 		   tti                       = config.getInt("tti", 604800);
 		   ttl                       = config.getInt("ttl", 2592000);
 		   xmlOutput                 = config.getBoolean("xmlOutput", false);
 		   
 		   useDiskCache              = config.getBoolean("diskCache", true);
 		   useDatabaseCache          = config.getBoolean("dbCache", false);
 		   
		   logger.info("Loaded reader properties from " + config.getFile().getAbsolutePath());
 		   return true;
 	    }
 		catch (org.apache.commons.configuration.ConfigurationException e)
 		{
 		  // do nothing just log and use defaults 
 		  logger.warn("Unable to load the configuration (reader.properties) file " + e.toString());
 		  return false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param directory name 
 	 * @return true if successfull 
 	 */
 	public boolean verifyCandidateHazardsDirectory()
 	{
 		File dir = new File(System.getProperty("user.dir"), this.candidateHazardsDirectory);
         if (!dir.exists()) dir.mkdirs();
         if (dir.exists() && dir.isDirectory())
         {
         	logger.info("candidate hazards directory exists " + dir.getAbsolutePath());
         	return true;
         }
         else
         {
         	logger.error("candidate hazards directory does not exist, could not create - UNKNOWN reason;  Exiting...");
         	return false; 
         }
 	}
 	
 	
 	/**
 	 * Parse the input source, retrurn Syndicated Entries  
 	 * @return SyndEntries from NWS CAP ATOM source uri 
 	 */
 	@SuppressWarnings("unchecked")
 	public List <SyndEntry> getEntries()
 	{
 		// Now try parsing the feed URL 
 		SyndFeed feed = null;
 		logger.info("ENTER parse step: building ROME 1.0 Syndicated Feed Object (SyndFeed) from Input Source " + feedURL);
 		try
 		{
 			feed = new SyndFeedInput().build(new InputSource(feedURL));
 		}
 		catch (Exception e)
 		{
 			logger.error("Unable to parse feed URL " + feedURL );
 			logger.error(e.toString());
 		}
         return feed.getEntries();
 	}
 	
 	/**
 	 *  candidate hazards 
 	 *  @return Entries - 
 	 */
 	public List <Entry> getCandidateEntries (List <SyndEntry> entries)
 	{
 		//////////////////////////////
         // to be used to hold the new/updated feed entries for output into candidate hazards directory  
         List<Entry> candidates = new ArrayList<Entry>();
 
         ////////////////////////////////////////////
         // Fire up the cache 
         DiskCache cache = new DiskCache(tti, ttl);
         int beginCacheSize = cache.getSize();
         logger.info("Size of cache BEGIN " + cache.getSize());
         for (SyndEntry entry : entries)
 		{
         	Entry e = new Entry(entry);
 			// put will only work (return true) if cache accepts the entry (i.e. it is new or updated) 
 			if (cache.put(e))
 			{
 				candidates.add(e);
 				logger.info("Added to Disk-Cache " + entry.getTitle());
 			}
 			else
 			{
 				logger.info(entry.getTitle() + " already in Disk-Cache"); 
 			}
         }
         
         
         if (candidates.size() != ( cache.getSize() - beginCacheSize ) )
         {
         	logger.warn("Cache should have increased by " + candidates.size() + ", but actual increase " + (cache.getSize() - beginCacheSize) );
         }
         else
         {
 		  
 		    logger.info(candidates.size() + " candidated hazards added to cache.  Will write to disk now...");
         }
 
         return candidates;
 	}
 	
 	/**
 	 *  candidate hazards 
 	 *  @return Entries - 
 	 */
 	public List <Entry> getCandidateEntriesPerDatabase (List <SyndEntry> entries)
 	{
 		//////////////////////////////
         // to be used to hold the new/updated feed entries for output into candidate hazards directory  
         List<Entry> candidates = new ArrayList<Entry>();
 
         ////////////////////////////////////////////
         // Fire up the cache 
         DatabaseCache cache = new DatabaseCache();
         int beginCacheSize = cache.getSize();
         logger.info("Size of cache BEGIN " + cache.getSize());
         for (SyndEntry entry : entries)
 		{
         	Entry e = new Entry(entry);
 			// put will only work (return true) if cache accepts the entry (i.e. it is new or updated) 
 			if (cache.put(e))
 			{
 				candidates.add(e);
 				logger.info("Added to Database-Cache " + entry.getTitle());
 			}
 			else
 			{
 				logger.info(entry.getTitle() + " already in Database-Cache"); 
 			}
         }
         
         
         if (candidates.size() != ( cache.getSize() - beginCacheSize ) )
         {
         	logger.warn("Cache should have increased by " + candidates.size() + ", but actual increase " + (cache.getSize() - beginCacheSize) );
         }
         else
         {
 		  
 		    logger.info(candidates.size() + " candidated hazards added to cache.  Will write to disk now...");
         }
 
         return candidates;
 	}
 
 	
 	public boolean writeCandidates(List <Entry> candidates)
 	{
 		for (Entry candidate : candidates)
 		{
 		  
 	      //////////
 		  // for xml files use the xml extension
 		  File candidateFile = new File(this.candidateHazardsDirectory, candidate.getFilename()); 
 		  if(xmlOutput) candidateFile = new File(this.candidateHazardsDirectory, candidate.getFilename("xml"));
 		  
 		  try
 		  {
 			 candidateFile.createNewFile();
 		  }
 		  catch (Exception e)
 		  {
 			  logger.error("Was unable to create candidate Hazard file " + candidateFile );
 			  logger.error("Title " + candidate.getSyndEntry().getTitle());
 			  return false; 
 		  }
 		  
 		  if (candidateFile.exists() && candidateFile.canWrite())
 		  {
 			try
 			{
   		      FileWriter fwr = new FileWriter(candidateFile, false);  // append = false, meaning it will over-write (update, as intended)
   		      
   		      // choice of XML or "properties" type of output
   		      if(xmlOutput) fwr.write(candidate.getXML());
   		      else candidate.write(fwr);
   		      
 	          fwr.close();
 		    }
 		    catch (Exception e)
 		    {
 			  logger.error("FATAL ERROR writing (the feed entry into) file " + candidateFile );
 			  logger.error("Title " + candidate.getSyndEntry().getTitle());
 			  logger.error(e.toString());
 			  return false; 
 		    }
 		  }
 		  else
 		  {
 			  logger.error("Was unable to create or, if created, was unable able to write candidate Hazard file " + candidateFile );
 			  logger.error("Title " + candidate.getSyndEntry().getTitle());
 			  return false; 
 		  }
 		}
 
 		return true; 
 
 	}
 	
 	
 	public static void main(String[] args) 
 	{
 		// measure time spent 
 		long startMillis = 0L;
 		long endMillis = 0L;
 		long duration = 0L;
 		
 		
 		startMillis = System.currentTimeMillis();
 		logger.info("NWS Feed Reader was STARTED at " + new Date(startMillis));
 		
 		FeedReader reader = new FeedReader();
 		
 		
 		///////////////////////////////////////////////////////////////////////////////
 		// try over-ride of default - properties file: NORMAL PRODUCTION 
         reader.loadConfiguration("reader.properties");
 		
         
         
 		//////////////////////////////////////////////////////////////////////////////////
 		// command line over-rides both default and config: FOR TESTING ONLY
 		if (args.length == 5)
 		{
            reader.setFeedURL(args[0]);
            reader.setCandidateHazardsDirectory(args[1]);
            reader.setTti(NumberUtils.toInt(args[2], 604800));
            reader.setTtl(NumberUtils.toInt(args[3], 2592000));
            reader.setXmlOutput(BooleanUtils.toBoolean(args[4]));
 		}
 		else if (args.length == 4)
 		{
            reader.setFeedURL(args[0]);
            reader.setCandidateHazardsDirectory(args[1]);
            reader.setTti(NumberUtils.toInt(args[2], 604800));
            reader.setTtl(NumberUtils.toInt(args[3], 2592000));
 		}
 		else if (args.length == 3)
 		{
            reader.setFeedURL(args[0]);
            reader.setCandidateHazardsDirectory(args[1]);
            reader.setTti(NumberUtils.toInt(args[2], 604800));
 		}
 		else if (args.length == 2)
 		{
            reader.setFeedURL(args[0]);
            reader.setCandidateHazardsDirectory(args[1]);
 		}
 		else if (args.length == 1)
 		{
            reader.setFeedURL(args[0]);
            logger.info("feed URL set from COMMAND line " + args[0]);
 		}
 
 		/////////////////////////////////////////////////////////////////////
 		// echo inputs/defaults
 		logger.info("feed URL = " + reader.getFeedURL());
 		logger.info("candidateHazardsDirectory = " + reader.getCandidateHazardsDirectory());
 		logger.info("Max Time to Idle (TTI) in cache, seconds " + reader.getTti()); 
 		logger.info("Max Time to Live (TTI) in cache, seconds " + reader.getTtl());
 		if (reader.isXmlOutput()) logger.info("Entries will be printed in XML format");
 		else logger.info("Entries will be printed in .properties format");
 		if (reader.isUseDiskCache()) logger.info("Disk Cache will be used");
 		else logger.info("Disk Caching DISABLED by request.  May be honored if a) database caching is enabled and b) it WORKS");
 		if (reader.isUseDatabaseCache()) logger.info("Secondary DataBase Cache ENABLED.");
 		else logger.info("No Database Caching.");
 		
 		
 		///////////////////////////////////////////////////////////////////
 		// verify if candidate hazards directory is there, else make it
 		// Abort, if unable to write to candidate hazards directory 
         if (!reader.verifyCandidateHazardsDirectory()) System.exit(1);
 		
 
         //////////////////////////////////////////////////////////////////
         // Parse the feedURL
         List<SyndEntry> entries = reader.getEntries();
         if (entries.isEmpty()) 
         {
         	logger.error("ATOM Feed Parser Failed, Exiting...");
         	System.exit(1);
         }
         else
         {
             logger.info("Parsed " + entries.size() + " feed entries");
         }
         
         ////////////////////////////////////////////////////////////////////////
         // new/updated feed entries for output into candidate hazards directory  
         List<Entry> candidates = null;
         
         if (reader.isUseDatabaseCache())
         {
            try
            {
              candidates = reader.getCandidateEntriesPerDatabase(entries);
              logger.info("DB Cache identified " + candidates.size() + " NEW candidate Hazards");
            }
            catch (Exception e)
            {
         	   logger.warn("Database Caching Error " + e.toString());
         	   logger.info("Using Disk Cache");
         	   candidates = reader.getCandidateEntries(entries);
            }
             
         }
         else
         {
         	// disk cache is default 
         	candidates = reader.getCandidateEntries(entries);
         	logger.info("DISK Cache identified " + candidates.size() + " NEW candidate Hazards");
         	// turn off so that next if does not fire and repeat...
         	reader.setUseDiskCache(false);
         }
 
         ////////////////////////////////////////////////////////////
         // nominal usage of database cache for sanity check only - 
         // disk cache will govern membership in candidate hazards 
         if(reader.isUseDiskCache())
         {
           candidates = reader.getCandidateEntries(entries);
       	  logger.info("DISK Cache OVERRIDES DB Cache identifying, hopefully ALSO, " + candidates.size() + " NEW candidate Hazards");
         }
         
         
         // this should ideally be testable - perhaps make cache a member variable 
         if (candidates.isEmpty())
         {
         	logger.info("No NEW or UPDATES Candidates found feed.  Nothing to do...Exiting");
         	System.exit(0);
         }
         
         
         if (reader.writeCandidates(candidates))
         {
     		endMillis = System.currentTimeMillis(); duration = endMillis - startMillis;
         	logger.info("END of Run (SUCCESS) " + new Date(endMillis) + ".  " + candidates.size() + " new candidate hazards files were printed.  (" + duration + " milliseconds)");
         }
         else
         {
     		endMillis = System.currentTimeMillis(); duration = endMillis - startMillis;
         	logger.error("FAILED TO WRITE " + candidates.size() + " CANDIDATES " + new Date(endMillis) + ".    (" + duration + " milliseconds)");
         	
         }
 		
 
   } // end main
 
 	
 	
 	// getters, settrs
 	
 	public String getFeedURL() {
 		return feedURL;
 	}
 	public void setFeedURL(String feedURL) {
 		this.feedURL = feedURL;
 	}
 	public String getCandidateHazardsDirectory() {
 		return candidateHazardsDirectory;
 	}
 	public void setCandidateHazardsDirectory(String candidateHazardsDirectory) {
 		this.candidateHazardsDirectory = candidateHazardsDirectory;
 	}
 	public int getTti() {
 		return tti;
 	}
 	public void setTti(int tti) {
 		this.tti = tti;
 	}
 	public int getTtl() {
 		return ttl;
 	}
 	public void setTtl(int ttl) {
 		this.ttl = ttl;
 	}
 	public boolean isXmlOutput() {
 		return xmlOutput;
 	}
 	public void setXmlOutput(boolean xmlOutput) {
 		this.xmlOutput = xmlOutput;
 	}
 	public boolean isUseDatabaseCache() {
 		return useDatabaseCache;
 	}
 	public void setUseDatabaseCache(boolean useDatabaseCache) {
 		this.useDatabaseCache = useDatabaseCache;
 	}
 	public boolean isUseDiskCache() {
 		return useDiskCache;
 	}
 	public void setUseDiskCache(boolean useDiskCache) {
 		this.useDiskCache = useDiskCache;
 	}
 
 } // end class definition
