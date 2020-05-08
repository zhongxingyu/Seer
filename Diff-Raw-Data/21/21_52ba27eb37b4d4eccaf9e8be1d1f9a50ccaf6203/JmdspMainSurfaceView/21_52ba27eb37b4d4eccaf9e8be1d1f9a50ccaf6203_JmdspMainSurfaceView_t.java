 package name.atsushieno;
 import name.atsushieno.midi.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.Bitmap.Config;
 import android.graphics.Paint.Join;
 import android.graphics.Paint.Style;
 import android.graphics.drawable.BitmapDrawable;
 import android.media.JetPlayer;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class JmdspMainSurfaceView extends Activity {
 	JmdspMainView view;
 
 	class JmdspMainView extends SurfaceView
 		implements SurfaceHolder.Callback, JetPlayer.OnJetEventListener, MidiPlayerCallback  
 	{
 		JmdspMainSurfaceView activity;
 		ScheduledExecutorService executor;
 		long time;
 		int color_background = 0xFF000008;
 		int color_white_key = 0xFFAaAaAa;
 		int color_basic_stroke = 0xFF000000;
 		int color_black_key = 0xFF000000;
 		int color_black_key_edge = 0xFFFfFfFf;
 		int color_keyon = 0xFFFfFf00;
 		int color_aftertouch = 0xFFFf8000;
 		int color_bend = 0xFF0080Ff;
 		int color_hold = 0xFF0080C0;
 		int color_bright = 0xFFFfFfE0;
 		int color_usual = 0xFF3060C0;
 		int color_dark = 0xFF1830C0;
 		int color_hidden = 0xFF000030;
 		int color_ch_base = color_bright;
 		int color_ch_colored = color_usual;
 		int color_ch_dark = color_dark;
 		int color_ch_hidden = color_hidden;
 		int color_ch_text_colored = color_ch_colored;
 		int color_ch_text_base = color_ch_base;
 		int color_ch_text_dark = color_ch_dark;
 		int color_ch_text_hidden = color_ch_hidden;
 		int all_keys, key_lower_bound, key_higher_bound;
 		boolean small_screen;
 		int channels;
 		int key_width;
 		int key_height;
 		float blackKeyWidth;
 		float blackKeyHeight;
 		int ch_height;
 		int text_height;
 		int play_info_section_width;
 		String [] ch_types;
 		Canvas canvas;
 		BitmapDrawable bitmap_drawable;
 		Paint paint = new Paint ();
 		boolean needs_redraw;
 
 		public JmdspMainView(Context context) 
 		{
 			super(context);
 			activity = (JmdspMainSurfaceView) context;
 			key_lower_bound = 24;
 			key_higher_bound = 128;
 			all_keys = key_higher_bound - key_lower_bound;
 			channels = 16;
 			key_width = 7;
 			key_height = 16;
 			blackKeyWidth = (float) (key_width * 0.4 + 1);
 			blackKeyHeight = key_height / 2;
 			ch_height = 32;
 			text_height = 8;
 			play_info_section_width = 200;
 			ch_types = new String [] {"MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI", "MIDI"};
 
 			getHolder().addCallback(this);
 			setFocusable(true);
 			requestFocus();
 		}
 
 		// JetPlayer.OnJetEventListener implementation
 
 		@Override
 		public void onJetEvent(JetPlayer player, short segment, byte track,
 				byte channel, byte controller, byte value)
 		{
 			// TODO Auto-generated method stub
 			switch (controller) {
 			case (byte) 0x90: // note on
 				break;
 			case (byte) 0x80: // note off
 				break;
 			}
 		}
 
 		@Override
 		public void onJetNumQueuedSegmentUpdate(JetPlayer player, int nbSegments) {
 			// nothing to do
 		}
 
 		@Override
 		public void onJetPauseUpdate(JetPlayer player, int paused)
 		{
 			// nothing to do yet
 		}
 
 		@Override
 		public void onJetUserIdUpdate(JetPlayer player, int userId,
 				int repeatCount)
 		{
 			// nothing to do
 		}
 		
 		// end of JetPlayer.OnJetEventListener implementation
 		
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			int x = (int) event.getX();
 			int y = (int) event.getY();
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				if (play_button.contains(x, y))
 					ProcessPlay();
 				else if (pause_button.contains(x, y))
 					ProcessPause();
 				else if (stop_button.contains(x, y))
 					ProcessStop();
 				else if (ff_button.contains(x, y))
 					ProcessFastForward();
 				else if (rew_button.contains(x, y))
 					ProcessRewind();
 				else if (load_button.contains(x, y))
 					ProcessLoad();
 				break;
 			}
 			// TODO Auto-generated method stub
 			return super.onTouchEvent(event);
 		}
 
 		@Override
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 				int height) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void surfaceCreated(SurfaceHolder holder)
 		{
 			// Init.
 			Rect rect = getHolder().getSurfaceFrame();
 			Bitmap bmp = Bitmap.createBitmap(rect.width(), rect.height(), Config.ARGB_4444);
 			bitmap_drawable = new BitmapDrawable(bmp);
 			canvas = new Canvas(bmp);
 			paint.setStyle(Paint.Style.FILL);
 			paint.setColor(color_background);
 			canvas.drawRect(canvas.getClipBounds(), paint);
 
 			// initialize display size
 			if (rect.width() < 600) {
 				small_screen = true;
 				key_lower_bound = 24;
 				key_higher_bound = 96;
 				all_keys = 96 - 24;
 			}
 
 			for (int i = 0; i < channels; i = i + 1)
 				setupChannelInfo (i);
 			for (int i = 0; i < channels; i = i + 1)
 				setupKeyboard (i);
 
 			setupParameterVisualizer ();
 			setupPlayerStatusPanel ();
 			//addPlayTimeStatusPanel ();
 			//addSpectrumAnalyzerPanel ();
 			//addKeyonMeterPanel ();
 			
 			Canvas c = getHolder().lockCanvas();
 			c.drawBitmap(bmp, new Matrix(), paint);
 			getHolder().unlockCanvasAndPost(c);
 
 			executor = Executors.newSingleThreadScheduledExecutor();
 			executor.scheduleAtFixedRate(new Runnable() {
 				public void run() {
 					if (!needs_redraw)
 						return;
 					UpdateView();
 				}
 			}, 100, 100, TimeUnit.MILLISECONDS);
 
 		}
 		
 		void UpdateView()
 		{
 			Canvas c = getHolder().lockCanvas();
 			if (c != null) {
 				c.drawBitmap(bitmap_drawable.getBitmap(), new Matrix (), paint);
 				needs_redraw = false;
 				getHolder().unlockCanvasAndPost(c);
 			}
 		}
 
 		void setupChannelInfo (int channel)
 		{
 			float yText1 = getChannelYPos (channel) + text_height;
 			float yText2 = getChannelYPos (channel) + text_height * 2;
 			paint.setColor (color_ch_text_colored);
 			paint.setTextSize(16);
 			canvas.drawText ("" + (channel + 1), 35, yText2, paint); // FIXME: nf(x,2)
 			paint.setColor (color_ch_text_colored);
 			paint.setTextSize(8);
 			canvas.drawText (ch_types [channel], 0, yText1, paint);
 			paint.setColor (color_ch_text_base);
 			canvas.drawText ("TRACK.", 0, yText2, paint);
 			paint.setStyle (Paint.Style.STROKE);
 			/*
 			paint.setColor (color_ch_colored);
 			canvas.drawLine (340, getChannelYPos (channel) + 2, 360, getChannelYPos (channel) + text_height - 2, paint);
 			paint.setStyle (Paint.Style.FILL);
 			paint.setColor (color_ch_text_colored);
 			canvas.drawText ("" + 1000, 364, getChannelYPos (channel) + text_height, paint); // FIXME: nf(x,5)
 			paint.setStyle (Paint.Style.FILL);
 			paint.setColor (color_ch_text_base);
 			canvas.drawText ("M:--------", 340, yText2, paint);
 			*/
 		}
 
 		float getChannelYPos (int channel)
 		{
 			return channel * ch_height;
 		}
 		
 		void setupKeyboard (int channel)
 		{
 			int octaves = all_keys / 12;
 			for (int octave = 0; octave < octaves; octave = octave + 1)
 				drawOctave (channel, octave);
 		}
 		
 		void drawOctave(int channel, int octave)
 		{
 			float x = octave * key_width * 7;
 			float y = getChannelYPos (channel) + ch_height - key_height;
 			//ProcessingApplication.Current.pushMatrix (); // user_code
 			//var h = ProcessingApplication.Current.Host; // user_code
 			// user_code
 			for (int n = 0; n < 12; n++)
 			{
 				if (!isWhiteKey (n))
 					continue;
 				int k = key_to_keyboard_idx [n];
 				// user_code
 				paint.setStrokeJoin(Join.ROUND);
 				paint.setStrokeWidth(1);
 				paint.setStyle(Paint.Style.FILL);
 				paint.setColor (color_white_key);
 				Rect rect = new Rect ();
 				rect.left = (int) (x + k * key_width);
 				rect.top = (int) y;
 				rect.right = rect.left + key_width;
 				rect.bottom = rect.top + key_height;
 				canvas.drawRect (rect, paint);
 				paint.setStyle(Paint.Style.STROKE);
 				paint.setColor(color_basic_stroke);
 				canvas.drawRect (rect, paint);
 				// /user_code
 				//key_rectangles [channel, octave * 12 + n] = (Rectangle) h.Children.Last ();
 			}
 			// user_code
 			//var wh = ProcessingApplication.Current.Host;
 			//ProcessingApplication.Current.popMatrix ();
 			//ProcessingApplication.Current.Host.Children.Remove (wh);
 			//white_key_panel.Children.Add (wh);
 
 			//ProcessingApplication.Current.pushMatrix ();
 			//h = ProcessingApplication.Current.Host;
 			// /user_code
 
 			paint.setStrokeJoin(Join.BEVEL);
 			paint.setStrokeWidth(1);
 			for (int n = 0; n < 12; n++)
 			{
 				if (isWhiteKey (n))
 					continue;
 				paint.setStyle(Paint.Style.FILL);
 				paint.setColor(color_black_key);
 				int k = key_to_keyboard_idx [n];
 				// custom_code
 				if (k != 2 && k != 6) {
 					int blackKeyStartX = (int) (x + (k + 0.8) * key_width);
 					Rect rect = new Rect ();
 					rect.left = blackKeyStartX;
 					rect.top = (int) (y + 1);
 					rect.right = (int) (rect.left + blackKeyWidth);
 					rect.bottom = (int) (rect.top + blackKeyHeight);
 					canvas.drawRect (rect, paint);
 					// /user_code
 					//key_rectangles [channel, octave * 12 + n] = (Rectangle) h.Children.Last ();
 					float bottom = y + blackKeyHeight + 1;
 					paint.setStyle(Paint.Style.STROKE);
 					paint.setColor(color_black_key_edge);
 					canvas.drawLine(blackKeyStartX + 1, bottom, blackKeyStartX + blackKeyWidth - 1, bottom, paint);
 				}
 			}
 			// user_code
 			//var bh = ProcessingApplication.Current.Host;
 			//ProcessingApplication.Current.popMatrix ();
 			//ProcessingApplication.Current.Host.Children.Remove (bh);
 			//black_key_panel.Children.Add (bh);
 			// /user_code
 		}
 
 		int [] key_to_keyboard_idx = {0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6};
 		boolean isWhiteKey (int note)
 		{
 			switch (note % 12) {
 			case 0: case 2: case 4: case 5: case 7: case 9: case 11:
 				return true;
 			default:
 				return false;
 			}
 		}
 
 		int getKeyIndexForNote (int value)
 		{
 			int note = value - key_lower_bound;
 			if (note < 0 || note < key_lower_bound || key_higher_bound < note)
 				return -1;
 			return note;
 		}
 
 		@Override
 		public void surfaceDestroyed(SurfaceHolder holder)
 		{
 			executor.shutdown();
 		}
 		
 		Rect getRect (int x, int y, float width, float height)
 		{
 			Rect rect = new Rect ();
 			rect.left = x;
 			rect.top = y;
 			rect.right = (int) (x + width);
 			rect.bottom = (int) (y + height);
 			return rect;
 		}
 
 		int left_base;
 		Rect play_button, pause_button, stop_button,
 			ff_button, rew_button, load_button;
 
 		PlayerStatusPanel player_status_panel;
 		PlayTimeStatusPanel play_time_status_panel;
 		ParameterVisualizerPanel [] parameter_visualizers;
 		//SpectrumAnalyzerPanel spectrum_analyzer_panel;
 		//KeyonMeterPanel keyon_meter_panel;
 
 		void setupParameterVisualizer ()
 		{
 			parameter_visualizers = new ParameterVisualizerPanel [16];
 			for (int i = 0; i < parameter_visualizers.length; i++) {
 				paint.setColor(0xFF60A0Ff);
 				paint.setStyle(Style.FILL);
 				canvas.translate(0, 8);
 				canvas.drawText("VOL", 80, i * 32, paint);
 				canvas.drawText("EXP", 130, i * 32, paint);
 				canvas.drawText("RSD", 80, i * 32 + 8, paint);
 				canvas.drawText("CSD", 130, i * 32 + 8, paint);
 				canvas.drawText("DSD", 180, i * 32 + 8, paint);
 				if (!small_screen) {
 					canvas.drawText("H", 220, i * 32, paint);
 					canvas.drawText("P", 220, i * 32 + 8, paint);
 					canvas.drawText("So", 240, i * 32, paint);
 					canvas.drawText("SP", 240, i * 32 + 8, paint);
 				}
 				canvas.translate(0, -8);
 
 				ParameterVisualizerPanel p = new ParameterVisualizerPanel ();
 				p.location = new Point (80, i * 32);
 				parameter_visualizers [i] = p;
 			}
 		}
 		
 		void setupPlayerStatusPanel()
 		{
 			left_base= small_screen ? 280 : 400;
 			paint.setTextSize(14);
			play_button = getRect (left_base + 50, 50 + 20, paint.measureText("Play"), 14);
			pause_button = getRect (left_base + 90, 50 + 20, paint.measureText("Pause"), 14);
			stop_button = getRect (left_base + 130, 50 + 20, paint.measureText("Stop"), 14);
			ff_button = getRect (left_base + 50, 50 + 40, paint.measureText("FF"), 14);
			rew_button = getRect (left_base + 90, 50 + 40, paint.measureText("Rew"), 14);
			load_button = getRect (left_base + 130, 50 + 40, paint.measureText("Load"), 14);

 			paint.setColor(color_dark);
 			paint.setStyle(Style.FILL);
 			canvas.drawText("Play", play_button.left, play_button.bottom, paint);
 			canvas.drawText("Pause", pause_button.left, pause_button.bottom, paint);
 			canvas.drawText("Stop", stop_button.left, stop_button.bottom, paint);
 			canvas.drawText("FF", ff_button.left, ff_button.bottom, paint);
 			canvas.drawText("Rew", rew_button.left, rew_button.bottom, paint);
 			canvas.drawText("Load", load_button.left, load_button.bottom, paint);
 		}
 
 		JetPlayer jet_player;
 		MidiPlayer midi_player;
 		File midifile;
 
 		void DrawCommon (String s)
 		{
 			synchronized(paint) {
 				int initX = small_screen ? 300 : 400;
 				paint.setStyle (Paint.Style.FILL_AND_STROKE);
 				paint.setColor (color_background);
				canvas.drawRect(initX, 110, 400, 140, paint);
 				paint.setColor (color_dark);
 				paint.setTextSize(16);
				canvas.drawText(s, initX, 140, paint);
 				this.needs_redraw = true;
 			}
 		}
 		void ProcessPlay()
 		{
 			if (jet_player == null)
 				return;
 			midi_player.playAsync();
 			jet_player.play();
 			DrawCommon ("PLAY");
 		}
 		void ProcessPause()
 		{
 			if (jet_player == null)
 				return;
 			midi_player.pauseAsync();
 			jet_player.pause();
 			DrawCommon ("PAUSE");
 		}
 		void ProcessStop()
 		{
 			if (jet_player == null)
 				return;
 			midi_player.close();
 			jet_player.release();
 			jet_player = null;
 			DrawCommon ("STOP");
 		}
 		void ProcessFastForward()
 		{
 			DrawCommon ("not supported yet");
 		}
 		void ProcessRewind()
 		{
 			DrawCommon ("not supported yet");
 		}
 
 		void ProcessLoad()
 		{
 			Intent intent = new Intent("org.openintents.action.PICK_FILE");
 			activity.startActivityForResult(intent, 1);
 		}
 
 		final MidiPlayerCallback callback = this;
 		
 		void LoadFileAsync(File file)
 		{
 			File prevfile = midifile;
 			if (file == prevfile || file == null)
 				return;
 			midifile = file;
 			new Thread (new Runnable () {
 				public void run() {
 					if (jet_player == null)
 						jet_player = JetPlayer.getJetPlayer();
 					try {
 						DrawCommon ("Loading " + midifile.getName());
 						UpdateView();
 						FileOutputStream outStream = getContext().openFileOutput("temporary-songfile.jet", Context.MODE_PRIVATE);
 						File jetFile = getContext().getFileStreamPath("temporary-songfile.jet");
 						new SmfToJetConverter ().convert (midifile, outStream);
 						SmfReader r = new SmfReader(new FileInputStream(midifile));
 						r.parse();
 						midi_player = new MidiPlayer (r.getMusic());
 						midi_player.setCallback(callback);
 						jet_player.loadJetFile(jetFile.getAbsolutePath());
 						jet_player.queueJetSegment(0, -1, 0, 0, 0, (byte) 0);
 						DrawCommon ("Loaded");
 					} catch (SmfParserException ex) {
 						DrawCommon ("Parse error " + ex);
 					} catch (IOException ex) {
 						DrawCommon ("I/O error " + ex);
 					}
 				}
 			}).start();
 		}
 
 		@Override
 		public void onFinished() {
 			stopViews();
 		}
 
 		@Override
 		public void onMessage(SmfMessage message) {
 			handleSmfMessage (message);
 		}
 		
 		void stopViews()
 		{
 			// initialize keyboard
 			for (int i = 0; i < channels; i++)
 				this.setupKeyboard(i);
 		}
 		
 		void drawNoteOnOff(SmfMessage m)
 		{
 			int note = getKeyIndexForNote (m.getMsb());
 			if (note < 0)
 				return; // out of range
 			int octave = note / 12;
 			int key = note % 12;
 			int channel = m.getChannel();
 			boolean isKeyOn = m.getMessageType() == SmfMessage.NoteOn && m.getLsb() != 0;
 
 			float x = octave * key_width * 7;
 			float y = getChannelYPos (channel) + ch_height - key_height;
 			int k = key_to_keyboard_idx [key];
 			if (isWhiteKey (key)) {
 				paint.setColor (isKeyOn ? color_keyon : color_white_key);
 				canvas.drawCircle(x + k * key_width + 3, y + 12, 2, paint);
 				//keyon_meter_panel.ProcessKeyOn (m.Channel, m.Msb, m.Lsb);
 				//spectrum_analyzer_panel.ProcessKeyOn (m.Channel, m.Msb, m.Lsb);
 			} else {
 				paint.setColor (isKeyOn ? color_keyon : color_black_key);
 				int blackKeyStartX = (int) (x + (k + 0.8) * key_width);
 				canvas.drawCircle(blackKeyStartX + 2, y + 1 + 5, 1, paint);
 			}
 			needs_redraw = true;
 		}
 		void handleSmfMessage(SmfMessage m)
 		{
 			switch (m.getMessageType()) {
 			case SmfMessage.NoteOn:
 			case SmfMessage.NoteOff:
 				drawNoteOnOff (m);
 				break;
 				/*
 			case SmfMessage.Program:
 				keyon_meter_panel.SetProgram (m.Channel, m.Msb);
 				break;
 			case SmfMessage.CC:
 				switch (m.Msb) {
 				case SmfCC.BankSelect:
 					keyon_meter_panel.SetBank (m.Channel, m.Lsb, true);
 					break;
 				case SmfCC.BankSelectLsb:
 					keyon_meter_panel.SetBank (m.Channel, m.Lsb, false);
 					break;
 				case SmfCC.Pan:
 					keyon_meter_panel.SetPan (m.Channel, m.Lsb);
 					break;
 				case SmfCC.Volume:
 					parameter_visualizers [m.Channel].Volume.SetValue (m.Lsb);
 					break;
 				case SmfCC.Expression:
 					parameter_visualizers [m.Channel].Expression.SetValue (m.Lsb);
 					break;
 				case SmfCC.Rsd:
 					parameter_visualizers [m.Channel].Rsd.SetValue (m.Lsb);
 					break;
 				case SmfCC.Csd:
 					parameter_visualizers [m.Channel].Csd.SetValue (m.Lsb);
 					break;
 				case SmfCC.Hold:
 					parameter_visualizers [m.Channel].Hold.Value = (m.Lsb > 63);
 					if (m.Lsb < 64 && key_rectangles != null) { // reset held keys to nothing
 						for (int i = 0; i < 128; i++) {
 							note = getKeyIndexForNote (i);
 							if (note < 0)
 								continue;
 							var rect = key_rectangles [m.Channel, note];
 							if (rect == null)
 								continue;
 							if (((SolidColorBrush) rect.Fill).Color == color_hold)
 								key_rectangles [m.Channel, note].Fill = IsWhiteKey (i) ? brush_white_key : brush_black_key;
 						}
 					}
 					break;
 				case SmfCC.PortamentoSwitch:
 					parameter_visualizers [m.Channel].PortamentoSwitch.Value = (m.Lsb > 63);
 					break;
 				case SmfCC.Sostenuto:
 					parameter_visualizers [m.Channel].Sostenuto.Value = (m.Lsb > 63);
 					break;
 				case SmfCC.SoftPedal:
 					parameter_visualizers [m.Channel].SoftPedal.Value = (m.Lsb > 63);
 					break;
 				}
 				break;
 			case SmfMessage.Meta:
 				switch (m.MetaType) {
 				case SmfMetaType.TimeSignature:
 					play_time_status_panel.SetTimeMeterValues (m.Data);
 					break;
 				case SmfMetaType.Tempo:
 					foreach (var view in player_status_views)
 						view.ProcessChangeTempo ((int) ((60.0 / SmfMetaType.GetTempo (m.Data)) * 1000000.0));
 					break;
 				}
 				break;
 				*/
 			}
 		}
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		view.ProcessPause();
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		view = new JmdspMainView(this);
 		setContentView(view);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
     	super.onActivityResult(requestCode, resultCode, data);
     	if (resultCode == RESULT_OK && data != null) { 
     		String filename = data.getDataString();
 			if (filename == null)
 				return;
 			if (filename.startsWith("file://"))
 				filename = filename.substring(7); // remove URI prefix
 			
 			this.view.LoadFileAsync (new File (filename));
     	}
 	}
 
 	class ParameterVisualizerPanel extends BitmapDrawable
 	{
 		public ParameterVisualizerPanel ()
 		{
 			paint = new Paint ();
 			paint.setTextSize(7);
 			paint.setColor(0xFF60A0FF);
 			paint.setStyle(Style.FILL);
 		}
 		
 		public Point location;
 		public Paint paint;
 	}
 
 	class PlayerStatusPanel extends BitmapDrawable
 	{
 		public PlayerStatusPanel ()
 		{
 		}
 	}
 	
 	abstract class VisualItem extends BitmapDrawable
 	{
 	}
 
 	class NumericVisualItem extends VisualItem
 	{
 	}
 
 	class PlayTimeStatusPanel extends BitmapDrawable
 	{
 		public PlayTimeStatusPanel ()
 		{
 		}
 	}
 }
