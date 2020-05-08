 package nz.gen.wellington.guardian.android.activities.widgets;
 
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.activities.favourites;
 import nz.gen.wellington.guardian.android.factories.ArticleSetFactory;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.Tag;
 import nz.gen.wellington.guardian.android.usersettings.FavouriteSectionsAndTagsDAO;
 import android.app.Activity;
 
 public class favouritewidget extends WidgetClickthroughActivity {
 	
 	@Override
 	protected ArticleSet getArticleSet() {	
 		FavouriteSectionsAndTagsDAO favouriteSectionAndTagsDAO = SingletonFactory.getFavouriteSectionsAndTagsDAO(this.getApplicationContext());		
 		List<Section> favouriteSections = favouriteSectionAndTagsDAO.getFavouriteSections();
 		List<Tag> favouriteTags = favouriteSectionAndTagsDAO.getFavouriteTags();
 		
		if (!favouriteSections.isEmpty() || !favouriteTags.isEmpty()) {
			ArticleSetFactory articleSetFactory = SingletonFactory.getArticleSetFactory(this.getApplicationContext());
			return articleSetFactory.getFavouritesArticleSetFor(favouriteSections, favouriteTags);
		}
		return null;
 	}
 
 	@Override
 	protected Class<? extends Activity> getDefaultActivity() {
 		return favourites.class;
 	}
 
 }
