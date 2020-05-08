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
 
 import java.io.BufferedInputStream;
 import java.util.Date;
 
 import org.dsanderson.util.INetConnection;
 import org.dsanderson.util.IProgressBar;
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.IReportListCreator;
 import org.dsanderson.xctrailreport.core.ISourceSpecificTrailInfo;
 import org.dsanderson.xctrailreport.core.ITrailInfoList;
 import org.dsanderson.xctrailreport.core.ITrailReportList;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 import org.dsanderson.xctrailreport.core.UserSettings;
 import org.dsanderson.xctrailreport.core.UserSettings.AutoRefreshMode;
 
 /**
  * 
  */
 public class MorcAllReportListCreator implements IReportListCreator {
 
 	private final IAbstractFactory factory;
 	private int page = 1;
 	private int lastPage = 1;
 
 	/**
 	 * 
 	 */
 	public MorcAllReportListCreator(IAbstractFactory factory) {
 		this.factory = factory;
 	}
 
 	public void setPage(int page) {
 		this.page = page;
 	}
 
 	public int getPage() {
 		return page;
 	}
 
 	public int getLastPage() {
 		return lastPage;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.IReportListCreator#getTrailReports(
 	 * org.dsanderson.xctrailreport.core.ITrailReportList,
 	 * org.dsanderson.xctrailreport.core.ITrailInfoList, boolean)
 	 */
 	@Override
 	public void getTrailReports(ITrailReportList trailReports,
 			ITrailInfoList trailInfos, boolean forced, IProgressBar progressBar)
 			throws Exception {
 		assert (trailInfos.size() <= 1);
 
 		boolean refreshNeeded = forced;
 
 		UserSettings settings = factory.getUserSettings();
 
 		if (settings.getAutoRefreshMode() == AutoRefreshMode.ALWAYS)
 			refreshNeeded = true;
 
 		if (!refreshNeeded
 				&& settings.getAutoRefreshMode() == AutoRefreshMode.IF_OUT_OF_DATE) {
 			try {
 				Date lastModified = trailReports.getTimestamp();
 				if (lastModified == null
 						|| (new Date()).getTime() - lastModified.getTime() > factory
 								.getUserSettings().getAutoRefreshCutoff())
 					refreshNeeded = true;
 			} catch (Exception e) {
 				refreshNeeded = true;
 				e.printStackTrace();
 			}
 		}
 
 		TrailInfo info = trailInfos.get(0);
 
 		if (!refreshNeeded || (info == null)) {
 			try {
 				trailReports.load();
 			} catch (Exception e) {
 				e.printStackTrace();
 				refreshNeeded = true;
 			}
 		}
 
 		if (info == null) {
 			try {
 				info = trailReports.get(0).getTrailInfo();
 			} catch (Exception e) {
 				info = null;
 			}
 
 			if (info == null)
 				throw new Exception("Cannot load trail info.");
 		}
 
 		if (refreshNeeded
 				|| (trailReports.size() == 0)
 				|| !info.getName().equals(
 						trailReports.get(0).getTrailInfo().getName())
				|| MorcFactory.getInstance().getAllReportsDate().getTime() > trailReports
						.getTimestamp().getTime()) {
 			refreshNeeded = true;
 		} else {
 			ISourceSpecificTrailInfo specificInfo = trailReports.get(0)
 					.getTrailInfo()
 					.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 			if (specificInfo != null) {
 				MorcSpecificTrailInfo morcInfo = (MorcSpecificTrailInfo) specificInfo;
 				lastPage = morcInfo.getLastPage();
 			} else {
 				lastPage = 1;
 			}
 		}
 
 		if (refreshNeeded) {
 			// start by clearing out the existing trail reports, if downloading
 			// first page
 			if (page <= 1)
 				trailReports.clear();
 
 			trailReports.beginTransaction();
 			try {
 				downloadTrailReports(trailReports, info, progressBar);
 				trailReports.save();
 				trailReports.endTransaction();
 			} catch (Exception e) {
 				trailReports.cancelTransaction();
 				throw (e);
 			}
 		}
 	}
 
 	void downloadTrailReports(ITrailReportList trailReports,
 			TrailInfo trailInfo, IProgressBar progressBar) throws Exception {
 		ISourceSpecificTrailInfo sourceSpecific = trailInfo
 				.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 		assert (sourceSpecific != null);
 		MorcSpecificTrailInfo morcSpecific = (MorcSpecificTrailInfo) sourceSpecific;
 
 		INetConnection netConnection = factory.getNetConnection();
 
 		try {
 			netConnection.connect(morcSpecific.getAllTrailReportUrl() + "/page"
 					+ Integer.toString(page));
 			progressBar.incrementProgress();
 			BufferedInputStream stream = new BufferedInputStream(
 					netConnection.getStream());
 			progressBar.incrementProgress();
 			MorcAllReportScanner scanner = new MorcAllReportScanner(stream,
 					factory.getTrailReportPool());
 
 			if (page == 1)
 				lastPage = scanner.findLastPage();
 
 			morcSpecific.setLastPage(lastPage);
 
 			if (scanner.findStartOfReports()) {
 				while (scanner.scanReports()) {
 					progressBar.incrementProgress();
 					TrailReport newTrailReport = scanner.getTrailReport();
 
 					newTrailReport.setTrailInfo(trailInfo);
 					newTrailReport.setSource(MorcFactory.SOURCE_NAME);
 					trailReports.add(newTrailReport);
 					factory.getTrailReportPool().deleteItem(newTrailReport);
 				}
 			}
 		} finally {
 			netConnection.disconnect();
 		}
 	}
 
 }
