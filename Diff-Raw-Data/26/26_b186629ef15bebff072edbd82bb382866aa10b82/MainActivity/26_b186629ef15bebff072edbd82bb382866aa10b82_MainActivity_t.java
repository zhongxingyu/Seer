 /*
  * CrayCray - A game formed as a pet in you android device for the user to take care of.
  *
  * Copyright (C) 2013  Sofia Edstrom, Emma Gustafsson, Patricia Paulsson, 
  * Josefin Ondrus, Hanna Materne, Elin Ljunggren & Jonathan Thunberg.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/.
  * 
  * To contact the creators of this program, please make use of any of the email addresses below.
  * hanna.materne@gmail.com, elin.l.ljunggren@gmail.com, sofia.edstrom@galaxen.se
  * chorriballong@gmail.com, jonathan.thunberg@gmail.com, emma.i.gustafsson@gmail.com,
  * josefin.ondrus@gmail.com
  *   
  */
 
 package se.chalmers.dat255.craycray;
 
 import java.util.Calendar;
 
 import se.chalmers.dat255.craycray.database.DatabaseAdapter;
 import se.chalmers.dat255.craycray.database.DatabaseConstants;
 
 import se.chalmers.dat255.craycray.model.DeadException;
 
 import se.chalmers.dat255.craycray.model.NeedsModel;
 import se.chalmers.dat255.craycray.util.TimeUtil;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.app.FragmentManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	// private TextView feedView;
 	// private TextView cuddleView;
 	// private TextView cleanView;
 	// private TextView energyView;
 
 	private Button feedButton;
 	private Button cuddleButton;
 	private Button cleanButton;
 	private Button energyButton;
 	private Button removePooButton;
 
 	private ProgressBar foodBar;
 	private ProgressBar cuddleBar;
 	private ProgressBar cleanBar;
 	private ProgressBar energyBar;
 
 	private ImageView crayCray;
 
 	// private String deathCause; ondig?
 
 	private NeedsModel model;
 	private Thread t;
 	private AlertDialog.Builder alertDialog;
 
 	private DatabaseAdapter dbA;
 
 	// A Handler to take care of updates in UI-thread
 	// When sendMessage method is called, this is where the message is sent
 	Handler handler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 
 			if (msg.obj instanceof DeadException) {
 				DeadException exception = (DeadException) msg.obj;
 				String message = exception.getDeathCause();
 				alertDialog.setMessage(message);
 				alertDialog.show();
 			}
 
 			// feedView.setText("" + model.getHungerLevel());
 			// energyView.setText("" + model.getEnergyLevel());
 			// cleanView.setText("" + model.getCleanLevel());
 			// cuddleView.setText("" + model.getCuddleLevel());
 
 			foodBar.setProgress(model.getHungerLevel());
 			cuddleBar.setProgress(model.getCuddleLevel());
 			cleanBar.setProgress(model.getCleanLevel());
 			energyBar.setProgress(model.getEnergyLevel());
 
			crayCray.invalidate();

 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		final ImageButton feedButton = (ImageButton) findViewById(R.id.feedButton);
 		feedButton.setImageResource(R.drawable.button_food);
 
 		final ImageButton cleanButton = (ImageButton) findViewById(R.id.cleanButton);
 		cleanButton.setImageResource(R.drawable.button_clean);
 
 		final ImageButton cuddleButton = (ImageButton) findViewById(R.id.cuddleButton);
 		cuddleButton.setImageResource(R.drawable.button_happiness);
 
 		final ImageButton energyButton = (ImageButton) findViewById(R.id.energyButton);
 		energyButton.setImageResource(R.drawable.button_energy);
 
 		final ImageButton removePooButton = (ImageButton) findViewById(R.id.removePooButton);
 		removePooButton.setImageResource(R.drawable.button_poo);
 		
 
 		foodBar = (ProgressBar) findViewById(R.id.foodBar);
 		cuddleBar = (ProgressBar) findViewById(R.id.cuddleBar);
 		cleanBar = (ProgressBar) findViewById(R.id.cleanBar);
 		energyBar = (ProgressBar) findViewById(R.id.energyBar);
 
 		crayCray = (ImageView) findViewById(R.id.crayCray);
 
 		// feedView = (TextView)findViewById(R.id.feedTextView);
 		// cleanView = (TextView)findViewById(R.id.cleanTextView);
 		// cuddleView = (TextView)findViewById(R.id.cuddleTextView);
 		// energyView = (TextView)findViewById(R.id.energyTextView);
 
 		model = NeedsModel.getInstance();
 
 		// feedView.setText("" + model.getHungerLevel());
 		// cleanView.setText("" + model.getCleanLevel());
 		// cuddleView.setText("" + model.getCuddleLevel());
 		// energyView.setText("" + model.getEnergyLevel());
 
 		foodBar.setProgress(model.getHungerLevel());
 		cuddleBar.setProgress(model.getCuddleLevel());
 		cleanBar.setProgress(model.getCleanLevel());
 		energyBar.setProgress(model.getEnergyLevel());
 		
