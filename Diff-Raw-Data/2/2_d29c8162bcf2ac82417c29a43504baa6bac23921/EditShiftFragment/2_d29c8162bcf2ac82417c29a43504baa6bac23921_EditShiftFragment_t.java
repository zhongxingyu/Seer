 package com.dgsd.android.ShiftTracker.Fragment;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.text.format.DateFormat;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.dgsd.android.ShiftTracker.Data.DbField;
 import com.dgsd.android.ShiftTracker.Data.Provider;
 import com.dgsd.android.ShiftTracker.Model.Shift;
 import com.dgsd.android.ShiftTracker.R;
 import com.dgsd.android.ShiftTracker.StApp;
 import com.dgsd.android.ShiftTracker.Util.Prefs;
 import com.dgsd.android.ShiftTracker.Util.TimeUtils;
 import com.dgsd.android.ShiftTracker.View.StatefulAutoCompleteTextView;
 import com.dgsd.android.ShiftTracker.View.StatefulEditText;
 import org.holoeverywhere.ArrayAdapter;
 import org.holoeverywhere.widget.Spinner;
 
 public class EditShiftFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>,
         View.OnClickListener,
         DatePickerFragment.OnDateSelectedListener,
         TimePickerFragment.OnTimeSelectedListener{
     private static final String KEY_SHIFT = "_shift";
     private static final String KEY_JULIAN_DAY = "_julian_day";
 
     private static final int LOADER_ID_SHIFT = 0x01;
     private static final int LOADER_ID_NAMES = 0x02;
 
     private static final int TYPE_CODE_START = 0;
     private static final int TYPE_CODE_END = 1;
 
 
     private Shift mInitialShift;
     private int mInitialJulianDay;
     private boolean mHasLoadedShift = false;
 
     private StatefulAutoCompleteTextView mName;
     private StatefulEditText mNotes;
     private StatefulEditText mPayRate;
     private StatefulEditText mUnpaidBreak;
     private TextView mStartDate;
     private TextView mEndDate;
     private TextView mStartTime;
     private TextView mEndTime;
     private View mReminders;
     private CheckBox mSaveAsTemplate;
 
     private SimpleCursorAdapter mNameAdapter;
 
     private DatePickerFragment mDateDialog;
     private TimePickerFragment mTimeDialog;
 
     private LastTimeSelected mLastTimeSelected;
     private String mLastNameFilter;
 
     private String[] mRemindersLabels;
     private String[] mRemindersValues;
 
     private LinkToPaidAppFragment mLinkToPaidAppFragment;
 
     private static enum LastTimeSelected {START, END};
 
     public static EditShiftFragment newInstance(int julianDay) {
         EditShiftFragment frag = new EditShiftFragment();
 
         Bundle args = new Bundle();
         args.putInt(KEY_JULIAN_DAY, julianDay);
         frag.setArguments(args);
 
         return frag;
     }
 
     public static EditShiftFragment newInstance(Shift shift) {
         EditShiftFragment frag = new EditShiftFragment();
 
         if(shift != null) {
             Bundle args = new Bundle();
             args.putParcelable(KEY_SHIFT, shift);
             frag.setArguments(args);
         }
 
         return frag;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mRemindersValues = getResources().getStringArray(R.array.reminder_minutes_values);
         mRemindersLabels = getResources().getStringArray(R.array.reminder_minutes_labels);
 
         mInitialJulianDay = -1;
         if(getArguments() != null) {
             mInitialShift = getArguments().getParcelable(KEY_SHIFT);
             mInitialJulianDay = getArguments().getInt(KEY_JULIAN_DAY, mInitialJulianDay);
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_edit_shift, container, false);
 
         mName = (StatefulAutoCompleteTextView) v.findViewById(R.id.name);
         mNotes = (StatefulEditText) v.findViewById(R.id.notes);
         mPayRate = (StatefulEditText) v.findViewById(R.id.pay_rate);
         mUnpaidBreak = (StatefulEditText) v.findViewById(R.id.unpaid_break);
         mStartDate = (TextView) v.findViewById(R.id.start_date);
         mEndDate = (TextView) v.findViewById(R.id.end_date);
         mStartTime = (TextView) v.findViewById(R.id.start_time);
         mEndTime = (TextView) v.findViewById(R.id.end_time);
         mSaveAsTemplate = (CheckBox) v.findViewById(R.id.is_template);
         mReminders = v.findViewById(R.id.reminders);
 
         ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), R.layout.reminders_spinner_item, mRemindersLabels);
         adapter.setDropDownViewResource(R.layout.reminders_spinner_dropdown_item);
         setAdapter(mReminders, adapter);
 
         if(StApp.isFreeApp(getActivity())) {
             mReminders.setEnabled(false);
             mReminders.setClickable(false);
             ViewGroup parent = ((ViewGroup) mReminders.getParent());
             parent.setClickable(true);
             parent.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if (mLinkToPaidAppFragment != null && mLinkToPaidAppFragment.isResumed())
                         return; //Already showing
 
                     mLinkToPaidAppFragment = LinkToPaidAppFragment.newInstance(getString(R.string.reminders_unavailable_message));
                     mLinkToPaidAppFragment.show(getSherlockActivity().getSupportFragmentManager(), null);
                 }
             });
         }
 
 
         mStartDate.setOnClickListener(this);
         mEndDate.setOnClickListener(this);
         mStartTime.setOnClickListener(this);
         mEndTime.setOnClickListener(this);
 
         mNameAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
                 new String[]{DbField.NAME.name}, new int[]{android.R.id.text1}, 0);
         mNameAdapter.setStringConversionColumn(0);//Index of 'Name' column
         mName.setAdapter(mNameAdapter);
 
         mName.addTextChangedListener(new TextWatcher() {
             @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
             @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
 
             @Override
             public void afterTextChanged(Editable editable) {
                 mName.setError(null);
 
                 mLastNameFilter = mName.getText() == null ? null : mName.getText().toString();
                 getLoaderManager().restartLoader(LOADER_ID_NAMES, null, EditShiftFragment.this);
             }
         });
 
         mUnpaidBreak.addTextChangedListener(new TextWatcher() {
             @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
             @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
 
             @Override
             public void afterTextChanged(Editable editable) {
                 mUnpaidBreak.setError(null);
             }
         });
 
         return v;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         if(mInitialShift != null && mInitialShift.id != -1)
             getLoaderManager().initLoader(LOADER_ID_SHIFT, null, this);
 
         getLoaderManager().initLoader(LOADER_ID_NAMES, null, this);
 
         prepopulate();
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
         switch(id) {
             case LOADER_ID_NAMES:
                 Uri uri = Provider.SHIFTS_URI.buildUpon().appendQueryParameter(Provider.QUERY_PARAMETER_DISTINCT, "1").build();
                 final String[] proj = {DbField.NAME.name, "1 as _id"};
                 final String sort = DbField.NAME + " ASC";
                 final String sel;
                 final String[] selArgs;
 
                 if(TextUtils.isEmpty(mLastNameFilter)) {
                     sel = null;
                     selArgs = null;
                 } else {
                     sel = DbField.NAME + " LIKE ?";
                     selArgs = new String[]{"%" + mLastNameFilter + "%"};
                 }
 
                 return new CursorLoader(getActivity(), uri, proj, sel, selArgs, sort);
             case LOADER_ID_SHIFT:
                 return new CursorLoader(getActivity(),
                         Provider.SHIFTS_URI,
                         null,
                         DbField.ID + "=?",
                         new String[]{String.valueOf(mInitialShift == null ? -1 : mInitialShift.id)},
                         null);
         }
         return null;
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
         switch(loader.getId()) {
             case LOADER_ID_SHIFT:
                 if(mHasLoadedShift)
                     return;
                 else
                     mHasLoadedShift = true;
 
                 break;
             case LOADER_ID_NAMES:
                 mNameAdapter.swapCursor(cursor);
                 break;
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         switch(loader.getId()) {
             case LOADER_ID_SHIFT:
                 break;
             case LOADER_ID_NAMES:
                 mNameAdapter.swapCursor(null);
                 break;
         }
     }
 
     private void prepopulate() {
         if(mInitialShift != null) {
             mName.setText(mInitialShift.name);
             mNotes.setText(mInitialShift.note);
             mSaveAsTemplate.setChecked(mInitialShift.isTemplate);
             if(mInitialShift.julianDay >= 0)
                 onDateSelected(TYPE_CODE_START, mInitialShift.julianDay);
 
             if(mInitialShift.endJulianDay >= 0)
                 onDateSelected(TYPE_CODE_END, mInitialShift.endJulianDay);
 
             if(mInitialShift.getStartTime() != -1)
                 setStartTime(mInitialShift.getStartTime());
 
             if(mInitialShift.getEndTime() != -1)
                 setEndTime(mInitialShift.getEndTime());
 
             if(mInitialShift.breakDuration >= 0)
                 mUnpaidBreak.setText(String.valueOf(mInitialShift.breakDuration));
 
             if(mInitialShift.payRate >= 0)
                 mPayRate.setText(String.valueOf(mInitialShift.payRate));
 
             for(int i = 0, len = mRemindersValues.length; i < len; i++) {
                 if(String.valueOf(mInitialShift.reminder).equals(mRemindersValues[i])) {
                     setSelection(mReminders, i);
                     break;
                 }
             }
 
            getSherlockActivity().invalidateOptionsMenu();
         } else {
             //No initial shift, just set up our date/time values
             final int jd = mInitialJulianDay < 0 ? TimeUtils.getCurrentJulianDay() : mInitialJulianDay;
             onDateSelected(TYPE_CODE_START, jd);
             onDateSelected(TYPE_CODE_END, jd);
 
             //Default 9 - 5 shift
             Time t = new Time();
             t.setToNow();
             t.hour = 9;
             t.minute = 0;
             t.second = 0;
             t.normalize(true);
 
             final Prefs p = Prefs.getInstance(getActivity());
             setStartTime(p.get(getString(R.string.settings_key_default_start_time), t.toMillis(true)));
 
             t.hour = 17;
             t.normalize(true);
 
             setEndTime(p.get(getString(R.string.settings_key_default_end_time), t.toMillis(true)));
 
             mUnpaidBreak.setText(p.get(getString(R.string.settings_key_default_break_duration), null));
             mPayRate.setText(p.get(getString(R.string.settings_key_default_pay_rate), null));
 
             String remindersVal = p.get(getString(R.string.settings_key_default_reminder), "None");
             int index = 0;
             for(int i = 0, len = mRemindersLabels.length; i < len; i++) {
                 if(TextUtils.equals(remindersVal, mRemindersLabels[i])) {
                     index = i;
                     break;
                 }
             }
 
             setSelection(mReminders, index);
         }
     }
 
     private void setStartTime(long time) {
         mLastTimeSelected = LastTimeSelected.START;
         onTimeSelected(time);
     }
 
     private void setEndTime(long time) {
         mLastTimeSelected = LastTimeSelected.END;
         onTimeSelected(time);
     }
 
     @Override
     public void onClick(View view) {
         final int id = view.getId();
         if(id == R.id.start_date || id == R.id.end_date) {
             if(mDateDialog != null && mDateDialog.isResumed())
                 return; //We're showing already!
 
             final int centerJd = TimeUtils.getCurrentJulianDay();
             final int count = 104 * 7; // 2 years
 
             final Time time = new Time();
             time.setJulianDay(centerJd - (count / 2));
             final long min = time.toMillis(true);
 
             time.setJulianDay(centerJd + (count / 2));
             final long max = time.toMillis(true);
 
             final Integer date;
             final int type;
             if(id == R.id.start_date) {
                 date = (Integer) mStartDate.getTag();
                 type = TYPE_CODE_START;
             } else {
                 date = (Integer) mEndDate.getTag();
                 type = TYPE_CODE_END;
             }
 
             mDateDialog = DatePickerFragment.newInstance("Date of shift", min, max, date == null ? -1 : date, type);
             mDateDialog.setOnDateSelectedListener(this);
             mDateDialog.show(getSherlockActivity().getSupportFragmentManager(), null);
         } else if(id == R.id.end_time) {
             mLastTimeSelected = LastTimeSelected.END;
             if(mTimeDialog != null && mTimeDialog.isResumed())
                 return; //We're showing already!
 
             long time = mEndTime.getTag() == null ? -1 : (Long) mEndTime.getTag();
             mTimeDialog = TimePickerFragment.newInstance(time);
             mTimeDialog.setOnTimeSelectedListener(this);
             mTimeDialog.show(getSherlockActivity().getSupportFragmentManager(), null);
         } else if(id == R.id.start_time) {
             mLastTimeSelected = LastTimeSelected.START;
             if(mTimeDialog != null && mTimeDialog.isResumed())
                 return; //We're showing already!
 
             long time = mStartTime.getTag() == null ? -1 : (Long) mStartTime.getTag();
             mTimeDialog = TimePickerFragment.newInstance(time);
             mTimeDialog.setOnTimeSelectedListener(this);
             mTimeDialog.show(getSherlockActivity().getSupportFragmentManager(), null);
         }
     }
 
     @Override
     public void onDateSelected(int typeCode, int julianDay) {
         if(getActivity() == null)
             return;
 
         final long millis = TimeUtils.getStartMillisForJulianDay(julianDay);
         String formatted = DateUtils.formatDateRange(getActivity(), millis, millis,
                 DateUtils.FORMAT_ABBREV_ALL |
                         DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);
 
         if(typeCode == TYPE_CODE_START) {
             mStartDate.setTag(julianDay);
             mStartDate.setText(formatted);
         } else {
             mEndDate.setTag(julianDay);
             mEndDate.setText(formatted);
         }
     }
 
     @Override
     public void onTimeSelected(long millis) {
         if(getActivity() == null)
             return;
 
         TextView tv = null;
         if(mLastTimeSelected == LastTimeSelected.START)
             tv = mStartTime;
         else if(mLastTimeSelected == LastTimeSelected.END)
             tv = mEndTime;
         else
             return; //O no! This should never happen!
 
         tv.setTag(millis);
 
         int flags = DateUtils.FORMAT_SHOW_TIME;
         if(DateFormat.is24HourFormat(getActivity()))
             flags |= DateUtils.FORMAT_24HOUR;
         else
             flags |= DateUtils.FORMAT_12HOUR;
 
         tv.setError(null);
         tv.setText(DateUtils.formatDateRange(getActivity(), millis, millis, flags));
     }
 
     public Shift getShift() {
         Shift shift = new Shift();
 
         shift.id = mInitialShift == null ? -1 : mInitialShift.id;
         shift.name = mName.getText() == null ? null : mName.getText().toString();
         shift.note = mNotes.getText() == null ? null : mNotes.getText().toString();
         shift.julianDay = mStartDate.getTag() == null ? -1 : (Integer) mStartDate.getTag();
         shift.endJulianDay = mEndDate.getTag() == null ? shift.julianDay : (Integer) mEndDate.getTag();
         shift.setStartTime(mStartTime.getTag() == null ? -1 : (Long) mStartTime.getTag());
         shift.setEndTime(mEndTime.getTag() == null ? -1 : (Long) mEndTime.getTag());
         shift.isTemplate = mSaveAsTemplate.isChecked();
         shift.reminder = Integer.valueOf(mRemindersValues[getSelectedPosition(mReminders)]);
 
         Time time = new Time();
 
         try {
             CharSequence breakDuration = mUnpaidBreak.getText();
             if(!TextUtils.isEmpty(breakDuration))
                 shift.breakDuration = Integer.valueOf(breakDuration.toString());
         } catch(NumberFormatException e) {
             shift.breakDuration = -1;
         }
 
         try {
             CharSequence payRate = mPayRate.getText();
             if(!TextUtils.isEmpty(payRate))
                 shift.payRate = Float.valueOf(payRate.toString());
         } catch(NumberFormatException e) {
             shift.breakDuration = -1;
         }
 
         return shift;
     }
 
     /**
      * @return Non-null with an error message to show the user
      */
     public String validate() {
         String error = null;
 
         if(TextUtils.isEmpty(mName.getText())) {
             error = "Please enter a name";
             mName.setError(error);
             return error;
         }
 
         error = validateTime();
         if(!TextUtils.isEmpty(error))
             return error;
 
         error = validateBreakDuration();
         if(!TextUtils.isEmpty(error))
             return error;
 
         return null;
     }
 
     private String validateBreakDuration() {
         Long startMillis = (Long) mStartTime.getTag();
         Long endMillis = (Long) mEndTime.getTag();
 
         final CharSequence breakDurationAsStr = mUnpaidBreak.getText();
         if(startMillis == null || endMillis == null || TextUtils.isEmpty(breakDurationAsStr))
             return null;
 
         if(!TextUtils.isDigitsOnly(breakDurationAsStr)) {
             String error = "Invalid break time";
             mUnpaidBreak.setError(error);
             return error;
         }
 
         long duration = (endMillis - startMillis) / TimeUtils.InMillis.MINUTE;
         if(duration < Long.valueOf(breakDurationAsStr.toString())) {
             String error = "Break duration longer than shift";
             mUnpaidBreak.setError(error);
             return error;
         }
 
         return null;
     }
 
     private String validateTime() {
         Long startMillis = (Long) mStartTime.getTag();
         Long endMillis = (Long) mEndTime.getTag();
 
         Integer startDay = (Integer) mStartDate.getTag();
         Integer endDay = (Integer) mEndDate.getTag();
 
         if(startMillis == null) {
             String error = "Please select a start time";
             mStartTime.setError(error);
             return error;
         }
 
         if(endMillis == null) {
             String error = "Please select an end time";
             mEndTime.setError(error);
             return error;
         }
 
         if(startDay == null) {
             String error = "Please select a start day";
             mStartDate.setError(error);
             return error;
         }
 
         if(endDay == null) {
             String error = "Please select an end day";
             mEndDate.setError(error);
             return error;
         }
 
         if(endDay < startDay) {
             String error = "End date must be after start date";
             mEndDate.setError(error);
             return error;
         }
 
         Time start = new Time();
         Time end = new Time();
 
         start.set(startMillis);
         end.set(endMillis);
 
         if(startDay.equals(endDay) && (start.hour > end.hour ||
                 (start.hour == end.hour && start.minute > end.minute))) {
             String error = "End time is before start time";
             mEndTime.setError(error);
             return error;
         }
 
         return null;
     }
 
     public boolean isEditing() {
         return getEditingId() >= 0;
     }
 
     public long getEditingId() {
         return mInitialShift != null ? mInitialShift.id : -1;
     }
 
     private void setSelection(View view, int index) {
         if(view instanceof Spinner)
             ((Spinner) view).setSelection(index);
         else if(view instanceof android.widget.Spinner)
             ((android.widget.Spinner) view).setSelection(index);
     }
 
     private int getSelectedPosition(View view) {
         if(view instanceof Spinner)
             return ((Spinner) view).getSelectedItemPosition();
         else if(view instanceof android.widget.Spinner)
             return ((android.widget.Spinner) view).getSelectedItemPosition();
         else
             return 0;
     }
 
     private void setAdapter(View view, SpinnerAdapter adapter) {
         if(view instanceof Spinner)
             ((Spinner) view).setAdapter(adapter);
         else if(view instanceof android.widget.Spinner)
             ((android.widget.Spinner) view).setAdapter(adapter);
     }
 }
