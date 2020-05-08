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
 
 package foam.nebogeo;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.util.Log;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import java.io.File;
 import java.io.FileOutputStream;
 
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.Space;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.view.View;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.text.TextWatcher;
 import android.text.Editable;
 import android.widget.DatePicker;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera;
 import java.io.FileNotFoundException;
 import java.util.TimeZone;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.text.DateFormat;
 
 import android.app.TimePickerDialog;
 import android.app.DatePickerDialog;
 import java.util.Calendar;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 public class StarwispBuilder
 {
     Scheme m_Scheme;
 
     public StarwispBuilder(Scheme scm) {
         m_Scheme = scm;
     }
 
     public int BuildOrientation(String p) {
         if (p.equals("vertical")) return LinearLayout.VERTICAL;
         if (p.equals("horizontal")) return LinearLayout.HORIZONTAL;
         return LinearLayout.VERTICAL;
     }
 
     public int BuildLayoutGravity(String p) {
         if (p.equals("centre")) return Gravity.CENTER;
         if (p.equals("left")) return Gravity.LEFT;
         if (p.equals("right")) return Gravity.RIGHT;
         return Gravity.LEFT;
     }
 
     public int BuildLayoutParam(String p) {
         if (p.equals("fill-parent")) return LayoutParams.FILL_PARENT;
         if (p.equals("match-parent")) return LayoutParams.MATCH_PARENT;
         if (p.equals("wrap-content")) return LayoutParams.WRAP_CONTENT;
         try {
             return Integer.parseInt(p);
         } catch (NumberFormatException e) {
             Log.i("starwisp", "Layout error with ["+p+"]");
             // send error message
             return LayoutParams.WRAP_CONTENT;
         }
     }
 
     public LinearLayout.LayoutParams BuildLayoutParams(JSONArray arr) {
         try {
             LinearLayout.LayoutParams lp =
                 new LinearLayout.LayoutParams(BuildLayoutParam(arr.getString(1)),
                                               BuildLayoutParam(arr.getString(2)),
                                               (float)arr.getDouble(3));
             lp.gravity=BuildLayoutGravity(arr.getString(4));
             return lp;
         } catch (JSONException e) {
             Log.e("starwisp", "Error parsing data " + e.toString());
             return null;
         }
     }
 
     private void Callback(StarwispActivity ctx, int wid)
     {
         try {
             String ret=m_Scheme.eval("(widget-callback \""+
                                      ctx.m_Name+"\" "+
                                      wid+" '())");
             UpdateList(ctx, new JSONArray(ret));
         } catch (JSONException e) {
             Log.e("starwisp", "Error parsing data " + e.toString());
         }
     }
 
     private void CallbackArgs(StarwispActivity ctx, int wid, String args)
     {
         try {
             String ret=m_Scheme.eval("(widget-callback \""+
                                      ctx.m_Name+"\" "+
                                      wid+" '("+args+"))");
             UpdateList(ctx, new JSONArray(ret));
         } catch (JSONException e) {
             Log.e("starwisp", "Error parsing data " + e.toString());
         }
     }
 
     public void Build(final StarwispActivity ctx, JSONArray arr, ViewGroup parent) {
         try {
             String type = arr.getString(0);
 
             if (type.equals("linear-layout")) {
                 LinearLayout v = new LinearLayout(ctx);
                 v.setId(arr.getInt(1));
                 v.setOrientation(BuildOrientation(arr.getString(2)));
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                 parent.addView(v);
                 JSONArray children = arr.getJSONArray(4);
                 for (int i=0; i<children.length(); i++) {
                     Build(ctx,new JSONArray(children.getString(i)), v);
                 }
                 return;
             }
 
             if (type.equals("space")) {
                 // Space v = new Space(ctx); (class not found runtime error??)
                 TextView v = new TextView(ctx);
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                 parent.addView(v);
             }
 
 
             if (type.equals("image-view")) {
                 ImageView v = new ImageView(ctx);
                 v.setId(arr.getInt(1));
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
 
                 String image = arr.getString(2);
 
                 Log.i("starwisp",image);
 
                 if (image.startsWith("/")) {
                     Bitmap bitmap = BitmapFactory.decodeFile(image);
                     v.setImageBitmap(bitmap);
                 } else {
                     int id = ctx.getResources().getIdentifier(image,"drawable", ctx.getPackageName());
                     v.setImageResource(id);
                 }
 
                 parent.addView(v);
             }
 
             if (type.equals("text-view")) {
                 TextView v = new TextView(ctx);
                 v.setId(arr.getInt(1));
                 v.setText(arr.getString(2));
                 v.setTextSize(arr.getInt(3));
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                 v.setTypeface(((StarwispActivity)ctx).m_Typeface);
                 parent.addView(v);
             }
 
             if (type.equals("edit-text")) {
                 final EditText v = new EditText(ctx);
                 v.setId(arr.getInt(1));
                 v.setText(arr.getString(2));
                 v.setTextSize(arr.getInt(3));
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                 v.setTypeface(((StarwispActivity)ctx).m_Typeface);
                 final String fn = arr.getString(5);
                 v.setSingleLine(true);
 /*                v.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         v.setFocusable(true);
                         v.requestFocus();
 
                         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                     }
                 });
 */
                 v.setOnKeyListener(new View.OnKeyListener() {
                     public boolean onKey(View a, int keyCode, KeyEvent event) {
                         if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                             (keyCode == KeyEvent.KEYCODE_ENTER)) {
                             CallbackArgs(ctx,v.getId(),"\""+v.getText()+"\"");
                         }
                         return false;
                     }
                 });
 
                 parent.addView(v);
 
 
             }
 
             if (type.equals("button")) {
                 Button v = new Button(ctx);
                 v.setId(arr.getInt(1));
                 v.setText(arr.getString(2));
                 v.setTextSize(arr.getInt(3));
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(4)));
                 v.setTypeface(((StarwispActivity)ctx).m_Typeface);
                 final String fn = arr.getString(5);
                 v.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         Callback(ctx,v.getId());
                     }
                 });
                 parent.addView(v);
             }
 
             if (type.equals("seek-bar")) {
                 SeekBar v = new SeekBar(ctx);
                 v.setId(arr.getInt(1));
                 v.setMax(arr.getInt(2));
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                 final String fn = arr.getString(4);
 
                 v.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                     public void onProgressChanged(SeekBar v, int a, boolean s) {
                         CallbackArgs(ctx,v.getId(),Integer.toString(a));
                     }
                     public void onStartTrackingTouch(SeekBar v) {}
                     public void onStopTrackingTouch(SeekBar v) {}
                 });
                 parent.addView(v);
             }
 
             if (type.equals("spinner")) {
                 Spinner v = new Spinner(ctx);
                 final int wid = arr.getInt(1);
                 v.setId(wid);
                 final JSONArray items = arr.getJSONArray(2);
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(3)));
                 ArrayList<String> spinnerArray = new ArrayList<String>();
 
                 for (int i=0; i<items.length(); i++) {
                     spinnerArray.add(items.getString(i));
                 }
 
                 ArrayAdapter spinnerArrayAdapter =
                     new ArrayAdapter<String>(ctx,
                                              android.R.layout.simple_spinner_item,
                                              spinnerArray) {
                     public View getView(int position, View convertView,ViewGroup parent) {
                         View v = super.getView(position, convertView, parent);
                         ((TextView) v).setTypeface(((StarwispActivity)ctx).m_Typeface);
                         return v;
                     }
                 };
 
                 v.setAdapter(spinnerArrayAdapter);
                 v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                     public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                         try {
                             CallbackArgs(ctx,wid,"\""+items.getString(pos)+"\"");
                         } catch (JSONException e) {
                             Log.e("starwisp", "Error parsing data " + e.toString());
                         }
                     }
                     public void onNothingSelected(AdapterView<?> v) {}
                 });
 
                 parent.addView(v);
             }
 
             if (type.equals("canvas")) {
                 StarwispCanvas v = new StarwispCanvas(ctx);
                 final int wid = arr.getInt(1);
                 v.setId(wid);
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                 v.SetDrawList(arr.getJSONArray(3));
                 parent.addView(v);
             }
 
             if (type.equals("camera-preview")) {
                 PictureTaker pt = new PictureTaker();
                 CameraPreview v = new CameraPreview(ctx,pt);
                 final int wid = arr.getInt(1);
                 v.setId(wid);
                 v.setLayoutParams(BuildLayoutParams(arr.getJSONArray(2)));
                 parent.addView(v);
             }
 
         } catch (JSONException e) {
             Log.e("starwisp", "Error parsing ["+arr.toString()+"] " + e.toString());
         }
     }
 
     public void UpdateList(Activity ctx, JSONArray arr) {
         try {
             for (int i=0; i<arr.length(); i++) {
                 Update(ctx,new JSONArray(arr.getString(i)));
             }
         } catch (JSONException e) {
             Log.e("starwisp", "Error parsing data " + e.toString());
         }
     }
 
     public void Update(final Activity ctx, JSONArray arr) {
         try {
 
             String type = arr.getString(0);
             Integer id = arr.getInt(1);
             String token = arr.getString(2);
 
             Log.i("starwisp", "Update: "+type+" "+id+" "+token);
 
             // non widget commands
             if (token.equals("toast")) {
                 Toast msg = Toast.makeText(ctx.getBaseContext(),arr.getString(3),Toast.LENGTH_SHORT);
                 msg.show();
                 return;
             }
 
             if (token.equals("time-picker-dialog")) {
 
                 final Calendar c = Calendar.getInstance();
                 int hour = c.get(Calendar.HOUR_OF_DAY);
                 int minute = c.get(Calendar.MINUTE);
 
                 // Create a new instance of TimePickerDialog and return it
                 TimePickerDialog d=new TimePickerDialog(ctx, null, hour, minute, true);
                 d.show();
 
                 Log.i("starwisp", "attempt time picker");
                 return;
             };
 
             if (token.equals("make-directory")) {
                 File file = new File(((StarwispActivity)ctx).m_AppDir+arr.getString(3));
                 file.mkdirs();
                 return;
             }
 
             if (token.equals("list-files")) {
                 final String name = arr.getString(3);
                 File file = new File(((StarwispActivity)ctx).m_AppDir+arr.getString(5));
                File list[] = file.listFiles();

                String code="(";
                for( int i=0; i< list.length; i++)
                {
                    code+=" \""+list[i].getName()+"\"";
                }
                code+=")";
 
                try {
                    String ret=m_Scheme.eval("(dialog-callback \""+ name+"\" '("+code+"))");
                    UpdateList(ctx, new JSONArray(ret));
                } catch (JSONException e) {
                    Log.e("starwisp", "Error parsing data " + e.toString());
                 }
                 return;
             }
 
             if (token.equals("date-picker-dialog")) {
                 final Calendar c = Calendar.getInstance();
                 int day = c.get(Calendar.DAY_OF_MONTH);
                 int month = c.get(Calendar.MONTH);
                 int year = c.get(Calendar.YEAR);
 
                 final String name = arr.getString(3);
 
                 // Create a new instance of TimePickerDialog and return it
                 DatePickerDialog d=new DatePickerDialog(
                     ctx,
                     new DatePickerDialog.OnDateSetListener() {
                         public void onDateSet(DatePicker view, int year, int month, int day) {
                             try {
                                 String ret=m_Scheme.eval("(dialog-callback \""+
                                                          name+"\" '("+day+" "+month+" "+year+"))");
                                 UpdateList(ctx, new JSONArray(ret));
                             } catch (JSONException e) {
                                 Log.e("starwisp", "Error parsing data " + e.toString());
                             }
                         }
                     }, year, month, day);
                 d.show();
                 return;
             };
 
 
             if (token.equals("start-activity")) {
                 ActivityManager.StartActivity(ctx,arr.getString(3),arr.getInt(4),arr.getString(5));
                 return;
             }
 
             if (token.equals("finish-activity")) {
                 ctx.setResult(arr.getInt(3));
                 ctx.finish();
                 return;
             }
 
             // now try and find the widget
             View vv=ctx.findViewById(id);
             if (vv==null)
             {
                 Log.i("starwisp", "Can't find widget : "+id);
                 return;
             }
 
             // tokens that work on everything
             if (token.equals("hide")) {
                 vv.setVisibility(View.GONE);
                 return;
             }
 
             if (token.equals("show")) {
                 vv.setVisibility(View.VISIBLE);
                 return;
             }
 
             // special cases
             if (type.equals("linear-layout")) {
                 LinearLayout v = (LinearLayout)vv;
                 if (token.equals("contents")) {
                     v.removeAllViews();
                     JSONArray children = arr.getJSONArray(3);
                     for (int i=0; i<children.length(); i++) {
                         Build((StarwispActivity)ctx,new JSONArray(children.getString(i)), v);
                     }
                 }
             }
 
             if (type.equals("image-view")) {
                 ImageView v = (ImageView)vv;
                 if (token.equals("image")) {
                     int iid = ctx.getResources().getIdentifier(arr.getString(3),
                                                                "drawable", ctx.getPackageName());
                     v.setImageResource(iid);
                 }
                 if (token.equals("external-image")) {
                     Bitmap bitmap = BitmapFactory.decodeFile(arr.getString(3));
                     v.setImageBitmap(bitmap);
                 }
                 return;
             }
 
             if (type.equals("text-view")) {
                 TextView v = (TextView)vv;
                 if (token.equals("text")) {
                     v.setText(arr.getString(3));
                 }
                 return;
             }
 
             if (type.equals("edit-text")) {
                 EditText v = (EditText)vv;
                 if (token.equals("text")) {
                     v.setText(arr.getString(3));
                 }
                 return;
             }
 
 
             if (type.equals("button")) {
                 Button v = (Button)vv;
                 if (token.equals("text")) {
                     v.setText(arr.getString(3));
                 }
 
                 if (token.equals("listener")) {
                     final String fn = arr.getString(3);
                     v.setOnClickListener(new View.OnClickListener() {
                         public void onClick(View v) {
                             m_Scheme.eval("("+fn+")");
                         }
                     });
                 }
                 return;
             }
 
             if (type.equals("canvas")) {
                 StarwispCanvas v = (StarwispCanvas)vv;
                 if (token.equals("drawlist")) {
                     v.SetDrawList(arr.getJSONArray(3));
                 }
                 return;
             }
 
             if (type.equals("camera-preview")) {
                 Log.i("starwisp","camera update");
                 final CameraPreview v = (CameraPreview)vv;
 
                 if (token.equals("take-picture")) {
                     final String path = ((StarwispActivity)ctx).m_AppDir+arr.getString(3);
 
                     v.TakePicture(
                         new PictureCallback() {
                             public void onPictureTaken(byte[] data, Camera camera) {
 
                                 Log.i("starwisp", "callback returns");
 
                                 String datetime = getDateTime();
                                 String filename = path+datetime + ".jpg";
 
                                 Log.i("starwisp", filename);
                                 SaveData(filename,data);
 
                                 v.Shutdown();
                                 ctx.finish();
 
                             }
                         });
                 }
 
                 if (token.equals("shutdown")) {
                     Log.i("starwisp","shutting down camera");
                     v.Shutdown();
                 }
 
 
 
                 return;
             }
 
 
             if (type.equals("spinner")) {
                 Spinner v = (Spinner)vv;
                 if (token.equals("array")) {
                     final JSONArray items = arr.getJSONArray(3);
                     ArrayList<String> spinnerArray = new ArrayList<String>();
 
                     for (int i=0; i<items.length(); i++) {
                         spinnerArray.add(items.getString(i));
                     }
 
                     ArrayAdapter spinnerArrayAdapter =
                         new ArrayAdapter<String>(ctx,
                                                  android.R.layout.simple_spinner_item,
                                                  spinnerArray) {
                         public View getView(int position, View convertView,ViewGroup parent) {
                             View v = super.getView(position, convertView, parent);
                             ((TextView) v).setTypeface(((StarwispActivity)ctx).m_Typeface);
                             return v;
                         }
                     };
 
                     v.setAdapter(spinnerArrayAdapter);
 
                     final int wid = id;
                     // need to update for new values
                     v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                         public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                             try {
                                 CallbackArgs((StarwispActivity)ctx,wid,"\""+items.getString(pos)+"\"");
                             } catch (JSONException e) {
                                 Log.e("starwisp", "Error parsing data " + e.toString());
                             }
                         }
                         public void onNothingSelected(AdapterView<?> v) {}
                     });
 
                 }
                 return;
             }
 
         } catch (JSONException e) {
             Log.e("starwisp", "Error parsing data " + e.toString());
         }
     }
 
 
     static public void SaveData(String path, byte[] data) {
         try {
             File file = new File(path);
 
             if (file == null) {
                 return;
             }
             try {
                 FileOutputStream fos = new FileOutputStream(file);
                 fos.write(data);
                 fos.close();
             } catch (FileNotFoundException e) {
             } catch (IOException e) {
             }
         } catch (Exception e) {
         }
     }
 
 	public static String getDateTime() {
 		DateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
 		df.setTimeZone(TimeZone.getTimeZone("GMT"));
 		return df.format(new Date());
 	}
 
 }
