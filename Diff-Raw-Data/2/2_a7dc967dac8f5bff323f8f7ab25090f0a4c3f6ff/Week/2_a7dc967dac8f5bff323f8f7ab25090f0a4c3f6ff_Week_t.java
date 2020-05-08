 package de.Psychologie.socialintelligence;
 
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 
 public class Week extends Activity {
 
 	private Button timeslot1, timeslot2, timeslot3, timeslot4, timeslot5,
 			timeslot6, timeslot7, timeslot8;
 	private Button timeslot9, timeslot10, timeslot11, timeslot12, timeslot13,
 			timeslot14, timeslot15;
 	private Button mon, tue, wed, thur, fri, sat, sun;
 	private Button saveWeek;
 	// Zeitslots verwalten
 	private Button[] ButtonHandler = new Button[15];
 	// Tage verwalten
 	private Day week[] = new Day[7];
 	// aktueller Tag
 	private Day currentDay;
 	// Ansicht gespeichert
 	private boolean saveAllTimeSlots = true;
 	// Alarm
 	private Alarm alarm;
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_week);
 
 		// Actionbar mit Zurueckknopf versehen
 		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
 		actionBar.setHomeAction(new IntentAction(this, new Intent(Week.this,
 				UserSettingActivity.class), R.drawable.back_button));
 
 		// //////////////////////////////////////////////////////////////////////
 		// Buttons definieren
 		// //////////////////////////////////////////////////////////////////////
 		// fetch Buttons times
 		timeslot1 = (Button) findViewById(R.id.timeslot1);
 		ButtonHandler[0] = timeslot1;
 		timeslot2 = (Button) findViewById(R.id.timeslot2);
 		ButtonHandler[1] = timeslot2;
 		timeslot3 = (Button) findViewById(R.id.timeslot3);
 		ButtonHandler[2] = timeslot3;
 		timeslot4 = (Button) findViewById(R.id.timeslot4);
 		ButtonHandler[3] = timeslot4;
 		timeslot5 = (Button) findViewById(R.id.timeslot5);
 		ButtonHandler[4] = timeslot5;
 		timeslot6 = (Button) findViewById(R.id.timeslot6);
 		ButtonHandler[5] = timeslot6;
 		timeslot7 = (Button) findViewById(R.id.timeslot7);
 		ButtonHandler[6] = timeslot7;
 		timeslot8 = (Button) findViewById(R.id.timeslot8);
 		ButtonHandler[7] = timeslot8;
 		timeslot9 = (Button) findViewById(R.id.timeslot9);
 		ButtonHandler[8] = timeslot9;
 		timeslot10 = (Button) findViewById(R.id.timeslot10);
 		ButtonHandler[9] = timeslot10;
 		timeslot11 = (Button) findViewById(R.id.timeslot11);
 		ButtonHandler[10] = timeslot11;
 		timeslot12 = (Button) findViewById(R.id.timeslot12);
 		ButtonHandler[11] = timeslot12;
 		timeslot13 = (Button) findViewById(R.id.timeslot13);
 		ButtonHandler[12] = timeslot13;
 		timeslot14 = (Button) findViewById(R.id.timeslot14);
 		ButtonHandler[13] = timeslot14;
 		timeslot15 = (Button) findViewById(R.id.timeslot15);
 		ButtonHandler[14] = timeslot15;
 
 		// fetch Buttons week
 		mon = (Button) findViewById(R.id.mon);
 		tue = (Button) findViewById(R.id.tue);
 		wed = (Button) findViewById(R.id.wed);
 		thur = (Button) findViewById(R.id.thur);
 		fri = (Button) findViewById(R.id.fri);
 		sat = (Button) findViewById(R.id.sat);
 		sun = (Button) findViewById(R.id.sun);
 
 		// Alarm erzeugen
 		alarm = new Alarm(this);
 
 		// //////////////////////////////////////////////////////////////////////
 		// Methoden OnClick
 		// //////////////////////////////////////////////////////////////////////
 
 		// Week
 		enableButton(mon, 0);
 		enableButton(tue, 0);
 		enableButton(wed, 0);
 		enableButton(thur, 0);
 		enableButton(fri, 0);
 		enableButton(sat, 0);
 		enableButton(sun, 0);
 
 		// Row1
 		enableButton(timeslot1, 1);
 		enableButton(timeslot2, 1);
 		enableButton(timeslot3, 1);
 		enableButton(timeslot4, 1);
 		// Row2
 		enableButton(timeslot5, 2);
 		enableButton(timeslot6, 2);
 		enableButton(timeslot7, 2);
 		// Row3
 		enableButton(timeslot8, 3);
 		enableButton(timeslot9, 3);
 		enableButton(timeslot10, 3);
 		enableButton(timeslot11, 3);
 		// Row4
 		enableButton(timeslot12, 4);
 		enableButton(timeslot13, 4);
 		enableButton(timeslot14, 4);
 		enableButton(timeslot15, 4);
 
 		// //////////////////////////////////////////////////////////////////////
 		// start Activity
 		// //////////////////////////////////////////////////////////////////////
 
 		// Standardeinstellungen
 		// Alle deaktivieren
 		disableWeek();
 		disableAllTimeSlots();
 		// TODO: kann weg oder? Disable all
 		//
 
 		// Zeitdaten aus der Datenbank holen
 		if (getWeekFromDatabase()) {
 			// Zeiten w�hlbar
 			activateAllTimes(true);
 			// Woche auslesen
 			for (int i = 0; i < week.length; i++) {
 				if (week[i] != null) {
 					// Wochentage zur�cksetzen
 					disableWeek();
 					// aktuellen Wochentag makieren
 					setButtonSelect((Button) findViewById(Day
 							.getViewIDfromWeekID(i)));
 					currentDay = week[i];
 					// Zeitslots zur�cksetzen
 					disableAllTimeSlots();
 					for (int j = 0; j < week[i].getTimeSlotsButton().length; j++) {
 						// setze Zeitslots f�r diesen Tag
 						setButtonSelect(week[i].getTimeSlotsButton()[j]);
 					}
 				}
 			}
 		}
 
 		// Aktuellen Wochentag anklicken
 		clickCurrentDay();
 
 		// //////////////////////////////////////////////////////////////////////
 		// save Week
 		// //////////////////////////////////////////////////////////////////////
 
 		saveWeek = (Button) findViewById(R.id.saveWeek);
 		saveWeek.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_green));
 		saveWeek.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Daten in Datenbank �berf�hren
 				// TODO: Make faster
 				writeWeekToDatabase();
 				// Alle Einstellungen erfolgreich gespeichert
 				saveAllTimeSlots = true;
 				// Alarm setzen
 				// TODO: Wann soll er dies immer setzen?
 				SQLHandler db = new SQLHandler(Week.this);
 				if (!db.getSnoozeActiv()) {
 					alarm.setNextAlarm(true);
 				}
 				db.close();
 				// Meldung ausgeben
 				Toast.makeText(
 						Week.this,
 						getResources().getString(R.string.txtWeekSaveTimeSlots),
 						Toast.LENGTH_SHORT).show();
 				// Weiter zu den Einstellungen
 				onBackPressed();				
 			}
 		});
 
 		ImageButton headerButton = (ImageButton) findViewById(R.id.actionbar_home_btn);
 		headerButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				onBackPressed();
 			}
 		});
 	}
 
 	@Override
 	public void onBackPressed() {
 		// wurde alles gespeichert?
 		if (saveAllTimeSlots) {
 			// go back to Settingsactivity
 			if (Week.this.isTaskRoot()) {
 				Week.this.startActivity(new Intent(Week.this,
 						UserSettingActivity.class));
 				Week.this.finish();
 			}
 			Week.super.onBackPressed();
 		} else {
 			// �nderungen verwerfen?
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(getResources()
 					.getString(R.string.txtWeekSaveTitle));
 			builder.setMessage(
 					getResources().getString(R.string.txtWeekSaveQuestion))
 					.setCancelable(false)
 					.setPositiveButton(
 							getResources().getString(R.string.txtYes),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// go back to Settingsactivity
 									if (Week.this.isTaskRoot()) {
 										Week.this.startActivity(new Intent(
 												Week.this,
 												UserSettingActivity.class));
 										Week.this.finish();
 									}
 									Week.super.onBackPressed();
 								}
 							})
 					.setNegativeButton(
 							getResources().getString(R.string.txtNo),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 		overridePendingTransition(0, 0);
 	}
 
 	// //////////////////////////////////////////////////////////////////////
 	// Private Methoden
 	// //////////////////////////////////////////////////////////////////////
 
 	// aktiviert nur den selektierten Button, alle anderen in der Zeile werden
 	// disabled
 	private void enableButton(final Button bnt, final int row) {
 		bnt.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				switch (row) {
 				case 0:
 					// korrekten Wochentag markieren
 					disableWeek();
 					// Auswahl zur�cksetzen
 					disableAllTimeSlots();
 					// existiert Tag bereits?
 					if (existDay(bnt.getId())) {
 						// dann setze alle Timeslots
 						for (int i = 0; i < 4; i++) {
 							if (week[Day.getWeekIDfromViewID(bnt.getId())]
 									.getTimeSlotsButton()[i] != null) {
 								setButtonSelect(week[Day
 										.getWeekIDfromViewID(bnt.getId())]
 										.getTimeSlotsButton()[i]);
 							}
 						}
 						// aktuellen Tag merken
 						currentDay = week[Day.getWeekIDfromViewID(bnt.getId())];
 						// Auswahl beschrnken
 						deactivateOldTimes();
 					} else {
 						// Tag hinzuf�gen und als aktuellen Tag merken
 						addDay(bnt.getId());
 					}
 					break;
 				case 1:
 					disableRow1();
 					saveAllTimeSlots = false;
 					currentDay.setTime1(bnt);
 					break;
 				case 2:
 					disableRow2();
 					saveAllTimeSlots = false;
 					currentDay.setTime2(bnt);
 					break;
 				case 3:
 					disableRow3();
 					saveAllTimeSlots = false;
 					currentDay.setTime3(bnt);
 					break;
 				case 4:
 					disableRow4();
 					saveAllTimeSlots = false;
 					currentDay.setTime4(bnt);
 					break;
 
 				default:
 					break;
 				}
 
 				// Button w�hlen (gr�n)
 				setButtonSelect(bnt);
 			}
 		});
 
 	}
 
 	private void activateAllTimes(boolean enable) {
 		for (Button aButtonHandler : ButtonHandler) {
 			aButtonHandler.setEnabled(enable);
 		}
 	}
 
 	private void clickCurrentDay() {
 		switch (alarm.getCurrentWeekDay()) {
 		case 0:
 			mon.performClick();
 			break;
 		case 1:
 			tue.performClick();
 			break;
 		case 2:
 			wed.performClick();
 			break;
 		case 3:
 			thur.performClick();
 			break;
 		case 4:
 			fri.performClick();
 			break;
 		case 5:
 			sat.performClick();
 			break;
 		case 6:
 			sun.performClick();
 			break;
 		default:
 			break;
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private void disableWeek() {
 		mon.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		tue.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		wed.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		thur.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		fri.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		sat.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		sun.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 	}
 
 	@SuppressWarnings("deprecation")
 	private void disableRow1() {
 
 		// old way:
 		// setBackgroundColor(getResources().getColor(R.color.uncheckedButton));
 		// die neue Methode ist zwar veraltet, aber so k�nnen auch alte
 		// Ger�te
 		// es nutzen
 
 		timeslot1.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot2.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot3.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot4.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 	}
 
 	@SuppressWarnings("deprecation")
 	private void disableRow2() {
 		timeslot5.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot6.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot7.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 	}
 
 	@SuppressWarnings("deprecation")
 	private void disableRow3() {
 		timeslot8.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot9.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot10.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot11.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 	}
 
 	@SuppressWarnings("deprecation")
 	private void disableRow4() {
 		timeslot12.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot13.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot14.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 		timeslot15.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red));
 	}
 
 	@SuppressWarnings("deprecation")
 	private void setButtonSelect(Button bnt) {
 		if (bnt != null) {
 			bnt.setBackgroundDrawable(getResources().getDrawable(
 					R.drawable.button_green));
 		}
 	}
 
 	private void disableAllTimeSlots() {
 		disableRow1();
 		disableRow2();
 		disableRow3();
 		disableRow4();
 	}
 
 	private void addDay(int rID) {
 		// aktuellen Tag erstellen
 		currentDay = new Day(rID);
 		// der Woche hinzuf�gen
 		week[currentDay.getWeekID()] = currentDay;
 	}
 
 	// pr�ft, ob Tag bereits gesetzt ist
 	private boolean existDay(int rID) {
 		boolean res = false;
 		for (Day aWeek : week) {
 			if (aWeek != null) {
 				if (aWeek.getViewID() == rID) {
 					res = true;
 				}
 			}
 		}
 		return res;
 	}
 
 	private boolean writeWeekToDatabase() {
 		// Datenbankverbindung aufbauen
 		SQLHandler db = new SQLHandler(Week.this);
 		// jeden Wochentag durchgehen
 		for (Day aWeek : week) {
 			// wurde Wochentag gesetzt?
 			if (aWeek != null) {
 				// alten Tag loeschen
 				db.deleteDay(aWeek.getWeekID());
 				// alle Zeitslots w�hlen
 				int length =  aWeek.getTimeSlots().length;
 				for (int j = 0; j <length; j++) {
 					// Wochentag und Zeitslot in Datenbank schreiben
 					db.addDayTime(aWeek.getWeekID(), aWeek.getTimeSlots()[j]);
 				}
 			}
 		}
 		// Datenbankverbindung schlie�en
 		db.close();
 		// Import ist erfolgreich
 		return true;
 	}
 
 	private boolean getWeekFromDatabase() {
 		// Datenbankverbindung aufbauen
 		SQLHandler db = new SQLHandler(Week.this);
 		Cursor cur = db.getDayTime();
 		int check = 0;
 		if (cur != null) {
 			if (cur.moveToFirst()) {
 				do {
 					// Tag erstellen, wenn noch nicht existent
 					if (!existDay(Day.getViewIDfromWeekID(cur.getInt(0)))) {
 						week[cur.getInt(0)] = new Day(
 								Day.getViewIDfromWeekID(cur.getInt(0)));
 					}
 					// Zeitslots setzen, alle Buttons werden �bergeben
 					week[cur.getInt(0)].setTimeSlots(cur.getString(1),
 							ButtonHandler);
 					// check
 					check++;
 				} while (cur.moveToNext());
 			}
 		}
 		cur.close();
 		db.close();
 
 		return check > 3;
 
 	}
 
 	public void deactivateOldTimes() {
 		
 		// aktueller Tag gewhlt?
 		// deaktiviere alte Zeitslots
 		if (alarm.getCurrentWeekDay() == currentDay.getWeekID()) {
 			Calendar cal = Calendar.getInstance();
 			int currentHour = cal.get(Calendar.HOUR_OF_DAY);
 			String[] times = week[currentDay.getWeekID()].getTimeSlots();
 			int[] demureHour = new int[4];
 			// gesetzte Zeiten in gesetzte Stunden umwandeln
 			for (int i = 0; i < times.length; i++) {
 				demureHour[i] = Integer.valueOf(times[i].substring(0, 2));
 			}
 
 			// alle Zeiten durchlaufen und alte Zeiten deaktivieren
 			int demurHourIndex = 0;
 			for (int i = 0; i < ButtonHandler.length; i++) {
 				int buttonHour = Integer.valueOf(ButtonHandler[i].getText().toString().substring(0, 2));
 
				if (demurHourIndex < 4 && currentHour >= demureHour[demurHourIndex]) {
 					// Zeile der Timeslots deaktivieren
 					deactivTimeButtonLine(demurHourIndex);
 					demurHourIndex++;
 				}
 
 				if (buttonHour <= currentHour) {
 					// Button deaktivieren
 					ButtonHandler[i].setEnabled(false);
 				}
 			}
 		} else {
 			// alle anderen Tage, alle Zeitslots aktiv
 			activateAllTimes(true);
 		}
 	}
 
 	private void deactivTimeButtonLine(int line) {
 		switch (line) {
 		case 0:
 			timeslot1.setEnabled(false);
 			timeslot2.setEnabled(false);
 			timeslot3.setEnabled(false);
 			timeslot4.setEnabled(false);
 			break;
 		case 1:
 			timeslot5.setEnabled(false);
 			timeslot6.setEnabled(false);
 			timeslot7.setEnabled(false);
 			break;
 		case 2:
 			timeslot8.setEnabled(false);
 			timeslot9.setEnabled(false);
 			timeslot10.setEnabled(false);
 			timeslot11.setEnabled(false);
 			break;
 		case 3:
 			timeslot12.setEnabled(false);
 			timeslot13.setEnabled(false);
 			timeslot14.setEnabled(false);
 			timeslot15.setEnabled(false);
 			break;
 		default:
 			break;
 		}
 	}
 
 	// //////////////////////////////////////////////////////////////////////
 	// Private Klassen
 	// //////////////////////////////////////////////////////////////////////
 
 	private static class Day {
 		private int rID;
 		private int weekID;
 		private String[] timeSlots = new String[4];
 		private Button[] timeSlotsButton = new Button[4];
 		private int timeSlotID;
 
 		Day(int rID) {
 			this.rID = rID;
 			this.weekID = Day.getWeekIDfromViewID(rID);
 			this.timeSlotID = 0;
 		}
 
 		public static int getWeekIDfromViewID(int rID) {
 			switch (rID) {
 			case R.id.mon:
 				return 0;
 			case R.id.tue:
 				return 1;
 			case R.id.wed:
 				return 2;
 			case R.id.thur:
 				return 3;
 			case R.id.fri:
 				return 4;
 			case R.id.sat:
 				return 5;
 			case R.id.sun:
 				return 6;
 			default:
 				// for compiler
 				return 99;
 			}
 		}
 
 		public static int getViewIDfromWeekID(int wID) {
 			switch (wID) {
 			case 0:
 				return R.id.mon;
 			case 1:
 				return R.id.tue;
 			case 2:
 				return R.id.wed;
 			case 3:
 				return R.id.thur;
 			case 4:
 				return R.id.fri;
 			case 5:
 				return R.id.sat;
 			case 6:
 				return R.id.sun;
 			default:
 				// for compiler
 				return 99;
 			}
 		}
 
 		public int getWeekID() {
 			return this.weekID;
 		}
 
 		public void setTime1(Button time) {
 			this.timeSlots[0] = time.getText().toString();
 			this.timeSlotsButton[0] = time;
 		}
 
 		public void setTime2(Button time) {
 			this.timeSlots[1] = time.getText().toString();
 			this.timeSlotsButton[1] = time;
 		}
 
 		public void setTime3(Button time) {
 			this.timeSlots[2] = time.getText().toString();
 			this.timeSlotsButton[2] = time;
 		}
 
 		public void setTime4(Button time) {
 			this.timeSlots[3] = time.getText().toString();
 			this.timeSlotsButton[3] = time;
 		}
 /*
 		public boolean existAllTimes() {
 			return timeSlots[0] != null && timeSlots[1] != null
 					&& timeSlots[2] != null && timeSlots[3] != null;
 		}
 */
 		public String[] getTimeSlots() {
 			return timeSlots;
 		}
 
 		public void setTimeSlots(String time, Button[] handler) {
 			// Zeit von HH:mm:ss in HH:mm umwandeln
 			time = time.substring(0, 5);
 			// pr�fen, ob ein Button den Zeittext enth�lt
 			for (Button aHandler : handler) {
 				if (aHandler.getText().toString().compareTo(time) == 0) {
 					// Zeit abspeichern
 					this.timeSlots[this.timeSlotID] = time;
 					// zugeh�rigen Button speichern
 					this.timeSlotsButton[this.timeSlotID] = aHandler;
 					// this.timeSlotID = (this.timeSlotID+1)%4;
 					this.timeSlotID++;
 				}
 			}
 		}
 
 		public int getViewID() {
 			return rID;
 		}
 
 		public Button[] getTimeSlotsButton() {
 			return timeSlotsButton;
 		}
 	}
 }
