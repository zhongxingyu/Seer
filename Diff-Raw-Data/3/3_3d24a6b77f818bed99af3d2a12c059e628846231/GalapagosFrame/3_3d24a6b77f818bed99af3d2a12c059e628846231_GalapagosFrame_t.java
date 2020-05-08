 package galapagos;
 
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * A graphical user-interface for creating and viewing Biotope's.
  * At the top of the screen there are a set of buttons for creating and simulating the Biotope.
  * At the middle of the screen the Biotope is shown graphically.
  * At the bottom of the screen there are a panel with statistics about the different types of behaviors
  * Its possible to spawn new Finches by choosing a behavior on the right and "draw" them on the Biotope.
  * On the left part of the screen you can turn on and of the graphical display and the logger (for performance),
  * its also here you change the updaterate. 
  * 
  */
 public class GalapagosFrame extends JFrame implements Observer {
 
     private AreaPanel area;
     public Map<Behavior, Color> colorMap;
     public int pixelSize;
     private NicerStatisticsPanel statistics;
     private BiotopeLogger logger;
     private BiotopeController controller;
     public final BiotopeCreator biotopeCreator;
     private Biotope biotope;
     
     private JButton newBiotope;
     private JButton nextRound;
     private JSpinner numberOfRounds;
     private JButton severalRounds;
     private JButton unlimitedRounds;
     private JButton stopRounds;
     private JCheckBox toggleLogging;
     private JCheckBox toggleDisplayRefresh;
     private JSpinner timerInterval;
 
     private ButtonGroup behaviorButtons;
     private Container behaviorButtonsBox;
     private JLabel behaviorButtonsLabel;
     private Behavior selectedBehavior;
 
     private boolean isLogging;
     private boolean isRefreshing;
     
     private static final Dimension minimumButtonDimension = new Dimension(0,30);
     private static final Dimension standardSpinnerSize = new Dimension(100,22);
     
     private List<Behavior> behaviors;
 
     /**
      * Create a GalapagosFrame simulating finches with the provided
      * behaviors and using the provided colors to visually represent
      * the simulation state.
      *
      * @param behaviors A mapping from behavior objects to colors. The
      * behavior objects specify which behaviors should be available
      * for use in the simulation (the user may choose to disable some
      * of them, so they are not guaranteed to participate in the run),
      * and the associated color will be used to draw a visual
      * representation of finches with that behavior.
      *
      * @require For every two distrinct behavior objects b1, b2 in
      * behaviors, b1.toString() != b2.toString() must hold.
      */
     
     public GalapagosFrame(Map<Behavior, Color> behaviors)
     {
         makeBehaviorListAndColorMap(behaviors);
         
         pixelSize = 5;
         isRefreshing = true;
         isLogging = false;
         
         area = new AreaPanel();
         MouseInputAdapter listener = new MouseInputAdapter () {
                 public void maybeAddFinchAt(int x, int y, Behavior b) { 
                     // Only add a finch if x,y is within the bounds of
                     // the world.
                     if (0 <= x && x < biotope.world.width() &&
                         0 <= y && y < biotope.world.height() &&
                         b != null)
                         biotope.putFinch(x, y, b.clone());
                 }
                 public void mousePressed(MouseEvent e) {
                     maybeAddFinchAt(e.getX() / pixelSize, e.getY() / pixelSize, 
                                     selectedBehavior);
                 }
                 public void mouseDragged(MouseEvent e) {
                     maybeAddFinchAt(e.getX() / pixelSize, e.getY() / pixelSize, 
                                     selectedBehavior);
                 }
             };
         area.addMouseListener(listener);
         area.addMouseMotionListener(listener);
         statistics = new NicerStatisticsPanel(colorMap);
         logger = new BiotopeLogger();
         controller = new BiotopeController(biotope);
 
         this.setLayout(new BorderLayout());
         this.doLayout();
         this.addWindowListener(new Terminator());
         this.setTitle("Galapagos Finch Simulator");
         
         initializeControls();
         
         // Create a new Biotope with the default-settings of the BiotopeCreator. 
         biotopeCreator = new BiotopeCreator(this.behaviors);
         setBiotope(biotopeCreator.createBiotope());
     }
     
     public void setBiotope(Biotope biotope)
     {
     	this.biotope = biotope;
     	
         //Create RadioButton's on the GalapagosFrame
         //for the spawning-tool
         behaviorButtons = new ButtonGroup();
         behaviorButtonsBox.removeAll();
         behaviorButtonsBox.add(Box.createGlue());
         behaviorButtonsBox.add(behaviorButtonsLabel);
 
         for (final Behavior b : biotope.behaviors()) {
             JRadioButton button = new JRadioButton(b.toString());
             button.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         selectedBehavior = b;
                     }
                 });
             behaviorButtons.add(button);
             behaviorButtonsBox.add(button);
         }
 
         behaviorButtonsBox.add(Box.createGlue());
 
         selectedBehavior = null;
         controller.setBiotope(biotope);
         area.reset(biotope.world.width(), biotope.world.height(), pixelSize);
         
         biotope.addObserver(statistics);
         if (isLogging)
             biotope.addObserver(logger);
         if (isRefreshing)
             biotope.addObserver(this);
         
         biotope.doNotifyObservers();
         this.setSize(combinedSize());
         this.validate();
        pack();
     }
     
     /**
      * Create and position the controls of the main interface. 
      */
     private void initializeControls()
     {
         //create top controls
         newBiotope = newButton ("New Biotope", "newBiotope");
         newBiotope = new JButton("New Biotope");
         newBiotope.setActionCommand("newBiotope");
         newBiotope.addActionListener(new ActionListener () {
         	public void actionPerformed(ActionEvent e) {
         		disableButtons();
                 biotopeCreator.setVisible(true);
                 Biotope biotope = biotopeCreator.biotope();
                 if(biotope != GalapagosFrame.this.biotope && biotope != null)
                 	GalapagosFrame.this.setBiotope(biotope);
                 enableButtons();
             }
         });
         nextRound = newButton ("Next Round", "nextRound");
         severalRounds = newButton ("Compute Several Rounds", "severalRounds");
         unlimitedRounds = newButton ("Go!", "unlimitedRounds");
         stopRounds = newButton ("Stop Simulation", "stopRounds");
         
         numberOfRounds = new JSpinner(new RevisedSpinnerNumberModel(50,0,Integer.MAX_VALUE,10));
         numberOfRounds.setPreferredSize(standardSpinnerSize);
         numberOfRounds.setName("numberOfRoundsSpinner");
         numberOfRounds.addChangeListener(controller);
         numberOfRounds.setMaximumSize(new Dimension(100,30));
         numberOfRounds.setMinimumSize(minimumButtonDimension);
         
         toggleLogging = new JCheckBox("Perform logging", isLogging);
         toggleLogging.addActionListener(new ActionListener () {
                 public void actionPerformed(ActionEvent e) {
                     if (isLogging)
                         biotope.deleteObserver(logger);
                     else
                         biotope.addObserver(logger);
                     isLogging = !isLogging;
                 }
             });
 
         toggleDisplayRefresh = new JCheckBox("Update display", isRefreshing);
         toggleDisplayRefresh.addActionListener(new ActionListener () {
                 public void actionPerformed(ActionEvent e) {
                     if (isRefreshing)
                         biotope.deleteObserver(GalapagosFrame.this);
                     else
                         biotope.addObserver(GalapagosFrame.this);
                     isRefreshing = !isRefreshing;
                 }
             });
         
         timerInterval = new JSpinner(new RevisedSpinnerNumberModel(200,0,Integer.MAX_VALUE,100));
         timerInterval.setPreferredSize(standardSpinnerSize);
         timerInterval.setMaximumSize(new Dimension(100,30));
         timerInterval.setMinimumSize(minimumButtonDimension);
         timerInterval.addChangeListener(controller);
         timerInterval.setName("timerIntervalSpinner");
         
         Container topContainer = Box.createHorizontalBox();
         topContainer.add(Box.createGlue());
         topContainer.add(newBiotope);
         topContainer.add(nextRound);
         topContainer.add(numberOfRounds);
         topContainer.add(severalRounds);
         topContainer.add(unlimitedRounds);
         topContainer.add(stopRounds);
         topContainer.add(Box.createGlue());
         
         //this container's only purpose is to center area
         Container centerContainer = new Container();
         centerContainer.setLayout(new GridBagLayout());
         centerContainer.add(area);
 
         JPanel leftContainer = new JPanel(new GridBagLayout());
         leftContainer.add(toggleLogging, getComponentConstraints(0,0, GridBagConstraints.CENTER));
         leftContainer.add(toggleDisplayRefresh, getComponentConstraints(0,1, GridBagConstraints.CENTER));
         leftContainer.add(new JLabel("Milliseconds between rounds"), getComponentConstraints(0,2, GridBagConstraints.CENTER));
         leftContainer.add(timerInterval,getComponentConstraints(0,3, GridBagConstraints.CENTER));
         leftContainer.setMaximumSize(leftContainer.getPreferredSize());
         Container outerLeftContainer = Box.createVerticalBox();
         outerLeftContainer.add(Box.createGlue());
         outerLeftContainer.add(leftContainer);
         outerLeftContainer.add(Box.createGlue());
 
         behaviorButtonsBox = Box.createVerticalBox();
         behaviorButtonsLabel = new JLabel("Pencil for freehand finch drawing");
         
         this.add(topContainer, BorderLayout.NORTH);
         this.add(centerContainer,BorderLayout.CENTER);
         this.add(statistics, BorderLayout.SOUTH);
         this.add(outerLeftContainer, BorderLayout.WEST);
         this.add(behaviorButtonsBox, BorderLayout.EAST);
     }
     
     /**
      * Create a new JButton with the specified text and actionCommand.
      * 
      * @param text The button's text
      * @param command The button's actionCommand
      * @return The new JButton
      */
     public JButton newButton(String text, String command)
     {
         JButton button = new JButton(text);
         button.setActionCommand(command);
         button.addActionListener(this.controller);
         button.setMinimumSize(minimumButtonDimension);
         
         return button;
     }
 
     /**
      * A set of GridBagConstraints for use with the GridBagLayout. Recommended for single components.
      * @param x the horisontal position of the component.
      * @param y the vertical position of the component.
      * @param alignment the alignment of the component in its display area.
      */            
     private GridBagConstraints getComponentConstraints (int x, int y, int alignment) {
         return new GridBagConstraints(x, y, 1, 1, 1.0, 1.0, 
                                         alignment,
                                         GridBagConstraints.NONE,
                                         new Insets(5,5,5,5),
                                         0, 0);
     }
     
     public void update(Observable observableBiotope, Object arg)
     {
         Biotope biotope = (Biotope) observableBiotope;
         for(World<GalapagosFinch>.Place place : biotope.world)
         {
             GalapagosFinch element = place.getElement();
             if(element != null)
                 area.pixel(place.xPosition(), place.yPosition(), colorByBehavior(element.behavior()));
             else
                 area.pixel(place.xPosition(), place.yPosition(), Color.BLACK);
         }
         area.update();
     }
     
     /**
      * Get the color associated with the behavior
      * @param behavior The behavior to look-up in the ColorMap
      * @return The color associated with behavior.
      * @require colorMap.containsKey(behavior)
      * @ensure colorByBehavior(behavior).equals(colorMap.get(behavior.toString())
      */
     public Color colorByBehavior(Behavior behavior)
     {
         Color c = colorMap.get(behavior);
         assert c != null : "Color not defined for this Behavior";
         return c;
     }
     
     /**
      * Maps each color in the behaviors-map to the associated behaviors name (Behavior.toString()).
      * 
      * @require For every two distrinct behavior objects b1, b2 in
      * behaviors, b1.toString() != b2.toString() must hold.
      * 
      * @ensure this.colorByBehavior(b).equals(behaviors.get(b)) for all Behavior objects b in behaviors.
      */
     private void makeBehaviorListAndColorMap(Map<Behavior, Color> behaviors)
     {
         this.behaviors = new ArrayList<Behavior>();
         this.colorMap = new HashMap<Behavior, Color>();
         
         //go through all the behaviors in the behaviors-map and add its string representation to new map 
         for(Map.Entry<Behavior, Color> entry : behaviors.entrySet()) {
         	Behavior currentBehavior = entry.getKey();
             assert !this.colorMap.containsKey(currentBehavior) : "Duplicate behaviors in Behavior-Color Map";
             this.behaviors.add(currentBehavior);
             this.colorMap.put(currentBehavior, entry.getValue());
         }
     }
     
     /**
      * Disable the control buttons of this frame. The biotope-simulation is stopped.
      */
     public void disableButtons() {
         newBiotope.setEnabled(false);
         nextRound.setEnabled(false);
         numberOfRounds.setEnabled(false);
         severalRounds.setEnabled(false);
         unlimitedRounds.setEnabled(false);
         stopRounds.doClick();
         stopRounds.setEnabled(false);
     }
     
     /**
      * Enable the control buttons of this frame.
      */
     public void enableButtons() {
         newBiotope.setEnabled(true);
         nextRound.setEnabled(true);
         numberOfRounds.setEnabled(true);
         severalRounds.setEnabled(true);
         unlimitedRounds.setEnabled(true);
         stopRounds.setEnabled(true);
     }
     
     /**
      * Get the combined size of all components preferred sizes plus 50 extra pixels in width and height.
      */
     public Dimension combinedSize() {
         BorderLayout layout = (BorderLayout) this.getContentPane().getLayout();
         Dimension centerDim = layout.getLayoutComponent(BorderLayout.CENTER).getPreferredSize();
         Dimension topDim = layout.getLayoutComponent(BorderLayout.NORTH).getPreferredSize();
         Dimension leftDim = layout.getLayoutComponent(BorderLayout.WEST).getPreferredSize();
         Dimension rightDim = layout.getLayoutComponent(BorderLayout.EAST).getPreferredSize();
         Dimension bottomDim = layout.getLayoutComponent(BorderLayout.SOUTH).getPreferredSize();
         
         /* Remark: With a BorderLayout the top and bottom areas cover the entire width of
          * of the frame, while the right and left areas only cover the part of the 
          * right and left frame borders not covered by the top and bottom areas.
          * This is reflected in a structural difference in the computation of the
          * combined Dimension of the five areas of the BorderLayout.
          */  
         int width = 50 + Math.max(leftDim.width + centerDim.width + rightDim.width, Math.max(topDim.width, bottomDim.width));
         int height = 50 + topDim.height + bottomDim.height + Math.max(centerDim.height, Math.max(leftDim.height, rightDim.height));
         return new Dimension(width, height);
     }
     
     /**
      * Get the BiotopeController used in this GalapagosFrame.
      */
     public BiotopeController controller() {
         return controller;
     }
 
     /**
      * Get the Biotope simulated in this GalapagosFrame.
      */
     public Biotope biotope() {
         return biotope;
     }
 }
