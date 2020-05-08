 package de.bennir.DVBViewerController;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxCallback;
 import com.androidquery.callback.AjaxStatus;
 import de.bennir.DVBViewerController.channels.ChannelListParcelable;
 import de.bennir.DVBViewerController.channels.DVBChannel;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URLDecoder;
 import java.util.ArrayList;
 
 public class ChannelFragment extends SherlockListFragment {
     final String TAG = "ChannelFragment";
     ListView lv;
     ChanGroupAdapter lvAdapter;
     ArrayList<String> groupNames = new ArrayList<String>();
     ArrayList<ArrayList<DVBChannel>> DVBChannels = new ArrayList<ArrayList<DVBChannel>>();
     ArrayList<ChannelListParcelable> chanParcel = new ArrayList<ChannelListParcelable>();
     AQuery aq;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         return inflater.inflate(R.layout.channel_fragment, container, false);
     }
 
     @Override
     public void onResume() {
         Log.d(TAG, "onResume() called");
         super.onResume();
 
         getSherlockActivity().getSupportActionBar().setTitle(R.string.channels);
         updateChannelList();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         Log.d(TAG, "onSaveInstanceState() called");
         super.onSaveInstanceState(outState);
 
         for(ArrayList<DVBChannel> chans : DVBChannels) {
             ChannelListParcelable parc = new ChannelListParcelable();
             parc.channels = chans;
             chanParcel.add(parc);
         }
 
         outState.putParcelableArrayList("chanParcel", chanParcel);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         Log.d(TAG, "onActivityCreated() called");
         super.onActivityCreated(savedInstanceState);
 
         if(savedInstanceState != null) {
             Log.d(TAG, "Restoring state");
 
             chanParcel = savedInstanceState.getParcelableArrayList("chanParcel");
             if(chanParcel != null) {
                 Log.d(TAG, "Restored chanParcel");
                 DVBChannels.clear();
 
                 for(ChannelListParcelable parc : chanParcel) {
                     DVBChannels.add(parc.channels);
                 }
             }
 
         }
 
         setHasOptionsMenu(true);
 
         aq = new AQuery(getSherlockActivity());
 
         lv = getListView();
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
 
                 Fragment newContent = new ChannelGroupFragment();
 
                 if (getActivity() instanceof DVBViewerControllerActivity) {
                     DVBViewerControllerActivity act = (DVBViewerControllerActivity) getActivity();
                     act.switchContent(newContent, groupNames.get(i), R.drawable.ic_action_channels, true);
                 }
             }
         });
 
         if (DVBChannels.isEmpty()) {
             updateChannelList();
         } else {
             lvAdapter = new ChanGroupAdapter(getSherlockActivity(), groupNames.toArray(new String[groupNames.size()]));
         }
     }
 
     private void updateChannelList() {
         Log.d(TAG, "updating channels");
         groupNames.clear();
         DVBChannels.clear();
         chanParcel.clear();
 
         if (DVBViewerControllerActivity.dvbHost == "Demo Device") {
             groupNames.clear();
             DVBChannels.clear();
 
             groupNames.add("ARD");
             ArrayList<DVBChannel> testChans = new ArrayList<DVBChannel>();
             DVBChannel test = new DVBChannel();
 
             test.name = "Das Erste";
             test.group = "ARD";
             testChans.add(test);
             testChans.add(test);
             DVBChannels.add(testChans);
 
             groupNames.add("ZDF");
             testChans = new ArrayList<DVBChannel>();
             test = new DVBChannel();
 
             test.name = "ZDF HD";
             test.group = "ZDF";
             testChans.add(test);
             testChans.add(test);
             DVBChannels.add(testChans);
 
             lvAdapter = new ChanGroupAdapter(getSherlockActivity(), groupNames.toArray(new String[groupNames.size()]));
             lv.setAdapter(lvAdapter);
         } else {
             String url = "http://" +
                     DVBViewerControllerActivity.dvbIp + ":" +
                     DVBViewerControllerActivity.dvbPort +
                     "/?getFavList";
             Log.d(TAG, "URL=" + url);
 
             Style st = new Style.Builder()
                     .setDuration(Style.DURATION_INFINITE)
                     .setBackgroundColorValue(Style.holoBlueLight)
                     .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                     .build();
             Crouton.makeText(getActivity(), R.string.loadingChannels, st).show();
 
             aq.ajax(url, JSONObject.class, this, "downloadChannelCallback");
         }
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         MenuItem item = menu.add(2, 2, 1, "Refresh");
         item.setIcon(R.drawable.ic_action_refresh);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 updateChannelList();
 
                 return true;
             }
         });
     }
 
     public void setChannel(String channelId) {
         if (DVBViewerControllerActivity.dvbHost != "Demo Device") {
             String url = "http://" +
                     DVBViewerControllerActivity.dvbIp + ":" +
                     DVBViewerControllerActivity.dvbPort +
                     "?setChannel=" + channelId;
 
             aq.ajax(url, String.class, new AjaxCallback<String>() {
 
                 @Override
                 public void callback(String url, String html, AjaxStatus status) {
 
                 }
 
             });
         }
     }
 
     public void downloadChannelCallback(String url, JSONObject json, AjaxStatus ajax) {
         Log.d(TAG, "downloadChannelCallback");
         Crouton.cancelAllCroutons();
 
         ArrayList<DVBChannel> dvbChans = new ArrayList<DVBChannel>();
 
         try {
             if (json != null) {
                 Log.d(TAG, "Received answer");
                 JSONArray channelsJSON = new JSONArray(
                         json.getString("channels"));
 
                 String currentGroup = "";
 
                 for (int i = 0; i < channelsJSON.length(); i++) {
                     JSONObject chan = channelsJSON.getJSONObject(i);
 
                     DVBChannel dvbChannel = new DVBChannel();
                     dvbChannel.name = chan.getString("name");
                     dvbChannel.favoriteId = chan.getString("id");
                     dvbChannel.channelId = chan.getString("channelid");
                     dvbChannel.group = chan.getString("group");
                     dvbChannel.epgTitle = URLDecoder.decode(chan.getString("epgtitle"));
                     dvbChannel.epgTime = chan.getString("epgtime");
                     dvbChannel.epgDuration = chan.getString("epgduration");
 
                     String group = chan.getString("group");
                     if (!group.equals(currentGroup)) {
                         if (i > 0) {
                             DVBChannels.add(dvbChans);
                             dvbChans = new ArrayList<DVBChannel>();
                         }
                         groupNames.add(group);
                         currentGroup = group;
 
                         dvbChans.add(dvbChannel);
                     } else {
                         dvbChans.add(dvbChannel);
                     }
                 }
                 DVBChannels.add(dvbChans);
 
                 lvAdapter = new ChanGroupAdapter(getSherlockActivity(), groupNames.toArray(new String[groupNames.size()]));
 
                 lv.setAdapter(lvAdapter);
             }
         } catch (JSONException e) {
             e.printStackTrace();
             System.out.println(e.toString());
         }
     }
 
     public class ChanGroupAdapter extends ArrayAdapter<String> {
         private final Context context;
         private final String[] values;
 
         public ChanGroupAdapter(Context context, String[] values) {
             super(context, R.layout.channels_group_list_item, values);
             this.context = context;
             this.values = values;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             View v = null;
 
             if (convertView != null)
                 v = convertView;
             else
                 v = inflater.inflate(R.layout.channels_group_list_item, parent,
                         false);
 
             TextView chanGroup = (TextView) v
                     .findViewById(R.id.channels_group_list_item);
            chanGroup.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoCondensed);
 
             chanGroup.setText(values[position]);
 
             return v;
         }
     }
 }
