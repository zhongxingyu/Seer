 package com.tug.kite;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.regex.*;
 
 import com.jjoe64.graphview.BarGraphView;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphView.GraphViewSeries;
 import com.jjoe64.graphview.GraphView.LegendAlign;
 import com.jjoe64.graphview.LineGraphView;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.Nickname;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Tugging extends Activity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	}
 
 	private static final int CONTACT_PICKER_RESULT = 1001;
 	private static final String DEBUG_TAG = null;
 
 	public void doLaunchContactPicker(View view) {
 		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
 				Contacts.CONTENT_URI);
 		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
 	}
 
 	// The method below is used for matching the last characters of the string
 	public String getLastnCharacters(String inputString, int subStringLength) {
 		int length = inputString.length();
 		if (length <= subStringLength) {
 			return inputString;
 		}
 		int startIndex = length - subStringLength;
 		return inputString.substring(startIndex);
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
 	
 	// The method below creates the counter and pushes it to the view as a
 	// separate, runnable thread
 	public void countUp(final TextView flipScore, final int topNum,
 			final int speedNum) {
 
 		new Thread(new Runnable() {
 			int counter = 0;
 
 			public void run() {
 				flipScore.post(new Runnable() {
 					public void run() {
 						flipScore.setText("0");
 						flipScore.setTextSize(55);
 					    flipScore.setTextColor(Color.GRAY);
 					} 
 				});
 				while (counter < topNum) {
 					try {
 						Thread.sleep(speedNum);
 					} catch (InterruptedException e) {
 
 						e.printStackTrace();
 					}
 					flipScore.post(new Runnable() {
 
 						public void run() {
 
 							// change font-size for ridic numbers
 							// TODO fix distortion
 							if (counter > 99) {
 								flipScore.setTextSize(45);
 							}
 							if (counter > 999) {
 								flipScore.setTextSize(35);
 							}
 
 							flipScore.setText("" + counter);
 
 						}
 
 					});
 					counter++;
 				}
 				flipScore.post(new Runnable() { 
 					public void run() {
 						flipScore.setTextColor(Color.BLACK);
 					}
 				});
 
 			}
 
 		}).start();
 
 	}
 
 	// This method calculates the mean
 	public static double findMean(ArrayList<Integer> anArray) {
 		ArrayList<Integer> myArray = anArray;
 		double arraySum = 0;
 		double arrayAverage = 0;
 		for (int x = 0; x < myArray.size() - 1; x++)
 			arraySum += myArray.get(x);
 		arrayAverage = arraySum / myArray.size();
 		return arrayAverage;
 	}
 
 	// this method finds the median
 	public static double findMedian(ArrayList<Integer> anArray) {
 		ArrayList<Integer> myArray = anArray;
 		Collections.sort(myArray);
 		int arrayLength = 0;
 		double arrayMedian = 0;
 		int currentIndex = 0;
 		arrayLength = myArray.size();
 		if (arrayLength % 2 != 0) {
 			currentIndex = ((arrayLength / 2) + 1);
 			arrayMedian = myArray.get(currentIndex - 1);
 		} else if (arrayLength == 0) {
 			arrayMedian = 0; //temp fix
 		} else {
 			int indexOne = (arrayLength / 2);
 			int indexTwo = arrayLength / 2 + 1;
 			Log.d("MEDIAN", "indexone: " + indexOne + " indexTwo " + indexTwo);
 			double arraysSum = myArray.get(indexOne - 1)
 					+ myArray.get(indexTwo - 1);
 			arrayMedian = arraysSum / 2;
 		}
 		return arrayMedian;
 	}
 
 	// this method converts milliseconds into a nice string that can be printed
 	public static String returnTime(Double milliSeconds) {
 
 		Double pure = milliSeconds;
 		Integer diff = (int) Math.round(pure);
 
 		Integer diffSecs = diff / 1000;
 		Integer diffMin = diff / (60 * 1000); // minutes
 		Integer diffHours = diff / (60 * 60 * 1000); // hours
 		Integer diffDays = diff / (24 * 60 * 60 * 1000);
 
 		String naturalTime = "";
 
 		if (diffSecs < 60) {
 			naturalTime = diffSecs.toString() + " secs";
 		} else if (diffMin > 1 && diffHours < 1) {
 			naturalTime = diffMin.toString() + " mins";
 		} else if (diffHours > 1 && diffDays < 1) {
 			naturalTime = diffHours.toString() + " hrs";
 		} else if (diffDays > 1) {
 			naturalTime = diffDays.toString() + " days";
 		}
 		return naturalTime;
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK) {
 			switch (requestCode) {
 			case CONTACT_PICKER_RESULT:
 				Cursor cursor = null;
 				String phone = "";
 				String name = "";
 				try {
 					Uri result = data.getData();
 					Log.v(DEBUG_TAG,
 							"Got a contact result: " + result.toString());
 
 					// get the contact id from the Uri
 					String id = result.getLastPathSegment();
 
 					// query for everything phone
 					cursor = getContentResolver().query(Phone.CONTENT_URI,
 							null, Phone.CONTACT_ID + "=?", new String[] { id },
 							null);
 
 					int phoneIdx = cursor.getColumnIndex(Phone.DATA);
 					int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
 
 					// let's just get the first phone
 					if (cursor.moveToFirst()) {
 						
 name = cursor.getString(nameIdx);
 						
 					//	Toast.makeText(this, "Name: " + name,
 								//Toast.LENGTH_LONG).show();
 						
 						phone = cursor.getString(phoneIdx);
 						
 						//Toast.makeText(this, "Phone: " + phone,
 							//	Toast.LENGTH_LONG).show();
 						
 						
 						Log.v(DEBUG_TAG, "Got phone: " + phone);
 						Log.v(DEBUG_TAG, "Got name:" + name);
 						//TODO strip string down to first name
 						// TODO change rat search into an array to allow for
 						// multiple numbers
 
 					} else {
 						Log.w(DEBUG_TAG, "No results");
 
 					}
 				} catch (Exception e) {
 					Log.e(DEBUG_TAG, "Failed to get phone data", e);
 				} finally {
 					if (cursor != null) {
 						cursor.close();
 					}
 
 					// if the number cannot be found let the user know and stop
 					// the stat engine running
 					if (phone.length() == 0) {
 						Toast.makeText(this, "No phone number found.",
 								Toast.LENGTH_LONG).show();
 					} else {
 
 						// START THE STAT ENGINE!
 						Uri uriSMSURI = Uri.parse("content://sms"); // access
 																	// the
 																	// sms db
 						Cursor cur = getContentResolver().query(uriSMSURI,
 								null, null, null, null);
 
 						// set the data to be collected
 
 						Integer total = 0;
 						Integer sent = 0;
 						Integer received = 0;
 
 						Integer kissesSent = 0;
 						Integer kissesReceived = 0;
 						Integer questionsSent = 0;
 						Integer questionsReceived = 0;
 						Integer smileysSent = 0;
 						Integer smileyReceived = 0;
 
 						Integer sendQuarterCount = 0;
 						Integer sendHourCount = 0;
 						Integer sendDayCount = 0;
 						Integer sendWeekCount = 0;
 						Integer sendWeekPlusCount = 0;
 
 						Integer receivedQuarterCount = 0;
 						Integer receivedHourCount = 0;
 						Integer receivedDayCount = 0;
 						Integer receivedWeekCount = 0;
 						Integer receivedWeekPlusCount = 0;
 
 						// Text-length monitoring
 						ArrayList<Integer> sentLengths = new ArrayList<Integer>();
 						ArrayList<Integer> receivedLengths = new ArrayList<Integer>();
 
 						// declare the ArrayList of reply-time integers
 						Integer lastMessageStatus = 0; // sent = 2, received = 1
 						Integer lastMessageTime = 0;
 						ArrayList<Integer> replySpeeds = new ArrayList<Integer>();
 						ArrayList<Integer> sendSpeeds = new ArrayList<Integer>();
 
 						// Double-texts
 						Integer sentDoubles = 0;
 						Integer receivedDoubles = 0;
 
 						Integer timesRun = 0;
 						Integer draftCount = 0;
 
 						String cleanPhone = getLastnCharacters(phone, 7);
 
 						while (cur.moveToNext()) {
 
 							// Number-matching is done from end to beginning
 							// with 7 figures
 
 							// Log.i("Number of message/draft", num.toString());
 							if (cur.getString(2) != null) { // catch drafts this
 															// way?
 
 								String num = cur.getString(2);
 								// TODO fix draft-handling
 								Integer messageStat = cur.getInt(8);
 								Log.d("message-type", messageStat.toString());
 
 								String cleanNum = getLastnCharacters(num, 7);
 
 								Log.d(phone, "Debug: " + cleanPhone.toString()
 										+ " == " + cleanNum.toString());
 
 								if (cleanNum.equals(cleanPhone)) {
 									Integer messageStatus = cur.getInt(8);
 									Integer replyTime = cur.getInt(4);
 									String message = cur.getString(11);
 									total++;
 
 									// messages sent
 									if (messageStatus == 2) {
 										sent++;
 										// length of message
 										sentLengths.add(message.length());
 
 										Log.d(phone, "Message Sent: " + message);
 
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
 										//kissesSent = kissesSent + countOccurrences(message, " x") + countOccurrences(message, " x ") + countOccurrences(message, "xx");
 										kissesSent = kissesSent + countKisses(message);
 
 										questionsSent = questionsSent + countOccurrences(
 												message, "?");
 										// smiley's sent
 										String[] smileys = { ":)", ";)", ":P",
 												":D", ";D" };
 										for (int i = 0; i < smileys.length; i++) {
 
 											if (message.indexOf(smileys[i]) > 0) {
 												smileysSent++;
 
 											}
 										}
 
 									} else if (messageStatus == 1) { // messages
 																		// received
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
 
 										Log.d(phone, "Message Received: "
 												+ message);
 										// kisses received
 										//kissesReceived = kissesReceived + countOccurrences(message, " x") + countOccurrences(message, " x ") + countOccurrences(message, "xx");
 										kissesReceived = kissesReceived + countKisses(message);
 										
 										
 										questionsReceived = questionsReceived + countOccurrences(
 												message, "?");
 										// smiley's received
 										String[] smileys = { ":)", ";)", ":P",
 												":D", ";D" };
 										for (int i = 0; i < smileys.length; i++) {
 
 											if (message.indexOf(smileys[i]) > 0) {
 												smileyReceived++;
 
 											}
 										}
 
 									} else {
 
 										Log.d(phone,
 												"Not sent or received. Odd");
 									}
 
 									// set the LastMessageStatus for next loop
 									lastMessageStatus = messageStatus;
 									lastMessageTime = replyTime;
 								} else {
 									// log no texts match to person here
 									Log.d(phone, "Not a match to rat.");
 								}
 
 							} else {
 								Log.i("DRAFT", "This is a draft.");
 
 								draftCount++;
 							}
 
 						}
 						Log.i("Draft:", draftCount.toString());
 
 						// if there's fewer than 2 messages, throw an error
 						if (total == 0) {
 							Toast.makeText(this, "No messages found.",
 									Toast.LENGTH_LONG).show();
 						} else if (total == 1) {
 							Toast.makeText(this, "1 message is not enough",
 									Toast.LENGTH_LONG).show();
 						} else if (total == 2) {
 							Toast.makeText(this, "2 messages are not enough",
 									Toast.LENGTH_LONG).show();
 
 							// TODO handle small sent or received better in
 							// graphing array
 							
 						} else if (sent == 0) {
 							Toast.makeText(this, "You have sent no messages",
 									Toast.LENGTH_LONG).show();
 						} else if (received == 0) {
 							Toast.makeText(this, "You have received no messages",
 									Toast.LENGTH_LONG).show();
 						} else {
 
 							timesRun++;
 
 							// GRAPHING TIME
 
 							// replySpeeds
 
 							Integer replyNum = replySpeeds.size();
 							GraphViewData[] replyData = new GraphViewData[replyNum];
 
 							Log.i("About to spin data in", "DATA GOING IN");
 							for (int i = 0; i < replyNum; i++) {
 
 								// push data into graph's line array
 								replyData[i] = new GraphViewData(i,
 										replySpeeds.get(i));
 								Log.i("pushing reply data", replySpeeds.get(i)
 										.toString());
 
 								// count delay categories
 								Integer diff = (int) Math.round(replySpeeds
 										.get(i));
 								Integer diffMin = diff / (60 * 1000); // minutes
 								Integer diffHours = diff / (60 * 60 * 1000); // hours
 								Integer diffDays = diff / (24 * 60 * 60 * 1000);
 
 								if (diffMin < 15) {
 									receivedQuarterCount++;
 								} else if (diffMin > 15 && diffMin < 60) {
 									receivedHourCount++;
 								} else if (diffHours > 1 && diffHours < 24) {
 									receivedDayCount++;
 								} else if (diffDays > 1 && diffDays < 7) {
 									receivedWeekCount++;
 								} else if (diffDays > 7) {
 									receivedWeekPlusCount++;
 								}
 
 							}
 
 							GraphViewSeries seriesReplies = new GraphViewSeries(
 									"Replying speeds", Color.rgb(200, 50, 00),
 									replyData);
 
 							// sendSpeeds
 							Integer sendNum = sendSpeeds.size();
 							GraphViewData[] sendData = new GraphViewData[sendNum];
 
 							for (int i = 0; i < sendNum; i++) {
 								// push data into graph's array
 								sendData[i] = new GraphViewData(i,
 										sendSpeeds.get(i));
 								Log.i("pushing send data", sendSpeeds.get(i)
 										.toString());
 
 								// count delay categories
 								Integer diff = (int) Math.round(sendSpeeds
 										.get(i));
 								Integer diffMin = diff / (60 * 1000); // minutes
 								Integer diffHours = diff / (60 * 60 * 1000); // hours
 								Integer diffDays = diff / (24 * 60 * 60 * 1000);
 
 								if (diffMin < 15) {
 									sendQuarterCount++;
 								} else if (diffMin > 15 && diffMin < 60) {
 									sendHourCount++;
 								} else if (diffHours > 1 && diffHours < 24) {
 									sendDayCount++;
 								} else if (diffDays > 1 && diffDays < 7) {
 									sendWeekCount++;
 								} else if (diffDays > 7) {
 									sendWeekPlusCount++;
 								}
 
 							}
 
 							GraphViewSeries seriesSent = new GraphViewSeries(
 									"Sending speeds", Color.rgb(0, 184, 0),
 									sendData);
 
 							// LinearLayout graphBox = (LinearLayout)
 							// findViewById(R.id.graph1);
 
 							GraphView graphView = new BarGraphView(this,
 									"Reply Speeds");
 
 							if (timesRun > 1) {
 								// graphBox.removeAllViews();
 							}
 
 							graphView.addSeries(seriesReplies);
 							graphView.addSeries(seriesSent);
 							// set legend
 							graphView.setShowLegend(true);
 							graphView.setLegendAlign(LegendAlign.BOTTOM);
 							graphView.setLegendWidth(200);
 
 							Log.d("Update Graph", "About to update...");
 
 							// graphBox.addView(graphView); // TODO fix graph
 							// updating
 
 							// Average Calculating
 							Double averageSentSpeedRaw = findMean(sendSpeeds);
 							Double averageReceivedSpeedRaw = findMean(replySpeeds);
 
 							String averageSentSpeed = returnTime(averageSentSpeedRaw);
 							String averageReceivedSpeed = returnTime(averageReceivedSpeedRaw);
 
 							// Median Calculating
 
 							// TODO fix calculations on small number of results
 							Double medianSentSpeedRaw = findMedian(sendSpeeds);
 							Double medianReceivedSpeedRaw = findMedian(replySpeeds);
 
 							String medianSentSpeed = returnTime(medianSentSpeedRaw);
 							String medianReceivedSpeed = returnTime(medianReceivedSpeedRaw);
 
 							// Push the data to the view
 
 							// Name of adversary
 							TextView nameOfRat = (TextView) findViewById(R.id.ratName);
 							nameOfRat.setText(name);
 							
 							int larger;
 							int speed;
 
 							// Score-cards!
 							// TODO calculate counter end-times
 							larger = (sent < received) ? received : sent;
 							speed = (larger > 0) ? 2000/(larger) : 0;
 							final TextView sentScore = (TextView) findViewById(R.id.sentScore);
 							countUp(sentScore, sent, speed);
 
 							final TextView receivedScore = (TextView) findViewById(R.id.receivedScore);
 							countUp(receivedScore, received, speed);
 
 							// Questions Row
 							larger = (questionsSent < questionsReceived) ? questionsReceived : questionsSent;
 							speed = (larger > 0) ? 2500/(larger) : 0;
 							TextView questionsSentCounter = (TextView) findViewById(R.id.questionsSent);
 							countUp(questionsSentCounter, questionsSent, speed);
 
 							TextView questionsReceivedCounter = (TextView) findViewById(R.id.questionsReceived);
 							countUp(questionsReceivedCounter, questionsReceived, speed);
 
 							// Kisses Row
 							larger = (kissesSent < kissesReceived) ? kissesReceived : kissesSent;
 							speed = (larger > 0) ? 3000/(larger) : 0;
 							TextView kissesSentCounter = (TextView) findViewById(R.id.kissesSent);
 							countUp(kissesSentCounter, kissesSent, speed);
 
 							TextView kissesReceivedCounter = (TextView) findViewById(R.id.kissesReceived);
 							countUp(kissesReceivedCounter, kissesReceived, speed);
 
 							// Smileys Row
 							larger = (smileysSent < smileyReceived) ? smileyReceived : smileysSent;
 							speed = (larger > 0) ? 3500/(larger) : 0;
 							TextView smileysSentCounter = (TextView) findViewById(R.id.smileysSent);
 							countUp(smileysSentCounter, smileysSent, speed);
 
 							TextView smileysReceivedCounter = (TextView) findViewById(R.id.smileysReceived);
 							countUp(smileysReceivedCounter, smileyReceived, speed);
 
 							// Doubles Row
 							larger = (sentDoubles < receivedDoubles) ? receivedDoubles : sentDoubles;
 							speed = (larger > 0) ? 4000/(larger) : 0;
 							TextView doublesSentCounter = (TextView) findViewById(R.id.doublesSent);
 							countUp(doublesSentCounter, sentDoubles, speed);
 
 							TextView doublesReceivedCounter = (TextView) findViewById(R.id.doublesReceived);
 							countUp(doublesReceivedCounter, receivedDoubles,
 									speed);
 
 							// Quarter Row
 							larger = (sendQuarterCount < receivedQuarterCount) ? receivedQuarterCount : sendQuarterCount;
 							speed = (larger > 0) ? 4500/(larger) : 0;
 							TextView quarterSent = (TextView) findViewById(R.id.quartersSent);
 							countUp(quarterSent, sendQuarterCount, speed);
 
 							TextView quarterReceived = (TextView) findViewById(R.id.quartersReceived);
 							countUp(quarterReceived, receivedQuarterCount, speed);
 
 							// Hour Row
 							larger = (sendHourCount < receivedHourCount) ? receivedHourCount : sendHourCount;
 							speed = (larger > 0) ? 5000/(larger) : 0;
 							TextView hourSent = (TextView) findViewById(R.id.hoursSent);
 							countUp(hourSent, sendHourCount, speed);
 
 							TextView hourReceived = (TextView) findViewById(R.id.hoursReceived);
 							countUp(hourReceived, receivedHourCount, speed);
 
 							// Day Row
 							larger = (sendDayCount < receivedDayCount) ? receivedDayCount : sendDayCount;
							speed = (larger > 0) ? 5500/(larger) : 0;
 							TextView daySent = (TextView) findViewById(R.id.daysSent);
 							countUp(daySent, sendDayCount, speed);
 
 							TextView dayReceived = (TextView) findViewById(R.id.daysReceived);
 							countUp(dayReceived, receivedDayCount, speed);
 
 							// Median Row
 							TextView medianSent = (TextView) findViewById(R.id.medianSent);
 							medianSent.setText(medianSentSpeed);
 
 							TextView medianReceived = (TextView) findViewById(R.id.medianReceived);
 							// TODO - fix expanding cell-size on this
 							medianReceived.setText(medianReceivedSpeed);
 
 						}
 					}
 				}
 
 				break;
 			}
 
 		} else {
 			Log.w(DEBUG_TAG, "Warning: activity result not ok");
 		}
 	}
 }
