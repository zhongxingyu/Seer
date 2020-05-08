 package com.ntnu.eit.pasients.model;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.ntnu.eit.R;
 import com.ntnu.eit.common.model.Department;
 import com.ntnu.eit.common.model.Patient;
 import com.ntnu.eit.common.model.Task;
 import com.ntnu.eit.common.service.ServiceFactory;
 import com.ntnu.eit.pasients.service.PatientPictureUpdaterService;
 
 public class PatientsListAdapter extends ArrayAdapter<Patient>{
 
 	private Context context;
 	private List<Patient> patients;
 	private int textViewResourceId;
 	private SimpleDateFormat format;
 
 	public PatientsListAdapter(Context context, int textViewResourceId, List<Patient> pasients) {
 		super(context, textViewResourceId, pasients);
 		
 		this.patients = pasients;
 		this.context = context;
 		this.textViewResourceId = textViewResourceId;
 		format = new SimpleDateFormat("HH:mm", Locale.getDefault());
 	}
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
     	View row = convertView;
     	PasientHolder holder = null;
         
         if(row == null){
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             row = inflater.inflate(textViewResourceId, parent, false);
             
             holder = new PasientHolder();
             holder.nameView = (TextView) row.findViewById(R.id.pasientName);
             holder.departmentView = (TextView) row.findViewById(R.id.pasientDepartment);
             holder.clockView = (TextView) row.findViewById(R.id.pasientClock);
             holder.pictureView = (ImageView) row.findViewById(R.id.pasientImage);
             
             row.setTag(holder);
         }else{
             holder = (PasientHolder)row.getTag();
         }
         
         //Text size
         int size = PreferenceManager.getDefaultSharedPreferences(context).getInt("text_size", 50);
         holder.nameView.setTextSize(50*size/100);
         holder.clockView.setTextSize(50*size/100);
         holder.departmentView.setTextSize(30*size/100);
         
         Patient pasient = patients.get(position);
         holder.nameView.setText(pasient.getFirstname() + ", " + pasient.getLastname());
         
         Department department = ServiceFactory.getInstance().getDepartmentService().getDepartmentById(pasient.getDepartmentID());
         holder.departmentView.setText(department.getName());
         
         //Setting picture
         PatientPictureUpdaterService.setPictureForPatient(context, holder.pictureView, pasient.getPatientID());
         int picture = 0;
         switch (pasient.getPatientID()%3) {
 		case 0:
 			picture = R.drawable.old_man1;
 			break;
 		case 1:
 			picture = R.drawable.old_man2;
 			break;
 		case 2:
			picture = R.drawable.old_man3;
			break;
        }
         
         Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), picture);
         if(bitmap != null){        	
         	holder.pictureView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 200, 200, false));
         }
         
         //Set clock text
         List<Task> tasks = ServiceFactory.getInstance().getTaskService().getTasks(pasient.getPatientID());
         Log.d("Boge", "Patient " + pasient.getPatientID() + " has " + tasks.size() + " Tasks");
         if(tasks.size() > 0){
         	for (int i = 0; i < tasks.size(); i++) {
 				if(!tasks.get(i).isExecuted()){	
 					holder.clockView.setText(format.format(tasks.get(i).getTimestamp()));
 					break;
 				}
 			}
         }else{
 			holder.clockView.setText(format.format(new Date()));
         }
         
         return row;
     } 
     
     static class PasientHolder{
         TextView nameView;
         TextView departmentView;
         TextView clockView;
         ImageView pictureView;
     }
 }
