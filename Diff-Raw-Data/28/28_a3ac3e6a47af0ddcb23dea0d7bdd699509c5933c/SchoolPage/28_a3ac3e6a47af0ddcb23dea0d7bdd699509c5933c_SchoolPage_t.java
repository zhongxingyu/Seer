 package mwhs.ap.bkat.app;
 
 import java.util.ArrayList;
 
 import mwhs.ap.doan.app.R;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SchoolPage extends Activity implements OnClickListener {
 	private School s;
 	private Button email;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.college);
 		
 		Bundle b = this.getIntent().getExtras();
 		 s = (School) b.get("school");
 
 		
 		setTextViews();
 		
 		String[] majors = new String[2];
 		majors[0]= "Computer Science";
 		majors[1]= "Math";		
		//s.setMajors(majors);
 		
 		String[] sports = new String[2];
 		sports[0]= "Hockey";
 		sports[1]= "Football";		
 		s.setSports(sports);
 		
 		
 		ListView m_listview = (ListView) findViewById(R.id.list_majors);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, s.getMajors());
 		m_listview.setAdapter(adapter);
 
 	}
 	
 	private void setTextViews(){
 		TextView college = (TextView) findViewById(R.id.curr_college);
 		college.setText(s.getSchoolName());
 		
 		TextView setting = (TextView) findViewById(R.id.curr_setting);
 		setting.setText(s.getSetting());
 		
 		TextView inTuition = (TextView) findViewById(R.id.curr_in_tuition);
 		inTuition.setText("" + s.getTuitionInState());
 		
 		TextView outTuition = (TextView) findViewById(R.id.curr_out_tuition);
 		outTuition.setText("" + s.getTuitionOutOfState());
 		
 		TextView size = (TextView) findViewById(R.id.curr_size);
 		size.setText("" + s.getTotalUndergrads());
 		
 		TextView housing = (TextView) findViewById(R.id.curr_housing);
 		housing.setText("" + s.getRoomAndBoardCost());
 		
 		email = (Button) findViewById(R.id.toggleButton1);
 		email.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.toggleButton1:
 			Intent i2 = new Intent(Intent.ACTION_SEND);
 			i2.setType("text/plain");
 			i2.putExtra(Intent.EXTRA_SUBJECT, "Search Results");
 			i2.putExtra(Intent.EXTRA_TEXT, "Name: " + s.getSchoolName());
 			try {
 				startActivity(Intent.createChooser(i2, "Send mail..."));
 			} catch (android.content.ActivityNotFoundException ex) {
 				Toast.makeText(SchoolPage.this,
 						"There are no email clients installed.",
 						Toast.LENGTH_SHORT).show();
 			}
 
 		}
 	}
 }
