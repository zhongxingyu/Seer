 package net.oemig.scta.model.data;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 /**
  * The {@link Millisecond} class represents the number
  * of milliseconds as class for type safety.
  * 
  * @author chris
  *
  */
public final class Millisecond {
 
 	public static Millisecond of(int m){
 		return new Millisecond(m);
 	}
 	
 	public static Millisecond of(long m){
 		return new Millisecond((int)m);
 	}
 
 	private int millis;
 	
 	private Millisecond(int m){
 		millis=m;
 	}
 	
 	public void plus(Millisecond m){
 		millis=millis+m.intValue();
 	}
 	
 	public int intValue(){
 		return millis;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		return EqualsBuilder.reflectionEquals(this,obj);
 	}
 	
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 	
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this);
 	}
 }
