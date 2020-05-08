 package com.weigreen.poker;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.content.Context;
 
 import com.weigreen.ncu.tfh.bridge.Card;
 import com.weigreen.ncu.tfh.bridge.TFHBridgeDataCard;
 import com.weigreen.ncu.tfh.bridge.TFHBridgeDataGodCard;
 import com.weigreen.ncu.tfh.bridge.TFHBridgeDataNewPlayer;
 import com.weigreen.ncu.tfh.bridge.TFHBridgeDataRoom;
 import com.weigreen.ncu.tfh.bridge.TFHBridgeMain;
 import com.weigreen.ncu.tfh.communication.TFHComm;
 
 import java.util.ArrayList;
 
 /**
  * Created by roy on 2013/6/10.
  */
 public class RoomActivity extends Activity {
 
     private ImageButton spadeButton;
     private ImageButton heartButton;
     private ImageButton diamondButton;
     private ImageButton clubButton;
 
     private Button oneButton;
     private Button twoButton;
     private Button threeButton;
     private Button fourButton;
     private Button fiveButton;
     private Button sixButton;
     private Button sevenButton;
     private Button eightButton;
     private Button nineButton;
 
     private Button callButton;
     private Button passButton;
 
     private ImageButton card_one;
     private ImageButton card_two;
     private ImageButton card_three;
     private ImageButton card_four;
     private ImageButton card_five;
     private ImageButton card_six;
     private ImageButton card_seven;
     private ImageButton card_eight;
     private ImageButton card_nine;
     private ImageButton card_ten;
     private ImageButton card_eleven;
     private ImageButton card_twelve;
     private ImageButton card_thirteen;
 
     private ImageButton opposite;
     private ImageButton left;
     private ImageButton right;
     private ImageButton home;
 
     private short suit;
     private short heap;
 
     private Handler handler = new Handler();
 
     //private
     private TFHClientRoomSocket roomSocket;
 
     private int port;
 
     private short inRoomPlayer = 0;
 
     private short myPlayerID = 100;
 
     private Card[] myCardArray;
     private boolean[] myCardUsed;
 
     private ArrayList<ImageButton> handCradArray;
 
     private ArrayList<ImageButton> callSuitArray;
 
     private ArrayList<Button> callButtonArray;
 
     private short callingSuit;
 
     private short callingNumber;
 
     private short wantCallSuit;
 
     private short wantCallNumber;
 
     private short kingSuit;
 
     private PowerManager powerManager;
     private WakeLock wakeLock;
 
     private short[] teamScore = new short[2];
 
     private short[] tableCard = new short[4];
 
     private short initPlayer;
 
     private short nowPlayer;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
         wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "BackLight");
         this.changeViewToWaiting();
 
         port = getIntent().getIntExtra("port", 0);
         roomSocket = new TFHClientRoomSocket(port, this);
         roomSocket.start();
     }
 
     private void changeViewToWaiting() {
         setContentView(R.layout.activity_table_waiting);
     }
 
 
     /**
      * set Call UI
      */
     private void setCallUI() {
 
         setContentView(R.layout.activity_room_call);
         handCradArray = new ArrayList<ImageButton>();
         callSuitArray = new ArrayList<ImageButton>();
         callButtonArray = new ArrayList<Button>();
 
 
         spadeButton = (ImageButton)findViewById(R.id.spadeButton);
         spadeButton.setOnClickListener(buttonSuitOnClick);
         heartButton = (ImageButton)findViewById(R.id.heartButton);
         heartButton.setOnClickListener(buttonSuitOnClick);
         diamondButton = (ImageButton)findViewById(R.id.diamondButton);
         diamondButton.setOnClickListener(buttonSuitOnClick);
         clubButton = (ImageButton)findViewById(R.id.clubButton);
         clubButton.setOnClickListener(buttonSuitOnClick);
 
         callSuitArray.add(spadeButton);
         callSuitArray.add(heartButton);
         callSuitArray.add(diamondButton);
         callSuitArray.add(clubButton);
 
 
         oneButton = (Button)findViewById(R.id.oneButton);
         oneButton.setOnClickListener(buttonNumberOnClick);
         twoButton = (Button)findViewById(R.id.twoButton);
         twoButton.setOnClickListener(buttonNumberOnClick);
         threeButton = (Button)findViewById(R.id.threeButton);
         threeButton.setOnClickListener(buttonNumberOnClick);
         fourButton = (Button)findViewById(R.id.fourButton);
         fourButton.setOnClickListener(buttonNumberOnClick);
         fiveButton = (Button)findViewById(R.id.fiveButton);
         fiveButton.setOnClickListener(buttonNumberOnClick);
         sixButton = (Button)findViewById(R.id.sixButton);
         sixButton.setOnClickListener(buttonNumberOnClick);
         sevenButton = (Button)findViewById(R.id.sevenButton);
         sevenButton.setOnClickListener(buttonNumberOnClick);
         eightButton = (Button)findViewById(R.id.eightButton);
         eightButton.setOnClickListener(buttonNumberOnClick);
         nineButton = (Button)findViewById(R.id.nineButton);
         eightButton.setOnClickListener(buttonNumberOnClick);
 
         callButtonArray.add(oneButton);
         callButtonArray.add(twoButton);
         callButtonArray.add(threeButton);
         callButtonArray.add(fourButton);
         callButtonArray.add(fiveButton);
         callButtonArray.add(sixButton);
         callButtonArray.add(sevenButton);
         callButtonArray.add(eightButton);
         callButtonArray.add(eightButton);
 
 
 
         callButton = (Button)findViewById(R.id.callButton);
         callButton.setOnClickListener(buttonFunctionOnClick);
         passButton = (Button)findViewById(R.id.passButton);
         passButton.setOnClickListener(buttonFunctionOnClick);
 
         card_one = (ImageButton)findViewById(R.id.card_one);
         setClickableFalse(card_one);
         card_two = (ImageButton)findViewById(R.id.card_two);
         setClickableFalse(card_two);
         card_three = (ImageButton)findViewById(R.id.card_three);
         setClickableFalse(card_three);
         card_four = (ImageButton)findViewById(R.id.card_four);
         setClickableFalse(card_four);
         card_five = (ImageButton)findViewById(R.id.card_five);
         setClickableFalse(card_five);
         card_six = (ImageButton)findViewById(R.id.card_six);
         setClickableFalse(card_six);
         card_seven = (ImageButton)findViewById(R.id.card_seven);
         setClickableFalse(card_seven);
         card_eight = (ImageButton)findViewById(R.id.card_eight);
         setClickableFalse(card_eight);
         card_nine = (ImageButton)findViewById(R.id.card_nine);
         setClickableFalse(card_nine);
         card_ten = (ImageButton)findViewById(R.id.card_ten);
         setClickableFalse(card_ten);
         card_eleven = (ImageButton)findViewById(R.id.card_eleven);
         setClickableFalse(card_eleven);
         card_twelve = (ImageButton)findViewById(R.id.card_twelve);
         setClickableFalse(card_twelve);
         card_thirteen = (ImageButton)findViewById(R.id.card_thirteen);
         setClickableFalse(card_thirteen);
 
         handCradArray.add(card_one);
         handCradArray.add(card_two);
         handCradArray.add(card_three);
         handCradArray.add(card_four);
         handCradArray.add(card_five);
         handCradArray.add(card_six);
         handCradArray.add(card_seven);
         handCradArray.add(card_eight);
         handCradArray.add(card_nine);
         handCradArray.add(card_ten);
         handCradArray.add(card_eleven);
         handCradArray.add(card_twelve);
         handCradArray.add(card_thirteen);
 
         setMyHandCard();
 		setCallSuitOnOff(0, false);
 		setCallNumberOnOff(0, false);
 		
     }
 
     /**
      * set Game UI
      */
     private void setGameUI() {
 
         setContentView(R.layout.activity_room_game);
 
         handCradArray = new ArrayList<ImageButton>();
 
         LinearLayout linearLayout = (LinearLayout)findViewById(R.id.op_layout);
 
         TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
         //lp.setMargins(2, 2, 2, 2);
 
         for (int i=0; i<myCardArray.length; i++){
             ImageButton imageButton = new ImageButton(this);
             imageButton.setLayoutParams(lp);
             imageButton.setImageResource(R.drawable.c000);
             imageButton.setClickable(false);
 
             final int hi = i;
 
             imageButton.setOnClickListener(new View.OnClickListener() {
                 private short thisCardID = myCardArray[hi].getId();
                 //TFHBridgeDataPlayer(short playerNumber, short cardId)
                 //playerSendCard
                 public void onClick(View v) {
                     // Perform action on click
                     roomSocket.playerSendCard(myPlayerID, thisCardID);
                 }
             });
 
 
             handCradArray.add(imageButton);
             linearLayout.addView(imageButton);
         }
         refreshCard();
 //        card_one = (ImageButton)findViewById(R.id.card_one);
 //        setClickableTrue(card_one);
 //        card_one.setOnClickListener(buttonOnClick);
 //        card_two = (ImageButton)findViewById(R.id.card_two);
 //        setClickableTrue(card_two);
 //        card_two.setOnClickListener(buttonOnClick);
 //        card_three = (ImageButton)findViewById(R.id.card_three);
 //        setClickableTrue(card_three);
 //        card_three.setOnClickListener(buttonOnClick);
 //        card_four = (ImageButton)findViewById(R.id.card_four);
 //        setClickableTrue(card_four);
 //        card_four.setOnClickListener(buttonOnClick);
 //        card_five = (ImageButton)findViewById(R.id.card_five);
 //        setClickableTrue(card_five);
 //        card_five.setOnClickListener(buttonOnClick);
 //        card_six = (ImageButton)findViewById(R.id.card_six);
 //        setClickableTrue(card_six);
 //        card_six.setOnClickListener(buttonOnClick);
 //        card_seven = (ImageButton)findViewById(R.id.card_seven);
 //        setClickableTrue(card_seven);
 //        card_seven.setOnClickListener(buttonOnClick);
 //        card_eight = (ImageButton)findViewById(R.id.card_eight);
 //        setClickableTrue(card_eight);
 //        card_eight.setOnClickListener(buttonOnClick);
 //        card_nine = (ImageButton)findViewById(R.id.card_nine);
 //        setClickableTrue(card_nine);
 //        card_nine.setOnClickListener(buttonOnClick);
 //        card_ten = (ImageButton)findViewById(R.id.card_ten);
 //        setClickableTrue(card_ten);
 //        card_ten.setOnClickListener(buttonOnClick);
 //        card_eleven = (ImageButton)findViewById(R.id.card_eleven);
 //        setClickableTrue(card_eleven);
 //        card_eleven.setOnClickListener(buttonOnClick);
 //        card_twelve = (ImageButton)findViewById(R.id.card_twelve);
 //        setClickableTrue(card_twelve);
 //        card_twelve.setOnClickListener(buttonOnClick);
 //        card_thirteen = (ImageButton)findViewById(R.id.card_thirteen);
 //        setClickableTrue(card_thirteen);
 //        card_thirteen.setOnClickListener(buttonOnClick);
 
 //        opposite = (ImageButton)findViewById(R.id.opposite);
 //        setClickableFalse(opposite);
 //        left = (ImageButton)findViewById(R.id.left);
 //        setClickableFalse(left);
 //        right = (ImageButton)findViewById(R.id.right);
 //        setClickableFalse(right);
 //        home = (ImageButton)findViewById(R.id.home);
 //        setClickableFalse(home);home
 
     }
 
     public void haveNewData(final TFHBridgeMain main){
         handler.post(new Runnable() {
             @Override
             public void run() {
                 short command = main.getCommand();
                 Log.d("(R)SERVER COMMAND:", String.valueOf(command));
 
                 switch(command){
                     case TFHComm.ROOM_NEW_PLAYER:
                         Log.d("(R)ACTION", "room new player");
                         TFHBridgeDataNewPlayer tfhBridgeDataNewPlayer = (TFHBridgeDataNewPlayer) main.getData();
                         inRoomPlayer = (short) (tfhBridgeDataNewPlayer.getNewPlayerNumber() + 1);
                         Log.d("(R)IN ROOM PLAYER", String.valueOf(inRoomPlayer));
                         changeWaitingPerson();
                         if(myPlayerID == 100){
                             myPlayerID = (short) (inRoomPlayer-1);
                             Log.d("My player id", String.valueOf(myPlayerID));
                             if(myPlayerID == 3){
                                 // deal card when all player is in
                                 roomSocket.dealCard();
                             }
                         }
                         break;
                     case TFHComm.CARD_DATA:
                         Log.d("(R)ACTION", "card data(deal card)");
                         TFHBridgeDataCard tfhBridgeDataCard = (TFHBridgeDataCard) main.getData();
                         myCardArray = tfhBridgeDataCard.getCardData()[myPlayerID];
                         for(int i=0; i<myCardArray.length; i++){
                             Log.d("(R)DEAL CARD", "my card:" + myCardArray[i].getId());
                             myCardUsed[i] = false;
                         }
                         setCallUI();
                         callingSuit = 0;
                         callingNumber = 0;
                         if(myPlayerID == 0){
                             decideOpenNumberButton();
                             showNowCall(0);
                         }else{
                             showNowCall(1);
                         }
 
                         break;
 					case TFHComm.GOD_CARD_DATA:
 						Log.d("(R)ACTION", "get god card data");
 						TFHBridgeDataGodCard tfhBridgeDataGodCard = (TFHBridgeDataGodCard) main.getData();
 						String godCardCommand = tfhBridgeDataGodCard.getCommand();
 						if (godCardCommand.equals("KEEP")){
 							//con
                             callingSuit = tfhBridgeDataGodCard.getGodCardSuit();
                             callingNumber = tfhBridgeDataGodCard.getHeap();
                             if(callingSuit != 0){
                                 kingSuit = callingSuit;
                             }
 
                             if(isNowMyCallingTime(tfhBridgeDataGodCard.getPlayerNumber())){
                                 decideOpenNumberButton();
                                 showNowCall(0);
                             }else{
                                 showNowCall(1);
                             }
 
 
 
 						}else{
 							//finish
                             Log.d("(A)ACTION", "finish call god card");
                             // TODO here!!!
                             setGameUI();
                             refreshCard();
 						}
 						break;
                     case TFHComm.ROOM_DATA:
                         Log.d("(R)START", "start game");
                         TFHBridgeDataRoom tfhBridgeDataRoom = (TFHBridgeDataRoom)main.getData();
                         String dataRoomCommand = tfhBridgeDataRoom.getCommand();
                         if (dataRoomCommand.equalsIgnoreCase("START")){
                             teamScore[0] = tfhBridgeDataRoom.getNorthernHeap();
                             teamScore[1] = tfhBridgeDataRoom.getEeasternHeap();
                             tableCard = new short[4];
                             initPlayer = tfhBridgeDataRoom.getInitialPlayerNumber();
                             nowPlayer = tfhBridgeDataRoom.getNowPlayerNumber();
                             refreshCard();
                         }else{
                             tableCard[nowPlayer] = tfhBridgeDataRoom.getCardId();
                             nowPlayer = tfhBridgeDataRoom.getNowPlayerNumber();
                             refreshCard();
                         }
                         break;
                 }
 
             }
         });
     }
 
     /**
      * change the in room player
      */
     private void changeWaitingPerson() {
         handler.post(new Runnable() {
             @Override
             public void run() {
                 TextView tableDisplayWord = (TextView) findViewById(R.id.table_display_word);
                 tableDisplayWord.setText(getString(R.string.table_waiting_word, inRoomPlayer));
             }
         });
     }
 
 
     public void showStateWord(final String word){
                     handler.post(new Runnable() {
                         @Override
                         public void run() {
 //                final TextView text = (TextView)findViewById(R.id.textView_state);
 //                text.setText(word);
 //                text.setVisibility(View.VISIBLE);
 
 
                         }
 
         });
     }
 
     private Button.OnClickListener buttonSuitOnClick = new Button.OnClickListener() {
         @Override
         public void onClick(View view) {
 
             switch (view.getId()){
 
                 case R.id.spadeButton:
                     suit = 1;
                     break;
                 case R.id.heartButton:
                     suit = 2;
                     break;
                 case R.id.diamondButton:
                     suit = 3;
                     break;
                 case R.id.clubButton:
                     suit = 4;
                     break;
 
             }
             setCallSuitOnOff(0, false);
             wantCallSuit = suit;
             roomSocket.playerCallGodCard("CLIENT", myPlayerID, wantCallSuit, wantCallNumber);
 
         }
     };
 
     private Button.OnClickListener buttonNumberOnClick = new Button.OnClickListener() {
         @Override
         public void onClick(View view) {
 
             switch (view.getId()){
                 case R.id.oneButton:
                     heap = 1;
                     break;
                 case R.id.twoButton:
                     heap = 2;
                     break;
                 case R.id.threeButton:
                     heap = 3;
                     break;
                 case R.id.fourButton:
                     heap = 4;
                     break;
                 case R.id.fiveButton:
                     heap = 5;
                     break;
                 case R.id.sixButton:
                     heap = 6;
                     break;
                 case R.id.sevenButton:
                     heap = 7;
                     break;
                 case R.id.eightButton:
                     heap = 8;
                     break;
                 case R.id.nineButton:
                     heap = 9;
                     break;
 
 
                     //case R.id.tenButton:
                     //    heap = 10;
 
             }
             setCallNumberOnOff(0, false);
             RoomActivity.this.wantCallNumber = RoomActivity.this.heap;
             decideOpenSuitButton();
         }
     };
 
     private Button.OnClickListener buttonFunctionOnClick = new Button.OnClickListener() {
         @Override
         public void onClick(View view) {
 
             switch (view.getId()){
                 case R.id.passButton:
                     roomSocket.playerCallGodCard("CLIENT", myPlayerID, (short)0, (short)0);
                     break;
             }
         }
     };
 
     private void setClickableFalse(ImageButton imageButton) {
 
         imageButton.setClickable(false);
     }
 
     private void setClickableTrue(ImageButton imageButton) {
         imageButton.setClickable(true);
     }
 	
 	private void setCallSuitOnOff(int from, boolean onOff){
 		for (int i=from; i<callSuitArray.size(); i++){
 			callSuitArray.get(i).setClickable(onOff);
 		}
 	}
 	
 	private void setCallNumberOnOff(int from, boolean onOff){
 		for (int i=from; i<callButtonArray.size(); i++){
 			callButtonArray.get(i).setClickable(onOff);
 		}
 	}
 
     private void setMyHandCard(){
         for (int i=0; i<handCradArray.size(); i++){
             Log.d("(R)SET CARD", String.valueOf(i));
             handCradArray.get(i).setImageResource(Functions.cardToDrawableID(myCardArray[i].getId()));
         }
     }
 
     private void decideOpenNumberButton(){
         if (callingSuit == 4){
             setCallNumberOnOff(callingNumber, true);
         }else{
             if (callingNumber == 0){
                 setCallNumberOnOff(callingNumber, true);
             }else{
                 setCallNumberOnOff(callingNumber-1, true);
             }
         }
     }
 
     private void decideOpenSuitButton(){
         if (wantCallNumber > callingNumber){
             setCallSuitOnOff(0, true);
         }else{
             switch(callingSuit){
                 case 4:
                     callSuitArray.get(2).setClickable(true);
                     callSuitArray.get(1).setClickable(true);
                     callSuitArray.get(0).setClickable(true);
                     break;
                 case 3:
                     callSuitArray.get(1).setClickable(true);
                     callSuitArray.get(0).setClickable(true);
                     break;
                 case 2:
                     callSuitArray.get(0).setClickable(true);
                     break;
                 case 1:
                     break;
 
             }
         }
     }
 
     private boolean isNowMyCallingTime(short upperPlayer){
         if (upperPlayer == 0){
             if (myPlayerID == 3){
                 return true;
             }else{
                 return false;
             }
         }else if(upperPlayer == 1){
             if (myPlayerID == 0){
                 return true;
             }else{
                 return false;
             }
         }else if(upperPlayer == 2){
             if (myPlayerID == 1){
                 return true;
             }else{
                 return false;
             }
         }else{
             if (myPlayerID == 2){
                 return true;
             }else{
                 return false;
             }
         }
     }
 
     private void showNowCall(int who){
         TextView textView = (TextView) findViewById(R.id.call_data_word);
         if (who == 0){
             textView.setText(getString(R.string.call_word_my, cardIDToString(callingSuit, callingNumber)));
         }else{
             textView.setText(getString(R.string.call_word_other, cardIDToString(callingSuit, callingNumber)));
         }
     }
 
     private String cardIDToString(short suit, short number){
         String word = "";
         switch(suit){
             case 1:
                 word = getString(R.string.spade);
                 break;
             case 2:
                 word = getString(R.string.heart);
                 break;
             case 3:
                 word = getString(R.string.diamond);
                 break;
             case 4:
                 word = getString(R.string.club);
                 break;
         }
         word += String.valueOf(number);
         return word;
     }
 
     private void refreshCard() {
         short startSuit = (short) (tableCard[initPlayer]/100);
         boolean haveSuit = haveSuitOrNot(startSuit);
 
 
         for (int i = 0; i < myCardUsed.length; i++) {
 
             ImageButton imageButton = handCradArray.get(i);
             imageButton.setClickable(false);
             if (myCardUsed[i] == false) {
                 imageButton.setImageResource(Functions.cardToDrawableID(myCardArray[i].getId()));
                 if (nowPlayer == myPlayerID){
                     if(initPlayer == myPlayerID) {
                         //all open
                         imageButton.setClickable(true);
                     }else if(haveSuit){
                         //open that
                         if(myCardArray[i].getSuit() == startSuit){
                             imageButton.setClickable(true);
                         }
                     }else{
                         //all
                         imageButton.setClickable(true);
                     }
                 }
             }
             else {
 
                 imageButton.setImageResource(R.drawable.c000);
             }
         }
     }
 
     private boolean haveSuitOrNot(short startSuit) {
            for(int i=0; i < myCardArray.length; i++){
                if (myCardArray[i].getSuit() == startSuit){
                    if (myCardUsed[i] == false){
                        return true;
                    }
                }
            }
             return false;
     }
 }
