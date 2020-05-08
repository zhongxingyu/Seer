 /*
  * Copyright (C) 2011-2012 AlarmApp.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.alarmapp.model.classes;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.alarmapp.AlarmApp;
 import org.alarmapp.model.Alarm;
 import org.alarmapp.model.AlarmState;
 import org.alarmapp.model.AlarmedUser;
 import org.alarmapp.model.BindableConverter;
 import org.alarmapp.util.BundleUtil;
 import org.alarmapp.util.CollectionUtil;
 import org.alarmapp.util.DateUtil;
 import org.alarmapp.util.Ensure;
 import org.alarmapp.util.LogEx;
 
 import android.os.Bundle;
 
 public class AlarmData implements Alarm {
 
 	private static final String IS_ALARMSTATUS_VIEWER = "is_alarmstatus_viewer";
 
 	private static final String ALARMED_USER_LIST = "alarmed_user_list";
 
 	private static final String OPERATION_STATUS = "operation_status";
 
 	private static final String TEXT = "text";
 
 	private static final String TITLE = "title";
 
 	private static final String ALARMED = "alarmed";
 
 	private static final String OPERATION_ID = "operation_id";
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private String operation_id;
 	private Date alarmed;
 	private String title;
 	private String text;
 	private AlarmState state = AlarmState.Delivered;
 	private boolean isAlarmstatusViewer = false;
 	private HashMap<String, String> extraValues = new HashMap<String, String>();
 
 	private static Set<String> keySet = CollectionUtil.asSet(OPERATION_ID,
 			ALARMED, TITLE, TEXT, OPERATION_STATUS, ALARMED_USER_LIST,
 			IS_ALARMSTATUS_VIEWER);
 
 	public Bundle getBundle() {
 		Bundle b = new Bundle();
 		b.putString(TITLE, this.title);
 		b.putString(TEXT, this.text);
 		b.putString(ALARMED, DateUtil.format(this.alarmed));
 		b.putString(OPERATION_ID, this.operation_id);
 		b.putInt(OPERATION_STATUS, state.getId());
 		if (this.alarmedUsers != null) {
			b.putSerializable(ALARMED_USER_LIST, this.alarmedUsers); //todo: Wrap this in a new HashSet<AlarmedUsers>()
 		}
 		b.putString(IS_ALARMSTATUS_VIEWER,
 				Boolean.toString(isAlarmstatusViewer));
 
 		LogEx.verbose("AlarmData Extra status is " + b.getInt(OPERATION_STATUS));
 
 		for (String key : extraValues.keySet()) {
 			b.putString(key, extraValues.get(key));
 		}
 
 		return b;
 	}
 
 	private AlarmData() {
 
 	}
 
 	public AlarmData(String operationId, Date alarmed, String title,
 			String text, AlarmState state, Map<String, String> extras) {
 		this.operation_id = operationId;
 		this.state = state;
 		this.text = text;
 		this.title = title;
 		this.alarmed = alarmed;
 
 		if (extras != null)
 			this.extraValues = new HashMap<String, String>(extras);
 		else
 			this.extraValues = new HashMap<String, String>();
 	}
 
 	public static boolean isAlarmDataBundle(Bundle extra) {
 		return BundleUtil
 				.containsAll(extra, OPERATION_ID, TITLE, TEXT, ALARMED);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static AlarmData create(Bundle extra) {
 
 		AlarmData a = new AlarmData();
 
 		a.operation_id = extra.getString(OPERATION_ID);
 		a.alarmed = DateUtil.parse(extra.getString(ALARMED));
 		a.title = extra.getString(TITLE);
 		a.text = extra.getString(TEXT);
 		if (extra.containsKey(ALARMED_USER_LIST)) {
 			a.alarmedUsers = (HashSet<AlarmedUser>) extra
 					.getSerializable(ALARMED_USER_LIST);
 		}
 		if (extra.containsKey(IS_ALARMSTATUS_VIEWER))
 			a.isAlarmstatusViewer = Boolean.parseBoolean(extra.getString(
 					IS_ALARMSTATUS_VIEWER).toLowerCase());
 
 		LogEx.verbose("AlarmData Extra status is "
 				+ extra.getInt(OPERATION_STATUS));
 
 		if (extra.containsKey(OPERATION_STATUS))
 			a.state = AlarmState.create(extra.getInt(OPERATION_STATUS));
 
 		for (String key : extra.keySet()) {
 			if (isExtra(key)) {
 				a.extraValues.put(key, extra.getString(key));
 				LogEx.debug("Additional Data " + key + " = "
 						+ extra.getString(key));
 			}
 		}
 		return a;
 	}
 
 	private static boolean isExtra(String key) {
 		return !keySet.contains(key);
 	}
 
 	public String getTitle() {
 		return this.title;
 	}
 
 	public String getText() {
 		return this.text;
 	}
 
 	public String getOperationId() {
 		return this.operation_id;
 	}
 
 	public Date getAlarmed() {
 		return this.alarmed;
 	}
 
 	public Map<String, String> getAdditionalValues() {
 		return Collections.unmodifiableMap(this.extraValues);
 	}
 
 	public boolean isFinal() {
 
 		return this.state.isFinal();
 	}
 
 	public AlarmState getState() {
 
 		return this.state;
 	}
 
 	public void setState(AlarmState newState) {
 		if (this.state == newState)
 			return;
 
 		LogEx.info("Actual State is: " + this.state.getName()
 				+ " New State is: " + newState.getName());
 
 		Ensure.valid(this.state.isNext(newState));
 
 		this.state = newState;
 
 		LogEx.info("Operation" + this.getOperationId() + " has now State "
 				+ this.state.getName());
 	}
 
 	private HashSet<AlarmedUser> alarmedUsers;
 
 	public Set<AlarmedUser> getAlarmedUsers() {
 		if (alarmedUsers == null)
 			return new HashSet<AlarmedUser>();
 		return alarmedUsers;
 	}
 
 	public void setAlarmedUsers(Set<AlarmedUser> users) {
 		if (users != null)
 			this.alarmedUsers = new HashSet<AlarmedUser>(users);
 		else
 			this.alarmedUsers = new HashSet<AlarmedUser>();
 	}
 
 	public void updateAlarmedUser(AlarmedUser user) {
 		if (alarmedUsers == null)
 			this.alarmedUsers = new HashSet<AlarmedUser>();
 
 		if (alarmedUsers.contains(user))
 			alarmedUsers.remove(user);
 		alarmedUsers.add(user);
 	}
 
 	public synchronized void save() {
 		AlarmApp.getAlarmStore().put(this);
 		AlarmApp.getAlarmStore().save();
 	}
 
 	public boolean isAlarmStatusViewer() {
 		return isAlarmstatusViewer;
 	}
 
 	public BindableConverter<AlarmData> getConverter() {
 		return new BindableConverter<AlarmData>() {
 
 			public boolean canConvert(Bundle b) {
 				return isAlarmDataBundle(b);
 			}
 
 			public Bundle convert(AlarmData obj) {
 				return obj.getBundle();
 			}
 
 			public AlarmData convert(Bundle b) {
 				return AlarmData.create(b);
 			}
 		};
 	}
 }
