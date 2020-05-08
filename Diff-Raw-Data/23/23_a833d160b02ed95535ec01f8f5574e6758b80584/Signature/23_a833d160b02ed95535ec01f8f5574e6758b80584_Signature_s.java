 package se.l4.sofa.dbus.spi;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 
 import se.l4.sofa.dbus.DType;
 
 /**
  * DBus signature, signatures are used to define how to marshall and unmarshall
  * data (serialize and deserialize in Java-terms).
  *  
  * @author Andreas Holstenson
  *
  */
 public class Signature
 {
 	private final String value;
 	private final SubSignature[] subs;
 	
 	private Signature(SubSignature[] subs)
 	{
 		this.subs = subs;
 		this.value = toSignatureString();
 	}
 	
 	/**
 	 * Get the value of the signature as a string.
 	 * @return
 	 */
 	public String getValue()
 	{
 		return value;
 	}
 	
 	/**
 	 * Get all of the sub signatures of this signature.
 	 * 
 	 * @return
 	 */
 	public SubSignature[] getSignatures()
 	{
 		return subs;
 	}
 	
 	private String toSignatureString()
 	{
 		StringBuilder b = new StringBuilder();
 		for(SubSignature s : subs)
 		{
 			s.toString(b);
 		}
 		return b.toString();
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "Signature[" + value + "]";
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj)
 	{
 		if(this == obj)
 			return true;
 		if(obj == null)
 			return false;
 		if(getClass() != obj.getClass())
 			return false;
 		Signature other = (Signature) obj;
 		if(value == null)
 		{
 			if(other.value != null)
 				return false;
 		}
 		else if(!value.equals(other.value))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Part of a larger signature, can in turn contain other sub signatures
 	 * if the type supports it.
 	 * 
 	 * @author Andreas Holstenson
 	 *
 	 */
 	public static class SubSignature
 	{
 		private static final EnumMap<DType, SubSignature> cache;
 		
 		static
 		{
 			cache = new EnumMap<DType, SubSignature>(DType.class);
 			
 			for(DType d : DType.values())
 			{
 				cache.put(d, new SubSignature(d));
 			}
 		}
 		
 		private final DType type;
 		private final SubSignature[] subs;
 		
 		public SubSignature(DType type)
 		{
 			this(type, EMTPY_ARRAY);
 		}
 		
 		public SubSignature(DType type, SubSignature[] subs)
 		{
 			this.type = type;
 			this.subs = subs;
 		}
 		
 		public DType getType()
 		{
 			return type;
 		}
 		
 		public SubSignature[] getSignatures()
 		{
 			return subs;
 		}
 		
 		@Override
 		public String toString()
 		{
 			StringBuilder b = new StringBuilder();
 			toString(b);
 			return b.toString();
 		}
 		
 		/**
 		 * Build the string representation of the signature in the given
 		 * {@link StringBuilder}.
 		 * 
 		 * @param b
 		 */
 		public void toString(StringBuilder b)
 		{
 			switch(type)
 			{
 				case BYTE:
 					b.append(TYPE_BYTE);
 					break;
 				case BOOLEAN:
 					b.append(TYPE_BOOLEAN);
 					break;
 				case INT16:
 					b.append(TYPE_INT16);
 					break;
 				case UINT16:
 					b.append(TYPE_UINT16);
 					break;
 				case INT32:
 					b.append(TYPE_INT32); 
 					break;
 				case UINT32:
 					b.append(TYPE_UINT32);
 					break;
 				case INT64:
 					b.append(TYPE_INT64);
 					break;
 				case UINT64:
 					b.append(TYPE_UINT64);
 					break;
 				case DOUBLE:
 					b.append(TYPE_DOUBLE);
 					break;
 				case STRING:
 					b.append(TYPE_STRING);
 					break;
 				case OBJECT_PATH:
 					b.append(TYPE_OBJECT_PATH);
 					break;
 				case SIGNATURE:
 					b.append(TYPE_SIGNATURE);
 					break;
 				case ARRAY:
 					b.append(TYPE_ARRAY);
 					for(SubSignature s : subs)
 					{
 						s.toString(b);
 					}
 					break;
 				case VARIANT:
 					b.append(TYPE_VARIANT);
 					break;
 				case STRUCT:
 					b.append(TYPE_STRUCT_BEGIN);
 					for(SubSignature s : subs)
 					{
 						s.toString(b);
 					}
 					b.append(TYPE_STRUCT_END);
 					break;
 				case DICT_ENTRY:
 					b.append(TYPE_DICT_ENTRY_BEGIN);
 					for(SubSignature s : subs)
 					{
 						s.toString(b);
 					}
 					b.append(TYPE_DICT_ENTRY_END);
 					break;
 				default:
 					b.append('?');
 					break;
 			}
 		}
 		
 		/**
 		 * Get a sub signature based on the given type. Does not allow for
 		 * further sub types.
 		 * 
 		 * @param type
 		 * @return
 		 */
 		public static SubSignature get(DType type)
 		{
 			return cache.get(type);
 		}
 	}
 	
 	/**
 	 * Parse a given signature string and return an instance of 
 	 * {@code Signature}.
 	 * 
 	 * @param signatureString
 	 * 		signature to parse
 	 * @throws IllegalArgumentException
 	 * 		if unable to parse the string due to signature errors
 	 * @return
 	 * 		signature instance
 	 */
 	public static Signature parse(String signatureString)
 	{
 		if("".equals(signatureString))
 		{
 			return EMTPY_SIGNATURE;
 		}
			
 		List<SubSignature> subs = new ArrayList<SubSignature>(signatureString.length());
 		MutableInt idx = new MutableInt();
		parse(signatureString, subs, idx);
 		
 		return new Signature(subs.toArray(EMTPY_ARRAY));
 	}
 	
 	/**
 	 * Create a signature from an array of sub signatures.
 	 * 
 	 * @param subs
 	 * @return
 	 */
 	public static Signature from(SubSignature... subs)
 	{
 		return new Signature(subs);
 	}
 	
 	/**
 	 * Create a signature from a list of sub signatures.
 	 * 
 	 * @param subs
 	 * @return
 	 */
 	public static Signature from(List<SubSignature> subs)
 	{
 		return from(subs.toArray(EMTPY_ARRAY));
 	}
 	
 	public static final SubSignature[] EMTPY_ARRAY = new SubSignature[0];
 	public static final Signature EMTPY_SIGNATURE = new Signature(EMTPY_ARRAY);
 	
 	private static final char TYPE_BYTE = 'y';
 	private static final char TYPE_BOOLEAN = 'b';
 	private static final char TYPE_INT16 = 'n';
 	private static final char TYPE_UINT16 = 'q';
 	private static final char TYPE_INT32 = 'i';
 	private static final char TYPE_UINT32 = 'u';
 	private static final char TYPE_INT64 = 'x';
 	private static final char TYPE_UINT64 = 't';
 	private static final char TYPE_DOUBLE = 'd';
 	private static final char TYPE_STRING = 's';
 	private static final char TYPE_OBJECT_PATH = 'o';
 	private static final char TYPE_SIGNATURE = 'g';
 	private static final char TYPE_ARRAY = 'a';
 	private static final char TYPE_VARIANT = 'v';
 	private static final char TYPE_STRUCT = 'r';
 	private static final char TYPE_STRUCT_BEGIN = '(';
 	private static final char TYPE_STRUCT_END = ')';
 	private static final char TYPE_DICT_ENTRY = 'e';
 	private static final char TYPE_DICT_ENTRY_BEGIN = '{';
 	private static final char TYPE_DICT_ENTRY_END = '}';
 	
 	private static void parse(String s, List<SubSignature> result, MutableInt idx)
 	{
 		char c = s.charAt(idx.value);
 		idx.value++;
 		
 		switch(c)
 		{
 			case TYPE_BYTE:
 				result.add(SubSignature.get(DType.BYTE));
 				break;
 			case TYPE_BOOLEAN:
 				result.add(SubSignature.get(DType.BOOLEAN));
 				break;
 			case TYPE_INT16:
 				result.add(SubSignature.get(DType.INT16));
 				break;
 			case TYPE_UINT16:
 				result.add(SubSignature.get(DType.UINT16));
 				break;
 			case TYPE_INT32:
 				result.add(SubSignature.get(DType.INT32));
 				break;
 			case TYPE_UINT32:
 				result.add(SubSignature.get(DType.UINT32));
 				break;
 			case TYPE_INT64:
 				result.add(SubSignature.get(DType.INT64));
 				break;
 			case TYPE_UINT64:
 				result.add(SubSignature.get(DType.UINT64));
 				break;
 			case TYPE_DOUBLE:
 				result.add(SubSignature.get(DType.DOUBLE));
 				break;
 			case TYPE_STRING:
 				result.add(SubSignature.get(DType.STRING));
 				break;
 			case TYPE_OBJECT_PATH:
 				result.add(SubSignature.get(DType.OBJECT_PATH));
 				break;
 			case TYPE_SIGNATURE:
 				result.add(SubSignature.get(DType.SIGNATURE));
 				break;
 			case TYPE_ARRAY:
 				{
 					// Arrays need to recurse down into the signature
 					int len = s.length();
 					if(len <= idx.value)
 					{
 						throw new IllegalArgumentException("Invalid signature " 
 							+ s + "; Array found at " + (idx.value-1) 
 							+ " but signature ends there");
 					}
 					
 					List<SubSignature> subs = new ArrayList<SubSignature>(1);
 					parse(s, subs, idx);
 					result.add(
 						new SubSignature(DType.ARRAY, subs.toArray(EMTPY_ARRAY))
 					);
 				}
 				break;
 			case TYPE_VARIANT:
 				result.add(SubSignature.get(DType.VARIANT));
 				break;
 			case TYPE_STRUCT_BEGIN:
 				{
 					// Structs have any number of subs
 					List<SubSignature> subs = new ArrayList<SubSignature>(10);
 					
 					while(true)
 					{
 						if(s.length() < idx.value + 1)
 						{
 							throw new IllegalArgumentException(
 								"Invalid signature " + s + "; Struct at " 
 								+ (idx.value-1)	+ " not closed with "
 								+ TYPE_STRUCT_END);
 						}
 						
 						char peek = s.charAt(idx.value);
 						
 						if(peek == TYPE_STRUCT_END)
 						{
 							// Also skip last )
 							idx.value++;
 							
 							result.add(
 								new SubSignature(DType.STRUCT, subs.toArray(EMTPY_ARRAY))
 							);
 							break;
 						}
 						else
 						{
 							parse(s, subs, idx);
 						}
 					}
 				}
 				break;
 			case TYPE_DICT_ENTRY_BEGIN:
 				{
 					// Dictionary entry has key and value
 					List<SubSignature> subs = new ArrayList<SubSignature>(2);
 					
 					parse(s, subs, idx); // key
 					parse(s, subs, idx); // value
 					
 					if(s.length() < idx.value + 1)
 					{
 						throw new IllegalArgumentException(
 							"Invalid signature " + s + "; Dictionary entry at " 
 							+ (idx.value-1)	+ " not closed with " 
 							+ TYPE_DICT_ENTRY_END);
 					}
 					
 					char peek = s.charAt(idx.value);
 					if(peek != TYPE_DICT_ENTRY_END)
 					{
 						throw new IllegalArgumentException(
 								"Invalid signature " + s + "; Dictionary entry at " 
 								+ (idx.value-1)	+ " not closed with " 
 								+ TYPE_DICT_ENTRY_END);
 					}
 					
 					idx.value++;
 					
 					result.add(new SubSignature(DType.DICT_ENTRY, subs.toArray(EMTPY_ARRAY)));
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("Invalid signature " + s 
 					+ "; Unknown type " + c + " at " + (idx.value-1));
 		}
 	}
 	
 	private static class MutableInt
 	{
 		int value;
 	}
 }
