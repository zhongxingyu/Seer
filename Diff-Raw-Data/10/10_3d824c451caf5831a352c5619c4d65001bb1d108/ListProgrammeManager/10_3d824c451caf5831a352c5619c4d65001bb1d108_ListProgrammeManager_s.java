 package fr.ybo.ybotv.android.activity;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Parcelable;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import fr.ybo.ybotv.android.adapter.ProgrammeAdapter;
 import fr.ybo.ybotv.android.modele.ChannelWithProgramme;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.util.Chrono;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 public class ListProgrammeManager {
 
     public interface GetProgramme {
         List<ChannelWithProgramme> getProgrammes();
     }
 
     private final GetProgramme getProgramme;
     private final ProgrammeAdapter adapter;
     private List<ChannelWithProgramme> channels = new ArrayList<ChannelWithProgramme>();
     private Context context;
 
     public ListProgrammeManager(AbsListView listView, Activity context, GetProgramme getProgramme) {
         this.getProgramme = getProgramme;
         this.adapter = new ProgrammeAdapter(context, channels);
         this.context = context;
        listView.setAdapter(adapter);
         listView.setTextFilterEnabled(true);
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Programme programme = channels.get(position).getProgramme();
                 Intent intent = new Intent(ListProgrammeManager.this.context, ProgrammeActivity.class);
                 intent.putExtra("programme", (Parcelable)programme);
                 ListProgrammeManager.this.context.startActivity(intent);
             }
         });
         context.registerForContextMenu(listView);
     }
 
     @SuppressWarnings("unchecked")
     public void constructAdapter() {
 
         new AsyncTask<Void, Void, Void>() {
 
             @Override
             protected Void doInBackground(Void... params) {
                 Chrono chrono = new Chrono("GetProgrammes").start();
                 List<ChannelWithProgramme> newChannels = getProgramme.getProgrammes();
                 chrono.stop();
 
                 chrono = new Chrono("Programmes>sort").start();
                 // Sort
                 Collections.sort(newChannels, new Comparator<ChannelWithProgramme>() {
                     @Override
                     public int compare(ChannelWithProgramme channelWithProgramme, ChannelWithProgramme channelWithProgramme1) {
                         int id1 = Integer.parseInt(channelWithProgramme.getChannel().getId());
                         int id2 = Integer.parseInt(channelWithProgramme1.getChannel().getId());
                         if (id1 == id2) {
                             String start1 = channelWithProgramme.getProgramme().getStart();
                             String start2 = channelWithProgramme1.getProgramme().getStart();
                             return start1.compareTo(start2);
                         }
                         return (id1 < id2) ? -1 : 1;
                     }
                 });
                 chrono.stop();
 
                 chrono = new Chrono("Programmes>listUpdate").start();
                 channels.clear();
                 channels.addAll(newChannels);
                 chrono.stop();
                 return null;
             }
 
             @Override
             protected void onPostExecute(Void aVoid) {
                 adapter.notifyDataSetChanged();
                 super.onPostExecute(aVoid);
             }
         }.execute();
 
     }
 }
