 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
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
 // Interval.java
 // Since: 2010/05/24
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.bio;
 
 import java.io.Serializable;
 
 /**
  * An interval in a genome sequence.
  * 
  * @author leo
  * 
  */
 public class Interval implements OnGenome, Comparable<Interval>, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	int start = -1; // 1-origin (inclusive, -1 means undefined value)
 	int end = -1; // 1-origin (exclusive, -1 means undefined value)
 
 	public Interval() {
 	}
 
 	public Interval(int start, int end) {
 		this.start = start;
 		this.end = end;
 		correctInterval();
 	}
 
 	public Interval(Interval other) {
 		this(other.start, other.end);
 	}
 
 	public String getName() {
 		return "[" + start + ", " + end + ")";
 	}
 
 	public void adjustToOneOrigin() {
 		if (start != -1)
 			start += 1;
 		if (end != -1)
 			end += 1;
 	}
 
 	/**
 	 * Get the start position of the locus. (start <= end)
 	 * 
 	 * @return the start position
 	 */
 	public int getStart() {
 		return start;
 	}
 
 	public void setStart(int start) {
 		this.start = start;
 		correctInterval();
 	}
 
 	protected void correctInterval() {
 		// do not swap start and end when one of them is undefined
 		if (start == -1 || end == -1)
 			return;
 
 		if (start > end) {
 			int tmp = start;
 			start = end;
 			end = tmp;
 		}
 	}
 
 	/**
 	 * Get the end position of the locus (start <= end)
 	 * 
 	 * @return the end position
 	 */
 	public int getEnd() {
 		return end;
 	}
 
 	public void setEnd(int end) {
 		this.end = end;
 		correctInterval();
 	}
 
 	public int length() {
 		// when [start:1, end:4), its length = 3
 		return getEnd() - getStart();
 	}
 
 	public boolean hasOverlap(Interval other) {
 		int s1 = getStart();
 		int e1 = getEnd();
 		int s2 = other.getStart();
 		int e2 = other.getEnd();
 
 		return s1 <= e2 && s2 <= e1;
 	}
 
 	public boolean intersectsWith(Interval other) {
 		if (start <= other.start)
 			return other.start <= end;
 		else
 			return start <= other.end;
 	}
 
 	public boolean contains(Interval other) {
 		return (start <= other.start) && (other.end <= end);
 	}
 
 	public void accept(OnGenomeDataVisitor visitor) {
 		visitor.visitInterval(this);
 	}
 
 	public int compareTo(Interval other) {
 		long diff = this.getStart() - other.getStart();
 		if (diff == 0) {
 			return this.hashCode() - other.hashCode();
 		}
 		else
 			return diff < 0 ? -1 : 1;
 	}
 
 	public String toJSONArray() {
 		return "[" + start + "," + end + "]";
 	}
 
 	@Override
 	public String toString() {
 		return "(" + start + ", " + end + ")";
 	}
 
 }
