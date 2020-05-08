 
 package com.example.myexamplecursorloader;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.example.myexamplecursorloader.data.Place;
 import com.example.myexamplecursorloader.data.PlaceManager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * CursorLoaderを使ってテーブルの詳細画面を表示します
  */
 public class PlaceDetailFragment extends Fragment implements
         LoaderManager.LoaderCallbacks<Cursor> {
 
     private List<Place> mPlace;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.place_detail, container, false);
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         getLoaderManager().initLoader(0, null, this);
     }
 
     @Override
     public void onDestroyView() {
         super.onDestroyView();
         getLoaderManager().destroyLoader(0);
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
         // 検索条件の指定
         Bundle b = this.getArguments();
         Uri uri = Uri.parse(PlaceManager.Place.CONTENT_URI + "/" + b.getString("place_id"));
         return new CursorLoader(this.getActivity(), uri, null, null, null, null);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 
        if (cursor == null) {
             mPlace = null;
             return;
         }
         mPlace = new ArrayList<Place>();
         if (cursor.moveToFirst()) {
             do {
                 Place place = new Place();
                 place.setID(cursor.getInt(0));
                 place.setPlaceID(cursor.getString(1));
                 place.setPlace(cursor.getString(2));
                 place.setUrl(cursor.getString(3));
                 // Adding to list
                 mPlace.add(place);
             } while (cursor.moveToNext());
         }
 
         viewSet();
     }
 
     private void viewSet() {
         Place place = mPlace.get(0);
 
         TextView view = (TextView) getView().findViewById(R.id.id);
         view.setText("データ詳細画面\n\nid=" + place.getID());
         TextView view2 = (TextView) getView().findViewById(R.id.place_id);
         view2.setText("place_id=" + place.getPlaceID());
         TextView view3 = (TextView) getView().findViewById(R.id.place);
         view3.setText("place=" + place.getPlace());
         TextView view4 = (TextView) getView().findViewById(R.id.url);
         view4.setText("url=" + place.getUrl());
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
     }
 
 }
