 package net.acuttone.reddimg.views;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import net.acuttone.reddimg.R;
 import net.acuttone.reddimg.core.ReddimgApp;
 import net.acuttone.reddimg.core.RedditClient;
 import net.acuttone.reddimg.core.RedditLink;
 import net.acuttone.reddimg.prefs.PrefsActivity;
 import net.acuttone.reddimg.utils.GifDecoder;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.SpannableStringBuilder;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.RelativeSizeSpan;
 import android.text.style.StyleSpan;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.TranslateAnimation;
 import android.widget.FrameLayout;
 import android.widget.ImageSwitcher;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ViewSwitcher.ViewFactory;
 
 public class LinkViewerActivity extends Activity {
 
 	private static final String TUTORIAL_SHOWN = "TUTORIAL_SHOWN";
 	public static final String LINK_INDEX = "LINK_INDEX";
 	private int currentLinkIndex;
 	private RedditLink currentLink;
 	private ImageSwitcher switcher;
 	private TextView textViewTitle;
 	private ImageView viewUpvote;
 	private ImageView viewDownvote;
 	private AsyncTask<Integer, RedditLink, Object []> loadTask;
 	private AsyncTask<GifDecoder, Bitmap, Void> loadGifTask;
 	private TranslateAnimation animToLeft1;
 	private TranslateAnimation animToLeft2;
 	private TranslateAnimation animToRight1;
 	private TranslateAnimation animToRight2;
 	private GestureDetector gestureDetector;
 	private View progressBar;
 	private TextView textviewLoading;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.linkviewer);
 		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
 		progressBar = findViewById(R.id.progressbar_linkviewer);
 		textviewLoading = (TextView) findViewById(R.id.textViewLoading);
 		switcher = (ImageSwitcher) findViewById(R.id.image_switcher);
 		switcher.setFactory(new ViewFactory() {
 			
 			@Override
 			public View makeView() {
 				ImageView i = new ImageView(LinkViewerActivity.this);  
 				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
 				i.setAdjustViewBounds(true);
 				i.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 				return i;
 			}
 		});
 		animToLeft1 = new TranslateAnimation(1000, 0, 0, 0);
 		animToLeft1.setDuration(500);
 		animToLeft2 = new TranslateAnimation(0, -1000, 0, 0);
 		animToLeft2.setDuration(500);
 		animToRight1 = new TranslateAnimation(0, 1000, 0, 0);
 		animToRight1.setDuration(500);
 		animToRight2 = new TranslateAnimation(-1000, 0, 0, 0);
 		animToRight2.setDuration(500);
 		viewUpvote = (ImageView) findViewById(R.id.imageupvote);
 		viewUpvote.setVisibility(View.GONE);
 		viewDownvote = (ImageView) findViewById(R.id.imagedownvote);
 		viewDownvote.setVisibility(View.GONE);
 		
 		if(savedInstanceState != null && savedInstanceState.containsKey(LINK_INDEX)) {
 			currentLinkIndex = savedInstanceState.getInt(LINK_INDEX);
 		} else {
 			currentLinkIndex = getIntent().getExtras().getInt(LINK_INDEX);
 		}
 		gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
 			
 			@Override
 			public boolean onDoubleTap(MotionEvent e) {
 				Intent i = new Intent(getApplicationContext(), FullImageActivity.class);
 				String imageDiskPath = ReddimgApp.instance().getImageCache().getImageDiskPath(currentLink.getUrl());
 				i.putExtra(FullImageActivity.IMAGE_NAME, imageDiskPath);
 				startActivity(i);
 				return super.onDoubleTapEvent(e);
 			}
 			
 			@Override
 			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 				float deltax = e1.getX() - e2.getX();
 				float deltay = e1.getY() - e2.getY();
 				if (Math.abs(deltax) > 50) {
 					if (deltax < 0) {
 						if(currentLinkIndex > 0) {
 							currentLinkIndex--;
 							switcher.setInAnimation(animToRight2);
 							switcher.setOutAnimation(animToRight1);
 							loadImage();
 						}
 					} else {
 						currentLinkIndex++;
 						switcher.setInAnimation(animToLeft1);
 						switcher.setOutAnimation(animToLeft2);
 						loadImage();
 					}
 				} else {
 					if (Math.abs(deltay) > 50) {
 						if (deltay < 0) {
 							ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.DOWNVOTE);
 						} else {
 							ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.UPVOTE);
 						}
 						refreshVoteIndicators();
 					}
 				}
 				return super.onFling(e1, e2, velocityX, velocityY);
 			}
 		});
 		
 		loadImage();
 		
 		if(ReddimgApp.instance().getPrefs().getBoolean(TUTORIAL_SHOWN, false) == false) {
 			showTutorial();
 		}
 	}
 	
 	private void showTutorial() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Tutorial");
 		builder.setMessage("Fling left and right to change image\n\nFling up and down to vote\n\nDouble tap to zoom")
 				.setCancelable(false).setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Editor editor = ReddimgApp.instance().getPrefs().edit();
 						editor.putBoolean(TUTORIAL_SHOWN, true);
 						editor.commit();
 						dialog.dismiss();
 					}
 				});
 		AlertDialog dlg = builder.create();
 		dlg.show();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureDetector.onTouchEvent(event);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(LINK_INDEX, currentLinkIndex);
 	}
 	
 	private void loadImage() {
 		if (loadTask != null) {
 			loadTask.cancel(true);
 		}
 		loadTask = new AsyncTask<Integer, RedditLink, Object[]>() {
 
 			@Override
 			protected void onPreExecute() {
 				super.onPreExecute();
 				if (loadGifTask != null) {
 					loadGifTask.cancel(true);
 				}
 				progressBar.setVisibility(View.VISIBLE);
 				textviewLoading.setText("Loading...");
 				textviewLoading.setVisibility(View.VISIBLE);
 			}
 
 			@Override
 			protected Object[] doInBackground(Integer... params) {
 				RedditLink redditLink = null;
 				while (redditLink == null) {
 					try {
 						redditLink = ReddimgApp.instance().getLinksQueue().get(params[0]);
 						if(redditLink == null) {
 							try { Thread.sleep(300); } catch (InterruptedException e) { }
 						}
 					} catch (IOException e) {
 						Log.e(ReddimgApp.APP_NAME, e.toString());
 					}
 				}
 				publishProgress(redditLink);
 				Bitmap bitmap = ReddimgApp.instance().getImageCache().getImage(redditLink.getUrl());
 				if (isGif(redditLink)) {
 					InputStream stream;
 					GifDecoder dec = null;
 					try {
 						stream = new BufferedInputStream(new FileInputStream(ReddimgApp.instance().getImageCache()
 								.getImageDiskPath(redditLink.getUrl())));
 						dec = new GifDecoder();
 						dec.read(stream);
 					} catch (FileNotFoundException e) {
 						Log.e(ReddimgApp.APP_NAME, e.toString());
 					}
 					return new Object[] { redditLink, dec };
 				} else {
 					return new Object[] { redditLink, bitmap };
 				}
 			}
 
 			private boolean isGif(RedditLink link) {
 				return link.getUrl().endsWith("gif");
 			}
 			
 			@Override
 			protected void onProgressUpdate(RedditLink... values) {
 				super.onProgressUpdate(values);
 				textviewLoading.setText("Loading " + values[0].getUrl());
 			}
 
 			@Override
 			protected void onPostExecute(Object[] result) {
 				super.onPostExecute(result);
 				currentLink = (RedditLink) result[0];
 				progressBar.setVisibility(View.GONE);
 				textviewLoading.setVisibility(View.GONE);
 				refreshVoteIndicators();
 				updateTitle();
 				if(result[1] == null) {
 					switcher.setImageDrawable(null);
 					textviewLoading.setVisibility(View.VISIBLE);
 					textviewLoading.setText("Error loading " + currentLink.getUrl());
 					return;
 				}
 				if (isGif(currentLink)) {
 					loadGifTask = new AsyncTask<GifDecoder, Bitmap, Void>() {
 
 						protected void onPreExecute() {
 							switcher.setInAnimation(null);
 							switcher.setOutAnimation(null);
 						}
 
 						@Override
 						protected Void doInBackground(GifDecoder... arg0) {
 							GifDecoder dec = arg0[0];
 							final int n = dec.getFrameCount();
 							int i = 0;
 							while (isCancelled() == false) {
 								Bitmap bmp = dec.getFrame(i);
 								int t = dec.getDelay(i);
 								publishProgress(bmp);
 								try {
 									Thread.sleep(t);
 								} catch (InterruptedException e) {
 								}
 								i++;
 								if (i == n) {
 									i = 0;
 								}
 							}
 							return null;
 						}
 
 						protected void onProgressUpdate(Bitmap... values) {
 							switcher.setImageDrawable(new BitmapDrawable(values[0]));
 						}
 					};
 					loadGifTask.execute((GifDecoder) result[1]);
 				} else {
 					switcher.setImageDrawable(new BitmapDrawable((Bitmap) result[1]));
 				}
 			}
 		};
 		loadTask.execute(currentLinkIndex);
 	}
 
 	private void refreshVoteIndicators() {
 		if(RedditClient.UPVOTE.equals(currentLink.getVoteStatus())) {
 			viewUpvote.setVisibility(View.VISIBLE);
 			viewDownvote.setVisibility(View.GONE);
 		} else if(RedditClient.DOWNVOTE.equals(currentLink.getVoteStatus())) {
 			viewUpvote.setVisibility(View.GONE);
 			viewDownvote.setVisibility(View.VISIBLE);
 		} else {
 			viewUpvote.setVisibility(View.GONE);
 			viewDownvote.setVisibility(View.GONE);
 		}
 	}
 
 	public void updateTitle() {
 		SharedPreferences sp = ReddimgApp.instance().getPrefs();
 		int size = Integer.parseInt(sp.getString(PrefsActivity.TITLE_SIZE_KEY, "24"));
 		textViewTitle.setTextSize(size);
 		SpannableStringBuilder builder = new SpannableStringBuilder();
 		if (sp.getBoolean("showScore", true)) {
 			SpannableString score = new SpannableString("[" + currentLink.getScore() + "] ");
 			score.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, score.length(), 0);
 			score.setSpan(new RelativeSizeSpan(0.8f), 0, score.length(), 0);
 			builder.append(score);
 		}
 		SpannableString title = new SpannableString(currentLink.getTitle());
 		title.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length(), 0);
 		builder.append(title);
 		if (sp.getBoolean("showAuthor", true)) {
 			SpannableString author = new SpannableString(" by " + currentLink.getAuthor());
 			author.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, author.length(), 0);
 			author.setSpan(new StyleSpan(Typeface.ITALIC), 0, author.length(), 0);
 			author.setSpan(new RelativeSizeSpan(0.8f), 0, author.length(), 0);
 			builder.append(author);
 		}
 		if (sp.getBoolean("showSubreddit", true)) {
 			SpannableString sub = new SpannableString(" in " + currentLink.getSubreddit());
 			sub.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, sub.length(), 0);
 			sub.setSpan(new StyleSpan(Typeface.ITALIC), 0, sub.length(), 0);
 			sub.setSpan(new RelativeSizeSpan(0.8f), 0, sub.length(), 0);
 			builder.append(sub);
 		}
 		textViewTitle.setText(builder);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.menu_linkviewer, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = null;
 		switch (item.getItemId()) {
 		case R.id.menuitem_share:
 			intent = new Intent();
 			intent.setAction(Intent.ACTION_SEND);
 			intent.setType("text/plain");
 			intent.putExtra(Intent.EXTRA_TEXT, currentLink.getUrl());
 			startActivity(Intent.createChooser(intent, "Share via"));
 			return true;
 		case R.id.menuitem_opencomments:
 			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentLink.getCommentUrl() + ".compact"));
 			startActivity(intent);
 			return true;
 		case R.id.menuitem_tutorial:
 			showTutorial();
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 }
