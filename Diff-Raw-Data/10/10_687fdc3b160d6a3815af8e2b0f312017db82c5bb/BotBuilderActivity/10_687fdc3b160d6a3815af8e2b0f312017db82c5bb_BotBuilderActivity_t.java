 package edu.sru.andgate.bitbot.ide.botbuilder;
 
 import java.util.ArrayList;
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.customdialogs.BotBuilderDialog;
 import edu.sru.andgate.bitbot.customdialogs.CustomDialogListView;
 import edu.sru.andgate.bitbot.graphics.NickGameActivity;
 import edu.sru.andgate.bitbot.ide.CodeBuilderActivity;
 import edu.sru.andgate.bitbot.ide.CodeListAdapter;
 import edu.sru.andgate.bitbot.interpreter.SourceCode;
 import edu.sru.andgate.bitbot.missionlist.MissionBriefingActivity;
 import edu.sru.andgate.bitbot.missionlist.MissionListActivity;
 import edu.sru.andgate.bitbot.tools.Constants;
 import edu.sru.andgate.bitbot.tools.FileManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class BotBuilderActivity extends Activity
 {
 	private Bot _currentBot;
 	private BotBuilderDialog bbd;
 	private TextView tv, bot_name;
 	private Constants constant;
 	private ListView bb_turret, bb_chassis,bb_bullet, availableBots;
 	private CustomListView chassis, turret, bullet;
 	private Spinner spinner;
 	private String missionType, mapFile;
 	private String[] code_files, botFiles;
 	ArrayAdapter<String> adapter;
 	private ArrayList<CustomListView> botBaseComponents, botTurretComponents, botBulletComponents;
 	private ArrayList<edu.sru.andgate.bitbot.ide.CustomListView> botLists;
 	private CodeListAdapter botList_adapter;
 	private BotComponentAdapter component_adapter;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ide_botbuilder_main);
 		
 		_currentBot = new Bot();
 		missionType = getIntent().getExtras().getString("GameType");
 		mapFile = getIntent().getExtras().getString("GameMap");
 		
 		FileManager.setContext(getBaseContext());
 		
 		bot_name = (TextView) findViewById(R.id.bot_name);
 		botFiles = FileManager.getBotFiles();
 		botLists = new ArrayList<edu.sru.andgate.bitbot.ide.CustomListView>();
 		
 		edu.sru.andgate.bitbot.ide.CustomListView[] clv = new edu.sru.andgate.bitbot.ide.CustomListView[botFiles.length];
 		for(int i = 0; i < botFiles.length; i++){
 			clv[i] = new edu.sru.andgate.bitbot.ide.CustomListView(FileManager.readInternalXML(botFiles[i].toString(), "Bot-Name"), botFiles[i].toString());
 			botLists.add(clv[i]);
 		}
 		
 		availableBots = (ListView)findViewById(R.id.botSelectorList);
 		this.botList_adapter = new CodeListAdapter(this, R.layout.code_row, botLists);
 		availableBots.setAdapter(botList_adapter);
 		
 		availableBots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 					bot_name.setText(FileManager.readInternalXML(botFiles[arg2].toString(), "Bot-Name"));
 					//set other attributes here including stats & Bot Components
 			}
 			
 		});
 
 		
 		
 		availableBots.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				bbd = new BotBuilderDialog(BotBuilderActivity.this, botFiles[arg2].toString(), BotBuilderActivity.this, R.style.CustomDialogTheme);
 				bbd.show();
 				return true;
 			}
 			
 		});
 		
 		botBaseComponents = new ArrayList<CustomListView>();
 		botTurretComponents = new ArrayList<CustomListView>();
 		botBulletComponents = new ArrayList<CustomListView>();
 		
 		chassis = new CustomListView();
 		chassis.setBotComponentName("Basic Chassis");
 		chassis.setBotComponentDescription("A stable base that is fast and sturdy");
 		chassis.setImageIcon(R.drawable.adambot);
 		
 		turret = new CustomListView();
 		turret.setBotComponentName("Basic Turret");
 		turret.setBotComponentDescription("A turret for shooting stuff");
 		turret.setImageIcon(R.drawable.adamturret);
 		
 		bullet = new CustomListView();
 		bullet.setBotComponentName("Basic Bullet");
 		bullet.setBotComponentDescription("A standard bullet with moderate damage");
 		bullet.setImageIcon(R.drawable.bulletnew);
 		
 		botBaseComponents.add(chassis);
 		botTurretComponents.add(turret);
 		botBulletComponents.add(bullet);	       
 	        
 		this.component_adapter = new BotComponentAdapter(this, R.layout.ide_botbuilder_botcomponent, botBaseComponents);
 		bb_chassis = (ListView) findViewById(R.id.bb_chassis);
 		bb_chassis.setAdapter(this.component_adapter);
 		
 		bb_chassis.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				Toast.makeText(BotBuilderActivity.this, "No other chassis available", Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		this.component_adapter = new BotComponentAdapter(this, R.layout.ide_botbuilder_botcomponent, botTurretComponents);
 		bb_turret = (ListView) findViewById(R.id.bb_turret);
 		bb_turret.setAdapter(this.component_adapter);
 		
 		bb_turret.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				Toast.makeText(BotBuilderActivity.this, "No other turrets available", Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		this.component_adapter = new BotComponentAdapter(this, R.layout.ide_botbuilder_botcomponent, botBulletComponents);
 		bb_bullet = (ListView) findViewById(R.id.bb_bullet);
 		bb_bullet.setAdapter(this.component_adapter);
 		
 		bb_bullet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				Toast.makeText(BotBuilderActivity.this, "No other bullets available", Toast.LENGTH_SHORT).show();
 			}
 		});
 				
 		code_files = FileManager.getFileNamesInDir(getDir("Code",Context.MODE_PRIVATE).getPath());
 		
 		spinner = (Spinner) findViewById(R.id.program_title);
 		adapter = new ArrayAdapter<String>(this, R.layout.spinner_line, code_files);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);  
 		
 		tv = (TextView) findViewById(R.id.program_text);
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) 
 			{
				try{
					tv.setText(FileManager.readTextFileFromDirectory("Code",((String)((TextView) view).getText().toString())));
				}catch(Exception e){
					
				}
 			}
 			
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 				// TODO Auto-generated method stub
 			}
 		});
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.botbuilder_menu, menu);
 	    return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.saveas:
 				promptSaveBot();
 				promptBotName();
 				break;
 				
 		}
 		return true;
 	}
 	
 	/**
 	 * Sets the code associated with the bot
 	 * @param sc a SourceCode object.
 	 */
 	public void setProgram(SourceCode sc)
 	{
 		if (_currentBot != null )
 			_currentBot.attachSourceCode(sc);
 	}
 	
 	/**
 	 * Start the mission
 	 * @param v the view that called this.
 	 */
 	public void begin(View v)
 	{
 		//Save bot to xml before going to graphics - Possible load from xml inside graphics
 		saveBot("test_bot.xml", "BotBuilder Bot");
 				
 		// Start up the game engine
 		Intent engineIntent = new Intent(BotBuilderActivity.this, NickGameActivity.class);
 		engineIntent.putExtra("Bot", "test_bot.xml");
 		engineIntent.putExtra("GameType", missionType);
 		engineIntent.putExtra("MapFile", mapFile);
 		startActivity(engineIntent);
 		
 	}
 	public void promptBotName(){
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Set Bot Name...");
 		alert.setMessage("Bot Name:");
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			  String value = input.getText().toString();
 			  if(value != null){
 				  _currentBot.setName(value);
 			  }
 		}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 		
 		alert.show();
 	}	
 	public void promptSaveBot(){
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Save Bot As...");
 		alert.setMessage("Bot Filename:");
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			  String value = input.getText().toString();
 			  if(value != null){
 				  saveBot(value, _currentBot.getName());
 				  Intent intent = getIntent();
 				  finish();
 				  startActivity(intent);
 			  }
 		}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 		
 		alert.show();
 	}	
 	
 	public void saveBot(String filename, String BotName){
 		//Save bot to xml before going to graphics - Possible load from xml inside graphics
 		_currentBot.setName(BotName);
 		constant = new Constants();
 		_currentBot.setBase(chassis.getImageIcon());
 		_currentBot.setTurret(turret.getImageIcon()); 
 		_currentBot.setBullet(bullet.getImageIcon());
 		_currentBot.setCode(tv.getText().toString());
 		Log.v("BitBot", _currentBot.getCode().getCode());
 		_currentBot.saveBotToXML(this.getBaseContext(), filename);
 	}
 	
 }
