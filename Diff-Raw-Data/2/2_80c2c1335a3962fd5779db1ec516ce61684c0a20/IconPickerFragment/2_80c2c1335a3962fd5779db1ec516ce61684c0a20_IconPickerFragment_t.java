 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.fragments;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.alexjf.tmm.R;
 import net.alexjf.tmm.adapters.DrawableAdapter;
 import net.alexjf.tmm.utils.Utils;
 
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.Spinner;
 
 public class IconPickerFragment extends DialogFragment {
     private static String KEY_CURRENTFILTER = "currentFilter";
 
     private OnIconPickedListener listener;
     private String filter;
     private ArrayAdapter<String> catAdapter;
     private DrawableAdapter gridAdapter;
     private List<IconCategory> iconCategories;
 
     private Spinner categorySpinner;
 
     @Override
     public void onResume() {
         super.onResume();
 
         int pixelsIn300Dp = Utils.displayPixelsToPixels(getActivity(), 300);
         getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, pixelsIn300Dp);
     }
 
     public interface OnIconPickedListener {
         public void onIconPicked(int drawableId, String drawableName);
     }
 
     public IconPickerFragment() {
         this.filter = null;
         setStyle(STYLE_NO_TITLE, 0);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         if (savedInstanceState != null) {
             filter = savedInstanceState.getString(KEY_CURRENTFILTER);
         }
 
         View v = inflater.inflate(R.layout.fragment_icon_picker, container, false);
         GridView drawableGrid = (GridView) v.findViewById(R.id.icon_grid);
         List<Integer> drawableIds = new LinkedList<Integer>();
 
         catAdapter = new ArrayAdapter<String>(getActivity(), 
                 android.R.layout.simple_spinner_item, android.R.id.text1);
         catAdapter.setDropDownViewResource(
                 android.R.layout.simple_spinner_dropdown_item); 
 
         categorySpinner = (Spinner) v.findViewById(R.id.iconcat_spinner);
         categorySpinner.setAdapter(catAdapter);
         categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parent, View view, 
                 int position, long id) {
                 updateGrid(position);
             };
 
             public void onNothingSelected(AdapterView<?> parent) {
             };
         });
 
         gridAdapter = new DrawableAdapter(getActivity(), drawableIds);
 
         drawableGrid.setAdapter(gridAdapter);
         drawableGrid.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, 
                 int position, long id) {
                 Integer drawableId = gridAdapter.getItem(position);
                 String drawableName = getResources().getResourceEntryName(drawableId);
                 dismiss();
 
                 if (listener != null) {
                     listener.onIconPicked(drawableId, drawableName);
                 } else {
                     Log.d("TMM", "Icon selected but listener null");
                 }
             }
         });
 
         updateIcons();
         updateSpinner();
         updateGrid(categorySpinner.getSelectedItemPosition());
         return v;
     }
 
     /**
      * @param listener the listener to set
      */
     public void setListener(OnIconPickedListener listener) {
         this.listener = listener;
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString(KEY_CURRENTFILTER, filter); 
         super.onSaveInstanceState(outState);
     }
 
     private void updateGrid(int selectedIndex) {
         if (gridAdapter == null) {
             return;
         }
 
         selectedIndex = Math.max(selectedIndex, 0);
 
         gridAdapter.setNotifyOnChange(false);
         gridAdapter.clear();
 
         List<IconCategory> selectedIconCategories; 
         
         if (selectedIndex == 0) {
             selectedIconCategories = iconCategories;
         } else {
             selectedIconCategories = new LinkedList<IconCategory>();
             selectedIconCategories.add(iconCategories.get(selectedIndex - 1));
         }
 
         for (IconCategory iconCat : selectedIconCategories) {
 
             for (Integer drawableId : iconCat.drawableIds) {
                 gridAdapter.add(drawableId);
             }
         }
 
         gridAdapter.notifyDataSetChanged();
     }
 
     private void updateSpinner() {
         catAdapter.setNotifyOnChange(false);
         catAdapter.clear();
 
         String strAll = getResources().getString(R.string.all);
         catAdapter.add(strAll);
 
         for (IconCategory iconCat : iconCategories) {
             catAdapter.add(iconCat.title);
         }
         
         catAdapter.notifyDataSetChanged();
     }
 
     private void updateIcons() {
         Resources res = getActivity().getResources();
 
         TypedArray iconCats = res.obtainTypedArray(R.array.iconcats);
         int numCategories = iconCats.length();
 
         iconCategories = new ArrayList<IconCategory>(numCategories);
 
         for (int i = 0; i < numCategories; i++) {
            int resId = iconCats.peekValue(i).resourceId;
 
             if (resId > 0) {
                 IconCategory iconCat = new IconCategory();
                 TypedArray catInfo = res.obtainTypedArray(resId);
                 iconCat.title = catInfo.getString(0);
                 int entriesResId = catInfo.getResourceId(1, 0);
 
                 if (entriesResId > 0) {
                     TypedArray icons = res.obtainTypedArray(entriesResId);
                     int numIcons = icons.length();
 
                     iconCat.drawableIds = new ArrayList<Integer>(numIcons);
                     for (int j = 0; j < numIcons; j++) {
                         int drawableId = icons.getResourceId(j, 0);
                         iconCat.drawableIds.add(drawableId);
                     }
 
                     icons.recycle();
                 } else {
                     Log.e("TMM", "Error reading icon entries of cat with index " + i);
                 }
 
                 iconCategories.add(iconCat);
             } else {
                 Log.e("TMM", "Error reading icon category with index " + i);
             }
         }
 
         iconCats.recycle();
         Collections.sort(iconCategories, new IconCategory.Comparator());
     }
 
     private static class IconCategory {
         String title;
         List<Integer> drawableIds;
 
         private static class Comparator implements java.util.Comparator<IconCategory> {
             public int compare(IconCategory lcat, IconCategory rcat) {
                 return lcat.title.compareTo(rcat.title);
             }
         }
     }
 }
 
