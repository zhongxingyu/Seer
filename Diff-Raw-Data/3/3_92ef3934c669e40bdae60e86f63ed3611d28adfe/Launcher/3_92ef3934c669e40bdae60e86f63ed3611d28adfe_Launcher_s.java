 package uprm.ece.icom4215.ar5;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.*;
 import javax.swing.text.DefaultCaret;
 
 import uprm.ece.icom4215.exceptions.InvalidAddressException;
 import uprm.ece.icom4215.exceptions.InvalidAddressValueException;
 import uprm.ece.icom4215.util.NotationConversion;
 
 
 @SuppressWarnings("serial")
 public class Launcher extends JFrame {
 
 	//File Path
 	static final String FILE_PATH = "./happyHour.txt";
 
 	/* GUI Fields */
 	// IR | SR
 	JLabel IRLabel, SRLabel;	
 	JTextField IRTextField, SRTextField;	
 
 	// PC | Keybd
 	JLabel PCLabel, KBLabel;	
 	JTextField PCTextField, KBTextField;
 
 
 	// A | Display
 	JLabel ALabel, DisplayLabel;
 	JTextField ATextField, DisplayTextField;
 
 	// Registers (Left)
 	JLabel R0Label, R1Label, R2Label, R3Label,
 	R4Label, R5Label, R6Label, R7Label;
 
 	JTextField R0TextField, R1TextField, R2TextField,
 	R3TextField, R4TextField, R5TextField,
 	R6TextField, R7TextField;
 
 	// Memory (Right)
 	JLabel MemoryLabel;
 	JTextArea MemoryTextArea;
 
 	// Buttons
 	JButton stepButton, runButton;
 
 	/**
 	 * Constructor for Launcher; initializes the AR5 main
 	 * components, and calls the external method that initializes
 	 * the GUI.
 	 */
 	public Launcher(){
 		//Initializes the processor.
 		RISC_AR5.init();
 
 		//Manages active and focus window. Used to receive all the
 		//KeyEvents generated when this application is on focus.
 		KeyboardFocusManager manager = KeyboardFocusManager.
 				getCurrentKeyboardFocusManager();
 		//Custom event dispatcher that handles key presses on the
 		//application, when it is focused.
 		manager.addKeyEventDispatcher(new MyDispatcher());
 
 		// Load memory
 		try {
 			RISC_AR5.memory.loadMemory(FILE_PATH);
 		} catch(Exception e) {
 			//Verify that the file exists and complies with the requirements
 			//for this processor. Usage: 4 digit hex word per line with a 
 			//maximum amount of 64 lines.
 			e.printStackTrace();
 		};
 
 		// initialize GUI and all its text fields
 		this.initGUI();
 		this.updateRegisters();
 		this.updateMemory();		
 	}
 
 	/**
 	 * Launch an instance of the simulator.
 	 * @param args
 	 */
 	public static void main(String[] args){
 		new Launcher();		
 	}
 
 	/**
 	 * Initializes the graphical user interface with all the defined
 	 * fields set with the values contained in the RISC_AR5. Sets properties
 	 * for each component of the interface.
 	 */
 	public void initGUI() {
 		// Create the frame, position it and handle closing it		
 		this.setSize(400,400);
 		// toolkit allows to interact with the OS
 		Toolkit tk = Toolkit.getDefaultToolkit();
 
 		// get system dimensions
 		Dimension dim = tk.getScreenSize();		
 
 		// for centering our frame
 		int xPos = (dim.width / 2) - (this.getWidth() / 2);
 		int yPos = (dim.height / 2) - (this.getHeight() / 2);
 		this.setLocation(xPos, yPos);
 
 		this.setResizable(false);
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		this.setTitle("RISC AR5 Simulator");
 
 		this.setLayout(new BorderLayout());
 
 		// Top Panel to present project members
 		JPanel panelTop = new JPanel();
 		panelTop.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panelTop.setBackground(Color.WHITE);				
 		panelTop.add(new JLabel("By: Frances Acevedo, Erick Caraballo, Manuel Saldana"));
 
 
 		// Middle Panel for TextFields and Labels
 		JPanel panelMiddle = new JPanel();
 		panelMiddle.setBackground(Color.ORANGE);
 		//Set the Middle panel with a grid bag layout,
 		//making it convenient to establish columns.
 		panelMiddle.setLayout(new GridBagLayout());		
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		gbc.gridx = 0;     gbc.gridy = 0;
 
 		gbc.gridwidth = 1; gbc.gridheight = 1;
 
 		gbc.insets = new Insets(2,5,2,5);
 
 		gbc.anchor = GridBagConstraints.NORTH;
 
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 
 
 		// IR | SR
 		IRLabel = new JLabel("IR");		
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		panelMiddle.add(IRLabel, gbc);
 
 		IRTextField = new JTextField(RISC_AR5.registers.getIR(), 13);
 		IRTextField.setEditable(false);
 		gbc.gridwidth = 7;
 		gbc.gridx = 1;
 		gbc.gridy = 0;
 		panelMiddle.add(IRTextField, gbc);
 
 		SRLabel = new JLabel("SR");
 		gbc.gridwidth = 1;
 		gbc.gridx = 8;
 		gbc.gridy = 0;		
 		panelMiddle.add(SRLabel, gbc);
 
 		SRTextField = new JTextField(RISC_AR5.registers.getSR(), 4);
 		SRTextField.setEditable(false);
 		SRTextField.setHorizontalAlignment(JTextField.CENTER);
 		gbc.gridwidth = 4;
 		gbc.gridx = 9;
 		gbc.gridy = 0;
 		panelMiddle.add(SRTextField, gbc);
 
 
 		// PC | Keybd
 		PCLabel = new JLabel("PC");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 1;		
 		panelMiddle.add(PCLabel, gbc);
 
 		PCTextField = new JTextField(2);
 		PCTextField.setEditable(false);
 		gbc.gridwidth = 4;
 		gbc.gridx = 1;
 		panelMiddle.add(PCTextField, gbc);
 
 		KBLabel = new JLabel("Keybd");
 		gbc.gridwidth = 1;
 		gbc.gridx = 8;	
 		panelMiddle.add(KBLabel, gbc);
 
 		try {
 			KBTextField = new JTextField((char)Integer.parseInt(
 					RISC_AR5.memory.getAddress("251"),2)+"",2);
 
 		} catch (InvalidAddressException e) {
 			//Incorrect addressing - occurs if the parameter is a value not 
 			//between 0-255.
 			KBTextField = new JTextField("", 2);
 		}
 		KBTextField.setEditable(false);
 		//KBTextField.addKeyListener(new KBListener());
 		KBTextField.setHorizontalAlignment(JTextField.CENTER);
 		gbc.gridwidth = 2;
 		gbc.gridx = 9;	
 		panelMiddle.add(KBTextField, gbc);
 
 
 		// A | Display
 		ALabel = new JLabel("A");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 2;		
 		panelMiddle.add(ALabel, gbc);
 
 		ATextField = new JTextField(2);
 		ATextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(ATextField, gbc);
 
 		DisplayLabel = new JLabel("Display");
 		gbc.gridwidth = 1;
 		gbc.gridx = 8;	
 		panelMiddle.add(DisplayLabel, gbc);
 
 		DisplayTextField = new JTextField(4);
 		try {
 			DisplayTextField.setText(((char)Integer.parseInt(RISC_AR5.memory.getAddress("252"),2)+"")+
 					((char)Integer.parseInt(RISC_AR5.memory.getAddress("253"),2)+"")+
 					((char)Integer.parseInt(RISC_AR5.memory.getAddress("254"),2)+"")+
 					((char)Integer.parseInt(RISC_AR5.memory.getAddress("255"),2)+""));
 		} catch (NumberFormatException e) {
 			// Number is not an 8 bit binary word
 			e.printStackTrace();
 		} catch (InvalidAddressException e) {
 			// Address is not between 252-255
 			e.printStackTrace();
 		}
 		DisplayTextField.setEditable(false);
 		DisplayTextField.setHorizontalAlignment(JTextField.CENTER);
 		gbc.gridwidth = 4;
 		gbc.gridx = 9;	
 		panelMiddle.add(DisplayTextField, gbc);
 
 
 		// REGISTERS R0-7 (LEFT)		
 		// R0
 		R0Label = new JLabel("R0");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 3;		
 		panelMiddle.add(new JLabel("R0"), gbc);
 
 		R0TextField = new JTextField(8);
 		R0TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R0TextField, gbc);
 
 		// R1
 		R1Label = new JLabel("R1");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 4;		
 		panelMiddle.add(R1Label, gbc);
 
 		R1TextField = new JTextField(8);
 		R1TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R1TextField, gbc);
 
 		// R2
 		R2Label = new JLabel("R2");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 5;		
 		panelMiddle.add(R2Label, gbc);
 
 		R2TextField = new JTextField(8);
 		R2TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R2TextField, gbc);
 
 		// R3
 		R3Label = new JLabel("R3");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 6;		
 		panelMiddle.add(R3Label, gbc);
 
 		R3TextField = new JTextField(8);
 		R3TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R3TextField, gbc);
 
 		// R4
 		R4Label = new JLabel("R4");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 7;		
 		panelMiddle.add(R4Label, gbc);
 
 		R4TextField = new JTextField(8);
 		R4TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R4TextField, gbc);
 
 		// R5
 		R5Label = new JLabel("R5");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 8;		
 		panelMiddle.add(R5Label, gbc);
 
 		R5TextField = new JTextField(8);
 		R5TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R5TextField, gbc);
 
 		// R6
 		R6Label = new JLabel("R6");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 9;		
 		panelMiddle.add(R6Label, gbc);
 
 		R6TextField = new JTextField(8);
 		R6TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R6TextField, gbc);
 
 		// R7	
 		R7Label = new JLabel("R7");
 		gbc.gridwidth = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 10;		
 		panelMiddle.add(R7Label, gbc);
 
 		R7TextField = new JTextField(8);
 		R7TextField.setEditable(false);
 		gbc.gridwidth = 3;
 		gbc.gridx = 1;
 		panelMiddle.add(R7TextField, gbc);
 
 
 
 		// Memory (RIGHT)
 		MemoryLabel = new JLabel("Memory");
 		gbc.gridwidth = 1;
 		gbc.gridx = 8;	
 		gbc.gridy = 3;
 		panelMiddle.add(MemoryLabel, gbc);
 
 		MemoryTextArea = new JTextArea(8, 10);
 		MemoryTextArea.setEditable(false);
 		// Prevent it from scrolling to bottom when setText() is called.
 		DefaultCaret caret = (DefaultCaret)MemoryTextArea.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);		
 		JScrollPane MemScroll = new JScrollPane(MemoryTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		gbc.gridx = 8;	
 		gbc.gridy = 4;
 		gbc.gridwidth = 3;
 		gbc.gridheight = 7;
 		panelMiddle.add(MemScroll, gbc);
 
 		// Bottom Panel for Control Buttons
 		JPanel panelBottom = new JPanel();
 		panelBottom.setBackground(Color.WHITE);
 		stepButton = new JButton("Step >");		
 		stepButton.addActionListener(new StepButtonListener());
 
 		panelBottom.add(stepButton);
 
 		runButton = new JButton("Run >>");
 		runButton.addActionListener(new RunButtonListener());
 		panelBottom.add(runButton);
 
 		this.add(panelTop, BorderLayout.NORTH);
 		this.add(panelMiddle, BorderLayout.CENTER);
 		this.add(panelBottom, BorderLayout.SOUTH);
 
 		//Use pack() to set layouts to the preferred size.
 		this.pack();
 		this.setVisible(true);
 
 	}
 
 	public void updateMemory() {
 		MemoryTextArea.setText(RISC_AR5.memory.memoryToString());
 	}
 
 	public void updateIR() {
 		IRTextField.setText(RISC_AR5.registers.getIR());
 	}
 
 	public void updateSR() {
 		SRTextField.setText(RISC_AR5.registers.getSR());
 	}
 
 	public void updateRegisters() {
 		PCTextField.setText(RISC_AR5.registers.getPC());
 		SRTextField.setText(RISC_AR5.registers.getSR());
 		ATextField.setText(RISC_AR5.registers.getAcc());
 		IRTextField.setText(RISC_AR5.registers.getIR());
 
 		try {		
 			R0TextField.setText(RISC_AR5.registers.getRegister("000"));
 			R1TextField.setText(RISC_AR5.registers.getRegister("001"));
 			R2TextField.setText(RISC_AR5.registers.getRegister("010"));
 			R3TextField.setText(RISC_AR5.registers.getRegister("011"));
 			R4TextField.setText(RISC_AR5.registers.getRegister("100"));
 			R5TextField.setText(RISC_AR5.registers.getRegister("101"));
 			R6TextField.setText(RISC_AR5.registers.getRegister("110"));
 			R7TextField.setText(RISC_AR5.registers.getRegister("111"));
 
 		} catch(InvalidAddressException e) {
 			//Input to the getRegister() method was not a three bit word.
 			System.out.println(e);
 		}
 
 	}
 	
 	public void updateGUI() {
 		updateRegisters();
 		updateMemory();
 		try {
			KBTextField = new JTextField((char)Integer.parseInt(
					RISC_AR5.memory.getAddress("251"),2)+"",2);
 			DisplayTextField.setText(((char)Integer.parseInt(RISC_AR5.memory.getAddress("252"),2)+"")+
 					((char)Integer.parseInt(RISC_AR5.memory.getAddress("253"),2)+"")+
 					((char)Integer.parseInt(RISC_AR5.memory.getAddress("254"),2)+"")+
 					((char)Integer.parseInt(RISC_AR5.memory.getAddress("255"),2)+""));
 		} catch (NumberFormatException e) {
 			// Not an 8 bit word.
 			e.printStackTrace();
 		} catch (InvalidAddressException e) {
 			// Address not in range 0-255
 			e.printStackTrace();
 		}
 	}
 	
 
 	// Listener classes
 
 	/**
 	 * Handles the use of the "Step" GUI button. So long as it is possible
 	 * to execute an instruction, this listener will invoke the execution
 	 * if the button is pressed. If the instruction happens to be stop, then 
 	 * the button will be changed to not allow modification.  
 	 *
 	 */
 	private class StepButtonListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			//Execute the next instruction.
 			RISC_AR5.step();
 			//Disable Step if the stop instruction has been reached.
 			if(RISC_AR5.isStopped()){
 				//Alert user that no more instructions will be executed.
 				JOptionPane.showMessageDialog(null, "All instructions have been executed!",
 						"End of Instructions", JOptionPane.WARNING_MESSAGE);
 				stepButton.setEnabled(false);
 				runButton.setEnabled(false);
 			}
 
 			System.out.println("result: "+RISC_AR5.registers.getIR() + " | Acc:" + RISC_AR5.registers.getAcc() + "\n");
 
 			//Update  the GUI's fields.
 			updateGUI();            
 		}
 	}
 
 	/**
 	 * Handles the use of the "Run" GUI button. This listener will invoke the 
 	 * execution of all instructions until the a stop instruction is found.
 	 * When this happens, a message box will alert the user that no further
 	 * instructions can be made. Also the Run and Step buttons are deactivated. 
 	 *
 	 */
 	private class RunButtonListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			while(!RISC_AR5.isStopped()){
 				//Execute the next instruction.
 				RISC_AR5.step();
 				//Disable Step if the stop instruction has been reached.
 				if(RISC_AR5.isStopped()){
 					//Alert user that no more instructions will be executed.
 					JOptionPane.showMessageDialog(null, "All instructions have been executed!",
 							"End of Instructions", JOptionPane.WARNING_MESSAGE);
 					stepButton.setEnabled(false);
 					runButton.setEnabled(false);
 				}
 
 				System.out.println("result: "+RISC_AR5.registers.getIR() + " | Acc:" + RISC_AR5.registers.getAcc() + "\n");
 
 				//Update  the GUI's fields.
 				updateGUI();            
 			}
 		}
 	}
 
 	/**
 	 * Rather than using a listener for every component, this custom 
 	 * handles the event "KEY_PRESSED".
 	 *
 	 */
 	private class MyDispatcher implements KeyEventDispatcher {
 		@Override
 		public boolean dispatchKeyEvent(KeyEvent e) {        	
 			if (e.getID() == KeyEvent.KEY_PRESSED) {              
 				if (e.getKeyCode() != 16 && e.getKeyCode() != 20) {
 					KBTextField.setText((e.getKeyChar()+""));
 					int ascii_code = (int) KBTextField.getText().charAt(0);
 
 					// addresses for storing keyboard input are 250-251                	
 					try {
 						// choosing 250's byte to store MSB
 						RISC_AR5.memory.setAddress("250", NotationConversion.decimalTo8BitBinary(0));
 						RISC_AR5.memory.setAddress("251", NotationConversion.decimalTo8BitBinary(ascii_code));
 						updateMemory();
 					} catch (InvalidAddressValueException e1) {
 						e1.printStackTrace();
 					} catch (InvalidAddressException e1) {
 						e1.printStackTrace();
 					}
 
 					// testing
 					System.out.println("Key Pressed: " + ascii_code);
 				}
 			}
 			return false;
 		}
 	}
 
 
 }
