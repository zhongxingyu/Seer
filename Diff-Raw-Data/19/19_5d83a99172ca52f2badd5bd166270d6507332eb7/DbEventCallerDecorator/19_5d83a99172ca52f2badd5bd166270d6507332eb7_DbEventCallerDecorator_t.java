 package org.aldeon.db.wrappers;
 
 import org.aldeon.core.events.IdentityAddedEvent;
 import org.aldeon.core.events.IdentityRemovedEvent;
 import org.aldeon.core.events.MessageAddedEvent;
 import org.aldeon.core.events.MessageRemovedEvent;
 import org.aldeon.core.events.UserAddedEvent;
 import org.aldeon.crypt.Key;
 import org.aldeon.db.Db;
 import org.aldeon.events.Callback;
 import org.aldeon.events.EventLoop;
 import org.aldeon.model.Identifier;
 import org.aldeon.model.Identity;
 import org.aldeon.model.Message;
 import org.aldeon.model.User;
 
 import java.util.Map;
 import java.util.Set;
 
 
 public class DbEventCallerDecorator extends AbstractDbWrapper {
 
     private final EventLoop eventLoop;
 
     public DbEventCallerDecorator(Db db, EventLoop eventLoop){
         super(db);
         this.eventLoop = eventLoop;
     }
 
     @Override
     public void getMessageById(Identifier msgId, Callback<Message> callback) {
         db.getMessageById(msgId,callback);
     }
 
     @Override
     public void insertMessage(final Message message, final Callback<Boolean> callback) {
         db.insertMessage(message, new Callback<Boolean>() {
             @Override
             public void call(Boolean messageInserted) {
                 if(messageInserted) {
                     eventLoop.notify(new MessageAddedEvent(message));
                 }
                 callback.call(messageInserted);
             }
         });
     }
 
     @Override
     public void deleteMessage(final Identifier msgId, final Callback<Boolean> callback) {
         db.deleteMessage(msgId, new Callback<Boolean>() {
             @Override
             public void call(Boolean messageRemoved) {
                 if (messageRemoved) {
                     eventLoop.notify(new MessageRemovedEvent(msgId));
                 }
                 callback.call(messageRemoved);
             }
         });
     }
 
     @Override
     public void getMessageXorById(Identifier msgId, Callback<Identifier> callback) {
         db.getMessageXorById(msgId,callback);
     }
 
     @Override
     public void getMessageIdsByXor(Identifier msgXor, Callback<Set<Identifier>> callback) {
         db.getMessageIdsByXor(msgXor,callback);
     }
 
     @Override
     public void getMessagesByParentId(Identifier parentId, Callback<Set<Message>> callback) {
         db.getMessagesByParentId(parentId, callback);
     }
 
     @Override
     public void getMessageIdsByParentId(Identifier parentId, Callback<Set<Identifier>> callback) {
         db.getMessageIdsByParentId(parentId, callback);
     }
 
     @Override
     public void getIdsAndXorsByParentId(Identifier parentId, Callback<Map<Identifier, Identifier>> callback) {
         db.getIdsAndXorsByParentId(parentId, callback);
     }
 
     @Override
     public void checkAncestry(Identifier descendant, Identifier ancestor, Callback<Boolean> callback) {
         db.checkAncestry(descendant, ancestor, callback);
     }
 
     @Override
     public void getClock(Callback<Long> callback) {
         db.getClock(callback);
     }
 
     @Override
     public void getMessagesAfterClock(Identifier topic, long clock, Callback<Set<Message>> callback) {
         db.getMessagesAfterClock(topic, clock, callback);
     }
 
     @Override
     public void insertUser(final User user, final Callback<Boolean> callback) {
        db.insertUser(user, callback);
     }
 
     @Override
     public void deleteUser(Key publicKey, Callback<Boolean> callback) {
         db.deleteUser(publicKey, callback);
     }
 
     @Override
     public void getUser(Key publicKey, Callback<User> callback) {
         db.getUser(publicKey, callback);
     }
 
     @Override
     public void getUsers(Callback<Set<User>> callback) {
         db.getUsers(callback);
     }
 
     @Override
     public void insertIdentity(final Identity identity, final Callback<Boolean> callback) {
         db.insertIdentity(identity, new Callback<Boolean>() {
             @Override
             public void call(Boolean identityInserted) {
                 if (identityInserted) {
                     eventLoop.notify(new IdentityAddedEvent(identity));
                 }
                 callback.call(identityInserted);
             }
         });
     }
 
     @Override
     public void deleteIdentity(final Key publicKey, final Callback<Boolean> callback) {
         db.deleteIdentity(publicKey, new Callback<Boolean>() {
             @Override
             public void call(Boolean identityRemoved) {
                 if (identityRemoved) {
                     eventLoop.notify(new IdentityRemovedEvent(publicKey));
                 }
                 callback.call(identityRemoved);
             }
         });
     }
 
     @Override
     public void getIdentity(Key publicKey, Callback<Identity> callback) {
         db.getIdentity(publicKey, callback);
     }
 
     @Override
     public void getIdentities(Callback<Set<Identity>> callback) {
         db.getIdentities(callback);
     }
 
     @Override
     public void start() {
         db.start();
     }
 
     @Override
     public void close() {
         db.close();
     }
 }
