 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.sapienter.jbilling.server.payment.event;
 
 import java.util.Calendar;
 
 import javax.ejb.EJBException;
 import javax.ejb.MessageDrivenBean;
 import javax.ejb.MessageDrivenContext;
 import javax.jms.MapMessage;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.interfaces.BillingProcessSessionLocal;
 import com.sapienter.jbilling.interfaces.BillingProcessSessionLocalHome;
 import com.sapienter.jbilling.server.process.BillingProcessRunBL;
 
 /*
  * This message bean is not configured using xdoclet.
  * The configuration needs to be done specifically for each installation/scenario
 * using the files jboss-beans.xml and message-driven-beans.xml
  */
 public class ProcessPaymentMDB implements MessageDrivenBean, MessageListener {
     
     private final Logger LOG = Logger.getLogger(ProcessPaymentMDB.class);
 
     public void ejbRemove() throws EJBException {
         LOG.debug("Removing MDB " + this.hashCode());
     }
 
     public void ejbCreate() {
         LOG.debug("Creating MDB " + this.hashCode());
     }
 
     public void setMessageDrivenContext(MessageDrivenContext context)
             throws EJBException {
     }
 
     public void onMessage(Message message) {
         try {
             LOG.debug("Processing message. Processor " + message.getStringProperty("processor") + 
                     " entity " + message.getIntProperty("entityId") + " by " + this.hashCode());
             MapMessage myMessage = (MapMessage) message;
             
             // use a session bean to make sure the processing is done in one transaction
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             BillingProcessSessionLocalHome processHome =
                 (BillingProcessSessionLocalHome) EJBFactory.lookUpLocalHome(
                 BillingProcessSessionLocalHome.class,
                 BillingProcessSessionLocalHome.JNDI_NAME);
             BillingProcessSessionLocal process = processHome.create();
 
             String type = message.getStringProperty("type"); 
             if (type.equals("payment")) {
             	LOG.debug("Now processing asynch payment:" +
             			" processId: " + myMessage.getInt("processId") +
             			" runId:" + myMessage.getInt("runId") +
             			" invoiceId:" + myMessage.getInt("invoiceId"));
                 process.processPayment(
                         (myMessage.getInt("processId") == -1) ? null : myMessage.getInt("processId"),
                         (myMessage.getInt("runId") == -1) ? null : myMessage.getInt("runId"),
                         (myMessage.getInt("invoiceId") == -1) ? null : myMessage.getInt("invoiceId"));
                 LOG.debug("Done");
             } else if (type.equals("ender")) {
                 BillingProcessRunBL run = new BillingProcessRunBL(myMessage.getInt("runId"));
                 run.getEntity().setPaymentFinished(Calendar.getInstance().getTime());
             } else {
                 LOG.error("Can not process message of type " + type);
             }
         } catch (Exception e) {
             LOG.error("Generating payment", e);
         }
     }
 
 }
