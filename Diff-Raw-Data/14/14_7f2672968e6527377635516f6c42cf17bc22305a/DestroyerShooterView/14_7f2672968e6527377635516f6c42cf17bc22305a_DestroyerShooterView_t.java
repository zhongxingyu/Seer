 package com.example.bato;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.DragEvent;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.TranslateAnimation;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class DestroyerShooterView extends Activity
 {
 	TextView [] positive = new TextView[2];
 	Context mContext;
 	Bitmap cloud;
 	private DestroyerShooter mDestroyerShooter;
     View.OnTouchListener gestureListener;
     int width;
     int height;
     Canvas canvas;
     RelativeLayout layout;
     Animation in;
     Animation out;
     TranslateAnimation LeftToRight;
     ScaleDbAdapter mDbHelper;
     GameDbAdapter mGameDbHelper;
     ArrayList<String> mPositive = new ArrayList<String>();
     ArrayList <String> mMatchNeg = new ArrayList<String>();
     ImageView cannon;
     ImageView rCannon;
     float slope;
     RelativeLayout.LayoutParams params;
     AnimationSet set;
     AlphaAnimation fade;
     TextView score;
     Score mScore;
     ScaleArrayAdapter arrayAdapter;
     int count;
     Typeface sans;
     //I need a new one every time! not just when the screen reloads! Also, I destroy with the correct thought!!
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 	    super.onCreate(savedInstanceState);
 	    this.getActionBar().hide();
    	 	sans = Typeface.create("sans-serif-condensed", Typeface.BOLD);
 	    mContext = this;
 	    mDbHelper=new ScaleDbAdapter(mContext);
 	    mDbHelper.open();
 	    mGameDbHelper = new GameDbAdapter(mContext);
 	    mGameDbHelper.open();
 	    Cursor cursor = mDbHelper.fetchPositives();
 	    	while (cursor.moveToNext())
 	    	{
 	    		mPositive.add(cursor.getString(cursor.getColumnIndexOrThrow(ScaleDbAdapter.COLUMN_NAME_POSITIVE)));
 	    	}
 	    cursor.close();
 	    setContentView(R.layout.activity_destroyer_shooter);
 	    score = (TextView) findViewById(R.id.score);
 	    score.setText("SCORE");
 	    mDestroyerShooter = (DestroyerShooter) findViewById(R.id.anim_view);
 	    arrayAdapter = 	new ScaleArrayAdapter(this, R.layout.positives, android.R.id.text1, mPositive);
 	    ListView listView = (ListView) findViewById(R.id.listview);
 	    listView.setAdapter(arrayAdapter);
 	    mScore = (Score) findViewById(R.id.score_view);
 	    layout = (RelativeLayout) findViewById(R.id.game_view);
 	    cannon = (ImageView) findViewById(R.id.cannon);
 	    rCannon = (ImageView) findViewById(R.id.rcannon);
 	    rCannon.setVisibility(View.INVISIBLE);
 		SharedPreferences preferences = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
 		if (preferences.getString("shooter instructions", null) == null)
 		{
 			
 		AlertDialog.Builder builder = new Builder(mContext);
 		builder.setTitle("Instructions");
 		builder.setMessage("Pick the correct positive thought and shoot down the negative thought!");
 		builder.setPositiveButton(android.R.string.ok, null);
 		builder.create().show();				
 		preferences.edit().putString("shooter instructions", "Yes").commit();
 		}
 	    
 		cannon.setOnDragListener(new OnDragListener()
 		{
 
 			@Override
 			public boolean onDrag(View arg0, DragEvent arg1) 
 			{
 				switch (arg1.getAction())
 				{
 					
 				case DragEvent.ACTION_DRAG_ENTERED:
 					cannon.setVisibility(View.INVISIBLE);
 					rCannon.setVisibility(View.VISIBLE);
 					break;
 				case DragEvent.ACTION_DROP:
 						View view = (View) arg1.getLocalState();
 						TextView positive = (TextView) view.findViewById(android.R.id.text1);
 						createPositive(positive.getText().toString());
 						break;
 						
 				case DragEvent.ACTION_DRAG_EXITED:
 					cannon.setVisibility(View.VISIBLE);
 					rCannon.setVisibility(View.INVISIBLE);
 				
 				}
 				
 				return true;
 			}
 			
 		});
 	}
 	
 	@Override
 	 public void onWindowFocusChanged(boolean hasFocus) {
 	    super.onWindowFocusChanged(hasFocus);
 	     
 	    mDestroyerShooter.setOnTouchListener(new OnTouchListener()
 	    {
 
 			@Override
 			public boolean onTouch(View arg0, MotionEvent event) {
 				int action = event.getActionMasked();
 				if (rCannon.getVisibility() == View.VISIBLE)
 				{
 				if (action == MotionEvent.ACTION_DOWN)
 				{
 			       float x = event.getX();
 			       float y = 0;
 			       positive[1] = new TextView (mContext);
 			       positive[1].layout(0, 0, mDestroyerShooter.width/3, mDestroyerShooter.height/4);
 				   positive[1].setGravity(Gravity.CENTER);
 				   positive[1].setTextSize(15);
 				   positive[1].setTextColor(Color.RED);
 				   positive[1].setTypeface(Typeface.DEFAULT_BOLD);
 				   positive[1].setShadowLayer(5, 2, 2, Color.YELLOW);
 				   positive[1].setDrawingCacheEnabled(true);
 				   positive[1].setBackgroundResource(R.drawable.whitecloud);
 				   positive[1].setTypeface(sans);
 				   positive[1].setText(positive[0].getText().toString());
 				   rCannon.setVisibility(View.INVISIBLE);
 				   cannon.setVisibility(View.VISIBLE);
 
 			       Cursor mMatch = mDbHelper.fetchThought(positive[1].getText().toString());
 			       while (mMatch.moveToNext())
 			       {
 			    	   mMatchNeg.add(mMatch.getString(mMatch.getColumnIndexOrThrow(ScaleDbAdapter.COLUMN_NAME_NEGATIVE)));
 			    	   if (mMatchNeg.contains(mDestroyerShooter.negative.getText().toString()))
 			    	   {
 	    					mDestroyerShooter.match = true;
 	    					//scores code will go here
 	    					if (mMatch.getInt(mMatch.getColumnIndexOrThrow(ScaleDbAdapter.COLUMN_NAME_HELPFUL)) > 3 &&
 	    							mMatch.getInt(mMatch.getColumnIndexOrThrow(ScaleDbAdapter.COLUMN_NAME_BELIEVE)) >3)
 	    					{
 	    						count = 75;
 	    					}
 	    					
 	    					else
 	    					{
 	    						count = 25;
 	    					}
 	    					if (mDestroyerShooter.match == true)
 	    					{
 	    						break;
 	    					}
 
 			    	   }
 			       }
 			       
			       mMatchNeg.clear();
 			       mMatch.close();
 			       mDestroyerShooter.touched = true;
 			       mDestroyerShooter.clear = true;
 			       
 			       mDestroyerShooter.positive = positive[1];
 			       mDestroyerShooter.move_to_x = x;
 			       mDestroyerShooter.move_to_y = y;
 			       
 			}
 			       return true;
 			}
 				else
 				{
 					return true;
 				}
 			}
 	    });
 
 
 
 }
 	public void createPositive(String positive_string)
 	{
 		positive[0]= new TextView(mContext);
 		params = new RelativeLayout.LayoutParams(mDestroyerShooter.width/3, mDestroyerShooter.height/4);
 		positive[0].layout(0, 0, mDestroyerShooter.width/3, mDestroyerShooter.height/4);
 		positive[0].setGravity(Gravity.CENTER);
 		positive[0].setTextSize(15);
 		positive[0].setTextColor(Color.RED);
 		positive[0].setTypeface(Typeface.DEFAULT_BOLD);
 		positive[0].setShadowLayer(5, 2, 2, Color.YELLOW);
 		positive[0].setDrawingCacheEnabled(true);
 		positive[0].setBackgroundResource(R.drawable.whitecloud);
 		positive[0].setTypeface(sans);
 		positive[0].setText(positive_string);
 	}
 
 	protected void clear(Context context)
 	{
 		cannon.setVisibility(View.VISIBLE);
 	}
 	
 	protected void update(Context context)
 	{
 		mScore.fin += count;
 	}
 	
 	protected void updateDb()
 	{
 		mGameDbHelper.createGame(mScore.count);
 	}
 
 
 
 	 public void mSwitchSuccess()
 	 {
 		   String item=mDestroyerShooter.positive.getText().toString();
 		   mPositive.remove(item);
 		   arrayAdapter.notifyDataSetChanged(); 
 	 }
 	 
 	 public void game_over()
 	 {
 			AlertDialog.Builder builder = new Builder(mContext);
 			builder.setTitle("Great Job!");
 			builder.setPositiveButton("Play Again", new DialogInterface.OnClickListener()
     		{
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) 
 				{
 					Intent i = new Intent(mContext, DestroyerShooterView.class);				
     				mContext.startActivity(i);	
 				}
 				
     		});
 			builder.setNegativeButton("Go Home", new DialogInterface.OnClickListener()
     		{
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) 
 				{
 					finish();
 					Intent i = new Intent(mContext, MainActivity.class);				
     				mContext.startActivity(i);	
 				}
 				
     		});
 			builder.create().show();	
 	 }
 	   
 		@Override
 		public void onDestroy()
 		{
 			super.onDestroy();
 			updateDb();
 			mDbHelper.close();
 			mGameDbHelper.close();
 		}
 }
