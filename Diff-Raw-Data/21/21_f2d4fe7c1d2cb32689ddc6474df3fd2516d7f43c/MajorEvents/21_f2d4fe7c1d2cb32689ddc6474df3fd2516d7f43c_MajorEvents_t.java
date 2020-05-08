 package k11.guildwars2eventviewer.Activities;
 
 import android.R;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import k11.guildwars2eventviewer.Callbacks.EventListCallback;
 import k11.guildwars2eventviewer.DataClasses.Event;
 import k11.guildwars2eventviewer.EventViewerApplication;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by bay on 5/23/13.
  */
 public class MajorEvents extends ListActivity {
     private Handler mHandler;
     static private ArrayList<Event> eventList;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         final EventViewerApplication application = (EventViewerApplication) getApplication();
         final Bundle bundle = getIntent().getExtras();
 
         //final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
         refresh();
 
         this.mHandler = new Handler();
         this.mHandler.postDelayed(m_Runnable,application.getRefresh() * 1000);
     }
 
     private final Runnable m_Runnable = new Runnable()
     {
         public void run()
 
         {
             refresh();
             final EventViewerApplication application = (EventViewerApplication) getApplication();
             mHandler.postDelayed(m_Runnable, application.getRefresh() * 1000);
         }
 
     };
 
     @Override
     protected void onListItemClick(ListView listView, View view, int position, long id) {
         //MapName mapName = (MapName) getListAdapter().getItem(position);
         //String url = ((Event)getListAdapter().getItem(position)).getEventName()
         final EventViewerApplication application = (EventViewerApplication) getApplication();
         Event event = (Event) getListAdapter().getItem(position);
         String eventName = event.getEventName(application).getName();
         eventName = eventName.replace(" ","+");
         if (eventName.endsWith(".")) {
             eventName = eventName.substring(0,eventName.length() - 1);
         }
         String url = "http://wiki.guildwars2.com/index.php?title=Special%3ASearch&search=" + eventName;
         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
         startActivity(browserIntent);
     }
 
     private class EventAdapter extends ArrayAdapter<Event> {
         private int textViewResourceId;
         private LayoutInflater layoutInflater;
         public EventAdapter(Context context, int textViewResourceId,List<Event> objects) {
             super(context, textViewResourceId, objects);
             textViewResourceId = this.textViewResourceId;
             layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         @Override
         public View getView (int position, View convertView, ViewGroup parent) {
             View view = convertView;
             if (convertView == null) {
                 view = layoutInflater.inflate(R.layout.simple_list_item_1, null);
             }
 
             final EventViewerApplication application = (EventViewerApplication) getApplication();
 
             TextView textView = (TextView) view.findViewById(android.R.id.text1);
             textView.setText(getItem(position).getEventName(application).getName());
             if (getItem(position).getState().equals(Event.PREPARATION)) {
                 textView.setBackgroundColor(Color.argb(64, 255, 255, 128)); //Yellow
             }
             else {
                 textView.setBackgroundColor(Color.argb(64, 255, 128, 128)); //Red
             }
 
             String currentEventID = getItem(position).getEventName(application).getId();
 
             String chestEvent;
             ArrayList<String> prereqList;
 
             chestEvent = "31CEBA08-E44D-472F-81B0-7143D73797F5";
             prereqList = new ArrayList<String>();
             prereqList.add("CFBC4A8C-2917-478A-9063-1A8B43CC8C38");
             prereqList.add("36330140-7A61-4708-99EB-010B10420E39");
             prereqList.add("AFCF031A-F71D-4CEA-85E1-957179414B25");
             prereqList.add("E539A5E3-A33B-4D5F-AEED-197D2716F79B");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "33F76E9E-0BB6-46D0-A3A9-BE4CDFC4A3A4";
             prereqList = new ArrayList<String>();
             prereqList.add("2C833C11-5CD5-4D96-A4CE-A74C04C9A278");
             prereqList.add("5E4E9CD9-DD7C-49DB-8392-C99E1EF4E7DF");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "C5972F64-B894-45B4-BC31-2DEEA6B7C033";
             prereqList = new ArrayList<String>();
             prereqList.add("613A7660-8F3A-4897-8FAC-8747C12E42F8");
             prereqList.add("1DCFE4AA-A2BD-44AC-8655-BBD508C505D1");
             prereqList.add("456DD563-9FDA-4411-B8C7-4525F0AC4A6F");
             prereqList.add("61BA7299-6213-4569-948B-864100F35E16");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "03BF176A-D59F-49CA-A311-39FC6F533F2F";
             prereqList = new ArrayList<String>();
             prereqList.add("580A44EE-BAED-429A-B8BE-907A18E36189");
             prereqList.add("8E064416-64B5-4749-B9E2-31971AB41783");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "9AA133DC-F630-4A0E-BB5D-EE34A2B306C2";
             prereqList = new ArrayList<String>();
             prereqList.add("3ED4FEB4-A976-4597-94E8-8BFD9053522F");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "0464CB9E-1848-4AAA-BA31-4779A959DD71";
             prereqList = new ArrayList<String>();
             prereqList.add("0CA3A7E3-5F66-4651-B0CB-C45D3F0CAD95");
             prereqList.add("BFD87D5B-6419-4637-AFC5-35357932AD2C");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "A5B5C2AF-22B1-4619-884D-F231A0EE0877";
             prereqList = new ArrayList<String>();
             prereqList.add("7E24F244-52AF-49D8-A1D7-8A1EE18265E0");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "99254BA6-F5AE-4B07-91F1-61A9E7C51A51";
             prereqList = new ArrayList<String>();
             prereqList.add("E16113B1-CE68-45BB-9C24-91523A663BCB");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "6A6FD312-E75C-4ABF-8EA1-7AE31E469ABA";
             prereqList = new ArrayList<String>();
             prereqList.add("526732A0-E7F2-4E7E-84C9-7CDED1962000");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "0372874E-59B7-4A8F-B535-2CF57B8E67E4";
             prereqList = new ArrayList<String>();
             prereqList.add("F66922B5-B4BD-461F-8EC5-03327BD2B558");
             prereqList.add("590364E0-0053-4933-945E-21D396B10B20");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "2555EFCB-2927-4589-AB61-1957D9CC70C8";
             prereqList = new ArrayList<String>();
             prereqList.add("D0ECDACE-41F8-46BD-BB17-8762EF29868C");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "F7D9D427-5E54-4F12-977A-9809B23FBA99";
             prereqList = new ArrayList<String>();
             prereqList.add("374FC8CB-7AB7-4381-AC71-14BFB30D3019");
             prereqList.add("DB83ABB7-E5FE-4ACB-8916-9876B87D300D");
             prereqList.add("90B241F5-9E59-46E8-B608-2507F8810E00");
             prereqList.add("6565EFD4-6E37-4C26-A3EA-F47B368C866D");
             prereqList.add("D5F31E0B-E0E3-42E3-87EC-337B3037F437");
             prereqList.add("6F516B2C-BD87-41A9-9197-A209538BB9DF");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "4B478454-8CD2-4B44-808C-A35918FA86AA";
             prereqList = new ArrayList<String>();
             prereqList.add("8D45B410-B614-4008-8A5C-E8D8230CEB40");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "E6872A86-E434-4FC1-B803-89921FF0F6D6";
             prereqList = new ArrayList<String>();
             prereqList.add("4A1DECF3-C1AD-42EC-9905-976B281CFA49");
             prereqList.add("AE7AAA0C-5619-4C94-918B-6022DB9AA481");
             prereqList.add("C3A1BAE2-E7F2-4929-A3AA-92D39283722C");
             prereqList.add("DDC0A526-A239-4791-8984-E7396525B648");
             prereqList.add("A3101CDC-A4A0-4726-85C0-147EF8463A50");
             prereqList.add("DA465AE1-4D89-4972-AD66-A9BE3C5A1823");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "95CA969B-0CC6-4604-B166-DBCCE125864F";
             prereqList = new ArrayList<String>();
             prereqList.add("F1F99810-D6A9-4263-A5BC-4257C5B7AD0D");
             prereqList.add("07536BE1-9796-4D40-A203-29B4FE270E64");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "242BD241-E360-48F1-A8D9-57180E146789";
             prereqList = new ArrayList<String>();
             prereqList.add("D682ABC2-6B73-4C8E-A246-E9C23ED99153");
             prereqList.add("B6B7EE2A-AD6E-451B-9FE5-D5B0AD125BB2");
             prereqList.add("189E7ABE-1413-4F47-858E-4612D40BF711");
             prereqList.add("0E0801AF-28CF-4FF7-8064-BB2F4A816D23");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
             chestEvent = "A0796EC5-191D-4389-9C09-E48829D1FDB2";
             prereqList = new ArrayList<String>();
             prereqList.add("42884028-C274-4DFA-A493-E750B8E1B353");
             markPrereqs(currentEventID,chestEvent,prereqList,application,textView);
 
 
             if (getItem(position).getState().equals(Event.ACTIVE)) {
                 textView.setBackgroundColor(Color.TRANSPARENT);
             }
 
             return view;
         }
     }
 
     private void markPrereqs(String currentID,String eventID,ArrayList<String> prereqList, EventViewerApplication application, TextView textView) {
         if (currentID.equals(eventID)) {
             Integer size = prereqList.size();
             for (int i=0;i<size;i++) {
                try {
                    Event prereqEvent = Event.findEvent(eventList,prereqList.get(i),application);
                    if (prereqEvent.getState().equals(Event.ACTIVE) || prereqEvent.getState().equals(Event.PREPARATION)) {
                        textView.setBackgroundColor(Color.argb(64, 255, 255, 128)); //Yellow
                    }
                }
                catch (Exception e) {
                    //Do nothing
                 }
             }
         }
     }
 
     private void refresh() {
         final EventViewerApplication application = (EventViewerApplication) getApplication();
 
         Event.readList(application,application.getHomeWorld().getId(),null,null,new EventListCallback() {
             @Override
             public void success(ArrayList<Event> eventList) {
                 ArrayList<Event> displayEventList = new ArrayList<Event>();
                 Integer size = eventList.size();
                 for (int i=0;i<size;i++) {
                     String currentEventID = eventList.get(i).getEventName(application).getId();
                     if (currentEventID.equals("31CEBA08-E44D-472F-81B0-7143D73797F5") || currentEventID.equals("33F76E9E-0BB6-46D0-A3A9-BE4CDFC4A3A4") || currentEventID.equals("C5972F64-B894-45B4-BC31-2DEEA6B7C033") || currentEventID.equals("03BF176A-D59F-49CA-A311-39FC6F533F2F") || currentEventID.equals("568A30CF-8512-462F-9D67-647D69BEFAED") || currentEventID.equals("9AA133DC-F630-4A0E-BB5D-EE34A2B306C2") || currentEventID.equals("0464CB9E-1848-4AAA-BA31-4779A959DD71") || currentEventID.equals("A5B5C2AF-22B1-4619-884D-F231A0EE0877") || currentEventID.equals("99254BA6-F5AE-4B07-91F1-61A9E7C51A51") || currentEventID.equals("6A6FD312-E75C-4ABF-8EA1-7AE31E469ABA") || currentEventID.equals("0372874E-59B7-4A8F-B535-2CF57B8E67E4") || currentEventID.equals("2555EFCB-2927-4589-AB61-1957D9CC70C8") || currentEventID.equals("F7D9D427-5E54-4F12-977A-9809B23FBA99") || currentEventID.equals("4B478454-8CD2-4B44-808C-A35918FA86AA") || currentEventID.equals("E6872A86-E434-4FC1-B803-89921FF0F6D6") || currentEventID.equals("95CA969B-0CC6-4604-B166-DBCCE125864F") || currentEventID.equals("242BD241-E360-48F1-A8D9-57180E146789") || currentEventID.equals("295E8D3B-8823-4960-A627-23E07575ED96") || currentEventID.equals("A0796EC5-191D-4389-9C09-E48829D1FDB2")) {
                         displayEventList.add(eventList.get(i));
                     }
                 }
                 MajorEvents.eventList = eventList;
                 EventAdapter adapter = new EventAdapter(MajorEvents.this,android.R.layout.simple_list_item_1,displayEventList);
                 setListAdapter(adapter);
                 //dialog.dismiss();
             }
 
             @Override
             public void failure(Exception e) {
                 Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
                 e.printStackTrace();
                 //dialog.dismiss();
             }
         });
 
     }
 }
