 package com.boomui.ddcharactersheet;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.*;
 import android.content.Context;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.*;
 import android.view.View.*;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 import android.widget.ExpandableListView.OnGroupClickListener;
 import android.widget.ExpandableListView.OnGroupCollapseListener;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 
 /*
  * At some point, this will also have to save the scroll distance on each of the panes
  */
 
 public class MagicTabFragment extends Fragment implements SpellInteractionListener{
 	FragmentCommunicator com;
 	Activity parent;
 	
 	LinearLayout[] classPanes;
 	String[] classes;
 	int maxHeight = -1;
 	int selected = -1;
 	
 	List<BaseExpandableListAdapter> expandableListAdapters;
 	
 	//For saving
 	List<SpellsKnown> allSpellsKnown;
 	List<SpellsPrepared> allSpellsPrepared;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 	}
 	
 	public void onAttach(Activity activity){
 		super.onAttach(activity);
 		
 		parent = activity;
 		com = (FragmentCommunicator)activity;
 
		//This code hides the keyboard
 		InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
 		inputManager.hideSoftInputFromWindow((null == activity.getCurrentFocus()) ? null : activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 	
 	public void onDestroyView(){
 		super.onDestroyView();
 		com.saveData(CharacterDataKey.MAGIC_TAB_CLASS_PAGE_OPEN, classes[selected]);
 		
 		String saveStr = "";
 		for(SpellsPrepared sp : allSpellsPrepared){
 			saveStr += sp.save() + SpellsPrepared.CHARACTER_SPLIT;
 		}
 		com.saveData(CharacterDataKey.SPELLS_PREPARED, saveStr);
 		
 		saveStr = "";
 		for(SpellsKnown sk : allSpellsKnown){
 			saveStr += sk.save() + SpellsKnown.CHARACTER_SPLIT;
 		}
 		com.saveData(CharacterDataKey.SPELLS_KNOWN, saveStr);
 	}
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		//return inflater.inflate(R.layout.magic_tab_fragment_layout, container, false);
 		//return createClassView("Misc.");
 		
 		allSpellsKnown = new LinkedList<SpellsKnown>();
 		allSpellsPrepared = new LinkedList<SpellsPrepared>();
 		expandableListAdapters = new LinkedList<BaseExpandableListAdapter>();
 		
 		LinearLayout retVal = new LinearLayout(parent);
 		retVal.setOrientation(LinearLayout.VERTICAL);
 		retVal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
 
 		//This will load the set of classes later
 		String allClasses = Constants.defaultClasses;
 		classes = allClasses.split(Constants.CHARACTER_CLASS_SEPARATOR);
 		
 		classPanes = new LinearLayout[classes.length];
 		for(int i = 0; i < classes.length; i++){
 			LinearLayout classPane = createClassView(classes[i]);
 			classPane.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1, 0));
 			classPanes[i] = classPane;
 		}
 		
 		String classOpen = com.loadData(CharacterDataKey.MAGIC_TAB_CLASS_PAGE_OPEN);
 		//Default spell page that will always be there
 		if(classOpen == null || allClasses.indexOf(classOpen) == -1){
 			classOpen = "Misc.";
 		}
 		
 		final MagicTabFragment thisFrag = this;
 		boolean addToTop = true;
 		
 		for(int i = 0; i < classes.length; i++){
 			Button b = new Button(parent);
 			b.setText(classes[i]);
 			
 			final int index = i;
 			OnClickListener buttonListener = new OnClickListener(){
 				public void onClick(View v){
 					thisFrag.setCurrentPane(index);
 				}
 			};
 			
 			b.setOnClickListener(buttonListener);
 	    	b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0) );
 			retVal.addView(b);
 			retVal.addView(classPanes[i]);
 			
 			if(classes[i].equals(classOpen) ){
 				classPanes[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
 				selected = i;
 			}
 		}
 		
 		return retVal;
 	}
 	
 	public void spellClicked(String spellName, int level){
 		allSpellsPrepared.get(selected).addNewSpell(spellName, level);
 		
 		for(BaseExpandableListAdapter bela : expandableListAdapters){
 			bela.notifyDataSetChanged();
 		}
 	}
 	
 	public void setCurrentPane(int selected){
 		maxHeight = classPanes[this.selected].getHeight();
 		
 		if(this.selected == selected){
 			return;
 		}
 		
 		/*for(int i = 0; i < classPanes.length; i++){
 			if(i != selected){
 				classPanes[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1, 0));
 			}
 			else{
 				classPanes[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
 				this.selected = selected;
 			}
 		}*/
 		
 		/*TabExpansionAnimation shrink = new TabExpansionAnimation(classPanes[this.selected], 100, 1, true);
 		TabExpansionAnimation grow = new TabExpansionAnimation(classPanes[selected], 100, maxHeight, false);
 		
 		classPanes[this.selected].startAnimation(shrink);
 		classPanes[selected].startAnimation(grow);*/
 		
 		TabExpansionAnimation change = new TabExpansionAnimation(classPanes[selected], classPanes[this.selected], Constants.MAGIC_TAB_ANIMATION_LENGTH);
 		classPanes[selected].startAnimation(change);
 		
 		this.selected = selected;
 	}
 	
 	public LinearLayout createClassView(String characterClass){
 		LinearLayout horizColumns = new LinearLayout(parent);
 		
 		View sk = createSpellsKnown(characterClass);
 		View sp = createSpellsPrepared(characterClass);
 		
     	sk.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1) );
     	sp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1) );
 		
 		horizColumns.addView(sk);
 		horizColumns.addView(sp);
 		horizColumns.setOrientation(LinearLayout.HORIZONTAL);
 		
 		horizColumns.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT) );
 		
 		return horizColumns;
 	}
 	
 	public View createSpellsPrepared(String characterClass){
 		TextView header = getGenericView();
 		header.setText("Spells Prepared");
 		
 		final ExpandableListView spellsPreparedView = new ExpandableListView(parent);
 		SpellsPrepared sp = new SpellsPrepared(com, characterClass);
 		allSpellsPrepared.add(sp);
 		SpellsPreparedExpandableListAdapter adapter = new SpellsPreparedExpandableListAdapter(sp, spellsPreparedView, parent);
 		spellsPreparedView.setAdapter(adapter);
 		expandableListAdapters.add(adapter);
 		//spellsPreparedView.setGroupIndicator(null);
 		
 		for(int i = 0; i < adapter.getGroupCount(); i++){
 			String result = com.loadData(CharacterDataKey.MAGIC_TAB_SPELLS_PREPARED_LEVEL_OPEN, "" + i);
 			if(result == null){
 				spellsPreparedView.expandGroup(i);
 				com.saveData(CharacterDataKey.MAGIC_TAB_SPELLS_PREPARED_LEVEL_OPEN, "" + i, "true");
 			}
 			else if(result.equals("true") ){
 				spellsPreparedView.expandGroup(i);
 			}
 		}
 
 		spellsPreparedView.setOnGroupCollapseListener(new OnGroupCollapseListener(){
 			public void onGroupCollapse(int groupPosition){
 				com.saveData(CharacterDataKey.MAGIC_TAB_SPELLS_PREPARED_LEVEL_OPEN, "" + groupPosition, "" + spellsPreparedView.isGroupExpanded(groupPosition) );
 			}
 		});
 		spellsPreparedView.setOnGroupExpandListener(new OnGroupExpandListener(){
 			public void onGroupExpand(int groupPosition){
 				com.saveData(CharacterDataKey.MAGIC_TAB_SPELLS_PREPARED_LEVEL_OPEN, "" + groupPosition, "" + spellsPreparedView.isGroupExpanded(groupPosition) );
 			}
 		});
 		
 		LinearLayout column = new LinearLayout(parent);
 		column.setOrientation(LinearLayout.VERTICAL);
 		column.addView(header);
 		column.addView(spellsPreparedView);
 		
 		return column;
 	}
 	
 	public View createSpellsKnown(String characterClass){
 		TextView header = getGenericView();
 		header.setText("Spells Known");
 		
 		final ExpandableListView spellsKnownView = new ExpandableListView(parent);
 		SpellsKnown sk = new SpellsKnown(com, characterClass);
 		allSpellsKnown.add(sk);
 		SpellsKnownExpandableListAdapter adapter = new SpellsKnownExpandableListAdapter(sk, parent, this);
 		spellsKnownView.setAdapter(adapter);
 		spellsKnownView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1) );
 		expandableListAdapters.add(adapter);
 		
 		for(int i = 0; i < adapter.getGroupCount(); i++){
 			String result = com.loadData(CharacterDataKey.MAGIC_TAB_SPELLS_KNOWN_LEVEL_OPEN, "" + i);
 			if(result == null){
 				spellsKnownView.expandGroup(i);
 				com.saveData(CharacterDataKey.MAGIC_TAB_SPELLS_KNOWN_LEVEL_OPEN, "" + i, "true");
 			}
 			else if(result.equals("true") ){
 				spellsKnownView.expandGroup(i);
 			}
 		}
 
 		spellsKnownView.setOnGroupCollapseListener(new OnGroupCollapseListener(){
 			public void onGroupCollapse(int groupPosition){
 				com.saveData(CharacterDataKey.MAGIC_TAB_SPELLS_KNOWN_LEVEL_OPEN, "" + groupPosition, "" + spellsKnownView.isGroupExpanded(groupPosition) );
 			}
 		});
 		spellsKnownView.setOnGroupExpandListener(new OnGroupExpandListener(){
 			public void onGroupExpand(int groupPosition){
 				com.saveData(CharacterDataKey.MAGIC_TAB_SPELLS_KNOWN_LEVEL_OPEN, "" + groupPosition, "" + spellsKnownView.isGroupExpanded(groupPosition) );
 			}
 		});
 		
 		Button addSpell = new Button(parent);
 		addSpell.setText("Add Spells");
 		addSpell.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0) );
 		
 		LinearLayout column = new LinearLayout(parent);
 		column.setOrientation(LinearLayout.VERTICAL);
 		column.addView(header);
 		column.addView(spellsKnownView);
 		column.addView(addSpell);
 		
 		return column;
 	}
 
     public TextView getGenericView() {
         // Layout parameters for the ExpandableListView
         AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT, 64);
 
         TextView textView = new TextView(parent);
         textView.setLayoutParams(lp);
         // Center the text vertically
         textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
         // Set the text starting position
         textView.setPadding(36, 0, 0, 0);
         return textView;
     }
 }
