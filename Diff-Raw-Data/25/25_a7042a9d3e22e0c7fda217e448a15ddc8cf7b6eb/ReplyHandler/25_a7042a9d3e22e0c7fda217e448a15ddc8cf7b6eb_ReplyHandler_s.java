 package handlers;
 
 import assistant.Constants;
 import assistant.SharedHandler;
 import assistant.pair.NullableExtendedParam;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import models.Account;
 import models.Reply;
 import models.ThumbsUp;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: marcus
  * Date: 4/22/13
  * Time: 2:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ReplyHandler extends SharedHandler {
     @Override
     public String[] handledRequests() {
         return new String[]{
                 Constants.Method.CREATE_REPLY,
                 Constants.Method.MODIFY_REPLY,
                 Constants.Method.DELETE_REPLY,
                 Constants.Method.THUMB_UP,
         };
 
     }
 
     @Override
     protected void process(Map<String, Object> jsonrpc2Params) throws Exception {
         if (method.equals(Constants.Method.CREATE_REPLY)) {
             createReply(jsonrpc2Params);
         } else if (method.equals(Constants.Method.MODIFY_REPLY)) {
             modifyReply(jsonrpc2Params);
         } else if (method.equals(Constants.Method.DELETE_REPLY)) {
             deleteReply(jsonrpc2Params);
         } else if (method.equals(Constants.Method.THUMB_UP)) {
             thumbUp(jsonrpc2Params);
         } else {
             throwJSONRPC2Error(JSONRPC2Error.METHOD_NOT_FOUND, Constants.Errors.METHOD_NOT_FOUND, method);
         }
     }
 
 
     /**
      * Creates a reply by the given threadId.
      * Returns the created reply if successful.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void createReply(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> params = getParams(jsonrpc2Params,
                 Constants.Param.Name.THREAD_ID,
                 Constants.Param.Name.TEXT);
 
         int parentThreadId = (Integer) params.get(Constants.Param.Name.THREAD_ID);
         String text = (String) params.get(Constants.Param.Name.TEXT);
 
         models.Thread parentThread = em.find(models.Thread.class, parentThreadId);
         Date currentDate = new Date();
 
         Reply reply = new Reply(parentThread, em.find(Account.class, accountId), text, new Timestamp(currentDate.getTime()));
 
         persistObjects(reply);
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         ArrayList<Reply> test = new ArrayList<Reply>();
         test.add(reply);
         responseParams.put(Constants.Param.Name.REPLY, serialize(test));
     }
 
     /**
      * Modify a reply by the given the replyId and returns the resulting object.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void modifyReply(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> params = getParams(jsonrpc2Params,
                 new NullableExtendedParam(Constants.Param.Name.REPLY_ID, false),
                 new NullableExtendedParam(Constants.Param.Name.TEXT, true));
 
         int replyId = (Integer)params.get(Constants.Param.Name.REPLY_ID);
         String text = (String) params.get(Constants.Param.Name.TEXT);
         Reply reply = em.find(Reply.class, (Integer) params.get(Constants.Param.Name.REPLY_ID));
 
         if(reply == null){
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.OBJECT_NOT_FOUND);
             return;
         }
         if(text != null){
             reply.setText(text);
         }
         reply.setTime(getCurrentTime());
         persistObjects(reply);
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.REPLY, serialize(reply));
     }
 
     /**
      * Delete a reply by the given the replyId.
      *
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void deleteReply(Map<String, Object> jsonrpc2Params) throws Exception {
//
//        Map<String, Object> createReplyParams = getParams(jsonrpc2Params,
//                Constants.Param.Name.REPLY_ID);
//
//        try {
//        models.Reply reply = em.remove(em.find(models.Reply.class, (Integer));
//        } catch (Exception e) {
//
//        }
//        responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
     }
 
     private void thumbUp(Map<String,Object> jsonrpc2Params) {
     }
 
 
 }
 
