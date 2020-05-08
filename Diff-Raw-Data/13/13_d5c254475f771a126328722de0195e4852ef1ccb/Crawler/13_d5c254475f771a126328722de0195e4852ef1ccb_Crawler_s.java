 package com.xepps.tinyBot;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.xepps.tinyBot.util.XParser;
 
 public class Crawler
 {
 	private static String ROOTURL;
 	private static String SITE;
 	
 	ArrayList<String> queue;
 	HashMap<String, Integer> pages;
 	private int queueLocation;
 	
 	public Crawler(String input)
 	{
 	    ROOTURL = input;
 	    SITE = input;
 	    
 	    if(ROOTURL.startsWith("http://"))
 	        ROOTURL = input.substring(7);
 	    
 	    if(ROOTURL.startsWith("www."))
 	        ROOTURL = ROOTURL.substring(4);
 	    
 	    if(SITE.endsWith("/"))
 	        SITE = SITE.substring(0, (SITE.length() - 1));
 	    
 		queueLocation = 0;
 		queue = new ArrayList<String>();
 		pages = new HashMap<String, Integer>();
 		
 		queue.add(input);
 		pages.put(input, 1);
 	}
 	
 	private void retrievePage()
 	{		
 		String nextPage = this.queue.get(queueLocation);	
 		
 		if(!nextPage.contains(ROOTURL))
             return;
 		
 		System.out.println("On Page: " + nextPage);
 		
 		Document doc;
 		try
 		{
 		    doc = Jsoup.parse(new URL(nextPage).openStream(), "UTF-8", nextPage);
 		    Elements links = doc.getElementsByTag("a");
 		
 		    for(Element link : links)
 		        addToQueue(link);
 		}
 		catch(Exception e)
 		{
 		    System.out.println("\t- Was not a valid web page");
 		}
 	}
 
 	private void addToQueue(Element link)
 	{
 	    String url = link.attr("href");
         
         if(url.startsWith("/") || url.startsWith("#") || url.startsWith("?"))
             url = SITE + url;
         
         if(url.contains("?"))
            url = url.substring(0, (url.indexOf("?")-1));
         
         if(url.contains("#"))
            url = url.substring(0, (url.indexOf("#")-1));
         
         if(url.endsWith("/"))
             url = url.substring(0, (url.length() - 1));
         
 		if(!pages.containsKey(url))
 		{		    
 			pages.put(url, 1);
 			queue.add(url);
 		}
 		else
 		{
 			Integer hits = pages.get(url);
 			pages.remove(url);
 			pages.put(url, (hits += 1));
 		}
 	}
 	
 	public void crawl() throws InterruptedException
 	{
 		do
 		{
 			this.retrievePage();
 			queueLocation++;
 			Thread.sleep(1 * 1000);
 		}
 		while(queue.size() > queueLocation);
 	}
 	
 	public ArrayList<String> getCrawledPages()
 	{
 		return this.queue;
 	}
 	
 	public HashMap<String, Integer> getHits()
 	{
 		return this.pages;
 	}
 	
 	private static final String INPUT = "input";
     private static final String OUTPUT = "output";
 	
 	@SuppressWarnings("static-access")
     public static void main(String[] args) throws IOException, InterruptedException
 	{
 	    Options options = new Options();
 
         options.addOption(OptionBuilder.withArgName("path").hasArg()
                 .withDescription("input path").create(INPUT));
         options.addOption(OptionBuilder.withArgName("path").hasArg()
                 .withDescription("output path").create(OUTPUT));
 
         CommandLine cmdline = null;
         CommandLineParser parser = new XParser(true);
 
         try
         {
             cmdline = parser.parse(options, args);
         }
         catch (ParseException exp)
         {
             System.err.println("Error parsing command line: "
                     + exp.getMessage());
             return;
         }
 
         if (!cmdline.hasOption(INPUT))
         {
             System.err.println("A website must be specified using:");
             System.err.println("\t -input http://yoursitehere.com");
             return;
         }
 
         String inputPath = cmdline.getOptionValue(INPUT);
         String outputPath = cmdline.getOptionValue(OUTPUT);
         
         if(outputPath.isEmpty())
             outputPath = "output";
         
         if(!outputPath.endsWith(".csv"))
             outputPath += ".csv";
         
         if(!inputPath.startsWith("http://"))
             inputPath = "http://" + inputPath;
 	    
 		Crawler crawler = new Crawler(inputPath);
 		crawler.crawl();
 		
 		HashMap<String, Integer> pages = crawler.getHits();
 		ArrayList<String> urls = crawler.getCrawledPages();
 		
 		PrintWriter out = new PrintWriter(outputPath, "UTF-8");
 		out.println("PAGE , \t HITS");
 		for(String url : urls)
 			out.println(url + " , \t " + pages.get(url));
 		
 		out.close();
 
 	}
 }
