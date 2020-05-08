 package org.jinglenodes.stats;
 
 import org.jinglenodes.component.SIPGatewayApplication;
 import org.jinglenodes.credit.CallKiller;
 import org.jinglenodes.jingle.Reason;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 5/24/12
  * Time: 2:51 PM
  */
 public class SwitjiAdminImpl implements SwitjiAdmin {
 
     private CallKiller callKiller;
     private SIPGatewayApplication sipApplication;
 
     @Override
     public boolean killSession(String sessionId) {
         return callKiller.immediateKill(sessionId, new Reason(Reason.Type.general_error));
     }
 
     @Override
     public int killAll() {
         return callKiller.killAll(new Reason(Reason.Type.general_error));
     }
 }
