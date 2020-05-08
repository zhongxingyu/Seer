 package com.audio.unicorn.view.gallery;
 
 import java.util.List;
 
 import android.content.ClipData;
 import android.content.ClipDescription;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.audio.unicorn.R;
 import com.audio.unicorn.dragdrop.DragDropUtil;
 import com.audio.unicorn.media.Track;
 
 public class TrackGalleryView extends ViewPager implements OnTouchListener {
 
     private static final long LONG_PRESS_TIME_OUT = 200;
 
     public interface OnViewInstantiatedListener {
         void onViewInstantiated(ImageView view, long albumId);
     }
 
     private OnViewInstantiatedListener mListener;
     private Runnable mLongPressRunnable;
     private float mOriginX;
     private float mOriginY;
     private int mTouchSlopSquare;
 
     public TrackGalleryView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init(context);
     }
 
     private void init(Context context) {
         setPageMargin((int) (5 * getResources().getDisplayMetrics().density + 0.5f));
         int scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
         mTouchSlopSquare = scaledTouchSlop * scaledTouchSlop;
     }
 
     public void setListener(OnViewInstantiatedListener listener) {
         mListener = listener;
     }
 
     public void setTracks(List<Track> tracks) {
         setAdapter(new TrackPagerAdapter(tracks));
     }
 
     public class TrackPagerAdapter extends PagerAdapter {
 
         private List<Track> mTracks;
 
         public TrackPagerAdapter(List<Track> tracks) {
             mTracks = tracks;
         }
 
         @Override
         public int getCount() {
             return mTracks.size();
         }
 
         @Override
         public Object instantiateItem(ViewGroup container, int position) {
             View view = LayoutInflater.from(getContext()).inflate(R.layout.track_gallery_item_view, container, false);
             ImageView imageView = (ImageView) view.findViewById(R.id.ImageView);
             TextView titleText = (TextView) view.findViewById(R.id.TitleText);
             titleText.setText(mTracks.get(position).getTitle());
             imageView.setImageResource(R.drawable.ic_launcher);
             container.addView(view);
             if (mListener != null) {
                 mListener.onViewInstantiated(imageView, mTracks.get(position).getAlbumId());
             }
             view.setTag(mTracks.get(position));
             view.setOnTouchListener(TrackGalleryView.this);
             return view;
         }
 
         @Override
         public int getItemPosition(Object object) {
             return POSITION_NONE;
         }
 
         @Override
         public void destroyItem(ViewGroup container, int position, Object object) {
             container.removeView((View) object);
         }
 
         @Override
         public float getPageWidth(int position) {
             return (float) getMeasuredHeight() / getMeasuredWidth();
         }
 
         @Override
         public boolean isViewFromObject(View view, Object object) {
             return view == object;
         }
 
     }
 
     @Override
     public boolean onTouch(final View v, MotionEvent event) {
         // Need to override onTouch to customize the long press delay.
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             mOriginX = event.getX();
             mOriginY = event.getY();
             if (mLongPressRunnable != null) {
                 getHandler().removeCallbacks(mLongPressRunnable);
             }
             mLongPressRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     startDrag(v);
                 }
 
             };
             getHandler().postDelayed(mLongPressRunnable, LONG_PRESS_TIME_OUT);
             return true;
         } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
             final int deltaX = (int) (event.getX() - mOriginX);
             final int deltaY = (int) (event.getY() - mOriginY);
             int distance = (deltaX * deltaX) + (deltaY * deltaY);
             if (distance > mTouchSlopSquare) {
                 getHandler().removeCallbacks(mLongPressRunnable);
             }
 
         } else {
             if (mLongPressRunnable != null) {
                 getHandler().removeCallbacks(mLongPressRunnable);
             }
         }
         return false;
     }
 
     private void startDrag(View v) {
         Log.d("TEST", "start drag");
         Intent intent = new Intent();
         Track track = (Track) v.getTag();
         intent.putExtra(DragDropUtil.EXTRA_ALBUM_ID, track.getAlbumId());
         intent.putExtra(DragDropUtil.EXTRA_TRACK_ID, track.getTrackId());
         intent.putExtra(DragDropUtil.EXTRA_TITLE, track.getTitle());
         intent.putExtra(DragDropUtil.EXTRA_FILE_PATH, track.getFilePath());
         ClipData.Item item = new ClipData.Item(intent);
         ClipData data = new ClipData("TEST", new String[] { ClipDescription.MIMETYPE_TEXT_INTENT }, item);
         View.DragShadowBuilder shadow = new DragShadowBuilder(v);
         v.startDrag(data, shadow, null, 0);
     }
 }
