 package com.campus.prime.ui.home;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.campus.prime.R;
import com.campus.prime.ui.user.DiaListAdapter;
import com.campus.prime.ui.user.DiaListAdapter.OnItemClickListener;
 import com.campus.prime.ui.user.EditUserActivity;
 import com.campus.prime.ui.view.ThemeDialog;
 import com.campus.prime.ui.view.ThemeTextView;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class EducationActivity extends Activity implements OnItemClickListener
 {
 	
 	private ThemeDialog dialog;
 	List <String> schools = new ArrayList <String> ();
 	List <String> academys = new ArrayList <String> ();
 	List <String> grades = new ArrayList <String> ();
 	
 	
 	ThemeTextView editSchool;
 	ThemeTextView editAcademy;
 	ThemeTextView editGrade;
 	
 	ThemeTextView currentView;
 	public void getSchools()
 	{
 		schools.add("ѧ");
 		schools.add("㽭ѧ");
 		schools.add("廪ѧ");
 		schools.add("۴ѧ");
 		schools.add("ྩѧ");
 		schools.add("ѧ");
 		schools.add("ӱѧ");
 		schools.add("ϴѧ");
 		schools.add("ѧ");
 		schools.add("ѧ");
 		schools.add("ϴѧ");
 		schools.add("ϴѧ");		
 	}
 	public void getAcademys()
 	{	
 		academys.add("ѧԺ");
 		academys.add("ѧԺ");
 		academys.add("ϢѧԺ");
 		academys.add("ѧͳѧԺ");
 		academys.add("ѧԺ");
 		academys.add("ѧԺ");
 		academys.add("ѧԺ");
 		academys.add("ѧԺ");
 		academys.add("ѧԺ");
 		academys.add("ùѧԺ");
 		
 		
 		
 	}
 	public void getGrades()
 	{
 		grades.add("2013");
 		grades.add("2012");
 		grades.add("2011");
 		grades.add("2010");
 		grades.add("2009");
 		grades.add("2008");
 		grades.add("2007");
 		grades.add("2006");
 		grades.add("2005");
 		grades.add("2004");
 		
 	}
 	public void getView()
 	{
 		editSchool = (ThemeTextView) this.findViewById(R.id.school);
 		editAcademy = (ThemeTextView) this.findViewById(R.id.academy);
 		editGrade = (ThemeTextView) this.findViewById(R.id.grade);
 	}
 	
 	public void getData()
 	{
 		getSchools();
 		getAcademys();
 		getGrades();
 	}
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_education);
 		getData();
 		this.findViewById(R.id.school).setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				currentView = (ThemeTextView) EducationActivity.this.findViewById(R.id.school);
 				showDialog(schools);
 			}});
 		this.findViewById(R.id.academy).setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				currentView = (ThemeTextView) EducationActivity.this.findViewById(R.id.academy);
 ;				showDialog(academys);
 			}});
 		this.findViewById(R.id.grade).setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				currentView = (ThemeTextView) EducationActivity.this.findViewById(R.id.grade);
 				showDialog(grades);
 			}});
 	}
 	
 	public void showDialog(List<String> data)
 	{
 		dialog = new ThemeDialog(EducationActivity.this,R.style.my_dialog);
 		LayoutInflater inflater = LayoutInflater.from(EducationActivity.this);
 		View view = inflater.inflate(R.layout.dia_list, null);
 		ListView listView = (ListView) view.findViewById(R.id.dia_list);
		DiaListAdapter adapter = new DiaListAdapter(EducationActivity.this,R.layout.dia_item,R.id.dia_item,data);
 		adapter.setListener(this);
 		dialog.setCancelable(true);
 		dialog.setCanceledOnTouchOutside(true);		
 		listView.setAdapter(adapter);			
 		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);			
 		dialog.show();
 		dialog.setContentView(view,params);
 	}
 	
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.education, menu);
 		
 		// Add Custom View
 		ActionBar actionBar = getActionBar();	
 		actionBar.setCustomView(R.layout.action_education);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setDisplayShowCustomEnabled(true);
         actionBar.setDisplayHomeAsUpEnabled(true);
  
         // Setting Action Event
         TextView next = (TextView) this.findViewById(R.id.next);
         next.setOnClickListener(new OnClickListener()      	
         {
 			@Override
 			public void onClick(View arg0) 
 			{
 				// TODO Auto-generated method stub
 				Intent intent_next = new Intent(EducationActivity.this, RegisterActivity.class);
 				startActivity(intent_next);
 			}
 		});
 		
 		return true;
 	}
 	
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		switch (item.getItemId()) 
 		{
 			case android.R.id.home:
 				Intent intent_previous = new Intent(this, LoginActivity.class);
 				startActivity(intent_previous);
 				break;
 			
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	@Override
	public void onClick(int position) {
 		// TODO Auto-generated method stub
 		dialog.dismiss();
		currentView.setText(position + "");
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 }
