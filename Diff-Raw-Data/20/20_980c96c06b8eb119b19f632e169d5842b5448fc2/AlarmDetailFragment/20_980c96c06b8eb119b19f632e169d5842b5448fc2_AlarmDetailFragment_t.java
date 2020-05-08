 package org.tamanegi.parasiticalarm;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.text.format.DateFormat;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class AlarmDetailFragment extends Fragment
 {
     public static final String ARG_ITEM_ID = "item_id";
 
     private AlarmSettings settings = null;
     private int id = -1;
 
     private CheckBox checkOnOff;
     private TextView textTime;
     private TextView textAlarm;
     private TextView textVolume;
     private TextView textDay;
     private CheckBox checkSnooze;
     private TextView textSnoozeInterval;
     private View itemSnoozeTimeout;
     private TextView textSnoozeTimeout;
     private CheckBox checkVibration;
 
     public AlarmDetailFragment()
     {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         settings = new AlarmSettings(getActivity());
         settings.setOnSettingChangeListener(settingChangeListener);
 
         if(getArguments().containsKey(ARG_ITEM_ID)) {
             id = getArguments().getInt(ARG_ITEM_ID);
         }
     }
 
     @Override
     public void onDestroy()
     {
         super.onDestroy();
 
         settings.setOnSettingChangeListener(null);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
     {
         View rootView = inflater.inflate(R.layout.fragment_alarm_detail,
                                          container, false);
 
         // on-off
         checkOnOff = (CheckBox)rootView.findViewById(R.id.pref_check_onoff);
         rootView.findViewById(R.id.pref_onoff).setOnClickListener(onClickOnOff);
 
         // time
         textTime = (TextView)rootView.findViewById(R.id.pref_summary_time);
         rootView.findViewById(R.id.pref_time).setOnClickListener(onClickTime);
 
         // alarm
         textAlarm = (TextView)rootView.findViewById(R.id.pref_summary_alarm);
         rootView.findViewById(R.id.pref_alarm).setOnClickListener(onClickAlarm);
 
         // volume
         textVolume = (TextView)rootView.findViewById(R.id.pref_summary_volume);
         rootView.findViewById(R.id.pref_volume)
             .setOnClickListener(onClickVolume);
 
         // day
         textDay = (TextView)rootView.findViewById(R.id.pref_summary_day);
         rootView.findViewById(R.id.pref_day).setOnClickListener(onClickDay);
 
         // snooze interval
         textSnoozeInterval = (TextView)
             rootView.findViewById(R.id.pref_summary_snooze_interval);
         checkSnooze = (CheckBox)rootView.findViewById(R.id.pref_check_snooze);
         checkSnooze.setOnClickListener(onClickSnoozeCheck);
         rootView.findViewById(R.id.pref_snooze_interval)
             .setOnClickListener(onClickSnoozeInterval);
 
         // snooze timeout
         itemSnoozeTimeout = rootView.findViewById(R.id.pref_snooze_timeout);
         itemSnoozeTimeout.setOnClickListener(onClickSnoozeTimeout);
         textSnoozeTimeout = (TextView)
             rootView.findViewById(R.id.pref_summary_snooze_timeout);
 
         // vibration
         checkVibration = (CheckBox)
             rootView.findViewById(R.id.pref_check_vibration);
         rootView.findViewById(R.id.pref_vibration)
             .setOnClickListener(onClickVibration);
 
         if(id >= 0) {
             updateViewContent();
         }
 
         return rootView;
     }
 
     private CharSequence getAlarmNames()
     {
         Context context = getActivity();
         String separator = getString(R.string.pref_summary_alarm_separator);
         String[] alarmstr = settings.getAlarms(id);
 
         StringBuilder sb = new StringBuilder();
         for(int i = 0; i < alarmstr.length; i++) {
             if(sb.length() != 0) {
                 sb.append(separator);
             }
 
             AlarmData alarm =
                 AlarmData.unflattenFromString(context, alarmstr[i]);
             if(alarm != null) {
                 sb.append(alarm.getName());
             }
             else {
                 sb.append(alarmstr[i]);
             }
         }
 
         return sb;
     }
 
     private void updateViewContent()
     {
         Context context = getActivity();
 
         // on-off
         checkOnOff.setChecked(settings.getOnOff(id));
 
         // time
         textTime.setText(settings.getTime(id));
 
         // alarm
         textAlarm.setText(getAlarmNames());
 
         // volume
         textVolume.setText(settings.getVolumeString(context, id));
 
         // day
         textDay.setText(settings.getDayText(context, id));
 
         // snooze
         checkSnooze.setChecked(settings.isSnoozeEnabled(id));
         textSnoozeInterval.setText(
             settings.getSnoozeIntervalString(context, id));
 
         itemSnoozeTimeout.setEnabled(settings.isSnoozeEnabled(id));
         textSnoozeTimeout.setText(
             settings.getSnoozeTimeoutString(context, id));
 
         // vibration
         checkVibration.setChecked(settings.getVibration(id));
     }
 
     private AlarmSettings.OnSettingChangeListener settingChangeListener =
         new AlarmSettings.OnSettingChangeListener() {
             @Override
             public void onSettingChanged(
                 AlarmSettings setting, int index, String key) {
                 if(index == id) {
                     updateViewContent();
                 }
             }
         };
 
     private View.OnClickListener onClickOnOff = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 boolean val = (! checkOnOff.isChecked());
                 if(val && settings.getAlarms(id).length == 0) {
                     NoAlarmsDialogFragment f = new NoAlarmsDialogFragment();
                     f.show(getFragmentManager(), "NoAlarms");
                     return;
                 }
 
                 settings.setOnOff(id, val);
                 AlarmService.startSetupAlarms(getActivity());
             }
         };
 
     private View.OnClickListener onClickTime = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 TimeDialogFragment f = new TimeDialogFragment();
                 f.setId(id);
                 f.show(getFragmentManager(), "Time");
             }
         };
 
     private View.OnClickListener onClickAlarm = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 AlarmSelectDialogFragment f = new AlarmSelectDialogFragment();
                 f.setId(id);
                 f.show(getFragmentManager(), "Alarm");
             }
         };
 
     private View.OnClickListener onClickVolume = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 VolumeDialogFragment f = new VolumeDialogFragment();
                 f.setId(id);
                 f.show(getFragmentManager(), "Volume");
             }
         };
 
     private View.OnClickListener onClickDay = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 DayDialogFragment f = new DayDialogFragment();
                 f.setId(id);
                 f.show(getFragmentManager(), "Day");
             }
         };
 
     private View.OnClickListener onClickSnoozeCheck =
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 boolean val = ((CheckBox)v).isChecked();
                 settings.setSnoozeEnabled(id, val);
             }
         };
 
     private View.OnClickListener onClickSnoozeInterval =
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 SnoozeIntervalDialogFragment f =
                     new SnoozeIntervalDialogFragment();
                 f.setId(id);
                 f.show(getFragmentManager(), "SnoozeInterval");
             }
         };
 
     private View.OnClickListener onClickSnoozeTimeout =
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 SnoozeTimeoutDialogFragment f =
                     new SnoozeTimeoutDialogFragment();
                 f.setId(id);
                 f.show(getFragmentManager(), "SnoozeTimeout");
             }
         };
 
     private View.OnClickListener onClickVibration = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 settings.setVibration(id, ! checkVibration.isChecked());
             }
         };
 
     private static abstract class DetailDialogFragment
         extends DialogFragment
     {
         protected AlarmSettings settings;
         protected int id;
 
         public void setId(int id)
         {
             Bundle args =
                 (getArguments() != null ? getArguments() : new Bundle());
             args.putInt(ARG_ITEM_ID, id);
             setArguments(args);
         }
 
         @Override
         public void onCreate(Bundle savedInstanceState)
         {
             super.onCreate(savedInstanceState);
 
             settings = new AlarmSettings(getActivity());
             id = getArguments().getInt(ARG_ITEM_ID);
         }
     }
 
     public static class TimeDialogFragment
         extends DetailDialogFragment
         implements TimePickerDialog.OnTimeSetListener
     {
         @Override
         public Dialog onCreateDialog(Bundle savedState)
         {
             return new TimePickerDialog(
                 getActivity(), this,
                 settings.getTimeHour(id),
                 settings.getTimeMinute(id),
                 DateFormat.is24HourFormat(getActivity()));
         }
 
         @Override
         public void onTimeSet(TimePicker view, int hour, int minute)
         {
             settings.setTime(id, hour, minute);
 
             if(settings.getOnOff(id)) {
                 AlarmService.startSetupAlarms(getActivity());
             }
         }
     }
 
     public static class AlarmSelectDialogFragment
         extends DetailDialogFragment
         implements SimpleAdapter.ViewBinder,
                    DialogInterface.OnClickListener,
                    View.OnClickListener
     {
         private static final String KEY_ICON = "icon";
         private static final String KEY_LABEL = "label";
 
         private static final String[] ITEM_FROM = {
             KEY_ICON, KEY_LABEL
         };
         private static final int[] ITEM_TO = {
             R.id.item_icon, R.id.item_label
         };
 
         private SimpleAdapter adapter;
         private ListView listView;
         private List<AlarmData> alarms;
 
         @Override
         public Dialog onCreateDialog(Bundle savedState)
         {
             LayoutInflater inflater = getActivity().getLayoutInflater();
             View view = inflater.inflate(
                 R.layout.dialog_alarm_selector, null, false);
 
             // alarm list
             listView = (ListView)view.findViewById(R.id.alarm_list);
 
             alarms = AlarmData.getAllAvailableAlarmData(getActivity());
             adapter = new SimpleAdapter(
                 getActivity(), buildList(), R.layout.alarm_item,
                 ITEM_FROM, ITEM_TO);
             listView.setAdapter(adapter);
             listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
             List<String> curNames = Arrays.asList(settings.getAlarms(id));
             for(int i = 0; i < alarms.size(); i++) {
                 AlarmData alarm = alarms.get(i);
                 listView.setItemChecked(
                     i, curNames.contains(alarm.flattenToString()));
             }
 
             // select all/none button
             view.findViewById(R.id.alarm_select_none).setOnClickListener(this);
             view.findViewById(R.id.alarm_select_all).setOnClickListener(this);
 
             return new AlertDialog.Builder(getActivity())
                 .setTitle(R.string.pref_desc_alarm)
                 .setView(view)
                 .setPositiveButton(android.R.string.ok, this)
                 .setNegativeButton(android.R.string.cancel, null)
                 .create();
         }
 
         @Override
         public boolean setViewValue(View view, Object data, String text)
         {
             if(view.getId() == R.id.item_icon) {
                 ((ImageView)view).setImageURI((Uri)data);
                 return true;
             }
             else {
                 return false;
             }
         }
 
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
             // on click ok button
             SparseBooleanArray checkedPositions =
                 listView.getCheckedItemPositions();
             ArrayList<String> vals = new ArrayList<String>();
 
             for(int i = 0; i < alarms.size(); i++) {
                 if(checkedPositions.get(i)) {
                     vals.add(alarms.get(i).flattenToString());
                 }
             }
 
             if(vals.size() == 0 && settings.getOnOff(id)) {
                 NoAlarmsDialogFragment f = new NoAlarmsDialogFragment();
                 f.show(getFragmentManager(), "NoAlarms");
                 settings.setOnOff(id, false);
             }
             settings.setAlarms(id, vals.toArray(new String[vals.size()]));
         }
 
         @Override
         public void onClick(View view)
         {
             // on click select all/none button
             boolean val = (view.getId() == R.id.alarm_select_all);
             for(int i = 0; i < alarms.size(); i++) {
                 listView.setItemChecked(i, val);
             }
         }
 
         private List<Map<String, Object>> buildList()
         {
             List<Map<String, Object>> data =
                 new ArrayList<Map<String, Object>>();
 
             for(AlarmData alarm : alarms) {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put(KEY_ICON, alarm.getIcon());
                 map.put(KEY_LABEL, alarm.getName());
                 data.add(map);
             }
 
             return data;
         }
     }
 
     public static class VolumeDialogFragment
         extends DetailDialogFragment
         implements DialogInterface.OnClickListener,
                    SeekBar.OnSeekBarChangeListener,
                    View.OnClickListener
     {
         private int value_max;
 
         private CheckBox check;
         private SeekBar bar;
 
         @Override
         public Dialog onCreateDialog(Bundle savedState)
         {
             LayoutInflater inflater = getActivity().getLayoutInflater();
             View view = inflater.inflate(R.layout.dialog_volume, null, false);
 
             int val = settings.getVolume(id);
             AudioManager am = (AudioManager)
                 getActivity().getSystemService(Context.AUDIO_SERVICE);
             value_max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
             int cur_val = am.getStreamVolume(AudioManager.STREAM_ALARM);
 
             // use system preference checkbox
             check = (CheckBox)view.findViewById(R.id.volume_use_system);
             check.setOnClickListener(this);
             check.setChecked(val < 0);
 
             // volume seekbar
             bar = (SeekBar)view.findViewById(R.id.volume_seekbar);
             bar.setMax(value_max);
             bar.setOnSeekBarChangeListener(this);
 
             if(val < 0) {
                 bar.setVisibility(View.GONE);
                 bar.setProgress(cur_val);
             }
             else {
                 bar.setVisibility(View.VISIBLE);
                 bar.setProgress(val);
             }
 
             return new AlertDialog.Builder(getActivity())
                 .setTitle(R.string.pref_desc_volume)
                 .setView(view)
                 .setPositiveButton(android.R.string.ok, this)
                 .setNegativeButton(android.R.string.cancel, null)
                 .create();
         }
 
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
             // on click ok button
             settings.setVolume(id, check.isChecked() ? -1 : bar.getProgress());
         }
 
         @Override
         public void onClick(View view)
         {
             // on click use system checkbox
             bar.setVisibility(check.isChecked() ? View.GONE : View.VISIBLE);
         }
 
         @Override
         public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)
         {
             // todo: play sample audio?
         }
 
         @Override
         public void onStartTrackingTouch(SeekBar seekBar)
         {
             // do nothing
         }
 
         @Override
         public void onStopTrackingTouch(SeekBar seekBar)
         {
             // do nothing
         }
     }
 
     public static class DayDialogFragment
         extends DetailDialogFragment
         implements DialogInterface.OnClickListener,
                    DialogInterface.OnMultiChoiceClickListener
     {
         private EnumSet<AlarmSettings.DayOfWeek> days;
 
         @Override
         public Dialog onCreateDialog(Bundle savedState)
         {
             Context context = getActivity();
             days = settings.getDay(id);
             AlarmSettings.DayOfWeek[] alldays =
                 AlarmSettings.DayOfWeek.values();
 
             String[] str = new String[alldays.length];
             boolean[] checked = new boolean[alldays.length];
             for(int i = 0; i < alldays.length; i++) {
                 str[i] = alldays[i].getFullName(context);
                 checked[i] = days.contains(alldays[i]);
             }
 
             return new AlertDialog.Builder(getActivity())
                 .setTitle(R.string.pref_desc_day)
                 .setMultiChoiceItems(str, checked, this)
                 .setPositiveButton(android.R.string.ok, this)
                 .setNegativeButton(android.R.string.cancel, null)
                 .create();
         }
 
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
             // on click ok button
             settings.setDay(id, days);
         }
 
         @Override
         public void onClick(
             DialogInterface dialog, int which, boolean isChecked)
         {
             // on click list item
             if(isChecked) {
                 days.add(AlarmSettings.DayOfWeek.values()[which]);
             }
             else {
                 days.remove(AlarmSettings.DayOfWeek.values()[which]);
             }
         }
     }
 
     public static abstract class NumberPickerDialogFragment
         extends DetailDialogFragment
         implements DialogInterface.OnClickListener,
                    View.OnClickListener
     {
         private EditText edit;
         private int initialValue;
         private int titleResId;
 
         protected abstract void onClickOk(int val);
 
         public void setValue(int val)
         {
             initialValue = val;
         }
 
         public void setTitle(int resId)
         {
             titleResId = resId;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedState)
         {
             LayoutInflater inflater = getActivity().getLayoutInflater();
             View view = inflater.inflate(R.layout.number_picker, null, false);
 
             edit = (EditText)view.findViewById(R.id.numberpicker_input);
             edit.setText(String.valueOf(initialValue));
 
             view.findViewById(R.id.numberpicker_increment)
                 .setOnClickListener(this);
             view.findViewById(R.id.numberpicker_decrement)
                 .setOnClickListener(this);
 
             return new AlertDialog.Builder(getActivity())
                 .setTitle(titleResId)
                 .setView(view)
                 .setPositiveButton(android.R.string.ok, this)
                 .setNegativeButton(android.R.string.cancel, null)
                 .create();
         }
 
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
             // on click ok button
            int val;
            try {
                val = Integer.parseInt(edit.getText().toString());
            }
            catch(NumberFormatException e) {
                // ignore
                return;
            }

             onClickOk(val);
         }
 
         @Override
         public void onClick(View view)
         {
             // on click increment/decrement button
            int val;
            try {
                val = Integer.parseInt(edit.getText().toString());
            }
            catch(NumberFormatException e) {
                // ignore
                return;
            }
 
             val += (view.getId() == R.id.numberpicker_increment ? +1 : -1);
             if(val < 1) {
                 val = 1;
             }
 
             edit.setText(String.valueOf(val));
         }
     }
 
     public static class SnoozeIntervalDialogFragment
         extends NumberPickerDialogFragment
     {
         @Override
         public void onCreate(Bundle savedInstanceState)
         {
             super.onCreate(savedInstanceState);
 
             setTitle(R.string.pref_desc_snooze_interval);
             setValue(settings.getSnoozeInterval(id));
         }
 
         @Override
         protected void onClickOk(int val)
         {
             settings.setSnoozeEnabled(id, true);
             settings.setSnoozeInterval(id, val);
         }
     }
 
     public static class SnoozeTimeoutDialogFragment
         extends NumberPickerDialogFragment
     {
         @Override
         public void onCreate(Bundle savedInstanceState)
         {
             super.onCreate(savedInstanceState);
 
             setTitle(R.string.pref_desc_snooze_timeout);
             setValue(settings.getSnoozeTimeout(id));
         }
 
         @Override
         protected void onClickOk(int val)
         {
             settings.setSnoozeTimeout(id, val);
         }
     }
 }
