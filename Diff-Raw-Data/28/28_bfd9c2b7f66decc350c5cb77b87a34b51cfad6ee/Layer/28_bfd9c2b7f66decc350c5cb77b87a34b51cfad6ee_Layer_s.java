 package com.vaadin.toolkitdraw.components.paintcanvas;
 
 import java.io.Serializable;
 
 /**
  * This class represents a image layer
  * @author John Ahlroos
  */
 public class Layer implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private boolean visible;
 	private String color;
 	private double alpha;
 	
 	private String name;
 	private PaintCanvas canvas;
 	
 	/**
 	 * Constructor
 	 * @return
 	 */
 	public PaintCanvas getCanvas() {
 		return canvas;
 	}
 
 	/**
 	 * Set the canvas
 	 * @param canvas
 	 * 	The canvas the layer draws on
 	 */
 	public void setCanvas(PaintCanvas canvas) {
 		this.canvas = canvas;
 	}
 
 	/**
 	 * Constructor
 	 * @param name
 	 * 		The name of the layer (identifier)
 	 * @param canvas
 	 * 		The  canvas the layer draws on
 	 */
 	public Layer(String name, PaintCanvas canvas) {			
 		this.name = name;
 		visible = true;
		this.canvas = canvas;
		
 		this.visible = true;
 		this.color = "0xFFFFFF";
 		this.alpha = 0;
 	}
 	
 	/**
 	 * Set layer visibility
 	 * @param visible
 	 * 		true if the layer is visible
 	 */
 	public void setVisible(boolean visible){	
 		this.visible = visible;
 		this.canvas.getLayers().setLayerVisibility(name, visible);
 	}
 	
 	/**
 	 * Set the color of the layer
 	 * @param color
 	 * 		Color of layer
 	 */
 	public void setColor(String color){
 		this.color = color;
 		this.canvas.getLayers().setLayerBackground(this, color, alpha);
 	}
 	
 	/**
 	 * Set the alpha channel value of the layer
 	 * @param alpha
 	 * 		A value between 0 and 1
 	 */
 	public void setAlpha(double alpha){
 		this.alpha = alpha;
 		this.canvas.getLayers().setLayerBackground(this, color, alpha);
 	}
 	
 	/**
 	 * Visibility of the layer
 	 * @return
 	 * 		Returns true if the layer is visible
 	 */
 	public boolean getVisible(){
 		return visible;
 	}
 
 	/**
 	 * Name of the layer
 	 * @return
 	 * 		Returns the name of the layer
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set the name of the layer
 	 * @param name
 	 * 		Layer name
 	 */
 	public void setName(String name) {	
 		this.name = name;
 		//TODO: Call PaintCanvas setLayerName method
 		
 		
 		
 	}
 
 	
 }
