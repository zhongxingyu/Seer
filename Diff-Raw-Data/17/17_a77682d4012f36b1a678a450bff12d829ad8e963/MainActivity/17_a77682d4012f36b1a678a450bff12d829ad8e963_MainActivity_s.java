 package mobserv.smsgaming;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	ListView lvListe;
 
 	ArrayList<Group> groups = new ArrayList<Group>();
 	ArrayList<Player> players = new ArrayList<Player>();
 	ArrayList<Challenge> challenges = new ArrayList<Challenge>();
	Boolean force_clean = false;
 	
 	String separator = "_;_";
 	@Override
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		System.out.println("in oncreate");
 
 
 		lvListe = (ListView)findViewById(R.id.listView1);
 		
 
 		//tests de cesar
 		Set<String> voidset = new HashSet<String>();
 		voidset.add("null");
 
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = preferences.edit();
 		
 
 		Set<String> set_groups_prov = preferences.getStringSet("groups", voidset);
 		if (set_groups_prov == voidset || force_clean){
 			editor.clear();
 			
 			Date date = new Date();
 			editor.putString("date", date.toString());
 			
 			Set<String> test = new HashSet<String>();
 			test.add("group1");
 			test.add("group2");
 			test.add("group3");
 			editor.putStringSet("groups", test);
 
 			Set<String> group1P = new HashSet<String>();
 			group1P.add("John"+separator+"0672838272"+separator+"true"+separator+"3");
 			group1P.add("Bob"+separator+"0630000000"+separator+"false"+separator+"4");
 			group1P.add("Frank"+separator+"0649857984"+separator+"false"+separator+"56");
 
 			editor.putStringSet("group1"+separator+"P", group1P);
 
 			
 			Set<String> group1C = new HashSet<String>();
 			group1C.add("peperroni"+separator+"30"+separator+false);
 			group1C.add("pizza"+separator+"50"+separator+false);
 			editor.putStringSet("group1"+separator+"C", group1C);
 
 			
 			Set<String> group2P = new HashSet<String>();
 			group2P.add("John"+separator+"0672838272"+separator+"true"+separator+"12");
 			group2P.add("Alice"+separator+"0632340000"+separator+"false"+separator+"9");
 			editor.putStringSet("group2"+separator+"P", group2P);
 
 			Set<String> group2C = new HashSet<String>();
 			group2C.add("Madonna"+separator+"40"+separator+false);
 			group2C.add("OMG"+separator+"10"+separator+false);
 			editor.putStringSet("group2"+separator+"C", group2C);
 			
 			Set<String> group3P = new HashSet<String>();
 			group3P.add("John"+separator+"0672838272"+separator+"true"+separator+"8");
 			group3P.add("Alice"+separator+"0632340000"+separator+"false"+separator+"10");
 			editor.putStringSet("group3"+separator+"P", group3P);
 
 			Set<String> group3C = new HashSet<String>();
 			group3C.add("Banana"+separator+"40"+separator+false);
 			group3C.add("Tomatoe"+separator+"10"+separator+false);
 			editor.putStringSet("group3"+separator+"C", group3C);
 
 			editor.putString("group3"+separator+"B", "Winner wins !");
 
 			editor.putString("group1"+separator+"B", "Last one pays pizza for everyone");
 			editor.putString("group2"+separator+"B", "Winner gets ticket to concert");
 
 			editor.commit();
 		}
 
 		
 
 		this.readData();	
 		this.printData();
 		//getUser().challengeCompleted(getGroup("group1"),challenges.get(0));
 		
 		//fin tests c���sar
 
 		SMSParser parser = new SMSParser(getUser(), groups);
 		SMSReceiver.setParserChall(parser, challenges, this);
 		for (Challenge chall : challenges){//Look if any challenge was completed
 		    parser.searchSMS(chall, this);
 		}
 		
 		GroupAdapter adapter = new GroupAdapter(this, groups);
 
 		lvListe.setAdapter(adapter);
 
 		lvListe.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				// selected item
 				TextView group_view = (TextView) ((LinearLayout) view).getChildAt(0);
 				// Launching new Activity on selecting single List Item
 				Intent i = new Intent(getApplicationContext(), GroupItem.class);
 				// sending data to new activity
 				i.putExtra("name",group_view.getText().toString());
 
 				/*
 	              ArrayList<String> playersinfos = new ArrayList<String>();
 	              playersinfos.add("Bob"+separator+"14");
 	              playersinfos.add("Jenny"+separator+"63");
 
 
 	              i.putExtra("players", playersinfos);
 				 */
 				startActivity(i);
 			}
 		});
 
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	protected void onResume() {
 		super.onResume();
 		System.out.println("in onResume");
 		this.readData();	
 		this.printData();
 		
 		GroupAdapter adapter = new GroupAdapter(this, groups);
 
 		lvListe.setAdapter(adapter);
 
 		lvListe.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				// selected item
 				TextView group_view = (TextView) ((LinearLayout) view).getChildAt(0);
 				// Launching new Activity on selecting single List Item
 				Intent i = new Intent(getApplicationContext(), GroupItem.class);
 				// sending data to new activity
 				i.putExtra("name",group_view.getText().toString());
 
 				/*
 	              ArrayList<String> playersinfos = new ArrayList<String>();
 	              playersinfos.add("Bob"+separator+"14");
 	              playersinfos.add("Jenny"+separator+"63");
 
 
 	              i.putExtra("players", playersinfos);
 				 */
 				startActivity(i);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	/** Called when the user clicks the joinGroup button */
 	public void joinGroup(View view) {
 	    Intent intent = new Intent(this, JoinGroupActivity.class);
 	    startActivity(intent);
 	}
 
 	/**
 	 * This method reads all the stored data of the game,
 	 * and puts it in variables.
 	 */
 	public void readData() {
 		int i,j;
 
 		challenges.clear();
 		players.clear();
 		groups.clear();
 
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 
 		Set<String> voidset = new HashSet<String>();
 		voidset.add("null");
 		// gets all groups
 		Set<String> set_groups_prov = preferences.getStringSet("groups", voidset);
 		// if not void, then loop on the groups to get their data
 		if (set_groups_prov!=voidset){
 			for (i=0;i<set_groups_prov.size();i++) {
 				String g_name_prov = set_groups_prov.toArray()[i].toString();
 				if (getGroup(g_name_prov)==null){
 					Group group_prov = new Group(g_name_prov);
 					//System.out.println("new group :"+g_name_prov);	
 
 
 					Set<String> set_p_prov = preferences.getStringSet(g_name_prov+separator+"P", voidset);
 					Set<String> set_c_prov = preferences.getStringSet(g_name_prov+separator+"C", voidset);
 					String bet_prov = preferences.getString(g_name_prov+separator+"B","");
 					//System.out.println(set_p_prov);
 					//System.out.println(set_c_prov);
 					//System.out.println(bet_prov);
 					if (set_p_prov!=voidset) {
 						for (j=0;j<set_p_prov.size();j++) {
 
 							String[] p_prov = set_p_prov.toArray()[j].toString().split(separator);
 							String p_name_prov = p_prov[0];
 							String p_number_prov = p_prov[1];
 							boolean p_isuser_prov = Boolean.parseBoolean(p_prov[2]);
 							int p_score_prov = Integer.parseInt(p_prov[3]);
 
 							//System.out.println("new player :"+p_name_prov+" with n*:"+p_number_prov);
 
 							if (getPlayer(p_name_prov,p_number_prov)==null) {
 								Player player_prov = new Player(this,p_name_prov,p_number_prov,p_isuser_prov);
 								player_prov.setScore(g_name_prov, p_score_prov);
 								this.players.add(player_prov);
 								group_prov.addPlayer(player_prov);
 							}
 							else {
 								Player player_prov;
 								player_prov = getPlayer(p_name_prov,p_number_prov);
 								player_prov.setScore(g_name_prov, p_score_prov);
 								group_prov.addPlayer(player_prov);
 							}
 						}
 					}
 
 					if (set_c_prov!=voidset) {
 						for (j=0;j<set_c_prov.size();j++) {
 							String c_name_prov = set_c_prov.toArray()[j].toString().split(separator)[0];
 							String c_points_prov = set_c_prov.toArray()[j].toString().split(separator)[1];
 							boolean c_completed_prov = Boolean.parseBoolean(set_c_prov.toArray()[j].toString().split(separator)[2]);
 							if (getChallenge(c_name_prov,g_name_prov)==null) {
 								//System.out.println("new c:"+c_name_prov+" value:"+c_points_prov+" done?:"+c_completed_prov);
 								Challenge c_prov = new Challenge(this, c_name_prov,Integer.parseInt(c_points_prov), g_name_prov,c_completed_prov);
 								this.challenges.add(c_prov);
 								group_prov.addChallenge(c_prov);
 							}
 						}
 					}	
 					
 					if (!bet_prov.equals("")) {
 						group_prov.setBet(bet_prov);
 					}
 					groups.add(group_prov);
 				}
 			}
 		}
 	}
 
 
 	public void AddGroup(String groupname){
 		Set<String> voidset = new HashSet<String>();
 		voidset.add("null");
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.clear();
 		Set<String> set_groups_prov = preferences.getStringSet("groups", voidset);
 		set_groups_prov.add(groupname);
 		editor.putStringSet("groups", set_groups_prov);
 		
 		Player user = getUser();
 		Set<String> groupP = new HashSet<String>();
 		groupP.add(user.getName()+separator+user.getPhone_number()+separator+"true"+separator+"0");
 		editor.putStringSet(groupname+separator+"P", groupP);
 
 		Set<String> groupC = new HashSet<String>();
 		groupC.add("Banana"+separator+"30"+separator+false);
 		groupC.add("Tomatoe"+separator+"20"+separator+false);
 		editor.putStringSet(groupname+separator+"C", groupC);
 
 		editor.putString(groupname+separator+"B", "Winner gets free cinema ticket");
 		
 		editor.commit();
 
 		this.readData();	
 
 
 
 	}
 	public Player getPlayer(String name, String number) {
 		Player ret = null;
 		for (Player player : this.players) {
 			if ((player.getName().equals(name))&&(player.getPhone_number().equals(number))) {
 				ret = player;
 			}
 		}
 		return ret;
 	}
 
 	public Group getGroup(String groupname) {
 		Group ret = null;
 		for (Group group : this.groups) {
 			if (group.getName().equals(groupname)) {
 				ret = group;
 			}
 		}
 		return ret;
 	}
 
 	public Challenge getChallenge(String objective,String groupname){
 		Challenge ret = null;
 		for (Challenge challenge: this.challenges) {
 			if ((challenge.getObjective().equals(objective))&&(challenge.getGroupname().equals(groupname))) {
 				ret = challenge;
 			}
 		}
 		return ret;
 	}
 
 	public ArrayList<Group> getGroups() {
 		return groups;
 	}
 
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 
 	public ArrayList<Challenge> getChallenges() {
 		return challenges;
 	}
 
 	public Player getUser(){
 		Player ret = null;
 		for (Player player : players) {
 			if (player.isUser()) ret = player;
 		}
 		return ret;
 	}
 	public void printData() {
 		System.out.println("nb groups:"+groups.size());
 		for(Group group : groups) {
 			Log.d("printData",group.toString());
 		}
 		System.out.println("nb players:"+players.size());
 		for (Player player : players) {
 			Log.d("printData",player.toString());
 		}
 		System.out.println("nb challenges:"+challenges.size());
 		for (Challenge challenge: challenges) {
 			Log.d("printData",challenge.toString());
 		}
 	}
 }
