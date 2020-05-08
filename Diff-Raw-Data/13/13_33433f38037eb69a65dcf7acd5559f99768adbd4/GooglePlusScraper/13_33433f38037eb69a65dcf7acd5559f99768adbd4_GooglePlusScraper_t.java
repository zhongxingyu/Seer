 package com.sk.impl;
 
 import java.util.regex.Pattern;
 
 import com.sk.util.SiteScraperInfo;
 import com.sk.util.parse.scrape.BasicGrabber;
 import com.sk.util.parse.scrape.Grabber;
 import com.sk.util.parse.scrape.GrabberSiteScraper;
 import com.sk.util.parse.scrape.RegexGrabber;
 
 @SiteScraperInfo(siteBase = "https://plus.google.com", siteId = "g+")
 public class GooglePlusScraper extends GrabberSiteScraper {
 
 	private static final Grabber[] grabbers = { new BasicGrabber("div[guidedhelpid=profile_name]", "name"),
 			new BasicGrabber("span:contains(Works) + span.FG2oob", "job-title"),
 			new BasicGrabber("span:contains(Attended) + span.FG2oob", "education"),
 			new BasicGrabber("span:contains(Lives) + span.FG2oob", "location"),
			new RegexGrabber(".g-x-Aa.ud-Aa", "token", Pattern.compile("(?<r>[0-9]+)/"), "uid") };
 
 	public GooglePlusScraper() {
 		super(grabbers);
 	}
 
 }
