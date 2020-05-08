 package fi.testbed2.view;
 
 import android.content.Context;
 import android.graphics.*;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.view.*;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.jhlabs.map.Point2D;
 import com.larvalabs.svgandroid.SVGParser;
 import fi.testbed2.R;
 import fi.testbed2.app.Logging;
 import fi.testbed2.data.Municipality;
 import fi.testbed2.data.TestbedMapImage;
 import fi.testbed2.service.BitmapService;
 import fi.testbed2.service.LocationService;
 import fi.testbed2.service.PageService;
 import fi.testbed2.service.PreferenceService;
 import fi.testbed2.util.SeekBarUtil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * View which shows the map animation.
  */
 public class AnimationView extends View {
 
     /**
      * Original map image dimensions.
      */
     public static final double MAP_IMAGE_ORIG_WIDTH = 600d;
     public static final double MAP_IMAGE_ORIG_HEIGHT = 508d;
 
     /**
      * Defines how many pixels the user is allowed to click
      * off the municipality point. If the touch is withing this
      * threshold, the municipality info is shown.
      */
     public static final float MUNICIPALITY_SEARCH_THRESHOLD = 15.0f;
 
     /**
      * Used for bounds calculation
      */
     private static final float GESTURE_THRESHOLD_DIP = 16.0f;
 
     // Scaling related
     private ScaleGestureDetector mScaleDetector;
     private GestureDetector mGestureDetector;
     private float scaleFactor = 1.0f;
    private float scalePivotX = 0.0f;
    private float scalePivotY = 0.0f;
     private float minScaleFactor = 0.5f;
     private float maxScaleFactor = 3.0f;
     private float scaleStepWhenDoubleTapping = 1.3f;
     private boolean mapWasScaled;
 
     // Animation frame related
     private int frameWidth;
     private int frameHeight;
 
     // Bounds calculation related
     private Rect bounds;
     private float boundsStartY;
     private float boundsStartX;
     private float boundsDistanceY;
 	private float boundsDistanceX;
 	private boolean boundsMoveMap;
 
     // Views and texts
 	private TextView timestampView;
     private SeekBar seekBar;
     private String downloadProgressText;
 
     // Marker image caching
     private Picture markerImage;
     private Picture pointImage;
     private Toast municipalityToast;
 
     // Data
     private List<Municipality> municipalities;
     private Map<Municipality, Point2D.Double> municipalitiesOnScreen;
 
     // Services
     public LocationService userLocationService;
     public BitmapService bitmapService;
     public PageService pageService;
     public PreferenceService preferenceService;
 
     private boolean allImagesDownloaded;
 
     private AnimationViewPlayer player;
 
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
 
         Logging.debug("Initializing AnimationView");
 
         player = new AnimationViewPlayer(this);
 
         mGestureDetector = new GestureDetector(context, new GestureListener());
         mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
 
         BitmapDrawable firstMap = new BitmapDrawable(bitmapService.getBitmap(getMapImagesToBeDrawn().get(0)));
 
         frameWidth = firstMap.getMinimumWidth();
         frameHeight = firstMap.getMinimumHeight();
 
         player.setFrameDelay(preferenceService.getSavedFrameDelay());
         player.setCurrentFrame(0);
         player.setFrames(getMapImagesToBeDrawn().size() - 1);
 
     }
 
     public void startAnimation(TextView timestampView, SeekBar seekBar, Rect bounds, float scale) {
         Logging.debug("Start animation");
         player.play();
         this.scaleFactor = scale;
         this.timestampView = timestampView;
         this.seekBar = seekBar;
         if (bounds==null) {
             initializeBounds();
         } else {
             this.bounds = bounds;
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
 
     private void initializeBounds() {
 
         // scale bounds
         int measuredHeight = getMeasuredHeight();
         int measuredWidth = getMeasuredWidth();
 
         Logging.debug("Initializing bounds...");
         Logging.debug("measuredHeight: "+measuredHeight);
         Logging.debug("measuredWidth: "+measuredWidth);
 
         if(measuredHeight > measuredWidth) {
             int scaleBy = measuredHeight-frameHeight;
             if(scaleBy > 0)
                 bounds = new Rect(0, 0, frameWidth + scaleBy, frameHeight + scaleBy);
             else
                 bounds = new Rect(0, 0, frameWidth, frameHeight);
         } else {
             int scaleBy = measuredWidth-frameWidth;
             if(scaleBy > 0)
                 bounds = new Rect(0, 0, frameWidth + scaleBy, frameHeight + scaleBy);
             else
                 bounds = new Rect(0, 0, frameWidth, frameHeight);
         }
     }
 
     private List<TestbedMapImage> getMapImagesToBeDrawn() {
 
         if (allImagesDownloaded) {
             return pageService.getTestbedParsedPage().getAllTestbedImages();
         } else {
             List<TestbedMapImage> list = new ArrayList<TestbedMapImage>();
             list.add(pageService.getTestbedParsedPage().getLatestTestbedImage());
             return list;
         }
 
     }
 
     @Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
         canvas.save();
        canvas.scale(scaleFactor, scaleFactor, scalePivotX, scalePivotY);
 
         TestbedMapImage currentMap = getMapImagesToBeDrawn().get(player.getCurrentFrame());
 
         updateTimestampView(currentMap);
         updateSeekBar();
 
         BitmapDrawable frame = new BitmapDrawable(bitmapService.getBitmap(currentMap));
         frame.setBounds(bounds);
         frame.draw(canvas);
 
         drawMunicipalities(canvas);
         drawUserLocation(canvas);
 
         canvas.restore();
 
     }
 
     private void updateTimestampView(TestbedMapImage currentMap) {
 
         String timestamp = currentMap.getTimestamp();
         String text = String.format("%1$02d/%2$02d @ ",
                 player.getCurrentFrame() + 1 , player.getFrames() + 1) + timestamp;
 
         if (downloadProgressText!=null) {
             text="@ "+timestamp+"  "+downloadProgressText;
         }
 
         timestampView.setText(text);
         timestampView.invalidate();
 
     }
 
     private void updateSeekBar() {
         seekBar.setProgress(SeekBarUtil.getSeekBarValueFromFrameNumber(player.getCurrentFrame(),
                 pageService.getTestbedParsedPage().getAllTestbedImages().size()));
     }
 
     private void drawUserLocation(Canvas canvas) {
         Point2D.Double userLocation = userLocationService.getUserLocationXY();
         if (userLocation!=null) {
             drawPoint(userLocation, Color.BLACK, canvas, null);
         }
     }
 
     private void drawMunicipalities(Canvas canvas) {
 
         for (Municipality municipality : municipalities) {
             if (municipality!=null) {
                 drawPoint(municipality.getXyPos(), Color.BLACK, canvas, municipality);
             }
         }
 
     }
 
     private void drawPoint(Point2D.Double point, int color, Canvas canvas, Municipality municipality) {
 
         Paint paint = new Paint();
         paint.setColor(color);
         paint.setAntiAlias(true);
         paint.setAlpha(200); // 0...255, 255 = no transparency
 
         double imgScaledWidth = bounds.width();
         double imgScaledHeight = bounds.height();
 
         double widthRatio = imgScaledWidth / MAP_IMAGE_ORIG_WIDTH;
         double heightRatio = imgScaledHeight / MAP_IMAGE_ORIG_HEIGHT;
 
         float xScaled = Double.valueOf(bounds.left + point.x*widthRatio).floatValue();
         float yScaled = Double.valueOf(bounds.top + point.y*heightRatio).floatValue();
 
         int xInt = Float.valueOf(xScaled).intValue();
         int yInt = Float.valueOf(yScaled).intValue();
 
         if (municipality==null) {
 
             Picture pic = getMarkerImage();
 
             int markerImageHeight = preferenceService.getMapMarkerSize();
 
             // Scale width a bit larger than original width (otherwise looks a bit too thin marker)
             float ratio = pic.getWidth()/pic.getHeight();
             int width = Float.valueOf(markerImageHeight*ratio+markerImageHeight/5).intValue();
             int height = markerImageHeight;
 
             /*
             * x, y coordinates are image's top left corner,
             * so position the marker to the bottom center
             */
             int left = xInt-width/2;
             int top = yInt-height;
             int right = xInt+width/2;
             int bottom = yInt;
 
             drawPicture(pic, canvas, new Rect(left,top,right,bottom));
 
         } else {
 
             // Save coordinates for info toast
             this.municipalitiesOnScreen.put(municipality,
                     new Point2D.Double(xInt*scaleFactor, yInt*scaleFactor));
 
             Picture pic = getPointImage();
 
             int circleDiameter = preferenceService.getMapPointSize();
 
             /*
             * x, y coordinates are image's top left corner,
             * so position the marker to the center
             */
 
             int left = xInt-circleDiameter/2;
             int top = yInt-circleDiameter/2;
             int right = xInt+circleDiameter/2;
             int bottom = yInt+circleDiameter/2;
 
             drawPicture(pic, canvas, new Rect(left, top, right, bottom));
         }
 
     }
 
     private void drawPicture(Picture pic, Canvas canvas, Rect rect) {
 
         try {
             canvas.drawPicture(pic, rect);
         } catch (Throwable e) {
             /*
              * Some user's seem to use Force GPU setting which does not support
              * the drawPicture method (throws java.lang.UnsupportedOperationException).
              * Do not show anything to those users but an alert dialog
              * that they should disabled Force GPU settings.
              */
         }
     }
 
     private Picture getMarkerImage() {
 
         if (markerImage==null) {
             String color = preferenceService.getMapMarkerColor();
             markerImage = SVGParser.getSVGFromString(new MapMarkerSVG(color).getXmlContent()).getPicture();
         }
         return markerImage;
 
     }
 
     private Picture getPointImage() {
 
         if (pointImage==null) {
             String color = preferenceService.getMapPointColor();
             pointImage = SVGParser.getSVGFromString(new MapPointSVG(color).getXmlContent()).getPicture();
         }
         return pointImage;
 
     }
 
     public void resetMarkerAndPointImageCache() {
         markerImage = null;
         pointImage = null;
     }
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
         mGestureDetector.onTouchEvent(event);
         mScaleDetector.onTouchEvent(event);
         calculateNewBounds(event);
 
 		return true;
 	}
 
 
     /**
      * This method is quite a mess. It is hard to understand how the bounds
      * are calculated. Do not touch this if you don't know what you are doing.
      *
      * @param event
      * @return
      */
     private boolean calculateNewBounds(MotionEvent event) {
 
         int action = event.getAction();
         int scaledWidth = Float.valueOf(getWidth()/ scaleFactor).intValue();
         int scaledHeight = Float.valueOf(getHeight()/ scaleFactor).intValue();
         Rect viewBounds = new Rect(0, 0, scaledWidth, scaledHeight);
 
         if (mapWasScaled && action==MotionEvent.ACTION_DOWN) {
             mapWasScaled = false;
         }
 
         if (mapWasScaled) {
             return true;
         }
 
         switch(action) {
             case MotionEvent.ACTION_CANCEL:
                 break;
             case MotionEvent.ACTION_UP:
                 boundsMoveMap = false;
                 break;
             case MotionEvent.ACTION_DOWN:
                 boundsStartY = event.getY();
                 boundsDistanceY = 0.0f;
                 boundsStartX = event.getX();
                 boundsDistanceX = 0.0f;
                 break;
             case MotionEvent.ACTION_MOVE:
                 boundsDistanceY = -(boundsStartY - event.getY());
                 boundsDistanceX = -(boundsStartX - event.getX());
 
                 Rect savedBounds = new Rect(bounds);
 
                 Logging.debug("Calculating new bounds...");
 
                 // Convert the dips to pixels
                 final float scale = getContext().getResources().getDisplayMetrics().density;
                 int mGestureThreshold = (int) (GESTURE_THRESHOLD_DIP * scale / scaleFactor + 0.5f);
 
                 if(!boundsMoveMap && (Math.abs(boundsDistanceY) > mGestureThreshold || Math.abs(boundsDistanceX) > mGestureThreshold)) {
                     boundsMoveMap = true;
                 }
 
                 if(boundsMoveMap) {
 
                     hideMunicipalityToast();
 
                     float mDistance_y_dip = boundsDistanceY * scale / scaleFactor + 0.5f;
                     float mDistance_x_dip = boundsDistanceX * scale / scaleFactor + 0.5f;
 
                     bounds.offset((int)mDistance_x_dip, (int)mDistance_y_dip);
 
                     Logging.debug("New bounds left: "+bounds.left);
                     Logging.debug("New bounds top: "+bounds.top);
                     Logging.debug("New bounds right: "+bounds.right);
                     Logging.debug("New bounds bottom: "+bounds.bottom);
 
                     // restart measuring distances
                     boundsStartY = event.getY();
                     boundsDistanceY = 0.0f;
                     boundsStartX = event.getX();
                     boundsDistanceX = 0.0f;
 
                     invalidate();
                 }
 
 
                 break;
             default:
                 return super.onTouchEvent(event);
 
         }
 
         return true;
     }
 
     /*
     * ============
     * Listeners for pinch zooming
     * ============
     */
 
     private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
         @Override
         public boolean onScale(ScaleGestureDetector detector) {

            scalePivotX = detector.getFocusX();
            scalePivotY = detector.getFocusY();
             scaleFactor *= detector.getScaleFactor();
 
             // Don't let the object get too small or too large.
             scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, maxScaleFactor));
 
             hideMunicipalityToast();
             mapWasScaled = true;
             invalidate();
             return true;
         }
     }
 
     private class GestureListener extends GestureDetector.SimpleOnGestureListener {
 
         @Override
         public boolean onDown(MotionEvent e) {
             return true;
         }
 
         @Override
         public boolean onDoubleTap(MotionEvent e) {
 
            scalePivotX = e.getX();
            scalePivotY = e.getY();
             scaleFactor *= scaleStepWhenDoubleTapping;
 
             if (scaleFactor >=maxScaleFactor) {
                 scaleFactor = 1.0f;
             }
 
             hideMunicipalityToast();
             mapWasScaled = true;
             invalidate();
             return true;
         }
 
         @Override
         public void onLongPress(MotionEvent e) {
             Logging.debug("Long pressed x: "+e.getX());
             Logging.debug("Long pressed y: "+e.getY());
             hideMunicipalityToast(); // always hide previous toast
             showInfoForMunicipality(e.getX(), e.getY());
         }
 
     }
 
     /**
      * Shows info toast about selected municipality if there is
      * a municipality close to the given x, y coordinates.
      *
      * @param x x coordinate
      * @param y y coordinate
      * @return True if the toast was shown, false if not shown.
      */
     private boolean showInfoForMunicipality(float x, float y) {
 
         for (Municipality municipality : municipalities) {
 
             if (municipality!=null) {
 
                 Point2D.Double pos = municipalitiesOnScreen.get(municipality);
 
                 if (pos!=null) {
 
                     Logging.debug("Pos: "+pos+", for: "+municipality.getName());
 
                     final float scale = getContext().getResources().getDisplayMetrics().density;
                     int threshold = (int) ((preferenceService.getMapPointSize()/2)*scale +
                                             (MUNICIPALITY_SEARCH_THRESHOLD/scaleFactor)*scale);
 
                     if (Math.abs(pos.x-x)<=threshold && Math.abs(pos.y-y)<=threshold) {
 
                         Logging.debug("Show info for municipality: "+municipality.getName());
 
                         LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
                         View layout = inflater.inflate(R.layout.toast_municipality,
                                 (ViewGroup) findViewById(R.id.toast_layout_root));
 
                         TextView text = (TextView) layout.findViewById(R.id.text);
                         text.setText(municipality.getName());
 
                         municipalityToast = new Toast(getContext());
                         municipalityToast.setGravity(Gravity.TOP| Gravity.LEFT,
                                 Double.valueOf(pos.x+20*scale).intValue(),
                                 Double.valueOf(pos.y-40*scale).intValue());
                         municipalityToast.setDuration(Toast.LENGTH_LONG);
                         municipalityToast.setView(layout);
                         municipalityToast.show();
                         return true;
 
 
 
 
 
 
                     }
 
                 }
 
             }
 
         }
 
         return false;
 
     }
 
     private void hideMunicipalityToast() {
         if (municipalityToast!=null) {
             municipalityToast.cancel();
         }
     }
 
     /*
     * ============
     * Setters and getters
     * ============
     */
 
 	public Rect getBounds() {
 		return bounds;
 	}
 
     public float getScaleFactor() {
         return scaleFactor;
     }
 
     public void setScaleFactor(float scaleFactor) {
         this.scaleFactor = scaleFactor;
     }
 
     public void updateBounds(Rect bounds) {
         if (bounds==null) {
             initializeBounds();
         } else {
             this.bounds = bounds;
         }
     }
 
     public void setAllImagesDownloaded(boolean allImagesDownloaded) {
         this.allImagesDownloaded = allImagesDownloaded;
     }
 
     public void setDownloadProgressText(String text) {
         downloadProgressText = text;
     }
 
     public AnimationViewPlayer getPlayer() {
         return player;
     }
 
     public void setMunicipalities(List<Municipality> municipalities) {
         this.municipalities = municipalities;
         this.municipalitiesOnScreen = new HashMap<Municipality, Point2D.Double>();
     }
 }
