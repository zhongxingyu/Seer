 package com.ell.MemoRazor.adapters;
 
 import android.content.Context;
 import android.view.*;
 import android.widget.*;
 import com.ell.MemoRazor.R;
 import com.ell.MemoRazor.data.WordGroup;
 import com.ell.MemoRazor.helpers.LanguageHelper;
 
 import java.util.ArrayList;
 
 public class WordGroupSelectionAdapter extends ArrayAdapter<WordGroup> {
     private ArrayList<WordGroup> selectedObjects;
     private ArrayList<WordGroup> objects;
     private MenuItem menuItem;
 
     CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
             if (b) {
                 selectedObjects.add((WordGroup)compoundButton.getTag());
             } else {
                 selectedObjects.remove(compoundButton.getTag());
             }
 
             if (menuItem != null) {
                 menuItem.setEnabled(selectedObjects.size() != 0);
             }
         }
     };
 
     public ArrayList<WordGroup> getSelectedWordGroups() {
         return selectedObjects;
     }
 
     public void setMenuItem(MenuItem menuItem) {
         this.menuItem = menuItem;
     }
 
     public WordGroupSelectionAdapter(Context context, int resource, ArrayList<WordGroup> objects) {
         super(context, resource, objects);
         this.objects = objects;
         this.selectedObjects = new ArrayList<WordGroup>();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.word_group_selection_layout, null);
         }
 
         WordGroup wordGroup = objects.get(position);
 
         if (wordGroup != null) {
             TextView groupName = (TextView)convertView.findViewById(R.id.selection_group_name);
             CheckBox selectionGroupCheckbox = (CheckBox)convertView.findViewById(R.id.selection_group_checkbox);
             ImageView imageView = (ImageView)convertView.findViewById(R.id.group_selection_lang_icon);
             imageView.setImageResource(LanguageHelper.langCodeToImage(wordGroup.getLanguage()));
 
            groupName.setText(String.format("%s (%s)", wordGroup.toString(),
                    wordGroup.getWords() == null ? 0 : wordGroup.getWords().size()));
             selectionGroupCheckbox.setTag(wordGroup);
             selectionGroupCheckbox.setOnCheckedChangeListener(checkListener);
         }
 
         return convertView;
     }
 }
