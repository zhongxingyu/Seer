 package cz.czechhackathon.knedlo;
 
import com.appspot.knedloreader.knedlo.Knedlo;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.widget.ImageView;
 
 /**
  * View of section feed - just header and feed fragment
  * @author shmoula
  *
  */
 public class SectionActivity extends FragmentActivity {
 	public static final String EXTRA_SECTION = "section";
 	
 	// hardcoded enumeration of sections
 	public enum SectionIdentificator { SECTION_DEFAULT, SECTION_MZ, SECTION_SUPER, SECTION_RESPEKT};
 	
 	private FeedFragment feedFragment;
 	private SectionIdentificator sectionIdentificator;
 	
	private Knedlo knedloService;
	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_section);
 		
		// service creation
		Knedlo.Builder builder = new Knedlo.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
		knedloService = builder.build();
		
 		Bundle bundle = getIntent().getExtras();
         if(bundle != null) {
         	sectionIdentificator = (SectionIdentificator) bundle.getSerializable(EXTRA_SECTION);
         }
         
         // section type switcher - for replace placeholder section image
         ImageView ivSectionImage = (ImageView) findViewById(R.id.section_image);
         switch(sectionIdentificator) {
         case SECTION_MZ:
         	ivSectionImage.setImageResource(R.drawable.mz_circle);
         	break;
         case SECTION_SUPER:
         	ivSectionImage.setImageResource(R.drawable.super_circle);
         	break;
         case SECTION_RESPEKT:
         	ivSectionImage.setImageResource(R.drawable.respekt_circle);
         	break;
         default:
         	break;
         }
 
 		feedFragment = new FeedFragment();
		feedFragment.setKnedloService(knedloService);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
 		transaction.replace(R.id.section_content_frame, feedFragment);
 		transaction.commit();
 	}
 }
