 package com.sianware.surgtech;
 
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 
public class ReviewSectionFragment extends Fragment
 {
     SimpleAdapter listAdapter;
     final ArrayList<HashMap<String,String>> listItems = new ArrayList<HashMap<String,String>>();
     final ArrayList<String> directoryNames = new ArrayList<String>();
     final static String SECTION_NAME = "sn";

 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
 	{
         View view = inflater.inflate(R.layout.review_list, container, false);
         final Context context = inflater.getContext();
 
         listAdapter = new SimpleAdapter(context, listItems, R.layout.item_section,
                 new String[]{SECTION_NAME}, new int[]{R.id.sectionName});
         //listAdapter = new SimpleAdapter(context, listItems, R.layout.item_section,
         //        new String[]{}, new int[]{});
 
         AssetManager assetManager = context.getAssets();
         //Esablishes string to be refresnced for our files.
        String[] assetDirs = null;
         try {
             assetDirs = assetManager.list("sections");
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 
 
 
         for(int i = 0; i < assetDirs.length; i++)
         {
             HashMap<String,String> item = new HashMap<String,String>();
             String dirName = assetDirs[i];
 
             String sectionName = "";
             try {
                 sectionName += streamToString(context.getAssets().open("sections/" + dirName + "/manifest.txt"));
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             Log.d("###FILENAME###", sectionName);
 
             item.put(SECTION_NAME, sectionName);
             listItems.add(item);
             directoryNames.add(dirName);
         }
 

        ListView listView = (ListView)view.findViewById(R.id.sectionList);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                Toast.makeText(context, "Hello?", 1000);
            }

        });
 
         listAdapter.notifyDataSetChanged();
 
         return view;
     }
 
     private static String streamToString(InputStream stream)
     {
         Scanner s = new Scanner(stream).useDelimiter("\\A");
         String toReturn = s.hasNext() ? s.next() : "";
         toReturn = toReturn.replace("\n","").replace("\n","");
         return toReturn;
     }
 }