//		setCrayExpression(1, model.getCleanLevel());
//		setCrayExpression(2, model.getHungerLevel());
 		
 		alertDialog = new AlertDialog.Builder(this);
 		alertDialog.setTitle("Game Over");
 		alertDialog.setPositiveButton("New Game",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 
 					}
 				});
 		alertDialog.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 
 					}
 				});
 
 		t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				while (true) {
 					try {
 						model.setHungerLevel(model.getHungerLevel() - 1);
 						model.setCleanLevel(model.getCleanLevel() - 3);
 						model.setCuddleLevel(model.getCuddleLevel() - 2);
 						model.setPooLevel(model.getPooLevel() - 9);
 						model.setEnergyLevel(model.getEnergyLevel() - 1);
 						
 						if (model.hasPooed()) {
 							// show poo on the screen, unimplemented
 						}
 						if (model.isIll()) {
 							// show a ill craycray, unimplemented
 						}
 						
						setCrayExpression(1, model.getCleanLevel());
						setCrayExpression(2, model.getHungerLevel());
 						
 						handler.sendMessage(handler.obtainMessage());
 						Thread.sleep(1000);
 						crayCray.invalidate();
 					} catch (Exception e) {
 						if (e instanceof DeadException) {
 							Message msg = Message.obtain();
 							msg.obj = e;
 							handler.sendMessage(msg);
 							break;
 						}
 					}
 
 				}
 			}
 		});
 
 		dbA = new DatabaseAdapter(getBaseContext());
 		// checks if the database exists
 		if (dbA.getValue("Firsttime") == -1) {
 			dbA.addValue("Firsttime", 1);
 			dbA.addValue(DatabaseConstants.HUNGER, model.getHungerLevel());
 			dbA.addValue(DatabaseConstants.CUDDLE, model.getCuddleLevel());
 			dbA.addValue(DatabaseConstants.POO, model.getPooLevel());
 			dbA.addValue(DatabaseConstants.CLEAN, model.getCleanLevel());
 			dbA.addStringValue(DatabaseConstants.TIME,
 					TimeUtil.getCurrentTime());
 		} else {
 			int differenceInSeconds = TimeUtil.compareTime(dbA
 					.getStringValue(DatabaseConstants.TIME));
 			Log.w("Database",
 					differenceInSeconds + ", "
 							+ dbA.getValue(DatabaseConstants.HUNGER));
 			try {
 				model.setHungerLevel(dbA.getValue(DatabaseConstants.HUNGER)
 						+ differenceInSeconds * (-1));
 				model.setCuddleLevel(dbA.getValue(DatabaseConstants.CUDDLE)
 						+ differenceInSeconds * (-3));
 				model.setCleanLevel(dbA.getValue(DatabaseConstants.CLEAN)
 						+ differenceInSeconds * (-2));
 				model.setPooLevel(dbA.getValue(DatabaseConstants.POO));
 			} catch (DeadException e) {
 				if (e instanceof DeadException) {
 					Message msg = Message.obtain();
 					msg.obj = e;
 					handler.sendMessage(msg);
 				}
 			}
 
 		}
 
 		// feedView.setText("" + model.getHungerLevel());
 		// foodBar.setProgress(model.getHungerLevel());
 		// cuddleView.setText("" + model.getCuddleLevel());
 		// cleanView.setText("" + model.getCleanLevel());
 		// energyView.setText("" + model.getEnergyLevel());
 
 		foodBar.setProgress(model.getHungerLevel());
 		cuddleBar.setProgress(model.getCuddleLevel());
 		cleanBar.setProgress(model.getCleanLevel());
 		energyBar.setProgress(model.getEnergyLevel());
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		t.start();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Updates the database if the application is shut down
 	 */
 	@Override
 	public void onDestroy() {
 		dbA.updateValue(DatabaseConstants.HUNGER, model.getHungerLevel());
 		dbA.updateValue(DatabaseConstants.CUDDLE, model.getCuddleLevel());
 		dbA.updateValue(DatabaseConstants.CLEAN, model.getCleanLevel());
 		dbA.updateValue(DatabaseConstants.POO, model.getPooLevel());
 		dbA.updateStringValue(DatabaseConstants.TIME, TimeUtil.getCurrentTime());
 		super.onDestroy();
 	}
 
 	// Changes the hungerlevel indicator int in the TextView
 	public void feed(View view) {
 		try {
 			model.setHungerLevel(model.getHungerLevel() + 5);
 		} catch (DeadException e) {
 
 		}
 		handler.sendMessage(handler.obtainMessage());
//		String feed = new String("" + model.getHungerLevel());
 
 	}
 
 	public void clean(View view) {
 
 		model.setCleanLevel(model.getCleanLevel() + 10);
 		handler.sendMessage(handler.obtainMessage());
 
 	}
 
 	public void cuddle(View view) {
 
 		model.setCuddleLevel(model.getCuddleLevel() + 7);
 		handler.sendMessage(handler.obtainMessage());
 
 	}
 
 	public void sleep(View view) {
 		model.setEnergyLevel(model.getEnergyLevel() + 50);
 		handler.sendMessage(handler.obtainMessage());
 
 	}
 
 	public void removePoo(View view) {
 		model.setHasPooedOrNot(false);
 		handler.sendMessage(handler.obtainMessage());
 
 	}
 
 	public void cure(View view) {
 		model.setIllness(false);
 		handler.sendMessage(handler.obtainMessage());
 
 	}
 	
 	/**
 	 * set correct image of craycray based on the different levels.
 	 * 
 	 * @param mode
 	 *            which level to check
 	 * @param level
 	 *            the value of the level
 	 */
	public void setCrayExpression(int mode, int level) {
 		int expression;
 		switch (mode) {
 
 		// dirtyLvl
 		case 1:
 			if (level < 50) {
 				System.out.println("inside case 1 (dirty)" + level);
 				expression = R.drawable.dirty_baby;
 				crayCray.setImageResource(expression);
				handler.sendMessage(handler.obtainMessage());
 			}
 			break;
 		// hungryLvl
 		case 2:
 			if (level == 0) {
 				expression = R.drawable.dead_baby;
 				crayCray.setImageResource(expression);
				handler.sendMessage(handler.obtainMessage());
 			} else if (level < 50) {
 				System.out.println("inside case 1 (hungry)" + level);
 				expression = R.drawable.feed_baby;
 				crayCray.setImageResource(expression);
				handler.sendMessage(handler.obtainMessage());
 			}
 			break;
 		default:
 			System.out.println("inside base-case" + level);
 			expression = R.drawable.regular_baby;
 			crayCray.setImageResource(expression);
			handler.sendMessage(handler.obtainMessage());
 		}
 	}
 
 }
