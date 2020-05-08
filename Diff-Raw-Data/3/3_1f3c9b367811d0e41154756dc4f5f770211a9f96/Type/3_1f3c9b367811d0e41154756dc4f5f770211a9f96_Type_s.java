 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.language;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.ast.IntValue;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.math.BigInteger;
 import static me.pavlina.alco.language.IntLimits.*;
 
 /**
  * Alpha type. */
 public class Type implements HasType {
 
     private String name;
     /* This ivar has a few uses, depending on the encoding:
      *  - ARRAY: Array type (for int[], it will contain int)
      *  - POINTER: Pointer type (for int*, it will contain int)
      *  - OBJECT: Arguments (for map<string, int>, it will contain string
      *      and int.
      */
     private List<Type> subtypes;
     private int size;
     private Encoding encoding;
     private boolean isConst;
 
 
     /**
      * Initialise a type. This is a quick constructor that will build a nested
      * type from specification. The param documentation provides example
      * values; these are for building the type map&lt;string, int&gt;[]*[]
      * (array of pointers to arrays of maps(string to int). Any of these params
      * (except name) may be null, including mods.
      * @param env Compile environment
      * @param name Base name of the type: "map"
      * @param args Array arguments: {Type("string"), Type("int")}
      * @param mods Modifiers: {ARRAY, POINTER, ARRAY} */
     public Type (Env env, String name, List<Type> args, Modifier... mods) {
         
         int modsLength = mods.length;
         isConst = false;
         // Eat any 'const' keywords
         while (modsLength != 0 && mods[modsLength - 1] == Modifier.CONST) {
             isConst = true;
         }
 
         // If there are no mods, type creation is simple.
         if (modsLength == 0) {
             this.baseType (env, name, args);
         }
 
         // Otherwise, the root type is the last mod, and recurse.
         else if (mods[modsLength - 1] == Modifier.ARRAY) {
             this.name = "";
             this.size = Type.OBJECT_SIZE;
             this.encoding = Encoding.ARRAY;
             this.subtypes = new ArrayList<Type> (1);
             this.subtypes.add (new Type (env, name, args, Arrays.copyOf
                                          (mods, modsLength - 1)));
         } else /* Modifier.POINTER */ {
             this.name = "";
             this.size = env.getBits () / 8;
             this.encoding = Encoding.POINTER;
             this.subtypes = new ArrayList<Type> (1);
             this.subtypes.add (new Type (env, name, args, Arrays.copyOf
                                          (mods, modsLength - 1)));
         }
     }
 
     /**
      * Get the unnamed null type. */
     public static Type getNull () {
         return nullType;
     }
 
     private static Type nullType;
     static {
         nullType = new Type ();
         nullType.name = "";
         nullType.size = 0;
         nullType.encoding = Encoding.NULL;
         nullType.subtypes = null;
     }
 
     /**
      * Basic type initialiser */
     private void baseType (Env env, String name, List<Type> args) {
         this.name = name;
         Encoding enc = this.PRIMITIVE_ENCODINGS.get (name);
         if (enc == null) {
             // Object type
             this.size = Type.OBJECT_SIZE;
             this.encoding = Encoding.OBJECT;
             if (args == null)
                 this.subtypes = null;
             else
                 this.subtypes = new ArrayList<Type> (args);
         } else {
             this.size = Type.PRIMITIVE_SIZES.get (name);
             if (this.size == -1)
                 this.size = env.getBits () / 8;
             this.encoding = enc;
             this.subtypes = null;
         }
     }
 
     private Type () {}
 
     /**
      * Get the basic name of the type. Arrays and pointers will return an
      * empty string. */
     public String getName () {
         return name;
     }
     
     /**
      * Returns this. */
     public Type getType () {
         return this;
     }
 
     /**
      * Cheat: returns null. */
     public Token getToken () {
         return null;
     }
 
     /**
      * Get the type arguments. Returns null for non-object types. */
     public List<Type> getArguments () {
         if (encoding == Encoding.OBJECT)
             return Collections.unmodifiableList (subtypes);
         else
             return null;
     }
 
     /**
      * Get the subtype. Returns null for types other than array and pointer. */
     public Type getSubtype () {
         if (encoding == Encoding.ARRAY || encoding == Encoding.POINTER)
             return subtypes.get (0);
         else
             return null;
     }
 
     /**
      * Get the size in bytes. */
     public int getSize () {
         return size;
     }
 
     /**
      * Get the encoding. */
     public Encoding getEncoding () {
         return encoding;
     }
 
     /**
      * Get whether the type is constant */
     public boolean isConst () {
         return isConst;
     }
 
     /**
      * Return a copy of this which is constant. */
     public Type getConst () {
         Type t = new Type ();
         t.name = name;
         t.subtypes = subtypes;
         t.size = size;
         t.encoding = encoding;
         t.isConst = true;
         return t;
     }
 
     /**
      * Return a copy of this which is not constant. */
     public Type getNotConst () {
         Type t = new Type ();
         t.name = name;
         t.subtypes = subtypes;
         t.size = size;
         t.encoding = encoding;
         t.isConst = false;
         return t;
     }
 
     /**
      * Get the encoded type name. This is used in code, both in the dynamic
      * type system and in method name mangling. It is fully reversible
      * (see fromEncodedName()). See Standard:CallingConvention:NameMangling */
     public String getEncodedName () {
         // Standard:CallingConvention:NameMangling
         String prefix = (isConst ? "K" : "");
         if (encoding == Encoding.SINT) {
             switch (size) {
             case 1: return prefix + "A";
             case 2: return prefix + "B";
             case 4: return prefix + "C";
             case 8: return prefix + "D";
             }
         } else if (encoding == Encoding.UINT) {
             switch (size) {
             case 1: return prefix + "E";
             case 2: return prefix + "F";
             case 4: return prefix + "G";
             case 5: return prefix + "H";
             }
         } else if (encoding == Encoding.FLOAT) {
             switch (size) {
             case 4: return prefix + "I";
             case 8: return prefix + "J";
             }
         } else if (encoding == Encoding.POINTER) {
             return prefix + "P" + subtypes.get (0).getEncodedName ();
         } else if (encoding == Encoding.ARRAY) {
             return prefix + "Q" + subtypes.get (0).getEncodedName ();
         } else if (encoding == Encoding.OBJECT) {
             return prefix + "M" + Integer.toString (name.length ()) + name;
         }
         throw new RuntimeException ("getEncodedName() on invalid type");
     }
 
     /**
      * Get a Type from an encoded type name. Returns null on error.
      * Create a MangleReader to read from the encoded name. A single
      * MangleReader can be used to get multiple types from one string. */
     public static Type fromEncodedName (Env env, MangleReader reader) {
         try {
             char ch = reader.nextChar ();
             switch (ch) {
             case 'A': return new Type (env, "i8", null);
             case 'B': return new Type (env, "i16", null);
             case 'C': return new Type (env, "int", null);
             case 'D': return new Type (env, "i64", null);
             case 'E': return new Type (env, "u8", null);
             case 'F': return new Type (env, "u16", null);
             case 'G': return new Type (env, "unsigned", null);
             case 'H': return new Type (env, "u64", null);
             case 'I': return new Type (env, "float", null);
             case 'J': return new Type (env, "double", null);
             case 'P':
                 {
                     Type baseType = Type.fromEncodedName (env, reader);
                     if (baseType == null) return null;
                     Type ty = new Type ();
                     ty.name = "";
                     ty.subtypes = new ArrayList<Type> (1);
                     ty.subtypes.add (baseType);
                     ty.size = env.getBits () / 8;
                     ty.encoding = Encoding.POINTER;
                     return ty;
                 }
             case 'Q':
                 {
                     Type baseType = Type.fromEncodedName (env, reader);
                     if (baseType == null) return null;
                     Type ty = new Type ();
                     ty.name = "";
                     ty.subtypes = new ArrayList<Type> (1);
                     ty.subtypes.add (baseType);
                     ty.size = Type.OBJECT_SIZE;
                     ty.encoding = Encoding.ARRAY;
                     return ty;
                 }
             case 'M':
                 {
                     int len = reader.nextInt ();
                     String name = reader.nextString (len);
                     Type ty = new Type ();
                     ty.name = name;
                     ty.subtypes = null;
                     ty.size = Type.OBJECT_SIZE;
                     ty.encoding = Encoding.OBJECT;
                     return ty;
                 }
             default:
                 return null;
             }
         } catch (IndexOutOfBoundsException e) {
             return null;
         } catch (NumberFormatException e) {
             return null;
         }
     }
 
     /**
      * Coerce type for assignment. See Standard:Types:Casting:ImplicitCasts
      * @throws CError on coercion error
      * @returns Coerced value */
     public static HasType coerce (HasType value, HasType destination,
                                   CastCreator creator, Env env) throws CError
     {
         Type vtype = value.getType ();
         Type dtype = destination.getType ();
 
         if (vtype.equals (dtype)) {
             // Same type - no cast
             return value;
 
         } else if (vtype.equalsNoConst (dtype) && !vtype.isConst () &&
                    dtype.isConst ()) {
             // T to T const
             return creator.cast (value, dtype, env);
 
         } else if (vtype.encoding == Encoding.SINT
             && dtype.encoding == vtype.encoding
             && vtype.size <= dtype.size) {
             // SIa to SIb where b >= a (signed upcast)
             return creator.cast (value, dtype, env);
 
         } else if (vtype.encoding == Encoding.UINT
                    && dtype.encoding == vtype.encoding
                    && vtype.size <= dtype.size) {
             // UIa to UIb where b >= a (unsigned upcast)
             return creator.cast (value, dtype, env);
         
         } else if (IntValue.class.isInstance (value)
                    && dtype.encoding == Encoding.UINT) {
             // The standard mentions both:
             //    IntValue within SIa to SIa
             //    IntValue within UIa to UIa
             // Because IntValues are implicitly i32/i64, the first one will
             // be done by "SIa to SIb". However, "SIa to UIb" is normally
             // illegal, so we have to specifically check for it.
             
             IntValue iv = (IntValue) value;
             BigInteger val = iv.getValue (), min, max;
 
             min = BigInteger.ZERO;
             if (dtype.size == 1)
                 max = U8_MAX;
             else if (dtype.size == 2)
                 max = U16_MAX;
             else if (dtype.size == 4)
                 max = U32_MAX;
             else if (dtype.size == 8)
                 max = U64_MAX;
             else
                 throw new RuntimeException ("Bad type size");
             if (min.compareTo (val) <= 0
                 && max.compareTo (val) >= 0) {
                 iv.setType (dtype);
                 return value;
             }
         } else if (IntValue.class.isInstance (value)
                    && dtype.encoding == Encoding.SINT) {
             // The standard mentions both:
             //    IntValue within SIa to SIa
             //    IntValue within UIa to UIa
             // Because IntValues are implicitly i32/i64, the first one will
             // be done by "SIa to SIb". However, "SIa to UIb" is normally
             // illegal, so we have to specifically check for it.
             
             IntValue iv = (IntValue) value;
             BigInteger val = iv.getValue (), min, max;
 
             if (dtype.size == 1) {
                 min = I8_MIN;
                 max = I8_MAX;
             } else if (dtype.size == 2) {
                 min = I16_MIN;
                 max = I16_MAX;
             } else if (dtype.size == 4) {
                 min = I32_MIN;
                 max = I32_MAX;
             } else if (dtype.size == 8) {
                 min = I64_MIN;
                 max = I64_MAX;
             } else
                 throw new RuntimeException ("Bad type size");
             if (min.compareTo (val) <= 0
                 && max.compareTo (val) >= 0) {
                 iv.setType (dtype);
                 return value;
             }
         } else if (vtype.encoding == Encoding.ARRAY
                    && dtype.encoding == Encoding.POINTER
                    && vtype.getSubtype ().equals (dtype.getSubtype ())) {
             // T[] to T*
             throw new RuntimeException ("T[] to T* cast not implemented yet");
 
         } else if (vtype.encoding == Encoding.NULL
                    && (dtype.encoding == Encoding.SINT ||
                        dtype.encoding == Encoding.UINT ||
                        dtype.encoding == Encoding.OBJECT ||
                        dtype.encoding == Encoding.ARRAY ||
                        dtype.encoding == Encoding.POINTER))
             // Null to SI, UI, class, T[], T*
             return creator.cast (value, dtype, env);
 
         throw CError.at ("invalid implicit cast: " + vtype.toString ()
                          + " to " + dtype.toString (), value.getToken ());
     }
 
     /**
      * Check whether a value can be coerced to a type.
      * @returns Whether the coercion is possible
      */
     public static boolean canCoerce (HasType value, HasType destination)
         throws CError
     {
         Type vtype = value.getType ();
         Type dtype = destination.getType ();
 
         if (vtype.equals (dtype)) {
             // Same type - no cast
             return true;
 
         } else if (vtype.equalsNoConst (dtype) && !vtype.isConst () &&
                    dtype.isConst ()) {
             // T to T const
             return true;
 
         } else if (vtype.encoding == Encoding.SINT
             && dtype.encoding == vtype.encoding
             && vtype.size <= dtype.size) {
             // SIa to SIb where b >= a (signed upcast)
             return true;
 
         } else if (vtype.encoding == Encoding.UINT
                    && dtype.encoding == vtype.encoding
                    && vtype.size <= dtype.size) {
             // UIa to UIb where b >= a (unsigned upcast)
             return true;
         
         } else if (IntValue.class.isInstance (value)
                    && dtype.encoding == Encoding.UINT) {
             // The standard mentions both:
             //    IntValue within SIa to SIa
             //    IntValue within UIa to UIa
             // Because IntValues are implicitly i32/i64, the first one will
             // be done by "SIa to SIb". However, "SIa to UIb" is normally
             // illegal, so we have to specifically check for it.
             
             IntValue iv = (IntValue) value;
             BigInteger val = iv.getValue (), min, max;
 
             min = BigInteger.ZERO;
             if (dtype.size == 1)
                 max = U8_MAX;
             else if (dtype.size == 2)
                 max = U16_MAX;
             else if (dtype.size == 4)
                 max = U32_MAX;
             else if (dtype.size == 8)
                 max = U64_MAX;
             else
                 throw new RuntimeException ("Bad type size");
             if (min.compareTo (val) <= 0
                 && max.compareTo (val) >= 0) {
                iv.setType (dtype);
                 return true;
             }
         } else if (IntValue.class.isInstance (value)
                    && dtype.encoding == Encoding.SINT) {
             // The standard mentions both:
             //    IntValue within SIa to SIa
             //    IntValue within UIa to UIa
             // Because IntValues are implicitly i32/i64, the first one will
             // be done by "SIa to SIb". However, "SIa to UIb" is normally
             // illegal, so we have to specifically check for it.
             
             IntValue iv = (IntValue) value;
             BigInteger val = iv.getValue (), min, max;
 
             if (dtype.size == 1) {
                 min = I8_MIN;
                 max = I8_MAX;
             } else if (dtype.size == 2) {
                 min = I16_MIN;
                 max = I16_MAX;
             } else if (dtype.size == 4) {
                 min = I32_MIN;
                 max = I32_MAX;
             } else if (dtype.size == 8) {
                 min = I64_MIN;
                 max = I64_MAX;
             } else
                 throw new RuntimeException ("Bad type size");
             if (min.compareTo (val) <= 0
                 && max.compareTo (val) >= 0) {
                iv.setType (dtype);
                 return true;
             }
         } else if (vtype.encoding == Encoding.ARRAY
                    && dtype.encoding == Encoding.POINTER
                    && vtype.getSubtype ().equals (dtype.getSubtype ())) {
             // T[] to T*
             throw new RuntimeException ("T[] to T* cast not implemented yet");
 
         } else if (vtype.encoding == Encoding.NULL
                    && (dtype.encoding == Encoding.SINT ||
                        dtype.encoding == Encoding.UINT ||
                        dtype.encoding == Encoding.OBJECT ||
                        dtype.encoding == Encoding.ARRAY ||
                        dtype.encoding == Encoding.POINTER))
             // Null to SI, UI, class, T[], T*
             return true;
 
         return false;
     }
 
     /**
      * Return whether two types are equivalent.
      *  - Integer: size, sign and const are equal
      *  - Float: size and const are equal
      *  - Array: subtype and const are equal
      *  - Pointer: subtype and const are equal
      *  - Object: name, arguments and const are equal
      *
      * Therefore: i32 == int, i32* == int*, list&lt;i32&gt; == list&lt;int&gt;
      *  i32 const != i32
      */
     public boolean equals (Type type) {
         if (type.encoding != encoding) return false;
         if (encoding == Encoding.UINT ||
             encoding == Encoding.SINT ||
             encoding == Encoding.FLOAT) {
             return size == type.size && isConst == type.isConst;
         }
         else if (encoding == Encoding.ARRAY ||
                  encoding == Encoding.POINTER) {
             return subtypes.get (0).equals (type.subtypes.get (0))
                 && isConst == type.isConst;
         }
         else if (encoding == Encoding.OBJECT) {
             if (!name.equals (type.name)) return false;
             if (subtypes.size () != type.subtypes.size ()) return false;
             if (isConst != type.isConst) return false;
             for (int i = 0; i < subtypes.size (); ++i) {
                 if (! subtypes.get (i).equals (type.subtypes.get (i)))
                     return false;
             }
             return true;
         }
         return false;
     }
 
     /**
      * Return whether two types are equivalent, ignoring const. */
     public boolean equalsNoConst (Type type) {
         if (type.encoding != encoding) return false;
         if (encoding == Encoding.UINT ||
             encoding == Encoding.SINT ||
             encoding == Encoding.FLOAT) {
             return size == type.size;
         }
         else if (encoding == Encoding.ARRAY ||
                  encoding == Encoding.POINTER) {
             return subtypes.get (0).equals (type.subtypes.get (0));
         }
         else if (encoding == Encoding.OBJECT) {
             if (!name.equals (type.name)) return false;
             if (subtypes.size () != type.subtypes.size ()) return false;
             for (int i = 0; i < subtypes.size (); ++i) {
                 if (! subtypes.get (i).equals (type.subtypes.get (i)))
                     return false;
             }
             return true;
         }
         return false;
     }
 
 
 
     /**
      * Return a string representing the type.
      *  - Integer, Float: base name
      *  - Array: subtype string + "[]"
      *  - Pointer: subtype string + "*"
      *  - Object: base name + args in &lt;&gt;
      */
     public String toString () {
         if (encoding == Encoding.UINT ||
             encoding == Encoding.SINT ||
             encoding == Encoding.FLOAT)
             return name + (isConst ? " const" : "");
         else if (encoding == Encoding.ARRAY)
             return subtypes.get (0).toString () + "[]"
                 + (isConst ? " const" : "");
         else if (encoding == Encoding.POINTER)
             return subtypes.get (0).toString () + "*"
                 + (isConst ? " const" : "");
         else if (encoding == Encoding.NULL)
             return "null" + (isConst ? " const" : "");
         else if (encoding == Encoding.OBJECT) {
             if (subtypes == null) return name;
             StringBuilder sb = new StringBuilder (name);
             sb.append ('<');
             boolean first = true;
             for (Type i: subtypes) {
                 if (first) first = false;
                 else sb.append (", ");
                 sb.append (i.toString ());
             }
             sb.append ('>');
             return sb.toString () + (isConst ? " const" : "");
         }
         return super.toString ();
     }
 
     /**
      * Interface for a cast creator. This takes a value and a desired type, and
      * wraps the value in a cast to it. */
     public static interface CastCreator {
         public HasType cast (HasType value, Type type, Env env);
     }
 
     /**
      * All possible type encodings. */
     public enum Encoding { UINT, SINT, FLOAT, ARRAY, POINTER, OBJECT, NULL }
 
     /**
      * Type modifiers */
     public enum Modifier { ARRAY, POINTER, CONST }
 
     private static Map<String, Encoding> PRIMITIVE_ENCODINGS;
     private static Map<String, Integer> PRIMITIVE_SIZES;
     private static Map<Encoding, String[]> ENCODED_NAMES;
     public static final int OBJECT_SIZE = 16;
     static {
         PRIMITIVE_ENCODINGS = new HashMap<String, Encoding> ();
         PRIMITIVE_SIZES = new HashMap<String, Integer> ();
         ENCODED_NAMES = new HashMap<Encoding, String[]> ();
 
         PRIMITIVE_ENCODINGS.put ("i8",       Encoding.SINT);
         PRIMITIVE_ENCODINGS.put ("i16",      Encoding.SINT);
         PRIMITIVE_ENCODINGS.put ("i32",      Encoding.SINT);
         PRIMITIVE_ENCODINGS.put ("i64",      Encoding.SINT);
         PRIMITIVE_ENCODINGS.put ("u8",       Encoding.UINT);
         PRIMITIVE_ENCODINGS.put ("u16",      Encoding.UINT);
         PRIMITIVE_ENCODINGS.put ("u32",      Encoding.UINT);
         PRIMITIVE_ENCODINGS.put ("u64",      Encoding.UINT);
         PRIMITIVE_ENCODINGS.put ("int",      Encoding.SINT);
         PRIMITIVE_ENCODINGS.put ("unsigned", Encoding.UINT);
         PRIMITIVE_ENCODINGS.put ("size",     Encoding.UINT);
         PRIMITIVE_ENCODINGS.put ("ssize",    Encoding.SINT);
         PRIMITIVE_ENCODINGS.put ("float",    Encoding.FLOAT);
         PRIMITIVE_ENCODINGS.put ("double",   Encoding.FLOAT);
 
         PRIMITIVE_SIZES.put ("i8",       1);
         PRIMITIVE_SIZES.put ("i16",      2);
         PRIMITIVE_SIZES.put ("i32",      4);
         PRIMITIVE_SIZES.put ("i64",      8);
         PRIMITIVE_SIZES.put ("u8",       1);
         PRIMITIVE_SIZES.put ("u16",      2);
         PRIMITIVE_SIZES.put ("u32",      4);
         PRIMITIVE_SIZES.put ("u64",      8);
         PRIMITIVE_SIZES.put ("int",      4);
         PRIMITIVE_SIZES.put ("unsigned", 4);
         PRIMITIVE_SIZES.put ("size",    -1);
         PRIMITIVE_SIZES.put ("ssize",   -1);
         PRIMITIVE_SIZES.put ("float",    4);
         PRIMITIVE_SIZES.put ("double",   8);
 
         ENCODED_NAMES.put (Encoding.POINTER, new String[] {"",  "",  "p", "p"});
         ENCODED_NAMES.put (Encoding.ARRAY,   new String[] {"",  "",  "q", "q"});
         ENCODED_NAMES.put (Encoding.FLOAT,   new String[] {"",  "",  "F", "f"});
         ENCODED_NAMES.put (Encoding.SINT,    new String[] {"A", "B", "C", "D"});
         ENCODED_NAMES.put (Encoding.UINT,    new String[] {"a", "b", "c", "d"});
     }
 }
 
