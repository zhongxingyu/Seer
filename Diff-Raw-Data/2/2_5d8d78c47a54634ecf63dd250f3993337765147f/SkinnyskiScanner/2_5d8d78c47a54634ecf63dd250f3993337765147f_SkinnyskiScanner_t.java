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
 import org.dsanderson.xctrailreport.core.TrailInfoPool;
 import org.dsanderson.xctrailreport.core.TrailReport;
 import org.dsanderson.xctrailreport.core.TrailReportPool;
 
 /**
  * 
  */
 public class SkinnyskiScanner {
 	private final TrailReportPool trailReportPool;
 	private final TrailInfoPool trailInfoPool;
 	Scanner scanner;
 	TrailReport trailReport;
 	TrailInfo trailInfo;
 	String state;
 
 	public SkinnyskiScanner(InputStream stream, TrailReportPool reportPool,
 			TrailInfoPool infoPool) {
 		trailReportPool = reportPool;
 		trailInfoPool = infoPool;
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
 		if (findNext("\\Q<font size=+1><b>" + region + "</b>\\E")) {
 			state = RegionManager.getStateByRegion(region);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean scanRegion() throws Exception {
 		if (!endOfRegion()) {
 			scanSingleReport();
 			return true;
 		} else {
 			return false;
 		}
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
 
 	private void scanSingleReport() throws Exception {
 		trailReport = trailReportPool.newTrailReport();
 		trailInfo = trailInfoPool.newTrailInfo();
 
 		scanDate();
 		scanUrl();
 		scanName();
 		scanCityAndState();
 		scanSummary();
 		scanDetailedAndAuthor();
 
 	}
 
 	private void scanDate() throws Exception {
 		String date;
 		while ((date = scan("\\<b\\>", "-", ".*")) == null)
 			scanner.nextLine();
 
 		if (date != null)
 			trailReport.setDate(new ReportDate(date.trim()));
 	}
 
 	private void scanUrl() {
 		String indexString = null;
 		if ((indexString = scan("\\Q<a href=\"traildetail.asp?Id=\\E", "\">",
 				"[\\d]*")) != null) {
 			trailInfo.setskinnyskiTrailIndex(Integer.parseInt(indexString));
 		}
 	}
 
 	private void scanName() {
 		String name = null;
 		if ((name = scan("", "\\<\\/a\\> \\(", ".*")) == null)
 			name = scan("", "\\(", ".*");
 		if (name != null)
 			trailInfo.setSkinnyskiSearchTerm(name.trim());
 	}
 
 	private void scanCityAndState() {
 		String city = scan("", ",", ".*");
 		String state = this.state;
 		if (city != null)
 			state = scan("", "\\)", ".*");
 		else
 			city = scan("", "\\)", ".*");
 		scanner.nextLine();
 		if (city != null) {
 			trailInfo.setCity(city.trim());
 			trailInfo.setState(state.trim());
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
 				String split[] = line.split("^[\\(]", 2);
 				if (split.length >= 2) {
 					author = split[1];
					split = author.split("[\\)]$", 2);
 					author = split[0];
 					trailReport.setAuthor(author.trim());
 				}
 				break;
 			} else if (line.startsWith("Photos:")) {
 				String split[] = line.split("\\Q<a onClick=\"reportviewer(\\E",
 						2);
 				if (split.length >= 2) {
 					split = split[1].split("\\Q);\\E", 2);
 					if (split.length >= 2) {
 						trailReport
 								.setPhotosetUrl("http://skinnyski.com/tools/photoviewer.asp?reportId="
 										+ split[0]);
 					}
 				}
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
 		return scanner.findWithinHorizon(pattern, 0) != null;
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
 
 }
