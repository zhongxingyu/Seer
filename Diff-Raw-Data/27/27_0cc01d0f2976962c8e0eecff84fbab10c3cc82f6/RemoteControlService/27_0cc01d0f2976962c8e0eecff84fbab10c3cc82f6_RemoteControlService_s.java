 package com.purplehatstands.clementine.remote;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jivesoftware.smack.SASLAuthentication;
 import org.jivesoftware.smack.XMPPException;
 
 import com.purplehatstands.libxrme.Connection;
 import com.purplehatstands.libxrme.PeerDiscoveryInterface;
 import com.purplehatstands.libxrme.RemoteControlHandler;
 import com.purplehatstands.libxrme.RemoteControlInterface;
 import com.purplehatstands.libxrme.State;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.app.AlertDialog;
 import android.app.Service;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Binder;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 public class RemoteControlService extends Service implements
     PeerDiscoveryInterface {
   private static final String TAG = "RemoteControlService";
   private final IBinder binder_ = new LocalBinder();
 
   private Context ui_context_;
   private String auth_token_;
   private Account google_account_;
  private Connection connection_;
   private boolean first_connection_attempt_ = true;
 
   private RemoteControlInterface remote_control_ = null;
 
   private List<ConnectionHandler> connection_handlers_ = new ArrayList<ConnectionHandler>();
   private List<MediaStateHandler> media_state_handlers_ = new ArrayList<MediaStateHandler>();
 
   public class LocalBinder extends Binder {
     RemoteControlService getService() {
       return RemoteControlService.this;
     }
   }
 
   public interface ConnectionHandler {
     public void OnConnected(String full_jid, String identity);
 
     public void OnConnectionFailure(String message);
 
     public void OnDisconnected();
   }
 
   public interface MediaStateHandler {
     public void OnStateChanged(final State state);
 
     public void OnAlbumArtChanged(final Bitmap image);
   }
 
   public RemoteControlInterface GetRemoteControl() {
     return remote_control_;
   }
 
   public void AddConnectionHandler(ConnectionHandler handler) {
     connection_handlers_.add(handler);
   }
 
   public void AddMediaStateHandler(MediaStateHandler handler) {
     media_state_handlers_.add(handler);
   }
 
   public IBinder onBind(Intent intent) {
     return binder_;
   }
 
   public void Connect(Context ui_context) {
     ui_context_ = ui_context;
 
     GetAuthToken();
   }
 
   private void GetAuthToken() {
     AccountManager manager = AccountManager.get(this);
     google_account_ = manager.getAccountsByType("com.google")[0];
 
     manager.getAuthToken(google_account_, "mail", true,
         new AccountManagerCallback<Bundle>() {
           public void run(AccountManagerFuture<Bundle> arg0) {
             try {
               Bundle bundle = arg0.getResult();
               if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                 auth_token_ = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                 StartConnection();
               } else if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                 Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                 ui_context_.startActivity(intent);
               }
             } catch (OperationCanceledException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
             } catch (AuthenticatorException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
             } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
             }
 
           }
         }, null);
   }
 
   private void StartConnection() {
     SASLAuthentication.registerSASLMechanism("X-GOOGLE-TOKEN",
         GoogleTokenAuthenticator.class);
     SASLAuthentication.supportSASLMechanism("X-GOOGLE-TOKEN", 0);
 
     connection_ = new Connection();
     connection_.set_username(google_account_.name);
     connection_.set_password(auth_token_);
    connection_.set_agent_name("Clementine Remote on " + Build.PRODUCT);
 
     connection_.SetPeerDiscoveryInterface(this);
 
     remote_control_ = new RemoteControlInterface() {
       public void StateChanged(String peer_jid_resource, State state) {
         for (MediaStateHandler handler : media_state_handlers_) {
           handler.OnStateChanged(state);
         }
       }
 
       public void AlbumArtChanged(String peer_jid_resource, Bitmap image) {
         for (MediaStateHandler handler : media_state_handlers_) {
           handler.OnAlbumArtChanged(image);
         }
       }
     };
     connection_.SetRemoteControl(remote_control_);
 
     try {
       connection_.Connect();
     } catch (XMPPException e) {
       HandleConnectionFailure(e.getMessage());
       return;
     }
 
   }
 
   private void HandleConnectionFailure(String message) {
     if (first_connection_attempt_) {
       // Try invalidating the auth token and trying to connect again
       AccountManager manager = AccountManager.get(this);
       manager.invalidateAuthToken("com.google", auth_token_);
 
       first_connection_attempt_ = false;
       GetAuthToken();
     } else {
       for (ConnectionHandler handler : connection_handlers_) {
         handler.OnConnectionFailure(message);
       }
     }
   }
 
   public void PeerFound(String full_jid, String identity) {
     for (ConnectionHandler handler : connection_handlers_) {
       handler.OnConnected(full_jid, identity);
     }
   }
 }
