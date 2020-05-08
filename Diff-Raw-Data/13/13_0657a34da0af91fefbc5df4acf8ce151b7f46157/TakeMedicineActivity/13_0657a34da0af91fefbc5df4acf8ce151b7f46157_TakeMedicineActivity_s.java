 package cis573.carecoor;
 
 import java.util.Date;
 import java.util.List;
 
import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import cis573.carecoor.bean.Medicine;
 import cis573.carecoor.bean.Schedule;
 import cis573.carecoor.bean.Schedule.Time;
 import cis573.carecoor.bean.TakeRecord;
 import cis573.carecoor.data.DataCenter;
 import cis573.carecoor.data.MedicineCenter;
 import cis573.carecoor.data.ScheduleCenter;
 import cis573.carecoor.reminder.ReminderCenter;
 import cis573.carecoor.utils.Const;
 import cis573.carecoor.utils.Utils;
 
public class TakeMedicineActivity extends Activity {
 
 	public static final String TAG = "TakeMedicineActivity";
 
 	private TextView mTvMedName;
 	private ImageView mIvMedImage;
 	private TextView mTvMedInfo1;
 	private TextView mTvMedInfo2;
 	private TextView mTvScheduleInfo;
 	private TextView mTvScheduleStatus;
 	private LinearLayout mStatusLayout;
 	private Button mBtnTake;
 	
 	private Schedule mSchedule;
 	private boolean mTaken = false;
 	private Time mNextTime = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle extras = getIntent().getExtras();
 		if(extras != null) {
 			mSchedule = (Schedule) extras.getSerializable(Const.EXTRA_SCHEDULE);
 		}
 		setContentView(R.layout.take_medicine_activity);
 		initViews();
 		
