 /*
  * Copyright (C) 2011-2012  Christian Roesch
  * 
  * This file is part of micro-debug.
  * 
  * micro-debug is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * micro-debug is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with micro-debug.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.croesch.micro_debug.console;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.github.croesch.micro_debug.annotation.NotNull;
 import com.github.croesch.micro_debug.annotation.Nullable;
 import com.github.croesch.micro_debug.commons.AbstractCodeContainer;
 import com.github.croesch.micro_debug.commons.Printer;
 import com.github.croesch.micro_debug.commons.Utils;
 import com.github.croesch.micro_debug.i18n.Text;
 import com.github.croesch.micro_debug.mic1.mem.IJVMCommand;
 import com.github.croesch.micro_debug.mic1.mem.IJVMCommandArgument;
 import com.github.croesch.micro_debug.mic1.mem.IJVMConfigReader;
 import com.github.croesch.micro_debug.mic1.mem.Memory;
 import com.github.croesch.micro_debug.mic1.register.Register;
 import com.github.croesch.micro_debug.settings.Settings;
 
 /**
  * Interpreter of a memory, can print code and stack read from the memory.
  * 
  * @author croesch
  * @since Date: Feb 13, 2012
  */
 public final class MemoryInterpreter extends AbstractCodeContainer {
 
   /** the map that contains the configuration with addresses in micro code and the belonging command */
   @Nullable
   private Map<Integer, IJVMCommand> commands = null;
 
   /** the memory to interprete */
   @NotNull
   private final Memory memory;
 
   /**
    * Constructs an interpreter of a memory, can print code and stack read from the memory.
    * 
    * @since Date: Feb 13, 2012
    * @param mem the memory to interprete
    */
   public MemoryInterpreter(final Memory mem) {
     if (mem == null) {
       throw new IllegalArgumentException();
     }
 
     this.memory = mem;
   }
 
   @Override
   protected int printCodeLine(final int addr) {
     final StringBuilder sb = new StringBuilder();
     final int bytesRead = getLineString(addr, sb);
     Printer.println(sb.toString());
 
     return bytesRead;
   }
 
   /**
    * Prints the content of the memory between the given addresses.
    * 
    * @since Date: Jan 29, 2012
    * @param pos1 the address to start (inclusive)
    * @param pos2 the address to end (inclusive)
    */
   public void printContent(final int pos1, final int pos2) {
     // correct arguments
     final int start = Math.max(0, Math.min(pos1, pos2));
     final int end = Math.min(this.memory.getSize() - 1, Math.max(pos1, pos2));
 
     for (int i = start; i <= end; ++i) {
       Printer.println(Text.MEMORY_CONTENT.text(formatIntToHex(i, Settings.MIC1_MEM_MACRO_ADDR_WIDTH.getValue()),
                                                Utils.toHexString(this.memory.getWord(i))));
     }
   }
 
   /**
    * Prints the content of the stack. Technical speaking it prints the content of the memory between the initial stack
    * pointer value and the current value of the stack (inclusive edges).
    * 
    * @since Date: Feb 5, 2012
    * @param elementsToHide the number of elements to hide. The first possible element is the one the initial stack
    *        pointer points to.
    */
   public void printStack(final int elementsToHide) {
     // fetch initial and current stack pointer values
     final int initialStackPointer = Settings.MIC1_REGISTER_SP_DEFVAL.getValue();
     final int currentStackPointer = Register.SP.getValue();
 
     int stackElement = elementsToHide;
 
     // iterate over the memory and print the content of the stack
     for (int addr = initialStackPointer + stackElement; addr <= currentStackPointer; ++addr, ++stackElement) {
       final String formattedAddress = formatIntToHex(addr, Settings.MIC1_MEM_MACRO_ADDR_WIDTH.getValue());
       final String formattedValue = Utils.toHexString(this.memory.getWord(addr));
       Printer.println(Text.STACK_CONTENT.text(stackElement, formattedAddress, formattedValue));
     }
 
     // instead of nothing display a text, if stack is empty
     if (stackElement == elementsToHide) {
       Printer.println(Text.STACK_EMPTY);
     }
   }
 
   /**
    * Returns the formatted line.
    * 
    * @since Date: Feb 3, 2012
    * @param line the number of line to fetch.
    * @return the {@link String} representing the given line number
    */
   @Nullable
   public String getFormattedLine(final int line) {
     if (line < 0) {
       return null;
     }
 
     final StringBuilder sb = new StringBuilder();
     getLineString(line, sb);
     return sb.toString();
   }
 
   /**
    * Returns the number of bytes read as arguments additional to the command byte.
    * 
    * @since Date: Feb 3, 2012
    * @param addr the absolute address of the code instruction to fetch
    * @param sb {@link StringBuilder} to append the formatted line to
    * @return the number of bytes read as arguments to the command byte
    */
   private int getLineString(final int addr, final StringBuilder sb) {
     final StringBuilder formattedArgs = new StringBuilder();
 
     final int cmdCode = this.memory.getByte(addr);
     final IJVMCommand cmd = lookupCommand(cmdCode);
 
     final String name = buildNameForCommand(cmd);
     final int bytesRead = readArgumentsIfAny(addr, cmd, formattedArgs);
 
     final String formattedAddr = formatIntToHex(addr, Settings.MIC1_MEM_MACRO_ADDR_WIDTH.getValue());
     final String formattedCmdCode = formatIntToHex(cmdCode, Settings.MIC1_MEM_MICRO_ADDR_WIDTH.getValue());
 
     sb.append(Text.MACRO_CODE_LINE.text(formattedAddr, formattedCmdCode, name, formattedArgs.toString()));
     return bytesRead;
   }
 
   /**
    * Returns the {@link IJVMCommand} that belongs to the given address.
    * 
    * @since Date: Jan 22, 2012
    * @param addr the address where the command is placed in the micro code
    * @return the {@link IJVMCommand} describing the command at the given address<br>
    *         or <code>null</code> if no command is configured for this address
    */
   @Nullable
   private IJVMCommand lookupCommand(final int addr) {
     if (this.commands == null) {
       // read configuration file the first time
       final InputStream in = getClass().getClassLoader().getResourceAsStream("ijvm.conf");
       this.commands = new IJVMConfigReader().readConfig(in);
     }
     return this.commands.get(Integer.valueOf(addr));
   }
 
   /**
    * Returns the name for the given command to display.
    * 
    * @since Date: Jan 23, 2012
    * @param cmd the {@link IJVMCommand} to fetch the name from, may be <code>null</code>
    * @return the name of the {@link IJVMCommand},<br>
    *         or an text to indicate, that the command is unknown, if the given command is <code>null</code>
    * @see IJVMCommand#getName()
    */
   @NotNull
   private String buildNameForCommand(final IJVMCommand cmd) {
     if (cmd == null) {
       return Text.UNKNOWN_IJVM_INSTRUCTION.text();
     }
     return cmd.getName();
   }
 
   /**
    * If the command is valid, this will read all argument bytes and build a formatted string with them.
    * 
    * @since Date: Jan 23, 2012
    * @param addr the address of the command byte
    * @param cmd the fetched {@link IJVMCommand} the command byte stands for
    * @param sb the {@link StringBuilder} that stores the formatted arguments
    * @return the number of bytes read as arguments
    */
   private int readArgumentsIfAny(final int addr, final IJVMCommand cmd, final StringBuilder sb) {
     if (cmd != null) {
       return createArgumentList(cmd.getArgs(), addr, sb);
     }
     return 0;
   }
 
   /**
    * Builds the formatted {@link String} based on the arguments to read.
    * 
    * @since Date: Jan 23, 2012
    * @param args the argument list that define the number of bytes to read, may not be <code>null</code>
    * @param addr the address of the command byte
    * @param sb the {@link StringBuilder} that stores the formatted arguments
    * @return the number of bytes read as arguments
    */
   private int createArgumentList(final List<IJVMCommandArgument> args, final int addr, final StringBuilder sb) {
     int bytesRead = 0;
     for (final IJVMCommandArgument arg : args) {
       int value = 0;
       for (int i = 1; i <= arg.getNumberOfBytes(); ++i) {
        value |= this.memory.getByte(addr + bytesRead + 1) << (Byte.SIZE * (arg.getNumberOfBytes() - i));
         ++bytesRead;
       }
       sb.append(" ").append(arg.getRepresentationOfArgument(addr, value, this.memory));
     }
     return bytesRead;
   }
 
   @Override
   protected int getFirstPossibleCodeAddress() {
     return 4 * (Settings.MIC1_REGISTER_PC_DEFVAL.getValue() + 1);
   }
 
   @Override
   protected int getLastPossibleCodeAddress() {
     return refineEndOfCode(4 * (Utils.getNextHigherValue(getFirstPossibleCodeAddress(),
                                                          Settings.MIC1_REGISTER_CPP_DEFVAL.getValue(),
                                                          Settings.MIC1_REGISTER_SP_DEFVAL.getValue(),
                                                          this.memory.getSize(),
                                                          Settings.MIC1_REGISTER_LV_DEFVAL.getValue()) - 1));
   }
 
   /**
    * Returns the address of the last assembler instruction that is only followed by <code>NOP</code>s
    * 
    * @since Date: Jan 26, 2012
    * @param end the end of the code area in the memory
    * @return the address of last assembler instruction
    */
   private int refineEndOfCode(final int end) {
     final int start = getFirstPossibleCodeAddress();
     int refEnd = end;
     while (refEnd >= start && this.memory.getByte(refEnd) == 0) {
       --refEnd;
     }
     return refEnd;
   }
 
   /**
    * Returns a {@link Map} containing for each line number in the macro code a {@link String} representing the code
    * line.
    * 
    * @since Date: Apr 19, 2012
    * @return a {@link Map} containing the line numbers of macro code and the string representation of that lines.
    */
   @NotNull
   public Map<Integer, String> getCodeMap() {
     final Map<Integer, String> map = new HashMap<Integer, String>();
 
     for (int line = getFirstPossibleCodeAddress(); line <= getLastPossibleCodeAddress(); ++line) {
       line += addLineToMap(map, line);
     }
 
     return map;
   }
 
   /**
    * Creates the string representation of the given line and adds it to the given code {@link Map}.
    * 
    * @since Date: Apr 19, 2012
    * @param codeMap the map that contains line numbers and string representations of the lines
    * @param line the number of the line to add to the map
    * @return the number of bytes read as arguments of the command in the given line
    */
   private int addLineToMap(final Map<Integer, String> codeMap, final int line) {
     final StringBuilder formattedArgs = new StringBuilder();
 
     final int cmdCode = this.memory.getByte(line);
     final IJVMCommand cmd = lookupCommand(cmdCode);
 
     final String name = buildNameForCommand(cmd);
     final int bytesRead = readArgumentsIfAny(line, cmd, formattedArgs);
 
     final String formattedCmdCode = formatIntToHex(cmdCode, Settings.MIC1_MEM_MICRO_ADDR_WIDTH.getValue());
 
     codeMap.put(Integer.valueOf(line), Text.MACRO_CODE_PLAIN.text(formattedCmdCode, name, formattedArgs.toString()));
     return bytesRead;
   }
 }
