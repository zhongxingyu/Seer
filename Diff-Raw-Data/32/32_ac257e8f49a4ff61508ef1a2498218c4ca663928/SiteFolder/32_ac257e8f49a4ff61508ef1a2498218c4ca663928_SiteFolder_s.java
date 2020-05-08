 package org.otherobjects.cms.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.otherobjects.cms.SingletonBeanLocator;
 import org.otherobjects.cms.Url;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.types.annotation.Property;
 import org.otherobjects.cms.types.annotation.PropertyType;
 import org.otherobjects.cms.types.annotation.Type;
 import org.otherobjects.cms.util.StringUtils;
 
 import flexjson.JSON;
 
 @Type(labelProperty = "label")
 public class SiteFolder extends BaseNode implements Folder
 {
     private String label;
    private String extraNavigationLabel;
     private String cssClass;
     private List<String> allowedTypes;
     private Url url;
     private boolean inMenu = true;
     private String tags;
 
     @Property(order = 5)
     public String getCode()
     {
         return super.getCode();
     }
 
     public void setCode(String code)
     {
         super.setCode(code);
     }
 
     public String getTags()
     {
         return tags;
     }
 
     public void setTags(String tags)
     {
         this.tags = tags;
     }
 
     @Override
     public boolean isFolder()
     {
         return true;
     }
 
     public List<TypeDef> getAllAllowedTypes()
     {
         TypeService typeService = ((TypeService) SingletonBeanLocator.getBean("typeService"));
         if (getAllowedTypes() != null && getAllowedTypes().size() > 0)
         {
             List<TypeDef> types = new ArrayList<TypeDef>();
             for (String t : getAllowedTypes())
             {
                 types.add(typeService.getType(t));
             }
             return types;
         }
         else
         {
             return new ArrayList<TypeDef>();//(List<TypeDef>) typeService.getTypesBySuperClass(BaseNode.class);
         }
     }
 
     @Property(order = 40)
     public String getCssClass()
     {
         return cssClass;
     }
 
     public void setCssClass(String cssClass)
     {
         this.cssClass = cssClass;
     }
 
     @JSON(include = false)
     @Property(order = 50, collectionElementType = PropertyType.STRING)
     public List<String> getAllowedTypes()
     {
         return allowedTypes;
     }
 
     public void setAllowedTypes(List<String> allowedTypes)
     {
         this.allowedTypes = allowedTypes;
     }
 
     @Override
     @Property(order = 20)
     public String getLabel()
     {
         return this.label;
     }
 
     @Override
     public void setLabel(String label)
     {
         this.label = label;
     }
 
     public int getDepth()
     {
         return getHref().getDepth();
     }
 
     public Url getHref()
     {
         if (url == null)
             url = new Url(getOoUrlPath());
         return url;
     }
 
     @Property(order = 35, type = PropertyType.BOOLEAN)
     public boolean isInMenu()
     {
         return inMenu;
     }
 
     public void setInMenu(boolean inMenu)
     {
         this.inMenu = inMenu;
     }
 
    @Property(order = 25)
    public String getExtraNavigationLabel()
     {
        return extraNavigationLabel;
     }
 
    public void setExtraNavigationLabel(String extraNavigationLabel)
     {
        this.extraNavigationLabel = extraNavigationLabel;
     }
 
     public String getNavigationLabel()
     {
        return (StringUtils.isNotBlank(getExtraNavigationLabel())) ? getExtraNavigationLabel() : getLabel();
     }
 }
