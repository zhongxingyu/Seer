 package com.NbodySimulator;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 public class NbodySimulatorActivity extends Activity {
 	private SimDataModel dataModel;
 	private Button start_button;
 	private Runnable runnable;
 	private Handler handler = new Handler();
 	private SimView simView;
 	private boolean running;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         dataModel = new SimDataModel();
         simView = new SimView(this, dataModel);
         running = false;
         
 		runnable = new Runnable(){
 			public void run(){
 				if(running){
 					dataModel.calc();
 					simView.invalidate();
 				}
 				handler.post(this);
 			}
 		};

         setContentView(R.layout.main);
         start_button = (Button)findViewById(R.id.start_button);
         start_button.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v){
         		if(running==false){
         			start_button.setText("stop");
         			running = true;
         		}else{
         			start_button.setText("start");
         			running = false;
         		}
         	}
         });
         LinearLayout ll = (LinearLayout)findViewById(R.id.layout);
         ll.addView(simView);
 		handler.post(runnable);
 	}
     public boolean onCreateOptionsMenu(Menu menu){
     	menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "load");
     	menu.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, "save");
     	menu.add(Menu.NONE, Menu.FIRST + 3, Menu.NONE, "reset");
     	menu.add(Menu.NONE, Menu.FIRST + 4, Menu.NONE, "footprint");
     	return super.onCreateOptionsMenu(menu);
     }
     public boolean onOptionsItemSelected(MenuItem item){
     	boolean ret = true;
     	switch(item.getItemId()){
     	default:
     		ret = super.onOptionsItemSelected(item);
     		break;
     	case Menu.FIRST + 1:
     		dataModel.readData();
     		ret = true;
     		break;
     	case Menu.FIRST + 2:
     		dataModel.writeData();
     		ret = true;
     		break;
     	case Menu.FIRST + 3:
     		dataModel.reset();
     		dataModel.writeData();
     		ret = true;
     		break;
     	case Menu.FIRST + 4:
     		dataModel.toggleShowFootprint();
     		ret = true;
     		break;
     	}
     	return ret;
     }
 }
