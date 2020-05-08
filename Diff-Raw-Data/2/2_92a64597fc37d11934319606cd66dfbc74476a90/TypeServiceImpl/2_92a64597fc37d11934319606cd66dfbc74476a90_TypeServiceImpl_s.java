 package org.otherobjects.cms.types;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.AtomicTypeConverter;
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.BooleanTypeConverterImpl;
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.Date2LongTypeConverterImpl;
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.LongTypeConverterImpl;
 import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.StringTypeConverterImpl;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.discovery.AnnotatedClassesScanner;
 import org.otherobjects.cms.jcr.BigDecimalTypeConverterImpl;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.JcrTypeDef;
 import org.otherobjects.cms.types.annotation.Type;
 import org.otherobjects.cms.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 
 public class TypeServiceImpl extends AbstractTypeService implements InitializingBean
 {
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private Map<String, AtomicTypeConverter> jcrAtomicConverters;
     private Map<String, Class<?>> jcrClassMappings;
 
     private AnnotationBasedTypeDefBuilder annotationBasedTypeDefBuilder;
 
     @Resource
     private OtherObjectsConfigurator otherObjectsConfigurator;
 
     private AnnotatedClassesScanner scanner;
 
     public void setScanner(AnnotatedClassesScanner scanner)
     {
         this.scanner = scanner;
     }
 
     @SuppressWarnings("unchecked")
     public void afterPropertiesSet() throws Exception
     {
         reset();
 
         // FIXME Temp hack to manually load annotated types
         try
         {
             String otherObjectsModelPackages = otherObjectsConfigurator.getProperty("otherobjects.model.packages");
             Assert.notNull(otherObjectsModelPackages, "No model package for otherobjects defined. Check our otherobjects.properties for a otherobjects.model.packages property");
 
             List<String> packages = new ArrayList<String>();
             packages.add(otherObjectsModelPackages);
             packages.add(otherObjectsConfigurator.getProperty("site.model.sdsd.packages"));
             packages.add(otherObjectsConfigurator.getProperty("site.model.packages"));
 
             String[] annotatedPackages = StringUtils.join(packages, ',').split(",");
 
             logger.info("Scanning the following packages for OTHERobjects types: " + StringUtils.join(annotatedPackages, ','));
 
             Set<String> annotatedClasses = scanner.findAnnotatedClasses(annotatedPackages, Type.class);
 
             Assert.notEmpty(annotatedClasses, "Found no annotated classes. Check your configuration esp. the 'otherobjects.model.packages' and 'site.model.packages' properties");
 
             for (String clazz : annotatedClasses)
             {
                 registerType(annotationBasedTypeDefBuilder.getTypeDef(clazz));
             }
 
             // FIXME Add TypeDef Validator here: labelProperty, extends BaseNode etc
         }
         catch (Exception e)
         {
             throw new OtherObjectsException("Error loading annotated classes.", e);
         }
 
         //        generateClasses();
     }
 
     @Override
     public TypeDef getType(String name)
     {
         TypeDef type = super.getType(name);
         return type;
     }
 
     /**
      * Loads TypeDefs from JCR. Types are stored in the default workspace at /types.
      * 
      * Called from BootstrapUtils.
      */
     public void loadJcrBackedTypes(UniversalJcrDao universalJcrDao)
     {
         // FIXME Change to getAllByType
         List<BaseNode> typeDefs = universalJcrDao.getAllByPath("/types/");
         for (BaseNode t : typeDefs)
         {
             TypeDef t2 = ((JcrTypeDef) t).toTypeDef();
             t2.setTypeService(this);
             registerType(t2);
         }
         //        generateClasses();
     }
 
     /**
      * Generates classes for types not backed by an existing class.
      */
     //    public void generateClasses()
     //    {
     //        for (TypeDef t : getTypes())
     //        {
     //            generateClass(t);
     //        }
     //    }
     //
     //    public void generateClass(TypeDef t)
     //    {
     //        if (!t.hasClass())
     //        {
     //            // Create bean class
     //            this.lo
     //        }
     //    }
     public Object getJcrConverter(String type)
     {
         AtomicTypeConverter atomicTypeConverter = getJcrAtomicConverters().get(type);
         if (atomicTypeConverter == null)
             throw new OtherObjectsException("No JCR converter defined for type: " + type);
         return atomicTypeConverter;
     }
 
     public Class<?> getJcrClassMapping(String type)
     {
         Class<?> clazz = getJcrClassMappings().get(type);
         if (clazz == null)
             throw new OtherObjectsException("No JCR class defined for type: " + type);
         return clazz;
     }
 
     public Map<String, AtomicTypeConverter> getJcrAtomicConverters()
     {
         return this.jcrAtomicConverters;
     }
 
     public void setJcrAtomicConverters(Map<String, AtomicTypeConverter> jcrAtomicConverters)
     {
         this.jcrAtomicConverters = jcrAtomicConverters;
     }
 
     public Map<String, Class<?>> getJcrClassMappings()
     {
         return this.jcrClassMappings;
     }
 
     public void setJcrClassMappings(Map<String, Class<?>> jcrClassMappings)
     {
         this.jcrClassMappings = jcrClassMappings;
     }
 
     public void reset()
     {
         registerConverters();
         registerClassMappings();
     }
 
     public String getClassNameForType(String type)
     {
         return this.jcrClassMappings.get(type).getName();
     }
 
    public Class getClassForType(String type)
     {
         return this.jcrClassMappings.get(type);
     }
 
     private void registerConverters()
     {
         this.jcrAtomicConverters = new HashMap<String, AtomicTypeConverter>();
         this.jcrAtomicConverters.put("string", new StringTypeConverterImpl());
         this.jcrAtomicConverters.put("text", new StringTypeConverterImpl());
         this.jcrAtomicConverters.put("html", new StringTypeConverterImpl());
         this.jcrAtomicConverters.put("date", new Date2LongTypeConverterImpl());
         this.jcrAtomicConverters.put("time", new Date2LongTypeConverterImpl());
         this.jcrAtomicConverters.put("timestamp", new Date2LongTypeConverterImpl());
         this.jcrAtomicConverters.put("boolean", new BooleanTypeConverterImpl());
         this.jcrAtomicConverters.put("number", new LongTypeConverterImpl());
         this.jcrAtomicConverters.put("decimal", new BigDecimalTypeConverterImpl());
     }
 
     private void registerClassMappings()
     {
         this.jcrClassMappings = new HashMap<String, Class<?>>();
         this.jcrClassMappings.put("string", String.class);
         this.jcrClassMappings.put("text", String.class);
         this.jcrClassMappings.put("html", String.class);
         this.jcrClassMappings.put("date", Date.class);
         this.jcrClassMappings.put("time", Date.class);
         this.jcrClassMappings.put("timestamp", Date.class);
         this.jcrClassMappings.put("boolean", Boolean.class);
         this.jcrClassMappings.put("number", Long.class);
         this.jcrClassMappings.put("decimal", BigDecimal.class);
     }
 
     public void setAnnotationBasedTypeDefBuilder(AnnotationBasedTypeDefBuilder annotationBasedTypeDefBuilder)
     {
         this.annotationBasedTypeDefBuilder = annotationBasedTypeDefBuilder;
     }
 
     public void setOtherObjectsConfigurator(OtherObjectsConfigurator otherObjectsConfigurator)
     {
         this.otherObjectsConfigurator = otherObjectsConfigurator;
     }
 
 }
