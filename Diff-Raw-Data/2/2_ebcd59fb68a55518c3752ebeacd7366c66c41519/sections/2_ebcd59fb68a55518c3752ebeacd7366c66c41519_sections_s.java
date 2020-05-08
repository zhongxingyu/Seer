 package nz.gen.wellington.guardian.android.activities;
 
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.activities.ui.TagListPopulatingService;
 import nz.gen.wellington.guardian.android.api.SectionDAO;
 import nz.gen.wellington.guardian.android.api.filtering.SectionSorter;
 import nz.gen.wellington.guardian.android.factories.ArticleSetFactory;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.network.NetworkStatusService;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class sections extends DownloadProgressAwareActivity implements FontResizingActivity {
 		
 	private SectionDAO sectionDAO;
 	private NetworkStatusService networkStatusService;
 	private ArticleSetFactory articleSetFactory;
 	private TagListPopulatingService tagListPopulatingService;
 		
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		sectionDAO = SingletonFactory.getSectionDAO(this.getApplicationContext());
 		articleSetFactory = SingletonFactory.getArticleSetFactory(this.getApplicationContext());
 		networkStatusService = SingletonFactory.getNetworkStatusService(this.getApplicationContext());
 		tagListPopulatingService = SingletonFactory.getTagListPopulator(this.getApplicationContext());
 		setContentView(R.layout.sections);
 		
 		setHeading("Sections");
 		setHeadingColour("#0061A6");
 	}
 	
 		
 	@Override
 	public void onResume() {
 		super.onResume();
 		LinearLayout mainPane = (LinearLayout) findViewById(R.id.MainPane);
 		mainPane.removeAllViews();
 		
 		if (sectionDAO.areSectionsAvailable()) {
 			populateSections(baseFontSize);
 		} else {
 			outputNoSectionsWarning(baseFontSize);	// TODO
 		}
 	}
 
 				
 	private void populateSections(int baseSize) {
 		List<Section> sections = sectionDAO.getSections();		
 		if (sections != null) {
 			sections = SectionSorter.sortByName(sections);	// TODO push this back behind the section dao for performance		
 			LayoutInflater inflater = LayoutInflater.from(this);		
 			LinearLayout authorList = (LinearLayout) findViewById(R.id.MainPane);
 			
 			tagListPopulatingService.populateTags(inflater,
 					networkStatusService.isConnectionAvailable(), authorList,
 					articleSetFactory.getArticleSetsForSections(sections),
 					colourScheme
 			);
 			
 		} else {
         	outputNoSectionsWarning(baseSize);
 		}
 	}
 	
 	
 	private void outputNoSectionsWarning(float baseFontSize) {
 		LinearLayout mainpane;
 		mainpane = (LinearLayout) findViewById(R.id.MainPane);
 		TextView noArticlesMessage = new TextView(this.getApplicationContext());
 		noArticlesMessage.setText("No sections available.");
 		
 		noArticlesMessage.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseFontSize);
 		noArticlesMessage.setTextColor(colourScheme.getHeadline());
 		noArticlesMessage.setPadding(2, 3, 2, 3);					
 		mainpane.addView(noArticlesMessage, 0);
 	}
 	
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MenuedActivity.HOME, 0, "Home");
 		menu.add(0, MenuedActivity.FAVOURITES, 0, "Favourites");
 		menu.add(0, MenuedActivity.SEARCH_TAGS, 0, "Search tags");
 	    return true;
 	}
 	
 }
