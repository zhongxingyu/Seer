 package nz.gen.wellington.guardian.android.activities;
 
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.activities.ui.TagListPopulatingService;
 import nz.gen.wellington.guardian.android.factories.ArticleSetFactory;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.ColourScheme;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.Tag;
 import nz.gen.wellington.guardian.android.network.NetworkStatusService;
 import nz.gen.wellington.guardian.android.usersettings.FavouriteSectionsAndTagsDAO;
 import nz.gen.wellington.guardian.android.usersettings.PreferencesDAO;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class favourites extends ArticleListActivity implements FontResizingActivity {
 	
 	private PreferencesDAO preferencesDAO;
     private ArticleSetFactory articleSetFactory;
 
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		
         preferencesDAO = SingletonFactory.getPreferencesDAO(this.getApplicationContext());
         articleSetFactory = SingletonFactory.getArticleSetFactory(this.getApplicationContext());
         
         setContentView(R.layout.favourites);        
         setHeading("Favourites");
         setHeadingColour("#0061A6");
     	showSeperators = true;
     	showMainImage = false;
 	}
 	
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		final int baseSize = preferencesDAO.getBaseFontSize();
 		setFontSize(baseSize);
 	}
 
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		LinearLayout favouritesPane = (LinearLayout) findViewById(R.id.FavouritesPane);
 		favouritesPane.removeAllViews();
 		populateFavourites();		
 	}
 
 
 	private void populateFavourites() {
 		FavouriteSectionsAndTagsDAO favouriteSectionsAndTagsDAO = SingletonFactory.getFavouriteSectionsAndTagsDAO(this.getApplicationContext());		
 
 		TextView description = (TextView) findViewById(R.id.Description);
 		
 		// TODO - this implies two sqllite queries in a row - needs to be done in one open open and close if possible.
 		List<Section> favouriteSections = favouriteSectionsAndTagsDAO.getFavouriteSections();
 		List<Tag> favouriteTags = favouriteSectionsAndTagsDAO.getFavouriteTags();
 		
 		boolean favouritesLoadedCorrectly = (favouriteSections != null && favouriteTags != null);
 		if (!favouritesLoadedCorrectly) {
 			description.setText("There was a problem loading your favorite sections and tags.");			
 			return;
 		}
 		
 		boolean hasFavourites = !favouriteSections.isEmpty() || !favouriteTags.isEmpty();
 		if (hasFavourites) {
 			LayoutInflater inflater = LayoutInflater.from(this);
 			LinearLayout authorList = (LinearLayout) findViewById(R.id.FavouritesPane);
 		
 			// TODO move to a layout
 			LinearLayout tagGroup = new LinearLayout(this.getApplicationContext());
 			tagGroup.setOrientation(LinearLayout.VERTICAL);
 			tagGroup.setPadding(2, 0, 2, 0);
 			
 			boolean connectionIsAvailable = new NetworkStatusService(this.getApplicationContext()).isConnectionAvailable();			
 			TagListPopulatingService.populateTags(inflater, connectionIsAvailable, tagGroup, articleSetFactory.getArticleSetsForSections(favouriteSections), this.getApplicationContext());
 			TagListPopulatingService.populateTags(inflater, connectionIsAvailable, tagGroup, articleSetFactory.getArticleSetsForTags(favouriteTags), this.getApplicationContext());
 			
 			authorList.addView(tagGroup);
 			
 			description.setText("The following sections and tags have been marked as favourites.");			
 			
 		} else {
 			description.setText("No favourite sections of tags have been set.\n\nAdd favourites to populate the articles on this screen and to " +
 					"indicate which articles should be downloaded for offline viewing.");			
 		}
 	}
 	
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, "Home");
 		menu.add(0, 2, 0, "Sections");
 		menu.add(0, 3, 0, "Saved articles");
 		menu.add(0, 4, 0, "Refresh");
 	    return true;
 	}
 	
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 1:
 			switchToMain();
 			return true;
 		case 2:
 			switchToSections();
 			return true;
 		case 3:
 		    switchToSavedArticles();
 		    return true;
 		case 4:
 			refresh();
 			return true;
 		}
 		
 		return false;
 	}
 
	// TODO this code is duplicated in several places - should the article set factory do the tag and section lookups?
 	@Override
 	protected ArticleSet getArticleSet() {	
 		FavouriteSectionsAndTagsDAO favouriteSectionAndTagsDAO = SingletonFactory.getFavouriteSectionsAndTagsDAO(this.getApplicationContext());		
 		List<Section> favouriteSections = favouriteSectionAndTagsDAO.getFavouriteSections();
 		List<Tag> favouriteTags = favouriteSectionAndTagsDAO.getFavouriteTags();
		return articleSetFactory.getFavouritesArticleSetFor(favouriteSections, favouriteTags);		
 	}
	
 	@Override
 	protected String getRefinementDescription(String refinementType) {
 		return null;
 	}
 
 
 	@Override
 	public void setFontSize(int baseSize) {
 		TextView description = (TextView) findViewById(R.id.Description);
         description.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize);
         description.setTextColor(ColourScheme.BODYTEXT);
 	}
 	
 }
