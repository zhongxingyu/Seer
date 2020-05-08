 package civ;
 
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JProgressBar;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.BoxLayout;
 import javax.swing.BorderFactory;
 import javax.swing.Popup;
 import javax.swing.PopupFactory;
 
 import java.util.Observer;
 import java.util.Observable;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import java.awt.*;
 
 import static civ.State.TileState.TileSelected;
 import static civ.State.TileState.TileUnSelected;
 import static civ.State.UnitState.UnitSelected;
 import static civ.State.UnitState.UnitUnSelected;
 import static civ.State.ActionState.Move;
 import static civ.State.ActionState.Attack;
 
 import static civ.State.HoverState.HoverNone;
 import static civ.State.HoverState.HoverTileOnly;
 import static civ.State.HoverState.HoverTileUnit;
 
 public class Menu extends JPanel implements Observer, ActionListener{
 
     private static final State state = State.getInstance();
     private JPanel north = new JPanel();
     private JPanel eastPanel = new JPanel();
     private JPanel westPanel = new JPanel();
     private JPanel createUnit = new JPanel(); // Temporary panel
 
     private JTabbedPane tabbedPane;
     private GlobalView globalViewObject;
     
     private PhysicalUnitView unitView;	
     private CityView cityView;
     private JProgressBar manPowerBar;    
     private JLabel tileLabel;
     //private JLabel unitPresentation;
     private GameMapView gmv = new GameMapView();    
 
     private JButton putUnit;
     private JComboBox selUnit;
 
     //Status is only for testing purpose
    private JLabel status = new JLabel("Status is: " + state.getUnitState());
     //private PopupWindow puw;
     private int curScale = 5 ;
     private int[] sizes = {50, 75, 90, 120, 150, 175, 190};
     
     Menu(){ 
         super(); 
         setLayout(new BorderLayout(0,10)); 
         
         //this.puw = puw;
         //Popup popup;
         //popup = PopupFactory.getSharedInstance().getPopup(null, puw, 200,200);
 		
         
         manPowerBar = new JProgressBar(0,100);  
         manPowerBar.setSize(new Dimension(30,10));
         manPowerBar.setString("Manpower "); 
         manPowerBar.setStringPainted(true); 
         
         //unitPresentation = new JLabel();
         tileLabel = new JLabel();
  
         selUnit = new JComboBox(PhysicalUnitType.values());
         putUnit = new JButton("Set Unit");
         putUnit.setActionCommand("setunit");
         putUnit.addActionListener(this);
 
         createUnit.add(selUnit);
         createUnit.add(putUnit);
 
         tabbedPane = new JTabbedPane(); 
         //tabbedPane.addTab("Cities ", null, tabContent, "Your cities ");
         //stabbedPane.addTab("Units", null, tabContent, "Your units ");
        
         add(north, BorderLayout.NORTH);      
         add(westPanel,  BorderLayout.WEST);
         add(eastPanel, BorderLayout.EAST);
         add(createUnit, BorderLayout.CENTER);
         
         // Set layout managers 
         north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS)); 
         eastPanel.setLayout(new BorderLayout());
         westPanel.setLayout(new BorderLayout());
         westPanel.setPreferredSize(new Dimension (600,220));
         
         // ADD CONTENT
         //unitView = GameMap.getInstance().getTile(1,1).getUnit().getView();
         //unitView.setPopup(popup);
         globalViewObject = new GlobalView();
         
         tabbedPane.addTab(" Hey ", null, unitView, "Inget objekt markerat ");
         //tabbedPane.addTab("Cities ", null, null, "About cities");
         tabbedPane.setPreferredSize(new Dimension(580,200));
             
         // East
         eastPanel.add(globalViewObject, BorderLayout.EAST);
         
         // West
         westPanel.add(tabbedPane, BorderLayout.WEST);
         
         north.add(tileLabel);
         //north.add(unitPresentation);
         
         state.addObserver(this);
         tabbedPane.repaint();
         update(); 
     }
 
     private void scaleUp(){
         if(curScale != sizes.length-1){
             GameMap.getInstance().resize(sizes[++curScale]);
         }
     }
     private void scaleDown(){
         if(curScale != 0){
             GameMap.getInstance().resize(sizes[--curScale]);
         }
     }
 
     public void update(Observable obs, Object obj){
         if(obs == state){
             update();
         }
     }
     
     private void update(){
         switch (state.getHoverState()) {
             case HoverNone:
                 tileLabel.setText("No tile");
                 break;
             case HoverTileOnly:
                 tileLabel.setText("Terrain: \n" + state.getHoverTile().getTerrain().toString());
                 break;
             case HoverTileUnit:
                 String outputTerrain = state.getHoverTile().getTerrain().toString();
                 String outputUnit = Integer.toString(state.getHoverTile().getUnit().getManPower());
                 if(state.getHoverTile().getUnit().isAlly()){
                     tileLabel.setText("<html>Terrain: " + outputTerrain +
                             "<br>Unit: " + state.getHoverTile().getUnit().getType() +
                             " Anfall: " + state.getHoverTile().getUnit().getType().getAttack() + 
                             " Försvar: " + state.getHoverTile().getUnit().getType().getDefence() + 
                             " Manpower: " + outputUnit + "</html>");
                 }
                 else{
                     // Enemy unit hovered
                     tileLabel.setText("<html>Terrain: " + outputTerrain +
                             "<br>Unit: Enemy unit");
 
                 }
                 
                 break;
         }        
 
         switch(state.getUnitState()){
 
             case UnitSelected: 
                 if(state.getSelectedUnit().isAlly()){
                     PhysicalUnit unit = state.getSelectedUnit();
                  	String unitTypeName = unit.getType().getName();
                  	tabbedPane.addTab(unitTypeName, null, unit.getView(), "Visa dina units ");
                 }
                 break;
                 
             case UnitUnSelected:                 
                 tabbedPane.removeAll();
                 tabbedPane.repaint();
 
                 break;
         }
         
         switch (state.getCityState()) {
         	case CitySelected:
         		System.out.println("Hello");
         		City city = state.getSelectedCity();
         		String cityName = city.getName();
         		tabbedPane.addTab(cityName, null, city.getView(), "Visa dina städer ");
         		
         		break;
         	case CityUnSelected:
         		//tabbedPane.removeAll();
         		tabbedPane.repaint();
         		
         		break;
         }
         
         updateState();
     }
 
     private void updateState(){
         status.setText("Status is: " + state.getUnitState());
     }
 
     public void actionPerformed(ActionEvent ae){
         if(putUnit == ae.getSource()){
             if(state.getTileState() == TileSelected){
                 if(state.getSelectedTile().hasUnit()){
                     status.setText("Status is: Can't place unit on another unit.");
                     GameMap gm = GameMap.getInstance();
                     //scaleUp();
                 }
                 else if(!state.getSelectedTile().getTerrain().isTraversible((PhysicalUnitType)selUnit.getSelectedItem())){
                     status.setText("Status is: Unit can't stand there.");
                     //scaleDown();
                 }
                 else{
                     state.getSelectedTile().setUnit(new PhysicalUnit(
                                 (PhysicalUnitType)selUnit.getSelectedItem(),
                                 Round.getActivePlayer()));//(Player)selPlayer.getSelectedItem()));
                     state.getSelectedTile().getView().repaint();
                 }
             }
         }
     }
 }
 
