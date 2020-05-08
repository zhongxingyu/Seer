 package com.danielpecos.gtdtm.views;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.TimePickerDialog;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.ToggleButton;
 
 import com.danielpecos.gtdtm.R;
 import com.danielpecos.gtdtm.model.TaskManager;
 import com.danielpecos.gtdtm.model.beans.Task;
 import com.danielpecos.gtdtm.model.beans.Task.Status;
 import com.danielpecos.gtdtm.utils.ActivityUtils;
 
 public class TaskViewHolder extends ViewHolder {
 	private Task task;
 
 	private LinearLayout layout_taskIcons_1;
 	private LinearLayout layout_taskIcons_2;
 
 	private TextView textView_taskName;
 	private EditText editText_taskName;
 	private TextView textView_taskDescription;
 	private EditText editText_taskDescription;
 	private Button button_taskDescriptionClear;
 	private ImageView imageViewCheckBox_taskStatus;
 	private Spinner spinner_taskPriority;
 	private ToggleButton toggleButton_taskDiscarded;
 	private Button button_takePicture;
 	private Button button_deletePicture;
 	private ImageView imageView_picture;
 
 	private TextView textView_taskDueDate;
 	private TextView textView_taskDueTime;
 	private Button button_changeDueDate;
 	private Button button_changeDueTime;
 
 	private TextView textView_locationLat;
 	private TextView textView_locationLong;
 	private Button button_changeMapPosition;
 
 	private List<TextView> textViews_labels;
 
 	public TaskViewHolder(View view, Task task) {
 		super(view);
 		this.task = task;
 	}
 
 	public Task getTask() {
 		return task;
 	}
 
 	@Override
 	public HashMap<String, Object> getListFields() {
 		HashMap<String, Object> taskData = new HashMap<String, Object>();
 
 		taskData.put("_BASE_", task);
 		taskData.put("id", task.getId());
 		taskData.put("name", task.getName());
 		taskData.put("description", task.getDescription());
 		taskData.put("status_check", task.getStatus() == Task.Status.Completed || task.getStatus() == Task.Status.Discarded_Completed);
 		taskData.put("priority", task.getPriority());
 
 		return taskData;
 	}
 
 	private void setUpView(final Activity activity) {
 		this.textViews_labels = new ArrayList<TextView>();
 		if (getView(R.id.task_priority_label) != null) { 
 			this.textViews_labels.add((TextView)getView(R.id.task_priority_label));
 		}
 		if (getView(R.id.task_description_label) != null) {
 			this.textViews_labels.add((TextView)getView(R.id.task_description_label));
 		}
 		if (getView(R.id.task_status_label) != null) {
 			this.textViews_labels.add((TextView)getView(R.id.task_status_label));
 		}
 		if (getView(R.id.task_duedate_label) != null) {
 			this.textViews_labels.add((TextView)getView(R.id.task_duedate_label));
 		}
 		if (getView(R.id.task_duetime_label) != null) {
 			this.textViews_labels.add((TextView)getView(R.id.task_duetime_label));
 		}
 		if (getView(R.id.task_picture_label) != null) {
 			this.textViews_labels.add((TextView)getView(R.id.task_picture_label));
 		}
 
 		if (getView(R.id.task_name) != null) {
 			this.textView_taskName = (TextView)getView(R.id.task_name);
 		} else {
 			this.editText_taskName = (EditText)getView(R.id.task_name_text);
 			if (this.editText_taskName != null) {
 				this.editText_taskName.addTextChangedListener(new TextWatcher() {
 					@Override
 					public void onTextChanged(CharSequence s, int start, int before, int count) {
 						task.setName(editText_taskName.getText().toString());
 					}
 					@Override
 					public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
 					}
 					@Override
 					public void afterTextChanged(Editable s) {
 					}
 				});
 			}
 		}
 		if (getView(R.id.task_description) != null) {
 			this.textView_taskDescription = (TextView)getView(R.id.task_description);
 		} else {
 			this.editText_taskDescription = (EditText)getView(R.id.task_description_text);
 			if (this.editText_taskDescription != null) {
 				this.editText_taskDescription.addTextChangedListener(new TextWatcher() {
 					@Override
 					public void onTextChanged(CharSequence s, int start, int before, int count) {
 						task.setDescription(editText_taskDescription.getText().toString());
 					}
 					@Override
 					public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
 					}
 					@Override
 					public void afterTextChanged(Editable s) {
 					}
 				});
 				this.button_taskDescriptionClear = (Button)getView(R.id.task_description_clear);
 				this.button_taskDescriptionClear.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						editText_taskDescription.setText("");
 						Log.d(TaskManager.TAG, "Description text cleared");
 					}
 				});
 			}
 		}
 
 		this.imageViewCheckBox_taskStatus = (ImageView)getView(R.id.task_status_check);
 		if (this.imageViewCheckBox_taskStatus != null) {
 			this.imageViewCheckBox_taskStatus.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 
 					if (isCallbacksEnabled()) {
 
 						if (task.getStatus() != Task.Status.Discarded && task.getStatus() != Task.Status.Discarded_Completed) {
 
 							if (task.getStatus() == Task.Status.Discarded) {
 								task.setStatus(Task.Status.Discarded_Completed);
 							} else if (task.getStatus() == Task.Status.Discarded_Completed) {
 								task.setStatus(Task.Status.Discarded);
 							} else if (task.getStatus() == Task.Status.Active) {
 								task.setStatus(Task.Status.Completed);
 							} else if (task.getStatus() == Task.Status.Completed) {
 								task.setStatus(Task.Status.Active);
 							}
 
 							boolean isChecked = task.getStatus() == Status.Completed || task.getStatus() == Status.Discarded_Completed;
 
 							if (viewListeners != null && viewListeners.get(R.id.task_status_check) != null) {
 								for (Object event : viewListeners.get(R.id.task_status_check)) {
 									((com.danielpecos.gtdtm.views.OnCheckedChangeListener)event).onCheckedChanged(v, isChecked);
 								}
 							}
 
 							updateView(activity);
 						}
 					}
 				}
 			});
 		}
 
 		this.spinner_taskPriority = (Spinner)getView(R.id.task_priority_spinner);
 		if (this.spinner_taskPriority != null) {
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.task_priorities, android.R.layout.simple_spinner_item);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			this.spinner_taskPriority.setAdapter(adapter);
 			this.spinner_taskPriority.setOnItemSelectedListener(new OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 					if (isCallbacksEnabled()) {
 						Task.Priority pIni = task.getPriority();
 						switch (pos) {
 						case 0:
 							if (task.getPriority() != Task.Priority.Critical) {
 								task.setPriority(Task.Priority.Critical);
 							}
 							break;
 						case 1:
 							if (task.getPriority() != Task.Priority.Important) {
 								task.setPriority(Task.Priority.Important);
 							}
 							break;
 						case 2:
 							if (task.getPriority() != Task.Priority.Normal) {
 								task.setPriority(Task.Priority.Normal);
 							}
 							break;
 						case 3:
 							if (task.getPriority() != Task.Priority.Low) {
 								task.setPriority(Task.Priority.Low);
 							}
 							break;
 						}
 						if (pIni != task.getPriority()) {
 							updateView(activity);
 						}
 					}
 				}
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 				}
 			});
 		}
 
 		this.toggleButton_taskDiscarded = (ToggleButton)getView(R.id.task_status_toggle);
 		if (this.toggleButton_taskDiscarded != null) {
 			this.toggleButton_taskDiscarded.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					if (isCallbacksEnabled()) {
 						if (!isChecked) {
 							if (task.getStatus() == Task.Status.Completed) {
 								task.setStatus(Task.Status.Discarded_Completed);
 							} else if (task.getStatus() == Task.Status.Active) {
 								task.setStatus(Task.Status.Discarded);
 							}
 						} else {
 							if (task.getStatus() == Task.Status.Discarded_Completed) {
 								task.setStatus(Task.Status.Completed);
 							} else if (task.getStatus() == Task.Status.Discarded) {
 								task.setStatus(Task.Status.Active);
 							}
 						}
 						updateView(activity);
 					}
 				}
 			});
 		}
 
 		this.textView_taskDueDate = (TextView)getView(R.id.task_duedate);
 		this.button_changeDueDate = (Button)getView(R.id.button_changeDueDate);
 		if (this.button_changeDueDate != null) {
 			this.button_changeDueDate.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Calendar c = Calendar.getInstance();
					if (task.getDueDate() != null) {
 						c.setTime(task.getDueDate());
					}
 					int mYear = c.get(Calendar.YEAR);
 					int mMonth = c.get(Calendar.MONTH);
 					int mDay = c.get(Calendar.DAY_OF_MONTH);
 					new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
 
 						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 							Calendar c = Calendar.getInstance();
 							if (task.getDueDate() != null) {
 								c.setTime(task.getDueDate());
 							} else {
 								c.set(Calendar.HOUR_OF_DAY, 0);
 								c.set(Calendar.MINUTE, 0);
 							}
 							c.set(Calendar.YEAR, year);
 							c.set(Calendar.MONTH, monthOfYear);
 							c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 							task.setDueDate(c.getTime());
 
 							updateView(activity);
 						}
 					}, mYear, mMonth, mDay).show();
 					
 					Log.d(TaskManager.TAG, "Due date set");
 				}
 			});
 		}
 
 		this.textView_taskDueTime = (TextView)getView(R.id.task_duetime);
 		this.button_changeDueTime = (Button)getView(R.id.button_changeDueTime);
 		if (this.button_changeDueTime != null) {
 			this.button_changeDueTime.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Calendar c = Calendar.getInstance();
 					if (task.getDueDate() != null) {
 						c.setTime(task.getDueDate());
 					} else {
 						c.set(Calendar.MINUTE, 0);
 					}
 					int mMinute = c.get(Calendar.MINUTE);
 					int mHour = c.get(Calendar.HOUR_OF_DAY);
 					new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
 						@Override
 						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 							Calendar c = Calendar.getInstance();
 							if (task.getDueDate() != null) {
 								c.setTime(task.getDueDate());
 							} else {
 								c.set(Calendar.YEAR, c.get(Calendar.YEAR));
 								c.set(Calendar.MONTH, c.get(Calendar.MONTH));
 								c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
 							}
 							c.set(Calendar.HOUR_OF_DAY, hourOfDay);
 							c.set(Calendar.MINUTE, minute);
 							task.setDueDate(c.getTime());
 
 							updateView(activity);
 						}
 					}, mHour, mMinute, true).show();
 					
 					Log.d(TaskManager.TAG, "Due time set");
 				}
 			});
 		}
 
 		this.textView_locationLat = (TextView)getView(R.id.task_location_lat);
 		this.textView_locationLong = (TextView)getView(R.id.task_location_long);
 		this.button_changeMapPosition = (Button)getView(R.id.button_changeMapPosition);
 		if (this.button_changeMapPosition != null) {
 			this.button_changeMapPosition.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					ActivityUtils.showMapActivity(activity, task);
 					Log.d(TaskManager.TAG, "Location set");
 				}
 			});
 		}
 
 
 		this.button_takePicture = (Button)getView(R.id.task_take_picture);
 		if (this.button_takePicture != null) {
 			this.button_takePicture.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					ActivityUtils.callDefaultCameraApp(activity);
 				}
 			});
 		}
 
 		this.button_deletePicture = (Button)getView(R.id.task_delete_picture);
 		if (this.button_deletePicture != null) {
 			this.button_deletePicture.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					task.setPicture(null);
 					updateView(activity);
 					Log.d(TaskManager.TAG, "Picture deleted");
 				}
 			});
 		}
 
 		this.imageView_picture = (ImageView)getView(R.id.task_picture);
 
 		this.layout_taskIcons_1 = (LinearLayout)getView(R.id.task_icons_1);
 		this.layout_taskIcons_2 = (LinearLayout)getView(R.id.task_icons_2);
 	}
 
 	@Override
 	public void updateView(Activity activity) {
 
 		if (this.isCallbacksEnabled()) {
 
 			boolean disableCallbacks = false;
 
 			if (this.textView_taskName == null && this.editText_taskName == null) {
 				disableCallbacks = true;
 				this.setCallbacksEnabled(false);
 
 				this.setUpView(activity);
 			}
 
 			final Resources res = this.view.getResources();
 
 			if (this.textView_taskName != null || this.editText_taskName != null) {
 				if (this.editText_taskName != null) {
 					this.editText_taskName.setText(task.getName());
 				} else if (this.textView_taskName != null){
 					this.textView_taskName.setText(task.getName());
 				}
 				if (task.getDescription() != null && !task.getDescription().equalsIgnoreCase("")) {
 					if (this.editText_taskDescription != null) {
 						this.editText_taskDescription.setText(task.getDescription());
 					} else if (this.textView_taskDescription != null) {
 						this.textView_taskDescription.setText(task.getDescription());
 					}
 				}
 
 				if (this.textView_taskName != null && this.textView_taskDescription != null) {
 					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 					lp.addRule(RelativeLayout.RIGHT_OF, R.id.task_status_check);
 					if (task.getDescription() == null || task.getDescription().equalsIgnoreCase("")) {
 						this.textView_taskDescription.setVisibility(View.GONE);
 						lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
 					} else {
 						this.textView_taskDescription.setVisibility(View.VISIBLE);
 						this.textView_taskName.setPadding(0, 7, 0, 0);
 					}
 					this.textView_taskName.setLayoutParams(lp);
 				}
 			}
 
 			if (this.imageViewCheckBox_taskStatus != null) {
 				boolean callbacksEnabled = this.isCallbacksEnabled();
 				this.setCallbacksEnabled(false);
 
 				if (task.getStatus() == Task.Status.Completed) {
 					this.imageViewCheckBox_taskStatus.setImageResource(R.drawable.btn_check_on);	
 				} else if (task.getStatus() == Task.Status.Discarded_Completed) {
 					this.imageViewCheckBox_taskStatus.setImageResource(R.drawable.btn_check_on_disable);
 				} else if (task.getStatus() == Task.Status.Active) {
 					this.imageViewCheckBox_taskStatus.setImageResource(R.drawable.btn_check_off);
 				} else if (task.getStatus() == Task.Status.Discarded) {
 					this.imageViewCheckBox_taskStatus.setImageResource(R.drawable.btn_check_off_disable);
 				}
 
 				this.imageViewCheckBox_taskStatus.requestLayout();
 
 				this.setCallbacksEnabled(callbacksEnabled);
 			}
 
 			if (this.spinner_taskPriority != null) {
 				switch (task.getPriority()) {
 				case Low:
 					this.spinner_taskPriority.setSelection(3);
 					break;
 				case Normal:
 					this.spinner_taskPriority.setSelection(2);
 					break;
 				case Important:
 					this.spinner_taskPriority.setSelection(1);
 					break;
 				case Critical:
 					this.spinner_taskPriority.setSelection(0);
 					break;
 				}
 
 			}
 
 			if (this.toggleButton_taskDiscarded != null) {
 				this.toggleButton_taskDiscarded.setChecked(task.getStatus() != Task.Status.Discarded && task.getStatus() != Task.Status.Discarded_Completed);
 			}
 
 			if (this.layout_taskIcons_1 != null) {
 				this.layout_taskIcons_1.removeAllViews();
 				this.layout_taskIcons_2.removeAllViews();
 
 				ArrayList<View> views = new ArrayList<View>();
 
 				int i = 0;
 				
 				if (this.task.getDueDate() != null && this.task.getDueDate().getTime() > System.currentTimeMillis()) {
 					views.add(this.newTaskIcon(R.drawable.stat_notify_alarm_little, (i++ % 2) == 0));
 				}
 				if (this.task.getPicture() != null) {
 					views.add(this.newTaskIcon(R.drawable.ic_menu_camera_little, (i++ % 2) == 0));
 				}
 				if (this.task.getLocation() != null) {
 					views.add(this.newTaskIcon(R.drawable.stat_sys_gps_on_little, (i++ % 2) == 0));
 				}
 
 				if (views.size() > 0) {
 					this.layout_taskIcons_1.setVisibility(View.VISIBLE);
 
 					if (views.size() <= 2) {
 						this.layout_taskIcons_2.setVisibility(View.GONE);
 					} else {
 						this.layout_taskIcons_2.setVisibility(View.VISIBLE);
 					}
 				} else {
 					this.layout_taskIcons_1.setVisibility(View.GONE);
 					this.layout_taskIcons_2.setVisibility(View.GONE);
 				}
 
 				for (i = 0; i<views.size(); i++) {
 					if (i < 2) {
 						this.layout_taskIcons_1.addView(views.get(i));
 					} else {
 						this.layout_taskIcons_2.addView(views.get(i));
 					}
 				}
 			}
 
 			if (this.textView_taskDueDate != null && this.textView_taskDueTime != null) { 
 
 				if (task.getDueDate() != null) {
 					Calendar c = Calendar.getInstance();
 					c.setTime(task.getDueDate());
 
 					int mYear = c.get(Calendar.YEAR);
					int mMonth = c.get(Calendar.MONTH) + 1;
 					int mDay = c.get(Calendar.DAY_OF_MONTH);
 					int mHour = c.get(Calendar.HOUR_OF_DAY);
 					int mMinute = c.get(Calendar.MINUTE);
 
 					this.textView_taskDueDate.setText(
 							new StringBuilder()
							.append(mYear).append("/")
 							.append(mMonth <= 9 ? "0" + mMonth : mMonth).append("/")
 							.append(mDay <= 9 ? "0" + mDay : mDay));
 
 					this.textView_taskDueTime.setText(
 							new StringBuilder()
 							.append(mHour <= 9 ? "0" + mHour : mHour).append(":")
 							.append(mMinute <= 9 ? "0" + mMinute : mMinute));
 				} else {
 					this.textView_taskDueDate.setText("-");
 					this.textView_taskDueTime.setText("-");
 				}
 
 			}
 
 			if (this.textView_locationLat != null && this.textView_locationLong != null) {
 				if (task.getLocation() != null) {
 					this.textView_locationLat.setText("" + (task.getLocation().getLatitudeE6() / 1E6));
 					this.textView_locationLong.setText("" + (task.getLocation().getLongitudeE6() / 1E6));
 				} else {
 					this.textView_locationLat.setText("-");
 					this.textView_locationLong.setText("-");
 				}
 			}
 
 			if (this.button_deletePicture != null) {
 				this.button_deletePicture.setEnabled(task.getPicture() != null);
 			}
 
 			if (this.imageView_picture != null) {
 				byte[] bb = task.getPicture();
 				if (bb != null) {
 					this.imageView_picture.setVisibility(View.VISIBLE);
 					this.imageView_picture.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));	
 				} else {
 					this.imageView_picture.setVisibility(View.GONE);
 				}
 
 			}
 
 			if (task.getStatus() == Task.Status.Discarded || task.getStatus() == Task.Status.Discarded_Completed) {
 				if (getView(R.id.task_priority) != null) {
 					getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityDiscarded);
 				}
 				if (getView(R.id.task_priority_big) != null) {
 					getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityDiscarded);
 				}
 				if (getView(R.id.task_name) != null) {
 					((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityDiscarded));
 				}
 				for (TextView label: this.textViews_labels) {
 					label.setTextColor(res.getColor(R.color.Task_PriorityDiscarded));
 				}
 
 				setInterfaceEnabled(false);
 			} else {
 				switch(task.getPriority()) {
 				case Low: 
 					if (getView(R.id.task_priority) != null) {
 						getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityLow);
 					}
 					if (getView(R.id.task_priority_big) != null) {
 						getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityLow);
 					}
 					if (getView(R.id.task_name) != null) {
 						((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityLow));
 					}
 					for (TextView label: this.textViews_labels) {
 						label.setTextColor(res.getColor(R.color.Task_PriorityLow));
 					}
 					break;
 				case Normal:
 					if (getView(R.id.task_priority) != null) {
 						getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityNormal);
 					}
 					if (getView(R.id.task_priority_big) != null) {
 						getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityNormal);
 					}
 					if (getView(R.id.task_name) != null) {
 						((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityNormal));
 					}
 					for (TextView label: this.textViews_labels) {
 						label.setTextColor(res.getColor(R.color.Task_PriorityNormal));
 					}
 					break;
 				case Important: 
 					if (getView(R.id.task_priority) != null) {
 						getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityImportant);
 					}
 					if (getView(R.id.task_priority_big) != null) {
 						getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityImportant);
 					}
 					if (getView(R.id.task_name) != null) {
 						((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityImportant));
 					}
 					for (TextView label: this.textViews_labels) {
 						label.setTextColor(res.getColor(R.color.Task_PriorityImportant));
 					}
 					break;
 				case Critical: 
 					if (getView(R.id.task_priority) != null) {
 						getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityCritical);
 					}
 					if (getView(R.id.task_priority_big) != null) {
 						getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityCritical);
 					}
 					if (getView(R.id.task_name) != null) {
 						((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityCritical));
 					}
 					for (TextView label: this.textViews_labels) {
 						label.setTextColor(res.getColor(R.color.Task_PriorityCritical));
 					}
 					break;
 				}
 
 				setInterfaceEnabled(true);
 			}
 
 			this.view.requestLayout();
 
 			if (disableCallbacks) {
 				this.setCallbacksEnabled(true);
 			}
 		}
 	}
 
 	private void setInterfaceEnabled(boolean enabled) {
 		if (this.imageViewCheckBox_taskStatus != null) 
 			this.imageViewCheckBox_taskStatus.setEnabled(enabled);
 		if (this.editText_taskName != null) {
 			this.editText_taskName.setEnabled(enabled);
 			this.editText_taskName.setInputType(enabled ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
 			this.editText_taskName.setCursorVisible(enabled); 
 		}
 
 		if (this.editText_taskDescription != null) {
 			this.editText_taskDescription.setEnabled(enabled);
 			this.editText_taskDescription.setInputType(enabled ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
 			this.editText_taskDescription.setCursorVisible(enabled); 
 		}
 		if (this.button_taskDescriptionClear != null)
 			this.button_taskDescriptionClear.setEnabled(enabled);
 
 		if (this.spinner_taskPriority != null)
 			this.spinner_taskPriority.setEnabled(enabled);
 
 		if (this.button_takePicture != null) 
 			this.button_takePicture.setEnabled(enabled);
 		if (this.button_deletePicture != null) 
 			this.button_deletePicture.setEnabled(enabled && task.getPicture() != null);
 
 		if (this.button_changeDueDate != null)
 			this.button_changeDueDate.setEnabled(enabled);
 		if (this.button_changeDueTime != null)
 			this.button_changeDueTime.setEnabled(enabled);
 
 		if (this.button_changeMapPosition != null)
 			this.button_changeMapPosition.setEnabled(enabled);
 	}
 
 	private ImageView newTaskIcon(int drawableId, boolean setMargins) {
 		ImageView icon = new ImageView(view.getContext());
 		icon.setImageResource(drawableId);
 
 		icon.setAlpha(128);
 
 //		Matrix matrix = new Matrix();
 //		if (setMargins) {
 //			matrix.postScale(0.6f, 0.6f);
 //		} else {
 //			matrix.postScale(0.7f, 0.7f);
 //		}
 //		icon.setImageMatrix(matrix);
 //		icon.setScaleType(ScaleType.MATRIX);
 
 		LayoutParams frame = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 //		frame.setMargins(0, 0, 0, 0);
 //		frame.width = (int) (38 * 0.5);
 //		frame.height = (int) (38 * 0.5);
 		if (setMargins) {
 			frame.setMargins(0, 0, 0, 5);
 		}
 		icon.setLayoutParams(frame);
 //		icon.setBackgroundColor(Color.BLUE);
 
 		return icon;
 	}
 
 }
