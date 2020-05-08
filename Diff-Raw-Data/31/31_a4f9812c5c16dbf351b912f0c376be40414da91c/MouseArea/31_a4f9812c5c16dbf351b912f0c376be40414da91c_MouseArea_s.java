 package com.maggen.wimouse;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import com.maggen.udp.client.UDPClient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.MotionEvent;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 
 public class MouseArea extends Activity implements OnTouchListener{
 
 	private Pad view;
 	private UDPClient client;
 //	private AlertDialog.Builder dia;
 
 	public void onCreate(Bundle savedInstanceSate)
 	{
 		super.onCreate(savedInstanceSate);
 
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		//Initialize the pad surface to be viewed.
 		view  = new Pad(this);
 		view.setOnTouchListener(this);
 		this.setContentView(view);
 
 		//Initialize alert dialog 
 //		dia = new AlertDialog.Builder(this);
 
 		try{
 			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
 			client = new UDPClient(WiMouseActivity.ip, pref);
 		}
 		catch (UnknownHostException e) {
 //			dia.setMessage("Invalid Host");
 //			AlertDialog alert = dia.create();
 //			alert.setTitle("Alert");
 //			alert.show();
 			this.finish();
 
 		}
 		catch (SocketException e) {
 //			dia.setMessage("Invalid socket");
 //			AlertDialog alert = dia.create();
 //			alert.setTitle("Alert");
 //			alert.show();
 			this.finish();
 		}
 	}
 
 	public void onPause()
 	{
 		super.onPause();
 		this.finish();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 
 		float x = event.getX();
 		float y = event.getY();

		//Log.d("update", "x "+x+" y "+y);
 
 		try{
			client.updatePointer(x, y);
 		}
 		catch (IOException e) {
 //			dia.setMessage("Could not send coordinates");
 //			AlertDialog alert = dia.create();
 //			alert.setTitle("Alert");
 //			alert.show();
 			this.finish();
 		}

 		return true;
 	}
 
 
 	/*-------------------------Private class -------------------------------------*/
 
 	private class Pad extends SurfaceView {
 
 		public Pad(Context context)
 		{
 			super(context);
 			this.setWillNotDraw(false);
 		}
 
 		public void onDraw(Canvas c)
 		{	
 			Paint p = new Paint();
 			p.setColor(Color.RED);
 			float width = c.getWidth();
 			float height = c.getHeight();
 
 			//Change the value for the top of the rectangle.
 			double dummy = height - height/4.2;
 			float top=Float.parseFloat(String.valueOf(dummy));
 
 			//Change the value for the right side of the rectangle
 			dummy = width/2.4;
 			float right = Float.parseFloat(String.valueOf(dummy));
 
 			c.drawRect(0, top , right, height, p);
 
 			//Change the value for the left side of the rectangle.
 			dummy = width - width/2.4; 
 			float left = Float.parseFloat(String.valueOf(dummy));
 
 			c.drawRect(left , top , width, height, p);
 
 		}
 	}
 	/*-------------------------Private class -------------------------------------*/
 
 }
