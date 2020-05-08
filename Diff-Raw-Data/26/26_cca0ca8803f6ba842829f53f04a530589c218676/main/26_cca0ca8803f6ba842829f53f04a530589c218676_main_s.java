 package nz.gen.wellington.guardian.android.activities;
 
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.api.ArticleDAOFactory;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.FavouriteStoriesArticleSet;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.Tag;
 import nz.gen.wellington.guardian.android.model.TopStoriesArticleSet;
 import nz.gen.wellington.guardian.android.usersettings.FavouriteSectionsAndTagsDAO;
 
 import org.joda.time.DateTime;
 
 import android.app.Dialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 public class main extends ArticleListActivity {
 	
 	private static final String TAG = "main";
 	private DateTime loaded;
 	
 	public main() {
 	}
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);        
         hindHeading();
     	updateArticlesHandler = new UpdateArticlesHandler(this, getArticleSet());
     	showSeperators = true;
     	showMainImage = false;
 	}
 	
 	
 	@Override
 	protected boolean shouldRefreshView(LinearLayout mainPane) {
 		DateTime modtime = ArticleDAOFactory.getDao(this.getApplicationContext()).getModificationTime(new TopStoriesArticleSet());
 		boolean topStoriesFileHasChanged = modtime != null && modtime.isAfter(loaded);
 		return super.shouldRefreshView(mainPane) || topStoriesFileHasChanged;
 	}
 
 	
 	@Override
 	protected ArticleSet getArticleSet() {		
 		FavouriteSectionsAndTagsDAO dao = new FavouriteSectionsAndTagsDAO(articleDAO, this);
 	
 		List<Section> favouriteSections = dao.getFavouriteSections();
 		List<Tag> favouriteTags = dao.getFavouriteTags();
 		
 		if (favouriteSections.isEmpty() && favouriteTags.isEmpty()) {
 			Log.i(TAG, "Using top stories article set as favourites are empty");
 			return new TopStoriesArticleSet();
 		} else {
 			return new FavouriteStoriesArticleSet(favouriteSections, favouriteTags);
 		}		
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, "Favourites");
 	    menu.add(0, 2, 0, "Sections");
 	    menu.add(0, 6, 0, "About");
 	    menu.add(0, 4, 0, "Settings");
 	    menu.add(0, 3, 0, "Offline");
 	    menu.add(0, 5, 0, "Refresh");
 	    return true;
 	}
 	
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {	   
 	    case 1: 	    	
 	    	switchToFavourites();
 	    	return true;	 
 	    case 2:
 	    	switchToSections();
 	    	return true;	 
 	    case 3: 	    	
 	    	swichToSync();
 	        return true;
 	    case 4:
 	    	switchToPreferences();
 	    	return true;
 	    case 5:
 			refresh(true);
 			return true;	
 	    case 6:
 	    	showAbout();
 	    }
 	    return false;
 	}
 
 
 	private void showAbout() {
 		Dialog dialog = new Dialog(this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.about_dialog);
 		dialog.show();
 		ImageView heading = (ImageView) dialog.findViewById(R.id.KingsPlace);
 		heading.setImageResource(R.drawable.kingsplace);
 		
 		ImageView image = (ImageView) dialog.findViewById(R.id.GuardianLogo);
 		image.setImageResource(R.drawable.poweredbyguardian);		
 		dialog.setTitle(null);
 	}
 	
 }
