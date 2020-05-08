 package org.afpparser.afp.modca.triplets.fullyqualifiedname;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.afpparser.afp.modca.ParameterAsString;
 
 /**
  * This fully qualified name object has an OID data type; The GID is an ASN.1 Object Identifier
  * (OID), defined in ISO/IEC 8824:1990(E). The data type is CODE. The OID is encoded using the
  * Basic Encoding Rules for ASN.1 specified in ISO/IEC 8825:1990(E).
  */
 final class FQNOidData extends FullyQualifiedName {
     private final FQNType type;
     private final ObjectId oid;
 
     FQNOidData(int length, ObjectId oid, FQNType type) {
         super(length);
         this.type = type;
         this.oid = oid;
     }
 
     @Override
     public FQNType getFQNType() {
         return type;
     }
 
     @Override
     public FQNFmt getFormat() {
         return FQNFmt.oid;
     }
 
     /**
      * Get the Object Identifier.
      *
      * @return the object identifier
      */
     public ObjectId getOid() {
         return oid;
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof FQNOidData)) {
             return false;
         }
         FQNOidData obj = (FQNOidData) o;
         return this.type == obj.type
                 && this.getLength() == obj.getLength()
                 && this.oid.equals(obj.oid);
     }
 
     @Override
     public int hashCode() {
         int result = 17;
         result = 31 * result + getLength();
         result = 31 * result + type.hashCode();
         result = 31 * result + oid.hashCode();
         return result;
     }
 
     @Override
     public List<ParameterAsString> getParameters() {
         List<ParameterAsString> params = new ArrayList<ParameterAsString>();
        params.add(new ParameterAsString("ObjectId", oid));
         return params;
     }
 }
