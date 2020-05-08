 package com.bignerdranch.android.photogallery;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
import com.bignerdranch.android.photogallery.picasso.R;
 import com.squareup.picasso.Picasso;
 
 import java.util.ArrayList;
 
 public class PhotoGalleryFragment extends Fragment {
 
     private GridView mGridView;
     private ArrayList<GalleryItem> mItems;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setRetainInstance(true);
         new FetchItemsTask().execute();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
         
         mGridView = (GridView)v.findViewById(R.id.gridView);
         
         setupAdapter();
         
         return v;
     }
 
     void setupAdapter() {
         if (getActivity() == null || mGridView == null) return;
         
         if (mItems != null) {
             mGridView.setAdapter(new GalleryItemAdapter(mItems));
         } else {
             mGridView.setAdapter(null);
         }
     }
 
     private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
         @Override
         protected ArrayList<GalleryItem> doInBackground(Void... params) {
             return new FlickrFetchr().fetchItems();
         }
 
         @Override
         protected void onPostExecute(ArrayList<GalleryItem> items) {
             mItems = items;
             setupAdapter();
         }
     }
     
     private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
         public GalleryItemAdapter(ArrayList<GalleryItem> items) {
             super(getActivity(), 0, items);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = getActivity().getLayoutInflater()
                         .inflate(R.layout.gallery_item, parent, false);
             }
             
             GalleryItem item = getItem(position);
             ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
 
             Picasso.with(getContext())
                     .load(item.getUrl())
                     .placeholder(R.drawable.brian_up_close)
                     .noFade()
                     .into(imageView);
 
             return convertView;
         }
     }
 }
