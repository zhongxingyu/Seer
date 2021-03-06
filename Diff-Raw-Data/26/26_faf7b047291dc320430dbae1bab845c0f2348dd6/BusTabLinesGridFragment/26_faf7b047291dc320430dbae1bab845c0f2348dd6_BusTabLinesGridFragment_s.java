 package org.montrealtransit.android.activity.v4;
 
 import java.util.List;
 
 import org.montrealtransit.android.BusUtils;
 import org.montrealtransit.android.MyLog;
 import org.montrealtransit.android.R;
 import org.montrealtransit.android.activity.BusLineInfo;
 import org.montrealtransit.android.api.SupportFactory;
 import org.montrealtransit.android.dialog.BusLineSelectDirection;
 import org.montrealtransit.android.provider.StmManager;
 import org.montrealtransit.android.provider.StmStore.BusLine;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.TextView;
 
 @TargetApi(4)
 public class BusTabLinesGridFragment extends Fragment {
 
 	/**
 	 * The log tag.
 	 */
 	private static final String TAG = BusTabLinesGridFragment.class.getSimpleName();
 
 	/**
 	 * @return the fragment
 	 */
 	public static Fragment newInstance() {
 		MyLog.v(TAG, "newInstance()");
 		return new BusTabLinesGridFragment();
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		MyLog.v(TAG, "onAttach()");
 		super.onAttach(activity);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		MyLog.v(TAG, "onCreate()");
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		MyLog.v(TAG, "onCreateView()");
 		return inflater.inflate(R.layout.bus_tab_bus_lines, container, false);
 	}
 
 	/**
 	 * The list of the bus lines.
 	 */
 	protected List<BusLine> busLines;
 
 	private void showAll() {
 		if (this.busLines == null) {
 			refreshBusLinesFromDB();
 		} else {
 			getView().findViewById(R.id.bus_lines).setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Refresh bus lines from database.
 	 */
 	private void refreshBusLinesFromDB() {
 		MyLog.v(TAG, "refreshBusLinesFromDB()");
 		new AsyncTask<Void, Void, List<BusLine>>() {
 			@Override
 			protected List<BusLine> doInBackground(Void... params) {
 				return StmManager.findAllBusLinesList(getActivity().getContentResolver());
 			}
 
 			@Override
 			protected void onPostExecute(List<BusLine> result) {
 				BusTabLinesGridFragment.this.busLines = result;
				GridView busLinesGrid = (GridView) getView().findViewById(R.id.bus_lines);
 				busLinesGrid.setAdapter(new BusLineArrayAdapter(BusTabLinesGridFragment.this.getActivity(), R.layout.bus_tab_bus_lines_grid_item));
 				busLinesGrid.setOnItemClickListener(new OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 						MyLog.v(TAG, "onItemClick(%s, %s,%s,%s)", parent.getId(), view.getId(), position, id);
 						if (BusTabLinesGridFragment.this.busLines != null && position < BusTabLinesGridFragment.this.busLines.size()
 								&& BusTabLinesGridFragment.this.busLines.get(position) != null) {
 							BusLine selectedLine = BusTabLinesGridFragment.this.busLines.get(position);
 							Intent intent = new Intent(BusTabLinesGridFragment.this.getActivity(), SupportFactory.getInstance(
 									BusTabLinesGridFragment.this.getActivity()).getBusLineInfoClass());
 							intent.putExtra(BusLineInfo.EXTRA_LINE_NUMBER, selectedLine.getNumber());
 							intent.putExtra(BusLineInfo.EXTRA_LINE_NAME, selectedLine.getName());
 							intent.putExtra(BusLineInfo.EXTRA_LINE_TYPE, selectedLine.getType());
 							startActivity(intent);
 						}
 					}
 				});
 				busLinesGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
 					@Override
 					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 						MyLog.v(TAG, "onItemClick(%s, %s,%s,%s)", parent.getId(), view.getId(), position, id);
 						if (BusTabLinesGridFragment.this.busLines != null && position < BusTabLinesGridFragment.this.busLines.size()
 								&& BusTabLinesGridFragment.this.busLines.get(position) != null) {
 							BusLine selectedLine = BusTabLinesGridFragment.this.busLines.get(position);
 							new BusLineSelectDirection(BusTabLinesGridFragment.this.getActivity(), selectedLine.getNumber(), selectedLine.getName(),
 									selectedLine.getType()).showDialog();
 							return true;
 						}
 						return false;
 					}
 				});
 				busLinesGrid.setVisibility(View.VISIBLE);
				getView().findViewById(R.id.bus_lines_loading).setVisibility(View.GONE);
 			}
 
 		}.execute();
 	}
 
 	/**
 	 * A custom array adapter with custom {@link BusLineArrayAdapter#getView(int, View, ViewGroup)}
 	 */
 	private class BusLineArrayAdapter extends ArrayAdapter<BusLine> {
 
 		/**
 		 * The layout inflater.
 		 */
 		private LayoutInflater layoutInflater;
 		/**
 		 * The view ID.
 		 */
 		private int viewId;
 
 		/**
 		 * The default constructor.
 		 * @param context the context
 		 * @param viewId the the view ID
 		 */
 		public BusLineArrayAdapter(Context context, int viewId) {
 			super(context, viewId);
 			this.viewId = viewId;
 			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		@Override
 		public int getCount() {
 			return BusTabLinesGridFragment.this.busLines == null ? 0 : BusTabLinesGridFragment.this.busLines.size();
 		}
 
 		@Override
 		public int getPosition(BusLine item) {
 			return BusTabLinesGridFragment.this.busLines.indexOf(item);
 		}
 
 		@Override
 		public BusLine getItem(int position) {
 			return BusTabLinesGridFragment.this.busLines.get(position);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// MyLog.v(TAG, "getView(%s)", position);
 			if (convertView == null) {
 				convertView = this.layoutInflater.inflate(this.viewId, parent, false);
 			}
 			BusLine busLine = getItem(position);
 			if (busLine != null) {
 				TextView lineNumberTv = (TextView) convertView.findViewById(R.id.line_number);
 				// bus line number
 				lineNumberTv.setText(busLine.getNumber());
 				// bus line color
 				lineNumberTv.setBackgroundColor(BusUtils.getBusLineTypeBgColor(busLine.getType(), busLine.getNumber()));
 			}
 			return convertView;
 		}
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		MyLog.v(TAG, "onActivityCreated()");
 		super.onActivityCreated(savedInstanceState);
 		showAll();
 	}
 
 	@Override
 	public void onStart() {
 		MyLog.v(TAG, "onStart()");
 		super.onStart();
 	}
 
 	@Override
 	public void onResume() {
 		MyLog.v(TAG, "onResume()");
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		MyLog.v(TAG, "onPause()");
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		MyLog.v(TAG, "onStop()");
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		MyLog.v(TAG, "onDestroy()");
 		super.onDestroy();
 	}
 
 	@Override
 	public void onDetach() {
 		MyLog.v(TAG, "onDetach()");
 		super.onDetach();
 	}
 }
