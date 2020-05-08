 package org.otherobjects.cms.types;
 
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.util.StringUtils;
 import org.springframework.util.Assert;
 
 import flexjson.JSON;
 
 /**
  * @author rich
  */
 public class TypeDefImpl implements TypeDef
 {
     private static final String DEFAULT_ADMIN_CONTROLLER_URL = "/otherobjects/workbench";
 
     private static final String DEFAULT_SUPER_CLASS_NAME = BaseNode.class.getName();
 
     /** The unique name for this type. */
     private String name;
 
     /** The class name of the backing class (if not using CmsNode). */
     private String className;
 
     /** 
      * The name of the class that this type extends. Defaults to DEFAULT_SUPER_CLASS_NAME.
      * 
      *  <p>TODO Add support for extending Types themselves
      */
     private String superClassName;
 
     /** Collection of properties for this type. */
     private Map<String, PropertyDef> properties = new LinkedHashMap<String, PropertyDef>();
 
     /** Human friendly name for type. TODO Can be inferred from name. */
     private String label;
 
     /** Description for this type. */
     private String description;
 
     /** Help text to assist choosing value for this type. */
     private String help;
 
     /** Reference to TypeService where this TypeDef is registered. */
     private TypeService typeService;
 
     /** Stores which property should be used for the labe.. */
     private String labelProperty;
 
     /** Which data store to use for this type. */
     private String store = TypeDef.JACKRABBIT;
 
     /** Custom admin controller URL. */
     private String customAdminController;
 
     public TypeDefImpl()
     {
     }
 
     public TypeDefImpl(String name)
     {
         setName(name);
     }
 
     @Override
     public String toString()
     {
         return "[Type: " + getName() + "]";
     }
 
     public PropertyDef getProperty(String name)
     {
         return properties.get(name);
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public void setProperties(List<PropertyDef> properties)
     {
         this.properties = new LinkedHashMap<String, PropertyDef>();
         if (properties == null)
             return;
         for (PropertyDef pd : properties)
             addProperty(pd);
     }
 
     public List<PropertyDef> getProperties()
     {
         ArrayList<PropertyDef> properties = new ArrayList<PropertyDef>();
         properties.addAll(this.properties.values());
         return properties;
     }
 
     public void addProperty(PropertyDef pd)
     {
         // TODO Check for duplicates 
         Assert.isNull(getProperty(pd.getName()));
         ((PropertyDefImpl) pd).setParentTypeDef(this);
         this.properties.put(pd.getName(), pd);
     }
 
     public String getLabel()
     {
         if (org.apache.commons.lang.StringUtils.isEmpty(label))
         {
             if (getName().contains("."))
                 return StringUtils.generateLabel(org.apache.commons.lang.StringUtils.substringAfterLast(getName(), "."));
             else
                 return StringUtils.generateLabel(getName());
         }
         else
             return label;
     }
 
     public void setLabel(String label)
     {
         this.label = label;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     public String getHelp()
     {
         return help;
     }
 
     public void setHelp(String help)
     {
         this.help = help;
     }
 
     public String getClassName()
     {
         if (this.className != null)
             return this.className;
         else
             return getName();
     }
 
     public void setClassName(String className)
     {
         this.className = className;
     }
 
     @JSON(include = false)
     public TypeService getTypeService()
     {
         return typeService;
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     /**
      * Determines if this TypeDef has a class associated with it. This 
      * only return true if the backing class exists or has been generated.
      * @return
      */
     public boolean hasClass()
     {
         return (this.className != null);
     }
 
     public String getSuperClassName()
     {
         return superClassName != null ? superClassName : DEFAULT_SUPER_CLASS_NAME;
     }
 
     public void setSuperClassName(String superClassName)
     {
         this.superClassName = superClassName;
     }
 
     public String getLabelProperty()
     {
         return labelProperty;
     }
 
     public void setLabelProperty(String labelProperty)
     {
         this.labelProperty = labelProperty;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         return EqualsBuilder.reflectionEquals(this, obj);
     }
 
     public String getStore()
     {
         return store;
     }
 
     public void setStore(String store)
     {
         this.store = store;
     }
 
     public String getAdminControllerUrl()
     {
         if (customAdminController != null)
             return customAdminController;
         else
             return DEFAULT_ADMIN_CONTROLLER_URL;
     }
 
     public String getCustomAdminController()
     {
         return customAdminController;
     }
 
     public void setCustomAdminController(String customAdminController)
     {
         this.customAdminController = customAdminController;
     }
 }
