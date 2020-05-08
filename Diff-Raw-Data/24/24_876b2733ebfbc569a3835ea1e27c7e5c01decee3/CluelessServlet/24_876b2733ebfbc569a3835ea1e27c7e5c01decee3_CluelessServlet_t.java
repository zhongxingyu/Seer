 //This sample is how to use websocket of Tomcat.
 package Clueless;
 
 import CluelessCommands.*;
 import com.google.gson.Gson;
 import com.google.gson.stream.JsonReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.catalina.websocket.MessageInbound;
 import org.apache.catalina.websocket.StreamInbound;
 import org.apache.catalina.websocket.WebSocketServlet;
 import org.apache.catalina.websocket.WsOutbound;
 
 public class CluelessServlet extends WebSocketServlet{
     private static final long serialVersionUID = 1L;
     private static ArrayList<MyMessageInbound> mmiList = new ArrayList<MyMessageInbound>();
 
     @Override
     public StreamInbound createWebSocketInbound(String protocol, HttpServletRequest hsr){
         return new MyMessageInbound();
     }
 
     private class MyMessageInbound extends MessageInbound{
         WsOutbound myoutbound;
 
         @Override
         public void onOpen(WsOutbound outbound){
             try {
                 System.out.println("Client connected");
                 this.myoutbound = outbound;
                 mmiList.add(this);
                 //Send the client all our commands
                 StringBuilder sb = new StringBuilder();
                 for(NotificationEnum ne : NotificationEnum.values())
                    sb = sb.append(ne.toString()).append('\n');
                 outbound.writeTextMessage(CharBuffer.wrap(sb.toString()));
             } catch (IOException e) {
                 Logger.getLogger(CluelessServlet.class.getName()).log(Level.SEVERE, null, e);
             }
         }
 
         @Override
         public void onClose(int status){
             System.out.println("Client disconnected");
             //TODO: PlayerQuit
             mmiList.remove(this);
         }
 
         @Override
         public void onTextMessage(CharBuffer cb) throws IOException{
            JsonReader jr = new JsonReader(new StringReader(cb.toString()));
            Gson gson = new Gson();
            Class<? extends Command> c = null;
            
            //Read the string for the command type
            try{
             while(jr.hasNext()){
                String name = jr.nextName();
                String val = jr.nextString();
                if(name.equals("cmd")){
                   c = (Class<? extends Command>) Class.forName(val);
                   break;
                }
             }
            }
            //If invalid command, "return" exception
            catch(ClassNotFoundException cnfe){
               Logger.getLogger(CluelessServlet.class.getName()).log(Level.SEVERE, null, cnfe);
               this.myoutbound.writeTextMessage(CharBuffer.wrap(gson.toJson(cnfe)));
            }
            
            //Begin call logic
            Command cmd = gson.fromJson(cb.toString(), c);
            if(cmd instanceof CreateGame){
               CreateGame cg = (CreateGame) cmd;
               cg.id = GameManager.getInstance().createGame(cg.playStyle,
                       cg.name, cg.password, myoutbound, cg.playerLanguage);
               cmd = cg;
            }
            else if(cmd instanceof QueryGames){
               QueryGames qg = (QueryGames) cmd;
               qg.games = GameManager.getInstance()
                       .queryGames(qg.playStyle, qg.secured);
               cmd = qg;
            }
            else if(cmd instanceof JoinGame){
               JoinGame jg = (JoinGame) cmd;
               jg.joined = GameManager.getInstance().joinGame(jg.gameId,
                       myoutbound, jg.playerLanguage);
               cmd = jg;
            }
            else if(cmd instanceof GetAvailableCharacters){
               GetAvailableCharacters gac = (GetAvailableCharacters) cmd;
               gac.characters = GameManager.getInstance().getGame(gac.gameId)
                       .getAvailableCharacters();
               cmd = gac;
            }
            else if(cmd instanceof ChangeCharacter){
               ChangeCharacter cc = (ChangeCharacter) cmd;
              Set<Character> suspects = GameManager.getCharacters();
              Character ch = null;
              for(Character ch2 : suspects)
                 if(ch2.getName().equals(cc.identity))
                    ch = ch2;
              if(ch == null) ch = new Character("<Random>");
               GameManager.getInstance().getGame(cc.gameId)
                      .getPlayer(myoutbound).setCharacter(ch);
               cc.identity = GameManager.getInstance().getGame(cc.gameId)
                      .getPlayer(myoutbound).getCharacter().getName();
               cmd = cc;
            }
            else if(cmd instanceof GetAvailableLanguages){
               GetAvailableLanguages gal = (GetAvailableLanguages) cmd;
               gal.languages = NotificationManager.getInstance().getSupportedLanguages();
               cmd = gal;
            }
            else if(cmd instanceof ChangeLanguage){
               ChangeLanguage cl = (ChangeLanguage) cmd;
               GameManager.getInstance().getGame(cl.gameId).getPlayer(myoutbound)
                       .setSelectedLanguage(cl.language);
               cl.language = GameManager.getInstance().getGame(cl.gameId)
                       .getPlayer(myoutbound).getSelectedLanguage();
               cmd = cl;
            }
            else if(cmd instanceof StartGame){
               StartGame sg = (StartGame) cmd;
               sg.started = GameManager.getInstance().getGame(sg.gameId).start();
            }
            else if(cmd instanceof PlayerChat){
               PlayerChat pc = (PlayerChat) cmd;
               ArrayList args = new ArrayList();
               args.add(pc.msg);
               GameManager.getInstance().getGame(pc.gameId)
                       .notifyAllPlayers(NotificationEnum.PlayerChat, args);
            }
            else if(cmd instanceof PlayerQuit){
               PlayerQuit pq = (PlayerQuit) cmd;
               pq.quit = GameManager.getInstance().getGame(pq.gameId).removePlayer(
                         GameManager.getInstance().getGame(pq.gameId).getPlayer(myoutbound));
               
               if(GameManager.getInstance().getGame(pq.gameId).getPlayerCount() == 0)
                  GameManager.getInstance().deleteGame(pq.gameId);
               cmd = pq;
            }
            
            //Return result
            myoutbound.writeTextMessage(CharBuffer.wrap(gson.toJson(cmd, c)));
         }
 
         @Override
         public void onBinaryMessage(ByteBuffer bb) throws IOException{
            throw new UnsupportedOperationException("onBinaryMessage");
         }
     }
 }
