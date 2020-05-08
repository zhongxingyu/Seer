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
 package org.dsanderson.xctrailreport.skinnyski;
 
 import java.io.InputStream;
 import java.util.Scanner;
 
 import org.dsanderson.xctrailreport.core.ReportDate;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 
 /**
  * 
  */
 public class SkinnyskiScanner {
 	Scanner scanner;
 	TrailReport trailReport;
 	TrailInfo trailInfo;
 
 	public SkinnyskiScanner(InputStream stream) {
 		scanner = new Scanner(stream);
 		scanner.useDelimiter("\n");
 	}
 
 	public TrailReport getTrailReport() {
 		return trailReport;
 	}
 
 	public TrailInfo getTrailInfo() {
 		return trailInfo;
 	}
 
 	public boolean findRegion(String region) {
 		while (findNext("\\<font\\ssize\\=\\+1\\>\\<b\\>")) {
 			String foundRegion;
 			if ((foundRegion = scan("", "\\<\\/b\\>", ".*")) != null) {
 				if (region.compareTo(foundRegion) == 0)
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean scanRegion() {
 		if (!endOfRegion()) {
 			scanSingleReport();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean scanReport() {
 		if (findNextReport()) {
 			scanSingleReport();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private boolean findNextReport() {
 		return findNext("\\<li\\>");
 	}
 
 	private boolean endOfRegion() {
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (line.contains("<li>"))
 				return false;
 			else if (line.contains("<P>&nbsp;<p>"))
 				return true;
 		}
 		return true;
 	}
 
 	private void scanSingleReport() {
 		trailReport = new TrailReport();
 		trailInfo = new TrailInfo();
 
 		scanDate();
 		scanUrl();
 		scanName();
 		scanCityAndState();
 		scanSummary();
 		scanDetailedAndAuthor();
 	}
 
 	private void scanDate() {
 		String date;
 		while ((date = scan("\\<b\\>", "-", ".*")) == null)
 			scanner.nextLine();
 
 		if (date != null)
 			trailReport.setDate(new ReportDate(date.trim()));
 	}
 
 	private void scanUrl() {
 		String url = null;
 		if ((url = scan("<a\\b[^>]*href=\"", "\">", "[^>]*")) != null)
 			trailInfo.setSkinnyskiUrl(url);
 	}
 
 	private void scanName() {
 		String name = null;
 		if ((name = scan("", "\\<\\/a\\> \\(", ".*")) == null)
 			name = scan("", "\\(", ".*");
 		if (name != null)
 			trailInfo.setSkinnyskiSearchTerm(name.trim());
 	}
 
 	private void scanCityAndState() {
 		String city = scan("", "\\)", ".*");
 		scanner.nextLine();
 		if (city != null) {
 			trailInfo.setCity(city.trim());
 			trailInfo.setCity("MN");
 		}
 	}
 
 	private void scanSummary() {
 		String summary = scan("Conditions\\:", "\\<br\\>", "[^\\<]*");
 		if (summary != null) {
 			trailReport.setSummary(summary.trim());
 		}
 
 	}
 
 	private void scanDetailedAndAuthor() {
 		String detailedString = "";
 		String author = null;
 
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 
 			if (line.startsWith("(")) {
 				String split[] = line.split("[\\(\\)\\<]");
 				if (split.length >= 2)
 					author = split[1];
 				else
 					author = line;
 				trailReport.setAuthor(author.trim());
 				break;
 			} else if (line.startsWith("Photos:")) {
 			} else {
 				detailedString += line;
 			}
 		}
 
 		if (detailedString != null) {
 			detailedString = detailedString.replaceAll("\\<br\\>", "\r\n")
 					.trim();
 		}
 
 		trailReport.setDetail(detailedString);
 	}
 
 	private boolean findNext(String pattern) {
 		do {
 			if (scanner.findInLine(pattern) != null)
 				return true;
 			else if (scanner.hasNextLine())
 				scanner.nextLine();
 			else
 				return false;
 		} while (true);
 	}
 
 	private String scan(String start, String end, String target) {
 		String result = null;
 
 		result = scanner.findInLine(start + target + end);
 		if (result != null) {
 			if (start != null && start.length() > 0) {
 				String results[] = result.split(start);
 				if (results.length < 2)
 					return null;
 
 				result = results[1];
 			}
 
 			if (end != null && end.length() > 0) {
 				String results[] = result.split(end);
 				if (results.length < 1)
 					return null;
 				result = results[0];
 			}
 		}
 		return result;
 	}
 
 }
