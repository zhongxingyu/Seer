 package at.ac.tuwien.sportmate;
 
 import java.util.ArrayList;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView.FindListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SelectTargetFragment extends Fragment implements EventInterface {
 	
 	private View view;
 	
 	BoGroupMember member;
 	
 	private int ausdauer_count;
 	private int kraft_count;
 	private int ballspiel_count;
 	private int gym_count;
 	private int leichte_count;
 	
 	TextView ac;
 	TextView kc;
 	TextView sc;
 	TextView gc;
 	TextView lc;
 	
 	//minute steps to increas or decrease targets
 	int minute_steps = 15;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.loadDataFromDB(1);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 	}
 	
 	@Override
 	public void onResume() {
 		SportMateApplication.getApplication().registerListener(this.getClass().getName(), this);
 		super.onResume();
 	}
 	
 	@Override
 	public void onStop() {
 		SportMateApplication.getApplication().unregisterListener(this.getClass().getName());
 		super.onStop();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		view = inflater.inflate(R.layout.select_target, container, false);
 		
 		//Ausdauer 
 		ac = (TextView)view.findViewById(R.id.ausdauer_count);
 		((Button) view.findViewById(R.id.ausdauer_down)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (ausdauer_count > 0)	ausdauer_count -= minute_steps;
 				ac.setText(String.format("%3d min", ausdauer_count));
 				refreshPoints();
 			}
 		});
 		
 		((Button) view.findViewById(R.id.ausdauer_up)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ausdauer_count += minute_steps;
 				ac.setText(String.format("%3d min", ausdauer_count));
 				refreshPoints();
 			}
 		});
 		
 		//Kraft
 		kc = (TextView)view.findViewById(R.id.kraft_count);
 		((Button) view.findViewById(R.id.kraft_down)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (kraft_count > 0)	kraft_count -= minute_steps;
 				kc.setText(String.format("%3d min", kraft_count));
 				refreshPoints();
 			}
 		});
 		
 		((Button) view.findViewById(R.id.kraft_up)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				kraft_count += minute_steps;
 				kc.setText(String.format("%3d min", kraft_count));
 				refreshPoints();
 			}
 		});
 		
 		//Ballspiel
 		sc = (TextView)view.findViewById(R.id.ballspiel_count);
 		((Button) view.findViewById(R.id.ballspiel_down)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (ballspiel_count > 0)	ballspiel_count -= minute_steps;
 				sc.setText(String.format("%3d min", ballspiel_count));
 				refreshPoints();
 			}
 		});
 		
 		((Button) view.findViewById(R.id.ballspiel_up)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ballspiel_count += minute_steps;
 				sc.setText(String.format("%3d min", ballspiel_count));
 				refreshPoints();
 			}
 		});
 		
 		//Gymnastik
 		gc = (TextView)view.findViewById(R.id.gymnastik_count);
 		((Button) view.findViewById(R.id.gymnastik_down)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (gym_count > 0)	gym_count -= minute_steps;
 				gc.setText(String.format("%3d min", gym_count));
 				refreshPoints();
 			}
 		});
 		
 		((Button) view.findViewById(R.id.gymnastik_up)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				gym_count += minute_steps;
 				gc.setText(String.format("%3d min", gym_count));
 				refreshPoints();
 			}
 		});
 		
 		//Leichte
 		lc = (TextView)view.findViewById(R.id.leichte_count);
 		((Button) view.findViewById(R.id.leichte_down)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (leichte_count > 14)	leichte_count -= minute_steps;
 				lc.setText(String.format("%3d min", leichte_count));
 				refreshPoints();
 			}
 		});
 		
 		((Button) view.findViewById(R.id.leichte_up)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				leichte_count += minute_steps;
 				lc.setText(String.format("%3d min", leichte_count));
 				refreshPoints();
 			}
 		});
 		
 		initTargets();
 		
 		return view;
 	}
 	
 	private void refreshPoints(){
 		//Gesamtcount
 		Log.d(this.getClass().getSimpleName(), "refreshing Points");
 		TextView gesamt = (TextView)view.findViewById(R.id.gesamt_count);
 		double punkte = ausdauer_count*1.2 +
 						 kraft_count*1.1 +
 						 ballspiel_count*1.0 +
 						 gym_count*0.9 +
 						 leichte_count*0.8;
 		gesamt.setText(" " + String.valueOf((int)punkte));
 	}
 	
 	private void initTargets(){
 		
 		//daten aus db
 		ausdauer_count = member.getWeeklyTargetCategoryMins(1); 
 		kraft_count = member.getWeeklyTargetCategoryMins(2);
 		ballspiel_count = member.getWeeklyTargetCategoryMins(3);
 		gym_count = member.getWeeklyTargetCategoryMins(4);
 		leichte_count = member.getWeeklyTargetCategoryMins(5);
 		
 		ac.setText(String.format("%3d min", ausdauer_count));
 		kc.setText(String.format("%3d min", kraft_count));
 		sc.setText(String.format("%3d min", ballspiel_count));
 		gc.setText(String.format("%3d min", gym_count));
 		lc.setText(String.format("%3d min", leichte_count));
 		
 		refreshPoints();
 		
 	}
 
 	@Override
 	public void eventA() {
 		System.out.println("testEventStartetInClass: "+this.getClass().getName());
 	}
 	
 	private void loadDataFromDB(int user_id){
		member = DBHandler.getGroupMember(1);
 		member.weeklyTargets = DBHandler.getWeeklyTargetsFromUser(member.getUser_id());
 	}
 }
