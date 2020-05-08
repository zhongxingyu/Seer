 package net.qlun.celllogger.fragment;
 
 import net.qlun.celllogger.R;
 import net.qlun.celllogger.Station;
 import net.qlun.celllogger.app.PhoneStateService;
 import net.qlun.celllogger.app.PhoneStateService.CurrentCellInfo;
 import net.qlun.celllogger.provider.CellLocationLog;
 import net.qlun.celllogger.util.QualityUtil;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RecordFragment extends Fragment implements OnClickListener {
 
 	PhoneStateService mService = null;
 
 	boolean mBound;
 
 	private static final int MAX_SAMPLES = 60;
 	protected static final long CHART_UPDATE_INTERVAL = 1000;
 
 	protected static final String TAG = "CL-Record";
 	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
 	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
 	private XYSeries mCurrentSeries;
 	private XYSeriesRenderer mCurrentRenderer;
 
 	private GraphicalView mChartView;
 
 	private Handler mHandler = new Handler();
 
 	private int index = 0;
 
 	private PhoneStateService.CurrentCellInfo previousCell = null;
 	private PhoneStateService.CurrentCellInfo currentCell = null;
 
 	private long time_switch = -1;
 
 	private PowerManager.WakeLock wakeLock = null;
 
 	private String last_save = null;
 
 	private Runnable mChartUpdateTask = new Runnable() {
 		public void run() {
 			// Log.v(TAG, "chart tick");
 			if (mBound) {
 
 				PhoneStateService.CurrentCellInfo ci = mService
 						.getCurrentCellInfo();
 
 				signalQualityPercent = QualityUtil
 						.getDbmQuality(ci.signalStrength);
 
 				if (!ci.equals(currentCell)) {
 					// switch cell
 					previousCell = null;
 					previousCell = currentCell;
 					currentCell = (CurrentCellInfo) ci.clone();
 					time_switch = System.currentTimeMillis();
 				}
 
 			} else {
 				signalQualityPercent = 0;
 			}
 
 			drawQuality();
 
 			showStateTexts();
 
 			mHandler.postDelayed(this, CHART_UPDATE_INTERVAL);
 
 		}
 	};
 
 	protected double signalQualityPercent;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.v(TAG, "create");
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		Log.v(TAG, "a createView");
 
 		mRenderer.setApplyBackgroundColor(true);
 		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
 		mRenderer.setAxisTitleTextSize(16);
 		mRenderer.setChartTitleTextSize(20);
 		mRenderer.setLabelsTextSize(15);
 		mRenderer.setLegendTextSize(15);
 		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
 		mRenderer.setZoomButtonsVisible(true);
 		// mRenderer.setPointSize(2);
 
 		mRenderer.setXAxisMax(MAX_SAMPLES);
 
 		mRenderer.setYAxisMin(0);
 		mRenderer.setYAxisMax(100);
 		mRenderer.setShowGrid(true);
 		{
 			XYSeries series = new XYSeries("test1");
 			mDataset.addSeries(series);
 			mCurrentSeries = series;
 			XYSeriesRenderer renderer = new XYSeriesRenderer();
 			mRenderer.addSeriesRenderer(renderer);
 
 			renderer.setFillPoints(true);
 			// renderer.setFillBelowLine(true); // achartengine3 bug
 			renderer.setLineWidth(2.0f);
 
 			mCurrentRenderer = renderer;
 		}
 
 		mHandler.postDelayed(mChartUpdateTask, CHART_UPDATE_INTERVAL);
 
 		View view = inflater.inflate(R.layout.record, container, false);
 
 		{
 			Button btn = (Button) view.findViewById(R.id.button_record);
 			btn.setOnClickListener(this);
 		}
 
 		return view;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.v(TAG, "start");
 		Intent intent = new Intent(getActivity(), PhoneStateService.class);
 		// System.out.println(mConnection);
 		getActivity().getApplicationContext().bindService(intent, mConnection,
 				Context.BIND_AUTO_CREATE);
 
 	}
 
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.v(TAG, "resume");
 
 		if (mChartView == null) {
 			LinearLayout layout = (LinearLayout) getActivity().findViewById(
 					R.id.chart);
 
 			mChartView = ChartFactory.getCubeLineChartView(getActivity(),
 					mDataset, mRenderer, 0.33f);
 
 			mRenderer.setClickEnabled(false);
 			mRenderer.setSelectableBuffer(100);
 			mRenderer.setPanEnabled(false, false);
 			mRenderer.setZoomEnabled(false, false);
 			mRenderer.setZoomButtonsVisible(false);
 			mRenderer.setShowLegend(false);
 			// mRenderer.setShowLabels(false);
 			mRenderer.setXLabels(0);
 
 			layout.addView(mChartView, new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 
 		} else {
 			mChartView.repaint();
 		}
 
 		PowerManager pm = (PowerManager) getActivity().getSystemService(
 				Context.POWER_SERVICE);
 		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
 		wakeLock.acquire();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.v(TAG, "pause");
 		wakeLock.release();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Log.v(TAG, "stop");
 		if (mBound) {
 			try {
 				getActivity().getApplicationContext()
 						.unbindService(mConnection);
 			} catch (IllegalArgumentException iae) {
 
 			}
 			mBound = false;
 		}
 		
 	}
 	
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 		Log.v(TAG, "destroyview");
 		
		mDataset.removeSeries(mCurrentSeries);
		mRenderer.removeSeriesRenderer(mCurrentRenderer);
 		
 		mChartView = null;
 		mCurrentRenderer = null;
 		mCurrentSeries = null;
 		
 		mHandler.removeCallbacks(mChartUpdateTask);
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.v(TAG, "destroy");
 
 
 	}
 
 	private void drawQuality() {
 
 		// fixme 会不会那啥
 		
 		int total = mCurrentSeries.getItemCount();
 		if (total >= MAX_SAMPLES) {
 
 			mCurrentSeries.remove(0);
 			mRenderer.setXAxisMax(index);
 		}
 
 		double x = index;
 		double y = signalQualityPercent;
 		mCurrentSeries.add(x, y);
 		if (mChartView != null) {
 			mChartView.repaint();
 		}
 
 		index++;
 
 	}
 
 	private void showStateTexts() {
 
 		if (currentCell != null) {
 			{
 				TextView ti = (TextView) getActivity().findViewById(
 						R.id.current_cid);
 				if (ti != null) {
 					ti.setText("" + currentCell.cid);
 				}
 			}
 			{
 				TextView ti = (TextView) getActivity().findViewById(
 						R.id.current_lac);
 				if (ti != null) {
 					ti.setText("" + currentCell.lac);
 				}
 			}
 		}
 
 		if (previousCell != null) {
 			{
 				TextView ti = (TextView) getActivity().findViewById(
 						R.id.previous_cid);
 				if (ti != null) {
 					ti.setText("" + previousCell.cid);
 				}
 			}
 			{
 				TextView ti = (TextView) getActivity().findViewById(
 						R.id.previous_lac);
 				if (ti != null) {
 					ti.setText("" + previousCell.lac);
 				}
 			}
 		}
 
 		if (time_switch > 0) {
 
 			long tc = (System.currentTimeMillis() - time_switch) / 1000;
 			long hours = tc / 3600;
 			long minutes = (tc % 3600) / 60;
 			long seconds = tc % 60;
 
 			String timeString = String.format("%02d:%02d:%02d", hours, minutes,
 					seconds);
 
 			TextView ti = (TextView) getActivity().findViewById(
 					R.id.time_change);
 			if (ti != null) {
 				ti.setText(timeString);
 			}
 		}
 		if (last_save != null) {
 			TextView ti = (TextView) getActivity().findViewById(
 					R.id.last_record);
 			if (ti != null) {
 				ti.setText(last_save);
 			}
 		}
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			Log.v(TAG, "service connected.");
 			PhoneStateService.LocalBinder binder = (PhoneStateService.LocalBinder) service;
 			mService = binder.getService();
 			mBound = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			Log.v(TAG, "service disconnected.");
 			mBound = false;
 		}
 
 	};
 
 	private void saveStation(String station_id,
 			PhoneStateService.CurrentCellInfo ci, long tm) {
 
 		Log.v(TAG, "save " + station_id + ", " + ci);
 
 		ContentValues values = new ContentValues();
 		values.put(CellLocationLog.NETWORK_TYPE, ci.networkType);
 		values.put(CellLocationLog.CID, ci.cid);
 		values.put(CellLocationLog.LAC, ci.lac);
 		values.put(CellLocationLog.SIGNAL_STRENGTH, ci.signalStrength);
 		values.put(CellLocationLog.STATION_ID, station_id);
 		values.put(CellLocationLog.TIME, tm);
 		getActivity().getContentResolver().insert(CellLocationLog.CONTENT_URI,
 				values);
 
 		last_save = station_id + ", " + ci.cid;
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.button_record:
 			Log.v(TAG, "save button click.");
 			{
 
 				final CurrentCellInfo stopCi = (CurrentCellInfo) currentCell
 						.clone();
 				final long tm = System.currentTimeMillis();
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						getActivity());
 				builder.setTitle("Pick a station");
 				final CharSequence[] items = Station.getInstance(getActivity())
 						.getAllItems();
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 
 						String station_id = items[item].toString();
 
 						saveStation(station_id, stopCi, tm);
 
 						Toast.makeText(getActivity().getApplicationContext(),
 								station_id, Toast.LENGTH_SHORT).show();
 					}
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 
 			}
 			break;
 		}
 
 	}
 }
