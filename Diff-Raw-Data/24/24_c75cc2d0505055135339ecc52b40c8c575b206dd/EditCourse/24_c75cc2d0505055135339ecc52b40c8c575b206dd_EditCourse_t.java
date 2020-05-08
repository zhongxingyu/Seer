 package com.acadplnr;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class EditCourse extends Activity implements OnClickListener {
 	Global1 globe;
 	EditText courseNm, courseLnk, instructname, off_hr_add;
 	Button bLab, bLec, bTut, bdone;
 	String lec_st,lec_et,tut_st,hdata,welink,tut_et,lab_st,lab_et,lec_v,tut_v,lab_v,lec_d,lab_d,tut_d,c_name,w_link,ins_name,off_hradd;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editadd);
 		courseNm = (EditText) findViewById(R.id.coursename);
 		courseLnk = (EditText) findViewById(R.id.courselink);
 		instructname = (EditText) findViewById(R.id.instructorname);
 		off_hr_add = (EditText) findViewById(R.id.officehour_and_address);
 		bLab = (Button) findViewById(R.id.labschedule);
 		bLec = (Button) findViewById(R.id.lectureschedule);
 		bTut = (Button) findViewById(R.id.tutorialschedule);
 		bdone = (Button) findViewById(R.id.bdoneeditadd);
 		bLab.setOnClickListener(this);
 		bLec.setOnClickListener(this);
 		bTut.setOnClickListener(this);
 		bdone.setOnClickListener(this);
 		
 		
 	}
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch(arg0.getId()){
 		case R.id.labschedule:
 			startActivityForResult(new Intent("com.acadplnr.TimeDate"), 1);
 			break;
 		case R.id.lectureschedule:
 			startActivityForResult(new Intent("com.acadplnr.TimeDate"), 2);
 			break;
 		case R.id.tutorialschedule:
 			startActivityForResult(new Intent("com.acadplnr.TimeDate"),3);
 			break;
 		case R.id.bdoneeditadd:
 			c_name= courseNm.getText().toString();
 			w_link=courseLnk.getText().toString();
 			ins_name=instructname.getText().toString();
 			off_hradd=off_hr_add.getText().toString();
 			welink = w_link;
 			if (!welink.startsWith("http://")&&!welink.startsWith("https://")) {
 			    welink = "http://" + welink;
 			}
			GetMethodEx test = new GetMethodEx(welink);
 			try{
 				hdata = test.getInternetData();
 			}catch(Exception e){
 				e.printStackTrace();
 				String error = e.toString();
 				Dialog h = new Dialog(this);
 				h.setTitle(" :(");
 				TextView tv1 = new TextView(this);
 				tv1.setText(error);
 				h.setContentView(tv1);
 				h.show();
 				hdata = "Could not retrieve data from internet";
 			}
 			if(tut_v==null)
 				tut_v ="Tutorial Venue : Not Yet Set";
 			else
 				tut_v="Tutorial Venue : " + tut_v;
 			
 			
 			if(lec_v==null)
 				lec_v ="Lecture Venue : Not Yet Set";
 			else
 				lec_v="Lecture Venue : "+ lec_v;
 			
 
 			if(lab_v==null)
 				lab_v ="Lab Venue : Not Yet Set";
 			else
 				lab_v="Lab Venue : "+ lab_v;
 			
 			Crs_database_help info = new Crs_database_help(this);
 			info.open();
 			try{
 			info.createEntry(c_name, ins_name, w_link, off_hradd, lec_d, lec_st, lec_et, tut_d,
 					tut_st, tut_et, lab_d,
 					lab_st, lab_et, lec_v, lab_v, tut_v,hdata);
 			info.KeyRowIdUpdate();
 			}
 			catch (Exception e)
 			{
 				String error = e.toString();
 				Dialog h = new Dialog(this);
 				h.setTitle(" :(");
 				TextView tv1 = new TextView(this);
 				tv1.setText(error);
 				h.setContentView(tv1);
 				h.show();
 				try {
 					wait(100000);
 				} catch (InterruptedException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 			globe.done = 1;
 			info.close();
 			Intent bablu = new Intent(EditCourse.this,MainActivity.class);
 			//startActivity(bablu);
 			//finish();
 			
 		}
 		
 		
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if(resultCode == RESULT_OK){
 			Bundle  received_basket =data.getExtras();
 			if(requestCode==1)
 			{
 				lab_st= ""+received_basket.getString("start_time");
 				lab_et=""+ received_basket.getString("end_time");
 				lab_v=""+ received_basket.getString("ven");
 				lab_d= ""+received_basket.getString("day");
 				
 			}
 			if(requestCode==2)
 			{
 				lec_st=""+ received_basket.getString("start_time");
 				lec_et=""+ received_basket.getString("end_time");
 				lec_v= ""+received_basket.getString("ven");
 				lec_d=""+ received_basket.getString("day");
 			}
 			if(requestCode==3)
 			{
 				tut_st=""+received_basket.getString("start_time");
 				tut_et=""+ received_basket.getString("end_time");
 				tut_v=""+ received_basket.getString("ven");
 				tut_d=""+ received_basket.getString("day");
 			}
 			
 			
 			
 		}
 		
 	}
 	
 }
 
 
