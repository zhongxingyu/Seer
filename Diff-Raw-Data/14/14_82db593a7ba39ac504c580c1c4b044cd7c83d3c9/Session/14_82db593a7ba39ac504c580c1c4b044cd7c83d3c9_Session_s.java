 package ru.tulupov.nsuconnect.protocol;
 
 import android.content.Context;
 import android.database.ContentObserver;
 import android.os.Handler;
 import android.util.Log;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.Volley;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 import ru.tulupov.nsuconnect.database.ContentUriHelper;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.helper.SearchSettingHelper;
 import ru.tulupov.nsuconnect.helper.SettingsHelper;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.model.Message;
 import ru.tulupov.nsuconnect.model.RequestSession;
 import ru.tulupov.nsuconnect.model.Settings;
 import ru.tulupov.nsuconnect.model.Status;
 import ru.tulupov.nsuconnect.model.Uid;
 import ru.tulupov.nsuconnect.model.User;
 import ru.tulupov.nsuconnect.request.CommandRequest;
 import ru.tulupov.nsuconnect.request.DisconnectRequest;
 import ru.tulupov.nsuconnect.request.GetUidRequest;
 import ru.tulupov.nsuconnect.request.SendMessageRequest;
 import ru.tulupov.nsuconnect.request.StartSearchRequest;
 import ru.tulupov.nsuconnect.request.StopTypingRequest;
 import ru.tulupov.nsuconnect.request.UploadRequest;
 
 public class Session {
 
     private static final String TAG = Session.class.getSimpleName();
     private static final long RETRY_DELAY = 1000;
     private RequestSession requestSession = new RequestSession();
 
     private RequestQueue queue;
     private Chat chat;
     private User myUser;
     private User anonymousUser;
     private User systemUser;
     private Context context;
 
     public Session(Context context, Chat chat) {
         this.context = context;
         this.chat = chat;
     }
 
     public void onCreate() {
        queue = Volley.newRequestQueue(context);
 
 
         myUser = new User();
         myUser.setName("Вы");
         myUser.setType(User.TYPE_YOUR);
         anonymousUser = new User();
         anonymousUser.setName("Аноним");
         anonymousUser.setType(User.TYPE_OTHER);
 
         systemUser = new User();
         systemUser.setName("Система");
         systemUser.setType(User.TYPE_SYSTEM);
         try {
             HelperFactory.getHelper().getUserDao().create(myUser);
             HelperFactory.getHelper().getUserDao().create(anonymousUser);
             HelperFactory.getHelper().getUserDao().create(systemUser);
 
         } catch (SQLException e) {
             Log.e(TAG, "cannot create chat entity", e);
         }
 
 
         Settings settings = SettingsHelper.getSettings(context);
 
         requestSession.setSearch(SearchSettingHelper.generate(settings.getSearchParameters()));
 
         Log.e("xxx", "set settings =" + requestSession.getSearch());
         GetUidRequest getUidRequest = new GetUidRequest(requestSession, createGetUidListener(), createGetUidErrorListener());
         queue.add(getUidRequest);
 
     }
 
     private Response.ErrorListener createGetUidErrorListener() {
         return new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError volleyError) {
                 createErrorListener().onErrorResponse(volleyError);
 
                 handler.postDelayed(getUidRunnable, RETRY_DELAY);
             }
         };
     }
 
     private Runnable getUidRunnable = new Runnable() {
         @Override
         public void run() {
             GetUidRequest getUidRequest = new GetUidRequest(requestSession, createGetUidListener(), createGetUidErrorListener());
             queue.add(getUidRequest);
         }
     };
 
     public void startMessageSending() {
         context.getContentResolver().registerContentObserver(ContentUriHelper.getConversationUri(chat.getId()), false, messageContentObserver);
         sendMessage();
     }
 
 
     public void onDestroy() {
         context.getContentResolver().unregisterContentObserver(messageContentObserver);
 
 
         DisconnectRequest request = new DisconnectRequest(requestSession, new Response.Listener() {
             @Override
             public void onResponse(Object o) {
                 queue.stop();
             }
         }, new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError volleyError) {
                 queue.stop();
             }
         }
         );
         handler.removeCallbacks(queryCommandRunnable);
         handler.removeCallbacks(getUidRunnable);
         handler.removeCallbacks(startSearchRunnable);
         queue.add(request);
     }
 
 
     public void sendStartTyping() {
 
     }
 
     public void sendStopTyping() {
         StopTypingRequest stopTypingRequest = new StopTypingRequest(requestSession, createErrorListener());
         queue.add(stopTypingRequest);
     }
 
 
     private ContentObserver messageContentObserver = new ContentObserver(new Handler()) {
         @Override
         public void onChange(boolean selfChange) {
 
             if (sendEnabled) {
                 sendMessage();
             } else {
                 wasUpdated = true;
             }
         }
     };
     boolean sendEnabled = true;
     boolean wasUpdated = false;
 
     private void sendMessage() {
         try {
             sendEnabled = false;
             List<Message> messages = HelperFactory.getHelper().getMessageDao().getUnsentMessagesByChat(chat);
             sendMessage(messages);
 
         } catch (SQLException e) {
             Log.e(TAG, "error", e);
         }
     }
 
     private void sendMessage(final List<Message> messages) {
         if (messages.isEmpty()) {
             sendEnabled = true;
             if (wasUpdated) {
                 wasUpdated = false;
 
                 sendMessage();
             }
 
             return;
         }
 
 
         final Message messageToSend = messages.remove(0);
         if (messageToSend.getUrl() != null) {
             UploadRequest uploadRequest = new UploadRequest(requestSession, messageToSend.getUrl(), new Response.Listener<Status>() {
                 @Override
                 public void onResponse(Status status) {
                     messageToSend.setSentFlag(true);
                     try {
                         HelperFactory.getHelper().getMessageDao().update(messageToSend);
                         ContentUriHelper.notifyChange(context, ContentUriHelper.getConversationUri(chat.getId()));
 
                     } catch (SQLException e) {
                         Log.e(TAG, "error", e);
                     }
 
                     sendMessage(messages);
                 }
             }, new Response.ErrorListener() {
                 @Override
                 public void onErrorResponse(VolleyError volleyError) {
                     createErrorListener().onErrorResponse(volleyError);
                     sendEnabled = true;
                 }
             }
             );
 
             queue.add(uploadRequest);
         } else {
             SendMessageRequest sendMessageRequest = new SendMessageRequest(requestSession, messageToSend.getMessage(), new Response.Listener<Message>() {
                 @Override
                 public void onResponse(Message message) {
                     Log.e("xxx", message.toString());
                     messageToSend.setSentFlag(true);
                     try {
                         HelperFactory.getHelper().getMessageDao().update(messageToSend);
                         ContentUriHelper.notifyChange(context, ContentUriHelper.getConversationUri(chat.getId()));
 
                     } catch (SQLException e) {
                         Log.e(TAG, "error", e);
                     }
 
                     sendMessage(messages);
                 }
             }, new Response.ErrorListener() {
                 @Override
                 public void onErrorResponse(VolleyError volleyError) {
                     createErrorListener().onErrorResponse(volleyError);
                     sendEnabled = true;
                 }
             }
             );
             queue.add(sendMessageRequest);
         }
 
     }
 
     private Response.Listener<Uid> createGetUidListener() {
         return new Response.Listener<Uid>() {
             @Override
             public void onResponse(Uid uid) {
                 requestSession.setUid(uid);
                 Log.e("xxx", uid.getUid());
                 StartSearchRequest startSearchRequest = new StartSearchRequest(requestSession, createStartSearchListener(), createStartSearchErrorListener());
                 queue.add(startSearchRequest);
             }
         };
     }
 
     private Response.ErrorListener createStartSearchErrorListener() {
         return new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError volleyError) {
                 createErrorListener().onErrorResponse(volleyError);
                 handler.postDelayed(startSearchRunnable, RETRY_DELAY);
             }
         };
     }
 
     private Runnable startSearchRunnable = new Runnable() {
         @Override
         public void run() {
             StartSearchRequest startSearchRequest = new StartSearchRequest(requestSession, createStartSearchListener(), createStartSearchErrorListener());
             queue.add(startSearchRequest);
         }
     };
 
     private Response.Listener<Status> createStartSearchListener() {
         return new Response.Listener<Status>() {
             @Override
             public void onResponse(Status status) {
                 requestSession.setLastId(requestSession.getUid().getUid());
                 Log.e("xxx", status.toString());
 
                 processCommand(status);
 
                 queryNextCommand();
             }
 
 
         };
     }
 
     private Response.Listener<ru.tulupov.nsuconnect.model.Command[]> createCommandListener() {
         return new Response.Listener<ru.tulupov.nsuconnect.model.Command[]>() {
             @Override
             public void onResponse(ru.tulupov.nsuconnect.model.Command[] commands) {
                 if (commands != null) {
                     for (ru.tulupov.nsuconnect.model.Command command : commands) {
 
                         // TODO fix commands id, now its ignore
 
                         Log.e("xxx", "start to process command, get ids");
 
                         if (command.getIds() != null) {
                             for (Map.Entry<String, String> entry : command.getIds().entrySet()) {
 
                                 requestSession.setLastId(String.format("%s:%s", entry.getValue(), entry.getKey()));
 
                                 Log.e("xxx", "current id=" + requestSession.getLastId());
                             }
                         }
 
 //                        session.setLastId(session.getUid().getUid());
 
                         processCommand(command.getStatus());
                         Log.e("xxx", "status=" + command.getStatus().toString());
 
                     }
 
                     queryNextCommand();
                 } else {
                     queryNextCommand();
                 }
             }
         };
 
 
     }
 
     public void processCommand(final Status status) {
         if (status != null) {
 
 
             CommandContext commandContext = new CommandContext() {
                 @Override
                 public Context getApplicationContext() {
                     return context;
                 }
 
                 @Override
                 public Status getStatus() {
                     return status;
                 }
 
                 @Override
                 public Chat getChat() {
                     return chat;
                 }
 
                 @Override
                 public User getAnonymousUser() {
                     return anonymousUser;
                 }
 
                 @Override
                 public User getSystemUser() {
                     return systemUser;
                 }
 
                 @Override
                 public User getCurrentUser() {
                     return myUser;
                 }
 
                 @Override
                 public RequestQueue getRequestQueue() {
                     return queue;
                 }
 
                 @Override
                 public RequestSession getRequestSession() {
                     return requestSession;
                 }
 
                 @Override
                 public Response.ErrorListener getErrorListener() {
                     return createErrorListener();
                 }
 
                 @Override
                 public Session getSession() {
                     return Session.this;
                 }
             };
 
 
             CommandFactory.get(status.getStatus()).execute(commandContext);
 
 
         }
     }
 
 
     private void queryNextCommand() {
         CommandRequest commandRequest = new CommandRequest(requestSession, createCommandListener(), createCommandErrorListener());
         queue.add(commandRequest);
         Log.e("xxx", "add CommandRequest");
     }
 
     private Response.ErrorListener createErrorListener() {
         return new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError volleyError) {
                 Log.e("xxx", volleyError.toString());
             }
         };
     }
 
     private Handler handler = new Handler();
     private Runnable queryCommandRunnable = new Runnable() {
         @Override
         public void run() {
             sendMessage();
             queryNextCommand();
         }
     };
 
     private Response.ErrorListener createCommandErrorListener() {
         return new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError volleyError) {
                 Log.e("xxx", volleyError.toString());
                 Log.e("xxx", "Try reconnect after delay");
                 handler.postDelayed(queryCommandRunnable, RETRY_DELAY);
 
 //                if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 502) {
 //                    Log.e("xxx", "session error, reset id");
 //                    requestSession.setLastId(requestSession.getUid().getUid());
 //                    queryNextCommand();
 //                }
 
             }
         };
     }
 }
