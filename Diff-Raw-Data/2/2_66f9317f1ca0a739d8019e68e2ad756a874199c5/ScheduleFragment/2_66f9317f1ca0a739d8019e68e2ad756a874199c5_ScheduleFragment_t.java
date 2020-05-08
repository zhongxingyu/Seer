 package jp.knct.di.c6t.ui.schedule;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.util.ActivityUtil;
 import jp.knct.di.c6t.util.TimeUtil;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 
 public class ScheduleFragment extends Fragment implements OnClickListener {
 
 	private Date mCurrentDate;
 	private List<Exploration> mExplorations;
 	private ViewGroup mScheduleRoot;
 	private TableLayout mScheduleContent;
 	private TableRow[] mSections = new TableRow[24];
 	private OnClickListener mListener;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		mScheduleRoot = (ViewGroup) inflater.inflate(R.layout.fragment_schedule, container);
 		mScheduleContent = (TableLayout) mScheduleRoot.findViewById(R.id.schedule_content);
 		for (int i = 0; i < 24; i++) {
 			mSections[i] = (TableRow) mScheduleContent.getChildAt(i);
 			ActivityUtil.setText(mSections[i], R.id.table_row_schedule_section_label, i + "");
 		}
 		return mScheduleRoot;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
 
 		setDate(new Date());
 
 		ActivityUtil.setOnClickListener(mScheduleRoot, this, new int[] {
 				R.id.schedule_back,
 				R.id.schedule_next,
 		});
 	}
 
 	public void setExplorations(List<Exploration> explorations) {
 		mExplorations = explorations;
 
 		if (mExplorations != null && mCurrentDate != null) {
 			updateScheduleView();
 		}
 	}
 
 	public void setDate(Date date) {
 		mCurrentDate = date;
 		ActivityUtil.setText(getActivity(),
 				R.id.schedule_current_date,
 				TimeUtil.formatOnlyDate(mCurrentDate));
 
 		if (mExplorations != null && mCurrentDate != null) {
 			updateScheduleView();
 		}
 	}
 
 	private void updateScheduleView() {
 		removeAllExplorations();
 		if (mExplorations == null) {
 			return;
 		}
 
 		for (Exploration exploration : mExplorations) {
 			if (TimeUtil.isSameDay(exploration.getStartTime(), mCurrentDate)) {
 				putExploration(exploration);
 			}
 		}
 	}
 
 	private void removeAllExplorations() {
 		for (TableRow section : mSections) {
 			((LinearLayout) section.findViewById(R.id.table_row_schedule_section_explorations))
 					.removeAllViews();
 		}
 	}
 
 	private void putExploration(Exploration exploration) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(exploration.getStartTime());
 		int hour = cal.get(Calendar.HOUR_OF_DAY);
 		LinearLayout container = (LinearLayout) mSections[hour]
 				.findViewById(R.id.table_row_schedule_section_explorations);
 
 		int minute = cal.get(Calendar.MINUTE);
 
 		ExplorationPin pin = (ExplorationPin) getActivity().getLayoutInflater()
 				.inflate(R.layout.item_scheduled_exploration, null);
 		pin.setExploration(exploration);
 		pin.setOnClickListener(mListener);
 
 		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20, 20);
 		int marginTop = (int) ((50 - 20) * (minute / 60.0)); // 50 = height of container
 		lp.setMargins(10, marginTop, 0, 0);
 
 		container.addView(pin, lp);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.schedule_back:
 			setDate(TimeUtil.getPreviousDate(mCurrentDate));
 			break;
 
 		case R.id.schedule_next:
 			setDate(TimeUtil.getNextDate(mCurrentDate));
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	public void setOnExplorationPinClickListener(OnClickListener listener) {
 		mListener = listener;
 	}
 }
