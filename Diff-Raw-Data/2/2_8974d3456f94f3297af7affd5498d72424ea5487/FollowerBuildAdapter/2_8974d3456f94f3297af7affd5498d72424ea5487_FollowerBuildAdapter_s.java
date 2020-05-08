 
 package com.wemakestuff.d3builder;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.wemakestuff.d3builder.model.ClassBuild;
 import com.wemakestuff.d3builder.string.Replacer;
 import com.wemakestuff.d3builder.string.Vars;
 
 public class FollowerBuildAdapter extends BaseAdapter
 {
 
     private ArrayList<ClassBuild>     items;
     private Context                   context;
     private OnLoadBuildClickInterface clickInterface;
 
     public FollowerBuildAdapter(ArrayList<ClassBuild> items, Context context, OnLoadBuildClickInterface clickInterface)
     {
 
         this.clickInterface = clickInterface;
         this.items = items;
         this.context = context;
     }
 
     @Override
     public int getCount()
     {
 
         return items.size();
     }
 
     @Override
     public ClassBuild getItem(int position)
     {
 
         return items.get(position);
     }
 
     @Override
     public long getItemId(int position)
     {
 
         return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent)
     {
 
         final ClassBuild item = getItem(position);
         LayoutInflater inflater = LayoutInflater.from(context);
         View v = inflater.inflate(R.layout.class_build_item, parent, false);
 
         TextView name = (TextView) v.findViewById(R.id.class_build_name);
         TextView className = (TextView) v.findViewById(R.id.class_build_class);
 
         name.setText(item.getName());
 
         className.setText("Class: " + item.getClassName());
 
         ImageButton delete = (ImageButton) v.findViewById(R.id.class_build_delete);
         ImageButton share = (ImageButton) v.findViewById(R.id.class_build_share);
         ImageButton load = (ImageButton) v.findViewById(R.id.class_build_load);
 
         OnClickListener deleteListener = new OnClickListener()
         {
 
             @Override
             public void onClick(View v)
             {
 
                 SharedPreferences valVals = context.getSharedPreferences("saved_build_value", Context.MODE_PRIVATE);
                 SharedPreferences.Editor valEdit = valVals.edit();
 
                 SharedPreferences clssVals = context.getSharedPreferences("saved_build_class", Context.MODE_PRIVATE);
 
                SharedPreferences followerVals = context.getSharedPreferences("saved_build_follower", Context.MODE_PRIVATE);
                 SharedPreferences.Editor followEdit = followerVals.edit();
 
                 //If class is declared with the same name, don't delete it from here.
                 if (!clssVals.contains(item.getName()))
                     valEdit.remove(item.getName());
                 followEdit.remove(item.getName());
 
                 if (!clssVals.contains(item.getName()))
                     valEdit.commit();
                 followEdit.commit();
                 clickInterface.onDeleteBuild(item);
             }
 
         };
         delete.setOnClickListener(deleteListener);
 
         OnClickListener shareListener = new OnClickListener()
         {
 
             @Override
             public void onClick(View v)
             {
 
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("text/plain");
                 intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out my " + item.getClassName() + " D3 build!");
                 intent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out my " + item.getClassName() + " D3 build: " + item.getUrl());
                 context.startActivity(Intent.createChooser(intent, "Share Using"));
                 clickInterface.onLoadBuildDismiss();
             }
 
         };
         share.setOnClickListener(shareListener);
 
         OnClickListener loadListener = new OnClickListener()
         {
 
             @Override
             public void onClick(View v)
             {
 
                 clickInterface.onLoadBuildClick(item);
                 clickInterface.onLoadBuildDismiss();
             }
 
         };
         load.setOnClickListener(loadListener);
 
         return v;
     }
 }
