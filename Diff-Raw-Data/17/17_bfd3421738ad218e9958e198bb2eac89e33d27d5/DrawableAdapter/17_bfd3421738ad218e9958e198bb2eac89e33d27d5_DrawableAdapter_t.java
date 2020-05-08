 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.adapters;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 public class DrawableAdapter extends ArrayAdapter<Integer> {
     public DrawableAdapter(Context context, List<Integer> objects) {
         super(context, 0, objects);
     }
 
     @Override
     public View getView(int position, View view, ViewGroup parent) {
         ImageView v;
         // only inflate the view if it's null
         if (view == null) {
             v = new ImageView(parent.getContext());
            float scale = getContext().getResources().getDisplayMetrics().density;
            int pixelsIn40Dp = (int) (40 * scale + 0.5f);
            v.setLayoutParams(new GridView.LayoutParams(pixelsIn40Dp, pixelsIn40Dp));
             v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
         } else {
             v = (ImageView) view;
         }
 
         v.setImageResource(getItem(position));
 
         return v;
     }
 }
