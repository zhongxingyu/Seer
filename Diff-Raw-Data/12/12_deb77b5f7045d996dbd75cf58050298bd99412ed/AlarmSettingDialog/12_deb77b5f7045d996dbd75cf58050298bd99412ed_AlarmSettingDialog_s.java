 package com.tools.tvguide.components;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.activities.ChannelDetailActivity;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.utils.Utility;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.widget.Toast;
 
 public class AlarmSettingDialog 
 {
     private Context mContext;
     private int mDay;
     private int mHour;
     private int mMin;
     private String mChannelId;
     private String mChannelName;
     private String mProgram;         // time + title
     private Calendar mCalendar;
     private OnAlarmSettingListener mListener;
     
     private final int DAY_IN_MS = 60 * 60 * 24 * 1000;              // 一天的毫秒数
     
     public interface OnAlarmSettingListener
     {
         public void onAlarmSetted(boolean success);
     }
     
     public AlarmSettingDialog(Context context, int day, int hour, int min, String channelId, String channelName, String program)
     {
         mContext = context;
         mDay = day;
         mHour = hour;
         mMin = min;
         mChannelId = channelId;
         mChannelName = channelName;
         mProgram = program;
         
         // -------------------- 对calendar的设置及调整 -------------------------
         mCalendar = Calendar.getInstance();
         mCalendar.setTimeInMillis(System.currentTimeMillis());
         mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
         mCalendar.set(Calendar.MINUTE, mMin);
         mCalendar.set(Calendar.SECOND, 0);
         mCalendar.set(Calendar.MILLISECOND, 0);
         long adjust = (mDay - Utility.getProxyDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))) * DAY_IN_MS;
         mCalendar.setTimeInMillis(mCalendar.getTimeInMillis() + adjust);
         
        // 因为周日算一周的第一天，所以这里要做特殊处理。如果使用API setFirstDayOfWeek, 则在月初时会设置到上月的末尾，故不用该API
        if (mDay == Utility.getProxyDay(Calendar.SUNDAY))
         {
             mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            mCalendar.setTimeInMillis(mCalendar.getTimeInMillis() + 60*60*24*1000);       // 周六再增加一天时间
         }
         
         // 如果今天是周日，则calendar的处理都是针对下一周的时间，所以这里要做特殊处理：减去一周的时间
         if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
         {
            mCalendar.setTimeInMillis(mCalendar.getTimeInMillis() - 60*60*24*7*1000);
         }
     }
     
     public void setAlarmSettingListener(OnAlarmSettingListener listener)
     {
         mListener = listener;
     }
     
     public void show()
     {
         if (mCalendar.getTimeInMillis() < System.currentTimeMillis())
         {
             AlertDialog dialog = new AlertDialog.Builder(mContext)
                 .setTitle(mContext.getResources().getString(R.string.tips))
                 .setMessage(mContext.getResources().getString(R.string.alarm_tips_cannot_set))
                 .setPositiveButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which) 
                     {
                         dialog.dismiss();
                     }
                 })
                 .create();
             dialog.show();
             return;
         }
         
         List<String> alarmList = new ArrayList<String>();
         alarmList.add(mContext.getResources().getString(R.string.m1_alarm));
         alarmList.add(mContext.getResources().getString(R.string.m5_alarm));
         alarmList.add(mContext.getResources().getString(R.string.m10_alarm));
         String alarmTimeString[] = (String[]) alarmList.toArray(new String[0]);
         
         long alarmTime = AppEngine.getInstance().getAlarmHelper().getAlarmTimeAtMillis(mChannelId, mChannelName, mProgram, mDay);
         int choice = -1;
         // Has already set the alarm clock
         if (alarmTime > 0)
         {
             long distance = mCalendar.getTimeInMillis() - alarmTime;
             int aheadSetMinute = new BigDecimal((double)distance / 1000 / 60).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();     // 四舍五入取整
             switch (aheadSetMinute)
             {
                 case 1:
                     choice = 0;
                     break;
                 case 5:
                     choice = 1;
                     break;
                 case 10:
                     choice = 2;
                     break;
             }
         }
         
         Dialog alertDialog = new AlertDialog.Builder(mContext)
                 .setTitle(mContext.getResources().getString(R.string.alarm_tips))
                 .setSingleChoiceItems(alarmTimeString, choice, new DialogInterface.OnClickListener() 
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which) 
                     {
                         int aheadMinute = 0;
                         switch (which)
                         {
                             case 0:
                                 aheadMinute = 1;
                                 break;
                             case 1:
                                 aheadMinute = 5;
                                 break;
                             case 2:
                                 aheadMinute = 10;
                                 break;
                         }
                         
                         // Try to remove the alarm first
                         AppEngine.getInstance().getAlarmHelper().removeAlarm(mChannelId, mChannelName, mProgram, mDay);
                         
                         // Set alarm clock
                         long alarmTimeInMillis = mCalendar.getTimeInMillis() - aheadMinute * 60 * 1000;
                         AppEngine.getInstance().getAlarmHelper().addAlarm(mChannelId, mChannelName, mProgram, mDay, alarmTimeInMillis);
                         if (alarmTimeInMillis < System.currentTimeMillis())   // The clock will sounds right now
                         {
                             if (mListener != null)
                                 mListener.onAlarmSetted(false);
                         }
                         else 
                         {
                             if (mListener != null)
                                 mListener.onAlarmSetted(true);
                             Toast.makeText(mContext, mContext.getResources().getString(R.string.alarm_tips_set), Toast.LENGTH_SHORT).show();
                         }
                         dialog.dismiss();
                     }
                 })
                 .setNegativeButton(mContext.getResources().getString(R.string.cancel_alarm), new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which) 
                     {
                         AppEngine.getInstance().getAlarmHelper().removeAlarm(mChannelId, mChannelName, mProgram, mDay);
                         if (mListener != null)
                             mListener.onAlarmSetted(false);
                         Toast.makeText(mContext, mContext.getResources().getString(R.string.alarm_tips_cancel), Toast.LENGTH_SHORT).show();
                     }
                     
                 })
                 .create();
         alertDialog.show();
     }
 }
