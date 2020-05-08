  /*
   * Copyright 2012 Michael Bischoff
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
 
 package de.jpaw.bonaparte.dsl.generator;
 
 // A class to extend the grammar's DataType EObject,
 // in order to provide space for internal extra fields used by the code generator, but also
 // in order to support O(1) lookup of recursive typedefs
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 
 import de.jpaw.bonaparte.dsl.bonScript.ClassReference;
 import de.jpaw.bonaparte.dsl.bonScript.EnumAlphaValueDefinition;
 import de.jpaw.bonaparte.dsl.bonScript.FieldDefaultsDefinition;
 import de.jpaw.bonaparte.dsl.bonScript.PackageDefinition;
 import de.jpaw.bonaparte.dsl.bonScript.TypeDefinition;
 import de.jpaw.bonaparte.dsl.bonScript.ClassDefinition;
 import de.jpaw.bonaparte.dsl.bonScript.DataType;
 import de.jpaw.bonaparte.dsl.bonScript.ElementaryDataType;
 import de.jpaw.bonaparte.dsl.bonScript.XAutoScale;
 import de.jpaw.bonaparte.dsl.bonScript.XRounding;
 import de.jpaw.bonaparte.dsl.bonScript.XTruncating;
 import de.jpaw.bonaparte.dsl.bonScript.XUsePrimitives;
 import de.jpaw.bonaparte.dsl.bonScript.XRequired;
 import de.jpaw.bonaparte.dsl.bonScript.XSignedness;
 import de.jpaw.bonaparte.dsl.bonScript.XSpecialCharsSetting;
 import de.jpaw.bonaparte.dsl.bonScript.XTrimming;
 
 public class DataTypeExtension {
     // constants for enumMaxTokenLength field
     public static final int NO_ENUM = -2;
     public static final int ENUM_NUMERIC = -1;
     
     // a lookup to determine if a data type can (should) be implemented as a Java primitive.
     // (LANGUAGE SPECIFIC: JAVA)
     private static final Set<String> JAVA_PRIMITIVES = new HashSet<String>(Arrays.asList(new String[] {
         "boolean", "int", "long", "float", "double", "byte", "short", "char"
     }));
 
     // a lookup to resolve typedefs. Also collects preprocessed information about a data type
     static private Map<DataType,DataTypeExtension> map = new HashMap<DataType,DataTypeExtension>(200);
 
     // a lookup to determine the Java data type to use for a given grammar type.
     // (LANGUAGE SPECIFIC: JAVA)
     static protected Map<String,DataCategory> dataCategory = new HashMap<String, DataCategory>(32);
     static {
         dataCategory.put("boolean",   DataCategory.MISC);
         dataCategory.put("int",       DataCategory.NUMERIC);
         dataCategory.put("integer",   DataCategory.NUMERIC);
         dataCategory.put("long",      DataCategory.NUMERIC);
         dataCategory.put("float",     DataCategory.NUMERIC);
         dataCategory.put("double",    DataCategory.NUMERIC);
         dataCategory.put("number",    DataCategory.NUMERIC);
         dataCategory.put("decimal",   DataCategory.NUMERIC);
         dataCategory.put("byte",      DataCategory.NUMERIC);
         dataCategory.put("short",     DataCategory.NUMERIC);
         dataCategory.put("char",      DataCategory.MISC);
         dataCategory.put("character", DataCategory.MISC);
         
         dataCategory.put("raw",       DataCategory.MISC);    // not recommended because mutable. Also weird for 2nd level of array index
         dataCategory.put("binary",    DataCategory.MISC);
         dataCategory.put("uuid",      DataCategory.MISC);
         dataCategory.put("calendar",  DataCategory.TEMPORAL);  // not recommended because mutable
         dataCategory.put("timestamp", DataCategory.TEMPORAL);  // temporary solution until JSR 310 has been implemented
         dataCategory.put("day",       DataCategory.TEMPORAL);      // temporary solution until JSR 310 has been implemented
         
         dataCategory.put("uppercase", DataCategory.STRING);
         dataCategory.put("lowercase", DataCategory.STRING);
         dataCategory.put("ascii",     DataCategory.STRING);
         dataCategory.put("unicode",   DataCategory.STRING);
         dataCategory.put("enum",      DataCategory.ENUM);  // artificial entry for enum
         dataCategory.put("object",    DataCategory.OBJECT);  // which is really an object reference instead of an elementary item...
     }
     
     // a lookup to determine the Java data type to use for a given grammar type.
     // (LANGUAGE SPECIFIC: JAVA)
     static protected Map<String,String> dataTypeJava = new HashMap<String, String>(32);
     static {
         dataTypeJava.put("boolean",   "Boolean");
         dataTypeJava.put("int",       "Integer");
         dataTypeJava.put("integer",   "Integer");
         dataTypeJava.put("long",      "Long");
         dataTypeJava.put("float",     "Float");
         dataTypeJava.put("double",    "Double");
         dataTypeJava.put("number",    "Integer");
         dataTypeJava.put("decimal",   "BigDecimal");
         dataTypeJava.put("byte",      "Byte");
         dataTypeJava.put("short",     "Short");
         dataTypeJava.put("char",      "Character");
         dataTypeJava.put("character", "Character");
         
         dataTypeJava.put("raw",       "byte []");    	// not recommended because mutable. Also weird for 2nd level of array index
         dataTypeJava.put("binary",    "ByteArray");
         dataTypeJava.put("uuid",      "UUID");
         dataTypeJava.put("calendar",  "Calendar");  	// not recommended because mutable
         dataTypeJava.put("timestamp", "LocalDateTime"); // temporary solution until JSR 310 has been implemented
         dataTypeJava.put("day",       "LocalDate");     // temporary solution until JSR 310 has been implemented
         
         dataTypeJava.put("uppercase", "String");
         dataTypeJava.put("lowercase", "String");
         dataTypeJava.put("ascii",     "String");
         dataTypeJava.put("unicode",   "String");
         dataTypeJava.put("enum",      "@");  			// artificial entry for enum
         dataTypeJava.put("object",    "BonaPortable");  // which is really an object reference instead of an elementary item...
     }
     
     
     // member variables
     private boolean currentlyVisited = false;
     public ElementaryDataType elementaryDataType;  	// primitive type, enum, unspecified object or boxed type
     public ClassDefinition objectDataType;			// explicit class reference (possibly with generics parameters)
     public boolean orSuperClass;                    // if subclasses are allowed
     public ClassReference genericsRef;				// a generic type argument
     public TypeDefinition typedef;
     public String javaType;  // resulting type after preprocessing, can be a java type or enum (always a boxed type) or a class reference
     public boolean isUpperCaseOrLowerCaseSpecialType = false;  // true for uppercase or lowercase (has extra built-in validation function)
     // parameters which cascade down from global defaults to package defaults to class defaults (grammar: FieldDefaultsDefinition)
     public boolean effectiveSigned = true;
     public boolean effectiveTrim = false;
     public boolean effectiveTruncate = false;
     public boolean effectiveRounding = false;
     public boolean effectiveAutoScale = false;
     public boolean effectiveAllowCtrls = false;
     public boolean isPrimitive = false;             // true if this refers to an atomic data type which in Java is a primitive (can never be null)
     private boolean wasUpperCase = false;           // internal variable, required condition for a java type to be primitive
     public XRequired defaultRequired;               // default value for requiredness of the enclosing package or class
     public XRequired isRequired;                    // true if the variable is explicitly required / optional, or references a typedef in a packeg which has defaults
     public int enumMaxTokenLength = NO_ENUM;  // -2 for non-enums, -1 for numeric, >= 0 for regular enums
     public boolean allTokensAscii = true;
     public DataCategory category = DataCategory.MISC;
     
     static public void clear() {
         map.clear();
     }
     
     static private void mergeFieldSpecsWithDefaultsForObjects(DataTypeExtension r, DataType key) throws Exception {
         ElementaryDataType e = r.elementaryDataType;
         // find the parent which is the relevant package definition. These are 2 or 3 steps
         // (Package => Typedef => DataType key) or
         // (Package => ClassDefinition => FieldDefinition => DataType key)
         // Still, we keep this generic in order to support possible changes of the grammar
         PackageDefinition p = null;
         ClassDefinition cd = null;
         FieldDefaultsDefinition classdefs = null;
         
         for (EObject i = key.eContainer(); ; i = i.eContainer()) {
             if (i instanceof ClassDefinition) {
                 cd = (ClassDefinition)i;
             } else if (i instanceof PackageDefinition) {
                 p = (PackageDefinition)i;
                 break;
             }
         }
         // assert results: p must exist, cd only if inside class
         if (p == null)
             throw new Exception("no wrapping package found for " + e.getName());
         if (cd != null)
             classdefs = cd.getDefaults();
 
         // for every field, prefer field level setting (if exists here), then fall back to class defaults,
         // then to package defaults, and finally to hardcoded defaults
         r.defaultRequired = classdefs != null && classdefs.getRequired() != null
                           ? classdefs.getRequired().getX()
                           : p.getDefaults() != null && p.getDefaults().getRequired() != null
                               ? p.getDefaults().getRequired().getX()
                               : null;
     }
 
     static private void mergeFieldSpecsWithDefaults(DataTypeExtension r, DataType key) throws Exception {
         ElementaryDataType e = r.elementaryDataType;
         // find the parent which is the relevant package definition. These are 2 or 3 steps
         // (Package => Typedef => DataType key) or
         // (Package => ClassDefinition => FieldDefinition => DataType key)
         // Still, we keep this generic in order to support possible changes of the grammar
         PackageDefinition p = null;
         ClassDefinition cd = null;
         FieldDefaultsDefinition classdefs = null;
         
         for (EObject i = key.eContainer(); ; i = i.eContainer()) {
             if (i instanceof ClassDefinition) {
                 cd = (ClassDefinition)i;
             } else if (i instanceof PackageDefinition) {
                 p = (PackageDefinition)i;
                 break;
             }
         }
         // assert results: p must exist, cd only if inside class
         if (p == null)
             throw new Exception("no wrapping package found for " + e.getName());
         if (cd != null)
             classdefs = cd.getDefaults();
 
         // for every field, prefer field level setting (if exists here), then fall back to class defaults,
         // then to package defaults, and finally to hardcoded defaults
                             
         XUsePrimitives up = classdefs != null && classdefs.getUsePrimitives() != null
                                     ? classdefs.getUsePrimitives().getX()
                                     : p.getDefaults() != null && p.getDefaults().getUsePrimitives() != null
                                         ? p.getDefaults().getUsePrimitives().getX()
                                         : XUsePrimitives.USE_PRIMITIVES;
         if (up == XUsePrimitives.USE_PRIMITIVES && JAVA_PRIMITIVES.contains(e.getName()))
             r.isPrimitive = true;
         // TODO: else: map back types: char => Character, int => Integer
         
         XSignedness s = e.getSigned() != null
                         ? e.getSigned().getX()
                         : classdefs != null && classdefs.getSigned() != null
                                 ? classdefs.getSigned().getX()
                                 : p.getDefaults() != null && p.getDefaults().getSigned() != null
                                     ? p.getDefaults().getSigned().getX()
                                     : XSignedness.SIGNED;
         r.effectiveSigned = s == XSignedness.SIGNED;
         
         XTrimming trim = e.getTrimming() != null
                 ? e.getTrimming().getX()
                 : classdefs != null && classdefs.getTrim() != null
                         ? classdefs.getTrim().getX()
                         : p.getDefaults() != null && p.getDefaults().getTrim() != null
                             ? p.getDefaults().getTrim().getX()
                             : XTrimming.NOTRIM;
         r.effectiveTrim = trim == XTrimming.TRIM;
 
         XTruncating trunc = e.getTruncating() != null
                 ? e.getTruncating().getX()
                 : classdefs != null && classdefs.getTruncate() != null
                         ? classdefs.getTruncate().getX()
                         : p.getDefaults() != null && p.getDefaults().getTruncate() != null
                             ? p.getDefaults().getTruncate().getX()
                             : XTruncating.NOTRUNCATE;
         r.effectiveTruncate = trunc == XTruncating.TRUNCATE;
 
         XRounding rnd = e.getRounding() != null
                 ? e.getRounding().getX()
                 : classdefs != null && classdefs.getRound() != null
                         ? classdefs.getRound().getX()
                         : p.getDefaults() != null && p.getDefaults().getRound() != null
                             ? p.getDefaults().getRound().getX()
                             : XRounding.NOROUND;
         r.effectiveRounding = rnd == XRounding.ROUND;
 
         XAutoScale autoScale = e.getAutoScale() != null
                 ? e.getAutoScale().getX()
                 : classdefs != null && classdefs.getAutoScale() != null
                         ? classdefs.getAutoScale().getX()
                         : p.getDefaults() != null && p.getDefaults().getAutoScale() != null
                             ? p.getDefaults().getAutoScale().getX()
                             : XAutoScale.NOAUTOSCALE;
         r.effectiveAutoScale = autoScale == XAutoScale.AUTOSCALE;
         
         XSpecialCharsSetting spc = e.getAllowCtrls() != null
                 ? e.getAllowCtrls().getX()
                 : classdefs != null && classdefs.getAllowCtrls() != null
                         ? classdefs.getAllowCtrls().getX()
                         : p.getDefaults() != null && p.getDefaults().getAllowCtrls() != null
                             ? p.getDefaults().getAllowCtrls().getX()
                             : XSpecialCharsSetting.ALLOW_CONTROL_CHARS;
         r.effectiveAllowCtrls = spc == XSpecialCharsSetting.ALLOW_CONTROL_CHARS;
         
         r.defaultRequired = classdefs != null && classdefs.getRequired() != null
                              ? classdefs.getRequired().getX()
                              : p.getDefaults() != null && p.getDefaults().getRequired() != null
                                  ? p.getDefaults().getRequired().getX()
                                  : null;
     }
 
     static public DataTypeExtension get(DataType key) throws Exception {
         // retrieve the DataTypeExtension class for the given key (auto-create it if not yet existing)
         DataTypeExtension r = map.get(key);
         if (r != null) {
             if (r.currentlyVisited)
                 // can only occur for typedefs
                 throw new Exception("recursive typedefs around " + r.typedef.getName());
             return r;
         }
         // does not exist, create a new one!
         r = new DataTypeExtension();
         r.elementaryDataType = key.getElementaryDataType();
         r.typedef = key.getReferenceDataType();
         r.objectDataType = null;
         r.genericsRef = key.getObjectDataType();
         if (key.getObjectDataType() != null) {
         	r.category = DataCategory.OBJECT;
         	r.orSuperClass = key.isOrSuperClass();
         	// construct explicit expanded type information for the object reference (potentially including generics arguments) into javaType
         	r.javaType = XUtil.genericRef2String(key.getObjectDataType());
         	if (key.getObjectDataType().getClassRef() != null)
         		r.objectDataType = key.getObjectDataType().getClassRef();
         	// TODO: how to fill objectDataType when we have generics...
         	
             // merge the defaults specifications
             mergeFieldSpecsWithDefaultsForObjects(r, key);
         }
         
         if (r.elementaryDataType != null) {
             // immediate data: perform postprocessing. transfer defaults of embedding package to this instance
             ElementaryDataType e = r.elementaryDataType;
 
             // map extra (convenience) data types to their standard java names 
             if (Character.isUpperCase(e.getName().charAt(0))) {
                 r.wasUpperCase = true;
                 if (e.getName().equals("Int"))
                     e.setName("Integer");     // fix java naming inconsistency
                 if (e.getName().equals("Char"))
                     e.setName("Character");   // fix java naming inconsistency
             } else {
                 if (e.getName().equals("integer"))
                     e.setName("int");         // fix java naming inconsistency
                 if (e.getName().equals("character"))
                     e.setName("char");        // fix java naming inconsistency
             }
             r.javaType = dataTypeJava.get(e.getName().toLowerCase());
             r.category = dataCategory.get(e.getName().toLowerCase());
             // merge the defaults specifications
             mergeFieldSpecsWithDefaults(r, key);
             
             if (!r.wasUpperCase)
                 r.isRequired = XRequired.REQUIRED;              // field is set to required by specification
             
             // special handling for enums
             if (r.javaType == null)
                 throw new Exception("unmapped Java data type for " + e.getName());
             else if (r.javaType.equals("@")) {  // special case for enum types: replace java type by referenced class
                 r.javaType = e.getEnumType().getName();
                 // also count the max length if alphanumeric
                 EList<EnumAlphaValueDefinition> ead = e.getEnumType().getAvalues();
                 r.enumMaxTokenLength = ENUM_NUMERIC;
                 if (ead != null && !ead.isEmpty()) {
                     // compute the maximum length of all tokens, could be useful for derived grammars...
                     for (EnumAlphaValueDefinition enumX : ead) {
                         if (enumX.getToken() != null && enumX.getToken().length() > r.enumMaxTokenLength) {
                             r.enumMaxTokenLength = enumX.getToken().length();
                             if (!Util.isAsciiString(enumX.getToken()))
                                 r.allTokensAscii = false;
                         }
                     }
                 }
             }
             // compatibility...
             if (!Util.useJoda()) {
                 if (r.javaType.equals("LocalDate") || r.javaType.equals("LocalDate"))
                     r.javaType = "Calendar";
             }
             
             // special treatment for uppercase / lowercase shorthands
             if (r.javaType.equals("String"))
                 if (e.getName().equals("uppercase") || e.getName().equals("lowercase"))
                     r.isUpperCaseOrLowerCaseSpecialType = true;
             
             //System.out.println("setting elem data type: " + e.getName() + String.format(": wasUpper=%b, primitive=%b, length=%d, key=",
             //      r.wasUpperCase, r.isPrimitive, e.getLength()) + key);
         }
         // now resolve the typedef, if exists
         if (r.typedef != null) {
             r.currentlyVisited = true;
             // add to map
             map.put(key, r);
             DataTypeExtension resolvedReference = get(r.typedef.getDatatype());  // descend via DFS
             r.elementaryDataType = resolvedReference.elementaryDataType;
             r.objectDataType = resolvedReference.objectDataType;
             r.orSuperClass = resolvedReference.orSuperClass;
             r.genericsRef = resolvedReference.genericsRef;
             r.wasUpperCase = resolvedReference.wasUpperCase;
             r.isPrimitive = resolvedReference.isPrimitive;
             
             r.effectiveSigned = resolvedReference.effectiveSigned;
             r.effectiveRounding = resolvedReference.effectiveRounding;
             r.effectiveAutoScale = resolvedReference.effectiveAutoScale;
             r.effectiveTrim = resolvedReference.effectiveTrim;
             r.effectiveTruncate = resolvedReference.effectiveTruncate;
             r.effectiveAllowCtrls = resolvedReference.effectiveAllowCtrls;
             r.javaType = resolvedReference.javaType;
             r.isUpperCaseOrLowerCaseSpecialType = resolvedReference.isUpperCaseOrLowerCaseSpecialType;
             r.enumMaxTokenLength = resolvedReference.enumMaxTokenLength;
             r.category = resolvedReference.category;
             r.currentlyVisited = false;
             // computation of the "required" state
             r.defaultRequired = null;
             PackageDefinition pkg = (PackageDefinition)r.typedef.eContainer();
             if (pkg.getDefaults() != null && pkg.getDefaults().getRequired() != null)
                 r.defaultRequired = pkg.getDefaults().getRequired().getX();     // defaults for the class containing this typedef
             // we have an explicit assignment if the referenced package had a default
             r.isRequired = resolvedReference.isRequired;
             if (r.isRequired == null && resolvedReference.defaultRequired != null)
                 r.isRequired = resolvedReference.defaultRequired;  // either REQUIRED or OPTIONAL
         } else {
             // just simply store it (elementary data type or object reference)
             map.put(key, r);
         }
         return r;
     }
 }
