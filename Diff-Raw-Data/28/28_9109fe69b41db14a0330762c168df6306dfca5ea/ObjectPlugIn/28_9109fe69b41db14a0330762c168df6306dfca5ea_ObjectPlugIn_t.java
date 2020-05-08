package gov.nih.nci.ncicb.cadsr.umlmodelbrowser.struts.common;

 import gov.nih.nci.ncicb.cadsr.util.ObjectFactory;
 import javax.servlet.ServletException;
 import org.apache.struts.action.ActionServlet;
 import org.apache.struts.action.PlugIn;
 import org.apache.struts.config.ModuleConfig;
 
 public class ObjectPlugIn implements PlugIn {
 
   
   private String className = null;
   private String applicationKey= null;
   private ActionServlet servlet;
     
   public ObjectPlugIn()
   {
   }
 
     /**
      * Intializer. Instantiates a new object and adds it to the application context by key
      *
      * @param servlet The ActionServlet for our application
      * @param config  The ModuleConfig for our owning module
      * @throws ServletException if we cannot configure ourselves correctly
      */
     public void init(ActionServlet servlet, ModuleConfig config)
             throws ServletException {
             
         this.servlet = servlet;
         try {
             Object obj = ObjectFactory.createObect(className);
             servlet.getServletContext().setAttribute(applicationKey,obj);
 
         } catch (Exception e) {
 
             throw new ServletException("Could not initalize Application Context with key= key"+applicationKey +"and className=" +className,e);
         }
 
     }  
   public void destroy() {
     servlet.getServletContext().setAttribute(
       applicationKey, null);
   }    
 
 
   public void setClassName(String className)
   {
     this.className = className;
   }
 
 
   public String getClassName()
   {
     return className;
   }
 
 
   public void setApplicationKey(String applicationKey)
   {
     this.applicationKey = applicationKey;
   }
 
 
   public String getApplicationKey()
   {
     return applicationKey;
   }
 
 
 
 }
