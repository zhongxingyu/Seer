 package de.raptor2101.GalDroid.Activities.Views;
 
 import java.lang.ref.WeakReference;
 import java.text.DateFormat;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Activities.GalDroidApp;
 import de.raptor2101.GalDroid.WebGallery.DegMinSec;
 import de.raptor2101.GalDroid.WebGallery.ImageInformation;
 import de.raptor2101.GalDroid.WebGallery.ImageInformation.WhiteBalance;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageInformationLoaderTaskListener;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageInformationLoaderTask;
 
 public class ImageInformationView extends TableLayout implements ImageInformationLoaderTaskListener, GalleryImageViewListener {
   private final static String CLASS_TAG = "ImageInformationView";
 
   private ImageInformationLoaderTask mLoadImageInformationTask;
 
   private GalleryImageView mCurrentListenedImageView;
   private WeakReference<GalleryImageView> mCurrentSelectedImageView;
 
   private ProgressBar mProgressBarTags;
 
   private ProgressBar mProgressBarComments;
 
   private ViewGroup mViewComments;
 
   private TextView mViewTags;
 
   private boolean mImageInformationsLoaded;
 
   private boolean mTagsLoaded;
 
   private boolean mCommentsLoaded;
 
   private GalleryObject mCurrentLoadingObject;
 
   public ImageInformationView(Context context, AttributeSet attrs) {
     super(context, attrs);
     initialize(context);
   }
 
   public ImageInformationView(Context context) {
     super(context);
     initialize(context);
   }
 
   private void initialize(Context context) {
     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     inflater.inflate(R.layout.image_information_view, this);
 
     mProgressBarTags = (ProgressBar) findViewById(R.id.progressBarTags);
     mProgressBarComments = (ProgressBar) findViewById(R.id.progressBarComments);
     mViewComments = (ViewGroup) findViewById(R.id.layoutComments);
     mViewTags = (TextView) findViewById(R.id.textTags);
 
     mCurrentSelectedImageView = new WeakReference<GalleryImageView>(null);
   }
 
   @Override
   protected void onDetachedFromWindow() {
     super.onDetachedFromWindow();
     
     mCurrentListenedImageView = null;
     
     try {
       mLoadImageInformationTask.cancel(true);
     } catch (InterruptedException e) {
       
     }
    mLoadImageInformationTask = null;
   }
   
   public void setGalleryImageView(GalleryImageView imageView) {
     GalleryObject requestedObject = imageView.getGalleryObject();
     Log.d(CLASS_TAG, String.format("setGalleryImageView: %s", requestedObject));
 
     GalleryImageView currentView = mCurrentSelectedImageView.get();
     if (currentView != null && currentView.getGalleryObject().equals(requestedObject)) {
       Log.d(CLASS_TAG, "extractImageInformation: view already selected!");
       return;
     }
 
     mCurrentSelectedImageView = new WeakReference<GalleryImageView>(imageView);
     if (getVisibility() == VISIBLE) {
       extractImageInformation(imageView);
     }
   }
 
   private void extractImageInformation(GalleryImageView imageView) {
     GalleryObject requestedObject = imageView.getGalleryObject();
     Log.d(CLASS_TAG, String.format("extractImageInformation: %s", requestedObject));
 
     if (mCurrentLoadingObject != null) {
       mLoadImageInformationTask.cancel(mCurrentLoadingObject);
     }
 
     clearImageInformations();
 
     mCommentsLoaded = false;
     mTagsLoaded = false;
     mImageInformationsLoaded = false;
 
     if (imageView.isLoaded()) {
       enqueueLoadingTask(requestedObject);
     } else {
       registerLoadingListener(imageView);
     }
   }
 
   private void enqueueLoadingTask(GalleryObject galleryObject) {
     Log.d(CLASS_TAG, String.format("enqueueExtrationTask: %s", galleryObject));
     mCurrentLoadingObject = galleryObject;
     mLoadImageInformationTask.load(galleryObject);
   }
 
   private void registerLoadingListener(GalleryImageView imageView) {
     Log.d(CLASS_TAG, String.format("registerLoadingListener: %s", imageView.getGalleryObject()));
     if (mCurrentListenedImageView != null) {
       mCurrentListenedImageView.setListener(null);
     }
 
     mCurrentListenedImageView = imageView;
     mCurrentListenedImageView.setListener(this);
   }
 
   @Override
   public void setVisibility(int visibility) {
     Log.d(CLASS_TAG, "setVisibility");
     GalleryImageView imageView = mCurrentSelectedImageView.get();
     if (visibility == VISIBLE && imageView != null) {
       extractImageInformation(imageView);
     }
 
     super.setVisibility(visibility);
   }
 
   public void initialize() {
     GalDroidApp app = (GalDroidApp) getContext().getApplicationContext();
     mLoadImageInformationTask = new ImageInformationLoaderTask(this, app.getWebGallery(), app.getImageCache());
 
     mCommentsLoaded = false;
     mTagsLoaded = false;
     mImageInformationsLoaded = false;
 
     mLoadImageInformationTask.start();
   }
 
   private void clearImageInformations() {
     mViewTags.setText("");
     mViewTags.setVisibility(View.GONE);
     
     mViewComments.removeAllViews();
     mViewComments.setVisibility(View.GONE);
     
     TextView textView = (TextView) findViewById(R.id.textTitle);
     textView.setText("");
 
     
     textView = (TextView) findViewById(R.id.textUploadDate);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifCreateDate);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifAperture);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifExposure);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifFlash);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifISO);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifModel);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifModel);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifMake);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifFocalLength);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textExifWhiteBalance);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textGeoLat);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textGeoLong);
     textView.setText("");
 
     textView = (TextView) findViewById(R.id.textGeoHeight);
     textView.setText("");
     
     mProgressBarComments.setVisibility(GONE);
     mProgressBarTags.setVisibility(GONE);
   }
 
   public void onImageInformationLoaded(GalleryObject galleryObject, ImageInformation info) {
     Log.d(CLASS_TAG, String.format("ImageInformation loaded for %s", galleryObject));
     TextView textField = (TextView) findViewById(R.id.textTitle);
     textField.setText(info.mTitle);
 
     textField = (TextView) findViewById(R.id.textUploadDate);
     textField.setText(info.mUploadDate.toLocaleString());
 
     textField = (TextView) findViewById(R.id.textExifCreateDate);
     textField.setText(info.mExifCreateDate);
 
     textField = (TextView) findViewById(R.id.textExifAperture);
     textField.setText(info.mExifAperture);
 
     textField = (TextView) findViewById(R.id.textExifExposure);
 
     textField.setText(String.format("1/%.0fs", info.mExifExposure));
 
     textField = (TextView) findViewById(R.id.textExifFlash);
     textField.setText(String.format("%d", info.mExifFlash));
 
     textField = (TextView) findViewById(R.id.textExifISO);
     textField.setText(info.mExifIso);
 
     textField = (TextView) findViewById(R.id.textExifModel);
     textField.setText(info.mExifModel);
 
     textField = (TextView) findViewById(R.id.textExifMake);
     textField.setText(info.mExifMake);
 
     textField = (TextView) findViewById(R.id.textExifFocalLength);
     textField.setText(String.format("%.0fmm", info.mExifFocalLength));
 
     textField = (TextView) findViewById(R.id.textExifWhiteBalance);
     if (info.mExifWhiteBalance == WhiteBalance.Auto) {
       textField.setText(R.string.object_exif_whitebalance_auto);
     } else {
       textField.setText(R.string.object_exif_whitebalance_manual);
     }
 
     DegMinSec val = info.mExifGpsLat;
     textField = (TextView) findViewById(R.id.textGeoLat);
     textField.setText(String.format("%.0f° %.0f' %.2f\"", val.mDeg, val.mMin, val.mSec));
 
     val = info.mExifGpsLong;
     textField = (TextView) findViewById(R.id.textGeoLong);
     textField.setText(String.format("%.0f° %.0f' %.2f\"", val.mDeg, val.mMin, val.mSec));
 
     textField = (TextView) findViewById(R.id.textGeoHeight);
     textField.setText(String.format("%.0f", info.mExifHeight));
 
     mProgressBarComments.setVisibility(VISIBLE);
     mProgressBarTags.setVisibility(VISIBLE);
     mImageInformationsLoaded = true;
 
     resetCurrentLoadingImage();
   }
 
   public void onImageTagsLoaded(GalleryObject galleryObject, List<String> tags) {
     Log.d(CLASS_TAG, String.format("Tags (Count: %d) loaded for %s", tags.size(), galleryObject));
     StringBuilder stringBuilder = new StringBuilder(tags.size() * 10);
     for (String tag : tags) {
       stringBuilder.append(String.format("%s, ", tag));
     }
     int length = stringBuilder.length();
     if (length > 0) {
       stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
     }
 
     mViewTags.setText(stringBuilder);
 
     mProgressBarTags.setVisibility(View.GONE);
     mViewTags.setVisibility(View.VISIBLE);
     mTagsLoaded = true;
 
     resetCurrentLoadingImage();
   }
 
   public void onImageCommetsLoaded(GalleryObject galleryObject, List<GalleryObjectComment> comments) {
     Log.d(CLASS_TAG, String.format("Comments (Count: %d) loaded for %s", comments.size(), galleryObject));
     Context context = getContext();
     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
     DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
 
     for (GalleryObjectComment comment : comments) {
       View commentView = inflater.inflate(R.layout.comment_entry, null);
 
       TextView textAuthor = (TextView) commentView.findViewById(R.id.textCommentAuthor);
       TextView textDate = (TextView) commentView.findViewById(R.id.textCommentPosted);
       TextView textMessage = (TextView) commentView.findViewById(R.id.textCommentMessage);
 
       textAuthor.setText(comment.getAuthorName());
       textDate.setText(dateFormat.format(comment.getCreateDate()));
       textMessage.setText(comment.getMessage());
 
       LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
       params.setMargins(0, 0, 0, 10);
       commentView.setLayoutParams(params);
 
       mViewComments.addView(commentView);
     }
     mViewComments.setVisibility(View.VISIBLE);
     mProgressBarComments.setVisibility(GONE);
     mCommentsLoaded = true;
 
     resetCurrentLoadingImage();
   }
 
   private void resetCurrentLoadingImage() {
     if (isLoaded()) {
       mCurrentLoadingObject = null;
     }
 
   }
 
   public ImageInformationLoaderTask getImageInformationLoaderTask() {
     return mLoadImageInformationTask;
   }
 
   public void onLoadingStarted(GalleryObject galleryObject) {
     // TODO Auto-generated method stub
 
   }
 
   public void onLoadingProgress(GalleryObject galleryObject, int currentValue, int maxValue) {
     // TODO Auto-generated method stub
 
   }
 
   public void onLoadingCompleted(GalleryObject galleryObject) {
     enqueueLoadingTask(galleryObject);
     mCurrentListenedImageView.setListener(null);
     mCurrentListenedImageView = null;
   }
 
   public void onLoadingCancelled(GalleryObject galleryObject) {
     // TODO Auto-generated method stub
 
   }
 
   public boolean isLoaded() {
     return mImageInformationsLoaded && mTagsLoaded && mCommentsLoaded;
   }
 
   public boolean areImageInformationsLoaded() {
     return mImageInformationsLoaded;
   }
 
   public boolean areTagsLoaded() {
     return mTagsLoaded;
   }
 
   public boolean areCommentsLoaded() {
     return mCommentsLoaded;
   }
 
   public GalleryImageView getCurrentListenedImageView() {
     return mCurrentListenedImageView;
   }
 
   public GalleryObject getCurrentLoadingObject() {
     return mCurrentLoadingObject;
   }
 }
