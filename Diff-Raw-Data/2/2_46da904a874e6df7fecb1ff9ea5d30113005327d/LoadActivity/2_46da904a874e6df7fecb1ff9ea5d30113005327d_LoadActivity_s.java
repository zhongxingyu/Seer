 //        Guidebook is an Android application that reads audioguides using Text-to-Speech services.
 //        Copyright (C) 2013  Adrián Romero Corchado
 //
 //        This program is free software: you can redistribute it and/or modify
 //        it under the terms of the GNU General Public License as published by
 //        the Free Software Foundation, either version 3 of the License, or
 //        (at your option) any later version.
 //
 //        This program is distributed in the hope that it will be useful,
 //        but WITHOUT ANY WARRANTY; without even the implied warranty of
 //        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //        GNU General Public License for more details.
 //
 //        You should have received a copy of the GNU General Public License
 //        along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.adrguides;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 /**
  * Created by adrian on 2/09/13.
  */
 public class LoadActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         FragmentManager fm = getFragmentManager();
         LoadGuideFragment loadguide = (LoadGuideFragment) fm.findFragmentByTag(LoadGuideFragment.TAG);
         if (loadguide == null) {
             // Calculate rezize dimensions
             Point size = new Point();
             this.getWindowManager().getDefaultDisplay().getSize(size);
             int imagesize = Math.max(size.x, size.y);
             // loading guide
             loadguide = new LoadGuideFragment();
             fm.beginTransaction().add(loadguide, LoadGuideFragment.TAG).commit();
 
             Log.d("com.adrguides.LoadActivity", "Loading Data --> " + getIntent().getDataString());
             loadguide.loadGuide(getApplicationContext(), getIntent().getDataString(), imagesize);
         }
 
         Fragment loadfragment = fm.findFragmentByTag(LoadFragment.TAG);
         if (loadfragment == null) {
             loadfragment = new LoadFragment();
             fm.beginTransaction()
                    .add(android.R.id.content, loadfragment, ReadGuideFragment.TAG)
                     .commit();
         }
     }
 }
