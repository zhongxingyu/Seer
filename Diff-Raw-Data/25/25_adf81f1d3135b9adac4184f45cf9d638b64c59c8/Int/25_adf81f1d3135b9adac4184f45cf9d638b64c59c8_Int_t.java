 package fatworm.dataentity;
 
 import java.math.BigDecimal;
 
 import fatworm.util.ByteLib;
 import static java.sql.Types.*;
 
 public class Int extends DataEntity
 {
     int value;
     public Int(int v) {
         value = v;
     }
 
     public Int(byte[] data, int offset) {
         value = ByteLib.bytesToInt(data, offset);
     }
 
     public byte[] getBytes() {
         byte[] data = new byte[4];
         ByteLib.intToBytes(value, data, 0);
         return data;
     }
 
     public int type() {
         return INTEGER;
     }
 
     public int compareTo(DataEntity t) {
         if(t instanceof Int) {
             int anotherv = ((Int)t).value;
             if(value == anotherv)
                 return 0;
             if(value < anotherv)
                 return -1;
             return 1;
         }
 
         if(t instanceof Float) {
             double anotherv = ((Float)t).value;
             if(value == anotherv)
                 return 0;
             if(value < anotherv)
                 return -1;
             return 1;
         }
 
         if(t instanceof Decimal) {
             return -t.compareTo(this);
         }
 
         return -1;
     }
 	@Override
 	public DataEntity opWith(DataEntity t, String op) {
         if(t instanceof Int) {
             int anotherv = ((Int)t).value;
             if (op.equals("+"))
                 return new Int(value+anotherv);
             if (op.equals("-"))
                 return new Int(value-anotherv);
             if (op.equals("*"))
                 return new Int(value*anotherv);
             if (op.equals("/"))
                 return new Float(value*1.0/anotherv);
             if (op.equals("%"))
                 return new Int(value%anotherv);
         }
         if(t instanceof Float) {
             double anotherv = ((Float)t).value;
             if (op.equals("+"))
                 return new Float(value+anotherv);
             if (op.equals("-"))
                 return new Float(value-anotherv);
             if (op.equals("*"))
                 return new Float(value *anotherv);
             if (op.equals("/"))
                 return new Float(value/anotherv);
         }
         if(t instanceof Decimal) {
             if (op.equals("+"))
                 return new Decimal((new BigDecimal(value)).add(((Decimal)t).value));
             if (op.equals("-"))
                 return new Decimal((new BigDecimal(value)).subtract(((Decimal)t).value));
             if (op.equals("*"))
                 return new Decimal((new BigDecimal(value)).multiply(((Decimal)t).value));
             if (op.equals("/"))
                 return new Decimal((new BigDecimal(value)).divide(((Decimal)t).value));
             if (op.equals("%"))
                 return new Decimal((new BigDecimal(value)).remainder(((Decimal)t).value));
         }
 		return new NullDataEntity();
 	}
 
     public String toString() {
         return new Integer(value).toString();
     }
 
 	public DataEntity toType(int type) {
 		if (type == java.sql.Types.FLOAT)
			return new Float(value);
 		else if (type == java.sql.Types.DECIMAL)
 			return new Decimal(new BigDecimal(value));
 		else 
 			return this;
 	}
 	
     public Object toJavaType() {
     	return value;
     }
 }
