 package com.example.archery.start;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import com.example.archery.R;
 import com.example.archery.database.CMySQLiteOpenHelper;
 import com.example.archery.target.CRing;
 import com.example.archery.target.CTarget;
 
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Андрей
  * Date: 12.05.13
  * Time: 19:03
  * To change this template use File | Settings | File Templates.
  */
 public class TargetCreateActivity extends Activity implements DialogInterface.OnDismissListener{
 
    float prevDistanceFromCenter;
     float distanceFromCenter;
     CTarget target;
     private final int RING_ADD_DIALOG=1;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_target);
         if (savedInstanceState==null)
             target = new CTarget("",new Vector<CRing>(),0);
         else
         {
             distanceFromCenter = savedInstanceState.getFloat("distanceFromCenter");
             target = (CTarget) savedInstanceState.getSerializable("target");
         }
         ((CTargetPreview)findViewById(R.id.targetPreview)).setTarget(target);
     }
 
     public void addRing(View view)    {
         this.showDialog(RING_ADD_DIALOG);
     }
 
     @Override
     public void onBackPressed()  {
        distanceFromCenter = prevDistanceFromCenter;
         target.removeRing();
         findViewById(R.id.targetPreview).invalidate();
     }
 
     @Override
     public boolean onKeyLongPress(int keyCode, KeyEvent event)   {
         if (keyCode == KeyEvent.KEYCODE_BACK)
         {
             if (!target.isEmpty())
             {
                 target.addClosingRing();
                 CMySQLiteOpenHelper.getHelper(getBaseContext()).addTarget(target);
             }
             finish();
             return true;
         }
         return super.onKeyLongPress(keyCode, event);
     }
 
     @Override
     public Dialog onCreateDialog(int id, Bundle args)    {
         AlertDialog dialog = null;
         switch (id)
         {
             case RING_ADD_DIALOG:
             {
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 LayoutInflater inflater = getLayoutInflater();
                 builder.setTitle("Новое кольцо:");
                 View view = inflater.inflate(R.layout.ring_add_dialog, null);
                 builder.setView(view);
                 builder.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         AlertDialog alertDialog = (AlertDialog) dialog;
                         try {
                            prevDistanceFromCenter = distanceFromCenter;
                             distanceFromCenter += Float.parseFloat(((EditText) alertDialog.findViewById(R.id.distanceFromCenter)).getText().toString());
                         }   catch (NumberFormatException e)   {
                         }
                         int points = 0;
                         try {
                             points = Integer.parseInt(((EditText) alertDialog.findViewById(R.id.points)).getText().toString());
                         } catch (NumberFormatException e) {
                         }
                         target.addRing(new CRing(points,
                                 distanceFromCenter,
                                 ((CColoredSeekBar)((AlertDialog) dialog).findViewById(R.id.ringColor)).getSelectedColor()));
                     }
                 });
                 dialog = builder.create();
                 dialog.setOnDismissListener(this);
             }
         }
         return dialog;
     }
 
     @Override
     public void onDismiss(DialogInterface dialogInterface) {
         findViewById(R.id.targetPreview).invalidate();
     }
 
     @Override
     public void onSaveInstanceState(Bundle state)   {
         state.putFloat("distanceFromCenter",distanceFromCenter);
         state.putSerializable("target",target);
         super.onSaveInstanceState(state);
     }
 }
