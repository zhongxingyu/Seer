 /*
  * Copyright 2013 Midhun Harikumar
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ae.feeds.reader.tests;
 
 import java.io.IOException;
 import java.util.Map;
 
 import com.ae.feeds.reader.exceptions.AppException;
 import com.ae.feeds.reader.model.Feed;
 import com.ae.feeds.reader.read.IFeedParser;
 import com.ae.feeds.reader.read.ParserReaderFactory;
 import com.ae.feeds.reader.utils.FeedUtils;
 import com.ae.feeds.reader.utils.PropertyReader;
 import com.ae.feeds.reader.utils.AppConstants;
 
 /**
  * Test Drive the feed image saving application
  * 
  * @author Midhun Harikumar
  * 
  */
 public class ReaderTest {
 
 	public static void main(String[] args) throws IOException {
 		System.out.println(AppConstants.APP_TITLE + AppConstants.APP_VERSION);
 
 		// Read the settings
 		int type = PropertyReader.readIntProperty(AppConstants.SETTING_FEED_SOURCE);
 		boolean saveImage = PropertyReader.readBooleanProperty(AppConstants.SETTING_SAVE_IMAGE);
 		String localImagePath = PropertyReader.readProperty(AppConstants.SETTING_SAVE_LOCATION);
 		boolean overwriteImage = PropertyReader.readBooleanProperty(AppConstants.SETTING_OVERWRITE);
 
 		// Log the current save image settings
 		if (saveImage) {
 			System.out.println(" > Saving images to " + localImagePath + " \n > Overwrite = " + overwriteImage);
 		} else {
 			System.out.println(" > Not Saving images");
 		}
 
 		try {
 			// Get a parser instance for parsing the data
 			IFeedParser feedParser = ParserReaderFactory.getParser(type);
 			System.out.println(" > Location : " + feedParser.getFeedSource());
 			Feed feed = feedParser.readFeed();
 
 			if (feed != null && feed.getEntries() != null && feed.getEntries().size() > 0) {
 				// Bing sends same image for different countries with different
 				// country names for some reason, so this needs to be removed
 				Map<String, String> uniqueUrlsMap = FeedUtils.removeDuplicateFeeds(feed.getEntries());
 
 				System.out.println("Found " + uniqueUrlsMap.size() + " unique image(s) for today.");
 
 				// Loop through this unique urls map
 				int i = 1;
 				long fileSize  = 0;
 				long totalSize = 0;
 				String fileName = null;
 				String imgPath  = null;
 				for (String imgName : uniqueUrlsMap.keySet()) {
 					System.out.println(" [" + i + "] " + imgName);
 					if (saveImage) {
 						imgPath = uniqueUrlsMap.get(imgName);
						fileName = localImagePath + imgName + "." + FeedUtils.getFileExtension(imgPath);
 						fileSize = FeedUtils.saveImageAsFile(imgPath, fileName, overwriteImage);
 						if(fileSize > 0){
 							System.out.println(" > " + FeedUtils.readableFileSize(fileSize));
 							totalSize += fileSize;
 						}
 					}
 					i++;
 				}
 				// Print out a pretty total size downloaded as summary
 				if(totalSize > 0){
 					System.out.println("--------------------------------------------------");
 					System.out.println(" > Total " + FeedUtils.readableFileSize(totalSize));
 				}
 			} else {
 				System.out.println("Unable to fetch feed or image details. Please try again later.");
 			}
 		} catch (AppException e) {
 			System.err.println("An Exception has occured :  " + e.getMessage());
 		}
 	}
 }
