 package uk.ac.cam.db538.dexter.aux.struct;
 
 public class TaintExternal implements Taint {
 
 	private int taint;
 	
 	public TaintExternal(int taint) {
 		this.taint = taint;
 	}
 	
 	public int get() { 
 		return this.taint; 
 	}
 	
 	public void set(int taint) {
		this.taint |= taint;
 	}
 }
