 package net.mindsoup.charactersoup;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
 import com.crashlytics.android.Crashlytics;
 
 import net.mindsoup.charactersoup.adapters.CharacterListAdapter;
 import net.mindsoup.charactersoup.fragments.CreateCharacterFragment;
 import net.mindsoup.charactersoup.pf.PfAttributes;
 import net.mindsoup.charactersoup.pf.PfCharacter;
 import net.mindsoup.charactersoup.pf.PfClasses;
 import net.mindsoup.charactersoup.pf.PfRaces;
 import net.mindsoup.charactersoup.pf.races.PfChooseBonusAttributeRace;
 import net.mindsoup.charactersoup.util.CharacterSoupUtils;
 import net.mindsoup.charactersoup.util.DatabaseHelper;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import io.fabric.sdk.android.Fabric;
 
 public class MainActivity extends SherlockFragmentActivity {
 	
 	private CharacterListAdapter adapter;
 	private ArrayList<PfCharacter> allCharacters = new ArrayList<PfCharacter>();
 	private ArrayList<PfCharacter> searchCharacters = new ArrayList<PfCharacter>();
 	private ListView characterList;
 	private DatabaseHelper dbHelper = new DatabaseHelper(this);
 
     private final String settingsFile = "CharacterSoupSettings";
     private final String previousVersion = "PreviousVersion";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Fabric.with(this, new Crashlytics());
 
 		setContentView(R.layout.activity_main);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 	    super.onCreateContextMenu(menu, v, menuInfo);
 	    android.view.MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.context, menu);
 	    menu.setHeaderTitle("Character options");
 	}
 	
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 	    
 	    switch (item.getItemId()) {
 	        case R.id.delete_button:
 	        	deleteCharacter(searchCharacters.get(info.position));
 	            return true;
 	        case R.id.open_button:
 	        	openCharacter(searchCharacters.get(info.position));
 	            return true;
 	        default:
 	            return super.onContextItemSelected(item);
 	    }
 	}
 	
 	private void createContextMenu() {
 		registerForContextMenu(characterList);
 	}
 	
 	private void createListAndAdapter() {
 		characterList = (ListView)findViewById(R.id.character_list);
 		adapter = new CharacterListAdapter(this, R.layout.character_list_item, searchCharacters);
 		characterList.setAdapter(adapter);
 		characterList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				openCharacter(searchCharacters.get(position));
 			}
 		});
 	}
 	
 	private void addCharactersFromDb() {
 		allCharacters.clear();
 		searchCharacters.clear();
 		allCharacters.addAll(dbHelper.getCharacters());
 		searchCharacters.addAll(allCharacters);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	   MenuInflater inflater = getSupportMenuInflater();
 	   inflater.inflate(R.menu.main, (com.actionbarsherlock.view.Menu) menu);
 	   
 	   makeSearchListener(menu);
 	   
 	   return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 	        case R.id.new_button:
 	        	newCharacterButtonClicked();
 	        	return true;
 	        case R.id.ogl_button:
 	        	Intent intent = new Intent(this, OglActivity.class);
 	        	startActivity(intent);
 	        	return true;
             case R.id.update_button:
                 showUpdateFragment();
                 return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		
 		addCharactersFromDb();
 
 		createListAndAdapter();
 		
 		createContextMenu();
 
         SharedPreferences settings = getSharedPreferences(settingsFile, 0);
         String previous = settings.getString(previousVersion, "");
 
         String versionName = "";
         try {
             versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
         } catch (PackageManager.NameNotFoundException e) {}
 
         if(!versionName.equalsIgnoreCase(previous)) {
             showUpdateFragment();
         }
 
         SharedPreferences.Editor editor = settings.edit();
         editor.putString(previousVersion, versionName);
         editor.commit();
 	}
 	
 	private void openCharacter(PfCharacter character) {	
 		Intent intent = new Intent(this, CharacterActivity.class);
 		intent.putExtra("CHAR", character);
     	startActivity(intent);
 	}
 	
 	public void newCharacterButtonClicked() {
 		showCreateCharacterDialog();
 	}
 	
 	private void showCreateCharacterDialog() {
         FragmentManager fm = this.getSupportFragmentManager();
         CreateCharacterFragment createChar = new CreateCharacterFragment();
         createChar.show(fm, "fragment_create_char");
     }
 
     private void showUpdateFragment() {
         final String message = "<p>New in this version:</p><p>• Improved feat selection view<br>• Added barbarian rage powers<br>• Added spells for caster classes<br>• Added help functionality on all screens</p><p>The following items are on my roadmap, although I cannot guarantee when they will be done:<br><br>• Character backup and export options<br>• Complete class and racial feats, bonuses and talents<br>• Special Abilities for all classes<br>• Spells for caster classes<br>• Rage powers for Barbarians<br>• Improved item managment<br>• Improved character overview<br>• Proper tablet support<br><br>Please keep in mind that this app is still in development and is not finished yet. There may be bugs and missing features. If you have feedback you can contact me at mindsouplabs@gmail.com.</p><p>Thank you for using Character Soup! :)</p>";
         String versionName = "";
         try {
             versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
         } catch (PackageManager.NameNotFoundException e) {}
 
         CharacterSoupUtils.showTextDialog(this, message, "Update " + versionName);
     }
 	
 	public void addNewCharacter(String name, int char_race, int char_class, boolean hpPerLevel, int bonus_stat) {
 		PfCharacter c = new PfCharacter(PfRaces.getRace(PfRaces.getRace(char_race)), PfClasses.getPfClass(PfClasses.getPfClass(char_class)), name);
 		
 		int hitpoints = 0;
 		
 		if(hpPerLevel)
 			hitpoints++;
 		
 		hitpoints += c.getPfClass().getHitDie().getMax();
 		
 		c.setHitpoints(hitpoints);
         c.setAvailableSpecialPowers(c.getLevelupSpecialPowers(1));
 		
		if(c.getRace().getRace() == PfRaces.HALFELF || c.getRace().getRace() == PfRaces.HALFORC || c.getRace().getRace() == PfRaces.HUMAN) 
			((PfChooseBonusAttributeRace)c.getRace()).setBonusAttribute(PfAttributes.getAttribute(bonus_stat));
 		
 		long id = dbHelper.addCharacter(c);
 		c.setId(id);
 		allCharacters.add(c);
 		searchCharacters.add(c);
 		adapter.notifyDataSetChanged();	// TODO: async task
 		openCharacter(c);		 
 	}
 	
 	@Override 
     public void onSaveInstanceState(Bundle savedState) {
     	super.onSaveInstanceState(savedState);
     	
     	savedState.putParcelableArrayList("characters", allCharacters);
     	
     }
     
     @Override
     public void onRestoreInstanceState(Bundle state) {
     	super.onRestoreInstanceState(state);
        	
     	allCharacters = state.getParcelableArrayList("characters");
     	searchCharacters.clear();
     	searchCharacters.addAll(allCharacters);
     	adapter = new CharacterListAdapter(this, R.layout.character_list_item, searchCharacters);
 		characterList.setAdapter(adapter);
     }
     
     private void makeSearchListener(Menu menu) {
     	SearchView search = (SearchView)menu.findItem(R.id.menu_search).getActionView();
     	search.setOnQueryTextListener(new OnQueryTextListener() {
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				return false;
 			}
 
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				searchCharactersFor(newText);
 				return true;
 			}
 		});
     }
     
     private void searchCharactersFor(String query) {
     	if(query.trim().length() == 0) {
     		searchCharacters.clear();
     		searchCharacters.addAll(allCharacters);
     	} else {
     		searchCharacters.clear();
     		query = query.toLowerCase(Locale.getDefault());
 	    	for(PfCharacter c : this.allCharacters) {
 	    		if(c.getName().toLowerCase(Locale.getDefault()).startsWith(query) || c.getPfClass().toString().toLowerCase(Locale.getDefault()).startsWith(query) || c.getRace().toString().toLowerCase(Locale.getDefault()).startsWith(query)) {
 	    			searchCharacters.add(c);
 	    		}
 	    	}
     	}
     	
     	adapter.notifyDataSetChanged();	
     }
     
     private void deleteCharacter(PfCharacter character) {
     	dbHelper.deleteCharacter(character);
     	allCharacters.remove(character);
     	searchCharacters.remove(character);
     	
     	adapter.notifyDataSetChanged();
     }
 }
