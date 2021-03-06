 /**
  * a small, limited port of Python's Struct, based on java.nio.ByteBuffer
  * @author zigliolie - Copyright Sirtrack Ltd.
  *
  */
 package construct;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 
 /**
 Functions to convert between Python values and C structs.
 Python strings are used to hold the data representing the C struct
 and also as format strings to describe the layout of data in the C struct.
 
 The optional first format char indicates byte order, size and alignment:
  @: native order, size & alignment (default)
  =: native order, std. size & alignment
  <: little-endian, std. size & alignment
  >: big-endian, std. size & alignment
  !: same as >
 
 The remaining chars indicate types of args and must match exactly;
 these can be preceded by a decimal repeat count:
    x: pad byte (no data);
    c:char;
    b:signed byte;
    B:unsigned byte;
    h:short;
    H:unsigned short;
    i:int;
    I:unsigned int;
    l:long;
    L:unsigned long;
    f:float;
    d:double.
 Special cases (preceding decimal count indicates length):
    s:string (array of char); p: pascal string (with count byte).
 Special case (only available in native format):
    P:an integer type that is wide enough to hold a pointer.
 Special case (not in native mode unless 'long long' in platform C):
    q:long long;
    Q:unsigned long long
 Whitespace between formats is ignored.
 
 The variable struct.error is an exception raised on errors. *
  */
 public class Packer {
   
 	static public class StructError extends RuntimeException{
   	public StructError( String err ){
   		super(err);
   	}
   }
 
   char fmt;
   char endianity;
 
   public Packer( char endianity, char fmt )
   {
     this.endianity = endianity;
     this.fmt = fmt;
   }
 
   public Object[] unpack( ByteBuffer buf )
   {
       ArrayList<Object> result = new ArrayList<Object>();
       Object obj;
       
       if( endianity == '>' )
         buf.order( ByteOrder.BIG_ENDIAN ); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
       else if( endianity == '<' )
         buf.order( ByteOrder.LITTLE_ENDIAN ); 
       else if( endianity == '=' )
         buf.order( ByteOrder.nativeOrder() ); 
   
       switch( fmt ){
       	case 'b':
      	case 'B':
      	case 'C':
       	case 's':
       	case 'p':
       		obj = new Integer(buf.get());
       	break;
 
       	case 'h':
      	case 'H':
       		obj = new Integer(buf.getShort());
       	break;
       	
       	case 'i':
       	case 'I':
       	case 'l':
       	case 'L':
       			obj = buf.getInt();
      		break;
      		default:
       			throw new StructError( "unrecognized fmt " + fmt);
       }
      result.add( obj );
      return result.toArray();
   }
 
   static public byte getByte( Object obj ){
     if( obj instanceof Byte )
     	return( (Byte)obj );
     else if( obj instanceof Short && (Short)obj < 256 ){
     	return (byte)((Short)obj & 0xFF);
     }
     else if( obj instanceof Integer && (Integer)obj < 256 ){
     	return (byte)((Integer)obj & 0xFF);
     }
   //else if( obj instanceof Character )
   //  b.putChar( (Character)obj );
   //else if( obj instanceof Double )
   //  b.putDouble( (Double)obj );
   //else if( obj instanceof Float )
   //  b.putFloat( (Float)obj );
   //else if( obj instanceof Integer )
   //  b.putInt( (Integer)obj );
   //else if( obj instanceof Long )
   //  b.putLong( (Long)obj );
   //else if( obj instanceof Short )
   //  b.putShort( (Short)obj );
   	throw new RuntimeException( "type not supported " + obj );
   }
 
   static public short getShort( Object obj ){
     if( obj instanceof Short ){
     	return( (Short)obj );
     }
     else if( obj instanceof Integer && (Integer)obj < 65536 ){
     	return (short)((Integer)obj & 0xFFFF );
     }
   //else if( obj instanceof Character )
   //  b.putChar( (Character)obj );
   //else if( obj instanceof Double )
   //  b.putDouble( (Double)obj );
   //else if( obj instanceof Float )
   //  b.putFloat( (Float)obj );
   //else if( obj instanceof Integer )
   //  b.putInt( (Integer)obj );
   //else if( obj instanceof Long )
   //  b.putLong( (Long)obj );
   //else if( obj instanceof Short )
   //  b.putShort( (Short)obj );
   	throw new RuntimeException( "type not supported " + obj );
   }
   
   /*
    * @return  Return byte[] containing value v  packed according to fmt.
    */
   public byte[] pack( Object obj )
   {
     ByteBuffer b = ByteBuffer.allocate( length() );
     
     if( endianity == '>' )
       b.order( ByteOrder.BIG_ENDIAN ); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
     else if( endianity == '<' )
       b.order( ByteOrder.LITTLE_ENDIAN ); 
     else if( endianity == '=' )
       b.order( ByteOrder.nativeOrder() ); 
     
     switch( fmt ){
     	case 'b':
     	case 'B':
     	case 'C':
     	case 's':
     	case 'p':
     		b.put( getByte(obj) );
     	break;
   
     	case 'h':
     	case 'H':
     		b.putShort( getShort(obj) );
     	break;
     	
     	case 'i':
     	case 'I':
     	case 'l':
     	case 'L':
   //      if( obj instanceof Integer )
         	b.putInt( (Integer)obj );
    		break;
    		default:
     			throw new StructError( "unrecognized fmt " + fmt);
     }
     return Arrays.copyOf( b.array(), b.position() );
   }
 
   public int length(){
   	int len = 0;
     switch( fmt ){
     	case 'b':
     	case 'B':
     	case 'C':
     	case 's':
     	case 'p':
     		len = 1;
     	break;
     
     	case 'h':
     	case 'H':
     		len = 2;
     	break;
     	
     	case 'i':
     	case 'I':
     	case 'l':
     	case 'L':
     		len = 4;
     	break;
     	default:
     		throw new StructError( "unrecognized fmt " + fmt );
     }
     return len;
   }
 }
