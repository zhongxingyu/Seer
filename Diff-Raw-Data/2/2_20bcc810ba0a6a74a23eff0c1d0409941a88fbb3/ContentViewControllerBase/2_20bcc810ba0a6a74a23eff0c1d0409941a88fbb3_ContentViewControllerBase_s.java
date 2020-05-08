 package gov.va.ptsd.ptsdcoach.controllers;
 
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Picture;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.accessibility.AccessibilityManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.LinearInterpolator;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.openmhealth.ohmage.campaigns.va.ptsd_explorer.ContentScreenViewedEvent;
 import com.openmhealth.ohmage.campaigns.va.ptsd_explorer.TimePerScreenEvent;
 import com.openmhealth.ohmage.core.EventLog;
 
 import gov.va.ptsd.ptsdcoach.UserDBHelper;
 import gov.va.ptsd.ptsdcoach.Util;
 import gov.va.ptsd.ptsdcoach.activities.NavigationController;
 import gov.va.ptsd.ptsdcoach.content.Caption;
 import gov.va.ptsd.ptsdcoach.content.Content;
 import gov.va.ptsd.ptsdcoach.services.TtsContentProvider;
 
 import java.net.URLDecoder;
 import java.text.BreakIterator;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 abstract public class ContentViewControllerBase extends FrameLayout implements OnGestureListener {
 
 	static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_]*)\\}");
 	static final private String webviewFormat = 
 		"<html><head>"+
	    "<script type=\"text/javascript\" src=\"content://gov.va.ptsd.ptsdcoach.services.localjs/ideal-loader.js\"/>"+
 		"<style>body{background-color:transparent;color:#FFFFFF;font-family:\"Helvetica\";font-size:14px;}\na:link {color:#AAAAFF;}</style></head><body>%s</body></html>";
 
 	Content content;
 	Content selectedContent;
 	
 	int contentsToLoad = 0;
 	OnViewReadyListener listener;
 
 	String title;
 	String spokenText;
 	MediaPlayer audioPlayer;
 	long audioPlayerStartTime;
 	Caption[] captions;
 	CaptionPlayer captionPlayer;
 	CaptionView captionView;
 	WebView lastWebView;
 	boolean blocked = false;
 	long timeAppeared;
 	public int viewTypeID=2;
 	
 	class JSInterface {
 		public void listen() {
 			playAudio();
 		}
 	}
 
 	class JSTalkBackInterface {
 		public void deliverText(String text) {
 			Log.d("JSTalkBackInterface","deliverText: "+text);
 		}
 
 		public void speakInteractive(String text) {
 			Log.d("JSTalkBackInterface","speakInteractive: "+text);
 		}
 	}
 
 	class CaptionView extends TextView {
 		Paint bgPaint;
 
 		public CaptionView(Context ctx) {
 			super(ctx);
 			bgPaint = new Paint();
 			bgPaint.setColor(0xA0404040);
 			bgPaint.setStyle(Style.FILL);
 			setTextColor(0xFFFFFFFF);
 			setTextSize(18);
 			setGravity(Gravity.CENTER);
 			setPadding(30, 10, 30, 10);
 		}
 		
 		@Override
 		protected void onTextChanged(CharSequence text, int start, int before, int after) {
 			// TODO Auto-generated method stub
 			super.onTextChanged(text, start, before, after);
 		}
 		
 		@Override
 		protected void onDraw(Canvas canvas) {
 			RectF r = new RectF(10, 0, getWidth()-10, getHeight());
 			canvas.drawRoundRect(r, 10, 10, bgPaint);
 			super.onDraw(canvas);
 		}
 	}
 	
 	class CaptionPlayer implements Runnable {
 		Handler handler;
 		int captionIndex;
 		boolean shown;
 		boolean on;
 		String currentCaption;
 
 		public void start() {
 			handler = getHandler();
 			captionIndex = 0;
 			shown = false;
 			on = captionsAreOn();
 			
 			Caption c = captions[0];
 			handler.postAtTime(this, audioPlayerStartTime + c.startTime);
 		}
 		
 		@Override
 		public void run() {
 			if (!shown) {
 				Caption c = captions[captionIndex];
 				currentCaption = c.text;
 				if (on) {
 					// show it
 					getCaptionView().setText(currentCaption);
 					AlphaAnimation alpha = new AlphaAnimation(0, 1);
 					alpha.setInterpolator(new LinearInterpolator());
 					alpha.setDuration(500);
 					alpha.setFillAfter(true);
 					alpha.setFillBefore(true);
 					alpha.setFillEnabled(true);
 					getCaptionView().startAnimation(alpha);
 				}
 
 				shown = true;
 				handler.postAtTime(this, audioPlayerStartTime + c.endTime);
 			} else {
 				// hide it
 				if (on) {
 					AlphaAnimation alpha = new AlphaAnimation(1, 0);
 					alpha.setInterpolator(new LinearInterpolator());
 					alpha.setDuration(500);
 					alpha.setFillAfter(true);
 					alpha.setFillBefore(true);
 					alpha.setFillEnabled(true);
 					getCaptionView().startAnimation(alpha);
 				}
 
 				shown = false;
 				captionIndex++;
 				
 				//check if we are beyond our bounds yet..if so, stop this
 				if(captionIndex >= captions.length)
 				{
 					stop();
 				}
 				else
 				{		
 					Caption c = captions[captionIndex];
 					handler.postAtTime(this, audioPlayerStartTime + c.startTime);
 				}
 			}
 		}
 		
 		public void toggleOnOff() {
 			boolean newOn = captionsAreOn();
 			if (shown) {
 				if (newOn && !on) {
 					AlphaAnimation alpha = new AlphaAnimation(1, 1);
 					alpha.setInterpolator(new LinearInterpolator());
 					alpha.setDuration(0);
 					alpha.setFillAfter(true);
 					alpha.setFillBefore(true);
 					alpha.setFillEnabled(true);
 					getCaptionView().setText(currentCaption);
 					getCaptionView().startAnimation(alpha);
 				} else if (on && !newOn) {
 					if (captionView != null) {
 						AlphaAnimation alpha = new AlphaAnimation(0, 0);
 						alpha.setInterpolator(new LinearInterpolator());
 						alpha.setDuration(0);
 						alpha.setFillAfter(true);
 						alpha.setFillBefore(true);
 						alpha.setFillEnabled(true);
 						captionView.startAnimation(alpha);
 					}
 				}
 			}
 			
 			 on = newOn;
 		}
 		
 		public void stop() {
 			if (handler != null) {
 				handler.removeCallbacks(captionPlayer);
 				handler = null;
 			}
 		}
 	}	
 	
 	public interface OnViewReadyListener {
 		public void onViewReady();
 	}
 	
 	public ContentViewControllerBase(Context ctx) {
 		super(ctx);
 	}
 	
 	public View makeTitleView(String title) {
 		this.title = title;
 		TextView titleView = new TextView(getContext());
 		titleView.setText(title);
 		titleView.setTextColor(0xFFFFFFFF);
 		titleView.setShadowLayer(1, 1, 1, 0xFF000000);
 		titleView.setTextSize(24);
 		titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);
 		titleView.setGravity(Gravity.CENTER);
 		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		layout.setMargins(10,20,10,10);
 		layout.gravity = Gravity.CENTER;
 		return titleView;
 	}
 
 	@Override
     public Drawable getBackground() {
 		return Util.makeDrawable(getContext(), "table_bg_darker.png", true);
 	}
 
 	public boolean captionsAreOn() {
 		UserDBHelper db = UserDBHelper.instance(getContext());
 		String cc = db.getSetting("cc");
 		return "true".equals(cc);
 	}
 	
 	public void toggleCC() {
 		UserDBHelper db = UserDBHelper.instance(getContext());
 		String cc = db.getSetting("cc");
 		if ("true".equals(cc)) {
 			db.setSetting("cc", "false");
 		} else {
 			db.setSetting("cc", "true");
 		}
 		
 		if (captionPlayer != null) {
 			captionPlayer.toggleOnOff();
 		}
 	}
 
 	public void setOnViewReadyListener(OnViewReadyListener listener) {
 		this.listener = listener;
 		if (contentsToLoad == 0) listener.onViewReady();
 	}
 	
 	public NavigationController getNavigator() {
 		NavigationController nav = (NavigationController)getContext();
 		return nav;
 	}
 	
 	public void pushContent(Content c) {
 		getNavigator().pushViewForContent(c);
 	}
 
 	public boolean hasCaptions() {
 		return getCaptions().length > 0;
 	}
 	
 	public CaptionView getCaptionView() {
 		if (captionView == null) {
 			FrameLayout captionParent = new FrameLayout(getContext());
 			FrameLayout.LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 			params.bottomMargin = 60;
 			params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
 			captionView = new CaptionView(getContext());
 
 			captionParent.addView(captionView, params);
 			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 			addView(captionParent);
 			
 			AlphaAnimation alpha = new AlphaAnimation(0, 0);
 			alpha.setInterpolator(new LinearInterpolator());
 			alpha.setDuration(0);
 			captionView.startAnimation(alpha);
 		}
 		
 		return captionView;
 	}
 	
 	public Caption[] getCaptions() {
 		if (captions == null) {
 			captions = getContent().getCaptions();
 			Log.v("PTSD", "Caption count: " + captions.length);
 			
 		}
 		
 		return captions;
 	}
 	
 	public void stopAudio() {
 		TtsContentProvider.stopSpeech(this);
 
 		if (audioPlayer != null) {
 			audioPlayer.stop();
 			audioPlayer = null;
 		}
 
 		if (captionPlayer != null) {
 			captionPlayer.stop();
 			captionPlayer = null;
 		}
 	}
 	
 	public boolean playAudio() {
 		TtsContentProvider.stopSpeech(this);
 		if (audioPlayer != null) {
 			audioPlayer.stop();
 			audioPlayer = null;
 
 			if (captionPlayer != null) {
 				captionPlayer.stop();
 				captionPlayer = null;
 			}
 
 			return false;
 		}
 		
 		audioPlayer = new MediaPlayer();
 		audioPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
 			@Override
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 		});
 
 		AssetFileDescriptor fd = getContent().getAudio();
 		
 		if (fd == null) {
 			audioPlayer = null;
 			return false;
 		}
 		
 		try {
 			audioPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
 			fd.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		audioPlayer.setVolume(1, 1);
 		try {
 			audioPlayer.prepare();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if (hasCaptions()) {
 			captionPlayer = new CaptionPlayer();
 		}
 		
 		if (audioPlayer != null) {
 			audioPlayer.start();
 			audioPlayerStartTime = SystemClock.uptimeMillis();
 			if (captionPlayer != null) {
 				captionPlayer.start();
 			}
 			return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	protected void onDetachedFromWindow() {
 		stopAudio();
 		
 		long timeGone = System.currentTimeMillis();
 
 		{
 		    ContentScreenViewedEvent e = new ContentScreenViewedEvent();
 		    if(content != null) {
 		        e.id = content.uniqueID;
 		        e.name = content.getName();
 		        e.displayName = content.getDisplayName();
 		    }
 		    e.timestampStart = timeAppeared;
 		    e.timestampDismissal = timeGone;
 		    e.type = viewTypeID;
 		    EventLog.log(e);
 		}
 
 		{
 		    TimePerScreenEvent e = new TimePerScreenEvent();
 		    if(content != null)
 		        e.id = content.uniqueID;
 		    e.start = timeAppeared;
 		    e.time = timeGone - timeAppeared;
 		    EventLog.log(e);
 		}
 
 		super.onDetachedFromWindow();
 	}
 
 	public void onViewReady() {
 		AccessibilityManager mgr = (AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
 		if (mgr.isEnabled()) {
 			speakSpokenText();		
 		}
 		if (listener != null) {
 			listener.onViewReady();
 		}
 		
 	}
 
 	public void viewLoaded() {
 		if (contentsToLoad > 0) {
 			contentsToLoad--;
 			if (contentsToLoad == 0) {
 				onViewReady();
 			}
 		}
 	}
 	public void parentActivityPaused()
 	{
 		
 	}
 	
 	public static void fillWebViewWithContent(WebView wv, Content c) {
 		String html = String.format(webviewFormat, c.getMainText());
 		wv.loadDataWithBaseURL("file:///android_asset/",html, "text/html", "utf-8", null);
 	}
 
 	public String replaceVariables(String text) {
 		while (true) {
 			Matcher matcher = VARIABLE_PATTERN.matcher(text);
 			if (matcher.find()) {
 				String var = matcher.group(1);
 				String value = getNavigator().getVariable(var);
 				if (value == null) value = "(value of '"+var+"')";
 				int start = matcher.start();
 				int end = matcher.end();
 				text = text.substring(0, start) + value + text.substring(end);
 			} else {
 				break;
 			}
 		}
 		
 		return text;
 	}
 
 	public void speakBlock(String msg) {
 		msg = msg.replace("\r\n", " ");
 		
 		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
 		iterator.setText(msg);
 		int start = iterator.first();
 		
 		ArrayList<String> lines = new ArrayList<String>();
 		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
 			lines.add(msg.substring(start, end));
 		}
 
 		String _title = this.title;
 		if (_title != null) {
 			_title = _title.replace("PTSD","P T S D");
 			TtsContentProvider.speak(this, _title.trim(), TextToSpeech.QUEUE_ADD);
 		}
 
 		for (int i=0;i<lines.size();i++) {
 			String line = lines.get(i);
 			line = line.replace("PTSD","P T S D");
 			line = line.replace("VA ","V A ");
 			TtsContentProvider.speak(this, line.trim(), TextToSpeech.QUEUE_ADD);
 		}
 		Log.d("tts","done with queuing full page");
 	}
 	
 	public boolean trySpeech(String message) {
 		if (message.startsWith("deliverText:")) {			
 			int startAt = message.lastIndexOf('}');
 			if (startAt == -1) 
 				startAt = 12;
 			else
 				startAt++;
 			String msg = message.substring(startAt).trim();
 			speakBlock(msg);
 			return true;
 		} else if (message.startsWith("speak:")) {
 			String msg = message.substring(6).trim();
 			TtsContentProvider.stopSpeech(this);
 			TtsContentProvider.speak(this, msg, TextToSpeech.QUEUE_FLUSH);
 			return true;
 		}
 		
 		return false;
 	}
 	/*
 	static WebChromeClient wcc = new WebChromeClient() {
 		@Override
 		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
 			if (trySpeech())
 				return true;
 			}
 			return super.onJsAlert(view, url, message, result);
 		}
 	};
 */	
 	public WebView createWebView(String htmlBody) {
 		htmlBody = replaceVariables(htmlBody);
 		String html = String.format(webviewFormat, htmlBody);
 
 		final WebView wv = new WebView(getContext()) {
 			@Override
             public boolean dispatchPopulateAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {
 //				event.setContentDescription("some other text");
 				return false;
 			}
 		};
 		wv.setFocusable(false);
 		lastWebView = wv;
 
 		WebSettings webSettings = wv.getSettings();
 		webSettings.setNeedInitialFocus(false);
 		webSettings.setJavaScriptEnabled(true);
 //		webSettings.setAllowFileAccess(true);
 //		webSettings.setDatabaseEnabled(true);
 //		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
 //		webSettings.setPluginsEnabled(true);
 		
 		wv.setBackgroundColor(0x00000000);
 		wv.setHorizontalScrollBarEnabled(false);
 		wv.setVerticalScrollBarEnabled(false);
 
 		wv.addJavascriptInterface(new JSInterface(), "ptsdcoach");
 //		wv.setWebChromeClient(wcc);
 
 		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
 		layout.gravity = Gravity.CENTER;
 		wv.setLayoutParams(layout);
 
 		contentsToLoad++;
 
 		wv.setPictureListener(new WebView.PictureListener() {
 			@Override
 			public void onNewPicture(WebView view, Picture picture) {
 				int top = wv.getTop();
 				int bottom = wv.getBottom();
 				int left = wv.getLeft();
 				int right = wv.getRight();
 				viewLoaded();
 			}
 		});
 		
 		
 		final GestureDetector gestureDetector = new GestureDetector(getContext(), this);
 		wv.setOnTouchListener(new View.OnTouchListener() {
 			
 		    @Override
 		    public boolean onTouch(View v, MotionEvent event) {
 		    	return gestureDetector.onTouchEvent(event);
 		    }
 		});
 
 		wv.setWebViewClient(new WebViewClient() {
 
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				url = URLDecoder.decode(url);
 				return trySpeech(url);
 			}
 /*
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				super.onPageFinished(view, url);
 				viewLoaded = true;
 				if (listener != null) {
 					listener.onViewReady();
 				}
 			}
 */			
 		});
 	
 		String strippedHtml = html.replaceAll("<style>(.|\n)*?</style>", "");
 		strippedHtml = strippedHtml.replaceAll("<(.|\n)*?>", "");
 		addSpokenText(strippedHtml);
 		wv.loadDataWithBaseURL("file:///android_asset/",html, "text/html", "utf-8", null);
 
 		return wv;
 	}
 	
 	public void addSpokenText(String text) {
 		if (spokenText == null) spokenText = text;
 		else spokenText = spokenText + " " + text;
 	}
 	
 	public void speakSpokenText() {
 		stopAudio();
 		
 		if ((getContent() != null) && getContent().hasAudio()) {
 			if (playAudio()) return;
 		}
 		
 		if (spokenText != null) speakBlock(spokenText);
 	}
 	  
 	abstract public void build();
 	
 	public void buttonTapped(int id) {
 		getNavigator().buttonTapped(id);
 	}
 	
 	public void handleButtonTap(int id) {
 		// Do nothing
 	}
 
 	public void setContent(Content content) {
 		this.content = content;
 		build();
 	}
 	
 	public void setSelectedContent(Content selectedContent) {
 		this.selectedContent = selectedContent;
 	}	
 
 	public Content getContent() {
 		return content;
 	}
 
 	public void unblockInput() {
 		blocked = false;
 	}
 
 	public void blockInput() {
 		blocked = true;
 	}
 	
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		if (blocked) return true;
 		return super.dispatchTouchEvent(ev);
 	}
 	
 	@Override
 	public boolean onDown(MotionEvent e) {
 		return false;
 	}
 	
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		return true;
 	}
 	
 	@Override
 	public void onLongPress(MotionEvent e) {
 	}
 	
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		return true;
 	}
 	
 	@Override
 	public void onShowPress(MotionEvent e) {
 	}
 	
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	@Override
 	protected void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		timeAppeared = System.currentTimeMillis();
 		unblockInput();
 	}
 	
 	public String checkPrerequisites() {
 		return null;
 	}
 }
