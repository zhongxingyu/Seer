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
 package com.sapienter.jbilling.server.mediation.task;
 
 import java.util.Vector;
 
 import org.drools.RuleBase;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.mediation.Record;
 import com.sapienter.jbilling.server.order.OrderLineDTOEx;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.user.UserBL;
 
 public class RulesMediationTask extends PluggableTask implements
         IMediationProcess {
 
     private Vector<Record> records = null;
     ProcessManager manager = null;
     
     
     public Integer getUserId() {
         return manager.getUserId();
     }
     
     public Integer getCurrencyId() {
         return manager.getCurrencyId();
     }
     
     public Vector<OrderLineDTOEx> process(Vector<Record> records, String configurationName) 
             throws TaskException {
         RuleBase ruleBase;
         try {
             ruleBase = readRule();
         } catch (Exception e) {
             throw new TaskException(e);
         }
         session = ruleBase.newStatefulSession();
         Vector<Object> rulesMemoryContext = new Vector<Object>();
         for (Record record: records) {
             for (PricingField field: record.getFields()) {
                 rulesMemoryContext.add(field);
             }
         }
         
         this.records = records;
         
         manager = new ProcessManager(configurationName);
         session.setGlobal("mediationManager", manager);
         // then execute the rules
         executeStatefulRules(session, rulesMemoryContext);
         
         return manager.getLines();
     }
     
     public class ProcessManager {
         private Vector <OrderLineDTOEx> lines = null;
         private Integer userId = null;
         private Integer currencyId = null;
         private final String configurationName;
         
         public ProcessManager(String configurationName) {
             this.configurationName = configurationName;
             lines = new Vector<OrderLineDTOEx>();
         }
         
         public String getConfigurationName() {
             return configurationName;
         }
         
         public int getTotalRecords() {
             return records.size();
         }
         
         public Vector<OrderLineDTOEx> getLines() {
             return lines;
         }
         
         public void addLine(Integer itemId, Integer quantity) {
             OrderLineDTOEx line =  new OrderLineDTOEx();
             line.setItemId(itemId);
             line.setQuantity(quantity);
             lines.add(line);
         }
         
         public Integer getUserId() {
             return userId;
         }
         
         public Integer getCurrencyId() {
             return currencyId;
         }
         
         public void setUserFromUsername(String username) 
                 throws TaskException {
             try {
                 UserBL user = new UserBL(username, getEntityId());
                 userId = user.getEntity().getUserId();
             } catch (Exception e) {
                 throw new TaskException(e);
             } 
         }
 
         public void setUserFromCustomField(Integer typeId, String value) throws TaskException {
             try {
                 UserBL user = new UserBL();
                 CachedRowSet set = user.getByCustomField(getEntityId(), typeId, value);
                 if (set.next()) {
                     userId = set.getInt(1);
                     if (set.next()) {
                         throw new TaskException("Too many users found for type " + typeId + " value " + value);
                     }
                     set.close();
                 } else {
                     throw new TaskException("User not found for type " + typeId + " value " + value);
                 }
             } catch (Exception e) {
                 throw new TaskException(e);
             }
         }
         
         public void setUserFromId(Integer userId) throws TaskException {
             try {
                 UserBL user = new UserBL(userId);
                 this.userId = userId;
             } catch (Exception e) {
                 throw new TaskException(e);
             } 
         }
         
         public void setCurrencyId(Integer currencyId) {
             this.currencyId = currencyId;
         }
 
         public void updateField(PricingField toUpdate) throws TaskException {
             updateObject(toUpdate, toUpdate);
         }
     }
 }
