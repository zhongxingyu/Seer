 package net.cyklotron.cms;
 
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 import org.objectledge.parameters.AmbiguousParameterException;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
import org.objectledge.utils.StackTrace;
 
 /**
  * A data object used to encapsulate component runtime data.
  *
  * @author <a href="mailto:zwierzem@caltha.pl">Damian Gajda</a>
  */
 public class CmsComponentData
 {
     // services and utility objects
     private CmsData cmsData;
     
     // attributes
     private String instanceName;
     private String app;
     private String clazz;
     private String variant;
     private Parameters configuration;
     private boolean global;
     private String configurationPrefix;
    private List<Map<String, String>> errorMessages;
 
     // initialization ////////////////////////////////////////////////////////
     CmsComponentData(CmsData cmsData, String instanceName, String app, String clazz)
     throws ProcessingException
     {
         this.cmsData = cmsData;
         this.instanceName = instanceName;
 
         Parameters config;
         global = cmsData.getSystemPreferences().isDefined("component."+instanceName+".class");
         if(global)
         {
             config = cmsData.getSystemPreferences();
         }
         else
         {
             config = cmsData.getPreferences();
         }
 
         // get app ////////
         if(app == null)
         {
             app = getParameter(config,"component."+instanceName+".app",null);
         }
         if(app == null || app.length() == 0)
         {
             error("Component app not configured", null);
         }
         else
         {
             this.app = app;
         }
 
         // get clazz ////////
         if(clazz == null)
         {
             clazz = getParameter(config, "component."+instanceName+".class",null);
         }
         if(clazz == null || clazz.length() == 0)
         {
             error("Component class not configured", null);
         }
         else
         {
             this.clazz = clazz.replace(',','.'); // WARN: Hack for old config compatibility
         }
 
         // get variant & configuration ////////
         if(app != null && clazz != null)
         {
             // 1.? Get the component variant
             //variant = config.get("component."+instanceName+".variant."+app+"."+
             //clazz.replace(',','.'),"Default");
             variant = getParameter(config,
                 "component."+instanceName+".variant."+this.app+"."+this.clazz, "Default");
             // 1.5. Get component configuration subset
             configurationPrefix = "component."+instanceName+".config."+this.app+"."+this.clazz+".";
             configuration = new SingleValueParameters(config.getChild(configurationPrefix));
         }        
     }
 
     // public interface    // ///////////////////////////////////////////////////////
 
     public void error(String message, Throwable e)
     throws ProcessingException
     {
         message = message + ", component instance: " + instanceName;
         if(app != null)
         {
             message = message + ", component app: " + app;
         }
         if(clazz != null)
         {
             message = message + ", component class: " + clazz;
         }
         if(cmsData.getNode() != null)
         {
             message = message + ", node id: " + cmsData.getNode().getIdString();
         }
         if(cmsData.getSite() != null)
         {
             message = message + ", site: " + cmsData.getSite().getName();
         }
         if(e != null)
         {
             cmsData.getLog().error(message, e);
             message = message + ", exception: " + e.toString(); 
         }
         else
         {
             cmsData.getLog().error(message);
         }
 
         if(errorMessages == null)
         {
            errorMessages = new ArrayList<Map<String, String>>();
         }
        Map<String,String> msg = new HashMap<String,String>(2);
        msg.put("message", message);
        msg.put("trace", new StackTrace(e).toString());
        errorMessages.add(msg);
     }
 
     /** Getter for property instanceName.
      * @return Value of property instanceName.
      *
      */
     public String getInstanceName()
     {
         return instanceName;
     }
 
     /** Getter for property app.
      * @return Value of property app.
      *
      */
     public String getApp()
     {
         return app;
     }
 
     /** Getter for property clazz.
      * @return Value of property clazz.
      *
      */
     public String getClazz()
     {
         return clazz;
     }
 
     /** Getter for property variant.
      * @return Value of property variant.
      *
      */
     public String getVariant()
     {
         return variant;
     }
     
     /** Checkcs if the component is configured at the system level.
      * @return true if the component is configured at the system level.
      */
     public boolean isGlobal()
     {
         return global;
     }
 
     /** Getter for property configuration.
      * @return Value of property configuration.
      *
      */
     public Parameters getConfiguration()
     {
         return configuration;
     }
 
     /** Getter for property errorMessages.
      * @return Value of property errorMessages.
      *
      */
 
     /**
      * Returns the prefix of the component configuration in the global namespace.
      * 
      * @return the configuration prefix.
      */
     public String getConfigurationPrefix()
     {
         return configurationPrefix;
     }
     
    public List<Map<String, String>> getErrorMessages()
     {
         return errorMessages;
     }
     // implementation //////////////////////////////////////////////////////////////////////////////
     
 
     // this is to avoid problem with multiple parameter definitions
     // we should explain how it could be possible to set more than one
     // parameter value, probably there is somewhere 'add' method used instead of 'set'
     public static String getParameter(Parameters parameters, String name, String defaultValue)
     {
         String value = defaultValue;
         try
         {
             value = parameters.get(name,defaultValue);
         }
         catch(AmbiguousParameterException e)
         {
             String[] values = parameters.getStrings(name);
             if(values.length == 1)
             {
                 value = values[0];
             }
             if(values.length > 1)
             {
                 // sort parameter values to be deterministic
                 ArrayList<String> list = new ArrayList<String>();
                 for(String v:values)
                 {
                     list.add(v);
                 }
                 Collections.sort(list);
                 value = list.get(0);
             }
         }        
         return value;
     }
 }
