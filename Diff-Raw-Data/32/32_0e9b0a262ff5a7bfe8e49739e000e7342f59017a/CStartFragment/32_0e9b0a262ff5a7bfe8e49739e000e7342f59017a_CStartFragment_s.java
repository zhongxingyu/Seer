 package com.Andryyo.ArchPad.start;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.Andryyo.ArchPad.R;
 import com.Andryyo.ArchPad.archeryFragment.CDistanceTemplate;
 import com.Andryyo.ArchPad.archeryFragment.CRoundTemplate;
 import com.Andryyo.ArchPad.database.CSQLiteOpenHelper;
 import com.Andryyo.ArchPad.target.CTarget;
 
 public class CStartFragment extends Fragment implements View.OnClickListener {
 
     private Context context;
 	private Spinner Number_of_ends_spinner;
     private Spinner Arrows_in_ends_spinner;
     private CTargetSelectView targetSelectView;
     private CArrowSelectView arrowSelectView;
     private IOnFragmentSwapRequiredListener listener;
     private View.OnTouchListener onTouchListener;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         context = getActivity();
         if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("firstLaunch", true))
         {
             CSQLiteOpenHelper.getHelper(context).init();
             PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("firstLaunch", false).commit();
         }
         ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.activity_start,null);
         Number_of_ends_spinner=(Spinner) scrollView.findViewById(R.id.spinner1);
         Arrows_in_ends_spinner=(Spinner) scrollView.findViewById(R.id.spinner2);
         ArrayAdapter<CharSequence> Number_of_ends_adapter = ArrayAdapter.createFromResource
                 (context, R.array.Number_of_ends_array, android.R.layout.simple_spinner_item);
         Number_of_ends_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         Number_of_ends_spinner.setAdapter(Number_of_ends_adapter);
         ArrayAdapter<CharSequence> Arrows_in_ends_adapter = ArrayAdapter.createFromResource
                 (context, R.array.Arrows_in_end_array, android.R.layout.simple_spinner_item);
         Arrows_in_ends_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         Arrows_in_ends_spinner.setAdapter(Arrows_in_ends_adapter);
         targetSelectView = new CTargetSelectView(context, getLoaderManager(), getFragmentManager());
         targetSelectView.setTag("targetSelectView");
         targetSelectView.setOnTouchListener(onTouchListener);
         ((LinearLayout)scrollView.findViewById(R.id.parentLinearLayout)).addView(targetSelectView);
         arrowSelectView = new CArrowSelectView(context, getLoaderManager(), getFragmentManager());
         arrowSelectView.setLayoutParams(new LinearLayout.LayoutParams
                 (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
         ((LinearLayout)scrollView.findViewById(R.id.parentLinearLayout)).addView(arrowSelectView);
         Button button = new Button(context);
         button.setLayoutParams
                 (new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         button.setText("Начать");
         button.setOnClickListener(this);
         ((LinearLayout)scrollView.findViewById(R.id.parentLinearLayout)).addView(button);
         return scrollView;
     }
 
    public void saveRing(float ringWidth, int points, int color)    {
        ((CTargetSelectView.CTargetCreateDialog)getFragmentManager().
                findFragmentByTag("targetCreateDialog")).saveRing(ringWidth, points, color);
    }

    public void saveTarget(CTarget target)  {
        targetSelectView.saveTarget(target);
    }

     @Override
     public void onClick(View view) {
         long targetId = targetSelectView.getSelectedItemId();
         long arrowId =  arrowSelectView.getSelectedItemId();
         if ((targetId==AdapterView.INVALID_ROW_ID)||
                 (arrowId==AdapterView.INVALID_ROW_ID))
         {
             Toast.makeText(context,"Необходимо выбрать тип мишени и стрел",3000).show();
         }
         else
         {;
             CRoundTemplate template =
                 new CRoundTemplate()
                 .addDistanceTemplate(new CDistanceTemplate(Integer.parseInt(Number_of_ends_spinner.getSelectedItem().toString()),
                         Integer.parseInt(Arrows_in_ends_spinner.getSelectedItem().toString()),
                         targetId))
                 .setArrowId(arrowId);
             listener.startArcheryFragment(template);
         }
     }
 
     public void setOnFragmentSwapListener(IOnFragmentSwapRequiredListener listener) {
         this.listener = listener;
     }
 
     public void setOnTouchListener(View.OnTouchListener listener)   {
         onTouchListener = listener;
         if (targetSelectView!=null)
             targetSelectView.setOnTouchListener(listener);
     }
 }
