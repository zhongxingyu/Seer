 package fedora.server.utilities;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 /**
  *
  * <p>Title: TypeUtility.java</p>
  * <p>Description: A utility class for converting back and forth from the
  * internal Fedora type classes in fedora.server.storage.types and the
  * generated type classes produced by the wsdl2java emitter in
  * fedora.server.types.gen.</p>
  *
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public abstract class TypeUtility
 {
 
   /**
    * <p>Converts an array of fedora.server.storage.types.MethodDef into an
    * array of fedora.server.types.gen.MethodDef.</p>
    *
    * @param methodDefs An array of fedora.server.storage.types.MethodDef.
    * @return An array of fedora.server.types.gen.MethodDef.
    */
   public static fedora.server.types.gen.MethodDef[]
       convertMethodDefArrayToGenMethodDefArray(
       fedora.server.storage.types.MethodDef[] methodDefs)
   {
     if (methodDefs != null && methodDefs.length > 0)
     {
       fedora.server.types.gen.MethodDef[] genMethodDefs =
           new fedora.server.types.gen.MethodDef[methodDefs.length];
       for (int i=0; i<genMethodDefs.length; i++)
       {
         fedora.server.types.gen.MethodDef genMethodDef =
                  new fedora.server.types.gen.MethodDef();
         genMethodDef.setMethodLabel(methodDefs[i].methodLabel);
         genMethodDef.setMethodName(methodDefs[i].methodName);
         fedora.server.storage.types.MethodParmDef[] methodParmDefs =
             methodDefs[i].methodParms;
         fedora.server.types.gen.MethodParmDef[] genMethodParmDefs = null;
         if (methodParmDefs != null && methodParmDefs.length > 0)
         {
           genMethodParmDefs =
               new fedora.server.types.gen.MethodParmDef[methodParmDefs.length];
           for (int j=0; j<methodParmDefs.length; j++)
           {
             genMethodParmDefs[j] =
                      convertMethodParmDefToGenMethodParmDef(methodParmDefs[j]);
           }
         }
         genMethodDef.setMethodParms(genMethodParmDefs);
         genMethodDefs[i] = genMethodDef;
       }
       return genMethodDefs;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.storage.types.MethodDef into an
    * instance of fedora.server.types.gen.MethodDef.</p>
    *
    * @param methodDefs An instance of fedora.server.storage.types.MethodDef.
    * @return An instance of fedora.server.types.gen.MethodDef.
    */
   public static fedora.server.types.gen.MethodDef
       convertMethodDefToGenMethodDef(
       fedora.server.storage.types.MethodDef methodDef)
   {
     if (methodDef != null )
     {
       fedora.server.types.gen.MethodDef genMethodDefs =
           new fedora.server.types.gen.MethodDef();
       fedora.server.types.gen.MethodDef genMethodDef =
           new fedora.server.types.gen.MethodDef();
       genMethodDef.setMethodLabel(methodDef.methodLabel);
       genMethodDef.setMethodName(methodDef.methodName);
       fedora.server.storage.types.MethodParmDef[] methodParmDefs =
           methodDef.methodParms;
       fedora.server.types.gen.MethodParmDef[] genMethodParmDefs = null;
       genMethodParmDefs = convertMethodParmDefArrayToGenMethodParmDefArray(
           methodParmDefs);
       if (methodParmDefs != null && methodParmDefs.length > 0)
       {
         genMethodParmDefs =
             new fedora.server.types.gen.MethodParmDef[methodParmDefs.length];
         for (int j=0; j<methodParmDefs.length; j++)
         {
           genMethodParmDefs[j] =
                    convertMethodParmDefToGenMethodParmDef(methodParmDefs[j]);
         }
       }
       genMethodDef.setMethodParms(genMethodParmDefs);
       return genMethodDefs;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an array of fedora.server.types.gen.MethodDef into an
    * array of fedora.server.storage.types.MethodDef.</p>
    *
    * @param methodDefs An array of fedora.server.types.gen.MethodDef.
    * @return An array of fedora.server.storage.types.MethodDef.
    */
   public static fedora.server.storage.types.MethodDef[]
       convertGenMethodDefArrayToMethodDefArray(
       fedora.server.types.gen.MethodDef[] genMethodDefs)
   {
     if (genMethodDefs != null && genMethodDefs.length > 0)
     {
       fedora.server.storage.types.MethodDef[] methodDefs =
           new fedora.server.storage.types.MethodDef[genMethodDefs.length];
       for (int i=0; i<genMethodDefs.length; i++)
       {
         fedora.server.storage.types.MethodDef methodDef =
                  new fedora.server.storage.types.MethodDef();
         methodDef.methodLabel = genMethodDefs[i].getMethodLabel();
         methodDef.methodName = genMethodDefs[i].getMethodName();
         fedora.server.types.gen.MethodParmDef[] genMethodParmDefs =
             genMethodDefs[i].getMethodParms();
         fedora.server.storage.types.MethodParmDef[] methodParmDefs = null;
         if (genMethodParmDefs != null && genMethodParmDefs.length > 0)
         {
           methodParmDefs =
               new fedora.server.storage.types.MethodParmDef[
               genMethodParmDefs.length];
           for (int j=0; j<genMethodParmDefs.length; j++)
           {
             methodParmDefs[j] = convertGenMethodParmDefToMethodParmDef(
             genMethodParmDefs[j]);
           }
         }
         methodDef.methodParms = methodParmDefs;
         methodDefs[i] = methodDef;
       }
       return methodDefs;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.types.gen.MethodDef into an
    * instance of fedora.server.storage.types.MethodDef.</p>
    *
    * @param methodDefs An instance of fedora.server.types.gen.MethodDef.
    * @return An instance of fedora.server.storage.types.MethodDef.
    */
   public static fedora.server.storage.types.MethodDef
       convertGenMethodDefToMethodDef(
       fedora.server.types.gen.MethodDef genMethodDef)
   {
     if (genMethodDef != null)
     {
       fedora.server.storage.types.MethodDef methodDef =
           new fedora.server.storage.types.MethodDef();
       methodDef.methodLabel = genMethodDef.getMethodLabel();
       methodDef.methodName = genMethodDef.getMethodName();
       fedora.server.types.gen.MethodParmDef[] genMethodParmDefs =
           genMethodDef.getMethodParms();
       fedora.server.storage.types.MethodParmDef[] methodParmDefs = null;
       if (genMethodParmDefs != null && genMethodParmDefs.length > 0)
       {
         methodParmDefs = convertGenMethodParmDefArrayToMethodParmDefArray(
             genMethodParmDefs);
       }
       methodDef.methodParms = methodParmDefs;
       return methodDef;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an array of fedora.server.storage.types.MethodParmDef into
    * an array of fedora.server.types.gen.MethodParmDef.</p>
    *
    * @param methodParmDef An array of fedora.server.storage.types.MethodParmDef.
    * @return An array of fedora.server.types.gen.MethodParmDef.
    */
   public static fedora.server.types.gen.MethodParmDef[]
       convertMethodParmDefArrayToGenMethodParmDefArray(
       fedora.server.storage.types.MethodParmDef[] methodParmDefs)
   {
     if (methodParmDefs != null && methodParmDefs.length > 0)
     {
       fedora.server.types.gen.MethodParmDef[] genMethodParmDefs =
           new fedora.server.types.gen.MethodParmDef[methodParmDefs.length];
       for (int i=0; i<genMethodParmDefs.length; i++)
       {
         fedora.server.types.gen.MethodParmDef genMethodParmDef =
                  new fedora.server.types.gen.MethodParmDef();
         genMethodParmDef =
             convertMethodParmDefToGenMethodParmDef(methodParmDefs[i]);
         genMethodParmDefs[i] = genMethodParmDef;
       }
       return genMethodParmDefs;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.storage.types.MethodParmDef into
    * an instance of fedora.server.types.gen.MethodParmDef.</p>
    *
    * @param methodParmDef An instance of
    * fedora.server.storage.types.MethodParmDef.
    * @return An instance of fedora.server.types.gen.MethodParmDef.
    */
   public static fedora.server.types.gen.MethodParmDef
       convertMethodParmDefToGenMethodParmDef(
       fedora.server.storage.types.MethodParmDef methodParmDef)
   {
     if (methodParmDef != null)
     {
       fedora.server.types.gen.MethodParmDef genMethodParmDef =
           new fedora.server.types.gen.MethodParmDef();
       genMethodParmDef.setParmName(methodParmDef.parmName);
       genMethodParmDef.setParmLabel(methodParmDef.parmLabel);
       genMethodParmDef.setParmDefaultValue(methodParmDef.parmDefaultValue);
       genMethodParmDef.setParmRequired(methodParmDef.parmRequired);
       return genMethodParmDef;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an array of fedora.server.types.gen.MethodParmDef into an
    * array of fedora.server.storage.types.MethodParmDef.</p>
    *
    * @param methodDefs An array of fedora.server.types.gen.MethodParmDef.
    * @return An array of fedora.server.storage.types.MethodParmDef.
    */
   public static fedora.server.storage.types.MethodParmDef[]
       convertGenMethodParmDefArrayToMethodParmDefArray(
       fedora.server.types.gen.MethodParmDef[] genMethodParmDefs)
   {
     if (genMethodParmDefs != null && genMethodParmDefs.length > 0)
     {
       fedora.server.storage.types.MethodParmDef[] methodParmDefs =
           new fedora.server.storage.types.MethodParmDef[
           genMethodParmDefs.length];
       for (int i=0; i<genMethodParmDefs.length; i++)
       {
         fedora.server.storage.types.MethodParmDef methodParmDef =
                  new fedora.server.storage.types.MethodParmDef();
         methodParmDef =
             convertGenMethodParmDefToMethodParmDef(genMethodParmDefs[i]);
         methodParmDefs[i] = methodParmDef;
       }
       return methodParmDefs;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.storage.types.MethodParmDef into
    * an instance of fedora.server.types.gen.MethodParmDef.</p>
    *
    * @param methodParmDef An instance of
    * fedora.server.storage.types.MethodParmDef.
    * @return An instance of fedora.server.types.gen.MethodParmDef.
    */
   public static fedora.server.storage.types.MethodParmDef
       convertGenMethodParmDefToMethodParmDef(
       fedora.server.types.gen.MethodParmDef genMethodParmDef)
   {
     if (genMethodParmDef != null)
     {
       fedora.server.storage.types.MethodParmDef methodParmDef =
           new fedora.server.storage.types.MethodParmDef();
       methodParmDef.parmName = genMethodParmDef.getParmName();
       methodParmDef.parmLabel = genMethodParmDef.getParmLabel();
       methodParmDef.parmDefaultValue = genMethodParmDef.getParmDefaultValue();
       methodParmDef.parmRequired = genMethodParmDef.isParmRequired();
       return methodParmDef;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.storage.types.MIMETypedStream into
    * an instance of fedora.server.types.gen.MIMETypedStream.</p>
    *
    * @param mimeTypedStream An instance of
    * fedora.server.storage.types.MIMETypedStream.
    * @return An instance of fedora.server.types.gen.MIMETypedStream.
    */
   public static fedora.server.types.gen.MIMETypedStream
       convertMIMETypedStreamToGenMIMETypedStream(
       fedora.server.storage.types.MIMETypedStream mimeTypedStream)
   {
     if (mimeTypedStream != null)
     {
       fedora.server.types.gen.MIMETypedStream genMIMETypedStream =
           new fedora.server.types.gen.MIMETypedStream();
       genMIMETypedStream.setMIMEType(mimeTypedStream.MIMEType);
       genMIMETypedStream.setStream(mimeTypedStream.stream);
       return genMIMETypedStream;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.types.gen.MIMETypedStream into
    * an instance of fedora.server.storage.types.MIMETypedStream.</p>
    *
    * @param mimeTypedStream An instance of
    * fedora.server.types.gen.MIMETypedStream.
    * @return an instance of fedora.server.storage.types.MIMETypedStream.
    */
   public static fedora.server.storage.types.MIMETypedStream
       convertGenMIMETypedStreamToMIMETypedStream(
       fedora.server.types.gen.MIMETypedStream genMIMETypedStream)
   {
     if (genMIMETypedStream != null)
     {
       fedora.server.storage.types.MIMETypedStream mimeTypedStream =
           new fedora.server.storage.types.MIMETypedStream(
           genMIMETypedStream.getMIMEType(), genMIMETypedStream.getStream());
       return mimeTypedStream;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an array of fedora.server.types.gen.ObjectMethodsDef into an
    * array of fedora.server.storage.types.ObjectMethodsDef.</p>
    *
    * @param genObjectMethodDefs An array of
    * fedora.server.types.gen.ObjectMethodsDef.
    * @return An array of fedora.server.storage.types.ObjectMethodsDef.
    */
   public static fedora.server.storage.types.ObjectMethodsDef[]
       convertGenObjectMethodsDefArrayToObjectMethodsDefArray(
       fedora.server.types.gen.ObjectMethodsDef[] genObjectMethodDefs)
   {
     if (genObjectMethodDefs != null && genObjectMethodDefs.length > 0)
     {
       fedora.server.storage.types.ObjectMethodsDef[] objectMethodDefs =
           new fedora.server.storage.types.ObjectMethodsDef[
           genObjectMethodDefs.length];
       for (int i=0; i<genObjectMethodDefs.length; i++)
       {
         fedora.server.storage.types.ObjectMethodsDef objectMethodDef =
                  new fedora.server.storage.types.ObjectMethodsDef();
         objectMethodDef.PID = genObjectMethodDefs[i].getPID();
         objectMethodDef.bDefPID = genObjectMethodDefs[i].getBDefPID();
         objectMethodDef.methodName = genObjectMethodDefs[i].getMethodName();
         objectMethodDefs[i] = objectMethodDef;
       }
       return objectMethodDefs;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.types.gen.ObjectMethodsDef into
    * an instance of fedora.server.storage.types.ObjectMethodsDef.</p>
    *
    * @param genObjectMethodDefs An instance of
    * fedora.server.types.gen.ObjectMethodsDef.
    * @return An instance of fedora.server.storage.types.ObjectMethodsDef.
    */
   public static fedora.server.storage.types.ObjectMethodsDef
       convertGenObjectMethodsDefToObjectMethodsDef(
       fedora.server.types.gen.ObjectMethodsDef genObjectMethodDef)
   {
     if (genObjectMethodDef != null)
     {
       fedora.server.storage.types.ObjectMethodsDef objectMethodDefs =
           new fedora.server.storage.types.ObjectMethodsDef();
       fedora.server.storage.types.ObjectMethodsDef objectMethodDef =
           new fedora.server.storage.types.ObjectMethodsDef();
       objectMethodDef.PID = genObjectMethodDef.getPID();
       objectMethodDef.bDefPID = genObjectMethodDef.getBDefPID();
       objectMethodDef.methodName = genObjectMethodDef.getMethodName();
       return objectMethodDef;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an array of fedora.server.storage.types.ObjectMethodsDef into
    * an array of fedora.server.types.gen.ObjectMethodsDef.</p>
    *
    * @param genObjectMethodDefs An array of
    * fedora.server.storage.types.ObjectMethodsDef.
    * @return An array of fedora.server.types.gen.ObjectMethodsDef.
    */
   public static fedora.server.types.gen.ObjectMethodsDef[]
       convertObjectMethodsDefArrayToGenObjectMethodsDefArray(
       fedora.server.storage.types.ObjectMethodsDef[] objectMethodDefs)
   {
     if (objectMethodDefs != null && objectMethodDefs.length > 0)
     {
       fedora.server.types.gen.ObjectMethodsDef[] genObjectMethodDefs =
           new fedora.server.types.gen.ObjectMethodsDef[objectMethodDefs.length];
       for (int i=0; i<objectMethodDefs.length; i++)
       {
         fedora.server.types.gen.ObjectMethodsDef genObjectMethodDef =
                  new fedora.server.types.gen.ObjectMethodsDef();
         genObjectMethodDef.setPID(objectMethodDefs[i].PID);
         genObjectMethodDef.setBDefPID(objectMethodDefs[i].bDefPID);
         genObjectMethodDef.setMethodName(objectMethodDefs[i].methodName);
         genObjectMethodDefs[i] = genObjectMethodDef;
       }
       return genObjectMethodDefs;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.storage.types.ObjectMethodsDef
    * into an instance of fedora.server.types.gen.ObjectMethodsDef.</p>
    *
    * @param genObjectMethodDefs An instance of
    * fedora.server.storage.types.ObjectMethodsDef.
    * @return An instance of fedora.server.types.gen.ObjectMethodsDef.
    */
   public static fedora.server.types.gen.ObjectMethodsDef
       convertObjectMethodsDefToGenObjectMethodsDef(
       fedora.server.storage.types.ObjectMethodsDef objectMethodDef)
   {
     if (objectMethodDef != null)
     {
       fedora.server.types.gen.ObjectMethodsDef genObjectMethodDef =
           new fedora.server.types.gen.ObjectMethodsDef();
       genObjectMethodDef.setPID(objectMethodDef.PID);
       genObjectMethodDef.setBDefPID(objectMethodDef.bDefPID);
       genObjectMethodDef.setMethodName(objectMethodDef.methodName);
       return genObjectMethodDef;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an array of fedora.server.types.gen.Property into an
    * array of fedora.server.storage.types.Property.</p>
    *
    * @param properties An array of fedora.server.types.gen.Property.
    * @return An array of fedora.server.storage.types.Property.
    */
   public static fedora.server.storage.types.Property[]
       convertGenPropertyArrayToPropertyArray(
       fedora.server.types.gen.Property[] genProperties)
   {
     if (genProperties != null && genProperties.length > 0)
     {
       fedora.server.storage.types.Property[] properties =
           new fedora.server.storage.types.Property[genProperties.length];
       for (int i=0; i<genProperties.length; i++)
       {
         fedora.server.storage.types.Property property =
                  new fedora.server.storage.types.Property();
         property =
             convertGenPropertyToProperty(genProperties[i]);
         properties[i] = property;
       }
       return properties;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.types.gen.Property into
    * an instance of fedora.server.storage.types.Property.</p>
    *
    * @param methodParmDef An instance of
    * fedora.server.types.gen.Property.
    * @return An instance of fedora.server.storage.types.Property.
    */
   public static fedora.server.storage.types.Property
       convertGenPropertyToProperty(
       fedora.server.types.gen.Property genProperty)
   {
    fedora.server.storage.types.Property property =
          new fedora.server.storage.types.Property();
     if (genProperty != null)
     {
       property.name = genProperty.getName();
       property.value = genProperty.getValue();
     }
    return property;
   }
 
   /**
    * <p>Converts an array of fedora.server.storage.types.Property into an
    * array of fedora.server.types.gen.Property.</p>
    *
    * @param genProperties An array of fedora.server.storage.typesProperty.
    * @return An array of fedora.server.types.gen.Property.
    */
   public static fedora.server.types.gen.Property[]
       convertPropertyArrayToGenPropertyArray(
       fedora.server.storage.types.Property[] properties)
   {
     if (properties != null && properties.length > 0)
     {
       fedora.server.types.gen.Property[] genProperties =
           new fedora.server.types.gen.Property[properties.length];
       for (int i=0; i<properties.length; i++)
       {
         fedora.server.types.gen.Property genProperty =
                  new fedora.server.types.gen.Property();
         genProperty =
             convertPropertyToGenProperty(properties[i]);
         genProperties[i] = genProperty;
       }
       return genProperties;
 
     } else
     {
       return null;
     }
   }
 
   /**
    * <p>Converts an instance of fedora.server.storage.types.Property into
    * an instance of fedora.server.types.gen.Property.</p>
    *
    * @param property An instance of
    * fedora.server.storage.types.Property.
    * @return An instance of fedora.server.types.gen.Property.
    */
   public static fedora.server.types.gen.Property
       convertPropertyToGenProperty(
       fedora.server.storage.types.Property property)
   {
    fedora.server.types.gen.Property genProperty =
        new fedora.server.types.gen.Property();
     if (property != null)
     {
       genProperty.setName(property.name);
       genProperty.setValue(property.value);
     }
    return genProperty;
   }
 
   public static void main(String[] args)
   {
     fedora.server.storage.types.MethodParmDef methodParmDef1 =
         new fedora.server.storage.types.MethodParmDef();
     methodParmDef1.parmName = "parm_name1";
     methodParmDef1.parmLabel = "parm_label1";
     methodParmDef1.parmDefaultValue = "parm_default_value1";
     methodParmDef1.parmRequired = true;
     fedora.server.storage.types.MethodParmDef methodParmDef2 =
         new fedora.server.storage.types.MethodParmDef();
     methodParmDef2.parmName = "parm_name2";
     methodParmDef2.parmLabel = "parm_label2";
     methodParmDef2.parmDefaultValue = "parm_default_value2";
     methodParmDef2.parmRequired = false;
     fedora.server.storage.types.MethodDef methodDef1 =
         new fedora.server.storage.types.MethodDef();
     methodDef1.methodName = "method_name1";
     methodDef1.methodLabel = "method_label1";
     fedora.server.storage.types.MethodParmDef[] methodParmDefs =
         new fedora.server.storage.types.MethodParmDef[2];
     methodParmDefs[0] = methodParmDef1;
     methodParmDefs[1] = methodParmDef2;
     methodDef1.methodParms = methodParmDefs;
     fedora.server.storage.types.MethodDef methodDef2 =
         new fedora.server.storage.types.MethodDef();
     methodDef2.methodName = "method_name2";
     methodDef2.methodLabel = "method_label2";
     methodDef2.methodParms = null;
 
     fedora.server.storage.types.MethodDef[] methodDef =
         new fedora.server.storage.types.MethodDef[2];
     methodDef[0] = methodDef1;
     methodDef[1] = methodDef2;
     fedora.server.storage.types.Property[] properties =
         new fedora.server.storage.types.Property[2];
     fedora.server.storage.types.Property prop1 =
         new fedora.server.storage.types.Property();
     fedora.server.storage.types.Property prop2 =
         new fedora.server.storage.types.Property();
     prop1.name = "prop1_name";
     prop1.value = "prop1_value";
     prop2.name = "prop2_name";
     prop2.value = "prop2_value";
     properties[0] = prop1;
     properties[1] = prop2;
 
     fedora.server.storage.types.ObjectMethodsDef[] objectMethods =
         new fedora.server.storage.types.ObjectMethodsDef[2];
     fedora.server.storage.types.ObjectMethodsDef objectMethod1 =
         new fedora.server.storage.types.ObjectMethodsDef();
     fedora.server.storage.types.ObjectMethodsDef objectMethod2 =
         new fedora.server.storage.types.ObjectMethodsDef();
     objectMethod1.PID = "PID1";
     objectMethod1.bDefPID = "bDefPID1";
     objectMethod1.methodName = "method1";
     objectMethod2.PID = "PID2";
     objectMethod2.bDefPID = "bDefPID2";
     objectMethod2.methodName = "method2";
     objectMethods[0] = objectMethod1;
     objectMethods[1] = objectMethod2;
 
     System.out.println("\n----- Started with these values:");
     for (int i=0; i<methodDef.length; i++)
     {
       System.out.println("name: "+methodDef[i].methodName+
       "\nlabel: "+methodDef[i].methodLabel+
       "\nparms:\n");
       fedora.server.storage.types.MethodParmDef[] methodParmDef = null;
       methodParmDef = methodDef[i].methodParms;
       if (methodParmDef != null && methodParmDef.length > 0)
       {
         methodParmDef = methodDef[i].methodParms;
         for (int j=0; j<methodParmDef.length; j++)
         {
           System.out.println("parmname: "+methodParmDef[j].parmName+
           "\nparmLabel: "+methodParmDef[j].parmLabel+
           "\nparmDefault: "+methodParmDef[j].parmDefaultValue+
           "\nparmrequired: "+methodParmDef[j].parmRequired);
         }
       }
     }
 
     System.out.println("\nObjectMethod  values:");
     for (int i=0; i<objectMethods.length; i++)
     {
       System.out.println("ObjectMethod[" + i + "] = " + "\nPID = "
       + objectMethods[i].PID+"\nbDefPID = " + objectMethods[i].bDefPID
       + "\nmethod = " + objectMethods[i].methodName);
     }
 
     System.out.println("\nProperty  values:");
     for (int i=0; i<properties.length; i++)
     {
       System.out.println("Prop[" + i + "] = " + "\nname = "
       + properties[i].name+"\nvalue = " + properties[i].value);
     }
 
     System.out.println("\n----- Converting MethodDefArray to "
                        + "GenMethodDefArray");
     fedora.server.types.gen.MethodDef[] genMethodDef =
         TypeUtility.convertMethodDefArrayToGenMethodDefArray(methodDef);
     for (int i=0; i<genMethodDef.length; i++)
     {
       System.out.println("name: "+genMethodDef[i].getMethodName()+
       "\nlabel: "+genMethodDef[i].getMethodLabel()+
       "\nparms:\n");
       fedora.server.types.gen.MethodParmDef[] methodParmDef = null;
       methodParmDef = genMethodDef[i].getMethodParms();
       if (methodParmDef != null && methodParmDef.length > 0)
       {
         methodParmDef = genMethodDef[i].getMethodParms();
         for (int j=0; j<methodParmDef.length; j++)
         {
           System.out.println("parmname: "+methodParmDef[j].getParmName()+
           "\nparmLabel: "+methodParmDef[j].getParmLabel()+
           "\nparmDefault: "+methodParmDef[j].getParmDefaultValue()+
           "\nparmrequired: "+methodParmDef[j].isParmRequired());
         }
       }
     }
     methodDef = null;
 
     System.out.println("\n----- Converting GenMethodDefArray to "
                        + "MethodDefArray");
     methodDef =
         TypeUtility.convertGenMethodDefArrayToMethodDefArray(genMethodDef);
     for (int i=0; i<methodDef.length; i++)
     {
       System.out.println("name: "+methodDef[i].methodName+
       "\nlabel: "+methodDef[i].methodLabel+
       "\nparms:\n");
       fedora.server.storage.types.MethodParmDef[] methodParmDef = null;
       methodParmDef = methodDef[i].methodParms;
       if (methodParmDef != null && methodParmDef.length > 0)
       {
         methodParmDef = methodDef[i].methodParms;
         for (int j=0; j<methodParmDef.length; j++)
         {
           System.out.println("parmname: "+methodParmDef[j].parmName+
           "\nparmLabel: "+methodParmDef[j].parmLabel+
           "\nparmDefault: "+methodParmDef[j].parmDefaultValue+
           "\nparmrequired: "+methodParmDef[j].parmRequired);
         }
       }
     }
 
     System.out.println("\n----- Starting with MIMETypedStream of:");
     String text = "this is some text for the bytestream";
     ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
     java.io.PrintWriter pw = new java.io.PrintWriter(baos);
     pw.println(text);
     pw.close();
     byte[] stream = baos.toByteArray();
     fedora.server.storage.types.MIMETypedStream mimeTypedStream =
         new fedora.server.storage.types.MIMETypedStream("text/plain", stream);
     System.out.println("MIMEType: " + mimeTypedStream.MIMEType);
     ByteArrayInputStream bais =
         new ByteArrayInputStream(mimeTypedStream.stream);
     int byteStream = 0;
     while ((byteStream = bais.read()) >= 0)
     {
       System.out.write(byteStream);
     }
     System.out.println("\n----- Converting GenMIMETypedStream to "+
                        " MIMETypedStream");
     fedora.server.types.gen.MIMETypedStream genMIMETypedStream =
         TypeUtility.convertMIMETypedStreamToGenMIMETypedStream(
         mimeTypedStream);
     System.out.println("MIMEType: " + genMIMETypedStream.getMIMEType());
     bais = new ByteArrayInputStream(genMIMETypedStream.getStream());
     byteStream = 0;
     while ((byteStream = bais.read()) >= 0)
     {
       System.out.write(byteStream);
     }
     System.out.println("\n----- Converting MIMETypedStream to "+
                        " GenMIMETypedStream");
     mimeTypedStream =
         TypeUtility.convertGenMIMETypedStreamToMIMETypedStream(
         genMIMETypedStream);
     System.out.println("MIMEType: " + mimeTypedStream.MIMEType);
     bais = new ByteArrayInputStream(mimeTypedStream.stream);
     byteStream = 0;
     while ((byteStream = bais.read()) >= 0)
     {
       System.out.write(byteStream);
     }
 
     System.out.println("\n----- Converting ObjectMethodsDefArray to "
                        + "GenObjectMethodsDefArray");
     fedora.server.types.gen.ObjectMethodsDef[] genObjectMethods = null;
     genObjectMethods =
         TypeUtility.convertObjectMethodsDefArrayToGenObjectMethodsDefArray(
         objectMethods);
     for (int i=0; i<genObjectMethods.length; i++)
     {
       System.out.println("GenProp[" + i + "]: " + "\nPID = "
       + genObjectMethods[i].getPID()+"\nbDefPID = "
       + genObjectMethods[i].getBDefPID() + "\nmethod = "
       + genObjectMethods[i].getMethodName());
     }
     System.out.println("\n----- Converting GenObjectMethodsDefArray to "
                        + "ObjectMethodsDefArray");
     objectMethods =
         TypeUtility.convertGenObjectMethodsDefArrayToObjectMethodsDefArray(
         genObjectMethods);
     for (int i=0; i<objectMethods.length; i++)
     {
       System.out.println("ObjectMethods[" + i + "]: " + "\nPID = "
       + objectMethods[i].PID+"\nbDefPID = " + objectMethods[i].bDefPID
       + "\nmethod = " + objectMethods[i].methodName);
     }
 
     System.out.println("\n----- Converting PropertyArray to "
                        + "GenPropertyArray");
     fedora.server.types.gen.Property[] genProperties =
         TypeUtility.convertPropertyArrayToGenPropertyArray(properties);
     for (int i=0; i<genProperties.length; i++)
     {
       System.out.println("GenProp[" + i + "]: " + "\nname = "
       + genProperties[i].getName()+"\nvalue = " + genProperties[i].getValue());
     }
     System.out.println("\n----- Converting GenPropertyArray to "
                        + "PropertyArray");
     properties = TypeUtility.convertGenPropertyArrayToPropertyArray(
         genProperties);
     for (int i=0; i<properties.length; i++)
     {
       System.out.println("Prop[" + i + "]: " + "\nname = "
       + properties[i].name+"\nvalue = " + properties[i].value);
     }
 
   }
 
 }
