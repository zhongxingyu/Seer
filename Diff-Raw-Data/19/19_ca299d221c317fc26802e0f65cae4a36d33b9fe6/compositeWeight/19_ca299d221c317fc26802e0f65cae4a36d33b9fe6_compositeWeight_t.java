 package com.menst_verstka.composite;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import com.menst_verstka.R;
 import com.menst_verstka.utils.jsonCompositeElement;
 import com.menst_verstka.utils.jsonframeActivity;
 
 import java.util.Calendar;
 
 /**
  * Created by Alexander on 02.09.13.
  */
 public class compositeWeight extends jsonCompositeElement implements RadioGroup.OnCheckedChangeListener,View.OnClickListener {
     protected RelativeLayout rl;
     protected Number_picker number_picker;
     protected double min = 0,max = 200;
     private RadioGroup radioGroup;
    // private RadioButton radioButton1,radioButton2;
     private Button delete,back;
     protected TextView left1,left2,right1,right2;
 
     public compositeWeight(Context context) {
         super(context);
     }
     @Override
     protected void SetEventListeners() {
         radioGroup.setOnCheckedChangeListener(this);
         back.setOnClickListener(this);
         left1.setOnClickListener(this);
         right1.setOnClickListener(this);
     }
     @Override
     protected void SetCompositeElements() {
         number_picker.setValue(30);
         rl.addView(number_picker);
     }
 
     @Override
     protected void InitializeComponent(Context context) {
         super.InitializeComponent(context);
         LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.composite_temperature, this);
         JSON_TAG = "weight";
         number_picker = new Number_picker(pActivity,1.0,min,max,"%.0f");
         rl = (RelativeLayout) findViewById(R.id.temp_number_picker_rl);
         radioGroup = (RadioGroup) findViewById(R.id.composite_temperature_radiogroup);
         delete = (Button) findViewById(R.id.composite_temperature_delete);
         back = (Button) findViewById(R.id.composite_temperature_back);
         left1 = (TextView) findViewById(R.id.composite_temperature_left1);
         left2 = (TextView) findViewById(R.id.composite_temperature_left2);
         right1 = (TextView) findViewById(R.id.composite_temperature_right1);
         right2 = (TextView) findViewById(R.id.composite_temperature_right2);
        // radioButton1 = (RadioButton) findViewById(R.id.composite_temperature_radio1);
        // radioButton2 = (RadioButton) findViewById(R.id.composite_temperature_radio2);
     }
 
 
     @Override
     public void onCheckedChanged(RadioGroup radioGroup, int i) {
         if(i == R.id.composite_temperature_radio2) {
             number_picker.setValue(number_picker.getValue()*2.205);
         } else {
             number_picker.setValue(number_picker.getValue()*0.454);
         }
     }
 
     @Override
     public void Set(Calendar calendar,JsonObject jo) {
         super.Set(calendar,jo);
        if(jo == null || !jo.has(JSON_TAG)) {
             return;
         }
         try {
             JsonArray array =  jo.getAsJsonArray(JSON_TAG);
             radioGroup.check((array.get(0).getAsInt() == 0)?R.id.composite_temperature_radio1:R.id.composite_temperature_radio2);
             number_picker.SetFormat(number_picker.DetermineFormat(array.get(1).getAsDouble()));
             number_picker.setValue(array.get(1).getAsDouble());
         } catch (Exception ex) {
             ex.printStackTrace();
             Log.d("Set_composite_weight_exception",ex.toString());
         }
     }
 
     private void ModifyJson() {
         JsonArray array = new JsonArray();
         array.add(new JsonPrimitive(radioGroup.getCheckedRadioButtonId() == R.id.composite_temperature_radio1?0:1));
         array.add(new JsonPrimitive(number_picker.getValue()));
         jo.add("weight",array);
     }
 
     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.composite_temperature_back:
                 ModifyJson();
                 Intent i = new Intent();
                 i.putExtras(pActivity.GenerateExtras(calendar,jo));
                 pActivity.setResult(Activity.RESULT_OK,i);
                 pActivity.finish();
                 break;
             case R.id.composite_temperature_delete:
 
                 break;
             case R.id.composite_temperature_left1:
             case R.id.composite_temperature_left2:
             case R.id.composite_temperature_right1:
             case R.id.composite_temperature_right2:
                 TextView txt = (TextView) view;
                 double d = Double.parseDouble(txt.getText().toString());
                 if((int)d != d && number_picker.format != "%.1f") {
                     number_picker.format = "%.1f"; }
                 number_picker.addValue(d);
                 break;
         }
     }
 }
