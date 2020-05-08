 package com.amca.android.replace;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.text.InputType;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioGroup;
 
 public class UserFormDetail extends Activity implements OnClickListener{
 
 	private static final int MY_BUTTON = 9000;
 	private String mode = null, attribute = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_user_form_detail);
 		
 		String[] dataSplit = null;
 		int n = 1;
 		
 		Intent intent = getIntent();
 		mode = intent.getStringExtra("mode");
 		attribute = intent.getStringExtra("attribute");
 		setTitle(attribute);
 		
		String data = intent.getStringExtra("data");
		if(!data.equals("null")){
 			dataSplit = data.split(",");
 			n = dataSplit.length + 1;
 		}
 		
 		LinearLayout ll = (LinearLayout)findViewById(R.id.linearLayout);
 		for(int i=0; i<n; i++){
 			EditText ed = new EditText(this);
 			ed.setInputType(InputType.TYPE_CLASS_TEXT);
 			
			if(i < n-1){
 				ed.setText(dataSplit[i]);
 			}else{
 				ed.setHint("Write your " + attribute + " here");
 			}
 			ll.addView(ed);
 		}
 		
 		Button b = new Button(this);
         b.setText("Save");
         b.setId(MY_BUTTON);
         b.setOnClickListener(this);
         ll.addView(b);
 	}
 
 	public void onClick(View v) {
         switch (v.getId()) {
         case MY_BUTTON:
         	saveForm();
             break;
         }
 	}
 	
     public void saveForm() {
         LinearLayout root = (LinearLayout) findViewById(R.id.linearLayout);
         loopQuestions(root);
     }
     
     private void loopQuestions(ViewGroup parent) {
     	ArrayList<String> dataList = new ArrayList<String>();
         for(int i = 0; i < parent.getChildCount(); i++) {
             View child = parent.getChildAt(i);
             if(child instanceof RadioGroup ) {
                 RadioGroup radio = (RadioGroup)child;
                 storeAnswer(radio.getId(), radio.getCheckedRadioButtonId());
             } else if(child instanceof EditText) {
                 EditText et = (EditText) child;
                 dataList.add(et.getText().toString());
             }else {
             }
         }
         
         dataList.removeAll(Arrays.asList("", null));
         StringBuilder builder = new StringBuilder();
         builder.append( dataList.remove(0));
         
         for(String s : dataList) {
         	builder.append(",");
         	builder.append(s);
         }
         
         Intent returnIntent = new Intent();
         returnIntent.putExtra("attribute", attribute);
         returnIntent.putExtra("data", builder.toString());
         setResult(RESULT_OK, returnIntent);
         finish();
     }
  
     private void storeAnswer(int question, int answer) {
 
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.user_form_detail, menu);
 		return true;
 	}
 
 }
