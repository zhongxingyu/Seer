 package org.otherobjects.cms.model;
 
 import java.util.List;
 
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeDefImpl;
 import org.otherobjects.cms.types.annotation.Property;
 import org.otherobjects.cms.types.annotation.PropertyType;
 import org.otherobjects.cms.types.annotation.Type;
 
 /**
  * FIXME Add validation
  * 
  * @author rich
  */
@Type(label = "Type Def", labelProperty="name")
 public class JcrTypeDef extends BaseNode
 {
     private String name;
     private String label;
     private String description;
     private String help;
     private String superClassName;
     private String labelProperty;
     private List<JcrPropertyDef> properties;
 
     public JcrTypeDef()
     {
     }
 
     public TypeDef toTypeDef()
     {
         TypeDefImpl td = new TypeDefImpl();
         td.setName(getName());
         td.setLabel(getLabel());
         td.setDescription(getDescription());
         td.setHelp(getHelp());
         td.setSuperClassName(getSuperClassName());
         td.setLabelProperty(getLabelProperty());
         if (getProperties() != null)
         {
             for (JcrPropertyDef pd : getProperties())
             {
                 td.addProperty(pd.toPropertyDef());
             }
         }
         return td;
     }
    
     @Override
     public String getOoLabel()
     {
         return getName();
     }
 
    @Property(order = 10)
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     @Property(order = 20)
     public String getLabel()
     {
         return label;
     }
 
     public void setLabel(String label)
     {
         this.label = label;
     }
 
     @Property(order = 30, type = PropertyType.TEXT)
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     @Property(order = 40, type = PropertyType.TEXT)
     public String getHelp()
     {
         return help;
     }
 
     public void setHelp(String help)
     {
         this.help = help;
     }
 
     @Property(order = 50)
     public String getSuperClassName()
     {
         return superClassName;
     }
 
     public void setSuperClassName(String superClassName)
     {
         this.superClassName = superClassName;
     }
 
     @Property(order = 60)
     public String getLabelProperty()
     {
         return labelProperty;
     }
 
     public void setLabelProperty(String labelProperty)
     {
         this.labelProperty = labelProperty;
     }
 
     @Property(order = 100, collectionElementType = PropertyType.COMPONENT)
     public List<JcrPropertyDef> getProperties()
     {
         return properties;
     }
 
     public void setProperties(List<JcrPropertyDef> properties)
     {
         this.properties = properties;
     }
 
 }
