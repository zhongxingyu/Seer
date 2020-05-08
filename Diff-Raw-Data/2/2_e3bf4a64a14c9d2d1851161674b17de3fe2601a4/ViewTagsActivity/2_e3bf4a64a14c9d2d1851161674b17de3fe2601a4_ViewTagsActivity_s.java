 package com.cs301w01.meatload.activities;
 
 import java.util.ArrayList;
 
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.adapters.AlbumAdapter;
 import com.cs301w01.meatload.adapters.TagAdapter;
 import com.cs301w01.meatload.controllers.MainManager;
 import com.cs301w01.meatload.model.Album;
 import com.cs301w01.meatload.model.Tag;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 /**
  * Implements the logic in the Tags view of the Tab layout in Skindex. 
  * @author Joel Burford
  */
 public class ViewTagsActivity extends Skindactivity {
 
 	private MainManager mainManager;
 	private TagAdapter adapter;
 	
 	//arraylist needed for second listview
 	private ArrayList<Tag> selectedTags;
 	
 	//list views
 	private ListView allTagsLV;
 	private ListView selectedTagsLV;
 	
 	//auto complete view
 	private AutoCompleteTextView searchField;
 	
 	//current picture count view
 	private TextView pictureCount;
 	
     //@Override
     public void update(Object model) {
         
     }
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.viewtags);
         
         mainManager = new MainManager();
         mainManager.setContext(this);
         
         selectedTags = new ArrayList<Tag>();
         
         createListeners();
         refreshScreen();
     }
     
     protected void createListeners() {
         
        final Button viewPicturesButton = (Button) findViewById(R.id.viewSelectections);
         viewPicturesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	
             }
         });
         
         allTagsLV.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 View temp = v;
                 allTagsLV.removeView(temp);
                 selectedTagsLV.addView(temp);
             }
         });
         
         //tagListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
     }
     
     public void refreshScreen() {
     	
     	//set top list
     	allTagsLV= (ListView) findViewById(R.id.tagListView);
 		ArrayList<Tag> tagList = mainManager.getAllTags();
 		adapter = new TagAdapter(this, R.layout.list_item, tagList);
 		allTagsLV.setAdapter(adapter);
 		
 		//create bottom list
 		selectedTagsLV = (ListView) findViewById(R.id.selectedTagsListView);
 		TagAdapter sTadapter = new TagAdapter(this, R.layout.list_item, selectedTags);
 		selectedTagsLV.setAdapter(sTadapter);
 		
 		
     }
 
 }
