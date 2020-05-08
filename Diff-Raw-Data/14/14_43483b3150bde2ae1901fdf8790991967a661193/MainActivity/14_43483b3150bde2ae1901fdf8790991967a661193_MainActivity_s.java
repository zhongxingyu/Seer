 package com.bettername.thepokemonone;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.LightingColorFilter;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.bettername.thepokemonone.model.Player;
 import com.bettername.thepokemonone.model.Player.Gender;
 
 public class MainActivity extends Activity implements OnClickListener
 {
 	Context appContext = this;
 	EditText usernameText;
 	Button boyButton;
 	Button girlButton;
 	Button continueButton;
 
 
 	private void playMp3(byte[] mp3SoundByteArray) {
 //	    try {
 	        // create temp file that will hold byte array
 //	        File tempMp3 = File.createTempFile("pirate", "mp3", getCacheDir());
 //	        tempMp3.deleteOnExit();
 //	        FileOutputStream fos = new FileOutputStream(tempMp3);
 //	        fos.write(mp3SoundByteArray);
 //	        fos.close();
 
 	        // Tried reusing instance of media player
 	        // but that resulted in system crashes...  
	        MediaPlayer mp = MediaPlayer.create(this, R.raw.pirate);
	        mp.start();
 	        
 	        // Tried passing path directly, but kept getting 
 	        // "Prepare failed.: status=0x1"
 	        // so using file descriptor instead
 //	        FileInputStream fis = new FileInputStream(tempMp3);
 //	        mediaPlayer.setDataSource(fis.getFD());
 //
 //	        mediaPlayer.prepare();
 //	        mediaPlayer.start();
 //	    } catch (IOException ex) {
 //	    	System.out.println("ERRORORORORORORORORO");
 //	        String s = ex.toString();
 //	        ex.printStackTrace();
 //	    }
 	}
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		playMp3(new byte[5000]);
 
 
 		usernameText = (EditText) findViewById(R.id.username_edittext);
 		boyButton = (Button) findViewById(R.id.boy_button);
 		girlButton = (Button) findViewById(R.id.girl_button);
 		continueButton = (Button) findViewById(R.id.continue_button);
 
 		Config.setContext(appContext);
 		Player.createCurrentUser();
 
 		final int mul = 0x8A8A8A8A;
 		boyButton.getBackground().setColorFilter(
 				new LightingColorFilter(mul, 0X00000000));
 		girlButton.getBackground().setColorFilter(
 				new LightingColorFilter(mul, 0X00000000));
 
 		boyButton.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				boyButton.getBackground().clearColorFilter();
 				girlButton.getBackground().setColorFilter(
 						new LightingColorFilter(mul, 0X00000000));
 				Player.getCurrentUser().setGender(Gender.Male);
 			}
 		});
 
 		girlButton.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				boyButton.getBackground().setColorFilter(
 						new LightingColorFilter(mul, 0X00000000));
 				girlButton.getBackground().clearColorFilter();
 				Player.getCurrentUser().setGender(Gender.Female);
 			}
 		});
 
 		continueButton.setOnClickListener(this);
 
 		usernameText.setOnEditorActionListener(new OnEditorActionListener()
 		{
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event)
 			{
 				return false;
 			}
 		});
 
 
 
 
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("About")
 		.setMessage(
 				"It's an app! Developed by:\n\n" + "Kristian\n"
 						+ "Nolan\n" + "Robert\n" + "Will\n" + "Holly\n"
 						+ "Kyle\n")
 						.setNeutralButton("OK", new DialogInterface.OnClickListener()
 						{
 							@Override
 							public void onClick(DialogInterface dialog, int which)
 							{
 								Intent continueIntent = new Intent(appContext,
 										MapActivity.class);
 								appContext.startActivity(continueIntent);
 							}
 						});
 		builder.create().show();
 		return true;
 	}
 
 	@Override
 	protected void onPause()
 	{
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@Override
 	protected void onStop()
 	{
 		// TODO Auto-generated method stub
 		super.onStop();
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		String username = usernameText.getText().toString();
 		Player.getCurrentUser().setUsername(username);
 		Intent continueIntent = new Intent(appContext,
 				CreaturePickActivity.class);
 		appContext.startActivity(continueIntent);
 	}
 }
