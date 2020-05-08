 package com.frenberg.tid;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class Tid {
 	private long currentTimeMillis;
 	private long workingTimeMillis;
 	private long longestPause = 0;
 
 	public Map<String, String> calculate(String input, boolean dayBeforeHoliday) {
 		currentTimeMillis = System.currentTimeMillis();
 
 		int numberOfValidInputs = 0;
 		long time = 0, tmpTime = 0, accumulatedTime = 0, lastTime = 0;
 		Map<String, String> returnStrings = new HashMap<String, String>();
 
 		Calendar cal = Calendar.getInstance();
 		if (cal.get(Calendar.MONTH) > 4 && cal.get(Calendar.MONTH) < 8) {
 			// kortare arbetstid under juni-augusti (7,18)
 			workingTimeMillis = 25848000;
 		} else {
 			// övrig period är ordinarie arbetstid 8,18
 			workingTimeMillis = 29448000;
 		}
 		
		if (dayBeforeHoliday) {
			workingTimeMillis -= 7200000; // two hours less if day before holiday
		}

 		SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
 		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
 
 		String[] lines = input.split("\r?\n|\r");
 
 		for (String line : lines) {
 			time = 0;
 			line = line.trim();
 			if (!line.equals("") && Pattern.matches("^\\d\\d\\:\\d\\d$", line)) {
 				numberOfValidInputs++;
 
 				try {
 					time = parser.parse(line).getTime();
 				} catch (ParseException e) {
 					// ska inte hända med rätt regexp
 					numberOfValidInputs--;
 					continue;
 				}
 				if (numberOfValidInputs % 2 == 0) {
 					lastTime = time;
 					accumulatedTime += (time - tmpTime);
 				} else {
 					tmpTime = time;
 					if (numberOfValidInputs > 1
 							&& longestPause < (time - lastTime)) {
 						longestPause = (time - lastTime);
 					}
 				}
 			}
 		}
 
 		if (longestPause > 0 && longestPause < 1800000) { // mindre än en
 															// halvtimmas lunch?
 			returnStrings
 					.put("warning",
 							"Om du varit utstämplad under mindre än 30 minuter för lunch,\nmåste du korrigera stämplingstiden och beräkna på nytt.");
 		}
 
 		// ojämna stämplingar så visas arbetad tid och tidpunkt för full
 		// arbetsdag
 		if (numberOfValidInputs % 2 == 1 && numberOfValidInputs > 0) {
 
 			try {
 				time = parser.parse(
 						formatter.format(new Date(currentTimeMillis)))
 						.getTime(); // hela minuter
 			} catch (ParseException e) {
 				// ska inte hända
 			}
 			accumulatedTime += (time - tmpTime);
 
 			long date = currentTimeMillis
 					+ (workingTimeMillis - accumulatedTime);
 			if (numberOfValidInputs == 1) {
 				// bara en stämpling, lägg på en timmes lunch
 				date += 3600000;
 			}
 
 			// bara en stämpling och tid på dagen är nu efter 13 - dra av en
 			// timme för lunch
 			if (numberOfValidInputs == 1 && time > 43200000) {
 				accumulatedTime -= 3600000;
 			}
 
 			writeLog(String.format("%.2f", accumulatedTime / 3600000.0));
 
 			cal.setTime(new Date(date));
 			cal.set(Calendar.SECOND, 0);
 			cal.set(Calendar.MILLISECOND, 0);
 			date = cal.getTimeInMillis();
 
 			returnStrings.put("notificationTime", Long.toString(date));
 			returnStrings
 					.put("response",
 							String.format(
 									"Du får gå hem %s, går du hem nu har du jobbat %.2f timmar (%.2fh).",
 									formatter.format(new Date(date)),
 									accumulatedTime / 3600000.0,
 									Math.abs((workingTimeMillis - accumulatedTime) / 3600000.0)));
 		} else {
 			// Summera
 			if (numberOfValidInputs == 2) {
 				accumulatedTime -= 3600000;
 			}
 			returnStrings.put("response", String.format(
 					"Summerad arbetstid: %.2f timmar (%.2fh).",
 					accumulatedTime / 3600000.0,
 					Math.abs((workingTimeMillis - accumulatedTime) / 3600000.0)));
 		}
 
 		writeLog(String.format("%.2f", accumulatedTime / 3600000.0));
 		return returnStrings;
 	}
 
 	private boolean writeLog(String time) {
 		String fileRow;
 		ArrayList<String> fileContentRows = new ArrayList<String>();
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		String today = formatter.format(new Date(this.currentTimeMillis));
 
 		// start by adding current calculation as first row in file
 		fileContentRows.add(today + "\t" + time);
 
 		// Read file and find a row for current date.
 		try {
 			FileInputStream fileInputStream = new FileInputStream(
 					"tidhistorik.log");
 
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fileInputStream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
 			// Read File Line By Line
 			while ((fileRow = br.readLine()) != null) {
 				if (!fileRow.startsWith(today)) {
 					fileContentRows.add(fileRow);
 				}
 			}
 
 			// Close the input stream
 			in.close();
 		} catch (FileNotFoundException e) {
 			// this is ok...
 		} catch (Exception e) {// Catch exception if any
 			e.printStackTrace();
 			return false;
 		}
 
 		FileWriter fileOutputStream;
 		try {
 			fileOutputStream = new FileWriter("tidhistorik.log");
 			BufferedWriter out = new BufferedWriter(fileOutputStream);
 			for (String textRow : fileContentRows) {
 				out.write(textRow + "\n");
 			}
 			out.close();
 		} catch (IOException e) {
 			return false;
 		}
 
 		return true;
 	}
 }
