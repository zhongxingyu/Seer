 package info.ohgita.bincalc_android;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TableRow;
 import android.widget.ToggleButton;
 
 import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class MainFragment extends SherlockFragment implements OnClickListener {
 	int selectedBasetypeId = -1; 
 	static int ID_BASETYPE_BIN =	100;
 	static int ID_BASETYPE_DEC =	200;
 	static int ID_BASETYPE_HEX =	300;
 	
 	int currentOperationModeId = -1;
 	static int ID_OPRMODE_PLUS = 1;
 	static int ID_OPRMODE_MINUS = 2;
 	static int ID_OPRMODE_MULTI = 3;
 	static int ID_OPRMODE_DIVIS = 4;
 	
 	boolean pref_keyVibration = false;
 	
 	View v = null;
 	
 	Vibrator vib;
 	
 	ViewPager baseinputsViewPager;
 	
 	@SuppressLint("NewApi")
 	@Override
 	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
 		/* inflating Fragment */
 		v = inflater.inflate(R.layout.fragment_main_portrait, container);
 		
 		/* baseinputsViewPager */
 		ViewPager baseinputsViewPager = (ViewPager)v.findViewById(R.id.baseinputsViewPager);
        PagerAdapter mPagerAdapter = new Adapter_BaseinputsViewPager(v.getContext(),this);
        baseinputsViewPager.setAdapter(mPagerAdapter);
 		
 		/* Event handler for Base-type ToggleButtons */
 		final ToggleButton tb_bin = (ToggleButton) v.findViewById(R.id.toggle_basetype_bin);
 		final ToggleButton tb_dec = (ToggleButton) v.findViewById(R.id.toggle_basetype_dec);
 		final ToggleButton tb_hex = (ToggleButton) v.findViewById(R.id.toggle_basetype_hex);
 		tb_bin.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if(isChecked == true){
 					switchBasetype(ID_BASETYPE_BIN);
 				}
 			}
 		});
 		tb_dec.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if(isChecked == true){
 					switchBasetype(ID_BASETYPE_DEC);
 				}
 			}
 		});
 		tb_hex.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if(isChecked == true){
 					switchBasetype(ID_BASETYPE_HEX);
 				}
 			}
 		});
 		
 		/* set Event-handler for key-buttons */
 		v.findViewById(R.id.keyButton0).setOnClickListener(this);
 		v.findViewById(R.id.keyButton1).setOnClickListener(this);
 		v.findViewById(R.id.keyButton2).setOnClickListener(this);
 		v.findViewById(R.id.keyButton3).setOnClickListener(this);
 		v.findViewById(R.id.keyButton4).setOnClickListener(this);
 		v.findViewById(R.id.keyButton5).setOnClickListener(this);
 		v.findViewById(R.id.keyButton6).setOnClickListener(this);
 		v.findViewById(R.id.keyButton7).setOnClickListener(this);
 		v.findViewById(R.id.keyButton8).setOnClickListener(this);
 		v.findViewById(R.id.keyButton9).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpPl).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpMi).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpMp).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpDi).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonEq).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonPo).setOnClickListener(this);
 		
 		/* initialize vibratation */
 		vib = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
 		
 		/* loading preferences */
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
 		pref_keyVibration = pref.getBoolean(getResources().getString(R.string.pref_item_keyVibration_key), false);
 		
 		/* return inflated view */
 		return v;
 	}
 	
 	/**
 	 * calculate base-number
 	 */
 	public void calculate( ){
 		String value = getCurrent_Baseinput_EditText().getText().toString();
 		EditText et_bin = (EditText) v.findViewById(R.id.editText_baseinput_bin);
 		EditText et_dec = (EditText) v.findViewById(R.id.editText_baseinput_dec);
 		EditText et_hex = (EditText) v.findViewById(R.id.editText_baseinput_hex);
 		
 		if(selectedBasetypeId == ID_BASETYPE_BIN){
 			//TODO not implemented
 			et_dec.setText(value);
			et_dec.setText(value);
 		}else if(selectedBasetypeId == ID_BASETYPE_DEC){
 			//TODO not implemented
 			et_bin.setText(value);
 			et_hex.setText(value);
 		}else if(selectedBasetypeId == ID_BASETYPE_HEX){
 			//TODO not implemented
 			et_bin.setText(value);
 			et_dec.setText(value);
 		}
 	}
 	
 	/** All-Clear calculator
 	 */
 	public void inputAllClear(){
 		EditText et = getCurrent_Baseinput_EditText();
 		et.setText("0");
 		calculate();
 	}
 	
 	/**
 	 * input base-number key
 	 * @param str input-Key
 	 */
 	public void inputBasenumber(String str){
 		EditText et = getCurrent_Baseinput_EditText();
 		if(et.getText().toString().contentEquals("0")){
 			et.setText(str);
 		}else{
 			et.setText(et.getText().toString() + str);
 		}
 		calculate();
 	}
 	
 	/**
 	 * input Operation key
 	 */
 	public void inputOpr(int oprmodeId){
 		
 	}
 	
 	/**
 	 * input equall key
 	 */
 	public void inputEquall(){
 		calculate();
 	}
 
 	/**
 	 * get current base-type(container) TableRow object
 	 */
 	public TableRow getCurrent_Basetype_TableRow(){
 		if(selectedBasetypeId == ID_BASETYPE_BIN){
 			return (TableRow) v.findViewById(R.id.tableRow_basetype_bin);
 		}else if(selectedBasetypeId == ID_BASETYPE_DEC){
 			return (TableRow) v.findViewById(R.id.tableRow_basetype_dec);
 		}else if(selectedBasetypeId == ID_BASETYPE_HEX){
 			return (TableRow) v.findViewById(R.id.tableRow_basetype_hex);
 		}
 		return null;
 	}
 	
 	/**
 	 * get current base-type ToggleButton object
 	 */
 	public ToggleButton getCurrent_Basetype_ToggleButton(){
 		if(selectedBasetypeId == ID_BASETYPE_BIN){
 			return (ToggleButton) v.findViewById(R.id.toggle_basetype_bin);
 		}else if(selectedBasetypeId == ID_BASETYPE_DEC){
 			return (ToggleButton) v.findViewById(R.id.toggle_basetype_dec);
 		}else if(selectedBasetypeId == ID_BASETYPE_HEX){
 			return (ToggleButton) v.findViewById(R.id.toggle_basetype_hex);
 		}
 		return null;
 	}
 	
 	/**
 	 * get current base-input(container) TableRow object
 	 */
 	public TableRow getCurrent_Baseinput_TableRow(){
 		if(selectedBasetypeId == ID_BASETYPE_BIN){
 			return (TableRow) v.findViewById(R.id.tableRow_baseinput_bin);
 		}else if(selectedBasetypeId == ID_BASETYPE_DEC){
 			return (TableRow) v.findViewById(R.id.tableRow_baseinput_dec);
 		}else if(selectedBasetypeId == ID_BASETYPE_HEX){
 			return (TableRow) v.findViewById(R.id.tableRow_baseinput_hex);
 		}
 		return null;
 	}
 
 	/**
 	 * get current base-input EditText object
 	 */
 	public EditText getCurrent_Baseinput_EditText(){
 		if(selectedBasetypeId == ID_BASETYPE_BIN){
 			return (EditText) v.findViewById(R.id.editText_baseinput_bin);
 		}else if(selectedBasetypeId == ID_BASETYPE_DEC){
 			return (EditText) v.findViewById(R.id.editText_baseinput_dec);
 		}else if(selectedBasetypeId == ID_BASETYPE_HEX){
 			return (EditText) v.findViewById(R.id.editText_baseinput_hex);
 		}
 		return null;
 	}
 
 	/**
 	 * switch base-type
 	 * @param basetypeId	Base-type ID number
 	 */
 	public void switchBasetype(int basetypeId){
 		selectedBasetypeId = basetypeId;
 		
 		TableRow tr_type_bin = (TableRow) v.findViewById(R.id.tableRow_basetype_bin);
 		ToggleButton tb_type_bin = (ToggleButton) v.findViewById(R.id.toggle_basetype_bin);
 		TableRow tr_input_bin = (TableRow) v.findViewById(R.id.tableRow_baseinput_bin);
 		
 		TableRow tr_type_dec = (TableRow) v.findViewById(R.id.tableRow_basetype_dec);
 		ToggleButton tb_type_dec = (ToggleButton) v.findViewById(R.id.toggle_basetype_dec);
 		TableRow tr_input_dec = (TableRow) v.findViewById(R.id.tableRow_baseinput_dec);
 		
 		TableRow tr_type_hex = (TableRow) v.findViewById(R.id.tableRow_basetype_hex);
 		ToggleButton tb_type_hex = (ToggleButton) v.findViewById(R.id.toggle_basetype_hex);
 		TableRow tr_input_hex = (TableRow) v.findViewById(R.id.tableRow_baseinput_hex);
 		
 		/* reset under-line (base-types & base-inputs) */
 		tr_type_bin.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_default));
 		tr_type_dec.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_default));
 		tr_type_hex.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_default));
 
 		tr_input_bin.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_default));
 		tr_input_dec.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_default));
 		tr_input_hex.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_default));
 		
 		tb_type_bin.setChecked(false);
 		tb_type_bin.setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_default));
 		tb_type_dec.setChecked(false);
 		tb_type_dec.setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_default));
 		tb_type_hex.setChecked(false);
 		tb_type_hex.setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_default));
 		
 		/* activate Base-type */
 		getCurrent_Basetype_ToggleButton().setChecked(true);
 		getCurrent_Basetype_ToggleButton().setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_active));
 		getCurrent_Basetype_TableRow().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_active));
 		getCurrent_Baseinput_TableRow().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.basetype_line_active));
 		
 	}
 
 	/* Event-handler for buttons */
 	@Override
 	public void onClick(View v) {
 		if(pref_keyVibration){
 			vib.vibrate(50);
 		}
 		switch(v.getId()){
 			/* Key-buttons (0-9) */
 			case R.id.keyButton0:
 				inputBasenumber("0");
 				break;
 			case R.id.keyButton1:
 				inputBasenumber("1");
 				break;
 			case R.id.keyButton2:
 				inputBasenumber("2");
 				break;
 			case R.id.keyButton3:
 				inputBasenumber("3");
 				break;
 			case R.id.keyButton4:
 				inputBasenumber("4");
 				break;
 			case R.id.keyButton5:
 				inputBasenumber("5");
 				break;
 			case R.id.keyButton6:
 				inputBasenumber("6");
 				break;
 			case R.id.keyButton7:
 				inputBasenumber("7");
 				break;
 			case R.id.keyButton8:
 				inputBasenumber("8");
 				break;
 			case R.id.keyButton9:
 				inputBasenumber("9");
 				break;
 			
 			/* operator-button */
 			case R.id.keyButtonOpPl:
 				inputOpr(ID_OPRMODE_PLUS);
 				break;
 			case R.id.keyButtonOpMi:
 				inputOpr(ID_OPRMODE_MINUS);
 				break;
 			case R.id.keyButtonOpMp:
 				inputOpr(ID_OPRMODE_MULTI);
 				break;
 			case R.id.keyButtonOpDi:
 				inputOpr(ID_OPRMODE_DIVIS);
 				break;
 			
 			/* Equall-button */
 			case R.id.keyButtonEq:
 				inputEquall();
 				break;
 		};
 	}
 }
