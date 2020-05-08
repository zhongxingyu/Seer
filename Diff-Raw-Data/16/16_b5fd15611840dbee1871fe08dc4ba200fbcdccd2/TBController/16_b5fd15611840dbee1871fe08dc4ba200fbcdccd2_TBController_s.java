 package de.fhb.twitzbotz.controller;
 
 import de.fhb.twitzbotz.helper.TwitterConnectHelper;
 import java.util.List;
 import java.util.logging.Level;
 import twitter4j.Status;
 import twitter4j.TwitterException;
 import twitter4j.Twitter;
 
 /**
  *
  * 
  * @author Michael Koppen
  */
 /*
  * TODO Die Keys sind so nicht sicher und muessten verschlüsselt gespeichert werden.
  */
 public class TBController {
 
 	private Twitter twitterService = null;
	private Status lastStatus = null;
 	private TwitterConnectHelper twitterConnectHelper;
 	
 
 	/**
 	 * Default-Konstruktor
 	 * 
 	 * nebst initialisierung des Objektes wird das Verbinden zum Service angestoßen.
 	 */
 	public TBController() {
 		describeEnviroment();
 		
 		twitterConnectHelper = new TwitterConnectHelper();
 		//Verbindungsaufbau mit hardcodet Account(keine PIN-Eingabe)
 		twitterService = twitterConnectHelper.connectToServiceSingleAcc();
 
 	}
 
 	/**
 	 * Methode zum Auslesen aller aktuellen Timeline auf dem jeweiligen Twitteraccount.
 	 * TODO uebergabeparameter mit anzahl auszulesender tweets
 	 * @return TweetList
 	 */
 	public List<Status> getMyTimeline() {
 		describeEnviroment();
 
 		List<Status> statuses = null;
 		try {
 
 			statuses = twitterService.getHomeTimeline();
 
 		} catch (TwitterException ex) {
 			twitterConnectHelper.handleTwitterException(ex);
 		}
 
 
 		return statuses;
 	}
 
 	public Status getUsersLatestStatus(String user) {
 		describeEnviroment();
 		return getUsersLatestStatus(this.getUsersID(user));
 	}
 
 	public Status getUsersLatestStatus(long userID) {
 		describeEnviroment();
 		Status userStatus = null;
 		try {
 			userStatus = twitterService.showUser(userID).getStatus();
 			java.util.logging.Logger.getLogger(TBController.class.getName()).log(java.util.logging.Level.INFO, "latestStatus: {0}, \nlatestStatusRaw: {1}", new Object[]{userStatus.getText(), userStatus});
 
 		} catch (TwitterException ex) {
 			twitterConnectHelper.handleTwitterException(ex);
 		}
 		return userStatus;
 	}
 
 	public List<Status> getUsersTimeline(String user) {
 		describeEnviroment();
 		return getUsersTimeline(this.getUsersID(user));
 	}
 
 	public List<Status> getUsersTimeline(long userID) {
 		describeEnviroment();
 		List<Status> userTimeline = null;
 		try {
 			userTimeline = twitterService.getUserTimeline(userID);
 			java.util.logging.Logger.getLogger(TBController.class.getName()).log(java.util.logging.Level.INFO, "User: {0}", userTimeline);
 
 		} catch (TwitterException ex) {
 			twitterConnectHelper.handleTwitterException(ex);
 		}
 		return userTimeline;
 	}
 
 	public void sendMessage(String message) {
 		describeEnviroment();
 		try {
 			lastStatus = twitterService.updateStatus(message);
 			System.out.println("Updated Status successfully to " + lastStatus.getText());
 		} catch (TwitterException ex) {
 			twitterConnectHelper.handleTwitterException(ex);
 		}
 	}
 
 	public long getUsersID(String user) {
 		describeEnviroment();
 		long userID = -1;
 		try {
 			userID = twitterService.showUser(user).getId();
 			java.util.logging.Logger.getLogger(TBController.class.getName()).log(java.util.logging.Level.INFO, "ID: {0}", userID);
 		} catch (TwitterException ex) {
 			twitterConnectHelper.handleTwitterException(ex);
 		}
 		return userID;
 	}
 	public long getMyID() {
 		describeEnviroment();
 		long userID = -1;
 		try {
 			userID = twitterService.getId();
 			java.util.logging.Logger.getLogger(TBController.class.getName()).log(java.util.logging.Level.INFO, "MyID: {0}", userID);
 		} catch (TwitterException ex) {
 			twitterConnectHelper.handleTwitterException(ex);
 		}
 		return userID;
 	}
 
 	private void describeEnviroment() {
 		StackTraceElement stackTop = new Exception().getStackTrace()[1];
 		java.util.logging.Logger.getLogger(TBController.class.getName()).log(Level.INFO, "Logger: class = {0},\n method: {1}", new Object[]{stackTop.getClassName(), stackTop.getMethodName()});
 	}
 	
 }
