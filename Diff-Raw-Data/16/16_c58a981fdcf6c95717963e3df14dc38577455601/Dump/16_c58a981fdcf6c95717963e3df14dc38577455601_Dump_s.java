 package org.strategoxt.debug.core.util;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.sun.jdi.ArrayReference;
 import com.sun.jdi.ClassType;
 import com.sun.jdi.Field;
 import com.sun.jdi.InterfaceType;
 import com.sun.jdi.ObjectReference;
 import com.sun.jdi.ReferenceType;
 import com.sun.jdi.Value;
 
 public class Dump {
 
 	public static void dump(Value value)
 	{
         ObjectReference obj = (ObjectReference) value;
         ReferenceType refType = obj.referenceType();
         dump(obj, refType, refType);
 	}
 	
 	public static void dump(ObjectReference obj, ReferenceType refType, ReferenceType refTypeBase) 
     {
     	System.out.println(obj);
     	System.out.println(refType);
     	System.out.println(refTypeBase);
     	System.out.println("Fields");
     	for (Iterator<Field> it = refType.fields().iterator(); it.hasNext(); ) 
     	{
     		StringBuffer o = new StringBuffer();
     		Field field = (Field)it.next();
     		o.append("    ");
     		if (!refType.equals(refTypeBase)) {
     			o.append(refType.name());
     			o.append(".");
 			}
     		o.append(field.name());
     		o.append(": ");
     		o.append(obj.getValue(field));
     		System.out.println(o.toString());
 		}
     	System.out.println("refType");
     	if (refType instanceof ClassType) {
     		ClassType sup = ((ClassType)refType).superclass();
     		if (sup != null) {
     			dump(obj, sup, refTypeBase);
     		}
     	} else if (refType instanceof InterfaceType) {
 			List<InterfaceType> sups = ((InterfaceType)refType).superinterfaces();
 			for (Iterator<InterfaceType> it = sups.iterator(); it.hasNext(); ) {
 				dump(obj, (ReferenceType)it.next(), refTypeBase);
 			}
     	} else {
     		/* else refType is an instanceof ArrayType */
     		if (obj instanceof ArrayReference) {
     			for (Iterator<Value> it = ((ArrayReference)obj).getValues().iterator(); it.hasNext(); ) 
     			{
     				System.out.println(it.next().toString());
     				
 					if (it.hasNext()) {
 						System.out.println(", ");
 
 					}
     			}
     			System.out.println();
     		}
     	}
     }
 }
