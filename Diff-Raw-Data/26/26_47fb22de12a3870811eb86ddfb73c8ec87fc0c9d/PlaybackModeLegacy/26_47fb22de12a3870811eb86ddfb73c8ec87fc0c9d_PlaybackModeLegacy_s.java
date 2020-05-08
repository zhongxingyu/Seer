  package com.MusicalSketches;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.MusicalSketches.EditModeLegacy.states;
 import com.MusicalSketches.datarep.Note;
 import com.MusicalSketches.datarep.NoteFrequencies;
 import com.MusicalSketches.datarep.Song;
 
 public class PlaybackModeLegacy extends Activity {
 	Song song;
 	int notesOnScreen = 0;
 	int state = 0;
 	int sampleRate = 8000;
 	byte[] generatedSnd;
 	int numSamples;
 	int screen = 0;
 	AudioTrack audioTrack;
 	ImageButton b;
 	int xloc;
 	ImageView arrow;
 	ViewGroup group;
 	ImageButton right_button;
 	ImageButton left_button;
 	TextView meter_disp;
 	ImageView clef_image;
 	Timer arrowTimer;
 	ImageButton play_pause;
 	ImageButton rewind_button;
 	ImageButton forward_button;
 	public static final int MAX_NOTES_ONSCREEN = 10;
 	int screen_number = 0;
 	View[] notes = new View[MAX_NOTES_ONSCREEN];
 	View[] annotations = new View[MAX_NOTES_ONSCREEN];
 	View[] dynamics = new View[MAX_NOTES_ONSCREEN];
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.playback_mode);
 
 		song = (Song) getIntent().getSerializableExtra("song object");
 		arrow = (ImageView) findViewById(R.id.arrow_indicator);
 		right_button = (ImageButton) findViewById(R.id.right_arrow);
 		left_button = (ImageButton) findViewById(R.id.left_arrow);
 		clef_image = (ImageView) findViewById(R.id.clef_image);
 		meter_disp = (TextView) findViewById(R.id.meter_text);
 
 		addClefMeterKey(song);
		genTone();
 
 		play_pause = (ImageButton) findViewById(R.id.play_pause_button);
 		rewind_button = (ImageButton) findViewById(R.id.rewind_button);
 		forward_button = (ImageButton) findViewById(R.id.forward_button);
 		b = (ImageButton) findViewById(R.id.play_pause_button);
 		group = (ViewGroup) findViewById(R.id.edit_layout);
 
 		class left_arrow_button_click implements OnClickListener {
 			@Override
 			public void onClick(View v) {
 				generateScreen(screen_number - 1);
 			}
 		}
 
 		class right_arrow_button_click implements OnClickListener {
 			@Override
 			public void onClick(View v) {
 				generateScreen(screen_number + 1);
 			}
 		}
 
 		play_pause.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (state == 0) {
 					state = 1;
 					b.setImageResource(R.drawable.pause);
 					audioTrack.play();
 					initArrowTimer();
 				} else {
 					state = 0;
 					b.setImageResource(R.drawable.play);
 					audioTrack.pause();
 				}
 			}
 		});
 
 		rewind_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				audioTrack.stop();
 				makeTrack();
 			}
 		});
 
 		forward_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				audioTrack.setPlaybackHeadPosition(audioTrack
 						.getPlaybackHeadPosition());
 			}
 		});
 		left_button.setOnClickListener(new left_arrow_button_click());
 		right_button.setOnClickListener(new right_arrow_button_click());
 		generateScreen(0);
 	}
 
 	private void generateScreen(int number) {
 		// plan here is to save all the shit onscreen
 		// then redisplay an overlapping note and arrows as necessary.
 		// NB: savePage() must occur before reassigning screen_number
 		this.screen_number = number;
 		if (this.screen_number > 0) {
 			this.left_button.setVisibility(View.VISIBLE);
 			this.clef_image.setVisibility(View.INVISIBLE);
 			this.meter_disp.setVisibility(View.INVISIBLE);
 		} else {
 			this.left_button.setVisibility(View.INVISIBLE);
 			this.clef_image.setVisibility(View.VISIBLE);
 			this.meter_disp.setVisibility(View.VISIBLE);
 		}
 		// show the right button if there are more notes off the page.
 		if (this.song.size() >= (this.screen_number + 1) * MAX_NOTES_ONSCREEN) {
 			this.right_button.setVisibility(View.VISIBLE);
 		} else {
 			this.right_button.setVisibility(View.INVISIBLE);
 		}
 		// before displaying anything, we hardcore clear the screen.
 		for (View v : notes) {
 			group.removeView(v);
 		}
 		for (View v : annotations) {
 			group.removeView(v);
 		}
 		for (View v : dynamics) {
 			group.removeView(v);
 		}
 		this.dynamics = new View[MAX_NOTES_ONSCREEN];
 		this.notes = new View[MAX_NOTES_ONSCREEN];
 		this.annotations = new View[MAX_NOTES_ONSCREEN];
 
 		// we do this by duplicating and hopefully replacing all of the
 		// uploadFromSong code.
 		int g = 0;
 		while (g < MAX_NOTES_ONSCREEN) {
 			Note n = this.song.getNoteNum(g + this.screen_number
 					* MAX_NOTES_ONSCREEN);
 			if (n == null) {
 				break;
 			}
 			Log.d("", "adding note");
 			double freq = n.getPitch();
 			double l = n.getLength();
 			double x = 60 + (g + 1) * 60;
 			Log.d("", "" + x);
 			String str = NoteFrequencies.freqToString.get(freq);
 			Log.d("", str);
 			str = str.substring(0, 2);
 			Log.d("", str);
 			double y = 0;
 			String[] staff_note_rep = null;
 			if (song.getClef() == 0) {
 				staff_note_rep = NoteFrequencies.bass_staff_notes;
 			} else {
 				staff_note_rep = NoteFrequencies.staff_notes;
 			}
 			for (int i = 0; i < staff_note_rep.length; i++) {
 				if (staff_note_rep[i].equals(str)) {
 					y = NoteFrequencies.staff_lines[i] - 30;
 				}
 			}
 			boolean line_through = false;
 			if (y == NoteFrequencies.staff_lines[0] - 30
 					|| y == NoteFrequencies.staff_lines[NoteFrequencies.staff_lines.length - 1] - 30) {
 				line_through = true;
 			}
 			ImageView img = new ImageView(getApplicationContext());
 			if (l == 0.125) {
 				if (!n.isRest()) {
 					if (!line_through) {
 						img.setImageResource(R.drawable.eigth_note_transparent);
 					} else {
 						img.setImageResource(R.drawable.eigth_note_transparent_low);
 					}
 				} else {
 					img.setImageResource(R.drawable.eighth_rest);
 				}
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 60 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			} else if (l == 0.25) {
 				if (!n.isRest()) {
 					if (!line_through) {
 						img.setImageResource(R.drawable.quarter_note_transparent);
 					} else {
 						img.setImageResource(R.drawable.quarter_note_transparent_low);
 					}
 				} else {
 					img.setImageResource(R.drawable.quarter_rest);
 				}
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 60 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			} else if (l == 0.5) {
 				if (!n.isRest()) {
 					if (!line_through) {
 						img.setImageResource(R.drawable.half_note_transparent);
 					} else {
 						img.setImageResource(R.drawable.half_note_transparent_low);
 					}
 				} else {
 					img.setImageResource(R.drawable.half_rest);
 				}
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 60 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			}
 			if (img != null) {
 				this.notes[g] = img;
 			}
 			x = 60 + (g + 1) * 60;
 			Log.d("", "" + x);
 			str = NoteFrequencies.freqToString.get(freq);
 			Log.d("", "str value: " + str);
 			img = new ImageView(getApplicationContext());
 			if (str.endsWith("sharp")) {
 				img.setImageResource(R.drawable.sharp_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 60 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.addView(img);
 			} else if (str.endsWith("flat")) {
 				img.setImageResource(R.drawable.flat_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 60 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.addView(img);
 			} else if (str.endsWith("natural")) {
 				img.setImageResource(R.drawable.natural_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 60 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			}
 			if (img != null) {
 				this.annotations[g] = img;
 			}
 			img = new ImageView(getApplicationContext());
 			String dynString = n.getDynamic();
 			if (dynString != null) {
 				if (dynString.equals("pp")) {
 					img.setImageResource(R.drawable.pp);
 				} else if (dynString.equals("p")) {
 					img.setImageResource(R.drawable.p);
 				} else if (dynString.equals("mp")) {
 					img.setImageResource(R.drawable.mp);
 				} else if (dynString.equals("mf")) {
 					img.setImageResource(R.drawable.mf);
 				} else if (dynString.equals("f")) {
 					img.setImageResource(R.drawable.f);
 				} else if (dynString.equals("ff")) {
 					img.setImageResource(R.drawable.ff);
 				}
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = 264;
 				img.setVisibility(0);
 				params.leftMargin = 60 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			}
 			if (img != null) {
 				this.dynamics[g] = img;
 			}
 			g++;
 		}
 	}
 
 	public enum playback_menu_options {
 		CLOSE, EDIT
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.playback_menu, menu);
 		return true;
 	}
 
 	@Override
 	public void onBackPressed() {
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.playback_close:
 			audioTrack.stop();
 			finish();
 			break;
 		case R.id.playback_edit:
 			audioTrack.stop();
 			Intent intent = new Intent(PlaybackModeLegacy.this,
 					EditModeLegacy.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 			intent.putExtra("song object", song);
 			startActivity(intent);
 			break;
 		}
 		return false;
 	}
 
 	public void addClefMeterKey(Song s) {
 		int clef = s.getClef();
 		if (clef == 1) {
 			clef_image.setImageResource(R.drawable.treble_clef);
 		}
 		meter_disp.setText("" + s.getMeterTop() + "\n" + s.getMeterBottom());
 	}
 
 	public void initArrowTimer() {
 		int g = 0;
 		arrowTimer = new Timer();
 		long time = 0;
 		group.removeView(arrow);
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		params.topMargin = (int) 50;
 		params.leftMargin = 120;
 		arrow.setLayoutParams(params);
 		group.addView(arrow);
 		arrow.setVisibility(0);
 		for (Note n : song.getNotes().getNotes()) {
 			time += n.getLength() * 1000;
 			g++;
 			if (g == MAX_NOTES_ONSCREEN) {
 				// this means that we are scheduling the arrow for the next
 				// screen
 				g = 0;
 				// now we need to click the next screen button.
 				arrowTimer.schedule(new TimerTask() {
 					@Override
 					public void run() {
 						class NextScreenClicker implements Runnable {
 							@Override
 							public void run() {
 								right_button.callOnClick();
 							}
 
 						}
 						runOnUiThread(new NextScreenClicker());
 					}
 				}, time);
 			}
 			final int x = 60 + (g + 1) * 60;
 			arrowTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					class ArrowRun implements Runnable {
 						int x;
 
 						public ArrowRun(int x) {
 							this.x = x;
 						}
 
 						@Override
 						public void run() {
 							group.removeView(arrow);
 							RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 									RelativeLayout.LayoutParams.WRAP_CONTENT,
 									RelativeLayout.LayoutParams.WRAP_CONTENT);
 							params.topMargin = (int) 50;
 							params.leftMargin = x;
 							arrow.setLayoutParams(params);
 							group.addView(arrow);
 							arrow.setVisibility(0);
 						}
 
 					}
 					runOnUiThread(new ArrowRun(x));
 				}
 			}, time);
 		}
 		arrowTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				class DoneClicker implements Runnable {
 					@Override
 					public void run() {
 						group.removeView(arrow);
 						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 								RelativeLayout.LayoutParams.WRAP_CONTENT,
 								RelativeLayout.LayoutParams.WRAP_CONTENT);
 						params.topMargin = (int) 50;
 						params.leftMargin = 120;
 						arrow.setLayoutParams(params);
 						group.addView(arrow);
 						arrow.setVisibility(0);
 						play_pause.performClick();
 						rewind_button.performClick();
 						generateScreen(0);
 					}
 
 				}
 				runOnUiThread(new DoneClicker());
 			}
 		}, time + 1000);
 	}
 
	void genTone() {
 		double duration = 0; // seconds
 		for (Note n : song.getNotes().getNotes()) {
 			duration += n.getLength(); // TODO tempo!!
 		}
 		numSamples = (int) (duration * sampleRate);
 		double[] sample = new double[numSamples];
 		generatedSnd = new byte[2 * numSamples];
 		int j = 0;
 		for (Note n : song.getNotes().getNotes()) {
 			Log.d("", "Pitch: " + n.getPitch());
 			double freqOfTone = n.getPitch(); // hz
 			if (n.isRest()) {
 				freqOfTone = 1;
 			}
 			int numThisNote = (int) (n.getLength() * sampleRate);
 			Log.d("", "J is: " + j);
 			// fill out the array
 			for (int i = 0; i < numThisNote; ++i) {
 				sample[j] = Math.sin(2 * Math.PI * i
 						/ (sampleRate / freqOfTone));
 				j++;
 			}
 		}
 
 		// convert to 16 bit pcm sound array
 		// assumes the sample buffer is normalised.
 		int idx = 0;
 		for (final double dVal : sample) {
 			// scale to maximum amplitude
 			final short val = (short) ((dVal * 32767));
 			// in 16 bit wav PCM, first byte is the low order byte
 			generatedSnd[idx++] = (byte) (val & 0x00ff);
 			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
 		}
 		Log.wtf("", "Buffer size in bytes: " + numSamples);
 		makeTrack();
 	}
 
 	public void makeTrack() {
 		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
 				AudioFormat.CHANNEL_OUT_DEFAULT,
 				AudioFormat.ENCODING_PCM_16BIT, 2 * numSamples,
 				AudioTrack.MODE_STATIC);
 		audioTrack.write(generatedSnd, 0, generatedSnd.length);
 		audioTrack.setPlaybackRate(8000);
 	}
 }
