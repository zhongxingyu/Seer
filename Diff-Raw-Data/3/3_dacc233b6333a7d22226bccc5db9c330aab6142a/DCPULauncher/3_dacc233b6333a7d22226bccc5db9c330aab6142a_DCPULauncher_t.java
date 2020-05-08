 package net.ian.dcpu;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import net.ian.dcpu.DCPU.Register;
 
 public class DCPULauncher extends JPanel implements ActionListener, Runnable {
 	private static final long serialVersionUID = 1L;
 	
 	DCPU cpu;
 	Monitor monitor;
 	
 	Assembler assembler;
 	
 	JTextArea codeEntry;
 	
 	JLabel[][] registers;
     Cell special[];
 	JLabel[][] specialLabels;
 	JLabel instructionLabel[];
 	
 	public DCPULauncher() {
 		super();
 		cpu = new DCPU();
 		assembler = new Assembler();
 	}
 	
 	public void init() {	
         JFrame frame = new JFrame("DCPU-16");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         frame.setContentPane(this);
         
         JPanel output = new JPanel(new BorderLayout()); 
         
         codeEntry = new JTextArea(0, 40);
         codeEntry.setFont(new Font("Monospaced", Font.BOLD, 16));
         JScrollPane codeScroll = new JScrollPane(codeEntry);
         frame.add(codeScroll);
 
         Keyboard keyboard = new Keyboard(cpu);
         monitor = new Monitor(cpu);
         monitor.addKeyListener(keyboard);
         output.add(monitor, BorderLayout.NORTH);        
         
         JPanel buttonBox = new JPanel(new GridLayout(1, 0));
         
         JButton runButton = new JButton("Run");
         runButton.setActionCommand("run");
         runButton.addActionListener(this);
         buttonBox.add(runButton);
         
         JButton stepButton = new JButton("Step");
         stepButton.setActionCommand("step");
         stepButton.addActionListener(this);
         buttonBox.add(stepButton);
         
         JButton stopButton = new JButton("Stop");
         stopButton.setActionCommand("stop");
         stopButton.addActionListener(this);
         buttonBox.add(stopButton);
         
         output.add(buttonBox, BorderLayout.CENTER);
                
         JPanel panel = new JPanel(new GridLayout(0, 4));
         panel.add(new JLabel("Registers"));
         panel.add(new JLabel("Bin"));
         panel.add(new JLabel("Hex"));
         panel.add(new JLabel("Dec"));
         
         // 3 for Bin, Hex, and Dec
         registers = new JLabel[Register.values().length][];
         for (Register r : Register.values()) {
         	registers[r.ordinal()] = new JLabel[3];
         	panel.add(new JLabel(r.toString() + ": "));
         	for (int i = 0; i < 3; i++) {
         		registers[r.ordinal()][i] = new JLabel();
         		panel.add(registers[r.ordinal()][i]);
         	}
         }
         
         String specialNames[] = {"SP", "PC", "O"};
         special = new Cell[3];
         special[0] = cpu.SP;
         special[1] = cpu.PC;
         special[2] = cpu.O;
         specialLabels = new JLabel[specialNames.length][];
 
         for (int i = 0; i < specialNames.length; i++) {
         	specialLabels[i] = new JLabel[3];
         	panel.add(new JLabel(specialNames[i] + ": "));
         	for (int j = 0; j < 3; j++) {
         		specialLabels[i][j] = new JLabel();
         		panel.add(specialLabels[i][j]);
         	}
         }
         
         panel.add(new JLabel("Instruction:"));
         instructionLabel = new JLabel[3];
         for (int i = 0; i < 3; i++) {
         	instructionLabel[i] = new JLabel();
         	panel.add(instructionLabel[i]);
         }
         
         output.add(panel, BorderLayout.SOUTH);
         
         frame.add(output);
  
         // Display the window.
         frame.pack();
         frame.setResizable(false);
         frame.setLocationRelativeTo(null);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
     }
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		if (command.equals("run")) {
 			cpu.setMemory(assembler.assemble(codeEntry.getText()));
 			cpu.PC.value = 0;
 			new Thread(this).start();
 		} else if (command.equals("step")) {
 			if (!cpu.running) {
 				cpu.setMemory(assembler.assemble(codeEntry.getText()));
 				cpu.PC.value = 0;
 				cpu.running = true;
 			}
 			cycle();
 			updateMonitor();
			monitor.repaint();
 		} else if (command.equals("stop"))
 			cpu.running = false;
 	}
 	
 	public void run() {
         cpu.running = true;
         while (cpu.running) {
         	cycle();
         	
         	if ((cpu.instructionCount % 60) == 0)
             	updateMonitor();
         }
         
         updateMonitor();
        monitor.repaint();
 	}
 	
 	public void cycle() {
     	cpu.cycle();
     	
     	for (Register r : Register.values())
     		setLabels(registers[r.ordinal()], cpu.getRegister(r).value);
     	
     	for (int i = 0; i < special.length; i++)
     		setLabels(specialLabels[i], special[i].value);
     	
     	setLabels(instructionLabel, cpu.instructionCount);
 	}
 	
 	public void updateMonitor() {
     	// Rebuild the fonts!
     	for (int i = 0x8180; i < 0x8280; i += 2) {
     		monitor.buildFontCharacter((i - 0x8180) / 2, cpu.memory[i].value, cpu.memory[i+1].value);
     	}
     	
     	// Display stuff.
     	for (int i = 0x8000; i < 0x8180; i++) {
     		char character = (char)(cpu.memory[i].value & 127);
     		Color bgColor = Monitor.convertColor(cpu.memory[i].value >> 8);
     		Color fgColor = Monitor.convertColor(cpu.memory[i].value >> 12);
     		Monitor.MonitorCell cell = monitor.cells[i - 0x8000];
     		
     		cell.character = character;
     		cell.fgColor = fgColor;
     		cell.bgColor = bgColor;
     	}
     	
     	// Set border color.
     	monitor.borderColor = Monitor.convertColor(cpu.memory[0x8280].value);
     	monitor.render();
 	}
 	
 	private void setLabels(JLabel[] labels, int value) {
     	labels[0].setText(Integer.toBinaryString(value));
     	labels[1].setText("0x" + Integer.toHexString(value));
     	labels[2].setText(Integer.toString(value));
 	}
 	
 	public static void main(String[] args) {
 		DCPULauncher launcher = new DCPULauncher();
 		launcher.init();
 	}
 }
