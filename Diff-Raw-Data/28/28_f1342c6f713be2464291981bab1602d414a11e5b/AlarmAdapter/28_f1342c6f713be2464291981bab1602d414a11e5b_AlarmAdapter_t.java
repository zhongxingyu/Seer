 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.adapter;
 
 import java.util.List;
 
 import se.toxbee.sleepfighter.R;
 import se.toxbee.sleepfighter.android.component.secondpicker.SecondTimePicker;
 import se.toxbee.sleepfighter.android.component.secondpicker.SecondTimePickerDialog;
 import se.toxbee.sleepfighter.model.Alarm;
 import se.toxbee.sleepfighter.model.time.AlarmTime;
 import se.toxbee.sleepfighter.model.time.CountdownTime;
 import se.toxbee.sleepfighter.model.time.ExactTime;
 import se.toxbee.sleepfighter.text.DateTextUtils;
 import se.toxbee.sleepfighter.text.MetaTextUtils;
 import se.toxbee.sleepfighter.utils.string.StringUtils;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class AlarmAdapter extends ArrayAdapter<Alarm> {
 
 	public AlarmAdapter(Context context, List<Alarm> alarms) {
 		super(context, 0, alarms);
 	}
 	
 	public void makeTimeTextViewHitboxBigger(View convertView) {
 		View container = convertView.findViewById(R.id.time_view_container);
 		final TextView timeTextView = (TextView) convertView.findViewById( R.id.time_view );
 		container.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				timeTextView.performClick();
 			}
 		});	
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			// A view isn't being recycled, so make a new one from definition
 			convertView = LayoutInflater.from(getContext()).inflate( R.layout.alarm_list_item, null);
 		}
 		
 		makeTimeTextViewHitboxBigger(convertView);
 	
 		// The alarm associated with the row
 		final Alarm alarm = getItem(position);
 
 		// Set properties of view elements to reflect model state
 		this.setupActivatedSwitch( alarm, convertView );
 
 		this.setupTimeText( alarm, convertView );
 
 		this.setupName( alarm, convertView );
 
 		this.setupWeekdays( alarm, convertView );
 		
 		return convertView;
 	}
 
 	public void pickTime( final Alarm alarm ) {
 		if ( alarm.isCountdown() ) {
 			this.pickCountdownTime( alarm );
 		} else {
 			this.pickNormalTime( alarm );
 		}
 	}
 
 	public void pickNormalTime( final Alarm alarm ) {
 		OnTimeSetListener onTimePickerSet = new OnTimeSetListener() {
 			@Override
 			public void onTimeSet(TimePicker view, int h, int m ) {
 				alarm.setTime( new ExactTime( h, m ) );
 			}
 		};
 
 		// TODO possibly use some way that doesn't make the dialog close on rotate
 		AlarmTime time = alarm.getTime();
 		TimePickerDialog tpd = new TimePickerDialog(
 			getContext(), onTimePickerSet,
 			time.getHour(), time.getMinute(),
 			true
 		);
 
 		tpd.show();
 	}
 
 	public void pickCountdownTime( final Alarm alarm ) {
 		SecondTimePickerDialog.OnTimeSetListener onTimePickerSet = new SecondTimePickerDialog.OnTimeSetListener() {
 			@Override
 			public void onTimeSet( SecondTimePicker view, int h, int m, int s ) {
 				alarm.setTime( new CountdownTime( h, m, s ) );
 			}
 		};
 
 		// TODO possibly use some way that doesn't make the dialog close on rotate
 		AlarmTime time = alarm.getTime();
		time.refresh();

 		SecondTimePickerDialog tpd = new SecondTimePickerDialog(
 			getContext(), onTimePickerSet,
 			time.getHour(), time.getMinute(), time.getSecond(),
 			true
 		);
 
 		tpd.show();
 	}
 
 	private void setupTimeText( final Alarm alarm, View convertView ) {
 		AlarmTime time = alarm.getTime();
 		time.refresh();
 
 		final TextView timeTextView = (TextView) convertView.findViewById( R.id.time_view );
 		timeTextView.setText( time.getTimeString() );
 
 		// Set countdown if needed.
 		final TextView secTextView = (TextView) convertView.findViewById( R.id.time_view_seconds );
 		if ( alarm.isCountdown() ) {
 			secTextView.setVisibility( View.VISIBLE );
 			secTextView.setText( StringUtils.joinTime( time.getSecond() ) + "\"" );
 		} else {
 			secTextView.setVisibility( View.GONE );
 		}
 
 		timeTextView.setOnClickListener( new OnClickListener() {
 			public void onClick( View v ) {
 				pickTime( alarm );
 			}
 		});
 	}
 
 	private void setupName( final Alarm alarm, View convertView ) {
 		TextView nameTextView = (TextView) convertView.findViewById(R.id.name_view);
 		String name = MetaTextUtils.printAlarmName( this.getContext(), alarm );
 		nameTextView.setText(name);
 	}
 
 	private void setupWeekdays( final Alarm alarm, View convertView ) {
 		TextView view = (TextView) convertView.findViewById(R.id.weekdaysText);
 		view.setText( DateTextUtils.makeEnabledDaysText( alarm ) );
 	}
 
 	private void setupActivatedSwitch(final Alarm alarm, View convertView) {
 		
 		final CompoundButton activatedSwitch = (CompoundButton) convertView.findViewById(R.id.activated);
 
 		View activatedBackground =  convertView.findViewById(R.id.activated_background);
 
 		// Makes sure that previous alarm for a recycled view won't get changed
 		// when setting initial value
 		activatedSwitch.setOnCheckedChangeListener(null);
 
 		activatedSwitch.setChecked(alarm.isActivated());
 
 		activatedSwitch.setOnCheckedChangeListener( new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
 				alarm.setActivated(isChecked);
 			}
 		} );
 		
 		// Allow pressing in area next to checkbox/switch to toggle
 		activatedBackground.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				activatedSwitch.toggle();
 			}
 		});
 	}
 }
