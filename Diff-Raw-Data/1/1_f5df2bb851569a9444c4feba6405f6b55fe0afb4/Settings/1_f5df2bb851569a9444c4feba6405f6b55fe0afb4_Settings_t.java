 package com.wazzup.ninedrink;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class Settings extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.settings);
 		findView();
 		setListener();
 		mOpenHelper = new NDDBOpenHelper(this);
 		getAll();
 	}
 
 	//ŧi
 	private Button btn_cancel;
 	private Button btn_done;
 	private Button btn_selectall ;
 	private int[] selected_number = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 	private boolean is_selected_all = false;
 	private CheckBox[] cbox_poker = new CheckBox[14] ;
 	private boolean[] poker_list = {false,false,false,false,false,false,false,false,false,false,false,false,false,false};
 	private ProgressBar processBar = null;
 	private int[] cboxId = {
 		R.id.checkBox0,R.id.checkBox1,R.id.checkBox2,
 		R.id.checkBox3,R.id.checkBox4,R.id.checkBox5,
 		R.id.checkBox6,R.id.checkBox7,R.id.checkBox8,
 		R.id.checkBox9,R.id.checkBox10,R.id.checkBox11,
 		R.id.checkBox12,R.id.checkBox13
 	};
 	
 	//]wƮw
 	public NDDBOpenHelper mOpenHelper;
 
 	//]wU
 	private void findView(){
 		for(int i = 0; i < 14; i++){
 			cbox_poker[i] = (CheckBox)findViewById(cboxId[i]);
 		}
 		btn_cancel = (Button)findViewById(R.id.btn_setcancel);
 		btn_done = (Button)findViewById(R.id.btn_setcomplete);
 		btn_selectall = (Button)findViewById(R.id.btn_selectall);
 		processBar=(ProgressBar)findViewById(R.id.processBar);
 	}
 
 	//ť
 	private void setListener(){
 		btn_cancel.setOnClickListener(setcancel);
 		for(int i = 0; i < 14; i++){
 			cbox_poker[i].setOnCheckedChangeListener(selected);
 		}
 		btn_done.setOnClickListener(setdone);
 		btn_selectall.setOnClickListener(selectAll);
 	}
 
 	private Button.OnClickListener setdone = new Button.OnClickListener(){
 		public void onClick(View v){
 			int selectCount = 0;
 			processBar.setVisibility(View.VISIBLE);
 			processBar.incrementProgressBy(70);
 			selectCount = selected_number[0] + selected_number[1] + selected_number[2] + 
 			   selected_number[3] + selected_number[4] + selected_number[5] + 
 			   selected_number[6] + selected_number[7] + selected_number[8] + 
 			   selected_number[9] + selected_number[10] + selected_number[11] + 
 			   selected_number[12] + selected_number[13];
 			
 			if(selectCount < 2){
 				setTitle(R.string.limit_msg);
 				limitDialog();
 			} else {
 				for(int i = 0; i < 14; i++){
 					mOpenHelper.update(i, poker_list[i]);
 					processBar.incrementProgressBy(5);
 				}
 				finish();
 			}
 			
 		}
 	};
 
 	private Button.OnClickListener setcancel = new Button.OnClickListener(){
 		public void onClick(View v){
 			finish();
 		}
 	};
 
 	private Button.OnClickListener selectAll = new Button.OnClickListener(){
 		public void onClick(View v){
 			selectAllEvent(is_selected_all);
 		}
 	};
 	//c selectAll ƥ
 	public void selectAllEvent(boolean checked){
 		for(int i = 0; i < 14; i++){
 			if(checked) selected_number[i] = 1;
 			else selected_number[i] = 0;
 			cbox_poker[i].setChecked(checked);
 			poker_list[i] = checked;
 		}
 	}
 
 	private CheckBox.OnCheckedChangeListener selected= new CheckBox.OnCheckedChangeListener()
 	{
 		@Override
 		public void onCheckedChanged(CompoundButton btnView, boolean isChecked){
 			// TODO Auto-generated method stub
 			for(int i = 0; i < 14; i++){
 				if(cboxId[i] == btnView.getId()){
 					poker_list[i] = isChecked;
 					if(isChecked) selected_number[i] = 1;
 					else selected_number[i] = 0;
 					break;
 				}
 			}
 			checkSelected();
 		}
 	};
 
 	// oҦO
 	public void getAll(){
 		int i = 0;
 		Cursor result = mOpenHelper.getAll();
 
 		result.moveToFirst();
 		while (!result.isAfterLast()){
 			i = result.getInt(0);
 			poker_list[i] = Boolean.valueOf(result.getString(1).equals("0") ? "false" : "true");
 			selected_number[i] = result.getInt(1);
 			cbox_poker[i].setChecked(poker_list[i]);
 			result.moveToNext();
 		}
 		checkSelected();
 	}
 
 	//c` CheckBoxO_Ŀ ]wƥ
 	public void checkSelected(){
 		int chkSelect;
 		chkSelect = selected_number[0] & selected_number[1] & selected_number[2] & 
 					selected_number[3] & selected_number[4] & selected_number[5] & 
 					selected_number[6] & selected_number[7] & selected_number[8] & 
 					selected_number[9] & selected_number[10] & selected_number[11] & 
 					selected_number[12] & selected_number[13];
 		if(chkSelect == 0){
 			btn_selectall.setText(R.string.select_all);
 			is_selected_all = true;
 		}else{
 			btn_selectall.setText(R.string.select_cancelall);
 			is_selected_all = false;
 		}
 	}
 
 	private void limitDialog(){
 		Toast popup =  Toast.makeText(Settings.this, R.string.limit_msg, Toast.LENGTH_SHORT);
 		popup.show();
 	}
 }
