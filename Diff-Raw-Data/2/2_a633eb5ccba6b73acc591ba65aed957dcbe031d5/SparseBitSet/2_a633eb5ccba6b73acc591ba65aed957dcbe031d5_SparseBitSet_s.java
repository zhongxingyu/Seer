  /**************************************************************************
  * Copyright 2008 Jules White                                              *
  *                                                                         *
  * Licensed under the Apache License, Version 2.0 (the "License");         *
  * you may not use this file except in compliance with the License.        *
  * You may obtain a copy of the License at                                 *
  *                                                                         *
  * http://www.apache.org/licenses/LICENSE-2.0                              *
  *                                                                         *
  * Unless required by applicable law or agreed to in writing, software     *
  * distributed under the License is distributed on an "AS IS" BASIS,       *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.*
  * See the License for the specific language governing permissions and     *
  * limitations under the License.                                          *
  **************************************************************************/
 
 package org.ascent.mmkp;
 
 import java.util.*;
 
 public class SparseBitSet extends BitSet implements Iterable<Integer> {
 
	protected Set bitset;
 	private int value_;
 	private int[] weight_;
 	private double netWeight_;
 	private double netValue_;
 	private boolean dirty_ = true;
 
 	protected Set getBitSet() {
 		return bitset;
 	}
 
 	// Replace BitSet no-arg constructor
 	public SparseBitSet() {
 		bitset = new HashSet();
 	}
 
 	// Performs a logical AND of this target bit set
 	// with the argument bit set
 	public void and(SparseBitSet set) {
 		dirty_ = true;
 		bitset.retainAll(set.bitset);
 	}
 
 	// Clears all of the bits in this bit set whose
 	// corresponding bit is set in the specified bit set
 	public void andNot(SparseBitSet set) {
 	}
 
 	// Removes the bit specified from the set
 	public void clear(int bit) {
 		dirty_ = true;
 		bitset.remove(new Integer(bit));
 	}
 
 	// Clone
 	public Object clone() {
 		SparseBitSet set = new SparseBitSet();
 		set.or(this);
 		return set;
 	}
 
 	// Equality check
 	public boolean equals(Object obj) {
 		if (obj == this)
 			return true;
 		if (!(obj instanceof SparseBitSet)) {
 			return false;
 		} else {
 			SparseBitSet other = (SparseBitSet) obj;
 			return bitset.equals(other.getBitSet());
 		}
 	}
 
 	// Checks if specific bit contained in set
 	public boolean get(int bit) {
 		return bitset.contains(new Integer(bit));
 	}
 
 	// Return internal set hashcode
 	public int hashCode() {
 		return bitset.hashCode();
 	}
 
 	// Returns the maximum element in set + 1
 	public int length() {
 		return (Integer) Collections.max(bitset);
 	}
 	
 	public double calculateNetWeight(){
 		double wsq = 0;
 		for(int i = 0; i < weight_.length; i++){
 			wsq += (weight_[i] * weight_[i]);
 		}
 		return Math.sqrt(wsq);
 	}
 	
 	public double getNetValue(){
 		if(dirty_)
 			update();
 		
 		return netWeight_;
 	}
 	
 	public void update(){
 		netWeight_ = calculateNetWeight();
 		netValue_ = ((int)value_) / netWeight_;
 	}
 
 	// Performs a logical OR of this bit set
 	// with the bit set argument
 	public void or(SparseBitSet set) {
 		if(!set.dirty_ && weight_ != null){
 			value_ += set.value_;
 			for(int i = 0; i < weight_.length; i++){
 				weight_[i] += set.weight_[i];
 			}
 			
 		}
 		bitset.addAll(set.bitset);
 	}
 
 	// Adds bit specified to set
 	public void set(int bit) {
 		dirty_ = true;
 		bitset.add(new Integer(bit));
 	}
 
 	// Return size of internal set
 	public int size() {
 		return bitset.size();
 	}
 
 	// Return string representation of internal set
 	public String toString() {
 		return bitset.toString();
 	}
 
 	// Performs a logical XOR of this bit set
 	// with the bit set argument
 	public void xor(SparseBitSet set) {
 		dirty_ = true;
 		Set other = new HashSet(set.bitset);
 		other.removeAll(bitset);
 		bitset.removeAll(other);
 		bitset.addAll(other);
 	}
 
 	public Iterator<Integer> iterator() {
 		return bitset.iterator();
 	}
 
 	public int getValue() {
 		return value_;
 	}
 
 	public void setValue(int value) {
 		value_ = value;
 	}
 
 	public int[] getWeight() {
 		return weight_;
 	}
 
 	public void setWeight(int[] weight) {
 		weight_ = weight;
 	}
 
 	public void setNetValue(double netValue) {
 		netValue_ = netValue;
 	}
 
 	public boolean isDirty() {
 		return dirty_;
 	}
 
 	public void setDirty(boolean dirty) {
 		dirty_ = dirty;
 	}
 
 }
