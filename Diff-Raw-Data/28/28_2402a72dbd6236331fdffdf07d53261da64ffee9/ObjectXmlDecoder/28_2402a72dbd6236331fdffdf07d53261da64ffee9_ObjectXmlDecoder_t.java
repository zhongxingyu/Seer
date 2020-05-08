 package org.otherobjects.cms.io;
 
 import java.beans.PropertyEditor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.binding.CmsNodeReferenceEditor;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.datastore.JackrabbitDataStore;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.types.PropertyDef;
 import org.otherobjects.cms.types.PropertyDefImpl;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.types.annotation.PropertyType;
 
 public class ObjectXmlDecoder
 {
     Map<String, Object> objects = new HashMap<String, Object>();
 
     private JackrabbitDataStore jackrabbitDataStore;
     private TypeService typeService;
     private DaoService daoService;
 
     public List<Object> decode(Document document)
     {
         try
         {
             if (document.getRootElement().getName().equals("objects"))
             {
                 scanObjects(document);
                 return populateObjects(document);
             }
             else
             {
                 throw new OtherObjectsException("No objects found. Root element must be &lt;objects&gt;");
             }
         }
         catch (Exception e)
         {
             throw new OtherObjectsException("Error encoding object.", e);
         }
     }
 
     /**
      * Scans all objects in document add registers them against their ID in
      * the objects map. 
      */
     @SuppressWarnings("unchecked")
     private void scanObjects(Document document)
     {
         List<Element> elements = document.selectNodes("/objects/object");
 
         for (Element element : elements)
         {
             String typeName = element.attribute("type").getValue();
             if (element.attribute("id") != null)
             {
                 String id = element.attribute("id").getValue();
                 TypeDef typeDef = typeService.getType(typeName);
                 Object object = jackrabbitDataStore.create(typeDef, null);
                 objects.put(id, object);
             }
         }
     }
 
     /**
      * Populates discovered objects with data from document. Sets references between objects
      * where possible. 
      * @throws NoSuchMethodException 
      * @throws InvocationTargetException 
      * @throws IllegalAccessException 
      */
     @SuppressWarnings("unchecked")
     private List<Object> populateObjects(Document document) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         List<Element> elements = document.selectNodes("/objects/object");
 
         List<Object> populatedObjects = new ArrayList<Object>();
         for (Element element : elements)
         {
             String typeName = element.attribute("type").getValue();
             TypeDef typeDef = typeService.getType(typeName);
             Object item = null;
             if (element.attribute("id") != null)
             {
                 String id = element.attribute("id").getValue();
                 item = objects.get(id);
             }
             else
             {
                 item = jackrabbitDataStore.create(typeDef, null);
             }
 
             populateObject(element, typeDef, item);
             populatedObjects.add(item);
         }
         return populatedObjects;
     }
 
     private void populateObject(Element element, TypeDef typeDef, Object item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         for (PropertyDef property : typeDef.getProperties())
         {
             Object el = element.selectNodes("* [@name='" + property.getName() + "']").get(0);
             setProperty((Element) el, item, (PropertyDefImpl) property);
         }
 
         // Set jcrPath if present
         if (item instanceof BaseNode)
         {
             String jcrPath = element.attributeValue("path");
             if (jcrPath != null)
                 ((BaseNode) item).setJcrPath(jcrPath);
             ((BaseNode) item).setId(element.attributeValue("id"));
         }
     }
 
     private void setProperty(Element element, Object item, PropertyDefImpl prop) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
 
         if (prop.getType().equals(PropertyType.LIST.value()))
         {
             setListProperty(element, (PropertyDefImpl) prop, item);
         }
         else if (prop.getType().equals(PropertyType.REFERENCE.value()))
         {
             setReferenceProperty(element, (PropertyDefImpl) prop, item);
         }
         else if (prop.getType().equals(PropertyType.COMPONENT.value()))
         {
             setComponentProperty(element, (PropertyDefImpl) prop, item);
         }
         else
         {
             setSimpleProperty(element, (PropertyDefImpl) prop, item);
         }
     }
 
     private void setSimpleProperty(Element element, PropertyDefImpl property, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String path = property.getPropertyPath();
         if (element.attribute("null") != null)
         {
             // Handle nulls
             PropertyUtils.setProperty(object, path, null);
         }
         else
         {
             PropertyEditor propertyEditor = property.getPropertyEditor();
             propertyEditor.setAsText(element.attributeValue("value"));
             PropertyUtils.setProperty(object, path, propertyEditor.getValue());
         }
     }
 
     private void setReferenceProperty(Element element, PropertyDefImpl property, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String path = property.getPropertyPath();
         if (element.attribute("null") != null)
         {
             // Handle nulls
             PropertyUtils.setProperty(object, path, null);
         }
         else
         {
             CmsNodeReferenceEditor propertyEditor = new CmsNodeReferenceEditor(daoService, property.getRelatedType());
             propertyEditor.setAsText(element.attributeValue("ref"));
             PropertyUtils.setProperty(object, path, propertyEditor.getValue());
         }
     }
 
     private void setComponentProperty(Element element, PropertyDefImpl property, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String path = property.getPropertyPath();
 
         if (element.attribute("null") != null)
         {
             // Handle nulls
             PropertyUtils.setProperty(object, path, null);
         }
         else
         {
            Object component = createComponent(property);
             populateObject(element.element("object"), property.getRelatedTypeDef(), component);
             PropertyUtils.setProperty(object, path, component);
         }
     }
    
    /**
     * FIXME Merge this with FormController/TypeService
     * FIXME this is very hacky atm
     */
    private Object createComponent(PropertyDef propertyDef)
    {
        try
        {
            Object n = Class.forName(propertyDef.getRelatedTypeDef().getClassName()).newInstance();
            return n;
        }
        catch (Exception e)
        {
            throw new OtherObjectsException("Could not create object for property: " + propertyDef, e);
        }
    }
 
     @SuppressWarnings("unchecked")
     private void setListProperty(Element element, PropertyDefImpl property, Object item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String path = property.getPropertyPath();
         if (element.attribute("null") != null)
         {
             // Handle nulls
             PropertyUtils.setProperty(item, path, null);
         }
         else
         {
             int counter = 0;
             List<Element> elements = element.elements("property");
 
             List list = new ArrayList(elements.size());
             for (int i = 0; i < (elements.size()); i++)
             {
                 list.add(null);
             }
 
             PropertyUtils.setProperty(item, path, list);
 
             for (Element el : elements)
             {
                 PropertyDefImpl pd = new PropertyDefImpl();
                 pd.setName(property.getName() + "[" + counter++ + "]");
                 pd.setType(property.getCollectionElementType());
                 pd.setParentTypeDef(property.getParentTypeDef());
                 pd.setRelatedType(property.getRelatedType());
                 setProperty(el, item, pd);
             }
         }
     }
 
     public void setJackrabbitDataStore(JackrabbitDataStore jackrabbitDataStore)
     {
         this.jackrabbitDataStore = jackrabbitDataStore;
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     public void setDaoService(DaoService daoService)
     {
         this.daoService = daoService;
     }
 }
