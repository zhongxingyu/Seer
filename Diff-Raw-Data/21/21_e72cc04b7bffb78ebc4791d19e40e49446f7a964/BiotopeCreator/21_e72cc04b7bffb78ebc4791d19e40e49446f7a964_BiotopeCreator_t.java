 package galapagos;
 
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.plaf.basic.BasicSeparatorUI;
 
 /**
  * A Dialog
  *
  */
 public class BiotopeCreator extends JDialog {
     private final JSpinner widthSpinner, heightSpinner;
     private final JSpinner breedingProbabilitySpinner;
     private final JSpinner maxHitpointsSpinner, initialHitpointsSpinner, hitpointsPerRoundSpinner;
     private final JSpinner minMaxAgeSpinner, maxMaxAgeSpinner;
     private final JSpinner finchesPerBehaviorSpinner;
     private final JCheckBox[] behaviorCheckboxes;
     private final JCheckBox selectAll;
     private final JButton okButton, cancelButton;
     private final JButton setVariantOneButton, setVariantTwoButton, setVariantThreeButton;
     private List<Behavior> behaviors;
     private Biotope biotope;
 
     private static final Dimension standardSpinnerSize = new Dimension(100,22);
     
     public BiotopeCreator(List<Behavior> behaviors) {
     	this.behaviors = behaviors;
     	this.setModal(true);
         // The world size options.
         JPanel sizeOptionGroup = new JPanel(new GridBagLayout());
         sizeOptionGroup.setBorder(BorderFactory.createTitledBorder("World size"));
         widthSpinner = newIntegerSpinner(100, 10, 1, "The width of the world.");
         heightSpinner = newIntegerSpinner(100, 10, 1, "The height of the world.");
         sizeOptionGroup.add(new JLabel("Width",SwingConstants.CENTER), getComponentConstraints(0,0, GridBagConstraints.CENTER));
         sizeOptionGroup.add(widthSpinner, getComponentConstraints(0,1, GridBagConstraints.CENTER));
         sizeOptionGroup.add(new JLabel("Height",SwingConstants.CENTER), getComponentConstraints(0,2, GridBagConstraints.CENTER));
         sizeOptionGroup.add(heightSpinner, getComponentConstraints(0,3, GridBagConstraints.CENTER));
         
         // Hitpoint options.
         JPanel hitpointsOptionGroup = new JPanel(new GridBagLayout());
         hitpointsOptionGroup.setBorder(BorderFactory.createTitledBorder("Finch hitpoints"));
         initialHitpointsSpinner = newIntegerSpinner(7, 1, 1, "A finch starts with this amount of hitpoints.");
         maxHitpointsSpinner = newIntegerSpinner(20, 1, 1, "The maximum number of hitpoints a finch can have.");
         hitpointsPerRoundSpinner = newIntegerSpinner(3, 1, 0, "The number of hitpoints lost each round due to ticks.");
         hitpointsOptionGroup.add(new JLabel("Initial hitpoints",SwingConstants.CENTER), getComponentConstraints(0,0, GridBagConstraints.CENTER));
         hitpointsOptionGroup.add(initialHitpointsSpinner, getComponentConstraints(0,1, GridBagConstraints.CENTER));
         hitpointsOptionGroup.add(new JLabel("Max. hitpoints",SwingConstants.CENTER), getComponentConstraints(0,3, GridBagConstraints.CENTER));
         hitpointsOptionGroup.add(maxHitpointsSpinner, getComponentConstraints(0,4, GridBagConstraints.CENTER));
         hitpointsOptionGroup.add(new JLabel("Hitpoints lost per round",SwingConstants.CENTER), getComponentConstraints(0,5, GridBagConstraints.CENTER));
         hitpointsOptionGroup.add(hitpointsPerRoundSpinner, getComponentConstraints(0,6, GridBagConstraints.CENTER));
         
         // Age options.
         JPanel ageOptionGroup = new JPanel(new GridBagLayout());
         ageOptionGroup.setBorder(BorderFactory.createTitledBorder("Finch age"));
         minMaxAgeSpinner = newIntegerSpinner(10, 1, 2, 
                 "<HTML>The minimum age a finch must have<br>" +
                 "before it can die of old age.</HTML>");
         maxMaxAgeSpinner = newIntegerSpinner(13, 1, 2, 
                 "<HTML>The minimum age a finch can have<br>" +
                 "before it dies of old age.</HTML>");
         ageOptionGroup.add(new JLabel("Least maximum age",SwingConstants.CENTER), getComponentConstraints(0,0, GridBagConstraints.CENTER));
         ageOptionGroup.add(minMaxAgeSpinner, getComponentConstraints(0,1, GridBagConstraints.CENTER));
         ageOptionGroup.add(new JLabel("Greatest maximum age",SwingConstants.CENTER), getComponentConstraints(0,2, GridBagConstraints.CENTER));
         ageOptionGroup.add(maxMaxAgeSpinner, getComponentConstraints(0,3, GridBagConstraints.CENTER));
                
         // Breeding probability and Finches per Behavior.
         JPanel otherOptionGroup = new JPanel(new GridBagLayout());
         otherOptionGroup.setBorder(BorderFactory.createTitledBorder("Other parametres"));
         breedingProbabilitySpinner = new JSpinner(new RevisedSpinnerNumberModel(1.0/6.0,0.0,1.0,0.01));
         breedingProbabilitySpinner.setPreferredSize(new Dimension(50,22));
         breedingProbabilitySpinner.setToolTipText(
                 "<HTML>The probability that a given finch<br>" +
                 "breeds in the beginning of a round.</HTML>");
         finchesPerBehaviorSpinner = newIntegerSpinner(40, 1, 0, 
                 "<HTML>The number of finches of each chosen<br>" +
                 "behavior in the new biotope.</HTML>");
         otherOptionGroup.add(new JLabel("Breeding probability",SwingConstants.CENTER), getComponentConstraints(0,0, GridBagConstraints.CENTER));
         otherOptionGroup.add(breedingProbabilitySpinner, getComponentConstraints(0,1, GridBagConstraints.CENTER));
         otherOptionGroup.add(new JLabel("Finches per behavior",SwingConstants.CENTER), getComponentConstraints(0,2, GridBagConstraints.CENTER));
         otherOptionGroup.add(finchesPerBehaviorSpinner, getComponentConstraints(0,3, GridBagConstraints.CENTER));
         
         // Behavior selection.
         JPanel behaviorsOptionGroup = new JPanel(new GridBagLayout());
         behaviorsOptionGroup.setBorder(BorderFactory.createTitledBorder("Behaviors"));
         
         //Create a checkbox to select all behaviors.
         selectAll = new JCheckBox("Select all behaviors", true);
         selectAll.setToolTipText("Select/deselect all behaviors.");
         selectAll.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) 
         	{
         		for (int i = 0; i < behaviorCheckboxes.length; i++)
         			behaviorCheckboxes[i].setSelected(selectAll.isSelected());
         	}
         });
         
         behaviorsOptionGroup.add(selectAll);
         behaviorsOptionGroup.add(selectAll, getComponentConstraints(0, 0, GridBagConstraints.WEST));
         
         //create a seperation between the select all checkbox and the rest
         JSeparator seperator = new JSeparator();
         seperator.setUI(new BasicSeparatorUI());
         behaviorsOptionGroup.add(seperator, getContainerConstraints(0, 1, GridBagConstraints.REMAINDER,  1));
         
         behaviorCheckboxes = new JCheckBox[this.behaviors.size()];
         for (int i = 0; i < behaviorCheckboxes.length; i++) {
             behaviorCheckboxes[i] = new JCheckBox(this.behaviors.get(i).toString(), true);
             behaviorCheckboxes[i].setToolTipText(this.behaviors.get(i).description());
             behaviorsOptionGroup.add(behaviorCheckboxes[i], getComponentConstraints(i / 8, 1 +i % 8, GridBagConstraints.WEST));
         }
         
         // OK and CANCEL.
         JPanel buttonPanel = new JPanel(new FlowLayout());
         okButton = new JButton("Genesis!");
         okButton.setToolTipText("Create a new biotope with the selected specifikations.");
         okButton.setActionCommand("okButton");
         okButton.addActionListener(new ActionListener () {
             public void actionPerformed(ActionEvent e) {
                 biotope = createBiotope();
             }
         });
         cancelButton = new JButton("Abort creation");
         cancelButton.setToolTipText("Return to the old biotope. No changes are made.");
         cancelButton.setActionCommand("cancelButton");
         cancelButton.addActionListener(new ActionListener () {
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
             }
         });
         buttonPanel.add(okButton);
         buttonPanel.add(cancelButton);
 
         JPanel standardSettingsPanel = new JPanel(new FlowLayout());
         setVariantOneButton = new JButton("Set variant #1");
         setVariantOneButton.setToolTipText(
                 "<HTML>Set specifikation to standard variant 1,<br>" +
                 "with five random behaviors chosen.</HTML>");
         setVariantOneButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     setConfiguration(1.0/6.0, 3, 20, 7, 10, 13, 40);
                     selectRandomBehaviors(5);
                 }
             });
         setVariantTwoButton = new JButton("Set variant #2");
         setVariantTwoButton.setToolTipText(
                 "<HTML>Set specifikation to standard variant 2,<br>" +
                 "with five random behaviors chosen.</HTML>");
         setVariantTwoButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     setConfiguration(1.0/6.0, 4, 20, 10, 20, 23, 40);
                     selectRandomBehaviors(5);
                 }
             });
         setVariantThreeButton = new JButton("Set variant #3");
         setVariantThreeButton.setToolTipText(
                 "<HTML>Set specifikation to standard variant 3,<br>" +
                 "with five random behaviors chosen.</HTML>");
         setVariantThreeButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     setConfiguration(1.0/12.0, 3, 20, 13, 11, 14, 40);
                     selectRandomBehaviors(5);
                 }
             });
         standardSettingsPanel.add(setVariantOneButton);
         standardSettingsPanel.add(setVariantTwoButton);
         standardSettingsPanel.add(setVariantThreeButton);
 
         Container bottomButtonsContainer = Box.createVerticalBox();
         bottomButtonsContainer.add(standardSettingsPanel);
         bottomButtonsContainer.add(buttonPanel);
         
         JPanel options = new JPanel(new GridBagLayout());
         options.add(sizeOptionGroup, getContainerConstraints(0,0,1,1));
         options.add(hitpointsOptionGroup, getContainerConstraints(0,1,1,1));
         options.add(ageOptionGroup, getContainerConstraints(1,0,1,1));
         options.add(otherOptionGroup, getContainerConstraints(1,1,1,1));
         options.add(behaviorsOptionGroup, getContainerConstraints(2,0,1,2));
         
         this.setDefaultCloseOperation(HIDE_ON_CLOSE);
         this.setLayout(new BorderLayout());
         this.add(options, BorderLayout.CENTER);
         this.add(bottomButtonsContainer, BorderLayout.SOUTH);
         this.setTitle("Biotope Creator");
         
         this.setSize(getPreferredSize().width + 20, getPreferredSize().height + 40);
     }
     
     /**
      * A set of GridBagConstraints for use with the GridBagLayout. Recommended for containers.
      * @param x the horisontal position of the component.
      * @param y the vertical position of the component.
      * @param xSize the horisontal size of the component. How many columns the component
      * covers in the layout.
      * @param ySize the vertical size of the component. How many rows the component
      * covers in the layout.
      */            
     private GridBagConstraints getContainerConstraints (int x, int y, int xSize, int ySize) {
         return new GridBagConstraints(x, y, xSize, ySize, 1.0, 1.0, 
                                         GridBagConstraints.CENTER,
                                         GridBagConstraints.BOTH,
                                         new Insets(0,0,0,0),
                                         0, 0);
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
     
     /**
      * Randomly select the given number of behaviors from the list
      * of behaviors.
      *
     * @param count How many behaviors that should be selected.
      *
      * @require count <= behaviorCheckboxes.length
      */
     private void selectRandomBehaviors(int count) {
         assert (count <= behaviorCheckboxes.length)
             : "More behaviors required than is available";
         LinkedList<Integer> list = new LinkedList<Integer>();
         for (Integer i = 0; i < behaviorCheckboxes.length; i++)
             list.add(i);
         Collections.shuffle(list);
         for (JCheckBox box : behaviorCheckboxes)
             box.setSelected(false);
         for (int i = 0; i < 5; i++)
             behaviorCheckboxes[list.get(i)].setSelected(true);
     }
 
     /**
     * Set the Biotope parameters edited by this BiotopeCreator by
      * changing the values of the control widgets.
      *
      * @param breedingProbability The chance each finch has of
      * reproducing each round.
     * @param roundPrice The amount of hit points each finch will
      * lose every round.
      * @param maxHitpoints The maximum amount of hit points a
      * single finch will be able to have.
      * @param startHitpoints The number of hit points a newly
      * created finch will have.
      * @param minMaxAge The lower bound on the max age of a finch.
     * @param maxMaxAge The upper bound on the max age of a finch.
      * @param initialFinchesPerBehavior The amount of finches
      * created for each behavior at the onset of the simulation.
      */
     private void setConfiguration(double breedingProbability, int roundPrice, 
                                   int maxHitpoints, int startHitpoints,
                                   int minMaxAge, int maxMaxAge,
                                   int initialFinchesPerBehavior) {
         breedingProbabilitySpinner.setValue(breedingProbability);
         maxHitpointsSpinner.setValue(maxHitpoints);
         initialHitpointsSpinner.setValue(startHitpoints);
         hitpointsPerRoundSpinner.setValue(roundPrice);
         minMaxAgeSpinner.setValue(minMaxAge);
         maxMaxAgeSpinner.setValue(maxMaxAge);
         finchesPerBehaviorSpinner.setValue(initialFinchesPerBehavior);
     }
     
     /**
      * Create a new spinner with a startValue, stepSize and minValue.
      * The max value is set to Integer.MAX_VALUE.
      * @param startValue The spinner's start-value.
      * @param stepSize The step size of the spinner.
      * @param toolTip The tool tip of the spinner.
      * @return The created spinner.
      */
     private JSpinner newIntegerSpinner(int startValue, int stepSize, int minValue, String toolTip)
     {
         JSpinner spinner = new JSpinner(new RevisedSpinnerNumberModel(startValue, minValue, Integer.MAX_VALUE, stepSize));
         spinner.setPreferredSize(standardSpinnerSize);
         spinner.setToolTipText(toolTip);
         return spinner;
     }
     
     public Biotope biotope() {
     	return biotope;
     }
 
     public Biotope createBiotope() {
         //Get the user input from the spinners.
         int width = (Integer) this.widthSpinner.getValue();
         int height = (Integer) this.heightSpinner.getValue();
         double breedingProbability = (Double) this.breedingProbabilitySpinner.getValue();
         int maxHitpoints = (Integer) this.maxHitpointsSpinner.getValue();
         int initialHitpoints = (Integer) this.initialHitpointsSpinner.getValue();
         int hitpointsPerRound = (Integer) this.hitpointsPerRoundSpinner.getValue();
         int minMaxAge = (Integer) this.minMaxAgeSpinner.getValue();
         int maxMaxAge = (Integer) this.maxMaxAgeSpinner.getValue();
         int finchesPerBehavior = (Integer) this.finchesPerBehaviorSpinner.getValue();
         
         //Get a list of the users chosen finches.
         List<Behavior> finchBehaviors = new LinkedList<Behavior>();
         for(int i = 0; i < behaviors.size(); ++i)
             if(behaviorCheckboxes[i].isSelected())
                 finchBehaviors.add(behaviors.get(i));
         
         //Check that the values are legal
         if (checkStartFinches(width, height, finchesPerBehavior, finchBehaviors.size()) 
                 & checkAge(minMaxAge, maxMaxAge) & checkHitpoints(maxHitpoints, initialHitpoints)) {
 
             Biotope biotope = new Biotope(width,height,breedingProbability,
                                   maxHitpoints,initialHitpoints,hitpointsPerRound,minMaxAge,
                                   maxMaxAge,finchesPerBehavior,finchBehaviors);
             
             this.setVisible(false);
             return biotope;            
         }
 
         return null;
     }
     
     /**
      * Check that user-chosen world is big enough for the number of finches that should be spawned.
      * Shows a messagebox if the values are illegal.
      * @param width The width of the world.
      * @param height The height of the world.
      * @param finchesPerBehavior How many finches that should be spawned per behavior.
      * @param numberOfBehaviors How many different behaviors the user has chosen.
      * @return true if the values are legal.
      */
     public boolean checkStartFinches(int width, int height, int finchesPerBehavior, int numberOfBehaviors) {
         if (width * height >= finchesPerBehavior * numberOfBehaviors) {
             return true;
         } else {
             JOptionPane.showMessageDialog(this,
                     "There is not enough room in the world for the initial amount of finches.",
                     "Impossible to create biotope", JOptionPane.WARNING_MESSAGE);
             return false;            
             
         }
     }
     
     /**
      * Check that the user-chosen age values are legal and show a messagebox if not.
      * (The minimum age must be smaller than the maximum age)
      * Shows a messagebox if the values are illegal
      * @param minMaxAge The user-chosen minimum age.
      * @param maxMaxAge The user-chosen maximum age.
      * @return true if the values are legal.
      */
     public boolean checkAge(int minMaxAge, int maxMaxAge) {
         if (minMaxAge <= maxMaxAge) {
             return true;
         } else {
             JOptionPane.showMessageDialog(this,
                     "The greatest maximum age must be at least as large as the least maximum age.",
                     "Impossible to create biotope", JOptionPane.WARNING_MESSAGE);
             return false;
         }
     }
     
     /**
      * Check that the user-chosen hitpoint values are legal.
      * (The finches can't start with more hitpoints than the maximum) 
      * Shows a messagebox if they are illegal.
      * @ensure (initialHitpoints <= maxHitpoints) ? true : false
      * @param maxHitpoints The user-chosen maximum hitpoints-value.
      * @param initialHitpoints The user-chosen initial hitpoints-value.
      * @return true if the values are legal.
      */
     public boolean checkHitpoints(int maxHitpoints, int initialHitpoints) {
         if (initialHitpoints <= maxHitpoints) {
             return true;
         } else {
             JOptionPane.showMessageDialog(this,
                     "The initial amount of hitpoints may at most be the maximum amount of hitpoints.",
                     "Impossible to create biotope", JOptionPane.WARNING_MESSAGE);
             return false;
         }
     }
 }
