 package com.gourmet6;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.DatePicker.OnDateChangedListener;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 
 public class ReservationActivity extends Activity {
 	
 	private int people;
 	private int year;
 	private int month;
 	private int day;
 	private int hour;
 	private int minute;
 	private int minYear;
 	private int minMonth;
 	private int minDay;
 	
 	private String s;
 	private Button dateTime;
 	private EditText nbrPrs;
 	private TextView tvHoraireReserv;
 	
 	private Gourmet g ;
 	
 	private Dialog dialog;
 	
 	private Context context;
 	
 	private boolean from;
 	private Restaurant currentRest;
 	
 	private CheckBox takeAway;
 	
 	private DBHandler dbh;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_reservation);
 		overridePendingTransition(0, R.anim.commetuveux);
 		setTitle(R.string.activity_reservation_title);
 		
 		g = (Gourmet)getApplication();
 		currentRest = g.getRest();
 		context = this;
 		dbh = new DBHandler(context);
 		from = false;
 		
 		Bundle extra = getIntent().getExtras();
 		this.from = extra.getBoolean("fromOrder");
 		
 		final Calendar c = Calendar.getInstance();
 		c.setTimeZone(TimeZone.getDefault());
 		year = c.get(Calendar.YEAR);
 		month = c.get(Calendar.MONTH);
 		day = c.get(Calendar.DAY_OF_MONTH);
 		hour = c.get(Calendar.HOUR_OF_DAY);
 		minute = c.get(Calendar.MINUTE);
 		
 		minYear = c.get(Calendar.YEAR);
 		minMonth = c.get(Calendar.MONTH);
 		minDay = c.get(Calendar.DAY_OF_MONTH);
 		
 		//TextView
 		tvHoraireReserv = (TextView) findViewById(R.id.horaireReserv);
 		setHorair();
 		
 		//EditText
 		nbrPrs = (EditText) findViewById(R.id.nbrPrsReserv);
 		
 		//CheckBox
		takeAway = (CheckBox) findViewById(R.id.checkBoxReserv);
 		takeAway.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
 				System.out.println(checked);
 				if(checked)
 				{
 					nbrPrs.setText("");
 					nbrPrs.setEnabled(false);
 					people=0;
 				}
 				else
 				{
 					nbrPrs.setEnabled(true);
 				}
 			}
 		});
 		
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		
 		
 		dateTime = (Button) findViewById(R.id.dateTime);
 		s = TimeTable.parseDateInStringForReservation(new GregorianCalendar(year, month, day, hour, minute));
 		dateTime.setText(s);
 		dateTime.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog = new Dialog(context);
 				dialog.setContentView(R.layout.datetimedialog);
 				dialog.setTitle("Choose date");
 				
 				final DatePicker dp = (DatePicker) dialog.findViewById(R.id.datePicker1);
 				dp.init(year, month, day, new OnDateChangedListener() {
 					@Override
 					public void onDateChanged(DatePicker view, int year, int monthOfYear,
 							int dayOfMonth) {
 							if (year < minYear){
 								view.updateDate(minYear, minMonth, minDay);
 							}
 			                if (monthOfYear < minMonth && year == minYear){
 			                	view.updateDate(minYear, minMonth, minDay);
 			                }
 			                if (dayOfMonth < minDay && year == minYear && monthOfYear == minMonth){
 			                	view.updateDate(minYear, minMonth, minDay);
 			                }
 					}
 				});
 				
 				final TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker1);
 				tp.setIs24HourView(true);
 				tp.setCurrentHour(hour);
 				tp.setCurrentMinute(0);
 				
 				Button ok = (Button) dialog.findViewById(R.id.buttonOkDialog);
 				ok.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						year = dp.getYear();
 						month = dp.getMonth();
 						day = dp.getDayOfMonth();
 						hour = tp.getCurrentHour();
 						minute = tp.getCurrentMinute();
 						
 						s = TimeTable.parseDateInStringForReservation(new GregorianCalendar(year, month, day, hour, minute));
 						dateTime.setText(s);
 						
 						dialog.cancel();
 					}
 				});
 				
 				Button cancel = (Button) dialog.findViewById(R.id.buttonCancelDialog);
 				cancel.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						dialog.cancel();
 					}
 				});
 				
 				dialog.show();
 			}
 		});
 		
 		//Reaction du bouton de commande
 		Button order = (Button) findViewById(R.id.comInReserv);
 		if(from){
 			order.setEnabled(false);
 		}else{
 			order.setEnabled(true);
 		}
 		order.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(!takeAway.isChecked() && nbrPrs.length()==0){
 					showAlert("Invalid number of people.");
 					return;
 				}
 				
 				Reservation res = checkClient();
 				if(res==null){
 					showAlert("You're not logged !");
 				}
 				else if(checkReservation(res)){
 					g.setReservation(res);
 					currentRest.createListDishes(new DBHandler(ReservationActivity.this));
 					Intent commande = new Intent(ReservationActivity.this, OrderActivity.class);
 					commande.putExtra("from", false);
 					startActivity(commande);
 				}
 			}
 		});	
 		
 		//Reaction du bouton de soumission
 		Button submit = (Button) findViewById(R.id.validateReserv);
 		submit.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO suppression reservation ?
 				if(!takeAway.isChecked() && nbrPrs.length()==0){
 					showAlert("Invalid number of people.");
 					return;
 				}
 				Reservation res = checkClient();
 				if(res==null)
 				{
 					showAlert("You're not logged !");
 				}
 				
 				else if(checkReservation(res)){
 					g.setReservation(res); //TODO que faire ?
 					Intent commande = new Intent(ReservationActivity.this, RestaurantActivity.class);
 					startActivity(commande);
 				}
 			}
 		});
 		
 	}
 	
 	private void setHorair()
 	{
 		if(currentRest.getSemaine()==null) return;
 		String s = currentRest.getHoraireInString();
 		if(s.length()!=0)tvHoraireReserv.setText(s);
 	}
 
 	public boolean checkReservation(Reservation reservTemp)
 	{
 		if(nbrPrs.length()==0){
 			Toast.makeText(getApplicationContext(), "Vous reservez pour 0 personnes ?", Toast.LENGTH_SHORT).show();
 			return false;
 		}
 		String res = g.getRest().checkReservation(reservTemp, dbh);
 		if(res!=null)
 		{			
 			showAlert(res);
 
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 	
 	public Reservation checkClient()
 	{
 		if(g.getClient()!=null)
 		{
 			people = Integer.parseInt(nbrPrs.getText().toString());
 			GregorianCalendar date = new GregorianCalendar(year, month, day, hour, minute);
 			return g.getClient().createReservation(currentRest.getName(), date, people);
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	public void showAlert(String s)
 	{
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
 		// set title
 		alertDialogBuilder.setTitle("Error");
 
 		// set dialog message
 		alertDialogBuilder
 			.setMessage(s)
 			.setCancelable(true)
 			.setPositiveButton("OK",null);
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		if(g.getClient() != null)
 			getMenuInflater().inflate(R.menu.main, menu);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent clientGo = new Intent(ReservationActivity.this, ClientActivity.class);
 		startActivity(clientGo);
 		return super.onOptionsItemSelected(item);
 	}
 
 }
