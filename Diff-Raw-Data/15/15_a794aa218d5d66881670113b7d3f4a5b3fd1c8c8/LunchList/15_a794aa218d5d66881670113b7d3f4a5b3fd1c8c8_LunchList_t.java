 package csci498.bidixon.lunchlist;
 
 import java.util.*;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 import android.view.View;
 import android.widget.*;
 
 public class LunchList extends Activity {
 	
 	List<Restaurant> model = new ArrayList<Restaurant>();
 	private RadioGroup radioGroup4minute;
 	private RadioButton radioButtonJiyoon;
 	private RadioButton radioButtonJihyun;
 	private RadioButton radioButtonGayoon;
 	private RadioButton radioButtonSohyun;
 	private RadioButton radioButtonHyuna;
 	private RadioButton radioButtonHara;
 	private RadioButton radioButtonGyuri;
 	private RadioButton radioButtonNicole;
 	private RadioButton radioButtonJiyoung;
 	private RadioButton radioButtonSeungyeon;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_lunch_list);
         
         addSomeRadioButtons();
         Button save = (Button) findViewById(R.id.save);
         save.setOnClickListener(onSave);
     }
     
     private void addSomeRadioButtons(){
     	radioGroup4minute = (RadioGroup) findViewById(R.id.types);
         
     	// 4Minute member radio buttons
         radioButtonJiyoon = new RadioButton(this);
         radioButtonJiyoon.setText("Jiyoon");
         radioButtonJihyun = new RadioButton(this);
         radioButtonJihyun.setText("Jihyun");
         radioButtonGayoon = new RadioButton(this);
         radioButtonGayoon.setText("Gayoon");
         radioButtonSohyun = new RadioButton(this);
         radioButtonSohyun.setText("Sohyun");
         radioButtonHyuna = new RadioButton(this);
         radioButtonHyuna.setText("Hyuna");
         
         // Kara member radio buttons
         radioButtonHara = new RadioButton(this);
         radioButtonHara.setText("Hara");
         radioButtonGyuri = new RadioButton(this);
         radioButtonGyuri.setText("Gyuri");
         radioButtonNicole = new RadioButton(this);
         radioButtonNicole.setText("Nicole");
         radioButtonJiyoung = new RadioButton(this);
         radioButtonJiyoung.setText("Jiyoung");
         radioButtonSeungyeon = new RadioButton(this);
         radioButtonSeungyeon.setText("Seungyeon");
         
         // add buttons to radio button group
         radioGroup4minute.addView(radioButtonJiyoon);
         radioGroup4minute.addView(radioButtonJihyun);
         radioGroup4minute.addView(radioButtonGayoon);
         radioGroup4minute.addView(radioButtonSohyun);
         radioGroup4minute.addView(radioButtonHyuna);
         
         radioGroup4minute.addView(radioButtonHara);
         radioGroup4minute.addView(radioButtonGyuri);
         radioGroup4minute.addView(radioButtonNicole);
         radioGroup4minute.addView(radioButtonJiyoung);
         radioGroup4minute.addView(radioButtonSeungyeon);
     }
     
     private View.OnClickListener onSave = new View.OnClickListener() {
 		public void onClick(View v) {
			Restaurant r = new Restaurant();
 			EditText name = (EditText) findViewById(R.id.name);
 			EditText address = (EditText) findViewById(R.id.addr);
 			
			r.setName(name.getText().toString());
			r.setAddress(address.getText().toString());
 			
 			RadioGroup types = (RadioGroup) findViewById(R.id.types);
 			
 			switch (types.getCheckedRadioButtonId()){
 			case R.id.sit_down:
				r.setType("sit_down");
 				break;
 			case R.id.take_out:
				r.setType("take_out");
 				break;
 			case R.id.delivery:
				r.setType("delivery");
 				break;
 			}
 		}
 	};
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_lunch_list, menu);
         return true;
     }
 }
