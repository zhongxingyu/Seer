 package com.gris.ege.activity;
 
 import com.gris.ege.R;
 import com.gris.ege.db.ResultsOpenHelper;
 import com.gris.ege.other.GlobalData;
 import com.gris.ege.other.LessonsParser;
 import com.gris.ege.other.Log;
 import com.gris.ege.other.TasksParser;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener
 {
 	private static final String TAG="MainActivity";
 
 	private static final int REQUEST_USER_SELECT   = 1;
     private static final int REQUEST_LESSON_SELECT = 2;
     private static final int REQUEST_VIEW_TASKS    = 3;
     private static final int REQUEST_START_TEST    = 4;
 
     public  static final int RESULT_USER_SELECT    = 1;
     public  static final int RESULT_LESSON_SELECT  = 1;
 
     private static final int CHOICE_VIEW_TASKS     = 0;
     private static final int CHOICE_START_TEST     = 1;
     private static final int CHOICE_VIEW_RESULTS   = 2;
 
 
 
     private EditText          mNameEditText;
     private Button            mSelectUserButton;
 
     private Button            mLessonButton;
 
     private Button            mViewTasksButton;
     private Button            mStartTestButton;
     private Button            mViewResultsButton;
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_main);
 
         if (savedInstanceState==null)
         {
         	Log.reset();
         	Log.v(TAG, "Application started");
         }
 
         // Translate lessons
         new LessonsParser().parse(this);
 
         // Get controls
         mNameEditText      = (EditText)findViewById(R.id.nameEditText);
         mSelectUserButton  = (Button)  findViewById(R.id.selectUserButton);
         mLessonButton      = (Button)  findViewById(R.id.lessonButton);
         mViewTasksButton   = (Button)  findViewById(R.id.viewTasksButton);
         mStartTestButton   = (Button)  findViewById(R.id.startTestButton);
         mViewResultsButton = (Button)  findViewById(R.id.viewResultsButton);
 
         // Set listeners
         mSelectUserButton.setOnClickListener(this);
         mLessonButton.setOnClickListener(this);
         mViewTasksButton.setOnClickListener(this);
         mStartTestButton.setOnClickListener(this);
         mViewResultsButton.setOnClickListener(this);
 
         // Restore preferences
         SharedPreferences aSettings = getSharedPreferences(GlobalData.PREFS_NAME, 0);
 
         String aUserName         = aSettings.getString(GlobalData.OPTION_USER_NAME, "");
         String aSelectedLessonID = aSettings.getString(GlobalData.OPTION_SELECTED_LESSON, "");
 
         mNameEditText.setText(aUserName);
         selectLesson(aSelectedLessonID);
 
         updateSelectUserButton();
     }
 
     @Override
     protected void onDestroy()
     {
         saveUserName();
 
         super.onDestroy();
     }
 
     public void saveUserName()
     {
         SharedPreferences aSettings = getSharedPreferences(GlobalData.PREFS_NAME, 0);
         SharedPreferences.Editor aEditor = aSettings.edit();
         aEditor.putString(GlobalData.OPTION_USER_NAME, mNameEditText.getText().toString());
         aEditor.commit();
     }
 
     public void updateSelectUserButton()
     {
     	if (
     	    mSelectUserButton.getVisibility()==View.GONE
     	    &&
     		!new ResultsOpenHelper(this).isUsersListEmpty()
            )
     	{
     		mSelectUserButton.setVisibility(View.VISIBLE);
     	}
     }
 
     public void selectLesson(String aId)
     {
         int index=-1;
 
         for (int i=0; i<GlobalData.lessons.size(); ++i)
         {
             if (GlobalData.lessons.get(i).getId().equals(aId))
             {
                 index=i;
                 break;
             }
         }
 
         if (index<0)
         {
             index=0;
         }
 
         mLessonButton.setText(GlobalData.lessons.get(index).getName());
 
 
 
         GlobalData.selectedLesson=GlobalData.lessons.get(index);
 
 
 
         // Save preferences
         SharedPreferences aSettings = getSharedPreferences(GlobalData.PREFS_NAME, 0);
         SharedPreferences.Editor aEditor = aSettings.edit();
         aEditor.putString(GlobalData.OPTION_SELECTED_LESSON, GlobalData.selectedLesson.getId());
         aEditor.commit();
     }
 
     public void chooseUser()
     {
         saveUserName();
 
         Intent aLessonSelectIntent=new Intent();
         aLessonSelectIntent.setClass(this, UserChooseActivity.class);
         startActivityForResult(aLessonSelectIntent, REQUEST_USER_SELECT);
     }
 
     public void chooseLesson()
     {
         saveUserName();
 
         Intent aLessonSelectIntent=new Intent();
         aLessonSelectIntent.setClass(this, LessonChooseActivity.class);
         startActivityForResult(aLessonSelectIntent, REQUEST_LESSON_SELECT);
     }
 
     public void makeChoose(int aChoice)
     {
         if (mNameEditText.length()>0)
         {
             saveUserName();
 
             // Get list of tasks for selectedLesson
             GlobalData.tasks=new TasksParser().parse(this);
 
             switch (aChoice)
             {
                 case CHOICE_VIEW_TASKS:
                 {
                    Intent aViewTasksIntent=new Intent();
                    aViewTasksIntent.setClass(this, ViewTasksActivity.class);
                    startActivityForResult(aViewTasksIntent, REQUEST_VIEW_TASKS);
                 }
                 break;
                 case CHOICE_START_TEST:
                 {
                    Intent aStartTestIntent=new Intent();
                    aStartTestIntent.setClass(this, StartTestActivity.class);
                    startActivityForResult(aStartTestIntent, REQUEST_START_TEST);
                 }
                 break;
                 case CHOICE_VIEW_RESULTS:
                 {
                    Intent aViewResultsIntent=new Intent();
                    aViewResultsIntent.setClass(this, ViewResultsActivity.class);
                    startActivity(aViewResultsIntent);
                 }
                 break;
             }
         }
         else
         {
             Toast.makeText(this, R.string.name_is_empty, Toast.LENGTH_SHORT).show();
         }
     }
 
     @Override
     public void onClick(View v)
     {
         switch (v.getId())
         {
             case R.id.selectUserButton:
                 chooseUser();
             break;
             case R.id.lessonButton:
                 chooseLesson();
             break;
             case R.id.viewTasksButton:
                 makeChoose(CHOICE_VIEW_TASKS);
             break;
             case R.id.startTestButton:
                 makeChoose(CHOICE_START_TEST);
             break;
             case R.id.viewResultsButton:
                 makeChoose(CHOICE_VIEW_RESULTS);
             break;
         }
     }
 
     @Override
     protected void onActivityResult(int aRequestCode, int aResultCode, Intent aData)
     {
         switch (aRequestCode)
         {
             case REQUEST_USER_SELECT:
             {
                 switch (aResultCode)
                 {
                     case RESULT_USER_SELECT:
                     {
                     	String aUserName=aData.getStringExtra(GlobalData.USER_NAME);
                     	mNameEditText.setText(aUserName);
                     }
                     break;
                 }
             }
             break;
             case REQUEST_LESSON_SELECT:
             {
                 switch (aResultCode)
                 {
                     case RESULT_LESSON_SELECT:
                     {
                         String aId=aData.getStringExtra(GlobalData.LESSON_ID);
                         selectLesson(aId);
                     }
                     break;
                 }
             }
             break;
             case REQUEST_VIEW_TASKS:
             case REQUEST_START_TEST:
             {
                 updateSelectUserButton();
             }
             break;
         }
     }
 }
