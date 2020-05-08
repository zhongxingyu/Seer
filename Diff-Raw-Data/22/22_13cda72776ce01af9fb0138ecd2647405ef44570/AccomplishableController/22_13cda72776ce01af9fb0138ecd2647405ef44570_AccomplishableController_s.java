 package com.strollimo.android.controller;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.android.volley.toolbox.ImageRequest;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.model.Mystery;
 import com.strollimo.android.model.Secret;
 import com.strollimo.android.network.AmazonUrl;
 import com.strollimo.android.network.StrollimoApi;
 import com.strollimo.android.network.response.GetMysteriesResponse;
 import com.strollimo.android.network.response.GetSecretsResponse;
 import com.strollimo.android.network.response.UpdateMysteryResponse;
 import com.strollimo.android.network.response.UpdateSecretResponse;
 import retrofit.Callback;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 
 import java.text.ParseException;
 import java.util.*;
 
 public class AccomplishableController {
     private static final String TAG = AccomplishableController.class.getSimpleName();
 
     private final StrollimoPreferences mPrefs;
     private final PhotoUploadController mPhotoUploadController;
     private final StrollimoApi mStrollimoApi;
     private Map<String, Mystery> mMysteries;
     private Context mContext;
     private LinkedHashMap<String, Secret> mSecrets = new LinkedHashMap<String, Secret>();
 
     public AccomplishableController(Context context, StrollimoPreferences prefs,
                                     PhotoUploadController photoUploadController, StrollimoApi strollimoApi) {
         mContext = context;
         mMysteries = new HashMap<String, Mystery>();
         mPrefs = prefs;
         mPhotoUploadController = photoUploadController;
         mStrollimoApi = strollimoApi;
         preloadPlaces();
     }
 
     public void preloadPlaces() {
         List<Mystery> mysteries = mPrefs.getMysteries();
         if (mysteries != null) {
             for (Mystery mystery : mysteries) {
                 addMystery(mystery);
                 Mystery myMystery = getMysteryById(mystery.getId());
                 for (String secretId : mystery.getChildren()) {
                     Secret secret = mPrefs.getSecret(secretId);
                     myMystery.addChild(secretId);
                     addSecret(secret, myMystery);
                 }
             }
         }
        preloadImages(new ArrayList<Mystery>(mMysteries.values()));
     }
 
    private void preloadImages(List<Mystery> misteries){
        for (Mystery mystery : misteries) {
            ImageRequest imageRequest = new ImageRequest(mystery.getImgUrl(), null, 0, 0, Bitmap.Config.RGB_565, null);
            VolleyRequestQueue.getInstance().add(imageRequest);
        }
    }
 
     public void asyncSyncMysteries(final String env, final OperationCallback callback) {
         new AsyncTask<Void, Void, Void>(){
 
             @Override
             protected Void doInBackground(Void... voids) {
                 GetMysteriesResponse getMysteriesResponse = mStrollimoApi.getMysteries(env);
                 if (getMysteriesResponse!= null && "success".equals(getMysteriesResponse.getState())) {
                     for (Mystery mystery : getMysteriesResponse.getBody()) {
                         addMystery(mystery);
                         syncSecrets(mystery);
                         saveAllData();
                     }
                     if (callback != null) {
                         callback.onSuccess();
                     }
                 } else {
                     if (callback != null) {
                         callback.onError("Error while getting mysteries");
                     }
                 }
                 return null;
             }
         }.execute();
     }
 
     private void syncSecrets(final Mystery mystery) {
         GetSecretsResponse getSecretsResponse = mStrollimoApi.getSecrets(mystery.getId());
         if ("success".equals(getSecretsResponse.getState())) {
             for (Secret secret : getSecretsResponse.getBody()) {
                 addSecret(secret, mystery);
             }
         } else {
             // TODO: we need something stronger than this :)
             Log.i(TAG, "Error while getting secrets");
         }
     }
 
     public void clearMysteries() {
         for (Secret secret : mSecrets.values()) {
             if (secret != null) {
                 mPrefs.clearSecret(secret.getId());
             }
         }
         mPrefs.clearMysteries();
         mSecrets.clear();
         mMysteries.clear();
     }
 
     public Mystery getFirstMystery() {
         if (mMysteries.size() > 0) {
             return mMysteries.values().iterator().next();
         } else {
             return null;
         }
     }
 
 //    private void preloadImages(final List<Mystery> mysteries) {
 //        new Thread(new Runnable() {
 //
 //            @Override
 //            public void run() {
 //                for (Mystery mystery : mysteries) {
 //                    if (!TextUtils.isEmpty(mystery.getImgUrl())) {
 //                        mImageManager.cacheImage(mystery.getImgUrl(), 800, 600);
 //                    }
 //                }
 //            }
 //        }).start();
 //    }
 
     public void start() {
 
     }
 
     public void addMystery(Mystery mystery) {
         mMysteries.put(mystery.getId(), mystery);
     }
 
     public void asynUploadMystery(final Mystery mystery, Bitmap photo, final OperationCallback callback) {
         AmazonUrl amazonUrl = null;
         try {
             amazonUrl = AmazonUrl.fromUrl(mystery.getImgUrl());
         } catch (ParseException e) {
             if (callback != null) {
                 callback.onError("Image URL is not an valid Amazon URL");
             }
         }
 
         // TODO save image in cache
         // mImageManager.getCacheManager().put(mystery.getImgUrl(), photo);
         mPhotoUploadController.asyncUploadPhotoToAmazon(amazonUrl, photo, new PhotoUploadController.Callback() {
             @Override
             public void onSuccess() {
                 mStrollimoApi.updateMystery(mystery, new Callback<UpdateMysteryResponse>() {
                     @Override
                     public void success(UpdateMysteryResponse updateMysteryResponse, Response response) {
                         if ("success".equals(updateMysteryResponse.getState())) {
                             addMystery(mystery);
                             saveAllData();
                             if (callback != null) {
                                 callback.onSuccess();
                             }
                         } else {
                             if (callback != null) {
                                 callback.onError("Uploading to strollimo server failed with status: " + updateMysteryResponse.getState());
                             }
                         }
                     }
 
                     @Override
                     public void failure(RetrofitError retrofitError) {
                         if (callback != null) {
                             callback.onSuccess();
                         }
                     }
                 });
             }
 
             @Override
             public void onError(Exception ex) {
                 if (callback != null) {
                     callback.onError("Uploading photo failed");
                 }
             }
         });
 
     }
 
     public void asynUploadSecret(final Secret secret, final Mystery mystery, Bitmap photo, final OperationCallback callback) {
         AmazonUrl amazonUrl = null;
         try {
             amazonUrl = AmazonUrl.fromUrl(secret.getImgUrl());
         } catch (ParseException e) {
             if (callback != null) {
                 callback.onError("Image URL is not an valid Amazon URL");
             }
         }
 
         //TODO save image in cache
         //mImageManager.getCacheManager().put(secret.getImgUrl(), photo);
         mPhotoUploadController.asyncUploadPhotoToAmazon(amazonUrl, photo, new PhotoUploadController.Callback() {
             @Override
             public void onSuccess() {
                 addSecret(secret, mystery);
                 saveAllData();
 
                 mStrollimoApi.updateMystery(mystery, new Callback<UpdateMysteryResponse>() {
                     @Override
                     public void success(UpdateMysteryResponse updateMysteryResponse, Response response) {
                         if ("success".equals(updateMysteryResponse.getState())) {
                             mStrollimoApi.updateSecret(secret, new Callback<UpdateSecretResponse>() {
 
                                 @Override
                                 public void success(UpdateSecretResponse updateSecretResponse, Response response) {
                                     if ("success".equals(updateSecretResponse.getState())) {
                                         if (callback != null) {
                                             callback.onSuccess();
                                         }
                                     } else {
                                         if (callback != null) {
                                             callback.onError("Uploading to strollimo server failed with status: " + updateSecretResponse.getState());
                                         }
                                     }
                                 }
 
                                 @Override
                                 public void failure(RetrofitError retrofitError) {
                                     if (callback != null) {
                                         callback.onSuccess();
                                     }
                                 }
                             });
 
                         } else {
                             if (callback != null) {
                                 callback.onError("Uploading to strollimo server failed with status: " + updateMysteryResponse.getState());
                             }
                         }
                     }
 
                     @Override
                     public void failure(RetrofitError retrofitError) {
                         if (callback != null) {
                             callback.onSuccess();
                         }
                     }
                 });
             }
 
             @Override
             public void onError(Exception ex) {
                 if (callback != null) {
                     callback.onError("Uploading photo failed");
                 }
             }
         });
 
     }
 
     public void addSecret(Secret secret, Mystery mystery) {
         mSecrets.put(secret.getId(), secret);
         mystery.addChild(secret.getId());
     }
 
     public Secret getSecretById(String id) {
         return mSecrets.get(id);
     }
 
     public Mystery getMysteryById(String id) {
         return mMysteries.get(id);
     }
 
     public Map<String, Secret> getAllSecrets() {
         return mSecrets;
     }
 
     public List<Mystery> getAllMysteries() {
         return new ArrayList<Mystery>(mMysteries.values());
     }
 
     public int getMysteriesCount() {
         return mMysteries.size();
     }
 
     public void saveAllData() {
         mPrefs.saveMissions(getAllMysteries(), getAllSecrets());
     }
 
     public interface OperationCallback {
         void onSuccess();
 
         void onError(String errorMsg);
     }
 }
