 package eu.nanairo_reader.ui.activity;
 
 import static eu.nanairo_reader.ui.constant.NanairoUiConstant.ARTICLE;
 import static eu.nanairo_reader.ui.constant.NanairoUiConstant.SUBSCRIPTION;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import eu.nanairo_reader.R;
 import eu.nanairo_reader.business.bean.Article;
 import eu.nanairo_reader.business.bean.Subscription;
 import eu.nanairo_reader.business.service.ArticleListManager;
 import eu.nanairo_reader.ui.component.ArticleArrayAdapter;
 
 public class ArticleListActivity extends BaseActivity {
 	@Inject
 	ArticleListManager articleListManager;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.article_list);
 
 		// パラメータ取得
 		Intent intent = getIntent();
 		Subscription subscription = (Subscription) intent.getSerializableExtra(SUBSCRIPTION);
 
 		this.rssService.loadArticleList(subscription.getId());
 
 		// ListView
 		ListView listView = (ListView) findViewById(R.id.listView);
 		List<Article> list = this.articleListManager.getArticleList();
 		ListAdapter listAdapter = new ArticleArrayAdapter(getApplicationContext(), list);
 		listView.setAdapter(listAdapter);
 
 		// ListViewクリック
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				ListView listView = (ListView) findViewById(R.id.listView);
 				Article article = (Article) listView.getItemAtPosition(position);
 
 				// インテントのインスタンス生成
 				Intent intent = new Intent(ArticleListActivity.this, ArticleActivity.class);
 				intent.putExtra(ARTICLE, article);
 				startActivity(intent);
 			}
 		});
 
 		// 更新ボタン
 		Button updateButton = (Button) findViewById(R.id.updateButton);
 		updateButton.setOnClickListener(new UpdateButtonOnClickListener(subscription.getId()));
 
 		// 全既読ボタン
 		Button allCheckButton = (Button) findViewById(R.id.allCheckButton);
 		allCheckButton.setOnClickListener(new AllCheckButtonOnClickListener(subscription.getId()));
 	}
 
 	class UpdateButtonOnClickListener implements View.OnClickListener {
 		long subscriptionId;
 
 		UpdateButtonOnClickListener(long subscriptionId) {
 			this.subscriptionId = subscriptionId;
 		}
 
 		public void onClick(View v) {
 			new AsyncTask<Long, Void, Integer>() {
 				@Override
 				protected Integer doInBackground(Long... params) {
 					return rssService.storeArticle(params[0]);
 				}
 
 				@Override
 				protected void onPostExecute(Integer count) {
 					ListView listView = (ListView) findViewById(R.id.listView);
 					((ArticleArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
 					Toast.makeText(getApplicationContext(), "新規記事数:" + count, Toast.LENGTH_SHORT).show();
 				};
 			}.execute(subscriptionId);
 		}
 	};
 
 	class AllCheckButtonOnClickListener implements View.OnClickListener {
 		long subscriptionId;
 
 		AllCheckButtonOnClickListener(long subscriptionId) {
 			this.subscriptionId = subscriptionId;
 		}
 
 		public void onClick(View v) {
 			rssService.kidokuAll(this.subscriptionId);
 
 			Toast.makeText(ArticleListActivity.this, "全て既読にしました。", Toast.LENGTH_SHORT).show();
 
 			// 次のsubscriptionIdを呼ぶ。
 			boolean isLoaded = rssService.loadArticleListByNext(this.subscriptionId);
 
			// ロードできない場合、画面戻る
			if (!isLoaded) {
				finish();
			}
 
 			// 購読の再表示
 			ListView listView = (ListView) findViewById(R.id.listView);
 			((ArticleArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
 		}
 	};
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// TODO StartActivityForResult
 		// 購読の再表示
 		ListView listView = (ListView) findViewById(R.id.listView);
 		((ArticleArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
 	}
 }
