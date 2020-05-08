 package codeplanes;
 
 import javax.swing.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SimulationDrawerTest extends JFrame {
     static class TestStrategy implements Strategy {
         @Override
         public void turn(final Plane plane, final World world, final Move move) {
             //move.setAngle(100500);
             move.setAngle(Math.random() - Math.random());
             move.setFire(true);
         }
     }
 
     private final BattleDrawer battleDrawer = new BattleDrawer();
 
     SimulationDrawerTest() {
         add(battleDrawer);
     }
 
     public void setBattle(final Battle battle) {
         battle.addHandler(battleDrawer);
     }
 
     public static void main(final String[] args) {
         final SimulationDrawerTest battleDrawerTest = new SimulationDrawerTest();
 
         final List<Strategy> strategies = new ArrayList<>();
         strategies.add( new TestStrategy() );
         strategies.add( new TestStrategy() );
 
         final Simulation battle = new Simulation(strategies, 800, 600, 4000);
 
         battleDrawerTest.setBattle(battle);
 
        battleDrawerTest.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         battleDrawerTest.setVisible(true);
         battleDrawerTest.setSize(500,500);
 
         battle.run();
     }
 }
