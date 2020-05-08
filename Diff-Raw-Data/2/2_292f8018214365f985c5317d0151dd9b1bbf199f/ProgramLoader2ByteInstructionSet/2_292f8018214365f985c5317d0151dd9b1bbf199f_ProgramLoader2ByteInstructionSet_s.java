 /*
  * File: 		ProgramLoader.java
  * Date: 		Oct 29, 2013
  *
  * Copyright 2013 Constantin Lazari. All rights reserved.
  *
  * Unless required by applicable law or agreed to in writing, this software
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.
  */
 package ch.zhaw.lazari.cpu.impl.utils;
 
 import static ch.zhaw.lazari.cpu.impl.utils.BooleanArrayUtils.fromInt;
 import static ch.zhaw.lazari.cpu.impl.utils.BooleanArrayUtils.toBinaryString;
 import static ch.zhaw.lazari.cpu.impl.utils.BooleanArrayUtils.toInt;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.zhaw.lazari.cpu.api.Memory;
 import ch.zhaw.lazari.cpu.api.ProgramLoader;
 import ch.zhaw.lazari.cpu.impl.InstructionSet2ByteWord;
 import ch.zhaw.lazari.cpu.impl.UnknownCommandException;
 
 /**
  * Loads a program into memory
  */
 public class ProgramLoader2ByteInstructionSet implements ProgramLoader {
 	
 	private static final Logger LOG = LoggerFactory.getLogger(ProgramLoader2ByteInstructionSet.class);
 	
 	@Override
 	public void load(final List<String> lines, final Memory memory) {
 		for(final String line : lines) {
 			if(line.length() > 0) {
 				InstructionSet2ByteWord word = InstructionSet2ByteWord.createFromMnemonic(line);
 				final String bits = getBits(word, line);
 				final int address = word.getLineNumber(line);
 				LOG.trace(String.format("Line %d: '%s' ...", address, word));
 				store(memory, address, bits);
 			}
 		}
 	}
 
 	private void store(final Memory memory, final int address, final String bits) {
 		LOG.trace(String.format("%d %s", address, bits));
 		final String left = bits.substring(0, Byte.SIZE);
 		final String right = bits.substring(Byte.SIZE);
 		memory.store(address, fromInt(toInt(left), Byte.SIZE));
 		memory.store(address + 1, fromInt(toInt(right), Byte.SIZE));
 	}
 	
 	private String getBits(final InstructionSet2ByteWord word, final String line) {
 		switch(word.getGroup()) {
 		case ACCU:
 			return createAccuBits(word, line);
 		case ARITHMETIC:
 			return createArithmeticBits(word, line);
 		case CPU:
 			return createCpuBits(word, line);
 		case LOGIC:
 			return createLogicBits(word, line);
 		case MEMORY:
 			return createMemoryBits(word, line);
 		case PROGRAM_COUNTER:
 			return createProgramCounterBits(word, line);
 		case REGISTER:
 			return createRegisterBits(word, line);
 		default: 
 			throw new UnknownCommandException(getMessage(word, line));
 		}		
 	}
 	
 	protected String createAccuBits(final InstructionSet2ByteWord word, final String line) {
 		switch (word) {
 		case ADD:
 			final int registerId = word.getMnemonicFirst(line);
 			return String.format(word.getFormat(), toRegisterBinary(registerId));
 		case ADDD:
 			final int value = word.getMnemonicFirst(line);
 			return String.format(word.getFormat(), toBinaryString(fromInt(value, 15)));
 		case INC:
 		case DEC:
 			return word.getFormat();
 		default:
 			throw new UnknownCommandException(getMessage(word, line));
 		}
 	}
 
 	protected String createArithmeticBits(final InstructionSet2ByteWord word, final String line) {
 		return word.getFormat();
 	}
 
 	protected String createCpuBits(final InstructionSet2ByteWord word, final String line) {
 		return word.getFormat();	
 	}
 
 	protected String createLogicBits(final InstructionSet2ByteWord word, final String line) {
 		switch(word) {
 		case AND:
 		case OR:
 			final int registerId = word.getMnemonicFirst(line);
 			return String.format(word.getFormat(), toRegisterBinary(registerId));
 		case NOT:
 			return word.getFormat();
 		default:
 			throw new UnknownCommandException(getMessage(word, line));
 		}
 	}
 
 	protected String createMemoryBits(final InstructionSet2ByteWord word, final String line) {
 		final int registerId = word.getMnemonicFirst(line);
 		final int address = word.getMnemonicSecond(line);
 		final String binaryRegister = toRegisterBinary(registerId);
 		final String binaryAddress = toBinaryString(fromInt(address, 10));
 		return String.format(word.getFormat(), binaryRegister, binaryAddress);
 	}
 
 	private String createProgramCounterBits(final InstructionSet2ByteWord word, final String line) {
 		switch(word) {
 		case BZ:
 		case BNZ:
 		case BC:
 		case B: 
 			final int registerId = word.getMnemonicFirst(line);
 			return String.format(word.getFormat(), toRegisterBinary(registerId));
 		case BZD:
 		case BNZD:
 		case BCD:
 		case BD:
 			final int address = word.getMnemonicFirst(line);
 			return String.format(word.getFormat(), toBinaryString(fromInt(address, 10)));			
 		default:
 			throw new UnknownCommandException(getMessage(word, line));
 		}
 	}
 
 	private String createRegisterBits(final InstructionSet2ByteWord word, final String line) {
 		final int registerId = word.getMnemonicFirst(line);		
 		return String.format(word.getFormat(), toRegisterBinary(registerId));
 	}
 
 	private String getMessage(InstructionSet2ByteWord word, String line) {
		return String.format("Command %s from '%s' is unknown.", word);
 	}
 	
 	private String toRegisterBinary(final int value) {
 		return toBinaryString(fromInt(value, 2));
 	}
 }
