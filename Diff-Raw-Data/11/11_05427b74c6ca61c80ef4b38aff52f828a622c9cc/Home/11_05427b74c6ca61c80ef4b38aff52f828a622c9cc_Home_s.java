 package bas.sie.Antonius;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class Home extends SherlockFragment implements OnClickListener {
 
 	Button mBtnDaySchedule;
 	Button mBtnStSchedule;
 	Button mBtnChanges;
 	Button mBtnDayClass;
 	EditText mEtxtStudentNum;
 	EditText mEtxtClass;
 	String UserInput;
 	String Title;
 	public static final String PREFS_NAMES = "bas.sie.Antonius_preferences";
 	public static final String StudentID = "StudentID";
 	public static final String Class = "ClassID";
 	private static String mainUrl = "http://www.carmelcollegegouda.nl/site_ant/";
 	private static String endUrl = ".htm";
 	private static String[] myUrls = { "roosters/dagroosters/Lee_V1_",
 			"roosters/standaardroosters/Lee1_",
 			"roosters/roosterwijzigingen/Ver_Kla_",
 			"roosters/dagroosters/Kla_V1_" };
 	SharedPreferences prefs;
 	String namestore1 = "";
 	String namestore2 = "";
 	private View mainLayout;
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.home, container, false);
 		this.mainLayout = v;
 		return mainLayout;
 		}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		prefs = getActivity().getSharedPreferences(PREFS_NAMES, 0);
 
 		mBtnDaySchedule = (Button) getActivity().findViewById(R.id.btnDaySchedule);
 		mBtnDaySchedule.setOnClickListener(this);
 		mBtnStSchedule = (Button) getActivity().findViewById(R.id.btnStSchedule);
 		mBtnStSchedule.setOnClickListener(this);
 		mBtnChanges = (Button) getActivity().findViewById(R.id.btnChanges);
 		mBtnChanges.setOnClickListener(this);
 		mBtnDayClass = (Button) getActivity().findViewById(R.id.btnDayClass);
 		mBtnDayClass.setOnClickListener(this);
 		mEtxtStudentNum = (EditText) getActivity().findViewById(R.id.eTxtStudentNum);
 		mEtxtStudentNum.setOnClickListener(this);
 		mEtxtClass = (EditText) getActivity().findViewById(R.id.eTxtClass);
 		mEtxtClass.setOnClickListener(this);
 
 		getMyPreferences();
 		mEtxtStudentNum.setText(namestore1);
 		mEtxtClass.setText(namestore2);		
 	}
 
 	private String makeUrl(int index) {
 		String s = mainUrl + myUrls[index] + UserInput + endUrl;
 		return s;
 	}// makeurl
 
 	private void getMyPreferences() {
 		namestore1 = prefs.getString(StudentID, "");
 		namestore2 = prefs.getString(Class, "");
 	}// getpreferences
 
 	public void onBackPressed() {
 		System.exit(0);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btnDaySchedule:
 			UserInput = mEtxtStudentNum.getText().toString();
 			Title = "Dagrooster";
 
 			Intent i = new Intent();
            i.setClass(getActivity(), MyWebView.class);
 			i.putExtra("home", makeUrl(0));
 			i.putExtra("title", Title);
 			startActivityForResult(i, 0);
 			break;
 		case R.id.btnStSchedule:
 			UserInput = mEtxtStudentNum.getText().toString();
 			Title = "Standaardrooster";
 
 			Intent i1 = new Intent();
            i1.setClass(getActivity(), MyWebView.class);
 			i1.putExtra("home", makeUrl(1));
 			i1.putExtra("title", Title);
 			startActivityForResult(i1, 0);
 			break;
 		case R.id.btnChanges:
 			UserInput = mEtxtClass.getText().toString();
 			UserInput = UserInput.replace("-", "_");
 			Title = "Wijzigingen klas";
 
 			Intent i2 = new Intent();
            i2.setClass(getActivity(), MyWebView.class);
 			i2.putExtra("home", makeUrl(2));
 			i2.putExtra("title", Title);
 			startActivityForResult(i2, 0);
 			break;
 		case R.id.btnDayClass:
 			UserInput = mEtxtStudentNum.getText().toString();
 			UserInput = UserInput.replace("-", "_");
 			Title = "Standaardrooster";
 
 			Intent i3 = new Intent();
            i3.setClass(getActivity(), MyWebView.class);
 			i3.putExtra("home", makeUrl(1));
 			i3.putExtra("title", Title);
 			startActivityForResult(i3, 0);
 			break;
 		}
 
 	}
 }
