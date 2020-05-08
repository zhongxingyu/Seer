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
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout.LayoutParams;
 import ca.cmput301.team13.taskman.model.BackedObjectCreatedComparator;
 import ca.cmput301.team13.taskman.model.Fulfillment;
 import ca.cmput301.team13.taskman.model.Requirement;
 import ca.cmput301.team13.taskman.model.Requirement.contentType;
 import ca.cmput301.team13.taskman.model.Task;
 
 /**
  * Provides a list of tasks from the {@link VirtualRepository}
  * to list views.
  */
 public class FulfillmentListAdapter implements ListAdapter {
 
     private Task task;
     private ArrayList<DataSetObserver> observers;
     private LayoutInflater inflater;
     private ArrayList<Fulfillment> fulfillments;
 
     /**
      * Used for launching intents for media viewing.
      */
     private Context context;
 
     /**
      * Construct a FulfillmentListAdapter.
      * @param task the task whose fulfillments to list
      * @param context the context of the activity
      */
     public FulfillmentListAdapter(Task task, Context context) {
         this.task = task;
         this.context = context;
         observers = new ArrayList<DataSetObserver>();
         inflater = LayoutInflater.from(context);
         fulfillments = new ArrayList<Fulfillment>();
 
         //Get our initial data
         update();
     }
 
     /**
      * Refresh task list from the local repository.
      */
     public void update() {
     	task = TaskMan.getInstance().getRepository().getTaskUpdate(task);
     	
     	//Clear the list
         fulfillments.clear();
         //Repopulate the list
         Log.w("FulfillmentListAdapter", "In "+task.getRequirementCount()+" requirements:");
         for(int i=0;i<task.getRequirementCount();i++) {
         	Requirement r = task.getRequirement(i);
         	for(int j=0;j<r.getFullfillmentCount();j++) {
         		fulfillments.add(r.getFulfillment(j));
         	}
         }
         Log.w("FulfillmentListAdapter", "Found "+fulfillments.size()+" Fulfillments.");
         //Sort the list
         sortByCreatedDate();
         notifyObservers();
     }
 
     /**
      * Returns the view type of an item at a given index.
      * @param viewIndex index of the item whose view type to return
      */
     public int getItemViewType(int viewIndex) {
         return ((Fulfillment)getItem(viewIndex)).getContentType().ordinal();
     }
 
     /**
      * Returns the {@link View} for an item in the list.
      * @param viewIndex the index of the item
      * @param convertView the old view
      * @param parent the parent view 
      */
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
             	
             } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.video)) {
                 newView = inflater.inflate(R.layout.ful_vid_elem, null);
                 
             } else {
             	Log.w("FulfillmentListAdapter", "Unknown content type");
             	newView = inflater.inflate(R.layout.ful_text_elem, null);
             }
         }
         
         //Setup the attributes of the view
         //TODO: DateFormatter
         DateFormat df = new SimpleDateFormat("'On' MMM dd, yyyy 'at' h:mm");
         ((TextView)newView.findViewById(R.id.fulTime)).setText(df.format(((Fulfillment)getItem(viewIndex)).getCreatedDate()));
         
         if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.text)) {
         	((TextView)newView.findViewById(R.id.ful_text)).setText(
         			((Fulfillment)getItem(viewIndex)).getText());
         	
         } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.image)) {
         	Bitmap b = ((Fulfillment)getItem(viewIndex)).getImage();
         	((ImageView)newView.findViewById(R.id.ful_img)).setImageBitmap(b);
         	((ImageView)newView.findViewById(R.id.ful_img)).setLayoutParams(new LayoutParams(b.getWidth()*90/b.getHeight(), 90));
         	
         } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.audio)) {
         	//ignore audio until list adapter is fixed
             final int index = viewIndex;
             ((ImageButton)newView.findViewById(R.id.audio_play_btn)).setOnClickListener(new OnClickListener() {
                 public void onClick(View source) {
                     view_short_array(((Fulfillment)getItem((index))).getAudio(), contentType.audio);
                 }
             });
             
         	
         } else if(((Fulfillment)getItem(viewIndex)).getContentType().equals(contentType.video)) {
             //ignore video until list adapter is fixed
             final int index = viewIndex;
             ((ImageButton)newView.findViewById(R.id.video_play_btn)).setOnClickListener(new OnClickListener() {
                 public void onClick(View source) {
                     view_short_array(((Fulfillment)getItem((index))).getVideo(), contentType.video);
                 }
             });
         }
         
         return newView;
     }
 
     /**
      * Launch an appropriate viewer for either audio or video content.
      * @param data the fulfillment content as a short array
      * @param ct the content type of the data (either audio or video)
      */
     private void view_short_array(short[] data, contentType ct) {
         // TODO: assert that ct is either audio or video
         assert(ct == contentType.audio || ct == contentType.video);
 
         String audio_ext = "3gp";
         String video_ext = "mp4";
 
         // TODO: check that tmp directory exists.
         File contentfile = new File(
                 Environment.getExternalStorageDirectory().getAbsolutePath()
                 + "/tmp/taskman_fulfillment_content."
                         + (ct == contentType.audio ?
                                 audio_ext : video_ext));
         Uri contentfileuri = Uri.fromFile(contentfile);
 
         try {
             BufferedOutputStream output =
                 new BufferedOutputStream(
                     new FileOutputStream(contentfile));
 
             // Note: the method of writing bytes must be appropriate for the endianness
             // in which the bytes were turned into a short array.
             for(int i = 0; i < data.length; i++) {
                 output.write((byte)(data[i] & 0xff));
                 output.write((byte)((data[i] >> 8) & 0xff));
             }
 
             output.close();
         }
         catch (Exception e) {
             // TODO: Handle this exception
         }
 
         // Launch built-in player
         context.startActivity(
                 (new Intent())
                     .setAction(android.content.Intent.ACTION_VIEW)
                     .setDataAndType(contentfileuri,
                             (ct == contentType.audio ?
                                     "audio/*" : "video/*")));
     }
 
     /**
      * Returns the number of view types.
      */
     public int getViewTypeCount() {
         return Requirement.contentType.values().length;
     }
 
     /**
      * Indicates stable ids.
      */
     public boolean hasStableIds() {
         //Our ids are dependant on array index, which changes on sort.
         return false;
     }
 
     /**
      * Registers a data set observer.
      * Warning: implementation is not Thread-safe.
      */
     public void registerDataSetObserver(DataSetObserver dso) {
         // TODO: This is used to tell the UI that a reload would be a good idea
         observers.add(dso);
     }
 
     /**
      * Unregisters a data set observer.
      * Warning: implementation is not Thread-safe.
      */
     public void unregisterDataSetObserver(DataSetObserver dso) {
         // TODO: This is used to tell the UI that a reload would be a good idea
         observers.remove(dso);
     }
 
     /**
      * Causes items to be sorted by creation date.
      */
     private void sortByCreatedDate() {
         //This as its own method may be unnecessary. THoughts?
         Collections.sort(fulfillments, new BackedObjectCreatedComparator());
     }
 
     /**
      * Notify registered observers of changes.
      */
     private void notifyObservers() {
         for(DataSetObserver dso : observers) {
             dso.onChanged();
         }
     }
 
     /**
      * Returns the number of items in the list.
      */
     public int getCount()        { return fulfillments.size(); }
     /**
      * Returns the item at a given index.
      * @param i index of the item to retrieve
      */
     public Object getItem(int i) { return fulfillments.get(i); }
     /**
      * Returns the id of an item at a given index.
      * @param i the index of the item whose index to return
      */
     public long getItemId(int i) { return i; }
     /**
      * Indicates whether or not the list is empty.
      */
     public boolean isEmpty()     { return fulfillments.isEmpty(); }
 
     /**
      * Indicates all items enabled.
      */
     public boolean areAllItemsEnabled() {
         return false;
     }
 
     /**
      * Indicates whether enabled.
      */
     public boolean isEnabled(int index) {
         return true;
     }
 }
