 /*
  * SafeOnline project.
  *
  * Copyright 2006-2009 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 package net.link.safeonline.attribute;
 
 import com.lyndir.lhunath.opal.system.util.MetaObject;
 import java.io.Serializable;
 import java.util.List;
 
 
 /**
  * <h2>{@link Compound}</h2>
  * <p/>
  * <p> <i>Nov 29, 2010</i>
  * <p/>
  * Compound Attribute Value class. </p>
  *
  * @author wvdhaute
  */
 public class Compound extends MetaObject implements Serializable {
 
     private final List<? extends AttributeSDK<?>> members;
 
     public Compound(List<? extends AttributeSDK<?>> members) {
 
         this.members = members;
     }
 
     /**
      * @return list of this compound value's members
      */
    public List<? extends AttributeSDK<?>> getMembers() {
 
         return members;
     }
 
     /**
      * @param attributeName attribute name of member attribute we are fetching.
      *
      * @return specific member with specified attribute name
      */
     public <T extends Serializable> AttributeSDK<T> findMember(String attributeName) {
 
         for (AttributeSDK<?> member : members) {
             if (member.getName().equals( attributeName ))
                 return (AttributeSDK<T>) member;
         }
 
         return null;
     }
 }
 
