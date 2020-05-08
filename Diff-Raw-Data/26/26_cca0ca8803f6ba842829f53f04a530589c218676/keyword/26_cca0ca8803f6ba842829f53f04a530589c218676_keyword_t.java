 package nz.gen.wellington.guardian.android.activities;
 
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.KeywordArticleSet;
 import nz.gen.wellington.guardian.android.model.Tag;
 import nz.gen.wellington.guardian.android.sqllite.DataHelper;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListAdapter;
 
 public class keyword extends ArticleListActivity {
 
 	private static final String TAG = "keyword";
 
 	ListAdapter adapter;
 	Tag keyword;
 	MenuItem favouriteMenuItem;
 		
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);	
 		keyword = (Tag) this.getIntent().getExtras().get("keyword");		
 		if (keyword.getSection() != null) {
 			setHeading(keyword.getSection().getName() + " - " + keyword.getName());
 			setHeadingColour(keyword.getSection().getColour());
 		} else {
 			setHeading(keyword.getName());
 		}	
 
 		updateArticlesHandler = new UpdateArticlesHandler(this, getArticleSet());
 	}
 	
	protected String getRefinementDescription() {
		return "Articles tagged with " + getArticleSet().getName() + " have also been tagged with:";
	}
	
 	protected ArticleSet getArticleSet() {
 		return new KeywordArticleSet(keyword);
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, "Home");	
 		menu.add(0, 5, 0, "Refresh");
 		
 		DataHelper dh = new DataHelper(this);
 		if (dh.isFavourite(keyword)) {
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
 		if (!dh.isFavourite(keyword)) {
 			Log.i(TAG, "Adding current tag to favourites: " + keyword.getName());
 			dh.addTag(keyword);
 			favouriteMenuItem.setTitle("Remove Favourite");			
 	
 		} else {
 			Log.i(TAG, "Removing current tag from favourites: " + keyword.getName());			
 			dh.removeTag(keyword);
 			favouriteMenuItem.setTitle("Add to Favourites");
 		}
 		dh.close();	
 	}
 	
 }
