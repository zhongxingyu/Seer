 package com.vitaltech.bioink;
 
 import rajawali.primitives.Sphere;
 import android.graphics.Color;
 import android.opengl.GLES20;
 
 public class Blob extends Sphere {
 	// current data
 	private float _x,_y,_z; // position
 	private int _color; // color
 	private float _energy; // amount of energy on the surface of the blob
 	private float _dist; // distance from the center of the scene
 	
 	// target data to animate towards
 	public float x,y,z;
 	public int color;
 	public float radius;
 	public float energy;
 	public float dist;
 	
 	// smooth out the animations with exponential decay towards the target
 	private float DECAY = 50.0f; // 1 = instantaneous, >1 for slower decay
 	
 	private BlobShader shader;
 	private int frameCount = 0;
 	
 	public Blob(){
 		super(2,60,60);
 		_x = _y = _z = x = y = z = 0;
 		_color = color = Color.BLACK;
 		_energy = energy = 0;
 		_dist = dist = 0;
 		
 		shader = new BlobShader();
 		shader.setUseColor(true);
 		setMaterial(shader);
 		setDrawingMode(GLES20.GL_LINES);
 	}
 	
 	public void draw(){
 		animate();
 		
 		setColor(_color);
 		shader.setTime((float)frameCount++);
		shader.setStrength(_energy/100);
 	}
 	
 	public void adjustColor(float temp){
 		final float MIN_TEMP = 96.0f;
 		final float MAX_TEMP = 102.0f;
 		
 		float ratio = Math.max(0,Math.min((temp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP),1.0f)); //clamp between 0 and 1
		color = interpolateColor(Color.BLUE,Color.RED,ratio);	
 	}
 	
 	// update current data by moving towards the target
 	private void animate(){
 		_x = _x + (x-_x)/DECAY;
 		_y = _y + (y-_y)/DECAY;
 		_z = _z + (z-_z)/DECAY;
 		_energy = _energy + (energy-_energy)/DECAY;
 		_dist = _dist + (dist-_dist)/DECAY;
 		_color = interpolateColor(_color,color,1/DECAY);
 	}
 	
 	private int interpolateColor(int a, int b, float proportion) {
 	    float[] hsva = new float[3];
 	    float[] hsvb = new float[3];
 	    Color.colorToHSV(a, hsva);
 	    Color.colorToHSV(b, hsvb);
 	    for (int i = 0; i < 3; i++) {
 	      hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
 	    }
 	    return Color.HSVToColor(hsvb);
 	}
 	
	//.8 -> .1: dist = .3
 	private float interpolate(float a, float b, float proportion) {
		float dist = Math.min(Math.abs(b-a),Math.abs(1-b+a)); // shortest distance between these colors
		if(Math.abs(b-a) == dist)
			return (a + (b-a)*proportion);
		else {
			// it wraps around the circle, so interpolate that way
			if(a > 0.5)
				return (a + dist*proportion) - 1;
			else
				return (a - dist*proportion) - 1;
		}
			
		//return (a + ((b - a) * proportion));
 	}
 }
