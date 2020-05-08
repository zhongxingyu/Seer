 package com.battlegame;
 
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 import com.battlegame.ConnectActivity.IncomingHandler;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.app.Activity;
 //import android.content.Intent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 
 public class CharacterSelectActivity extends Activity {
 
 	// Server vars
 	private Boolean mIsBound = false;
 	Messenger mService = null;
 	String type;
    
 	TextView rMessageText;
 	EditText sMessageText;
 	
 	String mClientMsg = "";
 
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_character_select);
 
         ImageButton unicornButton = (ImageButton) findViewById(R.id.unicornButton);
         ImageButton wyvernButton = (ImageButton) findViewById(R.id.wyvernButton);
         
         Button sButton = (Button) findViewById(R.id.sButton);
         
         rMessageText = (TextView) findViewById(R.id.rMessageText);
         sMessageText = (EditText) findViewById(R.id.sMessageText);
 
         Bundle extras = getIntent().getExtras();
         type = extras.getString("type");
         rMessageText.setText(type);
         
         // Set to use client/server key for encryption/decryption
         // This assumes only two users total
         // Also must create asset manager to open the files with
         
         RSAEncryption.assetMgr = this.getAssets();
 
         if(type.equals("client")) {
         	RSAEncryption.publicKeyType = "server";
         	RSAEncryption.privateKeyType = "client";
         }
         else {
         	RSAEncryption.publicKeyType = "client";
         	RSAEncryption.privateKeyType = "server";
         }
         
          if(mIsBound) {
         	 doUnbindService();
         	 doBindService();
          }
          else {
         	 doBindService();
          }
          
         View.OnClickListener selectListner = new View.OnClickListener() {
         	public void onClick(View argO) {
         		
         		// Wait for other player to select character
         		//while(msgNotReceived);
         	
         		//Intent i = new Intent(CharacterSelectActivity.this, BattleActivity.class);
         		//startActivity(i);
         	}
         };
         
         sButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 	            //tv.setText("Attached.");
 	            try {
 	            	Message msg;
 	            	Bundle b = new Bundle();
 	            	// Send encrypted data
		    		// First convert string to base 64
		    		// Then use your private key to encrypt
		    		// Then use public key from other guy to encrypt
		    		// Turn that to base 64 again and send it
	            	b.putString("data", Base64.encodeBytes(RSAEncryption.rsaEncrypt(RSAEncryption.rsaPrivateEncrypt(Base64.encodeBytes(sMessageText.getText().toString().getBytes("US-ASCII")).getBytes("US-ASCII")))));
 	            	if(type.equals("client")) {
 	            		msg = Message.obtain(null, SocketService.MSG_CHAR_SELECT);
 	            	}
 	            	else {
 	            		msg = Message.obtain(null, SocketServerService.MSG_CHAR_SELECT);
 	            	}
 	            	msg.setData(b);
 	                msg.replyTo = mMessenger;
 	                mService.send(msg);
 	            } catch (RemoteException e) {
 	                // In this case the service has crashed before we could even do anything with it
 	         	   e.printStackTrace();
 	            } catch (InvalidKeyException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NoSuchAlgorithmException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NoSuchPaddingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalBlockSizeException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (BadPaddingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
         });
         
         unicornButton.setOnClickListener(selectListner);
         wyvernButton.setOnClickListener(selectListner);
     }
 	    
 	class IncomingHandler extends Handler {
 	    @Override
 	    public void handleMessage(Message msg) {
 	    	Bundle b = msg.getData();
 	    	String data = b.getString("data");
 	    	try {
				rMessageText.setText(new String(Base64.decode(RSAEncryption.rsaPublicDecrypt(RSAEncryption.rsaDecrypt(Base64.decode(data))))));
 			} catch (InvalidKeyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchPaddingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalBlockSizeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (BadPaddingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 //	        switch (msg.what) {
 //	            case SocketService.MSG_CONNECT_SUCCESS:
 //	                //Toast.makeText(getApplicationContext(), "Connection successful!", Toast.LENGTH_SHORT).show();
 //	                //characterSelect();
 //	                break;
 //	            case SocketService.MSG_CHAR_SELECT:
 //	                //Toast.makeText(getApplicationContext(), "Characters selected!", Toast.LENGTH_SHORT).show();
 //	                break;
 //	            case SocketService.MSG_ATTACK:
 //	                //Toast.makeText(getApplicationContext(), "Attack message!", Toast.LENGTH_SHORT).show();
 //	                break;
 //	            default:
 //	                super.handleMessage(msg);
 //	        }
 	    }	
 	}
 	private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             mService = new Messenger(service);
             //tv.setText("Attached.");
             try {
             	Message msg;
             	if(type.equals("client")) {
             		msg = Message.obtain(null, SocketService.MSG_CONNECT_SUCCESS);
             	}
             	else {
             		msg = Message.obtain(null, SocketServerService.MSG_CONNECT_SUCCESS);
             	}
                 msg.replyTo = mMessenger;
                 mService.send(msg);
             } catch (RemoteException e) {
                 // In this case the service has crashed before we could even do anything with it
          	   e.printStackTrace();
             }
         }
 
         public void onServiceDisconnected(ComponentName className) {
             // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
             mService = null;
             //tv.setText("Disconnected.");
         }
     };
  	   
      void doBindService() {
     	 if(type.equals("client")) {
     		 bindService(new Intent(this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
     	 }
     	 else {
     		 bindService(new Intent(this, SocketServerService.class), mConnection, Context.BIND_AUTO_CREATE);
     	 }
          mIsBound = true;
          //tv.setText("Binding.");
      }
      
      void doUnbindService() {
          if (mIsBound) {
              // If we have received the service, and hence registered with it, then now is the time to unregister.
              if (mService != null) {
                  try {
                  	Message msg;
                  	if(type.equals("client")) {
                 		msg = Message.obtain(null, SocketService.MSG_CONNECT_SUCCESS);
                  	}
                  	else {
                 		msg = Message.obtain(null, SocketServerService.MSG_CONNECT_SUCCESS);
                  	}
                  	msg.replyTo = mMessenger;
                  	mService.send(msg);
                  } catch (RemoteException e) {
                      // There is nothing special we need to do if the service has crashed.
                  }
              }
              // Detach our existing connection.
              unbindService(mConnection);
              mIsBound = false;
              //tv.setText("Unbinding.");
          }
      }
 }
