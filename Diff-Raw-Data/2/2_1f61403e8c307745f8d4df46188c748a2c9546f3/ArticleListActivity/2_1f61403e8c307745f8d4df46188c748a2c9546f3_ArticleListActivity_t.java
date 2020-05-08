 package br.ufrn.dimap.pubshare.activity;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import br.ufrn.dimap.pubshare.adapters.ArticleListAdapter;
 import br.ufrn.dimap.pubshare.domain.Article;
 import br.ufrn.dimap.pubshare.mocks.ArticleMockFactory;
 import br.ufrn.dimap.pubshare.service.DownloaderService;
 
 /**
  * Responsible for managing the activity of displaying articles available.
  * 
  * @author Lucas Farias de Oliveira <i>luksrn@gmail.com</i>
  */
 public class ArticleListActivity extends Activity {
 	
 	private static final String TAG = ArticleListActivity.class.getSimpleName();
 
 	private ListView articlesListView;
 	private ArticleListAdapter adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);				
 		setContentView(R.layout.activity_article_list);
 		
 		List<Article> articles = ArticleMockFactory.makeArticleList();		
 		
 		configureListView(articles);		
 	}
 
 	private void configureListView(List<Article> articles) {
 		adapter = new ArticleListAdapter(this, R.layout.row_listview_article_list , articles);
 		
 		articlesListView = (ListView) findViewById(R.id.list_view_articles);
 		if ( articlesListView == null ){
 			Log.d(this.getClass().getSimpleName(), "Não foi possível encontrar R.layout.row_listview_article_list");
 		}
 		articlesListView.setAdapter( adapter );
 		registerForContextMenu(articlesListView);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {	
 		super.onCreateContextMenu(menu, v, menuInfo);
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu_long_press_article_list, menu);
 
	    MenuItem menuItem = (MenuItem)menu.findItem(R.id.contextual_menu_delete);
	    menuItem.setVisible(false);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		switch (item.getItemId()) {
 			case R.id.contextual_menu_view:
 				// view
 				return true;
 			case R.id.contextual_menu_download:
 				Intent intent = new Intent(this,  DownloaderService.class );		
 				Article selectedArticle =  ArticleMockFactory.singleArticle();
 				intent.putExtra( Article.KEY_INSTANCE , selectedArticle );
 				startService(intent);				
 				return true;
 			case R.id.contextual_menu_share:
 				// share
 				return true;
 		
 			default:
 				return super.onContextItemSelected(item);
 		}
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch ( item.getItemId() ) {
 			case R.id.menu_my_downloads:
 				showMyDownloadActivity();
 				return true;	
 			default:
 		        return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void showMyDownloadActivity(){
 		Log.d(TAG, "Display My Downloads Activity");
 		
 		Intent intent = new Intent(this, ArticlesDownloadedActivity.class);		
 		startActivity(intent);
 	}
  
 }
