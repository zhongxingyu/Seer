 package org.otherobjects.cms.jcr.dynamic;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.jcr.Node;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.Value;
 import javax.jcr.ValueFactory;
 import javax.jcr.ValueFormatException;
 import javax.persistence.PersistenceException;
 
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.AtomicTypeConverter;
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.AtomicTypeConverterProvider;
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.DefaultAtomicTypeConverterProvider;
 import org.apache.jackrabbit.ocm.manager.beanconverter.BeanConverter;
 import org.apache.jackrabbit.ocm.manager.beanconverter.impl.DefaultBeanConverterImpl;
 import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.CollectionConverter;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableCollection;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableObjects;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.AbstractCollectionConverterImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.DefaultCollectionConverterImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.ManageableMapImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.MultiValueCollectionConverterImpl;
 import org.apache.jackrabbit.ocm.manager.objectconverter.ObjectConverter;
 import org.apache.jackrabbit.ocm.manager.objectconverter.impl.ObjectConverterImpl;
 import org.apache.jackrabbit.ocm.mapper.Mapper;
 import org.apache.jackrabbit.ocm.mapper.model.BeanDescriptor;
 import org.apache.jackrabbit.ocm.mapper.model.CollectionDescriptor;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.SingletonBeanLocator;
 import org.otherobjects.cms.types.PropertyDef;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeServiceImpl;
 import org.springframework.util.Assert;
 
 @SuppressWarnings("unchecked")
 public class DynaNodeDataMapConverterImpl extends AbstractCollectionConverterImpl
 {
 
     private AtomicTypeConverterProvider atomicTypeConverterProvider = new DefaultAtomicTypeConverterProvider();
     private TypeServiceImpl typeService;
     private ObjectConverter objectConverter = new ObjectConverterImpl(this.mapper, atomicTypeConverterProvider);
 
     /**
      * Constructor
      *
      * @param atomicTypeConverters
      * @param objectConverter
      * @param mapper
      */
     public DynaNodeDataMapConverterImpl(Map atomicTypeConverters, ObjectConverter objectConverter, Mapper mapper)
     {
         super(atomicTypeConverters, objectConverter, mapper);
         typeService = (TypeServiceImpl) SingletonBeanLocator.getBean("typeService");
     }
 
     /**
      *
      * @see AbstractCollectionConverterImpl#doInsertCollection(Session, Node, CollectionDescriptor, ManageableCollection)
      */
     @Override
     protected void doInsertCollection(Session session, Node parentNode, CollectionDescriptor descriptor, ManageableObjects collection) throws RepositoryException
     {
         try
         {
             if (collection == null)
             {
                 return;
             }
            Node dataNode = parentNode;//.addNode(jcrName, "oo:dynadata");
             ValueFactory valueFactory = session.getValueFactory();
             Map map = (Map) collection.getObjects();
 
             String ooType = parentNode.getProperty("ooType").getString();
             TypeDef type = typeService.getType(ooType);
 
             Iterator i = map.keySet().iterator();
             while (i.hasNext())
             {
                 String propertyName = (String) i.next();
                 Object fieldValue = map.get(propertyName);
                 PropertyDef property = type.getProperty(propertyName);
                 Assert.notNull(property, "Unmapped property in node " + ooType + ": " + propertyName);
                 String propertyType = property.getType();
                 //String collectionElementType = property.getCollectionElementType();
 
                 if (propertyType != null && propertyType.equals(PropertyDef.LIST))
                 {
                     insertList(session, dataNode, propertyName, fieldValue, property);
                 }
                 else if (propertyType.equals(PropertyDef.COMPONENT))
                 {
                    if(parentNode.hasNode(propertyName))
                        parentNode.getNode(propertyName).remove();
                     insertComponentProperty(session, dataNode, propertyName, fieldValue);
                 }
                 else if (propertyType.equals(PropertyDef.REFERENCE))
                 {
                     insertReferenceProperty(session, dataNode, propertyName, fieldValue);
                 }
                 else
                 {
                     insertSimpleProperty(dataNode, valueFactory, property, fieldValue);
                 }
             }
 
         }
         catch (ValueFormatException vfe)
         {
             throw new PersistenceException("Cannot insert collection field : " + descriptor.getFieldName() + " of class " + descriptor.getClassDescriptor().getClassName(), vfe);
         }
     }
 
     private void insertList(Session session, Node dataNode, String propertyName, Object collection, PropertyDef property)
     {
 
         CollectionDescriptor collectionDescriptor = new CollectionDescriptor();
         collectionDescriptor.setFieldName(propertyName);
         collectionDescriptor.setJcrName(propertyName);
         String propertyType = property.getType();
 
         CollectionConverter collectionConverter = null;
         String elementClassName = null;
         if (propertyType.equals("component"))
         {
             collectionConverter = new DefaultCollectionConverterImpl(atomicTypeConverterProvider.getAtomicTypeConverters(), objectConverter, mapper);
             elementClassName = DynaNode.class.getName();
         }
         else if (propertyType.equals("reference"))
         {
             collectionConverter = new BeanReferenceCollectionConverterImpl(atomicTypeConverterProvider.getAtomicTypeConverters(), objectConverter, mapper);
             elementClassName = DynaNode.class.getName();
         }
         else
         {
             collectionConverter = new MultiValueCollectionConverterImpl(atomicTypeConverterProvider.getAtomicTypeConverters(), objectConverter, mapper);
         }
 
         collectionDescriptor.setElementClassName(elementClassName);
         //ManageableCollection manageableCollection = ManageableObjectsUtil.getManageableCollection(collection);
         collectionConverter.insertCollection(session, dataNode, collectionDescriptor, new ManageableMapImpl((Map) collection));
     }
 
     private void insertReferenceProperty(Session session, Node dataNode, String key, Object fieldValue)
     {
         BeanConverter beanConverter = new ReferenceBeanConverterImpl(this.mapper, objectConverter, atomicTypeConverterProvider);
         BeanDescriptor beanDescriptor = new BeanDescriptor();
         beanDescriptor.setFieldName(key);
         beanDescriptor.setJcrName(key);
         beanConverter.insert(session, dataNode, beanDescriptor, mapper.getClassDescriptorByClass(DynaNode.class), fieldValue, mapper.getClassDescriptorByClass(DynaNode.class), null);
     }
 
     private void insertComponentProperty(Session session, Node dataNode, String key, Object fieldValue)
     {
         // FIXME Need test for this. How can we then remove a component?
         if (fieldValue != null)
         {
             BeanConverter beanConverter = new DefaultBeanConverterImpl(this.mapper, objectConverter, atomicTypeConverterProvider);
             BeanDescriptor beanDescriptor = new BeanDescriptor();
             beanDescriptor.setFieldName(key);
             beanDescriptor.setJcrName(key);
             beanConverter.insert(session, dataNode, beanDescriptor, mapper.getClassDescriptorByClass(DynaNode.class), fieldValue, mapper.getClassDescriptorByClass(DynaNode.class), null);
         }
     }
 
     private void insertSimpleProperty(Node dataNode, ValueFactory valueFactory, PropertyDef property, Object value) throws RepositoryException
     {
         AtomicTypeConverter atomicTypeConverter = (AtomicTypeConverter) typeService.getJcrConverter(property.getType());
         Value v = atomicTypeConverter.getValue(valueFactory, value);
         dataNode.setProperty(property.getName(), v);
     }
 
     /**
      *
      * @see AbstractCollectionConverterImpl#doUpdateCollection(Session, Node, CollectionDescriptor, ManageableCollection)
      */
     @Override
     protected void doUpdateCollection(Session session, Node parentNode, CollectionDescriptor collectionDescriptor, ManageableObjects collection) throws RepositoryException
     {
         String jcrName = getCollectionJcrName(collectionDescriptor);
 
         // Delete existing values
         if (parentNode.hasNode(jcrName))
         {
             parentNode.getNode(jcrName).remove();
         }
 
         // Add values
         doInsertCollection(session, parentNode, collectionDescriptor, collection);
     }
 
     /**
      * @see AbstractCollectionConverterImpl#doGetCollection(Session, Node, CollectionDescriptor, Class)
      */
     protected ManageableObjects doGetCollection(Session session, Node parentNode, CollectionDescriptor collectionDescriptor, Class collectionFieldClass) throws RepositoryException
     {
         try
         {
//            if (!parentNode.hasNode(jcrName))
//            {
//                return null;
//            }
 
             String ooType = parentNode.getProperty("ooType").getString();
             TypeDef type = typeService.getType(ooType);
 
            Node dataNode = parentNode;//.getNode(jcrName);
             Map map = new HashMap();
 
             for (PropertyDef property : type.getProperties())
             {
                 String propertyType = property.getType();
                 Object value = null;
 
                 if (propertyType.equals(PropertyDef.LIST))
                 {
                     value = retrieveList(session, dataNode, property);
                 }
                 else if (propertyType.equals(PropertyDef.COMPONENT))
                 {
                     value = retrieveComponentProperty(session, dataNode, property);
                 }
                 else if (propertyType.equals(PropertyDef.REFERENCE))
                 {
                     value = retrieveReferenceProperty(session, dataNode, property);
                 }
                 else
                 {
                     value = retrieveSimpleProperty(dataNode, property);
 
                 }
                 map.put(property.getName(), value);
 
             }
             return new ManageableMapImpl(map);
         }
         catch (ValueFormatException vfe)
         {
             throw new PersistenceException("Cannot get the collection field : " + collectionDescriptor.getFieldName() + "for class " + collectionDescriptor.getClassDescriptor().getClassName(), vfe);
         }
     }
 
     private ManageableObjects retrieveList(Session session, Node dataNode, PropertyDef property)
     {
 
         CollectionDescriptor collectionDescriptor = new CollectionDescriptor();
         collectionDescriptor.setFieldName(property.getName());
         collectionDescriptor.setJcrName(property.getName());
         String propertyType = property.getType();
 
         String elementClassName = null;
         CollectionConverter collectionConverter = null;
         if (propertyType.equals("component"))
         {
             collectionConverter = new DefaultCollectionConverterImpl(atomicTypeConverterProvider.getAtomicTypeConverters(), objectConverter, mapper);
             elementClassName = DynaNode.class.getName();
         }
         else if (propertyType.equals("reference"))
         {
             collectionConverter = new BeanReferenceCollectionConverterImpl(atomicTypeConverterProvider.getAtomicTypeConverters(), objectConverter, mapper);
             elementClassName = DynaNode.class.getName();
         }
         else
         {
             collectionConverter = new MultiValueCollectionConverterImpl(atomicTypeConverterProvider.getAtomicTypeConverters(), objectConverter, mapper);
             elementClassName = typeService.getJcrClassMapping(propertyType).getName();
         }
         collectionDescriptor.setElementClassName(elementClassName);
         return collectionConverter.getCollection(session, dataNode, collectionDescriptor, ArrayList.class);
     }
 
     private Object retrieveSimpleProperty(Node dataNode, PropertyDef property) throws RepositoryException
     {
         if (dataNode.hasProperty(property.getName()))
         {
             Property jcrProperty = dataNode.getProperty(property.getName());
             AtomicTypeConverter atomicTypeConverter = (AtomicTypeConverter) typeService.getJcrConverter(property.getType());
             return atomicTypeConverter.getObject(jcrProperty.getValue());
         }
         else
             return null;
     }
 
     private Object retrieveReferenceProperty(Session session, Node dataNode, PropertyDef property)
     {
         Object value;
         BeanConverter beanConverter = new ReferenceBeanConverterImpl(this.mapper, objectConverter, atomicTypeConverterProvider);
         BeanDescriptor beanDescriptor = new BeanDescriptor();
         beanDescriptor.setFieldName(property.getName());
         beanDescriptor.setJcrName(property.getName());
         value = beanConverter.getObject(session, dataNode, beanDescriptor, mapper.getClassDescriptorByClass(DynaNode.class), DynaNode.class, null);
         return value;
     }
 
     private Object retrieveComponentProperty(Session session, Node dataNode, PropertyDef property)
     {
         Object value;
         BeanConverter beanConverter = new DefaultBeanConverterImpl(this.mapper, objectConverter, atomicTypeConverterProvider);
         BeanDescriptor beanDescriptor = new BeanDescriptor();
         beanDescriptor.setFieldName(property.getName());
         beanDescriptor.setJcrName(property.getName());
         Class clazz = DynaNode.class;
         try
         {
             // FIXME Clean up this method
             String propertyName = property.getFieldName().replaceAll("data\\[","").replaceAll("\\]","");
             if(!dataNode.hasProperty(propertyName + "/ocm_classname"))
                 return null;    
             Property classNameProperty = dataNode.getProperty(propertyName + "/ocm_classname");
             String className = classNameProperty.getString();
             clazz = Class.forName(className);
         }
         catch (Exception e)
         {
             throw new OtherObjectsException("Could not determine class for component.", e);
         }
         value = beanConverter.getObject(session, dataNode, beanDescriptor, mapper.getClassDescriptorByClass(clazz), clazz, null);
         return value;
     }
 
     /**
      * @see AbstractCollectionConverterImpl#doIsNull(Session, Node, CollectionDescriptor, Class)
      */
     protected boolean doIsNull(Session session, Node parentNode, CollectionDescriptor collectionDescriptor, Class collectionFieldClass) throws RepositoryException
     {
        // FIXME This doesn't work now we have don't have a subnode 
         String jcrName = getCollectionJcrName(collectionDescriptor);
 
         if (!parentNode.hasNode(jcrName))
         {
             return true;
         }
         return false;
     }
 
 }
