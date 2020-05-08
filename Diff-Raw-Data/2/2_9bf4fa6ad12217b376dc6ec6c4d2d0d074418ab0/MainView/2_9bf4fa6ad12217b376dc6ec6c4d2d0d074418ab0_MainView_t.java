 package gov.cdc;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import gov.cdc.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.view.WindowManager;
 import android.view.Window;
 import android.widget.Toast;
 import android.view.View.OnClickListener;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.text.method.ScrollingMovementMethod;
 
 
 public class MainView extends Activity {
 	
 
     int position = 0;
     int farthestPositionReached = 0;
     public static PTTController controller;
     
     //Make a public static variable for the controller.history
     public static ArrayList<PTTHistoryItem> mHistory;
     
     private String headerImagePath;
     private String headerImage;
     private ImageButton button1; 
     private ImageButton button2; 
     private ImageButton button3; 
     private ImageButton button4; 
     private ImageButton button5;
     private Button footnotesButton;
     private String answerChosenNodeId;
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         
         /**
          * Display the EULA.  Eula.show() will only show the EULA if
          * the user has not previously agreed to the current version's EULA.
          */
         new Eula(this).show();
         
         // TODO: Answer me this: Are we creating an entirely new controller here? Or wait, is this our only controller?
         controller = new PTTController(this.getApplicationContext());
         
         //Make a new member variable for the history. We'll use this to update it. Or maybe not needed.
         mHistory = controller.history;
         
         
         headerImage = controller.getHeaderImageForNodeNumber(0);
 
         
         
         // Hide the title bar
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         setContentView(R.layout.main);
                 
         PTTNode currentNode = controller.currentNode;
         
         //Get image path and put image into node header image
         ImageView headerImageView = new ImageView(this);
         headerImageView = (ImageView)findViewById(R.id.nodeHeaderImage);
         headerImagePath = controller.getHeaderImageForNodeNumber(currentNode.getId());
         //Log.d("IMAGE", headerImagePath);
         String imageString = "drawable/" + currentNode.getPathToHeaderImage();
         //Log.d("IMAGESTRING", imageString);
         int imageResource = getResources().getIdentifier(imageString,null,getPackageName());
         Drawable image = getResources().getDrawable(imageResource);
         headerImageView.setImageDrawable(image);
         
         
         //Get question text and put it on the screen
         TextView tv = new TextView(this);
     	tv = (TextView)findViewById(R.id.questionTextView);
     	// set the TextView as scrollable, just in case it's too big to fit
     	tv.setMovementMethod(new ScrollingMovementMethod()); 
     	tv.setText(currentNode.getQuestion());
     	
     	//Get answers and put them on the buttons.
     	updateButtons();
 
 
         button1 = (ImageButton)findViewById(R.id.tabBarButton1);
         button2 = (ImageButton)findViewById(R.id.tabBarButton2);
         button3 = (ImageButton)findViewById(R.id.tabBarButton3);
         button4 = (ImageButton)findViewById(R.id.tabBarButton4);
         button5 = (ImageButton)findViewById(R.id.tabBarButton5);
 
 
 
 
         button1.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
             	Log.d("NAV","button1");
             	navigateBackToPreviousNode();
 
             }
         });
 
         button2.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
             	Log.d("NAV","button2");
             	navigateForwardToNode();
             }
         });
 
         button3.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
             	Log.d("NAV","button3");
             	//I'm putting the logic all right here. I don't see (yet) any reason to put it in function
             	
             	int farthestNodeId = mHistory.get(mHistory.size()-1).getAnswerChosen().getNodeId();
             	Log.d("farthestNodeId",Integer.toString(farthestNodeId));
             	
             	position = farthestPositionReached;
             	controller.setCurrentNode(farthestNodeId);
             	navigateToAnotherNode(farthestNodeId);
             	
             }
         });
 
         
         final Context context = this;
         
         button5.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
 
             	Log.d("NAV","button5");
                 controller.logHistoryItems();
                 Intent historyViewIntent = new Intent(context, HistoryView.class);
         		startActivityForResult(historyViewIntent, 1);
                 //startActivityForResult(historyViewIntent,1);
             }
         });
         
         // TODO: maybe turn this into an intializeNavButtons() method??
         disableAllNavButtons();
         updateNavButtons();
 
         Button footnotesButton = (Button) findViewById(R.id.footnotesButton);
         footnotesButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view) {
         		Intent intent = new Intent(context, FootnotesView.class);
         		startActivity(intent);
         	}
         });
 
         /**
          * Assign an action to the infoButton to display the info page
          */
         ImageButton infoButton = (ImageButton) findViewById(R.id.infoButton);
         infoButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View arg0) {
         		Intent intent = new Intent(context, InfoView.class);
         		startActivity(intent);
         	}
         });
         
         /**
          * Assign an action to the helpButton to display the help page
          */
         ImageButton helpButton = (ImageButton) findViewById(R.id.helpButton);
         helpButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View arg0) {
         		Intent intent = new Intent(context, HelpView.class);
         		startActivity(intent);
         	}
         });
         
     }
 
 
     public void navigateToAnotherNode(int nodeId) {
     	
     	if (position > farthestPositionReached) {
     		farthestPositionReached = position;
     	}
     	
     	
     	
         //Get question text and put it on the screen
         TextView tv = (TextView)findViewById(R.id.questionTextView);
         tv.setText(controller.currentNode.getQuestion());
         
       //Get image path and put image into node header image
         ImageView headerImageView = (ImageView)findViewById(R.id.nodeHeaderImage);
         
         headerImagePath = controller.getHeaderImageForNodeNumber(nodeId);
         //Log.d("IMAGE", headerImagePath);
         String imageString = "drawable/" + controller.currentNode.getPathToHeaderImage();
         //Log.d("IMAGESTRING", imageString);
         int imageResource = getResources().getIdentifier(imageString,null,getPackageName());
         Drawable image = getResources().getDrawable(imageResource);
         headerImageView.setImageDrawable(image);
 
         updateButtons();
         //enableAllNavButtons();
         updateNavButtons();
         
         
         Log.d("navToNutherNode,CurrNodeId",Integer.toString(controller.currentNode.getId()));
         
     }
 
     
 
     public void disableAllNavButtons() {
     	button1.setEnabled(false);
     	button1.setImageResource(R.drawable.nav_button_back);
     	button2.setEnabled(false);
     	button2.setImageResource(R.drawable.nav_button_next_disabled);
     	button3.setEnabled(false);
     	button3.setImageResource(R.drawable.nav_button_back_to_last_disabled);
     	button4.setEnabled(false);
     	button4.setImageResource(R.drawable.nav_button_restart_disabled);
     	button5.setEnabled(false);
     	button5.setImageResource(R.drawable.nav_button_review_disabled);
     }
     
     public void enableAllNavButtons() {
     	button1.setEnabled(true);
     	button1.setImageResource(R.drawable.nav_button_back);
     	button2.setEnabled(true);
     	button2.setImageResource(R.drawable.nav_button_next);
     	button3.setEnabled(true);
     	button3.setImageResource(R.drawable.nav_button_back_to_last);
     	button4.setEnabled(true);
     	button4.setImageResource(R.drawable.nav_button_restart);
     	button5.setEnabled(true);
     	button5.setImageResource(R.drawable.nav_button_review);
     }
     
     
     public void updateNavButtons() {
     	Log.d("updateNavButtons","position: " + position + ", farthest: " + farthestPositionReached);
     	
     	if (position > 0) {
     		button1.setEnabled(true);
     		button1.setImageResource(R.drawable.nav_button_back);
     		button4.setEnabled(true);
         	button4.setImageResource(R.drawable.nav_button_restart);
         	button5.setEnabled(true);
         	button5.setImageResource(R.drawable.nav_button_review);
     	}
     	
     	//this is wrong. what if user goes all the way back to beginning? still need active buttons
     	if (farthestPositionReached > 0) {
     		button5.setEnabled(true);
         	button5.setImageResource(R.drawable.nav_button_review);
     	}
     	
     	
     	
     	if (position < farthestPositionReached) {
     		//activate the forward buttons
     		button2.setEnabled(true);
         	button2.setImageResource(R.drawable.nav_button_next);
     		button3.setEnabled(true);
         	button3.setImageResource(R.drawable.nav_button_back_to_last);
     	} else if (position == farthestPositionReached) {
     		button2.setEnabled(false);
         	button2.setImageResource(R.drawable.nav_button_next_disabled);
         	button3.setEnabled(false);
         	button3.setImageResource(R.drawable.nav_button_back_to_last_disabled);
     	}
     	
     	if (position == 0) {
     		button1.setEnabled(false);
         	button1.setImageResource(R.drawable.nav_button_back_disabled);
         	
     	}
     }
     
     
     
     public void navigateBackToPreviousNode() {
     	Log.d("Position before going back",Integer.toString(position));
     	Log.d("mHistory size:",Integer.toString(mHistory.size()));
     	
     	int prevNodeId = mHistory.get(position-1).getNode().getId();
  
     	Log.d("Question for prev question:",controller.getQuestionForNodeNumber(prevNodeId));
     	Log.d("Navigating back to node:",Integer.toString(prevNodeId));
     	
     	
     	position--;
     	controller.setCurrentNode(prevNodeId);
     	navigateToAnotherNode(prevNodeId);
     }
     
     
     public void navigateForwardToNode() {
     	Log.d("position",Integer.toString(position));
     	Log.d("mHistory size:",Integer.toString(mHistory.size()));
     	
     	int nextNodeId = mHistory.get(position).getAnswerChosen().getNodeId();
     	
     	Log.d("Question for next question:",controller.getQuestionForNodeNumber(nextNodeId));
     	Log.d("Navigating forward to node:",Integer.toString(mHistory.get(position).getAnswerChosen().getNodeId()));
     	
     	
     	position++;
     	controller.setCurrentNode(nextNodeId);
     	navigateToAnotherNode(nextNodeId);
     	
     }
     
 
     public void restart(View view) {
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setCancelable(true);
         builder.setIcon(R.drawable.nav_button_restart);
         builder.setTitle("Do you really want to restart?");
         builder.setInverseBackgroundForced(true);
         builder.setPositiveButton("Yes, restart", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 Toast.makeText(getBaseContext(),
                         "Restarting", Toast.LENGTH_SHORT).show();
                 position=0;
                 farthestPositionReached = 0;
                 controller.setCurrentNode(0);
                 mHistory.clear();
                 navigateToAnotherNode(0);
                 disableAllNavButtons();
 
             }
         });
         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     
     /**
      * Update the number, strings, and actions of the buttons to those
      * specified by the current node.
      */
     private void updateButtons() {
         ArrayList<PTTAnswer> answers = controller.currentNode.getAnswers();
     	Button b0 = (Button)findViewById(R.id.answerButton0);
     	Button b1 = (Button)findViewById(R.id.answerButton1);
     	//Log.d("DEBUG", "About to make buttons");
     	//Log.d("DEBUG", "Answers size is " + answers.size());
     	switch (answers.size()) {
     	case 0:
     		b0.setVisibility(View.INVISIBLE);
     		b1.setVisibility(View.INVISIBLE);
     		break;
     	case 1:
     		b0.setVisibility(View.VISIBLE);
     		b1.setVisibility(View.INVISIBLE);
     		break;
     	default:
     		b0.setVisibility(View.VISIBLE);
     		b1.setVisibility(View.VISIBLE);
     	}
     	
     	
     	
     	if (answers.size() > 0) {
     		String s = answers.get(0).answer;
     		
     		
     		
     		
     		b0.setText(s);
 			final int answer0Node = answers.get(0).nodeId;
 			//Log.d("Making button", "b0, '" + s + "', points to node " + answer0Node + ".  Current node is " + controller.currentNode.getId());
 			b0.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View view) {
                     //Log.d("GOTO Node:",Integer.toString(answer0Node));
                     
                     //get the current node and store the history item based on it
                     //Log.d("HISTLOG", "About to Log something");
                     PTTNode n = controller.getCurrentNode();
                     controller.storeHistoryItem(n, n.getAnswers().get(0));
                     controller.setCurrentNode(answer0Node);
                     position++;
                     navigateToAnotherNode(answer0Node);
                    }
             });
     	} 
     	if (answers.size() == 2) {
             String s1 = answers.get(1).answer;
             
             
             
             b1.setText(s1);
             final int answer1Node = answers.get(1).nodeId;
             //Log.d("Making button", "b1, " + s1 + ", points to node " + answer1Node + ".  Current node is " + controller.currentNode.getId());
             b1.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View view) {
                     //Log.d("GOTO Node:",Integer.toString(answer1Node));
                     
                     //get the current node and store the history item based on it
                     //Log.d("HISTLOG", "About to Log something");
                     PTTNode n = controller.getCurrentNode();
                     controller.storeHistoryItem(n, n.getAnswers().get(1));
                     controller.setCurrentNode(answer1Node);
                     position++;
                     navigateToAnotherNode(answer1Node);
                 }
             });
     	}
     	
     	
    	footnotesButton = (Button)findViewById(R.id.footnotesButton);
     	if (controller.currentNode.getFootnotes().size() > 0) {
     		footnotesButton.setEnabled(true);
     	} else {
     		footnotesButton.setEnabled(false);
     	}
     	
     	
     	adjustButtonColorsBasedOnHistory(answers);
     }
     
     
     public void adjustButtonColorsBasedOnHistory(ArrayList<PTTAnswer> answers) {
     	// set buttons appearance if it has been selected
     	Button b0 = (Button)findViewById(R.id.answerButton0);
     	Button b1 = (Button)findViewById(R.id.answerButton1);
     	b0.setBackgroundResource(R.drawable.button_regular);
     	b1.setBackgroundResource(R.drawable.button_regular);
     	
     	
     	if (position < farthestPositionReached) {
     		Log.d("BUTTON DECOR","we should decorate a button");
     		Log.d("answer of button to decorate:",mHistory.get(position).getAnswerChosen().getAnswer());
     		answerChosenNodeId = Integer.toString(mHistory.get(position).getAnswerChosen().getNodeId());
     		switch (answers.size()) {
         	case 0:
         		break;
         	case 1:
         		if (answers.get(0).getNodeId() == mHistory.get(position).getAnswerChosen().getNodeId()) {
     				//b0.setText(b0.getText()+ "  --  The Chosen One!");
     				b0.setBackgroundResource(R.drawable.button_down);
     			} else {
     				//b0.setBackgroundColor(android.graphics.Color.LTGRAY);
     				b0.setBackgroundResource(R.drawable.button_regular);
     			}
         		break;
         	default:
         		if (answers.get(0).getNodeId() == mHistory.get(position).getAnswerChosen().getNodeId()) {
         			//b0.setText(b0.getText()+ "  --  The Chosen One!");
         			b0.setBackgroundResource(R.drawable.button_down);
     			} else if (answers.get(1).getNodeId() == mHistory.get(position).getAnswerChosen().getNodeId()) {
         			//b1.setText(b1.getText()+ "  --  The Chosen One!");
         			b1.setBackgroundResource(R.drawable.button_down);
     			}
         		break;
         	}
     	}
     	
     	
     }
     
     
     public PTTController getController() {
     	return controller;
     }
 
     
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	Log.d("CheckStartActivity","onActivityResult and resultCode = "+resultCode);
     	// TODO Auto-generated method stub
     	super.onActivityResult(requestCode, resultCode, data);
     	if(resultCode==1){
     		int positionToNavigateTo = data.getIntExtra("nodeToNavigateTo",0);
     		Log.d("RETURNED. GOTO POS: ",Integer.toString(positionToNavigateTo));
     		Log.d("Quest to goto: ", mHistory.get(positionToNavigateTo).getNode().getQuestion());
     		int nodeIdToGoTo = mHistory.get(positionToNavigateTo).getNode().getId();
     		Log.d("nodeIdToGoTo:",Integer.toString(nodeIdToGoTo));
     		
     		position = positionToNavigateTo;
         	controller.setCurrentNode(nodeIdToGoTo);
     		navigateToAnotherNode(nodeIdToGoTo);
     		
     		
     	}
     	else{
     		Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show();
     	}
     }
 
 }
