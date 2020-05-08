 package pt.utl.ist.tese.utils;
 
 import java.util.Comparator;
 
 //
 //  Key.java
 //  Artists
 //
 //  Created by andré on 11/10/08.
 //  Copyright (c) 2008 __MyCompanyName__. All rights reserved.
 //
 
 //package pt.utl.ist.tese.utils;
 
 public class Key {
 	
 	protected double _euclDist;
 	protected int _sub;
 	protected int _index;
 	protected double _constant;
 	
 	
 	public Key( double euclDist  ) {
 		_euclDist = euclDist;
 		_sub = 0;
 		_constant = 1;
 	}
 	public Key( double euclDist , int sub ) {
 		_euclDist = euclDist;
 		_sub = sub;
 		_index = -1;
 		_constant = 1;
 	}
 	public Key( double euclDist , int sub , int index , double constant ) {
 		_euclDist = euclDist;
 		_sub = sub;
 		_index = index;
 		_constant = constant;
 	}
 	public boolean subspace() {
 		return _index != -1;
 	}
 	public double eucl() {
 		return _euclDist;
 	}
 	
 	public int index() {
 		return _index;
 	}
 	public double constant() {
 		return _constant;
 	}
 	public int sub() {
 		return _sub;
 	}
 
 	
 	public void setSub( int sub ) {
 		_sub = sub;
 	}
 	
 	public String toString() {
 		return "eucl: " + eucl();
 	}
 
 	public class KeyCharacteristicComparator implements Comparator<Key> {
 		//	private static final SimpleLogger log = new SimpleLogger(KeyCharacteristicComparator.class);
 		
 		public int compare(Key obj1, Key obj2){
 			
 			return (obj1.sub() - obj2.sub());
 		}
 		
 	}
 	public class KeyComparator implements Comparator<Key> {
 
 //		private static final SimpleLogger log = new SimpleLogger(KeyComparator.class);
 
 		@Override
 		public int compare(Key obj1, Key obj2){
 
			Key k1 = (Key) obj1;
			Key k2 = (Key) obj2;
 
 			double eucl1 = k1.eucl();
 			double eucl2 = k2.eucl();
 
 			if ( eucl1 != eucl2 )
 				return (int) (eucl1 - eucl2);
 			else {
				return (int) (k1.sub() - k2.sub());
 			}
 		}
 
 	}
 
 
 }
