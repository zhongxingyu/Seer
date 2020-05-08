 package org.daum.library.android.sitac.controller;
 
 import java.util.*;
 
 import org.daum.common.model.api.IModel;
 import org.daum.library.android.sitac.listener.OnEngineStateChangeListener;
 import org.daum.library.android.sitac.view.SITACMapView;
 import org.daum.library.android.sitac.view.SITACMenuView;
 import org.daum.library.android.sitac.view.entity.DemandEntity;
 import org.daum.library.android.sitac.view.entity.IEntity;
 import org.daum.library.android.sitac.view.entity.IEntityFactory;
 
 import android.util.Log;
 import org.daum.library.android.sitac.visitor.EntityUpdateVisitor;
 import org.daum.library.android.sitac.visitor.IVisitor;
 
 /**
  * Handle engine's events and updates views accordingly
  * This class should be used as a delegate of the SITACController
  * @author max
  *
  */
 public class EngineHandler implements OnEngineStateChangeListener {
 	
 	private static final String TAG = EngineHandler.class.getSimpleName();
 
 	private SITACMapView mapView;
 	private SITACMenuView menuView;
 	private IEntityFactory factory;
     private IVisitor visitor;
 	private Map<IEntity, IModel> modelMap;
 	
 	public EngineHandler(IEntityFactory factory, Hashtable<IEntity, IModel> modelMap) {
 		this.factory = factory;
 		this.modelMap = Collections.synchronizedMap(modelMap);
         this.visitor = new EntityUpdateVisitor();
 	}
 
     @Override
     public void onAdd(IModel m) {
         Log.d(TAG, "onAdd(IModel)");
         for (IModel model : modelMap.values()) {
             if (model.getId().equals(m.getId()))  {
                 update(m);
                 return;
             }
         }
         add(m);
     }
 
     @Override
     public void onUpdate(IModel m) {
         Log.d(TAG, "onUpdate(IModel) "+m);
         for (IModel model : modelMap.values()) {
             if (model.getId().equals(m.getId()))  {
                 update(m);
                 return;
             }
         }
         add(m);
     }
 
     @Override
     public void onDelete(String id) {
         Log.d(TAG, "onDelete(IModel)");
        delete(getEntityWithModelId(id));
     }
 
     @Override
     public void onReplicaSynced(ArrayList<IModel> data) {
         Log.d(TAG, "onReplicaSynced(ArrayList<IModel>)");
         for (IModel m : data) {
             onUpdate(m);
         }
     }
 
     private void add(IModel m) {
         Log.d(TAG, "add(IModel) with brand new entity");
         IEntity e = factory.build(m);
         add(m, e);
     }
 
     private void add(IModel m, IEntity e) {
         Log.d(TAG, "add(IModel, IEntity) in modelMap + setState");
         modelMap.put(e, m);
         e.setState(IEntity.State.SAVED);
         add(e);
     }
 
     private void add(IEntity e) {
         Log.d(TAG, "add(IEntity) to the mapView (and menuView if necessary)");
         if (e.getGeoPoint() == null) {
             // in case the entity has no location on map, add it to the menu
             menuView.addEntityWithNoLocation((DemandEntity) e);
         }
         mapView.addEntity(e);
 
         Log.d(TAG, ">>> entity >> "+e);
         for (IModel model : modelMap.values()) {
             Log.d(TAG, ">>> map >> "+model);
         }
     }
 
     private void update(IModel m) {
         Log.d(TAG, "update(IModel)");
         IEntity e = getEntityWithModel(m);
         modelMap.put(e, m); // update model in modelMap
         e.setState(IEntity.State.SAVED);
 
         if (!mapView.hasEntity(e)) mapView.addEntity(e);
 
         e.accept(visitor, m);
     }
 
     private void delete(IEntity e) {
         Log.d(TAG, "delete(IEntity)");
         modelMap.remove(e);
         mapView.deleteEntity(e);
     }
 
     /**
      * Returns the IEntity associated with the IModel in modelMap and
      * replace the old IModel value with the one given in parameter.
      * To find the IModel object it will search based on IModel ids.
      *
      * @param m model you want to find the entity associated with
      * @return IEntity associated with the model
      */
     private IEntity getEntityWithModel(IModel m) {
         Set<Map.Entry<IEntity, IModel>> entries = modelMap.entrySet();
         synchronized (modelMap) {
             for (Map.Entry<IEntity, IModel> entry: entries) {
                 if (entry.getValue().getId().equals(m.getId())) {
                     entry.setValue(m); // update model
                     return entry.getKey();
                 }
             }
         }
         return null;
     }
 
     private IEntity getEntityWithModelId(String id) {
         Set<Map.Entry<IEntity, IModel>> entries = modelMap.entrySet();
         synchronized (modelMap) {
             for (Map.Entry<IEntity, IModel> entry: entries) {
                 if (entry.getValue().getId().equals(id)) {
                     return entry.getKey();
                 }
             }
         }
         return null;
     }
 
     public void registerMenuView(SITACMenuView menuView) {
 		this.menuView = menuView;
 	}
 
 	public void registerMapView(SITACMapView mapView) {
 		this.mapView = mapView;
 	}
 }
