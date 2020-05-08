 package com.voracious.dragons.client.net;
 
 import com.voracious.dragons.client.Game;
 import com.voracious.dragons.client.screens.LoginScreen;
 import com.voracious.dragons.common.ConnectionManager;
 import com.voracious.dragons.common.Message;
 import com.voracious.dragons.common.Packet;
 
 public class AuthenticatePacket implements Packet {
 
     @Override
     public boolean wasCalled(Message message) {
         String msg = message.toString();
         return msg.equals("Hello!") || msg.startsWith("RS:") || msg.startsWith("LS:") || msg.startsWith("LRE:");
     }
 
     @Override
     public void process(Message message, ConnectionManager cm) {
         ClientConnectionManager ccm = (ClientConnectionManager) cm;
         String msg = message.toString();
         
         LoginScreen ls = (LoginScreen) Game.getScreen(LoginScreen.ID);
 
         if(msg.equals("Hello!")){
             String m = "";
             
             if(ls.isRegistering()){
                 m += "R:";
             }else{
                 m += "L:";
             }
             
             m += ls.getUsername() + ":" + ls.getPassword();
             
             ccm.sendMessage(m);
         }else if(msg.startsWith("RS:") || msg.startsWith("LS:")){
             if(!ls.hasLoggedIn()){
                ccm.setSessionId(msg.substring(3));
                 ls.onSuccess();
             }
         }else if(msg.startsWith("LRE:")){
             if(!ls.hasLoggedIn()){
                 ls.onFailure(msg.substring(2));
             }
         }
     }
 
     @Override
     public boolean isString() {
         return true;
     }
 }
