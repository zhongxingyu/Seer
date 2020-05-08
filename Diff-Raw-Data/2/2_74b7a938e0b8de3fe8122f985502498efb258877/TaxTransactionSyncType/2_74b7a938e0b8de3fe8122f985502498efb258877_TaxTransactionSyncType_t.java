 /**
  * Mule Vertex Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.vertex;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * @author Pablo Diez * @since 14/03/2012
  */
 public enum TaxTransactionSyncType
 {
 
     REVERSAL_TYPE(),
     TRANSACTION_EXISTS_TYPE(),
     ACCRUAL_SYNC_TYPE(),
     AP_INVOICE_TYPE("getAPInvoiceSync"),
    AR_BILLING_TYPE("getARBillingSync"),
     DELETE_TYPE(),
     ROLLBACK_TYPE();
 
     private String customAttributeName = null;
 
     private TaxTransactionSyncType(String responseGetter)
     {
         this.customAttributeName = responseGetter;
     }
 
     private TaxTransactionSyncType()
     {
     }
 
     public String getRequestGetter()
     {
         String res = "get" + StringUtils.capitalize(getCammelType()) + "Request";
         return this.customAttributeName == null ? res : this.customAttributeName + "Request";
     }
 
     public String getResponseGetter()
     {
         String res = "get" + StringUtils.capitalize(getCammelType()) + "Response";
         return this.customAttributeName == null ? res : this.customAttributeName + "Response";
     }
 
     public String getCammelType()
     {
         String[] split = StringUtils.split(this.toString(), '_');
         final StringBuilder cammel = new StringBuilder();
         cammel.append(split[0].toLowerCase());
         for (int i = 1; i < split.length - 1; i++)
         {
             cammel.append(StringUtils.capitalize(StringUtils.lowerCase(split[i])));
         }
         return cammel.toString();
     }
 
     public String getRequestAttr()
     {
         return this.customAttributeName == null ? getCammelType() + "Request" : StringUtils.removeStart(
             customAttributeName, "get") + "Request";
     }
 
 }
