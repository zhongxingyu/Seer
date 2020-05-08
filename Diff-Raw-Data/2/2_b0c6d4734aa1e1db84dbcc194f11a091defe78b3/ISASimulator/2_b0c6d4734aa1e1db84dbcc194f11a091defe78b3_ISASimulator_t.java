 import java.util.ArrayList;
 import java.util.concurrent.LinkedBlockingQueue; // for channels
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.math.BigInteger;
 import java.util.Iterator;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 public class ISASimulator {
 
   public static final int D_MEM_SIZE = 8192;
   public static final int I_MEM_SIZE = 8192;
   public static final int NUM_CHANNELS = 4;
 
   // constant values that depends on your ISA
   // TODO: modify this
   public static final int REG_FILE_SIZE = 16;
   public static final int OPCODE_LENGTH = 4;
 
   private int channel_buffer_size;
 
   private int PC; // current program counter
   private int inst_count; // number of instructions we have executed for the
                           // simulator
 
   private String[] inst_mem; // inst_mem is kept in a string for easier parsing
                              // of opcode/operands/etc
   private Int34[] data_mem;
 
   private Int34[] reg_file;
 
   private ArrayList<LinkedBlockingQueue<Int34>> channels; // Array of Linked
                                                           // Lists (LL) which
                                                           // each list acting as
                                                           // a channel.
 
   // default constructor
   public ISASimulator() {
     // init PC to 0
     PC = 0;
 
     channel_buffer_size = 16; // set default channel buffer size
 
     // initialize memories
     inst_mem = new String[I_MEM_SIZE];
     data_mem = new Int34[D_MEM_SIZE];
 
     initMem(true);
     initMem(false);
 
     // initialize register file
     reg_file = new Int34[REG_FILE_SIZE];
 
     initRegFile(); // clear registers
 
     // init channels
     channels = new ArrayList<LinkedBlockingQueue<Int34>>(NUM_CHANNELS);
     for (int i = 0; i < NUM_CHANNELS; ++i)
       channels.add(new LinkedBlockingQueue<Int34>());
 
     // clearChannels();
 
     // init $SP, init $0
     setReg(14, new Int34(1, (long) 8191));
     setReg(0, new Int34(0, (long) 0));
   }
 
   // reset everythinig to it's initial state (i.e. memory/registers/channels
   // cleared and PC = 0)
   public void resetSimulator() {
     PC = 0;
 
     // reset memory
     clearMem(true);
     clearMem(false);
 
     // reset register file
     clearRegFile();
 
     // init $SP, init $0
     setReg(14, new Int34(1, (long) 8191));
     setReg(0, new Int34(0, (long) 0));
 
     // make sure we have the default channel size
     channel_buffer_size = 16;
     setBufferSize(channel_buffer_size); // this also clears the channels
   }
 
   // initialize the memory (create and zero out)
   public void initMem(boolean imem) {
     if (imem) {
       for (int i = 0; i < I_MEM_SIZE; ++i)
         inst_mem[i] = new String("00000000000000000");
     } else {
       for (int i = 0; i < D_MEM_SIZE; ++i)
         data_mem[i] = new Int34(0L);
     }
   }
 
   // clears the memory: imem = true means inst memory, false = data mem
   public void clearMem(boolean imem) {
     if (imem) {
       for (int i = 0; i < I_MEM_SIZE; ++i)
         inst_mem[i] = "00000000000000000";
     } else {
       for (int i = 0; i < D_MEM_SIZE; ++i)
         data_mem[i].create(0, 0L);
     }
   }
 
   // init the reg files to all 0's
   public void initRegFile() {
     for (int i = 0; i < REG_FILE_SIZE; ++i)
       reg_file[i] = new Int34(0L);
   }
 
   // clears the register file
   public void clearRegFile() {
     for (int i = 0; i < REG_FILE_SIZE; ++i)
       reg_file[i].create(0, 0L);
   }
 
   // clears all channels
   public void clearChannels() {
     for (int i = 0; i < NUM_CHANNELS; ++i)
       clearChannel(i);
   }
 
   // clear a specific channel (chan_num)
   public void clearChannel(int chan_num) {
     // sanity check on input
     if (chan_num < 0 || chan_num >= NUM_CHANNELS) {
       System.out.println("Invalid channel number: " + chan_num);
       return;
     }
 
     channels.get(chan_num).clear();
   }
 
   /*
    * Description: loads either imem (imem = true) or dmem with a COE file
    * (starting at address "start_addr")
    * 
    * Returns: true if successful, false otherwise
    * 
    * Note: dmem expects the input to be in 2's complement format... this is a
    * bit hackish as sometimes the memory location will be a pointer and
    * therefore not really be 2's complement. However, we can (reasonably) assume
    * that this won't be a problem because the addresses used will never be large
    * enough to have a MSB of 1 (since the memory is only 8K)
    */
   public boolean loadMem(String coe, int start_addr, boolean imem) {
     int curr_addr = start_addr; // which address we are currently pointing to
     int curr_line = -1; // line number of the file that is currently read in
     String line, tmp;
     int radix = 16; // radix being used... default is 16
 
     BufferedReader in = null;
 
     // load COE file
     try {
       in = new BufferedReader(new FileReader(coe));
     } catch (Exception e) {
       System.out.println("Couldn't open file: " + coe + ". Load failed.");
       return false;
     }
 
     // read in first 2 lines of COE, which have a specific format
     try {
       line = in.readLine(); // this line should be
                             // MEMORY_INITIALIZATION_RADIX=XXX
       curr_line++;
       if (!line.contains("MEMORY_INITIALIZATION_RADIX=")) {
         System.out
             .println("Expected MEMORY_INITIALIZATION_RADIX=...; on line 0 of file. Load failed.");
         return false;
       }
 
       // extract the radix value from the string
       String radix_string = line.split("=")[1];
       radix_string = radix_string.substring(0, radix_string.length() - 1); // strip
                                                                            // off
                                                                            // the
                                                                            // trailing
                                                                            // ";"
       radix = Integer.valueOf(radix_string).intValue(); // convert to int
 
       // only support binary and hex for now
       if (radix != 2 && radix != 16) {
         System.out
             .println("Radix format must be 2 (binary) or 16 (decimal). Load failed.");
         return false;
       }
 
       line = in.readLine(); // this line should be MEMORY_INITIALIZATION_VECTOR=
       curr_line++;
       if (!line.contentEquals("MEMORY_INITIALIZATION_VECTOR=")) {
         System.out
             .println("line 1 must be \"MEMORY_INITIALIZATION_VECTOR=\". Load failed.");
         return false;
       }
     } catch (Exception e) {
       System.out.println("Error reading input file. Load failed.");
       return false;
     }
 
     // loop through file and get all the info
     try {
       line = in.readLine();
       curr_line++;
 
       while (line != null) { // stop when we read EOF
         if ((imem && curr_addr >= I_MEM_SIZE)
             || (!imem && curr_addr >= D_MEM_SIZE)) {
           System.out
               .println("Too many addresses specified in COE file. Load failed.");
           return false;
         }
 
         // if imem then we simply set the mem location as the binary version of
         // the string (minus the trailing ',')
         // if radix isn't binary, then conversion to binary must take place here
         // must take place here
         tmp = "";
         if (imem) {
           // strip off trailing ',' if there is one (should be EOF soon if not
           // one)
           if (line.substring(line.length() - 1, line.length()).equals(","))
             line = line.substring(0, line.length() - 1);
 
           // make sure the string is the correct length (based on radix)
           if ((radix == 2 && line.length() != 17)
               || (radix == 16 && line.length() != 5)) {
             System.out.println(line + " (line " + curr_line
                 + ") has incorrect format. Load failed.");
             return false;
           }
 
           // radix 2 means we just copy the string over to the imem location
           if (radix == 2) {
             inst_mem[curr_addr] = line;
             curr_addr++;
           } else { // need to convert each digit to binary string first
             // since our instruction length is not a multiple of 4, the first
             // hex digit must be 0 or 1
             if (line.charAt(0) != '0' && line.charAt(0) != '1') {
               System.out.println(line + " (line " + curr_line
                   + "): First hex value must be 0 or 1. Load failed.");
               return false;
             } else { // tack on first value
               if (line.charAt(0) == '0')
                 tmp = "0";
               else
                 tmp = "1";
             }
 
             String hexString = "";
             // loop through and convert all the values
             for (int i = 1; i < line.length(); ++i) {
               hexString = hexToBin(line.charAt(i));
               if (hexString.length() == 0) { // make sure the value was 0 - f
                                              // and not some random char
                 System.out.println(line + " (line " + curr_line
                     + "): Non-Hex value encountered. Load failed.");
                 return false;
               }
               tmp += hexString;
             }
 
             inst_mem[curr_addr] = tmp;
             curr_addr++;
           }
         }
 
         else { // convert to Int34 before loading into dmem
           boolean isPositive = true; // is this a positive value?
 
           if (line.substring(line.length() - 1, line.length()).equals(","))
             line = line.substring(0, line.length() - 1); // strip off the
                                                          // trailing ','
 
           // make sure the string is the correct length (based on radix)
           if ((radix == 2 && line.length() != 34)
               || (radix == 16 && line.length() != 9)) {
             System.out.println(line + " (line " + curr_line
                 + ") has incorrect format. Load failed.");
             return false;
           }
 
           // radix 2 means we just copy the string over to the imem location
           if (radix == 2)
             tmp = line;
 
           else { // need to convert each digit to binary string first
             // manually convert the first digit
             // if first binary digit is 0, then it is a positive number, else it
             // is negative
             if (line.charAt(0) == '0') {
               tmp = "00";
               isPositive = true;
             } else if (line.charAt(0) == '1') {
               tmp = "01";
               isPositive = true;
             } else if (line.charAt(0) == '2') {
               tmp = "10";
               isPositive = false;
             } else if (line.charAt(0) == '3') {
               tmp = "11";
               isPositive = false;
             } else {
               System.out.println(line + " (line " + curr_line
                   + "): First hex value must be 0, 1, 2, or 3. Load failed.");
               return false;
             }
 
             String hexString = "";
             // loop through and convert all the values
             for (int i = 1; i < line.length(); ++i) {
               hexString = hexToBin(line.charAt(i));
               if (hexString.length() == 0) { // make sure the value was 0 - f
                                              // and not some random char
                 System.out.println(line + " (line " + curr_line
                     + "): Non-Hex value encountered. Load failed.");
                 return false;
               }
               tmp += hexString;
             }
 
           }
 
           Int34 line_value;
 
           // if the number was positive, we can just convert string directly to
           // big integer
           if (isPositive) {
             BigInteger thisNum = new BigInteger(tmp, 2);
             line_value = new Int34(thisNum.signum(), thisNum.longValue());
           }
           // if negative, we first need to do twos complement
           else {
             boolean flip = false;
             String twos_comp = "";
             // leave all LSBs up to the first '1' unchanged, then flip them all
             for (int i = tmp.length() - 1; i >= 0; --i) {
               if (!flip) {
                 twos_comp = tmp.charAt(i) + twos_comp;
                 if (tmp.charAt(i) == '1')
                   flip = true; // start flipping from now on
               } else if (tmp.charAt(i) == '0')
                 twos_comp = '1' + twos_comp;
               else
                 twos_comp = '0' + twos_comp;
             }
 
             // tack on a negative sign
             twos_comp = "-" + twos_comp;
 
             // BigInteger thisNum = new BigInteger(twos_comp,2);
             // line_value = new Int34(thisNum.signum(),thisNum.longValue());
             line_value = new Int34(Long.parseLong(twos_comp, 2));
           }
 
           data_mem[curr_addr] = line_value;
           curr_addr++;
         }
 
         line = in.readLine(); // read in next line
         curr_line++;
       } // end while
 
       in.close();
     } catch (Exception e) {
       System.out.println("Error reading input file. Load failed.");
       return false;
     }
 
     return true;
   }
 
   // given a character (0 - f) returns a string of the binary representation of
   // that num
   public String hexToBin(char hex_char) {
     String result;
     switch (Character.digit(hex_char, 16)) {
     case 0:
       result = "0000";
       break;
     case 1:
       result = "0001";
       break;
     case 2:
       result = "0010";
       break;
     case 3:
       result = "0011";
       break;
     case 4:
       result = "0100";
       break;
     case 5:
       result = "0101";
       break;
     case 6:
       result = "0110";
       break;
     case 7:
       result = "0111";
       break;
     case 8:
       result = "1000";
       break;
     case 9:
       result = "1001";
       break;
     case 10:
       result = "1010";
       break;
     case 11:
       result = "1011";
       break;
     case 12:
       result = "1100";
       break;
     case 13:
       result = "1101";
       break;
     case 14:
       result = "1110";
       break;
     case 15:
       result = "1111";
       break;
     default:
       result = "";
       break;
     }
 
     return result;
   }
 
   public void printIMem() {
     printIMem(0, I_MEM_SIZE - 1);
   }
 
   public void printDMem() {
     printDMem(0, D_MEM_SIZE - 1);
   }
 
   // disassembles instructions starting at start_addr and going to
   // start_addr+range
   public void printIMem(int start_addr, int range) {
     // sanity check of inputs
     if (range < 0) {
       System.out.println("Range must be positive.");
       return;
     }
     if (start_addr + range >= I_MEM_SIZE) {
       System.out.println("startaddr + size must be less than " + I_MEM_SIZE);
       return;
     }
 
     // loop through and print values
     for (int i = start_addr; i < start_addr + range; ++i)
       System.out.println("IMEM[" + i + "]: " + inst_mem[i]);
   }
 
   // prints data mem contents from start_addr to start_addr+range
   public void printDMem(int start_addr, int range) {
     // sanity check of inputs
     if (range < 0) {
       System.out.println("Range must be non-negative.");
       return;
     }
     if (start_addr + range >= D_MEM_SIZE) {
       System.out.println("startaddr + size must be less than " + D_MEM_SIZE);
       return;
     }
 
     // loop through and print values
     for (int i = start_addr; i <= start_addr + range; ++i)
       System.out.println("DMEM[" + i + "]: " + data_mem[i]);
   }
 
   // prints the contents of all the registers
   public void printRegFile() {
     for (int i = 0; i < REG_FILE_SIZE; ++i)
       System.out.println(i + ": " + reg_file[i]);
   }
 
   // print out a specific channel
   public void printChannel(int chan_num) {
     // sanity check on input
     if (chan_num < 0 || chan_num >= NUM_CHANNELS) {
       System.out.println("Invalid channel number: " + chan_num);
       return;
     }
 
     Iterator<Int34> chan_vals = channels.get(chan_num).iterator(); // get an
                                                                    // iterator
                                                                    // for this
                                                                    // buffer
 
     int index = 0;
 
     while (chan_vals.hasNext()) {
       System.out.println(index + ": " + chan_vals.next());
       index++;
     }
   }
 
   // set specific register to a specific value
   public void setReg(int reg_num, Int34 value) {
     // sanity check of input
     if (reg_num < 0 || reg_num >= REG_FILE_SIZE) {
       System.out.println("Invalid register number: " + reg_num);
       return;
     }
 
     reg_file[reg_num] = value;
   }
 
   // set specific imem location (addr) to a specific value
   public void setIMem(int addr, String value) {
     // sanity check of input
     if (addr < 0 || addr >= I_MEM_SIZE) {
       System.out.println("Address out of range: " + addr);
       return;
     }
 
     if (!isIMemFormat(value)) {
       System.out.println("Incorrect format: " + value);
       return;
     }
 
     inst_mem[addr] = value;
   }
 
   // makes sure that this is a 17 bit binary number in string format
   public boolean isIMemFormat(String value) {
     if (value.length() != 17)
       return false; // not 17 bits
 
     // see if all chars are 0 or 1
     for (int i = 0; i < 17; ++i) {
       if (value.charAt(i) != '0' && value.charAt(i) != '1')
         return false;
     }
 
     return true;
   }
 
   // set specific dmem location (addr) to a specific value
   public void setDMem(int addr, Int34 value) {
     // sanity check of input
     if (addr < 0 || addr >= D_MEM_SIZE) {
       System.out.println("Address out of range: " + addr);
       return;
     }
 
     data_mem[addr] = value;
   }
 
   // adds a specific value to buffer of specified channel (chan_num)
   // returns true if added successfully, false otherwise
   public boolean addToChannel(int chan_num, Int34 value) {
     // sanity check of input
     if (chan_num < 0 || chan_num >= NUM_CHANNELS) {
       System.out.println("Invalid channel number: " + chan_num);
       return false;
     }
 
     return channels.get(chan_num).offer(value); // returns false if no room in
                                                 // the buffer
   }
 
   // resizes the channel buffers. also clears channels (so make sure there isn't
   // anything important there)
   public void setBufferSize(int size) {
     if (size < 0) {
       System.out.println("buffer size must be non-negative");
       return;
     }
 
     channel_buffer_size = size;
 
     for (int i = 0; i < NUM_CHANNELS; ++i)
       channels.get(i).clear();
   }
 
   // sign extend a string to a given length (assumes (i.e. doesn't check) it is
   // a string of 0's and 1's)
   public String signExtend(String value, int length) {
     String sign_bit = value.substring(0, 1);
 
     while (value.length() < length)
       value = sign_bit + value;
 
     return value;
   }
 
   // converts binary string into an Int34
   // assumes input value is strictly a binary number (doesn't check it)
   public Int34 twosCompValue(String value) {
     // examine first bit to see if it's positive or negative
     if (value.charAt(0) == '0') { // positive number
       return new Int34(Long.parseLong(value, 2));
     } else { // negative number
 
       boolean flip = false;
       String twos_comp = "";
       // leave all LSBs up to the first '1' unchanged, then flip them all
       for (int i = value.length() - 1; i >= 0; --i) {
         if (!flip) {
           twos_comp = value.charAt(i) + twos_comp;
           if (value.charAt(i) == '1')
             flip = true; // start flipping from now on
         } else if (value.charAt(i) == '0')
           twos_comp = '1' + twos_comp;
         else
           twos_comp = '0' + twos_comp;
       }
 
       // tack on a negative sign
       twos_comp = "-" + twos_comp;
 
       return new Int34(Long.parseLong(twos_comp, 2));
     }
   }
 
   // execute num_insts instruction
   public void execute(int num_insts) {
     // sanity check of inputs
     if (num_insts < 1) {
       System.out.println("Number of instructions must be positive.");
       return;
     }
 
     int num_done = 0; // number of instructions we have completed so far
     String curr_inst; // the current instruction
     String opcode_str; // string representing the opcode
     int opcode; // the opcode in integer form (so we can use a case statement)
     int r1, r2, imm;
 
     int funcCode;
     Int34 tmpI;
 
     while (num_done < num_insts) {
       curr_inst = inst_mem[PC].substring(3, 17); // get the next instruction
 
       opcode_str = curr_inst.substring(0, OPCODE_LENGTH); // get the op-code
                                                           // bits
       opcode = Integer.parseInt(opcode_str, 2);
 
       // TODO: complete this
       switch (opcode) {
       // add, sub, nor, mv
       case 0:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         r2 = Integer.parseInt(curr_inst.substring(8, 12), 2);
         funcCode = Integer.parseInt(curr_inst.substring(12, 14), 2);
         switch (funcCode) {
         // add
         case 0:
           tmpI = reg_file[r1].add(reg_file[r2]);
           setReg(r1, tmpI);
           PC++;
           break;
         // sub
         case 1:
           tmpI = reg_file[r1].subtract(reg_file[r2]);
           setReg(r1, tmpI);
           PC++;
           break;
         // nor
         case 2:
           tmpI = reg_file[r1].or(reg_file[r2]).not();
           setReg(r1, tmpI);
           PC++;
           break;
         // mv
         case 3:
           tmpI = reg_file[r2];
           setReg(r1, tmpI);
           PC++;
           break;
         }
         break;
       // in, out
       case 1:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         r2 = Integer.parseInt(curr_inst.substring(8, 12), 2);
         funcCode = Integer.parseInt(curr_inst.substring(12, 14), 2);
         switch (funcCode) {
         // in
         case 0:
           try {
             tmpI = channels.get((int) reg_file[r2].longValue()).take();
             setReg(r1, tmpI);
           } catch (InterruptedException e) {
             System.err.println("LOL INTERRUPTED EXCEPTION IDK WHAT TO DO");
             e.printStackTrace();
             System.exit(1);
           }
           PC++;
           break;
         // out
         case 1:
           channels.get((int) reg_file[r2].longValue()).offer(reg_file[r1]);
           PC++;
           break;
         }
         break;
       /*
        * case 2: break; case 3: break;
        */
       // lix
       case 4:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         r2 = Integer.parseInt(curr_inst.substring(8, 12), 2);
         imm = Integer.parseInt(curr_inst.substring(12, 14), 2);
         reg_file[6 + imm] = data_mem[(int) (reg_file[r1].longValue() + reg_file[r2]
             .longValue())];
         PC++;
         break;
       // six
       case 5:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         r2 = Integer.parseInt(curr_inst.substring(8, 12), 2);
         imm = Integer.parseInt(curr_inst.substring(12, 14), 2);
         data_mem[(int) (reg_file[r1].longValue() + reg_file[r2].longValue())] = reg_file[6 + imm];
         PC++;
         break;
       // lw
       case 6:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         r2 = Integer.parseInt(curr_inst.substring(8, 12), 2);
         imm = Integer.parseInt(curr_inst.substring(12, 14), 2);
         long test = reg_file[r2].longValue() + imm;
         reg_file[r1] = data_mem[(int) (reg_file[r2].longValue() + imm)];
         PC++;
         break;
       // sw
       case 7:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         r2 = Integer.parseInt(curr_inst.substring(8, 12), 2);
         imm = Integer.parseInt(curr_inst.substring(12, 14), 2);
         data_mem[(int) (reg_file[r2].longValue() + imm)] = reg_file[r1];
         PC++;
         break;
       // j, jal
       case 8:
         long imm1 = twosCompValue(curr_inst.substring(4, 13)).longValue();
         imm = (int) twosCompValue(curr_inst.substring(4, 13)).longValue();
         funcCode = Integer.parseInt(curr_inst.substring(13, 14), 2);
         // j
        if (funcCode == 0) {
           PC += imm;
         }
         // jal
         else {
           setReg(15, new Int34(1, (long) PC + 1));
           PC += imm;
         }
         break;
       // sra, jor
       case 9:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         imm = Integer.parseInt(curr_inst.substring(8, 12), 2);
         funcCode = Integer.parseInt(curr_inst.substring(12, 14), 2);
         switch (funcCode) {
         // sra
         case 1:
           tmpI = reg_file[r1].shiftRight(imm);
           setReg(r1, tmpI);
           PC++;
           break;
         // jor
         case 3:
           PC = (int) (PC + reg_file[r1].longValue() + imm);
           break;
         }
         break;
       // sloi
       case 10:
         imm = Integer.parseInt(curr_inst.substring(4, 14), 2);
         tmpI = reg_file[12].shiftLeft(10);
         tmpI = tmpI.or(new Int34((long) imm));
         setReg(12, tmpI);
         PC++;
         break;
       // jr
       case 11:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         PC = (int) (reg_file[r1].longValue());
         break;
       // bne
       case 12:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         imm = (int) twosCompValue(curr_inst.substring(8, 14)).longValue();
         if (reg_file[r1].longValue() != reg_file[9].longValue()) {
           PC += imm;
         } else {
           PC++;
         }
         break;
       // blt
       case 13:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         imm = (int) twosCompValue(curr_inst.substring(8, 14)).longValue();
         if (reg_file[r1].longValue() < reg_file[9].longValue()) {
           PC += imm;
         } else {
           PC++;
         }
         break;
       // addi
       case 14:
         r1 = Integer.parseInt(curr_inst.substring(4, 8), 2);
         tmpI = twosCompValue(curr_inst.substring(8, 14));
         tmpI = reg_file[r1].add(tmpI);
         setReg(r1, tmpI);
         PC++;
         break;
       // halt
       case 15:
         System.out.println("Execution halted");
         return;
       default:
         System.err.println("invalid opcode encountered at PC=" + PC);
         return;
       }
 
       inst_count++; // increase our global counter
 
       num_done++; // just finished another instruction
     }
   }
 
   // while loop asking for user input for next command
   public void run() {
     // set up to read user input from console
     BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
 
     if (cons == null) {
       System.out.println("No console available. Quitting.");
       System.exit(1);
     }
 
     /*
      * InputStreamReader input_stream = null; try { input_stream = new
      * InputStreamReader(System.in); } catch(Exception e) {
      * System.out.println("Could not get command line input. Quitting.");
      * System.exit(1); }
      */
 
     String input = null;
     StringTokenizer input_tokens = null;
     String curr_token;
 
     while (true) {
       System.out.print(">> "); // "command prompt"
 
       try {
         input = cons.readLine(); // get input from user
       } catch (Exception e) {
         System.out.println("Couldn't read input.  Bye.");
         System.exit(1);
       }
 
       input_tokens = new StringTokenizer(input); // tokenize the input for
                                                  // easier parsing
 
       // make sure it is a valid command and do that command
       if (input_tokens.hasMoreTokens())
         curr_token = input_tokens.nextToken();
       else
         continue;
 
       if (curr_token.equals("iload")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: iload $file_name $start_addr");
         else {
           String file_name = input_tokens.nextToken();
           int start_addr = Integer.parseInt(input_tokens.nextToken());
           loadMem(file_name, start_addr, true);
         }
       } else if (curr_token.equals("dload")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: dload $file_name $start_addr");
         else {
           String file_name = input_tokens.nextToken();
           int start_addr = Integer.parseInt(input_tokens.nextToken());
           loadMem(file_name, start_addr, false);
         }
       } else if (curr_token.equals("go")) {
         if (input_tokens.countTokens() != 1)
           System.out.println("usage: go $number");
         else {
           int number = Integer.parseInt(input_tokens.nextToken());
           execute(number);
         }
       } else if (curr_token.equals("dump_reg")) {
         printRegFile();
       } else if (curr_token.equals("set_reg")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: set_reg $reg_num $value");
         else {
           int reg_num = Integer.parseInt(input_tokens.nextToken());
           Int34 value = new Int34(Long.parseLong(input_tokens.nextToken()));
           setReg(reg_num, value);
         }
       } else if (curr_token.equals("dump_imem")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: dump_imem $start_addr $range");
         else {
           int start_addr = Integer.parseInt(input_tokens.nextToken());
           int range = Integer.parseInt(input_tokens.nextToken());
           printIMem(start_addr, range);
         }
       } else if (curr_token.equals("set_imem")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: set_imem $addr $value");
         else {
           int addr = Integer.parseInt(input_tokens.nextToken());
           String value = input_tokens.nextToken();
           setIMem(addr, value);
         }
       } else if (curr_token.equals("dump_dmem")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: dump_dmem $start_addr $range");
         else {
           int start_addr = Integer.parseInt(input_tokens.nextToken());
           int range = Integer.parseInt(input_tokens.nextToken());
           printDMem(start_addr, range);
         }
       } else if (curr_token.equals("set_dmem")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: set_dmem $addr $value");
         else {
           int addr = Integer.parseInt(input_tokens.nextToken());
           Int34 value = new Int34(Long.parseLong(input_tokens.nextToken()));
           setDMem(addr, value);
         }
       } else if (curr_token.equals("dump_channel")) {
         if (input_tokens.countTokens() != 1)
           System.out.println("usage: dump_channel $chan_number");
         else {
           int chan_num = Integer.parseInt(input_tokens.nextToken());
           printChannel(chan_num);
         }
       } else if (curr_token.equals("put_channel")) {
         if (input_tokens.countTokens() != 2)
           System.out.println("usage: put_channel $chan_number $value");
         else {
           int chan_num = Integer.parseInt(input_tokens.nextToken());
           Int34 value = new Int34(Long.parseLong(input_tokens.nextToken()));
           addToChannel(chan_num, value);
         }
       } else if (curr_token.equals("clear_channel")) {
         if (input_tokens.countTokens() != 1)
           System.out.println("usage: clear_channel $chan_number");
         else {
           int chan_num = Integer.parseInt(input_tokens.nextToken());
           clearChannel(chan_num);
         }
       } else if (curr_token.equals("set_buf_size")) {
         if (input_tokens.countTokens() != 1)
           System.out.println("usage: set_buf_size $size");
         else {
           int size = Integer.parseInt(input_tokens.nextToken());
           setBufferSize(size);
         }
       } else if (curr_token.equals("instr_count")) {
         System.out.println(inst_count + " instructions executed so far");
       } else if (curr_token.equals("dump_pc")) {
         System.out.println("current PC is " + PC);
       } else if (curr_token.equals("reset")) {
         resetSimulator();
       } else if (curr_token.equals("exit")) {
         System.out.println("leaving so soon? ... Bye!");
         break;
       } else {
         System.out.println("unrecognized command.");
       }
     }
   }
 
   public static void main(String[] args) {
     ISASimulator sim = new ISASimulator();
 
     sim.run(); // run the simulator
   }
 }
