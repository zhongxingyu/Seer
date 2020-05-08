 package org.dsanderson.morctrailreport;
 
 import java.util.Date;
 import java.util.List;
 
 import org.dsanderson.morctrailreport.R;
 import org.dsanderson.morctrailreport.parser.MorcAllReportListCreator;
 import org.dsanderson.morctrailreport.parser.MorcFactory;
 import org.dsanderson.morctrailreport.parser.MorcSpecificTrailInfo;
 import org.dsanderson.morctrailreport.parser.SingleTrailInfoList;
 
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.ISourceSpecificTrailInfo;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 import org.dsanderson.xctrailreport.core.android.IAbstractListEntryFactory;
 import org.dsanderson.xctrailreport.core.android.LoadReportsTask;
 import org.dsanderson.xctrailreport.core.android.TrailReportList;
 import org.dsanderson.xctrailreport.core.android.TrailReportPrinter;
 import org.dsanderson.xctrailreport.decorators.ITrailReportListEntry;
 import org.dsanderson.android.util.AndroidIntent;
 import org.dsanderson.android.util.AndroidProgressBar;
 import org.dsanderson.android.util.Dialog;
 import org.dsanderson.android.util.Maps;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 
 public class AllReportActivity extends ListActivity {
 
 	private final String databaseName = "all_reports_database";
 
 	private TrailReportList trailReports = null;
 	private TrailReportFactory factory = TrailReportFactory.getInstance();
 	MorcAllReportListCreator listCreator = new MorcAllReportListCreator(factory);
 	private AllTrailReportPrinter printer;
 	String appName;
 	private TrailInfo info;
 	MorcSpecificTrailInfo morcInfo;
 	boolean redraw = true;
 	private AllTrailReportAdapter adapter;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		appName = getString(R.string.app_name);
 
 		if (trailReports == null) {
 			trailReports = new TrailReportList(this,
 					factory.getTrailReportDatabaseFactory(), databaseName,
 					Integer.parseInt(getString(R.integer.databaseVersion)));
 			try {
 				trailReports.open();
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 		}
 
		TrailInfo allReportsInfo = MorcFactory.getInstance()
				.getAllReportsInfo();
		
		if (allReportsInfo != null)
			info = allReportsInfo;
		else if (trailReports.size() > 0)
 			info = trailReports.get(0).getTrailInfo();
 
 		printer = new AllTrailReportPrinter(this, factory, trailReports,
 				appName, ListEntryFactory.getInstance());
 
 		try {
 			factory.getUserSettingsSource().loadUserSettings();
 		} catch (Exception e) {
 			System.err.println(e);
 			factory.newDialog(e).show();
 		}
 
 		if (savedInstanceState == null) {
 			redraw = true;
 			refresh(false, 1);
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.all_reports_menu, menu);
 		// TODO need to manually inflate this menu, so that I don't add trail
 		// info if not available
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.allReportRefresh:
 			redraw = true;
 			refresh(true, 1);
 			return true;
 		case R.id.allReportCompose: {
 			String composeUrl = morcInfo.getComposeUrl();
 			if (composeUrl != null && composeUrl.length() > 0)
 				AndroidIntent.launchIntent(composeUrl, this);
 		}
 			return true;
 		case R.id.allReportsMap:
 			Maps.launchMap(info.getLocation(), info.getName(),
 					info.getSpecificLocation(), this);
 			return true;
 		case R.id.allReportsTrailInfo:
 			if (morcInfo != null) {
 				String trailInfoUrl = morcInfo.getTrailInfoUrl();
 				if (trailInfoUrl != null && trailInfoUrl.length() > 0)
 					AndroidIntent.launchIntent(trailInfoUrl, this);
 			}
 			return true;
 		case R.id.openInBrowser:
 			if (morcInfo != null) {
 				String allReportUrl = morcInfo.getAllTrailReportUrl();
 				if (allReportUrl != null && allReportUrl.length() > 0)
 					AndroidIntent.launchIntent(allReportUrl, this);
 			}
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus) {
 			try {
 				printer.printTrailReports();
 			} catch (Exception e) {
 				e.printStackTrace();
 				Dialog dialog = new Dialog(this, e);
 				dialog.show();
 			}
 		}
 	}
 
 	private void refresh(boolean forced, int page) {
 		factory.getUserSettings().setForcedRefresh(forced);
 
 		factory.getLocationSource().updateLocation();
 
 		morcInfo = (MorcSpecificTrailInfo) info
 				.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 		listCreator.setPage(page);
 
 		SingleTrailInfoList trailInfos = new SingleTrailInfoList();
 		trailInfos.add(info);
 
 		new LoadReportsTask(this, factory, listCreator, trailReports,
 				trailInfos, new AndroidProgressBar(this)).execute();
 	}
 
 	private class AllTrailReportPrinter extends TrailReportPrinter {
 
 		private final ListActivity context;
 		private final IAbstractFactory factory;
 		private final TrailReportList trailReports;
 		private final IAbstractListEntryFactory listEntryFactory;
 
 		public AllTrailReportPrinter(ListActivity context,
 				IAbstractFactory factory, TrailReportList trailReports,
 				String appName, IAbstractListEntryFactory listEntryFactory) {
 			super(context, factory, trailReports, appName, listEntryFactory);
 			this.context = context;
 			this.factory = factory;
 
 			this.trailReports = trailReports;
 			this.listEntryFactory = listEntryFactory;
 
 		}
 
 		public void printTrailReports() throws Exception {
 			Date lastRefreshDate = trailReports.getTimestamp();
 			String titleString = appName;
 			if (info != null)
 				titleString = info.getName();
 
 			if (lastRefreshDate != null && lastRefreshDate.getTime() != 0) {
 				Time time = new Time();
 				time.set(lastRefreshDate.getTime());
 				titleString += time.format(" (%b %e, %r)");
 			}
 			context.setTitle(titleString);
 
 			factory.filterReports(trailReports);
 			factory.sortReports(trailReports);
 
 			Cursor cursor = ((TrailReportList) trailReports).getCursor();
 			if (redraw || (adapter == null)) {
 				adapter = new AllTrailReportAdapter(context, cursor, factory,
 						trailReports, listEntryFactory);
 				context.setListAdapter(adapter);
 			} else {
 				adapter.changeCursor(cursor);
 			}
 		}
 
 	}
 
 	private class AllTrailReportAdapter extends CursorAdapter {
 
 		private final IAbstractFactory factory;
 		private final TrailReportList trailReports;
 		private final IAbstractListEntryFactory listEntryFactory;
 
 		/**
 		 * @param context
 		 * @param textViewResourceId
 		 * @param objects
 		 */
 		public AllTrailReportAdapter(Context context, Cursor cursor,
 				IAbstractFactory factory, TrailReportList trailReports,
 				IAbstractListEntryFactory listEntryFactory) {
 			super(context, cursor);
 			this.factory = factory;
 			this.trailReports = trailReports;
 			this.listEntryFactory = listEntryFactory;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.widget.CursorAdapter#newView(android.content.Context,
 		 * android.database.Cursor, android.view.ViewGroup)
 		 */
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			return listEntryFactory.inflate(context, parent);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.widget.CursorAdapter#bindView(android.view.View,
 		 * android.content.Context, android.database.Cursor)
 		 */
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			TrailReport currentReport = (TrailReport) trailReports.get(cursor);
 
 			if (currentReport != null) {
 				ITrailReportListEntry listEntry = listEntryFactory
 						.newListEntry(view);
 
 				boolean newTrail = false;
 				boolean last = cursor.isLast();
 				if (cursor.moveToPrevious()) {
 					TrailReport previousReport = (TrailReport) trailReports
 							.get(cursor);
 
 					if (previousReport.getTrailInfo().getName()
 							.compareTo(currentReport.getTrailInfo().getName()) != 0) {
 						newTrail = true;
 					}
 					TrailInfo previousInfo = previousReport.getTrailInfo();
 					for (ISourceSpecificTrailInfo specificInfo : previousInfo
 							.getSourceSpecificInfos()) {
 						specificInfo.deleteItem();
 					}
 					factory.getTrailInfoPool().deleteItem(previousInfo);
 					factory.getTrailReportPool().deleteItem(previousReport);
 				} else {
 					newTrail = true;
 				}
 
 				listEntry.setTitleGroupVisible(newTrail);
 
 				if (newTrail)
 					factory.getTrailInfoDecorators().decorate(currentReport,
 							listEntry);
 
 				factory.getTrailReportDecorators().decorate(currentReport,
 						listEntry);
 
 				listEntry.draw();
 
 				TrailInfo currentInfo = currentReport.getTrailInfo();
 				for (ISourceSpecificTrailInfo specificInfo : currentInfo
 						.getSourceSpecificInfos()) {
 					specificInfo.deleteItem();
 				}
 				factory.getTrailInfoPool().deleteItem(currentInfo);
 				factory.getTrailReportPool().deleteItem(currentReport);
 
 				// if on last entry and we have another page
 				if (last && (listCreator.getPage() < listCreator.getLastPage())) {
 					redraw = false;
 					refresh(true, listCreator.getPage() + 1);
 				}
 			}
 		}
 
 	}
 
 }
