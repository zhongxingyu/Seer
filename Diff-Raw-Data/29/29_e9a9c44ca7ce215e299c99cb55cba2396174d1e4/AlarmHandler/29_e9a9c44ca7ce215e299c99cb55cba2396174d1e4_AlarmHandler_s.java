 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugrd, Andreas Roln
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
 package edu.chalmers.dat255.group09.Alarmed.database;
 
 import java.util.List;
 
import android.database.Cursor;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 
 public interface AlarmHandler {
 
 	/**
 	 * Open the connection to the handler.
 	 * 
 	 * @return this, a self reference
 	 */
 
 	public AlarmHandler openCon();
 
 	/**
 	 * Closes the connection to the handler
 	 */
 	public void closeCon();
 
 	/**
 	 * Creates an alarm in the handler given the time and if it should be
 	 * recurring.
 	 * 
 	 * @param hour
 	 *            The hour which the alarm has been set to trigger on
 	 * @param minute
 	 *            The minute which the alarm has been set to trigger on
 	 * @param recurring
 	 *            True if the alarm is set to be recurring.
 	 * @return the id of the created Alarm, or -1 if an error occurred
 	 */
 
 	public long createAlarm(int hour, int minute, boolean recurring);
 
 	/**
 	 * Deletes an Alarm with a specified id.
 	 * 
 	 * @param alarmID
 	 *            id of the alarm to be deleted.
 	 * @return true if deleted else false
 	 */
 
 	public abstract boolean deleteAlarm(int alarmID);
 
 
 	/**
 	 * Fetches the first alarm given by the time to the next occurring alarm.
 	 * 
 	 * @return The next alarm to activate.
 	 */
 
 	public Alarm fetchFirstEnabledAlarm();
 
 	/**
 	 * Fetches all the alarm data and makes a list of Alarms
 	 * 
 	 * @return A list of all the alarms
 	 */
 
 	public List<Alarm> fetchAllAlarms();
 
 	/**
 	 * Fetches an alarm from the handler given an specified id.
 	 * 
 	 * @param alarmID
 	 *            the id of the alarm to retrieve
 	 * @return the alarm with the specified id, if not found null
 	 */
 	public Alarm fetchAlarm(int alarmID);
 
 	/**
 	 * 
 	 * @return The number of alarms.
 	 */
 
 	public int getNumberOfAlarms();
 
 	/**
 	 * 
 	 * @param id
 	 *            The id of the alarm
 	 * @return true if the alarm is enabled
 	 */
 
 	public boolean isEnabled(int id);
 
 	/**
 	 * Updates the alarm given the id and the state to set the alarm to
 	 * 
 	 * @param id
 	 *            The id of the alarm
 	 * @param enable
 	 *            the state to set the alarm to
 	 * @return true if changed
 	 */
 
 	public boolean setAlarmEnabled(int id, boolean enable);
 
 }
