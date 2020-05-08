 package org.hbird.core.commons.data;
 
 import org.apache.commons.lang.ArrayUtils;
 
public class GenericPayload {
 
 	public byte[] payload;
 	public String layoutIdentifier;
 	public long timeStamp;
 
 	public GenericPayload(final byte[] payload, final String layoutIdentifier, final long timeStamp) {
 		this.payload = ArrayUtils.clone(payload);
 		this.layoutIdentifier = layoutIdentifier;
 		this.timeStamp = timeStamp;
 	}
 
 }
