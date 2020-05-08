 
 package org.inftel.ssa.mobile.ui;
 
 import org.inftel.ssa.mobile.R;
 import org.inftel.ssa.mobile.ui.fragments.ProjectDetailFragment;
 
import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 
public class ProjectActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.ssa_project_activity);
     }
 
     static public final class Information {
 
         public static final String[] TITLES =
         {
                 "Titulo 1",
                 "Titulo 2",
                 "Titulo 3",
                 "Titulo 4",
                 "Titulo 5",
                 "Titulo 6"
         };
 
         public static final String[] TEXT =
         {
                 "Mostrando Titulo 1",
                 "Mostrando Titulo 2",
                 "Mostrando Titulo 3",
                 "Mostrando Titulo 4",
                 "Mostrando Titulo 5",
                 "Mostrando Titulo 6"
 
         };
     }
 
     static public class ProjectDetailPortraitContainerFragment extends FragmentActivity {
 
         @Override
         protected void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             ProjectDetailFragment details = new ProjectDetailFragment();
             details.setArguments(getIntent().getExtras());
             getSupportFragmentManager().beginTransaction()
                     .add(android.R.id.content, details).commit();
 
         }
     }
 
 }
