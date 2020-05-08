 package com.MusicalSketches;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.MediaPlayer;
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
 import android.widget.Toast;
 
 import com.MusicalSketches.EditMode.types;
 import com.MusicalSketches.datarep.Note;
 import com.MusicalSketches.datarep.NoteFrequencies;
 import com.MusicalSketches.datarep.Song;
 
 public class PlaybackMode extends Activity {
 	Song song;
 	int notesOnScreen = 0;
 	int state = 0;
 	MediaPlayer mediaPlayer; 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.playback_mode);
 		
 		mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.music);
 
 		song = (Song) getIntent().getSerializableExtra("song object");
 
 		ImageButton play_pause = (ImageButton) findViewById(R.id.play_pause_button);
 		ImageButton rewind_button = (ImageButton) findViewById(R.id.rewind_button);
 		ImageButton forward_button = (ImageButton) findViewById(R.id.forward_button);
 		
 		play_pause.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				ImageButton b =(ImageButton)findViewById(R.id.play_pause_button); 
 				if (state == 0) {
 					state = 1;
 					b.setImageResource(R.drawable.pause);
 					mediaPlayer.start();
 				} else {
 					state =0 ;
 					b.setImageResource(R.drawable.play);
 					mediaPlayer.pause();
 				}
 			}
 		});
 		
 		rewind_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-5000);
 			}
 		});
 		
 		forward_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+5000);
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
 			snapToBar(img);
 			snapLeftRight(img, notesOnScreen);
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
 
 	public void addAnnotation(View view, types t) {
 		// snap to note that's already placed...
 		int noteNum = getNoteNumFromX(view.getX());
 		if (noteNum < 0) {
 			noteNum = 0;
 		} else if (noteNum >= notesOnScreen) {
 			noteNum = notesOnScreen - 1;
 		}
 		Log.d("", "note number is: " + noteNum);
 		view.setX(100 * (noteNum + 1) - 10);
 		view.setY(this.getYFromNote(song.getNoteNum(noteNum).getPitch()));
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
 
 	public enum playback_menu_options {
 		CLOSE, EDIT,
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.playback_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.playback_close:
 			Toast.makeText(this, "Bye!", Toast.LENGTH_SHORT).show();
 			finish();
 			break;
 		case R.id.playback_edit:
 			Toast.makeText(this, "As you wish...", Toast.LENGTH_SHORT).show();
 			Intent next = new Intent(PlaybackMode.this, EditMode.class);
 			next.putExtra("song object", song);
 			startActivity(next);
 			break;
 		}
 		return false;
 	}
 }
