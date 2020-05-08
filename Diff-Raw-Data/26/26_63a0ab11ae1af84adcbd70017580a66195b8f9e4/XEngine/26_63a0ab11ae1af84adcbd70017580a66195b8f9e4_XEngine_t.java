 package com.github.vspiewak.mowitnow.mower.domain;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.vspiewak.mowitnow.mower.api.Engine;
 import com.github.vspiewak.mowitnow.mower.api.Scene;
 import com.github.vspiewak.mowitnow.mower.api.Vehicule;
 import com.github.vspiewak.mowitnow.mower.base.Position;
 
 /**
  * The XEngine (stand for X***a ;) is a simple <code>Engine</code>
  * handling scene bounds and collisions.
  * 
  * @author Vincent Spiewak
  * @since 1.0
  */
 public enum XEngine implements Engine {
 
    /* Singleton using Enum pattern */
    INSTANCE;
 
    private static final Logger LOG = LoggerFactory.getLogger(XEngine.class);
 
    private Scene scene;
    private Map<Position, Vehicule> vehicules;
 
    private XEngine() {
       vehicules = new HashMap<Position, Vehicule>();
    }
 
    @Override
    public void init(Scene scene) {
       clear();
       this.scene = scene;
    }
 
    @Override
    public void attach(Vehicule vehicule) {
 
       Position position = vehicule.getPosition();
 
       /* check vehicule position */
       if (!canMove(position)) {
          throw new IllegalArgumentException("Can't add the vehicule at: " + position);
       }
 
       /* add it */
       vehicules.put(position, vehicule);
       LOG.debug("add - stack {}", vehicules);
 
    }
 
    @Override
    public void detach(Vehicule vehicule) {
       vehicules.remove(vehicule);
    }
 
    @Override
    public boolean canMove(Position p) {
       return (getScene().canMove(p) && !vehicules.containsKey(p));
    }
 
    @Override
    public Position update(Vehicule vehicule, Position newPosition) {
 
       /* defensive-way: no update */
       Position returnedPosition = vehicule.getPosition();
       LOG.debug("update - from: {} to: {}", vehicule, newPosition);
 
       /* if outside of scene */
       if (!getScene().canMove(newPosition)) {
 
          LOG.warn("scene constraint: {} can't update at {}", vehicule, newPosition);
 
      /* if vehicule collision */
       } else if (vehicules.containsKey(newPosition)) {
 
          Vehicule collisionVehicule = vehicules.get(newPosition);
          LOG.warn("collision between {} and {}", vehicule, collisionVehicule);
 
      /* vehicule is free to update */
       } else {
 
          /* update vehicule position */
          vehicules.remove(vehicule.getPosition());
          vehicules.put(newPosition, vehicule);
          returnedPosition = newPosition;
       }
 
       LOG.debug("update - return: {}", returnedPosition);
 
       return returnedPosition;
 
    }
 
    @Override
    public void clear() {
       this.scene = null;
       vehicules.clear();
    }
 
    private Scene getScene() {
       return this.scene;
    }
 
    public static Engine get() {
       return INSTANCE;
    }
 
 }
