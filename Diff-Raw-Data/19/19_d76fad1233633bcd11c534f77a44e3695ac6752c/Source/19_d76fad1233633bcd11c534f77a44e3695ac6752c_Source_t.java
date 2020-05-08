 package com.ntraft.modlur.collada;
 
 import java.nio.FloatBuffer;
 
 /**
  * @author Neil Traft
  */
 public final class Source {
 
 	// TODO Hard-coded for now. In the future, the accessor determines the stride.
 	private static final int stride = 3;
 
 	private String id;
 	private FloatBuffer data;
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
	public int getStride() {
		return stride;
	}

 	public void setData(FloatBuffer data) {
 		this.data = data;
 	}
 
 	/** Read the vertex at <tt>index</tt> into the given buffer. */
 	public void get(FloatBuffer dest, int index) {
 		int start = index * stride;
 		for (int j = start; j < start + stride; j++) {
 			dest.put(data.get(j));
 		}
 	}
 }
