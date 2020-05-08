 package com.example.profile;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Set;
 
 import com.example.agenda.R;
 import com.example.agendaMain.AgendMainActivity;
 import com.example.courses.CoursesDataBaseHelper;
 import com.example.news.ITCutiesReaderAppActivity;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.text.Html;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 // Class responsibly for calculate and display profile infos
 public class Profile extends Activity {
 	CoursesDataBaseHelper db_courseHelper;
 	SQLiteDatabase dbC;
 	Set<String> set;
 	double average = 0.0;
 	int remain_courses = 48;
 	int semester_courses = 0;
 	String direction;
 	int direC = 0;
 	Context ctx = this;
 	
 	//kor=remain courses kormou -- bas=remain basic courses -- epil=remain choice courses -- remain general courses
 	int kor = 27, bas = 5, epil = 10, gen = 6;
 	
 	//th,ys,ep true if user has pass one basic course from each direction
 	boolean th = false, ys = false, ep = false;
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//set back button
         ActionBar bar = getActionBar();
         bar.setDisplayHomeAsUpEnabled(true);
         TextView bv = new TextView(Profile.this);
         bv.setTextSize(18);
         bar.setCustomView(bv);
         bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bd1));
 
 		// Read infos from courses in database
 		db_courseHelper = new CoursesDataBaseHelper(this);
         try { 
         	db_courseHelper.createDataBase();
         } catch (IOException ioe) {
         	throw new Error("Unable to create database");
         }
 		
         //Create dynamically the xml view
         ScrollView scrollv = new ScrollView(this);
 		RelativeLayout rl = new RelativeLayout(this);
         
         //Calculate AVERAGE and REMAIN COURSES
 		calculateAverage_RemainCourses();
 
 	// DISPLAY AVERAGE:
 		RelativeLayout average_rl = new RelativeLayout(this);
 		average_rl.setId(View.generateViewId());
 		RelativeLayout.LayoutParams avg_rl_par = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		avg_rl_par.addRule(RelativeLayout.ALIGN_PARENT_START);
 		average_rl.setLayoutParams(avg_rl_par);
 		
 		TextView avg_text = new TextView(this);
 		avg_text.setId(TextView.generateViewId());
 		avg_text.setText(Html.fromHtml("<big><b>Average</b></big>"));
 		RelativeLayout.LayoutParams avg_text_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		avg_text_rl_par.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		avg_text.setLayoutParams(avg_text_rl_par);
 		average_rl.addView(avg_text);
 
 		String average_string = Double.toString(average);
 		String[] numbers = average_string.split("\\.");
 
 		ImageView first_average_digit = new ImageView(this);
 		ImageView second_average_digit = new ImageView(this);
 		ImageView period = new ImageView(this);
 
 		Drawable first_average_digit_draw = find_png_files(numbers[0]);
 		Drawable second_average_digit_draw = find_png_files(numbers[1]);
 		Drawable period_draw = find_png_files("11");
 		
 		first_average_digit.setImageDrawable(first_average_digit_draw);
 		first_average_digit.setId(ImageView.generateViewId());
 		
 		second_average_digit.setImageDrawable(second_average_digit_draw);
 		second_average_digit.setId(ImageView.generateViewId());
 		
 		period.setImageDrawable(period_draw);
 		period.setId(ImageView.generateViewId());
 		
 		RelativeLayout.LayoutParams period_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		period_rl_par.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		period_rl_par.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		period.setLayoutParams(period_rl_par);
 		average_rl.addView(period);
 
 		RelativeLayout.LayoutParams avg_digit1_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		avg_digit1_rl_par.addRule(RelativeLayout.BELOW,avg_text.getId());
 		avg_digit1_rl_par.addRule(RelativeLayout.LEFT_OF,period.getId());
 		first_average_digit.setLayoutParams(avg_digit1_rl_par);
 		average_rl.addView(first_average_digit);
 
 		RelativeLayout.LayoutParams avg_digit2_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		avg_digit2_rl_par.addRule(RelativeLayout.RIGHT_OF,period.getId());
 		avg_digit2_rl_par.addRule(RelativeLayout.BELOW,avg_text.getId());
 		second_average_digit.setLayoutParams(avg_digit2_rl_par);
 		average_rl.addView(second_average_digit);
 
 
 	//DISPLAY REMAIN COURSES
 		RelativeLayout rmcourses_rl = new RelativeLayout(this);
 		rmcourses_rl.setId(View.generateViewId());
 		RelativeLayout.LayoutParams rmCourses_rl_par = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		rmCourses_rl_par.addRule(RelativeLayout.BELOW, average_rl.getId());
 		rmCourses_rl_par.setMargins(0, 25, 0, 0);
 		rmcourses_rl.setLayoutParams(rmCourses_rl_par);
 		
 		TextView rmCourses_text = new TextView(this);
 		rmCourses_text.setId(TextView.generateViewId());
 		rmCourses_text.setText(Html.fromHtml("<big><b>Remain Courses</b></big>"));
 		RelativeLayout.LayoutParams rmCourses_text_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		rmCourses_text_rl_par.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		rmCourses_text.setLayoutParams(rmCourses_text_rl_par);
 		rmcourses_rl.addView(rmCourses_text);
 
 		Drawable rmCourses_digit1_draw = null;
 		Drawable rmCourses_digit2_draw = null;
 
 		String remain_string = Integer.toString(remain_courses);
 		if(remain_courses>=10){
 			String[] rnumbers = remain_string.split("(?<=.)");
 			
 			rmCourses_digit1_draw = find_png_files(rnumbers[0]);
 			rmCourses_digit2_draw = find_png_files(rnumbers[1]);
 		}else{
 			rmCourses_digit1_draw = find_png_files("0");
 			rmCourses_digit2_draw = find_png_files(remain_string);
 		}
 
 		ImageView first_rmCourses_digit = new ImageView(this);
 		first_rmCourses_digit.setImageDrawable(rmCourses_digit1_draw);
 		first_rmCourses_digit.setId(ImageView.generateViewId());
 		
 		ImageView second_rmCourses_digit = new ImageView(this);
 		second_rmCourses_digit.setImageDrawable(rmCourses_digit2_draw);
 		second_rmCourses_digit.setId(ImageView.generateViewId());
 		
 		RelativeLayout.LayoutParams first_rmCourses_digit_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		first_rmCourses_digit_rl_par.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		first_rmCourses_digit_rl_par.addRule(RelativeLayout.BELOW,rmCourses_text.getId());
 		first_rmCourses_digit.setLayoutParams(first_rmCourses_digit_rl_par);
 		
 		RelativeLayout.LayoutParams second_rmCourses_digit_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		second_rmCourses_digit_rl_par.addRule(RelativeLayout.BELOW,rmCourses_text.getId());
 		second_rmCourses_digit_rl_par.addRule(RelativeLayout.RIGHT_OF,first_rmCourses_digit.getId());
 		second_rmCourses_digit.setLayoutParams(second_rmCourses_digit_rl_par);
 		
 		Button detail_button = new Button(this);
 		detail_button.setText("Details");
		detail_button.setBackgroundColor(Color.rgb(56,176,222));
 		RelativeLayout.LayoutParams button_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		button_rl_par.addRule(RelativeLayout.ALIGN_BOTTOM, second_rmCourses_digit.getId());
 		button_rl_par.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		detail_button.setLayoutParams(button_rl_par);
 		detail_button.setOnClickListener(new View.OnClickListener() {
 		    public void onClick(View v) {
 	    	TextView ms = new TextView(ctx);
 	    	String basika="";
 	    	int bas2 = bas;
 	    	
 	    	if(bas != 0){
 	    		if(!th || !ys || !ep){
 	    			basika = "( ";
 	    			if(!th){
 	    				basika += "1   ";
 	    				bas2--;
 	    			}
 	    			if(!ys){
 	    				if(!th)
 	    					basika += " - ";
 	    				basika += "1     ";
 	    				bas2--;
 	    			}
 	    			if(!ep){
 	    				if(!th || !ys)
 	    					basika += " - ";
 	    				basika += "1     ";
 	    				bas2--;
 	    			}
 	    			if(bas2 != 0){
 	    				if(!th || !ys || !ep)
 	    					basika += " - ";
 	    				basika += String.valueOf(bas2) + "   ";
 	    			}
 	    			basika += (")");	
 	    		}
 	    		else{
 	    			basika += ("(  )");
 	    		}
 	    	}
 	    	ms.setText(": " + kor + '\n'+ '\n' + " : " + bas + " " + basika + '\n' + '\n' + ": " + epil + '\n' + '\n' + " : " + gen );
 	    	ms.setTextSize(16);
 	    	AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 	    	builder
 	    	.setTitle("Remain Courses")
 	    	.setView(ms)
 	    	.setNeutralButton("OK", null)
 	    	.show();
 	    }
 	    });
 		
 		rmcourses_rl.addView(first_rmCourses_digit);
 		rmcourses_rl.addView(second_rmCourses_digit);
 		rmcourses_rl.addView(detail_button);
 
 
 		//Calculate CURRENT SEMESTER COURSES
 		semesterCourses();
 		
 	//DISPLAY SEMESTER COURSES
 		RelativeLayout semester_courses_rl = new RelativeLayout(this);
 		semester_courses_rl.setId(View.generateViewId());
 		RelativeLayout.LayoutParams smCourses_rl_par = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		smCourses_rl_par.addRule(RelativeLayout.BELOW, rmcourses_rl.getId());
 		smCourses_rl_par.setMargins(0, 25, 0, 0);
 		semester_courses_rl.setLayoutParams(smCourses_rl_par);
 				
 		TextView smCourses_text = new TextView(this);
 		smCourses_text.setId(TextView.generateViewId());
 		smCourses_text.setText(Html.fromHtml("<big><b>Current Semester Courses</b></big>"));
 		RelativeLayout.LayoutParams smCourses_text_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		smCourses_text_rl_par.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		smCourses_text.setLayoutParams(smCourses_text_rl_par);
 		semester_courses_rl.addView(smCourses_text);
 
 		Drawable smCourses_digit1_draw = null;
 		Drawable smCourses_digit2_draw = null;
 
 		String semester_string = Integer.toString(semester_courses);
 		if(semester_courses>=10){
 			String[] rnumbers = semester_string.split("(?<=.)");
 
 			smCourses_digit1_draw = find_png_files(rnumbers[0]);
 			smCourses_digit2_draw = find_png_files(rnumbers[1]);
 		}else{
 			smCourses_digit1_draw = find_png_files("0");
 			smCourses_digit2_draw = find_png_files(semester_string);
 		}
 
 		ImageView first_smCourses_digit = new ImageView(this);
 		first_smCourses_digit.setImageDrawable(smCourses_digit1_draw);
 		first_smCourses_digit.setId(ImageView.generateViewId());
 
 		ImageView second_smCourses_digit = new ImageView(this);
 		second_smCourses_digit.setImageDrawable(smCourses_digit2_draw);
 		second_smCourses_digit.setId(ImageView.generateViewId());
 
 		RelativeLayout.LayoutParams first_smCourses_digit_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		first_smCourses_digit_rl_par.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		first_smCourses_digit_rl_par.addRule(RelativeLayout.BELOW,smCourses_text.getId());
 		first_smCourses_digit.setLayoutParams(first_smCourses_digit_rl_par);
 
 		RelativeLayout.LayoutParams second_smCourses_digit_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		second_smCourses_digit_rl_par.addRule(RelativeLayout.BELOW,smCourses_text.getId());
 		second_smCourses_digit_rl_par.addRule(RelativeLayout.RIGHT_OF,first_smCourses_digit.getId());
 		second_smCourses_digit.setLayoutParams(second_smCourses_digit_rl_par);
 
 		Button detail_button2 = new Button(this);
 		detail_button2.setText("Details");
		detail_button2.setBackgroundColor(Color.rgb(56,176,222));
 		RelativeLayout.LayoutParams button2_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		button2_rl_par.addRule(RelativeLayout.ALIGN_BOTTOM, second_smCourses_digit.getId());
 		button2_rl_par.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		detail_button2.setLayoutParams(button2_rl_par);
 		detail_button2.setOnClickListener(new View.OnClickListener() {
 		    public void onClick(View v) {
 	    	String text = "";
 	    	TextView ms = new TextView(ctx);
 	    	ScrollView scroll = new ScrollView(ctx);
 	    	
 	    	if(set != null){
 	    		if(!set.isEmpty()){
 	    			Iterator<String> setIter = set.iterator();
 	    			for(int i=0; i<set.size();i++){
 	    				text += "--" + setIter.next() + '\n' + '\n';
 	    			}
 	    		}
 	    		else
 	    			text += "No course has been selected for this semester";
 	    	}
 	    	else
 	    		text += "No course has been selected for this semester";
 	    	
 	    	ms.setText(text);
 	    	scroll.addView(ms);
 	    	AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 	    	builder
 	    	.setTitle("Current Semester Courses")
 	    	.setView(scroll)
 	    	.setNeutralButton("OK", null)
 	    	.show();
 		    }
 		});
 		
 		semester_courses_rl.addView(first_smCourses_digit);
 		semester_courses_rl.addView(second_smCourses_digit);
 		semester_courses_rl.addView(detail_button2);
 		
 
 		//Calculate and display PROPOSED DIRECTION
 		calculateDirection();
 		
 	//DISPLAY PROPOSED DIRECTION
 		String p="";
 		if(!direction.equals(" - ")){
 			if(direC<=5)
 				p = "<font color=\"red\">( )</font>";
 			else if(direC<9)
 				p = "<font color=\"blue\">( )</font>";
 			else
 				p = "<font color=\"green\">( )</font>";
 		}
 		RelativeLayout proposed_dir_rl = new RelativeLayout(this);
 		RelativeLayout.LayoutParams proposed_dir_rl_par = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		proposed_dir_rl_par.addRule(RelativeLayout.BELOW, semester_courses_rl.getId());
 		proposed_dir_rl_par.setMargins(0, 25, 0, 0);
 		proposed_dir_rl.setLayoutParams(proposed_dir_rl_par);
 		
 		TextView pr_text = new TextView(this);
 		pr_text.setId(TextView.generateViewId());
 		pr_text.setText(Html.fromHtml("<big><b>Proposed direction</b></big>"));
 		RelativeLayout.LayoutParams pr_text_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		pr_text_rl_par.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		pr_text.setLayoutParams(pr_text_rl_par);
 		proposed_dir_rl.addView(pr_text);
 
 		TextView proposed_dir_text = new TextView(this);
 		proposed_dir_text.setText(Html.fromHtml("<big>"+direction+p+"</big>"));
 		RelativeLayout.LayoutParams proposed_dir_text_rl_par = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		proposed_dir_text_rl_par.addRule(RelativeLayout.BELOW,pr_text.getId());
 		proposed_dir_text_rl_par.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		proposed_dir_text.setLayoutParams(proposed_dir_text_rl_par);
 		proposed_dir_rl.addView(proposed_dir_text);
 
 		
 		// Add all the views
 		rl.addView(average_rl);
 		rl.addView(rmcourses_rl);
 		rl.addView(semester_courses_rl);
 		rl.addView(proposed_dir_rl);
 		scrollv.addView(rl);
 		this.setContentView(scrollv);	
 	}
 	
 	private Drawable find_png_files(String number){
 		int num= Integer.parseInt(number);
 		Drawable myIcon = null;
 
 		switch(num){
 		case 0:
			myIcon = getResources().getDrawable( R.drawable.one);
 			break;
 		case 1:
 			myIcon = getResources().getDrawable( R.drawable.one);
 			break;
 		case 2:
 			myIcon = getResources().getDrawable( R.drawable.two);
 			break;
 		case 3:
 			myIcon = getResources().getDrawable( R.drawable.three);
 			break;
 		case 4:
 			myIcon = getResources().getDrawable( R.drawable.four);
 			break;
 		case 5:
 			myIcon = getResources().getDrawable( R.drawable.five);
 			break;
 		case 6:
 			myIcon = getResources().getDrawable( R.drawable.six);
 			break;
 		case 7:
 			myIcon = getResources().getDrawable( R.drawable.seven);
 			break;
 		case 8:
 			myIcon = getResources().getDrawable( R.drawable.eight);
 			break;
 		case 9:
 			myIcon = getResources().getDrawable( R.drawable.nine);
 			break;
 		case 10:
 			myIcon = getResources().getDrawable( R.drawable.ten);
 			break;
 		case 11:
 			myIcon = getResources().getDrawable( R.drawable.period);
 			break;
 		}
 		
 		return myIcon;
 	}
 	
 	
 	private void calculateAverage_RemainCourses(){
 		ArrayList<Double> gradesK = new ArrayList<Double>();
 		ArrayList<Double> gradesTh = new ArrayList<Double>();
 		ArrayList<Double> gradesYs = new ArrayList<Double>();
 		ArrayList<Double> gradesEp = new ArrayList<Double>();
 		ArrayList<Double> gradesRest = new ArrayList<Double>();
 		ArrayList<Double> igrades = new ArrayList<Double>();
 		double k20=0.0,k23=0.0;
 		
 
 		dbC = db_courseHelper.getReadableDatabase();
 		String select = "SELECT Grade, Code FROM Subjects WHERE (Code LIKE '__' OR Code LIKE '__' OR Code LIKE '__')  AND Grade!=-1";
 		String select2 = "SELECT Grade, ThP, YS, EP FROM Subjects WHERE Code NOT LIKE '%' AND Code NOT LIKE '____' AND Code NOT LIKE '%' AND Grade!=-1";
 		String select3 = "SELECT COUNT(*) FROM Subjects WHERE Code LIKE '%' AND Grade!=-1";
 		
 		Cursor cursor = dbC.rawQuery(select, null);
 		if(cursor.moveToFirst()){
 			do{
 				String code = cursor.getString(1);
 				Double grade = cursor.getDouble(0);
 				
 				if(code.equals("26") || code.equals("27") || code.equals("28"))
 					igrades.add(grade);
 				else if(code.equals("20") || code.equals("20")){
 					if(k20 == 0.0)
 						k20 = grade;
 					else{
 						if(grade > k20){
 							if(code.equals("20"))
 								gradesEp.add(k20);
 							else
 								gradesTh.add(k20);
 							k20 = grade;
 						}
 						else{
 							if(code.equals("20"))
 								gradesEp.add(grade);
 							else
 								gradesTh.add(grade);
 						}
 							
 					}					
 				}
 				else if(code.equals("23") || code.equals("23")){
 					if(cursor.getDouble(0)>k23)
 						k23 = cursor.getDouble(0);
 				}
 				else
 					gradesK.add(cursor.getDouble(0));
 			}while(cursor.moveToNext());
 		}
 		cursor.close();
 		
 		cursor = dbC.rawQuery(select2, null);
 		if(cursor.moveToFirst()){
 			do{
 				Double grade = cursor.getDouble(0);
 				
 				if(cursor.getDouble(1) == 1){
 					gradesTh.add(grade);
 					th = true;
 				}
 				else if(cursor.getDouble(2) == 1){
 					gradesYs.add(grade);
 					ys = true;
 				}
 				else if(cursor.getDouble(3) ==1){
 					gradesEp.add(grade);
 					ep = true;
 				}
 				else
 					gradesRest.add(grade);
 			}while(cursor.moveToNext());
 		}
 		cursor.close();
 		
 		cursor = dbC.rawQuery(select3, null);
 		cursor.moveToFirst();
 		gen -= cursor.getInt(0);
 		remain_courses -= cursor.getInt(0);
 		cursor.close();
 		
 		dbC.close();
 		db_courseHelper.close();
 		Collections.sort(gradesTh);
 		Collections.sort(gradesYs);
 		Collections.sort(gradesEp);
 		Collections.sort(gradesRest);
 		Collections.sort(igrades);
 		Collections.reverse(gradesTh);
 		Collections.reverse(gradesYs);
 		Collections.reverse(gradesEp);
 		Collections.reverse(gradesRest);
 		Collections.reverse(igrades);
 		
 		double weight_grade = 0.0, syntelestes = 0.0;
 		
 		//We need at least one basic course from each direction (3 basic courses)
 		if(!gradesTh.isEmpty()){
 			//Log.i("PROFILE TH","Theoritiki "+String.valueOf(gradesTh.get(0)));
 			weight_grade += 2*gradesTh.get(0);
 			gradesTh.remove(0);
 			syntelestes += 2;
 			bas --;
 			remain_courses --;
 		}
 		if(!gradesYs.isEmpty()){
 			//Log.i("PROFILE YS","Ypologistika "+String.valueOf(gradesYs.get(0)));
 			weight_grade += 2*gradesYs.get(0);
 			gradesYs.remove(0);
 			syntelestes += 2;
 			bas --;
 			remain_courses --;
 		}
 		if(!gradesEp.isEmpty()){
 			//Log.i("PROFILE EP", "Epeksergasia "+String.valueOf(gradesEp.get(0)));
 			weight_grade += 2*gradesEp.get(0);
 			gradesEp.remove(0);
 			syntelestes += 2;
 			bas --;
 			remain_courses --;
 		}
 		
 		//And at least 2 basic course from whichever direction
 		gradesTh.addAll(gradesYs);
 		gradesTh.addAll(gradesEp);
 		Collections.sort(gradesTh);
 		Collections.reverse(gradesTh);
 		
 		for(int i=0; i<2; i++){
 			if(!gradesTh.isEmpty()){
 				weight_grade += 2*gradesTh.get(0);
 				syntelestes += 2;
 				gradesTh.remove(0);
 				bas --;
 				remain_courses --;
 			}
 			else
 				break;
 		}
 		
 		//We need 10 courses basic, epiloghs or eleuthera
 		gradesRest.addAll(gradesTh);
 		Collections.sort(gradesRest);
 		Collections.reverse(gradesRest);
 		
 		for(int i=0; i<10; i++){
 			if(!gradesRest.isEmpty()){
 				weight_grade += 1.5*gradesRest.get(0);
 				syntelestes += 1.5;
 				gradesRest.remove(0);
 				epil --;
 				remain_courses --;
 			}
 			else
 				break;
 		}
 		
 		//Ptyxiakh 1,2 h Praktiki
 		for(int i=0; i<2; i++){
 			if(!igrades.isEmpty()){
 				//Log.i("PROFILE PTYX",String.valueOf(igrades.get(0)));
 				weight_grade += 3.0*igrades.get(0);
 				syntelestes += 3.0;
 				igrades.remove(0);
 				kor --;
 				remain_courses --;
 			}
 			else
 				break;
 		}
 		
 		//Finally 25 courses kormou
 		for(int i=0; i<gradesK.size();i++){
 			//Log.i("PROFILE K value",String.valueOf(gradesK.get(i)));
 			weight_grade += 2.0*gradesK.get(i);
 			syntelestes += 2.0;
 			kor --;
 			remain_courses --;
 		}
 		if(k20!=0.0){
 			weight_grade += 2.0*k20;
 			syntelestes += 2.0;
 			kor --;
 			remain_courses --;
 		}
 		if(k23!=0.0){
 			weight_grade += 2.0*k23;
 			syntelestes += 2.0;
 			kor --;
 			remain_courses --;
 		}
 		//Log.i("PROFILE WEIGHT GRADE+SYNTEL",String.valueOf(weight_grade)+"  "+String.valueOf(syntelestes));
 		if(weight_grade!=0)
 			average = (double)weight_grade/(double)syntelestes;
 		DecimalFormat df = new DecimalFormat("#.#");
 		String average2 = df.format(average);	
 		average = Double.valueOf(average2);
 	}
 	
 	private void semesterCourses(){
 		SharedPreferences sharedPref;
 		String preFile = ITCutiesReaderAppActivity.PREFS_NAME;
 		
 		//Retrieve shared preferences
         sharedPref = getSharedPreferences(preFile, 0);
         
 		//Retrieve semester courses
 		set = sharedPref.getStringSet("CoursesChecked", null);
 		
 		if(set == null)
 			semester_courses = 0;
 		else
 			semester_courses = set.size();
 	}
 
 	private void calculateDirection(){
 		double gradeTh = 0.0, gradeYs = 0.0, gradeEp = 0.0;
 		
 		//Calculate a weighted average for each direction (weight for basic=3, for epiloghs kateythynshs=2 and for kormou=1)
 		double wTh = 0.0, wYs = 0.0, wEp = 0.0;
 		double synTh = 0.0, synYs = 0.0, synEp = 0.0;
 		
 		int ThC = 0, YsC = 0, EpC = 0;
 		
 		dbC = db_courseHelper.getReadableDatabase();
 		String select = "SELECT Grade, ThP, Code FROM Subjects WHERE Code NOT LIKE '___' AND Code NOT LIKE '____' AND (ThP=1 OR ThP=2) AND Grade!=-1";
 		String select2 = "SELECT Grade, YS, Code FROM Subjects WHERE Code NOT LIKE '___' AND Code NOT LIKE '____' AND (YS=1 OR YS=2) AND Grade!=-1";
 		String select3 = "SELECT Grade, EP, Code FROM Subjects WHERE Code NOT LIKE '___' AND Code NOT LIKE '____' AND (EP=1 OR EP=2) AND Grade!=-1";
 		
 		Cursor cursor = dbC.rawQuery(select, null);
 		if(cursor.moveToFirst()){
 			do{
 				ThC++;
 				if(cursor.getString(2).contains("") && cursor.getInt(1)==1){
 					wTh += (3*cursor.getDouble(0));
 					synTh += 3;
 				}
 				else{
 					wTh += (cursor.getInt(1)*cursor.getDouble(0));
 					synTh += cursor.getInt(1);
 				}
 			}while(cursor.moveToNext());
 		}
 		cursor.close();
 		if(wTh != 0.0)
 			gradeTh = wTh/synTh;
 		
 		cursor = dbC.rawQuery(select2, null);
 		if(cursor.moveToFirst()){
 			do{
 				YsC++;
 				if(cursor.getString(2).contains("") && cursor.getInt(1)==1){
 					wYs += (3*cursor.getDouble(0));
 					synYs += 3;
 				}
 				else{
 					wYs += (cursor.getInt(1)*cursor.getDouble(0));
 					synYs += cursor.getInt(1);
 					}
 			}while(cursor.moveToNext());
 		}
 		cursor.close();
 		if(wYs != 0.0)
 			gradeYs = wYs/synYs;
 		
 		cursor = dbC.rawQuery(select3, null);
 		if(cursor.moveToFirst()){
 			do{
 				EpC++;
 				if(cursor.getString(2).contains("") && cursor.getInt(1)==1){
 					wEp += (3*cursor.getDouble(0));
 					synEp += 3;
 				}
 				else{
 					wEp += (cursor.getInt(1)*cursor.getDouble(0));
 					synEp += cursor.getInt(1);
 					}				
 			}while(cursor.moveToNext());
 		}
 		cursor.close();
 		if(wEp != 0.0)
 			gradeEp = wEp/synEp;
 		
 		dbC.close();
 		if(gradeTh==0.0 && gradeYs==0.0 && gradeEp==0.0)
 			direction = " - ";
 		else if(gradeTh>gradeYs && gradeTh>gradeEp){
 			direction = " ";
 			direC = ThC;
 		}
 		else if(gradeYs>gradeTh && gradeYs>gradeEp){
 			direction = "   ";
 			direC = YsC;
 		}
 		else if(gradeEp>gradeTh && gradeEp>gradeYs){
 			direction = "   ";
 			direC = EpC;
 		}
 		else if(gradeTh==gradeYs && gradeTh==gradeEp ){
 			if(ThC>=YsC && ThC>=EpC){
 				direction = " ";
 				direC = ThC;
 			}
 			else if(YsC>=ThC && YsC>=EpC){
 				direction = "   ";
 				direC = YsC;
 			}
 			else{
 				direction = "   ";
 				direC = EpC;
 			}
 		}
 		else if(gradeTh == gradeYs){
 			if(ThC>=YsC){
 				direction = " ";
 				direC = ThC;
 			}
 			else{
 				direction = "   ";
 				direC = YsC;
 			}
 		}
 		else if(gradeTh == gradeEp){
 			if(ThC>=EpC){
 				direction = " ";
 				direC = ThC;
 			}
 			else{
 				direction = "   ";
 				direC = EpC;
 			}
 		}
 		else if(gradeYs==gradeEp){
 			if(YsC>=EpC){
 				direction = "   ";
 				direC = YsC;
 			}
 			else{
 				direction = "   ";
 				direC = EpC;
 			}
 		}		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.display_profile, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem menuItem){   
         startActivity(new Intent(this,AgendMainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                 | Intent.FLAG_ACTIVITY_SINGLE_TOP));
         return true;
     }
 }
