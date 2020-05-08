 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 package com.sapienter.jbilling.server.entity;
 
 import java.rmi.RemoteException;
 import java.util.Date;
 
 import javax.ejb.CreateException;
 import javax.ejb.EJBException;
 import javax.ejb.EntityBean;
 import javax.ejb.EntityContext;
 import javax.ejb.RemoveException;
 
import org.apache.log4j.Logger;

 import com.sapienter.jbilling.common.JBCrypto;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.interfaces.SequenceSessionLocal;
 import com.sapienter.jbilling.interfaces.SequenceSessionLocalHome;
 import com.sapienter.jbilling.server.util.Constants;
 
 /**
  * @ejb:bean name="CreditCardEntity" 
  *          display-name="Object representation of the table CREDIT_CARD" 
  *          view-type="local" 
  *          type="CMP" 
  *          local-jndi-name="com/sapienter/jbilling/server/entity/CreditCardEntityLocal"
  *          jndi-name="com/sapienter/jbilling/server/entity/CreditCardEntity"
  *          reentrant="false"
  *          cmp-version="2.x"
  *          primkey-field="id"
  *          schema="credit_card"
  *
  * @ejb:pk class="java.lang.Integer"
  *         generate="false"
  * 
  * @ejb.value-object name="CreditCard" match="matched"
  * 
  * @jboss:table-name "credit_card"
  * 
  * @jboss:create-table create="false"
  * @jboss:remove-table remove="false"
  */
 public abstract class CreditCardEntityBean implements EntityBean {
    
    private static final Logger LOG = Logger.getLogger(CreditCardEntityBean.class);
 
     /**
      * @ejb:create-method view-type="local"
      */
     public Integer ejbCreate(String number, Date expiry) 
             throws CreateException {
 
         Integer newId;
 
         try {
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             SequenceSessionLocalHome generatorHome =
                     (SequenceSessionLocalHome) EJBFactory.lookUpLocalHome(
                     SequenceSessionLocalHome.class,
                     SequenceSessionLocalHome.JNDI_NAME);
 
             SequenceSessionLocal generator = generatorHome.create();
             newId = new Integer(generator.getNextSequenceNumber(
                     Constants.TABLE_CREDIT_CARD));
 
         } catch (Exception e) {
             throw new CreateException(
                     "Problems generating the primary key "
                     + "for the credit_card table");
         }
 
         setId(newId);
         setNumber(number);
         setExpiry(expiry);
         setDeleted(new Integer(0));
         setType(Util.getPaymentMethod(number));
         if (getType() == null) {
             throw new CreateException("Credit card not supported: " + number);
         }
         
         return newId;
     }
     public void ejbPostCreate(String number, Date expiry) {}
 
 
     //  CMP field accessors -----------------------------------------------------
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @ejb:pk-field
      * @ejb.value-object match="matched"
      * @jboss:column-name name="id"
      * @jboss.method-attributes read-only="true"
      */
     public abstract Integer getId();
     public abstract void setId(Integer ruleId);
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.value-object match="matched"
      */
     public String getNumber(){
     	return JBCrypto.getCreditCardCrypto().decrypt(getNumberCrypt());
     }
     
     /**
      * @ejb:interface-method view-type="local"
      */
     public void setNumber(String number){
     	setNumberCrypt(JBCrypto.getCreditCardCrypto().encrypt(number));
         setNumberPlain(number.substring(number.length()-4));
     }
     
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @jboss:column-name name="cc_number"
      * @jboss.method-attributes read-only="true"
      */
     public abstract String getNumberCrypt();
     
     /**
      * @ejb:interface-method view-type="local"
      */
     public abstract void setNumberCrypt(String number);
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @jboss:column-name name="cc_number_plain"
      * @jboss.method-attributes read-only="true"
      */
     public abstract String getNumberPlain();
     
     /**
      * @ejb:interface-method view-type="local"
      */
     public abstract void setNumberPlain(String number);
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @ejb.value-object match="matched"
      * @jboss:column-name name="cc_expiry"
      * @jboss.method-attributes read-only="true"
      */
     public abstract Date getExpiry();
     /**
      * @ejb:interface-method view-type="local"
      */
     public abstract void setExpiry(Date expiry);
     
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.value-object match="matched"
      */
     public String getName(){
     	String crypted = getNameCrypt();
     	return JBCrypto.getCreditCardCrypto().decrypt(crypted);
     }
     
     /**
      * @ejb:interface-method view-type="local"
      */
     public void setName(String name){
     	String crypted = JBCrypto.getCreditCardCrypto().encrypt(name);
     	setNameCrypt(crypted);
        // validate that the saved name is retrivable and valid
        LOG.debug("Credit card named saved as " + crypted);
        try {
            if (!getName().equals(name)) {
                LOG.error("The credit card name " + name + " was wrongly encrypted to " + crypted);
            }
        } catch (Exception e) {
            LOG.error("The credit card name " + name + " was wrongly encrypted to " + crypted);
        }
     }
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @jboss:column-name name="name"
      * @jboss.method-attributes read-only="true"
      */
     public abstract String getNameCrypt();
 
     /**
      * @ejb:interface-method view-type="local"
      */
     public abstract void setNameCrypt(String name);
     
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @ejb.value-object match="matched"
      * @jboss:column-name name="cc_type"
      * @jboss.method-attributes read-only="true"
      */
     public abstract Integer getType();
     public abstract void setType(Integer type);
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @ejb.value-object match="matched"
      * @jboss:column-name name="deleted"
      * @jboss.method-attributes read-only="true"
      */
     public abstract Integer getDeleted();
     /**
      * @ejb:interface-method view-type="local"
      */
     public abstract void setDeleted(Integer deleted);
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb:persistent-field
      * @ejb.value-object match="matched"
      * @jboss:column-name name="security_code"
      * @jboss.method-attributes read-only="true"
      */
     public abstract Integer getSecurityCode();
     /**
      * @ejb:interface-method view-type="local"
      */
     public abstract void setSecurityCode(Integer number);
 
     /* (non-Javadoc)
      * @see javax.ejb.EntityBean#ejbActivate()
      */
     public void ejbActivate() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.EntityBean#ejbLoad()
      */
     public void ejbLoad() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.EntityBean#ejbPassivate()
      */
     public void ejbPassivate() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.EntityBean#ejbRemove()
      */
     public void ejbRemove()
         throws RemoveException, EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.EntityBean#ejbStore()
      */
     public void ejbStore() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
      */
     public void setEntityContext(EntityContext arg0)
         throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.EntityBean#unsetEntityContext()
      */
     public void unsetEntityContext() throws EJBException, RemoteException {
     }
 
 }
