 package com.github.joukojo.testgame.world.core;
 
 import java.util.List;
import java.util.Set;
 
 public interface WorldCore {
 
 	List<Moveable> getAllMoveables();
 
 	List<Drawable> getAllDrawables();
 
 	void addMoveable(String objectName, Moveable moveable);
 
 	Moveable getMoveable(String objectName);
 
 	List<Moveable> getMoveableObjects(String objectName);
 
 	void removeMoveable(String string, Moveable moveable);
 
 	void cleanMoveables();
 
 	List<String> getMoveableObjectNames();
 
 }
