 package com.tug.kite;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.util.Log;
 
 public class StatEngine {
 
 	// set the data to be collected
 
 	private int total = 0;
 	public int total() { //we have a getter allowing other people to read this result, but only we can set it
 		return total;
 	}
 	private int sent = 0;
 	public int sent() { return sent; }
 	private int received = 0;
 	public int received() { return received; }
 
 	private int kissesSent = 0;
 	public int kissesSent() { return kissesSent; }
 	private int kissesReceived = 0;
 	public int kissesReceived() { return kissesReceived; }
 	private int questionsSent = 0;
 	public int questionsSent() { return questionsSent; }
 	private int questionsReceived = 0;
 	public int questionsReceived() { return questionsReceived; }
 	private int smileysSent = 0;
 	public int smileysSent() { return smileysSent; }
 	private int smileyReceived = 0;
 	public int smileyReceived() { return smileyReceived; }
 
 	private int sendQuarterCount = 0;
 	public int sendQuarterCount() { return sendQuarterCount; }
 	private int sendHourCount = 0;
 	public int sendHourCount() { return sendHourCount; }
 	private int sendDayCount = 0;
 	public int sendDayCount() { return sendDayCount; }
 	private int sendWeekCount = 0;
 	public int sendWeekCount() { return sendWeekCount; }
 	private int sendWeekPlusCount = 0;
 	public int sendWeekPlusCount() { return sendWeekPlusCount; }
 
 	private int receivedQuarterCount = 0;
 	public int receivedQuarterCount() { return receivedQuarterCount; }
 	private int receivedHourCount = 0;
 	public int receivedHourCount() { return receivedHourCount; }
 	private int receivedDayCount = 0;
 	public int receivedDayCount() { return receivedDayCount; }
 	private int receivedWeekCount = 0;
 	public int receivedWeekCount() { return receivedWeekCount; }
 	private int receivedWeekPlusCount = 0;
 	public int receivedWeekPlusCount() { return receivedWeekPlusCount; }
 
 	// Text-length monitoring
 	private ArrayList<Integer> sentLengths = new ArrayList<Integer>();
 	private ArrayList<Integer> receivedLengths = new ArrayList<Integer>();
 
 	// declare the ArrayList of reply-time integers
 	private int lastMessageStatus = 0; // sent = 2, received = 1
 	private int lastMessageTime = 0;   // these are states of the engine. noone else needs to know
 	private ArrayList<Integer> replySpeeds = new ArrayList<Integer>(); // Noone needs these arrays, we shall calculate means here
 	private ArrayList<Integer> sendSpeeds = new ArrayList<Integer>();
 
 	// Double-texts
 	private int sentDoubles = 0;
 	public int sentDoubles() { return sentDoubles; }
 	private int receivedDoubles = 0;
 	public int receivedDoubles() { return receivedDoubles; }
 	
 	public StatEngine() { //constructor
 	}
 	
 	// The method below is for counting things like kisses and question-marks
 	public static int countOccurrences(String base, String searchFor) {
 
 		int len = searchFor.length();
 		int result = 0;
 		if (len > 0) {
 			int start = base.indexOf(searchFor);
 			while (start != -1) {
 				result++;
 				start = base.indexOf(searchFor, start + len);
 			}
 		}
 		
 		return result;
 
 	}
 
 	public static int countKisses(String searchText) {
 		int kisses = 0;
 		Pattern patternOneKiss = Pattern.compile("(\\p{Punct}|\\s)(?i)x(\\s|$)"); // (punctuation or whitespace) followed by x followed by (whitespace or end of line)
 		Pattern patternManyKiss = Pattern.compile("(?i)x{2,}"); // 2 or more 'x's after each other
 		Matcher matcherOneKiss = patternOneKiss.matcher(searchText);
 		Matcher matcherManyKiss = patternManyKiss.matcher(searchText);
 		
 		while (matcherOneKiss.find()) {
 		  kisses++;
 		}
 		while (matcherManyKiss.find()) {
 		  kisses = kisses + matcherManyKiss.end() - matcherManyKiss.start();
 		}
 		return kisses;
 	}
 	
 	public void processMessage(String message, int replyTime, int messageStatus) {
 		// messages sent
 		if (messageStatus == 2) {
			total++;
 			sent++;
 			// length of message
 			sentLengths.add(message.length());
 
 			Log.d("ENGINE", "Message Sent: " + message);
 
 			// see if this is a reply or a double
 
 			if (lastMessageStatus == 2) {
 				sentDoubles++;
 			} else if (lastMessageStatus == 1) {
 				Integer sendDiff = lastMessageTime
 						- replyTime;
 				sendSpeeds.add(sendDiff);
 				Log.d("Difference in time",
 						sendDiff.toString());
 			}
 
 			// kisses sent
 			kissesSent = kissesSent + countKisses(message);
 
 			questionsSent = questionsSent + countOccurrences(
 					message, "?");
 			// smiley's sent
			// TODO: Regex this!
 			String[] smileys = { ":)", ";)", ":P",
 					":D", ";D" };
 			for (int i = 0; i < smileys.length; i++) {
 
 				if (message.indexOf(smileys[i]) > 0) {
 					smileysSent++;
 
 				}
 			}
			// set the LastMessageStatus for next loop
			lastMessageStatus = messageStatus;
			lastMessageTime = replyTime;
 		} else if (messageStatus == 1) { // messages
 											// received
			total++;
 			received++;
 			receivedLengths.add(message.length());
 			// see if this a reply or a follow-up
 			// text
 			if (lastMessageStatus == 1) {
 				receivedDoubles++;
 			} else if (lastMessageStatus == 2) {
 				Integer replyDiff = lastMessageTime
 						- replyTime;
 				replySpeeds.add(replyDiff);
 				Log.d("Difference in time",
 						replyDiff.toString());
 			}
 
 			Log.d("ENGINE", "Message Received: "
 					+ message);
 			// kisses received
 			kissesReceived = kissesReceived + countKisses(message);
 			
 			
 			questionsReceived = questionsReceived + countOccurrences(
 					message, "?");
 			// smiley's received
			//TODO: Regex this shit!
 			String[] smileys = { ":)", ";)", ":P",
 					":D", ";D" };
 			for (int i = 0; i < smileys.length; i++) {
 
 				if (message.indexOf(smileys[i]) > 0) {
 					smileyReceived++;
 
 				}
 			}
			// set the LastMessageStatus for next loop
			lastMessageStatus = messageStatus;
			lastMessageTime = replyTime;
 		} else {
 
 			Log.d("ENGINE",	"Not sent or received. Odd");
 		}
 
 	}
 	
 }
