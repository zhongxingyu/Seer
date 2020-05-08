 package no.hist.aitel.android.tictactoe;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class GameActivity extends Activity {
 
     public static final int MODE_SINGLEPLAYER = 0;
     public static final int MODE_MULTIPLAYER_SHARED = 1;
     public static final int MODE_MULTIPLAYER_JOIN = 2;
     public static final int MODE_MULTIPLAYER_HOST = 3;
     private static final String TAG = GameActivity.class.getSimpleName();
     private static final int LISTENING_PORT = 8080;
     private GameView gameView;
     private TextView status;
     private ServerSocket serverSocket;
     private int mode;
 
     @Override
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.game);
        mode = getIntent().getExtras().getInt("mode");
         status = (TextView) findViewById(R.id.status);
         gameView = (GameView) findViewById(R.id.game_view);
         gameView.setFocusable(true);
         gameView.setFocusableInTouchMode(true);
         gameView.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) {
                     return true;
                 } else if (action == MotionEvent.ACTION_UP) {
                     int x = (int) event.getX();
                     int y = (int) event.getY();
                     int sxy = gameView.getSxy();
                     x = (x - 0) / sxy;
                     y = (y - 0) / sxy;
                     Toast.makeText(getApplicationContext(), String.valueOf(x + " " + y), Toast.LENGTH_SHORT).show();
                     if (gameView.isEnabled() && x >= 0 && x < gameView.getBoardSize() && y >= 0 & y < gameView.getBoardSize()) {
                         int cell = x + gameView.getBoardSize() * y;
                         GamePlayer state = cell == gameView.getSelectedCell() ? gameView.getSelectedValue() : gameView.getBoard().get(x, y);
                         state = state == GamePlayer.EMPTY ? gameView.getCurrentPlayer() : GamePlayer.EMPTY;
                         gameView.setSelectedCell(cell);
                         gameView.setSelectedValue(state);
                         if (gameView.getBoard().get(x, y) == GamePlayer.EMPTY) {
                             setCell(x, y, state);
                             if (gameView.getBoard().getState() == GameState.NEUTRAL) {
                                 if (gameView.getCurrentPlayer() == GamePlayer.PLAYER1) {
                                     gameView.setCurrentPlayer(GamePlayer.PLAYER2);
                                     status.setText("Player 2's turn");
                                 } else {
                                     gameView.setCurrentPlayer(GamePlayer.PLAYER1);
                                     status.setText("Player 1's turn");
                                 }
                             }
                         }
                     }
                     return true;
                 }
                 return false;
             }
         });
         switch (mode) {
             case MODE_SINGLEPLAYER: {
                 break;
             }
             case MODE_MULTIPLAYER_SHARED: {
                 break;
             }
             case MODE_MULTIPLAYER_JOIN: {
                 break;
             }
             case MODE_MULTIPLAYER_HOST: {
                 break;
             }
         }
     }
 
     public void setCell(int x, int y, GamePlayer player) {
         if (gameView.getBoard().put(x, y, player) == GameState.VALID_MOVE) {
             GameState s = gameView.getBoard().getState();
             switch (s) {
                 case WIN: {
                     gameView.setEnabled(false);
                     status.setText(player.toString() + " WINS!");
                     break;
                 }
                 case DRAW: {
                     gameView.setEnabled(false);
                     status.setText("DRAW");
                     break;
                 }
             }
         }
         gameView.invalidate();
     }
 
     private final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             status.setText(msg.getData().getString("msg"));
         }
     };
 
     private void sendMessage(String s) {
         final Message msg = handler.obtainMessage();
         final Bundle bundle = new Bundle();
         bundle.putString("msg", s);
         msg.setData(bundle);
         handler.sendMessage(msg);
     }
 
     private void sendMessage(int resId) {
         sendMessage(getResources().getString(resId));
     }
 
     private void stopServer() {
         if (serverSocket != null && !serverSocket.isClosed()) {
             try {
                 serverSocket.close();
                 Log.d(TAG, "Closed server socket");
             } catch (IOException e) {
                 Log.w(TAG, "Could not close socket", e);
             }
         }
     }
 
     private final Thread serverThread = new Thread() {
         @Override
         public void run() {
             try {
                 serverSocket = new ServerSocket(LISTENING_PORT);
                 sendMessage(R.string.waiting_for_player);
             } catch (IOException e) {
                 Log.e(TAG, "IOException", e);
             }
             if (serverSocket == null) {
                 sendMessage(R.string.server_socket_failed);
                 Log.e(TAG, "Server socket is null");
                 return;
             }
             while (true) {
                 Socket client = null;
                 try {
                     client = serverSocket.accept();
                     sendMessage(R.string.connected);
                 } catch (IOException e) {
                     Log.e(TAG, "IOException", e);
                 }
                 if (client == null) {
                     sendMessage(R.string.client_socket_failed);
                     Log.e(TAG, "Client socket is null");
                     return;
                 }
                 try {
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                     String line;
                     while ((line = in.readLine()) != null) {
                         Log.d(TAG, String.format("Received: %s", line));
                         // do something here
                     }
                     break;
                 } catch (IOException e) {
                     sendMessage(R.string.connection_failed);
                     Log.w(TAG, "IOException", e);
                 }
             }
         }
     };
 }
 
