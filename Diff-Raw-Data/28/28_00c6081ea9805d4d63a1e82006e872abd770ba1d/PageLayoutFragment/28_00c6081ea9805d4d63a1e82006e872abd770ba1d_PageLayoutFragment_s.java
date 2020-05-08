 package com.shivgadhia.android.tomato.fragments;
 
 import android.content.Intent;
 import android.support.v4.app.Fragment;
 import android.view.View;
 import android.widget.ImageView;
 import com.novoda.imageloader.core.model.ImageTag;
 import com.novoda.imageloader.core.model.ImageTagFactory;
 import com.shivgadhia.android.tomato.TomatoApplication;
 import com.shivgadhia.android.tomato.activities.PhotoViewPagerActivity;
 import com.shivgadhia.android.tomato.models.ImageModel;
 
 import java.util.List;
 
 public abstract class PageLayoutFragment extends Fragment {
     protected List<ImageModel> images;
     ImageTagFactory imageTagFactory;
    private int pos;
 
     protected abstract int getLayout();
 
     public void setImages(List<ImageModel> images) {
         this.images = images;
     }
 
     protected void setImage(ImageView imageView, ImageModel imageModel) {
         try {
             ImageTag tag = imageTagFactory.build(imageModel.getSmallUrl());
             imageView.setTag(tag);
             TomatoApplication.getImageManager().getLoader().load(imageView);
 
             imageView.setOnClickListener(createClickListener(imageModel.getPostId()));
 
         } catch (IndexOutOfBoundsException e) {
             e.printStackTrace();
         }
     }
 
     private View.OnClickListener createClickListener(final String postId) {
         return new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent showPhoto = new Intent(getActivity(), PhotoViewPagerActivity.class);
                 showPhoto.putExtra(PhotoViewPagerActivity.EXTRA_POST_ID, postId);
                 startActivity(showPhoto);
             }
         };
     }
 
     protected void setPosition(int pos) {
        this.pos = pos;
     }
 }
