 package org.eclipse.dltk.evaluation.types;
 
 import org.eclipse.dltk.ti.types.ClassType;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 
/**
 * @deprecated
 */
 public class SimpleType extends ClassType implements IClassType
 {
 	public final static int TYPE_STRING = 0;
 	public final static int TYPE_NUMBER = 1;
 	public final static int TYPE_ARRAY = 2;	
 	public final static int TYPE_LIST = 3;
 	public final static int TYPE_DICT = 4;
 	public final static int TYPE_BOOLEAN = 5;
 	public final static int TYPE_NONE = 6;
 	public final static int TYPE_TUPLE = 7;
 	public final static int TYPE_NULL = 8;
 		
 	private int fType;
 
 	public SimpleType( int type ) {
 
 		this.fType = type;
 	}
 
 	public String getTypeName( ) {
 
 		return getTypeString( this.fType );
 	}
 	
 	public int getType() {
 		return this.fType;
 	}
 	/**
 	 * Return type string for selected type.
 	 * @param type
 	 * @return
 	 */
 	public static String getTypeString( int type ) {
 		switch( type ) {
 			case TYPE_STRING:
 				return "string";
 			case TYPE_NUMBER:
 				return "number";
 			case TYPE_ARRAY:
 				return "array";
 			case TYPE_LIST:
 				return "list";
 			case TYPE_DICT:
 				return "dict";
 			case TYPE_BOOLEAN:
 				return "boolean";
 			case TYPE_TUPLE:
 				return "tuple";
 			case TYPE_NONE:
 				return "void";
 			case TYPE_NULL:
 				return "NULL";
 		}
 		return "unknown";
 	}
 	
 	public int hashCode() {
 		return fType ^ 0xDEADBEEF;
 	}
 
 	public boolean equals( Object obj ) {
 
 		if( obj instanceof SimpleType ) {
 			SimpleType o2 = (SimpleType)obj;
 			return this.fType == o2.fType;
 		}
 		return false;
 	}
 
 	public boolean subtypeOf(IEvaluatedType type) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public String getModelKey() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
