 package com.fantasycard.app;
 
 import java.util.Hashtable;
 
 import com.fantasycard.app.R;
 import com.fantasycard.cardinfo.CardInfo;
 import com.fantasycard.ui.CardDragListener;
 import com.fantasycard.ui.CardTouchListener;
 
 import android.app.Activity;
 import android.content.ClipData;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.DragShadowBuilder;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	
 	private MyCardSlotViewGroup mySlotView;
 	private DeckManager mDeckManager;
 	
 	public static CardInfo[] mMyHandSlot = new CardInfo[3];
 	public static CardView[] mMyHandSlotCardView = new CardView[3];
 	public static Hashtable<CardView, CardInfo> mCardViewList = new Hashtable<CardView, CardInfo>();
 	public int mSelectSlotId;
 	
 	public static CardInfo[] mMyBattleSlot = new CardInfo[3];
 	public static CardView[] mMyBattleSlotView = new CardView[3];
 	
 	public RelativeLayout[] mHandSlotFrame = new RelativeLayout[3];
 	public RelativeLayout[] mBattleSlotFrame = new RelativeLayout[3];
 	
 	public LinearLayout dropArea;
 	
 	private TextView mtxtTurnCnt;
 	private TextView mtxtManaCnt;
 	private TextView mtxtLifeCnt;
 	
 	private int mTurn;
 	private int mPlayerMana;
 	private int mPlayerLife;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		init();
 		
 	}
 	
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		appOnAttachedToWindow();
 	}
 	
 	public void init() {
 		setDensity();
 		appOnAttachedToWindow();
 		
 		dropArea = (LinearLayout) findViewById(R.id.drop_area); 
 				
 		mySlotView = (MyCardSlotViewGroup)findViewById(R.id.myslotview);
 		
 		mHandSlotFrame[0] = (RelativeLayout) findViewById(R.id.mycardslot01);
 		mHandSlotFrame[1] = (RelativeLayout) findViewById(R.id.mycardslot02);
 		mHandSlotFrame[2] = (RelativeLayout) findViewById(R.id.mycardslot03);
 		
 		mBattleSlotFrame[0] = (RelativeLayout) findViewById(R.id.cardslot01);
 		mBattleSlotFrame[1] = (RelativeLayout) findViewById(R.id.cardslot02);
 		mBattleSlotFrame[2] = (RelativeLayout) findViewById(R.id.cardslot03);
 		
 		mTurn = 0;
 		mPlayerMana = 0;
 		mPlayerLife = 10;
 		
 		///TurnCount初期化
 		mtxtTurnCnt = new TextView(this);
 		mtxtTurnCnt.setTextColor(Color.rgb(246, 196, 5));
 		mtxtTurnCnt.setText("Turn : " + mTurn);
         mySlotView.addView(mtxtTurnCnt);
         
 		RelativeLayout.LayoutParams turnparams = (RelativeLayout.LayoutParams) mtxtTurnCnt.getLayoutParams();
 		mtxtTurnCnt.setLayoutParams(UILayoutParams.changeMargin(turnparams, new Rect(10, 5, 10, 35)));
 
 		///ManaCount初期化
 		mtxtManaCnt = new TextView(this);
 		mtxtManaCnt.setTextColor(Color.rgb(246, 196, 5));
 		mtxtManaCnt.setText("Mana : " + mPlayerMana);
 		mySlotView.addView(mtxtManaCnt);
         
 		RelativeLayout.LayoutParams manaparams = (RelativeLayout.LayoutParams) mtxtManaCnt.getLayoutParams();
 		mtxtManaCnt.setLayoutParams(UILayoutParams.changeMargin(manaparams, new Rect(100, 5, 10, 35)));
 		
 		///LifeCount初期化
 		mtxtLifeCnt = new TextView(this);
 		mtxtLifeCnt.setTextColor(Color.rgb(246, 196, 5));
 		mtxtLifeCnt.setText("Life : " + mPlayerLife);
         mySlotView.addView(mtxtLifeCnt);
         
 		RelativeLayout.LayoutParams lifeparams = (RelativeLayout.LayoutParams) mtxtLifeCnt.getLayoutParams();
 		mtxtLifeCnt.setLayoutParams(UILayoutParams.changeMargin(lifeparams, new Rect(210, 5, 10, 35)));
         
 		mDeckManager = new DeckManager();
 		mDeckManager.init();
 		
 		firstDrawCard();
 		
 		mCardViewList.put(mMyHandSlotCardView[0], mMyHandSlot[0]);
 		mCardViewList.put(mMyHandSlotCardView[1], mMyHandSlot[1]);
 		mCardViewList.put(mMyHandSlotCardView[2], mMyHandSlot[2]);
 		
 		mMyHandSlotCardView[0].setOnTouchListener(new CardTouchListener(this));
 		mMyHandSlotCardView[1].setOnTouchListener(new CardTouchListener(this));
 		mMyHandSlotCardView[2].setOnTouchListener(new CardTouchListener(this));
 		
 		dropArea.setOnDragListener(new CardDragListener(this, dropArea));
 	}
 	
 	public CardInfo getCardInfoFromSelectHandSlot() {
 		return mMyHandSlot[mSelectSlotId - AppValues.HAND_SLOT_1];
 	}
 	
 	public CardInfo getCardInfoFromCardView(CardView view) {
 		for(int i=0;i<3;i++) {
 			if(mMyHandSlotCardView[i] == view) {
 				return mMyHandSlot[i];
 			}
 		}
 		return null;
 	}
 	
 	public void selectCardSlotId(int cardslotid){
 		mSelectSlotId = cardslotid;
 	}
 	
 	private void firstDrawCard(){
 		while(true){
 			for(int i=0;i<3;i++){
 				if(mMyHandSlot[i] == null){
 					mMyHandSlot[i] = mDeckManager.getCardFromDeck();
 					mMyHandSlotCardView[i] = drawCardImgMyHand(i, mMyHandSlot[i]);
 					mMyHandSlotCardView[i].mCardSlotId = AppValues.HAND_SLOT_1 + i;
 				}
 			}
 			
 			boolean isManaCardExist = false;
 			
 			for(int i=0;i<3;i++){
 				if(mMyHandSlot[i].card_category == AppValues.CARD_MANA){
 					isManaCardExist = true;
 					break;
 				}
 			}
 			
 			if(isManaCardExist) {
 				break;
 			} else {
 				for(int i=0;i<3;i++) {
 					mDeckManager.putCardToDeck(mMyHandSlot[i]);
 				}
 				mDeckManager.suffleDeck();
 			}
 		}
 	}
 	
 	private CardView drawCardImgMyHand(int idx, CardInfo cardinfo){
 		CardView cardImgView = getCardImageView(cardinfo);
 		mySlotView.addImgViewToMyHandSlot(idx, cardImgView);
 
 		return cardImgView;
 	}
 	
 	public void moveHandToEmptyBattleSlot(int slotNum) {
 		Log.d("Premo","moveHandToBattleSlot() ENTRY");
 		Log.d("Premo","moveHandToBattleSlot() slotNum = " + slotNum);
 		int emptyBattleSlotNum = -1;
 		for(int i=0;i<3;i++) {
 			if(mMyBattleSlot[i] == null) {
 				emptyBattleSlotNum = i;
 				break;
 			}
 		}
 		if(emptyBattleSlotNum != -1) {
 			if (mMyBattleSlot[slotNum] == null) {
 				moveHandToBattleSlot(getCardSlotNumFromSlotId(mSelectSlotId), emptyBattleSlotNum);
 			}
 		}else {
 			getCardViewFromSlotId(mSelectSlotId).setVisibility(View.VISIBLE);
 		}
 		Log.d("Premo","moveHandToBattleSlot() END");
 	}
 	
 	public void moveHandToBattleSlot(int fromHandSlotNum, int targetBattleSlotNum){
 		mMyBattleSlot[targetBattleSlotNum] = mMyHandSlot[fromHandSlotNum];
 		mMyBattleSlotView[targetBattleSlotNum] = mMyHandSlotCardView[fromHandSlotNum];
 	    mMyBattleSlotView[targetBattleSlotNum].mCardSlotId = AppValues.BATTLE_SLOT_1 + targetBattleSlotNum;
 	    
 		mHandSlotFrame[fromHandSlotNum].removeView(mMyHandSlotCardView[fromHandSlotNum]);
		
 		mySlotView.addImgViewToMyBattleSlot(targetBattleSlotNum, mMyBattleSlotView[targetBattleSlotNum]);
 	    final View droppedView = mMyBattleSlotView[targetBattleSlotNum];
 	    droppedView.post(new Runnable(){
 			@Override
 			public void run() {
 		        droppedView.setVisibility(View.VISIBLE);
 			}
 	    });	    
 	    
 	    mMyHandSlot[fromHandSlotNum] = null;
 	    mMyHandSlotCardView[fromHandSlotNum] = null;
 	}
 	
 	public void moveBattleToBattleSlot(int fromBattleSlotNum,int targetBattleSlotNum){
 		CardInfo tempinfo = mMyBattleSlot[targetBattleSlotNum];
 		CardView tempview = mMyBattleSlotView[targetBattleSlotNum];
 		mBattleSlotFrame[targetBattleSlotNum].removeView(mMyBattleSlotView[targetBattleSlotNum]);
 		
 		mMyBattleSlot[targetBattleSlotNum] = mMyBattleSlot[fromBattleSlotNum];
 		mMyBattleSlotView[targetBattleSlotNum] = mMyBattleSlotView[fromBattleSlotNum];
 		mBattleSlotFrame[fromBattleSlotNum].removeView(mMyBattleSlotView[fromBattleSlotNum]);
 				
 		mySlotView.addImgViewToMyBattleSlot(targetBattleSlotNum, mMyBattleSlotView[targetBattleSlotNum]);
 	    final View targetView = mMyBattleSlotView[targetBattleSlotNum];
 	    targetView.post(new Runnable(){
 			@Override
 			public void run() {
 				targetView.setVisibility(View.VISIBLE);
 			}
 	    });
 	    mMyBattleSlotView[targetBattleSlotNum].mCardSlotId = AppValues.BATTLE_SLOT_1 + targetBattleSlotNum;
 
 	    mMyBattleSlot[fromBattleSlotNum] = tempinfo;
 	    mMyBattleSlotView[fromBattleSlotNum] = tempview;
 		mySlotView.addImgViewToMyBattleSlot(fromBattleSlotNum, mMyBattleSlotView[fromBattleSlotNum]);
 	    final View fromView = mMyBattleSlotView[fromBattleSlotNum];
 	    fromView.post(new Runnable(){
 			@Override
 			public void run() {
 				fromView.setVisibility(View.VISIBLE);
 			}
 	    });
 	    mMyBattleSlotView[fromBattleSlotNum].mCardSlotId = AppValues.BATTLE_SLOT_1 + fromBattleSlotNum;
 	}
 	
 	public int getCardSlotNumFromSlotId(int slotid) {
 		if(slotid >= AppValues.BATTLE_SLOT_1 && slotid <= AppValues.BATTLE_SLOT_3) {
 			return slotid - AppValues.BATTLE_SLOT_1;
 		} else if(slotid >= AppValues.HAND_SLOT_1 && slotid <= AppValues.HAND_SLOT_3) {
 			return slotid - AppValues.HAND_SLOT_1;
 		}
 		return -1;
 	}
 	
 	public CardView getCardViewFromSlotId(int slotid) {
 		if(slotid >= AppValues.BATTLE_SLOT_1 && slotid <= AppValues.BATTLE_SLOT_3) {
 			return mMyBattleSlotView[getCardSlotNumFromSlotId(slotid)];
 		} else if(slotid >= AppValues.HAND_SLOT_1 && slotid <= AppValues.HAND_SLOT_3) {
 			return mMyHandSlotCardView[getCardSlotNumFromSlotId(slotid)];
 		}
 		return null;
 	}
 	
 	private CardView getCardImageView(CardInfo cardinfo) {
 		CardView cardimg = new CardView(this);
 		switch(cardinfo.card_id){
 			case 1:
 				cardimg.setBitmap(R.drawable.element1);
 				break;
 			case 2:
 				cardimg.setBitmap(R.drawable.element2);
 				break;
 			case 3:
 				cardimg.setBitmap(R.drawable.element3);
 				break;
 			case 4:
 				cardimg.setBitmap(R.drawable.card_1_1);
 				break;
 			case 5:
 				cardimg.setBitmap(R.drawable.card_1_2);
 				break;
 			case 6:
 				cardimg.setBitmap(R.drawable.card_1_3);
 				break;
 			case 7:
 				cardimg.setBitmap(R.drawable.card_2_1);
 				break;
 			case 8:
 				cardimg.setBitmap(R.drawable.card_2_2);
 				break;
 			case 9:
 				cardimg.setBitmap(R.drawable.card_2_3);
 				break;
 			case 10:
 				cardimg.setBitmap(R.drawable.card_3_1);
 				break;
 		}
 		
 		return cardimg;
 	}
 
 	/**
 	 * viewport 設定処理
 	 */
     private void setDensity() {
     	DisplayMetrics dm = getResources().getDisplayMetrics();
     	float wdp = dm.widthPixels / dm.density;
     	if(wdp > 720) wdp = 720;
     	UILayoutParams.DENSITY_DPI = Math.floor(320 * 160 / wdp);
     	if(UILayoutParams.DENSITY_DPI > 160) UILayoutParams.DENSITY_DPI = 160;
 	}
     
 	/**
 	 * 画面表示時に端末のdp設定処理
 	 */
 	private void appOnAttachedToWindow() {
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int px = getWindowManager().getDefaultDisplay().getWidth();
         float dp = px / getResources().getDisplayMetrics().density;
 		if(dp > 720 ){
 			UILayoutParams.SCALED_DENSITY = (float) ((720f / 320f) * metrics.scaledDensity);
 		} else {
 			UILayoutParams.SCALED_DENSITY = (float) (dp / 320) * metrics.scaledDensity;
 		}
 	}
 }
