 package pool;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class PoolFrame extends JFrame implements ChangeListener, ActionListener {
     PoolPanel poolPanel = new PoolPanel(.3,.35, 25, 13);
     JPanel content = new JPanel();
     JPanel south = new JPanel();
         
     int aimRange = 5000;
     int powerRange = 100;
     int spinRange = 100;
     
     //Buttons and components
     JComboBox colorChoice = new JComboBox();
     JSlider angleSlider = new JSlider(0,aimRange,0);//Defines angle of shot
     JSlider powerSlider = new JSlider(0,powerRange,0);
     JSlider spinSlider = new JSlider(-spinRange,spinRange,0);
     JButton selectionModeButton = new JButton("Selection Mode");
     JButton newBallButton = new JButton("New Ball");
     JButton shootButton = new JButton("Shoot");
     JButton makeRackButton = new JButton("Make Rack");
     JButton snapButton = new JButton("Snap");
     
         
     public void actionPerformed(ActionEvent evt){        
         Object source = evt.getSource();
         if       (source == shootButton)         {
             poolPanel.shoot();
         } else if(source == snapButton)          {
             poolPanel.cameraController.snapToShootingBall();
         } else if(source == selectionModeButton) {
             poolPanel.flipSelectionMode();
         } else if(source == makeRackButton)      {
             poolPanel.newRack();
         } else if(source == newBallButton)       {
            poolPanel.addBall(1.0, -1.0, 0.0, 0.0, 0.0);            
         }
     }
     
     public void stateChanged(ChangeEvent ce) {
         Object source = ce.getSource();
         if       (source == colorChoice) {
             
         } else if(source == angleSlider) {
             double val = angleSlider.getValue();
             poolPanel.setAim(Math.cos((val)*2*Math.PI/aimRange), Math.sin((val)*2*Math.PI/aimRange));            
         } else if(source == powerSlider) {
             double val = powerSlider.getValue();
             poolPanel.setPower(val/powerRange);
         } else if(source == spinSlider)  {
             double val = spinSlider.getValue();
             poolPanel.setSpin(val/spinRange);
         }        
     }                
     
     public PoolFrame(String str) {
         super(str);
         
         //Component Configuration
 	colorChoice.addItem("Red");
 	colorChoice.addItem("Blue");
 	colorChoice.addItem("Green");
 	colorChoice.addItem("Yellow");
         powerSlider.setOrientation(JSlider.VERTICAL);                
 	        
         //Add listeners
 	newBallButton.addActionListener(this);
         selectionModeButton.addActionListener(this);
 	shootButton.addActionListener(this);
         makeRackButton.addActionListener(this);
         snapButton.addActionListener(this);
 	powerSlider.addChangeListener(this);
         spinSlider.addChangeListener(this);
         angleSlider.addChangeListener(this);
         
         //Add content to frame.
 	content.setLayout(new BorderLayout());
 	content.add(poolPanel, BorderLayout.CENTER);
 	content.add(angleSlider, BorderLayout.NORTH);
 	content.add(powerSlider, BorderLayout.EAST);
         
         //Toolbar setup.
         south.setBackground(Color.BLACK);
         south.add(snapButton);
         south.add(makeRackButton);
 	south.add(selectionModeButton);
 	south.add(newBallButton);
 	south.add(shootButton);
 	south.add(colorChoice);
         south.add(spinSlider);
 	content.add(south, BorderLayout.SOUTH);
         
         //Finalize
 	this.setContentPane(content);
 	this.setSize(1000, 700);
 	this.setLocation(0,0);
 	this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	this.setVisible(true);
         
         //Init proper shooting values
         double val = angleSlider.getValue();
         poolPanel.setAim(Math.cos((val)*2*Math.PI/aimRange), Math.sin((val)*2*Math.PI/aimRange));
         poolPanel.setPower(val/100);
         val = powerSlider.getValue();
         poolPanel.setPower(val/50);
     }
     
 }
