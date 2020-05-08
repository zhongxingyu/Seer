 package edu.mit.mobile.android.livingpostcards;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AnimationUtils;
 import android.widget.TextView;
 import edu.mit.mobile.android.content.ProviderUtils;
 import edu.mit.mobile.android.imagecache.ImageCache;
 import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
 import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
 import edu.mit.mobile.android.livingpostcards.data.Card;
 import edu.mit.mobile.android.locast.net.NetworkClient;
 import edu.mit.mobile.android.locast.sync.LocastSyncService;
 import edu.mit.mobile.android.maps.GoogleStaticMapView;
 import edu.mit.mobile.android.maps.OnMapUpdateListener;
 
 public class CardDetailsFragment extends Fragment implements LoaderCallbacks<Cursor>,
         OnImageLoadListener, OnMapUpdateListener {
 
     /**
      * The card URI
      */
     public static final String ARGUMENT_URI = "uri";
 
     private Uri mCard;
 
     private GoogleStaticMapView mStaticMap;
 
     private int mTiming;
 
     private Uri mCardMedia;
 
     private ImageCache mImageCache;
 
     private final static int LOADER_CARD = 100;
 
     private static final String[] CARD_PROJECTION = new String[] { Card._ID,
             Card.COL_ANIMATED_RENDER, Card.COL_DESCRIPTION, Card.COL_COVER_PHOTO, Card.COL_TIMING,
             Card.COL_LATITUDE, Card.COL_LONGITUDE, Card.COL_MEDIA_URL };
 
     private static final String TAG = CardDetailsFragment.class.getSimpleName();
 
     public static CardDetailsFragment newInstance(Uri card) {
         final CardDetailsFragment cmf = new CardDetailsFragment();
 
         final Bundle args = new Bundle();
         args.putParcelable(ARGUMENT_URI, card);
         cmf.setArguments(args);
         return cmf;
     }
 
     @Override
     public void onAttach(Activity activity) {
 
         super.onAttach(activity);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         mImageCache = ImageCache.getInstance(getActivity());
 
         if (getArguments() != null) {
             mCard = getArguments().getParcelable(ARGUMENT_URI);
 
             if (mCard != null) {
                 mCardMedia = Card.MEDIA.getUri(mCard);
                 getLoaderManager().initLoader(LOADER_CARD, null, this);
             }
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         final View v = inflater.inflate(R.layout.card_details_fragment, container, false);
 
         mStaticMap = (GoogleStaticMapView) v.findViewById(R.id.static_map);
 
         mStaticMap.setOnMapUpdateListener(this);
         return v;
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         mImageCache.registerOnImageLoadListener(this);
         LocastSyncService.startExpeditedAutomaticSync(getActivity(), mCard);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mImageCache.unregisterOnImageLoadListener(this);
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
         switch (id) {
             case LOADER_CARD:
                 return new CursorLoader(getActivity(), mCard, CARD_PROJECTION, null, null, null);
 
             default:
                 return null;
         }
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
         switch (loader.getId()) {
             case LOADER_CARD:
                 if (c.moveToFirst()) {
                     if (BuildConfig.DEBUG) {
                         ProviderUtils.dumpCursorToLog(c, CARD_PROJECTION);
                     }
                     final View v = getView();
                     final String description = c.getString(c
                             .getColumnIndexOrThrow(Card.COL_DESCRIPTION));
                     final TextView descriptionTv = ((TextView) v.findViewById(R.id.description));
                     if (description != null && description.length() > 0) {
                         descriptionTv.setVisibility(View.VISIBLE);
                         descriptionTv.setText(description);
                     } else {
                         descriptionTv.setVisibility(View.GONE);
                     }
 
                     mStaticMap.setMap(c.getFloat(c.getColumnIndexOrThrow(Card.COL_LATITUDE)),
                             c.getFloat(c.getColumnIndexOrThrow(Card.COL_LONGITUDE)), false);
 
                     mTiming = c.getInt(c.getColumnIndexOrThrow(Card.COL_TIMING));
                     final String pubMediaUri = c.getString(c
                             .getColumnIndexOrThrow(Card.COL_MEDIA_URL));
                     if (pubMediaUri != null) {
                         LocastSyncService.startSync(
                                 getActivity(),
                                 NetworkClient.getInstance(
                                         getActivity(),
                                         Authenticator.getFirstAccount(getActivity(),
                                                 Authenticator.ACCOUNT_TYPE))
                                         .getFullUrl(pubMediaUri), mCardMedia, false);
                     }
                 }
                 break;
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
 
     }
 
     @Override
     public void onMapUpdate(GoogleStaticMapView v, Uri staticMap) {
         Log.d(TAG, "scheduling load of " + staticMap);
         try {
             final Drawable d = mImageCache.loadImage(v.getId(), staticMap, v.getMapWidth(),
                     v.getMapHeight());
            if (BuildConfig.DEBUG && d != null) {
                Log.d(TAG, "set map image immediately");
            }
            setMapDrawable(d, false);
         } catch (final IOException e) {
             Log.e(TAG, "error updating static map", e);
         }
     }
 
    private void setMapDrawable(Drawable image, boolean animate) {

        mStaticMap.setImageDrawable(image);

        if (animate) {
            mStaticMap.startAnimation(AnimationUtils.makeInAnimation(getActivity(), true));
        }
        mStaticMap.setVisibility(View.VISIBLE);
    }

     @Override
     public void onImageLoaded(long id, Uri imageUri, Drawable image) {
         if (id == R.id.static_map) {
             if (BuildConfig.DEBUG) {
                 Log.d(TAG, "loaded " + imageUri + " (id " + id + ")");
             }
 
            setMapDrawable(image, true);
         }
     }
 }
