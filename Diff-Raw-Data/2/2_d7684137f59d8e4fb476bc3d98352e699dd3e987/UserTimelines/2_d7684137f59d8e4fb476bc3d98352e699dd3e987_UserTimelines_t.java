 package matz;
 
 import java.io.*;
 
 import twitter4j.Paging;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.TwitterException;
 import twitter4j.json.DataObjectFactory;
 
 public class UserTimelines extends TwitterRest {
 	private static boolean testPaging = false;
 	public final static String testPagingOption = "-t";
 	
 	private static int timeLineAuthHead = 1;
 	private static int timeLineAuthTail;
 	private static int fullSizeTimeLine = 200;
 	private static int testSizeTimeLine = 5;
 	
 	private static void setTimeLineAuthTail() {
 		timeLineAuthTail = OAuthList.length - 1;
 	}
 
 	private static Paging setPaging(File thisUsersCurr) throws Exception {
 		Paging paging = new Paging();
 		paging.setCount(testPaging? testSizeTimeLine : fullSizeTimeLine);
 		if(thisUsersCurr.exists()) {
 			BufferedReader cbr = new BufferedReader(new InputStreamReader(new FileInputStream(thisUsersCurr)));
 			String curr = cbr.readLine();
 			paging.setSinceId(Long.parseLong(curr));
 			cbr.close();
 		}
 		return paging;
 	}
 	
 	public static boolean findAvailableAuth()  {
 		/*if (authAvailabilityCheck()) return true;
 		
 		int groundId = currentAuthId;
 		int nextId = (currentAuthId == timeLineAuthTail)? timeLineAuthHead : currentAuthId + 1;
 		//twitter = buildTwitterIns((groundId  == timeLineAuthTail) ? timeLineAuthHead : groundId + 1);
 		while(!authAvailabilityCheck(nextId)) {
 			nextId = (nextId == timeLineAuthTail)? timeLineAuthHead : nextId + 1;
 			if (nextId == groundId) return false;
 		}*/
 		if (!authAvailabilityCheck()) {
 			int nextId = (currentAuthId == timeLineAuthTail)? timeLineAuthHead : currentAuthId + 1;
 			twitter = buildTwitterIns(nextId);
 			saveAuthInfo();
 			if (!authAvailabilityCheck()) return false;
 		}
 		return true;
 	}
 
 	public static void main(String[] args) {
		OAuthList = loadAuthInfo();
 		setTimeLineAuthTail();
 		
 		for (String arg : args) {
 			if (arg == testPagingOption) testPaging = true;
 		}
 		
 		try { //userList loading
 			BufferedReader ulbr = new BufferedReader(new InputStreamReader(new FileInputStream(userListFile)));
 			String userid = new String();
 			while((userid = ulbr.readLine()) != null) {
 				if (!userList.contains(userid)) userList.add(userid);
 			}
 			ulbr.close();
 			
 			//userDir
 			if(!userDir.isDirectory()) userDir.mkdir();
 		} catch (FileNotFoundException e) {
 			//ignore
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//Twitter twitter = buildTwitterIns(timeLineAuthHead); //matz_0001, init
 		twitter = buildTwitterIns(timeLineAuthHead); //matz_0001, init
 		
 		try {	//for every recorded users, get their recent tweets
 			for (String id : userList) {
 				while (!findAvailableAuth()) {
 					sleepUntilReset();
 				}
 				
 				File thisUsersFile = new File(userDir,id + ".txt");
 				File thisUsersCurr = new File(userDir,id + ".curr.txt");
 				long userIdLong = Long.parseLong(id);
 				long maxid = 1;
 				
 				Paging paging = setPaging(thisUsersCurr);
 
 				
 				ResponseList<Status> timeLine = null;
 				try {
 					timeLine = twitter.getUserTimeline(userIdLong, paging);
 				} catch (TwitterException twe) {
 					/* for private users, accessing their timeline will return you 401: Not Authorized error.
 					 * capture 401 and just continue it.
 					 * there are some other statuses which could halt the script, you should handle them.
 					 */
 					int statusCode = twe.getStatusCode();
 					if (statusCode == STATUS_UNAUTHORIZED || statusCode == STATUS_NOT_FOUND) {
 						callCount(currentAuthId);
 						continue;
 					} else if (statusCode == STATUS_BAD_GATEWAY || statusCode == STATUS_SERVICE_UNAVAILABLE) {
 						sleepUntilReset(authLimitWindow);
 					} else if (statusCode == STATUS_TOO_MANY_REQUESTS || statusCode == STATUS_ENHANCE_YOUR_CALM) {
 						int secondsUntilReset = twe.getRateLimitStatus().getSecondsUntilReset(); // getSecondsUntilReset returns seconds until limit reset in Integer
 						long retryAfter = (long)(secondsUntilReset * 1000);
 						if (secondsUntilReset <= 0) retryAfter = authLimitWindow;
 						retryAfter += authRetryMargin;
 						sleepUntilReset(retryAfter);
 					} else {
 						twe.printStackTrace();
 						throw twe;
 					}
 					timeLine = twitter.getUserTimeline(userIdLong, paging);
 				}
 
 				callCount(currentAuthId);
 				
 				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(thisUsersFile, true)));
 				for (Status status : timeLine) {
 					long tmpid = status.getId(); 
 					maxid = (tmpid > maxid)? tmpid : maxid;
 					String rawJSON = DataObjectFactory.getRawJSON(status);
 					bw.write(rawJSON);
 					bw.newLine();
 				}
 				
 				bw.close();
 				
 				BufferedWriter cbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(thisUsersCurr)));
 				cbw.write(Long.toString(maxid));
 				cbw.close();
 				System.out.println(thisUsersFile);
 				//break;
 			}
 		} catch (Exception e) {
 			saveAuthInfo();
 			e.printStackTrace();
 		}
 
 		saveAuthInfo();
 		System.out.println("Done.");
 	}
 
 }
