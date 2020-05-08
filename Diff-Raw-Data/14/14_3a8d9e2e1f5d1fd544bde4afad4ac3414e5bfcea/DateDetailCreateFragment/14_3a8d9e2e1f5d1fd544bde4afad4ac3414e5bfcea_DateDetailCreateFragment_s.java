 package com.time.master.fragment.date;
 
 
 
 import java.util.Date;
 import java.util.HashMap;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.Date;
 import java.util.HashMap;
 import com.time.master.R;
 import com.time.master.activity.FrameActivity;
 import com.time.master.TimeMasterApplication;
 import com.time.master.dialog.DateTimeWarningDialogFragment;
 import com.time.master.dialog.DurationTimeDialogFragment;
 import com.time.master.dialog.HumanDialogFragment;
 import com.time.master.dialog.LocationDialogFragment;
 import com.time.master.dialog.RepeatDialogFragment;
 import com.time.master.dialog.TimeDialogFragment;
 import com.time.master.dialog.WheelDialogFragment;
 import com.time.master.interfacer.WheelResultInterface;
 import com.time.master.model.CacheModel;
 import com.time.master.tool.ChineseCalendar;
 import com.time.master.view.BasicEditText;
 import com.time.master.view.BasicTextView;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.text.InputType;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.ForegroundColorSpan;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 import com.time.master.R;
 import com.time.master.TimeMasterApplication;
 import com.time.master.dialog.DurationTimeDialogFragment;
 import com.time.master.dialog.HumanDialogFragment;
 import com.time.master.dialog.LocationDialogFragment;
 import com.time.master.dialog.RepeatDialogFragment;
 import com.time.master.dialog.TimeDialogFragment;
 import com.time.master.dialog.WheelDialogFragment;
 import com.time.master.interfacer.WheelResultInterface;
 import com.time.master.model.CacheModel;
 import com.time.master.tool.ChineseCalendar;
 import com.time.master.view.BasicEditText;
 import com.time.master.view.BasicTextView;
 
 /**
  * ---ѡ
  * 
  * @author Desmond
  * 
  */
 
 public class DateDetailCreateFragment extends Fragment implements
 		OnTouchListener, android.view.View.OnClickListener {
 	WheelDialogFragment dateFragment, locationFragment, humanFragment,planTimePeroidFragment;
 
 	DialogFragment repeatFragment,timewarningFragment;
 	BasicEditText startDateSelector,//ʼʱ
 	              locationSelector,
 	              humanSelector,
 	              planPeroidSelector,//ʱ01Сʱ0
 	              lengthSelector,
 	              endDateSelector;//ʱ
 
 	BasicTextView startClick,//ʼť
 	                tvdate, //  / ť
 			        tvduration,// ռ/ڼ ť
 			        dateRepeat,//ظť
                     dateWarning,//Ѱť
                     plan_previou,// ǰť
 					plan_type;// ఴť
 					
 	private ChineseCalendar startChineseDate,//ʼʱ
 	                        endChineseDate,
 	                        durationChinsesDate;//ʱ
 	
 	private Handler countTimeHandler;//ʱhandler
 	private Runnable mTicker;//ʱrunnable
 
 	
 	HashMap<Integer, Boolean> viewStatus = new HashMap<Integer, Boolean>();
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 
 		View layout = inflater.inflate(R.layout.date_detail_create_page,container, false);
 
 
 		startDateSelector = (BasicEditText) layout.findViewById(R.id.plan_time_start);
 		startDateSelector.setInputType(InputType.TYPE_NULL);
 		startDateSelector.setOnTouchListener(this);
 
 
 		locationSelector = (BasicEditText) layout
 				.findViewById(R.id.plan_location);
         locationSelector.setInputType(InputType.TYPE_NULL);
 		locationSelector.setOnTouchListener(this);
 
 		humanSelector = (BasicEditText) layout.findViewById(R.id.plan_human);
 		humanSelector.setInputType(InputType.TYPE_NULL);
 		humanSelector.setOnTouchListener(this);
 		
 
 /**
  * @author
  */
 		lengthSelector = (BasicEditText) layout.findViewById(R.id.plan_length);
 
 		endDateSelector = (BasicEditText) layout.findViewById(R.id.plan_time_end);
 		endDateSelector.setInputType(InputType.TYPE_NULL);
 		endDateSelector.setOnTouchListener(this);
 		
 		startClick = (BasicTextView) layout.findViewById(R.id.plan_start);
 		startClick.setOnClickListener(this);
 		viewStatus.put(startClick.getId(), false);
 		
 		plan_previou = (BasicTextView) layout.findViewById(R.id.plan_previous);
 		plan_previou.setOnClickListener(this);
 
 		dateRepeat = (BasicTextView) layout.findViewById(R.id.plan_repeat);
 		dateRepeat.setOnClickListener(this);
 		
 		planPeroidSelector=(BasicEditText)layout.findViewById(R.id.plan_length);
 		planPeroidSelector.setInputType(InputType.TYPE_NULL);
 		planPeroidSelector.setOnTouchListener(this);
 		
 		dateRepeat=(BasicTextView)layout.findViewById(R.id.plan_repeat);
 
 		dateRepeat.setOnClickListener(this);
 		
 		dateWarning=(BasicTextView)layout.findViewById(R.id.plan_warning);
 		dateWarning.setOnClickListener(this);
 		
 		plan_type=(BasicTextView)layout.findViewById(R.id.plan_type);
 		plan_type.setOnClickListener(this);
 
 		tvdate = (BasicTextView) layout.findViewById(R.id.plan_model);
 		tvdate.setOnClickListener(this);
 		viewStatus.put(tvdate.getId(), false);
 
 		
 		String dateString = (String) getText(R.string.date_layout_plan_model_1);
 		SpannableStringBuilder datestyle = new SpannableStringBuilder(
 				dateString);
 		datestyle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 3,
 				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
 		datestyle.setSpan(new ForegroundColorSpan(Color.WHITE), 3, 5,
 				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
 		tvdate.setText(datestyle);
 		
 		tvduration = (BasicTextView) layout.findViewById(R.id.plan_time_period);
 		tvduration.setOnClickListener(this);
 		viewStatus.put(tvduration.getId(), false);
 		// tvduration.setBackgroundColor(R.color.dateforcolor);
 		String durationString = (String) getText(R.string.date_plan_time_period_1);
 		SpannableStringBuilder durationstyle = new SpannableStringBuilder(
 				durationString);
 		durationstyle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 3,
 				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
 		durationstyle.setSpan(new ForegroundColorSpan(Color.WHITE), 3, 5,
 				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
 		tvduration.setText(durationstyle);
 		return layout;
 	}
 
 	void showDialog(DialogFragment dialogFragment) {
 
 		// DialogFragment.show() will take care of adding the fragment
 		// in a transaction. We also want to remove any currently showing
 		// dialog, so make our own transaction and take care of that here.
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 
 		// Create and show the dialog.
 		dialogFragment.show(ft, "dialog");
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			switch (v.getId()) {
 			case R.id.plan_time_start:
 				if (dateFragment == null) {
 					dateFragment = new TimeDialogFragment();
 				}
 				/** 趨ȡݽӿ */
 				dateFragment.setWheelInterface(new WheelResultInterface() {
 
 					@Override
 					public void getResult(String result) {
 						
 						startDateSelector.setText(result);
 						CacheModel model=TimeMasterApplication.getInstance().getCacheModel();
 						startChineseDate=model.currentTime;
 						model.startTime=startChineseDate;
 					}
 				});
 				showDialog(dateFragment);
 				break;
 			case R.id.plan_time_end:
 				if (dateFragment == null) {
 					dateFragment = new TimeDialogFragment();
 				}
 				/** 趨ȡݽӿ */
 				dateFragment.setWheelInterface(new WheelResultInterface() {
 
 					@Override
 					public void getResult(String result) {
 						endDateSelector.setText(result);
 						CacheModel model=TimeMasterApplication.getInstance().getCacheModel();
 						endChineseDate=model.currentTime;
 						model.endTime=endChineseDate;
 					}
 				});
 				showDialog(dateFragment);
 				break;
 			case R.id.plan_location:
 				if (locationFragment == null) {
 					locationFragment = new LocationDialogFragment();
 					/** 趨ȡݽӿ */
 					locationFragment
 							.setWheelInterface(new WheelResultInterface() {
 
 								@Override
 								public void getResult(String result) {
 									locationSelector.setText(result);
 								}
 							});
 				}
 				showDialog(locationFragment);
 				break;
 			case R.id.plan_human:
 				if (humanFragment == null) {
 					humanFragment = new HumanDialogFragment();
 					/** 趨ȡݽӿ */
 					humanFragment.setWheelInterface(new WheelResultInterface() {
 
 						@Override
 						public void getResult(String result) {
 							humanSelector.setText(result);
 						}
 					});
 				}
 				showDialog(humanFragment);
 				break;
 
 			case R.id.plan_length:
 				if(planTimePeroidFragment==null){
 					planTimePeroidFragment=new DurationTimeDialogFragment();
 					planTimePeroidFragment.setWheelInterface(new WheelResultInterface() {
 						
 						@Override
 						public void getResult(String result) {
 							// TODO Auto-generated method stub
 							planPeroidSelector.setText(result);
 						}
 					});
 				}
 				showDialog(planTimePeroidFragment);
 
 			default:
 				break;
 			}
 		}
 		return false;
 	}
 
 	
 	boolean flag = true;
 	@Override
 	public void onClick(View view) {
 		// TODO Auto-generated method stub
 		Class T =null;
 		FrameActivity activity=(FrameActivity)getActivity();
 		switch (view.getId()) {
 		case R.id.plan_repeat:
 			repeatFragment = new RepeatDialogFragment();
 			repeatFragment.setShowsDialog(true);
 			showDialog(repeatFragment);
 			break;
 		case R.id.plan_warning:
 			//warningFragment=new DateWarningFragment();
 			//T=DateWarningFragment.class;
 			activity.showNext(this.getId(),T, R.layout.date_warning);			
 			break;
 		case R.id.plan_type:
 			timewarningFragment=new DateTimeWarningDialogFragment();
 			timewarningFragment.setShowsDialog(true);
 			showDialog(timewarningFragment);
 			break;
 		case R.id.plan_model:
 			CacheModel model=TimeMasterApplication.getInstance().getCacheModel();
 			if (viewStatus.get(R.id.plan_model)) {
 				viewStatus.put(R.id.plan_model, false);
 				startDateSelector.setText(getChineseDateString(model.startTime));
 				endDateSelector.setText(getChineseDateString(model.endTime));
 			} else {
 				viewStatus.put(R.id.plan_model, true);
 				startDateSelector.setText(getCountdownDateString(model.startTime));
 				endDateSelector.setText(getCountdownDateString(model.endTime));
 			}
 
 			break;
 		case R.id.plan_time_period:
 			if (viewStatus.get(R.id.plan_time_period)) {
 
 				viewStatus.put(R.id.plan_time_period, false);
 			} else {
 				viewStatus.put(R.id.plan_time_period, true);
 			}
 
 			break;
 		case R.id.plan_start://ʼʱť
 			
 			if(countTimeHandler==null){
 				/**ʼʱhandler*/
 				countTimeHandler=new Handler(){
 					public void handleMessage(android.os.Message msg) {
 						switch(msg.what){
 						case 1://ʱ״̬
 							lengthSelector.setText((String)msg.obj);
 							
 							mTicker.run();
 							
 							break;
 						case 2://ֹ״̬
 							countTimeHandler.removeCallbacks(mTicker);
 							break;
 						}
 					};
 				};
 			}
 			if(mTicker==null){
 				/**ʼʱrunnable*/
 				mTicker = new Runnable() {
 					public void run() {
 						CacheModel m=TimeMasterApplication.getInstance().getCacheModel();
 						m.tickingTime+=1000L;
 						countTimeHandler.postDelayed(mTicker, 1000L);
 						lengthSelector.setText(getTimeGap(m.tickingTime,true));
 //						Message msg=new Message();
 //						msg.what=1;
 //						msg.obj=getTimeGap(m.tickingTime,true);
 //						countTimeHandler.sendMessageDelayed(msg, 1000L);
 					}
 				};
 			}
 		
 			model=TimeMasterApplication.getInstance().getCacheModel();
 			if(model.startTime==null)
 				model.startTime=new ChineseCalendar(new Date());
 			if(startDateSelector.getText().toString()==null||startDateSelector.getText().toString().equals("")){//ʼťΪ
 				startDateSelector.setText(getChineseDateString(model.startTime));
 			}
 			 BasicTextView v=(BasicTextView)view;
 			switch (v.getStatus()){
 			case 0:
 				v.setStatus(1);
 				startClick.setText("");
 				startClick.setBackgroundColor(0xFFFF0000);
 				Date date =new Date(System.currentTimeMillis());
 				model.currentTime=new ChineseCalendar(date);
 				startChineseDate=model.currentTime;
 				model.startTime=startChineseDate;
 				//뿪ʼʱ
 				TimeMasterApplication.getInstance().getDatabaseHelper().insertScheduleRecord(startChineseDate);
 				mTicker.run();
 				
 				break;
 			case 1://ֹͣʱ
 				v.setStatus(2);
 				startClick.setText("");
 				startClick.setBackgroundColor(0xFF00FF00);
 				countTimeHandler.sendEmptyMessage(2);
 				
 				if(model.endTime==null||(model.startTime.getTimeInMillis()+model.tickingTime)>model.endTime.getTimeInMillis()){
 					model.endTime=new ChineseCalendar(new Date(model.startTime.getTimeInMillis()+model.tickingTime));
 					endDateSelector.setText(getChineseDateString(model.endTime));
 					endChineseDate=new ChineseCalendar(model.endTime);
 					//½ʱ
 					TimeMasterApplication.getInstance().getDatabaseHelper().updateEndTime(endChineseDate);
 				}else{
 					if(endDateSelector.getText().toString()==null||endDateSelector.getText().toString().equals("")){
 						endDateSelector.setText(getChineseDateString(model.endTime));
 						endChineseDate=new ChineseCalendar(model.endTime);
 						//½ʱ
 						TimeMasterApplication.getInstance().getDatabaseHelper().updateEndTime(endChineseDate);
 					}
 				}
 				
 				break;
 			case 2:
 			    v.setStatus(1);
 			    startClick.setText("");
 			    startClick.setBackgroundColor(0xFFFF0000);
 			    mTicker.run();
 			    durationChinsesDate=new ChineseCalendar(new Date(model.tickingTime));
 			    //³ʱ
 			    TimeMasterApplication.getInstance().getDatabaseHelper().updateDurationTime(durationChinsesDate);
 			    break;
 			
 			default:
 				v.setStatus(0);
 				break;
 			}
 			break;
 		/*δݿ½ʱʱĽʱΪеһlastendtime½ʱΪڶ(lastsecondtime)*/
 		case R.id.plan_previous:
 
 			model=TimeMasterApplication.getInstance().getCacheModel();
 			if(model.startTime!=null){
 				long lastsecondendtime=0;
 				long lastendtime=0;
 				long finalendtime=0;
 				model.endTime=new ChineseCalendar(new Date(model.startTime.getTimeInMillis()+model.tickingTime));
 				//if(TimeMasterApplication.getInstance().getDatabaseHelper().getMaxRow()>2){
 				lastsecondendtime=TimeMasterApplication.getInstance().getDatabaseHelper().getLastSecondEndTime();
 				//}
 				lastendtime=TimeMasterApplication.getInstance().getDatabaseHelper().getLastEndTime();
 				finalendtime=lastsecondendtime;
 				/*ݿûʱ֪û*/
				if(lastsecondendtime==0||lastendtime==0){
 					Toast.makeText(getActivity(), "Ŀǰûѽ¼ʱ㹩ѡ",
 					     Toast.LENGTH_SHORT).show();
 					break;
 				}
 				/*δݿ½ʱ*/
 				if(lastendtime<model.endTime.getTimeInMillis()){
 						finalendtime=lastendtime;
 				
 				}
 				model.startTime=new ChineseCalendar(new Date(finalendtime));
 					startDateSelector.setText(getChineseDateString(model.startTime));
 						Toast.makeText(getActivity(), "ɳǰ",
 						     Toast.LENGTH_SHORT).show();//ʱεǰťǿʼʱʱ䱣ֲ䣬ЧʼӴToast
 				
 				
 			}
 			else{
 				Toast.makeText(getActivity(), "ѡһʼʱʼť",
 					     Toast.LENGTH_SHORT).show();
 
 			
 			}
 			break;
 		
 		}
 	}
 	private String getChineseDateString(ChineseCalendar date) {
 		if(date==null)
 			date=new ChineseCalendar(new Date());
 		int minute=date.get(ChineseCalendar.MINUTE);
 		return date.get(ChineseCalendar.YEAR)+"/"
 	            +(date.get(ChineseCalendar.MONTH)+1)+"/"
 				+date.get(ChineseCalendar.DAY_OF_MONTH)
 				+"  "+date.get(ChineseCalendar.HOUR_OF_DAY)+":"
 				+(minute<10?"0"+minute:minute);
 	}
 	
 	private String getCountdownDateString(ChineseCalendar date){
 		if(date==null)
 			return "0  00:00";
 		else{
 			StringBuffer stringB=new StringBuffer();
 			long now=new Date().getTime(),
 			future=date.getTime().getTime(),
 			between=future-now;
 			if(between<0)
 				stringB.append("-");
 			between=between>0?between:-between;
 	        stringB.append(getTimeGap(between,false));
 	        return stringB.toString();
 		}
 	}
 	private String getTimeGap(long diff,boolean hasSeconds){
 		StringBuffer stringB=new StringBuffer();
 		long day = diff / (24 * 60 * 60 * 1000);
         long hour = (diff / (60 * 60 * 1000) - day * 24);
         long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
         long seconds=((diff / (1000)) - day * 24 * 60 * 60 - hour * 60 * 60-min*60);
         //day + "" + hour + "Сʱ" + min + "" + s + "" + ms;
         stringB.append(day).append("  ").append(hour<10?"0"+hour:hour).append(":").append(min<10?"0"+min:min);
         if(hasSeconds)
         	stringB.append(":").append(seconds<10?"0"+seconds:seconds);
         return stringB.toString();
 	}
 
 
 
 //	public Map<String, Object> save(Context context) {
 //
 //		Map<String, Object> map = new HashMap<String, Object>();
 //
 //		try {
 //			FileInputStream in = new FileInputStream(
 //					"/data/data/com.time.master/files/Date.txt");
 //		    BufferedReader bf = new BufferedReader(new InputStreamReader(in));
 //			String tr = bf.readLine();
 //			String[] str = tr.split(";");
 //			map.put("now", str[0]);
 //			map.put("t", str[1]);
 //			map.put("end", str[2]);
 //		} catch (Exception e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		return map;
 //
 //	}
 }
 
