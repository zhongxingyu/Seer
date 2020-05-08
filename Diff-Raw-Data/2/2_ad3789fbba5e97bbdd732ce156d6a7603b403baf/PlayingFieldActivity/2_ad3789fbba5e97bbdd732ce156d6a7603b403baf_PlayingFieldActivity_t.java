 package com.jand.bombercommander.screens;
 
 import com.jand.bombercommander.R;
 import com.jand.bombercommander.R.layout;
 import com.jand.bombercommander.R.menu;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Picture;
 import android.graphics.drawable.Drawable;
 import android.content.Context;
 import android.content.res.Resources;
 import android.widget.RelativeLayout;
 import android.graphics.drawable.NinePatchDrawable;
 
 public class PlayingFieldActivity extends Activity {
 	private View playingFieldView;
 	Paint paint;
 	Canvas canvas;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		playingFieldView = new View(this);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_playing_field);
 		Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
 		canvas = new Canvas(b);
 		playingFieldView.invalidate();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_playing_field, menu);
 		return true;
 	}
 	
 	private class PlayingFieldView extends View{
 
 		public PlayingFieldView(Context context) {
 			super(context);
 			// TODO Auto-generated constructor stub
 		}
 		@Override
 		public void onDraw(Canvas c){
 			Resources res = getResources();
 		    Drawable myImage = res.getDrawable(R.drawable.bc_aa);
 		    myImage.draw(canvas);
 		}
 		
 	}
 }
