 package org.test.streaming;
 
 import java.io.Serializable;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 public class MovieCacho implements Serializable {
 	private int firstByteIndex;
 	private int length;
 
 	public MovieCacho() {
 	}
 
 	public MovieCacho(int firstByteIndex, int length) {
 		super();
 		if (length <= 0) {
 			throw new IllegalArgumentException("MovieCacho must have a positive length, got " + length);
 		}
 		if (firstByteIndex < 0) {
 			throw new IllegalArgumentException("MovieCacho must have a positive or zero firstByteIndex, got " + firstByteIndex);
 		}
 		this.firstByteIndex = firstByteIndex;
 		this.length = length;
 	}
 
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return EqualsBuilder.reflectionEquals(this, obj);
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 
 	public int getFirstByteIndex() {
 		return firstByteIndex;
 	}
 
 	public void setFirstByteIndex(int firstByteIndex) {
 		this.firstByteIndex = firstByteIndex;
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public void setLength(int length) {
 		this.length = length;
 	}
 
 	public boolean contains(MovieCacho cacho) {
 		return cacho.getFirstByteIndex() >= this.getFirstByteIndex() && cacho.getLastByteIndex() <= this.getLastByteIndex();
 	}
 
 	public MovieCacho overlaps(MovieCacho cacho) {
 		int max = Math.max(this.getFirstByteIndex(), cacho.getFirstByteIndex());
 		int min = Math.min(this.getLastByteIndex(), cacho.getLastByteIndex());
 		if ((min + 1 - max) > 0) {
 			MovieCacho movieCacho = new MovieCacho(max, min + 1 - max);
 			return movieCacho;
 		}
 		return null;
 	}
 
 	public int getLastByteIndex() {
 		return this.getFirstByteIndex() + this.getLength() - 1;
 	}
 
 	public MovieCacho translate(MovieCacho cacho) {
 		return new MovieCacho(cacho.getFirstByteIndex() - this.getFirstByteIndex(), cacho.getLength());
 	}
 
 	public boolean substract(MovieCacho overlap) {
 		int newLength = this.getLength() - overlap.getLength();
 		if (newLength > 0) {
 			this.setLength(newLength);
 			this.setFirstByteIndex(overlap.getLastByteIndex() + 1);
 			return true;
 		}
 		return false;
 	}
 
 }
