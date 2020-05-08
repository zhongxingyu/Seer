 package fr.ybo.ybotv.android.adapter;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.activity.ListProgrammeManager;
 import fr.ybo.ybotv.android.activity.ProgrammeActivity;
 import fr.ybo.ybotv.android.modele.Channel;
 import fr.ybo.ybotv.android.modele.ChannelWithProgramme;
 import fr.ybo.ybotv.android.modele.Programme;
 import org.taptwo.android.widget.TitleProvider;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class ParChaineViewFlowAdapter extends BaseAdapter implements TitleProvider {
 
 
     private LayoutInflater inflater;
     private Activity context;
 
     private List<Channel> channels;
 
     public ParChaineViewFlowAdapter(Activity context) {
         this.inflater = LayoutInflater.from(context);
         this.context = context;
         this.channels = ((YboTvApplication) context.getApplication()).getDatabase().selectAll(Channel.class);
     }
 
     @Override
     public int getCount() {
         return channels.size();
     }
 
     @Override
     public Channel getItem(int position) {
         return channels.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     public static class ProgrammeOnItemClickListener implements AdapterView.OnItemClickListener {
 
         private List<Programme> programmes;
         private Activity activity;
 
         public ProgrammeOnItemClickListener(List<Programme> programmes, Activity activity) {
             this.programmes = programmes;
             this.activity = activity;
         }
 
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             Programme programme = programmes.get(position);
             Intent intent = new Intent(activity, ProgrammeActivity.class);
             intent.putExtra("programme", (Parcelable)programme);
             activity.startActivity(intent);
         }
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.list, null);
         }
 
         ListView listView = (ListView) convertView.findViewById(android.R.id.list);
         List<Programme> programmes = Programme.getProgrammes((YboTvApplication) context.getApplication(), getItem(position));
         listView.setAdapter(new ProgrammeByChaineAdapter(context, programmes));
         listView.setTextFilterEnabled(true);
         listView.setOnItemClickListener(new ProgrammeOnItemClickListener(programmes, context));
         context.registerForContextMenu(listView);
 
         return convertView;
     }
 
 
     /* (non-Javadoc)
     * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
     */
     @Override
     public String getTitle(int position) {
         return getItem(position).getDisplayName();
     }
 }
