 package com.banzz.lifecounter.activities;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.banzz.lifecounter.R;
 import com.banzz.lifecounter.utils.Utils;
 
 import java.util.Random;
 
 public class ToolboxMenuDialog extends DialogFragment {
     private Context mContext;
     private ToolBoxDialogListener mListener;
 
     public ToolboxMenuDialog(Context context, ToolBoxDialogListener listener) {
         mContext = context;
         mListener = listener;
     }
 
     public interface ToolBoxDialogListener {
         public void onPickAction(int action);
     }
 
	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	    LayoutInflater inflater = getActivity().getLayoutInflater();
 
 	    View view = inflater.inflate(R.layout.toolbox_menu, null);
 
 	    Button moreColorButton = (Button) view.findViewById(R.id.option_restart);
         moreColorButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mListener.onPickAction(R.id.action_restart);
                 dismiss();
             }
         });
 
         Button coinButton = (Button) view.findViewById(R.id.option_coin);
         coinButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 flip_coin();
             }
         });
 
         Button DiceButton = (Button) view.findViewById(R.id.option_dice);
         DiceButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 roll_dice(20);
             }
         });
 
         Button loadButton = (Button) view.findViewById(R.id.option_load);
         loadButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mListener.onPickAction(R.id.action_load);
                 dismiss();
             }
         });
 
         Button editButton = (Button) view.findViewById(R.id.option_edit);
         editButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mListener.onPickAction(R.id.action_edit);
                 dismiss();
             }
         });
 
         Button rotateButton = (Button) view.findViewById(R.id.option_rotate);
         rotateButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mListener.onPickAction(R.id.action_flip);
                 dismiss();
             }
         });
 		
 	    builder.setView(view);
 	    return builder.create();
 	}
 
     private void roll_dice(int max_value) {
         Random random = new Random();
         //Both random and % seems to like negative values, hence the double trick...
         int dice_value = (((random.nextInt() % max_value) + max_value) % max_value) + 1;
         Toast dice_rolled = Toast.makeText(mContext, dice_value + (dice_value == 1 ? " :(" : dice_value == max_value ? "!!" : ""), Toast.LENGTH_SHORT);
         dice_rolled.show();
     }
 
 
     private void flip_coin() {
         Random random = new Random();
         Toast coin_tossed = Toast.makeText(mContext, random.nextInt() % 2 == 0 ? "HEADS" : "TAILS", Toast.LENGTH_SHORT);
         coin_tossed.show();
     }
 }
