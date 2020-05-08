 package org.karmaexchange.dao;
 
 import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlTransient;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.NoArgsConstructor;
 import lombok.ToString;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Embed;
 
 @Embed
 @Data
 @NoArgsConstructor
 @EqualsAndHashCode(callSuper=true)
 @ToString(callSuper=true)
 public final class NullableKeyWrapper<T> extends KeyWrapper<T> {
 
   public static <T> NullableKeyWrapper<T> create() {
     return new NullableKeyWrapper<T>(null);
   }
 
   public static <T> NullableKeyWrapper<T> create(Key<T> key) {
     return new NullableKeyWrapper<T>(key);
   }
 
   private NullableKeyWrapper(@Nullable Key<T> key) {
     this.key = key;
   }
 
   public void setKey(@Nullable String keyStr) {
     if (keyStr == null) {
       key = null;
     } else {
       key = Key.<T>create(keyStr);
     }
   }
 
   @Override
   public String getKey() {
     return (key == null) ? null : key.getString();
   }
 
  @XmlTransient
   public boolean isNull() {
     return (key == null);
   }
 
   @Override
   public int compareTo(KeyWrapper<T> other) {
     if (key == other.key) {
       return 0;
     }
     if (key == null) {
       return -1;
     }
     return this.key.compareTo(other.key);
   }
 }
