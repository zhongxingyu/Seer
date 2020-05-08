 package com.pate.diablo;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 
 import com.pate.diablo.model.D3Application;
 import com.pate.diablo.model.Skill;
 import com.pate.diablo.sectionlist.EntrySkill;
 import com.pate.diablo.sectionlist.EntrySkillAdapter;
 import com.pate.diablo.sectionlist.Item;
 
 public class SkillListFragment extends ListFragment
 {
   Context context;
    String skillType;
    String selectedClass;
    int requiredLevel;
     
     ArrayList<Item> items = new ArrayList<Item>();
     private static final String KEY_CONTENT = "TestFragment:Content";
 
     public static SkillListFragment newInstance(String content, Context c, String skillType, String selectedClass, int requiredLevel) {
         SkillListFragment fragment = new SkillListFragment();
 
        fragment.context = c;
        fragment.skillType = skillType;
        fragment.selectedClass = selectedClass;
        fragment.requiredLevel = requiredLevel;
         
         return fragment;
     }
 
     private String mContent = "???";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
        for (Skill s : D3Application.dataModel.getClassByName(selectedClass).getActiveSkillsByType(this.skillType))
         {
             items.add(new EntrySkill(s));
         }
         EntrySkillAdapter adapter = new EntrySkillAdapter(context, items);
 
         setListAdapter(adapter);
         
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString(KEY_CONTENT, mContent);
     }
 }
