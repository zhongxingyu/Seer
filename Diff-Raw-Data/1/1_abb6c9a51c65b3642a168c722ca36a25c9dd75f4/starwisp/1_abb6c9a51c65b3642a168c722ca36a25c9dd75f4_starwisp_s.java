 // Starwisp Copyright (C) 2013 Dave Griffiths
 //
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU Affero General Public License as
 // published by the Free Software Foundation, either version 3 of the
 // License, or (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Affero General Public License for more details.
 //
 // You should have received a copy of the GNU Affero General Public License
 // along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package foam.mongoose;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.content.Context;
 import android.graphics.Color;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.view.View;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.text.TextWatcher;
 import android.text.Editable;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 import java.util.Calendar;
 
 public class starwisp extends StarwispActivity
 {
     static {
         // register all activities here
         ActivityManager.RegisterActivity("splash",starwisp.class);
         ActivityManager.RegisterActivity("main",MainActivity.class);
         ActivityManager.RegisterActivity("observations",ObservationsActivity.class);
 
         ActivityManager.RegisterActivity("group-composition",GroupCompositionActivity.class);
         ActivityManager.RegisterActivity("pup-focal-start",PupFocalStartActivity.class);
         ActivityManager.RegisterActivity("pup-focal",PupFocalActivity.class);
         ActivityManager.RegisterActivity("group-events",GroupEventsActivity.class);
 
         ActivityManager.RegisterActivity("manage-packs",ManagePacksActivity.class);
         ActivityManager.RegisterActivity("new-pack",NewPackActivity.class);
         ActivityManager.RegisterActivity("manage-individual",ManageIndividualActivity.class);
         ActivityManager.RegisterActivity("new-individual",NewIndividualActivity.class);
         ActivityManager.RegisterActivity("update-individual",UpdateIndividualActivity.class);
 
         ActivityManager.RegisterActivity("tag-location",TagLocationActivity.class);
         ActivityManager.RegisterActivity("sync",SyncActivity.class);
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         setContentView(R.layout.main);
 
         String dirname = "mongoose/";
         m_AppDir = "/sdcard/"+dirname;
         File appdir = new File(m_AppDir);
         appdir.mkdirs();
 
         // build static things
         m_Scheme = new Scheme(this);
         m_Builder = new StarwispBuilder(m_Scheme);
         m_Name = "main";
 
         // tell scheme the date
         final Calendar c = Calendar.getInstance();
         int day = c.get(Calendar.DAY_OF_MONTH);
         int month = c.get(Calendar.MONTH)+1;
         int year = c.get(Calendar.YEAR);
 
         // pass in a bunch of useful stuff
         m_Scheme.eval("(define dirname \"/sdcard/"+dirname+"\")(define date-day "+day+") (define date-month "+month+") (define date-year "+year+")");
 
         Log.i("starwisp","started, now running starwisp.scm...");
         m_Scheme.eval(m_Scheme.readRawTextFile(this, "starwisp.scm"));
 
         super.onCreate(savedInstanceState);
     }
 }
