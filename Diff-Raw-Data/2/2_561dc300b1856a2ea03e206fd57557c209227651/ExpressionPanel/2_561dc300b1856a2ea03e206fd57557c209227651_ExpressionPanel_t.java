 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package jevolution.ui;
 
 import javax.swing.*;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * This panel contains all the labels and text fields that the user can change.
  *
  * The reference to the parent ApplicationPanel is to make communication easy.
  * I could spent tons of time worrying about event wiring or just get things working with this container class.
  *
  * Be aware that the CreatureExpressionTextFields here are given a reference to the ApplicationPanel.
  * This class should only be used as an organizational tool.  Refactor stuff if it grows beyond that.
  * 
  * @author kuhlmancer
  */
 public class ExpressionPanel extends JPanel {
 	private ApplicationPanel parent;
 
 	private final static String initialStrengthFunction = "blue - red - green + 3*(width - height)";
 	private final static String initialCostOfLivingFunction = "0.01 * width * height * acceleration";
 
 	private final static int initialSimulationSpeed = 1;
 	private final static int maxSimulationSpeed = 20;
 
 	private final static double initialMatingsPerSecond = 20;
 	private final static int maxMatingsPerSecond = 40;
 
 	private final static double initialRandomCreaturesCreatedPerSecond = 1;
 	private final static int maxRandomCreaturesPerSecond = 20;
 
 	public ExpressionPanel(ApplicationPanel app) {
 		super(new MigLayout("fillx, wrap", "left", "[top|]50[|]50[||]"));
 		
 		this.parent = app;
 
 		// strength function
 		this.add(new JLabel("How much energy per second that creatures steal while they touch each other:"));
 		this.add(new CreatureExpressionTextField(initialStrengthFunction, ExpressionId.STRENGTH, app), "growx");
 
 		// cost of living
		this.add(new JLabel("Energy cost of living per second:"));
 		this.add(new CreatureExpressionTextField(initialCostOfLivingFunction, ExpressionId.COST_OF_LIVING, app), "growx");
 
 		// speed
 		this.add(new ValueSliderWithLabel("Simulation speed: %.2fx", ValueId.SIMULATION_SPEED, maxSimulationSpeed, initialSimulationSpeed, app), "growx");
 
 		// mate rate
 		this.add(new ValueSliderWithLabel("Matings per second: %.2f", ValueId.MATINGS_PER_SECOND, maxMatingsPerSecond, initialMatingsPerSecond, app), "growx");
 
 		// random rate
 		this.add(new ValueSliderWithLabel("Random creatures created per second: %.2f", ValueId.RANDOM_CREATURES_PER_SECOND, maxRandomCreaturesPerSecond, initialRandomCreaturesCreatedPerSecond, app), "growx");
 	}
 }
