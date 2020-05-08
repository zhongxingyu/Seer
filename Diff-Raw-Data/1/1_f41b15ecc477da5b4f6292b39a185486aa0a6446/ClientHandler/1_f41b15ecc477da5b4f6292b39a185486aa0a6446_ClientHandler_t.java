 package com.yanchuanli.games.pokr.server;
 
 import com.yanchuanli.games.pokr.basic.Card;
 import com.yanchuanli.games.pokr.util.Config;
 import com.yanchuanli.games.pokr.util.Memory;
 import com.yanchuanli.games.pokr.util.Util;
 import org.apache.log4j.Logger;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IoSession;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: Yanchuan Li
  * Date: 5/27/12
  * Email: mail@yanchuanli.com
  */
 public class ClientHandler extends IoHandlerAdapter {
 
     private static Logger log = Logger.getLogger(ClientHandler.class);
 
     public ClientHandler() {
 
     }
 
     @Override
     public void sessionCreated(IoSession session) throws Exception {
         super.sessionCreated(session);
         Memory.sessionsOnClient.put(String.valueOf(session.getId()), session);
 //        log.info("sessionCreated ...");
     }
 
     @Override
     public void messageReceived(IoSession session, Object message) throws Exception {
         super.messageReceived(session, message);
         if (message instanceof IoBuffer) {
             IoBuffer buffer = (IoBuffer) message;
             List<Map<Integer, String>> list = Util.ioBufferToString(buffer);
             for (Map<Integer, String> map : list) {
                 for (Integer key : map.keySet()) {
 
                     String info = map.get(key);
                     log.debug("[messageReceived] status code: [" + key + "] " + info);
                     switch (key) {
                         case Config.TYPE_ACTION_INGAME:
                             String[] infos = info.split(",");
                             String username = infos[1];
                             log.debug(username + ":" + infos[2]);
                             break;
                         case Config.TYPE_HOLE_INGAME:
                             infos = info.split(",");
                             username = infos[1];
                             String[] pokers = infos[2].split("_");
                             String debuginfo = "";
                             for (String s : pokers) {
                                 Card c = new Card(Integer.parseInt(s));
                                 debuginfo = debuginfo + " " + c.toChineseString();
                             }
                             log.debug(username + ":" + debuginfo);
                             break;
                         case Config.TYPE_USER_INGAME:
 
                             break;
                         case Config.TYPE_CARD_INGAME:
                             infos = info.split(",");
                             pokers = infos[0].split("_");
                             debuginfo = "";
                             for (String s : pokers) {
                                 Card c = new Card(Integer.parseInt(s));
                                 debuginfo = debuginfo + " " + c.toChineseString();
                             }
                             log.debug("ontable" + ":" + debuginfo);
                            break;
                     }
                 }
             }
         } else {
             log.info("[messageReceived]illegal");
         }
     }
 
     @Override
     public void sessionClosed(IoSession session) throws Exception {
 //        log.info("sessionClosed");
         super.sessionClosed(session);
         Memory.sessionsOnClient.remove(String.valueOf(session.getId()));
     }
 }
