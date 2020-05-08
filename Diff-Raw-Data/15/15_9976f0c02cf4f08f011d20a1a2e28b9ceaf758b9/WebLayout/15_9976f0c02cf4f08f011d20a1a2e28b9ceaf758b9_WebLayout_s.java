 package cstdr.ningningcat.widget.layout;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.text.InputType;
 import android.view.LayoutInflater;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import cstdr.ningningcat.R;
 import cstdr.ningningcat.widget.DRAutoCompleteTextView;
 import cstdr.ningningcat.widget.DRWebView;
 
 /**
  * 浏览网页的主Layout
  * 
  * @author cstdingran@gmail.com
  */
 public class WebLayout extends DRRelativeLayout {
 
 	private DRWebView webview;
 
 	private RelativeLayout navLayout;
 
 	/** 添加收藏按钮 **/
 	private ImageView add;
 
 	private RelativeLayout websiteLayout;
 
 	/** 输入框 **/
 	private DRAutoCompleteTextView website;
 
 	/** 重写按钮 **/
 	private ImageView rewrite;
 
 	/** 网页加载进度条 **/
 	private ProgressBar progress;
 
 	private RelativeLayout.LayoutParams webviewLP;
 
 	private RelativeLayout.LayoutParams navLayoutLP;
 
 	private RelativeLayout.LayoutParams addLP;
 
 	private RelativeLayout.LayoutParams websiteLayoutLP;
 
 	private RelativeLayout.LayoutParams websiteLP;
 
 	private RelativeLayout.LayoutParams rewriteLP;
 
 	private final int ID_ADD = 1304010025;
 
 	private final int ID_WEBSITE_LAYOUT = 1304010027;
 
 	private final int ID_WEBSITE = 1304010028;
 
 	private final int ID_REWRITE = 1304010030;
 
 	public WebLayout(Context context) {
 		super(context);
 		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 		initWebView();
 		initNavLayout();
 		initAdd();
 		initWebsiteLayout();
 		initWebsite();
 		initRewrite();
 		initProgress();
 	}
 
 	private void initWebView() {
 		webviewLP = new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT);
 		webview = new DRWebView(mContext);
 		webview.setLayoutParams(webviewLP);
 		this.addView(webview);
 	}
 
 	private void initNavLayout() {
 		navLayoutLP = new LayoutParams(LayoutParams.MATCH_PARENT,
 				getIntScaleX(114));
 		navLayoutLP.addRule(ALIGN_PARENT_TOP);
 		navLayout = new RelativeLayout(mContext);
 		navLayout.setLayoutParams(navLayoutLP);
 		navLayout.setBackgroundResource(R.drawable.bg_navigation);
 		this.addView(navLayout);
 	}
 
 	private void initAdd() {
 		addLP = new LayoutParams(getIntScaleX(94), getIntScaleX(70));
 		addLP.addRule(CENTER_VERTICAL);
 		addLP.setMargins(0, 0, 0, 0);
 		add = new ImageView(mContext);
 		add.setId(ID_ADD);
 		add.setLayoutParams(addLP);
 		add.setImageResource(R.drawable.navigation_add_favorite);
 		navLayout.addView(add);
 	}
 
 	private void initWebsiteLayout() {
 		websiteLayoutLP = new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.WRAP_CONTENT);
		websiteLayoutLP.setMargins(0, 0, getIntScaleX(2), 0);
 		websiteLayoutLP.addRule(RIGHT_OF, ID_ADD);
		websiteLayoutLP.addRule(CENTER_VERTICAL);
 		websiteLayout = new RelativeLayout(mContext);
 		websiteLayout.setId(ID_WEBSITE_LAYOUT);
 		websiteLayout.setLayoutParams(websiteLayoutLP);
 		websiteLayout.setBackgroundResource(R.drawable.bg_website);
 		navLayout.addView(websiteLayout);
 	}
 
 	private void initWebsite() {
 		websiteLP = new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.WRAP_CONTENT);
 		websiteLP.setMargins(0, getIntScaleX(2), 0, 0);
 		websiteLP.addRule(ALIGN_PARENT_LEFT);
 		websiteLP.addRule(LEFT_OF, ID_REWRITE);
 		website = new DRAutoCompleteTextView(mContext);
 		website.setId(ID_WEBSITE);
 		website.setLayoutParams(websiteLP);
 		website.setBackgroundColor(Color.TRANSPARENT);
 		website.setHint(R.string.website_hint);
 		website.setSingleLine();
 		website.setTextSize(getIntScaleX(16));
 		website.setTextColor(Color.BLACK);
 		website.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
 		website.setHighlightColor(Color.parseColor("#0099CC"));
 		websiteLayout.addView(website);
 	}
 
 	private void initRewrite() {
 		rewriteLP = new LayoutParams(getIntScaleX(40), getIntScaleX(40));
 		rewriteLP.addRule(ALIGN_PARENT_RIGHT);
 		rewriteLP.addRule(CENTER_VERTICAL);
 		rewriteLP.setMargins(0, 0, getIntScaleX(4), 0);
 		rewrite = new ImageView(mContext);
 		rewrite.setId(ID_REWRITE);
 		rewrite.setLayoutParams(rewriteLP);
 		rewrite.setImageResource(R.drawable.navigation_rewrite);
 		websiteLayout.addView(rewrite);
 	}
 
 	private void initProgress() {
 		LayoutInflater inflater = LayoutInflater.from(mContext);
 		progress = (ProgressBar) inflater.inflate(
 				R.layout.progress_bar_horizontal, null);
 		// progress=(ProgressBar)findViewById(R.id.progress_bar_horizontal);
 		// 直接findView会报空指针
 		this.addView(progress);
 	}
 
 	public RelativeLayout getNavLayout() {
 		return navLayout;
 	}
 
 	public ImageView getAdd() {
 		return add;
 	}
 
 	public DRAutoCompleteTextView getWebsite() {
 		return website;
 	}
 
 	public ImageView getRewrite() {
 		return rewrite;
 	}
 
 	public DRWebView getWebview() {
 		return webview;
 	}
 
 	public void setProgress(int i) {
 		progress.setProgress(i);
 	}
 
 	public void setProgressVisibility(int i) {
 		progress.setVisibility(i);
 	}
 
 }
