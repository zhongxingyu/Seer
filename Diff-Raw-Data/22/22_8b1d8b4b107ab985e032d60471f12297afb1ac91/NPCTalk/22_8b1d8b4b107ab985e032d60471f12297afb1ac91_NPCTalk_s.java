 package edu.bonn.mobilegaming.geoquest.mission;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.dom4j.Element;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.qeevee.gq.history.Actor;
 import com.qeevee.gq.history.TextItem;
 import com.qeevee.gq.history.TransitionItem;
 import com.qeevee.gq.xml.XMLUtilities;
 
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.Globals;
 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.ui.InteractionBlocker;
 import edu.bonn.mobilegaming.geoquest.ui.NPCTalkUI;
 import edu.bonn.mobilegaming.geoquest.ui.UIFactory;
 
 /**
  * Just a talking NPC. The NPC has a Image and text is based on dialogItems. The
  * text scrolls down on the screen and after each dialogItem the player have to
  * press a button to proceed.
  * 
  * @author Folker Hoffmann
  * @author Krischan Udelhoven
  */
 
 public class NPCTalk extends MissionActivity implements OnClickListener {
     private static final String TAG = "NPCTalk";
 
     /** button for the player to preceed / answer */
     private Button proceedButton;
     private String endButtonText;
     private String nextDialogButtonText;
     /** textView to show the dialog text */
     private TextView dialogText;
     /** all dialogItems. Is filled in the onCreate method */
     private LinkedList<DialogItem> dialogItems = new LinkedList<DialogItem>();
     private Iterator<DialogItem> dialogItemIterator;
     /** currentDialogItem */
     private DialogItem currItem;
     private CountDownTimer myCountDownTimer;
     /** Inside the scroolView is the textView so the text can scroll down/up */
     private ScrollView scrollView;
 
     private NPCTalkUI ui;
 
     /**
      * Called by the android framework when the mission is created. Setups the
      * View and calls the readXML method to get the dialogItems. The dialog
      * starts with the first dialogItem.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
 	Long start = System.currentTimeMillis();
 	super.onCreate(savedInstanceState);
 
 	// Create and initialize UI for this mission:
 	ui = UIFactory.getInstance().createUI(this);
 
 	proceedButton = (Button) findViewById(R.id.proceedButton);
 	dialogText = (TextView) findViewById(R.id.npctext);
 	scrollView = (ScrollView) findViewById(R.id.npc_scroll_view);
 	proceedButton.setOnClickListener(this);
 
 	readXML();
 	dialogItemIterator = dialogItems.iterator();
 	Log.d(TAG,
 	      "RuntimeMeasure "
 		      + (System.currentTimeMillis() - start)
 		      + " ms");
 	gotoNextDialogItem();
     }
 
     /**
      * Starts a DialogItem by setting up a myCountDownTimer and starting it.
      * 
      * @param i
      *            DialogItem to be started
      */
     private void startDialogItem(DialogItem i) {
 	dialogText.append(i.getFormattedSpeaker());
 	scrollView.fullScroll(View.FOCUS_DOWN);
 	int numParts = i.getNumParts();
 	final long milliseconds_per_part = 100;
 	long millisecondsInFuture = milliseconds_per_part
 		* (numParts + 1); // +1
 	myCountDownTimer = new NPCTalk.TalkCountDownTimer(millisecondsInFuture,
 		milliseconds_per_part, i);
 	myCountDownTimer.start();
     }
 
     /**
      * Iterates to the next DialogItem. If there is another DialogItem in the
      * list, startDialogItem(DialogItem) is called with it, otherwise nothing
      * happens and currItem is set to {@code null}.
      */
     private void gotoNextDialogItem() {
 	// we always show the next dialogItem starting with the first.
 
 	if (!dialogItemIterator.hasNext()) {
 	    new TransitionItem(this);
 	    finish(Globals.STATUS_SUCCEEDED);
 	    return;
 	}
 	currItem = dialogItemIterator.next();
 
 	myCountDownTimer = new CountDownTimer(500, 500) {
 
 	    @Override
 	    public void onTick(long millisUntilFinished) {
 		// Do nothing
 	    }
 
 	    @Override
 	    public void onFinish() {
 		startDialogItem(currItem);
 	    }
 	}; // Ende Countdowntimer
 
 	// Start interaction with player:
 	// First start audio file if given:
 	if (currItem.getAudioFilePath() != null)
 	    GeoQuestApp.playAudio(currItem.getAudioFilePath(),
 				  currItem.blocking);
 	// Then start displaying the text word by word:
 	myCountDownTimer.start();
     }
 
     private void setProceedButtonText(DialogItem curItem) {
 	if (!dialogItemIterator.hasNext()) {
 	    // this is the last dialogitem to show:
 	    proceedButton.setText(endButtonText);
 	} else {
 	    if (curItem.nextDialogButtonText != null)
 		// use specific nextDialogButtonText for current DialogItem:
 		proceedButton.setText(curItem.nextDialogButtonText);
 	    else
 		// use global nextDialogButtonText (might be specified in
 		// game.xml or default)
 		proceedButton.setText(this.nextDialogButtonText);
 	}
     }
 
     /**
      * Reads the xml file. Is called by the onCreate method. Builds the
      * dialogItemList.
      */
     @SuppressWarnings("unchecked")
     private void readXML() {
 
 	initImage();
 	initEndButtonText();
 
 	// Prepare endButtonText:
 	String ebt = mission.xmlMissionNode.attributeValue("endbuttontext");
 	if (ebt == null) {
 	    this.endButtonText = getText(R.string.button_text_proceed)
 		    .toString();
 	} else {
	    this.endButtonText = ebt;
 	}
 
 	// Adjust text size (default is 30sp):
 	String textsize = mission.xmlMissionNode.attributeValue("textsize");
 	if (textsize != null) {
 	    this.dialogText.setTextSize(Float.parseFloat(textsize));
 	}
 
 	// Prepare nextDialogButtonText:
 	String ndbt = mission.xmlMissionNode
 		.attributeValue("nextdialogbuttontext");
 	if (ndbt == null) {
 	    this.nextDialogButtonText = getText(R.string.button_text_proceed)
 		    .toString();
 	} else {
 	    this.nextDialogButtonText = ndbt;
 	}
 
 	// Load Dialog Items:
 	List<Element> dialogItemList = mission.xmlMissionNode
 		.selectNodes("./dialogitem");
 	for (Iterator<Element> e = dialogItemList.iterator(); e.hasNext();) {
 	    dialogItems.addLast(new DialogItem(e.next()));
 	}
     }
 
     private void initEndButtonText() {
 	// TODO Auto-generated method stub
 	
     }
 
     private void initImage() {
 	String relPathToImageFile = null;
 	try {
 	    relPathToImageFile = getMissionAttribute("image",
 						     XMLUtilities.OPTIONAL_ATTRIBUTE)
 		    .toString();
 	    ui.setImage(relPathToImageFile);
 	} catch (IllegalArgumentException iae) {
 	    Log.e(TAG,
 		  "The image attribute is optional. This exception SHOULD NOT occur!");
 	}
     }
 
     /**
      * On Click handler. If there is no dialogItem left the mission is over,
      * else the next dialogItem is shown.
      */
     public void onClick(View v) {
 	if (currItem == null) { // Am Ende
 	    finish(Globals.STATUS_SUCCEEDED);
 	    return;
 	} else {
 	    gotoNextDialogItem();
 	}
     }
 
     /**
      * Timer der Wort f�r Wort zu dem TextView hinzuf�gt
      */
     class TalkCountDownTimer extends CountDownTimer implements
 	    InteractionBlocker {
 	DialogItem dialogItem;
 
 	public TalkCountDownTimer(long millisInFuture,
 				  long countDownInterval,
 				  DialogItem item) {
 	    super(millisInFuture, countDownInterval);
 	    this.dialogItem = item;
 	    // block interaction on the NPCTalk using this Timer as Blocker
 	    // monitor:
 	    NPCTalk.this.blockInteraction(this);
 	    setProceedButtonText(item);
 	}
 
 	@Override
 	public void onFinish() {
 	    // Zur Sicherheit, da manchmal Wörter verschluckt werden (nicht
 	    // ausreichend genauer timer!)
 	    CharSequence next = dialogItem.getNextPart();
 	    while (next != null) {
 		NPCTalk.this.dialogText.append(next);
 		scrollView.fullScroll(View.FOCUS_DOWN);
 		next = dialogItem.getNextPart();
 	    }
 	    scrollView.fullScroll(View.FOCUS_DOWN);
 	    /*
 	     * Store history item. TODO: add more argument for image, audio and
 	     * thumbnail.
 	     */
 	    new TextItem(currItem.getText(), NPCTalk.this, Actor.NPC);
 	    // release blocked interaction on the NPCTalk using this Timer as
 	    // Blocker monitor:
 	    NPCTalk.this.releaseInteraction(this);
 	}
 
 	@Override
 	public void onTick(long millisUntilFinished) {
 	    CharSequence next = dialogItem.getNextPart();
 	    if (next != null) {
 		NPCTalk.this.dialogText.append(next);
 		scrollView.fullScroll(View.FOCUS_DOWN);
 	    }
 	}
 
     }
 
     public void onBlockingStateUpdated(boolean isBlocking) {
 	proceedButton.setEnabled(!isBlocking);
     }
 
 }
