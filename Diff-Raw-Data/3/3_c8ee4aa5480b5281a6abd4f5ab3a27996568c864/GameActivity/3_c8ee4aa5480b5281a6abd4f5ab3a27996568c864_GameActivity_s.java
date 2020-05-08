 package no.hist.aitel.android.tictactoe;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Formatter;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class GameActivity extends Activity {
 
     private static final String TAG = GameActivity.class.getSimpleName();
     private static final String PREFS_NAME = "Prefs";
     public static final int MODE_SINGLEPLAYER = 0;
     public static final int MODE_MULTIPLAYER_SHARED = 1;
     public static final int MODE_MULTIPLAYER_JOIN = 2;
     public static final int MODE_MULTIPLAYER_HOST = 3;
     private static final int PORT = 8080;
     private static final String INIT_REQUEST = "init";
     private static final String INIT_RESPONSE_OK = "init ok";
     private GameView gameView;
     private TextView status;
     private int mode;
     private int boardSize;
     private int inRow;
     private String localIp;
     private String remoteIp;
     private PrintWriter clientOut;
     private PrintWriter serverOut;
     private ServerSocket serverSocket;
     private Socket clientSocket;
     private LinearLayout gameViewHolder;
 
     @Override
     protected void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.game);
         SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
         this.mode = getIntent().getExtras().getInt("mode");
         this.remoteIp = getIntent().getExtras().get("remoteIp") != null ?
                 getIntent().getExtras().get("remoteIp").toString() : null;
         this.localIp = findIpAddress();
         this.boardSize = settings.getInt("boardSize", 3);
         this.inRow = settings.getInt("inRow", boardSize);
         this.status = (TextView) findViewById(R.id.status);
         this.gameViewHolder = (LinearLayout) findViewById(R.id.game_view_holder);
         switch (mode) {
             case MODE_SINGLEPLAYER: {
                 createGameView(boardSize, inRow);
                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                 break;
             }
             case MODE_MULTIPLAYER_SHARED: {
                 createGameView(boardSize, inRow);
                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                 break;
             }
             case MODE_MULTIPLAYER_HOST: {
                 createGameView(boardSize, inRow);
                 gameView.setEnabled(false);
                 serverThread.start();
                 break;
             }
             case MODE_MULTIPLAYER_JOIN: {
                 clientThread.start();
                 break;
             }
         }
     }
 
     private void createGameView(int boardSize, int inRow) {
         this.gameView = new GameView(this, null, boardSize, inRow);
         this.gameView.setFocusable(true);
         this.gameView.setFocusableInTouchMode(true);
         this.gameView.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) {
                     return true;
                 } else if (action == MotionEvent.ACTION_UP) {
                     int sxy = gameView.getSxy();
                     int x = (int) event.getX() / sxy;
                     int y = (int) event.getY() / sxy;
                     if (gameView.isEnabled() && x >= 0 && x < gameView.getBoardSize() && y >= 0 & y < gameView.getBoardSize()) {
                         if (gameView.getBoard().get(x, y) == GamePlayer.EMPTY) {
                             setCell(x, y, gameView.getBoard().getCurrentPlayer());
                         }
                     }
                     return true;
                 }
                 return false;
             }
         });
         gameViewHolder.addView(gameView);
     }
 
     private void setCell(int x, int y, GamePlayer player) {
         if (gameView.getBoard().put(x, y, player) == GameState.VALID_MOVE) {
             switch (mode) {
                 case MODE_SINGLEPLAYER: {
                     gameView.setEnabled(false);
                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                     break;
                 }
                 case MODE_MULTIPLAYER_SHARED: {
                     if (gameView.getBoard().getState() == GameState.NEUTRAL) {
                         if (gameView.getBoard().getCurrentPlayer() == GamePlayer.PLAYER1) {
                             gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                             status.setText("Player 2's turn");
                         } else {
                             gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                             status.setText("Player 1's turn");
                         }
                     }
                     break;
                 }
                 case MODE_MULTIPLAYER_HOST: {
                     serverOut.printf("%d %d\n", x, y);
                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                     break;
                 }
                 case MODE_MULTIPLAYER_JOIN: {
                     clientOut.printf("%d %d\n", x, y);
                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                     break;
                 }
             }
             GameState s = gameView.getBoard().getState();
             switch (s) {
                 case WIN: {
                     gameView.setEnabled(false);
                     status.setText(String.format(getResources().getString(R.string.win),
                             player.toString()));
                     break;
                 }
                 case DRAW: {
                     gameView.setEnabled(false);
                     status.setText(R.string.draw);
                     break;
                 }
             }
         }
         gameView.invalidate();
     }
 
     private String findIpAddress() {
         final WifiInfo wifiInfo = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo();
         return Formatter.formatIpAddress(wifiInfo.getIpAddress());
     }
 
     private final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             final String s = msg.getData().getString("message");
             if (INIT_REQUEST.equals(s)) {
                 Log.d(TAG, "Sent init request");
                 serverOut.printf("%s %d %d\n", INIT_REQUEST, boardSize, inRow);
             } else {
                 status.setText(s);
             }
         }
     };
 
     private void sendMessage(String s) {
         final Message msg = handler.obtainMessage();
         final Bundle bundle = new Bundle();
         bundle.putString("message", s);
         msg.setData(bundle);
         handler.sendMessage(msg);
     }
 
     private void sendMessage(int resId) {
         sendMessage(getResources().getString(resId));
     }
 
     private int[] parseSize(String line) {
         String[] words = line.split(" ");
         return new int[]{Integer.parseInt(words[1]), Integer.parseInt(words[2])};
     }
 
     private int[] parseMove(String line) {
         String[] words = line.split(" ");
         return new int[]{Integer.parseInt(words[0]), Integer.parseInt(words[1])};
     }
 
     private final Thread clientThread = new Thread() {
         @Override
         public void run() {
             try {
                 clientSocket = new Socket(remoteIp, PORT);
             } catch (IOException e) {
                 Log.e(TAG, "IOException", e);
             }
             if (clientSocket == null) {
                 sendMessage(R.string.could_not_connect);
                 return;
             }
             BufferedReader in = null;
             try {
                 clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                 in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 String line;
                 while ((line = in.readLine()) != null) {
                     Log.d(TAG, "Client thread received: " + line);
                     if (line.startsWith(INIT_REQUEST)) {
                         final int[] boardParams = parseSize(line);
                         clientOut.println(INIT_RESPONSE_OK);
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 createGameView(boardParams[0], boardParams[1]);
                                 gameView.setEnabled(false);
                                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                             }
                         });
                     } else {
                         final int[] xy = parseMove(line);
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 gameView.getBoard().put(xy[0], xy[1], GamePlayer.PLAYER1);
                                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                                 gameView.setEnabled(true);
                             }
                         });
                         gameView.postInvalidate();
                     }
                 }
             } catch (IOException e) {
                 Log.e(TAG, "IOException", e);
                 sendMessage(R.string.connection_failed);
             } finally {
                 closeInAndOut(in, clientOut);
             }
         }
     };
     private final Thread serverThread = new Thread() {
         @Override
         public void run() {
             try {
                 serverSocket = new ServerSocket(PORT);
                 sendMessage(String.format(getResources().getString(R.string.waiting_for_player), localIp));
             } catch (IOException e) {
                 Log.e(TAG, "IOException", e);
             }
             if (serverSocket == null) {
                 sendMessage(R.string.server_socket_failed);
                 Log.e(TAG, "Server socket is null");
                 return;
             }
             while (true) {
                 Socket clientSocket = null;
                 try {
                     clientSocket = serverSocket.accept();
                     Log.d(TAG, "Received client connection");
                     sendMessage(R.string.connected);
                 } catch (IOException e) {
                     Log.e(TAG, "IOException", e);
                 }
                 if (clientSocket == null) {
                     sendMessage(R.string.client_socket_failed);
                     Log.e(TAG, "Client socket is null");
                     return;
                 }
                 BufferedReader in = null;
                 try {
                     serverOut = new PrintWriter(clientSocket.getOutputStream(), true);
                     sendMessage(INIT_REQUEST);
                     in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     String line;
                     while ((line = in.readLine()) != null) {
                         Log.d(TAG, "Server thread received: " + line);
                         if (INIT_RESPONSE_OK.equals(line)) {
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                                     gameView.setEnabled(true);
                                 }
                             });
                         } else {
                             final int[] xy = parseMove(line);
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     gameView.getBoard().put(xy[0], xy[1], GamePlayer.PLAYER2);
                                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                                     gameView.setEnabled(true);
                                 }
                             });
                         }
                         gameView.postInvalidate();
                     }
                 } catch (IOException e) {
                     Log.w(TAG, "IOException", e);
                     sendMessage(R.string.connection_failed);
                 } finally {
                     closeInAndOut(in, serverOut);
                 }
             }
         }
     };
 
     private void closeInAndOut(Reader in, Writer out) {
         if (in != null) {
             try {
                 in.close();
             } catch (IOException e) {
                 Log.w(TAG, "IOException", e);
             }
         }
         if (out != null) {
             try {
                 out.close();
             } catch (IOException e) {
                 Log.w(TAG, "IOException", e);
             }
         }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         if (clientSocket != null && !clientSocket.isClosed()) {
             try {
                 clientSocket.close();
             } catch (IOException e) {
                 Log.w(TAG, "Could not close clientSocket", e);
             }
         }
         if (serverSocket != null && !serverSocket.isClosed()) {
             try {
                 serverSocket.close();
             } catch (IOException e) {
                 Log.w(TAG, "Could not close serverSocket", e);
             }
         }
     }
 }
