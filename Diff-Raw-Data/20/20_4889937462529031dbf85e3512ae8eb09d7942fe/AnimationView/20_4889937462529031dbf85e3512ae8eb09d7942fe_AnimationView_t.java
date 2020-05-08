 package fi.testbed2.android.ui.view;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import com.google.inject.Inject;
 import com.googlecode.androidannotations.annotations.AfterInject;
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.EView;
 import com.jhlabs.map.Point2D;
 import fi.testbed2.android.app.Logger;
 import fi.testbed2.android.app.MainApplication;
 import fi.testbed2.android.ui.view.util.AnimationViewBoundsUtil;
 import fi.testbed2.android.ui.view.util.AnimationViewCanvasUtil;
 import fi.testbed2.android.ui.view.util.AnimationViewScaleAndGestureUtil;
 import fi.testbed2.domain.Municipality;
 import fi.testbed2.domain.TestbedMapImage;
 import fi.testbed2.domain.TestbedParsedPage;
 import fi.testbed2.service.BitmapService;
 import fi.testbed2.service.LocationService;
 import fi.testbed2.service.PageService;
 import fi.testbed2.service.SettingsService;
 import fi.testbed2.util.SeekBarUtil;
 import lombok.Getter;
 import lombok.Setter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * View which shows the map animation.
  */
 @EView
 public class AnimationView extends View {
 
     @Getter
     private List<Municipality> municipalities;
 
     @Setter
     private boolean allImagesDownloaded;
 
     @Getter
     private AnimationViewPlayer player;
 
     // Utils
     @Bean @Getter
     AnimationViewCanvasUtil canvasUtil;
     @Bean @Getter
     AnimationViewScaleAndGestureUtil scaleAndGestureUtil;
     @Bean @Getter
     AnimationViewBoundsUtil boundsUtil;
 
     // Scaling related
     private ScaleGestureDetector scaleDetector;
     private GestureDetector gestureDetector;
     @Getter @Setter
     private MapScaleInfo scaleInfo = new MapScaleInfo();
 
     // Views and texts
     TextView timestampView;
     SeekBar seekBar;
 
     @Setter
     private String downloadProgressText;
 
 
     // Services
 
     @Inject
     public LocationService userLocationService;
 
     @Inject
     public BitmapService bitmapService;
 
     @Inject
     public PageService pageService;
 
     @Inject
     public SettingsService settingsService;
 
     /**
      * All three constructors are needed!!
      */
 
 	public AnimationView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public AnimationView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public AnimationView(Context context) {
 		super(context);
 	}
 
     public void initView(Context context) {
 
         Logger.debug("Initializing AnimationView");
 
         player = new AnimationViewPlayer(this);
         scaleAndGestureUtil.setView(this);
         boundsUtil.setView(this);
 
         gestureDetector = new GestureDetector(context, scaleAndGestureUtil.getGestureListener());
         scaleDetector = new ScaleGestureDetector(context, scaleAndGestureUtil.getScaleListener());
 
         BitmapDrawable firstMap = new BitmapDrawable(bitmapService.getBitmap(getMapImagesToBeDrawn().get(0)));
 
         boundsUtil.setFrameWidth(firstMap.getMinimumWidth());
         boundsUtil.setFrameHeight(firstMap.getMinimumHeight());
 
         player.setFrameDelay(settingsService.getSavedFrameDelay());
         player.setCurrentFrame(0);
         player.setFrames(getMapImagesToBeDrawn().size() - 1);
 
     }
 
     public void startAnimation(TextView timestampView, SeekBar seekBar, Rect bounds, MapScaleInfo scaleInfo) {
         Logger.debug("Start animation");
         player.play();
         this.scaleInfo = scaleInfo;
         this.timestampView = timestampView;
         this.seekBar = seekBar;
         if (bounds==null) {
             boundsUtil.initializeBounds();
         } else {
             boundsUtil.setBounds(bounds);
         }
 		next();
 	}
 
     public void next() {
         postDelayed(new Runnable() {
             @Override
             public void run() {
                 if(player.isPlaying()) {
                     player.goToNextFrame();
                 }
             }
         }, player.getFrameDelay());
     }
 
 
     private List<TestbedMapImage> getMapImagesToBeDrawn() {
 
         if (allImagesDownloaded) {
             return pageService.getTestbedParsedPage().getAllTestbedImages();
         } else {
             List<TestbedMapImage> list = new ArrayList<TestbedMapImage>();
             TestbedParsedPage page = pageService.getTestbedParsedPage();
             list.add(page.getLatestTestbedImage());
             return list;
         }
 
     }
 
     @Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
         canvas.save();
         canvas.scale(scaleInfo.getScaleFactor(), scaleInfo.getScaleFactor(),
                 scaleInfo.getPivotX(), scaleInfo.getPivotY());
 
         TestbedMapImage currentMap = getMapImagesToBeDrawn().get(player.getCurrentFrame());
 
         updateTimestampView(currentMap);
         updateSeekBar();
 
         BitmapDrawable frame = new BitmapDrawable(bitmapService.getBitmap(currentMap));
         frame.setBounds(boundsUtil.getBounds());
         frame.draw(canvas);
 
         canvasUtil.drawMunicipalities(canvas, boundsUtil.getBounds(), municipalities);
         canvasUtil.drawUserLocation(canvas, boundsUtil.getBounds());
 
         canvas.restore();
 
     }
 
     private void updateTimestampView(TestbedMapImage currentMap) {
 
         String timestamp = currentMap.getLocalTimestamp();
         String text = String.format("%1$02d/%2$02d @ ",
                 player.getCurrentFrame() + 1 , player.getFrames() + 1) + timestamp;
 
         if (downloadProgressText!=null) {
             text="@ "+timestamp+"  "+downloadProgressText;
         }
 
         if (timestampView!=null) {
             timestampView.setText(text);
             timestampView.invalidate();
         }
 
     }
 
     private void updateSeekBar() {
         if (seekBar!=null) {
             seekBar.setProgress(SeekBarUtil.getSeekBarValueFromFrameNumber(player.getCurrentFrame(),
                     pageService.getTestbedParsedPage().getAllTestbedImages().size()));
         }
     }
 
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
         gestureDetector.onTouchEvent(event);
         scaleDetector.onTouchEvent(event);
         boundsUtil.calculateNewBounds(event);
 
         return true;
 	}
 
 
 
 
 
     /*
     * ============
     * Setters and getters
     * ============
     */
 
     public void updateBounds(Rect bounds) {
         if (bounds==null) {
             boundsUtil.initializeBounds();
         } else {
             boundsUtil.setBounds(bounds);
         }
     }
 
     public void setMunicipalities(List<Municipality> municipalities) {
         this.municipalities = municipalities;
         canvasUtil.setMunicipalitiesOnScreen(new HashMap<Municipality, Point2D.Double>());
     }
 
     @AfterInject
     void injectRoboGuiceDependencies() {
         MainApplication.getApplication().getInjector().injectMembers(this);
     }
 
 }
