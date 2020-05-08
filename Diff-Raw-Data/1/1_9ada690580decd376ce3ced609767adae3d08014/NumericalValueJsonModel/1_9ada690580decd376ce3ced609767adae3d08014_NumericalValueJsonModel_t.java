 package org.sensapp.android.sensappdroid.json;
 
 public class NumericalValueJsonModel extends ValueJsonModel {
 	
 	private int v;
 	
 	public NumericalValueJsonModel() {
		super();
 	}
 	
 	public NumericalValueJsonModel(int v, long t) {
 		super(t);
 		this.v = v;
 	}
 	
 	public int getV() {
 		return v;
 	}
 	
 	public void setV(int v) {
 		this.v = v;
 	}
 }
