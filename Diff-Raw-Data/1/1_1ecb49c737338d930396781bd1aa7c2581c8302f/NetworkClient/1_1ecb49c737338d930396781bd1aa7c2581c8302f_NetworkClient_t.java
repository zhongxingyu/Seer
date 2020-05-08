 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net.client;
 
 import brutes.gui.FightController;
 import brutes.gui.LoginController;
 import brutes.ScenesContext;
 import brutes.game.Bonus;
 import brutes.game.Character;
 import brutes.net.Network;
 import brutes.net.Protocol;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Karl
  */
 public class NetworkClient extends Network{
     public NetworkClient(Socket connection) throws IOException{
         super(connection);
     }
 
     public String sendLogin(String user, String password) throws IOException, ErrorResponseException, InvalidResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_LOGIN)
                 .writeString(user)
                 .writeString(password)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_LOGIN_SUCCESS:
                 return this.getReader().readString();
             case Protocol.ERROR_LOGIN_NOT_FOUND:
                 throw new ErrorResponseException(ErrorResponseException.LOGIN_NOT_FOUND);
             case Protocol.ERROR_WRONG_PASSWORD:
                 throw new ErrorResponseException(ErrorResponseException.WRONG_PASSWORD);
             default:
                 throw new InvalidResponseException();
         }
     }
     public void sendLogout(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_LOGOUT)
                 .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_LOGOUT_SUCCESS:
                 break;
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(ErrorResponseException.TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     
     public void sendCreateCharacter(String token, String name) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_CREATE_CHARACTER)
                 .writeString(token)
                 .writeString(name)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_ACTION_SUCCESS:
                 break;
             case Protocol.ERROR_CREATE_CHARACTER:
                 throw new ErrorResponseException(ErrorResponseException.CREATE_CHARACTER);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(ErrorResponseException.TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public void sendUpdateCharacter(String token, String name) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_UPDATE_CHARACTER)
                 .writeString(token)
                 .writeString(name)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_ACTION_SUCCESS:
                 break;
             case Protocol.ERROR_UPDATE_CHARACTER:
                 throw new ErrorResponseException(ErrorResponseException.UPDATE_CHARACTER);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(ErrorResponseException.TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public void sendDeleteCharacter(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_DELETE_CHARACTER)
                .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_ACTION_SUCCESS:
                 break;
             case Protocol.ERROR_DELETE_CHARACTER:
                 throw new ErrorResponseException(ErrorResponseException.UPDATE_CHARACTER);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(ErrorResponseException.TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     
     private int sendGetCharacterId(byte getIdDiscriminant, String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(getIdDiscriminant)
                 .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_CHARACTER:
                 return this.getReader().readLongInt();
             case Protocol.ERROR_CHARACTER_NOT_FOUND:
                 throw new ErrorResponseException(ErrorResponseException.CHARACTER_NOT_FOUND);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(ErrorResponseException.TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public int sendGetMyCharacterId(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return this.sendGetCharacterId(Protocol.D_GET_MY_CHARACTER_ID, token);
     }
     public int sendGetChallengerCharacterId(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return this.sendGetCharacterId(Protocol.D_GET_CHALLENGER_CHARACTER_ID, token);
     }
     
     private boolean sendFight(byte fightType, String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(fightType)
                 .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_FIGHT_RESULT:
                 return this.getReader().readBoolean();
             case Protocol.ERROR_FIGHT:
                 throw new ErrorResponseException(ErrorResponseException.TOKEN);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(ErrorResponseException.TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public boolean sendCheatFightWin(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_CHEAT_FIGHT_WIN, token);
     }
     public boolean sendCheatFightLoose(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_CHEAT_FIGHT_LOOSE, token);
     }
     public boolean sendCheatFightRandom(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_CHEAT_FIGHT_RANDOM, token);
     }
     public boolean sendDoFight(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_DO_FIGHT, token);
     }
     public brutes.game.Character getDataCharacter(int id) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_GET_CHARACTER)
                 .writeLongInt(id)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_DATA_CHARACTER:
                 int chId = this.getReader().readLongInt();
                 String name = this.getReader().readString();
                 short level = this.getReader().readShortInt();
                 short life = this.getReader().readShortInt();
                 short strength = this.getReader().readShortInt();
                 short speed = this.getReader().readShortInt();
                 int imageID = this.getReader().readLongInt();
                 int[] bonusesID = this.getReader().readLongIntArray();
                 Bonus[] bonuses = new Bonus[Character.MAX_BONUSES];
                 for(int i = 0; i < Character.MAX_BONUSES; i++){
                     if(bonusesID.length > i){
                         try (NetworkClient connection = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
                             try {
                                 bonuses[i] = connection.getDataBonus(bonusesID[i]);
                             } catch (InvalidResponseException | ErrorResponseException ex) {
                                 Logger.getLogger(FightController.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         } catch (IOException ex) {
                             Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                     else{
                         bonuses[i] = Bonus.EMPTY_BONUS;
                     }
                 }
                 return new brutes.game.Character(chId, name, level, life, strength, speed, imageID, bonuses);
             case Protocol.ERROR_CHARACTER_NOT_FOUND:
                 throw new ErrorResponseException(ErrorResponseException.CHARACTER_NOT_FOUND);
             default:
                 throw new InvalidResponseException();
         }
     }
     public Bonus getDataBonus(int id) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_GET_BONUS)
                 .writeLongInt(id)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_DATA_BONUS:
                 int boId = this.getReader().readLongInt();
                 String name = this.getReader().readString();
                 short level = this.getReader().readShortInt();
                 short strength = this.getReader().readShortInt();
                 short speed = this.getReader().readShortInt();
                 int imageID = this.getReader().readShortInt();
                 return new Bonus(boId, name, level, strength, speed, imageID);
             case Protocol.ERROR_BONUS_NOT_FOUND:
                 throw new ErrorResponseException(ErrorResponseException.BONUS_NOT_FOUND);
             default:
                 throw new InvalidResponseException();
         }
     }
 }
