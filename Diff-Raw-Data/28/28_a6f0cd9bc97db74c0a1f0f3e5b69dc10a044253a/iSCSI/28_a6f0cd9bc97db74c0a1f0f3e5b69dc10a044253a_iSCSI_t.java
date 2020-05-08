 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api.helpers;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import net.wgr.xenmaster.api.Host;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.controller.Controller;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 16, 2011
  * @author double-u
  */
 public class iSCSI {
 
     protected InetAddress target;
     protected String targetIQN, localIQN;
     protected String user, password;
     protected int port = 3260;
     protected boolean useDiscoveryNumber;
     protected Type type;
     protected Integer[] luns;
 
     public static enum Type {
 
         iSCSI, LVMoISCSI, EXToISCSI
     }
 
     public Map<String, String> toDeviceConfig() {
         HashMap<String, String> map = new HashMap<>();
         map.put("target", target.getCanonicalHostName());
         if (targetIQN != null) {
             map.put("targetIQN", targetIQN);
         }
         if (localIQN != null) {
             map.put("localIQN", localIQN);
         } else {
             map.put("localIQN", "iqn.2011-09.net.wgr.xenmaster:" + Controller.getSession().getThisHost().getNameLabel());
         }
         if (user != null && password != null) {
             map.put("chapuser", user);
             map.put("chappassword", password);
         }
         if (luns != null) {
             map.put("LUNid", StringUtils.join(luns, ','));
         }
         map.put("port", "" + port);
         if (useDiscoveryNumber) {
             map.put("usediscoverynumber", Boolean.toString(useDiscoveryNumber));
         }
         return map;
     }
 
     public List<String> getAvailableIQNs(Host host) throws BadAPICallException {
         if (target == null) {
             throw new IllegalArgumentException("Target is not set");
         }
         try {
             Controller.dispatch("SR.create", host.getReference(), this.toDeviceConfig(), "0", "", "", "iSCSI", "user", true, new HashMap<>());
         } catch (BadAPICallException ex) {
             if (ex.getErrorName().equals("SR_BACKEND_FAILURE_96")) {
                 // TODO
             } else {
                 throw ex;
             }
         }
         return null;
     }
 
     public String getLocalIQN() {
         return localIQN;
     }
 
     public void setLocalIQN(String localIQN) {
         this.localIQN = localIQN;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Integer[] getLUNs() {
         return luns;
     }
 
     public void setLUNs(Integer[] luns) {
        this.luns = luns.clone();
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public InetAddress getTarget() {
         return target;
     }
 
     public void setTarget(InetAddress target) {
         this.target = target;
     }
 
     public String getTargetIQN() {
         return targetIQN;
     }
 
     public void setTargetIQN(String targetIQN) {
         this.targetIQN = targetIQN;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public boolean isUseDiscoveryNumber() {
         return useDiscoveryNumber;
     }
 
     public void setUseDiscoveryNumber(boolean useDiscoveryNumber) {
         this.useDiscoveryNumber = useDiscoveryNumber;
     }
 
     public String getUser() {
         return user;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     protected boolean isReachable() {
         if (target != null) {
             try {
                 return target.isReachable(500);
             } catch (IOException ex) {
                 Logger.getLogger(getClass()).error(ex);
             }
         }
 
         return false;
     }
 }
