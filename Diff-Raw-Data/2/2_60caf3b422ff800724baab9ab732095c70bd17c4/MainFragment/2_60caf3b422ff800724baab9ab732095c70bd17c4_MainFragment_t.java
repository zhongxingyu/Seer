 package info.ohgita.bincalc_android;
 
 import java.util.LinkedList;
 
 import info.ohgita.bincalc_android.calc.BaseConverter;
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
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
 	
 	int DEFAULT_VIBRATION_MSEC = 25;
 	
 	boolean pref_keyVibration = false;
 	
 	Calculator calc;
 	BaseConverter baseconv;
 	
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
 		v.findViewById(R.id.keyButtonA).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonB).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonC).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonD).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonE).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonF).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpPl).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpMi).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpMp).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonOpDi).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonEq).setOnClickListener(this);
 		v.findViewById(R.id.keyButtonPo).setOnClickListener(this);
 		
 		/* initialize calculator class */
 		calc = new Calculator();
 		
 		/* initialize base-converter class */
 		baseconv = new BaseConverter();
 		
 		
 		/* initialize vibration */
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
 		Log.d("binCalc", "calculate()");
 		String value = getCurrent_Baseinput_EditText().getText().toString();
 		EditText et_bin = (EditText) v.findViewById(R.id.editText_baseinput_bin);
 		EditText et_dec = (EditText) v.findViewById(R.id.editText_baseinput_dec);
 		EditText et_hex = (EditText) v.findViewById(R.id.editText_baseinput_hex);
 		
 		et_bin.setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_default));
 		et_dec.setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_default));
 		et_hex.setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_default));
 		
 		LinkedList<String> parsedList = null;
 		try{
 			parsedList = calc.parseToList(value);
 		}catch(NullPointerException e){
 			Log.d("binCalc", e.toString());
 			getCurrent_Baseinput_EditText().setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_error));
 		}catch(NumberFormatException e){
 			Log.d("binCalc", e.toString());
 			getCurrent_Baseinput_EditText().setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_error));
 		};
 		
 		try{
 			if(selectedBasetypeId == ID_BASETYPE_BIN){
 				et_dec.setText( calc.listBaseConv(parsedList, 2, 10) );
 				et_hex.setText( calc.listBaseConv(parsedList, 2, 16) );
 			}else if(selectedBasetypeId == ID_BASETYPE_DEC){
 				et_bin.setText( calc.listBaseConv(parsedList, 10, 2) );
 				et_hex.setText( calc.listBaseConv(parsedList, 10, 16) );
 			}else if(selectedBasetypeId == ID_BASETYPE_HEX){
 				et_bin.setText( calc.listBaseConv(parsedList, 16, 2) );
 				et_dec.setText( calc.listBaseConv(parsedList, 16, 10) );
 			}
 		}catch (Exception e){
 			Log.e("binCalc", "listBaseConv error..."+e.toString());
 			getCurrent_Baseinput_EditText().setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_error));
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
 		
 		/* HEX alphabet */
 		if(str.charAt(0) >= 'A' && selectedBasetypeId != ID_BASETYPE_HEX){
 			// if alphabet && BASETYPE is not HEX... cancel
 			return;
 		}
 		
 		/* point */
 		if(et.getText().toString().contentEquals(".")){
 			et.setText(et.getText().toString() + ".");
 		}
 		
 		/* general number */
 		if(et.getText().toString().contentEquals("0")){
 			et.setText(str);
		}else if(et.getText().toString().contentEquals("0000")){
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
 		EditText et = getCurrent_Baseinput_EditText();
 		
 		String str = "";
 		if(oprmodeId == ID_OPRMODE_PLUS){
 			str = "+";
 		} else if(oprmodeId == ID_OPRMODE_MINUS){
 			str = "-";
 		} else if(oprmodeId == ID_OPRMODE_MULTI){
 			str = "*";
 		} else if(oprmodeId == ID_OPRMODE_DIVIS){
 			str = "/";
 		}
 		
 		if(et.getText().toString().contentEquals("0")){
 			
 		}else{
 			et.setText(et.getText().toString() + str);
 		}
 		calculate();
 	}
 	
 	/**
 	 * input equall key
 	 */
 	public void inputEquall(){
 		String value = getCurrent_Baseinput_EditText().getText().toString();
 		
 		try{
 			value = calc.calc(value);
 		}catch(NullPointerException e){
 			getCurrent_Baseinput_EditText().setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_error));
 		}catch(NumberFormatException e){
 			getCurrent_Baseinput_EditText().setTextColor(getResources().getColor(R.color.main_editText_baseinput_TextColor_error));
 		};
 		
 		getCurrent_Baseinput_EditText().setText(value);
 		calculate();
 	}
 	
 	/**
 	 * input Backspace key
 	 */
 	public void inputBackspace() {
 		Log.i("binCalc","MainFragment - inputBackspace()...");
 		EditText et = getCurrent_Baseinput_EditText();
 		String value = et.getText().toString();
 		if(!(value.contentEquals("0"))){
 			if(value.length() <= 1){
 				et.setText("0");
 			}else{
 				et.setText(value.substring(0, value.length() - 1));
 			}
 		}
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
 	 * get current base-input Backspace (ImageButton) object
 	 */
 	public ImageButton getCurrent_Baseinput_Backspace_ImageButton(){
 		if(selectedBasetypeId == ID_BASETYPE_BIN){
 			return (ImageButton) v.findViewById(R.id.ImageButton_baseinput_bs_bin);
 		}else if(selectedBasetypeId == ID_BASETYPE_DEC){
 			return (ImageButton) v.findViewById(R.id.ImageButton_baseinput_bs_dec);
 		}else if(selectedBasetypeId == ID_BASETYPE_HEX){
 			return (ImageButton) v.findViewById(R.id.ImageButton_baseinput_bs_hex);
 		}
 		return null;
 	}
 	
 	/**
 	 * Switch enable key-num button by basetypeId
 	 * @param basetypeId	Base-type ID number
 	 */
 	public void setEnableKeyButton(int basetypeId){
 		if(basetypeId == ID_BASETYPE_HEX){
 			/* Alphabet key */
 			v.findViewById(R.id.keyButtonA).setEnabled(true);
 			v.findViewById(R.id.keyButtonB).setEnabled(true);
 			v.findViewById(R.id.keyButtonC).setEnabled(true);
 			v.findViewById(R.id.keyButtonD).setEnabled(true);
 			v.findViewById(R.id.keyButtonE).setEnabled(true);
 			v.findViewById(R.id.keyButtonF).setEnabled(true);
 		}else{
 			/* Alphabet key */
 			v.findViewById(R.id.keyButtonA).setEnabled(false);
 			v.findViewById(R.id.keyButtonB).setEnabled(false);
 			v.findViewById(R.id.keyButtonC).setEnabled(false);
 			v.findViewById(R.id.keyButtonD).setEnabled(false);
 			v.findViewById(R.id.keyButtonE).setEnabled(false);
 			v.findViewById(R.id.keyButtonF).setEnabled(false);
 		}
 		if(basetypeId == ID_BASETYPE_BIN){
 			v.findViewById(R.id.keyButton2).setEnabled(false);
 			v.findViewById(R.id.keyButton3).setEnabled(false);
 			v.findViewById(R.id.keyButton4).setEnabled(false);
 			v.findViewById(R.id.keyButton5).setEnabled(false);
 			v.findViewById(R.id.keyButton6).setEnabled(false);
 			v.findViewById(R.id.keyButton7).setEnabled(false);
 			v.findViewById(R.id.keyButton8).setEnabled(false);
 			v.findViewById(R.id.keyButton9).setEnabled(false);
 		}else{
 			v.findViewById(R.id.keyButton2).setEnabled(true);
 			v.findViewById(R.id.keyButton3).setEnabled(true);
 			v.findViewById(R.id.keyButton4).setEnabled(true);
 			v.findViewById(R.id.keyButton5).setEnabled(true);
 			v.findViewById(R.id.keyButton6).setEnabled(true);
 			v.findViewById(R.id.keyButton7).setEnabled(true);
 			v.findViewById(R.id.keyButton8).setEnabled(true);
 			v.findViewById(R.id.keyButton9).setEnabled(true);
 		}
 	}
 	
 	/**
 	 * Switch base-type
 	 * @param basetypeId	Base-type ID number
 	 */
 	public void switchBasetype(int basetypeId){
 		selectedBasetypeId = basetypeId;
 		
 		ToggleButton tb_type_bin = (ToggleButton) v.findViewById(R.id.toggle_basetype_bin);
 		ImageView bs_bin = (ImageView) v.findViewById(R.id.ImageButton_baseinput_bs_bin);
 		EditText et_input_bin = (EditText) v.findViewById(R.id.editText_baseinput_bin);
 		
 		ToggleButton tb_type_dec = (ToggleButton) v.findViewById(R.id.toggle_basetype_dec);
 		ImageView bs_dec = (ImageView) v.findViewById(R.id.ImageButton_baseinput_bs_dec);
 		EditText et_input_dec = (EditText) v.findViewById(R.id.editText_baseinput_dec);
 		
 		ToggleButton tb_type_hex = (ToggleButton) v.findViewById(R.id.toggle_basetype_hex);
 		ImageView bs_hex = (ImageView) v.findViewById(R.id.ImageButton_baseinput_bs_hex);
 		EditText et_input_hex = (EditText) v.findViewById(R.id.editText_baseinput_hex);
 		
 		/* Set enable/disable Number key buttons */
 		setEnableKeyButton(basetypeId);
 		
 		/* Reset under-line (base-inputs) */
 		if(bs_bin == null) // FIXME! fixed temporally now
 			return;
 		et_input_bin.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.edittext_baseinput_default));
 		et_input_dec.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.edittext_baseinput_default));
 		et_input_hex.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.edittext_baseinput_default));
 		
 		/* Reset toggle-button (base-types) */
 		tb_type_bin.setChecked(false);
 		tb_type_bin.setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_default));
 		tb_type_dec.setChecked(false);
 		tb_type_dec.setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_default));
 		tb_type_hex.setChecked(false);
 		tb_type_hex.setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_default));
 		
 		/* Invisible backspace-button */
 		bs_bin.setImageDrawable(getResources().getDrawable(R.drawable.button_backspace_null));
 		bs_bin.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_backspace_background_null));
 		bs_dec.setImageDrawable(getResources().getDrawable(R.drawable.button_backspace_null));
 		bs_dec.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_backspace_background_null));
 		bs_hex.setImageDrawable(getResources().getDrawable(R.drawable.button_backspace_null));
 		bs_hex.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_backspace_background_null));
 		
 		/* activate Base-types & base-inputs */
 		getCurrent_Basetype_ToggleButton().setChecked(true);
 		getCurrent_Basetype_ToggleButton().setTextColor(getResources().getColor(R.color.main_toggle_basetype_TextColor_active));
 		getCurrent_Baseinput_EditText().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.edittext_baseinput_active));
 		getCurrent_Baseinput_Backspace_ImageButton().setImageDrawable(getResources().getDrawable(R.drawable.button_backspace));
 		getCurrent_Baseinput_Backspace_ImageButton().setBackgroundDrawable(getResources().getDrawable(R.drawable.button_backspace_background));
 	}
 
 	/* Event-handler for buttons */
 	@Override
 	public void onClick(View v) {
 		if(pref_keyVibration){
 			vib.vibrate(DEFAULT_VIBRATION_MSEC);
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
 			case R.id.keyButtonA:
 				inputBasenumber("A");
 				break;
 			case R.id.keyButtonB:
 				inputBasenumber("B");
 				break;
 			case R.id.keyButtonC:
 				inputBasenumber("C");
 				break;
 			case R.id.keyButtonD:
 				inputBasenumber("D");
 				break;
 			case R.id.keyButtonE:
 				inputBasenumber("E");
 				break;
 			case R.id.keyButtonF:
 				inputBasenumber("F");
 				break;
 			case R.id.keyButtonPo:
 				inputBasenumber(".");
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
