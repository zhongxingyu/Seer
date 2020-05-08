 package gov.nih.nci.iso21090;
 
 import java.io.Serializable;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 /**
  * Represents the iso data type.
  * @author lpower
  */
 public class Adxp implements Serializable, Cloneable {
 
     private static final long serialVersionUID = 1L;
     private String code;
     private String codeSystem;
     private String value;
     private final AddressPartType type;
     private static final int HASH_CODE_SEED_1 = 13;
     private static final int HASH_CODE_SEED_2 = 31;
 
     /** ctor with immutable type.
      * @param type type
      */
     protected Adxp(AddressPartType type) {
         this.type = type;
     }
 
     /** ctor default.
      */
     public Adxp() {
         this(null);
     }
 
     /**
      * @return the code
      */
     public String getCode() {
         return code;
     }
 
     /**
      * @param code the code to set
      */
     public void setCode(String code) {
         this.code = code;
     }
    
     /**
      * @return the code system
      */
     public String getCodeSystem() {
         return codeSystem;
     }
 
     /**
      * @param codeSystem the code system to set
      */
     public void setCodeSystem(String codeSystem) {
         this.codeSystem = codeSystem;
     }
 
 
     /**
      * @return the type
      */
     public AddressPartType getType() {
         return type;
     }
 
 
     /**
      * @return the value
      */
     public String getValue() {
         return value;
     }
 
     /**
      * @param value the value to set
      */
     public void setValue(String value) {
         this.value = value;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
 
         if (this == obj) {
             return true;
         }
 
         if (!(obj instanceof Adxp)) {
             return false;
         }
 
         Adxp x = (Adxp) obj;
 
         return new EqualsBuilder()
         .append(this.getCode(), x.getCode())
         .append(this.getType(), x.getType())
         .append(this.getValue(), x.getValue())
         .isEquals();
     }
 
      /**
       * {@inheritDoc}
       */
      @Override
      public int hashCode() {
 
          return new HashCodeBuilder(HASH_CODE_SEED_1, HASH_CODE_SEED_2)
              .append(this.getCode())
              .append(this.getType())
              .append(this.getValue())
              .toHashCode();
      }
 
      /**
       * {@inheritDoc}
       */
      @SuppressWarnings("PMD.CloneThrowsCloneNotSupportedException")
      @Override
      public Adxp clone() {
 
          Adxp snapshot = null;
          try {
              snapshot = (Adxp) BeanUtils.cloneBean(this);
          } catch (Exception e) {
              throw new IsoCloneException(e);
          }
 
          return snapshot;
      }
 }
