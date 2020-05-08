 package net.sf.andpdf.pdfviewer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 
 import net.sf.andpdf.nio.ByteBuffer;
 import net.sf.andpdf.pdfviewer.gui.FullScrollView;
 import net.sf.andpdf.refs.HardReference;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.RectF;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewTreeObserver;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.sec.android.allshare.Device;
 import com.sec.android.allshare.Device.DeviceType;
 import com.sec.android.allshare.DeviceFinder;
 import com.sec.android.allshare.DeviceFinder.IDeviceFinderEventListener;
 import com.sec.android.allshare.ERROR;
 import com.sec.android.allshare.ServiceConnector;
 import com.sec.android.allshare.ServiceConnector.IServiceConnectEventListener;
 import com.sec.android.allshare.ServiceConnector.ServiceState;
 import com.sec.android.allshare.ServiceProvider;
 import com.sec.android.allshare.media.ViewController;
 import com.sun.pdfview.PDFFile;
 import com.sun.pdfview.PDFImage;
 import com.sun.pdfview.PDFPage;
 import com.sun.pdfview.PDFPaint;
 import com.sun.pdfview.decrypt.PDFAuthenticationFailureException;
 import com.sun.pdfview.decrypt.PDFPassword;
 import com.sun.pdfview.font.PDFFont;
 
 import net.sf.andpdf.pdfviewer.DevicePicker;
 /**
  * U:\Android\android-sdk-windows-1.5_r1\tools\adb push u:\Android\simple_T.pdf /data/test.pdf
  * @author ferenc.hechler
  */
 public abstract class PdfViewerActivity extends FragmentActivity {
 
 	private static final int STARTPAGE = 1;
 	private static final float STARTZOOM = 1.3f;
 	
 	private static final float MIN_ZOOM = 0.25f;
 	private static final float MAX_ZOOM = 2.0f;
 	private static final float ZOOM_INCREMENT = 1.2f;
 	
 	private static final String TAG = "PDFVIEWER";
 	
     public static final String EXTRA_PDFFILENAME = "net.sf.andpdf.extra.PDFFILENAME";
     public static final String EXTRA_SHOWIMAGES = "net.sf.andpdf.extra.SHOWIMAGES";
     public static final String EXTRA_ANTIALIAS = "net.sf.andpdf.extra.ANTIALIAS";
     public static final String EXTRA_USEFONTSUBSTITUTION = "net.sf.andpdf.extra.USEFONTSUBSTITUTION";
     public static final String EXTRA_KEEPCACHES = "net.sf.andpdf.extra.KEEPCACHES";
 	
 	public static final boolean DEFAULTSHOWIMAGES = true;
 	public static final boolean DEFAULTANTIALIAS = true;
 	public static final boolean DEFAULTUSEFONTSUBSTITUTION = false; 
 	public static final boolean DEFAULTKEEPCACHES = true;
     
 	private final static int MENU_NEXT_PAGE = 1;
 	private final static int MENU_PREV_PAGE = 2;
 	private final static int MENU_GOTO_PAGE = 3;
 	private final static int MENU_ZOOM_IN   = 4;
 	private final static int MENU_ZOOM_OUT  = 5;
 	private final static int MENU_BACK      = 6;
 	private final static int MENU_CLEANUP   = 7;
 	
 	private final static int DIALOG_PAGENUM = 1;
 	
 	private GraphView mOldGraphView;
 	private GraphView mGraphView;
 	private String pdffilename;
 	private PDFFile mPdfFile;
 	private int mPage;
 	private float mZoom;
     private File mTmpFile;
     private ProgressDialog progress;
 
 
     /*private View navigationPanel;
     private Handler closeNavigationHandler;
     private Thread closeNavigationThread;*/
 
     
     private PDFPage mPdfPage; 
     
     private Thread backgroundThread;
     private Handler uiHandler;
 
     
     
     //ALLSHARE
 
         private String currentPageFilePath;
         private Uri mImageUri = null;
 
         // AllShare related fields
         private DevicePicker mDevicePicker;
         private AllShareService mAllShareService;
 
         // activity UI related fields
         private boolean mIsActivityVisible = false;
         private ImageView mImageView = null;
 
         private TextView mZoomTextView = null;
         // ImageView dimensions used in multiple calculations
         private int mImageViewWidth;
 
         private int mImageViewHeight;
         // helpers
         private PinchToZoomTransformation mPinchToZoomTransformation;
         private static final String IMAGE_PATH = "/tmp_AllShare_pdf";//.jpg"; // image path relative to sdcard
         private static final String IMAGE_ASSET = "tmp_AllShare_pdf";//.jpg"; // image asset name
         // indicates image rotation direction
         public enum RotationDirection {
             CLOCKWISE,
             COUNTER_CLOCKWISE
         }
  
     
         
         
         
         
 	/**
 	 * restore member variables from previously saved instance
 	 * @see onRetainNonConfigurationInstance
 	 * @return true if instance to restore from was found
 	 */
 	private boolean restoreInstance() {
 		mOldGraphView = null;
 		Log.e(TAG, "restoreInstance");
 		if (getLastNonConfigurationInstance()==null)
 			return false;
 		PdfViewerActivity inst =(PdfViewerActivity)getLastNonConfigurationInstance();
 		if (inst != this) {
 			Log.e(TAG, "restoring Instance");
 			mOldGraphView = inst.mGraphView;
 			mPage = inst.mPage;
 			mPdfFile = inst.mPdfFile;
 			mPdfPage = inst.mPdfPage;
 			mTmpFile = inst.mTmpFile;
 			mZoom = inst.mZoom;
 			pdffilename = inst.pdffilename;
 			backgroundThread = inst.backgroundThread; 
 			// mGraphView.invalidate();
 		}
 		return true;
 	}
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
    
         Log.i(TAG, "onCreate");
       
         //progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page");
         /*closeNavigationHandler = new Handler();
         closeNavigationThread = new Thread(new Runnable() {
 
         	public void run() {
         		navigationPanel.startAnimation(AnimationUtils.loadAnimation(PdfViewerActivity.this,
         				R.anim.slide_out));
         		navigationPanel.setVisibility(View.GONE);
         	}
         });*/
 
         /*if (navigationPanel == null) {
         	navigationPanel = ((ViewStub) findViewById(R.id.stub_navigation)).inflate();
         	navigationPanel.setVisibility(View.GONE);
         	ImageButton previous = (ImageButton)navigationPanel.findViewById(R.id.navigation_previous);
         	previous.setBackgroundDrawable(null);
         }*/
         
         uiHandler = new Handler();
         restoreInstance();
         
         mAllShareService = new AllShareService(getApplicationContext());
         
         if (mOldGraphView != null) {
         	
         
       	   
 	        System.out.println("mOldGraphView != null");
 	        mGraphView = new GraphView(this);
 	        //mGraphView.fileMillis = mOldGraphView.fileMillis;
 	        mGraphView.mBi = mOldGraphView.mBi;
 	        //mGraphView.mLine1 = mOldGraphView.mLine1;
 	        //mGraphView.mLine2 = mOldGraphView.mLine2;
 	        //mGraphView.mLine3 = mOldGraphView.mLine3;
 	        //mGraphView.mText = mOldGraphView.mText;
 	        //mGraphView.pageParseMillis= mOldGraphView.pageParseMillis;
 	        //mGraphView.pageRenderMillis= mOldGraphView.pageRenderMillis;
 	        mOldGraphView = null;
 	        mGraphView.mImageView.setImageBitmap(mGraphView.mBi);
 	        mGraphView.updateTexts();
 	       
 	        setContentView(mGraphView);
 	        
 	        
 	        
 	        
 	        
 	        //*********************************ALLSHARE****************************
 	             
 	        
 	        findUiElements();
 
 	        // checks if image to show exists; if not: create it
 	        copyAssets();
 	        
 	        // set the image to be shown
 	        File dir = Environment.getExternalStorageDirectory();
 	        File file = new File(dir, IMAGE_PATH);
 	        mImageUri = Uri.fromFile(file);
 
 	        if(IMAGE_PATH != null)
 	        {
 	        System.out.println("PDFFILE IMAGE_PATH " + IMAGE_PATH );
 	        }
 	        if(mImageUri != null)
 	        {
 	        	 System.out.println("\n mImageURI " + mImageUri);
 	        }
 	        else
 	        {
 	        	System.out.println("MIMAGEURI IS NULL");
 	        }
 	        
 	        System.out.println("DIR " + dir.getAbsolutePath());
 	        if(file != null)
 	        {
 	        	System.out.println("\n file " + file.getAbsolutePath());
 	        }
 	        
 	        mImageView = (ImageView) findViewById(R.id.pdf_image);
 	        if(mImageView == null)
 	        {
 	        	System.out.println("IMAGEVIEW NULL");
 	        }
 	        else
 	        {
 	        	System.out.println("IMAGE VIEW NOT NULL");
 	        }
 	        mImageView.setImageBitmap(decodeImage(file));
 
 	        mAllShareService = AllShareService.getInstance();
 	        
 
 	        if(mAllShareService == null)
 	        {
 	        	System.out.println("mAllShareService == null");
 	        	
 	        }
 	        
 	        mAllShareService.init(getApplicationContext());
 	      
 
 	        // listens for layout measurements finish
 	        ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
 	        if (viewTreeObserver.isAlive()) {
 	            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
 	                @Override
 	                public void onGlobalLayout() {
 	                    initialize();
 	                    System.out.println("GlobalLayout Listener");
 	                    scaleTv(0, mImageViewWidth / 2.0f, mImageViewHeight / 2.0f);
 	                }
 	            });
 	        }
 	         
 	         
 	         
 	         
 	         
 	         
 	         
 	         
 	         
 	         //*********************************ALLSHARE****************************
 	         
 	        
 	
 	        
 	        
 	        
 	        
 	        
 	        
 	        
 	        
 	        
 	        
 	        
 	        
 	        
 	        
         }
         else {
         	System.out.println("mOldGraphView == null");
 	        mGraphView = new GraphView(this);	        
 	        Intent intent = getIntent();
 	        Log.i(TAG, ""+intent);
 
 	        boolean showImages = getIntent().getBooleanExtra(PdfViewerActivity.EXTRA_SHOWIMAGES, PdfViewerActivity.DEFAULTSHOWIMAGES);
 	        PDFImage.sShowImages = showImages;
 	        boolean antiAlias = getIntent().getBooleanExtra(PdfViewerActivity.EXTRA_ANTIALIAS, PdfViewerActivity.DEFAULTANTIALIAS);
 	        PDFPaint.s_doAntiAlias = antiAlias;
 	    	boolean useFontSubstitution = getIntent().getBooleanExtra(PdfViewerActivity.EXTRA_USEFONTSUBSTITUTION, PdfViewerActivity.DEFAULTUSEFONTSUBSTITUTION);
 	        PDFFont.sUseFontSubstitution= useFontSubstitution;
 	    	boolean keepCaches = getIntent().getBooleanExtra(PdfViewerActivity.EXTRA_KEEPCACHES, PdfViewerActivity.DEFAULTKEEPCACHES);
 	        HardReference.sKeepCaches= keepCaches;
 		        
 	        if (intent != null) {
 	        	if ("android.intent.action.VIEW".equals(intent.getAction())) {
         			pdffilename = storeUriContentToFile(intent.getData());
 	        	}
 	        	else {
 	                pdffilename = getIntent().getStringExtra(PdfViewerActivity.EXTRA_PDFFILENAME);
 	        	}
 	        }
 	        
 	        if (pdffilename == null)
 	        	pdffilename = "no file selected";
 
 			mPage = STARTPAGE;
 			mZoom = STARTZOOM;
 
 			setContent(null);
 	        
         }
         
      
         
         
         
         
     }
 
     
     /**
     *
     * Android UI related helper methods.
     *
     */
 
    /**
     * Finds UI elements and stores their references in class fields for later use.
     */
    private void findUiElements() {
       // mImageView = (ImageView) findViewById(R.id.imageView);
 	   mImageView = (ImageView) findViewById(R.id.pdf_image);	   
        mZoomTextView = (TextView) findViewById(R.id.zoomTextView);
        mDevicePicker = (DevicePicker) getSupportFragmentManager().findFragmentById(R.id.devicePicker);
      
    }
 
    /**
     * Shows status message.
     */
    private void showStatusMessage(int message) {
        if (mIsActivityVisible) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
 
    /**
     * Shows error message.
     */
    private void showErrorMessage(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
 
    /**
     * Updates TextView with current scale.
     *
     * 100% means that image fits exactly in ImageView boundaries.
     */
    private void updateZoomTextViewText() {
        mZoomTextView.setText((int) (getCurrentScale() * 100) + "%");
    }
 
    /**
     * Updates ImageView with current transformation.
     */
    private void updateImageView() {
        mImageView.setImageMatrix(mPinchToZoomTransformation.getMatrix());
        mImageView.invalidate();
    }
 
    /**
     * Returns image of smaller size so it fits in memory of devices with smaller heap.
     */
    private Bitmap decodeImage(File file) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inPreferredConfig = Bitmap.Config.RGB_565;
            o.inPurgeable = true;//
            o.inJustDecodeBounds = true;//
            Bitmap retval = BitmapFactory.decodeFile(file.getAbsolutePath(), o);
            if(retval == null)
            {
         	   System.out.println("BITMAP RETURNED NULL");
            }
            
            
            return retval;
        } catch (Exception e) {
     	   System.out.println("UNABLE TO LAD IMAGE");
          e.printStackTrace();
           
            return null;
        }
    }
 
    
    
    /**
     * Forces Android Framework to immediately remove bitmap data kept in memory.
     */
    private void destroyImageView() {
        Drawable drawable = mImageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
 
        mImageView = null;
    }
    
    /**
     * Handles clockwise rotation button click.
     */
    private void onClockwiseRotateButtonClick() {
        updateUiAndTvRotation(RotationDirection.CLOCKWISE);
    }
    
    /**
     * Handles counter clockwise rotation button click.
     */
    private void onCounterClockwiseRotateButtonClick() {
        updateUiAndTvRotation(RotationDirection.COUNTER_CLOCKWISE);
    }
    
    /**
     * Handles reset button click.
     */
    private void onZoomResetButtonClick() {
        // resets image view scale and translation and makes it fit in screen's center
        mPinchToZoomTransformation.reset();
        updateImageView();
 
        updateZoomTextViewText();
 
        scaleTv(0, mImageViewWidth / 2.0f, mImageViewHeight / 2.0f);
    }
 
 
    /**
     * Listener responsible for handling ImageViewer/ViewController events.
     */
    private AllShareService.Listener mAllShareServiceListener = new AllShareService.Listener() {
        @Override
        public void onImageReady() {
 
            System.out.println("IMAGE VIEWER READY");
           
            scaleTv(0, mImageViewWidth / 2.0f, mImageViewHeight / 2.0f);
        }
 
        @Override
        public void onDisconnected() {
            mDevicePicker.setActive(false);
        }
    };
 
    /**
     * Listener responsible for handling GestureHandler scale and translate events.
     */
    private GestureHandler.Listener mGestureHandlerListener = new GestureHandler.Listener() {
        @Override
        public void onScale(GestureHandler.ScaleEvent event) {
            float scaleFactorDelta = event.getScaleFactorDelta();
            float scalePointX = event.getScalePointX();
            float scalePointY = event.getScalePointY();
 
            // updates ImageViewer and ViewController on scale event
            updateUiAndTvScale(scaleFactorDelta, scalePointX, scalePointY);
        }
    };
 
    /**
     *
     * Transformation related methods.
     *
     */
    
    /**
     * Initializes mPinchToZoomTransformation.
     *
     * After initialization subsequent calls will be ignored.
     *
     * Must be called after Android finished layout measurements and after drawable was set on ImageView.
     */
    private void initialize() {
        if (mPinchToZoomTransformation == null && mImageView != null && mImageView.getDrawable() != null) {
            int imageWidth = mImageView.getDrawable().getIntrinsicWidth();
            int imageHeight = mImageView.getDrawable().getIntrinsicHeight();
            mImageViewWidth = mImageView.getMeasuredWidth();
            mImageViewHeight = mImageView.getMeasuredHeight();
 
            mPinchToZoomTransformation = new PinchToZoomTransformation(
                    imageWidth,
                    imageHeight,
                    mImageViewWidth,
                    mImageViewHeight);
            mPinchToZoomTransformation.setMaxScale(5);
 
            mImageView.setScaleType(ImageView.ScaleType.MATRIX);
            mPinchToZoomTransformation.setScaleToFitInCenter();
 
            updateImageView();
        }
    }
 
    /**
     * Updates application UI and image on TV according to scale values.
     */
    private float getCurrentScale() {
        RectF imageRect = mPinchToZoomTransformation.getRect();
 
        float imageViewRatio = (float) mImageViewWidth / mImageViewHeight;
        float imageRatio = imageRect.width() / imageRect.height();
 
        return imageViewRatio < imageRatio ?
                imageRect.width() / mImageViewWidth :
                imageRect.height() / mImageViewHeight;
    }
 
    /**
     * Updates application UI and image on TV according to scale values.
     */
    private void updateUiAndTvScale(float scaleFactorDelta, float scalePointX, float scalePointY) {
        // rounds scale values so that they are the same as those accepted by ViewController
        float roundedScaleFactorDelta = (float) ((int) (scaleFactorDelta * 100)) / 100;
        float roundedScalePointX = (int) scalePointX;
        float roundedScalePointY = (int) scalePointY;
 
        // applies rounded scale to transformation object
        // additionally the scale that was applied after bounds check is stored in newScaleFactorDelta
        float newScaleFactorDelta =
            mPinchToZoomTransformation.updateScaleInBounds(
                    roundedScaleFactorDelta, roundedScalePointX, roundedScalePointY);
 
        updateImageView();
 
        scaleTv(newScaleFactorDelta, roundedScalePointX, roundedScalePointY);
 
        updateZoomTextViewText();
    }
 
    /**
     * Updates application UI and image on TV according to rotation values.
     */
    private void updateUiAndTvRotation(RotationDirection rotationDirection) {
        PinchToZoomTransformation.RotationDirection pinchToZoomRotationDirection =
            rotationDirection == RotationDirection.CLOCKWISE ?
            PinchToZoomTransformation.RotationDirection.CLOCKWISE :
            PinchToZoomTransformation.RotationDirection.COUNTER_CLOCKWISE;
 
        mPinchToZoomTransformation.updateRotationAndFitInBounds(pinchToZoomRotationDirection);
 
        updateImageView();
 
        scaleTv(0, mImageViewWidth / 2.0f, mImageViewHeight / 2.0f);
 
        updateZoomTextViewText();
    }
 
    /**
     * Abstracts call to ViewController.zoom() method.
     */
    private void scaleTv(float scaleFactorDelta, float scalePointX, float scalePointY) {
        if (mAllShareService.isImageReady()) {
            int tvScalePointX = (int) scalePointX;
            int tvScalePointY = (int) scalePointY;
            int tvScaleFactorDelta = (int) (scaleFactorDelta * 100);
 
            int rotation = mPinchToZoomTransformation.getRotation();
 
            mAllShareService.getViewController().zoom(
                    tvScalePointX,
                    tvScalePointY,
                    tvScaleFactorDelta,
                    rotation,
                    mImageViewWidth,
                    mImageViewHeight);
        }
    }
 
    /**
     * Function creates image file from application asset.
     */
    private void copyAssets() {
        AssetManager assetManager = getAssets();
         
        InputStream in = null;
        OutputStream out = null;
        try {
        	File dir = Environment.getExternalStorageDirectory();
        	File file = new File(dir, IMAGE_PATH + mPage + ".jpg");
 
 			if (file.exists() == false) {
 				
 				in = assetManager.open(IMAGE_ASSET);
 				out = new FileOutputStream(file);
 
 				byte[] buffer = new byte[1024];
 				int read;
 				
 				while ((read = in.read(buffer)) != -1) {
 					out.write(buffer, 0, read);
 				}
 			}
        }
        catch (IOException e) {
 
            System.out.println("UNABLE TO LoAD IMAGE");
        }
        finally {
            try {
                if (in != null) in.close();
            } catch (IOException ignored) {
                // Ignore
            }
            try {
                if (out != null) out.close();
            } catch (IOException ignored) {
                // Ignore
            }
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
 
        mIsActivityVisible = true;
 
        if(mAllShareService == null)
        {
     	   System.out.println("mAllShareService == null");
        }
        if(mAllShareServiceListener == null)
        {
     	   System.out.println("mAllShareServiceListener == null");
        }
        if(mDevicePicker == null)
        {
     	   System.out.println("mDevicePicker == null");
     	   mDevicePicker = new DevicePicker();
     	//   mDevicePicker = (DevicePicker) getSupportFragmentManager().findFragmentById(R.id.devicePicker);
        }
        if(mDevicePicker == null)
        {
     	   System.out.println("mDevicePicker == null");
     	 //  mDevicePicker = new DevicePicker();
        }
         
        
        mAllShareService.registerListener(mAllShareServiceListener);       
        mAllShareService.init(getApplicationContext());       
        
        
    }
 
 
    //***********************************
 	private void setContent(String password) {
         try { 
     		parsePDF(pdffilename, password);
 	        setContentView(mGraphView);
 	        startRenderThread(mPage, mZoom);
     	}
         catch (PDFAuthenticationFailureException e) {
         	setContentView(getPdfPasswordLayoutResource());
            	final EditText etPW= (EditText) findViewById(getPdfPasswordEditField());
            	Button btOK= (Button) findViewById(getPdfPasswordOkButton());
         	Button btExit = (Button) findViewById(getPdfPasswordExitButton());
             btOK.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					String pw = etPW.getText().toString();
 		        	setContent(pw);
 				}
 			});
             btExit.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					finish();
 				}
 			});
         }
 	}
 	private synchronized void startRenderThread(final int page, final float zoom) {
 		if (backgroundThread != null)
 			return;
 		
 		mGraphView.showText("reading page "+ page+", zoom:"+zoom);
 		//progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page");
         backgroundThread = new Thread(new Runnable() {
 			public void run() {
 				try {
 			        if (mPdfFile != null) {
 			    		//progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page");
 			    		
 //			        	File f = new File("/sdcard/andpdf.trace");
 //			        	f.delete();
 //			        	Log.e(TAG, "DEBUG.START");
 //			        	Debug.startMethodTracing("andpdf");
 			        	showPage(page, zoom);
 //			        	Debug.stopMethodTracing();
 //			        	Log.e(TAG, "DEBUG.STOP");
 				        
 				        /*if (progress != null)
 				        	progress.dismiss();*/
 			        }
 				} catch (Exception e) {
 					Log.e(TAG, e.getMessage(), e);
 				}
 		        backgroundThread = null;
 			}
 		});
         updateImageStatus();
         backgroundThread.start();
 	}
 
 
 	private void updateImageStatus() {
 //		Log.i(TAG, "updateImageStatus: " +  (System.currentTimeMillis()&0xffff));
 		if (backgroundThread == null) {
 			mGraphView.updateUi();
 			
 			/*if (progress != null)
 				progress.dismiss();*/
 			return;
 		}
 		mGraphView.updateUi();
 		mGraphView.postDelayed(new Runnable() {
 			public void run() {
 				updateImageStatus();
 				
 				/*if (progress != null)
 					progress.dismiss();*/
 			}
 		}, 1000);
 		
 		
 		
 		
 	}
 
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(Menu.NONE, MENU_PREV_PAGE, Menu.NONE, "Previous Page").setIcon(getPreviousPageImageResource());
         menu.add(Menu.NONE, MENU_NEXT_PAGE, Menu.NONE, "Next Page").setIcon(getNextPageImageResource());
         menu.add(Menu.NONE, MENU_GOTO_PAGE, Menu.NONE, "Goto Page");
         menu.add(Menu.NONE, MENU_ZOOM_OUT, Menu.NONE, "Zoom Out").setIcon(getZoomOutImageResource());
         menu.add(Menu.NONE, MENU_ZOOM_IN, Menu.NONE, "Zoom In").setIcon(getZoomInImageResource());
         if (HardReference.sKeepCaches)
             menu.add(Menu.NONE, MENU_CLEANUP, Menu.NONE, "Clear Caches");
         	
         return true;
     }
     
     /**
      * Called when a menu item is selected.
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
     	switch (item.getItemId()) {
     	case MENU_NEXT_PAGE: {
     		nextPage();
     		break;
     	}
     	case MENU_PREV_PAGE: {
     		prevPage();
     		break;
     	}
     	case MENU_GOTO_PAGE: {
     		gotoPage();
     		break;
     	}
     	case MENU_ZOOM_IN: {
     		zoomIn();
     		break;
     	}
     	case MENU_ZOOM_OUT: {
     		zoomOut();
     		break;
     	}
     	case MENU_BACK: {
             finish();
             break;
     	}
     	case MENU_CLEANUP: {
             HardReference.cleanup();
             break;
     	}
     	}
     	return true;
     }
     
     
     private void zoomIn() {
     	if (mPdfFile != null) {
     		if (mZoom < MAX_ZOOM) {
     			mZoom *= ZOOM_INCREMENT;
     			if (mZoom > MAX_ZOOM)
     				mZoom = MAX_ZOOM;
     			
     			if (mZoom >= MAX_ZOOM) {
     				Log.d(TAG, "Disabling zoom in button");
     				mGraphView.bZoomIn.setEnabled(false);
     			}
     			else
     				mGraphView.bZoomIn.setEnabled(true);
     			
 				mGraphView.bZoomOut.setEnabled(true);
 
     			System.out.println("Current Zoom " + mZoom + " This is where CHORD notifications can go.");
     			//progress = ProgressDialog.show(PdfViewerActivity.this, "Rendering", "Rendering PDF Page");
     			startRenderThread(mPage, mZoom);
     		}
     	}
 	}
 
     private void zoomOut() {
     	if (mPdfFile != null) {
     		if (mZoom > MIN_ZOOM) {
     			mZoom /= ZOOM_INCREMENT;
     			if (mZoom < MIN_ZOOM)
     				mZoom = MIN_ZOOM;
 
     			if (mZoom <= MIN_ZOOM) {
     				Log.d(TAG, "Disabling zoom out button");
     				mGraphView.bZoomOut.setEnabled(false);
     			}
     			else
     				mGraphView.bZoomOut.setEnabled(true);
 
     			mGraphView.bZoomIn.setEnabled(true);
     			System.out.println("Current Zoom " + mZoom + " This is where CHORD notifications can go.");
     			//progress = ProgressDialog.show(PdfViewerActivity.this, "Rendering", "Rendering PDF Page");
     			startRenderThread(mPage, mZoom);
     		}
     	}
 	}
 
 	private void nextPage() {
     	if (mPdfFile != null) {
     		if (mPage < mPdfFile.getNumPages()) {
     			mPage += 1;
     			System.out.println("Current Page " + mPage + " This is where CHORD notifications can go.");
     			
     			mGraphView.bZoomOut.setEnabled(true);
     			mGraphView.bZoomIn.setEnabled(true);
     			progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page " + mPage, true, true);
     			startRenderThread(mPage, mZoom);
     			sendPageNotification();//For chord Purposes
     		
     		}
     	}
 	}
 	
 	public void setPage(int targetPage) {//For chord Purposes
     	if (mPdfFile != null) {
     		if (mPage < mPdfFile.getNumPages()) {
     			mPage = targetPage;
     			System.out.println("Current Page " + mPage + " This is where CHORD notifications can go.");
     			mGraphView.bZoomOut.setEnabled(true);
     			mGraphView.bZoomIn.setEnabled(true);
     			progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page " + mPage, true, true);
     			startRenderThread(mPage, mZoom);
     		
     		}
     	}
 	}
 	
 	public int getPage()//For chord Purposes
 	{
 		return mPage;
 	}
 	
 	public void sendPageNotification(){}//For chord Purposes
 	public void getPageNotification(){}//For chord Purposes
 
     private void prevPage() {
     	if (mPdfFile != null) {
     		if (mPage > 1) {
     			mPage -= 1;
     			System.out.println("Current Page " + mPage + " This is where CHORD notifications can go.");
     			mGraphView.bZoomOut.setEnabled(true);
     			mGraphView.bZoomIn.setEnabled(true);
     			progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page " + mPage, true, true);
     			startRenderThread(mPage, mZoom);
     			sendPageNotification();//For chord Purposes
     		}
     	}
 	}
     
 	private void gotoPage() {
     	if (mPdfFile != null) {
             showDialog(DIALOG_PAGENUM);
     	}
 	}
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DIALOG_PAGENUM:
 	        LayoutInflater factory = LayoutInflater.from(this);
 	        final View pagenumView = factory.inflate(getPdfPageNumberResource(), null);
 			final EditText edPagenum = (EditText)pagenumView.findViewById(getPdfPageNumberEditField());
 			edPagenum.setText(Integer.toString(mPage));
 			edPagenum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 				
 				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 					if (event == null || ( event.getAction() == 1)) {
 					    // Hide the keyboard
 					    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 					    imm.hideSoftInputFromWindow(edPagenum.getWindowToken(), 0);
 					}					    
 					return true;
 				}
 			});
 	        return new AlertDialog.Builder(this)
 	            //.setIcon(R.drawable.icon)
 	            .setTitle("Jump to page")
 	            .setView(pagenumView)
 	            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int whichButton) {
 	            		String strPagenum = edPagenum.getText().toString();
 	            		int pageNum = mPage;
 	            		try {
 	            			pageNum = Integer.parseInt(strPagenum);
 	            		}
 	            		catch (NumberFormatException ignore) {}
 	            		if ((pageNum!=mPage) && (pageNum>=1) && (pageNum <= mPdfFile.getNumPages())) {
 	            			mPage = pageNum;
 	            			mGraphView.bZoomOut.setEnabled(true);
 	            			mGraphView.bZoomIn.setEnabled(true);
 	            			progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page " + mPage, true, true);
 	            			startRenderThread(mPage, mZoom);
 	            		}
 	                }
 	            })
 	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int whichButton) {
 	                }
 	            })
 	            .create();
         }        
         return null;
     }
     
 	private class GraphView extends FullScrollView {
     	//private String mText;
     	//private long fileMillis;
     	//private long pageParseMillis;
     	//private long pageRenderMillis;
     	private Bitmap mBi;
     	//private String mLine1;
     	//private String mLine2;
     	//private String mLine3;
     	private ImageView mImageView;
     	//private TextView mLine1View; 
     	//private TextView mLine2View; 
     	//private TextView mLine3View; 
     	private Button mBtPage;
     	private Button mBtPage2;
     	
     	ImageButton bZoomOut;
     	ImageButton bZoomIn;
         
         public GraphView(Context context) {
             super(context);
 
             //setContentView(R.layout.graphics_view);
             // layout params
 			LinearLayout.LayoutParams lpWrap1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
 			LinearLayout.LayoutParams lpWrap10 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,10);
 
             // vertical layout
 			LinearLayout vl=new LinearLayout(context);
 			vl.setLayoutParams(lpWrap10);
 			vl.setOrientation(LinearLayout.VERTICAL);
 
 			
 			
 			
 			
 			if (mOldGraphView == null)
 				progress = ProgressDialog.show(PdfViewerActivity.this, "Loading", "Loading PDF Page", true, true);
 			
 			addNavButtons(vl);
 		        // remember page button for updates
 		        mBtPage2 = mBtPage;
 		        
 		        mImageView = new ImageView(context);
 		        setPageBitmap(null);
 		        updateImage();
 		        mImageView.setLayoutParams(lpWrap1);
 		        mImageView.setPadding(5, 5, 5, 5);
 		        vl.addView(mImageView);
 		        /*mImageView = (ImageView) findViewById(R.id.pdf_image);
 		        if (mImageView == null) {
 		        	Log.i(TAG, "mImageView is null!!!!!!");
 		        }
 		        setPageBitmap(null);
 		        updateImage();*/
 		        
 		        /*
 		        navigationPanel = new ViewStub(PdfViewerActivity.this, R.layout.navigation_overlay);
 		        final ImageButton previous = (ImageButton)navigationPanel.findViewById(R.id.navigation_previous);
 		        previous.setBackgroundDrawable(null);
 		        previous.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						prevPage();
 					}
 				});
 
 		        final ImageButton next = (ImageButton)navigationPanel.findViewById(R.id.navigation_next);
 		        next.setBackgroundDrawable(null);
 		        previous.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						nextPage();
 					}
 				});
  
 		        //stub.setLayoutParams(Layou)
 		        vl.addView(navigationPanel);
 		        
 		        vl.setOnTouchListener(new OnTouchListener() {
 					
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						if (navigationPanel.getVisibility() != View.VISIBLE) {
 							navigationPanel.startAnimation(AnimationUtils.loadAnimation(PdfViewerActivity.this,
 									R.anim.slide_in));
 							navigationPanel.setVisibility(View.VISIBLE);
 						}
 
 						return false;
 					}
 				});
 				*/
 
 		        //addNavButtons(vl);
 			    
 			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 100));
 			setBackgroundColor(Color.LTGRAY);
 			setHorizontalScrollBarEnabled(true);
 			setHorizontalFadingEdgeEnabled(true);
 			setVerticalScrollBarEnabled(true);
 			setVerticalFadingEdgeEnabled(true);
 			addView(vl);
         }
 
         private void addNavButtons(ViewGroup vg) {
         	
 	        addSpace(vg, 6, 6);
 	        
 			LinearLayout.LayoutParams lpChild1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
 			LinearLayout.LayoutParams lpWrap10 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,10);
         	
         	Context context = vg.getContext();
 			LinearLayout hl=new LinearLayout(context);
 			hl.setLayoutParams(lpWrap10);
 			hl.setOrientation(LinearLayout.HORIZONTAL);
 
 			
 			
 				// zoom out button
 				bZoomOut=new ImageButton(context);
 				bZoomOut.setBackgroundDrawable(null);
 				bZoomOut.setLayoutParams(lpChild1);
 				//bZoomOut.setText("-");
 				//bZoomOut.setWidth(40);
 				bZoomOut.setImageResource(getZoomOutImageResource());
 				bZoomOut.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 			            zoomOut();
 					}
 				});
 		        hl.addView(bZoomOut);
 		        
 				// zoom in button
 		        bZoomIn=new ImageButton(context);
 		        bZoomIn.setBackgroundDrawable(null);
 				bZoomIn.setLayoutParams(lpChild1);
 		        //bZoomIn.setText("+");
 		        //bZoomIn.setWidth(40);
 				bZoomIn.setImageResource(getZoomInImageResource());
 		        bZoomIn.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 			            zoomIn();
 					}
 				});
 		        hl.addView(bZoomIn);
 	    
 		        addSpace(hl, 6, 6);
 		        
 				// prev button
 		        ImageButton bPrev=new ImageButton(context);
 		        bPrev.setBackgroundDrawable(null);
 		        bPrev.setLayoutParams(lpChild1);
 		        //bPrev.setText("<");
 		        //bPrev.setWidth(40);
 		        bPrev.setImageResource(getPreviousPageImageResource());
 		        bPrev.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 			            prevPage();
 					}
 				});
 		        hl.addView(bPrev);
         
 				// page button
 				mBtPage=new Button(context);
 				mBtPage.setLayoutParams(lpChild1);
 				String maxPage = ((mPdfFile==null)?"0":Integer.toString(mPdfFile.getNumPages()));
 				mBtPage.setText(mPage+"/"+maxPage);
 				mBtPage.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 			    		gotoPage();
 					}
 				});
 		        hl.addView(mBtPage);
         
 				// next button
 				ImageButton bNext=new ImageButton(context);
 				bNext.setBackgroundDrawable(null);
 		        bNext.setLayoutParams(lpChild1);
 		        //bNext.setText(">");
 		        //bNext.setWidth(40);
 		        bNext.setImageResource(getNextPageImageResource());
 		        bNext.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 			    		nextPage();
 					}
 				});
 		        hl.addView(bNext);
         
 		        addSpace(hl, 20, 20);
         
 				// exit button
 		        /*
 				Button bExit=new Button(context);
 		        bExit.setLayoutParams(lpChild1);
 		        bExit.setText("Back");
 		        bExit.setWidth(60);
 		        bExit.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 			            finish();
 					}
 				});
 		        hl.addView(bExit);*/
         	        
 		        vg.addView(hl);
 		    
 	        addSpace(vg, 6, 6);
 		}
 
 		private void addSpace(ViewGroup vg, int width, int height) {
 			TextView tvSpacer=new TextView(vg.getContext());
 			tvSpacer.setLayoutParams(new LinearLayout.LayoutParams(width,height,1));
 			tvSpacer.setText("");
 //			tvSpacer.setWidth(width);
 //			tvSpacer.setHeight(height);
 	        vg.addView(tvSpacer);
     
 		}
 
 		private void showText(String text) {
         	Log.i(TAG, "ST='"+text+"'");
         	//mText = text;
         	updateUi();
 		}
         
         private void updateUi() {
         	uiHandler.post(new Runnable() {
 				public void run() {
 		        	updateTexts();
 				}
 			});
 		}
 
         private void updateImage() {
         	uiHandler.post(new Runnable() {
 				public void run() {
 		        	mImageView.setImageBitmap(mBi);  
 		        
 			      
 				}
 			});
         	
         	
 		}
 
 		private void setPageBitmap(Bitmap bi) {
 			if (bi != null)
 				mBi = bi;
 			else {
 				/*
 				mBi = Bitmap.createBitmap(100, 100, Config.RGB_565);
 	            Canvas can = new Canvas(mBi);
 	            can.drawColor(Color.RED);
 	            
 				Paint paint = new Paint();
 	            paint.setColor(Color.BLUE);
 	            can.drawCircle(50, 50, 50, paint);
 	            
 	            paint.setStrokeWidth(0);
 	            paint.setColor(Color.BLACK);
 	            can.drawText("Bitmap", 10, 50, paint);
 	            */
 			}
 		}
         
 		protected void updateTexts() {
 			/*
             mLine1 = "PdfViewer: "+mText;
             float fileTime = fileMillis*0.001f;
             float pageRenderTime = pageRenderMillis*0.001f;
             float pageParseTime = pageParseMillis*0.001f;
             mLine2 = "render page="+format(pageRenderTime,2)+", parse page="+format(pageParseTime,2)+", parse file="+format(fileTime,2);
     		int maxCmds = PDFPage.getParsedCommands();
     		int curCmd = PDFPage.getLastRenderedCommand()+1;
     		mLine3 = "PDF-Commands: "+curCmd+"/"+maxCmds;
     		//mLine1View.setText(mLine1);
     		//mLine2View.setText(mLine2);
     		//mLine3View.setText(mLine3);
     		 */
     		if (mPdfPage != null) {
 	    		if (mBtPage != null)
 	    			mBtPage.setText(mPdfPage.getPageNumber()+"/"+mPdfFile.getNumPages());
 	    		if (mBtPage2 != null)
 	    			mBtPage2.setText(mPdfPage.getPageNumber()+"/"+mPdfFile.getNumPages());
     		}
         }
 
 		/*private String format(double value, int num) {
 			NumberFormat nf = NumberFormat.getNumberInstance();
 			nf.setGroupingUsed(false);
 			nf.setMaximumFractionDigits(num);
 			String result = nf.format(value);
 			return result;
 		}*/
     }
 
 	@Override
 	protected void onStop()
 	{		
 		super.onStop();
 		mAllShareService.disconnect();
 		cleanupTempFiles();
 	}
 	
 	
     private void showPage(int page, float zoom) throws Exception {
         //long startTime = System.currentTimeMillis();
         //long middleTime = startTime;
     	try {
 	        // free memory from previous page
 	        mGraphView.setPageBitmap(null);
 	        mGraphView.updateImage();
 	        
 	        // Only load the page if it's a different page (i.e. not just changing the zoom level) 
 	        if (mPdfPage == null || mPdfPage.getPageNumber() != page) {
 	        	mPdfPage = mPdfFile.getPage(page, true);
 	        }
 	        //int num = mPdfPage.getPageNumber();
 	        //int maxNum = mPdfFile.getNumPages();
 	        float width = mPdfPage.getWidth();
 	        float height = mPdfPage.getHeight();
 	        //String pageInfo= new File(pdffilename).getName() + " - " + num +"/"+maxNum+ ": " + width + "x" + height;
 	        //mGraphView.showText(pageInfo);
 	        //Log.i(TAG, pageInfo);
 	        RectF clip = null;
 	        //middleTime = System.currentTimeMillis();
 	        Bitmap bi = mPdfPage.getImage((int)(width*zoom), (int)(height*zoom), clip, true, true);
 	        mGraphView.setPageBitmap(bi);
 	        mGraphView.updateImage();
 	        
 	        
 	        writeTempFileToDisc(bi);
 	     //   bi.recycle();//ADDED FOR EFFICIENCY
 	      
 	        File dir = Environment.getExternalStorageDirectory();
 	        File file = new File(dir, IMAGE_PATH+ mPage +".jpg");
 	        mImageUri = Uri.fromFile(file);			       
 	        System.out.println("Displaying " + mImageUri.getPath());	      
 	        mAllShareService.start(mImageUri.getPath() );
 	        
 	        if (progress != null)
 	        	progress.dismiss();
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage(), e);
 			mGraphView.showText("Exception: "+e.getMessage());
 		}
         //long stopTime = System.currentTimeMillis();
         //mGraphView.pageParseMillis = middleTime-startTime;
         //mGraphView.pageRenderMillis = stopTime-middleTime;
     }
     
     private void cleanupTempFiles()
     {
     	File directory = Environment.getExternalStorageDirectory();   
     	File[] listOfFiles = directory.listFiles();
     	System.out.println("Cleanup CAlled");
         for (int i = 0; i < listOfFiles.length; i++) 
         {
           if (listOfFiles[i].isFile() && listOfFiles[i].getAbsolutePath().contains("tmp_AllShare_pdf")) 
           {
         	  listOfFiles[i].delete();
           } 
         }
     }
     
     private void writeTempFileToDisc(Bitmap bi) throws IOException
     {
         //write file for use in AllShare ImageViewer
     	File directory = Environment.getExternalStorageDirectory();        	
     	String filename = "tmp_AllShare_pdf";//.jpg";
         File f = new File(directory + "/" + filename + mPage  +".jpg");  
         
         System.out.println("WRITE TEMPDPF FILE " + f.getAbsolutePath()) ;
         if(f.exists())
         {
         	f.delete();
         }
         f = new File(directory + "/" + filename + mPage  +".jpg"); 
         f.createNewFile();      
         
         currentPageFilePath = f.getAbsolutePath();
         //Convert bitmap to byte array
         Bitmap bitmap = bi;
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         bitmap.compress(CompressFormat.JPEG,  30, bos); //100% of max quality
         byte[] bitmapdata = bos.toByteArray();
 
         //write the bytes in file
         FileOutputStream fos = new FileOutputStream(f);
         fos.write(bitmapdata);
         fos.flush();
         fos.close();
         bos.close();
        bitmap.recycle();
      }
 
 	private void parsePDF(String filename, String password) throws PDFAuthenticationFailureException {
         //long startTime = System.currentTimeMillis();
     	try {
         	File f = new File(filename);
         	long len = f.length();
         	if (len == 0) {
         		mGraphView.showText("file '" + filename + "' not found");
         	}
         	else {
         		mGraphView.showText("file '" + filename + "' has " + len + " bytes");
     	    	openFile(f, password);
         	}
     	}
         catch (PDFAuthenticationFailureException e) {
         	throw e; 
 		} catch (Throwable e) {
 			e.printStackTrace();
 			mGraphView.showText("Exception: "+e.getMessage());
 		}
         //long stopTime = System.currentTimeMillis();
         //mGraphView.fileMillis = stopTime-startTime;
 	}
 
     
     /**
      * <p>Open a specific pdf file.  Creates a DocumentInfo from the file,
      * and opens that.</p>
      *
      * <p><b>Note:</b> Mapping the file locks the file until the PDFFile
      * is closed.</p>
      *
      * @param file the file to open
      * @throws IOException
      */
     public void openFile(File file, String password) throws IOException {
         // first open the file for random access
         RandomAccessFile raf = new RandomAccessFile(file, "r");
 
         // extract a file channel
         FileChannel channel = raf.getChannel();
 
         // now memory-map a byte-buffer
         ByteBuffer bb =
                 ByteBuffer.NEW(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
         // create a PDFFile from the data
         if (password == null)
         	mPdfFile = new PDFFile(bb);
         else
         	mPdfFile = new PDFFile(bb, new PDFPassword(password));
 	        
         mGraphView.showText("Anzahl Seiten:" + mPdfFile.getNumPages());
     }
     
      
     /*private byte[] readBytes(File srcFile) throws IOException {
     	long fileLength = srcFile.length();
     	int len = (int)fileLength;
     	byte[] result = new byte[len];
     	FileInputStream fis = new FileInputStream(srcFile);
     	int pos = 0;
 		int cnt = fis.read(result, pos, len-pos);
     	while (cnt > 0) {
     		pos += cnt;
     		cnt = fis.read(result, pos, len-pos);
     	}
 		return result;
 	}*/
 
 	private String storeUriContentToFile(Uri uri) {
     	String result = null;
     	try {
 	    	if (mTmpFile == null) {
 				File root = Environment.getExternalStorageDirectory();
 				if (root == null)
 					throw new Exception("external storage dir not found");
 				mTmpFile = new File(root,"AndroidPdfViewer/AndroidPdfViewer_temp.pdf");
 				mTmpFile.getParentFile().mkdirs();
 	    		mTmpFile.delete();
 	    	}
 	    	else {
 	    		mTmpFile.delete();
 	    	}
 	    	InputStream is = getContentResolver().openInputStream(uri);
 	    	OutputStream os = new FileOutputStream(mTmpFile);
 	    	byte[] buf = new byte[1024];
 	    	int cnt = is.read(buf);
 	    	while (cnt > 0) {
 	    		os.write(buf, 0, cnt);
 		    	cnt = is.read(buf);
 	    	}
 	    	os.close();
 	    	is.close();
 	    	result = mTmpFile.getCanonicalPath();
 	    	mTmpFile.deleteOnExit();
     	}
     	catch (Exception e) {
     		Log.e(TAG, e.getMessage(), e);
 		}
 		return result;
 	}
 
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	mAllShareService.stop();
     	if (mTmpFile != null) {
     		mTmpFile.delete();
     		mTmpFile = null;
     	}
     }
 
     /*private void postHideNavigation() {
     	// Start a time to hide the panel after 3 seconds
     	closeNavigationHandler.removeCallbacks(closeNavigationThread);
     	closeNavigationHandler.postDelayed(closeNavigationThread, 3000);
     }*/
 
     public abstract int getPreviousPageImageResource(); // R.drawable.left_arrow
     public abstract int getNextPageImageResource(); // R.drawable.right_arrow
     public abstract int getZoomInImageResource(); // R.drawable.zoom_int
     public abstract int getZoomOutImageResource(); // R.drawable.zoom_out
     
     public abstract int getPdfPasswordLayoutResource(); // R.layout.pdf_file_password
     public abstract int getPdfPageNumberResource(); // R.layout.dialog_pagenumber
     
     public abstract int getPdfPasswordEditField(); // R.id.etPassword
     public abstract int getPdfPasswordOkButton(); // R.id.btOK
     public abstract int getPdfPasswordExitButton(); // R.id.btExit
     public abstract int getPdfPageNumberEditField(); // R.id.pagenum_edit
 }
