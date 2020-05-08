 package com.ntnu.eit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.ntnu.eit.common.model.Department;
 import com.ntnu.eit.common.service.ServiceFactory;
 import com.ntnu.eit.department.model.DepartmentListAdapter;
 
 public class DepartmentActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		//Super
 		super.onCreate(savedInstanceState);
 		
 		//Log
 		Log.i("EiT", getClass().getSimpleName() + ".onCreate()");
 		
 		//This
 		setContentView(R.layout.activity_department);
 		
 		//Departments
 		Department[] departments = ServiceFactory.getInstance().getDepartmentService().getDepartments();
 		ListView listView = (ListView) findViewById(R.id.departmentList);
 		listView.setAdapter(new DepartmentListAdapter(this, R.layout.department_row, departments));
 	}
 	
 	@Override
 	protected void onResume() {
 		//Super
 		super.onResume();
 
 		//This
 		int size = PreferenceManager.getDefaultSharedPreferences(this).getInt("text_size", 50);
 		Button button = (Button) findViewById(R.id.departmentNextButton);
 		button.setTextSize(50*size/100);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_department, menu);
 		return true;
 	}
 	
 	public void onDepartmentRowClick(View view){
 		CheckBox checkBox = (CheckBox) view.findViewById(R.id.departmentCheckBox);
 		checkBox.setChecked(!checkBox.isChecked());
 	}
 	
 	public void onDepartmentRowClick2(View view){
 		onDepartmentRowClick((View) view.getParent());
 	}
 	
 	public void onNextClicked(View view){
 		//Finding selected departments
 		List<Integer> indices = new ArrayList<Integer>();
 		ListView listView = (ListView) findViewById(R.id.departmentList);
 		for(int i = 0; i < listView.getAdapter().getCount(); i++){
 			Department Department = (Department) listView.getAdapter().getItem(i);
 			ViewGroup row = (ViewGroup) listView.getChildAt(i);
 			
			if(row != null){		
				CheckBox box = (CheckBox)row.getChildAt(1);
				if(box != null){					
					boolean isChecked = box.isChecked();
					
					if(isChecked){				
						indices.add(Department.getDepartmentID());
					}
				}
 			}
 		}
 		
 		//Starting PasientsActivity
 		if(indices.isEmpty()){
 			Toast.makeText(this, "Please select departments", Toast.LENGTH_SHORT).show();
 		}else{
 			//Init
 			Intent intent = new Intent(this, PatientsActivity.class);
 			
 			//Creating param
 			int[] temp = new int[indices.size()];
 			for (int i = 0; i < temp.length; i++) {
 				temp[i] = indices.get(i).intValue();
 			}
 			
 			intent.putExtra(PatientsActivity.DEPARTMENTS_INDICES, temp);
 			
 			startActivity(intent);
 		}
 	}
 }
