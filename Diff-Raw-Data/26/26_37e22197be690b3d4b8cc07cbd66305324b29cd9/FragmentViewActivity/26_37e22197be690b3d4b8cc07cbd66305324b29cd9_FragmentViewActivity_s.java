 package ualberta.g12.adventurecreator.views;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import ualberta.g12.adventurecreator.R;
 import ualberta.g12.adventurecreator.controllers.FragmentController;
 import ualberta.g12.adventurecreator.data.AdventureCreator;
 import ualberta.g12.adventurecreator.data.Fragment;
 
 /**
  * Activity for viewing a fragment.  User can follow choices and will
  * be taken to the next fragment.
  */
 public class FragmentViewActivity extends Activity implements FView<Fragment> {
 
     private TextView fragmentTitleTextView;
     private ListView fragmentPartListView;
     private FragmentPartAdapter adapter;
     private FragmentController fragmentController;
     //private static final String TAG = "FragmentViewActivity";
     private Fragment fragment;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_fragment_viewer);
 
         fragmentController = AdventureCreator.getFragmentController();
         
         // obtain the intent
         Intent viewFragIntent = getIntent();
         fragment = (Fragment) viewFragIntent.getSerializableExtra("Fragment");
         
         // get widget references
         fragmentPartListView = (ListView) findViewById(R.id.fragmentViewPartList);
         fragmentTitleTextView = (TextView) findViewById(R.id.fragmentTitleText);
 
         setListClickListener();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         // TODO: This should be in its own method
 
         // First load fragment parts as that is the same for both modes
         // Loads fragment parts (text, images, videos, sounds, etc)
         adapter = new FragmentPartAdapter(this, R.layout.activity_fragment_editor, fragment);
         fragmentPartListView.setAdapter(adapter);
 
 
         // Loads title
         if (fragmentTitleTextView != null) 
             fragmentTitleTextView.setText(fragment.getTitle());
    }
 
     @Override
     public void update(Fragment model) {
         // TODO reload all fields based on new info from model
 
     }
 
     private void setListClickListener() {
         fragmentPartListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
                 Fragment goToFrag = fragmentController.getLinkedToFragmentOfChoice(fragment, position);
                
                if(fragment.getChoices().get(position).getisRandom()){
             		boolean notSamePos = false;
             		while(notSamePos == false){
             			Random random = new Random();
             			int max = fragment.getChoices().size() -1;
             			int randPos = random.nextInt((max - 0) + 1) + 0;
             			if (position != randPos){
             				goToFrag = fragmentController.getLinkedToFragmentOfChoice(fragment, randPos);
             				notSamePos = true;
             			}
             		}
                 }
                 if (goToFrag != null){
                     Intent intent = new Intent(FragmentViewActivity.this, FragmentViewActivity.class);
                     intent.putExtra("Fragment", goToFrag);
                     startActivity(intent);
                     finish();
                 }
             }
         });
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.fragment_part_menu, menu);
     }
 }
