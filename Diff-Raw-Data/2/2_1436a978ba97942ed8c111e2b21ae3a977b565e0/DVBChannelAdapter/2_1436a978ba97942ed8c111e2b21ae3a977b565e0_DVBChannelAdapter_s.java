 package de.bennir.DVBViewerController.channels;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.androidquery.AQuery;
 import de.bennir.DVBViewerController.DVBViewerControllerActivity;
 import de.bennir.DVBViewerController.R;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class DVBChannelAdapter extends ArrayAdapter<DVBChannel> {
     private static final String TAG = DVBChannelAdapter.class.toString();
     private ArrayList<DVBChannel> chans;
     private Context context;
 
     public DVBChannelAdapter(Context context, ArrayList<DVBChannel> dvbChans) {
         super(context, R.layout.channels_channel_list_item, dvbChans);
         this.chans = dvbChans;
         this.context = context;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         View v;
 
         if (convertView != null)
             v = convertView;
         else
             v = inflater.inflate(R.layout.channels_channel_list_item, parent,
                     false);
 
         AQuery aq = new AQuery(v);
 
         ((TextView) v.findViewById(R.id.channel_item_name)).setText(chans.get(position).name);
         ((TextView) v.findViewById(R.id.channel_item_current_epg)).setText(chans.get(position).epgInfo.time + " - " + chans.get(position).epgInfo.title);
         ((TextView) v.findViewById(R.id.channel_item_favid)).setText(chans.get(position).favoriteId);
 
         /**
          * Duration Progress
          */
         SimpleDateFormat format = new SimpleDateFormat("HH:mm");
         String curTime = format.format(new Date());
         String startTime = chans.get(position).epgInfo.time;
         String duration = chans.get(position).epgInfo.duration;
 
         Date curDate;
         Date startDate;
         Date durDate = new Date();
         long diff = 0;
 
         try {
             curDate = format.parse(curTime);
             startDate = format.parse(startTime);
             durDate = format.parse(duration);
 
             diff = curDate.getTime() - startDate.getTime();
         } catch (ParseException ex) {
             ex.printStackTrace();
         }
 
         double elapsed = (diff / 1000 / 60);
         long durMinutes = (durDate.getHours() * 60 + durDate.getMinutes());
 
         ProgressBar progress = (ProgressBar) v.findViewById(R.id.channel_item_progress);
         progress.setProgress(Double.valueOf((elapsed / durMinutes * 100)).intValue());
 
         if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             String url = "";
             try {
                 url = "http://" +
                         DVBViewerControllerActivity.dvbIp + ":" +
                         DVBViewerControllerActivity.dvbPort +
                         "/?getChannelLogo=" + URLEncoder.encode(chans.get(position).name, "UTF-8");
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
 
            aq.id(R.id.channel_item_logo).image(url, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
         }
         return v;
     }
 
 
 }
