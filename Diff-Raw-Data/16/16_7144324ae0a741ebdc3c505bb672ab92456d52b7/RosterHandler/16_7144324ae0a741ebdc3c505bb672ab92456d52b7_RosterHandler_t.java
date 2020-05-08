 package directi.androidteam.training.chatclient.PacketHandlers;
 
 import android.content.Context;
 import android.util.Log;
 import directi.androidteam.training.ChatApplication;
 import directi.androidteam.training.TagStore.*;
 import directi.androidteam.training.chatclient.Authentication.Account;
 import directi.androidteam.training.chatclient.Authentication.AccountManager;
 import directi.androidteam.training.chatclient.Roster.RosterManager;
 import directi.androidteam.training.chatclient.Roster.VCard;
 import directi.androidteam.training.chatclient.Roster.VCardDatabaseHandler;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.UUID;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class RosterHandler implements Handler {
     private static RosterHandler rosterHandler = new RosterHandler();
     private Lock lock = new ReentrantLock();
 
     public static RosterHandler getInstance() {
         return rosterHandler;
     }
 
     @Override
     public void processPacket(Tag tag) {
         if(tag.getTagname().equals("message")){
         } else if (tag.getTagname().equals("stream:stream") || tag.getTagname().equals("success") || tag.getTagname().equals("failure")) {
         } else if (tag.getTagname().equals("iq") && tag.contains("bind")) {
         } else if (tag.getChildTags() == null) {
         } else {
             if (tag.getTagname().equals("iq")) {
                 processIqPacket(new IQTag(tag));
             } else if (tag.getTagname().equals("presence")) {
                 processPresencePacket(new Presence(tag));
             }
         }
     }
 
     private void processIqPacket(final IQTag tag) {
         Account account = AccountManager.getInstance().getAccount(tag.getRecipientAccount());
         account.setFullJID(tag.getAttribute("to"));
         if (tag.contains("query")) {
             processQueryPacket(new Query(tag.getChildTag("query").setRecipientAccount(tag.getRecipientAccount())), account);
         } else if (tag.contains("vCard")) {
             processVCardPacket(new VCardTag(tag.getChildTag("vCard").setRecipientAccount(tag.getRecipientAccount())), tag.getAttribute("from").split("/")[0]);
         }
     }
 
     private void processPresencePacket(Presence presence) {
         if(presence.getType() != null && presence.getType().equals("unavailable")) {
             presence.setShow("unavailable");
         }
         RosterManager.getInstance().updatePresence(presence);
     }
 
     private void processQueryPacket(final Query queryTag, final Account account) {
         if (queryTag.getAttribute("xmlns").equals("jabber:iq:roster")) {
             RosterManager.getInstance().updateRosterList(queryTag);
             PacketWriter.addToWriteQueue(new Presence(UUID.randomUUID().toString(), account.getFullJID(), account.getStatus(), account.getShow()).setRecipientAccount(account.getAccountUid()));
             requestVCards(queryTag);
         }
     }
 
     private void processVCardPacket(VCardTag vCardTag, String senderBareJID) {
         VCard vCard = new VCard(senderBareJID);
         vCard.populateFromTag(vCardTag);
         RosterManager.getInstance().updatePhoto(vCard, vCardTag.getRecipientAccount(), senderBareJID);
//        lock.lock();
         VCardDatabaseHandler db = new VCardDatabaseHandler(ChatApplication.getAppContext());
         String key = vCardTag.getRecipientAccount() + "_" + senderBareJID;
         if (vCardTag.getChildTag("PHOTO") == null || vCardTag.getChildTag("PHOTO").getChildTags() == null || vCardTag.getChildTag("PHOTO").getChildTag("BINVAL") == null) {
            if (vCardTag.getChildTag("PHOTO") == null || vCardTag.getChildTag("PHOTO").getChildTags() == null || vCardTag.getChildTag("PHOTO").getChildTag("EXTVAL") == null) {
                db.addVCard(key, vCard.getName(), VCardDatabaseHandler.AVATAR_DOES_NOT_EXIST);
            }
         } else {
             db.addVCard(key, vCard.getName(), VCardDatabaseHandler.AVATAR_EXISTS);
             try {
                 FileOutputStream fileOutputStream = ChatApplication.getAppContext().openFileOutput(key, Context.MODE_PRIVATE);
                 fileOutputStream.write(vCardTag.getChildTag("PHOTO").getChildTag("BINVAL").getContent().getBytes());
                 fileOutputStream.close();
             } catch (FileNotFoundException e) {
                 Log.d("FileNotFoundException", e.toString());
             } catch (IOException e) {
                 Log.d("IOException", e.toString());
             }
         }
         db.close();
//        lock.unlock();
     }
 
     private void requestVCards(Tag rosterResult) {
         for (Tag tag : rosterResult.getChildTags()) {
             if (tag.getAttribute("subscription").equals("both")) {
                 VCard vCard = getCachedVCard(rosterResult.getRecipientAccount(), tag.getAttribute("jid"));
                 if (vCard == null) {
                     PacketWriter.addToWriteQueue(new IQTag("getVCard", tag.getAttribute("jid"), "get", new VCardTag("vcard-temp")).setRecipientAccount(rosterResult.getRecipientAccount()));
                 } else {
                     RosterManager.getInstance().updatePhoto(vCard, rosterResult.getRecipientAccount(), tag.getAttribute("jid"));
                 }
             }
         }
     }
 
     private VCard getCachedVCard(String accountUID, String itemBareJID) {
         String key = accountUID + "_" + itemBareJID;
//        lock.lock();
         VCardDatabaseHandler db = new VCardDatabaseHandler(ChatApplication.getAppContext());
         String fullName = db.getFullName(key);
         if (fullName == null) {db.close(); return null;}
         VCard vCard = null;
         if (fullName != null) {
             vCard = new VCard(fullName);
             if (db.avatarExists(key).equals(VCardDatabaseHandler.AVATAR_EXISTS)) {
                 vCard.setAvatar(vCard.decodeAvatar(getCachedAvatar(key)));
             }
         }
         db.close();
//        lock.unlock();
         return vCard;
     }
 
     private String getCachedAvatar(String key) {
         String encodedAvatar = "";
         try {
             FileInputStream fileInputStream = ChatApplication.getAppContext().openFileInput(key);
             StringBuffer fileContent = new StringBuffer("");
             byte [] buffer = new byte[1024];
             int length;
             while ((length = fileInputStream.read(buffer)) != -1) {
                 fileContent.append(new String(buffer));
             }
             fileInputStream.close();
             encodedAvatar = new String(fileContent);
         } catch (IOException e) {
             Log.d("IOException", e.toString());
         }
         return encodedAvatar;
     }
 }
