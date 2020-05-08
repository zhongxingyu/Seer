 package com.lifecity.felux;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.GradientDrawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.lifecity.felux.scenes.LightScene;
 import com.lifecity.felux.scenes.Scene;
 import com.lifecity.felux.scenes.Scene;
 
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * Created by doceme on 6/6/13.
  */
 public class CueSceneListAdapter extends ArrayAdapter<Scene> implements View.OnClickListener {
     private Context context;
     private int layoutResourceId;
     private List<Scene> scenes;
     private boolean editMode;
     private ItemChangedListener itemChangedListener;
 
     @Override
     public void onClick(View view) {
         CheckBox checkBox = (CheckBox)view;
         Scene scene = (Scene)checkBox.getTag();
         scene.setChecked(checkBox.isChecked());
 
         if (itemChangedListener != null) {
             itemChangedListener.onItemCheckedChanged(scene);
         }
     }
 
     static class SceneHolder {
         CheckBox enable;
         TextView name;
         TextView hold;
         TextView fade;
        LinearLayout fadeLayout;
     }
 
     public CueSceneListAdapter(Context context, int layoutResourceId, List<Scene> scenes) {
         super(context, layoutResourceId, scenes);
         this.context = context;
         this.layoutResourceId = layoutResourceId;
         this.scenes = scenes;
     }
 
     public void setItemChangedListener(ItemChangedListener itemChangedListener) {
         this.itemChangedListener = itemChangedListener;
     }
 
     public void startEditMode() {
         editMode = true;
         notifyDataSetChanged();
     }
 
     public void stopEditMode() {
         editMode = false;
         notifyDataSetChanged();
     }
 
     public void selectAll(boolean select) {
         for (Scene scene: scenes) {
             scene.setChecked(select);
         }
         notifyDataSetChanged();
     }
 
     public int getSingleCheckedItemPosition() {
         int result = -1;
         boolean checked = false;
         for (int i = 0; i < scenes.size(); i++) {
             if (scenes.get(i).getChecked()) {
                 if (checked) {
                     return -1;
                 } else {
                     checked = true;
                     result = i;
                 }
             }
         }
 
         return result;
     }
 
     public boolean areAnyItemsChecked() {
         for (Scene scene: scenes) {
             if (scene.getChecked()) {
                 return true;
             }
         }
         return false;
     }
 
     public void removeChecked() {
         ListIterator<Scene> iterator = scenes.listIterator();
         while (iterator.hasNext()) {
             Scene scene = iterator.next();
             if (scene.getChecked()) {
                 iterator.remove();
             }
         }
         notifyDataSetChanged();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View row = convertView;
         SceneHolder holder = null;
         Scene scene = getItem(position);
 
         if (row == null) {
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             row = inflater.inflate(layoutResourceId, parent, false);
 
             holder = new SceneHolder();
             holder.enable = (CheckBox)row.findViewById(R.id.cue_scene_list_row_checkbox);
             holder.hold = (TextView)row.findViewById(R.id.cue_scene_list_row_hold);
             holder.fade = (TextView)row.findViewById(R.id.cue_scene_list_row_fade);
            holder.fadeLayout = (LinearLayout)row.findViewById(R.id.cue_scene_list_row_fade_layout);
             holder.name = (TextView)row.findViewById(R.id.cue_scene_list_row_name);
 
             holder.enable.setOnClickListener(this);
 
             row.setTag(holder);
 
         } else {
             holder = (SceneHolder)row.getTag();
         }
 
         holder.enable.setTag(scene);
         holder.enable.setVisibility(editMode ? View.VISIBLE : View.GONE);
         holder.enable.setChecked(scene.getChecked());
         holder.name.setText(scene.getName());
 
         holder.hold.setText(String.valueOf(scene.getHold()));
         if (scene instanceof LightScene) {
             LightScene lightScene = (LightScene)scene;
             holder.fade.setText(String.valueOf(lightScene.getFade()));
            //holder.fade.setVisibility(View.VISIBLE);
            holder.fadeLayout.setVisibility(View.VISIBLE);
         } else {
            //holder.fade.setVisibility(View.GONE);
            holder.fadeLayout.setVisibility(View.INVISIBLE);
         }
 
         return row;
     }
 }
