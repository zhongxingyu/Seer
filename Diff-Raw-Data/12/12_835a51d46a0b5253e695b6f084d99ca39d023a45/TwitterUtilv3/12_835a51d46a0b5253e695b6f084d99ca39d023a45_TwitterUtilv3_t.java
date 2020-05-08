 /*
  * Copyright (C) 2011-2013 Kuropen.
  * 
  * This file is part of the Electricity Warning Crawler, Version 3.
  * 
  * The Electricity Warning Crawler is free software:
  * you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * The Electricity Warning Crawler is distributed in the hope that
  * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with The Electricity Warning Crawler.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.kuropen.elecwarnv3.util;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 
 public class TwitterUtilv3 {
 
 	private Twitter twitter;
 	
 	/**
 	 * テストするときはtrueにする。これにより「試験」とつけてツイートできる。
 	 */
 	public static boolean isTestMode = false;
 	
 	public TwitterUtilv3 (String cKey, String cSecret, String uKey, String uSecret) {
 		twitter = new TwitterFactory().getInstance();
 		twitter.setOAuthConsumer(cKey, cSecret);
 		AccessToken aToken = new AccessToken(uKey, uSecret);
 		twitter.setOAuthAccessToken(aToken);
 	}
 	
 	/**
 	 * ツイートする
 	 */
 	public void sendTweet(String content) {
		sendTweet(content, false);
	}
 	public void sendTweet(String content, boolean testflag) {
 		String status;
 		if(isTestMode || testflag) {
 			status = "【試験】" + content;
 		}else{
 			status = content;
 		}
 		try {
 			twitter.updateStatus(status);
 		} catch (TwitterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 }
