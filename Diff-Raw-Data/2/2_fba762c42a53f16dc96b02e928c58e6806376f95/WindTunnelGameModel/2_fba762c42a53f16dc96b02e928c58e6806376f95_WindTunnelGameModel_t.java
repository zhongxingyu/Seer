 package com.navid.trafalgar.mod.windtunnel;
 
 import com.jme3.light.AmbientLight;
 import com.jme3.post.Filter;
 import com.jme3.scene.Node;
 import com.navid.trafalgar.mod.windtunnel.model.AHarnessModel;
 import com.navid.trafalgar.model.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * @author alberto
  */
 public class WindTunnelGameModel {
 
     private AShipModel ship;
     private IContext context;
     private AHarnessModel harness;
     
     private Node gameNode = new Node("model");
     private List<Filter> fpp = new ArrayList<Filter>();
     
     private boolean inited = false;
     
     public boolean isInited(){
         return inited;
     }
     
     public void init(GameModel gameModel) {
         
         if(inited){
             throw new IllegalStateException("Instance CounterClockGameModel already inited");
         }
         
         inited = true;
         
         ship = (AShipModel) gameModel.getSingleByType(AShipModel.class);
         context = (IContext) gameModel.getSingleByType(IContext.class);
         harness = (AHarnessModel) gameModel.getSingleByType(AHarnessModel.class);
         
        gameNode.addLight( (SunModel) gameModel.getSingleByType(SunModel.class));
        
         fpp = gameModel.getByType(Filter.class);
         
         gameNode.attachChild(ship);
         gameNode.addLight(new AmbientLight());
     }
     
     /**
      * @return the gameNode
      */
     public Node getGameNode() {
         if(!inited){
             throw new IllegalStateException("Instance WindTunnelGameModel not yet inited");
         }
         
         return gameNode;
     }
 
     /**
      * @return the fpp
      */
     public Collection<Filter> getFpp() {
         if(!inited){
             throw new IllegalStateException("Instance WindTunnelGameModel not yet inited");
         }
         
         return fpp;
     }
 
     /**
      * @return the ship
      */
     public AShipModel getShip() {
         return ship;
     }
 
     /**
      * 
      * @return 
      */
     public IContext getIContext() {
         return context;
     }
 
     /**
      * @return the harness
      */
     public AHarnessModel getHarness() {
         return harness;
     }
 
     /**
      * @param harness the harness to set
      */
     public void setHarness(AHarnessModel harness) {
         this.harness = harness;
     }
 
 }
