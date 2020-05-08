 package ch.hszt.MiniPowerPC;
 
 import java.io.File;
 
 import ch.hszt.MiniPowerPC.GUI.MiniPowerPCGUI;
import ch.hszt.MiniPowerPC.helper.Helper;
 import ch.hszt.MiniPowerPC.helper.MemoryParser;
 
 public class Controller implements ControllerIfc {
 	private MiniPowerPC emu;
 	private MiniPowerPCGUI gui;
 
 	public Controller(MemoryEntry[] memory) {
 		emu = new MiniPowerPC(memory);
 		gui = new MiniPowerPCGUI(this);
 		updateGUI();
 	}
 
 	public MiniPowerPC getEmu() {
 		return emu;
 	}
 
 	public Controller(MemoryEntry[] memory, int startPosition) {
 		this(memory);
 		emu.setInstructionCounter(startPosition);
 		updateGUI(); 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		MemoryEntry[] m = new MemoryEntry[1024];
 
 		int i1 = 100;
 		int i2 = 150;
 
 		// Add example
 		m[100] = new MemoryEntry("0100000111110100".toCharArray()); // LWDD
 																	// 0,500
 		m[102] = new MemoryEntry("0100010111110110".toCharArray()); // LWDD
 																	// 1,502
 		m[104] = new MemoryEntry("0000011110000000".toCharArray()); // ADD 1
 		m[106] = new MemoryEntry("0110000111111000".toCharArray()); // SWDD
 																	// 0,504
 		m[108] = new MemoryEntry("0010000001111010".toCharArray()); // BD 122
 
 		// jump to 100
 		m[120] = new MemoryEntry("0010000001100100".toCharArray()); // BD 100
 		m[122] = new MemoryEntry("0000000000000000".toCharArray()); // END
 
 		m[500] = MemoryEntry.parseMemoryEntry(i1);
 		m[502] = MemoryEntry.parseMemoryEntry(i2);
 
 		Controller c = new Controller(m, 120);
 		/*
 		 * for (int i = 100; i < 1024; i++) { if (c.getEmu().getMemory()[i] !=
 		 * null) { System.out.println(i + " - " +
 		 * String.valueOf(c.getEmu().getMemory()[i] .getBinaryString())); } }
 		 */
 
 	}
 
 	@Override
 	public void updateGUI() {
 		gui.setAkku((gui.getBinDec() == "dec") ? String.valueOf(emu.getAkku().getNumericValue()) : String.valueOf(emu.getAkku().getBinaryString()));
 		if (emu.getInstructionReg() != null) {
 			gui.setBefReg(emu.getInstructionReg().toString(), emu
 					.getInstructionReg().getBinaryString());
 		}
 		gui.setBefZ(String.valueOf(emu.getInstructionCounter()));
 		gui.setReg1(gui.getBinDec().equals("dec") ? String.valueOf(emu.getRegister()[1].getNumericValue()) : String.valueOf(emu.getRegister()[1].getBinaryString()));
 		gui.setReg2(gui.getBinDec().equals("dec") ? String.valueOf(emu.getRegister()[2].getNumericValue()) : String.valueOf(emu.getRegister()[2].getBinaryString()));
 		gui.setReg3(gui.getBinDec().equals("dec") ? String.valueOf(emu.getRegister()[3].getNumericValue()) : String.valueOf(emu.getRegister()[3].getBinaryString()));
 		
 		gui.setCarryBit(String.valueOf(emu.getCarryFlag()));
 		gui.setCountBef(String.valueOf(emu.getStepCounter()));
 		gui.setMemoryEntries(emu.getMemory());
 	}
 	
 	@Override
 	public void runSimulationSlow(){
 		new Thread(simulationSlow).start();
 	}
 
 	//Simulation slow thread
 	private Runnable simulationSlow = new Runnable(){
 		@Override
 		public void run() {
 			while(emu.getInstructionReg() != null){
 				emu.nextStep();
 				updateGUI();
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
                        
                        if(emu.getMemory()[504] != null && emu.getMemory()[506] != null){
                            String resultString = String.valueOf(emu.getMemory()[506].getBinaryString()) + (String.valueOf(emu.getMemory()[504].getBinaryString())).substring(1);
                            System.out.println("Result (memory[504-507]):");
                            System.out.println("Binary: " + resultString);
                            System.out.println("Decimal: " + Helper.binaryCharArrayToInt(resultString.toCharArray(), true));
                        }
 		}
 	};
 	
 	@Override
 	public void runSimulation() {
 		emu.run();
 		updateGUI();
 	}
 
 	@Override
 	public void nextStep() {
 		emu.nextStep();
 		updateGUI();
 	}
 
 	public void loadProgramm(File inProgramm) throws IllegalArgumentException {
 		MemoryEntry[] m = MemoryParser.parseTextFile(inProgramm);
 		if (m != null) {
 			emu.setMemory(m);
 			emu.reset();
 			updateGUI();
 		}
 	}
 
 	public void setMultiplicationOP(int inMemoryPos1, int inOP1,
 			int inMemoryPos2, int inOP2) {
 		MemoryEntry[] m = MemoryParser.parseTextFile(new File("multiplikationsProgramm.txt"));
 		if(m != null){
 			m[inMemoryPos1] = MemoryEntry.parseMemoryEntry(inOP1);
 			m[inMemoryPos2] = MemoryEntry.parseMemoryEntry(inOP2);
 			
 			emu.setMemory(m);
 			emu.reset();
 			updateGUI();
 		}else{
 			System.out.println("Programm nicht gefunden!");
 		}
 	}
 
 }
