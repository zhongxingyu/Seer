 package com.mobilewiki;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Html.ImageGetter;
 import android.text.Spanned;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private TextView article_text_view;
 	private ImageGetter imgGetter = new ImageGetter() {
 		public Drawable getDrawable(String source) {
 			Drawable drawable = getResources().getDrawable(
 					R.drawable.borat);
 			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
 					drawable.getIntrinsicHeight());
 			return drawable;
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState); 
 		setContentView(R.layout.activity_main); 
 		article_text_view = (TextView) findViewById(R.id.article_content);
 	}
 
 	@Override
 	public void onStart() {  
 		super.onStart();
 		final EditText searchPhrase = (EditText) findViewById(R.id.search_text);
 

 		Bundle bundle = getIntent().getExtras();
 		int article_id = 1;
 		
  		if (bundle != null) {
			if (bundle.getString("article_id") != null) {
				article_id = Integer.parseInt(bundle.getString("article_id").toString());
 			}
 		}
 		
		
		article_text_view.setMovementMethod(new ScrollingMovementMethod());		

		RequestHandler request_handler = RequestHandler.getInstance();	

		
 		List<Integer> content_ids = request_handler.getContentIdsforArticleId(article_id);
		String title = request_handler.getTitleForArticleId(article_id);
 		String content = request_handler.getContentForContentId(content_ids.get(0));
 		
		title = IHTMLConstants.HTML_START_TITLE_TAG + title + IHTMLConstants.HTML_END_TITLE_TAG;

 		String total = title + content;
 		Log.e("Raw", total);
 		article_text_view.setText(Html.fromHtml(total));
 
 		ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
 		searchButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this,
 						SearchActivity.class);
 				Bundle parameters = new Bundle(1);
 				parameters.putString("SEARCH_PHRASE", searchPhrase.getText()
 						.toString());
 				intent.putExtras(parameters);
 				startActivity(intent);
 			}
 		});
 
 		searchButton.requestFocus();
 
 		setKeyboardSearchButtonListener();
 	}
 
 	private void setKeyboardSearchButtonListener() {
 		EditText searchText = (EditText) findViewById(R.id.search_text);
 		final ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
 		if (null != searchText) {
 			searchText
 					.setOnEditorActionListener(new EditText.OnEditorActionListener() {
 						@Override
 						public boolean onEditorAction(TextView v, int actionId,
 								KeyEvent event) {
 							if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 								searchButton.performClick();
 								return true;
 							}
 							return false;
 						}
 					});
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.edit_article:
 			intent = new Intent(MainActivity.this, EditorActivity.class);
 			Bundle parameters = new Bundle(1);
 			
 			String htmlString = Html.toHtml((Spanned) article_text_view.getText());
 			Log.e("Raw what is given to edit: ", htmlString);
 			parameters.putString("content", htmlString);
 			
 			intent.putExtras(parameters);
 			startActivity(intent);
 			return true;
 		case R.id.about:
             intent = new Intent(MainActivity.this, AboutActivity.class);
             startActivity(intent);
             return true;
 		case R.id.exit_app:
             super.finish();
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
