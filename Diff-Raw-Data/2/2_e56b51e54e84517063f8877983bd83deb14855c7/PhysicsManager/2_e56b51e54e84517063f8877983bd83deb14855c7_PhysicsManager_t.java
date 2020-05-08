 package com.angrykings;
 
 import com.angrykings.maps.BasicMap;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.debug.Debug;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * PhysicsManager
  *
  * Manages all physically simulated game objects.
  *
  * @author Shivan Taher <zn31415926535@gmail.com>
  * @date 31.05.13
  */
 public class PhysicsManager implements IUpdateHandler {
 	private static PhysicsManager instance = null;
 	private boolean ready;
 	private boolean freezed;
 	protected BaseGameActivity context;
 	
 	public static PhysicsManager getInstance() {
 		if(instance == null)
 			instance = new PhysicsManager();
 
 		return instance;
 	}
 	
 	public void setContext(BaseGameActivity context){
 		this.context = context;
 	}
 
 	private static final float MIN_LINEAR_VELOCITY = 1e-2f;
 	private static final float MIN_ANGULAR_VELOCITY = 1e-1f;
 	private ArrayList<PhysicalEntity> physicalEntities;
 
	private PhysicsManager() {
 		this.ready = true;
 		this.physicalEntities = new ArrayList<PhysicalEntity>();
 	}
 
 	public void addPhysicalEntity(PhysicalEntity entity) {
 		this.physicalEntities.add(entity);
 	}
 
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		this.ready = true;
 
 		Iterator<PhysicalEntity> it = this.physicalEntities.iterator();
 		while(it.hasNext()) {
 			PhysicalEntity entity = it.next();
 			Body b = entity.getBody();
 
 			if(!entity.isAutoRemoveEnabled())
 				continue;
 
 			float linearVelocity = b.getLinearVelocity().len();
 			float angularVelocity = b.getAngularVelocity();
 
 			if(linearVelocity > 0 && linearVelocity < 2.5 && angularVelocity < 3){
 				b.setAngularVelocity(0.0f);
 			}
 
 			if(linearVelocity < PhysicsManager.MIN_LINEAR_VELOCITY && angularVelocity < PhysicsManager.MIN_ANGULAR_VELOCITY) {
 				Debug.d("remove physical entity: lin: " + b.getLinearVelocity().len() + " angular: " + b.getAngularVelocity());
 				entity.remove(context);
 				it.remove();
 			}else{
 				Debug.d("not ready: lin="+linearVelocity+", ang="+angularVelocity);
 				this.ready = false;
 			}
 
 			if(entity.getAreaShape().getY() > BasicMap.GROUND_Y + 100) {
 				Debug.d("remove physical entity (seems to have fallen down of the 'ground'): y " + entity.getAreaShape().getY());
 				entity.remove(context);
 				it.remove();
 			}
 		}
 	}
 
 	public void setFreeze(boolean freeze) {
 		Debug.d((freeze ? "" : "un") + "freeze");
 
 		Iterator<PhysicalEntity> it = this.physicalEntities.iterator();
 		while(it.hasNext()) {
 			PhysicalEntity entity = it.next();
 			Body b = entity.getBody();
 
 			// ignore auto removable entities like cannon balls -> just freeze the castle blocks
 
 			if(entity.isAutoRemoveEnabled())
 				continue;
 
 			b.setActive(!freeze);
 		}
 
 		GameContext.getInstance().getPhysicsWorld().clearForces();
 
 		this.freezed = freeze;
 	}
 
 	/**
 	 * @param ignoreAutoRemovables	If true the entities with autoRemove enabled will be ignored
 	 * @return	Returns a list of physical entities.
 	 */
 	public ArrayList<PhysicalEntity> getPhysicalEntities(boolean ignoreAutoRemovables) {
 		ArrayList<PhysicalEntity> entities = new ArrayList<PhysicalEntity>();
 
 		Iterator<PhysicalEntity> it = this.physicalEntities.iterator();
 		int count =0;
 		while(it.hasNext()) {
 			PhysicalEntity entity = it.next();
 
 			if(ignoreAutoRemovables && entity.isAutoRemoveEnabled())
 				continue;
 
 			entities.add(entity);
 		}
 
 		return entities;
 	}
 
 	/**
 	 * @return Returns a list of all physical entities except of the ones where autoRemove ist enabled
 	 */
 	public ArrayList<PhysicalEntity> getPhysicalEntities() {
 		return this.getPhysicalEntities(true);
 	}
 
 	public boolean isReady() {
 		return this.ready && this.freezed;
 	}
 
 	/**
 	 * @param id	Id of the entity.
 	 * @return		Returns the physical entity or null if not found.
 	 */
 	public PhysicalEntity getEntityById(final int id) {
 		for(PhysicalEntity e : this.physicalEntities)
 			if(e.getId() == id)
 				return e;
 
 		Debug.d("Warning: entity with id=" + id + " not found!");
 
 		return null;
 	}
 
 	public void updateEntities(final JSONArray jsonEntities) {
 		context.runOnUpdateThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					for(int i=0; i < jsonEntities.length(); i++) {
 						JSONObject jsonEntity = jsonEntities.getJSONObject(i);
 
 						final int id = jsonEntity.getInt("id");
 						final float x = (float) jsonEntity.getDouble("x");
 						final float y = (float) jsonEntity.getDouble("y");
 						final float rotation = (float) jsonEntity.getDouble("rotation");
 
 						PhysicalEntity e = PhysicsManager.this.getEntityById(id);
 
 						if(e != null){
 							final float widthD2 = e.getAreaShape().getWidth() / 2;
 							final float heightD2 = e.getAreaShape().getHeight() / 2;
 							final Vector2 v2 = Vector2Pool.obtain(
 									(x + widthD2) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
 									(y + heightD2) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT
 							);
 	
 							e.getBody().setTransform(v2, rotation);
 							Vector2Pool.recycle(v2);
 						}
 					}
 				} catch (JSONException e) {
 
 				}				
 			}
 		});
 	}
 
 	public void clearEntities() {
 		this.physicalEntities.clear();
 		PhysicalEntity.CURRENT_ID = 0;
 	}
 
 	@Override
 	public void reset() {
 	}
 }
