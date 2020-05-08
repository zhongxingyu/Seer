 package com.h2.org.springframework.beans;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 public class Bean {
    
    public static enum BeanType {
       NONE,
       OBJECT,
       MAP,
       PROPERTIES,
    };
    
    private String _id;
    
    private String _name;
    
    private String _clazzName;
    
    private String _parent;
    
    private String _scope;
    
    private Boolean _abstract;
    
    private String _initMethod;
    
    private Map<String, String> _properties;
    
    private Collection<IValueBean> _beanProperties;
    
    private BeanType _type;
    
    private Object _instantiatedObject;
    
    private Class<?> _clazz;
    
    private String _factoryBean;
    
    private String _factoryMethod;
    
    private String _dependsOn;
    
    private List<ConstructorArg> _constructorArgs =
       new ArrayList<ConstructorArg>();
    
    private String _context;
    
    ////
    ////
    
    public Bean() {
       //nothing
    }
    
    public Bean(String id, String clazzName) {
       setId(id);
       setClazzName(clazzName);
    }
    
    @Override
    public String toString() {
       return String.format("Bean: %s %s %s in %s",
             getId(), getName(), getClazzName(), getContext());
    }
    
    ////
 
    /**
     * @return the id
     */
    public String getId() {
       return _id;
    }
 
    /**
     * @param id the id to set
     */
    public void setId(String id) {
       _id = id;
    }
 
    /**
     * @return the name
     */
    public String getName() {
       return _name;
    }
 
    /**
     * @param name the name to set
     */
    public void setName(String name) {
       _name = name;
    }
 
    /**
     * @return the clazzName
     */
    public String getClazzName() {
       return _clazzName;
    }
 
    /**
     * @param class1 the class to set
     */
    public void setClazzName(String class1) {
       _clazzName = class1;
    }
 
    /**
     * @return the parent
     */
    public String getParent() {
       return _parent;
    }
 
    /**
     * @param parent the parent to set
     */
    public void setParent(String parent) {
       _parent = parent;
    }
 
    /**
     * @return the scope
     */
    public String getScope() {
       return _scope;
    }
 
    /**
     * @param scope the scope to set
     */
    public void setScope(String scope) {
       _scope = scope;
    }
 
    /**
     * @return the abstract
     */
    public Boolean getAbstract() {
       return _abstract;
    }
 
    /**
     * @param abstract1 the abstract to set
     */
    public void setAbstract(Boolean abstract1) {
       _abstract = abstract1;
    }
 
    /**
     * @return the initMethod
     */
    public String getInitMethod() {
       return _initMethod;
    }
 
    /**
     * @param initMethod the initMethod to set
     */
    public void setInitMethod(String initMethod) {
       _initMethod = initMethod;
    }
 
    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
       return _properties;
    }
 
    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {
       _properties = properties;
    }
 
    /**
     * @return the type
     */
    public BeanType getType() {
       return _type;
    }
 
    /**
     * @param type the type to set
     */
    public void setType(BeanType type) {
       _type = type;
    }
 
    /**
     * @return the beanProperties
     */
    public Collection<IValueBean> getBeanProperties() {
       return _beanProperties;
    }
 
    /**
     * @param beanProperties the beanProperties to set
     */
    public void setBeanProperties(Collection<IValueBean> beanProperties) {
       _beanProperties = beanProperties;
    }
 
    /**
     * @return the instantiatedObject
     */
    public Object getInstantiatedObject() {
       return _instantiatedObject;
    }
 
    /**
     * @param instantiatedObject the instantiatedObject to set
     */
    public void setInstantiatedObject(Object instantiatedObject) {
       _instantiatedObject = instantiatedObject;
    }
 
    /**
     * @return the clazz
     */
    public Class<?> getClazz() {
       return _clazz;
    }
 
    /**
     * @param clazz the clazz to set
     */
    public void setClazz(Class<?> clazz) {
       _clazz = clazz;
    }
 
    public String getNameResolved() {
       if(getId() != null) {
          return getId();
       }
       else if (getName() != null) {
          return getName();
       }
      return null;
    }
 
    /**
     * @return the factoryBean
     */
    public String getFactoryBean() {
       return _factoryBean;
    }
 
    /**
     * @param factoryBean the factoryBean to set
     */
    public void setFactoryBean(String factoryBean) {
       _factoryBean = factoryBean;
    }
 
    /**
     * @return the factoryMethod
     */
    public String getFactoryMethod() {
       return _factoryMethod;
    }
 
    /**
     * @param factoryMethod the factoryMethod to set
     */
    public void setFactoryMethod(String factoryMethod) {
       _factoryMethod = factoryMethod;
    }
 
    /**
     * @return the dependsOn
     */
    public String getDependsOn() {
       return _dependsOn;
    }
 
    /**
     * @param dependsOn the dependsOn to set
     */
    public void setDependsOn(String dependsOn) {
       _dependsOn = dependsOn;
    }
 
    /**
     * @return the constructorArgs
     */
    public List<ConstructorArg> getConstructorArgs() {
       return _constructorArgs;
    }
 
    /**
     * @param constructorArgs the constructorArgs to set
     */
    public void setConstructorArgs(List<ConstructorArg> constructorArgs) {
       _constructorArgs = constructorArgs;
    }
 
    /**
     * @return the context
     */
    public String getContext() {
       return _context;
    }
 
    /**
     * @param context the context to set
     */
    public void setContext(String context) {
       _context = context;
    }
 
 }
