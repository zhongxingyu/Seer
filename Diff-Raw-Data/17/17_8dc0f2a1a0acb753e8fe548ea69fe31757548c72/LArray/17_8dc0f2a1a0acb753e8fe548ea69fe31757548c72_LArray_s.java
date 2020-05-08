 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // UInt32Array.java
 // Since: 2010/11/05
 //
 //--------------------------------------
 package org.utgenome.util.aligner;
 
 import java.util.ArrayList;
 
 /**
 * array allowing the size more than 2GB
  * 
  * @author leo
  * 
  */
 public class LArray implements RandomAccess {
 
 	private final long size;
 
 	private ArrayList<int[]> array;
 
 	public LArray(long size) {
 		this.size = size;
 
 		// (flag)|---(array index)----|---------------------------|
 		// (flag)|------(31 bit)-------|------(index: 31bit)------|
 		int container = (int) (size >> 31);
 		int remainder = (int) size & 0x7FFFFFFF;
 
 		array = new ArrayList<int[]>(container + 1);
 		for (int i = 0; i < container; i++) {
 			array.add(new int[Integer.MAX_VALUE]);
 		}
 		if (remainder > 0)
 			array.add(new int[remainder]);
 
 	}
 
 	public long size() {
 		return size;
 	}
 
 	public int get(long index) {
		int container = (int) (size >> 31);
		int remainder = (int) size & 0x7FFFFFFF;
 
 		return array.get(container)[remainder];
 	}
 
	public void set(long index, int value) {
		int container = (int) (size >> 31);
		int remainder = (int) size & 0x7FFFFFFF;
 
		array.get(container)[remainder] = value;
 	}
 
 }
