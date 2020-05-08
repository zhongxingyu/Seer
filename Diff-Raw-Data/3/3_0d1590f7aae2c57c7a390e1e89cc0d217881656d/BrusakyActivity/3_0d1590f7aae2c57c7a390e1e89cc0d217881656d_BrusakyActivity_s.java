 package cz.balent.brusaky;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class BrusakyActivity extends Activity {
 	private int crunchesTime = 70;
 	private int restTime = 30;
 	private int totalSeries = 4;
 	private boolean isRunning = false;
 
 	private Button timeButton;
 	private Button restButton;
 	private Button plusButton;
 	private Button minusButton;
 	private Button startButton;
 
 	private TextView serieLengthTextView;
 	private TextView restLengthTextView;
 	private TextView seriesCountTextView;
 	private TextView statusLabelTextView;
 	private TextView timeCounter;
 
 	private enum SoundType {
 		WAIT, START, STOP;
 	}
 
 	private TimePickerDialog.OnTimeSetListener crunchesTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			crunchesTime = (hourOfDay * 60) + minute;
 			setTextCorrectly(serieLengthTextView, getStringTime(crunchesTime));
 		}
 	};
 
 	private TimePickerDialog.OnTimeSetListener restTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			restTime = (hourOfDay * 60) + minute;
 			setTextCorrectly(restLengthTextView, getStringTime(restTime));
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		timeButton = (Button) findViewById(R.id.button1);
 		restButton = (Button) findViewById(R.id.button2);
 		plusButton = (Button) findViewById(R.id.button5);
 		minusButton = (Button) findViewById(R.id.button10);
 		startButton = (Button) findViewById(R.id.button4);
 
 		serieLengthTextView = (TextView) findViewById(R.id.textView1a);
 		restLengthTextView = (TextView) findViewById(R.id.textView2a);
 		seriesCountTextView = (TextView) findViewById(R.id.textView3a);
 		statusLabelTextView = (TextView) findViewById(R.id.textView4);
 		timeCounter = (TextView) findViewById(R.id.textView5);
 
 		class ExerciseRunner extends Thread {
 
 			private volatile boolean shutdown = false;
 
 			private void sleepSeconds(int i) {
 				sleepMiliSeconds(i * 1000);
 			}
 
 			private void sleepMiliSeconds(int i) {
 				try {
 					Thread.sleep(i);
 				} catch (InterruptedException ex) {
 					// nothing
 				}
 			}
 
 			private void playSound(final SoundType st) {
 
 				Runnable r = new Runnable() {
 					public void run() {
 						MediaPlayer mediaPlayer = null;
 
 						if (st == SoundType.WAIT) {
 							mediaPlayer = MediaPlayer.create(
 									getApplicationContext(), R.raw.prepare);
 						} else if (st == SoundType.START) {
 							mediaPlayer = MediaPlayer.create(
 									getApplicationContext(), R.raw.start);
 						} else if (st == SoundType.STOP) {
 							mediaPlayer = MediaPlayer.create(
 									getApplicationContext(), R.raw.end);
 						}
 						mediaPlayer.start();
 						while (mediaPlayer.isPlaying()) {
 							sleepMiliSeconds(1000);
 						}
 						mediaPlayer.stop();
 						mediaPlayer.release();
 						mediaPlayer = null;
 					}
 				};
 				Thread t = new Thread(r);
 				t.start();
 			}
 
 			public void run() {
 				setTextCorrectly(statusLabelTextView, "Priprav sa");
 				for (int i = 5; i > 0; i--) {
 					if (shutdown == true) {
 						return;
 					}
 					updateTime(i);
 					sleepSeconds(1);
 
 					if (i > 1) {
 						playSound(SoundType.WAIT);
 					} else {
 						playSound(SoundType.START);
 					}
 				}
 
 				for (int series = totalSeries; series > 0; series--) {
 					setTextCorrectly(statusLabelTextView, "Makaj");
 					setTextCorrectly(seriesCountTextView,
 							String.valueOf(series));
 					for (int time = crunchesTime; time > 0; time--) {
 						if (shutdown == true) {
 							return;
 						}
 						updateTime(time);
 						sleepSeconds(1);
 						if (time == 1) {
 							playSound(SoundType.STOP);
 						}
 					}
 					if (series > 1) {
 						setTextCorrectly(statusLabelTextView, "Oddych");
 						setTextCorrectly(seriesCountTextView,
 								String.valueOf(series - 1));
 						for (int time = restTime; time > 0; time--) {
 							if (shutdown == true) {
 								return;
 							}
 							updateTime(time);
 							sleepSeconds(1);
 							if ((time < 6) && (time > 1)) {
 								setTextCorrectly(statusLabelTextView,
 										"Priprav sa");
 								playSound(SoundType.WAIT);
 							}
 							if (time == 1) {
 								playSound(SoundType.START);
 							}
 						}
 					}
 				}
 				setTextCorrectly(seriesCountTextView,
 						String.valueOf(totalSeries));
 				updateTime(0);
 				setTextCorrectly(statusLabelTextView, "Cas nebezi");
 				setTextCorrectly(startButton, "Start");
 				isRunning = false;
 			}
 
 			public void shutdown() {
 				shutdown = true;
 			}
 		}
 		;
 
 		startButton.setOnClickListener(new View.OnClickListener() {
 			private ExerciseRunner exerciseRunner;
 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			PowerManager.WakeLock wl = pm.newWakeLock(
 					PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
 
 			public void onClick(View v) {
 
 				if (isRunning == false) {
 					wl.acquire();
 					isRunning = true;
 					startButton.setText("Stop");
 					exerciseRunner = new ExerciseRunner();
 					exerciseRunner.start();
 				} else {
 					isRunning = false;
 					startButton.setText("Start");
 					setTextCorrectly(seriesCountTextView,
 							String.valueOf(totalSeries));
 					exerciseRunner.shutdown();
 					setTextCorrectly(timeCounter, "00:00");
 					wl.release();
 				}
 			}
 		});
 
 		timeButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				showDialog(1);
 			}
 		});
 
 		restButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				showDialog(2);
 			}
 		});
 
 		plusButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				totalSeries++;
 				setTextCorrectly(seriesCountTextView,
 						String.valueOf(totalSeries));
 			}
 		});
 
 		minusButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				if (totalSeries > 1) {
 					totalSeries--;
 					setTextCorrectly(seriesCountTextView,
 							String.valueOf(totalSeries));
 				}
 
 			}
 		});
 
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		TimePickerDialog tpd;
 		if (id == 1) {
 			tpd = new TimePickerDialog(this, crunchesTimeSetListener,
 					crunchesTime / 60, crunchesTime % 60, true);
 		} else {
 			tpd = new TimePickerDialog(this, restTimeSetListener,
 					restTime / 60, restTime % 60, true);
 		}
 		return tpd;
 	}
 
 	private String getStringTime(int seconds) {
 		return String.format("%02d:%02d", seconds / 60, seconds % 60);
 	}
 
 	private void updateTime(int seconds) {
 		setTextCorrectly(timeCounter, getStringTime(seconds));
 	}
 
 	private void setTextCorrectly(final TextView textView, final String text) {
 		textView.post(new Runnable() {
 			public void run() {
 				textView.setText(text);
 			}
 		});
 	}
 }
