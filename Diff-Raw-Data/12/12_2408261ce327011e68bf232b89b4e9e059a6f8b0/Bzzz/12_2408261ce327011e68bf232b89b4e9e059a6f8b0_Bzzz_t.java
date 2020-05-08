 package com.joilnen;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnTouchListener;
 import android.view.MotionEvent;
 import android.view.KeyEvent;
 import android.widget.Toast;
 import android.util.Log;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 import com.joilnen.SkinType;
 import com.joilnen.Mosca;
 import com.joilnen.Bolo;
 import com.joilnen.GameState;
 import com.joilnen.StatOption;
 import com.joilnen.EventCatchHelper;
 import com.joilnen.BigMosca;
 import com.joilnen.SoundEfect;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.Random;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Bzzz extends Activity implements OnTouchListener, SensorEventListener
 {
 	RenderView renderView = null;
 	MenuView menuView = null;
 	DrawGameHelper drawHelper = null;
 
 	public  class RenderView extends View implements OnTouchListener {
 
 		List<Mosca> moscas = new LinkedList<Mosca>();
 		List<Mosca> moscas2 = new LinkedList<Mosca>();
 		List<BigMosca> big_moscas = new LinkedList<BigMosca>();
 
 		Bolo bolo;
 
 		// Implementado usando memento para p3
 		GameState<Integer> gameState;
 		// Implementado usando singleton para p3 (opcoes do usuario tipo sem som essas coisas)
 		StatOption options;
 
 		RenderView(Context context) {
 			super(context);
 			try {
 				for(int i = 0; i < 3; i++) {
 					Mosca m = new Mosca(context);
 					if(i == 0 || i == 3 || i == 6) m.setStatus(SkinType.VOANDO_D);
 					else m.setStatus(SkinType.VOANDO_E);
 					moscas.add(m);
 				}
 
 				for(int i = 0; i < 3; i++) {
 					Mosca m = new Mosca(context);
 					if(i == 0 || i == 3 || i == 6) m.setStatus(SkinType.VOANDO_D);
 					else m.setStatus(SkinType.VOANDO_E);
 					moscas2.add(m);
 				}
 
 				big_moscas.add(new BigMosca(context));
 				big_moscas.add(new BigMosca(context));
 				big_moscas.get(1).setStatus(SkinType.VOANDO_D);
 
 				bolo = new Bolo(context);
 			}
 			catch(Exception e) {
 				Log.d("Bzzz", "Nao consegui instanciar a moscas");
 			}
 
 			gameState = new GameState<Integer>(GameStateType.IN_MENU);
 			drawHelper = new DrawGameHelper(this);
 		}
 
 		protected void onDraw(Canvas canvas) {
 			drawHelper.draw(canvas);
 			invalidate();
 		}
 
 		public boolean onTouch(View v, MotionEvent event) {
 
 			// try { Thread.sleep(16); } catch (Exception e) {  }
 			return new EventCatchHelper(this, event).doCatch();
 		}
 
 		public void onSensorChanged(SensorEvent event) {
 			new EventCatchHelper(this, event).doCatch();
 		}
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		menuView = new MenuView(this);
 		menuView.setOnTouchListener(this);
 		setContentView(menuView);
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		if(renderView == null) {
 			if(event.getX() > 91 && event.getX() < 248  &&
 			   event.getY() > 235 && event.getY() < 315) {
 				SoundEfect.getSingleton(v.getContext()).play(SoundEfectType.PLUK);
 
 				renderView = new RenderView(this);
 				renderView.setOnTouchListener(this);
 				setContentView(renderView);
 			}
 			else if(event.getX() > 115 && event.getX() < 215  &&
 				event.getY() > 348 && event.getY() < 395) {
 				SoundEfect.getSingleton(v.getContext()).play(SoundEfectType.PLUK);
 				finish();
 			}
 
 			return true;
 		}
 		else {
 			switch(event.getAction()) {
 				case MotionEvent.ACTION_CANCEL:
 					menuView = new MenuView(this);
 					menuView.setOnTouchListener(this);
 					setContentView(menuView);
 					renderView = null;
 					return true;
 			}
 			return renderView.onTouch(v, event);
 		}
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		renderView.onSensorChanged(event);
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 	}
 
	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		
 		if(keyCode == KeyEvent.KEYCODE_BACK && renderView != null) {
 			menuView = new MenuView(this);
 			menuView.setOnTouchListener(this);
 			setContentView(menuView);
 			renderView = null;
 			return true;
 		}
 
		return super.onKeyUp(keyCode, event);
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return true;
 	}
 


 	public  class MenuView extends View implements OnTouchListener {
 
 		Bitmap menuBitmap = null;
 		Bitmap jogarBitmap = null;
 		Bitmap sairBitmap = null;
 
 		MenuView(Context context) {
 			super(context);
 			try {
 				InputStream s = context.getAssets().open("bzzz_menu_c1.png"); 
 				menuBitmap = BitmapFactory.decodeStream(s); 
 				s.close();
 			}
 			catch(Exception e) {
 
 			}
 		}
 
 		protected void onDraw(Canvas canvas) {
 
 			if(menuBitmap != null) {
 				Rect dst = new Rect(0, 0, canvas.getWidth() - 1, canvas.getHeight() - 1);
 				canvas.drawBitmap(menuBitmap, null, dst, null); 
 				// Paint p = new Paint();
 				// p.setStyle(Style.STROKE);
 				// canvas.drawRect(91, 248, 235, 315, p);
 				// canvas.drawRect(115, 348, 215, 395, p);
 			}
 			invalidate();
 		}
 
 		public boolean onTouch(View v, MotionEvent event) {
 
 			// try { Thread.sleep(16); } catch (Exception e) {  }
 			return true;
 		}
 
 		public void onSensorChanged(SensorEvent event) {
 
 		}
 	}
 }
 
 
