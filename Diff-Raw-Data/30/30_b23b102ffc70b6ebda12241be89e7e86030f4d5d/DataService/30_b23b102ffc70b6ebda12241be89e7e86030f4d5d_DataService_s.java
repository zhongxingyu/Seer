 package ru.tulupov.nsuconnect.service;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.Volley;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Map;
 
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.helper.SearchSettingHelper;
 import ru.tulupov.nsuconnect.helper.SettingsHelper;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.model.Command;
 import ru.tulupov.nsuconnect.model.Message;
 import ru.tulupov.nsuconnect.model.Session;
 import ru.tulupov.nsuconnect.model.Settings;
 import ru.tulupov.nsuconnect.model.Status;
 import ru.tulupov.nsuconnect.model.Success;
 import ru.tulupov.nsuconnect.model.Uid;
 import ru.tulupov.nsuconnect.model.User;
 import ru.tulupov.nsuconnect.request.CommandRequest;
 import ru.tulupov.nsuconnect.request.Constants;
 import ru.tulupov.nsuconnect.request.GetUidRequest;
 import ru.tulupov.nsuconnect.request.SendMessageRequest;
 import ru.tulupov.nsuconnect.request.SetSearchParametersRequest;
 import ru.tulupov.nsuconnect.request.StartSearchRequest;
 import ru.tulupov.nsuconnect.request.StartTypingRequest;
 import ru.tulupov.nsuconnect.request.StopTypingRequest;
 
 
 public class DataService extends Service {
     public static final String ACTION_SEND_MESSAGE = "send_message";
     public static final String ACTION_LOGIN = "login";
     public static final String ACTION_START_TYPING = "start_typing";
     public static final String ACTION_STOP_TYPING = "stop_typing";
     public static final String EXTRA_MESSAGE = "message";
     private static final String TAG = DataService.class.getSimpleName();
 
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     private Session session = new Session();
 
     private RequestQueue queue;
     private Chat chat;
     private User myUser;
     private User anonymousUser;
     private User systemUser;
 
     @Override
     public void onCreate() {
         super.onCreate();
         queue = Volley.newRequestQueue(this);
 
        session.setSearch("nsu_department=0; search_nsu_department=0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0; age=0; gender=1; search_age=0%2C0%2C0%2C0%2C0; search_gender=0%2C0;");

 
         try {
             chat = HelperFactory.getHelper().getChatDao().getLast();
 
             SettingsHelper.setChat(getApplicationContext(), chat);
         } catch (SQLException e) {
             Log.e(TAG, "cannot create chat entity", e);
         }
 
 
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
 
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
 
         if (intent != null && intent.getAction() != null) {
             String action = intent.getAction();
 
             if (action.equals(ACTION_START_TYPING)) {
                 StartTypingRequest startTypingRequest = new StartTypingRequest(session, createErrorListener());
                 queue.add(startTypingRequest);
             } else if (action.equals(ACTION_STOP_TYPING)) {
                 StopTypingRequest stopTypingRequest = new StopTypingRequest(session, createErrorListener());
                 queue.add(stopTypingRequest);
             } else if (action.equals(ACTION_SEND_MESSAGE)) {
                 String text = intent.getStringExtra(EXTRA_MESSAGE);
 
                 Message message = new Message();
                 message.setMessage(text);
                 message.setDate(new Date());
                 message.setChat(chat);
                 message.setUser(myUser);
                 try {
                     HelperFactory.getHelper().getMessageDao().create(message);
                     sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_MESSAGE_LIST));
 
                 } catch (SQLException e) {
                     Log.e(TAG, "cannot create message entity", e);
                 }
 
 
                 SendMessageRequest sendMessageRequest = new SendMessageRequest(session, text, new Response.Listener<Message>() {
                     @Override
                     public void onResponse(Message message) {
                         Log.e("xxx", message.toString());
                     }
                 }, createErrorListener());
                 queue.add(sendMessageRequest);
                 Log.e("xxx", "add SendMessageRequest");
             } else if (action.equals(ACTION_LOGIN)) {
                 Settings settings = SettingsHelper.getSettings(getApplicationContext());
 
                 session.setSearch(SearchSettingHelper.generate(settings.getSearchParameters()));
 
                 Log.e("xxx", "set settings =" + session.getSearch());
                 GetUidRequest getUidRequest = new GetUidRequest(session, createGetUidListener(), createErrorListener());
                 queue.add(getUidRequest);
 
 //                SetSearchParametersRequest setSearchParametersRequest = new SetSearchParametersRequest(session, settings.getSearchParameters(), createSearchParametersListener(), createErrorListener());
 //                queue.add(setSearchParametersRequest);
 
             }
         }
         return super.onStartCommand(intent, flags, startId);
     }
 
     private Response.Listener<Success> createSearchParametersListener() {
         return new Response.Listener<Success>() {
             @Override
             public void onResponse(Success success) {
                 Log.e("xxx", "set settings =" + success);
                 Log.e("xxx", "set settings =" + session.getSearch());
                 GetUidRequest getUidRequest = new GetUidRequest(session, createGetUidListener(), createErrorListener());
                 queue.add(getUidRequest);
             }
         };
     }
 
     private Response.Listener<Uid> createGetUidListener() {
         return new Response.Listener<Uid>() {
             @Override
             public void onResponse(Uid uid) {
                 session.setUid(uid);
                 Log.e("xxx", uid.getUid());
                 StartSearchRequest startSearchRequest = new StartSearchRequest(session, createStartSearchListener(), createErrorListener());
                 queue.add(startSearchRequest);
             }
         };
     }
 
     private Response.Listener<Status> createStartSearchListener() {
         return new Response.Listener<Status>() {
             @Override
             public void onResponse(Status status) {
                 session.setLastId(session.getUid().getUid());
                 Log.e("xxx", status.toString());
                 queryNextCommand();
             }
 
 
         };
     }
 
     private Response.Listener<Command[]> createCommandListener() {
         return new Response.Listener<Command[]>() {
             @Override
             public void onResponse(Command[] commands) {
                 if (commands != null) {
                     for (Command command : commands) {
                        if (command.getIds() != null) {
                            for (Map.Entry<String, String> entry : command.getIds().entrySet()) {
                                session.setLastId(String.format("%s:%s", entry.getValue(), entry.getKey()));
                            }
                        }
                         if (command.getStatus() != null) {
                             Status status = command.getStatus();
 
                            if (status.getStatus().equals(Constants.STATUS_CONNECTED)) {
//                                SendMessageRequest sendMessageRequest = new SendMessageRequest(session, "Привет :)", new Response.Listener<Message>() {
//                                    @Override
//                                    public void onResponse(Message message) {
//                                        Log.e("xxx", message.toString());
//                                    }
//                                }, createErrorListener());
//                                queue.add(sendMessageRequest);
                                Log.e("xxx", "add SendMessageRequest");
                            }
 
                             if (status.getStatus().equals(Constants.STATUS_MESSAGE)) {
                                 Message message = new Message();
                                 message.setMessage(status.getMsg());
                                 message.setDate(new Date());
                                 message.setChat(chat);
                                 message.setUser(anonymousUser);
                                 try {
                                     HelperFactory.getHelper().getMessageDao().create(message);
                                     sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_MESSAGE_LIST));
 
                                 } catch (SQLException e) {
                                     Log.e(TAG, "cannot create message entity", e);
                                 }
                             }
 
                             if (status.getStatus().equals(Constants.STATUS_START_TYPING)) {
                                 sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_TYPING_STATUS).putExtra(DatabaseConstants.EXTRA_IS_TYPING, true));
                             }
                             if (status.getStatus().equals(Constants.STATUS_STOP_TYPING)) {
                                 sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_TYPING_STATUS).putExtra(DatabaseConstants.EXTRA_IS_TYPING, false));
                             }
 
                             if (status.getStatus().equals(Constants.STATUS_WAITING)) {
                                 writeSystem("Ожидание подключения");
                             }
                             if (status.getStatus().equals(Constants.STATUS_CONNECTED)) {
                                 writeSystem("Подключено");
                             }
                             if (status.getStatus().equals(Constants.STATUS_DISCONNECTED)) {
                                 writeSystem("Отключено");
                                 sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_TYPING_STATUS).putExtra(DatabaseConstants.EXTRA_IS_TYPING, false));
                             }
                         }
                         Log.e("xxx", "status=" + command.getStatus().toString());
                         queryNextCommand();
                     }
                 } else {
                     queryNextCommand();
                 }
             }
         }
 
                 ;
     }
 
     private void writeSystem(String text) {
         Message message = new Message();
         message.setMessage(text);
         message.setDate(new Date());
         message.setChat(chat);
         message.setUser(systemUser);
         try {
             HelperFactory.getHelper().getMessageDao().create(message);
             sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_MESSAGE_LIST));
 
         } catch (SQLException e) {
             Log.e(TAG, "cannot create message entity", e);
         }
     }
 
     private void queryNextCommand() {
         CommandRequest commandRequest = new CommandRequest(session, createCommandListener(), createCommandErrorListener());
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
 
     private Response.ErrorListener createCommandErrorListener() {
         return new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError volleyError) {
                 Log.e("xxx", volleyError.toString());
                 if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 502) {
                     session.setLastId(session.getUid().getUid());
                     queryNextCommand();
                 }
 
             }
         };
     }
 }
