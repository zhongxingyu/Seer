 package com.wemakestuff.diablo3builder.followers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.ParcelUuid;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ListView;
 
 import com.wemakestuff.diablo3builder.model.D3Application;
 import com.wemakestuff.diablo3builder.model.Follower;
 import com.wemakestuff.diablo3builder.model.Skill;
 import com.wemakestuff.diablo3builder.sectionlist.EntryFollowerSkill;
 import com.wemakestuff.diablo3builder.sectionlist.EntrySkillAdapter;
 import com.wemakestuff.diablo3builder.sectionlist.Item;
 import com.wemakestuff.diablo3builder.sectionlist.SectionItem;
 import com.wemakestuff.diablo3builder.string.Vars;
 
 public class FollowerListFragment extends ListFragment
 {
 
     private String                         selectedFollower;
     private OnLoadFragmentCompleteListener loadFragmentCompleteListener;
     private OnRequiredLevelUpdateListener  requiredLevelListener;
     private OnSkillUpdateListener          skillUpdateListener;
     private ArrayList<Item>                items = new ArrayList<Item>();
 
     public interface OnRequiredLevelUpdateListener
     {
         void OnRequiredLevelUpdate(String name, int level);
     }
 
     public interface OnSkillUpdateListener
     {
         void OnSkillUpdate(String name, List<ParcelUuid> skills);
     }
 
     public interface OnLoadFragmentCompleteListener
     {
         void OnLoadFragmentComplete(String follower);
     }
 
     public static FollowerListFragment newInstance(String selectedFollower, Context c)
     {
 
         FollowerListFragment fragment = new FollowerListFragment();
 
         fragment.selectedFollower = selectedFollower;
         return fragment;
     }
 
     @Override
     public void onAttach(Activity activity)
     {
         super.onAttach(activity);
 
         try
         {
             requiredLevelListener = (OnRequiredLevelUpdateListener) activity;
         }
         catch (ClassCastException e)
         {
             throw new ClassCastException(activity.toString() + " must implement OnRequiredLevelUpdate(int level)");
         }
 
         try
         {
             loadFragmentCompleteListener = (OnLoadFragmentCompleteListener) activity;
         }
         catch (ClassCastException e)
         {
             throw new ClassCastException(activity.toString() + " must implement OnLoadFragmentCompleteListener(String follower)");
         }
 
         try
         {
             skillUpdateListener = (OnSkillUpdateListener) activity;
         }
         catch (ClassCastException e)
         {
             throw new ClassCastException(activity.toString() + " must implement skillUpdateListener(String name, List<ParcelUuid> Skills)");
         }
     }
 
     public String getSelectedFollower()
     {
         return selectedFollower;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 
         super.onCreate(savedInstanceState);
 
         if (savedInstanceState != null)
         {
             selectedFollower = savedInstanceState.getString("selectedFollower");
         }
 
         setRetainInstance(true);
         populateSkillListAdapter(false);
 
     }
 
     @Override
     public void onResume()
     {
         super.onResume();
         requiredLevelListener.OnRequiredLevelUpdate(selectedFollower, getMaxLevel());
         loadFragmentCompleteListener.OnLoadFragmentComplete(selectedFollower);
     }
 
     @Override
     public void onPause()
     {
         super.onPause();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         outState.putString("selectedFollower", selectedFollower);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id)
     {
         super.onListItemClick(l, v, position, id);
         SelectFollower sf = (SelectFollower) getActivity();
         EntrySkillAdapter skillAdapter = (EntrySkillAdapter) l.getAdapter();
         Item item = (Item) l.getItemAtPosition(position);
         EntryFollowerSkill pairedSkill = null;
 
         if (item instanceof EntryFollowerSkill)
         {
             Follower follower = D3Application.getInstance().getFollowerByName(selectedFollower);
             EntryFollowerSkill e = (EntryFollowerSkill) item;
 
             pairedSkill = getPairedFollowerSkill(skillAdapter, pairedSkill, follower, e);
 
             if (e.isChecked())
             {
                 e.setIsChecked(false);
             }
             else
             {
                 e.setIsChecked(true);
                 pairedSkill.setIsChecked(false);
            }            
         }
 
         if (selectedFollower.equals(Vars.TEMPLAR))
         {
             sf.setTemplarSkills(getSelectedSkills());
         }
         else if (selectedFollower.equals(Vars.SCOUNDREL))
         {
             sf.setScoundrelSkills(getSelectedSkills());
         }
         else if (selectedFollower.equals(Vars.ENCHANTRESS))
         {
             sf.setEnchantressSkills(getSelectedSkills());
         }
 
         requiredLevelListener.OnRequiredLevelUpdate(selectedFollower, getMaxLevel());
     }
 
     public List<ParcelUuid> getSelectedSkills()
     {
         List<ParcelUuid> skills = new ArrayList<ParcelUuid>();
         ArrayList<Item> items = ((EntrySkillAdapter) getListAdapter()).getItems();
 
         for (Item i : items)
         {
             if (i instanceof EntryFollowerSkill)
             {
                 EntryFollowerSkill e = (EntryFollowerSkill) i;
                 if (e.isChecked())
                     skills.add(new ParcelUuid(e.getSkill().getUuid()));
             }
         }
 
         return skills;
     }
 
     private EntryFollowerSkill getPairedFollowerSkill(EntrySkillAdapter skillAdapter, EntryFollowerSkill pairedSkill, Follower follower, EntryFollowerSkill e)
     {
         for (Skill s : follower.getSkillsByRequiredLevel(e.getSkill().getRequiredLevel()))
         {
             if (!s.getUuid().equals(e.getSkill().getUuid()))
             {
                 pairedSkill = (EntryFollowerSkill) skillAdapter.getFollowerSkillByUUID(s.getUuid());
             }
 
         }
         return pairedSkill;
     }
     
     private void populateSkillListAdapter(boolean clear)
     {
         LayoutInflater l = LayoutInflater.from(getActivity());
 
         items = new ArrayList<Item>();
         SelectFollower f = (SelectFollower) getActivity();
         List<ParcelUuid> selectedSkills = f.getSkillsByClass(selectedFollower);
 
         Follower follower = D3Application.getInstance().getFollowerByName(selectedFollower);
         List<Integer> requiredLevels = follower.getRequiredLevels();
 
         for (Integer i : requiredLevels)
         {
             items.add(new SectionItem(l, "Level " + i));
             List<Skill> skillsByLevel = follower.getSkillsByRequiredLevel(i.intValue());
 
             for (Skill s : skillsByLevel)
             {
                 if (clear)
                     items.add(new EntryFollowerSkill(l, s, selectedFollower, false));
                 else
                     items.add(new EntryFollowerSkill(l, s, selectedFollower, selectedSkills.contains(new ParcelUuid(s.getUuid()))));
 
             }
 
         }
 
         setListAdapter(new EntrySkillAdapter(getActivity(), items));
     }
 
     public int getMaxLevel()
     {
         return ((EntrySkillAdapter) getListAdapter()).getFollowerMaxLevel(selectedFollower);
     }
 
     public void clear()
     {
         clearSelectedSkills();
         populateSkillListAdapter(true);
         requiredLevelListener.OnRequiredLevelUpdate(selectedFollower, 1);
     }
 
     public void clearSelectedSkills()
     {
         for (int i = 0; i < items.size(); i++)
         {
             Item item = items.get(i);
             if (item instanceof EntryFollowerSkill)
             {
                 EntryFollowerSkill s1 = (EntryFollowerSkill) item;
                 s1.setIsChecked(false);
             }
         }
 
         List<ParcelUuid> blank = new ArrayList<ParcelUuid>();
         skillUpdateListener.OnSkillUpdate(Vars.TEMPLAR, blank);
         skillUpdateListener.OnSkillUpdate(Vars.SCOUNDREL, blank);
         skillUpdateListener.OnSkillUpdate(Vars.ENCHANTRESS, blank);
     }
 
     public void setSelectedSkills(String followerLink)
     {
         ArrayList<Item> items = ((EntrySkillAdapter) getListAdapter()).getItems();
 
         if (followerLink.length() > 4)
         {
             Log.e("Follower URL too Long!", followerLink);
             return;
         }
 
         int charIndex = 0;
         for (int i = 0; i < items.size(); i++)
         {
             Item item = items.get(i);
             //@formatter:off
             if (item instanceof EntryFollowerSkill)
             {
                 if (charIndex >= followerLink.length())
                     break;
                     
                 String s = String.valueOf(followerLink.charAt(charIndex));
                 EntryFollowerSkill s1 = (EntryFollowerSkill) item;
                 EntryFollowerSkill s2 = (EntryFollowerSkill) items.get(i+1);
                 
                 if (s.equals("0"))
                 {
                     s1.setIsChecked(true);
                     s2.setIsChecked(false);
                 }
                 else if (s.equals("1"))
                 {
                     s1.setIsChecked(false);
                     s2.setIsChecked(true);
                 }
                 else if (s.equals("."))
                 {
                     s1.setIsChecked(false);
                     s2.setIsChecked(false);
                 }
 
                 charIndex++;
                 // Skip the paired skill since we'll have set it's value already
                 i++;
             }
         }
         
         setListAdapter(new EntrySkillAdapter(getActivity(), items));
         requiredLevelListener.OnRequiredLevelUpdate(selectedFollower, getMaxLevel());
     }
 
     public String linkifyClassBuild() {
         String missingVal = D3Application.getInstance().getSkillAttributes().getMissingValue();
         ArrayList<Item> items = ((EntrySkillAdapter) getListAdapter()).getItems();
 
         StringBuffer build = new StringBuffer("");
 
         for (int i = 0; i < items.size(); i++) {
             Item item = items.get(i);
             //@formatter:off
             if (item instanceof EntryFollowerSkill)
             {
                 EntryFollowerSkill s1 = (EntryFollowerSkill) item;
                 EntryFollowerSkill s2 = (EntryFollowerSkill) items.get(i+1);
                 build.append(s1.isChecked() ? "0" 
                                             : s2.isChecked() ? "1" 
                                                              : missingVal);
                 
                 i++;
             }
         }
         //@formatter:on
 
         return build.toString();
     }
 
 }
