 package cpu;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import util.Entry;
 import cpu.instpar.InstructionParameter;
 import cpu.instpar.InvalidParameterException;
 import cpu.instruction.Add;
 import cpu.instruction.And;
 import cpu.instruction.Call;
 import cpu.instruction.Cmp;
 import cpu.instruction.Dec;
import cpu.instruction.Div;
 import cpu.instruction.In;
 import cpu.instruction.Inc;
 import cpu.instruction.Instruction;
 import cpu.instruction.InvalidInstructionException;
 import cpu.instruction.Je;
 import cpu.instruction.Jg;
 import cpu.instruction.Jge;
 import cpu.instruction.Jl;
 import cpu.instruction.Jle;
 import cpu.instruction.Jmp;
 import cpu.instruction.Jne;
 import cpu.instruction.Jno;
 import cpu.instruction.Jo;
 import cpu.instruction.Mov;
 import cpu.instruction.Mul;
 import cpu.instruction.Not;
 import cpu.instruction.Or;
 import cpu.instruction.Out;
 import cpu.instruction.Pop;
 import cpu.instruction.Push;
 import cpu.instruction.Ret;
 import cpu.instruction.Sub;
 import cpu.instruction.Test;
 import cpu.instruction.Xor;
 import device.AddressingException;
 import device.Bus;
 import device.DeviceObserver;
 
 public class VmCpu implements Cpu, InstructionRunner {
 	private Bus bus = null;
 	private ProgramCounter pc = new ProgramCounter();
 	private InstructionIdentifier id = new InstructionIdentifier();
 	private Map<REGISTER_NAME, Integer> registers;
 	private Map<FLAG_NAME, Boolean> flags;
 	private VmStack stack;
 	private Set<DeviceObserver> registerObservers = new HashSet<DeviceObserver>();
 	
 	/**
 	 * Initializes the CPU
 	 * 
 	 * Loads name-instruction mapping into id,
 	 * inits registers to 0, inits flags to false.
 	 */
 	public VmCpu() {
 		// load available instructions
 		id.addInstruction("ADD", new Add());
 		id.addInstruction("AND", new And());
 		id.addInstruction("CALL", new Call());
 		id.addInstruction("CMP", new Cmp());
 		id.addInstruction("DEC", new Dec());
		id.addInstruction("DIV", new Div());
 		id.addInstruction("IN", new In());
 		id.addInstruction("INC", new Inc());
 		id.addInstruction("JE", new Je());
 		id.addInstruction("JG", new Jg());
 		id.addInstruction("JGE", new Jge());
 		id.addInstruction("JL", new Jl());
 		id.addInstruction("JLE", new Jle());
 		id.addInstruction("JMP", new Jmp());
 		id.addInstruction("JNE", new Jne());
 		id.addInstruction("JNO", new Jno());
 		id.addInstruction("JO", new Jo());
 		id.addInstruction("MOV", new Mov());
 		id.addInstruction("MUL", new Mul());
 		id.addInstruction("NOT", new Not());
 		id.addInstruction("OR", new Or());
 		id.addInstruction("OUT", new Out());
 		id.addInstruction("POP", new Pop());
 		id.addInstruction("PUSH", new Push());
 		id.addInstruction("RET", new Ret());
 		id.addInstruction("SUB", new Sub());
 		id.addInstruction("TEST", new Test());
 		id.addInstruction("XOR", new Xor());
 		
 		reset();
 	}
 	
 	@Override
 	public void setBus(Bus bus) {
 		this.bus = bus;
 	}
 	
 	public void reset() {
 		pc.reset();
 		stack = new VmStack();
 		
 		// reset registers
 		registers = new HashMap<InstructionRunner.REGISTER_NAME, Integer>();
 		for (REGISTER_NAME register : REGISTER_NAME.values()) {
 			registers.put(register, 0);
 			notifyRegisterObservers(register);
 		}
 		
 		// reset flags
 		flags = new HashMap<InstructionRunner.FLAG_NAME, Boolean>();
 		for (FLAG_NAME flag : FLAG_NAME.values()) {
 			flags.put(flag, false);
 		}
 	}
 	
 	public void addPcObserver(DeviceObserver observer) {
 		pc.addObserver(observer);
 	}
 	
 	public void addRegisterObserver(DeviceObserver observer) {
 		registerObservers.add(observer);
 	}
 	
 	private void notifyRegisterObservers(REGISTER_NAME targetRegisterName) {
 		int targetRegisterIndex = 0;
 		
 		int registerIndex = 0;
 		for (REGISTER_NAME registerName : REGISTER_NAME.values()) {
 			if (targetRegisterName == registerName) {
 				targetRegisterIndex = registerIndex;
 				break;
 			}
 			
 			registerIndex++;
 		}
 		
 		for (DeviceObserver observer : registerObservers) {
 			observer.fireDataChange(targetRegisterIndex);
 		}
 	}
 	
 	private void executeNextInstruction() {
 		// store pc current state
 		int currentPcState = pc.getState();
 		
 		// fetch instruction from memory pointed by pc
 		// using bus
 		String word = fetchWord(currentPcState);
 		
 		// id instruction
 		Entry<Instruction, InstructionParameter[]> instructionWithParameters = null;
 		try {
 			instructionWithParameters = id.identify(word);
 		} catch (UnidentifiableInstructionException e) {
 			// TODO call sw exception routine
 			e.printStackTrace();
 		}
 		
 		Instruction instruction = instructionWithParameters.getKey();
 		InstructionParameter[] parameters = instructionWithParameters.getValue();
 		
 		// execute instruction
 		try {
 			instruction.execute(this, parameters);
 		} catch (InvalidInstructionException e) {
 			// TODO call sw exception routine
 			e.printStackTrace();
 		} catch (InvalidParameterException e) {
 			// TODO call sw exception routine
 			e.printStackTrace();
 		}
 		
 		// increment pc if not jump
 		if (currentPcState == pc.getState()) {
 			pc.increment();
 		}		
 	}
 	
 	@Override
 	public void step() throws NoBusSetException {
 		if (bus == null) {
 			throw new NoBusSetException();
 		}
 		
 		if (stack.getCurrentFrameIndex() >= 0) {
 			executeNextInstruction();
 		}
 	}
 
 	@Override
 	public void run() throws NoBusSetException {
 		if (bus == null) {
 			throw new NoBusSetException();
 		}
 		
 		// while the are stack frames
 		while (stack.getCurrentFrameIndex() >= 0) {
 			executeNextInstruction();
 		}
 	}
 	
 	public String fetchWord(int address) {
 		String word = null;
 		try {
 			word = bus.getWord(address);
 		} catch (AddressingException e) {
 			// TODO call sw exception routine
 			e.printStackTrace();
 		}
 		
 		return word;
 	}
 	
 	public void putWord(int address, String word) {
 		try {
 			bus.putWord(address, word);
 		} catch (AddressingException e) {
 			// TODO call sw exception routine
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public int getRegisterContent(REGISTER_NAME registerName) throws InvalidRegisterException {
 		if (registers.containsKey(registerName)) {
 			return registers.get(registerName);
 		} else {
 			throw new InvalidRegisterException();
 		}
 	}
 
 	@Override
 	public void setRegisterContent(REGISTER_NAME registerName, int content) throws InvalidRegisterException {
 		if (registers.containsKey(registerName)) {
 			registers.put(registerName, content);
 			
 			notifyRegisterObservers(registerName);
 		} else {
 			throw new InvalidRegisterException();
 		}
 	}
 	
 	@Override
 	public InstructionParameter identifyInstructionParameter(String parameter) {
 		return id.identifyParameter(parameter);
 	}
 
 	@Override
 	public void increaseStackframe() {
 		stack.increaseStackFrame();
 	}
 
 	@Override
 	public void decreaseStackframe() {
 		stack.decreaseStackFrame();
 	}
 	
 	@Override
 	public int getStackframeIndex() {
 		return stack.getCurrentFrameIndex();
 	}
 
 	@Override
 	public void pushStack(int value) {
 		stack.push(value);
 	}
 
 	@Override
 	public int popStack() {
 		return stack.pop();
 	}
 
 	@Override
 	public void setFlag(FLAG_NAME flag, boolean value) throws InvalidFlagException {
 		if (flags.containsKey(flag)) {
 			flags.put(flag, value);
 		} else {
 			throw new InvalidFlagException();
 		}
 	}
 
 	@Override
 	public boolean getFlag(FLAG_NAME flag) throws InvalidFlagException {
 		if (flags.containsKey(flag)) {
 			return flags.get(flag);
 		} else {
 			throw new InvalidFlagException();
 		}
 	}
 
 	@Override
 	public void jump(int address) {
 		pc.jump(address);
 	}
 
 	@Override
 	public int getPcState() {
 		return pc.getState();
 	}
 }
