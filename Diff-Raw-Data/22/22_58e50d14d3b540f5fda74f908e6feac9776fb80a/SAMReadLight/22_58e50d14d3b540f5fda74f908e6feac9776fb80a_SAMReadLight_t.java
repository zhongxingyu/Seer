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
 // SAMReadLight.java
 // Since: 2010/09/30
 //
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.bio;
 
 /**
  * Light weight SAMRead
  * 
  * @author leo
  * 
  */
 public class SAMReadLight extends Interval {
 
 	private static final long serialVersionUID = 1L;
 
 	//schema record(qname, flag, rname, start, end, mapq, cigar, mrnm, mpos, isize, seq, qual, tag*)
 	public String qname;
 	public int flag;
 	// left-most position on the reference sequence
 	public int unclippedStart;
 	public int unclippedEnd;
 
 	public String cigar;
 	public String mrnm; // mate reference name
 	public int mStart; // mate start (new parameter!) 
 
 	public SAMReadLight() {
 
 	}
 
 	public SAMReadLight(int start, int end) {
 		super(start, end);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("flag=" + flag);
 		return sb.toString();
 	}
 
 	public boolean isMate(SAMReadLight other) {
 		if (this.isFirstRead()) {
 			if (other.isSecondRead())
 				return qname != null && qname.equals(other.qname);
 		}
 		else {
 			if (other.isFirstRead())
 				return qname != null && qname.equals(other.qname);
 		}
 		return false;
 	}
 
 	@Override
 	public String getName() {
 		return qname;
 	}
 
 	@Override
 	public boolean isSense() {
 		return (flag & SAMReadFlag.FLAG_STRAND_OF_QUERY) == 0;
 	}
 
 	@Override
 	public boolean isAntiSense() {
 		return !isSense();
 	}
 
 	//	@Override
 	//	public void accept(OnGenomeDataVisitor visitor) {
 	//		visitor.visitSAMReadLight(this);
 	//	}
 
 	public boolean isPairedRead() {
 		return SAMReadFlag.isPairedRead(this.flag);
 	}
 
 	public boolean isMappedInProperPair() {
 		return SAMReadFlag.isMappedInProperPair(this.flag);
 	}
 
 	public boolean isFirstRead() {
 		return SAMReadFlag.isFirstRead(this.flag);
 	}
 
 	public boolean isSecondRead() {
 		return SAMReadFlag.isSecondRead(this.flag);
 	}
 
 	public boolean isUnmapped() {
 		return SAMReadFlag.isQueryUnmapped(this.flag);
 	}
 
 }
