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
 package org.dsanderson.xctrailreport.skinnyski.test;
 
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 import org.dsanderson.xctrailreport.skinnyski.RegionManager;
 import org.dsanderson.xctrailreport.skinnyski.SkinnyskiScanner;
 
 /**
  * 
  */
 public class ScannerTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		FileInputStream fileStream = null;
 
 		try {
 			fileStream = new FileInputStream("reports.asp");
 
			List<String> regions = new ArrayList<String>();

			for (String region : RegionManager.supportedRegions)
				regions.add(region);

 			SkinnyskiScanner skinnyskiScanner = new SkinnyskiScanner(fileStream);
 
			for (String region : regions) {
 
 				System.out.println("Region: " + region);
 
				skinnyskiScanner.findRegion(region);

				while (skinnyskiScanner.scanRegion()) {
					printReportInfo(skinnyskiScanner);
 				}
 			}
 
 		} catch (Exception e) {
 			System.err.println(e);
 		} finally {
 			try {
 				fileStream.close();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	private static void printReportInfo(SkinnyskiScanner skinnyskiScanner) {
 		TrailInfo trailInfo = skinnyskiScanner.getTrailInfo();
 		TrailReport trailReport = skinnyskiScanner.getTrailReport();
 
 		System.out.println("New Report:");
 		System.out.println("Date: " + trailReport.getDate().formatDate());
 		System.out.println("URL: " + trailInfo.getSkinnySkiUrl());
 		System.out.println("Submit: " + trailInfo.getSkinnySkiSubmitUrl());
 		System.out.println("Name: " + trailInfo.getSkinnyskiSearchTerm());
 		System.out.println("City: " + trailInfo.getCity() + ", "
 				+ trailInfo.getState());
 
 		System.out.println("Summary: " + trailReport.getSummary());
 		System.out.println("Detailed: " + trailReport.getDetail());
 		System.out.println("Author: " + trailReport.getAuthor());
 		System.out.println("");
 
 	}
 
 }
