 
 package com.twitter.android.yamba;
 
 import static com.twitter.android.yambacontract.TimelineContract.Columns.CREATED_AT;
 import static com.twitter.android.yambacontract.TimelineContract.Columns.LATITUDE;
 import static com.twitter.android.yambacontract.TimelineContract.Columns.LONGITUDE;
 import static com.twitter.android.yambacontract.TimelineContract.Columns.MESSAGE;
 import static com.twitter.android.yambacontract.TimelineContract.Columns.USER;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Fragment;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.database.Cursor;
 import android.location.Address;
 import android.location.Geocoder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class StatusDetailsFragment extends Fragment implements LoaderCallbacks<Cursor> {
     private static final String TAG = StatusDetailsFragment.class.getSimpleName();
 
     public static StatusDetailsFragment build(Uri uri) {
         StatusDetailsFragment statusDetailsFragment = new StatusDetailsFragment();
         Bundle args = new Bundle(1);
         args.putParcelable("uri", uri);
         statusDetailsFragment.setArguments(args);
         return statusDetailsFragment;
     }
 
     private TextView user;
 
     private TextView message;
 
     private TextView createdAt;
 
     private TextView location;
 
     private Geocoder geocoder;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_status_details, container, false);
         this.user = (TextView) view.findViewById(R.id.user);
         this.message = (TextView) view.findViewById(R.id.message);
         this.createdAt = (TextView) view.findViewById(R.id.created_at);
         this.location = (TextView) view.findViewById(R.id.location);
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
        this.geocoder = new Geocoder(super.getActivity());
         super.getLoaderManager().initLoader(0, null, this);
 
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         Log.d(TAG, "onCreateLoader()");
         Uri uri = super.getArguments().getParcelable("uri");
         return new CursorLoader(this.getActivity(), uri, null, null, null, null);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         if (data.moveToFirst()) {
             String user = data.getString(data.getColumnIndex(USER));
             String message = data.getString(data.getColumnIndex(MESSAGE));
             CharSequence createdAt = DateUtils.getRelativeDateTimeString(super.getActivity(),
                     data.getLong(data.getColumnIndex(CREATED_AT)), DateUtils.MINUTE_IN_MILLIS,
                     DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
             if (Geocoder.isPresent()) {
                 final int latitudeIndex = data.getColumnIndex(LATITUDE);
                 final int longitudeIndex = data.getColumnIndex(LONGITUDE);
                 if (!data.isNull(latitudeIndex) && !data.isNull(longitudeIndex)) {
                     double latitude = data.getDouble(latitudeIndex);
                     double longitude = data.getDouble(longitudeIndex);
                     try {
                         // TODO: do on a background thread
                         List<Address> addresses = this.geocoder.getFromLocation(latitude,
                                 longitude, 1);
                         if (addresses != null && !addresses.isEmpty()) {
                             this.location.setText(addresses.get(0).getLocality());
                         }
                     } catch (IOException e) {
                         Log.w(TAG, "Failed to geocode", e);
                     }
                 }
             } else {
                 Log.d(TAG, "No geocoder support");
             }
 
             this.user.setText(user);
             // TODO: make actionable
             this.message.setText(message);
             this.createdAt.setText(createdAt);
 
             // TODO: geo-code to location
 
         } else {
             Log.w(TAG, "No data!!!");
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
     }
 
 }
