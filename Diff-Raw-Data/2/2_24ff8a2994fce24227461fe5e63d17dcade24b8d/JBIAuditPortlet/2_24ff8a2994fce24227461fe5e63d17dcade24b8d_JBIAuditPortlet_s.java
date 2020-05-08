 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.console;
 
 import org.apache.servicemix.JbiConstants;
 import org.apache.servicemix.jbi.audit.AuditorMBean;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.messaging.MessageExchangeSupport;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.RenderRequest;
 
 import java.net.URI;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class JBIAuditPortlet extends ServiceMixPortlet {
     
     protected int page = 0;
 
     public static class ExchangeInfo {
         private String id;
         private String date;
         private String mep;
         private String status;
         
         /**
          * @return Returns the dateStamp.
          */
         public String getDate() {
             return date;
         }
         /**
          * @param dateStamp The dateStamp to set.
          */
         public void setDate(String dateStamp) {
             this.date = dateStamp;
         }
         /**
          * @return Returns the status.
          */
         public String getStatus() {
             return status;
         }
         /**
          * @param status The status to set.
          */
         public void setStatus(String status) {
             this.status = status;
         }
         /**
          * @return Returns the id.
          */
         public String getId() {
             return id;
         }
         /**
          * @param id The id to set.
          */
         public void setId(String id) {
             this.id = id;
         }
         /**
          * @return Returns the mep.
          */
         public String getMep() {
             return mep;
         }
         /**
          * @param mep The mep to set.
          */
         public void setMep(String mep) {
             this.mep = mep;
         }
     }
     
     protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
         System.err.println(actionRequest.getParameterMap());
         if (actionRequest.getParameter("view") != null) {
             page = Integer.parseInt(actionRequest.getParameter("view"));
         }
     }
 
     protected void fillViewRequest(RenderRequest request) throws Exception {
         AuditorMBean auditor = getJdbcAuditor();
         int count = auditor.getExchangeCount();
         request.setAttribute("count", new Integer(count));
         request.setAttribute("page", new Integer(page));
        MessageExchange[] exchanges = auditor.getExchanges(page * 10, Math.min((page + 1) * 10, count));
         request.setAttribute("exchanges", prepare(exchanges));
         super.fillViewRequest(request);
     }
 
     private ExchangeInfo[] prepare(MessageExchange[] exchanges) throws Exception {
         ExchangeInfo[] infos = new ExchangeInfo[exchanges.length];
         for (int i = 0; i < infos.length; i++) {
             infos[i] = new ExchangeInfo();
             infos[i].id = exchanges[i].getExchangeId();
             infos[i].mep = getMep(exchanges[i]);
             infos[i].status = exchanges[i].getStatus().toString();
             Object c = exchanges[i].getProperty(JbiConstants.DATESTAMP_PROPERTY_NAME);
             if (c instanceof Calendar) {
                 infos[i].date = DateFormat.getDateTimeInstance().format(((Calendar) c).getTime());
             } else if (c instanceof Date) {
                 infos[i].date = DateFormat.getDateTimeInstance().format((Date) c);
             }
         }
         return infos;
     }
     
     private String getMep(MessageExchange exchange) {
         URI uri = exchange.getPattern();
         if (MessageExchangeSupport.IN_ONLY.equals(uri)) {
             return "In Only";
         } else if (MessageExchangeSupport.IN_OPTIONAL_OUT.equals(uri)) {
             return "In Opt Out";
         } else if (MessageExchangeSupport.IN_OUT.equals(uri)) {
             return "In Out";
         } else if (MessageExchangeSupport.ROBUST_IN_ONLY.equals(uri)) {
             return "Robust In Only";
         } else {
             return uri.toString();
         }
     }
 
     private String prepareContent(NormalizedMessage msg) throws Exception {
         if (msg != null) {
             SourceTransformer st = new SourceTransformer();
             String s = st.contentToString(msg);
             if (s != null && s.length() > 30) {
                 return s.substring(0, 30) + "...";
             } else {
                 return s;
             }
         } else {
             return null;
         }
     }
 
 }
