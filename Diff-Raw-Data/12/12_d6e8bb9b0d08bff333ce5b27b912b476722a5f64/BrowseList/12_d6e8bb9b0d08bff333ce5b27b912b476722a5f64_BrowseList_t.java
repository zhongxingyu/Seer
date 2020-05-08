 package ch.zhaw.warranty.photo;
 
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Spinner;
 import ch.zhaw.warranty.R;
 
 public class BrowseList extends Activity implements OnInitListener,
 		android.view.View.OnClickListener {
 
 	Spinner dropDownSpinner = null;
 	Button searchButton = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.browse_list_layout);
 	}
 	
 	
 	public BrowseList() {
 		// TODO Auto-generated constructor stub
 	}
 
//	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 	}
 
//	@Override
 	public void onInit(int status) {
 		searchButton = (Button) findViewById(R.id.btn_search);
 		BtnLisSearchButton();
 
 	}
 
 	private void BtnLisSearchButton() {
 		// TODO Listener For Search Button
 	}
 
 }
