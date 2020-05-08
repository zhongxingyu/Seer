 /**
  * @author David S Anderson
  *
  *
  * Copyright (C) 2012 David S Anderson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.dsanderson.morctrailreport.parser;
 
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
 
 import org.dsanderson.xctrailreport.core.ReportDate;
 import org.dsanderson.xctrailreport.core.TrailReport;
 import org.dsanderson.xctrailreport.core.TrailReportPool;
 import org.dsanderson.xctrailreport.core.Units;
 
 /**
  * 
  */
 public class MorcAllReportScanner {
 
 	private final TrailReportPool trailReportPool;
 	private final Scanner scanner;
 
 	private TrailReport trailReport;
 
 	public MorcAllReportScanner(InputStream stream, TrailReportPool reportPool) {
 		trailReportPool = reportPool;
 		scanner = new Scanner(stream);
 	}
 
 	public TrailReport getTrailReport() {
 		return trailReport;
 	}
 
 	public int findLastPage() {
		if (findNext("\\Q<span class=\"first_last\"><a href=\"\\E.*\\Q/page\\E")) {
			String lastPageString = scan("", "\\Q\"\\E", ".*");
 			if (lastPageString != null && !lastPageString.isEmpty()) {
 				return Integer.parseInt(lastPageString);
 			}
 		}
 		return 1;
 	}
 
 	public boolean findStartOfReports() {
 		return findNext("\\Q<ol id=\"posts\" class=\"posts\" start=\"1\">\\E");
 	}
 
 	public boolean scanReports() {
 		if (!endOfReports()) {
 			scanSingleReport();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private boolean endOfReports() {
 		return !findNext("\\Q<li class=\"postbitlegacy postbitim postcontainer\\E");
 	}
 
 	void scanSingleReport() {
 		trailReport = trailReportPool.newItem();
 
 		scanDate();
 		scanAuthor();
 		scanSummary();
 		scanDetailed();
 	}
 
 	private void scanDate() {
 		findNext("\\Q<span class=\"date\">\\E");
 		String dateString = scan("", "\\Q,&nbsp;<span class=\"time\">\\E", ".*");
 		String timeString = " " + scan("", "\\Q</span></span>\\E", ".*");
 
 		SimpleDateFormat dateFormat;
 		if (dateString.contains("Today") || dateString.contains("Yesterday")) {
 			dateFormat = new SimpleDateFormat("hh:mm a");
 			// 03-16-2012 07:48 AM
 			try {
 				GregorianCalendar today = new GregorianCalendar();
 				if (dateString.contains("Yesterday")) {
 					today.setTimeInMillis(today.getTimeInMillis()
 							- Units.daysToMilliseconds(1));
 				}
 				GregorianCalendar date = new GregorianCalendar();
 				date.setTime(dateFormat.parse(timeString));
 				date.set(GregorianCalendar.YEAR,
 						today.get(GregorianCalendar.YEAR));
 				date.set(GregorianCalendar.MONTH,
 						today.get(GregorianCalendar.MONTH));
 				date.set(GregorianCalendar.DATE,
 						today.get(GregorianCalendar.DATE));
 				trailReport.setDate(new ReportDate(date.getTime()));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		} else {
 			dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
 			// 03-16-2012 07:48 AM
 			try {
 				Date date = dateFormat.parse(dateString + " " + timeString);
 				trailReport.setDate(new ReportDate(date));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void scanAuthor() {
 		findNext("\\Q<div class=\"username_container\">\\E");
 		scanner.nextLine();
 		String authorName = scan("\\Q<font size=\"2pt\"><b>\\E",
 				"\\Q</b><br></font>\\E", ".*");
 		// / TODO parse author profile url
 		trailReport.setAuthor(authorName);
 	}
 
 	private void scanSummary() {
 		if (!findNext("\\Q<!-- Trail Condition Block -->\\E"))
 			return;
 		if (findNext("\\Q<b>Trail Condition: </b>\\E"))
 			trailReport.setSummary(scanner.nextLine().trim());
 	}
 
 	private void scanDetailed() {
 		// TODO cannot handle quote block in here
 		if (!findNext("\\Q<b>Details:&nbsp</b>\\E"))
 			return;
 		scanner.nextLine();
 		String detailString = "";
 		String lineString;
 		while (scanner.hasNextLine()) {
 			lineString = scanner.nextLine();
 			if (lineString.contains("<div class=\"bbcode_container\">")) {
 				detailString += scanQuote();
 			} else if (lineString.contains("</div>")) {
 				break;
 			} else {
 				detailString += lineString;
 			}
 		}
 		detailString = detailString.replace("<br />", "\n");
 		detailString = detailString.replace("\n\n", "\n");
 		detailString = detailString.replace("\n \n", "\n");
 		detailString = removeImage(detailString);
 		trailReport.setDetail(detailString.trim());
 	}
 
 	private String scan(String start, String end, String target) {
 		String result = null;
 
 		result = scanner.findInLine(start + target + end);
 		if (result != null) {
 			if (start != null && !start.isEmpty()) {
 				String results[] = result.split(start);
 				if (results.length < 2)
 					return null;
 
 				result = results[1];
 			}
 
 			if (end != null && !end.isEmpty()) {
 				String results[] = result.split(end);
 				if (results.length < 1)
 					return null;
 				result = results[0];
 			}
 		}
 		return result;
 	}
 
 	private boolean findNext(String pattern) {
 		return scanner.findWithinHorizon(pattern, 0) != null;
 	}
 
 	private String removeImage(String htmlString) {
 		return htmlString.replaceAll("\\Q<img\\E.*\\Q>\\E", "");
 	}
 
 	private String scanQuote() {
 		if (!findNext("\\QOriginally Posted by <strong>\\E"))
 			return "";
 		String quoteAuthor = scan("", "\\Q</strong>\\E", ".*");
 		scanner.nextLine();
 		String urlString = scan("\\Q<a href=\"\\E", "[\"]", ".*");
 
 		if (!findNext("\\Q<div class=\"message\">\\E"))
 			return "";
 		String quoteText = scan("", "\\Q</div>\\E", ".*");
 		// need to close 3 /div tags
 		for (int i = 0; i < 3; i++) {
 			if (!findNext("\\Q</div>\\E"))
 				return "";
 		}
 
 		String returnString = "\"" + quoteText + "\"" + "<br>";
 		if (urlString != null) {
 			returnString += "<a href=\"" + MorcSpecificTrailInfo.FORUM_PREFIX
 					+ urlString + "\">" + quoteAuthor + "</a><br>";
 		} else {
 			returnString += "<b>" + quoteAuthor + "</b><br>";
 		}
 		return returnString;
 	}
 }
