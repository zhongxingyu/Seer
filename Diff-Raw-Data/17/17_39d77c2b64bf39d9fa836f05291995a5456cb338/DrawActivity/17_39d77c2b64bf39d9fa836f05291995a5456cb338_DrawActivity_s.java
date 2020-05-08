 package com.example;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 
 import java.io.*;
 
 public class DrawActivity extends Activity implements View.OnClickListener, ColorPickerDialog.OnColorChangedListener {
 
     private String fileName;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Set full screen view
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setContentView(R.layout.main);
 
         //this initializes with the default paint values
         Paint paint = new Paint();
         paint.setColor(Config.DEFAULT_FORE_COLOR);
         paint.setAntiAlias(true);
         paint.setStrokeWidth(0);
 
         Config.globalPaintBrush = paint;
 
         Bundle b = getIntent().getExtras();
 
         if (b != null) {
             fileName = b.getString("loadFile");
             try {
                 loadFile(fileName);
             } catch (Exception e) {
             }
         } else {
             newFile();
         }
 
         getActionBar().setDisplayShowHomeEnabled(false);
         getActionBar().setDisplayShowTitleEnabled(false);
 
     }
 
 
     public boolean validateTitle() {
         EditText txt = (EditText) findViewById(R.id.txtFileTitle);
 
         //failed, because no title was given
         if (txt.getText().toString().equals(this.getString(R.string.untitle)))
             return false;
 
         //failed because file name is already used
         if (isFileNameUsed(txt.getText().toString()))
             return false;
 
         return true;
     }
 
     public void showDialog(String text) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(text);
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void saveFile() throws IOException {
         if (!validateTitle()) {
             showDialog("Please give your file a new title.");
             return;
         }
 
         fileName = ((EditText) findViewById(R.id.txtFileTitle)).getText().toString();
         FileOutputStream fos = this.openFileOutput(fileName, Context.MODE_PRIVATE);
         ObjectOutputStream os = new ObjectOutputStream(fos);
 
         LinearLayout layout = (LinearLayout) findViewById(R.id.linLayout);
         DrawView view = (DrawView) layout.getChildAt(0);
 
         view.writeObject(os);
         os.close();
 
         Context context = getApplicationContext();
         CharSequence text = fileName + " saved.";
         int duration = Toast.LENGTH_SHORT;
         Toast toast = Toast.makeText(context, text, duration);
         toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
         toast.show();
 
     }
 
     public void loadFile(String fileName) throws IOException, ClassNotFoundException {
         LinearLayout layout = (LinearLayout) findViewById(R.id.linLayout);
         layout.removeAllViews();
 
         FileInputStream fis = this.openFileInput(fileName);
         ObjectInputStream is = new ObjectInputStream(fis);
         DrawView drawView = new DrawView(this, Config.globalPaintBrush);
         drawView.readObject(is);
         is.close();
 
         drawView.setLayoutParams(new ViewGroup.LayoutParams(Config.CANVAS_WIDTH, Config.CANVAS_HEIGHT));
         layout.addView(drawView);
 
     }
 
     public void newFile() {
         DrawView view = new DrawView(this, Config.globalPaintBrush);
         view.setLayoutParams(new ViewGroup.LayoutParams(Config.CANVAS_WIDTH, Config.CANVAS_HEIGHT));
         LinearLayout layout = (LinearLayout) findViewById(R.id.linLayout);
         layout.removeAllViews();
 
         layout.addView(view);
         fileName = getString(R.string.untitle);
 
         EditText txt = (EditText) findViewById(R.id.txtFileTitle);
         if (txt != null)
             txt.setText(fileName);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
 
         EditText title = (EditText) menu.findItem(R.id.pageTitle).getActionView();
         title.setText(fileName);
 
         ImageView iv = (ImageView) menu.findItem(R.id.toggleScroll).getActionView();
         iv.setOnClickListener(this);
         return true;
     }
 
 
     private void showLoadFileActivity() {
         Intent myIntent = new Intent(this, OpenFileActivity.class);
         startActivity(myIntent);
         finish();
     }
 
     private void toggleScroll(ImageView view) {
         Config.scrollOn = !Config.scrollOn;
         if (Config.scrollOn)
             view.setImageResource(R.drawable.scroll_32);
         else
             view.setImageResource(R.drawable.brush_32);
     }
 
     private void undo() {
         LinearLayout layout = (LinearLayout) findViewById(R.id.linLayout);
         DrawView view = (DrawView) layout.getChildAt(0);
         view.undoLastAction();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         try {
             switch (item.getItemId()) {
                 case R.id.new_file:
                     newFile();
                     return true;
                 case R.id.save_file:
                     saveFile();
                     return true;
                 case R.id.open_file:
                     showLoadFileActivity();
                     return true;
                 case R.id.undo:
                     undo();
                     return true;
                 case R.id.colorPicker:
                     chooseColor();
                     return true;
                 case R.id.erase:
                     toggleErase();
                     return true;
                 default:
                     return super.onOptionsItemSelected(item);
             }
         } catch (Exception e) {
             Log.e("ERROR", e.getMessage());
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     private boolean isFileNameUsed(String txt) {
         String files[] = fileList();
 
         for (String s : files) {
             if (s.equalsIgnoreCase(txt))
                 return true;
         }
 
         return false;
     }
 
     private void chooseColor() {
         ColorPickerDialog picker = new ColorPickerDialog(this, this, Config.globalPaintBrush.getColor());
         picker.show();
     }
 
     private void toggleErase() {
         LinearLayout layout = (LinearLayout) findViewById(R.id.linLayout);
         DrawView view = (DrawView) layout.getChildAt(0);
         view.isErasing = !view.isErasing;
     }
 
     @Override
     public void onClick(View view) {
         toggleScroll((ImageView) view);
     }
 
     @Override
     public void colorChanged(int color) {
         Config.globalPaintBrush.setColor(color);
     }
 }