 		showMedicineInfo();
 		showScheduleInfo();
 		showScheduleStatus();
 	}
 	
 	private void initViews() {
 		mTvMedName = (TextView) findViewById(R.id.take_medicine_medname);
 		mIvMedImage = (ImageView) findViewById(R.id.take_medicine_medimage);
 		mTvMedInfo1 = (TextView) findViewById(R.id.take_medicine_medinfo1);
 		mTvMedInfo2 = (TextView) findViewById(R.id.take_medicine_medinfo2);
 		mTvScheduleInfo = (TextView) findViewById(R.id.take_medicine_schedule_info);
 		mTvScheduleStatus = (TextView) findViewById(R.id.take_medicine_schedule_status);
 		mStatusLayout = (LinearLayout) findViewById(R.id.take_medicine_status_layout);
 		mBtnTake = (Button) findViewById(R.id.take_medicine_take_btn);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if(mTaken) {
 			setResult(RESULT_OK);
 		}
 		super.onBackPressed();
 	}
 
 	private void showMedicineInfo() {
 		Medicine medicine = null;
 		if(mSchedule != null) {
 			medicine = mSchedule.getMedicine();
 		}
 		if(medicine == null) {
 			return;
 		}
 		mTvMedName.setText(medicine.getName());
 		mIvMedImage.setImageResource(MedicineCenter.getMedicineImageRes(TakeMedicineActivity.this, medicine));
 		mTvMedInfo1.setText(medicine.getDetailedName() + " - " + medicine.getCapacity());
 		mTvMedInfo2.setText(medicine.getInstructions());
 	}
 	
 	private void showScheduleInfo() {
 		if(mSchedule == null) {
 			return;
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append(getString(R.string.take_medicine_created_at,
 				Utils.getDateString(mSchedule.getCreateDate()))).append('\n');
 		
 		StringBuilder sbHours = new StringBuilder();
 //		List<Integer> hours = mSchedule.getHours();
 		List<Time> times = mSchedule.getTimes();
 		Time time;
 		for(int i = 0; i < times.size(); i++) {
 			time = times.get(i);
 			sbHours.append(Utils.get12ClockTimeString(time));
 			if(i != times.size() - 1) {
 				sbHours.append(", ");
 			}
 		}
 		sb.append(getString(R.string.take_medicine_hours_to_take, sbHours.toString())).append('\n');
 		
 		List<Integer> days = mSchedule.getDays();
 		String daysStr = "";
 		if(days == null || days.size() == 0) {
 			daysStr = getString(R.string.set_schedule_every_day);
 		} else {
 			StringBuilder sbDays = new StringBuilder();
 			int day;
 			for(int i = 0; i < days.size(); i++) {
 				day = days.get(i);
 				sbDays.append(Utils.getWeekName(day));
 				if(i != days.size() - 1) {
 					sbDays.append(", ");
 				}
 			}
 			daysStr = sbDays.toString();
 		}
 		sb.append(getString(R.string.take_medicine_days_to_take, daysStr)).append('\n');
 		
 		int duration = mSchedule.getDuration();
 		String durationStr = "";
 		if(duration > 0) {
 			durationStr = getString(R.string.duration_days, duration);
 		} else {
 			durationStr = getString(R.string.duration_continuous);
 		}
 		sb.append(getString(R.string.take_medicine_duration, durationStr));
 		
 		mTvScheduleInfo.setText(sb.toString());
 	}
 	
 	private void showScheduleStatus() {
 		mBtnTake.setEnabled(false);
 		mStatusLayout.removeAllViews();
 		int status = ScheduleCenter.getScheduleStatusSimple(TakeMedicineActivity.this, mSchedule);
 		if(status == ScheduleCenter.SCHEDULE_NO_TODAY) {
 			mTvScheduleStatus.setText(R.string.schedule_state_no_today);
 		} else if(status == ScheduleCenter.SCHEDULE_ENDED) {
 			mTvScheduleStatus.setText(R.string.schedule_state_ended);
 		} else if(status >= 0) {
 			mTvScheduleStatus.setText(R.string.take_medicine_schedule_today);
 //			List<Integer> hours = mSchedule.getHours();
 			List<Time> times = mSchedule.getTimes();
 			List<TakeRecord> records = ScheduleCenter.getDayTakeRecordsForScheduleToday(TakeMedicineActivity.this,
 					mSchedule);
 			if(records != null && records.size() < times.size()) {
 				mNextTime = times.get(records.size());
 			}
 			
 //			int hour;
 			Time time;
 			TakeRecord record;
 			View statusView;
 			TextView tvHour, tvTaken;
 			for(int i = 0; i < times.size(); i++) {
 				statusView = View.inflate(TakeMedicineActivity.this, R.layout.schedule_status_item, null);
 				tvHour = (TextView) statusView.findViewById(R.id.schedule_status_time);
 				tvTaken = (TextView) statusView.findViewById(R.id.schedule_status_taken);
 				
 				time = times.get(i);
 				tvHour.setText(Utils.get12ClockTimeString(time));
 				if(records != null) {
 					if(i < records.size()) {
 						record = records.get(i);
 						tvTaken.setTextColor(getResources().getColor(R.color.green_text));
 						tvTaken.setText(getString(R.string.taken_at,
 								Utils.getTimeString(record.getTakeTime())));
 					}
 				}
 				
 				mStatusLayout.addView(statusView);
 			}
 			
 			if(records == null || records.size() < times.size()) {
 				mBtnTake.setEnabled(true);
 			}
 		}
 	}
 
 	public void onTakeClick(View v) {
 		new AlertDialog.Builder(TakeMedicineActivity.this)
 		.setTitle(R.string.dialog_title_take_med)
 		.setMessage(R.string.dialog_msg_take_med)
 		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Date now = new Date();
 				TakeRecord record = new TakeRecord(mSchedule, now, Utils.getAdjustedDate(now, mNextTime));
 				DataCenter.addTakeRecord(TakeMedicineActivity.this, record);
 				ReminderCenter.addNextReminder(TakeMedicineActivity.this, mSchedule);
 				showScheduleStatus();
 				mTaken = true;
 			}
 		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				return;
 			}
 		}).show();
 	}
 }
