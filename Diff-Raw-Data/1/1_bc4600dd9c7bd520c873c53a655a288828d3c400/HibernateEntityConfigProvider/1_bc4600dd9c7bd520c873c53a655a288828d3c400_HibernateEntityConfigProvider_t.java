 package org.otherobjects.cms.hibernate;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.discovery.AnnotatedClassesScanner;
 import org.otherobjects.cms.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 
@SuppressWarnings("unchecked")
 public class HibernateEntityConfigProvider implements InitializingBean
 {
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private AnnotatedClassesScanner scanner;
     private OtherObjectsConfigurator otherObjectsConfigurator;
 
     private Set<String> annotatedClasses;
     private String[] annotatedPackages;
 
     public void setScanner(AnnotatedClassesScanner scanner)
     {
         this.scanner = scanner;
     }
 
     public void afterPropertiesSet() throws Exception
     {
         Assert.notNull(scanner, "class path scanner must be set");
         Assert.notNull(otherObjectsConfigurator, "OtherObjectsConfigurator must be set");
         List<String> packages = new ArrayList<String>();
         packages.add(otherObjectsConfigurator.getProperty("otherobjects.model.packages"));
         packages.add(otherObjectsConfigurator.getProperty("site.entity.packages"));
 
         annotatedPackages = StringUtils.join(packages, ',').split(",");
 
         logger.info("Scanning the following packages: " + annotatedPackages);
 
         annotatedClasses = scanner.findAnnotatedClasses(annotatedPackages, Entity.class);
 
         logger.info("Found the following entities: " + StringUtils.join(annotatedClasses, ','));
     }
 
     public Class[] getAnnotatedClasses() throws Exception
     {
         Assert.notNull(annotatedClasses, "Classes haven't been discovered");
 
         Class[] classes = new Class[annotatedClasses.size()];
 
         int i = 0;
         for (String clazz : annotatedClasses)
         {
             classes[i] = Class.forName(clazz);
             i++;
         }
 
         return classes;
     }
 
     public String[] getAnnotatedPackages()
     {
         return annotatedPackages;
     }
 
     public void setOtherObjectsConfigurator(OtherObjectsConfigurator otherObjectsConfigurator)
     {
         this.otherObjectsConfigurator = otherObjectsConfigurator;
     }
 
 }
