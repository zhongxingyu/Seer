 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net.server;
 
 import brutes.Brutes;
 import brutes.db.DatasManager;
 import brutes.db.entity.BonusEntity;
 import brutes.db.entity.CharacterEntity;
 import brutes.db.entity.FightEntity;
 import brutes.db.entity.UserEntity;
 import brutes.game.Bonus;
 import brutes.game.Fight;
 import brutes.game.User;
 import brutes.net.Network;
 import brutes.net.NetworkReader;
 import brutes.net.Protocol;
 import brutes.ui;
 import java.io.IOException;
 import java.net.Socket;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Karl
  */
 public class NetworkLocalTestServer extends Network {
 
     protected String token;
 
     public NetworkLocalTestServer(Socket connection) throws IOException {
         super(connection);
     }
 
     protected String checkToken(String rToken) throws NetworkResponseException {
         if (!rToken.equals(this.token)) {
             //throw new Exception("Bad token: " + rToken + " - " + this.token);
             throw new NetworkResponseException(Protocol.ERROR_TOKEN);
         }
         return rToken;
     }
 
     public void read() throws IOException, SQLException {
         NetworkReader r = this.getReader();
         r.readMessageSize();
         byte disc = this.getReader().readDiscriminant();
         try {
             switch (disc) {
                 case Protocol.D_CHEAT_FIGHT_LOOSE:
                     // token, newName
                     this.readCheatFightLoose(r.readString());
                     break;
                 case Protocol.D_CHEAT_FIGHT_RANDOM:
                     // token, newName
                     this.readCheatFightRandom(r.readString());
                     break;
                 case Protocol.D_CHEAT_FIGHT_WIN:
                     // token, newName
                     this.readCheatFightWin(r.readString());
                     break;
                 case Protocol.D_CREATE_CHARACTER:
                     // token, name
                     this.readCreateCharacter(r.readString(), r.readString());
                     break;
                 case Protocol.D_UPDATE_CHARACTER:
                     // token, newName
                     this.readUpdateCharacter(r.readString(), r.readString());
                     break;
                 case Protocol.D_DELETE_CHARACTER:
                     // token
                     this.readDeleteCharacter(r.readString());
                     break;
                 case Protocol.D_DO_FIGHT:
                     // token
                     this.readDoFight(r.readString());
                     break;
                 case Protocol.D_GET_BONUS:
                     // token
                     this.readDataBonus(r.readLongInt());
                     break;
                 case Protocol.D_GET_CHALLENGER_CHARACTER_ID:
                     // token
                     this.readGetChallengerCharacterId(r.readString());
                     break;
                 case Protocol.D_GET_CHARACTER:
                     // token
                     this.readDataCharacter(r.readLongInt());
                     break;
                 case Protocol.D_GET_MY_CHARACTER_ID:
                     // token
                     this.readGetMyCharacterId(r.readString());
                     break;
                 case Protocol.D_LOGIN:
                     // pseudo, password
                     this.readLogin(r.readString(), r.readString());
                     break;
                 case Protocol.D_LOGOUT:
                     // token
                     this.readLogout(r.readString());
                     break;
                 default:
                     throw new NetworkResponseException(Protocol.ERROR_SRLY_WTF);
             }
         } catch (NetworkResponseException e) {
             this.getWriter().writeDiscriminant(e.getError()).send();
         }
     }
 
     private void readCheatFightWin(String token) throws IOException, SQLException {
         User user = UserEntity.findByToken(token);
         Fight fight = FightEntity.findByUser(user);
         
         fight.setWinner(fight.getCharacter1());
         DatasManager.save(fight);
 
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(true)
                 .send();
     }
 
     private void readCheatFightLoose(String token) throws IOException, SQLException {
         User user = UserEntity.findByToken(token);
         Fight fight = FightEntity.findByUser(user);
 
         fight.setWinner(fight.getCharacter2());
         DatasManager.save(fight);
 
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(false)
                 .send();
     }
 
     private void readCheatFightRandom(String token) throws IOException, SQLException {
         if (Math.random() < 0.5) {
             this.readCheatFightLoose(token);
         } else {
             this.readCheatFightWin(token);
         }
     }
 
     private void readDoFight(String token) throws IOException {
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(true)
                 .send();
     }
 
     private void readLogin(String login, String password) throws IOException, SQLException, NetworkResponseException {
 
         if (login.isEmpty()) {
             throw new NetworkResponseException(Protocol.ERROR_LOGIN_NOT_FOUND);
         } else if (password.isEmpty()) {
             throw new NetworkResponseException(Protocol.ERROR_WRONG_PASSWORD);
         } else {
             PreparedStatement psql = DatasManager.prepare("SELECT id, password FROM users WHERE pseudo = ?");
             psql.setString(1, login);
             ResultSet rs = psql.executeQuery();
 
             if (!rs.next()) {
                 throw new NetworkResponseException(Protocol.ERROR_LOGIN_NOT_FOUND);
             } else {
                 if (!password.equals(rs.getString("password"))) {
                     throw new NetworkResponseException(Protocol.ERROR_WRONG_PASSWORD);
                 } else {
                     this.token = UserEntity.updateToken(rs.getInt("id"));
 
                     Logger.getLogger(Brutes.class.getName()).log(Level.INFO, "New token [{0}] for user [{1}]", new Object[]{this.token, rs.getInt("id")});
                     this.getWriter().writeDiscriminant(Protocol.R_LOGIN_SUCCESS)
                             .writeString(this.token)
                             .send();
                 }
             }
         }
     }
 
     private void readLogout(String token) throws IOException, SQLException {
         PreparedStatement psql = DatasManager.prepare("UPDATE users SET token = NULL WHERE token = ?");
         psql.setString(1, token);
         psql.executeUpdate();
 
         this.getWriter().writeDiscriminant(Protocol.R_LOGOUT_SUCCESS)
                 .send();
     }
 
     private void readCreateCharacter(String token, String name) throws IOException, SQLException {
         User user = UserEntity.findByToken(token);
         
         short level = 1;
         short strength = (short) ui.random(3, 10);
         short speed    = (short) ui.random(3, 10);
         short life     = (short) (ui.random(10, 20) + strength/3);
         int imageID = ui.random(1, 3);
         
         brutes.game.Character character = new brutes.game.Character(0, name, level, life, strength, speed, imageID);
         character.setUserId(user.getId());
        System.out.println("NEW: " + DatasManager.insert(character));
         
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
     private void readUpdateCharacter(String token, String name) throws IOException, SQLException {
         User user = UserEntity.findByToken(token);
         brutes.game.Character character = CharacterEntity.findByUser(user);
         character.setName(name);
         DatasManager.save(character);
         
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
     private void readDeleteCharacter(String token) throws IOException, SQLException {
         User user = UserEntity.findByToken(token);
         brutes.game.Character character = CharacterEntity.findByUser(user);
         DatasManager.delete(character);
         
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
     private void readDataBonus(int id) throws IOException, SQLException, NetworkResponseException {
         Bonus bonus = BonusEntity.findById(id);
 
         if (bonus == null) {
             throw new NetworkResponseException(Protocol.ERROR_BONUS_NOT_FOUND);
         }
 
         this.getWriter().writeDiscriminant(Protocol.R_DATA_BONUS)
                 .writeLongInt(id)
                 .writeString(bonus.getName())
                 .writeShortInt((short) bonus.getLevel())
                 .writeShortInt((short) bonus.getStrength())
                 .writeShortInt((short) bonus.getSpeed())
                 .writeLongInt(id)
                 .send();
     }
 
     private void readDataCharacter(int id) throws IOException, SQLException, NetworkResponseException {
         brutes.game.Character character = CharacterEntity.findById(id);
 
         if (character == null) {
             throw new NetworkResponseException(Protocol.ERROR_CHARACTER_NOT_FOUND);
         }
 
         this.getWriter().writeDiscriminant(Protocol.R_DATA_CHARACTER)
                 .writeLongInt(id)
                .writeString(character.getName() + " #" + id)
                 .writeShortInt((short) character.getLevel())
                 .writeShortInt((short) character.getLife())
                 .writeShortInt((short) character.getStrength())
                 .writeShortInt((short) character.getSpeed())
                 .writeLongInt(id) // @TODO : image
                .writeLongIntArray(new int[]{1, 2}) // @TODO : bonus
                 .send();
     }
 
     private void readGetChallengerCharacterId(String token) throws IOException, SQLException {
         User user = UserEntity.findByToken(token);
         brutes.game.Character character = CharacterEntity.findByUser(user);
         Fight fight = FightEntity.findByUser(user);
         if (fight == null) {
             PreparedStatement psql = DatasManager.prepare("SELECT id FROM Brutes WHERE user_id <> ? ORDER BY RANDOM() LIMIT 1");
             psql.setInt(1, user.getId());
             ResultSet query = psql.executeQuery();
             query.next();
 
             psql = DatasManager.prepare("INSERT INTO fights (brute_id1, brute_id2) VALUES (?, ?)");
             psql.setInt(1, character.getId());
             psql.setInt(2, query.getInt("id")); // @TODO: random
             psql.executeUpdate();
 
             fight = FightEntity.findByUser(user);
         }
 
         this.getWriter().writeDiscriminant(Protocol.R_CHARACTER)
                 .writeLongInt(fight.getCharacter2().getId())
                 .send();
     }
 
     private void readGetMyCharacterId(String token) throws IOException, SQLException, NetworkResponseException {
         User user = UserEntity.findByToken(token);
         brutes.game.Character character = CharacterEntity.findByUser(user);
         
         if(  character == null ) {
             throw new NetworkResponseException(Protocol.ERROR_CHARACTER_NOT_FOUND);
         }
        
        System.out.println("CHARACTER:" + character.getName());
 
         this.getWriter().writeDiscriminant(Protocol.R_CHARACTER)
                 .writeLongInt(character.getId())
                 .send();
     }
 }
