 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.IPCondition;
 import java.io.Serializable;
 
 public class IPConditionType 
     extends ConditionType
     implements Serializable {
     public ViewCondition newViewCondition() {
         ViewCondition vc = new IPViewCondition();
         vc.setConditionType(this);
 
         return vc;
     }
     
     public ViewCondition newViewCondition(EntitlementCondition ec, ConditionTypeFactory conditionTypeFactory) {
         assert(ec instanceof IPCondition);
         IPCondition ipc = (IPCondition)ec;
 
         IPViewCondition ipvc = (IPViewCondition)newViewCondition();
         ipvc.setStartIp(parseIp(ipc.getStartIp()));
         ipvc.setEndIp(parseIp(ipc.getEndIp()));
 
         return ipvc;
     }
 
     private int[] parseIp(String ipString) {
        ipString = ipString.substring(1, ipString.length()-1);
         String[] ips = ipString.split("\\.");
         assert(ips.length == 4);
 
         int[] ipi = new int[4];
         for (int i = 0; i < 4; i++) {
             ipi[i] = Integer.valueOf(ips[i]);
         }
 
         return ipi;
     }
 
 }
