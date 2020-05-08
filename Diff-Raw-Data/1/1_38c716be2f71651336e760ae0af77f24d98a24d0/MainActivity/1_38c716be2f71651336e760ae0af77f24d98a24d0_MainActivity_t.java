 package edu.mines.csci498.bwisdom.lunchlist;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.app.TabActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends TabActivity {
 
 	Restaurant r = new Restaurant();
 	Restaurant current;
 	List<Restaurant> model = new ArrayList<Restaurant>();
 	RestaurantAdapter adapter; 
 	EditText name;
 	EditText address;
 	RadioGroup types;
 	EditText notes;
 	AtomicBoolean isActive = new AtomicBoolean(true);
 	int progress;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_PROGRESS);
 		setContentView(R.layout.activity_main);
 
 		name = (EditText)findViewById(R.id.name);
 		address = (EditText)findViewById(R.id.addr);
 		types = (RadioGroup)findViewById(R.id.types);
 		notes = (EditText)findViewById(R.id.notes);
 		
 		Button save = (Button) findViewById(R.id.save);
 
 		save.setOnClickListener(onSave);
 		
 		ListView list = (ListView) findViewById(R.id.restaurants);
 		
 		adapter = new RestaurantAdapter();
 		list.setAdapter(adapter);
 		
 		TabHost.TabSpec spec = getTabHost().newTabSpec("tag1");
 		
 		spec.setContent(R.id.restaurants);
 		spec.setIndicator("List", getResources().getDrawable(R.drawable.list));
 		getTabHost().addTab(spec);
 		
 		spec = getTabHost().newTabSpec("tag2");
 		spec.setContent(R.id.details);
 		
 		spec.setIndicator("Details", getResources().getDrawable(R.drawable.restaurant));
 		getTabHost().addTab(spec);
 		
 		getTabHost().setCurrentTab(0);
 		
 		list.setOnItemClickListener( onListClick);		
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		isActive.set(false);
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		
 		isActive.set(true);
 		
 		if(progress>0){
 			startWork();
 		}
 	}
 	
 	private void startWork(){
 		setProgressBarVisibility(true);
 		new Thread(longTask).start();
 	}
 	
 	//Method used to test a "LONG" Background thread process 
 	private void doSomeLongAssWork(final int incr){
 		runOnUiThread(new Runnable(){
 			public void run(){
 				progress+=incr;
 				setProgress(progress);
 			}
 		});
 		SystemClock.sleep(250);
 	}
 	
 	private Runnable longTask = new Runnable() {
 		public void run() {
 			for (int i = progress; i < 10000 && isActive.get(); i += 200) {
 				doSomeLongAssWork(200);
 			}
 			if (isActive.get()) {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						setProgressBarVisibility(false);
						progress = 0;
 					}
 				});
 			}
 		}
 	};
 	
 	private View.OnClickListener onSave = new View.OnClickListener() {
 		public void onClick(View v) {
 			current = new Restaurant();	
 			EditText name = (EditText) findViewById(R.id.name);
 			EditText address = (EditText) findViewById(R.id.addr);
 			EditText notes = (EditText) findViewById(R.id.notes);
 			
 			current.setName(name.getText().toString());
 			current.setAddress(address.getText().toString());
 			current.setNotes(notes.getText().toString());
 			
 			RadioGroup types = (RadioGroup) findViewById(R.id.types);
 			
 			switch(types.getCheckedRadioButtonId()){
 				case R.id.sit_down:
 					current.setType("sit_down");
 					break;
 				case R.id.takeout:
 					current.setType("takeout");
 					break;
 				case R.id.delivery:
 					current.setType("delivery");
 					break;
 			}	
 			adapter.add(current);  
 		}
 	};
 	
 	private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			current = model.get(position);
 			
 			name.setText(current.getName());
 			address.setText(current.getAddress());
 			notes.setText(current.getNotes());
 			
 			if(current.getType().equals("sit_down")){
 				types.check(R.id.sit_down);
 			}else if(current.getType().equals("takeout")){
 				types.check(R.id.takeout);
 			}else{
 				types.check(R.id.delivery);
 			}
 			getTabHost().setCurrentTab(1);
 
 		}
 	};
 
 	
 	@Override 
 	public boolean onCreateOptionsMenu(Menu menu){
 		new MenuInflater(this).inflate(R.menu.options, menu);
 		
 		return(super.onCreateOptionsMenu(menu));
 	}
 	
 	@Override 
 	public boolean onOptionsItemSelected(MenuItem item){
 		if(item.getItemId() == R.id.toast){
 			String message = "No Restaurant Selected";
 			
 			if (current != null){
 				message=current.getNotes();
 			}
 			
 			Toast.makeText(this,message,Toast.LENGTH_LONG).show();
 			
 			return(true);
 		}else if(item.getItemId() == R.id.run){
 			
 			startWork();
 			
 			return(true);
 		}
 		return(super.onOptionsItemSelected(item));
 	}
 	
 	class RestaurantAdapter extends ArrayAdapter<Restaurant>{
 		RestaurantAdapter(){
 			super(MainActivity.this, android.R.layout.simple_list_item_1,model);
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			RestaurantHolder holder = null;
 
 			if (row == null) {
 				LayoutInflater inflater = getLayoutInflater();
 
 				row = inflater.inflate(R.layout.row, parent, false);
 				holder = new RestaurantHolder(row);
 				row.setTag(holder);
 			} else {
 				holder = (RestaurantHolder) row.getTag();
 			}
 
 			holder.populateFrom(model.get(position));
 
 			return row;
 		}
 	}
 	
 	static class RestaurantHolder{
 		private TextView name = null;
 		private TextView address = null;
 		private ImageView icon = null; 
 		
 		RestaurantHolder(View row) {
 			name = (TextView) row.findViewById(R.id.title);
 			address = (TextView) row.findViewById(R.id.address);
 			icon = (ImageView) row.findViewById(R.id.icon);
 		}
 		
 		void populateFrom(Restaurant r) {
 			name.setText(r.getName());
 			address.setText(r.getAddress());
 
 			if (r.getType().equals("sit_down")) {
 				icon.setImageResource(R.drawable.sit_down);
 			} else if (r.getType().equals("takeout")) {
 				icon.setImageResource(R.drawable.takeout);
 			} else {
 				icon.setImageResource(R.drawable.delivery);
 			}
 		}
 		
 	}
 }
