 package com.pate.diablo;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ListView;
 
 import com.pate.diablo.model.D3Application;
 import com.pate.diablo.model.Rune;
 import com.pate.diablo.model.Skill;
 import com.pate.diablo.sectionlist.EmptyRune;
 import com.pate.diablo.sectionlist.EmptySkill;
 import com.pate.diablo.sectionlist.EntryRune;
 import com.pate.diablo.sectionlist.EntrySkill;
 import com.pate.diablo.sectionlist.EntrySkillAdapter;
 import com.pate.diablo.sectionlist.Item;
 import com.pate.diablo.sectionlist.SectionItem;
 
 public class ClassListFragment extends ListFragment
 {
     private Context context;
     private String selectedClass;
     private int requiredLevel;
     private OnClickListener listener;
     private EntrySkillAdapter listAdapter;
     private String mContent = "???";
     int index;
     int GET_SKILL = 0;
     int REPLACE_SKILL = 1;
     int GET_RUNE = 2;
     int REPLACE_RUNE = 3;
     
     ArrayList<Item> items = new ArrayList<Item>();
 
     public static ClassListFragment newInstance(String selectedClass,int requiredLevel, Context c) {
         ClassListFragment fragment = new ClassListFragment();
 
         fragment.context = c;
         fragment.selectedClass = selectedClass;
         fragment.requiredLevel = requiredLevel;
         
         return fragment;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         setRetainInstance(true);
         setListAdapter(getSkillListAdapter());
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
     }
     
     public void setOnListItemClickListener(OnClickListener listener)
     {
     	this.listener = listener;
     }
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 	    Item item = items.get(position);
         Bundle b = new Bundle();
         
         if (item instanceof EmptySkill)
         {
             EmptySkill e = (EmptySkill) item;
             
             Intent intent = new Intent(v.getContext(), SelectSkill.class);     
             b.putString("SkillType", e.getSkillType());
             b.putString("SelectedClass", selectedClass);
             b.putInt("RequiredLevel", requiredLevel);
             b.putInt("Index", position);
             intent.putExtras(b);
 
             startActivityForResult(intent, GET_SKILL);
         } 
         else if (item instanceof EntrySkill)
         {
             EntrySkill e = (EntrySkill) item;
             
             Intent intent = new Intent(v.getContext(), SelectSkill.class);     
             b.putString("SkillType", e.getSkill().getType());
             b.putString("SelectedClass", selectedClass);
             b.putInt("RequiredLevel", requiredLevel);
             b.putInt("Index", position);
             intent.putExtras(b);
 
             startActivityForResult(intent, REPLACE_SKILL);
         } 
         else if (item instanceof EmptyRune)
         {
             EmptyRune e = (EmptyRune) item;
             
             Intent intent = new Intent(v.getContext(), SelectRune.class);     
             b.putString("SkillName", e.getSkillName());
             b.putString("SelectedClass", selectedClass);
             b.putInt("RequiredLevel", requiredLevel);
             b.putSerializable("SkillUUID", e.getSkillUUID());
             b.putInt("Index", position);
             intent.putExtras(b);
 
             startActivityForResult(intent, GET_RUNE);
         }
         else if (item instanceof EntryRune)
         {
             EntryRune e = (EntryRune) item;
             
             Intent intent = new Intent(v.getContext(), SelectRune.class);     
             b.putString("SkillName", e.getSkillName());
             b.putString("SelectedClass", selectedClass);
             b.putInt("RequiredLevel", requiredLevel);
             b.putSerializable("SkillUUID", e.getSkillUUID());
             b.putInt("Index", position);
             intent.putExtras(b);
 
             startActivityForResult(intent, REPLACE_RUNE);
         }
        super.onListItemClick(l, v, position, id);
 		super.onListItemClick(l, v, position, id);
 	}
 	
     private EntrySkillAdapter getSkillListAdapter()
     {
         items = new ArrayList<Item>();
         String[] skillTypes = D3Application.dataModel.getClassAttributesByName(selectedClass).getSkillTypes();
         
         items.add(new SectionItem("Left Click - Primary"));
         items.add(new EmptySkill("Choose Skill", 1, skillTypes[0]));
         
         items.add(new SectionItem("Right Click - Secondary"));
         items.add(new EmptySkill("Choose Skill", 2, skillTypes[1]));
         
         items.add(new SectionItem("Action Bar Skills"));
         items.add(new EmptySkill("Choose Skill", 4 , skillTypes[2]));
         items.add(new EmptySkill("Choose Skill", 9 , skillTypes[3]));
         items.add(new EmptySkill("Choose Skill", 14, skillTypes[4]));
         items.add(new EmptySkill("Choose Skill", 19, skillTypes[5]));
         
         items.add(new SectionItem("Passive Skills"));
         items.add(new EmptySkill("Choose Skill", 10, "Passive"));
         items.add(new EmptySkill("Choose Skill", 20, "Passive"));
         items.add(new EmptySkill("Choose Skill", 30, "Passive"));
         
         listAdapter = new EntrySkillAdapter(context, items);
         
         return listAdapter;
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         if (requestCode == GET_SKILL || requestCode == REPLACE_SKILL)
         {
             if (resultCode == Activity.RESULT_OK)
             {
                 Bundle b = data.getExtras();
                 
                 String skillUUID = null;
                 int index = -1;
                 
                 if (b.containsKey("Skill_UUID"))
                 {
                     skillUUID = b.getString("Skill_UUID");
                 }
                 
                 if (b.containsKey("Index"))
                 {
                     index = b.getInt("Index");
                 }
                 
                 Log.i("onActivityResult", "UUID: " + skillUUID + " Index: " + index);
                 
                 if (skillUUID != null && index >= 0 && D3Application.dataModel.getClassByName(selectedClass).containsActiveSkillByUUID(UUID.fromString(skillUUID)))
                 {
                     Log.i("onActivityResult", "Active Skill Found!");
                     Skill s = D3Application.dataModel.getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID));
                     items.set(index, new EntrySkill(s));
                     if (requestCode == GET_SKILL)
                     {
                         items.add(index + 1,  new EmptyRune("Choose Rune", 1, s.getName(), s.getUuid()));
                     }
                     else if (requestCode == REPLACE_SKILL)
                     {
                         items.set(index + 1,  new EmptyRune("Choose Rune", 1, s.getName(), s.getUuid()));
                     }
                     listAdapter.setList(items);
                     
                 }
                 else if (skillUUID != null && index >= 0 && D3Application.dataModel.getClassByName(selectedClass).containsPassiveSkillByUUID(UUID.fromString(skillUUID)))
                 {
                     Log.i("onActivityResult", "Passive Skill Found!");
                     Skill s = D3Application.dataModel.getClassByName(selectedClass).getPassiveSkillByUUID(UUID.fromString(skillUUID));
                     items.set(index, new EntrySkill(s));
                     listAdapter.setList(items);
                 }
                 else
                 {
                     //Uh-Oh!
                 }
             }
             else
             {
                 //Do nothing?
             }
         }
         else if (requestCode == GET_RUNE || requestCode == REPLACE_RUNE)
         {
             if (resultCode == Activity.RESULT_OK)
             {
                 Bundle b = data.getExtras();
                 
                 String runeUUID = null;
                 String skillUUID = null;
                 int index = -1;
                 
                 if (b.containsKey("Rune_UUID"))
                 {
                     runeUUID = b.getString("Rune_UUID");
                 }
                 
                 if (b.containsKey("Skill_UUID"))
                 {
                     skillUUID = b.getString("Skill_UUID");
                 }
                 
                 if (b.containsKey("Index"))
                 {
                     index = b.getInt("Index");
                 }
                 
                 Log.i("onActivityResult", "UUID: " + runeUUID + " Index: " + index);
                 
                 if (D3Application.dataModel.getClassByName(selectedClass).containsActiveSkillByUUID(UUID.fromString(skillUUID)))
                 {
                     if (skillUUID != null && index >= 0 && D3Application.dataModel.getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID)).containsRuneByUUID(UUID.fromString(runeUUID)))
                     {
                         Log.i("onActivityResult", "Active Skill Found!");
                         Rune s = D3Application.dataModel.getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID)).getRuneByUUID(UUID.fromString(runeUUID));
                         items.set(index, new EntryRune(s, D3Application.dataModel.getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID)).getName(), UUID.fromString(skillUUID)));
                         listAdapter.setList(items);
                     }
                     else
                     {
                         //Uh-Oh!
                     }
                 }
                 else
                 {
                     //Uh-Oh
                 }
             }
             else
             {
                 //Do nothing?
             }
         }
     }
     
 }
