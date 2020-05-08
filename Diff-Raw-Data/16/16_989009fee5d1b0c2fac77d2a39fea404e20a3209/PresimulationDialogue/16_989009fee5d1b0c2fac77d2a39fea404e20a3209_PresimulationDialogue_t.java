 package gui;
 
 import javax.swing.*;
 
 import tape.ConsoleTape;
 import tape.TapeException;
 
 import machine.Machine;
 import machine.Simulation;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class PresimulationDialogue extends JDialog implements ActionListener{
 	private static final long serialVersionUID = 5423418738498074784L;
 
 	/**
 	 * Represents the return values for this window
 	 */
 	public enum ReturnValue {
 		CANCEL, SIMULATE
 	}
 
 	JPanel panelText, panelButtons;
 	JLabel labelText, labelDuration;
 	JButton buttonCancel, buttonSimulate;
 	Machine machine;
 	Simulation sim;
 	String duration;
 	int simulationTime;
 	/**
 	 * Stores the value returned by the dialog
 	 */
 	private ReturnValue returnValue;
 
 	PresimulationDialogue(Machine clonedMachine){
 		this.setModal(true);
 		duration = new String();
 		this.panelText = new JPanel();
 		this.panelButtons = new JPanel();
 		this.labelText = new JLabel();
 		this.labelDuration = new JLabel();
 		this.buttonCancel = new JButton("Cancel");
 		this.buttonSimulate = new JButton("Simulate");
 		this.machine = clonedMachine;
 		this.init();
 
 	}
 
 	private void init(){
 		//TODO: make it beautiful!
 		this.setTitle("Presimulation");
 		this.setLayout(new GridBagLayout());
 		
 		
 		
 		this.panelText.setLayout(new GridLayout(3,0));
 		this.panelText.add(this.labelText);
 		this.panelText.add(this.labelDuration);
 
 		this.buttonCancel.addActionListener(this);
 		this.buttonSimulate.addActionListener(this);
 		panelButtons = new JPanel();
 		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
 		panelButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		panelButtons.add(buttonCancel);
 		panelButtons.add(Box.createHorizontalGlue());
 		panelButtons.add(Box.createRigidArea(new Dimension(10, 0)));
 		panelButtons.add(this.buttonSimulate);
 
 		getRootPane().setDefaultButton(this.buttonSimulate);
 		
 	
 		this.setSize(250, 150);
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		
 		int numberOfTapes = this.machine.getNumberOfTapes();
 
 		for(int i = 0; i < numberOfTapes; i++){
 			ConsoleTape t = new ConsoleTape();
 			try {
 				t.setInputWord(machine.getTapes().get(i).getInputWord());
 			} catch (TapeException e1) {
 				ErrorDialog.showError("Error changing Tapetype", e1);
 			}	
 			this.machine.getTapes().set(i,t);
 		}
 
 		try {
 			this.machine.initTapes();
 			this.machine.writeInputWords();
 			this.machine.getTapes().get(0).getWriteInputWordThread().join();
 			this.sim = this.machine.createSimulation();
 			this.sim.getSimulationThread().start();
 			sim.getSimulationThread().join();
 		} 
 
 		catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (TapeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		this.labelText.setText("The simulation needs " +Integer.toString(this.sim.getNumberOfSteps()) +" Steps." );
 		if(this.simulationTime() < 1){
 			this.labelDuration.setText("This will last less then one minute.");
 		}
 		else{
 		this.labelDuration.setText("This will last about " + Integer.valueOf(this.simulationTime())+" minutes.");
 		
 		}
 		
 		GridBagConstraints windowConstraints = new GridBagConstraints();
 		windowConstraints.gridx = 0;
 		windowConstraints.gridy = 0;
 		windowConstraints.weightx = 1.0;
 		windowConstraints.weighty = 0.05;
 		this.add(this.panelText);
 		windowConstraints.gridy = 1;
 		
 
 		this.add(this.panelButtons, windowConstraints);
 		
 		this.centerDialogOnTheScreen();
 	}
 
 	/**
 	 * Shows the window
 	 * @return RUN/CANCEL depending on the user decision
 	 */
 	public ReturnValue showDialogue() {
 		this.setVisible(true);
 
 		return returnValue;
 	}
 	
 	public int simulationTime(){
		if(this.machine.getType() == Machine.MachineType.BrainfuckMachine){
			this.simulationTime = this.sim.getNumberOfSteps() * 500;
		}
		else if(this.machine.getType() == Machine.MachineType.TuringMachine){
 		this.simulationTime= (this.sim.getNumberOfMovements() + this.sim.getNumberOfWritings() * 500);
		}
 		return this.simulationTime/(1000*60);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == this.buttonCancel){
 			dispose();
 			this.returnValue = ReturnValue.CANCEL;
 		}
 		else if(e.getSource() == this.buttonSimulate){
 			dispose();
 			this.returnValue = ReturnValue.SIMULATE;
 		}
 	}
 	
 	/**
 	 * Center the dialog window on the screen
 	 */
 	private void centerDialogOnTheScreen() {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension dialogSize = this.getSize();
 		int centerPosX = (screenSize.width - dialogSize.width) / 2;
 		int centerPosY = (screenSize.height - dialogSize.height) / 2;
 		setLocation(centerPosX, centerPosY);
 	}
 
 }
