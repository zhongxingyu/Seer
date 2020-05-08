 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
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
 package edu.chalmers.dat255.group09.Alarmed.test.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.test.AndroidTestCase;
 import edu.chalmers.dat255.group09.Alarmed.controller.AlarmController;
 import edu.chalmers.dat255.group09.Alarmed.database.AlarmHandler;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 
 /**
  * 
  * @author Daniel Augurell
  * @author Joakim Persson
  * 
  */
 public class AlarmControllerTest extends AndroidTestCase {
 	private AlarmController ac;
 	private Context context;
 	private AlarmHandler handler;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		context = getContext();
 		ac = AlarmController.getInstance();
 		handler = new MockAlarmHandler().openCon();
 		ac.init(context, handler);
		ac.createAlarm(10, 10, 0, "", 0);
		ac.createAlarm(20, 20, 0, "", 0);
 	}
 
 	public void testDestroy() {
 		ac.destroy();
 		assertNull(handler.fetchAllAlarms());
 	}
 
 	public void testDeleteAlarm() {
 		assertTrue(ac.deleteAlarm(2));
 		assertNull(handler.fetchAlarm(2));
 		assertFalse(ac.deleteAlarm(2));
 	}
 
 	public void testGetAllAlarms() {
 		List<Alarm> list = ac.getAllAlarms();
 		assertEquals(2, list.size());
 		for (Alarm alarm : list) {
 			assertNotNull(alarm);
 		}
 	}
 
 	public void testAlarmReceived() {
 		assertFalse(ac.alarmReceived(3));
 		ac.alarmReceived(2);
 		assertFalse(handler.isEnabled(2));
 	}
 
 	public void testAlarmEnabled() {
 		assertTrue(ac.enableAlarm(1, false));
 		assertFalse(ac.enableAlarm(2, true));
 		assertFalse(ac.isAlarmEnabled(1));
 	}
 
 	public void testCreateAlarm() {
 		assertNotNull(handler.fetchAlarm(1));
 		assertNotNull(handler.fetchAlarm(2));
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		ac = null;
 	}
 
 	/**
 	 * The MockAlarmHandler is an handler which instead of using a database is
 	 * using a list to represent the stored values. The biggest difference
 	 * between this class and a regular implementation of the interface is that
 	 * the fetchFirstEnabledAlarm is returning a null value because of the code
 	 * not able to run tests on.
 	 * 
 	 * @author Daniel Augurell
 	 * 
 	 */
 	private class MockAlarmHandler implements AlarmHandler {
 		private int nbrID;
 		private List<Alarm> alarms;
 
 		public AlarmHandler openCon() {
 			alarms = new ArrayList<Alarm>();
 			return this;
 		}
 
 		public void closeCon() {
 			alarms = null;
 			nbrID = 0;
 		}
 
		public long createAlarm(int hour, int minute, int daysOfWeek, String module, int volume) {
			Alarm alarm = new Alarm(hour, minute, ++nbrID, module, volume);
			alarms.add(alarm);
			return alarm.getId();
 		}
 
 		public boolean deleteAlarm(int alarmID) {
 			for (Alarm alarm : alarms) {
 				if (alarm.getId() == alarmID) {
 					alarms.remove(alarm);
 					return true;
 				}
 			}
 			return false;
 		}
 
 		public Alarm fetchFirstEnabledAlarm() {
 			return null;
 		}
 
 		public List<Alarm> fetchAllAlarms() {
 			return alarms;
 		}
 
 		public Alarm fetchAlarm(int alarmID) {
 			for (Alarm alarm : alarms) {
 				if (alarm.getId() == alarmID) {
 					return alarm;
 				}
 			}
 			return null;
 		}
 
 		public int getNumberOfAlarms() {
 			return alarms.size();
 		}
 
 		public boolean isEnabled(int id) {
 			for (Alarm alarm : alarms) {
 				if (alarm.getId() == id) {
 					return alarm.isEnabled();
 				}
 			}
 			return false;
 		}
 
 		public boolean setAlarmEnabled(int id, boolean enable) {
 
 			for (Alarm alarm : alarms) {
 				if (alarm.getId() == id) {
 					boolean change = alarm.isEnabled() != enable;
 					alarm.setEnabled(enable);
 					return change;
 				}
 			}
 			return false;
 		}
 
 	}
 }
