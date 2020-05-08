 package at.ac.tuwien.sportmate;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Timer;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class StartFragment extends Fragment implements EventInterface, OnClickListener
 {
 	private View selected_item = null;
 	private int offset_x = 0;
 	private int offset_y = 0;
 	
 	private BoCategory selectedCategory;
 	
 	View view;
 	
 	TextView header;
 	LinearLayout currentInformation;
 
 	ImageView image1;
 	ImageView image2;
 	ImageView image3;
 	ImageView image4;
 	ImageView image5;
 	
 	LinearLayout category1;
 	LinearLayout category2;
 	LinearLayout category3;
 	LinearLayout category4;
 	LinearLayout category5;
 	
 	ArrayList<LinearLayout> categoryViews;
 
 	boolean animationDone = false;
 	
 	TextView lblStarttimeOut, lblTimeOut, lblPointsOut, lblGroupmembersOut, lblBonusOut;
 	Button btnStop;
 	long start_time, stop_time, duration;
 	int count_groupMembersDuringActivity = -1; 
 	boolean STOP;
 	Handler handler;
 	Timer timerActivityStart;
 	
 	double points, bonusPoints;
 	
 	@Override
 	public void eventA() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		
 		handler = new Handler();
 		categoryViews = new ArrayList<LinearLayout>();
 		
 		selectedCategory = null;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		view = inflater.inflate(R.layout.start, container, false);
 		
 		header = (TextView) view.findViewById(R.id.chooseCategory);
 		currentInformation = (LinearLayout) view.findViewById(R.id.currentInformation);
 		currentInformation.setVisibility(View.GONE);
 		
 		category1 = (LinearLayout)view.findViewById(R.id.categoryChoose1);
 		category2 = (LinearLayout)view.findViewById(R.id.categoryChoose2);
 		category3 = (LinearLayout)view.findViewById(R.id.categoryChoose3);
 		category4 = (LinearLayout)view.findViewById(R.id.categoryChoose4);
 		category5 = (LinearLayout)view.findViewById(R.id.categoryChoose5);
 		
 		categoryViews.add(category1);
 		categoryViews.add(category2);
 		categoryViews.add(category3);
 		categoryViews.add(category4);
 		categoryViews.add(category5);
 
 		image1 = (ImageView) view.findViewById(R.id.categoryImageMain1);
 		image1.setOnClickListener(this);
 		image2 = (ImageView) view.findViewById(R.id.categoryImageMain2);
 		image2.setOnClickListener(this);
 		image3 = (ImageView) view.findViewById(R.id.categoryImageMain3);
 		image3.setOnClickListener(this);
 		image4 = (ImageView) view.findViewById(R.id.categoryImageMain4);
 		image4.setOnClickListener(this);
 		image5 = (ImageView) view.findViewById(R.id.categoryImageMain5);
 		image5.setOnClickListener(this);
 
 		LinearLayout linearLayout1 = (LinearLayout) view.findViewById(R.id.categoryChoose1);
 		linearLayout1.setOnClickListener(this);
 
 		LinearLayout linearLayout2 = (LinearLayout) view.findViewById(R.id.categoryChoose2);
 		linearLayout2.setOnClickListener(this);
 
 		LinearLayout linearLayout3 = (LinearLayout) view.findViewById(R.id.categoryChoose3);
 		linearLayout3.setOnClickListener(this);
 
 		LinearLayout linearLayout4 = (LinearLayout) view.findViewById(R.id.categoryChoose4);
 		linearLayout4.setOnClickListener(this);
 
 		LinearLayout linearLayout5 = (LinearLayout) view.findViewById(R.id.categoryChoose5);
 		linearLayout5.setOnClickListener(this);
 
 		/*
 		 * Nicht mehr aktuell -> Default ber Sternsymbol
 		switch(AppData.getInstance().getDefaultSportCategory())
 		{
 		case 1:
 			image1.setBackgroundResource(R.drawable.ausdauer_banner);
 			break;
 		case 2:
 			image2.setBackgroundResource(R.drawable.kraft_banner);
 			break;
 		case 3:
 			image3.setBackgroundResource(R.drawable.ball_banner);
 			break;
 		case 4:
 			image4.setBackgroundResource(R.drawable.gymnastik_banner);
 			break;
 		case 5:
 			image5.setBackgroundResource(R.drawable.leichte_banner);
 			break;
 		}
 		*/
 		
 		
 		STOP = false;
 		// ..................... Controls finden ................
 
 		btnStop = (Button) view.findViewById(R.id.stopActivity);
 		lblStarttimeOut = (TextView) view.findViewById(R.id.lblStartzeitOut);
 		lblTimeOut = (TextView) view.findViewById(R.id.lblSportzeitOut);
 		lblPointsOut = (TextView) view.findViewById(R.id.lblPunkteOut);
 		lblGroupmembersOut = (TextView) view.findViewById(R.id.lblGroupMembersOut);
 		lblBonusOut = (TextView) view.findViewById(R.id.lblBonusOut);
 
 		
 
 		// / ..............listeners....................
 		/* ................. stop button klick auf stopbutton................ */
 		btnStop.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				STOP = true;
 
 				// berechnungen durchfhren.
 				stop_time = System.currentTimeMillis();
 
 				duration = stop_time - start_time;
 
 			}
 		});
 
 		
 
 		return view;
 	}
 
 	public void onClick(View v)
 	{
 		switch (v.getId()) 
 		{
 		case R.id.categoryChoose1:
 			AppData.getInstance().setDefaultSportCategory(1);
 			image1.setBackgroundResource(R.drawable.ausdauer_banner);
 			image2.setBackgroundResource(R.drawable.kraft_square);
 			image3.setBackgroundResource(R.drawable.ball_square);
 			image4.setBackgroundResource(R.drawable.gymnastik_square);
 			image5.setBackgroundResource(R.drawable.leichte_square);
			selectedCategory = AppData.getInstance().getCategories().get(1);
 			selectCategoryView(v);
 			break;
 		case R.id.categoryChoose2:
 			AppData.getInstance().setDefaultSportCategory(2);
 			image2.setBackgroundResource(R.drawable.kraft_banner);
 			image1.setBackgroundResource(R.drawable.ausdauer_square);
 			image3.setBackgroundResource(R.drawable.ball_square);
 			image4.setBackgroundResource(R.drawable.gymnastik_square);
 			image5.setBackgroundResource(R.drawable.leichte_square);
			selectedCategory = AppData.getInstance().getCategories().get(2);
 			selectCategoryView(v);
 			break;
 		case R.id.categoryChoose3:
 			AppData.getInstance().setDefaultSportCategory(3);
 			image3.setBackgroundResource(R.drawable.ball_banner);
 			image1.setBackgroundResource(R.drawable.ausdauer_square);
 			image2.setBackgroundResource(R.drawable.kraft_square);
 			image4.setBackgroundResource(R.drawable.gymnastik_square);
 			image5.setBackgroundResource(R.drawable.leichte_square);
			selectedCategory = AppData.getInstance().getCategories().get(3);
 			selectCategoryView(v);
 			break;
 		case R.id.categoryChoose4:
 			AppData.getInstance().setDefaultSportCategory(4);
 			image4.setBackgroundResource(R.drawable.gymnastik_banner);
 			image1.setBackgroundResource(R.drawable.ausdauer_square);
 			image2.setBackgroundResource(R.drawable.kraft_square);
 			image3.setBackgroundResource(R.drawable.ball_square);
 			image5.setBackgroundResource(R.drawable.leichte_square);
			selectedCategory = AppData.getInstance().getCategories().get(4);
 			selectCategoryView(v);
 			break;
 		case R.id.categoryChoose5:
 			AppData.getInstance().setDefaultSportCategory(5);
 			image5.setBackgroundResource(R.drawable.leichte_banner);
 			image1.setBackgroundResource(R.drawable.ausdauer_square);
 			image2.setBackgroundResource(R.drawable.kraft_square);
 			image3.setBackgroundResource(R.drawable.ball_square);
 			image4.setBackgroundResource(R.drawable.gymnastik_square);
			selectedCategory = AppData.getInstance().getCategories().get(5);
 			selectCategoryView(v);
 			break;
 		}
 	}
 	
 	
 	private void selectCategoryView(View v){
 		
 		final View selectedView = v;
 		
 		final float positionY = v.getY();
 		
 		final TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -positionY+5);
 		ta.setDuration(500);
 		ta.setAnimationListener(new AnimationListener() {
 			
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				header.setVisibility(View.GONE);
 				for (LinearLayout ll: categoryViews) {
 					if (ll.getId()!=selectedView.getId()){
 						ll.setVisibility(View.GONE);
 					}
 				}
 				
 				// Show Timer and Buttons
 				currentInformation.setVisibility(View.VISIBLE);
 				setTimerTicker(view);
 				setGroupMembersDuringActivity(view); 
 				
 			}
 		});
 		
 		final AlphaAnimation aa = new AlphaAnimation(1, 0);
 		aa.setDuration(500);
 		aa.setAnimationListener(new AnimationListener() {
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//after views disappeared, animate selected view to top
 				
 				header.setAlpha(0);
 				for (LinearLayout ll: categoryViews){
 					if (ll.getId()!=selectedView.getId()) {
 						ll.setAlpha(0);
 					}	
 				}
 				
 				selectedView.startAnimation(ta);
 				
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// TODO Auto-generated method stub
 			}
 		});
 		
 		header.startAnimation(aa);
 		for(LinearLayout ll: categoryViews){
 			if (ll.getId() != v.getId()){
 				ll.startAnimation(aa);
 			}
 		}
 		
 		
 		
 	}
 	
 	public void setGroupMembersDuringActivity(View view) {
 		// Do something long
 
 		Runnable runnable = new Runnable() {
 			String sOut = ""; 
 			@Override
 			public void run() {
 				while (!STOP) {
 					
 					// da DB-fkt aufrufen
 					BoGroupMember member = AppData.getInstance().getCurrentMember(); 
 					if (member != null) {
 						System.out.println(member.toString()); 
 						//count_groupMembersDuringActivity = DBHandler.getActiveGroupMembers(member.user_id, member.group_id); 
 						count_groupMembersDuringActivity = AppData.getInstance().getActiveMembers().size();
 						
 					}
 					handler.post(new Runnable() {
 						@Override
 						public void run() {
 							// ticker setzen
 							if (count_groupMembersDuringActivity == 1)
 								sOut = String.format("Aktuell macht 1 weiteres Gruppenmitglied Sport", count_groupMembersDuringActivity); 
 							else 
 								sOut = String.format("Aktuell machen %d weitere Gruppenmitglieder Sport", count_groupMembersDuringActivity); 
 							
 							lblGroupmembersOut.setText(sOut);
 						}
 					});
 					try {
 						// nicht vergessen.. jetzte sekunde erst setzen. 
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	public void setTimerTicker(View view) {
 		// Do something long
 
 		// aktuelle startzeit bestimmen
 		start_time = System.currentTimeMillis();
 		lblStarttimeOut.setText(DateFormat.format("dd.MM.yyyy hh:mm", new Date(
 				start_time)));
 		
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				while (!STOP) {
 					
 					stop_time = System.currentTimeMillis();
 					duration = stop_time - start_time;
 					
 					handler.post(new Runnable() {
 						@Override
 						public void run() {
 							// ticker setzen
 							lblTimeOut.setText(DateFormat.format("mm:ss",
 									new Date(duration)));
 							//points = (duration/1000) * ((1000.0/3600.0)*selectedCategory.getCategory_intensity());
 							points += (((1000.0/3600.0)*selectedCategory.getCategory_intensity()));
 							lblPointsOut.setText(String.valueOf((int)points));
 							
 							bonusPoints += ((1000.0/3600.0)*(count_groupMembersDuringActivity*0.1));
 							lblBonusOut.setText(String.valueOf((int)bonusPoints));
 						}
 					});
 					try {
 						// nicht vergessen.. jetzte sekunde erst setzen. 
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 			}
 		};
 		new Thread(runnable).start();
 	}
 }
