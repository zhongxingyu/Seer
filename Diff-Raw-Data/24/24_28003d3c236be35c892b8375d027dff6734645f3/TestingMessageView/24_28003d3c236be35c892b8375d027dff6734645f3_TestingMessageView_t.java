 package fakes;
 
 import com.cannedapps.cachat.app.views.interfaces.IChatView;
 
 public class TestingMessageView implements IChatView {
 
  public String lastMessage;
   
   @Override
  public void appendNewMessage(String message) {
     lastMessage = message;
   }
 
 }
