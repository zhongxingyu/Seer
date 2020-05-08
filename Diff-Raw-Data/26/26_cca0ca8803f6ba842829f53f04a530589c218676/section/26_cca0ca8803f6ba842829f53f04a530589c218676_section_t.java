 package nz.gen.wellington.guardian.android.activities;
 
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.SectionArticleSet;
 import nz.gen.wellington.guardian.android.sqllite.DataHelper;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class section extends ArticleListActivity {
 	
 	private static final String TAG = "section";	
 	private Section section;
 	private MenuItem favouriteMenuItem;
 
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         section = (Section) this.getIntent().getExtras().get("section");
     	setHeading(section.getName());
     	setHeadingColour(section.getColour());
     	updateArticlesHandler = new UpdateArticlesHandler(this, getArticleSet());
 	}
 
 	
 	protected ArticleSet getArticleSet() {
 		return new SectionArticleSet(section);
 	}
 		
	@Override
	protected String getRefinementDescription() {
		return "These tags have been used within the " + getArticleSet().getName() + " section recently:";
	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, "Home");
 		menu.add(0, 5, 0, "Refresh");
 		
 		DataHelper dh = new DataHelper(this);
 		if (dh.isFavourite(section)) {
 			favouriteMenuItem = menu.add(0, 4, 0, "Remove Favourite");
 		} else {
 			favouriteMenuItem = menu.add(0, 4, 0, "Add to Favourites");
 		}
 		dh.close();
 		
 	    return true;
 	}
 	
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 1:
 			switchToMain();
 			return true;
 		case 2:
 			switchToFavourites();
 			return true;
 		case 3:
 			switchToSections();
 			return true;
 		case 4:
 			addToFavourites();
 			return true;
 		case 5:
 			refresh(true);
 			return true;
 		}
 		return false;
 	}
 	
 	
 	private void addToFavourites() {
 		DataHelper dh = new DataHelper(this);
 		if (!dh.isFavourite(section)) {
 			Log.i(TAG, "Adding current tag to favourites: " + section.getName());
 			dh.addSection(section);
 			favouriteMenuItem.setTitle("Remove Favourite");
 	
 		} else {
 			Log.i(TAG, "Removing current section from favourites: " + section.getName());			
 			dh.removeSection(section);
 			favouriteMenuItem.setTitle("Add to Favourites");
 		}
 		dh.close();	
 	}
 	
 }
