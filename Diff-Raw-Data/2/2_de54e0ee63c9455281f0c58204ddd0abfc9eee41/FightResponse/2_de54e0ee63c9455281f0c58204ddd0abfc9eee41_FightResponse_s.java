 package brutes.server.net.response;
 
 import brutes.net.NetworkWriter;
 import brutes.net.Protocol;
 import brutes.server.DoFight;
 import brutes.server.db.DatasManager;
 import brutes.server.db.entity.BonusEntity;
 import brutes.server.db.entity.BruteEntity;
 import brutes.server.db.entity.FightEntity;
 import brutes.server.db.entity.NotFoundEntityException;
 import brutes.server.game.Bonus;
 import brutes.server.game.Brute;
 import brutes.server.game.Fight;
 import brutes.server.game.User;
 import brutes.server.net.NetworkResponseException;
 import brutes.server.ServerMath;
 import java.io.IOException;
 import java.sql.SQLException;
 
 /**
  *
  * @author Olivares Georges <dev@olivares-georges.fr>
  */
 public class FightResponse extends Response {
 
     public FightResponse(NetworkWriter writer) {
         super(writer);
     }
 
     static public Fight getFightWithChallenger(User user) throws IOException, SQLException, NetworkResponseException {
         try {
             Brute brute = BruteEntity.findByUser(user);
 
             Fight fight;
             try {
                 return FightEntity.findByUser(user);
             } catch (NotFoundEntityException ex) {
                 Brute otherBrute = BruteEntity.findOneRandomAnotherToBattleByUser(user);
 
                 fight = new Fight();
                 fight.setBrute1(brute);
                 fight.setBrute2(otherBrute);
                 return DatasManager.insert(fight);
             }
         } catch (NotFoundEntityException ex) {
             throw new NetworkResponseException(Protocol.ERROR_BRUTE_NOT_FOUND);
         }
     }
 
     public void readCheatFightWin(String token) throws IOException, SQLException, NetworkResponseException {
         User user = FightResponse.checkTokenAndReturnUser(token);
         Fight fight = FightResponse.getFightWithChallenger(user);
 
         if (fight == null) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
 
         Brute brute = fight.getBrute1();
 
        if (brute.getLevel() <= 100) {
             // level UP !
             brute.setLevel((short) (brute.getLevel() + 1));
 
             // stats UP !
             switch (ServerMath.random(2)) {
                 case 0:
                     brute.setLife((short) (brute.getLife() + ServerMath.random(1, 5)));
                     break;
                 case 1:
                     brute.setSpeed((short) (brute.getSpeed() + ServerMath.random(1, 5)));
                     break;
                 case 2:
                     brute.setStrength((short) (brute.getStrength() + ServerMath.random(1, 5)));
                     break;
             }
         }
 
         // bonus UP/RM
         // 1/3 : perte # 2/3 nouveau # action sur un des trois bonus, existant ou non.
         brute.setBonus(ServerMath.random(0, Brute.MAX_BONUSES - 1), ServerMath.random(1, 3) == 1 ? Bonus.EMPTY_BONUS : BonusEntity.findMathematicalRandom());
 
         DatasManager.save(brute);
         fight.setWinner(brute);
         DatasManager.save(fight);
 
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(true)
                 .send();
     }
 
     public void readCheatFightLoose(String token) throws IOException, SQLException, NetworkResponseException {
         User user = FightResponse.checkTokenAndReturnUser(token);
         Fight fight = FightResponse.getFightWithChallenger(user);
 
         if (fight == null) {
             throw new NetworkResponseException(Protocol.ERROR_FIGHT);
         }
 
         fight.setWinner(fight.getBrute2());
         DatasManager.save(fight);
 
         this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                 .writeBoolean(false)
                 .send();
     }
 
     public void readCheatFightRandom(String token) throws IOException, SQLException, NetworkResponseException {
         if (Math.random() < 0.5) {
             this.readCheatFightLoose(token);
         } else {
             this.readCheatFightWin(token);
         }
     }
 
     public void readDoFight(String token) throws IOException, SQLException, NetworkResponseException {
         User user = FightResponse.checkTokenAndReturnUser(token);
         Fight fight = FightResponse.getFightWithChallenger(user);
 
         DoFight f = new DoFight(fight);
         Brute winner = f.exec();
         fight.setWinner(winner);
         DatasManager.save(fight);
 
         if (winner == fight.getBrute1()) {
             this.readCheatFightWin(token);
         } else {
             this.readCheatFightLoose(token);
         }
 
         System.out.println(f.getLogs());
     }
 }
