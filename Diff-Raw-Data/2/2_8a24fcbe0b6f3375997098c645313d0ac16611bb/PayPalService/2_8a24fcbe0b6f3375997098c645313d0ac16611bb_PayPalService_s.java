 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.payment.service;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.FlushMode;
 import org.hibernate.HibernateException;
 import org.hibernate.ObjectNotFoundException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 
 import com.celements.payment.raw.PayPal;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.store.XWikiHibernateBaseStore;
 import com.xpn.xwiki.store.XWikiHibernateStore;
 
 @Component
 public class PayPalService implements IPayPalService {
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(PayPalService.class);
 
   @Requirement
   Execution execution;
 
   private XWikiContext getContext() {
     return (XWikiContext)execution.getContext().getProperty("xwikicontext");
   }
 
   public void storePayPalObject(final PayPal payPalObj, boolean bTransaction
       ) throws XWikiException {
     getStore().executeWrite(getContext(), bTransaction,
         new XWikiHibernateBaseStore.HibernateCallback<Object>() {
         public Object doInHibernate(Session session) throws HibernateException {
           LOGGER.debug("in doInHibernate with session: " + session);
           session.saveOrUpdate(payPalObj);
           LOGGER.debug("after saveOrUpdate in doInHibernate with session: " + session);
           return null;
         }
       });
   }
 
 //  /**
 //   * {@inheritDoc}
 //   */
 //  public List<StatusNode> loadPayPalObject(final long id,
 //      boolean bTransaction, XWikiContext context) throws XWikiException {
 //      return getStore(context).executeRead(context, bTransaction,
 //          new HibernateCallback<List<StatusNode>>() {
 //          @SuppressWarnings("unchecked")
 //          public List<StatusNode> doInHibernate(Session session
 //              ) throws HibernateException {
 //              try {
 //                  return session.createCriteria(StatusNode.class
 //                      ).add(Restrictions.eq("id.docId", Long.valueOf(id))
 //                      ).addOrder(Order.desc("id.version1")
 //                      ).addOrder(Order.desc("id.version2")
 //                     ).list();
 //              } catch (IllegalArgumentException ex) {
 //                  // This happens when the database has wrong values...
 //                  mLogger.warn("Invalid status protocol for document " + id);
 //                  return Collections.emptyList();
 //              }
 //          }
 //      });
 //  }
 
   public PayPal loadPayPalObject(final String txnId) throws XWikiException {
     boolean bTransaction = true;
 
     PayPal payPalObj = new PayPal();
     payPalObj.setTxn_id(txnId);
 
     getStore().checkHibernate(getContext());
 
     SessionFactory sfactory = getStore().injectCustomMappingsInSessionFactory(
         getContext());
     bTransaction = bTransaction && getStore().beginTransaction(sfactory, false,
         getContext());
     Session session = getStore().getSession(getContext());
     session.setFlushMode(FlushMode.MANUAL);
 
     try {
        session.load(payPalObj, new Long(payPalObj.getTxn_id()));
     } catch (ObjectNotFoundException e) {
       // No paypall object in store
     }
     return payPalObj;
   }
 
   XWikiHibernateStore getStore() {
     return getContext().getWiki().getHibernateStore();
   }
 
 }
