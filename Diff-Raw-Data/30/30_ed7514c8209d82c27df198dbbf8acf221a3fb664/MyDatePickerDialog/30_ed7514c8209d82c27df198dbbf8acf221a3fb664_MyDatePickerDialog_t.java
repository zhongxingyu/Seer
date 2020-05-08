 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.lge.calendar;
 
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.Switch;
 import android.widget.TextView;
 
 import com.lge.calendar.MyDatePicker.OnDateChangedListener;
 
 public class MyDatePickerDialog extends AlertDialog implements OnClickListener,
 		OnDateChangedListener, OnCheckedChangeListener {
 
 	private static final String DATE = "date";
 
 	private final MyDatePicker mDatePicker;
 	private final OnDateSetListener mCallBack;
 	private TextView alternateDateText;
 	private Calendar alternateCalendar;
 
 	/**
 	 * The callback used to indicate the user is done filling in the date.
 	 */
 	public interface OnDateSetListener {
 
 		/**
 		 * @param view
 		 *            The view associated with this listener.
 		 * @param year
 		 *            The year that was set.
 		 * @param monthOfYear
 		 *            The month that was set (0-11) for compatibility with
 		 *            {@link java.util.Calendar}.
 		 * @param dayOfMonth
 		 *            The day of the month that was set.
 		 */
 		void onDateSet(MyDatePicker view, Calendar newDate);
 	}
 
 	/**
 	 * @param context
 	 *            The context the dialog is to run in.
 	 * @param callBack
 	 *            How the parent is notified that the date is set.
 	 * @param year
 	 *            The initial year of the dialog.
 	 * @param monthOfYear
 	 *            The initial month of the dialog.
 	 * @param dayOfMonth
 	 *            The initial day of the dialog.
 	 */
 	public MyDatePickerDialog(Context context, OnDateSetListener callBack,
 			Calendar date1, Calendar date2) {
 		this(context, 0, callBack, date1, date2);
 	}
 
 	/**
 	 * @param context
 	 *            The context the dialog is to run in.
 	 * @param theme
 	 *            the theme to apply to this dialog
 	 * @param callBack
 	 *            How the parent is notified that the date is set.
 	 * @param date
 	 *            The initial date of the dialog.
 	 */
 	public MyDatePickerDialog(Context context, int theme,
 			OnDateSetListener callBack, Calendar date1, Calendar date2) {
 		super(context, theme);
 
 		mCallBack = callBack;
 
 		Context themeContext = getContext();
 		setButton(BUTTON_POSITIVE,
 				themeContext.getText(R.string.date_time_set), this);
 		setButton(BUTTON_NEGATIVE,
 				themeContext.getText(android.R.string.cancel),
 				(OnClickListener) null);
 		setIcon(0);
 		setTitle(R.string.date_picker_dialog_title);
 
 		LayoutInflater inflater = (LayoutInflater) themeContext
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View view = inflater.inflate(R.layout.date_picker_dialog, null);
 		setView(view);
 		mDatePicker = (MyDatePicker) view.findViewById(R.id.datePicker);
 		mDatePicker.init(date1, this);
 
 		alternateCalendar = (Calendar) date2.clone();
 
 		Switch sw = (Switch) view.findViewById(R.id.switch_calendar);
 		sw.setOnCheckedChangeListener(this);
 
 		alternateDateText = (TextView) view.findViewById(R.id.alternate_date);
 		updateAlternateDate();
 	}
 
 	public void onClick(DialogInterface dialog, int which) {
 		if (mCallBack != null) {
 			mDatePicker.clearFocus();
 			mCallBack.onDateSet(mDatePicker, mDatePicker.getDate());
 		}
 	}
 
 	public void onDateChanged(MyDatePicker view, Calendar date) {
 		syncCalendars(date);
 	}
 
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		switchCalendars();
 	}
 
 	private void syncCalendars(Calendar date) {
 		alternateCalendar.setTimeInMillis(date.getTimeInMillis());
 		updateAlternateDate();
 	}
 
 	private String format(Calendar date) {
 		return String.format("%d-%02d-%02d", date.get(Calendar.YEAR),
 				date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
 	}
 
 	private void switchCalendars() {
 		Calendar tempDate = mDatePicker.getDate();
 		mDatePicker.init(alternateCalendar, this);
 		alternateCalendar = tempDate;
 		updateAlternateDate();
 	}
 
 	private void updateAlternateDate() {
 		alternateDateText.setText(format(alternateCalendar));
 	}
 
 	/**
 	 * Gets the {@link DatePicker} contained in this dialog.
 	 * 
 	 * @return The calendar view.
 	 */
 	public MyDatePicker getDatePicker() {
 		return mDatePicker;
 	}
 
 	@Override
 	public Bundle onSaveInstanceState() {
 		Bundle state = super.onSaveInstanceState();
 		state.putSerializable(DATE, mDatePicker.getDate());
 		return state;
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		mDatePicker.init((Calendar) savedInstanceState.getSerializable(DATE),
 				this);
 	}
 }
