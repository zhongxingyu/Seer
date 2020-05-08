 package civ;
 
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 
 import java.util.Observer;
 import java.util.Observable;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import java.awt.*;
 
 import static civ.State.UnitState.Selected;
 import static civ.State.UnitState.UnSelected;
 import static civ.State.ActionState.Move;
 import static civ.State.ActionState.Attack;
 
 import static civ.State.HoverState.HoverNone;
 import static civ.State.HoverState.HoverTileOnly;
 import static civ.State.HoverState.HoverTileUnit;
 
 public class Menu extends JPanel implements Observer, ActionListener{
 
     private static final State state = State.getInstance();
     private JPanel north = new JPanel();
    
     private JButton move;
     private JButton attack;
     private JButton plus;
     private JButton minus;
 
     private JLabel tileLabel;
     private JLabel unitPresentation;
     private GameMapView gmv = new GameMapView();    
    
     //Status is only for testing purpose
     private JLabel status = new JLabel("Status is: " + state.getUnitState());
 
     Menu(){ 
         super(); 
         setLayout(new BorderLayout());
        
         tileLabel = new JLabel("Tile info is empty");  
         unitPresentation = new JLabel();
        
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
         // Intended to be the north panel    
       add(north); 
        
         //north.add(status); 
         add(move, BorderLayout.CENTER); 
         add(attack, BorderLayout.WEST); 
         add(unitPresentation, BorderLayout.NORTH);
         add(tileLabel, BorderLayout.SOUTH);
        
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
                 tileLabel.setText("Terrain: " + state.getHoverTile().getTerrain().toString());
                 break;
             case HoverTileUnit:
                 String outputTerrain = state.getHoverTile().getTerrain().toString();
                 String outputUnit = Integer.toString(state.getHoverTile().getUnit().getManPower());
                 tileLabel.setText("Terrain: " +outputTerrain+" - Unit: " +state.getHoverTile().getUnit().getType() +" Anf: " +state.getHoverTile().getUnit().getType().getAttack() + " Def: "+state.getHoverTile().getUnit().getType().getDefence()+ " Mnp: "+ outputUnit);
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
                 // Add html taggs in JLabel unitPresentation with -arrow- BR -arrow- for line break
                 // unitPresentation should contain victory chance variable 
                
                 switch(state.getActionState()){
                     case Move: move.setEnabled(false); break;
                     case Attack: attack.setEnabled(false); break;
                 }
                
                 unitPresentation.setText(state.getHoverTile().getUnit().getType().getName()+ " is marked \n Attack: " +state.getHoverTile().getUnit().getType().getAttack());
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
 
