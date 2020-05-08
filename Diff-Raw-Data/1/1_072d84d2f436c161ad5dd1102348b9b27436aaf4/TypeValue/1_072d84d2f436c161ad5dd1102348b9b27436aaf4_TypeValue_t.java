 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2004-2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 /*
  * Written by Jeff Dyer
  * Copyright (c) 1998-2003 Mountain View Compiler Company
  * All rights reserved.
  */
 
 package macromedia.asc.semantics;
 
 import macromedia.asc.util.*;
 
 import java.util.HashMap;
 
 /**
  * The interface for all types.
  *
  * @author Jeff Dyer
  */
 public final class TypeValue extends ObjectValue
 {
     public static void init()
     {
     }
 
     public static void clear()
     {
     }
 
     public static TypeValue newTypeValue(Context cx, Builder builder, QName name, int type_id)
     {
         String fullname = name.toString();
  
         TypeValue type = cx.userDefined(fullname);
         if (type == null)
         {
             type = new TypeValue(cx, builder, name, type_id);
             cx.setUserDefined(fullname, type);
         }
         else
         {
             type.clearInstance(cx, builder, null, fullname.intern(), false);
             type.type_id = type_id;
             // Don't clear the prototype, we can reuse the object value
 //			type.prototype.clearInstance() = null;
             type.name = name;
             type.type = null;
             type.baseclass = null;
 
             if( type.default_typeinfo != null )
             {
                 type.default_typeinfo.clearInstance();
             }
             if( type.explicit_nonnullable_typeinfo != null )
             {
                 type.explicit_nonnullable_typeinfo.clearInstance();
             }
             if( type.explicit_nullable_typeinfo != null )
             {
                 type.explicit_nullable_typeinfo.clearInstance();
             }
         }
         return type;
     }
 
     public ObjectValue prototype;
     public TypeValue baseclass;
     public boolean is_parameterized;
     public TypeValue indexed_type;
 
     public QName name;
 
     public TypeValue(Context cx, Builder builder, QName name, int type_id)
     {
         super(cx, builder, null);
         this.type_id = type_id;
         this.prototype = null;
         this.name = name;
         this.type = null;
         this.baseclass = null;
         super.name = this.name.toString();
     }
 
     public TypeInfo getType(Context cx)
     {
         return cx.typeType().getDefaultTypeInfo();
     }
 
     public int type_id;
 
     public int getTypeId()
     {
         return type_id;
     }
 
     public String toString()
     {
         return name!=null?name.toString():"";
     }
 
     public boolean includes(Context cx, TypeValue type)
     {
         if (this == cx.noType())
         {
             return true;
         }
 
         if (!isInterface())
         {
             while (type != null)
             {
                 if( this == type )
                 {
                     return true;
                 }
                 type = type.baseclass;
             }
         }
         else
        if (type != null) // type == null -> * type
         {
             InterfaceWalker interfaceWalker = new InterfaceWalker(type);
             while (interfaceWalker.hasNext())
             {
                 if (interfaceWalker.next().type.getTypeValue() == this)
                 {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public void build(Context cx, ObjectValue ob)
     {
     }
 
     public ObjectValue proto()
     {
            return null;
     }
 
     public String getPrintableName() {
         return name.name;
     }
 
     public boolean isNumeric(Context cx) {
         return ((this == cx.intType()) || (this == cx.uintType()) || (this == cx.doubleType()) ||
                 (this == cx.numberType()) || (cx.statics.es4_numerics && (this == cx.decimalType())));
     }
 
     public boolean isDynamic() { return false; }
 
     private TypeInfo default_typeinfo = null;
     private TypeInfo explicit_nullable_typeinfo = null;
     private TypeInfo explicit_nonnullable_typeinfo = null;
 
     public boolean is_nullable = true;
 
     public TypeInfo getDefaultTypeInfo()
     {
         if( default_typeinfo == null )
             default_typeinfo = new TypeInfo(this, this.is_nullable, true);
         return default_typeinfo;
     }
 
     public TypeInfo getTypeInfo(boolean nullable)
     {
         TypeInfo ti = nullable ? explicit_nullable_typeinfo : explicit_nonnullable_typeinfo;
 
         if( ti == null)
         {
             if( nullable )
             {
                 ti = explicit_nullable_typeinfo = new TypeInfo(this, nullable, false);
             }
             else
             {
                 ti = explicit_nonnullable_typeinfo = new TypeInfo(this, nullable, false);
             }
         }
         if( ti.getPrototype() == this.prototype )
         {
             // Create a dummy prototype object that the new TypeInfo can point too.
             ObjectValue nullable_proto = new ObjectValueWrapper(prototype);
             nullable_proto.builder = prototype.builder;
             nullable_proto.type = ti;
             ti.setPrototype(nullable_proto);
         }
         return ti;
     }
 
     public HashMap<String, Slot> types;
     public void addParameterizedTypeSlot(Context cx, String name, Slot s)
     {
         if( types == null )
             types = new HashMap<String, Slot>();
         types.put(name, s);
     }
     
     /**
      *  Propagate type data from an uinstantiated parameterized type (e.g., Vector)
      *  to a specialized instantiation (e.g., Vector&lt;int&gt;
      *  @param this - the specialized, instantiated type.
      *  @param uninstantiated_type - the "parent" uninstantiated type.
      */
     public void copyInstantiationData(TypeValue uninstantiated_type)
     {
     	this.builder.is_final = uninstantiated_type.builder.is_final;
     }
 }
