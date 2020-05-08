 package civ;
 
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 
 import java.util.Observer;
 import java.util.Observable;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
<<<<<<< HEAD:src/Menu.java
import java.awt.*;

import static src.State.UnitState.Selected;
import static src.State.UnitState.UnSelected;
import static src.State.ActionState.Move;
import static src.State.ActionState.Attack;
=======
 import static civ.State.UnitState.Selected;
 import static civ.State.UnitState.UnSelected;
 import static civ.State.ActionState.Move;
 import static civ.State.ActionState.Attack;
>>>>>>> 685a7138538e38b4c07585716b1f9e4f5fbf1d8a:src/civ/Menu.java

 import static civ.State.HoverState.HoverNone;
 import static civ.State.HoverState.HoverTileOnly;
 
 
 public class Menu extends JPanel implements Observer, ActionListener{
 
     private static final State state = State.getInstance();
     private JPanel north = new JPanel();
     
     private JButton move;
     private JButton attack;
     private JButton plus;
     private JButton minus;
 
     private JLabel tileLabel;
     private GameMapView gmv = new GameMapView();    
     
     //Status is only for testing purpose
     private JLabel status = new JLabel("Status is: " + state.getUnitState());
 
     Menu(){ 
         super(); 
         setLayout(new BorderLayout());
         tileLabel = new JLabel("Tile info is empty"); 
 
         move = new JButton("Move");
         //move.setMnemonic(KeyEvent.VK_D);
         move.setActionCommand("move");
         move.addActionListener(this);
 
         attack = new JButton("Attack");
         attack.setActionCommand("attack");
         attack.addActionListener(this);
 
         plus = new JButton("+");
         plus.setActionCommand("+");
         plus.addActionListener(this);
 
         minus = new JButton("-");
         minus.setActionCommand("-");
         minus.addActionListener(this);
 
         state.addObserver(this);
 
         add(status, BorderLayout.CENTER); 
         add(move, BorderLayout.SOUTH); 
         add(attack, BorderLayout.EAST); 
         add(tileLabel, BorderLayout.NORTH); 
 
         update(); 
     }
 
     public void update(Observable obs, Object obj){
         if(obs == state){
             update();
         }
     }
 
     private void update(){
         switch (state.getHoverState()) {
             case HoverNone:
                 tileLabel.setText("No tile selected ");
                 break;
             case HoverTileOnly:
                 tileLabel.setText(state.getHoverTile().getTerrain().toString());
                 break;
         }        
 
         switch(state.getUnitState()){
             case UnSelected: 
                 move.setEnabled(false); 
                 attack.setEnabled(false);
                 break;
             case Selected: 
                 move.setEnabled(true);
                 attack.setEnabled(true);
                 switch(state.getActionState()){
                     case Move: move.setEnabled(false); break;
                     case Attack: attack.setEnabled(false); break;
                 }
                 break;
         }	
         updateState();
 
     }
 
     private void updateState(){
         status.setText("Status is: " + state.getUnitState());
     }
 
     public void actionPerformed(ActionEvent ae){
         if(move == ae.getSource()){
             state.setActionState(Move);
         }
         if(attack == ae.getSource()){
             state.setActionState(Attack);
         }
         if(plus == ae.getSource()){
             //            GameMap.getInstance().scale(1.1);
         }
         if(minus == ae.getSource()){
             //            GameMap.getInstance().scale(0.9);
         }
     }
 }
 
