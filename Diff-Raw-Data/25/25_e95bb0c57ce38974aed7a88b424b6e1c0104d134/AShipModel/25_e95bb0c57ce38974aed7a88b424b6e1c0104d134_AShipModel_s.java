 package com.navid.trafalgar.model;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.input.InputManager;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.Control;
 import com.navid.trafalgar.manager.EventManager;
 import com.navid.trafalgar.manager.statistics.StatisticsManager;
 import com.navid.trafalgar.persistence.CandidateRecord;
 import com.navid.trafalgar.persistence.StepRecord;
 
 /**
  *
  * @author alberto
  */
 public abstract class AShipModel extends TrafalgarNode implements Control, Dependent {
 
     protected IContext context;
     
     protected StatisticsManager statisticsManager;
 
     public AShipModel(Vector3f lookAt, AssetManager assetManager, EventManager eventManager) {
         super(lookAt, assetManager, eventManager);
     }
 
     public abstract void registerInput(InputManager inputManager);
 
     public abstract float getSpeed();
 
     public abstract void setTransparent(boolean b);
 
     public abstract StepRecord getSnapshot();
 
     public abstract void updateFromRecord(StepRecord currentStepRecord);
 
     public abstract CandidateRecord getCandidateRecordInstance();
 
     public final boolean isEnabled() {
         return true;
     }
 
     public void setEnabled(boolean value) {
         //todo
     }
 
     public void setStatisticsManager(StatisticsManager statisticsManager) {
         this.statisticsManager = statisticsManager;
 
         initStatisticsManager();
     }
 
     protected abstract void initStatisticsManager();
 
     public abstract void setWindNode(IWind.WindGeometry windGeometry);
 
     public final Control cloneForSpatial(Spatial spatial) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public final void setSpatial(Spatial spatial) {
     }
 
     public final void render(RenderManager rm, ViewPort vp) {
     }
     
     
     public void resolveDependencies(GameModel gameModel) {
         this.context = (IContext) gameModel.getSingleByType(IContext.class);
     }
 }
