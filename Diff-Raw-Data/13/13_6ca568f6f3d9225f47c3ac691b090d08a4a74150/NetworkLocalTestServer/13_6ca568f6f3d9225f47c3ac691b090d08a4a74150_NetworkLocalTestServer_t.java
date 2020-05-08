 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net.server;
 
 import brutes.net.Network;
 import brutes.net.Protocol;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Karl
  */
 public class NetworkLocalTestServer extends Network{
     public NetworkLocalTestServer(Socket connection) throws IOException{
         super(connection);
     }
     
     public synchronized void read() throws IOException{
         
         try { //server delay for tests
             wait(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         this.getReader().readMessageSize();
         byte disc = this.getReader().readDiscriminant();
         switch(disc){
             case Protocol.D_CHEAT_FIGHT_LOOSE:
                 this.readCheatFightLoose();
                 break;
             case Protocol.D_CHEAT_FIGHT_RANDOM:
                 this.readCheatFightRandom();
                 break;
             case Protocol.D_CHEAT_FIGHT_WIN:
                 this.readCheatFightWin();
                 break;
             case Protocol.D_CREATE_CHARACTER:
                 this.readCreateCharacter();
                 break;
             case Protocol.D_UPDATE_CHARACTER:
                 this.readUpdateCharacter();
                 break;
             case Protocol.D_DELETE_CHARACTER:
                 this.readDeleteCharacter();
                 break;
             case Protocol.D_DO_FIGHT:
                 this.readDoFight();
                 break;
             case Protocol.D_GET_BONUS:
                 this.readDataBonus();
                 break;
             case Protocol.D_GET_CHALLENGER_CHARACTER_ID:
                 this.readGetChallengerCharacterId();
                 break;
             case Protocol.D_GET_CHARACTER:
                 this.readDataCharacter();
                 break;
             case Protocol.D_GET_MY_CHARACTER_ID:
                 this.readGetMyCharacterId();
                 break;
             case Protocol.D_LOGIN:
                 this.readLogin();
                 break;
             case Protocol.D_LOGOUT:
                 this.readLogout();
                 break;
             default:
                 this.getWriter().writeDiscriminant(Protocol.ERROR_SRLY_WTF).send();
         }
     }
     
     private void readCheatFightWin() throws IOException{
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(true)
                 .send();
     }
     private void readCheatFightLoose() throws IOException{
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(false)
                 .send();
     }
     private void readCheatFightRandom() throws IOException{
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(Math.random() < 0.5)
                 .send();
     }
     private void readDoFight() throws IOException{
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(true)
                 .send();
     }
      
     private void readLogin() throws IOException{
         String login = this.getReader().readString();
         String password = this.getReader().readString();
         
         if(login.isEmpty()){
             this.getWriter().writeDiscriminant(Protocol.ERROR_LOGIN_NOT_FOUND)
                     .send();
         }
         else if(password.isEmpty()){
             this.getWriter().writeDiscriminant(Protocol.ERROR_WRONG_PASSWORD)
                     .send();
         }
         else{
             String token = login + '@' + password;
             this.getWriter().writeDiscriminant(Protocol.R_LOGIN_SUCCESS)
                     .writeString(token)
                     .send();
         }
     }
     private void readLogout() throws IOException{
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_LOGOUT_SUCCESS)
                 .send();
     }
 
     private void readCreateCharacter() throws IOException {
         this.getReader().readString();
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
     private void readUpdateCharacter() throws IOException {
         this.getReader().readString();
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
     private void readDeleteCharacter() throws IOException {
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
    private void readDataBonus() throws IOException {
        int id = this.getReader().readLongInt();
         this.getWriter().writeDiscriminant(Protocol.R_DATA_BONUS)
                .writeLongInt(id)
                 .writeString("Hache")
                 .writeShortInt((short)2)
                 .writeShortInt((short)5)
                 .writeShortInt((short)7)
                 .writeLongInt(2)
                 .send();
     }
 
    private void readDataCharacter() throws IOException {
        int id = this.getReader().readLongInt();
         this.getWriter().writeDiscriminant(Protocol.R_DATA_CHARACTER)
                .writeLongInt(id)
                 .writeString("Rauks")
                 .writeShortInt((short)15)
                 .writeShortInt((short)17)
                 .writeShortInt((short)6)
                 .writeShortInt((short)12)
                 .writeLongInt(1)
                 .writeLongIntArray(new int[]{1, 1})
                 .send();
     }
 
     private void readGetChallengerCharacterId() throws IOException {
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_CHARACTER)
                 .writeLongInt(1)
                 .send();
     }
     private void readGetMyCharacterId() throws IOException {
         this.getReader().readString();
         this.getWriter().writeDiscriminant(Protocol.R_CHARACTER)
                 .writeLongInt(2)
                 .send();
     }
 }
