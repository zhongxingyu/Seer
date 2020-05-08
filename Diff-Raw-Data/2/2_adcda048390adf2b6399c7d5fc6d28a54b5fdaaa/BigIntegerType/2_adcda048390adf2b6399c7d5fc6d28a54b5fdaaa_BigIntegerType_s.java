 package org.codehaus.xfire.aegis.type.basic;
 
 import java.math.BigInteger;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.type.Type;
 
 /**
 * <code>Type</code> for a <code>BigDecimal</code>
  *
  * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
  */
 public class BigIntegerType extends Type
 {
     public BigIntegerType()
     {
         super();
     }
 
     public Object readObject( final MessageReader reader, final MessageContext context )
     {
         final String value = reader.getValue();
 
         return null == value ? null : new BigInteger( value );
     }
 
     public void writeObject( final Object object, final MessageWriter writer, final MessageContext context )
     {
         writer.writeValue( object.toString() );
     }
 }
