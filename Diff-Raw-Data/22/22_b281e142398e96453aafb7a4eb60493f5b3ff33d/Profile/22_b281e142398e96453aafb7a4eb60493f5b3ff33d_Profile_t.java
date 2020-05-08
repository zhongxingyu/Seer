 package org.sukrupa.student;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.sukrupa.platform.DoNotRemove;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 @Entity
 public class Profile {
 
     @Id
     @GeneratedValue
     private long id;
 
     @DoNotRemove
     Profile(){
     }
 
 
     private String background;
 
     public Profile(String background){
         this.background = background;
     }
     public String getBackground(){
         return background;
     }
 
     public void background(String background){
        if(background != null)
            this.background = background;
     }
 
     private static String[] excludedFields = new String[]{"id"};
 
     public boolean equals(Object other) {
         return EqualsBuilder.reflectionEquals(this, other, excludedFields);
     }
 
     public int hashCode() {
         return HashCodeBuilder.reflectionHashCode(this, excludedFields);
     }
 
     public String toString() {
         return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
     }
 }
