 package net.mindsoup.pathfindercharactersheet;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import net.mindsoup.pathfindercharactersheet.adapters.CharacterListAdapter;
 import net.mindsoup.pathfindercharactersheet.fragments.CreateCharacterFragment;
 import net.mindsoup.pathfindercharactersheet.pf.PfAttributes;
 import net.mindsoup.pathfindercharactersheet.pf.PfCharacter;
 import net.mindsoup.pathfindercharactersheet.pf.PfClasses;
 import net.mindsoup.pathfindercharactersheet.pf.PfRaces;
 import net.mindsoup.pathfindercharactersheet.pf.races.PfChooseBonusAttributeRace;
 import net.mindsoup.pathfindercharactersheet.util.DatabaseHelper;
 import android.content.Intent;
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
 
 public class MainActivity extends SherlockFragmentActivity {
 	
 	private CharacterListAdapter adapter;
 	private ArrayList<PfCharacter> allCharacters = new ArrayList<PfCharacter>();
 	private ArrayList<PfCharacter> searchCharacters = new ArrayList<PfCharacter>();
 	private ListView characterList;
 	private DatabaseHelper dbHelper = new DatabaseHelper(this);
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
		
		if(savedInstanceState == null)
			addCharactersFromDb();

		createListAndAdapter();
		
		createContextMenu();
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
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
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
 	
 	public void addNewCharacter(String name, int char_race, int char_class, boolean hpPerLevel, int bonus_stat) {
 		PfCharacter c = new PfCharacter(PfRaces.getRace(PfRaces.getRace(char_race)), PfClasses.getPfClass(PfClasses.getPfClass(char_class)), hpPerLevel, name);
 		
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
