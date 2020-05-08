 package com.tzapps.tzpalette.ui;
 
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 import com.tzapps.common.ui.BaseFragment;
 import com.tzapps.common.utils.BitmapUtils;
 import com.tzapps.common.utils.ColorUtils;
 import com.tzapps.common.utils.MediaHelper;
 import com.tzapps.tzpalette.Constants;
 import com.tzapps.tzpalette.R;
 import com.tzapps.tzpalette.data.PaletteData;
 import com.tzapps.tzpalette.data.PaletteDataHelper;
 import com.tzapps.tzpalette.debug.MyDebug;
 import com.tzapps.tzpalette.ui.view.ColorEditView;
 import com.tzapps.tzpalette.ui.view.ColorImageView;
 import com.tzapps.tzpalette.ui.view.ColorImageView.OnColorImageClickListener;
 import com.tzapps.tzpalette.ui.view.ColorRow;
 
 public class PaletteEditFragment extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, OnColorImageClickListener
 {
     private static final String TAG = "PaletteEditFragment";
     
     private PaletteData mData;
     
     private View mView;
     private ColorImageView mImageView;
     private ColorRow mColoursRow;
     private View mColorsBar;
     private TextView mTitle;
     private CheckBox mFavourite;
     private ColorEditView mColorEditView;
     
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         Log.d(TAG, "onCreateView()");
         
         setRetainInstance(true);
         
         // Inflate the layout for this fragment
         mView = inflater.inflate(R.layout.palette_edit_view, container, false);
         
         mImageView = (ColorImageView) mView.findViewById(R.id.palette_edit_view_picture);
         mImageView.setOnColorImageClickListener(this);
         
         mColorsBar = (View) mView.findViewById(R.id.palette_edit_view_colors_bar);
         mTitle = (TextView) mView.findViewById(R.id.palette_edit_view_title);
         mFavourite = (CheckBox) mView.findViewById(R.id.palette_edit_view_favourite);
         
         mColoursRow = (ColorRow) mView.findViewById(R.id.palette_edit_view_colors);
         mColoursRow.setOnItemClickListener(this);
         mColoursRow.setOnItemLongClickListener(this);
         
         mColorEditView = (ColorEditView) mView.findViewById(R.id.palette_edit_view_color_edit_area);
         
         refresh(true);
         
         return mView;
     }
     
     public PaletteData getData()
     {
         return mData;
     }
     
     public void refresh(boolean updatePicture)
     {
         if (mData == null)
             return;
         
         mTitle.setText(mData.getTitle());
         mFavourite.setChecked(mData.isFavourite());
         mColoursRow.setColors(mData.getColors());
         
         if (mData.getColors().length != 0)
             showColorsBar();
 
         if (updatePicture)
         {
             Bitmap bitmap    = null;
             
             bitmap = PaletteDataHelper.getInstance(getActivity()).getThumb(mData.getId());
             
             if (bitmap == null)
             {
                 String imagePath = mData.getImageUrl();
                 Uri    imageUri  = imagePath == null ? null : Uri.parse(imagePath);
                 
                 bitmap = BitmapUtils.getBitmapFromUri(getActivity(), imageUri, Constants.THUMB_MAX_SIZE);
                 
                 if (bitmap != null)
                 {
                     int orientation;
                     
                     /*
                      * This is a quick fix on picture orientation for the picture taken
                      * from the camera, as it will be always rotated to landscape 
                      * incorrectly even if we take it in portrait mode...
                      */
                     orientation = MediaHelper.getPictureOrientation(getActivity(), imageUri);
                     bitmap = BitmapUtils.getRotatedBitmap(bitmap, orientation);
                 } 
             }
             
             updateImageView(bitmap);
         }
     }
     
     public void updateData(PaletteData data, boolean updatePicture)
     {
         mData = data;
         refresh(updatePicture);
     }
     
     public void updateImageView(Bitmap bitmap)
     {
         Log.d(TAG, "updateImageView");
         
         if (mImageView == null)
             return;
         
         mImageView.setImageBitmap(bitmap);
     }
 
     public void updateColors(int[] colors)
     {
         if (MyDebug.LOG)
             Log.d(TAG, "updateColors");
         
         mData.addColors(colors, false);
         
         if (colors.length != 0)
             showColorsBar();
         
         refresh(false);
     }
     
     public void updateTitle(String title)
     {
         if (MyDebug.LOG)
             Log.d(TAG, "update title " + title);
         
         mData.setTitle(title);
         showTitleBar();
     }
     
     public void updateFavourite(boolean favourite)
     {
         if (MyDebug.LOG)
             Log.d(TAG, "update favourite" + favourite);
         
         mData.setFavourite(favourite);
         refresh(false);
     }
     
     public void addNewColorIntoColorsBar()
     {
         int newColor = mColorEditView.getNewColor();
         
         mData.addColor(newColor);
         mColoursRow.addColor(newColor);
     }
     
     private void showColorsBar()
     {
         if (mColorsBar.getVisibility() != View.VISIBLE)
         {
             mColorsBar.setVisibility(View.VISIBLE);
             
             Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_anim);
             mColorsBar.startAnimation(anim);
         }
     }
     
     private void showTitleBar()
     {
         if (mTitle.getVisibility() != View.VISIBLE)
         {
             mTitle.setVisibility(View.VISIBLE);
             
             Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_anim);
             mTitle.startAnimation(anim);
         }
     }
     
     @Override
     public void onColorImageClicked(ColorImageView view, int xPos, int yPos, int color)
     {
         Log.d(TAG, "image clicked at x=" + xPos + " y=" + yPos + " color=" + ColorUtils.colorToHtml(color));
         
         mColorEditView.setColor(color);
     }
 
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id)
     {
         int color = mColoursRow.getColor(position);
         
         mColorEditView.setColor(color);
     }
     
     @Override
     public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
     {
         //long click to remove this color from palette data and colors bar
         
         int color = mColoursRow.getColor(position);
         mColoursRow.removeColor(color);
         mData.removeColor(color);
         
         return true;
     }
 
 }
 
