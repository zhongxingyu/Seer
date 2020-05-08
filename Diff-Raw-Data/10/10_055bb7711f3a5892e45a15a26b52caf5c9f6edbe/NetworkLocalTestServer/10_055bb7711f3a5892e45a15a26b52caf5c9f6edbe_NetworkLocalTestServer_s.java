 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net.server;
 
 import brutes.Brutes;
 import brutes.db.DatasManager;
 import brutes.db.entity.BonusEntity;
 import brutes.db.entity.BruteEntity;
 import brutes.db.entity.FightEntity;
 import brutes.db.entity.UserEntity;
 import brutes.game.Bonus;
 import brutes.game.Brute;
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
 
     public NetworkLocalTestServer(Socket connection) throws IOException {
         super(connection);
     }
 
     protected User checkTokenAndReturnUser(String token) throws IOException, SQLException, NetworkResponseException {
         User user = UserEntity.findByToken(token);
         if (user == null) {
             throw new NetworkResponseException(Protocol.ERROR_TOKEN);
         }
         return user;
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
                 case Protocol.D_CREATE_BRUTE:
                     // token, name
                     this.readCreateBrute(r.readString(), r.readString());
                     break;
                 case Protocol.D_UPDATE_BRUTE:
                     // token, newName
                     this.readUpdateBrute(r.readString(), r.readString());
                     break;
                 case Protocol.D_DELETE_BRUTE:
                     // token
                     this.readDeleteBrute(r.readString());
                     break;
                 case Protocol.D_DO_FIGHT:
                     // token
                     this.readDoFight(r.readString());
                     break;
                 case Protocol.D_GET_BONUS:
                     // token
                     this.readDataBonus(r.readLongInt());
                     break;
                 case Protocol.D_GET_CHALLENGER_BRUTE_ID:
                     // token
                     this.readGetChallengerBruteId(r.readString());
                     break;
                 case Protocol.D_GET_BRUTE:
                     // token
                     this.readDataBrute(r.readLongInt());
                     break;
                 case Protocol.D_GET_MY_BRUTE_ID:
                     // token
                     this.readGetMyBruteId(r.readString());
                     break;
                 case Protocol.D_LOGIN:
                     // pseudo, password
                     this.readLogin(r.readString(), r.readString());
                     break;
                 case Protocol.D_LOGOUT:
                     // token
                     this.readLogout(r.readString());
                     break;
                 /* @TODO define it !
                  * case Protocol.D_CREATE_USER:
                  *    // pseudo, password
                  *    this.readCreateUser(r.readString(), r.readString());
                  *    break;
                  * case Protocol.D_DELETE_USER:
                  *    // token
                  *    this.readDeleteUser(r.readString());
                  *    break;
                  */
                 default:
                     throw new NetworkResponseException(Protocol.ERROR_SRLY_WTF);
             }
         } catch (NetworkResponseException e) {
             this.getWriter().writeDiscriminant(e.getError()).send();
         }
     }
 
     private void readCheatFightWin(String token) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         Fight fight = FightEntity.findByUser(user);
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
         
         Brute brute = fight.getBrute1();
         switch(ui.random(1, 6))
         {
             case 1: // Level Up
                 brute.setLevel((short)(brute.getLevel()+1));
                 Logger.getLogger(Brutes.class.getName()).log(Level.INFO, "Result: +1 brute level ({0})", brute.getLevel());
                 DatasManager.save(brute);
                 break;
             case 2: // Bonus Up
             case 3: // Bonus Up
                 Bonus bonus = BonusEntity.findRandomByBrute(brute);
                 if( bonus != null )
                 {
                     bonus.setLevel((short)(bonus.getLevel()+1));
                     bonus.setStrength((short)(((double)bonus.getStrength())*(1+Math.random())/2));
                     bonus.setSpeed((short)(((double)bonus.getSpeed())*(1+Math.random())/2));
                     DatasManager.save(bonus);
                     Logger.getLogger(Brutes.class.getName()).log(Level.INFO, "Result: +1 bonus level ({0} [{1}])", new Object[]{bonus.getName(), bonus.getLevel()});
                 }
                 else
                 {
                     bonus = BonusEntity.findRandomByBrute(fight.getBrute2());
                     if( bonus != null )
                     {
                         bonus.setLevel((short) ui.random(1, (int)(brute.getLevel()/2)));
                         bonus.setStrength((short)(((double)bonus.getStrength())*(1+Math.random())/2));
                         bonus.setSpeed((short)(((double)bonus.getSpeed())*(1+Math.random())/2));
                         bonus.setBruteId(brute.getId());
                         DatasManager.insert(bonus);
                         Logger.getLogger(Brutes.class.getName()).log(Level.INFO, "Result: new bonus ({0} [{1}])", new Object[]{bonus.getName(), bonus.getLevel()});
                     }
                     
                 }
                 break;
             default: // New
                 Logger.getLogger(Brutes.class.getName()).log(Level.INFO, "Result: Nothing ...");
                 break;
         }
         
         fight.setWinner(brute);
         DatasManager.save(fight);
 
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(true)
                 .send();
     }
 
     private void readCheatFightLoose(String token) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         Fight fight = FightEntity.findByUser(user);
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
 
         fight.setWinner(fight.getBrute2());
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
         User user = this.checkTokenAndReturnUser(token);
         Fight fight = FightEntity.findByUser(user);
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
         
         int i = 0;
         int lost;
         while( fight.getBrute1().getLife() > 0 && fight.getBrute2().getLife() > 0 ) {
             
             for( int j = 0 ; j < 2 ; j++ )
             {
                 Brute ch1 = j==0 ? fight.getBrute1() : fight.getBrute2();
                 Brute ch2 = j==0 ? fight.getBrute2() : fight.getBrute1();
                 int random = ui.random(0, 10);
                 if( random == 0 ) {
                 }
                 else if( random == 1 ) {
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
                     ch2.setLife((short) (ch2.getLife() - pWin));
                 }
             }
         }
         if( fight.getBrute1().getLife() > 0 ) {
             this.readCheatFightWin(token);
         }
         else {
             this.readCheatFightLoose(token);
         }
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
                     String token = UserEntity.updateToken(rs.getInt("id"));
 
                     Logger.getLogger(Brutes.class.getName()).log(Level.INFO, "New token [{0}] for user [{1}]", new Object[]{token, rs.getInt("id")});
                     this.getWriter().writeDiscriminant(Protocol.R_LOGIN_SUCCESS)
                             .writeString(token)
                             .send();
                 }
             }
         }
     }
 
     private void readLogout(String token) throws IOException, SQLException {
         UserEntity.updateTokenToNull(token);
 
         this.getWriter().writeDiscriminant(Protocol.R_LOGOUT_SUCCESS)
                 .send();
     }
 
     private void readCreateBrute(String token, String name) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         
         // Brute already exists !
         if( BruteEntity.findByUser(user) != null ) {
             throw new NetworkResponseException(Protocol.ERROR_CREATE_BRUTE);
         }
         
         /* @TODO
          * if( BruteEntity.findByName(name) != null ) {
          *    throw new NetworkResponseException(Protocol.ERROR_BRUTE_ALREADY_USED);
          * }
          */
         
         short level = 1;
         short strength = (short) ui.random(3, 10);
         short speed    = (short) ui.random(3, 10);
         short life     = (short) (ui.random(10, 20) + strength/3);
         int imageID = ui.random(1, 3);
         
         Brute brute = new Brute(0, name, level, life, strength, speed, imageID);
         brute.setUserId(user.getId());
         DatasManager.insert(brute);
         
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
     private void readUpdateBrute(String token, String name) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         
         if( name.isEmpty() ) {
            throw new NetworkResponseException(Protocol.ERROR); // @TODO Protocol.ERROR_INPUT_DATAS
         }
         
         /* @TODO define it !
          * if( BruteEntity.findByName(name) != null ) {
          *    throw new NetworkResponseException(Protocol.ERROR_BRUTE_ALREADY_USED);
          * }
          */
         
         Brute brute = BruteEntity.findByUser(user);
         brute.setName(name);
         DatasManager.save(brute);
         
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
     private void readDeleteBrute(String token) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         Brute brute = BruteEntity.findByUser(user);
         
         if (brute == null) {
             throw new NetworkResponseException(Protocol.ERROR_BRUTE_NOT_FOUND);
         }
         
         DatasManager.delete(brute);
         
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
 
     private void readDataBrute(int id) throws IOException, SQLException, NetworkResponseException {
         Brute brute = BruteEntity.findById(id);
 
         if (brute == null) {
             throw new NetworkResponseException(Protocol.ERROR_BRUTE_NOT_FOUND);
         }
         
         brute.setBonuses(BonusEntity.findAllByBrute(brute));
 
         this.getWriter().writeDiscriminant(Protocol.R_DATA_BRUTE)
                 .writeLongInt(id)
                 .writeString(brute.getName())
                 .writeShortInt((short) brute.getLevel())
                 .writeShortInt((short) brute.getLife())
                 .writeShortInt((short) brute.getStrength())
                 .writeShortInt((short) brute.getSpeed())
                 .writeLongInt(id) // @TODO : image
                 .writeLongIntArray(brute.getBonusesIDs())
                 .send();
     }
 
     private void readGetChallengerBruteId(String token) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         Brute brute = BruteEntity.findByUser(user);
         
         if( brute == null ) {
             throw new NetworkResponseException(Protocol.ERROR_BRUTE_NOT_FOUND);
         }
         
         Fight fight = FightEntity.findByUser(user);
         
         if (fight == null) {
             Brute otherBrute = BruteEntity.findRandomAnotherToBattleByUser(user);
             
             if( otherBrute == null ) {
                 throw new NetworkResponseException(Protocol.ERROR_FIGHT);
             }
             
             fight = new Fight();
             fight.setBrute1(brute);
             fight.setBrute2(otherBrute);
             DatasManager.insert(fight);
         }
         
         if( fight == null ) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
 
         this.getWriter().writeDiscriminant(Protocol.R_BRUTE)
                 .writeLongInt(fight.getBrute2().getId())
                 .send();
     }
 
     private void readGetMyBruteId(String token) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         Brute brute = BruteEntity.findByUser(user);
         
         if(  brute == null ) {
             throw new NetworkResponseException(Protocol.ERROR_BRUTE_NOT_FOUND);
         }
 
         this.getWriter().writeDiscriminant(Protocol.R_BRUTE)
                 .writeLongInt(brute.getId())
                 .send();
     }
     
     private void readCreateUser(String pseudo, String password) throws IOException, SQLException, NetworkResponseException {
         if( pseudo.isEmpty() || password.isEmpty() ) {
             throw new NetworkResponseException(Protocol.ERROR); // @TODO Protocol.ERROR_INPUT_DATAS
         }
         
         
         /* @TODO define it !
          * if( UserEntity.findByPseudo(pseudo) != null ) {
          *    throw new NetworkResponseException(Protocol.ERROR_USER_ALREADY_USED);
          * }
          */
         
         User user = new User(0, pseudo, password, null);
         DatasManager.insert(user);
         
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
         
     }
     
     private void readDeleteUser(String token) throws IOException, SQLException, NetworkResponseException {
         User user = this.checkTokenAndReturnUser(token);
         DatasManager.delete(user);
     
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 }
