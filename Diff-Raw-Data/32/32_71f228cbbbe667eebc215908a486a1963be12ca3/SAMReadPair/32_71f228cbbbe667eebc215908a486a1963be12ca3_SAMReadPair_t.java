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
 // SAMReadPair.java
 // Since: Aug 2, 2010
 //
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.bio;
 
 /**
  * Mate pair (paired end) of SAMReads
  * 
  * @author leo
  * 
  */
 public class SAMReadPair extends Interval {
 
 	private static final long serialVersionUID = 1L;
 
 	private SAMReadLight first;
 	private SAMReadLight second;
 
 	public SAMReadPair() {
 	}
 
 	public SAMReadPair(SAMReadLight first, SAMReadLight second) {
		super(first.unclippedStart, second.unclippedEnd);
 
 		this.first = first;
 		this.second = second;
 	}
 
 	public SAMReadLight getFirst() {
 		return first;
 	}
 
 	public SAMReadLight getSecond() {
 		return second;
 	}
 
 	public Gap getGap() {
 		return new Gap(first.getEnd(), second.getStart());
 	}
 
 	@Override
 	public String getName() {
 		return first.getName();
 	}
 
 	@Override
 	public void accept(OnGenomeDataVisitor visitor) {
 		visitor.visitSAMReadPair(this);
 	}
 }
