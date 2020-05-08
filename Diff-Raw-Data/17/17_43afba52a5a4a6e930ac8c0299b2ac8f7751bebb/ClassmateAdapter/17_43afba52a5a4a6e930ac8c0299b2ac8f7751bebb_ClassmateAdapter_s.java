 package com.frca.vsexam.adapters;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.frca.vsexam.R;
 import com.frca.vsexam.entities.Classmate;
 import com.frca.vsexam.entities.ClassmateList;
 import com.frca.vsexam.entities.Exam;
 import com.frca.vsexam.entities.ExamList;
 import com.frca.vsexam.helper.Helper;
 import com.frca.vsexam.helper.ImageDownloaderTask;
 
 /**
  * Created by KillerFrca on 5.10.13.
  */
 public class ClassmateAdapter extends ArrayAdapter<String> {
 
     private ClassmateList classmates;
 
     private static final int resourceLayout = R.layout.classmates_item;
 
     public ClassmateAdapter(Context context, ClassmateList classmates) {
         super(context, resourceLayout);
         this.classmates = classmates;
     }
 
     @Override
     public int getCount() {
         return classmates.size();
     }
 
     @Override
     public View getView(final int position, final View convertView, final ViewGroup parent) {
         Classmate classmate = classmates.get(position);
 
         View view;
         if (convertView != null)
             view = convertView;
         else {
             final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
             view = inflater.inflate(resourceLayout, null);
         }
 
         View logo = view.findViewById(R.id.logo);                       // logo
         TextView text1 = (TextView)view.findViewById(R.id.text1);       // name
         TextView text2 = (TextView)view.findViewById(R.id.text2);       // date
         TextView text3 = (TextView)view.findViewById(R.id.text3);       // time
 
 
        new ImageDownloaderTask(getContext(), logo).loadLogo(classmate.id);
         text1.setText(classmate.name);
         text2.setText(Helper.getDateOutput(classmate.registered, Helper.DateOutputType.DATE));
         text3.setText(Helper.getDateOutput(classmate.registered, Helper.DateOutputType.TIME));
 
         return view;
     }
 }
