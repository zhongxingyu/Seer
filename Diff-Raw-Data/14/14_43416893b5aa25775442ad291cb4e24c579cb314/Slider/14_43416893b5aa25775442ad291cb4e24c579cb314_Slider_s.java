 package com.globerry.project.service;
 
 import java.util.Observable;
 
 import com.globerry.project.domain.PropertyType;
 import com.globerry.project.service.interfaces.ISlider;
 
 public class Slider extends Observable implements ISlider
 {
     private float state;
     private PropertyType type;
     public Slider(PropertyType type){
 	this.type = type;
     }
     @Override
     public void onChange(float newState)
     {
 	state = newState;
 	super.notifyObservers(new EventUI(this));
     }
     public float getState()
     {
 	return state;
     }
     public PropertyType getType()
     {
 	return type;
     }
     @Override
     public void onChange(float newStateLeft, float newStateRight)
     {
 	// TODO Auto-generated method stub
	
     }
 
 }
