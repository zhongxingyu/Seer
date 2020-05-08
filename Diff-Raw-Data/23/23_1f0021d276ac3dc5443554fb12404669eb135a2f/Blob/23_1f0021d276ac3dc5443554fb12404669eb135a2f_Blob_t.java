 package com.vitaltech.bioink;
 
 import rajawali.primitives.Sphere;
 import android.graphics.Color;
 
 public class Blob extends Sphere {
 	// current data
 	private float _x,_y,_z; // position
 	private int _color; // color
 	private float _energy; // amount of energy on the surface of the blob
 	private float _volumeMultiplier; // amount to multiply the radius by
 	
 	// target data to animate towards
 	public float x,y,z;
 	private int color;
 	private float volumeMultiplier = 1;
 	public float energy;
 	private long startTime = 0;
 	
 	// smooth out the animations with exponential decay towards the target
 	private float DECAY = 50.0f; // 1 = instantaneous, >1 for slower decay
 	
 	private BlobShader shader;
 	
 	public Blob(){
 		super(0.2f,40,40);
 		_x = _y = _z = x = y = z = 0;
		_color = color = Color.WHITE;
 		_energy = energy = 0;
 		startTime = System.currentTimeMillis();
 		
 		shader = new BlobShader();
 		shader.setUseColor(true);
 		setMaterial(shader);
 	}
 	
 	public void draw(){
 		animate();
 		
 		setPosition(_x,_y,_z);
 		setColor(_color);
 		shader.setTime((float)(System.currentTimeMillis()-startTime));
 		shader.setStrength(_energy);
 		shader.setVolumeMultiplier(_volumeMultiplier);
 	}
 	
 	public void adjustColor(float ratio){
 		color = interpolateColor(Color.GREEN,Color.RED,ratio);
 	}
 	
 	// adjust the volume of the blob
 	public void setVolume(float size){
 		if(size < 1){
 			setVisible(false);
 		} else {
 			setVisible(true);
 			volumeMultiplier = size;
 		}
 	}
 	
 	public float getRadius(){
 		return (float) Math.pow(_volumeMultiplier, 1.0/3) * getGeometry().getBoundingSphere().getRadius();
 	}
 	
 	// update current data by moving towards the target
 	private void animate(){
 		_x = _x + (x-_x)/DECAY;
 		_y = _y + (y-_y)/DECAY;
 		_z = _z + (z-_z)/DECAY;
 		_energy = _energy + (energy-_energy)/DECAY;
 		_color = interpolateColor(_color,color,1/DECAY);
 		_volumeMultiplier = _volumeMultiplier + (volumeMultiplier-_volumeMultiplier)/DECAY;
 	}
 	
 	// calculate an interpolated color given a start and end color and the
 	// proportion of the distance between them
 	private int interpolateColor(int a, int b, float proportion) {
 	    float[] hsva = new float[3];
 	    float[] hsvb = new float[3];
 	    Color.colorToHSV(a, hsva);
 	    Color.colorToHSV(b, hsvb);
 	    for (int i = 0; i < 3; i++) {
 	    	if(i==0)
 	    		hsvb[i] = interpolateCircular(hsva[i],hsvb[i],proportion); // hue is interpolated from [0,360) where it wraps around
 	    	else
 	    		hsvb[i] = interpolateLinear(hsva[i],hsvb[i],proportion);
 	    }
 	    return Color.HSVToColor(hsvb);
 	}
 	
 	// interpolate linearly (usually 0-1)
 	private float interpolateLinear(float a, float b, float proportion){
 		return (a + (b-a)*proportion);
 	}
 	
 	// interpolate from [0,360) around a circle
 	private float interpolateCircular(float a, float b, float proportion) {
 		float dist = Math.min(Math.abs(b-a),Math.abs(360-b+a)); // shortest distance between these colors
 		if(Math.abs(b-a) == dist) // interpolate in the normal direction
 			return (a + (b-a)*proportion);
 		else {
 			// it wraps around the circle, so interpolate the other way
 			float value = a - (360-b+a)*proportion;
 			
 			// perform the wrap
 			if (value < 0) value += 360;
 			if (value > 360) value -= 360;
 			return value;
 		}
 	}
 }
