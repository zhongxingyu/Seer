 /*******************************************************************************
  * Copyright 2011 Andrew Jenkins
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.ajj.robodtn;
 
 import java.util.Date;
import java.util.Iterator;
import java.util.List;
 
 import com.ajj.robodtn.acquire.AcquireBundleDict;
 
 public class Bundle {
 	/* Bundle characterization flags: RFC5050 */
 	public static final long FRAG =           1 << 0;
 	public static final long ADMIN =          1 << 1;
 	public static final long DONTFRAG =       1 << 2;
 	public static final long CTREQ =          1 << 3;
 	public static final long DESTSINGLETON =  1 << 4;
 	public static final long APPACKREQ =      1 << 5;
 	
 	/* Bundle Class of Service flags: RFC5050 */
 	public static final long COS_BULK =            0;
 	public static final long COS_NORMAL =     1 << 7;
 	public static final long COS_EXPEDITED =  1 << 8;
 	
 	/* Bundle Report Request flags: RFC5050 */
 	public static final long RPTRX =          1 << 14;
 	public static final long RPTCT =          1 << 15;
 	public static final long RPTFWD =         1 << 16;
 	public static final long RPTDLV =         1 << 17;
 	public static final long RPTDEL =         1 << 18;
 	
 	/* Number of seconds difference between the DTN epoch (1/1/2000) and the
 	 * UNIX epoch (1/1/1970) */
 	public static final long DTNEPOCH = 	 946684800;
 	public static final int  VERSION_RFC5050 =      6;
 	
 	
 	/* Primary Bundle Block: RFC5050 */
 	public int version;
 	public long procFlags;
 	public long blockLength;
 	public String dst;
 	public String src;
 	public String rptto;
 	public String cust;
 	public Date createTimestamp;
 	public long createSeq;
 	public long lifetime;
 	public AcquireBundleDict dict;
 	
 	/* Fragment fields: RFC5050
 	 * Only useful if bundle is a fragment */	
 	public long fragOffset;
 	public long aduLength;
 	
 	/* Bundle Blocks (aka Extension Blocks) */
 	public BundleBlocks blocks = new BundleBlocks();
 
 	public Bundle(long procFlags, String dst, String src,
 			String rptto, String cust, Date createTimestamp, long createSeq,
 			long lifetime, long fragOffset, long aduLength, BundleBlock [] blocks) {
 		this.version = VERSION_RFC5050;
 		this.procFlags = procFlags;
 		this.dst = dst;
 		this.src = src;
 		this.rptto = rptto;
 		this.cust = cust;
 		this.createTimestamp = createTimestamp;
 		this.createSeq = createSeq;
 		this.lifetime = lifetime;
 		this.fragOffset = fragOffset;
 		this.aduLength = aduLength;
 		
 		if(blocks == null) {
 			this.blocks = null;
 		} else {
 			this.blocks = new BundleBlocks();
 			for(int i = 0; i < blocks.length; i++) {
 				this.blocks.addBlock(blocks[i]);
 			}	
 		}
 		this.dict = null;
 	}
 
 	public Bundle() {
 		
 	}
 }
