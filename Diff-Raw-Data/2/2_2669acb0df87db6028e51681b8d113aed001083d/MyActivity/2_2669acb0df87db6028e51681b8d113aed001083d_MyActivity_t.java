 package pl.byd.promand.Team1;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.*;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 
 import android.provider.MediaStore;
 import android.view.*;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.internal.view.menu.ActionMenu;
 import com.actionbarsherlock.view.*;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.promand.Team1.R;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.io.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class MyActivity extends SherlockActivity {
 
     public static SurfaceViewDraw view;
     Context context = this;
     private Dialog start;
     private Button tools;
     private Bundle savedInstanceState;
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.upbutton2: {
                 start = new Dialog(context);
                 start.setTitle("Let's start");
                 start.setContentView(R.layout.start_dialog);
                 start.setCancelable(false);
                 start.show();
 
                 ImageButton newFile = (ImageButton) start.findViewById(R.id.newButton);
                 ImageButton openFile = (ImageButton) start.findViewById(R.id.loadButton);
                 ImageButton takeAPhoto = (ImageButton) start.findViewById(R.id.takeAPhotoButton);
                 Button exit = (Button) start.findViewById(R.id.exitB);
 
                 newFile.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         MyActivity.this.onCreate(savedInstanceState);
                         start.dismiss();
                     }
                 });
 
                 openFile.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         start.dismiss();
                         ModelRoot.getRoot().setFilePath("/mnt");
                         Intent i = new Intent(context, OpenFileActivity.class);
                         startActivityForResult(i, 4);
                     }
                 });
 
                 takeAPhoto.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                         startActivityForResult(cameraIntent, 0);
                     }
                 });
 
                 exit.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         finish();
                     }
                 });
 
                 start.setOnDismissListener(new DialogInterface.OnDismissListener() {
                     @Override
                     public void onDismiss(DialogInterface dialog) {
                         final Dialog orient = new Dialog(MyActivity.this);
                         orient.setTitle("Select in which orientation you will work");
                         orient.setCancelable(false);
                         orient.setContentView(R.layout.port_or_land);
 
                         ImageButton portB = (ImageButton) orient.findViewById(R.id.portB);
                         ImageButton landB = (ImageButton) orient.findViewById(R.id.landB);
 
                         portB.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                 orient.dismiss();
                             }
                         });
 
                         landB.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                 orient.dismiss();
                             }
                         });
 
                         orient.show();
                     }
                 });
                 break;
             }
             case R.id.upbutton4: {
                 taptoshare();
                 break;
             }
             case R.id.upbutton3: {
                 taptosave();
                 break;
             }
             default: {
                 break;
             }
         }
         return true;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.savedInstanceState = savedInstanceState;
 
         if (ModelRoot.getRoot().isStart()) {
             final Dialog orient = new Dialog(MyActivity.this);
             orient.setTitle("Select in which orientation you will work");
             orient.setCancelable(false);
             orient.setContentView(R.layout.port_or_land);
 
             ImageButton portB = (ImageButton) orient.findViewById(R.id.portB);
             ImageButton landB = (ImageButton) orient.findViewById(R.id.landB);
 
             portB.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                     orient.dismiss();
                 }
             });
 
             landB.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                     orient.dismiss();
                 }
             });
 
             orient.show();
             ModelRoot.getRoot().setStart(false);
         }
 
         //Main screen (bottom buttons)
         tools = (Button) findViewById(R.id.button1);
         Button width = (Button) findViewById(R.id.button2);
         width.setText(width.getText() + ": " + ModelRoot.getRoot().getWidth());
         Button colors = (Button) findViewById(R.id.button3);
         Button settings = (Button) findViewById(R.id.button4);
 
         tools.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 final Dialog toolD = new Dialog(MyActivity.this);
                 toolD.setTitle("Select a tool");
                 toolD.setContentView(R.layout.activity_tools);
                 toolD.show();
 
                 final ImageButton btnBrush = (ImageButton) toolD.findViewById(R.id.button1);
                 final ImageButton btnPen = (ImageButton) toolD.findViewById(R.id.button4);
                 final ImageButton btnEraser = (ImageButton) toolD.findViewById(R.id.button3);
                 final ImageButton btnFiller = (ImageButton) toolD.findViewById(R.id.button2);
                 final ImageButton btnLine = (ImageButton) toolD.findViewById(R.id.button6);
                 final ImageButton btnCurvedLine = (ImageButton) toolD.findViewById(R.id.button5);
                 final ImageButton btnText = (ImageButton) toolD.findViewById(R.id.button7);
                 final ImageButton btnCircle = (ImageButton) toolD.findViewById(R.id.button8);
                 final ImageButton btnRectangle = (ImageButton) toolD.findViewById(R.id.button9);
 
                 btnBrush.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("BRUSH has chosen");
                         ModelRoot.getRoot().setTool("brush");
                         ModelRoot.getRoot().setToolI(btnBrush.getDrawable());
                         toolD.dismiss();
                     }
 
 
                 });
 
                 btnPen.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("PEN has chosen");
                         ModelRoot.getRoot().setTool("pen");
                         ModelRoot.getRoot().setToolI(btnPen.getDrawable());
                         toolD.dismiss();
                     }
 
 
                 });
 
                 btnEraser.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("ERASER has chosen");
                         ModelRoot.getRoot().setTool("eraser");
                         ModelRoot.getRoot().setToolI(btnEraser.getDrawable());
                         toolD.dismiss();
                     }
 
 
                 });
 
                 btnFiller.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("FILLER has chosen");
                         ModelRoot.getRoot().setTool("filler");
                         ModelRoot.getRoot().setToolI(btnFiller.getDrawable());
                         toolD.dismiss();
                     }
 
 
                 });
 
                 btnLine.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("LINE has chosen");
                         ModelRoot.getRoot().setTool("fill");
                         ModelRoot.getRoot().setToolI(btnLine.getDrawable());
                         toolD.dismiss();
                     }
                 });
 
                 btnCurvedLine.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("CURVED LINE has chosen");
                         ModelRoot.getRoot().setTool("curved_line");
                         ModelRoot.getRoot().setToolI(btnCurvedLine.getDrawable());
                         toolD.dismiss();
                     }
 
 
                 });
 
                 btnText.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("TEXT has chosen");
                         ModelRoot.getRoot().setTool("text");
                         ModelRoot.getRoot().setToolI(btnText.getDrawable());
                         toolD.dismiss();
                     }
 
 
                 });
 
                 btnCircle.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("CIRCLE has chosen");
                         ModelRoot.getRoot().setTool("circle");
                         ModelRoot.getRoot().setToolI(btnCircle.getDrawable());
                         toolD.dismiss();
                     }
 
 
                 });
 
                 btnRectangle.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showToastMessage("RECTANGLE has chosen");
                         ModelRoot.getRoot().setTool("rectangle");
                         ModelRoot.getRoot().setToolI(btnRectangle.getDrawable());
                         toolD.dismiss();
                     }
                 });
 
                 toolD.setOnDismissListener(new DialogInterface.OnDismissListener() {
                     @Override
                     public void onDismiss(DialogInterface dialog) {
                         tools.setCompoundDrawables(null, null, ModelRoot.getRoot().getToolI(), null);
                     }
                 });
             }
         });
 
         width.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 final Dialog dialogWidth = new Dialog(context);
 
                 dialogWidth.setContentView(R.layout.activity_width_main);
                 SeekBar bar = (SeekBar) dialogWidth.findViewById(R.id.seekbar);
                 final TextView txtNumbers = (TextView) dialogWidth.findViewById(R.id.TextView01);
                 Button SetWidth = (Button) dialogWidth.findViewById(R.id.bSetWidth);
 
 
                 SetWidth.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         dialogWidth.dismiss();
                     }
                 });
 
                 dialogWidth.setOnDismissListener(new DialogInterface.OnDismissListener() {
                     @Override
                     public void onDismiss(DialogInterface dialog) {
                         Button widthB = (Button) findViewById(R.id.button2);
                         widthB.setText("Width: " + ModelRoot.getRoot().getWidth());
                     }
                 });
 
                 bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
                     @Override
                     public void onStopTrackingTouch(SeekBar arg0) {
 
                     }
 
                     @Override
                     public void onStartTrackingTouch(SeekBar arg0) {
 
                     }
 
                     @Override
                     public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                         txtNumbers.setText(String.valueOf(arg1));
                         ModelRoot.getRoot().setWidth(String.valueOf(arg1));
                     }
                 });
 
                 dialogWidth.show();
             }
         });
 
         colors.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 final Dialog colorD = new Dialog(MyActivity.this);
                 colorD.setTitle("Select color");
                 colorD.setCancelable(true);
                 colorD.setContentView(R.layout.color_selector);
 
                 final SeekBar redSeek = (SeekBar) colorD.findViewById(R.id.seekBarRed);
                 final SeekBar greenSeek = (SeekBar) colorD.findViewById(R.id.seekBarGreen);
                 final SeekBar blueSeek = (SeekBar) colorD.findViewById(R.id.seekBarBlue);
                 final EditText hexColor = (EditText) colorD.findViewById(R.id.colorInHex);
                 redSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                     @Override
                     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 
                         TextView redText = (TextView) colorD.findViewById(R.id.redInNumbers);
                         redText.setText(String.valueOf(progress));
 
                         String r = Integer.toHexString(progress).length() < 2 ? 0 + Integer.toHexString(progress) : Integer.toHexString(progress);
                         String g = Integer.toHexString(greenSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(greenSeek.getProgress()) :
                                 Integer.toHexString(greenSeek.getProgress());
                         String b = Integer.toHexString(blueSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(blueSeek.getProgress()) :
                                 Integer.toHexString(blueSeek.getProgress());
                         String hexCode = r + g + b;
                         hexColor.setText("#" + hexCode.toUpperCase());
 
                         hexColor.setBackgroundColor(Color.parseColor("#" + hexCode));
                     }
 
                     @Override
                     public void onStartTrackingTouch(SeekBar seekBar) {
                     }
 
                     @Override
                     public void onStopTrackingTouch(SeekBar seekBar) {
                     }
                 });
                 greenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                     @Override
                     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                         TextView greenText = (TextView) colorD.findViewById(R.id.greenInNumbers);
                         greenText.setText(String.valueOf(progress));
 
                         String r = Integer.toHexString(redSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(redSeek.getProgress()) :
                                 Integer.toHexString(redSeek.getProgress());
                         String g = Integer.toHexString(progress).length() < 2 ? 0 + Integer.toHexString(progress) : Integer.toHexString(progress);
                         String b = Integer.toHexString(blueSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(blueSeek.getProgress()) :
                                 Integer.toHexString(blueSeek.getProgress());
                         String hexCode = r + g + b;
                         hexColor.setText("#" + hexCode.toUpperCase());
                         hexColor.setBackgroundColor(Color.parseColor("#" + hexCode));
                     }
 
                     @Override
                     public void onStartTrackingTouch(SeekBar seekBar) {
                     }
 
                     @Override
                     public void onStopTrackingTouch(SeekBar seekBar) {
                     }
                 });
                 blueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                     @Override
                     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                         TextView blueText = (TextView) colorD.findViewById(R.id.blueInNumbers);
                         blueText.setText(String.valueOf(progress));
 
                         String r = Integer.toHexString(redSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(redSeek.getProgress()) :
                                 Integer.toHexString(redSeek.getProgress());
                         String g = Integer.toHexString(greenSeek.getProgress()).length() < 2 ? 0 + Integer.toHexString(greenSeek.getProgress()) :
                                 Integer.toHexString(greenSeek.getProgress());
                         String b = Integer.toHexString(progress).length() < 2 ? 0 + Integer.toHexString(progress) :
                                 Integer.toHexString(progress);
                         String hexCode = r + g + b;
                         hexColor.setText("#" + hexCode.toUpperCase());
                         hexColor.setBackgroundColor(Color.parseColor("#" + hexCode));
                     }
 
                     @Override
                     public void onStartTrackingTouch(SeekBar seekBar) {
                     }
 
                     @Override
                     public void onStopTrackingTouch(SeekBar seekBar) {
                     }
                 });
 
                 Button selectColor = (Button) colorD.findViewById(R.id.selectB);
                 selectColor.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
 
                         String color = hexColor.getText().toString();
                         if (color.length() > 5 && color.length() < 8) {
                             Pattern p = Pattern.compile("[0-9a-fA-F]{6}");
                             Matcher m = p.matcher(color);
                             if (m.find() && m.group().length() == 6) {
                                 if (color.length() == 6) {
                                     color = "#" + color;
                                 }
                                 ModelRoot.getRoot().setColor(color);
                                 Toast.makeText(MyActivity.this, "Color selected", 5000).show();
                                 setResult(1, getIntent());
                                 colorD.dismiss();
                             } else {
                                 final AlertDialog.Builder alert = new AlertDialog.Builder(MyActivity.this);
                                 alert.setTitle("Error!");
                                 alert.setMessage("Please enter a valid hexadecimal color representation");
                                 alert.setCancelable(true);
                                 alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                     }
                                 });
                                 alert.show();
                             }
                         } else {
                             final AlertDialog.Builder alert = new AlertDialog.Builder(MyActivity.this);
                             alert.setTitle("Error!");
                             alert.setMessage("Please enter a valid hexadecimal color representation");
                             alert.setCancelable(true);
                             alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                 }
                             });
                             alert.show();
                         }
                     }
                 });
 
 
                 hexColor.clearFocus();
 
                 hexColor.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         hexColor.setText("");
                     }
                 });
 
                 hexColor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                     @Override
                     public void onFocusChange(View v, boolean hasFocus) {
                         String color = hexColor.getText().toString();
                         hexColor.setText("#" + hexColor.getText().toString());
                         if (color.length() == 6) {
                             Pattern p = Pattern.compile("[0-9a-fA-F]+");
                             Matcher m = p.matcher(color);
                             if (m.find() && m.group().length() == 6) {
 
                                 String colorPart = m.group();
                                 String red = colorPart.substring(0, 1);
                                 String green = colorPart.substring(2, 3);
                                 String blue = colorPart.substring(4, 5);
 
                                 redSeek.setProgress(Integer.parseInt(red, 16));
                                 greenSeek.setProgress(Integer.parseInt(green, 16));
                                 blueSeek.setProgress(Integer.parseInt(blue, 16));
 
                                 ModelRoot.getRoot().setColor(color);
                                 setResult(1, getIntent());
                             }
                         }
                     }
                 });
 
                 final ImageButton whiteB = (ImageButton) colorD.findViewById(R.id.whiteB);
                 final ImageButton redB = (ImageButton) colorD.findViewById(R.id.redB);
                 final ImageButton yellowB = (ImageButton) colorD.findViewById(R.id.yellowB);
                 final ImageButton greenB = (ImageButton) colorD.findViewById(R.id.greenB);
                 final ImageButton blueB = (ImageButton) colorD.findViewById(R.id.blueB);
                 final ImageButton violetB = (ImageButton) colorD.findViewById(R.id.violetB);
                 final ImageButton greyB = (ImageButton) colorD.findViewById(R.id.greyB);
                 final ImageButton blackB = (ImageButton) colorD.findViewById(R.id.blackB);
 
                 whiteB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(255);
                         greenSeek.setProgress(255);
                         blueSeek.setProgress(255);
                         hexColor.setText("#FFFFFF");
                         hexColor.setTextColor(Color.BLACK);
                         hexColor.setBackgroundColor(Color.parseColor("#FFFFFF"));
                     }
                 });
 
                 redB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(255);
                         greenSeek.setProgress(0);
                         blueSeek.setProgress(0);
                         hexColor.setText("#FF0000");
                         hexColor.setBackgroundColor(Color.parseColor("#FF0000"));
                     }
                 });
 
                 yellowB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(255);
                         greenSeek.setProgress(255);
                         blueSeek.setProgress(0);
                         hexColor.setText("#FFFF00");
                         hexColor.setTextColor(Color.BLACK);
                         hexColor.setBackgroundColor(Color.parseColor("#FFFF00"));
                     }
                 });
 
                 greenB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(0);
                         greenSeek.setProgress(255);
                         blueSeek.setProgress(0);
                         hexColor.setText("#00FF00");
                         hexColor.setTextColor(Color.BLACK);
                         hexColor.setBackgroundColor(Color.parseColor("#00FF00"));
                     }
                 });
 
                 blueB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(0);
                         greenSeek.setProgress(0);
                         blueSeek.setProgress(255);
                         hexColor.setText("#0000FF");
                         hexColor.setBackgroundColor(Color.parseColor("#0000FF"));
                     }
                 });
 
                 violetB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(204);
                         greenSeek.setProgress(0);
                         blueSeek.setProgress(255);
                         hexColor.setText("#CC00FF");
                         hexColor.setBackgroundColor(Color.parseColor("#CC00FF"));
                     }
                 });
 
                 greyB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(103);
                         greenSeek.setProgress(103);
                         blueSeek.setProgress(103);
                         hexColor.setText("#676767");
                         hexColor.setBackgroundColor(Color.parseColor("#676767"));
                     }
                 });
 
                 blackB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         redSeek.setProgress(0);
                         greenSeek.setProgress(0);
                         blueSeek.setProgress(0);
                         hexColor.setTextColor(Color.WHITE);
                         hexColor.setText("#000000");
                         hexColor.setBackgroundColor(Color.parseColor("#000000"));
                     }
                 });
                 colorD.show();
                 hexColor.clearFocus();
             }
         });
 
 
         settings.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent i = new Intent(context, SettingsActivity.class);
                 startActivityForResult(i, 3);
 
             }
         });
 
         view = new SurfaceViewDraw(this);
         view.setBackgroundColor(Color.WHITE);
         LinearLayout layout = (LinearLayout) findViewById(R.id.forSurfaceViewDraw);
         layout.addView(view);
     }
 
     //Functionality
     @Override
     protected void onResume() {
         super.onResume();
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         if (resultCode == 3) {
             start.dismiss();
         }
 
         if (resultCode == 4) {
             view.setBackgroundDrawable(Drawable.createFromPath(ModelRoot.getRoot().getFilePath()));
         }
     }
 
     private void showToastMessage(String msg) {
 
         Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
 
         toast.show();
 
     }
 
     public void taptoshare() {
 //        View content = findViewById(R.id.SurfaceViewLayout);
         View content = MyActivity.view;
         content.setDrawingCacheEnabled(true);
         Bitmap bitmap = content.getDrawingCache();
         File file = new File(getExternalCacheDir(), "image.jpg");
         try {
             file.createNewFile();
             FileOutputStream ostream = new FileOutputStream(file);
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
             ostream.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
         Intent share = new Intent(Intent.ACTION_SEND);
         share.setType("image/jpeg");
 
         share.putExtra(Intent.EXTRA_STREAM,
                 Uri.parse("file:///" + file.getAbsolutePath()));
 
         startActivity(Intent.createChooser(share, "Share Image"));
 
     }
 
     public void taptosave() {
 
         final Dialog saveD = new Dialog(this);
         saveD.setTitle("Choose name of file");
         saveD.setContentView(R.layout.save_dialog);
 
         Button okB = (Button) saveD.findViewById(R.id.okB);
         Button cancelB = (Button) saveD.findViewById(R.id.cancelB3);
 
         okB.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 EditText fileName = (EditText) saveD.findViewById(R.id.fileName);
                 String imageName = fileName.getText().toString();
                final File file = new File(getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getPath() + imageName + ".jpg");
                 if (file.exists()) {
                     final Dialog check = new Dialog(MyActivity.this);
                     check.setTitle("File with same name exists");
                     check.setContentView(R.layout.check);
 
                     Button y = (Button) check.findViewById(R.id.yes);
                     Button n = (Button) check.findViewById(R.id.no);
 
                     y.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             file.delete();
                             try {
                                 FileOutputStream fOut = new FileOutputStream(file);
 
                                 Bitmap bitmap = view.getDrawingCache();
                                 bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                                 fOut.flush();
                                 fOut.close();
 
 //                    MediaStore.Images.Media.insertImage(getContentResolver(), file.getPath(), imageName, imageName);
                                 Toast.makeText(MyActivity.this, "Your file is saved to " + file.getAbsolutePath(), 5000).show();
                             } catch (FileNotFoundException e) {
                                 e.printStackTrace();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                             check.dismiss();
                         }
                     });
 
                     n.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             check.dismiss();
                             taptosave();
                         }
                     });
 
                     check.show();
                 } else {
                     try {
                         FileOutputStream fOut = new FileOutputStream(file);
 
                         Bitmap bitmap = view.getDrawingCache();
                         bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                         fOut.flush();
                         fOut.close();
 
 //                    MediaStore.Images.Media.insertImage(getContentResolver(), file.getPath(), imageName, imageName);
                         Toast.makeText(MyActivity.this, "Your file is saved to " + file.getAbsolutePath(), 5000).show();
                     } catch (FileNotFoundException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
 
                 saveD.dismiss();
             }
         });
 
         cancelB.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 saveD.dismiss();
             }
         });
 
         saveD.show();
     }
 }
