 
 package com.beem.project.beem.service;
 
 import static com.beem.project.beem.utils.CommonUtils.d;
 import static com.beem.project.beem.utils.CommonUtils.e;
 import static com.beem.project.beem.utils.CommonUtils.getJIDWithoutResource;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.XMPPException;
 
 import android.os.RemoteException;
 import ca.uwaterloo.crysp.otr.iface.OTRCallbacks;
 import ca.uwaterloo.crysp.otr.iface.OTRContext;
 import ca.uwaterloo.crysp.otr.iface.OTRInterface;
 import ca.uwaterloo.crysp.otr.iface.Policy;
 import ca.uwaterloo.crysp.otr.iface.StringTLV;
 
 public class OTRChatAdapter extends ChatAdapter {
 
   String ownerJid;
 
   public OTRChatAdapter(Chat chat, String ownerJid) {
     super(chat);
     this.ownerJid = getJIDWithoutResource(ownerJid);
   }
 
   @Override
   MsgListener createMessageListener() {
     return new OTRMessageListener(this);
   }
 
   @Override
   public void sendMessage(Message message) throws RemoteException {
 
     DefaultOTRCallbacks cb = new DefaultOTRCallbacks(this);
     OTRManager otrManager = OTRManager.getInstance();
     try {
       d("OTRChatAdapter.sendMessage (raw message - will send to OTR processor)", message.getBody());
       otrManager.getInterface().messageSending(ownerJid, message.getProtocol(),
           getJIDWithoutResource(this.getParticipant().getJID()), message.getBody(), null,
           Policy.FRAGMENT_SEND_ALL, cb);
       // TODO: May not be a good idea to queue before sending
       mMessages.add(message);
     } catch (Exception e) {
       d("OTRChatAdapter.sendMessage", message, e.getMessage(), "Sending message in plaintext");
       sendOtrMessage(message);
     }
   }
 
   public void sendOtrMessage(Message message) {
     org.jivesoftware.smack.packet.Message send = new org.jivesoftware.smack.packet.Message();
     send.setTo(message.getTo());
     d("OTRChatAdapter.sendOtrMessage", message.getTo(), message.getBody());
 
     send.setBody(message.getBody());
     send.setThread(message.getThread());
     send.setSubject(message.getSubject());
     send.setType(org.jivesoftware.smack.packet.Message.Type.chat);
     // TODO gerer les messages contenant des XMPPError
     // send.set
     try {
       mAdaptee.sendMessage(send);
     } catch (XMPPException e) {
       e("OTRChatAdapter.sendMessage", e, "Could not send message");
     }
   }
 
   class OTRMessageListener extends MsgListener {
 
     private OTRChatAdapter sender;
 
     public OTRMessageListener(OTRChatAdapter sender) {
       this.sender = sender;
     }
 
     @Override
     public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
 
       try {
         if (message.getBody() != null) {
           d("OTRMessageListener.processMessage (raw message - will send to OTR processor)",
               message.getBody());
           OTRCallbacks cb = new DefaultOTRCallbacks(sender);
           OTRInterface otrInterface = OTRManager.getInstance().getInterface();
           StringTLV stlv = otrInterface.messageReceiving(getJIDWithoutResource(message.getTo()),
               "xmpp", getJIDWithoutResource(message.getFrom()), message.getBody(), cb);
           if (stlv != null) {
             d("OTRMessageListener.processMessage (processed message for user)", stlv.msg);
             Message plainTextMessage = new Message(message);
             plainTextMessage.setBody(stlv.msg);
             plainTextMessage.setType(stlv.type);
             super.processMessage(chat, plainTextMessage);
           } else {
             d("OTRMessageListener.processMessage (processed message not for user)",
                 "OTR may have already responded");
           }
         } else {
           d("OTRMessageListener.processMessage (raw message - null message. Will be ignored)");
         }
       } catch (Exception e) {
         e("OTRChatAdapter.processMessage", e, "Could not receive message");
         Message plainTextMessage = new Message(message);
         plainTextMessage.setBody("Unable to receive message from user. [" + e.getMessage() + "]");
         plainTextMessage.setType(Message.MSG_TYPE_ERROR);
         super.processMessage(chat, plainTextMessage);
       }
     }
   }
 
   private class DefaultOTRCallbacks extends AbstractOTRCallbacks {
 
     private OTRChatAdapter otrChatAdapter;
 
     public DefaultOTRCallbacks(OTRChatAdapter adapter) {
       this.otrChatAdapter = adapter;
     }
 
     @Override
     public String errorMessage(OTRContext context, int err_code) {
       handleMsgEvent(err_code, context, null);
       return null;
     }
 
     @Override
     public void handleMsgEvent(int msg_event, OTRContext context, String message) {
       switch (msg_event) {
         case OTRCallbacks.OTRL_ERRCODE_MSG_NOT_IN_PRIVATE:
          try {
            otrChatAdapter.addMessage(new Message(this.otrChatAdapter.getParticipant().getJID(),
                Message.MSG_TYPE_ERROR));
          } catch (RemoteException e) {
            e("DefaultOTRCallbacks.handleMsgEvent", e, msg_event, context, message);
          }
           break;
       }
     }
 
     @Override
     public void injectMessage(String accName, String prot, String rec, String msg) {
       d("DefaultOTRCallbacks.injectMessage (message to buddy)", accName, prot, rec, msg);
       Message message = new Message(getJIDWithoutResource(accName), Message.MSG_TYPE_CHAT);
       message.setBody(msg);
       otrChatAdapter.sendOtrMessage(message);
     }
   }
 }
