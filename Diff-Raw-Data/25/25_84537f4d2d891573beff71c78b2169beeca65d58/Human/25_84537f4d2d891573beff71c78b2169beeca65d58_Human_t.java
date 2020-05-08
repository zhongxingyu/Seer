 package org.noip.staticattic.entities;
 
 import javax.swing.JLabel;
 
 import org.noip.staticattic.fileutils.TextureHandler;
 import org.noip.staticattic.world.Environment;
 
 public abstract class Human extends Entity {
 
 	public enum AnimationState {
 		
 		IDLE, WALKING_UP, WALKING_LEFT, WALKING_RIGHT, WALKING_DOWN, RUNNING_UP, RUNNING_DOWN, RUNNING_LEFT, RUNNING_RIGHT, FISHING, SWINGING_NET
 		
 	}
 	
 	private AnimationState state = AnimationState.IDLE;
 	private Environment currentenvironment;	
 	private String name;
 	
 	public void setAnimationState(AnimationState state) {
 		
 		this.state = state;
 		
 	}
 	
 	public AnimationState getAnimationState() { 
 		
 		return this.state;
 		
 	}
 	
 	public Environment getCurrentEnvironment() {
 		
 		return currentenvironment;
 		
 	}
 
 	public void setCurrentEnvironment(Environment currentenvironment) {
 		
 		this.currentenvironment = currentenvironment;
 		
 	}
 
 	public Human(int xloc, int yloc) {
 		
 		super(xloc, yloc);
 		
 		this.setIcon(TextureHandler.getPlayer());
 					  
 		this.setLabel(new JLabel(this.getIcon()));
 		this.getLabel().setBounds(this.getLocation().getX(), this.getLocation().getY(), 34, 44);
		
 	}	
 	
 	public String getName() {
 		
 		return name;
 		
 	}
 
 	public void setName(String name) {
 		
 		this.name = name;
 		
 	}
 	
 }
