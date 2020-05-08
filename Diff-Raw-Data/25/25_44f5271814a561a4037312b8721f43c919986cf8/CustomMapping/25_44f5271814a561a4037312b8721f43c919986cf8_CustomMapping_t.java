 /* ****************************************************************************
 * CustomMapping.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package examples.javarpc;
 
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openlaszlo.data.helpers.ILaszloRPCAdapter;
 import org.openlaszlo.data.helpers.LaszloRPCAdapter;
 import org.openlaszlo.data.helpers.LaszloTypeMapping;
 import org.openlaszlo.data.helpers.mappings.CastTableToObject;
 
 import examples.javarpc.beans.Organisation;
 import examples.javarpc.beans.User;
 
 /**
  * 
  * This sample illustrates how to use the Adapter Class to map items to 
  * custom classes and custom Objects
  * 
  * 
  * @author sebastianwagner
  *
  */
 public class CustomMapping extends LaszloRPCAdapter implements ILaszloRPCAdapter {
     
     private static final Log mLogger = LogFactory.getLog(CustomMapping.class);
 
     public CustomMapping() {
         //Add Mappings
         
         //regular Java Types
         this.addCustomMappings(new LaszloTypeMapping("java.lang.Long","parseLong"));
         this.addCustomMappings(new LaszloTypeMapping("java.lang.Double","parseDouble"));
         this.addCustomMappings(new LaszloTypeMapping("java.lang.Integer","parseInteger"));
         this.addCustomMappings(new LaszloTypeMapping("java.util.Map","parseToMap"));
         
         //Custom Class
         this.addCustomMappings(new LaszloTypeMapping("examples.javarpc.beans.User","parseToUser"));
     }
     
     /*
      * The Custom Mapping Methods
      * This method is called whenever the Gateway detects that a Methods need an Object of Type java.util.Long
      * 
      */
     public Long parseLong(Object obj) {
         try {
             return Long.valueOf(""+obj).longValue();
         } catch (Exception err) {
             mLogger.error("[parseLong]",err);
         }
         return null;
     }
 
     public Double parseDouble(Object obj) {
         try {
             return Double.valueOf(""+obj).doubleValue();
         } catch (Exception err) {
             mLogger.error("[parseDouble]",err);
         }
         return null;
     }
     public Integer parseInteger(Object obj) {
         try {
             return Integer.valueOf(""+obj).intValue();
         } catch (Exception err) {
             mLogger.error("[parseInteger]",err);
         }
         return null;
     }
     public Map parseToMap(Object obj) {
         try {
             
             if (Hashtable.class.isInstance(obj)) {
                 Hashtable table = (Hashtable) obj;
                 mLogger.debug("Is Table");
                 Map m = new HashMap();
                 for (Iterator iter = table.keySet().iterator();iter.hasNext();) {
                     Object key = iter.next();
                     m.put(key, table.get(key));
                 }
                 
                 return m;
             } else {
                 mLogger.debug("Is No Table: "+obj);
             }
             
         } catch (Exception err) {
             mLogger.error("[parseToMap]",err);
         }
         return null;
     }
     public User parseToUser(Object obj) {
         try {
             
             if (Hashtable.class.isInstance(obj)) {
                 return (User) CastTableToObject.getInstance().castByGivenObject((Hashtable) obj, User.class);
             } else {
                 mLogger.debug("Is No Table: "+obj);
             }
             
         } catch (Exception err) {
             mLogger.error("[parseToUser]",err);
         }
         return null;
     }
 
     /* 
      * This method is called whenever the User invokes a Method
      */
     @Override
     public void onCall(String methodName, Class[] argClasses, Object[] argValues) {
         
         mLogger.debug("onCall: "+methodName);
         // TODO Auto-generated method stub
         super.onCall(methodName, argClasses, argValues);
     }
     
     /* 
      * This method is called whenever the Gateway detects a Type in a Method it could not found in the Custom-Mappings
      */
     @Override
     public void onMappingNotFound(String neededType, Object value) {
         
         mLogger.debug("onMappingNotFound: "+neededType+" value: "+value);
         // TODO Auto-generated method stub
         super.onMappingNotFound(neededType, value);
     }
     
     public Long doTestMethod(Long myLong) {
         try {
             
             mLogger.debug("doTestMethod: "+myLong);
             
             return new Long(myLong+1);
             
         } catch (Exception err) {
             mLogger.error("[doTestMethod]",err);
         }
         return null;
     }
     
     public Long doTestMethodSimple(int myInt) {
         try {
             
             mLogger.debug("doTestMethodSimple: "+myInt);
             
             return new Long(myInt+1);
             
         } catch (Exception err) {
             mLogger.error("[doTestMethodSimple]",err);
         }
         return null;
     }
     
     public Long doTestMethodMultiple(Long myMethod, Integer myInt, Double myDouble) {
         try {
             
             mLogger.debug("doTestMethod: "+myMethod);
             
             return new Long(myMethod+1);
             
         } catch (Exception err) {
             mLogger.error("[doTestMethod]",err);
         }
         return null;
     }
     
     /*
      * current HastTable
      */
     public String passClientObject(Hashtable t) {
         try {
             mLogger.debug("passClientObject "+t);
             return "got hashtable parameter: " + t;
         } catch (Exception err) {
             mLogger.error("passClientObject: ",err);
         }
        return null;
     }
     
     public String passClientMap(Map m) {
         try {
             mLogger.debug("passClientMap "+m);
             return "got map parameter: " + m;
         } catch (Exception err) {
             mLogger.error("passClientMap: ",err);
         }
        return null;
     }
     
     public User getUserObject() {
         try {
             
             User user = new User();
             
             user.setUserid(1L);
             user.setUsername("hans");
             user.setOrganisations(new Organisation());
             user.getOrganisations().setName("Organization");
             user.getOrganisations().setOrganisationId(1L);
             
             mLogger.debug("getUserObject "+user);
             return user;
         } catch (Exception err) {
             mLogger.error("getUserObject: ",err);
         }
        return null;
     }
     
     public User setUserObject(User user) {
         try {
            mLogger.debug("Servlet Context "+this.servletRequest);
             mLogger.debug("User1: "+user);
             mLogger.debug("User2: "+user.getUserid());
             mLogger.debug("User3: "+user.getUsername());
             
             mLogger.debug("User4: "+user.getOrganisations());
             
             if (user.getOrganisations() != null) {
                 mLogger.debug("User5: "+user.getOrganisations().getName());
                 mLogger.debug("User6: "+user.getOrganisations().getOrganisationId());
             }
             
             mLogger.debug("setUserObject "+user);
             return user;
         } catch (Exception err) {
             mLogger.error("setUserObject: ",err);
         }
        return null;
     }
     
     /*
      * This Illustrates how the Mapping works for Objects manually
      */
     public String setUserObjectByTable(Hashtable t) {
         try {
             
             User user = (User) CastTableToObject.getInstance().castByGivenObject(t, User.class);
             
             mLogger.debug("User1: "+user);
             mLogger.debug("User2: "+user.getUserid());
             mLogger.debug("User3: "+user.getUsername());
             
             mLogger.debug("User4: "+user.getOrganisations());
             
             if (user.getOrganisations() != null) {
                 mLogger.debug("User: "+user.getOrganisations().getName());
                 mLogger.debug("User: "+user.getOrganisations().getOrganisationId());
             }
             
             mLogger.debug("setUserObject "+t);
             return "got map parameter: " + t;
         } catch (Exception err) {
             mLogger.error("setUserObject: ",err);
         }
        return null;
     }
     
     
    
    public Map returnMapWithIntKey() {
        Map myMap = new HashMap();
        myMap.put(1, "one1");
        myMap.put(2, "two2");
        return myMap;
    }
 
     
 }
