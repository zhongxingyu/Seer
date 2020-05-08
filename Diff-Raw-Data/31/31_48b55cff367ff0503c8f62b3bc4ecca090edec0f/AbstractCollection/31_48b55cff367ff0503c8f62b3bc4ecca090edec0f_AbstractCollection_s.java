 package com.tuvistavie.meetup.model;
 
 import android.os.AsyncTask;
 
 import com.tuvistavie.meetup.auth.model.User;
 import com.tuvistavie.meetup.model.listener.OnFetchListener;
 import com.tuvistavie.meetup.model.listener.OnUpdateListener;
 import com.tuvistavie.meetup.util.HTTPHelper;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by daniel on 9/1/13.
  */
 public abstract class AbstractCollection<T extends Entity> implements Collection {
 
     protected List<T> entities;
     protected int minFetchTimeDiff;
     private Date lastFetch;
 
     private OnFetchListener onFetchListener;
     private OnUpdateListener onUpdateListener;
 
     public AbstractCollection() {
         entities = new ArrayList<T>();
         minFetchTimeDiff = 1000;
         lastFetch = new Date(0);
     }
 
     @Override
     public JSONArray toJSONArray() {
         JSONArray jsonArray = new JSONArray();
         for(Entity e: entities) {
             jsonArray.put(e.toJSONObject());
         }
 
         return  jsonArray;
     }
 
     @Override
     public void fromJSON(JSONArray jsonArray) {
         for(int i = 0; i < jsonArray.length(); i++) {
             try {
                 JSONObject jsonObject = jsonArray.getJSONObject(i);
                 T entity = createEntity(jsonObject);
                 entities.add(entity);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void fromJSON(String json) {
         try {
             fromJSON(new JSONArray(json));
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public String toJSON() {
         return toJSONArray().toString();
     }
 
     @Override
     public void fetch() {
         Date currentTime = new Date();
         if(currentTime.getTime() - lastFetch.getTime() <= minFetchTimeDiff) {
             return;
         }
         lastFetch = new Date();
         String uri = getURI();
         if(needsAuthentication()) {
             uri = HTTPHelper.addParameterToURI(uri, "token", User.getInstance().getToken());
         }
         JSONArray jsonArray = HTTPHelper.getJSONArray(uri);
         fromJSON(jsonArray);
         runOnFetch();
     }
 
     private void runOnFetch() {
         runOnUpdate();
         if(onFetchListener != null) {
             onFetchListener.onFetch(null);
         }
     }
 
     private void runOnUpdate() {
         if(onUpdateListener != null) {
             onUpdateListener.onUpdate(this);
         }
     }
 
     // FIXME: batch save collection
     @Override
     public boolean save() {
         return true;
     }
 
     protected abstract Class<?> getEntityClass();
     protected abstract String getURI();
 
     protected T createEntity(JSONObject json) {
         try {
             return (T)getEntityClass().getConstructor(JSONObject.class).newInstance(json);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public List<T> getEntities() {
         return entities;
     }
 
     protected void setEntities(List<T> entities) {
         this.setEntities(entities, true);
     }
 
     protected void setEntities(List<T> entities, boolean notify) {
         this.entities = entities;
         if(notify) {
             runOnUpdate();
         }
     }
 
     protected boolean needsAuthentication() {
         return true;
     }
 
     public void setOnFetchListener(OnFetchListener onFetchListener) {
         this.onFetchListener = onFetchListener;
     }
 
     public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
         this.onUpdateListener = onUpdateListener;
     }
 
     public void fetchInBackground() {
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected Void doInBackground(Void... params) {
                 fetch();
                 return null;
             }
         }.execute();
     }
 }
