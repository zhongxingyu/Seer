 package edu.cmusv.lions.petmobile;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import edu.cmusv.lions.petmobile.domain.Deliverable;
 import edu.cmusv.lions.petmobile.domain.ProjectPhase;
 import edu.cmusv.lions.petmobile.util.DataSource;
 
 public class DeliverableDetailsActivity extends PetDetailsActivity {
 
 	private ViewGroup mDetailsContainer;
 	private String mDeliverableId;
 	private String mProjectId;
 	private String mProjectPhaseId;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle("Deliverable Details");

 		setContentView(R.layout.activity_deliverable_details);
		mDetailsContainer = (ViewGroup) findViewById(R.id.details_container);
 
 		mDataSource = new DataSource();
 		Intent intent = getIntent();
 		mProjectId = intent.getStringExtra(ProjectPhase.PROJECT_ID);
 		mProjectPhaseId = intent.getStringExtra(Deliverable.PROJECT_PHASE_ID);
 		mDeliverableId = intent.getStringExtra(Deliverable.ID);
 		renderDetails();
 	}
 
 	@Override
 	protected String[] getDisplayKeys() {
		return new String[] { Deliverable.NAME, Deliverable.DESCRIPTION, Deliverable.DELIVERABLE_TYPE_NAME, Deliverable.COMPLEXITY_NAME, Deliverable.UNIT_OF_MEASURE_NAME, Deliverable.ESTIMATED_EFFORT, Deliverable.ESTIMATED_PRODUCTION_RATE, Deliverable.ESTIMATED_SIZE };
 	}
 	
 	@Override
 	protected ViewGroup getDetailsContainer() {
 		return mDetailsContainer;
 	}
 
 	@Override
 	protected void requestJsonData() {
 		mDataSource.getDeliverableAsync(mProjectId, mProjectPhaseId, mDeliverableId);
 	}
 
 }
