 /*
  * This file is part of TaskMan
  *
  * Copyright (C) 2012 Jed Barlow, Mark Galloway, Taylor Lloyd, Braeden Petruk
  *
  * TaskMan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * TaskMan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TaskMan.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.cmput301.team13.taskman;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.MediaController;
 import android.widget.TextView;
 import ca.cmput301.team13.taskman.model.BackedObjectCreatedComparator;
 import ca.cmput301.team13.taskman.model.Fulfillment;
 import ca.cmput301.team13.taskman.model.Requirement;
 import ca.cmput301.team13.taskman.model.Requirement.contentType;
 import ca.cmput301.team13.taskman.model.Task;
 import ca.cmput301.team13.taskman.model.TaskFilter;
 import ca.cmput301.team13.taskman.model.VirtualRepository;
 
 /**
  * Provides a list of tasks from the virtual repo to list views
  */
 public class FulfillmentListAdapter implements ListAdapter {
 
     private Task task;
     private ArrayList<DataSetObserver> observers;
     private LayoutInflater inflater;
     private ArrayList<Fulfillment> fulfillments;
 
     //View types
     enum viewType {
         Header,
         Task
     };
 
     /**
      * Construct a TaskListAdapter
      * @param vr The virtual repository instance
      */
     public FulfillmentListAdapter(Task task, Context context) {
         this.task = task;
         observers = new ArrayList<DataSetObserver>();
         inflater = LayoutInflater.from(context);
         fulfillments = new ArrayList<Fulfillment>();
 
         //Get our initial data
         update();
     }
 
     /**
      * Refresh task list from the local repo.
      */
     public void update() {
    	task = TaskMan.getInstance().getRepository().getTaskUpdate(task);
    	
     	//Clear the list
         fulfillments.clear();
         //Repopulate the list
        for(int i=0;i<task.getRequirementCount()-1;i++) {
         	Requirement r = task.getRequirement(i);
         	for(int j=0;j<r.getFullfillmentCount();j++) {
         		fulfillments.add(r.getFulfillment(j));
         	}
         }
         //Sort the list
         sortByCreatedDate();
         notifyObservers();
     }
 
     public int getItemViewType(int viewIndex) {
         return ((Fulfillment)getItem(viewIndex)).getContentType().ordinal();
     }
 
     public View getView(int viewIndex, View convertView, ViewGroup parent) {
         View newView;
         if(convertView != null) {
             //Re-use the given view
             newView = convertView;
         } else {
             //Instantiate a new view
             if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.text)) {
             	newView = inflater.inflate(R.layout.ful_text_elem, null);
             	
             } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.image)) {
             	newView = inflater.inflate(R.layout.ful_img_elem, null);
             	
             } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.audio)) {
             	newView = inflater.inflate(R.layout.ful_aud_elem, null);
             	
             } else {
             	Log.w("FulfillmentListAdapter", "Unknown content type");
             	newView = inflater.inflate(R.layout.ful_text_elem, null);
             }
         }
         
         //Setup the attributes of the view
         //TODO: DateFormatter
         ((TextView)newView.findViewById(R.id.fulTime)).setText("On ----:");
         
         if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.text)) {
         	((TextView)newView.findViewById(R.id.ful_text)).setText(
         			((Fulfillment)getItem(viewIndex)).getText());
         	
         } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.image)) {
         	((ImageView)newView.findViewById(R.id.ful_img)).setImageBitmap(
         			((Fulfillment)getItem(viewIndex)).getImage());
         	
         } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.audio)) {
         	//Ignore Audio for now
         	//((MediaController)newView.findViewById(R.id.ful_audio)).set
         	
         }
         
         return newView;
     }
 
     public int getViewTypeCount() {
         return viewType.values().length;
     }
 
     public boolean hasStableIds() {
         //Our ids are dependant on array index, which changes on sort.
         return false;
     }
 
     /**
      * Implementation is not Thread-safe.
      */
     public void registerDataSetObserver(DataSetObserver dso) {
         // TODO: This is used to tell the UI that a reload would be a good idea
         observers.add(dso);
     }
 
     /**
      * Implementation is not Thread-safe
      */
     public void unregisterDataSetObserver(DataSetObserver dso) {
         // TODO: This is used to tell the UI that a reload would be a good idea
         observers.remove(dso);
     }
 
     private void sortByCreatedDate() {
         //This as its own method may be unnecessary. THoughts?
         Collections.sort(fulfillments, new BackedObjectCreatedComparator());
     }
 
     private void notifyObservers() {
         for(DataSetObserver dso : observers) {
             dso.onChanged();
         }
     }
 
     public int getCount()        { return fulfillments.size(); }
     public Object getItem(int i) { return fulfillments.get(i); }
     public long getItemId(int i) { return i; }
     public boolean isEmpty()     { return fulfillments.isEmpty(); }
 
     public boolean areAllItemsEnabled() {
         return false;
     }
 
     public boolean isEnabled(int index) {
         return true;
     }
 }
