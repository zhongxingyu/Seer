 package pl.warsjawa.android2.ui.list;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 
 import com.google.android.gms.internal.p;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pl.warsjawa.android2.model.gmapsapi.nearby.NearbyPlace;
 import pl.warsjawa.android2.model.gmapsapi.nearby.NearbyPlacesList;
 import pl.warsjawa.android2.model.meetup.Event;
 
 /**
 * Created by krzysztofsiejkowski on 10/6/13.
 */
 class MeetupMergeAdapter extends BaseAdapter {
 
    private boolean expanded = false;
     private List<ItemAdapter<?>> data = new ArrayList<ItemAdapter<?>>(1);
     private LayoutInflater inflater;
     private static final int TYPE_COUNT = 3;
 
     MeetupMergeAdapter(LayoutInflater inflater, Object obj) {
         this.inflater = inflater;
         if (data.isEmpty()) {
             data.add(new MeetupEventAdapter(null));
         }
         updateData(obj);
     }
 
     public void updateData(Object obj) {
         ItemAdapter<?> retain = data.get(0);
         data.clear();
         data.add(retain);
 
         if (obj instanceof Event) {
 
             data.set(0, new MeetupEventAdapter((Event) obj));
 
         } else if (obj instanceof NearbyPlacesList) {
 
             for (NearbyPlace nearbyPlace : ((NearbyPlacesList) obj).getResults()) {
                 data.add(new MeetupNearbyPlaceAdapter(nearbyPlace));
             }
 
         } else if (obj == null) {
 
             data.add(new MeetupLoadingAdapter());
 
         } else throw new IllegalArgumentException();
     }
 
     @Override
     public int getCount() {
         return (expanded) ? data.size() : 1;
     }
 
     @Override
     public Object getItem(int position) {
         return data.get(position).getItem();
     }
 
     @Override
     public long getItemId(int position) {
         return data.get(position).getItemId();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         return data.get(position).getView(inflater, convertView, parent);
     }
 
     @Override
     public int getViewTypeCount() {
         return TYPE_COUNT;
     }
 
     @Override
     public int getItemViewType(int position) {
         return data.get(position).getItemViewType();
     }
 
     @Override
     public boolean areAllItemsEnabled() {
         return false;
     }
 
     @Override
     public boolean isEnabled(int position) {
         return data.get(position).isEnabled();
     }
 
     public void toogleExpand() {
         expanded = !expanded;
     }
 }
