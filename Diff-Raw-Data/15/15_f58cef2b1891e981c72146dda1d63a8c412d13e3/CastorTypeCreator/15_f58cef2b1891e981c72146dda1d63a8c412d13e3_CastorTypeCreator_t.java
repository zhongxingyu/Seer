 package org.codehaus.xfire.castor;
 
 import java.beans.PropertyDescriptor;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.TypeCreator;
 import org.codehaus.xfire.aegis.type.TypeMapping;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.util.XMLClassDescriptorImpl;
 
 /**
  * TypeCreator factory class for creating CastorType classes.
  * 
  * @see org.codehaus.xfire.aegis.type.TypeCreator
  * @author Adam Kramer
  * @author Paul Saxman
  */
 public class CastorTypeCreator
     implements TypeCreator
 {
     private static final Log log = LogFactory.getLog(CastorTypeCreator.class);
 
     private TypeCreator nextCreator;
 
     private Mapping mapping = null;
 
     private TypeMapping typeMapping;
 
     /**
      * Constructor that instantiates the Castor mapping file to use for
      * de/marshalling.
      * 
      * @param typeCreator
      *            Default type creator used next in case castor not configured
      *            to map a certain type.
      * @param mappingFile
      *            The name of the Castor mapping file used for de/marshalling
      *            which presently needs to be in the classpath.
      */
     public CastorTypeCreator(TypeCreator typeCreator, String mappingFile)
     {
         this.nextCreator = typeCreator;
 
         try
         {
             if (mappingFile != null && mappingFile.length() > 0)
             {
                mapping = new Mapping();
                mapping.loadMapping(ClassLoaderUtils.getResource(mappingFile,
                                                                 CastorTypeCreator.class));
             }
         }
         catch (IOException e)
         {
             String error = "Error accessing mapping file '" + mappingFile + "'.";
             log.error(error, e);
             throw new XFireRuntimeException(error, e);
         }
         catch (MappingException e)
         {
             String error = "Error processing mapping file '" + mappingFile + "'.";
             log.error(error, e);
             throw new XFireRuntimeException(error, e);
         }
     }
 
     public Type createType(Method method, int index)
     {
         Class clazz = null;
         if (index > -1)
         {
             clazz = method.getParameterTypes()[index];
         }
         else
         {
             clazz = method.getReturnType();
         }
 
         if (isCastorType(clazz))
         {
             return createCastorType(clazz);
         }
         else
         {
             return nextCreator.createType(clazz);
         }
     }
 
     public Type createType(PropertyDescriptor propDesc)
     {
         if (isCastorType(propDesc.getPropertyType()))
         {
             return createCastorType(propDesc.getPropertyType());
         }
         else
         {
             return nextCreator.createType(propDesc.getPropertyType());
         }
     }
 
     public Type createType(Field field)
     {
 
         if (isCastorType(field.getType()))
         {
             return createCastorType(field.getType());
         }
         else
         {
             return nextCreator.createType(field.getType());
         }
     }
 
     public Type createType(Class clazz)
     {
         if (isCastorType(clazz))
         {
             return createCastorType(clazz);
         }
         else
         {
             return nextCreator.createType(clazz);
         }
     }
 
     public void setTypeMapping(TypeMapping typeMapping)
     {
         this.typeMapping = typeMapping;
 
         nextCreator.setTypeMapping(typeMapping);
     }
 
     private boolean isCastorType(Class clazz)
     {
         Class xdClass = null;
         boolean isCastor = false;
 
         // If there is a castor mapping, see if it is mapped there
         if (mapping != null)
         {
             try
             {
                XMLClassDescriptorImpl xd = (XMLClassDescriptorImpl) mapping
                        .getResolver(Mapping.XML).getDescriptor(clazz);
                 if (xd != null)
                     isCastor = true;
             }
             catch (MappingException e)
             {
                 String error = "Error getting resolver from mapping";
                 log.error(error, e);
                 throw new XFireRuntimeException(error, e);
             }
         }
         // If it has an accompanying auto-generated XML Descriptor
         // class, it is a castor type
         try
         {
             xdClass = ClassLoaderUtils.loadClass(clazz.getName() + "Descriptor", this.getClass());
            if (xdClass != null && (XMLClassDescriptorImpl.class.isAssignableFrom(xdClass)))
             {
                 isCastor = true;
             }
         }
         catch (Exception e)
         {
         }
         return isCastor;
     }
 
     private CastorType createCastorType(Class clazz)
     {
         CastorType type = new CastorType(clazz, mapping);
 
         return type;
     }
 }
