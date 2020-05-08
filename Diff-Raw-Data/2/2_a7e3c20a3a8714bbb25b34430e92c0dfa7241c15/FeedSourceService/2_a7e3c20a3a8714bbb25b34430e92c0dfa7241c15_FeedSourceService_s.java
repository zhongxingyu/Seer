 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  ******************************************************************************/
 package org.sociotech.communitymashup.source.feed;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.Source;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.source.feed.properties.FeedProperties;
 import org.sociotech.communitymashup.source.feed.transformation.FeedTransformation;
 import org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl;
 
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 
 /**
  * @author Peter Lachenmaier, Martin Burkhard
  * 
  * Main class of the Feed source service.
  */
 public class FeedSourceService extends SourceServiceFacadeImpl {
 
 	private FeedTransformation transformation;
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#createDefaultConfiguration()
 	 */
 	@Override
 	protected void createDefaultConfiguration() {
 
 		super.createDefaultConfiguration();
 
 		// default feed
 		source.addProperty(FeedProperties.FEED_URL_PROPERTY, FeedProperties.FEED_URL_DEFAULT);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#initialize(org.sociotech.communitymashup.application.Source)
 	 */
 	@Override
 	public boolean initialize(Source configuration) {
 
 		boolean initialized = super.initialize(configuration);
 
 		if(initialized)
 		{
 			String feedUrl = source.getPropertyValue(FeedProperties.FEED_URL_PROPERTY);
 			
 			// check url property
			initialized &= (feedUrl != null & !feedUrl.isEmpty());
 		}
 		
 		if(!initialized)
 		{
 			log("No feed url set in the configuration. Use property " + FeedProperties.FEED_URL_PROPERTY + "to set it.", LogService.LOG_WARNING);
 		}
 		else
 		{
 			// create new feed transformation instance
 			transformation = new FeedTransformation(this);
 	
 			// set transformation properties
 			transformation.setFirstCategoryIsCategory(source.isPropertyTrue(FeedProperties.SET_FIRST_CATEGORY_PROPERTY));
 			transformation.setAddOnlyFirstImage(source.isPropertyTrue(FeedProperties.ADD_ONLY_FIRST_IMAGE_PROPERTY));
 		}
 		
 		this.setInitialized(initialized);
 		
 		return initialized;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#fillDataSet(org.sociotech.communitymashup.data.DataSet)
 	 */
 	@Override
 	public void fillDataSet(DataSet dataSet) {
 
 		super.fillDataSet(dataSet);
 		
 		// get url from property and load the feed
 		getAndAddFeed(source.getPropertyValue(FeedProperties.FEED_URL_PROPERTY), source.getPropertyValue(FeedProperties.FEED_ENTRY_METATAG_PROPERTY));
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#updateDataSet()
 	 */
 	@Override
 	public void updateDataSet() {
 		
 		super.updateDataSet();
 		
 		// get url from property and load the feed
 		getAndAddFeed(source.getPropertyValue(FeedProperties.FEED_URL_PROPERTY), source.getPropertyValue(FeedProperties.FEED_ENTRY_METATAG_PROPERTY));
 	}
 
 
 	/**
 	 * Parses RSS or ATOM feed for a given feed url.
 	 * 
 	 * @param url URL of the feed
 	 * @param dataSet Data set to fill with feed elements
 	 */
 	public void getAndAddFeed(String url, String contentMetaTag) {
 
 		log("Loading Feed from: " + url, LogService.LOG_DEBUG);
 
 		SyndFeed feed;
 
 		try {
 			SyndFeedInput input = new SyndFeedInput();
 			feed = input.build(new XmlReader(new URL(url)));
 		}
 		catch (IOException e) {
 			log("IOException while accessing feed. Error:" + e.getMessage(), LogService.LOG_ERROR);
 			return;
 		} catch (FeedException e) {
 			log("FeedException while parsing feed. Error:" + e.getMessage(), LogService.LOG_ERROR);
 			return;
 		}
 		catch (Exception e) {
 			log("Error (" + e.getMessage() + ") occured trying to create the input for feed: " + url, LogService.LOG_ERROR);
 			return;
 		}
 
 		if(feed == null || feed.getEntries().isEmpty())
 		{
 			// nothing to do
 			return;
 		}
 
 		transformation.transformFeed(feed, contentMetaTag);
 	}
 }
