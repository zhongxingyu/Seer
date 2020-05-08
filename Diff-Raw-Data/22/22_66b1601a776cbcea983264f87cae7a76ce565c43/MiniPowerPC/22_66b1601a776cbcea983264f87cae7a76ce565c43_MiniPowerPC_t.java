 package ch.hszt.MiniPowerPC;
 
 import ch.hszt.MiniPowerPC.helper.Helper;
 
 
 public class MiniPowerPC {
 	public final static int START_OF_INSTR_REG = 100;
 	public final static int END_OF_INSTR_REG = 499;
 	
 	private short carryFlag = 0;
 	private MemoryEntry[] memory;
 	private MemoryEntry[] register = new MemoryEntry[4]; // register[0] = akku, 1-3 register
 	private int instructionCounter = START_OF_INSTR_REG;
 	private long stepCounter;
 	private Instruction instructionReg;
 
 	public MiniPowerPC(MemoryEntry[] memory) {
 		this.memory = memory;
 		
 		//init memory
 		for(int i = 0;i < register.length;i++){
 			clr((short)i);
 		}
 		
 		getNextInstruction();
 	}
 
 	public MiniPowerPC(MemoryEntry[] memory, int instructionCounter){
 		this(memory);
 		setInstructionCounter(instructionCounter);
 	}
 	
 	public void setCarryFlag(short carryFlag) {
 		this.carryFlag = carryFlag;
 	}
 
 	public void setAkku(int i) {
 		register[0] = MemoryEntry.parseMemoryEntry(i);
 	}
 
 	public Instruction getInstructionReg(){
 		return instructionReg;
 	}
 	
 	public MemoryEntry getAkku(){
 		return register[0];
 	}
 	
 	public MemoryEntry[] getRegister(){
 		return register;
 	}
 
 	public int getInstructionCounter(){
 		return instructionCounter;
 	}
 	
 	public short getCarryFlag() {
 		return carryFlag;
 	}
 
 	public long getStepCounter() {
 		return stepCounter;
 	}
 
 	// Geter and seter for memory
 	public MemoryEntry[] getMemory() {
 		return memory;
 	}
 
 	public void setMemory(MemoryEntry[] memory) {
 		this.memory = memory;
 	}
 
 	public void setInstructionCounter(int instructionCounter) {
 		this.instructionCounter = instructionCounter;
 		getNextInstruction();
 	}
 
 	/**
 	 * Run whole simulation
 	 */
 	public void run() {
 		while(instructionReg != null){
 			instructionReg.run(this);
 			instructionCounter++;
 			stepCounter++;
 			getNextInstruction();
 		}
 		
 		if(memory[504] != null && memory[506] != null){
 			String resultString = String.valueOf(memory[506].getBinaryString()) + (String.valueOf(memory[504].getBinaryString())).substring(1);
 			System.out.println("Result (memory[504-507]):");
 			System.out.println("Binary: " + resultString);
 			System.out.println("Decimal: " + Helper.binaryCharArrayToInt(resultString.toCharArray(), true));
 		}
 		
 		
 	}
 	
 	/**
 	 * Get the next instruction
 	 */
 	public void getNextInstruction(){
 		while (memory[instructionCounter] == null && instructionCounter < END_OF_INSTR_REG) {
 			instructionCounter++;
 		}
 		if(memory[instructionCounter] != null){
 			instructionReg = Instruction.parseInstruction(memory[instructionCounter]); 
 		}else{
 			//finished
 			instructionReg = null;
 		}
 	}
 	
 	/**
 	 * Run a single instruction
 	 */
 	public void nextStep() {
 		if(instructionReg != null){
 			instructionReg.run(this);
 			instructionCounter++;
 			stepCounter++;
 			getNextInstruction();
 		}
                else{
                    if(memory[504] != null && memory[506] != null){
			String resultString = String.valueOf(memory[506].getBinaryString()) + (String.valueOf(memory[504].getBinaryString())).substring(1);
			System.out.println("Result (memory[504-507]):");
			System.out.println("Binary: " + resultString);
			System.out.println("Decimal: " + Helper.binaryCharArrayToInt(resultString.toCharArray(), true));
		}
                }
 	}	
 	
 	public void reset() {
 		//Reset all values
 		instructionCounter = START_OF_INSTR_REG;
 		stepCounter = 0;
 		for(int i = 0;i < register.length;i++){
 			clr((short)i);
 		}
 		carryFlag = 0;
 		
 		//Get first instruction
 		getNextInstruction();
 	}
 	
 	
 	/**
 	 * Register rnr := 0
 	 * 
 	 * @param rnr
 	 *            Registernummer (0 - 3)
 	 */
 	public void clr(short rnr) {
 		register[rnr] = MemoryEntry.parseMemoryEntry(0);
 	}
 
 	/**
 	 * Binary additon of 2 Memory Entries
 	 * @param m1 Memory Entry 1
 	 * @param m2 Memory Entry 2
 	 * @return resulting Memory Entry
 	 */
 	public MemoryEntry binaryAddition(MemoryEntry m1, MemoryEntry m2){
 		MemoryEntry result = MemoryEntry.parseMemoryEntry(0);
 		boolean c = false;
 		
 		for(int i = m1.getBinaryString().length-1;i > 0;i--){
 			if((m1.getBinaryString()[i] == m2.getBinaryString()[i]) &&  (m2.getBinaryString()[i] == '1')){
 				if(c){
 					result.getBinaryString()[i] = '1';
 				}else{
 					result.getBinaryString()[i] = '0';
 				}
 				c = true;
 			}else if((m1.getBinaryString()[i] == m2.getBinaryString()[i]) &&  (m2.getBinaryString()[i] == '0')){
 				if(c){
 					result.getBinaryString()[i] = '1';
 				}else{
 					result.getBinaryString()[i] = '0';
 				}
 				c = false;
 			}else if(m1.getBinaryString()[i] != m2.getBinaryString()[i]){
 				if(c){
 					result.getBinaryString()[i] = '0';
 					c = true;
 				}else{
 					result.getBinaryString()[i] = '1';
 					c = false;
 				}
 			}
 		}
 		
 		if(c){
 			carryFlag = 1;
 		}else{
 			carryFlag = 0;
 		}
 		
 		if(m1.getNumericValue() + m2.getNumericValue() >= 0){
 			result.getBinaryString()[0] = '0';
 		}else{
 			result.getBinaryString()[0] = '1';
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Akku := Akku + Register rnr
 	 * 
 	 * @param rnr
 	 *            Registernummer 0-2
 	 */
 	public void add(short rnr) {
 		register[0] = binaryAddition(register[0].clone(), register[rnr].clone());
 	}
 
 	/**
 	 * Akku := Akku + zahl
 	 * 
 	 * @param zahl
 	 *            Zahl die hinzugefügt wird
 	 */
 	public void addd(int no) {
 		register[0] = binaryAddition(register[0].clone(), MemoryEntry.parseMemoryEntry(no));
 	}
 
 	/**
 	 * Akku++;
 	 */
 	public void inc() {
 		register[0] = binaryAddition(register[0].clone(), MemoryEntry.parseMemoryEntry(1));
 	}
 
 	/**
 	 * Akku--;
 	 */
 	public void dec() {
 		register[0] = binaryAddition(register[0].clone(), MemoryEntry.parseMemoryEntry(-1));
 	}
 
 	/**
 	 * Register rnr := Speicher(adr)
 	 * 
 	 * @param rnr
 	 *            register nummer
 	 * @param adr
 	 *            memory address
 	 */
 	public void lwdd(short rnr, short adr) {
 		register[rnr] = memory[adr].clone();
 	}
 
 	/**
 	 * Speicher(adr) := Register rnr
 	 * 
 	 * @param rnr
 	 *            register nummer
 	 * @param adr
 	 *            speicheradresse
 	 */
 	public void swdd(short rnr, short adr) {
 		memory[adr] = register[rnr].clone();
 	}
 
 	/**
 	 * Arithmetical shift right
 	 */
 	public void sra() {
 		MemoryEntry m = register[0].clone();
 		carryFlag = Short.parseShort(String.valueOf(m.getBinaryString()[m
 				.getBinaryString().length - 1]));
 
 		char[] ca = m.getBinaryString();
 
 		// shift left (dont touch first 2 bits)
 		for (int i = ca.length - 1; i > 1; i--) {
 			ca[i] = ca[i - 1];
 		}
 
 		register[0] = new MemoryEntry(ca);
 	}
 
 	/**
 	 * Arithmetical shift left
 	 */
 	public void sla() {
 		MemoryEntry m = register[0].clone();
 		carryFlag = Short.parseShort(String.valueOf(m.getBinaryString()[1]));
 
 		char[] co = m.getBinaryString();
 		char[] cn = new char[16];
 		// shift left
 		for (int i = 1; i < co.length - 1; i++) {
 			cn[i] = co[i + 1];
 		}
 		// Keep first bit
 		cn[0] = co[0];
 		// add a 0 to fill the gap
 		cn[15] = '0';
 
 		register[0] = new MemoryEntry(cn);
 	}
 
 	/**
 	 * Logical shift right
 	 */
 	public void srl() {
 		MemoryEntry m = register[0].clone();
 		carryFlag = Short.parseShort(String.valueOf(m.getBinaryString()[m
 				.getBinaryString().length - 1]));
 
 		char[] ca = m.getBinaryString();
 
 		// shift right
 		for (int i = ca.length - 1; i > 0; i--) {
 			ca[i] = ca[i - 1];
 		}
 		// add a 0 to fill the gap
 		ca[0] = '0';
 		register[0] = new MemoryEntry(ca);
 	}
 
 	/**
 	 * Logical shift left
 	 */
 	public void sll() {
 		MemoryEntry m = register[0].clone();
 		carryFlag = Short.parseShort(String.valueOf(m.getBinaryString()[0]));
 
 		char[] ca = m.getBinaryString();
 		// shift left
 		for (int i = 0; i < ca.length - 1; i++) {
 			ca[i] = ca[i + 1];
 		}
 		// add a 0 to fill the gap
 		ca[ca.length - 1] = '0';
 		register[0] = new MemoryEntry(ca);
 	}
 
 	/**
 	 * Binary AND
 	 * 
 	 * @param rnr
 	 */
 	public void and(short rnr) {
 		MemoryEntry ma = register[0].clone();
 		MemoryEntry mr = register[rnr].clone();
 
 		char[] caa = ma.getBinaryString();
 		char[] car = mr.getBinaryString();
 
 		for (int i = 0; i < caa.length; i++) {
 			if (caa[i] == '1' && car[i] == '1') {
 				caa[i] = '1';
 			} else {
 				caa[i] = '0';
 			}
 		}
 		register[0] = new MemoryEntry(caa);
 	}
 
 	/**
 	 * Binary OR
 	 * 
 	 * @param rnr
 	 */
 	public void or(short rnr) {
 		MemoryEntry ma = register[0].clone();
 		MemoryEntry mr = register[rnr].clone();
 
 		char[] caa = ma.getBinaryString();
 		char[] car = mr.getBinaryString();
 
 		for (int i = 0; i < caa.length; i++) {
 			if (caa[i] == '1' || car[i] == '1') {
 				caa[i] = '1';
 			} else {
 				caa[i] = '0';
 			}
 		}
 		register[0] = new MemoryEntry(caa);
 	}
 
 	/**
 	 * Binary NOT
 	 */
 	public void not() {
 		MemoryEntry m = register[0].clone();
 		char[] ca = m.getBinaryString();
 		for (int i = 0; i < ca.length; i++) {
 			ca[i] = (ca[i] == '1' ? '0' : '1');
 		}
 		register[0] = new MemoryEntry(ca);
 	}
 
 	/**
 	 * Conditional Jump. If Akku == 0 jump to the address in register rnr
 	 * 
 	 * @param rnr
 	 */
 	public void bz(short rnr) {
 		if (register[0].getNumericValue() == 0) {
 			b(rnr);
 		}
 	}
 
 	/**
 	 * Conditional Jump. If Akku != 0 jump to the address in register rnr
 	 * 
 	 * @param rnr
 	 */
 	public void bnz(short rnr) {
 		if (register[0].getNumericValue() != 0) {
 			b(rnr);
 		}
 	}
 
 	/**
 	 * Conditional Jump. If carryFlag is set, jump to the address in register
 	 * rnr
 	 * 
 	 * @param rnr
 	 */
 	public void bc(short rnr) {
 		if (carryFlag == 1) {
 			b(rnr);
 		}
 	}
 
 	/**
 	 * Unconditional Jump. Jump to the address in register rnr
 	 * 
 	 * @param rnr
 	 */
 	public void b(short rnr) {
 		instructionCounter = (register[rnr].getNumericValue() - 1); // -1 because it will be
 													// incremented immediately
 	}
 
 	/**
 	 * Conditional Jump. If Akku == 0 jump to the given address
 	 * 
 	 * @param adr
 	 */
 	public void bzd(short adr) {
 		if (register[0].getNumericValue() == 0) {
 			bd(adr);
 		}
 	}
 
 	/**
 	 * Conditional Jump. If Akku != 0 jump to the given address
 	 * 
 	 * @param rnr
 	 */
 	public void bnzd(short adr) {
 		if (register[0].getNumericValue() != 0) {
 			bd(adr);
 		}
 	}
 
 	/**
 	 * Conditional Jump. If carryFlag is set, jump to the given address
 	 * 
 	 * @param rnr
 	 */
 	public void bcd(short adr) {
 		if (carryFlag == 1) {
 			bd(adr);
 		}
 	}
 
 	/**
 	 * Unconditional Jump. Jump to the given address
 	 * 
 	 * @param adr
 	 */
 	public void bd(short adr) {
 		instructionCounter = (adr - 1); // -1 because it will be incremented
 										// immediately
 	}
 
 	/**
 	 * Unconditional Jump to the end of our instruction memory
 	 *	
 	 */
 	public void end() {
 		instructionCounter = (END_OF_INSTR_REG-1);
 	}
 
 
 	
 }
