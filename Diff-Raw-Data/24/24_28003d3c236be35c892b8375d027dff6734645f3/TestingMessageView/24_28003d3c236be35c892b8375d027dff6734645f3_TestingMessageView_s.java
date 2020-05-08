 package fakes;
 
 import com.cannedapps.cachat.app.views.interfaces.IChatView;
 
 public class TestingMessageView implements IChatView {
 
  private String lastMessage;
   
   @Override
  public void addMessage(String message) {
     lastMessage = message;
   }
 
  @Override
  public String getLastMessage() {
    return lastMessage;
  }

 }
