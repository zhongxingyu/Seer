 package edu.cmusv.lions.petmobile;
 
 import java.util.Map;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ListAdapter;
 import android.widget.SimpleAdapter;
 import edu.cmusv.lions.petmobile.domain.Project;
 import edu.cmusv.lions.petmobile.domain.ProjectPhase;
 
 public class ProjectPhaseListActivity extends PetListActivity {
 	
 	private String mProjectId;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle("Project Phases");
 	}
 	
 	protected Class<?> getConstantsClass() {
 		return ProjectPhase.class;
 	}
 	
 	@Override
 	protected void requestJsonData() {
 		Intent intent = getIntent();
 		mProjectId = intent.getStringExtra(Project.ID);
 		mDataSource.getProjectPhaseListAsync(mProjectId);
 	}
 	
 	protected ListAdapter getListAdapter() {
 		return new SimpleAdapter(this, mList, R.layout.list_item, new String[] { ProjectPhase.ID },
 				new int[] { R.id.list_item_name });
 	}
 	
 	protected void onItemSelected(Map<String, String> selectedItemAttributes) {
 		Intent intent = new Intent(getApplicationContext(), ProjectPhaseDetailsActivity.class);
 		intent.putExtra(ProjectPhase.ID, selectedItemAttributes.get(ProjectPhase.ID));
		intent.putExtra(Project.ID, mProjectId);
 		startActivity(intent);
 	}
 
 }
