 package com.undertree.showcase.jsf2.primefaces.admin;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.Application;
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.jar.Manifest;
 
 /**
  *
  */
 @ManagedBean
 @ApplicationScoped
 public class ManifestBean implements Serializable {
 
     private static final long serialVersionUID = 20120428L;
     static final Logger LOG = LoggerFactory.getLogger(ManifestBean.class);
 
     private Manifest localWarManifest;
     private String manifestPath;
     private String manifestBuiltBy;
     private String manifestBuildJDK;
 
 
     public ManifestBean() {
         LOG.trace("Creating ManifestBean");
        try {
            localWarManifest =
                    new Manifest(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
     }
 
     @PostConstruct
     public void initManifestBean() {
 
     }
 
     public String getManifestPath() {
         // TODO not sure how to get the path yet
         return ""; // StringUtils.defaultIfEmpty(localWarManifest.toString(), "unknown");
     }
 
     public String getManifestBuiltBy() {
         return ""; // localWarManifest.getMainAttributes().getValue("Built-By");
     }
 
     public String getManifestBuildJDK() {
         return ""; // localWarManifest.getMainAttributes().getValue("Build-Jdk");
     }
 }
 
 /* TODO this is a PoC
     public String getManifestInfo() {
 
         final Class<SystemInfoBean> clazz = SystemInfoBean.class;
         final String className = clazz.getSimpleName() + ".class";
         final String classPath = clazz.getResource(className).toString();
 
         LOG.trace("{} @ {}", className, classPath);
 
         manifestPath = "";
 
         if (classPath.startsWith("file:")) {
             manifestPath = classPath.substring(0, classPath.lastIndexOf("WEB-INF")) + "/META-INF/MANIFEST.MF";
             LOG.trace("manifestPath = {}", manifestPath);
         }
         else if (classPath.startsWith("war:")) {
             // todo see how this really works in tomcat, et al.
         }
 
         try {
             Manifest manifest = new Manifest(new URL(manifestPath).openStream());
             Attributes attr = manifest.getMainAttributes();
 
             manifestBuiltBy = attr.getValue("Built-By");
             LOG.trace("Built-By = {}", manifestBuiltBy);
 
             manifestBuildJDK = attr.getValue("Build-Jdk");
             LOG.trace("Build-Jdk = {}", manifestBuildJDK);
         }
         catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return "";
     } */
