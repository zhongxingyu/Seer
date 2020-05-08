 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.server;
 
 import brutes.server.db.entity.BonusEntity;
 import brutes.server.db.entity.NotFoundEntityException;
 import brutes.server.game.Bonus;
 import brutes.server.game.Brute;
 import brutes.server.game.Fight;
 import java.io.IOException;
 import java.sql.SQLException;
 
 /**
  *
  * @author Olivares Georges <dev@olivares-georges.fr>
  */
 public class DoFight {
 
     protected Fight fight;
     protected StringBuilder logs;
 
     public DoFight(Fight fight) {
         this.fight = fight;
         this.logs = new StringBuilder();
     }
 
     protected DoFight addLogs(String text) {
         this.logs.append(text);
         return this;
     }
 
     public StringBuilder getLogs() {
         return this.logs;
     }
 
     protected DoFight addLogsLn(String text) {
         return this.addLogs(text).addLogs("\n");
     }
 
     protected void round(int round, Brute brute, Brute against) throws IOException, SQLException {
         this.addLogs("\t[" + brute + "] ");
         int pv;
 
         if (brute.getWithBonusLife() < 0 || against.getWithBonusLife() < 0) {
             return;
         }
 
         if (ui.random(0, 75) == ui.random(25, 100 + round)) {
             this.addLogsLn(" ... slips and dies ...");
             brute.setLife((short) 0);
             return;
         }
 
         if (ui.random(5) == 0) {
             switch (ui.random(5)) {
                 case 0:
                     this.addLogsLn("miss his attacks");
                     return;
                 case 1:
                     this.addLogsLn("falls asleep ...");
                     return;
                 case 2:
                     this.addLogsLn("forgetting what he does ...");
                     return;
                 case 3:
                     this.addLogsLn("look at the pretty cloud ...");
                     return;
                 case 4:
                 case 5:
                     pv = ui.random(0, brute.getLife() / 4);
                     brute.setLife((short) (brute.getLife() - pv));
 
                     this.addLogsLn("attaks himself :s and lose " + pv + " PV ");
                     return;
             }
         }
 
         Bonus bonusUsed;
         try {
             bonusUsed = BonusEntity.findById(brute.getBonuses()[ui.random(3 - 1)].getId());
         } catch (NotFoundEntityException ex) {
             bonusUsed = Bonus.EMPTY_BONUS;
         }
 
         double d = brute.getWithBonusStrength() * against.getWithBonusSpeed() - brute.getWithBonusStrength() * against.getWithBonusSpeed();
         d = Math.min(0, d) + ui.random(1, 50) / 10 + ui.random(brute.getLevel());
 
         // If he uses a bonus
         if (bonusUsed != Bonus.EMPTY_BONUS) {
             d += ui.random(bonusUsed.getSpeed() * bonusUsed.getStrength());
         }
 
         double power = ((double) ui.random(30, 40) / 40); // coefficient aléatoire entre .75 et 1
         power *= 1 + d;
         pv = (int) Math.ceil(power);
 
         if (bonusUsed != Bonus.EMPTY_BONUS) {
             this.addLogsLn("attacks with " + bonusUsed + " (-" + pv + ")");
         } else {
             this.addLogsLn("attacks (-" + pv + ")");
         }
 
         against.setLife((short) (against.getLife() - pv));
 
         if (ui.random(0, 6) == 0) {
             this.addLogsLn("\t[" + against + "] is really, really mad (and win +1 in VIT and STR) !!!");
             against.setSpeed((short) (against.getSpeed() + 1));
             against.setStrength((short) (against.getStrength() + 1));
         }
     }
 
     public Brute exec() throws IOException, SQLException {
         for (int i = 0; i < 10; i++) {
            if (this.fight.getBrute1().getLife() <= 0 || this.fight.getBrute2().getLife() <= 0) {
                 break;
             }
 
             this.addLogs("Round " + i + "  ")
                     .addLogs(this.fight.getBrute1() + " (" + this.fight.getBrute1().getWithBonusLife() + "pv")
                     .addLogs(", " + this.fight.getBrute1().getWithBonusSpeed() + " & " + this.fight.getBrute1().getWithBonusStrength())
                     .addLogs(") VS ")
                     .addLogs(this.fight.getBrute2() + " (" + this.fight.getBrute2().getWithBonusLife() + "pv")
                     .addLogs(", " + this.fight.getBrute2().getWithBonusSpeed() + " & " + this.fight.getBrute2().getWithBonusStrength() + ")")
                     .addLogsLn("");
             // Who attack first ?
             if (ui.random(this.fight.getBrute1().getWithBonusSpeed()) > ui.random(this.fight.getBrute2().getWithBonusSpeed())) {
                 // The Brute1 is more speed than Brute2
                 this.round(i, this.fight.getBrute1(), this.fight.getBrute2());
                 this.round(i, this.fight.getBrute2(), this.fight.getBrute1());
             } else {
                 this.round(i, this.fight.getBrute2(), this.fight.getBrute1());
                 this.round(i, this.fight.getBrute1(), this.fight.getBrute2());
             }
         }
 
         if (this.fight.getBrute1().getLife() > 0 && this.fight.getBrute2().getLife() > 0) {
             this.addLogsLn("YOU CAN'T CHOOSE ? GOD CAN !");
             return ui.random() ? this.fight.getBrute1() : this.fight.getBrute2();
         }
         return this.fight.getBrute1().getLife() > 0 ? this.fight.getBrute1() : this.fight.getBrute2(); // return the winner
     }
 }
