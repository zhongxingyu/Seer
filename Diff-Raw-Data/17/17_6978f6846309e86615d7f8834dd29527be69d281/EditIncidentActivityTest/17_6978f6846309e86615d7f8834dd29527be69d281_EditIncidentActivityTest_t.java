 package au.com.dius.resilience.activity.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.view.KeyEvent;
 import android.widget.Button;
 import android.widget.EditText;
 import au.com.dius.resilience.R;
 import au.com.dius.resilience.activity.EditIncidentActivity;
 import au.com.dius.resilience.model.Incident;
 import au.com.dius.resilience.persistence.Repository;
 import au.com.dius.resilience.persistence.RepositoryFactory;
 import au.com.dius.resilience.persistence.SqlLiteRepository;
 
 public class EditIncidentActivityTest extends
     ActivityInstrumentationTestCase2<EditIncidentActivity> {
 
   private EditIncidentActivity activity;
 
   public EditIncidentActivityTest() {
     super("au.com.dius.resilience", EditIncidentActivity.class);
   }
 
   public void setUp() {
     activity = getActivity();
<<<<<<< HEAD
     RepositoryFactory.setTestFlag(true);
=======
    repository = RepositoryFactory.create(getActivity());
>>>>>>> 2ed11af... merge fix
     getActivity().getApplication().deleteDatabase(SqlLiteRepository.DB_NAME);
   }
 
   public void testSaveNoteToDB() {
     
     EditText name = (EditText) activity.findViewById(R.id.incident_name);
     EditText notes = (EditText) activity.findViewById(R.id.incident_note);
     Button createButton = (Button) activity.findViewById(R.id.submit_incident);
     
     TouchUtils.clickView(this, name);
     this.sendKeys(KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_E);
     this.sendKeys(KeyEvent.KEYCODE_BACK);
     
     sleep(2000);
     
     TouchUtils.clickView(this, notes);
     this.sendKeys(KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_E);
     this.sendKeys(KeyEvent.KEYCODE_BACK);
     
     sleep(2000);
     
     TouchUtils.clickView(this, createButton);
     
     // Check that it saved
     Repository repository = RepositoryFactory.create(getActivity());
     Incident incident = repository.findAll().get(0);
     
     assertEquals("name", incident.getName());
     assertEquals("fire", incident.getNote());
   }
 
   private void sleep(long time) {
     try {
       Thread.sleep(2000);
     } catch (InterruptedException e) {
       throw new RuntimeException(e);
     }
   }
   
   public void tearDown() {
     RepositoryFactory.setTestFlag(false);
     getActivity().getApplication().deleteDatabase(SqlLiteRepository.DB_NAME);
   }
 }
