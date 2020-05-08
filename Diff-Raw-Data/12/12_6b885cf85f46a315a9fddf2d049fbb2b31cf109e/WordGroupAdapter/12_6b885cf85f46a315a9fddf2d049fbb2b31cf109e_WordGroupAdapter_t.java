 package com.ell.MemoRazor.adapters;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.ell.MemoRazor.App;
 import com.ell.MemoRazor.R;
 import com.ell.MemoRazor.data.WordGroup;
 import com.ell.MemoRazor.helpers.LanguageHelper;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 public class WordGroupAdapter extends ArrayAdapter<WordGroup> {
     private ArrayList<WordGroup> objects;
 
     public WordGroupAdapter(Context context, int resource, ArrayList<WordGroup> objects) {
         super(context, resource, objects);
         this.objects = objects;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.word_group_layout, null);
         }
 
         WordGroup wordGroup = objects.get(position);
 
         if (wordGroup != null) {
             TextView groupNameTextView = (TextView)convertView.findViewById(R.id.group_name_text);
             ImageView imageView = (ImageView)convertView.findViewById(R.id.group_lang_icon);
 
             imageView.setImageResource(LanguageHelper.langCodeToImage(wordGroup.getLanguage()));
             //TextView groupAddedTextView = (TextView)convertView.findViewById(R.id.group_added_text);
 
             //DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            groupNameTextView.setText(String.format("%s (%s)", wordGroup.toString(),
                    wordGroup.getWords() == null ? 0 : wordGroup.getWords().size()));
             //groupAddedTextView.setText(dateFormat.format(wordGroup.getCreatedDate()));
         }
 
         return convertView;
     }
 
 
 
 }
