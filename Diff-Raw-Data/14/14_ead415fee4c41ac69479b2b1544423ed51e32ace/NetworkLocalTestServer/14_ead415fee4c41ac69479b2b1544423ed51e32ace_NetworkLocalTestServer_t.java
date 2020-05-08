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
 import brutes.game.Character;
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
 
     private void readCheatFightWin(String token) throws IOException, SQLException, NetworkResponseException {
         User user = UserEntity.findByToken(token);
         Fight fight = FightEntity.findByUser(user);
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
         
         fight.setWinner(fight.getCharacter1());
         DatasManager.save(fight);
 
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(true)
                 .send();
     }
 
     private void readCheatFightLoose(String token) throws IOException, SQLException, NetworkResponseException {
         User user = UserEntity.findByToken(token);
         Fight fight = FightEntity.findByUser(user);
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
 
         fight.setWinner(fight.getCharacter2());
         DatasManager.save(fight);
 
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(false)
                 .send();
     }
 
     private void readCheatFightRandom(String token) throws IOException, SQLException, NetworkResponseException {
         if (Math.random() < 0.5) {
             this.readCheatFightLoose(token);
         } else {
             this.readCheatFightWin(token);
         }
     }
 
     private void readDoFight(String token) throws IOException, SQLException, NetworkResponseException {
         User user = UserEntity.findByToken(token);
         Fight fight = FightEntity.findByUser(user);
         System.out.println("[DoFight]");
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
         
         System.out.println("SET ch" + fight.getCharacter1().getId() + "[life=" + fight.getCharacter1().getLife() + "] VS ch" + fight.getCharacter2().getId() + "[life=" + fight.getCharacter2().getLife() + "]");
         
         int i = 0;
         int lost;
         while( fight.getCharacter1().getLife() > 0 && fight.getCharacter2().getLife() > 0 ) {
             System.out.println("FIGHT: " + (++i) + " ");
             System.out.println("\tBrute[" + fight.getCharacter1().getName() + "] (" + fight.getCharacter1().getLife() + "pv) VS Brute[" + fight.getCharacter2().getName() + "] (" + fight.getCharacter2().getLife() + "pv)");
             
             for( int j = 0 ; j < 2 ; j++ )
             {
                 Character ch1 = j==0 ? fight.getCharacter1() : fight.getCharacter2();
                 Character ch2 = j==0 ? fight.getCharacter2() : fight.getCharacter1();
                 int random = ui.random(0, 10);
                 
                System.out.print("\t\tBrute[" + ch1.getName() + "] ");
                 if( random == 0 ) {
                     System.out.println("rate son attaque ...");
                 }
                 else if( random == 1 ) {
                     System.out.println("est enragé et gagne +1 dans chaque compétance !");
                     ch1.setSpeed((short) (ch1.getSpeed()+1));
                     ch1.setStrength((short) (ch1.getStrength()+1));
                 }
                 else {
                     double pWin = ((double)(10*ch1.getLevel() + ch1.getStrength()));
                     pWin *= ((double) ch1.getSpeed()/((double)(1+ch1.getSpeed()+ch2.getSpeed())));
                     pWin *= ((double) ch1.getStrength()/((double)(1+ch1.getStrength()+ch2.getStrength())));
                     pWin *= 1+((double) ch1.getLife()/((double)(1+ch1.getLife()+ch2.getLife())));
                     // DEBUG
                     //System.out.println("@@" + pWin);
                     //System.out.println("@@ 100*(10*" + ch1.getLevel() + "+" + ch1.getStrength() + ")*(" + ch1.getSpeed() + "/(1+" + ch1.getSpeed() + "+" + ch2.getSpeed() + ")");
                     //System.out.println(ch1.getLife() + "/(1+" + ch1.getLife() + "+" + ch2.getLife() + ")");
                     ch2.setLife((short) (fight.getCharacter2().getLife() - pWin));
                     
                     System.out.println("attaque. ATK " + ((short) pWin) + " pv");
                 }
             }
         }
         System.out.println("\tBrute[" + fight.getCharacter1().getName() + "] (" + fight.getCharacter1().getLife() + "pv) VS Brute[" + fight.getCharacter2().getName() + "] (" + fight.getCharacter2().getLife() + "pv)");
         
         System.out.println("\t\tVous " + (fight.getCharacter1().getLife() > 0 ? "gagnez" : "perdez"));
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(fight.getCharacter1().getLife() > 0)
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
 
     private void readCreateCharacter(String token, String name) throws IOException, SQLException, NetworkResponseException {
         User user = UserEntity.findByToken(token);
         
         // Character already exists !
         if( CharacterEntity.findByUser(user) != null ) {
             throw new NetworkResponseException(Protocol.ERROR_CREATE_CHARACTER);
         }
         
         short level = 1;
         short strength = (short) ui.random(3, 10);
         short speed    = (short) ui.random(3, 10);
         short life     = (short) (ui.random(10, 20) + strength/3);
         int imageID = ui.random(1, 3);
         
         brutes.game.Character character = new brutes.game.Character(0, name, level, life, strength, speed, imageID);
         character.setUserId(user.getId());
         DatasManager.insert(character);
         
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
         
         character.setBonuses(BonusEntity.findAllByCharacter(character));
 
         this.getWriter().writeDiscriminant(Protocol.R_DATA_CHARACTER)
                 .writeLongInt(id)
                 .writeString(character.getName())
                 .writeShortInt((short) character.getLevel())
                 .writeShortInt((short) character.getLife())
                 .writeShortInt((short) character.getStrength())
                 .writeShortInt((short) character.getSpeed())
                 .writeLongInt(id) // @TODO : image
                 .writeLongIntArray(character.getBonusesIDs())
                 .send();
     }
 
     private void readGetChallengerCharacterId(String token) throws IOException, SQLException, NetworkResponseException {
         User user = UserEntity.findByToken(token);
         brutes.game.Character character = CharacterEntity.findByUser(user);
         
         if( character == null ) {
             throw new NetworkResponseException(Protocol.ERROR_CHARACTER_NOT_FOUND);
         }
         
         Fight fight = FightEntity.findByUser(user);
         
         if (fight == null) {
             PreparedStatement psql = DatasManager.prepare("SELECT id FROM Brutes WHERE user_id <> ? ORDER BY RANDOM() LIMIT 1");
             psql.setInt(1, user.getId());
             ResultSet query = psql.executeQuery();
             
             if( !query.next() ) {
                 throw new NetworkResponseException(Protocol.ERROR_FIGHT);
             }
             
             fight = new Fight();
             fight.setCharacter1(character);
             fight.setCharacter2(CharacterEntity.findById(query.getInt("id")));
             DatasManager.insert(fight);
 
             /*psql = DatasManager.prepare("INSERT INTO fights (brute_id1, brute_id2) VALUES (?, ?)");
             psql.setInt(1, character.getId());
             psql.setInt(2, query.getInt("id")); // @TODO: random
             psql.executeUpdate();*/
 
             //fight = FightEntity.findByUser(user);
         }
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
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
 
         this.getWriter().writeDiscriminant(Protocol.R_CHARACTER)
                 .writeLongInt(character.getId())
                 .send();
     }
 }
