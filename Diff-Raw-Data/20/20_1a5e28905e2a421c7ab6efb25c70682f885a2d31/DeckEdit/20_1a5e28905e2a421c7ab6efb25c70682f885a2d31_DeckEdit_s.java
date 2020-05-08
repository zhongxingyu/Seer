 package com.example.glsurfaceview;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.data.CardInformation;
 import com.example.data.DataBase;
 
 import android.widget.Button;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.Window;
 import android.view.animation.TranslateAnimation;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class DeckEdit extends Activity {
 	
 	/*ł́ACardData͏Ă͂邯ǉɂgĂȂł*/
 	
 	public static final int THE_NUMBER_OF_DECK = 11;		//fbL̖@11
 	
 	TextView title;			//ufbLҏWʁvƂ
 	TextView instruct;		//uJ[h^b`āAfbLҏW悤Iv
 	List<CardData> deckList;//̃fbL̃Xgf[^@CardDataNXList ArrayListƂĐĂ
 	ImageView[] member = new ImageView[THE_NUMBER_OF_DECK];	//\11ImageView
 	String[] memberName = new String[THE_NUMBER_OF_DECK];  //Deckɂ͂ĂJ[h̖O
 	Button back;			//߂@{^
 	
 	LinearLayout ll;		//Ŝ𕢂Layout
 	LinearLayout topRow;	//fbLiLayout
 	LinearLayout middleRow;	//fbLiLayout
 	LinearLayout bottomRow;	//ʂ̈ԉLayout
 	
 	
 	LinearLayout.LayoutParams titleParam;//p[^[
 	LinearLayout.LayoutParams topParam;//p[^[
 	LinearLayout.LayoutParams middleParam;//p[^[
 	LinearLayout.LayoutParams bottomParam;//p[^[
 	LinearLayout.LayoutParams backParam;//p[^[
 	LinearLayout.LayoutParams imageParam;//p[^[
 	LinearLayout.LayoutParams instructParam;//p[^[
 	
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		
 	//낢됶
 		//CAEg̐&ݒ
 		ll = new LinearLayout(this);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		topRow = new LinearLayout(this);
 		topRow.setOrientation(LinearLayout.HORIZONTAL);
 		topRow.setGravity(Gravity.CENTER);
 		middleRow = new LinearLayout(this);
 		middleRow.setOrientation(LinearLayout.HORIZONTAL);
 		middleRow.setGravity(Gravity.CENTER);
 		bottomRow = new LinearLayout(this);
 		bottomRow.setOrientation(LinearLayout.HORIZONTAL);
 		
 	//p[^
 		//傫ݒ
 		titleParam = new LinearLayout.LayoutParams(Const.rx(0.5), Const.ry(0.2));
 		topParam = new LinearLayout.LayoutParams(Const.rx(0.9), Const.ry(0.3));
 		middleParam = new LinearLayout.LayoutParams(Const.rx(0.9), Const.ry(0.3));
 		bottomParam = new LinearLayout.LayoutParams(Const.rx(0.95), Const.ry(0.1));
 		instructParam = new LinearLayout.LayoutParams(Const.rx(0.6), Const.ry(0.1));
 		backParam = new LinearLayout.LayoutParams(Const.rx(0.2), Const.ry(0.1));
 		imageParam = new LinearLayout.LayoutParams(Const.rx(0.1), Const.ry(0.25));
 		
 		//C[W̊Ԋuݒ
 		imageParam.setMargins(Const.rx(0.05), Const.ry(0.05), 0, Const.ry(0.05));
 		//Ԋuݒ
 		titleParam.setMargins(Const.rx(0.025),Const.ry(0.01),0, 0);
 		topParam.setMargins(Const.rx(0.03), 0,Const.rx(0.05), 0);
 		middleParam.setMargins(Const.rx(0.03), 0,Const.rx(0.05), 0);
 		bottomParam.setMargins(0, 0,Const.rx(0.05), 0);
 		instructParam.setMargins(Const.rx(0.1), 0,Const.rx(0.05), 0);
 		
 	//
 		title = new TextView(this);
 		instruct = new TextView(this);
 		deckList = new ArrayList<CardData>();//͂gĂȂł
 		back = new Button(this);
 		for(int i=0;i<THE_NUMBER_OF_DECK;i++){
 			member[i] = new ImageView(this);
 			
 			String selectedCard = DataBase.GetMyDeck(i);
 			member[i].setImageResource(CardInformation.GetCardInformaionFromName(selectedCard).getCard_id());
 			memberName[i] = CardInformation.GetCardInformaionFromName(selectedCard).getName();
 			
 			if(i<5)topRow.addView(member[i],imageParam);   //n߂5̒i
 			if(i>4)middleRow.addView(member[i],imageParam);//c̒iɕ\
 			member[i].setOnClickListener(new SampleClickListener());
 		
 		}
 		back.setOnClickListener(new SampleClickListener());
 		
 		//eLXg̃TCYƂ
 		title.setText("fbLҏW");
 		title.setTextSize(Const.rx(0.035));
 		instruct.setText("J[h^b`āAfbLҏW悤I");
 		instruct.setTextSize(Const.rx(0.015));
 		back.setText("߂");
 		back.setTextSize(Const.rx(0.02));
 		
 		
 		
 		//낢Zbg
 		setContentView(ll);
 		ll.addView(title,titleParam);
 		ll.addView(topRow,topParam);
 		ll.addView(middleRow,middleParam);
 		ll.addView(bottomRow,bottomParam);
 		bottomRow.addView(instruct,instructParam);
 		bottomRow.addView(back,backParam);
 		
 		//obN̎ʐ^Zbg
 		ll.setBackgroundResource(R.drawable.background);
 		topRow.setBackgroundResource(R.drawable.explain);
 		middleRow.setBackgroundResource(R.drawable.explain);
 		instruct.setBackgroundResource(R.drawable.back);
 		
 	}
 	class SampleClickListener implements android.view.View.OnClickListener{
 		
 		public void onClick(View v) 
 		{
 			if(v.equals(back))finish();
 			for(int i=0;i<THE_NUMBER_OF_DECK;i++){
 			
 				if(v.equals(member[i]) ){
 					Intent intent = new Intent();
 					intent.setClass( OpenGLSurfaceView.c , com.example.glsurfaceview.DeckSelect.class);
 					intent.putExtra("name", memberName[i]);		//ǂ̃J[h^b`̏ADeckSelectNXɑ
 					OpenGLSurfaceView.c.startActivity(intent);
 					finish();
 				}
 			}
 		}
 		
 	}
 }
