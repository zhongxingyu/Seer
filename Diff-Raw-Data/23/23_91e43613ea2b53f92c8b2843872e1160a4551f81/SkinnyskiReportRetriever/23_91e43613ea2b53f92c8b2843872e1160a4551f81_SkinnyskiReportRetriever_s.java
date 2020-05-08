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
 
 import org.dsanderson.util.INetConnection;
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.IReportRetriever;
 import org.dsanderson.xctrailreport.core.ITrailInfoList;
 import org.dsanderson.xctrailreport.core.ITrailReportList;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 
 /**
  * 
  */
 public class SkinnyskiReportRetriever implements IReportRetriever {
 
 	private final IAbstractFactory factory;
 	private final SkinnyskiFactory skinnySkiFactory;
 
 	public SkinnyskiReportRetriever(IAbstractFactory factory,
 			SkinnyskiFactory skinnyskiFactory) {
 		this.factory = factory;
 		this.skinnySkiFactory = skinnyskiFactory;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.IReportRetriever#getReports(org.dsanderson.TrailInfo)
 	 */
 	public void getReports(ITrailReportList trailReports,
 			ITrailInfoList trailInfos) throws Exception {
 		INetConnection netConnection = factory.getNetConnection();
 		try {
 			netConnection.connect("http://skinnyski.com/trails/reports.asp");
 			BufferedInputStream stream = new BufferedInputStream(
 					netConnection.getStream());
 			SkinnyskiScanner scanner = new SkinnyskiScanner(stream,
 					factory.getTrailReportPool(), factory.getTrailInfoPool(),
 					skinnySkiFactory.getTrailInfoPool());
 
 			for (String region : skinnySkiFactory.getRegions().getRegions()) {
 
 				if (scanner.findRegion(region)) {
 					while (scanner.scanRegion()) {
 						TrailReport newTrailReport = scanner.getTrailReport();
 						TrailInfo newTrailInfo = scanner.getTrailInfo();
 
 						newTrailInfo = trailInfos.mergeIntoList(newTrailInfo);
 
 						newTrailReport.setTrailInfo(newTrailInfo);
 						newTrailReport
 								.setSource(SkinnyskiFactory.SKINNYSKI_SOURCE_NAME);
 						trailReports.add(newTrailReport);
 					}
 				}
 			}
 		} finally {
 			netConnection.disconnect();
 		}
 	}
 
 }
