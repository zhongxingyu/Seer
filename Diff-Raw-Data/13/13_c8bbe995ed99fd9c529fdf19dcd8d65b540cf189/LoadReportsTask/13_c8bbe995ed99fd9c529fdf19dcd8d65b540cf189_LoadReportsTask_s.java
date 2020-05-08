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
 package org.dsanderson.xctrailreport.core.android;
 
 import org.dsanderson.android.util.Dialog;
 import org.dsanderson.util.IProgressBar;
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.IReportListCreator;
 import org.dsanderson.xctrailreport.core.ITrailInfoList;
 import org.dsanderson.xctrailreport.core.ITrailReportList;
 
 import android.app.ListActivity;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 
 /**
  * 
  */
 public class LoadReportsTask extends AsyncTask<Integer, Integer, Integer> {
 
 	private final IAbstractFactory factory;
 	private final ITrailReportList trailReports;
 	private final ITrailInfoList trailInfos;
 	private final IReportListCreator listCreator;
 	private final ListActivity context;
 	private final IProgressBar progressBar;
 	private final IPrinter printer;
 	Exception e = null;
 
 	/**
 * 
 */
 	public LoadReportsTask(ListActivity context, IAbstractFactory factory,
 			IReportListCreator listCreator, ITrailReportList trailReports,
 			ITrailInfoList trailInfos, IPrinter printer,
 			IProgressBar progressBar) {
 		this.factory = factory;
 		this.listCreator = listCreator;
 		this.trailReports = trailReports;
 		this.trailInfos = trailInfos;
 		this.context = context;
 		this.printer = printer;
 		this.progressBar = progressBar;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		LockScreenRotation();
 		progressBar.setMessage("Loading trail reports...");
 		progressBar.show();
 		e = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.os.AsyncTask#doInBackground(Params[])
 	 */
 	@Override
 	protected Integer doInBackground(Integer... params) {
 		Integer size = null;
 		try {
 			loadTrailReports();
 			size = trailReports.size();
 			if (size == 0)
 				throw new Exception("No reports found.");
 		} catch (Exception e) {
 			this.e = e;
 		}
 		return size;
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 		progressBar.close();
 
 		if (e == null) {
 			try {
 				printer.printTrailReports();
 			} catch (Exception e) {
 				this.e = e;
 			}
 		}
 
 		if (e != null) {
 			e.printStackTrace();
 			Dialog dialog = new Dialog(context, e);
 			dialog.show();
 		}
 		UnlockScreenRotation();
 	}
 
 	private void loadTrailReports() throws Exception {
 
 		try {
 			listCreator.getTrailReports(trailReports, trailInfos, factory
 					.getUserSettings().getForcedRefresh(), progressBar);
 		} catch (Exception e) {
 			e.printStackTrace();
			trailReports.cancelTransaction();
 			trailReports.clear();
 			throw e;
 		} finally {
 			factory.getUserSettings().setForcedRefresh(false);
 		}
 	}
 
 	// Sets screen rotation as fixed to current rotation setting
 	private void LockScreenRotation() {
 		// Stop the screen orientation changing during an event
 		switch (context.getResources().getConfiguration().orientation) {
 		case Configuration.ORIENTATION_PORTRAIT:
 			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 			break;
 		case Configuration.ORIENTATION_LANDSCAPE:
 			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			break;
 		}
 	}
 
 	private void UnlockScreenRotation() {
 		// allow screen rotations again
 		context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 	}
 
 }
