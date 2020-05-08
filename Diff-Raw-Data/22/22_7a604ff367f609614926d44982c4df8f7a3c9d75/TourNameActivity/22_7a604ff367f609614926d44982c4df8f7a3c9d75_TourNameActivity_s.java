 package edu.stanford.mdocent;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import edu.stanford.mdocent.db.Constants;
 
 public class TourNameActivity extends Activity {
 
 	private static final String TAG = "TourNameActivity";
 	private final static int tourNameRequestCode = 2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.tourname);

 		Button loginButton = (Button) findViewById(R.id.button1);
 		loginButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				EditText nameText = (EditText)findViewById(R.id.editText1);
 				String nameStr = nameText.getText().toString();
 				EditText descriptionText = (EditText)findViewById(R.id.editText2);
 				String descriptionStr = descriptionText.getText().toString();
 				startCreateTour(nameStr, descriptionStr);
 
 			}
 		});
 	}
 	public void startCreateTour (String nameStr, String descriptionStr){
 		Intent intent = new Intent(this, CreateTourActivity.class );
 		intent.putExtra("tourName", nameStr);
 		intent.putExtra("tourDescription", descriptionStr);
 		startActivityForResult(intent, tourNameRequestCode);
 	}
 	@Override
 	public void onActivityResult(int requestCode,int resultCode,Intent data){
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == Constants.RESULT_RETURN){
 			Log.v(TAG, "RESULT_RETURN");
 			Intent intent = new Intent(this, MainPageActivity.class );
 			setResult(Constants.RESULT_RETURN, intent);
 			finish();
 		}
 		else{
 			Log.v(TAG, "RESULT_CANCELED");
 			Intent intent = new Intent(this, MainPageActivity.class );
 			setResult(RESULT_CANCELED, intent);
 			finish();
 		}
 	}
 
 
 }
