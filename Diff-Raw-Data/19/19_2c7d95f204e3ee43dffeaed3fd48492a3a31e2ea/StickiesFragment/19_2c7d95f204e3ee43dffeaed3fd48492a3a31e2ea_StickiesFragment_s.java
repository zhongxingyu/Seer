 package com.github.remomueller.tasktracker.android;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import java.lang.Thread;
 import java.lang.InterruptedException;
 
 import android.widget.Toast;
 
 // From libs directory
 import org.apache.commons.io.IOUtils;
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 
 import com.github.remomueller.tasktracker.android.util.Base64;
 
 import com.github.remomueller.tasktracker.android.util.StickiesRequest;
 import com.github.remomueller.tasktracker.android.util.StickiesRequest.StickiesRequestFinishedListener;
 import com.github.remomueller.tasktracker.android.util.DatabaseHandler;
 
 public class StickiesFragment extends SherlockFragment {
     private static final String TAG = "TaskTrackerAndroid";
 
     private int position = -1;
     Project current_project;
     ListView list;
 
     public ArrayList<Sticky> stickies = new ArrayList<Sticky>();
     StickyAdapter stickyAdapter;
 
     // DatabaseHandler db;
 
     public static StickiesFragment newInstance(int location, Project project) {
         StickiesFragment fragment = new StickiesFragment();
         fragment.position = location;
         fragment.current_project = project;
         return fragment;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         this.setRetainInstance(true);
 
         // db = new DatabaseHandler(getActivity());

        // new GetStickies().execute(Integer.toString(position));
 
         Date today = new Date();
 
         Calendar c = Calendar.getInstance();
         c.setTime(today);
         c.add(Calendar.DATE, 1);
         Date tomorrow = c.getTime();
 
         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
         String due_date_today = formatter.format(today);
         String due_date_tomorrow = formatter.format(tomorrow);
         SimpleDateFormat dbFormatter = new SimpleDateFormat("yyyy-MM-dd");
         final String dbToday = dbFormatter.format(today);
         final String dbTomorrow = dbFormatter.format(tomorrow);
 
         StickiesRequestFinishedListener finishedListener = new StickiesRequestFinishedListener()
         {
             @Override
             public void onTaskFinished(ArrayList<Sticky> loadedStickies) {
                 stickies.addAll(loadedStickies);
                 if(stickyAdapter != null) stickyAdapter.notifyDataSetChanged();
             }
         };
 
         String params = "";
 
         if(position == 0) { // Completed
             params = "status[]=completed&order=stickies.due_date+DESC&due_date_end_date="+due_date_today;
         }else if(position == 2) { // Upcoming
             params = "status[]=planned&order=stickies.due_date+ASC&due_date_start_date="+due_date_tomorrow;
         }else{ // Past Due
             params = "status[]=planned&order=stickies.due_date+DESC&due_date_end_date="+due_date_today;
         }
 
         String conditions = "";
 
         if(current_project.id > 0)
             conditions = "project_id = " + current_project.id + " and ";
 
         if(position == 0) { // Completed
             conditions += "completed = 1 and due_date < '" + dbTomorrow + "' ORDER BY due_date DESC";
         } else if(position == 2) { // Upcoming
             conditions += "completed = 0 and due_date >= '" + dbTomorrow + "' ORDER BY due_date ASC";
         } else { // Past Due
             conditions += "completed = 0 and due_date < '" + dbTomorrow + "' ORDER BY due_date DESC";
         }
 
         // TODO: Allow user to set preference from 2. View Control
         // http://developer.android.com/design/patterns/actionbar.html
         if(true)
             params = params + "&owner_id=me";
 
         // Filter by project if project is selected
        if(current_project != null && current_project.id > 0)
             params = params + "&project_id=" + current_project.id;
 
         if(getActivity() != null)
             new StickiesRequest(getActivity().getApplicationContext(), "GET", "/stickies.json", params, conditions, finishedListener).execute();
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.stickies_index, container, false);
         list=(ListView)view.findViewById(R.id.stickies_list);
 
         stickyAdapter = new StickyAdapter(getActivity(), stickies);
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         list.setAdapter(stickyAdapter);
 
         list.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Sticky sticky = stickies.get(position);
                // String text = stickies.get(position).id + ": " + stickies.get(position).description;
 
                 Intent intent = new Intent(getActivity(), StickiesShow.class);
 
                // intent.putExtra(Sticky.STICKY_POSITION, Integer.toString(position));
                 intent.putExtra(Sticky.STICKY_ID, Integer.toString(sticky.id));
                 intent.putExtra(Project.PROJECT_ID, Integer.toString(current_project.id));
                // intent.putExtra(Sticky.STICKY_DESCRIPTION, sticky.description);
                // intent.putExtra(Sticky.STICKY_GROUP_DESCRIPTION, sticky.group_description);
                // intent.putExtra(Sticky.STICKY_DUE_DATE, sticky.due_date);
                // intent.putExtra(Sticky.STICKY_COMPLETED, Boolean.toString(sticky.completed));
 
                 String tag_id = "0";
                 String tag_name = "";
                 String tag_color = "#80FFFFFF";
 
                 for(int i = 0; i < sticky.tags.length; i++){
                     if(i == 0){
                         tag_id = Integer.toString(sticky.tags[i].id);
                         tag_name = sticky.tags[i].name;
                         tag_color = sticky.tags[i].color;
                     }
                 }
 
                 intent.putExtra(Tag.TAG_ID, tag_id);
                 intent.putExtra(Tag.TAG_NAME, tag_name);
                 intent.putExtra(Tag.TAG_COLOR, tag_color);
 
                 startActivity(intent);
             }
         });
     }
 
 }
