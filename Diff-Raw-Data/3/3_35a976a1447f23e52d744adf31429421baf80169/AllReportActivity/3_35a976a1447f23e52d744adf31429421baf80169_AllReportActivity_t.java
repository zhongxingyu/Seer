 package org.dsanderson.morctrailreport;
 
 import java.util.Date;
 import java.util.List;
 
 import org.dsanderson.android.util.ListEntry;
 import org.dsanderson.morctrailreport.R;
 import org.dsanderson.morctrailreport.parser.MorcAllReportListCreator;
 import org.dsanderson.morctrailreport.parser.MorcFactory;
 import org.dsanderson.morctrailreport.parser.MorcSpecificTrailInfo;
 import org.dsanderson.morctrailreport.parser.SingleTrailInfoList;
 
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.ISourceSpecificTrailInfo;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.TrailReport;
 import org.dsanderson.xctrailreport.core.android.LoadReportsTask;
 import org.dsanderson.xctrailreport.core.android.TrailReportList;
 import org.dsanderson.xctrailreport.core.android.TrailReportPrinter;
 import org.dsanderson.android.util.AndroidIntent;
 import org.dsanderson.android.util.Maps;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 
 public class AllReportActivity extends ListActivity {
 
 	private final String databaseName = "all_reports_database";
 
 	private TrailReportList trailReports = null;
 	private SingleTrailInfoList trailInfos = null;
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
 
 		if (trailInfos == null)
 			trailInfos = new SingleTrailInfoList();
 
 		info = trailInfos.get(0);
 
 		printer = new AllTrailReportPrinter(this, factory, trailReports,
 				appName, R.layout.row);
 
 		try {
 			factory.getUserSettingsSource().loadUserSettings();
 
 			@SuppressWarnings("unchecked")
 			final List<TrailReport> savedTrailReports = (List<TrailReport>) getLastNonConfigurationInstance();
 			if (savedTrailReports == null) {
 				redraw = true;
 				refresh(false, 1);
 			}
 
 		} catch (Exception e) {
 			System.err.println(e);
 			factory.newDialog(e).show();
 		}
 
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
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
 			Maps.launchMap(info.getLocation(), info.getName(), info.getSpecificLocation(), this);
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
 		try {
 			if (hasFocus && trailReports != null
 					&& factory.getUserSettings().getRedrawNeeded())
 				printer.printTrailReports();
 		} catch (Exception e) {
 			e.printStackTrace();
 			factory.newDialog(e);
 		}
 	}
 
 	private void refresh(boolean forced, int page) {
 		factory.getUserSettings().setForcedRefresh(forced);
 
 		if (factory.getUserSettings().getLocationEnabled())
 			factory.getLocationSource().updateLocation();
 		else
 			factory.getLocationSource().setLocation(
 					factory.getUserSettings().getDefaultLocation());
 
 		info = MorcFactory.getInstance().getAllReportsInfo();
 		morcInfo = (MorcSpecificTrailInfo) info
 				.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 		trailInfos.add(info);
 		listCreator.setPage(page);
 		new LoadReportsTask(this, factory, listCreator, printer, trailReports,
 				trailInfos).execute();
 	}
 
 	private class AllTrailReportPrinter extends TrailReportPrinter {
 
 		private final ListActivity context;
 		private final IAbstractFactory factory;
 		private final TrailReportList trailReports;
 		private final int rowId;
 
 		public AllTrailReportPrinter(ListActivity context,
 				IAbstractFactory factory, TrailReportList trailReports,
 				String appName, int rowId) {
 			super(context, factory, trailReports, appName, rowId);
 			this.context = context;
 			this.factory = factory;
 			this.rowId = rowId;
 
 			this.trailReports = trailReports;
 		}
 
 		public void printTrailReports() throws Exception {
 			Date lastRefreshDate = trailReports.getTimestamp();
 			String titleString = info.getName();
 			if (lastRefreshDate != null && lastRefreshDate.getTime() != 0) {
 				Time time = new Time();
 				time.set(lastRefreshDate.getTime());
 				titleString += time.format(" (%b %e, %r)");
 			}
 			context.setTitle(titleString);
 
 			trailReports.filter(factory.getUserSettings());
 			trailReports.sort(factory.getUserSettings());
 
 			Cursor cursor = ((TrailReportList) trailReports).getCursor();
 			if (redraw || (adapter == null)) {
 				adapter = new AllTrailReportAdapter(context, rowId, cursor,
 						factory, trailReports);
 				context.setListAdapter(adapter);
 			} else {
 				adapter.changeCursor(cursor);
 			}
 
 			factory.getUserSettings().setRedrawNeeded(false);
 		}
 
 	}
 
 	private class AllTrailReportAdapter extends CursorAdapter {
 
 		private final IAbstractFactory factory;
 		private final TrailReportList trailReports;
 		private final int textViewResourceId;
 
 		/**
 		 * @param context
 		 * @param textViewResourceId
 		 * @param objects
 		 */
 		public AllTrailReportAdapter(Context context, int textViewResourceId,
 				Cursor cursor, IAbstractFactory factory,
 				TrailReportList trailReports) {
 			super(context, cursor);
 			this.factory = factory;
 			this.trailReports = trailReports;
 			this.textViewResourceId = textViewResourceId;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.widget.CursorAdapter#newView(android.content.Context,
 		 * android.database.Cursor, android.view.ViewGroup)
 		 */
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 
 			final LayoutInflater inflater = LayoutInflater.from(context);
 			View layout = inflater.inflate(textViewResourceId, parent, false);
 
 			return layout;
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
 				ListEntry listEntry = new ListEntry((LinearLayout) view,
 						context);
 
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
