 package com.example.wiiphone;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.PowerManager.WakeLock;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 
 public class MainActivity extends Activity
 {
 	private Context mContext = null;
     private WakeLock mWakeLock = null;
     private TCPClient mTcpClient = null;
     private ConnectTask mConnectTask = null;
     
     public TCPClient GetTCP()
     {
     	return mTcpClient;
     }
     
 	@Override
     public void onCreate(Bundle savedInstanceState)
     {
     	System.out.println("onCreate START");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         mContext = this;
         
         Button tButton = (Button) findViewById(R.id.game_1);
         // SpaceShip Button
         tButton.setOnClickListener(new OnClickListener() 
         {
 			@Override
 			public void onClick(View v) 
 			{
 				String message = "GAM 1";
 				if(mTcpClient != null)
 				{
 					mTcpClient.sendMessage(message);
 					startActivityByNr(1);
 				}
 			}
 		});
         
         // Labyrinth Button
         tButton = (Button) findViewById(R.id.game_2);
         tButton.setOnClickListener(new OnClickListener() 
         {
 			@Override
 			public void onClick(View v) 
 			{
 				String message = "GAM 2";
 				if(mTcpClient != null)
 				{
 					mTcpClient.sendMessage(message);
 					startActivityByNr(2);
 				}
 			}
 		});
         
         // Helicopter Button
         tButton = (Button) findViewById(R.id.game_3);
         tButton.setOnClickListener(new OnClickListener() 
         {
 			@Override
 			public void onClick(View v) 
 			{
 				String message = "GAM 3";
 				if(mTcpClient != null)
 				{
 					mTcpClient.sendMessage(message);
 					startActivityByNr(3);
 				}
 			}
 		});
 
         // connect to the server.
         mConnectTask = (ConnectTask) new ConnectTask().execute("");
         
         System.out.println("onCreate END");
     }
 	private void startActivityByNr(int nr)
 	{
 		if(nr == 1)
 		{
 			startActivity(new Intent(MainActivity.this, SpaceShipActivity.class));
 		}
 		else if(nr == 2)
 		{
 			startActivity(new Intent(MainActivity.this, LabyrinthActivity.class));
 		}
 		else if(nr == 3)
 		{
 			startActivity(new Intent(MainActivity.this, FPSGameActivity.class));
 		}
 	}
     @Override
     public void onResume()
     {
     	System.out.println("onResume START");
         super.onResume(); 
         if(mWakeLock != null)
         {
         	mWakeLock.acquire();
         }
         System.out.println("onResume END");
     }
     
     @Override
     public void onPause()
     {
     	System.out.println("onPause START");
         super.onPause();
         if(mWakeLock != null)
         {
         	mWakeLock.release();
         }
         System.out.println("onPause END");
     }
     
     @Override
     public void onStart()
     {
     	System.out.println("onStart START");
         super.onStart();
         System.out.println("onStart END");
     }
     
     @Override
     public void onStop()
     {
     	System.out.println("onStop START");
         super.onStop();
         System.out.println("onStop END");
     }
     
     @Override
     public void onDestroy()
     {
     	System.out.println("onDestroy START");
         super.onDestroy();
         
         if(mTcpClient != null)
         {
  			String message = "EXIT";
  			mTcpClient.sendMessage(message);
 	        mTcpClient.stopClient(); // Stop update if we close app
 	    	mConnectTask.cancel(true);
 	    	mConnectTask = null;
         	mTcpClient = null;
         }
         
         System.out.println("onDestroy END");
     }
     
     public class ConnectTask extends AsyncTask<String,String,TCPClient> 
     {
     	
         @Override
         protected TCPClient doInBackground(String... message) 
         {
             //we create a TCPClient object and
             System.out.println("Connection to Server");
     		mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() 
     		{
                 @Override
                 //here the messageReceived method is implemented
                 public void messageReceived(String message) {
                     //this method calls the onProgressUpdate
                     publishProgress(message);
                 }
             }, mContext);
     		
     		if(mTcpClient != null)
     		{
     			mTcpClient.run();
     		}
  
             return null;
         }
         @Override
         protected void onProgressUpdate(String... values) 
         {
             super.onProgressUpdate(values);
             values[0] = values[0].trim();
             System.out.println(values[0]);
             String[] SString = values[0].split(":");
             if(SString[0].equals("CURRENT MODE"))
             {
             	int GameMode = Integer.parseInt(SString[1].trim());
             	if(GameMode != 0)
             	{
             		startActivityByNr(GameMode);
             	}
             }
             if(SString[0].equals("QUITTING"))
             {
             	Log.e("QUIT", "QUTIING GAMEMODE");
         		
             	/*Intent intentB = new Intent();
             	intentB.setClass(SpaceShipActivity.this, MainActivity.class);
             	intentB.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             	startActivity(intentB);*/
             }
             if(SString[0].equals("KEYBOARD CHOSE MODE"))
             {
             	int GameMode = Integer.parseInt(SString[1].trim());
             	if(GameMode != 0)
             	{
             		startActivityByNr(GameMode);
             	}
             }
             if(SString[0].equals("PING"))
             {
            	
             	if(mTcpClient != null)
             	{
             		mTcpClient.sendMessage("PING");
             	}
             }
             if(SString[0].equals("VIB"))
             {
             	long duration = (long) Float.parseFloat(SString[1].trim());
 	            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 	        	v.vibrate(duration);
             }
         }
     }
 }
