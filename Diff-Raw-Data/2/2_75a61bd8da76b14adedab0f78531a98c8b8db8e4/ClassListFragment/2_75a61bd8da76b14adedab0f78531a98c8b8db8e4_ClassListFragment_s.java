 package com.wemakestuff.d3builder;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.ParcelUuid;
 import android.os.Parcelable;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.view.ActionMode;
 import com.wemakestuff.d3builder.followers.FollowerListFragment.OnRequiredLevelUpdateListener;
 import com.wemakestuff.d3builder.followers.SelectFollower;
 import com.wemakestuff.d3builder.model.D3Application;
 import com.wemakestuff.d3builder.model.Follower;
 import com.wemakestuff.d3builder.model.Rune;
 import com.wemakestuff.d3builder.model.Skill;
 import com.wemakestuff.d3builder.model.SkillAttribute;
 import com.wemakestuff.d3builder.sectionlist.EmptyFollower;
 import com.wemakestuff.d3builder.sectionlist.EmptyRune;
 import com.wemakestuff.d3builder.sectionlist.EmptySkill;
 import com.wemakestuff.d3builder.sectionlist.EntryRune;
 import com.wemakestuff.d3builder.sectionlist.EntrySkill;
 import com.wemakestuff.d3builder.sectionlist.EntrySkillAdapter;
 import com.wemakestuff.d3builder.sectionlist.Item;
 import com.wemakestuff.d3builder.sectionlist.SectionItem;
 import com.wemakestuff.d3builder.string.Vars;
 
 public class ClassListFragment extends ListFragment
 {
 
     private Context                         context;
     private String                          selectedClass;
     private String                          followerUrl;
     private EntrySkillAdapter               listAdapter;
     private OnLoadFragmentsCompleteListener listener;
     private List<ParcelUuid>                templarSkills         = new ArrayList<ParcelUuid>();
     private List<ParcelUuid>                scoundrelSkills       = new ArrayList<ParcelUuid>();
     private List<ParcelUuid>                enchantressSkills     = new ArrayList<ParcelUuid>();
     private OnRequiredLevelUpdateListener   requiredLevelListener;
     int                                     index;
     int                                     GET_SKILL             = 0;
     int                                     REPLACE_SKILL         = 1;
     int                                     GET_RUNE              = 2;
     int                                     REPLACE_RUNE          = 3;
     int                                     NEW_FOLLOWER          = 4;
     int                                     REPLACE_FOLLOWER      = 5;
     int                                     followerRequiredLevel = 1;
     ActionMode                              mMode;
 
     ArrayList<Item>                         items                 = new ArrayList<Item>();
 
     public static ClassListFragment newInstance(String selectedClass, Context c, OnLoadFragmentsCompleteListener listener)
     {
 
         ClassListFragment fragment = new ClassListFragment();
 
         fragment.context = c;
         fragment.selectedClass = selectedClass;
         fragment.listener = listener;
         return fragment;
     }
 
     public String getSelectedClass()
     {
 
         return selectedClass;
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
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 
         super.onCreate(savedInstanceState);
 
         if (savedInstanceState != null)
         {
             selectedClass = savedInstanceState.getString("selectedClass");
         }
 
         setRetainInstance(true);
         setListAdapter(getSkillListAdapter());
         
     }
     
     public void clearListItem(int position)
     {
         Item i = items.get(position);
         
         if (i instanceof EntrySkill)
         {
             // If this is a skill, check if the next item in the list is a rune, if so we need to remove that too.
             String type = ((EntrySkill) i).getSkill().getType();
             items.set(position, new EmptySkill("Choose Skill", 1, type));
             if (items.size() > (position + 1))
             {
                 Item i2 = items.get(position + 1);
                 if (i2 instanceof EntryRune || i2 instanceof EmptyRune)
                 {
                     items.remove(i2);
                 }
             }
             
         }
         else if (i instanceof EntryRune)
         {
             items.set(position, new EmptyRune("Choose Rune", 1, "Rune", null));
         }
         else if (i instanceof EmptyFollower)
         {
             EmptyFollower e = (EmptyFollower) i;
             
             String existingName = e.getName();
             
             // No skills selected, nothing to clear
             if (!(getFollowerSkillsCount() > 0))
                 return;
             
             for (Follower f : D3Application.getInstance().getFollowers())
             {
                 if (f.getName().equalsIgnoreCase(existingName))
                 {
                     items.set(position, new EmptyFollower(f.getName(), f.getShortDescription(), f.getIcon(), f.getUuid(), ""));
                     
                     // Clear the skills, update the list, remove the skills from the URL
                     if (existingName.equals(Vars.TEMPLAR))
                     {
                         Log.i("FollowerUrl", followerUrl);
                         templarSkills = new ArrayList<ParcelUuid>();
                         updateFollowerData(templarSkills, scoundrelSkills, enchantressSkills, clearClassFromUrl(Vars.TEMPLAR));
                     }
                     else if (existingName.equals(Vars.SCOUNDREL))
                     {
                         Log.i("FollowerUrl", followerUrl);
                         scoundrelSkills = new ArrayList<ParcelUuid>();
                         updateFollowerData(templarSkills, scoundrelSkills, enchantressSkills, clearClassFromUrl(Vars.SCOUNDREL));
                     }
                     else if (existingName.equals(Vars.ENCHANTRESS))
                     {
                         Log.i("FollowerUrl", followerUrl);
                         enchantressSkills = new ArrayList<ParcelUuid>();
                         updateFollowerData(templarSkills, scoundrelSkills, enchantressSkills, clearClassFromUrl(Vars.ENCHANTRESS));
                     }
                 }
             }
         }
         
         listAdapter.notifyDataSetChanged();
     }
     
     public String clearClassFromUrl(String searchClass)
     {
         String[] url = followerUrl.split("#");
        StringBuffer returnVal = new StringBuffer(url[0]);
         
         String build = url[1];
         String templarLink = null;
         String scoundrelLink = null;
         String enchantressLink = null;
 
         String[] followers = build.split("!");
 
         switch (followers.length)
         {
             case 1:
                 templarLink = followers[0];
                 break;
 
             case 2:
                 templarLink = followers[0];
                 scoundrelLink = followers[1];
                 break;
 
             case 3:
                 templarLink = followers[0];
                 scoundrelLink = followers[1];
                 enchantressLink = followers[2];
                 break;
 
             default:
                 break;
         }
         
         if (searchClass.equalsIgnoreCase(Vars.TEMPLAR))
         {
             templarLink = "....";
         }
         else if (searchClass.equalsIgnoreCase(Vars.SCOUNDREL))
         {
             scoundrelLink = "....";
         }
         else if (searchClass.equalsIgnoreCase(Vars.ENCHANTRESS))
         {
             enchantressLink = "....";
         }
         
         returnVal.append(templarLink);
         returnVal.append(D3Application.getInstance().getSkillAttributes().getFollowerSeparator());
         returnVal.append(scoundrelLink);
         returnVal.append(D3Application.getInstance().getSkillAttributes().getFollowerSeparator());
         returnVal.append(enchantressLink);
         
         Log.i("clearClassFromurl", returnVal.toString());
         
         return returnVal.toString();
         
     }
 
     @Override
     public void onResume()
     {
 
         super.onResume();
 
         if (listener != null)
             listener.OnLoadFragmentsComplete(selectedClass);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState)
     {
         super.onActivityCreated(savedInstanceState);
 
         getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
 
             @Override
             public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
             {
                 Item selected = (Item) getListAdapter().getItem(position);
                 
                 if (selected instanceof EntrySkill)
                 {
                     ((SelectClass) getActivity()).onListItemLongClick(position, "Remove Skill");
                     
                 }
                 else if (selected instanceof EntryRune)
                 {
                     ((SelectClass) getActivity()).onListItemLongClick(position, "Remove Rune");
                     
                 }
                 else if (selected instanceof EmptyFollower)
                 {
                     EmptyFollower e = (EmptyFollower) selected;
                     
                     if (e.getSkills().size() > 0)
                     {
                         ((SelectClass) getActivity()).onListItemLongClick(position, "Remove Follower");
                     }
                     
                 }
                 
                 return true;
             }
         });
 
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
     {
 
         super.onCreateContextMenu(menu, v, menuInfo);
     }
 
     private boolean isBlankBuild(String classLink)
     {
 
         return classLink.length() == 0 || classLink.matches("http://.+.battle.net/d3/.+/calculator/.+#[\\.]+![\\.]+![\\.]+");
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
         outState.putString("selectedClass", selectedClass);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id)
     {
         ((SelectClass) getActivity()).clearActionMode();
 
         EntrySkillAdapter listAdapter = (EntrySkillAdapter) getListAdapter();
 
         Item item = (Item) getListAdapter().getItem(position);
         int maxLevel = 60;// ((Main) getActivity()).getMaxLevel();
         Bundle b = new Bundle();
 
         if (item instanceof EmptySkill)
         {
             EmptySkill e = (EmptySkill) item;
 
             Intent intent = new Intent(v.getContext(), SelectSkill.class);
             b.putString("SkillType", e.getSkillType());
             b.putString("SelectedClass", selectedClass);
             b.putInt("RequiredLevel", getMaxLevel());
             b.putInt("Index", position);
 
             List<ParcelUuid> skills = listAdapter.getCurrentSkills();
 
             if (skills.size() > 0)
                 b.putParcelableArrayList("UUIDs", (ArrayList<? extends Parcelable>) listAdapter.getCurrentSkills());
 
             intent.putExtras(b);
 
             startActivityForResult(intent, GET_SKILL);
         }
         else if (item instanceof EntrySkill)
         {
             EntrySkill e = (EntrySkill) item;
 
             Intent intent = new Intent(v.getContext(), SelectSkill.class);
             b.putString("SkillType", e.getSkill().getType());
             b.putString("SelectedClass", selectedClass);
             b.putInt("RequiredLevel", getMaxLevel());
             b.putInt("Index", position);
 
             List<ParcelUuid> skills = listAdapter.getCurrentSkills();
 
             if (skills.size() > 0)
                 b.putParcelableArrayList("UUIDs", (ArrayList<? extends Parcelable>) listAdapter.getCurrentSkills());
 
             intent.putExtras(b);
 
             startActivityForResult(intent, REPLACE_SKILL);
         }
         else if (item instanceof EmptyRune)
         {
             EmptyRune e = (EmptyRune) item;
 
             Intent intent = new Intent(v.getContext(), SelectRune.class);
             b.putString("SkillName", e.getSkillName());
             b.putString("SelectedClass", selectedClass);
             b.putInt("RequiredLevel", getMaxLevel());
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
             b.putInt("RequiredLevel", getMaxLevel());
             b.putSerializable("SkillUUID", e.getSkillUUID());
             b.putInt("Index", position);
             intent.putExtras(b);
 
             startActivityForResult(intent, REPLACE_RUNE);
         }
         else if (item instanceof EmptyFollower)
         {
             EmptyFollower e = (EmptyFollower) item;
 
             Intent intent = new Intent(v.getContext(), SelectFollower.class);
             intent.putParcelableArrayListExtra(Vars.TEMPLAR, (ArrayList<ParcelUuid>) templarSkills);
             intent.putParcelableArrayListExtra(Vars.SCOUNDREL, (ArrayList<ParcelUuid>) scoundrelSkills);
             intent.putParcelableArrayListExtra(Vars.ENCHANTRESS, (ArrayList<ParcelUuid>) enchantressSkills);
             intent.putExtra(Vars.FOLLOWERS, e.getName());
 
             startActivityForResult(intent, NEW_FOLLOWER);
         }
         super.onListItemClick(l, v, position, id);
     }
 
     private EntrySkillAdapter getSkillListAdapter()
     {
 
         return getSkillListAdapter(false);
     }
 
     private EntrySkillAdapter getSkillListAdapter(boolean includeRunes)
     {
 
         items = new ArrayList<Item>();
         String[] skillTypes = D3Application.getInstance().getClassAttributesByName(selectedClass).getSkillTypes();
 
         items.add(new SectionItem("Left Click - Primary"));
         items.add(new EmptySkill("Choose Skill", 1, skillTypes[0]));
         if (includeRunes)
             items.add(new EmptyRune("Choose Rune", 1, "Rune", null));
 
         items.add(new SectionItem("Right Click - Secondary"));
         items.add(new EmptySkill("Choose Skill", 2, skillTypes[1]));
         if (includeRunes)
             items.add(new EmptyRune("Choose Rune", 1, "Rune", null));
 
         items.add(new SectionItem("Action Bar Skills"));
         items.add(new EmptySkill("Choose Skill", 4, skillTypes[2]));
         if (includeRunes)
             items.add(new EmptyRune("Choose Rune", 1, "Rune", null));
         items.add(new EmptySkill("Choose Skill", 9, skillTypes[3]));
         if (includeRunes)
             items.add(new EmptyRune("Choose Rune", 1, "Rune", null));
         items.add(new EmptySkill("Choose Skill", 14, skillTypes[4]));
         if (includeRunes)
             items.add(new EmptyRune("Choose Rune", 1, "Rune", null));
         items.add(new EmptySkill("Choose Skill", 19, skillTypes[5]));
         if (includeRunes)
             items.add(new EmptyRune("Choose Rune", 1, "Rune", null));
 
         items.add(new SectionItem("Passive Skills"));
         items.add(new EmptySkill("Choose Skill", 10, "Passive"));
         items.add(new EmptySkill("Choose Skill", 20, "Passive"));
         items.add(new EmptySkill("Choose Skill", 30, "Passive"));
 
         items.add(new SectionItem("Followers"));
         for (Follower f : D3Application.getInstance().getFollowers())
         {
             items.add(new EmptyFollower(f.getName(), f.getShortDescription(), f.getIcon(), f.getUuid(), ""));
         }
 
         listAdapter = new EntrySkillAdapter(context, items);
 
         return listAdapter;
     }
 
     private void updateFollowerData(List<ParcelUuid> templar, List<ParcelUuid> scoundrel, List<ParcelUuid> enchantress, String followerUrl)
     {
 
         List<Item> items = ((EntrySkillAdapter) getListAdapter()).getFollowers();
 
         this.followerUrl = followerUrl;
 
         for (Item i : items)
         {
             if (i instanceof EmptyFollower)
             {
                 EmptyFollower e = (EmptyFollower) i;
                 if (e.getName().equals(Vars.TEMPLAR))
                 {
                     e.setSkills(templar);
                 }
                 else if (e.getName().equals(Vars.SCOUNDREL))
                 {
                     e.setSkills(scoundrel);
                 }
                 else if (e.getName().equals(Vars.ENCHANTRESS))
                 {
                     e.setSkills(enchantress);
                 }
             }
         }
 
         int maxLevel = 1;
         for (ParcelUuid u : templar)
         {
             Skill s = D3Application.getInstance().getFollowerByName(Vars.TEMPLAR).getSkillByUUID(u.getUuid());
 
             if (s.getRequiredLevel() > maxLevel)
             {
                 maxLevel = s.getRequiredLevel();
             }
         }
         for (ParcelUuid u : scoundrel)
         {
             Skill s = D3Application.getInstance().getFollowerByName(Vars.SCOUNDREL).getSkillByUUID(u.getUuid());
 
             if (s.getRequiredLevel() > maxLevel)
             {
                 maxLevel = s.getRequiredLevel();
             }
         }
         for (ParcelUuid u : enchantress)
         {
             Skill s = D3Application.getInstance().getFollowerByName(Vars.ENCHANTRESS).getSkillByUUID(u.getUuid());
 
             if (s.getRequiredLevel() > maxLevel)
             {
                 maxLevel = s.getRequiredLevel();
             }
         }
 
         followerRequiredLevel = maxLevel;
 
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data)
     {
 
         if (requestCode == NEW_FOLLOWER)
         {
             if (resultCode == Activity.RESULT_OK)
             {
                 if (data.hasExtra(Vars.TEMPLAR))
                 {
                     templarSkills = data.getParcelableArrayListExtra(Vars.TEMPLAR);
                 }
 
                 if (data.hasExtra(Vars.SCOUNDREL))
                 {
                     scoundrelSkills = data.getParcelableArrayListExtra(Vars.SCOUNDREL);
                 }
 
                 if (data.hasExtra(Vars.ENCHANTRESS))
                 {
                     enchantressSkills = data.getParcelableArrayListExtra(Vars.ENCHANTRESS);
                 }
 
                 if (data.hasExtra(Vars.URL))
                 {
                     followerUrl = data.getStringExtra(Vars.URL);
                 }
 
                 if (data.hasExtra(Vars.REQUIRED_LEVEL))
                 {
                     followerRequiredLevel = data.getIntExtra(Vars.REQUIRED_LEVEL, 1);
                     requiredLevelListener.OnRequiredLevelUpdate(Vars.FOLLOWERS, getMaxLevel());
                 }
 
                 updateFollowerData(templarSkills, scoundrelSkills, enchantressSkills, followerUrl);
             }
         }
 
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
 
                 if (skillUUID != null && index >= 0
                         && D3Application.getInstance().getClassByName(selectedClass).containsActiveSkillByUUID(UUID.fromString(skillUUID)))
                 {
                     Skill s = D3Application.getInstance().getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID));
                     items.set(index, new EntrySkill(s));
                     if (requestCode == GET_SKILL)
                     {
                         items.add(index + 1, new EmptyRune("Choose Rune", 1, s.getName(), s.getUuid()));
                     }
                     else if (requestCode == REPLACE_SKILL)
                     {
                         Item item = items.get(index + 1);
 
                         if (item instanceof EntryRune)
                         {
                             EntryRune e = (EntryRune) item;
                             if (!e.getSkillUUID().equals(s.getUuid()))
                             {
                                 items.set(index + 1, new EmptyRune("Choose Rune", 1, s.getName(), s.getUuid()));
                             }
                         }
                         else
                         {
                             items.set(index + 1, new EmptyRune("Choose Rune", 1, s.getName(), s.getUuid()));
                         }
                     }
                     listAdapter.notifyDataSetChanged();
                     requiredLevelListener.OnRequiredLevelUpdate(selectedClass, getMaxLevel());
 
                 }
                 else if (skillUUID != null && index >= 0
                         && D3Application.getInstance().getClassByName(selectedClass).containsPassiveSkillByUUID(UUID.fromString(skillUUID)))
                 {
                     Skill s = D3Application.getInstance().getClassByName(selectedClass).getPassiveSkillByUUID(UUID.fromString(skillUUID));
                     items.set(index, new EntrySkill(s));
                     listAdapter.notifyDataSetChanged();
                     requiredLevelListener.OnRequiredLevelUpdate(selectedClass, getMaxLevel());
                 }
                 else
                 {
                     // Uh-Oh!
                 }
                 this.getListView().setSelection(index);
             }
             else
             {
                 // Do nothing?
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
 
                 if (D3Application.getInstance().getClassByName(selectedClass).containsActiveSkillByUUID(UUID.fromString(skillUUID)))
                 {
                     if (skillUUID != null
                             && index >= 0
                             && D3Application.getInstance().getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID))
                                     .containsRuneByUUID(UUID.fromString(runeUUID)))
                     {
                         Rune s = D3Application.getInstance().getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID))
                                 .getRuneByUUID(UUID.fromString(runeUUID));
                         items.set(index,
                                 new EntryRune(s, D3Application.getInstance().getClassByName(selectedClass).getActiveSkillByUUID(UUID.fromString(skillUUID))
                                         .getName(), UUID.fromString(skillUUID)));
                         listAdapter.setList(items);
                         ((SelectClass) getActivity()).setRequiredLevel(listAdapter.getMaxLevel(false));
                     }
                     else
                     {
                         // Uh-Oh!
                     }
                 }
                 else
                 {
                     // Uh-Oh
                 }
                 this.getListView().setSelection(index - 1);
             }
             else
             {
                 // Do nothing?
             }
             
         }
     }
 
     public int getMaxLevel()
     {
         return Math.max(followerRequiredLevel, listAdapter.getMaxLevel(false));
     }
 
     public void clear()
     {
         templarSkills = new ArrayList<ParcelUuid>();
         scoundrelSkills = new ArrayList<ParcelUuid>();
         enchantressSkills = new ArrayList<ParcelUuid>();
         followerRequiredLevel = 1;
         requiredLevelListener.OnRequiredLevelUpdate(Vars.FOLLOWERS, 1);
         requiredLevelListener.OnRequiredLevelUpdate(selectedClass, 1);
         setListAdapter(getSkillListAdapter());
     }
 
     public String linkifyClassBuild(String prefix)
     {
 
         StringBuffer activeVal = new StringBuffer();
         StringBuffer passiveVal = new StringBuffer();
         StringBuffer runeVal = new StringBuffer();
 
         com.wemakestuff.d3builder.model.Class currClass = D3Application.getInstance().getClassByName(selectedClass);
         List<Skill> activeSkills = currClass.getActiveSkills();
         List<Skill> passiveSkills = currClass.getPassiveSkills();
         ArrayList<Item> items = listAdapter.getItems();
 
         SkillAttribute skillAttrbs = D3Application.getInstance().getSkillAttributes();
         String[] skillMapping = skillAttrbs.getSkillMapping();
 
         for (Item item : items)
         {
             if (item instanceof EmptySkill)
             {
                 EmptySkill e = (EmptySkill) item;
 
                 if (e.getSkillType().equals("Passive"))
                 {
                     passiveVal.append(skillAttrbs.getMissingValue());
                 }
                 else
                 {
                     activeVal.append(skillAttrbs.getMissingValue());
                     runeVal.append(skillAttrbs.getMissingValue());
                 }
             }
             else if (item instanceof EntrySkill)
             {
                 Skill s = ((EntrySkill) item).getSkill();
 
                 if (s.getType().equals("Passive"))
                 {
                     passiveVal.append(skillMapping[passiveSkills.indexOf(s)]);
                 }
                 else
                 {
                     activeVal.append(skillMapping[activeSkills.indexOf(s)]);
                 }
             }
             else if (item instanceof EmptyRune)
             {
                 EmptyRune e = (EmptyRune) item;
                 runeVal.append(skillAttrbs.getMissingValue());
 
             }
             else if (item instanceof EntryRune)
             {
                 EntryRune e = (EntryRune) item;
                 Rune r = e.getRune();
                 Skill s = currClass.getSkillByUUID(activeSkills, e.getSkillUUID());
                 runeVal.append(skillMapping[s.getRunes().indexOf(r)]);
             }
         }
 
         return prefix + selectedClass.toLowerCase().replace(" ", "-") + "#" + activeVal.toString() + skillAttrbs.getPassiveSeparator() + passiveVal.toString()
                 + skillAttrbs.getRuneSeparator() + runeVal.toString();
     }
 
     public String getFollowerSkills()
     {
 
         List<Item> items = ((EntrySkillAdapter) getListAdapter()).getFollowers();
         String templar = null;
         String scoundrel = null;
         String enchantress = null;
 
         for (Item i : items)
         {
             if (i instanceof EmptyFollower)
             {
                 EmptyFollower e = (EmptyFollower) i;
                 if (e.getName().equals(Vars.TEMPLAR))
                 {
                     templar = e.getSkills().toString();
                 }
                 else if (e.getName().equals(Vars.SCOUNDREL))
                 {
                     scoundrel = e.getSkills().toString();
                 }
                 else if (e.getName().equals(Vars.ENCHANTRESS))
                 {
                     enchantress = e.getSkills().toString();
                 }
             }
         }
 
         return templar + "|" + scoundrel + "|" + enchantress;
     }
 
     public static String[] trim(final String[] val)
     {
 
         for (int i = 0, len = val.length; i < len; i++)
         {
             if (val[i] != null)
                 val[i] = val[i].trim();
         }
 
         return val;
     }
 
     public void setFollowerSkills(String skills)
     {
 
         String build = skills.split("#")[1];
         String templarLink = null;
         String scoundrelLink = null;
         String enchantressLink = null;
 
         String[] followers = build.split("!");
 
         switch (followers.length)
         {
             case 1:
                 templarLink = followers[0];
                 break;
 
             case 2:
                 templarLink = followers[0];
                 scoundrelLink = followers[1];
                 break;
 
             case 3:
                 templarLink = followers[0];
                 scoundrelLink = followers[1];
                 enchantressLink = followers[2];
                 break;
 
             default:
                 break;
         }
 
         List<Item> items = ((EntrySkillAdapter) getListAdapter()).getFollowers();
         List<ParcelUuid> templarSkills = new ArrayList<ParcelUuid>();
         List<ParcelUuid> scoundrelSkills = new ArrayList<ParcelUuid>();
         List<ParcelUuid> enchantressSkills = new ArrayList<ParcelUuid>();
 
         Map<String, List<ParcelUuid>> followersMap = new HashMap<String, List<ParcelUuid>>();
         Map<String, String> followersLinkMap = new HashMap<String, String>();
 
         followersMap.put(Vars.TEMPLAR, templarSkills);
         followersLinkMap.put(Vars.TEMPLAR, templarLink);
 
         followersMap.put(Vars.SCOUNDREL, scoundrelSkills);
         followersLinkMap.put(Vars.SCOUNDREL, scoundrelLink);
 
         followersMap.put(Vars.ENCHANTRESS, enchantressSkills);
         followersLinkMap.put(Vars.ENCHANTRESS, enchantressLink);
 
         for (Item i : items)
         {
             if (i instanceof EmptyFollower)
             {
                 EmptyFollower e = (EmptyFollower) i;
                 String name = e.getName();
                 Follower f = D3Application.getInstance().getFollowerByName(name);
                 List<Integer> levels = f.getRequiredLevels();
 
                 for (int x = 0; x < levels.size(); x++)
                 {
                     List<Skill> skillsForLevel = f.getSkillsByRequiredLevel(levels.get(x).intValue());
 
                     String indicator = String.valueOf(followersLinkMap.get(name).charAt(x));
                     if (indicator.equals("0"))
                     {
                         followersMap.get(name).add(new ParcelUuid(skillsForLevel.get(0).getUuid()));
                     }
                     else if (indicator.equals("1"))
                     {
                         followersMap.get(name).add(new ParcelUuid(skillsForLevel.get(1).getUuid()));
                     }
 
                 }
             }
         }
 
         updateFollowerData(followersMap.get(Vars.TEMPLAR), followersMap.get(Vars.SCOUNDREL), followersMap.get(Vars.ENCHANTRESS), skills);
 
     }
 
     public String getFollowerUrl()
     {
         return followerUrl;
     }
 
     public void delinkifyClassBuild(String url)
     {
 
         Pattern p = Pattern.compile("^http://.*/calculator/(.*)#([a-zA-Z\\.]*)!?([a-zA-Z\\.]*)!?([a-zA-Z\\.]*)$");
         Matcher m = p.matcher(url);
 
         Pattern cap = Pattern.compile("\b([a-z])");
 
         String activeVal = "";
         String passiveVal = "";
         String runeVal = "";
         String tempClass = "";
 
         while (m.find())
         {
             if (m.groupCount() >= 1)
             {
 
                 // Doesn't work :( Trying to capitalize each word with this, but
                 // it's being a bitch.
                 /*
                  * Matcher capital = cap.matcher(tempClass); while
                  * (capital.find()) { if (capital.groupCount() >= 1) { String g
                  * = capital.group(1); tempClass = tempClass.replace("\b" + g,
                  * g.toUpperCase()); } }
                  */
 
                 if (selectedClass.equalsIgnoreCase(m.group(1).replace("-", " ")))
                 {
                     tempClass = m.group(1).replace("-", " ");
                 }
                 else
                 {
                     // Uh-oh!
                 }
             }
 
             if (m.groupCount() >= 2)
             {
                 activeVal = m.group(2);
             }
 
             if (m.groupCount() >= 3)
             {
                 passiveVal = m.group(3);
             }
 
             if (m.groupCount() >= 4)
             {
                 runeVal = m.group(4);
             }
         }
 
         while (activeVal.length() < 6)
         {
             activeVal = activeVal + ".";
         }
 
         while (passiveVal.length() < 3)
         {
             passiveVal = passiveVal + ".";
         }
 
         while (runeVal.length() < 6)
         {
             runeVal = runeVal + ".";
         }
 
         // Reset list
         setListAdapter(getSkillListAdapter(true));
 
         com.wemakestuff.d3builder.model.Class currClass = D3Application.getInstance().getClassByName(selectedClass);
         String[] skillTypes = D3Application.getInstance().getClassAttributesByName(selectedClass).getSkillTypes();
         List<Skill> activeSkills = currClass.getActiveSkills();
         List<Skill> passiveSkills = currClass.getPassiveSkills();
         ArrayList<Item> items = listAdapter.getItems();
 
         SkillAttribute skillAttrbs = D3Application.getInstance().getSkillAttributes();
 
         List<String> skillMapping = Arrays.asList(skillAttrbs.getSkillMapping());
 
         int activeIndex = 0;
         int passiveIndex = 0;
         int listIndex = 0;
 
         ArrayList<Item> tempItems = new ArrayList<Item>();
 
         for (Item item : items)
         {
             if (item instanceof EmptySkill && !((EmptySkill) item).getSkillType().equals("Passive"))
             {
                 if (activeVal.charAt(activeIndex) != skillAttrbs.getMissingValue().charAt(0))
                 {
                     Skill s = activeSkills.get(skillMapping.indexOf(String.valueOf(activeVal.charAt(activeIndex))));
                     tempItems.add(listIndex, new EntrySkill(s));
                     listIndex++;
                 }
                 else
                 {
                     tempItems.add(new EmptySkill("Choose Skill", 1, skillTypes[activeIndex]));
                     listIndex++;
                 }
             }
             else if (item instanceof EmptySkill && ((EmptySkill) item).getSkillType().equals("Passive"))
             {
                 if (passiveVal.charAt(passiveIndex) != skillAttrbs.getMissingValue().charAt(0))
                 {
                     Skill s = passiveSkills.get(skillMapping.indexOf(String.valueOf(passiveVal.charAt(passiveIndex))));
                     tempItems.add(listIndex, new EntrySkill(s));
                     listIndex++;
                 }
                 else
                 {
                     tempItems.add(new EmptySkill("Choose Skill", 1, "Passive"));
                     listIndex++;
                 }
                 passiveIndex++;
             }
             else if (item instanceof EmptyRune)
             {
                 if (activeVal.charAt(activeIndex) != skillAttrbs.getMissingValue().charAt(0))
                 {
                     Skill s = activeSkills.get(skillMapping.indexOf(String.valueOf(activeVal.charAt(activeIndex))));
                     if (runeVal.charAt(activeIndex) == skillAttrbs.getMissingValue().charAt(0))
                     {
                         tempItems.add(listIndex, new EmptyRune("Choose Rune", 1, s.getName(), s.getUuid()));
 
                     }
                     else
                     {
                         Rune r = s.getRunes().get(skillMapping.indexOf(String.valueOf(runeVal.charAt(activeIndex))));
                         tempItems.add(listIndex, new EntryRune(r, s.getName(), s.getUuid()));
                     }
                     listIndex++;
                 }
                 else
                 {
                     // Don't add the rune, since no skill was picked.
                 }
                 activeIndex++;
             }
             else
             {
                 tempItems.add(listIndex, item);
                 listIndex++;
             }
         }
 
         this.items = tempItems;
         listAdapter = new EntrySkillAdapter(context, this.items);
 
         setListAdapter(listAdapter);
     }
 
     public int getFollowerSkillsCount()
     {
 
         List<Item> items = ((EntrySkillAdapter) getListAdapter()).getFollowers();
         int templar = 0;
         int scoundrel = 0;
         int enchantress = 0;
 
         for (Item i : items)
         {
             if (i instanceof EmptyFollower)
             {
                 EmptyFollower e = (EmptyFollower) i;
                 if (e.getName().equals(Vars.TEMPLAR))
                 {
                     templar = e.getSkills().size();
                 }
                 else if (e.getName().equals(Vars.SCOUNDREL))
                 {
                     scoundrel = e.getSkills().size();
                 }
                 else if (e.getName().equals(Vars.ENCHANTRESS))
                 {
                     enchantress = e.getSkills().size();
                 }
             }
         }
 
         return templar + scoundrel + enchantress;
     }
 
 }
