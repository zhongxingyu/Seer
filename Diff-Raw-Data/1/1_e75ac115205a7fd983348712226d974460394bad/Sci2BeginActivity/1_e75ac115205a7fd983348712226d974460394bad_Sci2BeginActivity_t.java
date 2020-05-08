 package com.backtoschool.testself;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.RadioButton;
 import android.widget.Toast;
 
 /**
  * Created by MiracleLife on 9/28/13.
  */
 public class Sci2BeginActivity extends Activity {
     private RadioButton radChoice1, radChoice2, radChoice3;
     private String strAns = "", strMenberID;
     private myDBClass objMyDBClass;
     private Intent objIntent;
 
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sci2testpre_layout);
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             strMenberID = extras.getString("MemberID");
         }
         initialWidget();
     }
 
     private void initialWidget() {
         radChoice1 = (RadioButton) findViewById(R.id.radioButton);
         radChoice2 = (RadioButton) findViewById(R.id.radioButton2);
         radChoice3 = (RadioButton) findViewById(R.id.radioButton3);
     }
     public void onClickNext(View view){
 
 
         try{
 
             if(radChoice1.isChecked()){
 
                 strAns = "Sci1-1";
 
             }else if(radChoice2.isChecked()){
 
                 strAns = "Sci1-2";
 
             }else if(radChoice3.isChecked()){
 
                 strAns = "Sci1-3";
 
             }
 
 
             if(strAns.equals("")){
 
                 Log.d("Database", "Have Check Button !!!!!");
 
                 Toast.makeText(Sci2BeginActivity.this, "กรุณาเลือกคำตอบด้วยค่ะ",
                         Toast.LENGTH_SHORT).show();
 
 
             }else{
 
                 UpdateDataSQLite();
 
                 objIntent = new Intent(Sci2BeginActivity.this, Sci2Test1Activity.class);
                 objIntent.putExtra("MemberID",strMenberID);
                 startActivity(objIntent);
 
             }
 
 
         }catch (Exception e){
 
             Log.d("Database", "Sci2TestPreActivity Error " + e.toString());
 
         }
 
     }
 
     public void UpdateDataSQLite(){
 
         objMyDBClass = new myDBClass(this);
         long updateData = objMyDBClass.Insert_Data_sciencescore_t2(strMenberID, strAns);
 
         Log.d("Database", "Sci2TestPreActivity insert DB Success !!!!!" + strAns);
 
     }
 }
