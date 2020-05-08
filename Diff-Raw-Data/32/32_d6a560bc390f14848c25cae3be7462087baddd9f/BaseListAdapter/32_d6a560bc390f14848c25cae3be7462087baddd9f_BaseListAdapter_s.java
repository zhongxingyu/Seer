 package com.zen.droidparts.ui.fragment;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.SparseArray;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 import de.greenrobot.event.EventBus;
 
 /**
  * Created by zen on 10/27/13.
  */
 public class BaseListAdapter<T> extends ArrayAdapter<T> {
     private final CollectionFragment.BaseCell.CellBuilder cellBuilder;
     private final EventBus eventBus;
     private SparseArray<Bundle> states;
 
     public BaseListAdapter(Context context, CollectionFragment.BaseCell.CellBuilder cellBuilder, EventBus bus) {
         super(context, -1);
         this.cellBuilder = cellBuilder;
         this.eventBus = bus;
         this.states = new SparseArray<Bundle>();
     }
 
     @Override
     public void notifyDataSetChanged() {
         this.states.clear();
         super.notifyDataSetChanged();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         CollectionFragment.BaseCell cell = (CollectionFragment.BaseCell) convertView;
         T item = getItem(position);
 
         if (cell == null) {
             cell = cellBuilder.build(getContext(), item);
             cell.setEventBus(getEventBus());
         }
 
         Bundle b = new Bundle();
 
         cell.saveState(b);
 
        saveState(b, position);
 
         cell.prepareForReuse();
         cell.fillWithItem(item);
         Bundle state = getState(position);
 
         if (state != null) {
             cell.restoreState(getState(position));
         }
 
         return (View) cell;
     }
 
     private Bundle getState(int position) {
         return this.states.get(position);
     }
 
     private void saveState(Bundle state, int position) {
         this.states.put(position, state);
     }
 
     public EventBus getEventBus() {
         return eventBus;
     }
 }
