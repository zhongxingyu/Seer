 package Hanes.testProj;
 /*
  * CSH Drink App by Andrew Hanes
  */
 
 import java.util.ArrayList;
 import Hanes.testProj.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.method.PasswordTransformationMethod;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class Drink_Main extends Activity {
 	public Connector drinkServ;
 	SharedPreferences sp;
 	SharedPreferences.Editor edit;
 	TextView title;
 	LinearLayout linearLayout;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		/*
 		 * Sets up the GUI and connector class.  
 		 */
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
 		this.linearLayout = (LinearLayout) findViewById(R.id.widget43);
 		title = new TextView(this);
 		title.setText("Drink App");
 		title.setPadding(linearLayout.getWidth()/2, 10, 0, 20);
 		title.setGravity(Gravity.CENTER_HORIZONTAL);
 		title.setClickable(false);
 		title.setTextColor(Color.BLACK);
 		title.setBackgroundColor(Color.rgb(50, 100, 200));
 		//this.title = (TextView)findViewById(R.id.title);
 		sp = this.getSharedPreferences("drinkPrefs", MODE_WORLD_READABLE);
 		edit = sp.edit();
 		this.drinkServ = new Connector(sp.getString("serv", "drink")+".csh.rit.edu",4242);
 		if(drinkServ.skt == null)
 		{
 			this.serverDown();
 		}
 		else
 		{
 			if(!sp.contains("user"))
 			{
 				this.changeUsernameAlert();
 			}
 			else
 			{
 				title.setText(R.string.title);
 				Log.d("Existing Username","Found existing username "+sp.getString("user", "....nevermind"));
 				if(!(drinkServ.command("GETBALANCE").get(0).indexOf("ERR") == -1))
 					changePasswordAlert();
 			}
 			AlertDialog.Builder noSSLAlert = new AlertDialog.Builder(this);
 			noSSLAlert.setTitle("WARNING");
 			noSSLAlert.setMessage("THIS APP DOES NOT USE ENCRYPTION IN ITS CURRENT STATE\nUSE IT AT YOUR OWN RISK");
 			noSSLAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 
 				}
 			});
 			noSSLAlert.show();
 			/*Done getting user info*/
 			linearLayout.addView(title);
 			linearLayout.setBackgroundColor(Color.GRAY);
 			linearLayout.setPadding(20, 10, 20, 10);
 			updateButtons();
 			Log.d("Done","Done w/ onCreate");
 			//linearLayout.setScaleY(1);
 		}
 	}
 
 	public void displayAlert(String text)
 	/*
 	 * Displays an alert
 	 */
 	{
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Alert");
 		alert.setMessage(text);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 
 			}
 		});
 		alert.show();
 	}
 	public void onDestroy()
 	/*
 	 * Runs when the app is closed
 	 */
 	{
 		super.onDestroy();
 		Log.d("Goodbye Cruel World!!!!","Exiting");
 		drinkServ.close();
 	}
 	public void updateButtons()
 	/*
 	 * Creates buttons
 	 * Preconditions:  linearLayout has been initialized, drinkServ has been initialized
 	 */
 	{
 		this.changeMachine(sp.getString("serv", "d"));
 		linearLayout.removeAllViews();
 		linearLayout.addView(title);
 		ArrayList<drinkButton> buttons = this.getButtons(this.drinkServ);
 		for (drinkButton dB : buttons)
 		{
 			linearLayout.addView(dB);
 		}
 	}
 	public boolean onCreateOptionsMenu(Menu menu)
 	/*
 	 * Uses options_menu.xml to create an options menu
 	 */
 	{
 		MenuInflater inflater = this.getMenuInflater();
 		inflater.inflate(R.layout.options_menu, menu);
 		return true;
 	}
 	public void setDelay()
 	/*
 	 * Allows the user to set a delay.  It is stored in sp with the key "delay"
 	 */
 	{
 		AlertDialog.Builder inputDiag = new AlertDialog.Builder(this);
 		inputDiag.setTitle("Enter Delay");
 		inputDiag.setMessage("Enter Delay (int)\nCurrent delay = "+sp.getInt("delay", 0));
 		final EditText input = new EditText(this);
 		inputDiag.setView(input);
 		inputDiag.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				try
 				{
 					int n = Integer.parseInt(input.getText().toString());
 					if( n < 1000 && n > 0)
 						edit.putInt("delay", n);
 					else
 					{
 						displayAlert("Invalid value\nRequires 0 < Delay < 1000");
 						edit.putInt("delay", 0);
 					}
 				}
 				catch(Exception e)
 				{
 					edit.putInt("delay",0);
 					displayAlert("Invalid value\n Requires 0 < n < 1000");
 				}
 				edit.commit();
 
 			}
 		});
 		inputDiag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 
 			}
 
 		});
 		inputDiag.show();
 	}
 	public boolean onOptionsItemSelected(MenuItem item)
 	/*
 	 * What should be done based on the id of the menu item selected
 	 */
 	{
 		switch(item.getItemId()){
 		case R.id.delay:
 			this.setDelay();
 			return true;
 		case R.id.chserv:
 			this.changeServerAlert();
 			return true;
 		case R.id.chusr:
 			this.changeUsernameAlert();
 			return true;
 		case R.id.logout:
 			this.logout();
 			return true;
 		}
 		return false;
 	}
 	public void changeServerAlert()
 	/*
 	 * Promts user for new server
 	 * 
 	 * TODO Make it a menu
 	 */
 	{
 		AlertDialog.Builder changeServerDiag = new AlertDialog.Builder(this);
 		changeServerDiag.setTitle("Change Server");
 		changeServerDiag.setMessage("Select New Server");
 		final EditText input = new EditText(this);
 		final RadioButton ld = new RadioButton(this); ld.setText("Little Drink"); ld.setId(1);
 		final RadioButton s = new RadioButton(this); s.setText("Snack"); s.setId(2);
 		final RadioButton d = new RadioButton(this); d.setText("Drink"); d.setId(3);
 		final RadioGroup rg = new RadioGroup(this); rg.addView(ld); rg.addView(s); rg.addView(d);
 		changeServerDiag.setView(rg);
 		changeServerDiag.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String newServ;
 				int n = rg.getCheckedRadioButtonId();
 				if(n==1)
 					newServ = "ld";
 				else if (n ==2)
 					newServ = "s";
 				else
 					newServ = "d";
 				edit.putString("serv", newServ);
 				edit.commit();
 				updateButtons();
 			}
 		});
 		changeServerDiag.setNegativeButton("Cancel",new OnClickListener(){
 			public void onClick(DialogInterface dialog,int whichButton){
 				//Don't do anything
 			}
 
 		});
 		changeServerDiag.show();
 	}
 	public void serverDown()
 	/*
 	 * Displays a message if the server is down
 	 */
 	{
 		AlertDialog.Builder serverDownDiag = new AlertDialog.Builder(this);
 		serverDownDiag.setTitle("Server is down");
		serverDownDiag.setMessage("Server is down\nGo bug McGary\n(Or I can't connect to drink");
 		serverDownDiag.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 
 			}
 		});
 		serverDownDiag.show();
 	}
 	public void updateTitle()
 	/*
 	 * Updates the TextView that is at the top of the UI
 	 */
 	{
 		title.setText("Drink App"+"\n Current User: "+sp.getString("user", "null")+"\nCurrent Credits: "+drinkServ.command("GETBALANCE").get(0).split("\\s+")[1]);
 	}
 	public void logout()
 	/*
 	 * Logs the currently logged in user out
 	 */
 	{
 		edit.remove("user");
 		edit.commit();
 		this.displayAlert("User Credentials Wiped\nApp must be restarted to take effect");
 	}
 	public void changeMachine(String machine)
 	/*
 	 * Switches to another server. Current options are ld, d, s
 	 */
 	{
 		this.drinkServ.command("machine "+machine);
 	}
 	public ArrayList<drinkButton> getButtons(Connector drinkServ)
 	/*
 	 * Returns an ArrayList that contains buttons for all of the drinks
 	 */
 	{
 		ArrayList<String> drinks = drinkServ.command("stat");
 		ArrayList<drinkButton> buttons = new ArrayList<drinkButton>();
 		int price,slot,count,place;
 		price = -256;
 		slot = -256;
 		count = -256;
 		place = 1;
 		String name = "";
 		try{
 
 			for(String s : drinks)
 			{
 				String [] temp = s.split("\\s+");
 				place = 1;
 				name = "";
 				while(temp[place].charAt(temp[place].length()-1) != '"')
 				{
 					name+=temp[place]+ " ";
 					place++;
 				}
 				name+=temp[place];
 				place++;
 				slot = Integer.parseInt(temp[0].trim());
 				price = Integer.parseInt(temp[place].trim());
 				count = Integer.parseInt(temp[place+1].trim());
 				buttons.add(new drinkButton(this,name,price,count,slot,drinkServ,this));
 			}
 		}
 		catch(Exception e)
 		{
 			Log.d(e.toString(),e.toString());
 			buttons.add(new drinkButton(this,slot+" ERROR: "+name,price,count,slot,drinkServ,this));
 		}	
 		if(buttons.size() > 0)
 			buttons.remove(buttons.size()-1);
 		return buttons;
 	}
 	public void changeUsernameAlert()
 	/*
 	 * Opens a dialog box and prompts user for new username and new password
 	 */
 	{
 		AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
 		alert2.setTitle("Username");
 		alert2.setMessage("Enter New Username");
 		final EditText input2 = new EditText(this);
 		alert2.setView(input2);
 
 		alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				edit.putString("user", input2.getText().toString());
 				edit.commit();
 				changePasswordAlert();
 			}
 		});
 		alert2.show();
 	}
 	public void changePasswordAlert()
 	/*
 	 * Logs in a user
 	 */
 	{
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Password");
 		alert.setMessage("Enter Password for Account "+sp.getString("user", "null"));
 		final EditText input = new EditText(this);
 		input.setTransformationMethod(new PasswordTransformationMethod());
 		alert.setView(input);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				drinkServ.command("USER " +sp.getString("user", "nulluser"));
 				ArrayList<String>temp = drinkServ.command("PASS "+input.getText().toString());
 				if(temp.get(0).toLowerCase().indexOf("err") == -1)
 				{
 					updateTitle();
 					return;
 				}
 				else
 				{
 					changePasswordAlert();
 				}
 			}
 		});
 		alert.show();
 	}
 }
