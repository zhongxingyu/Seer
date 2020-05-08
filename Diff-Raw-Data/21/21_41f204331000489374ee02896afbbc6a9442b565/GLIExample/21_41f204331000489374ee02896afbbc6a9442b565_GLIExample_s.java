 import java.util.List;
 
 import com.google.protobuf.ByteString;
 
 
 public class GLIExample implements GLI {
 
     private GLSRequestHandler service;
 
     public GLIExample(GLSRequestHandler service){
         this.service = service;
     }
 
        @Override
     public JOutPiqi.response init(List<String> players) {
         System.out.println("init, players: [" + players + "]");
        return demo().mergeResponse(
            JOutPiqi.callback_response.newBuilder().addActions(
                JOutPiqi.action.newBuilder().setStartTimer(
                JOutPiqi.timer.newBuilder().setId("timer")
                                               .setDurationInMs(1000)
                                               .setTickDurationInMs(3000)
            )).build()
         ).build();
     }
 
     @Override
     public JOutPiqi.response handle_action(String userId, String commandName,
             List<ByteString> args, ByteString state) {
         System.out.println("handle_action [" + commandName + "] with [" + args + "], state: ["+state.toStringUtf8() + "]");
         // service.buy_item_request(userId, 1, 1);
         return demo().build();
     }
 
     @Override
     public JOutPiqi.response handle_timer(String id, int delta,
             ByteString state) {
         System.out.println("handle_timer [" + id + "], delta [" + delta + "], state: [" + state.toStringUtf8() + "]");
         return demo().build();
     }
 
     @Override
     public JOutPiqi.response handle_timer_complete(String id, int delta,
             ByteString state) {
         System.out.println("handle_timer_complete [" + id + "], delta [" + delta + "], state: [" + state.toStringUtf8() + "]");
         return demo().build();
     }
 
     @Override
     public JOutPiqi.response handle_user_join(String userId, ByteString state) {
         System.out.println("user_join [" + userId + "]. state: [" + state.toStringUtf8() + "]");
         return demo().build();
     }
 
     @Override
     public JOutPiqi.response handle_user_leave(String userId, ByteString state) {
         System.out.println("user_leave [" + userId + "]. state: [" + state.toStringUtf8() + "]");
         return demo().build();
     }
 
     private JOutPiqi.response.Builder demo() {
         return JOutPiqi.response.newBuilder().setResponse(
             JOutPiqi.callback_response.newBuilder()
                 .setResult(JOutPiqi.result.newBuilder().setOk(true))
                 .setState(ByteString.copyFromUtf8("newstate"))
         );
     }
 }
