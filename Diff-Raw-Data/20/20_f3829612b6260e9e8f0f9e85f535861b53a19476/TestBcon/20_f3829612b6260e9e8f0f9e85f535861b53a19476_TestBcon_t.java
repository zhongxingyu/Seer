 package bacn;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class TestBcon {
   private static interface MessageAboutUser<T extends MessageAboutUser<T>> extends Bcon {
     static class Impl extends BconAdapter implements MessageAboutUser<Impl> {
       
       Prim mUserId = member("userId");
 
       @Override
       public String userId() {
         return mUserId.asString();
       }
 
       @Override
       public Impl setUserId(String userId) {
         mUserId.setTo(userId);
         return this;
       }
       
     }
     
     String userId();
     T setUserId(String userId);
     
   }
   
   private static interface UserLeftRoom extends MessageAboutUser<UserLeftRoom> {
     static class Impl extends BconAdapter implements UserLeftRoom {
       
       private Value<MessageAboutUser.Impl> messageAboutUser = ancestor(MessageAboutUser.Impl.class, new MessageAboutUser.Impl());
 
       @Override
       public String userId() {
         if(messageAboutUser.notNull()) {
           return messageAboutUser.get().userId();
         }
         
         return null;
       }
 
       @Override
       public Impl setUserId(String userId) {
         if(messageAboutUser.notNull()) {
           messageAboutUser.get().setUserId(userId);
         }
         
         return this;
       }
       
     }
   }
   
   @Test
   public void testBcon() {
     String userId = "Karen";
     UserLeftRoom userLeftRoom = new UserLeftRoom.Impl().setUserId(userId);
     Assert.assertEquals(userId, userLeftRoom.userId());
   }
   
   @Test
   public void testEquality() {
     String firstUser = "Michael";
     String secondUser = "John";
     
     
     Assert.assertEquals(new UserLeftRoom.Impl().setUserId(firstUser),
         new UserLeftRoom.Impl().setUserId(firstUser));
     Assert.assertFalse(new UserLeftRoom.Impl().setUserId(firstUser).equals(
         new UserLeftRoom.Impl().setUserId(secondUser)));
    
    // Check for equality when referencing the same object.
    UserLeftRoom.Impl instance = new UserLeftRoom.Impl();
    Assert.assertEquals(instance, instance);
   }
 }
