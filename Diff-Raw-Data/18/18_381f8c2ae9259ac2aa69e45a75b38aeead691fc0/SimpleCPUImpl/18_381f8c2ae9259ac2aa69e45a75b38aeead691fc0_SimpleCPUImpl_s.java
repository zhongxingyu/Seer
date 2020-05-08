 /*
  * File: 		SimpleCPUImpl.java
  * Date: 		Oct 15, 2013
  *
  * Copyright 2013 Constantin Lazari. All rights reserved.
  *
  * Unless required by applicable law or agreed to in writing, this software
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.
  */
 package ch.zhaw.lazari.cpu.impl;
 
 import static ch.zhaw.lazari.cpu.impl.utils.BooleanArrayUtils.fromInt;
 import static ch.zhaw.lazari.cpu.impl.utils.BooleanArrayUtils.toBinaryString;
 import static ch.zhaw.lazari.cpu.impl.utils.BooleanArrayUtils.toInt;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.zhaw.lazari.cpu.api.*;
 import ch.zhaw.lazari.cpu.impl.commands.*;
 import ch.zhaw.lazari.cpu.impl.program_counter.SimpleProgramCounterImpl;
 import ch.zhaw.lazari.cpu.impl.register.ArithmeticLogicalAccumulatorImpl;
 import ch.zhaw.lazari.cpu.impl.register.SimpleRegisterImpl;
 
 /**
  * Simple Implementation of a central processing unit
  */
 public class SimpleCPUImpl implements CPU {
 
 	private static final Logger LOG = LoggerFactory.getLogger(CPU.class);
 
 	private static final String LOG_UNKNOWN_INSTRUCTION_FORMAT = "\t\tUnknown instruction '%s'";
 	
 	private static final int DEFAULT_WORD_LENGTH = 16;
 	
 	private static final int REGISTERS = 3;
 	
 	private final ArithmeticLogicalAccumulator accu = new ArithmeticLogicalAccumulatorImpl(DEFAULT_WORD_LENGTH);
 	
 	private final Register[] registers = new Register[REGISTERS + 1];
 	
 	private final ProgramCounter programCounter = new SimpleProgramCounterImpl(100, 2);
 	
 	private final Memory memory;
 	
 	private int counter = 1;
 	
 	private boolean isFinished = true;
 	
 	/**
 	 * Creates a new CPU using the passed components
 	 * @param memory The memory where program code and data is stored
 	 */
 	public SimpleCPUImpl(
 			final Memory memory) {
 		this.memory = memory;
 		this.registers[0] = accu; 
 		for(int index = 1; index < registers.length; ++index) {
 			registers[index] = new SimpleRegisterImpl(DEFAULT_WORD_LENGTH);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#start()
 	 */
 	@Override
 	public void start() {
 		isFinished = false;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#stop()
 	 */
 	@Override
 	public void stop() {
 		isFinished = true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#tick()
 	 */
 	@Override
 	public void tick() {
 		LOG.trace("Recieved tick executing instructions:");
 		final Command command = getCommand();
 		LOG.trace(String.format("\t%d. Incrementing program counter.", counter++));
 		programCounter.next();
		LOG.trace(String.format("\t%d. Executing command:", counter));
 		command.execute();
 		counter = 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#isFinished()
 	 */
 	@Override
 	public boolean isFinished() {
 		return isFinished;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#getRegister(int)
 	 */
 	@Override
 	public Register getRegister(final int index) {
 		return registers[index];
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#getMemory()
 	 */
 	@Override
 	public Memory getMemory() {
 		return memory;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#getProgramCounter()
 	 */
 	@Override
 	public ProgramCounter getProgramCounter() {
 		return programCounter;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.lazari.cpu.api.CPU#getAccumulator()
 	 */
 	@Override
 	public LogicalAccumulator getAccumulator() {
 		return accu;
 	}
 	
 	private Command getCommand() {
 		LOG.trace(String.format("\t%d. Fetching command from memory:", counter++));
 		final String word = getCommandWord();
 		LOG.trace(String.format("\t%d. Interpreting command word '%s':", counter++, word));
 		return interpret(word);
 	}
 
 	private String getCommandWord() {
 		final boolean[] word = new boolean[DEFAULT_WORD_LENGTH];
 		int address = programCounter.get();
 		int read = 0;
 		while(read < word.length) {
 			final boolean[] stored = memory.load(address++);
 			for(final boolean bit : stored) {
 				word[read++] = bit;
 			}
 		}
 		return toBinaryString(word);
 	}
 
 	private Command interpret(final String word) {
 		// FIXME Reduce complexity
 		final InstructionSet2ByteWord instruction = InstructionSet2ByteWord.createFromBits(word);
 		LOG.trace(String.format("\t\tas %s command of type %s", instruction.getGroup(), instruction));
 		switch (instruction.getGroup()) {
 		case REGISTER:
 			return createRegisterCommand(instruction, word);
 		case ACCU:
 			return createAccumulatorCommand(instruction, word);
 		case MEMORY:
 			return createMemoryCommand(instruction, word);
 		case ARITHMETIC:
 			return createArithmeticCommand(instruction, word);
 		case LOGIC:
 			return createLogicCommand(instruction, word);
 		case PROGRAM_COUNTER:
 			return createProgramCounterCommand(instruction, word);
 		case CPU:
 			return createCPUCommand();
 		default:
 			logUnknown(instruction);
 			throw new UnknownCommandException(word);				
 		}
 	}
 
 	private Command createRegisterCommand(final InstructionSet2ByteWord instruction, final String word) {
 		return new CLR(registers[instruction.getRegisterId(word)]);
 	}
 
 	private Command createAccumulatorCommand(final InstructionSet2ByteWord instruction, final String word) {
 		switch(instruction) {
 		case ADD:
 			return new ADD(accu, registers[instruction.getRegisterId(word)]);
 		case ADDD:
 			final int value = toInt(instruction.getSecondWord(word));
 			return new ADDD(accu, fromInt(value, DEFAULT_WORD_LENGTH));
 		case INC:
 			return new INC(accu);
 		case DEC:
 			return new DEC(accu);
 		default:
 			logUnknown(instruction);
 			throw new UnknownCommandException(word);				
 		}
 	}
 
 	private Command createMemoryCommand(final InstructionSet2ByteWord instruction, final String word) {
 		final int register = instruction.getRegisterId(word);
		final int address = toInt(instruction.getSecondWord(word));
		LOG.trace(String.format("Creating command '%s' for register %d and address %d", instruction, register, address));
 		switch(instruction) {
 		case LWDD:
 			return new LWDD(memory, registers[register], address);
 		case SWDD:
 			return new SWDD(memory, registers[register], address);			
 		default:
 			logUnknown(instruction);
 			throw new UnknownCommandException(word);				
 		}
 	}
 
 	private Command createArithmeticCommand(final InstructionSet2ByteWord instruction, final String word) {
 		switch(instruction) {
 		case SRA:
 			return new SRA(accu);
 		case SLA:
 			return new SLA(accu);
 		case SRL:
 			return new SRL(accu);
 		case SLL:
 			return new SLL(accu);
 		default:
 			logUnknown(instruction);
 			throw new UnknownCommandException(word);				
 		}
 	}
 	
 	private Command createLogicCommand(final InstructionSet2ByteWord instruction, final String word) {
 		switch(instruction) {
 		case AND:
 			return new AND(accu, registers[instruction.getRegisterId(word)]);
 		case OR:
 			return new OR(accu, registers[instruction.getRegisterId(word)]);
 		case NOT:
 			return new NOT(accu);
 		default:
 			logUnknown(instruction);
 			throw new UnknownCommandException(word);				
 		}
 	}
 
 	private Command createProgramCounterCommand(final InstructionSet2ByteWord instruction, final String word) {
 		switch(instruction) {
 		case BZ:
 			return new BZ(programCounter, accu, registers[instruction.getRegisterId(word)]);
 		case BNZ:
 			return new BNZ(programCounter, accu, registers[instruction.getRegisterId(word)]);
 		case BC:
 			return new BZ(programCounter, accu, registers[instruction.getRegisterId(word)]);
 		case B:
 			return new B(programCounter, registers[instruction.getRegisterId(word)]);
 		case BZD:
			return new BZD(programCounter, accu, instruction.getAddress(word));
 		case BNZD:
			return new BNZD(programCounter, accu, instruction.getAddress(word));
 		case BCD:
			return new BCD(programCounter, accu, instruction.getAddress(word));
 		case BD:
			return new BD(programCounter, instruction.getAddress(word));
 		default:
 			logUnknown(instruction);
 			throw new UnknownCommandException(word);				
 		}
 	}
 
 	private Command createCPUCommand() {
 		return new END(this);
 	}
 	
 	private void logUnknown(final InstructionSet2ByteWord instruction) {
 		LOG.error(LOG_UNKNOWN_INSTRUCTION_FORMAT, instruction);
 	}
 }
