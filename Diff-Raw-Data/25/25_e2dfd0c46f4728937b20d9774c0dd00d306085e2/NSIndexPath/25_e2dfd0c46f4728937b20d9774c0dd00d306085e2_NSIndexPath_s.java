 /*
  * Copyright (C) 2012 Wu Tong
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.cocoa4android.ns;
 
 public class NSIndexPath extends NSObject implements Comparable<NSIndexPath> {
 	private int section;
 	private int row;
 	
 	public NSIndexPath(int section,int row) {
 		this.section = section;
 		this.row = row;
 	}
 	
 	public int section() {
 		return section;
 	}
 	public int row() {
 		return row;
 	}
	
 
 	@Override
 	public boolean equals(Object o) {
 		if (!this.getClass().isInstance(o)) {
 			return false;
 		}
 		return this.isEqual((NSIndexPath)o);
 	}
 
 	@Override
 	public int hashCode() {
 		return this.hash();
 	}
 
 	@Override
 	public String toString() {
 		return super.toString();
 	}
 
 	@Override
 	public boolean isEqual(NSObject o) {
 		if (!o.isKindOfClass(this.getClass())) {
 			return false;
 		}
 		NSIndexPath obj = (NSIndexPath) o;
 		if (this.section != obj.section) {
 			return false;
 		}
 		if (this.row != obj.row) {
 			return false;
 		}
 		return true;
 	}
 	@Override
 	public int hash() {
 		return this.section + this.row;
 	}
 
 	@Override
 	public int compareTo(NSIndexPath another) {
 		if (this.section < another.section()) {
 			return -1;
 		}
 		else if (this.section > another.section()) {
 			return 1;
 		}
 		else {
 			if (this.row < another.row()) {
 				return -1;
 			}
 			else if (this.row > another.row()) {
 				return 1;
 			}
 			else {
 				return 0;
 			}
 		}
 	}
 }
