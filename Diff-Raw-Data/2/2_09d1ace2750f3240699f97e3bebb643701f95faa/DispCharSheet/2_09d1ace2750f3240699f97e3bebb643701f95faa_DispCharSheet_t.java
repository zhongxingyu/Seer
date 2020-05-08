 package com.app.swcharsheet;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * @author Mike Rushford
  * @author Eric Martin
  * @version 20100809
  */
 
 
 public class DispCharSheet extends Activity {
 
 	characterSheet sheet;
 	
 	private int adjHP[], adjXP[];
 	
 	int displayFields[], SkillIds[];
 	
 	Dialog adjHPDialog, adjXPDialog;
 		
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.dispcharsheet);
         
         sheet = new characterSheet();
 
         adjHPDialog = new Dialog(this);
         adjXPDialog = new Dialog(this);
         
         adjHP = new int[] {0,0,0,0};
         adjXP = new int[] {0,0,0,0};
         
         displayFields = new int[] 
                        {R.id.dispCharName,   R.id.dispPlayerName,
        		 			R.id.dispCharClass,  R.id.dispCharSpecies,
        		 			R.id.dispCharLevel,  R.id.dispCharAge,
        		 			R.id.dispCharGender, R.id.dispCharHeight,
        		 			R.id.dispCharWeight, R.id.dispCharDestiny,
        		 			R.id.dispCharXP,	   R.id.dispCharToNextLevel,
        		 			R.id.dispSTR,		   R.id.dispDEX, R.id.dispINT,
        		 			R.id.dispCON, 	   R.id.dispWIS, R.id.dispCHA,
        		 			R.id.dispHP, 	 	   R.id.dispDmgThresh,
        		 			R.id.dispFort, 	   R.id.dispRef, R.id.dispWill,
        		 			R.id.dispCharSpeed,  R.id.dispCharInit,
        		 			R.id.dispCharPerc,   R.id.dispCharBaseAtk,
        		 			R.id.dispCharFP};
         SkillIds = new int[] 
                        {R.id.dispAcro, R.id.dispClimb, R.id.dispDec,   R.id.dispEnd,   R.id.dispGInfo,
                         R.id.dispInit, R.id.dispJump,  R.id.dispKnow1, R.id.dispKnow2, R.id.dispMech,
                         R.id.dispPerc, R.id.dispPers,  R.id.dispPil,   R.id.dispRide,  R.id.dispStlth,
                         R.id.dispSurv, R.id.dispSwim,  R.id.dispTreat, R.id.dispUCom,  R.id.dispUForce};	 				  
         drawScreen();
    	}
 	
 	/**
 	 * return Populate all data fields, as well as update XP progress bar
 	 */
 	public void drawScreen(){
 		int i;
 		for(i = 0; i < 28; i++){
         	populateField(displayFields[i], sheet.fieldStats[i]);
         }
         for(i = 0; i < 20; i++){
           	populateField(SkillIds[i], Integer.toString(sheet.getSkillBonus(i)));
           }	
 
 	        // progress bar stuff
 	        int screenwidth = 320;//(int) findViewById(R.id.ParentWindow).getWidth();
 	        //TODO: Get getWidth() ^^ to return something other than 0
 	
 	        View XPBar1 = (View) findViewById(R.id.XPBar1);
 	        View XPBar2 = (View) findViewById(R.id.XPBar2);
 	
 	        int xpb1 = (int) (screenwidth * sheet.XPProgress);
 	        int xpb2 = screenwidth-xpb1;
 	
 	        XPBar1.setLayoutParams(new LinearLayout.LayoutParams(xpb1,8));
 	        XPBar2.setLayoutParams(new LinearLayout.LayoutParams(xpb2,8));
 	}
 	
 	/**
 	 * return Populate a text field with a string
 	 * @param textID The ID of the TextView to be changed
 	 * @param value The string to be displayed
 	 */
 	public void populateField(int textID, String value){
 		TextView thisText = (TextView) findViewById(textID);
 		thisText.setText(value);
 	}
 	
 	/**
 	 * return Same as populateField(int textID, String value), except for Dialogs
 	 * @param dialog The dialog containing the TextViews
 	 * @param textID The ID of the TextView to be changed
 	 * @param value The string to be displayed
 	 */
 	public void populateDialogField(Dialog dialog, int textID, String value){
 		TextView thisText = (TextView) dialog.findViewById(textID);
 		thisText.setText(value);
 	}
 		
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.dispmenu, menu);
 	    return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.adjHP:
 	    	adjHPDialog.setContentView(R.layout.adjhp);
 	    	adjHPDialog.setTitle("Adjust HP");
 	    	adjHPDialog.show();
 	    	adjHPInteractions();
 	    	return true;
 	    case R.id.adjXP:
 	    	adjXPDialog.setContentView(R.layout.adjxp);
 	    	adjXPDialog.setTitle("Adjust XP");
 	    	adjXPDialog.show();
 	    	adjXPInteractions();
 	    	return true;
 	    case R.id.adjFP:
 	    	return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 
 	//TODO: See if adjHPInteractions and adjXPInteractions
 	//		can be tidied up
 	private void adjHPInteractions(){
 		
     	Button incHPButton1000 = (Button) adjHPDialog.findViewById(R.id.incHP1000);
     	Button incHPButton0100 = (Button) adjHPDialog.findViewById(R.id.incHP0100);
     	Button incHPButton0010 = (Button) adjHPDialog.findViewById(R.id.incHP0010);
     	Button incHPButton0001 = (Button) adjHPDialog.findViewById(R.id.incHP0001);
     	Button decHPButton1000 = (Button) adjHPDialog.findViewById(R.id.decHP1000);
     	Button decHPButton0100 = (Button) adjHPDialog.findViewById(R.id.decHP0100);
     	Button decHPButton0010 = (Button) adjHPDialog.findViewById(R.id.decHP0010);
     	Button decHPButton0001 = (Button) adjHPDialog.findViewById(R.id.decHP0001);
    		Button adjHPOK = (Button) adjHPDialog.findViewById(R.id.adjHPOK);
    		Button adjHPCANCEL = (Button) adjHPDialog.findViewById(R.id.adjHPCANCEL);
    		
     	incHPButton1000.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.incHP1000));
     	incHPButton0100.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.incHP0100));
     	incHPButton0010.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.incHP0010));
     	incHPButton0001.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.incHP0001));
     	decHPButton1000.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.decHP1000));
     	decHPButton0100.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.decHP0100));
     	decHPButton0010.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.decHP0010));
     	decHPButton0001.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.decHP0001));
     	adjHPOK.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.adjHPOK));
     	adjHPCANCEL.setOnClickListener(new ButtonClickListener(adjHPDialog, R.id.adjHPCANCEL));
 	}
 	
 	private void adjXPInteractions(){
 		
     	Button incXPButton1000 = (Button) adjXPDialog.findViewById(R.id.incXP1000);
     	Button incXPButton0100 = (Button) adjXPDialog.findViewById(R.id.incXP0100);
     	Button incXPButton0010 = (Button) adjXPDialog.findViewById(R.id.incXP0010);
     	Button incXPButton0001 = (Button) adjXPDialog.findViewById(R.id.incXP0001);
     	Button decXPButton1000 = (Button) adjXPDialog.findViewById(R.id.decXP1000);
     	Button decXPButton0100 = (Button) adjXPDialog.findViewById(R.id.decXP0100);
     	Button decXPButton0010 = (Button) adjXPDialog.findViewById(R.id.decXP0010);
     	Button decXPButton0001 = (Button) adjXPDialog.findViewById(R.id.decXP0001);
    		Button adjXPOK = (Button) adjXPDialog.findViewById(R.id.adjXPOK);
    		Button adjXPCANCEL = (Button) adjXPDialog.findViewById(R.id.adjXPCANCEL);
    		
     	incXPButton1000.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.incXP1000));
     	incXPButton0100.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.incXP0100));
     	incXPButton0010.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.incXP0010));
     	incXPButton0001.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.incXP0001));
     	decXPButton1000.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.decXP1000));
     	decXPButton0100.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.decXP0100));
     	decXPButton0010.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.decXP0010));
     	decXPButton0001.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.decXP0001));
     	adjXPOK.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.adjXPOK));
     	adjXPCANCEL.setOnClickListener(new ButtonClickListener(adjXPDialog, R.id.adjXPCANCEL));
 	}
 	
 	protected class ButtonClickListener implements OnClickListener {
 		
 		private Dialog dialog;
 		private int id;
 		 
         public ButtonClickListener(Dialog dialog, int which) {
                 this.dialog = dialog;
                 id=which;
         }
 
         public void onClick(View v) {
         	switch (id) {
         		case (R.id.incHP1000):
         			adjHP[0]++;
         			populateDialogField(adjHPDialog, R.id.adjHPText1000, Integer.toString(adjHP[0]));
         			break;
         		case (R.id.incHP0100):
             		adjHP[1]++;
             		populateDialogField(adjHPDialog, R.id.adjHPText0100, Integer.toString(adjHP[1]));
             		break;
             	case (R.id.incHP0010):
             		adjHP[2]++;
             		populateDialogField(adjHPDialog, R.id.adjHPText0010, Integer.toString(adjHP[2]));
             		break;
             	case (R.id.incHP0001):
             		adjHP[3]++;
             		populateDialogField(adjHPDialog, R.id.adjHPText0001, Integer.toString(adjHP[3]));
             		break;
             	case (R.id.decHP1000):
         			if(adjHP[0]>0){ adjHP[0]--;}
         			populateDialogField(adjHPDialog, R.id.adjHPText1000, Integer.toString(adjHP[0]));
         			break;
         		case (R.id.decHP0100):
         			if(adjHP[1]>0){ adjHP[1]--;}
             		populateDialogField(adjHPDialog, R.id.adjHPText0100, Integer.toString(adjHP[1]));
             		break;
             	case (R.id.decHP0010):
             		if(adjHP[2]>0){ adjHP[2]--;}
             		populateDialogField(adjHPDialog, R.id.adjHPText0010, Integer.toString(adjHP[2]));
             		break;
             	case (R.id.decHP0001):
             		if(adjHP[3]>0){ adjHP[3]--;}
             		populateDialogField(adjHPDialog, R.id.adjHPText0001, Integer.toString(adjHP[3]));
             		break;
             	case (R.id.adjHPOK):
             		sheet.adjHP(0-convertToInt(adjHP));
             		adjHP[0]=adjHP[1]=adjHP[2]=adjHP[3]=0;
             		dialog.dismiss();
             		TextView tempHP = (TextView) findViewById(R.id.dispHP);
            		tempHP.setTextColor(0xFFFF0000 + (int)(0x0000FFFF*sheet.getCharCurrentHP()/sheet.getCharTotalHP()));
             		drawScreen();
             		break;
         		case (R.id.adjHPCANCEL):
             		adjHP[0]=adjHP[1]=adjHP[2]=adjHP[3]=0;
         			dialog.dismiss();
         			break;
         		case (R.id.incXP1000):
         			adjXP[0]++;
         			populateDialogField(adjXPDialog, R.id.adjXPText1000, Integer.toString(adjXP[0]));
         			break;
         		case (R.id.incXP0100):
             		adjXP[1]++;
             		populateDialogField(adjXPDialog, R.id.adjXPText0100, Integer.toString(adjXP[1]));
             		break;
             	case (R.id.incXP0010):
             		adjXP[2]++;
             		populateDialogField(adjXPDialog, R.id.adjXPText0010, Integer.toString(adjXP[2]));
             		break;
             	case (R.id.incXP0001):
             		adjXP[3]++;
             		populateDialogField(adjXPDialog, R.id.adjXPText0001, Integer.toString(adjXP[3]));
             		break;
             	case (R.id.decXP1000):
         			if(adjXP[0]>0){ adjXP[0]--;}
         			populateDialogField(adjXPDialog, R.id.adjXPText1000, Integer.toString(adjXP[0]));
         			break;
         		case (R.id.decXP0100):
         			if(adjXP[1]>0){ adjXP[1]--;}
             		populateDialogField(adjXPDialog, R.id.adjXPText0100, Integer.toString(adjXP[1]));
             		break;
             	case (R.id.decXP0010):
             		if(adjXP[2]>0){ adjXP[2]--;}
             		populateDialogField(adjXPDialog, R.id.adjXPText0010, Integer.toString(adjXP[2]));
             		break;
             	case (R.id.decXP0001):
             		if(adjXP[3]>0){ adjXP[3]--;}
             		populateDialogField(adjXPDialog, R.id.adjXPText0001, Integer.toString(adjXP[3]));
             		break;
             	case (R.id.adjXPOK):
             		sheet.adjXP(convertToInt(adjXP));
             		adjXP[0]=adjXP[1]=adjXP[2]=adjXP[3]=0;
             		dialog.dismiss();
             		drawScreen();
             		break;
         		case (R.id.adjXPCANCEL):
             		adjXP[0]=adjXP[1]=adjXP[2]=adjXP[3]=0;
         			dialog.dismiss();
         			break;
         		default:
         			break;
         	}
         }
 	}
 	
 	private int convertToInt(int temp[]){
 		return temp[0]*1000+temp[1]*100+temp[2]*10+temp[3];
 	}
 }
