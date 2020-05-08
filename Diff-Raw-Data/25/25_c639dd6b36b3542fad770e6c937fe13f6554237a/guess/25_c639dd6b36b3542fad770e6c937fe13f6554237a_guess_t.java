 package com.example.undercover;
 
 import java.util.Random;
 
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class guess extends BaseActivity {
 	private TableLayout contentTable;
 
 	// 卧底人数
 	private int soncount;
 	// 平民人数
 	private int fathercount;
 	// 卧底的词语
 	private String son;
 	// 0-n人数的词语数组
 	private String[] content;
 	private TextView txtTitle;
 	private Button punishBtn;
 	private Button startBtn;
 	private Button quickStartBtn;
 	private ImageView home;
 	
 	private int totalcount;
 	private boolean isOver;
 	private boolean flag;
 	private boolean isGetRestart;
 	private boolean isShow;
 	private int temindex;
 	private Random random = new Random();
 	private TextView txtLong;
 	
 //	private String overString1; 
 //	private String overString2; 
 //	private String overString3; 
 //	private String overString4; 
 //	private String overString5; 
 //	private String overString6; 
 //	private String overString7; 
 //	private String overString8; 
 	private String[] overString;
 //	={overString1,overString2,overString3,overString4,overString5,overString6,overString7,overString8};
 	private String gusswhoisspy;
 	private String undercover;
 	private String blank;
 	private String aggrieved;
 	private String taplong;
 	private String gameOver;
 	private String gameoverspy;
 	private String hy;
 	private String ge;
 	private boolean[] hasClicked;
 	protected void onCreate(Bundle savedInstanceState) {
 		gusswhoisspy=getResources().getString(R.string.gusswhoisspy);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.guess);
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
 		//string_start
 //		overString1 = getResources().getString(R.string.overString1);  
 //		overString2 = getResources().getString(R.string.overString2);  
 //		overString3 = getResources().getString(R.string.overString3);  
 //		overString4 = getResources().getString(R.string.overString4);  
 //		overString5 = getResources().getString(R.string.overString5);  
 //		overString6 = getResources().getString(R.string.overString6);  
 //		overString7 = getResources().getString(R.string.overString7);  
 //		overString8 = getResources().getString(R.string.overString8);
 		overString = getResources().getStringArray(R.array.overstring);
 		gusswhoisspy=getResources().getString(R.string.gusswhoisspy); 
 		undercover=getResources().getString(R.string.undercover);
 		blank=getResources().getString(R.string.blank);                
 		aggrieved=getResources().getString(R.string.aggrieved);        
 		taplong=getResources().getString(R.string.taplong);            
 		gameOver=getResources().getString(R.string.gameOver);          
 		gameoverspy=getResources().getString(R.string.gameoverspy);    
 		hy=getResources().getString(R.string.hy);                      
 		ge=getResources().getString(R.string.ge);
 		
 		//string_end
 		
 		isOver = false;
 		flag = false;
 		isGetRestart = false;
 		contentTable = (TableLayout) findViewById(R.id.tableContent);
 		txtTitle = (TextView) findViewById(R.id.txtTitle);
 //		punishBtn = new Button(this);
 //		startBtn = new Button(this);
 		punishBtn = (Button) findViewById(R.id.btn_punish);
 		startBtn = (Button) findViewById(R.id.btn_restart);
 		quickStartBtn = (Button) findViewById(R.id.btn_quickstart);
 		home = (ImageView) findViewById(R.id.btnhome);
 		quickStartBtn.setBackgroundResource(R.drawable.btnbg);
 		
 //		Bundle bundle = this.getIntent().getExtras();
 //		isShow	= bundle.getBoolean("isShow");
 //		son = bundle.getString("son");
 //		soncount = bundle.getInt("underCount");
 //		content = bundle.getStringArray("content");
 		
 		if (savedInstanceState == null) {
 			Bundle bundle = this.getIntent().getExtras();
 			isShow	= bundle.getBoolean("isShow");
 			son = bundle.getString("son");
 			soncount = bundle.getInt("underCount");
 			content = bundle.getStringArray("content");
 			hasClicked = new boolean[content.length];
 		}else{
 			content = savedInstanceState.getStringArray("content");
 			isShow  = savedInstanceState.getBoolean("isShow");
 			son     = savedInstanceState.getString("son");
 			soncount = savedInstanceState.getInt("soncount");
 			isOver  = savedInstanceState.getBoolean("isOver");
 			hasClicked = savedInstanceState.getBooleanArray("hasClicked");
 		}
 		
 		Log.d("isover", String.valueOf(isOver));
 		Log.d("soncount", String.valueOf(soncount));
		Log.d("content", String.valueOf(content.length));
 		fathercount = content.length - soncount;
 		totalcount = content.length;
 		txtTitle.setText(gusswhoisspy);
 		temindex = 0;
 		for (int i = 0; i < Math.ceil((float) content.length / 4); i++) {
 			TableRow newrow = new TableRow(this);
 			for (int m = 0; m < 4; m++) {
				
				if (temindex >= content.length) {
 					break;
 				}
 				FrameLayout fl = new FrameLayout(this);
 				ImageView select = new ImageView(this);
 				final TextView text	= new TextView(this);
				text.setText(String.valueOf(temindex+1));
 				text.setGravity(Gravity.CENTER);
 				text.setTextSize(30);
 
 				final TextView shenfen = new TextView(this);
				shenfen.setText(content[temindex]);
 				shenfen.setGravity(Gravity.BOTTOM);
 				shenfen.setTextSize(15);
 				shenfen.setTag(999);
 				shenfen.setVisibility(View.INVISIBLE);
 
				select.setTag(temindex);
				if (hasClicked[temindex]) {
 					select.setBackgroundResource(R.drawable.popogray72);
 					select.setClickable(false);
 				}else{
 					select.setBackgroundResource(R.drawable.popo72);
 					select.setOnLongClickListener(new Button.OnLongClickListener() {
 						@Override
 						public boolean onLongClick(View v) {
 							tapIndex((Integer) v.getTag());
 							hasClicked[(Integer)v.getTag()] = true;
 							v.setClickable(false);
 							ImageView tt = (ImageView) v;
 							tt.setBackgroundResource(R.drawable.popogray72);
 							SoundPlayer.playball();
 							if(isShow){
 								text.setTextSize(20);
 								if (content[(Integer) v.getTag()].equals(son)) {
 									text.setText(undercover);
 									text.setTextColor(getResources().getColor(R.color.RED));
 								} else {
 									if (content[(Integer) v.getTag()]
 											.equals(blank)) {
 										text.setText(blank);
 										text.setTextColor(getResources().getColor(R.color.BLUE));
 									}else{
 										text.setText(aggrieved);
 									}
 								}
 							}
 							// tt.setText("*");
 							return true;
 						}
 					});
 				}
				temindex ++;
 				fl.addView(select);
 				fl.addView(text);
 				fl.addView(shenfen);
 				newrow.addView(fl, disWidth / 4, disWidth / 4);
 			}
 			contentTable.addView(newrow);
 		}
 		txtLong = new TextView(this);
 		txtLong.setText(taplong);
 		txtLong.setTag(100099);
 		contentTable.addView(txtLong);
 		
 		home.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		checkGameOver();
 	}
 
 	protected void setAllButton(boolean useable) {
 		for (int i = 0; i < content.length; i++) {
 			ImageView tem = (ImageView) contentTable.findViewWithTag(i);
 			tem.setClickable(useable);
 		}
 	}
 	protected void tapIndex(int tag) {
 		txtLong.setVisibility(View.INVISIBLE);
 		if (soncount + fathercount == totalcount) {
 			uMengClick("click_guess_first");
 		}
 		if (content[tag].equals(son)) {
 			soncount--;
 		} else {
 			fathercount--;
 		}
 		checkGameOver();
 	}
 	
 	protected void checkGameOver(){
 		Log("CheeckGameOver");
 //		if (!isOver) {
 			if (soncount <= 0) {
 				Log("任务完成");
 				txtTitle.setText(gameOver + "【" + son + "】");
 				isOver = true;
 				uMengClick("click_guess_last");
 				SoundPlayer.playclaps();
 				refash();
 				setAllButton(false);
 			} else if (fathercount <= soncount) {
 				Log("卧底胜利");
 				txtTitle.setText(gameoverspy + "【" + son + "】");
 				isOver = true;
 				uMengClick("click_guess_last");
 				refash();
 				setAllButton(false);
 			} else {
 				int stringcount = overString.length;
 				int stringindex = Math.abs(random.nextInt()) % stringcount;
 				txtTitle.setText(overString[stringindex]);
 				Log(hy + soncount + ge);
 			}
 //		}else{
 //			
 //		}
 	}
 
 	protected void Log(String string) {
 		Log.v("tag", string);
 	}
 
 	private void refash() {
 		// 所有身份亮明
 
 
 		//Button punishBtn = new Button(this);
 //		punishBtn.setVisibility(View.VISIBLE);
 //		startBtn.setVisibility(View.VISIBLE);
 //		quickStartBtn.setVisibility(View.VISIBLE);
 		TableLayout btn_wrapper = (TableLayout) findViewById(R.id.btn_wrapper);
 		btn_wrapper.setVisibility(View.VISIBLE);
 
 		for (int i = 0; i < contentTable.getChildCount(); i++) {
 			if (contentTable.getChildAt(i) instanceof TableRow) {
 				TableRow v = (TableRow) contentTable.getChildAt(i);
 				for (int m = 0; m < v.getChildCount(); m++) {
 					if (v.getChildAt(m) instanceof FrameLayout) {
 						FrameLayout imageTem = (FrameLayout) v.getChildAt(m);
 						TextView temtext = (TextView) imageTem
 								.findViewWithTag(999);
 						temtext.setVisibility(View.VISIBLE);
 					}
 				}
 			}
 
 		}
 		punishBtn.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				 Intent goMain = new Intent();
 				 goMain.setClass(guess.this, PunishActivity.class);
 				uMengClick("game_undercover_punish");
 				 startActivity(goMain);
 			}
 		});
 //		contentTable.addView(punishBtn);
 		// startBtn.setText("重新开始");
 		startBtn.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				Intent goMain = new Intent();
 				goMain.setClass(guess.this, Setting.class);
 				startActivity(goMain);
 				uMengClick("game_undercover_resert");
 				finish();
 			}
 		});
 		
 		 
 		quickStartBtn.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				Intent goMain = new Intent();
 				goMain.setClass(guess.this, fanpai.class);
 				startActivity(goMain);
 				uMengClick("game_undercover_quickresert");
 				finish();
 			}
 		});
 //		contentTable.addView(startBtn);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		savedInstanceState.putBoolean("isShow", isShow);
 		savedInstanceState.putInt("soncount", soncount);
 		savedInstanceState.putStringArray("content", content);
 		savedInstanceState.putString("son", son);
 		savedInstanceState.putBoolean("isOver", isOver);
 		savedInstanceState.putBooleanArray("hasClicked",hasClicked);
 		Log.d("saved","onSaveInstanceState");
 	}
 
 }
