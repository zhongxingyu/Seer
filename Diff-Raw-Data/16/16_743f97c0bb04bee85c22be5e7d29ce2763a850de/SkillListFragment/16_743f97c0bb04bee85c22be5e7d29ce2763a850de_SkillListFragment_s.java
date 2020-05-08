 package com.wemakestuff.d3builder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.ParcelUuid;
 import android.support.v4.app.ListFragment;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ListView;
 
 import com.wemakestuff.d3builder.model.D3Application;
 import com.wemakestuff.d3builder.model.Skill;
 import com.wemakestuff.d3builder.sectionlist.EntrySkill;
 import com.wemakestuff.d3builder.sectionlist.EntrySkillAdapter;
 import com.wemakestuff.d3builder.sectionlist.Item;
 
 public class SkillListFragment extends ListFragment
 {
    Context context;
     String skillType;
     String selectedClass;
     int maxLevel = 60;
     OnClickListener listener;
     List<ParcelUuid> excludeSkills;
     
     ArrayList<Item> items = new ArrayList<Item>();
     private static final String KEY_CONTENT = "TestFragment:Content";
 
     public static SkillListFragment newInstance(String content, Context c, String skillType, String selectedClass, int maxLevel, List<ParcelUuid> excludeSkills) {
         SkillListFragment fragment = new SkillListFragment();
 
         fragment.context = c;
         fragment.skillType = skillType;
         fragment.selectedClass = selectedClass;
         fragment.maxLevel = maxLevel;
         fragment.excludeSkills = excludeSkills;
         
         return fragment;
     }
 
     private String mContent = "???";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         setRetainInstance(true);
         for (Skill s : D3Application.getInstance().getClassByName(selectedClass).getActiveSkillsByTypeAndRequiredLevel(skillType, maxLevel))
         {
             // Exclude already selected skills
             if (excludeSkills == null || !excludeSkills.contains(new ParcelUuid(s.getUuid())))
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
     
     public void setOnListItemClickListener(OnClickListener listener)
     {
     	this.listener = listener;
     }
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		listener.onClick(v);
 		super.onListItemClick(l, v, position, id);
 	}
     
 }
