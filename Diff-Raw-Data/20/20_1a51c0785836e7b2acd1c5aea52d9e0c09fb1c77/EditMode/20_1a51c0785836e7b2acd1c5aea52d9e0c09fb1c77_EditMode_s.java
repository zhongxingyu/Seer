 package com.MusicalSketches;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.MediaRecorder;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.MusicalSketches.datarep.Note;
 import com.MusicalSketches.datarep.NoteFrequencies;
 import com.MusicalSketches.datarep.Song;
 
 public class EditMode extends Activity {
 	private Song song;
 	int notesOnScreen = 0;
 
 	public enum types {
 		note, annotation, sharp, flat,natural
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_mode);
 
 		song = (Song) getIntent().getSerializableExtra("song object");
 		
 		addClefMeterKey(song);
 
 		ImageButton left_note = (ImageButton) findViewById(R.id.left_note);
 		ImageButton middle_note = (ImageButton) findViewById(R.id.middle_note);
 		ImageButton right_note = (ImageButton) findViewById(R.id.right_note);
 		ImageButton more_notes = (ImageButton) findViewById(R.id.more_notes);
 		Button rests_button = (Button) findViewById(R.id.rests_button);
 		ImageButton record_button = (ImageButton) findViewById(R.id.record_button);
 		Button dynamics_button = (Button) findViewById(R.id.dynamics_button);
 		ImageButton slurs_button = (ImageButton) findViewById(R.id.slurs_button);
 		ImageButton sharp_button = (ImageButton) findViewById(R.id.sharp_button);
 		ImageButton flat_button = (ImageButton) findViewById(R.id.flat_button);
 		ImageButton natural_button = (ImageButton) findViewById(R.id.natural_button);
 		ImageView music_score = (ImageView) findViewById(R.id.music_score);
 
 		Log.wtf("", "it's open");
 
 		class ButtonTouchListener implements OnTouchListener {
 
 			private int resourceID;
 			private ImageView img;
 			private View v;
 			private int offsetX;
 			private int offsetY;
 			private types code;
 
 			public ButtonTouchListener() {
 				throw new RuntimeException("Unimplemented");
 			}
 
 			// draws the given resourceID
 			public ButtonTouchListener(int resourceID, View v,
 					EditMode.types code) {
 				this.resourceID = resourceID;
 				this.v = v;
 				Log.d("", "image constructed from button");
 				this.code = code;
 			}
 
 			@Override
 			public boolean onTouch(View vi, MotionEvent event) {
 				final int action = event.getAction();
 				switch (action) {
 				case MotionEvent.ACTION_DOWN:
 					renewImage();
 					((ViewGroup) findViewById(R.id.edit_layout)).addView(img);
 					img.setX(event.getX() + offsetX);
 					img.setY(event.getY() + offsetY);
 					Log.d("",
 							"action down: " + event.getX() + " " + event.getY());
 					break;
 				case MotionEvent.ACTION_UP:
 					Log.d("", "action up: " + event.getX() + " " + event.getY());
 					if (this.code == types.note) {
 						addNote(img, this.resourceID);
 					} else if (this.code == types.sharp) {
 						addAnnotation(img, types.sharp);
 					} else if (this.code == types.flat) {
 						addAnnotation(img, types.flat);
 					}  else if (this.code == types.natural) {
 						addAnnotation(img, types.natural);
 					}
 					img = null;
 					break;
 				case MotionEvent.ACTION_MOVE:
 					Log.d("",
 							"action move: " + event.getX() + " " + event.getY());
 					img.setX(event.getX() + offsetX);
 					img.setY(event.getY() + offsetY);
 					break;
 				}
 				return true;
 			}
 
 			public void renewImage() {
 				img = new ImageView(getApplicationContext());
 				this.offsetX = this.v.getLeft() - 15;
 				this.offsetY = this.v.getTop() - 35;
 				img.setImageResource(this.resourceID);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 			}
 		}
 
 		left_note.setOnTouchListener(new ButtonTouchListener(
 				R.drawable.eigth_note_transparent, left_note, types.note));
 		middle_note.setOnTouchListener(new ButtonTouchListener(
 				R.drawable.quarter_note_transparent, middle_note, types.note));
 		right_note.setOnTouchListener(new ButtonTouchListener(
 				R.drawable.half_note_transparent, right_note, types.note));
 		sharp_button.setOnTouchListener(new ButtonTouchListener(
 				R.drawable.sharp_transparent, sharp_button, types.sharp));
 		natural_button.setOnTouchListener(new ButtonTouchListener(
 				R.drawable.natural_transparent, natural_button,
 				types.natural));
 		flat_button.setOnTouchListener(new ButtonTouchListener(
 				R.drawable.flat_transparent, flat_button, types.flat));
 		dynamics_button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				createDynamicsDialog();
 			}
 		});
 
 		record_button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				createRecordDialog();
 			}
 		});
 
 		updateFromLoadedSong(song);
 
 	}
 
 	public void updateFromLoadedSong(Song song) {
 		notesOnScreen = 0;
 		for (Note n : song.getNotes().getNotes()) {
 			ImageView img;
 			img = new ImageView(getApplicationContext());
 			if (n.getLength() == 0.125) {
 				img.setImageResource(R.drawable.eigth_note_transparent);
 			} else if (n.getLength() == 0.25) {
 				img.setImageResource(R.drawable.quarter_note_transparent);
 			} else if (n.getLength() == 0.5) {
 				img.setImageResource(R.drawable.half_note_transparent);
 			}
 			img.setAdjustViewBounds(true);
 			img.setMaxHeight(65);
 			img.setMaxWidth(50);
 			img.setVisibility(0);
 			((ViewGroup) findViewById(R.id.edit_layout)).addView(img);
 			if (n.getName().startsWith("e4")) {
 				img.setY(186);
 			} else if (n.getName().startsWith("f4")) {
 				img.setY(176);
 			} else if (n.getName().startsWith("g4")) {
 				img.setY(166);
 			} else if (n.getName().startsWith("a4")) {
 				img.setY(156);
 			} else if (n.getName().startsWith("b4")) {
 				img.setY(146);
 			} else if (n.getName().startsWith("c5")) {
 				img.setY(136);
 			} else if (n.getName().startsWith("d5")) {
 				img.setY(126);
 			} else if (n.getName().startsWith("e5")) {
 				img.setY(116);
 			} else if (n.getName().startsWith("f5")) {
 				img.setY(106);
 			}
 			addNote(img, 0);
 			notesOnScreen += 1;
 			float y = img.getY();
 			float x = img.getX();
 			if (n.getName().endsWith("flat")) {
 				img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.flat_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				((ViewGroup) findViewById(R.id.edit_layout)).addView(img);
 				img.setY(y);
 				img.setX(x);
 				addAnnotation(img, types.flat);
 			} else if (n.getName().endsWith("sharp")) {
 				img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.sharp_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				((ViewGroup) findViewById(R.id.edit_layout)).addView(img);
 				img.setY(y);
 				img.setX(x);
 				addAnnotation(img, types.sharp);
 			} else if (n.getName().endsWith("natural")) {
 				img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.natural_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				((ViewGroup) findViewById(R.id.edit_layout)).addView(img);
 				img.setY(y);
 				img.setX(x);
 				addAnnotation(img, types.natural);
 			}
 		}
 	}
 
 	public void addAnnotation(View view, types t) {
 		class PlacedItemTouchListener implements OnTouchListener {
 			// TODO figure out why the offsets are -23 and -140...
 			private View v;
 			private types t;
 			float startx;
 			float starty;
 
 			public PlacedItemTouchListener() {
 				throw new RuntimeException("Unimplemented");
 			}
 
 			public PlacedItemTouchListener(View v, types t) {
 				this.v = v;
 				this.t = t;
 			}
 
 			@Override
 			public boolean onTouch(View vi, MotionEvent event) {
 				final int action = event.getAction();
 				switch (action) {
 				case MotionEvent.ACTION_DOWN:
 					this.v.setX(event.getRawX() - 23);
 					this.v.setY(event.getRawY() - 140);
 					Log.d("", "action down (modify): " + event.getRawX() + " "
 							+ event.getRawY());
 					startx = event.getRawX() - 23;
 					starty = event.getRawY();
 					break;
 				case MotionEvent.ACTION_UP:
 					Log.d("", "action up (modify): " + event.getRawX() + " "
 							+ event.getRawY());
 					if (event.getRawX() > 750 && event.getRawY() > 400) {
 						// trash
 						this.v.setVisibility(View.GONE);
 						// delete note ?
 						int noteNum = getNoteNumFromX(startx);
 						if (noteNum < 0) {
 							noteNum = 0;
 						} else if (noteNum >= notesOnScreen) {
 							noteNum = notesOnScreen - 1;
 						}
 						deleteAnnotation(this.v,noteNum);
 					} else {
 						int noteNum = getNoteNumFromX(startx);
 						if (noteNum < 0) {
 							noteNum = 0;
 						} else if (noteNum >= notesOnScreen) {
 							noteNum = notesOnScreen - 1;
 						}
 						deleteAnnotation(this.v,noteNum);
 						updateAnnotation(this.v, t);
 					}
 					break;
 				case MotionEvent.ACTION_MOVE:
 					Log.d("", "action move (modify): " + event.getRawX() + " "
 							+ event.getRawY());
 					this.v.setX(event.getRawX() - 23);
 					this.v.setY(event.getRawY() - 140);
 					break;
 				}
 				return true;
 			}
 		}
 		view.setOnTouchListener(new PlacedItemTouchListener(view, t));
 		// snap to note that's already placed...
 		int noteNum = getNoteNumFromX(view.getX());
 		if (noteNum < 0) {
 			noteNum = 0;
 		} else if (noteNum >= notesOnScreen) {
 			noteNum = notesOnScreen - 1;
 		}
 		updateAnnotation(view, t);
 		Log.d("", "note number is: " + noteNum);
 		view.setX(100 * (noteNum + 1) - 10);
 		view.setY(this.getYFromNote(song.getNoteNum(noteNum).getPitch()));
 	}
 
 	public void addNote(View view, int code) {
 		class PlacedItemTouchListener implements OnTouchListener {
 			// TODO figure out why the offsets are -23 and -140...
 			private View v;
 			private int noteNum;
 
 			public PlacedItemTouchListener() {
 				throw new RuntimeException("Unimplemented");
 			}
 
 			public PlacedItemTouchListener(View v, int noteNum) {
 				this.v = v;
 				this.noteNum = noteNum;
 			}
 
 			@Override
 			public boolean onTouch(View vi, MotionEvent event) {
 				final int action = event.getAction();
 				switch (action) {
 				case MotionEvent.ACTION_DOWN:
 					this.v.setX(event.getRawX() - 23);
 					this.v.setY(event.getRawY() - 140);
 					Log.d("", "action down (modify): " + event.getRawX() + " "
 							+ event.getRawY());
 					break;
 				case MotionEvent.ACTION_UP:
 					Log.d("", "action up (modify): " + event.getRawX() + " "
 							+ event.getRawY());
 					if (event.getRawX() > 750 && event.getRawY() > 400) {
 						// trash
 						this.v.setVisibility(View.GONE);
 						// delete note ?
 						deleteNote(noteNum);
 					} else {
 						updateNote(this.v, noteNum);
 					}
 					break;
 				case MotionEvent.ACTION_MOVE:
 					Log.d("", "action move (modify): " + event.getRawX() + " "
 							+ event.getRawY());
 					this.v.setX(event.getRawX() - 23);
 					this.v.setY(event.getRawY() - 140);
 					break;
 				}
 				return true;
 			}
 		}
 		view.setOnTouchListener(new PlacedItemTouchListener(view, notesOnScreen));
 		snapToBar(view);
 		snapLeftRight(view, notesOnScreen);
 		if (code != 0) {
 			// need to add to the song!
 			// code should be the resource associated with the note.
 			double duration = 0;
 			if (code == R.drawable.eigth_note_transparent) {
 				duration = 0.125;
 			} else if (code == R.drawable.quarter_note_transparent) {
 				duration = 0.25;
 			} else if (code == R.drawable.half_note_transparent) {
 				duration = 0.5;
 			}
 			notesOnScreen++;
 			snapLeftRight(view, notesOnScreen);
 			addToSong(view, duration);
 		}
 	}
 
 	public void addToSong(View v, double length) {
 		song.addNote(new Note(getPitchFromYIndex(v.getY()), length, ""));
 	}
 
 	public void updateAnnotation(View view, types t) {
 		int noteNum = getNoteNumFromX(view.getX());
 		if (noteNum < 0) {
 			noteNum = 0;
 		} else if (noteNum >= notesOnScreen) {
 			noteNum = notesOnScreen - 1;
 		}
 		Log.d("", "notes on screen: " + notesOnScreen);
 		Log.d("", "note number is: " + noteNum);
 		view.setX(100 * (noteNum + 1) - 10);
 		view.setY(this.getYFromNote(song.getNoteNum(noteNum).getPitch()));
 		if (t == types.flat) {
 			song.updateNoteName(noteNum, song.getNoteNum(noteNum).getName()
 					+ "flat");
 		} else if (t == types.sharp) {
 			song.updateNoteName(noteNum, song.getNoteNum(noteNum).getName()
 					+ "sharp");
 		} else if (t == types.natural) {
 			song.updateNoteName(noteNum, song.getNoteNum(noteNum).getName()
 					+ "natural");
 		}
 	}
 	
 	public void deleteAnnotation(View view ,int noteNum) {
 		song.updateNoteName(noteNum, song.getNoteNum(noteNum).getName().substring(0, 2));
 		Log.d("","Deleted annotation: " + noteNum);
 	}
 
 	public void deleteNote(int noteNum) {
 		notesOnScreen -= 1;
 	}
 
 	public void updateNote(View v, int noteNum) {
 		snapToBar(v);
 		snapLeftRight(v, noteNum);
 		song.updateNotePitch(noteNum, getPitchFromYIndex(v.getY()));
 		song.updateNoteName(noteNum, getNameFromYIndex(v.getY()));
 		Log.d("", "updated note pitch: " + (noteNum));
 	}
 
 	public double getPitchFromYIndex(float y) {
 		if (191 >= y && y > 181) {
 			return NoteFrequencies.getFrequency("e4");
 		} else if (181 >= y && y > 171) {
 			return NoteFrequencies.getFrequency("f4");
 		} else if (171 >= y && y > 161) {
 			return NoteFrequencies.getFrequency("g4");
 		} else if (161 >= y && y > 151) {
 			return NoteFrequencies.getFrequency("a4");
 		} else if (151 >= y && y > 141) {
 			return NoteFrequencies.getFrequency("b4");
 		} else if (141 >= y && y > 131) {
 			return NoteFrequencies.getFrequency("c5");
 		} else if (131 >= y && y > 121) {
 			return NoteFrequencies.getFrequency("d5");
 		} else if (121 >= y && y > 111) {
 			return NoteFrequencies.getFrequency("e5");
 		} else if (111 >= y && y > 101) {
 			return NoteFrequencies.getFrequency("f5");
 		}
 		return -1;
 	}
 
 	public String getNameFromYIndex(float y) {
 		if (191 >= y && y > 181) {
 			return "e4";
 		} else if (181 >= y && y > 171) {
 			return "f4";
 		} else if (171 >= y && y > 161) {
 			return ("g4");
 		} else if (161 >= y && y > 151) {
 			return ("a4");
 		} else if (151 >= y && y > 141) {
 			return ("b4");
 		} else if (141 >= y && y > 131) {
 			return ("c5");
 		} else if (131 >= y && y > 121) {
 			return ("d5");
 		} else if (121 >= y && y > 111) {
 			return ("e5");
 		} else if (111 >= y && y > 101) {
 			return ("f5");
 		}
 		return null;
 	}
 
 	public void snapToBar(View v) {
 		double y = v.getY();
 		if (y > 191) {
 			v.setY(186);
 		} else if (191 >= y && y > 181) {
 			v.setY(186);
 		} else if (181 >= y && y > 171) {
 			v.setY(176);
 		} else if (171 >= y && y > 161) {
 			v.setY(166);
 		} else if (161 >= y && y > 151) {
 			v.setY(156);
 		} else if (151 >= y && y > 141) {
 			v.setY(146);
 		} else if (141 >= y && y > 131) {
 			v.setY(136);
 		} else if (131 >= y && y > 121) {
 			v.setY(126);
 		} else if (121 >= y && y > 111) {
 			v.setY(116);
 		} else if (111 >= y && y > 101) {
 			v.setY(106);
 		} else if (y < 101) {
 			v.setY(106);
 		}
 	}
 
 	public void snapLeftRight(View v, int noteNum) {
 		v.setX(10 + 100 * (noteNum + 1));
 	}
 
 	public int getNoteNumFromX(float x) {
 		return (int) ((x - 10) / 100);
 	}
 
 	public int getYFromNote(double pitch) {
 		if (pitch == NoteFrequencies.getFrequency("e4")) {
 			return 186;
 		} else if (pitch == NoteFrequencies.getFrequency("f4")) {
 			return 176;
 		} else if (pitch == NoteFrequencies.getFrequency("f4sharp")) {
 			return 176;
 		} else if (pitch == NoteFrequencies.getFrequency("g4flat")) {
 			return 166;
 		} else if (pitch == NoteFrequencies.getFrequency("g4")) {
 			return 166;
 		} else if (pitch == NoteFrequencies.getFrequency("g4sharp")) {
 			return 166;
 		} else if (pitch == NoteFrequencies.getFrequency("a4flat")) {
 			return 156;
 		} else if (pitch == NoteFrequencies.getFrequency("a4")) {
 			return 156;
 		} else if (pitch == NoteFrequencies.getFrequency("a4sharp")) {
 			return 156;
 		} else if (pitch == NoteFrequencies.getFrequency("b4flat")) {
 			return 146;
 		} else if (pitch == NoteFrequencies.getFrequency("b4")) {
 			return 146;
 		} else if (pitch == NoteFrequencies.getFrequency("c5")) {
 			return 136;
 		} else if (pitch == NoteFrequencies.getFrequency("c5sharp")) {
 			return 136;
 		} else if (pitch == NoteFrequencies.getFrequency("d5flat")) {
 			return 126;
 		} else if (pitch == NoteFrequencies.getFrequency("d5")) {
 			return 126;
 		} else if (pitch == NoteFrequencies.getFrequency("d5sharp")) {
 			return 126;
 		} else if (pitch == NoteFrequencies.getFrequency("e5flat")) {
 			return 116;
 		} else if (pitch == NoteFrequencies.getFrequency("e5")) {
 			return 116;
 		} else if (pitch == NoteFrequencies.getFrequency("f5")) {
 			return 106;
 		} else if (pitch == NoteFrequencies.getFrequency("f5sharp")) {
 			return 106;
 		}
 
 		return -1;
 	}
 
 	public enum edit_menu_options {
 		UNDO, SAVE, CLOSE, PLAY,
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.edit_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.edit_close:
 			Toast.makeText(this, "Closing!", Toast.LENGTH_SHORT).show();
 			save();
 			finish();
 			break;
 		case R.id.edit_play:
 			Toast.makeText(this, "Play...", Toast.LENGTH_SHORT).show();
 			Intent next = new Intent(EditMode.this, PlaybackMode.class);
 			next.putExtra("song object",song);
 			startActivity(next);
 			break;
 		case R.id.edit_save:
 			Toast.makeText(this, "Save...", Toast.LENGTH_SHORT).show();
 			save();
 			break;
 		case R.id.edit_undo:
 			Toast.makeText(this, "Undo...", Toast.LENGTH_SHORT).show();
 			break;
 		}
 		return false;
 	}
 
 	public void save() {
 		Intent i = new Intent();
 		i.putExtra("song object", song);
 		setResult(0, i);
 	}
 
 	public void createClefDialog() {
 		final CharSequence[] clefs = { "Bass", "Treble" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose New Clef");
 		builder.setItems(clefs, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				song.setClef(item);
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void createDynamicsDialog() {
 		final CharSequence[] dynamics = { "pp", "p", "mp", "mf", "f", "ff" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose New Dynamic");
 		builder.setItems(dynamics, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				// Need code to change the img on the dynamics button to the
 				// selected value.
 
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 
 	}
 
 	public void createKeyDialog() {
 		final CharSequence[] keys = { "C", "G", "D", "A", "E", "B", "F#", "Db",
 				"Ab", "Eb", "Bb", "F", "A minor", "E minor", "B minor",
 				"F# minor", "Db minor", "Ab minor", "Eb minor", "Bb minor",
 				"F minor", "C minor", "G minor", "D minor" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose New Key");
 		builder.setItems(keys, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				song.setKey((String) keys[item]); // not sure this works
 
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	// rules to keep entries within tempo ranges (40 to about 220)
 	// implemented in song.setTempo
 	public void createTempoDialog(int currentTempo) {
 		final View layout = View.inflate(this, R.layout.tempo, null);
 
 		final EditText input = ((EditText) layout.findViewById(R.id.myEditText));
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("New Tempo");
 		builder.setView(layout);
 
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				song.setTempo(Integer
 						.valueOf(input.getText().toString().trim()));
 			}
 		});
 
 		builder.setPositiveButton("Cancel",
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						return;
 					}
 				});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void createMeterDialog() {
 		final View layout = View.inflate(this, R.layout.meter, null);
 
 		final EditText top = ((EditText) layout.findViewById(R.id.meterTop));
 		final EditText bottom = ((EditText) layout
 				.findViewById(R.id.meterBottom));
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("New Meter");
 		builder.setView(layout);
 
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				song.setMeter(Integer.valueOf(top.getText().toString().trim()),
 						Integer.valueOf(bottom.getText().toString().trim()));
 			}
 		});
 
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						return;
 					}
 				});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void createRecordDialog() {
 		final View layout = View.inflate(this, R.layout.recording, null);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 
 		final MediaRecorder recording = new android.media.MediaRecorder();
 		recording.setAudioSource(MediaRecorder.AudioSource.MIC); // can't test
 																	// this with
 																	// emulator
 																	// because
 																	// it has no
 																	// mic.
 																	// should
 																	// work in
 																	// phone
 		recording.start();
 
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				recording.stop();
 				// need to save the recording somewhere
 			}
 		});	
 
 	}
 	
 	@Override
 	public void onBackPressed() {
 		save();
 		finish();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		save();
 	}
 	
 	public void addClefMeterKey(Song s) {
 		int clef = s.getClef();
		if (clef == 0) {
 			((ImageView)findViewById(R.id.clef_image)).setImageResource(R.drawable.treble_clef);
 		} 
 		((TextView)findViewById(R.id.meter_text)).setText(""+s.getMeterTop()+"\n"+s.getMeterBottom());
 	}
 }
