 package eu.kingcastle.loancalculator;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import eu.kingcastle.loancalculator.model.Loan;
 
 public class FragmentSchedule extends ListFragment {
 	private Loan loan;
 	private final ArrayList<String[]> data = new ArrayList<String[]>();
 	private ScheduleAdapter arrayAdapter;
 	private TextView textHeaderPayment;
 	private LinearLayout layoutNoSchedule;
 
 	public FragmentSchedule() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_schedule, container,
 				false);
 		textHeaderPayment = (TextView) view
 				.findViewById(R.id.text_header_payment);
 		layoutNoSchedule = (LinearLayout) view
 				.findViewById(R.id.layout_no_schedule);
 
 		if (savedInstanceState != null) {
 			Loan tmpLoan = savedInstanceState.getParcelable("loan");
 			if (tmpLoan != null) {
 				loan = tmpLoan;
 			}
 		}
 
 		DFragmentHelp.addClickListener(textHeaderPayment, getActivity(),
 				R.string.payment_help);
 
 		return view;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putParcelable("loan", loan);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		arrayAdapter = new ScheduleAdapter(getActivity(), data);
 		setListAdapter(arrayAdapter);
 		getListView().setTextFilterEnabled(true);
 
 		// fragment gets recreated from pager class during paging, so don't show
 		// up the loading dialog
 		if (loan != null) {
 			updateTable(loan, false);
 		} else {
 			layoutNoSchedule.setVisibility(View.VISIBLE);
 		}
 	}
 
 	// TODO: split loading dialog/calculation apart
 	public void updateTable(Loan loan, final boolean showLoading) {
 		if (loan == null) {
 			return;
 		}
 		this.loan = loan;
 
 		// might not be set (fragment is not created immediately on orientation
 		// change)
 		if (arrayAdapter == null) {
 			return;
 		}
 
 		// hide no schedule textview
 		layoutNoSchedule.setVisibility(View.GONE);
 
		new AsyncTask<Loan, Void, String[][]>() {
 			ProgressDialog progress;
 
 			@Override
 			protected void onPreExecute() {
 				progress = new ProgressDialog(getActivity());
 				progress.setMessage(getString(R.string.loading));
 				if (showLoading) {
 					progress.show();
 				}
 			}
 
 			@Override
			protected String[][] doInBackground(Loan... params) {
				return params[0].getSchedule();
 			}
 
 			@Override
			protected void onPostExecute(String[][] result) {
				// dataset changes must happen on ui thread
				data.clear();
				for (int i = 0; i < result.length; i++) {
					data.add(result[i]);
				}
 				arrayAdapter.notifyDataSetChanged();
 				if (showLoading) {
 					progress.cancel();
 					((ActivityMain) getActivity()).mViewPager
 							.setCurrentItem(ActivityMain.ITEM_ID_SCHEDULE);
 				}
 			}
 		}.execute(loan);
 
 	}
 
 	/**
 	 * Very fast adapter with custom views. Checkout
 	 * http://www.youtube.com/watch?v=wDBM6wVEO70&t=40m45s
 	 * 
 	 */
 	private static class ScheduleAdapter extends BaseAdapter {
 
 		enum ItemType {
 			NORMAL;
 		}
 
 		final Context context;
 		final ArrayList<String[]> data;
 
 		public ScheduleAdapter(Context context, ArrayList<String[]> data) {
 			this.context = context;
 			this.data = data;
 		}
 
 		@Override
 		public int getItemViewType(int position) {
 			return ItemType.NORMAL.ordinal();
 		}
 
 		@Override
 		public int getViewTypeCount() {
 			return ItemType.values().length;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 
 			final String[] itemData = data.get(position);
			if (itemData == null) {
				return null;
			}
 			final ItemType type = ItemType.values()[getItemViewType(position)];
 
 			// create new view if null or type changed
 			if (convertView == null
 					|| ((ViewHolder) convertView.getTag()).type != type) {
 				int res;
 				switch (type) {
 				case NORMAL:
 					res = R.layout.schedule_row_item;
 					break;
 				default:
 					throw new IllegalArgumentException();
 				}
 
 				convertView = ((Activity) context).getLayoutInflater().inflate(
 						res, parent, false);
 				holder = new ViewHolder();
 				holder.textPeriod = (TextView) convertView
 						.findViewById(R.id.textPeriod);
 				// holder.textRepayment = (TextView)
 				// convertView.findViewById(R.id.textRepayment);
 				holder.textInterest = (TextView) convertView
 						.findViewById(R.id.textInterest);
 				holder.textPayment = (TextView) convertView
 						.findViewById(R.id.textPayment);
 				holder.textLoanLeft = (TextView) convertView
 						.findViewById(R.id.textLoanLeft);
 				// holder.textMisc = (TextView)
 				// convertView.findViewById(R.id.textMisc);
 				holder.type = type;
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			// set text in holder
 			holder.textPeriod.setText(itemData[0]);
 			// holder.textRepayment.setText(itemData[1]);
 			holder.textInterest.setText(itemData[2]);
 			holder.textPayment.setText(itemData[3]);
 			holder.textLoanLeft.setText(itemData[4]);
 			// holder.textMisc.setText(itemData[5]);
 
 			return convertView;
 		}
 
 		// fast references, so no need for slow findViewById
 		static class ViewHolder {
 			TextView textPeriod;
 			// TextView textRepayment;
 			TextView textInterest;
 			TextView textPayment;
 			TextView textLoanLeft;
 			// TextView textMisc;
 			ItemType type;
 		}
 
 		@Override
 		public int getCount() {
 			return data.size();
 		}
 
 		@Override
 		public String[] getItem(int position) {
 			return data.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 	}
 }
