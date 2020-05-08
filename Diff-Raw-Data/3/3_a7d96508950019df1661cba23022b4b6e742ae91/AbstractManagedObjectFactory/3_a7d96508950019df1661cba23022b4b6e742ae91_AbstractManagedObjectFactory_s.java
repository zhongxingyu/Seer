 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.managed.plugins.factory;
 
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Constructor;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.config.plugins.property.PropertyConfiguration;
 import org.jboss.config.spi.Configuration;
 import org.jboss.logging.Logger;
 import org.jboss.managed.api.Fields;
 import org.jboss.managed.api.ManagedObject;
 import org.jboss.managed.api.ManagedOperation;
 import org.jboss.managed.api.ManagedParameter;
 import org.jboss.managed.api.ManagedProperty;
 import org.jboss.managed.api.ManagedOperation.Impact;
 import org.jboss.managed.api.annotation.ManagementConstants;
 import org.jboss.managed.api.annotation.ManagementObject;
 import org.jboss.managed.api.annotation.ManagementOperation;
 import org.jboss.managed.api.annotation.ManagementProperties;
 import org.jboss.managed.api.annotation.ManagementProperty;
 import org.jboss.managed.api.factory.ManagedObjectFactory;
 import org.jboss.managed.plugins.DefaultFieldsImpl;
 import org.jboss.managed.plugins.ManagedObjectImpl;
 import org.jboss.managed.plugins.ManagedOperationImpl;
 import org.jboss.managed.plugins.ManagedPropertyImpl;
 import org.jboss.managed.spi.factory.ManagedObjectBuilder;
 import org.jboss.managed.spi.factory.ManagedObjectPopulator;
 import org.jboss.managed.spi.factory.ManagedPropertyConstraintsPopulator;
 import org.jboss.managed.spi.factory.ManagedPropertyConstraintsPopulatorFactory;
 import org.jboss.metatype.api.types.ArrayMetaType;
 import org.jboss.metatype.api.types.GenericMetaType;
 import org.jboss.metatype.api.types.MetaType;
 import org.jboss.metatype.api.types.MetaTypeFactory;
 import org.jboss.metatype.api.values.ArrayValueSupport;
 import org.jboss.metatype.api.values.GenericValueSupport;
 import org.jboss.metatype.api.values.MetaValue;
 import org.jboss.metatype.api.values.MetaValueFactory;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.MethodInfo;
 import org.jboss.reflect.spi.ParameterInfo;
 import org.jboss.reflect.spi.TypeInfo;
 
 /**
  * AbstractManagedObjectFactory.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author Scott.Stark@jboss.org
  * @version $Revision: 1.1 $
  */
 public class AbstractManagedObjectFactory extends ManagedObjectFactory
    implements ManagedObjectBuilder, ManagedObjectPopulator<Serializable>
 {
    private static final Logger log = Logger.getLogger(AbstractManagedObjectFactory.class);
 
    /** The configuration */
    private static final Configuration configuration;
 
    /** The managed object meta type */
    public static final GenericMetaType MANAGED_OBJECT_META_TYPE = new GenericMetaType(ManagedObject.class.getName(), ManagedObject.class.getName());
    
    /** The meta type factory */
    private MetaTypeFactory metaTypeFactory = MetaTypeFactory.getInstance(); 
 
    /** The meta value factory */
    private MetaValueFactory metaValueFactory = MetaValueFactory.getInstance(); 
    
    /** The managed object builders */
    private Map<Class, WeakReference<ManagedObjectBuilder>> builders = new WeakHashMap<Class, WeakReference<ManagedObjectBuilder>>();
 
    static
    {
       configuration = AccessController.doPrivileged(new PrivilegedAction<Configuration>()
       {
          public Configuration run()
          {
             return new PropertyConfiguration();
          }
       });
    }
    
    @Override
    public <T extends Serializable> ManagedObject createManagedObject(Class<T> clazz)
    {
       if (clazz == null)
          throw new IllegalArgumentException("Null class");
 
       ManagedObject result = createSkeletonManagedObject(clazz);
       ManagedObjectPopulator<Serializable> populator = getPopulator(clazz);
       populator.createObject(result, clazz);
       
       return result;
    }
 
    @Override
    public ManagedObject initManagedObject(Serializable object)
    {
       if (object == null)
          throw new IllegalArgumentException("Null object");
 
       Class<? extends Serializable> clazz = object.getClass();
       ManagedObject result = createSkeletonManagedObject(clazz);
       ManagedObjectPopulator<Serializable> populator = getPopulator(clazz);
       populator.populateManagedObject(result, object);
 
       return result;
    }
 
    @Override
    public void setBuilder(Class<?> clazz, ManagedObjectBuilder builder)
    {
       synchronized (builders)
       {
          if (builder == null)
             builders.remove(clazz);
          builders.put(clazz, new WeakReference<ManagedObjectBuilder>(builder));
       }
    }
    
    /**
     * Create a skeleton managed object
     * 
     * @param <T> the type
     * @param clazz the clazz
     * @return the skeleton managed object
     */
    protected <T extends Serializable> ManagedObject createSkeletonManagedObject(Class<T> clazz)
    {
       if (clazz == null)
          throw new IllegalArgumentException("Null class");
 
       ManagedObjectBuilder builder = getBuilder(clazz);
       return builder.buildManagedObject(clazz);
    }
    
    public ManagedObject buildManagedObject(Class<? extends Serializable> clazz)
    {
       BeanInfo beanInfo = configuration.getBeanInfo(clazz);
       ClassInfo classInfo = beanInfo.getClassInfo();
 
       ManagementObject managementObject = classInfo.getUnderlyingAnnotation(ManagementObject.class);
       if( managementObject == null )
       {
          // Skip the ManagedObject creation
          return null;
       }
 
       String name = ManagementConstants.GENERATED;
       if (managementObject != null)
          name = managementObject.name();
       if (ManagementConstants.GENERATED.equals(name))
          name = classInfo.getName();
       
       ManagementProperties propertyType = ManagementProperties.ALL;
       if (managementObject != null)
          propertyType = managementObject.properties();
       
       Set<ManagedProperty> properties = new HashSet<ManagedProperty>();
       
       Set<PropertyInfo> propertyInfos = beanInfo.getProperties();
       if (propertyInfos != null && propertyInfos.isEmpty() == false)
       {
          for (PropertyInfo propertyInfo : propertyInfos)
          {
             // Ignore the "class" property
             if ("class".equals(propertyInfo.getName()))
                continue;
 
             ManagementProperty managementProperty = propertyInfo.getUnderlyingAnnotation(ManagementProperty.class);
 
             // Check for a simple property
             boolean includeProperty = (propertyType == ManagementProperties.ALL);
             if (managementProperty != null)
                includeProperty = (managementProperty.ignored() == false);
 
             if (includeProperty)
             {
                Fields fields = null;
                if (managementProperty != null)
                {
                   Class<? extends Fields> factory = managementProperty.fieldsFactory();
                   if (factory != ManagementProperty.NULL_FIELDS_FACTORY.class)
                   {
                      try
                      {
                         fields = factory.newInstance();
                      }
                      catch (Exception e)
                      {
                         log.debug("Failed to created Fields", e);
                      }
                   }
                }
                if (fields == null)
                   fields = new DefaultFieldsImpl();
 
                if( propertyInfo instanceof Serializable )
                {
                   Serializable info = Serializable.class.cast(propertyInfo);
                   fields.setField(Fields.PROPERTY_INFO, info);
                }
 
                String propertyName = propertyInfo.getName();
                if (managementProperty != null)
                   propertyName = managementProperty.name();
                if( propertyName.length() == 0 )
                   propertyName = propertyInfo.getName();
                fields.setField(Fields.NAME, propertyName);
 
                // This should probably always the the propertyInfo name?
                String mappedName = propertyInfo.getName();
                if (managementProperty != null)
                   managementProperty.mappedName();
                if( mappedName.length() == 0 )
                   mappedName = propertyInfo.getName();
                fields.setField(Fields.MAPPED_NAME, mappedName);
 
                String description = ManagementConstants.GENERATED;
                if (managementProperty != null)
                   description = managementProperty.description();
                if (description.equals(ManagementConstants.GENERATED))
                   description = propertyName;
                fields.setField(Fields.DESCRIPTION, description);
 
                boolean mandatory = false;
                if (managementProperty != null)
                   mandatory = managementProperty.mandatory();
                if (mandatory)
                   fields.setField(Fields.MANDATORY, Boolean.TRUE);
                
                boolean managed = false;
                if (managementProperty != null)
                   managed = managementProperty.managed();
                
                MetaType metaType;
                if (managed)
                {
                   TypeInfo typeInfo = propertyInfo.getType();
                   if( typeInfo.isArray() || typeInfo.isCollection() )
                      metaType = new ArrayMetaType(1, MANAGED_OBJECT_META_TYPE);
                   else
                      metaType = MANAGED_OBJECT_META_TYPE;
                }
                else
                {
                   metaType = metaTypeFactory.resolve(propertyInfo.getType());
                }
                fields.setField(Fields.META_TYPE, metaType);
 
                // Delegate others (legal values, min/max etc.) to the constraints factory
                try
                {
                   Class<? extends ManagedPropertyConstraintsPopulatorFactory> factoryClass = managementProperty.constraintsFactory();
                   ManagedPropertyConstraintsPopulatorFactory factory = factoryClass.newInstance();
                   ManagedPropertyConstraintsPopulator populator = factory.newInstance();
                   if (populator != null)
                      populator.populateManagedProperty(clazz, propertyInfo, fields);
                }
                catch(Exception e)
                {
                   
                }
 
                
                ManagedProperty property = null;
                if (managementProperty != null)
                {
                   Class<? extends ManagedProperty> factory = managementProperty.propertyFactory();
                   if (factory != ManagementProperty.NULL_PROPERTY_FACTORY.class)
                   {
                      property = getManagedProperty(factory, fields);
                   }
                }
                if (property == null)
                   property = new ManagedPropertyImpl(fields);
                properties.add(property);
             }
          }
       }
 
       /* TODO: Operations. In general the bean metadata does not contain
        operation information.
       */
       Set<ManagedOperation> operations = new HashSet<ManagedOperation>();
       
       Set<MethodInfo> methodInfos = beanInfo.getMethods();
       if (methodInfos != null && methodInfos.isEmpty() == false)
       {
          for (MethodInfo methodInfo : methodInfos)
          {
             ManagementOperation managementOp = methodInfo.getUnderlyingAnnotation(ManagementOperation.class);
             if (managementOp == null)
                continue;
 
             ManagedOperation op = getManagedOperation(methodInfo, managementOp);
             operations.add(op);
          }
       }
 
       ManagedObjectImpl result = new ManagedObjectImpl(name, properties);
       for (ManagedProperty property : properties)
       {
          ManagedPropertyImpl managedPropertyImpl = (ManagedPropertyImpl) property;
          managedPropertyImpl.setManagedObject(result);
       }
       return result;
    }
 
    public void createObject(ManagedObject managedObject, Class<? extends Serializable> clazz)
    {
       if (managedObject instanceof ManagedObjectImpl == false)
          throw new IllegalStateException("Unable to create object " + managedObject.getClass().getName());
       
       ManagedObjectImpl managedObjectImpl = (ManagedObjectImpl) managedObject;
       Serializable object = createUnderlyingObject(managedObjectImpl, clazz);
       populateManagedObject(managedObject, object);
    }
    
    public void populateManagedObject(ManagedObject managedObject, Serializable object)
    {
       if (managedObject instanceof ManagedObjectImpl == false)
          throw new IllegalStateException("Unable to populate managed object " + managedObject.getClass().getName());
       
       ManagedObjectImpl managedObjectImpl = (ManagedObjectImpl) managedObject;
       managedObjectImpl.setAttachment(object);
       populateValues(managedObjectImpl, object);
    }
    
    /**
     * Create the underlying object
     * 
     * @param managedObject the managed object
     * @param clazz the class
     * @return the object
     */
    protected Serializable createUnderlyingObject(ManagedObjectImpl managedObject, Class<? extends Serializable> clazz)
    {
       BeanInfo beanInfo = configuration.getBeanInfo(clazz);
       try
       {
          Object result = beanInfo.newInstance();
          return Serializable.class.cast(result);
       }
       catch (Throwable t)
       {
          throw new RuntimeException("Unable to create new object for " + managedObject + " clazz=" + clazz, t);
       }
    }
    
    /**
     * Populate the values
     * 
     * @param managedObject the managed object
     * @param object the object
     */
    protected void populateValues(ManagedObjectImpl managedObject, Serializable object)
    {
       BeanInfo beanInfo = configuration.getBeanInfo(object.getClass());
 
       Set<ManagedProperty> properties = managedObject.getProperties();
       if (properties != null && properties.size() > 0)
       {
          for (ManagedProperty property : properties)
          {
             MetaValue value = getValue(beanInfo, property, object);
             if (value != null)
                property.setField(Fields.VALUE, value);
          }
       }
    }
 
    /**
     * Get a value
     * 
     * @param beanInfo the bean info
     * @param property the property
     * @param object the object
     * @return the meta value
     */
    protected MetaValue getValue(BeanInfo beanInfo, ManagedProperty property, Serializable object)
    {
       // First look to the mapped name
       String name = property.getMappedName();
       if (name == null)
          property.getName();
 
       PropertyInfo propertyInfo = beanInfo.getProperty(name);
       if (propertyInfo == null)
          throw new IllegalStateException("Unable to find property: " + name + " for " + object.getClass().getName());
       
       Object value = null;
       try
       {
          value = propertyInfo.get(object);
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException("Error getting property " + name + " for " + object.getClass().getName(), t);
       }
 
       if (value == null)
          return null;
 
       MetaType propertyType = property.getMetaType();
       if (MANAGED_OBJECT_META_TYPE == propertyType)
       {
          if (value instanceof Serializable == false)
             throw new IllegalStateException("Object is not serializable: " + value.getClass().getName());
          ManagedObject mo = initManagedObject((Serializable) value);
          return new GenericValueSupport(MANAGED_OBJECT_META_TYPE, mo);
       }
       else if (propertyType.isArray())
       {
          ArrayMetaType arrayType = ArrayMetaType.class.cast(propertyType);
          if (MANAGED_OBJECT_META_TYPE == arrayType.getElementType())
          {
             Collection cvalue = getAsCollection(value);
             ArrayMetaType moType = new ArrayMetaType(1, MANAGED_OBJECT_META_TYPE);
             ArrayValueSupport moArrayValue = new ArrayValueSupport(moType);
             ArrayList<ManagedObject> tmp = new ArrayList<ManagedObject>();
             for(Object element : cvalue)
             {
                ManagedObject mo = initManagedObject((Serializable) element);
                tmp.add(mo);
             }
             ManagedObject[] mos = new ManagedObject[tmp.size()];
             tmp.toArray(mos);
             moArrayValue.setValue(mos);
             return moArrayValue;
          }
       }
       
       return metaValueFactory.create(value, propertyInfo.getType());
    }
 
    protected ManagedOperation getManagedOperation(MethodInfo methodInfo,
          ManagementOperation opAnnotation)
    {
       String name = methodInfo.getName();
       String description = opAnnotation.description();
       Impact impact = opAnnotation.impact();
       ParameterInfo[] params = methodInfo.getParameters();
       TypeInfo returnInfo = methodInfo.getReturnType();
       MetaType returnType = metaTypeFactory.resolve(returnInfo);
       ArrayList<ManagedParameter> mparams = new ArrayList<ManagedParameter>();
       if( params != null )
       {
          for(ParameterInfo param : params)
          {
             
          }
       }
       ManagedParameter[] parameters = new ManagedParameter[mparams.size()];
       mparams.toArray(parameters);
 
       ManagedOperationImpl op = new ManagedOperationImpl(name, description, impact,
             parameters, returnType);
       return op;
    }
 
    /**
     * Get the builder for a class
     * 
     * @param clazz the class
     * @return the builder
     */
    protected ManagedObjectBuilder getBuilder(Class<?> clazz)
    {
       synchronized (builders)
       {
          WeakReference<ManagedObjectBuilder> weak = builders.get(clazz);
          if (weak != null)
             return weak.get();
       }
       return this;
    }
    
    /**
     * Get the populator for a class
     * 
     * @param clazz the class
     * @return the populator
     */
    @SuppressWarnings("unchecked")
    protected ManagedObjectPopulator<Serializable> getPopulator(Class<?> clazz)
    {
       ManagedObjectBuilder builder = getBuilder(clazz);
       if (builder instanceof ManagedObjectPopulator)
          return (ManagedObjectPopulator) builder;
       return this;
    }
 
    protected Collection getAsCollection(Object value)
    {
       if( value.getClass().isArray() )
          return Arrays.asList(value);
       else if (value instanceof Collection)
          return Collection.class.cast(value);
       return null;
    }
 
    /**
     * Look for ctor(Fields)
     * @param factory - the ManagedProperty implementation class
     * @param fields - the fields to pass to the ctor
     * @return
     */
    protected ManagedProperty getManagedProperty(Class<? extends ManagedProperty> factory, Fields fields)
    {
       ManagedProperty property = null;
       try
       {
          Class[] sig = {Fields.class};
          Constructor<? extends ManagedProperty> ctor = factory.getConstructor(sig);
          Object[] args = {fields};
          property = ctor.newInstance(args);
       }
       catch(Exception e)
       {
          log.debug("Failed to create ManagedProperty", e);
       }
       return property;
    }
 }
