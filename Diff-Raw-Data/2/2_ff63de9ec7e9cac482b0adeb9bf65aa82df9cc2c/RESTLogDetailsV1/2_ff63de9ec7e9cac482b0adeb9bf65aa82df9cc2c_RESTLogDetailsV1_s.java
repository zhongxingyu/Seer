 package org.jboss.pressgang.ccms.rest.v1.entities.base;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 
 public class RESTLogDetailsV1
 {
     public static final byte MINOR_CHANGE_FLAG_BIT = 0x01;
     public static final byte MAJOR_CHANGE_FLAG_BIT = 0x02;
     
     public static final String MESSAGE_NAME = "message";
     public static final String FLAG_NAME = "flag";
     public static final String USERNAME_NAME = "username";
 
     private String message = null;
     private Integer flag = null;
     private RESTUserV1 user = null;
     private Date date = null;
     
     /**
      * Maintains a list of the database fields that have been specifically set
      * on this object. This allows us to distinguish them from those that are
      * just null by default
      */
     private List<String> configuredParameters = null;
 
     public String getMessage()
     {
         return message;
     }
 
     public void setMessage(final String message)
     {
         this.message = message;
     }
     
     public void explicitSetMessage(final String message)
     {
         this.message = message;
         this.setParameterToConfigured(MESSAGE_NAME);
     }
 
     public Integer getFlag()
     {
         return flag;
     }
 
     public void setFlag(final Integer flag)
     {
         this.flag = flag;
     }
     
     public void explicitSetFlag(final Integer flag)
     {
         this.flag = flag;
         this.setParameterToConfigured(FLAG_NAME);
     }
 
     public RESTUserV1 getUser()
     {
         return user;
     }
 
     public void setUser(final RESTUserV1 user)
     {
         this.user = user;
     }
     
     public void explicitSetUser(final RESTUserV1 user)
     {
         this.user = user;
         this.setParameterToConfigured(USERNAME_NAME);
     }
     
     public Date getDate()
     {
         return date;
     }
 
     public void setDate(Date date)
     {
         this.date = date;
     }
 
     public RESTLogDetailsV1 clone(final boolean deepCopy)
     {
         final RESTLogDetailsV1 retValue = new RESTLogDetailsV1();
         
         this.cloneInto(retValue, deepCopy);
         
         return retValue;
     }
     
     public void cloneInto(final RESTLogDetailsV1 clone, final boolean deepCopy)
     {
         clone.setFlag(flag);
         clone.setMessage(message);
        clone.setUser(user.clone(deepCopy));
         clone.setDate((Date) (date == null ? null : date.clone()));
     }
 
     public List<String> getConfiguredParameters()
     {
         return configuredParameters;
     }
 
     public void setConfiguredParameters(final List<String> configuredParameters)
     {
         this.configuredParameters = configuredParameters;
     }
     
     /**
      * This is a convenience method that adds a value to the configuredParameters collection
      * @param parameter The parameter to specify as configured
      */
     protected void setParameterToConfigured(final String parameter)
     {
         if (configuredParameters == null)
             configuredParameters = new ArrayList<String>();
         if (!configuredParameters.contains(parameter))
             configuredParameters.add(parameter);
     }
     
     public boolean hasParameterSet(final String parameter)
     {
         return getConfiguredParameters() != null && getConfiguredParameters().contains(parameter);
     }
 }
