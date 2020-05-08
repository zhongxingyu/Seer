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
 
 import java.io.BufferedInputStream;
import java.io.BufferedReader;
 import java.util.List;
import java.util.Scanner;
 
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.INetConnection;
 import org.dsanderson.xctrailreport.core.IReportRetriever;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 
 /**
  * 
  */
 public class SkinnyskiReportRetriever implements IReportRetriever {
 
 	IAbstractFactory factory;
 
 	public SkinnyskiReportRetriever(IAbstractFactory factory) {
 		this.factory = factory;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.IReportRetriever#getReports(org.dsanderson.TrailInfo)
 	 */
 	public void getReports(List<TrailReport> trailReports,
 			List<TrailInfo> trailInfos) throws Exception {
 		parseHtml(trailReports, trailInfos);
 	}
 
 	private void parseHtml(List<TrailReport> trailReports,
 			List<TrailInfo> trailInfos) throws Exception {
 		INetConnection netConnection = factory.getNetConnection();
 		try {
 			netConnection.connect("http://skinnyski.com/trails/reports.asp");
 			BufferedInputStream stream = new BufferedInputStream(
 					netConnection.getStream());
 			SkinnyskiScanner scanner = new SkinnyskiScanner(stream);
 
 			for (String region : factory.getUserSettings().getEnabledRegions()) {
 
 				if (scanner.findRegion(region)) {
 					while (scanner.scanRegion()) {
 						TrailReport newTrailReport = scanner.getTrailReport();
 						TrailInfo newTrailInfo = scanner.getTrailInfo();
 
 						boolean existingTrail = false;
 						TrailInfo trailInfo = null;
 						for (TrailInfo info : trailInfos) {
 							if (newTrailInfo.getSkinnyskiSearchTerm()
 									.compareTo(info.getSkinnyskiSearchTerm()) == 0) {
 								existingTrail = true;
 								trailInfo = info;
 							}
 						}
 
 						if (!existingTrail) {
 							newTrailInfo.setLocation(newTrailInfo.getCity()
 									+ ", " + newTrailInfo.getState());
 							newTrailInfo.setName(newTrailInfo
 									.getSkinnyskiSearchTerm());
 
 							trailInfos.add(newTrailInfo.copy());
 							trailInfo = trailInfos.get(trailInfos.size() - 1);
 						}
 
 						newTrailReport.setTrailInfo(trailInfo);
 						newTrailReport.setSource("SkinnySki");
 
 						trailReports.add(newTrailReport.copy());
 					}
 				}
 			}
 		} finally {
 			netConnection.disconnect();
 		}
 	} // parseHtml
 
 }
