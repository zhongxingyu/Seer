 package com.refnil.uqcard;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class BoardActivity extends Activity implements OnTouchListener {
 	private ImageAdapter adapter;
 	private float pointY;
 	private int phase=1;
 	private int selectedCard = 0;
 	private OnClickListener c = new OnClickListener()
 	{
 
 		public void onClick(View v) {
 			ImageView iv = (ImageView) v;
 			if(phase == 0 && iv.getDrawable() != null && selectedCard != 0)
 			{
 					switch(iv.getId())
 					{
 					case R.id.opponentGeneral :
 					case R.id.opponentBack1 : 
 					case R.id.opponentBack2 :
 					case R.id.opponentBack3 :
 					case R.id.opponentFront1 :
 					case R.id.opponentFront2 :
 					case R.id.opponentFront3 :
 					case R.id.opponentFront4 : 
 					case R.id.opponentFront5 : attackCard(iv);
 												break;
 					}
 			}
 			else
 			{
 				if((phase == 1 && iv.getDrawable() == null) || (phase == 0 && selectedCard == 0))
 				{
 					
 					switch(iv.getId())
 					{
 					case R.id.playerPhenomenon : placeCard(iv);
 												break;
 					case R.id.playerBack1:
 					case R.id.playerBack2:
 					case R.id.playerBack3:
 					case R.id.playerFront1:
 					case R.id.playerFront2:
 					case R.id.playerFront3:
 					case R.id.playerFront4: 
 					case R.id.playerFront5 : if(phase == 0)
 												selectedCard = iv.getId();
 											else
 												placeCard(iv);
 											break;
 					}
 				}
 			}
 			
 		}
 		
 	};
 	
 	private OnLongClickListener lc = new OnLongClickListener(){
 
 		public boolean onLongClick(View v) {
 			ImageView iv = (ImageView) v;
 			int vID = iv.getId();
 			if(vID != R.id.playerDeck && vID != R.id.opponentDeck && iv.getDrawable() != null)
 				showCard(iv);
 			return true;
 		}
 		
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	    View view = inflater.inflate(R.layout.activity_board, null);
    	    setContentView(view);
    	    view.setOnTouchListener(this);       
         
         TextView tv = (TextView) findViewById(R.id.opponentText);
         tv.setText("My opponent");
         tv = (TextView) findViewById(R.id.playerText);
         tv.setText("Me");
         
         //Opponent
         ImageView iv = (ImageView) findViewById(R.id.opponentBack1);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentBack2);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentBack3);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentFront1);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setTag(R.drawable.carreau);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentFront2);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentFront3);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentFront4);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentFront5);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentGeneral);
         iv.setTag(R.drawable.coeur);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.opponentCemetery);
         iv.setTag(R.drawable.coeur);
         iv.setOnLongClickListener(lc);
         
         //Player
         iv = (ImageView) findViewById(R.id.playerBack1);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerBack2);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerBack3);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerFront1);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerFront2);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerFront3);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerFront4);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerFront5);
         iv.setTag(R.drawable.carreau);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerGeneral);
         iv.setTag(R.drawable.coeur);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerPhenomenon);
         iv.setTag(R.drawable.coeur);
         iv.setOnClickListener(c);
         iv.setOnLongClickListener(lc);
         iv = (ImageView) findViewById(R.id.playerCemetery);
         iv.setTag(R.drawable.coeur);
         iv.setOnLongClickListener(lc);
         
         // Hand initialisation
         
        
         
         Gallery gallery = (Gallery)findViewById(R.id.Gallery);
         adapter = new ImageAdapter(this);
         gallery.setAdapter(adapter);   
         
         gallery.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Intent i = new Intent(getApplicationContext(), FullCardActivity.class);
 				int id = (int) adapter.getItemId(arg2);
 				i.putExtra("id", id);
 				startActivity(i);
 				
 			}
         });
         
         SemiClosedSlidingDrawer slider = (SemiClosedSlidingDrawer) findViewById(R.id.mySlidingDrawer);
         slider.setOnDrawerOpenListener(new com.refnil.uqcard.SemiClosedSlidingDrawer.OnDrawerOpenListener()
         {
 
 			public void onDrawerOpened() {
 				Gallery gallery = (Gallery)findViewById(R.id.Gallery);
 				gallery.setScaleY((float) 2);
 				gallery.setScaleX((float) 1.5);
 			}
         	
         });
         
         slider.setOnDrawerCloseListener(new com.refnil.uqcard.SemiClosedSlidingDrawer.OnDrawerCloseListener()
         {
 
 			public void onDrawerClosed() {
 				Gallery gallery = (Gallery)findViewById(R.id.Gallery);
 				gallery.setScaleY((float) 0.9);
 				gallery.setScaleX((float) 0.9);
 			}
         	
         });
         
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_board, menu);
         return true;
     }
 	
 	public void attackCard(ImageView iv)
 	{
 		if(selectedCard != 0)
 		{
 
 			Toast.makeText(getApplicationContext(), "Attack done", Toast.LENGTH_SHORT).show();
 			selectedCard = 0;
 			
 			//TEMP
 			phase = 1;
 		}
 	}
 	
 	public void placeCard(ImageView iv)
 	{
 		if(selectedCard != 0)
 		{
 			iv.setImageResource(selectedCard);
 			iv.setTag(selectedCard);
 			selectedCard = 0;
 			
 			//TEMP
 			phase = 0;
 		}
 	}
 	
 	public void showCard(ImageView image)
 	{
 		Intent i = new Intent(getApplicationContext(), FullCardActivity.class);
 		int id=(Integer) image.getTag();
 		i.putExtra("id",id);
 		startActivity(i);
 	}
 
 	public boolean onTouch(View arg0, MotionEvent event) {
 		 float eventY = event.getY();
 
 		    switch (event.getAction()) {
 		    case MotionEvent.ACTION_DOWN:
 		      pointY=eventY;
 		      return true;
 		    case MotionEvent.ACTION_MOVE:
 		      break;
 		    case MotionEvent.ACTION_UP:
 		    	SemiClosedSlidingDrawer slider = (SemiClosedSlidingDrawer) findViewById(R.id.mySlidingDrawer);
 		    	Gallery gallery = (Gallery)findViewById(R.id.Gallery);
 		    	if(eventY<pointY)
 		    	{
 		    		selectedCard = (int)gallery.getSelectedItemId();
 		        	slider.animateClose();
 		        	pointY =0;
 		    	}
 		      break;
 		    default:
 		      return false;
 		    }
 			return false;
 	}
 }
