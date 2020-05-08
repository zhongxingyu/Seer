 package de.engineapp.controls;
 
 import java.awt.BorderLayout;
 import java.awt.event.*;
 import java.util.Iterator;
 
 import javax.swing.*;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.engine.environment.Scene;
 import de.engine.objects.Ground;
 import de.engine.objects.ObjectProperties;
 import de.engine.objects.ObjectProperties.Material;
 import de.engineapp.*;
 import de.engineapp.PresentationModel.*;
 import de.engineapp.visual.ISelectable;
 
 public class PropertiesPanel extends VerticalBoxPanel implements SceneListener, ActionListener, ChangeListener, StorageListener, MouseListener
 {
     private static final long serialVersionUID = 8656904964293251249L;
     
     private final static Localizer LOCALIZER = Localizer.getInstance();
     
     private PresentationModel pModel;
     
     //Label erstellen
     private JLabel nameLabel;
     private JLabel materialLabel;
     private JLabel xCordinateLabel;
     private JLabel yCordinateLabel;
     private JLabel xSpeedLabel;
     private JLabel ySpeedLabel;
     private JLabel massLabel;
     
     private JLabel LabelPotE; //Schriftzug
     private JLabel LabelKinE; //Schriftzug
     
     private JLabel potLabel;  //Werte
     private JLabel kinLabel;  //Werte
     
     //Buttons erstellen
     private JButton del;
     private JButton close;
     private EasyButton next;
     private EasyButton previous;
     
     //Namensfeld erstellen
     private JTextField name;
     private JPanel groupName;
     
     //ComboBox und CheckBox erstellen
     private JCheckBox fix;
     
     private IconComboBox<Material> MaterialCombo;
     
     //Spinner erstellen
     private PropertySpinner massInput;
     private PropertySpinner xCord;
     private PropertySpinner yCord;
     private PropertySpinner vx;
     private PropertySpinner vy;
     
     
     //Variablen
     private int avoidUpdate;
     
     public PropertiesPanel(PresentationModel model)
     {
         pModel = model;
         
         createControls();
         
          //+++++++++++++++++++++++++++++++++++++++++++++++++//
         //================= Konfiguration =================//
        //+++++++++++++++++++++++++++++++++++++++++++++++++//
         
         //Buttons
           del.addActionListener(this);
         close.addActionListener(this);
           del.setActionCommand("del");
         close.setActionCommand("close");
         
         //Namensfeld konfigurieren
         name.setEditable(isEnabled());
         name.addFocusListener(new FocusAdapter()
             {
                 @Override
                 public void focusGained(FocusEvent e)
                 {
                     name.selectAll();
                 }
             }
         );
         
         
         
         
         this.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ) );
         
 
         pModel.addSceneListener(this);
         pModel.addStorageListener(this);
         pModel.addMouseListenerToCanvas(this);
     }
     
     private void createControls()
     {
         //Labels
         nameLabel       = new JLabel(LOCALIZER.getString("NAME_OF_OBJECT"));
         materialLabel   = new JLabel(LOCALIZER.getString("MATERIAL"));
         xCordinateLabel = new JLabel(LOCALIZER.getString("X_COORDINATE"));
         yCordinateLabel = new JLabel(LOCALIZER.getString("Y_COORDINATE"));
         xSpeedLabel     = new JLabel(LOCALIZER.getString("X_VELOCITY"));
         ySpeedLabel     = new JLabel(LOCALIZER.getString("Y_VELOCITY"));
         massLabel       = new JLabel(LOCALIZER.getString("MASS"));
         
         LabelPotE       = new JLabel(LOCALIZER.getString("POT_ENERGY"));
         LabelKinE       = new JLabel(LOCALIZER.getString("KIN_ENERGY"));
 
         //Buttons
         del          = new JButton(LOCALIZER.getString("REMOVE"));
         close        = new JButton(LOCALIZER.getString("CLOSE"));
         
         //Namensfeld
         name         = new JTextField();
         groupName = new JPanel(new BorderLayout(3, 0));
         groupName.setOpaque(true);
         
         //Combobox + CheckBox
         fix          = new JCheckBox(LOCALIZER.getString("PINNED"));
     }
 
     @Override
     public void objectAdded(ObjectProperties object) {}
     @Override
     public void objectRemoved(ObjectProperties object) {}
 
     @Override
     public void groundAdded(Ground ground) {}
 
     @Override
     public void groundRemoved(Ground ground) {}
 
 
     @Override
     public void objectSelected(ObjectProperties object)
     {
         if (!pModel.isState("dblClickshowProperties"))
         {
             showPanel(object);
         }
     }
     
     
     private void showPanel(ObjectProperties object)
     {
         //Instanzieren
         massInput    = new PropertySpinner(object.getMass(),1,1000,1,this);
         xCord        = new PropertySpinner(object.getPosition().getX(),-100000.0,100000,10,this);
         yCord        = new PropertySpinner(object.getPosition().getY(),-100000,100000,10,this);
         vx           = new PropertySpinner(object.velocity.getX(),-1000,1000,10,this);
         vy           = new PropertySpinner(object.velocity.getY(),-1000,1000,10,this);
         
         potLabel     = new JLabel(this.formatDoubleValue(object.potential_energy));
         kinLabel     = new JLabel(this.formatDoubleValue(object.kinetic_energy));
         
         MaterialCombo = new IconComboBox<Material>(Material.values(), "materials");
         
         next         = new EasyButton(Util.getIcon("next"),"next",this);
         previous     = new EasyButton(Util.getIcon("previous"),"previous",this);
         groupName.add(previous, BorderLayout.LINE_START);
         groupName.add(next, BorderLayout.LINE_END);
         groupName.add(name);
         
         massInput.setValue(object.getMass());
         //Konfigurieren
         massInput.setValue(object.getMass());
         xCord.setValue(object.getPosition().getX()); 
         yCord.setValue(object.getPosition().getY());
         vx.setValue(object.velocity.getX());
         vy.setValue(object.velocity.getY());
         
         name.setText(((ISelectable)object).getName());
         
         MaterialCombo.setSelectedItem(pModel.getSelectedObject().surface);
         MaterialCombo.addActionListener(this);
         
         fix.addActionListener(this);
         
         //Hinzufügen
         
         this.addGap(10);
         this.add(nameLabel, CENTER_ALIGNMENT);
         this.addGap(5);
 //        this.addGroup(3,previous,name,next);
         this.addGroup(3, groupName);
         this.addGap(25);
         this.add(del, RIGHT_ALIGNMENT);
         this.addGap(10);
         this.addSeparator();
         this.addGap(15);
         this.addGroup(5, materialLabel);
         this.addGap(5);
         this.addGroup(5,MaterialCombo);
         this.addGap(10);
         this.addGroup(5, xCordinateLabel, yCordinateLabel);
         this.addGroup(5, xCord, yCord);
         this.addGap(10);
         this.addGroup(5, xSpeedLabel,ySpeedLabel);
         this.addGroup(5, vx, vy);
         this.addGap(10);
         this.add(massLabel, LEFT_ALIGNMENT);
         this.add(massInput, LEFT_ALIGNMENT);
         this.addGap(20);
         this.add(fix, LEFT_ALIGNMENT);
         this.addGap(20);
         this.addGroup(5,LabelPotE, potLabel);
         this.addGap(5);
         this.addGroup(5,LabelKinE, kinLabel);
         this.addGap(25);
         this.add(close, LEFT_ALIGNMENT);
         
         this.updateUI();
         this.setVisible(true);
     }
     
     
     @Override
     public void objectDeselected(ObjectProperties object)
     {
         this.setVisible(false);
         this.removeAll();
     }
     
     
     @Override
     public void actionPerformed(ActionEvent e)
     {
         switch(e.getActionCommand())
         {
             case "del":
                 pModel.removedObject(pModel.getSelectedObject());
                 pModel.fireRepaintEvents();
                 break;
                 
             case "close":
                 pModel.setSelectedObject(null);
                 pModel.fireRepaintEvents();
                 break;
                 
             case "next":
                 Iterator<ObjectProperties> it = pModel.getScene().getObjects().iterator();
                 while(it.next() != pModel.getSelectedObject());
                 if(it.hasNext())
                 {
                     pModel.setSelectedObject(it.next());
                 }
                 else
                 {
                     pModel.setSelectedObject(pModel.getScene().getObject(0));
                 }
                 pModel.fireRepaintEvents();
                 break;
                 
             case "previous":
                 if(pModel.getSelectedObject() == pModel.getScene().getObject(0))
                 {
                     pModel.setSelectedObject(pModel.getScene().getObject(pModel.getScene().getCount()-1));
                 }
                 else
                 {
                     for(int i = pModel.getScene().getCount()-1; i >= 0; i--)
                     {
                         if(pModel.getSelectedObject() == pModel.getScene().getObject(i)) 
                         {
                             pModel.setSelectedObject(pModel.getScene().getObject(i-1));
                             break;
                         }
                     }
                 }
                 pModel.fireRepaintEvents();
                 break;
         }
 
         if(e.getSource() == MaterialCombo)
         {
             pModel.getSelectedObject().surface = MaterialCombo.getSelectedItem();
         }
         if(e.getSource() == fix)
         {
             pModel.getSelectedObject().isPinned = fix.isSelected();
             System.out.println(pModel.getSelectedObject().isPinned);
         }
         
         
     }
 
     //Werte der Controls auf die Eigenschaften des Objekts übertragen
     @Override
     public void stateChanged(ChangeEvent e)
     {
         if(avoidUpdate != 1)
         {
             pModel.getSelectedObject().world_position.translation.setX(xCord.getValue());
             pModel.getSelectedObject().world_position.translation.setY(yCord.getValue());
             pModel.getSelectedObject().velocity.setX(vx.getValue());
             pModel.getSelectedObject().velocity.setY(vy.getValue());
             pModel.getSelectedObject().setMass(massInput.getValue());
             pModel.getSelectedObject().surface = (Material) MaterialCombo.getSelectedItem();
             pModel.getSelectedObject().isPinned = fix.isSelected();
 
             pModel.fireRepaintEvents();
         }
         
     }
     
     //Werte der Controls zur Laufzeit der Szene anpassen
     @Override
     public void sceneUpdated(Scene scene)
     {
         if(pModel.getSelectedObject() != null) //vermeidet ungewollten Aufruf des ChangeListeners
         {
             avoidUpdate = 1;
             massInput.setValue(pModel.getSelectedObject().getMass());
             xCord.setValue(pModel.getSelectedObject().getPosition().getX()); 
             yCord.setValue(pModel.getSelectedObject().getPosition().getY());
             vx.setValue(pModel.getSelectedObject().velocity.getX());
             vy.setValue(pModel.getSelectedObject().velocity.getY());
     
             name.setText(((ISelectable)pModel.getSelectedObject()).getName());
             
             potLabel.setText(this.formatDoubleValue(pModel.getSelectedObject().potential_energy));
             kinLabel.setText(this.formatDoubleValue(pModel.getSelectedObject().kinetic_energy));
             avoidUpdate = 0;
         }
         
     }
 
     //Formatvorlage für die Labels Epot & Ekin
     private String formatDoubleValue(double d)
     {
         String s;
         if(Math.abs(d) > 1000)
         {
             s = " kJ";
             d = d/1000;
         }
         else
         {
             s = " J";
         }
         s = String.format("%.2f", d) + s;
         return s;
     }
     
     @Override
     public void stateChanged(String id, boolean value) { }
     
     @Override
     public void propertyChanged(String id, String value)
     {
         if (id.equals("langCode"))
         {
             createControls();
             
             if (this.isVisible())
             {
                 this.setVisible(false);
                 this.removeAll();
                objectSelected(pModel.getSelectedObject());
             }
         }
     }
     
     @Override
     public void mouseClicked(MouseEvent e)
     {
         if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && 
                 pModel.isState("dblClickshowProperties") && pModel.getSelectedObject() != null)
         {
             showPanel(pModel.getSelectedObject());
         }
     }
     
     @Override
     public void mouseEntered(MouseEvent e) { }
     
     @Override
     public void mouseExited(MouseEvent e) { }
     
     @Override
     public void mousePressed(MouseEvent e) { }
     
     @Override
     public void mouseReleased(MouseEvent e) { }
 }
