 package com.roman.ppaper.UI;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.roman.ppaper.R;
 import com.roman.ppaper.helpers.GameCharacter;
 import com.roman.ppaper.helpers.xmlsql.DataSet;
 import com.roman.ppaper.helpers.xmlsql.EntityView;
 
 public class CharaktereFragment extends Fragment 
 {
 	//Variablen festlegen fr die Charakterdaten
     public ArrayList<Integer> avatarIDs      = new ArrayList<Integer>();
     public ArrayList<String>  characterNames = new ArrayList<String> ();
     public ArrayList<Integer> characterIds   = new ArrayList<Integer>();
 
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         Log.d(this.toString(), "Character Fragment loaded");
     }
 
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
     {
         Context _context = inflater.getContext(); 
 
         // if(Debug.isDebuggerConnected())
         //_context.deleteDatabase("character");      
 
         View chara = inflater.inflate(R.layout.activity_character_overview, container, false);
         
         createButtonContent(chara);//Button wird aufgerufen
         createListContent(_context, chara);//Charakterliste wird aufgerufen
 
         return chara;
     }
 
     //Erstellen des Buttons fuer die Charaktererstellung
     public void createButtonContent(View chara) 
     {
         Button char_creation = (Button)chara.findViewById(R.id.buttonchar_creation);
         Typeface font        = Typeface.createFromAsset(getActivity().getAssets(), "balthazar.TTF");
         char_creation.setTypeface(font);
         
         char_creation.setOnClickListener(new View.OnClickListener() 
         {
         	   //Beim Klick auf den Button wird das Fragment geschlossen und von dem Charaktereditor ersetzt. Dabei wird als id "-1" uebergeben, was fr einen neuen Charakter steht
                public void onClick(View chara) 
                {
                    Fragment newFragment = new CharakterEditorFragment1();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.char_overview, newFragment);
                    transaction.addToBackStack(null);
                    Bundle args = new Bundle();
                    args.putInt("gc_id",-1);
                    newFragment.setArguments(args);
                    transaction.commit();
                }
               
          });
     }
 
     //Erstellen der Charakterliste
     public void createListContent(Context _context, View chara) 
     {	
     	//zum Testen erstellen wir hier zunaechst einen neuen Charakter. Natuerlich wird er so jedes Mal, wenn dieses Fragment neu geladen wird, neu erstellt
     	GameCharacter gcE1 = new GameCharacter(_context, -1);
     	
     	gcE1.set("avatar", "4");
     	gcE1.set("player_name", "Andreas Grabski");
     	gcE1.set("name", "Eugene Francois Vidocq");
     	gcE1.set("job", "Privatdetektiv");
     	gcE1.set("title", "Professor");
     	gcE1.set("birth_location", "Arras, Frankreich");
     	gcE1.set("sex", "Mnnlich");
     	gcE1.set("age", "37");
     	gcE1.set("insanities", "Keine");
     	gcE1.set("strength", "18");
     	gcE1.set("constitution", "18");
     	gcE1.set("height", "18");
     	gcE1.set("dexterity", "18");
     	gcE1.set("appearance", "15");
     	gcE1.set("mental_stability", "79");
     	gcE1.set("intelligence", "25");
     	gcE1.set("mana", "25");
     	gcE1.set("education", "25");
     	gcE1.set("idea", "125");
     	gcE1.set("luck", "125");
     	gcE1.set("knowledge", "125");
     	gcE1.set("max_stability", "79");
     	gcE1.set("damage_bonus", "+1W6");
     	gcE1.set("sanity", "79");
     	gcE1.set("magic", "25");
     	gcE1.set("hit_points", "18");
     	gcE1.set("Ansehen", "50");
     	gcE1.set("Anthropologie", "1");
     	gcE1.set("Archaeologie", "70");
     	gcE1.set("Astronomie", "60");
     	gcE1.set("Ausweichen", "90");
     	gcE1.set("Bibliotheksnutzung", "50");
     	gcE1.set("Internetnutzung", "30");
     	gcE1.set("Biologie", "50");
     	gcE1.set("Buchfuehrung", "70");
     	gcE1.set("Chemie", "50");
     	gcE1.set("Computernutzung", "30");
     	gcE1.set("Cthulhu_Mythos", "20");
     	gcE1.set("Elektrische_Reparaturen", "50");
     	gcE1.set("Elektronik", "35");
     	gcE1.set("Erste_Hilfe", "70");
     	gcE1.set("Fallschirmspringen", "20");
     	gcE1.set("Fahren", "50");
     	gcE1.set("Feilschen", "70");
     	gcE1.set("Fotografie", "40");
     	gcE1.set("Fremdsprache", "01");
     	gcE1.set("Geologie", "50");
     	gcE1.set("Geschichtskenntnisse", "50");
     	gcE1.set("Gesetzeskenntnisse", "50");
     	gcE1.set("Handwerk", "50");
     	gcE1.set("Horchen", "75");
     	gcE1.set("Kampfsportart", "90");
     	gcE1.set("Klettern", "96");
     	gcE1.set("Kunst", "25");
     	gcE1.set("Mechanische_Reparaturen", "50");
     	gcE1.set("Medizin", "60");
     	gcE1.set("Muttersprache", "99");
     	gcE1.set("M_Slot", "99");
     	gcE1.set("Naturkunde", "50");
     	gcE1.set("Navigation", "30");
     	gcE1.set("Okkultismus", "85");
     	gcE1.set("Orientierung", "60");
     	gcE1.set("Pharmazie", "50");
     	gcE1.set("Physik", "50");
     	gcE1.set("Pilot", "01");
     	gcE1.set("Psychoanalyse", "60");
     	gcE1.set("Psychologie", "75");
     	gcE1.set("Reiten", "30");
     	gcE1.set("Schleichen", "70");
     	gcE1.set("Schlosserarbeiten", "70");
     	gcE1.set("Schweres_Geraet", "20");
     	gcE1.set("Schwimmen", "30");
     	gcE1.set("Sprengen", "40");
     	gcE1.set("Springen", "30");
     	gcE1.set("Spurensuche", "75");
     	gcE1.set("Tarnen", "90");
     	gcE1.set("Ueberreden", "75");
     	gcE1.set("Ueberzeugen", "75");
     	gcE1.set("Verbergen", "90");
     	gcE1.set("Verborgenes_erkennen", "90");
     	gcE1.set("Verkleiden", "50");
     	gcE1.set("Werfen", "50");
     	gcE1.set("Artillerie", "30");
     	gcE1.set("Bogen", "30");
     	gcE1.set("Faustfeuerwaffe", "99");
     	gcE1.set("Gewehr", "99");
     	gcE1.set("Granatgewehr", "15");
     	gcE1.set("Maschinengewehr", "99");
     	gcE1.set("Maschinenpistole", "99");
     	gcE1.set("Schrotflinte", "99");
     	gcE1.set("Schwere_Waffen", "15");
     	gcE1.set("home_location", "Chicago");
     	gcE1.set("description", "2m gro, muskuls und schlank, Glatze, graue Augen, leichte Brunung, Narben");
     	gcE1.set("family_friends", "Keine");
     	gcE1.set("temporary_madness", "Keine");
     	gcE1.set("scars", "Narben am ganzen Krper");
     	gcE1.set("history", "Ein Weisenkind aus Frankreich, welches nach Chicago kam und vom Ordensvater Ambrosin aufgenommen wurde. Er erlernte die dunklen Knste und macht sich nun auf die Jagd nach Wesen aus anderen Welten.");
     	gcE1.set("income", "2500$");
     	gcE1.set("cash", "1250$");
     	gcE1.set("savings", "1250$");
     	gcE1.set("possession", "Bro, Wohnung");
     	gcE1.set("property", "Lndereien um Arkham herum");
     	int idGCE1 = gcE1.write();
     	EntityView weaponsView1 = gcE1.createEntityView("weapons");
     	EntityView kampfmesser = weaponsView1.createNew("weapon");
     	kampfmesser.set("name", "Kampfmesser");
     	kampfmesser.set("category", "Nahkampf");
     	kampfmesser.set("basic_chance", "99");
     	kampfmesser.set("damage", "1W4+2+Sb");
     	kampfmesser.set("hands", "1");
     	kampfmesser.set("basic_distance", "Berhrung");
     	kampfmesser.set("attacks_per_round", "1");
     	kampfmesser.set("hit_points", "19");
     	EntityView.linkEntityInstances(_context, gcE1, kampfmesser, "weapons");
     	int idKampfmesser = kampfmesser.write();
     	EntityView revolver4511 = weaponsView1.createNew("weapon");
     	revolver4511.set("name", ".45er Revolver");
     	revolver4511.set("category", "Schusswaffe");
     	revolver4511.set("basic_chance", "99");
     	revolver4511.set("damage", "1W10+2");
     	revolver4511.set("fail_function", "00");
     	revolver4511.set("basic_distance", "15");
     	revolver4511.set("attacks_per_round", "1");
     	revolver4511.set("capacity", "6");
     	revolver4511.set("hit_points", "8");
     	int idRevolver11 = weaponsView1.write();
     	EntityView revolver4512 = weaponsView1.createNew("weapon");
     	revolver4512.set("name", ".45er Revolver");
     	revolver4512.set("category", "Schusswaffe");
     	revolver4512.set("basic_chance", "99");
     	revolver4512.set("damage", "1W10+2");
     	revolver4512.set("fail_function", "00");
     	revolver4512.set("basic_distance", "15");
     	revolver4512.set("attacks_per_round", "1");
     	revolver4512.set("capacity", "6");
     	revolver4512.set("hit_points", "8");
     	int idRevolver12 = weaponsView1.write();
     	EntityView assetsView1 = gcE1.createEntityView("assets");
     	EntityView altZeichen = assetsView1.createNew("equipment");
     	altZeichen.set("name", "lteres Zeichen");
     	int idAltZeichen = assetsView1.write();
     	EntityView silbUhr = assetsView1.createNew("equipment");
     	silbUhr.set("name", "Silberne Uhr");
     	int idSilbUhr = assetsView1.write();
     	EntityView lightT = assetsView1.createNew("equipment");
     	lightT.set("name", "Taschenlampe");
     	int idLightT = assetsView1.write();
     	EntityView mythsView1 = gcE1.createEntityView("readen_myths");
     	EntityView necronomicon = mythsView1.createNew("myth");
     	necronomicon.set("name", "Necronomicon");
     	int idNecronomicon = mythsView1.write();
     	EntityView artefactView1 = gcE1.createEntityView("artefacts");
     	EntityView altZeichenObj = artefactView1.createNew("artefact");
     	altZeichenObj.set("name", "lteres Zeichen");
     	int idAltZeichenObj = artefactView1.write();
     	EntityView electroPistol = artefactView1.createNew("artefact");
     	electroPistol.set("name", "Elektropistole");
     	int idElectroPistol = artefactView1.write();
     	EntityView mythCView1 = gcE1.createEntityView("myth_creatures");
     	EntityView byakhee = mythCView1.createNew("myth_creature");
     	byakhee.set("name", "Byakhee");
     	int idByakhee = mythCView1.write();
     	EntityView migo = mythCView1.createNew("myth_creature");
     	migo.set("name", "Mi-Go");
     	int idMigo = mythCView1.write();
     	EntityView ghoul = mythCView1.createNew("myth_creature");
     	ghoul.set("name", "Ghoul");
     	int idGhoul = mythCView1.write();
     	
     	
     	/*GameCharacter gcE2 = new GameCharacter(_context, -1);
     	gcE2.set("avatar", "7");
     	gcE2.set("player_name", "Andreas Grabski");
     	gcE2.set("name", "Dale Cooper");
     	gcE2.set("job", "Privatdetektiv");
     	gcE2.set("title", "Abitur");
     	gcE2.set("birth_location", "Mystic");
     	gcE2.set("sex", "Mnnlich");
     	gcE2.set("age", "24");
     	gcE2.set("insanities", "Keine");
     	gcE2.set("strength", "13");
     	gcE2.set("constitution", "15");
     	gcE2.set("height", "13");
     	gcE2.set("dexterity", "12");
     	gcE2.set("appearance", "10");
     	gcE2.set("mental_stability", "50");
     	gcE2.set("intelligence", "17");
     	gcE2.set("mana", "10");
     	gcE2.set("education", "13");
     	gcE2.set("idea", "85");
     	gcE2.set("luck", "50");
     	gcE2.set("knowledge", "65");
     	gcE2.set("max_stability", "99");
     	gcE2.set("damage_bonus", "+1W4");
     	gcE2.set("sanity", "50");
     	gcE2.set("magic", "10");
     	gcE2.set("hit_points", "14");
     	gcE2.set("Ausweichen", "34");
     	gcE2.set("Erste_Hilfe", "50");
     	gcE2.set("Gesetzeskenntnisse", "25");
     	gcE2.set("Horchen", "50");
     	gcE2.set("Klettern", "25");
     	gcE2.set("Medizin", "20");
     	gcE2.set("Muttersprache", "65");
     	gcE2.set("M_Slot", "65");
     	gcE2.set("Okkultismus", "27");
     	gcE2.set("Pharmazie", "16");
     	gcE2.set("Psychologie", "50");
     	gcE2.set("Schleichen", "30");
     	gcE2.set("Schlosserarbeiten", "31");
     	gcE2.set("Spurensuche", "50");
     	gcE2.set("Ueberreden", "35");
     	gcE2.set("Ueberzeugen", "42");
     	gcE2.set("Verborgenes_erkennen", "72");
     	gcE2.set("Faustfeuerwaffe", "80");
     	gcE2.set("home_location", "Chicago");
     	gcE2.set("family_friends", "Mutter lebt, Vater ermordet");
     	gcE2.set("temporary_madness", "Keine");
     	gcE2.set("injuries", "Keine");
     	gcE2.set("scars", "Keine");
     	gcE2.set("income", "4500$");
     	gcE2.set("cash", "500$");
     	gcE2.set("savings", "4000$");
     	gcE2.set("possession", "Wohnung");
     	int idGCE2 = gcE2.write();
     	EntityView weaponsView2 = gcE2.createEntityView("weapons");
     	EntityView revolver4521 = weaponsView2.createNew("weapon");
     	revolver4521.set("name", "");
     	revolver4521.set("category", "");
     	revolver4521.set("basic_chance", "");
     	revolver4521.set("damage", "");
     	revolver4521.set("fail_function", "");
     	revolver4521.set("basic_distance", "");
     	revolver4521.set("attacks_per_round", "");
     	revolver4521.set("capacity", "");
     	revolver4521.set("hit_points", "");
     	int idRevolver21 = weaponsView2.write();
     	EntityView revolver4522 = weaponsView2.createNew("weapon");
     	revolver4522.set("name", "");
     	revolver4522.set("category", "");
     	revolver4522.set("basic_chance", "");
     	revolver4522.set("damage", "");
     	revolver4522.set("fail_function", "");
     	revolver4522.set("basic_distance", "");
     	revolver4522.set("attacks_per_round", "");
     	revolver4522.set("capacity", "");
     	revolver4522.set("hit_points", "");
     	int idRevolver22 = weaponsView2.write();*/
 
     	//Hier werden die DataSets aller sich in der Datenbank befindenden Charaktere geholt und anschliessend in die ArrayLists verteilt
         ArrayList<DataSet> characters     = GameCharacter.getCharacters(_context);
 
         for (DataSet characterSet : characters) 
         {
             Integer avatarId      = characterSet.getAsInteger("avatar");
             String  characterName = characterSet.getAsString ("name");
             int     characterId   = characterSet.getAsInteger("character_id");
             avatarIDs     .add(avatarId     );
             characterNames.add(characterName);
             characterIds  .add(characterId  );
         }
         
         //Das custom Layout wird mittels eines Adapters auf die ListView gesetzt
         final ArrayAdapter<String> adapter = new CharListAdapter(getActivity(), R.layout.char_list_row_layout, characterNames); 
         
         ListView listView = (ListView)chara.findViewById(R.id.list);
         listView.setAdapter(adapter);
         
         //Beim einfachen Klick auf den Charakter gelangt man zu dessen Charakteransicht. Dabei wird seine ID mit uebergeben, damit das CharAnzeigeFragment weiss, welchen Charakter es laden soll
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
         {
         	@Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
             {
         		Fragment            newFragment = new CharAnzeigeFragment();
                 FragmentTransaction transaction = getFragmentManager().beginTransaction();
                 transaction.replace(R.id.char_overview, newFragment);
                 transaction.addToBackStack(null);
                 Bundle b = new Bundle();
                 b.putInt("character_id", characterIds.get((int) id));
                 newFragment.setArguments(b);
                 transaction.commit(); 
             }
         });
         
         //Klickt man laenger auf einen Charakter, so oeffnet sich ein Dialog
         listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
         {
         	@Override
             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, final long id)
             {
        
         		final class MenuFragment extends DialogFragment
         		{
         			protected int args;
         			protected GameCharacter gc;
         			public ArrayList<String> menu_list = new ArrayList<String>();
         			
         			@Override
         		    public Dialog onCreateDialog(Bundle savedInstanceState) 
         			{	//der Dialog holt sich die CharakterID des Chars auf dem er aufgerufen wurde und gelangt so zu dem Namen, der im Titel erscheint
         				args = getArguments().getInt("character_id");
         				gc = new GameCharacter(getActivity(),args);
         				String name = gc.getAsString("name");
         				
         				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         				//Der Dialog besteht aus einer Liste mit eigenem Layout und dem Titel, der direkt im code designed wird
         		        menu_list.add("Anzeigen");
         		        menu_list.add("Editieren");
         		        menu_list.add("Lschen");
         		        final ArrayAdapter<String> arrayAdapter = new MenuAdapter(getActivity(), R.layout.char_list_menu_layout, menu_list);
         		        
         		        TextView title = new TextView(getActivity());
         		        Typeface font        = Typeface.createFromAsset(getActivity().getAssets(), "balthazar.TTF");
         		        title.setText(name);
         		        title.setBackgroundColor(Color.rgb(239, 219, 170));
         		        title.setPadding(10, 10, 10, 10);
         		        title.setKeepScreenOn(true);
         		        title.setGravity(Gravity.CENTER);
         		        title.setTextColor(Color.BLACK);
         		        title.setTextSize(40);
         		        title.setTypeface(font);
         		        builder.setCustomTitle(title);
         		        
         		        builder.setAdapter(arrayAdapter,new DialogInterface.OnClickListener() 
         		        {
         		              @Override
         		              public void onClick(DialogInterface dialog, int which) 
         		              {
         		            	  switch (which) 
         		          		  {
         		          		  		case 0: //beim Klick auf Anzeigen kommt man ebenfalls in die Charakteransicht
         		          	        		Fragment            newFragment = new CharAnzeigeFragment();
         		          	                FragmentTransaction transaction = getFragmentManager().beginTransaction();
         		          	                transaction.replace(R.id.char_overview, newFragment);
         		          	                transaction.addToBackStack(null);
         		          	                Bundle b1 = new Bundle();
         		          	                b1.putInt("character_id", characterIds.get((int) id));
         		          	                newFragment.setArguments(b1);
         		          	                transaction.commit(); 
         		          	                break;
         		          		  		case 1: //beim Klick auf editieren in den Charaktereditor, der nun aber schon mit den Werten des Chars ausgefuellt ist (theoretisch...)
         		          		  			Fragment            newFragment2 = new CharakterEditorFragment1();
         		          		  			FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
         		          		  			Bundle b2 = new Bundle();
         		          		  			b2.putInt("character_id", args);
         		          		  			transaction2.replace(R.id.char_overview, newFragment2);
         		          		  			transaction2.addToBackStack(null);
         		          		  			newFragment2.setArguments(b2);
         		          		  			transaction2.commit(); 
         		          		  			break;
         		          		  		case 2: //beim Klick auf loeschen wird die Funktion zum loeschen eines Chars aufgerufen. Theoretisch sollte hier noch eine ja/nein Abfrage dazwischen stehen
         		          		  			deleteChar(adapter, (int) id);
         		          		   }
         		   
         		               }
         		        });
         		        
         		        return builder.create();
         			}
         			
         			//hier bekommt die Liste im Dialog ihr schickes Layout
         			final class MenuAdapter extends ArrayAdapter<String>
         			{
 
         		        public MenuAdapter(Context context, int textViewResourceId, List<String> charas) 
         		        {
         		            super(context, textViewResourceId, charas);
         		        }
 
         		        @Override
         		        public View getView(int position, View convertView, ViewGroup parent)
         		        {
         		            LayoutInflater inflater = getActivity().getLayoutInflater();
         		            View row = inflater.inflate(R.layout.char_list_menu_layout, parent, false);
 
         		            TextView button = (TextView)row.findViewById(R.id.auswahl);
         		            button.setText(menu_list.get(position));
         		            
         		            ImageView icon = (ImageView)row.findViewById(R.id.icon);
 
         		            switch(position)
         		            {
         		                case 0:
         		                    icon.setImageResource(R.drawable.auswahl_icon02);
         		                    break;
         		                case 1:
         		                    icon.setImageResource(R.drawable.auswahl_icon03);
         		                    break;
         		                case 2:
         		                    icon.setImageResource(R.drawable.auswahl_icon01);
         		                    break;
 
         		            }
         		            
         		            Typeface font        = Typeface.createFromAsset(getActivity().getAssets(), "balthazar.TTF");
         		            button.setTypeface(font);
         		            
         		            return row;
         		        }
         			}
 
         		}
         		
         		//hier wird das obige MenuFragment aufgerufen, nachdem man laenger auf einen ListView Eintrag geklickt hat und bekommt die id des Chars auf dem es aufgerufen wurde 
         		Bundle b = new Bundle();
                 b.putInt("character_id", characterIds.get((int) id));
         		FragmentTransaction ft = getFragmentManager().beginTransaction();
         	    MenuFragment newFragment = new MenuFragment();
         	    newFragment.setArguments(b);
         	    newFragment.show(ft, "dialog");
  
         	    return true;
             }
         });
         
     }
     
     //diese Funktion loescht den ausgewaehlten Char. Zur Zeit schummelt sie noch und loescht ihn nur aus der Liste, nicht aber komplett aus der Datenbank
     public void deleteChar(ArrayAdapter<String> adapter, int id)
     {
     	characterNames.remove(id);
     	avatarIDs.remove(id);
     	adapter.notifyDataSetChanged();
     }
     
     //Dieser Adapter sorgt dafuer, dass die Liste schick aussieht
 	public class CharListAdapter extends ArrayAdapter<String>
 	{
 
         public CharListAdapter(Context context, int textViewResourceId, List<String> charas) 
         {
             super(context, textViewResourceId, charas);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent)
         {
             LayoutInflater inflater = getActivity().getLayoutInflater();
             View row = inflater.inflate(R.layout.char_list_row_layout, parent, false);
             
             TextView name = (TextView)row.findViewById(R.id.char_name);
             name.setText(characterNames.get(position));
 
             ImageView ava=(ImageView)row.findViewById(R.id.avatar);
             
             //Hier wird jedem Char der korrekte Avatar zugeordnet
             switch(avatarIDs.get(position))
             {
                 case 1:
                     ava.setImageResource(R.drawable.ava01);
                     break;
                 case 2:
                     ava.setImageResource(R.drawable.ava02);
                     break;
                 case 3:
                     ava.setImageResource(R.drawable.ava03);
                     break;
                 case 4:
                     ava.setImageResource(R.drawable.ava04);
                     break;
                 case 5:
                     ava.setImageResource(R.drawable.ava05);
                     break;
                 case 6:
                     ava.setImageResource(R.drawable.ava06);
                     break;
                 case 7:
                     ava.setImageResource(R.drawable.ava07);
                     break;
                 case 8:
                     ava.setImageResource(R.drawable.ava08);
                     break;
                 case 9:
                     ava.setImageResource(R.drawable.ava09);
                     break;
                 case 10:
                     ava.setImageResource(R.drawable.ava10);
                     break;
                 default: //da es zur Zeit bei der Charaktererstellung noch nicht moeglich ist, sich einen Avatar auszusuchen, gibt es zusaetzlich eine Random-Variante die das fuer einen uebernimmt
                 	int random = new Random().nextInt(10);
                 	switch(random)
                 	{
                 	case 0: ava.setImageResource(R.drawable.ava01);
                			avatarIDs.add(1);
                 	case 1: ava.setImageResource(R.drawable.ava02);
                			avatarIDs.add(2);
                 	case 2: ava.setImageResource(R.drawable.ava03);
                			avatarIDs.add(3);
                 	case 3: ava.setImageResource(R.drawable.ava04);
                			avatarIDs.add(4);
                 	case 4: ava.setImageResource(R.drawable.ava05);
                			avatarIDs.add(5);
                 	case 5: ava.setImageResource(R.drawable.ava06);
                			avatarIDs.add(6);
                 	case 6: ava.setImageResource(R.drawable.ava07);
                			avatarIDs.add(7);
                 	case 7: ava.setImageResource(R.drawable.ava08);
                			avatarIDs.add(8);
                 	case 8: ava.setImageResource(R.drawable.ava09);
                			avatarIDs.add(9);
                 	case 9:	ava.setImageResource(R.drawable.ava10);
                			avatarIDs.add(10);
                 	}
             }
             
             Typeface font        = Typeface.createFromAsset(getActivity().getAssets(), "balthazar.TTF");
             name.setTypeface(font);
             TextView spiel = (TextView)row.findViewById(R.id.spielsystem);
             spiel.setTypeface(font);
             
             return row;
         }
     }
     
 }
